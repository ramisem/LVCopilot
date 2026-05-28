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
import sapphire.util.StringUtil;

public class ValidateSUMove
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 82137 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String error = "";
        String storageunitid = request.getParameter("storageunitid");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select moveableflag, labelpath from storageunit where storageunitid in (" + safeSQL.addIn(storageunitid, ";") + ")", safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ds.size(); ++i) {
                if ("Y".equals(ds.getValue(i, "moveableflag"))) continue;
                sb.append("{{Storageunit}} \"").append(ds.getValue(i, "labelpath")).append("\" {{is not moveable}}<br>");
            }
            error = sb.toString();
        }
        if (StringUtil.getLen(error) > 0L) {
            error = this.getTranslationProcessor().translatePartial(error);
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(error);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

