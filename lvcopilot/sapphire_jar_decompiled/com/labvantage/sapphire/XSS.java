/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.DateTimeUtil;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class XSS
extends BaseAjaxRequest {
    private static boolean showAlerts = false;
    private static boolean showAlertAlways = false;
    private static boolean mock = false;
    private static HashSet<String> whitepageSet = null;
    private static ThreadLocal<String> threadLocal = null;
    private static String excludecolumnList = "sdcid;columnid;rsetid;connectionid;propertytreeid;propertytreetype;extendnodeid;webpageid;elementid;propertyid;editorstyleid;paramlistitem.editorstyleid;datatype;paramtype;R: Gender;R: YesNo;sysuserid;profileid;bulletinid;versionstatus;createby;createtool;modby;modtool;securitydepartment;securityuser;";

    public static void setMock(boolean mock) {
        XSS.mock = mock;
    }

    public static void setShowAlerts(boolean showAlerts) {
        XSS.showAlerts = showAlerts;
    }

    public static void setShowAlertsAlways(boolean showAlerts) {
        showAlertAlways = showAlerts;
    }

    public static void setWhitepageSet(DataSet ds) {
        whitepageSet = new HashSet();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            whitepageSet.add(ds.getValue(i, "whitepageurl"));
        }
    }

    public static void setThreadPageURL(String pageURL) {
        if (XSS.isMock()) {
            threadLocal = new ThreadLocal();
            threadLocal.set(pageURL);
        }
    }

    public static void addWhitepage(String pageurl) {
    }

    public static boolean isMock() {
        return mock;
    }

    public static boolean isExcludedColumn(String columnid) {
        if (columnid == null) {
            columnid = "";
        }
        return excludecolumnList.indexOf(columnid + ";") >= 0;
    }

    public static String mock(String val) {
        if (!mock) {
            return val;
        }
        return val + "<script>x('" + val + "')</script>";
    }

    public static String mock(String val, String extra) {
        String url;
        if (!mock || XSS.isExcludedColumn(extra) || extra.endsWith("flag")) {
            return val;
        }
        if (threadLocal != null && (url = threadLocal.get()) != null && whitepageSet.contains(url)) {
            return val;
        }
        if (extra.startsWith("R:") || extra.endsWith("desc") || extra.endsWith("notes") || extra.endsWith("label") || extra.endsWith("type")) {
            if (val == null) {
                val = "";
            }
            if (val.indexOf("<script>x(") >= 0) {
                return val;
            }
            return val + "<script>x('" + val + "qq', '" + extra + "pp')</script>";
        }
        return val;
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String val = ar.getRequestParameter("val");
        String extra = ar.getRequestParameter("extra");
        String lasturl = ar.getRequestParameter("lasturl");
        String whitepageurl = ar.getRequestParameter("whitepageurl");
        if (val != null && val.length() > 0 || extra != null && extra.length() > 0) {
            try {
                if (this.getQueryProcessor().getPreparedCount("select count(*) from xsslog WHERE val=? and extra=?", new String[]{val, extra}) == 0) {
                    this.getQueryProcessor().execPreparedUpdate("insert into xsslog(val, extra, lasturl,logdt) values (? ,?,?,? )", new Object[]{val, extra, lasturl, DateTimeUtil.getNowTimestamp()});
                    if (showAlerts) {
                        ar.addCallbackArgument("message", val + " (" + extra + ")");
                    }
                }
                if (showAlertAlways) {
                    ar.addCallbackArgument("message", val + " (" + extra + ")");
                }
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
        ar.print();
    }
}

