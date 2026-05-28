/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.xml.PropertyList;

public class HTMLGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String HTML_PROPERTY = "html";

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(HTML_PROPERTY, "Y");
        return up;
    }

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element != null) {
            String HTML = this.element.getProperty(HTML_PROPERTY, "&nbsp;");
            html.append(HTML);
        } else {
            TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }
}

