/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class CommonParamMap
extends HashMap {
    public static final String REPORT_ROOT = "SAPPHIRE_ReportRoot";
    public static final String REPORT_USER = "SAPPHIRE_CurrentUser";
    public static final String psREPORT_USER = "psSAPPHIRE_CurrentUser";
    public static final String pmREPORT_USER = "pmSAPPHIRE_CurrentUser";
    public static final String REPORT_DATABASE = "SAPPHIRE_DatabaseName";
    public static final String REPORT_USER_TIMEZONE = "SAPPHIRE_UserTimeZone";
    public static final String REPORT_USER_LOCALE = "SAPPHIRE_UserLocale";
    public static final String REPORT_USER_TIMEZONE_OFFSET = "SAPPHIRE_UserTimeZoneOffset";
    public static final String REPORT_PATH = "SAPPHIRE_ReportPath";
    public static final String REPORT_ID = "SAPPHIRE_ReportID";
    public static final String REPORT_TITLE = "SAPPHIRE_ReportTitle";
    public static final String REPORT_VERSION = "SAPPHIRE_ReportVersion";
    public static final String REPORT_EVENT_ID = "SAPPHIRE_ReportEventID";
    public static final String psREPORT_EVENT_ID = "psSAPPHIRE_ReportEventID";
    public static final String pmREPORT_EVENT_ID = "pmSAPPHIRE_ReportEventID";
    public static final String BASE_REPORT_EVENT_ID = "SAPPHIRE_BaseReportEventID";
    public static final String psBASE_REPORT_EVENT_ID = "psSAPPHIRE_BaseReportEventID";
    public static final String pmBASE_REPORT_EVENT_ID = "pmSAPPHIRE_BaseReportEventID";
    public static final String REPORT_EVENT_VERSIONID = "SAPPHIRE_ReportEventVersionID";
    public static final String psREPORT_EVENT_VERSIONID = "psSAPPHIRE_ReportEventVersionID";
    public static final String pmREPORT_EVENT_VERSIONID = "pmSAPPHIRE_ReportEventVersionID";
    public static final String PRIOR_REPORT_EVENT_ID = "SAPPHIRE_PriorReportEventID";
    public static final String psPRIOR_REPORT_EVENT_ID = "psSAPPHIRE_PriorReportEventID";
    public static final String pmPRIOR_REPORT_EVENT_ID = "pmSAPPHIRE_PriorReportEventID";
    public static final String REPORT_KEYID1VALUE = "SAPPHIRE_Keyid1Value";
    public static final String REPORT_KEYID2VALUE = "SAPPHIRE_Keyid2Value";
    public static final String REPORT_KEYID3VALUE = "SAPPHIRE_Keyid3Value";
    public static final String REPORT_SDCID = "SAPPHIRE_SDCID";
    public static final String psREPORT_SDCID = "psSAPPHIRE_SDCID";
    public static final String pmREPORT_SDCID = "pmSAPPHIRE_SDCID";
    public static final String REPORT_KEYID1 = "SAPPHIRE_KEYID1";
    public static final String psREPORT_KEYID1 = "psSAPPHIRE_KEYID1";
    public static final String pmREPORT_KEYID1 = "pmSAPPHIRE_KEYID1";
    public static final String REPORT_KEYID1List = "SAPPHIRE_KEYID1List";
    public static final String psREPORT_KEYID1List = "psSAPPHIRE_KEYID1List";
    public static final String pmREPORT_KEYID1List = "pmSAPPHIRE_KEYID1List";
    public static final String REPORT_KEYID2 = "SAPPHIRE_KEYID2";
    public static final String psREPORT_KEYID2 = "psSAPPHIRE_KEYID2";
    public static final String pmREPORT_KEYID2 = "pmSAPPHIRE_KEYID2";
    public static final String REPORT_KEYID3 = "SAPPHIRE_KEYID3";
    public static final String psREPORT_KEYID3 = "psSAPPHIRE_KEYID3";
    public static final String pmREPORT_KEYID3 = "pmSAPPHIRE_KEYID3";
    public static final String REPORT_RSETID = "SAPPHIRE_RSETID";
    public static final String SUBREPORT_DIR = "SUBREPORT_DIR";
    public static final String REPORT_CONNECTIONID = "SAPPHIRE_CONNECTIONID";
    public static final String psREPORT_CONNECTIONID = "psSAPPHIRE_CONNECTIONID";
    public static final String pmREPORT_CONNECTIONID = "pmSAPPHIRE_CONNECTIONID";
    public static final String REPORT_SELECTED_TIMEZONE = "SAPPHIRE_REPORT_TIMEZONE";
    public static final String REPORT_SELECTED_LANGUAGE = "SAPPHIRE_REPORT_LANGUAGE";
    public static final String REPORT_TRANSLATION_PROCESSOR = "translationProcessor";
    public static final String REPORT_LOCALE_OBJ = "SAPPHIRE_UserLocale_Obj";
    public static final String REPORT_TIMEZONE_OBJ = "SAPPHIRE_UserTimeZone_Obj";
    public static final String REPORT_ADDRESS_ID = "addressid";
    private static Set set = new HashSet();

    public CommonParamMap(PageContext pageContext) {
        this((HttpServletRequest)pageContext.getRequest());
    }

    public CommonParamMap(HashMap paramMap, ConnectionInfo connInfo) {
        this.init(paramMap, connInfo);
    }

    private void init(Map paramMap, ConnectionInfo connInfo) {
        String keyid3;
        String keyid2;
        TranslationProcessor tp = new TranslationProcessor(connInfo.getConnectionId());
        String reportTimezone = (String)paramMap.get("timezone");
        Object reportLanguage = paramMap.get("languageid");
        tp.setLanguage(reportLanguage != null ? reportLanguage.toString() : connInfo.getLanguage());
        this.put(REPORT_TRANSLATION_PROCESSOR, tp);
        this.put(REPORT_CONNECTIONID, connInfo.getConnectionId());
        this.put(psREPORT_CONNECTIONID, connInfo.getConnectionId());
        this.put(pmREPORT_CONNECTIONID, connInfo.getConnectionId());
        Locale userLocale = I18nUtil.getConnectionLocale(connInfo);
        TimeZone userTimeZone = I18nUtil.getConnectionTimeZone(connInfo);
        this.put(REPORT_LOCALE_OBJ, userLocale);
        this.put(REPORT_USER_LOCALE, userLocale.getDisplayName(userLocale));
        this.put(REPORT_TIMEZONE_OBJ, userTimeZone);
        this.put(REPORT_USER_TIMEZONE, userTimeZone.getDisplayName(userLocale));
        this.put(REPORT_USER, connInfo.getSysuserId());
        this.put(psREPORT_USER, connInfo.getSysuserId());
        this.put(pmREPORT_USER, connInfo.getSysuserId());
        this.put(REPORT_DATABASE, connInfo.getDatabaseId());
        this.put(REPORT_ROOT, SapphireJasperUtil.REPORT_ROOT);
        this.put(REPORT_SELECTED_TIMEZONE, OpalUtil.isNotEmpty(reportTimezone) ? reportTimezone.trim() : (OpalUtil.isNotEmpty(connInfo.getTimeZone()) ? connInfo.getTimeZone().trim() : this.getSystemTimeZone(connInfo)));
        this.put(REPORT_SELECTED_LANGUAGE, reportLanguage != null ? reportLanguage.toString() : (OpalUtil.isNotEmpty(connInfo.getLanguage()) ? connInfo.getLanguage() : ""));
        this.put(REPORT_ADDRESS_ID, paramMap.get(REPORT_ADDRESS_ID));
        this.put(REPORT_SDCID, paramMap.get("sdcid"));
        this.put(psREPORT_SDCID, paramMap.get("sdcid"));
        this.put(pmREPORT_SDCID, paramMap.get("sdcid"));
        Object o = paramMap.get("keyid1");
        String keyid1 = (String)paramMap.get("keyid1");
        if (keyid1 != null) {
            this.put(REPORT_KEYID1, keyid1);
            this.put(psREPORT_KEYID1, keyid1);
            this.put(pmREPORT_KEYID1, keyid1);
            if (keyid1.length() > 4000) {
                this.put(REPORT_KEYID1List, "rset");
                this.put(psREPORT_KEYID1List, "rset");
                this.put(pmREPORT_KEYID1List, "rset");
            } else {
                this.put(REPORT_KEYID1List, "'" + keyid1.replaceAll(";", "','") + "'");
                this.put(psREPORT_KEYID1List, "'" + keyid1.replaceAll(";", "','") + "'");
                this.put(pmREPORT_KEYID1List, "'" + keyid1.replaceAll(";", "','") + "'");
            }
        }
        if ((keyid2 = (String)paramMap.get("keyid2")) != null) {
            this.put(REPORT_KEYID2, keyid2);
            this.put(psREPORT_KEYID2, keyid2);
            this.put(pmREPORT_KEYID2, keyid2);
        }
        if ((keyid3 = (String)paramMap.get("keyid3")) != null) {
            this.put(REPORT_KEYID3, keyid3);
            this.put(psREPORT_KEYID3, keyid3);
            this.put(pmREPORT_KEYID3, keyid3);
        }
    }

    private String getSystemTimeZone(ConnectionInfo connInfo) {
        TimeZone tz = I18nUtil.getConnectionTimeZone(connInfo);
        boolean isDayLightSaving = tz.inDaylightTime(new Date());
        String timezoneid = tz.getDisplayName(isDayLightSaving, 0);
        return timezoneid;
    }

    public CommonParamMap(HttpServletRequest request) {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        ConnectionInfo connInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        String regenerateflag = request.getParameter("regenerate");
        HashMap<String, String> paramMap = new HashMap<String, String>();
        if (regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
            DataSet ds = (DataSet)request.getAttribute("paramvalueds");
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String paramid = ds.getString(i, "paramid");
                    String paramvalue = ds.getClob(i, "paramvalueclob", "");
                    if (paramvalue != null && paramvalue.length() > 0 && paramvalue.indexOf(",") >= 0) {
                        paramvalue = paramvalue.replaceAll(",", ";");
                    }
                    paramMap.put(paramid, paramvalue);
                }
            }
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramKey = (String)e.nextElement();
                if (!paramKey.equalsIgnoreCase("timezone") && !paramKey.equalsIgnoreCase("languageid")) continue;
                paramMap.put(paramKey, request.getParameter(paramKey));
            }
        } else {
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramKey = (String)e.nextElement();
                paramMap.put(paramKey, request.getParameter(paramKey));
            }
        }
        this.init(paramMap, connInfo);
    }

    public CommonParamMap(Map paramMap, String connectionid) {
        ConnectionInfo connInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        this.init(paramMap, connInfo);
    }

    public static boolean isCommonParam(String paramname) {
        return set.contains(paramname);
    }

    public static String substituteValue(String value, HashMap cparamsMap) {
        if (value != null && value.length() > 0) {
            if (value.equalsIgnoreCase("[keyid1]")) {
                value = (String)cparamsMap.get(REPORT_KEYID1);
            } else if (value.equalsIgnoreCase("[keyid2]")) {
                value = (String)cparamsMap.get(REPORT_KEYID2);
            } else if (value.equalsIgnoreCase("[keyid3]")) {
                value = (String)cparamsMap.get(REPORT_KEYID3);
            } else if (value.equalsIgnoreCase("[sdcid]")) {
                value = (String)cparamsMap.get(REPORT_SDCID);
            }
        }
        return value;
    }

    static {
        set.add(REPORT_ROOT);
        set.add(REPORT_PATH);
        set.add(REPORT_SDCID);
        set.add(psREPORT_SDCID);
        set.add(pmREPORT_SDCID);
        set.add(REPORT_KEYID1);
        set.add(psREPORT_KEYID1);
        set.add(pmREPORT_KEYID1);
        set.add(REPORT_KEYID1List);
        set.add(psREPORT_KEYID1List);
        set.add(pmREPORT_KEYID1List);
        set.add(REPORT_KEYID2);
        set.add(psREPORT_KEYID2);
        set.add(pmREPORT_KEYID2);
        set.add(REPORT_KEYID3);
        set.add(psREPORT_KEYID3);
        set.add(pmREPORT_KEYID3);
        set.add(REPORT_ID);
        set.add(REPORT_TITLE);
        set.add(REPORT_VERSION);
        set.add(REPORT_USER);
        set.add(psREPORT_USER);
        set.add(pmREPORT_USER);
        set.add(REPORT_RSETID);
        set.add(REPORT_DATABASE);
        set.add(REPORT_EVENT_ID);
        set.add(psREPORT_EVENT_ID);
        set.add(pmREPORT_EVENT_ID);
        set.add(BASE_REPORT_EVENT_ID);
        set.add(psBASE_REPORT_EVENT_ID);
        set.add(pmBASE_REPORT_EVENT_ID);
        set.add(REPORT_EVENT_VERSIONID);
        set.add(psREPORT_EVENT_VERSIONID);
        set.add(pmREPORT_EVENT_VERSIONID);
        set.add(PRIOR_REPORT_EVENT_ID);
        set.add(psPRIOR_REPORT_EVENT_ID);
        set.add(pmPRIOR_REPORT_EVENT_ID);
        set.add(SUBREPORT_DIR);
        set.add(REPORT_SELECTED_TIMEZONE);
        set.add(REPORT_SELECTED_LANGUAGE);
    }
}

