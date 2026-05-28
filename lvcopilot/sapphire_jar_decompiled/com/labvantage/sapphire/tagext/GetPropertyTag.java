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

public class GetPropertyTag
extends BaseTagSupport {
    private String _propertyid = "";
    private String _scope = "";

    public void setPropertyid(String propertyid) {
        this._propertyid = propertyid;
    }

    public void setScope(String scope) {
        this._scope = scope;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        String output = "";
        output = this.isControlledPage() ? (this._propertyid.length() > 0 ? (this._scope.length() > 0 && !this._scope.equals("full") ? (this._scope.equalsIgnoreCase("request") ? this.requestContext.getProperty(this._propertyid) : (this._scope.equalsIgnoreCase("page") ? this.requestContext.getProperty(this._propertyid) : "TAG ERROR: Scope not recognized in getproperty tag")) : this.requestContext.getProperty(this._propertyid)) : "TAG ERROR: Propertyid not specified in getproperty tag") : "TAG ERROR: Tag can only be used via the request controller and within controlled pages";
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._propertyid = "";
        this._scope = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this._propertyid = JstlUtil.evaluateExpression(this._propertyid, this.pageContext, "").toString();
        this._scope = JstlUtil.evaluateExpression(this._scope, this.pageContext, "").toString();
    }
}

