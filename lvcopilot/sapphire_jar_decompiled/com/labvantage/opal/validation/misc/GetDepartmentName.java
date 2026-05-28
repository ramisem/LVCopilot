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

public class GetDepartmentName
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53254 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String departmentid = request.getParameter("departmentid");
        String departmentname = "";
        if (departmentid != null && departmentid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(new StringBuffer().append("select departmentdesc from department where departmentid = ").append(safeSQL.addVar(departmentid)).toString(), safeSQL.getValues());
            if (ds != null && (departmentname = ds.getString(0, "departmentdesc")) == null) {
                departmentname = "";
            }
        }
        try {
            PrintWriter out = response.getWriter();
            out.write(departmentname);
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

