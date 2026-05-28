/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class VersionStatusFilter
extends SearchContent {
    @Override
    public String getHtml() {
        boolean expired;
        boolean active;
        boolean current;
        boolean provisional;
        StringBuffer output = new StringBuffer("");
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String cookieOptions = userConfig.getProperty("as_versionfilter_" + this.cookieKey);
        if (cookieOptions.length() > 0) {
            provisional = cookieOptions.contains("P");
            current = cookieOptions.contains("C");
            active = cookieOptions.contains("A");
            expired = cookieOptions.contains("E");
        } else {
            PropertyList pageOptions = requestContext.getPropertyList("pagedata").getPropertyListNotNull("versionstatus");
            provisional = pageOptions.getProperty("provisional").equals("Y");
            current = pageOptions.getProperty("current").equals("Y");
            active = pageOptions.getProperty("active").equals("Y");
            expired = pageOptions.getProperty("expired").equals("Y");
        }
        output.append("<table class=\"search_table\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n").append("    <tr>").append("       <td><input type=\"checkbox\" " + (provisional ? "checked" : "") + " id=\"versionstatusfilter_provisional\" /> ").append(this.translator.translate("Provisional")).append("</td>\n").append("       <td><input type=\"checkbox\" " + (current ? "checked" : "") + " id=\"versionstatusfilter_current\" /> ").append(this.translator.translate("Current")).append("</td>\n").append("    </tr>").append("    <tr>").append("       <td><input type=\"checkbox\" " + (active ? "checked" : "") + " id=\"versionstatusfilter_active\" /> ").append(this.translator.translate("Active")).append("</td>\n").append("       <td><input type=\"checkbox\" " + (expired ? "checked" : "") + " id=\"versionstatusfilter_expired\" /> ").append(this.translator.translate("Expired")).append("</td>\n").append("    </tr>\n").append("</table>\n");
        return output.toString();
    }
}

