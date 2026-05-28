/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;

public class TabGroupTag
extends BaseBodyTagSupport {
    private String id;
    private String context;
    private String tip = "";
    private String bodyWidth = "100%";
    private String bodyHeight = "";
    private String appearance = "standard";
    private String multitab = "true";
    private String useChangeTab = "true";
    private TabGroup tabGroup = null;

    public void setId(String id) {
        id = StringUtil.replaceAll(id, "(", "_");
        id = StringUtil.replaceAll(id, ")", "_");
        id = StringUtil.replaceAll(id, "\ufb22", "_");
        this.id = id = StringUtil.replaceAll(id, " \ufb1d", "_");
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setBodywidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public void setBodyheight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public void setMultitab(String multitab) {
        this.multitab = multitab;
    }

    public void setUsechangetab(String useChangeTab) {
        this.useChangeTab = useChangeTab;
    }

    public void addTab(Tab tab) {
        this.tabGroup.setTab(tab);
    }

    public int doStartTag() throws JspTagException {
        int rc;
        this.doInit();
        this.evaluateExpressions();
        if (this.id != null && this.id.length() > 0) {
            this.tabGroup = new TabGroup();
            this.tabGroup.setPageContext(this.pageContext);
            this.tabGroup.setAppearance(this.appearance);
            this.tabGroup.setBodywidth(this.bodyWidth);
            if (this.bodyHeight != null && this.bodyHeight.length() > 0) {
                this.tabGroup.setBodyheight(this.bodyHeight);
            }
            this.tabGroup.setId(this.id);
            this.tabGroup.setContext(this.context);
            this.tabGroup.setTip(this.tip);
            this.tabGroup.setMultiTab(this.multitab.equals("true"));
            this.tabGroup.setUseChangeTab(this.useChangeTab.equals("true"));
            rc = 2;
        } else {
            this.write("TAG ERROR: id not defined for tab tag");
            rc = 0;
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        int rc = 0;
        try {
            this.getPreviousOut().write(this.tabGroup.getHtml());
        }
        catch (IOException ioe) {
            throw new JspTagException("Failed to write tag contents. Exception: " + ioe.getMessage());
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.id = null;
        this.context = null;
        this.tip = "";
        this.bodyWidth = "100%";
        this.appearance = "standard";
        this.multitab = "true";
        this.useChangeTab = "true";
        this.tabGroup = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.id = JstlUtil.evaluateExpression(this.id, this.pageContext, "").toString();
        this.bodyWidth = JstlUtil.evaluateExpression(this.bodyWidth, this.pageContext, "100%").toString();
        this.tip = JstlUtil.evaluateExpression(this.tip, this.pageContext, "").toString();
        this.multitab = JstlUtil.evaluateExpression(this.multitab, this.pageContext, "true").toString();
    }
}

