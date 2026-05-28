/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CommandRequest
implements CommandConstants {
    private String commmand;
    private String commandHandler;
    private JSONableMap requestJSONData = new JSONableMap();
    private HttpServletRequest request;

    public CommandRequest(HttpServletRequest request) {
        this.request = request;
        String jsonCommandRequest = request.getParameter("commandrequest");
        if (jsonCommandRequest != null) {
            try {
                JSONObject jsonCommand = new JSONObject(jsonCommandRequest.startsWith("{|}") ? EncryptDecrypt.decryptRSA(jsonCommandRequest.substring("{|}".length())) : jsonCommandRequest);
                this.commmand = jsonCommand.getString("command");
                this.commandHandler = jsonCommand.getString("commandhandler");
                JSONObject jsonData = jsonCommand.getJSONObject("data");
                Iterator it = jsonData.keys();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    this.requestJSONData.put(name, new JSONableString(jsonData.getString(name)));
                }
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
    }

    public CommandRequest(String command) {
        this.commmand = command;
    }

    public void set(String name, boolean value) {
        this.set(name, new JSONableString(value ? "Y" : "N"));
    }

    public void set(String name, String value) {
        this.set(name, new JSONableString(value));
    }

    public void set(String name, JSONable data) {
        this.requestJSONData.put(name, data);
    }

    public String getCommand() {
        return this.commmand;
    }

    public String getCommandHandler() {
        return this.commandHandler;
    }

    public boolean getBoolean(String name) {
        return this.getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return this.getString(name, defaultValue ? "Y" : "N").equals("Y");
    }

    public String getString(String name) {
        return this.requestJSONData.getString(name);
    }

    public String getString(String name, String defaultValue) {
        return this.requestJSONData.getString(name, defaultValue);
    }

    public PropertyListCollection getCollection(String name) {
        return this.requestJSONData.getCollection(name);
    }

    public PropertyListCollection getCollectionNotNull(String name) {
        PropertyListCollection ret = this.getCollection(name);
        return ret == null ? new PropertyListCollection() : ret;
    }

    public PropertyList getPropertyList(String name) {
        return this.requestJSONData.getPropertyList(name);
    }

    public PropertyList getPropertyListNotNull(String name) {
        PropertyList ret = this.getPropertyList(name);
        return ret == null ? new PropertyList() : ret;
    }

    public SDIMaint getSDIMaint(String name) {
        return new SDIMaint(new SDCProcessor(RequestContext.getRequestContext(this.request).getConnectionId()), this.requestJSONData.get(name).toJSONString());
    }

    public SDIRequest getSDIRequest(String name) {
        return this.requestJSONData.getSDIRequest(name);
    }

    public DataSet getDataSet(String name) {
        return this.requestJSONData.getDataSet(name);
    }

    public SDIList getSDIList(String name) {
        return this.requestJSONData.getSDIList(name);
    }

    public TaskContext getTaskContext(String name) {
        return this.requestJSONData.getTaskContext(name);
    }

    public JSONableMap getJSONableMap(String name) {
        return this.requestJSONData.getJSONableMap(name);
    }

    public Set<String> keySet() {
        return this.requestJSONData.keySet();
    }

    public Object get(String name) {
        return this.requestJSONData.get(name);
    }

    public boolean contains(String name) {
        return this.requestJSONData.containsKey(name);
    }

    public PropertyList getStringPropertyList() {
        PropertyList propertyList = new PropertyList();
        for (String name : this.keySet()) {
            Object value = this.get(name);
            if (!(value instanceof JSONableString) || name.endsWith("_type") || name.startsWith("__")) continue;
            propertyList.setProperty(name, this.getString(name));
        }
        return propertyList;
    }
}

