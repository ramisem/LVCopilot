/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class AddRemoveCategory
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54855 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcId = ajaxResponse.getRequestParameter("sdcid");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String categoryId = ajaxResponse.getRequestParameter("categoryid");
        String operation = ajaxResponse.getRequestParameter("operation");
        String sql = "";
        QueryProcessor qp = this.getQueryProcessor();
        try {
            if (operation.equalsIgnoreCase("add")) {
                DataSet ds = qp.getPreparedSqlDataSet("select * from category where categoryid=? and sdcid=?", new Object[]{categoryId, sdcId});
                if (ds.size() <= 0) {
                    sql = "insert into category (categoryid, sdcid) values (?, ?)";
                    qp.execPreparedUpdate(sql, new Object[]{categoryId, sdcId});
                    message = "<font color=green>Added Category</font>";
                }
                sql = "insert into categoryitem (categoryid, sdcid, keyid1, usersequence) values (?, ?, ?, 0)";
                qp.execPreparedUpdate(sql, new Object[]{categoryId, sdcId, keyid1});
                message = message.length() > 0 ? message + "<br><font color=green>Added CategoryItem</font>" : "<font color=green>Added Category</font>";
            } else if (operation.equalsIgnoreCase("remove")) {
                sql = "delete from categoryitem where categoryid = ? and sdcid = ? and keyid1 = ?";
                qp.execPreparedUpdate(sql, new Object[]{categoryId, sdcId, keyid1});
                message = "<font color=green>Removed CategoryItem</font>";
            }
        }
        catch (Exception e) {
            this.logError("AddRemoveCategory.processRequest -> Got an exception during " + operation + " category for sdcId=" + sdcId + ", keyid1=" + keyid1 + ", categoryid=" + categoryId + ". Exception: " + e);
            message = "<font color=red>" + e.toString() + "</font>";
        }
        finally {
            ajaxResponse.addCallbackArgument("itemid", sdcId + "_" + keyid1);
            ajaxResponse.addCallbackArgument("message", message);
            ajaxResponse.print();
        }
    }
}

