/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SitemapGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String SITEMAP_PROPERTY = "sitemap";
    private String eid = "sitemap";

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        PropertyList sm_prop = new PropertyList();
        PropertyList t_prop = new PropertyList();
        t_prop.setProperty("text", "N");
        t_prop.setProperty("show", "Y");
        PropertyList m_prop = new PropertyList();
        m_prop.setProperty("text", "N");
        m_prop.setProperty("show", "Y");
        t_prop.setProperty("menus", m_prop);
        sm_prop.setProperty("tabs", t_prop);
        up.setProperty(SITEMAP_PROPERTY, sm_prop);
        return up;
    }

    @Override
    public String getHtml() {
        this.eid = this.elementid.trim();
        this.eid = StringUtil.replaceAll(this.eid, " ", "_");
        StringBuffer html = new StringBuffer();
        TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        if (this.element != null) {
            PropertyList sitemap = this.element.getPropertyList(SITEMAP_PROPERTY);
            if (sitemap != null) {
                html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/dashboard/scripts/sitemapgizmo.js\"></script>");
                html.append("<script>");
                html.append("sitemapGizmo.sitemaps['").append(this.eid).append("'];");
                html.append("</script>");
                html.append("<iframe onload=\"if (typeof(sitemapGizmo)!='undefined'){sitemapGizmo.frameLoad('").append(this.eid).append("');}\" name=\"smg_").append(this.eid).append("_frame\" frameborder=0 id=\"smg_").append(this.eid).append("_frame\" frameborder=0 scrolling=false src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
                html.append("<form style=\"display:none\" method=\"post\" id=\"smg_").append(this.eid).append("_form\" name=\"smg_").append(this.eid).append("_form\" action=\"rc?command=file&file=WEB-OPAL/pagetypes/sitemap/tramline/sitemap.jsp\" target=\"smg_").append(this.eid).append("_frame\">");
                html.append("<input type=\"hidden\" name=\"").append("target").append("\" value=\"").append(this.element.getProperty("target", "_top")).append("\">");
                html.append("<input type=\"hidden\" name=\"").append("mode").append("\" value=\"").append(this.element.getProperty("renderingmode", "Modern")).append("\">");
                html.append("<textarea style=\"display:none;\" name=\"").append("properties").append("\">").append(sitemap.toJSONString(false)).append("</textarea>");
                html.append("</form>");
            } else {
                html.append("<font size=2>").append(translationProcessor.translate("No properties provided.")).append("</font>");
            }
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuffer html = new StringBuffer();
        html.append("window.setTimeout(function(){");
        html.append("var __smgf = document.getElementById('smg_").append(this.eid).append("_form").append("');");
        html.append("if (__smgf != null){__smgf.submit();}");
        html.append("if (typeof(sitemapGizmo) != 'undefined'){sitemapGizmo.gizmoId='").append(this.elementid).append("';}");
        html.append("},100);");
        return html.toString();
    }

    @Override
    public String getIcon() {
        return this.getImage("Sitemap Gizmo", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "Signpost";
    }
}

