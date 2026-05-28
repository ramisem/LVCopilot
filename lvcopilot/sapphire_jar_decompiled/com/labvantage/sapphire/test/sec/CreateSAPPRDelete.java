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

public class CreateSAPPRDelete
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
        String sql = "SELECT purchreqno, u_indentid, linenum from u_indent, u_indentitem where u_indent.u_indentid=u_indentitem.indentid AND u_indentitemid in (" + inlist + " )";
        DataSet results = this.getQueryProcessor().getSqlDataSet(sql);
        DataSet numberDS = new DataSet(this.connectionInfo);
        numberDS.addColumn("purchreqno", 0);
        numberDS.addRow();
        numberDS.setString(0, "purchreqno", results.getValue(0, "purchreqno"));
        DataSet reqItemsToDelete = new DataSet(this.connectionInfo);
        reqItemsToDelete.addColumn("linenum", 0);
        reqItemsToDelete.addColumn("deleteindicator", 0);
        for (int i = 0; i < results.getRowCount(); ++i) {
            reqItemsToDelete.addRow();
            reqItemsToDelete.setValue(i, "linenum", results.getValue(i, "linenum", "1000"));
            reqItemsToDelete.setValue(i, "deleteindicator", "Y");
        }
        PropertyList props = new PropertyList();
        props.setProperty("messagetypeid", "REQ_DELETE");
        props.setProperty("processedby", this.connectionInfo.getSysuserId());
        props.setProperty("NUMBER", numberDS.toXML());
        props.setProperty("REQ_ITEMS_TO_DELETE", reqItemsToDelete.toXML());
        this.getActionProcessor().processAction("ProcessOutMessage", "1", props);
        if (!"SUCCESS".equals(props.getProperty("status"))) {
            throw new ActionException(props.getProperty("error"));
        }
    }
}

