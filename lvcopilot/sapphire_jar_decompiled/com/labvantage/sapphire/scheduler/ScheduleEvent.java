/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTime
 *  org.joda.time.DateTimeZone
 *  org.joda.time.ReadableInstant
 */
package com.labvantage.sapphire.scheduler;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import sapphire.util.M18NUtil;

public class ScheduleEvent
implements Cloneable,
Comparable {
    public static final String EVENTSTATUS_FLAGGED = "F";
    public static final String EVENTSTATUS_SKIPPED = "O";
    public static final String EVENTSTATUS_SCHEDULED = "S";
    public static final String EVENTSTATUS_PLANNED = "P";
    public static final String EVENTSTATUS_DONE = "D";
    public static final String EVENTSTATUS_ERROR = "E";
    public static final String EVENTSTATUS_INACTIVE = "I";
    public static final int EVENTNUM_DEFAULT = 1;
    public static final String INSERT_SQL = "INSERT INTO scheduleevent ( scheduleplanid, scheduleplanitemid, eventdt, eventnum, eventstatus, createdt, executedt, originaleventdt, createby, createtool, tracelogid, adhocflag ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private String schedulePlanid;
    private String schedulePlanitemid;
    private DateTime eventDt;
    private DateTime originalEventDt;
    private String eventStatus;
    private String linkSdcid;
    private String linkKeyid1;
    private String linkKeyid2;
    private String linkKeyid3;
    private String schedulePlanItemDesc;
    private String scheduleTemplateSdcId;
    private String scheduleTemplateKeydId1;
    private String scheduleTemplateKeydId2;
    private String scheduleTemplateKeydId3;
    private int executeaheadTime;
    private String executeaheadTimeUnit;
    private String numCopies;
    private boolean adHocFlag;
    private int eventNum;
    private TimeZone timeZone;

    public ScheduleEvent() {
    }

    public ScheduleEvent(String schedulePlanid, String schedulePlanitemid, Calendar eventDt) {
        this(schedulePlanid, schedulePlanitemid, eventDt, TimeZone.getDefault());
    }

    public ScheduleEvent(String schedulePlanid, String schedulePlanitemid, Calendar eventDt, TimeZone timeZone) {
        this.schedulePlanid = schedulePlanid;
        this.timeZone = timeZone;
        this.schedulePlanitemid = schedulePlanitemid;
        this.eventNum = 1;
        this.eventDt = new DateTime((Object)eventDt, this.getDateTimeZone());
        this.originalEventDt = new DateTime((Object)((Calendar)eventDt.clone()), this.getDateTimeZone());
    }

    public ScheduleEvent(String schedulePlanid, String schedulePlanitemid, Calendar eventDt, String linksdcid, String linkkeyid1, String linkkeyid2, String linkkeyid3) {
        this(schedulePlanid, schedulePlanitemid, eventDt, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, TimeZone.getDefault());
    }

    public ScheduleEvent(String schedulePlanid, String schedulePlanitemid, Calendar eventDt, String linksdcid, String linkkeyid1, String linkkeyid2, String linkkeyid3, TimeZone timeZone) {
        this.schedulePlanid = schedulePlanid;
        this.schedulePlanitemid = schedulePlanitemid;
        this.timeZone = timeZone;
        this.eventDt = new DateTime((Object)eventDt, this.getDateTimeZone());
        this.originalEventDt = new DateTime((Object)((Calendar)eventDt.clone()), this.getDateTimeZone());
        this.linkSdcid = linksdcid;
        this.linkKeyid1 = linkkeyid1;
        this.linkKeyid2 = linkkeyid2;
        this.linkKeyid3 = linkkeyid3;
        this.adHocFlag = false;
        this.eventNum = 1;
    }

    public ScheduleEvent clone(Calendar newEventDt) {
        try {
            ScheduleEvent scheduleEvent = (ScheduleEvent)super.clone();
            newEventDt.setTimeZone(this.timeZone);
            scheduleEvent.setEventDt(newEventDt);
            scheduleEvent.setOriginalEventDt((Calendar)newEventDt.clone());
            return scheduleEvent;
        }
        catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    public void setLinkSdcid(String linkSdcid) {
        this.linkSdcid = linkSdcid;
    }

    public void setLinkKeyid1(String linkKeyid1) {
        this.linkKeyid1 = linkKeyid1;
    }

    public void setLinkKeyid2(String linkKeyid2) {
        this.linkKeyid2 = linkKeyid2;
    }

    public void setLinkKeyid3(String linkKeyid3) {
        this.linkKeyid3 = linkKeyid3;
    }

    public String getSchedulePlanid() {
        return this.schedulePlanid;
    }

    public void setSchedulePlanid(String schedulePlanid) {
        this.schedulePlanid = schedulePlanid;
    }

    public String getSchedulePlanitemid() {
        return this.schedulePlanitemid;
    }

    public void setSchedulePlanitemid(String schedulePlanitemid) {
        this.schedulePlanitemid = schedulePlanitemid;
    }

    public Calendar getEventDt() {
        this.eventDt = this.eventDt.withMillisOfSecond(0);
        return this.eventDt.toGregorianCalendar();
    }

    public DateTime getEventDateTime() {
        return this.eventDt;
    }

    public DateTime getOriginalEventDateTime() {
        return this.originalEventDt;
    }

    public Calendar getOriginalEventDt() {
        this.originalEventDt = this.originalEventDt.withMillisOfSecond(0);
        return this.originalEventDt.toGregorianCalendar();
    }

    public void setOriginalEventDt(Calendar originalEventDt) {
        this.originalEventDt = new DateTime((Object)originalEventDt, this.getDateTimeZone());
    }

    public boolean isAdHocFlag() {
        return this.adHocFlag;
    }

    public void setAdHocFlag(boolean adHocFlag) {
        this.adHocFlag = adHocFlag;
    }

    public String getLinkSdcid() {
        return this.linkSdcid;
    }

    public String getLinkKeyid1() {
        return this.linkKeyid1;
    }

    public String getLinkKeyid2() {
        return this.linkKeyid2;
    }

    public String getLinkKeyid3() {
        return this.linkKeyid3;
    }

    public void setEventDt(Calendar eventDt) {
        this.eventDt = new DateTime((Object)eventDt, this.getDateTimeZone());
    }

    public int getEventNum() {
        return this.eventNum;
    }

    public void setEventNum(int eventNum) {
        this.eventNum = eventNum;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public DateTimeZone getDateTimeZone() {
        DateTimeZone zone;
        try {
            zone = DateTimeZone.forID((String)this.timeZone.getID());
        }
        catch (IllegalArgumentException e) {
            zone = DateTimeZone.forTimeZone((TimeZone)this.timeZone);
        }
        return zone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getScheduleTemplateSdcId() {
        return this.scheduleTemplateSdcId;
    }

    public String getScheduleTemplateKeydId1() {
        return this.scheduleTemplateKeydId1;
    }

    public String getScheduleTemplateKeydId2() {
        return this.scheduleTemplateKeydId2;
    }

    public String getScheduleTemplateKeydId3() {
        return this.scheduleTemplateKeydId3;
    }

    public String getNumCopies() {
        return this.numCopies;
    }

    public void setScheduleTemplateSdcId(String scheduleTemplateSdcId) {
        this.scheduleTemplateSdcId = scheduleTemplateSdcId;
    }

    public void setScheduleTemplateKeydId1(String scheduleTemplateKeydId1) {
        this.scheduleTemplateKeydId1 = scheduleTemplateKeydId1;
    }

    public void setScheduleTemplateKeydId2(String scheduleTemplateKeydId2) {
        this.scheduleTemplateKeydId2 = scheduleTemplateKeydId2;
    }

    public void setScheduleTemplateKeydId3(String scheduleTemplateKeydId3) {
        this.scheduleTemplateKeydId3 = scheduleTemplateKeydId3;
    }

    public void setNumCopies(String numCopies) {
        this.numCopies = numCopies;
    }

    public String getSchedulePlanItemDesc() {
        if (this.schedulePlanItemDesc == null) {
            return "";
        }
        return this.schedulePlanItemDesc;
    }

    public void setSchedulePlanItemDesc(String schedulePlanItemDesc) {
        this.schedulePlanItemDesc = schedulePlanItemDesc;
    }

    public void setExecuteaheadTime(int time) {
        this.executeaheadTime = time;
    }

    public int getExecuteaheadTime() {
        return this.executeaheadTime;
    }

    public void setExecuteaheadTimeUnit(String timeUnit) {
        this.executeaheadTimeUnit = timeUnit;
    }

    public String getExecuteaheadTimeUnit() {
        return this.executeaheadTimeUnit;
    }

    public String getEventStatus() {
        return this.eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String toString() {
        M18NUtil m18NUtil = new M18NUtil();
        M18NUtil m18NUtilTimeZone = new M18NUtil(Locale.getDefault(), this.timeZone);
        return "SchedulePlanId = " + this.schedulePlanid + ", SchedulePlanItemId = " + this.schedulePlanitemid + ", EventDt = " + m18NUtilTimeZone.format(this.eventDt.toGregorianCalendar()) + ", OriginalEventDt = " + m18NUtilTimeZone.format(this.originalEventDt.toGregorianCalendar()) + ", TimeZone: " + this.timeZone.getDisplayName() + ",  EventDt in database: " + m18NUtil.format(this.eventDt.toGregorianCalendar());
    }

    public int compareTo(Object o1) {
        ScheduleEvent event2 = (ScheduleEvent)o1;
        return this.eventDt.isEqual((ReadableInstant)event2.eventDt) ? 0 : (this.eventDt.isBefore((ReadableInstant)event2.eventDt) ? -1 : 1);
    }
}

