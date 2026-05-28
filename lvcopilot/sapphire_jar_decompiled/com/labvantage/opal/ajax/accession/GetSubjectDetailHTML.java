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
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetSubjectDetailHTML
extends BaseAjaxRequest {
    public static final String ACCESSIONMODE_AUTOENROLLMENT = "Auto Enrollment";
    public static final String ACCESSIONMODE_EXISTINGPARTICIPANT = "Existing Participant";
    private Map<String, String> studymap = new HashMap<String, String>();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean policy_participantStudySubjectOnly = false;
        PropertyList bioBankingPolicy = null;
        try {
            bioBankingPolicy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
            policy_participantStudySubjectOnly = "Study-Subject".equals(bioBankingPolicy.getProperty("participantcreationrule"));
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuilder sb = new StringBuilder();
        String accessionmode = ajaxResponse.getRequestParameter("accessionmode", ACCESSIONMODE_AUTOENROLLMENT);
        String participantid = ajaxResponse.getRequestParameter("participantid");
        String subjectid = ajaxResponse.getRequestParameter("subjectid");
        String columncollection = ajaxResponse.getRequestParameter("columns");
        boolean singleSelectionMode = "Y".equals(ajaxResponse.getRequestParameter("singleselectionmode", "N"));
        String studyid = ajaxResponse.getRequestParameter("studyid", "");
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid", "");
        String cohort = ajaxResponse.getRequestParameter("cohort", "");
        boolean renderheader = "Y".equals(ajaxResponse.getRequestParameter("renderheader", "N"));
        int participantColumnCount = 0;
        String studydisplayformat = ajaxResponse.getRequestParameter("studydisplayformat", "[studydesc] ([s_studyid])");
        try {
            JSONObject object = new JSONObject(columncollection);
            int columncount = 0;
            while (object.has(String.valueOf(columncount))) {
                ++columncount;
            }
            StringBuilder sql = new StringBuilder();
            JSONArray searchData = new JSONArray(ajaxResponse.getRequestParameter("searchdata"));
            HashMap<String, String> searchMap = new HashMap<String, String>();
            for (int i = 0; i < searchData.length(); ++i) {
                JSONObject column = searchData.getJSONObject(i);
                String columnid = column.getString("columnid");
                if (!columnid.startsWith("participant.")) continue;
                searchMap.put(columnid.substring(columnid.indexOf(".") + 1), column.getString("value"));
            }
            String validationdata = ajaxResponse.getRequestParameter("validationdata");
            HashMap<String, String> validationMap = new HashMap<String, String>();
            if (OpalUtil.isNotEmpty(validationdata)) {
                try {
                    JSONObject jsonObject = new JSONObject(validationdata);
                    Iterator keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String columnid = (String)keys.next();
                        String columnvalue = jsonObject.getString(columnid);
                        validationMap.put(columnid, columnvalue);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String validationdatadateformat = ajaxResponse.getRequestParameter("validationdatadateformat");
            HashMap<String, String> validationDateFormatMap = new HashMap<String, String>();
            if (OpalUtil.isNotEmpty(validationdatadateformat)) {
                try {
                    JSONObject jsonObject = new JSONObject(validationdatadateformat);
                    Iterator keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String columnid = (String)keys.next();
                        String columnvalue = jsonObject.getString(columnid);
                        validationDateFormatMap.put("v_" + columnid, columnvalue);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<String> subjectcolumns = new ArrayList<String>();
            ArrayList<String> participantcolumns = new ArrayList<String>();
            if (renderheader) {
                StringBuilder sb2 = new StringBuilder();
                sb.append("<table cellpadding=\"4px\" cellspacing=\"0\" border=\"0\" id=\"resulttable\" class=\"maintform_table\">");
                sb.append("<tr>");
                sb.append("<th style=\"border-top-left-radius: 4px;\" class=\"maintform_fieldtitle\">&nbsp;</th>");
                for (int i = 0; i < columncount; ++i) {
                    String mode;
                    JSONObject column = object.getJSONObject(String.valueOf(i));
                    String subjectcolumnid = column.has("subjectcolumnid") ? column.getString("subjectcolumnid") : "";
                    String participantcolumnid = column.has("participantcolumnid") ? column.getString("participantcolumnid") : "";
                    String string = mode = column.has("mode") ? column.getString("mode") : "Display Value";
                    if ("Hidden".equals(mode)) continue;
                    if (OpalUtil.isNotEmpty(subjectcolumnid)) {
                        if (!"alias".equals(subjectcolumnid)) {
                            subjectcolumns.add(subjectcolumnid);
                        }
                        sb.append("<th class=\"maintform_fieldtitle\">");
                        sb.append(this.getTranslationProcessor().translate(column.getString("title")));
                        sb.append("</th>");
                        continue;
                    }
                    if (!OpalUtil.isNotEmpty(participantcolumnid)) continue;
                    ++participantColumnCount;
                    if (!"alias".equals(participantcolumnid)) {
                        participantcolumns.add(participantcolumnid);
                    }
                    sb2.append("<th class=\"maintform_fieldtitle\">");
                    sb2.append(this.getTranslationProcessor().translate(column.getString("title")));
                    sb2.append("</th>");
                }
                sb.append(sb2.toString());
                sb.append("</tr>");
            } else {
                for (int i = 0; i < columncount; ++i) {
                    String mode;
                    JSONObject column = object.getJSONObject(String.valueOf(i));
                    String subjectcolumnid = column.has("subjectcolumnid") ? column.getString("subjectcolumnid") : "";
                    String participantcolumnid = column.has("participantcolumnid") ? column.getString("participantcolumnid") : "";
                    String string = mode = column.has("mode") ? column.getString("mode") : "Display Value";
                    if ("Hidden".equals(mode)) continue;
                    if (OpalUtil.isNotEmpty(subjectcolumnid)) {
                        if ("alias".equals(subjectcolumnid)) continue;
                        subjectcolumns.add(subjectcolumnid);
                        continue;
                    }
                    if (!OpalUtil.isNotEmpty(participantcolumnid)) continue;
                    ++participantColumnCount;
                    if ("alias".equals(participantcolumnid)) continue;
                    participantcolumns.add(participantcolumnid);
                }
            }
            StringBuilder sqlSelect = new StringBuilder();
            sqlSelect.append(OpalUtil.toDelimitedString(subjectcolumns, ","));
            if (validationMap.size() > 0) {
                if (sqlSelect.length() != 0) {
                    sqlSelect.append(", ");
                }
                for (String columnid : validationMap.keySet()) {
                    sqlSelect.append("s_subject.").append(columnid).append(" v_").append(columnid).append(", ");
                }
                sqlSelect.append(" s_subject.s_subjectid");
            } else {
                sqlSelect.append(", s_subject.s_subjectid");
            }
            sql.append("select ").append((CharSequence)sqlSelect);
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" from s_subject where s_subject.s_subjectid in (").append(safeSQL.addIn(subjectid, ";")).append(")");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            DataMaskUtil dmu = new DataMaskUtil(this.getQueryProcessor());
            dmu.maskPrimaryDataSet(ds, "LV_Subject", "primary[" + sqlSelect + "]", true);
            if (ds != null) {
                for (int col = 0; col < columncount; ++col) {
                    String dateformat;
                    String mode;
                    JSONObject column = object.getJSONObject(String.valueOf(col));
                    String subjectcolumnid = column.has("subjectcolumnid") ? column.getString("subjectcolumnid") : "";
                    String string = mode = column.has("mode") ? column.getString("mode") : "Display Value";
                    if ("Hidden".equals(mode) || subjectcolumnid.length() <= 0 || 2 != ds.getColumnType(subjectcolumnid)) continue;
                    boolean isTimeZoneInsensitive = "Y".equals(this.getSDCProcessor().getSDCColumnProperty("LV_Subject", subjectcolumnid, "timezoneindependent"));
                    if (isTimeZoneInsensitive) {
                        ds.setTimeZoneInsensitive(subjectcolumnid);
                    }
                    String string2 = dateformat = column.has("dateformat") ? column.getString("dateformat") : "";
                    if (!OpalUtil.isNotEmpty(dateformat)) continue;
                    ds.setDateDisplayFormat(subjectcolumnid, ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), dateformat, !isTimeZoneInsensitive));
                }
                for (String columnid : validationDateFormatMap.keySet()) {
                    ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), (String)validationDateFormatMap.get(columnid)));
                }
                if (participantcolumns.size() == 0) {
                    participantcolumns.add("s_participantid");
                }
                for (int i = 0; i < ds.size(); ++i) {
                    String displayformat;
                    Calendar dbcal;
                    String databasevalue;
                    String _subjectid = ds.getString(i, "s_subjectid");
                    if (ACCESSIONMODE_AUTOENROLLMENT.equals(accessionmode)) {
                        String[] tokens;
                        String title;
                        String displayvalue;
                        String columnValue;
                        String mode;
                        JSONObject column;
                        int col;
                        DataSet pds;
                        sql.setLength(0);
                        StringBuilder selectSql = new StringBuilder();
                        if (policy_participantStudySubjectOnly) {
                            for (String columnid : participantcolumns) {
                                selectSql.append(columnid).append(", ");
                            }
                            selectSql.append(" sstudyid pstudyid, studysiteid pstudysiteid, cpcohortid pcpcohortid");
                            sql.append("select ");
                            sql.append((CharSequence)selectSql);
                            sql.append(" from s_participant where s_participant.subjectid = ? and s_participant.sstudyid = ?");
                            pds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{_subjectid, studyid});
                        } else {
                            for (String columnid : participantcolumns) {
                                selectSql.append(columnid).append(", ");
                            }
                            selectSql.append(" sstudyid pstudyid, studysiteid pstudysiteid, cpcohortid pcpcohortid");
                            sql.append("select ");
                            sql.append((CharSequence)selectSql);
                            sql.append(" from s_participant where s_participant.subjectid = ? and s_participant.sstudyid = ? and s_participant.studysiteid = ?");
                            pds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{_subjectid, studyid, studysiteid});
                        }
                        dmu.maskPrimaryDataSet(pds, "LV_Participant", "primary[" + selectSql + "]", true);
                        boolean validationFailed = false;
                        for (String columnid : validationMap.keySet()) {
                            String validationvalue = (String)validationMap.get(columnid);
                            if (!OpalUtil.isNotEmpty(validationvalue)) continue;
                            databasevalue = ds.getValue(i, "v_" + columnid);
                            if (ds.getColumnType(columnid) == 2) {
                                dbcal = ds.getCalendar(i, columnid);
                                if (dbcal == null) continue;
                                displayformat = (String)validationDateFormatMap.get("v_" + columnid);
                                if (OpalUtil.isNotEmpty(displayformat)) {
                                    DateFormat dateFormat = ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), displayformat);
                                    try {
                                        Date date = dateFormat.parse(validationvalue);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        validationFailed = dbcal.get(1) != cal.get(1) || dbcal.get(2) != cal.get(2) || dbcal.get(5) != cal.get(5);
                                    }
                                    catch (ParseException e) {
                                        validationFailed = !validationvalue.equals(databasevalue);
                                    }
                                    continue;
                                }
                                validationFailed = !validationvalue.equals(databasevalue);
                                continue;
                            }
                            validationFailed = !validationvalue.equals(databasevalue);
                        }
                        if (pds.size() > 0) {
                            for (String searchcolumn : searchMap.keySet()) {
                                if (pds.getValue(0, searchcolumn, "").equals(searchMap.get(searchcolumn))) continue;
                                validationFailed = true;
                            }
                        }
                        if (validationFailed) {
                            sb.append("<tr datarow=\"Y\" style=\"background:#F6CECE\" validationfailed=Y>");
                        } else {
                            sb.append("<tr datarow=\"Y\" validationfailed=N>");
                        }
                        sb.append("<td valign=\"top\" class=\"maintform_field\">");
                        sb.append("<input type='hidden' name='newenrollment' value='").append(pds != null && pds.size() > 0 ? "N" : "Y").append("'>");
                        if (singleSelectionMode) {
                            sb.append("<input type=\"radio\" name=\"radio_subjectid\" value=\"").append(_subjectid).append("\" class=\"searchSubjectID\"").append(ds.size() == 1 ? " checked" : "").append(">");
                        } else {
                            sb.append("<input type=\"checkbox\" value=\"").append(_subjectid).append("\" class=\"searchSubjectID\"").append(ds.size() == 1 ? " checked" : "").append(">");
                        }
                        sb.append("</td>");
                        for (col = 0; col < columncount; ++col) {
                            String subjectcolumnid;
                            column = object.getJSONObject(String.valueOf(col));
                            mode = column.has("mode") ? column.getString("mode") : "Display Value";
                            String string = subjectcolumnid = column.has("subjectcolumnid") ? column.getString("subjectcolumnid") : "";
                            if ("Hidden".equals(mode) || !OpalUtil.isNotEmpty(subjectcolumnid)) continue;
                            if ("alias".equals(subjectcolumnid)) {
                                StringBuilder sbalias = new StringBuilder();
                                DataSet aliasds = this.getQueryProcessor().getPreparedSqlDataSet("select * from sdialias where sdcid = 'LV_Subject' and keyid1 = ?", (Object[])new String[]{_subjectid});
                                dmu.maskSDIAliasDataSet(aliasds, true);
                                if (aliasds != null) {
                                    for (int aliasindex = 0; aliasindex < aliasds.size(); ++aliasindex) {
                                        sbalias.append("<div>").append(aliasds.getValue(aliasindex, "aliasid")).append(" (").append(aliasds.getString(aliasindex, "aliastype")).append(")</div>");
                                    }
                                }
                                columnValue = sbalias.toString();
                            } else {
                                String href;
                                JSONObject link;
                                columnValue = ds.getValue(i, subjectcolumnid, "&nbsp;");
                                String string3 = displayvalue = column.has("displayvalue") ? column.getString("displayvalue") : "";
                                if (OpalUtil.isNotEmpty(displayvalue)) {
                                    columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                                }
                                JSONObject jSONObject = link = column.has("link") ? column.getJSONObject("link") : new JSONObject();
                                if (link.has("href") && OpalUtil.isNotEmpty(href = link.getString("href"))) {
                                    title = link.has("title") ? link.getString("title") : "";
                                    for (String token : tokens = StringUtil.getTokens(href)) {
                                        if (!ds.isValidColumn(token)) continue;
                                        href = StringUtil.replaceAll(href, "[" + token + "]", ds.getValue(i, token, ""));
                                    }
                                    columnValue = "<a href=\"#\" onclick=\"openLinkURL( '" + href + "', '" + title + "' )\">" + columnValue + "</a>";
                                }
                            }
                            if (columnValue.contains("</script")) {
                                columnValue = SafeHTML.encodeForHTML(columnValue);
                            }
                            sb.append("<td valign=\"top\" class=\"maintform_field\">").append(columnValue).append("</td>");
                        }
                        if (pds == null || pds.size() == 0) {
                            if (participantColumnCount > 0) {
                                sb.append("<td valign=\"top\" class=\"maintform_field\" colspan=\"").append(participantColumnCount).append("\">");
                                sb.append(this.getTranslationProcessor().translate("No Enrollment found in Study")).append(" (").append(SafeHTML.encodeForHTML(this.getStudyDisplayValue(studyid, studydisplayformat), true)).append(")");
                                sb.append("</td>");
                            }
                            sb.append("</tr>");
                            continue;
                        }
                        for (col = 0; col < columncount; ++col) {
                            String href;
                            JSONObject link;
                            String participantcolumnid;
                            column = object.getJSONObject(String.valueOf(col));
                            String string = mode = column.has("mode") ? column.getString("mode") : "Display Value";
                            if ("Hidden".equals(mode)) continue;
                            String string4 = participantcolumnid = column.has("participantcolumnid") ? column.getString("participantcolumnid") : "";
                            if (!OpalUtil.isNotEmpty(participantcolumnid)) continue;
                            if ("alias".equals(participantcolumnid)) {
                                StringBuilder sbalias = new StringBuilder();
                                DataSet aliasds = this.getQueryProcessor().getPreparedSqlDataSet("select * from sdialias where sdcid = 'LV_Participant' and keyid1 = ?", (Object[])new String[]{pds.getString(0, "s_participantid")});
                                dmu.maskSDIAliasDataSet(aliasds, true);
                                if (aliasds != null) {
                                    for (int aliasindex = 0; aliasindex < aliasds.size(); ++aliasindex) {
                                        sbalias.append("<div>").append(aliasds.getValue(aliasindex, "aliasid")).append(" (").append(aliasds.getString(aliasindex, "aliastype")).append(")</div>");
                                    }
                                }
                                sb.append("<td valign=\"top\" class=\"maintform_field\">").append(sbalias.toString()).append("</td>");
                                continue;
                            }
                            if (participantcolumnid.startsWith("(")) {
                                String columnid = participantcolumnid.substring(participantcolumnid.lastIndexOf(" ") + 1);
                                columnValue = pds.getValue(0, columnid, "&nbsp;");
                            } else {
                                columnValue = pds.getValue(0, participantcolumnid, "&nbsp;");
                            }
                            String string5 = displayvalue = column.has("displayvalue") ? column.getString("displayvalue") : "";
                            if (OpalUtil.isNotEmpty(displayvalue)) {
                                columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                            }
                            JSONObject jSONObject = link = column.has("link") ? column.getJSONObject("link") : new JSONObject();
                            if (link.has("href") && OpalUtil.isNotEmpty(href = link.getString("href"))) {
                                title = link.has("title") ? link.getString("title") : "";
                                for (String token : tokens = StringUtil.getTokens(href)) {
                                    if (!ds.isValidColumn(token)) continue;
                                    href = StringUtil.replaceAll(href, "[" + token + "]", ds.getValue(i, token, ""));
                                }
                                columnValue = "<a href=\"#\" onclick=\"openLinkURL( '" + href + "', '" + title + "' )\">" + columnValue + "</a>";
                            }
                            if ("sstudyid".equals(participantcolumnid)) {
                                columnValue = this.getStudyDisplayValue(columnValue, studydisplayformat);
                            }
                            if (columnValue.contains("</script")) {
                                columnValue = SafeHTML.encodeForHTML(columnValue);
                            }
                            sb.append("<td valign=\"top\" class=\"maintform_field\">").append(columnValue).append("</td>");
                        }
                        sb.append("</tr>");
                        continue;
                    }
                    sql.setLength(0);
                    StringBuilder selectSql = new StringBuilder();
                    for (String columnid : participantcolumns) {
                        selectSql.append(columnid).append(", ");
                    }
                    selectSql.append(" sstudyid pstudyid, studysiteid pstudysiteid, cpcohortid pcpcohortid");
                    sql.append("select ");
                    sql.append((CharSequence)selectSql);
                    sql.append(" from s_participant where s_participant.subjectid = ?");
                    if (OpalUtil.isNotEmpty(participantid)) {
                        sql.append(" and s_participant.s_participantid = '").append(participantid).append("'");
                    }
                    DataSet pds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{_subjectid});
                    dmu.maskPrimaryDataSet(pds, "LV_Participant", "primary[" + selectSql + "]", true);
                    if (pds == null || pds.size() <= 0) continue;
                    boolean subjectValidationFailed = false;
                    for (String columnid : validationMap.keySet()) {
                        String validationvalue = (String)validationMap.get(columnid);
                        if (!OpalUtil.isNotEmpty(validationvalue)) continue;
                        databasevalue = ds.getValue(i, "v_" + columnid);
                        if (ds.getColumnType(columnid) == 2) {
                            dbcal = ds.getCalendar(i, columnid);
                            displayformat = (String)validationDateFormatMap.get("v_" + columnid);
                            if (OpalUtil.isNotEmpty(displayformat)) {
                                DateFormat dateFormat = ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), displayformat);
                                try {
                                    Date date = dateFormat.parse(validationvalue);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    subjectValidationFailed = dbcal.get(1) != cal.get(1) || dbcal.get(2) != cal.get(2) || dbcal.get(5) != cal.get(5);
                                }
                                catch (ParseException e) {
                                    subjectValidationFailed = !validationvalue.equals(databasevalue);
                                }
                                continue;
                            }
                            subjectValidationFailed = !validationvalue.equals(databasevalue);
                            continue;
                        }
                        subjectValidationFailed = !validationvalue.equals(databasevalue);
                    }
                    for (int row = 0; row < pds.size(); ++row) {
                        String[] tokens;
                        String title;
                        String displayvalue;
                        String columnValue;
                        String mode;
                        JSONObject column;
                        int col;
                        boolean validationFailed = subjectValidationFailed;
                        if (!validationFailed) {
                            for (String searchcolumn : searchMap.keySet()) {
                                if (pds.getValue(0, searchcolumn, "").equals(searchMap.get(searchcolumn))) continue;
                                validationFailed = true;
                            }
                        }
                        if (validationFailed) {
                            sb.append("<tr datarow=\"Y\" style=\"background:#F6CECE\" validationfailed=Y>");
                        } else {
                            sb.append("<tr datarow=\"Y\" validationfailed=N>");
                        }
                        sb.append("<td valign=\"top\" class=\"maintform_field\">");
                        sb.append("<input type='hidden' name='newenrollment' value='N'>");
                        if (singleSelectionMode) {
                            sb.append("<input type=\"radio\" name=\"radio_subjectid\" value=\"").append(_subjectid).append("\" class=\"searchSubjectID\"");
                            sb.append(" studyid=\"").append(pds.getString(row, "pstudyid")).append("\"");
                            sb.append(" studysiteid=\"").append(pds.getString(row, "pstudysiteid")).append("\"");
                            sb.append(" cohortid=\"").append(pds.getString(row, "pcpcohortid")).append("\"");
                            sb.append(">");
                        } else {
                            sb.append("<input type=\"checkbox\" value=\"").append(_subjectid).append("\" class=\"searchSubjectID\"");
                            sb.append(" studyid=\"").append(pds.getString(row, "pstudyid")).append("\"");
                            sb.append(" studysiteid=\"").append(pds.getString(row, "pstudysiteid")).append("\"");
                            sb.append(" cohortid=\"").append(pds.getString(row, "pcpcohortid")).append("\"");
                            sb.append(">");
                        }
                        sb.append("</td>");
                        for (col = 0; col < columncount; ++col) {
                            String subjectcolumnid;
                            column = object.getJSONObject(String.valueOf(col));
                            mode = column.has("mode") ? column.getString("mode") : "Display Value";
                            String string = subjectcolumnid = column.has("subjectcolumnid") ? column.getString("subjectcolumnid") : "";
                            if ("Hidden".equals(mode) || !OpalUtil.isNotEmpty(subjectcolumnid)) continue;
                            if ("alias".equals(subjectcolumnid)) {
                                StringBuilder sbalias = new StringBuilder();
                                DataSet aliasds = this.getQueryProcessor().getPreparedSqlDataSet("select * from sdialias where sdcid = 'LV_Subject' and keyid1 = ?", (Object[])new String[]{_subjectid});
                                dmu.maskSDIAliasDataSet(aliasds, true);
                                if (aliasds != null) {
                                    for (int aliasindex = 0; aliasindex < aliasds.size(); ++aliasindex) {
                                        sbalias.append("<div>").append(aliasds.getValue(aliasindex, "aliasid")).append(" (").append(aliasds.getString(aliasindex, "aliastype")).append(")</div>");
                                    }
                                }
                                columnValue = sbalias.toString();
                            } else {
                                String href;
                                JSONObject link;
                                columnValue = ds.getValue(i, subjectcolumnid, "&nbsp;");
                                String string6 = displayvalue = column.has("displayvalue") ? column.getString("displayvalue") : "";
                                if (OpalUtil.isNotEmpty(displayvalue)) {
                                    columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                                }
                                JSONObject jSONObject = link = column.has("link") ? column.getJSONObject("link") : new JSONObject();
                                if (link.has("href") && OpalUtil.isNotEmpty(href = link.getString("href"))) {
                                    title = link.has("title") ? link.getString("title") : "";
                                    for (String token : tokens = StringUtil.getTokens(href)) {
                                        if (!ds.isValidColumn(token)) continue;
                                        href = StringUtil.replaceAll(href, "[" + token + "]", ds.getValue(i, token, ""));
                                    }
                                    columnValue = "<a href=\"#\" onclick=\"openLinkURL( '" + href + "', '" + title + "' )\">" + columnValue + "</a>";
                                }
                            }
                            sb.append("<td valign=\"top\" class=\"maintform_field\">").append(columnValue).append("</td>");
                        }
                        for (col = 0; col < columncount; ++col) {
                            String href;
                            JSONObject link;
                            String participantcolumnid;
                            column = object.getJSONObject(String.valueOf(col));
                            String string = mode = column.has("mode") ? column.getString("mode") : "Display Value";
                            if ("Hidden".equals(mode)) continue;
                            String string7 = participantcolumnid = column.has("participantcolumnid") ? column.getString("participantcolumnid") : "";
                            if (!OpalUtil.isNotEmpty(participantcolumnid)) continue;
                            if ("alias".equals(participantcolumnid)) {
                                StringBuilder sbalias = new StringBuilder();
                                DataSet aliasds = this.getQueryProcessor().getPreparedSqlDataSet("select * from sdialias where sdcid = 'LV_Participant' and keyid1 = ?", (Object[])new String[]{pds.getString(row, "s_participantid")});
                                dmu.maskSDIAliasDataSet(aliasds, true);
                                if (aliasds != null) {
                                    for (int aliasindex = 0; aliasindex < aliasds.size(); ++aliasindex) {
                                        sbalias.append("<div>").append(aliasds.getValue(aliasindex, "aliasid")).append(" (").append(aliasds.getString(aliasindex, "aliastype")).append(")</div>");
                                    }
                                }
                                sb.append("<td valign=\"top\" class=\"maintform_field\">").append(sbalias.toString()).append("</td>");
                                continue;
                            }
                            if (participantcolumnid.startsWith("(")) {
                                String columnid = participantcolumnid.substring(participantcolumnid.lastIndexOf(" ") + 1);
                                columnValue = pds.getValue(row, columnid, "&nbsp;");
                            } else {
                                columnValue = pds.getValue(row, participantcolumnid, "&nbsp;");
                            }
                            String string8 = displayvalue = column.has("displayvalue") ? column.getString("displayvalue") : "";
                            if (OpalUtil.isNotEmpty(displayvalue)) {
                                columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                            }
                            JSONObject jSONObject = link = column.has("link") ? column.getJSONObject("link") : new JSONObject();
                            if (link.has("href") && OpalUtil.isNotEmpty(href = link.getString("href"))) {
                                title = link.has("title") ? link.getString("title") : "";
                                for (String token : tokens = StringUtil.getTokens(href)) {
                                    if (!ds.isValidColumn(token)) continue;
                                    href = StringUtil.replaceAll(href, "[" + token + "]", ds.getValue(i, token, ""));
                                }
                                columnValue = "<a href=\"#\" onclick=\"openLinkURL( '" + href + "', '" + title + "' )\">" + columnValue + "</a>";
                            }
                            if ("sstudyid".equals(participantcolumnid)) {
                                columnValue = this.getStudyDisplayValue(columnValue, studydisplayformat);
                            }
                            sb.append("<td valign=\"top\" class=\"maintform_field\">").append(columnValue).append("</td>");
                        }
                        sb.append("</tr>");
                    }
                }
            }
            if (renderheader) {
                sb.append("</table>");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }

    private String getStudyDisplayValue(String studyid, String studydisplayformat) {
        if (!this.studymap.containsKey(studyid)) {
            List<String> list = Arrays.asList(StringUtil.getTokens(studydisplayformat));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select " + OpalUtil.toDelimitedString(list, ",") + " from s_study where s_studyid = ?", (Object[])new String[]{studyid});
            if (ds != null && ds.size() > 0) {
                for (String token : list) {
                    studydisplayformat = StringUtil.replaceAll(studydisplayformat, "[" + token + "]", ds.getValue(0, token));
                }
                this.studymap.put(studyid, studydisplayformat);
            } else {
                this.studymap.put(studyid, studyid);
            }
        }
        return this.studymap.get(studyid);
    }
}

