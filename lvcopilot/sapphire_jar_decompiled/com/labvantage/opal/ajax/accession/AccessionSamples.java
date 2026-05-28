/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.ajax.util.SetActionProgressStatus;
import com.labvantage.opal.util.ChildSampleUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAlias;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AccessionSamples
extends BaseAction {
    private static final String PARTICIPANT_CREATION_POLICY_STUDY_SUBJECT = "Study-Subject";
    private String participantcreationrule = "Study-Subject";
    private Map<String, String> localCache = new HashMap<String, String>();
    Map<String, Map<String, String>> copyDownDataCache = new HashMap<String, Map<String, String>>();

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String accessionid = actionProps.getProperty("accessionid");
        String data = actionProps.getProperty("data");
        String samplefamilynotes = actionProps.getProperty("samplefamilynotes");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        String defaultdepartment = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
        String sdcruleconfirm = actionProps.getProperty("sdcruleconfirm", "N");
        String attributes = actionProps.getProperty("attributes");
        String message = "";
        String sampleid = "";
        HashSet<String> markConsumedRowSet = new HashSet<String>();
        HashSet<String> markConsumedSampleSet = new HashSet<String>();
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        if (data.length() > 0) {
            try {
                Object errorHandler;
                int i;
                String columnid;
                JSONArray attributesArray = null;
                if (OpalUtil.isNotEmpty(attributes)) {
                    attributesArray = new JSONArray(attributes);
                }
                DataSet ds = new DataSet();
                JSONArray dataArray = new JSONArray(data);
                for (int i2 = 0; i2 < dataArray.length(); ++i2) {
                    int dsrow = ds.addRow();
                    JSONArray rowArray = dataArray.getJSONArray(i2);
                    for (int col = 0; col < rowArray.length(); ++col) {
                        String value;
                        JSONObject column = rowArray.getJSONObject(col);
                        columnid = column.has("columnid") ? column.getString("columnid") : "";
                        String string = value = column.has("value") ? column.getString("value") : "";
                        if (!OpalUtil.isNotEmpty(columnid)) continue;
                        ds.setString(dsrow, columnid, value);
                    }
                }
                PropertyList bbpolicy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                this.participantcreationrule = bbpolicy.getProperty("participantcreationrule", PARTICIPANT_CREATION_POLICY_STUDY_SUBJECT);
                PropertyList props = new PropertyList();
                DataSet childds = new DataSet();
                DataSet sampleds = new DataSet();
                for (i = 0; i < ds.size() && !OpalUtil.isNotEmpty(message); ++i) {
                    DataSet processds;
                    this.setActionStatus(configurationProcessor, accessionid, "||INFO||" + this.getTranslationProcessor().translate("Adding samples") + " (" + (i + 1) + "/" + ds.size() + ")...");
                    String studyid = ds.getString(i, "studyid");
                    String studysiteid = ds.getString(i, "studysiteid");
                    String cohortid = ds.getString(i, "cohortid");
                    String subjectid = ds.getString(i, "subjectid");
                    String participantid = "";
                    String externalparticipantid = ds.getString(i, "externalparticipantid");
                    String eventdefid = ds.getString(i, "eventdefid");
                    String clinicaleventid = ds.getString(i, "clinicaleventid");
                    String sampletypeid = ds.getString(i, "sampletypeid");
                    String specimendefid = ds.getString(i, "specimendefid");
                    String specimentype = ds.getString(i, "specimentype");
                    String string = ds.getString(i, "qtycurrent");
                    String qtyunits = ds.getString(i, "qtyunits");
                    int neweventinstance = "Y".equals(ds.getString(i, "neweventinstance", "N"));
                    boolean ischildsample = "Y".equals(ds.getString(i, "ischildsample"));
                    String processtype = "";
                    String processinstructions = "";
                    if (ischildsample) {
                        String parentrowuuid = ds.getString(i, "parentrowuuid", "");
                        String parentsampleid = ds.getString(i, "parentsampleid", "");
                        String childsample_plantype = "";
                        int row = childds.addRow();
                        String childsampleplanid = ds.getString(i, "childsampleplanid");
                        String childsampleplanversionid = ds.getString(i, "childsampleplanversionid");
                        String childsampleplanitemid = ds.getString(i, "childsampleplanitemid");
                        if (OpalUtil.isNotEmpty(childsampleplanid) && OpalUtil.isNotEmpty(childsampleplanversionid) && OpalUtil.isNotEmpty(childsampleplanitemid)) {
                            DataSet processds2 = this.getQueryProcessor().getPreparedSqlDataSet("select plantype, processtype, processinstruction from s_childsampleplanitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?", (Object[])new String[]{childsampleplanid, childsampleplanversionid, childsampleplanitemid});
                            if (processds2 != null && processds2.size() > 0) {
                                processtype = processds2.getString(0, "processtype", "");
                                processinstructions = processds2.getString(0, "processinstruction", "");
                                childsample_plantype = processds2.getString(0, "plantype", "");
                            }
                            childds.setString(row, "__childsampleplanid", childsampleplanid);
                            childds.setString(row, "__childsampleplanversionid", childsampleplanversionid);
                            childds.setString(row, "__childsampleplanitemid", childsampleplanitemid);
                            childds.setString(row, "__childsampleplantype", childsample_plantype);
                            if (this.isMarkParentConsumed(childsampleplanid, childsampleplanversionid)) {
                                markConsumedRowSet.add(parentrowuuid);
                            }
                        }
                        childds.setString(row, "__parentrowuuid", parentrowuuid);
                        childds.setString(row, "__parentsampleid", parentsampleid);
                        childds.setString(row, "sstudyid", studyid);
                        childds.setString(row, "sampletypeid", sampletypeid);
                        childds.setString(row, "processtype", processtype);
                        childds.setString(row, "processinstruction", processinstructions);
                        childds.setString(row, "__parentfamily_studysiteid", studysiteid);
                        childds.setString(row, "__parentfamily_clinicalprotocolid", studyid);
                        childds.setString(row, "__parentfamily_clinicalprotocolversionid", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolversionid"));
                        childds.setString(row, "__parentfamily_clinicalprotocolrevision", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolrevision"));
                        childds.setString(row, "__parentfamily_initialvolume", string);
                        childds.setString(row, "__parentfamily_initialvolumeunits", qtyunits);
                        childds.setString(row, "__parentfamily_sampletypeid", sampletypeid);
                        childds.setString(row, "__parentfamily_specimendefid", specimendefid);
                        childds.setString(row, "samplestatus", "Received");
                        childds.setString(row, "storagestatus", "Received");
                        if (OpalUtil.isNotEmpty(subjectid)) {
                            try {
                                participantid = this.getParticipantID(studyid, studysiteid, subjectid, cohortid, externalparticipantid);
                                childds.setString(row, "__parentfamily_subjectid", subjectid);
                                childds.setString(row, "__parentfamily_externalsubject", externalparticipantid);
                                childds.setString(row, "__parentfamily_participantid", participantid);
                                if (OpalUtil.isNotEmpty(eventdefid)) {
                                    String participanteventid = this.getParticipantEventID(participantid, eventdefid, studysiteid, attributesArray, neweventinstance != 0);
                                    childds.setString(row, "__parentfamily_participanteventid", participanteventid);
                                    childds.setString(row, "__parentfamily_clinicalevent", this.getProtocolEventDefLabel(eventdefid));
                                    childds.setString(row, "__parentfamily_eventdefid", eventdefid);
                                } else {
                                    childds.setString(row, "__parentfamily_participanteventid", "");
                                    childds.setString(row, "__parentfamily_clinicalevent", this.getNonProtocolEventLabel(clinicaleventid));
                                    childds.setString(row, "__parentfamily_eventdefid", "");
                                }
                            }
                            catch (ActionException e) {
                                message = "||ERROR||<span style='color:red;font-weight:bold;'>" + this.getTranslationProcessor().translate("Error adding new Participant record") + "</span><hr>";
                                message = message + "<div style='padding:10px 2px;'>";
                                ErrorHandler errorHandler2 = e.getErrorHandler();
                                if (errorHandler2 != null && errorHandler2.hasErrors()) {
                                    if ((message = message + errorHandler2.getLastErrorMessage()).endsWith("|")) {
                                        message = message + message.substring(0, message.length() - 1);
                                    }
                                } else {
                                    message = message + e.getMessage();
                                }
                                message = message + "</div>";
                            }
                        }
                        if (!OpalUtil.isEmpty(message)) continue;
                        childds.setString(row, "__trackitem_containertypeid", specimentype);
                        childds.setString(row, "__trackitem_qtycurrent", string);
                        childds.setString(row, "__trackitem_qtyunits", qtyunits);
                        childds.setString(row, "__trackitem_custodialuserid", sysuserid);
                        childds.setString(row, "__trackitem_custodialdepartmentid", defaultdepartment);
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String columnid2 = ds.getColumnId(col);
                            if (columnid2.startsWith("s_sample.")) {
                                childds.setString(row, columnid2.substring(9), ds.getValue(i, columnid2, ""));
                                continue;
                            }
                            if (!columnid2.startsWith("trackitem.")) continue;
                            childds.setString(row, "__trackitem_" + columnid2.substring(10), ds.getValue(i, columnid2, ""));
                        }
                        continue;
                    }
                    if (OpalUtil.isNotEmpty(eventdefid) && OpalUtil.isNotEmpty(sampletypeid) && OpalUtil.isNotEmpty(specimendefid) && (processds = this.getQueryProcessor().getPreparedSqlDataSet("select processtype, processinstructions from s_eventdefstspecimendef where s_eventdefid = ? and s_sampletypeid = ? and s_specimendefid = ?", (Object[])new String[]{eventdefid, sampletypeid, specimendefid})) != null && processds.size() > 0) {
                        processtype = processds.getString(0, "processtype");
                        processinstructions = processds.getString(0, "processinstructions");
                    }
                    int row = sampleds.addRow();
                    sampleds.setString(row, "__parentrowuuid", ds.getString(i, "parentrowuuid", ""));
                    sampleds.setString(row, "sstudyid", studyid);
                    sampleds.setString(row, "sampletypeid", sampletypeid);
                    sampleds.setString(row, "processtype", processtype);
                    sampleds.setString(row, "processinstruction", processinstructions);
                    sampleds.setString(row, "__samplefamily_studysiteid", studysiteid);
                    sampleds.setString(row, "__samplefamily_clinicalprotocolid", studyid);
                    sampleds.setString(row, "__samplefamily_clinicalprotocolversionid", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolversionid"));
                    sampleds.setString(row, "__samplefamily_clinicalprotocolrevision", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolrevision"));
                    sampleds.setString(row, "__samplefamily_initialvolume", string);
                    sampleds.setString(row, "__samplefamily_initialvolumeunits", qtyunits);
                    sampleds.setString(row, "__samplefamily_sampletypeid", sampletypeid);
                    sampleds.setString(row, "__samplefamily_specimendefid", specimendefid);
                    sampleds.setString(row, "samplestatus", "Received");
                    sampleds.setString(row, "storagestatus", "Received");
                    if (OpalUtil.isNotEmpty(subjectid)) {
                        try {
                            participantid = this.getParticipantID(studyid, studysiteid, subjectid, cohortid, externalparticipantid);
                            sampleds.setString(row, "__samplefamily_subjectid", subjectid);
                            sampleds.setString(row, "__samplefamily_externalsubject", externalparticipantid);
                            sampleds.setString(row, "__samplefamily_participantid", participantid);
                            if (OpalUtil.isNotEmpty(eventdefid)) {
                                String participanteventid = this.getParticipantEventID(participantid, eventdefid, studysiteid, attributesArray, neweventinstance != 0);
                                sampleds.setString(row, "__samplefamily_participanteventid", participanteventid);
                                sampleds.setString(row, "__samplefamily_clinicalevent", this.getProtocolEventDefLabel(eventdefid));
                                sampleds.setString(row, "__samplefamily_eventdefid", eventdefid);
                            } else {
                                sampleds.setString(row, "__samplefamily_participanteventid", "");
                                sampleds.setString(row, "__samplefamily_clinicalevent", this.getNonProtocolEventLabel(clinicaleventid));
                                sampleds.setString(row, "__samplefamily_eventdefid", "");
                            }
                            sampleds.setString(row, "__samplefamily_notes", samplefamilynotes);
                        }
                        catch (ActionException e) {
                            message = "||ERROR||<span style='color:red;font-weight:bold;'>" + this.getTranslationProcessor().translate("Error adding new Participant record") + "</span><hr>";
                            message = message + "<div style='padding:10px 2px;'>";
                            ErrorHandler errorHandler3 = e.getErrorHandler();
                            if (errorHandler3 != null && errorHandler3.hasErrors()) {
                                if ((message = message + errorHandler3.getLastErrorMessage()).endsWith("|")) {
                                    message = message + message.substring(0, message.length() - 1);
                                }
                            } else {
                                message = message + e.getMessage();
                            }
                            message = message + "</div>";
                        }
                    } else {
                        sampleds.setString(row, "__samplefamily_subjectid", "");
                        sampleds.setString(row, "__samplefamily_externalsubject", "");
                        sampleds.setString(row, "__samplefamily_participantid", "");
                        sampleds.setString(row, "__samplefamily_participanteventid", "");
                        sampleds.setString(row, "__samplefamily_clinicalevent", this.getNonProtocolEventLabel(clinicaleventid));
                        sampleds.setString(row, "__samplefamily_eventdefid", eventdefid);
                        sampleds.setString(row, "__samplefamily_notes", samplefamilynotes);
                    }
                    if (!OpalUtil.isEmpty(message)) continue;
                    sampleds.setString(row, "__trackitem_containertypeid", specimentype);
                    sampleds.setString(row, "__trackitem_qtycurrent", string);
                    sampleds.setString(row, "__trackitem_qtyunits", qtyunits);
                    sampleds.setString(row, "__trackitem_custodialuserid", sysuserid);
                    sampleds.setString(row, "__trackitem_custodialdepartmentid", defaultdepartment);
                    for (int col = 0; col < ds.getColumnCount(); ++col) {
                        String columnid3 = ds.getColumnId(col);
                        if (columnid3.startsWith("s_sample.")) {
                            sampleds.setString(row, columnid3.substring(9), ds.getValue(i, columnid3, ""));
                            continue;
                        }
                        if (columnid3.startsWith("s_samplefamily.")) {
                            sampleds.setString(row, "__samplefamily_" + columnid3.substring(15), ds.getValue(i, columnid3, ""));
                            if (!"collectiondt".equals(columnid3.substring(15))) continue;
                            sampleds.setString(row, "collectiondt", ds.getValue(i, columnid3, ""));
                            continue;
                        }
                        if (!columnid3.startsWith("trackitem.")) continue;
                        sampleds.setString(row, "__trackitem_" + columnid3.substring(10), ds.getValue(i, columnid3, ""));
                    }
                }
                if (OpalUtil.isEmpty(message) && sampleds.size() > 0) {
                    sampleds.setString(-1, "receivedby", sysuserid);
                    sampleds.setString(-1, "receiveddt", "n");
                    sampleds.setString(-1, "confirmedby", sysuserid);
                    sampleds.setString(-1, "confirmeddt", "n");
                    if (attributesArray != null) {
                        try {
                            for (i = 0; i < attributesArray.length(); ++i) {
                                String value;
                                JSONObject object = attributesArray.getJSONObject(i);
                                String columnid4 = object.has("columnid") ? object.getString("columnid") : "";
                                String string = value = object.has("value") ? object.getString("value") : "";
                                if (!OpalUtil.isNotEmpty(columnid4) || !OpalUtil.isNotEmpty(value)) continue;
                                String column = "";
                                if (columnid4.startsWith("s_sample.")) {
                                    column = columnid4.substring(9);
                                } else if (columnid4.startsWith("s_samplefamily.")) {
                                    column = "__samplefamily_" + columnid4.substring(15);
                                } else if (columnid4.startsWith("trackitem.")) {
                                    column = "__trackitem_" + columnid4.substring(10);
                                }
                                if (!OpalUtil.isNotEmpty(column) || sampleds.isValidColumn(column)) continue;
                                sampleds.setString(-1, column, value);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("copies", String.valueOf(sampleds.size()));
                        for (int col = 0; col < sampleds.getColumnCount(); ++col) {
                            columnid = sampleds.getColumnId(col);
                            props.setProperty(columnid, sampleds.getColumnValues(columnid, ";"));
                        }
                        props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    }
                    catch (ActionException e) {
                        ErrorHandler errorHandler4 = e.getErrorHandler();
                        if (errorHandler4 != null && errorHandler4.hasErrors()) {
                            ErrorDetail errorDetail = (ErrorDetail)errorHandler4.get(0);
                            message = errorDetail.getErrorType().equals("CONFIRM") ? "||CONFIRM||" + errorDetail.getMessage() : "||ERROR||" + errorDetail.getMessage();
                        }
                        message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
                    }
                    if (OpalUtil.isEmpty(message)) {
                        sampleid = props.getProperty("newkeyid1");
                        sampleds.addColumnValues("s_sampleid", 0, sampleid, ";");
                        this.setActionStatus(configurationProcessor, accessionid, "||INFO||" + this.getTranslationProcessor().translate("Adding aliases..."));
                        DataSet aliasds = new DataSet();
                        aliasds.addColumnValues("s_sampleid", 0, sampleid, ";");
                        aliasds.addColumnValues("aliasid", 0, props.getProperty("aliasid"), ";");
                        aliasds.addColumnValues("aliastype", 0, props.getProperty("aliastype"), ";");
                        if (aliasds.size() > 0) {
                            DataSet addaliasds = new DataSet();
                            for (int i3 = 0; i3 < aliasds.size(); ++i3) {
                                String _sampleid = aliasds.getString(i3, "s_sampleid");
                                String _aliasid = aliasds.getString(i3, "aliasid");
                                String _aliastype = aliasds.getString(i3, "aliastype");
                                if (!OpalUtil.isNotEmpty(_sampleid) || !OpalUtil.isNotEmpty(_aliasid) || !OpalUtil.isNotEmpty(_aliastype)) continue;
                                int row = addaliasds.addRow();
                                addaliasds.setString(row, "keyid1", _sampleid);
                                addaliasds.setString(row, "aliasid", _aliasid);
                                addaliasds.setString(row, "aliastype", _aliastype);
                            }
                            if (addaliasds.size() > 0) {
                                props.clear();
                                props.setProperty("sdcid", "Sample");
                                props.setProperty("keyid1", addaliasds.getColumnValues("keyid1", ";"));
                                props.setProperty("aliasid", addaliasds.getColumnValues("aliasid", ";"));
                                props.setProperty("aliastype", addaliasds.getColumnValues("aliastype", ";"));
                                props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                                this.getActionProcessor().processActionClass(AddSDIAlias.class.getName(), props);
                            }
                        }
                        StringBuilder sql = new StringBuilder();
                        SafeSQL safeSQL = new SafeSQL();
                        sql.append("select s.s_sampleid, s.sstudyid, wi.workitemid, wi.workitemversionid, wi.applyonaddflag, wi.embedchildsampleplanid, wi.embedchildsampleplanversionid, wi.assigneddepartmentid");
                        sql.append(" ,(select csp.useforaccessionflag from s_childsampleplan csp, workitem w where csp.s_childsampleplanid = w.embedchildsampleplanid");
                        sql.append(" and csp.s_childsampleplanversionid = w.embedchildsampleplanversionid and w.workitemid = wi.workitemid and ((w.workitemversionid = wi.workitemversionid and wi.workitemversionid is not null) or (w.versionstatus='C' and wi.workitemversionid is null))) useforaccessionflag");
                        sql.append(" from s_eventdefstspecimendefwi wi, s_samplefamily sf, s_sample s");
                        sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
                        sql.append(" and wi.s_eventdefid = sf.eventdefid");
                        sql.append(" and wi.s_sampletypeid = sf.sampletypeid");
                        sql.append(" and wi.s_specimendefid = sf.specimendefid");
                        sql.append(" and wi.workitemid is not null");
                        sql.append(" and sf.subjectid is not null");
                        Object wids = null;
                        if (sampleds.size() > 1000) {
                            try {
                                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                                sql.append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                sql.append(" order by wi.usersequence");
                                wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                this.getDAMProcessor().clearRSet(rsetid);
                            }
                            catch (SapphireException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
                            sql.append(" order by wi.usersequence");
                            wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        }
                        if (wids != null && ((ArrayList)wids).size() > 0) {
                            DataSet dataset = new DataSet();
                            for (int i4 = 0; i4 < ((ArrayList)wids).size(); ++i4) {
                                if ("Y".equals(((DataSet)wids).getString(i4, "useforaccessionflag", "N"))) continue;
                                dataset.copyRow((DataSet)wids, i4, 1);
                            }
                            if (dataset.size() > 0) {
                                dataset.sort("applyonaddflag");
                                ArrayList<DataSet> list = dataset.getGroupedDataSets("applyonaddflag");
                                int index = 0;
                                for (DataSet _ds : list) {
                                    if (_ds.size() <= 0) continue;
                                    this.setActionStatus(configurationProcessor, accessionid, "||INFO||" + this.getTranslationProcessor().translate("Adding services") + " (" + ++index + "/" + list.size() + ")...");
                                    props.clear();
                                    props.setProperty("sdcid", "Sample");
                                    props.setProperty("keyid1", _ds.getColumnValues("s_sampleid", ";"));
                                    props.setProperty("workitemid", _ds.getColumnValues("workitemid", ";"));
                                    props.setProperty("workitemversionid", _ds.getColumnValues("workitemversionid", ";"));
                                    props.setProperty("sourcesstudyid", _ds.getColumnValues("sstudyid", ";"));
                                    props.setProperty("embedchildsampleplanid", _ds.getColumnValues("embedchildsampleplanid", ";"));
                                    props.setProperty("embedchildsampleplanversionid", _ds.getColumnValues("embedchildsampleplanversionid", ";"));
                                    props.setProperty("applyworkitem", _ds.getString(0, "applyonaddflag", "N"));
                                    props.setProperty("s_assigneddepartment", _ds.getColumnValues("assigneddepartmentid", ";"));
                                    props.setProperty("propsmatch", "Y");
                                    props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                                    try {
                                        this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                                    }
                                    catch (ActionException e) {
                                        errorHandler = e.getErrorHandler();
                                        if (errorHandler != null && ((com.labvantage.sapphire.gwt.shared.error.ErrorHandler)errorHandler).hasErrors()) {
                                            ErrorDetail errorDetail = (ErrorDetail)((ArrayList)errorHandler).get(0);
                                            if (errorDetail.getErrorType().equals("CONFIRM")) {
                                                message = "||CONFIRM||" + errorDetail.getMessage();
                                                continue;
                                            }
                                            message = "||ERROR||" + errorDetail.getMessage();
                                            continue;
                                        }
                                        message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
                                    }
                                }
                            }
                        }
                    }
                }
                if (OpalUtil.isEmpty(message) && childds.size() > 0) {
                    this.setActionStatus(configurationProcessor, accessionid, "||INFO||" + this.getTranslationProcessor().translate("Adding child samples..."));
                    childds.setString(-1, "receivedby", sysuserid);
                    childds.setString(-1, "receiveddt", "n");
                    childds.setString(-1, "confirmedby", sysuserid);
                    childds.setString(-1, "confirmeddt", "n");
                    childds.sort("__parentrowuuid");
                    ArrayList<DataSet> list = childds.getGroupedDataSets("__parentrowuuid");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    DataSet insertds = new DataSet();
                    for (DataSet _ds : list) {
                        String parentrowuuid = _ds.getString(0, "__parentrowuuid");
                        String parentsampleid = _ds.getString(0, "__parentsampleid", "");
                        String parentsamplefamilyid = "";
                        if (OpalUtil.isEmpty(parentsampleid)) {
                            filter.clear();
                            filter.put("__parentrowuuid", parentrowuuid);
                            int row = sampleds.findRow(filter);
                            if (row == -1) {
                                props.clear();
                                props.setProperty("sdcid", "Sample");
                                props.setProperty("copies", "1");
                                props.setProperty("sstudyid", _ds.getString(0, "sstudyid"));
                                props.setProperty("samplestatus", "Virtual");
                                props.setProperty("storagestatus", "Virtual");
                                for (int col = 0; col < _ds.getColumnCount(); ++col) {
                                    String columnid5 = _ds.getColumnId(col);
                                    if (!columnid5.startsWith("__parentfamily_")) continue;
                                    props.setProperty(StringUtil.replaceAll(columnid5, "__parentfamily_", "__samplefamily_"), _ds.getValue(0, columnid5, ""));
                                }
                                props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                                try {
                                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                                }
                                catch (ActionException e) {
                                    errorHandler = e.getErrorHandler();
                                    if (errorHandler != null && ((com.labvantage.sapphire.gwt.shared.error.ErrorHandler)errorHandler).hasErrors()) {
                                        ErrorDetail errorDetail = (ErrorDetail)((ArrayList)errorHandler).get(0);
                                        message = errorDetail.getErrorType().equals("CONFIRM") ? "||CONFIRM||" + errorDetail.getMessage() : "||ERROR||" + errorDetail.getMessage();
                                    }
                                    message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
                                }
                                parentsampleid = props.getProperty("newkeyid1");
                            } else {
                                parentsampleid = sampleds.getString(row, "s_sampleid");
                            }
                        }
                        if (!OpalUtil.isEmpty(message) || !OpalUtil.isNotEmpty(parentsampleid)) continue;
                        String childsample_plantype = _ds.getString(0, "__childsampleplantype", "");
                        if ("Aliquot".equals(childsample_plantype) || "Derivative".equals(childsample_plantype)) {
                            Map<String, String> trackitemCopyDownMap;
                            Map<String, String> sampleCopyDownMap = this.getCopyDownDataMap(childsample_plantype.toLowerCase(), "Sample", parentsampleid, null);
                            if (sampleCopyDownMap != null) {
                                for (String columnid6 : sampleCopyDownMap.keySet()) {
                                    if ("sampletypeid".equalsIgnoreCase(columnid6)) continue;
                                    _ds.setString(-1, columnid6, sampleCopyDownMap.get(columnid6));
                                }
                            }
                            if ((trackitemCopyDownMap = this.getCopyDownDataMap(childsample_plantype.toLowerCase(), "TrackItemSDC", parentsampleid, null)) != null) {
                                for (String string : trackitemCopyDownMap.keySet()) {
                                    _ds.setString(-1, "__trackitem_" + string, trackitemCopyDownMap.get(string));
                                }
                            }
                        }
                        if (markConsumedRowSet.contains(parentrowuuid)) {
                            markConsumedSampleSet.add(parentsampleid);
                        }
                        parentsamplefamilyid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", "samplefamilyid", "s_sampleid = ?", new String[]{parentsampleid});
                        _ds.setString(-1, "samplefamilyid", parentsamplefamilyid);
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("copies", String.valueOf(_ds.size()));
                        for (int col = 0; col < _ds.getColumnCount(); ++col) {
                            String columnid2 = _ds.getColumnId(col);
                            if (columnid2.startsWith("__parentfamily_")) continue;
                            props.setProperty(columnid2, _ds.getColumnValues(columnid2, ";"));
                        }
                        props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                        try {
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                            String childsampleids = props.getProperty("newkeyid1");
                            sampleid = sampleid + (OpalUtil.isNotEmpty(sampleid) ? ";" : "") + childsampleids;
                            String[] childsamplearray = StringUtil.split(childsampleids, ";");
                            int childindex = 0;
                            for (String childsampleid : childsamplearray) {
                                int insertRow = insertds.addRow();
                                insertds.setString(insertRow, "sourcesampleid", parentsampleid);
                                insertds.setString(insertRow, "destsampleid", childsampleid);
                                insertds.setString(insertRow, "childsampleplanid", _ds.getString(childindex, "__childsampleplanid", ""));
                                insertds.setString(insertRow, "childsampleplanversionid", _ds.getString(childindex, "__childsampleplanversionid", ""));
                                insertds.setString(insertRow, "childsampleplanitemid", _ds.getString(childindex, "__childsampleplanitemid", ""));
                                ++childindex;
                            }
                            DataSet dataSet = new DataSet();
                            dataSet.addColumnValues("s_sampleid", 0, childsampleids, ";");
                            dataSet.addColumnValues("aliasid", 0, props.getProperty("aliasid"), ";");
                            dataSet.addColumnValues("aliastype", 0, props.getProperty("aliastype"), ";");
                            if (dataSet.size() <= 0) continue;
                            DataSet addaliasds = new DataSet();
                            for (int i5 = 0; i5 < dataSet.size(); ++i5) {
                                String _sampleid = dataSet.getString(i5, "s_sampleid");
                                String _aliasid = dataSet.getString(i5, "aliasid");
                                String _aliastype = dataSet.getString(i5, "aliastype");
                                if (!OpalUtil.isNotEmpty(_sampleid) || !OpalUtil.isNotEmpty(_aliasid) || !OpalUtil.isNotEmpty(_aliastype)) continue;
                                int aliasrow = addaliasds.addRow();
                                addaliasds.setString(aliasrow, "keyid1", _sampleid);
                                addaliasds.setString(aliasrow, "aliasid", _aliasid);
                                addaliasds.setString(aliasrow, "aliastype", _aliastype);
                            }
                            if (addaliasds.size() <= 0) continue;
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", addaliasds.getColumnValues("keyid1", ";"));
                            props.setProperty("aliasid", addaliasds.getColumnValues("aliasid", ";"));
                            props.setProperty("aliastype", addaliasds.getColumnValues("aliastype", ";"));
                            this.getActionProcessor().processActionClass(AddSDIAlias.class.getName(), props);
                        }
                        catch (ActionException e) {
                            errorHandler = e.getErrorHandler();
                            if (errorHandler != null && ((com.labvantage.sapphire.gwt.shared.error.ErrorHandler)errorHandler).hasErrors()) {
                                ErrorDetail errorDetail = (ErrorDetail)((ArrayList)errorHandler).get(0);
                                if (errorDetail.getErrorType().equals("CONFIRM")) {
                                    message = "||CONFIRM||" + errorDetail.getMessage();
                                    continue;
                                }
                                message = "||ERROR||" + errorDetail.getMessage();
                                continue;
                            }
                            message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
                        }
                    }
                    if (OpalUtil.isEmpty(message) && insertds.size() > 0) {
                        StringBuilder sql = new StringBuilder();
                        HashSet<String> childSampleSet = new HashSet<String>();
                        for (int i6 = 0; i6 < insertds.size(); ++i6) {
                            String sourcesampleid = insertds.getString(i6, "sourcesampleid");
                            String destsampleid = insertds.getString(i6, "destsampleid");
                            if (!OpalUtil.isNotEmpty(sourcesampleid) || !OpalUtil.isNotEmpty(destsampleid)) continue;
                            childSampleSet.add(destsampleid);
                            sql.setLength(0);
                            String childsampleplanid = insertds.getString(i6, "childsampleplanid", "");
                            String childsampleplanversionid = insertds.getString(i6, "childsampleplanversionid", "");
                            String childsampleplanitemid = insertds.getString(i6, "childsampleplanitemid", "");
                            SafeSQL safeSQL = new SafeSQL();
                            if (OpalUtil.isNotEmpty(childsampleplanid) && OpalUtil.isNotEmpty(childsampleplanversionid) && OpalUtil.isNotEmpty(childsampleplanitemid)) {
                                sql.append("insert into s_samplemap ( sourcesampleid, destsampleid, childsampleplanid, childsampleplanversionid, childsampleplanitemid )");
                                sql.append(" values ( ");
                                sql.append(safeSQL.addVar(sourcesampleid)).append(",");
                                sql.append(safeSQL.addVar(destsampleid)).append(",");
                                sql.append(safeSQL.addVar(childsampleplanid)).append(",");
                                sql.append(safeSQL.addVar(childsampleplanversionid)).append(",");
                                sql.append(safeSQL.addVar(childsampleplanitemid)).append("");
                                sql.append(")");
                            } else {
                                sql.append("insert into s_samplemap ( sourcesampleid, destsampleid )");
                                sql.append(" values ( ").append(safeSQL.addVar(sourcesampleid)).append(", ").append(safeSQL.addVar(destsampleid)).append(" )");
                            }
                            this.getQueryProcessor().execPreparedUpdate(sql.toString(), safeSQL.getValues());
                        }
                        this.setActionStatus(configurationProcessor, accessionid, "||INFO||" + this.getTranslationProcessor().translate("Adding services..."));
                        SafeSQL safeSQL = new SafeSQL();
                        String childtestsql = "select (select s.sstudyid from s_sample s where s.s_sampleid = sm.destsampleid) sstudyid, sm.destsampleid, wi.workitemid, wi.workitemversionid, wi.embedchildsampleplanid, wi.embedchildsampleplanversionid, wi.assigneddepartmentid, wi.applyonaddflag from s_childsampleplanworkitem wi, s_samplemap sm where wi.s_childsampleplanid = sm.childsampleplanid and wi.s_childsampleplanversionid = sm.childsampleplanversionid and wi.s_childsampleplanitemid = sm.childsampleplanitemid and wi.workitemid is not null and sm.destsampleid in (" + safeSQL.addIn(childSampleSet) + ")";
                        DataSet childTestDS = this.getQueryProcessor().getPreparedSqlDataSet(childtestsql, safeSQL.getValues());
                        if (childTestDS != null && childTestDS.size() > 0) {
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", childTestDS.getColumnValues("destsampleid", ";"));
                            props.setProperty("workitemid", childTestDS.getColumnValues("workitemid", ";"));
                            props.setProperty("workitemversionid", childTestDS.getColumnValues("workitemversionid", ";"));
                            props.setProperty("sourcesstudyid", childTestDS.getColumnValues("sstudyid", ";"));
                            props.setProperty("embedchildsampleplanid", childTestDS.getColumnValues("embedchildsampleplanid", ";"));
                            props.setProperty("embedchildsampleplanversionid", childTestDS.getColumnValues("embedchildsampleplanversionid", ";"));
                            props.setProperty("applyworkitem", childTestDS.getColumnValues("applyonaddflag", ";"));
                            props.setProperty("s_assigneddepartment", childTestDS.getColumnValues("assigneddepartmentid", ";"));
                            props.setProperty("propsmatch", "Y");
                            props.setProperty("__sdcruleconfirm", sdcruleconfirm);
                            try {
                                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                            }
                            catch (ActionException e) {
                                ErrorHandler errorHandler5 = e.getErrorHandler();
                                if (errorHandler5 != null && errorHandler5.hasErrors()) {
                                    ErrorDetail errorDetail = (ErrorDetail)errorHandler5.get(0);
                                    message = errorDetail.getErrorType().equals("CONFIRM") ? "||CONFIRM||" + errorDetail.getMessage() : "||ERROR||" + errorDetail.getMessage();
                                }
                                message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while adding Services to Child Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
                            }
                        }
                    }
                }
                if (markConsumedSampleSet.size() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    DataSet markConsumedDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.s_sampleid, trackitem.trackitemid from s_sample, trackitem where s_sample.storagestatus != 'Disposed' and s_sample.s_sampleid in (" + safeSQL.addIn(markConsumedSampleSet) + ") and trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid", safeSQL.getValues());
                    if (markConsumedDS != null && markConsumedDS.size() > 0) {
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", markConsumedDS.getColumnValues("s_sampleid", ";"));
                        props.setProperty("samplestatus", "Disposed");
                        props.setProperty("storagestatus", "Disposed");
                        props.setProperty("disposalstatus", "Consumed");
                        props.setProperty("storagedisposalstatus", "Consumed");
                        props.setProperty("disposaldt", "n");
                        props.setProperty("disposedby", this.connectionInfo.getSysuserId());
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        props.clear();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("keyid1", markConsumedDS.getColumnValues("trackitemid", ";"));
                        props.setProperty("qtycurrent", "0");
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                }
            }
            catch (JSONException e) {
                message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
            }
            catch (SapphireException e) {
                e.printStackTrace();
                Trace.logError(e.getMessage());
                message = "||ERROR||" + this.getTranslationProcessor().translate("Some unknown error happened while allocating Samples. If problem persists, please contact your Administrator.") + ": " + e.getMessage();
            }
        }
        if (OpalUtil.isEmpty(message)) {
            this.setActionStatus(configurationProcessor, accessionid, "||SAMPLE||" + sampleid);
        } else {
            this.setActionStatus(configurationProcessor, accessionid, message);
        }
    }

    private void setActionStatus(ConfigurationProcessor configurationProcessor, String accessionid, String status) {
        try {
            PropertyList props = new PropertyList();
            props.setProperty("actionprogressid", accessionid);
            props.setProperty("message", status);
            this.getActionProcessor().processActionClass(SetActionProgressStatus.class.getName(), props, true);
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
    }

    private String getParticipantEventID(String participantid, String eventdefid, String studysiteid, JSONArray attributesArray, boolean neweventinstance) throws ActionException {
        String key = "participanteventid" + participantid + studysiteid + eventdefid;
        if (!this.localCache.containsKey(key)) {
            String participanteventid = "";
            ArrayList ds = null;
            if (!neweventinstance) {
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participanteventid, parentparticipanteventid from s_participantevent where PARTICIPANTID = ? and EVENTDEFID = ? and SSTUDYSITEID = ?", (Object[])new String[]{participantid, eventdefid, studysiteid});
            }
            if (ds == null || ds.size() == 0) {
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select ed.s_eventdefid, ed.eventdefdesc, ed.eventdeflabel, ed.eventdeftype, ed.visittype, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.s_eventdefid) parenteventdeflabel from s_eventdef ed where ed.s_eventdefid = ?", (Object[])new String[]{eventdefid});
                if (ds != null && ds.size() > 0) {
                    PropertyList props = new PropertyList();
                    String parenteventdefid = ((DataSet)ds).getString(0, "parenteventdefid", "");
                    String parentparticipanteventid = "";
                    if (OpalUtil.isNotEmpty(parenteventdefid)) {
                        DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participanteventid from s_participantevent where PARTICIPANTID = ? and EVENTDEFID = ? and SSTUDYSITEID = ?", (Object[])new String[]{participantid, parenteventdefid, studysiteid});
                        if (_ds != null && _ds.size() > 0) {
                            parentparticipanteventid = _ds.getString(0, "s_participanteventid");
                        } else {
                            props.clear();
                            props.setProperty("sdcid", "LV_ParticipantEvent");
                            props.setProperty("participantid", participantid);
                            props.setProperty("sstudysiteid", studysiteid);
                            props.setProperty("eventdefid", parenteventdefid);
                            props.setProperty("eventlabel", ((DataSet)ds).getString(0, "parenteventdeflabel"));
                            props.setProperty("activeflag", "Y");
                            props.setProperty("eventstatus", "Completed");
                            props.setProperty("eventdt", "n");
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                            parentparticipanteventid = props.getProperty("newkeyid1");
                        }
                    }
                    props.clear();
                    props.setProperty("sdcid", "LV_ParticipantEvent");
                    props.setProperty("participantid", participantid);
                    props.setProperty("sstudysiteid", studysiteid);
                    props.setProperty("eventdefid", eventdefid);
                    props.setProperty("eventlabel", ((DataSet)ds).getString(0, "eventdeflabel"));
                    props.setProperty("parentparticipanteventid", parentparticipanteventid);
                    props.setProperty("activeflag", "Y");
                    props.setProperty("eventstatus", "Completed");
                    props.setProperty("eventdt", "n");
                    if (attributesArray != null) {
                        try {
                            for (int i = 0; i < attributesArray.length(); ++i) {
                                String value;
                                String columnid;
                                JSONObject object = attributesArray.getJSONObject(i);
                                String string = columnid = object.has("columnid") ? object.getString("columnid") : "";
                                if (!columnid.startsWith("s_participantevent.")) continue;
                                String string2 = value = object.has("value") ? object.getString("value") : "";
                                if (!OpalUtil.isNotEmpty(value)) continue;
                                props.setProperty(columnid.substring(19), value);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    participanteventid = props.getProperty("newkeyid1");
                }
            } else {
                participanteventid = ((DataSet)ds).getString(0, "s_participanteventid");
            }
            this.localCache.put(key, participanteventid);
        }
        return this.localCache.get(key);
    }

    private String getParticipantID(String studyid, String studysiteid, String subjectid, String cohortid, String externalparticipantid) throws ActionException {
        String key = PARTICIPANT_CREATION_POLICY_STUDY_SUBJECT.equals(this.participantcreationrule) ? "participantid_" + studyid + "_" + subjectid : "participantid_" + studyid + "_" + subjectid + "_" + studysiteid;
        if (!this.localCache.containsKey(key)) {
            if (OpalUtil.isNotEmpty(studyid) && OpalUtil.isNotEmpty(subjectid)) {
                DataSet ds;
                String participantid = "";
                if (PARTICIPANT_CREATION_POLICY_STUDY_SUBJECT.equals(this.participantcreationrule)) {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid from s_participant where sstudyid = ? and subjectid = ?", (Object[])new String[]{studyid, subjectid});
                    if (ds != null && ds.size() > 0) {
                        participantid = ds.getString(0, "s_participantid", "");
                    }
                } else {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid from s_participant where sstudyid = ? and studysiteid = ? and subjectid = ?", (Object[])new String[]{studyid, studysiteid, subjectid});
                    if (ds != null && ds.size() > 0) {
                        participantid = ds.getString(0, "s_participantid", "");
                    }
                }
                if (OpalUtil.isNotEmpty(participantid)) {
                    this.localCache.put(key, participantid);
                } else {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_Participant");
                    props.setProperty("copies", "1");
                    props.setProperty("sstudyid", studyid);
                    props.setProperty("studysiteid", studysiteid);
                    props.setProperty("subjectid", subjectid);
                    props.setProperty("externalparticipantid", externalparticipantid);
                    props.setProperty("enrolldt", "n");
                    props.setProperty("participantstatus", "Enrolled");
                    if (this.isStudyProtocolDriven(studyid)) {
                        props.setProperty("cpcohortid", cohortid);
                        props.setProperty("clinicalprotocolid", studyid);
                        props.setProperty("clinicalprotocolrevision", OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "clinicalprotocolrevision", "s_studysiteid = ?", new String[]{studysiteid}));
                    }
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    this.localCache.put(key, props.getProperty("newkeyid1"));
                }
            } else {
                this.localCache.put(key, "");
            }
        }
        return this.localCache.get(key);
    }

    private boolean isStudyProtocolDriven(String studyid) {
        String key = "isprotocol_" + studyid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, Study.isProtocolDriven(this.getQueryProcessor(), studyid) ? "Y" : "N");
        }
        return "Y".equals(this.localCache.get(key));
    }

    private String getEventDefProperty(String eventdefid, String property) {
        DataSet ds;
        String key = property + eventdefid;
        if (!this.localCache.containsKey(key) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select eventdeflabel, eventdeftype from s_eventdef where s_eventdefid = ?", (Object[])new String[]{eventdefid})) != null && ds.size() > 0) {
            this.localCache.put("eventdeflabel" + eventdefid, ds.getString(0, "eventdeflabel", ""));
            this.localCache.put("eventdeftype" + eventdefid, ds.getString(0, "eventdeftype", ""));
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : "";
    }

    private String getClinicalProtocolProperty(String studyid, String studysiteid, String property) {
        DataSet ds;
        String key = property + studyid + studysiteid;
        if (!this.localCache.containsKey(key) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision ,(select cp.s_clinicalprotocolversionid from s_clinicalprotocol cp where cp.sstudyid = s_studysite.sstudyid and cp.s_clinicalprotocolrevision = s_studysite.clinicalprotocolrevision and cp.versionstatus = 'C') currentprotocolversion from s_studysite where SSTUDYID = ? and S_STUDYSITEID = ?", (Object[])new String[]{studyid, studysiteid})) != null && ds.size() > 0) {
            this.localCache.put("clinicalprotocolid" + studyid + studysiteid, ds.getString(0, "clinicalprotocolid", ""));
            this.localCache.put("clinicalprotocolversionid" + studyid + studysiteid, ds.getValue(0, "currentprotocolversion", "1"));
            this.localCache.put("clinicalprotocolrevision" + studyid + studysiteid, ds.getString(0, "clinicalprotocolrevision", ""));
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : "";
    }

    private String getNonProtocolEventLabel(String clinicaleventid) {
        String key = "eventlabel" + clinicaleventid;
        if (!this.localCache.containsKey(key)) {
            String clinicaleventdesc = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_clinicalevent", "clinicaleventdesc", "s_clinicaleventid = ?", new String[]{clinicaleventid});
            if (OpalUtil.isEmpty(clinicaleventdesc)) {
                clinicaleventdesc = clinicaleventid;
            }
            this.localCache.put(key, clinicaleventdesc);
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : clinicaleventid;
    }

    private String getProtocolEventDefLabel(String eventdefid) {
        String key = "eventdeflabel" + eventdefid;
        if (!this.localCache.containsKey(key)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select (select p.eventdeflabel from s_eventdef p where p.s_eventdefid = s_eventdef.parenteventdefid) parentlabel, s_eventdef.eventdeflabel from s_eventdef where s_eventdef.s_eventdefid = ?", (Object[])new String[]{eventdefid});
            if (ds != null && ds.size() > 0) {
                String parentlabel = ds.getString(0, "parentlabel", "");
                String eventdeflabel = ds.getString(0, "eventdeflabel", "");
                this.localCache.put(key, (OpalUtil.isNotEmpty(parentlabel) ? parentlabel + " / " : "") + eventdeflabel);
            } else {
                this.localCache.put(key, "");
            }
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : eventdefid;
    }

    private boolean isMarkParentConsumed(String childsampleplanid, String childsampleplanversionid) {
        String key = "markparentconsumed-" + childsampleplanid + ":" + childsampleplanversionid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_childsampleplan", "markparentconsumedflag", "s_childsampleplanid=? and s_childsampleplanversionid=?", new String[]{childsampleplanid, childsampleplanversionid}));
        }
        return "Y".equals(this.localCache.get(key));
    }

    private Map<String, String> getCopyDownDataMap(String childtype, String sdcid, String parentsampleid, String copyDownColumns) {
        String key = childtype + sdcid + parentsampleid;
        if (!this.copyDownDataCache.containsKey(key)) {
            this.copyDownDataCache.put(key, ChildSampleUtil.getCopyDownValues(this.getConfigurationProcessor(), this.getQueryProcessor(), sdcid, parentsampleid, childtype, copyDownColumns));
        }
        return this.copyDownDataCache.get(key);
    }
}

