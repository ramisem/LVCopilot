/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

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

public class AddSDIWorkflowRule
extends BaseAction {
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
        String select = "SELECT\tsdiworkflowrule.sdcid, sdiworkflowrule.keyid1, sdiworkflowrule.keyid2, sdiworkflowrule.keyid3, sdiworkflowrule.workflowdefid, sdiworkflowrule.workflowdefversionid, sdiworkflowrule.workflowdefvariantid,        sdiworkflowrule.taskdefitemid, sdiworkflowrule.ioitemid, sdiworkflowrule.workflowexecid, sdiworkflowrule.addmodeflag FROM\tsdiworkflowrule, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdiworkflowrule.sdcid AND \t\trsetitems.keyid1 = sdiworkflowrule.keyid1 AND \t\trsetitems.keyid2 = sdiworkflowrule.keyid2 AND \t\trsetitems.keyid3 = sdiworkflowrule.keyid3 ORDER BY 2, 3, 4, 5, 6, 7, 8, 9";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        DataSet sdiworkflowrule = new DataSet();
        sdiworkflowrule.setResultSet(this.database.getResultSet());
        DataSet insert = new DataSet();
        insert.addColumn("sdcid", 0);
        insert.addColumn("keyid1", 0);
        insert.addColumn("keyid2", 0);
        insert.addColumn("keyid3", 0);
        insert.addColumn("workflowdefid", 0);
        insert.addColumn("workflowdefversionid", 0);
        insert.addColumn("workflowdefvariantid", 0);
        insert.addColumn("taskdefitemid", 0);
        insert.addColumn("ioitemid", 0);
        insert.addColumn("workflowexecid", 0);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createtool", 0);
        insert.addColumn("addmodeflag", 0);
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] workflowdefidprop = StringUtil.split(properties.getProperty("workflowdefid"), ";");
        String[] workflowdefversionidprop = StringUtil.split(properties.getProperty("workflowdefversionid"), ";");
        String[] workflowdefvariantidprop = StringUtil.split(properties.getProperty("workflowdefvariantid"), ";");
        String[] taskdefitemidprop = StringUtil.split(properties.getProperty("taskdefitemid"), ";");
        String[] ioitemidprop = StringUtil.split(properties.getProperty("ioitemid"), ";");
        String[] workflowexecidprop = StringUtil.split(properties.getProperty("workflowexecid"), ";");
        String[] addmodeflagprop = StringUtil.split(properties.getProperty("addmodeflag"), ";");
        if (keyid1prop.length == workflowdefidprop.length && workflowdefidprop.length == workflowdefversionidprop.length) {
            Calendar now = DateTimeUtil.getNowCalendar();
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int i = 0; i < keyid1prop.length; ++i) {
                int row;
                findMap.put("keyid1", keyid1prop[i]);
                if (keyid2prop.length > i && keyid2prop[i].length() > 0) {
                    findMap.put("keyid2", keyid2prop[i]);
                }
                if (keyid3prop.length > i && keyid3prop[i].length() > 0) {
                    findMap.put("keyid3", keyid3prop[i]);
                }
                findMap.put("workflowdefid", workflowdefidprop[i]);
                findMap.put("workflowdefversionid", workflowdefversionidprop[i]);
                findMap.put("workflowdefvariantid", workflowdefversionidprop[i]);
                findMap.put("taskdefitemid", taskdefitemidprop[i]);
                findMap.put("ioitemid", ioitemidprop[i]);
                findMap.put("workflowexecid", workflowexecidprop[i]);
                if (addmodeflagprop.length > i) {
                    findMap.put("addmodeflag", addmodeflagprop[i]);
                }
                if ((row = sdiworkflowrule.findRow(findMap)) >= 0) continue;
                row = insert.addRow();
                insert.setString(row, "sdcid", sdcid);
                insert.setString(row, "keyid1", keyid1prop[i]);
                insert.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                insert.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                insert.setString(row, "workflowdefid", workflowdefidprop[i]);
                insert.setString(row, "workflowdefversionid", workflowdefversionidprop[i]);
                insert.setString(row, "workflowdefvariantid", workflowdefvariantidprop[i]);
                insert.setString(row, "taskdefitemid", taskdefitemidprop[i]);
                insert.setString(row, "ioitemid", ioitemidprop[i]);
                insert.setString(row, "workflowexecid", workflowexecidprop[i]);
                if (addmodeflagprop.length > i) {
                    insert.setString(row, "addmodeflag", addmodeflagprop[i]);
                }
                insert.setString(row, "createby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "createdt", now);
                insert.setString(row, "createtool", this.connectionInfo.getTool());
            }
            DataSetUtil.insert(this.database, insert, "sdiworkflowrule");
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

