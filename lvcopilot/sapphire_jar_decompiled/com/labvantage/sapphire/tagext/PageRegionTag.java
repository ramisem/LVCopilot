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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class PageRegionTag
extends BaseBodyTagSupport {
    public static final String REGIONLIST_NAME = "RegionList";
    private String templateId = "";
    private PageRegion pageRegion;

    public void setTemplateid(String templateId) {
        this.templateId = templateId;
    }

    protected String getTemplateid() {
        return this.templateId;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        if (this.templateId != null && this.templateId.length() > 0) {
            ArrayList<PageRegion> regionList = (ArrayList<PageRegion>)this.pageContext.getRequest().getAttribute(REGIONLIST_NAME);
            if (regionList == null) {
                regionList = new ArrayList<PageRegion>();
                this.pageContext.getRequest().setAttribute(REGIONLIST_NAME, regionList);
            }
            this.pageRegion = new PageRegion(this.templateId);
            regionList.add(this.pageRegion);
            if (Trace.on) {
                this.logTrace("PAGEREGION: New PageRegion created for template '" + this.templateId + "'");
            }
        }
        return 1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int doEndTag() throws JspTagException {
        if (this.templateId != null && this.templateId.length() > 0) {
            ArrayList regionList = (ArrayList)this.pageContext.getRequest().getAttribute(REGIONLIST_NAME);
            if (regionList == null) throw new JspTagException("Region List not defined correctly");
            PageRegion region = (PageRegion)regionList.get(regionList.size() - 1);
            if (region == null) throw new JspTagException("Page Region not defined correctly");
            try {
                if (this.templateId.indexOf("/") != 0 && this.templateId.indexOf("\\") != 0) {
                    this.templateId = "/" + this.templateId;
                }
                if (regionList.size() > 1) {
                    if (Trace.on) {
                        this.logTrace("PAGEREGION: End of PageRegionTag - Buffering template '" + this.templateId + "'");
                    }
                    PageRegion outerRegion = (PageRegion)regionList.get(regionList.size() - 2);
                    outerRegion.setInnerRegion(this.pageRegion);
                } else {
                    if (Trace.on) {
                        this.logTrace("PAGEREGION: End of PageRegionTag - Including template '" + this.templateId + "'");
                    }
                    this.pageContext.include(this.templateId);
                }
            }
            catch (Exception e) {
                try {
                    this.pageContext.getOut().print("Exception raised rendering region: " + e.getMessage());
                    this.logError("Exception raised rendering region: " + e.getMessage(), e);
                }
                catch (IOException ioe) {
                    throw new JspTagException(ioe.getMessage());
                }
            }
            regionList.remove(regionList.size() - 1);
        }
        this.templateId = "";
        this.pageRegion = null;
        super.doEndTag();
        return 6;
    }

    public void addPageContent(PageContent pageContent) throws JspTagException {
        if (this.pageRegion == null) {
            throw new JspTagException("Attempt to add page content to a non existent region - check layout definition");
        }
        this.pageRegion.addPageContent(pageContent);
    }

    public void addPageRegion(String name, PageRegion newPageRegion) {
        if (this.pageRegion != null) {
            this.pageRegion.addPageContent(new PageContent(name, newPageRegion.getTemplateId(), false));
            Set keyset = newPageRegion.getPageContents().keySet();
            Iterator it = keyset.iterator();
            while (it.hasNext()) {
                this.pageRegion.addPageContent(newPageRegion.getPageContent((String)it.next()));
            }
        }
    }

    public PageRegion getInnerRegion() {
        return this.pageRegion != null ? this.pageRegion.getInnerRegion() : null;
    }

    private void evaluateExpressions() {
        this.templateId = JstlUtil.evaluateExpression(this.templateId, this.pageContext, "").toString();
    }
}

