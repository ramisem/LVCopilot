/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.search.Search;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class SDISearchTag
extends BaseBodyTagSupport {
    private String elementid = "";
    private String _sdcid;

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setSdcid(String sdcid) {
        this._sdcid = sdcid;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        Search search = new Search(this.pageContext, this.getConnectionId());
        PropertyList properties = this.requestContext.getPropertyList().getPropertyList(this.elementid);
        search.setElementProperties(properties);
        search.setSdcid(this._sdcid);
        try {
            this.pageContext.getOut().print(search.getHtml());
        }
        catch (Exception e) {
            throw new JspTagException(e.getMessage());
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.elementid = "";
        this._sdcid = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this._sdcid = JstlUtil.evaluateExpression(this._sdcid, this.pageContext, "").toString();
    }
}

