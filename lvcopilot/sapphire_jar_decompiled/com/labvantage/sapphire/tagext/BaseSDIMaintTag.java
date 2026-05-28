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
import sapphire.tagext.SDITagInfo;
import sapphire.util.JstlUtil;

public class BaseSDIMaintTag
extends BaseTagSupport {
    protected String elementid = "";

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        if (this.isControlledPage()) {
            SDITag sdi = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
            if (sdi != null) {
                this.evaluateExpressions();
                SDITagInfo sdiInfo = (SDITagInfo)this.pageContext.getAttribute("sdiinfo");
                this.doTag(sdiInfo);
            } else {
                this.write("TAG ERROR: sdimaint tags must be nested in an sdi tag.");
            }
        } else {
            this.write("TAG ERROR: Request context null");
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this.elementid = JstlUtil.evaluateExpression(this.elementid, this.pageContext, "").toString();
    }

    protected void doTag(SDITagInfo sdiInfo) throws JspTagException {
        this.write("TAG ERROR: doTag method not written");
    }
}

