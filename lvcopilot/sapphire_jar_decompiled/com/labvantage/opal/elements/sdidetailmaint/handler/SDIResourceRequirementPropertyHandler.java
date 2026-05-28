/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.modules.wap.actions.AddSDIResourceRequirement;
import com.labvantage.sapphire.modules.wap.actions.DeleteSDIResourceRequirement;
import com.labvantage.sapphire.modules.wap.actions.EditSDIResourceRequirement;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class SDIResourceRequirementPropertyHandler
extends BaseDetailPropertyHandler {
    @Override
    protected void saveData() throws SapphireException {
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem add = this.addActionItem(AddSDIResourceRequirement.class.getName());
        BaseDetailPropertyHandler.ActionItem edit = this.addActionItem(EditSDIResourceRequirement.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            BaseDetailPropertyHandler.ActionItem deleteSDIResourceRequirement = this.addActionItem(DeleteSDIResourceRequirement.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDIResourceRequirement);
                deleteSDIResourceRequirement.put("resourcenum", ds[0]);
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIResourceRequirementPropertyHandler.getCoreColumnsList());
        for (int i = 0; i < elementData.size(); ++i) {
            String resourcenum = elementData.getColumnData(i, "resourcenum");
            String __status = elementData.getColumnData(i, "__status");
            int rowNum = i + 1;
            if (__status.equals("N")) {
                this.putKeysInActionItem(add);
                add.put("resourcenum", resourcenum);
                add.put("resourcetypeflag", elementData.getColumnData(i, "resourcetypeflag"));
                add.put("resourcelabel", elementData.getColumnData(i, "resourcelabel"));
                add.put("analysttype", elementData.getColumnData(i, "analysttype"));
                add.put("instrumenttypeid", elementData.getColumnData(i, "instrumenttypeid"));
                add.put("instrumentmodelid", elementData.getColumnData(i, "instrumentmodelid"));
                add.put("autoassignflag", elementData.getColumnData(i, "autoassignflag"));
                add.put("autoassignanalystid", elementData.getColumnData(i, "autoassignanalystid"));
                add.put("autoassigninstrumentid", elementData.getColumnData(i, "autoassigninstrumentid"));
                add.put("autoassigndepartmentid", elementData.getColumnData(i, "autoassigndepartmentid"));
                add.put("linktocontext", elementData.getColumnData(i, "linktocontext"));
                add.put("durationrule", elementData.getColumnData(i, "durationrule"));
                add.put("usersequence", String.valueOf(rowNum));
                continue;
            }
            if (!__status.equals("E") || editableColumnList.size() <= 0) continue;
            this.putKeysInActionItem(edit);
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                edit.put(columnid, value);
            }
            edit.put("resourcenum", resourcenum);
            edit.put("usersequence", String.valueOf(rowNum));
        }
        this.processActionItems(false);
    }
}

