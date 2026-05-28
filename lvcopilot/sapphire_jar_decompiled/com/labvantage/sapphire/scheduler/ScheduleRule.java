/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class ScheduleRule {
    public static final int UNRECOGNIZED = -1;
    public static final int MINUTELY = 0;
    public static final int HOURLY = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY_DATES = 4;
    public static final int MONTHLY_WEEKS = 5;
    public static final int MINUTE_OFFSET = 6;
    public static final int HOUR_OFFSET = 7;
    public static final int DAY_OFFSET = 8;
    public static final int WEEK_OFFSET = 9;
    public static final int MONTH_OFFSET = 10;
    public static final int YEAR_OFFSET = 11;
    public static final int COUNT = 12;
    public static final int ABSDATE = 13;
    private String rule;
    private String enteredRule;
    private int ruleType = 0;
    private Pattern minutelyPattern = Pattern.compile("every( )*((((1)|(single)|(other)))?( )*minute)|([0-9]( )*minutes)");
    private Pattern hourlyPattern = Pattern.compile("every( )*((((1)|(single)|(other)))?( )*hour)|([0-9]( )*hours)");
    private Pattern dailyPattern = Pattern.compile("every( )*(([0-9]*1st)|(single)|([0-9]*2nd)|(other)|([0-9]*3rd)|([0-9]+th))?( )*day(( )*except( )*(((mon)|(tue)|(wed)|(thu)|(fri)|(sat)|(sun))( )*(,)*( )*)+)?( )*@( )*([0-2][0-9]:[0-5][0-9](,)*)+");
    private Pattern weeklyPattern = Pattern.compile("every( )*((1st)|(2nd)|(other)|(3rd)|(4th)|(last))?( )*week( )*on( )*(((mon)|(tue)|(wed)|(thu)|(fri)|(sat)|(sun))( )*(,)*( )*)+( )*@( )*([0-2][0-9]:[0-5][0-9](,)*)+");
    private Pattern monthlyDatesPattern = Pattern.compile("every( )*((((jan)|(feb)|(mar)|(apr)|(may)|(jun)|(jul)|(aug)|(sep)|(oct)|(nov)|(dec))( )*(,)*( )*)+|(month))*( )*on( )*(((1st)|(2nd)|(3rd)|(4th)|(5th)|(6th)|(7th)|(8th)|(9th)|(10th)|(11th)|(12th)|(13th)|(14th)|(15th)|(16th)|(17th)|(18th)|(19th)|(20th)|(21st)|(22nd)|(23rd)|(24th)|(25th)|(26th)|(27th)|(28th)|(29th)|(30th)|(31st))( )*(,)*( )*)+( )*@( )*([0-2][0-9]:[0-5][0-9](,)*)+");
    private Pattern monthlyWeekPattern = Pattern.compile("every( )*((((jan)|(feb)|(mar)|(apr)|(may)|(jun)|(jul)|(aug)|(sep)|(oct)|(nov)|(dec))( )*(,)*( )*)+|(month))*( )*on( )*((1st)|(2nd)|(3rd)|(4th)|(5th)|(last))?( )*((mon)|(tue)|(wed)|(thu)|(fri)|(sat)|(sun))?( )*@( )*([0-2][0-9]:[0-5][0-9](,)*)+");
    private Pattern countPattern = Pattern.compile("(every{1}|after{1})( )+[0-9]+(( )*(\\+)( )*([0-9])+)?");
    private Pattern minutePattern = Pattern.compile("(on)?minute( )*([0-9])+");
    private Pattern hourPattern = Pattern.compile("(on)?hour( )*([0-9])+");
    private Pattern dayPattern = Pattern.compile("(on)?( )*day( )*([0-9]( )*(,)*( )*)+( )*@( )*([0-2][0-9]:[0-5][0-9](,)*)+");
    private Pattern weekPattern = Pattern.compile("(on)?week( )*([0-9])+( )*@( )*([0-2][0-9]:[0-5][0-9])");
    private Pattern monthPattern = Pattern.compile("(on)?month( )*([0-9])+( )*@( )*([0-2][0-9]:[0-5][0-9])");
    private Pattern yearPattern = Pattern.compile("(on)?year( )*([0-9])+( )*@( )*([0-2][0-9]:[0-5][0-9])");
    private Pattern[] patterns = new Pattern[]{this.minutelyPattern, this.hourlyPattern, this.dailyPattern, this.weeklyPattern, this.monthlyDatesPattern, this.monthlyWeekPattern, this.minutePattern, this.hourPattern, this.dayPattern, this.weekPattern, this.monthPattern, this.yearPattern, this.countPattern};
    private Matcher ruleMatcher;
    private Calendar absDt;

    public ScheduleRule() {
    }

    public ScheduleRule(String rule) {
        this.setRule(rule);
    }

    public int setRule(String rule) {
        this.enteredRule = rule;
        this.rule = rule.toLowerCase();
        this.ruleType = -1;
        for (int i = 0; this.ruleType == -1 && i < this.patterns.length; ++i) {
            this.ruleMatcher = this.patterns[i].matcher(this.rule);
            if (!this.ruleMatcher.find()) continue;
            this.ruleType = i;
            break;
        }
        if (this.ruleType == -1) {
            this.absDt = new DateTimeUtil().getCalendar(this.rule);
            if (this.absDt != null) {
                this.ruleType = 13;
            }
        }
        return this.ruleType;
    }

    public String getEnteredRule() {
        return this.enteredRule;
    }

    public int getRuleType() {
        return this.ruleType;
    }

    public boolean isValidRule() {
        return this.isValidRule(false);
    }

    public boolean isValidRule(boolean calendarBasedOnly) {
        boolean validRule;
        boolean bl = validRule = this.ruleType != -1;
        if (this.ruleType == 2 && this.getOrdinal() == 0) {
            validRule = false;
        }
        if (calendarBasedOnly && validRule && this.ruleType == 12) {
            validRule = false;
        }
        return validRule;
    }

    public int getOrdinal() {
        Pattern ordinalPattern = null;
        if (this.ruleType == 1 || this.ruleType == 0 || this.ruleType == 12) {
            ordinalPattern = Pattern.compile("(single)|(other)|(([0-9])+)");
        } else if (this.ruleType == 2) {
            ordinalPattern = Pattern.compile("([0-9]*1st)|(single)|([0-9]*2nd)|(other)|([0-9]*3rd)|([0-9]+th)");
        } else if (this.ruleType == 3 || this.ruleType == 4 || this.ruleType == 5) {
            ordinalPattern = Pattern.compile("(1st)|(2nd)|(other)|(3rd)|(4th)|(5th)|(last)");
        } else {
            return 1;
        }
        Matcher ordinalMatch = ordinalPattern.matcher(this.rule);
        int ordinal = 1;
        if (ordinalMatch.find()) {
            String group = ordinalMatch.group();
            if (group.equals("other")) {
                ordinal = 2;
            } else if (group.equals("last")) {
                ordinal = 6;
            } else if (this.ruleType == 1 || this.ruleType == 0 || this.ruleType == 12) {
                try {
                    ordinal = Integer.parseInt(group);
                }
                catch (NumberFormatException numberFormatException) {}
            } else if (this.ruleType == 2) {
                int ordinalEndPos = 1;
                if (group.contains("th") || group.contains("st") || group.contains("rd") || group.contains("nd")) {
                    ordinalEndPos = group.length() - 2;
                }
                try {
                    ordinal = Integer.parseInt(group.substring(0, ordinalEndPos));
                }
                catch (NumberFormatException numberFormatException) {}
            } else {
                try {
                    ordinal = Integer.parseInt(group.substring(0, 1));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        return ordinal;
    }

    public String[] getDays() {
        return this.getOffsets();
    }

    public int[] getNumericDays() {
        return this.getNumericOffsets();
    }

    public String[] getOffsets() {
        ArrayList<String> offsets = new ArrayList<String>();
        Pattern offsetPattern = Pattern.compile("([0-9])+");
        int atPos = this.rule.indexOf(64);
        Matcher offsetMatch = offsetPattern.matcher(this.rule.substring(0, atPos > -1 ? atPos : this.rule.length()));
        while (offsetMatch.find()) {
            offsets.add(offsetMatch.group());
        }
        return offsets.size() > 0 ? offsets.toArray(new String[offsets.size()]) : new String[]{};
    }

    public int[] getNumericOffsets() {
        String[] offsets = this.getOffsets();
        int[] numericOffsets = new int[offsets.length];
        for (int i = 0; i < offsets.length; ++i) {
            try {
                numericOffsets[i] = Integer.parseInt(offsets[i]);
                continue;
            }
            catch (NumberFormatException nfe) {
                numericOffsets[i] = 0;
            }
        }
        return numericOffsets;
    }

    public boolean isNthWeekDayOfMonth(Calendar dt, int n) {
        boolean is = false;
        Calendar overflowdt = (Calendar)dt.clone();
        Calendar nooverflowdt = (Calendar)dt.clone();
        if (n > 0 && n < 5) {
            overflowdt.add(5, (0 - n) * 7);
            nooverflowdt.add(5, (1 - n) * 7);
            if (overflowdt.get(2) != nooverflowdt.get(2)) {
                is = true;
            }
        } else if (n == 5 && dt.get(5) > 7) {
            overflowdt.add(5, (0 - n) * 7);
            nooverflowdt.add(5, (1 - n) * 7);
            if (overflowdt.get(2) != nooverflowdt.get(2)) {
                is = true;
            }
        } else if (n == 6) {
            overflowdt.add(5, 7);
            if (overflowdt.get(2) != dt.get(2)) {
                is = true;
            }
        }
        return is;
    }

    public String[] getDaysOfWeek() {
        ArrayList<String> days = new ArrayList<String>();
        Pattern dayPattern = Pattern.compile("(mon(?!t))|(tue)|(wed)|(thu)|(fri)|(sat)|(sun)");
        Matcher dayMatch = dayPattern.matcher(this.rule);
        while (dayMatch.find()) {
            days.add(dayMatch.group());
        }
        return days.size() > 0 ? days.toArray(new String[days.size()]) : new String[]{};
    }

    public int[] getCalendarDaysOfWeek() {
        String[] days = this.getDaysOfWeek();
        int[] calendarDays = new int[days.length];
        for (int i = 0; i < days.length; ++i) {
            calendarDays[i] = days[i].equals("mon") ? 2 : (days[i].equals("tue") ? 3 : (days[i].equals("wed") ? 4 : (days[i].equals("thu") ? 5 : (days[i].equals("fri") ? 6 : (days[i].equals("sat") ? 7 : 1)))));
        }
        return calendarDays;
    }

    public String[] getTimes() {
        ArrayList<String> times = new ArrayList<String>();
        Pattern timePattern = Pattern.compile("[0-2][0-9]:[0-5][0-9]");
        Matcher timeMatch = timePattern.matcher(this.rule);
        while (timeMatch.find()) {
            times.add(timeMatch.group());
        }
        return times.size() > 0 ? times.toArray(new String[times.size()]) : new String[]{};
    }

    public String[] getMonths() {
        ArrayList<String> months = new ArrayList<String>();
        Pattern monthPattern = Pattern.compile("(jan)|(feb)|(mar)|(apr)|(may)|(jun)|(jul)|(aug)|(sep)|(oct)|(nov)|(dec)|(month)");
        Matcher monthMatch = monthPattern.matcher(this.rule);
        while (monthMatch.find()) {
            if (monthMatch.group().equals("month")) {
                return new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
            }
            months.add(monthMatch.group());
        }
        return months.size() > 0 ? months.toArray(new String[months.size()]) : new String[]{};
    }

    public int[] getCalendarMonths() {
        String[] months = this.getMonths();
        int[] calendarMonths = new int[months.length];
        for (int i = 0; i < months.length; ++i) {
            calendarMonths[i] = months[i].equals("jan") ? 0 : (months[i].equals("feb") ? 1 : (months[i].equals("mar") ? 2 : (months[i].equals("apr") ? 3 : (months[i].equals("may") ? 4 : (months[i].equals("jun") ? 5 : (months[i].equals("jul") ? 6 : (months[i].equals("aug") ? 7 : (months[i].equals("jul") ? 6 : (months[i].equals("aug") ? 7 : (months[i].equals("sep") ? 8 : (months[i].equals("oct") ? 9 : (months[i].equals("nov") ? 10 : 11))))))))))));
        }
        return calendarMonths;
    }

    public String[] getDates() {
        ArrayList<String> dates = new ArrayList<String>();
        Pattern datePattern = Pattern.compile("(1st)|(2nd)|(3rd)|(4th)|(5th)|(6th)|(7th)|(8th)|(9th)|(10th)|(11th)|(12th)|(13th)|(14th)|(15th)|(16th)|(17th)|(18th)|(19th)|(20th)|(21st)|(22nd)|(23rd)|(24th)|(25th)|(26th)|(27th)|(28th)|(29th)|(30th)|(31st)");
        Matcher dateMatch = datePattern.matcher(this.rule);
        while (dateMatch.find()) {
            dates.add(dateMatch.group().length() == 3 ? dateMatch.group().substring(0, 1) : dateMatch.group().substring(0, 2));
        }
        return dates.size() > 0 ? dates.toArray(new String[dates.size()]) : new String[]{};
    }

    public int[] getCalendarDates() {
        String[] dates = this.getDates();
        int[] calendarDates = new int[dates.length];
        for (int i = 0; i < dates.length; ++i) {
            try {
                calendarDates[i] = Integer.parseInt(dates[i]);
                continue;
            }
            catch (NumberFormatException nfe) {
                calendarDates[i] = 1;
            }
        }
        return calendarDates;
    }

    public String[] getCounts() {
        String[] countRule = StringUtil.split(this.enteredRule, " ", true);
        String[] counts = new String[]{};
        if (countRule.length > 1) {
            counts = new String[]{countRule[0], countRule[1].equalsIgnoreCase("count") ? "0" : countRule[1], countRule.length > 3 ? countRule[3] : ""};
        }
        return counts;
    }

    public ArrayList getEvents(ScheduleEvent scheduleEvent, Calendar fromDt, Calendar toDt, Calendar startDt, boolean sort) throws SapphireException {
        if (fromDt == null) {
            throw new SapphireException("FromDt cannot be null");
        }
        if (toDt == null) {
            toDt = Calendar.getInstance();
            toDt.set(1, fromDt.get(1) + 50);
        }
        ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
        Calendar currentDt = (Calendar)fromDt.clone();
        String[] times = this.getTimes();
        int ordinal = this.getOrdinal();
        if (this.getRuleType() == 0) {
            while (currentDt.before(toDt)) {
                if (currentDt.after(startDt) || currentDt.compareTo(startDt) == 0) {
                    scheduleEvents.add(scheduleEvent.clone((Calendar)currentDt.clone()));
                }
                currentDt.add(12, ordinal);
            }
        } else if (this.getRuleType() == 1) {
            while (currentDt.before(toDt)) {
                if (currentDt.after(startDt) || currentDt.compareTo(startDt) == 0) {
                    scheduleEvents.add(scheduleEvent.clone((Calendar)currentDt.clone()));
                }
                currentDt.add(10, ordinal);
            }
        } else if (this.getRuleType() == 2) {
            int[] days = this.getCalendarDaysOfWeek();
            boolean include = true;
            while (currentDt.before(toDt)) {
                include = true;
                for (int i = 0; i < days.length && include; ++i) {
                    if (currentDt.get(7) != days[i]) continue;
                    include = false;
                }
                if (include && (currentDt.after(startDt) || currentDt.compareTo(startDt) == 0)) {
                    this.addTimeEvents(scheduleEvents, scheduleEvent, times, currentDt, toDt);
                }
                currentDt.set(currentDt.get(1), currentDt.get(2), currentDt.get(5), 0, 0, 0);
                currentDt.add(5, ordinal);
            }
        } else if (this.getRuleType() == 3) {
            int[] days = this.getCalendarDaysOfWeek();
            int nextDtDay = currentDt.get(7);
            while (currentDt.before(toDt)) {
                for (int i = 0; i < days.length; ++i) {
                    Calendar dayDt = (Calendar)currentDt.clone();
                    dayDt.add(5, nextDtDay > days[i] ? 7 - (nextDtDay - days[i]) : days[i] - nextDtDay);
                    if (!dayDt.after(startDt) && dayDt.compareTo(startDt) != 0) continue;
                    this.addTimeEvents(scheduleEvents, scheduleEvent, times, dayDt, toDt);
                }
                currentDt.set(currentDt.get(1), currentDt.get(2), currentDt.get(5), 0, 0, 0);
                currentDt.add(5, ordinal * 7);
            }
        } else if (this.getRuleType() == 4) {
            int[] months = this.getCalendarMonths();
            int[] dates = this.getCalendarDates();
            int nextDtMonth = currentDt.get(2);
            Calendar fromDateMidnight = (Calendar)fromDt.clone();
            fromDateMidnight.set(fromDateMidnight.get(1), fromDateMidnight.get(2), fromDateMidnight.get(5), 0, 0, 0);
            Calendar startDtDateMidnight = (Calendar)startDt.clone();
            startDtDateMidnight.set(startDtDateMidnight.get(1), startDtDateMidnight.get(2), startDtDateMidnight.get(5), 0, 0, 0);
            while (currentDt.before(toDt)) {
                for (int i = 0; i < months.length; ++i) {
                    Calendar monthDt = (Calendar)currentDt.clone();
                    monthDt.add(2, nextDtMonth > months[i] ? 12 - (nextDtMonth - months[i]) : months[i] - nextDtMonth);
                    for (int j = 0; j < dates.length; ++j) {
                        monthDt.set(monthDt.get(1), monthDt.get(2), dates[j], 0, 0, 0);
                        if (!monthDt.after(fromDateMidnight) && monthDt.compareTo(fromDateMidnight) != 0 || !monthDt.after(startDtDateMidnight) && monthDt.compareTo(startDtDateMidnight) != 0) continue;
                        this.addTimeEvents(scheduleEvents, scheduleEvent, times, monthDt, toDt);
                    }
                }
                currentDt.set(currentDt.get(1), currentDt.get(2), currentDt.get(5), 0, 0, 0);
                currentDt.add(1, 1);
            }
        } else if (this.getRuleType() == 5) {
            int[] months = this.getCalendarMonths();
            int dayofweek = this.getCalendarDaysOfWeek()[0];
            int n = this.getOrdinal();
            int nextDtDay = currentDt.get(7);
            currentDt.add(5, nextDtDay > dayofweek ? 7 - (nextDtDay - dayofweek) : dayofweek - nextDtDay);
            while (currentDt.before(toDt)) {
                if (Arrays.binarySearch(months, currentDt.get(2)) >= 0 && (currentDt.after(startDt) || currentDt.compareTo(startDt) == 0) && this.isNthWeekDayOfMonth(currentDt, n)) {
                    this.addTimeEvents(scheduleEvents, scheduleEvent, times, currentDt, toDt);
                }
                currentDt.add(5, 7);
            }
        } else if (this.getRuleType() == 8 || this.getRuleType() == 9 || this.getRuleType() == 10 || this.getRuleType() == 11) {
            if (startDt == null) {
                throw new SapphireException("startDt cannot be null");
            }
            int[] offsets = this.getNumericOffsets();
            for (int i = 0; i < offsets.length; ++i) {
                Calendar offsetDt = (Calendar)startDt.clone();
                switch (this.getRuleType()) {
                    case 8: {
                        offsetDt.add(5, offsets[i]);
                        break;
                    }
                    case 9: {
                        offsetDt.add(5, offsets[i] * 7);
                        break;
                    }
                    case 10: {
                        offsetDt.add(2, offsets[i]);
                        break;
                    }
                    case 11: {
                        offsetDt.add(1, offsets[i]);
                    }
                }
                if (!offsetDt.equals(currentDt) && !offsetDt.after(currentDt) || !offsetDt.before(toDt)) continue;
                this.addTimeEvents(scheduleEvents, scheduleEvent, times, offsetDt, toDt);
            }
        } else if (this.getRuleType() == 6 || this.getRuleType() == 7) {
            if (startDt == null) {
                throw new SapphireException("startDt cannot be null");
            }
            int[] offsets = this.getNumericOffsets();
            for (int i = 0; i < offsets.length; ++i) {
                Calendar offsetDt = (Calendar)startDt.clone();
                switch (this.getRuleType()) {
                    case 6: {
                        offsetDt.add(12, offsets[i]);
                        break;
                    }
                    case 7: {
                        offsetDt.add(11, offsets[i]);
                    }
                }
                if (!offsetDt.equals(currentDt) && !offsetDt.after(currentDt) || !offsetDt.before(toDt)) continue;
                scheduleEvents.add(scheduleEvent.clone(offsetDt));
            }
        } else if (this.getRuleType() == 13) {
            if (this.absDt.before(toDt)) {
                scheduleEvents.add(scheduleEvent.clone((Calendar)this.absDt.clone()));
            }
        } else {
            throw new SapphireException("ScheduleSource rule '" + this.getEnteredRule() + "' invalid: " + scheduleEvent.getLinkSdcid() + scheduleEvent.getLinkKeyid1() + scheduleEvent.getLinkKeyid2() + scheduleEvent.getLinkKeyid3());
        }
        if (sort) {
            Collections.sort(scheduleEvents);
        }
        return scheduleEvents;
    }

    private boolean addTimeEvents(ArrayList scheduleEvents, ScheduleEvent scheduleEvent, String[] times, Calendar currentDt, Calendar endDt) {
        boolean added = true;
        for (int i = 0; i < times.length; ++i) {
            currentDt.set(currentDt.get(1), currentDt.get(2), currentDt.get(5), Integer.parseInt(times[i].substring(0, 2)), Integer.parseInt(times[i].substring(3)), 0);
            if (currentDt.before(endDt)) {
                scheduleEvents.add(scheduleEvent.clone((Calendar)currentDt.clone()));
                continue;
            }
            added = false;
        }
        return added;
    }

    public static int[] getRulesSequence(String[] rules, Calendar startDt) throws SapphireException {
        Calendar fromDt = (Calendar)startDt.clone();
        Calendar toDt = (Calendar)startDt.clone();
        toDt.set(startDt.get(1) + 50, 0, 1, 0, 0, 0);
        ArrayList eventSeq = new ArrayList();
        for (int i = 0; i < rules.length; ++i) {
            ScheduleRule sr = new ScheduleRule(rules[i]);
            ScheduleEvent se = new ScheduleEvent(Integer.toString(i), "", Calendar.getInstance());
            if (sr.getRuleType() == 6 || sr.getRuleType() == 7 || sr.getRuleType() == 8 || sr.getRuleType() == 9 || sr.getRuleType() == 10 || sr.getRuleType() == 11 || sr.getRuleType() == 13) {
                ArrayList events = sr.getEvents(se, fromDt, toDt, startDt, true);
                eventSeq.add(events.get(0));
                continue;
            }
            Calendar tempDt = (Calendar)toDt.clone();
            switch (sr.getRuleType()) {
                case 2: {
                    tempDt.add(5, 1);
                    break;
                }
                case 3: {
                    tempDt.add(5, 7);
                    break;
                }
                case 4: 
                case 5: {
                    tempDt.add(2, 1);
                    break;
                }
                default: {
                    tempDt.add(1, 10);
                }
            }
            eventSeq.add(new ScheduleEvent(rules[i], "", tempDt));
        }
        Collections.sort(eventSeq);
        int[] ruleSeq = new int[eventSeq.size()];
        for (int i = 0; i < eventSeq.size(); ++i) {
            ruleSeq[i] = Integer.parseInt(((ScheduleEvent)eventSeq.get(i)).getSchedulePlanid());
        }
        return ruleSeq;
    }

    public static void main(String[] args) {
        String[] test = new String[]{"Every day @ 06:00,17:00", "Every day @08:00", "Every day @ 06:00, 07:00", "eVeRy 1st day @ 06:00,17:00", "Every 2nd day @08:00", "Every other day @ 06:00, 07:00", "Every 3rd day @ 06:00,17:00", "Every 4th day @08:00", "Every 5th day @ 06:00, 07:00", "Every day except Mon @ 06:00,17:00", "Every day except Mon ,Tue,Wed @08:00", "Every day except Sat,  Sun @ 06:00, 07:00", "Every week on Mon @ 06:00,17:00", "Every other week on Mon,Fri @ 06:00,17:00", "Every last week on mon ,Tue,wed, thu,FRi @ 06:00", "Every week on mon,wed, fri @ 07:00,18:00", "Every month on 15th @ 06:00", "Every jan, feb on 15th @ 06:00", "Every jan,mar,jul,oct on 1st,15th @ 06:00,18:00", "Every month on 1st mon @ 06:00", "Every jan, feb on 2nd tue @ 06:00", "Every jan,mar,jul,oct on 4th wed @ 06:00,18:00", "On day 1 @ 06:00", "on Day 1,3,5 @ 06:00,18:00", "on Day 11,31,55 @ 06:00,18:00", "on Day 1 , 11 ,111, 1111 @ 06:00,18:00"};
        long start = System.currentTimeMillis();
        ScheduleRule sr = new ScheduleRule();
        for (int i = 0; i < test.length; ++i) {
            int j;
            String[] months;
            String[] days;
            StringBuffer output;
            String[] times;
            sr.setRule(test[i]);
            if (sr.getRuleType() == 2) {
                times = sr.getTimes();
                output = new StringBuffer("Rule: " + test[i] + " - DAILY [times=");
                for (int j2 = 0; j2 < times.length; ++j2) {
                    output.append(times[j2] + (j2 < times.length - 1 ? "," : ""));
                }
                output.append("], [ordinal=" + sr.getOrdinal() + "], [exceptions=");
                days = sr.getDaysOfWeek();
                for (int j3 = 0; j3 < days.length; ++j3) {
                    output.append(days[j3] + (j3 < days.length - 1 ? "," : ""));
                }
                output.append("]");
                continue;
            }
            if (sr.getRuleType() == 3) {
                times = sr.getTimes();
                output = new StringBuffer("Rule: " + test[i] + " - WEEKLY [times=");
                for (int j4 = 0; j4 < times.length; ++j4) {
                    output.append(times[j4] + (j4 < times.length - 1 ? "," : ""));
                }
                output.append("], [ordinal=" + sr.getOrdinal() + "], [days=");
                days = sr.getDaysOfWeek();
                for (int j5 = 0; j5 < days.length; ++j5) {
                    output.append(days[j5] + (j5 < days.length - 1 ? "," : ""));
                }
                output.append("]");
                continue;
            }
            if (sr.getRuleType() == 4) {
                times = sr.getTimes();
                output = new StringBuffer("Rule: " + test[i] + " - MONTHLY DATES [times=");
                for (int j6 = 0; j6 < times.length; ++j6) {
                    output.append(times[j6] + (j6 < times.length - 1 ? "," : ""));
                }
                output.append("], [ordinal=" + sr.getOrdinal() + "], [months=");
                months = sr.getMonths();
                for (int j7 = 0; j7 < months.length; ++j7) {
                    output.append(months[j7] + (j7 < months.length - 1 ? "," : ""));
                }
                output.append("], [dates=");
                String[] dates = sr.getDates();
                for (j = 0; j < dates.length; ++j) {
                    output.append(dates[j] + (j < dates.length - 1 ? "," : ""));
                }
                output.append("]");
                continue;
            }
            if (sr.getRuleType() == 5) {
                times = sr.getTimes();
                output = new StringBuffer("Rule: " + test[i] + " - MONTHLY WEEKS [times=");
                for (int j8 = 0; j8 < times.length; ++j8) {
                    output.append(times[j8] + (j8 < times.length - 1 ? "," : ""));
                }
                output.append("], [ordinal=" + sr.getOrdinal() + "], [months=");
                months = sr.getMonths();
                for (int j9 = 0; j9 < months.length; ++j9) {
                    output.append(months[j9] + (j9 < months.length - 1 ? "," : ""));
                }
                output.append("], [weekdays=");
                String[] dates = sr.getDaysOfWeek();
                for (j = 0; j < dates.length; ++j) {
                    output.append(dates[j] + (j < dates.length - 1 ? "," : ""));
                }
                output.append("]");
                continue;
            }
            if (sr.getRuleType() == 8) {
                times = sr.getTimes();
                output = new StringBuffer("Rule: " + test[i] + " - DAY OFFSET [times=");
                for (int j10 = 0; j10 < times.length; ++j10) {
                    output.append(times[j10] + (j10 < times.length - 1 ? "," : ""));
                }
                output.append("], [days=");
                days = sr.getDays();
                for (int j11 = 0; j11 < days.length; ++j11) {
                    output.append(days[j11] + (j11 < days.length - 1 ? "," : ""));
                }
                output.append("]");
                continue;
            }
            Trace.logError("ERROR Rule: " + test[i]);
        }
        Trace.logInfo(test.length + " rules validated and decomposed in " + (System.currentTimeMillis() - start) + "ms");
    }
}

