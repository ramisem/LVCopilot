/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTime
 *  org.joda.time.DateTimeZone
 *  org.joda.time.LocalDate
 */
package com.labvantage.sapphire.scheduler;

import java.util.Calendar;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class ScheduleExcludeEvent {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private Calendar excludeDt;
    private LocalDate excludeJodaDate;
    private String excludeRule;
    private boolean isRecurring;
    private TimeZone timezone;

    public ScheduleExcludeEvent(Calendar eventDt, TimeZone timezone) {
        this.excludeJodaDate = new LocalDate((Object)((Calendar)eventDt.clone()));
        this.isRecurring = false;
        this.excludeDt = (Calendar)eventDt.clone();
        this.timezone = timezone;
        this.excludeDt.setTimeZone(timezone);
        this.excludeDt.getTime();
    }

    public ScheduleExcludeEvent(String excludeRule) {
        this.excludeRule = excludeRule;
        this.isRecurring = true;
    }

    public boolean isExcluded(Calendar eventDt) {
        boolean isScheduleExcluded = false;
        if (this.isRecurring) {
            if (this.excludeRule != null && this.excludeRule.length() > 0) {
                DateTime day = new DateTime((Object)((Calendar)eventDt.clone()));
                int dayOfWeek = day.getDayOfWeek();
                if (dayOfWeek == 1 && "Mon".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 2 && "Tue".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 3 && "Wed".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 4 && "Thu".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 5 && "Fri".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 6 && "Sat".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                } else if (dayOfWeek == 7 && "Sun".equals(this.excludeRule)) {
                    isScheduleExcluded = true;
                }
            }
        } else {
            LocalDate day = new LocalDate((Object)((Calendar)eventDt.clone()), DateTimeZone.forID((String)this.timezone.getID()));
            if (this.excludeJodaDate != null && day.getYear() == this.excludeJodaDate.getYear() && day.getMonthOfYear() == this.excludeJodaDate.getMonthOfYear() && day.getDayOfMonth() == this.excludeJodaDate.getDayOfMonth()) {
                isScheduleExcluded = true;
            }
        }
        return isScheduleExcluded;
    }

    public String getExcludeRule() {
        return this.excludeRule;
    }
}

