/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class PingTag
extends BaseTagSupport {
    private String rsetList = "";

    public void setrsetlist(String rsetList) {
        this.rsetList = rsetList;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        if (this.rsetList.length() > 0) {
            this.write("<iframe id=\"__rsetping\" name=\"__rsetping\" src=\"rc?command=ping&rsetlist=" + this.rsetList + "\" frameborder=\"0\" width=\"0\" height=\"0\" >Your browser does not support iframes!</iframe>");
        }
        return 1;
    }

    @Override
    public int doEndTag() throws JspTagException {
        super.doEndTag();
        this.rsetList = "";
        return 6;
    }

    protected void evaluateExpressions() {
        this.rsetList = JstlUtil.evaluateExpression(this.rsetList, this.pageContext, "").toString();
    }
}

