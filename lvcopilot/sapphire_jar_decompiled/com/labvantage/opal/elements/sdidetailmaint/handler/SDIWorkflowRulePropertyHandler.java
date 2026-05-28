/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.workflow.AddSDIWorkflowRule;
import com.labvantage.sapphire.actions.workflow.DeleteSDIWorkflowRule;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SDIWorkflowRulePropertyHandler
extends BaseDetailPropertyHandler {
    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem add = this.addActionItem(AddSDIWorkflowRule.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem deleteSDIWorkflowRule = this.addActionItem(DeleteSDIWorkflowRule.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDIWorkflowRule);
                deleteSDIWorkflowRule.put("workflowdefid", ds[0]);
                deleteSDIWorkflowRule.put("workflowdefversionid", ds[1]);
                deleteSDIWorkflowRule.put("workflowdefvariantid", ds[2]);
                deleteSDIWorkflowRule.put("taskdefitemid", ds[3]);
                deleteSDIWorkflowRule.put("ioitemid", ds[4]);
                deleteSDIWorkflowRule.put("workflowexecid", ds[5]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIWorkflowRulePropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        for (i = 0; i < elementData.size(); ++i) {
            String workflowdefid = elementData.getColumnData(i, "workflowdefid");
            String workflowdefversionid = elementData.getColumnData(i, "workflowdefversionid");
            String workflowdefvariantid = elementData.getColumnData(i, "workflowdefvariantid");
            String taskdefitemid = elementData.getColumnData(i, "taskdefitemid");
            String ioitemid = elementData.getColumnData(i, "ioitemid");
            String workflowexecid = elementData.getColumnData(i, "workflowexecid");
            String addmodeflag = elementData.getColumnData(i, "addmodeflag");
            String __status = elementData.getColumnData(i, "__status");
            if ("N".equals(__status)) {
                this.putKeysInActionItem(add);
                add.put("workflowdefid", workflowdefid);
                add.put("workflowdefversionid", workflowdefversionid);
                add.put("workflowdefvariantid", workflowdefvariantid);
                add.put("taskdefitemid", taskdefitemid);
                add.put("ioitemid", ioitemid);
                add.put("workflowexecid", workflowexecid);
                add.put("addmodeflag", addmodeflag);
                continue;
            }
            if (!__status.equals("E") || editableColumnList.size() <= 0) continue;
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                updateMap.put(columnid, updateMap.containsKey(columnid) ? updateMap.get(columnid) + ";" + value : value);
            }
        }
        this.processActionItems(false);
        if (updateMap.size() > 0) {
            this.updateSDIDetail(updateMap);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void update(HashMap updateMap) throws SapphireException {
        DataSet dsupdate = new DataSet(this.connectionInfo);
        for (String columnid : updateMap.keySet()) {
            if (!this._TableMD.doesColumnExists(columnid)) continue;
            String columnType = this._TableMD.getDataType(columnid);
            String value = (String)updateMap.get(columnid);
            if (columnType.equals("C")) {
                dsupdate.addColumnValues(columnid, 0, value, ";");
                continue;
            }
            if (columnType.equals("D")) {
                dsupdate.addColumnValues(columnid, 2, value, ";");
                continue;
            }
            if (!columnType.equals("N")) continue;
            dsupdate.addColumnValues(columnid, 1, value, ";");
        }
        dsupdate.addColumnValues("sdcid", 0, this.getSdcId(), ";");
        dsupdate.addColumnValues("keyid1", 0, this.getKeyid1(), ";");
        dsupdate.addColumnValues("keyid2", 0, this.getKeyid2(), ";");
        dsupdate.addColumnValues("keyid3", 0, this.getKeyid3(), ";");
        dsupdate.padColumn("sdcid");
        dsupdate.padColumn("keyid1");
        dsupdate.padColumn("keyid2");
        dsupdate.padColumn("keyid3");
        DBUtil dbutil = new DBUtil();
        dbutil.setConnection(this.sapphireConnection);
        String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "workflowdefid", "workflowdefversionid", "workflowdefvariantid", "taskdefitemid", "ioitemid", "workflowexecid"};
        try {
            DataSetUtil.update(dbutil, dsupdate, "sdiworkflowrule", keycols);
        }
        finally {
            dbutil.reset();
        }
    }
}

