/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.calendar;

import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class LVCalendarCache {
    protected static final int CALENDAR_CACHETIME = 60;
    protected String calendarid = null;
    protected String sourceflag = null;
    protected String sharedflag = null;
    protected String externalOptions = null;
    protected WorkHours corehours = null;
    protected Instant loadedFromInstantUTC = null;
    protected Instant loadedToInstantUTC = null;
    protected List<CalendarItem> calendarItems;
    protected HashMap<String, CalendarItem> allOverrides;
    protected List<CalendarItem> allRepeats;
    protected ZoneId timezone = null;
    protected boolean isSharedAndNoTimezone = false;
    private boolean fullyLoaded = false;
    private boolean overridesLoaded;
    private boolean repeatsLoaded;
    private Calendar loadedTime = null;

    public LVCalendarCache(String calendarid) {
        this.calendarid = calendarid;
        this.sourceflag = "";
        this.externalOptions = "";
        this.corehours = null;
        this.sharedflag = "";
        this.calendarItems = new ArrayList<CalendarItem>();
        this.allOverrides = new HashMap();
        this.allRepeats = new ArrayList<CalendarItem>();
        this.loadedTime = null;
        this.timezone = null;
        this.isSharedAndNoTimezone = false;
    }

    public LVCalendarCache(LVCalendarCache cache) {
        this.calendarid = cache.calendarid;
        this.sourceflag = cache.sourceflag;
        this.externalOptions = cache.externalOptions;
        this.corehours = cache.corehours;
        this.sharedflag = cache.sharedflag;
        this.loadedFromInstantUTC = cache.loadedFromInstantUTC;
        this.loadedToInstantUTC = cache.loadedToInstantUTC;
        this.calendarItems = cache.calendarItems;
        this.fullyLoaded = cache.fullyLoaded;
        this.allOverrides = cache.allOverrides;
        this.overridesLoaded = cache.overridesLoaded;
        this.allRepeats = cache.allRepeats;
        this.repeatsLoaded = cache.repeatsLoaded;
        this.timezone = cache.timezone;
        this.isSharedAndNoTimezone = cache.isSharedAndNoTimezone;
        this.loadedTime = null;
    }

    public boolean isFullyLoaded() {
        return this.fullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
    }

    public boolean isOverridesLoaded() {
        return this.overridesLoaded;
    }

    public void setOverridesLoaded(boolean overridesLoaded) {
        this.overridesLoaded = overridesLoaded;
    }

    public boolean isRepeatsLoaded() {
        return this.repeatsLoaded;
    }

    public void setRepeatsLoaded(boolean repeatsLoaded) {
        this.repeatsLoaded = repeatsLoaded;
    }

    public Calendar getLoadedTime() {
        return this.loadedTime;
    }

    public void setLoadedTime() {
        this.loadedTime = Calendar.getInstance();
    }

    public void setLoadedTime(Calendar loaded) {
        this.loadedTime = loaded;
    }
}

