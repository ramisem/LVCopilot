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

import com.labvantage.opal.util.ColumnUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetStudyExpectedSamples
extends BaseAjaxRequest {
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_STUDYID = "studyid";
    public static final String PARAM_STUDYSITEID = "studysiteid";
    public static final String PARAM_COHORTID = "cohortid";
    public static final String PARAM_SUBJECTID = "subjectid";
    public static final String PARAM_EVENTID = "eventid";
    public static final String PARAM_SUBJECTDISPLAYFORMAT = "subjectdisplayformat";
    public static final String PARAM_SAMPLEALIASSHOW = "samplealiasshow";
    public static final String PARAM_SAMPLEALIASTYPE = "samplealiastype";
    private static final String SQL_PROTOCOL_EVENTDEF = "select sp.s_eventdefid, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.parenteventdefid) parenteventdeflabel, sp.s_sampletypeid, sp.s_specimendefid, sp.specimenlabel, sp.volume, sp.volumeunits, sp.quantity, sp.specimentype, sp.processtype, sp.labelmethodid, sp.labelmethodversionid, ed.eventdeflabel, ed.eventdeftype from s_eventdefstspecimendef sp, s_eventdef ed, s_eventdefsampletype edst where sp.s_eventdefid = ? and ed.s_eventdefid = sp.s_eventdefid and edst.s_eventdefid = sp.s_eventdefid and edst.s_sampletypeid = sp.s_sampletypeid order by edst.usersequence, sp.usersequence";
    private static final String SQL_NONPROTOCOL_EVENTDEF = "select s_clinicaleventid, clinicaleventdesc from s_clinicalevent where s_clinicaleventid = ? and (activeflag = 'Y' or activeflag is null) order by usersequence";
    private Map<String, DataSet> specimenCache;
    private boolean titleRowRendered;
    Map<String, String> protocolMap = new HashMap<String, String>();
    private Map<String, String> localCache = new HashMap<String, String>();

    /*
     * Unable to fully structure code
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block90: {
            block84: {
                block85: {
                    block88: {
                        block86: {
                            policy_participantStudySubjectOnly = false;
                            bioBankingPolicy = null;
                            try {
                                bioBankingPolicy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                                policy_participantStudySubjectOnly = "Study-Subject".equals(bioBankingPolicy.getProperty("participantcreationrule"));
                            }
                            catch (SapphireException e) {
                                e.printStackTrace();
                            }
                            ajaxResponse = new AjaxResponse(request, response);
                            sb = new StringBuilder();
                            param_mode = ajaxResponse.getRequestParameter("mode", "full");
                            param_studyid = ajaxResponse.getRequestParameter("studyid");
                            param_studysiteid = ajaxResponse.getRequestParameter("studysiteid");
                            param_subjectid = ajaxResponse.getRequestParameter("subjectid");
                            param_cohortid = ajaxResponse.getRequestParameter("cohortid");
                            param_eventid = ajaxResponse.getRequestParameter("eventid");
                            param_subjectdisplayformat = ajaxResponse.getRequestParameter("subjectdisplayformat", "[s_subjectid]");
                            param_events = ajaxResponse.getRequestParameter("events");
                            eventsArray = null;
                            try {
                                eventsArray = new JSONArray(param_events);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            requestParam = new PropertyList();
                            requestParam.putAll(ajaxResponse.getRequestParameters());
                            param_attributes = ajaxResponse.getRequestParameter("attributes", "");
                            attributesMap = new HashMap<String, String>();
                            if (OpalUtil.isNotEmpty(param_attributes)) {
                                try {
                                    attributes = new JSONArray(param_attributes);
                                    for (i = 0; i < attributes.length(); ++i) {
                                        jsonObject = attributes.getJSONObject(i);
                                        columnid = jsonObject.has("columnid") != false ? jsonObject.getString("columnid") : "";
                                        v0 = columnvalue = jsonObject.has("value") != false ? jsonObject.getString("value") : "";
                                        if (!OpalUtil.isNotEmpty(columnid)) continue;
                                        attributesMap.put(columnid, columnvalue);
                                    }
                                }
                                catch (JSONException e) {
                                    Logger.logWarn("Unable to parse additional columns");
                                }
                            }
                            columnCollection = new PropertyListCollection();
                            try {
                                columnCollection.setJSONString(ajaxResponse.getRequestParameter("additionalcolumns"));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            sampleAliasTypeDS = this.getQueryProcessor().getRefTypeDataSet("SampleAliasType");
                            sdiRequest = new SDIRequest();
                            sdiRequest.setSDCid("Units");
                            sdiRequest.setRequestItem("primary");
                            sdiRequest.setQueryFrom("units");
                            sdiRequest.setQueryOrderBy("unitsid");
                            sdiRequest.setQueryWhere("unitsid in (select ci.keyid1 from categoryitem ci where ci.sdcid = 'Units' and ci.categoryid in ( 'VolumeUnits', 'MassUnits' ))");
                            unitds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
                            unitList = new ArrayList<String>();
                            for (i = 0; i < unitds.size(); ++i) {
                                unitList.add(unitds.getString(i, "unitsid"));
                            }
                            sql = new StringBuilder();
                            subjectMap = new HashMap<String, String>();
                            safeSQL = new SafeSQL();
                            sql.append("select * from s_subject where s_subjectid in (").append(safeSQL.addIn(param_subjectid, ";")).append(")");
                            subjectds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            if (subjectds != null && subjectds.size() > 0) {
                                for (i = 0; i < subjectds.size(); ++i) {
                                    format = param_subjectdisplayformat;
                                    for (String token : tokens = StringUtil.getTokens(format)) {
                                        format = StringUtil.replaceAll(format, "[" + token + "]", subjectds.getValue(i, token, ""));
                                    }
                                    subjectMap.put(subjectds.getString(i, "s_subjectid"), format);
                                }
                            }
                            isProtocolDriven = Study.isProtocolDriven(this.getQueryProcessor(), param_studyid);
                            subjects = OpalUtil.isNotEmpty(param_subjectid) != false ? StringUtil.split(param_subjectid, ";") : new String[]{};
                            events = StringUtil.split(param_eventid, ";");
                            full = "full".equals(param_mode);
                            additionalColumnCount = 0;
                            for (col = 0; col < columnCollection.size(); ++col) {
                                column = columnCollection.getPropertyList(col);
                                sdcid = column.getProperty("sdcid");
                                if ("CollectionPlan".equals(sdcid)) {
                                    if (!isProtocolDriven || !OpalUtil.isNotEmpty(column.getProperty("specimencolumnid"))) continue;
                                    ++additionalColumnCount;
                                    continue;
                                }
                                v1 = "Sample".equals(sdcid) != false ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) != false ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) != false ? column.getProperty("trackitemcolumn") : ""));
                                if (!OpalUtil.isNotEmpty(columnid)) continue;
                                ++additionalColumnCount;
                            }
                            if (!isProtocolDriven) break block84;
                            if (!full) break block85;
                            sb.append("<table cellpadding=0 cellspacing=0 border=0 class='maintform_table' id='sampleexpectedtable' style='background:white'>");
                            sb.append(this.getTitleRowHTML(columnCollection, true, false, isProtocolDriven, "Y".equals(requestParam.getProperty("samplealiasshow", "Y"))));
                            sb.append("<tbody>");
                            if (subjects.length <= 0) break block86;
                            for (String subjectid : subjects) {
                                sql.setLength(0);
                                sql.append("select s_samplefamilyid, subjectid, sstudyid, clinicalprotocolversionid, studysiteid, eventdefid, sampletypeid, specimendefid, initialvolume, initialvolumeunits, initialmass, initialmassunits");
                                for (col = 0; col < columnCollection.size(); ++col) {
                                    column = columnCollection.getPropertyList(col);
                                    if (!"LV_SampleFamily".equals(column.getProperty("sdcid")) || !OpalUtil.isNotEmpty(columnid = column.getProperty("familycolumn"))) continue;
                                    sql.append(",").append(columnid);
                                }
                                if (policy_participantStudySubjectOnly) {
                                    sql.append(" from s_samplefamily where subjectid = ? and sstudyid = ? order by createdt asc");
                                    sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{subjectid, param_studyid});
                                } else {
                                    sql.append(" from s_samplefamily where subjectid = ? and sstudyid = ? and studysiteid = ? order by createdt asc");
                                    sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{subjectid, param_studyid, param_studysiteid});
                                }
                                sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' subjectid=\"").append(subjectid).append("\" headerrow='Y'>");
                                sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append(">");
                                sb.append("<img src='WEB-CORE/imageref/flat/16/flat_black_minus1_collapse.svg' class='expandsubject' action='expand'>&nbsp;");
                                sb.append("<img src='rc?command=image&image=User' style='width:16px;height:16px'>&nbsp;");
                                sb.append(SafeHTML.encodeForHTML((String)subjectMap.get(subjectid)));
                                sb.append("<span style='float:").append(this.getConnectionProcessor().getSapphireConnection().isRtl() != false ? "left" : "right").append("'>");
                                sb.append(this.getTranslationProcessor().translate("External Participant ID")).append("&nbsp;");
                                p = policy_participantStudySubjectOnly != false ? this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid from s_participant where subjectid = ? and sstudyid = ? and cpcohortid = ?", (Object[])new String[]{subjectid, param_studyid, param_cohortid}) : this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid from s_participant where subjectid = ? and sstudyid = ? and studysiteid = ? and cpcohortid = ?", (Object[])new String[]{subjectid, param_studyid, param_studysiteid, param_cohortid});
                                sb.append("<input class='mandatoryfield' style='width:120px;border:1px solid gray;' subjectid=\"").append(subjectid).append("\" name=\"externalparticipantid\"");
                                if (p != null && p.size() > 0) {
                                    sb.append(" value=\"").append(p.getString(0, "externalparticipantid", "")).append("\"");
                                    sb.append(" participantid=\"").append(p.getString(0, "s_participantid")).append("\"");
                                }
                                if (OpalUtil.isNotEmpty(p.getString(0, "externalparticipantid", ""))) {
                                    sb.append(" readonly");
                                }
                                sb.append(">");
                                sb.append("</span>");
                                sb.append("</td>");
                                sb.append("</tr>");
                                eventList = Arrays.asList(events);
lbl154:
                                // 2 sources

                                try {
                                    for (index = 0; index < eventsArray.length(); ++index) {
                                        block87: {
                                            eventObject = eventsArray.getJSONObject(index);
                                            v2 = event_subjectid = eventObject.has("subjectid") != false ? eventObject.getString("subjectid") : "";
                                            if (event_subjectid.length() > 0 && !event_subjectid.equals(subjectid)) continue;
                                            s_eventdefid = eventObject.getString("eventid");
                                            neweventinstance = eventObject.has("newinstance") != false && "Y".equals(eventObject.getString("newinstance")) != false;
                                            ds = this.getQueryProcessor().getPreparedSqlDataSet("select sp.s_eventdefid, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.parenteventdefid) parenteventdeflabel, sp.s_sampletypeid, sp.s_specimendefid, sp.specimenlabel, sp.volume, sp.volumeunits, sp.quantity, sp.specimentype, sp.processtype, sp.labelmethodid, sp.labelmethodversionid, ed.eventdeflabel, ed.eventdeftype from s_eventdefstspecimendef sp, s_eventdef ed, s_eventdefsampletype edst where sp.s_eventdefid = ? and ed.s_eventdefid = sp.s_eventdefid and edst.s_eventdefid = sp.s_eventdefid and edst.s_sampletypeid = sp.s_sampletypeid order by edst.usersequence, sp.usersequence", (Object[])new String[]{s_eventdefid});
                                            if (ds == null) continue;
                                            if (ds.size() <= 0) break block87;
                                            for (i = 0; i < ds.size(); ++i) {
                                                s_sampletypeid = ds.getString(i, "s_sampletypeid");
                                                s_specimendefid = ds.getString(i, "s_specimendefid");
                                                eventdeftype = ds.getString(i, "eventdeftype");
                                                eventdeflabel = HttpUtil.htmlEncode(ds.getString(i, "eventdeflabel"));
                                                isTimepoint = "Timepoint".equals(eventdeftype);
                                                if (i == 0) {
                                                    if (!isTimepoint) {
                                                        sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                                        sb.append("<td class='maintform_field'>&nbsp;</td>");
                                                        sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                                        sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'> ").append(eventdeftype).append(": ").append(eventdeflabel);
                                                        sb.append("<img class='addrow' subjectid='").append(subjectid).append("' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid='").append(s_sampletypeid).append("'");
                                                        sb.append(" s_specimendefid='").append(s_specimendefid).append("' istimepoint='N' src='rc?command=image&image=TableRowAddAfter'>");
                                                        sb.append("</td>");
                                                        sb.append("</tr>");
                                                    } else {
                                                        parenteventdefid = ds.getString(i, "parenteventdefid", "");
                                                        if (!eventList.contains(parenteventdefid)) {
                                                            parenteventdeflabel = HttpUtil.htmlEncode(ds.getString(i, "parenteventdeflabel", ""));
                                                            sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                                            sb.append("<td class='maintform_field'>&nbsp;</td>");
                                                            sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                                            sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'>").append(parenteventdeflabel);
                                                            sb.append("</td>");
                                                            sb.append("</tr>");
                                                        }
                                                        sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='Y' class='timepointheaderrow'>");
                                                        sb.append("<td class='maintform_field' colspan=2>&nbsp;</td>");
                                                        sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                                        sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTimeClock&size=16'> ").append(this.getProtocolEventDefLabel(s_eventdefid));
                                                        sb.append("<img class='addrow' subjectid='").append(subjectid).append("' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid='").append(s_sampletypeid).append("'");
                                                        sb.append(" s_specimendefid='").append(s_specimendefid).append("' istimepoint='Y' src='rc?command=image&image=TableRowAddAfter'>");
                                                        sb.append("</td>");
                                                        sb.append("</tr>");
                                                    }
                                                }
                                                childds = this.getQueryProcessor().getPreparedSqlDataSet("select e.workitemid, e.workitemversionid, e.embedchildsampleplanid privateplanid, e.embedchildsampleplanversionid privateplanversionid, w.embedchildsampleplanid embedplanid, w.embedchildsampleplanversionid embedplanversionid, (select cp.useforaccessionflag from s_childsampleplan cp where cp.s_childsampleplanid = e.embedchildsampleplanid and cp.s_childsampleplanversionid = e.embedchildsampleplanversionid) privateuseflag, (select cp1.useforaccessionflag from s_childsampleplan cp1 where cp1.s_childsampleplanid = w.embedchildsampleplanid and cp1.s_childsampleplanversionid = w.embedchildsampleplanversionid) embeduseflag from s_eventdefstspecimendefwi e, workitem w where e.s_eventdefid = ? and e.s_sampletypeid = ? and e.s_specimendefid = ? and w.workitemid = e.workitemid and ((w.workitemversionid = e.workitemversionid and e.workitemversionid is not null) or (w.versionstatus='C' and e.workitemversionid is null))", (Object[])new String[]{s_eventdefid, s_sampletypeid, s_specimendefid});
                                                haveChildSamplesToAccession = false;
                                                if (childds != null && childds.size() > 0) {
                                                    haveChildSamplesToAccession = true;
                                                }
                                                quantity = ds.getInt(i, "quantity", 1);
                                                volume = ds.getValue(i, "volume", "");
                                                volumeunits = ds.getString(i, "volumeunits", "");
                                                specimentype = ds.getString(i, "specimentype", "");
                                                for (q = 0; q < quantity; ++q) {
                                                    parentrowuuid = "";
                                                    if (haveChildSamplesToAccession) {
                                                        parentrowuuid = UUID.randomUUID().toString();
                                                    }
                                                    sb.append(this.getRowHTML(requestParam, q, sampleds, subjectid, s_eventdefid, s_sampletypeid, s_specimendefid, specimentype, volume, volumeunits, isTimepoint, columnCollection, attributesMap, unitList, sampleAliasTypeDS, false, false, q == quantity - 1, parentrowuuid, "", "", "", "", neweventinstance));
                                                    if (!haveChildSamplesToAccession) continue;
                                                    parentsampleid = "";
                                                    allchildsampleds = null;
                                                    if (sampleds != null && sampleds.size() > 0) {
                                                        filter = new HashMap<String, String>();
                                                        filter.put("sstudyid", param_studyid);
                                                        filter.put("studysiteid", param_studysiteid);
                                                        filter.put("subjectid", subjectid);
                                                        filter.put("eventdefid", s_eventdefid);
                                                        filter.put("sampletypeid", s_sampletypeid);
                                                        filter.put("specimendefid", s_specimendefid);
                                                        datads = sampleds.getFilteredDataSet(filter);
                                                        if (datads != null && datads.size() > q && !neweventinstance && OpalUtil.isNotEmpty(parentsampleid = this.getPreparedValue("select s_sampleid from s_sample where samplefamilyid = ? and not exists (select sm.destsampleid from s_samplemap sm where sm.destsampleid = s_sample.s_sampleid)", new String[]{datads.getValue(q, "s_samplefamilyid", "")}, "s_sampleid"))) {
                                                            allchildsampleds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, s.sampletypeid, t.containertypeid, t.qtycurrent, t.qtyunits from s_sample s, trackitem t, s_samplemap sm where t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid and sm.destsampleid = s.s_sampleid and sm.sourcesampleid = ?", (Object[])new String[]{parentsampleid});
                                                        }
                                                    }
                                                    for (row = 0; row < childds.size(); ++row) {
                                                        childsampleplanid = "";
                                                        childsampleplanversionid = "";
                                                        if ("Y".equals(childds.getString(row, "privateuseflag"))) {
                                                            childsampleplanid = childds.getString(row, "privateplanid");
                                                            childsampleplanversionid = childds.getString(row, "privateplanversionid");
                                                        } else if ("Y".equals(childds.getString(row, "embeduseflag"))) {
                                                            childsampleplanid = childds.getString(row, "embedplanid");
                                                            childsampleplanversionid = childds.getString(row, "embedplanversionid");
                                                        }
                                                        if (!OpalUtil.isNotEmpty(childsampleplanid) || !OpalUtil.isNotEmpty(childsampleplanversionid) || (childaccessionds = this.getQueryProcessor().getPreparedSqlDataSet("select s_childsampleplanitemid, plantype, containertypeid, childsamplecount, derivativesampletypeid, quantity, quantityunits, processinstruction from s_childsampleplanitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and activeflag = 'Y' and parentitemid is null", (Object[])new String[]{childsampleplanid, childsampleplanversionid})) == null || childaccessionds.size() <= 0) continue;
                                                        for (childrow = 0; childrow < childaccessionds.size(); ++childrow) {
                                                            childsampleplanitemid = childaccessionds.getString(childrow, "s_childsampleplanitemid");
                                                            childsamplecount = Integer.parseInt(childaccessionds.getValue(childrow, "childsamplecount", "0"));
                                                            childsampletypeid = "Derivative".equals(childaccessionds.getString(childrow, "plantype")) != false ? childaccessionds.getString(childrow, "derivativesampletypeid", s_sampletypeid) : s_sampletypeid;
                                                            for (childquantity = 0; childquantity < childsamplecount; ++childquantity) {
                                                                childspecimentype = childaccessionds.getValue(childrow, "containertypeid", "");
                                                                childvolume = childaccessionds.getValue(childrow, "quantity", "");
                                                                childvolumeunits = childaccessionds.getValue(childrow, "quantityunits", "");
                                                                childrowcount = 0;
                                                                if (allchildsampleds != null) {
                                                                    for (k = 0; k < allchildsampleds.size(); ++k) {
                                                                        if (!allchildsampleds.getValue(k, "containertypeid", "").equals(childspecimentype) || !allchildsampleds.getValue(k, "sampletypeid", "").equals(childsampletypeid)) continue;
                                                                        ++childrowcount;
                                                                    }
                                                                }
                                                                childsampleexists = false;
                                                                if (!neweventinstance) {
                                                                    v3 = childsampleexists = childrowcount > childquantity;
                                                                    if (childsampleexists) {
                                                                        childvolume = allchildsampleds.getValue(childquantity, "qtycurrent", "");
                                                                        childvolumeunits = allchildsampleds.getValue(childquantity, "qtyunits", "");
                                                                    }
                                                                }
                                                                sb.append(this.getRowHTML(requestParam, childquantity, null, subjectid, s_eventdefid, childsampletypeid, s_specimendefid, childspecimentype, childvolume, childvolumeunits, isTimepoint, columnCollection, attributesMap, unitList, sampleAliasTypeDS, true, childsampleexists, childquantity == childsamplecount - 1, parentrowuuid, parentsampleid, childsampleplanid, childsampleplanversionid, childsampleplanitemid, neweventinstance));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            ** GOTO lbl154
                                        }
                                        eventds = this.getQueryProcessor().getPreparedSqlDataSet("select ed.eventdeftype, ed.eventdeflabel, ed.parenteventdefid,  (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.parenteventdefid) parenteventdeflabel from s_eventdef ed where ed.s_eventdefid = ?", (Object[])new String[]{s_eventdefid});
                                        if (eventds == null || eventds.size() <= 0) continue;
                                        eventdeftype = eventds.getString(0, "eventdeftype");
                                        eventdeflabel = HttpUtil.htmlEncode(eventds.getString(0, "eventdeflabel"));
                                        isTimepoint = "Timepoint".equals(eventdeftype);
                                        sb.append(this.getTitleRowHTML(columnCollection, false, isTimepoint, isProtocolDriven, "Y".equals(requestParam.getProperty("samplealiasshow", "Y"))));
                                        if (!isTimepoint) {
                                            sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                            sb.append("<td class='maintform_field'>&nbsp;</td>");
                                            sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append("'>");
                                            sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'> ").append(eventdeftype).append(": ").append(eventdeflabel);
                                            sb.append("<img class='addrow' subjectid='").append(subjectid).append("' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid=''");
                                            sb.append(" s_specimendefid='' istimepoint='N' src='rc?command=image&image=TableRowAddAfter'>");
                                            sb.append("</td>");
                                            sb.append("</tr>");
                                            continue;
                                        }
                                        parenteventdefid = eventds.getString(0, "parenteventdefid", "");
                                        if (!eventList.contains(parenteventdefid)) {
                                            parenteventdeflabel = HttpUtil.htmlEncode(eventds.getString(0, "parenteventdeflabel", ""));
                                            sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                            sb.append("<td class='maintform_field'>&nbsp;</td>");
                                            sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append("'>");
                                            sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'>").append(parenteventdeflabel);
                                            sb.append("</td>");
                                            sb.append("</tr>");
                                        }
                                        sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='Y' class='timepointheaderrow'>");
                                        sb.append("<td class='maintform_field' colspan=2>&nbsp;</td>");
                                        sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append("'>");
                                        sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTimeClock&size=16'> ").append(this.getProtocolEventDefLabel(s_eventdefid));
                                        sb.append("<img class='addrow' subjectid='").append(subjectid).append("' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid=''");
                                        sb.append(" s_specimendefid='' istimepoint='Y' src='rc?command=image&image=TableRowAddAfter'>");
                                        sb.append("</td>");
                                        sb.append("</tr>");
                                    }
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            break block88;
                        }
                        sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' subjectid='' headerrow='Y' childsamplerow=N>");
                        sb.append("<td class='maintform_field' colspan='").append(10 + additionalColumnCount).append(">");
                        sb.append("<img src='WEB-CORE/imageref/flat/16/flat_black_minus1_collapse.svg' class='expandsubject' action='expand'>&nbsp;");
                        sb.append("<img src='rc?command=image&image=UserDelete' style='width:16px;height:16px'>&nbsp;");
                        sb.append("<span style='color:red'>").append(this.getTranslationProcessor().translate("Receiving samples without any Subject information")).append("</span>");
                        sb.append("</td>");
                        sb.append("</tr>");
                        eventList = Arrays.asList(events);
lbl372:
                        // 2 sources

                        try {
                            for (index = 0; index < eventsArray.length(); ++index) {
                                block89: {
                                    eventObject = eventsArray.getJSONObject(index);
                                    s_eventdefid = eventObject.getString("eventid");
                                    neweventinstance = eventObject.has("newinstance") != false && "Y".equals(eventObject.getString("newinstance")) != false;
                                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select sp.s_eventdefid, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.parenteventdefid) parenteventdeflabel, sp.s_sampletypeid, sp.s_specimendefid, sp.specimenlabel, sp.volume, sp.volumeunits, sp.quantity, sp.specimentype, sp.processtype, sp.labelmethodid, sp.labelmethodversionid, ed.eventdeflabel, ed.eventdeftype from s_eventdefstspecimendef sp, s_eventdef ed, s_eventdefsampletype edst where sp.s_eventdefid = ? and ed.s_eventdefid = sp.s_eventdefid and edst.s_eventdefid = sp.s_eventdefid and edst.s_sampletypeid = sp.s_sampletypeid order by edst.usersequence, sp.usersequence", (Object[])new String[]{s_eventdefid});
                                    if (ds == null) continue;
                                    if (ds.size() <= 0) break block89;
                                    for (i = 0; i < ds.size(); ++i) {
                                        s_sampletypeid = ds.getString(i, "s_sampletypeid");
                                        s_specimendefid = ds.getString(i, "s_specimendefid");
                                        eventdeftype = ds.getString(i, "eventdeftype");
                                        eventdeflabel = HttpUtil.htmlEncode(ds.getString(i, "eventdeflabel"));
                                        isTimepoint = "Timepoint".equals(eventdeftype);
                                        if (i == 0) {
                                            sb.append(this.getTitleRowHTML(columnCollection, true, isTimepoint, isProtocolDriven, "Y".equals(requestParam.getProperty("samplealiasshow", "Y"))));
                                            if (!isTimepoint) {
                                                sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                                sb.append("<td class='maintform_field'>&nbsp;</td>");
                                                sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                                sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'>").append(eventdeflabel);
                                                sb.append("<img class='addrow' subjectid='' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid='").append(s_sampletypeid).append("'");
                                                sb.append(" s_specimendefid='").append(s_specimendefid).append("' istimepoint='N' src='rc?command=image&image=TableRowAddAfter'>");
                                                sb.append("</td>");
                                                sb.append("</tr>");
                                            } else {
                                                parenteventdefid = ds.getString(i, "parenteventdefid", "");
                                                if (!eventList.contains(parenteventdefid)) {
                                                    parenteventdeflabel = HttpUtil.htmlEncode(ds.getString(i, "parenteventdeflabel", ""));
                                                    sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                                    sb.append("<td class='maintform_field'>&nbsp;</td>");
                                                    sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                                    sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'>").append(parenteventdeflabel);
                                                    sb.append("</td>");
                                                    sb.append("</tr>");
                                                }
                                                sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='Y' class='timepointheaderrow'>");
                                                sb.append("<td class='maintform_field' colspan=2>&nbsp;</td>");
                                                sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append("'>");
                                                sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTimeClock&size=16'> ").append(this.getProtocolEventDefLabel(s_eventdefid));
                                                sb.append("<img class='addrow' subjectid='' s_eventdefid='").append(s_eventdefid).append("' s_sampletypeid='").append(s_sampletypeid).append("'");
                                                sb.append(" s_specimendefid='").append(s_specimendefid).append("' istimepoint='Y' src='rc?command=image&image=TableRowAddAfter'>");
                                                sb.append("</td>");
                                                sb.append("</tr>");
                                            }
                                        }
                                        childds = this.getQueryProcessor().getPreparedSqlDataSet("select e.workitemid, e.workitemversionid, e.embedchildsampleplanid privateplanid, e.embedchildsampleplanversionid privateplanversionid, w.embedchildsampleplanid embedplanid, w.embedchildsampleplanversionid embedplanversionid, (select cp.useforaccessionflag from s_childsampleplan cp where cp.s_childsampleplanid = e.embedchildsampleplanid and cp.s_childsampleplanversionid = e.embedchildsampleplanversionid) privateuseflag, (select cp1.useforaccessionflag from s_childsampleplan cp1 where cp1.s_childsampleplanid = w.embedchildsampleplanid and cp1.s_childsampleplanversionid = w.embedchildsampleplanversionid) embeduseflag from s_eventdefstspecimendefwi e, workitem w where e.s_eventdefid = ? and e.s_sampletypeid = ? and e.s_specimendefid = ? and w.workitemid = e.workitemid and ((w.workitemversionid = e.workitemversionid and e.workitemversionid is not null) or (w.versionstatus='C' and e.workitemversionid is null))", (Object[])new String[]{s_eventdefid, s_sampletypeid, s_specimendefid});
                                        haveChildSamplesToAccession = false;
                                        if (childds != null && childds.size() > 0) {
                                            haveChildSamplesToAccession = true;
                                        }
                                        quantity = ds.getInt(i, "quantity", 1);
                                        volume = ds.getValue(i, "volume", "");
                                        volumeunits = ds.getString(i, "volumeunits", "");
                                        specimentype = ds.getValue(i, "specimentype", "");
                                        for (q = 0; q < quantity; ++q) {
                                            parentrowuuid = "";
                                            if (haveChildSamplesToAccession) {
                                                parentrowuuid = UUID.randomUUID().toString();
                                            }
                                            sb.append(this.getRowHTML(requestParam, q, null, "", s_eventdefid, s_sampletypeid, s_specimendefid, specimentype, volume, volumeunits, isTimepoint, columnCollection, attributesMap, unitList, sampleAliasTypeDS, false, false, q == quantity - 1, parentrowuuid, "", "", "", "", neweventinstance));
                                            if (!haveChildSamplesToAccession) continue;
                                            for (row = 0; row < childds.size(); ++row) {
                                                childsampleplanid = "";
                                                childsampleplanversionid = "";
                                                if ("Y".equals(childds.getString(row, "privateuseflag"))) {
                                                    childsampleplanid = childds.getString(row, "privateplanid");
                                                    childsampleplanversionid = childds.getString(row, "privateplanversionid");
                                                } else if ("Y".equals(childds.getString(row, "embeduseflag"))) {
                                                    childsampleplanid = childds.getString(row, "embedplanid");
                                                    childsampleplanversionid = childds.getString(row, "embedplanversionid");
                                                }
                                                if (!OpalUtil.isNotEmpty(childsampleplanid) || !OpalUtil.isNotEmpty(childsampleplanversionid) || (childaccessionds = this.getQueryProcessor().getPreparedSqlDataSet("select s_childsampleplanitemid, plantype, containertypeid, childsamplecount, derivativesampletypeid, quantity, quantityunits from s_childsampleplanitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and activeflag = 'Y' and parentitemid is null", (Object[])new String[]{childsampleplanid, childsampleplanversionid})) == null) continue;
                                                for (childrow = 0; childrow < childaccessionds.size(); ++childrow) {
                                                    childsampleplanitemid = childaccessionds.getString(childrow, "s_childsampleplanitemid");
                                                    childsamplecount = Integer.parseInt(childaccessionds.getValue(childrow, "childsamplecount", "0"));
                                                    childsampletypeid = "Derivative".equals(childaccessionds.getString(childrow, "plantype")) != false ? childaccessionds.getString(childrow, "derivativesampletypeid", s_sampletypeid) : s_sampletypeid;
                                                    for (childquantity = 0; childquantity < childsamplecount; ++childquantity) {
                                                        childspecimentype = childaccessionds.getValue(childrow, "containertypeid", "");
                                                        childvolume = childaccessionds.getValue(childrow, "quantity", "");
                                                        childvolumeunits = childaccessionds.getValue(childrow, "quantityunits", "");
                                                        sb.append(this.getRowHTML(requestParam, childquantity, null, "", s_eventdefid, childsampletypeid, "", childspecimentype, childvolume, childvolumeunits, isTimepoint, columnCollection, attributesMap, unitList, sampleAliasTypeDS, true, false, childquantity == childsamplecount - 1, parentrowuuid, "", childsampleplanid, childsampleplanversionid, childsampleplanitemid, false));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    ** GOTO lbl372
                                }
                                eventds = this.getQueryProcessor().getPreparedSqlDataSet("select ed.eventdeftype, ed.eventdeflabel, ed.parenteventdefid, (select ped.eventdeflabel from s_eventdef ped where ped.s_eventdefid = ed.parenteventdefid) parenteventdeflabel from s_eventdef ed where ed.s_eventdefid = ?", (Object[])new String[]{s_eventdefid});
                                if (eventds == null || eventds.size() <= 0) continue;
                                eventdeftype = eventds.getString(0, "eventdeftype");
                                eventdeflabel = HttpUtil.htmlEncode(eventds.getString(0, "eventdeflabel"));
                                isTimepoint = "Timepoint".equals(eventdeftype);
                                sb.append(this.getTitleRowHTML(columnCollection, false, isTimepoint, isProtocolDriven, "Y".equals(requestParam.getProperty("samplealiasshow", "Y"))));
                                if (!isTimepoint) {
                                    sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                    sb.append("<td class='maintform_field'>&nbsp;</td>");
                                    sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                    sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'> ").append(eventdeftype).append(": ").append(eventdeflabel);
                                    sb.append("</td>");
                                    sb.append("</tr>");
                                    continue;
                                }
                                parenteventdefid = eventds.getString(0, "parenteventdefid", "");
                                if (!eventList.contains(parenteventdefid)) {
                                    parenteventdeflabel = HttpUtil.htmlEncode(eventds.getString(0, "parenteventdeflabel", ""));
                                    sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                    sb.append("<td class='maintform_field'>&nbsp;</td>");
                                    sb.append("<td class='maintform_field' colspan='").append(9 + additionalColumnCount).append("'>");
                                    sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'>").append(parenteventdeflabel);
                                    sb.append("</td>");
                                    sb.append("</tr>");
                                }
                                sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='Y' class='timepointheaderrow'>");
                                sb.append("<td class='maintform_field' colspan=2>&nbsp;</td>");
                                sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append("'>");
                                sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTimeClock&size=16'> ").append(this.getProtocolEventDefLabel(s_eventdefid));
                                sb.append("</td>");
                                sb.append("</tr>");
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    sb.append("</tbody></table>");
                    break block90;
                }
                sb.append(this.getRowHTML(requestParam, -1, null, ajaxResponse.getRequestParameter("subjectid"), param_eventid, ajaxResponse.getRequestParameter("s_sampletypeid", ""), ajaxResponse.getRequestParameter("s_specimendefid", ""), ajaxResponse.getRequestParameter("specimentype", ""), ajaxResponse.getRequestParameter("qtycurrent", ""), ajaxResponse.getRequestParameter("qtyunits", ""), "Y".equals(ajaxResponse.getRequestParameter("istimepoint")), columnCollection, attributesMap, unitList, sampleAliasTypeDS, "Y".equals(ajaxResponse.getRequestParameter("childrow", "N")), false, false, ajaxResponse.getRequestParameter("parentrowuuid", ""), ajaxResponse.getRequestParameter("parentsampleid", ""), ajaxResponse.getRequestParameter("childsampleplanid", ""), ajaxResponse.getRequestParameter("childsampleplanversionid", ""), ajaxResponse.getRequestParameter("childsampleplanitemid", ""), false));
                break block90;
            }
            if (full) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0 class='maintform_table' id='sampleexpectedtable' style='background:white'>");
                sb.append("<tbody>");
                if (subjects.length > 0) {
                    for (String subjectid : subjects) {
                        sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' subjectid=\"").append(subjectid).append("\" headerrow='Y'>");
                        sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append(">");
                        sb.append("<img src='WEB-CORE/imageref/flat/16/flat_black_minus1_collapse.svg' class='expandsubject' action='expand'>&nbsp;");
                        sb.append("<img src='rc?command=image&image=User' style='width:16px;height:16px'>&nbsp;");
                        sb.append(SafeHTML.encodeForHTML((String)subjectMap.get(subjectid)));
                        sb.append("<span style='float:right'>");
                        sb.append(this.getTranslationProcessor().translate("External Participant ID")).append("&nbsp;");
                        p = policy_participantStudySubjectOnly != false ? this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid from s_participant where subjectid = ? and sstudyid = ?", (Object[])new String[]{subjectid, param_studyid}) : this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid from s_participant where subjectid = ? and sstudyid = ? and studysiteid = ?", (Object[])new String[]{subjectid, param_studyid, param_studysiteid});
                        sb.append("<input style='width:120px' subjectid=\"").append(subjectid).append("\" name=\"externalparticipantid\"");
                        if (p != null && p.size() > 0) {
                            sb.append(" value=\"").append(p.getString(0, "externalparticipantid", "")).append("\"");
                            sb.append(" participantid=\"").append(p.getString(0, "s_participantid")).append("\" readonly");
                        }
                        sb.append(">");
                        sb.append("</span>");
                        sb.append("</td>");
                        sb.append("</tr>");
                        for (String s_eventdefid : events) {
                            ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_clinicaleventid, clinicaleventdesc from s_clinicalevent where s_clinicaleventid = ? and (activeflag = 'Y' or activeflag is null) order by usersequence", (Object[])new String[]{s_eventdefid});
                            if (ds == null || ds.size() == 0) {
                                row = ds.addRow();
                                ds.setString(row, "s_clinicaleventid", s_eventdefid);
                                ds.setString(row, "clinicaleventdesc", s_eventdefid);
                            }
                            if (ds == null || ds.size() <= 0) continue;
                            colCount = 0;
                            sb.append("<thead id='sampleexpectedtableheader'>");
                            sb.append("<tr>");
                            sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
                            sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Sample Type")).append("</td>");
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Container")).append("</td>");
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Volume")).append("</td>");
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Unit")).append("</td>");
                            for (col = 0; col < columnCollection.size(); ++col) {
                                column = columnCollection.getPropertyList(col);
                                sdcid = column.getProperty("sdcid");
                                v4 = "Sample".equals(sdcid) != false ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) != false ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) != false ? column.getProperty("trackitemcolumn") : ""));
                                if (!OpalUtil.isNotEmpty(columnid)) continue;
                                sb.append("<td class='maintform_fieldtitle' style='text-align:center' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>");
                                sb.append(column.getProperty("title", "&nbsp;"));
                                sb.append("</td>");
                                ++additionalColumnCount;
                            }
                            if ("Y".equals(requestParam.getProperty("samplealiasshow", "Y"))) {
                                sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Sample Alias")).append("</td>");
                                sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Alias Type")).append("</td>");
                            }
                            sb.append("</tr>");
                            sb.append("</thead>");
                            for (i = 0; i < ds.size(); ++i) {
                                s_clinicaleventid = ds.getString(i, "s_clinicaleventid");
                                clinicaleventdesc = ds.getString(i, "clinicaleventdesc");
                                if (i == 0) {
                                    sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                    sb.append("<td class='maintform_field'>&nbsp;</td>");
                                    sb.append("<td class='maintform_field' colspan='").append(7 + additionalColumnCount).append("'>");
                                    sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'> ").append(SafeHTML.encodeForHTML(clinicaleventdesc, true));
                                    sb.append("<img class='addrow' subjectid='").append(subjectid).append("' s_eventdefid='' s_sampletypeid=''");
                                    sb.append(" s_specimendefid='' istimepoint='N' src='rc?command=image&image=TableRowAddAfter' s_clinicaleventid=\"").append(s_clinicaleventid).append("\">");
                                    sb.append("</td>");
                                    sb.append("</tr>");
                                }
                                sb.append(this.getNonProtocolRowHTML(requestParam, subjectid, s_clinicaleventid, clinicaleventdesc, columnCollection, attributesMap, sampleAliasTypeDS, unitList));
                            }
                        }
                    }
                } else {
                    sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' subjectid=\"\" headerrow='Y' class='eventheaderrow'>");
                    sb.append("<td class='maintform_field' colspan='").append(8 + additionalColumnCount).append(">");
                    sb.append("<img src='WEB-CORE/imageref/flat/16/flat_black_minus1_collapse.svg' class='expandsubject' action='expand'>&nbsp;");
                    sb.append("<img src='rc?command=image&image=UserDelete' style='width:16px;height:16px'>&nbsp;");
                    sb.append("<span style='color:red'>").append(this.getTranslationProcessor().translate("Receiving samples without any Subject information")).append("</span>");
                    sb.append("</td>");
                    sb.append("</tr>");
                    for (String s_eventdefid : events) {
                        ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_clinicaleventid, clinicaleventdesc from s_clinicalevent where s_clinicaleventid = ? and (activeflag = 'Y' or activeflag is null) order by usersequence", (Object[])new String[]{s_eventdefid});
                        if (ds == null || ds.size() == 0) {
                            row = ds.addRow();
                            ds.setString(row, "s_clinicaleventid", s_eventdefid);
                            ds.setString(row, "clinicaleventdesc", s_eventdefid);
                        }
                        if (ds == null || ds.size() <= 0) continue;
                        colCount = 0;
                        sb.append("<thead id='sampleexpectedtableheader'>");
                        sb.append("<tr>");
                        sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
                        sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
                        sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Sample Type")).append("</td>");
                        sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Container")).append("</td>");
                        sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Volume")).append("</td>");
                        sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Unit")).append("</td>");
                        for (col = 0; col < columnCollection.size(); ++col) {
                            column = columnCollection.getPropertyList(col);
                            if ("hidden".equals(column.getProperty("mode"))) continue;
                            sdcid = column.getProperty("sdcid");
                            v5 = "Sample".equals(sdcid) != false ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) != false ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) != false ? column.getProperty("trackitemcolumn") : ""));
                            if (!OpalUtil.isNotEmpty(columnid)) continue;
                            sb.append("<th class='maintform_fieldtitle' style='text-align:center' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(")\">");
                            sb.append(column.getProperty("title", "&nbsp;"));
                            sb.append("</th>");
                            ++additionalColumnCount;
                        }
                        if ("Y".equals(requestParam.getProperty("samplealiasshow", "Y"))) {
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Sample Alias")).append("</td>");
                            sb.append("<th class='maintform_fieldtitle' onclick='mainthandler1.selectColumn(").append(colCount++).append(");'>").append(this.getTranslationProcessor().translate("Alias Type")).append("</td>");
                        }
                        sb.append("</tr>");
                        sb.append("</thead>");
                        for (i = 0; i < ds.size(); ++i) {
                            s_clinicaleventid = ds.getString(i, "s_clinicaleventid");
                            clinicaleventdesc = ds.getString(i, "clinicaleventdesc");
                            if (i == 0) {
                                sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='N' class='visitheaderrow'>");
                                sb.append("<td class='maintform_field'>&nbsp;</td>");
                                sb.append("<td class='maintform_field' colspan='").append(7 + additionalColumnCount).append("'>");
                                sb.append("<input type='checkbox' class='selectorAll' onclick='selectAllExpectedSamples()'>&nbsp;<img src='rc?command=image&image=FlatBlackTesttube2&size=16'> ").append(clinicaleventdesc);
                                sb.append("<img class='addrow' subjectid='' s_eventdefid='' s_sampletypeid=''");
                                sb.append(" s_specimendefid='' istimepoint='N' src='rc?command=image&image=TableRowAddAfter' s_clinicaleventid=\"").append(s_clinicaleventid).append("\">");
                                sb.append("</td>");
                                sb.append("</tr>");
                            }
                            sb.append(this.getNonProtocolRowHTML(requestParam, "", s_clinicaleventid, clinicaleventdesc, columnCollection, attributesMap, sampleAliasTypeDS, unitList));
                        }
                    }
                }
                sb.append("</tbody></table>");
            } else {
                sb.append(this.getNonProtocolRowHTML(requestParam, ajaxResponse.getRequestParameter("subjectid"), ajaxResponse.getRequestParameter("s_clinicaleventid"), ajaxResponse.getRequestParameter("clinicaleventdesc"), columnCollection, attributesMap, sampleAliasTypeDS, unitList));
            }
        }
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.addCallbackArgument("rowid", ajaxResponse.getRequestParameter("rowid", ""));
        ajaxResponse.print();
    }

    private String getNonProtocolRowHTML(PropertyList requestParam, String subjectid, String clinicaleventid, String clinicaleventdesc, PropertyListCollection columnCollection, Map<String, String> attributesMap, DataSet sampleAliasTypeDS, List<String> unitsList) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr datarow=Y>");
        sb.append("<td class='maintform_field'>&nbsp;</td>");
        sb.append("<td class='maintform_field' align=center style='padding-top:10px;'>");
        sb.append("<input class='sampleselector' type='checkbox'>");
        sb.append("<input type='hidden' datafield=Y columnid='studyid' value='").append(requestParam.getProperty(PARAM_STUDYID)).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='clinicalflag' value='N'>");
        sb.append("<input type='hidden' datafield=Y columnid='studysiteid' value='").append(requestParam.getProperty(PARAM_STUDYSITEID)).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='subjectid' value='").append(subjectid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='clinicaleventid' value='").append(clinicaleventid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='clinicaleventdesc' value='").append(clinicaleventdesc).append("'>");
        sb.append("</td>");
        String validation_sampletype = "";
        String validation_specimentype = "";
        String validation_volume = "";
        String title_sampletype = "";
        String title_specimentype = "";
        String title_volume = "";
        for (int col = 0; col < columnCollection.size(); ++col) {
            PropertyList column = columnCollection.getPropertyList(col);
            if (!"CollectionPlan".equals(column.getProperty("sdcid"))) continue;
            String columnid = column.getProperty("specimencolumnid");
            if ("s_sampletypeid".equals(columnid)) {
                validation_sampletype = column.getProperty("validation");
                title_sampletype = column.getProperty("title", column.getProperty("columnid"));
                continue;
            }
            if ("specimentype".equals(columnid)) {
                validation_specimentype = column.getProperty("validation");
                title_specimentype = column.getProperty("title", column.getProperty("columnid"));
                continue;
            }
            if (!"volume".equals(columnid)) continue;
            validation_volume = column.getProperty("validation");
            title_volume = column.getProperty("title", column.getProperty("columnid"));
        }
        String uuid = UUID.randomUUID().toString();
        uuid = StringUtil.replaceAll(uuid, "-", "");
        String sampletypefieldid = "field_" + uuid;
        sb.append("<td class='maintform_field'>");
        sb.append("<input name='").append(sampletypefieldid).append("' id='").append(sampletypefieldid).append("'");
        if (validation_sampletype.length() > 0) {
            sb.append(validation_sampletype.contains("Mandatory") ? " class='mandatoryfield'" : "");
            sb.append(" validation=\"").append(validation_sampletype).append("\" ttl=\"").append(title_sampletype).append("\"");
        }
        sb.append(" uuid=\"").append(uuid).append("\"");
        sb.append(" edit=\"lookup\" type=\"text\"");
        sb.append(" datafield=Y columnid='sampletypeid'");
        sb.append(" onkeyup=\"showSuggestion()\"");
        sb.append(" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear('").append(sampletypefieldid).append("');}\"");
        sb.append(" onchange=\"handleSampleTypeChange(this)\" readonly style='border:1px solid limegreen;'>");
        sb.append("<script>var oLUPD_").append(sampletypefieldid).append(" = {\"selectortype\": \"\", \"sdcid\": \"SampleType\", restrictivewhere: \"templateflag != 'Y'\"};</script>");
        sb.append("</td>");
        sb.append("<td class='maintform_field'>");
        sb.append("<select style='height:22px;' datafield=Y columnid='specimentype' style='width:100%'");
        if (validation_specimentype.length() > 0) {
            sb.append(validation_specimentype.contains("Mandatory") ? " class='mandatoryfield'" : "");
            sb.append(" validation=\"").append(validation_specimentype).append("\" ttl=\"").append(title_specimentype).append("\"");
        }
        sb.append(" uuid='").append(uuid).append("' onchange=\"selectRow()\" id='").append(UUID.randomUUID().toString()).append("'>");
        sb.append("<option></option>");
        sb.append("</select>");
        sb.append("</td>");
        sb.append("<td class='maintform_field'>");
        sb.append("<input datafield=Y columnid='qtycurrent' style='width:40px;text-align:right;' onchange=\"selectRow()\"");
        if (validation_volume.length() > 0) {
            sb.append(validation_volume.contains("Mandatory") ? " class='mandatoryfield'" : "");
            sb.append(" validation=\"").append(validation_volume).append("\" ttl=\"").append(title_volume).append("\"");
        }
        sb.append(" id='").append(UUID.randomUUID().toString()).append("'></td>");
        sb.append("<td class='maintform_field'>");
        sb.append("<select datafield=Y columnid='qtyunits' style='height:22px;'");
        if (validation_volume.length() > 0 && validation_volume.contains("Mandatory")) {
            sb.append(" class='mandatoryfield' validation='Mandatory;' ttl=\"").append(this.getTranslationProcessor().translate("Units")).append("\"");
        }
        sb.append(" id='").append(UUID.randomUUID().toString()).append("'>");
        sb.append("<option></option>");
        for (String unitid : unitsList) {
            sb.append("<option>").append(unitid).append("</option>");
        }
        sb.append("</select></td>");
        for (int col = 0; col < columnCollection.size(); ++col) {
            String columnLength;
            String value;
            String columnid;
            String table;
            String sdcid;
            PropertyList column = columnCollection.getPropertyList(col);
            if ("hidden".equals(column.getProperty(PARAM_MODE)) || "CollectionPlan".equals(sdcid = column.getProperty("sdcid"))) continue;
            String string = "Sample".equals(sdcid) ? "s_sample" : ("LV_SampleFamily".equals(sdcid) ? "s_samplefamily" : (table = "TrackItemSDC".equals(sdcid) ? "trackitem" : ""));
            String string2 = "Sample".equals(sdcid) ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) ? column.getProperty("trackitemcolumn") : ""));
            if (!OpalUtil.isNotEmpty(columnid)) continue;
            String validation = column.getProperty("validation");
            sb.append("<td class='maintform_field'>");
            String attributekey = table + "." + columnid;
            String string3 = value = attributesMap != null && attributesMap.containsKey(attributekey) ? attributesMap.get(attributekey) : "";
            if (OpalUtil.isEmpty(value)) {
                value = column.getProperty("default", "");
            }
            String fieldid = UUID.randomUUID().toString();
            String mode = column.getProperty(PARAM_MODE);
            String attribute = "datafield=Y columnid='" + attributekey + "' value=\"" + value + "\" onchange=\"selectRow()\"";
            if ("readonly".equals(mode)) {
                attribute = attribute + " readonly";
                sb.append(ColumnUtil.getInputHtml(column, fieldid, attribute));
            } else if ("input".equals(mode)) {
                if (validation.length() > 0) {
                    attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                }
                columnLength = this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "columnlength");
                attribute = attribute + " maxlength='" + columnLength + "'";
                sb.append(ColumnUtil.getInputHtml(column, fieldid, attribute));
            } else if ("lookup".equals(mode)) {
                if (validation.length() > 0) {
                    attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                }
                columnLength = this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "columnlength");
                attribute = attribute + " maxlength='" + columnLength + "'";
                sb.append(ColumnUtil.getLookupHtml(column, fieldid, attribute));
            } else if ("datelookup".equals(mode)) {
                String dateFormat;
                if (validation.length() > 0) {
                    attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                }
                if ((dateFormat = column.getProperty("format", "").trim()).length() == 0) {
                    try {
                        dateFormat = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom").getProperty("defaultdateformat", "");
                        column.setProperty("format", dateFormat);
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                }
                sb.append(ColumnUtil.getDateLookupHtml(column, fieldid, attribute));
            } else if ("dropdownlist".equals(mode)) {
                if (validation.length() > 0) {
                    attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                }
                sb.append(ColumnUtil.getDropDownListHtml(column, fieldid, attribute, this.getQueryProcessor(), this.getSDIProcessor()));
            }
            sb.append("</td>");
        }
        if ("Y".equals(requestParam.getProperty(PARAM_SAMPLEALIASSHOW))) {
            String samplealiastype = requestParam.getProperty(PARAM_SAMPLEALIASTYPE);
            sb.append("<td class='maintform_field'>");
            sb.append("<input datafield=Y columnid='s_sample.aliasid' style='width:120px;' value=\"\" onchange=\"selectRow()\" id='").append(UUID.randomUUID().toString()).append("' style='vertical-align:top'>");
            sb.append("</td>");
            if (OpalUtil.isEmpty(samplealiastype)) {
                sb.append("<td class='maintform_field'>");
                sb.append("<select datafield=Y columnid='s_sample.aliastype' style='height:22px;' id='").append(UUID.randomUUID().toString()).append("'>");
                sb.append("<option></option>");
                for (int i = 0; i < sampleAliasTypeDS.size(); ++i) {
                    String refvalue = sampleAliasTypeDS.getString(i, "refvalueid");
                    String refdisplayvalue = sampleAliasTypeDS.getString(i, "refdisplayvalueid", refvalue);
                    sb.append("<option value=\"").append(refvalue).append("\">").append(SafeHTML.encodeForHTML(refdisplayvalue)).append("</option>");
                }
                sb.append("</select></td>");
            } else {
                sb.append("<td class='maintform_field'>");
                sb.append("<input type=hidden datafield=Y columnid='s_sample.aliastype' value=\"").append(samplealiastype).append("\">");
                sb.append("</td>");
            }
        }
        sb.append("</tr>");
        return sb.toString();
    }

    private String getRowHTML(PropertyList requestParam, int quantityCount, DataSet sampleds, String subjectid, String s_eventdefid, String s_sampletypeid, String s_specimendefid, String specimentype, String volume, String volumeunits, boolean isTimepoint, PropertyListCollection columnCollection, Map<String, String> attributesMap, List<String> unitsList, DataSet sampleAliasTypeDS, boolean isChildSample, boolean doesChildSampleExists, boolean isLastRow, String parentrowuuid, String parentsampleid, String childsampleplanid, String childsampleplanversionid, String childsampleplanitemid, boolean neweventinstance) {
        DataSet datads = null;
        boolean isSampleReceived = false;
        if (!neweventinstance && sampleds != null && sampleds.size() > 0) {
            DataSet ds;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put(PARAM_SUBJECTID, subjectid);
            filter.put("eventdefid", s_eventdefid);
            filter.put("sampletypeid", s_sampletypeid);
            filter.put("specimendefid", s_specimendefid);
            datads = sampleds.getFilteredDataSet(filter);
            if (datads != null && datads.size() > 0 && datads.size() > quantityCount) {
                sampleds.setString(sampleds.findRow(filter), "receivedflag", "Y");
                isSampleReceived = true;
            }
            if (!isSampleReceived && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select t2.s_eventdefid, (select s_specimendefid from s_eventdefstspecimendef where s_eventdefid = t2.s_eventdefid and s_sampletypeid = ?) specimendefid from s_eventdef t1, s_eventdef t2 where t2.clinicalprotocolid = t1.clinicalprotocolid and t2.clinicalprotocolrevision = t1.clinicalprotocolrevision and t2.cohortid = t1.cohortid and t2.eventdeftype = t1.eventdeftype and t2.eventdeflabel = t1.eventdeflabel and t2.parenteventdefid = t1.parenteventdefid and t1.s_eventdefid = ? and t2.s_eventdefid != t1.s_eventdefid", (Object[])new String[]{s_sampletypeid, s_eventdefid})) != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    filter.put("specimendefid", ds.getString(i, "specimendefid", ""));
                    filter.put("eventdefid", ds.getString(i, "s_eventdefid"));
                    DataSet _datads = sampleds.getFilteredDataSet(filter);
                    if (_datads.size() <= quantityCount) continue;
                    sampleds.setString(sampleds.findRow(filter), "receivedflag", "Y");
                    datads = _datads;
                    isSampleReceived = true;
                    break;
                }
            }
        }
        String rowid = StringUtil.replaceAll(UUID.randomUUID().toString(), "-", "");
        StringBuilder sb = new StringBuilder();
        if (isSampleReceived || isChildSample && doesChildSampleExists) {
            sb.append("<tr datarow=Y style='background-color:#efefef;' id=\"").append(rowid).append("\"");
            if (isSampleReceived) {
                sb.append(" class='receivedsamplerow'");
            }
        } else {
            sb.append("<tr datarow=Y id=\"").append(rowid).append("\"");
        }
        if (isChildSample) {
            sb.append(" childsamplerow=Y>");
        } else {
            sb.append(" childsamplerow=N>");
        }
        if (isTimepoint) {
            sb.append("<td class='maintform_field'>&nbsp;</td>");
        }
        sb.append("<td class='maintform_field'>&nbsp;</td>");
        sb.append("<td class='maintform_field' align=center style='padding-top:10px;position:relative;'>");
        if (!isChildSample) {
            sb.append("<input class='sampleselector' type='checkbox' ").append(isSampleReceived ? "disabled" : "").append(">");
        } else {
            if (isLastRow || quantityCount == -1) {
                sb.append("<img src='WEB-OPAL/images/tree1.gif' style='position:absolute;top:0;left:0;height:100%;width:100%;' class='treenode'>");
            } else {
                sb.append("<img src='WEB-OPAL/images/tree2.gif' style='position:absolute;top:0;left:0;height:100%;width:100%;' class='treenode'>");
            }
            sb.append("<input type='hidden' datafield=Y columnid='parentsampleid' value='").append(parentsampleid).append("'>");
            sb.append("<input type='hidden' datafield=Y columnid='childsampleplanid' value='").append(childsampleplanid).append("'>");
            sb.append("<input type='hidden' datafield=Y columnid='childsampleplanversionid' value='").append(childsampleplanversionid).append("'>");
            sb.append("<input type='hidden' datafield=Y columnid='childsampleplanitemid' value='").append(childsampleplanitemid).append("'>");
        }
        sb.append("<input type='hidden' datafield=Y columnid='studyid' value='").append(requestParam.getProperty(PARAM_STUDYID)).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='clinicalflag' value='Y'>");
        sb.append("<input type='hidden' datafield=Y columnid='studysiteid' value='").append(requestParam.getProperty(PARAM_STUDYSITEID)).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='cohortid' value='").append(requestParam.getProperty(PARAM_COHORTID)).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='subjectid' value='").append(subjectid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='eventdefid' value='").append(s_eventdefid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='neweventinstance' value='").append(neweventinstance ? "Y" : "N").append("'>");
        if (!this.isStudyProtocolDriven(requestParam.getProperty(PARAM_STUDYID))) {
            sb.append("<input type='hidden' datafield=Y columnid='clinicalevent' value='").append("").append("'>");
        }
        sb.append("<input type='hidden' datafield=Y columnid='sampletypeid' value='").append(s_sampletypeid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='specimendefid' value='").append(s_specimendefid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='specimendefid' value='").append(s_specimendefid).append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='ischildsample' value='").append(isChildSample ? "Y" : "N").append("'>");
        sb.append("<input type='hidden' datafield=Y columnid='parentrowuuid' value='").append(parentrowuuid).append("'>");
        sb.append("</td>");
        if (!isTimepoint) {
            sb.append("<td class='maintform_field' colspan='2'>");
            if (isChildSample) {
                boolean markParentConsumed = this.isMarkParentConsumed(childsampleplanid, childsampleplanversionid);
                sb.append("<table cellpadding=2 cellspacing=0 border=0><tr>");
                sb.append("<td><input class='sampleselector' type='checkbox' ").append(doesChildSampleExists ? "disabled" : "").append("></td>");
                if (doesChildSampleExists) {
                    sb.append("<td>").append(this.getTranslationProcessor().translate("Received"));
                } else {
                    sb.append("<td>").append(this.getTranslationProcessor().translate("Not Received"));
                }
                if (markParentConsumed) {
                    sb.append("<div style='font-style:italic;color:#888;'>(").append(this.getTranslationProcessor().translate("Parent will be marked Consumed")).append(")</div>");
                }
                sb.append("</td></tr></table>");
            } else {
                sb.append(isSampleReceived ? this.getTranslationProcessor().translate("Received") : this.getTranslationProcessor().translate("Not Received"));
            }
            sb.append("</td>");
        } else {
            sb.append("<td class='maintform_field'>");
            if (isChildSample) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0><tr>");
                sb.append("<td><input class='sampleselector' type='checkbox' ").append(doesChildSampleExists ? "disabled" : "").append("></td>");
                if (doesChildSampleExists) {
                    sb.append("<td>").append(this.getTranslationProcessor().translate("Received")).append("</td>");
                } else {
                    sb.append("<td>").append(this.getTranslationProcessor().translate("Not Received")).append("</td>");
                }
                sb.append("</tr></table>");
            } else {
                sb.append(isSampleReceived ? this.getTranslationProcessor().translate("Received") : this.getTranslationProcessor().translate("Not Received"));
            }
            sb.append("</td>");
        }
        if (columnCollection != null) {
            String uuid = StringUtil.replaceAll(UUID.randomUUID().toString(), "-", "");
            for (int col = 0; col < columnCollection.size(); ++col) {
                String columnLength;
                String value;
                String columnid;
                String table;
                PropertyList column = columnCollection.getPropertyList(col);
                if ("hidden".equals(column.getProperty(PARAM_MODE))) continue;
                String sdcid = column.getProperty("sdcid");
                if ("CollectionPlan".equals(sdcid)) {
                    String columnid2 = column.getProperty("specimencolumnid");
                    if (!OpalUtil.isNotEmpty(columnid2)) continue;
                    String validation = column.getProperty("validation");
                    if ("s_sampletypeid".equals(columnid2)) {
                        sb.append("<td class='maintform_field'>");
                        if (OpalUtil.isNotEmpty(s_sampletypeid)) {
                            sb.append("<input datafield=Y columnid='sampletypeid' value='").append(s_sampletypeid).append("' readonly style='border:0;background:transparent;color:#000;' tabindex='-1' id='").append(UUID.randomUUID().toString()).append("'>");
                        } else {
                            String sampletypefieldid = "field_" + uuid;
                            sb.append("<input name='").append(sampletypefieldid).append("' id='").append(sampletypefieldid).append("'");
                            if (validation.length() > 0) {
                                sb.append(validation.contains("Mandatory") ? " class='mandatoryfield'" : "");
                                sb.append(" validation=\"").append(validation).append("\" ttl=\"").append(column.getProperty("title")).append("\"");
                            }
                            sb.append(" uuid=\"").append(uuid).append("\"");
                            sb.append(" edit=\"lookup\" type=\"text\"");
                            sb.append(" datafield=Y columnid='sampletypeid'");
                            sb.append(" onkeyup=\"showSuggestion()\"");
                            sb.append(" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear('").append(sampletypefieldid).append("');}\"");
                            sb.append(" onchange=\"handleSampleTypeChange(this)\" readonly style='border:1px solid limegreen;'>");
                            sb.append("<script>var oLUPD_").append(sampletypefieldid).append(" = {\"selectortype\": \"\", \"sdcid\": \"SampleType\", restrictivewhere: \"templateflag != 'Y'\"};</script>");
                        }
                        sb.append("</td>");
                        continue;
                    }
                    if ("specimentype".equals(columnid2)) {
                        sb.append("<td class='maintform_field'>");
                        if (OpalUtil.isNotEmpty(specimentype)) {
                            sb.append("<input datafield='Y' columnid='specimentype'");
                            if (validation.length() > 0) {
                                sb.append(validation.contains("Mandatory") ? " class='mandatoryfield'" : "");
                            }
                            sb.append(" value=\"").append(specimentype).append("\" readonly style='border:0;background:transparent;color:#000;' tabindex='-1' id='").append(UUID.randomUUID().toString()).append("'>");
                        } else {
                            sb.append("<select style='height:22px;' datafield=Y columnid='specimentype'");
                            if (validation.length() > 0) {
                                sb.append(validation.contains("Mandatory") ? " class='mandatoryfield'" : "");
                                sb.append(" validation=\"").append(validation).append("\" ttl=\"").append(column.getProperty("title")).append("\"");
                            }
                            sb.append(" style='width:100%' uuid='").append(uuid).append("' onchange=\"selectRow()\" id='").append(UUID.randomUUID().toString()).append("'>");
                            sb.append("<option></option>");
                            sb.append("</select>");
                        }
                        sb.append("</td>");
                        continue;
                    }
                    if ("volume".equals(columnid2)) {
                        if (isSampleReceived || isChildSample && doesChildSampleExists) {
                            if (datads != null) {
                                volume = datads.getValue(quantityCount, "initialvolume", datads.getValue(quantityCount, "initialmass", ""));
                                volumeunits = datads.getValue(quantityCount, "initialvolumeunits", datads.getValue(quantityCount, "initialmassunits", ""));
                            }
                            sb.append("<td class='maintform_field'>");
                            sb.append("<input datafield='Y' id='").append(UUID.randomUUID().toString()).append("'");
                            sb.append(" readonly style='border:0;background:transparent;color:#000;width:40px;text-align:right;' tabindex='-1' value='").append(volume).append("'>");
                            sb.append("</td>");
                            sb.append("<td class='maintform_field'>&nbsp;<input datafield='Y' id='").append(UUID.randomUUID().toString()).append("'");
                            sb.append(" readonly style='border:0;background:transparent;color:#000;width:50px;' tabindex='-1' value='").append(volumeunits).append("'>");
                            sb.append("</td>");
                            continue;
                        }
                        sb.append("<td class='maintform_field' style='vertical-align:top'>");
                        sb.append("<input datafield=Y columnid='qtycurrent' style='width:40px;text-align:right;padding-right:2px;height:18px;border:1px solid #cdcdcd;'");
                        if (validation.length() > 0) {
                            sb.append(validation.contains("Mandatory") ? " class='mandatoryfield'" : "");
                            sb.append(" validation=\"").append(validation).append("\" ttl=\"").append(column.getProperty("title")).append("\"");
                        }
                        sb.append(" value=\"").append(volume).append("\" onchange=\"selectRow()\" id='").append(UUID.randomUUID().toString()).append("'></td>");
                        sb.append("<td class='maintform_field' style='vertical-align:top'>");
                        sb.append("<select datafield=Y columnid='qtyunits'");
                        if (validation.length() > 0 && validation.contains("Mandatory")) {
                            sb.append(" class='mandatoryfield' validation='Mandatory;' ttl=\"").append(this.getTranslationProcessor().translate("Unit")).append("\"");
                        }
                        sb.append(" style='height:22px;border:1px solid #cdcdcd;' id='").append(UUID.randomUUID().toString()).append("'>");
                        sb.append("<option></option>");
                        for (String unitid : unitsList) {
                            sb.append("<option ").append(volumeunits.equals(unitid) ? "selected" : "").append(">").append(unitid).append("</option>");
                        }
                        sb.append("</select></td>");
                        continue;
                    }
                    sb.append("<td class='maintform_field'>");
                    if (isChildSample) {
                        if ("processinstructions".equals(columnid2) || "labelmethodid".equals(columnid2) || "labelmethodversionid".equals(columnid2)) {
                            if ("processinstructions".equals(columnid2)) {
                                columnid2 = "processinstruction";
                            }
                            sb.append(OpalUtil.getColumnValue(this.getQueryProcessor(), "s_childsampleplanitem", columnid2, "s_childsampleplanitemid = ?", new String[]{childsampleplanitemid}));
                        } else {
                            sb.append("<span style='color:#ccc'>").append(this.getTranslationProcessor().translate("This value is not available to be displayed for child samples")).append("</span>");
                        }
                    } else {
                        sb.append(this.getSpecimenColumnValue(s_eventdefid, s_sampletypeid, s_specimendefid, columnid2));
                    }
                    sb.append("</td>");
                    continue;
                }
                String string = "Sample".equals(sdcid) ? "s_sample" : ("LV_SampleFamily".equals(sdcid) ? "s_samplefamily" : (table = "TrackItemSDC".equals(sdcid) ? "trackitem" : ""));
                String string2 = "Sample".equals(sdcid) ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) ? column.getProperty("trackitemcolumn") : ""));
                if (!OpalUtil.isNotEmpty(columnid)) continue;
                String validation = column.getProperty("validation");
                sb.append("<td class='maintform_field'>");
                if (isSampleReceived || isChildSample && doesChildSampleExists) {
                    sb.append(datads != null ? datads.getValue(quantityCount, columnid, "") : "");
                    continue;
                }
                String attributekey = table + "." + columnid;
                String string3 = value = attributesMap != null && attributesMap.containsKey(attributekey) ? attributesMap.get(attributekey) : "";
                if (OpalUtil.isEmpty(value) && (value = column.getProperty("default", "")).startsWith("[collectionplan.") && value.endsWith("]")) {
                    String specimencolumn = value.substring(16, value.length() - 1);
                    value = this.getSpecimenColumnValue(s_eventdefid, s_sampletypeid, s_specimendefid, specimencolumn);
                }
                String fieldid = UUID.randomUUID().toString();
                String mode = column.getProperty(PARAM_MODE);
                String attribute = "datafield=Y columnid='" + attributekey + "' value=\"" + value + "\" onchange=\"selectRow()\"";
                if ("readonly".equals(mode)) {
                    attribute = attribute + " readonly";
                    sb.append(ColumnUtil.getInputHtml(column, fieldid, attribute));
                } else if ("input".equals(mode)) {
                    columnLength = this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "columnlength");
                    if (validation.length() > 0) {
                        attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                    }
                    attribute = attribute + " maxlength='" + columnLength + "'";
                    sb.append(ColumnUtil.getInputHtml(column, fieldid, attribute));
                } else if ("lookup".equals(mode)) {
                    columnLength = this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "columnlength");
                    if (validation.length() > 0) {
                        attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                    }
                    attribute = attribute + " maxlength='" + columnLength + "'";
                    sb.append(ColumnUtil.getLookupHtml(column, fieldid, attribute));
                } else if ("datelookup".equals(mode)) {
                    String dateFormat;
                    if (validation.length() > 0) {
                        attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                    }
                    if ((dateFormat = column.getProperty("format", "").trim()).length() == 0) {
                        try {
                            dateFormat = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom").getProperty("defaultdateformat", "");
                            column.setProperty("format", dateFormat);
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                    }
                    sb.append(ColumnUtil.getDateLookupHtml(column, fieldid, attribute));
                } else if ("dropdownlist".equals(mode)) {
                    if (validation.length() > 0) {
                        attribute = attribute + " validation=\"" + validation + "\" ttl=\"" + column.getProperty("title") + "\"";
                    }
                    sb.append(ColumnUtil.getDropDownListHtml(column, fieldid, attribute, this.getQueryProcessor(), this.getSDIProcessor()));
                }
                sb.append("</td>");
            }
            if ("Y".equals(requestParam.getProperty(PARAM_SAMPLEALIASSHOW))) {
                String samplealiastype = requestParam.getProperty(PARAM_SAMPLEALIASTYPE);
                if (isSampleReceived || isChildSample && doesChildSampleExists) {
                    sb.append("<td class='maintform_field' colspan='2'>&nbsp;</td>");
                } else {
                    sb.append("<td class='maintform_field'>");
                    sb.append("<input datafield=Y columnid='s_sample.aliasid' style='width:120px;' value=\"\" onchange=\"selectRow()\" id='").append(UUID.randomUUID().toString()).append("' style='vertical-align:top'>");
                    sb.append("</td>");
                    if (OpalUtil.isEmpty(samplealiastype)) {
                        sb.append("<td class='maintform_field'>");
                        sb.append("<select datafield=Y columnid='s_sample.aliastype' style='height:22px;' id='").append(UUID.randomUUID().toString()).append("'>");
                        sb.append("<option></option>");
                        for (int i = 0; i < sampleAliasTypeDS.size(); ++i) {
                            String refvalue = sampleAliasTypeDS.getString(i, "refvalueid");
                            String refdisplayvalue = sampleAliasTypeDS.getString(i, "refdisplayvalueid", refvalue);
                            sb.append("<option value=\"").append(refvalue).append("\">").append(refdisplayvalue).append("</option>");
                        }
                        sb.append("</select>");
                        sb.append("</td>");
                    } else {
                        sb.append("<td class='maintform_field'>");
                        sb.append("<input type=hidden datafield=Y columnid='s_sample.aliastype' value=\"").append(samplealiastype).append("\">");
                        sb.append("</td>");
                    }
                }
            }
            sb.append("<td class='maintform_field'>");
            if (isLastRow) {
                sb.append("<img src=\"WEB-OPAL/elements/advancedtoolbar/images/addtests.gif\" style='cursor:pointer' class=\"addNewEventRowImg\"");
                sb.append(" istimepoint=\"").append(isTimepoint ? "Y" : "N").append("\"");
                sb.append(" ischildsample=\"").append(isChildSample ? "Y" : "N").append("\"");
                sb.append(" parentrowuuid=\"").append(parentrowuuid).append("\"");
                sb.append(" parentsampleid=\"").append(parentsampleid).append("\">");
            } else {
                sb.append("&nbsp;");
            }
            sb.append("</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    private boolean isMarkParentConsumed(String childsampleplanid, String childsampleplanversionid) {
        String key = "markparentconsumed-" + childsampleplanid + ":" + childsampleplanversionid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_childsampleplan", "markparentconsumedflag", "s_childsampleplanid=? and s_childsampleplanversionid=?", new String[]{childsampleplanid, childsampleplanversionid}));
        }
        return "Y".equals(this.localCache.get(key));
    }

    private String getSpecimenColumnValue(String eventdefid, String sampletypeid, String specimendefid, String columnid) {
        DataSet ds;
        String key = "specimen_" + eventdefid + sampletypeid + specimendefid;
        if (this.specimenCache == null) {
            this.specimenCache = new HashMap<String, DataSet>();
        }
        if (!this.specimenCache.containsKey(key)) {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select * from s_eventdefstspecimendef where s_eventdefid = ? and s_sampletypeid = ? and s_specimendefid = ?", (Object[])new String[]{eventdefid, sampletypeid, specimendefid});
            if (ds != null && ds.size() > 0) {
                this.specimenCache.put(key, ds);
            } else {
                this.specimenCache.put(key, null);
            }
        }
        return (ds = this.specimenCache.get(key)) != null && ds.size() > 0 ? ds.getValue(0, columnid) : "";
    }

    private String getTitleRowHTML(PropertyListCollection columnCollection, boolean collectionsExist, boolean isTimepoint, boolean isProtocolDriven, boolean showSampleAlias) {
        if (this.titleRowRendered) {
            return "";
        }
        this.titleRowRendered = true;
        int additionalColumnCount = 0;
        int colCount = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("<thead id='sampleexpectedtableheader'>");
        sb.append("<tr class='columnheader'>");
        sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
        if (isTimepoint) {
            sb.append("<th class='maintform_fieldtitle' style='width:20px;text-align:center;'>&nbsp;</td>");
            sb.append("<th class='maintform_fieldtitle' style='width:20px;text-align:center;'>&nbsp;</td>");
            sb.append("<th class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate("Status")).append("</td>");
        } else {
            sb.append("<th class='maintform_fieldtitle' style='width:20px;text-align:center;'>&nbsp;</td>");
            sb.append("<th class='maintform_fieldtitle' colspan='2'>").append(this.getTranslationProcessor().translate("Status")).append("</td>");
        }
        if (!isProtocolDriven) {
            sb.append("<th class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate("Sample Type")).append("</td>");
            sb.append("<th class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate("Container")).append("</td>");
            sb.append("<th class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate("Amount")).append("</td>");
            sb.append("<th class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate("Unit")).append("</td>");
        }
        for (int col = 0; col < columnCollection.size(); ++col) {
            String columnid;
            PropertyList column = columnCollection.getPropertyList(col);
            if ("hidden".equals(column.getProperty(PARAM_MODE))) continue;
            String sdcid = column.getProperty("sdcid");
            if ("CollectionPlan".equals(sdcid) && isProtocolDriven) {
                columnid = column.getProperty("specimencolumnid");
                if (!OpalUtil.isNotEmpty(columnid)) continue;
                if ("Amount".equals(column.getProperty("title"))) {
                    sb.append("<th class='maintform_fieldtitle' style='text-align:center' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(");\">");
                    sb.append(this.getTranslationProcessor().translate(column.getProperty("title", "&nbsp;")));
                    sb.append("</th>");
                    sb.append("<th class='maintform_fieldtitle' style='text-align:center' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(");\">");
                    sb.append(this.getTranslationProcessor().translate("Unit"));
                    sb.append("</th>");
                } else {
                    sb.append("<th class='maintform_fieldtitle' style='text-align:center' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(");\">");
                    sb.append(this.getTranslationProcessor().translate(column.getProperty("title", "&nbsp;")));
                    sb.append("</th>");
                }
                ++additionalColumnCount;
                continue;
            }
            String string = "Sample".equals(sdcid) ? column.getProperty("samplecolumn") : ("LV_SampleFamily".equals(sdcid) ? column.getProperty("familycolumn") : (columnid = "TrackItemSDC".equals(sdcid) ? column.getProperty("trackitemcolumn") : ""));
            if (!OpalUtil.isNotEmpty(columnid)) continue;
            sb.append("<th class='maintform_fieldtitle' style='text-align:center' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(");\">");
            sb.append(this.getTranslationProcessor().translate(column.getProperty("title", "&nbsp;")));
            sb.append("</th>");
            ++additionalColumnCount;
        }
        if (showSampleAlias) {
            sb.append("<th class='maintform_fieldtitle' onclick=\"mainthandler1.selectColumn(").append(colCount++).append(");\">");
            sb.append(this.getTranslationProcessor().translate("Sample Alias"));
            sb.append("</th>");
            sb.append("<th class='maintform_fieldtitle' onclick=\"mainthandler1.selectColumn(").append(colCount).append(");\">");
            sb.append(this.getTranslationProcessor().translate("Alias Type"));
            sb.append("</th>");
        }
        sb.append("<th class='maintform_fieldtitle' style='width:20px'>&nbsp;</td>");
        sb.append("</tr>");
        if (!collectionsExist) {
            sb.append("<tr id='").append(UUID.randomUUID().toString()).append("' headerrow='Y' timepoint='Y'>");
            sb.append("<td class='maintform_field'>&nbsp;</td>");
            sb.append("<td class='maintform_field'>&nbsp;</td>");
            sb.append("<td class='maintform_field' colspan='").append(4 + additionalColumnCount).append("'>");
            sb.append("No collection defined in this event");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</thead>");
        return sb.toString();
    }

    private boolean isStudyProtocolDriven(String studyid) {
        if (!this.protocolMap.containsKey(studyid)) {
            this.protocolMap.put(studyid, Study.isProtocolDriven(this.getQueryProcessor(), studyid) ? "Y" : "N");
        }
        return "Y".equals(this.protocolMap.get(studyid));
    }

    private String getProtocolEventDefLabel(String eventdefid) {
        String key = "eventdeflabel" + eventdefid;
        if (!this.localCache.containsKey(key)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select (select p.eventdeflabel from s_eventdef p where p.s_eventdefid = s_eventdef.parenteventdefid) parentlabel, s_eventdef.eventdeflabel from s_eventdef where s_eventdef.s_eventdefid = ?", (Object[])new String[]{eventdefid});
            if (ds != null && ds.size() > 0) {
                String parentlabel = ds.getString(0, "parentlabel", "");
                String eventdeflabel = HttpUtil.htmlEncode(ds.getString(0, "eventdeflabel", ""));
                this.localCache.put(key, (OpalUtil.isNotEmpty(parentlabel) ? parentlabel + " > " : "") + eventdeflabel);
            } else {
                this.localCache.put(key, "");
            }
        }
        return this.localCache.containsKey(key) ? this.localCache.get(key) : eventdefid;
    }

    private int getSampleReceiveCount(String studyid, String subjectid, String eventdefid, String sampletypeid, String specimendefid) {
        return this.getQueryProcessor().getPreparedSqlDataSet("select count(s_samplefamilyid) samplecount from s_samplefamily where sstudyid = ? and subjectid = ? and eventdefid = ? and sampletypeid = ? and specimendefid = ?", (Object[])new String[]{studyid, subjectid, eventdefid, sampletypeid, specimendefid}).getInt(0, "samplecount", 0);
    }

    public String getPreparedValue(String sql, String[] keys, String columnid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])keys);
        if (ds != null && ds.size() > 0) {
            return ds.getValue(0, columnid);
        }
        return "";
    }
}

