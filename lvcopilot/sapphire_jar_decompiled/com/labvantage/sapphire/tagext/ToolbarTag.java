/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.ArrayList;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ToolbarTag
extends BaseBodyTagSupport {
    private String elementid;
    private String insertPosition = "End";
    private ArrayList toolbarItems = new ArrayList();

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setInsertPosition(String insertPosition) {
        this.insertPosition = insertPosition;
    }

    public void setToolbarItem(String content) {
        this.toolbarItems.add(content);
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        return 2;
    }

    @Override
    public int doEndTag() throws JspTagException {
        PropertyListCollection items;
        PropertyList properties;
        StringBuffer html = new StringBuffer();
        if (this.insertPosition.equalsIgnoreCase("Start") || this.insertPosition.equals("1")) {
            this.insertToolbarItems(html);
        }
        if ((properties = this.requestContext.getPropertyList().getPropertyList(this.elementid)) != null && (items = properties.getCollection("toolbaritems")) != null) {
            for (int i = 0; i < items.size(); ++i) {
                PropertyList buttonProps;
                PropertyList toolbarItem = items.getPropertyList(i);
                String type = toolbarItem.getProperty("type");
                if (!type.equals("Button") || (buttonProps = toolbarItem.getPropertyList("button")) == null || buttonProps.getProperty("mode").length() != 0 && !buttonProps.getProperty("mode").equals("Button") && !buttonProps.getProperty("mode").equals("Both")) continue;
                Button button = new Button(this.pageContext);
                button.setText(buttonProps.getProperty("text"));
                button.setAction(buttonProps.getProperty("js"));
                button.setTip(buttonProps.getProperty("tip"));
                button.setWidth(buttonProps.getProperty("width"));
                button.setAppearance(buttonProps.getProperty("appearance", "standard"));
                button.setId(buttonProps.getProperty("id"));
                button.setMargin(buttonProps.getProperty("margin"));
                button.setImg(buttonProps.getProperty("img"));
                button.setStyle(buttonProps.getProperty("style"));
                button.setHighlight(buttonProps.getProperty("highlight", "true"));
                html.append("<td>" + button.getHtml() + "</td>");
            }
        }
        if (this.insertPosition.equalsIgnoreCase("End")) {
            this.insertToolbarItems(html);
        }
        if (html.length() > 0) {
            this.write("<table class=\"toolbartable\"><tr>");
            this.write(html.toString());
            this.write("</tr></table>");
            this.write("<hr/>");
        }
        this.elementid = null;
        this.insertPosition = "End";
        this.toolbarItems = new ArrayList();
        super.doEndTag();
        return 6;
    }

    private void insertToolbarItems(StringBuffer html) throws JspTagException {
        for (int i = 0; i < this.toolbarItems.size(); ++i) {
            html.append("<td>" + (String)this.toolbarItems.get(i) + "</td>");
        }
    }

    protected void evaluateExpressions() {
        this.elementid = JstlUtil.evaluateExpression(this.elementid, this.pageContext, "").toString();
        this.insertPosition = JstlUtil.evaluateExpression(this.insertPosition, this.pageContext, "").toString();
    }
}

