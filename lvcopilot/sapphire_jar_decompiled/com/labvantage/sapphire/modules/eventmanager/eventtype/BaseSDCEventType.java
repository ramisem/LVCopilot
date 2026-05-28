/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.DataItemEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.DataSetEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.NoteEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditWorkItemEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.WorkItemEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.util.regex.RegexUtil;
import com.labvantage.sapphire.xml.Column;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseSDCEventType
extends BaseEventType {
    protected PropertyList sdcProps;

    public String[] getSupplementalRequestItems(DataSet eventPlanConditions) {
        return null;
    }

    public void setSdcProps(PropertyList sdcProps) {
        this.sdcProps = sdcProps;
    }

    protected Condition getCascadingColumnValueSetupCondition(int index, DataSet setupConditionValues) {
        return this.getCascadingColumnValueSetupCondition(index, setupConditionValues, "");
    }

    protected Condition getCascadingColumnValueSetupCondition(int index, DataSet setupConditionValues, String tablename) {
        if (index == 1 && setupConditionValues.size() >= 1) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            PropertyList columnProps = new PropertyList();
            if (sdcid.startsWith("[variables.")) {
                return new Condition(new ConditionItem("columnid", "Column", "equals", columnProps.toXMLString()));
            }
            if (sdcid.length() > 0 && !sdcid.equals("(none)")) {
                columnProps.setProperty("mode", "dropdownlist");
                if (tablename.length() > 0) {
                    columnProps.setProperty("sqlcode", "20016");
                    columnProps.setProperty("sqlcode_tablename", tablename);
                } else {
                    columnProps.setProperty("sqlcode", "20017");
                    columnProps.setProperty("sqlcode_sdcid", sdcid);
                }
                return new Condition(new ConditionItem("columnid", "Column", "equals", columnProps.toXMLString()));
            }
            return null;
        }
        if (index == 2 && setupConditionValues.size() >= 2) {
            String sdcid = setupConditionValues.getValue(0, "value1");
            String columnid = setupConditionValues.getValue(1, "value1");
            if (sdcid.startsWith("[variables.") || columnid.startsWith("[variables.")) {
                return new Condition(new ConditionItem(columnid, "S", ""));
            }
            if (sdcid.length() > 0 && !sdcid.equals("(none)") && columnid.length() > 0 && !columnid.equals("(none)")) {
                if (tablename.length() > 0) {
                    return new Condition(this.getTableColumnConditionItem(sdcid, columnid, "goes to"));
                }
                return new Condition(this.getPrimaryColumnConditionItem(sdcid, columnid, "goes to"));
            }
            return null;
        }
        return null;
    }

    protected Condition[] getSDCColumnValueSetupConditions(DataSet eventTypeSetupConditions) {
        Condition[] conditions = null;
        if (eventTypeSetupConditions != null && eventTypeSetupConditions.size() >= 1 && eventTypeSetupConditions.getValue(0, "conditionitem").equals("sdcid") && eventTypeSetupConditions.getValue(0, "operator1").equals("equals") && eventTypeSetupConditions.getValue(0, "value1").length() > 0) {
            if (eventTypeSetupConditions.size() >= 2 && eventTypeSetupConditions.getValue(1, "conditionitem").equals("columnid") && eventTypeSetupConditions.getValue(1, "operator1").equals("equals") && eventTypeSetupConditions.getValue(1, "value1").length() > 0) {
                if (eventTypeSetupConditions.size() >= 3 && eventTypeSetupConditions.getValue(2, "conditionitem").equals("value") && eventTypeSetupConditions.getValue(2, "operator1").equals("goes to") && (eventTypeSetupConditions.getValue(2, "value1").length() > 0 || eventTypeSetupConditions.getValue(2, "value2").length() > 0)) {
                    return null;
                }
                conditions = new Condition[]{this.getSetupCondition(2, eventTypeSetupConditions)};
                return conditions;
            }
            conditions = new Condition[]{this.getSetupCondition(1, eventTypeSetupConditions), new Condition(new ConditionItem("value", "Value", "goes to", true))};
            return conditions;
        }
        conditions = new Condition[]{new Condition(this.getSDCConditionItem()), new Condition(new ConditionItem("columnid", "Column", "equals", true)), new Condition(new ConditionItem("value", "Value", "goes to", true))};
        return conditions;
    }

    protected ArrayList<ConditionItem> getSDCColumnConditionItems(String sdcid) {
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
        if (sdcProps != null) {
            String tableid = sdcProps.getProperty("tableid");
            ArrayList<ConditionItem> conditionColumns = new ArrayList<ConditionItem>();
            DataSet columnData = this.getSDCProcessor().getTableColumnData(tableid);
            columnData.sort("columnid");
            for (int i = 0; i < columnData.size(); ++i) {
                String columnid = columnData.getValue(i, "columnid");
                String datatype = columnData.getValue(i, "datatype", "C");
                if (Column.isAuditColumn(columnid) || datatype.equals("T") || columnid.equalsIgnoreCase("usersequence")) continue;
                conditionColumns.add(new ConditionItem(tableid + "_" + columnid, columnData.getValue(i, "columnlabel", tableid + "_" + columnid), datatype.equals("C") ? "S" : (datatype.equals("D") ? "D" : "N"), columnData.getValue(i, this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "editorstyleprops"))));
            }
            return conditionColumns;
        }
        return null;
    }

    protected ArrayList<ConditionItem> getTableColumnConditionItems(String tableid) {
        DataSet columnData = this.getSDCProcessor().getTableColumnData(tableid);
        columnData.sort("columnid");
        if (columnData != null) {
            ArrayList<ConditionItem> conditionColumns = new ArrayList<ConditionItem>();
            for (int i = 0; i < columnData.size(); ++i) {
                String columnid = columnData.getValue(i, "columnid");
                String datatype = columnData.getValue(i, "datatype", "C");
                if (Column.isAuditColumn(columnid) || datatype.equals("T") || columnid.equalsIgnoreCase("usersequence")) continue;
                conditionColumns.add(new ConditionItem(tableid + "_" + columnid, columnData.getValue(i, "columnlabel", tableid + "_" + columnid), datatype.equals("C") ? "S" : (datatype.equals("D") ? "D" : "N"), ""));
            }
            return conditionColumns;
        }
        return null;
    }

    protected HashMap getPrimaryTokenValues(String sdcid, BaseEventObject eventObject, String[] inputTokens) {
        HashMap<String, String> tokenValues = new HashMap<String, String>();
        BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
        DataSet primary = sdcEventObject.getDataSet("primary", true, this.sapphireConnection);
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
        if (sdcProps != null) {
            String tableid = sdcProps.getProperty("tableid");
            if (primary != null && inputTokens.length > 0) {
                HashSet conditionItems;
                ArrayList<BaseEventItem> eventItems = sdcEventObject.getProcessingEventItems();
                ArrayList<SDIEventItem> convertedEventItems = null;
                if (eventObject.getEventItemType() != 1) {
                    convertedEventItems = new ArrayList<SDIEventItem>();
                    HashSet<Integer> resolvedIndexes = new HashSet<Integer>();
                    for (int i = 0; i < eventItems.size(); ++i) {
                        BaseEventItem eventItem = eventItems.get(i);
                        if (eventItem instanceof NoteEventItem || eventItem instanceof DataSetEventItem || eventItem instanceof DataItemEventItem || eventItem instanceof WorkItemEventItem) {
                            int primaryIndex;
                            SDIEventItem sdiEventItem = (SDIEventItem)eventItem;
                            HashMap<String, String> findMap = new HashMap<String, String>();
                            findMap.put(sdcProps.getProperty("keycolid1"), sdiEventItem.getKeyid1());
                            if (sdiEventItem.getKeyCols() >= 2) {
                                findMap.put(sdcProps.getProperty("keycolid2"), sdiEventItem.getKeyid2());
                            }
                            if (sdiEventItem.getKeyCols() >= 3) {
                                findMap.put(sdcProps.getProperty("keycolid3"), sdiEventItem.getKeyid3());
                            }
                            int n = primaryIndex = sdiEventItem.getPrimaryIndex() >= 0 ? sdiEventItem.getPrimaryIndex() : primary.findRow(findMap);
                            if (resolvedIndexes.contains(primaryIndex)) continue;
                            resolvedIndexes.add(primaryIndex);
                            SDIEventItem convertedEventItem = new SDIEventItem(sdiEventItem.getSdcid(), sdiEventItem.getKeyid1(), sdiEventItem.getKeyid2(), sdiEventItem.getKeyid3());
                            convertedEventItem.setDataIndex(primaryIndex);
                            convertedEventItems.add(convertedEventItem);
                            continue;
                        }
                        this.logger.error("Unknown event item foundin getPrimaryTokenValues!");
                        return tokenValues;
                    }
                }
                if ((conditionItems = this.getTableColumnSet(tableid)) != null && conditionItems.size() > 0) {
                    SDIData beforeSDIData = eventObject instanceof PostEditEventObject ? ((PostEditEventObject)eventObject).getBeforeSDIData() : null;
                    for (String token : inputTokens) {
                        if (!conditionItems.contains(token)) continue;
                        tokenValues.put(token, this.getEventItemColumnValues(primary, beforeSDIData != null ? beforeSDIData.getDataset("primary") : null, tableid, token, convertedEventItems != null ? convertedEventItems : eventItems));
                    }
                }
            }
        }
        return tokenValues;
    }

    protected HashMap getTableTokenValues(BaseEventObject eventObject, String datasetname, String tablename, String[] inputTokens) {
        HashMap<String, String> tokenValues = new HashMap<String, String>();
        BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
        DataSet tableData = sdcEventObject.getDataSet(datasetname);
        if (tableData != null && inputTokens.length > 0) {
            ArrayList<BaseEventItem> eventItems = sdcEventObject.getProcessingEventItems();
            HashSet conditionItems = this.getTableColumnSet(tablename);
            if (conditionItems != null && conditionItems.size() > 0) {
                SDIData beforeSDIData = null;
                if ("sdiworkitem".equalsIgnoreCase(tablename)) {
                    beforeSDIData = eventObject instanceof PostEditWorkItemEventObject ? ((PostEditWorkItemEventObject)eventObject).getBeforeSDIData() : null;
                }
                for (String token : inputTokens) {
                    if (!conditionItems.contains(token)) continue;
                    if (beforeSDIData != null) {
                        tokenValues.put(token, this.getEventItemTableColumnValues(tableData, beforeSDIData.getDataset(datasetname), tablename, token, eventItems, beforeSDIData.getDataSetKeys(datasetname)));
                        continue;
                    }
                    tokenValues.put(token, this.getEventItemTableColumnValues(tableData, null, tablename, token, eventItems, null));
                }
            }
        }
        return tokenValues;
    }

    protected HashSet getTableColumnSet(String tableid) {
        DataSet columnData = this.getSDCProcessor().getTableColumnData(tableid);
        if (columnData != null) {
            HashSet<String> columnSet = new HashSet<String>();
            for (int i = 0; i < columnData.size(); ++i) {
                columnSet.add(tableid + "_" + columnData.getValue(i, "columnid"));
            }
            return columnSet;
        }
        return null;
    }

    protected ConditionItem getPrimaryColumnConditionItem(String sdcid, String columnid, String operator) {
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
        if (sdcProps != null) {
            String tableid = sdcProps.getProperty("tableid");
            PropertyListCollection columns = sdcProps.getCollection("columns");
            PropertyList column = columns.getPropertyList(columnid.startsWith(tableid + "_") ? columnid.substring((tableid + "_").length()) : columnid);
            String datatype = column.getProperty("datatype", "C");
            return new ConditionItem(tableid + "_" + columnid, column.getProperty("columnlabel", tableid + "_" + columnid), operator != null && operator.length() > 0 ? operator : (datatype.equals("C") ? "S" : (datatype.equals("D") ? "D" : "N")), column.getProperty("editorstyleprops"));
        }
        return null;
    }

    protected ConditionItem getTableColumnConditionItem(String tableid, String columnid, String operator) {
        DataSet tableColumns = this.getSDCProcessor().getTableColumnData(tableid);
        int row = tableColumns.findRow("columnid", columnid.startsWith(tableid + "_") ? columnid.substring((tableid + "_").length()) : columnid);
        String datatype = tableColumns.getValue(row, "datatype", "C");
        return new ConditionItem(tableid + "_" + columnid, tableColumns.getValue(row, "columnlabel", tableid + "_" + columnid), operator != null && operator.length() > 0 ? operator : (datatype.equals("C") ? "S" : (datatype.equals("D") ? "D" : "N")), tableColumns.getValue(row, "editorstyleprops"));
    }

    protected boolean hasSDIXXXCreatedEventFired(BaseEventObject eventObject, String datasetname, String tablename, DataSet eventplanconditions) {
        if (eventObject instanceof BaseSDCEventObject) {
            if (eventObject instanceof PostAddEventObject) {
                eventObject.clearEventItems();
                if (tablename.equals("sdidata")) {
                    ((BaseSDCEventObject)eventObject).addEventItemsFromDatasets();
                } else if (tablename.equals("sdiworkitem")) {
                    ((BaseSDCEventObject)eventObject).addEventItemsFromWorkitems();
                }
            }
            DataSet primary = ((BaseSDCEventObject)eventObject).getDataSet("primary");
            DataSet sdiXXX = ((BaseSDCEventObject)eventObject).getDataSet(datasetname);
            if (eventObject.hasEventItems() && eventplanconditions.size() >= 1) {
                String sdcid = ((BaseSDCEventObject)eventObject).getSdcid();
                String primaryTableid = this.sdcProps.getProperty("tableid");
                if (eventplanconditions.getValue(0, "value1").equals(sdcid)) {
                    if (eventplanconditions.size() > 1) {
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
                            if (eventItem.hasEventPlan(eventObject.getCurrentEventPlan()) && !isTemplate) {
                                for (int j = 1; eventItem.hasFired() && j < eventplanconditions.size(); ++j) {
                                    String conditionItem = eventplanconditions.getValue(j, "conditionitem");
                                    if (sdiXXX != null && conditionItem.startsWith(tablename + "_") && !this.conditionMatch(sdiXXX, eventItem.getDataIndex(), tablename, eventplanconditions, j)) {
                                        eventItem.setHasFired(false);
                                        --eventItemsFired;
                                        continue;
                                    }
                                    if (primary == null || !conditionItem.startsWith(primaryTableid + "_") || eventItem.getPrimaryIndex() < 0 || this.conditionMatch(primary, eventItem.getPrimaryIndex(), primaryTableid, eventplanconditions, j)) continue;
                                    eventItem.setHasFired(false);
                                    --eventItemsFired;
                                }
                                continue;
                            }
                            eventItem.setHasFired(false);
                            --eventItemsFired;
                        }
                        return eventItemsFired > 0;
                    }
                    eventObject.setAllEventItemsFired();
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean hasSDIXXXEditedEventFired(BaseEventObject eventObject, String datasetname, String tablename, DataSet eventplanconditions, boolean edit, SDIData beforeSDIData) {
        if (eventObject instanceof BaseSDCEventObject) {
            DataSet primary = ((BaseSDCEventObject)eventObject).getDataSet("primary");
            DataSet sdiXXX = ((BaseSDCEventObject)eventObject).getDataSet(datasetname);
            if (eventObject.hasEventItems() && eventplanconditions.size() >= 3) {
                String sdcid = ((BaseSDCEventObject)eventObject).getSdcid();
                String primaryTableid = this.sdcProps.getProperty("tableid");
                if (eventplanconditions.getValue(0, "value1").equals(sdcid)) {
                    String columnid = eventplanconditions.getValue(1, "value1");
                    if (columnid.startsWith(tablename + "_")) {
                        columnid = columnid.substring((tablename + "_").length());
                    }
                    if (sdiXXX.isValidColumn(columnid)) {
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
                            if (eventItem.hasEventPlan(eventObject.getCurrentEventPlan()) && !isTemplate) {
                                for (int j = 2; eventItem.hasFired() && j < eventplanconditions.size(); ++j) {
                                    if (j == 2) {
                                        String value1 = eventplanconditions.getValue(j, "value1");
                                        String value2 = eventplanconditions.getValue(j, "value2");
                                        if (!edit) {
                                            if (sdiXXX.getValue(eventItem.getDataIndex(), columnid).equals(value1) && value2.length() <= 0) continue;
                                            eventItem.setHasFired(false);
                                            --eventItemsFired;
                                            continue;
                                        }
                                        if (!edit) continue;
                                        String[] keyCols = beforeSDIData != null ? beforeSDIData.getDataSetKeys(datasetname) : null;
                                        String oldValue = this.getOldSDIXXXValue(sdiXXX, eventItem.getDataIndex(), columnid, beforeSDIData != null ? beforeSDIData.getDataset(datasetname) : null, keyCols);
                                        if (this.hasEventItemChangeFired(sdiXXX, eventItem.getDataIndex(), columnid, oldValue, value1, value2)) continue;
                                        eventItem.setHasFired(false);
                                        --eventItemsFired;
                                        continue;
                                    }
                                    String conditionItem = eventplanconditions.getValue(j, "conditionitem");
                                    if (sdiXXX != null && conditionItem.startsWith(tablename + "_") && !this.conditionMatchSDIXXX(sdiXXX, eventItem.getDataIndex(), tablename, eventplanconditions, j, beforeSDIData != null ? beforeSDIData.getDataset(datasetname) : null, beforeSDIData != null ? beforeSDIData.getDataSetKeys(datasetname) : null)) {
                                        eventItem.setHasFired(false);
                                        --eventItemsFired;
                                        continue;
                                    }
                                    if (primary == null || !conditionItem.startsWith(primaryTableid + "_") || eventItem.getPrimaryIndex() < 0 || this.conditionMatch(primary, eventItem.getPrimaryIndex(), primaryTableid, eventplanconditions, j)) continue;
                                    eventItem.setHasFired(false);
                                    --eventItemsFired;
                                }
                                continue;
                            }
                            eventItem.setHasFired(false);
                            --eventItemsFired;
                        }
                        return eventItemsFired > 0;
                    }
                }
            }
        }
        return false;
    }

    protected boolean hasEventItemChangeFired(DataSet dataset, int row, String columnid, String oldValue, String value1, String value2) {
        if (this.hasValueChanged(dataset, row, columnid, oldValue)) {
            return !(value1.length() > 0 && value2.length() > 0 ? (!value1.equalsIgnoreCase("(blank)") || dataset.getValue(row, columnid).length() != 0) && !value1.equalsIgnoreCase("(any)") && !RegexUtil.wildcardMatch(dataset.getValue(row, columnid), value1) && !dataset.getValue(row, columnid).equals(value1) || (!value2.equalsIgnoreCase("(blank)") || oldValue.length() != 0) && !value2.equalsIgnoreCase("(any)") && !RegexUtil.wildcardMatch(oldValue, value2) && !oldValue.equals(value2) : (value1.length() > 0 && value2.length() == 0 ? (!value1.equalsIgnoreCase("(blank)") || dataset.getValue(row, columnid).length() != 0) && !value1.equalsIgnoreCase("(any)") && !RegexUtil.wildcardMatch(dataset.getValue(row, columnid), value1) && !dataset.getValue(row, columnid).equals(value1) : value1.length() == 0 && value2.length() > 0 && (!value2.equalsIgnoreCase("(blank)") || oldValue.length() != 0) && !value2.equalsIgnoreCase("(any)") && !RegexUtil.wildcardMatch(oldValue, value2) && !oldValue.equals(value2)));
        }
        return false;
    }

    protected boolean conditionMatch(DataSet data, int dataRow, String tablename, DataSet conditions, int conditionRow) {
        return this.conditionMatch(data, dataRow, tablename, conditions, conditionRow, null);
    }

    protected boolean conditionMatch(DataSet data, int dataRow, String tablename, DataSet conditions, int conditionRow, DataSet beforeData) {
        String value1 = conditions.getValue(conditionRow, "value1");
        String value2 = conditions.getValue(conditionRow, "value2");
        String dataColumn = conditions.getValue(conditionRow, "conditionitem");
        if (dataColumn.startsWith(tablename + "_")) {
            dataColumn = dataColumn.substring((tablename + "_").length());
        }
        int columnType = -1;
        Object matchValue = null;
        if (data.isValidColumn(dataColumn)) {
            columnType = data.getColumnType(dataColumn);
            matchValue = data.getObject(dataRow, dataColumn);
        } else if (beforeData != null && beforeData.isValidColumn(dataColumn)) {
            columnType = beforeData.getColumnType(dataColumn);
            matchValue = this.getOldValue(data, dataRow, dataColumn, beforeData);
        }
        String compareValue1 = null;
        String compareValue2 = null;
        if (columnType == 0 || matchValue instanceof String) {
            compareValue1 = value1.length() > 0 ? value1 : null;
            compareValue2 = value2.length() > 0 ? value2 : null;
        } else if (columnType == 1 || matchValue instanceof BigDecimal) {
            compareValue1 = value1.length() > 0 ? this.formatUtil.parseBigDecimal(value1) : null;
            compareValue2 = value2.length() > 0 ? this.formatUtil.parseBigDecimal(value2) : null;
        } else if (columnType == 2 || matchValue instanceof Calendar) {
            compareValue1 = value1.length() > 0 ? this.m18n.parseCalendar(value1) : null;
            compareValue2 = value2.length() > 0 ? this.m18n.parseCalendar(value2) : null;
        } else {
            compareValue1 = value1.length() > 0 ? value1 : null;
            compareValue2 = value2.length() > 0 ? value2 : null;
        }
        return ConditionItem.conditionMatch(matchValue, compareValue1, compareValue2, conditions.getValue(conditionRow, "operator1"));
    }

    protected boolean conditionMatchSDIXXX(DataSet data, int dataRow, String tablename, DataSet conditions, int conditionRow, DataSet beforeData, String[] keyCols) {
        String value1 = conditions.getValue(conditionRow, "value1");
        String value2 = conditions.getValue(conditionRow, "value2");
        String dataColumn = conditions.getValue(conditionRow, "conditionitem");
        if (dataColumn.startsWith(tablename + "_")) {
            dataColumn = dataColumn.substring((tablename + "_").length());
        }
        int columnType = -1;
        Object matchValue = null;
        if (data.isValidColumn(dataColumn)) {
            columnType = data.getColumnType(dataColumn);
            matchValue = data.getObject(dataRow, dataColumn);
        } else if (beforeData != null && beforeData.isValidColumn(dataColumn) && keyCols != null) {
            columnType = beforeData.getColumnType(dataColumn);
            matchValue = this.getOldSDIXXXValue(data, dataRow, dataColumn, beforeData, keyCols);
        }
        String compareValue1 = null;
        String compareValue2 = null;
        if (columnType == 0 || matchValue instanceof String) {
            compareValue1 = value1.length() > 0 ? value1 : null;
            compareValue2 = value2.length() > 0 ? value2 : null;
        } else if (columnType == 1 || matchValue instanceof BigDecimal) {
            compareValue1 = value1.length() > 0 ? this.formatUtil.parseBigDecimal(value1) : null;
            compareValue2 = value2.length() > 0 ? this.formatUtil.parseBigDecimal(value2) : null;
        } else if (columnType == 2 || matchValue instanceof Calendar) {
            compareValue1 = value1.length() > 0 ? this.m18n.parseCalendar(value1) : null;
            compareValue2 = value2.length() > 0 ? this.m18n.parseCalendar(value2) : null;
        } else {
            compareValue1 = value1.length() > 0 ? value1 : null;
            compareValue2 = value2.length() > 0 ? value2 : null;
        }
        return ConditionItem.conditionMatch(matchValue, compareValue1, compareValue2, conditions.getValue(conditionRow, "operator1"));
    }

    protected String getEventItemColumnValues(DataSet data, DataSet beforeData, String tableid, String columnid, ArrayList<SDIEventItem> eventItems) {
        StringBuffer values = new StringBuffer();
        String dataColumn = columnid.startsWith(tableid + "_") ? columnid.substring((tableid + "_").length()) : columnid;
        for (int i = 0; i < eventItems.size(); ++i) {
            String value = null;
            int dataRow = eventItems.get(i).getDataIndex();
            if (data.isValidColumn(dataColumn)) {
                value = data.getValue(dataRow, dataColumn);
            } else if (beforeData != null && beforeData.isValidColumn(dataColumn)) {
                value = this.getOldValue(data, dataRow, dataColumn, beforeData);
            }
            if (value == null) continue;
            values.append(";").append(value);
        }
        return values.length() > 0 ? values.substring(1) : "";
    }

    protected String getEventItemTableColumnValues(DataSet data, DataSet beforeData, String tableid, String columnid, ArrayList<SDIEventItem> eventItems, String[] keyCols) {
        StringBuffer values = new StringBuffer();
        String dataColumn = columnid.startsWith(tableid + "_") ? columnid.substring((tableid + "_").length()) : columnid;
        for (int i = 0; i < eventItems.size(); ++i) {
            String value = null;
            int dataRow = eventItems.get(i).getDataIndex();
            if (data.isValidColumn(dataColumn)) {
                value = data.getValue(dataRow, dataColumn);
            } else if (beforeData != null && beforeData.isValidColumn(dataColumn)) {
                value = this.getOldSDIXXXValue(data, dataRow, dataColumn, beforeData, keyCols);
            }
            if (value == null) continue;
            values.append(";").append(value);
        }
        return values.length() > 0 ? values.substring(1) : "";
    }

    protected boolean hasValueChanged(DataSet newDataSet, int row, String columnId, String oldValue) {
        boolean hasChanged = false;
        if (newDataSet.isValidColumn(columnId)) {
            String newValue = newDataSet.getValue(row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    protected String getOldValue(DataSet newDataSet, int row, String columnId, DataSet oldDataSet) {
        int oldRow;
        String oldValue = "";
        String keyid1 = newDataSet.getValue(row, this.sdcProps.getProperty("keycolid1"));
        String keyid2 = newDataSet.getValue(row, this.sdcProps.getProperty("keycolid2"));
        String keyid3 = newDataSet.getValue(row, this.sdcProps.getProperty("keycolid3"));
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(this.sdcProps.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            findMap.put(this.sdcProps.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            findMap.put(this.sdcProps.getProperty("keycolid3"), keyid3);
        }
        if (oldDataSet != null && (oldRow = oldDataSet.findRow(findMap)) >= 0 && oldRow < oldDataSet.size()) {
            oldValue = oldDataSet.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    protected String getOldSDIXXXValue(DataSet newDataSet, int row, String columnId, DataSet oldDataSet, String[] keyCols) {
        String oldValue = "";
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        if (oldDataSet != null) {
            for (String keyCol : keyCols) {
                if (1 == newDataSet.getColumnType(keyCol)) {
                    findMap.put(keyCol, newDataSet.getBigDecimal(row, keyCol));
                    continue;
                }
                findMap.put(keyCol, newDataSet.getValue(row, keyCol));
            }
            int oldRow = oldDataSet.findRow(findMap);
            if (oldRow >= 0 && oldRow < oldDataSet.size()) {
                oldValue = oldDataSet.getValue(oldRow, columnId);
            }
        }
        return oldValue;
    }

    public abstract boolean supportsSdcid(String var1);

    protected ConditionItem getSDCConditionItem() {
        PropertyList editorProps = new PropertyList();
        editorProps.setProperty("mode", "dropdownlist");
        editorProps.setProperty("sqlcode", "20018");
        return new ConditionItem("sdcid", "SDC", "equals", editorProps.toXMLString());
    }

    protected ConditionItem getEventSourceConditionItem(String[] sources) {
        StringBuffer displayvalues = new StringBuffer();
        for (int i = 0; i < sources.length; ++i) {
            displayvalues.append(";").append(sources[i]);
        }
        PropertyList editorProps = new PropertyList();
        editorProps.setProperty("mode", "dropdownlist");
        editorProps.setProperty("displayvalue", displayvalues.substring(1));
        return new ConditionItem("eventsource", "Event Source", "equals", editorProps.toXMLString());
    }

    protected String resolveParamListVersion(String paramlistid, String paramlistversionid, String variantid) {
        if (paramlistversionid.length() == 0 || paramlistversionid.equalsIgnoreCase("C")) {
            String sql = "SELECT paramlistversionid FROM paramlist WHERE paramlistid = ? AND variantid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( paramlistversionid as integer ) DESC";
            DataSet versions = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{paramlistid, variantid});
            if (versions.size() > 0) {
                return versions.getValue(0, "paramlistversionid");
            }
            return paramlistversionid;
        }
        return paramlistversionid;
    }
}

