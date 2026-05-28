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

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateChangeLogChanges
extends BaseAjaxRequest {
    private String title = "";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "").trim();
        String keyid1 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", "").trim(), "%3B", ";");
        String keyid2 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid2", "").trim(), "%3B", ";");
        String keyid3 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid3", "").trim(), "%3B", ";");
        String propertyTreeNodeId = StringUtil.replaceAll(ajaxResponse.getRequestParameter("propertytreenodeid", "").trim(), "%3B", ";");
        ArrayList<String> list = new ArrayList<String>();
        if (sdcid.length() > 0 && keyid1.length() > 0) {
            int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            String[] key1array = StringUtil.split(keyid1, ";");
            String[] key2array = keycolumns > 1 ? StringUtil.split(keyid2, ";") : null;
            String[] key3array = keycolumns > 2 ? StringUtil.split(keyid3, ";") : null;
            String[] ptreeNodeArray = StringUtil.split(propertyTreeNodeId, ";");
            int index = 0;
            for (String key1 : key1array) {
                String changelogids = this.getChanges(sdcid, key1, key2array != null ? key2array[index] : null, key3array != null ? key3array[index] : null, ptreeNodeArray.length > index ? ptreeNodeArray[index] : null);
                if (OpalUtil.isNotEmpty(changelogids)) {
                    list.add(OpalUtil.toUniqueString(changelogids, ";"));
                }
                ++index;
            }
        }
        if (list.size() == 0) {
            message = this.getTranslationProcessor().translate("No Change Logs found for selected item(s)");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("changelogid", OpalUtil.toDelimitedString(list, ";"));
        ajaxResponse.addCallbackArgument("title", this.title);
        ajaxResponse.print();
    }

    private String getChanges(String sdcid, String keyid1, String keyid2, String keyid3, String propertyTreeNodeId) {
        StringBuilder html = new StringBuilder();
        if (!"LV_ChangeLog".equals(sdcid)) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select changelogid from changelog where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 = " + safeSQL.addVar(keyid1);
            if (OpalUtil.isNotEmpty(keyid2)) {
                sql = sql + " and linkkeyid2 = " + safeSQL.addVar(keyid2);
            }
            if (OpalUtil.isNotEmpty(keyid3)) {
                sql = sql + " and linkkeyid3 = " + safeSQL.addVar(keyid3);
            }
            if ("PropertyTree".equals(sdcid) && propertyTreeNodeId != null && propertyTreeNodeId.length() > 0 && !"__FULL".equals(propertyTreeNodeId)) {
                sql = sql + " AND propertytreenodeid = " + safeSQL.addVar(propertyTreeNodeId);
            }
            sql = sql + " order by checkedoutdt desc";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                this.title = this.getSDCProcessor().getProperty(sdcid, "singular", sdcid);
                this.title = this.title + " " + keyid1;
                this.title = this.title + (keyid2 != null && keyid2.length() > 0 ? " (" + keyid2 + ")" : "");
                this.title = this.title + (keyid3 != null && keyid3.length() > 0 ? " (" + keyid3 + ")" : "");
                html.append(ds.getColumnValues("changelogid", ";"));
            }
        } else {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select c1.changelogid, c1.linksdcid, c1.linkkeyid1, c1.linkkeyid2, c1.linkkeyid3 from changelog c1, changelog c2 where c1.linksdcid = c2.linksdcid and c1.linkkeyid1 = c2.linkkeyid1 and c1.linkkeyid2 = c2.linkkeyid2 and c1.linkkeyid3 = c2.linkkeyid3 and c2.changelogid = ? order by c1.checkedoutdt desc", (Object[])new String[]{keyid1});
            if (ds != null && ds.size() > 0) {
                String linksdcid = ds.getString(0, "linksdcid");
                String linkkeyid1 = ds.getString(0, "linkkeyid1", "");
                String linkkeyid2 = ds.getString(0, "linkkeyid2", "");
                String linkkeyid3 = ds.getString(0, "linkkeyid3", "");
                this.title = this.getSDCProcessor().getProperty(linksdcid, "singular", linksdcid);
                this.title = this.title + " " + linkkeyid1;
                this.title = this.title + (linkkeyid2.length() > 0 && !"(null)".equals(linkkeyid2) ? " (" + linkkeyid2 + ")" : "");
                this.title = this.title + (linkkeyid3.length() > 0 && !"(null)".equals(linkkeyid3) ? " (" + linkkeyid3 + ")" : "");
                html.append(ds.getColumnValues("changelogid", ";"));
            }
        }
        return html.toString();
    }
}

