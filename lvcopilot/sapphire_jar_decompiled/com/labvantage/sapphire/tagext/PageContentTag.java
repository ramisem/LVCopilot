/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.PageContent;
import com.labvantage.sapphire.tagext.PageRegionTag;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.SafeHTML;

public class PageContentTag
extends BaseBodyTagSupport {
    private String _name;
    private String _file;
    private String _iframe;
    private String _iframewidth;
    private String _iframeheight;
    private String _iframeborder;
    private String _iframescrolling;
    private String _layout;

    public void setName(String name) {
        this._name = name;
    }

    public void setFile(String file) {
        this._file = file;
    }

    public void setIframe(String iframe) {
        this._iframe = iframe;
    }

    public void setIframewidth(String iframewidth) {
        this._iframewidth = iframewidth;
    }

    public void setIframeheight(String iframeheight) {
        this._iframeheight = iframeheight;
    }

    public void setIframeborder(String iframeborder) {
        this._iframeborder = iframeborder;
    }

    public void setIframescrolling(String iframescrolling) {
        this._iframescrolling = iframescrolling;
    }

    public void setLayout(String layout) {
        this._layout = layout;
    }

    public int doStartTag() throws JspTagException {
        HttpServletRequest request;
        String iframename;
        this.doInit();
        this.evaluateExpressions();
        int rc = 2;
        if (this._name != null && this._name.length() > 0 && this._iframe != null && this._iframe.equals("true") && ((iframename = (request = (HttpServletRequest)this.pageContext.getRequest()).getParameter("_iframename")) == null || iframename.length() == 0)) {
            PageRegionTag pageRegionTag = (PageRegionTag)PageContentTag.findAncestorWithClass((Tag)this, PageRegionTag.class);
            if (pageRegionTag != null) {
                String crcId = "";
                RequestContext requestContext = this.getRequestContext();
                if (requestContext != null) {
                    crcId = "&__crc=crc_" + requestContext.getRequestId();
                    requestContext.setProperty("__cached", "Y");
                    request.getSession().setAttribute("crc_" + requestContext.getRequestId(), (Object)requestContext);
                }
                String src = this.pageContext.getServletContext().getInitParameter("RequestControllerName") + "?";
                String qs = request.getQueryString();
                if (qs != null && qs.length() > 0) {
                    src = src + qs + "&";
                }
                if (requestContext != null && requestContext.getPropertyList("pagedata") != null) {
                    if (src.indexOf("&command=") == -1 && requestContext.getPropertyList("pagedata").getProperty("command", "").length() > 0) {
                        if ((src = src + "command=" + requestContext.getPropertyList("pagedata").getProperty("command", "") + "&").indexOf("&page=") == -1 && requestContext.getPropertyList("pagedata").getProperty("page", "").length() > 0) {
                            src = src + "page=" + HttpUtil.encodeURIComponent(requestContext.getPropertyList("pagedata").getProperty("page", "")) + "&";
                        }
                        if (src.indexOf("&file=") == -1 && requestContext.getPropertyList("pagedata").getProperty("file").length() > 0) {
                            src = src + "file=" + HttpUtil.encodeURIComponent(requestContext.getPropertyList("pagedata").getProperty("file", "")) + "&";
                        }
                    }
                    if (src.indexOf("&sdcid=") == -1 && requestContext.getPropertyList("pagedata").getProperty("sdcid", "").length() > 0) {
                        src = src + "sdcid=" + HttpUtil.encodeURIComponent(requestContext.getPropertyList("pagedata").getProperty("sdcid", "")) + "&";
                    }
                }
                src = src + "_iframename=" + this._name + crcId;
                String iframetext = "<iframe id=\"" + this._name + "_iframe\" name=\"" + this._name + "_iframe\" src=\"" + SafeHTML.encodeForHTMLAttribute(src) + "\" style=\"height:" + this._iframeheight + ";width:" + this._iframewidth + "\"frameborder=" + (this._iframeborder != null && this._iframeborder.equals("1") ? "\"1\" " : "\"0\" ") + (this._iframewidth != null && this._iframewidth.length() > 0 ? "width=\"" + this._iframewidth + "\" " : "") + (this._iframeheight != null && this._iframeheight.length() > 0 ? "height=\"" + this._iframeheight + "\" " : "") + (this._iframescrolling != null && this._iframescrolling.length() > 0 ? " scrolling=\"" + this._iframescrolling + "\" " : "") + ">Your browser does not support iframes!</iframe>";
                pageRegionTag.addPageContent(new PageContent(this._name, iframetext, true));
                this.logTrace("PAGECONTENT: Added iframe html for name '" + this._name + "'");
                if (this._layout != null && this._layout.equals("false")) {
                    try {
                        this.pageContext.getOut().print(iframetext);
                    }
                    catch (IOException ioe) {
                        throw new JspTagException(ioe.getMessage());
                    }
                    this.logTrace("PAGECONTENT: Output iframe html for name '" + this._name + "'");
                }
                rc = 0;
            } else {
                throw new JspTagException("PageContent Tag must exist within a Page or PageRegion Tag");
            }
        }
        return rc;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int doEndTag() throws JspTagException {
        if (this._name != null && this._name.length() > 0) {
            PageRegionTag pageRegionTag = (PageRegionTag)PageContentTag.findAncestorWithClass((Tag)this, PageRegionTag.class);
            if (pageRegionTag == null) throw new JspTagException("PageContent Tag must exist within a Page or PageRegion Tag");
            if (this._iframe != null && this._iframe.equals("true")) {
                HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
                String iframename = request.getParameter("_iframename");
                if (iframename != null && iframename.equals(this._name)) {
                    pageRegionTag.addPageContent(new PageContent("iframecontent", this.getActualContent(), this.isDirect()));
                    this.logTrace("PAGECONTENT: Added iframe content name '" + this._name + "'");
                }
            } else if (pageRegionTag.getInnerRegion() != null) {
                pageRegionTag.addPageRegion(this._name, pageRegionTag.getInnerRegion());
                this.logTrace("PAGECONTENT: Added region name '" + this._name + "'");
            } else {
                pageRegionTag.addPageContent(new PageContent(this._name, this.getActualContent(), this.isDirect()));
                this.logTrace("PAGECONTENT: Added content name '" + this._name + "'");
            }
        } else {
            String content = this.bodyContent.getString();
            this.logTrace("CONTENT");
            this.logTrace(content);
            if (content == null || content.length() <= 0) throw new JspTagException("Name attribute or content not specified for PageContent tag");
            try {
                this.pageContext.getOut().print(content);
                return 6;
            }
            catch (IOException ioe) {
                throw new JspTagException(ioe.getMessage());
            }
        }
        this._name = null;
        this._file = null;
        this._iframe = null;
        this._iframewidth = null;
        this._iframeheight = null;
        this._iframeborder = null;
        this._iframescrolling = null;
        this._layout = null;
        super.doEndTag();
        return 0;
    }

    private boolean isDirect() {
        return this.hasBody() && (this._file == null || this._file.length() == 0);
    }

    private boolean hasBody() {
        return this.bodyContent != null && this.bodyContent.getString().length() > 0;
    }

    private String getActualContent() throws JspTagException {
        boolean hasBody = this.hasBody();
        String content = "";
        if (hasBody && (this._file == null || this._file.length() == 0)) {
            content = this.bodyContent.getString();
        } else if (!hasBody && this._file != null && this._file.length() > 0) {
            content = this._file;
        }
        return content;
    }

    private void evaluateExpressions() {
        this._name = JstlUtil.evaluateExpression(this._name, this.pageContext, "").toString();
        this._file = JstlUtil.evaluateExpression(this._file, this.pageContext, "").toString();
        this._iframe = JstlUtil.evaluateExpression(this._iframe, this.pageContext, "").toString();
        this._iframewidth = JstlUtil.evaluateExpression(this._iframewidth, this.pageContext, "").toString();
        this._iframeheight = JstlUtil.evaluateExpression(this._iframeheight, this.pageContext, "").toString();
        this._iframeborder = JstlUtil.evaluateExpression(this._iframeborder, this.pageContext, "").toString();
        this._iframescrolling = JstlUtil.evaluateExpression(this._iframescrolling, this.pageContext, "").toString();
        this._layout = JstlUtil.evaluateExpression(this._layout, this.pageContext, "").toString();
    }
}

