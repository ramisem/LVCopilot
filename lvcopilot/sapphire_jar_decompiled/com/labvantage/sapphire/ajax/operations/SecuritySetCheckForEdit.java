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

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.SecuritySetUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class SecuritySetCheckForEdit
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        String securitySetId = ajaxResponse.getRequestParameter("securitysetid");
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        String currentUser = sapphireConnection.getSysuserId();
        String currentUserJobType = sapphireConnection.getCurrentJobtype();
        boolean editable = false;
        String viewMessage = "";
        TranslationProcessor tp = this.getTranslationProcessor();
        if (securitySetId.length() > 0) {
            DataSet ds = qp.getPreparedSqlDataSet("select 1 from securityset where securitysetid = ?", new Object[]{securitySetId});
            if (ds.getRowCount() == 0) {
                ajaxResponse.setError(tp.translate("SecuritySet Id is not valid!"));
            } else {
                try {
                    editable = SecuritySetUtil.isSecuritySetOperationPermitted(securitySetId, currentUser, currentUserJobType, "LV_SecuritySet", "Admin", qp, sapphireConnection);
                }
                catch (SapphireException e) {
                    ajaxResponse.setError(e.getMessage());
                }
                if (!editable) {
                    viewMessage = tp.translate("You are not allowed to edit this SecuritySet. Would you like to view it?") + " ";
                }
                ajaxResponse.addCallbackArgument("message", viewMessage);
                ajaxResponse.addCallbackArgument("securitysetid", securitySetId);
            }
        } else {
            ajaxResponse.setError(tp.translate("SecuritySet Id not specified!"));
        }
        ajaxResponse.print();
    }
}

