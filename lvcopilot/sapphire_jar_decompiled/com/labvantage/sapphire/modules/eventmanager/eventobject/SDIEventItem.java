/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.modules.eventmanager.EventPlan;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SDIEventItem
extends BaseEventItem {
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private int keyCols;
    private HashSet<String> eventPlans = new HashSet();
    private HashMap<String, ArrayList<HashMap>> eventPlanEvents = new HashMap();
    private int primaryIndex = -1;

    public SDIEventItem(String sdcid, String keyid1) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyCols = 1;
    }

    public SDIEventItem(String sdcid, String keyid1, String keyid2) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyCols = 2;
    }

    public SDIEventItem(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
        this.keyCols = keyid2.equals("(null)") && keyid3.equals("(null)") ? 1 : (keyid3.equals("(null)") ? 2 : 3);
    }

    public boolean matches(String keyid1, String keyid2, String keyid3) {
        if (this.keyCols == 1 && this.keyid1.equals(keyid1)) {
            return true;
        }
        if (this.keyCols == 2 && this.keyid1.equals(keyid1) && this.keyid2.equals(keyid2)) {
            return true;
        }
        return this.keyCols == 3 && this.keyid1.equals(keyid1) && this.keyid2.equals(keyid2) && this.keyid3.equals(keyid3);
    }

    public void addEventPlan(String eventplanid, String eventplanversionid) {
        this.eventPlans.add(eventplanid + ";" + eventplanversionid);
    }

    public void addEventPlan(ArrayList<BaseEventItem> origEventItems) {
        for (BaseEventItem eventItem : origEventItems) {
            SDIEventItem sdiEventItem = (SDIEventItem)eventItem;
            if (sdiEventItem.eventPlans.size() <= 0 || !this.matches(sdiEventItem.getKeyid1(), sdiEventItem.getKeyid2(), sdiEventItem.getKeyid3())) continue;
            for (String eventplankey : sdiEventItem.eventPlans) {
                if (this.eventPlans.contains(eventplankey)) continue;
                this.eventPlans.add(eventplankey);
            }
        }
    }

    public boolean hasEventPlan(EventPlan eventPlan) {
        return eventPlan != null && (eventPlan.isGlobal() || this.eventPlans.contains(eventPlan.getEventplanid() + ";" + eventPlan.getEventplanversionid()));
    }

    public boolean hasGlobalOverride(EventPlan eventPlan) {
        return eventPlan != null && this.eventPlans.contains(eventPlan.getEventplanid() + ";" + eventPlan.getEventplanversionid());
    }

    public int getKeyCols() {
        return this.keyCols;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public String getKeyid2() {
        return this.keyCols >= 2 ? this.keyid2 : "(null)";
    }

    public String getKeyid3() {
        return this.keyCols >= 3 ? this.keyid3 : "(null)";
    }

    public int getPrimaryIndex() {
        return this.primaryIndex;
    }

    public void setPrimaryIndex(int primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    @Override
    public void logEvent(String eventplanid, String eventplanversionid, String eventplanitemid, String action, String actionitemid, String summary, String log) {
        ArrayList eventPlanLogEvents = this.eventPlanEvents.get(eventplanid + ";" + eventplanversionid + ";" + eventplanitemid);
        if (eventPlanLogEvents == null) {
            eventPlanLogEvents = new ArrayList();
            this.eventPlanEvents.put(eventplanid + ";" + eventplanversionid + ";" + eventplanitemid, eventPlanLogEvents);
        }
        HashMap<String, String> eventPlanLogEvent = new HashMap<String, String>();
        eventPlanLogEvent.put("action", action);
        eventPlanLogEvent.put("actionitemid", actionitemid);
        eventPlanLogEvent.put("summary", summary);
        eventPlanLogEvent.put("log", log);
        eventPlanLogEvents.add(eventPlanLogEvent);
    }

    @Override
    public ArrayList getLogEvents(String eventplanid, String eventplanversionid, String eventplanitemid) {
        return this.eventPlanEvents.get(eventplanid + ";" + eventplanversionid + ";" + eventplanitemid);
    }

    @Override
    public String toString() {
        return new SDI(this.getSdcid(), this.getKeyid1(), this.getKeyid2(), this.getKeyid3()).toString();
    }
}

