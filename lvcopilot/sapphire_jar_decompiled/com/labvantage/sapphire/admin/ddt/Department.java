/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.admin.ddt.rules.PackageCDRule;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Department
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 88986 $";
    public static final String SDC = "Department";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        primary.addColumn("custodialdomainflag", 0);
        primary.addColumn("bulletinflag", 0);
        primary.addColumn("workassignmentflag", 0);
        primary.addColumn("testingflag", 0);
        primary.addColumn("ownershipflag", 0);
        for (int i = 0; i < primary.size(); ++i) {
            if (!"Y".equals(primary.getValue(i, "custodialdomainflag", "N"))) {
                if ("Y".equals(primary.getString(i, "repositoryflag", "N")) || "Y".equals(primary.getString(i, "allowtempflag", "N")) || "Y".equals(primary.getString(i, "externalflag", "N"))) {
                    primary.setString(i, "custodialdomainflag", "Y");
                } else {
                    primary.setString(i, "custodialdomainflag", "N");
                }
            }
            if (!"Y".equals(primary.getString(i, "bulletinflag"))) {
                primary.setString(i, "bulletinflag", "N");
            }
            if (!"Y".equals(primary.getString(i, "workassignmentflag"))) {
                primary.setString(i, "workassignmentflag", "N");
            }
            if (!"Y".equals(primary.getString(i, "testingflag"))) {
                primary.setString(i, "testingflag", "N");
            }
            if (!"Y".equals(primary.getString(i, "ownershipflag"))) {
                primary.setString(i, "ownershipflag", "N");
            }
            if (this.isCMTImport()) continue;
            this.validateWAPProperties(primary, i);
        }
        this.createAddressSDI(primary);
    }

    private void createAddressSDI(DataSet primary) throws ActionException {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("portalclientflag", "Y");
        DataSet portalClients = primary.getFilteredDataSet(filterMap);
        if (portalClients.getRowCount() > 0) {
            primary.addColumn("primaryaddressid", 0);
            primary.addColumn("primaryaddresstype", 0);
            PropertyList addSDIProps = new PropertyList();
            addSDIProps.setProperty("sdcid", "Address");
            addSDIProps.setProperty("keyid2", "Contact");
            addSDIProps.setProperty("copies", String.valueOf(portalClients.getRowCount()));
            addSDIProps.setProperty("autoallocateaddressid", "Y");
            addSDIProps.setProperty("externalflag", "N");
            this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
            portalClients.addColumnValues("primaryaddressid", 0, addSDIProps.getProperty("newkeyid1"), ";");
            portalClients.addColumnValues("primaryaddresstype", 0, addSDIProps.getProperty("newkeyid2"), ";");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if ((this.hasPrimaryValueChanged(primary, i, "repositoryflag") || this.hasPrimaryValueChanged(primary, i, "allowtempflag") || this.hasPrimaryValueChanged(primary, i, "externalflag")) && !"Y".equals(primary.getValue(i, "custodialdomainflag", "N"))) {
                if ("Y".equals(primary.getString(i, "repositoryflag", "N")) || "Y".equals(primary.getString(i, "allowtempflag", "N")) || "Y".equals(primary.getString(i, "externalflag", "N"))) {
                    primary.setString(i, "custodialdomainflag", "Y");
                } else {
                    primary.setString(i, "custodialdomainflag", "N");
                }
            }
            this.validateWAPProperties(primary, i);
        }
    }

    private void validateWAPProperties(DataSet primary, int i) throws SapphireException {
        DataSet ds;
        StringBuffer sql;
        SafeSQL safeSQL;
        if (this.hasPrimaryValueChanged(primary, i, "sitedepartmentflag") && primary.getValue(i, "sitedepartmentflag", "N").equalsIgnoreCase("Y")) {
            primary.setValue(i, "testinglabtype", "");
            if (primary.getValue(i, "testingflag", "N").equalsIgnoreCase("N")) {
                primary.setValue(i, "workassignmentflag", "N");
            }
            primary.setValue(i, "workareatype", "");
            primary.setValue(i, "parentdepartmentid", "");
        } else {
            if (this.hasPrimaryValueChanged(primary, i, "testingflag") && primary.getValue(i, "testingflag", "N").equalsIgnoreCase("N")) {
                primary.setValue(i, "testinglabtype", "");
            }
            if (!(!this.hasPrimaryValueChanged(primary, i, "testingflag") && !this.hasPrimaryValueChanged(primary, i, "workassignmentflag") && !this.hasPrimaryValueChanged(primary, i, "testinglabtype") || primary.getValue(i, "testingflag", "N").equalsIgnoreCase("Y") && primary.getValue(i, "workassignmentflag", "N").equalsIgnoreCase("N") || primary.getValue(i, "testingflag", "N").equalsIgnoreCase("N") && primary.getValue(i, "workassignmentflag", "N").equalsIgnoreCase("Y"))) {
                primary.setValue(i, "parentdepartmentid", "");
            }
            if (this.hasPrimaryValueChanged(primary, i, "testinglabtype") && primary.getValue(i, "testinglabtype", "").length() > 0) {
                primary.setValue(i, "workassignmentflag", "N");
                primary.setValue(i, "workareatype", "");
            }
            if ((this.hasPrimaryValueChanged(primary, i, "testingflag") || this.hasPrimaryValueChanged(primary, i, "workassignmentflag")) && primary.getValue(i, "testingflag", "N").equalsIgnoreCase("Y") && primary.getValue(i, "workassignmentflag", "N").equalsIgnoreCase("Y")) {
                primary.setValue(i, "workareatype", "");
            }
        }
        String parentDeptId = primary.getValue(i, "parentdepartmentid", "");
        if (parentDeptId.length() > 0 && (this.hasPrimaryValueChanged(primary, i, "testingflag") || this.hasPrimaryValueChanged(primary, i, "testinglabtype") || this.hasPrimaryValueChanged(primary, i, "workassignmentflag") || this.hasPrimaryValueChanged(primary, i, "parentdepartmentid"))) {
            String exceptionMSG = "";
            if (primary.getValue(i, "testingflag", "N").equalsIgnoreCase("Y") && primary.getValue(i, "testinglabtype", "").length() > 0 && !this.isTestingSite(parentDeptId)) {
                exceptionMSG = "Parent Site(" + parentDeptId + ") is not a Site.";
            } else if (primary.getValue(i, "testingflag", "N").equalsIgnoreCase("N") && primary.getValue(i, "workassignmentflag", "N").equalsIgnoreCase("Y") && !this.isTestingLab(parentDeptId)) {
                exceptionMSG = "Parent Testing Lab(" + parentDeptId + ") is not a Testing Lab.";
            }
            if (exceptionMSG.length() > 0) {
                throw new SapphireException(this.getTranslationProcessor().translate(exceptionMSG));
            }
        }
        if ((this.hasPrimaryValueChanged(primary, i, "parentdepartmentid") || this.hasPrimaryValueChanged(primary, i, "testingflag") || this.hasPrimaryValueChanged(primary, i, "testinglabtype")) && primary.getValue(i, "testingflag", "N").equalsIgnoreCase("Y") && primary.getValue(i, "testinglabtype", "").length() > 0 && parentDeptId.length() > 0) {
            String testingLabType = primary.getValue(i, "testinglabtype", "");
            safeSQL = new SafeSQL();
            sql = new StringBuffer();
            sql.append("select departmentid from department where parentdepartmentid = ").append(safeSQL.addVar(parentDeptId));
            sql.append(" and testinglabtype=").append(safeSQL.addVar(testingLabType));
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("A Site can only have one Testing Lab per Testing Lab Type."));
            }
        }
        if ((this.hasPrimaryValueChanged(primary, i, "parentdepartmentid") || this.hasPrimaryValueChanged(primary, i, "workassignmentflag") || this.hasPrimaryValueChanged(primary, i, "workareatype")) && primary.getValue(i, "workassignmentflag", "N").equalsIgnoreCase("Y") && primary.getValue(i, "workareatype", "").length() > 0 && parentDeptId.length() > 0) {
            String workareatype = primary.getValue(i, "workareatype", "");
            safeSQL = new SafeSQL();
            sql = new StringBuffer();
            sql.append("select departmentid from department where parentdepartmentid = ").append(safeSQL.addVar(parentDeptId));
            sql.append(" and workareatype=").append(safeSQL.addVar(workareatype));
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("A Testing Lab can only have one WorkArea per WorkArea Type."));
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String departmentid = StringUtil.replaceAll(actionProps.getProperty("keyid1", ""), ";", "','");
        SafeSQL safeSQL = new SafeSQL();
        String invarholders = safeSQL.addIn(departmentid);
        this.database.executePreparedUpdate("update s_eventlog set departmentid = null where departmentid in (" + invarholders + ")", safeSQL.getValues());
        this.database.executePreparedUpdate("update trackitem set custodialdepartmentid = null where custodialdepartmentid in (" + invarholders + ")", safeSQL.getValues());
        this.database.executePreparedUpdate("update trackitem set ownerdepartmentid = null where ownerdepartmentid in (" + invarholders + ")", safeSQL.getValues());
        this.database.executePreparedUpdate("delete from sdisecuritydepartment where securitydepartment in (" + invarholders + ")", safeSQL.getValues());
        this.database.executePreparedUpdate("DELETE FROM DEPARTMENTASSIGNMENT WHERE DEPARTMENTID IN (" + invarholders + ")", safeSQL.getValues());
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String[] departmentIds;
        for (String departmentId : departmentIds = actionProps.getProperty("keyid1", "").trim().split(";")) {
            CacheUtil.remove(this.getDatabaseid(), SDC, departmentId);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        int i;
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkGLPRule(primary, "Y".equals(actionProps.getProperty("__sdcruleconfirm")));
        }
        if (this.connectionInfo.hasModule("ASL") || this.connectionInfo.hasModule("SMS")) {
            this.checkPackageCDRule(primary);
        }
        for (i = 0; i < primary.size(); ++i) {
            String departmentId = primary.getString(i, "departmentid", "");
            CacheUtil.remove(this.getDatabaseid(), SDC, departmentId);
        }
        if (primary != null) {
            for (i = 0; i < primary.size(); ++i) {
                String departmentid = primary.getString(i, "departmentid", "");
                if (!this.hasPrimaryValueChanged(primary, i, "workassignmentflag")) continue;
                String oldWorkAssignmentFlag = this.getOldPrimaryValue(primary, i, "workassignmentflag");
                if ("Y".equals(oldWorkAssignmentFlag)) {
                    this.deletePermanentScheduledUsers(departmentid, "");
                    continue;
                }
                this.createPermanentScheduledUsers(departmentid);
            }
        }
    }

    private void checkGLPRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        GLPRule rule = new GLPRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "glpflag") || !"N".equals(primary.getValue(i, "glpflag"))) continue;
            rule.processDepartmentGLPRule(primary.getString(i, "departmentid"), forceUpdate);
        }
    }

    private void checkPackageCDRule(DataSet primary) {
        PackageCDRule rule = new PackageCDRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if ((!this.hasPrimaryValueChanged(primary, i, "externalflag") || !primary.getValue(i, "externalflag").equals("Y")) && (!this.hasPrimaryValueChanged(primary, i, "repositoryflag") || !primary.getValue(i, "repositoryflag").equals("N"))) continue;
            String departmentid = primary.getString(i, "departmentid");
            ArrayList packageList = Department.getPackageList(this.getQueryProcessor(), departmentid);
            for (String packageid : packageList) {
                try {
                    rule.processRule(packageid);
                }
                catch (SapphireException se) {
                    this.setError(rule.getClass().getName(), "VALIDATION", se.getMessage());
                }
            }
        }
    }

    public static boolean isGLP(QueryProcessor queryProcessor, String departmentid) {
        boolean glpFlag = false;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select glpflag from department where departmentid = ").append(safeSQL.addVar(departmentid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            glpFlag = "Y".equals(ds.getString(0, "glpflag"));
        }
        return glpFlag;
    }

    public static boolean isGLP(DBAccess database, String departmentid) throws SapphireException {
        boolean glpFlag = false;
        database.createPreparedResultSet("select glpflag from department where departmentid = ?", new Object[]{departmentid});
        if (database.getNext()) {
            glpFlag = "Y".equals(database.getString("glpflag"));
        }
        database.closeResultSet();
        return glpFlag;
    }

    public static boolean isCustodialDomain(QueryProcessor queryProcessor, String departmentid) {
        boolean isCD = false;
        if (departmentid != null && departmentid.trim().length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = new StringBuffer().append("SELECT externalflag FROM department WHERE departmentid=").append(safeSQL.addVar(departmentid)).toString();
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null) {
                isCD = !"Y".equals(ds.getString(0, "externalflag"));
            }
        }
        return isCD;
    }

    public static boolean isCustodialDomain(DBAccess database, String departmentid) throws SapphireException {
        boolean isCD = false;
        if (departmentid != null && departmentid.trim().length() > 0) {
            database.createPreparedResultSet("select externalflag from department where departmentid = ?", new Object[]{departmentid});
            if (database.getNext()) {
                isCD = !"Y".equals(database.getString("externalflag"));
            }
            database.closeResultSet();
        }
        return isCD;
    }

    public static boolean isRepository(QueryProcessor queryProcessor, String departmentid) {
        boolean repositoryFlag = false;
        SafeSQL safeSQL = new SafeSQL();
        String sql = new StringBuffer().append("SELECT repositoryflag FROM department where departmentid = ").append(safeSQL.addVar(departmentid)).toString();
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null) {
            repositoryFlag = "Y".equals(ds.getString(0, "repositoryflag"));
        }
        return repositoryFlag;
    }

    public static boolean isRepository(DBAccess database, String departmentid) throws SapphireException {
        boolean repositoryFlag = false;
        database.createPreparedResultSet("select repositoryflag from department where departmentid = ?", new Object[]{departmentid});
        if (database.getNext()) {
            repositoryFlag = "Y".equals(database.getString("repositoryflag"));
        }
        database.closeResultSet();
        return repositoryFlag;
    }

    public static ArrayList getPackageList(QueryProcessor queryProcessor, String departmentid) {
        ArrayList<String> packages = new ArrayList<String>();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT s_packageid FROM s_package WHERE senderdepartmentid=").append(safeSQL.addVar(departmentid)).append(" OR recipientdepartmentid=").append(safeSQL.addVar(departmentid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                packages.add(ds.getValue(i, "s_packageid"));
            }
        }
        return packages;
    }

    public static ArrayList getSampleList(QueryProcessor queryProcessor, String samplefamilyid) {
        ArrayList<String> samples = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s_sampleid FROM s_sample, trackitem WHERE trackitem.linksdcid='Sample'");
        sql.append(" AND trackitem.linkkeyid1=s_sampleid AND custodialdepartmentid=");
        sql.append(safeSQL.addVar(samplefamilyid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                samples.add(ds.getValue(i, "s_sampleid"));
            }
        }
        return samples;
    }

    private boolean isTestingSite(String parentDeptId) {
        boolean flag = false;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select sitedepartmentflag from department where departmentid = ").append(safeSQL.addVar(parentDeptId));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            flag = "Y".equals(ds.getString(0, "sitedepartmentflag"));
        }
        return flag;
    }

    private boolean isTestingLab(String parentDeptId) {
        boolean flag = false;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select testingflag from department where departmentid = ").append(safeSQL.addVar(parentDeptId));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            flag = "Y".equals(ds.getString(0, "testingflag"));
        }
        return flag;
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("shiftid") != null && !actionProps.getProperty("shiftid").toString().trim().isEmpty() && !actionProps.getProperty("shiftid").toString().trim().equals("(null)")) {
            String[] departmentids = actionProps.getProperty("departmentid").split(";");
            String[] shiftids = actionProps.getProperty("shiftid").split(";");
            StringBuffer calendardesc = new StringBuffer();
            StringBuffer sharedflags = new StringBuffer();
            StringBuffer sourceflags = new StringBuffer();
            StringBuffer keyid1s = new StringBuffer();
            for (int i = 0; i < shiftids.length; ++i) {
                calendardesc.append("Department " + departmentids[i] + " " + shiftids[i] + " Calendar;");
                sharedflags.append("N;");
                sourceflags.append("I;");
                keyid1s.append("(null);");
            }
            PropertyList addsdi = new PropertyList();
            addsdi.setProperty("sdcid", "LV_Calendar");
            addsdi.setProperty("keyid1", keyid1s.substring(0, keyid1s.length() - 1).toString());
            addsdi.setProperty("corehours", actionProps.getProperty("calendarid"));
            addsdi.setProperty("sharedflag", sharedflags.substring(0, sharedflags.length() - 1).toString());
            addsdi.setProperty("sourceflag", sourceflags.substring(0, sourceflags.length() - 1).toString());
            addsdi.setProperty("calendardesc", calendardesc.substring(0, calendardesc.length() - 1).toString());
            this.getActionProcessor().processAction("AddSDI", "1", addsdi);
            actionProps.setProperty("calendarid", addsdi.getProperty("newkeyid1"));
            DataSet departmentshift = sdiData.getDataset("departmentshift");
            if (departmentshift != null && departmentshift.getRowCount() > 0) {
                String[] newKeyId1s = addsdi.getProperty("newkeyid1").split(";");
                for (int i = 0; i < newKeyId1s.length; ++i) {
                    departmentshift.setString(i, "calendarid", newKeyId1s[i]);
                }
            }
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet deptUserDS = sdiData.getDataset("departmentsysuser");
        this.checkAndClearOtherDefaultTestingLab(deptUserDS);
        this.createPermanentScheduledUsers(deptUserDS);
        this.flushCaches(actionProps);
        Department.addUserAppAccess(deptUserDS, this.getQueryProcessor(), this.getActionProcessor(), this.logger);
        Department.checkIfUserAlreadyPortalAdmin(deptUserDS, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor());
    }

    public static void addUserAppAccess(DataSet departmentSysuserDS, QueryProcessor qp, ActionProcessor ap, Logger logger) {
        if (departmentSysuserDS != null) {
            HashSet<String> uniqueDepts = new HashSet<String>();
            for (int i = 0; i < departmentSysuserDS.getRowCount(); ++i) {
                uniqueDepts.add(departmentSysuserDS.getString(i, "departmentid"));
            }
            DataSet portalClientDeptsInfo = qp.getSqlDataSet("SELECT departmentid, portalclientflag FROM department WHERE departmentid IN ('" + StringUtil.replaceAll(OpalUtil.toDelimitedString(uniqueDepts, ";"), ";", "','") + "') AND portalclientflag = 'Y'");
            if (portalClientDeptsInfo.getRowCount() > 0) {
                DataSet deptsApps = qp.getSqlDataSet("SELECT departmentid, appid FROM appdepartment WHERE departmentid IN ('" + portalClientDeptsInfo.getColumnValues("departmentid", ";") + "')");
                DataSet appSysuser = new DataSet();
                HashSet<String> appSysUserSet = new HashSet<String>();
                HashSet<String> uniqueUserid = new HashSet<String>();
                HashSet<String> uniqueAppId = new HashSet<String>();
                departmentSysuserDS.sort("departmentid");
                ArrayList<DataSet> deptSysUserGroups = departmentSysuserDS.getGroupedDataSets("departmentid");
                HashMap<String, String> filterMap = new HashMap<String, String>();
                for (int i = 0; i < deptSysUserGroups.size(); ++i) {
                    DataSet deptSysuserGroup = deptSysUserGroups.get(i);
                    String deptId = deptSysuserGroup.getString(0, "departmentid");
                    if (portalClientDeptsInfo.findRow("departmentid", deptId) <= -1) continue;
                    filterMap.clear();
                    filterMap.put("departmentid", deptId);
                    DataSet deptApps = deptsApps.getFilteredDataSet(filterMap);
                    for (int j = 0; j < deptSysuserGroup.getRowCount(); ++j) {
                        for (int k = 0; k < deptApps.getRowCount(); ++k) {
                            if (appSysUserSet.contains(deptSysuserGroup.getString(j, "sysuserid") + ";" + deptApps.getString(k, "appid"))) continue;
                            appSysUserSet.add(deptSysuserGroup.getString(j, "sysuserid") + ";" + deptApps.getString(k, "appid"));
                            uniqueUserid.add(deptSysuserGroup.getString(j, "sysuserid"));
                            uniqueAppId.add(deptApps.getString(k, "appid"));
                            int newRow = appSysuser.addRow();
                            appSysuser.setString(newRow, "sysuserid", deptSysuserGroup.getString(j, "sysuserid"));
                            appSysuser.setString(newRow, "appid", deptApps.getString(k, "appid"));
                        }
                    }
                }
                if (appSysuser.getRowCount() > 0) {
                    DataSet userAppsDB = qp.getSqlDataSet("SELECT sysuserid, appid FROM appsysuser WHERE sysuserid IN ('" + OpalUtil.toDelimitedString(uniqueUserid, "','") + "')");
                    DataSet appInfo = qp.getSqlDataSet("SELECT appid, licensedflag, maxusers FROM app WHERE appid IN ('" + OpalUtil.toDelimitedString(uniqueAppId, "','") + "')");
                    appSysuser.sort("appid");
                    ArrayList<DataSet> appSysUserGroups = appSysuser.getGroupedDataSets("appid");
                    for (int i = 0; i < appSysUserGroups.size(); ++i) {
                        DataSet appSysUserGroup = appSysUserGroups.get(i);
                        int appRow = appInfo.findRow("appid", appSysUserGroup.getString(0, "appid"));
                        for (int j = appSysUserGroup.getRowCount() - 1; j >= 0; --j) {
                            filterMap.clear();
                            filterMap.put("sysuserid", appSysUserGroup.getString(j, "sysuserid"));
                            filterMap.put("appid", appSysUserGroup.getString(j, "appid"));
                            if (userAppsDB.findRow(filterMap) > -1) {
                                appSysUserGroup.deleteRow(j);
                                continue;
                            }
                            if (!"Y".equals(appInfo.getString(appRow, "licensedflag", "N")) || "U".equals(appInfo.getString(appRow, "maxusers")) || "S".equals(appInfo.getString(appRow, "maxusers"))) continue;
                            appSysUserGroup.deleteRow(j);
                        }
                        if (appSysUserGroup.getRowCount() <= 0) continue;
                        PropertyList addSDIDetailProps = new PropertyList();
                        addSDIDetailProps.setProperty("sdcid", "LV_App");
                        addSDIDetailProps.setProperty("linkid", "App Users");
                        addSDIDetailProps.setProperty("appid", appSysUserGroup.getColumnValues("appid", ";"));
                        addSDIDetailProps.setProperty("sysuserid", appSysUserGroup.getColumnValues("sysuserid", ";"));
                        try {
                            ap.processAction("AddSDIDetail", "1", addSDIDetailProps);
                            continue;
                        }
                        catch (ActionException e) {
                            logger.error("Exception occurred whilst trying to assign Apps to Users for LV_App: " + appSysUserGroup.getString(0, "appid"));
                        }
                    }
                }
            }
        }
    }

    public void flushCaches(PropertyList actionProps) {
        HashSet<String> departmentSet = new HashSet<String>();
        departmentSet.addAll(Arrays.asList(StringUtil.split(actionProps.getProperty("departmentid"), ";")));
        for (String departmentid : departmentSet) {
            CacheUtil.remove(this.getDatabaseid(), "DepartmentUsers", departmentid);
            CacheUtil.removeAllStartWith(this.getDatabaseid(), "DepartmentSchedules", departmentid);
        }
        HashSet<String> userSet = new HashSet<String>();
        userSet.addAll(Arrays.asList(StringUtil.split(actionProps.getProperty("sysuserid"), ";")));
        for (String userid : userSet) {
            CacheUtil.removeAllStartWith(this.getDatabaseid(), "UserSchedules", userid);
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet deptUserDS = sdiData.getDataset("departmentsysuser");
        this.checkAndClearOtherDefaultTestingLab(deptUserDS);
        this.flushCaches(actionProps);
        Department.checkIfUserAlreadyPortalAdmin(deptUserDS, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor());
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.flushCaches(actionProps);
        String[] departmentids = actionProps.getProperty("departmentid", "").split(";");
        String[] sysuserids = actionProps.getProperty("sysuserid", "").split(";");
        SafeSQL safeSQL = new SafeSQL();
        if (departmentids != null && sysuserids != null && departmentids.length == sysuserids.length) {
            for (int i = 0; i < departmentids.length; ++i) {
                String departmentid = departmentids[i];
                String sysuserid = sysuserids[i];
                this.deletePermanentScheduledUsers(departmentid, sysuserid);
            }
        }
    }

    private void createPermanentScheduledUsers(String departmentid) throws SapphireException {
        if (departmentid != null && !departmentid.trim().isEmpty()) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT SYSUSERID FROM DEPARTMENTSYSUSER WHERE DEPARTMENTID=" + safeSQL.addVar(departmentid);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String sysuserid = ds.getString(i, "sysuserid");
                    if (sysuserid == null || sysuserid.trim().isEmpty()) continue;
                    this.enterPermanentSchduledUsers(departmentid, sysuserid);
                }
            }
        }
    }

    private void createPermanentScheduledUsers(DataSet deptUserDS) throws SapphireException {
        if (deptUserDS != null && deptUserDS.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            for (int i = 0; i < deptUserDS.size(); ++i) {
                boolean isWorkArea;
                String departmentid = deptUserDS.getString(i, "departmentid", "");
                String sysuserid = deptUserDS.getString(i, "sysuserid", "");
                String transientFlag = deptUserDS.getString(i, "transientflag", "");
                if (!transientFlag.trim().equals("")) continue;
                safeSQL.reset();
                String sql = "SELECT count(1) ISWORKAREA FROM DEPARTMENT WHERE DEPARTMENTID=" + safeSQL.addVar(departmentid) + " AND WORKASSIGNMENTFLAG='Y'";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds == null || ds.size() != 1) continue;
                boolean bl = isWorkArea = ds.getInt(0, "isworkarea") == 1;
                if (!isWorkArea) continue;
                this.enterPermanentSchduledUsers(departmentid, sysuserid);
            }
        }
    }

    private void enterPermanentSchduledUsers(String departmentid, String sysuserid) throws SapphireException {
        Calendar assignmentStartDate = Calendar.getInstance();
        assignmentStartDate.set(1900, 0, 1);
        Calendar assignmentEndDate = Calendar.getInstance();
        assignmentEndDate.set(2100, 0, 1);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_DepartmentAssignment");
        props.setProperty("departmentassignmentdesc", "Permanent");
        props.setProperty("departmentid", departmentid);
        props.setProperty("sysuserid", sysuserid);
        props.setProperty("assignmentstatus", "Permanent");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        props.setProperty("assignmentstartdt", sdf.format(assignmentStartDate.getTime()));
        props.setProperty("assignmentenddt", sdf.format(assignmentEndDate.getTime()));
        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
    }

    private void deletePermanentScheduledUsers(String departmentid, String sysuserid) throws SapphireException {
        int count = this.getQueryProcessor().getCount("select count(1) from department where  departmentid='" + departmentid + "'");
        if (count > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String deleteSql = null;
            deleteSql = sysuserid.trim().equals("") ? "DELETE FROM DEPARTMENTASSIGNMENT WHERE DEPARTMENTID=" + safeSQL.addVar(departmentid) + " AND ASSIGNMENTSTATUS<>'Pending'" : "DELETE FROM DEPARTMENTASSIGNMENT WHERE DEPARTMENTID=" + safeSQL.addVar(departmentid) + " AND SYSUSERID=" + safeSQL.addVar(sysuserid) + " AND ASSIGNMENTSTATUS<>'Pending'";
            this.getQueryProcessor().execPreparedUpdate(deleteSql, safeSQL.getValues());
        }
    }

    private void checkAndClearOtherDefaultTestingLab(DataSet deptUserDS) throws SapphireException {
        if (deptUserDS != null && deptUserDS.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            for (int i = 0; i < deptUserDS.size(); ++i) {
                String defaultTetsingLabFlag = deptUserDS.getString(i, "defaulttestinglabflag", "N");
                if (!defaultTetsingLabFlag.equalsIgnoreCase("Y")) continue;
                String sysuserid = deptUserDS.getString(i, "sysuserid", "");
                String departmentid = deptUserDS.getString(i, "departmentid", "");
                safeSQL.reset();
                String sql = "select sysuserid,departmentid from departmentsysuser where sysuserid = " + safeSQL.addVar(sysuserid) + " and defaulttestinglabflag ='Y' and departmentid!=" + safeSQL.addVar(departmentid);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds == null || ds.size() <= 0) continue;
                this.clearDefaultTestingLabFlag(ds.getColumnValues("sysuserid", ";"), ds.getColumnValues("departmentid", ";"));
            }
        }
    }

    private void clearDefaultTestingLabFlag(String sysuserid, String departmentid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC);
        props.setProperty("linkid", "department users");
        props.setProperty("sysuserid", sysuserid);
        props.setProperty("departmentid", departmentid);
        props.setProperty("defaulttestinglabflag", "N");
        try {
            this.getActionProcessor().processAction("EditSDIDetail", "1", props);
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Failed to clear DefaultTestingLabFlag", e);
        }
    }

    public static void checkIfUserAlreadyPortalAdmin(DataSet departmentSysUser, QueryProcessor qp, TranslationProcessor tp, ActionProcessor ap) throws SapphireException {
        if (departmentSysUser == null || departmentSysUser.getRowCount() == 0) {
            return;
        }
        if (departmentSysUser.isValidColumn("portaladministratorflag")) {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("portaladministratorflag", "Y");
            DataSet adminRows = departmentSysUser.getFilteredDataSet(filterMap);
            if (adminRows.getRowCount() > 0) {
                HashSet<String> adminUsers = new HashSet<String>();
                for (int i = 0; i < adminRows.getRowCount(); ++i) {
                    adminUsers.add(adminRows.getString(i, "sysuserid"));
                }
                String sql = "SELECT sysuserid, COUNT(departmentid) FROM departmentsysuser WHERE portaladministratorflag = 'Y' AND sysuserid IN ('" + OpalUtil.toDelimitedString(adminUsers, "','") + "') GROUP BY sysuserid HAVING COUNT(departmentid) > 1 ";
                DataSet multiAdminRows = qp.getSqlDataSet(sql);
                if (multiAdminRows.getRowCount() > 0) {
                    throw new SapphireException("UserMultiPortalAdminCheck", "VALIDATION", tp.translate("User can't be Administrator for multiple Portal Clients. User Id: ") + multiAdminRows.getColumnValues("sysuserid", ","));
                }
                sql = "SELECT dsu.sysuserid, dsu.departmentid, dsu.portaladministratorflag, s.basedepartment, s.logonname FROM departmentsysuser dsu, sysuser s WHERE dsu.sysuserid = s.sysuserid AND dsu.portaladministratorflag = 'Y' AND s.basedepartment != dsu.departmentid AND s.sysuserid IN ('" + OpalUtil.toDelimitedString(adminUsers, "','") + "') ORDER BY s.sysuserid, dsu.departmentid";
                DataSet crossClientAdmin = qp.getSqlDataSet(sql);
                if (crossClientAdmin.getRowCount() > 0) {
                    throw new SapphireException("UserCrossPortalAdminCheck", "VALIDATION", tp.translate("User can only be Administrator for it's Base Client. User Id: ") + crossClientAdmin.getColumnValues("sysuserid", ","));
                }
                SafeSQL safeSQL = new SafeSQL();
                String adminRoleSql = "SELECT u.sysuserid, 'App_Admin' roleid FROM sysuser u WHERE u.sysuserid IN (" + safeSQL.addIn(adminUsers) + ") AND (SELECT 1 FROM sysuserrole ur WHERE u.sysuserid = ur.sysuserid AND ur.roleid = 'App_Admin') IS NULL";
                DataSet adminRoleDs = qp.getPreparedSqlDataSet(adminRoleSql, safeSQL.getValues());
                if (!adminRoleDs.isEmpty()) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "User");
                    props.setProperty("linkid", "user roles");
                    props.setProperty("sysuserid", adminRoleDs.getColumnValues("sysuserid", ";"));
                    props.setProperty("roleid", adminRoleDs.getColumnValues("roleid", ";"));
                    try {
                        ap.processAction("AddSDIDetail", "1", props);
                    }
                    catch (Exception e) {
                        throw new SapphireException("DB_ACTION_FAILED", "Failed to add App_Admin role for Portal users: " + adminRoleDs.getColumnValues("sysuserid", ","), e);
                    }
                }
            }
        }
    }
}

