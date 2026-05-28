/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import sapphire.pageelements.BaseGizmo;
import sapphire.xml.PropertyList;

public class GoogleGadgetGizmo
extends BaseGizmo {
    public static final String GADGETRESIZE_PROPERTY = "gadgetresize";
    public static final String SHOWSCROLLBARS_PROPERTY = "showscrollbars";
    public static final String PAGEID_PROPERTY = "pageid";
    public static final String WEBPAGEID_PROPERTY = "webpageid";
    public static final String ELEMENTID_PROPERTY = "elementid";
    public static final String GADGETPROPS_PROPERTY = "gadgetproperties";
    public static final String GADGETPROPNAME_PROPERTY = "propertyname";
    public static final String GADGETPROPVALUE_PROPERTY = "propertyvalue";
    public static final String GADGETURL_PROPERTY = "gadgeturl";
    public static final String WIDTH_PROPERTY = "width";
    public static final String HEIGHT_PROPERTY = "height";
    public static final String CALENDAR_TYPE = "Calendar";
    public static final String SEARCH_TYPE = "Search";
    public static final String DOCUMENTS_TYPE = "Documents";
    public static final String WEATHER_TYPE = "Weather";
    public static final String SERVICES_TYPE = "Services";
    public static final String NEWS_TYPE = "News";

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        PropertyList up = new PropertyList();
        up.setProperty(GADGETRESIZE_PROPERTY, "Y");
        up.setProperty(GADGETURL_PROPERTY, "Y");
        up.setProperty(SHOWSCROLLBARS_PROPERTY, "Y");
        PropertyList up_prop = new PropertyList();
        up_prop.setProperty(GADGETPROPNAME_PROPERTY, "Y");
        up_prop.setProperty(GADGETPROPVALUE_PROPERTY, "Y");
        up.setProperty(GADGETPROPS_PROPERTY, up_prop);
        this.setUserProperties(up);
        return true;
    }

    @Override
    public String getScript() {
        return "";
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            html.append("On November 1, 2013, iGoogle will be retired by Google. As such all gadgets will cease to function and therefore Google Gadgets as Dashboard Gizmo's are no longer available.");
        }
        return html.toString();
    }
}

