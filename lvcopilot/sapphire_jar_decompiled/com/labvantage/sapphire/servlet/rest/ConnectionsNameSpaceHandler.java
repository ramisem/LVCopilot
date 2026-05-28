/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestException;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class ConnectionsNameSpaceHandler
extends BaseNameSpaceHandler {
    @Override
    public boolean requiresConnection() {
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void process() throws Exception {
        PropertyList systemResources;
        PropertyList propertyList = systemResources = this.sapphireConnection != null ? this.getPolicyPropertyList().getPropertyListNotNull("systemresources") : null;
        if (this.doGet()) {
            if (this.getNameSpaceSegmentCount() == 0) {
                if (!this.isJUnit && systemResources != null && !systemResources.getPropertyListNotNull("connectionresources").getPropertyListNotNull("getconnection").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
                if (this.sapphireConnection == null) throw new RestException(401, "Missing, invalid or timed out connectionid", "No connectionid found in request");
                JSONObject connections = new JSONObject();
                connections.put("connectionid", this.sapphireConnection.getConnectionId());
                this.setResponseValue("connections", connections);
                return;
            } else {
                if (this.getNameSpaceSegmentCount() != 1) throw new RestException(400, "Malformed request URL", "Expecting request in the form /connections/{connectionresource}, e.g. /connections/conn1234");
                if (!this.isJUnit && systemResources != null && !systemResources.getPropertyList("connectionresources").getPropertyList("getconnection").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
                String connectionid = this.getNameSpaceSegment(0);
                ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                if (!cp.checkConnection(connectionid)) throw new RestException(404, "Resource not found", "/connections/" + connectionid + " resource is not available");
                this.logInfo("Connection " + connectionid + " checked");
                this.setResponseValue("message", "Connection " + connectionid + " checked");
            }
            return;
        } else if (this.doPost()) {
            String connectionid;
            if (systemResources != null) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
            if (this.getNameSpaceSegmentCount() != 0) throw new RestException(400, "Malformed request URL", "Expecting request in the form /connections, e.g. /connections");
            String databaseid = this.jsonRequest.optString("databaseid");
            String username = this.jsonRequest.optString("username");
            String password = this.jsonRequest.optString("password");
            if (databaseid == null || databaseid.length() <= 0 || username == null || username.length() <= 0 || password == null || password.length() <= 0) throw new RestException(401, "Malformed request body", "Missing connections details - requires databaseid, username and password");
            ConnectionProcessor cp = new ConnectionProcessor();
            String systemConnectionid = SapphireService.getInternalConnectionid(databaseid);
            PropertyTree propertyTree = new WebAdminProcessor(systemConnectionid).getPropertyTree("RESTPolicy");
            PropertyList restPolicyPropertyList = propertyTree.getNodePropertyList("Sapphire Custom", true);
            PropertyList systemResourcesAfterInternalLogin = restPolicyPropertyList.getPropertyListNotNull("systemresources");
            boolean rootResourceBoolean = systemResourcesAfterInternalLogin.getPropertyList("connectionresources").getPropertyList("postconnection").getProperty("enabled", "N").equals("Y");
            if (!rootResourceBoolean && !this.isJUnit) {
                throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
            }
            DataSet user = new QueryProcessor(systemConnectionid).getPreparedSqlDataSet("SELECT nameduserflag, externalappid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{username});
            String usertype = user.getValue(0, "nameduserflag");
            String externalappid = user.getValue(0, "externalappid");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("remotehost", this.request.getRemoteHost());
            options.put("remoteip", this.request.getRemoteAddr());
            options.put("tool", "SapphireREST");
            options.put("deviceid", "WebService");
            if (usertype.equals("A") && externalappid.length() > 0) {
                options.put("externalappid", externalappid);
            }
            if ((connectionid = cp.getConnectionid(databaseid, username, password, options)).length() <= 0 || cp.hasErrors()) throw new RestException(403, "Connection refused - bad credentials", cp.getLastErrorMessage());
            this.logInfo("Connection " + connectionid + " created");
            JSONObject connections = new JSONObject();
            connections.put("connectionid", connectionid);
            this.setResponseValue("connections", connections);
            this.setResponseCode(201, "Connection " + connectionid + " created");
            if (allowCookies) {
                HttpUtil httpUtil = new HttpUtil(this.request, this.response);
                httpUtil.setCookieValue("connectionid", connectionid, false, true);
            }
            Browser browser = new Browser(this.request);
            PropertyList connectionLogProps = new PropertyList();
            connectionLogProps.setProperty("clientuseragent", browser.getUseragent());
            connectionLogProps.setProperty("clientbrowser", browser.getName() + " (" + browser.getVersion() + ")");
            connectionLogProps.setProperty("clienthostname", this.request.getRemoteHost());
            connectionLogProps.setProperty("clientipaddress", this.request.getRemoteAddr());
            connectionLogProps.setProperty("connectiontypeflag", "W");
            SecurityService.updateConnectionLog(connectionid, connectionLogProps);
            return;
        } else if (this.doPut()) {
            if (this.getNameSpaceSegmentCount() != 1) throw new RestException(400, "Malformed request URL", "Expecting request in the form /connections/{connectionresource}, e.g. /connections/conn1234");
            if (!this.isJUnit && systemResources != null && !systemResources.getPropertyList("connectionresources").getPropertyList("putconnection").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
            String connectionid = this.getNameSpaceSegment(0);
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            boolean validId = cp.checkConnection(connectionid);
            if (!validId) throw new RestException(401, "Missing, invalid or timed out connectionid", "Invalid connectionid");
            this.logInfo("Connection " + connectionid + " refreshed");
            this.setResponseValue("message", "Connection " + connectionid + " refreshed");
            return;
        } else {
            if (!this.doDelete()) throw new RestException(405, "Invalid request method", "/connections resource only accepts GET, POST, PUT and DELETE methods.");
            if (!this.isJUnit && systemResources != null && !systemResources.getPropertyList("connectionresources").getPropertyList("deleteconnection").getProperty("enabled", "N").equals("Y")) throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
            if (this.getNameSpaceSegmentCount() == 0 && allowCookies) {
                HttpUtil httpUtil = new HttpUtil(this.request, this.response);
                String connectionid = httpUtil.getCookieValue("connectionid");
                ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                cp.clearConnection(connectionid);
                this.logInfo("Connection " + connectionid + " cleared");
                this.setResponseValue("message", "Connection " + connectionid + " cleared");
                httpUtil.removeCookie("connectionid");
                return;
            } else {
                if (this.getNameSpaceSegmentCount() != 1) throw new RestException(400, "Malformed request URL", "Expecting request in the form /connections/{connectionresource}, e.g. /connections/conn1234");
                String connectionid = this.getNameSpaceSegment(0);
                ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                cp.clearConnection(connectionid);
                this.logInfo("Connection " + connectionid + " cleared");
                this.setResponseValue("message", "Connection " + connectionid + " cleared");
                if (!allowCookies) return;
                HttpUtil httpUtil = new HttpUtil(this.request, this.response);
                httpUtil.removeCookie("connectionid");
            }
        }
    }
}

