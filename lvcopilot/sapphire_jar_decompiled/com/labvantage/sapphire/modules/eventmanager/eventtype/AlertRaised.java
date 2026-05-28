/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.AlertRaisedEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AlertRaised
extends BaseEventType {
    protected static final String INCIDENTID = "incidentid";
    protected static final String INCIDENTTYPE = "incidenttype";
    protected static final String SEVERITY = "severity";
    protected static final String DESCRIPTION = "description";
    protected static final String EXPLANATION = "explanation";
    protected static final String CAUSALSDCID = "causalsdcid";
    protected static final String CAUSALKEYID1 = "causalkeyid1";
    protected static final String CAUSALKEYID2 = "causalkeyid2";
    protected static final String CAUSALKEYID3 = "causalkeyid3";

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{AlertRaisedEventObject.class};
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return null;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        ArrayList<ConditionItem> conditions = new ArrayList<ConditionItem>();
        conditions.add(new ConditionItem(INCIDENTID, "Incident Id", "S", ""));
        conditions.add(new ConditionItem(INCIDENTTYPE, "Type", "S", ""));
        PropertyList editorProps = new PropertyList();
        editorProps.setProperty("mode", "dropdownlist");
        editorProps.setProperty("displayvalue", "Warning;Failure");
        conditions.add(new ConditionItem(SEVERITY, "Severity", "equals", editorProps.toXMLString()));
        conditions.add(new ConditionItem(DESCRIPTION, "Description", "S", ""));
        conditions.add(new ConditionItem(EXPLANATION, "Explanation", "S", ""));
        editorProps = new PropertyList();
        editorProps.setProperty("mode", "dropdownlist");
        editorProps.setProperty("sqlcode", "20018");
        conditions.add(new ConditionItem(CAUSALSDCID, "Causal SDC", "equals", editorProps.toXMLString()));
        conditions.add(new ConditionItem(CAUSALKEYID1, "Causal Keyid1", "S", ""));
        conditions.add(new ConditionItem(CAUSALKEYID2, "Causal Keyid2", "S", ""));
        conditions.add(new ConditionItem(CAUSALKEYID3, "Causal Keyid3", "S", ""));
        return new Condition(conditions.toArray(new ConditionItem[0]));
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        AlertRaisedEventObject alertRaisedEventObject = (AlertRaisedEventObject)eventObject;
        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
        int eventItemsFired = eventItems.size();
        if (eventItems != null && eventItems.size() > 0) {
            for (int i = 0; i < eventItems.size(); ++i) {
                BaseEventItem eventItem = eventItems.get(i);
                eventItem.setHasFired(true);
                for (int j = 0; j < eventplanconditions.size(); ++j) {
                    String conditionitem = eventplanconditions.getValue(j, "conditionitem");
                    String value1 = eventplanconditions.getValue(j, "value1");
                    String operator1 = eventplanconditions.getValue(j, "operator1");
                    String eventValue = this.getEventObjectValue(alertRaisedEventObject, conditionitem);
                    if (ConditionItem.conditionMatch(eventValue, value1, "", operator1)) continue;
                    eventItem.setHasFired(false);
                    --eventItemsFired;
                }
            }
            return eventItemsFired > 0;
        }
        return true;
    }

    private String getEventObjectValue(AlertRaisedEventObject alertRaisedEventObject, String conditionitem) {
        return conditionitem.equals(INCIDENTID) ? alertRaisedEventObject.getIncidentId() : (conditionitem.equals(INCIDENTTYPE) ? alertRaisedEventObject.getIncidentType() : (conditionitem.equals(SEVERITY) ? alertRaisedEventObject.getSeverity() : (conditionitem.equals(DESCRIPTION) ? alertRaisedEventObject.getDescription() : (conditionitem.equals(EXPLANATION) ? alertRaisedEventObject.getExplanation() : (conditionitem.equals(CAUSALSDCID) ? alertRaisedEventObject.getCausalSDCid() : (conditionitem.equals(CAUSALKEYID1) ? alertRaisedEventObject.getCausalKeyid1() : (conditionitem.equals(CAUSALKEYID2) ? alertRaisedEventObject.getCausalKeyid2() : (conditionitem.equals(CAUSALKEYID3) ? alertRaisedEventObject.getCausalKeyid3() : ""))))))));
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        AlertRaisedEventObject alertRaisedEventObject = (AlertRaisedEventObject)eventObject;
        HashMap<String, String> inputValues = new HashMap<String, String>();
        for (int i = 0; i < inputTokens.length; ++i) {
            inputValues.put(inputTokens[i], this.getEventObjectValue(alertRaisedEventObject, inputTokens[i]));
        }
        return inputValues;
    }
}

