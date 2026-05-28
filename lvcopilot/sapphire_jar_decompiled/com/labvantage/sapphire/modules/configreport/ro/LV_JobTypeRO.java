/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;

public class LV_JobTypeRO
extends BaseSDCRO {
    public String getLV_JobTypeId() {
        return this.getKeyid1();
    }

    public String getJobTypeDesc() {
        return this.getDescription();
    }

    public String getDefaultLogonURL() {
        return this.getPrimaryValue("defaultlogonurl");
    }

    public String getDefaultDepartment() {
        return this.getPrimaryValue("defaultdepartment");
    }

    public String getRoles() {
        DataSet ds = this.getDataSet("jobtyperole");
        if (ds != null) {
            return ds.getColumnValues("roleid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public String getDepartments() {
        DataSet ds = this.getDataSet("jobtypedepartment");
        if (ds != null) {
            return ds.getColumnValues("departmentid", ";").replaceAll(";", ",");
        }
        return "";
    }

    public String getUsers() {
        DataSet ds = this.getDataSet("sysuserjobtype");
        if (ds != null) {
            return ds.getColumnValues("SYSUSERID", ";").replaceAll(";", ",");
        }
        return "";
    }

    public DataSet getSDCAccessMatrix() {
        if (this.dataSource.equals("DATABASE")) {
            return this.getAllLV_JobTypeSDCAccessDetailsMatrixFromDB();
        }
        if (this.dataSource.equals("INPUT")) {
            return this.getAllLV_JobTypeSDCAccessDetailsMatrixFromSDIData();
        }
        if (this.currentSDI != null) {
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
        }
        return null;
    }

    public DataSet getDeptAccessMatrix() {
        if (this.dataSource.equals("DATABASE")) {
            return this.getAllLV_JobTypeSDCDeptAccessDetailsMatrixFromDB();
        }
        if (this.dataSource.equals("INPUT")) {
            return this.getAllLV_JobTypeSDCDeptAccessDetailsMatrixFromSDIData();
        }
        if (this.currentSDI != null) {
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
        }
        return null;
    }

    private DataSet getAccessInfoForSDCFromDB(String sdc) {
        String sql = "select sdcid , operationid, jobtypeid,accesstype from  sdcjobtypesecurity where  JOBTYPEID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype  IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAccessInfoForSDCFromSDIData(String sdc) {
        DataSet ds = this.currentSDIData.getDataset("sdcjobtypesecurity");
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

    public boolean isDepartmentAccessSetForSDCFromDB(String sdc) {
        String sql = "select count(accesstype) from  sdcjobtypesecurity where  JOBTYPEID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        try {
            if (this.getQueryProcessor().getCount(sql) > 0) {
                return true;
            }
        }
        catch (SapphireException e) {
            Trace.log("Error Occurred at isDepartmentAccessSet, error is " + e.getMessage());
        }
        return false;
    }

    public boolean isDepartmentAccessSetForSDCFromSDIData(String sdc) {
        DataSet sdcjobtypesecurity = this.currentSDIData.getDataset("sdcjobtypesecurity");
        if (sdcjobtypesecurity != null) {
            for (int i = 0; i < sdcjobtypesecurity.getRowCount(); ++i) {
                String currsdcid = sdcjobtypesecurity.getString(i, "sdcid");
                String accesstype = sdcjobtypesecurity.getString(i, "accesstype");
                if (!currsdcid.equals(sdc) || accesstype.equals("member") || accesstype.equals("world") || accesstype.equals("owner")) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isAccessSetForSDCFromDB(String sdc) {
        String sql = "select count(accesstype) from  sdcjobtypesecurity where  JOBTYPEID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype  IN ('member', 'world', 'owner')";
        try {
            if (this.getQueryProcessor().getCount(sql) > 0) {
                return true;
            }
        }
        catch (SapphireException e) {
            Trace.log("Error Occurred at isAccessSetForSDC, error is " + e.getMessage());
        }
        return false;
    }

    public boolean isAccessSetForSDCFromSDIData(String sdc) {
        DataSet sdcjobtypesecurity = this.currentSDIData.getDataset("sdcjobtypesecurity");
        if (sdcjobtypesecurity != null) {
            for (int i = 0; i < sdcjobtypesecurity.getRowCount(); ++i) {
                String currsdcid = sdcjobtypesecurity.getString(i, "sdcid");
                String accesstype = sdcjobtypesecurity.getString(i, "accesstype");
                if (!currsdcid.equals(sdc) || !accesstype.equals("member") && !accesstype.equals("world") && !accesstype.equals("owner")) continue;
                return true;
            }
        }
        return false;
    }

    private DataSet getSDCOperationsFromDB() {
        String sql = "select distinct sdcid SDC from sdcoperation";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAllLV_JobTypeSDCAccessDetailsMatrixFromDB() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("JobTypeID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isAccessSetForSDCFromDB(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "JobTypeID", this.currentSDI.getKeyid1());
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
                if (!sdiOperations.equalsIgnoreCase("List")) continue;
                ds.setString(currRow, "Operation List", sdcAccess.getValue(j, "accesstype"));
            }
        }
        return ds;
    }

    private DataSet getAllLV_JobTypeSDCAccessDetailsMatrixFromSDIData() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("JobTypeID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isAccessSetForSDCFromSDIData(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "JobTypeID", this.currentSDI.getKeyid1());
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
                if (!sdiOperations.equalsIgnoreCase("List")) continue;
                ds.setString(currRow, "Operation List", sdcAccess.getValue(j, "accesstype"));
            }
        }
        return ds;
    }

    private DataSet getAllDeptAccessOperationsFromDB(String sdc) {
        String sql = "select  distinct operationid from  sdcjobtypesecurity where  JOBTYPEID ='" + this.currentSDI.getKeyid1() + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getAllDeptAccessOperationsFromSDIData(String sdc) {
        DataSet sdcjobtypesecurity = this.currentSDIData.getDataset("sdcjobtypesecurity");
        DataSet ret = new DataSet();
        if (sdcjobtypesecurity != null) {
            for (int i = 0; i < sdcjobtypesecurity.getRowCount(); ++i) {
                String currsdcid = sdcjobtypesecurity.getString(i, "sdcid");
                String accesstype = sdcjobtypesecurity.getString(i, "accesstype");
                if (!currsdcid.equals(sdc) || accesstype.equals("memeber") || accesstype.equals("world") || accesstype.equals("owner")) continue;
                ret.copyRow(sdcjobtypesecurity, i, 1);
            }
        }
        return ret;
    }

    private DataSet getDeptAccessForOperationFromDB(String operation, String sdc) {
        String sql = "select  accesstype from  sdcjobtypesecurity where  JOBTYPEID ='" + this.currentSDI.getKeyid1() + "' AND operationid ='" + operation + "' AND sdcid ='" + sdc + "' AND accesstype NOT IN ('member', 'world', 'owner')";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getDeptAccessForOperationFromSDIData(String operation, String sdc) {
        DataSet sdcjobtypesecurity = this.currentSDIData.getDataset("sdcjobtypesecurity");
        DataSet ret = new DataSet();
        if (sdcjobtypesecurity != null) {
            for (int i = 0; i < sdcjobtypesecurity.getRowCount(); ++i) {
                String currsdcid = sdcjobtypesecurity.getString(i, "sdcid");
                String accesstype = sdcjobtypesecurity.getString(i, "accesstype");
                String currop = sdcjobtypesecurity.getString(i, "operationid");
                if (!currop.equals(operation) || !currsdcid.equals(sdc) || accesstype.equals("member") || accesstype.equals("world") || accesstype.equals("owner")) continue;
                ret.copyRow(sdcjobtypesecurity, i, 1);
            }
        }
        return ret;
    }

    private String getCommaSeperatedValues(DataSet ds) {
        if (ds == null || ds.getRowCount() == 0) {
            return "";
        }
        String str = ds.getColumnValues("accesstype", ",");
        return str;
    }

    private DataSet getAllLV_JobTypeSDCDeptAccessDetailsMatrixFromDB() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("JobTypeID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isDepartmentAccessSetForSDCFromDB(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "JobTypeID", this.currentSDI.getKeyid1());
            DataSet operations = this.getAllDeptAccessOperationsFromDB(sdcs.getValue(i, "SDC"));
            if (operations == null || operations.getRowCount() <= 0) continue;
            for (int j = 0; j < operations.getRowCount(); ++j) {
                String sdiOperation = operations.getValue(j, "operationid");
                if (sdiOperation.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromDB(sdiOperation, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperation.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromDB(sdiOperation, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (!sdiOperation.equalsIgnoreCase("List")) continue;
                ds.setString(currRow, "Operation List", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromDB(sdiOperation, sdcs.getValue(i, "SDC"))));
            }
        }
        return ds;
    }

    private DataSet getAllLV_JobTypeSDCDeptAccessDetailsMatrixFromSDIData() {
        DataSet sdcs = this.getSDCOperationsFromDB();
        DataSet ds = new DataSet();
        ds.addColumn("Sdcid", 0);
        ds.addColumn("JobTypeID", 0);
        ds.addColumn("Operation Delete", 0);
        ds.addColumn("Operation Edit", 0);
        ds.addColumn("Operation List", 0);
        for (int i = 0; i < sdcs.size(); ++i) {
            if (!this.isDepartmentAccessSetForSDCFromSDIData(sdcs.getValue(i, "SDC"))) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Sdcid", sdcs.getValue(i, "SDC"));
            ds.setString(currRow, "JobTypeID", this.currentSDI.getKeyid1());
            DataSet operations = this.getAllDeptAccessOperationsFromSDIData(sdcs.getValue(i, "SDC"));
            if (operations == null || operations.getRowCount() <= 0) continue;
            for (int j = 0; j < operations.getRowCount(); ++j) {
                String sdiOperation = operations.getValue(j, "operationid");
                if (sdiOperation.equalsIgnoreCase("Delete")) {
                    ds.setString(currRow, "Operation Delete", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromSDIData(sdiOperation, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (sdiOperation.equalsIgnoreCase("Edit")) {
                    ds.setString(currRow, "Operation Edit", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromSDIData(sdiOperation, sdcs.getValue(i, "SDC"))));
                    continue;
                }
                if (!sdiOperation.equalsIgnoreCase("List")) continue;
                ds.setString(currRow, "Operation List", this.getCommaSeperatedValues(this.getDeptAccessForOperationFromSDIData(sdiOperation, sdcs.getValue(i, "SDC"))));
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
        if (type.equals("world")) {
            return "W";
        }
        return "";
    }
}

