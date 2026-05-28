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

public class GetWebPageNames
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65752 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String name = request.getParameter("name");
        StringBuffer sb = new StringBuffer();
        if (name != null) {
            StringBuffer sql = new StringBuffer();
            String bindvariable = "%" + name.toLowerCase() + "%";
            sql.append("select w.webpageid, w.productedition from webpage w where lower(w.webpageid) like ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{bindvariable});
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    sb.append(";").append(ds.getValue(i, "webpageid")).append("|").append(ds.getValue(i, "productedition"));
                }
            }
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(sb.length() > 0 ? sb.substring(1) : "");
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

