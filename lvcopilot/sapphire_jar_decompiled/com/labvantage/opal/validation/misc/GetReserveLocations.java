/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetReserveLocations
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 86523 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuffer sql = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        String success = "Y";
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select t.trackitemid, r.storageunitid, t.linksdcid sdcid, t.linkkeyid1 keyid1, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn, su.labelpath,");
        sql.append(" (select se.freezethawcandidateflag from storageenv se where se.storageenvid = su.storageenvid) ftcandidateflag,");
        sql.append(" (select ase.freezethawcandidateflag from storageenv ase where ase.storageenvid = ( select asu.storageenvid from storageunit asu where asu.storageunitid = su.ancestorid) ) aftcandidateflag");
        sql.append(" from reservestorageunit r, trackitem t, storageunit su");
        sql.append(" where t.trackitemid in ( ").append(safeSQL.addIn(ajaxResponse.getRequestParameter("trackitemid"), ";")).append(" )");
        sql.append(" and r.trackitemid = t.trackitemid");
        sql.append(" and su.storageunitid = r.storageunitid");
        sql.append(" order by t.trackitemid");
        DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (_ds != null && _ds.size() > 0) {
            _ds.sort("trackitemid");
            ArrayList<DataSet> list = _ds.getGroupedDataSets("trackitemid");
            sb.append("<p>");
            sb.append(this.getTranslationProcessor().translate("Please confirm the reserve locations for selected trackitem(s)"));
            sb.append("</p>");
            sb.append("<br>");
            sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
            sb.append("<tr>");
            sb.append("<td rowspan=2 class='maintform_fieldtitle' width='20' style='padding:4px'>&nbsp;</td>");
            sb.append("<td rowspan=2 class='maintform_fieldtitle' style='padding:4px'>").append(this.getTranslationProcessor().translate("Trackitem")).append("</td>");
            sb.append("<td rowspan=2 class='maintform_fieldtitle' style='padding:4px'>").append(this.getTranslationProcessor().translate("SDC")).append("</td>");
            sb.append("<td rowspan=2 class='maintform_fieldtitle' style='padding:4px'>").append(this.getTranslationProcessor().translate("KeyID")).append("</td>");
            sb.append("<td rowspan=2 class='maintform_fieldtitle' style='padding:4px'>").append(this.getTranslationProcessor().translate("Reserve Location")).append("</td>");
            sb.append("<td colspan=4 class='maintform_fieldtitle' style='padding:4px' align=center>").append(this.getTranslationProcessor().translate("Freeze Thaw")).append("</td>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td class='maintform_fieldtitle' width=40 style='padding:4px' align=center>").append(this.getTranslationProcessor().translate("Thawed")).append("</td>");
            sb.append("<td class='maintform_fieldtitle' width=50 style='padding:4px' align=center>").append(this.getTranslationProcessor().translate("Count")).append("</td>");
            sb.append("<td class='maintform_fieldtitle' width=40 style='padding:4px' align=center>").append(this.getTranslationProcessor().translate("Warn")).append("</td>");
            sb.append("<td class='maintform_fieldtitle' width=40 style='padding:4px' align=center>").append(this.getTranslationProcessor().translate("Max")).append("</td>");
            sb.append("</tr>");
            for (int i = 0; i < list.size(); ++i) {
                DataSet d = (DataSet)list.get(i);
                int rowspan = d.size();
                String ftimage = "greenled.gif";
                String trackitemid = d.getString(0, "trackitemid");
                String sdcid = d.getString(0, "sdcid");
                String keyid1 = d.getString(0, "keyid1");
                boolean isTrackItemFTCandidate = "Y".equals(d.getString(0, "freezethawflag"));
                int freezethawcount = d.getInt(0, "freezethawcount", -1) + 1;
                int freezethawcountmax = d.getInt(0, "freezethawcountmax");
                int freezethawcountwarn = d.getInt(0, "freezethawcountwarn");
                if (isTrackItemFTCandidate) {
                    if (freezethawcount == -1) {
                        ftimage = "greenled.gif";
                    } else if (freezethawcount > freezethawcountmax) {
                        ftimage = "redled.gif";
                    } else if (freezethawcount == freezethawcountmax) {
                        ftimage = "orangeled.gif";
                    } else if (freezethawcount >= freezethawcountwarn && freezethawcount < freezethawcountmax) {
                        ftimage = "yellowled.gif";
                    } else if (freezethawcount < freezethawcountwarn) {
                        ftimage = "greenled.gif";
                    }
                }
                for (int row = 0; row < rowspan; ++row) {
                    String storageunitid = d.getString(row, "storageunitid");
                    String labelpath = d.getString(row, "labelpath");
                    boolean isStorageUnitFTCandidate = "Y".equals(d.getString(row, "ftcandidateflag")) || "Y".equals(d.getString(row, "aftcandidateflag"));
                    sb.append("<tr>");
                    if (row == 0) {
                        sb.append("<td class='maintform_field' valign='top' align=right rowspan=").append(rowspan).append(" style='padding:4px'>");
                        sb.append(i + 1);
                        sb.append("    <input type=hidden id='__res_trackitemid_").append(i).append("' value='").append(trackitemid).append("'>");
                        sb.append("    <input type=hidden id='__res_sdcid_").append(i).append("' value='").append(sdcid).append("'>");
                        sb.append("    <input type=hidden id='__res_keyid1_").append(i).append("' value='").append(keyid1).append("'>");
                        sb.append("    <input type=hidden id='__res_labelpath_").append(i).append("' value='").append(labelpath).append("'>");
                        sb.append("    <input type=hidden id='__res_storageunitid_").append(i).append("' value='").append(storageunitid).append("'>");
                        sb.append("</td>");
                        sb.append("<td class='maintform_field' valign='top' rowspan=").append(rowspan).append(" style='padding:4px'>").append(trackitemid).append("</td>");
                        sb.append("<td class='maintform_field' valign='top' rowspan=").append(rowspan).append(" style='padding:4px'>").append(sdcid).append("</td>");
                        sb.append("<td class='maintform_field' valign='top' rowspan=").append(rowspan).append(" style='padding:4px'>").append(keyid1).append("</td>");
                        sb.append("<td class='maintform_field' valign='top' style='padding:4px'>");
                        sb.append("    <input type='radio' checked name='__res_storageunitlabel_").append(i).append("' onclick=\"_nav_frame1.__res_selectReserveUnit('").append(storageunitid).append("', '").append(labelpath).append("', ").append(isStorageUnitFTCandidate ? "true" : "false").append(", '").append(i).append("')\">&nbsp;").append(labelpath);
                        sb.append("</td>");
                        sb.append("<td class='maintform_field' valign='top' rowspan=").append(rowspan).append(" align=center colspan=4 style='padding:4px'>");
                        if (isTrackItemFTCandidate) {
                            sb.append("<table cellpadding=2 cellspacing=0 border=0 id='__res_table_").append(i).append("' style='display:").append(isStorageUnitFTCandidate ? "block" : "none").append("'>");
                            sb.append("    <tr>");
                            sb.append("        <td class='maintform_field' width='40' align=center>");
                            sb.append("            <input type=checkbox id='__res_thawed_").append(i).append("' ").append(isStorageUnitFTCandidate ? "checked" : "").append(">");
                            sb.append("        </td>");
                            sb.append("        <td class='maintform_field' width='50'>");
                            sb.append("            <input type=hidden id='__res_initftcount_").append(i).append("' value='").append(freezethawcount).append("'>");
                            sb.append("            <input type=hidden id='__res_ftcount_").append(i).append("' value='").append(freezethawcount).append("'>");
                            sb.append("            <table cellpadding=0 cellspacing=0 border=0>");
                            sb.append("                <tr>");
                            sb.append("                    <td rowspan=2><img src='WEB-OPAL/images/").append(ftimage).append("'></td>");
                            sb.append("                    <td rowspan=2>");
                            sb.append("                        <input style='width:32px;border:1px solid gray' readonly value='").append(freezethawcount).append("' id='__res_ftcountdisplay_").append(i).append("'>");
                            sb.append("                    </td>");
                            sb.append("                    <td width=10><img src='WEB-CORE/elements/images/up.gif' style='cursor:pointer' title='Raise FT Count' onclick=\"_nav_frame1.__res_raiseFTCount('").append(i).append("', true)\"></td>");
                            sb.append("                </tr><tr>");
                            sb.append("                <td width=10><img src='WEB-CORE/elements/images/down.gif' style='cursor:pointer' title='Lower FT Count' onclick=\"_nav_frame1.__res_raiseFTCount('").append(i).append("', false)\"></td>");
                            sb.append("            </tr></table>");
                            sb.append("        </td>");
                            sb.append("        <td class='maintform_field' width='40' align=center>").append(freezethawcountwarn).append("</td>");
                            sb.append("        <td class='maintform_field' width='40' align=center>").append(freezethawcountmax).append("</td>");
                            sb.append("    </tr>");
                            sb.append("</table>&nbsp;");
                        } else {
                            sb.append("&nbsp;");
                        }
                        sb.append("</td>");
                    } else {
                        sb.append("<td class='maintform_field' valign='top' style='padding:4px'>");
                        sb.append("    <input type='radio' name='__res_storageunitlabel_").append(i).append("' onclick=\"_nav_frame1.__res_selectReserveUnit('").append(storageunitid).append("', '").append(labelpath).append("', ").append(isStorageUnitFTCandidate ? "true" : "false").append(", '").append(i).append("')\">&nbsp;").append(labelpath);
                        sb.append("</td>");
                    }
                    sb.append("</tr>");
                }
            }
            sb.append("</table>");
            sb.append("<input type=hidden id='__res_rows' value='").append(list.size()).append("'>");
        } else {
            success = "N";
            sb.append(this.getTranslationProcessor().translate("None of the selected trackitems are reserved."));
        }
        ajaxResponse.addCallbackArgument("success", success);
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

