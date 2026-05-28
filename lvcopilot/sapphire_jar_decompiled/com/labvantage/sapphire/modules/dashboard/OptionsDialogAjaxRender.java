/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class OptionsDialogAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block9: {
            ajaxResponse = new AjaxResponse(request, response, "DashboardHandler");
            JSONObject job = ajaxResponse.getCallProperties();
            if (job != null) {
                try {
                    int tabindex = job.getInt("tab");
                    String gizmoid = job.getString("gizmoid");
                    PropertyList props = new PropertyList(job.getJSONObject("properties"));
                    PropertyList gizmo = props.getPropertyList(gizmoid);
                    if (gizmo != null) {
                        String className = gizmo.getProperty("objectname", "");
                        if (className.length() > 0) {
                            Object obG = Class.forName(className).newInstance();
                            if (obG instanceof BaseGizmo) {
                                BaseGizmo gizmoOb = (BaseGizmo)obG;
                                gizmoOb.setRakFile(this.getRakFile());
                                gizmoOb.setConnectionId(this.getConnectionId());
                                gizmoOb.setRequest(request);
                                gizmoOb.setElementType(gizmo.getProperty("propertytreeid", ""));
                                gizmoOb.setElementid(gizmoid);
                                gizmoOb.setElementProperties(gizmo);
                                gizmoOb.setBaseProperties();
                                ajaxResponse.addCallbackArgument("html", gizmoOb.getOptionsHtml());
                                ajaxResponse.addCallbackArgument("tab", "" + tabindex);
                                ajaxResponse.addCallbackArgument("gizmoid", gizmoid);
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("The element is not a Gizmo."));
                            }
                        } else {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find object name."));
                        }
                        break block9;
                    }
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain gizmo properties."));
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not create Gizmo options."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
            }
        }
        ajaxResponse.print();
    }
}

