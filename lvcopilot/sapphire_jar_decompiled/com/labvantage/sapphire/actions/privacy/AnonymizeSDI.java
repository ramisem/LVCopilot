/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.privacy;

import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.CallableStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AnonymizeSDI
extends BaseAction
implements sapphire.action.AnonymizeSDI {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private static final String SDC_USER = "User";
    private static final String SDC_SUBJECT = "LV_Subject";
    private static final String SDC_ADDRESS = "Address";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String auditReason;
        String traceLogIdStr;
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String includeAudit = properties.getProperty("includeauditflag");
        if (sdcId.length() == 0) {
            throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("SDC Id missing."));
        }
        if (keyId1.length() == 0) {
            throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("KeyId1 missing."));
        }
        if (sdcId.indexOf(";") > -1) {
            throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("Only single SDCID is allowed."));
        }
        if (includeAudit.indexOf(";") > -1) {
            throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("Only single value is allowed for 'Include Audit'."));
        }
        String[] keyId1Arr = StringUtil.split(keyId1, ";");
        String[] keyId2Arr = StringUtil.split(keyId2, ";");
        if (SDC_ADDRESS.equals(sdcId)) {
            if (keyId2.length() == 0) {
                throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("KeyId2 missing."));
            }
            if (keyId1Arr.length != keyId2Arr.length) {
                throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("KeyId 1 & 2 mismatch."));
            }
        }
        if ("(system)".equalsIgnoreCase(this.connectionInfo.getSysuserId())) {
            throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("Action can not be executed by Super User (system)."));
        }
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcId);
        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        int traceLogId = 0;
        boolean traceLogSetToSession = false;
        if (!sdcProps.getProperty("auditedflag").equalsIgnoreCase("N") && (traceLogIdStr = properties.getProperty("tracelogid", "").trim()).length() == 0 && (auditReason = properties.getProperty("auditreason", "")).length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProps.getProperty("auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            try {
                traceLogId = Integer.parseInt(audit.addSDITraceLogEntry(sdcId, keyId1, keyId2, "", auditReason, properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"), "Anonymize " + sdcId + ", " + keyId1, standard.equals("Y")));
                properties.setProperty("tracelogid", String.valueOf(traceLogId));
                audit.setTracelogIdInDBSession(traceLogId + "");
                traceLogSetToSession = true;
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        includeAudit = StringUtil.getYN(includeAudit, "Y");
        for (int i = 0; i < keyId1Arr.length; ++i) {
            try {
                CallableStatement cs;
                if (sdcId.equals(SDC_USER)) {
                    cs = this.database.prepareCall("{call lv_anonymize" + (this.connectionInfo.isOracle() ? "." : "_") + "sysuserdata( ?,?,? ) }");
                    cs.setString(1, keyId1Arr[i]);
                    cs.setString(2, this.connectionInfo.getSysuserId());
                    cs.setString(3, includeAudit);
                    cs.executeUpdate();
                    this.database.closeCall();
                    continue;
                }
                if (sdcId.equals(SDC_SUBJECT)) {
                    cs = this.database.prepareCall("{call lv_anonymize" + (this.connectionInfo.isOracle() ? "." : "_") + "subjectdata( ?,?,? ) }");
                    cs.setString(1, keyId1Arr[i]);
                    cs.setString(2, this.connectionInfo.getSysuserId());
                    cs.setString(3, includeAudit);
                    cs.executeUpdate();
                    this.database.closeCall();
                    continue;
                }
                if (sdcId.equals(SDC_ADDRESS)) {
                    cs = this.database.prepareCall("{call lv_anonymize" + (this.connectionInfo.isOracle() ? "." : "_") + "contactdata( ?,?,?,? ) }");
                    cs.setString(1, keyId1Arr[i]);
                    cs.setString(2, keyId2Arr[i]);
                    cs.setString(3, this.connectionInfo.getSysuserId());
                    cs.setString(4, includeAudit);
                    cs.executeUpdate();
                    this.database.closeCall();
                    continue;
                }
                throw new SapphireException("AnonymizeSDI", "VALIDATION", this.getTranslationProcessor().translate("Un-supported SDC."));
            }
            catch (SQLException e) {
                throw new SapphireException(e);
            }
        }
        if (traceLogSetToSession) {
            try {
                audit.removeTracelogIdFromDBSession();
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to clear Tracelog Info from session: ", e);
            }
        }
    }
}

