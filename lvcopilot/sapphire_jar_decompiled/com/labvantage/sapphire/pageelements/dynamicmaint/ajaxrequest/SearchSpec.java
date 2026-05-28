/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class SearchSpec
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String inputText = ar.getRequestParameter("specid", "");
        String elementid = ar.getRequestParameter("elementid", "");
        String focusFieldId = ar.getRequestParameter("focusfieldid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        DataSet ds = new DataSet();
        if (!inputText.equals("")) {
            String[] specs;
            for (String spec : specs = inputText.split(";")) {
                DataSet dsSpec;
                String[] arr = spec.split("\\|");
                String specid = arr[0];
                if (arr.length > 1) {
                    String specversionid = arr[1];
                    if (specversionid.equals("C")) {
                        specversionid = this.getCurrentSpecVersion(specid);
                    }
                    String sql = "SELECT specid, specversionid FROM spec WHERE specid=? and specversionid=?";
                    Object[] params = new String[]{specid, specversionid};
                    dsSpec = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                } else {
                    String sql = "SELECT specid, specversionid FROM spec WHERE specid=? AND versionstatus='C' UNION ALL SELECT specid, specversionid FROM spec WHERE specid=? AND versionstatus='P' AND NOT EXISTS (SELECT 1 FROM spec WHERE specid=? AND versionstatus='C')";
                    Object[] params = new String[]{specid, specid, specid};
                    dsSpec = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                }
                ds.copyRow(dsSpec, 0, 1);
            }
        }
        ds.setSequence(0, ds.getRowCount(), "usersequence", 1);
        Utils.fillKeyids(ds, keyid1, keyid2, keyid3);
        ar.addCallbackArgument("spec", ds);
        ar.addCallbackArgument("elementid", elementid);
        ar.addCallbackArgument("focusFieldId", focusFieldId);
        ar.print();
    }

    private String getCurrentSpecVersion(String specid) {
        String sql = "SELECT x.specversionid FROM spec x WHERE x.specid=? AND x.versionstatus='C' UNION ALL SELECT max(x.specversionid) FROM spec x WHERE x.specid=? AND x.versionstatus='P' AND NOT EXISTS (SELECT 1 FROM spec z WHERE z.specid=? AND z.versionstatus='C') ";
        Object[] params = new String[]{specid, specid, specid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        return ds.getString(0, "specversionid", "");
    }
}

