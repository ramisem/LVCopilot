/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class GetSDIValue
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String value = "";
        String sdcid = request.getParameter("sdcid");
        String keyid1 = request.getParameter("keyid1");
        String keyid2 = request.getParameter("keyid2");
        String keyid3 = request.getParameter("keyid3");
        String columnid = request.getParameter("columnid");
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        if (sdi.isValid()) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList(sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3());
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            sdiData.getDataset("primary");
            value = sdiData.getDataset("primary").getValue(0, columnid);
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(value);
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

