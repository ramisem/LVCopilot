/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.EventPlan;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.eventtype.EventTypeSetter;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaintRequest;
import com.labvantage.sapphire.pageelements.gwt.shared.EventPlanConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EventPlanRequest
extends SDIMaintRequest
implements EventPlanConstants {
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        ConnectionProcessor cp = new ConnectionProcessor(this.sapphireConnection.getConnectionId());
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        EventManager.checkEventPlansLoaded(sapphireConnection);
        LinkedHashMap eventTypes = (LinkedHashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), "EventManager", "EventTypes");
        super.processCommand(command, commandRequest, commandResponse);
        if (command.equals("loadsdimaint")) {
            try {
                commandResponse.set("eventtypes", this.loadEventTypes(eventTypes, false));
                commandResponse.set("sdc", this.getQueryProcessor().getSqlDataSet("SELECT sdcid FROM sdc ORDER BY sdcid"));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load eventplan. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        if (command.equals("savesdimaint")) {
            try {
                EventManager.loadEventPlans(sapphireConnection);
                if (commandResponse.get("sdcid").equals("LV_EventPlan")) {
                    SDIMaint sdiMaint = (SDIMaint)commandResponse.get("eventplan");
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDIList("LV_EventPlan", sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), "");
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setRequestItem("eventplanitem");
                    sdiRequest.setRequestItem("eventplanitemproperty");
                    sdiRequest.setRequestItem("eventplanitemcondition");
                    sdiRequest.setRequestItem("category");
                    sdiRequest.setExtendedDataTypes(true);
                    sdiRequest.setPrimaryLockOption("LA");
                    sdiRequest.setValidateCheckout(true);
                    sdiRequest.setRetainRsetid(true);
                    SDIProcessor sdiProcessor = this.getSDIProcessor();
                    SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                    if (sdiData == null) {
                        throw sdiProcessor.getLastException();
                    }
                    commandResponse.set("eventplan", new SDIMaint(this.getSDCProcessor().getPropertyList("LV_EventPlan"), sdiData));
                }
                SDIMaint sdiMaint = (SDIMaint)commandResponse.get("eventtype");
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDIList("LV_EventType", sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), "");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("eventtypecondition");
                sdiRequest.setRequestItem("category");
                sdiRequest.setExtendedDataTypes(true);
                sdiRequest.setPrimaryLockOption("LA");
                sdiRequest.setValidateCheckout(true);
                sdiRequest.setRetainRsetid(true);
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData == null) {
                    throw sdiProcessor.getLastException();
                }
                commandResponse.set("eventtype", new SDIMaint(this.getSDCProcessor().getPropertyList("LV_EventType"), sdiData));
            }
            catch (Throwable e) {
                throw new SapphireException("Failed to save eventplan/type. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        } else {
            if (command.equals("loadsetupconditions")) {
                try {
                    String eventtypeid = commandRequest.getString("eventtypeid");
                    HashMap eventTypeDetails = (HashMap)eventTypes.get(eventtypeid);
                    BaseEventType eventType = EventManager.getEventTypeInstance(eventtypeid, (String)eventTypeDetails.get("objectname"));
                    EventTypeSetter.startEvent(eventType, sapphireConnection, eventtypeid);
                    Condition[] setupConditions = eventType.getSetupConditions((DataSet)eventTypeDetails.get("setupconditions"));
                    PropertyList conditionData = new PropertyList();
                    commandResponse.set("conditiondata", conditionData);
                    conditionData.setProperty("status", "noconditions");
                    if (setupConditions != null) {
                        PropertyListCollection conditions = new PropertyListCollection();
                        for (int i = 0; i < setupConditions.length; ++i) {
                            Condition setupCondition = setupConditions[i];
                            PropertyList condition = new PropertyList();
                            DataSet conditionItems = new DataSet();
                            ConditionItem[] setupConditionItems = setupCondition.getConditionItems();
                            for (int j = 0; j < setupConditionItems.length; ++j) {
                                conditionItems.addRow();
                                conditionItems.setString(j, "conditionitem", setupConditionItems[j].getConditionitem());
                                conditionItems.setString(j, "displayvalue", setupConditionItems[j].getDisplayvalue());
                                conditionItems.setString(j, "conditionitemstatic", setupConditionItems[j].isStaticConditionItem() ? "Y" : "N");
                                conditionItems.setString(j, "operators", setupConditionItems[j].getOperators());
                                conditionItems.setString(j, "operatorstatic", setupConditionItems[j].isStaticOperator() ? "Y" : "N");
                                conditionItems.setString(j, "dynamicvalueeditor", setupConditionItems[j].isDynamicValueEditor() ? "Y" : "N");
                                if (setupConditionItems[j].isDynamicValueEditor()) continue;
                                if (setupConditionItems[j].getValueEditor().contains("<propertylist")) {
                                    PropertyList props = new PropertyList();
                                    props.setPropertyList(setupConditionItems[j].getValueEditor());
                                    conditionItems.setString(j, "valueeditor", props.toJSONString(false));
                                    continue;
                                }
                                conditionItems.setString(j, "valueeditor", setupConditionItems[j].getValueEditor());
                            }
                            condition.setProperty("conditionitems", JSONUtil.toJSONString(conditionItems));
                            conditions.add(condition);
                        }
                        conditionData.setProperty("status", "ok");
                        conditionData.setProperty("setupconditions", conditions);
                        Condition[] primitiveSetupConditions = eventType.getSetupConditions(null);
                        conditionData.setProperty("primitivesetupconditioncount", String.valueOf(primitiveSetupConditions != null ? primitiveSetupConditions.length : 0));
                        conditionData.setProperty("cascading", eventType.isSetupConditionsCascading() ? "Y" : "N");
                    } else {
                        conditionData.setProperty("status", "none");
                    }
                    EventTypeSetter.endEvent(eventType);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load setup conditions. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equals("loadsetupcondition")) {
                try {
                    String eventtypeid = commandRequest.getString("eventtypeid");
                    HashMap eventTypeDetails = (HashMap)eventTypes.get(eventtypeid);
                    BaseEventType eventType = EventManager.getEventTypeInstance(eventtypeid, (String)((HashMap)eventTypes.get(eventtypeid)).get("objectname"));
                    EventTypeSetter.startEvent(eventType, sapphireConnection, eventtypeid);
                    DataSet setupConditionValues = commandRequest.getDataSet("setupconditionvalues");
                    BaseEventType.addEventTypeConditions(setupConditionValues, (DataSet)eventTypeDetails.get("setupconditions"));
                    Condition setupCondition = eventType.getSetupCondition(Integer.parseInt(commandRequest.getString("setupconditionindex")), setupConditionValues);
                    PropertyList conditionData = new PropertyList();
                    commandResponse.set("conditiondata", conditionData);
                    conditionData.setProperty("status", "noconditions");
                    if (setupCondition != null) {
                        PropertyList condition = new PropertyList();
                        DataSet conditionItems = new DataSet();
                        ConditionItem[] setupConditionItems = setupCondition.getConditionItems();
                        for (int j = 0; j < setupConditionItems.length; ++j) {
                            conditionItems.addRow();
                            conditionItems.setString(j, "conditionitem", setupConditionItems[j].getConditionitem());
                            conditionItems.setString(j, "displayvalue", setupConditionItems[j].getDisplayvalue());
                            conditionItems.setString(j, "conditionitemstatic", setupConditionItems[j].isStaticConditionItem() ? "Y" : "N");
                            conditionItems.setString(j, "operators", setupConditionItems[j].getOperators());
                            conditionItems.setString(j, "operatorstatic", setupConditionItems[j].isStaticOperator() ? "Y" : "N");
                            conditionItems.setString(j, "dynamicvalueeditor", setupConditionItems[j].isDynamicValueEditor() ? "Y" : "N");
                            if (setupConditionItems[j].getValueEditor().contains("<propertylist")) {
                                PropertyList props = new PropertyList();
                                props.setPropertyList(setupConditionItems[j].getValueEditor());
                                conditionItems.setString(j, "valueeditor", props.toJSONString(false));
                                continue;
                            }
                            conditionItems.setString(j, "valueeditor", setupConditionItems[j].getValueEditor());
                        }
                        condition.setProperty("conditionitems", JSONUtil.toJSONString(conditionItems));
                        conditionData.setProperty("status", "ok");
                        conditionData.setProperty("condition", condition);
                    } else {
                        conditionData.setProperty("status", "none");
                    }
                    EventTypeSetter.endEvent(eventType);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load setup condition. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equals("loadfilterconditions")) {
                try {
                    String eventtypeid = commandRequest.getString("eventtypeid");
                    HashMap eventTypeDetails = (HashMap)eventTypes.get(eventtypeid);
                    BaseEventType eventType = EventManager.getEventTypeInstance(eventtypeid, (String)eventTypeDetails.get("objectname"));
                    EventTypeSetter.startEvent(eventType, sapphireConnection, eventtypeid);
                    DataSet conditionValues = commandRequest.getDataSet("setupconditionvalues");
                    BaseEventType.addEventTypeConditions(conditionValues, (DataSet)eventTypeDetails.get("setupconditions"));
                    Condition conditionTemplate = eventType.getFilterConditionTemplate(conditionValues);
                    PropertyList filterData = new PropertyList();
                    commandResponse.set("filterdata", filterData);
                    filterData.setProperty("status", "nocondition");
                    if (conditionTemplate != null) {
                        PropertyList condition = new PropertyList();
                        DataSet conditionItems = new DataSet();
                        ConditionItem[] filterConditionItems = conditionTemplate.getConditionItems();
                        for (int j = 0; j < filterConditionItems.length; ++j) {
                            conditionItems.addRow();
                            conditionItems.setString(j, "conditionitem", filterConditionItems[j].getConditionitem());
                            conditionItems.setString(j, "displayvalue", filterConditionItems[j].getDisplayvalue());
                            conditionItems.setString(j, "conditionitemstatic", filterConditionItems[j].isStaticConditionItem() ? "Y" : "N");
                            conditionItems.setString(j, "operators", filterConditionItems[j].getOperators());
                            conditionItems.setString(j, "operatorstatic", filterConditionItems[j].isStaticOperator() ? "Y" : "N");
                            if (filterConditionItems[j].getValueEditor().contains("<propertylist")) {
                                PropertyList props = new PropertyList();
                                props.setPropertyList(filterConditionItems[j].getValueEditor());
                                conditionItems.setString(j, "valueeditor", props.toJSONString(false));
                            } else {
                                conditionItems.setString(j, "valueeditor", filterConditionItems[j].getValueEditor());
                            }
                            conditionItems.setString(j, "dynamicvalueeditor", filterConditionItems[j].isDynamicValueEditor() ? "Y" : "N");
                        }
                        condition.setProperty("conditionitems", JSONUtil.toJSONString(conditionItems));
                        filterData.setProperty("status", "ok");
                        filterData.setProperty("condition", condition);
                    }
                    EventTypeSetter.endEvent(eventType);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load filter conditions. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equals("loadeventtype")) {
                try {
                    commandResponse.set("eventtypes", this.loadEventTypes(eventTypes, true));
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load eventtype. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equals("loadeventtypes")) {
                try {
                    if (commandRequest.getBoolean("eventdef") && !commandRequest.getBoolean("notificationevents")) {
                        commandResponse.set("eventtypes", this.loadEventDefTypes(eventTypes));
                    }
                    commandResponse.set("eventtypes", this.loadEventTypes(eventTypes, false));
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load eventtypes. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            } else {
                if (command.equals("saveeventtype")) {
                    try {
                        EventManager.loadEventPlans(sapphireConnection);
                        SDIMaint sdiMaint = (SDIMaint)commandResponse.get("eventtype");
                        SDIRequest sdiRequest = new SDIRequest();
                        sdiRequest.setSDIList("LV_EventType", sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), "");
                        sdiRequest.setRequestItem("primary");
                        sdiRequest.setRequestItem("eventtypecondition");
                        sdiRequest.setRequestItem("category");
                        sdiRequest.setExtendedDataTypes(true);
                        sdiRequest.setPrimaryLockOption("LA");
                        sdiRequest.setRetainRsetid(true);
                        commandResponse.set("eventtype", new SDIMaint(this.getSDCProcessor().getPropertyList("LV_EventType"), this.getSDIProcessor().getSDIData(sdiRequest)));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to save eventtype. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equals("loadsdieventplan")) {
                    try {
                        ArrayList policyNodeList;
                        WebAdminProcessor webAdminProcessor;
                        PropertyTree policy;
                        boolean linked = commandRequest.getString("linked").equals("Y");
                        String sdcid = commandRequest.getString("sdcid");
                        ArrayList<String> sdclist = new ArrayList<String>();
                        if (linked && (policy = (webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId())).getPropertyTree("CopyDownPolicy")) != null && (policyNodeList = policy.getAllNodes()) != null) {
                            StringBuffer linkedsdclist = new StringBuffer();
                            StringBuffer linkedsdcplurallist = new StringBuffer();
                            for (int i = 0; i < policyNodeList.size(); ++i) {
                                Node node = (Node)policyNodeList.get(i);
                                if (node.getNodeList().size() != 0) continue;
                                PropertyList nodePropertyList = policy.getNodePropertyList(node.getNodeId(), true);
                                String nodesdcid = nodePropertyList.getProperty("sdcid");
                                PropertyListCollection copyFrom = nodePropertyList.getCollection("copyfrom");
                                if (copyFrom == null) continue;
                                for (int j = 0; j < copyFrom.size(); ++j) {
                                    PropertyList copyFromSDC = copyFrom.getPropertyList(j);
                                    if (!copyFromSDC.getProperty("sdcid").equals(sdcid) || !copyFromSDC.getProperty("copyeventplans").equals("Y")) continue;
                                    sdclist.add(nodesdcid);
                                    linkedsdclist.append(";").append(nodesdcid);
                                    linkedsdcplurallist.append(";").append(this.getSDCProcessor().getPropertyList(nodesdcid).getProperty("plural"));
                                }
                            }
                            commandResponse.set("linkedsdclist", linkedsdclist.length() > 0 ? linkedsdclist.substring(1) : "");
                            commandResponse.set("linkedsdcplurallist", linkedsdcplurallist.length() > 0 ? linkedsdcplurallist.substring(1) : "");
                        }
                        ArrayList<EventPlan> eventPlans = EventManager.getSDIEventPlans(sapphireConnection, sdcid, commandRequest.getString("keyid1"), commandRequest.getString("keyid2"), commandRequest.getString("keyid3"), linked, sdclist);
                        commandResponse.set("eventplans", this.getJSONEventPlanArray(eventPlans).toString());
                        commandResponse.set("editorstyles", this.getEditorStyles(eventPlans));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load sdieventplans. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equals("loadeventplanhistory")) {
                    try {
                        String sdcid = commandRequest.getString("sdcid");
                        String keyid1 = commandRequest.getString("keyid1");
                        String keyid2 = commandRequest.getString("keyid2", "(null)");
                        String keyid3 = commandRequest.getString("keyid3", "(null)");
                        String eventplanid = commandRequest.getString("eventplanid");
                        String eventplanversionid = commandRequest.getString("eventplanversionid");
                        String eventplanitemid = commandRequest.getString("eventplanitemid");
                        String sql = "SELECT eph.eventplanid, eph.eventplanversionid, eph.eventplaninstance, epi.eventplanitemid, epi.eventplanitemdesc, epi.eventtypeid, eph.eventdt, eph.eventsummary, eph.eventlog, eph.createby FROM eventplanhistory eph, eventplanitem epi WHERE eph.eventplanid = epi.eventplanid AND eph.eventplanversionid = epi.eventplanversionid AND eph.eventplanitemid = epi.eventplanitemid AND sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?";
                        DataSet history = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcid, keyid1, keyid2, keyid3}, true);
                        HashMap<String, String> findMap = new HashMap<String, String>();
                        findMap.put("eventplanid", eventplanid);
                        findMap.put("eventplanversionid", eventplanversionid);
                        findMap.put("eventplanitemid", eventplanitemid);
                        int selectRow = history.findRow(findMap);
                        commandResponse.set("eventplanhistory", history);
                        commandResponse.set("selectrow", String.valueOf(selectRow));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load eventplan history. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
                if (command.equals("loadeventplans")) {
                    try {
                        ArrayList<EventPlan> eventPlans = EventManager.getEventPlans(sapphireConnection, StringUtil.split(commandRequest.getString("eventplanid"), ";"), StringUtil.split(commandRequest.getString("eventplanversionid"), ";"));
                        commandResponse.set("eventplans", this.getJSONEventPlanArray(eventPlans).toString());
                        commandResponse.set("editorstyles", this.getEditorStyles(eventPlans));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load eventplans. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                    }
                }
            }
        }
        return true;
    }

    private JSONArray getJSONEventPlanArray(ArrayList<EventPlan> eventPlans) {
        JSONArray jsonArray = new JSONArray();
        for (EventPlan eventPlan : eventPlans) {
            jsonArray.put(eventPlan.toJSONObject());
        }
        return jsonArray;
    }

    private PropertyList getEditorStyles(ArrayList<EventPlan> eventPlans) {
        StringBuffer editorstyles = new StringBuffer();
        for (EventPlan eventPlan : eventPlans) {
            DataSet eventplanproperties = eventPlan.getEventPlanProperties();
            for (int i = 0; i < eventplanproperties.size(); ++i) {
                if (eventplanproperties.getValue(i, "editorstyleid").length() <= 0) continue;
                editorstyles.append(";").append(eventplanproperties.getValue(i, "editorstyleid"));
            }
        }
        if (editorstyles.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList("LV_EditorStyle", editorstyles.substring(1), "", "");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet primary = sdiData.getDataset("primary");
            PropertyList editorstylemap = new PropertyList();
            for (int i = 0; i < primary.size(); ++i) {
                PropertyList def = new PropertyList();
                try {
                    def.setPropertyList(primary.getValue(i, "editordefinition"));
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
                editorstylemap.setProperty(primary.getValue(i, "editorstyleid"), def.toJSONString(false));
            }
            return editorstylemap;
        }
        return new PropertyList();
    }

    private DataSet loadEventTypes(HashMap<String, HashMap> eventTypes, boolean primatives) {
        DataSet eventtypes = new DataSet();
        for (String eventtypeid : eventTypes.keySet()) {
            HashMap eventType = eventTypes.get(eventtypeid);
            if (primatives && (!primatives || ((String)eventType.get("basedoneventtypeid")).length() != 0)) continue;
            int row = eventtypes.addRow();
            eventtypes.setString(row, "eventtypeid", eventtypeid);
            eventtypes.setString(row, "eventtypedesc", (String)eventType.get("eventtypedesc"));
            eventtypes.setString(row, "eventtypesdcid", (String)eventType.get("eventtypesdcid"));
            eventtypes.setString(row, "basedoneventtypeid", (String)eventType.get("basedoneventtypeid"));
            eventtypes.setString(row, "objectname", (String)eventType.get("objectname"));
            eventtypes.setString(row, "usercontexttoken", (String)eventType.get("usercontexttoken"));
        }
        return eventtypes;
    }

    private DataSet loadEventDefTypes(HashMap<String, HashMap> eventTypes) {
        DataSet eventtypes = new DataSet();
        for (String eventtypeid : eventTypes.keySet()) {
            HashMap eventType = eventTypes.get(eventtypeid);
            if (!"SDIPrimaryValueChanged".equals((String)eventType.get("basedoneventtypeid")) && !"SDIPrimaryValueChanged".equals((String)eventType.get("eventtypeid")) && !"TestValueChanged".equals((String)eventType.get("basedoneventtypeid")) && !"TestValueChanged".equals((String)eventType.get("eventtypeid"))) continue;
            int row = eventtypes.addRow();
            eventtypes.setString(row, "eventtypeid", eventtypeid);
            eventtypes.setString(row, "eventtypedesc", (String)eventType.get("eventtypedesc"));
            eventtypes.setString(row, "eventtypesdcid", (String)eventType.get("eventtypesdcid"));
            eventtypes.setString(row, "basedoneventtypeid", (String)eventType.get("basedoneventtypeid"));
            eventtypes.setString(row, "objectname", (String)eventType.get("objectname"));
            eventtypes.setString(row, "usercontexttoken", (String)eventType.get("usercontexttoken"));
        }
        return eventtypes;
    }
}

