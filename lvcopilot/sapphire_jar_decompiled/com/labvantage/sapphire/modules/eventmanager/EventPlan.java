/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.pageelements.gwt.shared.EventPlanConstants;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class EventPlan
implements EventPlanConstants {
    public static final String UNVERSIONED = "U";
    private String eventplanid;
    private String eventplanversionid;
    private String eventplandesc;
    private boolean active = true;
    private boolean global = false;
    private String sdcid;
    private DataSet eventplan;
    private DataSet eventplanitems;
    private DataSet eventplanproperties;
    private DataSet eventplanconditions;
    private DataSet sdieventplan;
    private DataSet sdieventplanitems;
    private DataSet sdieventplanproperties;
    private DataSet eventplanhistory;
    private String eventplaneventplanitemid;
    private HashMap<String, DataSet> eventplanitemproperties = new HashMap();
    private HashMap<String, DataSet> eventplanitemconditions = new HashMap();
    private HashMap<String, DataSet> eventfunctions = new HashMap();
    private HashMap<String, String[]> scripttokens = new HashMap();
    private HashMap<String, ArrayList<Integer>> eventObjectPlanEvents;
    private HashMap<Integer, String> eventEventSource = new HashMap();
    private int eventplanitemid = 1;
    public static SDIData eventPlanDataTemplate;
    private HashMap<String, Integer> evaluatedCount = new HashMap();
    private HashMap<String, Integer> firedCount = new HashMap();
    private HashMap<String, Integer> executedCount = new HashMap();
    private HashMap<String, Integer> eventItemsCount = new HashMap();
    private HashMap<String, Calendar> lastEventDt = new HashMap();
    private HashMap<String, String> lastEventId = new HashMap();
    private HashMap<String, String> lastEventConnectionid = new HashMap();
    private HashMap<String, String> lastEventSDC = new HashMap();

    public EventPlan(DataSet eventplan) {
        this.eventplan = eventplan;
        this.initEventPlan(eventplan);
    }

    public EventPlan(String eventplanid, String eventplandesc) {
        if (eventPlanDataTemplate == null) {
            throw new RuntimeException("Event plan tables template not loaded");
        }
        this.eventplan = eventPlanDataTemplate.getDataset("primary").copy();
        this.eventplan.addRow();
        this.eventplan.setString(0, "eventplanid", eventplanid);
        this.eventplan.setString(0, "eventplanversionid", UNVERSIONED);
        this.eventplan.setString(0, "eventplandesc", eventplandesc);
        this.eventplan.setString(0, "scopeflag", "G");
        this.eventplan.setString(0, "statusflag", "A");
        this.initEventPlan(this.eventplan);
        this.setEventPlanItems(eventPlanDataTemplate.getDataset("eventplanitem").copy());
        this.setEventPlanProperties(eventPlanDataTemplate.getDataset("eventplanitemproperty").copy());
        this.setEventPlanConditions(eventPlanDataTemplate.getDataset("eventplanitemcondition").copy());
        this.setSDIEventPlan(eventPlanDataTemplate.getDataset("sdieventplan").copy());
        this.setSDIEventPlanItems(eventPlanDataTemplate.getDataset("sdieventplanitem").copy());
        this.setSDIEventPlanProperties(eventPlanDataTemplate.getDataset("sdieventplanitemproperty").copy());
    }

    private void initEventPlan(DataSet eventplan) {
        this.eventplanid = eventplan.getValue(0, "eventplanid");
        this.eventplanversionid = eventplan.getValue(0, "eventplanversionid");
        this.eventplandesc = eventplan.getValue(0, "eventplandesc");
        this.global = eventplan.getValue(0, "scopeflag").equals("G");
        this.active = eventplan.getValue(0, "statusflag").equals("A");
        this.sdcid = eventplan.getValue(0, "eventplansdcid");
    }

    public String getEventPlanKey() {
        return this.eventplanid + ";" + this.eventplanversionid;
    }

    public String getEventplanid() {
        return this.eventplanid;
    }

    public String getEventplanversionid() {
        return this.eventplanversionid;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public String getSdcid() {
        return this.sdcid != null ? this.sdcid : "";
    }

    public String getEventPlanEventPlanItemId() {
        return this.eventplaneventplanitemid;
    }

    public void setEventPlanEventPlanItemId(String eventplaneventplanitemid) {
        this.eventplaneventplanitemid = eventplaneventplanitemid;
    }

    public void setEventPlan(DataSet eventplan) {
        this.eventplan = eventplan;
    }

    public void setEventPlanItems(DataSet eventplanitems) {
        this.eventplanitems = eventplanitems;
    }

    public void addEvent(String eventtypeid, String eventplanitemdesc, DataSet conditions, String type, String script, boolean processAsync) {
        if (this.eventplanitems == null) {
            this.eventplanitems = eventPlanDataTemplate.getDataset("eventplanitem").copy();
            this.eventplanitemid = 1;
            this.eventplanitems.addRow();
            this.eventplanitems.setString(0, "eventplanid", this.getEventplanid());
            this.eventplanitems.setString(0, "eventplanversionid", this.getEventplanversionid());
            this.eventplanitems.setString(0, "eventplanitemid", String.valueOf(this.eventplanitemid));
            this.eventplanitems.setString(0, "itemtypeflag", "P");
            this.eventplanitems.setNumber(0, "usersequence", this.eventplanitemid);
            ++this.eventplanitemid;
        }
        int row = this.eventplanitems.addRow();
        this.eventplanitems.setString(row, "eventplanid", this.getEventplanid());
        this.eventplanitems.setString(row, "eventplanversionid", this.getEventplanversionid());
        this.eventplanitems.setString(row, "eventplanitemid", String.valueOf(this.eventplanitemid));
        this.eventplanitems.setString(row, "eventplanitemdesc", eventplanitemdesc);
        this.eventplanitems.setString(row, "itemtypeflag", "E");
        this.eventplanitems.setString(row, "eventtypeid", eventtypeid);
        this.eventplanitems.setString(row, "parenteventplanitemid", "1");
        this.eventplanitems.setNumber(0, "usersequence", this.eventplanitemid);
        if (conditions != null && conditions.size() > 0) {
            if (this.eventplanconditions == null) {
                this.eventplanconditions = eventPlanDataTemplate.getDataset("eventplanitemcondition").copy();
            }
            for (int i = 0; i < conditions.size(); ++i) {
                row = this.eventplanconditions.addRow();
                this.eventplanconditions.setString(row, "eventplanid", this.getEventplanid());
                this.eventplanconditions.setString(row, "eventplanversionid", this.getEventplanversionid());
                this.eventplanconditions.setString(row, "eventplanitemid", String.valueOf(this.eventplanitemid));
                this.eventplanconditions.setString(row, "conditionid", conditions.getValue(i, "conditionid"));
                this.eventplanconditions.setString(row, "conditionitem", conditions.getValue(i, "conditionitem"));
                this.eventplanconditions.setString(row, "operator1", conditions.getValue(i, "operator1"));
                this.eventplanconditions.setString(row, "value1", conditions.getValue(i, "value1"));
                this.eventplanconditions.setString(row, "operator2", conditions.getValue(i, "operator2"));
                this.eventplanconditions.setString(row, "value2", conditions.getValue(i, "value2"));
            }
        }
        ++this.eventplanitemid;
        row = this.eventplanitems.addRow();
        this.eventplanitems.setString(row, "eventplanid", this.getEventplanid());
        this.eventplanitems.setString(row, "eventplanversionid", this.getEventplanversionid());
        this.eventplanitems.setString(row, "eventplanitemid", String.valueOf(this.eventplanitemid));
        this.eventplanitems.setString(row, "eventplanitemdesc", eventplanitemdesc);
        this.eventplanitems.setString(row, "itemtypeflag", "F");
        this.eventplanitems.setString(row, "parenteventplanitemid", String.valueOf(this.eventplanitemid - 1));
        this.eventplanitems.setNumber(0, "usersequence", this.eventplanitemid);
        this.eventplanitems.setString(row, "processingscripttypeflag", type);
        this.eventplanitems.setClob(row, "processingscript", script);
        this.eventplanitems.setString(row, "asynchronousflag", processAsync ? "Y" : "N");
        ++this.eventplanitemid;
    }

    public boolean hasEvent(String eventplanitemdesc) {
        return this.eventplanitems != null && this.eventplanitems.findRow("eventplanitemdesc", eventplanitemdesc) >= 0;
    }

    public DataSet getEventPlanItems() {
        return this.eventplanitems;
    }

    public void setEventPlanProperties(DataSet eventplanproperties) {
        this.eventplanproperties = eventplanproperties;
    }

    public DataSet getEventPlanProperties() {
        return this.eventplanproperties;
    }

    public void setEventPlanConditions(DataSet eventplanconditions) {
        this.eventplanconditions = eventplanconditions;
    }

    public DataSet getEventPlanConditions() {
        return this.eventplanconditions;
    }

    public DataSet getEventPlanProperties(String eventplanitemid) {
        DataSet properties = this.eventplanitemproperties.get(eventplanitemid);
        if (properties == null) {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("eventplanitemid", eventplanitemid);
            properties = this.eventplanproperties.getFilteredDataSet(filterMap);
            this.eventplanitemproperties.put(eventplanitemid, properties);
        }
        return properties;
    }

    public DataSet getEventPlanConditions(String eventplanitemid) {
        DataSet conditions = this.eventplanitemconditions.get(eventplanitemid);
        if (conditions == null) {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("eventplanitemid", eventplanitemid);
            conditions = this.eventplanconditions.getFilteredDataSet(filterMap);
            this.eventplanitemconditions.put(eventplanitemid, conditions);
        }
        return conditions;
    }

    public DataSet getEventFunctions(String eventplanitemid) {
        DataSet functions = this.eventfunctions.get(eventplanitemid);
        if (functions == null) {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("parenteventplanitemid", eventplanitemid);
            functions = this.eventplanitems.getFilteredDataSet(filterMap);
            this.eventfunctions.put(eventplanitemid, functions);
        }
        return functions;
    }

    public void setSDIEventPlan(DataSet sdieventplan) {
        this.sdieventplan = sdieventplan;
    }

    public void setSDIEventPlanItems(DataSet sdieventplanitems) {
        this.sdieventplanitems = sdieventplanitems;
    }

    public void setSDIEventPlanProperties(DataSet sdieventplanproperties) {
        this.sdieventplanproperties = sdieventplanproperties;
    }

    public void setEventPlanHistory(DataSet eventplanhistory) {
        this.eventplanhistory = eventplanhistory;
    }

    public String[] getProcessingInputTokens(String eventplanitemid) {
        String[] tokens = this.scripttokens.get(eventplanitemid);
        if (tokens == null) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("eventplanitemid", eventplanitemid);
            int row = this.eventplanitems.findRow(findMap);
            if (row >= 0) {
                ArrayList<String> tokenList = new ArrayList<String>();
                if (this.eventplanitems.getValue(row, "processingscripttypeflag", "B").equals("B")) {
                    String processingscript = this.eventplanitems.getValue(row, "processingscript");
                    for (String token : tokens = StringUtil.getTokens(processingscript, "[", "]", false)) {
                        if (token.startsWith("$G{")) {
                            tokenList.addAll(this.getGroovyTokens(token, "inputs."));
                            continue;
                        }
                        tokenList.add(token);
                    }
                    for (String token : tokens = StringUtil.getTokens(processingscript, "$G{", "}", false)) {
                        if (token.indexOf("/*-GAP Editor Generated-*/;") >= 0) {
                            token = token.substring(token.indexOf("/*-GAP Editor Generated-*/;") + "/*-GAP Editor Generated-*/;".length());
                            String[] conditions = StringUtil.split(token, "|%|");
                            for (int i = 0; i < conditions.length; ++i) {
                                String[] parts = StringUtil.split(conditions[i], "#$#");
                                tokenList.add(parts[0]);
                            }
                            continue;
                        }
                        tokenList.addAll(this.getGroovyTokens(token, "inputs."));
                    }
                } else {
                    tokenList.addAll(this.getGroovyTokens(this.eventplanitems.getValue(row, "processingscript"), "inputs."));
                }
                this.scripttokens.put(eventplanitemid, tokenList.toArray(new String[tokenList.size()]));
            }
        }
        return this.scripttokens.get(eventplanitemid);
    }

    private ArrayList getGroovyTokens(String groovyScript, String groovyBindVar) {
        ArrayList<String> groovyTokens = new ArrayList<String>();
        int pos = groovyScript.indexOf(groovyBindVar);
        while (pos >= 0 && pos < groovyScript.length()) {
            int start = pos += groovyBindVar.length();
            if (Character.isJavaIdentifierStart(groovyScript.charAt(pos))) {
                ++pos;
                while (pos < groovyScript.length() && Character.isJavaIdentifierPart(groovyScript.charAt(pos))) {
                    ++pos;
                }
                groovyTokens.add(groovyScript.substring(start, pos));
            }
            pos = groovyScript.indexOf(groovyBindVar, pos);
        }
        return groovyTokens;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("eventplanid", this.eventplanid);
            jsonObj.put("eventplanversionid", this.eventplanversionid);
            jsonObj.put("eventplandesc", this.eventplandesc);
            jsonObj.put("sdcid", this.sdcid);
            jsonObj.put("global", this.global ? "Y" : "N");
            jsonObj.put("eventplan", JSONUtil.toJSONString(this.eventplan));
            jsonObj.put("eventplanitem", JSONUtil.toJSONString(this.eventplanitems));
            jsonObj.put("eventplanitemproperty", JSONUtil.toJSONString(this.eventplanproperties));
            jsonObj.put("eventplanitemcondition", JSONUtil.toJSONString(this.eventplanconditions));
            jsonObj.put("sdieventplan", JSONUtil.toJSONString(this.sdieventplan));
            jsonObj.put("sdieventplanitem", JSONUtil.toJSONString(this.sdieventplanitems));
            jsonObj.put("sdieventplanitemproperty", JSONUtil.toJSONString(this.sdieventplanproperties));
            if (this.eventplanhistory == null) {
                this.eventplanhistory = new DataSet();
            }
            jsonObj.put("eventplanhistory", JSONUtil.toJSONString(this.eventplanhistory));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return jsonObj;
    }

    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public EventPlan copy() {
        EventPlan copy = new EventPlan(this.eventplan.copy());
        copy.setEventPlanItems(this.eventplanitems.copy());
        copy.setEventPlanProperties(this.eventplanproperties.copy());
        copy.setEventPlanConditions(this.eventplanconditions.copy());
        copy.setSDIEventPlan(this.sdieventplan.copy());
        copy.setSDIEventPlanItems(this.sdieventplanitems.copy());
        copy.setSDIEventPlanProperties(this.sdieventplanproperties.copy());
        return copy;
    }

    public void setEventObjectPlanEvents(HashMap<String, ArrayList<Integer>> eventObjectPlanEvents) {
        this.eventObjectPlanEvents = eventObjectPlanEvents;
    }

    public ArrayList<Integer> getEventObjectPlanEvents(String eventObjectClassName) {
        return this.eventObjectPlanEvents.containsKey(eventObjectClassName) ? this.eventObjectPlanEvents.get(eventObjectClassName) : new ArrayList<Integer>();
    }

    public HashMap<Integer, String> getEventEventSource() {
        return this.eventEventSource;
    }

    public void setEventEventSource(HashMap<Integer, String> eventEventSource) {
        this.eventEventSource = eventEventSource;
    }

    public void evaluate(String eventplanitemid, BaseEventObject eventObject, String connectionid) {
        this.evaluatedCount.put(eventplanitemid, this.evaluatedCount.get(eventplanitemid) == null ? 1 : this.evaluatedCount.get(eventplanitemid) + 1);
        this.lastEventId.put(eventplanitemid, eventObject.getEventid());
        this.lastEventDt.put(eventplanitemid, Calendar.getInstance());
        this.lastEventConnectionid.put(eventplanitemid, connectionid);
    }

    public void fired(String eventplanitemid, BaseEventObject eventObject) {
        this.firedCount.put(this.eventplaneventplanitemid, this.firedCount.get(this.eventplaneventplanitemid) == null ? 1 : this.firedCount.get(this.eventplaneventplanitemid) + 1);
        this.firedCount.put(eventplanitemid, this.firedCount.get(eventplanitemid) == null ? 1 : this.firedCount.get(eventplanitemid) + 1);
    }

    public void executed(String eventplanitemid, BaseEventObject eventObject, ArrayList<BaseEventItem> firedEventItems) {
        this.executedCount.put(eventplanitemid, this.executedCount.get(eventplanitemid) == null ? 1 : this.executedCount.get(eventplanitemid) + 1);
        this.eventItemsCount.put(eventplanitemid, firedEventItems.size());
        this.lastEventSDC.put(eventplanitemid, eventObject instanceof BaseSDCEventObject ? ((BaseSDCEventObject)eventObject).getSdcid() : "");
    }

    public int getEvalCount(String eventplanitemid) {
        return this.evaluatedCount.get(eventplanitemid) != null ? this.evaluatedCount.get(eventplanitemid) : 0;
    }

    public int getFiredCount(String eventplanitemid) {
        return this.firedCount.get(eventplanitemid) != null ? this.firedCount.get(eventplanitemid) : 0;
    }

    public int getExecutedCount(String eventplanitemid) {
        return this.executedCount.get(eventplanitemid) != null ? this.executedCount.get(eventplanitemid) : 0;
    }

    public int getEventItemsCount(String eventplanitemid) {
        return this.eventItemsCount.get(eventplanitemid) != null ? this.eventItemsCount.get(eventplanitemid) : 0;
    }

    public Calendar getLastEventDt(String eventplanitemid) {
        return this.lastEventDt.get(eventplanitemid);
    }

    public String getLastEventId(String eventplanitemid) {
        return this.lastEventId.get(eventplanitemid);
    }

    public String getLastEventConnectionId(String eventplanitemid) {
        return this.lastEventConnectionid.get(eventplanitemid);
    }

    public String getLastEventSDC(String eventplanitemid) {
        return this.lastEventSDC.get(eventplanitemid);
    }

    public void resetStats() {
        this.evaluatedCount.clear();
        this.firedCount.clear();
        this.executedCount.clear();
        this.eventItemsCount.clear();
        this.lastEventDt.clear();
        this.lastEventId.clear();
        this.lastEventConnectionid.clear();
        this.lastEventSDC.clear();
    }
}

