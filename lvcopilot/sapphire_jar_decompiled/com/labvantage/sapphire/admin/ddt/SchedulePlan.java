/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTimeZone
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.admin.ddt.SchedulePlanItem;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.HashMap;
import org.joda.time.DateTimeZone;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SchedulePlan
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String scheduleplanid = primary.getString(i, "scheduleplanid");
            this.database.createPreparedResultSet("SELECT\tscheduleplanid FROM\tscheduleplan WHERE\tscheduleplanid = ?", new Object[]{scheduleplanid});
            if (this.database.getNext()) {
                this.throwError("CheckExists", "VALIDATION", "SchedulePlan '" + scheduleplanid + "' already exists.");
            }
            this.validateTimeZone(primary, i);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean isFromTemplate = !actionProps.getProperty("templateid", "").isEmpty() || !actionProps.getProperty("templatekeyid1", "").isEmpty();
        boolean deepCopyPlanItems = actionProps.getProperty("copyplanitems", "N").startsWith("Y");
        if (isFromTemplate && deepCopyPlanItems) {
            String templateKeyid1 = actionProps.getProperty("templateid", "");
            if (templateKeyid1.isEmpty()) {
                templateKeyid1 = actionProps.getProperty("templatekeyid1", "");
            }
            String newKeyid1 = primary.getColumnValues("scheduleplanid", ";");
            SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
            schedulerUtil.copySchedulePlanItemsFromSchedulePlan(templateKeyid1, newKeyid1, false);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String activeFlag;
            boolean hidden;
            String scheduleplanid = primary.getString(i, "scheduleplanid");
            if (primary.isValidColumn("timezone")) {
                this.validateTimeZone(primary, i);
            }
            String checkExecSql = "select spi.scheduleplanid, spi.scheduleplanitemid from scheduleplanitem spi where spi.scheduleplanid = ? and  not exists (select spe.scheduleplanid from scheduleplanitemexec spe where spe.scheduleplanid = spi.scheduleplanid and spe.scheduleplanitemid = spi.scheduleplanitemid)";
            DataSet notExistingExecRowsDs = this.getQueryProcessor().getPreparedSqlDataSet(checkExecSql, (Object[])new String[]{scheduleplanid});
            if (notExistingExecRowsDs.getRowCount() > 0) {
                DataSetUtil.insert(this.database, notExistingExecRowsDs, "scheduleplanitemexec");
                if (!this.hasPrimaryValueChanged(primary, i, "planstatus") && this.getOldPrimaryValue(primary, i, "planstatus").equals("A")) {
                    this.setPlanItemsToBeScheduled(scheduleplanid);
                }
            }
            if (this.hasPrimaryValueChanged(primary, i, "activeflag") && (hidden = (activeFlag = primary.getValue(i, "activeflag")).equals("N"))) {
                primary.setString(i, "planstatus", "X");
            }
            if (primary.getString(i, "planstatus", "").equals("X")) {
                SchedulePlanItem.clearUnExecutedEvent(this.database, scheduleplanid, "(all)");
                this.database.executePreparedUpdate("update scheduleplanitemexec set lastscheduledt=null, nextscheduledt=null, scheduledtodt=null, lasteventdt=null where scheduleplanid=?", new Object[]{scheduleplanid});
                continue;
            }
            if (!primary.getString(i, "planstatus", "").equals("A")) continue;
            if (!this.hasPrimaryValueChanged(primary, i, "activeflag") && this.getOldPrimaryValue(primary, i, "activeflag").equals("N")) {
                throw new SapphireException("Cannot turn on hidden Schedule Plan!");
            }
            this.setPlanItemsToBeScheduled(scheduleplanid);
        }
    }

    private void validateTimeZone(DataSet primary, int i) throws SapphireException {
        String timezoneStr = primary.getValue(i, "timezone", "");
        if (!timezoneStr.isEmpty()) {
            try {
                DateTimeZone dateTimeZone = DateTimeZone.forID((String)timezoneStr);
            }
            catch (Exception e) {
                HashMap<String, String> transmap = new HashMap<String, String>();
                transmap.put("timezone", timezoneStr);
                throw new SapphireException(this.getTranslationProcessor().translate("Timezone [timezone] is not supported by Scheduler. Please select another timezone.", transmap));
            }
        }
    }

    private void setPlanItemsToBeScheduled(String scheduleplanid) throws SapphireException {
        this.database.executePreparedUpdate("update scheduleplanitemexec set nextscheduledt=? where scheduleplanid=? and nextscheduledt IS NULL  and scheduleplanitemid in (select scheduleplanitemid from scheduleplanitem where scheduleplanid = ? and startdt IS NOT NULL and planitemstatus='A' ) ", new Object[]{DateTimeUtil.getNowTimestamp(), scheduleplanid, scheduleplanid});
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("linkstosamples", "select distinct eventplan from s_sample where eventplan IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? ) ", new String[]{rsetid});
        DataSet dsLinksToSamples = new DataSet(this.database.getResultSet("linkstosamples"));
        this.database.closeResultSet("linkstosamples");
        if (dsLinksToSamples.getRowCount() > 0) {
            String planIds = dsLinksToSamples.getColumnValues("eventplanid", ",");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("planids", planIds);
            String msg = this.getTranslationProcessor().translate("SchedulePlan [planids] has Schedule Plan Items linking to samples. Consider Hiding instead.", token);
            throw new SapphireException("Samples created from Schedule Plan Exist", "VALIDATION", msg);
        }
        this.database.createPreparedResultSet("linkstoworkorders", "select distinct scheduleplanid from workorder where scheduleplanid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? ) ", new String[]{rsetid});
        DataSet dsLinksToWorkOrders = new DataSet(this.database.getResultSet("linkstoworkorders"));
        this.database.closeResultSet("linkstoworkorders");
        if (dsLinksToWorkOrders.getRowCount() > 0) {
            String planIds = dsLinksToWorkOrders.getColumnValues("scheduleplanid", ",");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("planids", planIds);
            String msg = this.getTranslationProcessor().translate("SchedulePlan [planids] has Schedule Plan Items linking to Work Orders. Consider Hiding instead.", token);
            throw new SapphireException("Work Orders created from Schedule Plan Exist", "VALIDATION", msg);
        }
        this.database.createPreparedResultSet("activecondition", "select distinct scheduleplanid from schedulecondition where scheduleplanid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? ) and conditionstatus = ?", new String[]{rsetid, "R"});
        DataSet dsActiveConditions = new DataSet(this.database.getResultSet("activecondition"));
        this.database.closeResultSet("activecondition");
        if (dsActiveConditions.getRowCount() > 0) {
            String planIds = dsActiveConditions.getColumnValues("scheduleplanid", ",");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("planids", planIds);
            String msg = this.getTranslationProcessor().translate("SchedulePlan [planids] are found with active condition(s).", token);
            throw new SapphireException("Exists Active Condition", "VALIDATION", msg);
        }
        this.database.createPreparedResultSet("completedevent", "select distinct e.scheduleplanid from scheduleevent e, schedulecondition c where c.scheduleplanid = e.scheduleplanid and e.scheduleplanid in ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? ) and e.eventstatus = ?", new String[]{rsetid, "D"});
        DataSet dsCompletedevents = new DataSet(this.database.getResultSet("completedevent"));
        this.database.closeResultSet("completedevent");
        if (dsCompletedevents.getRowCount() > 0) {
            String planIds = dsCompletedevents.getColumnValues("scheduleplanid", ",");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("planids", planIds);
            String msg = this.getTranslationProcessor().translate("SchedulePlan [planids] are found with completed task(s).", token);
            throw new SapphireException("Exists Completed Tasks", "VALIDATION", msg);
        }
        Object[] vars = new Object[]{rsetid};
        this.database.executePreparedUpdate("DELETE FROM study_scheduleplan WHERE ( scheduleplanid ) IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM protocolprod_scheduleplan WHERE ( scheduleplanid ) IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        this.database.executePreparedUpdate("DELETE FROM protocol_scheduleplan WHERE ( scheduleplanid ) IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )", vars);
        String sql = "SELECT scheduleplanid, scheduleplanitemid FROM scheduleplanitem WHERE ( scheduleplanid ) IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ? )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, vars);
        if (ds.size() > 0) {
            StringBuffer planlist = new StringBuffer();
            StringBuffer planitemlist = new StringBuffer();
            for (int i = 0; i < ds.size(); ++i) {
                planlist.append(";").append(ds.getString(i, "scheduleplanid"));
                planitemlist.append(";").append(ds.getString(i, "scheduleplanitemid"));
            }
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "SchedulePlanItem");
            props.put("keyid1", planlist.substring(1));
            props.put("keyid2", planitemlist.substring(1));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet scheduleConditionTrackItems = sdiData.getDataset("schedulecondition_trackitem");
        if (scheduleConditionTrackItems != null && scheduleConditionTrackItems.getRowCount() > 0) {
            this.validateTrackItemUnits(scheduleConditionTrackItems);
        }
    }

    private void validateTrackItemUnits(DataSet scheduleConditionTrackItems) throws SapphireException {
        String trackItemIds = scheduleConditionTrackItems.getColumnValues("trackitemid", "','");
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsTrackItems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT trackitemid, qtycurrenttype, qtyunits  FROM trackitem WHERE trackitemid in(" + safeSQL.addIn(trackItemIds) + ") and qtycurrenttype != 'C' ", safeSQL.getValues());
        if (dsTrackItems.getRowCount() > 0) {
            QueryProcessor qp = this.getQueryProcessor();
            TranslationProcessor tp = this.getTranslationProcessor();
            StabilityUtil.validateTrackItemUnits(dsTrackItems, qp, tp);
        }
    }
}

