/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.ActionBlockTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class ActionTag
extends BaseBodyTagSupport {
    private String _error = "";
    private String _id = "(default)";
    private String _actionid = "";
    private String _versionid = "1";

    public void setName(String id) {
        this._id = id;
    }

    public String getName() {
        return this._id;
    }

    public void setActionid(String actionid) {
        this._actionid = actionid;
    }

    public void setActionversionid(String versionid) {
        this._versionid = versionid;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: action tag must be nested in a actionblock tag.<br>");
        } else {
            try {
                this.evaluateExpressions();
                _actionblocktag._actionblock.setAction(this._id, this._actionid, this._versionid);
                rc = 2;
            }
            catch (Exception e) {
                this.write("TAG ERROR: " + e.getMessage() + "<br>");
            }
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            rc = 5;
            this.write(this._error);
        }
        this._error = "";
        this._id = "(default)";
        this._actionid = "";
        this._versionid = "1";
        super.doEndTag();
        return rc;
    }

    protected void evaluateExpressions() {
        this._id = JstlUtil.evaluateExpression(this._id, this.pageContext, "").toString();
        this._versionid = JstlUtil.evaluateExpression(this._versionid, this.pageContext, "").toString();
        this._actionid = JstlUtil.evaluateExpression(this._actionid, this.pageContext, "").toString();
    }
}

