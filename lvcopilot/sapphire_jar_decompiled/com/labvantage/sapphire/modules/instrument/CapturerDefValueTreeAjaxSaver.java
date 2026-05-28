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
package com.labvantage.sapphire.modules.instrument;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.instrument.CapturerDefValueTree;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class CapturerDefValueTreeAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 61636 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean devMode;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        String props_im_vt = ajaxResponse.getRequestParameter("modelvaluetree", "");
        String props_vt = ajaxResponse.getRequestParameter("collectorvaluetree", "");
        if (props_vt.length() > 0) {
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            try {
                PropertyList capturerprops_vt = new PropertyList(new JSONObject(props_vt));
                PropertyList capturerprops_im_vt = new PropertyList(new JSONObject(props_im_vt));
                String instrumentmodelid = ajaxResponse.getRequestParameter("instrumentmodelid");
                PropertyList out = CapturerDefValueTree.getProperties(capturerprops_vt, capturerprops_im_vt, instrumentmodelid, ajaxResponse.getRequestParameter("collectorpropertytreeid"), ajaxResponse.getRequestParameter("collectorextendnodeid"), devMode, false, pageContext, this.logger);
                SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
                CacheUtil.clear(sc.getDatabaseId(), "GizmoDef");
                CacheUtil.clear(sc.getDatabaseId(), "GizmoDefAssets");
                ajaxResponse.addCallbackArgument("overrides", out.getPropertyList("overrides").toXMLString());
                ajaxResponse.addCallbackArgument("devmode", devMode ? "Y" : "N");
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

