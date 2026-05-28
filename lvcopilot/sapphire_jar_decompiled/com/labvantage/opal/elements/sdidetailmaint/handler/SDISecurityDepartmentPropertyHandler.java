/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.sapphire.actions.sdi.AddSDISecurityDept;
import com.labvantage.sapphire.actions.sdi.DeleteSDISecurityDep;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class SDISecurityDepartmentPropertyHandler
extends BaseDetailPropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 58185 $";

    @Override
    protected void saveData() throws SapphireException {
        int i;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem addSDISecurityDept = this.addActionItem(AddSDISecurityDept.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 0) {
            BaseDetailPropertyHandler.ActionItem deleteSDISecurityDep = this.addActionItem(DeleteSDISecurityDep.class.getName());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                this.putKeysInActionItem(deleteSDISecurityDep);
                deleteSDISecurityDep.put("departmentid", ds[0]);
                deleteSDISecurityDep.put("operationid", ds[1]);
            }
        }
        List coreColumnsList = SDISecurityDepartmentPropertyHandler.getCoreColumnsList();
        coreColumnsList.add("operationid");
        coreColumnsList.add("securitydepartment");
        List editableColumnList = elementColumns.getExcludedColumnList(coreColumnsList);
        for (i = 0; i < elementData.size(); ++i) {
            String securitydepartment = elementData.getColumnData(i, "securitydepartment");
            String operationid = elementData.getColumnData(i, "operationid");
            String __status = elementData.getColumnData(i, "__status");
            int rowNum = i;
            if (__status == null || __status.length() == 0 || __status.equals("S") || !__status.equals("N")) continue;
            this.putKeysInActionItem(addSDISecurityDept);
            addSDISecurityDept.put("departmentid", securitydepartment);
            addSDISecurityDept.put("operationid", operationid);
            addSDISecurityDept.put("usersequence", String.valueOf(rowNum));
            addSDISecurityDept.putColumns(editableColumnList, elementData, i);
        }
        this.processActionItems();
    }
}

