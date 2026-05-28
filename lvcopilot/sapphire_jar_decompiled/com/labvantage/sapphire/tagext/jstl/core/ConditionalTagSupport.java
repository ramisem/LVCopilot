/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext.jstl.core;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public abstract class ConditionalTagSupport
extends TagSupport {
    private boolean result;
    private String var;
    private int scope;

    protected abstract boolean condition() throws JspTagException;

    public ConditionalTagSupport() {
        this.init();
    }

    public int doStartTag() throws JspException {
        this.result = this.condition();
        this.exposeVariables();
        if (this.result) {
            return 1;
        }
        return 0;
    }

    public void release() {
        super.release();
        this.init();
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        if (scope.equalsIgnoreCase("request")) {
            this.scope = 2;
        } else if (scope.equalsIgnoreCase("session")) {
            this.scope = 3;
        } else if (scope.equalsIgnoreCase("application")) {
            this.scope = 4;
        }
    }

    private void exposeVariables() {
        if (this.var != null) {
            this.pageContext.setAttribute(this.var, (Object)this.result, this.scope);
        }
    }

    private void init() {
        this.result = false;
        this.var = null;
        this.scope = 1;
    }
}

