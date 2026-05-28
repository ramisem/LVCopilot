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

import com.labvantage.opal.elements.treelist.TreeList;
import com.labvantage.opal.util.OpalUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetTreeUnitNodeHtml
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 72690 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            ArrayList<String> expandnodelist = new ArrayList<String>();
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");
            String level = ajaxResponse.getRequestParameter("level", "0");
            String className = ajaxResponse.getRequestParameter("classname");
            String imageid = ajaxResponse.getRequestParameter("imageid");
            boolean autoexpand = "Y".equals(ajaxResponse.getRequestParameter("autoexpand"));
            int nextLevel = Integer.parseInt(level) + 1;
            boolean even = className.endsWith("odd");
            String __element = null;
            try {
                __element = URLDecoder.decode(ajaxResponse.getRequestParameter("element"), "utf-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                JSONObject json = new JSONObject(__element);
                PropertyList element = new PropertyList(json);
                int expand_level = 0;
                try {
                    expand_level = Integer.parseInt(element.getProperty("expandlevel", "0"));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                String appearance = element.getProperty("appearance");
                String keycolid1 = element.getProperty("keycolid1");
                DataSet ds = this.getExpandNodeDataSet(element, keyid1);
                StringBuilder sb = new StringBuilder();
                if (ds != null && ds.size() > 0) {
                    for (int row = 0; row < ds.size(); ++row) {
                        String nodeid = "tn_" + StringUtil.replaceAll(ds.getString(row, keycolid1), " ", "_");
                        sb.append("<tr name='list_tablerow' rownum='").append(row).append("' class='list_");
                        sb.append(appearance).append("tablerow");
                        sb.append(even ? "even" : "odd");
                        sb.append("' level='").append(nextLevel).append("'");
                        sb.append(" imgid=\"").append(nodeid).append("\">");
                        sb.append(TreeList.getRowHtml(nodeid, element, ds, row, nextLevel, this.getTranslationProcessor()));
                        sb.append("</tr>");
                        if (autoexpand && (expand_level == -1 || nextLevel < expand_level)) {
                            expandnodelist.add(nodeid);
                        }
                        even = !even;
                    }
                }
                ajaxResponse.addCallbackArgument("keyid1", keyid1);
                ajaxResponse.addCallbackArgument("html", sb.toString());
                ajaxResponse.addCallbackArgument("imageid", imageid);
                ajaxResponse.addCallbackArgument("expandnode", OpalUtil.toDelimitedString(expandnodelist, ";"));
            }
            catch (Exception e1) {
                this.logger.error("Error", e1);
                ajaxResponse.setError("Error", e1);
            }
        }
        finally {
            ajaxResponse.print();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DataSet getExpandNodeDataSet(PropertyList element, String keyid1) {
        DataSet rootds;
        StringBuilder sql = new StringBuilder();
        String keycolid1 = element.getProperty("keycolid1");
        String parentcolumnid = element.getProperty("parentcolumnid");
        List<String> rsetkeys = OpalUtil.toList((String)element.get("rsetkeys"), ";");
        if (rsetkeys == null) {
            rsetkeys = new ArrayList<String>();
        }
        String expandrestrictivewhere = element.getProperty("expandrestrictivewhere", "");
        String querywhere = parentcolumnid + " = '" + SafeSQL.encodeForSQL(keyid1, this.getConnectionProcessor().isOra()) + "'";
        if (expandrestrictivewhere.trim().length() > 0) {
            querywhere = querywhere + " and " + expandrestrictivewhere;
        }
        String rsetid = "";
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(element.getProperty("sdcid"));
        sdiRequest.setQueryFrom(element.getProperty("tableid"));
        sdiRequest.setQueryWhere(querywhere);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRetainRsetid(true);
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        if (sdiData != null) {
            rsetid = sdiData.getRsetid();
        }
        try {
            SafeSQL safeSQL = new SafeSQL();
            sql.append(element.getProperty("selectclause"));
            sql.append(" from ").append(element.getProperty("tableid"));
            sql.append(" where ").append(keycolid1).append(" in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(element.getProperty("orderbyclause"));
            rootds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (rootds != null) {
                rootds.addColumn("searchresult", 0);
                int size = rootds.size();
                for (int i = 0; i < size; ++i) {
                    rootds.setString(i, "searchresult", rsetkeys.contains(rootds.getValue(i, keycolid1)) ? "Y" : "N");
                }
            }
        }
        finally {
            if (StringUtil.getLen(rsetid) > 0L) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        return rootds;
    }
}

