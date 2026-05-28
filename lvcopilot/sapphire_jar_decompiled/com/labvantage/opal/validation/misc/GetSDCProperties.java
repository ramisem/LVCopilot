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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;

public class GetSDCProperties
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String sdcid = request.getParameter("sdcid");
            String requestType = request.getParameter("mode");
            String outText = "";
            if (requestType.length() == 0) {
                requestType = "getsdcproperties";
            }
            SDCProcessor sdcp = this.getSDCProcessor();
            DataSet dsLinksData = sdcp.getLinksData(sdcid);
            outText = dsLinksData.toXML();
            if (requestType.equalsIgnoreCase("getlinksdata")) {
                outText = dsLinksData.toXML();
            } else if (requestType.equalsIgnoreCase("getcolumndata")) {
                DataSet dsColumnData = sdcp.getColumnData(sdcid);
                outText = dsColumnData.toXML();
            } else {
                HashMap hmProps = sdcp.getSDCProperties(sdcid);
                StringBuffer output = new StringBuffer();
                Set keyset = hmProps.keySet();
                output.append("<dataset>\n");
                output.append("  <columns>\n");
                for (String column : keyset) {
                    output.append("    <coldef id=\"").append(column).append("\" type=\"STRING\" />\n");
                }
                output.append("  </columns>\n");
                output.append("  <rows>\n");
                output.append("    <row>\n");
                for (String column : keyset) {
                    output.append("      <col id=\"").append(column).append("\"><![CDATA[").append(hmProps.get(column)).append("]]></col>\n");
                }
                output.append("    </row>\n");
                output.append("  </rows>\n");
                output.append("</dataset>\n");
                outText = output.toString();
            }
            out.print(outText);
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            out.print(e.getMessage());
        }
        finally {
            out.flush();
            out.close();
        }
    }
}

