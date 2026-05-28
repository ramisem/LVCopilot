/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.RequestDispatcher
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.tagext.TagSupport
 */
package sapphire.tagext;

import com.labvantage.sapphire.Trace;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.servlet.RequestContext;
import sapphire.util.LogContext;
import sapphire.xml.PropertyList;

public abstract class BaseTagSupport
extends TagSupport {
    protected static final int SUCCESS = 1;
    protected static final int FAILURE = 2;
    private String _errorpage = "";
    private PropertyList attibutes = new PropertyList();
    protected RequestContext requestContext;
    protected LogContext logContext;
    private String loggerName;

    public void doInit() {
        this.loggerName = ((Object)((Object)this)).getClass().getName().substring(((Object)((Object)this)).getClass().getPackage().getName().length() + 1).toUpperCase();
        this.requestContext = RequestContext.getRequestContext((HttpServletRequest)this.pageContext.getRequest());
        this.logContext = new LogContext(this.requestContext != null ? this.requestContext.getConnectionId() : "(none)");
    }

    public boolean isControlledPage() {
        return this.requestContext != null && this.requestContext.isControlledPage();
    }

    public int doEndTag() throws JspTagException {
        try {
            super.doEndTag();
        }
        catch (JspException e) {
            throw new JspTagException((Throwable)e);
        }
        finally {
            this.requestContext = null;
            this.logContext = null;
            this._errorpage = "";
            this.attibutes.clear();
            this.pageContext = null;
        }
        return 6;
    }

    public void setAttribute(String paramid, String paramvalue) {
        this.attibutes.setProperty(paramid, paramvalue);
    }

    public String getAttribute(String paramid) {
        return this.attibutes.getProperty(paramid);
    }

    public String getAttribute(String paramid, String defaultValue) {
        return this.attibutes.getProperty(paramid, defaultValue);
    }

    public PropertyList getAttributes() {
        return this.attibutes;
    }

    protected void setNameserverlist(String nameserverlist) {
    }

    protected String getNameserverlist() {
        return "";
    }

    protected String getConnectionId() {
        return this.requestContext != null ? this.requestContext.getConnectionId() : "";
    }

    public void setErrorpage(String errorpage) {
        this._errorpage = errorpage;
    }

    public String getErrorpage() {
        return this._errorpage;
    }

    protected RequestContext getRequestContext() {
        return this.requestContext != null ? this.requestContext : (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
    }

    protected void write(String output) throws JspTagException {
        try {
            JspWriter out = this.pageContext.getOut();
            out.write(output);
        }
        catch (IOException e) {
            throw new JspTagException("Failed to write body: " + e.getMessage());
        }
    }

    protected void logError(String errormsg) {
        Trace.logError(this.loggerName, (Object)errormsg, this.logContext);
    }

    protected void logError(String errormsg, Exception exception) {
        Trace.logError(this.loggerName, errormsg, exception, this.logContext);
    }

    protected void logDebug(Object message) {
        Trace.logDebug(message, this.logContext);
    }

    protected void logTrace(String tracemsg) {
        Trace.logInfo(this.loggerName, tracemsg, this.logContext);
    }

    public void goErrorPage(String errormsg, String extraparameters) throws JspTagException {
        this.logError(errormsg);
        if (this._errorpage == null || this._errorpage.length() == 0) {
            this._errorpage = this.pageContext.getServletContext().getInitParameter("errorpage");
        }
        if (this._errorpage != null && this._errorpage.length() > 0) {
            String rc = "/" + this.pageContext.getServletContext().getInitParameter("RequestControllerName") + "?page=&file=" + this._errorpage;
            if (extraparameters != null) {
                rc = rc + extraparameters;
            }
            RequestDispatcher rd = this.pageContext.getServletContext().getRequestDispatcher(rc + "&errormsg=" + errormsg);
            try {
                rd.forward(this.pageContext.getRequest(), this.pageContext.getResponse());
            }
            catch (ServletException e) {
                throw new JspTagException("Failed to forward to error page: " + e.getMessage());
            }
            catch (IOException e) {
                throw new JspTagException("Failed to forward to error page: " + e.getMessage());
            }
        } else {
            throw new JspTagException("No error page specified");
        }
    }

    public void goErrorPage(String errormsg) throws JspTagException {
        this.goErrorPage(errormsg, "");
    }
}

