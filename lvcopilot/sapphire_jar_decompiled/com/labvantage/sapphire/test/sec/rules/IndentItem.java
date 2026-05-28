/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.rules;

import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class IndentItem
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        String maxlinenum;
        DataSet primary = sdiData.getDataset("primary");
        String indentId = primary.getValue(0, "indentid");
        String sqlMaxLinenum = "SELECT max(linenum) maxlinenum FROM u_indentitem WHERE indentid='" + indentId + "'";
        DataSet linenumDS = this.getQueryProcessor().getSqlDataSet(sqlMaxLinenum);
        int curMax = 0;
        if (linenumDS != null && !"".equals(maxlinenum = linenumDS.getValue(0, "maxlinenum"))) {
            curMax = new Integer(maxlinenum);
        }
        for (int i = 0; i < primary.getRowCount(); ++i) {
            int newLineNo = curMax + (i + 1) * 10;
            if (!primary.isValidColumn("linenum")) {
                primary.addColumn("linenum", 1);
            }
            primary.setValue(i, "linenum", new Integer(newLineNo).toString());
        }
        Trace.log("updated linenums");
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String indentId = primary.getValue(0, "indentid");
        String[] temp = StringUtil.split(indentId, ";");
        if (temp.length > 0) {
            indentId = temp[0];
        }
        String statusSQL = "select purchreqno, status from u_indent where u_indentid = '" + indentId + "'";
        DataSet stat = this.getQueryProcessor().getSqlDataSet(statusSQL);
        String status = stat.getValue(0, "status");
        String prnumber = stat.getValue(0, "purchreqno");
        if (status != null && status.length() > 0) {
            String indentItemList = propertyList.getProperty("newkeyid1");
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("indentitemid", indentItemList);
            throw new SapphireException("Cannot add items to an indent that already has a PRNumber");
        }
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("indentid", indentId);
        this.getActionProcessor().processAction("CreateSAPPR", "1", props);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        String indentItemList = propertyList.getProperty("keyid1");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("indentitemid", indentItemList);
        this.getActionProcessor().processAction("CreateSAPPREdit", "1", props);
    }

    @Override
    public void preDelete(String s, PropertyList propertyList) throws SapphireException {
        String indentItemList = propertyList.getProperty("keyid1");
        Trace.log("Deleting the indent items " + indentItemList);
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("indentitemid", indentItemList);
        this.getActionProcessor().processAction("CreateSAPPRDelete", "1", props);
    }
}

