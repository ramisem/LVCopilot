/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdi.AddSDINote;
import com.labvantage.sapphire.actions.sdi.DeleteSDINote;
import com.labvantage.sapphire.actions.sdi.EditSDINote;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.gwt.server.command.BaseCommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StandardCommandRequest
extends BaseCommandRequest {
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        if (command.equalsIgnoreCase("loadsdimaint")) {
            return this.loadSDIMaintSet(commandRequest, commandResponse);
        }
        if (command.equalsIgnoreCase("savesdimaint")) {
            ActionService actionService = new ActionService(this.sapphireConnection);
            AuditService auditService = new AuditService(this.sapphireConnection);
            ErrorHandler errorHandler = new ErrorHandler();
            try {
                for (String name : commandRequest.keySet()) {
                    Object value = commandRequest.get(name);
                    if (!(value instanceof SDIMaint)) continue;
                    SDIMaint sdiMaint = (SDIMaint)value;
                    this.logInfo("Saving SDIMaint for " + new SDI(sdiMaint.getSdcid(), sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), sdiMaint.getKeyid3()).toString());
                    sdiMaint.save(actionService, auditService, errorHandler, this);
                    commandResponse.set(name, sdiMaint);
                }
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (ServiceException se) {
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to save SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (command.equalsIgnoreCase("loadeditorstyles")) {
            try {
                commandResponse.set("editorstyles", this.getEditorStyles(commandRequest.getString("editorstyleid")));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load editor style data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (command.equalsIgnoreCase("loadquerydefs")) {
            try {
                commandResponse.set("querydefinitions", this.getQueryDefinitions(commandRequest.getString("queryid"), commandRequest.getString("basedonid")));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load query param data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (command.equalsIgnoreCase("checkuser")) {
            try {
                ConnectionProcessor cp = this.getConnectionProcessor();
                String password = commandRequest.getString("password");
                if (password != null && password.indexOf("{|}") == 0) {
                    password = EncryptDecrypt.decryptRSA(password.substring("{|}".length()));
                }
                if (cp.checkUser(commandRequest.getString("sysuserid"), password)) {
                    commandResponse.setStatus("ok");
                }
                commandResponse.setStatusFail("Unrecognized user/password combination!");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to check user. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        } else if (command.equalsIgnoreCase("checkkeyid")) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                Object[] objectArray;
                String sdcid = commandRequest.getString("sdcid");
                String keyid1 = commandRequest.getString("keyid1");
                String keyid2 = commandRequest.getString("keyid2");
                String keyid3 = commandRequest.getString("keyid3");
                SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
                String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                dbu.setConnection(this.sapphireConnection);
                String string = "SELECT " + sdcProcessor.getProperty(sdcid, "keycolid1") + (keycols > 1 ? "," + sdcProcessor.getProperty(sdcid, "keycolid2") : "") + (keycols > 2 ? "," + sdcProcessor.getProperty(sdcid, "keycolid3") : "") + " FROM " + tableid + " WHERE " + sdcProcessor.getProperty(sdcid, "keycolid1") + "=?" + (keycols > 1 ? " AND " + sdcProcessor.getProperty(sdcid, "keycolid2") + "=?" : "") + (keycols > 2 ? " AND " + sdcProcessor.getProperty(sdcid, "keycolid3") + "=?" : "");
                if (keycols == 1) {
                    Object[] objectArray2 = new Object[1];
                    objectArray = objectArray2;
                    objectArray2[0] = keyid1;
                } else if (keycols == 2) {
                    Object[] objectArray3 = new Object[2];
                    objectArray3[0] = keyid1;
                    objectArray = objectArray3;
                    objectArray3[1] = keyid2;
                } else {
                    Object[] objectArray4 = new Object[3];
                    objectArray4[0] = keyid1;
                    objectArray4[1] = keyid2;
                    objectArray = objectArray4;
                    objectArray4[2] = keyid3;
                }
                dbu.createPreparedResultSet(string, objectArray);
                if (dbu.getNext()) {
                    commandResponse.set("exists", "Y");
                }
                commandResponse.set("exists", "N");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to check keyid. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
            finally {
                dbu.releaseConnection();
            }
        } else if (command.equalsIgnoreCase("checkaliasid")) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                String sdcid = commandRequest.getString("sdcid");
                String aliasid = commandRequest.getString("aliasid");
                String aliastype = commandRequest.getString("aliastype");
                dbu.setConnection(this.sapphireConnection);
                dbu.createPreparedResultSet("SELECT keyid1, keyid2, keyid3 FROM sdialias WHERE sdcid = ? AND aliasid = ?" + (aliastype.length() > 0 ? " AND aliastype = '" + aliastype + "'" : ""), new Object[]{sdcid, aliasid});
                if (dbu.getNext()) {
                    commandResponse.set("exists", "Y");
                    commandResponse.set("keyid1", dbu.getValue("keyid1"));
                    commandResponse.set("keyid2", dbu.getValue("keyid2"));
                    commandResponse.set("keyid3", dbu.getValue("keyid3"));
                }
                commandResponse.set("exists", "N");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to check aliasid. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
            finally {
                dbu.releaseConnection();
            }
        } else if (command.equalsIgnoreCase("checkalternateid")) {
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                String sdcid = commandRequest.getString("sdcid");
                String columnid = commandRequest.getString("columnid");
                String value = commandRequest.getString("value");
                dbu.setConnection(this.sapphireConnection);
                String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
                String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                dbu.createPreparedResultSet("SELECT " + keycolid1 + " FROM " + tableid + " WHERE " + columnid + " = ?", new Object[]{value});
                if (dbu.getNext()) {
                    commandResponse.set("exists", "Y");
                    StringBuffer keyid1 = new StringBuffer(";" + dbu.getValue(keycolid1));
                    while (dbu.getNext()) {
                        keyid1.append(";").append(dbu.getValue(keycolid1));
                    }
                    commandResponse.set("keyid1", keyid1.substring(1));
                }
                commandResponse.set("exists", "N");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to check aliasid. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
            finally {
                dbu.releaseConnection();
            }
        } else {
            if (command.equalsIgnoreCase("loaddataset")) {
                try {
                    QueryProcessor queryProcessor = this.getQueryProcessor();
                    int sqlcode = Integer.parseInt(commandRequest.getString("sqlcode"));
                    DataSet dataset = null;
                    dataset = commandRequest.contains("bindvars") ? queryProcessor.getPreparedSqlDataSet(sqlcode, (Object[])StringUtil.split(commandRequest.getString("bindvars"), ";"), true) : queryProcessor.getSqlDataSet(sqlcode);
                    commandResponse.set(commandRequest.getString("name", "dataset"), dataset);
                    commandResponse.setStatus("ok");
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load dataset. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equalsIgnoreCase("processaction")) {
                try {
                    ActionProcessor actionProcessor = this.getActionProcessor();
                    String actionid = commandRequest.getString("actionid");
                    String actionversionid = commandRequest.getString("actionversionid", "1");
                    HashMap<String, String> actionProps = new HashMap<String, String>();
                    for (String name : commandRequest.keySet()) {
                        if (name.equals("actionid") || name.equals("actionversionid") || name.endsWith("_type")) continue;
                        actionProps.put(name, commandRequest.getString(name));
                    }
                    actionProcessor.processAction(actionid, actionversionid, actionProps);
                    commandResponse.setStatus("ok");
                }
                catch (ActionException ae) {
                    ErrorHandler errorHandler = ae.getErrorHandler();
                    commandResponse.setStatusFail(errorHandler.getLastErrorMessage());
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to process action. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            } else if (command.equalsIgnoreCase("generatetracelog")) {
                commandResponse.set("tracelogid", this.getTracelogid(commandRequest.getString("auditreason"), commandRequest.getString("auditactivity"), commandRequest.getString("auditsigned", "N"), commandRequest.getString("auditdt", "now"), commandRequest.getBoolean("standard"), commandRequest.getString("tracelogdesc")));
            } else {
                if (command.equalsIgnoreCase("createrset")) {
                    try {
                        SDIList sdiList = commandRequest.getSDIList("sdilist");
                        DataAccessService dataAccessService = new DataAccessService(this.sapphireConnection);
                        RSet rset = dataAccessService.createRSet(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(), sdiList.getKeyid3());
                        commandResponse.set("rsetid", rset.getRsetid());
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to create rset. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equalsIgnoreCase("clearrset")) {
                    try {
                        String rsetid = commandRequest.getString("rsetid");
                        DataAccessService dataAccessService = new DataAccessService(this.sapphireConnection);
                        dataAccessService.clearRSet(new RSet(rsetid));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to clear rset. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equalsIgnoreCase("loadpolicy")) {
                    String policyid = commandRequest.getString("policyid");
                    String policynode = commandRequest.getString("policynode", "Sapphire Custom");
                    String propertyid = commandRequest.getString("propertyid");
                    try {
                        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
                        PropertyList policy = configurationProcessor.getPolicy(policyid, policynode);
                        if (propertyid.length() > 0) {
                            if (policy.isPropertyList(propertyid)) {
                                commandResponse.set(policyid, policy.getPropertyList(propertyid));
                            } else {
                                commandResponse.set(policyid, policy.getProperty(propertyid));
                            }
                        } else {
                            commandResponse.set(policyid, policy);
                        }
                        commandResponse.setStatus("ok");
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load policy '" + policyid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equalsIgnoreCase("initnotes")) {
                    PropertyList elementconfig;
                    String policyid = commandRequest.getString("policyid");
                    String propertyid = commandRequest.getString("propertyid");
                    String policynode = commandRequest.getString("policynode", "Sapphire Custom");
                    PropertyList configuration = commandRequest.getPropertyList("configuration");
                    try {
                        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
                        PropertyList policy = configurationProcessor.getPolicy(policyid, policynode);
                        commandResponse.set(policyid, policy);
                        commandResponse.setStatus("ok");
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load policy '" + policyid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                    boolean isOverrideConfig = commandRequest.getBoolean("isoverrideconfig");
                    String sdcid = commandRequest.getString("sdcid", "");
                    if (isOverrideConfig && !sdcid.isEmpty()) {
                        try {
                            ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
                            elementconfig = configurationProcessor.findPolicy("sdinotes", "sdcid", sdcid);
                            if (elementconfig == null || elementconfig.size() == 0) {
                                elementconfig = configurationProcessor.getPolicy("sdinotes", policynode);
                            }
                            elementconfig.putAll(configuration);
                        }
                        catch (Exception e) {
                            throw new SapphireException("Failed to load policy '" + policyid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                        }
                    } else {
                        elementconfig = configuration;
                    }
                    commandResponse.set("configuration", elementconfig);
                    String value = "";
                    StringBuilder html = new StringBuilder();
                    String name = "noteeditor";
                    HTMLEditorControl editor = new HTMLEditorControl();
                    editor.setId(name);
                    editor.setDebug(true);
                    editor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
                    editor.setInline(true);
                    editor.getEditor().setUseFullIncludes(false);
                    editor.setContent(value);
                    ArrayList<String> includes = HTMLEditorControl.getScriptIncludes(false);
                    PropertyList incl = new PropertyList();
                    PropertyListCollection incld = new PropertyListCollection();
                    incl.setProperty("includes", incld);
                    for (String include : includes) {
                        PropertyList inc = new PropertyList();
                        incld.add(inc);
                        inc.setProperty("href", include);
                    }
                    String initScript = editor.getInitScript();
                    commandResponse.set("richtexthtml", editor.getHtml());
                    commandResponse.set("script", editor.getScript());
                    commandResponse.set("initscript", initScript);
                    commandResponse.set("includes", incl);
                    HTMLEditorControl popupEditor = new HTMLEditorControl();
                    popupEditor.setId("noteeditor_popup");
                    popupEditor.setDebug(true);
                    popupEditor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
                    popupEditor.setInline(false);
                    popupEditor.setWidth("800");
                    popupEditor.setHeight("600");
                    popupEditor.setContent(value);
                    commandResponse.set("richtexthtml_popup", popupEditor.getHtml());
                    commandResponse.set("initscript_popup", popupEditor.getInitScript());
                    commandResponse.set("script_popup", popupEditor.getScript());
                } else if (command.equalsIgnoreCase("loadpolicynodes")) {
                    String policyid = commandRequest.getString("policyid");
                    String propertyid = commandRequest.getString("propertyid");
                    HashMap<String, String> namedNodes = new HashMap<String, String>();
                    try {
                        WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.sapphireConnection.getConnectionId());
                        PropertyTree tree = webAdminProcessor.getPropertyTree(policyid);
                        StringBuffer nodeids = new StringBuffer();
                        StringBuffer names = new StringBuffer();
                        ArrayList nodes = tree.getAllNodes();
                        for (Node node : nodes) {
                            String name = tree.getNodePropertyList(node.getNodeId(), true).getProperty(propertyid);
                            if (name.length() <= 0) continue;
                            namedNodes.put(name, node.getNodeId());
                        }
                        for (String name : namedNodes.keySet()) {
                            names.append(";").append(name);
                            nodeids.append(";").append((String)namedNodes.get(name));
                        }
                        commandResponse.set(propertyid, names.length() > 0 ? names.substring(1) : "");
                        commandResponse.set("nodes", nodeids.length() > 0 ? nodeids.substring(1) : "");
                        commandResponse.setStatus("ok");
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load policy '" + policyid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("loadnotesconfig")) {
                    String sdcid = commandRequest.getString("sdcid", "");
                    String defaultnode = "Sapphire Custom";
                    String elementid = "sdinotes";
                    try {
                        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
                        PropertyList policy = configurationProcessor.findPolicy(elementid, "sdcid", sdcid);
                        if (policy == null || policy.size() == 0) {
                            policy = configurationProcessor.getPolicy(elementid, defaultnode);
                        }
                        commandResponse.set(elementid, policy);
                        commandResponse.setStatus("ok");
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load sdinotes configuration '" + elementid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("loadnote")) {
                    try {
                        String sdcid = commandRequest.getString("sdcid");
                        String keyid1 = commandRequest.getString("keyid1");
                        String keyid2 = commandRequest.getString("keyid2", "(null)");
                        String keyid3 = commandRequest.getString("keyid3", "(null)");
                        int notenum = commandRequest.getString("notenum").length() > 0 ? Integer.parseInt(commandRequest.getString("notenum")) : -1;
                        QueryProcessor queryProcessor = this.getQueryProcessor();
                        commandResponse.set("sdcid", sdcid);
                        commandResponse.set("keyid1", keyid1);
                        commandResponse.set("keyid2", keyid2);
                        commandResponse.set("keyid3", keyid3);
                        PropertyListCollection notetypes = commandRequest.getCollectionNotNull("notetypes");
                        NoteTypeCollection noteTypeCollection = this.getNoteTypeCollection(notetypes, sdcid, keyid1, keyid2, keyid3);
                        commandResponse.set("notetypes", noteTypeCollection.getNoteTypeCollection());
                        if (sdcid.equals("SDIWorkItem") || sdcid.equals("DataSet")) {
                            String oldSdcid = sdcid;
                            DataSet newKeys = queryProcessor.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3" + (oldSdcid.equals("SDIWorkItem") ? ", workitemid, workiteminstance FROM sdiworkitem WHERE sdiworkitemid = ?" : ", paramlistid, paramlistversionid, variantid, dataset FROM sdidata WHERE sdidataid = ?"), new Object[]{keyid1});
                            if (newKeys.size() == 1) {
                                sdcid = newKeys.getValue(0, "sdcid");
                                keyid1 = newKeys.getValue(0, "keyid1");
                                keyid2 = newKeys.getValue(0, "keyid2");
                                keyid3 = newKeys.getValue(0, "keyid3");
                                commandResponse.set("sdcid", sdcid);
                                commandResponse.set("keyid1", keyid1);
                                commandResponse.set("keyid2", keyid2);
                                commandResponse.set("keyid3", keyid3);
                                commandResponse.set("contexttype", oldSdcid.equals("SDIWorkItem") ? "sdiworkitem" : "dataset");
                                commandResponse.set("context", sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + (oldSdcid.equals("SDIWorkItem") ? newKeys.getValue(0, "workitemid") + ";" + newKeys.getValue(0, "workiteminstance") : newKeys.getValue(0, "paramlistid") + ";" + newKeys.getValue(0, "paramlistversionid") + ";" + newKeys.getValue(0, "variantid") + ";" + newKeys.getValue(0, "dataset")));
                            }
                        }
                        commandResponse.set("notes", this.loadNote(sdcid, keyid1, keyid2, keyid3, notenum, commandRequest.getString("sort", "ASC"), commandRequest.getString("groupby"), commandRequest.getCollection("contexts"), noteTypeCollection));
                        commandResponse.set("singular", this.getSDCProcessor().getProperty(sdcid, "singular"));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load notes. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("addnote")) {
                    try {
                        String sdcid = commandRequest.getString("sdcid");
                        String keyid1 = commandRequest.getString("keyid1");
                        String keyid2 = commandRequest.getString("keyid2", "(null)");
                        String keyid3 = commandRequest.getString("keyid3", "(null)");
                        String linksdcid = commandRequest.getString("linksdcid");
                        String linkkeyid1 = commandRequest.getString("linkkeyid1");
                        String linkkeyid2 = commandRequest.getString("linkkeyid2", "(null)");
                        String linkkeyid3 = commandRequest.getString("linkkeyid3", "(null)");
                        PropertyListCollection noteTypesCollection = commandRequest.getCollection("notetypes");
                        NoteTypeCollection noteTypes = this.getNoteTypeCollection(noteTypesCollection, sdcid, keyid1, keyid2, keyid3);
                        commandResponse.set("notetypes", noteTypes.getNoteTypeCollection());
                        PropertyList addProps = new PropertyList();
                        addProps.setProperty("sdcid", sdcid);
                        addProps.setProperty("keyid1", keyid1);
                        addProps.setProperty("keyid2", keyid2);
                        addProps.setProperty("keyid3", keyid3);
                        addProps.setProperty("threadnum", commandRequest.getString("threadnum"));
                        addProps.setProperty("notetype", commandRequest.getString("notetype"));
                        addProps.setProperty("commentnotify", commandRequest.getString("commentnotify"));
                        addProps.setProperty("note", commandRequest.getString("note"));
                        addProps.setProperty("activity", commandRequest.getString("activity"));
                        addProps.setProperty("contexttype", commandRequest.getString("contexttype"));
                        addProps.setProperty("context", commandRequest.getString("context"));
                        addProps.setProperty("linksdcid", linksdcid);
                        addProps.setProperty("linkkeyid1", linkkeyid1);
                        addProps.setProperty("linkkeyid2", linkkeyid2);
                        addProps.setProperty("linkkeyid3", linkkeyid3);
                        addProps.setProperty("followup", commandRequest.getBoolean("followup") ? "Y" : "N");
                        addProps.setProperty("followupuserid", commandRequest.getString("followupuserid"));
                        addProps.setProperty("followupnotifyuser", commandRequest.getBoolean("followupnotifyuser") ? "Y" : "N");
                        addProps.setProperty("followupnotifyowner", commandRequest.getBoolean("followupnotifyowner") ? "Y" : "N");
                        addProps.setProperty("followupsubject", commandRequest.getString("followupsubject"));
                        addProps.setProperty("notesconfig", noteTypes.getNoteTypeCollection());
                        ActionProcessor actionProcessor = this.getActionProcessor();
                        actionProcessor.processActionClass(AddSDINote.class.getName(), addProps);
                        commandResponse.set("notes", this.loadNote(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(addProps.getProperty("notenum")), commandRequest.getString("sort", "ASC"), commandRequest.getString("groupby"), commandRequest.getCollection("contexts"), noteTypes));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to add note. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("editnote")) {
                    try {
                        String sdcid = commandRequest.getString("sdcid");
                        String keyid1 = commandRequest.getString("keyid1");
                        String keyid2 = commandRequest.getString("keyid2", "(null)");
                        String keyid3 = commandRequest.getString("keyid3", "(null)");
                        PropertyListCollection notetypes = commandRequest.getCollection("notetypes");
                        PropertyList editProps = new PropertyList();
                        editProps.setProperty("sdcid", sdcid);
                        editProps.setProperty("keyid1", keyid1);
                        editProps.setProperty("keyid2", keyid2);
                        editProps.setProperty("keyid3", keyid3);
                        editProps.setProperty("notenum", commandRequest.getString("notenum"));
                        editProps.setProperty("notetype", commandRequest.getString("notetype"));
                        if (commandRequest.contains("followupuserid")) {
                            editProps.setProperty("followupuserid", commandRequest.getString("followupuserid"));
                            editProps.setProperty("followupnotifyuser", commandRequest.getBoolean("followupnotifyuser") ? "Y" : "N");
                            editProps.setProperty("followupnotifyowner", commandRequest.getBoolean("followupnotifyowner") ? "Y" : "N");
                            editProps.setProperty("followupsubject", commandRequest.getString("followupsubject"));
                        }
                        if (commandRequest.contains("note")) {
                            editProps.setProperty("note", commandRequest.getString("note"));
                        }
                        if (commandRequest.contains("resolvedflag")) {
                            String resolvedFlag = commandRequest.getString("resolvedflag", "");
                            boolean isResolved = true;
                            if (resolvedFlag.isEmpty() || resolvedFlag.equals("N")) {
                                resolvedFlag = "N";
                                isResolved = false;
                            }
                            editProps.setProperty("resolvedflag", resolvedFlag);
                            if (isResolved) {
                                editProps.setProperty("resolvedby", this.sapphireConnection.getSysuserId());
                                editProps.setProperty("resolveddt", "n");
                                editProps.setProperty("resolvednote", commandRequest.getString("resolvednote"));
                            } else {
                                editProps.setProperty("resolvedby", "(null)");
                                editProps.setProperty("resolveddt", "(null)");
                                editProps.setProperty("resolvednote", commandRequest.getString("resolvednote"));
                            }
                        }
                        if (commandRequest.contains("tagcolumnid")) {
                            editProps.setProperty("tagcolumnid", commandRequest.getString("tagcolumnid"));
                            editProps.setProperty(commandRequest.getString("tagcolumnid"), commandRequest.getString(commandRequest.getString("tagcolumnid")));
                        }
                        NoteTypeCollection noteTypes = this.getNoteTypeCollection(notetypes, sdcid, keyid1, keyid2, keyid3);
                        commandResponse.set("notetypes", noteTypes.getNoteTypeCollection());
                        editProps.setProperty("notesconfig", noteTypes.getNoteTypeCollection());
                        ActionProcessor actionProcessor = this.getActionProcessor();
                        actionProcessor.processActionClass(EditSDINote.class.getName(), editProps);
                        commandResponse.set("notes", this.loadNote(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(commandRequest.getString("notenum")), commandRequest.getString("sort", "ASC"), commandRequest.getString("groupby"), commandRequest.getCollection("contexts"), noteTypes));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to edit note. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("deletenote")) {
                    try {
                        PropertyList deleteProps = new PropertyList();
                        deleteProps.setProperty("sdcid", commandRequest.getString("sdcid"));
                        deleteProps.setProperty("keyid1", commandRequest.getString("keyid1"));
                        deleteProps.setProperty("keyid2", commandRequest.getString("keyid2"));
                        deleteProps.setProperty("keyid3", commandRequest.getString("keyid3"));
                        deleteProps.setProperty("notenum", commandRequest.getString("notenum"));
                        ActionProcessor actionProcessor = this.getActionProcessor();
                        actionProcessor.processActionClass(DeleteSDINote.class.getName(), deleteProps);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to delete note. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                } else if (command.equalsIgnoreCase("setuserconfig")) {
                    commandResponse.set("__hostwebpageid", commandRequest.getString("__hostwebpageid"));
                    commandResponse.set("__hostsysuserid", this.sapphireConnection.getSysuserId());
                    commandResponse.set("setuserconfig", commandRequest.getPropertyList("userconfig"));
                }
            }
        }
        return false;
    }

    private NoteTypeCollection getNoteTypeCollection(PropertyListCollection noteTypes, String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        return new NoteTypeCollection(noteTypes, sdcid, keyid1, keyid2, keyid3);
    }

    protected DataSet loadNote(String sdcid, String keyid1, String keyid2, String keyid3, int notenum, String sort, String groupby, PropertyListCollection contexts, NoteTypeCollection noteTypes) throws SapphireException {
        DataAccessService dataAccessService = new DataAccessService(this.sapphireConnection);
        RSet rset = null;
        try {
            DataSet notes;
            String sql;
            QueryProcessor queryProcessor = this.getQueryProcessor();
            String select = "SELECT sdinote.*, sysuser.sysuserid, sysuser.sysuserdesc, sysuser.initials,        linksdinote.sdcid linksdcid, linksdinote.keyid1 linkkeyid1, linksdinote.keyid2 linkkeyid2, linksdinote.keyid3 linkkeyid3, linksdinote.notenum linknotenum, linksdinote.note linknote, linksdinote.activity linkactivity, linksdinote.sdinoteid linksdinoteid,        (SELECT COUNT(*) FROM sdiattachment WHERE sdiattachment.sdcid = 'SDINote' AND sdiattachment.keyid1 = sdinote.sdinoteid ) attachmentcount,        (SELECT COUNT(*) FROM sdiattachment WHERE sdiattachment.sdcid = 'SDINote' AND sdiattachment.keyid1 = linksdinote.sdinoteid ) linkattachmentcount ";
            String orderby = " ORDER BY sdinote.threadnum " + sort + ", sdinote.ownerdt ASC";
            if (keyid1.contains(";")) {
                rset = dataAccessService.createRSet(sdcid, keyid1, keyid2, keyid3);
                sql = select + "FROM   rsetitems, sdinote LEFT OUTER JOIN sysuser ON sdinote.ownerid = sysuser.sysuserid LEFT OUTER JOIN sdinote linksdinote ON        linksdinote.sdcid = sdinote.linksdcid AND linksdinote.keyid1 = sdinote.linkkeyid1 AND linksdinote.keyid2 = sdinote.linkkeyid2 AND linksdinote.keyid3 = sdinote.linkkeyid3 AND linksdinote.notenum = sdinote.linknotenum WHERE sdinote.sdcid = rsetitems.sdcid AND sdinote.keyid1 = rsetitems.keyid1 AND sdinote.keyid2 = rsetitems.keyid2 AND sdinote.keyid3 = rsetitems.keyid3 AND rsetid = ?" + orderby;
                notes = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{rset.getRsetid()}, true);
            } else {
                sql = select + "FROM  sdinote LEFT OUTER JOIN sysuser ON sdinote.ownerid = sysuser.sysuserid LEFT OUTER JOIN sdinote linksdinote ON        linksdinote.sdcid = sdinote.linksdcid AND linksdinote.keyid1 = sdinote.linkkeyid1 AND linksdinote.keyid2 = sdinote.linkkeyid2 AND linksdinote.keyid3 = sdinote.linkkeyid3 AND linksdinote.notenum = sdinote.linknotenum WHERE sdinote.sdcid = ? AND sdinote.keyid1 = ? AND sdinote.keyid2 = ? AND sdinote.keyid3 = ?" + (notenum != -1 ? " AND sdinote.notenum = ?" : "") + orderby;
                notes = notenum != -1 ? queryProcessor.getPreparedSqlDataSet(sql, new Object[]{sdcid, keyid1, keyid2, keyid3, notenum}, true) : queryProcessor.getPreparedSqlDataSet(sql, new Object[]{sdcid, keyid1, keyid2, keyid3}, true);
            }
            notes.addColumn("contexttitle", 0);
            notes.addColumn("contextnotetitle", 0);
            notes.addColumn("contextgrouptitle", 0);
            notes.addColumn("contexttip", 0);
            notes.addColumn("editable", 0);
            notes.addColumn("resolvable", 0);
            noteTypes.setNotesData(notes);
            for (int i = 0; i < notes.getRowCount(); ++i) {
                String noteType = notes.getString(i, "notetypeflag", "U");
                notes.setString(i, "__editable", noteTypes.isEditable(noteType, i));
                notes.setString(i, "__resolvable", noteTypes.isResolvable(noteType, i));
            }
            if (contexts == null || contexts.size() == 0) {
                contexts = new PropertyListCollection();
                PropertyList primary = new PropertyList();
                primary.setProperty("contexttype", "primary");
                primary.setProperty("notetitle", "[sysuserid]");
                primary.setProperty("contexttitle", "[sdcid] Notes: [keyid1]");
                primary.setProperty("grouptitle", "[sdcid] Notes: [keyid1]");
                primary.setProperty("tip", "[sysusername]");
                contexts.add(primary);
                PropertyList attachment = new PropertyList();
                attachment.setProperty("contexttype", "attachment");
                attachment.setProperty("notetitle", "[sysuserid] - Attachment[attachmentnum] <a href=\"rc?command=viewattachment&sdcid=[sdcid]&keyid1=[keyid1]&keyid2=[keyid2]&keyid3=[keyid3]&attachmentnum=[attachmentnum]\" target=\"_blank\"><img src=\"WEB-CORE/modules/documents/images/DocumentAttachment.gif\" style=\"margin-bottom:-2px\"/></a>");
                attachment.setProperty("contexttitle", "Attachment Notes");
                attachment.setProperty("grouptitle", "Attachment[attachmentnum] <a href=\"rc?command=viewattachment&sdcid=[sdcid]&keyid1=[keyid1]&keyid2=[keyid2]&keyid3=[keyid3]&attachmentnum=[attachmentnum]\" target=\"_blank\"><img src=\"WEB-CORE/modules/documents/images/DocumentAttachment.gif\" style=\"margin-bottom:-2px\"/></a>");
                attachment.setProperty("tip", "Attachment [attachmentnum] for [sdcid] [keyid1]");
                contexts.add(attachment);
                PropertyList dataset = new PropertyList();
                dataset.setProperty("contexttype", "dataset");
                dataset.setProperty("notetitle", "[sysuserid] - [paramlistid]");
                dataset.setProperty("contexttitle", "DataSet Notes");
                dataset.setProperty("grouptitle", "[paramlistid]");
                dataset.setProperty("tip", "[paramlistid] (ver:[paramlistversionid], var:[variantid]) Inst: [dataset]");
                contexts.add(dataset);
                PropertyList dataitem = new PropertyList();
                dataitem.setProperty("contexttype", "dataitem");
                dataitem.setProperty("notetitle", "[sysuserid] - [paramlistid]/[paramid]");
                dataitem.setProperty("contexttitle", "DataItem Notes");
                dataitem.setProperty("grouptitle", "[paramlistid]/[paramid]");
                dataitem.setProperty("tip", "[paramlistid] (ver:[paramlistversionid], var:[variantid]) Inst: [dataset] Param:[paramid] [paramtype] Rep:[replicateid]");
                contexts.add(dataitem);
                PropertyList workitem = new PropertyList();
                workitem.setProperty("contexttype", "sdiworkitem");
                workitem.setProperty("notetitle", "[sysuserid] - [workitemid]");
                workitem.setProperty("contexttitle", "WorkItem Notes");
                workitem.setProperty("grouptitle", "[workitemid]");
                workitem.setProperty("tip", "[workitemid] (inst:[workiteminstance])");
                contexts.add(workitem);
            }
            contexts.index("contexttype");
            for (int i = 0; i < notes.size(); ++i) {
                String contexttitle = "";
                String contextnotetitle = "";
                String contextgrouptitle = "";
                String contexttip = "";
                String sysuserid = notes.getValue(i, "sysuserid");
                String sysusername = notes.getValue(i, "sysuserdesc");
                String contexttype = notes.getValue(i, "contexttype", "primary");
                String context = notes.getValue(i, "context", sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                PropertyList contextdef = contexts.getIndexedPropertyList(contexttype);
                if (contextdef != null) {
                    String[] keyparts = StringUtil.split(context, ";");
                    contexttitle = this.evaluateNoteExpression(this.unescapeChars(contextdef.getProperty("contexttitle")), contexttype, keyparts, sysuserid, sysusername);
                    contextnotetitle = this.evaluateNoteExpression(this.unescapeChars(contextdef.getProperty("notetitle")), contexttype, keyparts, sysuserid, sysusername);
                    contextgrouptitle = this.evaluateNoteExpression(this.unescapeChars(contextdef.getProperty("grouptitle")), contexttype, keyparts, sysuserid, sysusername);
                    contexttip = this.evaluateNoteExpression(this.unescapeChars(contextdef.getProperty("tip")), contexttype, keyparts, sysuserid, sysusername);
                }
                notes.setValue(i, "contexttitle", contexttitle);
                notes.setValue(i, "contextnotetitle", contextnotetitle);
                notes.setValue(i, "contextgrouptitle", contextgrouptitle);
                notes.setValue(i, "contexttip", contexttip);
            }
            notes.sort((groupby.length() > 0 ? groupby + " A," : "") + "threadnum " + sort.substring(0, 1) + ", ownerdt A");
            DataSet dataSet = notes;
            return dataSet;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create rset. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            if (rset != null && rset.getRsetid().length() > 0) {
                try {
                    dataAccessService.clearRSet(rset);
                }
                catch (ServiceException serviceException) {}
            }
        }
    }

    protected String escapeChars(String text) {
        return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }

    protected String unescapeChars(String text) {
        return text.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    private String evaluateNoteExpression(String expression, String contexttype, String[] keyparts, String sysuserid, String sysusername) {
        String sdcIdTrans;
        expression = StringUtil.replaceAll(expression, "[sysuserid]", sysuserid);
        expression = StringUtil.replaceAll(expression, "[sysusername]", sysusername);
        String string = sdcIdTrans = keyparts.length > 0 ? keyparts[0] : "";
        if (!sdcIdTrans.isEmpty()) {
            sdcIdTrans = this.getTranslationProcessor().translate(sdcIdTrans);
        }
        expression = StringUtil.replaceAll(expression, "[sdcid]", sdcIdTrans);
        expression = StringUtil.replaceAll(expression, "[keyid1]", keyparts.length > 1 ? keyparts[1] : "");
        expression = StringUtil.replaceAll(expression, "[keyid2]", keyparts.length > 2 ? keyparts[2] : "");
        expression = StringUtil.replaceAll(expression, "[keyid3]", keyparts.length > 3 ? keyparts[3] : "");
        if (contexttype.equalsIgnoreCase("attachment")) {
            expression = StringUtil.replaceAll(expression, "[attachmentnum]", keyparts.length > 4 ? keyparts[4] : "");
        } else if (contexttype.equalsIgnoreCase("sdiworkitem")) {
            expression = StringUtil.replaceAll(expression, "[workitemid]", keyparts.length > 4 ? keyparts[4] : "");
            expression = StringUtil.replaceAll(expression, "[workiteminstance]", keyparts.length > 5 ? keyparts[5] : "");
        } else if (contexttype.equalsIgnoreCase("dataset") || contexttype.equalsIgnoreCase("dataitem")) {
            expression = StringUtil.replaceAll(expression, "[paramlistid]", keyparts.length > 4 ? keyparts[4] : "");
            expression = StringUtil.replaceAll(expression, "[paramlistversionid]", keyparts.length > 5 ? keyparts[5] : "");
            expression = StringUtil.replaceAll(expression, "[variantid]", keyparts.length > 6 ? keyparts[6] : "");
            expression = StringUtil.replaceAll(expression, "[dataset]", keyparts.length > 7 ? keyparts[7] : "");
            if (contexttype.equalsIgnoreCase("dataitem")) {
                expression = StringUtil.replaceAll(expression, "[paramid]", keyparts.length > 8 ? keyparts[8] : "");
                expression = StringUtil.replaceAll(expression, "[paramtype]", keyparts.length > 9 ? keyparts[9] : "");
                expression = StringUtil.replaceAll(expression, "[replicateid]", keyparts.length > 10 ? keyparts[10] : "");
            }
        }
        return expression;
    }

    protected boolean loadSDIMaintSet(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        try {
            for (String name : commandRequest.keySet()) {
                Object value = commandRequest.get(name);
                if (!(value instanceof SDIRequest)) continue;
                commandResponse.set(name, this.loadSDIMaint((SDIRequest)value));
            }
            return true;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to load SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    protected SDIMaint loadSDIMaint(SDIRequest value) {
        SDIRequest sdiRequest = value;
        this.logInfo("Loading SDIMaint for " + new SDI(sdiRequest.getSDCid(), sdiRequest.getKeyid1List(), sdiRequest.getKeyid2List(), sdiRequest.getKeyid3List()).toString());
        return new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), this.getSDIProcessor().getSDIData(sdiRequest));
    }

    protected JSONableMap getEditorStyles(String editorstyleid) {
        JSONableMap editorStyles = new JSONableMap();
        if (editorstyleid.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList("LV_EditorStyle", editorstyleid, "", "");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                PropertyList def = new PropertyList();
                try {
                    PropertyListCollection columns;
                    String sdcid;
                    def.setPropertyList(primary.getValue(i, "editordefinition"));
                    PropertyList lookuplink = def.getPropertyList("lookuplink");
                    String string = sdcid = lookuplink != null ? lookuplink.getProperty("sdcid") : "";
                    if (sdcid.length() > 0 && (columns = lookuplink.getCollection("columns")) != null) {
                        for (int c = 0; c < columns.size(); ++c) {
                            if ("keycolid1".equals(columns.getPropertyList(c).getProperty("columnid"))) {
                                columns.getPropertyList(c).setProperty("columnid", this.getSDCProcessor().getProperty(sdcid, "keycolid1"));
                                continue;
                            }
                            if ("keycolid2".equals(columns.getPropertyList(c).getProperty("columnid"))) {
                                columns.getPropertyList(c).setProperty("columnid", this.getSDCProcessor().getProperty(sdcid, "keycolid2"));
                                continue;
                            }
                            if ("keycolid3".equals(columns.getPropertyList(c).getProperty("columnid"))) {
                                columns.getPropertyList(c).setProperty("columnid", this.getSDCProcessor().getProperty(sdcid, "keycolid3"));
                                continue;
                            }
                            if (!"desccol".equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                            columns.getPropertyList(c).setProperty("columnid", this.getSDCProcessor().getProperty(sdcid, "desccol"));
                        }
                    }
                    editorStyles.put(primary.getValue(i, "editorstyleid"), def);
                    continue;
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
            }
        }
        return editorStyles;
    }

    protected JSONableMap getQueryDefinitions(String queryid, String basedonid) {
        JSONableMap queryDefs = new JSONableMap();
        if (queryid.length() > 0 && basedonid.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList("Query", queryid, basedonid, "");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("queryarg");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet queryData = sdiData.getDataset("primary");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (int i = 0; i < queryData.size(); ++i) {
                PropertyList query = new PropertyList();
                query.setProperty("queryid", queryData.getValue(i, "queryid"));
                query.setProperty("basedonid", queryData.getValue(i, "basedonid"));
                query.setProperty("querydesc", queryData.getValue(i, "querydesc"));
                query.setProperty("cascadedargflag", queryData.getValue(i, "cascadedargflag"));
                filterMap.put("queryid", queryData.getValue(i, "queryid"));
                filterMap.put("basedonid", queryData.getValue(i, "basedonid"));
                DataSet queryargData = sdiData.getDataset("queryarg").getFilteredDataSet(filterMap);
                queryargData.sort("usersequence A");
                PropertyListCollection queryargs = new PropertyListCollection();
                for (int j = 0; j < queryargData.size(); ++j) {
                    PropertyList queryarg = new PropertyList();
                    queryarg.setProperty("argid", queryargData.getValue(j, "argid"));
                    queryarg.setProperty("argdesc", queryargData.getValue(j, "argdesc"));
                    queryarg.setProperty("argtype", queryargData.getValue(j, "argtype"));
                    queryarg.setProperty("sdcid", queryargData.getValue(j, "sdcid"));
                    queryarg.setProperty("reftypeid", queryargData.getValue(j, "reftypeid"));
                    queryarg.setProperty("weblookupurl", queryargData.getValue(j, "weblookupurl"));
                    queryarg.setProperty("lookuppageid", queryargData.getValue(j, "lookuppageid"));
                    queryarg.setProperty("argdata", queryargData.getValue(j, "argdata"));
                    queryargs.add(queryarg);
                }
                query.setProperty("queryargs", queryargs);
                queryDefs.put(queryData.getValue(i, "queryid") + ";" + queryData.getValue(i, "basedonid"), query);
            }
        }
        return queryDefs;
    }

    protected String getTracelogid(String auditReason, String auditActivity, String auditSigned, String auditDate, boolean standard, String tracelogDesc) throws SapphireException {
        try {
            AuditService auditService = new AuditService(this.sapphireConnection);
            return auditService.addTraceLogEntry(auditReason, auditActivity, auditSigned, auditDate, tracelogDesc, standard);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate trace log. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    private class NoteTypeCollection {
        private HashMap<String, NoteType> noteTypeMap;
        private PropertyListCollection noteTypeCollection;
        private SDIRequest sdiRequest = new SDIRequest();
        private QueryData queryData;
        private QueryData notesData;
        private ConnectionInfo connectionInfo;
        private SDITagInfo sdiTagInfo;

        private void setNotesData(DataSet notes) {
            this.notesData = new QueryData("notes", notes);
            HashMap<String, QueryData> datamap = new HashMap<String, QueryData>();
            datamap.put("primary", this.queryData);
            if (this.notesData != null) {
                datamap.put("notes", this.notesData);
            }
            this.sdiTagInfo = new SDITagInfo(datamap);
        }

        private PropertyListCollection getNoteTypeCollection() {
            return this.noteTypeCollection;
        }

        protected String isEditable(String noteTypeStr, Integer rowNum) {
            String editable = "N";
            NoteType noteType = this.noteTypeMap.get(noteTypeStr);
            if (noteType != null) {
                editable = noteType.getEditRule().equalsIgnoreCase("R") ? (!noteType.getEditRole().isEmpty() ? (StandardCommandRequest.this.getConnectionProcessor().getRoleList().contains(noteType.getEditRole()) ? "Y" : "N") : "Y") : this.evaluateRule(noteType.getEditRule(), rowNum);
            }
            return editable;
        }

        private String isResolvable(String noteTypeStr, Integer rowNum) {
            String resolvable = "N";
            NoteType noteType = this.noteTypeMap.get(noteTypeStr);
            if (noteType != null) {
                if (noteType.getResolveBy().equalsIgnoreCase("role")) {
                    resolvable = StandardCommandRequest.this.getConnectionProcessor().getRoleList().contains(noteType.getResolveRole()) ? "Y" : "N";
                } else if (noteType.getResolveBy().equalsIgnoreCase("user")) {
                    resolvable = this.notesData.getValue((int)rowNum, "followupuserid", "").equals(StandardCommandRequest.this.getConnectionInfo().getSysuserId()) ? "Y" : "N";
                } else if (noteType.getResolveBy().equalsIgnoreCase("rule")) {
                    resolvable = this.evaluateRule(noteType.getResolvableRule(), rowNum);
                }
            }
            return resolvable;
        }

        private String evaluateRule(String rule, Integer rowNum) {
            if (!(rule.isEmpty() || rule.equals("Y") || rule.equals("N") || rule.indexOf("$G{") != 0)) {
                if (this.queryData == null) {
                    DataSet primary = StandardCommandRequest.this.getSDIProcessor().getSDIData(this.sdiRequest).getDataset("primary");
                    this.queryData = new QueryData("primary", primary);
                    HashMap<String, QueryData> datamap = new HashMap<String, QueryData>();
                    datamap.put("primary", this.queryData);
                    if (this.notesData != null) {
                        datamap.put("notes", this.notesData);
                    }
                    this.sdiTagInfo = new SDITagInfo(datamap);
                }
                if (this.connectionInfo == null) {
                    SapphireConnection conn = StandardCommandRequest.this.getConnectionProcessor().getSapphireConnection();
                    this.connectionInfo = new ConnectionInfo(conn);
                }
                if (rowNum != null && this.notesData != null) {
                    this.notesData.setCurrentRow(rowNum);
                }
                try {
                    rule = GroovyUtil.evaluate(rule, this.connectionInfo, this.sdiTagInfo, null, null, null, "notes");
                }
                catch (SapphireException e) {
                    rule = "N";
                }
                if ("true".equals(rule)) {
                    rule = "Y";
                } else if ("false".equals(rule)) {
                    rule = "N";
                }
            }
            return rule;
        }

        protected NoteTypeCollection(PropertyListCollection noteTypeCollection, String sdcid, String keyid1, String keyid2, String keyid3) {
            this.sdiRequest.setSDCid(sdcid);
            this.sdiRequest.setKeyid1List(keyid1);
            this.sdiRequest.setKeyid2List(keyid2);
            this.sdiRequest.setKeyid3List(keyid3);
            this.sdiRequest.setRetainRsetid(false);
            this.sdiRequest.setRequestItem("primary");
            this.noteTypeCollection = noteTypeCollection;
            this.noteTypeMap = new HashMap();
            for (int i = 0; i < noteTypeCollection.size(); ++i) {
                PropertyList noteProps = noteTypeCollection.getPropertyList(i);
                String noteType = noteProps.getProperty("notetype");
                NoteType newType = this.noteTypeMap.get(noteType);
                if (newType != null) continue;
                newType = new NoteType(noteProps);
                this.noteTypeMap.put(noteType, newType);
            }
        }

        private class NoteType {
            private String addRule;
            private String editRule;
            private String resolvableRule;
            private String phraseTypeRule;
            private String phraseLookupUrl;
            private String addRole = "";
            private String editRole = "";
            private String resolveBy;
            private String resolveRole = "";

            public String getResolveBy() {
                return this.resolveBy;
            }

            public String getResolveRole() {
                return this.resolveRole;
            }

            private NoteType(PropertyList noteTypeProps) {
                PropertyList operations = noteTypeProps.getPropertyListNotNull("operations");
                PropertyList resolutionProps = operations.getPropertyListNotNull("resolutionprops");
                this.addRule = operations.getProperty("addrule", "Y");
                if (this.addRule.equalsIgnoreCase("R")) {
                    this.addRole = operations.getProperty("addrole", "");
                    this.addRule = !this.addRole.isEmpty() ? (StandardCommandRequest.this.getConnectionProcessor().getRoleList().contains(this.addRole) ? "Y" : "N") : "N";
                } else {
                    this.addRule = NoteTypeCollection.this.evaluateRule(this.addRule, null);
                }
                operations.setProperty("addrule", this.addRule);
                this.editRule = operations.getProperty("editrule", "Y");
                this.editRole = operations.getProperty("editrole", "");
                this.resolvableRule = resolutionProps.getProperty("resolvablerule");
                this.phraseTypeRule = NoteTypeCollection.this.evaluateRule(noteTypeProps.getProperty("phrasetype", ""), null);
                noteTypeProps.setProperty("phrasetype", this.phraseTypeRule);
                this.phraseLookupUrl = NoteTypeCollection.this.evaluateRule(noteTypeProps.getProperty("phraselookupurl", ""), null);
                noteTypeProps.setProperty("phraselookupurl", this.phraseLookupUrl);
                this.resolveBy = resolutionProps.getProperty("resolveby", "");
                if (this.resolveBy.equalsIgnoreCase("role")) {
                    this.resolveRole = resolutionProps.getProperty("resolverole");
                }
                String editorStyle = resolutionProps.getProperty("resolvebyeditorstyle", "User");
                noteTypeProps.setProperty("editorstyle", new PropertyList(StandardCommandRequest.this.getEditorStyles(editorStyle).getPropertyListNotNull(editorStyle)));
            }

            public String getAddRule() {
                return this.addRule;
            }

            private String getEditRule() {
                return this.editRule;
            }

            private String getResolvableRule() {
                return this.resolvableRule;
            }

            public String getAddRole() {
                return this.addRole;
            }

            public String getEditRole() {
                return this.editRole;
            }
        }
    }
}

