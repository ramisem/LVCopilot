/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.el.ELContext
 *  javax.servlet.Servlet
 *  javax.servlet.ServletConfig
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 *  javax.servlet.jsp.el.ExpressionEvaluator
 *  javax.servlet.jsp.el.VariableResolver
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.Trace;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

public class PseudoPageContext
extends PageContext {
    protected Exception exception;
    protected Map<String, Object> values;
    protected ServletContext context;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    public PseudoPageContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    public ServletRequest getRequest() {
        return this.request;
    }

    public ServletResponse getResponse() {
        return this.response;
    }

    public ServletContext getServletContext() {
        return this.context;
    }

    public ServletConfig getServletConfig() {
        Trace.logWarn("PseudoPageContext does not support servlet configuration.");
        return null;
    }

    public JspWriter getOut() {
        Trace.logWarn("PseudoPageContext does not support output.");
        return null;
    }

    public HttpSession getSession() {
        return this.request.getSession();
    }

    public Object findAttribute(String name) {
        Object ret = this.getAttribute(name, 1);
        if (ret != null) {
            return ret;
        }
        ret = this.getAttribute(name, 2);
        if (ret != null) {
            return ret;
        }
        ret = this.getAttribute(name, 3);
        if (ret != null) {
            return ret;
        }
        ret = this.getAttribute(name, 4);
        if (ret != null) {
            return ret;
        }
        return null;
    }

    public Object getAttribute(String name) {
        return this.findAttribute(name);
    }

    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case 4: {
                return this.getServletContext().getAttribute(name);
            }
            case 2: {
                Object ret = this.getRequest().getAttribute(name);
                if (ret == null) {
                    ret = this.getRequest().getParameter(name);
                }
                return ret;
            }
            case 3: {
                if (this.getSession() != null) {
                    return this.getSession().getAttribute(name);
                }
                return null;
            }
            case 1: {
                return this.getValue(name);
            }
        }
        return null;
    }

    public void setAttribute(String name, Object obj) {
        this.setValue(name, obj);
    }

    public void setAttribute(String name, Object obj, int scope) {
        switch (scope) {
            case 4: {
                this.getServletContext().setAttribute(name, obj);
                break;
            }
            case 2: {
                this.getRequest().setAttribute(name, obj);
                break;
            }
            case 3: {
                if (this.getSession() == null) break;
                this.getSession().setAttribute(name, obj);
                break;
            }
            case 1: {
                this.setValue(name, obj);
            }
        }
    }

    public void removeAttribute(String name) {
        this.removeValue(name);
    }

    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case 4: {
                this.getServletContext().removeAttribute(name);
                break;
            }
            case 2: {
                this.getRequest().removeAttribute(name);
                break;
            }
            case 3: {
                if (this.getSession() == null) break;
                this.getSession().removeAttribute(name);
                break;
            }
            case 1: {
                this.removeValue(name);
            }
        }
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        switch (scope) {
            case 4: {
                return this.getServletContext().getAttributeNames();
            }
            case 2: {
                return this.getRequest().getAttributeNames();
            }
            case 3: {
                return this.getSession().getAttributeNames();
            }
            case 1: {
                return this.getValueNames();
            }
        }
        return null;
    }

    public int getAttributesScope(String name) {
        if (this.getValue(name) != null) {
            return 1;
        }
        if (this.getRequest().getAttribute(name) != null) {
            return 2;
        }
        if (this.getRequest().getParameter(name) != null) {
            return 2;
        }
        if (this.getSession().getAttribute(name) != null) {
            return 3;
        }
        if (this.getServletContext().getAttribute(name) != null) {
            return 4;
        }
        return 0;
    }

    public void forward(String url) throws ServletException, IOException {
        Trace.logWarn("PseudoPageContext does not support forwarding.");
    }

    public void include(String url) throws ServletException, IOException {
        this.include(url, true);
    }

    public void include(String url, boolean b) throws ServletException, IOException {
        Trace.logWarn("PseudoPageContext does not support including.");
    }

    public void release() {
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        Trace.logWarn("PseudoPageContext does not expressions.");
        return null;
    }

    public ELContext getELContext() {
        Trace.logWarn("PseudoPageContext does not ELContext.");
        return null;
    }

    public VariableResolver getVariableResolver() {
        Trace.logWarn("PseudoPageContext does not Variable Resolver.");
        return null;
    }

    public void handlePageException(Throwable t) {
        Trace.logError("PseudoPageContext receieved page exception with message: " + this.exception.getMessage(), t);
    }

    public void handlePageException(Exception e) {
        Trace.logInfo("PseudoPageContext handled page exception with message: " + e.getMessage());
        this.exception = e;
    }

    public Exception getException() {
        return this.exception;
    }

    public Object getPage() {
        Trace.logInfo("PseudoPageContext does not support Page");
        return null;
    }

    public void initialize(Servlet srv, ServletRequest req, ServletResponse res, String s1, boolean b1, int i1, boolean b2) {
        Trace.logInfo("PseudoPageContext initialising.");
    }

    protected Object getValue(String key) {
        if (this.values == null) {
            this.values = new HashMap<String, Object>();
        }
        return this.values.get(key);
    }

    protected void setValue(String key, Object value) {
        if (this.values == null) {
            this.values = new HashMap<String, Object>();
        }
        this.values.put(key, value);
    }

    protected void removeValue(String key) {
        if (this.values == null) {
            this.values = new HashMap<String, Object>();
        }
        this.values.remove(key);
    }

    protected Enumeration getValueNames() {
        ArrayList<String> array = new ArrayList<String>();
        for (String s : this.values.keySet()) {
            array.add(s);
        }
        return Collections.enumeration(array);
    }
}

