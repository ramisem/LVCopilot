/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.servlet.RequestProcessor;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class FileTag
extends BaseBodyTagSupport {
    private String file;

    public void setFile(String file) {
        this.file = file;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        if (this.requestContext == null) throw new JspTagException("No request context found in request - file tag must be used with RequestController");
        try {
            RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
            requestProcessor.addPropertyData(this.requestContext);
            if (this.file == null || this.file.length() <= 0) return 1;
            this.pageContext.include(this.file);
            return 1;
        }
        catch (Exception e) {
            throw new JspTagException("Failed to add property data in file tag");
        }
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.file = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.file = JstlUtil.evaluateExpression(this.file, this.pageContext, "").toString();
    }
}

