/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.Cookie
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class HttpUtil
extends BaseClass {
    public static final String UTF8 = "UTF-8";
    public static final String STANDARD_ENCODE = "UTF-8";
    private PageContext pagecontext = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    public HttpUtil() {
    }

    public HttpUtil(PageContext pagecontext) {
        this.pagecontext = pagecontext;
        this.request = (HttpServletRequest)pagecontext.getRequest();
        this.response = (HttpServletResponse)pagecontext.getResponse();
    }

    public HttpUtil(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpUtil(HttpServletResponse response) {
        this.response = response;
    }

    public boolean existCookie(String name) {
        Cookie[] cookies;
        boolean rc = false;
        if (this.request != null && (cookies = this.request.getCookies()) != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (!cookies[i].getName().equalsIgnoreCase(name)) continue;
                rc = true;
                break;
            }
        }
        return rc;
    }

    public void removeCookie(String name) {
        Cookie[] cookies;
        if (this.request != null && this.response != null && (cookies = this.request.getCookies()) != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (!cookies[i].getName().equalsIgnoreCase(name)) continue;
                cookies[i].setValue("");
                cookies[i].setMaxAge(0);
                this.response.addCookie(cookies[i]);
                break;
            }
        }
    }

    public void setCookieValue(String name, String value) {
        this.setCookieValue(name, value, false);
    }

    public void setCookieValue(String name, String value, boolean permenant) {
        this.setCookieValue(name, value, permenant, false);
    }

    public void setCookieValue(String name, String value, boolean permenant, boolean httponly) {
        if ("connectionid".equals(name)) {
            this.setCookieHeader(name, value, httponly);
        } else if (this.response != null) {
            if ("*connectionid".equals(name)) {
                name = "connectionid";
            }
            Cookie cookie = null;
            try {
                cookie = new Cookie(name, URLEncoder.encode(value, "UTF-8"));
                cookie.setHttpOnly(httponly);
                if (this.request.isSecure() || "https".equalsIgnoreCase(this.request.getScheme())) {
                    cookie.setSecure(true);
                }
                if ("connectionid".equals(name) || "webSSOEsigURL".equals(name)) {
                    cookie.setPath(this.request.getContextPath());
                }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (permenant) {
                cookie.setMaxAge(Integer.MAX_VALUE);
            }
            this.response.addCookie(cookie);
        }
    }

    public void setCookieHeader(String name, String value, boolean httponly) {
        this.setCookieHeader(name, value, false, httponly);
    }

    public void setCookieHeader(String name, String value, boolean permanent, boolean httponly) {
        if (this.response != null) {
            StringBuilder cookieString = new StringBuilder();
            try {
                cookieString.append(URLEncoder.encode(name, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
                if (httponly) {
                    cookieString.append("; HttpOnly");
                }
                if (this.request.isSecure() || "https".equalsIgnoreCase(this.request.getScheme())) {
                    cookieString.append("; Secure");
                }
                cookieString.append("; Path=").append(this.request.getContextPath());
                Browser browser = new Browser(this.request);
                if (!browser.isSafari() || browser.getOS() != 3) {
                    cookieString.append("; SameSite=Strict");
                }
                if (permanent) {
                    cookieString.append("; Max-Age=2147483647");
                }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            this.response.addHeader("Set-Cookie", cookieString.toString());
        }
    }

    public String getCookieValue(String name) {
        Cookie[] cookies;
        String rc = "";
        if (this.response != null && (cookies = this.request.getCookies()) != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (!cookies[i].getName().equalsIgnoreCase(name) || cookies[i].getValue().equalsIgnoreCase("null")) continue;
                rc = HttpUtil.decodeURIComponent(StringUtil.replaceAll(cookies[i].getValue(), "+", " "));
                break;
            }
        }
        return rc;
    }

    public HashMap getCookies() {
        Cookie[] cookies;
        HashMap<String, String> map = new HashMap<String, String>();
        if (this.response != null && (cookies = this.request.getCookies()) != null) {
            for (int i = 0; i < cookies.length; ++i) {
                String name = cookies[i].getName();
                String value = cookies[i].getValue();
                if (name == null || value == null || value.length() <= 0) continue;
                map.put(name, HttpUtil.decodeURIComponent(value));
            }
        }
        return map;
    }

    public void goErrorPage(String errorpage, String errormsg) {
        if (errorpage == null || errorpage.length() == 0) {
            errorpage = this.pagecontext.getServletContext().getInitParameter("errorpage");
        }
        if (this.response.isCommitted()) {
            try {
                PrintWriter out = this.response.getWriter();
                ((Writer)out).write(errormsg);
            }
            catch (Exception e) {
                this.pagecontext.getServletContext().log("Could not write the error message (" + errormsg + "): " + e.getMessage());
            }
        } else if (errorpage != null && errorpage.length() > 0) {
            String rc = this.request.getContextPath() + "/" + this.pagecontext.getServletContext().getInitParameter("RequestControllerName") + "?file=";
            try {
                if (Trace.on) {
                    this.pagecontext.getServletContext().log("Forwarding to the page: " + rc + errorpage + "&errormsg=" + errormsg);
                }
                this.response.sendRedirect(rc + errorpage + "&errormsg=" + HttpUtil.encodeURIComponent(errormsg));
            }
            catch (IOException e) {
                this.pagecontext.getServletContext().log("Failed to forward to error page: " + e.getMessage());
            }
        }
    }

    public static String getAppRoot(ServletContext servletContext) {
        try {
            return Configuration.getInstance().getAppRoot(servletContext);
        }
        catch (SapphireException e) {
            return "";
        }
    }

    public static String getWebAppRoot(ServletContext servletContext) {
        try {
            return Configuration.getInstance().getWebAppRoot(servletContext);
        }
        catch (SapphireException e) {
            return "";
        }
    }

    public static String getSapphireHome() {
        try {
            return Configuration.getInstance().getSapphireHome();
        }
        catch (SapphireException e) {
            return "";
        }
    }

    public static String getApplicationHome() {
        try {
            return Configuration.getInstance().getApplicationHome();
        }
        catch (SapphireException e) {
            return "";
        }
    }

    public static PropertyList getRequestPropertyList(ServletRequest request) {
        PropertyList props = new PropertyList();
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            props.setProperty(name, request.getParameter(name));
        }
        return props;
    }

    public static HashMap getRequestMap(ServletRequest request) {
        HashMap<String, String> props = new HashMap<String, String>();
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            props.put(name, request.getParameter(name));
        }
        return props;
    }

    public static HashMap<String, String> getQueryStringMap(URL url) {
        String[] pairs;
        HashMap<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        for (String pair : pairs = query.split("&")) {
            int idx = pair.indexOf("=");
            query_pairs.put(HttpUtil.decodeURIComponent(pair.substring(0, idx), "UTF-8"), HttpUtil.decodeURIComponent(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public static void setRequestVariables(HttpServletRequest request, String varName, PropertyList propertyList) {
        request.setAttribute(varName, (Object)propertyList);
        Set keySet = propertyList.keySet();
        StringBuffer treeList = new StringBuffer("");
        for (String propertyId : keySet) {
            Object value = propertyList.get(propertyId);
            if (value instanceof PropertyList) {
                treeList.append(";").append(propertyId);
                request.setAttribute(propertyId, (Object)propertyList.getPropertyList(propertyId));
                continue;
            }
            if (!(value instanceof PropertyListCollection)) continue;
            treeList.append(";").append(propertyId);
            request.setAttribute(propertyId, (Object)propertyList.getCollection(propertyId));
        }
        propertyList.setProperty("propertytreelist", treeList.length() > 0 ? treeList.substring(1) : "");
    }

    public static String appendRequestParameters(PageContext pageContext, String url) {
        StringBuffer src = new StringBuffer(url);
        boolean first = true;
        Enumeration e = pageContext.getRequest().getParameterNames();
        while (e.hasMoreElements()) {
            String propertyid = (String)e.nextElement();
            src.append(first && url.indexOf("?") == -1 ? "?" : "&").append(propertyid).append("=").append(pageContext.getRequest().getParameter(propertyid));
            first = false;
        }
        return src.toString();
    }

    public static String getConnectionId(PageContext pageContext) {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        if (requestContext != null) {
            return requestContext.getConnectionId();
        }
        return "";
    }

    public static com.labvantage.sapphire.services.ConnectionInfo getConnectionInfo(PageContext pageContext) {
        if (pageContext != null) {
            if (pageContext.getAttribute("sapphire_connectionInfo") == null) {
                String connectionid = HttpUtil.getConnectionId(pageContext);
                if (connectionid != null && connectionid.length() > 0) {
                    ConnectionProcessor cp = new ConnectionProcessor(pageContext);
                    if (cp != null && cp.getConnectionid() != null && cp.getConnectionid().length() > 0) {
                        ConnectionInfo ci = cp.getConnectionInfo(connectionid);
                        pageContext.setAttribute("sapphire_connectionInfo", (Object)ci);
                        return ci;
                    }
                    return null;
                }
                return null;
            }
            return (com.labvantage.sapphire.services.ConnectionInfo)pageContext.getAttribute("sapphire_connectionInfo");
        }
        return null;
    }

    public static String getNameServerList(PageContext pageContext) {
        return "";
    }

    public static String getEncryptionJS() {
        return HttpUtil.getEncryptionJS("");
    }

    public static String getEncryptionJS(boolean useFullIncludes) {
        return HttpUtil.getEncryptionJS("", useFullIncludes);
    }

    public static String getGWTEncryptionJS(File consoleConfigFile) {
        StringBuffer out = new StringBuffer();
        out.append("<script language=\"JavaScript\" src=\"").append("WEB-CORE/scripts/sapphirersaencrypt.js\"></script>");
        out.append("<script language=\"JavaScript\">");
        out.append("var rsa = new RSAKey();");
        out.append("rsa.setPublic('").append(EncryptDecrypt.getPublicKey(consoleConfigFile)).append("','10001');");
        out.append("rsa.setPrefix('{|}');");
        out.append("</script>");
        return out.toString();
    }

    public static String getEncryptionJS(String pathpreifx) {
        return HttpUtil.getEncryptionJS(pathpreifx, false);
    }

    public static String getEncryptionJS(String pathpreifx, boolean useFullIncludes) {
        StringBuffer out = new StringBuffer();
        out.append("<script language=\"JavaScript\" src=\"").append(pathpreifx).append(com.labvantage.sapphire.util.http.HttpUtil.getScript("WEB-CORE/scripts/sapphirersaencrypt.js", useFullIncludes)).append("\"></script>");
        out.append("<script language=\"JavaScript\">");
        out.append("var rsa = new RSAKey();");
        out.append("rsa.setPublic('").append(EncryptDecrypt.getPublicKey()).append("','10001');");
        out.append("rsa.setPrefix('{|}');");
        out.append("rsa.setChunkSeparator('[!@]');");
        out.append("</script>");
        return out.toString();
    }

    public static String encode(String str) {
        String[] thex = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String encStr = "";
        for (int i = 0; i < str.length(); ++i) {
            char unicodeval = str.charAt(i);
            encStr = encStr + thex[unicodeval >> 12] + thex[unicodeval >> 8 & 0xF] + thex[unicodeval >> 4 & 0xF] + thex[unicodeval & 0xF];
        }
        return encStr;
    }

    public static String decode(String str) {
        byte[] b = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            b[i / 2] = (byte)Integer.parseInt(str.substring(i, i + 2), 16);
        }
        String s = null;
        try {
            s = new String(b, "UTF-16BE");
        }
        catch (UnsupportedEncodingException e) {
            Logger.logStackTrace(e);
        }
        return s;
    }

    public static TimeZone getSessionTimeZone(PageContext pageContext) {
        return I18nUtil.getSessionTimeZone(pageContext);
    }

    public static Locale getSessionLocale(PageContext pageContext) {
        return I18nUtil.getSessionLocale(pageContext);
    }

    public static String encodeURIComponent(String component) {
        return HttpUtil.encodeURIComponent(component, "UTF-8");
    }

    public static String encodeURIComponent(String component, String encoding) {
        String result;
        try {
            result = URLEncoder.encode(component, encoding).replaceAll("\\+", "%20").replaceAll("\\%27", "'").replaceAll("\\%21", "!").replaceAll("\\%7E", "~");
        }
        catch (UnsupportedEncodingException e) {
            result = component;
        }
        return result;
    }

    public static String decodeURIComponent(String component) {
        return HttpUtil.decodeURIComponent(component, "UTF-8");
    }

    public static String decodeURIComponent(String component, String encoding) {
        if (component != null) {
            String result;
            try {
                if (component != null) {
                    component = StringUtil.replaceAll(component, "+", "%2B");
                }
                result = URLDecoder.decode(component, encoding);
            }
            catch (IllegalArgumentException ea) {
                result = component;
            }
            catch (UnsupportedEncodingException e) {
                result = component;
            }
            return result;
        }
        return "";
    }

    public static String decrypt(String text) {
        return EncryptDecrypt.decryptRSA(text);
    }
}

