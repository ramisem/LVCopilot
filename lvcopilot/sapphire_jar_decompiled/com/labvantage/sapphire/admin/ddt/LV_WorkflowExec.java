/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_WorkflowExec
extends BaseSDCRules {
    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            String workflowexecid = primary.getValue(i, "workflowexecid");
            if (EventManager.hasWorkflowEventPlan(sapphireConnection, workflowexecid, false)) continue;
            EventManager.loadEventPlans(sapphireConnection);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.containsKey("execstatus")) {
            DataSet primary = sdiData.getDataset("primary");
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            for (int i = 0; i < primary.size(); ++i) {
                String workflowexecid = primary.getValue(i, "workflowexecid");
                String execstatus = primary.getValue(i, "execstatus", "A");
                if (!EventManager.hasWorkflowEventPlan(sapphireConnection, workflowexecid, false)) {
                    EventManager.loadEventPlans(sapphireConnection);
                }
                if (!execstatus.equals("X")) continue;
                StringBuffer taskexecid = new StringBuffer();
                this.database.createPreparedResultSet("SELECT taskexecid FROM taskexec WHERE workflowexecid = ? AND execstatus IN ( 'P', 'I' ) ", new Object[]{workflowexecid});
                while (this.database.getNext()) {
                    taskexecid.append(";").append(this.database.getValue("taskexecid"));
                }
                this.database.createPreparedResultSet("SELECT DISTINCT taskexec.taskexecid FROM taskexec, taskexecitem WHERE taskexecitem.taskexecid = taskexec.taskexecid AND taskexecitem.workflowexecid = ? AND taskexec.execstatus IN ( 'P', 'I' ) ", new Object[]{workflowexecid});
                while (this.database.getNext()) {
                    taskexecid.append(";").append(this.database.getValue("taskexecid"));
                }
                if (taskexecid.length() > 0) {
                    PropertyList deleteTaskExec = new PropertyList();
                    deleteTaskExec.setProperty("sdcid", "LV_TaskExec");
                    deleteTaskExec.setProperty("keyid1", taskexecid.substring(1));
                    this.getActionProcessor().processAction("DeleteSDI", "1", deleteTaskExec);
                }
                SafeSQL safeSQL = new SafeSQL();
                WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, "workflowexecid = " + safeSQL.addVar(workflowexecid), safeSQL, false, true);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        if (this.database.checkPreparedExists("SELECT taskexec.taskexecid FROM taskexec WHERE taskexec.execstatus = ? AND taskexec.workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{"A", rsetid})) {
            throw new SapphireException("Cannot delete workflow execution while a task is active!");
        }
        if (this.database.checkPreparedExists("SELECT taskexec.taskexecid FROM taskexec, taskexecitem WHERE taskexec.taskexecid = taskexecitem.taskexecid AND taskexec.execstatus = ? AND taskexecitem.workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{"A", rsetid})) {
            throw new SapphireException("Cannot delete workflow execution while a task is active!");
        }
        SafeSQL safeSQL = new SafeSQL();
        WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, "workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ")", safeSQL, false, true);
        this.database.executePreparedUpdate("DELETE FROM taskexecitem WHERE taskexecid IN (SELECT taskexecid FROM taskexec WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?))", new Object[]{rsetid});
        this.database.executePreparedUpdate("DELETE FROM taskexec WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
        this.database.executePreparedUpdate("DELETE FROM taskexec WHERE taskexecid IN (SELECT taskexecid FROM taskexecitem WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?))", new Object[]{rsetid});
        this.database.executePreparedUpdate("DELETE FROM taskexecitem WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
        this.database.executePreparedUpdate("DELETE FROM sdiworkflowrule WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
        ((DBUtil)this.database).createPreparedResultSet("SELECT keyid1 FROM rsetitems WHERE rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            if (EventManager.hasWorkflowEventPlan(sapphireConnection, this.database.getValue("keyid1"), true)) continue;
            ((DBUtil)this.database).executePreparedUpdate("UPDATE workflowexec SET execstatus = NULL WHERE workflowexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
            EventManager.loadEventPlans(sapphireConnection);
        }
    }

    public static String generateName(QueryProcessor queryProcessor, SDCProcessor sdcProcessor, SequenceProcessor sequenceProcessor, String sdcid, String keyid1, String workflowdefid, String workflowdefversionid, String workflowdefvariantid) {
        DataSet workflowdef = queryProcessor.getPreparedSqlDataSet("SELECT exectypeflag FROM workflowdef WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
        if (workflowdef.getValue(0, "exectypeflag", "S").equals("S")) {
            return workflowdefid;
        }
        if (keyid1.contains(";")) {
            return sdcProcessor.getProperty(sdcid, "singular") + " Group: " + sequenceProcessor.getSequence("LV_WorkflowExec", workflowdefid + ";" + workflowdefversionid + ";" + workflowdefvariantid);
        }
        return sdcProcessor.getProperty(sdcid, "singular") + ": " + keyid1;
    }
}

