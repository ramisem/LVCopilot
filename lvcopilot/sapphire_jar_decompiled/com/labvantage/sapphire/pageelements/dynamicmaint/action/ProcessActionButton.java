/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.action;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ProcessActionButton
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sActionButtonConfig = properties.getProperty("actionbuttonconfig");
        PropertyList globalValues = new PropertyList();
        try {
            JSONObject jActionButtonConfig = new JSONObject(sActionButtonConfig);
            globalValues.setProperty("currentuser", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId());
            globalValues.setProperty("currentconnection", this.getConnectionId());
            for (Object o : properties.keySet()) {
                String key = (String)o;
                if (!key.startsWith("action_")) continue;
                globalValues.setProperty(key.substring(7), properties.getProperty(key));
            }
            JSONArray actions = jActionButtonConfig.optJSONArray("actions");
            for (int i = 0; i < actions.length(); ++i) {
                JSONObject action = actions.optJSONObject(i);
                String actionid = action.optString("actionid");
                String actionversionid = action.optString("actionversionid", "1");
                PropertyList actionProperties = new PropertyList();
                JSONArray inputProperties = action.optJSONArray("inputprops");
                if (inputProperties != null) {
                    for (int j = 0; j < inputProperties.length(); ++j) {
                        JSONObject inputProperty = inputProperties.getJSONObject(j);
                        String propertyid = inputProperty.optString("propertyid", "");
                        String propertyvalue = inputProperty.optString("value", "");
                        if (propertyvalue.contains("[") && propertyvalue.contains("]")) {
                            propertyvalue = Utils.replaceVariables(propertyvalue, globalValues);
                        }
                        actionProperties.setProperty(propertyid, propertyvalue);
                    }
                }
                this.getActionProcessor().processAction(actionid, actionversionid, actionProperties);
                JSONArray outputProperties = action.optJSONArray("outputprops");
                if (outputProperties == null) continue;
                for (int j = 0; j < outputProperties.length(); ++j) {
                    JSONObject outputProperty = outputProperties.getJSONObject(j);
                    String propertyid = outputProperty.optString("propertyid", "");
                    String propertyvalue = outputProperty.optString("variable", "");
                    if (!propertyvalue.startsWith("[") || !propertyvalue.endsWith("]")) continue;
                    propertyvalue = propertyvalue.substring(1, propertyvalue.length() - 1);
                    globalValues.setProperty(propertyvalue, actionProperties.getProperty(propertyid));
                }
            }
        }
        catch (JSONException je) {
            throw new SapphireException("Failed to parse JSON: " + sActionButtonConfig, je);
        }
        catch (SapphireException se) {
            throw se;
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected error: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        for (Object o : globalValues.keySet()) {
            String key = (String)o;
            properties.setProperty(key, globalValues.getProperty(key));
        }
    }
}

