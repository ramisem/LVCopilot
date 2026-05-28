/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.admin.ddt.Study;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReceiveBulkSamples
extends BaseAjaxRequest {
    private Map<String, String> localCache = new HashMap<String, String>();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String sampleid;
        String message;
        AjaxResponse ajaxResponse;
        block26: {
            ajaxResponse = new AjaxResponse(request, response);
            message = "";
            sampleid = "";
            String data = ajaxResponse.getRequestParameter("data");
            String defaultdepartment = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
            boolean protocolDriven = false;
            DataSet ds = new DataSet();
            try {
                JSONArray jsonArray = new JSONArray(data);
                for (int row = 0; row < jsonArray.length(); ++row) {
                    int dsrow = ds.addRow();
                    String studyid = "";
                    String studysiteid = "";
                    String cohort = "";
                    String subjectid = "";
                    String eventid = "";
                    String sampletypeid = "";
                    String containertypeid = "";
                    JSONArray rowArray = jsonArray.getJSONArray(row);
                    for (int col = 0; col < rowArray.length(); ++col) {
                        String value;
                        JSONObject column = rowArray.getJSONObject(col);
                        String sdcid = column.has("sdcid") ? column.getString("sdcid") : "";
                        String columnid = column.has("columnid") ? column.getString("columnid") : "";
                        String string = value = column.has("value") ? column.getString("value") : "";
                        if ("task".equals(sdcid)) {
                            if ("studyid".equals(columnid)) {
                                studyid = value;
                                ds.setString(dsrow, "sstudyid", studyid);
                                continue;
                            }
                            if ("studysiteid".equals(columnid)) {
                                studysiteid = value;
                                ds.setString(dsrow, "__samplefamily_studysiteid", value);
                                continue;
                            }
                            if ("subjectid".equals(columnid)) {
                                subjectid = value;
                                ds.setString(dsrow, "__samplefamily_subjectid", value);
                                continue;
                            }
                            if ("cohort".equals(columnid)) {
                                cohort = value;
                                continue;
                            }
                            if (!"eventid".equals(columnid) || !OpalUtil.isNotEmpty(value)) continue;
                            eventid = value;
                            if (this.isStudyProtocolDriven(studyid)) {
                                ds.setString(dsrow, "__samplefamily_clinicalevent", this.getProtocolEventDefLabel(eventid));
                                ds.setString(dsrow, "__samplefamily_eventdefid", eventid);
                                continue;
                            }
                            ds.setString(dsrow, "__samplefamily_clinicalevent", this.getNonProtocolEventLabel(eventid));
                            continue;
                        }
                        if ("Sample".equals(sdcid)) {
                            ds.setString(dsrow, columnid, value);
                            if (!"sampletypeid".equals(columnid)) continue;
                            ds.setString(dsrow, "__samplefamily_sampletypeid", value);
                            sampletypeid = value;
                            continue;
                        }
                        if ("TrackItemSDC".equals(sdcid)) {
                            ds.setString(dsrow, "__trackitem_" + columnid, value);
                            if (!"containertypeid".equals(columnid)) continue;
                            containertypeid = value;
                            continue;
                        }
                        if (!"LV_SampleFamily".equals(sdcid)) continue;
                        ds.setString(dsrow, "__samplefamily_" + columnid, value);
                        if ("initialvolume".equals(columnid) || "initialmass".equals(columnid)) {
                            ds.setString(dsrow, "__trackitem_qtycurrent", value);
                            continue;
                        }
                        if (!"initialvolumeunits".equals(columnid) && !"initialmassunits".equals(columnid)) continue;
                        ds.setString(dsrow, "__trackitem_qtyunits", value);
                    }
                    protocolDriven = this.isStudyProtocolDriven(studyid);
                    if (protocolDriven && OpalUtil.isNotEmpty(studysiteid)) {
                        ds.setString(row, "__samplefamily_clinicalprotocolid", studyid);
                        ds.setString(row, "__samplefamily_clinicalprotocolversionid", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolversionid"));
                        ds.setString(row, "__samplefamily_clinicalprotocolrevision", this.getClinicalProtocolProperty(studyid, studysiteid, "clinicalprotocolrevision"));
                    }
                    if (!OpalUtil.isNotEmpty(subjectid)) continue;
                    String participantid = this.getParticipantID(studyid, studysiteid, subjectid, cohort, "");
                    String participanteventid = "";
                    if (protocolDriven) {
                        participanteventid = this.getParticipantEventID(participantid, eventid, studysiteid);
                        if (OpalUtil.isNotEmpty(sampletypeid) && OpalUtil.isNotEmpty(eventid) && OpalUtil.isNotEmpty(containertypeid)) {
                            ds.setString(dsrow, "__samplefamily_specimendefid", this.getSpecimenDefID(eventid, sampletypeid, containertypeid));
                        }
                    }
                    ds.setString(dsrow, "__samplefamily_participantid", participantid);
                    ds.setString(dsrow, "__samplefamily_participanteventid", participanteventid);
                }
                if (ds.size() <= 0) break block26;
                ds.setString(-1, "samplestatus", "Received");
                ds.setString(-1, "storagestatus", "Received");
                ds.setString(-1, "__trackitem_custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                ds.setString(-1, "__trackitem_custodialdepartmentid", defaultdepartment);
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("copies", String.valueOf(ds.size()));
                    for (int col = 0; col < ds.getColumnCount(); ++col) {
                        String columnid = ds.getColumnId(col);
                        props.setProperty(columnid, ds.getColumnValues(columnid, ";"));
                    }
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    sampleid = props.getProperty("newkeyid1");
                    if (protocolDriven) {
                        SafeSQL safeSQL = new SafeSQL();
                        StringBuilder sql = new StringBuilder();
                        sql.append("select s.s_sampleid, wi.workitemid, wi.workitemversionid, wi.applyonaddflag");
                        sql.append(" from s_eventdefstspecimendefwi wi, s_samplefamily sf, s_sample s");
                        sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
                        sql.append(" and wi.s_eventdefid = sf.eventdefid");
                        sql.append(" and wi.s_sampletypeid = sf.sampletypeid");
                        sql.append(" and wi.s_specimendefid = sf.specimendefid");
                        sql.append(" and wi.workitemid is not null");
                        ArrayList wids = null;
                        if (ds.size() > 1000) {
                            try {
                                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                                sql.append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                this.getDAMProcessor().clearRSet(rsetid);
                            }
                            catch (SapphireException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
                            wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        }
                        if (wids != null && wids.size() > 0) {
                            ((DataSet)wids).sort("applyonaddflag");
                            ArrayList<DataSet> list = ((DataSet)wids).getGroupedDataSets("applyonaddflag");
                            for (DataSet _ds : list) {
                                if (_ds.size() <= 0) continue;
                                props.clear();
                                props.setProperty("sdcid", "Sample");
                                props.setProperty("keyid1", _ds.getColumnValues("s_sampleid", ";"));
                                props.setProperty("workitemid", _ds.getColumnValues("workitemid", ";"));
                                props.setProperty("workitemversionid", _ds.getColumnValues("workitemversionid", ";"));
                                props.setProperty("applyworkitem", _ds.getString(0, "applyonaddflag", "N"));
                                props.setProperty("propsmatch", "Y");
                                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                            }
                        }
                    }
                }
                catch (ActionException e) {
                    e.printStackTrace();
                }
            }
            catch (JSONException e) {
                message = "Exception receiving Samples " + e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.print();
    }

    private String getSpecimenDefID(String eventdefid, String sampletypeid, String containertypeid) {
        String key = "specimendefid" + eventdefid + sampletypeid + containertypeid;
        if (!this.localCache.containsKey(key)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_specimendefid from s_eventdefstspecimendef where s_eventdefid = ? and s_sampletypeid = ? and specimentype = ?", (Object[])new String[]{eventdefid, sampletypeid, containertypeid});
            if (ds != null && ds.size() > 0) {
                this.localCache.put(key, ds.getString(0, "s_specimendefid"));
            } else {
                this.localCache.put(key, "");
            }
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : "";
    }

    private String getNonProtocolEventLabel(String clinicaleventid) {
        String key = "eventlabel" + clinicaleventid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_clinicalevent", "clinicaleventdesc", "s_clinicaleventid = ?", new String[]{clinicaleventid}));
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

    private boolean isStudyProtocolDriven(String studyid) {
        String key = "isprotocol_" + studyid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, Study.isProtocolDriven(this.getQueryProcessor(), studyid) ? "Y" : "N");
        }
        return "Y".equals(this.localCache.get(key));
    }

    private String getParticipantEventID(String participantid, String eventdefid, String studysiteid) {
        String key = "participanteventid" + participantid + studysiteid + eventdefid;
        if (!this.localCache.containsKey(key)) {
            String participanteventid = "";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participanteventid, parentparticipanteventid from s_participantevent where PARTICIPANTID = ? and EVENTDEFID = ? and SSTUDYSITEID = ?", (Object[])new String[]{participantid, eventdefid, studysiteid});
            if (ds == null || ds.size() == 0) {
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select ed.s_eventdefid, ed.eventdefdesc, ed.eventdeflabel, ed.eventdeftype, ed.visittype, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.s_eventdefid) parenteventdeflabel from s_eventdef ed where ed.s_eventdefid = ?", (Object[])new String[]{eventdefid});
                if (ds != null && ds.size() > 0) {
                    try {
                        DataSet _ds;
                        PropertyList props = new PropertyList();
                        String parenteventdefid = ds.getString(0, "parenteventdefid", "");
                        String parentparticipanteventid = "";
                        if (OpalUtil.isNotEmpty(parenteventdefid) && ((_ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participanteventid from s_participantevent where PARTICIPANTID = ? and EVENTDEFID = ? and SSTUDYSITEID = ?", (Object[])new String[]{participantid, parenteventdefid, studysiteid})) == null || _ds.size() == 0)) {
                            props.clear();
                            props.setProperty("sdcid", "LV_ParticipantEvent");
                            props.setProperty("participantid", participantid);
                            props.setProperty("sstudysiteid", studysiteid);
                            props.setProperty("eventdefid", parenteventdefid);
                            props.setProperty("eventlabel", ds.getString(0, "parenteventdeflabel"));
                            props.setProperty("activeflag", "Y");
                            props.setProperty("eventstatus", "Completed");
                            props.setProperty("eventdt", "n");
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                            parentparticipanteventid = props.getProperty("newkeyid1");
                        }
                        props.clear();
                        props.setProperty("sdcid", "LV_ParticipantEvent");
                        props.setProperty("participantid", participantid);
                        props.setProperty("sstudysiteid", studysiteid);
                        props.setProperty("eventdefid", eventdefid);
                        props.setProperty("eventlabel", ds.getString(0, "eventdeflabel"));
                        props.setProperty("parentparticipanteventid", parentparticipanteventid);
                        props.setProperty("activeflag", "Y");
                        props.setProperty("eventstatus", "Completed");
                        props.setProperty("eventdt", "n");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        participanteventid = props.getProperty("newkeyid1");
                    }
                    catch (ActionException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                participanteventid = ds.getString(0, "s_participanteventid");
            }
            this.localCache.put(key, participanteventid);
        }
        return this.localCache.get(key);
    }

    private String getParticipantID(String studyid, String studysiteid, String subjectid, String cohortid, String externalparticipantid) {
        String key = "participantid" + studyid + studysiteid + subjectid + cohortid;
        if (!this.localCache.containsKey(key)) {
            if (OpalUtil.isNotEmpty(studyid) && OpalUtil.isNotEmpty(studysiteid) && OpalUtil.isNotEmpty(subjectid)) {
                DataSet ds;
                String participantid = "";
                if (OpalUtil.isNotEmpty(cohortid)) {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid from s_participant where sstudyid = ? and studysiteid = ? and subjectid = ? and cpcohortid = ?", (Object[])new String[]{studyid, studysiteid, subjectid, cohortid});
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
                    try {
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
                    catch (ActionException e) {
                        this.localCache.put(key, "");
                        Trace.log("Exception while adding new Participant " + e.getMessage());
                    }
                }
            } else {
                this.localCache.put(key, "");
            }
        }
        return this.localCache.get(key);
    }

    private String getClinicalProtocolProperty(String studyid, String studysiteid, String property) {
        DataSet ds;
        String key = property + studyid + studysiteid;
        if (!this.localCache.containsKey(key) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision from s_studysite where SSTUDYID = ? and S_STUDYSITEID = ?", (Object[])new String[]{studyid, studysiteid})) != null && ds.size() > 0) {
            this.localCache.put("clinicalprotocolid" + studyid + studysiteid, ds.getString(0, "clinicalprotocolid", ""));
            this.localCache.put("clinicalprotocolversionid" + studyid + studysiteid, ds.getString(0, "clinicalprotocolversionid", ""));
            this.localCache.put("clinicalprotocolrevision" + studyid + studysiteid, ds.getString(0, "clinicalprotocolrevision", ""));
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : "";
    }
}

