/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class RegexConverter {
    public static final String yy = "(\\d{2}|\\d{4})";
    public static final String yyyy = "\\d{4}";
    public static final String mm = "([0-5][0-9])";
    public static final String ss = "([0-5][0-9])";
    public static final String slash = "\\/";
    public static final String[] M = new String[]{"(([13578])|(([0][13578])|(1[02])))", "(([469])|(([0][469])|(11)))", "(([2])|([0][2]))"};
    public static final String[] d = new String[]{"(([1-9])|(3[01]|0[1-9]|[12]\\d))", "(([1-9])|(3[0]|0[1-9]|[12]\\d))", "(([1-9])|(0[1-9]|[12]\\d))"};
    public static final String[] dd = new String[]{"((3[01]|0[1-9]|[12]\\d|([1-9])))", "((3[0]|0[1-9]|[12]\\d|([1-9])))", "((0[1-9]|[12]\\d|([1-9])))"};
    public static final String h = "(([1-9])|(([0][1-9])|(1[012])))";
    public static final String hh = "(([1-9])|(([0][1-9])|(1[012])))";
    public static final String H = "(([0-9])|(([01][0-9])|(2[0-3])))";
    public static final String HH = "(([0-9])|(([01][0-9])|(2[0-3])))";
    public static final String k = "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))";
    public static final String kk = "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))";
    public static final String K = "(([0-9])|((0[0-9])|(1[0-1])))";
    public static final String KK = "(([0-9])|((0[0-9])|(1[0-1])))";

    private static String convert(String pattern, Locale locale, TimeZone timeZone, boolean matchEntirePattern, boolean twoDigitRegEx) {
        String matchString;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        if (timeZone != null) {
            sdf.setTimeZone(timeZone);
        } else {
            sdf.setTimeZone(TimeZone.getDefault());
        }
        String[] symboltokens = sdf.getDateFormatSymbols().getAmPmStrings();
        String a = RegexConverter.getExpFromArray(symboltokens);
        symboltokens = sdf.getDateFormatSymbols().getShortMonths();
        String[] MMMs = new String[]{"((" + symboltokens[0] + ")|(" + symboltokens[2] + ")|(" + symboltokens[4] + ")|(" + symboltokens[6] + ")|(" + symboltokens[7] + ")|(" + symboltokens[9] + ")|(" + symboltokens[11] + "))", "((" + symboltokens[3] + ")|(" + symboltokens[5] + ")|(" + symboltokens[8] + ")|(" + symboltokens[10] + "))", "((" + symboltokens[1] + "))"};
        symboltokens = sdf.getDateFormatSymbols().getMonths();
        String[] MMMMs = new String[]{"((" + symboltokens[0] + ")|(" + symboltokens[2] + ")|(" + symboltokens[4] + ")|(" + symboltokens[6] + ")|(" + symboltokens[7] + ")|(" + symboltokens[9] + ")|(" + symboltokens[11] + "))", "((" + symboltokens[3] + ")|(" + symboltokens[5] + ")|(" + symboltokens[8] + ")|(" + symboltokens[10] + "))", "((" + symboltokens[1] + "))"};
        String tzDST = "";
        String tzST = "";
        TimeZone tz = sdf.getTimeZone();
        String z = "";
        if (pattern.toLowerCase().indexOf("z") > -1 && tz.observesDaylightTime()) {
            tzDST = tz.getDisplayName(true, 0);
            tzST = tz.getDisplayName(false, 0);
            z = "((" + tzST + ")|(" + tzDST + "))";
        } else {
            sdf.applyPattern("z");
            z = sdf.format(Calendar.getInstance().getTime());
        }
        StringBuffer[] subregexs = new StringBuffer[3];
        for (int m = 0; m < 3; ++m) {
            String[] regexs;
            StringBuffer regex = new StringBuffer();
            if (!twoDigitRegEx) {
                regexs = new String[]{dd[m], d[m], yyyy, yy, yy, "(([0-9])|(([01][0-9])|(2[0-3])))", "(([0-9])|(([01][0-9])|(2[0-3])))", "(([1-9])|(([0][1-9])|(1[012])))", "(([1-9])|(([0][1-9])|(1[012])))", "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))", "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))", "(([0-9])|((0[0-9])|(1[0-1])))", "(([0-9])|((0[0-9])|(1[0-1])))", "([0-5][0-9])", "([0-5][0-9])", slash, MMMMs[m], MMMs[m], M[m], M[m], a, z};
            } else {
                String y2 = "\\d{2}";
                regexs = new String[]{dd[m], d[m], yyyy, y2, yy, "(([0-9])|(([01][0-9])|(2[0-3])))", "(([0-9])|(([01][0-9])|(2[0-3])))", "(([1-9])|(([0][1-9])|(1[012])))", "(([1-9])|(([0][1-9])|(1[012])))", "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))", "([1-9]|(1[0-9])|(2[0-4]))|(([0-1][1-9])|(2[0-4]))", "(([0-9])|((0[0-9])|(1[0-1])))", "(([0-9])|((0[0-9])|(1[0-1])))", "([0-5][0-9])", "([0-5][0-9])", slash, MMMMs[m], MMMs[m], M[m], M[m], a, z};
            }
            String[] tokens = new String[]{"dd", "d", "yyyy", "yy", "y", "HH", "H", "hh", "h", "kk", "k", "KK", "K", "mm", "ss", "/", "MMMM", "MMM", "MM", "M", "a", "z"};
            String tempPattern = pattern;
            block1: while (tempPattern.length() > 0) {
                if (tempPattern.indexOf("'") == 0) {
                    if (tempPattern.indexOf("''") == 0) {
                        regex.append("'");
                        tempPattern = tempPattern.substring(2);
                    } else {
                        int newindex = tempPattern.indexOf("'", 1);
                        while (newindex > 0 && newindex == tempPattern.indexOf("''", newindex)) {
                            newindex = tempPattern.indexOf("'", newindex + 2);
                        }
                        String token = tempPattern.substring(1, newindex);
                        token = token.replaceAll("''", "'");
                        regex.append(token);
                        tempPattern = tempPattern.substring(newindex + 1);
                    }
                }
                for (int i = 0; i < tokens.length; ++i) {
                    int index = tempPattern.indexOf(tokens[i]);
                    if (index != 0) continue;
                    regex.append(regexs[i]);
                    tempPattern = tempPattern.substring(tokens[i].length());
                    continue block1;
                }
                if (tempPattern.length() <= 0) continue;
                regex.append(tempPattern.substring(0, 1));
                tempPattern = tempPattern.substring(1);
            }
            subregexs[m] = regex;
        }
        String string = matchString = matchEntirePattern ? "(^" + subregexs[0].toString() + "$)|(^" + subregexs[1].toString() + "$)|(^" + subregexs[2].toString() + "$)" : "(" + subregexs[0].toString() + ")|(" + subregexs[1].toString() + ")|(" + subregexs[2].toString() + ")";
        if (matchString.indexOf(".") > 0) {
            matchString = StringUtil.replaceAll(matchString, ".", "\\.");
        }
        return matchString;
    }

    public static String convert(String pattern) {
        return RegexConverter.convert(pattern, Locale.getDefault(), null, true, false);
    }

    public static String convert(String pattern, boolean matchEntirePattern) {
        return RegexConverter.convert(pattern, Locale.getDefault(), null, matchEntirePattern, false);
    }

    public static String getExpFromArray(String[] months) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < months.length; ++i) {
            if (i == 0) {
                sb.append("((" + months[i] + ")");
                continue;
            }
            if (months[i].length() <= 0) continue;
            sb.append("|(" + months[i] + ")");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getValidDateFormatExp(M18NUtil m18NUtil) {
        return RegexConverter.getValidDateFormatExp(m18NUtil, true);
    }

    public static String getValidDateFormatExp(M18NUtil m18NUtil, boolean matchEntirePattern) {
        int[] styles = new int[]{3, 2};
        Locale locale = m18NUtil.getLocale();
        TimeZone timeZone = m18NUtil.getTimezone();
        StringBuffer sb = new StringBuffer("/");
        String defaultDateFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateFormat()).toPattern();
        String defaultDateOnlyFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateOnlyFormat()).toPattern();
        sb.append(RegexConverter.convert(defaultDateFormatPattern, locale, timeZone, matchEntirePattern, false));
        sb.append("|" + RegexConverter.convert(defaultDateOnlyFormatPattern, locale, timeZone, matchEntirePattern, false));
        SimpleDateFormat sdf = null;
        for (int i = 0; i < styles.length; ++i) {
            for (int j = 0; j < styles.length; ++j) {
                sdf = (SimpleDateFormat)DateFormat.getDateTimeInstance(styles[i], styles[j], locale);
                sb.append("|" + RegexConverter.convert(sdf.toPattern(), locale, timeZone, matchEntirePattern, false));
            }
            sdf = (SimpleDateFormat)DateFormat.getDateInstance(styles[i], locale);
            sb.append("|" + RegexConverter.convert(sdf.toPattern(), locale, timeZone, matchEntirePattern, false));
        }
        sb.append("/");
        return sb.toString();
    }

    public static String getValidDateFormatExp4DigitYear(M18NUtil m18NUtil, boolean matchEntirePattern, String fourDGFlag) {
        boolean twoDigitRegEx;
        int[] styles = new int[]{3, 2};
        Locale locale = m18NUtil.getLocale();
        TimeZone timeZone = m18NUtil.getTimezone();
        StringBuffer sb = new StringBuffer("/");
        String defaultDateFormatPattern = "";
        String defaultDateOnlyFormatPattern = "";
        boolean bl = twoDigitRegEx = !"4".equals(fourDGFlag);
        if ("4".equals(fourDGFlag)) {
            defaultDateFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateFormat()).toPattern().replaceAll("y+", "yyyy");
            defaultDateOnlyFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateOnlyFormat()).toPattern().replaceAll("y+", "yyyy");
        } else {
            defaultDateFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateFormat()).toPattern().replaceAll("y+", "yy");
            defaultDateOnlyFormatPattern = ((SimpleDateFormat)m18NUtil.getDefaultDateOnlyFormat()).toPattern().replaceAll("y+", "yy");
        }
        sb.append(RegexConverter.convert(defaultDateFormatPattern, locale, timeZone, matchEntirePattern, twoDigitRegEx));
        sb.append("|" + RegexConverter.convert(defaultDateOnlyFormatPattern, locale, timeZone, matchEntirePattern, twoDigitRegEx));
        SimpleDateFormat sdf = null;
        for (int i = 0; i < styles.length; ++i) {
            for (int j = 0; j < styles.length; ++j) {
                sdf = (SimpleDateFormat)DateFormat.getDateTimeInstance(styles[i], styles[j], locale);
                if ("4".equals(fourDGFlag)) {
                    sdf.applyPattern(sdf.toPattern().replaceAll("y+", "yyyy"));
                } else {
                    sdf.applyPattern(sdf.toPattern().replaceAll("y+", "yy"));
                }
                sb.append("|" + RegexConverter.convert(sdf.toPattern(), locale, timeZone, matchEntirePattern, twoDigitRegEx));
            }
            sdf = (SimpleDateFormat)DateFormat.getDateInstance(styles[i], locale);
            if ("4".equals(fourDGFlag)) {
                sdf.applyPattern(sdf.toPattern().replaceAll("y+", "yyyy"));
            } else {
                sdf.applyPattern(sdf.toPattern().replaceAll("y+", "yy"));
            }
            sb.append("|" + RegexConverter.convert(sdf.toPattern(), locale, timeZone, matchEntirePattern, twoDigitRegEx));
        }
        sb.append("/");
        return sb.toString();
    }

    public static String getSapDateFormat(PageContext pageContext) {
        String sapDateFormat = (String)pageContext.getSession().getAttribute("SapDateFormat");
        sapDateFormat = RegexConverter.getValidDateFormatExp(new M18NUtil(pageContext));
        pageContext.getSession().setAttribute("SapDateFormat", (Object)sapDateFormat);
        return sapDateFormat;
    }

    public static String getSapDateFormat4or2DigitYear(PageContext pageContext, String fourDGFlag) {
        String sapDateFormat = "";
        sapDateFormat = RegexConverter.getValidDateFormatExp4DigitYear(new M18NUtil(pageContext), true, fourDGFlag);
        return sapDateFormat;
    }
}

