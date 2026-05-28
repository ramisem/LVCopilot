/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.PlanItemList;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class StabilityUtil {
    private String LABVANTAGE_CVS_ID = "$Revision: 88505 $";

    public static String getOptionsString(PageContext pageContext, String sqlStmt, String descColumnid, String valueColumnid) {
        QueryProcessor qp = null;
        DataSet ds = null;
        qp = new QueryProcessor(pageContext);
        ds = qp.getSqlDataSet(sqlStmt);
        return StabilityUtil.getOptionsString(ds, descColumnid, valueColumnid);
    }

    public static String getOptionsString(PageContext pageContext, String sqlStmt, String descColumnid, String valueColumnid, SafeSQL safeSQL) {
        QueryProcessor qp = null;
        DataSet ds = null;
        qp = new QueryProcessor(pageContext);
        ds = qp.getPreparedSqlDataSet(sqlStmt, safeSQL.getValues());
        return StabilityUtil.getOptionsString(ds, descColumnid, valueColumnid);
    }

    public static String getOptionsString(DataSet ds, String descColumnid, String valueColumnid) {
        StringBuffer optionsBuff = new StringBuffer();
        int rowCount = ds.getRowCount();
        for (int count = 0; count < rowCount; ++count) {
            String value = ds.getValue(count, valueColumnid);
            String description = ds.getValue(count, descColumnid);
            optionsBuff.append("<OPTION VALUE = '");
            optionsBuff.append(value);
            optionsBuff.append("' > ");
            optionsBuff.append(description);
            optionsBuff.append("</OPTION>");
        }
        return optionsBuff.toString();
    }

    public static DataSet getPlanItemWorkItems(String schedulePlanId, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        sql.append("select w.workitemid, w.quantitytype, w.quantityunit, c.conditionlabel, r.schedulerule, p.scheduleplandesc ").append(" from scheduleplanitemworkitem w , scheduleplanitem spi, schedulecondition c, scheduletimerule r, scheduleplan p ").append(" where w.scheduleplanid = spi.scheduleplanid and w.scheduleplanitemid = spi.scheduleplanitemid  and spi.scheduleplanid = c.scheduleplanid ").append(" and spi.scheduleconditionid = c.scheduleconditionid and r.scheduleplanid=spi.scheduleplanid and r.scheduletimeruleid = spi.scheduletimeruleid ").append(" and p.scheduleplanid=w.scheduleplanid and w.scheduleplanid = ?");
        DataSet ds = qp.getPreparedSqlDataSet("getplanworkitems", sql.toString(), new Object[]{schedulePlanId});
        return ds;
    }

    public static DataSet getPlanItems(String schedulePlanId, QueryProcessor qp) {
        DataSet ds = qp.getPreparedSqlDataSet("getplanitems", "select  i.scheduleplanid, i.valuetree, i.propertytreeid, i.scheduleconditionid, c.conditionlabel, r.schedulerule, p.scheduleplandesc  from scheduleplanitem i, scheduletimerule r, schedulecondition c, scheduleplan p  where c.scheduleplanid = i.scheduleplanid and c.scheduleconditionid = i.scheduleconditionid and r.scheduleplanid = i.scheduleplanid and r.scheduletimeruleid = i.scheduletimeruleid and p.scheduleplanid=i.scheduleplanid and i.scheduleplanid = ?", new Object[]{schedulePlanId}, true);
        return ds;
    }

    public static DataSet getPlanConditions(String schedulePlanId, QueryProcessor qp) {
        DataSet ds = qp.getPreparedSqlDataSet("getplanconditions", "select  c.scheduleconditionid, c.conditionlabel, c.qtypulltype, c.qtypullunits, c.qtyreservetype, c.qtyreserveunits, p.scheduleplandesc  from schedulecondition c, scheduleplan p  where c.scheduleplanid = p.scheduleplanid and p.scheduleplanid = ? and ( qtypulltype != 'C' OR qtyreservetype != 'C' )", new Object[]{schedulePlanId});
        return ds;
    }

    public static void validatePlanItemWorkItemUnits(DataSet dsWorkItems, String containerUnit, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        for (int d = 0; d < dsWorkItems.getRowCount(); ++d) {
            String workItemId = dsWorkItems.getValue(d, "workitemid");
            if (!"U".equals(dsWorkItems.getValue(d, "quantitytype"))) continue;
            String wiQtyUnit = dsWorkItems.getValue(d, "quantityunit", "");
            String timeRule = dsWorkItems.getValue(d, "schedulerule", "");
            String conditionLabel = dsWorkItems.getValue(d, "conditionlabel", "");
            String planDesc = dsWorkItems.getValue(d, "scheduleplandesc", "");
            if (wiQtyUnit.length() <= 0 || containerUnit == null || containerUnit.length() <= 0 || containerUnit.equals(wiQtyUnit) || UnitsUtil.isUnitCompatible(qp, wiQtyUnit, containerUnit)) continue;
            HashMap<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
            valueMap.put("wiQtyUnit", "<b>'" + wiQtyUnit + "'</b>");
            valueMap.put("planDesc", "<b>'" + planDesc + "'</b>");
            valueMap.put("workItemId", "<b>'" + workItemId + "'</b>");
            valueMap.put("conditionLabel", "<b>'" + conditionLabel + "'</b>");
            valueMap.put("timeRule", "<b>'" + timeRule + "'</b>");
            throw new SapphireException("VALIDATION", "\n " + tp.translate("Unit conversion not defined between the Container Unit [containerUnit] and the Unit [wiQtyUnit] specified in Plan [planDesc], Test Method [workItemId], Condition [conditionLabel], Time Point [timeRule].", valueMap));
        }
    }

    public static void validatePlanItemPullAmountUnits(DataSet dsPlanItems, String containerUnit, QueryProcessor qp, TranslationProcessor tp, String unitId) throws SapphireException {
        if (unitId == null || unitId.length() == 0) {
            unitId = "Container";
        }
        for (int d = 0; d < dsPlanItems.getRowCount(); ++d) {
            String valueTree = dsPlanItems.getClob(d, "valuetree", "");
            String timeRule = dsPlanItems.getValue(d, "schedulerule", "");
            String conditionLabel = dsPlanItems.getValue(d, "conditionlabel", "");
            String propertyTreeId = dsPlanItems.getValue(d, "propertytreeid", "");
            String conditionId = dsPlanItems.getValue(d, "scheduleconditionid", "");
            String planId = dsPlanItems.getValue(d, "scheduleplanid", "");
            String planDesc = dsPlanItems.getValue(d, "scheduleplandesc", "");
            if (valueTree.length() <= 0) continue;
            PropertyList pl = new PropertyList();
            pl.setPropertyList(valueTree);
            PropertyList pullAmount = pl.getPropertyListNotNull("pullamount");
            String amountUnit = pullAmount.getProperty("units");
            if (amountUnit.length() == 0) {
                amountUnit = ScheduleGridUtil.getAmountUnitFromAncestors(planId, conditionId, propertyTreeId, qp);
            }
            if (amountUnit.length() <= 0 || "(Containers)".equals(amountUnit) || containerUnit == null || containerUnit.length() <= 0 || containerUnit.equals(amountUnit) || UnitsUtil.isUnitCompatible(qp, amountUnit, containerUnit)) continue;
            HashMap<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
            valueMap.put("unitId", unitId);
            valueMap.put("planDesc", "<b>'" + planDesc + "'</b>");
            valueMap.put("amountUnit", "<b>'" + amountUnit + "'</b>");
            valueMap.put("conditionLabel", "<b>'" + conditionLabel + "'</b>");
            valueMap.put("timeRule", "<b>'" + timeRule + "'</b>");
            throw new SapphireException("VALIDATION", "\n" + tp.translate("Unit conversion not defined between the [unitId] Unit [containerUnit] and the Pull Amount Unit [amountUnit], specified in Plan [planDesc], at Time Point [timeRule], Condition [conditionLabel].", valueMap));
        }
    }

    public static DataSet getPlanWorkItemsByTrackItem(DBAccess db, String trackItemId) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select w.workitemid, w.quantitytype, w.quantityunit,c.conditionlabel, r.schedulerule, p.scheduleplandesc ").append(" from scheduleplanitemworkitem w, schedulecondition_trackitem sct, scheduleplanitem spi, schedulecondition c ,scheduletimerule r, scheduleplan p ").append(" where w.scheduleplanid = spi.scheduleplanid and w.scheduleplanitemid = spi.scheduleplanitemid ").append(" and spi.scheduleplanid = sct.scheduleplanid and spi.scheduleconditionid = sct.scheduleconditionid ").append("  and c.scheduleplanid=sct.scheduleplanid and c.scheduleconditionid=sct.scheduleconditionid and r.scheduleplanid = spi.scheduleplanid ").append(" and r.scheduletimeruleid = spi.scheduletimeruleid and p.scheduleplanid=sct.scheduleplanid and sct.trackitemid = ? and w.quantitytype != 'C'");
        db.createPreparedResultSet("getStudyConditionWorkItems", sql.toString(), new Object[]{trackItemId});
        DataSet ds = new DataSet(db.getResultSet("getStudyConditionWorkItems"));
        return ds;
    }

    public static DataSet getPlanItemsByTrackItem(String trackItemId, QueryProcessor qp) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select spi.scheduleplanid, spi.valuetree, c.scheduleconditionid, spi.propertytreeid, c.conditionlabel, r.schedulerule,p.scheduleplandesc ").append(" from  schedulecondition_trackitem sct, scheduleplanitem spi, schedulecondition c ,scheduletimerule r, scheduleplan p ").append(" where spi.scheduleplanid = sct.scheduleplanid and spi.scheduleconditionid = sct.scheduleconditionid and sct.trackitemid = ? ").append(" and c.scheduleplanid=sct.scheduleplanid and c.scheduleconditionid=sct.scheduleconditionid and r.scheduleplanid=spi.scheduleplanid ").append(" and r.scheduletimeruleid = spi.scheduletimeruleid and p.scheduleplanid=sct.scheduleplanid  order by spi.scheduletimeruleid");
        DataSet ds = qp.getPreparedSqlDataSet("getStudyPlanItems", sql.toString(), new Object[]{trackItemId}, true);
        return ds;
    }

    public static void validateTrackItemUnits(DataSet dsTrackItems, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        HashMap<String, String> planContainer = new HashMap<String, String>();
        for (int i = 0; i < dsTrackItems.getRowCount(); ++i) {
            DataSet planItems;
            String trackItemId = dsTrackItems.getValue(i, "trackitemid");
            String qtyUnits = dsTrackItems.getValue(i, "qtyunits");
            String containerUnit = "";
            if (qtyUnits.length() <= 0 || (planItems = StabilityUtil.getPlanItemsByTrackItem(trackItemId, qp)).getRowCount() <= 0) continue;
            String planId = planItems.getValue(0, "scheduleplanid");
            if (planContainer.containsKey(planId)) {
                containerUnit = (String)planContainer.get(planId);
            } else {
                containerUnit = ScheduleGridUtil.getContainerUnit(planId, qp);
                planContainer.put(planId, containerUnit);
            }
            if (containerUnit != null && containerUnit.length() > 0 && !containerUnit.equals(qtyUnits) && !UnitsUtil.isUnitCompatible(qp, containerUnit, qtyUnits)) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
                valueMap.put("qtyUnits", "<b>'" + qtyUnits + "'</b>");
                throw new SapphireException("VALIDATION", "\n " + tp.translate("Unit conversion not defined between the Container Unit [containerUnit] and the Trackitem Unit [qtyUnits].", valueMap));
            }
            StabilityUtil.validatePlanItemPullAmountUnits(planItems, qtyUnits, qp, tp, "TrackItem");
        }
    }

    public static void validatePlanConditionUnits(DataSet dsConditions, String containerUnit, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        for (int i = 0; i < dsConditions.getRowCount(); ++i) {
            String conditionLabel = dsConditions.getValue(i, "conditionlabel");
            String planDesc = dsConditions.getValue(i, "scheduleplandesc");
            String pullType = dsConditions.getValue(i, "qtypulltype");
            String qtyPullUnits = dsConditions.getValue(i, "qtypullunits");
            String reserveType = dsConditions.getValue(i, "qtyreservetype");
            String reserveUnits = dsConditions.getValue(i, "qtyreserveunits");
            if (!("C".equals(pullType) || qtyPullUnits.length() <= 0 || qtyPullUnits.equals(containerUnit) || UnitsUtil.isUnitCompatible(qp, qtyPullUnits, containerUnit))) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
                valueMap.put("qtyPullUnits", "<b>'" + qtyPullUnits + "'</b>");
                valueMap.put("planDesc", "<b>'" + planDesc + "'</b>");
                valueMap.put("conditionLabel", "<b>'" + conditionLabel + "'</b>");
                throw new SapphireException("VALIDATION", "\n " + tp.translate("Unit conversion not defined between the Container Unit [containerUnit] and Total Pull Amount Unit [qtyPullUnits] specified in Plan [planDesc], Condition [conditionLabel].", valueMap));
            }
            if ("C".equals(reserveType) || reserveUnits.length() <= 0 || reserveUnits.equals(containerUnit) || UnitsUtil.isUnitCompatible(qp, reserveUnits, containerUnit)) continue;
            HashMap<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
            valueMap.put("reserveUnits", " <b>'" + reserveUnits + "'</b>");
            valueMap.put("planDesc", "<b>'" + planDesc + "'</b>");
            valueMap.put("conditionLabel", "<b>'" + conditionLabel + "'</b>");
            throw new SapphireException("VALIDATION", "\n " + tp.translate("Unit conversion not defined between the Container Unit [containerUnit] and Reserve Amount Unit [reserveUnits] specified in Plan [planDesc], Condition [conditionLabel].", valueMap));
        }
    }

    public static DataSet getTimePointSamples(QueryProcessor qp, PlanItem planItem) {
        boolean count = false;
        DataSet ds = qp.getPreparedSqlDataSet("gettimepointsample", "select s.samplestatus, s.reviewrequiredflag from s_sample s, scheduleplanitem sp  where sp.scheduleplanid = ? and sp.scheduletimeruleid = ? and sp.scheduleconditionid = ? and s.eventplan = sp.scheduleplanid and s.eventplanitem = sp.scheduleplanitemid", new String[]{planItem.scheduleplanid, planItem.timeruleid, planItem.conditionid});
        return ds;
    }

    public static boolean isTimePointReadyForAcceptance(DataSet ds, PlanItem planItem, QueryProcessor qp) throws SapphireException {
        if (ds.getRowCount() == 0) {
            String propertyTreeId = planItem.propertyTreeid;
            SafeSQL safeSQL = new SafeSQL();
            DataSet taskDS = qp.getPreparedSqlDataSet("select objectname from propertytree where propertytreeid = " + safeSQL.addVar(propertyTreeId), safeSQL.getValues());
            String objectname = taskDS.getString(0, "objectname");
            try {
                if (PlanItemList.isPullAmountInterfaceImplemented(objectname)) {
                    return false;
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return StabilityUtil.isTimePointReadyForAcceptance(ds);
    }

    public static boolean isTimePointReadyForAcceptance(DataSet ds) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (!("Reviewed".equals(ds.getValue(i, "samplestatus")) || "Completed".equals(ds.getValue(i, "samplestatus")) || "Cancelled".equals(ds.getValue(i, "samplestatus")) || "Disposed".equals(ds.getValue(i, "samplestatus")))) {
                return false;
            }
            if (!"Completed".equals(ds.getValue(i, "samplestatus")) || !"Y".equalsIgnoreCase(ds.getValue(i, "reviewrequiredflag"))) continue;
            return false;
        }
        return true;
    }

    public static int getTimePointPlanItemCount(QueryProcessor qp, PlanItem planItem) {
        DataSet ds = qp.getPreparedSqlDataSet("planitemcount", "select count(scheduleplanitemid) count from scheduleplanitem where scheduleplanid = ? and scheduletimeruleid = ? and scheduleconditionid = ?", new String[]{planItem.scheduleplanid, planItem.timeruleid, planItem.conditionid});
        return ds.getInt(0, "count", 1);
    }

    public static DataSet getSpecConditionRefTypeData(QueryProcessor qp) throws SapphireException {
        DataSet ds = qp.getRefTypeDataSet("Spec Condition");
        ds.sort("usersequence D");
        return ds;
    }

    public static String getSpecHtml(DataSet specs, DataSet dsSpecCondRefType, String sampleid, TranslationProcessor tp) {
        StringBuffer output = new StringBuffer();
        if (specs.getRowCount() == 1) {
            String condition = specs.getValue(0, "worstoosspeccondition");
            if (condition.length() > 0) {
                int findRefValueRow = dsSpecCondRefType.findRow("refvalueid", condition);
                if (findRefValueRow > -1) {
                    String refValueDesc = dsSpecCondRefType.getValue(findRefValueRow, "refvaluedesc");
                    String refDispValue = dsSpecCondRefType.getValue(findRefValueRow, "refdisplayvalue", refValueDesc);
                    if (refDispValue.equals("")) {
                        refDispValue = condition;
                    }
                    refDispValue = "Pass".equals(condition) ? condition + "es" : condition + "s";
                    String refDispIcon = dsSpecCondRefType.getValue(findRefValueRow, "refdisplayicon");
                    output.append("&nbsp;<img src=" + refDispIcon + " border=0 onclick=\"javascript:sapphire.ui.dialog.open( '").append("View Sample Specs").append("', 'rc?command=page&page=LV_ViewSampleSpec&sdcid=Sample&keyid1=").append(HttpUtil.encodeURIComponent(sampleid)).append("&oosonly=Y',true, 850, 660, {'Close': 'this.frame.winclose();'}, null   )\" target=\"_blank\"  title=\"" + tp.translate("Spec " + refDispValue) + "\"/>");
                } else {
                    output.append("&nbsp;");
                }
            } else {
                output.append("&nbsp;");
            }
        } else {
            output.append("&nbsp;");
        }
        return output.toString();
    }
}

