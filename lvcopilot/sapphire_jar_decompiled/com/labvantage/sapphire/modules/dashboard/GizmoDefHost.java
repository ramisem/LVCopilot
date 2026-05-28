/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.layouts.modern.GizmoTargetAjaxManager;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import org.json.JSONObject;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class GizmoDefHost
extends BaseElement {
    @Override
    public String getHtml() {
        RequestContext requestContext = RequestContext.getRequestContext(this.pageContext);
        PropertyList pageprops = requestContext.getPropertyList();
        PropertyList pagedata = requestContext.getPropertyList("pagedata") != null ? requestContext.getPropertyList("pagedata") : new PropertyList();
        String gizmodefid = pageprops.getProperty("gizmo", pageprops.getProperty("gizmoid", pageprops.getProperty("gizmodefid", pagedata.getProperty("gizmoid"))));
        String gizmostyle = pageprops.getProperty("style", pageprops.getProperty("gizmostyle", BaseGizmo.GizmoStyle.FULL.toString()));
        String background = pageprops.getProperty("background", "transparent");
        boolean applyuseroverrides = pageprops.getProperty("applyuseroverrides", "Y").equalsIgnoreCase("Y");
        boolean isLocGizmo = true;
        String locationId = "";
        if (isLocGizmo) {
            locationId = pagedata.getProperty("location", "");
        }
        BaseGizmo.GizmoStyle style = BaseGizmo.GizmoStyle.FULL;
        try {
            style = BaseGizmo.GizmoStyle.valueOf(gizmostyle.toUpperCase());
        }
        catch (Exception e) {
            style = BaseGizmo.GizmoStyle.getGizmoStyle(gizmostyle, false);
        }
        PropertyList params = null;
        if (pageprops.getProperty("parameters").length() > 0) {
            try {
                params = new PropertyList(new JSONObject(pageprops.getProperty("parameters")));
            }
            catch (Exception exception) {}
        } else {
            params = new PropertyList();
            BaseGizmo.setUpParameters(this.pageContext.getRequest().getParameterMap(), params, this.connectionInfo);
        }
        sapphire.pageelements.BaseGizmo gizmoEl = BaseGizmo.getInstance(this.pageContext, gizmodefid, params, applyuseroverrides, false);
        StringBuffer html = new StringBuffer();
        if (gizmoEl != null) {
            html.append("<script type=\"text/javascript\" src='WEB-CORE/modules/dashboard/scripts/gizmodefhost.js'></script>");
            JavaScriptAPITag.getJQueryAPI(true, false, null, "", true, this.pageContext);
            html.append("<script>var gizmoDefHost = new GizmoDefHost('").append(gizmodefid).append("','").append(gizmoEl.getElementType().toLowerCase()).append("');</script>");
            html.append("<style>.gizmo_innercontainer{width:100% !important; height:100% !important;</style>");
            gizmoEl.setElementid("request_" + gizmodefid);
            gizmoEl.setGizmoStyle(style);
            gizmoEl.setGizmoLocation(BaseGizmo.GizmoLocation.PAGE);
            if (style == BaseGizmo.GizmoStyle.FULL) {
                StringBuffer missingBuffer = GizmoTargetAjaxManager.getMissingParameters(gizmoEl);
                if (missingBuffer.length() > 0) {
                    html.append("<div class=\"dashboard_gizmoerror\">");
                    html.append("Gizmo ").append(gizmoEl.getGizmoDefId()).append(" could not be loaded due to it requiring the following parameters:<br>");
                    html.append("<blockquote>").append(missingBuffer).append("</blockquote>");
                    html.append("<br>Parameterized Gizmos can only be added to a dashboard.");
                    html.append("</div>");
                } else {
                    int w = 200;
                    int h = 200;
                    if (requestContext.getProperty("width").length() > 0) {
                        try {
                            w = Integer.parseInt(requestContext.getProperty("width"));
                            w -= 2;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (requestContext.getProperty("height").length() > 0) {
                        try {
                            h = Integer.parseInt(requestContext.getProperty("height"));
                            h -= 12;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    gizmoEl.setWidth(w);
                    gizmoEl.setHeight(h);
                    html.append(gizmoEl.getHtml());
                }
            } else {
                String containerclass = "";
                if (gizmoEl.getElementType().equalsIgnoreCase("groupgizmo") && style != BaseGizmo.GizmoStyle.TEXT && style != BaseGizmo.GizmoStyle.SMALLTEXT) {
                    switch (style) {
                        case LARGE: 
                        case LARGETEXT: {
                            containerclass = "groupgizmo_container_large";
                            break;
                        }
                        case SMALL: {
                            containerclass = "groupgizmo_container_small";
                            break;
                        }
                        case MEDIUM: {
                            containerclass = "groupgizmo_container_medium";
                            break;
                        }
                        case MEDIUMTEXT: {
                            containerclass = "groupgizmo_container_medium";
                            break;
                        }
                        default: {
                            containerclass = "";
                        }
                    }
                }
                html.append("<div").append(containerclass.length() > 0 ? " class=\"" + containerclass + "\"" : "").append(">");
                html.append(gizmoEl.getIconHtml());
                html.append("</div>");
            }
            String title = pagedata.getProperty("title", gizmoEl.getTitle());
            html.append("<script>");
            html.append("document.title = '").append(title).append("';");
            html.append(((BaseGizmo)gizmoEl).getScript());
            html.append("</script>");
        } else {
            html.append("No gizmo provided.");
        }
        return html.toString();
    }
}

