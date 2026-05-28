/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.PageLogTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;

public class PageLogSetPropertyTag
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
        PageLogTag pagelog = (PageLogTag)TagSupport.findAncestorWithClass((Tag)this, PageLogTag.class);
        if (pagelog != null) {
            pagelog.setProperty(this._propertyid, this._value);
        } else {
            this.write("TAG ERROR: pagelogsetproperty tag must be nested in a pagelog tag.");
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
}

