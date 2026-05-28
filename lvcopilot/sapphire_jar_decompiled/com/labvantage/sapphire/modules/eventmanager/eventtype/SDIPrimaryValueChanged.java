/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.util.regex.RegexUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.SDIData;

public class SDIPrimaryValueChanged
extends BaseSDCEventType {
    private SDIData sdiData;

    @Override
    public Class[] getEventObjectImplementations() {
        return new Class[]{PostAddEventObject.class, PostEditEventObject.class};
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
        return this.getCascadingColumnValueSetupCondition(index, setupConditionValues);
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        return this.getSDCColumnValueSetupConditions(eventTypeSetupConditions);
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        if (setupConditionValues != null && setupConditionValues.size() > 0) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            if (sdcid.startsWith("[variables.")) {
                return new Condition(new ConditionItem("", "S", ""));
            }
            ArrayList<ConditionItem> conditionItems = this.getSDCColumnConditionItems(sdcid);
            conditionItems.add(this.getEventSourceConditionItem(new String[]{"PostAdd", "PostEdit"}));
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
        if (eventObject instanceof BaseSDCEventObject && (primary = ((BaseSDCEventObject)eventObject).getDataSet("primary")) != null && eventObject.hasEventItems() && eventplanconditions.size() >= 3) {
            String sdcid = ((BaseSDCEventObject)eventObject).getSdcid();
            String tableid = this.sdcProps.getProperty("tableid");
            if (eventplanconditions.getValue(0, "value1").equals(sdcid)) {
                String columnid = eventplanconditions.getValue(1, "value1");
                if (columnid.startsWith(tableid + "_")) {
                    columnid = columnid.substring((tableid + "_").length());
                }
                if (primary.isValidColumn(columnid)) {
                    ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
                    int eventItemsFired = eventItems.size();
                    for (int i = 0; i < eventItems.size(); ++i) {
                        SDIEventItem sdiEventItem = (SDIEventItem)eventItems.get(i);
                        sdiEventItem.setHasFired(true);
                        if (sdiEventItem.hasEventPlan(eventObject.getCurrentEventPlan())) {
                            SDIData beforeSDIData = eventObject instanceof PostEditEventObject ? ((PostEditEventObject)eventObject).getBeforeSDIData() : null;
                            for (int j = 2; sdiEventItem.hasFired() && j < eventplanconditions.size(); ++j) {
                                if (j == 2) {
                                    String value1 = eventplanconditions.getValue(j, "value1");
                                    String value2 = eventplanconditions.getValue(j, "value2");
                                    if (eventObject instanceof PostAddEventObject) {
                                        if ((value1.equalsIgnoreCase("(any)") || RegexUtil.wildcardMatch(primary.getValue(sdiEventItem.getDataIndex(), columnid), value1) || primary.getValue(sdiEventItem.getDataIndex(), columnid).equals(value1)) && value2.length() <= 0) continue;
                                        sdiEventItem.setHasFired(false);
                                        --eventItemsFired;
                                        continue;
                                    }
                                    if (!(eventObject instanceof PostEditEventObject)) continue;
                                    String oldValue = this.getOldValue(primary, sdiEventItem.getDataIndex(), columnid, beforeSDIData != null ? beforeSDIData.getDataset("primary") : null);
                                    if (this.hasEventItemChangeFired(primary, sdiEventItem.getDataIndex(), columnid, oldValue, value1, value2)) continue;
                                    sdiEventItem.setHasFired(false);
                                    --eventItemsFired;
                                    continue;
                                }
                                if (this.conditionMatch(primary, sdiEventItem.getDataIndex(), tableid, eventplanconditions, j, beforeSDIData != null ? beforeSDIData.getDataset("primary") : null)) continue;
                                sdiEventItem.setHasFired(false);
                                --eventItemsFired;
                            }
                            continue;
                        }
                        --eventItemsFired;
                    }
                    return eventItemsFired > 0;
                }
            }
        }
        return false;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        if (eventObject instanceof BaseSDCEventObject) {
            return this.getPrimaryTokenValues(((BaseSDCEventObject)eventObject).getSdcid(), eventObject, inputTokens);
        }
        return null;
    }
}

