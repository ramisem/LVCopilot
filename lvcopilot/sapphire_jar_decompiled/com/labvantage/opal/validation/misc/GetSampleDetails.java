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

public class GetSampleDetails
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53257 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String mode = request.getParameter("mode");
        StringBuffer sb = new StringBuffer();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if ("destsampletype".equals(mode)) {
            String sampleid = request.getParameter("sampleid");
            sql.append("select distinct destsampletypeid from s_preptypesampletypemap ");
            sql.append("where sourcesampletypeid = ( select sampletypeid from s_sample where s_sampleid = ");
            sql.append(safeSQL.addVar(sampleid));
            sql.append(" )");
        } else if ("preptype".equals(mode)) {
            String sampletypeid = request.getParameter("sampletypeid");
            String sampleid = request.getParameter("sampleid");
            sql.append("select distinct s_preptypeid from s_preptypesampletypemap ");
            sql.append(" where destsampletypeid = ").append(safeSQL.addVar(sampletypeid));
            sql.append(" and sourcesampletypeid = ( select sampletypeid from s_sample where s_sampleid = ").append(safeSQL.addVar(sampleid)).append(" )");
        } else if ("treatmenttype".equals(mode)) {
            String preptypeid = request.getParameter("preptypeid");
            sql.append("select s_treatmenttypeid from s_preptypetreatmenttypemap where s_preptypeid = ");
            sql.append(safeSQL.addVar(preptypeid));
        }
        try {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    sb.append(ds.getValue(i, ds.getColumnId(0)));
                    sb.append(";");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
            }
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

