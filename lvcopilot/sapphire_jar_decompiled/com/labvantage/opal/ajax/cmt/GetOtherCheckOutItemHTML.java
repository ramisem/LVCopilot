/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.opal.util.CheckInDialogUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.format.RelativeDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetOtherCheckOutItemHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        StringBuilder html = new StringBuilder();
        String changelogid = ajaxResponse.getRequestParameter("changelogid");
        HashSet<String> sdcSet = new HashSet<String>();
        HashSet<String> changeRequestSet = new HashSet<String>();
        try {
            RelativeDateFormat relativeDateFormat = new RelativeDateFormat(false, this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.getTranslationProcessor());
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.changerequestid, changelog.checkedoutbyuserid, changelog.checkedoutdt, changelog.checkedoutbydepartmentid, changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid from changelog where changelog.changelogid in (" + safeSQL.addIn(changelogid, ";") + ") ORDER BY linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                ds.sort("linksdcid");
                ArrayList<DataSet> list = ds.getGroupedDataSets("linksdcid");
                for (DataSet dataset : list) {
                    dataset.sort("changerequestid");
                    String linksdcid = dataset.getString(0, "linksdcid");
                    html.append("<table cellpadding=4 class='maintform_table otherdatatable' style='width:100%;margin-bottom:10px;background:white;' sdcid='").append(linksdcid).append("'>");
                    html.append(CheckInDialogUtil.renderHeaderRow(this.getSDCProcessor(), this.getTranslationProcessor(), linksdcid, false, true, false));
                    for (int i = 0; i < dataset.size(); ++i) {
                        sdcSet.add(dataset.getString(i, "linksdcid", ""));
                        changeRequestSet.add(dataset.getString(i, "changerequestid", ""));
                        String id = dataset.getString(i, "changelogid");
                        html.append(CheckInDialogUtil.renderDataRow(this.getSDCProcessor(), this.getQueryProcessor(), this.getTranslationProcessor(), relativeDateFormat, linksdcid, id, dataset, i, "", false, false));
                    }
                    html.append("</table>");
                }
            }
        }
        catch (SapphireException e) {
            message = "Exception raised: " + e.getMessage();
            e.printStackTrace();
        }
        StringBuilder s = new StringBuilder();
        if (sdcSet.size() > 0) {
            s = new StringBuilder("SDC: <select id='othersdcselect'><option></option>");
            for (String sdc : sdcSet) {
                s.append("<option>").append(sdc).append("</option>");
            }
            s.append("</select>");
        }
        if (changeRequestSet.size() > 0) {
            s.append("&nbsp; Change Request: <select id='otherchangerequestselect'><option></option>");
            for (String sdc : changeRequestSet) {
                if (!OpalUtil.isNotEmpty(sdc)) continue;
                s.append("<option>").append(sdc).append("</option>");
            }
            s.append("</select>");
        }
        s = s.length() > 0 ? new StringBuilder("<div style='width:100%;padding-bottom:5px;'>" + s + "</div>" + html.toString()) : new StringBuilder(html.toString());
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", s.toString());
        ajaxResponse.print();
    }
}

