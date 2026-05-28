/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.util.http.HttpUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseTagSupport;
import sapphire.tagext.ControlledPageTagInfo;

public class ControlledPageTag
extends BaseTagSupport {
    private String _error = "";
    private boolean _allowanonymous = false;
    private boolean _expirepage = false;

    public void setAllowanonymous(String allowanonymous) {
        this._allowanonymous = allowanonymous.equalsIgnoreCase("true");
    }

    public void setExpirepage(String expirepage) {
        this._expirepage = expirepage.equalsIgnoreCase("true");
    }

    public int doStartTag() throws JspTagException {
        HttpServletResponse response;
        HttpServletRequest request;
        int rc;
        block12: {
            rc = 1;
            this.doInit();
            request = (HttpServletRequest)this.pageContext.getRequest();
            response = (HttpServletResponse)this.pageContext.getResponse();
            if (this.requestContext != null) {
                String userid;
                boolean hasaccess = true;
                sapphire.util.HttpUtil cookie = new sapphire.util.HttpUtil(request, response);
                cookie.removeCookie("rsetlist");
                this.pageContext.setAttribute("requestinfo", (Object)new ControlledPageTagInfo(this.requestContext));
                if (this.requestContext.getProperty("registeredpage").equals("true")) {
                    this._allowanonymous = this.requestContext.getProperty("allowanonymous").toLowerCase().equals("true");
                }
                hasaccess = (userid = this.requestContext.getProperty("sysuserid")).equalsIgnoreCase(this.pageContext.getServletContext().getInitParameter("anonymoususerid")) && this._allowanonymous;
                boolean bl = hasaccess = hasaccess || !userid.equalsIgnoreCase(this.pageContext.getServletContext().getInitParameter("anonymoususerid"));
                if (hasaccess) {
                    this.logTrace(userid + " has access to page (" + this.pageContext.getServletContext().getInitParameter("RequestControllerName") + "?" + request.getQueryString() + ")");
                    try {
                        if (this.requestContext != null && this.requestContext.getConnectionId() != null && this.requestContext.getConnectionId().length() > 0) {
                            this.pageContext.getOut().print(HttpUtil.getSapphireCoreJSHTML(this.pageContext, this.requestContext));
                            break block12;
                        }
                        this.pageContext.getOut().print(HttpUtil.getSapphireCoreJSHTML(this.pageContext, null));
                    }
                    catch (Exception e) {
                        this.logError("Could not render JS API from ControlledPage Tag.");
                    }
                } else {
                    this._error = "The page requires a valid logon";
                    String nexturl = "rc?";
                    nexturl = this.requestContext.getProperty("page").length() > 0 ? nexturl + "page=" + this.requestContext.getProperty("page") : nexturl + "file=" + this.requestContext.getProperty("file");
                    if (this.getErrorpage().length() == 0) {
                        this.setErrorpage(this.pageContext.getServletContext().getInitParameter("useraccess"));
                    }
                    this.goErrorPage("The page requires a valid logon.", "&nexturl=" + sapphire.util.HttpUtil.encodeURIComponent(nexturl));
                    rc = 0;
                }
            } else {
                this.goErrorPage("Controlled page can only be accessed via the RequestController.");
                rc = 0;
            }
        }
        if (rc != 0) {
            this.requestContext.setControlledPage(true);
            if (this._expirepage) {
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
        int rc = 6;
        if (this._error.length() > 0) {
            rc = 5;
            this.write(this._error);
        }
        this._error = "";
        this._allowanonymous = false;
        this._expirepage = false;
        super.doEndTag();
        return rc;
    }
}

