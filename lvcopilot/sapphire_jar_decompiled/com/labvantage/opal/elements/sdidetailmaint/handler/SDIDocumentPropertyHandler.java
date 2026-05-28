/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.documents.AddSDIDocument;
import com.labvantage.sapphire.actions.documents.DeleteSDIDocument;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class SDIDocumentPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 58185 $";

    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addSDIDocument = this.addActionItem(AddSDIDocument.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem deleteSDIDocument = this.addActionItem(DeleteSDIDocument.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDIDocument);
                deleteSDIDocument.put("documentid", ds[0]);
                deleteSDIDocument.put("documentversionid", ds[1]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIDocumentPropertyHandler.getCoreColumnsList());
        HashMap<String, String> updateMap = new HashMap<String, String>();
        for (i = 0; i < elementData.size(); ++i) {
            String documentid = elementData.getColumnData(i, "documentid");
            String documentversionid = elementData.getColumnData(i, "documentversionid");
            String __status = elementData.getColumnData(i, "__status");
            boolean editFlag = false;
            int rowNum = i + 1;
            if (__status.equals("N")) {
                this.putKeysInActionItem(addSDIDocument);
                addSDIDocument.put("documentid", documentid);
                addSDIDocument.put("documentversionid", documentversionid);
                addSDIDocument.put("usersequence", String.valueOf(rowNum));
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
            updateMap.put("documentid", updateMap.containsKey("documentid") ? updateMap.get("documentid") + ";" + documentid : documentid);
            updateMap.put("documentversionid", updateMap.containsKey("documentversionid") ? updateMap.get("documentversionid") + ";" + documentversionid : documentversionid);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? updateMap.get("usersequence") + ";" + String.valueOf(rowNum) : String.valueOf(rowNum));
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            this.updateSDIDetail(updateMap);
        }
    }
}

