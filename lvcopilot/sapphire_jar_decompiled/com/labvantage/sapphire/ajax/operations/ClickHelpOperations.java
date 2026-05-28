/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.ajax.operations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ClickHelpOperations
extends BaseAjaxRequest {
    protected PageContext pageContext;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String helpurlroot = ajaxResponse.getRequestParameter("helpurlroot");
        String completeClickHelpUrl = "";
        try {
            String tokenClickHelp = "";
            tokenClickHelp = this.getToken();
            completeClickHelpUrl = helpurlroot + "?t=" + tokenClickHelp;
        }
        catch (Exception e) {
            this.logError("Failed to get help gizmo data. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.addCallbackArgument("message", completeClickHelpUrl);
        ajaxResponse.print();
    }

    private String getToken() {
        String tokenClickHelpTemp = "";
        try {
            String output;
            URL urlClickHelp = new URL("https://labvantage.clickhelp.co/api/v1/users/lvdocumentation/tokens?exp=");
            HttpURLConnection conn = (HttpURLConnection)urlClickHelp.openConnection();
            String plainCreds = "lvsadmin:J3wQSUX68n341jrp7JcX227Z";
            byte[] encodedAuth = Base64.encodeBase64((byte[])plainCreds.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String jsonOutput = "";
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                jsonOutput = output;
            }
            JSONObject obj = new JSONObject(jsonOutput);
            tokenClickHelpTemp = obj.getString("token");
            conn.disconnect();
        }
        catch (Exception e) {
            this.logError("Failed to get token. Reason: " + e.getMessage(), e);
        }
        return tokenClickHelpTemp;
    }

    private boolean isHelpURLReachable(String helpRootURL, HttpServletRequest request) {
        block6: {
            try {
                HttpSession session = request.getSession();
                String helpURLFlag = (String)session.getAttribute("helpurlaccessibleflag");
                if (helpURLFlag == null) {
                    URL url = new URL(helpRootURL + "/index.htm");
                    try {
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("GET");
                        if (connection.getResponseCode() == 200) {
                            session.setAttribute("helpurlaccessibleflag", (Object)"Y");
                            return true;
                        }
                        session.setAttribute("helpurlaccessibleflag", (Object)"N");
                    }
                    catch (Exception e) {
                        session.setAttribute("helpurlaccessibleflag", (Object)"N");
                        this.logError("Failed to establish connection to help application using " + helpRootURL + "/index.htm", e);
                    }
                    break block6;
                }
                return "Y".equals(helpURLFlag);
            }
            catch (Exception e) {
                this.logError("Exception occurred when trying to establish connection to help application using " + helpRootURL + "/index.htm");
            }
        }
        return false;
    }
}

