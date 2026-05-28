/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.EventPlan;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import java.util.ArrayList;

public abstract class BaseEventObject {
    public static final int EVENTTYPE_GENERAL = 0;
    public static final int EVENTTYPE_SDI = 1;
    public static final int EVENTTYPE_DATASET = 2;
    public static final int EVENTTYPE_DATAITEM = 3;
    public static final int EVENTTYPE_WORKITEM = 4;
    public static final int EVENTTYPE_NOTE = 5;
    private String eventid;
    private Object[] eventData;
    private Object supplementalData;
    private int eventItemType = 0;
    private int eventItemTypeCopy;
    private ArrayList<BaseEventItem> eventItems = new ArrayList();
    private ArrayList<BaseEventItem> eventItemsCopy = new ArrayList();
    private ArrayList<BaseEventItem> processingeventItems = new ArrayList();
    private EventPlan eventPlan;
    public static final String EVENTSOURCE_CONDITIONITEM = "eventsource";
    public static final String EVENTSOURCE_CONDITIONITEMDESC = "Event Source";

    public BaseEventObject(String eventid, Object[] eventData) {
        this.eventid = eventid;
        this.eventData = eventData;
    }

    public void setEventid(String eventid) {
        this.eventid = eventid;
    }

    public String getEventid() {
        return this.eventid;
    }

    public void setEventData(int index, Object data) {
        if (index >= this.eventData.length || index < 0) {
            throw new IndexOutOfBoundsException("Event data index out of bounds - define size in event object constructor!");
        }
        this.eventData[index] = data;
    }

    public Object[] getEventData() {
        return this.eventData;
    }

    public Object getSupplementalData() {
        return this.supplementalData;
    }

    public void setSupplementalData(Object supplementalData) {
        this.supplementalData = supplementalData;
    }

    public int getEventItemType() {
        return this.eventItemType;
    }

    public void setEventItemType(int eventItemType) {
        this.eventItemType = eventItemType;
    }

    public void addEventItem(BaseEventItem eventItem) {
        this.eventItems.add(eventItem);
    }

    public ArrayList<BaseEventItem> getEventItems() {
        return this.eventItems;
    }

    public boolean hasEventItems() {
        return this.eventItems.size() > 0;
    }

    public void clearEventItems() {
        this.eventItems.clear();
    }

    public ArrayList<BaseEventItem> getOrigEventItems() {
        return this.eventItemsCopy;
    }

    public void copyEventItems() {
        this.eventItemTypeCopy = this.eventItemType;
        this.eventItemsCopy.clear();
        this.eventItemsCopy.addAll(this.eventItems);
    }

    public void resetEventItems() {
        this.eventItems.clear();
        this.eventItems.addAll(this.eventItemsCopy);
        this.eventItemType = this.eventItemTypeCopy;
    }

    public abstract int addEventItems();

    public void addProcessingEventItem(BaseEventItem eventItem) {
        this.processingeventItems.add(eventItem);
    }

    public void addProcessingEventItems(ArrayList<BaseEventItem> eventItems) {
        this.processingeventItems.addAll(eventItems);
    }

    public void clearProcessingSet() {
        this.processingeventItems.clear();
    }

    public ArrayList<BaseEventItem> getProcessingEventItems() {
        return this.processingeventItems;
    }

    public void setCurrentEventPlan(EventPlan eventPlan) {
        this.eventPlan = eventPlan;
    }

    public EventPlan getCurrentEventPlan() {
        return this.eventPlan;
    }

    public void setAllEventItemsFired() {
        for (int i = 0; i < this.eventItems.size(); ++i) {
            BaseEventItem eventItem = this.eventItems.get(i);
            eventItem.setHasFired(true);
        }
    }
}

