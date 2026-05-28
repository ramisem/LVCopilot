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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
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
import sapphire.util.SafeSQL;

public class SearchSubject
extends BaseAjaxRequest {
    SimpleDateFormat SQLDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean warnlimitexceeded;
        int subjectcount;
        String message;
        String participantid;
        HashSet<String> subjectSet;
        AjaxResponse ajaxResponse;
        block38: {
            DataSet ds;
            ajaxResponse = new AjaxResponse(request, response);
            subjectSet = new HashSet<String>();
            String data = ajaxResponse.getRequestParameter("data");
            participantid = ajaxResponse.getRequestParameter("participantid", "");
            message = "";
            subjectcount = 0;
            warnlimitexceeded = false;
            boolean bypasssearchlimit = "Y".equals(ajaxResponse.getRequestParameter("bypasssearchlimit", "N"));
            if (OpalUtil.isNotEmpty(participantid)) {
                Object[] s = new String[]{participantid};
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select subjectid from s_participant where s_participantid = ?", s);
                if (ds != null && ds.size() > 0) {
                    subjectSet.add(ds.getString(0, "subjectid"));
                } else {
                    message = this.getTranslationProcessor().translate("No Subject found for the searched criteria");
                }
            } else if (OpalUtil.isNotEmpty(data)) {
                try {
                    SafeSQL safeSQL_whereSubject = new SafeSQL();
                    SafeSQL safeSQL_whereParticipant = new SafeSQL();
                    StringBuilder whereSubject = new StringBuilder();
                    StringBuilder sqlSubject = new StringBuilder();
                    sqlSubject.append("select s_subjectid from s_subject");
                    StringBuilder whereParticipant = new StringBuilder();
                    StringBuilder sqlParticipant = new StringBuilder();
                    sqlParticipant.append("select subjectid from s_participant");
                    JSONArray searchData = new JSONArray(data);
                    for (int i = 0; i < searchData.length(); ++i) {
                        String aliastype;
                        JSONObject column = searchData.getJSONObject(i);
                        String columnid = column.getString("columnid");
                        String value = column.getString("value");
                        String dateformat = column.has("dateformat") ? column.getString("dateformat") : "";
                        boolean matchexact = "Y".equals(column.has("matchexact") ? column.getString("matchexact") : "Y");
                        boolean caseindependent = "Y".equals(column.has("caseindependent") ? column.getString("caseindependent") : "N");
                        if (columnid.startsWith("subject.")) {
                            whereSubject.append(whereSubject.length() == 0 ? " where " : " and ");
                            if (columnid.endsWith(".alias")) {
                                String string = aliastype = column.has("aliastype") ? column.getString("aliastype") : "";
                                if (OpalUtil.isNotEmpty(aliastype)) {
                                    whereSubject.append("exists (select 1 from sdialias where sdialias.aliasid = ").append(safeSQL_whereSubject.addVar(value));
                                    whereSubject.append(" and sdialias.aliastype = ").append(safeSQL_whereSubject.addVar(aliastype));
                                    whereSubject.append(" and sdialias.sdcid = 'LV_Subject' and sdialias.keyid1 = s_subject.s_subjectid)");
                                    continue;
                                }
                                whereSubject.append("exists (select 1 from sdialias where sdialias.aliasid = ").append(safeSQL_whereSubject.addVar(value));
                                whereSubject.append(" and sdialias.sdcid = 'LV_Subject' and sdialias.keyid1 = s_subject.s_subjectid)");
                                continue;
                            }
                            if (OpalUtil.isNotEmpty(dateformat)) {
                                DateFormat dateFormat = ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), dateformat);
                                try {
                                    Date date = dateFormat.parse(value);
                                    whereSubject.append("s_").append(columnid).append(" = ").append(safeSQL_whereSubject.addVar(this.SQLDateFormat.format(date)));
                                }
                                catch (ParseException e) {
                                    whereSubject.append("s_").append(columnid).append(" = ").append(safeSQL_whereSubject.addVar(value));
                                }
                                continue;
                            }
                            if (matchexact) {
                                if (caseindependent) {
                                    whereSubject.append("lower(s_").append(columnid).append(") = ").append(safeSQL_whereSubject.addVar(value.toLowerCase()));
                                    continue;
                                }
                                whereSubject.append("s_").append(columnid).append(" = ").append(safeSQL_whereSubject.addVar(value));
                                continue;
                            }
                            if (caseindependent) {
                                whereSubject.append("lower(s_").append(columnid).append(") like ").append(safeSQL_whereSubject.addVar("%" + value.toLowerCase() + "%"));
                                continue;
                            }
                            whereSubject.append("s_").append(columnid).append(" like ").append(safeSQL_whereSubject.addVar("%" + value + "%"));
                            continue;
                        }
                        if (!columnid.startsWith("participant.")) continue;
                        whereParticipant.append(whereParticipant.length() == 0 ? " where " : " and ");
                        if (columnid.endsWith(".alias")) {
                            String string = aliastype = column.has("aliastype") ? column.getString("aliastype") : "";
                            if (OpalUtil.isNotEmpty(aliastype)) {
                                whereParticipant.append("exists (select 1 from sdialias where sdialias.aliasid = ").append(safeSQL_whereParticipant.addVar(value));
                                whereParticipant.append(" and sdialias.aliastype = ").append(safeSQL_whereParticipant.addVar(aliastype));
                                whereParticipant.append(" and sdialias.sdcid = 'LV_Particiapnt' and sdialias.keyid1 = s_participant.s_participantid)");
                                continue;
                            }
                            whereParticipant.append("exists (select 1 from sdialias where sdialias.aliasid = ").append(safeSQL_whereParticipant.addVar(value));
                            whereParticipant.append(" and sdialias.sdcid = 'LV_Particiapnt' and sdialias.keyid1 = s_participant.s_participantid)");
                            continue;
                        }
                        if (matchexact) {
                            if (caseindependent) {
                                whereParticipant.append("lower(s_").append(columnid).append(") = ").append(safeSQL_whereParticipant.addVar(value.toLowerCase()));
                                continue;
                            }
                            whereParticipant.append("s_").append(columnid).append(" = ").append(safeSQL_whereParticipant.addVar(value));
                            continue;
                        }
                        if (caseindependent) {
                            whereParticipant.append("lower(s_").append(columnid).append(") like ").append(safeSQL_whereParticipant.addVar("%" + value.toLowerCase() + "%"));
                            continue;
                        }
                        whereParticipant.append("s_").append(columnid).append(" like ").append(safeSQL_whereParticipant.addVar("%" + value + "%"));
                    }
                    if (whereParticipant.length() > 0) {
                        sqlParticipant.append((CharSequence)whereParticipant);
                        DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sqlParticipant.toString(), safeSQL_whereParticipant.getValues());
                        if (ds2 != null && ds2.size() > 0) {
                            for (int i = 0; i < ds2.size(); ++i) {
                                subjectSet.add(ds2.getString(i, "subjectid"));
                            }
                        }
                    }
                    if (whereSubject.length() > 0) {
                        sqlSubject.append((CharSequence)whereSubject);
                        DataSet ds3 = this.getQueryProcessor().getPreparedSqlDataSet(sqlSubject.toString(), safeSQL_whereSubject.getValues());
                        if (ds3 != null && ds3.size() > 0) {
                            for (int i = 0; i < ds3.size(); ++i) {
                                subjectSet.add(ds3.getString(i, "s_subjectid"));
                            }
                        }
                    }
                    if (subjectSet.size() == 0) {
                        message = this.getTranslationProcessor().translate("No Subject found for the searched criteria");
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (subjectSet.size() > 0) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet("LV_Subject", OpalUtil.toDelimitedString(subjectSet, ";"), null, null);
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select keyid1 from rsetitems where rsetid = ?", (Object[])new String[]{rsetid});
                    if (ds != null) {
                        subjectSet.clear();
                        for (int i = 0; i < ds.size(); ++i) {
                            subjectSet.add(ds.getString(i, "keyid1"));
                        }
                    }
                    this.getDAMProcessor().clearRSet(rsetid);
                    if (subjectSet.size() == 0) {
                        message = this.getTranslationProcessor().translate("No Subject found for the searched criteria");
                    }
                    subjectcount = subjectSet.size();
                    if (bypasssearchlimit) break block38;
                    int searchwarnlimit = 0;
                    try {
                        searchwarnlimit = Integer.parseInt(ajaxResponse.getRequestParameter("searchwarnlimit", "0"));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    if (searchwarnlimit > 0 && subjectSet.size() > searchwarnlimit) {
                        warnlimitexceeded = true;
                    }
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("subjectid", warnlimitexceeded ? "" : OpalUtil.toDelimitedString(subjectSet, ";"));
        ajaxResponse.addCallbackArgument("participantid", warnlimitexceeded ? "" : participantid);
        ajaxResponse.addCallbackArgument("warnlimitexceeded", warnlimitexceeded ? "Y" : "N");
        ajaxResponse.addCallbackArgument("subjectcount", subjectcount);
        ajaxResponse.print();
    }
}

