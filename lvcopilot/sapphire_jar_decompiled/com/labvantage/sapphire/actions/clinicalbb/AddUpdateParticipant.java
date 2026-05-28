/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddUpdateParticipant
extends BaseAction
implements sapphire.action.AddUpdateParticipant {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103910 $";
    private static ArrayList<String> nonUpdatableCols = new ArrayList();
    private Map<String, String> actionCache = new HashMap<String, String>();

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String columnid;
        String participantid;
        String externalparticipantid;
        String sstudyid;
        String subjectid;
        String columnid2;
        long startime = System.currentTimeMillis();
        PropertyList bioBankingPolicy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        boolean policy_participantStudySubjectOnly = "Study-Subject".equals(bioBankingPolicy.getProperty("participantcreationrule"));
        boolean policy_autoCreateSubject = "Y".equals(bioBankingPolicy.getProperty("autocreatesubject"));
        String mode = actionProps.getProperty("mode", "unknown");
        TranslationProcessor tp = this.getTranslationProcessor();
        this.validateInputParams(actionProps, tp);
        DataSet inputParamsDSOrig = this.createDSFromInputParams(actionProps);
        HashMap<String, String> studyTypeMap = new HashMap<String, String>();
        String auditreason = actionProps.getProperty("auditreason", "");
        String auditactivity = actionProps.getProperty("auditactivity", "");
        String auditsignedflag = actionProps.getProperty("auditsignedflag", "N");
        actionProps.deleteProperty("auditreason");
        actionProps.deleteProperty("auditactivity");
        actionProps.deleteProperty("auditsignedflag");
        for (int i = 0; i < inputParamsDSOrig.size(); ++i) {
            inputParamsDSOrig.setString(i, "__row", String.valueOf(i));
            String participantid2 = inputParamsDSOrig.getString(i, "participantid", "");
            String subjectid2 = inputParamsDSOrig.getString(i, "subjectid", "");
            if (participantid2.length() > 0) {
                if (subjectid2.length() != 0) continue;
                subjectid2 = this.getSubjectFromParticipant(participantid2);
                inputParamsDSOrig.setString(i, "subjectid", subjectid2);
                continue;
            }
            if (participantid2.length() != 0) continue;
            String sstudyid2 = inputParamsDSOrig.getString(i, "sstudyid", "");
            String studyalias = inputParamsDSOrig.getString(i, "studyalias", "");
            String studysiteid = inputParamsDSOrig.getString(i, "studysiteid", "");
            if (sstudyid2.length() == 0) {
                if (studyalias.length() > 0) {
                    sstudyid2 = this.getStudyIDfromStudyCode(studyalias);
                } else if (studysiteid.length() > 0) {
                    sstudyid2 = this.getStudyIDfromSite(studysiteid);
                }
                if (OpalUtil.isNotEmpty(sstudyid2)) {
                    inputParamsDSOrig.setString(i, "sstudyid", sstudyid2);
                } else {
                    throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Arguments:  (Participant ID) or (Study ID & Subject ID) or (Site ID & External Participant ID) or (Study ID & External Participant ID)"));
                }
            }
            if (!studyTypeMap.containsKey(sstudyid2)) {
                studyTypeMap.put(sstudyid2, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "clinicalflag", "s_studyid=?", new String[]{sstudyid2}));
            }
            if (studysiteid.length() == 0) {
                String sitedepartment = inputParamsDSOrig.getString(i, "sitedepartment", "");
                String sitedesc = inputParamsDSOrig.getString(i, "sitedesc", "");
                if (sitedepartment.length() > 0) {
                    studysiteid = this.getStudySiteIDfromDepartment(sstudyid2, sitedepartment);
                } else if (sitedesc.length() > 0) {
                    studysiteid = this.getStudySiteIDfromSiteName(sstudyid2, sitedesc);
                }
                if (OpalUtil.isEmpty(studysiteid)) {
                    if (subjectid2.length() == 0) {
                        throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Arguments:  (Participant ID) or (Study ID & Subject ID) or (Site ID & External Participant ID) or (Study ID & External Participant ID)"));
                    }
                } else {
                    inputParamsDSOrig.setString(i, "studysiteid", studysiteid);
                }
            }
            if (subjectid2.length() > 0) {
                sstudyid2 = inputParamsDSOrig.getString(i, "sstudyid", "");
                studysiteid = inputParamsDSOrig.getString(i, "studysiteid", "");
                participantid2 = policy_participantStudySubjectOnly || OpalUtil.isEmpty(studysiteid) ? this.getParticipantFromStudySubject(sstudyid2, subjectid2) : this.getParticipantFromStudySubjectSite(sstudyid2, studysiteid, subjectid2);
                inputParamsDSOrig.setString(i, "participantid", participantid2);
                continue;
            }
            String externalparticipantid2 = inputParamsDSOrig.getString(i, "externalparticipantid", "");
            if (externalparticipantid2.length() <= 0) continue;
            sstudyid2 = inputParamsDSOrig.getString(i, "sstudyid", "");
            if (policy_participantStudySubjectOnly) {
                participantid2 = this.getParticipantFromStudyExternalParticipant(sstudyid2, externalparticipantid2);
            } else {
                studysiteid = inputParamsDSOrig.getString(i, "studysiteid", "");
                if (studysiteid.length() > 0) {
                    participantid2 = this.getParticipantFromStudySiteExternalParticipant(sstudyid2, studysiteid, externalparticipantid2);
                }
            }
            inputParamsDSOrig.setString(i, "participantid", participantid2);
        }
        HashSet<String> participantSet = new HashSet<String>();
        HashSet<String> subjectSet = new HashSet<String>();
        HashSet<String> externalParticipantStudySubjectSet = new HashSet<String>();
        DataSet updateParticipantDS = new DataSet();
        DataSet updateSubjectDS = new DataSet();
        DataSet addParticipantDS = new DataSet();
        DataSet addParticipantWithSiteDS = new DataSet();
        DataSet addSubjectDS = new DataSet();
        DataSet addSubjectWithSiteDS = new DataSet();
        for (int i = 0; i < inputParamsDSOrig.size(); ++i) {
            boolean validArguments = false;
            String sstudyid3 = inputParamsDSOrig.getString(i, "sstudyid", "");
            String studysiteid = inputParamsDSOrig.getString(i, "studysiteid", "");
            String subjectid3 = inputParamsDSOrig.getString(i, "subjectid", "");
            String participantid3 = inputParamsDSOrig.getString(i, "participantid", "");
            String cpcohortid = inputParamsDSOrig.getString(i, "cpcohortid", "");
            String externalparticipantid3 = inputParamsDSOrig.getString(i, "externalparticipantid", "");
            if (participantid3.length() > 0) {
                validArguments = true;
                if (participantSet.add(participantid3)) {
                    updateParticipantDS.copyRow(inputParamsDSOrig, i, 1);
                }
                if (subjectid3.length() > 0 && subjectSet.add(subjectid3)) {
                    updateSubjectDS.copyRow(inputParamsDSOrig, i, 1);
                }
            } else {
                if ("Y".equalsIgnoreCase((String)studyTypeMap.get(sstudyid3)) && cpcohortid.length() == 0) {
                    throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Arguments:  (Participant ID) or (Study ID & Subject ID) or (Site ID & External Participant ID) or (Study ID & External Participant ID)") + ". " + this.getTranslationProcessor().translate("Missing Cohort."));
                }
                if (externalparticipantid3.length() > 0) {
                    String columnid3;
                    int col;
                    int row;
                    validArguments = true;
                    if (policy_participantStudySubjectOnly) {
                        if (externalParticipantStudySubjectSet.add("participantwithoutsite-" + sstudyid3 + ";" + externalparticipantid3 + ";" + subjectid3)) {
                            if (subjectid3.length() == 0) {
                                row = addSubjectDS.addRow();
                                addSubjectDS.setString(row, "__sstudyid", sstudyid3);
                                addSubjectDS.setString(row, "__externalparticipantid", externalparticipantid3);
                                for (col = 0; col < inputParamsDSOrig.getColumnCount(); ++col) {
                                    columnid3 = inputParamsDSOrig.getColumnId(col).toLowerCase();
                                    if (!columnid3.startsWith("subject_")) continue;
                                    addSubjectDS.setString(row, columnid3.substring(8), inputParamsDSOrig.getValue(i, columnid3, ""));
                                }
                            } else {
                                HashMap<String, String> filter = new HashMap<String, String>();
                                filter.put("sstudyid", sstudyid3);
                                filter.put("externalparticipantid", externalparticipantid3);
                                if (addParticipantDS.findRow(filter) != -1) {
                                    throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Multiple Participant") + ". " + this.getTranslationProcessor().translate("Creation of multiple participant for same external participant is not allowed"));
                                }
                            }
                            if (inputParamsDSOrig.getString(i, "enrolldt", "").length() == 0) {
                                inputParamsDSOrig.setString(i, "enrolldt", "n");
                            }
                            addParticipantDS.copyRow(inputParamsDSOrig, i, 1);
                        }
                    } else if (externalParticipantStudySubjectSet.add("participantwithsite-" + sstudyid3 + ";" + externalparticipantid3 + ";" + studysiteid + ";" + subjectid3)) {
                        if (subjectid3.length() == 0) {
                            row = addSubjectWithSiteDS.addRow();
                            addSubjectWithSiteDS.setString(row, "__sstudyid", sstudyid3);
                            addSubjectWithSiteDS.setString(row, "__externalparticipantid", externalparticipantid3);
                            addSubjectWithSiteDS.setString(row, "__studysiteid", studysiteid);
                            for (col = 0; col < inputParamsDSOrig.getColumnCount(); ++col) {
                                columnid3 = inputParamsDSOrig.getColumnId(col).toLowerCase();
                                if (!columnid3.startsWith("subject_")) continue;
                                addSubjectWithSiteDS.setString(row, columnid3.substring(8), inputParamsDSOrig.getValue(i, columnid3, ""));
                            }
                        } else {
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("sstudyid", sstudyid3);
                            filter.put("externalparticipantid", externalparticipantid3);
                            filter.put("studysiteid", studysiteid);
                            if (addParticipantWithSiteDS.findRow(filter) != -1) {
                                throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Multiple Participant") + ". " + this.getTranslationProcessor().translate("Creation of multiple participant for same external participant is not allowed"));
                            }
                        }
                        if (inputParamsDSOrig.getString(i, "enrolldt", "").length() == 0) {
                            inputParamsDSOrig.setString(i, "enrolldt", "n");
                        }
                        addParticipantWithSiteDS.copyRow(inputParamsDSOrig, i, 1);
                    }
                } else if (subjectid3.length() > 0) {
                    validArguments = true;
                    if (policy_participantStudySubjectOnly || OpalUtil.isEmpty(studysiteid)) {
                        if (externalParticipantStudySubjectSet.add("subjectwithoutsite" + sstudyid3 + ";" + subjectid3)) {
                            if (inputParamsDSOrig.getString(i, "enrolldt", "").length() == 0) {
                                inputParamsDSOrig.setString(i, "enrolldt", "n");
                            }
                            addParticipantDS.copyRow(inputParamsDSOrig, i, 1);
                        }
                    } else if (externalParticipantStudySubjectSet.add("subjectwithsite" + sstudyid3 + ";" + subjectid3 + ";" + studysiteid)) {
                        if (inputParamsDSOrig.getString(i, "enrolldt", "").length() == 0) {
                            inputParamsDSOrig.setString(i, "enrolldt", "n");
                        }
                        addParticipantWithSiteDS.copyRow(inputParamsDSOrig, i, 1);
                    }
                }
            }
            if (validArguments) continue;
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Missing Required Arguments:  (Participant ID) or (Study ID & Subject ID) or (Site ID & External Participant ID) or (Study ID & External Participant ID)"));
        }
        if ("edit".equalsIgnoreCase(mode)) {
            if (addParticipantDS.size() > 0 || addParticipantWithSiteDS.size() > 0) {
                throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("Editing failed. Participant not found"));
            }
        } else if ("add".equalsIgnoreCase(mode) && updateParticipantDS.size() > 0) {
            throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("Subject already enrolled"));
        }
        if (!(policy_autoCreateSubject || addSubjectDS.size() <= 0 && addSubjectWithSiteDS.size() <= 0)) {
            throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("Subject can not be automatically created as per BioBanking Policy"));
        }
        PropertyList props = new PropertyList();
        HashMap<String, String> filter = new HashMap<String, String>();
        if (addSubjectDS.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "LV_Subject");
            props.setProperty("copies", String.valueOf(addSubjectDS.size()));
            for (int col = 0; col < addSubjectDS.getColumnCount(); ++col) {
                columnid2 = addSubjectDS.getColumnId(col);
                if (columnid2.startsWith("__")) continue;
                props.setProperty(columnid2, addSubjectDS.getColumnValues(columnid2, ";"));
            }
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            addSubjectDS.addColumnValues("subjectid", 0, props.getProperty("newkeyid1"), ";");
            for (int i = 0; i < addSubjectDS.size(); ++i) {
                subjectid = addSubjectDS.getString(i, "subjectid", "");
                sstudyid = addSubjectDS.getString(i, "__sstudyid", "");
                externalparticipantid = addSubjectDS.getString(i, "__externalparticipantid", "");
                filter.clear();
                filter.put("sstudyid", sstudyid);
                filter.put("externalparticipantid", externalparticipantid);
                int row = addParticipantDS.findRow(filter);
                if (row == -1) continue;
                addParticipantDS.setString(row, "subjectid", subjectid);
            }
        }
        if (addSubjectWithSiteDS.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "LV_Subject");
            props.setProperty("copies", String.valueOf(addSubjectWithSiteDS.size()));
            for (int col = 0; col < addSubjectWithSiteDS.getColumnCount(); ++col) {
                columnid2 = addSubjectWithSiteDS.getColumnId(col);
                if (columnid2.startsWith("__")) continue;
                props.setProperty(columnid2, addSubjectWithSiteDS.getColumnValues(columnid2, ";"));
            }
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            addSubjectWithSiteDS.addColumnValues("subjectid", 0, props.getProperty("newkeyid1"), ";");
            for (int i = 0; i < addSubjectWithSiteDS.size(); ++i) {
                subjectid = addSubjectWithSiteDS.getString(i, "subjectid", "");
                sstudyid = addSubjectWithSiteDS.getString(i, "__sstudyid", "");
                String studysiteid = addSubjectWithSiteDS.getString(i, "__studysiteid", "");
                String externalparticipantid4 = addSubjectWithSiteDS.getString(i, "__externalparticipantid", "");
                filter.clear();
                filter.put("sstudyid", sstudyid);
                filter.put("externalparticipantid", externalparticipantid4);
                filter.put("studysiteid", studysiteid);
                int row = addParticipantWithSiteDS.findRow(filter);
                if (row == -1) continue;
                addParticipantWithSiteDS.setString(row, "subjectid", subjectid);
            }
        }
        if (addParticipantDS.size() > 0) {
            int i;
            for (i = 0; i < addParticipantDS.size(); ++i) {
                if ("Y".equals(addParticipantDS.getString(i, "autoPartEnrollment", "N"))) {
                    addParticipantDS.setString(i, "participantstatus", "Enrolled");
                    continue;
                }
                addParticipantDS.setString(i, "participantstatus", "Pending");
                addParticipantDS.setString(i, "enrolldt", "");
            }
            props.clear();
            props.setProperty("sdcid", "LV_Participant");
            props.setProperty("copies", String.valueOf(addParticipantDS.size()));
            props.setProperty("participantstatus", addParticipantDS.getColumnValues("participantstatus", ";"));
            props.setProperty("sstudyid", addParticipantDS.getColumnValues("sstudyid", ";"));
            props.setProperty("subjectid", addParticipantDS.getColumnValues("subjectid", ";"));
            props.setProperty("cpcohortid", addParticipantDS.getColumnValues("cpcohortid", ";"));
            props.setProperty("externalparticipantid", addParticipantDS.getColumnValues("externalparticipantid", ";"));
            props.setProperty("studysiteid", addParticipantDS.getColumnValues("studysiteid", ";"));
            props.setProperty("enrolldt", addParticipantDS.getColumnValues("enrolldt", ";"));
            for (i = 0; i < addParticipantDS.getColumnCount(); ++i) {
                columnid2 = addParticipantDS.getColumnId(i);
                if (!columnid2.startsWith("participant_")) continue;
                props.setProperty(columnid2.substring(12), addParticipantDS.getColumnValues(columnid2, ";"));
            }
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            addParticipantDS.addColumnValues("participantid", 0, props.getProperty("newkeyid1"), ";");
            for (i = 0; i < addParticipantDS.size(); ++i) {
                participantid = addParticipantDS.getString(i, "participantid");
                sstudyid = addParticipantDS.getString(i, "sstudyid");
                externalparticipantid = addParticipantDS.getString(i, "externalparticipantid", "");
                String subjectid4 = addParticipantDS.getString(i, "subjectid", "");
                for (int row = 0; row < inputParamsDSOrig.size(); ++row) {
                    String _participantid = inputParamsDSOrig.getString(row, "participantid", "");
                    String _sstudyid = inputParamsDSOrig.getString(row, "sstudyid");
                    String _subjectid = inputParamsDSOrig.getString(row, "subjectid", "");
                    String _externalparticipantid = inputParamsDSOrig.getString(row, "externalparticipantid");
                    if (_participantid.length() != 0 || !sstudyid.equals(_sstudyid)) continue;
                    if (subjectid4.equals(_subjectid)) {
                        inputParamsDSOrig.setString(row, "participantid", participantid);
                        continue;
                    }
                    if (!externalparticipantid.equals(_externalparticipantid)) continue;
                    inputParamsDSOrig.setString(row, "participantid", participantid);
                }
            }
        }
        if (addParticipantWithSiteDS.size() > 0) {
            int i;
            for (i = 0; i < addParticipantWithSiteDS.size(); ++i) {
                if ("Y".equals(addParticipantWithSiteDS.getString(i, "autoPartEnrollment", "N"))) {
                    addParticipantWithSiteDS.setString(i, "participantstatus", "Enrolled");
                    continue;
                }
                addParticipantWithSiteDS.setString(i, "participantstatus", "Pending");
                addParticipantWithSiteDS.setString(i, "enrolldt", "");
            }
            props.clear();
            props.setProperty("sdcid", "LV_Participant");
            props.setProperty("copies", String.valueOf(addParticipantWithSiteDS.size()));
            props.setProperty("participantstatus", addParticipantWithSiteDS.getColumnValues("participantstatus", ";"));
            props.setProperty("sstudyid", addParticipantWithSiteDS.getColumnValues("sstudyid", ";"));
            props.setProperty("subjectid", addParticipantWithSiteDS.getColumnValues("subjectid", ";"));
            props.setProperty("cpcohortid", addParticipantWithSiteDS.getColumnValues("cpcohortid", ";"));
            props.setProperty("studysiteid", addParticipantWithSiteDS.getColumnValues("studysiteid", ";"));
            props.setProperty("externalparticipantid", addParticipantWithSiteDS.getColumnValues("externalparticipantid", ";"));
            props.setProperty("enrolldt", addParticipantWithSiteDS.getColumnValues("enrolldt", ";"));
            for (i = 0; i < addParticipantWithSiteDS.getColumnCount(); ++i) {
                columnid2 = addParticipantWithSiteDS.getColumnId(i);
                if (!columnid2.startsWith("participant_")) continue;
                props.setProperty(columnid2.substring(12), addParticipantWithSiteDS.getColumnValues(columnid2, ";"));
            }
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            addParticipantWithSiteDS.addColumnValues("participantid", 0, props.getProperty("newkeyid1"), ";");
            for (i = 0; i < addParticipantWithSiteDS.size(); ++i) {
                participantid = addParticipantWithSiteDS.getString(i, "participantid");
                sstudyid = addParticipantWithSiteDS.getString(i, "sstudyid");
                externalparticipantid = addParticipantWithSiteDS.getString(i, "externalparticipantid", "");
                String studysiteid = addParticipantWithSiteDS.getString(i, "studysiteid", "");
                String subjectid5 = addParticipantWithSiteDS.getString(i, "subjectid", "");
                for (int row = 0; row < inputParamsDSOrig.size(); ++row) {
                    String _participantid = inputParamsDSOrig.getString(row, "participantid", "");
                    String _sstudyid = inputParamsDSOrig.getString(row, "sstudyid");
                    String _subjectid = inputParamsDSOrig.getString(row, "subjectid", "");
                    String _externalparticipantid = inputParamsDSOrig.getString(row, "externalparticipantid");
                    String _studysiteid = inputParamsDSOrig.getString(row, "studysiteid");
                    if (_participantid.length() != 0 || !sstudyid.equals(_sstudyid)) continue;
                    if (subjectid5.equals(_subjectid)) {
                        if (policy_participantStudySubjectOnly) {
                            inputParamsDSOrig.setString(row, "participantid", participantid);
                            continue;
                        }
                        if (!studysiteid.equals(_studysiteid)) continue;
                        inputParamsDSOrig.setString(row, "participantid", participantid);
                        continue;
                    }
                    if (!externalparticipantid.equals(_externalparticipantid)) continue;
                    if (policy_participantStudySubjectOnly) {
                        inputParamsDSOrig.setString(row, "participantid", participantid);
                        continue;
                    }
                    if (!studysiteid.equals(_studysiteid)) continue;
                    inputParamsDSOrig.setString(row, "participantid", participantid);
                }
            }
        }
        if (updateParticipantDS.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "LV_Participant");
            props.setProperty("keyid1", updateParticipantDS.getColumnValues("participantid", ";"));
            boolean updateParticipant = false;
            for (int i = 0; i < updateParticipantDS.getColumnCount(); ++i) {
                columnid = updateParticipantDS.getColumnId(i);
                if (!columnid.startsWith("participant_")) continue;
                updateParticipant = true;
                props.setProperty(columnid.substring(12), updateParticipantDS.getColumnValues(columnid, ";"));
            }
            if (updateParticipant) {
                props.setProperty("auditreason", auditreason);
                props.setProperty("auditactivity", auditactivity);
                props.setProperty("auditsignedflag", auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (updateSubjectDS.size() > 0) {
            props.clear();
            props.setProperty("sdcid", "LV_Subject");
            props.setProperty("keyid1", updateSubjectDS.getColumnValues("subjectid", ";"));
            boolean updateSubject = false;
            for (int i = 0; i < updateSubjectDS.getColumnCount(); ++i) {
                columnid = updateSubjectDS.getColumnId(i);
                if (!columnid.startsWith("subject_")) continue;
                updateSubject = true;
                props.setProperty(columnid.substring(8), updateSubjectDS.getColumnValues(columnid, ";"));
            }
            if (updateSubject) {
                props.setProperty("auditreason", auditreason);
                props.setProperty("auditactivity", auditactivity);
                props.setProperty("auditsignedflag", auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        actionProps.setProperty("newparticipantid", inputParamsDSOrig.getColumnValues("participantid", ";"));
        this.logger.info("AddUpdateParticipant action took " + (System.currentTimeMillis() - startime) + " ms.");
    }

    private String getParticipantFromStudyExternalParticipant(String sstudyid, String externalparticpantid) {
        String key = "participantfromstudyexternalparticipant-" + sstudyid + ":" + externalparticpantid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "s_participantid", "sstudyid=? and externalparticipantid=?", new String[]{sstudyid, externalparticpantid}));
        }
        return this.actionCache.get(key);
    }

    private String getParticipantFromStudySiteExternalParticipant(String sstudyid, String studysiteid, String externalparticpantid) {
        String key = "participantfromstudysiteexternalparticipant-" + sstudyid + ":" + studysiteid + ":" + externalparticpantid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "s_participantid", "sstudyid=? and studysiteid=? and externalparticipantid=?", new String[]{sstudyid, studysiteid, externalparticpantid}));
        }
        return this.actionCache.get(key);
    }

    private String getParticipantFromStudySubjectSite(String sstudyid, String studysiteid, String subjectid) {
        String key = "participantfromstudysubjectsite-" + sstudyid + ":" + subjectid + ":" + studysiteid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "s_participantid", "sstudyid=? and subjectid=? and studysiteid=?", new String[]{sstudyid, subjectid, studysiteid}));
        }
        return this.actionCache.get(key);
    }

    private String getParticipantFromStudySubject(String sstudyid, String subjectid) {
        String key = "participantfromstudysubject-" + sstudyid + ":" + subjectid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "s_participantid", "sstudyid=? and subjectid=?", new String[]{sstudyid, subjectid}));
        }
        return this.actionCache.get(key);
    }

    private String getSubjectFromParticipant(String participantid) {
        String key = "subjectfromparticipant-" + participantid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "subjectid", "s_participantid=?", new String[]{participantid}));
        }
        return this.actionCache.get(key);
    }

    private String getStudyIDfromStudyCode(String studycode) {
        String key = "studyidfromstudycode-" + studycode;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "s_studyid", "studyalias=?", new String[]{studycode}));
        }
        return this.actionCache.get(key);
    }

    private String getStudyIDfromSite(String studysiteid) {
        String key = "studyforsite-" + studysiteid;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "sstudyid", "s_studysiteid=?", new String[]{studysiteid}));
        }
        return this.actionCache.get(key);
    }

    private String getStudySiteIDfromSiteName(String studyid, String sitename) {
        String key = "siteidfromname-" + studyid + ":" + sitename;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "s_studysiteid", "sstudyid=? and studysitedesc=?", new String[]{studyid, sitename}));
        }
        return this.actionCache.get(key);
    }

    private String getStudySiteIDfromDepartment(String studyid, String department) {
        String key = "siteidfromdepartment-" + studyid + ":" + department;
        if (!this.actionCache.containsKey(key)) {
            this.actionCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "s_studysiteid", "sstudyid=? and departmentid=?", new String[]{studyid, department}));
        }
        return this.actionCache.get(key);
    }

    private void validateInputParams(PropertyList properties, TranslationProcessor tp) throws SapphireException {
        int maxlength = this.findMaxLength(properties);
        for (Object o : properties.keySet()) {
            String param = (String)o;
            String value = properties.getProperty(param);
            int length = StringUtil.split(value, ";").length;
            if (maxlength <= length || length <= 1) continue;
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Invalid Input:") + param + " " + tp.translate("Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values."));
        }
    }

    private int findMaxLength(PropertyList properties) {
        int maxlength = 0;
        for (Object o : properties.keySet()) {
            String param = (String)o;
            String value = properties.getProperty(param);
            int l = StringUtil.split(value, ";").length;
            if (l <= maxlength) continue;
            maxlength = l;
        }
        return maxlength;
    }

    private DataSet createDSFromInputParams(PropertyList actionProps) {
        DataSet ds = new DataSet();
        ds.addColumnValues("sstudyid", 0, actionProps.getProperty("studyid", ""), ";");
        ds.addColumnValues("studyalias", 0, actionProps.getProperty("studycode", ""), ";");
        ds.addColumnValues("studysiteid", 0, actionProps.getProperty("siteid", ""), ";");
        ds.addColumnValues("clinicalprotocolrevision", 0, actionProps.getProperty("protocolrevision", ""), ";");
        ds.addColumnValues("cpcohortid", 0, actionProps.getProperty("cohortid", ""), ";");
        ds.addColumnValues("participantid", 0, actionProps.getProperty("participantid", ""), ";");
        ds.addColumnValues("externalparticipantid", 0, actionProps.getProperty("externalparticipantid", ""), ";");
        ds.addColumnValues("subjectid", 0, actionProps.getProperty("subjectid", ""), ";");
        ds.addColumnValues("enrolldt", 0, actionProps.getProperty("participantenrollmentdt", ""), ";");
        ds.addColumnValues("autoPartEnrollment", 0, actionProps.getProperty("autopartenrollment", "Y"), ";");
        ds.addColumnValues("sitedesc", 0, actionProps.getProperty("sitename", ""), ";");
        ds.addColumnValues("sitedepartment", 0, actionProps.getProperty("departmentid", ""), ";");
        ds = this.addParamsToDataSet("LV_Participant", "participant_", ds, actionProps);
        ds = this.addParamsToDataSet("LV_Subject", "subject_", ds, actionProps);
        ds.padColumns();
        return ds;
    }

    private DataSet addParamsToDataSet(String sdcid, String colPrefix, DataSet ds, PropertyList actionProps) {
        PropertyListCollection columns = this.getSDCProcessor().getColumns(sdcid);
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (!actionProps.containsKey(colPrefix + colid) || nonUpdatableCols.contains(colid)) continue;
            ds.addColumnValues(colPrefix + colid, 0, actionProps.getProperty(colPrefix + colid, ""), ";");
        }
        return ds;
    }

    static {
        nonUpdatableCols.add("moddt");
        nonUpdatableCols.add("modby");
        nonUpdatableCols.add("modtool");
        nonUpdatableCols.add("createdt");
        nonUpdatableCols.add("createby");
        nonUpdatableCols.add("createtool");
        nonUpdatableCols.add("auditsequence");
        nonUpdatableCols.add("templateflag");
    }

    static class UserMessages {
        static final String MISSINGREQARGS = "Missing Required Arguments:  (Participant ID) or (Study ID & Subject ID) or (Site ID & External Participant ID) or (Study ID & External Participant ID)";
        static final String SUBJECT = "subject";
        static final String SUBJECTALREADYENROLLED = "Subject already enrolled in Study";
        static final String SUBJECTCANNOTBECREATED = "Subject can not be automatically created as per BioBanking Policy";
        static final String PARTICIPANTNOTFOUND = "Editing failed. Participant not found";
        static final String INVALIDINPUT = "Invalid Input:";
        static final String MULTIPARAMINPUT = "Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values.";
        static final String CANNOTBEUPDATED = "param can not be updated";
        static final String COHORTIDREQD = "CohortId is required to enroll participant";

        UserMessages() {
        }
    }
}

