/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostDataReleaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class DataReleaseUnrelease
extends BaseSDCEventType {
    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{PostDataReleaseEventObject.class};
    }

    @Override
    public boolean requiresSupplementalData(DataSet conditionValues) {
        return true;
    }

    @Override
    public String[] getSupplementalRequestItems(DataSet conditionValues) {
        return new String[]{"dataspec"};
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
            return new Condition(conditionItems.toArray(new ConditionItem[conditionItems.size()]));
        }
        return null;
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        PostDataReleaseEventObject dataReleaseEventObject = (PostDataReleaseEventObject)eventObject;
        if (dataReleaseEventObject.isRelease()) {
            return false;
        }
        return false;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        return null;
    }
}

