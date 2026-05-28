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

import com.labvantage.opal.validation.BaseValidation;
import com.labvantage.sapphire.admin.ddt.rules.SMSUser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetDepartments
extends BaseValidation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53255 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        StringBuffer sb = new StringBuffer();
        StringBuffer sql = new StringBuffer();
        String columnid = request.getParameter("columnid");
        String userid = request.getParameter("userid");
        SafeSQL safeSQL = null;
        if (userid != null && userid.trim().length() > 0) {
            columnid = "departmentid";
            if (userid.equals("[currentuser]")) {
                userid = this.getSysUserId();
            }
            safeSQL = new SafeSQL();
            sql.append("select departmentid from departmentsysuser where sysuserid = ").append(safeSQL.addVar(userid));
        } else {
            if (columnid == null || columnid.length() == 0) {
                columnid = "departmentid";
            }
            sql.append("select ").append(columnid).append(" from department order by departmentid");
        }
        if (this.getSysUserId().equals(userid)) {
            sb.append(this.getSapphireConnection().getDepartmentList());
        } else {
            DataSet ds = safeSQL == null ? this.getQueryProcessor().getSqlDataSet(sql.toString()) : this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            int defaultRow = -1;
            if (ds != null) {
                if (userid != null && userid.length() > 0) {
                    String defaultDept = SMSUser.getDefaultDepartment(this.getQueryProcessor(), userid);
                    HashMap<String, String> findDef = new HashMap<String, String>();
                    findDef.put("departmentid", defaultDept);
                    defaultRow = ds.findRow(findDef);
                    if (defaultRow >= 0) {
                        sb.append(defaultDept);
                        sb.append(";");
                    }
                }
                for (int i = 0; i < ds.size(); ++i) {
                    if (i == defaultRow) continue;
                    sb.append(ds.getValue(i, columnid));
                    sb.append(";");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
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

