/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eventplans.EventPlanHistory;
import com.labvantage.sapphire.actions.workflow.TaskInputEvent;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.eventmanager.Event;
import com.labvantage.sapphire.modules.eventmanager.EventPlan;
import com.labvantage.sapphire.modules.eventmanager.Notify;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.WorkItemEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseSDCEventType;
import com.labvantage.sapphire.modules.eventmanager.eventtype.EventTypeSetter;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.NotificationConstants;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.shared.EventPlanConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EventManager
implements EventPlanConstants,
NotificationConstants {
    public static final String LOGGER = "EventManager";
    public static final String WORKFLOW_EVENTPLAN = "__WorkflowEvents";
    public static final String EVENTTYPES = "EventTypes";
    public static final String EVENTPLANS = "EventPlans";
    public static final String GLOBALPLANS = "GlobalPlans";
    public static final String GLOBALSDCPLANS = "GlobalSDCPlans";
    public static final String HASPLANS = "HasPlans";
    public static final String REQUIRESSUPPLEMENTALDATA = "RequiresSupplementalData";
    public static final String EVENTTYPEIMPLEMENTATIONS = "EventTypeImplementations";
    public static final String STATS_EID = "eid";
    public static final String STATS_CONNECTIONID = "connectionid";
    public static final String STATS_EVENTID = "eventid";
    public static final String STATS_EVENTDT = "eventdt";
    public static final String STATS_EVENTCLASS = "eventclass";
    public static final String STATS_EVENTITEMCOUNT = "eventitemcount";
    public static final String STATS_EVENTITEMS = "eventitems";
    public static final String STATS_EVENTPLANCOUNT = "eventplancount";
    public static final String STATS_EVENTPLANID = "eventplanid";
    public static final String STATS_EVENTPLANITEMDESC = "eventplanitemdesc";
    public static final String STATS_FUNCTIONDESC = "functiondesc";
    public static final String STATS_SDCID = "sdcid";
    public static final String STATS_STATUS = "status";
    public static final String STATS_STATUSMESSAGE = "statusmessage";
    public static final String STATS_STATUS_IGNORED = "Ignored";
    public static final String STATS_STATUS_DISABLED = "Disabled";
    public static final String STATS_STATUS_HASSDCPLAN = "HasSDCPlan";
    public static final String STATS_STATUS_NOTMATCHED = "No Match";
    public static final String STATS_STATUS_FIRED = "FIRED";
    private static Boolean checkLoadedLock = new Boolean(true);
    private static Boolean loadingLock = new Boolean(false);
    private static boolean loading = false;
    private static ArrayList<DataSet> eventStatsList = new ArrayList();
    private static int maxEventStats = 100;
    private static boolean statsLogging = true;
    static File testRAK;
    static boolean eventLogging;
    static boolean testMode;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void resetStats(SapphireConnection sapphireConnection) {
        ArrayList<DataSet> arrayList = eventStatsList;
        synchronized (arrayList) {
            eventStatsList.clear();
        }
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        if (eventPlans != null) {
            for (String eventplan : eventPlans.keySet()) {
                ((EventPlan)eventPlans.get(eventplan)).resetStats();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkEventPlansLoaded(SapphireConnection sapphireConnection) {
        Boolean bl = checkLoadedLock;
        synchronized (bl) {
            HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
            if (eventPlans == null || eventPlans.size() == 0) {
                EventManager.loadEventPlans(sapphireConnection);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadEventPlans(SapphireConnection sapphireConnection) {
        Boolean bl = loadingLock;
        synchronized (bl) {
            loading = true;
            EventManager.loadEventPlans(sapphireConnection, "statusflag='A' AND ( activeflag = 'Y' OR activeflag IS NULL )");
            EventManager.defineWorkflowEventPlans(sapphireConnection);
            EventManager.addTransientPlan(sapphireConnection, "__nullplan", "", new ArrayList<Event>());
            EventManager.defineNotificationEventPlans(sapphireConnection);
            loading = false;
        }
    }

    public static void loadEventPlans(SapphireConnection sapphireConnection, String queryWhere) {
        LinkedHashMap<String, HashMap> eventTypes = new LinkedHashMap<String, HashMap>();
        HashMap<String, EventPlan> eventPlans = new HashMap<String, EventPlan>();
        HashSet<String> globalPlans = new HashSet<String>();
        HashSet<String> globalSDCPlans = new HashSet<String>();
        ArrayList<String> instancePlans = new ArrayList<String>();
        HashSet<String> sdcHasPlans = new HashSet<String>();
        HashMap<String, Object> requiresSupplementalData = new HashMap<String, Object>();
        HashMap<String, HashSet> eventTypeImplementations = new HashMap<String, HashSet>();
        SDIProcessor sdiProcessor = testRAK != null ? new SDIProcessor(testRAK, sapphireConnection.getConnectionId()) : new SDIProcessor(sapphireConnection.getConnectionId());
        LogContext logContext = new LogContext(sapphireConnection.getConnectionId());
        try {
            Trace.logDebug(LOGGER, "Loading event types into cache", logContext);
            SDIRequest eventTypeRequest = new SDIRequest();
            eventTypeRequest.setRequestItem("primary");
            eventTypeRequest.setRequestItem("eventtypecondition");
            eventTypeRequest.setSDCid("LV_EventType");
            eventTypeRequest.setQueryFrom("eventtype");
            eventTypeRequest.setQueryOrderBy("eventtypeid");
            eventTypeRequest.setExtendedDataTypes(true);
            SDIData eventTypeData = sdiProcessor.getSDIData(eventTypeRequest);
            DataSet eventTypePrimary = eventTypeData.getDataset("primary");
            DataSet eventTypeConditions = eventTypeData.getDataset("eventtypecondition");
            HashMap<String, String> eventTypeFilterMap = new HashMap<String, String>();
            for (int i = 0; i < eventTypePrimary.size(); ++i) {
                String eventtypeid = eventTypePrimary.getValue(i, "eventtypeid");
                String eventtypeClassName = eventTypePrimary.getValue(i, "objectname");
                eventTypeFilterMap.put("eventtypeid", eventtypeid);
                HashMap<String, Object> eventTypeDetails = new HashMap<String, Object>();
                eventTypes.put(eventtypeid, eventTypeDetails);
                eventTypeDetails.put("eventtypeid", eventtypeid);
                eventTypeDetails.put("eventtypedesc", eventTypePrimary.getValue(i, "eventtypedesc"));
                eventTypeDetails.put("eventtypesdcid", eventTypePrimary.getValue(i, "eventtypesdcid"));
                eventTypeDetails.put("basedoneventtypeid", eventTypePrimary.getValue(i, "basedoneventtypeid"));
                eventTypeDetails.put("objectname", eventtypeClassName);
                DataSet eventtypesetupconditions = eventTypeConditions.getFilteredDataSet(eventTypeFilterMap);
                eventTypeDetails.put("setupconditions", eventtypesetupconditions);
                for (int k = 0; k < eventtypesetupconditions.size(); ++k) {
                    if (!eventtypesetupconditions.getValue(k, "conditionitem").equals(STATS_SDCID)) continue;
                    eventTypeDetails.put("eventtypesdcid", eventtypesetupconditions.getValue(k, "value1"));
                }
                try {
                    BaseEventType eventType = (BaseEventType)Class.forName(eventtypeClassName).newInstance();
                    eventTypeDetails.put("usercontexttoken", eventType.getUserContextToken());
                    Class[] eventTypeEventObjects = eventType.getEventObjectImplementations();
                    HashSet<String> eti = new HashSet<String>();
                    for (Class eventTypeEventObject : eventTypeEventObjects) {
                        eti.add(eventTypeEventObject.getName());
                    }
                    eventTypeImplementations.put(eventtypeid, eti);
                    continue;
                }
                catch (Exception e) {
                    Trace.logError(LOGGER, "Error creating event type for eventtypeid '" + eventtypeid + "'. Reason: " + e.getMessage(), e, logContext);
                }
            }
            if (EventPlan.eventPlanDataTemplate == null) {
                Trace.logDebug(LOGGER, "Loading template event plan tables", logContext);
                SDIRequest eventPlanRequest = new SDIRequest();
                eventPlanRequest.setRequestItem("primary");
                eventPlanRequest.setRequestItem("eventplanitem");
                eventPlanRequest.setRequestItem("eventplanitemproperty");
                eventPlanRequest.setRequestItem("eventplanitemcondition");
                eventPlanRequest.setRequestItem("sdieventplan");
                eventPlanRequest.setRequestItem("sdieventplanitem");
                eventPlanRequest.setRequestItem("sdieventplanitemproperty");
                eventPlanRequest.setSDCid("LV_EventPlan");
                eventPlanRequest.setQueryFrom("eventplan");
                eventPlanRequest.setQueryWhere("1=2");
                eventPlanRequest.setExtendedDataTypes(true);
                EventPlan.eventPlanDataTemplate = sdiProcessor.getSDIData(eventPlanRequest);
            }
            Trace.logDebug(LOGGER, "Loading event plans into cache", logContext);
            SDIRequest eventPlanRequest = new SDIRequest();
            eventPlanRequest.setRequestItem("primary");
            eventPlanRequest.setRequestItem("eventplanitem");
            eventPlanRequest.setRequestItem("eventplanitemproperty");
            eventPlanRequest.setRequestItem("eventplanitemcondition");
            eventPlanRequest.setRequestItem("sdieventplan");
            eventPlanRequest.setRequestItem("sdieventplanitem");
            eventPlanRequest.setRequestItem("sdieventplanitemproperty");
            eventPlanRequest.setSDCid("LV_EventPlan");
            eventPlanRequest.setQueryFrom("eventplan");
            eventPlanRequest.setQueryWhere(queryWhere);
            eventPlanRequest.setExtendedDataTypes(true);
            SDIData eventPlanData = sdiProcessor.getSDIData(eventPlanRequest);
            DataSet eventPlanPrimary = eventPlanData.getDataset("primary");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (int i = 0; i < eventPlanPrimary.size(); ++i) {
                String eventplanid = eventPlanPrimary.getValue(i, STATS_EVENTPLANID);
                String eventplanversionid = eventPlanPrimary.getValue(i, "eventplanversionid");
                filterMap.put(STATS_EVENTPLANID, eventplanid);
                filterMap.put("eventplanversionid", eventplanversionid);
                EventPlan eventPlan = new EventPlan(eventPlanPrimary.getFilteredDataSet(filterMap));
                eventPlan.setEventPlan(eventPlanData.getDataset("primary").getFilteredDataSet(filterMap));
                eventPlan.setEventPlanItems(eventPlanData.getDataset("eventplanitem").getFilteredDataSet(filterMap));
                eventPlan.setEventPlanProperties(eventPlanData.getDataset("eventplanitemproperty").getFilteredDataSet(filterMap));
                DataSet eventplanconditions = eventPlanData.getDataset("eventplanitemcondition").getFilteredDataSet(filterMap);
                eventplanconditions.sort("conditionid");
                eventPlan.setEventPlanConditions(eventplanconditions);
                DataSet sdieventplan = eventPlanData.getDataset("sdieventplan");
                sdieventplan.clear();
                eventPlan.setSDIEventPlan(sdieventplan);
                DataSet sdieventplanitems = eventPlanData.getDataset("sdieventplanitem");
                sdieventplanitems.clear();
                eventPlan.setSDIEventPlanItems(sdieventplanitems);
                DataSet sdieventplanproperties = eventPlanData.getDataset("sdieventplanitemproperty");
                sdieventplanproperties.clear();
                eventPlan.setSDIEventPlanProperties(sdieventplanproperties);
                eventPlans.put(eventPlan.getEventplanid() + ";" + eventPlan.getEventplanversionid(), eventPlan);
                EventManager.analyzeEventPlan(sapphireConnection, eventPlan, globalPlans, instancePlans, globalSDCPlans, sdcHasPlans, eventTypes, requiresSupplementalData, eventTypeImplementations, logContext);
            }
            if (Trace.isDebugEnabled()) {
                Trace.logDebug(LOGGER, "Global Plans", logContext);
                Iterator it = globalPlans.iterator();
                while (it.hasNext()) {
                    Trace.logDebug(LOGGER, " - " + (String)it.next(), logContext);
                }
                Trace.logDebug(LOGGER, "Global SDC Plans", logContext);
                it = globalSDCPlans.iterator();
                while (it.hasNext()) {
                    Trace.logDebug(LOGGER, " - " + (String)it.next(), logContext);
                }
                Trace.logDebug(LOGGER, "Local Plans", logContext);
                it = instancePlans.iterator();
                while (it.hasNext()) {
                    Trace.logDebug(LOGGER, " - " + (String)it.next(), logContext);
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to load event plans for database '" + sapphireConnection.getDatabaseId() + "'. Reason: " + e.getMessage(), e, logContext);
        }
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPES, eventTypes);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS, eventPlans);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, GLOBALPLANS, globalPlans);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, GLOBALSDCPLANS, globalSDCPlans);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, HASPLANS, sdcHasPlans);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, REQUIRESSUPPLEMENTALDATA, requiresSupplementalData);
        CacheUtil.put(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPEIMPLEMENTATIONS, eventTypeImplementations);
    }

    private static void analyzeEventPlan(SapphireConnection sapphireConnection, EventPlan eventPlan, HashSet<String> globalPlans, ArrayList<String> instancePlans, HashSet<String> globalSDCPlans, HashSet<String> sdcHasPlans, LinkedHashMap<String, HashMap> eventTypes, HashMap<String, Object> requiresSupplementalData, HashMap<String, HashSet> eventTypeImplementations, LogContext logContext) {
        int j;
        DataSet eventplanitems;
        String eventplankey = eventPlan.getEventplanid() + ";" + eventPlan.getEventplanversionid();
        if (eventPlan.isGlobal()) {
            if (eventPlan.getSdcid().length() > 0) {
                globalSDCPlans.add(eventplankey);
                sdcHasPlans.add(eventPlan.getSdcid());
            } else {
                globalPlans.add(eventplankey);
                HashMap<String, String> sdcfilterMap = new HashMap<String, String>();
                sdcfilterMap.put("conditionitem", STATS_SDCID);
                DataSet sdcidconditions = eventPlan.getEventPlanConditions().getFilteredDataSet(sdcfilterMap);
                for (int j2 = 0; j2 < sdcidconditions.size(); ++j2) {
                    sdcHasPlans.add(sdcidconditions.getValue(j2, "value1"));
                }
                eventplanitems = eventPlan.getEventPlanItems();
                for (j = 0; j < eventplanitems.size(); ++j) {
                    HashMap eventType = eventTypes.get(eventplanitems.getValue(j, "eventtypeid"));
                    if (eventType == null) continue;
                    if (((String)eventType.get("eventtypesdcid")).length() > 0) {
                        sdcHasPlans.add((String)eventType.get("eventtypesdcid"));
                    }
                    if ((DataSet)eventType.get("setupconditions") == null) continue;
                    DataSet eventtypesetupconditions = (DataSet)eventType.get("setupconditions");
                    for (int k = 0; k < eventtypesetupconditions.size(); ++k) {
                        if (!eventtypesetupconditions.getValue(k, "conditionitem").equals(STATS_SDCID)) continue;
                        sdcHasPlans.add(eventtypesetupconditions.getValue(k, "value1"));
                    }
                }
            }
        } else {
            instancePlans.add(eventplankey);
            sdcHasPlans.add(eventPlan.getSdcid());
        }
        HashMap<String, ArrayList<Integer>> eventObjectPlanEvents = new HashMap<String, ArrayList<Integer>>();
        HashMap<Integer, String> eventEventSource = new HashMap<Integer, String>();
        eventplanitems = eventPlan.getEventPlanItems();
        for (j = 0; j < eventplanitems.size(); ++j) {
            String eventtypeid;
            if (eventplanitems.getValue(j, "itemtypeflag").equals("P")) {
                eventPlan.setEventPlanEventPlanItemId(eventplanitems.getValue(j, "eventplanitemid"));
                continue;
            }
            if (!eventplanitems.getValue(j, "itemtypeflag").equals("E") || (eventtypeid = eventplanitems.getValue(j, "eventtypeid")).length() <= 0) continue;
            boolean missingSetupConditions = false;
            HashMap eventTypeDetails = eventTypes.get(eventtypeid);
            try {
                BaseEventType eventType = (BaseEventType)Class.forName((String)eventTypeDetails.get("objectname")).newInstance();
                EventTypeSetter.startEvent(eventType, sapphireConnection, LOGGER);
                DataSet eventPlanConditions = eventPlan.getEventPlanConditions(eventplanitems.getValue(j, "eventplanitemid"));
                Condition[] conditions = eventType.getSetupConditions((DataSet)eventTypeDetails.get("setupconditions"));
                if (conditions != null) {
                    if (eventPlanConditions.size() >= conditions.length) {
                        for (int k = 0; !missingSetupConditions && k < conditions.length; ++k) {
                            if (eventPlanConditions.getValue(k, "operator1").length() != 0 && (eventPlanConditions.getValue(k, "value1").length() != 0 || eventPlanConditions.getValue(k, "value2").length() != 0)) continue;
                            missingSetupConditions = true;
                        }
                    } else {
                        missingSetupConditions = true;
                    }
                }
                if (!missingSetupConditions) {
                    DataSet conditionValues;
                    int row;
                    HashSet implementations = eventTypeImplementations.get(eventtypeid);
                    if (implementations != null) {
                        for (String eventObjectClass : implementations) {
                            ArrayList<Integer> eventItemIndexes = eventObjectPlanEvents.get(eventObjectClass);
                            if (eventItemIndexes == null) {
                                eventItemIndexes = new ArrayList();
                                eventObjectPlanEvents.put(eventObjectClass, eventItemIndexes);
                            }
                            eventItemIndexes.add(j);
                            if (!eventType.requiresSupplementalData(eventPlanConditions)) continue;
                            String[] requestItems = (String[])requiresSupplementalData.get(eventObjectClass);
                            if (eventType instanceof BaseSDCEventType) {
                                String[] eventTypeRequestItems = ((BaseSDCEventType)eventType).getSupplementalRequestItems(eventPlanConditions);
                                if (requestItems == null) {
                                    requestItems = eventTypeRequestItems;
                                } else {
                                    List<String> list = Arrays.asList(requestItems);
                                    for (int k = 0; k < eventTypeRequestItems.length; ++k) {
                                        if (list.contains(eventTypeRequestItems[k])) continue;
                                        list.add(eventTypeRequestItems[k]);
                                    }
                                    requestItems = list.toArray(new String[list.size()]);
                                }
                            }
                            requiresSupplementalData.put(eventObjectClass, requestItems);
                        }
                    }
                    if ((row = (conditionValues = BaseEventType.getConditionValues((DataSet)eventTypeDetails.get("setupconditions"), eventPlanConditions, false)).findRow("conditionitem", "eventsource")) >= 0 && (conditionValues.getValue(row, "value1").length() > 0 || conditionValues.getValue(row, "value2").length() > 0)) {
                        if (conditionValues.getValue(row, "value1").length() > 0) {
                            eventEventSource.put(j, conditionValues.getValue(row, "value1"));
                        } else {
                            eventEventSource.put(j, conditionValues.getValue(row, "value2"));
                        }
                    }
                } else {
                    Trace.logError(LOGGER, (Object)("Missing setup conditions for event item '" + eventplanitems.getValue(j, STATS_EVENTPLANITEMDESC) + "', eventtypeid '" + eventtypeid + "' in event plan '" + eventPlan.getEventplanid() + "'. Setting event fire status to Disabled."), logContext);
                    eventplanitems.setValue(j, "eventfireflag", "D");
                }
                EventTypeSetter.endEvent(eventType);
                continue;
            }
            catch (Exception e) {
                Trace.logError(LOGGER, "Error creating event type for eventtypeid '" + eventtypeid + "' in event plan '" + eventPlan.getEventplanid() + "'. Reason: " + e.getMessage(), e, logContext);
            }
        }
        eventPlan.setEventObjectPlanEvents(eventObjectPlanEvents);
        eventPlan.setEventEventSource(eventEventSource);
    }

    public static boolean requiresSupplementalData(SapphireConnection sapphireConnection, ErrorHandler errorHandler, BaseEventObject eventObject) {
        EventManager.checkEventPlansLoaded(sapphireConnection);
        if (eventObject instanceof BaseSDCEventObject) {
            BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
            HashSet sdcHasPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, HASPLANS);
            HashMap requiresSupplementalData = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, REQUIRESSUPPLEMENTALDATA);
            String sdcid = sdcEventObject.getSdcid();
            if (sdcHasPlans != null && sdcHasPlans.contains(sdcid) && requiresSupplementalData != null && requiresSupplementalData.containsKey(eventObject.getClass().getName())) {
                sdcEventObject.setSupplementalRequestItems((String[])requiresSupplementalData.get(eventObject.getClass().getName()));
                Trace.logDebug(LOGGER, "Supplemental data required for " + sdcid + " event object with " + eventObject.getClass().getName() + " event object", new LogContext(sapphireConnection.getConnectionId()));
                return true;
            }
            return false;
        }
        return false;
    }

    private static void defineWorkflowEventPlans(SapphireConnection sapphireConnection) {
        try {
            QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
            DataSet workflowTaskEvents = queryProcessor.getSqlDataSet("SELECT taskdefio.taskdefid, taskdefio.taskdefversionid, taskdefio.taskdefvariantid, taskdefio.ioid, taskdef.autoexecflag, connectortype.connectortypesdcid,        workflowdeftaskio.workflowdefid, workflowdeftaskio.workflowdefversionid,  workflowdeftaskio.workflowdefvariantid, workflowdeftaskio.taskdefitemid FROM   workflowdeftaskio, taskdefio, taskdef, connectortype WHERE  workflowdeftaskio.taskdefid = taskdefio.taskdefid AND    workflowdeftaskio.taskdefversionid = taskdefio.taskdefversionid AND    workflowdeftaskio.taskdefvariantid = taskdefio.taskdefvariantid AND    workflowdeftaskio.ioid = taskdefio.ioid AND    taskdefio.taskdefid = taskdef.taskdefid AND    taskdefio.taskdefversionid = taskdef.taskdefversionid AND    taskdefio.taskdefvariantid = taskdef.taskdefvariantid AND    taskdefio.waittype = 'event' AND    taskdefio.connectortypeid = connectortype.connectortypeid AND    EXISTS (            SELECT workflowexecid FROM workflowexec            WHERE workflowexec.workflowdefid = workflowdeftaskio.workflowdefid            AND workflowexec.workflowdefversionid = workflowdeftaskio.workflowdefversionid            AND workflowexec.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid            AND workflowexec.execstatus = 'A' ) ORDER BY taskdefio.taskdefid, taskdefio.taskdefversionid, taskdefio.taskdefvariantid");
            ArrayList<Event> events = new ArrayList<Event>();
            for (int i = 0; i < workflowTaskEvents.size(); ++i) {
                String workflowdefid = workflowTaskEvents.getValue(i, "workflowdefid");
                String workflowdefversionid = workflowTaskEvents.getValue(i, "workflowdefversionid");
                String workflowdefvariantid = workflowTaskEvents.getValue(i, "workflowdefvariantid");
                String taskdefitemid = workflowTaskEvents.getValue(i, "taskdefitemid");
                String taskdefid = workflowTaskEvents.getValue(i, "taskdefid");
                String taskdefversionid = workflowTaskEvents.getValue(i, "taskdefversionid");
                String taskdefvariantid = workflowTaskEvents.getValue(i, "taskdefvariantid");
                String ioid = workflowTaskEvents.getValue(i, "ioid");
                String connectortypesdcid = workflowTaskEvents.getValue(i, "connectortypesdcid");
                String autoexecflag = workflowTaskEvents.getValue(i, "autoexecflag", "N");
                try {
                    TaskDef taskDef = TaskDef.getInstance(sapphireConnection, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid);
                    PropertyListCollection variables = taskDef.getTaskdef().getCollection("variables");
                    JSONableMap taskVariables = taskDef.setupTaskVariables(variables, "", null, queryProcessor, sapphireConnection);
                    PropertyListCollection taskio = taskDef.getTaskdef().getCollection("taskio");
                    for (int j = 0; j < taskio.size(); ++j) {
                        PropertyList event;
                        PropertyList io = taskio.getPropertyList(j);
                        if (!io.getProperty("ioid").equals(ioid) || !io.getProperty("waittype").equals("event") || (event = io.getPropertyList("event")) == null || event.getProperty("eventtypeid").length() <= 0) continue;
                        Event waitEvent = new Event(LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid) + " - " + taskdefitemid + " - " + ioid, event.getProperty("eventtypeid"));
                        PropertyListCollection conditions = event.getCollection("conditions");
                        if (conditions != null) {
                            for (int k = 0; k < conditions.size(); ++k) {
                                PropertyList condition = conditions.getPropertyList(k);
                                waitEvent.addCondition(condition.getProperty("conditionitem"), condition.getProperty("operator1"), WorkflowManager.resolveTokens(condition.getProperty("value1"), taskVariables, variables, null, sapphireConnection));
                            }
                        }
                        waitEvent.setFunction("B", "<actionblock>   <action name=\"action0\" actionclass=\"" + TaskInputEvent.class.getName() + "\">\n       <property id=\"workflowdefid\">" + workflowdefid + "</property>\n       <property id=\"workflowdefversionid\">" + workflowdefversionid + "</property>\n       <property id=\"workflowdefvariantid\">" + workflowdefvariantid + "</property>\n       <property id=\"taskdefitemid\">" + taskdefitemid + "</property>\n       <property id=\"taskdefid\">" + taskdefid + "</property>\n       <property id=\"taskdefversionid\">" + taskdefversionid + "</property>\n       <property id=\"taskdefvariantid\">" + taskdefvariantid + "</property>\n       <property id=\"ioid\">" + ioid + "</property>\n       <property id=\"autoexec\">" + autoexecflag + "</property>\n       <property id=\"sdcid\">[__sdcid]</property>\n       <property id=\"keyid1\">[__keyid1]</property>\n       <property id=\"keyid2\">[__keyid2]</property>\n       <property id=\"keyid3\">[__keyid3]</property>\n       <property id=\"workitemid\">[__workitemid]</property>\n       <property id=\"workitemversionid\">[__workitemversionid]</property>\n       <property id=\"workiteminstance\">[__workiteminstance]</property>\n       <property id=\"eventid\">" + waitEvent.getEventid() + "</property>\n       <property id=\"eventtypeid\">" + waitEvent.getEventtypeid() + "</property>\n       <property id=\"connectortypesdcid\">" + connectortypesdcid + "</property>\n   </action>\n</actionblock>", false);
                        events.add(waitEvent);
                    }
                    continue;
                }
                catch (Exception e) {
                    Trace.logError(LOGGER, (Object)("Failed to add event to event plan for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid)), e);
                }
            }
            if (events.size() > 0) {
                EventManager.addTransientPlan(sapphireConnection, WORKFLOW_EVENTPLAN, "", events);
            }
        }
        catch (Exception e) {
            Trace.logError(LOGGER, (Object)"Failed to define event plans for workflows", e);
        }
    }

    public static boolean hasWorkflowEventPlan(SapphireConnection sapphireConnection, String workflowexecid, boolean delete) {
        QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        DataSet workflowtaskevents = queryProcessor.getPreparedSqlDataSet("SELECT workflowdeftaskio.workflowdefid, workflowdeftaskio.workflowdefversionid,  workflowdeftaskio.workflowdefvariantid, workflowdeftaskio.taskdefitemid, workflowdeftaskio.ioid,        taskdef.taskdefid, taskdef.taskdefversionid, taskdef.taskdefvariantid FROM   workflowexec, workflowdeftaskio, taskdefio, taskdef WHERE  workflowexec.workflowexecid = " + safeSQL.addVar(workflowexecid) + " AND    workflowexec.execstatus = " + safeSQL.addVar("A") + " AND    workflowexec.workflowdefid = workflowdeftaskio.workflowdefid AND    workflowexec.workflowdefversionid = workflowdeftaskio.workflowdefversionid AND    workflowexec.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid AND    workflowdeftaskio.taskdefid = taskdefio.taskdefid AND    workflowdeftaskio.taskdefversionid = taskdefio.taskdefversionid AND    workflowdeftaskio.taskdefvariantid = taskdefio.taskdefvariantid AND    workflowdeftaskio.ioid = taskdefio.ioid AND    taskdefio.taskdefid = taskdef.taskdefid AND    taskdefio.taskdefversionid = taskdef.taskdefversionid AND    taskdefio.taskdefvariantid = taskdef.taskdefvariantid AND    taskdefio.waittype = 'event' ", safeSQL.getValues());
        if (workflowtaskevents != null && workflowtaskevents.size() > 0) {
            HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
            EventPlan eventPlan = (EventPlan)eventPlans.get("__WorkflowEvents;U");
            if (eventPlan == null) {
                return false;
            }
            if (delete) {
                return false;
            }
            for (int i = 0; i < workflowtaskevents.size(); ++i) {
                try {
                    DataSet eventplanitems = eventPlan.getEventPlanItems();
                    if (eventplanitems == null) continue;
                    int row = eventplanitems.findRow(STATS_EVENTPLANITEMDESC, LV_WorkflowDef.getText(workflowtaskevents.getValue(i, "workflowdefid"), workflowtaskevents.getValue(i, "workflowdefversionid"), workflowtaskevents.getValue(i, "workflowdefvariantid")) + " - " + workflowtaskevents.getValue(i, "taskdefitemid") + " - " + workflowtaskevents.getValue(i, "ioid"));
                    if (row >= 0) {
                        TaskDef taskDef = TaskDef.getInstance(sapphireConnection, workflowtaskevents.getValue(i, "workflowdefid"), workflowtaskevents.getValue(i, "workflowdefversionid"), workflowtaskevents.getValue(i, "workflowdefvariantid"), workflowtaskevents.getValue(i, "taskdefitemid"));
                        PropertyListCollection variables = taskDef.getTaskdef().getCollection("variables");
                        JSONableMap taskVariables = taskDef.setupTaskVariables(variables, "", null, queryProcessor, sapphireConnection);
                        PropertyListCollection taskio = taskDef.getTaskdef().getCollection("taskio");
                        PropertyList io = taskio.find("ioid", workflowtaskevents.getValue(i, "ioid"));
                        if (io != null && io.getProperty("waittype").equals("event")) {
                            PropertyList event = io.getPropertyList("event");
                            if (event != null && event.getProperty("eventtypeid").length() > 0) {
                                PropertyListCollection taskeventconditions = event.getCollection("conditions");
                                DataSet eventplanconditions = eventPlan.getEventPlanConditions(eventplanitems.getValue(row, "eventplanitemid"));
                                if (taskeventconditions != null) {
                                    for (int j = 0; j < taskeventconditions.size(); ++j) {
                                        PropertyList taskeventcondition = taskeventconditions.getPropertyList(j);
                                        HashMap<String, String> findMap = new HashMap<String, String>();
                                        findMap.put("conditionitem", taskeventcondition.getProperty("conditionitem"));
                                        findMap.put("operator1", taskeventcondition.getProperty("operator1"));
                                        findMap.put("value1", WorkflowManager.resolveTokens(taskeventcondition.getProperty("value1"), taskVariables, variables, null, sapphireConnection));
                                        if (taskeventcondition.getProperty("value2").length() > 0) {
                                            findMap.put("vaule2", WorkflowManager.resolveTokens(taskeventcondition.getProperty("value2"), taskVariables, variables, null, sapphireConnection));
                                        }
                                        if (eventplanconditions.findRow(findMap) != -1) continue;
                                        return false;
                                    }
                                    continue;
                                }
                                return eventplanconditions == null;
                            }
                            return false;
                        }
                        return false;
                    }
                    return false;
                }
                catch (SapphireException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void defineNotificationEventPlans(SapphireConnection sapphireConnection) {
        try {
            QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
            DataSet users = queryProcessor.getSqlDataSet("SELECT sysuser.sysuserid FROM   sysuser WHERE  ( sysuser.activeflag IS NULL OR sysuser.activeflag = 'Y' ) ORDER BY sysuser.sysuserid");
            for (int i = 0; i < users.size(); ++i) {
                String sysuserid = users.getValue(i, "sysuserid");
                try {
                    EventManager.removeTransientPlan(sapphireConnection, "__subscriber_" + sysuserid);
                    PropertyList userNotificationProps = new PropertyList();
                    File file = new File(Configuration.getInstance().getSapphireHome(), "notifications" + File.separator + sysuserid + ".xml");
                    if (!file.exists()) continue;
                    userNotificationProps.setPropertyList(FileUtil.getFileString(file));
                    ArrayList<Event> events = new ArrayList<Event>();
                    PropertyListCollection notifications = userNotificationProps.getCollection("notifications");
                    for (int j = 0; j < notifications.size(); ++j) {
                        PropertyList notification = notifications.getPropertyList(j);
                        Event event = new Event(notification.getProperty("eventtypeid"), notification.getProperty("eventtypeid"));
                        PropertyListCollection conditions = notification.getCollection("conditions");
                        if (conditions != null) {
                            for (int k = 0; k < conditions.size(); ++k) {
                                PropertyList condition = conditions.getPropertyList(k);
                                event.addCondition(condition.getProperty("conditionitem"), condition.getProperty("operator1"), condition.getProperty("value1"));
                            }
                        }
                        event.setFunction("B", "<actionblock>   <action name=\"action0\" actionclass=\"" + Notify.class.getName() + "\">\n       <property id=\"" + "subscriberid" + "\">" + sysuserid + "</property>\n       <property id=\"" + "type" + "\">" + notification.getProperty("type", "T").charAt(0) + "</property>\n       <property id=\"" + "expiry" + "\">" + notification.getProperty("expiry", "L").charAt(0) + "</property>\n       <property id=\"" + "response" + "\">" + notification.getProperty("response", "N").charAt(0) + "</property>\n       <property id=\"" + "message" + "\">" + notification.getProperty("message") + "</property>\n       <property id=\"" + "link" + "\">" + notification.getProperty("link") + "</property>\n       <property id=\"eventtypeid\">" + notification.getProperty("eventtypeid") + "</property>\n   </action>\n</actionblock>", false);
                        events.add(event);
                    }
                    EventManager.addTransientPlan(sapphireConnection, "__subscriber_" + sysuserid, "", events);
                    continue;
                }
                catch (Exception e) {
                    Trace.logError(LOGGER, (Object)("Failed to define event plan for notifications for user '" + sysuserid + "'. Reason: " + e.getMessage()), e);
                }
            }
            WebAdminProcessor webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
            ArrayList<Event> events = new ArrayList<Event>();
            DataSet gizmodefs = queryProcessor.getSqlDataSet("SELECT gizmodefid, propertytreeid, extendnodeid, productvaluetree, valuetree FROM GIZMODEF ORDER BY gizmodefid", true);
            for (int i = 0; i < gizmodefs.size(); ++i) {
                PropertyList gizmoProps;
                String gizmodefid = gizmodefs.getValue(i, "gizmodefid");
                String propertytreeid = gizmodefs.getValue(i, "propertytreeid");
                String extendnodeid = gizmodefs.getValue(i, "extendnodeid");
                if (gizmodefid.length() <= 0 || propertytreeid.length() <= 0 || extendnodeid.length() <= 0 || (gizmoProps = BaseGizmo.getGizmoDefProperties(gizmodefid, propertytreeid, extendnodeid, gizmodefs.getValue(i, "productvaluetree"), gizmodefs.getValue(i, "valuetree"), webAdminProcessor, sapphireConnection)) == null) continue;
                gizmoProps = BaseGizmo.applyRootProperties(gizmoProps, propertytreeid, sapphireConnection);
                try {
                    boolean enableNotifications = "Y".equals(gizmoProps.getPropertyList("gizmoprops").getProperty("enablenotifications"));
                    String notificationsProp = gizmoProps.getPropertyList("gizmoprops").getProperty("notifications");
                    if (!enableNotifications || notificationsProp.length() <= 0) continue;
                    PropertyList notificationProps = new PropertyList();
                    notificationProps.setPropertyList(notificationsProp);
                    PropertyListCollection notifications = notificationProps.getCollection("notifications");
                    for (int j = 0; j < notifications.size(); ++j) {
                        PropertyList notification = notifications.getPropertyList(j);
                        Event event = new Event(gizmodefid + "_" + notification.getProperty("eventtypeid"), notification.getProperty("eventtypeid"));
                        PropertyListCollection conditions = notification.getCollection("conditions");
                        if (conditions != null) {
                            for (int k = 0; k < conditions.size(); ++k) {
                                PropertyList condition = conditions.getPropertyList(k);
                                event.addCondition(condition.getProperty("conditionitem"), condition.getProperty("operator1"), condition.getProperty("value1"));
                            }
                        }
                        event.setFunction("B", "<actionblock>   <action name=\"action0\" actionclass=\"" + Notify.class.getName() + "\">\n       <property id=\"" + "subscriberid" + "\">" + "(system)" + "</property>\n       <property id=\"" + "elementtype" + "\">" + propertytreeid + "</property>\n       <property id=\"" + "elementid" + "\">" + gizmodefid + "</property>\n       <property id=\"" + "type" + "\">" + "E" + "</property>\n       <property id=\"" + "message" + "\">" + notification.getProperty("message") + "</property>\n       <property id=\"usercontext\">" + notification.getProperty("usercontexttoken") + "</property>\n       <property id=\"eventtypeid\">" + notification.getProperty("eventtypeid") + "</property>\n   </action>\n</actionblock>", true);
                        events.add(event);
                    }
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (events.size() > 0) {
                String applicationid = Configuration.getInstance().getApplicationid();
                EventManager.removeTransientPlan(sapphireConnection, "__application_" + applicationid);
                EventManager.addTransientPlan(sapphireConnection, "__application_" + applicationid, "", events);
            }
        }
        catch (Exception e) {
            Trace.logError(LOGGER, (Object)"Failed to define event plans for notifications", e);
        }
    }

    public static void addTransientPlan(SapphireConnection sapphireConnection, String eventplanid, String eventplandesc, ArrayList<Event> events) {
        LinkedHashMap eventTypes = (LinkedHashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPES);
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        HashSet globalPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALPLANS);
        HashSet globalSDCPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALSDCPLANS);
        HashSet sdcHasPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, HASPLANS);
        HashMap eventTypeImplementations = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPEIMPLEMENTATIONS);
        HashMap requiresSupplementalData = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, REQUIRESSUPPLEMENTALDATA);
        ArrayList<String> instancePlans = new ArrayList<String>();
        EventPlan eventPlan = (EventPlan)eventPlans.get(eventplanid + ";" + "U");
        if (eventPlan == null) {
            eventPlan = new EventPlan(eventplanid, eventplandesc);
            eventPlans.put(eventPlan.getEventPlanKey(), eventPlan);
        }
        if (events != null) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);
                if (eventPlan.hasEvent(event.getEventid())) continue;
                eventPlan.addEvent(event.getEventtypeid(), event.getEventid(), event.getConditions(), event.getScripttype(), event.getScript(), event.isProcessAsync());
            }
            EventManager.analyzeEventPlan(sapphireConnection, eventPlan, globalPlans, instancePlans, globalSDCPlans, sdcHasPlans, eventTypes, requiresSupplementalData, eventTypeImplementations, new LogContext(sapphireConnection.getConnectionId()));
        }
    }

    public static void removeTransientPlan(SapphireConnection sapphireConnection, String eventplanid) {
        LinkedHashMap eventTypes = (LinkedHashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPES);
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        HashSet globalPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALPLANS);
        HashSet globalSDCPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALSDCPLANS);
        HashSet sdcHasPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, HASPLANS);
        EventPlan eventPlan = (EventPlan)eventPlans.get(eventplanid + ";" + "U");
        if (eventPlan != null) {
            String eventplankey = eventPlan.getEventplanid() + ";" + eventPlan.getEventplanversionid();
            eventPlans.remove(eventplankey);
            globalPlans.remove(eventplankey);
            globalSDCPlans.remove(eventplankey);
            if (eventPlan.getSdcid().length() > 0) {
                // empty if block
            }
        }
    }

    public static ArrayList<DataSet> getEventStats() {
        return eventStatsList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean generateEvent(SapphireConnection sapphireConnection, ErrorHandler errorHandler, BaseEventObject eventObject) throws SapphireException {
        while (loading) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {}
        }
        long start = System.currentTimeMillis();
        String eventid = "EID:" + start;
        StringBuffer eventDebugLog = new StringBuffer();
        EventManager.updateEventLog(eventDebugLog, 0L, "Event debug log for " + eventid);
        EventManager.updateEventLog(eventDebugLog, 0L, "Received event:\t" + eventObject.getEventid() + "(" + eventObject.getClass().getSimpleName() + ")");
        LogContext logContext = new LogContext(sapphireConnection.getConnectionId());
        Trace.logDebug(LOGGER, eventObject.getEventid() + " event generated (" + eventid + ")", logContext);
        boolean evaluate = true;
        Trace.setStartCodeBlock("GenerateEvent");
        EventManager.checkEventPlansLoaded(sapphireConnection);
        DataSet eventStats = null;
        if (statsLogging) {
            eventStats = new DataSet();
            ArrayList<DataSet> arrayList = eventStatsList;
            synchronized (arrayList) {
                eventStatsList.add(eventStats);
                if (eventStatsList.size() > maxEventStats) {
                    eventStatsList.remove(0);
                }
            }
            eventStats.addRow();
            eventStats.setString(0, STATS_EID, eventid);
            eventStats.setString(0, STATS_CONNECTIONID, sapphireConnection.getConnectionId());
            eventStats.setDate(0, STATS_EVENTDT, Calendar.getInstance());
            eventStats.setString(0, STATS_EVENTID, eventObject.getEventid());
        }
        if (eventObject instanceof BaseSDCEventObject) {
            evaluate = EventManager.checkSDCEventObject(sapphireConnection, eventObject, logContext, eventDebugLog, start, eventStats);
        }
        if (evaluate) {
            int eventItemCount = eventObject.addEventItems();
            eventObject.copyEventItems();
            Trace.logDebug(LOGGER, eventItemCount + " event items found", logContext);
            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, eventItemCount + " event items found");
            boolean bl = evaluate = eventItemCount > 0;
            if (statsLogging) {
                eventStats.setNumber(0, STATS_EVENTITEMCOUNT, eventItemCount);
                if (eventItemCount > 0) {
                    ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
                    StringBuffer eventItemHTML = new StringBuffer();
                    for (int i = 0; i < eventItems.size(); ++i) {
                        eventItemHTML.append(eventItems.get(i).toString()).append("\n");
                    }
                    eventStats.setString(0, STATS_EVENTITEMS, eventItemHTML.toString());
                } else {
                    eventStats.setString(0, STATS_STATUS, STATS_STATUS_IGNORED);
                    eventStats.setString(0, STATS_STATUSMESSAGE, "No event items in event object");
                }
            }
        }
        if (evaluate) {
            ArrayList<String> executionPlans = EventManager.getExecutionEventPlans(sapphireConnection, eventObject, logContext, eventDebugLog, start);
            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, executionPlans.size() + " event plans found for execution");
            if (statsLogging) {
                HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
                eventStats.setNumber(0, STATS_EVENTPLANCOUNT, executionPlans.size());
                for (int i = 0; i < executionPlans.size(); ++i) {
                    int statsRow = 0;
                    if (i > 0) {
                        statsRow = eventStats.addRow();
                    }
                    eventStats.setString(statsRow, STATS_EVENTPLANID, executionPlans.get(i));
                    EventPlan eventPlan = (EventPlan)eventPlans.get(executionPlans.get(i));
                    ArrayList<Integer> eventObjectPlanEvents = eventPlan.getEventObjectPlanEvents(eventObject.getClass().getName());
                    if (eventObjectPlanEvents == null || eventObjectPlanEvents.size() <= 0) continue;
                    for (int j = 0; j < eventObjectPlanEvents.size(); ++j) {
                        if (j <= 0) continue;
                        statsRow = eventStats.addRow();
                    }
                }
            }
            if (executionPlans.size() > 0) {
                Trace.logDebug(LOGGER, "Evaluating " + executionPlans.size() + " plans", logContext);
                evaluate = EventManager.evaluatePlans(sapphireConnection, executionPlans, errorHandler, eventObject, logContext, eventDebugLog, start, eventStats);
            }
        }
        if (evaluate) {
            Trace.setEndCodeBlock("GenerateEvent", "EventGenerated - FIRED");
        } else {
            Trace.setEndCodeBlock("GenerateEvent", "EventGenerated - NO FIRE");
        }
        if (eventLogging) {
            Trace.logDebug(LOGGER, eventDebugLog.toString(), logContext);
        }
        return evaluate;
    }

    private static boolean checkSDCEventObject(SapphireConnection sapphireConnection, BaseEventObject eventObject, LogContext logContext, StringBuffer eventDebugLog, long start, DataSet eventStats) {
        HashSet sdcHasPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, HASPLANS);
        BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
        String sdcid = sdcEventObject.getSdcid();
        if (statsLogging) {
            eventStats.setString(0, STATS_SDCID, sdcid);
        }
        if (sdcEventObject.getProperties().getProperty("suppresseventgeneration", "N").equals("Y")) {
            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "  Ignoring event object:\tSuppress event generation flag set in properties");
            if (statsLogging) {
                eventStats.setString(0, STATS_STATUS, STATS_STATUS_IGNORED);
                eventStats.setString(0, STATS_STATUSMESSAGE, "Suppress event generation flag set in properties");
            }
            return false;
        }
        if (sdcHasPlans.contains(sdcid)) {
            if (sdcEventObject.getSdcProps() == null) {
                Trace.logDebug(LOGGER, "Adding sdc properties: " + sdcid, logContext);
                SDCProcessor sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
                PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
                sdcEventObject.setSdcProps(sdcProps);
            }
            if (statsLogging) {
                eventStats.setString(0, STATS_STATUS, STATS_STATUS_HASSDCPLAN);
            }
            return true;
        }
        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "  Ignoring event object:\t" + sdcid + " SDC has no event plans");
        if (statsLogging) {
            eventStats.setString(0, STATS_STATUS, STATS_STATUS_IGNORED);
            eventStats.setString(0, STATS_STATUSMESSAGE, "SDC has no event plans");
        }
        return false;
    }

    private static ArrayList<String> getExecutionEventPlans(SapphireConnection sapphireConnection, BaseEventObject eventObject, LogContext logContext, StringBuffer eventDebugLog, long start) {
        ArrayList<String> executionPlans = new ArrayList<String>();
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        HashSet globalPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALPLANS);
        HashSet globalSDCPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALSDCPLANS);
        for (String eventplankey : globalPlans) {
            if (!((EventPlan)eventPlans.get(eventplankey)).isActive()) continue;
            executionPlans.add(eventplankey);
        }
        if (eventObject instanceof BaseSDCEventObject) {
            BaseSDCEventObject sdcEventObject = (BaseSDCEventObject)eventObject;
            String sdcid = sdcEventObject.getSdcid();
            for (String eventplankey : globalSDCPlans) {
                if (!((EventPlan)eventPlans.get(eventplankey)).isActive() || !((EventPlan)eventPlans.get(eventplankey)).getSdcid().equals(sdcid)) continue;
                executionPlans.add(eventplankey);
            }
            DataSet sdieventplan = sdcEventObject.getSDIEventPlanData(sapphireConnection);
            for (int i = 0; i < sdieventplan.size(); ++i) {
                if (!sdieventplan.getValue(i, STATS_SDCID).equals(sdieventplan.getValue(i, "eventplansdcid"))) continue;
                sdcEventObject.addSDIEventPlan(sdieventplan.getValue(i, STATS_EVENTPLANID), sdieventplan.getValue(i, "eventplanversionid"), sdieventplan.getValue(i, "keyid1"), sdieventplan.getValue(i, "keyid2"), sdieventplan.getValue(i, "keyid3"));
                String eventplankey = sdieventplan.getValue(i, STATS_EVENTPLANID) + ";" + sdieventplan.getValue(i, "eventplanversionid");
                if (eventPlans.get(eventplankey) == null || !((EventPlan)eventPlans.get(eventplankey)).isActive() || executionPlans.contains(eventplankey)) continue;
                executionPlans.add(eventplankey);
            }
        }
        return executionPlans;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean evaluatePlans(SapphireConnection sapphireConnection, ArrayList<String> executionPlans, ErrorHandler errorHandler, BaseEventObject eventObject, LogContext logContext, StringBuffer eventDebugLog, long start, DataSet eventStats) throws SapphireException {
        boolean eventFired = false;
        LinkedHashMap eventTypes = (LinkedHashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTTYPES);
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        DataSet eventPlanHistory = new DataSet();
        try {
            int statsRow = 0;
            for (int i = 0; i < executionPlans.size(); ++i) {
                if (i > 0) {
                    ++statsRow;
                }
                EventPlan eventPlan = (EventPlan)eventPlans.get(executionPlans.get(i));
                if (statsLogging) {
                    eventPlan.evaluate(eventPlan.getEventPlanEventPlanItemId(), eventObject, sapphireConnection.getConnectionId());
                }
                eventObject.setCurrentEventPlan(eventPlan);
                Trace.logDebug(LOGGER, "Evaluating EVENTPLAN: " + eventPlan.getEventplanid(), logContext);
                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "Evaluating EVENTPLAN:\t" + eventPlan.getEventplanid() + " (" + (eventPlan.isGlobal() ? "GLOBAL" : "LOCAL") + ")");
                DataSet eventplanitems = eventPlan.getEventPlanItems();
                ArrayList<Integer> eventObjectPlanEvents = eventPlan.getEventObjectPlanEvents(eventObject.getClass().getName());
                HashMap<Integer, String> eventEventSource = eventPlan.getEventEventSource();
                if (eventObjectPlanEvents != null && eventObjectPlanEvents.size() > 0) {
                    for (int j = 0; j < eventObjectPlanEvents.size(); ++j) {
                        if (j > 0) {
                            ++statsRow;
                        }
                        int eventIndex = eventObjectPlanEvents.get(j);
                        String eventplanitemid = eventplanitems.getValue(eventIndex, "eventplanitemid");
                        String eventplandesc = eventplanitems.getValue(eventIndex, STATS_EVENTPLANITEMDESC);
                        String eventfireflag = eventplanitems.getValue(eventIndex, "eventfireflag", "A");
                        if (statsLogging) {
                            eventStats.setString(statsRow, STATS_EVENTPLANITEMDESC, eventplandesc);
                            eventPlan.evaluate(eventplanitemid, eventObject, sapphireConnection.getConnectionId());
                        }
                        Trace.logDebug(LOGGER, "Evaluating EVENT: " + eventplandesc, logContext);
                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "    Evaluating EVENT:\t" + eventplandesc);
                        if (!eventfireflag.equals("D")) {
                            boolean excludeEvent = false;
                            String eventtypeid = eventplanitems.getValue(eventIndex, "eventtypeid");
                            HashMap eventTypeDetails = (HashMap)eventTypes.get(eventtypeid);
                            BaseEventType eventType = EventManager.getEventTypeInstance(eventtypeid, (String)eventTypeDetails.get("objectname"));
                            if (eventEventSource != null && eventEventSource.containsKey(eventIndex) && !eventObject.getEventid().equals(eventEventSource.get(eventIndex))) {
                                excludeEvent = true;
                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Ignoring event:\tEventSource condition does not match " + eventObject.getEventid() + " event object");
                                if (statsLogging) {
                                    eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_IGNORED);
                                    eventStats.setString(statsRow, STATS_STATUSMESSAGE, "EventSource condition does not match");
                                }
                            }
                            if (eventType instanceof BaseSDCEventType && eventObject instanceof BaseSDCEventObject) {
                                if (((BaseSDCEventType)eventType).supportsSdcid(((BaseSDCEventObject)eventObject).getSdcid())) {
                                    ((BaseSDCEventType)eventType).setSdcProps(((BaseSDCEventObject)eventObject).getSdcProps());
                                } else {
                                    excludeEvent = true;
                                    EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Ignoring event:\t" + eventtypeid + " does not support SDC " + ((BaseSDCEventObject)eventObject).getSdcid());
                                    if (statsLogging) {
                                        eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_IGNORED);
                                        eventStats.setString(statsRow, STATS_STATUSMESSAGE, eventtypeid + " does not support SDC ");
                                    }
                                }
                            }
                            if (excludeEvent) continue;
                            try {
                                EventTypeSetter.startEvent(eventType, sapphireConnection, eventtypeid, errorHandler);
                                eventObject.resetEventItems();
                                DataSet conditionValues = BaseEventType.getConditionValues((DataSet)eventTypeDetails.get("setupconditions"), eventPlan.getEventPlanConditions(eventplanitemid), true);
                                Trace.setStartCodeBlock("EventFiredTest - " + eventtypeid);
                                if (eventType.hasEventFired(eventObject, conditionValues)) {
                                    HashMap logEvent;
                                    ArrayList logEvents;
                                    StringBuffer eventLog;
                                    ArrayList<BaseEventItem> eventItems;
                                    int k;
                                    ArrayList<BaseEventItem> firedEventItems;
                                    if (statsLogging) {
                                        eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_FIRED);
                                        eventPlan.fired(eventplanitemid, eventObject);
                                        ArrayList<BaseEventItem> eventItems2 = eventObject.getEventItems();
                                        StringBuffer eventItemHTML = new StringBuffer();
                                        for (int k2 = 0; k2 < eventItems2.size(); ++k2) {
                                            eventItemHTML.append(eventItems2.get(k2).toString()).append(eventItems2.get(k2).hasFired() ? " FIRED\n" : "\n");
                                        }
                                        eventStats.setString(statsRow, STATS_EVENTITEMS, eventItemHTML.toString());
                                    }
                                    Trace.setEndCodeBlock("EventFiredTest - " + eventtypeid);
                                    EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Event FIRED:\t" + eventObject.getEventItems().size() + " event items match conditions");
                                    eventFired = true;
                                    Calendar eventDt = DateTimeUtil.getNowCalendar();
                                    String loggingFlag = eventplanitems.getValue(eventIndex, "loggingflag", "N");
                                    if ((eventfireflag.equals("O") || eventfireflag.equals("S")) && loggingFlag.equals("N")) {
                                        loggingFlag = "S";
                                    }
                                    if ((firedEventItems = EventManager.getFiredEventItems(sapphireConnection, eventPlan, eventObject, eventplanitemid, eventplandesc, eventfireflag, eventPlanHistory)).size() > 0) {
                                        DataSet eventFunctions = eventPlan.getEventFunctions(eventplanitemid);
                                        for (k = 0; k < eventFunctions.size(); ++k) {
                                            if (k > 0) {
                                                statsRow = eventStats.addRow(statsRow + 1);
                                            }
                                            String functiondesc = eventFunctions.getValue(k, STATS_EVENTPLANITEMDESC);
                                            if (eventFunctions.getValue(k, "processingscript").length() <= 0) continue;
                                            if (statsLogging) {
                                                eventStats.setString(statsRow, STATS_FUNCTIONDESC, functiondesc);
                                                eventPlan.executed(eventFunctions.getValue(k, "eventplanitemid"), eventObject, firedEventItems);
                                            }
                                            Trace.logDebug(LOGGER, "Processing function block: " + functiondesc, logContext);
                                            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing FUNCTION:\t" + functiondesc);
                                            boolean processIndividually = eventFunctions.getValue(k, "groupprocessingflag", "G").equals("I");
                                            boolean forcePropsMatch = eventFunctions.getValue(k, "groupprocessingflag", "G").equals("P");
                                            SDIData sdiData = null;
                                            DataSet sdieventplanproperties = null;
                                            if (eventObject instanceof BaseSDCEventObject) {
                                                sdiData = ((BaseSDCEventObject)eventObject).getSDIData();
                                                sdieventplanproperties = sdiData.getDataset("sdieventplanitemproperty");
                                            }
                                            if (eventPlan.isGlobal()) {
                                                if (eventObject instanceof BaseSDCEventObject) {
                                                    if (forcePropsMatch) {
                                                        eventObject.addProcessingEventItems(firedEventItems);
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t" + firedEventItems.size() + " SDC based event items grouped together with props match");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, true, sdieventplanproperties, logContext, errorHandler, eventDebugLog, start);
                                                        continue;
                                                    }
                                                    for (BaseEventItem firedEventItem : firedEventItems) {
                                                        if (((SDIEventItem)firedEventItem).hasGlobalOverride(eventPlan)) continue;
                                                        eventObject.addProcessingEventItem(firedEventItem);
                                                        if (!processIndividually) continue;
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t1 SDC based event item individually - no override");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, false, null, logContext, errorHandler, eventDebugLog, start);
                                                    }
                                                    if (!processIndividually) {
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t" + firedEventItems.size() + " SDC based event items grouped together - no override");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, false, null, logContext, errorHandler, eventDebugLog, start);
                                                    }
                                                    for (BaseEventItem firedEventItem : firedEventItems) {
                                                        if (!((SDIEventItem)firedEventItem).hasGlobalOverride(eventPlan)) continue;
                                                        eventObject.addProcessingEventItem(firedEventItem);
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t1 SDC based event item individually - global override");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, false, sdieventplanproperties, logContext, errorHandler, eventDebugLog, start);
                                                    }
                                                    continue;
                                                }
                                                if (processIndividually) {
                                                    for (BaseEventItem firedEventItem : firedEventItems) {
                                                        eventObject.addProcessingEventItem(firedEventItem);
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t1 event item individually");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, false, null, logContext, errorHandler, eventDebugLog, start);
                                                    }
                                                    continue;
                                                }
                                                eventObject.addProcessingEventItems(firedEventItems);
                                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t" + firedEventItems.size() + " event items grouped together");
                                                EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, forcePropsMatch, null, logContext, errorHandler, eventDebugLog, start);
                                                continue;
                                            }
                                            if (eventObject instanceof BaseSDCEventObject) {
                                                if (processIndividually) {
                                                    for (BaseEventItem firedEventItem : firedEventItems) {
                                                        if (!((SDIEventItem)firedEventItem).hasEventPlan(eventPlan)) continue;
                                                        eventObject.addProcessingEventItem(firedEventItem);
                                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t1 SDC based event item individually");
                                                        EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, false, sdieventplanproperties, logContext, errorHandler, eventDebugLog, start);
                                                    }
                                                    continue;
                                                }
                                                for (BaseEventItem firedEventItem : firedEventItems) {
                                                    if (!((SDIEventItem)firedEventItem).hasEventPlan(eventPlan)) continue;
                                                    eventObject.addProcessingEventItem(firedEventItem);
                                                }
                                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "        Processing fired items:\t" + firedEventItems.size() + " SDC based event items grouped together");
                                                EventManager.processEventItems(sapphireConnection, eventPlan, eventObject, eventType, eventplanitems, eventIndex, eventFunctions, k, loggingFlag, forcePropsMatch, sdieventplanproperties, logContext, errorHandler, eventDebugLog, start);
                                                continue;
                                            }
                                            throw new SapphireException("Unexpected eventObject type processing function block '" + functiondesc + "' in event plan '" + eventPlan.getEventplanid() + "'");
                                        }
                                    } else {
                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Event IGNORED:\tNo event items fired (due to multiple event suppression rules)");
                                    }
                                    if (!loggingFlag.equals("N")) {
                                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "    Logging event activity:");
                                        eventItems = eventObject.getEventItems();
                                        for (k = 0; k < eventItems.size(); ++k) {
                                            BaseEventItem eventItem = eventItems.get(k);
                                            if (!eventItem.hasFired()) continue;
                                            int row = eventPlanHistory.addRow();
                                            if (eventObject instanceof BaseSDCEventObject) {
                                                eventPlanHistory.setString(row, "keyid1", ((SDIEventItem)eventItem).getKeyid1());
                                                eventPlanHistory.setString(row, "keyid2", ((SDIEventItem)eventItem).getKeyid2());
                                                eventPlanHistory.setString(row, "keyid3", ((SDIEventItem)eventItem).getKeyid3());
                                            }
                                            eventPlanHistory.setString(row, STATS_EVENTPLANID, eventPlan.getEventplanid());
                                            eventPlanHistory.setString(row, "eventplanversionid", eventPlan.getEventplanversionid());
                                            eventPlanHistory.setString(row, "eventplanitemid", eventplanitemid);
                                            eventPlanHistory.setDate(row, STATS_EVENTDT, eventDt);
                                            StringBuffer eventSummary = new StringBuffer();
                                            eventLog = new StringBuffer();
                                            logEvents = eventItem.getLogEvents(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventplanitemid);
                                            if (logEvents != null) {
                                                for (int l = 0; l < logEvents.size(); ++l) {
                                                    logEvent = (HashMap)logEvents.get(l);
                                                    eventSummary.append(logEvent.get("summary")).append("\n");
                                                    if (!loggingFlag.equals("D")) continue;
                                                    eventLog.append(logEvent.get("log")).append("\n\n");
                                                }
                                            }
                                            eventPlanHistory.setString(row, "eventsummary", eventSummary.toString());
                                            eventPlanHistory.setClob(row, "eventlog", eventLog.toString());
                                        }
                                    }
                                    if (statsLogging) {
                                        eventItems = eventObject.getEventItems();
                                        StringBuffer eventItemHTML = new StringBuffer();
                                        for (int k3 = 0; k3 < eventItems.size(); ++k3) {
                                            BaseEventItem eventItem = eventItems.get(k3);
                                            if (eventItem.hasFired()) {
                                                StringBuffer eventSummary = new StringBuffer();
                                                eventLog = new StringBuffer();
                                                logEvents = eventItem.getLogEvents(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventplanitemid);
                                                if (logEvents != null) {
                                                    for (int l = 0; l < logEvents.size(); ++l) {
                                                        logEvent = (HashMap)logEvents.get(l);
                                                        eventSummary.append(logEvent.get("summary")).append("\n");
                                                        eventLog.append(logEvent.get("log")).append("\n\n");
                                                    }
                                                }
                                                eventItemHTML.append(eventItems.get(k3).toString()).append(eventSummary.toString()).append("\n").append(eventLog.toString()).append("\n");
                                                continue;
                                            }
                                            eventItemHTML.append(eventItems.get(k3).toString()).append("\n");
                                        }
                                        eventStats.setString(statsRow, STATS_EVENTITEMS, eventItemHTML.toString());
                                    }
                                } else {
                                    Trace.setEndCodeBlock("EventFiredTest - " + eventtypeid);
                                    EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Event IGNORED:\tNo event items match conditions");
                                    if (statsLogging) {
                                        eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_NOTMATCHED);
                                        eventStats.setString(statsRow, STATS_STATUSMESSAGE, "No event items match conditions");
                                    }
                                }
                                EventTypeSetter.endEvent(eventType);
                                continue;
                            }
                            catch (Exception e) {
                                Trace.logError(LOGGER, "Failed to evaluate event '" + eventplanitems.getValue(eventIndex, STATS_EVENTPLANITEMDESC) + "' in event plan '" + eventPlan.getEventplanid() + "'. Reason: " + e.getMessage(), e, logContext);
                                throw new SapphireException("Failed to evaluate event '" + eventplanitems.getValue(eventIndex, STATS_EVENTPLANITEMDESC) + "' in event plan '" + eventPlan.getEventplanid() + "'. Reason: " + e.getMessage(), e);
                            }
                        }
                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "      Ignoring event:\tEventFire flag set to disabled");
                        if (!statsLogging) continue;
                        eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_DISABLED);
                    }
                    continue;
                }
                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "  Ignoring event plan:\tNo events in the event plan support event object " + eventObject.getEventid());
                if (!statsLogging) continue;
                eventStats.setString(statsRow, STATS_STATUS, STATS_STATUS_IGNORED);
                eventStats.setString(statsRow, STATS_STATUSMESSAGE, "No events in the event plan support event object");
            }
        }
        catch (Throwable throwable) {
            if (eventPlanHistory.size() > 0) {
                ActionProcessor actionProcessor = testRAK != null ? new ActionProcessor(testRAK, sapphireConnection.getConnectionId()) : new ActionProcessor(sapphireConnection.getConnectionId());
                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put(STATS_SDCID, eventObject instanceof BaseSDCEventObject ? ((BaseSDCEventObject)eventObject).getSdcid() : "");
                props.put("eventplanhistory", eventPlanHistory);
                actionProcessor.processActionClass(EventPlanHistory.class.getName(), props, true);
            }
            throw throwable;
        }
        if (eventPlanHistory.size() > 0) {
            ActionProcessor actionProcessor = testRAK != null ? new ActionProcessor(testRAK, sapphireConnection.getConnectionId()) : new ActionProcessor(sapphireConnection.getConnectionId());
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put(STATS_SDCID, eventObject instanceof BaseSDCEventObject ? ((BaseSDCEventObject)eventObject).getSdcid() : "");
            props.put("eventplanhistory", eventPlanHistory);
            actionProcessor.processActionClass(EventPlanHistory.class.getName(), props, true);
        }
        return eventFired;
    }

    private static ArrayList<BaseEventItem> getFiredEventItems(SapphireConnection sapphireConnection, EventPlan eventPlan, BaseEventObject eventObject, String eventplanitemid, String eventplanitemdesc, String eventfireflag, DataSet eventPlanHistory) throws SapphireException {
        QueryProcessor queryProcessor = eventObject instanceof BaseSDCEventObject && (eventfireflag.equals("O") || eventfireflag.equals("S")) ? new QueryProcessor(sapphireConnection.getConnectionId()) : null;
        ArrayList<BaseEventItem> eventItems = eventObject.getEventItems();
        ArrayList<BaseEventItem> firedEventItems = new ArrayList<BaseEventItem>();
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (BaseEventItem eventItem : eventItems) {
            if (!eventItem.hasFired()) continue;
            if (eventfireflag.equals("O") || eventfireflag.equals("S")) {
                if (!(eventItem instanceof SDIEventItem)) {
                    throw new SapphireException("Event property 'Event Firing' = 'Once or 'After First' NOT SUPPORTED for " + eventObject.getEventid() + " events");
                }
                findMap.put("keyid1", ((SDIEventItem)eventItem).getKeyid1());
                findMap.put("keyid2", ((SDIEventItem)eventItem).getKeyid2());
                findMap.put("keyid3", ((SDIEventItem)eventItem).getKeyid3());
                findMap.put(STATS_EVENTPLANID, eventPlan.getEventplanid());
                findMap.put("eventplanversionid", eventPlan.getEventplanversionid());
                findMap.put("eventplanitemid", eventplanitemid);
                int row = eventPlanHistory.findRow(findMap);
                if (row == -1) {
                    SafeSQL safeSQL = new SafeSQL();
                    int count = queryProcessor.getPreparedCount("SELECT count(*) FROM eventplanhistory WHERE sdcid = " + safeSQL.addVar(((BaseSDCEventObject)eventObject).getSdcid()) + " AND keyid1 = " + safeSQL.addVar(((SDIEventItem)eventItem).getKeyid1()) + " AND keyid2 = " + safeSQL.addVar(((SDIEventItem)eventItem).getKeyid2()) + " AND keyid3 = " + safeSQL.addVar(((SDIEventItem)eventItem).getKeyid3()) + " AND eventplanid = " + safeSQL.addVar(eventPlan.getEventplanid()) + " AND eventplanversionid = " + safeSQL.addVar(eventPlan.getEventplanversionid()) + " AND eventplaninstance = 1 AND eventplanitemid = " + safeSQL.addVar(eventplanitemid), safeSQL.getValues());
                    if (eventfireflag.equals("O") && count == 0) {
                        firedEventItems.add(eventItem);
                        continue;
                    }
                    if (eventfireflag.equals("S") && count >= 1) {
                        firedEventItems.add(eventItem);
                        continue;
                    }
                    eventItem.logEvent(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventplanitemid, "IGNORE " + (eventfireflag.equals("O") ? "SUBSEQUENT" : "FIRST") + " EVENT", eventplanitemid, "Ignore " + (eventfireflag.equals("O") ? "subsequent" : "first") + " event: " + eventplanitemdesc, "");
                    continue;
                }
                if (eventfireflag.equals("S") && eventPlanHistory.findRow(findMap, row + 1) != -1) {
                    firedEventItems.add(eventItem);
                    continue;
                }
                eventItem.logEvent(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventplanitemid, "IGNORE " + (eventfireflag.equals("O") ? "SUBSEQUENT" : "FIRST") + " EVENT", eventplanitemid, "Ignore " + (eventfireflag.equals("O") ? "subsequent" : "first") + " event: " + eventplanitemdesc, "");
                continue;
            }
            firedEventItems.add(eventItem);
        }
        return firedEventItems;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void processEventItems(SapphireConnection sapphireConnection, EventPlan eventPlan, BaseEventObject eventObject, BaseEventType eventType, DataSet eventPlanItems, int eventPlanItemsRow, DataSet eventFunctions, int eventFunctionsRow, String loggingFlag, boolean propsMatch, DataSet propertyOverrides, LogContext logContext, ErrorHandler errorHandler, StringBuffer eventDebugLog, long start) throws SapphireException {
        block32: {
            ArrayList<BaseEventItem> eventItems = eventObject.getProcessingEventItems();
            if (eventItems.size() > 0) {
                String eventitemid = eventFunctions.getValue(eventFunctionsRow, "parenteventplanitemid");
                String functionitemid = eventFunctions.getValue(eventFunctionsRow, "eventplanitemid");
                String functiondesc = eventFunctions.getValue(eventFunctionsRow, STATS_EVENTPLANITEMDESC);
                String processingScript = eventFunctions.getValue(eventFunctionsRow, "processingscript");
                String processingType = eventFunctions.getValue(eventFunctionsRow, "processingscripttypeflag", "B");
                boolean ignoreMissingMandatory = eventFunctions.getValue(eventFunctionsRow, "missingmandatoryflag", "E").equals("I");
                boolean newTransaction = eventFunctions.getValue(eventFunctionsRow, "transactionflag", "E").equals("N");
                boolean supressError = eventFunctions.getValue(eventFunctionsRow, "supresserrorflag", "N").equals("Y");
                boolean processAsync = eventFunctions.getValue(eventFunctionsRow, "asynchronousflag", "N").equals("Y");
                String log = "";
                String error = "";
                try {
                    String missingProperties = "";
                    HashMap<String, HashMap> bindings = new HashMap<String, HashMap>();
                    HashMap eventPlanBindMap = new HashMap();
                    HashMap propertyMap = new HashMap();
                    HashMap dataMap = new HashMap();
                    HashMap<String, Object> eventObjectBindMap = new HashMap<String, Object>();
                    eventPlanBindMap.put("property", propertyMap);
                    eventPlanBindMap.put("data", dataMap);
                    eventObjectBindMap.put("eventsource", eventObject.getEventid());
                    if (eventObject instanceof BaseSDCEventObject) {
                        eventObjectBindMap.put("properties", ((BaseSDCEventObject)eventObject).getProperties());
                        SDIData sDIData = ((BaseSDCEventObject)eventObject).getSDIData();
                    }
                    bindings.put("event", eventObjectBindMap);
                    bindings.put("eventplan", eventPlanBindMap);
                    HashMap inputProps = null;
                    String[] tokens = eventPlan.getProcessingInputTokens(functionitemid);
                    if (tokens != null && tokens.length > 0) {
                        inputProps = eventType.getProcessingInputs(eventObject, tokens);
                        bindings.put("inputs", inputProps);
                        HashMap functionProperties = new HashMap();
                        missingProperties = EventManager.resolveProperties(functionProperties, eventItems, eventPlan, functionitemid, propsMatch, propertyOverrides);
                        if (missingProperties.length() > 0 && !ignoreMissingMandatory) {
                            throw new SapphireException("Missing mandatory function properties: " + missingProperties);
                        }
                        propertyMap.putAll(functionProperties);
                        inputProps.putAll(functionProperties);
                        HashMap eventProperties = new HashMap();
                        missingProperties = EventManager.resolveProperties(eventProperties, eventItems, eventPlan, eventitemid, propsMatch, propertyOverrides);
                        if (missingProperties.length() > 0 && !ignoreMissingMandatory) {
                            throw new SapphireException("Missing mandatory event properties: " + missingProperties);
                        }
                        propertyMap.putAll(eventProperties);
                        inputProps.putAll(eventProperties);
                        HashMap planProperties = new HashMap();
                        String planitemid = eventPlanItems.getValue(eventPlanItemsRow, "parenteventplanitemid");
                        missingProperties = EventManager.resolveProperties(planProperties, eventItems, eventPlan, planitemid, propsMatch, propertyOverrides);
                        if (missingProperties.length() > 0 && !ignoreMissingMandatory) {
                            throw new SapphireException("Missing mandatory plan properties: " + missingProperties);
                        }
                        propertyMap.putAll(planProperties);
                        inputProps.putAll(planProperties);
                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "          Evaluating tokens - " + tokens.length + " found");
                        for (int i = 0; i < tokens.length; ++i) {
                            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "            " + tokens[i] + "=" + inputProps.get(tokens[i]));
                        }
                    } else {
                        inputProps = new HashMap();
                        EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "          Evaluating tokens - None found");
                    }
                    String eventitemSDCid = "";
                    StringBuffer keyid1 = new StringBuffer();
                    StringBuffer keyid2 = new StringBuffer();
                    StringBuffer keyid3 = new StringBuffer();
                    StringBuffer workitemid = new StringBuffer();
                    StringBuffer workitemversionid = new StringBuffer();
                    StringBuffer workiteminstance = new StringBuffer();
                    for (int i = 0; i < eventItems.size(); ++i) {
                        if (!(eventItems.get(i) instanceof SDIEventItem)) continue;
                        SDIEventItem sdiEventItem = (SDIEventItem)eventItems.get(i);
                        eventitemSDCid = sdiEventItem.getSdcid();
                        keyid1.append(";").append(sdiEventItem.getKeyid1());
                        keyid2.append(";").append(sdiEventItem.getKeyid2());
                        keyid3.append(";").append(sdiEventItem.getKeyid3());
                        if (!(eventItems.get(i) instanceof WorkItemEventItem)) continue;
                        WorkItemEventItem workItemEventItem = (WorkItemEventItem)eventItems.get(i);
                        workitemid.append(";").append(workItemEventItem.getWorkitemid());
                        workitemversionid.append(";").append(workItemEventItem.getWorkitemversionid());
                        workiteminstance.append(";").append(workItemEventItem.getWorkiteminstance());
                    }
                    if (keyid1.length() > 0) {
                        inputProps.put("__sdcid", eventitemSDCid);
                        inputProps.put("__keyid1", keyid1.substring(1));
                        inputProps.put("__keyid2", keyid2.substring(1));
                        inputProps.put("__keyid3", keyid3.substring(1));
                        if (workitemid.length() > 0) {
                            inputProps.put("__workitemid", workitemid.substring(1));
                            inputProps.put("__workitemversionid", workitemversionid.substring(1));
                            inputProps.put("__workiteminstance", workiteminstance.substring(1));
                        }
                    }
                    if (missingProperties.length() == 0) {
                        if (processingType.equals("G")) {
                            Trace.logDebug(LOGGER, "Processing Groovy script...", logContext);
                            StringBuffer groovyLog = new StringBuffer();
                            try {
                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "          Evaluating GROOVY");
                                ProcessingUtil.processScript(sapphireConnection, processingScript, bindings, groovyLog, "EVENTPLANPROCESSING");
                                log = groovyLog.toString();
                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "            Success");
                                break block32;
                            }
                            catch (Exception e) {
                                String message = e.getMessage();
                                error = "Failed to process function block '" + functiondesc + "' in event plan '" + eventPlan.getEventplanid() + "'. Reason:\n" + (message.contains("//startinsert") ? message.substring(0, message.indexOf("//startinsert")) + "\n" + message.substring(message.indexOf("//endinsert") + 11) : message);
                                EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "            Failure: " + error);
                                throw e;
                            }
                        }
                        Trace.logDebug(LOGGER, "Processing ActionBlock...", logContext);
                        ActionBlock ab = new ActionBlock("EventPlan: " + eventPlan.getEventplanid() + " (v" + eventPlan.getEventplanversionid() + ") - Function: " + functiondesc, processingScript);
                        ab.setDebugMode(loggingFlag.equals("D") || Trace.isDebugEnabled());
                        if (testMode && processAsync) {
                            ab.setAsyncDueDt("ENDYEAR");
                        }
                        try {
                            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "          Evaluating ACTIONBLOCK");
                            HashMap<String, HashMap> groovyBindMap = new HashMap<String, HashMap>();
                            groovyBindMap.put("inputs", inputProps);
                            if (processingScript.contains("/*-GAP Editor Generated-*/;")) {
                                groovyBindMap.putAll(inputProps);
                            }
                            ab.setGroovyBindings(groovyBindMap);
                            ab.setBlockProperties(inputProps);
                            ActionProcessor actionProcessor = testRAK != null ? new ActionProcessor(testRAK, sapphireConnection.getConnectionId()) : new ActionProcessor(sapphireConnection.getConnectionId());
                            actionProcessor.processActionBlock(ab, newTransaction, processAsync);
                            if (actionProcessor.getErrorHandler() != null) {
                                errorHandler.addAll(actionProcessor.getErrorHandler());
                            }
                            log = ab.getDebugLog();
                            Trace.logDebug(LOGGER, "Function block execution log: " + log, logContext);
                            if (!loggingFlag.equals("N") || statsLogging) {
                                for (int i = 0; i < eventItems.size(); ++i) {
                                    BaseEventItem eventItem = eventItems.get(i);
                                    eventItem.logEvent(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventitemid, "EXECUTE FUNCTION", functionitemid, "Execute: " + functiondesc, loggingFlag.equals("D") || statsLogging ? log : "");
                                }
                            }
                            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "            Success");
                            break block32;
                        }
                        catch (Exception e) {
                            log = ab.getDebugLog();
                            error = "Failed to process function block '" + functiondesc + "' in event plan '" + eventPlan.getEventplanid() + "'. Reason: " + e.getMessage();
                            EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "            Failure: " + error);
                            throw e;
                        }
                    }
                    String debug = "Function block IGNORED because of missing properties: " + missingProperties;
                    EventManager.updateEventLog(eventDebugLog, System.currentTimeMillis() - start, "          " + debug);
                    Trace.logDebug(LOGGER, debug, logContext);
                    if (!loggingFlag.equals("N") || statsLogging) {
                        for (int i = 0; i < eventItems.size(); ++i) {
                            BaseEventItem eventItem = eventItems.get(i);
                            eventItem.logEvent(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventitemid, "IGNORE FUNCTION", functionitemid, "Ignore: " + functiondesc, "");
                        }
                    }
                }
                catch (Exception e) {
                    if (!loggingFlag.equals("N") || statsLogging) {
                        for (int i = 0; i < eventItems.size(); ++i) {
                            BaseEventItem eventItem = eventItems.get(i);
                            eventItem.logEvent(eventPlan.getEventplanid(), eventPlan.getEventplanversionid(), eventitemid, "FAILED FUNCTION", functionitemid, "Failed: " + functiondesc, loggingFlag.equals("D") || statsLogging ? log : "");
                        }
                    }
                    if (newTransaction && supressError) {
                        Trace.logDebug(LOGGER, "Suppressing error - " + error, logContext);
                        break block32;
                    }
                    Trace.logError(LOGGER, error, e, logContext);
                    Trace.logError(LOGGER, (Object)("Function block execution log: " + log), logContext);
                    throw new SapphireException(error, e);
                }
                finally {
                    eventObject.clearProcessingSet();
                }
            }
        }
    }

    private static String resolveProperties(HashMap inputProps, ArrayList<BaseEventItem> eventItems, EventPlan eventPlan, String eventplanitemid, boolean propsMatch, DataSet propertyOverrides) {
        StringBuffer missingMandatory = new StringBuffer();
        DataSet eventPlanItemProperties = eventPlan.getEventPlanProperties(eventplanitemid);
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (int i = 0; i < eventPlanItemProperties.size(); ++i) {
            boolean mandatory = eventPlanItemProperties.getValue(i, "mandatoryflag", "N").equals("Y");
            if (propsMatch || eventItems.size() == 1) {
                StringBuffer propertyValue = new StringBuffer();
                for (BaseEventItem eventItem : eventItems) {
                    if (eventItem instanceof SDIEventItem) {
                        int row = -1;
                        if (propertyOverrides != null) {
                            findMap.put("keyid1", ((SDIEventItem)eventItem).getKeyid1());
                            findMap.put("keyid2", ((SDIEventItem)eventItem).getKeyid2());
                            findMap.put("keyid3", ((SDIEventItem)eventItem).getKeyid3());
                            findMap.put("eventplanitemid", eventplanitemid);
                            findMap.put("propertyid", eventPlanItemProperties.getValue(i, "propertyid"));
                            row = propertyOverrides.findRow(findMap);
                        }
                        propertyValue.append(";").append(row > -1 ? propertyOverrides.getValue(row, "propertyvalue") : eventPlanItemProperties.getValue(i, "propertyvalue"));
                        continue;
                    }
                    propertyValue.append(";").append(eventPlanItemProperties.getValue(i, "propertyvalue"));
                }
                inputProps.put(eventPlanItemProperties.getValue(i, "propertyid"), propertyValue.substring(1));
            } else {
                inputProps.put(eventPlanItemProperties.getValue(i, "propertyid"), eventPlanItemProperties.getValue(i, "propertyvalue"));
            }
            if (!mandatory || ((String)inputProps.get(eventPlanItemProperties.getValue(i, "propertyid"))).length() != 0) continue;
            missingMandatory.append(";").append(eventPlanItemProperties.getValue(i, "propertyid"));
        }
        return missingMandatory.length() > 0 ? missingMandatory.substring(1) : "";
    }

    public static ArrayList<EventPlan> getSDIEventPlans(SapphireConnection sapphireConnection, String sdcid, String keyid1, String keyid2, String keyid3, boolean linked, ArrayList<String> sdclist) throws SapphireException {
        int planIndex;
        EventManager.checkEventPlansLoaded(sapphireConnection);
        ArrayList<EventPlan> plans = new ArrayList<EventPlan>();
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        HashSet globalSDCPlans = (HashSet)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, GLOBALSDCPLANS);
        if (!linked) {
            sdclist.add(sdcid);
        }
        for (String sdc : sdclist) {
            for (String eventplankey : globalSDCPlans) {
                if (!((EventPlan)eventPlans.get(eventplankey)).getSdcid().equals(sdc)) continue;
                plans.add(((EventPlan)eventPlans.get(eventplankey)).copy());
            }
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDIList(sdcid, keyid1, keyid2, keyid3);
        sdiRequest.setRequestItem("sdieventplan");
        sdiRequest.setRequestItem("sdieventplanitem");
        sdiRequest.setRequestItem("sdieventplanitemproperty");
        sdiRequest.setRequestItem("eventplanhistory");
        sdiRequest.setExtendedDataTypes(false);
        SDIData sdiData = new SDIProcessor(sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
        HashMap<String, String> filterMap = new HashMap<String, String>();
        DataSet sdieventplan = sdiData.getDataset("sdieventplan");
        for (int i = 0; i < sdieventplan.size(); ++i) {
            String eventplankey = sdieventplan.getValue(i, STATS_EVENTPLANID) + ";" + sdieventplan.getValue(i, "eventplanversionid");
            if ((linked || !sdieventplan.getValue(i, STATS_SDCID).equals(sdieventplan.getValue(i, "eventplansdcid"))) && (!linked || sdieventplan.getValue(i, STATS_SDCID).equals(sdieventplan.getValue(i, "eventplansdcid"))) || !eventPlans.containsKey(eventplankey)) continue;
            EventPlan eventPlan = ((EventPlan)eventPlans.get(eventplankey)).copy();
            filterMap.put(STATS_EVENTPLANID, sdieventplan.getValue(i, STATS_EVENTPLANID));
            filterMap.put("eventplanversionid", sdieventplan.getValue(i, "eventplanversionid"));
            filterMap.put("eventplansdcid", sdieventplan.getValue(i, "eventplansdcid"));
            eventPlan.setSDIEventPlan(sdiData.getDataset("sdieventplan").getFilteredDataSet(filterMap));
            eventPlan.setSDIEventPlanItems(sdiData.getDataset("sdieventplanitem").getFilteredDataSet(filterMap));
            eventPlan.setSDIEventPlanProperties(sdiData.getDataset("sdieventplanitemproperty").getFilteredDataSet(filterMap));
            planIndex = EventManager.getEventPlanIndex(plans, (EventPlan)eventPlans.get(eventplankey));
            if (planIndex >= 0 && globalSDCPlans.contains(eventplankey)) {
                plans.set(planIndex, eventPlan);
                continue;
            }
            plans.add(eventPlan);
        }
        DataSet eventplanhistory = sdiData.getDataset("eventplanhistory");
        filterMap.clear();
        for (int i = 0; i < eventplanhistory.size(); ++i) {
            String eventplankey = eventplanhistory.getValue(i, STATS_EVENTPLANID) + ";" + eventplanhistory.getValue(i, "eventplanversionid");
            planIndex = EventManager.getEventPlanIndex(plans, (EventPlan)eventPlans.get(eventplankey));
            if (planIndex < 0) continue;
            EventPlan eventPlan = plans.get(planIndex);
            filterMap.put(STATS_EVENTPLANID, eventplanhistory.getValue(i, STATS_EVENTPLANID));
            filterMap.put("eventplanversionid", eventplanhistory.getValue(i, "eventplanversionid"));
            eventPlan.setEventPlanHistory(sdiData.getDataset("eventplanhistory").getFilteredDataSet(filterMap));
        }
        return plans;
    }

    private static int getEventPlanIndex(ArrayList<EventPlan> eventPlans, EventPlan eventPlan) {
        if (eventPlan != null) {
            for (int i = 0; i < eventPlans.size(); ++i) {
                if (!eventPlans.get(i).getEventplanid().equals(eventPlan.getEventplanid()) || !eventPlans.get(i).getEventplanversionid().equals(eventPlan.getEventplanversionid())) continue;
                return i;
            }
        }
        return -1;
    }

    public static ArrayList<EventPlan> getActiveEventPlans(SapphireConnection sapphireConnection) {
        SDIRequest eventPlanRequest = new SDIRequest();
        eventPlanRequest.setRequestItem("primary");
        eventPlanRequest.setSDCid("LV_EventPlan");
        eventPlanRequest.setQueryFrom("eventplan");
        eventPlanRequest.setQueryWhere("statusflag='A'");
        SDIProcessor sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
        SDIData eventPlans = sdiProcessor.getSDIData(eventPlanRequest);
        DataSet primary = eventPlans.getDataset("primary");
        ArrayList<EventPlan> activeEventPlans = EventManager.getEventPlans(sapphireConnection, StringUtil.split(primary.getColumnValues(STATS_EVENTPLANID, ";"), ";"), StringUtil.split(primary.getColumnValues("eventplanversionid", ";"), ";"));
        HashMap cachedEventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        if (cachedEventPlans != null) {
            EventPlan workflowEventPlan = (EventPlan)cachedEventPlans.get("__WorkflowEvents;U");
            if (workflowEventPlan != null) {
                activeEventPlans.add(workflowEventPlan);
            }
            for (String eventplankey : cachedEventPlans.keySet()) {
                if (!eventplankey.startsWith("__subscriber_") && !eventplankey.startsWith("__application_")) continue;
                activeEventPlans.add((EventPlan)cachedEventPlans.get(eventplankey));
            }
        }
        return activeEventPlans;
    }

    public static boolean hasSubscriberEventPlan(SapphireConnection sapphireConnection) {
        HashMap cachedEventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        if (cachedEventPlans != null) {
            for (String eventplankey : cachedEventPlans.keySet()) {
                if (!eventplankey.equals("__subscriber_" + sapphireConnection.getSysuserId())) continue;
                return true;
            }
        }
        return false;
    }

    public static ArrayList<EventPlan> getEventPlans(SapphireConnection sapphireConnection, String[] eventplanid, String[] eventplanversionid) {
        EventManager.checkEventPlansLoaded(sapphireConnection);
        ArrayList<EventPlan> plans = new ArrayList<EventPlan>();
        HashMap eventPlans = (HashMap)CacheUtil.get(sapphireConnection.getDatabaseId(), LOGGER, EVENTPLANS);
        if (eventPlans != null) {
            for (int i = 0; i < eventplanid.length; ++i) {
                if (!eventPlans.containsKey(eventplanid[i] + ";" + eventplanversionid[i])) continue;
                plans.add((EventPlan)eventPlans.get(eventplanid[i] + ";" + eventplanversionid[i]));
            }
        }
        return plans;
    }

    public static BaseEventType getEventTypeInstance(String eventtypeid, String eventtypeclassname) throws SapphireException {
        try {
            return (BaseEventType)Class.forName(eventtypeclassname).newInstance();
        }
        catch (Exception e) {
            Trace.logError(LOGGER, (Object)("Failed to instanciate class for eventtypeid '" + eventtypeid + "'. Reason: " + e.getMessage()), e);
            throw new SapphireException("Failed to instanciate class for eventtypeid '" + eventtypeid + "'. Reason: " + e.getMessage(), e);
        }
    }

    private static void updateEventLog(StringBuffer eventDebugLog, long timeoffset, String message) {
        if (eventLogging) {
            String timeStr = String.valueOf(timeoffset);
            eventDebugLog.append(("00000" + timeStr).substring(timeStr.length())).append(" ").append(message).append("\n");
        }
    }

    public static void setEventLogging(boolean eventLogging) {
        EventManager.eventLogging = eventLogging;
    }

    public static void setTestMode() {
        testMode = true;
    }

    public static boolean isTestMode() {
        return testMode;
    }

    static {
        eventLogging = true;
        testMode = false;
    }
}

