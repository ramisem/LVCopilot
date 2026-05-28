/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.tagext.PageContent;
import com.labvantage.sapphire.tagext.PageRegion;
import java.util.ArrayList;
import javax.servlet.jsp.JspTagException;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class PageAreaTag
extends BaseBodyTagSupport {
    private String _name;
    private String _undefinedText;

    public void setName(String name) {
        this._name = name;
    }

    public void setUndefinedtext(String undefinedText) {
        TranslationProcessor _tp = new TranslationProcessor(this.pageContext);
        this._undefinedText = _tp.translate(undefinedText);
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        return 1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int doEndTag() throws JspTagException {
        PageContent pageContent;
        if (this._name == null || this._name.length() <= 0) throw new JspTagException("Name attribute not specified for PageArea tag");
        ArrayList regionList = (ArrayList)this.pageContext.getRequest().getAttribute("RegionList");
        if (regionList == null || regionList.size() <= 0) throw new JspTagException("No region list found.");
        PageRegion pageRegion = (PageRegion)regionList.get(regionList.size() - 1);
        if (pageRegion != null) {
            pageContent = pageRegion.getPageContent(this._name);
            if (pageContent == null) {
                pageContent = (PageContent)this.pageContext.getSession().getAttribute(this._name);
            }
        } else {
            pageContent = (PageContent)this.pageContext.getSession().getAttribute(this._name);
        }
        if (pageContent != null) {
            String areaContent;
            if (Trace.on) {
                this.logTrace("PAGEAREA: Rendering area name '" + this._name + "' within template '" + pageRegion.getTemplateId() + "'");
            }
            if ((areaContent = pageContent.getContent()) != null && areaContent.length() > 0) {
                try {
                    if (pageContent.isDirect()) {
                        this.pageContext.getOut().print(areaContent);
                    }
                    this.pageContext.include(areaContent);
                }
                catch (Throwable t) {
                    throw new JspTagException(t.getMessage());
                }
            }
        } else {
            if (this._undefinedText == null || this._undefinedText.length() <= 0) throw new JspTagException("Failed to find page section for '" + this._name + "' to render and no undefinedtext specified");
            if (Trace.on) {
                this.logTrace("PAGEAREA: Rendering undefinedtext for name '" + this._name + "' within template '" + pageRegion.getTemplateId() + "'");
            }
            try {
                this.pageContext.getOut().print(this._undefinedText);
            }
            catch (Exception e) {
                throw new JspTagException(e.getMessage());
            }
        }
        this._name = null;
        this._undefinedText = null;
        super.doEndTag();
        return 6;
    }

    private void evaluateExpressions() {
        this._name = JstlUtil.evaluateExpression(this._name, this.pageContext, "").toString();
        this._undefinedText = JstlUtil.evaluateExpression(this._undefinedText, this.pageContext, "").toString();
    }
}

