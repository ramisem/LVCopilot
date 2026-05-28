/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CopyCohort
extends BaseAction
implements sapphire.action.CopyCohort {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77314 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String protocolId = properties.getProperty("clinicalprotocolid", "");
        String protocolVersionId = properties.getProperty("clinicalprotocolversionid", "");
        String protocolRevision = properties.getProperty("clinicalprotocolrevision", "");
        String sourceCohortId = properties.getProperty("sourcecohortid", "");
        String newCohortId = properties.getProperty("newcohortid", "");
        String cohortDesc = properties.getProperty("newcohortdesc", "");
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
        if (sourceCohortId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Source Cohort Id not passed into the action."));
        }
        if (newCohortId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("New Cohort Id is not specified."));
        }
        SequenceProcessor sp = this.getSequenceProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        ActionProcessor ap = this.getActionProcessor();
        SDCProcessor sdp = this.getSDCProcessor();
        Calendar now = Calendar.getInstance();
        Object columnsDS = null;
        try {
            String sql = "INSERT INTO s_cpcohort ( s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, s_cpcohortid, cohortdesc, dose, productid, productversionid, plannedparticipants, createdt, createby, createtool ) ( SELECT s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, ?, ?, dose, productid, productversionid, plannedparticipants, ?, ?, ? FROM s_cpcohort WHERE s_clinicalprotocolid = ? AND s_clinicalprotocolversionid = ? AND s_clinicalprotocolrevision = ? AND s_cpcohortid = ?)";
            this.database.executePreparedUpdate(sql, new Object[]{newCohortId, cohortDesc, DateTimeUtil.getNowTimestamp(), this.connectionInfo.getSysuserId(), "CopyCohort", protocolId, protocolVersionId, protocolRevision, sourceCohortId});
            this.database.createPreparedResultSet("eventdefs", " SELECT * FROM s_eventdef WHERE clinicalprotocolid = ? AND clinicalprotocolversionid = ? AND clinicalprotocolrevision = ? AND cohortid = ? AND (parenteventdefid is null or parenteventdefid = '')  order by usersequence", new Object[]{protocolId, protocolVersionId, protocolRevision, sourceCohortId});
            DataSet eventDef = new DataSet(this.database.getResultSet("eventdefs"));
            this.database.closeResultSet("eventdefs");
            this.copyEventDef(eventDef, protocolId, protocolVersionId, protocolRevision, newCohortId, ap);
            properties.put("newcohortid", newCohortId);
            Trace.log(tp.translate("New Cohort id") + ": " + newCohortId);
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Could not process action") + " " + "CopyCohort" + "=>" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void copyEventDef(DataSet eventDef, String protocolId, String protocolVersionId, String protocolRevision, String newCohortId, ActionProcessor ap) throws ActionException {
        if (eventDef != null && eventDef.size() > 0) {
            for (int i = 0; i < eventDef.size(); ++i) {
                String eventDefId = eventDef.getValue(i, "s_eventdefid");
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sdcid", "LV_EventDef");
                props.put("clinicalprotocolid", protocolId);
                props.put("clinicalprotocolrevision", protocolRevision);
                props.put("clinicalprotocolversionid", protocolVersionId);
                props.put("cohortid", newCohortId);
                props.put("templateid", eventDefId);
                PropertyList pl = new PropertyList(props);
                ap.processActionClass("com.labvantage.sapphire.actions.clinicalbb.AddEventDef", pl);
            }
        }
    }
}

