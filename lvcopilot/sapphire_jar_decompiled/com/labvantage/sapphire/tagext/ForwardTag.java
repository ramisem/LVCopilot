/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.ForwardUtil;
import sapphire.util.JstlUtil;

public class ForwardTag
extends BaseBodyTagSupport {
    private String _error = "";
    private String _formid = "__formdata";
    private String _action = "";
    private String _method = "post";
    private String _message = "";
    private boolean _autosubmit = true;
    ForwardUtil _forward = null;

    public void setId(String id) {
        this._formid = id;
    }

    public void setAction(String action) {
        this._action = action;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public void setMessage(String message) {
        this._message = message;
    }

    public void setAutosubmit(String autosubmit) {
        if (autosubmit.equals("false")) {
            this._autosubmit = false;
        }
    }

    public void setProperty(String propertyid, String value) {
        if (this._forward == null) {
            this._forward = new ForwardUtil((ServletRequest)((HttpServletRequest)this.pageContext.getRequest()));
        }
        this._forward.setProperty(propertyid, value);
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        if (this._forward == null) {
            this._forward = new ForwardUtil((ServletRequest)((HttpServletRequest)this.pageContext.getRequest()));
        }
        this.doInit();
        this.evaluateExpressions();
        if (this.isControlledPage()) {
            rc = 2;
        } else {
            this.goErrorPage("RequestContext or controlled page tag does not exist. Tags can only be used via the Request Controller and in a controlled page.");
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() == 0) {
            StringBuffer output = new StringBuffer(500);
            if (this._method != null && this._method.length() > 0 && this._method.equalsIgnoreCase("get")) {
                String URLParams = this._forward.getURLParams();
                output.append("<script language=\"JavaScript\">\n");
                output.append("window.location.href='" + this._action + (URLParams != null && URLParams.length() > 0 ? "&" + this._forward.getURLParams() : "") + "'\n");
                output.append("</script>\n");
            } else {
                if (this._message.length() > 0) {
                    output.append(this._message + "<br>\n");
                    output.append("<br><br><a href=\"JavaScript:" + this._formid + ".submit()\">Click here</a> if you are not redirected to a new page within a few seconds.\n");
                }
                output.append(this._forward.getForm(this._formid, this._action, "post", this._autosubmit));
            }
            this.write(output.toString());
        } else {
            this.write(this._error);
        }
        this._error = "";
        this._formid = "__formdata";
        this._action = "";
        this._method = "post";
        this._message = "";
        this._autosubmit = true;
        this._forward = null;
        super.doEndTag();
        return rc;
    }

    protected void evaluateExpressions() {
        this._formid = JstlUtil.evaluateExpression(this._formid, this.pageContext, "").toString();
        this._action = JstlUtil.evaluateExpression(this._action, this.pageContext, "").toString();
        this._method = JstlUtil.evaluateExpression(this._method, this.pageContext, "").toString();
        this._message = JstlUtil.evaluateExpression(this._message, this.pageContext, "").toString();
    }
}

