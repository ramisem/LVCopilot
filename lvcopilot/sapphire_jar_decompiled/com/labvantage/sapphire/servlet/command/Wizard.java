/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspFactory
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.PseudoPageContext;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

public class Wizard {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void view(Servlet servlet, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            int p;
            try {
                Configuration config = Configuration.getInstance();
                p = config.getPlatform();
            }
            catch (Exception e) {
                throw new ServletException("Failed to obtain plaform", (Throwable)e);
            }
            PageContext pageContext = p == 3 && servlet instanceof HttpServlet ? new PseudoPageContext(((HttpServlet)servlet).getServletContext(), request, response) : JspFactory.getDefaultFactory().getPageContext(servlet, (ServletRequest)request, (ServletResponse)response, "", true, 0, false);
            try {
                com.labvantage.sapphire.pageelements.controls.Wizard wizard = new com.labvantage.sapphire.pageelements.controls.Wizard(pageContext);
                response.getWriter().println(wizard.getHtml());
            }
            finally {
                if (p != 3) {
                    JspFactory.getDefaultFactory().releasePageContext(pageContext);
                }
            }
        }
        catch (IOException ioe) {
            throw new ServletException((Throwable)ioe);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void view(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try (PrintWriter output = response.getWriter();){
            com.labvantage.sapphire.pageelements.controls.Wizard wizard = new com.labvantage.sapphire.pageelements.controls.Wizard(servletContext, request);
            output.println(wizard.getHtml());
        }
        catch (IOException ioe) {
            throw new ServletException((Throwable)ioe);
        }
    }
}

