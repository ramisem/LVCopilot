/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.PageContent;
import com.labvantage.sapphire.tagext.PageRegionTag;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.tagext.PageTagInfo;
import sapphire.util.Browser;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PageTag
extends PageRegionTag {
    private String layout = "";
    private String html5 = "N";
    private String error = "";
    private boolean expirePage = false;
    private boolean updatePropertyTreeData = false;
    private String module = "";
    private String var = "requestdata";
    private String pagelogtitle = "";

    public void setPagelogtitle(String title) {
        this.pagelogtitle = title;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setHtml5(String html5) {
        this.html5 = html5;
    }

    public void setExpirePage(String expirePage) {
        this.expirePage = expirePage.equalsIgnoreCase("true");
    }

    public void setUpdate(String update) {
        this.updatePropertyTreeData = update.equals("true");
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public int doStartTag() throws JspTagException {
        int rc;
        HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse)this.pageContext.getResponse();
        this.doInit();
        if (this.requestContext != null) {
            boolean iFrameRequest;
            if (this.updatePropertyTreeData) {
                try {
                    RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
                    requestProcessor.addPropertyData(this.requestContext);
                }
                catch (SapphireException e) {
                    throw new JspTagException("Failed to add property data in file tag");
                }
            }
            PageTagInfo pageTagInfo = new PageTagInfo(this.pageContext, this.requestContext);
            this.pageContext.setAttribute("pageinfo", (Object)pageTagInfo);
            this.pageContext.setAttribute("logger", (Object)new Logger(this.pageContext));
            Browser browser = new Browser(this.pageContext);
            this.pageContext.setAttribute("browser", (Object)browser);
            PropertyList propertyList = this.requestContext.getPropertyList();
            propertyList.setGuiMode(browser.getGUIMode() != null ? browser.getGUIMode().getId() : (browser.isPhone() ? "phone" : (browser.isMobile() ? "tablet" : "desktop")));
            this.pageContext.setAttribute("pagelogtitle", (Object)this.pagelogtitle);
            HttpUtil.setRequestVariables(request, this.var, propertyList);
            this.evaluateExpressions();
            this.html5 = this.requestContext.getProperty("modernlayout").equalsIgnoreCase("Y") ? "Y" : (this.html5.equalsIgnoreCase("Y") || this.html5.equalsIgnoreCase("true") ? "Y" : "N");
            this.requestContext.setProperty("html5", this.html5);
            if (this.module.length() > 0) {
                String[] modulelist = StringUtil.split(this.module, ";");
                PropertyList modules = this.requestContext.getPropertyList("hasModule");
                if (modules != null) {
                    boolean hasAccess = false;
                    for (int i = 0; i < modulelist.length; ++i) {
                        if (!modules.getProperty(modulelist[i]).equals("true")) continue;
                        hasAccess = true;
                    }
                    if (!hasAccess) {
                        throw new JspTagException("User '" + this.requestContext.getProperty("sysuserid") + "' has not been granted access to module(s) '" + this.module + "'");
                    }
                }
            }
            boolean bl = iFrameRequest = this.requestContext.getProperty("_iframename").length() > 0;
            if (iFrameRequest) {
                this.setTemplateid("/WEB-CORE/layouts/standard/iframelayout.jsp");
                rc = super.doStartTag();
                this.addPageContent(new PageContent(this.requestContext.getProperty("_iframename"), "/WEB-CORE/layouts/standard/iframelayout.jsp", false));
            } else if (this.layout != null && this.layout.length() > 0) {
                PropertyList layoutPL = propertyList.getPropertyList("layout");
                if ("navigator".equals(this.pageContext.getRequest().getParameter("layout")) || "Y".equals(this.pageContext.getRequest().getParameter("nolayout")) || "Y".equals(this.pageContext.getRequest().getParameter("taskpage"))) {
                    this.layout = "/WEB-OPAL/layouts/popup/popuplayout.jsp";
                    if (layoutPL != null) {
                        layoutPL.setProperty("hidetitle", "Y");
                    }
                }
                if (layoutPL != null && layoutPL.getProperty("objectname").length() == 0 && this.layout.endsWith(".jsp")) {
                    layoutPL.setProperty("objectname", this.layout);
                }
                this.setTemplateid(this.layout);
                rc = super.doStartTag();
            } else {
                rc = super.doStartTag();
            }
            if ("Y".equals(this.pageContext.getRequest().getParameter("taskpage"))) {
                pageTagInfo.setTaskPage(true);
            }
            HttpUtil cookie = new HttpUtil(request, response);
            cookie.removeCookie("rsetlist");
            String userid = this.requestContext.getProperty("sysuserid");
            if (Trace.on) {
                this.logTrace("PAGE: " + userid + " has access to page (" + this.pageContext.getServletContext().getInitParameter("RequestControllerName") + "?" + request.getQueryString() + ")");
            }
            Browser b = new Browser(this.pageContext);
            String jsAPI = JavaScriptAPITag.getJavaScriptAPI(this.pageContext, this.requestContext, null, true);
            if (jsAPI.length() > 0) {
                try {
                    this.pageContext.getOut().print(jsAPI);
                }
                catch (Exception exception) {}
            }
        } else {
            this.goErrorPage("Controlled page can only be accessed via the RequestController.");
            rc = 0;
        }
        if (rc != 0) {
            this.requestContext.setControlledPage(true);
            if (this.expirePage) {
                response.setDateHeader("Expires", 0L);
                response.setHeader("Pragma", "no-cache");
                if (request.getProtocol().equalsIgnoreCase("HTTP/1.1")) {
                    response.setHeader("Cache-Control", "no-cache");
                }
            }
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        if (this.requestContext.getProperty("currentlayout").equals("Generic") && this.pagelogtitle.length() > 0) {
            try {
                HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
                RequestProcessor requestProcessor = new RequestProcessor(this.pageContext);
                requestProcessor.logPageAccess(this.pagelogtitle, request);
            }
            catch (SapphireException e) {
                Trace.log("Failed to add page " + this.pagelogtitle + " to webpagelog." + e.getMessage());
            }
        }
        int rc = super.doEndTag();
        if (this.error.length() > 0) {
            rc = 5;
            this.write(this.error);
        }
        this.layout = "";
        this.html5 = "N";
        this.error = "";
        this.expirePage = false;
        this.updatePropertyTreeData = false;
        this.module = "";
        this.var = "requestdata";
        this.pagelogtitle = "";
        return rc;
    }

    private void evaluateExpressions() {
        this.layout = JstlUtil.evaluateExpression(this.layout, this.pageContext, "").toString();
        this.html5 = JstlUtil.evaluateExpression(this.html5, this.pageContext, "").toString();
        this.module = JstlUtil.evaluateExpression(this.module, this.pageContext, "").toString();
    }
}

