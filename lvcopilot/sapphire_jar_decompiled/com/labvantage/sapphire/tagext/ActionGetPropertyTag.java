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

public class ActionGetPropertyTag
extends BaseTagSupport {
    private String _id = "";
    private String _propertyid = "";

    public void setName(String id) {
        this._id = id;
    }

    public void setPropertyid(String propertyid) {
        this._propertyid = propertyid;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionGetPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: actionsetproperty tag must be nested in an actionblock tag.<br>");
        } else {
            try {
                this.write(_actionblocktag._actionblock.getActionProperty(this._id, this._propertyid));
            }
            catch (Exception e) {
                this.write("TAG ERROR");
            }
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this._id = JstlUtil.evaluateExpression(this._id, this.pageContext, "").toString();
        this._propertyid = JstlUtil.evaluateExpression(this._propertyid, this.pageContext, "").toString();
    }
}

