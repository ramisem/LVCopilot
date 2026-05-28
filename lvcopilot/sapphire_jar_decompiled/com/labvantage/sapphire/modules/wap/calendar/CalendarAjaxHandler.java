/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.wap.calendar;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.calendar.CalendarPage;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class CalendarAjaxHandler
extends BaseAjaxRequest {
    ZoneId displayTimeZone;
    private M18NUtil m18;
    private int clientOffsetMinutes = 0;
    private int clientOffsetMinutes_from;
    private int clientOffsetMinutes_to;

    public static String getOrdinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11: 
            case 12: 
            case 13: {
                return i + "th";
            }
        }
        return i + sufixes[i % 10];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block45: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                boolean testinglab;
                boolean workarea;
                String shiftid;
                String keyid1;
                Mode mode = Mode.CREATESINGLE;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", mode.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                CalendarPage.Mode pagemode = CalendarPage.Mode.USER;
                try {
                    pagemode = CalendarPage.Mode.valueOf(ajaxresponse.getRequestParameter("pagemode", pagemode.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    this.clientOffsetMinutes = Integer.parseInt(ajaxresponse.getRequestParameter("timezoneoffset"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    this.clientOffsetMinutes_from = Integer.parseInt(ajaxresponse.getRequestParameter("timezoneoffset_from"));
                }
                catch (Exception e) {
                    this.clientOffsetMinutes_from = this.clientOffsetMinutes;
                }
                try {
                    this.clientOffsetMinutes_to = Integer.parseInt(ajaxresponse.getRequestParameter("timezoneoffset_to"));
                }
                catch (Exception e) {
                    this.clientOffsetMinutes_to = this.clientOffsetMinutes;
                }
                String timezone = ajaxresponse.getRequestParameter("timezone");
                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                this.m18 = new M18NUtil(connectionInfo);
                if (timezone.length() > 0) {
                    try {
                        try {
                            this.displayTimeZone = TimeZone.getTimeZone(timezone).toZoneId();
                        }
                        catch (Exception e1) {
                            this.displayTimeZone = I18nUtil.getZoneIdFromString(timezone);
                        }
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to find timezone", e);
                    }
                } else if (connectionInfo.getTimeZone() != null && connectionInfo.getTimeZone().length() > 0) {
                    try {
                        try {
                            this.displayTimeZone = TimeZone.getTimeZone(connectionInfo.getTimeZone()).toZoneId();
                        }
                        catch (Exception e1) {
                            this.displayTimeZone = I18nUtil.getZoneIdFromString(timezone);
                        }
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to find timezone", e);
                    }
                } else {
                    this.displayTimeZone = TimeZone.getDefault().toZoneId();
                }
                String calendarid = ajaxresponse.getRequestParameter("calendarid");
                int repeatCount = Integer.parseInt(ajaxresponse.getRequestParameter("repeatCount", "-1"));
                String sdcid = ajaxresponse.getRequestParameter("sdcid");
                LVCalendar calendar = CalendarPage.getCalendar(pagemode, sdcid, keyid1 = ajaxresponse.getRequestParameter("keyid1"), shiftid = ajaxresponse.getRequestParameter("shiftid"), workarea = sdcid.equalsIgnoreCase("department") && CalendarPage.isWorkarea(keyid1, this.getQueryProcessor()), testinglab = sdcid.equalsIgnoreCase("department") && CalendarPage.isTestingLab(keyid1, this.getQueryProcessor()), null, null, this.getConnectionProcessor().getSapphireConnection(), this.getQueryProcessor(), this.getTranslationProcessor(), this.logger);
                if (calendar != null) {
                    if (ajaxresponse.getRequestParameter("undostack").length() > 0) {
                        try {
                            calendar.setUndoStack(new JSONArray(ajaxresponse.getRequestParameter("undostack")));
                        }
                        catch (Exception e) {
                            this.logger.warn("Failed to load undostack.");
                        }
                    }
                    if (mode == Mode.RENAME) {
                        this.handleModeRename(ajaxresponse, pagemode, calendarid, sdcid, keyid1, shiftid, workarea, testinglab, calendar);
                    } else if (mode == Mode.UNDO) {
                        this.handleModeUndo(ajaxresponse, calendar);
                    } else if (mode == Mode.REFRESH) {
                        this.doRefresh(ajaxresponse, calendar);
                    } else if (mode == Mode.IMPORT) {
                        this.handleModeImport(ajaxresponse, calendar);
                    } else if (mode == Mode.CREATESINGLE || mode == Mode.CREATEREPEAT) {
                        this.handleModeCreateCalendarItem(ajaxresponse, mode, repeatCount, calendar);
                    } else if (mode == Mode.DELETESINGLE || mode == Mode.DELETEOVERRIDE || mode == Mode.DELETEINSTANCE || mode == Mode.DELETEREPEAT) {
                        this.handleModeDeleteCalendarItem(ajaxresponse, mode, calendarid, calendar);
                    } else if (mode == Mode.EDITSINGLE || mode == Mode.EDITOVERRIDE || mode == Mode.EDITINSTANCE || mode == Mode.EDITREPEAT) {
                        this.handleModeEditCalendarItem(ajaxresponse, mode, calendarid, calendar);
                    }
                    break block45;
                }
                this.logger.error("Unable to obtain Calendar");
                ajaxresponse.setError("Unable to obtain Calendar");
            }
            catch (Exception e) {
                this.logger.error("Unable to execute Calendar command", e);
                ajaxresponse.setError("Unable to execute Calendar command");
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    private void handleModeRename(AjaxResponse ajaxresponse, CalendarPage.Mode pagemode, String calendarid, String sdcid, String keyid1, String shiftid, boolean workarea, boolean testinglab, LVCalendar calendar) {
        if (ajaxresponse.getRequestParameters().containsKey("calendardesc")) {
            if (calendar.isShared()) {
                PropertyList propertyList = new PropertyList();
                propertyList.setProperty("sdcid", "LV_Calendar");
                propertyList.setProperty("keyid1", calendarid);
                propertyList.setProperty("calendardesc", ajaxresponse.getRequestParameter("calendardesc", ""));
                try {
                    this.getActionProcessor().processAction("EditSDI", "1", propertyList);
                    StringBuilder title = new StringBuilder();
                    StringBuilder desc = new StringBuilder();
                    CalendarPage.getCalendar(pagemode, sdcid, keyid1, shiftid, workarea, testinglab, title, desc, this.getConnectionProcessor().getSapphireConnection(), this.getQueryProcessor(), this.getTranslationProcessor(), this.logger);
                    ajaxresponse.addCallbackArgument("description", desc.toString());
                    ajaxresponse.addCallbackArgument("title", title.toString());
                }
                catch (Exception e) {
                    ajaxresponse.setError("Failed to update description.");
                }
            } else {
                ajaxresponse.setError("Cannot update description for private calendars.");
            }
        } else {
            ajaxresponse.setError("No description provided.");
        }
    }

    private void handleModeUndo(AjaxResponse ajaxresponse, LVCalendar calendar) {
        if (calendar.canUndo()) {
            try {
                calendar.undo();
                if (ajaxresponse.getRequestParameter("date", ajaxresponse.getRequestParameter("maxdate")).length() > 0) {
                    this.doRefresh(ajaxresponse, calendar);
                } else {
                    ajaxresponse.addCallbackArgument("cal", "");
                }
                ajaxresponse.addCallbackArgument("undostack", calendar.getUndoStack().toString());
            }
            catch (Exception e) {
                ajaxresponse.setError("Failed to undo.");
            }
        } else {
            ajaxresponse.setError("Nothing to undo.");
        }
    }

    private void doRefresh(AjaxResponse ajaxresponse, LVCalendar calendar) {
        CalendarPage.View view = CalendarPage.View.WEEK;
        try {
            view = CalendarPage.View.valueOf(ajaxresponse.getRequestParameter("view", view.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        String from = ajaxresponse.getRequestParameter("date", ajaxresponse.getRequestParameter("mindate"));
        String to = ajaxresponse.getRequestParameter("maxdate");
        if (from.matches("^\\d+$") && (to.length() == 0 || to.matches("^\\d+$"))) {
            try {
                ZonedDateTime zonedTo;
                ZonedDateTime zonedFrom = CalendarConverter.getZonedDateTimeFromClientValue(from, this.displayTimeZone, this.clientOffsetMinutes);
                ZonedDateTime zonedDateTime = zonedTo = to.length() > 0 ? CalendarConverter.getZonedDateTimeFromClientValue(to, this.displayTimeZone, this.clientOffsetMinutes) : zonedFrom;
                if (zonedFrom.compareTo(zonedTo) == 0) {
                    switch (view) {
                        case DAY: 
                        case WEEK: {
                            zonedFrom = CalendarConverter.getStartOfWeek(zonedFrom, this.m18.getLocale());
                            break;
                        }
                        case MONTH: {
                            zonedFrom = zonedFrom.with(LocalTime.of(0, 0, 0)).withDayOfMonth(1);
                            break;
                        }
                        case YEAR: {
                            zonedFrom = zonedFrom.with(LocalTime.of(0, 0, 0)).withDayOfYear(1);
                        }
                    }
                }
                switch (view) {
                    case DAY: 
                    case WEEK: {
                        zonedTo = zonedTo.plus(7L, ChronoUnit.DAYS);
                        break;
                    }
                    case MONTH: {
                        zonedTo = zonedTo.plus(31L, ChronoUnit.DAYS);
                        break;
                    }
                    case YEAR: {
                        zonedTo = zonedTo.plus(366L, ChronoUnit.DAYS);
                    }
                }
                if (ajaxresponse.getRequestParameter("clearcache", "N").equalsIgnoreCase("Y")) {
                    calendar.clearCache();
                }
                ajaxresponse.addCallbackArgument("calendar", calendar.toJSONObject(zonedFrom, zonedTo, this.displayTimeZone).toString());
            }
            catch (Exception e) {
                this.logger.error("Failed to refresh calendar", e);
                ajaxresponse.setError("Failed to refresh calendar");
            }
        } else {
            this.logger.error("Expected UTC number dates");
            ajaxresponse.setError("Expected UTC number dates");
        }
    }

    private void handleModeImport(AjaxResponse ajaxresponse, LVCalendar calendar) {
        if (calendar.isExternal()) {
            try {
                calendar.importExternal();
            }
            catch (Exception e) {
                this.logger.error("Failed to import calendar.", e);
                ajaxresponse.setError("Failed to import calendar.");
            }
        }
    }

    private void handleModeCreateCalendarItem(AjaxResponse ajaxresponse, Mode mode, int repeatCount, LVCalendar calendar) {
        try {
            String calendarItemLabel = ajaxresponse.getRequestParameter("label");
            String from = ajaxresponse.getRequestParameter("from");
            String to = ajaxresponse.getRequestParameter("to");
            String repeattext = mode == Mode.CREATEREPEAT ? ajaxresponse.getRequestParameter("repeat") : "";
            String calendaritemid = null;
            if (mode == Mode.CREATEREPEAT && repeattext.length() == 0) {
                ajaxresponse.setError("No repeat rule provided.");
            } else {
                try {
                    boolean allday;
                    ZonedDateTime fromZoned = CalendarConverter.getZonedDateTimeFromClientValue(from, this.displayTimeZone, this.clientOffsetMinutes_from);
                    ZonedDateTime toZoned = CalendarConverter.getZonedDateTimeFromClientValue(to, this.displayTimeZone, this.clientOffsetMinutes_to);
                    boolean bl = allday = fromZoned.getHour() == 0 && fromZoned.getMinute() == 0 && fromZoned.getSecond() == 0 && toZoned.getHour() == 0 && toZoned.getMinute() == 0 && toZoned.getSecond() == 0 && (int)ChronoUnit.DAYS.between(fromZoned, toZoned) >= 1;
                    if (mode == Mode.CREATEREPEAT) {
                        boolean forceMidnight = calendar.isSharedAndNoTimezone && allday;
                        repeattext = this.getRepeatRule(repeattext, fromZoned.toInstant(), forceMidnight);
                        if (forceMidnight) {
                            fromZoned = CalendarConverter.getZonedDateTimeFromClientValue(from, calendar.getTimeZone(), this.clientOffsetMinutes_from);
                            toZoned = CalendarConverter.getZonedDateTimeFromClientValue(to, calendar.getTimeZone(), this.clientOffsetMinutes_to);
                        }
                        if (repeattext.length() > 0) {
                            calendaritemid = calendar.createRecurringCalendarItem(calendarItemLabel, fromZoned, toZoned, allday, repeattext, repeatCount);
                        } else {
                            ajaxresponse.setError("Invalid repeat rule provided.");
                        }
                    } else {
                        calendaritemid = calendar.createCalendarItem(calendarItemLabel, fromZoned, toZoned, allday);
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to create calendar item from parse from and to dates", e);
                    ajaxresponse.setError("Failed to create calendar item from numeric from and to dates");
                }
            }
            if (calendaritemid != null && calendaritemid.length() > 0) {
                if (mode == Mode.CREATEREPEAT) {
                    this.doRefresh(ajaxresponse, calendar);
                } else {
                    this.doRefresh(ajaxresponse, calendar);
                }
                ajaxresponse.addCallbackArgument("undostack", calendar.getUndoStack().toString());
                if (ajaxresponse.getRequestParameters().containsKey("sid")) {
                    ajaxresponse.addCallbackArgument("sid", ajaxresponse.getRequestParameter("sid"));
                }
            } else {
                ajaxresponse.setError("Failed to create calendar item");
            }
        }
        catch (Exception e) {
            this.logger.error("Unable to create Calendar item", e);
            ajaxresponse.setError("Unable to create Calendar item");
        }
    }

    private void handleModeDeleteCalendarItem(AjaxResponse ajaxresponse, Mode mode, String calendarid, LVCalendar calendar) {
        try {
            String calendaritemid = ajaxresponse.getRequestParameter("calendaritemid");
            try {
                if (mode == Mode.DELETEOVERRIDE) {
                    block17: {
                        String parentCal = ajaxresponse.getRequestParameter("parentcalendarid", "");
                        if (parentCal.length() > 0) {
                            String parentcalitem = ajaxresponse.getRequestParameter("parentcalendaritemid", "");
                            try {
                                if (parentcalitem.length() > 0) {
                                    calendar.deleteCalendarItem(calendaritemid);
                                    calendar.overrideCalendarItemAsDelete(parentCal, parentcalitem);
                                    break block17;
                                }
                                calendar.overrideCalendarItemAsDelete(parentCal, calendaritemid);
                            }
                            catch (Exception e1) {
                                this.logger.error(e1.getMessage());
                                ajaxresponse.setError("Failed to remove calendar item.");
                            }
                        } else {
                            ajaxresponse.setError("No parent calendar provided");
                        }
                    }
                    this.doRefresh(ajaxresponse, calendar);
                } else if (mode == Mode.DELETEINSTANCE) {
                    try {
                        ZonedDateTime override = CalendarConverter.getZonedDateTimeFromClientValue(ajaxresponse.getRequestParameter("overridedate"), this.displayTimeZone, this.clientOffsetMinutes);
                        calendar.overrideInstanceAsDelete(calendarid, calendaritemid, override.toInstant());
                        this.doRefresh(ajaxresponse, calendar);
                    }
                    catch (Exception e) {
                        ajaxresponse.setError("Failed to override instance as delete.");
                    }
                } else {
                    calendar.deleteCalendarItem(calendaritemid);
                    if (mode == Mode.DELETEREPEAT) {
                        this.doRefresh(ajaxresponse, calendar);
                    } else {
                        this.doRefresh(ajaxresponse, calendar);
                    }
                }
                ajaxresponse.addCallbackArgument("undostack", calendar.getUndoStack().toString());
            }
            catch (Exception e) {
                this.logger.error("Failed to delete calendar item", e);
                ajaxresponse.setError("Failed to delete calendar item");
            }
        }
        catch (Exception e) {
            this.logger.error("Unable to create Calendar item", e);
            ajaxresponse.setError("Unable to create Calendar item");
        }
    }

    private void handleModeEditCalendarItem(AjaxResponse ajaxresponse, Mode mode, String calendarid, LVCalendar calendar) {
        try {
            String calendaritemid = ajaxresponse.getRequestParameter("calendaritemid");
            String calendarItemLabel = ajaxresponse.getRequestParameter("label");
            String from = ajaxresponse.getRequestParameter("from");
            String to = ajaxresponse.getRequestParameter("to");
            try {
                boolean allday;
                ZonedDateTime fromZoned = CalendarConverter.getZonedDateTimeFromClientValue(from, this.displayTimeZone, this.clientOffsetMinutes_from);
                ZonedDateTime toZoned = CalendarConverter.getZonedDateTimeFromClientValue(to, this.displayTimeZone, this.clientOffsetMinutes_to);
                boolean bl = allday = fromZoned.getHour() == 0 && fromZoned.getMinute() == 0 && fromZoned.getSecond() == 0 && toZoned.getHour() == 0 && toZoned.getMinute() == 0 && toZoned.getSecond() == 0 && (int)ChronoUnit.DAYS.between(fromZoned, toZoned) >= 1;
                if (mode == Mode.EDITOVERRIDE) {
                    String parentCal = ajaxresponse.getRequestParameter("parentcalendarid", "");
                    if (parentCal.length() > 0) {
                        calendar.overrideCalendarItem(parentCal, calendaritemid, calendarItemLabel, fromZoned, toZoned, allday);
                    } else {
                        ajaxresponse.setError("No parent calendar provided");
                    }
                } else if (mode == Mode.EDITINSTANCE) {
                    ZonedDateTime override = CalendarConverter.getZonedDateTimeFromClientValue(ajaxresponse.getRequestParameter("overridedate"), this.displayTimeZone, this.clientOffsetMinutes);
                    calendar.overrideInstance(calendarid, calendaritemid, override.toInstant(), calendarItemLabel, fromZoned, toZoned, allday);
                } else if (mode == Mode.EDITSINGLE) {
                    calendar.editCalendarItem(calendaritemid, calendarItemLabel, fromZoned, toZoned, allday);
                } else if (mode == Mode.EDITREPEAT) {
                    calendar.editCalendarItem(calendaritemid, calendarItemLabel, fromZoned, toZoned, allday, true);
                } else {
                    calendar.editCalendarItem(calendaritemid, calendarItemLabel, fromZoned, toZoned, allday);
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to edit calendar item", e);
                ajaxresponse.setError("Failed to edit calendar item");
            }
            this.doRefresh(ajaxresponse, calendar);
            ajaxresponse.addCallbackArgument("undostack", calendar.getUndoStack().toString());
        }
        catch (Exception e) {
            this.logger.error("Unable to create Calendar item", e);
            ajaxresponse.setError("Unable to create Calendar item");
        }
    }

    private String getRepeatRule(String orginalRule, Instant start, boolean forceMidnight) {
        String time2;
        StringBuilder rule = new StringBuilder();
        Date date = Date.from(start);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time1 = forceMidnight ? "00:00" : simpleDateFormat.format(date.getTime());
        String string = time2 = forceMidnight ? "00:00" : simpleDateFormat.format(date);
        if (orginalRule.startsWith("{") && orginalRule.endsWith("}")) {
            try {
                String type;
                JSONObject jsonrule = new JSONObject(orginalRule);
                String string2 = type = jsonrule.has("type") ? jsonrule.getString("type") : "";
                if (type.length() > 0) {
                    if (type.equalsIgnoreCase("daily")) {
                        String every;
                        String string3 = every = jsonrule.has("every") ? jsonrule.getString("every") : "";
                        if (every.equalsIgnoreCase("everyn")) {
                            String days;
                            String string4 = days = jsonrule.has("days") ? jsonrule.getString("days") : "";
                            if (days.length() > 0) {
                                int d = Integer.parseInt(days);
                                if (d > 0) {
                                    String n = d == 1 ? "day" : CalendarAjaxHandler.getOrdinal(d) + " day";
                                    rule.append("Every ").append(n).append(" @ ").append(time1);
                                }
                                throw new SapphireException("Invalid number for day repeat provided.");
                            }
                            throw new SapphireException("No number for day repeat provided.");
                        }
                        if (every.equalsIgnoreCase("everyweekday")) {
                            rule.append("Every day except Sat ,Sun").append(" @ ").append(time1);
                        }
                        throw new SapphireException("Invalid day repeat provided.");
                    }
                    if (type.equalsIgnoreCase("weekly")) {
                        String weeks;
                        String string5 = weeks = jsonrule.has("weeks") ? jsonrule.getString("weeks") : "";
                        if (weeks.length() > 0) {
                            int w = Integer.parseInt(weeks);
                            if (w > 0) {
                                String n;
                                if (w == 1) {
                                    n = "week";
                                } else if (w == 2 || w == 3 || w == 4) {
                                    n = CalendarAjaxHandler.getOrdinal(w) + " week";
                                } else {
                                    throw new SapphireException("Invalid number for week repeat provided. Must be 1-4");
                                }
                                if (jsonrule.has("days") && jsonrule.getJSONArray("days").length() > 0) {
                                    StringBuilder days = new StringBuilder();
                                    for (int i = 0; i < jsonrule.getJSONArray("days").length(); ++i) {
                                        if (days.length() > 0) {
                                            days.append(",");
                                        }
                                        days.append(jsonrule.getJSONArray("days").getString(i), 0, 3);
                                    }
                                    if (days.length() > 0) {
                                        rule.append("Every ").append(n).append(" on ").append((CharSequence)days).append(" @ ").append(time2);
                                    }
                                    rule.append("Every ").append(n).append(" @ ").append(time2);
                                }
                                rule.append("Every ").append(n).append(" @ ").append(time2);
                            }
                            throw new SapphireException("Invalid number for week repeat provided.");
                        }
                        throw new SapphireException("No number for week repeat provided.");
                    }
                    if (type.equalsIgnoreCase("monthly")) {
                        String every;
                        String string6 = every = jsonrule.has("every") ? jsonrule.getString("every") : "";
                        if (every.equalsIgnoreCase("day")) {
                            String day;
                            String string7 = day = jsonrule.has("day") ? jsonrule.getString("day") : "";
                            if (day.length() > 0) {
                                int d = Integer.parseInt(day);
                                if (d > 0) {
                                    if (jsonrule.has("months") && jsonrule.getJSONArray("months").length() > 0) {
                                        if (jsonrule.getJSONArray("months").length() == 12) {
                                            rule.append("Every month on ").append(CalendarAjaxHandler.getOrdinal(d)).append(" @ ").append(time2);
                                        }
                                        StringBuilder months = new StringBuilder();
                                        for (int i = 0; i < jsonrule.getJSONArray("months").length(); ++i) {
                                            if (months.length() > 0) {
                                                months.append(",");
                                            }
                                            months.append(jsonrule.getJSONArray("months").getString(i), 0, 3);
                                        }
                                        if (months.length() > 0) {
                                            rule.append("Every ").append((CharSequence)months).append(" on ").append(CalendarAjaxHandler.getOrdinal(d)).append(" @ ").append(time2);
                                        }
                                        rule.append("Every month on ").append(CalendarAjaxHandler.getOrdinal(d)).append(" @ ").append(time2);
                                    }
                                    rule.append("Every month on ").append(CalendarAjaxHandler.getOrdinal(d)).append(" @ ").append(time2);
                                }
                                throw new SapphireException("Invalid number for month repeat  provided.");
                            }
                            throw new SapphireException("No day number for month repeat  provided.");
                        }
                        if (every.equalsIgnoreCase("the")) {
                            String when = jsonrule.has("when") ? jsonrule.getString("when") : "";
                            String day = jsonrule.has("day") ? jsonrule.getString("day") : "";
                            String w = "";
                            if (when.equalsIgnoreCase("first")) {
                                w = CalendarAjaxHandler.getOrdinal(1);
                            } else if (when.equalsIgnoreCase("second")) {
                                w = CalendarAjaxHandler.getOrdinal(2);
                            } else if (when.equalsIgnoreCase("third")) {
                                w = CalendarAjaxHandler.getOrdinal(3);
                            } else if (when.equalsIgnoreCase("fourth")) {
                                w = CalendarAjaxHandler.getOrdinal(4);
                            } else if (when.equalsIgnoreCase("fifth")) {
                                w = CalendarAjaxHandler.getOrdinal(5);
                            } else if (when.equalsIgnoreCase("last")) {
                                w = "last";
                            }
                            if (w.length() > 0 && day.length() > 0) {
                                rule.append("Every month on ").append(w).append(" ").append(day, 0, 3).append(" @ ").append(time2);
                            }
                            throw new SapphireException("Invalid day selection for monthly repeat provided.");
                        }
                        throw new SapphireException("Invalid monthly repeat type provided.");
                    }
                    throw new SapphireException("Invalid repeat type provided.");
                }
                this.logger.error("Invalid repeat rule found.");
                return "";
            }
            catch (Exception e) {
                this.logError("Failed to parse complex rule: " + e.getMessage(), e);
                return e.getMessage();
            }
        } else if (orginalRule.equalsIgnoreCase("week")) {
            DayOfWeek dayOfWeek = start.atZone(ZoneOffset.UTC).getDayOfWeek();
            String dayOfWeekDisplayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US);
            rule.append("Every week on ").append(dayOfWeekDisplayName).append(" @ ").append(time2);
        } else if (orginalRule.equalsIgnoreCase("month")) {
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(start, ZoneOffset.UTC);
            int d = dateTime.getDayOfMonth();
            rule.append("Every month on ").append(CalendarAjaxHandler.getOrdinal(d)).append(" @ ").append(time2);
        } else if (orginalRule.equalsIgnoreCase("day")) {
            rule.append("Every day @ ").append(time2);
        } else {
            rule.append(orginalRule);
        }
        ScheduleRule scheduleRule = new ScheduleRule();
        int r = scheduleRule.setRule(rule.toString());
        if (r == -1 || !scheduleRule.isValidRule()) {
            return "";
        }
        return rule.toString();
    }

    public static enum Mode {
        CREATESINGLE,
        CREATEREPEAT,
        EDITSINGLE,
        EDITREPEAT,
        EDITOVERRIDE,
        EDITINSTANCE,
        DELETESINGLE,
        DELETEOVERRIDE,
        DELETEREPEAT,
        DELETEINSTANCE,
        REFRESH,
        UNDO,
        RENAME,
        IMPORT;

    }
}

