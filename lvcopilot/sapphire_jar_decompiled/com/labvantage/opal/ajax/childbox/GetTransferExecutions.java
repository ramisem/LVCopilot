/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childbox;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetTransferExecutions
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String transfermethodid = ajaxResponse.getRequestParameter("transfermethodid", "");
        String transfermethodversionid = ajaxResponse.getRequestParameter("transfermethodversionid", "");
        String transferexecutionid = "";
        if (OpalUtil.isNotEmpty(transfermethodid) && OpalUtil.isNotEmpty(transfermethodversionid)) {
            try {
                String rsetid = this.getDAMProcessor().createRSet("LV_ArrayTransferMethod", StringUtil.replaceAll(transfermethodid, "%3B", ";"), StringUtil.replaceAll(transfermethodversionid, "%3B", ";"), null);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select transferexecution.transferexecutionid from transferexecution, rsetitems where arraytransfermethodid = rsetitems.keyid1 and arraytransfermethodversionid = rsetitems.keyid2 and rsetitems.rsetid = ?", (Object[])new String[]{rsetid});
                if (ds != null) {
                    transferexecutionid = ds.getColumnValues("transferexecutionid", ";");
                }
                this.getDAMProcessor().clearRSet(rsetid);
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
        if (OpalUtil.isEmpty(transferexecutionid)) {
            message = this.getTranslationProcessor().translate("No Executions found for selected Transfer Methods");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("transferexecutionid", transferexecutionid);
        ajaxResponse.print();
    }
}

