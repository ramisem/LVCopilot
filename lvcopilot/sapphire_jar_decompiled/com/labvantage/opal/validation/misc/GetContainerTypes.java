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

public class GetContainerTypes
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 66109 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String incolumnid = request.getParameter("columnid");
        String columnid = "containertypeid";
        if (incolumnid != null && incolumnid.length() > 0) {
            columnid = this.getSDCProcessor().getSDCColumnProperty("ContainerType", incolumnid, "columnid");
        }
        if (columnid != null && columnid.length() > 0) {
            StringBuffer values = new StringBuffer();
            DataSet ds = this.getQueryProcessor().getSqlDataSet("select " + columnid + " from containertype order by containertypeid");
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    values.append(ds.getValue(i, columnid));
                    values.append(";");
                }
                if (values.length() > 0) {
                    values.setLength(values.length() - 1);
                }
            }
            try {
                PrintWriter out = response.getWriter();
                out.print(values.toString());
                out.flush();
                out.close();
            }
            catch (IOException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
    }
}

