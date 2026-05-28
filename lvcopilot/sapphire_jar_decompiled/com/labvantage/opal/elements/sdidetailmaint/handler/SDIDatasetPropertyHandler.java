/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import com.labvantage.sapphire.actions.sdidata.CopyDataSet;
import com.labvantage.sapphire.actions.sdidata.DeleteDataSet;
import com.labvantage.sapphire.actions.sdidata.EditDataSet;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SDIDatasetPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 58185 $";

    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addDataset = this.addActionItem(AddDataSet.class.getName());
        BaseDetailPropertyHandler.ActionItem newEditDataset = this.addActionItem(EditDataSet.class.getName());
        BaseDetailPropertyHandler.ActionItem copyDataset = this.addActionItem(CopyDataSet.class.getName());
        BaseDetailPropertyHandler.ActionItem remeasureDataset = this.addActionItem("RemeasureDataSet", "1");
        BaseDetailPropertyHandler.ActionItem retestDataset = this.addActionItem("RetestDataSet", "1");
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 0) {
            BaseDetailPropertyHandler.ActionItem deleteDataset = this.addActionItem(DeleteDataSet.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteDataset);
                deleteDataset.put("paramlistid", ds[0]);
                deleteDataset.put("paramlistversionid", ds[1]);
                deleteDataset.put("variantid", ds[2]);
                deleteDataset.put("dataset", ds[3]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIDatasetPropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        for (i = 0; i < elementData.size(); ++i) {
            String paramlistid = elementData.getColumnData(i, "paramlistid");
            String paramlistversionid = elementData.getColumnData(i, "paramlistversionid");
            String variantid = elementData.getColumnData(i, "variantid");
            String dataset = elementData.getColumnData(i, "dataset");
            String __status = elementData.getColumnData(i, "__status");
            if (__status == null || __status.length() == 0 || __status.equals("S") || __status.equals("C")) continue;
            if (__status.equals("N")) {
                this.putKeysInActionItem(addDataset);
                addDataset.put("paramlistid", paramlistid);
                addDataset.put("paramlistversionid", paramlistversionid);
                addDataset.put("variantid", variantid);
                addDataset.put("dataset", dataset);
                addDataset.put("usersequence", String.valueOf(i));
                addDataset.put("s_datasetstatus", "Initial");
                addDataset.putColumns(editableColumnList, elementData, i);
                continue;
            }
            if (__status.equals("E")) {
                if (editableColumnList.size() > 0) {
                    for (Object anEditableColumnList : editableColumnList) {
                        String columnid = (String)anEditableColumnList;
                        if (!this._TableMD.doesColumnExists(columnid)) continue;
                        String value = elementData.getColumnData(i, columnid);
                        updateMap.put(columnid, updateMap.containsKey(columnid) ? updateMap.get(columnid) + ";" + value : value);
                    }
                }
                updateMap.put("paramlistid", updateMap.containsKey("paramlistid") ? updateMap.get("paramlistid") + ";" + paramlistid : paramlistid);
                updateMap.put("paramlistversionid", updateMap.containsKey("paramlistversionid") ? updateMap.get("paramlistversionid") + ";" + paramlistversionid : paramlistversionid);
                updateMap.put("variantid", updateMap.containsKey("variantid") ? updateMap.get("variantid") + ";" + variantid : variantid);
                updateMap.put("dataset", updateMap.containsKey("dataset") ? updateMap.get("dataset") + ";" + dataset : dataset);
                updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(i) : String.valueOf(i));
                continue;
            }
            if (__status.equals("RM")) {
                this.putKeysInActionItem(remeasureDataset);
                remeasureDataset.put("paramlistid", paramlistid);
                remeasureDataset.put("paramlistversionid", paramlistversionid);
                remeasureDataset.put("variantid", variantid);
                remeasureDataset.put("dataset", dataset);
                remeasureDataset.put("olddsstatus", "Remeasured");
                remeasureDataset.put("newdsstatus", "Initial");
                continue;
            }
            if (__status.equals("RT")) {
                this.putKeysInActionItem(retestDataset);
                retestDataset.put("paramlistid", paramlistid);
                retestDataset.put("paramlistversionid", paramlistversionid);
                retestDataset.put("variantid", variantid);
                retestDataset.put("dataset", dataset);
                retestDataset.put("olddsstatus", "Retested");
                retestDataset.put("newdsstatus", "Initial");
                continue;
            }
            try {
                Integer.parseInt(__status);
                this.putKeysInActionItem(copyDataset);
                copyDataset.put("paramlistid", paramlistid);
                copyDataset.put("paramlistversionid", paramlistversionid);
                copyDataset.put("variantid", variantid);
                copyDataset.put("dataset", __status);
                continue;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (addDataset.size() > 0) {
            this.handleTemplate(addDataset);
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            updateMap.put("__actionclass", EditDataSet.class.getName());
            this.updateSDIDetail(updateMap);
        }
    }

    private void handleTemplate(BaseDetailPropertyHandler.ActionItem addDataset) {
        DataSet ds;
        SDCProcessor sdcProcessor = this.getSdcProcessor();
        String sdcId = this.getSdcId();
        String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcId, "keycolid3");
        String table = sdcProcessor.getProperty(sdcId, "tableid");
        String keyid1 = this.getKeyid1();
        String keyid2 = this.getKeyid2();
        String keyid3 = this.getKeyid3();
        StringBuffer sql = new StringBuffer("SELECT ");
        SafeSQL safeSQL = new SafeSQL();
        sql.append(keycolid1).append(", ");
        if (StringUtil.getLen(keycolid2) > 0L) {
            sql.append(keycolid2).append(", ");
        }
        if (StringUtil.getLen(keycolid3) > 0L) {
            sql.append(keycolid3).append(", ");
        }
        sql.append("templateflag");
        sql.append(" FROM ").append(table);
        sql.append(" WHERE ");
        sql.append(keycolid1).append(" = ");
        sql.append(safeSQL.addVar(keyid1)).append("");
        if (StringUtil.getLen(keycolid2) > 0L) {
            sql.append(" AND ").append(keycolid2).append(" = ");
            sql.append(safeSQL.addVar(keyid2)).append("");
        }
        if (StringUtil.getLen(keycolid3) > 0L) {
            sql.append(" AND ").append(keycolid3).append(" = ");
            sql.append(safeSQL.addVar(keyid3)).append("");
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet("gettemplateflag", sql.toString(), safeSQL.getValues())).getRowCount() > 0 && StringUtil.getYN(ds.getValue(0, "templateflag"), "N").equals("Y")) {
            addDataset.put("createworksheet", "N");
        }
    }
}

