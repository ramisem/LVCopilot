/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.BaseAccessor;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class RequestProcessor
extends BaseAccessor {
    public RequestProcessor(String connectionid) {
        super(connectionid);
    }

    public RequestProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public PropertyList getConnectionProperties() throws SapphireException {
        try {
            return this.getRequestManager().getConnectionProperties(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get connection properties", e);
        }
    }

    public PropertyList getWebPageProperties(String webpageid, RequestContext requestContext) throws SapphireException {
        try {
            return this.getRequestManager().getWebPageProperties(this.getConnectionid(), webpageid, requestContext.getPropertyList());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get web page properties", e);
        }
    }

    public PropertyList getWebPageProperties(String webpageid, String productedition, PropertyList requestProps, boolean filterProperties) throws SapphireException {
        try {
            return this.getRequestManager().getWebPageProperties(this.getConnectionid(), webpageid, productedition, requestProps, filterProperties);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get web page properties", e);
        }
    }

    public PropertyList getBulletinProperties(String viewbulletinpage, String bulletin, RequestContext requestContext) throws SapphireException {
        try {
            return this.getRequestManager().getBulletinProperties(this.getConnectionid(), viewbulletinpage, bulletin, requestContext.getPropertyList());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get bulletin properties", e);
        }
    }

    public PropertyList getHistoryProperties(String history, RequestContext requestContext) throws SapphireException {
        try {
            return this.getRequestManager().getHistoryProperties(this.getConnectionid(), history, requestContext.getPropertyList());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get history properties", e);
        }
    }

    public PropertyList getFavoriteProperties(String favorite, RequestContext requestContext) throws SapphireException {
        try {
            return this.getRequestManager().getFavoriteProperties(this.getConnectionid(), favorite, requestContext.getPropertyList());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get favorite properties", e);
        }
    }

    public void addPropertyData(RequestContext requestContext) throws SapphireException {
        PropertyList layout;
        String sdcid;
        HashMap requestProps = null;
        PropertyList pageData = requestContext.getPropertyList("pagedata");
        if (pageData != null && (sdcid = requestContext.getPropertyList("pagedata").getProperty("sdcid")).length() > 0) {
            requestProps = new PropertyList();
            ((PropertyList)requestProps).setProperty("sdcid", sdcid);
        }
        if ((layout = requestContext.getPropertyList("layout")) != null) {
            if (layout.getProperty("showbulletins").equals("Y")) {
                if (requestProps == null) {
                    requestProps = new PropertyList();
                }
                ((PropertyList)requestProps).setProperty("showbulletins", "Y");
            }
            if (layout.getProperty("showfavorites").equals("Y")) {
                if (requestProps == null) {
                    requestProps = new PropertyList();
                }
                ((PropertyList)requestProps).setProperty("showfavorites", "Y");
            }
            if (layout.getProperty("showhistory").equals("Y")) {
                if (requestProps == null) {
                    requestProps = new PropertyList();
                }
                ((PropertyList)requestProps).setProperty("showhistory", "Y");
            }
        }
        if (requestProps != null && requestProps.size() > 0) {
            try {
                requestContext.getPropertyList().putAll(this.getRequestManager().addPropertyData(this.getConnectionid(), (PropertyList)requestProps));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to add property data", e);
            }
        }
    }

    public HashMap processRequest(String requestHandler, HashMap requestProps) throws SapphireException {
        try {
            HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), requestHandler, requestProps);
            ErrorHandler errorHandler = (ErrorHandler)returnProps.get("ERRORHANDLER");
            if (errorHandler != null && errorHandler.size() > 0) {
                if (errorHandler.hasErrors()) {
                    this.setErrorString(errorHandler.getEncodedString());
                } else {
                    this.setInfoErrorString(errorHandler.getEncodedString());
                }
            }
            return returnProps;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to process request", e);
        }
    }

    public String logPageState(String pagename, HttpServletRequest request) throws SapphireException {
        PropertyList ignore = new PropertyList();
        ignore.setProperty("_states", "Y");
        ignore.setProperty("_nav", "Y");
        return this.logPageAccess(pagename, request, ignore, true);
    }

    public void logPageAccess(String title, HttpServletRequest request, PropertyList ignoreProperties) throws SapphireException {
        boolean newMode;
        PropertyList guiPolicy = null;
        try {
            guiPolicy = new ConfigurationProcessor(this.getConnectionid()).getPolicy("GUIPolicy", "Sapphire Custom");
        }
        catch (Exception e) {
            guiPolicy = null;
        }
        boolean bl = newMode = guiPolicy != null ? guiPolicy.getProperty("enable", "N").equalsIgnoreCase("Y") : false;
        if (!newMode) {
            this.logPageAccess(title, request, ignoreProperties, false);
        }
    }

    private String logPageAccess(String title, HttpServletRequest request, PropertyList ignoreProperties, boolean state) throws SapphireException {
        String out = "";
        String requestStr = "";
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String layout1 = requestContext.getPropertyList("layout") == null ? "" : requestContext.getPropertyList("layout").getProperty("propertytreeid");
        String layout2 = requestContext.getPropertyList("pagedata") == null ? "" : requestContext.getPropertyList("pagedata").getProperty("layout");
        boolean modernLayout = requestContext.getProperty("modernlayout").equalsIgnoreCase("Y");
        if (modernLayout || !layout1.equals("GenericPopup") && !layout2.equalsIgnoreCase("navigator")) {
            if (modernLayout) {
                if (title == null || title.length() == 0) {
                    title = requestContext.getProperty(requestContext.getProperty("command"));
                }
                requestStr = requestContext.getProperty("command") + "=" + requestContext.getProperty(requestContext.getProperty("command"));
            } else if (requestContext.getProperty("page") != null && requestContext.getProperty("page").length() > 0) {
                requestStr = "page=" + requestContext.getProperty("page");
                if (title == null || title.length() == 0) {
                    title = requestContext.getProperty("page");
                }
            } else if (requestContext.getProperty("wizard") != null && requestContext.getProperty("wizard").length() > 0) {
                requestStr = "wizard=" + requestContext.getProperty("wizard");
            }
            HashMap<String, Object> params = new HashMap<String, Object>();
            if (requestStr.length() > 0 && title != null && title.length() > 0) {
                if (!modernLayout) {
                    params.put("currentlayout", request.getSession().getAttribute("currentlayout"));
                    params.put("currentlayoutnode", request.getSession().getAttribute("currentlayoutnode"));
                    PropertyList userPreferences = (PropertyList)request.getSession().getAttribute("userconfig");
                    params.put("currentlayouttab", userPreferences.getProperty("genericlayout_lastlinktab"));
                    params.put("currentlayoutmenu", userPreferences.getProperty("genericlayout_lastlinkmenu"));
                }
                Enumeration parameterNames = request.getParameterNames();
                while (parameterNames.hasMoreElements()) {
                    String parameterName = (String)parameterNames.nextElement();
                    String parameterValue = request.getParameter(parameterName);
                    if (ignoreProperties != null && ignoreProperties.getProperty(parameterName, "").length() != 0) continue;
                    params.put(parameterName, parameterValue);
                }
                try {
                    out = this.getRequestManager().logPageAccess(this.getConnectionid(), requestStr, title, title, params, state);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to log page access", e);
                }
            }
        }
        return out;
    }

    public void logPageAccess(String title, HttpServletRequest request) throws SapphireException {
        this.logPageAccess(title, request, null);
    }

    public void logPageAccess(String request, String title, String tip, HashMap requestProps) throws SapphireException {
        try {
            this.getRequestManager().logPageAccess(this.getConnectionid(), request, title, tip, requestProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to log page access", e);
        }
    }

    public String processFileCommand(String fileName) throws Exception {
        return this.getRequestManager().processFileCommand(this.getConnectionid(), fileName);
    }
}

