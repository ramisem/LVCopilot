/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditSDIResourceRequirement
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
        String delimeter = properties.getProperty(PROPERTY_SEPARATOR, properties.getProperty("delimeter", ";"));
        if (properties.getProperty(PROPERTY_SDCID).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty(PROPERTY_KEYID1).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        if (properties.getProperty(PROPERTY_KEYID2).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid2 specified.");
        }
        if (properties.getProperty(PROPERTY_RESOURCENUM).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Resource Num specified.");
        }
        this.clearIrrelevantData(properties);
        Calendar now = DateTimeUtil.getNowCalendar();
        DataSet props = new DataSet();
        props.addColumnValues(PROPERTY_SDCID, 0, properties.getProperty(PROPERTY_SDCID), delimeter);
        props.addColumnValues(PROPERTY_KEYID1, 0, properties.getProperty(PROPERTY_KEYID1), delimeter);
        props.addColumnValues(PROPERTY_KEYID2, 0, properties.getProperty(PROPERTY_KEYID2), delimeter);
        props.addColumnValues(PROPERTY_KEYID3, 0, properties.getProperty(PROPERTY_KEYID3), delimeter, "(null)");
        props.addColumnValues(PROPERTY_RESOURCENUM, 0, properties.getProperty(PROPERTY_RESOURCENUM), delimeter);
        props.padColumns();
        props.addColumnValues(PROPERTY_RESOURCETYPEFLAG, 0, properties.getProperty(PROPERTY_RESOURCETYPEFLAG), delimeter);
        props.addColumnValues(PROPERTY_INSTRUMENTTYPEID, 0, properties.getProperty(PROPERTY_INSTRUMENTTYPEID), delimeter);
        props.addColumnValues(PROPERTY_INSTRUMENTMODELID, 0, properties.getProperty(PROPERTY_INSTRUMENTMODELID), delimeter);
        props.addColumnValues(PROPERTY_RESOURCEDESC, 0, properties.getProperty(PROPERTY_RESOURCEDESC), delimeter);
        props.addColumnValues(PROPERTY_AUTOASSIGNFLAG, 0, properties.getProperty(PROPERTY_AUTOASSIGNFLAG), delimeter);
        props.addColumnValues(PROPERTY_AUTOASSIGNANALYSTID, 0, properties.getProperty(PROPERTY_AUTOASSIGNANALYSTID), delimeter);
        props.addColumnValues(PROPERTY_AUTOASSIGNINSTRUMENTID, 0, properties.getProperty(PROPERTY_AUTOASSIGNINSTRUMENTID), delimeter);
        props.addColumnValues(PROPERTY_AUTOASSIGNDEPARTMENTID, 0, properties.getProperty(PROPERTY_AUTOASSIGNDEPARTMENTID), delimeter);
        props.addColumnValues(PROPERTY_LINKEDTO, 0, properties.getProperty(PROPERTY_LINKEDTO), delimeter);
        props.addColumnValues(PROPERTY_DURATIONRULE, 0, properties.getProperty(PROPERTY_DURATIONRULE), delimeter);
        props.addColumnValues("usersequence", 0, properties.getProperty("usersequence"), delimeter);
        props.setDate(-1, "moddt", now);
        props.setString(-1, "modtool", this.connectionInfo.getTool());
        props.setString(-1, "modby", this.connectionInfo.getSysuserId());
        DataSetUtil.update(this.database, props, PROPERTY_TABLEID, new String[]{PROPERTY_SDCID, PROPERTY_KEYID1, PROPERTY_KEYID2, PROPERTY_KEYID3, PROPERTY_RESOURCENUM});
    }

    private void clearIrrelevantData(PropertyList properties) {
        String resourceTypes = properties.getProperty(PROPERTY_RESOURCETYPEFLAG, "");
        String analystTypes = properties.getProperty(PROPERTY_ANALYSTTYPE, "");
        String instTypes = properties.getProperty(PROPERTY_INSTRUMENTTYPEID, "");
        String models = properties.getProperty(PROPERTY_INSTRUMENTMODELID, "");
        String[] resourceTypesArr = StringUtil.split(resourceTypes, ";");
        String[] analystTypesArr = StringUtil.split(analystTypes, ";");
        String[] instTypesArr = StringUtil.split(instTypes, ";");
        String[] modelsArr = StringUtil.split(models, ";");
        String[] autoAssignFlagArr = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNFLAG, ""), ";");
        String[] autoAssignAnalystArr = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNANALYSTID, ""), ";");
        String[] autoAssignInstrtArr = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNINSTRUMENTID, ""), ";");
        String[] autoAssignDeptArr = StringUtil.split(properties.getProperty(PROPERTY_AUTOASSIGNDEPARTMENTID, ""), ";");
        boolean intruChanged = false;
        boolean analysttypeChanged = false;
        boolean autoAnalystChanged = false;
        boolean autoInstChanged = false;
        boolean autoDeptChanged = false;
        boolean autoAssignFlagChanged = false;
        for (int i = 0; i < resourceTypesArr.length; ++i) {
            String resourceType = resourceTypesArr[i];
            if (resourceType.length() == 0) {
                instTypesArr[i] = "";
                modelsArr[i] = "";
                analystTypesArr[i] = "";
                autoAssignAnalystArr[i] = "";
                autoAssignInstrtArr[i] = "";
                autoAssignDeptArr[i] = "";
                autoAssignFlagArr[i] = "";
                intruChanged = true;
                autoAnalystChanged = true;
                autoInstChanged = true;
                autoDeptChanged = true;
                autoAssignFlagChanged = true;
                continue;
            }
            if (resourceType.equalsIgnoreCase("I")) {
                analystTypesArr[i] = "";
                analysttypeChanged = true;
            } else if (resourceType.equalsIgnoreCase("A")) {
                instTypesArr[i] = "";
                modelsArr[i] = "";
                intruChanged = true;
            }
            if (autoAssignFlagArr[i].equalsIgnoreCase("I")) {
                if (resourceType.equalsIgnoreCase("A")) {
                    autoAssignInstrtArr[i] = "";
                    autoAssignDeptArr[i] = "";
                    autoInstChanged = true;
                    autoDeptChanged = true;
                    continue;
                }
                if (resourceType.equalsIgnoreCase("I")) {
                    autoAssignAnalystArr[i] = "";
                    autoAssignDeptArr[i] = "";
                    autoAnalystChanged = true;
                    autoDeptChanged = true;
                    continue;
                }
                autoAssignAnalystArr[i] = "";
                autoAssignInstrtArr[i] = "";
                autoAssignDeptArr[i] = "";
                autoAnalystChanged = true;
                autoInstChanged = true;
                autoDeptChanged = true;
                continue;
            }
            if (autoAssignFlagArr[i].equalsIgnoreCase("W")) {
                if (resourceType.equalsIgnoreCase("A") || resourceType.equalsIgnoreCase("I")) {
                    autoAssignAnalystArr[i] = "";
                    autoAssignInstrtArr[i] = "";
                    autoAnalystChanged = true;
                    autoInstChanged = true;
                    continue;
                }
                autoAssignAnalystArr[i] = "";
                autoAssignInstrtArr[i] = "";
                autoAssignDeptArr[i] = "";
                autoAnalystChanged = true;
                autoInstChanged = true;
                autoDeptChanged = true;
                continue;
            }
            autoAssignAnalystArr[i] = "";
            autoAssignInstrtArr[i] = "";
            autoAssignDeptArr[i] = "";
            autoAnalystChanged = true;
            autoInstChanged = true;
            autoDeptChanged = true;
        }
        if (intruChanged) {
            properties.setProperty(PROPERTY_INSTRUMENTTYPEID, StringUtil.arrayToString(instTypesArr, ";"));
            properties.setProperty(PROPERTY_INSTRUMENTMODELID, StringUtil.arrayToString(modelsArr, ";"));
        }
        if (analysttypeChanged) {
            properties.setProperty(PROPERTY_ANALYSTTYPE, StringUtil.arrayToString(analystTypesArr, ";"));
        }
        if (autoAnalystChanged) {
            properties.setProperty(PROPERTY_AUTOASSIGNANALYSTID, StringUtil.arrayToString(autoAssignAnalystArr, ";"));
        }
        if (autoInstChanged) {
            properties.setProperty(PROPERTY_AUTOASSIGNINSTRUMENTID, StringUtil.arrayToString(autoAssignInstrtArr, ";"));
        }
        if (autoDeptChanged) {
            properties.setProperty(PROPERTY_AUTOASSIGNDEPARTMENTID, StringUtil.arrayToString(autoAssignDeptArr, ";"));
        }
        if (autoAssignFlagChanged) {
            properties.setProperty(PROPERTY_AUTOASSIGNFLAG, StringUtil.arrayToString(autoAssignFlagArr, ";"));
        }
    }
}

