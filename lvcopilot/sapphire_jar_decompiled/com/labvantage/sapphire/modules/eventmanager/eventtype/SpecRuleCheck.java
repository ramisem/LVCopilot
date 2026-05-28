/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostDataReleaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SpecRuleCheck
extends BaseSDCEventType {
    private static int SPECCHECKSOURCE_INDEX = 1;
    private static String SPECCHECKSOURCE_ID = "specchecksource";
    private static String SPECCHECKSOURCE_DISPLAY = "Check Source";
    private static int SPECCHECKSOURCEOPTIONS_INDEX = 2;
    private static String SPECCHECKSOURCEOPTIONS_ID = "specchecksourceoptions";
    private static String SPECCHECKSOURCEOPTIONS_DISPLAY = "Source Options";
    private static String SPECCHECKSOURCEOPTIONS_ANYRELEASED = "Y";
    private static String SPECCHECKSOURCEOPTIONS_MANDATORYRELEASED = "M";
    private static String SPECCHECKSOURCEOPTIONS_ALLRELEASED = "A";
    private static int SPECCHECKCONDITION_INDEX = 3;
    private static String SPECCHECKCONDITION_ID = "speccheckcondition";
    private static String SPECCHECKCONDITION_DISPLAY = "Spec Condition";
    private static int SPECCHECKTYPE_INDEX = 4;
    private static String SPECCHECKTYPE_ID = "specchecktype";
    private static String SPECCHECKTYPE_DISPLAY = "Spec Type";
    private static String SPECCHECKTYPE_ANYSPEC = "A";
    private static String SPECCHECKTYPE_PRIMARYSPEC = "P";

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
        return new String[]{"primary", "sdispec"};
    }

    @Override
    public boolean supportsSdcid(String sdcid) {
        return true;
    }

    @Override
    public boolean isSetupConditionsCascading() {
        return true;
    }

    @Override
    public Condition getSetupCondition(int index, DataSet setupConditionValues) {
        if (index == SPECCHECKSOURCE_INDEX) {
            PropertyList editorProps = new PropertyList();
            editorProps.setProperty("mode", "dropdownlist");
            editorProps.setProperty("displayvalue", "PostDataRelease=Data Release");
            return new Condition(new ConditionItem(SPECCHECKSOURCE_ID, SPECCHECKSOURCE_DISPLAY, "equals", editorProps.toXMLString()));
        }
        if (index == SPECCHECKSOURCEOPTIONS_INDEX) {
            PropertyList editorProps = new PropertyList();
            editorProps.setProperty("mode", "dropdownlist");
            editorProps.setProperty("displayvalue", SPECCHECKSOURCEOPTIONS_ANYRELEASED + "=Any DataItem Released;" + SPECCHECKSOURCEOPTIONS_MANDATORYRELEASED + "=All Mandatory DataItems Released;" + SPECCHECKSOURCEOPTIONS_ALLRELEASED + "=All DataItems Released");
            return new Condition(new ConditionItem(SPECCHECKSOURCEOPTIONS_ID, SPECCHECKSOURCEOPTIONS_DISPLAY, "equals", editorProps.toXMLString()));
        }
        if (index == SPECCHECKCONDITION_INDEX) {
            PropertyList editorProps = new PropertyList();
            editorProps = new PropertyList();
            editorProps.setProperty("mode", "dropdownlist");
            editorProps.setProperty("reftypeid", "Spec Condition");
            return new Condition(new ConditionItem(SPECCHECKCONDITION_ID, SPECCHECKCONDITION_DISPLAY, "equals", editorProps.toXMLString()));
        }
        if (index == SPECCHECKTYPE_INDEX) {
            PropertyList editorProps = new PropertyList();
            editorProps = new PropertyList();
            editorProps.setProperty("mode", "dropdownlist");
            editorProps.setProperty("displayvalue", SPECCHECKTYPE_ANYSPEC + "=Any");
            return new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", editorProps.toXMLString()));
        }
        return null;
    }

    @Override
    public Condition[] getSetupConditions(DataSet eventTypeSetupConditions) {
        Condition[] conditions = null;
        if (eventTypeSetupConditions != null && eventTypeSetupConditions.size() >= 1 && eventTypeSetupConditions.getValue(0, "conditionitem").equals("sdcid") && eventTypeSetupConditions.getValue(0, "operator1").equals("equals") && eventTypeSetupConditions.getValue(0, "value1").length() > 0) {
            if (eventTypeSetupConditions.size() >= 2 && eventTypeSetupConditions.getValue(1, "conditionitem").equals(SPECCHECKSOURCE_ID) && eventTypeSetupConditions.getValue(1, "operator1").equals("equals") && eventTypeSetupConditions.getValue(1, "value1").length() > 0) {
                if (eventTypeSetupConditions.size() >= 3 && eventTypeSetupConditions.getValue(2, "conditionitem").equals(SPECCHECKSOURCEOPTIONS_ID) && eventTypeSetupConditions.getValue(2, "operator1").equals("equals") && eventTypeSetupConditions.getValue(2, "value1").length() > 0) {
                    if (eventTypeSetupConditions.size() >= 4 && eventTypeSetupConditions.getValue(3, "conditionitem").equals(SPECCHECKCONDITION_ID) && eventTypeSetupConditions.getValue(3, "operator1").equals("equals") && eventTypeSetupConditions.getValue(3, "value1").length() > 0) {
                        if (eventTypeSetupConditions.size() >= 5 && eventTypeSetupConditions.getValue(4, "conditionitem").equals(SPECCHECKTYPE_ID) && eventTypeSetupConditions.getValue(4, "operator1").equals("equals") && eventTypeSetupConditions.getValue(4, "value1").length() > 0) {
                            return null;
                        }
                        conditions = new Condition[]{new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", true))};
                        return conditions;
                    }
                    conditions = new Condition[]{new Condition(new ConditionItem(SPECCHECKCONDITION_ID, SPECCHECKCONDITION_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", true))};
                    return conditions;
                }
                conditions = new Condition[]{new Condition(new ConditionItem(SPECCHECKSOURCEOPTIONS_ID, SPECCHECKSOURCEOPTIONS_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKCONDITION_ID, SPECCHECKCONDITION_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", true))};
                return conditions;
            }
            conditions = new Condition[]{new Condition(new ConditionItem(SPECCHECKSOURCE_ID, SPECCHECKSOURCE_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKSOURCEOPTIONS_ID, SPECCHECKSOURCEOPTIONS_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKCONDITION_ID, SPECCHECKCONDITION_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", true))};
            return conditions;
        }
        conditions = new Condition[]{new Condition(this.getSDCConditionItem()), new Condition(new ConditionItem(SPECCHECKSOURCE_ID, SPECCHECKSOURCE_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKSOURCEOPTIONS_ID, SPECCHECKSOURCEOPTIONS_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKCONDITION_ID, SPECCHECKCONDITION_DISPLAY, "equals", true)), new Condition(new ConditionItem(SPECCHECKTYPE_ID, SPECCHECKTYPE_DISPLAY, "equals", true))};
        return conditions;
    }

    @Override
    public Condition getFilterConditionTemplate(DataSet setupConditionValues) {
        if (setupConditionValues != null && setupConditionValues.size() > 0) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            ArrayList<ConditionItem> conditionItems = this.getSDCColumnConditionItems(sdcid);
            conditionItems.addAll(this.getSDCColumnConditionItems("SDISpec"));
            return new Condition(conditionItems.toArray(new ConditionItem[conditionItems.size()]));
        }
        return null;
    }

    @Override
    public boolean hasEventFired(BaseEventObject eventObject, DataSet eventplanconditions) {
        PostDataReleaseEventObject dataReleaseEventObject;
        if (eventObject instanceof PostDataReleaseEventObject && (dataReleaseEventObject = (PostDataReleaseEventObject)eventObject).isRelease() && dataReleaseEventObject.getProperties().getProperty("dataentryautorelease", "N").equals("N")) {
            DataSet primary = dataReleaseEventObject.getDataSet("primary");
            DataSet sdispec = dataReleaseEventObject.getDataSet("sdispec");
            if (sdispec != null && eventObject.hasEventItems() && eventplanconditions.size() >= 5) {
                String sourceoption;
                String sdcid = dataReleaseEventObject.getSdcid();
                String primaryTableid = this.sdcProps.getProperty("tableid");
                if (eventplanconditions.getValue(0, "value1").equals(sdcid) && eventplanconditions.getValue(SPECCHECKSOURCE_INDEX, "value1").equals(eventObject.getEventid()) && ((sourceoption = eventplanconditions.getValue(SPECCHECKSOURCEOPTIONS_INDEX, "value1", SPECCHECKSOURCEOPTIONS_ALLRELEASED)).equals(SPECCHECKSOURCEOPTIONS_ALLRELEASED) && dataReleaseEventObject.isAllDataItemsReleased() || sourceoption.equals(SPECCHECKSOURCEOPTIONS_MANDATORYRELEASED) && dataReleaseEventObject.isAllMandatoryDataItemsReleased() || sourceoption.equals(SPECCHECKSOURCEOPTIONS_ANYRELEASED) && dataReleaseEventObject.isRelease())) {
                    boolean anySpec = eventplanconditions.getValue(SPECCHECKTYPE_INDEX, "value1", SPECCHECKTYPE_ANYSPEC).equals(SPECCHECKTYPE_ANYSPEC);
                    if (!anySpec) {
                        // empty if block
                    }
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("condition", eventplanconditions.getValue(SPECCHECKCONDITION_INDEX, "value1"));
                    if (sdispec.findRow(valueMap) > -1) {
                        if (eventObject instanceof PostDataReleaseEventObject) {
                            eventObject.clearEventItems();
                            ((BaseSDCEventObject)eventObject).addEventItemsFromPrimary(eventObject.getOrigEventItems());
                        }
                        DataSet matchingSDISpec = sdispec.getFilteredDataSet(valueMap);
                        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
                        int eventItemsFired = eventItems.size();
                        for (int i = 0; i < eventItems.size(); ++i) {
                            boolean isTemplate;
                            SDIEventItem eventItem = (SDIEventItem)eventItems.get(i);
                            eventItem.setHasFired(true);
                            HashMap<String, String> findMap = new HashMap<String, String>();
                            findMap.put(this.sdcProps.getProperty("keycolid1"), eventItem.getKeyid1());
                            if (eventItem.getKeyCols() >= 2) {
                                findMap.put(this.sdcProps.getProperty("keycolid2"), eventItem.getKeyid2());
                            }
                            if (eventItem.getKeyCols() >= 3) {
                                findMap.put(this.sdcProps.getProperty("keycolid3"), eventItem.getKeyid3());
                            }
                            int primaryRow = eventItem.getPrimaryIndex() >= 0 ? eventItem.getPrimaryIndex() : (primary != null ? primary.findRow(findMap) : -1);
                            eventItem.setPrimaryIndex(primaryRow);
                            boolean bl = isTemplate = primaryRow >= 0 && primary.getValue(primaryRow, "templateflag", "N").equals("Y");
                            if (!eventItem.hasEventPlan(eventObject.getCurrentEventPlan()) || isTemplate) continue;
                            for (int j = 3; eventItem.hasFired() && j < eventplanconditions.size(); ++j) {
                                if (j == SPECCHECKCONDITION_INDEX) {
                                    valueMap.put("keyid1", eventItem.getKeyid1());
                                    valueMap.put("keyid2", eventItem.getKeyid2());
                                    valueMap.put("keyid3", eventItem.getKeyid3());
                                    if (matchingSDISpec.findRow(valueMap) != -1) continue;
                                    eventItem.setHasFired(false);
                                    --eventItemsFired;
                                    continue;
                                }
                                if (j == SPECCHECKTYPE_INDEX) continue;
                                String conditionItem = eventplanconditions.getValue(j, "conditionitem");
                                if (sdispec != null && conditionItem.startsWith("sdispec_") && !this.conditionMatch(sdispec, eventItem.getDataIndex(), "sdispec", eventplanconditions, j)) {
                                    eventItem.setHasFired(false);
                                    --eventItemsFired;
                                    continue;
                                }
                                if (primary == null || !conditionItem.startsWith(primaryTableid + "_") || eventItem.getPrimaryIndex() < 0 || this.conditionMatch(primary, eventItem.getPrimaryIndex(), primaryTableid, eventplanconditions, j)) continue;
                                eventItem.setHasFired(false);
                                --eventItemsFired;
                            }
                        }
                        return eventItemsFired > 0;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public HashMap getProcessingInputs(BaseEventObject eventObject, String[] inputTokens) {
        if (eventObject instanceof BaseSDCEventObject) {
            BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
            HashMap tokenValues = this.getPrimaryTokenValues(sdcEventObject.getSdcid(), eventObject, inputTokens);
            tokenValues.putAll(this.getTableTokenValues(eventObject, "sdispec", "sdispec", inputTokens));
            return tokenValues;
        }
        return null;
    }
}

