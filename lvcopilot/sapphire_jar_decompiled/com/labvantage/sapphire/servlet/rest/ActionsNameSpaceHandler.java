/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActionsNameSpaceHandler
extends BaseNameSpaceHandler {
    private static HashMap<String, ArrayList<String>> databaseActions = new HashMap();
    private static HashMap<String, HashMap<String, DataSet>> databaseActionDefs = new HashMap();
    private static HashMap<String, HashMap<String, DataSet>> databaseActionInputs = new HashMap();
    private static final String ACTIONPROCESSING_FULL = "F";
    private static final String ACTIONPROCESSING_RESTICTED = "R";
    private static final String ACTIONPROCESSING_ROLE = "P";

    public static void policyChange(String databaseid) {
        databaseActions.entrySet().removeIf(stringHashMapEntry -> ((String)stringHashMapEntry.getKey()).startsWith(databaseid + ";"));
        databaseActionDefs.remove(databaseid);
        databaseActionInputs.remove(databaseid);
    }

    @Override
    public boolean requiresConnection() {
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void process() throws Exception {
        String cacheKey = this.sapphireConnection.getDatabaseId() + ";" + this.getRestPolicyNodeid();
        ArrayList<String> actions = databaseActions.get(cacheKey);
        if (actions == null) {
            actions = new ArrayList();
            if (Configuration.isJunitServer()) {
                Collections.addAll(actions, "AddSDI", "EditSDI", "DeleteSDI");
            } else {
                Collections.addAll(actions, ActionsNameSpaceHandler.getPermittedActions(this.getPolicyPropertyList(), this.getQueryProcessor(), this.sapphireConnection));
            }
            databaseActions.put(cacheKey, actions);
        }
        PropertyList systemResources = this.getPolicyPropertyList().getPropertyListNotNull("systemresources");
        if (this.doGet()) {
            if (this.getNameSpaceSegmentCount() == 0) {
                if (!this.isJUnit && !systemResources.getPropertyListNotNull("actionresources").getPropertyListNotNull("getactions").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
                JSONArray collection = new JSONArray();
                for (String actionid : actions) {
                    JSONObject action = new JSONObject();
                    action.put("actionid", actionid);
                    collection.put(action);
                }
                this.setResponseValue("actions", collection);
                return;
            } else {
                DataSet actionInput;
                HashMap<String, DataSet> actionInputs;
                DataSet actionDef;
                if (this.getNameSpaceSegmentCount() != 1) throw new RestException(400, "Malformed request URL", "Expecting request in the form /actions/{actionresource}, e.g. /actions/SendMail");
                if (!this.isJUnit && !systemResources.getPropertyListNotNull("actionresources").getPropertyListNotNull("getaction").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
                String actionid = this.getNameSpaceSegment(0);
                if (!actions.contains(actionid)) throw new RestException(404, "Resource not found", "/actions/" + actionid + " resource is not available");
                HashMap<String, DataSet> actionDefs = databaseActionDefs.get(this.sapphireConnection.getDatabaseId());
                if (actionDefs == null) {
                    actionDefs = new HashMap();
                    databaseActionDefs.put(this.sapphireConnection.getDatabaseId(), actionDefs);
                }
                if ((actionDef = actionDefs.get(actionid)) == null) {
                    actionDef = this.getQueryProcessor().getPreparedSqlDataSet("SELECT actionid, actiondesc FROM action WHERE actionid = ? AND actionversionid = ?", new Object[]{actionid, 1});
                    actionDefs.put(actionid, actionDef);
                }
                if ((actionInputs = databaseActionInputs.get(this.sapphireConnection.getDatabaseId())) == null) {
                    actionInputs = new HashMap();
                    databaseActionInputs.put(this.sapphireConnection.getDatabaseId(), actionInputs);
                }
                if ((actionInput = actionInputs.get(actionid)) == null) {
                    actionInput = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM actionproperty WHERE actionid = ? AND actionversionid = ? AND propertytypeflag = 'I' ORDER BY usersequence", new Object[]{actionid, 1});
                    actionInputs.put(actionid, actionInput);
                }
                JSONObject jsonAction = new JSONObject();
                for (int i = 0; i < actionInput.size(); ++i) {
                    String propertyid = actionInput.getValue(i, "propertyid");
                    if (!actionInput.getValue(i, "propertytypeflag", "I").equals("I")) continue;
                    jsonAction.put(propertyid, "{" + propertyid + "}");
                }
                this.setResponseValue(actionDef.getValue(0, "actionid"), jsonAction);
            }
            return;
        }
        if (!this.doPost()) throw new RestException(405, "Invalid request method", "/actions resource only accepts POST and GET methods.");
        if (this.getNameSpaceSegmentCount() == 0) {
            List<String> distinctActionClasses;
            ActionBlock actionBlock;
            if (!this.isJUnit && !systemResources.getPropertyListNotNull("actionresources").getPropertyListNotNull("postactions").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
            try {
                actionBlock = new ActionBlock(this.jsonRequest);
            }
            catch (Exception e) {
                throw new RestException(400, "Malformed request body", "Failed to parse action block definition");
            }
            List<String> distinctActions = actionBlock.getDistinctActions();
            if (distinctActions != null && distinctActions.size() > 0) {
                for (String distinctAction : distinctActions) {
                    if (actions.contains(distinctAction)) continue;
                    throw new RestException(404, "Resource not found", distinctAction + " is not available");
                }
            }
            if (!((distinctActionClasses = actionBlock.getDistinctActionClasses()) == null || distinctActionClasses.size() <= 0 || distinctActionClasses.size() == 1 && distinctActionClasses.get(0).equals("BaseAction"))) {
                throw new RestException(400, "Malformed request body", "Action block cannot contain action classes");
            }
            this.getActionProcessor().processActionBlock(actionBlock);
            this.setResponseValue("message", "ActionBlock " + (actionBlock.getName().length() > 0 ? "'" + actionBlock.getName() + "' " : "") + "executed successfully");
            HashMap returnProps = actionBlock.getReturnProperties();
            if (returnProps == null || returnProps.size() <= 0) return;
            JSONObject returns = new JSONObject();
            for (Object o : returnProps.keySet()) {
                String propertyid = (String)o;
                returns.put(propertyid, returnProps.get(propertyid));
            }
            this.setResponseValue("output", returns);
            return;
        }
        if (this.getNameSpaceSegmentCount() != 1) throw new RestException(400, "Malformed request URL", "Expecting request in the form /actions/{actionresource}, e.g. /actions/SendMail");
        if (!this.isJUnit && !systemResources.getPropertyList("actionresources").getPropertyList("postaction").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
        String actionid = this.getNameSpaceSegment(0);
        if (!actions.contains(actionid)) throw new RestException(404, "Resource not found", "/actions/" + actionid + " resource is not available");
        PropertyList actionProps = new PropertyList();
        Iterator iterator = this.jsonRequest.keys();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            actionProps.setProperty(name, this.jsonRequest.getString(name));
        }
        this.getActionProcessor().processAction(actionid, "1", actionProps);
        this.setResponseValue("message", "Action " + actionid + " executed successfully");
        this.setResponseValue("output", this.getActionOutput(actionid, actionProps));
    }

    public static String[] getPermittedActions(String connectionid) {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        String externalappid = sapphireConnection == null ? null : sapphireConnection.getExternalAppId();
        String nodeid = "Sapphire Custom";
        QueryProcessor queryProcessor = new QueryProcessor(connectionid);
        if (externalappid != null && externalappid.length() > 0) {
            DataSet externalAppDS = ExternalAuthenticationUtil.getCachedExternalAppDS(externalappid, sapphireConnection.getDatabaseId(), queryProcessor);
            nodeid = externalAppDS.getValue(0, "restpolicynodeid", "Sapphire Custom");
        }
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(connectionid);
        try {
            PropertyList policy = configurationProcessor.getPolicy("RESTPolicy", nodeid);
            return ActionsNameSpaceHandler.getPermittedActions(policy, queryProcessor, sapphireConnection);
        }
        catch (SapphireException e) {
            return new String[0];
        }
    }

    public static String[] getPermittedActions(PropertyList policy, QueryProcessor queryProcessor, SapphireConnection sapphireConnection) {
        ArrayList<String> permitted = new ArrayList<String>();
        try {
            if (policy != null) {
                PropertyList section = policy.getPropertyListNotNull("systemresources").getPropertyListNotNull("actionresources");
                String actionprocessing = section.getProperty("actionprocessing", ACTIONPROCESSING_FULL);
                if (actionprocessing.equals(ACTIONPROCESSING_RESTICTED)) {
                    PropertyListCollection permittedactions = section.getCollection("permittedactions");
                    for (int i = 0; i < permittedactions.size(); ++i) {
                        String actionid = permittedactions.getPropertyList(i).getProperty("actionid");
                        permitted.add(actionid.contains("|") ? actionid.substring(0, actionid.indexOf("|")) : actionid);
                    }
                } else if (actionprocessing.equals(ACTIONPROCESSING_ROLE)) {
                    DataSet actions = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT keyid1 FROM sdirole WHERE sdcid = 'Action' AND roleid IN ( SELECT roleid FROM sysuserrole WHERE sysuserid = ? ) ORDER BY keyid1", new Object[]{sapphireConnection.getSysuserId()});
                    for (int i = 0; i < actions.size(); ++i) {
                        permitted.add(actions.getValue(i, "keyid1"));
                    }
                } else if (actionprocessing.equals(ACTIONPROCESSING_FULL)) {
                    DataSet actions = queryProcessor.getSqlDataSet("SELECT actionid FROM action ORDER BY actionid");
                    for (int i = 0; i < actions.size(); ++i) {
                        permitted.add(actions.getValue(i, "actionid"));
                    }
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return permitted.toArray(new String[permitted.size()]);
    }
}

