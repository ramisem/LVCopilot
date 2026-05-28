/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.format;

import com.labvantage.sapphire.Trace;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.util.StringUtil;

public class DateFormatter {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String formatDateTime(Calendar value) {
        return DateFormatter.formatDateTime(value, "");
    }

    public static String formatDateTime(Calendar value, String format, Locale locale, TimeZone timezone) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        int ampm = 0;
        String javaCompatibleFormatString = "";
        String returnString = "";
        if (value == null) {
            return null;
        }
        if (format == null) {
            format = "";
        }
        try {
            Trace.logDebug("DateFormat Engine: Using supplied PB Format = \"" + format + "\" & Date = " + value.getTime().toString());
            javaCompatibleFormatString = DateFormatter.getJavaCompatibleFormatString(format, locale);
            if (format.indexOf("AM/PM") >= 0) {
                ampm = 1;
            } else if (format.indexOf("am/pm") >= 0) {
                ampm = 2;
            } else if (format.indexOf("A/P") >= 0) {
                ampm = 3;
            } else if (format.indexOf("a/p") >= 0) {
                ampm = 4;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(javaCompatibleFormatString, locale);
            if (timezone != null) {
                simpleDateFormat.setTimeZone(timezone);
            }
            returnString = simpleDateFormat.format(value.getTime());
            if (ampm > 0) {
                if (ampm == 2) {
                    returnString = returnString.replaceAll("AM", "am");
                    returnString = returnString.replaceAll("PM", "pm");
                } else if (ampm == 3) {
                    returnString = returnString.replaceAll("AM", "A");
                    returnString = returnString.replaceAll("PM", "P");
                } else if (ampm == 4) {
                    returnString = returnString.replaceAll("AM", "a");
                    returnString = returnString.replaceAll("PM", "p");
                }
            }
            Trace.logDebug("   Formatted Output = " + returnString);
            return returnString;
        }
        catch (Exception dateFormatException) {
            Trace.logError("DateFormatter: Exception generated : " + dateFormatException.toString());
            return "";
        }
    }

