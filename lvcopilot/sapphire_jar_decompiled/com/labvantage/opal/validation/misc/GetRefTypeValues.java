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

import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.DataSet;

public class GetRefTypeValues
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String reftypeid = request.getParameter("reftypeid");
        String values = "";
        if (reftypeid != null && reftypeid.length() > 0) {
            DataSet ds = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
            values = ds.getColumnValues("refvalueid", ";");
        }
        try {
            PrintWriter out = response.getWriter();
            out.println(values.toString());
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

