/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import com.labvantage.sapphire.actions.sdidata.AddReplicate;
import com.labvantage.sapphire.actions.sdidata.DeleteDataItem;
import com.labvantage.sapphire.actions.sdidata.EditDataItem;
import com.labvantage.sapphire.actions.sdidata.EnterDataItem;
import com.labvantage.sapphire.actions.sdidata.ExtendDataSet;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SDIDataitemPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 58185 $";
    private String __Paramlistid;
    private String __Paramlistversionid;
    private String __Variantid;
    private String __Dataset;

    @Override
    protected void saveData() throws SapphireException {
        String removedkeys;
        this.__Paramlistid = (String)this._ElementProps.get("paramlistid");
        this.__Paramlistversionid = (String)this._ElementProps.get("paramlistversionid");
        this.__Variantid = (String)this._ElementProps.get("variantid");
        this.__Dataset = (String)this._ElementProps.get("dataset");
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addDataSet = this.addActionItem(AddDataSet.class.getName());
        BaseDetailPropertyHandler.ActionItem deleteDataitem = this.addActionItem(DeleteDataItem.class.getName());
        BaseDetailPropertyHandler.ActionItem extendDataSet = this.addActionItem(ExtendDataSet.class.getName());
        BaseDetailPropertyHandler.ActionItem addReplicate = this.addActionItem(AddReplicate.class.getName());
        BaseDetailPropertyHandler.ActionItem editParam = null;
        List editableColumnList = elementColumns.getExcludedColumnList(SDIDataitemPropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        if (editableColumnList.contains("enteredtext")) {
            editParam = this.addActionItem(EnterDataItem.class.getName());
        }
        if ((removedkeys = (String)this._ElementProps.get("eremove")) != null && removedkeys.length() > 1) {
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putDataSetInActionItem(deleteDataitem, ds[0], ds[1]);
                deleteDataitem.put("replicateid", ds[2]);
            }
        }
        for (int i = 0; i < elementData.size(); ++i) {
            String paramid = elementData.getColumnData(i, "paramid");
            String paramtype = elementData.getColumnData(i, "paramtype");
            String replicateid = elementData.getColumnData(i, "replicateid");
            String __status = elementData.getColumnData(i, "__status");
            int rowNum = i + 1;
            if (__status.equals("N")) {
                if (replicateid.equals("1")) {
                    this.putDataSetInActionItem(extendDataSet, paramid, paramtype);
                    extendDataSet.put("numreplicate", "1");
                    extendDataSet.put("usersequence", String.valueOf(rowNum));
                    extendDataSet.putColumns(editableColumnList, elementData, i);
                    this.putDataSetInActionItem(addDataSet, paramid, paramtype);
                    addDataSet.put("addnewonly", "Y");
                    addDataSet.putColumns(editableColumnList, elementData, i);
                    if (editParam == null) continue;
                    this.putDataSetInActionItem(editParam, paramid, paramtype);
                    editParam.put("replicateid", replicateid);
                    editParam.put("usersequence", String.valueOf(rowNum));
                    editParam.putColumns(editableColumnList, elementData, i);
                    continue;
                }
                if (this.doesDataitemExists(paramid, paramtype)) {
                    this.putDataSetInActionItem(addReplicate, paramid, paramtype);
                    addReplicate.put("numreplicate", "1");
                    addReplicate.put("usersequence", String.valueOf(rowNum));
                    addReplicate.putColumns(editableColumnList, elementData, i);
                    continue;
                }
                this.putDataSetInActionItem(extendDataSet, paramid, paramtype);
                extendDataSet.put("numreplicate", "1");
                extendDataSet.put("usersequence", String.valueOf(rowNum));
                extendDataSet.putColumns(editableColumnList, elementData, i);
                continue;
            }
            if (!__status.equals("E")) continue;
            if (editableColumnList.size() > 0) {
                for (Object anEditableColumnList : editableColumnList) {
                    String columnid = (String)anEditableColumnList;
                    if (!this._TableMD.doesColumnExists(columnid)) continue;
                    String value = elementData.getColumnData(i, columnid);
                    updateMap.put(columnid, updateMap.containsKey(columnid) ? updateMap.get(columnid) + ";" + value : value);
                }
            }
            updateMap.put("paramlistid", updateMap.containsKey("paramlistid") ? updateMap.get("paramlistid") + ";" + this.__Paramlistid : this.__Paramlistid);
            updateMap.put("paramlistversionid", updateMap.containsKey("paramlistversionid") ? updateMap.get("paramlistversionid") + ";" + this.__Paramlistversionid : this.__Paramlistversionid);
            updateMap.put("variantid", updateMap.containsKey("variantid") ? updateMap.get("variantid") + ";" + this.__Variantid : this.__Variantid);
            updateMap.put("dataset", updateMap.containsKey("dataset") ? updateMap.get("dataset") + ";" + this.__Dataset : this.__Dataset);
            updateMap.put("paramid", updateMap.containsKey("paramid") ? updateMap.get("paramid") + ";" + paramid : paramid);
            updateMap.put("paramtype", updateMap.containsKey("paramtype") ? updateMap.get("paramtype") + ";" + paramtype : paramtype);
            updateMap.put("replicateid", updateMap.containsKey("replicateid") ? updateMap.get("replicateid") + ";" + replicateid : replicateid);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(i) : String.valueOf(i));
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            updateMap.put("__actionclass", EditDataItem.class.getName());
            this.updateSDIDetail(updateMap);
        }
    }

    protected void putDataSetInActionItem(BaseDetailPropertyHandler.ActionItem actionItem, String paramid, String paramtype) {
        this.putKeysInActionItem(actionItem);
        actionItem.put("paramlistid", this.__Paramlistid);
        actionItem.put("paramlistversionid", this.__Paramlistversionid);
        actionItem.put("variantid", this.__Variantid);
        actionItem.put("dataset", this.__Dataset);
        actionItem.put("paramid", paramid);
        actionItem.put("paramtype", paramtype);
    }

    private boolean doesDataitemExists(String paramid, String paramtype) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT PARAMID FROM SDIDATAITEM  WHERE SDCID = " + safeSQL.addVar(this.getSdcId()) + " AND KEYID1 = " + safeSQL.addVar(this.getKeyid1()) + " AND KEYID2 = " + safeSQL.addVar(this.getKeyid2()) + " AND KEYID3 = " + safeSQL.addVar(this.getKeyid3()) + " AND PARAMLISTID = " + safeSQL.addVar(this._ElementProps.get("paramlistid")) + " AND PARAMLISTVERSIONID = " + safeSQL.addVar(this._ElementProps.get("paramlistversionid")) + " AND VARIANTID = " + safeSQL.addVar(this._ElementProps.get("variantid")) + " AND DATASET = " + safeSQL.addVar(this._ElementProps.get("dataset")) + " AND PARAMID = " + safeSQL.addVar(paramid) + " AND PARAMTYPE = " + safeSQL.addVar(paramtype);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds.size() > 0;
    }
}

