/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddDataSetEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditDataSetEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class DataSetValueChanged
extends BaseSDCEventType {
    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{PostAddDataSetEventObject.class, PostEditDataSetEventObject.class};
    }

    @Override
    public boolean requiresSupplementalData(DataSet conditionValues) {
        return true;
    }

    @Override
    public boolean isSetupConditionsCascading() {
        return true;
    }

    @Override
    public Condition getSetupCondition(int index, DataSet setupConditionValues) {
        return this.getCascadingColumnValueSetupCondition(index, setupConditionValues, "sdidata");
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return this.getSDCColumnValueSetupConditions(eventTypeSetupConditions);
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        if (setupConditionValues != null && setupConditionValues.size() > 0) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            ArrayList<ConditionItem> conditionItems = this.getSDCColumnConditionItems(sdcid);
            conditionItems.addAll(this.getSDCColumnConditionItems("DataSet"));
            conditionItems.add(this.getEventSourceConditionItem(new String[]{"PostAddDataSet", "PostEditDataSet"}));
            return new Condition(conditionItems.toArray(new ConditionItem[conditionItems.size()]));
        }
        return null;
    }

    @Override
    public boolean supportsSdcid(String sdcid) {
        return true;
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        return this.hasSDIXXXEditedEventFired(eventObject, "dataset", "sdidata", eventplanconditions, eventObject instanceof PostEditDataSetEventObject, eventObject instanceof PostEditDataSetEventObject ? ((PostEditDataSetEventObject)eventObject).getBeforeSDIData() : null);
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

