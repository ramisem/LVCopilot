/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SendBulletinEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SendBulletin
extends BaseEventType {
    private static final String USERNAME = "username";
    private static final String DESCRIPTION = "description";
    private static final String BODY = "body";
    private static final String PRIORITY = "priority";
    private static final String URL = "url";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{SendBulletinEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(USERNAME, "Username", "equals", ""), new ConditionItem(DESCRIPTION, "Description", "S", ""), new ConditionItem(BODY, "Body", "S", ""), new ConditionItem(PRIORITY, "Priority", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        SendBulletinEventObject sendBulletinEventObject = (SendBulletinEventObject)eventObject;
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
                    String string = conditionitem.equals(USERNAME) ? sendBulletinEventObject.getUsernames()[i] : (conditionitem.equals(DESCRIPTION) ? sendBulletinEventObject.getDescription() : (conditionitem.equals(BODY) ? sendBulletinEventObject.getBody() : (conditionitem.equals(PRIORITY) ? sendBulletinEventObject.getPriority() : (eventValue = conditionitem.equals(URL) ? sendBulletinEventObject.getUrl() : ""))));
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
        SendBulletinEventObject sendBulletinEventObject = (SendBulletinEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(USERNAME)) {
                inputValues.put(USERNAME, StringUtil.arrayToString(sendBulletinEventObject.getUsernames(), ";"));
                continue;
            }
            if (inputTokens[i].equals(DESCRIPTION)) {
                inputValues.put(DESCRIPTION, sendBulletinEventObject.getDescription());
                continue;
            }
            if (inputTokens[i].equals(BODY)) {
                inputValues.put(BODY, sendBulletinEventObject.getBody());
                continue;
            }
            if (inputTokens[i].equals(PRIORITY)) {
                inputValues.put(PRIORITY, sendBulletinEventObject.getPriority());
                continue;
            }
            if (!inputTokens[i].equals(URL)) continue;
            inputValues.put(URL, sendBulletinEventObject.getUrl());
        }
        return inputValues;
    }

    @Override
    public String getUserContextToken() {
        return "[username]";
    }
}

