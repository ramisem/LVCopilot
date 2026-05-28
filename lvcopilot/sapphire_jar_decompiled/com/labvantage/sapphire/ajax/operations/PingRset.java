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
import sapphire.accessor.DAMProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class PingRset
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "sapphire.connection.pingRsetHandler");
        if (ajaxResponse != null) {
            try {
                String rsetid = ajaxResponse.getRequestParameter("rsetid", "");
                if (rsetid != null && rsetid.length() > 0) {
                    boolean clear = ajaxResponse.getRequestParameter("releaselock", "N").equalsIgnoreCase("Y");
                    DAMProcessor dam = new DAMProcessor(this.getRakFile(), this.getConnectionId());
                    String[] rsetidarray = StringUtil.split(rsetid, "|");
                    for (int i = 0; i < rsetidarray.length; ++i) {
                        if (clear) {
                            dam.clearRSet(rsetidarray[i]);
                            continue;
                        }
                        dam.touchRSet(rsetidarray[i]);
                    }
                    ajaxResponse.addCallbackArgument("rsetid", "" + rsetid);
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not create JSON Object."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No Properties provided."));
        }
        ajaxResponse.print();
    }
}

