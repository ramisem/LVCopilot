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
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetSubjectDetails
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53259 $";
    private static final String MODE = "mode";
    private static final String MODE_SPECIES = "species";
    private static final String MODE_STRAIN = "strain";
    private static final String PROPERTY_SPECIESID = "speciesid";
    private static final String COLUMN_SPECIESID = "s_speciesid";
    private static final String COLUMN_STRAINID = "s_strainid";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String mode = request.getParameter(MODE);
        String result = null;
        QueryProcessor qp = this.getQueryProcessor();
        DataSet ds = null;
        if (mode != null) {
            String speciesid;
            if (mode.equals(MODE_SPECIES)) {
                ds = qp.getSqlDataSet("select s_speciesid from s_species order by s_speciesid");
                if (ds != null) {
                    result = ds.getColumnValues(COLUMN_SPECIESID, ";");
                }
            } else if (mode.equals(MODE_STRAIN) && (speciesid = request.getParameter(PROPERTY_SPECIESID)) != null && speciesid.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                ds = qp.getPreparedSqlDataSet("select s_strainid from s_strain where speciesid=" + safeSQL.addVar(speciesid) + " order by s_strainid", safeSQL.getValues());
                if (ds != null) {
                    result = ds.getColumnValues(COLUMN_STRAINID, ";");
                }
            }
        }
        if (result != null) {
            try {
                PrintWriter out = response.getWriter();
                out.write(result);
            }
            catch (IOException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
    }
}

