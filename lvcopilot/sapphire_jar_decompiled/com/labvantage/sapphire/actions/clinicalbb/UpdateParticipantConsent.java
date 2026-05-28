/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpdateParticipantConsent
extends BaseAction
implements sapphire.action.UpdateParticipantConsent {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String participant = properties.getProperty("participantid");
        String answers = properties.getProperty("answer");
        String answerDt = properties.getProperty("answerdt");
        String questionid = properties.getProperty("questionid");
        DataSet newAnswers = new DataSet();
        newAnswers.addColumnValues("questionid", 0, questionid, ";");
        newAnswers.addColumnValues("answer", 0, answers, ";");
        newAnswers.addColumnValues("answerdt", 0, answerDt, ";");
        String sql = "select s_participantconsentitemid, s_participantconsentitem.s_participantconsentid, clinicalprotocolid, clinicalprotocolrevision, questionid, questiontext, answer, answergranted, answereditorstyleid, answerdt, answerstatus, s_participantconsentitem.usersequence from s_participantconsent, s_participantconsentitem  where s_participantconsent.s_participantid = ? and  s_participantconsent.s_participantid = s_participantconsentitem.s_participantid  and s_participantconsent.s_participantconsentid = s_participantconsentitem.s_participantconsentid  and s_participantconsent.s_participantconsentid = (SELECT max(s_participantconsentid) FROM s_participantconsent WHERE s_participantid = ? ) ";
        DataSet fromDB = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{participant, participant});
        if (participant != null && participant.length() > 0 && answers != null && answers.length() > 0) {
            for (int i = 0; i < newAnswers.getRowCount(); ++i) {
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("questionid", newAnswers.getString(i, "questionid"));
                int findRow = fromDB.findRow(find);
                if (findRow <= -1) continue;
                newAnswers.setString(i, "s_participantconsentitemid", fromDB.getString(findRow, "s_participantconsentitemid"));
                newAnswers.setString(i, "s_participantconsentid", fromDB.getString(findRow, "s_participantconsentid"));
            }
            ArrayList<DataSet> consentInstances = newAnswers.getGroupedDataSets("s_participantconsentid");
            for (int i = 0; i < consentInstances.size(); ++i) {
                DataSet currConsent = consentInstances.get(i);
                PropertyList propertyList = new PropertyList();
                propertyList.setProperty("sdcid", "LV_ParticipantConsent");
                propertyList.setProperty("keyid1", participant);
                propertyList.setProperty("keyid2", currConsent.getValue(0, "s_participantconsentid"));
                propertyList.setProperty("linkid", "consent responses");
                propertyList.setProperty("s_participantconsentitemid", currConsent.getColumnValues("s_participantconsentitemid", ";"));
                propertyList.setProperty("answer", currConsent.getColumnValues("answer", ";"));
                propertyList.setProperty("answerdt", currConsent.getColumnValues("answerdt", ";"));
                this.getActionProcessor().processAction("EditSDIDetail", "1", propertyList);
            }
        }
    }
}

