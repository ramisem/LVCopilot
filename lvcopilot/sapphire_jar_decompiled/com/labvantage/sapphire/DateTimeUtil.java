/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DateTimeUtil
extends BaseClass
implements Serializable {
    public static final String NOW = "NOW";
    public static final String TODAY = "TODAY";
    public static final String START_TODAY = "TODAY";
    public static final String MIDDAY_TODAY = "MIDDAYTODAY";
    public static final String END_TODAY = "ENDTODAY";
    public static final String YESTERDAY = "YESTERDAY";
    public static final String START_YESTERDAY = "YESTERDAY";
    public static final String MIDDAY_YESTERDAY = "MIDDAYYESTERDAY";
    public static final String END_YESTERDAY = "ENDYESTERDAY";
    public static final String TOMORROW = "TOMORROW";
    public static final String START_TOMORROW = "TOMORROW";
    public static final String MIDDAY_TOMORROW = "MIDDAYTOMORROW";
    public static final String END_TOMORROW = "ENDTOMORROW";
    public static final String START_WEEK = "STARTWEEK";
    public static final String END_WEEK = "ENDWEEK";
    public static final String START_MONTH = "STARTMONTH";
    public static final String END_MONTH = "ENDMONTH";
    public static final String START_PREV_MONTH = "STARTPREVMONTH";
    public static final String END_PREV_MONTH = "ENDPREVMONTH";
    public static final String START_NEXT_MONTH = "STARTNEXTMONTH";
    public static final String END_NEXT_MONTH = "ENDNEXTMONTH";
    public static final String START_YEAR = "STARTYEAR";
    public static final String END_YEAR = "ENDYEAR";
    public static final String START_HOUR = "STARTHOUR";
    public static final String END_HOUR = "ENDHOUR";
    public static final String START_NEXT_HOUR = "STARTNEXTHOUR";
    public static final String END_NEXT_HOUR = "ENDNEXTHOUR";
    private int[] _monthdays = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private Locale locale = null;
    private TimeZone timezone = null;
    private static final HashMap defaultFormatCache = new HashMap();
    private static final HashMap sysQueryDateFormatCache = new HashMap();
    private static final HashSet formatSet = new HashSet();
    private String databaseid;
    public static final String PeriodUnit_DAY = "Days";
    public static final String PeriodUnit_MONTH = "Months";
    public static final String PeriodUnit_YEAR = "Years";
    public static final String PeriodUnit_HOUR = "Hours";
    public static final String PeriodUnit_WEEK = "Weeks";
    public static final String PeriodUnit_MINUTE = "Minutes";

    @Deprecated
    public static Calendar getOffsetDate(Calendar offsetfromDt, String periodUnit, float period) throws SapphireException {
        Calendar offSetDate = (Calendar)offsetfromDt.clone();
        boolean validUnit = false;
        if (periodUnit.equalsIgnoreCase(PeriodUnit_DAY)) {
            int realPartOfPeriod = Math.round(period * 24.0f);
            offSetDate.add(11, realPartOfPeriod);
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_MONTH)) {
            int realPartOfPeriod = (int)period;
            offSetDate.add(2, realPartOfPeriod);
            float fractionPrtOfPeriod = period - (float)realPartOfPeriod;
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 30.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_YEAR)) {
            int realPartOfPeriod = (int)period;
            offSetDate.add(1, realPartOfPeriod);
            float fractionPrtOfPeriod = period - (float)realPartOfPeriod;
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 365.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_HOUR)) {
            int realPartOfPeriod = (int)period;
            offSetDate.add(11, realPartOfPeriod);
            float fractionPrtOfPeriod = period - (float)realPartOfPeriod;
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 60.0f);
                offSetDate.add(12, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_WEEK)) {
            int realPartOfPeriod = (int)period;
            offSetDate.add(4, realPartOfPeriod);
            float fractionPrtOfPeriod = period - (float)realPartOfPeriod;
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 7.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_MINUTE)) {
            int realPartOfPeriod = (int)period;
            offSetDate.add(12, realPartOfPeriod);
            float fractionPrtOfPeriod = period - (float)realPartOfPeriod;
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 60.0f);
                offSetDate.add(13, fraction);
            }
            validUnit = true;
        }
        if (validUnit) {
            return offSetDate;
        }
        throw new SapphireException(periodUnit + " is invalid Period Unit.");
    }

    public static Calendar getOffsetDate(Calendar offsetfromDt, String periodUnit, BigDecimal period) throws SapphireException {
        Calendar offSetDate = (Calendar)offsetfromDt.clone();
        boolean validUnit = false;
        if (periodUnit.equalsIgnoreCase(PeriodUnit_DAY)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(5, realPartOfPeriod);
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_MONTH)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(2, realPartOfPeriod);
            float fractionPrtOfPeriod = period.subtract(new BigDecimal(period.intValue())).floatValue();
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 30.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_YEAR)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(1, realPartOfPeriod);
            float fractionPrtOfPeriod = period.subtract(new BigDecimal(period.intValue())).floatValue();
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 365.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_HOUR)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(11, realPartOfPeriod);
            float fractionPrtOfPeriod = period.subtract(new BigDecimal(period.intValue())).floatValue();
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 60.0f);
                offSetDate.add(12, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_WEEK)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(4, realPartOfPeriod);
            float fractionPrtOfPeriod = period.subtract(new BigDecimal(period.intValue())).floatValue();
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 7.0f);
                offSetDate.add(5, fraction);
            }
            validUnit = true;
        } else if (periodUnit.equalsIgnoreCase(PeriodUnit_MINUTE)) {
            int realPartOfPeriod = period.intValue();
            offSetDate.add(12, realPartOfPeriod);
            float fractionPrtOfPeriod = period.subtract(new BigDecimal(period.intValue())).floatValue();
            if ((double)fractionPrtOfPeriod > 0.0) {
                int fraction = Math.round(fractionPrtOfPeriod * 60.0f);
                offSetDate.add(13, fraction);
            }
            validUnit = true;
        }
        if (validUnit) {
            return offSetDate;
        }
        throw new SapphireException(periodUnit + " is invalid Period Unit.");
    }

    public DateTimeUtil() {
        this.setLocale(Locale.getDefault());
        this.setTimeZone(TimeZone.getDefault());
    }

    public DateTimeUtil(TimeZone timezone) {
        this.setLocale(Locale.getDefault());
        this.setTimeZone(timezone);
    }

    public DateTimeUtil(Locale locale) {
        this.setLocale(locale);
        this.setTimeZone(TimeZone.getDefault());
    }

    public DateTimeUtil(TimeZone timezone, Locale locale) {
        this.setLocale(locale);
        this.setTimeZone(timezone);
    }

    public DateTimeUtil(ConnectionInfo connectionInfo) {
        this.initDateTimeUtil(null, connectionInfo);
    }

    private void initDateTimeUtil(File rakFile, ConnectionInfo connectionInfo) {
        this.databaseid = connectionInfo.getDatabaseId();
        if (defaultFormatCache.get(this.databaseid + "_customFormatList") == null) {
            try {
                ConfigurationProcessor cp = rakFile != null ? new ConfigurationProcessor(rakFile, connectionInfo.getConnectionId()) : new ConfigurationProcessor(connectionInfo.getConnectionId());
                PropertyList dateFormatPolicy = cp.getPolicy("DateFormatPolicy", "Sapphire Custom");
                this.setDateFormatPolicy(this.databaseid, dateFormatPolicy);
            }
            catch (Exception e) {
                Trace.logWarn("Failed to retrieve DateFormatPolicy", e);
            }
        }
        this.setTimeZone(I18nUtil.getConnectionTimeZone(connectionInfo));
        if (!"N".equals(defaultFormatCache.get(this.databaseid + "_translatetouserlocale"))) {
            this.setLocale(I18nUtil.getConnectionLocale(connectionInfo));
        } else {
            this.setLocale(Locale.getDefault());
        }
    }

    public DateTimeUtil(File rakFile, ConnectionInfo connectionInfo) {
        this.initDateTimeUtil(rakFile, connectionInfo);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDateFormatPolicy(String databaseid, PropertyList dateFormatPolicy) {
        boolean hasCustomQueryDefined;
        String defaultdateformat = dateFormatPolicy.getProperty("defaultdateformat");
        String defaultdateonlyformat = dateFormatPolicy.getProperty("defaultdateonlyformat");
        ArrayList<String> customFormatList = new ArrayList<String>();
        customFormatList.add(defaultdateformat);
        customFormatList.add(defaultdateonlyformat);
        HashMap hashMap = defaultFormatCache;
        synchronized (hashMap) {
            defaultFormatCache.put(databaseid + "_customFormatList", customFormatList);
            defaultFormatCache.put(databaseid + "_translatetouserlocale", dateFormatPolicy.getProperty("translatetouserlocale"));
        }
        String sysQueryDateFormat = dateFormatPolicy.getProperty("querydateformat");
        String queryLocale = dateFormatPolicy.getProperty("querydateformatlocale");
        DateFormat df = null;
        Locale queryLocaleObject = I18nUtil.getLocaleFromString(queryLocale);
        boolean bl = hasCustomQueryDefined = sysQueryDateFormat.length() > 0 || queryLocale.length() > 0;
        df = sysQueryDateFormat.length() > 0 ? (formatSet.contains(sysQueryDateFormat) ? this.getDateFormatFromSMLString(sysQueryDateFormat, queryLocaleObject) : new SimpleDateFormat(sysQueryDateFormat, queryLocaleObject)) : DateFormat.getDateTimeInstance(2, 2, queryLocaleObject);
        HashMap hashMap2 = sysQueryDateFormatCache;
        synchronized (hashMap2) {
            sysQueryDateFormatCache.put(databaseid, df);
            if (hasCustomQueryDefined) {
                sysQueryDateFormatCache.put(databaseid + "_CustomQueryFormat", "Y");
            }
        }
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setTimeZone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public TimeZone getTimeZone() {
        return this.timezone;
    }

    public boolean isDefaultDateFormatDefined() {
        return defaultFormatCache.get(this.databaseid + "_customFormatList") != null && ((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(0).toString().length() > 0;
    }

    public boolean isDefaultDateOnlyFormatDefined() {
        return defaultFormatCache.get(this.databaseid + "_customFormatList") != null && ((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(1).toString().length() > 0;
    }

    public boolean isCustomQueryFormatDefined() {
        return "Y".equals(sysQueryDateFormatCache.get(this.databaseid + "_CustomQueryFormat"));
    }

    public DateFormat getDefaultDateFormat() {
        String customDateFormat;
        DateFormat df = defaultFormatCache.get(this.databaseid + "_customFormatList") == null || "".equals(((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(0)) ? DateFormat.getDateTimeInstance(3, 3, this.locale) : (formatSet.contains(customDateFormat = (String)((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(0)) ? this.getDateFormatFromSMLString(customDateFormat) : new SimpleDateFormat(customDateFormat, this.locale));
        df.setTimeZone(this.timezone);
        return df;
    }

    public DateFormat getDefaultDateOnlyFormat() {
        String customDateOnlyFormat;
        DateFormat df = defaultFormatCache.get(this.databaseid + "_customFormatList") == null || "".equals(((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(1)) ? DateFormat.getDateInstance(3, this.locale) : (formatSet.contains(customDateOnlyFormat = (String)((ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList")).get(1)) ? this.getDateFormatFromSMLString(customDateOnlyFormat) : new SimpleDateFormat(customDateOnlyFormat, this.locale));
        df.setTimeZone(this.timezone);
        return df;
    }

    public DateFormat getSysQueryDateFormat() {
        return (DateFormat)sysQueryDateFormatCache.get(this.databaseid);
    }

    public DateFormat getDateFormatFromSMLString(String customDateFormat) {
        return this.getDateFormatFromSMLString(customDateFormat, this.locale);
    }

    private DateFormat getDateFormatFromSMLString(String customDateFormat, Locale locale) {
        int dformat;
        String[] formatS = StringUtil.split(customDateFormat, " ");
        int n = formatS[0].equals("S") ? 3 : (dformat = formatS[0].equals("L") ? 1 : 2);
        if (formatS.length == 2) {
            int tformat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
            return DateFormat.getDateTimeInstance(dformat, tformat, locale);
        }
        return DateFormat.getDateInstance(dformat, locale);
    }

    private void parseCalendar(String datetime, ParseInfo parseInfo) {
        this.parseCalendar(datetime, parseInfo, false);
    }

    private void parseCalendar(String datetime, ParseInfo parseInfo, boolean dateOnly) {
        boolean customDateFormatChecked = false;
        block4: for (int i = 0; i < parseInfo.format.length && parseInfo.isRelDate; ++i) {
            for (int j = 0; j < parseInfo.timeformat.length && parseInfo.isRelDate; ++j) {
                parseInfo.simpleDateFormat = dateOnly ? (SimpleDateFormat)DateFormat.getDateInstance(parseInfo.format[i], this.locale) : (SimpleDateFormat)DateFormat.getDateTimeInstance(parseInfo.format[i], parseInfo.timeformat[j], this.locale);
                parseInfo.simpleDateFormat.setLenient(false);
                parseInfo.simpleDateFormat.setTimeZone(this.timezone);
                if (parseInfo.customDateFormat.length() > 0 && !customDateFormatChecked && !formatSet.contains(parseInfo.customDateFormat)) {
                    String pattern = parseInfo.simpleDateFormat.toPattern();
                    if (parseInfo.customDateFormat.indexOf("yyyy") < 0 && parseInfo.customDateFormat.indexOf("yy") >= 0 && pattern.indexOf("yyyy") < 0 && pattern.indexOf("yy") >= 0 || parseInfo.customDateFormat.indexOf("yyyy") >= 0 && pattern.indexOf("yyyy") >= 0) {
                        customDateFormatChecked = true;
                        SimpleDateFormat csdf = new SimpleDateFormat(dateOnly ? parseInfo.customDateOnlyFormat : parseInfo.customDateFormat, this.locale);
                        csdf.setLenient(false);
                        csdf.setTimeZone(this.timezone);
                        try {
                            csdf.parse(datetime);
                            parseInfo.isRelDate = false;
                            parseInfo.isParsed = true;
                            parseInfo.simpleDateFormat = csdf;
                            break block4;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                try {
                    parseInfo.simpleDateFormat.parse(datetime);
                    parseInfo.isRelDate = false;
                    parseInfo.isParsed = true;
                    break block4;
                }
                catch (Exception exception) {
                    continue;
                }
            }
        }
    }

    public boolean isRelDate(String datetime) {
        return this.isRelDate(datetime, false);
    }

    public boolean isRelDate(String datetime, boolean dateOnly) {
        ParseInfo parseInfo = new ParseInfo();
        Object cal = null;
        parseInfo.format = new int[]{3, 2, 1, 0};
        parseInfo.timeformat = new int[]{0, 1, 2, 3};
        if (datetime == null || datetime.equals("")) {
            datetime = "N";
        }
        datetime = datetime.trim();
        if (!parseInfo.isParsed && this.databaseid != null) {
            ArrayList customFormatList = (ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList");
            if ("N".equals(defaultFormatCache.get(this.databaseid + "_translatetouserlocale"))) {
                this.locale = Locale.getDefault();
            }
            if (customFormatList != null) {
                parseInfo.customDateFormat = (String)customFormatList.get(0);
                parseInfo.customDateOnlyFormat = (String)customFormatList.get(1);
            }
        }
        if (!parseInfo.isParsed) {
            this.parseCalendar(datetime, parseInfo, dateOnly);
        }
        return parseInfo.isRelDate;
    }

    public Instant getInstant(String datetime) {
        if (datetime == null || datetime.trim().length() == 0 || datetime.equals("(null)")) {
            return null;
        }
        Calendar calendar = this.getCalendar(datetime);
        return calendar == null ? null : calendar.toInstant();
    }

    public Calendar getCalendar(String datetime) {
        Calendar cal;
        block89: {
            int i;
            String[] pattern;
            cal = null;
            ParseInfo parseInfo = new ParseInfo();
            parseInfo.format = new int[]{3, 2, 1, 0};
            parseInfo.timeformat = new int[]{0, 1, 2, 3};
            if (datetime == null || datetime.equals("")) {
                datetime = "N";
            }
            datetime = datetime.trim();
            if (!parseInfo.isParsed && this.databaseid != null) {
                ArrayList customFormatList = (ArrayList)defaultFormatCache.get(this.databaseid + "_customFormatList");
                if ("N".equals(defaultFormatCache.get(this.databaseid + "_translatetouserlocale"))) {
                    this.locale = Locale.getDefault();
                }
                if (customFormatList != null) {
                    parseInfo.customDateFormat = (String)customFormatList.get(0);
                    parseInfo.customDateOnlyFormat = (String)customFormatList.get(1);
                }
            }
            if (!parseInfo.isParsed) {
                this.parseCalendar(datetime, parseInfo);
            }
            if (parseInfo.isRelDate && (datetime.indexOf("-") < 0 || datetime.indexOf("-") == 4)) {
                parseInfo.simpleDateFormat = new SimpleDateFormat();
                parseInfo.simpleDateFormat.setTimeZone(this.timezone);
                parseInfo.simpleDateFormat.setLenient(false);
                pattern = new String[]{"MM/dd/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"};
                i = 0;
                do {
                    try {
                        parseInfo.simpleDateFormat.applyPattern(pattern[i]);
                        parseInfo.simpleDateFormat.parse(datetime);
                        parseInfo.isRelDate = false;
                        parseInfo.isParsed = true;
                        break;
                    }
                    catch (ParseException e) {
                        ++i;
                    }
                    catch (IllegalArgumentException argumentException) {
                        ++i;
                    }
                    catch (Exception argumentException) {
                        // empty catch block
                    }
                } while (i < pattern.length && parseInfo.isRelDate);
            }
            if (parseInfo.isRelDate) {
                boolean customDateOnlyFormatChecked = false;
                for (i = 0; i < parseInfo.format.length && parseInfo.isRelDate; ++i) {
                    parseInfo.simpleDateFormat = (SimpleDateFormat)DateFormat.getDateInstance(parseInfo.format[i], this.locale);
                    parseInfo.simpleDateFormat.setLenient(false);
                    parseInfo.simpleDateFormat.setTimeZone(this.timezone);
                    if (parseInfo.customDateOnlyFormat.length() > 0 && !customDateOnlyFormatChecked && !formatSet.contains(parseInfo.customDateOnlyFormat)) {
                        String pattern2 = parseInfo.simpleDateFormat.toPattern();
                        if (parseInfo.customDateOnlyFormat.indexOf("yyyy") < 0 && parseInfo.customDateOnlyFormat.indexOf("yy") >= 0 && pattern2.indexOf("yyyy") < 0 && pattern2.indexOf("yy") >= 0 || parseInfo.customDateOnlyFormat.indexOf("yyyy") >= 0 && parseInfo.customDateOnlyFormat.indexOf("yyyy") >= 0) {
                            customDateOnlyFormatChecked = true;
                            SimpleDateFormat csdf = new SimpleDateFormat(parseInfo.customDateOnlyFormat, this.locale);
                            csdf.setLenient(false);
                            csdf.setTimeZone(this.timezone);
                            try {
                                csdf.parse(datetime);
                                parseInfo.isRelDate = false;
                                parseInfo.isParsed = true;
                                parseInfo.simpleDateFormat = csdf;
                                break;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    }
                    try {
                        parseInfo.simpleDateFormat.parse(datetime);
                        parseInfo.isRelDate = false;
                        parseInfo.isParsed = true;
                        break;
                    }
                    catch (Exception pattern2) {
                        continue;
                    }
                }
            }
            if (parseInfo.isRelDate && (datetime.indexOf("-") < 0 || datetime.indexOf("-") == 4)) {
                parseInfo.simpleDateFormat = new SimpleDateFormat();
                parseInfo.simpleDateFormat.setTimeZone(this.timezone);
                pattern = new String[]{"yyyy-MM-dd"};
                i = 0;
                do {
                    try {
                        parseInfo.simpleDateFormat.applyPattern(pattern[i]);
                        parseInfo.simpleDateFormat.parse(datetime);
                        parseInfo.isRelDate = false;
                        parseInfo.isParsed = true;
                        break;
                    }
                    catch (ParseException e) {
                        ++i;
                    }
                    catch (IllegalArgumentException argumentException) {
                        ++i;
                    }
                    catch (Exception argumentException) {
                        // empty catch block
                    }
                } while (i < pattern.length && parseInfo.isRelDate);
            }
            if (parseInfo.isRelDate) {
                parseInfo.isParsed = true;
                if ((datetime = datetime.toUpperCase()).equals("N") || datetime.equals(NOW)) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                } else if (datetime.equals("TODAY") || datetime.equals("T") || datetime.equals("ST") || datetime.equals("STARTTODAY")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(MIDDAY_TODAY) || datetime.equals("MT") || datetime.equals("MIDDAY TODAY")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 12);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_TODAY) || datetime.equals("ET")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals("YESTERDAY") || datetime.equals("Y") || datetime.equals("SY") || datetime.equals("STARTYESTERDAY")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                    cal.add(5, -1);
                } else if (datetime.equals(MIDDAY_YESTERDAY) || datetime.equals("MY") || datetime.equals("MIDDAY YESTERDAY")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 12);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                    cal.add(5, -1);
                } else if (datetime.equals(END_YESTERDAY) || datetime.equals("EY")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                    cal.add(5, -1);
                } else if (datetime.equals("TOMORROW") || datetime.equals("O") || datetime.equals("SO") || datetime.equals("STARTTOMORROW")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                    cal.add(5, 1);
                } else if (datetime.equals(MIDDAY_TOMORROW) || datetime.equals("MO") || datetime.equals("MIDDAY TOMORROW")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 12);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                    cal.add(5, 1);
                } else if (datetime.equals(END_TOMORROW) || datetime.equals("EO")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                    cal.add(5, 1);
                } else if (datetime.equals(START_WEEK) || datetime.equals("SW") || datetime.equals("START WEEK")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                    cal.set(7, 1);
                } else if (datetime.equals(END_WEEK) || datetime.equals("EW") || datetime.equals("END WEEK")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                    cal.set(7, 7);
                } else if (datetime.equals(START_MONTH) || datetime.equals("SM") || datetime.equals("START MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(5, 1);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_MONTH) || datetime.equals("EM") || datetime.equals("END MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(5, this._monthdays[cal.get(2)]);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals(START_PREV_MONTH) || datetime.equals("SPM") || datetime.equals("START PREV MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(2, -1);
                    cal.set(5, 1);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_PREV_MONTH) || datetime.equals("EPM") || datetime.equals("END PREV MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(2, -1);
                    cal.set(5, this._monthdays[cal.get(2)]);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals(START_NEXT_MONTH) || datetime.equals("SNM") || datetime.equals("START NEXT MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(2, 1);
                    cal.set(5, 1);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_NEXT_MONTH) || datetime.equals("ENM") || datetime.equals("END NEXT MONTH")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(2, 1);
                    cal.set(5, this._monthdays[cal.get(2)]);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals(START_YEAR) || datetime.equals("SR") || datetime.equals("START YEAR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(2, 0);
                    cal.set(5, 1);
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_YEAR) || datetime.equals("ER") || datetime.equals("END YEAR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(2, 11);
                    cal.set(5, this._monthdays[cal.get(2)]);
                    cal.set(11, 23);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals(START_HOUR) || datetime.equals("SH") || datetime.equals("START HOUR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_HOUR) || datetime.equals("EH") || datetime.equals("END HOUR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else if (datetime.equals(START_NEXT_HOUR) || datetime.equals("SNH") || datetime.equals("START NEXT HOUR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(10, 1);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                } else if (datetime.equals(END_NEXT_HOUR) || datetime.equals("ENH") || datetime.equals("END NEXT HOUR")) {
                    cal = Calendar.getInstance(this.timezone, this.locale);
                    cal.add(10, 1);
                    cal.set(12, 59);
                    cal.set(13, 59);
                    cal.set(14, 0);
                } else {
                    int pos = datetime.indexOf("+");
                    int n = pos = pos == -1 ? datetime.indexOf("-") : pos;
                    if (pos != -1) {
                        cal = this.getCalendar(datetime.substring(0, pos));
                        String units = datetime.substring(datetime.length() - 1).toUpperCase();
                        boolean isUnit = false;
                        try {
                            Integer.parseInt(units);
                        }
                        catch (Exception e) {
                            isUnit = true;
                        }
                        int factor = datetime.substring(pos, pos + 1).equals("+") ? 1 : -1;
                        try {
                            String temp = "";
                            if (isUnit) {
                                temp = datetime.substring(pos + 1, datetime.length() - 1).trim();
                            } else {
                                temp = datetime.substring(pos + 1).trim();
                                units = "D";
                            }
                            if (units.equals("S")) {
                                cal.add(13, factor * Integer.parseInt(temp));
                                break block89;
                            }
                            if (units.equals("M")) {
                                cal.add(12, factor * Integer.parseInt(temp));
                                break block89;
                            }
                            if (units.equals("H")) {
                                cal.add(10, factor * Integer.parseInt(temp));
                                break block89;
                            }
                            if (units.equals("D")) {
                                cal.add(5, factor * Integer.parseInt(temp));
                                break block89;
                            }
                            cal = null;
                        }
                        catch (Exception e) {
                            Trace.log("DT-UTIL", "ERROR: Failed to calculate the date '" + datetime + "': " + e.getMessage());
                            cal = null;
                        }
                    } else {
                        parseInfo.isParsed = false;
                    }
                }
            } else if (parseInfo.isParsed) {
                parseInfo.simpleDateFormat = DateTimeUtil.correctTwoYearDigits(parseInfo.simpleDateFormat, datetime);
                cal = parseInfo.simpleDateFormat.getCalendar();
            } else {
                Trace.log("DT-UTIL", "ERROR: Failed to parse the date '" + datetime + "'. ");
                throw new RuntimeException("ERROR: Failed to parse the date '" + datetime + "'. ");
            }
        }
        return cal;
    }

    public Timestamp getTimestamp(String datetime) {
        Timestamp ts = null;
        Calendar cal = this.getCalendar(datetime);
        if (cal != null) {
            ts = new Timestamp(cal.getTime().getTime());
        }
        return ts;
    }

    public static Timestamp getNowTimestamp() {
        Calendar cal = Calendar.getInstance();
        return new Timestamp(cal.getTime().getTime());
    }

    public static Calendar getNowCalendar() {
        return Calendar.getInstance();
    }

    public static SimpleDateFormat correctTwoYearDigits(SimpleDateFormat sdf, String datetime) {
        String pattern;
        if (sdf.getCalendar().get(1) < 100 && (pattern = sdf.toPattern()).indexOf("yyyy") >= 0) {
            pattern = pattern.replace("yyyy", "yy");
            sdf.applyPattern(pattern);
            try {
                sdf.parse(datetime);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return sdf;
    }

    public static void resetToStartOfDay(Calendar cal) {
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
    }

    public static void resetToEndOfDay(Calendar cal) {
        cal.set(11, 23);
        cal.set(12, 59);
        cal.set(13, 59);
        cal.set(14, 999);
    }

    public static int getOverlappingMillis(Calendar start1, Calendar end1, Calendar start2, Calendar end2) {
        Calendar minEnd;
        Calendar maxStart = start1.before(start2) ? start2 : start1;
        Calendar calendar = minEnd = end1.before(end2) ? end1 : end2;
        if (maxStart.before(minEnd)) {
            return (int)(minEnd.getTimeInMillis() - maxStart.getTimeInMillis());
        }
        return 0;
    }

    public static int getOverlappingMinutes(Calendar start1, Calendar end1, Calendar start2, Calendar end2) {
        return DateTimeUtil.getOverlappingMillis(start1, end1, start2, end2) / 60000;
    }

    public static int getOverlappingMillis(Instant start1, Instant end1, Instant start2, Instant end2) {
        Instant minEnd;
        Instant maxStart = start1.isBefore(start2) ? start2 : start1;
        Instant instant = minEnd = end1.isBefore(end2) ? end1 : end2;
        if (maxStart.isBefore(minEnd)) {
            return (int)(minEnd.toEpochMilli() - maxStart.toEpochMilli());
        }
        return 0;
    }

    public static int getOverlappingMinutes(Instant start1, Instant end1, Instant start2, Instant end2) {
        return DateTimeUtil.getOverlappingMillis(start1, end1, start2, end2) / 60000;
    }

    static {
        String[] sets = new String[]{"S", "M", "L", "S S", "S M", "S L", "M S", "M M", "M L", "L S", "L M", "L L"};
        for (int i = 0; i < sets.length; ++i) {
            formatSet.add(sets[i]);
        }
    }

    private class ParseInfo {
        String customDateFormat = "";
        String customDateOnlyFormat = "";
        int[] format = null;
        int[] timeformat = null;
        boolean isParsed = false;
        boolean isRelDate = true;
        SimpleDateFormat simpleDateFormat = null;

        public ParseInfo(String customDateFormat, String customDateOnlyFormat, int[] format, int[] timeformat) {
            this.customDateOnlyFormat = customDateOnlyFormat;
            this.customDateFormat = customDateFormat;
            this.format = format;
            this.timeformat = timeformat;
        }

        public ParseInfo() {
        }
    }
}

