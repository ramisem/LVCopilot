/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class ParseDateTime
extends BaseAjaxRequest {
    private static final Set<String> STANDARD_FORMATS = new HashSet<String>(Arrays.asList("S", "M", "L", "S S", "S M", "S L", "M S", "M M", "M L", "L S", "L M", "L L"));

    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        String userTimeZoneStr;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String fieldid = ar.getRequestParameter("fieldid", "");
        String datetime = ar.getRequestParameter("value", "");
        String dateonly = ar.getRequestParameter("dateonly", "smart").toLowerCase();
        String dateformat = ar.getRequestParameter("dateformat", "");
        String datasource = ar.getRequestParameter("datasource", "");
        String rownum = ar.getRequestParameter("rownum", "");
        String columnid = ar.getRequestParameter("columnid", "");
        String retval = "";
        String userLocaleStr = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getLocale();
        if (datetime.length() == 6 && Utils.isInt(datetime)) {
            if ("fi_FI".equals(userLocaleStr)) {
                datetime = datetime.substring(0, 2) + "." + datetime.substring(2, 4) + ".20" + datetime.substring(4, 6);
            } else if ("sv_SE".equals(userLocaleStr)) {
                datetime = "20" + datetime.substring(0, 2) + "-" + datetime.substring(2, 4) + "-" + datetime.substring(4, 6);
            }
        }
        DateTimeUtil dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        dtu.setTimeZone(TimeZone.getDefault());
        Calendar c = dtu.getCalendar(datetime);
        if (datetime.toLowerCase().startsWith("n") && c != null && (userTimeZoneStr = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getTimeZone()) != null && !userTimeZoneStr.equals("") && !userTimeZoneStr.equals(TimeZone.getDefault().getDisplayName())) {
            TimeZone userTimeZone = TimeZone.getTimeZone(userTimeZoneStr);
            TimeZone serverTimeZone = TimeZone.getDefault();
            c.add(14, serverTimeZone.getRawOffset() * -1);
            if (serverTimeZone.inDaylightTime(c.getTime())) {
                c.add(14, c.getTimeZone().getDSTSavings() * -1);
            }
            c.add(14, userTimeZone.getRawOffset());
            if (userTimeZone.inDaylightTime(c.getTime())) {
                c.add(14, userTimeZone.getDSTSavings());
            }
        }
        if (c != null) {
            boolean containsTimeComponent = c.get(11) + c.get(12) + c.get(13) > 0;
            try {
                DateFormat dt;
                try {
                    PropertyList policy = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom");
                    String formatFromPolicy = dateonly.equals("dateonly") || dateonly.equals("smart") && !containsTimeComponent ? policy.getProperty("defaultdateonlyformat", "") : policy.getProperty("defaultdateformat", "");
                    if (dateformat.equals("") && !formatFromPolicy.equals("") && !STANDARD_FORMATS.contains(formatFromPolicy)) {
                        dateformat = formatFromPolicy;
                    }
                }
                catch (SapphireException policy) {
                    // empty catch block
                }
                if (!dateformat.equals("")) {
                    dt = new SimpleDateFormat(dateformat);
                } else if (userLocaleStr != null && !userLocaleStr.equals("")) {
                    Locale userLocale;
                    if (userLocaleStr.contains("_")) {
                        String country = userLocaleStr.substring(0, userLocaleStr.indexOf(95));
                        String language = userLocaleStr.substring(userLocaleStr.indexOf(95) + 1);
                        userLocale = new Locale(country, language);
                    } else {
                        userLocale = new Locale(userLocaleStr);
                    }
                    dt = dateonly.equals("dateonly") || dateonly.equals("smart") && !containsTimeComponent ? DateFormat.getDateInstance(3, userLocale) : DateFormat.getDateTimeInstance(3, 3, userLocale);
                } else {
                    dt = dateonly.equals("dateonly") || dateonly.equals("smart") && !containsTimeComponent ? dtu.getDefaultDateOnlyFormat() : dtu.getDefaultDateFormat();
                }
                retval = dt.format(c.getTime());
            }
            catch (Exception e) {
                retval = "";
            }
        }
        ar.addCallbackArgument("fieldid", fieldid);
        ar.addCallbackArgument("formatteddate", retval);
        ar.addCallbackArgument("datasource", datasource);
        ar.addCallbackArgument("rownum", rownum);
        ar.addCallbackArgument("columnid", columnid);
        ar.print();
    }
}

