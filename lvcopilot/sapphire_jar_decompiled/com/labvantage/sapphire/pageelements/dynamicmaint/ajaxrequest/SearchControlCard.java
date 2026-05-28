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

public class SearchControlCard
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String controlCardId = ar.getRequestParameter("controlcardid", "");
        String elementid = ar.getRequestParameter("elementid", "");
        String focusFieldId = ar.getRequestParameter("focusfieldid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        String pageId = ar.getRequestParameter("pageid", "");
        if (!controlCardId.equals("")) {
            Object[] params = controlCardId.split(";");
            StringBuilder queryParams = new StringBuilder();
            for (String string : params) {
                queryParams.append(",?");
            }
            String sql = "SELECT spc_controlcardid, controlcarddesc FROM spc_controlcard WHERE spc_controlcardid IN (" + queryParams.toString().substring(1) + ")";
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
            List<String> extraColumns = allExtraColumns.get(elementid);
            if (extraColumns != null) {
                extraColumnUtil.fillExtraColumnData(extraColumns, ds, "spc_sdicontrolcard");
            }
        }
        ar.addCallbackArgument("controlCard", ds);
        ar.addCallbackArgument("elementid", elementid);
        ar.addCallbackArgument("focusFieldId", focusFieldId);
        ar.print();
    }
}

