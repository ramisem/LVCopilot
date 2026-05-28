/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class ReceiveSamples
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String accessionid = UUID.randomUUID().toString();
        String data = ajaxResponse.getRequestParameter("data");
        String samplefamilynotes = ajaxResponse.getRequestParameter("samplefamilynotes", "");
        String sdcruleconfirm = ajaxResponse.getRequestParameter("sdcruleconfirm", "N");
        PropertyList props = new PropertyList();
        props.setProperty("actionid", "AccessionSamples");
        props.setProperty("actionversionid", "1");
        props.setProperty("processassysuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        props.setProperty("accessionid", accessionid);
        props.setProperty("data", data);
        props.setProperty("samplefamilynotes", samplefamilynotes);
        props.setProperty("sdcruleconfirm", sdcruleconfirm);
        props.setProperty("attributes", ajaxResponse.getRequestParameter("attributes", ""));
        try {
            this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props);
        }
        catch (ActionException e) {
            message = this.getTranslationProcessor().translate("Unable to accession sample. Please try again.");
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("accessionid", accessionid);
        ajaxResponse.print();
    }
}

