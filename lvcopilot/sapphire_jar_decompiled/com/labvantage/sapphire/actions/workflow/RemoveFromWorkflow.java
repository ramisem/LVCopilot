/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RemoveFromWorkflow
extends BaseAction
implements sapphire.action.RemoveFromWorkflow {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Sdcid not specified");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Keyid1 not specified");
        }
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        DataSet propsDS = new DataSet(this.connectionInfo);
        if (propsmatch) {
            propsDS.addColumnValues("queuekeyid1", 0, properties.getProperty("keyid1"), ";");
            propsDS.addColumnValues("queuekeyid2", 0, properties.getProperty("keyid2"), ";");
            propsDS.addColumnValues("queuekeyid3", 0, properties.getProperty("keyid3"), ";");
            propsDS.addColumnValues("workflowexecid", 0, properties.getProperty("workflowexecid"), ";");
            propsDS.addColumnValues("workflowdefid", 0, properties.getProperty("workflowdefid"), ";");
            propsDS.addColumnValues("workflowdefversionid", 0, properties.getProperty("workflowdefversionid"), ";");
            propsDS.addColumnValues("workflowdefvariantid", 0, properties.getProperty("workflowdefvariantid"), ";");
            propsDS.padColumns();
        } else {
            propsDS.addColumn("queuekeyid1", 0);
            propsDS.addColumn("queuekeyid2", 0);
            propsDS.addColumn("queuekeyid3", 0);
            propsDS.addColumn("workflowexecid", 0);
            propsDS.addColumn("workflowdefid", 0);
            propsDS.addColumn("workflowdefversionid", 0);
            propsDS.addColumn("workflowdefvariantid", 0);
            String[] keyid1Props = StringUtil.split(properties.getProperty("keyid1"), ";");
            String[] keyid2Props = StringUtil.split(properties.getProperty("keyid2"), ";");
            String[] keyid3Props = StringUtil.split(properties.getProperty("keyid3"), ";");
            String[] workflowexecidProps = StringUtil.split(properties.getProperty("workflowexecid"), ";");
            String[] workflowdefidProps = StringUtil.split(properties.getProperty("workflowdefid"), ";");
            String[] workflowdefversionidProps = StringUtil.split(properties.getProperty("workflowdefversionid"), ";");
            String[] workflowdefvariantidProps = StringUtil.split(properties.getProperty("workflowdefvariantid"), ";");
            for (int i = 0; i < keyid1Props.length; ++i) {
                for (int j = 0; j < workflowdefidProps.length; ++j) {
                    int row = propsDS.addRow();
                    propsDS.setValue(row, "queuekeyid1", keyid1Props[i]);
                    propsDS.setValue(row, "queuekeyid2", keyid2Props.length > i && keyid2Props[i].length() > 0 ? keyid2Props[i] : "(null)");
                    propsDS.setValue(row, "queuekeyid3", keyid3Props.length > i && keyid3Props[i].length() > 0 ? keyid3Props[i] : "(null)");
                    propsDS.setValue(row, "workflowexecid", workflowexecidProps.length > j ? workflowexecidProps[j] : "");
                    propsDS.setValue(row, "workflowdefid", workflowdefidProps[j]);
                    propsDS.setValue(row, "workflowdefversionid", workflowdefversionidProps.length > j ? workflowdefversionidProps[j] : "1");
                    propsDS.setValue(row, "workflowdefvariantid", workflowdefvariantidProps.length > j ? workflowdefvariantidProps[j] : "1");
                }
            }
        }
        SafeSQL safeSQL = null;
        StringBuffer where = null;
        safeSQL = new SafeSQL();
        where = new StringBuffer("queuesdcid=" + safeSQL.addVar(sdcid) + " AND (");
        int noOfParameters = 1;
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        for (int i = 0; i < propsDS.size(); ++i) {
            int orBlockParamCount = 0;
            if (noOfParameters >= 2000) {
                where.append(")");
                WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, where.toString(), safeSQL, true, false);
                safeSQL = new SafeSQL();
                where = new StringBuffer("queuesdcid=" + safeSQL.addVar(sdcid) + " AND (");
                noOfParameters = 1;
            }
            where.append(noOfParameters > 1 ? " OR (" : "(");
            where.append("queuekeyid1=").append(safeSQL.addVar(propsDS.getValue(i, "queuekeyid1"))).append("");
            ++orBlockParamCount;
            if (propsDS.getValue(i, "queuekeyid2").length() > 0) {
                where.append(" AND queuekeyid2=").append(safeSQL.addVar(propsDS.getValue(i, "queuekeyid2"))).append("");
                ++orBlockParamCount;
            }
            if (propsDS.getValue(i, "queuekeyid3").length() > 0) {
                where.append(" AND queuekeyid3=").append(safeSQL.addVar(propsDS.getValue(i, "queuekeyid3"))).append("");
                ++orBlockParamCount;
            }
            if (propsDS.getValue(i, "workflowexecid").length() > 0) {
                where.append(" AND workflowexecid=").append(safeSQL.addVar(propsDS.getValue(i, "workflowexecid"))).append("");
                ++orBlockParamCount;
            }
            if (propsDS.getValue(i, "workflowdefid").length() > 0) {
                where.append(" AND workflowdefid=").append(safeSQL.addVar(propsDS.getValue(i, "workflowdefid"))).append("");
                ++orBlockParamCount;
                if (propsDS.getValue(i, "workflowdefversionid").length() > 0) {
                    where.append(" AND workflowdefversionid=").append(safeSQL.addVar(propsDS.getValue(i, "workflowdefversionid"))).append("");
                    ++orBlockParamCount;
                }
                if (propsDS.getValue(i, "workflowdefvariantid").length() > 0) {
                    where.append(" AND workflowdefvariantid=").append(safeSQL.addVar(propsDS.getValue(i, "workflowdefvariantid"))).append("");
                    ++orBlockParamCount;
                }
            }
            where.append(")");
            noOfParameters += orBlockParamCount;
        }
        if (noOfParameters > 0) {
            where.append(")");
            WorkflowManager.deleteTaskQueueItems(sapphireConnection, (DBUtil)this.database, where.toString(), safeSQL, true, false);
        }
    }
}

