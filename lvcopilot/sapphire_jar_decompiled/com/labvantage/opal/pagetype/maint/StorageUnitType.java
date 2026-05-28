/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.pagetype.maint;

import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.DataSet;

public class StorageUnitType
extends BaseRequest {
    String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        String operationName = request.getParameter("operationname");
        if ("getsutypeinfo".equalsIgnoreCase(operationName)) {
            this.getStorageUnitTypeInfo(response);
        }
    }

    private void getStorageUnitTypeInfo(HttpServletResponse response) {
        PrintWriter out;
        StringBuffer output = new StringBuffer();
        try {
            out = response.getWriter();
        }
        catch (Exception ex) {
            this.logger.error(ex.getMessage(), ex);
            return;
        }
        output.append("<sappireajaxresponse>");
        try {
            DataSet nodeDS = new DataSet();
            DataSet nodeValidChildrenDS = new DataSet();
            StorageUnitUtil suUtil = new StorageUnitUtil(this.getConnectionId());
            suUtil.populateStorageUnitTypeDS(nodeDS, nodeValidChildrenDS);
            output.append("<status>SUCCESS</status>").append("<message></message>").append("<data>").append("<nodeds>").append("<rowcount>").append(nodeDS.size()).append("</rowcount>").append(nodeDS.toXML()).append("</nodeds>").append("<nodevalidachildrends>").append("<rowcount>").append(nodeValidChildrenDS.size()).append("</rowcount>").append(nodeValidChildrenDS.toXML()).append("</nodevalidachildrends>").append("</data>");
        }
        catch (Exception ex) {
            output.append("<status>FAIL</status>").append("<message>").append(ex.getMessage()).append("</message>").append("<data></data>");
        }
        output.append("</sappireajaxresponse>");
        out.println(output.toString());
        out.flush();
        out.close();
    }
}

