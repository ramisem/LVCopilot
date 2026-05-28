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

public class GetDefaultDepartment
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53253 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String defaultdepartment = "";
        String userid = request.getParameter("userid");
        if (userid == null || userid.trim().length() == 0) {
            userid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
        }
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select defaultdepartment from sysuser where sysuserid = ").append(safeSQL.addVar(userid));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0 && (defaultdepartment = ds.getValue(0, "defaultdepartment")) != null) {
            defaultdepartment = defaultdepartment.trim();
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(defaultdepartment);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

