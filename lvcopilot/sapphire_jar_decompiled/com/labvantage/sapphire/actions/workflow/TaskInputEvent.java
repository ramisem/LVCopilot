/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TaskInputEvent
extends BaseAction
implements WorkflowManagerConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String taskdefid = properties.getProperty("taskdefid");
        String taskdefversionid = properties.getProperty("taskdefversionid");
        String taskdefvariantid = properties.getProperty("taskdefvariantid");
        boolean autoexec = properties.getProperty("autoexec").equals("Y");
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        if (properties.getProperty("connectortypesdcid").equals("SDIWorkItem") && properties.getProperty("workitemid").length() > 0) {
            String[] keyid1list = StringUtil.split(keyid1, ";");
            String[] keyid2list = StringUtil.split(keyid2, ";");
            String[] keyid3list = StringUtil.split(keyid3, ";");
            String[] workitemidlist = StringUtil.split(properties.getProperty("workitemid"), ";");
            String[] workiteminstancelist = StringUtil.split(properties.getProperty("workiteminstance"), ";");
            StringBuffer newkeyid1 = new StringBuffer();
            StringBuffer newkeyid2 = new StringBuffer();
            StringBuffer newkeyid3 = new StringBuffer();
            for (int i = 0; i < keyid1list.length; ++i) {
                this.database.createPreparedResultSet("SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?", new Object[]{sdcid, keyid1list[i], keyid2list[i], keyid3list[i], workitemidlist[i], workiteminstancelist[i]});
                if (!this.database.getNext()) continue;
                newkeyid1.append(";").append(this.database.getValue("sdiworkitemid"));
                newkeyid2.append(";").append("(null)");
                newkeyid3.append(";").append("(null)");
            }
            if (newkeyid1.length() > 0) {
                sdcid = "SDIWorkItem";
                keyid1 = newkeyid1.substring(1);
                keyid2 = newkeyid2.substring(1);
                keyid3 = newkeyid3.substring(1);
            }
        }
        String[] queuekeyid1 = StringUtil.split(keyid1, ";");
        String[] queuekeyid2 = StringUtil.split(keyid2, ";");
        String[] queuekeyid3 = StringUtil.split(keyid3, ";");
        StringBuffer queuekeyid1buff = new StringBuffer(" AND ( queuekeyid1 IN ( ");
        StringBuffer queuekeyid2buff = new StringBuffer(" AND ( queuekeyid2 IN ( ");
        StringBuffer queuekeyid3buff = new StringBuffer(" AND ( queuekeyid3 IN ( ");
        int count = 0;
        for (int i = 0; i < queuekeyid1.length; ++i) {
            if (count > 0) {
                queuekeyid1buff.append(",");
                queuekeyid2buff.append(",");
                queuekeyid3buff.append(",");
            }
            queuekeyid1buff.append("'").append(queuekeyid1[i]).append("'");
            queuekeyid2buff.append("'").append(queuekeyid2[i]).append("'");
            queuekeyid3buff.append("'").append(queuekeyid3[i]).append("'");
            if (count >= 250 && i < queuekeyid1.length - 1) {
                queuekeyid1buff.append(" ) OR queuekeyid1 IN ( ");
                queuekeyid2buff.append(" ) OR queuekeyid2 IN ( ");
                queuekeyid3buff.append(" ) OR queuekeyid3 IN ( ");
                count = 0;
                continue;
            }
            ++count;
        }
        queuekeyid1buff.append(") )");
        queuekeyid2buff.append(") )");
        queuekeyid3buff.append(") )");
        StringBuffer where = new StringBuffer();
        where.append("taskdefid = ? AND taskdefversionid = ? AND taskdefvariantid = ? AND queuesdcid = ? ").append(queuekeyid1buff).append(queuekeyid2buff).append(queuekeyid3buff).append(" AND queuestatus = '").append(autoexec ? "Aevent" : "Wevent").append("' ");
        if (autoexec) {
            this.database.createPreparedResultSet("SELECT taskqueueid, createby FROM taskqueue WHERE " + where.toString() + " ORDER BY createby ASC, taskqueueid ASC", new Object[]{taskdefid, taskdefversionid, taskdefvariantid, sdcid});
            DataSet queuedata = new DataSet(this.database.getResultSet());
            if (queuedata.size() > 0) {
                ArrayList<DataSet> groups = queuedata.getGroupedDataSets("createby");
                for (int i = 0; i < groups.size(); ++i) {
                    DataSet group = groups.get(i);
                    if (group.size() <= 0) continue;
                    int updates = WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "queuestatus = 'A'", where.toString(), new Object[]{taskdefid, taskdefversionid, taskdefvariantid, sdcid}, false);
                    ActionProcessor actionProcessor = this.getActionProcessor();
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("actionid", "ProcessAutoExecTask");
                    actionProps.setProperty("actionversionid", "1");
                    actionProps.setProperty("duedate", "Y");
                    actionProps.setProperty("taskqueueid", group.getColumnValues("taskqueueid", ";"));
                    actionProps.setProperty("processassysuserid", group.getValue(0, "createby"));
                    actionProcessor.processAction("AddToDoListEntry", "1", actionProps);
                    this.logger.debug(updates + " taskqueue item events fulfilled for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid) + " and sent for auto execution");
                }
            } else {
                this.logger.debug("No taskqueue item events found for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid) + ". WHERE=" + where.toString());
            }
        } else {
            int updates = WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "queuestatus = 'W'", where.toString(), new Object[]{taskdefid, taskdefversionid, taskdefvariantid, sdcid}, true);
            this.logger.debug(updates + " taskqueue item events fulfilled for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid));
        }
    }
}

