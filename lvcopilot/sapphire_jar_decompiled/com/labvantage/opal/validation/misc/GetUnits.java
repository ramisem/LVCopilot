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

public class GetUnits
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54554 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        StringBuffer sbunits = new StringBuffer();
        DataSet ds = this.getQueryProcessor().getSqlDataSet("select unitsid from units");
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                sbunits.append(ds.getValue(i, "unitsid"));
                sbunits.append(";");
            }
            if (sbunits.length() > 0) {
                sbunits.setLength(sbunits.length() - 1);
            }
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(sbunits.toString());
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

