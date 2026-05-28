/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.ArrayList;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SyncASLArrayType
extends BaseAction {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String columnid;
        PropertyList props;
        int row;
        PropertyList oldGridTypeList = new PropertyList();
        oldGridTypeList.setPropertyList(actionProps.getProperty("__oldgridtypelist"));
        Map<String, PropertyList> newGridTypeMap = StorageUnitTypeDef.getInstance().getTypeMap(this.getQueryProcessor(), "Grid");
        DataSet dsAdd = new DataSet();
        DataSet dsEdit = new DataSet();
        DataSet dsDelete = new DataSet();
        ArrayList<String> aslArrayTypeList = new ArrayList<String>();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select arraytypeid from arraytype where aslflag = ?", (Object[])new String[]{"Y"});
        for (int i = 0; i < ds.size(); ++i) {
            aslArrayTypeList.add(ds.getString(i, "arraytypeid"));
        }
        for (String nodeid : newGridTypeMap.keySet()) {
            if (!aslArrayTypeList.contains("ASL " + nodeid)) {
                PropertyList gridProperty = newGridTypeMap.get(nodeid);
                row = dsAdd.addRow();
                dsAdd.setString(row, "arraytypeid", "ASL " + nodeid);
                dsAdd.setString(row, "arraytypeversionid", "1");
                dsAdd.setString(row, "arraytypedesc", "Grid Type " + nodeid);
                dsAdd.setString(row, "classification", "ASL");
                dsAdd.setString(row, "numrows", gridProperty.getProperty("rows"));
                dsAdd.setString(row, "numcolumns", gridProperty.getProperty("columns"));
                dsAdd.setString(row, "horizontallabeltype", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("type"));
                dsAdd.setString(row, "horizontallabelstart", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("startat"));
                dsAdd.setString(row, "horizontallabeldirection", gridProperty.getPropertyListNotNull("indexorder").getProperty("horizontal").equals("Left->Right") ? "LR" : "RL");
                dsAdd.setString(row, "verticallabeltype", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("type"));
                dsAdd.setString(row, "verticallabelstart", gridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("startat"));
                dsAdd.setString(row, "verticallabeldirection", gridProperty.getPropertyListNotNull("indexorder").getProperty("vertical").equals("Top->Bottom") ? "TB" : "BT");
                dsAdd.setString(row, "activeflag", "Y");
                dsAdd.setString(row, "versionstatus", "C");
                dsAdd.setString(row, "aslflag", "Y");
                continue;
            }
            if (!oldGridTypeList.isPropertyList(nodeid)) continue;
            boolean isModified = false;
            PropertyList oldGridProperty = oldGridTypeList.getPropertyListNotNull(nodeid);
            PropertyList newGridProperty = newGridTypeMap.get(nodeid);
            if (!oldGridProperty.getProperty("rows").equals(newGridProperty.getProperty("rows"))) {
                isModified = true;
            } else if (!oldGridProperty.getProperty("columns").equals(newGridProperty.getProperty("columns"))) {
                isModified = true;
            } else if (!oldGridProperty.getPropertyListNotNull("indexorder").getProperty("horizontal").equals(newGridProperty.getPropertyListNotNull("indexorder").getProperty("horizontal"))) {
                isModified = true;
            } else if (!oldGridProperty.getPropertyListNotNull("indexorder").getProperty("vertical").equals(newGridProperty.getPropertyListNotNull("indexorder").getProperty("vertical"))) {
                isModified = true;
            } else {
                PropertyList oldLabelGenRuleHorizontal = oldGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule");
                PropertyList oldLabelGenRuleVertical = oldGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule");
                PropertyList newLabelGenRuleHorizontal = newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule");
                PropertyList newLabelGenRuleVertical = newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule");
                if (!oldLabelGenRuleHorizontal.getProperty("type").equals(newLabelGenRuleHorizontal.getProperty("type"))) {
                    isModified = true;
                } else if (!oldLabelGenRuleHorizontal.getProperty("startat").equals(newLabelGenRuleHorizontal.getProperty("startat"))) {
                    isModified = true;
                } else if (!oldLabelGenRuleVertical.getProperty("type").equals(newLabelGenRuleVertical.getProperty("type"))) {
                    isModified = true;
                } else if (!oldLabelGenRuleVertical.getProperty("startat").equals(newLabelGenRuleVertical.getProperty("startat"))) {
                    isModified = true;
                }
            }
            if (!isModified) continue;
            int row2 = dsEdit.addRow();
            dsEdit.setString(row2, "keyid1", "ASL " + nodeid);
            dsEdit.setString(row2, "keyid2", "1");
            dsEdit.setString(row2, "arraytypedesc", "Grid Type " + nodeid);
            dsEdit.setString(row2, "classification", "ASL");
            dsEdit.setString(row2, "numrows", newGridProperty.getProperty("rows"));
            dsEdit.setString(row2, "numcolumns", newGridProperty.getProperty("columns"));
            dsEdit.setString(row2, "horizontallabeltype", newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("type"));
            dsEdit.setString(row2, "horizontallabelstart", newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("horizontallabelgenrule").getProperty("startat"));
            dsEdit.setString(row2, "horizontallabeldirection", newGridProperty.getPropertyListNotNull("indexorder").getProperty("horizontal").equals("Left->Right") ? "LR" : "RL");
            dsEdit.setString(row2, "verticallabeltype", newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("type"));
            dsEdit.setString(row2, "verticallabelstart", newGridProperty.getPropertyListNotNull("labelgenrule").getPropertyListNotNull("verticallabelgenrule").getProperty("startat"));
            dsEdit.setString(row2, "verticallabeldirection", newGridProperty.getPropertyListNotNull("indexorder").getProperty("vertical").equals("Top->Bottom") ? "TB" : "BT");
            dsEdit.setString(row2, "activeflag", "Y");
            dsEdit.setString(row2, "versionstatus", "C");
            dsEdit.setString(row2, "aslflag", "Y");
        }
        for (String arraytypeid : aslArrayTypeList) {
            String nodeid = arraytypeid.startsWith("ASL ") ? arraytypeid.substring(4) : arraytypeid;
            if (newGridTypeMap.containsKey(nodeid)) continue;
            row = dsDelete.addRow();
            dsDelete.setString(row, "keyid1", arraytypeid);
            dsDelete.setString(row, "keyid2", "1");
        }
        if (dsEdit.size() > 0) {
            props = new PropertyList();
            props.setProperty("sdcid", "LV_ArrayType");
            for (int col = 0; col < dsEdit.getColumnCount(); ++col) {
                columnid = dsEdit.getColumnId(col);
                props.setProperty(columnid, dsEdit.getColumnValues(columnid, ";"));
            }
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (dsAdd.size() > 0) {
            props = new PropertyList();
            props.setProperty("sdcid", "LV_ArrayType");
            for (int col = 0; col < dsAdd.getColumnCount(); ++col) {
                columnid = dsAdd.getColumnId(col);
                props.setProperty(columnid, dsAdd.getColumnValues(columnid, ";"));
            }
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
        }
        if (dsDelete.size() > 0) {
            props = new PropertyList();
            props.setProperty("sdcid", "LV_ArrayType");
            props.setProperty("keyid1", dsDelete.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsDelete.getColumnValues("keyid2", ";"));
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }
    }
}

