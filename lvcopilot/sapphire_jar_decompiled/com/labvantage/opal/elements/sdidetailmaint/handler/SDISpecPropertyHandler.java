/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.spec.AddSDISpec;
import com.labvantage.sapphire.actions.spec.RemoveSDISpec;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SDISpecPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 62348 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void saveData() throws SapphireException {
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        boolean saveCurrent = "Y".equalsIgnoreCase((String)this._ElementProps.get("savespecversionascurrent"));
        BaseDetailPropertyHandler.ActionItem addSDISpec = this.addActionItem(AddSDISpec.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem removeSDISpec = this.addActionItem(RemoveSDISpec.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(removeSDISpec);
                removeSDISpec.put("specid", ds[0]);
                removeSDISpec.put("specversionid", ds[1]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDISpecPropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        DataSet dsInsert = new DataSet();
        for (int i = 0; i < elementData.size(); ++i) {
            String __autoapplyFlag;
            String specid = elementData.getColumnData(i, "specid");
            String specversionid = elementData.getColumnData(i, "specversionid");
            String specuseType = elementData.getColumnData(i, "specusetype");
            String __status = elementData.getColumnData(i, "__status");
            String __oosgeneratingFlag = elementData.getColumnData(i, "oosgeneratingflag");
            if (__oosgeneratingFlag == null || __oosgeneratingFlag.equals("")) {
                __oosgeneratingFlag = "N";
            }
            if ((__autoapplyFlag = elementData.getColumnData(i, "autoapplyflag")) == null || __autoapplyFlag.trim().equals("")) {
                __autoapplyFlag = "Y".equalsIgnoreCase(__oosgeneratingFlag) || !"Customer".equalsIgnoreCase(specuseType) ? "Y" : "N";
            }
            boolean editFlag = false;
            int rowNum = i + 1;
            if (__status.equals("N")) {
                if (saveCurrent) {
                    int row = dsInsert.addRow();
                    dsInsert.setString(row, "specid", specid);
                    dsInsert.setString(row, "specversionid", specversionid);
                    dsInsert.setNumber(row, "usersequence", rowNum);
                    dsInsert.setString(row, "oosgeneratingflag", __oosgeneratingFlag);
                    dsInsert.setString(row, "appliedflag", "N");
                    dsInsert.setString(row, "autoapplyflag", __autoapplyFlag);
                } else {
                    this.putKeysInActionItem(addSDISpec);
                    addSDISpec.put("specid", specid);
                    addSDISpec.put("specversionid", specversionid);
                    addSDISpec.put("usersequence", String.valueOf(rowNum));
                    editableColumnList.remove("appliedflag");
                    editableColumnList.remove("autoapplyflag");
                    editableColumnList.remove("condition");
                }
                editFlag = true;
            } else if (__status.equals("E")) {
                editFlag = true;
            }
            if (!editFlag || editableColumnList.size() <= 0) continue;
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                updateMap.put(columnid, updateMap.containsKey(columnid) ? updateMap.get(columnid) + ";" + value : value);
            }
            updateMap.put("specid", updateMap.containsKey("specid") ? updateMap.get("specid") + ";" + specid : specid);
            updateMap.put("specversionid", updateMap.containsKey("specversionid") ? updateMap.get("specversionid") + ";" + specversionid : specversionid);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(rowNum) : String.valueOf(rowNum));
        }
        if (saveCurrent && dsInsert.getRowCount() > 0) {
            dsInsert.addColumnValues("sdcid", 0, this.getSdcId(), ";");
            dsInsert.addColumnValues("keyid1", 0, this.getKeyid1(), ";");
            dsInsert.addColumnValues("keyid2", 0, this.getKeyid2(), ";");
            dsInsert.addColumnValues("keyid3", 0, this.getKeyid3(), ";");
            dsInsert.addColumnValues("createdt", 2, "n", ";");
            dsInsert.addColumnValues("createby", 0, this.connectionInfo.getSysuserId(), ";");
            dsInsert.padColumn("sdcid");
            dsInsert.padColumn("keyid1");
            dsInsert.padColumn("keyid2");
            dsInsert.padColumn("keyid3");
            dsInsert.padColumn("createdt");
            dsInsert.padColumn("createby");
            DBUtil dbutil = new DBUtil();
            dbutil.setConnection(this.sapphireConnection);
            try {
                DataSetUtil.insert(dbutil, dsInsert, "sdispec");
            }
            finally {
                dbutil.reset();
            }
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            if (updateMap.containsKey("specversionid") && !saveCurrent) {
                String specVersionIds = (String)updateMap.get("specversionid");
                String userSequence = (String)updateMap.get("usersequence");
                updateMap.put("specversionid", this.resolveCurrentVersion(specVersionIds, userSequence, addSDISpec));
            }
            this.updateSDIDetail(updateMap);
        }
    }

    private String resolveCurrentVersion(String specVersionString, String findString, BaseDetailPropertyHandler.ActionItem addSDIPropsItem) {
        StringBuffer resolvedVersionBuffer = new StringBuffer();
        if (specVersionString != null) {
            String[] findStringArray;
            String[] specVersionIdsArray = StringUtil.split(specVersionString, ";");
            if (specVersionIdsArray.length == (findStringArray = StringUtil.split(findString, ";")).length) {
                for (int i = 0; i < specVersionIdsArray.length; ++i) {
                    if (specVersionIdsArray[i].equals("C")) {
                        HashMap actionPropsFound = (HashMap)addSDIPropsItem.findRow("usersequence", findStringArray[i]);
                        String resolvedVersion = (String)actionPropsFound.get("specversionid");
                        if (resolvedVersion != null && resolvedVersion.length() > 0) {
                            resolvedVersionBuffer.append(";").append(resolvedVersion);
                            continue;
                        }
                        resolvedVersionBuffer.append(";").append(specVersionIdsArray[i]);
                        continue;
                    }
                    resolvedVersionBuffer.append(";").append(specVersionIdsArray[i]);
                }
            } else {
                this.logError("Invalid specversionid or usersequence");
            }
        }
        if (resolvedVersionBuffer.length() > 0) {
            return resolvedVersionBuffer.substring(1);
        }
        return specVersionString;
    }
}

