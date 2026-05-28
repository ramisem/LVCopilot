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
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class ActionBlockSetPropertyTag
extends BaseTagSupport {
    private String _id = "";
    private String _value = "";

    public void setPropertyid(String id) {
        this._id = id;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionBlockSetPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: actionblocksetproperty tag must be nested in an actionblock tag.<br>");
        } else if (this._id.length() > 0) {
            _actionblocktag._actionblock.setBlockProperty(this._id, this._value);
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this._id = JstlUtil.evaluateExpression(this._id, this.pageContext, "").toString();
        this._value = JstlUtil.evaluateExpression(this._value, this.pageContext, "").toString();
    }
}

