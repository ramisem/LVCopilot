/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.WorkflowCompleteEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.TaskCompleted;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class WorkflowCompleted
extends TaskCompleted {
    private static final String WORKFLOWDEFID = "workflowdefid";
    private static final String WORKFLOWDEFVERSIONID = "workflowdefversionid";
    private static final String WORKFLOWDEFVARIANTID = "workflowdefvariantid";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{WorkflowCompleteEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        return new Condition(new ConditionItem[]{new ConditionItem(WORKFLOWDEFID, "Workflow", "equals", ""), new ConditionItem(WORKFLOWDEFVERSIONID, "Workflow Version", "equals", ""), new ConditionItem(WORKFLOWDEFVARIANTID, "Workflow Variant", "equals", ""), new ConditionItem("taskdefid", "Task", "equals", ""), new ConditionItem("taskdefversionid", "Task Version", "equals", ""), new ConditionItem("taskdefvariantid", "Task Variant", "equals", ""), new ConditionItem("outputid", "Output Id", "equals", "")});
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        WorkflowCompleteEventObject workflowCompleteEventObject = (WorkflowCompleteEventObject)eventObject;
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
                    String string = conditionitem.equals(WORKFLOWDEFID) ? workflowCompleteEventObject.getWorkflowdefid() : (conditionitem.equals(WORKFLOWDEFVERSIONID) ? workflowCompleteEventObject.getWorkflowdefversionid() : (conditionitem.equals(WORKFLOWDEFVARIANTID) ? workflowCompleteEventObject.getWorkflowdefvariantid() : (conditionitem.equals("taskdefid") ? workflowCompleteEventObject.getTaskdefid() : (conditionitem.equals("taskdefversionid") ? workflowCompleteEventObject.getTaskdefversionid() : (conditionitem.equals("taskdefvariantid") ? workflowCompleteEventObject.getTaskdefvariantid() : (eventValue = conditionitem.equals("outputid") ? workflowCompleteEventObject.getOutputid() : ""))))));
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
        WorkflowCompleteEventObject workflowCompleteEventObject = (WorkflowCompleteEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            if (inputTokens[i].equals(WORKFLOWDEFID)) {
                inputValues.put(WORKFLOWDEFID, workflowCompleteEventObject.getWorkflowdefid());
                continue;
            }
            if (inputTokens[i].equals(WORKFLOWDEFVERSIONID)) {
                inputValues.put(WORKFLOWDEFVERSIONID, workflowCompleteEventObject.getWorkflowdefversionid());
                continue;
            }
            if (inputTokens[i].equals(WORKFLOWDEFVARIANTID)) {
                inputValues.put(WORKFLOWDEFVARIANTID, workflowCompleteEventObject.getWorkflowdefvariantid());
                continue;
            }
            if (inputTokens[i].equals("taskdefid")) {
                inputValues.put("taskdefid", workflowCompleteEventObject.getTaskdefid());
                continue;
            }
            if (inputTokens[i].equals("taskdefversionid")) {
                inputValues.put("taskdefversionid", workflowCompleteEventObject.getTaskdefversionid());
                continue;
            }
            if (inputTokens[i].equals("taskdefvariantid")) {
                inputValues.put("taskdefvariantid", workflowCompleteEventObject.getTaskdefvariantid());
                continue;
            }
            if (inputTokens[i].equals("outputid")) {
                inputValues.put("outputid", workflowCompleteEventObject.getOutputid());
                continue;
            }
            if (inputTokens[i].equals("sdcid")) {
                inputValues.put("sdcid", workflowCompleteEventObject.getTaskExecItems().getSdcid());
                continue;
            }
            if (inputTokens[i].equals("keyid1")) {
                inputValues.put("keyid1", workflowCompleteEventObject.getTaskExecItems().getKeyid1());
                continue;
            }
            if (inputTokens[i].equals("keyid2")) {
                inputValues.put("keyid2", workflowCompleteEventObject.getTaskExecItems().getKeyid2());
                continue;
            }
            if (!inputTokens[i].equals("keyid3")) continue;
            inputValues.put("keyid3", workflowCompleteEventObject.getTaskExecItems().getKeyid3());
        }
        return inputValues;
    }
}

