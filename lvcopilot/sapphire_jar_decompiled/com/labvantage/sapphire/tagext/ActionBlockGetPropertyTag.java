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

public class ActionBlockGetPropertyTag
extends BaseTagSupport {
    private String _id = "";

    public void setId(String id) {
        this._id = id;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionBlockGetPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: actionblockgetproperty tag must be nested in an actionblock tag.<br>");
        } else if (this._id.length() > 0) {
            this.write(_actionblocktag._actionblock.getBlockProperty(this._id));
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this._id = JstlUtil.evaluateExpression(this._id, this.pageContext, "").toString();
    }
}

