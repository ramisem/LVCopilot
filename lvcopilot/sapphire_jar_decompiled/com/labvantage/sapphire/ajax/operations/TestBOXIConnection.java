/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;

public class TestBOXIConnection
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89410 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionProcessor().getConnectionid());
        TranslationProcessor tp = this.getTranslationProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String profileuserid = ajaxResponse.getRequestParameter("profileuserid", "(system)");
        try {
            SapphireBOUtil sapphireBOUtil = new SapphireBOUtil();
            sapphireBOUtil.setBourl(cp.getProfileProperty(profileuserid, "bourl"));
            sapphireBOUtil.setBocmsname(cp.getProfileProperty(profileuserid, "bocmsname"));
            sapphireBOUtil.setBousername(cp.getProfileProperty(profileuserid, "bousername"));
            sapphireBOUtil.setBoauthenticationtype("sec" + cp.getProfileProperty(profileuserid, "boauthenticationtype"));
            String boConnectionTimeout = cp.getProfileProperty(profileuserid, "boconnectiontimeout");
            sapphireBOUtil.setBopassword(EncryptDecrypt.decrypt(cp.getProfileProperty(profileuserid, "bopassword", ""), connectionInfo.getDatabaseId()));
            String logoToken = sapphireBOUtil.restApiLogon();
            StringBuffer ajaxMessage = new StringBuffer();
            if (OpalUtil.isNotEmpty(logoToken)) {
                ajaxMessage.append(tp.translate("BOXI Connection Success!") + " \n\n " + tp.translate("The Server") + " " + sapphireBOUtil.getBourl() + " " + tp.translate("is responding"));
            } else {
                ajaxMessage.append(" \n\n " + tp.translate("BO REST service is not responding"));
            }
            sapphireBOUtil.restApiLogoff();
            ajaxResponse.addCallbackArgument("connection", ajaxMessage.toString());
        }
        catch (Exception e) {
            Trace.logInfo("BOXI Login error:" + e.getMessage());
            ajaxResponse.addCallbackArgument("connection", tp.translate("The Connection Failed!") + " \n\n " + tp.translate("1. Check the profile properties (BO URL, BO Username, BO Password, BO Authentication Type,BO CMS Name)") + " \n\n " + tp.translate("2. Check BOXI URL is correct (http://<ServerName>:port)") + " \n\n " + tp.translate("3.Check Central Management Server Service is up in BOXI Server"));
        }
        ajaxResponse.print();
    }
}

