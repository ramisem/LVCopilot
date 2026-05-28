/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PlanItemList
extends ArrayList {
    String planid;
    int nextPlanitemid = 0;
    PlanItem cutPlanItem = null;
    ArrayList deleteItems = new ArrayList();
    transient Cache ptreeNodes = new Cache("PLanItemList ptreeNodes");
    private ScheduleGrid grid;

    PlanItemList(ScheduleGrid grid) {
        this.grid = grid;
    }

    public void retrieve(String planid) {
        this.planid = planid;
        SafeSQL safeSQL = new SafeSQL();
        String workitemSql = "SELECT * FROM scheduleplanitemworkitem WHERE scheduleplanid = " + safeSQL.addVar(planid) + " order by usersequence";
        DataSet workitems = this.grid.queryProcessor.getPreparedSqlDataSet(workitemSql, safeSQL.getValues());
        ScheduleGridUtil.toggleCurrentWIVersion(workitems, true);
        for (int i = 0; i < workitems.size(); ++i) {
            if (!"C".equals(workitems.getString(i, "quantitytype"))) continue;
            workitems.setString(i, "quantityunit", "(Containers)");
        }
        safeSQL.reset();
        String cellsSql = "SELECT spi.scheduleplanitemid, spi.scheduletimeruleid, spi.planitemstatus, spi.scheduleconditionid, spi.reviewdispositionflag, spi.reviewdt, spi.reviewby, sysuser.sysuserdesc, spi.propertytreeid, se.eventdt, se.eventstatus FROM scheduleplanitem spi LEFT OUTER JOIN scheduleevent se ON spi.scheduleplanid = se.scheduleplanid AND spi.scheduleplanitemid = se.scheduleplanitemid  LEFT OUTER JOIN sysuser ON spi.reviewby = sysuser.sysuserid WHERE spi.scheduleplanid = " + safeSQL.addVar(planid) + " order by ";
        cellsSql = cellsSql + (this.grid.connectionProcessor.isOra() ? " to_number( spi.scheduleplanitemid )" : " cast( spi.scheduleplanitemid AS Integer )");
        DataSet ds = this.grid.queryProcessor.getPreparedSqlDataSet(cellsSql, safeSQL.getValues());
        HashMap<String, String> workitemFilter = new HashMap<String, String>();
        for (int i = 0; i < ds.size(); ++i) {
            String planitemid = ds.getValue(i, "scheduleplanitemid");
            String propertyTreeid = ds.getValue(i, "propertytreeid");
            int planitemNumber = Integer.parseInt(planitemid);
            if (planitemNumber > this.nextPlanitemid) {
                this.nextPlanitemid = planitemNumber;
            }
            try {
                PropertyList propertyList = new PropertyList();
                propertyList.setUsePropertyValues(true);
                String valueTree = this.grid.planProcessor.loadItemValueTree(planid, planitemid);
                propertyList.setPropertyList(valueTree, false, "__condition", false);
                PlanItem planItem = this.newPlanItem(this.grid.planid, planitemid, ds.getValue(i, "scheduletimeruleid"), ds.getValue(i, "scheduleconditionid"), propertyTreeid, propertyList, ds.getValue(i, "planitemstatus", "A"));
                planItem.setReviewDispositionFlag(ds.getValue(i, "reviewdispositionflag"));
                planItem.reviewBy = ds.getValue(i, "reviewby");
                planItem.reviewByDesc = ds.getValue(i, "sysuserdesc");
                planItem.reviewDt = ds.getCalendar(i, "reviewdt");
                if (!ds.getValue(i, "eventstatus").equals("")) {
                    planItem.setEventDetails(ds.getValue(i, "eventstatus", "S"), ds.getCalendar(i, "eventdt"));
                }
                workitemFilter.put("scheduleplanitemid", planitemid);
                DataSet planItemWorkItems = workitems.getFilteredDataSet(workitemFilter);
                planItem.setWorkitems(planItemWorkItems);
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
            ++this.nextPlanitemid;
        }
    }

    public PlanItem newPlanItem(String planitemid, String timeruleid, String conditionid, String propertytreeid, PropertyList propertyList, String status) throws SapphireException {
        return this.newPlanItem(null, planitemid, timeruleid, conditionid, propertytreeid, propertyList, status);
    }

    public PlanItem newPlanItem(String scheduleplanid, String planitemid, String timeruleid, String conditionid, String propertytreeid, PropertyList propertyList, String status) throws SapphireException {
        if (planitemid == null || planitemid.length() == 0) {
            planitemid = Integer.toString(this.nextPlanitemid++);
        }
        if (status == null || status.length() == 0) {
            status = "A";
        }
        PlanItem planItem = new PlanItem(this.grid, scheduleplanid, planitemid, timeruleid, conditionid, propertytreeid, propertyList, status);
        this.add(planItem);
        return planItem;
    }

    public void save(DBUtil database) throws Exception {
        block17: {
            boolean isAuditTableExist;
            database.executePreparedUpdate("DELETE FROM scheduleplanitemworkitem WHERE scheduleplanid=?", this.planid);
            database.executePreparedUpdate("DELETE FROM scheduleplanitemdetails WHERE scheduleplanid=?", this.planid);
            String schedPlanItemSDCId = "SchedulePlanItem";
            boolean bl = isAuditTableExist = !this.grid.sdcProcessor.getProperty(schedPlanItemSDCId, "auditedflag").equalsIgnoreCase("N");
            if (isAuditTableExist && this.grid.traceLogId != null && this.grid.traceLogId.trim().length() > 0) {
                try {
                    SafeSQL safeSQL = new SafeSQL();
                    database.executePreparedUpdate("UPDATE a_scheduleplanitemworkitem SET modby = " + safeSQL.addVar(this.grid.connectionProcessor.getSapphireConnection().getSysuserId()) + ", modtool = 'DELETE', moddt = " + safeSQL.addVar(DateTimeUtil.getNowTimestamp()) + ", tracelogid = " + safeSQL.addVar(this.grid.traceLogId) + " WHERE scheduleplanid = " + safeSQL.addVar(this.planid) + "  AND tracelogid = 'DELETED' AND  auditsequence = ( SELECT max( auditsequence ) FROM a_scheduleplanitemworkitem WHERE scheduleplanid=" + safeSQL.addVar(this.planid) + " AND tracelogid = 'DELETED')", safeSQL.getValues());
                    safeSQL.reset();
                    database.executePreparedUpdate("UPDATE a_scheduleplanitemdetails SET modby = " + safeSQL.addVar(this.grid.connectionProcessor.getSapphireConnection().getSysuserId()) + ", modtool = 'DELETE', moddt = " + safeSQL.addVar(DateTimeUtil.getNowTimestamp()) + ", tracelogid = " + safeSQL.addVar(this.grid.traceLogId) + " WHERE scheduleplanid = " + safeSQL.addVar(this.planid) + "  AND tracelogid = 'DELETED' AND  auditsequence = ( SELECT max( auditsequence ) FROM a_scheduleplanitemdetails WHERE scheduleplanid=" + safeSQL.addVar(this.planid) + " AND tracelogid = 'DELETED')", safeSQL.getValues());
                }
                catch (Exception ex) {
                    int errIndx;
                    String errorMsg = ex.getMessage();
                    int n = errIndx = database.isOracle() ? errorMsg.indexOf("942") : errorMsg.indexOf("240");
                    if (errIndx >= 0) break block17;
                    throw new SapphireException("DB_UPDATE_FAILED", ex);
                }
            }
        }
        if (this.deleteItems.size() > 0) {
            StringBuffer keyid1 = new StringBuffer();
            StringBuffer keyid2 = new StringBuffer();
            Iterator it = this.deleteItems.iterator();
            while (it.hasNext()) {
                keyid1.append(";" + this.planid);
                keyid2.append(";" + it.next());
            }
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "SchedulePlanItem");
            props.put("keyid1", keyid1.substring(1));
            props.put("keyid2", keyid2.substring(1));
            this.grid.actionProcessor.processAction("deletesdi", "1", props);
        }
        PreparedStatement insertDetails = database.prepareStatement("insertdetails", "INSERT INTO scheduleplanitemdetails ( scheduleplanid, scheduleplanitemid, propertyid, propertyvalue, createdt, createby, createtool, tracelogid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )");
        insertDetails.setString(1, this.grid.planid);
        HashSet<String> conditionSet = new HashSet<String>();
        for (PlanItem planItem : this) {
            PropertyList planItemPropertyList;
            conditionSet.add(planItem.conditionid);
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "SchedulePlanItem");
            props.put("keyid1", this.planid);
            props.put("keyid2", planItem.planItemid);
            props.put("planitemstatus", planItem.status);
            if (planItem.getReviewDispositionFlagUpdated()) {
                props.put("reviewdispositionflag", planItem.getReviewDispositionFlag());
                props.put("reviewdt", "n");
                props.put("reviewby", this.grid.connectionProcessor.getSapphireConnection().getSysuserId());
            }
            if (planItem.scheduleplanid == null) {
                props.put("scheduletimeruleid", planItem.timeruleid);
                props.put("scheduleconditionid", planItem.conditionid);
                props.put("tracelogid", this.grid.traceLogId);
                this.grid.actionProcessor.processAction("addsdi", "1", props);
            } else {
                props.put("auditreason", this.grid.auditReason);
                props.put("auditactivity", this.grid.auditActivity);
                props.put("auditsignedflag", this.grid.auditSignedFlag);
                props.put("tracelogid", this.grid.traceLogId);
                this.grid.actionProcessor.processAction("editsdi", "1", props);
            }
            this.grid.planProcessor.saveItemValueTree(this.planid, planItem.planItemid, planItem.getPropertyList().toXMLString(), planItem.propertyTreeid);
            DataSet workitems = planItem.getWorkitems();
            DataSet planItemWorkitemOrig = workitems.copy();
            workitems.setString(-1, "scheduleplanid", this.planid);
            workitems.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
            workitems.setString(-1, "createby", this.grid.connectionProcessor.getSapphireConnection().getSysuserId());
            workitems.setString(-1, "createtool", this.grid.connectionProcessor.getSapphireConnection().getTool());
            workitems.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
            workitems.setString(-1, "modby", this.grid.connectionProcessor.getSapphireConnection().getSysuserId());
            workitems.setString(-1, "modtool", this.grid.connectionProcessor.getSapphireConnection().getTool());
            workitems.setString(-1, "tracelogid", this.grid.traceLogId);
            HashMap<String, String> workitemFind = new HashMap<String, String>();
            for (int i = workitems.size() - 1; i >= 0; --i) {
                String workitemid = workitems.getValue(i, "workitemid");
                String workitemversionid = workitems.getValue(i, "workitemversionid");
                workitemFind.put("workitemid", workitemid);
                if (this.grid.workItems.items.findRow(workitemFind) == -1) {
                    workitems.deleteRow(i);
                    continue;
                }
                if ("(Containers)".equals(workitems.getString(i, "quantityunit"))) {
                    workitems.setString(i, "quantitytype", "C");
                    workitems.setString(i, "quantityunit", "");
                    continue;
                }
                workitems.setString(i, "quantitytype", "U");
            }
            DataSet planItemWorkitems = planItem.getWorkitems();
            ScheduleGridUtil.toggleCurrentWIVersion(planItemWorkitems, false);
            DataSetUtil.insert(database, planItemWorkitems, "scheduleplanitemworkitem");
            if (planItem.getInstanceHasDetails() == null) continue;
            PropertyTree tree = this.grid.getPropertyTree(planItem.propertyTreeid);
            Node planNode = tree.getNode("__plan");
            Node conditionNode = tree.createNode("__condition", planNode);
            int conditionRow = this.grid.conditionAxis.findRow(planItem.conditionid);
            conditionNode.setPropertyList(this.grid.conditionAxis.getPropertyList(conditionRow, planItem.propertyTreeid));
            PropertyList propertyList = planItemPropertyList = planItem == null ? null : planItem.getPropertyList();
            if (planItemPropertyList == null) {
                planItemPropertyList = new PropertyList();
                planItemPropertyList.setUsePropertyValues(true);
            }
            Node planItemNode = tree.createNode("__item", conditionNode);
            planItemNode.setPropertyList(planItemPropertyList);
            insertDetails.setString(2, planItem.planItemid);
            PropertyList properties = tree.getNodePropertyList("__item", true);
            HashMap details = planItem.getInstanceHasDetails().getDetails(properties);
            if (details == null) continue;
            Set s = details.keySet();
            for (String propertyid : s) {
                String propertyValue = (String)details.get(propertyid);
                if (propertyValue == null || propertyValue.length() <= 0) continue;
                insertDetails.setString(3, propertyid);
                insertDetails.setString(4, propertyValue);
                insertDetails.setTimestamp(5, DateTimeUtil.getNowTimestamp());
                insertDetails.setString(6, this.grid.connectionProcessor.getSapphireConnection().getSysuserId());
                insertDetails.setString(7, this.grid.connectionProcessor.getSapphireConnection().getTool());
                insertDetails.setString(8, this.grid.traceLogId);
                insertDetails.executeUpdate();
            }
        }
        if (conditionSet.size() > 0) {
            database.createPreparedResultSet("studydetails", "select study.partialpullflag, ct.sizevalue, ct.sizeunits from study left outer join containertype ct on ct.containertypeid = study.containertypeid where studyid = (select studyid from study_scheduleplan where scheduleplanid = ?) ", new String[]{this.grid.planid});
            DataSet studyDetails = new DataSet(database.getResultSet("studydetails"));
            String containerUnit = "";
            String ppFlag = "";
            if (studyDetails.getRowCount() > 0) {
                ppFlag = studyDetails.getValue(0, "partialpullflag", "");
                double containerSize = studyDetails.getBigDecimal(0, "sizevalue", new BigDecimal(-1)).doubleValue();
                containerUnit = studyDetails.getValue(0, "sizeunits");
                for (String conditionid : conditionSet) {
                    boolean partialPullFlag = !"".equals(ppFlag) && !ppFlag.equals("N") && !ppFlag.equals("X");
                    this.grid.setPartialDistribution("X".equalsIgnoreCase(ppFlag));
                    int totalContainers = this.grid.conditionAxis.getContainersForCondition(conditionid, containerSize, containerUnit, partialPullFlag, true, new StringBuffer());
                    int conditionRow = this.grid.conditionAxis.findRow(conditionid);
                    if (conditionRow <= -1 || this.grid.conditionAxis.items.getInt(conditionRow, "qtypull", 0) == totalContainers) continue;
                    database.executePreparedUpdate("Update schedulecondition set qtypull = ? where scheduleplanid = ? and scheduleconditionid = ?", new Object[]{totalContainers, this.grid.planid, conditionid});
                }
            }
            database.closeResultSet("studydetails");
        }
        database.closeStatement(insertDetails);
    }

    public ArrayList findByTimeCondition(String timeruleid, String conditionid) {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            if (!planItem.timeruleid.equals(timeruleid) || !planItem.conditionid.equals(conditionid)) continue;
            foundCells.add(planItem);
        }
        return foundCells;
    }

    public ArrayList findByTime(String timeruleid) {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            if (!planItem.timeruleid.equals(timeruleid)) continue;
            foundCells.add(planItem);
        }
        return foundCells;
    }

    public ArrayList findByCondition(String conditionid) {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            if (!planItem.conditionid.equals(conditionid)) continue;
            foundCells.add(planItem);
        }
        return foundCells;
    }

    public ArrayList findPullTypePlanItemsByTime(String timeruleid, QueryProcessor qp) throws SapphireException {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            if (!planItem.timeruleid.equals(timeruleid)) continue;
            String propertyTreeId = planItem.propertyTreeid;
            SafeSQL safeSQL = new SafeSQL();
            DataSet taskDS = qp.getPreparedSqlDataSet("select objectname from propertytree where propertytreeid = " + safeSQL.addVar(propertyTreeId), safeSQL.getValues());
            String objectname = taskDS.getString(0, "objectname");
            try {
                Class<?> c = Class.forName(objectname);
                Method[] methods = c.getDeclaredMethods();
                boolean include = false;
                if (PlanItemList.isPullAmountInterfaceImplemented(objectname) && PlanItemList.isAnyAdvPullTaskMethodPresent(methods)) {
                    include = true;
                }
                if (!include) continue;
                foundCells.add(planItem);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return foundCells;
    }

    public ArrayList findPullTypePlanItemsByCondition(String conditionid, QueryProcessor qp) throws SapphireException {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            String propertyTreeId = planItem.propertyTreeid;
            if (!planItem.conditionid.equals(conditionid)) continue;
            SafeSQL safeSQL = new SafeSQL();
            DataSet taskDS = qp.getPreparedSqlDataSet("select objectname from propertytree where propertytreeid = " + safeSQL.addVar(propertyTreeId), safeSQL.getValues());
            String objectname = taskDS.getString(0, "objectname");
            try {
                Class<?> c = Class.forName(objectname);
                Method[] methods = c.getDeclaredMethods();
                boolean include = false;
                if (PlanItemList.isPullAmountInterfaceImplemented(objectname) && PlanItemList.isAnyAdvPullTaskMethodPresent(methods)) {
                    include = true;
                }
                if (!include) continue;
                foundCells.add(planItem);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return foundCells;
    }

    public static boolean isAnyAdvPullTaskMethodPresent(Method[] methods) {
        if (methods != null && methods.length > 0) {
            for (int j = 0; j < methods.length; ++j) {
                String mName = methods[j].getName();
                if (!"isTimeZeroTask".equals(mName) && !"loadTestProperties".equals(mName) && !"setCrossSampleRelations".equals(mName) && !"handleContainerReuseSamples".equals(mName)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isPullAmountInterfaceImplemented(String objectname) throws SapphireException {
        try {
            Class<?> c = Class.forName(objectname);
            while (!"com.labvantage.sapphire.scheduler.BaseScheduleTask".equals(c.getName())) {
                Class<?>[] interfaces = c.getInterfaces();
                if (interfaces != null && interfaces.length > 0) {
                    for (int j = 0; j < interfaces.length; ++j) {
                        if (!"com.labvantage.sapphire.stability.task.PullAmount".equals(interfaces[j].getName())) continue;
                        return true;
                    }
                }
                c = c.getSuperclass();
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return false;
    }

    public ArrayList findByTimeConditionPropertyTree(String timeruleid, String conditionid, String propertyTreeid) {
        ArrayList<PlanItem> foundCells = new ArrayList<PlanItem>();
        for (PlanItem planItem : this) {
            if (!planItem.timeruleid.equals(timeruleid) || !planItem.conditionid.equals(conditionid) || !planItem.propertyTreeid.equals(propertyTreeid)) continue;
            foundCells.add(planItem);
        }
        return foundCells;
    }

    public PlanItem findById(String id) {
        PlanItem foundItem = null;
        for (PlanItem planItem : this) {
            if (!planItem.planItemid.equals(id)) continue;
            foundItem = planItem;
        }
        return foundItem;
    }

    public void sortByTime(ArrayList list) {
        for (int i = 0; i < list.size(); ++i) {
            PlanItem item = (PlanItem)list.get(i);
            String timeruleid = item.timeruleid;
            int row = this.grid.timeAxis.findRow(timeruleid);
            if (row < 0) continue;
            item.setSequence(this.grid.timeAxis.items.getInt(row, "usersequence"));
        }
        Collections.sort(list);
    }

    public void sortByCondition(ArrayList list) {
        for (int i = 0; i < list.size(); ++i) {
            PlanItem item = (PlanItem)list.get(i);
            String conditionid = item.conditionid;
            int row = this.grid.conditionAxis.findRow(conditionid);
            if (row < 0) continue;
            item.setSequence(this.grid.conditionAxis.items.getInt(row, "usersequence"));
        }
        Collections.sort(list);
    }

    public PlanItem deletePlanItem(String planitemidlist, boolean cut) {
        String[] planitemid = StringUtil.split(planitemidlist, ";");
        for (int i = 0; i < planitemid.length; ++i) {
            PlanItem item;
            if (planitemid[i].length() <= 0 || (item = this.findById(planitemid[i])) == null || item.readonly) continue;
            this.deleteItems.add(planitemid[i]);
            if (cut) {
                this.cutPlanItem = item;
            }
            this.remove(item);
        }
        return null;
    }

    public void deleteTimeItems(String timeruleid) {
        Iterator it = this.iterator();
        while (it.hasNext()) {
            PlanItem planItem = (PlanItem)it.next();
            if (!planItem.timeruleid.equals(timeruleid)) continue;
            this.deleteItems.add(planItem.planItemid);
            it.remove();
        }
    }

    public void deleteConditionItems(String conditionid) {
        Iterator it = this.iterator();
        while (it.hasNext()) {
            PlanItem planItem = (PlanItem)it.next();
            if (!planItem.conditionid.equals(conditionid)) continue;
            this.deleteItems.add(planItem.planItemid);
            it.remove();
        }
    }

    public String paste(String from, String toPlanitemidList, String toTimeruleidList, String toConditionidList) throws SapphireException {
        StringBuffer returnList = new StringBuffer();
        Trace.log("Pasting from " + from);
        PlanItem fromItem = this.findById(from);
        if (fromItem == null && this.cutPlanItem != null && this.cutPlanItem.planItemid.equals(from)) {
            fromItem = this.cutPlanItem;
        }
        if (fromItem != null) {
            Trace.log("To: " + toPlanitemidList + " - " + toTimeruleidList + " - " + toConditionidList);
            String[] toPlanitemid = StringUtil.split(toPlanitemidList, ";");
            String[] toTimeruleid = StringUtil.split(toTimeruleidList, ";");
            String[] toConditionid = StringUtil.split(toConditionidList, ";");
            for (int i = 0; i < toPlanitemid.length; ++i) {
                if (this.grid.conditionAxis.isReadOnly(this.grid.conditionAxis.findRow(toConditionid[i]))) {
                    returnList.setLength(0);
                    break;
                }
                PlanItem toItem = this.findById(toPlanitemid[i]);
                if (toItem == null) {
                    toItem = this.newPlanItem(toPlanitemid[i], toTimeruleid[i], toConditionid[i], fromItem.propertyTreeid, fromItem.getPropertyList().copy(), fromItem.status);
                } else if (!toItem.readonly) {
                    toItem.setPropertyTreeid(fromItem.propertyTreeid);
                    toItem.setPropertyList(fromItem.getPropertyList().copy());
                }
                returnList.append(";" + toItem.planItemid);
            }
        }
        return returnList.length() > 0 ? returnList.substring(1) : "";
    }

    public void copyConditionItems(ScheduleGrid useGrid, String fromConditionid, String toConditionid) throws SapphireException {
        ArrayList copyItems = useGrid.planItems.findByCondition(fromConditionid);
        useGrid.planItems.sortByTime(copyItems);
        for (PlanItem fromItem : copyItems) {
            String oldTimeruleid = fromItem.timeruleid;
            int fromRow = useGrid.timeAxis.findRow(oldTimeruleid);
            String scheduleRule = useGrid.timeAxis.items.getValue(fromRow, "schedulerule");
            String scheduleRuleLabel = useGrid.timeAxis.items.getValue(fromRow, "schedulerulelabel");
            int newrow = this.grid.timeAxis.findOrAddScheduleRule(scheduleRule, scheduleRuleLabel);
            String newTimeruleid = this.grid.timeAxis.items.getValue(newrow, this.grid.timeAxis.idColumn);
            PlanItem newItem = this.newPlanItem("", newTimeruleid, toConditionid, fromItem.propertyTreeid, fromItem.getPropertyList(), fromItem.status);
            newItem.workitems = fromItem.workitems.copy();
            newItem.workitems.setString(-1, "scheduleplanitemid", newItem.planItemid);
        }
    }

    public void copyTimeItems(String fromTimeruleid, String toTimeruleid) throws SapphireException {
        ArrayList copyItems = this.findByTime(fromTimeruleid);
        for (PlanItem fromItem : copyItems) {
            if (fromItem.conditionreadonly) continue;
            PlanItem newItem = this.newPlanItem("", toTimeruleid, fromItem.conditionid, fromItem.propertyTreeid, fromItem.getPropertyList(), fromItem.status);
            newItem.workitems = fromItem.workitems.copy();
            newItem.workitems.setString(-1, "scheduleplanitemid", newItem.planItemid);
        }
    }

    public void setReadonly() {
        for (PlanItem planItem : this) {
            planItem.readonly = true;
        }
    }
}

