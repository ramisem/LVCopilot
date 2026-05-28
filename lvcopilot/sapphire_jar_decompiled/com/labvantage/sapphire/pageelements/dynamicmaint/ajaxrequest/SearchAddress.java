/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.ExtraColumnUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SearchAddress
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        Object[] params;
        String sql;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String addressid = ar.getRequestParameter("addressid", "");
        String addresstype = ar.getRequestParameter("addresstype", "");
        String elementid = ar.getRequestParameter("elementid", "");
        String focusFieldId = ar.getRequestParameter("focusfieldid", "");
        String pageId = ar.getRequestParameter("pageid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        String keyid2 = ar.getRequestParameter("keyid2", "[keyid2]");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        if (!addressid.equals("") && !addresstype.equals("")) {
            sql = "SELECT addressid, addresstype FROM address WHERE addressid=? AND addresstype=?";
            params = new String[]{addressid, addresstype};
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        } else if (!addressid.equals("") && addresstype.equals("")) {
            sql = "SELECT addressid, addresstype FROM address WHERE addressid=?";
            params = new String[]{addressid};
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        } else {
            ds = new DataSet();
        }
        if (ds != null) {
            Utils.fillKeyids(ds, keyid1, keyid2, keyid3);
            HttpSession session = req.getSession();
            PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
            ExtraColumnUtil extraColumnUtil = new ExtraColumnUtil(this.getQueryProcessor(), this.logger);
            Map<String, List<String>> allExtraColumns = extraColumnUtil.parseExtraColumns(pageProps);
            List<String> extraColumns = allExtraColumns.get("sdiaddress");
            if (extraColumns != null) {
                extraColumnUtil.fillExtraColumnData(extraColumns, ds, "sdiaddress");
            }
        }
        ar.addCallbackArgument("address", ds);
        ar.addCallbackArgument("elementid", elementid);
        ar.addCallbackArgument("focusFieldId", focusFieldId);
        ar.print();
    }
}

