/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.tagext.TabGroupTag;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class TabTag
extends BaseBodyTagSupport {
    private String id;
    private String context;
    private String text;
    private String collapsedText = "";
    private String width = "1%";
    private String tip = "";
    private String bodyWidth = "100%";
    private String bodyHeight = "";
    private String expandable = "false";
    private String expanded = "false";
    private String appearance = "standard";
    private String highlight = "true";
    private String action = "";
    private Tab tab = null;

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCollapsedtext(String collapsedText) {
        this.collapsedText = collapsedText;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setBodywidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public void setBodyheight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public void setExpandable(String expandable) {
        this.expandable = expandable;
    }

    public void setExpanded(String expanded) {
        this.expanded = expanded;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this.evaluateExpressions();
        if (this.id != null && this.id.length() > 0) {
            this.tab = new Tab();
            this.tab.setAppearance(this.appearance);
            this.tab.setBodyheight(this.bodyHeight);
            this.tab.setBodywidth(this.bodyWidth);
            this.tab.setCollapsedtext(this.collapsedText);
            this.tab.setExpandable(this.expandable);
            this.tab.setExpanded(this.expanded);
            this.tab.setHighlight(this.highlight);
            this.tab.setId(this.id);
            this.tab.setContext(this.context);
            this.tab.setText(this.text);
            this.tab.setTip(this.tip);
            this.tab.setWidth(this.width);
            this.tab.setAction(this.action);
            this.tab.setPageContext(this.pageContext);
            rc = 2;
        } else {
            this.write("TAG ERROR: id not defined for tab tag");
            rc = 0;
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        int rc = 0;
        this.tab.setContent(this.getBodyContent().getString());
        TabGroupTag tagGroupTag = (TabGroupTag)TagSupport.findAncestorWithClass((Tag)this, TabGroupTag.class);
        if (tagGroupTag != null) {
            tagGroupTag.addTab(this.tab);
        } else {
            try {
                this.getPreviousOut().write(this.tab.getHtml());
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.id = null;
        this.context = null;
        this.text = null;
        this.collapsedText = "";
        this.width = "1%";
        this.tip = "";
        this.bodyWidth = "100%";
        this.bodyHeight = "";
        this.expandable = "false";
        this.expanded = "false";
        this.appearance = "standard";
        this.highlight = "true";
        this.action = "";
        this.tab = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.id = JstlUtil.evaluateExpression(this.id, this.pageContext, "").toString();
        this.text = JstlUtil.evaluateExpression(this.text, this.pageContext, this.id).toString();
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        this.collapsedText = JstlUtil.evaluateExpression(this.collapsedText, this.pageContext, tp.translate("Click the tab to show more information.")).toString();
        this.expandable = JstlUtil.evaluateExpression(this.expandable, this.pageContext, "false").toString();
        this.expanded = JstlUtil.evaluateExpression(this.expanded, this.pageContext, "false").toString();
        this.width = JstlUtil.evaluateExpression(this.width, this.pageContext, "1%").toString();
        this.bodyWidth = JstlUtil.evaluateExpression(this.bodyWidth, this.pageContext, "100%").toString();
        this.bodyHeight = JstlUtil.evaluateExpression(this.bodyHeight, this.pageContext, "").toString();
        this.tip = JstlUtil.evaluateExpression(this.tip, this.pageContext, "").toString();
        this.action = JstlUtil.evaluateExpression(this.action, this.pageContext, "").toString();
    }
}

