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
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class SetSamplePointLocation
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String locationId = ar.getRequestParameter("locationid");
        String samplePointId = ar.getRequestParameter("samplepointid");
        if (locationId.isEmpty()) {
            locationId = "(null)";
        }
        if (!samplePointId.isEmpty()) {
            PropertyList editSDIProps = new PropertyList();
            editSDIProps.setProperty("sdcid", "SamplePoint");
            editSDIProps.setProperty("keyid1", samplePointId);
            editSDIProps.setProperty("locationid", locationId);
            try {
                this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
            }
            catch (ActionException e) {
                throw new ServletException("Cannot edit sample point(s) " + samplePointId, (Throwable)e);
            }
        }
        ar.print();
    }
}

