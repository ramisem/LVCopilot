/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package sapphire.ext;

import com.labvantage.sapphire.Trace;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.ext.LogonRequestValidator;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class BaseWebSSOHandler
implements LogonRequestValidator {
    private String webssodatabase = "";
    private String webssologoffurl = "loggedoffmessage.jsp";
    private String webssoesigurl = "";
    private String useridattributename = "";
    private boolean allowHeaderAttributes = false;
    private static HashMap<String, HashMap<String, PropertyList>> portalSSOProps = new HashMap();

    public static void setPortalSSOProps(String databaseId, String portalId, PropertyList portalProps) {
        HashMap<String, PropertyList> pProps = portalSSOProps.getOrDefault(databaseId, new HashMap());
        portalSSOProps.put(databaseId, pProps);
        PropertyList logonProps = portalProps.getPropertyListNotNull("logonpagesettings");
        PropertyList ssoProps = logonProps.getPropertyListNotNull("ssoprops");
        pProps.put(portalId, ssoProps);
    }

    public static boolean isEnabled(String portalId, String databaseId) {
        return "Y".equals(portalSSOProps.getOrDefault(databaseId, new HashMap()).getOrDefault(portalId, new PropertyList()).getProperty("enable", "N"));
    }

    public static String getWebssoesigurl(String portalId, String databaseId) {
        return portalSSOProps.getOrDefault(databaseId, new HashMap()).getOrDefault(portalId, new PropertyList()).getProperty("webssoesigurl");
    }

    public static String getUseridattributename(String portalId, String databaseId) {
        return portalSSOProps.getOrDefault(databaseId, new HashMap()).getOrDefault(portalId, new PropertyList()).getProperty("useridattributename");
    }

    public static boolean isAllowHeaderAttributes(String portalId, String databaseId) {
        return "Y".equals(portalSSOProps.getOrDefault(databaseId, new HashMap()).getOrDefault(portalId, new PropertyList()).getProperty("allowheaderattributes", "N"));
    }

    @Override
    public boolean isRequireSysuserInfo() {
        return false;
    }

    public String getWebssodatabase() {
        return this.webssodatabase;
    }

    public void setWebssodatabase(String webssodatabase) {
        this.webssodatabase = webssodatabase;
    }

    public String getWebssologoffurl() {
        return this.webssologoffurl;
    }

    public void setWebssologoffurl(String webssologoffurl) {
        if (webssologoffurl != null && webssologoffurl.length() > 0) {
            this.webssologoffurl = webssologoffurl;
        }
    }

    public String getWebssoesigurl() {
        return this.webssoesigurl;
    }

    public void setWebssoesigurl(String webssoesigurl) {
        this.webssoesigurl = webssoesigurl;
    }

    public String getUseridattributename() {
        return this.useridattributename;
    }

    public void setUseridattributename(String useridattributename) {
        this.useridattributename = useridattributename;
    }

    public boolean isAllowHeaderAttributes() {
        return this.allowHeaderAttributes;
    }

    public void setAllowHeaderAttributes(boolean allowHeaderAttributes) {
        this.allowHeaderAttributes = allowHeaderAttributes;
    }

    public String getUserid(HttpServletRequest request) {
        String remoteUser = request.getRemoteUser();
        if (remoteUser == null && this.useridattributename.length() > 0) {
            remoteUser = (String)request.getAttribute(this.useridattributename);
            if (remoteUser == null && this.useridattributename.indexOf("AJP_") != 0) {
                remoteUser = (String)request.getAttribute("AJP_" + this.useridattributename);
            }
            if (remoteUser == null && this.allowHeaderAttributes) {
                remoteUser = request.getHeader(this.useridattributename);
            }
        }
        if (remoteUser != null && remoteUser.indexOf("\\") > 0) {
            remoteUser = remoteUser.substring(remoteUser.indexOf("\\") + 1);
        }
        return remoteUser;
    }

    public PropertyList getSSOAttributes(HttpServletRequest request) {
        PropertyList ssoattributes = new PropertyList();
        if (this.allowHeaderAttributes) {
            Enumeration requestheadernames = request.getHeaderNames();
            while (requestheadernames.hasMoreElements()) {
                String ssoattributename = (String)requestheadernames.nextElement();
                String ssoattributevalue = request.getHeader(ssoattributename);
                Trace.logDebug("SSO Request attribute " + ssoattributename + " has value: " + ssoattributevalue);
                ssoattributes.setProperty(ssoattributename, ssoattributevalue);
            }
        } else {
            Enumeration requestnames = request.getSession().getAttributeNames();
            while (requestnames.hasMoreElements()) {
                String ssoattributename = (String)requestnames.nextElement();
                String ssoattributevalue = request.getHeader(ssoattributename);
                Trace.logDebug("SSO Request header " + ssoattributename + " has value: " + ssoattributevalue);
                ssoattributes.setProperty(ssoattributename, ssoattributevalue);
            }
        }
        return ssoattributes;
    }

    @Override
    public String validateRequest(HttpServletRequest request) {
        return "";
    }

    @Override
    public String validateRequest(HttpServletRequest request, DataSet sysuser) {
        return "";
    }

    public void handleLVLogonError(String errorMessage, HttpServletResponse response) {
        try {
            response.getWriter().write("<p style=\"color:red\">" + errorMessage + "</p>");
        }
        catch (IOException ioe) {
            Trace.logError(ioe.getMessage());
        }
    }

    public void logoff(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.sendRedirect(this.getWebssologoffurl());
        }
        catch (Exception e) {
            Trace.logError("Error redirect to " + this.getWebssologoffurl(), e);
        }
    }
}

