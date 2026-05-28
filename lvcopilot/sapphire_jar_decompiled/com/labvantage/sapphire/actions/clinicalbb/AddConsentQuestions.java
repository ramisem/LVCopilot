/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddConsentQuestions
extends BaseAction {
    public static final String PROPERTY_STUDYID = "studyid";
    public static final String PROPERTY_QUESTIONID = "questionid";
    public static final String PROPERTY_QUESTIONTEXT = "questiontext";
    public static final String PROPERTY_ANSWERGRANTED = "answergranted";
    public static final String PROPERTY_ANSWEREDITORSTYLEID = "answereditorstyleid";
    public static final String PROPERTY_CONSENTTYPE = "consenttype";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String studyid = properties.getProperty(PROPERTY_STUDYID, "");
        if (studyid.length() == 0) {
            throw new SapphireException("Studyid is mandatory.");
        }
        String sqlparticipant = "SELECT s_participantid FROM s_participant WHERE sstudyid = ?";
        DataSet participantlist = this.getQueryProcessor().getPreparedSqlDataSet(sqlparticipant, new Object[]{studyid});
        String questionidlist = properties.getProperty(PROPERTY_QUESTIONID, "");
        if (questionidlist.length() == 0) {
            throw new SapphireException("questionid is mandatory.");
        }
        String questiontextlist = properties.getProperty(PROPERTY_QUESTIONTEXT, "");
        if (questiontextlist.length() == 0) {
            throw new SapphireException("questiontext is mandatory.");
        }
        DataSet questionids = new DataSet();
        questionids.addColumnValues(PROPERTY_QUESTIONID, 0, questionidlist, ";");
        questionids.addColumnValues(PROPERTY_QUESTIONTEXT, 0, questiontextlist, ";");
        questionids.addColumnValues(PROPERTY_ANSWERGRANTED, 0, properties.getProperty(PROPERTY_ANSWERGRANTED, "Y"), ";");
        questionids.addColumnValues(PROPERTY_ANSWEREDITORSTYLEID, 0, properties.getProperty(PROPERTY_ANSWEREDITORSTYLEID, "Yes/No"), ";");
        questionids.addColumnValues(PROPERTY_CONSENTTYPE, 0, properties.getProperty(PROPERTY_CONSENTTYPE, ""), ";");
        questionids.padColumn(PROPERTY_ANSWERGRANTED);
        questionids.padColumn(PROPERTY_ANSWEREDITORSTYLEID);
        questionids.padColumn(PROPERTY_CONSENTTYPE);
        if (participantlist != null && participantlist.getRowCount() > 0) {
            for (int i = 0; i < participantlist.getRowCount(); ++i) {
                String consentinstancesql = "SELECT * FROM s_participantconsent WHERE s_participantid = ? ORDER by s_participantconsentid desc";
                String participantid = participantlist.getString(i, "s_participantid");
                DataSet consentinfo = this.getQueryProcessor().getPreparedSqlDataSet(consentinstancesql, new Object[]{participantid});
                int consentinstance = 0;
                DataSet finalconsentitems = new DataSet();
                finalconsentitems.addColumn("answerdt", 2);
                finalconsentitems.addColumn(PROPERTY_QUESTIONID, 0);
                finalconsentitems.addColumn(PROPERTY_QUESTIONTEXT, 0);
                finalconsentitems.addColumn("answer", 0);
                finalconsentitems.addColumn(PROPERTY_ANSWERGRANTED, 0);
                finalconsentitems.addColumn(PROPERTY_ANSWEREDITORSTYLEID, 0);
                finalconsentitems.addColumn(PROPERTY_CONSENTTYPE, 0);
                if (consentinfo != null && consentinfo.getRowCount() > 0) {
                    String str = consentinfo.getString(0, "s_participantconsentid");
                    consentinstance = Integer.parseInt(str) + 1;
                    String consentquestionsql = "SELECT * FROM s_participantconsentitem WHERE s_participantconsentid = ? and s_participantid= ?";
                    DataSet prevconsentitems = this.getQueryProcessor().getPreparedSqlDataSet(consentquestionsql, new Object[]{str, participantid});
                    for (int ci = 0; ci < prevconsentitems.getRowCount(); ++ci) {
                        int row;
                        String currquestionid = prevconsentitems.getString(ci, PROPERTY_QUESTIONID);
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put(PROPERTY_QUESTIONID, currquestionid);
                        int q = questionids.findRow(filter);
                        if (q > -1) {
                            questionids.setString(q, "processed", "Y");
                            row = finalconsentitems.addRow();
                            finalconsentitems.setString(row, PROPERTY_QUESTIONID, questionids.getString(q, PROPERTY_QUESTIONID, ""));
                            finalconsentitems.setString(row, PROPERTY_QUESTIONTEXT, questionids.getString(q, PROPERTY_QUESTIONTEXT, ""));
                            finalconsentitems.setString(row, PROPERTY_ANSWERGRANTED, prevconsentitems.getString(ci, PROPERTY_ANSWERGRANTED, ""));
                            finalconsentitems.setString(row, PROPERTY_ANSWEREDITORSTYLEID, prevconsentitems.getString(ci, PROPERTY_ANSWEREDITORSTYLEID, ""));
                            finalconsentitems.setString(row, PROPERTY_CONSENTTYPE, prevconsentitems.getString(ci, PROPERTY_CONSENTTYPE, ""));
                            continue;
                        }
                        row = finalconsentitems.addRow();
                        finalconsentitems.setString(row, PROPERTY_QUESTIONID, prevconsentitems.getString(ci, PROPERTY_QUESTIONID, ""));
                        finalconsentitems.setString(row, PROPERTY_QUESTIONTEXT, prevconsentitems.getString(ci, PROPERTY_QUESTIONTEXT, ""));
                        finalconsentitems.setString(row, "answer", prevconsentitems.getString(ci, "answer", ""));
                        finalconsentitems.setString(row, "answerstatus", prevconsentitems.getString(ci, "answerstatus", ""));
                        finalconsentitems.setValue(row, "answerdt", prevconsentitems.getValue(ci, "answerdt", ""));
                        finalconsentitems.setString(row, PROPERTY_ANSWERGRANTED, prevconsentitems.getString(ci, PROPERTY_ANSWERGRANTED, ""));
                        finalconsentitems.setString(row, PROPERTY_ANSWEREDITORSTYLEID, prevconsentitems.getString(ci, PROPERTY_ANSWEREDITORSTYLEID, ""));
                        finalconsentitems.setString(row, PROPERTY_CONSENTTYPE, prevconsentitems.getString(ci, PROPERTY_CONSENTTYPE, ""));
                    }
                }
                for (int q = 0; q < questionids.getRowCount(); ++q) {
                    if ("Y".equals(questionids.getString(q, "processed"))) continue;
                    int row = finalconsentitems.addRow();
                    finalconsentitems.setString(row, PROPERTY_QUESTIONID, questionids.getString(q, PROPERTY_QUESTIONID, ""));
                    finalconsentitems.setString(row, PROPERTY_QUESTIONTEXT, questionids.getString(q, PROPERTY_QUESTIONTEXT, ""));
                    finalconsentitems.setString(row, PROPERTY_ANSWERGRANTED, questionids.getString(q, PROPERTY_ANSWERGRANTED, ""));
                    finalconsentitems.setString(row, PROPERTY_ANSWEREDITORSTYLEID, questionids.getString(q, PROPERTY_ANSWEREDITORSTYLEID, ""));
                    finalconsentitems.setString(row, PROPERTY_CONSENTTYPE, questionids.getString(q, PROPERTY_CONSENTTYPE, ""));
                }
                this.addParticipantConsentItemsNP(participantid, finalconsentitems, "" + consentinstance);
            }
        }
    }

    public void addParticipantConsentItemsNP(String participantid, DataSet consentQuestions, String consentinstance) throws SapphireException {
        if (consentQuestions != null && consentQuestions.getRowCount() > 0) {
            PropertyList addProps = new PropertyList();
            addProps.setProperty("sdcid", "LV_ParticipantConsent");
            addProps.setProperty("keyid1", participantid);
            addProps.setProperty("keyid2", consentinstance);
            addProps.setProperty("consentinstancestatus", "P");
            this.getActionProcessor().processAction("AddSDI", "1", addProps);
            DataSet addDetails = new DataSet();
            addDetails.addColumnValues("keyid1", 0, participantid, ";");
            addDetails.addColumnValues("keyid2", 0, consentinstance, ";");
            addDetails.addColumnValues(PROPERTY_QUESTIONID, 0, consentQuestions.getColumnValues(PROPERTY_QUESTIONID, ";"), ";");
            addDetails.addColumnValues(PROPERTY_QUESTIONTEXT, 0, consentQuestions.getColumnValues(PROPERTY_QUESTIONTEXT, ";"), ";");
            addDetails.addColumnValues("answer", 0, consentQuestions.getColumnValues("answer", ";"), ";");
            addDetails.addColumnValues("answerdt", 0, consentQuestions.getColumnValues("answerdt", ";"), ";");
            addDetails.addColumnValues(PROPERTY_ANSWERGRANTED, 0, consentQuestions.getColumnValues(PROPERTY_ANSWERGRANTED, ";"), ";");
            addDetails.addColumnValues(PROPERTY_ANSWEREDITORSTYLEID, 0, consentQuestions.getColumnValues(PROPERTY_ANSWEREDITORSTYLEID, ";"), ";");
            addDetails.addColumnValues(PROPERTY_CONSENTTYPE, 0, consentQuestions.getColumnValues(PROPERTY_CONSENTTYPE, ";"), ";");
            addDetails.addColumnValues("usersequence", 0, consentQuestions.getColumnValues("usersequence", ";"), ";");
            addDetails.addColumnValues("answerstatus", 0, "P", ";");
            addDetails.padColumn("keyid1");
            addDetails.padColumn("keyid2");
            addDetails.padColumn("answerstatus");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_ParticipantConsent");
            detailProps.setProperty("linkid", "consent responses");
            for (int i = 0; i < addDetails.getRowCount(); ++i) {
                detailProps.setProperty("keyid1", addDetails.getString(i, "keyid1", ""));
                detailProps.setProperty("keyid2", addDetails.getString(i, "keyid2", ""));
                detailProps.setProperty(PROPERTY_QUESTIONID, addDetails.getString(i, PROPERTY_QUESTIONID, ""));
                detailProps.setProperty(PROPERTY_QUESTIONTEXT, addDetails.getString(i, PROPERTY_QUESTIONTEXT, ""));
                detailProps.setProperty("answer", addDetails.getString(i, "answer", ""));
                detailProps.setProperty(PROPERTY_ANSWERGRANTED, addDetails.getString(i, PROPERTY_ANSWERGRANTED, ""));
                String answerStatus = "P";
                if (addDetails.getString(i, "answer", "").length() > 0) {
                    answerStatus = addDetails.getString(i, "answer", "").equals(addDetails.getString(i, PROPERTY_ANSWERGRANTED, "")) ? "G" : "R";
                }
                detailProps.setProperty("answerstatus", answerStatus);
                detailProps.setProperty("answerdt", addDetails.getValue(i, "answerdt", ""));
                detailProps.setProperty(PROPERTY_ANSWEREDITORSTYLEID, addDetails.getString(i, PROPERTY_ANSWEREDITORSTYLEID, ""));
                detailProps.setProperty(PROPERTY_CONSENTTYPE, addDetails.getString(i, PROPERTY_CONSENTTYPE, ""));
                detailProps.setProperty("usersequence", addDetails.getString(i, "usersequence", ""));
                this.getActionProcessor().processAction("AddSDIDetail", "1", detailProps);
            }
        }
    }
}

