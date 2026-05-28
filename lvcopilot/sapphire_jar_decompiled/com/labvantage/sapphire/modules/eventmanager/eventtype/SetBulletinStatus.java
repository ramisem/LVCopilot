/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SetBulletinStatusEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class SetBulletinStatus
extends BaseEventType {
    private static final String USERNAME = "username";
    private static final String STATUS = "status";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{SetBulletinStatusEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(USERNAME, "Username", "equals", ""), new ConditionItem(STATUS, "Status", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        SetBulletinStatusEventObject setBulletinStatusEventObject = (SetBulletinStatusEventObject)eventObject;
        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
        int eventItemsFired = eventItems.size();
        if (eventItems != null && eventItems.size() > 0) {
            for (int i = 0; i < eventItems.size(); ++i) {
                EventItem eventItem = (EventItem)eventItems.get(i);
                eventItem.setHasFired(true);
                for (int j = 0; j < eventplanconditions.size(); ++j) {
                    String eventValue;
                    String conditionitem = eventplanconditions.getValue(j, "conditionitem");
                    String value1 = eventplanconditions.getValue(j, "value1");
                    String string = conditionitem.equals(USERNAME) ? setBulletinStatusEventObject.getUsername() : (eventValue = conditionitem.equals(STATUS) ? setBulletinStatusEventObject.getStatus() : "");
                    if (eventValue.equals(value1)) continue;
                    eventItem.setHasFired(false);
                    --eventItemsFired;
                }
            }
            return eventItemsFired > 0;
        }
        return true;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        SetBulletinStatusEventObject setBulletinStatusEventObject = (SetBulletinStatusEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(USERNAME)) {
                inputValues.put(USERNAME, setBulletinStatusEventObject.getUsername());
                continue;
            }
            if (!inputTokens[i].equals(STATUS)) continue;
            inputValues.put(STATUS, setBulletinStatusEventObject.getStatus());
        }
        return inputValues;
    }

    @Override
    public String getUserContextToken() {
        return "[username]";
    }
}

