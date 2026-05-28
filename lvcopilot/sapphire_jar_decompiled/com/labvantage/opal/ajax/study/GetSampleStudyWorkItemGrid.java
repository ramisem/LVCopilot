/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.study;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetSampleStudyWorkItemGrid
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean studyExists;
        StringBuilder sb = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid", "").trim();
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String sampledisplaycolumnid = ajaxResponse.getRequestParameter("sampledisplaycolumnid", "s_sampleid").trim();
        String[] samples = StringUtil.split(sampleid, ";");
        SafeSQL safeSQL = new SafeSQL();
        boolean bl = studyExists = this.getQueryProcessor().getPreparedSqlDataSet("select s_studyid from s_study where s_studyid = " + safeSQL.addVar(studyid) + " and templateflag != 'Y'", safeSQL.getValues()).size() > 0;
        if (studyExists) {
            int i;
            ArrayList<String> list = new ArrayList<String>();
            sql.setLength(0);
            safeSQL.reset();
            DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select " + sampledisplaycolumnid + ", sstudyid from s_sample where s_sampleid in ( " + safeSQL.addIn(sampleid, ";") + " )", safeSQL.getValues());
            if (_ds != null && _ds.size() > 0) {
                for (i = 0; i < _ds.size(); ++i) {
                    if (!_ds.getString(i, "sstudyid").equals(studyid)) continue;
                    list.add(_ds.getValue(i, sampledisplaycolumnid, ""));
                }
                if (list.size() > 0) {
                    sb.append("<div style='font:normal 12 sans-serif;text-align:left;'><span style='color:red;'>").append(this.getTranslationProcessor().translate("ERROR")).append("</span><hr>");
                    sb.append(this.getTranslationProcessor().translate("The selected study is already a Primary Study on following Sample(s)"));
                    sb.append("<br>");
                    i = 1;
                    for (String s : list) {
                        sb.append("<br>").append(i++).append(". ").append(s);
                    }
                    sb.append("</div>");
                }
            }
            safeSQL.reset();
            _ds = this.getQueryProcessor().getPreparedSqlDataSet("select (select s." + sampledisplaycolumnid + " from s_sample s where s.s_sampleid = ss.s_sampleid) " + sampledisplaycolumnid + ", ss.s_studyid from s_samples_study ss where ss.s_sampleid in ( " + safeSQL.addIn(sampleid, ";") + " )", safeSQL.getValues());
            if (_ds != null && _ds.size() > 0) {
                list.clear();
                for (i = 0; i < _ds.size(); ++i) {
                    if (!_ds.getString(i, "s_studyid").equals(studyid)) continue;
                    list.add(_ds.getValue(i, sampledisplaycolumnid, ""));
                }
                if (list.size() > 0) {
                    if (sb.length() > 0) {
                        sb.append("<br><br>");
                    }
                    sb.append("<div style='font:normal 12 sans-serif;text-align:left;'><span style='color:red;'>").append(this.getTranslationProcessor().translate("ERROR")).append("</span><hr>");
                    sb.append(this.getTranslationProcessor().translate("The selected study is already an Auxiliary Study on following Sample(s)"));
                    sb.append("<br>");
                    i = 1;
                    for (String s : list) {
                        sb.append("<br>").append(i++).append(". ").append(s);
                    }
                    sb.append("</div>");
                }
            }
        } else if (studyid.length() > 0) {
            sb.append("<div style='font:normal 12 sans-serif;text-align:left;'><span style='color:red;'>").append(this.getTranslationProcessor().translate("ERROR")).append("</span><hr>");
            sb.append(this.getTranslationProcessor().translate("Study does not exist")).append(": <strong>").append(studyid).append("</strong></div>");
        }
        if (sb.length() == 0) {
            HashMap<String, String> sampleTypeMap = new HashMap<String, String>();
            HashMap<String, String> sampleDisplayMap = new HashMap<String, String>();
            sql.setLength(0);
            safeSQL.reset();
            sql.append("select s_sampleid, sampletypeid, ").append(sampledisplaycolumnid);
            sql.append(" from s_sample");
            sql.append(" where s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
            DataSet sampletypeds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            for (int i = 0; i < sampletypeds.size(); ++i) {
                sampleTypeMap.put(sampletypeds.getString(i, "s_sampleid"), sampletypeds.getString(i, "sampletypeid", ""));
                sampleDisplayMap.put(sampletypeds.getString(i, "s_sampleid"), sampletypeds.getString(i, sampledisplaycolumnid, ""));
            }
            if (OpalUtil.isNotEmpty(studyid)) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("select workitemid, workitemversionid, workiteminstance, s_sampletypeid");
                sql.append(" from sdiworkitem");
                sql.append(" where sdcid = 'Study'");
                sql.append(" and keyid1 = ").append(safeSQL.addVar(studyid));
                sql.append(" and workiteminstance = '1'");
                sql.append(" order by usersequence");
                DataSet studyds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (studyds != null && studyds.size() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("select keyid1, workitemid, workitemversionid, workiteminstance, workitemstatus");
                    sql.append(" from sdiworkitem");
                    sql.append(" where sdcid = 'Sample'");
                    sql.append(" and keyid1 in (").append(safeSQL.addIn(sampleid, ";")).append(")");
                    sql.append(" and workiteminstance = '1'");
                    sql.append(" order by workitemid, workitemversionid, workiteminstance");
                    DataSet sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    int optiongroup = 0;
                    sb.append("<table cellpadding=0 cellspacing=0 border=0 class='gridTable'>");
                    sb.append("<tr>");
                    sb.append("<td class='gridHeader'>");
                    sb.append(this.getTranslationProcessor().translate("Sample"));
                    sb.append("<br><span class='gridSampleType'>[").append(this.getTranslationProcessor().translate("Sample Type")).append("]</span>");
                    sb.append("</td>");
                    for (int studyindex = 0; studyindex < studyds.size(); ++studyindex) {
                        String workitemid = studyds.getValue(studyindex, "workitemid");
                        String workitemversionid = studyds.getValue(studyindex, "workitemversionid");
                        String sampletypeid = studyds.getValue(studyindex, "s_sampletypeid");
                        if (!sampleTypeMap.containsValue(sampletypeid)) continue;
                        workitemid = StringUtil.replaceAll(workitemid, "<", "&lt;");
                        workitemid = StringUtil.replaceAll(workitemid, ">", "&gt;");
                        sb.append("<td class='gridHeader'>");
                        sb.append(workitemid).append(" (").append(workitemversionid).append(")");
                        sb.append("<br>[").append(sampletypeid).append("]");
                        if (samples.length > 1) {
                            sb.append("<table cellpadding=2 cellspacing=0 border=0 style='padding-top:3px;margin:0 auto;'><tr>");
                            sb.append("<td align=center><input type='radio' class='optionall' name='t").append(studyindex).append("' childclass='Skip' checked><br>Skip</td>");
                            sb.append("<td align=center><input type='radio' class='optionall' name='t").append(studyindex).append("' childclass='Add'><br>Add</td>");
                            sb.append("<td align=center><input type='radio' class='optionall' name='t").append(studyindex).append("' childclass='Repeat'><br>Repeat</td>");
                            sb.append("<td align=center><input type='radio' class='optionall' name='t").append(studyindex).append("' childclass='Link'><br>Link</td>");
                            sb.append("</tr></table>");
                        }
                        sb.append("</td>");
                    }
                    sb.append("</tr>");
                    if (studyds.size() > 0) {
                        boolean testfoundflag = false;
                        StringBuilder s = new StringBuilder();
                        for (String sample : samples) {
                            String sampleType = (String)sampleTypeMap.get(sample);
                            s.append("<tr>");
                            s.append("<td class='gridField gridSample'>");
                            s.append((String)sampleDisplayMap.get(sample)).append("<br><span class='gridSampleType'>[").append(sampleType).append("]</span>");
                            s.append("</td>");
                            for (int studyindex = 0; studyindex < studyds.size(); ++studyindex) {
                                String workitemid = studyds.getValue(studyindex, "workitemid");
                                String workitemversionid = studyds.getValue(studyindex, "workitemversionid");
                                String sampletypeid = studyds.getValue(studyindex, "s_sampletypeid");
                                if (!sampleTypeMap.containsValue(sampletypeid)) continue;
                                if (sampleType.equals(sampletypeid)) {
                                    testfoundflag = true;
                                    filter.clear();
                                    filter.put("keyid1", sample);
                                    filter.put("workitemid", workitemid);
                                    filter.put("workitemversionid", workitemversionid);
                                    String optionname = "option_" + optiongroup++;
                                    String key = sample + "|" + workitemid + "|" + workitemversionid;
                                    int row = sampleds.findRow(filter);
                                    if (row != -1) {
                                        s.append("<td class='gridField yestest'>");
                                        s.append("<table cellpadding=0 cellspacing=0 border=0 style='width:100%'>");
                                        s.append("<tr><td colspan=3 class='testStatus' style='text-align:left'><span style='color:#666'>").append(this.getTranslationProcessor().translate("Test Status")).append(": </span>").append(sampleds.getValue(row, "workitemstatus", "")).append("</td></tr>");
                                        s.append("<tr>");
                                        s.append("<td class='testOptions'>");
                                        s.append("<input type='radio' name='").append(optionname).append("' class='t").append(studyindex).append("Skip' value='").append(key).append("|_Skip_' checked> ").append(this.getTranslationProcessor().translate("Skip"));
                                        s.append("</td>");
                                        s.append("<td class='testOptions'>");
                                        s.append("<input type='radio' name='").append(optionname).append("' class='t").append(studyindex).append("Repeat' value='").append(key).append("|Repeat'> ").append(this.getTranslationProcessor().translate("Repeat"));
                                        s.append("</td>");
                                        boolean renderLink = true;
                                        DataSet instanceds = this.getQueryProcessor().getPreparedSqlDataSet("select max(workiteminstance) instance from sdiworkitem where sdcid = 'Sample' and keyid1 = ? and workitemid = ? and workitemversionid = ?", (Object[])new String[]{sample, workitemid, workitemversionid});
                                        if (instanceds != null && instanceds.size() > 0) {
                                            boolean bl2 = renderLink = 1 == instanceds.getInt(0, "instance");
                                        }
                                        if (renderLink) {
                                            s.append("<td class='testOptions'>");
                                            s.append("<input type='radio' name='").append(optionname).append("' class='t").append(studyindex).append("Link' value='").append(key).append("|Link'> ").append(this.getTranslationProcessor().translate("Link"));
                                            s.append("</td>");
                                        }
                                        s.append("</tr>");
                                        s.append("</table>");
                                    } else {
                                        s.append("<td class='gridField notest'>");
                                        s.append("<table cellpadding=0 cellspacing=0 border=0 style='width:100%'>");
                                        s.append("<tr><td colspan=2 class='testStatus' style='color:#666'>").append(this.getTranslationProcessor().translate("Test not found on sample")).append("</td></tr>");
                                        s.append("<tr>");
                                        s.append("<td class='testOptions'>");
                                        s.append("<input type='radio' name='").append(optionname).append("' class='t").append(studyindex).append("Skip' value='").append(key).append("|_Skip_' checked> ").append(this.getTranslationProcessor().translate("Skip"));
                                        s.append("</td>");
                                        s.append("<td class='testOptions'>");
                                        s.append("<input type='radio' name='").append(optionname).append("' class='t").append(studyindex).append("Add' value='").append(key).append("|Add'> ").append(this.getTranslationProcessor().translate("Add"));
                                        s.append("</td>");
                                        s.append("</tr>");
                                        s.append("</table>");
                                    }
                                } else {
                                    s.append("<td class='gridField empty'>");
                                    s.append("&nbsp;");
                                }
                                s.append("</td>");
                            }
                            s.append("</tr>");
                        }
                        if (testfoundflag) {
                            sb.append((CharSequence)s);
                            sb.append("</table>");
                        } else {
                            sb.setLength(0);
                            sb.append(this.renderNoTestsHTML(studyid, samples, sampleTypeMap, sampleDisplayMap));
                        }
                    } else {
                        sb.setLength(0);
                        sb.append(this.renderNoTestsHTML(studyid, samples, sampleTypeMap, sampleDisplayMap));
                    }
                } else {
                    sb.append(this.renderNoTestsHTML(studyid, samples, sampleTypeMap, sampleDisplayMap));
                }
            } else {
                sb.append(this.renderNoTestsHTML(studyid, samples, sampleTypeMap, sampleDisplayMap));
            }
        }
        ajaxResponse.addCallbackArgument("data", sb.toString());
        ajaxResponse.addCallbackArgument("validstudy", studyExists ? "Y" : "N");
        ajaxResponse.print();
    }

    private String renderNoTestsHTML(String studyid, String[] samples, Map<String, String> sampleTypeMap, Map<String, String> sampleDisplayMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=0 cellspacing=0 border=0 class='gridTable'>");
        sb.append("<tr>");
        sb.append("<td class='gridHeader'>");
        sb.append(this.getTranslationProcessor().translate("Sample"));
        sb.append("<br><span class='gridSampleType'>[").append(this.getTranslationProcessor().translate("Sample Type")).append("]</span>");
        sb.append("</td>");
        sb.append("<td class='gridHeader' style='width:300px;text-align:center;'>");
        sb.append(this.getTranslationProcessor().translate("Test Selection"));
        sb.append("</td>");
        sb.append("</tr>");
        int i = 0;
        for (String sample : samples) {
            String sampleType = sampleTypeMap.get(sample);
            sb.append("<tr>");
            sb.append("<td class='gridField gridSample'>");
            sb.append(sampleDisplayMap.get(sample)).append("<br><span class='gridSampleType'>[").append(sampleType).append("]</span>");
            sb.append("</td>");
            if (i == 0) {
                sb.append("<td class='gridField' rowspan='").append(samples.length).append("' style='padding-top:10px;text-align:center;'>");
                if (OpalUtil.isNotEmpty(studyid)) {
                    sb.append(this.getTranslationProcessor().translate("No applicable tests found in Study to apply on samples"));
                } else {
                    sb.append(this.getTranslationProcessor().translate("Please enter Study"));
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
            ++i;
        }
        sb.append("</table>");
        return sb.toString();
    }
}

