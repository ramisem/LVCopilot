/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.sample.BaseSampleValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.util.StringUtil;

public class DisposeSamplesValidation
extends BaseSampleValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        try {
            String sampleid = ajaxResponse.getRequestParameter("keyid1", "");
            if (sampleid != null && sampleid.trim().length() > 0) {
                String[] s;
                sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
                for (String value : s = StringUtil.split(sampleid, ";")) {
                    String storagestatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", "storagestatus", "s_sampleid = ?", new String[]{value});
                    if ("Disposed".equals(storagestatus)) {
                        throw new SapphireException(this.translate("Sample is already Disposed") + " (" + value + ")");
                    }
                    if ("Allocated".equals(storagestatus) || "In Prep".equals(storagestatus)) {
                        OpalUtil.isSDIEditable(this.getQueryProcessor(), this.getSysUserId(), "Sample", value, true);
                        continue;
                    }
                    OpalUtil.isSDIEditable(this.getQueryProcessor(), this.getSysUserId(), "Sample", value, false);
                }
            }
        }
        catch (SapphireException e) {
            message = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

