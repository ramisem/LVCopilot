/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.TaskQueueEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class TaskQueueChanged
extends BaseEventType {
    private static final String TASKDEFID = "taskdefid";
    private static final String TASKDEFVERSIONID = "taskdefversionid";
    private static final String TASKDEFVARIANTID = "taskdefvariantid";
    private static final String WORKFLOWDEFID = "workflowdefid";
    private static final String WORKFLOWDEFVERSIONID = "workflowdefversionid";
    private static final String WORKFLOWDEFVARIANTID = "workflowdefvariantid";
    private static final String MODE = "mode";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{TaskQueueEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(TASKDEFID, "Task", "equals", ""), new ConditionItem(TASKDEFVERSIONID, "Task Version", "equals", ""), new ConditionItem(TASKDEFVARIANTID, "Task Variant", "equals", ""), new ConditionItem(WORKFLOWDEFID, "Workflow", "equals", ""), new ConditionItem(WORKFLOWDEFVERSIONID, "Workflow Version", "equals", ""), new ConditionItem(WORKFLOWDEFVARIANTID, "Workflow Variant", "equals", ""), new ConditionItem(MODE, "Mode", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        TaskQueueEventObject taskQueueEventObject = (TaskQueueEventObject)eventObject;
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
                    String string = conditionitem.equals(TASKDEFID) ? taskQueueEventObject.getTaskdefid(i) : (conditionitem.equals(TASKDEFVERSIONID) ? taskQueueEventObject.getTaskdefversionid(i) : (conditionitem.equals(TASKDEFVARIANTID) ? taskQueueEventObject.getTaskdefvariantid(i) : (conditionitem.equals(WORKFLOWDEFID) ? taskQueueEventObject.getWorkflowdefid(i) : (conditionitem.equals(WORKFLOWDEFVERSIONID) ? taskQueueEventObject.getWorkflowdefversionid(i) : (conditionitem.equals(WORKFLOWDEFVARIANTID) ? taskQueueEventObject.getWorkflowdefvariantid(i) : (eventValue = conditionitem.equals(MODE) ? taskQueueEventObject.getMode() : ""))))));
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
        TaskQueueEventObject taskQueueEventObject = (TaskQueueEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(TASKDEFID)) {
                inputValues.put(TASKDEFID, taskQueueEventObject.getTaskdefid());
                continue;
            }
            if (inputTokens[i].equals(TASKDEFVERSIONID)) {
                inputValues.put(TASKDEFVERSIONID, taskQueueEventObject.getTaskdefversionid());
                continue;
            }
            if (inputTokens[i].equals(TASKDEFVARIANTID)) {
                inputValues.put(TASKDEFVARIANTID, taskQueueEventObject.getTaskdefvariantid());
                continue;
            }
            if (inputTokens[i].equals(WORKFLOWDEFID)) {
                inputValues.put(WORKFLOWDEFID, taskQueueEventObject.getWorkflowdefid());
                continue;
            }
            if (inputTokens[i].equals(WORKFLOWDEFVERSIONID)) {
                inputValues.put(WORKFLOWDEFVERSIONID, taskQueueEventObject.getWorkflowdefversionid());
                continue;
            }
            if (inputTokens[i].equals(WORKFLOWDEFVARIANTID)) {
                inputValues.put(WORKFLOWDEFVARIANTID, taskQueueEventObject.getWorkflowdefvariantid());
                continue;
            }
            if (!inputTokens[i].equals(MODE)) continue;
            inputValues.put(MODE, taskQueueEventObject.getMode());
        }
        return inputValues;
    }
}

