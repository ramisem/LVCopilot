/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class WAPDataBuilder
extends BaseCustom
implements WAPConstants {
    private DateTimeUtil dtu;
    private static final String LOGNAME = "WAPCommands";
    Writer out;
    QueryProcessor qp;
    ActionProcessor ap;
    private int resourceNum = 0;
    private int workiteminstrumentid = 0;
    private int workitemitemid = 0;

    public WAPDataBuilder(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.dtu = new DateTimeUtil(cp.getConnectionInfo(connectionid));
        this.qp = new QueryProcessor(connectionid);
        this.ap = new ActionProcessor(connectionid);
    }

    public WAPDataBuilder(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.dtu = new DateTimeUtil(cp.getConnectionInfo(connectionid));
        this.qp = new QueryProcessor(rakFile, connectionid);
        this.ap = new ActionProcessor(rakFile, connectionid);
    }

    public void setOut(Writer out) {
        this.out = out;
    }

    private void logMessage(String s) throws IOException {
        Trace.logInfo("WAP DEMO BUILDER: " + s);
        if (this.out != null) {
            this.out.write(s + "<br>");
        }
    }

    public void cleanteTestingLab(String testinglab) throws SapphireException, IOException {
        this.cleanDepartment(testinglab, false);
    }

    public void createTestingLab(String testinglab) throws SapphireException, IOException {
        this.createDepartment(testinglab, "", false);
    }

    public void cleanWorkarea(String workarea) throws SapphireException, IOException {
        this.cleanDepartment(workarea, true);
    }

    public void createWorkarea(String workarea, String testinglab) throws SapphireException, IOException {
        this.createDepartment(workarea, testinglab, true);
    }

    private void cleanDepartment(String departmentid, boolean workarea) throws SapphireException, IOException {
        this.logMessage("Cleaning " + (workarea ? "WorkArea" : "TestingLab") + " " + departmentid);
        String calendarid = this.qp.getPreparedSqlDataSet("SELECT calendarid FROM department WHERE departmentid=?", (Object[])new String[]{departmentid}).getValue(0, "calendarid");
        this.qp.execPreparedUpdate("delete from departmentsysuser WHERE departmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("delete from departmentshift WHERE departmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("delete from departmentassignment WHERE departmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("update sysuser set basedepartment = null WHERE basedepartment=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("update sysuser set defaultdepartment = null WHERE defaultdepartment=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE workitem set testingdepartmentid =null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE workitem set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE paramlist set testingdepartmentid =null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE paramlist set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE sdiworkitem set testingdepartmentid =null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE sdiworkitem set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE sdiworkitem set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE s_sample set testingdepartmentid =null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE s_sample set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE sdidata set testingdepartmentid =null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE sdidata set workareadepartmentid =null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE department set parentdepartmentid =null WHERE parentdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE workorder set testingdepartmentid=null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE workorder set workareadepartmentid=null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE instrument set testingdepartmentid=null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE instrument set workareadepartmentid=null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE s_samplepoint set testingdepartmentid=null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE s_location set testingdepartmentid=null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE activity set testingdepartmentid=null WHERE testingdepartmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("UPDATE activityresource set workareadepartmentid=null WHERE workareadepartmentid=?", new String[]{departmentid});
        this.deleteCalendar(calendarid);
        this.qp.execPreparedUpdate("DELETE FROM departmentsysuser WHERE departmentid=?", new String[]{departmentid});
        this.qp.execPreparedUpdate("DELETE FROM department WHERE departmentid=?", new String[]{departmentid});
    }

    private void createDepartment(String departmentid, String parentdepartmentid, boolean workarea) throws SapphireException, IOException {
        this.cleanDepartment(departmentid, workarea);
        this.logMessage("Creating " + (workarea ? "WorkArea" : "TestingLab") + " " + departmentid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Department");
        props.setProperty("keyid1", departmentid);
        if (workarea) {
            props.setProperty("workassignmentflag", "Y");
            props.setProperty("parentdepartmentid", parentdepartmentid);
        } else {
            props.setProperty("testingflag", "Y");
        }
        this.ap.processAction("AddSDI", "1", props);
    }

    private void deleteCalendar(String calendarid) throws IOException {
        this.logMessage("Cleaning Calendar " + calendarid);
        this.qp.execPreparedUpdate("update department set calendarid=null where calendarid=?", new String[]{calendarid});
        this.qp.execPreparedUpdate("update sysuser set calendarid=null where calendarid=?", new String[]{calendarid});
        this.qp.execPreparedUpdate("update instrument set calendarid=null where calendarid=?", new String[]{calendarid});
        this.qp.execPreparedUpdate("DELETE from calendaritem WHERE calendarid=?", new String[]{calendarid});
        this.qp.execPreparedUpdate("DELETE from calendar WHERE calendarid=?", new String[]{calendarid});
    }

    public void createUser(String sysuserid, String username, String testinglabid) throws ActionException, IOException {
        this.createUser(sysuserid, username, testinglabid, "");
    }

    public String createSample(int copies, String templateid, String productid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Sample");
        props.setProperty("copies", "" + copies);
        props.setProperty("templateid", templateid);
        if (productid.length() > 0) {
            props.setProperty("productid", productid);
            props.setProperty("productversionid", "1");
        }
        this.ap.processAction("AddSDI", "1", props);
        return props.getProperty("newkeyid1");
    }

    public void cleanProduct(String productid) throws ActionException, IOException {
        this.logMessage("Cleaning product " + productid);
        this.qp.execPreparedUpdate("update s_sample set productid=null, productversionid=null where productid=?", new String[]{productid});
        this.qp.execPreparedUpdate("DELETE from sdiresourcerequirement WHERE keyid1=?", new String[]{productid});
        this.qp.execPreparedUpdate("DELETE from sdiresourcerequirement WHERE keyid1=?", new String[]{productid});
        this.qp.execPreparedUpdate("DELETE from s_requestitemdetail WHERE requestitemid in ( SELECT s_requestitemid FROM s_requestitem WHERE productid=? )", new String[]{productid});
        this.qp.execPreparedUpdate("DELETE from s_requestitem WHERE productid=?", new String[]{productid});
        this.qp.execPreparedUpdate("delete from s_product where s_productid=?", new String[]{productid});
    }

    public void createProduct(String productid) throws ActionException, IOException {
        this.cleanProduct(productid);
        this.logMessage("Creating product " + productid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Product");
        props.setProperty("keyid1", productid);
        props.setProperty("keyid2", "1");
        props.setProperty("createactivityrule", "On Demand");
        props.setProperty("maxactivitysize", "20");
        this.ap.processAction("AddSDI", "1", props);
    }

    public void cleanUser(String sysuserid, boolean calendar) throws ActionException, IOException {
        this.logMessage("Cleaning User " + sysuserid);
        String calendarid = calendar ? this.qp.getPreparedSqlDataSet("SELECT calendarid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sysuserid}).getValue(0, "calendarid") : "";
        this.qp.execPreparedUpdate("delete from worksheetactivitylog WHERE activityby=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("update workbook set ownerid=null WHERE ownerid=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("update sdiworkitem set appliedby=null where appliedby=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("update sdiresourcerequirement set autoassignanalystid=null where autoassignanalystid=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("update activityresource set analystid=null where analystid=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("DELETE FROM departmentsysuser WHERE sysuserid=?", new String[]{sysuserid});
        this.qp.execPreparedUpdate("delete from departmentassignment WHERE sysuserid=?", new String[]{sysuserid});
        if (calendarid.length() > 0) {
            this.deleteCalendar(calendarid);
        }
    }

    public void createUser(String sysuserid, String username, String testinglabid, String shiftid) throws ActionException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT sysuserid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sysuserid}).getRowCount() > 0) {
            this.cleanUser(sysuserid, false);
            this.logMessage("User " + sysuserid + " already exists, so update");
        } else {
            this.cleanUser(sysuserid, true);
            this.logMessage("Creating User " + sysuserid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("keyid1", sysuserid);
            props.setProperty("templateid", "admin");
            props.setProperty("sysuserdesc", username);
            props.setProperty("password", sysuserid);
            props.setProperty("nameduserflag", "C");
            props.setProperty("calendarid", "(null)");
            props.setProperty("glpflag", "Y");
            props.put("bypassformatcheck", "Y");
            this.ap.processAction("AddSDI", "1", props);
        }
        if (testinglabid.length() > 0) {
            this.qp.execPreparedUpdate("INSERT INTO departmentsysuser ( departmentid, sysuserid, defaulttestinglabflag, shiftid ) VALUES ( ?, ?, ?, ? )", new String[]{testinglabid, sysuserid, "Y", shiftid});
        }
    }

    public void addUserToWorkarea(String sysuserid, String departmentid) throws IOException {
        this.logMessage("Assigning " + sysuserid + " to " + departmentid);
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("sdcid", "Department");
        propertyList.setProperty("linkid", "department users");
        propertyList.setProperty("departmentid", departmentid);
        propertyList.setProperty("sysuserid", sysuserid);
        try {
            this.getActionProcessor().processAction("AddSDIDetail", "1", propertyList);
        }
        catch (Exception e) {
            this.logMessage(e.getMessage());
        }
    }

    public void cleanWorkitem(String workitemid) throws ActionException, IOException {
        this.logMessage("Cleaning workitem " + workitemid);
        this.qp.execPreparedUpdate("DELETE from sdiworkitemrelation WHERE workitemid=?", new String[]{workitemid});
        this.qp.execPreparedUpdate("DELETE from sdiworkitemitem WHERE workitemid=?", new String[]{workitemid});
        this.qp.execPreparedUpdate("DELETE from sdiworkitem WHERE workitemid=?", new String[]{workitemid});
        this.qp.execPreparedUpdate("DELETE from workitemitem WHERE workitemid=?", new String[]{workitemid});
        this.qp.execPreparedUpdate("DELETE from workiteminstrument WHERE workitemid=?", new String[]{workitemid});
        this.qp.execPreparedUpdate("DELETE from sdiresourcerequirement WHERE keyid1=?", new String[]{workitemid});
    }

    public void createWorkitem(String workitemid, String testeingdepartmentid, String workareadepartmentid, boolean byDataset) throws ActionException, IOException {
        this.cleanWorkitem(workitemid);
        this.logMessage("Creating workitem " + workitemid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "WorkItem");
        props.setProperty("keyid1", workitemid);
        props.setProperty("keyid2", "1");
        props.setProperty("testingdepartmentid", testeingdepartmentid);
        props.setProperty("workareadepartmentid", workareadepartmentid);
        props.setProperty("copies", "1");
        props.setProperty("maxactivitysize", "100");
        props.setProperty("workitemtypeflag", "W");
        props.setProperty("createactivityrule", byDataset ? "On Demand By DataSet" : "On Demand By WorkItem");
        this.ap.processAction("AddOrEditSDI", "1", props);
    }

    public void addWorkItemResourceUser(String workitemid, String label, String analysttype, String durationRule, String linktoContext) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires an analyst for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, analysttype, durationrule, resourcelabel, linktocontext ) values (?,?,?,?,?,?,?,?,?,?)", new Object[]{"WorkItem", workitemid, "1", "(null)", "" + this.resourceNum++, "A", analysttype, durationRule, label, linktoContext});
    }

    public void addWorkItemResourceUserWithPreferred(String workitemid, String label, String analysttype, String durationRule, String linktoContext, String preferredUser, String preferredWorkarea) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires an analyst for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, analysttype, durationrule, resourcelabel, linktocontext, autoassignflag, autoassignanalystid, autoassigndepartmentid ) values (?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{"WorkItem", workitemid, "1", "(null)", "" + this.resourceNum++, "A", analysttype, durationRule, label, linktoContext, preferredUser.length() > 0 ? "I" : (preferredWorkarea.length() > 0 ? "W" : ""), preferredUser, preferredWorkarea});
    }

    public String addWorkitemParamlist(String workitemid, String paramlistid, String variantid, int maxactivitysize) throws IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Adding " + paramlistid + " (" + variantid + ") to " + workitemid);
        String id = "" + this.workitemitemid++;
        this.qp.execPreparedUpdate("insert into workitemitem ( workitemid, workitemversionid, workitemitemid, sdcid, keyid1, keyid2, keyid3, forcenewflag, maxactivitysize ) values ( ?,?,?,?,?,?,?,?,?)", new Object[]{workitemid, "1", id, "ParamList", paramlistid, "1", variantid, "Y", maxactivitysize});
        return id;
    }

    public void cleanParamList(String paramlistid, String variantid) throws IOException, ActionException {
        this.logMessage("Cleaning parameter list " + paramlistid + " - " + variantid);
        this.qp.execPreparedUpdate("DELETE from sdiworkitemitem WHERE itemkeyid1=?", new String[]{paramlistid});
        this.qp.execPreparedUpdate("DELETE from sdidatarelation WHERE paramlistid=?", new String[]{paramlistid});
        this.qp.execPreparedUpdate("DELETE from sdidataitem WHERE paramlistid=?", new String[]{paramlistid});
        this.qp.execPreparedUpdate("DELETE from sdidata WHERE paramlistid=?", new String[]{paramlistid});
        this.qp.execPreparedUpdate("DELETE from sdidataapproval WHERE paramlistid=?", new String[]{paramlistid});
        this.qp.execPreparedUpdate("DELETE from workiteminstrument WHERE workitemitemid in ( select workitemitemid from workitemitem WHERE keyid1=? and keyid3=? )", new String[]{paramlistid, variantid});
        this.qp.execPreparedUpdate("DELETE from workitemitem WHERE keyid1=? and keyid3=?", new String[]{paramlistid, variantid});
        this.qp.execPreparedUpdate("DELETE from paramlistitem WHERE paramlistid=? and variantid=?", new String[]{paramlistid, variantid});
        this.qp.execPreparedUpdate("DELETE from paramlist WHERE paramlistid=? and variantid=?", new String[]{paramlistid, variantid});
    }

    public void cleanParameter(String paramid) throws IOException, ActionException {
        this.logMessage("Cleaning parameter " + paramid);
        this.qp.execPreparedUpdate("DELETE from sdidataitem WHERE paramid=?", new String[]{paramid});
        this.qp.execPreparedUpdate("DELETE from paramlistitem WHERE paramid=?", new String[]{paramid});
        this.qp.execPreparedUpdate("DELETE from param WHERE paramid=?", new String[]{paramid});
    }

    public void createParamList(String paramlistid, String variantid, String[] params) throws IOException, ActionException {
        this.cleanParamList(paramlistid, variantid);
        this.logMessage("Creating parameter list " + paramlistid + " - " + variantid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "ParamList");
        props.setProperty("keyid1", paramlistid);
        props.setProperty("keyid2", "1");
        props.setProperty("keyid3", variantid);
        props.setProperty("copies", "1");
        props.setProperty("modifiableflag", "Y");
        props.setProperty("s_paramlisttype", "Procedural");
        this.ap.processAction("AddSDI", "1", props);
        for (int i = 0; i < params.length; ++i) {
            String paramid = params[i];
            this.cleanParameter(paramid);
            this.logMessage("Creating parameter " + paramid);
            props = new PropertyList();
            props.setProperty("sdcid", "Param");
            props.setProperty("keyid1", paramid);
            this.ap.processAction("AddSDI", "1", props);
            this.qp.execPreparedUpdate("INSERT INTO paramlistitem ( paramlistid, paramlistversionid, variantid, paramid, paramtype, datatypes, numreplicates ) values (?,?,?,?,?,?,?)", new Object[]{paramlistid, "1", variantid, paramid, "Standard", "N", 1});
            DataSet ds = this.qp.getSqlDataSet("select * from paramlistitem where paramlistid='" + paramlistid + "'");
            String string = "";
        }
    }

    public void setParamListInstrumentModelType(String paramlistid, String variantid, String instrumenttype, String model) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "ParamList");
        props.setProperty("keyid1", paramlistid);
        props.setProperty("keyid2", "1");
        props.setProperty("keyid3", variantid);
        props.setProperty("s_instrumenttype", instrumenttype);
        props.setProperty("s_instrumentmodel", model);
        this.ap.processAction("EditSDI", "1", props);
    }

    public void addWorkItemResourceInstrument(String workitemid, String instrumenttypeid, String instrumentmodelid, String label, String durationRule, String linktoContext) throws SapphireException, IOException {
        this.addWorkItemResourceInstrument(workitemid, instrumenttypeid, instrumentmodelid, label, durationRule, linktoContext, linktoContext);
    }

    public void addWorkItemResourceInstrument(String workitemid, String instrumenttypeid, String instrumentmodelid, String label, String durationRule, String activtylinktoContext, String equipmentlinktocontext) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires a " + instrumenttypeid + " for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, instrumenttypeid, instrumentmodelid, durationrule, resourcelabel, linktocontext ) values (?,?,?,?,?,?,?,?,?,?,?)", new Object[]{"WorkItem", workitemid, "1", "(null)", this.resourceNum++, "I", instrumenttypeid, instrumentmodelid, durationRule, label, activtylinktoContext});
        this.qp.execPreparedUpdate("INSERT into workiteminstrument ( workitemid, workitemversionid, workiteminstrumentitemid, instrumenttypeid, instrumentmodelid, instrumentcount, workitemitemid ) values (?,?,?,?,?,?,?)", new Object[]{workitemid, "1", "" + this.workiteminstrumentid++, instrumenttypeid, instrumentmodelid, 1, equipmentlinktocontext});
    }

    protected void addProductResourceUser(String productid, String analysttype, String durationRule) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires an analyst for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, analysttype, durationrule ) values (?,?,?,?,?,?,?,?)", new Object[]{"Product", productid, "1", "(null)", "" + this.resourceNum++, "A", analysttype, durationRule});
    }

    public void addProductResourceInstrument(String productid, String instrumenttypeid, String instrumentmodelid, String label, String durationRule) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires a " + instrumenttypeid + " for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, instrumenttypeid, instrumentmodelid, durationrule, resourcelabel, linktocontext ) values (?,?,?,?,?,?,?,?,?,?,?)", new Object[]{"Product", productid, "1", "(null)", this.resourceNum++, "I", instrumenttypeid, instrumentmodelid, durationRule, label, ""});
    }

    public void addSamplePointResourceUser(String samplepointid, String label, String analysttype, String durationRule, String linktoContext) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires an analyst for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, analysttype, durationrule, resourcelabel, linktocontext ) values (?,?,?,?,?,?,?,?,?,?)", new Object[]{"SamplePoint", samplepointid, "(null)", "(null)", "" + this.resourceNum++, "A", analysttype, durationRule, label, linktoContext});
    }

    public void addLocationResourceUser(String locationid, String label, String analysttype, String durationRule, String linktoContext) throws SapphireException, IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Requires an analyst for " + durationRule);
        this.qp.execPreparedUpdate("INSERT into sdiresourcerequirement ( sdcid, keyid1, keyid2, keyid3, resourcenum, resourcetypeflag, analysttype, durationrule, resourcelabel, linktocontext ) values (?,?,?,?,?,?,?,?,?,?)", new Object[]{"Location", locationid, "(null)", "(null)", "" + this.resourceNum++, "A", analysttype, durationRule, label, linktoContext});
    }

    public void cleanInstrument(String instrumentid) throws ActionException, IOException {
        this.logMessage("Cleaning instrument " + instrumentid);
        this.qp.execPreparedUpdate("UPDATE s_sample SET instrumentid = null WHERE instrumentid=? ", new String[]{instrumentid});
        DataSet cal = this.qp.getPreparedSqlDataSet("SELECT calendarid FROM instrument WHERE instrumentid = ?", (Object[])new String[]{instrumentid});
        this.qp.execPreparedUpdate("DELETE FROM instrument WHERE instrumentid=? ", new String[]{instrumentid});
        if (cal != null && cal.size() > 0 && cal.getValue(0, "calendarid").length() > 0) {
            this.qp.execPreparedUpdate("DELETE FROM calendaritem WHERE calendaritem.calendarid = ?", new String[]{cal.getValue(0, "calendarid")});
            this.qp.execPreparedUpdate("DELETE FROM calendar WHERE calendar.calendarid = ?", new String[]{cal.getValue(0, "calendarid")});
        }
    }

    public void createInstrument(String instrumentid, String instrumenttype, String instrumentmodelid, String testinglabid, String workareaid) throws ActionException, IOException {
        this.cleanInstrument(instrumentid);
        this.logMessage("Creating a " + instrumenttype + " called " + instrumentid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Instrument");
        props.setProperty("keyid1", instrumentid);
        props.setProperty("instrumenttype", instrumenttype);
        props.setProperty("instrumentmodelid", instrumentmodelid);
        props.setProperty("certificationreqflag", "P");
        props.setProperty("testingdepartmentid", testinglabid);
        props.setProperty("workareadepartmentid", workareaid);
        this.ap.processAction("AddOrEditSDI", "1", props);
    }

    public void cleanInstrumentType(String instrumenttype) throws ActionException, IOException {
        this.logMessage("Cleaning an instrument-type=" + instrumenttype);
        this.qp.execPreparedUpdate("UPDATE s_sample SET instrumentid = null WHERE instrumentid in ( select instrumentid from instrument WHERE instrumenttype=? ) ", new String[]{instrumenttype});
        DataSet cal = this.qp.getPreparedSqlDataSet("SELECT calendarid FROM instrument WHERE instrumenttype = ?", (Object[])new String[]{instrumenttype});
        this.qp.execPreparedUpdate("DELETE from instrument WHERE instrumenttype=?", new String[]{instrumenttype});
        if (cal != null) {
            for (int i = 0; i < cal.getRowCount(); ++i) {
                if (cal.getValue(i, "calendarid").length() <= 0) continue;
                this.qp.execPreparedUpdate("DELETE FROM calendaritem WHERE calendaritem.calendarid = ?", new String[]{cal.getValue(i, "calendarid")});
                this.qp.execPreparedUpdate("DELETE FROM calendar WHERE calendar.calendarid = ?", new String[]{cal.getValue(i, "calendarid")});
            }
        }
    }

    public void createInstrumentType(String instrumenttype) throws ActionException, IOException {
        this.logMessage("Creating an instrument-type=" + instrumenttype);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_InstrumentType");
        props.setProperty("keyid1", instrumenttype);
        this.ap.processAction("AddOrEditSDI", "1", props);
    }
}

