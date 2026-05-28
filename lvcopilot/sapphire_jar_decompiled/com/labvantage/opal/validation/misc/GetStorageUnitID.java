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

public class GetStorageUnitID
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54496 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String value = "";
        String sdcid = request.getParameter("sdcid");
        String keyid1 = request.getParameter("keyid1");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where linkkeyid1 = ? and linksdcid = ?", (Object[])new String[]{keyid1, sdcid});
        if (ds != null) {
            value = ds.getValue(0, "storageunitid");
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(value);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

