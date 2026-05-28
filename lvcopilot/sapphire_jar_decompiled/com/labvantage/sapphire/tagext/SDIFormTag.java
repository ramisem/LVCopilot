/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.SafeHTML;

public class SDIFormTag
extends BaseBodyTagSupport {
    private String _error = "";
    private String _nextpage = "";
    private String _formid = "";
    private boolean ping = false;
    private String _prefixlist = "";
    private String _rsetlist = "";
    private boolean formSuccess = false;
    private boolean formFailure = false;
    private boolean _hasOldFormSucessTag = false;
    private boolean _pingWritten = false;
    private String _rsetlockoption;
    private int _rsetlocktype;

    protected void setRsetLockOption(String lockoption, String primarylockoption, String datalockoption) {
        if (lockoption.length() > 0) {
            this._rsetlockoption = lockoption;
            this._rsetlocktype = 3;
        } else if (primarylockoption.length() > 0) {
            this._rsetlockoption = primarylockoption;
            this._rsetlocktype = 1;
        } else if (datalockoption.length() > 0) {
            this._rsetlockoption = datalockoption;
            this._rsetlocktype = 2;
        } else {
            this._rsetlockoption = "";
            this._rsetlocktype = -1;
        }
    }

    public boolean getFormSuccess() {
        return this.formSuccess;
    }

    public boolean getFormFailure() {
        return this.formFailure;
    }

    public void setHasOldFormSucessTag(boolean hasOldFormSucessTag) {
        this._hasOldFormSucessTag = hasOldFormSucessTag;
    }

    public void setNextpage(String nextpage) {
        this._nextpage = nextpage;
    }

    public void setId(String id) {
        this._formid = id;
    }

    public String getId() {
        return this._formid;
    }

    protected String getPingHtml() {
        StringBuffer sb = new StringBuffer();
        if (this.ping && this._rsetlist.length() > 0) {
            try {
                if (!this._pingWritten) {
                    this._pingWritten = true;
                    sb.append("<iframe frameborders=\"0\" style=\"display:none;\" src=\"rc?command=file&file=WEB-CORE/pagetypes/maint/rsetping.jsp&rsetid=").append(this._rsetlist).append("\" id=\"rset_iframe\" name=\"rset_iframe\">\n");
                    sb.append("</iframe >\n");
                } else {
                    sb.append("<script  type=\"text/javascript\">\n");
                    sb.append("  if(typeof(rset_iframe.init)!='undefined'){\n");
                    sb.append("     rset_iframe.init('").append(this._rsetlist).append("');\n");
                    sb.append("   }else{");
                    sb.append("     sapphire.events.attachEvent(rset_iframe, 'onload', function(){rset_iframe.init('").append(this._rsetlist).append("')});\n");
                    sb.append("   }\n");
                    sb.append("</script>\n");
                }
            }
            catch (Exception e) {
                this.logError("Could not write out ping.", e);
            }
        }
        return sb.toString();
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

    public void addPrefix(String prefix) {
        this._prefixlist = this._prefixlist + (this._prefixlist.length() > 0 ? ";" : "") + prefix;
    }

    public void addRset(String rsetid) {
        this._rsetlist = (this._rsetlist.length() > 0 ? ";" : "") + rsetid;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        this.doInit();
        HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
        StringBuffer formaction = new StringBuffer("action=\"");
        String querystring = "";
        try {
            querystring = SafeHTML.encodeForURL(request.getQueryString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        querystring = HttpUtil.decodeURIComponent(querystring);
        this.evaluateExpressions();
        String transactionid = this._formid + "_" + System.currentTimeMillis();
        this.pageContext.setAttribute("transactionid", (Object)transactionid);
        formaction.append(this.pageContext.getServletContext().getInitParameter("RequestControllerName")).append("?command=sdiform\"");
        this.write("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/activitylog.js\"></script>\n");
        this.write("<form onchange=\"logSDIFormActivity( this, event )\" " + formaction + (this._formid.length() > 0 ? " id=\"" + this._formid + "\" name=\"" + this._formid + "\"" : "") + " method=\"post\">\n");
        this.write("<input type=\"hidden\" id=\"transactionid\" name=\"transactionid\" value=\"" + transactionid + "\">\n");
        if (this._nextpage.length() > 0) {
            try {
                this.write("<input type=\"hidden\" id=\"__nexturl\" name=\"__nexturl\" value=\"" + SafeHTML.encodeForURL(this._nextpage) + "\">\n");
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (querystring.contains("&page=") || querystring.contains("&file=")) {
            this.write("<input type=\"hidden\" id=\"__nexturl\" name=\"__nexturl\" value=\"" + "(self)rc?" + querystring + "\">\n");
        } else if (this.requestContext.getProperty("command").equalsIgnoreCase("page")) {
            this.write("<input type=\"hidden\" id=\"__nexturl\" name=\"__nexturl\" value=\"" + "(self)rc?command=page&page=" + this.requestContext.getProperty("page") + "\">\n");
        } else if (this.requestContext.getProperty("command").equalsIgnoreCase("file")) {
            this.write("<input type=\"hidden\" id=\"__nexturl\" name=\"__nexturl\" value=\"" + "(self)rc?command=file&file=" + this.requestContext.getProperty("file") + "\">\n");
        } else {
            this.write("<input type=\"hidden\" id=\"__nexturl\" name=\"__nexturl\" value=\"" + "(self)rc?" + querystring + "\">\n");
        }
        if (querystring.contains("&page=") || querystring.contains("&file=")) {
            this.write("<input type=\"hidden\"  name=\"__self\" value=\"" + "rc?" + querystring + "\">\n");
        } else if (this.requestContext.getProperty("command").equalsIgnoreCase("page")) {
            this.write("<input type=\"hidden\"  name=\"__self\" value=\"" + "rc?command=page&page=" + this.requestContext.getProperty("page") + "\">\n");
        } else if (this.requestContext.getProperty("command").equalsIgnoreCase("file")) {
            this.write("<input type=\"hidden\"  name=\"__self\" value=\"" + "rc?command=file&file=" + this.requestContext.getProperty("file") + "\">\n");
        } else {
            this.write("<input type=\"hidden\"  name=\"__self\" value=\"" + "rc?" + querystring + "\">\n");
        }
        this.write("<input type=\"hidden\" name=\"__formcommand\" value=\"display\">\n");
        String formsuccess = this.requestContext.getProperty("__formsuccess");
        if (formsuccess != null) {
            this.formSuccess = formsuccess.equals("true");
            this.formFailure = formsuccess.equals("false");
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
    }

    public int doAfterBody() throws JspTagException {
        this.write(this.getPingHtml());
        this.write("<input type=\"hidden\" name=\"__prefixlist\" id=\"__prefixlist\" value=\"" + this._prefixlist + "\">");
        this.write("<input type=\"hidden\" name=\"__hasoldformsucesstag\" id=\"__hasoldformsucesstag\" value=\"" + this._hasOldFormSucessTag + "\">");
        this.write("</form> ");
        this.write("<script>sapphire.events.attachEvent( window, sapphire.browser.id=='SA' ? 'onunload' : 'onbeforeunload', chkformUnload );");
        this.write("\nfunction chkformUnload() {\n");
        this.write("  typeof(formUnload) != 'undefined' ? formUnload() : '' ;\n}");
        this.write("\nvar enableDynamicAudit = true;");
        this.write("\n</script>");
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._error = "";
        this._nextpage = "";
        this._formid = "";
        this.ping = false;
        this._prefixlist = "";
        this._rsetlist = "";
        this._pingWritten = false;
        this.formSuccess = false;
        this.formFailure = false;
        this._hasOldFormSucessTag = false;
        this._rsetlocktype = -1;
        this._rsetlockoption = "";
        super.doEndTag();
        return rc;
    }

    protected void evaluateExpressions() {
        this._nextpage = JstlUtil.evaluateExpression(this._nextpage, this.pageContext, "").toString();
        this._formid = JstlUtil.evaluateExpression(this._formid, this.pageContext, "").toString();
    }
}

