/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.ActionBlockTag;
import com.labvantage.sapphire.tagext.ActionTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class ActionSetPropertyTag
extends BaseTagSupport {
    private String _propertyid = "";
    private String _value = "";

    public void setPropertyid(String propertyid) {
        this._propertyid = propertyid;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public int doStartTag() throws JspTagException {
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionSetPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        ActionTag _actiontag = (ActionTag)ActionSetPropertyTag.findAncestorWithClass((Tag)this, ActionTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: actionsetproperty tag must be nested in an action tag.<br>");
        } else if (_actiontag == null) {
            this.write("TAG ERROR: actionsetproperty tag must be nested in an action tag.<br>");
        } else {
            try {
                this.evaluateExpressions();
                _actionblocktag._actionblock.setActionProperty(_actiontag.getName(), this._propertyid, this._value);
            }
            catch (Exception e) {
                this.write("TAG ERROR: " + e.getMessage() + "<br>");
            }
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this._propertyid = JstlUtil.evaluateExpression(this._propertyid, this.pageContext, "").toString();
        this._value = JstlUtil.evaluateExpression(this._value, this.pageContext, "").toString();
    }
}

