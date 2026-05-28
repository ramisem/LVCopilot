/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.SDIFormTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.tagext.SDIFormFailureTagInfo;

public class SDIFormFailureTag
extends BaseBodyTagSupport {
    private SDIFormTag _sdiformtag = null;
    private String _error = "";

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this._sdiformtag = (SDIFormTag)TagSupport.findAncestorWithClass((Tag)this, SDIFormTag.class);
        if (this._sdiformtag != null) {
            if (this._sdiformtag.getFormFailure()) {
                SDIFormFailureTagInfo failureInfo = new SDIFormFailureTagInfo();
                failureInfo.setLastError(this.requestContext.getProperty("__lasterror"));
                failureInfo.setErrorString(this.requestContext.getProperty("__ruleerror"));
                this.pageContext.setAttribute("sdiformfailureinfo", (Object)failureInfo);
                rc = 2;
            }
        } else {
            this._error = this._error + "TAG ERROR: sdiformfailure tag must be nested in a sdiform tag";
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
    }

    public int doAfterBody() throws JspTagException {
        int rc = 0;
        this.writeBodyContent();
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._sdiformtag = null;
        this._error = "";
        super.doEndTag();
        return rc;
    }
}

