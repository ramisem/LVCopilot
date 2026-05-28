/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class SDICreated
extends BaseSDCEventType {
    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{PostAddEventObject.class};
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
            conditionItems.add(this.getEventSourceConditionItem(new String[]{"PostAdd"}));
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
        DataSet primary;
        if (eventObject instanceof PostAddEventObject && (primary = ((PostAddEventObject)eventObject).getDataSet("primary")) != null && eventObject.hasEventItems() && eventplanconditions.size() >= 1) {
            String sdcid = ((BaseSDCEventObject)eventObject).getSdcid();
            if (eventplanconditions.getValue(0, "value1").equals(sdcid)) {
                if (eventplanconditions.size() > 1) {
                    String tableid = this.sdcProps.getProperty("tableid");
                    ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
                    int eventItemsFired = eventItems.size();
                    for (int i = 0; i < eventItems.size(); ++i) {
                        SDIEventItem sdiEventItem = (SDIEventItem)eventItems.get(i);
                        sdiEventItem.setHasFired(true);
                        if (sdiEventItem.hasEventPlan(eventObject.getCurrentEventPlan())) {
                            for (int j = 1; sdiEventItem.hasFired() && j < eventplanconditions.size(); ++j) {
                                if (this.conditionMatch(primary, sdiEventItem.getDataIndex(), tableid, eventplanconditions, j)) continue;
                                sdiEventItem.setHasFired(false);
                                --eventItemsFired;
                            }
                            continue;
                        }
                        --eventItemsFired;
                    }
                    return eventItemsFired > 0;
                }
                eventObject.setAllEventItemsFired();
                return true;
            }
        }
        return false;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        if (eventObject instanceof PostAddEventObject) {
            PostAddEventObject postAddEventObject = (PostAddEventObject)eventObject;
            return this.getPrimaryTokenValues(postAddEventObject.getSdcid(), eventObject, inputTokens);
        }
        return null;
    }
}

