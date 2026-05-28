/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CreateSAPPR
extends BaseAction {
    public static final String PROPERTY_INDENTID = "indentid";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String indentId = propertyList.getProperty(PROPERTY_INDENTID);
        if (indentId != null && indentId.length() > 0) {
            String statusSQL = "select status from u_indent where u_indentid = '" + indentId + "'";
            DataSet stat = this.getQueryProcessor().getSqlDataSet(statusSQL);
            String status = stat.getValue(0, "status");
            if ("".equals(status)) {
                DataSet indentDS = new DataSet(this.connectionInfo);
                indentDS.addColumn(PROPERTY_INDENTID, 0);
                indentDS.addRow();
                indentDS.setValue(0, PROPERTY_INDENTID, indentId);
                String reqItems = "SELECT doctype, purchreqdate, purchorg , purchgroup, linenum, indentitem.materialid as materialid, quantity, deliverydate, u_plant as plant, u_unit as unit from u_indent indent, u_indentitem indentitem, s_material material WHERE indent.u_indentid = indentitem.indentid and indentitem.materialid = material.s_materialid  and indent.u_indentid = '" + indentId + "'";
                DataSet reqItemsDS = this.getQueryProcessor().getSqlDataSet(reqItems);
                if (reqItemsDS == null || reqItemsDS.getRowCount() == 0) {
                    throw new ActionException("Failed to fetch the indent items from Sapphire");
                }
                reqItemsDS.addColumn("deldatcat", 0);
                reqItemsDS.setValue(0, "deldatcat", "1");
                reqItemsDS.addColumn("createdby", 0);
                reqItemsDS.setValue(0, "createdby", "SEC");
                reqItemsDS.padColumn("deldatcat");
                reqItemsDS.padColumn("createdby");
                PropertyList props = new PropertyList();
                props.setProperty("messagetypeid", "REQ_CREATE");
                props.setProperty("processedby", this.connectionInfo.getSysuserId());
                props.setProperty(PROPERTY_INDENTID, indentId);
                props.setProperty("INDENT", indentDS.toXML());
                props.setProperty("REQUISITION_ITEMS", reqItemsDS.toXML());
                this.getActionProcessor().processAction("ProcessOutMessage", "1", props);
                if (!"SUCCESS".equals(props.getProperty("status"))) {
                    throw new ActionException(props.getProperty("error"));
                }
            } else {
                Trace.logInfo("Indent status is not empty");
            }
        }
    }
}

