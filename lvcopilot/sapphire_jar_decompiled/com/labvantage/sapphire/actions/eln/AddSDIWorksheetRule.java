/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

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

public class AddSDIWorksheetRule
extends BaseAction
implements sapphire.action.AddSDIWorksheetRule {
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
        String select = "SELECT\tsdiworksheetrule.sdcid, sdiworksheetrule.keyid1, sdiworksheetrule.keyid2, sdiworksheetrule.keyid3, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.worksheetinstance FROM\tsdiworksheetrule, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdiworksheetrule.sdcid AND \t\trsetitems.keyid1 = sdiworksheetrule.keyid1 AND \t\trsetitems.keyid2 = sdiworksheetrule.keyid2 AND \t\trsetitems.keyid3 = sdiworksheetrule.keyid3 ORDER BY sdiworksheetrule.keyid1, sdiworksheetrule.keyid2, sdiworksheetrule.keyid3, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetinstance";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        DataSet sdiworksheetrule = new DataSet();
        sdiworksheetrule.setResultSet(this.database.getResultSet());
        DataSet insert = new DataSet();
        insert.addColumn("sdcid", 0);
        insert.addColumn("keyid1", 0);
        insert.addColumn("keyid2", 0);
        insert.addColumn("keyid3", 0);
        insert.addColumn("worksheetid", 0);
        insert.addColumn("worksheetversionid", 0);
        insert.addColumn("worksheetinstance", 1);
        insert.addColumn("workbookid", 0);
        insert.addColumn("workbookversionid", 0);
        insert.addColumn("worksheetrule", 0);
        insert.addColumn("maxsdiperworksheet", 1);
        insert.addColumn("authorflag", 0);
        insert.addColumn("createflag", 0);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createtool", 0);
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] worksheetidprop = StringUtil.split(properties.getProperty("worksheetid"), ";");
        String[] worksheetinstanceprop = StringUtil.split(properties.getProperty("worksheetinstance"), ";");
        String[] worksheetversionidprop = StringUtil.split(properties.getProperty("worksheetversionid"), ";");
        String[] workbookidprop = StringUtil.split(properties.getProperty("workbookid"), ";");
        String[] workbookversionidprop = StringUtil.split(properties.getProperty("workbookversionid"), ";");
        String[] worksheetruleprop = StringUtil.split(properties.getProperty("worksheetrule"), ";");
        String[] maxsdiperworksheetprop = StringUtil.split(properties.getProperty("maxsdiperworksheet"), ";");
        String[] authorflagprop = StringUtil.split(properties.getProperty("authorflag"), ";");
        String[] createflagprop = StringUtil.split(properties.getProperty("createflag"), ";");
        String[] usersequenceprops = StringUtil.split(properties.getProperty("usersequence"), ";");
        if (keyid1prop.length == worksheetidprop.length && worksheetidprop.length == worksheetinstanceprop.length) {
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
                findMap.put("worksheetid", worksheetidprop[i]);
                findMap.put("worksheetinstance", worksheetinstanceprop[i]);
                int row = sdiworksheetrule.findRow(findMap);
                if (row >= 0) continue;
                row = insert.addRow();
                insert.setString(row, "sdcid", sdcid);
                insert.setString(row, "keyid1", keyid1prop[i]);
                insert.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                insert.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                insert.setString(row, "worksheetid", worksheetidprop[i]);
                insert.setNumber(row, "worksheetinstance", worksheetinstanceprop[i]);
                insert.setString(row, "worksheetversionid", (String)(worksheetversionidprop.length > i ? (worksheetversionidprop[i] != null && (worksheetversionidprop[i].equalsIgnoreCase("C") || worksheetversionidprop[i].equalsIgnoreCase("(null)") || worksheetversionidprop[i].trim().length() == 0) ? null : worksheetversionidprop[i]) : ""));
                if ("Y".equals(properties.getProperty("propsmatch"))) {
                    insert.setString(row, "workbookid", properties.getProperty("workbookid", ""));
                    insert.setString(row, "workbookversionid", properties.getProperty("workbookversionid", ""));
                    insert.setString(row, "worksheetrule", properties.getProperty("worksheetrule", ""));
                    insert.setNumber(row, "maxsdiperworksheet", properties.getProperty("maxsdiperworksheet", ""));
                    insert.setString(row, "authorflag", properties.getProperty("authorflag", "N"));
                    insert.setString(row, "createflag", properties.getProperty("createflag", "N"));
                } else {
                    insert.setString(row, "workbookid", workbookidprop.length > i ? workbookidprop[i] : "");
                    insert.setString(row, "workbookversionid", workbookversionidprop.length > i ? workbookversionidprop[i] : "");
                    insert.setString(row, "worksheetrule", worksheetruleprop.length > i ? worksheetruleprop[i] : "");
                    insert.setNumber(row, "maxsdiperworksheet", maxsdiperworksheetprop.length > i ? maxsdiperworksheetprop[i] : "");
                    insert.setString(row, "authorflag", authorflagprop.length > i ? authorflagprop[i] : "N");
                    insert.setString(row, "createflag", createflagprop.length > i ? createflagprop[i] : "N");
                }
                insert.setString(row, "createby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "createdt", now);
                insert.setString(row, "createtool", this.connectionInfo.getTool());
                insert.setString(row, "usersequence", usersequenceprops.length > i ? usersequenceprops[i] : "");
            }
            DataSetUtil.insert(this.database, insert, "sdiworksheetrule");
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

