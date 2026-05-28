/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.form.AddSDIFormRule;
import com.labvantage.sapphire.actions.form.DeleteSDIFormRule;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class SDIFormRulePropertyHandler
extends BaseDetailPropertyHandler {
    protected static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem add = this.addActionItem(AddSDIFormRule.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem deleteSDIFormRule = this.addActionItem(DeleteSDIFormRule.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDIFormRule);
                deleteSDIFormRule.put("formid", ds[0]);
                deleteSDIFormRule.put("forminstance", ds[1]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIFormRulePropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        for (i = 0; i < elementData.size(); ++i) {
            String formid = elementData.getColumnData(i, "formid");
            String formversionid = elementData.getColumnData(i, "formversionid");
            String forminstance = elementData.getColumnData(i, "forminstance");
            String __status = elementData.getColumnData(i, "__status");
            boolean editFlag = false;
            int rowNum = i + 1;
            if (__status.equals("N")) {
                this.putKeysInActionItem(add);
                List<String> versions = OpalUtil.toList(formversionid, ";");
                for (int j = 0; j < versions.size(); ++j) {
                    String version = versions.get(j);
                    if (!"C".equalsIgnoreCase(version) && !"(null)".equals(version) && version.trim().length() != 0) continue;
                    versions.set(j, "");
                }
                formversionid = OpalUtil.toDelimitedString(versions, ";");
                add.put("formid", formid);
                add.put("formversionid", formversionid);
                add.put("forminstance", forminstance);
                add.put("formrule", elementData.getColumnData(i, "formrule"));
                add.put("mandatoryflag", elementData.getColumnData(i, "mandatoryflag"));
                add.put("usersequence", String.valueOf(rowNum));
            } else if (__status.equals("E")) {
                editFlag = true;
            }
            if (!editFlag || editableColumnList.size() <= 0) continue;
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                updateMap.put(columnid, updateMap.containsKey(columnid) ? (String)updateMap.get(columnid) + ";" + value : value);
            }
            updateMap.put("formid", updateMap.containsKey("formid") ? (String)updateMap.get("formid") + ";" + formid : formid);
            updateMap.put("forminstance", updateMap.containsKey("forminstance") ? (String)updateMap.get("forminstance") + ";" + forminstance : forminstance);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? (String)updateMap.get("usersequence") + ";" + rowNum : String.valueOf(rowNum));
        }
        this.processActionItems(false);
        if (updateMap.size() > 0) {
            this.updateSDIDetail(updateMap);
        }
    }
}

