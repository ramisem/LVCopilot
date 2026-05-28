/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.file.DocumentFileParsingOptions;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.webservices.client.ActionBlockTransport;
import com.labvantage.sapphire.webservices.client.DataSetTransport;
import com.labvantage.sapphire.webservices.client.PropertyListTransport;
import com.labvantage.sapphire.webservices.client.SDIDataTransport;
import com.labvantage.sapphire.webservices.client.SDIRequestTransport;
import com.labvantage.sapphire.webservices.client.SapphireWSServiceLocator;
import com.labvantage.sapphire.webservices.client.SapphireWS_PortType;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class TalendUtil {
    public static final String PROPERTYPREFIX = "property_";
    private SapphireWS_PortType endpoint = null;
    private Properties context = null;
    private Map<String, Object> globmap = null;
    private static final String GLOBALVAR = "$talendUtil";
    private URL url = null;
    private boolean isExternal = false;
    private String connectionId = "";
    private String tokenid = "";
    private String jobname = "";
    private boolean debugMode = false;

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public TalendUtil(Properties context, Map<String, Object> globmap) {
        this.context = context;
        this.globmap = globmap;
    }

    public boolean isInLV() {
        return !this.isExternal;
    }

    public static TalendUtil getTalendUtil(Properties context, Map<String, Object> globmap, String cid, boolean allowsExternalOperation) {
        return TalendUtil.getTalendUtil("", context, globmap, cid, allowsExternalOperation);
    }

    public void logInfo(String msg) {
        System.out.println("" + msg);
    }

    public void logError(String msg) {
        System.out.println("ERROR: " + msg);
    }

    public void logDebug(String msg) {
        if (this.debugMode) {
            System.out.println("DEBUG: " + msg);
        }
    }

    public static TalendUtil getTalendUtil(String jobname, Properties context, Map<String, Object> globmap, String cid, boolean allowsExternalOperation) {
        TalendUtil talendUtil = null;
        if (globmap.containsKey(GLOBALVAR) && globmap.get(GLOBALVAR) instanceof TalendUtil) {
            talendUtil = (TalendUtil)globmap.get(GLOBALVAR);
            if (cid != null && cid.length() > 0) {
                talendUtil.logDebug("Talend Util Fetched For: " + cid);
            }
        } else {
            block45: {
                talendUtil = new TalendUtil(context, globmap);
                talendUtil.jobname = jobname;
                if (context.containsKey("lvdebug") && context.getProperty("lvdebug").equalsIgnoreCase("Y")) {
                    talendUtil.debugMode = true;
                }
                globmap.put(GLOBALVAR, talendUtil);
                if (talendUtil.jobname.length() > 0) {
                    talendUtil.logInfo("Job '" + talendUtil.jobname + "' Started");
                } else {
                    talendUtil.logInfo("Job Started");
                }
                talendUtil.connectionId = context.getProperty("connectionid") == null ? "" : context.getProperty("connectionid");
                boolean bl = talendUtil.isExternal = talendUtil.connectionId == null || talendUtil.connectionId.length() == 0;
                if (talendUtil.isInLV()) {
                    try {
                        Configuration c = Configuration.getInstance();
                        if (c != null) {
                            talendUtil.logDebug("Job running inside of LabVantage (server " + c.getServerHostName() + ").");
                            break block45;
                        }
                        talendUtil.logDebug("Job running inside of LabVantage (could not determine server).");
                    }
                    catch (Throwable e) {
                        talendUtil.logDebug("Job running inside of LabVantage (unable to determine server).");
                    }
                } else {
                    talendUtil.debugMode = true;
                    talendUtil.logDebug("Job running outside of LabVantage.");
                }
            }
            talendUtil.logDebug("SapphireJar Build:" + Build.getBuild());
            if (cid != null && cid.length() > 0) {
                talendUtil.logDebug("Talend Util Fetched For: " + cid);
            }
            if (context.containsKey("fileName1") && context.getProperty("fileName1") != null) {
                Path p = Paths.get(context.getProperty("fileName1"), new String[0]);
                talendUtil.logDebug("File passed in as 'fileName1': '" + p.toString() + "' (" + (Files.exists(p, new LinkOption[0]) ? "exists" : "does not exist") + ")");
            }
            String string = talendUtil.tokenid = context.getProperty("devtoken") == null ? "" : context.getProperty("devtoken");
            if (talendUtil.connectionId.length() > 0) {
                talendUtil.logDebug("Labvantage Connection: " + talendUtil.connectionId);
            } else if (talendUtil.tokenid.length() > 0) {
                DataSet variables;
                String finvar;
                String attachmentHandlerId;
                block46: {
                    talendUtil.logDebug("Labvantage Development Mode Token: " + talendUtil.tokenid);
                    attachmentHandlerId = context.getProperty("attachmenthandlerid");
                    if (attachmentHandlerId != null && attachmentHandlerId.length() > 0) {
                        DataSet variables2 = talendUtil.getSQLDataSet("SELECT propertyclob FROM attachmenthandler WHERE attachmenthandlerid='" + attachmentHandlerId + "'");
                        if (variables2 != null && variables2.getRowCount() > 0) {
                            try {
                                JSONObject properties = new JSONObject(variables2.getClob(0, "propertyclob", "{}"));
                                if (!properties.has("variables")) break block46;
                                JSONArray jay = properties.getJSONArray("variables");
                                if (jay != null && jay.length() > 0) {
                                    for (int i = 0; i < jay.length(); ++i) {
                                        JSONObject var = jay.getJSONObject(i);
                                        if (!var.has("variableid")) continue;
                                        String variableid = var.optString("variableid");
                                        String value = var.optString("value");
                                        finvar = PROPERTYPREFIX + variableid;
                                        context.setProperty(finvar, value);
                                        talendUtil.logDebug("9Context Variable From Attachment Handler: " + finvar + " = " + value);
                                    }
                                    break block46;
                                }
                                talendUtil.logDebug("No attachment handler variables could be found.");
                            }
                            catch (Exception e) {
                                talendUtil.logDebug("Warning - Failed to obtain attachment handler variables.");
                            }
                        } else {
                            talendUtil.logDebug("No attachment handler variables could be obtained.");
                        }
                    } else {
                        talendUtil.logDebug("Warning - no attachment handler provided so no set up variables will be loaded.");
                    }
                }
                String sdcid = context.getProperty("sdcid");
                String keyid1 = context.getProperty("keyid1");
                if (sdcid != null && sdcid.length() > 0 && keyid1 != null && keyid1.length() > 0) {
                    DataSet metadata;
                    block47: {
                        if (attachmentHandlerId != null && attachmentHandlerId.length() > 0) {
                            DataSet variables3;
                            String sql = "SELECT propertyclob FROM sdiattachmentoperation WHERE sdcid='" + sdcid + "' AND keyid1='" + keyid1 + "'";
                            if (context.containsKey("keyid2") && context.getProperty("keyid2").length() > 0) {
                                sql = sql + " AND keyid2='" + context.getProperty("keyid2") + "'";
                                if (context.containsKey("keyid3") && context.getProperty("keyid3").length() > 0) {
                                    sql = sql + " AND keyid3='" + context.getProperty("keyid3") + "'";
                                }
                            }
                            if ((variables3 = talendUtil.getSQLDataSet(sql = sql + " AND operationsdcid='LV_AttachmentHandler' AND operationkeyid1 = '" + attachmentHandlerId + "'")) != null && variables3.getRowCount() > 0) {
                                try {
                                    JSONObject properties = new JSONObject(variables3.getClob(0, "propertyclob", "{}"));
                                    if (!properties.has("variables")) break block47;
                                    JSONArray jay = properties.getJSONArray("variables");
                                    if (jay != null && jay.length() > 0) {
                                        for (int i = 0; i < jay.length(); ++i) {
                                            JSONObject var = jay.getJSONObject(i);
                                            if (!var.has("variableid")) continue;
                                            String variableid = var.optString("variableid");
                                            String value = var.optString("value");
                                            String finvar2 = PROPERTYPREFIX + variableid;
                                            context.setProperty(finvar2, value);
                                            talendUtil.logDebug("Context Variable From Operation: " + finvar2 + " = " + value);
                                        }
                                        break block47;
                                    }
                                    talendUtil.logDebug("No operation variables could be found.");
                                }
                                catch (Exception e) {
                                    talendUtil.logDebug("Warning - Failed to obtain operation variables.");
                                }
                            } else {
                                talendUtil.logDebug("No operation variables could be obtained.");
                            }
                        } else {
                            talendUtil.logDebug("Warning - no attachment handler provided so operation variables could not be obtained.");
                        }
                    }
                    String msql = "SELECT attributeid, textvalue FROM SDIATTRIBUTE WHERE sdcid='" + sdcid + "' AND keyid1='" + keyid1 + "'";
                    if (context.containsKey("keyid2") && context.getProperty("keyid2").length() > 0) {
                        msql = msql + " AND keyid2='" + context.getProperty("keyid2") + "'";
                        if (context.containsKey("keyid3") && context.getProperty("keyid3").length() > 0) {
                            msql = msql + " AND keyid3='" + context.getProperty("keyid3") + "'";
                        }
                    }
                    if ((metadata = talendUtil.getSQLDataSet(msql)) != null && metadata.getRowCount() > 0) {
                        for (int i = 0; i < metadata.getRowCount(); ++i) {
                            String attributeid = metadata.getValue(i, "attributeid", "");
                            String value = metadata.getValue(i, "textvalue", "");
                            finvar = "metadata_" + attributeid;
                            context.setProperty(finvar, value);
                            talendUtil.logDebug("Context Variable From Meta Data: " + finvar + " = " + value);
                        }
                    } else {
                        talendUtil.logDebug("No meta data could be obtained.");
                    }
                } else {
                    talendUtil.logDebug("Warning - No sdcid or keyid provided so will not load up overriden variables or meta data.");
                }
                String reportid = context.getProperty("reportid");
                String reportversionid = context.getProperty("reportversionid");
                if (reportid != null && reportid.length() > 0 && reportversionid != null && reportversionid.length() > 0 && (variables = talendUtil.getSQLDataSet("SELECT paramid, paramvalue FROM reportparam WHERE reportid='" + reportid + "' AND reportversionid='" + reportversionid + "'")) != null && variables.getRowCount() > 0) {
                    for (int i = 0; i < variables.getRowCount(); ++i) {
                        String paramid = variables.getValue(i, "paramid", "");
                        if (paramid.length() <= 0) continue;
                        String paramvalue = variables.getValue(i, "paramvalue", "");
                        String finvar3 = PROPERTYPREFIX + paramid;
                        context.setProperty(finvar3, paramvalue);
                        talendUtil.logDebug("Context Variable From Report: " + finvar3 + " = " + paramvalue);
                    }
                }
            } else {
                talendUtil.logDebug("Warning - no devtoken provided and in external operation mode.");
            }
        }
        if (cid != null && cid.length() > 0 && talendUtil.isExternal && !allowsExternalOperation) {
            talendUtil.logInfo("Talend Process " + cid + " does not allow processing outside Labvantage. Only limited processing will be done and debug information will be seen.");
        }
        return talendUtil;
    }

    private SapphireWS_PortType getEndpoint() {
        if (this.endpoint == null) {
            if (this.url == null) {
                try {
                    String lvendpoint = this.context.getProperty("lvendpoint");
                    if (lvendpoint != null && lvendpoint.length() > 0) {
                        this.url = new URL(lvendpoint + (lvendpoint.endsWith("/") ? "" : "/") + "services/SapphireWS?wsdl");
                    } else {
                        this.logDebug("No end point provided for development mode");
                    }
                }
                catch (Exception e) {
                    this.logDebug("Unable to set URL");
                }
            }
            if (this.url != null) {
                try {
                    SapphireWSServiceLocator serviceLocator = new SapphireWSServiceLocator();
                    this.endpoint = serviceLocator.getSapphireWS(this.url);
                }
                catch (Exception e) {
                    this.logDebug("Unable to set end point");
                }
            }
        }
        return this.endpoint;
    }

    public static String getVariable(String name, String defaultValue, Properties context, Map<String, Object> globalmap) {
        TalendUtil talendUtil = TalendUtil.getTalendUtil(context, globalmap, null, true);
        return talendUtil.getVariable(name, defaultValue);
    }

    public static String getVariable(String name, String defaultValue, Properties context, Map<String, Object> globalmap, boolean ignoreCase) {
        TalendUtil talendUtil = TalendUtil.getTalendUtil(context, globalmap, null, true);
        return talendUtil.getVariable(name, defaultValue, ignoreCase);
    }

    public String getVariable(String name, String defaultValue) {
        String v;
        String string = v = defaultValue == null ? "" : defaultValue;
        if (name.startsWith(PROPERTYPREFIX)) {
            if (this.context.containsKey(name)) {
                v = this.context.getProperty(name, v);
            }
        } else if (this.context.containsKey(PROPERTYPREFIX + name)) {
            v = this.context.getProperty(PROPERTYPREFIX + name);
        }
        return v;
    }

    public String getVariable(String name, String defaultValue, boolean ignoreCase) {
        String v;
        String string = v = defaultValue == null ? "" : defaultValue;
        if (!ignoreCase) {
            v = this.getVariable(name, defaultValue);
        } else {
            String k = name.toLowerCase().startsWith(PROPERTYPREFIX) ? name : PROPERTYPREFIX + name;
            Iterator<Object> it = this.context.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                if (!key.equalsIgnoreCase(defaultValue)) continue;
                v = this.context.getProperty(key, v);
            }
        }
        return v;
    }

    public static void main(String[] args) {
        Properties context = new Properties();
        context.setProperty("devtoken", "wiot-kd7F-?ku]-TIHV-7IIQ-QHUQ-7X7V-EZ");
        context.setProperty("lvendpoint", "https://lt6491eu.lims.com:8443/labvantage");
        context.setProperty("attachmenthandlerid", "ParsePDF");
        context.setProperty("sdcid", "LV_DataCapture");
        context.setProperty("keyid1", "DC-022020-000016");
        HashMap<String, Object> g = new HashMap<String, Object>();
        TalendUtil talendUtil = TalendUtil.getTalendUtil(context, g, "FAKE", true);
        DataSet ds = talendUtil.getSQLDataSet("SELECT count(*) FROM s_sample");
        DataSet ds2 = talendUtil.getSDIData("Units", "units", "", "primary");
        talendUtil.logDebug("Dataset 1 : " + ds.getRowCount());
        talendUtil.logDebug("Dataset 2 : " + ds2.getRowCount());
    }

    public List<DataSet> parseDataSetsFromDocumentFile(Path fileToParse, DocumentFileParsingOptions options) {
        List<DataSet> out = new ArrayList<DataSet>();
        if (this.connectionId.length() > 0) {
            this.logDebug("About to parse document: " + fileToParse.toString());
            try {
                out = FileManager.parseDataSetsFromDocumentFile(fileToParse, options, this.connectionId);
            }
            catch (Exception e) {
                this.logError("Failed to parse document.");
            }
        } else {
            this.logInfo("Parsing document files can only be executed in LabVantage. Empty list will be returned.");
        }
        return out;
    }

    public DataSet getSQLDataSet(String query) {
        DataSet ds;
        block11: {
            ds = null;
            if (this.connectionId.length() > 0) {
                QueryProcessor qp = new QueryProcessor(this.connectionId);
                this.logDebug("About to execute query: " + query);
                try {
                    ds = qp.getSqlDataSet(query);
                }
                catch (Exception e) {
                    this.logError("Failed to run query. " + e.getMessage());
                }
            } else if (this.tokenid.length() > 0) {
                try {
                    SapphireWS_PortType lvendpoint = this.getEndpoint();
                    if (lvendpoint != null) {
                        DataSetTransport dataSetTransport = lvendpoint.getSqlDataSet(this.tokenid, query, true);
                        ds = dataSetTransport.toDataSet();
                        break block11;
                    }
                    this.logError("No end point provided for development mode");
                }
                catch (Exception e) {
                    this.logError("Could not call webservice. " + e.getMessage());
                }
            } else {
                this.logError("No connection id or authentication token provided for LV");
            }
        }
        if (ds != null) {
            this.logDebug("Query succeeded. Rows: " + ds.getRowCount());
        } else {
            ds = new DataSet();
            this.logError("Query failed. No data returned.");
        }
        return ds;
    }

    public PropertyList processAction(String actionid, String actionversion, PropertyList propertyList) {
        PropertyList out;
        block10: {
            this.logDebug("Executing action syncronously in talend");
            out = new PropertyList();
            if (this.connectionId.length() > 0) {
                ActionProcessor actionProcessor = new ActionProcessor(this.connectionId);
                try {
                    actionProcessor.processAction(actionid, actionversion, propertyList);
                }
                catch (Exception e) {
                    this.logError("Failed to process action. " + e.getMessage());
                }
            } else if (this.tokenid.length() > 0) {
                try {
                    SapphireWS_PortType lvendpoint = this.getEndpoint();
                    if (lvendpoint != null) {
                        PropertyListTransport propertyListTransport = new PropertyListTransport();
                        propertyListTransport.setPropertyList(propertyList);
                        PropertyListTransport outPropertyListTransport = lvendpoint.processAction(this.tokenid, actionid, actionversion, propertyListTransport);
                        PropertyList outPropertyList = outPropertyListTransport.getPropertyList();
                        for (String k : outPropertyList.keySet()) {
                            if (!(outPropertyList.get(k) instanceof String)) continue;
                            propertyList.setProperty(k, outPropertyList.getProperty(k, ""));
                        }
                        break block10;
                    }
                    this.logError("No end point provided for development mode");
                }
                catch (Exception e) {
                    this.logError("Could not call webservice. " + e.getMessage());
                }
            } else {
                this.logError("No connection id or authentication token provided for LV");
            }
        }
        return out;
    }

    public PropertyList processActionBlock(ActionBlock actionBlock, String actionId) {
        PropertyList out;
        block9: {
            this.logDebug("Executing block syncronously in talend");
            out = new PropertyList();
            if (this.connectionId.length() > 0) {
                ActionProcessor actionProcessor = new ActionProcessor(this.connectionId);
                try {
                    actionProcessor.processActionBlock(actionBlock, true);
                    out = actionBlock.getAction((String)actionId).properties;
                }
                catch (Exception e) {
                    this.logError("Failed to process action. " + e.getMessage());
                }
            } else if (this.tokenid.length() > 0) {
                try {
                    SapphireWS_PortType lvendpoint = this.getEndpoint();
                    if (lvendpoint != null) {
                        ActionBlockTransport actionBlockTransport = new ActionBlockTransport();
                        actionBlockTransport.setActionBlock(actionBlock);
                        ActionBlockTransport outActionBlockTransport = lvendpoint.processActionBlock(this.tokenid, actionBlockTransport);
                        ActionBlock outActionBlock = outActionBlockTransport.getActionBlock();
                        out = outActionBlock.getAction((String)actionId).properties;
                        break block9;
                    }
                    this.logError("No end point provided for development mode");
                }
                catch (Exception e) {
                    this.logError("Could not call webservice. " + e.getMessage());
                }
            } else {
                this.logError("No connection id or authentication token provided for LV");
            }
        }
        return out;
    }

    public DataSet getSDIData(String sdcid, String queryfrom, String querywhere, String dataset) {
        DataSet ds;
        block16: {
            ds = null;
            if (this.connectionId.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRequestItem("primary");
                if (!dataset.equalsIgnoreCase("primary")) {
                    sdiRequest.setRequestItem(dataset);
                }
                if (queryfrom.length() > 0) {
                    sdiRequest.setQueryFrom(queryfrom);
                } else {
                    SDCProcessor sdcProcessor = new SDCProcessor(this.connectionId);
                    sdiRequest.setQueryFrom(sdcProcessor.getProperty(sdcid, "tableid"));
                }
                if (querywhere.length() > 0) {
                    sdiRequest.setQueryWhere(querywhere);
                }
                this.logDebug("About to execute sdi request");
                SDIProcessor sdiProcessor = new SDIProcessor(this.connectionId);
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData != null && sdiData.getDataset(dataset) != null) {
                    ds = sdiData.getDataset(dataset);
                }
            } else if (this.tokenid.length() > 0) {
                try {
                    SapphireWS_PortType lvendpoint = this.getEndpoint();
                    if (lvendpoint != null) {
                        SDIRequestTransport sdiRequestTransport = new SDIRequestTransport();
                        sdiRequestTransport.setSdcid(sdcid);
                        String[] sdirequestitems = !dataset.equalsIgnoreCase("primary") ? new String[]{"primary", dataset} : new String[]{"primary"};
                        sdiRequestTransport.setSdirequestitems(sdirequestitems);
                        if (queryfrom.length() > 0) {
                            sdiRequestTransport.setQueryfrom(queryfrom);
                        }
                        if (querywhere.length() > 0) {
                            sdiRequestTransport.setQuerywhere(querywhere);
                        }
                        SDIDataTransport sdiDataTransport = lvendpoint.getSDIData(this.tokenid, sdiRequestTransport);
                        ds = sdiDataTransport.getDataSet(dataset);
                        break block16;
                    }
                    this.logError("No end point provided for development mode");
                }
                catch (Exception e) {
                    this.logError("Could not call webservice. " + e.getMessage());
                }
            } else {
                this.logError("No connection id or authentication token provided for LV");
            }
        }
        if (ds != null) {
            this.logDebug("SDI Request succeeded. Rows: " + ds.getRowCount());
        } else {
            ds = new DataSet();
            this.logError("SDI Request failed. No data returned.");
        }
        return ds;
    }

    public void finish(String contextName, String cid) {
        if (contextName != null && contextName.length() > 0 && (this.debugMode || this.isExternal)) {
            StringBuilder debugout = new StringBuilder();
            Iterator<Object> iterator = this.context.keySet().iterator();
            while (iterator.hasNext()) {
                String k = iterator.next().toString();
                if (!k.startsWith(contextName) || !k.endsWith(cid)) continue;
                String v = this.context.getProperty(k) != null ? this.context.getProperty(k) : "";
                debugout.append(k).append(" = ").append(v).append("\n");
            }
            if (debugout.length() > 0) {
                System.out.println("DEBUG: CONTEXT FOR " + cid + ":");
                System.out.println(debugout.toString());
                System.out.println("----------------------");
            }
        }
        this.logDebug("Talend Util Finished For: " + cid);
    }
}

