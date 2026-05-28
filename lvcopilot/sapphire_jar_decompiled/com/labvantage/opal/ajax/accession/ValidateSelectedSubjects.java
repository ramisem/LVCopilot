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
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ValidateSelectedSubjects
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid");
        String participantid = ajaxResponse.getRequestParameter("participantid");
        String subjectid = ajaxResponse.getRequestParameter("subjectid");
        String accessionmode = ajaxResponse.getRequestParameter("accessionmode", "Multiple");
        String selectionmode = ajaxResponse.getRequestParameter("selectionmode");
        String subjectstudies = "";
        String subjectsites = "";
        String subjectstudysitedesc = "";
        String subjectcohort = "";
        String message = "";
        StringBuilder html = new StringBuilder();
        JSONObject validationObject = null;
        JSONObject validationDateFormatObject = null;
        if (OpalUtil.isEmpty(studyid)) {
            if (OpalUtil.isNotEmpty(participantid)) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct sstudyid, studysiteid, (select studysitedesc from s_studysite where s_studysiteid = s_participant.studysiteid) studysitedesc, cpcohortid from s_participant where s_participantid in (" + safeSQL.addIn(participantid, ";") + ") order by sstudyid", safeSQL.getValues());
                if (ds.size() > 1) {
                    message = this.getTranslationProcessor().translate("Selected participants must belong to same Study, Site and Cohort");
                } else {
                    if ("Single".equals(selectionmode) && validationObject != null) {
                        HashMap<Object, String> columnTypeMap = new HashMap<Object, String>();
                        StringBuilder sql = new StringBuilder();
                        Iterator iterator = validationObject.keys();
                        while (iterator.hasNext()) {
                            String columnid = (String)iterator.next();
                            String datatype = this.getSDCProcessor().getSDCColumnProperty("LV_Subject", columnid, "datatype");
                            if (!OpalUtil.isNotEmpty(datatype)) continue;
                            columnTypeMap.put(columnid, datatype);
                        }
                        if (columnTypeMap.size() > 0) {
                            sql.append("select ");
                            for (String columnid : columnTypeMap.keySet()) {
                                sql.append("s_subject.").append(columnid).append(", ");
                            }
                            sql.append(" s_subject.s_subjectid from s_subject, s_participant where s_subject.s_subjectid = s_participant.subjectid and s_participant.s_participantid = ?");
                            DataSet validateds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{participantid});
                            if (validateds != null && validateds.size() > 0) {
                                for (String columnid : columnTypeMap.keySet()) {
                                    try {
                                        String uservalue;
                                        String databasevalue;
                                        String datatype = (String)columnTypeMap.get(columnid);
                                        if (datatype.equals("D") && validationDateFormatObject != null && validationDateFormatObject.has(columnid)) {
                                            String format = validationDateFormatObject.getString(columnid);
                                            validateds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), format));
                                        }
                                        if ((databasevalue = validateds.getValue(0, columnid)).equals(uservalue = validationObject.getString(columnid))) continue;
                                        message = "Selected subject's information does not match the entered validation information";
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    subjectstudies = ds.getString(0, "sstudyid");
                    subjectsites = ds.getString(0, "studysiteid");
                    subjectstudysitedesc = ds.getString(0, "studysitedesc");
                    subjectcohort = ds.getString(0, "cpcohortid");
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct sstudyid, studysiteid, (select studysitedesc from s_studysite where s_studysiteid = s_participant.studysiteid) studysitedesc, cpcohortid from s_participant where subjectid in (" + safeSQL.addIn(subjectid, ";") + ") order by sstudyid, studysitedesc", safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    html.append("<link rel=\"stylesheet\" href=\"WEB-OPAL/stylesheets/pure.css\">");
                    html.append("<table cellpadding='4' cellspacing=0 border=0 class=\"pure-table pure-table-striped\" style='width:100%'>");
                    html.append("<thead>");
                    html.append("<th style='width:20px'>&nbsp;</th>");
                    html.append("<th>").append(this.getTranslationProcessor().translate("Study")).append("</th>");
                    html.append("<th>").append(this.getTranslationProcessor().translate("Site")).append("</th>");
                    html.append("<th>").append(this.getTranslationProcessor().translate("Cohort")).append("</th>");
                    html.append("</thead>");
                    html.append("<tbody>");
                    for (int i = 0; i < ds.size(); ++i) {
                        String study = ds.getString(i, "sstudyid");
                        html.append("<tr>");
                        html.append("<td><input type='radio' name='enrollstudyid' value=\"").append(study).append("\"");
                        html.append(" studysiteid=\"").append(ds.getString(i, "studysiteid")).append("\"");
                        html.append(" studysitedesc=\"").append(ds.getString(i, "studysitedesc")).append("\"");
                        html.append(" cohort=\"").append(ds.getString(i, "cpcohortid", "")).append("\"></td>");
                        html.append("<td>").append(study).append("</td>");
                        html.append("<td>").append(ds.getString(i, "studysitedesc")).append("</td>");
                        html.append("<td>").append(ds.getString(i, "cpcohortid", "")).append("</td>");
                        html.append("</tr>");
                    }
                    html.append("<tr>");
                    html.append("<td><input type='radio' name='enrollstudyid' value=\"\"></td>");
                    html.append("<td colspan=3>").append(this.getTranslationProcessor().translate("New Enrollment")).append("</td>");
                    html.append("</tr>");
                    html.append("</tbody></table>");
                }
            }
        } else {
            String studysiteid = ajaxResponse.getRequestParameter("studysiteid");
            String studysitedesc = ajaxResponse.getRequestParameter("studysitedesc");
            String cohort = ajaxResponse.getRequestParameter("cohort");
            if (OpalUtil.isNotEmpty(participantid)) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct sstudyid, studysiteid, (select studysitedesc from s_studysite where s_studysiteid = s_participant.studysiteid) studysitedesc, cpcohortid from s_participant where s_participantid in (" + safeSQL.addIn(participantid, ";") + ") order by sstudyid", safeSQL.getValues());
                if (ds.size() > 1) {
                    message = this.getTranslationProcessor().translate("Selected participant(s) must belong to same Study, Site and Cohort");
                } else {
                    subjectstudies = ds.getString(0, "sstudyid");
                    if (!subjectstudies.equals(studyid)) {
                        message = this.getTranslationProcessor().translate("Selected participant(s) does not belong to selected Study") + " (" + studyid + ")";
                    }
                    if (!(subjectsites = ds.getString(0, "studysiteid")).equals(studysiteid)) {
                        message = message + "<br>" + this.getTranslationProcessor().translate("Selected participant(s) does not belong to selected Site") + " (" + studysitedesc + ")";
                    }
                    if (OpalUtil.isNotEmpty(cohort) && !(subjectcohort = ds.getString(0, "cpcohortid")).equals(cohort)) {
                        message = message + (message.length() > 0 ? "<br>" : "" + this.getTranslationProcessor().translate("Selected participant(s) does not belong to selected Cohort") + " (" + cohort + ")");
                    }
                }
            } else {
                subjectstudies = studyid;
                subjectsites = studysiteid;
                subjectstudysitedesc = studysitedesc;
                subjectcohort = cohort;
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", html.toString());
        ajaxResponse.addCallbackArgument("studyid", subjectstudies);
        ajaxResponse.addCallbackArgument("studysiteid", subjectsites);
        ajaxResponse.addCallbackArgument("studysitedesc", subjectstudysitedesc);
        ajaxResponse.addCallbackArgument("cohort", subjectcohort);
        ajaxResponse.print();
    }
}

