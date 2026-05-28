/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.search;

import com.labvantage.sapphire.pageelements.controls.Button;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;

public class SearchByBasic
extends BaseElement {
    public SearchByBasic(PropertyList element) {
        this.element = element;
    }

    public SearchByBasic(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer("");
        String title = this.element.getPropertyList("basicsearch") != null ? this.element.getPropertyList("basicsearch").getProperty("title", "Search:") : "Search:";
        html.append("<form id=\"basicsearchform\" action=\"javascript:startSearch();\">\n").append("    <tr class=\"search_header\"><td colspan=\"2\"><b>" + title + "</b></td></tr>\n").append("    <tr><td><input class=\"search_basicinput\" id=\"searchtext\" type=\"text\" size=\"16\" onkeydown=\"if ( event.keyCode == 13 ) javascript:startSearch( 'basic' );\"/>&nbsp;</td><td>\n");
        Button button = new Button(this.pageContext);
        button.setText(this.element.getPropertyList("basicsearch") != null ? this.element.getPropertyList("basicsearch").getProperty("buttontext", "OK") : "OK");
        button.setMargin("3");
        button.setAction("startSearch( 'basic' )");
        html.append(button.getHtml());
        html.append("    </td></tr>\n").append("</form>\n");
        return html.toString();
    }
}

