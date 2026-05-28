/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddEvent
extends BaseAction
implements sapphire.action.AddEvent {
    static final String LABVANTAGE_CVS_ID = "$Revision: 67311 $";
    private static ArrayList<String> nonUpdatableCols = new ArrayList();
    String addEventPolicy = "";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String uniqueIdentifier = "";
        String participantId = actionProps.getProperty("participantid", "");
        String eventLabel = actionProps.getProperty("eventlabel", "");
        String eventType = actionProps.getProperty("eventtype", "");
        String eventDefId = actionProps.getProperty("eventdefid", "");
        String actionAddEventPolicy = actionProps.getProperty("addeventpolicy");
        if (actionAddEventPolicy.length() > 0 && (actionAddEventPolicy.equals("Always add new event") || actionAddEventPolicy.equals("Add new event when permitted") || actionAddEventPolicy.equals("Update existing event"))) {
            this.addEventPolicy = actionAddEventPolicy;
        }
        this.validateInputParams(actionProps);
        DataSet inputParamsDSOrig = this.createDataSetFromInputParams(actionProps);
        DataSet ds = new DataSet();
        String filterProperty = "";
        if (actionProps.containsKey("eventlabel") && OpalUtil.isNotEmpty(actionProps.getProperty("eventlabel"))) {
            filterProperty = "eventlabel";
        } else if (actionProps.containsKey("eventdefid") && OpalUtil.isNotEmpty(actionProps.getProperty("eventdefid"))) {
            filterProperty = "eventdefid";
        }
        if (OpalUtil.isNotEmpty(filterProperty)) {
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < inputParamsDSOrig.size(); ++i) {
                String participantid;
                String filterValue = inputParamsDSOrig.getString(i, filterProperty, "");
                if (filterValue.length() <= 0) continue;
                filter.clear();
                filter.put(filterProperty, filterValue);
                String visiteventid = inputParamsDSOrig.getString(i, "visiteventid", "");
                if (visiteventid.length() > 0) {
                    filter.put("visiteventid", visiteventid);
                }
                if ((participantid = inputParamsDSOrig.getString(i, "participantid", "")).length() > 0) {
                    filter.put("participantid", participantid);
                }
                if (ds.findRow(filter) != -1) continue;
                ds.copyRow(inputParamsDSOrig, i, 1);
            }
        }
        if ("Timepoint".equalsIgnoreCase(eventType)) {
            if (actionProps.getProperty("visiteventid", "").length() == 0) {
                throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Parameters:  (Visit Event ID/Event Label) or (Visit Event ID/EventDefId)"));
            }
            if (eventLabel.length() == 0 && eventDefId.length() == 0) {
                throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Parameters:  (Visit Event ID/Event Label) or (Visit Event ID/EventDefId)"));
            }
            if (eventLabel.length() > 0) {
                uniqueIdentifier = "visiteventid,eventlabel";
            } else if (eventDefId.length() > 0) {
                uniqueIdentifier = "visiteventid,eventdefid";
            }
            ds = this.getParentEvtDetails(ds);
        } else {
            if (participantId.length() == 0) {
                throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Parameters:  (Participant ID/Event Label) or (Participant ID/EventDefId)"));
            }
            if (eventLabel.length() == 0 && eventDefId.length() == 0) {
                throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Parameters:  (Visit Event ID/Event Label) or (Visit Event ID/EventDefId)"));
            }
            if (eventLabel.length() > 0) {
                uniqueIdentifier = "participantid,eventlabel";
            } else if (eventDefId.length() > 0) {
                uniqueIdentifier = "participantid,eventdefid";
            }
            if (StringUtil.getLen(actionProps.getProperty("visitedsite")) == 0L) {
                if (StringUtil.getLen(actionProps.getProperty("visiteddepartment")) > 0L || StringUtil.getLen(actionProps.getProperty("visitedsitename")) > 0L) {
                    this.getSiteId(actionProps, ds);
                } else {
                    this.getSiteIdFromParticipant(ds);
                }
            }
        }
        DataSet addAdhocEventsDS = new DataSet();
        this.addColumnsToDataSet(addAdhocEventsDS, ds);
        DataSet addEventsDS = new DataSet();
        this.addColumnsToDataSet(addEventsDS, ds);
        DataSet protocolEventsDS = new DataSet();
        this.addColumnsToDataSet(protocolEventsDS, ds);
        DataSet existingEventDS = new DataSet();
        this.addColumnsToDataSet(existingEventDS, ds);
        if (actionProps.getProperty("eventdefid").length() > 0) {
            this.generateProtocolEventDS4EventDef(ds, protocolEventsDS, eventType, addEventsDS, existingEventDS, uniqueIdentifier);
        } else {
            this.logger.info("Identify event definition for the following Visit Labels : " + actionProps.getProperty("eventlabel"));
            this.findEventDef(ds, eventType, addAdhocEventsDS, protocolEventsDS);
            this.findEvent(protocolEventsDS, eventType, addEventsDS, existingEventDS, uniqueIdentifier);
        }
        if (addAdhocEventsDS.size() > 0) {
            this.createAdhocEvents(addAdhocEventsDS, eventType);
        }
        if (addEventsDS.size() > 0) {
            this.createAdhocEvents(addEventsDS, eventType);
        }
        for (int i = 0; i < inputParamsDSOrig.size(); ++i) {
            String[] identifierArr;
            HashMap<String, String> map = new HashMap<String, String>();
            for (String anIdentifierArr : identifierArr = StringUtil.split(uniqueIdentifier, ",")) {
                map.put(anIdentifierArr, inputParamsDSOrig.getValue(i, anIdentifierArr));
            }
            int index = addEventsDS.findRow(map);
            if (index != -1) {
                inputParamsDSOrig.setValue(i, "s_participanteventid", addEventsDS.getValue(index, "s_participanteventid"));
                continue;
            }
            int rowIndex = addAdhocEventsDS.findRow(map);
            if (rowIndex != -1) {
                inputParamsDSOrig.setValue(i, "s_participanteventid", addAdhocEventsDS.getValue(rowIndex, "s_participanteventid"));
                continue;
            }
            int ri = existingEventDS.findRow(map);
            if (ri == -1) continue;
            inputParamsDSOrig.setValue(i, "s_participanteventid", existingEventDS.getValue(ri, "s_participanteventid"));
        }
        actionProps.setProperty("neweventid", inputParamsDSOrig.getColumnValues("s_participanteventid", ";"));
    }

    private void generateProtocolEventDS4EventDef(DataSet ds, DataSet protocolEventsDS, String eventType, DataSet addEventsDS, DataSet existingEventDS, String uniqueIdentifier) throws SapphireException {
        for (int i = 0; i < ds.size(); ++i) {
            protocolEventsDS.copyRow(ds, i, 1);
        }
        StringBuffer sql = new StringBuffer();
        if (eventType.equalsIgnoreCase("Timepoint")) {
            protocolEventsDS.sort("visiteventid,eventdefid");
            ArrayList<DataSet> dsAddSDIPropList = protocolEventsDS.getGroupedDataSets("visiteventid,eventdefid");
            for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
                String rsetid2 = "";
                DataSet groupDataSet = aDsAddSDIPropList;
                String visitid = groupDataSet.getValue(0, "visitEventId");
                SafeSQL safeSQL = new SafeSQL();
                sql.setLength(0);
                String eventDefs = groupDataSet.getColumnValues("eventdefid", ";");
                sql.append("SELECT TPe.s_participanteventid, TPe.eventlabel, TPed.s_eventdefid, TPed.allowmultipleflag, TPed.eventdeflabel, TPe.parentparticipanteventid ").append(" FROM s_participantevent TPe, s_eventdef TPed, s_participantevent Ve  ");
                if (StringUtil.split(eventDefs, ";").length <= 750) {
                    sql.append(" WHERE TPed.s_eventdefid in (").append(safeSQL.addIn(eventDefs, ";")).append(") ");
                } else {
                    rsetid2 = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventDefs, null, null);
                    sql.append(" WHERE TPed.s_eventdefid in (");
                    sql.append(" select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid2)).append(")");
                }
                sql.append("AND TPe.parentparticipanteventid = ").append(safeSQL.addVar(visitid)).append(" ").append("AND Ve.s_participanteventid = TPe.parentparticipanteventid AND ").append(" Ve.eventdefid = TPed.parenteventdefid AND ").append(" TPe.eventdefid = TPed.s_eventdefid  ");
                DataSet eventds = new DataSet();
                eventds.copyRow(groupDataSet, 0, 1);
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDS(safeSQL, protocolEventsDS, addEventsDS, eventds, "visitEventId", "parentparticipanteventid", existingEventDS, uniqueIdentifier);
                if (StringUtil.getLen(rsetid2) <= 0L) continue;
                this.getDAMProcessor().clearRSet(rsetid2);
            }
        } else {
            String rsetid = "";
            protocolEventsDS.sort("participantid,eventdefid");
            ArrayList<DataSet> dsAddSDIPropList = protocolEventsDS.getGroupedDataSets("participantid,eventdefid");
            SafeSQL safeSQL = new SafeSQL();
            Iterator<DataSet> rsetid2 = dsAddSDIPropList.iterator();
            while (rsetid2.hasNext()) {
                DataSet aDsAddSDIPropList;
                DataSet groupDataSet = aDsAddSDIPropList = rsetid2.next();
                String participantid = groupDataSet.getValue(0, "participantid");
                DataSet eventds = new DataSet();
                eventds.copyRow(groupDataSet, 0, 1);
                safeSQL.reset();
                sql.setLength(0);
                String eventDefs = groupDataSet.getColumnValues("eventdefid", ";");
                sql.append("SELECT pe.s_participanteventid, ed.allowmultipleflag, ed.eventdeflabel, pe.participantid, pe.eventlabel, ed.s_eventdefid, pe.eventdt FROM s_eventdef ed, s_participantevent pe  ");
                if (StringUtil.split(eventDefs, ";").length <= 750) {
                    sql.append(" WHERE ed.s_eventdefid in (").append(safeSQL.addIn(eventDefs, ";")).append(") ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventDefs, null, null);
                    sql.append(" WHERE ed.s_eventdefid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append("AND ed.s_eventdefid = pe.eventdefid AND pe.participantid = ").append(safeSQL.addVar(participantid));
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDS(safeSQL, protocolEventsDS, addEventsDS, eventds, "participantid", "participantid", existingEventDS, uniqueIdentifier);
                if (StringUtil.getLen(rsetid) <= 0L) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
            for (int i = 0; i < protocolEventsDS.size(); ++i) {
                if (StringUtil.getLen(protocolEventsDS.getString(i, "s_participanteventid")) != 0L || "Y".equals(protocolEventsDS.getString(i, "__addflag", "N"))) continue;
                addEventsDS.copyRow(protocolEventsDS, i, 1);
                protocolEventsDS.setString(i, "__addflag", "Y");
            }
        }
    }

    private String createAdhocEvents(DataSet addAdhocEventsDS, String eventType) throws ActionException {
        DataSet newEventDS = new DataSet();
        String sql = "SELECT S_PARTICIPANTEVENTID FROM S_PARTICIPANTEVENT WHERE PARTICIPANTID = ? AND EVENTDEFID IN ( SELECT t2.s_eventdefid FROM s_eventdef t1, s_eventdef t2 WHERE t2.clinicalprotocolid = t1.clinicalprotocolid AND t2.clinicalprotocolrevision = t1.clinicalprotocolrevision AND t2.cohortid = t1.cohortid AND t2.eventdeftype = t1.eventdeftype AND t2.eventdeflabel = t1.eventdeflabel AND t1.s_eventdefid = ?)";
        for (int i = 0; i < addAdhocEventsDS.size(); ++i) {
            String eventdefid = addAdhocEventsDS.getString(i, "eventdefid");
            String participantid = addAdhocEventsDS.getString(i, "participantid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{participantid, eventdefid});
            if (ds != null && ds.size() > 0) {
                if ("Update existing event".equals(this.addEventPolicy)) {
                    addAdhocEventsDS.setString(i, "s_participanteventid", ds.getString(0, "s_participanteventid"));
                    continue;
                }
                newEventDS.copyRow(addAdhocEventsDS, i, 1);
                continue;
            }
            newEventDS.copyRow(addAdhocEventsDS, i, 1);
        }
        if (newEventDS.size() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_ParticipantEvent");
            for (int i = 0; i < newEventDS.getColumnCount(); ++i) {
                String colid = newEventDS.getColumnId(i);
                if (newEventDS.getColumnValues(colid, ";").length() <= 0) continue;
                actionProps.setProperty(colid, newEventDS.getColumnValues(colid, ";"));
            }
            if (eventType.equalsIgnoreCase("Timepoint")) {
                actionProps.setProperty("parentparticipanteventid", newEventDS.getColumnValues("visitEventId", ";"));
            }
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), actionProps);
            String eventIds = actionProps.getProperty("newkeyid1");
            newEventDS.addColumnValues("s_participanteventid", 0, eventIds, ";");
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < newEventDS.size(); ++i) {
                String participantid = newEventDS.getString(i, "participantid", "");
                String eventdefid = newEventDS.getString(i, "eventdefid", "");
                String s_participanteventid = newEventDS.getString(i, "s_participanteventid", "");
                filter.clear();
                filter.put("participantid", participantid);
                filter.put("eventdefid", eventdefid);
                int row = addAdhocEventsDS.findRow(filter);
                if (row == -1) continue;
                addAdhocEventsDS.setString(row, "s_participanteventid", s_participanteventid);
            }
        }
        return addAdhocEventsDS.getColumnValues("s_participanteventid", ";");
    }

    private void findEvent(DataSet protocolEventsDS, String eventType, DataSet addEventsDS, DataSet existingEventDS, String uniqueIndentifier) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        if (eventType.equalsIgnoreCase("Timepoint")) {
            protocolEventsDS.sort("visiteventid");
            ArrayList<DataSet> dsAddSDIPropList = protocolEventsDS.getGroupedDataSets("visiteventid");
            for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
                String rsetid = "";
                DataSet groupDataSet = aDsAddSDIPropList;
                String visitid = groupDataSet.getValue(0, "visitEventId");
                SafeSQL safeSQL = new SafeSQL();
                sql.setLength(0);
                String eventLabels = groupDataSet.getColumnValues("eventlabel", ";");
                sql.append("SELECT TPe.s_participanteventid, TPed.allowmultipleflag, TPed.eventdeflabel, TPed.s_eventdefid, TPe.eventlabel,  TPe.parentparticipanteventid ").append(" FROM s_participantevent TPe, s_eventdef TPed, s_participantevent Ve");
                if (StringUtil.split(eventLabels, ";").length <= 750) {
                    sql.append(" WHERE TPed.eventdeflabel in (").append(safeSQL.addIn(eventLabels, ";")).append(") ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventLabels, null, null);
                    sql.append(" WHERE TPed.eventdeflabel in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append(" AND TPe.parentparticipanteventid = ").append(safeSQL.addVar(visitid)).append(" AND Ve.s_participanteventid = TPe.parentparticipanteventid").append(" AND Ve.eventdefid = TPed.parenteventdefid").append(" AND TPe.eventdefid = TPed.s_eventdefid ");
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDS(safeSQL, protocolEventsDS, addEventsDS, groupDataSet, "visitEventId", "parentparticipanteventid", existingEventDS, uniqueIndentifier);
                if (StringUtil.getLen(rsetid) <= 0L) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            protocolEventsDS.sort("participantid");
            ArrayList<DataSet> dsAddSDIPropList = protocolEventsDS.getGroupedDataSets("participantid");
            SafeSQL safeSQL = new SafeSQL();
            for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
                String rsetid = "";
                DataSet groupDataSet = aDsAddSDIPropList;
                boolean useEventDefID = true;
                for (int i = 0; i < groupDataSet.size(); ++i) {
                    if (groupDataSet.getString(i, "eventdefid", "").length() != 0) continue;
                    useEventDefID = false;
                    break;
                }
                String participantid = groupDataSet.getValue(0, "participantid");
                safeSQL.reset();
                sql.setLength(0);
                sql.append("SELECT pe.s_participanteventid, ed.allowmultipleflag, ed.eventdeflabel, pe.participantid, pe.eventlabel, ed.s_eventdefid ");
                sql.append(" FROM s_eventdef ed, s_participantevent pe");
                if (useEventDefID) {
                    String eventDefIds = groupDataSet.getColumnValues("eventdefid", ";");
                    if (StringUtil.split(eventDefIds, ";").length <= 750) {
                        sql.append(" WHERE ed.s_eventdefid in (").append(safeSQL.addIn(eventDefIds, ";")).append(")");
                    } else {
                        rsetid = this.getDAMProcessor().createRSet("LV_EventDef", eventDefIds, null, null);
                        sql.append(" WHERE ed.s_eventdefid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                    }
                } else {
                    String eventLabels = groupDataSet.getColumnValues("eventlabel", ";");
                    if (StringUtil.split(eventLabels, ";").length <= 750) {
                        sql.append(" WHERE ed.eventdeflabel in (").append(safeSQL.addIn(eventLabels, ";")).append(")");
                    } else {
                        rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventLabels, null, null);
                        sql.append(" WHERE ed.eventdeflabel  in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                    }
                }
                sql.append(" AND ed.s_eventdefid = pe.eventdefid AND pe.participantid = ").append(safeSQL.addVar(participantid));
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDS(safeSQL, protocolEventsDS, addEventsDS, groupDataSet, "participantid", "participantid", existingEventDS, uniqueIndentifier);
                if (StringUtil.getLen(rsetid) <= 0L) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    public void processEventDS(SafeSQL safeSQL, DataSet protocolEventsDS, DataSet addEventsDS, DataSet groupDataSet, String protocolEventDSCol, String dbCol, DataSet existingEventDS, String uniqueIdentifier) throws SapphireException {
        int j;
        String extracolstomatch = "";
        String dbCol2Match = "";
        if (uniqueIdentifier.contains("eventdefid")) {
            extracolstomatch = "eventdefid";
            dbCol2Match = "s_eventdefid";
        } else if (uniqueIdentifier.contains("eventlabel")) {
            extracolstomatch = "eventlabel";
            dbCol2Match = "eventlabel";
        }
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        int counter = 0;
        if (OpalUtil.isEmpty(this.addEventPolicy)) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
            this.addEventPolicy = policy != null ? policy.getProperty("addeventpolicy", "Always add new event") : "Always add new event";
        }
        for (j = 0; j < protocolEventsDS.size(); ++j) {
            ++counter;
            boolean eventExists = false;
            for (int row = 0; row < dataSet.size(); ++row) {
                String eventId = dataSet.getString(row, "s_participanteventid");
                String allowMultiple = this.ifNull(dataSet.getString(row, "allowmultipleflag"));
                if (!protocolEventsDS.getValue(j, protocolEventDSCol).equals(this.ifNull(dataSet.getString(row, dbCol))) || !protocolEventsDS.getValue(j, extracolstomatch).equals(this.ifNull(dataSet.getString(row, dbCol2Match)))) continue;
                eventExists = true;
                if (protocolEventsDS.getValue(j, "eventlabel", "").length() == 0) {
                    protocolEventsDS.setValue(j, "eventlabel", dataSet.getString(row, "eventdeflabel", ""));
                }
                if (eventId != null && eventId.length() > 0) {
                    if (!"Y".equalsIgnoreCase(allowMultiple) && !"Yes".equalsIgnoreCase(allowMultiple)) {
                        if ("Always add new event".equals(this.addEventPolicy)) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Unable to Add Visit"), "VALIDATION", this.getTranslationProcessor().translate("The Visit already exists and does not allow multiple instances") + ": " + existingEventDS.getValue(j, "eventlabel"));
                        }
                        protocolEventsDS.setValue(j, "s_participanteventid", eventId);
                        existingEventDS.copyRow(protocolEventsDS, j, 1);
                        continue;
                    }
                    if ("Update existing event".equals(this.addEventPolicy)) {
                        protocolEventsDS.setValue(j, "s_participanteventid", eventId);
                        existingEventDS.copyRow(protocolEventsDS, j, 1);
                        continue;
                    }
                    if ("Y".equals(protocolEventsDS.getString(j, "__addflag", "N"))) continue;
                    protocolEventsDS.setString(j, "__addflag", "Y");
                    addEventsDS.copyRow(protocolEventsDS, j, 1);
                    continue;
                }
                if ("Y".equals(protocolEventsDS.getString(j, "__addflag", "N"))) continue;
                protocolEventsDS.setString(j, "__addflag", "Y");
                addEventsDS.copyRow(protocolEventsDS, j, 1);
            }
            if (eventExists || "Y".equals(protocolEventsDS.getString(j, "__addflag", "N"))) continue;
            protocolEventsDS.setString(j, "__addflag", "Y");
            addEventsDS.copyRow(protocolEventsDS, j, 1);
        }
        if (counter == 0) {
            for (j = 0; j < groupDataSet.size(); ++j) {
                addEventsDS.copyRow(groupDataSet, j, 1);
            }
            protocolEventsDS.setString(-1, "__addflag", "Y");
        }
    }

    private void findEventDef(DataSet ds, String eventType, DataSet addAdhocEventsDS, DataSet protocolEventsDS) throws SapphireException {
        String eventLabel = ds.getColumnValues("eventlabel", ";");
        StringBuilder sql = new StringBuilder();
        if (eventType.equalsIgnoreCase("Timepoint")) {
            ds.sort("visiteventid");
            ArrayList<DataSet> dsAddSDIPropList = ds.getGroupedDataSets("visiteventid");
            SafeSQL safeSQL = new SafeSQL();
            for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
                String rsetid = "";
                DataSet groupDataSet = aDsAddSDIPropList;
                String visitid = groupDataSet.getValue(0, "visitEventId");
                String eventLabels = groupDataSet.getColumnValues("eventlabel", ";");
                safeSQL.reset();
                sql.setLength(0);
                sql.append("SELECT ed.s_eventdefid, ed.eventdeflabel, pe.s_participanteventid , ed.allowmultipleflag FROM s_eventdef ed, s_participantevent pe  ");
                if (StringUtil.split(eventLabels, ";").length <= 750) {
                    sql.append(" WHERE ed.eventdeflabel IN (").append(safeSQL.addIn(eventLabel, ";")).append(") ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventLabels, null, null);
                    sql.append(" WHERE ed.eventdeflabel in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append(" AND pe.s_participanteventid = ").append(safeSQL.addVar(visitid)).append(" AND ed.parenteventdefid = pe.eventdefid");
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDefDS(safeSQL, groupDataSet, protocolEventsDS, addAdhocEventsDS, "visitEventId", "s_participanteventid");
                if (!OpalUtil.isNotEmpty(rsetid)) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            ds.sort("participantid");
            ArrayList<DataSet> dsAddSDIPropList = ds.getGroupedDataSets("participantid");
            SafeSQL safeSQL = new SafeSQL();
            for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
                String rsetid = "";
                DataSet groupDataSet = aDsAddSDIPropList;
                String participantid = groupDataSet.getValue(0, "participantid");
                String eventLabels = groupDataSet.getColumnValues("eventlabel", ";");
                safeSQL.reset();
                sql.setLength(0);
                sql.append("SELECT ed.s_eventdefid, ed.eventdeflabel, p.s_participantid, ed.allowmultipleflag ");
                sql.append(" FROM s_eventdef ed, s_participant p, s_clinicalprotocol cp");
                if (StringUtil.split(eventLabels, ";").length <= 750) {
                    sql.append(" WHERE ed.eventdeflabel in (").append(safeSQL.addIn(eventLabels, ";")).append(") ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", eventLabels, null, null);
                    sql.append(" WHERE ed.eventdeflabel in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append(" and ed.clinicalprotocolid = cp.s_clinicalprotocolid").append(" and ed.clinicalprotocolversionid = cp.s_clinicalprotocolversionid").append(" and ed.clinicalprotocolrevision = p.clinicalprotocolrevision").append(" and ed.cohortid = p.cpcohortid").append(" and p.s_participantid = ").append(safeSQL.addVar(participantid)).append(" and cp.s_clinicalprotocolid = p.clinicalprotocolid").append(" and cp.versionstatus = 'C'").append(" and cp.s_clinicalprotocolrevision = p.clinicalprotocolrevision");
                safeSQL.setPreparedSQL(sql.toString());
                this.processEventDefDS(safeSQL, groupDataSet, protocolEventsDS, addAdhocEventsDS, "participantid", "s_participantid");
                if (StringUtil.getLen(rsetid) <= 0L) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    public void processEventDefDS(SafeSQL safeSQL, DataSet groupDataSet, DataSet protocolEventsDS, DataSet addAdhocEventsDS, String dsCol, String dbCol) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        int counter = 0;
        if (ds != null) {
            counter = ds.size();
            for (int i = 0; i < ds.size(); ++i) {
                String eventDefId = ds.getString(i, "s_eventdefid", "");
                for (int j = 0; j < groupDataSet.size(); ++j) {
                    if (!groupDataSet.getValue(j, "eventlabel").equals(ds.getString(i, "eventdeflabel", "")) || !groupDataSet.getValue(j, dsCol).equals(ds.getString(i, dbCol, "")) || eventDefId == null || eventDefId.length() <= 0) continue;
                    if (ds.getString(i, "allowmultipleflag") != null && (ds.getString(i, "allowmultipleflag").equalsIgnoreCase("Y") || ds.getString(i, "allowmultipleflag").equalsIgnoreCase("Yes"))) {
                        if (groupDataSet.getValue(j, "eventdt", "").length() > 0) {
                            groupDataSet.setValue(j, "eventdefid", eventDefId);
                            protocolEventsDS.copyRow(groupDataSet, j, 1);
                            continue;
                        }
                        throw new SapphireException("INVALID_PARAMETER", this.getTranslationProcessor().translate("Event Date is mandatory for reoccuring Events") + " ");
                    }
                    groupDataSet.setValue(j, "eventdefid", eventDefId);
                    protocolEventsDS.copyRow(groupDataSet, j, 1);
                }
            }
        }
        if (counter == 0 || groupDataSet.size() > 0) {
            for (int j = 0; j < groupDataSet.size(); ++j) {
                if (groupDataSet.getValue(j, "eventdefid", "").length() != 0) continue;
                if (groupDataSet.getValue(j, "adhocevtflag", "").equalsIgnoreCase("Y") || groupDataSet.getValue(j, "adhocevtflag", "").equalsIgnoreCase("Yes")) {
                    this.logger.info("Event Definition not found...Creating Adhoc event");
                    addAdhocEventsDS.copyRow(groupDataSet, j, 1);
                    continue;
                }
                throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("Adhoc Events are not allowed"));
            }
        }
    }

    private void addColumnsToDataSet(DataSet addDataSet, DataSet inputParamsDS) {
        for (int i = 0; i < inputParamsDS.getColumnCount(); ++i) {
            String colid = inputParamsDS.getColumnId(i);
            if (colid.contains("event_")) {
                colid = StringUtil.split(colid, "event_")[1];
            }
            if (nonUpdatableCols.contains(colid)) continue;
            addDataSet.addColumn(colid, 0);
        }
        addDataSet.addColumn("keyid1", 0);
        addDataSet.addColumn("eventdefid", 0);
    }

    private void validateInputParams(PropertyList actionProps) throws SapphireException {
        int maxlength = this.findMaxLength(actionProps);
        for (Object o : actionProps.keySet()) {
            String param = (String)o;
            String value = actionProps.getProperty(param);
            int length = StringUtil.split(value, ";").length;
            if (maxlength <= length || length <= 1) continue;
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Invalid Input:") + param + this.getTranslationProcessor().translate(" Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values."));
        }
    }

    private int findMaxLength(PropertyList actionProps) {
        int maxlength = 0;
        for (Object o : actionProps.keySet()) {
            String param = (String)o;
            String value = actionProps.getProperty(param);
            int l = StringUtil.split(value, ";").length;
            if (l <= maxlength) continue;
            maxlength = l;
        }
        return maxlength;
    }

    private DataSet createDataSetFromInputParams(PropertyList actionProps) {
        String eventdate = actionProps.getProperty("eventdt");
        DataSet ds = new DataSet();
        ds.addColumnValues("participantid", 0, actionProps.getProperty("participantid", ""), ";");
        ds.addColumnValues("eventType", 0, actionProps.getProperty("eventtype", ""), ";");
        ds.addColumnValues("eventlabel", 0, actionProps.getProperty("eventlabel", ""), ";");
        ds.addColumnValues("eventdefid", 0, actionProps.getProperty("eventdefid", ""), ";");
        ds.addColumnValues("eventdt", 0, eventdate, ";");
        ds.addColumnValues("eventstatus", 0, actionProps.getProperty("eventstatus", "Completed"), ";");
        ds.addColumnValues("visitEventId", 0, actionProps.getProperty("visiteventid", ""), ";");
        ds.addColumnValues("visitedDept", 0, actionProps.getProperty("visiteddepartment", ""), ";");
        ds.addColumnValues("sstudysiteid", 0, actionProps.getProperty("visitedsite", ""), ";");
        ds.addColumnValues("sitedesc", 0, actionProps.getProperty("visitedsitename", ""), ";");
        ds.addColumnValues("adhocevtflag", 0, actionProps.getProperty("adhocevtflag", "N"), ";", "N");
        ds.addColumnValues("auditreason", 0, actionProps.getProperty("auditreason", ""), ";");
        ds.addColumnValues("auditactivity", 0, actionProps.getProperty("auditactivity", ""), ";");
        ds.addColumnValues("auditsignedflag", 0, actionProps.getProperty("auditsignedflag", "N"), ";");
        PropertyListCollection columns = this.getSDCProcessor().getColumns("LV_ParticipantEvent");
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (nonUpdatableCols.contains(colid) || !actionProps.containsKey("event_" + colid)) continue;
            ds.addColumnValues(colid, 0, actionProps.getProperty("event_" + colid, ""), ";");
        }
        ds.addColumn("s_participanteventid", 0);
        ds.padColumn("participantid");
        ds.padColumn("eventdt");
        ds.padColumn("eventtype");
        ds.padColumn("eventstatus");
        ds.padColumn("visiteventid");
        if (actionProps.containsKey("adhocevtflag")) {
            ds.padColumn("adhocevtflag");
        }
        return ds;
    }

    private DataSet getParentEvtDetails(DataSet ds) throws SapphireException {
        String rsetid = "";
        String visitEventIds = ds.getColumnValues("visitEventId", ";");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT participantid, sstudysiteid, s_participanteventid FROM s_participantevent ");
        if (StringUtil.split(visitEventIds, ";").length <= 750) {
            sql.append(" WHERE s_participanteventid in (").append(safeSQL.addIn(visitEventIds, ";")).append(")");
        } else {
            rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", visitEventIds, null, null);
            sql.append(" WHERE s_participanteventid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        }
        DataSet evtDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (evtDS != null) {
            for (int i = 0; i < evtDS.size(); ++i) {
                for (int j = 0; j < ds.size(); ++j) {
                    if (!evtDS.getValue(i, "s_participanteventid").equals(ds.getValue(j, "visitEventId"))) continue;
                    ds.setValue(j, "participantid", evtDS.getValue(i, "participantid", ";"));
                    ds.setValue(j, "sstudysiteid", evtDS.getValue(i, "sstudysiteid", ";"));
                }
            }
        }
        if (OpalUtil.isNotEmpty(rsetid)) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return ds;
    }

    private void getSiteId(PropertyList properties, DataSet ds) throws SapphireException {
        ds.sort("participantid");
        ArrayList<DataSet> dsAddSDIPropList = ds.getGroupedDataSets("participantid");
        for (DataSet aDsAddSDIPropList : dsAddSDIPropList) {
            String rsetid = "";
            DataSet groupDataSet = aDsAddSDIPropList;
            String participantid = groupDataSet.getValue(0, "participantid");
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append(" SELECT s.s_studysiteid, p.s_participantid, s.departmentid, s.studysitedesc FROM s_studysite s, s_participant p ");
            if (properties.getProperty("visitedsitename").length() > 0) {
                String siteNames = groupDataSet.getColumnValues("sitedesc", ";");
                if (StringUtil.split(siteNames, ";").length <= 750) {
                    sql.append(" WHERE s.studysiteDesc in (").append(safeSQL.addIn(siteNames, ";")).append(") AND s.sstudyid = p.sstudyid AND p.s_participantid = ").append(safeSQL.addVar(participantid)).append(" ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_StudySite", siteNames, null, null);
                    sql.append(" WHERE s.studysiteDesc in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(") AND s.sstudyid = p.sstudyid AND p.s_participantid = ").append(safeSQL.addVar(participantid)).append(" ");
                }
            } else if (properties.getProperty("visiteddepartment").length() > 0) {
                String departments = groupDataSet.getColumnValues("visitedDept", ";");
                if (StringUtil.split(departments, ";").length <= 750) {
                    sql.append(" WHERE s.departmentid in (").append(safeSQL.addIn(departments, ";")).append(") AND s.sstudyid = p.sstudyid AND p.s_participantid = ").append(safeSQL.addVar(participantid)).append(" ");
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_StudySite", departments, null, null);
                    sql.append(" WHERE s.departmentid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(") AND s.sstudyid = p.sstudyid AND p.s_participantid = ").append(safeSQL.addVar(participantid)).append(" ");
                }
            }
            DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (dataset != null) {
                for (int i = 0; i < dataset.size(); ++i) {
                    for (int k = 0; k < ds.size(); ++k) {
                        if (!ds.getValue(k, "participantid", "").equals(dataset.getString(i, "s_participantid"))) continue;
                        if (properties.getProperty("visitedsitename").length() > 0 && ds.getValue(k, "sitedesc", "").equals(dataset.getString(i, "studysitedesc"))) {
                            ds.setValue(k, "sstudysiteid", dataset.getString(i, "s_studysiteid", ""));
                        }
                        if (properties.getProperty("visiteddepartment").length() <= 0 || !ds.getValue(k, "visitedDept", "").equals(dataset.getString(i, "departmentid"))) continue;
                        ds.setValue(k, "sstudysiteid", dataset.getString(i, "s_studysiteid", ""));
                    }
                }
            }
            if (!OpalUtil.isNotEmpty(rsetid)) continue;
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private void getSiteIdFromParticipant(DataSet inputParamsDS) throws SapphireException {
        String participantIds = inputParamsDS.getColumnValues("participantid", ";");
        String rsetid = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT studysiteid, s_participantid FROM s_participant p WHERE ");
        if (StringUtil.split(participantIds, ";").length <= 750) {
            sql.append(" s_participantid in (").append(safeSQL.addIn(participantIds, ";")).append(")");
        } else {
            rsetid = this.getDAMProcessor().createRSet("LV_StudySite", participantIds, null, null);
            sql.append(" s_participantid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getString(i, "studysiteid") == null || ds.getString(i, "studysiteid").length() == 0) {
                    this.logger.info("No enrolling site found for the participant " + ds.getString(i, "s_participantid"));
                    continue;
                }
                for (int k = 0; k < inputParamsDS.size(); ++k) {
                    if (!inputParamsDS.getValue(k, "participantid", "").equals(ds.getString(i, "s_participantid"))) continue;
                    if (inputParamsDS.getValue(k, "sstudysiteid").length() == 0) {
                        inputParamsDS.setValue(k, "sstudysiteid", ds.getString(i, "studysiteid", ""));
                        continue;
                    }
                    throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("participant") + " " + ds.getString(i, "s_participantid") + this.getTranslationProcessor().translate(" More than one site found for input parameters: "));
                }
            }
        }
        if (OpalUtil.isNotEmpty(rsetid)) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private String ifNull(String str) {
        if (str == null) {
            str = "";
        }
        return str;
    }

    static {
        nonUpdatableCols.add("moddt");
        nonUpdatableCols.add("modby");
        nonUpdatableCols.add("modtool");
        nonUpdatableCols.add("createdt");
        nonUpdatableCols.add("createby");
        nonUpdatableCols.add("createtool");
        nonUpdatableCols.add("auditsequence");
        nonUpdatableCols.add("templateflag");
    }

    static class UserMessages {
        static final String MISSINGREQARGSFORVISIT = "Missing Required Parameters:  (Visit Event ID/Event Label) or (Visit Event ID/EventDefId)";
        static final String MISSINGREQARGSFORTP = "Missing Required Parameters:  (Participant ID/Event Label) or (Participant ID/EventDefId)";
        static final String INVALIDINPUT = "Invalid Input:";
        static final String MULTIPARAMINPUT = " Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values.";
        static final String ADHOCEVENTNOTALLOWED = "Adhoc Events are not allowed";
        static final String MORETHANONESITE = " More than one site found for input parameters: ";

        UserMessages() {
        }
    }
}

