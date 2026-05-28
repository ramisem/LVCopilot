/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIResourceRequirement
extends BaseAction {
    public static final String PROPERTY_RESOURCENUM = "resourcenum";
    public static final String PROPERTY_RSETID = "rsetid";
    public static final String PROPERTY_APPLYLOCK = "applylock";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SEPARATOR = "separator";
    public static final String PROPERTY_RESOURCETYPEFLAG = "resourcetypeflag";
    public static final String PROPERTY_DURATIONRULE = "durationrule";
    public static final String PROPERTY_ANALYSTTYPE = "analysttype";
    public static final String PROPERTY_INSTRUMENTTYPEID = "instrumenttypeid";
    public static final String PROPERTY_INSTRUMENTMODELID = "instrumentmodelid";
    public static final String PROPERTY_AUTOASSIGNFLAG = "autoassignflag";
    public static final String PROPERTY_AUTOASSIGNANALYSTID = "autoassignanalystid";
    public static final String PROPERTY_AUTOASSIGNINSTRUMENTID = "autoassigninstrumentid";
    public static final String PROPERTY_AUTOASSIGNDEPARTMENTID = "autoassigndepartmentid";
    public static final String PROPERTY_LINKEDTO = "linktocontext";
    public static final String PROPERTY_RESOURCEDESC = "resourcelabel";
    private static final String PROPERTY_TABLEID = "sdiresourcerequirement";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty(PROPERTY_SDCID);
        String rsetid = properties.getProperty(PROPERTY_RSETID);
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty(PROPERTY_APPLYLOCK).equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty(PROPERTY_KEYID1), properties.getProperty(PROPERTY_KEYID2), properties.getProperty(PROPERTY_KEYID3)) : dam.createRSet(sdcid, properties.getProperty(PROPERTY_KEYID1), properties.getProperty(PROPERTY_KEYID2), properties.getProperty(PROPERTY_KEYID3));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String select = "SELECT\tsdiresourcerequirement.sdcid, sdiresourcerequirement.keyid1, sdiresourcerequirement.keyid2, sdiresourcerequirement.keyid3, sdiresourcerequirement.resourcenum FROM\tsdiresourcerequirement, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdiresourcerequirement.sdcid AND \t\trsetitems.keyid1 = sdiresourcerequirement.keyid1 AND \t\trsetitems.keyid2 = sdiresourcerequirement.keyid2 AND \t\trsetitems.keyid3 = sdiresourcerequirement.keyid3 ORDER BY sdiresourcerequirement.keyid1, sdiresourcerequirement.keyid2, sdiresourcerequirement.keyid3, sdiresourcerequirement.resourcenum";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        this.clearIrrelevantData(properties);
        DataSet sdiresourcerequirement = new DataSet();
        sdiresourcerequirement.setResultSet(this.database.getResultSet());
        DataSet insert = new DataSet();
        insert.addColumn(PROPERTY_SDCID, 0);
        insert.addColumn(PROPERTY_KEYID1, 0);
        insert.addColumn(PROPERTY_KEYID2, 0);
        insert.addColumn(PROPERTY_KEYID3, 0);
        insert.addColumn(PROPERTY_RESOURCENUM, 0);
        insert.addColumn(PROPERTY_RESOURCEDESC, 0);
        insert.addColumn(PROPERTY_RESOURCETYPEFLAG, 0);
        insert.addColumn(PROPERTY_ANALYSTTYPE, 0);
        insert.addColumn(PROPERTY_INSTRUMENTTYPEID, 0);
        insert.addColumn(PROPERTY_INSTRUMENTMODELID, 0);
        insert.addColumn(PROPERTY_AUTOASSIGNFLAG, 0);
        insert.addColumn(PROPERTY_AUTOASSIGNANALYSTID, 0);
        insert.addColumn(PROPERTY_AUTOASSIGNINSTRUMENTID, 0);
        insert.addColumn(PROPERTY_AUTOASSIGNDEPARTMENTID, 0);
        insert.addColumn(PROPERTY_LINKEDTO, 0);
        insert.addColumn(PROPERTY_DURATIONRULE, 0);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createtool", 0);
        String[] keyid1prop = StringUtil.split(properties.getProperty(PROPERTY_KEYID1), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty(PROPERTY_KEYID2), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty(PROPERTY_KEYID3), ";");
        String[] resourcenumprop = StringUtil.split(properties.getProperty(PROPERTY_RESOURCENUM), ";");
        String[] resourcetypeflagprop = StringUtil.split(properties.getProperty(PROPERTY_RESOURCETYPEFLAG), ";");
        String[] resourcedescprop = StringUtil.split(properties.getProperty(PROPERTY_RESOURCEDESC), ";");
        String[] analysttypeprop = StringUtil.split(properties.getProperty(PROPERTY_ANALYSTTYPE), ";");
        String[] instrumenttypeidprop = StringUtil.split(properties.getProperty(PROPERTY_INSTRUMENTTYPEID), ";");
        String[] instrumentmodelidprop = StringUtil.split(properties.getProperty(PROPERTY_INSTRUMENTMODELID), ";");
        String[] autoassignflagprop = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNFLAG), ";");
        String[] autoassignAnalystprop = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNANALYSTID), ";");
        String[] autoassignInstrumentprop = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNINSTRUMENTID), ";");
        String[] autoassignDepartmentprop = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNDEPARTMENTID), ";");
        String[] linkedtoprop = StringUtil.split(properties.getProperty(PROPERTY_LINKEDTO), ";");
        String[] durationruleprop = StringUtil.split(properties.getProperty(PROPERTY_DURATIONRULE), ";");
        String[] usersequenceprops = StringUtil.split(properties.getProperty("usersequence"), ";");
        if (keyid1prop.length == resourcenumprop.length) {
            Calendar now = DateTimeUtil.getNowCalendar();
            HashMap<String, String> findMap = new HashMap<String, String>();
            int maxRowNum = this.getMaxRowNum(sdiresourcerequirement);
            for (int i = 0; i < keyid1prop.length; ++i) {
                String resourcenum = resourcenumprop[i];
                findMap.put(PROPERTY_KEYID1, keyid1prop[i]);
                if (keyid2prop.length > i && keyid2prop[i].length() > 0) {
                    findMap.put(PROPERTY_KEYID2, keyid2prop[i]);
                }
                if (keyid3prop.length > i && keyid3prop[i].length() > 0) {
                    findMap.put(PROPERTY_KEYID3, keyid3prop[i]);
                }
                findMap.put(PROPERTY_RESOURCENUM, resourcenumprop[i]);
                int row = sdiresourcerequirement.findRow(findMap);
                if (row >= 0) continue;
                if (resourcenum != null && resourcenum.length() == 0) {
                    resourcenum = "" + ++maxRowNum;
                }
                row = insert.addRow();
                String resourceTypeFlag = resourcetypeflagprop[i];
                insert.setString(row, PROPERTY_SDCID, sdcid);
                insert.setString(row, PROPERTY_KEYID1, keyid1prop[i]);
                insert.setString(row, PROPERTY_KEYID2, keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                insert.setString(row, PROPERTY_KEYID3, keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                insert.setString(row, PROPERTY_RESOURCENUM, resourcenum);
                insert.setString(row, PROPERTY_RESOURCETYPEFLAG, resourceTypeFlag);
                insert.setString(row, PROPERTY_RESOURCEDESC, resourcedescprop[i]);
                insert.setString(row, PROPERTY_ANALYSTTYPE, resourceTypeFlag.equalsIgnoreCase("A") ? "Analyst" : "");
                insert.setString(row, PROPERTY_INSTRUMENTTYPEID, instrumenttypeidprop[i]);
                insert.setString(row, PROPERTY_INSTRUMENTMODELID, instrumentmodelidprop[i]);
                insert.setString(row, PROPERTY_AUTOASSIGNFLAG, autoassignflagprop[i]);
                if (autoassignflagprop[i].equalsIgnoreCase("I")) {
                    if (resourceTypeFlag.equalsIgnoreCase("A")) {
                        insert.setString(row, PROPERTY_AUTOASSIGNANALYSTID, autoassignAnalystprop[i]);
                        insert.setString(row, PROPERTY_AUTOASSIGNINSTRUMENTID, "");
                        insert.setString(row, PROPERTY_AUTOASSIGNDEPARTMENTID, "");
                    } else if (resourceTypeFlag.equalsIgnoreCase("I")) {
                        insert.setString(row, PROPERTY_AUTOASSIGNANALYSTID, "");
                        insert.setString(row, PROPERTY_AUTOASSIGNINSTRUMENTID, autoassignInstrumentprop[i]);
                        insert.setString(row, PROPERTY_AUTOASSIGNDEPARTMENTID, "");
                    }
                } else if (autoassignflagprop[i].equalsIgnoreCase("W")) {
                    insert.setString(row, PROPERTY_AUTOASSIGNANALYSTID, "");
                    insert.setString(row, PROPERTY_AUTOASSIGNINSTRUMENTID, "");
                    insert.setString(row, PROPERTY_AUTOASSIGNDEPARTMENTID, autoassignDepartmentprop[i]);
                } else {
                    insert.setString(row, PROPERTY_AUTOASSIGNANALYSTID, "");
                    insert.setString(row, PROPERTY_AUTOASSIGNINSTRUMENTID, "");
                    insert.setString(row, PROPERTY_AUTOASSIGNDEPARTMENTID, "");
                }
                insert.setString(row, PROPERTY_LINKEDTO, linkedtoprop[i]);
                insert.setString(row, PROPERTY_DURATIONRULE, durationruleprop[i]);
                insert.setString(row, "createby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "createdt", now);
                insert.setString(row, "createtool", this.connectionInfo.getTool());
                insert.setString(row, "usersequence", usersequenceprops.length > i ? usersequenceprops[i] : "");
            }
            DataSetUtil.insert(this.database, insert, PROPERTY_TABLEID);
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }

    private int getMaxRowNum(DataSet ds) {
        int max = 0;
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                int currRownUm = ds.getInt(i, PROPERTY_RESOURCENUM);
                if (currRownUm <= max) continue;
                max = currRownUm;
            }
        }
        return max;
    }

    private void clearIrrelevantData(PropertyList properties) {
        String resourceTypes = properties.getProperty(PROPERTY_RESOURCETYPEFLAG, "");
        String analystTypes = properties.getProperty(PROPERTY_ANALYSTTYPE, "");
        String instTypes = properties.getProperty(PROPERTY_INSTRUMENTTYPEID, "");
        String models = properties.getProperty(PROPERTY_INSTRUMENTMODELID, "");
        String[] typesArr = StringUtil.split(resourceTypes, ";");
        String[] analystTypesArr = StringUtil.split(analystTypes, ";");
        String[] instTypesArr = StringUtil.split(instTypes, ";");
        String[] modelsArr = StringUtil.split(models, ";");
        boolean changed = false;
        for (int i = 0; i < typesArr.length; ++i) {
            String type = typesArr[i];
            if (type.equalsIgnoreCase("A")) {
                changed = true;
                instTypesArr[i] = "";
                modelsArr[i] = "";
                continue;
            }
            if (!type.equalsIgnoreCase("I")) continue;
            changed = true;
            analystTypesArr[i] = "";
        }
        if (changed) {
            properties.setProperty(PROPERTY_INSTRUMENTTYPEID, StringUtil.arrayToString(instTypesArr, ";"));
            properties.setProperty(PROPERTY_INSTRUMENTMODELID, StringUtil.arrayToString(modelsArr, ";"));
            properties.setProperty(PROPERTY_ANALYSTTYPE, StringUtil.arrayToString(analystTypesArr, ";"));
        }
    }
}

