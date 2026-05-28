/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.gwt.server.ext.BaseDataSetProvider;
import com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class TaskExecutionsProvider
extends BaseDataSetProvider {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public DataSet getDataSet(PropertyList properties) throws SapphireException {
        DataSet dataSet;
        String sortby = properties.getProperty("sortby");
        String sortbydir = properties.getProperty("sortbydir");
        String groupby = properties.getProperty("groupby");
        String groupbysortdir = properties.getProperty("groupbysortdir");
        String queryfrom = WorkflowManagerRequest.evalTokens(this.sapphireConnection, properties.getProperty("queryfrom"));
        String querywhere = WorkflowManagerRequest.evalTokens(this.sapphireConnection, properties.getProperty("querywhere"));
        DataAccessService das = new DataAccessService(this.sapphireConnection);
        RSet rset = null;
        try {
            rset = das.createRSetQ("LV_TaskExec", "", null, queryfrom, querywhere, "", "", 0);
            String sql = "SELECT taskexec.taskexecid, taskexec.taskexecdesc, taskexec.workflowexecid, taskexec.startdt, taskexec.createby, taskexec.completedt, taskexec.moddt, taskexec.modby, taskexec.execstatus, taskexec.summary,        CASE WHEN taskexec.execstatus = 'A' THEN 'Active' WHEN taskexec.execstatus = 'C' THEN 'Complete' WHEN taskexec.execstatus = 'X' THEN 'Cancelled' WHEN taskexec.execstatus = 'P' THEN 'Paused' WHEN taskexec.execstatus = 'W' THEN 'Waiting' ELSE 'Unknown' END AS execstatus_text,        taskdef.taskdefid, taskdef.taskdefdesc,        (SELECT CASE COUNT(*) WHEN 0 THEN 0 ELSE 1 END FROM sdinote WHERE sdcid='LV_TaskExec' AND keyid1 = taskexec.taskexecid) notes FROM   rsetitems, taskexec, taskdef WHERE  taskexec.taskexecid = rsetitems.keyid1 AND rsetitems.rsetid = ?  AND   taskexec.taskdefid = taskdef.taskdefid AND taskexec.taskdefversionid = taskdef.taskdefversionid AND taskexec.taskdefvariantid = taskdef.taskdefvariantid  AND   taskexec.workflowexecid IS NOT NULL UNION SELECT DISTINCT taskexec.taskexecid, taskexec.taskexecdesc, taskexecitem.workflowexecid, taskexec.startdt, taskexec.createby, taskexec.completedt, taskexec.moddt, taskexec.modby, taskexec.execstatus, taskexec.summary,        CASE WHEN taskexec.execstatus = 'A' THEN 'Active' WHEN taskexec.execstatus = 'C' THEN 'Complete' WHEN taskexec.execstatus = 'X' THEN 'Cancelled' WHEN taskexec.execstatus = 'P' THEN 'Paused' WHEN taskexec.execstatus = 'W' THEN 'Waiting' ELSE 'Unknown' END AS execstatus_text,        taskdef.taskdefid, taskdef.taskdefdesc,        (SELECT CASE COUNT(*) WHEN 0 THEN 0 ELSE 1 END FROM sdinote WHERE sdcid='LV_TaskExec' AND keyid1 = taskexec.taskexecid) notes FROM   rsetitems, taskexec, taskdef, taskexecitem WHERE  taskexec.taskexecid = rsetitems.keyid1 AND rsetitems.rsetid = ?  AND   taskexec.taskdefid = taskdef.taskdefid AND taskexec.taskdefversionid = taskdef.taskdefversionid AND taskexec.taskdefvariantid = taskdef.taskdefvariantid  AND   taskexec.taskexecid = taskexecitem.taskexecid  AND   ( taskexec.workflowexecid IS NULL OR taskexec.workflowexecid = '' ) ORDER BY " + (sortby.length() > 0 ? sortby + (sortbydir.length() > 0 ? " " + sortbydir : " ASC") : "taskexecid DESC");
            this.database.createPreparedResultSet(sql, new Object[]{rset.getRsetid(), rset.getRsetid()});
            DataSet dataset = new DataSet(this.sapphireConnection);
            dataset.setResultSet(this.database.getResultSet());
            dataSet = dataset;
        }
        catch (Exception e) {
            try {
                throw new SapphireException("Failed to get provider dataset. Reason: " + e.getMessage(), e);
            }
            catch (Throwable throwable) {
                try {
                    if (rset == null) throw throwable;
                    das.clearRSet(rset);
                    throw throwable;
                }
                catch (ServiceException e2) {
                    throw new SapphireException("Failed to clear rset '" + rset.getRsetid() + "'. Reason: " + e2.getMessage(), e2);
                }
            }
        }
        try {
            if (rset == null) return dataSet;
            das.clearRSet(rset);
            return dataSet;
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to clear rset '" + rset.getRsetid() + "'. Reason: " + e.getMessage(), e);
        }
    }
}

