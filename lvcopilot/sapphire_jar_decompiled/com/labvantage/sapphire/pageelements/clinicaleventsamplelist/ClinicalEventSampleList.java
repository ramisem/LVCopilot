/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.clinicaleventsamplelist;

import sapphire.pageelements.BaseElement;

public class ClinicalEventSampleList
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String PAGEID_PROPERTY = "pageid";
    public static final String WEBPAGEID_PROPERTY = "webpageid";
    public static final String ELEMENTID_PROPERTY = "elementid";
    private String keyid1 = "";

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.keyid1 = this.requestContext.getProperty("keyid1");
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            html.append("<iframe width=\"520px\" height=\"480px\" src=\"");
            html.append("rc?command=file&file=WEB-CORE/elements/clinicaleventsamplelist/clinicaleventsamplelist.jsp");
            html.append("&").append(ELEMENTID_PROPERTY).append("=").append(this.elementid);
            html.append("&").append(PAGEID_PROPERTY).append("=").append(this.element.getProperty(WEBPAGEID_PROPERTY, ""));
            html.append("&").append("eventid").append("=").append(this.keyid1);
            html.append("\" frameborder=0 name=\"").append(this.elementid).append("_iframe\" id=\"").append(this.elementid).append("_iframe\"></iframe>");
        }
        return html.toString();
    }
}

