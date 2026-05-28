/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class DeleteCohort
extends BaseAction
implements sapphire.action.DeleteCohort {
    static final String LABVANTAGE_CVS_ID = "$Revision: 97349 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String protocolId = properties.getProperty("clinicalprotocolid", "");
        String protocolVersionId = properties.getProperty("clinicalprotocolversionid", "");
        String protocolRevision = properties.getProperty("clinicalprotocolrevision", "");
        String cohortIds = properties.getProperty("cohortid", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (protocolId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Id not passed into the action."));
        }
        if (protocolVersionId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Version Id not passed into the action."));
        }
        if (protocolRevision.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Revision Id not passed into the action."));
        }
        if (cohortIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No Cohort Id passed into the action."));
        }
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder findSql = new StringBuilder("SELECT s_participantid FROM s_participant");
            findSql.append(" WHERE cpcohortid in (").append(safeSQL.addIn(cohortIds, ";")).append(")");
            findSql.append(" AND clinicalprotocolid=").append(safeSQL.addVar(protocolId));
            findSql.append(" AND clinicalprotocolrevision=").append(safeSQL.addVar(protocolRevision));
            findSql.append(" AND clinicalprotocolversionid=").append(safeSQL.addVar(protocolVersionId));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(findSql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Cohort has Participants. So, it can not be deleted."));
            }
            BusinessRulesUtil.updateConsentQuestions(cohortIds, protocolId, protocolRevision, this.database);
            ActionProcessor ap = this.getActionProcessor();
            PropertyList actionProps = new PropertyList();
            safeSQL.reset();
            String sql = "SELECT s_eventdefid FROM s_eventdef ";
            sql = sql + " WHERE clinicalprotocolid = " + safeSQL.addVar(protocolId) + " AND clinicalprotocolversionid = " + safeSQL.addVar(protocolVersionId) + " AND clinicalprotocolrevision = " + safeSQL.addVar(protocolRevision) + " AND cohortid IN (" + safeSQL.addIn(cohortIds, ";") + ")";
            DataSet eventDefs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (eventDefs != null && eventDefs.size() > 0) {
                String eventDefIds = eventDefs.getColumnValues("s_eventdefid", ";");
                actionProps.clear();
                actionProps.setProperty("sdcid", "LV_EventDef");
                actionProps.setProperty("keyid1", eventDefIds);
                ap.processAction("DeleteSDI", "1", actionProps);
            }
            sql = "DELETE FROM s_cpcohort WHERE s_clinicalprotocolid = '" + protocolId + "' AND s_clinicalprotocolversionid = '" + protocolVersionId + "' AND s_clinicalprotocolrevision = '" + protocolRevision + "' AND s_cpcohortid IN ('" + cohortIds.replaceAll(";", "','") + "')";
            this.database.executeSQL(sql);
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "DeleteCohort " + tp.translate("action could not be processed.") + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

