/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.actions.AddSDIAttachmentOperation;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.attachmenthandler.HandlerType;
import sapphire.attachmenthandler.SampleCreationHandler;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDMSDataBuilder
extends BaseCustom
implements SDMSConstants {
    private static final String CATEGORY = "SDMSDemoData";
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("com.labvantage.sapphire.modules.sdms.collector.SDMSDataBuilderBundle");
    public static final String rSETUP_PDFHANDLER = "SETUP_PDFHANDLER";
    public static final String rSETUP_AUTOTASTERRESULTS = "SETUP_AUTOTASTERRESULTS";
    public static final String rSETUP_VISCOSITYRESULTSIMPORT = "SETUP_VISCOSITYRESULTSIMPORT";
    public static final String rSETUPOVV_PARSEPDFTABLE = "SETUPOVV_PARSEPDFTABLE";
    public static final String rTALEND_IMAGE = "TALEND_IMAGE";
    public static final String rTALEND_JOB = "TALEND_JOB";
    public static final String rXSLX_IMPORT = "XSLX_IMPORT";
    public static final String rJSON_IMPORT = "JSON_IMPORT";
    public static final String rVISCOSITY_IMAGE_JPG = "VISCOSITY_IMAGE_JPG";
    public static final String rVISCOSITY_JOB_JAR = "VISCOSITY_JOB_JAR";
    public static final String rVISCOSITY_FILE_XLS = "VISCOSITY_FILE_XLS";
    public static final String rTASTER_FILE_PDF = "TASTER_FILE_PDF";
    public static final String rTASTER_JOB_JAR = "TASTER_JOB_JAR";
    public static final String rTASTER_ADDITIONAL_JAR = "TASTER_ADDITIONAL_JAR";
    public static final String rTASTER_FILE_CSV = "TASTER_FILE_CSV";
    public static final String rTASTER_IMAGE_JPG = "TASTER_IMAGE_JPG";
    public static final String rVISCOSITY_ICON_SVG = "VISCOSITY_ICON_SVG";
    public static final String rINCUBATOR_ICON_SVG = "INCUBATOR_ICON_SVG";
    public static final String rTASTER_ICON_SVG = "TASTER_ICON_SVG";
    public static final String rUSER_PROFILEPIC_ALICE = "USER_PROFILEPIC_ALICE";
    public static final String rUSER_PROFILEPIC_BOB = "USER_PROFILEPIC_BOB";
    public static final String rUSER_PROFILEPIC_CHARLIE = "USER_PROFILEPIC_CHARLIE";
    public static final String rUSER_PROFILEPIC_DEBS = "USER_PROFILEPIC_DEBS";
    public static final String rUSER_PROFILEPIC_GARY = "USER_PROFILEPIC_GARY";
    public static final String rUSER_PROFILEPIC_MARK = "USER_PROFILEPIC_MARK";
    private DateTimeUtil dtu;
    Writer out;
    QueryProcessor qp;
    ActionProcessor ap;
    private int workitemitemid = 0;
    public static String AdvancedFileCollector = "AdvancedFileCollectorType";
    public static String SimpleFileCollector = "SimpleFileCollectorType";
    public static String NetworkCollector = "NetworkCollectorType";
    public static String EmailCollector = "EmailCollectorType";

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public SDMSDataBuilder(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.dtu = new DateTimeUtil(cp.getConnectionInfo(connectionid));
        this.qp = new QueryProcessor(connectionid);
        this.ap = new ActionProcessor(connectionid);
    }

    public SDMSDataBuilder(String connectionid, File rakFile) {
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

    public void createInstrumentType(String instrumenttype, String desc) throws ActionException, IOException {
        this.logMessage("Creating an instrument-type=" + instrumenttype);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_InstrumentType");
        props.setProperty("keyid1", instrumenttype);
        props.setProperty("instrumenttypedesc", desc.length() > 0 ? desc : instrumenttype);
        this.ap.processAction("AddOrEditSDI", "1", props);
        this.addCategory(CATEGORY, "LV_InstrumentType", instrumenttype);
    }

    public void createInstrumentModel(String typeid, String modelid, String modeldesc, PropertyList collectorProps, PropertyList emulatorProps, String collectorType, String nodeid) throws ActionException, IOException {
        this.logMessage("Creating an instrument-model=" + modelid);
        PropertyList tree = new PropertyList();
        if (collectorProps != null) {
            tree.setProperty("enablecollection", "Y");
            tree.setProperty("collectorprops", collectorProps);
        }
        if (emulatorProps != null && emulatorProps.size() > 0) {
            tree.setProperty("enableemulator", "Y");
            tree.setProperty("emulatorprops", emulatorProps);
        } else {
            tree.setProperty("enableemulator", "N");
        }
        tree.setProperty("enablerunfiledelivery", "N");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_InstrumentModel");
        props.setProperty("keyid1", modelid);
        props.setProperty("instrumentmodeldesc", modeldesc.length() > 0 ? modeldesc : modelid);
        props.setProperty("instrumenttypeid", typeid);
        props.setProperty("collectorpropertytreeid", collectorType);
        props.setProperty("collectorextendnodeid", nodeid);
        props.setProperty("collectorvaluetree", tree.toXMLString());
        this.ap.processAction("AddOrEditSDI", "1", props);
        this.addCategory(CATEGORY, "LV_InstrumentModel", modelid);
    }

    public void createAttachmentHandler(String attachmenthandlerid, String handleddesc, String objectname, HandlerType handlerType) throws ActionException, IOException {
        this.createAttachmentHandler(attachmenthandlerid, handleddesc, objectname, handlerType, null);
    }

    public void createAttachmentHandler(String attachmenthandlerid, String handleddesc, String objectname, HandlerType handlerType, PropertyList setup) throws ActionException, IOException {
        this.logMessage("Creating an attachment-handler=" + attachmenthandlerid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_AttachmentHandler");
        props.setProperty("keyid1", attachmenthandlerid);
        props.setProperty("attachmenthandlerdesc", handleddesc.length() > 0 ? handleddesc : attachmenthandlerid);
        props.setProperty("handlerclass", objectname);
        props.setProperty("typeflag", handlerType.getTypeFlag());
        if (setup != null) {
            props.setProperty("propertyclob", setup.toJSONString());
        }
        this.ap.processAction("AddOrEditSDI", "1", props);
        this.addCategory(CATEGORY, "LV_AttachmentHandler", attachmenthandlerid);
    }

    public void createInstrumentAttachmentOperation(String instrument, String attachmenthandlerid, String attachmentclass, int usersequece, PropertyList overrides) throws ActionException, IOException {
        this.createAttachmentOperation("Instrument", instrument, "", attachmenthandlerid, attachmentclass, usersequece, overrides);
    }

    public void createInstrumentModelAttachmentOperation(String instrumentModel, String instrumentType, String attachmenthandlerid, String attachmentclass, int usersequece, PropertyList overrides) throws ActionException, IOException {
        this.createAttachmentOperation("LV_InstrumentModel", instrumentModel, instrumentType, attachmenthandlerid, attachmentclass, usersequece, overrides);
    }

    public void createInstrumentModelAttachmentOperation(String instrumentModel, String instrumentType, String attachmenthandlerid, String attachmentclass, int usersequece) throws ActionException, IOException {
        this.createAttachmentOperation("LV_InstrumentModel", instrumentModel, instrumentType, attachmenthandlerid, attachmentclass, usersequece, null);
    }

    public void createInstrumentModelAttachmentOperation(String instrumentModel, String instrumentType, String attachmenthandlerid) throws ActionException, IOException {
        this.createAttachmentOperation("LV_InstrumentModel", instrumentModel, instrumentType, attachmenthandlerid, "", 1, null);
    }

    private void createAttachmentOperation(String sdcid, String instrumentOrModel, String instrumenttype, String attachmenthandlerid, String attachmentclass, int usersequece, PropertyList overrides) throws ActionException, IOException {
        this.logMessage("Creating an attahment-operation for =" + sdcid + " - " + instrumentOrModel);
        DBUtil dbUtil = new DBUtil();
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        SapphireConnection sapphireConnection = new SapphireConnection(this.getConnectionProcessor().getSapphireConnection().getConnection(), connectionInfo);
        dbUtil.setConnection(sapphireConnection);
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("sdcid", sdcid);
        propertyList.setProperty("keyid1", instrumentOrModel);
        if (instrumenttype != null && instrumenttype.length() > 0) {
            propertyList.setProperty("keyid2", instrumenttype);
        }
        propertyList.setProperty("attachmenthandlerid", attachmenthandlerid);
        propertyList.setProperty("attachmentclass", attachmentclass);
        propertyList.setProperty("synchronousflag", "Y");
        propertyList.setProperty("usersequence", "" + usersequece);
        if (overrides != null) {
            propertyList.setProperty("propertyclob", overrides.toJSONString(true, true));
        }
        this.getActionProcessor().processActionClass(AddSDIAttachmentOperation.class.getName(), propertyList);
    }

    public void createInstrument(String instrumentid, String instrumenttype, String instrumentmodelid, String collectorid, String instrumentroot, PropertyList collectorProps, PropertyList emulatorProps, String collectorType, String nodeid) throws ActionException, IOException {
        this.createInstrument(instrumentid, instrumenttype, instrumentmodelid, "", collectorid, instrumentroot, null, collectorProps, emulatorProps, collectorType, nodeid);
    }

    public void createInstrument(String instrumentid, String instrumenttype, String instrumentmodelid, String testingLabId, String collectorid, String instrumentroot, PropertyList collectorProps, PropertyList emulatorProps, String collectorType, String nodeid) throws ActionException, IOException {
        this.createInstrument(instrumentid, instrumenttype, instrumentmodelid, testingLabId, collectorid, instrumentroot, null, collectorProps, emulatorProps, collectorType, nodeid);
    }

    public void createInstrument(String instrumentid, String instrumenttype, String instrumentmodelid, String testinglabid, String collectorid, String instrumentroot, PropertyList deliveryprops, PropertyList collectorProps, PropertyList emulatorProps, String collectorType, String nodeid) throws ActionException, IOException {
        this.cleanInstrument(instrumentid);
        instrumentroot = StringUtil.replaceAll(instrumentroot, "[instrumentid]", instrumentid);
        Path path = Paths.get(instrumentroot, new String[0]);
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        PropertyList tree = new PropertyList();
        File f = new File(instrumentroot);
        if (!f.exists()) {
            f.mkdir();
        }
        if (deliveryprops != null) {
            tree.setProperty("deliveryprops", deliveryprops);
        }
        if (collectorProps != null && collectorProps.size() > 0) {
            collectorProps.setProperty("instrumentremoteroot", instrumentroot);
            tree.setProperty("collectorprops", collectorProps);
            tree.setProperty("enablecollection", "Y");
        }
        if (emulatorProps != null && emulatorProps.size() > 0) {
            tree.setProperty("emulatorprops", emulatorProps);
            tree.setProperty("enableemulator", "Y");
        }
        tree.setProperty("instrumentremoteroot", instrumentroot);
        this.logMessage("Creating a " + instrumenttype + " called " + instrumentid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Instrument");
        props.setProperty("keyid1", instrumentid);
        props.setProperty("instrumenttype", instrumenttype);
        props.setProperty("instrumentmodelid", instrumentmodelid);
        props.setProperty("certificationreqflag", "P");
        if (collectorid != null && collectorid.length() > 0) {
            props.setProperty("sdmscollectorid", collectorid);
        }
        props.setProperty("collectorpropertytreeid", collectorType);
        props.setProperty("collectorextendnodeid", nodeid);
        props.setProperty("collectorvaluetree", StringUtil.replaceAll(tree.toXMLString(), ";", "#semicolon#"));
        if (testinglabid != null && testinglabid.length() > 0) {
            props.setProperty("testingdepartmentid", testinglabid);
        }
        props.setProperty("copies", "1");
        this.ap.processAction("AddOrEditSDI", "1", props);
        this.addCategory(CATEGORY, "Instrument", instrumentid);
    }

    public PropertyList buildAdvancedCollectorProps(PropertyList trigger, PropertyList wait, PropertyListCollection collect) {
        PropertyList collectorProps = new PropertyList();
        collectorProps.setProperty("trigger", trigger);
        collectorProps.setProperty("wait", wait);
        collectorProps.setProperty("collect", collect);
        return collectorProps;
    }

    public void logMessage(String s) throws IOException {
        Trace.logInfo("SDMS  DEMO BUILDER: " + s);
        if (this.out != null) {
            this.out.write(s + "<br>");
        }
    }

    public void createCollector(String id, String internalflag, String storageModeFlag, String storagePathLocal, String storagePathRemote) throws ActionException, IOException {
        Path path = Paths.get(storagePathLocal, new String[0]);
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        if (!Files.exists(path = Paths.get(storagePathRemote, new String[0]), new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        this.logMessage("Creating a collector " + id);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_SDMSCollector");
        props.setProperty("keyid1", id);
        props.setProperty("sdmscollectordesc", "SDMS");
        props.setProperty("internalflag", internalflag);
        props.setProperty("storagemodeflag", storageModeFlag);
        props.setProperty("storagepathlocal", storagePathLocal);
        props.setProperty("storagepathremote", storagePathRemote);
        props.setProperty("allowisolatedflag", "Y");
        props.setProperty("pausedflag", "Y");
        this.ap.processAction("AddSDI", "1", props);
        this.addCategory(CATEGORY, "LV_SDMSCollector", id);
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

    public void createTestingLab(String testinglab) throws SapphireException, IOException {
        this.createDepartment(testinglab, "", false);
    }

    private void createDepartment(String departmentid, String parentdepartmentid, boolean workarea) throws SapphireException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT departmentid FROM department WHERE departmentid=?", (Object[])new String[]{departmentid}).getRowCount() > 0) {
            this.logMessage("Department " + departmentid + " already exists, so update");
        } else {
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
            this.addCategory(CATEGORY, "Department", departmentid);
        }
    }

    public void createUser(String sysuserid, String username, String testinglabid) throws ActionException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT sysuserid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sysuserid}).getRowCount() > 0) {
            this.logMessage("User " + sysuserid + " already exists, so update");
        } else {
            this.logMessage("Creating User " + sysuserid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("keyid1", sysuserid);
            props.setProperty("templateid", "admin");
            props.setProperty("sysuserdesc", username);
            props.setProperty("password", StringUtils.capitalize((String)sysuserid) + "_1234");
            props.setProperty("nameduserflag", "C");
            props.setProperty("calendarid", "(null)");
            props.setProperty("glpflag", "Y");
            this.ap.processAction("AddSDI", "1", props);
            this.addCategory(CATEGORY, "User", sysuserid);
        }
        if (testinglabid.length() > 0) {
            this.qp.execPreparedUpdate("INSERT INTO departmentsysuser ( departmentid, sysuserid, defaulttestinglabflag ) VALUES ( ?, ?, ? )", new String[]{testinglabid, sysuserid, "Y"});
        }
    }

    public void createWorkitem(String workitemid, String testeingdepartmentid) throws ActionException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT workitemid, workitemversionid FROM workitem WHERE workitemid=? AND workitemversionid=?", (Object[])new String[]{workitemid, "1"}).getRowCount() > 0) {
            this.logMessage("Work item " + workitemid + " already exists so update.");
        } else {
            this.logMessage("Creating workitem " + workitemid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "WorkItem");
            props.setProperty("keyid1", workitemid);
            props.setProperty("keyid2", "1");
            props.setProperty("testingdepartmentid", testeingdepartmentid);
            props.setProperty("copies", "1");
            props.setProperty("workitemtypeflag", "W");
            this.ap.processAction("AddOrEditSDI", "1", props);
            this.addCategory(CATEGORY, "WorkItem", workitemid);
        }
    }

    public void createParamList(String paramlistid, String variantid, String[] params) throws IOException, ActionException {
        this.createParamList(paramlistid, variantid, params, null);
    }

    public void createParamList(String paramlistid, String variantid, String[] params, String[] datatype) throws IOException, ActionException {
        if (this.qp.getPreparedSqlDataSet("SELECT paramlistid, paramlistversionid, variantid FROM paramlist WHERE paramlistid=? AND paramlistversionid=? AND variantid=?", (Object[])new String[]{paramlistid, "1", variantid}).getRowCount() > 0) {
            this.logMessage("Param list " + paramlistid + " already exists so update.");
        } else {
            int i;
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
            if (datatype == null) {
                datatype = new String[params.length];
                for (i = 0; i < params.length; ++i) {
                    datatype[i] = "N";
                }
            }
            for (i = 0; i < params.length; ++i) {
                String paramid = params[i];
                this.logMessage("Creating parameter " + paramid);
                props = new PropertyList();
                props.setProperty("sdcid", "Param");
                props.setProperty("keyid1", paramid);
                this.ap.processAction("AddSDI", "1", props);
                this.addCategory(CATEGORY, "Param", paramid);
                this.qp.execPreparedUpdate("INSERT INTO paramlistitem ( paramlistid, paramlistversionid, variantid, paramid, paramtype, datatypes, numreplicates ) values (?,?,?,?,?,?,?)", new Object[]{paramlistid, "1", variantid, paramid, "Standard", datatype[i], 1});
                DataSet ds = this.qp.getSqlDataSet("select * from paramlistitem where paramlistid='" + paramlistid + "'");
                String string = "";
            }
            this.addCategory(CATEGORY, "ParamList", paramlistid);
        }
    }

    public String addWorkitemParamlist(String workitemid, String paramlistid, String variantid) throws IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Adding " + paramlistid + " (" + variantid + ") to " + workitemid);
        String id = "" + this.workitemitemid++;
        this.qp.execPreparedUpdate("insert into workitemitem ( workitemid, workitemversionid, workitemitemid, sdcid, keyid1, keyid2, keyid3, forcenewflag ) values ( ?,?,?,?,?,?,?,?)", new Object[]{workitemid, "1", id, "ParamList", paramlistid, "1", variantid, "Y"});
        return id;
    }

    public void cleanUp(boolean datacapturesOnly) throws SapphireException, IOException {
        DataSet sdmsColl = this.qp.getPreparedSqlDataSet("SELECT sdmscollectorid FROM sdmscollector WHERE sdmscollectorid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", (Object[])new String[]{CATEGORY, "LV_SDMSCollector"});
        for (int i = 0; i < sdmsColl.size(); ++i) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_SDMSCollector");
            props.setProperty("keyid1", sdmsColl.getValue(i, "sdmscollectorid"));
            props.setProperty("pausedflag", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", props);
            SDMSUtil.sendCollectorCommand(this.qp, this.ap, sdmsColl.getValue(i, "sdmscollectorid"), "", "COLLECTORCOMMAND_GETCOLLECTORSTATE", "");
        }
        this.logMessage("Paused collectors.<br>");
        this.logMessage("Waiting for processing to complete...<br>");
        this.out.flush();
        try {
            Thread.sleep(30000L);
        }
        catch (Exception i) {
            // empty catch block
        }
        this.logMessage("...Continuing to clean up.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("UPDATE instrument SET sdmscollectorid=null WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "Instrument"});
            this.logMessage("Updated instruments to remove collectors.<br>");
        }
        this.qp.execPreparedUpdate("DELETE from todolist", new String[0]);
        this.logMessage("Cleared todo list");
        this.logMessage("Waiting for processing to complete...<br>");
        this.out.flush();
        try {
            Thread.sleep(30000L);
        }
        catch (Exception i) {
            // empty catch block
        }
        this.logMessage("...Continuing to clean up.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemitem WHERE itemkeyid1 IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidatarelation WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidataitem WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidata WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidataapproval WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.logMessage("Deleted data items.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemrelation WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM workitemitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
            this.qp.execPreparedUpdate("DELETE FROM workiteminstrument WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
            this.qp.execPreparedUpdate("DELETE FROM sdiresourcerequirement WHERE keyid1 IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        }
        this.logMessage("Deleted workitems.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM paramlistitem WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
            this.logMessage("Deleted param lists.<br>");
            this.qp.execPreparedUpdate("DELETE FROM param WHERE paramid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Param"});
            this.qp.execPreparedUpdate("DELETE FROM paramlist WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
            this.qp.execPreparedUpdate("DELETE FROM workitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
            this.logMessage("Deleted parameters.<br>");
        }
        this.qp.execPreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = 'LV_DataCapture' AND keyid1 IN (SELECT datacaptureid FROM datacapture WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?))", new String[]{CATEGORY, "Instrument"});
        this.logMessage("Deleted attachments for data captures.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = 'LV_AttachmentHandler' AND keyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_AttachmentHandler"});
            this.logMessage("Deleted attachments for attachment handlers.<br>");
        }
        this.qp.execPreparedUpdate("delete from s_sample where s_sampleid in (select keyid1 from sdidatacapture where datacaptureid in (select datacaptureid from datacapture where instrumentid in (select keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)))", new String[]{CATEGORY, "Instrument"});
        this.logMessage("Deleted samples.<br>");
        this.qp.execPreparedUpdate("delete from sdidatacapture where datacaptureid in (select datacaptureid from datacapture where instrumentid in (select keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?))", new String[]{CATEGORY, "Instrument"});
        this.logMessage("Deleted sdi data captures.<br>");
        DataSet incidents = this.qp.getPreparedSqlDataSet("SELECT incidentid FROM incidentitem WHERE sourcesdcid=? AND sourcekeyid1 IN (SELECT datacaptureid FROM datacapture WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?))", (Object[])new String[]{"LV_DataCapture", CATEGORY, "Instrument"});
        this.qp.execPreparedUpdate("DELETE from incidentitem WHERE sourcesdcid=? AND sourcekeyid1 in (SELECT datacaptureid FROM datacapture WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?))", new String[]{"LV_DataCapture", CATEGORY, "Instrument"});
        this.qp.execPreparedUpdate("DELETE incident WHERE incidentid IN ('" + incidents.getColumnValues("incidentid", "','") + "')", new String[0]);
        this.logMessage("Deleted data capture alerts.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sdiattachmentoperationexec WHERE attachmentoperationid IN (SELECT attachmentoperationid FROM sdiattachmentoperation WHERE operationkeyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?))", new String[]{CATEGORY, "LV_AttachmentHandler"});
        this.qp.execPreparedUpdate("delete from sdiattachmentoperationexec where attachmentoperationid in (select attachmentoperationid from sdiattachmentoperation where sdcid='LV_DataCapture' and keyid1 in (select datacaptureid from datacapture where instrumentid in (select keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)))", new String[]{CATEGORY, "Instrument"});
        this.logMessage("Deleted operation exec.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM sdiattachmentoperation WHERE operationkeyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_AttachmentHandler"});
            this.qp.execPreparedUpdate("delete from SDIATTACHMENTOPERATION where attachmentoperationid in (select attachmentoperationid from sdiattachmentoperation where sdcid='LV_DataCapture' and keyid1 in (select datacaptureid from datacapture where instrumentid in (select keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)))", new String[]{CATEGORY, "Instrument"});
            this.qp.execPreparedUpdate("DELETE FROM sdiattachmentoperation WHERE keyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_InstrumentModel"});
            this.qp.execPreparedUpdate("DELETE FROM sdiattachmentoperation WHERE keyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "Instrument"});
            this.logMessage("Deleted attachment operations.<br>");
        }
        this.qp.execPreparedUpdate("DELETE FROM datacapture WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "Instrument"});
        this.logMessage("Deleted data captures.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM attachmenthandler WHERE attachmenthandlerid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_AttachmentHandler"});
            this.logMessage("Deleted attachment handlers.<br>");
        }
        incidents = this.qp.getPreparedSqlDataSet("SELECT incidentid FROM incidentitem WHERE sourcesdcid=? AND sourcekeyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", (Object[])new String[]{"Instrument", CATEGORY, "Instrument"});
        this.qp.execPreparedUpdate("DELETE from incidentitem WHERE sourcesdcid=? AND sourcekeyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{"Instrument", CATEGORY, "Instrument"});
        this.qp.execPreparedUpdate("DELETE incident WHERE incidentid IN ('" + incidents.getColumnValues("incidentid", "','") + "')", new String[0]);
        this.logMessage("Deleted instrument alerts.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = 'LV_InstrumentModel' AND keyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_InstrumentModel"});
            this.logMessage("Deleted instrument icons.<br>");
            this.qp.execPreparedUpdate("DELETE FROM instrument WHERE instrumentid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "Instrument"});
            this.logMessage("Deleted instruments.<br>");
            this.qp.execPreparedUpdate("DELETE FROM instrumentmodel WHERE instrumentmodelid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_InstrumentModel"});
            this.logMessage("Deleted instrument models.<br>");
            this.qp.execPreparedUpdate("DELETE FROM instrumenttype WHERE instrumenttypeid IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "LV_InstrumentType"});
            this.logMessage("Deleted instrument types.<br>");
        }
        this.qp.execPreparedUpdate("DELETE FROM sdmscollectorcommand", new String[0]);
        this.logMessage("Deleted collectors commands.<br>");
        incidents = this.qp.getPreparedSqlDataSet("SELECT incidentid FROM incidentitem WHERE sourcesdcid=? AND sourcekeyid1 IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", (Object[])new String[]{"LV_SDMSCollector", CATEGORY, "LV_SDMSCollector"});
        this.qp.execPreparedUpdate("DELETE from incidentitem WHERE sourcesdcid=? AND sourcekeyid1 IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{"LV_SDMSCollector", CATEGORY, "LV_SDMSCollector"});
        this.qp.execPreparedUpdate("DELETE incident WHERE incidentid IN ('" + incidents.getColumnValues("incidentid", "','") + "')", new String[0]);
        this.logMessage("Deleted collector alerts.<br>");
        if (!datacapturesOnly) {
            this.qp.execPreparedUpdate("DELETE FROM sdmscollector WHERE sdmscollectorid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "LV_SDMSCollector"});
            this.logMessage("Deleted collectors.<br>");
            this.qp.execPreparedUpdate("DELETE FROM departmentsysuser WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
            this.qp.execPreparedUpdate("DELETE FROM departmentshift WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
            this.qp.execPreparedUpdate("DELETE FROM departmentassignment WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
            this.qp.execPreparedUpdate("UPDATE sysuser SET basedepartment = null WHERE basedepartment IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
            this.qp.execPreparedUpdate("DELETE FROM department WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
            this.logMessage("Deleted departments.<br>");
        }
    }

    private Attachment addProfilePicture(String userid, String base64) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattachmentid FROM sdiattachment WHERE sdcid='User' AND keyid1=? AND attachmentclass='ProfilePicture'", (Object[])new String[]{userid});
        Attachment attachment = null;
        if (ds.size() == 0) {
            attachment = this.addAttachment("User", userid, "Profile Picture", "ProfilePicture", "profilepic.jpg", base64);
        }
        return attachment;
    }

    public Attachment addAttachment(String sdcid, String keyid1, String description, String className, String filename, String base64) {
        return this.addAttachment(sdcid, keyid1, null, description, className, filename, base64);
    }

    public Attachment addAttachment(String sdcid, String keyid1, String keyid2, String description, String className, String filename, String base64) {
        Attachment attachment = Attachment.getAttachment(sdcid, keyid1, keyid2, null);
        attachment.setDescription(description);
        attachment.setAttachmentClass(className);
        File rakFile = this.getRakFile();
        FileManager.FileData fileData = rakFile != null ? new FileManager.FileData(base64, FileType.getFileType(filename, rakFile, this.getConnectionId()).getMime()) : new FileManager.FileData(base64, FileType.getFileType(filename, this.getConnectionId()).getMime());
        attachment.setData(fileData.getData());
        attachment.setFilename(filename);
        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getRakFile(), this.getConnectionid());
        return attachmentProcessor.addSDIAttachment(attachment);
    }

    public void buildScenarioIceCreamLabFull() throws ActionException, IOException, SapphireException {
        this.buildScenarioIceCreamLab(true);
    }

    public void buildScenarioIceCreamLab() throws ActionException, IOException, SapphireException {
        this.buildScenarioIceCreamLab(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void buildScenarioIceCreamLab(boolean full) throws ActionException, IOException, SapphireException {
        String testinglab = "IceCream";
        this.createTestingLab(testinglab);
        this.createUser("alice1", "Alice", testinglab);
        this.addProfilePicture("alice1", resourceBundle.getString(rUSER_PROFILEPIC_ALICE));
        this.createUser("bob", "Bob", testinglab);
        this.addProfilePicture("bob", resourceBundle.getString(rUSER_PROFILEPIC_BOB));
        this.createUser("charlie", "Charlie", testinglab);
        this.addProfilePicture("charlie", resourceBundle.getString(rUSER_PROFILEPIC_CHARLIE));
        this.createUser("debs", "Debs", testinglab);
        this.addProfilePicture("debs", resourceBundle.getString(rUSER_PROFILEPIC_DEBS));
        this.createUser("mark", "Mark", testinglab);
        this.addProfilePicture("mark", resourceBundle.getString(rUSER_PROFILEPIC_MARK));
        this.createUser("gary", "Gary", testinglab);
        this.addProfilePicture("gary", resourceBundle.getString(rUSER_PROFILEPIC_GARY));
        String CollectorUK = "CollectorUK";
        String CollectorUSA = "CollectorUSA";
        Path rootpath = Paths.get("C:/Development/SDMS/Environment", new String[0]);
        if (!Files.exists(rootpath, new LinkOption[0])) {
            Files.createDirectories(rootpath, new FileAttribute[0]);
        }
        this.createCollector(CollectorUK, "Y", "I", rootpath.resolve("UK/sdms").toString(), rootpath.resolve("UK/sdms").toString());
        this.createCollector(CollectorUSA, "Y", "D", rootpath.resolve("US/sdms").toString(), rootpath.resolve("USA/sdms").toString());
        String HANDLER1 = "ViscocityResultsImport";
        PropertyList setup3 = null;
        try {
            setup3 = new PropertyList(new JSONObject(resourceBundle.getString(rSETUP_VISCOSITYRESULTSIMPORT)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.createAttachmentHandler(HANDLER1, "", "labvantage_sdms.viscocitysample_0_1.ViscocitySample", HandlerType.TALENDJOB, setup3);
        this.addAttachment("LV_AttachmentHandler", HANDLER1, "Import File", "HandlerExampleFile", "sludeomter_results.xls", resourceBundle.getString(rVISCOSITY_FILE_XLS));
        this.addAttachment("LV_AttachmentHandler", HANDLER1, "Talend Job", "HandlerLibrary", "viscocitysample_0_1.jar", resourceBundle.getString(rVISCOSITY_JOB_JAR));
        String viscometer_type = "Viscometer";
        String incubator_type = "Incubator";
        this.createInstrumentType(viscometer_type, "");
        this.createInstrumentType(incubator_type, "");
        String Node_SapphireCustom = "Sapphire Custom";
        String viscometer_model = "Sludgeometer-2000";
        String incubator_model = "Heratherm-14";
        PropertyList collectorProps1 = new PropertyList();
        collectorProps1.setProperty("triggerpollintervalseconds", "40");
        collectorProps1.setProperty("globpathmatcher", "*.xls");
        collectorProps1.setProperty("waitseconds", "20");
        collectorProps1.setProperty("actiononoriginal", "Delete");
        PropertyList collectorPropsInc1 = new PropertyList();
        PropertyList triggerPropsInc1 = new PropertyList();
        triggerPropsInc1.setProperty("collectmode", "Discrete");
        triggerPropsInc1.setProperty("discretestart", "TEMP:");
        triggerPropsInc1.setProperty("discreteend", "Silence");
        triggerPropsInc1.setProperty("discreteendsilence", "5");
        collectorPropsInc1.setProperty("trigger", triggerPropsInc1);
        PropertyList collectPropsInc1 = new PropertyList();
        collectPropsInc1.setProperty("trimresult", "Y");
        collectPropsInc1.setProperty("excludestarttrigger", "Y");
        collectPropsInc1.setProperty("excludeendtrigger", "Y");
        collectPropsInc1.setProperty("filename", "temperature.txt");
        collectorPropsInc1.setProperty("collect", collectPropsInc1);
        InetAddress inetAddress = InetAddress.getLocalHost();
        collectorPropsInc1.setProperty("collectoraddress", inetAddress.getHostAddress());
        try (ServerSocket s2 = new ServerSocket(0);){
            collectorPropsInc1.setProperty("collectorport", "" + s2.getLocalPort());
        }
        catch (Exception s2) {
            // empty catch block
        }
        PropertyList emulatorProps1 = new PropertyList();
        emulatorProps1.setProperty("frequency", "10");
        emulatorProps1.setProperty("filename", "sludeomter_results.xls");
        PropertyList emulatorPropsInc = new PropertyList();
        emulatorPropsInc.setProperty("frequency", "60");
        PropertyList streamdataProps = new PropertyList();
        streamdataProps.setProperty("datacontent", "1234567890#semicolon#TEMP:20c#semicolon#123456#semicolon#TEMP:21c#semicolon#876366#semicolon#TEMP:19c");
        emulatorPropsInc.setProperty("streamdata", streamdataProps);
        this.createInstrumentModel(viscometer_type, viscometer_model, "", collectorProps1, emulatorProps1, SimpleFileCollector, Node_SapphireCustom);
        this.addAttachment("LV_InstrumentModel", viscometer_model, viscometer_type, "Instrument Icon", "Icon", "icon.svg", resourceBundle.getString(rVISCOSITY_ICON_SVG));
        this.createInstrumentModel(incubator_type, incubator_model, "", collectorPropsInc1, emulatorPropsInc, NetworkCollector, Node_SapphireCustom);
        this.addAttachment("LV_InstrumentModel", incubator_model, incubator_type, "Instrument Icon", "Icon", "icon.svg", resourceBundle.getString(rINCUBATOR_ICON_SVG));
        this.createInstrumentModelAttachmentOperation(viscometer_model, viscometer_type, HANDLER1);
        String instrumentUS1 = "Visc-US-01";
        String instrumentUS2 = "Visc-US-02";
        String instrumentUS3 = "Visc-US-03";
        String instrumentUS4 = "Visc-US-04";
        String instrumentUS5 = "Inc-US-01";
        String instrumentUK1 = "Visc-UK-01";
        String instrumentUK2 = "Visc-UK-02";
        String instrumentUK3 = "Visc-UK-03";
        String instrumentUK4 = "Inc-UK-01";
        PropertyList collectorProps_INST = new PropertyList();
        PropertyList emulatorProps1_INST = new PropertyList();
        try {
            FileManager.FileData fileData = new FileManager.FileData(resourceBundle.getString(rVISCOSITY_FILE_XLS), FileType.getFileTypeByName("XLS", this.getConnectionId()).getMime());
            emulatorProps1_INST.setProperty("filecontent", fileData.getDataURL());
        }
        catch (Exception fileData) {
            // empty catch block
        }
        String viscosityTest = "Viscosity";
        String viscosityParamList = "ViscosityPL";
        this.createWorkitem(viscosityTest, testinglab);
        this.createParamList(viscosityParamList, "1", new String[]{"Viscosity"});
        this.addWorkitemParamlist(viscosityTest, viscosityParamList, "1");
        this.createInstrument(instrumentUK1, viscometer_type, viscometer_model, testinglab, CollectorUK, rootpath.resolve("UK/network1/path/[instrumentid]").toString(), collectorProps_INST, emulatorProps1_INST, SimpleFileCollector, Node_SapphireCustom);
        PropertyList emuCopy = emulatorProps1_INST.copy();
        emuCopy.setProperty("frequency", "15");
        this.createInstrument(instrumentUK2, viscometer_type, viscometer_model, testinglab, CollectorUK, rootpath.resolve("UK/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        emuCopy = emulatorProps1_INST.copy();
        emuCopy.setProperty("frequency", "20");
        this.createInstrument(instrumentUK3, viscometer_type, viscometer_model, testinglab, CollectorUK, rootpath.resolve("UK/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        PropertyList collectorProps_INCUBATOR1 = new PropertyList();
        try (ServerSocket s3 = new ServerSocket(0);){
            collectorProps_INCUBATOR1.setProperty("collectorport", "" + s3.getLocalPort());
        }
        catch (Exception s3) {
            // empty catch block
        }
        this.createInstrument(instrumentUK4, incubator_type, incubator_model, testinglab, CollectorUK, rootpath.resolve("UK/network1/path/[instrumentid]").toString(), collectorProps_INCUBATOR1, new PropertyList(), NetworkCollector, Node_SapphireCustom);
        emuCopy.setProperty("frequency", "25");
        this.createInstrument(instrumentUS1, viscometer_type, viscometer_model, testinglab, CollectorUSA, rootpath.resolve("USA/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        emuCopy.setProperty("frequency", "30");
        this.createInstrument(instrumentUS2, viscometer_type, viscometer_model, testinglab, CollectorUSA, rootpath.resolve("USA/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        emuCopy.setProperty("frequency", "35");
        this.createInstrument(instrumentUS3, viscometer_type, viscometer_model, testinglab, CollectorUSA, rootpath.resolve("USA/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        emuCopy.setProperty("frequency", "40");
        this.createInstrument(instrumentUS4, viscometer_type, viscometer_model, testinglab, CollectorUSA, rootpath.resolve("USA/network1/path/[instrumentid]").toString(), collectorProps_INST, emuCopy, SimpleFileCollector, Node_SapphireCustom);
        PropertyList collectorProps_INCUBATOR = new PropertyList();
        try (ServerSocket s4 = new ServerSocket(0);){
            collectorProps_INCUBATOR.setProperty("collectorport", "" + s4.getLocalPort());
        }
        catch (Exception s4) {
            // empty catch block
        }
        this.createInstrument(instrumentUS5, incubator_type, incubator_model, testinglab, CollectorUSA, rootpath.resolve("USA/network1/path/[instrumentid]").toString(), collectorProps_INCUBATOR, new PropertyList(), NetworkCollector, Node_SapphireCustom);
        String taster_type = "AutoTaster";
        this.createInstrumentType(taster_type, "");
        String taster_model = "AutoTaster101";
        PropertyList collectorProps2 = new PropertyList();
        PropertyList triggerProps = new PropertyList();
        triggerProps.setProperty("triggertype", "FileCreated");
        triggerProps.setProperty("triggerpollintervalseconds", "10");
        triggerProps.setProperty("globfilenamematcher", "*.pdf");
        collectorProps2.setProperty("trigger", triggerProps);
        PropertyList waitProps = new PropertyList();
        waitProps.setProperty("waittype", "Time");
        waitProps.setProperty("waitseconds", "10");
        collectorProps2.setProperty("wait", waitProps);
        PropertyListCollection collectListProps = new PropertyListCollection();
        PropertyList collectProps = new PropertyList();
        collectProps.setProperty("collecttype", "TriggerFile");
        collectProps.setProperty("attachmentclass", "RawDataFile");
        collectProps.setProperty("actiononoriginal", "Delete");
        collectListProps.add(collectProps);
        collectProps = new PropertyList();
        collectProps.setProperty("collecttype", "FilesMatching");
        collectProps.setProperty("globpathmatcher", "*.log");
        collectProps.setProperty("attachmentclass", "");
        collectProps.setProperty("actiononoriginal", "Delete");
        collectListProps.add(collectProps);
        collectorProps2.setProperty("collect", collectListProps);
        PropertyList emulatorProps2 = new PropertyList();
        this.createInstrumentModel(taster_type, taster_model, "", collectorProps2, emulatorProps2, AdvancedFileCollector, Node_SapphireCustom);
        this.addAttachment("LV_InstrumentModel", taster_model, taster_type, "Instrument Icon", "Icon", "icon.svg", resourceBundle.getString(rTASTER_ICON_SVG));
        String HANDLER2 = "ParseAutoTasterPDF";
        PropertyList setup = null;
        try {
            setup = new PropertyList(new JSONObject(resourceBundle.getString(rSETUP_PDFHANDLER)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        PropertyList setupPDF = null;
        try {
            setupPDF = new PropertyList(new JSONObject(resourceBundle.getString(rSETUPOVV_PARSEPDFTABLE)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.createAttachmentHandler(HANDLER2, "Parse a pdf document and extract first table as CSV", "com.labvantage.sapphire.modules.sdms.handlers.PDFHandler", HandlerType.HANDLERCLASS, setupPDF);
        this.addAttachment("LV_AttachmentHandler", HANDLER2, "AutoTaster File", "HandlerExampleFile", "AutoTaster101.pdf", resourceBundle.getString(rTASTER_FILE_PDF));
        String tasterTest = "Taste";
        String tasterParamList = "TastePL";
        this.createWorkitem(tasterTest, testinglab);
        this.createParamList(tasterParamList, "1", new String[]{"Sourness", "Sweetness", "Taste"}, new String[]{"N", "N", "T"});
        this.addWorkitemParamlist(tasterTest, tasterParamList, "1");
        if (full) {
            String HANDLER3 = "AutoTasterResults";
            PropertyList setup2 = null;
            try {
                setup2 = new PropertyList(new JSONObject(resourceBundle.getString(rSETUP_AUTOTASTERRESULTS)));
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.createAttachmentHandler(HANDLER3, "Auto Taster Results Entry", "labvantage_sdms.autotasterresults_0_1.AutoTasterResults", HandlerType.TALENDJOB, setup2);
            this.addAttachment("LV_AttachmentHandler", HANDLER3, "Results File", "HandlerExampleFile", "AutoTaster101.csv", resourceBundle.getString(rTASTER_FILE_CSV));
            this.addAttachment("LV_AttachmentHandler", HANDLER3, "Talend Job", "HandlerLibrary", "autotasterresults_0_1.jar", resourceBundle.getString(rTASTER_JOB_JAR));
            this.addAttachment("LV_AttachmentHandler", HANDLER3, "Talend CSV Lib", "HandlerLibrary", "talendcsv.jar", resourceBundle.getString(rTASTER_ADDITIONAL_JAR));
            this.createInstrumentModelAttachmentOperation(taster_model, taster_type, HANDLER2, "RawDataFile", 1);
            this.createInstrumentModelAttachmentOperation(taster_model, taster_type, HANDLER3, "ProcessedDataFile", 2);
            String instrumentUK5 = "ATaste-UK-01";
            this.createInstrument(instrumentUK5, taster_type, taster_model, testinglab, CollectorUK, rootpath.resolve("UK/network1/path/[instrumentid]").toString(), new PropertyList(), new PropertyList(), AdvancedFileCollector, Node_SapphireCustom);
        }
    }

    public void buildScenarioSimpleInternal() throws ActionException, IOException {
        String CollectorUK = "CollectorUK";
        String CollectorUSA = "CollectorUSA";
        Path rootpath = Paths.get("C:/Development/SDMS/Environment", new String[0]);
        if (!Files.exists(rootpath, new LinkOption[0])) {
            Files.createDirectories(rootpath, new FileAttribute[0]);
        }
        this.createCollector(CollectorUK, "Y", "I", rootpath.resolve("UK/sdms").toString(), rootpath.resolve("UK/sdms").toString());
        this.createCollector(CollectorUSA, "Y", "D", rootpath.resolve("US/sdms").toString(), rootpath.resolve("USA/sdms").toString());
        String HANDLER1 = "SDMS_JSONImport";
        this.createAttachmentHandler(HANDLER1, "", SampleCreationHandler.class.getName(), HandlerType.HANDLERCLASS);
        this.addAttachment("LV_AttachmentHandler", HANDLER1, "Import File", "HandlerExampleFile", "newsample.txt", resourceBundle.getString(rJSON_IMPORT));
        String HANDLER2 = "SDMS_ExcelImport";
        this.createAttachmentHandler(HANDLER2, "", "local_project.mysampletest_0_1.MySampleTest", HandlerType.TALENDJOB);
        this.addAttachment("LV_AttachmentHandler", HANDLER2, "Import File", "HandlerExampleFile", "samples.xlsx", resourceBundle.getString(rXSLX_IMPORT));
        this.addAttachment("LV_AttachmentHandler", HANDLER2, "Talend Job", "HandlerLibrary", "talendjob.jar", resourceBundle.getString(rTALEND_JOB));
        String GC = "SDMS_GC";
        this.createInstrumentType(GC, "");
        String Node_SapphireCustom = "Sapphire Custom";
        String GC_HONEYWELL = "GC_HONEYWELL";
        PropertyList collectorProps1 = new PropertyList();
        collectorProps1.setProperty("triggerpollintervalseconds", "10");
        collectorProps1.setProperty("globpathmatcher", "*.txt");
        collectorProps1.setProperty("waitseconds", "10");
        collectorProps1.setProperty("actiononoriginal", "Delete");
        PropertyList emulatorProps1 = new PropertyList();
        emulatorProps1.setProperty("frequency", "5");
        emulatorProps1.setProperty("filename", "newsample.txt");
        this.createInstrumentModel(GC, GC_HONEYWELL, "", collectorProps1, emulatorProps1, SimpleFileCollector, Node_SapphireCustom);
        this.createInstrumentModelAttachmentOperation(GC_HONEYWELL, GC, HANDLER1);
        String GC_AGILENT = "GC_AGILENT";
        PropertyList collectorProps2 = new PropertyList();
        collectorProps2.setProperty("triggerpollintervalseconds", "10");
        collectorProps2.setProperty("globpathmatcher", "*.xlsx");
        collectorProps2.setProperty("waitseconds", "10");
        collectorProps2.setProperty("actiononoriginal", "Delete");
        PropertyList emulatorProps2 = new PropertyList();
        emulatorProps2.setProperty("frequency", "5");
        emulatorProps2.setProperty("filename", "samplesforimport.xlsx");
        this.createInstrumentModel(GC, GC_AGILENT, "", collectorProps2, emulatorProps2, SimpleFileCollector, Node_SapphireCustom);
        this.createInstrumentModelAttachmentOperation(GC_AGILENT, GC, HANDLER2);
        String GC102 = "SDMS_GC102";
        String GC206 = "SDMS_GC206";
        PropertyList collectorProps_INST = new PropertyList();
        PropertyList emulatorProps1_INST = new PropertyList();
        PropertyList emulatorProps2_INST = emulatorProps1_INST.copy();
        try {
            JSONObject output = new JSONObject();
            output.put("sdcid", "Sample");
            output.put("copies", "1");
            output.put("sampledesc", "Created from SDMS for instrument " + GC102);
            emulatorProps1_INST.setProperty("filecontent", output.toString());
        }
        catch (Exception output) {
            // empty catch block
        }
        try {
            FileManager.FileData fileData = new FileManager.FileData(resourceBundle.getString(rXSLX_IMPORT), FileType.getFileTypeByName("XLSX", this.getConnectionId()).getMime());
            emulatorProps2_INST.setProperty("filecontent", fileData.getDataURL());
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.createInstrument(GC102, GC, GC_HONEYWELL, "", CollectorUK, rootpath.resolve("UK/network1/path/" + GC102).toString(), collectorProps_INST, emulatorProps1_INST, SimpleFileCollector, Node_SapphireCustom);
        this.createInstrument(GC206, GC, GC_AGILENT, "", CollectorUSA, rootpath.resolve("USA/network1/path/" + GC206).toString(), collectorProps_INST, emulatorProps2_INST, SimpleFileCollector, Node_SapphireCustom);
    }

    public void addCategory(String categoryId, String sdcId, String keyid1) throws ActionException {
        DataSet ds = this.qp.getPreparedSqlDataSet("select * from category where categoryid=? and sdcid=?", new Object[]{categoryId, sdcId});
        if (ds.size() == 0) {
            String sql = "insert into category (categoryid, sdcid) values (?, ?)";
            this.qp.execPreparedUpdate(sql, new Object[]{categoryId, sdcId});
        }
        if ((ds = this.qp.getPreparedSqlDataSet("select * from categoryitem where categoryid=? and sdcid=? and keyid1=?", new Object[]{categoryId, sdcId, keyid1})).size() == 0) {
            PropertyList category = new PropertyList();
            category.setProperty("categoryid", categoryId);
            category.setProperty("sdcid", sdcId);
            category.setProperty("keyid1", keyid1);
            this.ap.processAction("AddCategoryItem", "1", category);
        }
    }

    public void buildScenario(String scenario) throws IOException, ActionException, SapphireException {
        Scenarios scenarios = Scenarios.SIMPLEINTERNAL;
        try {
            scenarios = Scenarios.valueOf(scenario.toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        switch (scenarios) {
            case SIMPLEINTERNAL: {
                this.buildScenarioSimpleInternal();
                break;
            }
            case ICECREAMLAB: {
                this.buildScenarioIceCreamLab();
                break;
            }
            case ICECREAMLABFULL: {
                this.buildScenarioIceCreamLabFull();
                break;
            }
        }
    }

    private class CollectorBuilder {
        private String id;
        private String remotePath;
        private String internalflagExternal;
        private String storagemode;
        private int instruments;
        private String instrumentprefix;

        public CollectorBuilder(String id, String remotePath, String internalflagExternal, String storagemode, int instruments, String instrumentprefix) {
            this.id = id;
            this.remotePath = remotePath;
            this.internalflagExternal = internalflagExternal;
            this.storagemode = storagemode;
            this.instruments = instruments;
            this.instrumentprefix = instrumentprefix;
        }
    }

    public static enum Scenarios {
        SIMPLEINTERNAL("Simple Internal"),
        ICECREAMLAB("Ice Cream Lab"),
        ICECREAMLABFULL("Ice Cream Lab FULL");

        String description = "";

        public String getDescription() {
            return this.description;
        }

        private Scenarios(String description) {
            this.description = description;
        }
    }
}

