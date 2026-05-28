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

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.servlet.filter.BaseServletFilter;
import com.labvantage.sapphire.servlet.filter.RequestWrapper;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RejectRequest
extends BaseServletFilter {
    private static HashMap<String, FilterOptions> databaseFilterOptions = new HashMap();
    private static boolean enabled = true;
    private static boolean sqlLoggingEnabled = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        RejectRequest.enabled = enabled;
    }

    public static void setSqlLoggingEnabled(boolean sqlLoggingEnabled) {
        RejectRequest.sqlLoggingEnabled = sqlLoggingEnabled;
    }

    public static void resetDatabaseFilterOptions(String databaseid) {
        databaseFilterOptions.remove(databaseid);
        databaseFilterOptions.remove(databaseid + "_virtual");
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
        if (RejectRequest.isEnabled()) {
            boolean cont = true;
            if (request instanceof HttpServletRequest && ((HttpServletRequest)request).getRequestURI().contains("/WEB-STELLAR/")) {
                cont = false;
            }
            if (cont) {
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
                String databaseid = "";
                boolean virtualUser = false;
                boolean portalUser = false;
                if (connectionid != null && connectionid.length() > 0) {
                    databaseid = SecurityService.getDatabaseId(connectionid);
                    virtualUser = SecurityService.isVirtualUser(connectionid);
                    portalUser = SecurityService.isPortalUser(connectionid);
                } else if (request.getParameter("database") != null && request.getParameter("database").length() > 0) {
                    databaseid = request.getParameter("database");
                    virtualUser = false;
                    portalUser = false;
                }
                if (databaseid != null && databaseid.length() > 0) {
                    HttpServletRequest httpReq;
                    String serverName;
                    FilterOptions filterOptions = databaseFilterOptions.get(databaseid + (virtualUser ? "_virtual" : ""));
                    if (filterOptions == null) {
                        PropertyList policy = Configuration.getDatabaseSecurityPolicy(databaseid, virtualUser, portalUser);
                        if (policy != null) {
                            PropertyList requestFilter = policy.getPropertyList("requestfilter");
                            filterOptions = new FilterOptions();
                            filterOptions.enableFilter = requestFilter.getProperty("enable", "N").equals("Y");
                            filterOptions.checkReferrer = requestFilter.getProperty("checkreferrer", "N").equals("Y");
                            filterOptions.checkCSRFToken = requestFilter.getProperty("checkcsrftoken", "N").equals("Y");
                            filterOptions.sanitizeRequest = requestFilter.getProperty("sanitize", "N").equals("Y");
                            filterOptions.enableHostHeaderInjectionFilter = requestFilter.getProperty("enablehostheaderinjectionfilter", "N").equals("Y");
                            filterOptions.doNotFilterQueryStringPatternList = this.getDoNotFilterQueryStringPatternList(requestFilter.getCollection("donotfilterquerystrings"));
                            filterOptions.doNotCheckCSRFTokenURLList = this.doNotCheckCSRFTokenURLList(requestFilter.getCollection("donotcheckcsrftokenurls"));
                            filterOptions.doNotFilterParamList = this.getDoNotFilterParamList(requestFilter.getCollection("donotfilterparams"));
                            filterOptions.checkForJSFunctionParamList = this.getCheckForJSFunctionParamList(requestFilter.getCollection("checkforjsparams"));
                            filterOptions.hostNameList = this.getHostNamesList(requestFilter.getCollection("hostnames"));
                            filterOptions.blockedGetParamList = this.getBlockedGetParamList(requestFilter.getCollection("blockedgetparams"));
                            filterOptions.jsInjectionPatternList = this.getJsInjectionPatternList(requestFilter.getCollection("jsinjections"));
                            filterOptions.jsInjectionReplaceList = this.getJsInjectionReplaceList(requestFilter.getCollection("jsinjections"));
                            filterOptions.jsInjectionTags = this.getJsInjections(requestFilter.getCollection("jsinjections"));
                            filterOptions.jsReplaceTags = this.getJsReplacements(requestFilter.getCollection("jsinjections"));
                            filterOptions.policy = policy;
                            databaseFilterOptions.put(databaseid + (virtualUser ? "_virtual" : ""), filterOptions);
                        } else {
                            filterOptions = new FilterOptions();
                        }
                    }
                    if (filterOptions.enableFilter && filterOptions.enableHostHeaderInjectionFilter && !filterOptions.hostNameList.contains(serverName = (httpReq = (HttpServletRequest)request).getServerName().toLowerCase()) && !filterOptions.localHostNameList.contains(serverName)) {
                        SecurityPolicyUtil.handleFilterViolation(connectionid != null && connectionid.length() > 0 ? connectionid : "", filterOptions.policy, false, httpReq, "Host", "Host name is not allowed", serverName);
                    }
                    if (connectionid != null && connectionid.length() > 0) {
                        if (filterOptions.enableFilter && !this.doNotFilterQueryString((HttpServletRequest)request, filterOptions)) {
                            if (filterOptions.checkReferrer) {
                                boolean logonUrl;
                                httpReq = (HttpServletRequest)request;
                                String referer = httpReq.getHeader("referer");
                                String server = httpReq.getServerName();
                                String requestURL = httpReq.getRequestURL().toString();
                                boolean bl = logonUrl = requestURL.indexOf("logon.jsp") > 0;
                                if (!(logonUrl || referer != null && referer.indexOf("//" + server) != -1)) {
                                    SecurityPolicyUtil.handleFilterViolation(connectionid, filterOptions.policy, false, httpReq, "Referrer", "Referrer is not correct", referer);
                                }
                            }
                            if (filterOptions.checkCSRFToken) {
                                String command = request.getParameter("command");
                                String[] csrfToken = null;
                                if ("ajax".equals(command)) {
                                    String csrfheader;
                                    String string = csrfheader = ((HttpServletRequest)request).getHeader("csrftoken") != null ? HttpUtil.decodeURIComponent(((HttpServletRequest)request).getHeader("csrftoken")) : null;
                                    if (csrfheader != null) {
                                        csrfToken = new String[]{csrfheader};
                                    }
                                } else {
                                    csrfToken = request.getParameterValues("csrftoken");
                                }
                                boolean isValidSession = false;
                                try {
                                    ((HttpServletRequest)request).getSession().getAttribute("dummy");
                                    isValidSession = true;
                                }
                                catch (Exception requestURL) {
                                    // empty catch block
                                }
                                if (command != null && isValidSession && !"upload".equals(command) && (((HttpServletRequest)request).getMethod().equals("POST") || "sdiform".equals(command) || "ajax".equals(command) || "wizard".equals(command)) && ("page".equals(command) || "sdiform".equals(command) || "ajax".equals(command) || "wizard".equals(command) || "file".equals(command) && request.getParameter("file") != null && request.getParameter("file").contains("bulletin") || csrfToken != null && csrfToken.length > 0 && !"undefined".equals(csrfToken[0]))) {
                                    String serverToken = (String)((HttpServletRequest)request).getSession().getAttribute("csrftoken");
                                    if (serverToken == null && csrfToken != null && csrfToken.length > 0) {
                                        serverToken = csrfToken[0];
                                        ((HttpServletRequest)request).getSession().setAttribute("csrftoken", (Object)serverToken);
                                    }
                                    if (csrfToken == null || !csrfToken[csrfToken.length - 1].equals(serverToken)) {
                                        String url = ((HttpServletRequest)request).getRequestURI() + "?" + ((HttpServletRequest)request).getQueryString();
                                        boolean csrfexcluded = false;
                                        if (filterOptions.doNotCheckCSRFTokenURLList != null) {
                                            for (int i = 0; i < filterOptions.doNotCheckCSRFTokenURLList.size(); ++i) {
                                                String donotfilterurl = (String)filterOptions.doNotCheckCSRFTokenURLList.get(i);
                                                if (url.indexOf(donotfilterurl) < 0) continue;
                                                csrfexcluded = true;
                                                break;
                                            }
                                        }
                                        if (!csrfexcluded && SecurityPolicyUtil.checkIfEmpowerURL((HttpServletRequest)request, url)) {
                                            csrfexcluded = true;
                                        }
                                        if (!csrfexcluded) {
                                            SecurityPolicyUtil.handleFilterViolation(connectionid, filterOptions.policy, false, (HttpServletRequest)request, "CSRF", "csrftoken is not correct", url);
                                        }
                                    }
                                }
                            }
                            chain.doFilter((ServletRequest)new RequestWrapper((HttpServletRequest)request, filterOptions, connectionid, sqlLoggingEnabled), response);
                        } else {
                            chain.doFilter(request, response);
                        }
                    } else {
                        chain.doFilter(request, response);
                    }
                } else {
                    chain.doFilter(request, response);
                }
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private ArrayList getDoNotFilterQueryStringPatternList(PropertyListCollection doNotFilterQueryStrings) {
        ArrayList<String> list = new ArrayList<String>();
        if (doNotFilterQueryStrings != null) {
            for (int i = 0; i < doNotFilterQueryStrings.size(); ++i) {
                String queryString = doNotFilterQueryStrings.getPropertyList(i).getProperty("querystring");
                StringBuffer sb = new StringBuffer(queryString.length() + 10);
                for (int j = 0; j < queryString.length(); ++j) {
                    if (queryString.charAt(j) == '*') {
                        sb.append(".*");
                        continue;
                    }
                    sb.append(queryString.charAt(j));
                }
                list.add(sb.toString());
            }
        }
        return list;
    }

    private ArrayList doNotCheckCSRFTokenURLList(PropertyListCollection doNotCheckCSRFTokenURLs) {
        ArrayList<String> list = new ArrayList<String>();
        if (doNotCheckCSRFTokenURLs != null) {
            for (int i = 0; i < doNotCheckCSRFTokenURLs.size(); ++i) {
                String url = doNotCheckCSRFTokenURLs.getPropertyList(i).getProperty("URL");
                list.add(url);
            }
        }
        return list;
    }

    private HashSet getDoNotFilterParamList(PropertyListCollection doNotFilterParams) {
        HashSet<String> set = new HashSet<String>();
        if (doNotFilterParams != null) {
            for (int i = 0; i < doNotFilterParams.size(); ++i) {
                set.add(doNotFilterParams.getPropertyList(i).getProperty("param").toLowerCase());
            }
        }
        return set;
    }

    private HashSet getJsInjectionPatternList(PropertyListCollection jsInjections) {
        HashSet<String> set = new HashSet<String>();
        if (jsInjections != null) {
            for (int i = 0; i < jsInjections.size(); ++i) {
                set.add(jsInjections.getPropertyList(i).getProperty("jsinjection"));
            }
        }
        return set;
    }

    private HashSet getJsInjectionReplaceList(PropertyListCollection jsInjections) {
        HashSet<String> set = new HashSet<String>();
        if (jsInjections != null) {
            for (int i = 0; i < jsInjections.size(); ++i) {
                set.add(jsInjections.getPropertyList(i).getProperty("jsreplacement"));
            }
        }
        return set;
    }

    private String[] getJsInjections(PropertyListCollection jsInjections) {
        if (jsInjections != null) {
            String[] injections = new String[jsInjections.size()];
            for (int i = 0; i < jsInjections.size(); ++i) {
                injections[i] = jsInjections.getPropertyList(i).getProperty("jsinjection");
            }
            return injections;
        }
        return new String[0];
    }

    private String[] getJsReplacements(PropertyListCollection jsInjections) {
        if (jsInjections != null) {
            String[] replacements = new String[jsInjections.size()];
            for (int i = 0; i < jsInjections.size(); ++i) {
                replacements[i] = jsInjections.getPropertyList(i).getProperty("jsreplacement");
            }
            return replacements;
        }
        return new String[0];
    }

    private HashSet getCheckForJSFunctionParamList(PropertyListCollection checkForJSParams) {
        HashSet<String> set = new HashSet<String>();
        if (checkForJSParams != null) {
            for (int i = 0; i < checkForJSParams.size(); ++i) {
                set.add(checkForJSParams.getPropertyList(i).getProperty("param").toLowerCase());
            }
        }
        return set;
    }

    private HashSet getHostNamesList(PropertyListCollection hostNamesList) {
        HashSet<String> set = new HashSet<String>();
        if (hostNamesList != null) {
            for (int i = 0; i < hostNamesList.size(); ++i) {
                set.add(hostNamesList.getPropertyList(i).getProperty("hostname").toLowerCase());
            }
        }
        return set;
    }

    private HashSet getBlockedGetParamList(PropertyListCollection blockedGetParams) {
        HashSet<String> set = new HashSet<String>();
        if (blockedGetParams != null) {
            for (int i = 0; i < blockedGetParams.size(); ++i) {
                set.add(blockedGetParams.getPropertyList(i).getProperty("param").toLowerCase());
            }
        }
        return set;
    }

    private boolean doNotFilterQueryString(HttpServletRequest request, FilterOptions filterOptions) {
        boolean donotfilter = false;
        String queryString = request.getQueryString();
        if (queryString != null && filterOptions.doNotFilterQueryStringPatternList != null) {
            for (String np : filterOptions.doNotFilterQueryStringPatternList) {
                Pattern p = Pattern.compile(np);
                Matcher m = p.matcher(queryString);
                if (!m.matches()) continue;
                donotfilter = true;
                break;
            }
        }
        return donotfilter;
    }

    public class FilterOptions {
        PropertyList policy;
        ArrayList doNotFilterQueryStringPatternList;
        ArrayList doNotCheckCSRFTokenURLList;
        HashSet doNotFilterParamList;
        HashSet checkForJSFunctionParamList;
        HashSet blockedGetParamList;
        HashSet jsInjectionPatternList;
        HashSet jsInjectionReplaceList;
        HashSet<String> hostNameList;
        HashSet<String> localHostNameList = new HashSet();
        String[] jsInjectionTags;
        String[] jsReplaceTags;
        boolean enableFilter = false;
        boolean checkReferrer = true;
        boolean checkCSRFToken = true;
        boolean sanitizeRequest = false;
        boolean enableHostHeaderInjectionFilter = false;

        public FilterOptions() {
            this.localHostNameList.add("localhost");
            this.localHostNameList.add("127.0.0.1");
            try {
                this.localHostNameList.add(InetAddress.getLocalHost().getHostName().toLowerCase());
                this.localHostNameList.add(InetAddress.getLocalHost().getHostAddress());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

