/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_DepartmentAssignment
extends BaseSDCRules {
    private final String COLUMN_ASSIGNMENTSTATUS = "assignmentstatus";
    private final String COLUMN_DEPARTMENTID = "departmentid";
    private final String COLUMN_SYSUSERID = "sysuserid";
    private final String COLUMN_ASSIGNMENTSTARTDATE = "assignmentstartdt";
    private final String COLUMN_ASSIGNMENTENDDATE = "assignmentenddt";
    private final String COLUMN_DEPARTMENT_ASSIGNMENTID = "departmentassignmentid";
    private final String COLUMN_TRANSIENTFLAG = "transientflag";
    private final String ASSIGNMENTSTATUS_PENDING = "Pending";
    private final String ASSIGNMENTSTATUS_ACTIVE = "Active";
    private final String ASSIGNMENTSTATUS_DONE = "Done";
    private final String ASSIGNMENTSTATUS_PERMANENT = "Permanent";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String assignmentstatus = primary.getString(i, "assignmentstatus", "");
            String sysuserid = primary.getString(i, "sysuserid");
            String departmentid = primary.getString(i, "departmentid");
            Calendar calEnd = primary.getCalendar(i, "assignmentenddt");
            if (calEnd != null && !assignmentstatus.equalsIgnoreCase("Permanent")) {
                calEnd.set(11, calEnd.getMaximum(11));
                calEnd.set(12, calEnd.getMaximum(12));
                calEnd.set(13, calEnd.getMaximum(13));
                if (this.getConnectionProcessor().isMSS()) {
                    calEnd.set(14, 997);
                } else {
                    calEnd.set(14, calEnd.getMaximum(14));
                }
                primary.setDate(i, "assignmentenddt", calEnd);
            }
            if (this.checkIfPermanentlyAssignedUser(sysuserid, departmentid)) {
                throw new SapphireException(this.getTranslationProcessor().translate(sysuserid + " is assigned permanently"));
            }
            if (this.checkIfEndDateIsLessThanStartDate(primary, i)) {
                throw new SapphireException(this.getTranslationProcessor().translate("End date can not be less than start date"));
            }
            if (assignmentstatus.trim().length() != 0) continue;
            primary.setString(i, "assignmentstatus", "Pending");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String sysuserid = primary.getString(i, "sysuserid");
            String departmentid = primary.getString(i, "departmentid");
            String assignmentstatus = primary.getString(i, "assignmentstatus", "");
            Calendar calEnd = primary.getCalendar(i, "assignmentenddt");
            if (calEnd != null && !assignmentstatus.equalsIgnoreCase("Permanent")) {
                calEnd.set(11, calEnd.getMaximum(11));
                calEnd.set(12, calEnd.getMaximum(12));
                calEnd.set(13, calEnd.getMaximum(13));
                if (this.getConnectionProcessor().isMSS()) {
                    calEnd.set(14, 997);
                } else {
                    calEnd.set(14, calEnd.getMaximum(14));
                }
                primary.setDate(i, "assignmentenddt", calEnd);
            }
            if (this.checkIfPermanentlyAssignedUser(sysuserid, departmentid)) {
                throw new SapphireException(this.getTranslationProcessor().translate(sysuserid + " is assigned permanently"));
            }
            if (!this.checkIfEndDateIsLessThanStartDate(primary, i)) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("End date can not be less than start date"));
        }
    }

    private boolean checkIfPermanentlyAssignedUser(String sysuserid, String departmentid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select sysuserid from departmentassignment where assignmentstatus=" + safeSQL.addVar("Permanent") + " and sysuserid=" + safeSQL.addVar(sysuserid) + " and departmentid=" + safeSQL.addVar(departmentid);
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return dataSet.getRowCount() > 0;
    }

    private boolean checkIfEndDateIsLessThanStartDate(DataSet primary, int rowndex) {
        Timestamp assignmentStartDate = primary.getTimestamp(rowndex, "assignmentstartdt");
        Timestamp assignmentEndDate = primary.getTimestamp(rowndex, "assignmentenddt");
        return assignmentStartDate != null && assignmentEndDate != null && assignmentEndDate.before(assignmentStartDate);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null && primary.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                String departmentId = primary.getString(i, "departmentid", "");
                String sysUserId = primary.getString(i, "sysuserid", "");
                this.clearCache("UserSchedules", sysUserId);
                this.clearCache("DepartmentSchedules", departmentId);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
        this.addRemoveDepartmentSysuserRecords(primary, oldPrimary);
        if (primary != null && primary.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                String departmentId = primary.getString(i, "departmentid", "");
                String sysUserId = primary.getString(i, "sysuserid", "");
                this.clearCache("UserSchedules", sysUserId);
                this.clearCache("DepartmentSchedules", departmentId);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        int i;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select departmentassignmentid,departmentid,sysuserid from departmentassignment where assignmentstatus='Active' and departmentassignmentid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
        if (ds != null && ds.size() > 0) {
            for (i = 0; i < ds.getRowCount(); ++i) {
                String sysuserid;
                String departmentassignmentid = ds.getString(i, "departmentassignmentid");
                String departmentid = ds.getString(i, "departmentid");
                if (!this.hasOtherDepartmentAssignments(departmentid, sysuserid = ds.getString(i, "sysuserid"), departmentassignmentid)) continue;
                this.removeAssignmentFromDeptSysUser(";" + departmentid, ";" + sysuserid);
            }
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet("select departmentid,sysuserid from departmentassignment where  departmentassignmentid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid})) != null && ds.size() > 0) {
            for (i = 0; i < ds.size(); ++i) {
                String departmentid = ds.getString(i, "departmentid");
                String sysuserid = ds.getString(i, "sysuserid");
                this.clearCache("UserSchedules", sysuserid);
                this.clearCache("DepartmentSchedules", departmentid);
            }
        }
    }

    private boolean hasOtherDepartmentAssignments(String departmentid, String sysuserid, String departmentassignmentid) throws SapphireException {
        String sql = "select count(departmentassignmentid) countofotherdeptassignments  from departmentassignment where departmentid=? and sysuserid=? and departmentassignmentid <> ? and assignmentstatus in ('Active')";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{departmentid, sysuserid, departmentassignmentid});
        int count = 0;
        if (ds.getRowCount() > 0) {
            count = ds.getInt(0, "countofotherdeptassignments", 0);
        }
        return count <= 0;
    }

    private void addRemoveDepartmentSysuserRecords(DataSet primary, DataSet oldPrimary) throws SapphireException {
        StringBuffer addDeptids = new StringBuffer();
        StringBuffer addSysuserids = new StringBuffer();
        HashSet<String> addToAddMap = new HashSet<String>();
        StringBuffer removeDeptids = new StringBuffer();
        StringBuffer removeSysuserids = new StringBuffer();
        DataSet ds = null;
        HashMap<String, String> hm = new HashMap<String, String>();
        for (int i = 0; i < primary.size(); ++i) {
            String assignmentstatus = primary.getString(i, "assignmentstatus", "");
            String deptid = this.getColumnValue(primary, oldPrimary, i, "departmentid");
            String sysuserid = this.getColumnValue(primary, oldPrimary, i, "sysuserid");
            String departmentassignmentid = this.getColumnValue(primary, oldPrimary, i, "departmentassignmentid");
            if (!this.hasPrimaryValueChanged(primary, i, "assignmentstatus")) continue;
            if (assignmentstatus.equalsIgnoreCase("Active")) {
                ds = ds == null ? this.getQueryProcessor().getSqlDataSet("select departmentid,sysuserid from departmentsysuser") : ds;
                hm.clear();
                hm.put("departmentid", deptid);
                hm.put("sysuserid", sysuserid);
                if (ds.findRow(hm) > -1 || !addToAddMap.add(deptid + "-" + sysuserid)) continue;
                addDeptids.append(";").append(deptid);
                addSysuserids.append(";").append(sysuserid);
                continue;
            }
            if (!assignmentstatus.equalsIgnoreCase("Done")) continue;
            if (this.hasOtherDepartmentAssignments(deptid, sysuserid, departmentassignmentid)) {
                removeDeptids.append(";").append(deptid);
                removeSysuserids.append(";").append(sysuserid);
                continue;
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_DepartmentAssignment");
            props.setProperty("keyid1", departmentassignmentid);
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
        if (addDeptids.length() > 0) {
            this.addAssignmentToDeptSysUser(addDeptids.substring(1), addSysuserids.substring(1));
        }
        if (removeDeptids.length() > 0) {
            this.removeAssignmentFromDeptSysUser(removeDeptids.substring(1), removeSysuserids.substring(1));
        }
    }

    private void addAssignmentToDeptSysUser(String deptid, String sysuserid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Department");
        props.setProperty("linkid", "department users");
        props.setProperty("sysuserid", sysuserid);
        props.setProperty("departmentid", deptid);
        props.setProperty("transientflag", "Y");
        try {
            this.getActionProcessor().processAction("AddSDIDetail", "1", props);
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Not able to add Assignment to DepartmentSysuser", e);
        }
    }

    private void removeAssignmentFromDeptSysUser(String deptid, String sysuserid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Department");
        props.setProperty("linkid", "department users");
        props.setProperty("sysuserid", sysuserid);
        props.setProperty("departmentid", deptid);
        try {
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", props);
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Not able to remove Assignment from DepartmentSysuser", e);
        }
    }

    private String getColumnValue(DataSet primary, DataSet oldPrimary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : oldPrimary.getString(indx, columnid, "");
        return value;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditDetailImage() {
        return true;
    }

    private void clearCache(String cacheName, String key) {
        CacheUtil.removeAllStartWith(this.getDatabaseid(), cacheName, key);
    }
}

