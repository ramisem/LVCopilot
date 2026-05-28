/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UserRO
extends BaseSDCRO {
    public void initialize(SapphireConnection sapphireConnection) throws SapphireException {
        super.initialize("User", sapphireConnection);
    }

    public String getUserId() {
        return this.getKeyid1();
    }

    public String getUserDesc() {
        return this.getDescription();
    }

    public String getUserType() {
        String colValue = this.getPrimaryValue("nameduserflag");
        if ("S".equals(colValue)) {
            return "Full Named";
        }
        if ("A".equals(colValue)) {
            return "External App User";
        }
        if ("C".equals(colValue)) {
            return "Full Concurrent";
        }
        if ("V".equals(colValue)) {
            return "Virtual";
        }
        if ("I".equals(colValue)) {
            return "Virtual Concurrent User";
        }
        if ("U".equals(colValue)) {
            return "Unnamed User";
        }
        return colValue;
    }

    public String getPasswordExpiryDt() {
        return this.getPrimaryValue("passwordexpirydt");
    }

    public String getForceChangePassword() {
        String colValue = this.getPrimaryValue("changepasswordflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        if ("N".equals(colValue)) {
            return "No";
        }
        return "";
    }

    public String getLocale() {
        String localeid = this.getPrimaryValue("localeid");
        return localeid;
    }

    public String getTimeZone() {
        return this.getPrimaryValue("timezone");
    }

    public String getLanguage() {
        return this.getPrimaryValue("languageid");
    }

    public String getStatus() {
        String colValue = this.getPrimaryValue("disabledflag");
        if ("Y".equals(colValue)) {
            return "Disabled";
        }
        if ("N".equals(colValue)) {
            return "Active";
        }
        return "";
    }

    public String getDisabledReason() {
        return this.getPrimaryValue("disabledreason");
    }

    public String getGLPFlag() {
        return this.getPrimaryValue("glpflag");
    }

    public String getDefaultDepartment() {
        return this.getPrimaryValue("defaultdepartment");
    }

    public String getBaseDepartment() {
        return this.getPrimaryValue("basedepartment");
    }

    public String getDefaultJobType() {
        return this.getPrimaryValue("defaultjobtype");
    }

    public String getLastLogOnJobType() {
        return this.getPrimaryValue("lastjobtype");
    }

    public String getSecurityType() {
        String colValue = this.getPrimaryValue("securitytypeflag");
        if ("U".equals(colValue)) {
            return "User";
        }
        if ("J".equals(colValue)) {
            return "Job Type";
        }
        return "";
    }

    public String getAuthenticationType() {
        String colValue = this.getPrimaryValue("authenticationtypeflag");
        if ("E".equals(colValue)) {
            return "External";
        }
        if ("I".equals(colValue)) {
            return "Internal";
        }
        return "";
    }

    public String getEmail() {
        return this.getPrimaryValue("email");
    }

    public String getRoles() {
        DataSet ds = this.getDataSet("sysuserrole");
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getColumnValues("roleid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public String getDepartments() {
        DataSet ds = this.getDataSet("departmentsysuser");
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getColumnValues("departmentid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public String getModules() {
        DataSet ds = this.getDataSet("modulesysuser");
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getColumnValues("moduleid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public String getJobTypes() {
        DataSet ds = this.getDataSet("sysuserjobtype");
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getColumnValues("jobtypeid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public DataSet getAllUserProfileProperties() {
        if (!this.dataSource.equals("XMLREPORT")) {
            String sqlPP = "SELECT propertyid, propertyvalue FROM profileproperty where PROFILEID='System' and sysuserid='" + this.getUserId() + "' ";
            return this.getQueryProcessor().getSqlDataSet(sqlPP);
        }
        String fileName = this.refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        fileName = fileName.replace(".xml", "_profileprops.xml");
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    public PropertyList getUserProfile() {
        PropertyList props = new PropertyList();
        DataSet profileProperties = this.getAllUserProfileProperties();
        props.setProperty("logonpageurl", this.getProfileProperty(profileProperties, "logonpageurl"));
        props.setProperty("bourl", this.getProfileProperty(profileProperties, "bourl"));
        props.setProperty("bodocumentdomain", this.getProfileProperty(profileProperties, "bodocumentdomain"));
        props.setProperty("bodomain", this.getProfileProperty(profileProperties, "bodomain"));
        props.setProperty("boexchangemode", this.getProfileProperty(profileProperties, "boexchangemode"));
        props.setProperty("bouniverse", this.getProfileProperty(profileProperties, "bouniverse"));
        props.setProperty("bousername", this.getProfileProperty(profileProperties, "bousername"));
        props.setProperty("bopassword", this.getProfileProperty(profileProperties, "bopassword"));
        props.setProperty("boauthenticationtype", this.getProfileProperty(profileProperties, "boauthenticationtype"));
        return props;
    }

    public String getProfileProperty(DataSet profileProperties, String propertyid) {
        for (int i = 0; profileProperties != null && i < profileProperties.size(); ++i) {
            String currPropertyId = profileProperties.getString(i, "propertyid");
            if (!currPropertyId.equals(propertyid)) continue;
            String currVal = profileProperties.getString(i, "propertyvalue");
            if (currVal == null) {
                currVal = "";
            }
            return currVal;
        }
        return "";
    }

    public DataSet getUserCategories() {
        DataSet ds = this.getCategories();
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Category ID", 0);
        ret.addColumnValues("Category ID", 0, ds.getColumnValues("categoryid", ";"), ";");
        return ret;
    }

    public DataSet getSDCAccessMatrix() {
        if (this.dataSource.equals("DATABASE")) {
            return this.getUserSDCAccessInfo();
        }
        if (this.dataSource.equals("INPUT")) {
            return this.getUserSDCAccessInfoFromSDIData();
        }
        String fileName = this.refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        fileName = fileName.replace(".xml", "_sdcaccess.xml");
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getAccessInfoForSDCFromDB(String sdc) {
        String sql = "SELECT sdcid , operationid, sysuserid,accesstype from sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype  IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAccessInfoForSDCFromSDIData(String sdc) {
        DataSet ds = this.currentSDIData.getDataset("sdcsecurity");
        DataSet ret = new DataSet();
        if (ds != null) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String curracesstype = ds.getString(i, "accesstype", "");
                if (!ds.getString(i, "sdcid", "").equals(sdc) || !curracesstype.equals("member") && !curracesstype.equals("owner") && !curracesstype.equals("world")) continue;
                ret.copyRow(ds, i, 1);
            }
        }
        return ret;
    }

    private DataSet getSDCOperationsFromDB() {
        String sql = "select distinct sdcid SDC from sdcoperation";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    public boolean isAccessSetForSDC(String sdc) {
        if (this.currentSDI != null) {
            String sql = "select count(accesstype) from  sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype  IN ('member', 'world', 'owner')";
            try {
                if (this.getQueryProcessor().getCount(sql) > 0) {
                    return true;
                }
            }
            catch (SapphireException e) {
                Trace.log("Error Occurred at isAccessSetForSDC, error is " + e.getMessage());
            }
        }
        return false;
    }

    private DataSet getUserSDCAccessInfo() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("SysUserID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        ds.addColumn("Operation ViewMaskedData", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isAccessSetForSDC(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "SysUserID", this.currentSDI.getKeyid1());
            DataSet sdcAccess = this.getAccessInfoForSDCFromDB(sdcs.getValue(i, "SDC"));
            if (sdcAccess == null || sdcAccess.getRowCount() <= 0) continue;
            for (int j = 0; j < sdcAccess.getRowCount(); ++j) {
                String sdiOperations = sdcAccess.getValue(j, "operationid");
                if (sdiOperations.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("List")) {
                    ds.setString(currRow, "Operation List", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                ds.setString(currRow, "Operation ViewMaskedData", sdcAccess.getValue(j, "accesstype"));
            }
        }
        return ds;
    }

    private DataSet getUserSDCAccessInfoFromSDIData() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("SysUserID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        ds.addColumn("Operation ViewMaskedData", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isAccessSetForSDC(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "SysUserID", this.currentSDI.getKeyid1());
            DataSet sdcAccess = this.getAccessInfoForSDCFromSDIData(sdcs.getValue(i, "SDC"));
            if (sdcAccess == null || sdcAccess.getRowCount() <= 0) continue;
            for (int j = 0; j < sdcAccess.getRowCount(); ++j) {
                String sdiOperations = sdcAccess.getValue(j, "operationid");
                if (sdiOperations.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("List")) {
                    ds.setString(currRow, "Operation List", sdcAccess.getValue(j, "accesstype"));
                    continue;
                }
                ds.setString(currRow, "Operation ViewMaskedData", sdcAccess.getValue(j, "accesstype"));
            }
        }
        return ds;
    }

    public String createSDCAccessRowData(String type) {
        if (type.length() == 0) {
            return "";
        }
        if (type.equals("owner")) {
            return "O";
        }
        if (type.equals("member")) {
            return "M";
        }
        return "W";
    }

    public DataSet getDeptAccessMatrix() {
        if (this.dataSource.equals("DATABASE")) {
            return this.getUserSDCDepartmentAccessInfoFromDB();
        }
        if (this.dataSource.equals("INPUT")) {
            return this.getUserSDCDepartmentAccessInfoFromSDIData();
        }
        String fileName = this.refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        fileName = fileName.replace(".xml", "_deptaccess.xml");
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    public boolean isDepartmentAccessSet(String sdc) {
        String sql = "select count(accesstype) from  sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        try {
            if (this.getQueryProcessor().getCount(sql) > 0) {
                return true;
            }
        }
        catch (SapphireException e) {
            Trace.log("Error Occurred at isDepartmentAccessSet(), error is " + e.getMessage());
        }
        return false;
    }

    private DataSet getDeptAccess(String sdc) {
        String sql = "select sdcid , operationid, sysuserid,accesstype from  sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAllDeptAccessOperationsFromDB(String sdc) {
        String sql = "select  distinct operationid from  sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAllDeptAccessOperationsFromSDIData(String sdc) {
        DataSet ds = this.currentSDIData.getDataset("sdcsecurity");
        DataSet ret = new DataSet();
        ret.addColumn("operationid", 0);
        if (ds != null) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String currsdcid = ds.getString(i, "sdcid", "");
                String curraccesstype = ds.getString(i, "accesstype", "");
                if (!currsdcid.equals(sdc) || curraccesstype.equals("owner") || curraccesstype.equals("world") || curraccesstype.equals("member")) continue;
                String op = ds.getString(i, "operationid");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("operationid", "op");
                if (ret.findRow(filter) != -1) continue;
                int row = ret.addRow();
                ret.setString(row, "operationid", op);
            }
        }
        return ret;
    }

    private DataSet getAllDeptAccessForOperationFromDB(String operation, String sdc) {
        String sql = "select  accesstype from  sdcsecurity where  SYSUSERID ='" + this.currentSDI.getKeyid1() + "' AND operationid ='" + operation + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAllDeptAccessForOperationFromSDIData(String operation, String sdc) {
        DataSet sdcsecurity = this.currentSDIData.getDataset("sdcsecurity");
        DataSet ret = new DataSet();
        for (int i = 0; i < sdcsecurity.getRowCount(); ++i) {
            String currentop = sdcsecurity.getString(i, "operationid", "");
            String accesstype = sdcsecurity.getString(i, "accesstype", "");
            String currsdcid = sdcsecurity.getString(i, "sdcid", "");
            if (!currentop.equals(operation) || !currsdcid.equals(sdc) || accesstype.equals("member") || accesstype.equals("owner") || accesstype.equals("world")) continue;
            ret.copyRow(sdcsecurity, i, 1);
        }
        return ret;
    }

    private String getCommaSeperatedValues(DataSet ds) {
        if (ds == null) {
            return "";
        }
        String str = ds.getColumnValues("accesstype", ",");
        return str;
    }

    private DataSet getUserSDCDepartmentAccessInfoFromDB() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("SysUserID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        ds.addColumn("Operation ViewMaskedData", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isDepartmentAccessSet(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "SysUserID", this.currentSDI.getKeyid1());
            DataSet operations = this.getAllDeptAccessOperationsFromDB(sdcs.getValue(i, "SDC"));
            if (operations == null || operations.getRowCount() <= 0) continue;
            for (int j = 0; j < operations.getRowCount(); ++j) {
                String sdiOperations = operations.getValue(j, "operationid");
                if (sdiOperations.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromDB(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromDB(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("List")) {
                    ds.setString(currRow, "Operation List", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromDB(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                ds.setString(currRow, "Operation ViewMaskedData", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromDB(sdiOperations, sdcs.getValue(i, "SDC"))));
            }
        }
        return ds;
    }

    private DataSet getUserSDCDepartmentAccessInfoFromSDIData() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("SysUserID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        ds.addColumn("Operation ViewMaskedData", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isDepartmentAccessSet(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "SysUserID", this.currentSDI.getKeyid1());
            DataSet operations = this.getAllDeptAccessOperationsFromSDIData(sdcs.getValue(i, "SDC"));
            if (operations == null || operations.getRowCount() <= 0) continue;
            for (int j = 0; j < operations.getRowCount(); ++j) {
                String sdiOperations = operations.getValue(j, "operationid");
                if (sdiOperations.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromSDIData(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromSDIData(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperations.equalsIgnoreCase("List")) {
                    ds.setString(currRow, "Operation List", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromSDIData(sdiOperations, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                ds.setString(currRow, "Operation ViewMaskedData", this.getCommaSeperatedValues(this.getAllDeptAccessForOperationFromSDIData(sdiOperations, sdcs.getValue(i, "SDC"))));
            }
        }
        return ds;
    }
}

