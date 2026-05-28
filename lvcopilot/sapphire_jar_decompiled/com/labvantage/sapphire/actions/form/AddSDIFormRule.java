/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.form;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIFormRule
extends BaseAction
implements sapphire.action.AddSDIFormRule {
    protected static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String select = "SELECT\tsdiformrule.sdcid, sdiformrule.keyid1, sdiformrule.keyid2, sdiformrule.keyid3, sdiformrule.formid, sdiformrule.formversionid, sdiformrule.forminstance FROM\tsdiformrule, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdiformrule.sdcid AND \t\trsetitems.keyid1 = sdiformrule.keyid1 AND \t\trsetitems.keyid2 = sdiformrule.keyid2 AND \t\trsetitems.keyid3 = sdiformrule.keyid3 ORDER BY sdiformrule.keyid1, sdiformrule.keyid2, sdiformrule.keyid3, sdiformrule.formid, sdiformrule.forminstance";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        DataSet sdiformrule = new DataSet();
        sdiformrule.setResultSet(this.database.getResultSet());
        DataSet insert = new DataSet();
        insert.addColumn("sdcid", 0);
        insert.addColumn("keyid1", 0);
        insert.addColumn("keyid2", 0);
        insert.addColumn("keyid3", 0);
        insert.addColumn("formid", 0);
        insert.addColumn("formversionid", 0);
        insert.addColumn("forminstance", 1);
        insert.addColumn("formrule", 0);
        insert.addColumn("mandatoryflag", 0);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createtool", 0);
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] formidprop = StringUtil.split(properties.getProperty("formid"), ";");
        String[] forminstanceprop = StringUtil.split(properties.getProperty("forminstance"), ";");
        String[] formversionidprop = StringUtil.split(properties.getProperty("formversionid"), ";");
        String[] formruleprop = StringUtil.split(properties.getProperty("formrule"), ";");
        String[] mandatoryflagprop = StringUtil.split(properties.getProperty("mandatoryflag"), ";");
        String[] usersequenceprops = StringUtil.split(properties.getProperty("usersequence"), ";");
        if (keyid1prop.length == formidprop.length && formidprop.length == forminstanceprop.length) {
            Calendar now = DateTimeUtil.getNowCalendar();
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int i = 0; i < keyid1prop.length; ++i) {
                findMap.put("keyid1", keyid1prop[i]);
                if (keyid2prop.length > i && keyid2prop[i].length() > 0) {
                    findMap.put("keyid2", keyid2prop[i]);
                }
                if (keyid3prop.length > i && keyid3prop[i].length() > 0) {
                    findMap.put("keyid3", keyid3prop[i]);
                }
                findMap.put("formid", formidprop[i]);
                findMap.put("forminstance", forminstanceprop[i]);
                int row = sdiformrule.findRow(findMap);
                if (row >= 0) continue;
                row = insert.addRow();
                insert.setString(row, "sdcid", sdcid);
                insert.setString(row, "keyid1", keyid1prop[i]);
                insert.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                insert.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                insert.setString(row, "formid", formidprop[i]);
                insert.setNumber(row, "forminstance", forminstanceprop[i]);
                insert.setString(row, "formversionid", (String)(formversionidprop.length > i ? (formversionidprop[i] != null && (formversionidprop[i].equalsIgnoreCase("C") || formversionidprop[i].equalsIgnoreCase("(null)") || formversionidprop[i].trim().length() == 0) ? null : formversionidprop[i]) : ""));
                if ("Y".equals(properties.getProperty("propsmatch"))) {
                    insert.setString(row, "formrule", properties.getProperty("formrule", ""));
                    insert.setString(row, "mandatoryflag", properties.getProperty("mandatoryflag", "N"));
                } else {
                    insert.setString(row, "formrule", formruleprop.length > i ? formruleprop[i] : "");
                    insert.setString(row, "mandatoryflag", mandatoryflagprop.length > i ? mandatoryflagprop[i] : "N");
                }
                insert.setString(row, "createby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "createdt", now);
                insert.setString(row, "createtool", this.connectionInfo.getTool());
                insert.setString(row, "usersequence", usersequenceprops.length > i ? usersequenceprops[i] : "");
            }
            DataSetUtil.insert(this.database, insert, "sdiformrule");
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

