/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.select.Elements
 */
package com.labvantage.sapphire.modules.eln;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.EditWorksheet;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest;
import com.labvantage.sapphire.modules.eln.gwt.server.LockWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.pageelements.gwt.server.ApprovalStepUtil;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Worksheet
extends BaseCustom
implements ELNConstants {
    protected SapphireConnection sapphireConnection;
    private SDIData worksheetData;
    private boolean preview = false;
    private PropertyList returnPolicy;
    private String userprivs;
    private String usertype;
    private DataSet cachedAttributes;
    DataSet dependencyList = new DataSet();

    public Worksheet(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
        this.setConnectionId(sapphireConnection.getConnectionId());
    }

    public void open(String worksheetid, String worksheetversionid, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        boolean suppressActivityLog = commandRequest.getString("suppressactivitylog", "N").equalsIgnoreCase("Y");
        PropertyList openProps = commandRequest.getStringPropertyList();
        openProps.setProperty("openworksheet", "Y");
        openProps.setProperty("loadviewhtml", "Y");
        openProps.setProperty("loadoptions", "Y");
        openProps.setProperty("lockrequired", commandRequest.getString("lockrequired", "N"));
        this.load(worksheetid, worksheetversionid, openProps);
        DataSet worksheet = this.getWorksheet();
        if (worksheet.size() == 1) {
            ActionBlock ab = new ActionBlock();
            if (!(this.isPreview() || worksheet.getValue(0, "templateflag").equals("Y") || !worksheet.getValue(0, "worksheetstatus").equals("Pending") && worksheet.getValue(0, "authorid").length() != 0)) {
                if (worksheet.getValue(0, "worksheetstatus").equals("Pending")) {
                    PropertyList setStatusProps = new PropertyList();
                    setStatusProps.setProperty("worksheetid", worksheetid);
                    setStatusProps.setProperty("worksheetversionid", worksheetversionid);
                    setStatusProps.setProperty("status", "InProgress");
                    ab.setActionClass("SetStatus", SetWorksheetStatus.class.getName(), setStatusProps);
                    worksheet.setValue(0, "worksheetstatus", "InProgress");
                }
                if (worksheet.getValue(0, "authorid").length() == 0) {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("worksheetid", worksheetid);
                    editProps.setProperty("worksheetversionid", worksheetversionid);
                    editProps.setProperty("authorid", this.sapphireConnection.getSysuserId());
                    editProps.setProperty("authordt", "now");
                    editProps.setProperty("elnrequest", "Y");
                    ab.setActionClass("EditAuthor", EditWorksheet.class.getName(), editProps);
                    worksheet.setValue(0, "authorid", this.sapphireConnection.getSysuserId());
                    M18NUtil m18n = new M18NUtil(this.sapphireConnection);
                    worksheet.setValue(0, "authordt", m18n.format(m18n.getNowCalendar()));
                }
            }
            if (!suppressActivityLog) {
                PropertyList activityProps = new PropertyList();
                activityProps.setProperty("worksheetid", worksheetid);
                activityProps.setProperty("worksheetversionid", worksheetversionid);
                activityProps.setProperty("targetsdcid", "LV_Worksheet");
                activityProps.setProperty("targetkeyid1", worksheetid);
                activityProps.setProperty("targetkeyid2", worksheetversionid);
                activityProps.setProperty("activitytype", "Open");
                activityProps.setProperty("activitylog", "Opened worksheet");
                ab.setActionClass("LogActivity", AddWorksheetActivity.class.getName(), activityProps);
            }
            this.getActionProcessor().processActionBlock(ab);
            PropertyList wsOptions = new PropertyList();
            try {
                wsOptions.setJSONString(worksheet.getValue(0, "options", "{}"));
            }
            catch (JSONException e) {
                throw new SapphireException("Unable to load worksheet options", e);
            }
            ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
            PropertyList elnPolicy = configurationProcessor.getPolicy("ELNPolicy", wsOptions.getProperty("worksheetpolicynode", "Sapphire Custom"));
            this.returnPolicy = new PropertyList();
            if (!this.isPreview()) {
                PropertyListCollection filelocations;
                PropertyList filePolicy;
                this.usertype = "W";
                DataSet usertypes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 'A' type, null departmentid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? AND worksheet.authorid = ? UNION SELECT 'C' type, null departmentid FROM worksheetcontributor WHERE worksheetid = ? AND worksheetversionid = ? AND worksheetcontributor.contributorid = ? AND nominatedflag = 'Y' UNION SELECT 'D' type, null FROM departmentsysuser WHERE departmentsysuser.sysuserid = ? AND departmentsysuser.departmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? ) ) UNION SELECT 'D' type, null FROM departmentsysuser WHERE departmentsysuser.sysuserid = ? AND departmentsysuser.departmentid IN (SELECT departmentid FROM JOBTYPEDEPARTMENT, SYSUSERJOBTYPE, sysuser WHERE JOBTYPEDEPARTMENT.JOBTYPEID = SYSUSERJOBTYPE.JOBTYPEID AND SYSUSERJOBTYPE.SYSUSERID IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? ) AND sysuser.SYSUSERID = SYSUSERJOBTYPE.SYSUSERID AND sysuser.securitytypeflag = 'J') UNION SELECT 'W' type, null FROM sysuser WHERE sysuserid = ? UNION SELECT DISTINCT 'L' type, departmentid FROM worksheetactivitylog, sysuser LEFT OUTER JOIN departmentsysuser ON departmentsysuser.sysuserid = sysuser.sysuserid AND departmentsysuser.departmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? ) ) WHERE worksheetactivitylog.activityby = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? AND sysuser.sysuserid = ? ORDER BY type", new Object[]{worksheetid, worksheetversionid, this.sapphireConnection.getSysuserId(), worksheetid, worksheetversionid, this.sapphireConnection.getSysuserId(), this.sapphireConnection.getSysuserId(), worksheetid, worksheetversionid, this.sapphireConnection.getSysuserId(), worksheetid, worksheetversionid, this.sapphireConnection.getSysuserId(), worksheetid, worksheetversionid, worksheetid, worksheetversionid, this.sapphireConnection.getSysuserId()});
                if (usertypes.size() > 0) {
                    String string = usertypes.getValue(0, "type").equals("L") ? (usertypes.getValue(0, "departmentid").length() > 0 ? "D" : "W") : (this.usertype = usertypes.getValue(0, "type"));
                }
                if (!this.usertype.equals("A") && !this.usertype.equals("C")) {
                    String worksheetStatus = worksheet.getValue(0, "worksheetstatus");
                    if (worksheet.getValue(0, "approvable").equals("Y")) {
                        this.usertype = "R";
                    } else if (worksheetStatus.equals("Complete") || worksheetStatus.equals("Cancelled")) {
                        DataSet approvalSteps;
                        DataSet dataSet = approvalSteps = this.worksheetData != null ? this.worksheetData.getDataset("approvalstep") : null;
                        if (approvalSteps != null) {
                            for (int i = 0; i < approvalSteps.size(); ++i) {
                                if (!approvalSteps.getValue(i, "reviewedby").equals(this.sapphireConnection.getSysuserId())) continue;
                                this.usertype = "R";
                            }
                        }
                    }
                }
                PropertyList attachments = elnPolicy.getPropertyList("attachments");
                this.returnPolicy.setProperty("filerepositoryid", attachments.getProperty("filerepositoryid"));
                this.returnPolicy.setProperty("filerepositorynode", attachments.getProperty("filerepositorynode"));
                this.returnPolicy.setProperty("attachmentclass", attachments.getProperty("attachmentclass"));
                if (attachments != null && attachments.getProperty("attachmentsbyref", "N").equals("Y") && (filePolicy = configurationProcessor.getPolicy("FileLocationPolicy", attachments.getProperty("attachbyrefpolicynode"))) != null && (filelocations = filePolicy.getCollection("locations")) != null) {
                    for (int i = 0; i < filelocations.size(); ++i) {
                        PropertyList filelocation = filelocations.getPropertyList(i);
                        if (!filelocation.getProperty("id").equals(attachments.getProperty("attachbyreffilelocation"))) continue;
                        String sh = Configuration.getInstance().getSapphireHome();
                        this.returnPolicy.setProperty("attachmenttype", "U");
                        this.returnPolicy.setProperty("attachmentuploaddir", StringUtil.replaceAll(StringUtil.replaceAll(filelocation.getProperty("location"), "[labvantagehome]", sh), "[sapphirehome]", sh));
                        break;
                    }
                }
                this.returnPolicy.setProperty("incidents", this.getIncidentProperties(elnPolicy));
                PropertyList globalattachments = elnPolicy.getPropertyListNotNull("globaltemplates");
                this.returnPolicy.setProperty("globaltemplates", globalattachments);
                PropertyList addContent = elnPolicy.getPropertyList("addcontent");
                PropertyListCollection controls = addContent.getCollection("controls");
                HashMap<String, PropertyList> pseudoGroups = new HashMap<String, PropertyList>();
                for (int i = 0; i < controls.size(); ++i) {
                    PropertyListCollection subcontrols;
                    PropertyList control = controls.getPropertyList(i);
                    if (control.getProperty("groupid").length() <= 0) continue;
                    String groupid = control.getProperty("groupid");
                    PropertyList newSubControl = control.copy();
                    PropertyList pseudoGroup = (PropertyList)pseudoGroups.get(groupid);
                    if (pseudoGroup == null) {
                        control.setProperty("type", "Group");
                        control.setProperty("text", groupid);
                        subcontrols = new PropertyListCollection();
                        control.setProperty("controls", subcontrols);
                        subcontrols.add(newSubControl);
                        pseudoGroups.put(groupid, control);
                        continue;
                    }
                    subcontrols = pseudoGroup.getCollection("controls");
                    subcontrols.add(newSubControl);
                    control.setProperty("show", "N");
                }
                this.returnPolicy.setProperty("addcontent", addContent);
                this.returnPolicy.setProperty("exportoptions", elnPolicy.getPropertyListNotNull("exportformat").getPropertyListNotNull("exportoptions"));
                PropertyList noteProps = configurationProcessor.findPolicy("sdinotes", "sdcid", "LV_Worksheet");
                if (noteProps == null || noteProps.size() == 0) {
                    noteProps = new PropertyList();
                }
                this.returnPolicy.setProperty("notesconfig", noteProps);
                this.returnPolicy.setProperty("wsm", this.sapphireConnection.hasRole(elnPolicy.getProperty("wsmrole")) ? "Y" : "N");
                String jsonPrivs = worksheet.getValue(0, "userprivs", "{}");
                if (jsonPrivs.equals("{}")) {
                    jsonPrivs = elnPolicy.getProperty("userprivs", "{}");
                }
                try {
                    JSONObject privs = new JSONObject(jsonPrivs);
                    StringBuffer sb = new StringBuffer(";");
                    if (privs.has(this.usertype)) {
                        JSONObject usertypeprivs = privs.getJSONObject(this.usertype);
                        Iterator iterator = usertypeprivs.keys();
                        while (iterator.hasNext()) {
                            sb.append(iterator.next()).append(";");
                        }
                    }
                    this.userprivs = sb.toString();
                }
                catch (JSONException e) {
                    throw new SapphireException("Failed to load user privs. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            }
            this.returnPolicy.setProperty("tablestyling", Worksheet.getTableStyling(elnPolicy));
            ELNRequest.updateMRUList(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.getConnectionId()), this.sapphireConnection.getSysuserId(), commandRequest.getString("__hostwebpageid") + "_recent_worksheets", worksheetid + ";" + worksheetversionid);
        }
        commandResponse.set("workbook", this.getWorkbook());
        commandResponse.set("usernames", this.getUserNames());
        commandResponse.set("worksheet", this.getWorksheet());
        commandResponse.set("worksheetsections", this.getWorksheetSections());
        commandResponse.set("worksheetitems", this.getWorksheetItems());
        commandResponse.set("policy", this.getReturnPolicy());
        commandResponse.set("usertype", this.getUsertype());
        commandResponse.set("userprivs", this.getUserprivs());
        commandResponse.set("currentuserdetails", this.getCurrentUserDetails());
        commandResponse.set("dependencylist", this.dependencyList);
    }

    private JSONable getCurrentUserDetails() {
        JSONableMap jso = new JSONableMap();
        jso.put("sysuserid", this.sapphireConnection.getSysuserId());
        jso.put("sysusername", this.sapphireConnection.getSysuserName());
        jso.put("defaultdepartment", this.sapphireConnection.getDefaultDepartment());
        jso.put("departmentlist", this.sapphireConnection.getDepartmentList());
        jso.put("connectionid", this.sapphireConnection.getConnectionId());
        jso.put("databaseid", this.sapphireConnection.getDatabaseId());
        return jso;
    }

    public static String getTableStyling(PropertyList elnPolicy) {
        StringBuilder cssOut = new StringBuilder();
        PropertyListCollection styling = elnPolicy.getCollectionNotNull("tablestyling");
        for (int i = 0; i < styling.size(); ++i) {
            PropertyList style = styling.getPropertyList(i);
            String tableclass = style.getProperty("tableclass");
            String table = Worksheet.prepStyle(style.getProperty("table"));
            String title = Worksheet.prepStyle(style.getProperty("title"));
            String value = Worksheet.prepStyle(style.getProperty("value"));
            cssOut.append("." + tableclass + table);
            cssOut.append("." + tableclass + " .title " + title);
            cssOut.append("." + tableclass + " .value " + value);
            cssOut.append(style.getProperty("extra"));
        }
        return cssOut.toString();
    }

    private static String prepStyle(String css) {
        if (css == null) {
            return "";
        }
        if (!(css = css.trim()).startsWith("{")) {
            css = "{" + css;
        }
        if (!css.endsWith("}")) {
            css = css + "}";
        }
        css = StringUtil.replaceAll(css, "\n", "");
        return css;
    }

    private PropertyList getIncidentProperties(PropertyList elnPolicy) {
        HashSet<String> incidentid = new HashSet<String>();
        PropertyList incidents = elnPolicy.getPropertyListNotNull("incidents").copy();
        PropertyListCollection templates = incidents.getCollectionNotNull("templates");
        for (int i = templates.size() - 1; i >= 0; --i) {
            PropertyList template = templates.getPropertyList(i);
            if (template.getProperty("show", "Y").equals("N")) {
                templates.remove(i);
                continue;
            }
            incidentid.add(template.getProperty("incidentid"));
        }
        if (incidentid.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet incidentTemplates = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM incident WHERE incidentid in (" + safeSQL.addIn(incidentid) + ")", safeSQL.getValues());
            String format = incidents.getProperty("templatedropdownformat", "[incidentid] ([incidenttype])");
            String[] tokens = StringUtil.getTokens(format);
            templates.clear();
            for (int i = 0; i < incidentTemplates.size(); ++i) {
                PropertyList template = new PropertyList();
                templates.add(template);
                String desc = format;
                for (String token : tokens) {
                    if (!incidentTemplates.isValidColumn(token)) continue;
                    desc = StringUtil.replaceAll(desc, "[" + token + "]", incidentTemplates.getValue(i, token));
                }
                template.setProperty("id", incidentTemplates.getValue(i, "incidentid"));
                template.setProperty("desc", desc);
            }
        }
        return incidents;
    }

    public DataSet loadWorksheetFields(String worksheetid, String worksheetversionid) throws SapphireException {
        CommandRequest commandRequest = new CommandRequest("lf");
        commandRequest.set("worksheetid", worksheetid);
        commandRequest.set("worksheetversionid", worksheetversionid);
        CommandResponse commandResponse = new CommandResponse();
        HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
        requestMap.put("commandrequest", commandRequest);
        requestMap.put("commandresponse", commandResponse);
        RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionid());
        HashMap returnMap = requestProcessor.processRequest("com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest", requestMap);
        commandResponse = (CommandResponse)returnMap.get("commandresponse");
        return (DataSet)commandResponse.get("fields");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public SDIData load(String worksheetid, String worksheetversionid, PropertyList properties) throws SapphireException {
        DataSet items;
        DataSet sections;
        int i;
        String worksheetsectionid = properties.getProperty("worksheetsectionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        boolean loadWorksheet = worksheetsectionid.length() == 0 && worksheetitemid.length() == 0;
        boolean worksheetOnly = properties.getProperty("worksheetonly").equals("Y");
        boolean loadSection = worksheetsectionid.length() > 0;
        boolean loadItem = worksheetitemid.length() > 0;
        boolean loadViewHTML = properties.getProperty("loadviewhtml", "N").equals("Y");
        boolean convertOptionsToJson = properties.getProperty("convertoptionstojson", "Y").equals("Y");
        boolean loadOptions = properties.getProperty("loadoptions", "N").equals("Y");
        boolean loadBehaviors = properties.getProperty("loadbehaviors", "Y").equals("Y");
        boolean isLoadingForCopy = properties.getProperty("loadforcopy", "N").equals("Y");
        boolean lockRequired = properties.getProperty("lockrequired", "N").equalsIgnoreCase("Y");
        this.preview = properties.getProperty("preview", "N").equals("Y");
        String highlight = properties.getProperty("highlight");
        highlight = highlight.startsWith("+") ? highlight.substring(1) : highlight;
        highlight = highlight.endsWith("*") || highlight.endsWith("~") ? highlight.substring(0, highlight.length() - 1) : highlight;
        worksheetversionid = BaseELNAction.resolveVersion(this.getQueryProcessor(), worksheetid, worksheetversionid, "worksheet");
        this.clearRSet(this.sapphireConnection, properties.getProperty("rsetid"));
        SDIRequest wsRequest = new SDIRequest();
        if (loadWorksheet) {
            wsRequest.setSDIList("LV_Worksheet", worksheetid, worksheetversionid, "");
            PropertyListCollection worksheetcolumns = this.getSDCProcessor().getColumns("LV_Worksheet");
            StringBuffer worksheetrequestItem = new StringBuffer();
            for (int i2 = 0; i2 < worksheetcolumns.size(); ++i2) {
                worksheetrequestItem.append(",").append(worksheetcolumns.getPropertyList(i2).getProperty("columnid"));
            }
            wsRequest.setRequestItem("primary[" + worksheetrequestItem.substring(1) + ", authorid.sysuserdesc, " + Worksheet.getIncidentCountClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + ", " + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + "]");
            if (!this.preview) {
                wsRequest.setRequestItem("approval");
                wsRequest.setRequestItem("approvalstep");
                wsRequest.setRequestItem("worksheetactivitylog");
                wsRequest.setRetainRsetid(true);
            }
        } else {
            wsRequest.setSDIList("LV_Worksheet", "__dummy__", "__dummy__", "");
            wsRequest.setRequestItem("primary");
        }
        wsRequest.setExtendedDataTypes(true);
        this.worksheetData = this.getSDIProcessor().getSDIData(wsRequest);
        if (this.worksheetData == null || this.worksheetData.getDataset("primary") == null) throw new SapphireException("Failed to load worksheet");
        DataSet worksheet = this.worksheetData.getDataset("primary");
        worksheet.setString(0, "__checkedoutbyuser", "__notchangecontrolled");
        boolean isTemplate = worksheet.getValue(0, "templateflag").equals("Y");
        boolean isGlobal = worksheet.getValue(0, "templateprivacyflag").equals("G");
        PropertyList wsOptions = new PropertyList();
        wsOptions.setPropertyList(worksheet.getClob(0, "options", ""));
        boolean excludeNoIncludes = wsOptions.getProperty("excludesectionnoincludes", "N").equals("Y");
        boolean excludeUnavailable = wsOptions.getProperty("excludeitemunavailable", "N").equals("Y");
        boolean excludeNoData = wsOptions.getProperty("excludeitemnodata", "N").equals("Y");
        String legacyDefault = excludeNoData || excludeUnavailable || excludeNoIncludes ? "Y" : "N";
        boolean useLegacyExcludeRules = wsOptions.getProperty("uselegacyexcluderules", legacyDefault).equals("Y");
        wsOptions.setProperty("uselegacyexcluderules", useLegacyExcludeRules ? "Y" : "N");
        if (loadWorksheet) {
            DataSet wsApproval;
            if (convertOptionsToJson) {
                worksheet.setValue(0, "options", wsOptions.toJSONString());
            }
            if (highlight.length() > 0) {
                worksheet.setString(0, "worksheetdesc_hl", worksheet.getValue(0, "worksheetdesc"));
            }
            if ((!this.preview && wsOptions.getProperty("lockworksheet", "N").equals("Y") || isTemplate && isGlobal) && lockRequired) {
                CMTPolicy cmtPolicy;
                String changecontrolledflag;
                ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
                PropertyList lockProps = new PropertyList();
                lockProps.setProperty("worksheetid", worksheetid);
                lockProps.setProperty("worksheetversionid", worksheetversionid);
                lockProps.setProperty("rsetid", this.worksheetData.getRsetid());
                actionProcessor.processActionClass(LockWorksheet.class.getName(), lockProps);
                worksheet.setString(0, "rsetid", lockProps.getProperty("rsetid"));
                if (lockProps.getProperty("lockedby").length() > 1) {
                    worksheet.setString(0, "__lockedby", lockProps.getProperty("lockedby"));
                }
                String string = changecontrolledflag = (cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "LV_Worksheet")).isChangeControlDeferToRepository() ? "N" : cmtPolicy.getChangeControlledFlag();
                if (isTemplate && isGlobal && ("Y".equals(changecontrolledflag) || "T".equals(changecontrolledflag) && isTemplate)) {
                    worksheet.setString(0, "__checkedoutbyuser", lockProps.getProperty("checkedoutbyuserid"));
                    worksheet.setString(0, "__checkedoutbydepartment", lockProps.getProperty("checkedoutbydepartmentid"));
                }
            } else {
                this.clearRSet(this.sapphireConnection, this.worksheetData.getRsetid());
            }
            String workbookid = worksheet.getValue(0, "workbookid");
            String workbookversionid = worksheet.getValue(0, "workbookversionid");
            DataSet workbook = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workbook.*, sysuser.sysuserdesc ownername FROM workbook LEFT OUTER JOIN sysuser ON workbook.ownerid = sysuser.sysuserid WHERE workbookid = ? AND workbookversionid = ?", new Object[]{workbookid, workbookversionid}, true);
            this.worksheetData.setDataset("workbook", workbook);
            if (!this.preview && (wsApproval = this.worksheetData.getDataset("approval")) != null) {
                if (wsApproval.size() == 1) {
                    worksheet.addColumn("approvable", 0);
                    worksheet.addColumn("hasapprovalstarted", 0);
                    DataSet wsApprovalSteps = this.worksheetData.getDataset("approvalstep");
                    wsApprovalSteps.addColumn("stepstatusflag", 0);
                    DataSet peerApprovalDataSet = this.getPeerApprovalDataSet(worksheet);
                    ApprovalStepUtil.checkApprovalSteps(wsApprovalSteps, this.sapphireConnection.getRoleList(), this.sapphireConnection.getSysuserId(), wsApproval.getValue(0, "sequenceflag"), wsApproval.getValue(0, "uniquenessflag"), peerApprovalDataSet, "activityby", false);
                    for (i = 0; i < wsApprovalSteps.size(); ++i) {
                        if (!wsApprovalSteps.getValue(i, "stepstatusflag").equals("C") || !wsApprovalSteps.getValue(i, "approvalflag").equals("U")) continue;
                        worksheet.setValue(0, "approvable", worksheet.getValue(0, "worksheetstatus").equals("PendingApproval") ? "Y" : "N");
                        break;
                    }
                    worksheet.setString(0, "hasapprovalstarted", ApprovalStepUtil.hasApprovalStarted(wsApprovalSteps) ? "Y" : "N");
                } else if (wsApproval.size() > 1) {
                    ActionBlock ab = new ActionBlock();
                    ELNRequest.resolveWorksheetSDIApprovals(this.getQueryProcessor(), worksheetid, worksheetversionid, wsOptions, ab);
                    if (ab.getActionCount() > 0) {
                        this.getActionProcessor().processActionBlock(ab);
                    }
                }
            }
        } else {
            worksheet.addRow();
        }
        if (!worksheetOnly) {
            String loadSectionWhere = "";
            SDIRequest wssRequest = new SDIRequest();
            wssRequest.setSDCid("LV_WorksheetSection");
            if (loadWorksheet) {
                wssRequest.setQueryFrom("worksheetsection");
                wssRequest.setQueryWhere("worksheetid='" + SafeSQL.encodeForSQL(worksheetid, this.sapphireConnection.isOracle()) + "' AND worksheetversionid='" + SafeSQL.encodeForSQL(worksheetversionid, this.sapphireConnection.isOracle()) + "'");
                wssRequest.setQueryOrderBy("usersequence");
                wssRequest.setShowTemplates(true);
                PropertyListCollection sectioncolumns = this.getSDCProcessor().getColumns("LV_WorksheetSection");
                StringBuffer sectionrequestItem = new StringBuffer();
                for (int i3 = 0; i3 < sectioncolumns.size(); ++i3) {
                    sectionrequestItem.append(",").append(sectioncolumns.getPropertyList(i3).getProperty("columnid"));
                }
                wssRequest.setRequestItem("primary[" + sectionrequestItem.substring(1) + ", " + Worksheet.getIncidentCountClause("LV_WorksheetSection", "worksheetsection", "worksheetsectionid", "worksheetsectionversionid") + ", " + Worksheet.getNoteStatusClause("LV_WorksheetSection", "worksheetsection", "worksheetsectionid", "worksheetsectionversionid") + "]");
                if (!this.preview) {
                    wssRequest.setRequestItem("approval");
                    wssRequest.setRequestItem("approvalstep");
                }
            } else if (loadSection) {
                String worksheetsectionversionid = properties.getProperty("worksheetsectionversionid", "1");
                if (properties.getProperty("includesubsections").equals("Y")) {
                    DBUtil dbUtil = new DBUtil();
                    dbUtil.setConnection(this.sapphireConnection);
                    loadSectionWhere = BaseELNAction.getSubSectionWhere(worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, dbUtil);
                    wssRequest.setQueryFrom("worksheetsection");
                    wssRequest.setQueryWhere(loadSectionWhere);
                    wssRequest.setQueryOrderBy("usersequence");
                } else {
                    wssRequest.setKeyid1List(worksheetsectionid);
                    wssRequest.setKeyid2List(worksheetsectionversionid);
                    loadSectionWhere = "worksheetsectionid='" + SafeSQL.encodeForSQL(worksheetsectionid, this.sapphireConnection.isOracle()) + "' AND worksheetsectionversionid='" + SafeSQL.encodeForSQL(properties.getProperty("worksheetsectionversionid", "1"), this.sapphireConnection.isOracle()) + "'";
                }
                wssRequest.setRequestItem("primary");
            } else if (loadItem) {
                wssRequest.setKeyid1List("__dummy__");
                wssRequest.setKeyid2List("__dummy__");
                wssRequest.setRequestItem("primary");
            }
            wssRequest.setExtendedDataTypes(true);
            SDIData sectionData = this.getSDIProcessor().getSDIData(wssRequest);
            SDIRequest wsiRequest = new SDIRequest();
            wsiRequest.setSDCid("LV_WorksheetItem");
            if (loadWorksheet) {
                wsiRequest.setQueryFrom("worksheetitem");
                wsiRequest.setQueryWhere("worksheetid='" + SafeSQL.encodeForSQL(worksheetid, this.sapphireConnection.isOracle()) + "' AND worksheetversionid='" + SafeSQL.encodeForSQL(worksheetversionid, this.sapphireConnection.isOracle()) + "'");
                wsiRequest.setQueryOrderBy("usersequence");
                wsiRequest.setShowTemplates(true);
                PropertyListCollection itemcolumns = this.getSDCProcessor().getColumns("LV_WorksheetItem");
                StringBuffer itemrequestItem = new StringBuffer();
                for (i = 0; i < itemcolumns.size(); ++i) {
                    itemrequestItem.append(",").append(itemcolumns.getPropertyList(i).getProperty("columnid"));
                }
                wsiRequest.setRequestItem("primary[" + itemrequestItem.substring(1) + ", " + Worksheet.getIncidentCountClause("LV_WorksheetItem", "worksheetitem", "worksheetitemid", "worksheetitemversionid") + ", " + Worksheet.getNoteStatusClause("LV_WorksheetItem", "worksheetitem", "worksheetitemid", "worksheetitemversionid") + "]");
            } else if (loadSection) {
                wsiRequest.setQueryFrom("worksheetitem");
                wsiRequest.setQueryWhere(loadSectionWhere);
                wsiRequest.setQueryOrderBy("usersequence");
                wsiRequest.setShowTemplates(true);
                wsiRequest.setRequestItem("primary");
            } else {
                wsiRequest.setKeyid1List(worksheetitemid);
                wsiRequest.setKeyid2List(properties.getProperty("worksheetitemversionid"));
                wsiRequest.setRequestItem("primary");
            }
            wsiRequest.setExtendedDataTypes(true);
            SDIData itemData = this.getSDIProcessor().getSDIData(wsiRequest);
            DataSet items2 = null;
            if (itemData != null && itemData.getDataset("primary") != null) {
                items2 = itemData.getDataset("primary");
            }
            if (sectionData == null || sectionData.getDataset("primary") == null) throw new SapphireException("Failed to load worksheetsection");
            DataSet sections2 = sectionData.getDataset("primary");
            sections2.addColumn("include", 0);
            sections2.addColumn("_behavior", 0);
            sections2.addColumn("_behaviordepid", 0);
            sections2.addColumn("_behaviordepwhentype", 0);
            if (loadWorksheet) {
                sections2.addColumn("approvable", 0);
                sections2.addColumn("hasapprovalstarted", 0);
                DataSet wssApproval = sectionData.getDataset("approval");
                DataSet wssApprovalSteps = sectionData.getDataset("approvalstep");
                if (wssApprovalSteps != null) {
                    wssApprovalSteps.addColumn("stepstatusflag", 0);
                }
                HashMap<String, String> findMap = new HashMap<String, String>();
                int lastSectionBehaviorLevel = -1;
                String lastSectionBehaviorType = "";
                for (int i4 = 0; i4 < sections2.size(); ++i4) {
                    sections2.setValue(i4, "include", "Y");
                    int currentlevel = Integer.parseInt(sections2.getValue(i4, "sectionlevel", "1"));
                    if (highlight.length() > 0) {
                        sections2.setString(i4, "worksheetsectiondesc_hl", sections2.getValue(i4, "worksheetsectiondesc").replaceAll("(?i)(" + Pattern.quote(highlight) + ")", "<span style=\"background-color:yellow\">$1</span>"));
                    }
                    PropertyList wssOptions = new PropertyList();
                    wssOptions.setPropertyList(sections2.getClob(i4, "options", ""));
                    if (convertOptionsToJson) {
                        sections2.setValue(i4, "options", wssOptions.toJSONString());
                    }
                    sections2.setString(i4, "option_allowadditem", wssOptions.getProperty("allowadditem", "Y"));
                    sections2.setString(i4, "option_sectioncompletion", wssOptions.getProperty("sectioncompletion", "N"));
                    sections2.setString(i4, "option_excludesectionfromexport", wssOptions.getProperty("excludesectionfromexport", "n"));
                    sections2.setString(i4, "option_sectionbehaviortype", wssOptions.getProperty("sectionbehaviortype"));
                    if (isTemplate) {
                        sections2.setString(i4, "option_sectionbehaviorwhentype", wssOptions.getProperty("sectionbehaviorwhentype"));
                        sections2.setString(i4, "option_sectionbehaviorwhen", wssOptions.getProperty("sectionbehaviorwhen"));
                        sections2.setString(i4, "option_sectionid", wssOptions.getProperty("sectionid"));
                        sections2.setString(i4, "option_controlid", wssOptions.getProperty("controlid"));
                        sections2.setString(i4, "option_generatesectionrepeat", wssOptions.getProperty("generatesectionrepeat"));
                    }
                    String sectionStatus = sections2.getValue(i4, "sectionstatus");
                    if (!isTemplate) {
                        if (lastSectionBehaviorLevel >= 0 && currentlevel > lastSectionBehaviorLevel) {
                            sections2.setString(i4, "_behavior", lastSectionBehaviorType);
                        } else {
                            lastSectionBehaviorLevel = -1;
                            lastSectionBehaviorType = "";
                            String internalBehaviorType = sectionStatus.equals("Complete") || sectionStatus.equals("PendingApproval") ? sections2.getValue(i4, "behaviortype", "none") : this.deriveSectionBehaviorType(worksheetid, worksheetversionid, wssOptions, sections2, i4, items2);
                            sections2.setString(i4, "_behavior", internalBehaviorType);
                            if (!internalBehaviorType.equals("none")) {
                                lastSectionBehaviorLevel = currentlevel;
                                lastSectionBehaviorType = internalBehaviorType;
                            }
                        }
                    }
                    if (this.preview || wssApproval == null || wssApproval.size() <= 0 || wssApprovalSteps == null || wssApprovalSteps.size() <= 0) continue;
                    findMap.put("keyid1", sections2.getValue(i4, "worksheetsectionid"));
                    findMap.put("keyid2", sections2.getValue(i4, "worksheetsectionversionid"));
                    int wssApprovalRow = wssApproval.findRow(findMap);
                    if (wssApprovalRow <= -1) continue;
                    DataSet wssApprovalStepsFiltered = wssApprovalSteps.getFilteredDataSet(findMap);
                    DataSet peerApprovalDataSet = this.getPeerApprovalDataSet(worksheet);
                    int dummyrow = peerApprovalDataSet.addRow();
                    peerApprovalDataSet.setValue(dummyrow, "activityby", sections2.getValue(i4, "createby"));
                    ApprovalStepUtil.checkApprovalSteps(wssApprovalStepsFiltered, this.sapphireConnection.getRoleList(), this.sapphireConnection.getSysuserId(), wssApproval.getValue(wssApprovalRow, "sequenceflag"), wssApproval.getValue(wssApprovalRow, "uniquenessflag"), peerApprovalDataSet, "activityby", false);
                    for (int j = 0; j < wssApprovalStepsFiltered.size(); ++j) {
                        if (!wssApprovalStepsFiltered.getValue(j, "stepstatusflag").equals("C") || !wssApprovalStepsFiltered.getValue(j, "approvalflag").equals("U")) continue;
                        sections2.setValue(i4, "approvable", sections2.getValue(i4, "sectionstatus").equals("PendingApproval") ? "Y" : "N");
                        break;
                    }
                    sections2.setString(i4, "hasapprovalstarted", ApprovalStepUtil.hasApprovalStarted(wssApprovalStepsFiltered) ? "Y" : "N");
                }
            } else if (loadItem) {
                sections2.addRow();
            }
            if (items2 == null) throw new SapphireException("Failed to load worksheetitem");
            items2.addColumn("include", 0);
            if (loadWorksheet || loadSection && loadViewHTML || loadItem && loadViewHTML) {
                DataSet worksheetFields = this.loadWorksheetFields(worksheetid, worksheetversionid);
                items2.addColumn("_behavior", 0);
                for (int i5 = 0; i5 < items2.size(); ++i5) {
                    try {
                        items2.setValue(i5, "include", "Y");
                        WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)items2.get(i5), properties.getProperty("width"));
                        worksheetItem.setTemplate(isTemplate);
                        worksheetItem.setWorksheetFields(worksheetFields);
                        PropertyList wsiOptions = worksheetItem.getWorksheetItemOptions().toPropertyList();
                        items2.setString(i5, "itemtype", worksheetItem.getClientRenderer());
                        items2.setString(i5, "elementid", worksheetItem.getElementId());
                        boolean supportsDataAvailability = worksheetItem.getWorksheetItemOptions().getOption("supportsdataavailability").equals("Y");
                        if (loadViewHTML && (items2.getValue(i5, "itemstatus", "InProgress").equals("InProgress") || items2.getValue(i5, "html").length() == 0) || worksheetItem.getWorksheetItemOptions().getOption("isalwayslive").equals("Y")) {
                            if (items2.getValue(i5, "propertytreeid").equalsIgnoreCase("SignatureControl") && properties.getProperty("export").equalsIgnoreCase("Y")) {
                                items2.setValue(i5, "html", worksheetItem.getExportHTML(properties));
                            } else if (isLoadingForCopy) {
                                items2.setValue(i5, "html", worksheetItem.getCopyHTML());
                            } else {
                                items2.setValue(i5, "html", worksheetItem.getViewHTML());
                            }
                            if (supportsDataAvailability) {
                                items2.setString(i5, "availabilitystatus", worksheetItem.getAvailability());
                            }
                        } else if (supportsDataAvailability && items2.getValue(i5, "itemstatus", "InProgress").equals("InProgress")) {
                            worksheetItem.setAvailability();
                            items2.setString(i5, "availabilitystatus", worksheetItem.getAvailability());
                        }
                        if (worksheetItem.getWorksheetItemOptions().getOption("hideifnodata", "N").equals("Y") && items2.getValue(i5, "availabilitystatus").equals("NoData")) {
                            items2.setString(i5, "include", "N");
                        }
                        if (highlight.length() > 0) {
                            String html = items2.getValue(i5, "html");
                            Document document = Jsoup.parse((String)html);
                            Elements elements = document.body().select("*");
                            String TEMP_SUBSTITUTE = "!?!?!?!?";
                            for (Element element : elements) {
                                String text = element.ownText();
                                if (!text.contains(highlight)) continue;
                                element.text(text.replace(highlight, TEMP_SUBSTITUTE));
                            }
                            html = document.html().replace(TEMP_SUBSTITUTE, "<mark>" + highlight + "</mark>");
                            items2.setValue(i5, "html", html);
                            items2.setString(i5, "_hl", items2.getValue(i5, "html").contains("<mark>") ? "Y" : "N");
                        }
                        if (loadOptions) {
                            if (convertOptionsToJson) {
                                items2.setValue(i5, "options", wsiOptions.toJSONString());
                            }
                            items2.setString(i5, "option_itemcompletion", wsiOptions.getProperty("itemcompletion", "N"));
                            items2.setString(i5, "option_excludeitemfromrollup", wsiOptions.getProperty("excludeitemfromrollup", "N"));
                            items2.setString(i5, "option_excludeitemfromexport", wsiOptions.getProperty("excludeitemfromexport", "n"));
                        }
                        this.dependencyList.copyRow(worksheetItem.getDependencyList(), -1, 1);
                        continue;
                    }
                    catch (Exception e) {
                        items2.setString(i5, "itemtype", "Generic");
                        items2.setValue(i5, "html", "<p style=\"color:Red\">Error loading class for worksheetitem type '" + items2.getValue(i5, "propertytreeid") + "'</p>");
                    }
                }
            }
            if (!isTemplate && loadBehaviors) {
                this.determineSectionAndItemBehaviors(worksheetid, worksheetversionid, sections2, items2, false, useLegacyExcludeRules, wsOptions);
            }
            this.worksheetData.setSDIData("sections", sectionData);
            this.worksheetData.setSDIData("items", itemData);
        }
        if (!loadWorksheet) return this.worksheetData;
        HashSet<String> userSet = new HashSet<String>();
        DataSet worksheet2 = this.worksheetData.getDataset("primary");
        if (worksheet2 != null) {
            userSet.add(worksheet2.getString(0, "authorid"));
            userSet.add(worksheet2.getString(0, "createby"));
            userSet.add(worksheet2.getString(0, "modby"));
            userSet.add(worksheet2.getString(0, "contentcompletedby"));
        }
        if ((sections = this.getWorksheetSections()) != null) {
            for (int i6 = 0; i6 < sections.size(); ++i6) {
                userSet.add(sections.getString(i6, "createby"));
                userSet.add(sections.getString(i6, "modby"));
                userSet.add(sections.getString(i6, "contentcompletedby"));
            }
        }
        if ((items = this.getWorksheetItems()) != null) {
            for (int i7 = 0; i7 < items.size(); ++i7) {
                userSet.add(items.getString(i7, "createby"));
                userSet.add(items.getString(i7, "modby"));
                userSet.add(items.getString(i7, "contentcompletedby"));
            }
        }
        SafeSQL safeSQL = new SafeSQL();
        DataSet users = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sysuserid, sysuserdesc FROM sysuser WHERE sysuserid in ( " + safeSQL.addIn(userSet) + ")", safeSQL.getValues());
        this.worksheetData.setDataset("usernames", users);
        return this.worksheetData;
    }

    private boolean deriveMatchOnMetadata(String worksheetid, String worksheetversionid, PropertyList wssOptions, DataSet sections, int row, String behaviorWhenType, String worksheetItemid, String worksheetItemVersionid) {
        DataSet attributes;
        String metadata;
        boolean matched = false;
        HashMap<String, String> filter = new HashMap<String, String>();
        if (behaviorWhenType.equals("worksheet_metadata")) {
            metadata = wssOptions.getProperty("worksheet_metadata");
            filter.put("type", "W");
            attributes = this.getWorksheetAttributesCached(worksheetid, worksheetversionid, filter);
        } else if (behaviorWhenType.equals("section_metadata")) {
            metadata = wssOptions.getProperty("section_metadata");
            filter.put("type", "S");
            filter.put("id", worksheetItemid);
            filter.put("version", worksheetItemVersionid);
            attributes = this.getWorksheetAttributesCached(worksheetid, worksheetversionid, filter);
        } else {
            metadata = wssOptions.getProperty("control_metadata");
            filter.put("type", "I");
            filter.put("id", worksheetItemid);
            filter.put("version", worksheetItemVersionid);
            attributes = this.getWorksheetAttributesCached(worksheetid, worksheetversionid, filter);
        }
        if (metadata.contains("=")) {
            String[] parts = StringUtil.split(metadata, "=");
            String attributeid = parts[0];
            String value = parts[1];
            int findRow = attributes.findRow("attributeid", attributeid);
            if (findRow >= 0 && value.length() > 0) {
                BigDecimal numericValue;
                sections.setValue(row, "_behaviordepwhentype", behaviorWhenType);
                sections.setValue(row, "_behaviordepid", (worksheetItemid.length() > 0 ? worksheetItemid + ";" + worksheetItemVersionid + ";" : "") + attributeid);
                String datatype = attributes.getValue(findRow, "datatype");
                matched = datatype.equals("N") ? (numericValue = attributes.getBigDecimal(findRow, "numericvalue")) != null && new BigDecimal(value).compareTo(numericValue) == 0 : attributes.getValue(findRow, "textvalue").equals(value);
            }
        }
        return matched;
    }

    private DataSet getWorksheetAttributesCached(String worksheetid, String worksheetversionid, HashMap<String, String> filter) {
        if (this.cachedAttributes == null) {
            this.cachedAttributes = ELNRequest.loadAllWorksheetAttributes(this.getQueryProcessor(), new M18NUtil(this.sapphireConnection), worksheetid, worksheetversionid, true);
        }
        return this.cachedAttributes.getFilteredDataSet(filter);
    }

    private int findPriorSectionRow(DataSet sections, int row, boolean anyLevel) {
        int priorSectionRow = -1;
        int level = sections.getInt(row, "sectionlevel");
        for (int i = row - 1; i >= 0 && priorSectionRow == -1; --i) {
            if (sections.getValue(i, "_behavior").equals("hide") || !anyLevel && sections.getInt(i, "sectionlevel") != level) continue;
            priorSectionRow = i;
        }
        return priorSectionRow;
    }

    private DataSet getPeerApprovalDataSet(DataSet worksheet) {
        DataSet activitylog = this.worksheetData.getDataset("worksheetactivitylog");
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("activitytype", "SetContent");
        DataSet peerApprovalDataSet = activitylog.getFilteredDataSet(filterMap);
        int dummyrow = peerApprovalDataSet.addRow();
        peerApprovalDataSet.setValue(dummyrow, "activityby", worksheet.getValue(0, "authorid"));
        return peerApprovalDataSet;
    }

    public boolean isPreview() {
        return this.preview;
    }

    public DataSet getWorkbook() {
        return this.worksheetData != null ? this.worksheetData.getDataset("workbook") : null;
    }

    public DataSet getUserNames() {
        return this.worksheetData != null ? this.worksheetData.getDataset("usernames") : null;
    }

    public DataSet getWorksheet() {
        return this.worksheetData != null ? this.worksheetData.getDataset("primary") : null;
    }

    public DataSet getWorksheetSections() {
        return this.worksheetData != null && this.worksheetData.getSDIData("sections") != null ? this.worksheetData.getSDIData("sections").getDataset("primary") : null;
    }

    public DataSet getWorksheetItems() {
        return this.worksheetData != null && this.worksheetData.getSDIData("items") != null ? this.worksheetData.getSDIData("items").getDataset("primary") : null;
    }

    public PropertyList getReturnPolicy() {
        return this.returnPolicy != null ? this.returnPolicy : new PropertyList();
    }

    public String getUserprivs() {
        return this.userprivs != null ? this.userprivs : "";
    }

    public String getUsertype() {
        return this.usertype != null ? this.usertype : "W";
    }

    private boolean hasIncludedSubSections(DataSet sections, int index) {
        if (index + 1 == sections.size()) {
            return false;
        }
        int nextlevel = Integer.parseInt(sections.getValue(index + 1, "sectionlevel", "1"));
        if (nextlevel <= Integer.parseInt(sections.getValue(index, "sectionlevel", "1"))) {
            return false;
        }
        do {
            if (Integer.parseInt(sections.getValue(++index, "sectionlevel", "1")) > nextlevel) {
                return this.hasIncludedSubSections(sections, index);
            }
            if (sections.getValue(index, "include").equals("N")) continue;
            return true;
        } while (index < sections.size() - 1 && Integer.parseInt(sections.getValue(index + 1, "sectionlevel", "1")) == nextlevel);
        return false;
    }

    private void hideSection(DataSet sections, int row, DataSet items) {
        sections.setValue(row, "include", "N");
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("worksheetsectionid", sections.getValue(row, "worksheetsectionid"));
        filterMap.put("worksheetsectionversionid", sections.getValue(row, "worksheetsectionversionid"));
        DataSet sectionitems = items.getFilteredDataSet(filterMap);
        for (int i = 0; i < sectionitems.size(); ++i) {
            sectionitems.setValue(i, "include", "N");
        }
    }

    private void clearRSet(SapphireConnection sapphireConnection, String rsetid) {
        if (rsetid.length() > 0) {
            DAMProcessor damProcessor = new DAMProcessor(sapphireConnection.getConnectionId());
            damProcessor.clearRSet(rsetid);
        }
    }

    public static PropertyList getOptions(QueryProcessor queryProcessor, String worksheetid, String worksheetversionid) {
        PropertyList wsOptions = new PropertyList();
        DataSet worksheet = queryProcessor.getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
        if (worksheet.size() == 1) {
            try {
                wsOptions.setPropertyList(worksheet.getClob(0, "options", ""));
            }
            catch (Exception e) {
                Trace.logError("Failed to load worksheet options for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid), e);
            }
        }
        return wsOptions;
    }

    public static String getPolicyNode(QueryProcessor queryProcessor, String worksheetid, String worksheetversionid) {
        PropertyList options = Worksheet.getOptions(queryProcessor, worksheetid, worksheetversionid);
        return options.getProperty("worksheetpolicynode", "Sapphire Custom");
    }

    public static String getNoteStatusClause(String sdcid, String tableid, String id, String ver) {
        return "( SELECT CASE WHEN Sum ( CASE WHEN followupflag = 'Y' AND NOT Coalesce (resolvedflag,'N') = 'Y' THEN 1 ELSE 0 END ) > 0 THEN 'U' WHEN Sum ( CASE WHEN followupflag = 'Y' AND Coalesce (resolvedflag,'N') = 'Y' THEN 1 ELSE 0 END ) > 0 THEN 'R' WHEN Count(*) > 0 THEN 'Y' ELSE 'N' END flag FROM sdinote WHERE sdcid = '" + sdcid + "' AND keyid1 = " + tableid + "." + id + " AND keyid2 = " + tableid + "." + ver + " AND keyid3 = '(null)' ) notestatus";
    }

    public static String getIncidentCountClause(String sdcid, String tableid, String id, String ver) {
        return "( SELECT count(*) FROM incidentitem WHERE causalobjectflag='Y' AND sourcesdcid = '" + sdcid + "' AND sourcekeyid1 = " + tableid + "." + id + " AND sourcekeyid2 = " + tableid + "." + ver + " ) incidentcount";
    }

    public DataSet[] loadBehaviors(String worksheetid, String worksheetversionid, boolean refreshItemAvailability, boolean processLegacyExcludeRules) throws SapphireException {
        DataSet[] dataSetArray;
        int i;
        DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid, sectionstatus, sectionlevel, usersequence, options, behaviortype FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=?  order by usersequence", (Object[])new String[]{worksheetid, worksheetversionid}, true);
        DataSet items = null;
        if (refreshItemAvailability || processLegacyExcludeRules) {
            items = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, worksheetsectionid, worksheetsectionversionid, worksheetid, worksheetversionid, itemstatus, options, availabilitystatus, propertytreeid, config, contents  FROM worksheetitem WHERE worksheetid=? AND worksheetversionid=?  order by usersequence", (Object[])new String[]{worksheetid, worksheetversionid}, true);
        }
        this.determineSectionAndItemBehaviors(worksheetid, worksheetversionid, sections, items, refreshItemAvailability, processLegacyExcludeRules, null);
        for (i = 0; i < sections.size(); ++i) {
            sections.setString(i, "options", "");
        }
        if (items != null) {
            for (i = 0; i < items.size(); ++i) {
                items.setString(i, "options", "");
                items.setString(i, "config", "");
            }
        }
        if (items == null) {
            DataSet[] dataSetArray2 = new DataSet[1];
            dataSetArray = dataSetArray2;
            dataSetArray2[0] = sections;
        } else {
            DataSet[] dataSetArray3 = new DataSet[2];
            dataSetArray3[0] = sections;
            dataSetArray = dataSetArray3;
            dataSetArray3[1] = items;
        }
        return dataSetArray;
    }

    private void determineSectionAndItemBehaviors(String worksheetid, String worksheetversionid, DataSet sections, DataSet items, boolean refreshItemAvailability, boolean processLegacyExcludeRules, PropertyList worksheetOptions) throws SapphireException {
        if (refreshItemAvailability && items != null) {
            items.setString(-1, "include", "Y");
            for (int i = 0; i < items.size(); ++i) {
                HashMap rowdata = (HashMap)items.get(i);
                WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, rowdata, "-1");
                if (items.getValue(i, "itemstatus", "InProgress").equals("InProgress") && worksheetItem.getWorksheetItemOptions().getOption("supportsdataavailability").equals("Y")) {
                    worksheetItem.setAvailability();
                    String availabilityStatus = worksheetItem.getAvailability();
                    items.setString(i, "availabilitystatus", availabilityStatus);
                }
                if (!worksheetItem.getWorksheetItemOptions().getOption("hideifnodata", "N").equals("Y") || !items.getValue(i, "availabilitystatus").equals("NoData")) continue;
                items.setString(i, "include", "N");
            }
        }
        sections.addColumn("_behavior", 0);
        sections.addColumn("_behaviordepid", 0);
        sections.addColumn("_behaviordepwhentype", 0);
        int lastHideSectionBehaviorLevel = -1;
        int lastDisableSectionBehaviorLevel = -1;
        for (int i = 0; i < sections.size(); ++i) {
            try {
                int currentlevel = Integer.parseInt(sections.getValue(i, "sectionlevel", "1"));
                if (currentlevel <= 0) continue;
                PropertyList wssOptions = new PropertyList();
                if (sections.getClob(i, "options", "{}").startsWith("{")) {
                    wssOptions.setJSONString(sections.getClob(i, "options"));
                } else {
                    wssOptions.setPropertyList(sections.getClob(i, "options"));
                }
                String sectionStatus = sections.getValue(i, "sectionstatus");
                String nextBehaviorType = sectionStatus.equals("Complete") || sectionStatus.equals("PendingApproval") ? sections.getValue(i, "behaviortype", "none") : this.deriveSectionBehaviorType(worksheetid, worksheetversionid, wssOptions, sections, i, items);
                if (lastHideSectionBehaviorLevel >= 0 && currentlevel > lastHideSectionBehaviorLevel) {
                    sections.setString(i, "_behavior", "hide");
                    continue;
                }
                if (lastDisableSectionBehaviorLevel >= 0 && currentlevel > lastDisableSectionBehaviorLevel) {
                    lastHideSectionBehaviorLevel = -1;
                    if (nextBehaviorType.equals("hide")) {
                        lastHideSectionBehaviorLevel = currentlevel;
                        sections.setString(i, "_behavior", "hide");
                        continue;
                    }
                    sections.setString(i, "_behavior", "disable");
                    continue;
                }
                lastHideSectionBehaviorLevel = -1;
                lastDisableSectionBehaviorLevel = -1;
                sections.setString(i, "_behavior", nextBehaviorType);
                if (nextBehaviorType.equals("hide")) {
                    lastHideSectionBehaviorLevel = currentlevel;
                    continue;
                }
                if (!nextBehaviorType.equals("disable")) continue;
                lastDisableSectionBehaviorLevel = currentlevel;
                continue;
            }
            catch (Exception e) {
                Trace.logWarn("Failed to determine section behavior: " + e.getMessage());
            }
        }
        if (processLegacyExcludeRules) {
            if (worksheetOptions == null) {
                DataSet options = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetstatus, options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
                worksheetOptions = new PropertyList();
                worksheetOptions.setPropertyList(options.getString(0, "options"));
            }
            this.handleLegacyExcludeRules(sections, items, worksheetOptions);
        }
    }

    private String deriveSectionBehaviorType(String worksheetid, String worksheetversionid, PropertyList wssOptions, DataSet sections, int row, DataSet items) {
        String newBehavior = "none";
        String sectionid = sections.getString(row, "worksheetsectionid");
        try {
            String behaviorType = wssOptions.getProperty("sectionbehaviortype", "none");
            if (!behaviorType.equals("none")) {
                if (behaviorType.equals("sameaspriorsame") || behaviorType.equals("sameaspriorany")) {
                    int priorSectionRow = this.findPriorSectionRow(sections, row, behaviorType.equals("sameaspriorany"));
                    if (priorSectionRow >= 0) {
                        newBehavior = sections.getString(priorSectionRow, "_behavior");
                    }
                } else {
                    String behaviorWhen = wssOptions.getProperty("sectionbehaviorwhen", "always");
                    if (behaviorWhen.equals("always")) {
                        newBehavior = behaviorType;
                    } else {
                        boolean isMatched = false;
                        String behaviorWhenType = wssOptions.getProperty("sectionbehaviorwhentype");
                        if (behaviorWhenType.equals("worksheet_metadata")) {
                            isMatched = this.deriveMatchOnMetadata(worksheetid, worksheetversionid, wssOptions, sections, row, behaviorWhenType, "", "");
                        } else if (behaviorWhenType.equals("section_status") || behaviorWhenType.equals("section_metadata")) {
                            String targetSectionid = wssOptions.getProperty("sectionid");
                            int priorSectionRow = -1;
                            if (targetSectionid.equals("priorany")) {
                                priorSectionRow = this.findPriorSectionRow(sections, row, true);
                            } else if (targetSectionid.equals("priorsame")) {
                                priorSectionRow = this.findPriorSectionRow(sections, row, false);
                            } else {
                                String id = targetSectionid.substring("sectionid".length() + 1);
                                if (id.contains(";")) {
                                    String[] parts = StringUtil.split(id, ";");
                                    HashMap<String, String> find = new HashMap<String, String>();
                                    find.put("worksheetsectionid", parts[0]);
                                    find.put("worksheetsectionversionid", parts[1]);
                                    priorSectionRow = sections.findRow(find);
                                }
                            }
                            if (priorSectionRow >= 0 && sections.getInt(priorSectionRow, "sectionlevel") > 0) {
                                String priorSectionid = sections.getValue(priorSectionRow, "worksheetsectionid");
                                String priorSectionVersionid = sections.getValue(priorSectionRow, "worksheetsectionversionid");
                                String priorSectionKey = priorSectionid + ";" + priorSectionVersionid;
                                if (behaviorWhenType.equals("section_status")) {
                                    sections.setValue(row, "_behaviordepwhentype", "section_status");
                                    sections.setValue(row, "_behaviordepid", priorSectionKey);
                                    String behaviorSectionStatus = wssOptions.getProperty("sectionstatus");
                                    String sectionStatus = sections.getValue(priorSectionRow, "sectionstatus");
                                    switch (behaviorSectionStatus) {
                                        case "inprogress": {
                                            isMatched = sectionStatus.equals("InProgress");
                                            break;
                                        }
                                        case "complete": {
                                            isMatched = sectionStatus.equals("Complete");
                                            break;
                                        }
                                        case "pendingorcomplete": {
                                            isMatched = sectionStatus.equals("Complete") || sectionStatus.equals("PendingApproval");
                                        }
                                    }
                                } else if (behaviorWhenType.equals("section_metadata")) {
                                    isMatched = this.deriveMatchOnMetadata(worksheetid, worksheetversionid, wssOptions, sections, row, behaviorWhenType, priorSectionid, priorSectionVersionid);
                                }
                            }
                        } else if (behaviorWhenType.equals("control_status") || behaviorWhenType.equals("control_dataavailability") || behaviorWhenType.equals("control_metadata")) {
                            String targetControlid = wssOptions.getProperty("controlid");
                            String id = targetControlid.substring("controlid".length() + 1);
                            int dependsControlRow = -1;
                            if (id.contains(";")) {
                                String[] parts = StringUtil.split(id, ";");
                                HashMap<String, String> find = new HashMap<String, String>();
                                find.put("worksheetitemid", parts[0]);
                                find.put("worksheetitemversionid", parts[1]);
                                if (items == null) {
                                    items = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, worksheetsectionid, worksheetsectionversionid, worksheetid, worksheetversionid, itemstatus, options, availabilitystatus, propertytreeid, config, contents  FROM worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?  order by usersequence", (Object[])new String[]{parts[0], parts[1]}, true);
                                    dependsControlRow = 0;
                                } else {
                                    dependsControlRow = items.findRow(find);
                                }
                            }
                            if (dependsControlRow >= 0) {
                                String dependsItemid = items.getValue(dependsControlRow, "worksheetitemid");
                                String dependsItemVersionid = items.getValue(dependsControlRow, "worksheetitemversionid");
                                String dependsItemKey = dependsItemid + ";" + dependsItemVersionid;
                                if (behaviorWhenType.equals("control_status")) {
                                    sections.setValue(row, "_behaviordepwhentype", "control_status");
                                    sections.setValue(row, "_behaviordepid", dependsItemKey);
                                    String targetStatus = wssOptions.getProperty("controlstatus");
                                    String currentControlStatus = items.getValue(dependsControlRow, "itemstatus");
                                    switch (targetStatus) {
                                        case "inprogress": {
                                            isMatched = currentControlStatus.equals("InProgress");
                                            break;
                                        }
                                        case "complete": {
                                            isMatched = currentControlStatus.equals("Complete");
                                        }
                                    }
                                } else if (behaviorWhenType.equals("control_dataavailability")) {
                                    HashMap rowdata;
                                    WorksheetItem worksheetItem;
                                    sections.setValue(row, "_behaviordepwhentype", "control_dataavailability");
                                    sections.setValue(row, "_behaviordepid", dependsItemKey);
                                    if ((items.getValue(dependsControlRow, "availabilitystatus").length() == 0 || items.getValue(dependsControlRow, "itemstatus", "InProgress").equals("InProgress")) && (worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, rowdata = (HashMap)items.get(dependsControlRow), "-1")).getWorksheetItemOptions().getOption("supportsdataavailability").equals("Y")) {
                                        worksheetItem.setAvailability();
                                        String availabilityStatus = worksheetItem.getAvailability();
                                        items.setString(dependsControlRow, "availabilitystatus", availabilityStatus);
                                    }
                                    String targetAvailabilty = wssOptions.getProperty("controldataavailability");
                                    String availabilityStatus = items.getValue(dependsControlRow, "availabilitystatus");
                                    if (targetAvailabilty.equalsIgnoreCase("nodata")) {
                                        isMatched = availabilityStatus.equalsIgnoreCase("NoData");
                                    } else if (targetAvailabilty.equalsIgnoreCase("dataavailable")) {
                                        isMatched = availabilityStatus.equalsIgnoreCase("OK");
                                    } else if (targetAvailabilty.equalsIgnoreCase("dataunavailable")) {
                                        isMatched = availabilityStatus.equalsIgnoreCase("Unavailable");
                                    }
                                } else if (behaviorWhenType.equals("control_metadata")) {
                                    isMatched = this.deriveMatchOnMetadata(worksheetid, worksheetversionid, wssOptions, sections, row, behaviorWhenType, dependsItemid, dependsItemVersionid);
                                }
                            }
                        }
                        switch (behaviorType) {
                            case "show": {
                                newBehavior = isMatched ? "show" : "hide";
                                break;
                            }
                            case "hide": {
                                newBehavior = isMatched ? "hide" : "show";
                                break;
                            }
                            case "enable": {
                                newBehavior = isMatched ? "enable" : "disable";
                                break;
                            }
                            case "disable": {
                                newBehavior = isMatched ? "disable" : "enable";
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Failed to parse behavior rule for section " + sectionid + ": " + e.getMessage(), e);
        }
        return newBehavior;
    }

    private void handleLegacyExcludeRules(DataSet sections, DataSet items, PropertyList wsOptions) {
        int i;
        boolean excludeItemsUnavailable = wsOptions.getProperty("excludeitemunavailable", "N").equals("N");
        boolean excludeItemsNoData = wsOptions.getProperty("excludeitemnodata", "N").equals("N");
        boolean excludeSectionsNoIncludes = wsOptions.getProperty("excludesectionnoincludes", "N").equals("Y");
        boolean hasExcludes = false;
        HashMap<String, String> filter = new HashMap<String, String>();
        for (i = 1; i < sections.size(); ++i) {
            filter.put("worksheetsectionid", sections.getValue(i, "worksheetsectionid"));
            filter.put("worksheetsectionversionid", sections.getValue(i, "worksheetsectionversionid"));
            DataSet sectionitems = items.getFilteredDataSet(filter);
            int includeCount = 0;
            String includestatus = "Y";
            for (int j = 0; j < sectionitems.size(); ++j) {
                boolean included;
                boolean excludeFromIncludeRollup = sectionitems.getValue(j, "option_excludeitemfromrollup").equals("Y");
                String availabilitystatus = sectionitems.getValue(j, "availabilitystatus", "OK");
                boolean bl = included = availabilitystatus.equals("OK") || availabilitystatus.equals("Unavailable") && excludeItemsUnavailable || availabilitystatus.equals("NoData") && excludeItemsNoData;
                if (included && !excludeFromIncludeRollup) {
                    ++includeCount;
                }
                if (included && excludeFromIncludeRollup) {
                    includestatus = "C";
                }
                sectionitems.setString(j, "include", included ? "Y" : "N");
            }
            if (sectionitems.size() > 0 && includeCount == 0 && excludeSectionsNoIncludes && !includestatus.equals("C")) {
                includestatus = "N";
            }
            sections.setString(i, "include", includestatus);
            if (sections.getValue(i, "include").equals("Y")) continue;
            hasExcludes = true;
        }
        if (hasExcludes) {
            for (i = 1; i < sections.size(); ++i) {
                int currentlevel;
                if (sections.getValue(i, "include").equals("Y")) continue;
                if (!this.hasIncludedSubSections(sections, i) && sections.getValue(i, "include").equals("N")) {
                    this.hideSection(sections, i, items);
                    currentlevel = Integer.parseInt(sections.getValue(i, "sectionlevel", "1"));
                    while (i < sections.size() - 1 && Integer.parseInt(sections.getValue(i + 1, "sectionlevel", "1")) > currentlevel) {
                        this.hideSection(sections, ++i, items);
                    }
                    continue;
                }
                if (sections.getValue(i, "include").equals("N")) {
                    sections.setValue(i, "include", "Y");
                }
                currentlevel = Integer.parseInt(sections.getValue(i, "sectionlevel", "1"));
                while (i < sections.size() - 1 && Integer.parseInt(sections.getValue(i + 1, "sectionlevel", "1")) > currentlevel) {
                    if (!sections.getValue(++i, "include").equals("N")) continue;
                    this.hideSection(sections, i, items);
                }
            }
        }
        if (Configuration.isDevmode(this.sapphireConnection.getDatabaseId())) {
            for (i = 1; i < sections.size(); ++i) {
                this.logger.info(sections.getValue(i, "sectionlevel", "1") + "/" + sections.getValue(i, "include") + " - " + sections.getValue(i, "worksheetsectionid") + " - " + sections.getValue(i, "worksheetsectiondesc"));
                filter.put("worksheetsectionid", sections.getValue(i, "worksheetsectionid"));
                filter.put("worksheetsectionversionid", sections.getValue(i, "worksheetsectionversionid"));
                DataSet sectionitems = items.getFilteredDataSet(filter);
                for (int j = 0; j < sectionitems.size(); ++j) {
                    this.logger.info(sectionitems.getValue(j, "include") + " - " + sectionitems.getValue(j, "worksheetitemid") + " - " + sectionitems.getValue(j, "propertytreeid"));
                }
            }
        }
    }
}

