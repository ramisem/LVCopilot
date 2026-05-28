/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.portal;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PortalDataBuilder
extends BaseCustom
implements SDMSConstants {
    private static final String CATEGORY = "PortalDemoData";
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("com.labvantage.sapphire.modules.portal.PortalDataBuilderBundle");
    public static final String rUSER_PROFILEPIC_ALICE = "USER_PROFILEPIC_ALICE";
    public static final String rUSER_PROFILEPIC_BOB = "USER_PROFILEPIC_BOB";
    public static final String rUSER_PROFILEPIC_CHARLIE = "USER_PROFILEPIC_CHARLIE";
    public static final String rUSER_PROFILEPIC_DEBS = "USER_PROFILEPIC_DEBS";
    public static final String rUSER_PROFILEPIC_GARY = "USER_PROFILEPIC_GARY";
    public static final String rUSER_PROFILEPIC_MARK = "USER_PROFILEPIC_MARK";
    public static final String rPORTAL_LOGO = "PORTAL_LOGO";
    public static final String rPORTAL_BACKGROUND = "PORTAL_BACKGROUND";
    public static final String rREQUEST_IMAGE_LEMON = "REQUEST_IMAGE_LEMON";
    public static final String rREQUEST_IMAGE_SALTEDCARAMEL = "REQUEST_IMAGE_SALTEDCARAMEL";
    public static final String rREQUEST_IMAGE_TUTTIFRUITTI = "REQUEST_IMAGE_TUTTIFRUITTI";
    public static final String rREQUEST_IMAGE_STRAWBERRY = "REQUEST_IMAGE_STRAWBERRY";
    public static final String rREQUEST_IMAGE_CHOCFUDGE = "REQUEST_IMAGE_CHOCFUDGE";
    public static final String rPORTAL_PROPS = "PORTAL_PROPS";
    Writer out;
    QueryProcessor qp;
    ActionProcessor ap;
    private int workitemitemid = 0;

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public PortalDataBuilder(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.qp = new QueryProcessor(connectionid);
        this.ap = new ActionProcessor(connectionid);
    }

    public PortalDataBuilder(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.qp = new QueryProcessor(rakFile, connectionid);
        this.ap = new ActionProcessor(rakFile, connectionid);
    }

    public void setOut(Writer out) {
        this.out = out;
    }

    public void logMessage(String s) throws IOException {
        Trace.logInfo("PORTAL  DEMO BUILDER: " + s);
        if (this.out != null) {
            this.out.write(s + "<br>");
        }
    }

    private void setupPortal(String portalId) throws SapphireException, IOException {
        Trace.logInfo("Setting up portal " + portalId);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Portal");
        props.setProperty("keyid1", portalId);
        props.setProperty("productvaluetree", resourceBundle.getString(rPORTAL_PROPS));
        this.ap.processAction("EditSDI", "1", props);
        this.addAttachment("LV_Portal", portalId, "Icon", "Icon", "icon.png", resourceBundle.getString(rPORTAL_LOGO));
        this.addAttachment("LV_Portal", portalId, "Logo Dark", "Logo Dark", "icon.png", resourceBundle.getString(rPORTAL_LOGO));
        this.addAttachment("LV_Portal", portalId, "Logo Light", "Logo Light", "icon.png", resourceBundle.getString(rPORTAL_LOGO));
        this.addAttachment("LV_Portal", portalId, "Background", "Logon Image", "background.png", resourceBundle.getString(rPORTAL_BACKGROUND));
    }

    private void createClient(String clientId, String clientName) throws SapphireException, IOException {
        String newDep = this.createDepartment(clientId, clientName, false);
        if (newDep.length() > 0) {
            this.qp.execPreparedUpdate("INSERT INTO portaldepartment ( portalid, departmentid ) VALUES ( ?, ? )", new String[]{"RequestPortal", clientId});
            this.qp.execPreparedUpdate("INSERT INTO appdepartment ( appid, departmentid ) VALUES ( ?, ? )", new String[]{"SubmissionRequestApp", clientId});
            this.qp.execPreparedUpdate("INSERT INTO appdepartment ( appid, departmentid ) VALUES ( ?, ? )", new String[]{"KitRequestApp", clientId});
            this.qp.execPreparedUpdate("INSERT INTO appdepartment ( appid, departmentid ) VALUES ( ?, ? )", new String[]{"ClientAdminApp", clientId});
        }
    }

    private void createSite(String siteId, String siteName) throws SapphireException, IOException {
        String newDep = this.createDepartment(siteId, siteName, true);
    }

    private String createDepartment(String departmentid, String departmentName, boolean isSite) throws SapphireException, IOException {
        String out = "";
        if (this.qp.getPreparedSqlDataSet("SELECT departmentid FROM department WHERE departmentid=?", (Object[])new String[]{departmentid}).getRowCount() > 0) {
            this.logMessage("Department " + departmentid + " already exists, so leave");
        } else {
            this.logMessage("Creating Portal Client " + departmentid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Department");
            props.setProperty("keyid1", departmentid);
            props.setProperty("departmentdesc", departmentName);
            props.setProperty("sitedepartmentflag", "Y");
            props.setProperty("portalclientflag", "Y");
            if (isSite) {
                props.setProperty("sitedepartmentflag", "Y");
            }
            this.ap.processAction("AddSDI", "1", props);
            this.addCategory(CATEGORY, "Department", departmentid);
            out = props.getProperty("newkeyid1");
        }
        return out;
    }

    public String createPortalUser(String logonname, String name, String client, boolean portaladmin) throws ActionException, IOException {
        String sysuserid = this.createUser("", logonname, name, client, portaladmin);
        if (sysuserid.length() > 0) {
            this.qp.execPreparedUpdate("INSERT INTO appsysuser ( appid, sysuserid ) VALUES ( ?, ? )", new String[]{"SubmissionRequestApp", sysuserid});
            this.qp.execPreparedUpdate("INSERT INTO appsysuser ( appid, sysuserid ) VALUES ( ?, ? )", new String[]{"KitRequestApp", sysuserid});
            if (portaladmin) {
                this.qp.execPreparedUpdate("INSERT INTO appsysuser ( appid, sysuserid ) VALUES ( ?, ? )", new String[]{"ClientAdminApp", sysuserid});
            }
        }
        return sysuserid;
    }

    public String createUser(String sysuserid, String logonname, String name, String client, boolean portaladmin) throws ActionException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT logonname FROM sysuser WHERE logonname=?", (Object[])new String[]{logonname}).getRowCount() > 0) {
            this.logMessage("User " + logonname + " already exists, so leave");
        } else {
            this.logMessage("Creating User " + logonname);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("templateid", "PortalUser");
            props.setProperty("logonname", logonname);
            props.setProperty("email", logonname);
            props.setProperty("keyid1", sysuserid.length() > 0 ? sysuserid : "(null)");
            props.setProperty("sysuserdesc", name);
            props.setProperty("password", name);
            props.setProperty("nameduserflag", "P");
            props.setProperty("calendarid", "(null)");
            props.setProperty("glpflag", "Y");
            props.setProperty("glpflag", "Y");
            props.setProperty("basedepartment", client);
            this.ap.processAction("AddSDI", "1", props);
            sysuserid = props.getProperty("newkeyid1");
            this.addCategory(CATEGORY, "User", sysuserid);
        }
        if (client.length() > 0 && sysuserid.length() > 0 && portaladmin) {
            this.qp.execPreparedUpdate("UPDATE departmentsysuser  set portaladministratorflag=? WHERE departmentid=? AND sysuserid=?", new String[]{"Y", client, sysuserid});
        }
        return sysuserid;
    }

    public String addWorkitemParamlist(String workitemid, String paramlistid, String variantid) throws IOException {
        this.logMessage("&nbsp;&nbsp;&nbsp;Adding " + paramlistid + " (" + variantid + ") to " + workitemid);
        String id = "" + this.workitemitemid++;
        this.qp.execPreparedUpdate("insert into workitemitem ( workitemid, workitemversionid, workitemitemid, sdcid, keyid1, keyid2, keyid3, forcenewflag ) values ( ?,?,?,?,?,?,?,?)", new Object[]{workitemid, "1", id, "ParamList", paramlistid, "1", variantid, "Y"});
        return id;
    }

    public void createWorkitem(String workitemid) throws ActionException, IOException {
        if (this.qp.getPreparedSqlDataSet("SELECT workitemid, workitemversionid FROM workitem WHERE workitemid=? AND workitemversionid=?", (Object[])new String[]{workitemid, "1"}).getRowCount() > 0) {
            this.logMessage("Work item " + workitemid + " already exists so update.");
        } else {
            this.logMessage("Creating workitem " + workitemid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "WorkItem");
            props.setProperty("keyid1", workitemid);
            props.setProperty("keyid2", "1");
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
                if (this.qp.getPreparedSqlDataSet("SELECT paramid FROM param WHERE paramid=?", (Object[])new String[]{paramid}).getRowCount() > 0) {
                    this.logMessage("Param " + paramid + " already exists so update.");
                } else {
                    this.logMessage("Creating parameter " + paramid);
                    props = new PropertyList();
                    props.setProperty("sdcid", "Param");
                    props.setProperty("keyid1", paramid);
                    this.ap.processAction("AddSDI", "1", props);
                    this.addCategory(CATEGORY, "Param", paramid);
                }
                this.qp.execPreparedUpdate("INSERT INTO paramlistitem ( paramlistid, paramlistversionid, variantid, paramid, paramtype, datatypes, numreplicates ) values (?,?,?,?,?,?,?)", new Object[]{paramlistid, "1", variantid, paramid, "Standard", datatype[i], 1});
            }
            this.addCategory(CATEGORY, "ParamList", paramlistid);
        }
    }

    public void createSampleType(String sampletypeid, String sampletypedesc) throws IOException, ActionException {
        if (this.qp.getPreparedSqlDataSet("SELECT s_sampletypeid FROM s_sampletype WHERE s_sampletypeid=?", (Object[])new String[]{sampletypeid}).getRowCount() > 0) {
            this.logMessage("Sample type " + sampletypeid + " already exists so update.");
        } else {
            this.logMessage("Creating sample type " + sampletypeid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "SampleType");
            props.setProperty("copies", "1");
            props.setProperty("keyid1", sampletypeid);
            props.setProperty("sampletypedesc", sampletypedesc);
            this.ap.processAction("AddSDI", "1", props);
            this.addCategory(CATEGORY, "SampleType", sampletypeid);
        }
    }

    public void createSampleTemplate(String templateid, String templatedesc, String sampletype) throws IOException, ActionException {
        if (this.qp.getPreparedSqlDataSet("SELECT s_sampleid FROM s_sample WHERE s_sampleid=?", (Object[])new String[]{templateid}).getRowCount() > 0) {
            this.logMessage("Sample " + templateid + " already exists so update.");
        } else {
            this.logMessage("Creating sample template " + templateid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("copies", "1");
            props.setProperty("keyid1", templateid);
            props.setProperty("sampledesc", templatedesc);
            props.setProperty("samplestatus", "Initial");
            props.setProperty("templateflag", "Y");
            props.setProperty("autoreceiveflag", "Y");
            props.setProperty("sampletypeid", sampletype);
            this.ap.processAction("AddSDI", "1", props);
            this.addCategory(CATEGORY, "Sample", templateid);
        }
    }

    public String createRequest(String requestdesc, String requester, String client, String site) throws IOException, ActionException {
        this.logMessage("Creating request " + requestdesc);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Request");
        props.setProperty("copies", "1");
        props.setProperty("templateid", "Research Submission");
        props.setProperty("requestdesc", requestdesc);
        props.setProperty("requeststatus", "Draft");
        props.setProperty("requesterid", requester);
        props.setProperty("submitbydepartmentid", client);
        props.setProperty("requestdt", "n+1");
        props.setProperty("sitedepartmentid", site);
        this.ap.processAction("AddSDI", "1", props);
        String rid = props.getProperty("newkeyid1");
        this.addCategory(CATEGORY, "Request", rid);
        this.qp.execPreparedUpdate("UPDATE s_request set createby=? WHERE s_requestid=?", new String[]{requester, rid});
        return rid;
    }

    public void createRequestItem(String requestid, String requesttext, String testmethod, String template) throws IOException, ActionException {
        this.logMessage("Creating request item for  " + requestid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_RequestItem");
        props.setProperty("copies", "1");
        props.setProperty("requestid", requestid);
        props.setProperty("templatesdcid", "Sample");
        props.setProperty("templatekeyid1", template);
        props.setProperty("itemcount", "1");
        props.setProperty("itemcount", "1");
        props.setProperty("requesttext", requesttext);
        props.setProperty("templatepropsascolumn", "Y");
        this.ap.processAction("AddSDI", "1", props);
        String rid = props.getProperty("newkeyid1");
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_RequestItem");
        actionProps.setProperty("keyid1", rid);
        actionProps.setProperty("workitemid", testmethod);
        actionProps.setProperty("workitemversionid", "1");
        actionProps.setProperty("propsmatch", "N");
        actionProps.setProperty("applyworkitem", "N");
        this.ap.processAction("AddSDIWorkItem", "1", actionProps);
    }

    public void cleanUp() throws SapphireException, IOException {
        this.logMessage("Ready to clean up.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemitem WHERE itemkeyid1 IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidatarelation WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidataitem WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidata WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM sdidataapproval WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.logMessage("Deleted data items.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemrelation WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitemitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.qp.execPreparedUpdate("DELETE FROM sdiworkitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.qp.execPreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = 'Request' AND keyid1 IN (SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=?)", new String[]{CATEGORY, "Request"});
        this.logMessage("Deleted attachments for Requests.<br>");
        this.qp.execPreparedUpdate("DELETE FROM s_requestitemdetail WHERE requestid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Request"});
        this.qp.execPreparedUpdate("DELETE FROM s_requestitem WHERE requestid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Request"});
        this.qp.execPreparedUpdate("DELETE FROM s_request WHERE s_requestid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Request"});
        this.logMessage("Deleted requests.<br>");
        this.qp.execPreparedUpdate("DELETE FROM workitemitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.logMessage("Deleted workitems.<br>");
        this.qp.execPreparedUpdate("DELETE FROM paramlistitem WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.logMessage("Deleted param lists.<br>");
        this.qp.execPreparedUpdate("DELETE FROM param WHERE paramid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Param"});
        this.qp.execPreparedUpdate("DELETE FROM paramlist WHERE paramlistid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "ParamList"});
        this.qp.execPreparedUpdate("DELETE FROM workitem WHERE workitemid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "WorkItem"});
        this.logMessage("Deleted parameters.<br>");
        this.qp.execPreparedUpdate("DELETE FROM s_sample WHERE s_sampleid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Sample"});
        this.qp.execPreparedUpdate("DELETE FROM s_sampletype WHERE s_sampletypeid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "SampleType"});
        this.logMessage("Deleted sample template.<br>");
        this.qp.execPreparedUpdate("DELETE FROM departmentsysuser WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
        this.qp.execPreparedUpdate("UPDATE sysuser SET basedepartment = null WHERE basedepartment IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
        this.qp.execPreparedUpdate("DELETE FROM appdepartment WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
        this.qp.execPreparedUpdate("DELETE FROM portaldepartment WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
        this.logMessage("Deleted departments.<br>");
        this.qp.execPreparedUpdate("DELETE FROM sysuserconfig WHERE sysuserid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "User"});
        this.qp.execPreparedUpdate("DELETE FROM sysuserrole WHERE sysuserid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "User"});
        this.qp.execPreparedUpdate("DELETE FROM appsysuser WHERE sysuserid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "User"});
        this.qp.execPreparedUpdate("DELETE FROM sysuser WHERE sysuserid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "User"});
        this.qp.execPreparedUpdate("DELETE FROM department WHERE departmentid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "Department"});
        this.logMessage("Deleted Portal Users.<br>");
        this.qp.execPreparedUpdate("DELETE FROM refvalue WHERE reftypeid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "RefType"});
        this.qp.execPreparedUpdate("DELETE FROM reftype WHERE reftypeid IN ( SELECT keyid1 FROM categoryitem WHERE categoryid=? AND sdcid=? )", new String[]{CATEGORY, "RefType"});
        this.logMessage("Deleted reftypes.<br>");
        this.qp.execSQL("DELETE FROM sdiattachment WHERE sdcid = 'Portal' AND keyid1='RequestPortal'");
        this.qp.execPreparedUpdate("UPDATE portal set productvaluetree=? WHERE portalid=", new String[]{"", "RequestPortal"});
        this.logMessage("Portal Reset.<br>");
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

    public void createRefType(String reftypeid, String[] values) throws IOException, ActionException {
        if (this.qp.getPreparedSqlDataSet("SELECT reftypeid FROM reftype WHERE reftypeid=?", (Object[])new String[]{reftypeid}).getRowCount() > 0) {
            this.logMessage("Reftype " + reftypeid + " already exists so update.");
        } else {
            this.logMessage("Creating reftype " + reftypeid);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "RefType");
            props.setProperty("keyid1", reftypeid);
            props.setProperty("copies", "1");
            this.ap.processAction("AddSDI", "1", props);
            for (int i = 0; i < values.length; ++i) {
                String value = values[i];
                this.logMessage("Creating refvalue " + value);
                this.qp.execPreparedUpdate("INSERT INTO refvalue ( reftypeid, refvalueid, refvaluedesc ) values (?,?,?)", new Object[]{reftypeid, value, value});
            }
            this.addCategory(CATEGORY, "RefType", reftypeid);
        }
    }

    private void buildScenarioIceCreamLab() throws ActionException, IOException, SapphireException {
        String client = "TastersRUs";
        this.createClient(client, "Tasters R Us");
        String site = "AcmeIceCream";
        this.createSite(site, "Acme Ice Cream");
        String u = this.createPortalUser("alice@tastersrus.com", "Alice", client, false);
        this.addProfilePicture(u, resourceBundle.getString(rUSER_PROFILEPIC_ALICE));
        u = this.createPortalUser("bob@tastersrus.com", "Bob", client, false);
        this.addProfilePicture(u, resourceBundle.getString(rUSER_PROFILEPIC_BOB));
        u = this.createPortalUser("charlie@tastersrus.com", "Charlie", client, false);
        this.addProfilePicture(u, resourceBundle.getString(rUSER_PROFILEPIC_CHARLIE));
        u = this.createPortalUser("debs@tastersrus.com", "Debs", client, false);
        this.addProfilePicture(u, resourceBundle.getString(rUSER_PROFILEPIC_DEBS));
        String mark = this.createPortalUser("mark@tastersrus.com", "Mark", client, false);
        this.addProfilePicture(mark, resourceBundle.getString(rUSER_PROFILEPIC_MARK));
        String gary = this.createPortalUser("gary@tastersrus.com", "Gary", client, true);
        this.addProfilePicture(gary, resourceBundle.getString(rUSER_PROFILEPIC_GARY));
        this.createRefType("Taste", new String[]{"Citrusy", "Fruity", "Meaty", "Salty", "Soily", "Sour", "Sweet"});
        this.createRefType("Smell", new String[]{"Aromatic", "Bad", "Good", "OK", "Pungent"});
        this.createParamList("IceCreamTasteAnalysis", "prep", new String[]{"Weight"});
        this.createParamList("IceCreamTasteAnalysis", "1", new String[]{"Taste", "Smell"});
        this.createWorkitem("IceCreamTasteTest");
        this.addWorkitemParamlist("IceCreamTasteTest", "IceCreamTasteAnalysis", "prep");
        this.addWorkitemParamlist("IceCreamTasteTest", "IceCreamTasteAnalysis", "1");
        this.createParamList("IceCreamMeltAnalysis", "prep", new String[]{"Weight"});
        this.createParamList("IceCreamMeltAnalysis", "1", new String[]{"Viscosity1Min", "Viscosity5Min", "Viscosity10Min", "Viscosity30Min"});
        this.createWorkitem("IceCreamMeltTest");
        this.addWorkitemParamlist("IceCreamMeltTest", "IceCreamMeltAnalysis", "prep");
        this.addWorkitemParamlist("IceCreamMeltTest", "IceCreamMeltAnalysis", "1");
        String template = "Ice Cream Sample";
        this.createSampleType("Ice Cream", "Ice Cream");
        this.createSampleTemplate(template, "Ice Cream", "Ice Cream");
        String requesttext = "Simple Tub";
        for (int i = 0; i < 10; ++i) {
            String request = this.createRequest("Ice Cream Analysis Of Tutti Fruitti", mark, client, site);
            this.addAttachment("Request", request, "Tutti Fruitti", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_TUTTIFRUITTI));
            this.createRequestItem(request, requesttext, "IceCreamTasteTest", template);
            request = this.createRequest("Ice Cream Analysis Of Choc Fudge", mark, client, site);
            this.addAttachment("Request", request, "Choc Fudge", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_CHOCFUDGE));
            this.createRequestItem(request, requesttext, "IceCreamTasteTest", template);
            request = this.createRequest("Ice Cream Analysis Of Strawberry Cream", mark, client, site);
            this.addAttachment("Request", request, "Strawberry Cream", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_STRAWBERRY));
            this.createRequestItem(request, requesttext, "IceCreamTasteTest", template);
            request = this.createRequest("Ice Cream Analysis Of Lemon Sorbe", mark, client, site);
            this.addAttachment("Request", request, "Lemon Sorbe", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_LEMON));
            this.createRequestItem(request, requesttext, "IceCreamTasteTest", template);
            request = this.createRequest("Ice Cream Analysis Of Salted Caramel", mark, client, site);
            this.addAttachment("Request", request, "Salted Caramel", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_SALTEDCARAMEL));
            this.createRequestItem(request, requesttext, "IceCreamTasteTest", template);
            request = this.createRequest("Melt Analysis Of Tutti Fruitti", gary, client, site);
            this.addAttachment("Request", request, "Tutti Fruitti", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_TUTTIFRUITTI));
            this.createRequestItem(request, requesttext, "IceCreamMeltTest", template);
            request = this.createRequest("Melt Analysis Of Choc Fudge", gary, client, site);
            this.addAttachment("Request", request, "Choc Fudge", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_CHOCFUDGE));
            this.createRequestItem(request, requesttext, "IceCreamMeltTest", template);
            request = this.createRequest("Melt Analysis Of Strawberry Cream", gary, client, site);
            this.addAttachment("Request", request, "Strawberry Cream", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_STRAWBERRY));
            this.createRequestItem(request, requesttext, "IceCreamMeltTest", template);
            request = this.createRequest("Melt Analysis Of Lemon Sorbe", gary, client, site);
            this.addAttachment("Request", request, "Lemon Sorbe", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_LEMON));
            this.createRequestItem(request, requesttext, "IceCreamMeltTest", template);
            request = this.createRequest("Melt Analysis Of Salted Caramel", gary, client, site);
            this.addAttachment("Request", request, "Salted Caramel", "", "image.jpg", resourceBundle.getString(rREQUEST_IMAGE_SALTEDCARAMEL));
            this.createRequestItem(request, requesttext, "IceCreamMeltTest", template);
        }
        this.setupPortal("RequestPortal");
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
        Scenarios scenarios = Scenarios.ICECREAMLAB;
        try {
            scenarios = Scenarios.valueOf(scenario.toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        switch (scenarios) {
            case ICECREAMLAB: {
                this.buildScenarioIceCreamLab();
                break;
            }
        }
    }

    public static enum Scenarios {
        ICECREAMLAB("Ice Cream Lab");

        String description = "";

        public String getDescription() {
            return this.description;
        }

        private Scenarios(String description) {
            this.description = description;
        }
    }
}

