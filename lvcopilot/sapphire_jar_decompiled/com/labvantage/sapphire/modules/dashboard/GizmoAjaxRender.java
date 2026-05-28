/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.modules.dashboard.Dashboard;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GizmoAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block16: {
            ajaxResponse = new AjaxResponse(request, response, "DashboardHandler");
            JSONObject job = ajaxResponse.getCallProperties();
            if (job != null) {
                try {
                    int tabindex = job.getInt("tab");
                    String gizmoid = job.getString("gizmoid");
                    boolean workspace = job.has("workspace") && job.getString("workspace").equalsIgnoreCase("Y");
                    boolean preview = job.has("preview") && job.getString("preview").equalsIgnoreCase("Y");
                    PropertyList props = new PropertyList(job.getJSONObject("properties"));
                    if (props != null) {
                        PropertyList gizmo = props.getPropertyList(gizmoid);
                        if (gizmo != null) {
                            String className = gizmo.getProperty("objectname", "");
                            if (className.length() > 0) {
                                Object obG = Class.forName(className).newInstance();
                                if (obG instanceof BaseGizmo) {
                                    String script;
                                    String html;
                                    BaseGizmo gizmoOb = (BaseGizmo)obG;
                                    gizmoOb.setRakFile(this.getRakFile());
                                    gizmoOb.setConnectionId(this.getConnectionId());
                                    gizmoOb.setRequest(request);
                                    gizmoOb.setPageContext(ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                                    gizmoOb.setElementType(gizmo.getProperty("propertytreeid", ""));
                                    gizmoOb.setElementid(gizmoid);
                                    gizmoOb.setElementProperties(gizmo);
                                    PropertyList parameters = job.has("parameters") ? new PropertyList(job.getJSONObject("parameters")) : new PropertyList();
                                    gizmoOb.setParameters(parameters);
                                    gizmoOb.init();
                                    gizmoOb.setBaseProperties();
                                    if (workspace && !preview) {
                                        int width = (gizmoOb.getWidth() + 50) / 100;
                                        int height = (gizmoOb.getHeight() + 50) / 100;
                                        if (width < 3 && height < 2) {
                                            html = width < 2 ? Dashboard.renderPreview(gizmoOb, true, tabindex, gizmoid) : Dashboard.renderPreview(gizmoOb, false, tabindex, gizmoid);
                                            script = "";
                                        } else {
                                            html = gizmoOb.getHtml();
                                            script = gizmoOb.getScript();
                                        }
                                    } else {
                                        if (preview) {
                                            gizmoOb.setWidth(535);
                                            gizmoOb.setHeight(460);
                                        }
                                        html = gizmoOb.getHtml();
                                        script = gizmoOb.getScript();
                                    }
                                    ajaxResponse.addCallbackArgument("html", html);
                                    ajaxResponse.addCallbackArgument("tab", "" + tabindex);
                                    ajaxResponse.addCallbackArgument("gizmoid", gizmoid);
                                    ajaxResponse.addCallbackArgument("script", "" + script);
                                } else {
                                    ajaxResponse.setError(this.getTranslationProcessor().translate("The element is not a Gizmo."));
                                }
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find object name."));
                            }
                        } else {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain gizmo properties."));
                        }
                        break block16;
                    }
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain properties."));
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not create Gizmo."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
            }
        }
        ajaxResponse.print();
    }
}

