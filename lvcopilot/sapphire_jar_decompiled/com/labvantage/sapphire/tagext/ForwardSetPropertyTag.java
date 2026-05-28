/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.ForwardTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class ForwardSetPropertyTag
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
        this.evaluateExpressions();
        ForwardTag forward = (ForwardTag)TagSupport.findAncestorWithClass((Tag)this, ForwardTag.class);
        if (forward != null) {
            forward.setProperty(this._propertyid, this._value);
        } else {
            this.write("TAG ERROR: forwardsetproperty tag must be nested in a forward tag.");
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._propertyid = "";
        this._value = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this._propertyid = JstlUtil.evaluateExpression(this._propertyid, this.pageContext, "").toString();
        this._value = JstlUtil.evaluateExpression(this._value, this.pageContext, "").toString();
    }
}

