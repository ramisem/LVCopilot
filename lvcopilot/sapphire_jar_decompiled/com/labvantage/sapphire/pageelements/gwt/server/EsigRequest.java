/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.util.json.JSONUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EsigRequest
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String requestCommand = request.getParameter("requestcommand");
            PropertyList requestData = new PropertyList(new JSONObject(request.getParameter(requestCommand)));
            if ("checkuser".equalsIgnoreCase(requestCommand)) {
                ConnectionProcessor cp = this.getConnectionProcessor();
                String password = requestData.getProperty("password");
                if (password != null && password.indexOf("{|}") == 0) {
                    password = EncryptDecrypt.decryptRSA(password.substring("{|}".length()));
                }
                if (cp.checkUser(requestData.getProperty("sysuserid"), password)) {
                    this.write("OK");
                } else {
                    this.write("FAIL");
                }
            } else if ("getreasons".equalsIgnoreCase(requestCommand)) {
                DataSet refdata = this.getQueryProcessor().getRefTypeDataSet(requestData.getProperty("reftypeid"));
                this.write(JSONUtil.toJSONString(refdata));
            }
        }
        catch (Exception e) {
            this.write("Error processing request: " + e.getMessage());
            this.logger.error("Error processing request: " + e.getMessage(), e);
        }
    }
}

