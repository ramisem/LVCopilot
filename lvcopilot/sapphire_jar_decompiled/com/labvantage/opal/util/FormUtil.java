/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FormUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static PropertyList addBlankDocument(QueryProcessor queryProcessor, ActionProcessor actionProcessor, String sql, String sdcid, String keyid1, String studyid, String studysiteid) throws ActionException {
        PropertyList props = new PropertyList();
        DataSet ds = queryProcessor.getSqlDataSet(sql.toString());
        if (ds != null && ds.isValidColumn("formid") && ds.isValidColumn("formversionid")) {
            for (int row = 0; row < ds.size(); ++row) {
                String formid = ds.getValue(row, "formid");
                String formversionid = ds.getValue(row, "formversionid", ds.getValue(row, "defaultformversionid", "1"));
                if (StringUtil.getLen(formid) <= 0L || StringUtil.getLen(formversionid) <= 0L) continue;
                props.setProperty("formid", formid);
                props.setProperty("formversionid", formversionid);
                if (StringUtil.getLen(studyid) > 0L) {
                    props.setProperty("studyid", studyid);
                }
                if (StringUtil.getLen(studysiteid) > 0L) {
                    props.setProperty("studysitename", studysiteid);
                }
                actionProcessor.processAction("AddDocument", "1", props);
                String documentid = props.getProperty("documentid");
                String documentversionid = props.getProperty("documentversionid");
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", keyid1);
                props.setProperty("documentid", documentid);
                props.setProperty("documentversionid", documentversionid);
                actionProcessor.processAction("AddSDIDocument", "1", props);
            }
        }
        return props;
    }

    public static PropertyList addBlankDocument(DataSet ds, ActionProcessor actionProcessor, String sdcid, String keyid1, String studyid, String studysiteid) throws ActionException {
        PropertyList props = new PropertyList();
        if (ds != null && ds.isValidColumn("formid") && ds.isValidColumn("formversionid")) {
            for (int row = 0; row < ds.size(); ++row) {
                String formid = ds.getValue(row, "formid");
                String formversionid = ds.getValue(row, "formversionid", ds.getValue(row, "defaultformversionid", "1"));
                if (StringUtil.getLen(formid) <= 0L || StringUtil.getLen(formversionid) <= 0L) continue;
                props.setProperty("formid", formid);
                props.setProperty("formversionid", formversionid);
                props.setProperty("studyid", ds.getValue(row, "sstudyid"));
                props.setProperty("siteid", ds.getValue(row, "studysiteid"));
                if (ds.isValidColumn("s_sampleid")) {
                    props.setProperty("sampleid", ds.getValue(row, "s_sampleid"));
                }
                if (sdcid.equals("LV_ParticipantEvent")) {
                    props.setProperty("participantid", ds.getValue(row, "s_participantid"));
                    props.setProperty("subjectid", ds.getValue(row, "subjectid"));
                    props.setProperty("clinicalprotocolrevision", ds.getValue(row, "clinicalprotocolrevision"));
                    props.setProperty("participanteventid", ds.getValue(row, "s_participanteventid"));
                } else if (sdcid.equals("LV_Subject")) {
                    props.setProperty("subjectid", ds.getValue(row, "subjectid"));
                }
                actionProcessor.processAction("AddDocument", "1", props);
                String documentid = props.getProperty("documentid");
                String documentversionid = props.getProperty("documentversionid");
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", keyid1);
                props.setProperty("documentid", documentid);
                props.setProperty("documentversionid", documentversionid);
                actionProcessor.processAction("AddSDIDocument", "1", props);
            }
        }
        return props;
    }

    public static void addBlankDocument(DataSet ds, ActionProcessor actionProcessor, String sdcid, String keycolid1) throws ActionException {
        PropertyList props = new PropertyList();
        if (ds != null && ds.isValidColumn("formid") && ds.isValidColumn("formversionid")) {
            for (int row = 0; row < ds.size(); ++row) {
                String formid = ds.getValue(row, "formid");
                String formversionid = ds.getValue(row, "formversionid", ds.getValue(row, "defaultformversionid", "1"));
                if (StringUtil.getLen(formid) <= 0L || StringUtil.getLen(formversionid) <= 0L) continue;
                props.setProperty("formid", formid);
                props.setProperty("formversionid", formversionid);
                props.setProperty("studyid", ds.getValue(row, "sstudyid"));
                props.setProperty("siteid", ds.getValue(row, "studysiteid"));
                if (ds.isValidColumn("s_sampleid")) {
                    props.setProperty("sampleid", ds.getValue(row, "s_sampleid"));
                }
                if (sdcid.equals("LV_Participant")) {
                    props.setProperty("participantid", ds.getValue(row, "s_participantid"));
                    props.setProperty("subjectid", ds.getValue(row, "subjectid"));
                    props.setProperty("clinicalprotocolrevision", ds.getValue(row, "clinicalprotocolrevision"));
                    props.setProperty("cohort", ds.getValue(row, "cpcohortid"));
                } else if (sdcid.equals("LV_ParticipantEvent")) {
                    props.setProperty("participantid", ds.getValue(row, "s_participantid"));
                    props.setProperty("subjectid", ds.getValue(row, "subjectid"));
                    props.setProperty("clinicalprotocolrevision", ds.getValue(row, "clinicalprotocolrevision"));
                    props.setProperty("participanteventid", ds.getValue(row, "s_participanteventid"));
                    props.setProperty("cohort", ds.getValue(row, "cpcohortid"));
                } else if (sdcid.equals("LV_SampleFamily")) {
                    props.setProperty("participantid", ds.getValue(row, "participantid"));
                    props.setProperty("samplefamilyid", ds.getValue(row, "s_samplefamilyid"));
                    props.setProperty("clinicalprotocolrevision", ds.getValue(row, "clinicalprotocolrevision"));
                    props.setProperty("participanteventid", ds.getValue(row, "participanteventid"));
                    props.setProperty("subjectid", ds.getValue(row, "subjectid"));
                }
                actionProcessor.processAction("AddDocument", "1", props);
                String documentid = props.getProperty("documentid");
                String documentversionid = props.getProperty("documentversionid");
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", ds.getValue(row, keycolid1));
                props.setProperty("documentid", documentid);
                props.setProperty("documentversionid", documentversionid);
                actionProcessor.processAction("AddSDIDocument", "1", props);
            }
        }
    }
}

