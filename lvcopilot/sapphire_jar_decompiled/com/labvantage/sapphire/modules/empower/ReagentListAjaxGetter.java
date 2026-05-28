/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ReagentListAjaxGetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "downloadMappingPage.doAdd_AjaxCallback");
        String lookupRet = ajaxResponse.getRequestParameter("lookupInfo");
        Trace.logDebug("Lookup returns:" + lookupRet);
        String[] rows = StringUtil.split(lookupRet, "%3B");
        if (rows.length == 0) {
            ajaxResponse.setError("No Consumables identified.");
        }
        String reagentLotIds = "";
        for (int i = 0; i < rows.length; ++i) {
            String[] tokens = StringUtil.split(rows[i], "|");
            String reagentlotid = tokens[0];
            reagentLotIds = i == 0 ? reagentlotid : reagentLotIds + DELIMITER + reagentlotid;
        }
        ajaxResponse.addCallbackArgument("sReagentLotIds", reagentLotIds);
        ajaxResponse.print();
    }
}

