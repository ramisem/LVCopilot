/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.security;

import com.duosecurity.duoweb.DuoWeb;
import com.duosecurity.duoweb.DuoWebException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.ext.BaseWebMFAHandler;
import sapphire.util.ConnectionInfo;

public class DuoSecurity2FAHandler
extends BaseWebMFAHandler {
    @Override
    public void renderPrompt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String signedrequest = this.generateSignedRequest();
        PrintWriter output = response.getWriter();
        if (signedrequest.startsWith("ERR|")) {
            output.write("<h1>Error, bad Duo request: " + signedrequest + "</h1>");
        }
        String duoHost = this.getHost();
        String frameHtml = DuoSecurity2FAHandler.getFramePage(duoHost, signedrequest);
        output.write(frameHtml);
        output.close();
    }

    public String generateSignedRequest() {
        return this.generateSignedRequest(this.username);
    }

    public String generateSignedRequest(String logonName) {
        String ikey = this.getAuthenticationProps().getProperty("iKey");
        String skey = this.getAuthenticationProps().getDecryptedProperty("sKey");
        String akey = this.getAuthenticationProps().getProperty("aKey");
        return DuoWeb.signRequest(ikey, skey, akey, logonName);
    }

    public String getHost() {
        return this.getAuthenticationProps().getProperty("apihostname");
    }

    @Override
    public boolean verifyResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String duoResponse = request.getParameter("sig_response");
        this.verifySignedResponse(duoResponse);
        return true;
    }

    public String verifySignedResponse(String duoResponse) throws DuoWebException, NoSuchAlgorithmException, IOException, InvalidKeyException, SapphireException {
        ConnectionInfo connectionInfo = this.getConnectionInfo();
        String ikey = this.getAuthenticationProps().getProperty("iKey");
        String skey = this.getAuthenticationProps().getDecryptedProperty("sKey");
        String akey = this.getAuthenticationProps().getProperty("aKey");
        return DuoWeb.verifyResponse(ikey, skey, akey, duoResponse);
    }

    private static String getFramePage(String host, String request) {
        String eol = System.getProperty("line.separator");
        String framePage = "<!DOCTYPE html>" + eol + "<html>" + eol + "  <head>" + eol + "    <title>Duo Authentication Prompt</title>" + eol + "    <meta name='viewport' content='width=device-width, initial-scale=1'>" + eol + "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + eol + "    <link rel='stylesheet' type='text/css' href='/labvantage/Duo-Frame.css'>" + eol + "  </head>" + eol + "  <body>" + eol + "    <h1>Duo Authentication Prompt</h1>" + eol + "    <script src='/labvantage/Duo-Web-v2.js'></script>" + eol + "    <iframe id='duo_iframe'" + eol + "            title='Two-Factor Authentication'" + eol + "            frameborder='0'" + eol + "            data-host='" + host + "'" + eol + "            data-sig-request='" + request + "'" + eol + "    </iframe>" + eol + "  </body>" + eol + "</html>";
        return framePage;
    }
}

