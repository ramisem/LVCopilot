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

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class CancelSessionExpiry
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            try {
                ServiceLocator.getInstance().getTDQManager().writeConnectionCache(sapphireConnection.getConnectionId());
                Trace.log("Updated last accesseddt in database");
            }
            catch (Throwable t) {
                Trace.logWarn(t.getMessage());
            }
            ajaxResponse.addCallbackArgument("status", "OK");
        }
        finally {
            ajaxResponse.print();
        }
    }
}

