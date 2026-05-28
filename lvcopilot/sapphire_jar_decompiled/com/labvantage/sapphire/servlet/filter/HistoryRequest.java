/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.FilterChain
 *  javax.servlet.FilterConfig
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.filter.BaseServletFilter;
import com.labvantage.sapphire.servlet.filter.HistoryWrapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.HttpUtil;

public class HistoryRequest
extends BaseServletFilter {
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        HistoryRequest.enabled = enabled;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (HistoryRequest.isEnabled()) {
            request.setCharacterEncoding("UTF-8");
            String connectionid = request.getParameter("connectionid");
            if (connectionid == null || connectionid.length() == 0) {
                if (request instanceof HttpServletRequest) {
                    HttpUtil httpUtil = new HttpUtil((HttpServletRequest)request, (HttpServletResponse)response);
                    connectionid = httpUtil.getCookieValue("connectionid");
                } else {
                    this.log("Failed to obtain connection id.");
                }
            }
            if (connectionid != null && connectionid.length() > 0) {
                Trace.startThreadMDCByConnectionid(connectionid, "Browser");
                ConnectionProcessor cp = new ConnectionProcessor();
                if (cp.checkConnection(connectionid)) {
                    String command = request.getParameter("command");
                    if ("history".equals(command) || "state".equals(command) || (command == null || command.length() == 0) && request.getParameter("history") != null && request.getParameter("history").length() > 0) {
                        try {
                            chain.doFilter((ServletRequest)new HistoryWrapper((HttpServletRequest)request, connectionid), response);
                        }
                        catch (SapphireException e) {
                            throw new ServletException(e.getMessage());
                        }
                    } else {
                        chain.doFilter(request, response);
                    }
                } else {
                    this.log("Connection invalid.");
                    chain.doFilter(request, response);
                }
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}