    public static String formatDateTime(Calendar value, String format, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        int ampm = 0;
        String javaCompatibleFormatString = "";
        String returnString = "";
        if (value == null) {
            return null;
        }
        if (format == null) {
            format = "";
        }
        try {
            Trace.logDebug("DateFormat Engine: Using supplied PB Format = \"" + format + "\" & Date = " + value.getTime().toString());
            javaCompatibleFormatString = DateFormatter.getJavaCompatibleFormatString(format, locale);
            if (format.indexOf("AM/PM") >= 0) {
                ampm = 1;
            } else if (format.indexOf("am/pm") >= 0) {
                ampm = 2;
            } else if (format.indexOf("A/P") >= 0) {
                ampm = 3;
            } else if (format.indexOf("a/p") >= 0) {
                ampm = 4;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(javaCompatibleFormatString, locale);
            returnString = simpleDateFormat.format(value.getTime());
            if (ampm > 0) {
                if (ampm == 2) {
                    returnString = returnString.replaceAll("AM", "am");
                    returnString = returnString.replaceAll("PM", "pm");
                } else if (ampm == 3) {
                    returnString = returnString.replaceAll("AM", "A");
                    returnString = returnString.replaceAll("PM", "P");
                } else if (ampm == 4) {
                    returnString = returnString.replaceAll("AM", "a");
                    returnString = returnString.replaceAll("PM", "p");
                }
            }
            Trace.logDebug("   Formatted Output = " + returnString);
            return returnString;
        }
        catch (Exception dateFormatException) {
            Trace.logError("DateFormatter: Exception generated : " + dateFormatException.toString());
            return returnString;
        }
    }

    public static String formatDateTime(Calendar value, String format) {
        return DateFormatter.formatDateTime(value, format, null);
    }

    public static String getJavaCompatibleFormatString(String pbFormatString) {
        return DateFormatter.getJavaCompatibleFormatString(pbFormatString, Locale.getDefault());
    }

    public static String getJavaCompatibleFormatString(String pbFormatString, Locale locale) {
        String javaCompatibleFormatString = "";
        try {
            if (pbFormatString.trim().length() == 0) {
                pbFormatString = "M/d/yyyy HH:mm:ss";
            } else if (!pbFormatString.startsWith("'") && !pbFormatString.startsWith("\"")) {
                if (pbFormatString.toLowerCase().indexOf("dddd") >= 0) {
                    pbFormatString = pbFormatString.replaceAll("dddd", "EEEE");
                    pbFormatString = pbFormatString.replaceAll("DDDD", "EEEE");
                }
                if (pbFormatString.toLowerCase().indexOf("ddd") >= 0) {
                    pbFormatString = pbFormatString.replaceAll("ddd", "EEE");
                    pbFormatString = pbFormatString.replaceAll("DDD", "EEE");
                }
                if (pbFormatString.indexOf("D") >= 0) {
                    pbFormatString = pbFormatString.replaceAll("D", "d");
                }
                if (pbFormatString.indexOf("Y") >= 0) {
                    pbFormatString = pbFormatString.replaceAll("Y", "y");
                }
                if ((pbFormatString = pbFormatString.indexOf("AM/PM") >= 0 ? pbFormatString.replaceAll("AM/PM", "a") : (pbFormatString.indexOf("am/pm") >= 0 ? pbFormatString.replaceAll("am/pm", "a") : (pbFormatString.indexOf("A/P") >= 0 ? pbFormatString.replaceAll("A/P", "a") : (pbFormatString.indexOf("a/p") >= 0 ? pbFormatString.replaceAll("a/p", "a") : pbFormatString.replaceAll("h", "H"))))).indexOf("m") >= 0) {
                    if (pbFormatString.toLowerCase().indexOf("h:m") < 0 && pbFormatString.toLowerCase().indexOf("hm") < 0) {
                        pbFormatString = pbFormatString.replaceAll("m", "M");
                    } else {
                        for (int fi = pbFormatString.indexOf("m"); fi < pbFormatString.length(); ++fi) {
                            if (pbFormatString.charAt(fi) != 'm' || fi >= 1 && (fi != 1 || pbFormatString.charAt(fi - 1) == ':' || pbFormatString.toLowerCase().charAt(fi - 1) == 'h') && (fi <= 1 || pbFormatString.charAt(fi - 2) == ':' || pbFormatString.toLowerCase().charAt(fi - 2) == 'h')) continue;
                            pbFormatString = pbFormatString.substring(0, fi) + "M" + pbFormatString.substring(fi + 1);
                        }
                        if (pbFormatString.indexOf("mmmm") >= 0) {
                            pbFormatString = pbFormatString.replaceAll("mmmm", "MMMM");
                        }
                        if (pbFormatString.indexOf("mmm") >= 0) {
                            pbFormatString = pbFormatString.replaceAll("mmmm", "MMM");
                        }
                    }
                }
                if ((pbFormatString = pbFormatString.replaceAll("f", "S")).toLowerCase().indexOf("[shortdate]") >= 0 || pbFormatString.toLowerCase().indexOf("[longdate]") >= 0 || pbFormatString.toLowerCase().indexOf("[mediumdate]") >= 0 || pbFormatString.toLowerCase().indexOf("[time]") >= 0 || pbFormatString.toLowerCase().indexOf("[shorttime]") >= 0) {
                    pbFormatString = pbFormatString.toLowerCase();
                    pbFormatString = StringUtil.replaceAll(pbFormatString, "[shortdate]", ((SimpleDateFormat)DateFormat.getDateInstance(3, locale)).toPattern());
                    pbFormatString = StringUtil.replaceAll(pbFormatString, "[mediumdate]", ((SimpleDateFormat)DateFormat.getDateInstance(2, locale)).toPattern());
                    pbFormatString = StringUtil.replaceAll(pbFormatString, "[longdate]", ((SimpleDateFormat)DateFormat.getDateInstance(1, locale)).toPattern());
                    pbFormatString = StringUtil.replaceAll(pbFormatString, "[time]", "HH:mm:ss");
                    pbFormatString = StringUtil.replaceAll(pbFormatString, "[shorttime]", "HH:mm");
                }
            }
            javaCompatibleFormatString = pbFormatString;
            return javaCompatibleFormatString;
        }
        catch (Exception dateFormatException) {
            Trace.logError("DateFormatter: Exception generated : " + dateFormatException.toString());
            return "";
        }
    }
}

