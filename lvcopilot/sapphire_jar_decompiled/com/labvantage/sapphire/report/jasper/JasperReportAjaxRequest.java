/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.JasperReportPropertyHandler;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class JasperReportAjaxRequest
extends BaseRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String mode = request.getParameter("mode");
        try {
            if ("savejrxml".equals(mode)) {
                HashMap requestProps = new RequestProcessor(requestContext.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), HttpUtil.getRequestMap((ServletRequest)request));
                response.getWriter().write("savejrxmldone");
            } else if ("retrievejrxml".equals(mode)) {
                String reportid = request.getParameter("reportid");
                SafeSQL safeSQL = new SafeSQL();
                QueryProcessor qp = this.getQueryProcessor();
                String sql = "select * from report where reportid=" + safeSQL.addVar(reportid);
                DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
                String xml = ds.getClob(0, "objectsyntax", "default xml");
                response.getWriter().write(xml);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }
}

