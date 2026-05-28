/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class CreateProtocol
extends BaseAction
implements sapphire.action.CreateProtocol {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77314 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String studyId = properties.getProperty("clinicalstudyid", "");
        String copyProtocolFlag = properties.getProperty("copyprotocolflag", "N");
        try {
            TranslationProcessor tp = this.getTranslationProcessor();
            ActionProcessor ap = this.getActionProcessor();
            if (studyId.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Study Id not specified."));
            }
            String protocolId = properties.getProperty("clinicalprotocolid", "");
            String protocolVersionId = properties.getProperty("clinicalprotocolversionid", "");
            String protocolRevision = properties.getProperty("clinicalprotocolrevision", "");
            String newRevision = properties.getProperty("newclinicalprotocolrevision", "");
            String newProtocolDesc = properties.getProperty("newclinicalprotocoldesc", "");
            PropertyList actionProp = new PropertyList();
            if (protocolId.length() > 0) {
                if (protocolVersionId.length() == 0) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("Protocol version id not specified for the protocol:") + " " + protocolId);
                }
                if (protocolRevision.length() == 0) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("Protocol revision not specified for the protocol:") + " " + protocolId);
                }
                if (copyProtocolFlag.equalsIgnoreCase("N") && (newRevision.length() == 0 || newRevision.equals(protocolRevision))) {
                    actionProp.setProperty("sdcid", "LV_ClinicalProtocol");
                    actionProp.setProperty("keyid1", protocolId);
                    actionProp.setProperty("keyid2", protocolVersionId);
                    actionProp.setProperty("keyid3", protocolRevision);
                    actionProp.setProperty("sstudyid", studyId);
                    if (newProtocolDesc.length() > 0) {
                        actionProp.setProperty("clinicalprotocoldesc", newProtocolDesc);
                    }
                    ap.processAction("AddSDIVersion", "1", actionProp);
                    properties.setProperty("returnnewclinicalprotocolid", protocolId);
                    properties.setProperty("returnnewclinicalprotocolversionid", actionProp.getProperty("keyid2", ""));
                    properties.setProperty("returnnewclinicalprotocolrevision", protocolRevision);
                    this.copyAssayTypeSampleTypeCollection(protocolId, protocolVersionId, protocolRevision, properties.getProperty("returnnewclinicalprotocolid"), properties.getProperty("returnnewclinicalprotocolversionid"), properties.getProperty("returnnewclinicalprotocolrevision"));
                    this.copyCohortEvents(copyProtocolFlag, studyId, protocolId, protocolVersionId, protocolRevision, actionProp.getProperty("keyid2", ""), protocolRevision);
                } else {
                    actionProp.setProperty("sdcid", "LV_ClinicalProtocol");
                    actionProp.setProperty("keyid1", studyId);
                    actionProp.setProperty("templatekeyid1", protocolId);
                    actionProp.setProperty("templatekeyid2", protocolVersionId);
                    actionProp.setProperty("templatekeyid3", protocolRevision);
                    actionProp.setProperty("sstudyid", studyId);
                    if (copyProtocolFlag.equalsIgnoreCase("Y")) {
                        actionProp.setProperty("keyid2", protocolVersionId);
                        actionProp.setProperty("keyid3", protocolRevision);
                        actionProp.setProperty("versionstatus", properties.getProperty("versionstatus", "P"));
                    } else {
                        actionProp.setProperty("keyid2", "1");
                        actionProp.setProperty("keyid3", newRevision);
                        actionProp.setProperty("versionstatus", "P");
                    }
                    if (newProtocolDesc.length() > 0) {
                        actionProp.setProperty("clinicalprotocoldesc", newProtocolDesc);
                    }
                    ap.processAction("AddSDI", "1", actionProp);
                    properties.setProperty("returnnewclinicalprotocolid", actionProp.getProperty("newkeyid1"));
                    properties.setProperty("returnnewclinicalprotocolversionid", actionProp.getProperty("newkeyid2"));
                    properties.setProperty("returnnewclinicalprotocolrevision", actionProp.getProperty("newkeyid3"));
                    this.copyAssayTypeSampleTypeCollection(protocolId, protocolVersionId, protocolRevision, properties.getProperty("returnnewclinicalprotocolid"), properties.getProperty("returnnewclinicalprotocolversionid"), properties.getProperty("returnnewclinicalprotocolrevision"));
                    this.copyCohortEvents(copyProtocolFlag, studyId, protocolId, protocolVersionId, protocolRevision, actionProp.getProperty("newkeyid2"), actionProp.getProperty("newkeyid3"));
                }
            } else {
                if (newRevision.length() == 0) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("Revision not specified for the new protocol."));
                }
                actionProp.setProperty("sdcid", "LV_ClinicalProtocol");
                actionProp.setProperty("keyid1", studyId);
                actionProp.setProperty("keyid2", "1");
                actionProp.setProperty("keyid3", newRevision);
                actionProp.setProperty("sstudyid", studyId);
                actionProp.setProperty("versionstatus", "P");
                actionProp.setProperty("clinicalprotocoldesc", newProtocolDesc);
                ap.processAction("AddSDI", "1", actionProp);
                properties.setProperty("returnnewclinicalprotocolid", actionProp.getProperty("newkeyid1"));
                properties.setProperty("returnnewclinicalprotocolversionid", actionProp.getProperty("newkeyid2"));
                properties.setProperty("returnnewclinicalprotocolrevision", actionProp.getProperty("newkeyid3"));
            }
            Trace.log("New Clinical Protocol created: " + properties.getProperty("returnnewclinicalprotocolid") + "/" + properties.getProperty("returnnewclinicalprotocolversionid") + "/" + properties.getProperty("returnnewclinicalprotocolrevision"));
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "CreateProtocol: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void copyAssayTypeSampleTypeCollection(String templateProtocolId, String templateProtocolVersionId, String templateProtocolRevision, String newProtocolId, String newProtocolVersionId, String newProtocolRevision) throws SapphireException {
        this.logger.info("copying assaytype sampletype definition to the new protocol version");
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s_assaytypeid, s_sampletypeid, workitemid, usersequence, specimentype, arrivalorder FROM ").append("s_cpassaytypesampletype WHERE ").append("s_clinicalprotocolid = ").append(safeSQL.addVar(templateProtocolId)).append(" AND ").append("s_clinicalprotocolversionid = ").append(safeSQL.addVar(templateProtocolVersionId)).append(" AND ").append("s_clinicalprotocolrevision = ").append(safeSQL.addVar(templateProtocolRevision)).append(" ");
        DataSet testDSFromTemplate = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        try {
            PreparedStatement insertCPAssaytypeSamplType = this.database.prepareStatement("insertCPAssaySampTypes", "INSERT INTO s_cpassaytypesampletype (s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, s_assaytypeid, s_sampletypeid, workitemid, specimentype, arrivalorder, usersequence, createdt, createtool, createby ) values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (int i = 0; i < testDSFromTemplate.size(); ++i) {
                insertCPAssaytypeSamplType.setString(1, newProtocolId);
                insertCPAssaytypeSamplType.setString(2, newProtocolVersionId);
                insertCPAssaytypeSamplType.setString(3, newProtocolRevision);
                insertCPAssaytypeSamplType.setString(4, testDSFromTemplate.getString(i, "s_assaytypeid"));
                insertCPAssaytypeSamplType.setString(5, testDSFromTemplate.getString(i, "s_sampletypeid"));
                insertCPAssaytypeSamplType.setString(6, testDSFromTemplate.getString(i, "workitemid"));
                insertCPAssaytypeSamplType.setString(7, testDSFromTemplate.getString(i, "specimentype"));
                insertCPAssaytypeSamplType.setString(8, testDSFromTemplate.getString(i, "arrivalorder"));
                insertCPAssaytypeSamplType.setBigDecimal(9, testDSFromTemplate.getBigDecimal(i, "usersequence"));
                insertCPAssaytypeSamplType.setTimestamp(10, DateTimeUtil.getNowTimestamp());
                insertCPAssaytypeSamplType.setString(11, this.connectionInfo.getTool());
                insertCPAssaytypeSamplType.setString(12, this.connectionInfo.getSysuserId());
                insertCPAssaytypeSamplType.execute();
            }
        }
        catch (SQLException e) {
            throw new SapphireException(e);
        }
    }

    private void copyCohortEvents(String copyProtocolFlag, String studyId, String protocolId, String protocolVersionId, String protocolRevision, String newVersion, String newRevision) throws SapphireException {
        this.logger.info("copying cohort event definition to the new protocol version");
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT s_eventdefid FROM s_eventdef ed, s_cpcohort ch").append(" WHERE ed.clinicalprotocolid = ? AND ed.clinicalprotocolversionid = ?").append(" AND ed.clinicalprotocolrevision = ? AND ch.s_clinicalprotocolid = ed.clinicalprotocolid").append(" AND ch.s_clinicalprotocolversionid = ed.clinicalprotocolversionid AND ch.s_clinicalprotocolrevision = ed.clinicalprotocolrevision ").append(" AND ed.cohortid = ch.s_cpcohortid  AND (ed.parenteventdefid is null or ed.parenteventdefid = '') ").append(" order by ed.usersequence, ed.s_eventdefid");
        this.database.createPreparedResultSet("eventdefs", sql.toString(), new Object[]{protocolId, protocolVersionId, protocolRevision});
        DataSet eventDef = new DataSet(this.database.getResultSet("eventdefs"));
        this.database.closeResultSet("eventdefs");
        if (eventDef != null && eventDef.size() > 0) {
            String eventDefId = eventDef.getColumnValues("s_eventdefid", ";");
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "LV_EventDef");
            if (copyProtocolFlag.equalsIgnoreCase("Y")) {
                props.put("clinicalprotocolid", studyId);
            } else {
                props.put("clinicalprotocolid", protocolId);
            }
            props.put("clinicalprotocolrevision", newRevision);
            props.put("clinicalprotocolversionid", newVersion);
            props.put("templateid", eventDefId);
            PropertyList pl = new PropertyList(props);
            this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.clinicalbb.AddEventDef", pl);
        }
    }
}

