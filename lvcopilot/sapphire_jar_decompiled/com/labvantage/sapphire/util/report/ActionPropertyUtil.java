/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.report;

import com.labvantage.sapphire.Trace;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ActionPropertyUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static Class[] getClasses(String pckgname) throws ClassNotFoundException {
        ArrayList classes = new ArrayList();
        File directory = null;
        try {
            directory = new File(Thread.currentThread().getContextClassLoader().getResource('/' + pckgname.replace('.', '/')).getFile());
        }
        catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
        }
        if (directory.exists()) {
            String[] files = directory.list();
            for (int i = 0; i < files.length; ++i) {
                if (!files[i].endsWith(".class")) continue;
                classes.add(Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6)));
            }
        } else {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
        }
        Class[] classesA = new Class[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }

    public static DataSet getProperties(Class[] interfaceClasses, String packageName) throws SapphireException {
        DataSet actionPropsDS = new DataSet();
        actionPropsDS.addColumn("actionid", 0);
        actionPropsDS.addColumn("actionversionid", 0);
        actionPropsDS.addColumn("propertyid", 0);
        actionPropsDS.addColumn("propertytype", 0);
        actionPropsDS.addColumn("propertytitle", 0);
        actionPropsDS.addColumn("propertyhelp", 0);
        actionPropsDS.addColumn("defaultvalue", 0);
        actionPropsDS.addColumn("propertytypeflag", 0);
        try {
            for (int i = 0; i < interfaceClasses.length; ++i) {
                String actionid;
                if (!interfaceClasses[i].isInterface() || (actionid = interfaceClasses[i].getName().split(packageName + ".")[1]).equals("ActionConstants")) continue;
                Field[] fields = interfaceClasses[i].getDeclaredFields();
                Field versionidFld = null;
                String versionid = "";
                try {
                    versionidFld = interfaceClasses[i].getDeclaredField("VERSIONID");
                }
                catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                if (versionidFld != null) {
                    versionid = (String)versionidFld.get(versionidFld.getName());
                }
                for (int j = 0; j < fields.length; ++j) {
                    int dsRowIndex = actionPropsDS.size();
                    actionPropsDS.addRow();
                    actionPropsDS.setValue(dsRowIndex, "actionid", actionid);
                    actionPropsDS.setValue(dsRowIndex, "actionversionid", versionid);
                    String propertyid = fields[j].get(fields[j].getName()).toString();
                    if (!(actionid.contains(propertyid) || fields[j].getName().equalsIgnoreCase("VERSIONID") || fields[j].getName().contains("LABVANTAGE_CVS_ID"))) {
                        actionPropsDS.setValue(dsRowIndex, "propertyid", propertyid);
                    }
                    String type = fields[j].getType().toString().split("java.lang.")[1].toLowerCase();
                    actionPropsDS.setValue(dsRowIndex, "propertytype", type);
                    if (fields[j].getName().indexOf("PROPERTY_") != -1) {
                        actionPropsDS.setValue(dsRowIndex, "propertytypeflag", "I");
                        continue;
                    }
                    actionPropsDS.setValue(dsRowIndex, "propertytypeflag", "O");
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return actionPropsDS;
    }

    public static String getMaxUserSequence(String actionid, QueryProcessor qp) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT MAX(usersequence) FROM actionproperty WHERE actionid =" + safeSQL.addVar(actionid);
        return qp.getPreparedSqlDataSet(sql, safeSQL.getValues()).getValue(0, "max(usersequence)");
    }

    public static DataSet getActionPropsFromDB(QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT action.actionid, ap.actionversionid, ap.propertyid, ap.propertytype, ap.propertytitle, ").append("ap.propertyhelp, ap.defaultvalue, ap.propertytypeflag").append(" FROM action  LEFT OUTER JOIN  actionproperty ap ON ").append(" action.actionid = ap.actionid AND ").append(" action.actionversionid = ap.actionversionid ").append(" WHERE action.actionlanguage = 'java' order by action.actionid");
        return qp.getSqlDataSet(sql.toString());
    }

    public static DataSet getActionDSInDB(DataSet actionPropsInDB, String actionid) {
        HashMap<String, String> propsMap = new HashMap<String, String>();
        propsMap.put("actionid", actionid);
        return actionPropsInDB.getFilteredDataSet(propsMap);
    }

    public static HashMap getPropertyValFromDBDS(DataSet filteredActionDSInDB, String propertyId) {
        HashMap<String, String> dataMap = new HashMap<String, String>();
        HashMap<String, String> propsMap = new HashMap<String, String>();
        propsMap.put("propertyid", propertyId);
        int index = filteredActionDSInDB.findRow(propsMap);
        dataMap.put("propertytitle", filteredActionDSInDB.getValue(index, "propertytitle"));
        dataMap.put("propertyhelp", filteredActionDSInDB.getValue(index, "propertyhelp"));
        dataMap.put("defaultvalue", filteredActionDSInDB.getValue(index, "defaultvalue"));
        dataMap.put("propertytype", filteredActionDSInDB.getValue(index, "propertytype"));
        if (index != -1) {
            filteredActionDSInDB.deleteRow(index);
            dataMap.put("propertyid", propertyId);
        } else {
            dataMap.put("propertyid", "");
        }
        return dataMap;
    }

    public static ArrayList getExtraActionsDefinedInDB(ArrayList actionPropsAL, DataSet actionPropsDSInDB) {
        int j;
        actionPropsDSInDB.sort("actionid");
        ArrayList<DataSet> groupedActionsDSIndBAL = actionPropsDSInDB.getGroupedDataSets("actionid");
        Trace.logDebug(" groupedActionsDSIndBAL.size() " + groupedActionsDSIndBAL.size());
        Trace.logDebug(" actionPropsAL.size() " + actionPropsAL.size());
        Vector<Integer> rowsToBeRemoved = new Vector<Integer>();
        if (groupedActionsDSIndBAL.size() > actionPropsAL.size()) {
            for (int i = 0; i < groupedActionsDSIndBAL.size(); ++i) {
                DataSet dsInDB = groupedActionsDSIndBAL.get(i);
                String actionId = dsInDB.getValue(0, "actionid");
                for (j = 0; j < actionPropsAL.size(); ++j) {
                    DataSet dsInClass = (DataSet)actionPropsAL.get(j);
                    String actionIdInClass = dsInClass.getValue(0, "actionid");
                    if (!actionId.trim().equals(actionIdInClass.trim())) continue;
                    rowsToBeRemoved.add(i);
                }
            }
        }
        DataSet extraActionsDS = new DataSet();
        for (int i = 0; i < groupedActionsDSIndBAL.size(); ++i) {
            if (rowsToBeRemoved.contains(i)) continue;
            DataSet dsToBeCopied = groupedActionsDSIndBAL.get(i);
            for (j = 0; j < dsToBeCopied.size(); ++j) {
                extraActionsDS.copyRow(dsToBeCopied, j, 1);
            }
        }
        return extraActionsDS.getGroupedDataSets("actionid");
    }
}

