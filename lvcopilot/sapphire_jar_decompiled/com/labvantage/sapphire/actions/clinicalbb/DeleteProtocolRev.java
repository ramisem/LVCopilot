/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteProtocolRev
extends BaseAction
implements sapphire.action.DeleteProtocolRev {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53946 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        String clinicalProtocolIds = properties.getProperty("clinicalprotocolid");
        String clinicalProtocolRevisions = properties.getProperty("clinicalprotocolrevision");
        this.logger.info("Calling DeleteProtocolRev action to delete the revisions: " + clinicalProtocolRevisions + " of the following protocols: " + clinicalProtocolRevisions);
        if (clinicalProtocolIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Protocol Id is not specified") + " ");
        }
        if (clinicalProtocolRevisions.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Protocol revision is not specified") + " ");
        }
        DataSet cpDS = new DataSet();
        cpDS.addColumn("s_clinicalprotocolid", 0);
        cpDS.addColumn("s_clinicalprotocolrevision", 0);
        DataSet finalds = this.generateCPDataSet(clinicalProtocolIds, clinicalProtocolRevisions, cpDS);
        this.deleteTestGrid(finalds);
        this.deleteEventDefs(finalds);
        PropertyList props = new PropertyList();
        props.setProperty("keyid1", finalds.getColumnValues("s_clinicalprotocolid", ";"));
        props.setProperty("keyid2", finalds.getColumnValues("s_clinicalprotocolversionid", ";"));
        props.setProperty("keyid3", finalds.getColumnValues("s_clinicalprotocolrevision", ";"));
        props.setProperty("sdcid", "LV_ClinicalProtocol");
        this.getActionProcessor().processAction("DeleteSDI", "1", props);
    }

    private void deleteEventDefs(DataSet finalds) throws ActionException {
        String eventdefids = "";
        ArrayList<DataSet> groupedDSAL = finalds.getGroupedDataSets("s_clinicalprotocolid");
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet groupDataSet = groupedDSAL.get(i);
            String cpid = groupDataSet.getValue(0, "s_clinicalprotocolid");
            String cprevisions = groupDataSet.getColumnValues("s_clinicalprotocolrevision", ";");
            SafeSQL safeSQL = new SafeSQL();
            String sql = " SELECT s_eventdefid FROM s_eventdef  WHERE clinicalprotocolid = " + safeSQL.addVar(cpid) + "  AND clinicalprotocolrevision IN (" + safeSQL.addIn(cprevisions, ";") + ") ";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            eventdefids = eventdefids != null && eventdefids.length() > 0 ? eventdefids + ";" + ds.getColumnValues("s_eventdefid", ";") : ds.getColumnValues("s_eventdefid", ";");
        }
        this.logger.info("calling deletesdi on LV_EventDef sdc to delete all associated events");
        if (eventdefids.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("keyid1", eventdefids);
            props.setProperty("sdcid", "LV_EventDef");
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }

    private void deleteTestGrid(DataSet finalds) {
        ArrayList<DataSet> groupedDSAL = finalds.getGroupedDataSets("s_clinicalprotocolid");
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet groupDataSet = groupedDSAL.get(i);
            String cpid = groupDataSet.getValue(0, "s_clinicalprotocolid");
            String cprevisions = groupDataSet.getColumnValues("s_clinicalprotocolrevision", ";");
            this.logger.info("deleting sampletype assaytype definitions for revisions: " + cprevisions + " of the following protocols: " + cpid);
            SafeSQL safeSQL = new SafeSQL();
            String sql = "DELETE FROM  s_cpassaytypesampletype WHERE s_clinicalprotocolid = " + safeSQL.addVar(cpid) + " AND s_clinicalprotocolrevision IN  (" + safeSQL.addIn(cprevisions, ";") + ")  ";
            try {
                this.database.executePreparedUpdate(sql, safeSQL.getValues());
                continue;
            }
            catch (SapphireException e) {
                this.logger.error("Failed to execute SQL \"" + sql + "\". Reason: " + e.getMessage(), e);
            }
        }
    }

    private DataSet generateCPDataSet(String clinicalProtocolIds, String clinicalProtocolRevisions, DataSet cpDS) throws SapphireException {
        DataSet finalds = new DataSet();
        finalds.addColumn("s_clinicalprotocolid", 0);
        finalds.addColumn("s_clinicalprotocolversionid", 0);
        finalds.addColumn("s_clinicalprotocolrevision", 0);
        String[] cpIdArr = StringUtil.split(clinicalProtocolIds, ";");
        String[] cprRevArr = StringUtil.split(clinicalProtocolRevisions, ";");
        for (int i = 0; i < cprRevArr.length; ++i) {
            cpDS.addRow();
            cpDS.setValue(i, "s_clinicalprotocolid", cpIdArr[i]);
            cpDS.setValue(i, "s_clinicalprotocolrevision", cprRevArr[i]);
        }
        ArrayList<DataSet> groupedDSAL = cpDS.getGroupedDataSets("s_clinicalprotocolid");
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet groupDataSet = groupedDSAL.get(i);
            String cpid = groupDataSet.getValue(0, "s_clinicalprotocolid");
            String cprevisions = groupDataSet.getColumnValues("s_clinicalprotocolrevision", ";");
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision FROM s_clinicalprotocol WHERE ");
            sql.append("s_clinicalprotocolid = ").append(safeSQL.addVar(cpid)).append(" AND ");
            if (StringUtil.split(cprevisions, ";").length <= 750) {
                sql.append("s_clinicalprotocolrevision in (").append(safeSQL.addIn(cprevisions, ";")).append(")");
            } else {
                String rsetid = this.getDAMProcessor().createRSet("LV_ClinicalProtocol", cprevisions, null, null);
                sql.append("s_clinicalprotocolrevision  in (");
                sql.append(" select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            finalds.addAll(ds);
        }
        return finalds;
    }
}

