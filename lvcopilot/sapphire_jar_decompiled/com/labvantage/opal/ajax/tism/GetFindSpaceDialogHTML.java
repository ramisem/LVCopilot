/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.tism;

import com.labvantage.opal.util.OpalUtil;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetFindSpaceDialogHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int i;
        PropertyListCollection storageunittypes;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        try {
            PropertyList searchstoragelocation = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getPropertyListNotNull("searchstoragelocation");
            storageunittypes = searchstoragelocation.getCollectionNotNull("storageunittypes");
        }
        catch (SapphireException e) {
            storageunittypes = new PropertyListCollection();
        }
        String defaultsampletypeid = "";
        String defaultstudyid = "";
        String freespace = "1";
        if (trackitemid.length() > 0) {
            boolean sampleonlyflag = true;
            DataSet ds = new DataSet();
            List<String> list = OpalUtil.toList(trackitemid, ";");
            String sql = "select trackitem.trackitemid, trackitem.linksdcid, s_sample.s_sampleid, s_sample.sampletypeid, s_sample.sstudyid from trackitem, s_sample where trackitem.linkkeyid1 = s_sample.s_sampleid and trackitem.linksdcid = 'Sample'";
            if (list.size() > 1000) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                    sql = sql + " and trackitem.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql = sql + " and trackitem.trackitemid in (" + safeSQL.addIn(list) + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                for (int i2 = 0; i2 < ds.size(); ++i2) {
                    String sstudyid;
                    String sampletypeid = ds.getString(i2, "sampletypeid", "");
                    if (sampletypeid.length() > 0) {
                        if (defaultsampletypeid.length() == 0) {
                            defaultsampletypeid = sampletypeid;
                        } else if (!defaultsampletypeid.equals(sampletypeid)) {
                            defaultsampletypeid = "";
                            sampleonlyflag = false;
                            break;
                        }
                    }
                    if ((sstudyid = ds.getString(i2, "sstudyid", "")).length() <= 0) continue;
                    if (defaultstudyid.length() == 0) {
                        defaultstudyid = sstudyid;
                        continue;
                    }
                    if (defaultstudyid.equals(sstudyid)) continue;
                    defaultstudyid = "";
                    sampleonlyflag = false;
                    break;
                }
            }
            if (sampleonlyflag) {
                freespace = String.valueOf(list.size());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:4px;'>");
        sb.append("<table cellpadding=4 cellspacing=0 border=0 style='width:100%'>");
        sb.append("<tr>");
        sb.append("<td>").append(this.getTranslationProcessor().translate("Find")).append("</td>");
        sb.append("<td>");
        sb.append("<select id=\"search_storageunittype\" name=\"search_storageunittype\" class=\"mandatoryfield\">");
        sb.append("<option></option>");
        for (int i3 = 0; i3 < storageunittypes.size(); ++i3) {
            String title;
            PropertyList pl = storageunittypes.getPropertyList(i3);
            if (!"Y".equals(pl.getProperty("show")) || !OpalUtil.isNotEmpty(title = pl.getProperty("title").trim())) continue;
            sb.append("<option name='search_storageunittype' value=\"").append(title).append("\">");
            sb.append(title);
            sb.append("</option>");
        }
        sb.append("</select>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>").append(this.getTranslationProcessor().translate("Having free space")).append("</td>");
        sb.append("<td><input style=\"width:30px;text-align: right;\" value=\"").append(freespace).append("\" id=\"search_freespace\" name=\"search_freespace\"></td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>").append(this.getTranslationProcessor().translate("For Sample Type")).append("</td>");
        sb.append("<td>");
        sb.append("<select id=\"search_sampletypeid\" name=\"search_sampletypeid\">");
        sb.append("<option value=''>").append(this.getTranslationProcessor().translate("Any")).append("</option>");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("SampleType");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryFrom("s_sampletype");
        sdiRequest.setQueryOrderBy("s_sampletypeid");
        DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        for (i = 0; i < primary.size(); ++i) {
            String sampletypeid = primary.getString(i, "s_sampletypeid");
            sb.append("<option value=\"").append(sampletypeid).append("\"").append(sampletypeid.equals(defaultsampletypeid) ? " selected" : "").append(">").append(sampletypeid).append("</option>");
        }
        sb.append("</select>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>").append(this.getTranslationProcessor().translate("For Study")).append("</td>");
        sb.append("<td>");
        sb.append("<select id=\"search_studyid\" name=\"search_studyid\">");
        sb.append("<option value=''>").append(this.getTranslationProcessor().translate("Any")).append("</option>");
        sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Study");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryFrom("s_study");
        sdiRequest.setQueryOrderBy("s_studyid");
        primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        for (i = 0; i < primary.size(); ++i) {
            String studyid = primary.getString(i, "s_studyid");
            sb.append("<option value=\"").append(studyid).append("\"").append(studyid.equals(defaultstudyid) ? " selected" : "").append(">").append(studyid).append("</option>");
        }
        sb.append("</select>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

