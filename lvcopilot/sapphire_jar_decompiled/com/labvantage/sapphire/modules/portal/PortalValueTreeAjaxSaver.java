/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.portal;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.portal.PortalValueTree;
import com.labvantage.sapphire.services.SapphireConnection;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PortalValueTreeAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 61636 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean devMode;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String configCompCode = "";
        ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            configCompCode = devMode ? "" : config.getSysConfigProperty("compcode");
        }
        catch (Exception e) {
            devMode = false;
        }
        String props_pvt = ajaxResponse.getRequestParameter("productvaluetree", "");
        String props_cvt = ajaxResponse.getRequestParameter("componentvaluetree", "");
        String props_vt = ajaxResponse.getRequestParameter("valuetree", "");
        if (props_pvt.length() > 0 && props_vt.length() > 0) {
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            try {
                PropertyList portalprops_pvt = new PropertyList(new JSONObject(props_pvt));
                PropertyList portalprops_cvt = new PropertyList(new JSONObject(props_cvt));
                PropertyList portalprops_vt = new PropertyList(new JSONObject(props_vt));
                String portalid = ajaxResponse.getRequestParameter("portalid");
                boolean isCompMode = false;
                if (portalid.length() == 0) {
                    isCompMode = configCompCode.length() > 0;
                } else if (configCompCode.length() > 0) {
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT compcode FROM portal WHERE portalid=?", (Object[])new String[]{portalid});
                    if (ds.size() == 0) {
                        isCompMode = configCompCode.length() > 0;
                    } else {
                        String compcode = ds.getValue(0, "compcode");
                        isCompMode = compcode.equals(configCompCode);
                    }
                }
                boolean compOverride = !devMode && !isCompMode && configCompCode.length() > 0;
                PropertyList out = PortalValueTree.getProperties(portalprops_pvt, portalprops_cvt, portalprops_vt, portalid, devMode, isCompMode, compOverride, false, pageContext, this.logger);
                SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
                ajaxResponse.addCallbackArgument("overrides", out.getPropertyList("overrides").toXMLString());
                ajaxResponse.addCallbackArgument("devmode", devMode || isCompMode ? "Y" : "N");
                ajaxResponse.addCallbackArgument("compoverridemode", compOverride ? "Y" : "N");
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

