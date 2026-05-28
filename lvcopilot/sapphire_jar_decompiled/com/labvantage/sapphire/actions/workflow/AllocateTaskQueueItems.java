/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AllocateTaskQueueItems
extends BaseAction
implements sapphire.action.AllocateTaskQueueItems,
WorkflowManagerConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String taskexecid = properties.getProperty("taskexecid");
        String taskqueueid = properties.getProperty("taskqueueid");
        String usage = properties.getProperty("queueusage", "E");
        boolean removeExisting = properties.getProperty("removeexisting", "Y").equals("Y");
        String[] taskqueueidlist = StringUtil.split(taskqueueid, ";");
        if (taskqueueidlist.length > 0) {
            try {
                String ioid = "";
                this.database.createPreparedResultSet("getioid", "SELECT ioid FROM taskqueue WHERE taskqueueid = ? ", new String[]{taskqueueidlist[0]});
                if (this.database.getNext("getioid")) {
                    ioid = this.database.getString("getioid", "ioid");
                }
                if (removeExisting) {
                    WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "queuestatus = 'W'", "ioid=? AND queuestatus = 'E' AND taskqueueid IN ( SELECT taskqueueid FROM taskexecitem WHERE taskexecid = ? )", new Object[]{ioid, taskexecid}, false);
                    this.database.executePreparedUpdate("DELETE FROM taskexecitem WHERE ioid=? AND taskexecid = ? ", new Object[]{ioid, taskexecid});
                }
                Timestamp now = DateTimeUtil.getNowTimestamp();
                String insert = "INSERT INTO taskexecitem ( taskexecid, itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, taskqueueid,   workflowexecid, taskdefitemid, taskdefid, taskdefversionid, taskdefvariantid,   ioid, typeflag, usersequence, createdt, createby, fromtaskexecid ) SELECT '" + SafeSQL.encodeForSQL(taskexecid, this.database.isOracle()) + "', taskqueue.queuesdcid, taskqueue.queuekeyid1, taskqueue.queuekeyid2, taskqueue.queuekeyid3, taskqueue.taskqueueid,   taskqueue.workflowexecid, taskqueue.taskdefitemid, taskqueue.taskdefid, taskqueue.taskdefversionid, taskqueue.taskdefvariantid,   taskqueue.ioid, 'I', ?, ?, ?, taskqueue.fromtaskexecid FROM taskqueue WHERE taskqueue.taskqueueid = ?";
                PreparedStatement ps = this.database.prepareStatement(insert);
                for (int i = 0; i < taskqueueidlist.length; ++i) {
                    ps.setInt(1, i);
                    ps.setTimestamp(2, now);
                    ps.setString(3, this.connectionInfo.getSysuserId());
                    ps.setString(4, taskqueueidlist[i]);
                    ps.executeUpdate();
                }
                this.database.executePreparedUpdate("UPDATE taskexecitem SET totaskexecid = ? WHERE taskexecid IN ( SELECT distinct fromtaskexecid FROM taskexecitem tei2 WHERE tei2.taskexecid = ? )", new Object[]{taskexecid, taskexecid});
                this.database.executePreparedUpdate("UPDATE taskexec SET workflowexecid = NULL, taskdefitemid = NULL WHERE taskexecid = ?", new Object[]{taskexecid});
                int rows = WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "taskqueue.queuestatus = '" + SafeSQL.encodeForSQL(usage, this.database.isOracle()) + "', taskqueue.taskexecutions = taskqueue.taskexecutions + 1", "ioid=? AND taskqueue.taskqueueid IN (    SELECT taskexecitem.taskqueueid FROM taskexecitem, taskqueue    WHERE taskexecitem.taskexecid = ? AND taskexecitem.taskqueueid = taskqueue.taskqueueid AND taskqueue.queuestatus IN ( 'W', 'A', 'S' ) )", new Object[]{ioid, taskexecid}, false);
                if (rows != taskqueueidlist.length) {
                    throw new SapphireException("Failed to update taskqueue from new taskexec SDIs.");
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed insert taskexec rows. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }
}

