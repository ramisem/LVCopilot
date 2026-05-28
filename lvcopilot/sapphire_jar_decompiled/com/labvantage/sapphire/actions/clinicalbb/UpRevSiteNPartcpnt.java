/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpRevSiteNPartcpnt
extends BaseAction
implements sapphire.action.UpRevSiteNPartcpnt {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53302 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String participantQueryWhere;
        String participantids;
        if (properties.getProperty("siteid", "").length() == 0 && properties.getProperty("participantid", "").length() == 0) {
            throw new SapphireException("Mandatory properties not set");
        }
        String auditReason = properties.getProperty("auditreason", "");
        String auditActivity = properties.getProperty("auditactivity", "");
        String auditSignedFlag = properties.getProperty("auditsignedflag", "");
        ActionBlock ab = new ActionBlock();
        if (properties.getProperty("siteid", "").length() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_StudySite");
            actionProps.setProperty("keyid1", properties.getProperty("siteid"));
            actionProps.setProperty("clinicalprotocolrevision", properties.getProperty("clinicalprotocolrevision", ""));
            if (auditReason.length() > 0) {
                actionProps.setProperty("auditreason", auditReason);
            }
            if (auditActivity.length() > 0) {
                actionProps.setProperty("auditactivity", auditActivity);
            }
            if (auditSignedFlag.length() > 0) {
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
            }
            ab.setAction("LV_StudySite", "EditSDI", "1");
            ab.setActionProperties("LV_StudySite", actionProps);
        }
        if ((participantids = properties.getProperty("participantid", "")).length() == 0 && (participantQueryWhere = properties.getProperty("participantfilter", "")).length() > 0) {
            String sql = "SELECT s_participantid FROM s_participant WHERE " + participantQueryWhere;
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
            participantids = ds.getColumnValues("s_participantid", ";");
        }
        if (participantids.length() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_Participant");
            actionProps.setProperty("keyid1", participantids);
            actionProps.setProperty("clinicalprotocolrevision", properties.getProperty("clinicalprotocolrevision", ""));
            if (auditReason.length() > 0) {
                actionProps.setProperty("auditreason", auditReason);
            }
            if (auditActivity.length() > 0) {
                actionProps.setProperty("auditactivity", auditActivity);
            }
            if (auditSignedFlag.length() > 0) {
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
            }
            ab.setAction("LV_Participant", "EditSDI", "1");
            ab.setActionProperties("LV_Participant", actionProps);
        }
        this.getActionProcessor().processActionBlock(ab);
    }
}

