/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class I18nUtil {
    private static final Locale sysLocale = Locale.getDefault();
    private static final String sysdecimalSep = FormatUtil.getInstance(sysLocale).getDecimalSeparator() + "";
    private static final TimeZone sysTimeZone = TimeZone.getDefault();
    private static final Pattern numberPattern = Pattern.compile("(((-|\\+)?[0-9]+(" + sysdecimalSep + "[0-9]+)?)+(e|E)(-|\\+)[0-9]+)|((-|\\+)?[0-9]+(" + sysdecimalSep + "[0-9]+)?)+");

    public static String convertToSysNumberString(String number, ConnectionInfo connectionInfo) throws NumberFormatException {
        FormatUtil formatUtil = FormatUtil.getInstance(I18nUtil.getConnectionLocale(connectionInfo));
        return FormatUtil.getInstance().format(formatUtil.parseBigDecimal(number));
    }

    public static int getSysTimeZoneOffSet(TimeZone clientTZ) {
        int cTZoffset = clientTZ.getOffset(Calendar.getInstance().getTimeInMillis());
        int sTZoffset = sysTimeZone.getOffset(Calendar.getInstance().getTimeInMillis());
        return (cTZoffset - sTZoffset) / 60000;
    }

    public static int getSessionTimeZoneOffSet(PageContext pageContext, TimeZone compareTimeZone) {
        int cTZoffset = compareTimeZone.getOffset(Calendar.getInstance().getTimeInMillis());
        int sTZoffset = I18nUtil.getSessionTimeZone(pageContext).getOffset(Calendar.getInstance().getTimeInMillis());
        return (cTZoffset - sTZoffset) / 60000;
    }

    public static ZoneId getZoneIdFromString(String timezoneid) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezoneid);
        }
        catch (Exception e) {
            zoneId = ZoneId.of(timezoneid, ZoneId.SHORT_IDS);
        }
        return zoneId;
    }

    public static Locale getSysLocale() {
        return sysLocale;
    }

    public static TimeZone getSysTimeZone() {
        return sysTimeZone;
    }

    public static Locale getSessionLocale(PageContext pageContext) {
        return I18nUtil.getSessionLocale((HttpServletRequest)pageContext.getRequest());
    }

    public static TimeZone getSessionTimeZone(PageContext pageContext) {
        return I18nUtil.getSessionTimeZone((HttpServletRequest)pageContext.getRequest());
    }

    public static Locale getSessionLocale(HttpServletRequest request) {
        Locale locale = (Locale)request.getSession().getAttribute("sapphireuser_locale");
        if (locale == null) {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            locale = I18nUtil.getLocaleFromString(requestContext.getPropertyList().getProperty("locale"));
            request.getSession().setAttribute("sapphireuser_locale", (Object)locale);
        }
        return locale;
    }

    public static ArrayList<String> getSessionTimeList(PageContext pageContext, DateFormat timeFormatter, int interval) {
        ArrayList<String> timeList = (ArrayList<String>)pageContext.getSession().getAttribute("sapphireuser_timeList");
        if (timeList == null && timeFormatter != null) {
            timeList = new ArrayList<String>();
            int count = 1440 / interval;
            Calendar c = Calendar.getInstance(I18nUtil.getSessionTimeZone(pageContext), I18nUtil.getSessionLocale(pageContext));
            c.set(13, 0);
            for (int i = 0; i < count; ++i) {
                int minuteOfDay = i * interval;
                int hourOfDay = minuteOfDay / 60;
                int minute = minuteOfDay % 60;
                c.set(9, 0);
                c.set(10, hourOfDay);
                c.set(12, minute);
                timeList.add(minuteOfDay + "|" + timeFormatter.format(c.getTime()));
            }
            pageContext.getSession().setAttribute("sapphireuser_timeList", timeList);
        }
        return timeList;
    }

    public static Locale getLocaleFromString(String localeStr) {
        Locale locale;
        block7: {
            locale = null;
            if (localeStr != null) {
                try {
                    locale = Locale.forLanguageTag(localeStr);
                    if (locale.getDisplayName().length() != 0) break block7;
                    String[] localeStrs = StringUtil.split(localeStr, "_");
                    if (localeStrs.length == 2) {
                        locale = new Locale(localeStrs[0], localeStrs[1]);
                        break block7;
                    }
                    if (localeStrs.length == 3) {
                        locale = new Locale(localeStrs[0], localeStrs[1] + "_" + localeStrs[2]);
                        break block7;
                    }
                    if (localeStrs.length == 1 && localeStrs[0].length() > 0) {
                        locale = new Locale(localeStrs[0]);
                        break block7;
                    }
                    locale = Locale.getDefault();
                }
                catch (Exception e) {
                    locale = Locale.getDefault();
                }
            } else {
                locale = Locale.getDefault();
            }
        }
        return locale;
    }

    public static TimeZone getSessionTimeZone(HttpServletRequest request) {
        TimeZone timezone = (TimeZone)request.getSession().getAttribute("sapphireuser_timezone");
        if (timezone == null) {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String timezoneid = requestContext.getPropertyList().getProperty("timezone");
            timezone = timezoneid != null && timezoneid.length() > 0 ? TimeZone.getTimeZone(timezoneid) : TimeZone.getDefault();
            request.getSession().setAttribute("sapphireuser_timezone", (Object)timezone);
        }
        return timezone;
    }

    public static Locale getConnectionLocale(ConnectionInfo connectionInfo) {
        String locale = connectionInfo == null ? null : connectionInfo.getLocale();
        return I18nUtil.getLocaleFromString(locale);
    }

    public static TimeZone getConnectionTimeZone(ConnectionInfo connectionInfo) {
        String timezone = connectionInfo == null ? null : connectionInfo.getTimeZone();
        TimeZone timezoneObj = null;
        timezoneObj = timezone != null && timezone.length() > 0 ? TimeZone.getTimeZone(timezone) : TimeZone.getDefault();
        return timezoneObj;
    }

    public static String convertToQueryDateString(String datetime, ConnectionInfo connectionInfo) {
        return I18nUtil.convertToQueryDateString(datetime, connectionInfo, true);
    }

    public static String convertToQueryDateString(String datetime, ConnectionInfo connectionInfo, boolean isTimeZoneAware) {
        if (datetime == null || datetime.trim().length() == 0) {
            return datetime;
        }
        M18NUtil m18n = new M18NUtil(connectionInfo);
        Calendar cal = m18n.parseCalendar(datetime, isTimeZoneAware);
        if (cal != null) {
            return m18n.getSysQueryDateFormat().format(cal.getTime());
        }
        return null;
    }

    public static String formatDataEntryDisplay(String displayvalue, String datatype, BigDecimal transformvalue, Calendar transformdt, String displayformat, String displayvalueformat, Locale userLocale, TimeZone userTimeZone, M18NUtil m18nUtil) {
        FormatUtil formatutil = FormatUtil.getInstance(userLocale);
        String locdecimalSep = "" + formatutil.getDecimalSeparator();
        if ("A".equals(datatype)) {
            if (transformvalue != null) {
                datatype = "N";
            } else if (transformdt != null) {
                datatype = "D";
            }
        }
        boolean isLiteral = false;
        if (displayvalueformat != null && displayvalueformat.length() > 0) {
            if ("[literal]".equals(displayvalueformat)) {
                isLiteral = true;
            } else {
                displayformat = displayvalueformat;
            }
        }
        if (!userLocale.equals(sysLocale) && !isLiteral && ("N".equals(datatype) || "NC".equals(datatype))) {
            String numoperator = "";
            if (displayvalue != null && displayvalue.length() >= 1) {
                String localizedDisplay;
                block30: {
                    boolean hasGroupSeparatorInDisplay;
                    switch (displayvalue.charAt(0)) {
                        case '<': 
                        case '>': {
                            numoperator = displayvalue.length() > 1 && displayvalue.charAt(1) == '=' ? displayvalue.substring(0, 2) : displayvalue.substring(0, 1);
                        }
                    }
                    localizedDisplay = NumericFormatter.formatNumber(transformvalue, displayformat, userLocale, displayvalue);
                    boolean bl = hasGroupSeparatorInDisplay = displayformat != null && displayformat.indexOf("#,##") == 0 && localizedDisplay != null && localizedDisplay.indexOf(formatutil.getGroupingSeparator()) > 0;
                    if (!(hasGroupSeparatorInDisplay || sysdecimalSep.equals(formatutil.getDecimalSeparator() + "") || localizedDisplay == null || localizedDisplay.indexOf(sysdecimalSep) <= 0 || "AsEntered".equalsIgnoreCase(displayformat))) {
                        String numberStr = localizedDisplay;
                        switch (numberStr.charAt(0)) {
                            case '<': 
                            case '>': {
                                numberStr = numberStr.length() > 1 && numberStr.charAt(1) == '=' ? numberStr.substring(2) : numberStr.substring(1);
                            }
                        }
                        try {
                            formatutil.parseBigDecimal(StringUtil.replaceAll(numberStr, sysdecimalSep, formatutil.getDecimalSeparator() + ""));
                            localizedDisplay = StringUtil.replaceAll(localizedDisplay, sysdecimalSep, formatutil.getDecimalSeparator() + "");
                        }
                        catch (Exception e) {
                            Matcher matcher = numberPattern.matcher(localizedDisplay);
                            String matched = "";
                            if (!matcher.find()) break block30;
                            matched = matcher.group();
                            String replaceMatched = StringUtil.replaceAll(matched, sysdecimalSep, locdecimalSep);
                            localizedDisplay = StringUtil.replaceAll(localizedDisplay, matched, replaceMatched);
                        }
                    }
                }
                if (localizedDisplay != null) {
                    if (localizedDisplay.indexOf(numoperator) >= 0) {
                        return localizedDisplay;
                    }
                    return numoperator + localizedDisplay;
                }
            }
        } else if (("D".equals(datatype) || "DC".equals(datatype)) && transformdt != null) {
            try {
                if (displayformat != null && displayformat.length() > 0 && !displayformat.equals("[defaultdateformat]") && !displayformat.equals("[defaultdateonlyformat]")) {
                    return DateFormatter.formatDateTime(transformdt, displayformat, userLocale, userTimeZone);
                }
                DateFormat df = "[defaultdateonlyformat]".equals(displayformat) ? m18nUtil.getDefaultDateOnlyFormat() : m18nUtil.getDefaultDateFormat();
                return df.format(transformdt.getTime());
            }
            catch (Throwable t) {
                Logger.logError("Failed to format data entry display", t);
            }
        } else if (("O".equals(datatype) || "OC".equals(datatype)) && transformdt != null) {
            try {
                if (displayformat != null && displayformat.length() > 0 && !displayformat.equals("[defaultdateformat]") && !displayformat.equals("[defaultdateonlyformat]")) {
                    return DateFormatter.formatDateTime(transformdt, displayformat, userLocale);
                }
                DateFormat df = "[defaultdateformat]".equals(displayformat) ? m18nUtil.getDefaultDateFormat(false) : m18nUtil.getDefaultDateOnlyFormat(false);
                return df.format(transformdt.getTime());
            }
            catch (Throwable t) {
                Logger.logError("Failed to format data entry display", t);
            }
        }
        return displayvalue;
    }

    public static void localizeDisplayValues(DataSet dataitem, PageContext pageContext) {
        I18nUtil.localizeDisplayValues(dataitem, HttpUtil.getConnectionInfo(pageContext));
    }

    public static void localizeDisplayValues(DataSet dataitem, ConnectionInfo connectionInfo) {
        Locale userLocale = I18nUtil.getConnectionLocale(connectionInfo);
        TimeZone userTimeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
        M18NUtil m18NUtil = new M18NUtil(connectionInfo);
        String locdecimalSep = "" + FormatUtil.getInstance(connectionInfo).getDecimalSeparator();
        for (int row = 0; row < dataitem.getRowCount(); ++row) {
            if (dataitem.getString(row, "displayvalue") != null && dataitem.getString(row, "displayvalue").length() > 0) {
                String localizedDisplay = I18nUtil.formatDataEntryDisplay(dataitem.getString(row, "displayvalue"), dataitem.getString(row, "datatypes"), dataitem.getBigDecimal(row, "transformvalue"), dataitem.getCalendar(row, "transformdt"), dataitem.getString(row, "displayformat"), dataitem.getString(row, "displayvalueformat"), userLocale, userTimeZone, m18NUtil);
                dataitem.setString(row, "displayvalue", localizedDisplay);
                if (dataitem.getValue(row, "uncertaintydisplayvalue").length() > 0) {
                    dataitem.setString(row, "uncertaintydisplayvalue", StringUtil.replaceAll(dataitem.getValue(row, "uncertaintydisplayvalue"), sysdecimalSep, locdecimalSep));
                }
                if (dataitem.getValue(row, "uncertaintydisplayvalueupper").length() <= 0) continue;
                dataitem.setString(row, "uncertaintydisplayvalueupper", StringUtil.replaceAll(dataitem.getValue(row, "uncertaintydisplayvalueupper"), sysdecimalSep, locdecimalSep));
                continue;
            }
            if (dataitem.getString(row, "defaultvalue") == null || dataitem.getString(row, "defaultvalue").length() <= 0 || dataitem.getString(row, "defaultvalue").indexOf(sysdecimalSep) < 0 || sysdecimalSep.equals(locdecimalSep)) continue;
            String defaultvalue = dataitem.getValue(row, "defaultvalue");
            Matcher matcher = numberPattern.matcher(defaultvalue);
            String matched = "";
            if (matcher.find()) {
                matched = matcher.group();
                String replaceMatched = StringUtil.replaceAll(matched, sysdecimalSep, locdecimalSep);
                defaultvalue = StringUtil.replaceAll(defaultvalue, matched, replaceMatched);
            }
            dataitem.setString(row, "defaultvalue", defaultvalue);
        }
    }

    public static String getUnitTrimmedEnteredText(String enteredText, FormatUtil formatutil) {
        String numoperator = "";
        String numtext = enteredText;
        switch (enteredText.charAt(0)) {
            case '<': 
            case '>': {
                if (enteredText.length() > 1 && enteredText.charAt(1) == '=') {
                    numoperator = enteredText.substring(0, 2);
                    numtext = enteredText.substring(2);
                    break;
                }
                numoperator = enteredText.substring(0, 1);
                numtext = enteredText.substring(1);
                break;
            }
            case '=': {
                numtext = enteredText.substring(1);
            }
        }
        for (int i = numtext.length(); i > 0; --i) {
            try {
                formatutil.parseBigDecimal(numtext.substring(0, i));
                if (i == 0) break;
                numtext = numtext.substring(0, i);
                break;
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
        }
        return numoperator + numtext;
    }
}

