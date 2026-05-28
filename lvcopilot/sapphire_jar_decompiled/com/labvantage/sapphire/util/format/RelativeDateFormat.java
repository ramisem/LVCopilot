/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTime
 *  org.joda.time.Period
 *  org.joda.time.ReadableInstant
 */
package com.labvantage.sapphire.util.format;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RelativeDateFormat
extends DateFormat {
    protected final long startTime = System.currentTimeMillis();
    private PropertyList dateFormatPolicy;
    private TranslationProcessor translationProcessor;
    boolean isDateOnly;

    public RelativeDateFormat(boolean isDateOnly, PropertyList dateFormatPolicy, TranslationProcessor translationProcessor) {
        this.isDateOnly = isDateOnly;
        this.dateFormatPolicy = dateFormatPolicy;
        this.translationProcessor = translationProcessor;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        StringBuffer sb = new StringBuffer();
        Calendar now = DateTimeUtil.getNowCalendar();
        try {
            PropertyListCollection relativedateformats;
            Period period;
            boolean isPastDate = date.before(now.getTime());
            if (isPastDate) {
                period = new Period((ReadableInstant)new DateTime((Object)date), (ReadableInstant)new DateTime((Object)now));
                relativedateformats = this.dateFormatPolicy.getPropertyListNotNull("relativedateformat").getCollection("pastdateformat");
            } else {
                period = new Period((ReadableInstant)new DateTime((Object)now), (ReadableInstant)new DateTime((Object)date));
                relativedateformats = this.dateFormatPolicy.getPropertyListNotNull("relativedateformat").getCollection("futuredateformat");
            }
            String format = null;
            int years = period.getYears();
            int months = period.getMonths();
            int weeks = period.getWeeks();
            int days = period.getDays();
            int hours = period.getHours();
            int minutes = period.getMinutes();
            if (weeks > 0) {
                days = weeks * 7 + days;
            }
            if (relativedateformats != null) {
                for (int i = 0; i < relativedateformats.size(); ++i) {
                    PropertyList list = relativedateformats.getPropertyList(i);
                    int whenlessthan = Integer.parseInt(list.getProperty("whenlessthan").trim());
                    String unit = list.getProperty("unit");
                    if ("Minutes".equals(unit)) {
                        if (years == 0 && months == 0 && weeks == 0 && days == 0 && hours == 0 && minutes < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if ("Hours".equals(unit)) {
                        if (years == 0 && months == 0 && weeks == 0 && days == 0 && hours < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if ("Days".equals(unit)) {
                        if (years == 0 && months == 0 && days < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if ("Weeks".equals(unit)) {
                        if (years == 0 && months == 0 && weeks < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if ("Months".equals(unit)) {
                        if (years == 0 && months < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if ("Years".equals(unit)) {
                        if (years < whenlessthan) {
                            format = list.getProperty("format");
                        }
                    } else if (-1 == whenlessthan) {
                        format = list.getProperty("format");
                    }
                    if (format != null && format.length() > 0) break;
                }
            }
            if (format != null && format.length() > 0) {
                String[] tokens = StringUtil.getTokens(format);
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                for (String token : tokens) {
                    String tokenvalue = "";
                    switch (token.toLowerCase()) {
                        case "years": {
                            tokenvalue = String.valueOf(years);
                            format = StringUtil.replaceAll(format, "(years)", years > 1 ? "years" : "year");
                            break;
                        }
                        case "months": {
                            tokenvalue = String.valueOf(months);
                            format = StringUtil.replaceAll(format, "(months)", months > 1 ? "months" : "month");
                            break;
                        }
                        case "weeks": {
                            tokenvalue = String.valueOf(weeks);
                            format = StringUtil.replaceAll(format, "(weeks)", weeks > 1 ? "weeks" : "week");
                            break;
                        }
                        case "days": {
                            tokenvalue = String.valueOf(days);
                            format = StringUtil.replaceAll(format, "(days)", days > 1 ? "days" : "day");
                            break;
                        }
                        case "hours": {
                            tokenvalue = String.valueOf(hours);
                            format = StringUtil.replaceAll(format, "(hours)", hours > 1 ? "hours" : "hour");
                            break;
                        }
                        case "minutes": {
                            tokenvalue = String.valueOf(minutes);
                            format = StringUtil.replaceAll(format, "(minutes)", minutes > 1 ? "minutes" : "minute");
                            break;
                        }
                        case "seconds": {
                            tokenvalue = String.valueOf(period.getSeconds());
                            format = StringUtil.replaceAll(format, "(seconds)", period.getSeconds() > 1 ? "seconds" : "second");
                        }
                    }
                    tokenMap.put(token, tokenvalue);
                }
                sb.append(this.translationProcessor.translate(format, tokenMap));
            } else {
                sb.append(new M18NUtil().getDefaultDateFormat(!this.isDateOnly).format(date));
            }
        }
        catch (NumberFormatException e) {
            sb.append(new M18NUtil().getDefaultDateFormat(!this.isDateOnly).format(date));
            Trace.logError("RelativeDateFormat", "Failed to load Relative date format from Date Format Policy");
        }
        return sb;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return null;
    }
}

