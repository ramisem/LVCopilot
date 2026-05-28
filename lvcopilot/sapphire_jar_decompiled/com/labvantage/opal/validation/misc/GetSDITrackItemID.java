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
import sapphire.util.SafeSQL;

public class GetSDITrackItemID
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54495 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String sdcid = request.getParameter("sdcid");
        String keyid1 = request.getParameter("keyid1");
        StringBuilder sb = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select trackitemid from trackitem");
        sql.append(" where linksdcid = ").append(safeSQL.addVar(sdcid));
        sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(")");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                sb.append(ds.getValue(i, "trackitemid")).append(";");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(sb.toString());
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

