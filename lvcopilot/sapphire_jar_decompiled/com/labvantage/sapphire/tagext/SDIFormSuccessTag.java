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
import java.util.Enumeration;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.tagext.SDIFormSuccessTagInfo;
import sapphire.util.ForwardUtil;

public class SDIFormSuccessTag
extends BaseBodyTagSupport {
    private SDIFormTag _sdiformtag = null;
    private String _error = "";

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this._sdiformtag = (SDIFormTag)TagSupport.findAncestorWithClass((Tag)this, SDIFormTag.class);
        if (this._sdiformtag != null) {
            if (this.pageContext.getAttribute("sdiinfo") != null) {
                this._sdiformtag.setHasOldFormSucessTag(true);
            }
            if (this._sdiformtag.getFormSuccess()) {
                SDIFormSuccessTagInfo successInfo = new SDIFormSuccessTagInfo();
                successInfo.setInfoErrorString(this.requestContext.getProperty("__rulewarning"));
                this.pageContext.setAttribute("sdiformsuccessinfo", (Object)successInfo);
                rc = 2;
            }
        } else {
            this._error = this._error + "TAG ERROR: sdiformsuccess tag must be nested in a sdiform tag";
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
        } else if (this._sdiformtag.getFormSuccess() && "true".equals(this.requestContext.getProperty("__hasoldformsucesstag")) && "true".equals(this.requestContext.getProperty("__formsuccess"))) {
            this.write("Processed form success. Refreshing form or go to next URL...");
            ForwardUtil forward = new ForwardUtil(this.pageContext.getRequest());
            Enumeration e = this.pageContext.getRequest().getParameterNames();
            while (e.hasMoreElements()) {
                String propertyid = (String)e.nextElement();
                if (propertyid.equals("command") || propertyid.equals("__hasoldformsucesstag")) continue;
                forward.setProperty(propertyid, this.pageContext.getRequest().getParameter(propertyid));
            }
            this.write("</form>" + forward.getForm("__oldformsuccessdone", this.requestContext.getProperty("__nexturl"), "post", false));
            this.write("<script>document.body.onbeforeunload='';");
            this.write("\n__oldformsuccessdone.submit();</script>");
        }
        this._sdiformtag = null;
        this._error = "";
        super.doEndTag();
        return rc;
    }
}

