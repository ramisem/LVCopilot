/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.linkedsdimaint;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LinkedSDIItemPropertyHandler
extends BaseDetailPropertyHandler {
    public static final String PREFIX = "___";
    public static final String LINKELEMENTID_POSTFIX = "_linkelement_";

    @Override
    protected void saveData() throws SapphireException {
        String _eunlink;
        String _eremove;
        String _sdcid = (String)this._ElementProps.get("sdcid");
        String _keycolid1 = (String)this._ElementProps.get("keycolid1");
        String _audit = (String)this._ElementProps.get("audit");
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        List columnList = elementData.getColumnList();
        String autoKeyFlag = (String)this._ElementProps.get("autokeyflag");
        if (autoKeyFlag == null || autoKeyFlag.length() == 0) {
            autoKeyFlag = "N";
        }
        boolean userSequenceFlag = false;
        if (this._TableMD.doesColumnExists("usersequence")) {
            elementData.addColumn("usersequence");
            for (int i = 0; i < elementData.size(); ++i) {
                elementData.setColumnValue(i, "usersequence", Integer.toString(i));
            }
            userSequenceFlag = true;
        }
        BaseDetailPropertyHandler.Pair foreignKey = new BaseDetailPropertyHandler.Pair((String)this._ElementProps.get("foreignkeys"));
        List foreignKeyList = foreignKey.getKeysAsList();
        String _auditReason = "";
        String _auditActivity = "";
        String _auditSignedFlag = "";
        if (_audit.equals("Y")) {
            _auditReason = (String)OpalUtil.parseExtraProps(this._ExtraProps).get("auditreason");
            _auditActivity = (String)OpalUtil.parseExtraProps(this._ExtraProps).get("auditactivity");
            _auditSignedFlag = (String)OpalUtil.parseExtraProps(this._ExtraProps).get("auditsignedflag");
        }
        List statusList = elementData.getDataList("__status");
        ActionBlock actionBlock = new ActionBlock();
        if (statusList != null && statusList.contains("N")) {
            HashMap<String, Object> newMap = new HashMap<String, Object>();
            int copies = -1;
            newMap.put("sdcid", _sdcid);
            if (autoKeyFlag.equals("N")) {
                String keyId1Data = elementData.getColumnDataBuffer(_keycolid1, ";", "N");
                newMap.put("keyid1", keyId1Data);
                copies = StringUtil.split(keyId1Data, ";", true).length;
            }
            if (userSequenceFlag) {
                Iterator userSequenceData = elementData.getColumnDataBuffer("usersequence", ";", "N");
                newMap.put("usersequence", userSequenceData);
                if (copies == -1) {
                    copies = StringUtil.split((String)((Object)userSequenceData), ";", true).length;
                }
            }
            for (Object aColumnList : columnList) {
                String columnName = (String)aColumnList;
                if (columnName == null || columnName.equalsIgnoreCase(_keycolid1) || foreignKeyList.contains(columnName)) continue;
                String columnData = elementData.getColumnDataBuffer(columnName, ";", "N");
                newMap.put(columnName, columnData);
                if (copies != -1) continue;
                copies = StringUtil.split(columnData, ";", true).length;
            }
            if (copies == -1) {
                copies = 1;
            }
            newMap.put("copies", Integer.toString(copies));
            for (String key : foreignKeyList) {
                newMap.put(key, this.getMasterKeyValue(foreignKey.getValue(key)));
            }
            newMap.put("auditreason", _auditReason);
            newMap.put("auditactivity", _auditActivity);
            newMap.put("auditsignedflag", _auditSignedFlag);
            actionBlock.setAction("AddSDI_New", "AddSDI", "1", newMap);
        }
        if (statusList != null && statusList.contains("E")) {
            DataSet dsupdate = new DataSet();
            if (userSequenceFlag) {
                dsupdate.addColumnValues("usersequence", 0, elementData.getColumnDataBuffer("usersequence", ";", "E"), ";");
            }
            for (Object aColumnList : columnList) {
                String columnName = (String)aColumnList;
                if (columnName == null || foreignKeyList.contains(columnName)) continue;
                dsupdate.addColumnValues(columnName, 0, elementData.getColumnDataBuffer(columnName, ";", "E"), ";");
            }
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ").append(this.getSdcProcessor().getProperty(_sdcid, "tableid"));
            sql.append(" where ");
            boolean prependAnd = false;
            Map keys = OpalUtil.string2Map((String)this._ElementProps.get("foreignkeys"));
            SafeSQL safeSQL = new SafeSQL();
            for (String key : keys.keySet()) {
                String value;
                String column = (String)keys.get(key);
                if (!column.equals("keyid1") && !column.equals("keyid2") && !column.equals("keyid3")) continue;
                String string = "keyid1".equals(column) ? this.getKeyid1() : (value = "keyid2".equals(column) ? this.getKeyid2() : this.getKeyid3());
                if (prependAnd) {
                    sql.append(" and ");
                }
                sql.append(key).append(" = ").append(safeSQL.addVar(value));
                prependAnd = true;
            }
            DataSet updateDataSet = new DataSet();
            DataSet currentData = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (currentData != null && currentData.size() > 0) {
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int i = 0; i < dsupdate.size(); ++i) {
                    boolean updateRow = false;
                    filter.put(_keycolid1, dsupdate.getValue(i, _keycolid1));
                    int row = currentData.findRow(filter);
                    if (row == -1) continue;
                    for (int col = 0; col < dsupdate.getColumnCount(); ++col) {
                        String columnid = dsupdate.getColumnId(col);
                        if (_keycolid1.equals(columnid) || updateExcludeColumnList.contains(columnid) || currentData.getValue(row, columnid).equals(dsupdate.getValue(i, columnid))) continue;
                        updateRow = true;
                        break;
                    }
                    if (!updateRow) continue;
                    updateDataSet.copyRow(dsupdate, i, 1);
                }
            }
            if (updateDataSet.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", _sdcid);
                props.setProperty("keyid1", updateDataSet.getColumnValues(_keycolid1, ";"));
                for (int col = 0; col < updateDataSet.getColumnCount(); ++col) {
                    String columnid = updateDataSet.getColumnId(col);
                    if (_keycolid1.equals(columnid) || updateExcludeColumnList.contains(columnid)) continue;
                    for (int i = 0; i < updateDataSet.size(); ++i) {
                        String value = updateDataSet.getValue(i, columnid, "");
                        if (!value.contains("&amp;")) continue;
                        updateDataSet.setValue(i, columnid, value.replace("&amp;", "&"));
                    }
                    props.setProperty(columnid, updateDataSet.getColumnValues(columnid, ";"));
                }
                props.setProperty("auditreason", _auditReason);
                props.setProperty("auditactivity", _auditActivity);
                props.setProperty("auditsignedflag", _auditSignedFlag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (statusList != null && statusList.contains("L")) {
            PropertyList props = new PropertyList();
            props.setProperty("keyid1", elementData.getColumnDataBuffer(_keycolid1, ";", "L"));
            props.setProperty("sdcid", _sdcid);
            if (userSequenceFlag) {
                props.setProperty("usersequence", elementData.getColumnDataBuffer("usersequence", ";", "L"));
            }
            for (String key : foreignKeyList) {
                props.setProperty(key, this.getMasterKeyValue(foreignKey.getValue(key)));
            }
            props.setProperty("auditreason", _auditReason);
            props.setProperty("auditactivity", _auditActivity);
            props.setProperty("auditsignedflag", _auditSignedFlag);
            actionBlock.setAction("EditSDI_Link", "EditSDI", "1", props);
        }
        if ((_eremove = (String)this._ElementProps.get("eremove")) != null && _eremove.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", _sdcid);
            props.setProperty("keyid1", _eremove);
            props.setProperty("auditreason", _auditReason);
            props.setProperty("auditactivity", _auditActivity);
            props.setProperty("auditsignedflag", _auditSignedFlag);
            actionBlock.setAction("DeleteSDI_Delete", "DeleteSDI", "1", props);
        }
        if ((_eunlink = (String)this._ElementProps.get("eunlink")) != null && _eunlink.length() > 0) {
            HashMap<String, String> unlinkMap = new HashMap<String, String>();
            unlinkMap.put("sdcid", _sdcid);
            unlinkMap.put("keyid1", _eunlink);
            for (int j = 0; j < foreignKeyList.size(); ++j) {
                unlinkMap.put((String)foreignKeyList.get(j), (String)null);
            }
            unlinkMap.put("auditreason", _auditReason);
            unlinkMap.put("auditactivity", _auditActivity);
            unlinkMap.put("auditsignedflag", _auditSignedFlag);
            actionBlock.setAction("EditSDI_Unlink", "EditSDI", "1", unlinkMap);
        }
        this.executeActionBlock(actionBlock);
    }
}

