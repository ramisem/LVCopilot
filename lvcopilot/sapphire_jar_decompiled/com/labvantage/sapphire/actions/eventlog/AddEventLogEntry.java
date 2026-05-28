/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eventlog;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddEventLogEntry
extends BaseAction
implements sapphire.action.AddEventLogEntry {
    private ActionProcessor ap;

    @Override
    public void processAction(PropertyList property) throws SapphireException {
        String[] traceLogIdArray;
        String trackItemId = property.getProperty("trackitemid");
        String tracelogId = property.getProperty("tracelogid");
        String eventLogDesc = property.getProperty("eventlogdesc");
        String eventType = property.getProperty("eventtype");
        String eventDt = property.getProperty("eventdt");
        String oldValue = property.getProperty("oldvalue");
        String newValue = property.getProperty("newvalue");
        String departmentId = property.getProperty("departmentid", "");
        String delimiter = property.getProperty("delimiter", ";");
        TranslationProcessor tp = this.getTranslationProcessor();
        DataSet trackitemDS = null;
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        Calendar calendar = Calendar.getInstance();
        SafeSQL safesql = new SafeSQL();
        if (trackItemId == null || trackItemId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("TrackItemId must be specified. Cannot create EventLog."));
        }
        if (tracelogId == null || tracelogId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("TraceLog ID must be specified. Cannot create EventLog."));
        }
        String[] trackItemIdArray = StringUtil.split(trackItemId, delimiter);
        if (trackItemIdArray.length != (traceLogIdArray = StringUtil.split(tracelogId, delimiter)).length) {
            this.logger.error("TrackItem ID and TraceLog ID must have the same number of values.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("TrackItem ID and TraceLog ID must have the same number of values."));
        }
        String sql = "select trackitemid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, custodialdepartmentid from trackitem where trackitemid in (" + safesql.addIn(trackItemId, delimiter) + ")";
        trackitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safesql.getValues());
        String[] eventLogDescArray = StringUtil.split(eventLogDesc, delimiter);
        if (eventLogDescArray.length > 1 && trackItemIdArray.length != eventLogDescArray.length) {
            this.logger.error("Incorrect number of values passed for EventLog Description.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for EventLog Description."));
        }
        String[] eventTypeArray = StringUtil.split(eventType, delimiter);
        if (eventTypeArray.length > 1 && trackItemIdArray.length != eventTypeArray.length) {
            this.logger.error("Incorrect number of values passed for Event Type.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for Event Type."));
        }
        String[] eventDtArray = StringUtil.split(eventDt, delimiter);
        if (eventDtArray.length > 1 && trackItemIdArray.length != eventDtArray.length) {
            this.logger.error("Incorrect number of values passed for Event Date.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for Event Date."));
        }
        String[] oldValueArray = StringUtil.split(oldValue, delimiter);
        if (oldValueArray.length > 1 && trackItemIdArray.length != oldValueArray.length) {
            this.logger.error("Incorrect number of values passed for Old Value.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for Old Value."));
        }
        String[] newValueArray = StringUtil.split(newValue, delimiter);
        if (newValueArray.length > 1 && trackItemIdArray.length != newValueArray.length) {
            this.logger.error("Incorrect number of values passed for New Value.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for New Value."));
        }
        String[] departmentIdArray = StringUtil.split(departmentId, delimiter);
        if (departmentIdArray.length > 1 && trackItemIdArray.length != departmentIdArray.length) {
            this.logger.error("Incorrect number of values passed for Department ID.");
            throw new SapphireException("INVALID_PROPERTIES", tp.translate("Incorrect number of values passed for Department ID."));
        }
        Calendar calEventDt = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat();
        DataSet insert = new DataSet();
        insert.addColumn("s_eventlogid", 0);
        insert.addColumn("trackitemid", 0);
        insert.addColumn("eventsdcid", 0);
        insert.addColumn("eventkeyid1", 0);
        insert.addColumn("eventkeyid2", 0);
        insert.addColumn("eventkeyid3", 0);
        insert.addColumn("eventlogdesc", 0);
        insert.addColumn("eventtype", 0);
        insert.addColumn("eventdt", 2);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("oldvalue", 0);
        insert.addColumn("newvalue", 0);
        insert.addColumn("tracelogid", 0);
        insert.addColumn("departmentid", 0);
        String eventLogID = "";
        String sequenceid = String.valueOf(calendar.get(1)) + String.valueOf(calendar.get(2) + 1) + String.valueOf(calendar.get(5));
        for (int i = 0; i < trackItemIdArray.length; ++i) {
            int sequence = sequenceProcessor.getSequence("s_eventlog", sequenceid) + 1;
            int row = insert.addRow();
            String strTrackItemId = trackItemIdArray[i];
            HashMap<String, String> filteredDsMap = new HashMap<String, String>();
            filteredDsMap.put("trackitemid", strTrackItemId);
            DataSet dsTrackItemFiltered = null;
            if (trackitemDS != null && trackitemDS.getRowCount() > 0) {
                dsTrackItemFiltered = trackitemDS.getFilteredDataSet(filteredDsMap);
            }
            insert.setString(row, "s_eventlogid", sequenceid + "-" + sequence);
            insert.setString(row, "trackitemid", strTrackItemId);
            insert.setString(row, "tracelogid", traceLogIdArray[i]);
            if (dsTrackItemFiltered != null && dsTrackItemFiltered.getRowCount() > 0) {
                insert.setString(row, "eventsdcid", dsTrackItemFiltered.getValue(0, "linksdcid", ""));
            }
            if (dsTrackItemFiltered != null && dsTrackItemFiltered.getRowCount() > 0) {
                insert.setString(row, "eventkeyid1", dsTrackItemFiltered.getValue(0, "linkkeyid1", ""));
            }
            if (dsTrackItemFiltered != null && dsTrackItemFiltered.getRowCount() > 0) {
                insert.setString(row, "eventkeyid2", dsTrackItemFiltered.getValue(0, "linkkeyid2", ""));
            }
            if (dsTrackItemFiltered != null && dsTrackItemFiltered.getRowCount() > 0) {
                insert.setString(row, "eventkeyid3", dsTrackItemFiltered.getValue(0, "linkkeyid3", ""));
            }
            if (eventLogDescArray.length == 1) {
                insert.setString(row, "eventlogdesc", eventLogDescArray[0]);
            } else {
                insert.setString(row, "eventlogdesc", eventLogDescArray[i]);
            }
            if (eventTypeArray.length == 1) {
                insert.setString(row, "eventtype", eventTypeArray[0]);
            } else {
                insert.setString(row, "eventtype", eventTypeArray[i]);
            }
            try {
                String strEventDt = "";
                strEventDt = eventDtArray.length == 1 ? eventDtArray[0] : eventDtArray[i];
                Date date = sdf.parse(strEventDt);
                calEventDt.setTime(date);
            }
            catch (ParseException e) {
                this.logger.error(e.getMessage());
            }
            insert.setDate(row, "eventdt", calEventDt);
            if (oldValueArray.length == 1) {
                insert.setString(row, "oldvalue", oldValueArray[0]);
            } else {
                insert.setString(row, "oldvalue", oldValueArray[i]);
            }
            if (newValueArray.length == 1) {
                insert.setString(row, "newvalue", newValueArray[0]);
            } else {
                insert.setString(row, "newvalue", newValueArray[i]);
            }
            if (departmentId != null && !"".equalsIgnoreCase(departmentId)) {
                if (departmentIdArray.length == 1) {
                    insert.setString(row, "departmentid", departmentIdArray[0]);
                } else {
                    insert.setString(row, "departmentid", departmentIdArray[i]);
                }
            } else {
                insert.setString(row, "departmentid", dsTrackItemFiltered.getValue(0, "custodialdepartmentid", ""));
            }
            insert.setString(row, "createby", this.connectionInfo.getSysuserId());
            insert.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
            eventLogID = eventLogID + ";" + sequenceid + "-" + sequence++;
        }
        DataSetUtil.insert(this.database, insert, "s_eventlog");
        property.setProperty("newkeyid1", eventLogID.substring(1));
    }
}

