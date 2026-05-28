/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateSAPPREdit
extends BaseAction {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String indentItemList = propertyList.getProperty("indentitemid");
        String[] indentItems = StringUtil.split(indentItemList, ";");
        String inlist = "";
        for (int i = 0; i < indentItems.length; ++i) {
            if (indentItems[i].length() <= 0) continue;
            if (i != 0) {
                inlist = inlist + ",";
            }
            inlist = inlist + "'" + indentItems[i] + "'";
        }
        String sql = "SELECT purchreqno, u_indentid, linenum, materialid, deliverydate, quantity from u_indent, u_indentitem where u_indent.u_indentid=u_indentitem.indentid AND u_indentitemid in (" + inlist + " )";
        DataSet results = this.getQueryProcessor().getSqlDataSet(sql);
        DataSet numberDS = new DataSet(this.connectionInfo);
        numberDS.addColumn("purchreqno", 0);
        numberDS.addRow();
        numberDS.setString(0, "purchreqno", results.getValue(0, "purchreqno"));
        DataSet reqItemsToEdit = new DataSet(this.connectionInfo);
        reqItemsToEdit.addColumn("linenum", 0);
        reqItemsToEdit.addColumn("materialid", 0);
        reqItemsToEdit.addColumn("quantity", 1);
        reqItemsToEdit.addColumn("deliverydate", 2);
        for (int i = 0; i < results.getRowCount(); ++i) {
            reqItemsToEdit.addRow();
            reqItemsToEdit.setValue(i, "linenum", results.getValue(i, "linenum"));
            reqItemsToEdit.setValue(i, "materialid", results.getValue(i, "materialid"));
            reqItemsToEdit.setValue(i, "quantity", results.getValue(i, "quantity"));
            reqItemsToEdit.setValue(i, "deliverydate", results.getValue(i, "deliverydate"));
        }
        PropertyList props = new PropertyList();
        props.setProperty("messagetypeid", "REQ_UPDATE");
        props.setProperty("processedby", this.connectionInfo.getSysuserId());
        props.setProperty("NUMBER", numberDS.toXML());
        props.setProperty("REQUISITION_ITEMS", reqItemsToEdit.toXML());
        this.getActionProcessor().processAction("ProcessOutMessage", "1", props);
        if (!"SUCCESS".equals(props.getProperty("status"))) {
            throw new ActionException(props.getProperty("error"));
        }
    }
}

