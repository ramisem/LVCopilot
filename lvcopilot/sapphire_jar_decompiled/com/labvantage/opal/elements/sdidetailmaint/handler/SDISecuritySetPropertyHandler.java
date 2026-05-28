/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.sdi.AddSDISecuritySet;
import com.labvantage.sapphire.actions.sdi.DeleteSDISecuritySet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class SDISecuritySetPropertyHandler
extends BaseDetailPropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65154 $";

    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addSDISecuritySet = this.addActionItem(AddSDISecuritySet.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 0) {
            BaseDetailPropertyHandler.ActionItem deleteSDISecuritySet = this.addActionItem(DeleteSDISecuritySet.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDISecuritySet);
                deleteSDISecuritySet.put("securityset", ds[0]);
            }
        }
        List coreColumnsList = SDISecuritySetPropertyHandler.getCoreColumnsList();
        coreColumnsList.add("operationid");
        coreColumnsList.add("securityset");
        List editableColumnList = elementColumns.getExcludedColumnList(coreColumnsList);
        for (i = 0; i < elementData.size(); ++i) {
            String securityset = elementData.getColumnData(i, "securityset");
            String operationid = elementData.getColumnData(i, "operationid");
            String __status = elementData.getColumnData(i, "__status");
            int rowNum = i;
            if (__status == null || __status.length() == 0 || __status.equals("S") || !__status.equals("N")) continue;
            this.putKeysInActionItem(addSDISecuritySet);
            addSDISecuritySet.put("securityset", securityset);
            addSDISecuritySet.put("operationid", operationid);
            addSDISecuritySet.put("usersequence", String.valueOf(rowNum));
            addSDISecuritySet.putColumns(editableColumnList, elementData, i);
        }
        this.processActionItems();
    }
}

