/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.eln.AddSDIWorksheetRule;
import com.labvantage.sapphire.actions.eln.DeleteSDIWorksheetRule;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SDIWorksheetRulePropertyHandler
extends BaseDetailPropertyHandler {
    protected static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    protected void saveData() throws SapphireException {
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem add = this.addActionItem(AddSDIWorksheetRule.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem deleteSDIWorksheetRule = this.addActionItem(DeleteSDIWorksheetRule.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDIWorksheetRule);
                deleteSDIWorksheetRule.put("worksheetid", ds[0]);
                deleteSDIWorksheetRule.put("worksheetinstance", ds[1]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIWorksheetRulePropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        DataSet maxinstance = this.getQueryProcessor().getPreparedSqlDataSet("SELECT " + (this.connectionInfo.isOracle() ? "nvl( max( to_number( worksheetinstance ) ) + 1, 0 )" : "isnull( max( cast( worksheetinstance AS Integer ) ) + 1, 0 )") + " max  FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{this.getSdcId(), this.getKeyid1(), this.getKeyid2(), this.getKeyid3()});
        int instance = maxinstance.size() == 0 ? 0 : maxinstance.getInt(0, "max");
        for (int i = 0; i < elementData.size(); ++i) {
            String worksheetid = elementData.getColumnData(i, "worksheetid");
            String worksheetversionid = elementData.getColumnData(i, "worksheetversionid");
            String worksheetinstance = elementData.getColumnData(i, "worksheetinstance");
            String __status = elementData.getColumnData(i, "__status");
            boolean editFlag = false;
            int rowNum = i + 1;
            if (__status.equals("N")) {
                this.putKeysInActionItem(add);
                List<String> versions = OpalUtil.toList(worksheetversionid, ";");
                for (int j = 0; j < versions.size(); ++j) {
                    String version = versions.get(j);
                    if (!"C".equalsIgnoreCase(version) && !"(null)".equals(version) && version.trim().length() != 0) continue;
                    versions.set(j, "");
                }
                worksheetversionid = OpalUtil.toDelimitedString(versions, ";");
                add.put("worksheetid", worksheetid);
                add.put("worksheetversionid", worksheetversionid);
                add.put("worksheetinstance", String.valueOf(instance));
                ++instance;
                add.put("workbookid", elementData.getColumnData(i, "workbookid"));
                add.put("workbookversionid", elementData.getColumnData(i, "workbookversionid"));
                add.put("worksheetrule", elementData.getColumnData(i, "worksheetrule"));
                add.put("maxsdiperworksheet", elementData.getColumnData(i, "maxsdiperworksheet"));
                add.put("authorflag", elementData.getColumnData(i, "authorflag"));
                add.put("createflag", elementData.getColumnData(i, "createflag"));
                add.put("usersequence", String.valueOf(rowNum));
            } else if (__status.equals("E")) {
                editFlag = true;
            }
            if (!editFlag || editableColumnList.size() <= 0) continue;
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                if (updateMap.containsKey(columnid)) {
                    updateMap.put(columnid, (String)updateMap.get(columnid) + ";" + value);
                    continue;
                }
                updateMap.put(columnid, value);
            }
            if (updateMap.containsKey("worksheetid")) {
                updateMap.put("worksheetid", (String)updateMap.get("worksheetid") + ";" + worksheetid);
            } else {
                updateMap.put("worksheetid", worksheetid);
            }
            if (updateMap.containsKey("worksheetinstance")) {
                updateMap.put("worksheetinstance", (String)updateMap.get("worksheetinstance") + ";" + worksheetinstance);
            } else {
                updateMap.put("worksheetinstance", worksheetinstance);
            }
            if (updateMap.containsKey("usersequence")) {
                updateMap.put("usersequence", (String)updateMap.get("usersequence") + ";" + String.valueOf(rowNum));
                continue;
            }
            updateMap.put("usersequence", String.valueOf(rowNum));
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
        DataSet dsupdate = new DataSet();
        for (Object o : updateMap.keySet()) {
            String columnid = (String)o;
            if (!this._TableMD.doesColumnExists(columnid)) continue;
            String columnType = this._TableMD.getDataType(columnid);
            String value = (String)updateMap.get(columnid);
            switch (columnType) {
                case "C": {
                    dsupdate.addColumnValues(columnid, 0, value, ";");
                    break;
                }
                case "D": {
                    dsupdate.addColumnValues(columnid, 2, value, ";");
                    break;
                }
                case "N": {
                    dsupdate.addColumnValues(columnid, 1, value, ";");
                }
            }
        }
        dsupdate.setString(-1, "sdcid", this.getSdcId());
        dsupdate.setString(-1, "keyid1", this.getKeyid1());
        dsupdate.setString(-1, "keyid2", this.getKeyid2());
        dsupdate.setString(-1, "keyid3", this.getKeyid3());
        DBUtil dbutil = new DBUtil();
        dbutil.setConnection(this.sapphireConnection);
        String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "worksheetid", "worksheetinstance"};
        for (int i = 0; i < dsupdate.getRowCount(); ++i) {
            String worksheetversionid = dsupdate.getString(i, "worksheetversionid", "");
            if (!worksheetversionid.equalsIgnoreCase("C") && !worksheetversionid.equalsIgnoreCase("(null)") && worksheetversionid.trim().length() != 0) continue;
            dsupdate.setString(i, "worksheetversionid", "");
        }
        try {
            DataSetUtil.update(dbutil, dsupdate, "sdiworksheetrule", keycols);
        }
        finally {
            dbutil.reset();
        }
    }
}

