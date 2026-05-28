/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ParticipantConsent
extends BaseSDCRules {
    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.updateAnswerConsentStatus(actionProps, sdiData);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.rollupConsentInstanceStatus(sdiData);
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.rollupConsentInstanceStatus(sdiData);
    }

    private void updateAnswerConsentStatus(PropertyList newProps, SDIData sdiData) throws SapphireException {
        DataSet participantconsentitems = sdiData.getDataset("s_participantconsentitem");
        if (participantconsentitems == null || participantconsentitems.getRowCount() == 0) {
            return;
        }
        DataSet inputAnswers = new DataSet();
        inputAnswers.addColumnValues("s_participantconsentitemid", 0, newProps.getProperty("s_participantconsentitemid"), ";");
        inputAnswers.addColumnValues("answer", 0, newProps.getProperty("answer"), ";");
        for (int i = 0; i < inputAnswers.getRowCount(); ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("s_participantconsentitemid", inputAnswers.getString(i, "s_participantconsentitemid"));
            DataSet match = participantconsentitems.getFilteredDataSet(filter);
            if (match.getRowCount() <= 0) continue;
            String curranswer = inputAnswers.getString(i, "answer", "");
            if (curranswer.trim().length() > 0) {
                if (participantconsentitems.getString(i, "answergranted", "Y").equals(curranswer)) {
                    participantconsentitems.setString(i, "answerstatus", "G");
                    continue;
                }
                participantconsentitems.setString(i, "answerstatus", "R");
                continue;
            }
            participantconsentitems.setString(i, "answerstatus", "P");
        }
    }

    private void rollupConsentInstanceStatus(SDIData sdiData) throws SapphireException {
        DataSet participantconsentitems = sdiData.getDataset("s_participantconsentitem");
        if (participantconsentitems == null || participantconsentitems.getRowCount() == 0) {
            return;
        }
        String participantid = participantconsentitems.getString(0, "s_participantid", "");
        String participantconsentid = participantconsentitems.getString(0, "s_participantconsentid");
        String sql = "SELECT * FROM s_participantconsentitem WHERE s_participantid = ? AND s_participantconsentid = ? ";
        DataSet allparticipantconsentitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{participantid, participantconsentid});
        String allanswerstatus = allparticipantconsentitems.getColumnValues("answerstatus", ";");
        String consentDate = this.findLatestConsentDate(allparticipantconsentitems);
        if (allanswerstatus.indexOf("P") == -1) {
            if (allanswerstatus.indexOf("R") > -1) {
                PropertyList consentProps = new PropertyList();
                consentProps.setProperty("sdcid", "LV_ParticipantConsent");
                consentProps.setProperty("keyid1", participantid);
                consentProps.setProperty("keyid2", allparticipantconsentitems.getValue(0, "s_participantconsentid"));
                consentProps.setProperty("consentinstancestatus", "R");
                consentProps.setProperty("consentinstancedt", consentDate);
                this.getActionProcessor().processAction("EditSDI", "1", consentProps);
            } else {
                PropertyList consentProps = new PropertyList();
                consentProps.setProperty("sdcid", "LV_ParticipantConsent");
                consentProps.setProperty("keyid1", participantid);
                consentProps.setProperty("keyid2", allparticipantconsentitems.getValue(0, "s_participantconsentid"));
                consentProps.setProperty("consentinstancestatus", "G");
                consentProps.setProperty("consentinstancedt", consentDate);
                this.getActionProcessor().processAction("EditSDI", "1", consentProps);
            }
        } else {
            PropertyList consentProps = new PropertyList();
            consentProps.setProperty("sdcid", "LV_ParticipantConsent");
            consentProps.setProperty("keyid1", participantid);
            consentProps.setProperty("keyid2", allparticipantconsentitems.getValue(0, "s_participantconsentid"));
            consentProps.setProperty("consentinstancestatus", "P");
            consentProps.setProperty("consentinstancedt", consentDate);
            this.getActionProcessor().processAction("EditSDI", "1", consentProps);
        }
    }

    private String findLatestConsentDate(DataSet participantconsentitems) {
        DataSet ci = participantconsentitems.copy();
        ci.sort("answerdt d");
        return ci.getValue(0, "answerdt");
    }
}

