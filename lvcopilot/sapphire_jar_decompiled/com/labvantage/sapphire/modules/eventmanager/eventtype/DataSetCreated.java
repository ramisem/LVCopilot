/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddDataSetEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class DataSetCreated
extends BaseSDCEventType {
    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{PostAddEventObject.class, PostAddDataSetEventObject.class};
    }

    @Override
    public boolean requiresSupplementalData(DataSet conditionValues) {
        for (int i = 0; i < conditionValues.size(); ++i) {
            if (!conditionValues.getValue(i, "conditionitem").contains("_") || conditionValues.getValue(i, "conditionitem").startsWith("sdidata")) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsSdcid(String sdcid) {
        return true;
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        if (eventTypeSetupConditions != null && eventTypeSetupConditions.size() == 1 && eventTypeSetupConditions.getValue(0, "conditionitem").equals("sdcid") && eventTypeSetupConditions.getValue(0, "operator1").equals("equals") && eventTypeSetupConditions.getValue(0, "value1").length() > 0) {
            return null;
        }
        Condition[] conditions = new Condition[]{new Condition(this.getSDCConditionItem())};
        return conditions;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        if (setupConditionValues != null && setupConditionValues.size() > 0) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            ArrayList<ConditionItem> conditionItems = this.getSDCColumnConditionItems(sdcid);
            conditionItems.addAll(this.getSDCColumnConditionItems("DataSet"));
            conditionItems.add(this.getEventSourceConditionItem(new String[]{"PostAddDataSet"}));
            return new Condition(conditionItems.toArray(new ConditionItem[conditionItems.size()]));
        }
        return null;
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        return this.hasSDIXXXCreatedEventFired(eventObject, "dataset", "sdidata", eventplanconditions);
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        if (eventObject instanceof BaseSDCEventObject) {
            BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
            HashMap tokenValues = this.getPrimaryTokenValues(sdcEventObject.getSdcid(), eventObject, inputTokens);
            tokenValues.putAll(this.getTableTokenValues(eventObject, "dataset", "sdidata", inputTokens));
            return tokenValues;
        }
        return null;
    }

    @Override
    public String getUserContextToken() {
        return "[sdidata_s_assignedanalyst]";
    }
}

