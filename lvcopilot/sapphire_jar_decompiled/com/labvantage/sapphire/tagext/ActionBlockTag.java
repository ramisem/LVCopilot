/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import javax.servlet.jsp.JspTagException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.tagext.ActionTagInfo;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.ActionBlock;

public class ActionBlockTag
extends BaseBodyTagSupport {
    private String _error = "";
    private boolean _hasrun = true;
    private boolean _successful = true;
    public ActionBlock _actionblock = null;
    private ActionTagInfo _actioninfo = null;

    public boolean isSuccessful() {
        return this._successful;
    }

    public void processBlock() {
        if (!this._hasrun) {
            this._hasrun = true;
            ActionProcessor actionprocessor = new ActionProcessor(this.getConnectionId());
            try {
                actionprocessor.processActionBlock(this._actionblock, false);
                this._successful = true;
            }
            catch (ActionException actionexception) {
                this._actioninfo.setErrorStack(actionprocessor.getErrorCodeList(), actionprocessor.getErrorStack());
                this._successful = false;
            }
            this._actioninfo.setErrorHandler(actionprocessor.getErrorHandler());
        }
    }

    public int doStartTag() throws JspTagException {
        this._hasrun = false;
        int rc = 0;
        this.doInit();
        if (this.isControlledPage()) {
            this._actionblock = new ActionBlock();
            this._actioninfo = new ActionTagInfo(this._actionblock);
            this.pageContext.setAttribute("actioninfo", (Object)this._actioninfo);
            rc = 2;
        } else {
            this.goErrorPage("RequestContext or controlled page tag does not exist. Tags can only be used via the Request Controller in controlled pages.<br>");
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            rc = 5;
            this.write(this._error);
        } else {
            this.processBlock();
        }
        this._error = "";
        this._hasrun = true;
        this._successful = true;
        this._actionblock = null;
        this._actioninfo = null;
        super.doEndTag();
        return rc;
    }
}

