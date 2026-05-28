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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class SearchPhrase
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String inputText = ar.getRequestParameter("text", "");
        String phraseId = "";
        String phraseText = "";
        if (!inputText.equals("")) {
            String[] arr = inputText.split(" ");
            String searchparam = arr[arr.length - 1];
            String sql = "SELECT phraseshortcut, phrasetext FROM phrase WHERE lower(phraseshortcut)=lower(?) AND coalesce(activeflag,'Y')='Y' ";
            Object[] params = new String[]{searchparam};
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
            if (ds != null && ds.getRowCount() > 0) {
                phraseId = ds.getString(0, "phraseshortcut", "");
                phraseText = ds.getString(0, "phrasetext", "");
            }
        }
        ar.addCallbackArgument("phraseid", phraseId);
        ar.addCallbackArgument("phrasetext", phraseText);
        ar.print();
    }
}

