/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eventplans;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.EventPlan;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIEventPlan
extends BaseAction
implements sapphire.action.AddSDIEventPlan {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing sdcid!");
        }
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        if (properties.getProperty("eventplanid").contains(";") || properties.getProperty("eventplanversionid").contains(";")) {
            throw new SapphireException("INVALID_PROPERTY", "Only 1 event plan can be added to SDIs");
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String select = "SELECT\tsdieventplan.sdcid, sdieventplan.keyid1, sdieventplan.keyid2, sdieventplan.keyid3, sdieventplan.eventplanid, sdieventplan.eventplanversionid, sdieventplan.eventplansdcid, sdieventplan.eventplaninstance, sdieventplan.usersequence FROM\tsdieventplan, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdieventplan.sdcid AND \t\trsetitems.keyid1 = sdieventplan.keyid1 AND \t\trsetitems.keyid2 = sdieventplan.keyid2 AND \t\trsetitems.keyid3 = sdieventplan.keyid3 ORDER BY sdieventplan.keyid1, sdieventplan.keyid2, sdieventplan.keyid3, sdieventplan.eventplanid, sdieventplan.eventplanversionid, sdieventplan.eventplansdcid, sdieventplan.eventplaninstance";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        DataSet sdieventplan = new DataSet(this.database.getResultSet());
        this.database.createResultSet("SELECT * FROM sdieventplanitem WHERE 1=2");
        DataSet sdieventplanitem = new DataSet(this.database.getResultSet());
        this.database.createResultSet("SELECT * FROM sdieventplanitemproperty WHERE 1=2");
        DataSet sdieventplanitemproperty = new DataSet(this.database.getResultSet());
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String eventplanid = properties.getProperty("eventplanid");
        String eventplanversionid = properties.getProperty("eventplanversionid");
        String eventplansdcid = properties.getProperty("eventplansdcid");
        properties.remove("sdcid");
        properties.remove("keyid1");
        properties.remove("keyid2");
        properties.remove("keyid3");
        properties.remove("eventplanid");
        properties.remove("eventplanversionid");
        properties.remove("eventplansdcid");
        properties.remove("applylock");
        properties.remove("rsetid");
        DataSetUtil.addUpdateColumn(sdieventplan);
        DataSetUtil.addUpdateColumn(sdieventplanitem);
        DataSetUtil.addUpdateColumn(sdieventplanitemproperty);
        Calendar now = DateTimeUtil.getNowCalendar();
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (int i = 0; i < keyid1prop.length; ++i) {
            findMap.put("keyid1", keyid1prop[i]);
            if (keyid2prop.length > i && keyid2prop[i].length() > 0) {
                findMap.put("keyid2", keyid2prop[i]);
            }
            if (keyid3prop.length > i && keyid3prop[i].length() > 0) {
                findMap.put("keyid3", keyid3prop[i]);
            }
            findMap.put("eventplanid", eventplanid);
            findMap.put("eventplanversionid", eventplanversionid);
            findMap.put("eventplansdcid", eventplansdcid);
            int row = sdieventplan.findRow(findMap);
            if (row >= 0) continue;
            row = sdieventplan.addRow();
            sdieventplan.setValue(row, "__insertupdate", "I");
            sdieventplan.setString(row, "sdcid", sdcid);
            sdieventplan.setString(row, "keyid1", keyid1prop[i]);
            sdieventplan.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
            sdieventplan.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
            sdieventplan.setString(row, "eventplanid", eventplanid);
            sdieventplan.setString(row, "eventplanversionid", eventplanversionid);
            sdieventplan.setString(row, "eventplansdcid", eventplansdcid);
            sdieventplan.setNumber(row, "eventplaninstance", 1);
            sdieventplan.setString(row, "createby", this.connectionInfo.getSysuserId());
            sdieventplan.setDate(row, "createdt", now);
            sdieventplan.setString(row, "createtool", this.connectionInfo.getTool());
            sdieventplan.setString(row, "modby", this.connectionInfo.getSysuserId());
            sdieventplan.setDate(row, "moddt", now);
            sdieventplan.setString(row, "modtool", this.connectionInfo.getTool());
            if (properties.size() <= 0) continue;
            ArrayList<EventPlan> eventPlans = EventManager.getEventPlans(new SapphireConnection(this.database.getConnection(), this.connectionInfo), new String[]{eventplanid}, new String[]{eventplanversionid});
            if (eventPlans.size() == 0) {
                throw new SapphireException("Event plan " + eventplanid + "(v" + eventplanversionid + ") is not valid or active!");
            }
            DataSet eventplanproperties = eventPlans.get(0).getEventPlanProperties();
            for (int j = 0; j < eventplanproperties.size(); ++j) {
                String eventplanitemid = eventplanproperties.getValue(j, "eventplanitemid");
                String propertyid = eventplanproperties.getValue(j, "propertyid");
                if (!properties.containsKey(propertyid)) continue;
                row = sdieventplanitemproperty.addRow();
                sdieventplanitemproperty.setValue(row, "__insertupdate", "I");
                sdieventplanitemproperty.setString(row, "sdcid", sdcid);
                sdieventplanitemproperty.setString(row, "keyid1", keyid1prop[i]);
                sdieventplanitemproperty.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                sdieventplanitemproperty.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                sdieventplanitemproperty.setString(row, "eventplanid", eventplanid);
                sdieventplanitemproperty.setString(row, "eventplanversionid", eventplanversionid);
                sdieventplanitemproperty.setString(row, "eventplansdcid", eventplansdcid);
                sdieventplanitemproperty.setNumber(row, "eventplaninstance", 1);
                sdieventplanitemproperty.setString(row, "eventplanitemid", eventplanitemid);
                sdieventplanitemproperty.setString(row, "propertyid", propertyid);
                sdieventplanitemproperty.setString(row, "propertyvalue", properties.getProperty(propertyid));
                sdieventplanitemproperty.setString(row, "createby", this.connectionInfo.getSysuserId());
                sdieventplanitemproperty.setDate(row, "createdt", now);
                sdieventplanitemproperty.setString(row, "createtool", this.connectionInfo.getTool());
                sdieventplanitemproperty.setString(row, "modby", this.connectionInfo.getSysuserId());
                sdieventplanitemproperty.setDate(row, "moddt", now);
                sdieventplanitemproperty.setString(row, "modtool", this.connectionInfo.getTool());
                findMap.put("eventplanitemid", eventplanitemid);
                row = sdieventplanitem.findRow(findMap);
                if (row == -1) {
                    row = sdieventplanitem.addRow();
                    sdieventplanitem.setValue(row, "__insertupdate", "I");
                    sdieventplanitem.setString(row, "sdcid", sdcid);
                    sdieventplanitem.setString(row, "keyid1", keyid1prop[i]);
                    sdieventplanitem.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                    sdieventplanitem.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                    sdieventplanitem.setString(row, "eventplanid", eventplanid);
                    sdieventplanitem.setString(row, "eventplanversionid", eventplanversionid);
                    sdieventplanitem.setString(row, "eventplansdcid", eventplansdcid);
                    sdieventplanitem.setNumber(row, "eventplaninstance", 1);
                    sdieventplanitem.setString(row, "eventplanitemid", eventplanitemid);
                    sdieventplanitem.setString(row, "createby", this.connectionInfo.getSysuserId());
                    sdieventplanitem.setDate(row, "createdt", now);
                    sdieventplanitem.setString(row, "createtool", this.connectionInfo.getTool());
                    sdieventplanitem.setString(row, "modby", this.connectionInfo.getSysuserId());
                    sdieventplanitem.setDate(row, "moddt", now);
                    sdieventplanitem.setString(row, "modtool", this.connectionInfo.getTool());
                }
                findMap.remove("eventplanitemid");
            }
        }
        DataSetUtil.insert(this.database, sdieventplan, "sdieventplan");
        DataSetUtil.insert(this.database, sdieventplanitem, "sdieventplanitem");
        DataSetUtil.insert(this.database, sdieventplanitemproperty, "sdieventplanitemproperty");
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }

    public static void copyDownEventPlans(DataSet eventplanData, DataSet eventplanitemData, DataSet eventplanitempropertyData, DataSet primaryData, DataSet beforePrimaryData, PropertyList sdcProps, ArrayList<PropertyList> copyFromSDCList, DBAccess database, ConnectionInfo connectionInfo, Logger logger) throws SapphireException {
        StringBuffer sdiclause;
        String[] parts;
        HashMap<String, String> findMap;
        String sdcid = sdcProps.getProperty("sdcid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        int beforePrimaryRow = -1;
        HashSet<String> uniqueSelectSDIs = new HashSet<String>();
        HashSet<String> uniqueDeleteSDIs = new HashSet<String>();
        for (int i = 0; i < copyFromSDCList.size(); ++i) {
            PropertyList copyFromSDC = copyFromSDCList.get(i);
            String copyfromsdcid = copyFromSDC.getProperty("sdcid");
            String fkcolumnid = copyFromSDC.getProperty("fkcolumnid");
            String fkcolumnid2 = copyFromSDC.getProperty("fkcolumnid2");
            String fkcolumnid3 = copyFromSDC.getProperty("fkcolumnid3");
            if (!primaryData.isValidColumn(fkcolumnid)) continue;
            findMap = new HashMap<String, String>();
            for (int j = 0; j < primaryData.size(); ++j) {
                boolean isTemplate = false;
                String oldValue = "";
                String oldValue2 = "";
                String oldValue3 = "";
                if (beforePrimaryData != null) {
                    findMap.put(keycolid1, primaryData.getValue(j, keycolid1));
                    if (keycolid2.length() > 0) {
                        findMap.put(keycolid2, primaryData.getValue(j, keycolid2));
                    }
                    if (keycolid3.length() > 0) {
                        findMap.put(keycolid3, primaryData.getValue(j, keycolid3));
                    }
                    if ((beforePrimaryRow = beforePrimaryData.findRow(findMap)) >= 0) {
                        oldValue = beforePrimaryData.getValue(beforePrimaryRow, fkcolumnid);
                        oldValue2 = beforePrimaryData.getValue(beforePrimaryRow, fkcolumnid2);
                        oldValue3 = beforePrimaryData.getValue(beforePrimaryRow, fkcolumnid3);
                        isTemplate = beforePrimaryData.getValue(beforePrimaryRow, "templateflag", "N").equals("Y");
                    }
                }
                if (isTemplate) continue;
                String fkvalue = primaryData.getValue(j, fkcolumnid);
                if (beforePrimaryData == null && fkvalue.length() > 0 || beforePrimaryData != null && fkvalue.length() > 0 && !fkvalue.equals(oldValue)) {
                    uniqueSelectSDIs.add((String)copyfromsdcid + ";" + fkvalue + ";" + primaryData.getValue(j, fkcolumnid2) + ";" + primaryData.getValue(j, fkcolumnid3));
                }
                if (beforePrimaryData == null || oldValue.length() <= 0 || fkvalue.equals(oldValue)) continue;
                uniqueDeleteSDIs.add((String)copyfromsdcid + ";" + oldValue + ";" + oldValue2 + ";" + oldValue3);
            }
        }
        if (uniqueSelectSDIs.size() > 0) {
            StringBuffer selectWhereClause = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            for (String selectSDI : uniqueSelectSDIs) {
                parts = StringUtil.split(selectSDI, ";");
                sdiclause = new StringBuffer(" sdcid = " + safeSQL.addVar(parts[0]) + " AND keyid1 = " + safeSQL.addVar(parts[1]));
                if (parts[2].length() > 0) {
                    sdiclause.append(" AND keyid2 = ").append(safeSQL.addVar(parts[2]));
                } else {
                    sdiclause.append(" AND keyid2 = '(null)'");
                }
                if (parts[3].length() > 0) {
                    sdiclause.append(" AND keyid3 = ").append(safeSQL.addVar(parts[3]));
                } else {
                    sdiclause.append(" AND keyid3 = '(null)'");
                }
                sdiclause.append(" AND eventplansdcid = ").append(safeSQL.addVar(sdcid));
                selectWhereClause.append(" OR ( ").append(sdiclause).append(" )");
            }
            QueryProcessor queryProcessor = new QueryProcessor(connectionInfo.getConnectionId());
            DataSet eventplanCopyDown = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdieventplan WHERE " + selectWhereClause.substring(4) + " ORDER BY sdcid, keyid1, keyid2, keyid3, eventplanid, eventplanversionid", safeSQL.getValues());
            DataSet eventplanitemCopyDown = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdieventplanitem WHERE " + selectWhereClause.substring(4) + " ORDER BY sdcid, keyid1, keyid2, keyid3, eventplanid, eventplanversionid", safeSQL.getValues());
            DataSet eventplanitempropertyCopyDown = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdieventplanitemproperty WHERE " + selectWhereClause.substring(4) + " ORDER BY sdcid, keyid1, keyid2, keyid3, eventplanid, eventplanversionid", safeSQL.getValues());
            findMap = new HashMap();
            for (int i = 0; i < copyFromSDCList.size(); ++i) {
                PropertyList copyFromSDC = copyFromSDCList.get(i);
                String copyfromsdcid = copyFromSDC.getProperty("sdcid");
                String fkcolumnid = copyFromSDC.getProperty("fkcolumnid");
                String fkcolumnid2 = copyFromSDC.getProperty("fkcolumnid2");
                String fkcolumnid3 = copyFromSDC.getProperty("fkcolumnid3");
                findMap.put("sdcid", copyfromsdcid);
                for (int j = 0; j < primaryData.size(); ++j) {
                    String fkvalue = primaryData.getValue(j, fkcolumnid);
                    String fkvalue2 = primaryData.getValue(j, fkcolumnid2);
                    String fkvalue3 = primaryData.getValue(j, fkcolumnid3);
                    if (fkvalue.length() <= 0) continue;
                    findMap.put("keyid1", fkvalue);
                    findMap.put("keyid2", fkcolumnid2.length() > 0 ? fkvalue2 : "(null)");
                    findMap.put("keyid3", fkcolumnid3.length() > 0 ? fkvalue3 : "(null)");
                    int row = -1;
                    while ((row = eventplanCopyDown.findRow(findMap, row + 1)) >= 0) {
                        eventplanData.copyRow(eventplanCopyDown, row, 1);
                        row = eventplanData.size() - 1;
                        eventplanData.setValue(row, "sdcid", sdcid);
                        eventplanData.setValue(row, "keyid1", primaryData.getValue(j, keycolid1));
                        eventplanData.setValue(row, "keyid2", primaryData.getValue(j, keycolid2, "(null)"));
                        eventplanData.setValue(row, "keyid3", primaryData.getValue(j, keycolid3, "(null)"));
                        eventplanData.setValue(row, "sourcesdcid", copyfromsdcid);
                        eventplanData.setValue(row, "sourcekeyid1", fkvalue);
                        eventplanData.setValue(row, "sourcekeyid2", fkcolumnid2.length() > 0 ? fkvalue2 : "(null)");
                        eventplanData.setValue(row, "sourcekeyid3", fkcolumnid3.length() > 0 ? fkvalue3 : "(null)");
                    }
                    row = -1;
                    while ((row = eventplanitemCopyDown.findRow(findMap, row + 1)) >= 0) {
                        eventplanitemData.copyRow(eventplanitemCopyDown, row, 1);
                        row = eventplanitemData.size() - 1;
                        eventplanitemData.setValue(row, "sdcid", sdcid);
                        eventplanitemData.setValue(row, "keyid1", primaryData.getValue(j, keycolid1));
                        eventplanitemData.setValue(row, "keyid2", primaryData.getValue(j, keycolid2, "(null)"));
                        eventplanitemData.setValue(row, "keyid3", primaryData.getValue(j, keycolid3, "(null)"));
                    }
                    row = -1;
                    while ((row = eventplanitempropertyCopyDown.findRow(findMap, row + 1)) >= 0) {
                        eventplanitempropertyData.copyRow(eventplanitempropertyCopyDown, row, 1);
                        row = eventplanitempropertyData.size() - 1;
                        eventplanitempropertyData.setValue(row, "sdcid", sdcid);
                        eventplanitempropertyData.setValue(row, "keyid1", primaryData.getValue(j, keycolid1));
                        eventplanitempropertyData.setValue(row, "keyid2", primaryData.getValue(j, keycolid2, "(null)"));
                        eventplanitempropertyData.setValue(row, "keyid3", primaryData.getValue(j, keycolid3, "(null)"));
                    }
                }
            }
        }
        if (uniqueDeleteSDIs.size() > 0) {
            StringBuffer deleteWhereClause = new StringBuffer();
            SafeSQL safeSQLdelete = new SafeSQL();
            for (String deleteSDI : uniqueDeleteSDIs) {
                parts = StringUtil.split(deleteSDI, ";");
                sdiclause = new StringBuffer(" sourcesdcid = " + safeSQLdelete.addVar(parts[0]) + " AND sourcekeyid1 = " + safeSQLdelete.addVar(parts[1]));
                if (parts[2].length() > 0) {
                    sdiclause.append(" AND sourcekeyid2 = ").append(safeSQLdelete.addVar(parts[2]));
                } else {
                    sdiclause.append(" AND sourcekeyid2 = '(null)'");
                }
                if (parts[3].length() > 0) {
                    sdiclause.append(" AND sourcekeyid3 = ").append(safeSQLdelete.addVar(parts[3]));
                } else {
                    sdiclause.append(" AND sourcekeyid3 = '(null)'");
                }
                sdiclause.append(" AND eventplansdcid = ").append(safeSQLdelete.addVar(sdcid));
                deleteWhereClause.append(" OR ( ").append(sdiclause).append(" )");
            }
            database.createPreparedResultSet("sdieventplan", "SELECT sdcid, keyid1, keyid2, keyid3, eventplanid, eventplanversionid, eventplansdcid, eventplaninstance FROM sdieventplan WHERE " + deleteWhereClause.substring(4), safeSQLdelete.getValues());
            SafeSQL safeSQL = new SafeSQL();
            while (database.getNext("sdieventplan")) {
                safeSQL.reset();
                String where = "WHERE sdcid = " + safeSQL.addVar(database.getString("sdcid")) + " AND keyid1 = " + safeSQL.addVar(database.getString("keyid1")) + " AND keyid2 = " + safeSQL.addVar(database.getString("keyid2")) + " AND keyid3 = " + safeSQL.addVar(database.getString("keyid3")) + " AND eventplanid = " + safeSQL.addVar(database.getString("eventplanid")) + " AND eventplanversionid = " + safeSQL.addVar(database.getString("eventplanversionid")) + " AND eventplansdcid = " + safeSQL.addVar(database.getString("eventplansdcid")) + " AND eventplaninstance = " + safeSQL.addVar(database.getInt("eventplaninstance"));
                database.executePreparedUpdate("DELETE FROM sdieventplanitemproperty " + where, safeSQL.getValues());
                database.executePreparedUpdate("DELETE FROM sdieventplanitem " + where, safeSQL.getValues());
                database.executePreparedUpdate("DELETE FROM sdieventplan " + where, safeSQL.getValues());
            }
            database.closeResultSet("sdieventplan");
        }
    }
}

