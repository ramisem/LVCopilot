/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sdi.AddSDIAddress;
import com.labvantage.sapphire.actions.sdi.DeleteSDIAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SDIAddressPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 58185 $";

    @Override
    protected void saveData() throws SapphireException {
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addSDIAddress = this.addActionItem(AddSDIAddress.class.getName());
        BaseDetailPropertyHandler.ActionItem deleteSDIAddress = this.addActionItem(DeleteSDIAddress.class.getName());
        ArrayList<String> deleteList = new ArrayList<String>();
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                String _addressid = ds[0];
                String _addresstype = ds[1];
                String _contactfunction = ds[2];
                String key = this.getSdcId() + this.getKeyid1() + _addressid + _addresstype + _contactfunction;
                if (deleteList.contains(key)) continue;
                deleteList.add(key);
                this.putKeysInActionItem(deleteSDIAddress);
                deleteSDIAddress.put("addressid", _addressid);
                deleteSDIAddress.put("addresstype", _addresstype);
                deleteSDIAddress.put("contactfunction", _contactfunction);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIAddressPropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        List packageList = null;
        for (int i = 0; i < elementData.size(); ++i) {
            String addressid = elementData.getColumnData(i, "addressid");
            String addresstype = elementData.getColumnData(i, "addresstype");
            String contactfunction = elementData.getColumnData(i, "contactfunction");
            String functiondt = elementData.getColumnData(i, "functiondt");
            String __status = elementData.getColumnData(i, "__status");
            boolean editFlag = false;
            int rowNum = i + 1;
            if (__status.equals("N")) {
                if (this.getSdcId().equals("LV_Package")) {
                    if (packageList == null) {
                        packageList = this.getContactFunctionAddress(this.getSdcId(), this.getKeyid1(), contactfunction);
                        for (int list = 0; list < packageList.size(); ++list) {
                            HashMap map = (HashMap)packageList.get(list);
                            String _addressid = (String)map.get("addressid");
                            String _addresstype = (String)map.get("addresstype");
                            String key = this.getSdcId() + this.getKeyid1() + _addressid + _addresstype + contactfunction;
                            if (deleteList.contains(key)) continue;
                            deleteList.add(key);
                            this.putKeysInActionItem(deleteSDIAddress);
                            deleteSDIAddress.put("addressid", _addressid);
                            deleteSDIAddress.put("addresstype", _addresstype);
                            deleteSDIAddress.put("contactfunction", contactfunction);
                        }
                    }
                    this.putKeysInActionItem(addSDIAddress);
                    addSDIAddress.put("addressid", addressid);
                    addSDIAddress.put("addresstype", addresstype);
                    addSDIAddress.put("contactfunction", contactfunction);
                    addSDIAddress.put("functiondt", functiondt);
                    addSDIAddress.put("usersequence", String.valueOf(rowNum));
                } else {
                    this.putKeysInActionItem(addSDIAddress);
                    addSDIAddress.put("addressid", addressid);
                    addSDIAddress.put("addresstype", addresstype);
                    addSDIAddress.put("contactfunction", contactfunction);
                    addSDIAddress.put("functiondt", functiondt);
                    addSDIAddress.put("usersequence", String.valueOf(rowNum));
                }
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
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(rowNum) : String.valueOf(rowNum));
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            this.updateSDIDetail(updateMap);
        }
    }

    private List getContactFunctionAddress(String sdcId, String keyid1, String contactfunction) {
        SafeSQL safeSQL = new SafeSQL();
        ArrayList list = new ArrayList();
        StringBuilder sql = new StringBuilder();
        sql.append("select addressid, addresstype from sdiaddress");
        sql.append(" where sdcid = ").append(safeSQL.addVar(sdcId));
        sql.append(" and keyid1 = ").append(safeSQL.addVar(keyid1));
        sql.append(" and contactfunction = ").append(safeSQL.addVar(contactfunction));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("addressid", ds.getValue(0, "addressid"));
                map.put("addresstype", ds.getValue(0, "addresstype"));
                list.add(map);
            }
        }
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void update(HashMap updateMap) throws SapphireException {
        DataSet dsupdate = new DataSet(this.connectionInfo);
        for (Object o : updateMap.keySet()) {
            String columnid = (String)o;
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
            if (!columnType.equals("N") && !columnType.equals("R")) continue;
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
        String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "addressid", "addresstype"};
        try {
            DataSetUtil.update(dbutil, dsupdate, "sdiaddress", keycols);
        }
        finally {
            dbutil.reset();
        }
    }
}

