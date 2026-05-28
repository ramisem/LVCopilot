/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.PasswordException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class ChangePassword
extends BaseRequest {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String querystring = request.getQueryString();
        querystring = HttpUtil.decodeURIComponent(querystring);
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        if ("reset2fa".equals(request.getParameter("mode"))) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("keyid1", connectionInfo.getSysuserId());
            props.setProperty("mfasecretkey", "(null)");
            try {
                new ActionProcessor(this.getConnectionId()).processAction("EditSDI", "1", props);
                new HttpUtil(request, response).removeCookie("rememberdevice");
                response.getWriter().write("2FA reset successfully.");
            }
            catch (Exception exception) {}
        } else {
            String nexturl = request.getParameter("nexturl") != null ? request.getParameter("nexturl") : querystring.substring(querystring.indexOf("nexturl=") + 8);
            throw new PasswordException("CHANGE_PASSWORD", "Change password", nexturl, connectionInfo.getSysuserId(), connectionInfo.getPassword(), connectionInfo.getDatabaseId());
        }
    }
}

