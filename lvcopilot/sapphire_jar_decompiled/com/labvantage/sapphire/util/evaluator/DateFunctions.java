/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.format.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateFunctions {
    public static Date parseDate(String strdate, String format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
        return df.parse(strdate);
    }

    public static String getDate(String strdate, String format) {
        String returnedDate = "";
        format = DateFormatter.getJavaCompatibleFormatString(format);
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
        try {
            Date parsedDate = df.parse(strdate);
            returnedDate = df.format(parsedDate);
            if (DateFunctions.getYear(strdate, format).length() == 2) {
                returnedDate = returnedDate.replaceAll("/00", "/");
                returnedDate = returnedDate.replaceAll("-00", "-");
            }
            return returnedDate;
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static boolean isDate(String strdate, String format) {
        try {
            strdate = DateFunctions.getDate(strdate, format);
            return strdate != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String getTime(String strtime, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
        try {
            Date parsedTime = df.parse(strtime);
            String returnedTime = df.format(parsedTime);
            if (DateFunctions.getYear(strtime, format).length() == 2) {
                returnedTime = returnedTime.replaceAll("/00", "/");
                returnedTime = returnedTime.replaceAll("-00", "-");
            }
            return returnedTime;
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static boolean isTime(String strtime, String format) {
        try {
            strtime = DateFunctions.getTime(strtime, format);
            return strtime != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String getDateTime(String strdatetime, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
        try {
            Date parsedDateTime = df.parse(strdatetime);
            String returnedDateTime = df.format(parsedDateTime);
            if (DateFunctions.getYear(strdatetime, format).length() == 2) {
                returnedDateTime = returnedDateTime.replaceAll("/00", "/");
                returnedDateTime = returnedDateTime.replaceAll("-00", "-");
            }
            return returnedDateTime;
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static String getDay(String strdate, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            return Integer.toString(calendar.get(5));
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static String getDayName(String strdate, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            int weekday = calendar.get(7);
            String dayName = "";
            switch (weekday) {
                case 1: {
                    dayName = "Sunday";
                    break;
                }
                case 2: {
                    dayName = "Monday";
                    break;
                }
                case 3: {
                    dayName = "Tuesday";
                    break;
                }
                case 4: {
                    dayName = "Wednesday";
                    break;
                }
                case 5: {
                    dayName = "Thursday";
                    break;
                }
                case 6: {
                    dayName = "Friday";
                    break;
                }
                case 7: {
                    dayName = "Saturday";
                }
            }
            return dayName;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getDayNumber(String strdate, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            int weekday = calendar.get(7);
            return Integer.toString(weekday);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getDaysAfter(String strdate1, String strdate2, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar date1 = new GregorianCalendar();
            GregorianCalendar date2 = new GregorianCalendar();
            int daydiff = 0;
            date1.setTime(DateFunctions.parseDate(strdate1, format));
            date2.setTime(DateFunctions.parseDate(strdate2, format));
            while (date1.getTime().compareTo(date2.getTime()) < 0) {
                ((Calendar)date1).add(5, 1);
                ++daydiff;
            }
            while (date1.getTime().compareTo(date2.getTime()) > 0) {
                ((Calendar)date2).add(5, 1);
                --daydiff;
            }
            return Integer.toString(daydiff);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getHour(String strtime, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strtime, format));
            int hour = calendar.get(11);
            return Integer.toString(hour);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getMinute(String strtime, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strtime, format));
            int minute = calendar.get(12);
            return Integer.toString(minute);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getMonth(String strdate, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            int month = calendar.get(2);
            return Integer.toString(++month);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentTime(String format) {
        format = format == null || format.equals("") ? "HH:mm:ss" : DateFormatter.getJavaCompatibleFormatString(format);
        try {
            Date currdate = new Date();
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            return df.format(currdate);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getRelativeDate(String strdate, String daynum, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            int dnum = Integer.parseInt(daynum);
            ((Calendar)calendar).add(5, dnum);
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            return df.format(calendar.getTime());
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getRelativeTime(String strtime, String secnum, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strtime, format));
            int snum = Integer.parseInt(secnum);
            ((Calendar)calendar).add(13, snum);
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            return df.format(calendar.getTime());
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getSecond(String strtime, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strtime, format));
            int second = calendar.get(13);
            return Integer.toString(second);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getSecondsAfter(String strtime1, String strtime2, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar time1 = new GregorianCalendar();
            GregorianCalendar time2 = new GregorianCalendar();
            int seconddiff = 0;
            time1.setTime(DateFunctions.parseDate(strtime1, format));
            time2.setTime(DateFunctions.parseDate(strtime2, format));
            while (time1.getTime().compareTo(time2.getTime()) < 0) {
                ((Calendar)time1).add(13, 1);
                ++seconddiff;
            }
            while (time1.getTime().compareTo(time2.getTime()) > 0) {
                ((Calendar)time2).add(13, 1);
                --seconddiff;
            }
            return Integer.toString(seconddiff);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentDate(String format) {
        format = format == null || format.equals("") ? "M/d/yyyy" : DateFormatter.getJavaCompatibleFormatString(format);
        try {
            Date currdate = new Date();
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            return df.format(currdate);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getYear(String strdate, String format) {
        format = DateFormatter.getJavaCompatibleFormatString(format);
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(DateFunctions.parseDate(strdate, format));
            int year = calendar.get(1);
            return Integer.toString(year);
        }
        catch (Exception e) {
            return null;
        }
    }
}

