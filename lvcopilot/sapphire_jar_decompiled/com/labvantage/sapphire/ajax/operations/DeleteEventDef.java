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
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteEventDef
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String eventDefId = ajaxResponse.getRequestParameter("eventid", "");
        String eventtype = ajaxResponse.getRequestParameter("eventtype", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (eventDefId.length() == 0) {
            ajaxResponse.setError(tp.translate("Event Id not known."));
            ajaxResponse.print();
        } else {
            try {
                if (eventtype.equalsIgnoreCase("timepoint")) {
                    this.deleteEventDef(eventDefId);
                    ajaxResponse.addCallbackArgument("response", tp.translate("Event Definition deleted successfully"));
                } else {
                    DataSet tpDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_eventdefid, eventdeflabel FROM s_eventdef WHERE parenteventdefid = ?", new Object[]{eventDefId});
                    this.deleteEventDef(tpDS.getColumnValues("s_eventdefid", ";"));
                    this.deleteEventDef(eventDefId);
                    ajaxResponse.addCallbackArgument("response", tp.translate("Event Definition deleted successfully"));
                }
            }
            catch (SapphireException e) {
                ajaxResponse.setError("Failed to process action. Exception: " + e.getMessage(), e);
            }
        }
        ajaxResponse.print();
    }

    private void deleteEventDef(String eventdefIds) throws SapphireException {
        if (eventdefIds != null && eventdefIds.length() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_EventDef");
            actionProps.setProperty("keyid1", eventdefIds);
            try {
                this.getActionProcessor().processAction("DeleteSDI", "1", actionProps);
            }
            catch (SapphireException e) {
                throw new SapphireException("Event(s) already exists for the selected definition(s): ");
            }
        }
    }
}

