/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletRequestWrapper
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class HistoryWrapper
extends HttpServletRequestWrapper {
    HttpServletRequest servletRequest;
    String connectionid;
    boolean isHistory = false;
    PropertyList historyProps = null;

    public HistoryWrapper(HttpServletRequest servletRequest, String connectionid) throws SapphireException {
        super(servletRequest);
        this.servletRequest = servletRequest;
        this.connectionid = connectionid;
        if (servletRequest.getParameter("history") != null || servletRequest.getParameter("state") != null) {
            this.isHistory = true;
            try {
                this.historyProps = this.getHistoryProperties(servletRequest, connectionid);
            }
            catch (SapphireException e) {
                this.historyProps = new PropertyList();
                this.historyProps.setProperty("page", "SystemAdminTramline");
                this.historyProps.setProperty("displaymessage", "Failed to retrieve history properties.");
                Trace.logError("Failed to retrieve history properties. Defaulting page.");
            }
        }
    }

    public String getQueryString() {
        if (!this.isHistory || this.historyProps == null) {
            return super.getQueryString();
        }
        if (this.historyProps.getProperty("page") != null && this.historyProps.getProperty("page").length() > 0) {
            return "command=page&page=" + this.historyProps.getProperty("page");
        }
        return super.getQueryString();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private PropertyList getHistoryProperties(HttpServletRequest httpRequest, String connectionId) throws SapphireException {
        String webpagelogid = httpRequest.getParameter("history");
        if (webpagelogid == null || webpagelogid.length() == 0) {
            webpagelogid = httpRequest.getParameter("state");
        }
        PropertyList historyProps = new PropertyList();
        QueryProcessor queryProcessor = new QueryProcessor(connectionId);
        if (webpagelogid == null || webpagelogid.length() <= 0) throw new SapphireException("Invalid history command. No webpagelog entry provided.");
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT webpagerequest, propertyclob, sysuserid FROM webpagelog WHERE webpagelogid = ?", new Object[]{webpagelogid}, true);
        if (ds == null || ds.getRowCount() <= 0) throw new SapphireException("Invalid history command. The webpagelog entry does not exist.");
        String[] parts = StringUtil.split(ds.getString(0, "webpagerequest"), "&");
        String[] request = StringUtil.split(parts[0], "=");
        boolean userValid = false;
        try {
            ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionId);
            ConnectionInfo conInfo = connectionProcessor.getConnectionInfo(connectionId);
            ConfigurationProcessor cp = new ConfigurationProcessor(connectionId);
            PropertyList securityPolicy = cp.getPolicy("SecurityPolicy", conInfo != null && (conInfo.getUserType().equalsIgnoreCase("I") || conInfo.getUserType().equalsIgnoreCase("V")) ? "Virtual Custom" : "Sapphire Custom");
            boolean sharestate = securityPolicy.getPropertyList("sessionmanagement").getProperty("sharestate", "N").equalsIgnoreCase("Y");
            if (sharestate) {
                userValid = true;
            } else {
                String currentuser = conInfo.getSysuserId();
                String sysuserid = ds.getValue(0, "sysuserid", currentuser);
                userValid = currentuser.equals(sysuserid);
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to check user.", e);
        }
        if (!userValid) throw new SapphireException("Invalid history command. History entry owned by another user and security policy set to disable state sharing.");
        if (request != null && request.length == 2) {
            String properties = ds.getClob(0, "propertyclob");
            PropertyList logProps = new PropertyList();
            if (properties != null && properties.length() > 0) {
                logProps.setPropertyList(properties);
            }
            RequestContext requestContext = RequestContext.getInstance(httpRequest);
            requestContext.setRequestId(String.valueOf(System.currentTimeMillis()));
            if (request[0].equals("page")) {
                historyProps = new RequestProcessor(connectionId).getWebPageProperties(request[1], requestContext);
                historyProps.setProperty("command", "page");
                historyProps.setProperty("page", request[1]);
                historyProps.setProperty("registeredpage", "true");
            }
            for (int i = 1; i < parts.length; ++i) {
                String[] partParts = StringUtil.split(parts[i], "=");
                if (partParts.length != 2) continue;
                historyProps.setProperty(partParts[0], partParts[1]);
            }
            historyProps.setProperty("pagedata", new PropertyList());
            PropertyList pageData = historyProps.getPropertyList("pagedata");
            Set keyset = logProps.keySet();
            for (String propertyid : keyset) {
                String value = logProps.getProperty(propertyid);
                if (!propertyid.equalsIgnoreCase("_nav")) {
                    historyProps.setProperty(propertyid, value);
                }
                if (pageData == null) continue;
                pageData.setProperty(propertyid, value);
            }
            if (httpRequest.getParameter("_nav") != null && httpRequest.getParameter("_nav").toString().length() > 0) {
                historyProps.setProperty("_nav", httpRequest.getParameter("_nav").toString());
            }
            if (httpRequest.getParameter("command") != null && httpRequest.getParameter("command").equalsIgnoreCase("state")) {
                if (webpagelogid.length() > 0) {
                    historyProps.setProperty("_wpsid", webpagelogid);
                }
                if (httpRequest.getParameter("_states") != null && httpRequest.getParameter("_states").length() > 0) {
                    historyProps.setProperty("_states", httpRequest.getParameter("_states"));
                }
                if (httpRequest.getParameter("wid") != null && httpRequest.getParameter("wid").length() > 0) {
                    historyProps.setProperty("_wid", httpRequest.getParameter("wid"));
                }
                if (httpRequest.getParameter("guimode") != null && httpRequest.getParameter("guimode").length() > 0) {
                    historyProps.setProperty("guimode", httpRequest.getParameter("guimode"));
                }
            }
        }
        if (historyProps.findProperty("connectionid") == null) return historyProps;
        historyProps.deleteProperty("connectionid");
        return historyProps;
    }

    public String[] getParameterValues(String parameter) {
        if ("csrftoken".equals(parameter)) {
            return super.getParameterValues(parameter);
        }
        if (this.isHistory) {
            String val = this.historyProps.getProperty(parameter);
            if (val != null && val.length() > 0) {
                return StringUtil.split(val, ";");
            }
            return null;
        }
        return super.getParameterValues(parameter);
    }

    public Enumeration getParameterNames() {
        if (!this.isHistory) {
            return super.getParameterNames();
        }
        return Collections.enumeration(this.historyProps.keySet());
    }

    public Map getParameterMap() {
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if (this.isHistory) {
            for (String param : this.historyProps.keySet()) {
                map.put(param, this.getParameterValues(param));
            }
        } else {
            Enumeration e = super.getParameterNames();
            while (e.hasMoreElements()) {
                String parameter = (String)e.nextElement();
                String[] values = this.getParameterValues(parameter);
                map.put(parameter, values);
            }
        }
        return map;
    }

    public String getParameter(String parameter) {
        if ("csrftoken".equals(parameter)) {
            return super.getParameter(parameter);
        }
        if (this.isHistory) {
            if (this.historyProps.findProperty(parameter) == null) {
                return null;
            }
            return this.historyProps.getProperty(parameter);
        }
        return super.getParameter(parameter);
    }
}

