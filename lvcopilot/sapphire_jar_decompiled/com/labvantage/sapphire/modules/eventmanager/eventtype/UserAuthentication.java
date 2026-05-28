/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.UserAuthenticationEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.util.regex.RegexUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class UserAuthentication
extends BaseEventType {
    private static final String USERNAME = "username";
    private static final String DATABASEID = "databaseid";
    private static final String ACTIVITY = "activity";
    private static final String DISABLEREASON = "disablereason";
    private static final String LOGONATTEMPTS = "logonattempts";
    private static final String LOGONSUCCESS = "logonsuccess";
    private static final String ERRORID = "errorid";
    private static final String ERRORMESSAGE = "errormessage";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{UserAuthenticationEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(USERNAME, "Username", "equals", ""), new ConditionItem(DATABASEID, "Database", "equals", ""), new ConditionItem(LOGONATTEMPTS, "Logon Attempts", "N", ""), new ConditionItem(LOGONSUCCESS, "Logon Success", "equals", ""), new ConditionItem(ERRORID, "Error Id", "equals", ""), new ConditionItem(ERRORMESSAGE, "Error Message", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        UserAuthenticationEventObject loginFailureEventObject = (UserAuthenticationEventObject)eventObject;
        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
        if (eventItems != null && eventItems.size() > 0) {
            ((EventItem)eventItems.get(0)).setHasFired(true);
            for (int i = 0; i < eventplanconditions.size(); ++i) {
                String eventValue;
                String conditionitem = eventplanconditions.getValue(i, "conditionitem");
                String value1 = eventplanconditions.getValue(i, "value1");
                String string = conditionitem.equals(USERNAME) ? loginFailureEventObject.getUsername() : (conditionitem.equals(DATABASEID) ? loginFailureEventObject.getDatabaseid() : (conditionitem.equals(ACTIVITY) ? loginFailureEventObject.getActivity() : (conditionitem.equals(DISABLEREASON) ? loginFailureEventObject.getDisableReason() : (conditionitem.equals(LOGONATTEMPTS) ? String.valueOf(loginFailureEventObject.getLogonAttempts()) : (conditionitem.equals(LOGONSUCCESS) ? loginFailureEventObject.getLogonSuccess() : (conditionitem.equals(ERRORID) ? loginFailureEventObject.getErrorid() : (eventValue = conditionitem.equals(ERRORMESSAGE) ? loginFailureEventObject.getErrormessage() : "")))))));
                if (eventValue.length() != 0 && (eventValue.equals(value1) || value1.equalsIgnoreCase("(any)") || RegexUtil.wildcardMatch(eventValue, value1))) continue;
                ((EventItem)eventItems.get(0)).setHasFired(false);
                return false;
            }
        }
        return true;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        UserAuthenticationEventObject loginFailureEventObject = (UserAuthenticationEventObject)eventObject;
        HashMap<String, Object> inputValues = new HashMap<String, Object>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(USERNAME)) {
                inputValues.put(USERNAME, loginFailureEventObject.getUsername());
                continue;
            }
            if (inputTokens[i].equals(DATABASEID)) {
                inputValues.put(DATABASEID, loginFailureEventObject.getDatabaseid());
                continue;
            }
            if (inputTokens[i].equals(ACTIVITY)) {
                inputValues.put(ACTIVITY, loginFailureEventObject.getActivity());
                continue;
            }
            if (inputTokens[i].equals(DISABLEREASON)) {
                inputValues.put(DISABLEREASON, loginFailureEventObject.getDisableReason());
                continue;
            }
            if (inputTokens[i].equals(LOGONATTEMPTS)) {
                inputValues.put(LOGONATTEMPTS, loginFailureEventObject.getLogonAttempts());
                continue;
            }
            if (inputTokens[i].equals(LOGONSUCCESS)) {
                inputValues.put(LOGONSUCCESS, loginFailureEventObject.getLogonSuccess());
                continue;
            }
            if (inputTokens[i].equals(ERRORID)) {
                inputValues.put(ERRORID, loginFailureEventObject.getErrorid());
                continue;
            }
            if (!inputTokens[i].equals(ERRORMESSAGE)) continue;
            inputValues.put(ERRORMESSAGE, loginFailureEventObject.getErrormessage());
        }
        return inputValues;
    }

    @Override
    public String getUserContextToken() {
        return "[username]";
    }
}

