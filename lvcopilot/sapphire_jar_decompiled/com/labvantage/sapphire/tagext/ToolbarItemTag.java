/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.BodyContent
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.ToolbarTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;

public class ToolbarItemTag
extends BaseBodyTagSupport {
    private ToolbarTag toolbarTag;

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.toolbarTag = (ToolbarTag)TagSupport.findAncestorWithClass((Tag)this, ToolbarTag.class);
        if (this.toolbarTag == null) {
            throw new JspTagException("TAG ERROR: ToolbarItem tag found with no surrounding Toolbar tag");
        }
        return 2;
    }

    @Override
    public int doEndTag() throws JspTagException {
        BodyContent body = this.getBodyContent();
        String text = body.getString();
        this.toolbarTag.setToolbarItem(text);
        this.toolbarTag = null;
        super.doEndTag();
        return 6;
    }
}

