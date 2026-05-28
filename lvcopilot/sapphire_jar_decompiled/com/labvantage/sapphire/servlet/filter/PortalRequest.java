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
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.filter.BaseServletFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class PortalRequest
extends BaseServletFilter {
    private static boolean enabled = true;
    private static final String WEBSOCKETCRSFTOKEN = "WSToken";
    private static final String WEBSOCKETSECRET = "WSSec";
    private ArrayList<String> patternList;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        PortalRequest.enabled = enabled;
    }

    private ArrayList<String> getPatternList(FilterConfig filterConfig) {
        String patternList = filterConfig.getInitParameter("allowed");
        ArrayList<String> list = new ArrayList<String>();
        if (patternList != null) {
            String[] patterns;
            for (String pattern : patterns = StringUtil.split(patternList, ";")) {
                StringBuffer sb = new StringBuffer(pattern.length() + 10);
                for (int j = 0; j < pattern.length(); ++j) {
                    if (pattern.charAt(j) == '*') {
                        sb.append(".*");
                        continue;
                    }
                    sb.append(pattern.charAt(j));
                }
                list.add(sb.toString());
            }
        }
        return list;
    }

    private boolean matchRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) {
            return false;
        }
        String param = request.getQueryString();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            path = path + pathInfo;
        }
        if (param != null) {
            path = path + "&" + param;
        }
        if (this.patternList == null || this.patternList.size() == 0) {
            return true;
        }
        boolean pathInCollection = false;
        for (String np : this.patternList) {
            Pattern p = Pattern.compile(np);
            Matcher m = p.matcher(path);
            if (!m.matches()) continue;
            pathInCollection = true;
            break;
        }
        return pathInCollection;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.patternList = this.getPatternList(filterConfig);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (PortalRequest.isEnabled()) {
            HttpSession session = null;
            if (request instanceof HttpServletRequest && ((HttpServletRequest)request).getRequestURI().endsWith("/stellar") && ((HttpServletRequest)request).getHeader("Upgrade") != null && ((HttpServletRequest)request).getHeader("Upgrade").equalsIgnoreCase("websocket")) {
                boolean websocketcsrf;
                String websocketSecret;
                session = ((HttpServletRequest)request).getSession(false);
                boolean clientDebug = session != null && session.getAttribute("clientDebug") != null && (Boolean)session.getAttribute("clientDebug") != false;
                Trace.logDebug("PortalRequest - Session id for websocket crsf token = " + (session != null ? session.getId() : "(null)"));
                String string = websocketSecret = session != null && session.getAttribute(WEBSOCKETSECRET) != null ? session.getAttribute(WEBSOCKETSECRET).toString() : "";
                if (websocketSecret != null && websocketSecret.length() > 0) {
                    Trace.logDebug("PortalRequest - Websocket secret provided");
                } else {
                    Trace.logInfo("PortalRequest - Websocket secret not provided");
                }
                String crsfTokenSession = session != null && session.getAttribute(WEBSOCKETCRSFTOKEN) != null ? session.getAttribute(WEBSOCKETCRSFTOKEN).toString() : "";
                HttpUtil httpUtil = new HttpUtil((HttpServletRequest)request, (HttpServletResponse)response);
                String crsfTokenCookie = httpUtil.getCookieValue(WEBSOCKETCRSFTOKEN);
                boolean bl = websocketcsrf = this.filterConfig.getInitParameter("websocketcsrf") == null || !this.filterConfig.getInitParameter("websocketcsrf").equalsIgnoreCase("N");
                if (!websocketcsrf || crsfTokenCookie.length() > 0 && crsfTokenSession.equals(crsfTokenCookie)) {
                    session = ((HttpServletRequest)request).getSession(true);
                    Trace.logDebug("PortalRequest - Session id for websocket handshake = " + (session != null ? session.getId() : "(null)"));
                    session.setAttribute(WEBSOCKETCRSFTOKEN, (Object)crsfTokenSession);
                    session.setAttribute(WEBSOCKETSECRET, (Object)websocketSecret);
                    session.setAttribute("clientDebug", (Object)clientDebug);
                    session.setAttribute("WebsocketRemoteAddr", (Object)request.getRemoteAddr());
                    session.setAttribute("WebsocketRemoteHost", (Object)request.getRemoteHost());
                } else {
                    Trace.logError("Websocket crsf token not provided or does not match.");
                    throw new ServletException("Unable to open websocket connection due to security.");
                }
            }
            if (this.filterConfig.getInitParameter("enabled") == null || !this.filterConfig.getInitParameter("enabled").equalsIgnoreCase("N")) {
                String redirect;
                if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) throw new ServletException("Invalid request");
                if (this.matchRequest((HttpServletRequest)request)) {
                    chain.doFilter(request, response);
                    return;
                }
                String string = redirect = this.filterConfig.getInitParameter("redirect") != null && this.filterConfig.getInitParameter("redirect").length() > 0 ? this.filterConfig.getInitParameter("redirect") : "";
                if (redirect.length() > 0) {
                    ((HttpServletResponse)response).sendRedirect(redirect);
                    return;
                }
                int code = 400;
                try {
                    code = this.filterConfig.getInitParameter("code") != null && this.filterConfig.getInitParameter("code").length() > 0 ? Integer.parseInt(this.filterConfig.getInitParameter("code")) : 400;
                }
                catch (NumberFormatException crsfTokenSession) {
                    // empty catch block
                }
                String message = this.filterConfig.getInitParameter("message") != null && this.filterConfig.getInitParameter("message").length() > 0 ? this.filterConfig.getInitParameter("message") : "Not found";
                ((HttpServletResponse)response).sendError(code, message);
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, response);
    }
}

