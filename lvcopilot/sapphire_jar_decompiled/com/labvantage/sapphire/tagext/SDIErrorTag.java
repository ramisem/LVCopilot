/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;

public class SDIErrorTag
extends BaseTagSupport {
    public int doStartTag() throws JspTagException {
        String output = "";
        SDITag sdi = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        output = sdi != null ? sdi.getSDIError() : "TAG ERROR: sdierror tag must be nested in a sdi tag.";
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        super.doEndTag();
        return 6;
    }
}

