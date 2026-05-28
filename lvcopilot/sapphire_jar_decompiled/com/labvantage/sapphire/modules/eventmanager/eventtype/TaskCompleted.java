/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.TaskCompleteEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class TaskCompleted
extends BaseEventType {
    protected static final String TASKDEFID = "taskdefid";
    protected static final String TASKDEFVERSIONID = "taskdefversionid";
    protected static final String TASKDEFVARIANTID = "taskdefvariantid";
    protected static final String OUTPUTID = "outputid";
    protected static final String SDCID = "sdcid";
    protected static final String KEYID1 = "keyid1";
    protected static final String KEYID2 = "keyid2";
    protected static final String KEYID3 = "keyid3";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{TaskCompleteEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(TASKDEFID, "Task", "equals", ""), new ConditionItem(TASKDEFVERSIONID, "Version", "equals", ""), new ConditionItem(TASKDEFVARIANTID, "Variant", "equals", ""), new ConditionItem(OUTPUTID, "Output Id", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        TaskCompleteEventObject taskCompleteEventObject = (TaskCompleteEventObject)eventObject;
        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
        int eventItemsFired = eventItems.size();
        if (eventItems != null && eventItems.size() > 0) {
            for (int i = 0; i < eventItems.size(); ++i) {
                BaseEventItem eventItem = eventItems.get(i);
                eventItem.setHasFired(true);
                for (int j = 0; j < eventplanconditions.size(); ++j) {
                    String eventValue;
                    String conditionitem = eventplanconditions.getValue(j, "conditionitem");
                    String value1 = eventplanconditions.getValue(j, "value1");
                    String string = conditionitem.equals(TASKDEFID) ? taskCompleteEventObject.getTaskdefid() : (conditionitem.equals(TASKDEFVERSIONID) ? taskCompleteEventObject.getTaskdefversionid() : (conditionitem.equals(TASKDEFVARIANTID) ? taskCompleteEventObject.getTaskdefvariantid() : (eventValue = conditionitem.equals(OUTPUTID) ? taskCompleteEventObject.getOutputid() : "")));
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
        TaskCompleteEventObject taskCompleteEventObject = (TaskCompleteEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(TASKDEFID)) {
                inputValues.put(TASKDEFID, taskCompleteEventObject.getTaskdefid());
                continue;
            }
            if (inputTokens[i].equals(TASKDEFVERSIONID)) {
                inputValues.put(TASKDEFVERSIONID, taskCompleteEventObject.getTaskdefversionid());
                continue;
            }
            if (inputTokens[i].equals(TASKDEFVARIANTID)) {
                inputValues.put(TASKDEFVARIANTID, taskCompleteEventObject.getTaskdefvariantid());
                continue;
            }
            if (inputTokens[i].equals(OUTPUTID)) {
                inputValues.put(OUTPUTID, taskCompleteEventObject.getOutputid());
                continue;
            }
            if (inputTokens[i].equals(SDCID)) {
                inputValues.put(SDCID, taskCompleteEventObject.getTaskExecItems().getSdcid());
                continue;
            }
            if (inputTokens[i].equals(KEYID1)) {
                inputValues.put(KEYID1, taskCompleteEventObject.getTaskExecItems().getKeyid1());
                continue;
            }
            if (inputTokens[i].equals(KEYID2)) {
                inputValues.put(KEYID2, taskCompleteEventObject.getTaskExecItems().getKeyid2());
                continue;
            }
            if (!inputTokens[i].equals(KEYID3)) continue;
            inputValues.put(KEYID3, taskCompleteEventObject.getTaskExecItems().getKeyid3());
        }
        return inputValues;
    }
}

