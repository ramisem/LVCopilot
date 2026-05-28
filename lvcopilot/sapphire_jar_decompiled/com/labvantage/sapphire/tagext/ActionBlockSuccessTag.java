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

public class ActionBlockSuccessTag
extends BaseBodyTagSupport {
    public int doStartTag() throws JspTagException {
        int rc = 0;
        ActionBlockTag _actionblocktag = (ActionBlockTag)ActionBlockSuccessTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
        if (_actionblocktag == null) {
            this.write("TAG ERROR: actionblocksuccess tag must be nested in a actionblock tag.<br>");
        } else {
            _actionblocktag.processBlock();
            if (_actionblocktag.isSuccessful()) {
                rc = 2;
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
        super.doEndTag();
        return 6;
    }
}

