/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletRequestWrapper
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.filter.HistoryWrapper;
import com.labvantage.sapphire.servlet.filter.RejectRequest;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;

public final class RequestWrapper
extends HttpServletRequestWrapper {
    HttpServletRequest servletRequest;
    RejectRequest.FilterOptions filterOptions;
    String connectionid;
    boolean sqlLoggingEnabled = false;

    public RequestWrapper(HttpServletRequest servletRequest, RejectRequest.FilterOptions filterOptions, String connectionid, boolean sqlLoggingEnabled) {
        super(servletRequest);
        this.filterOptions = filterOptions;
        this.servletRequest = servletRequest;
        this.connectionid = connectionid;
        this.sqlLoggingEnabled = sqlLoggingEnabled;
    }

    private boolean checkParameterName(String parameter) {
        return !parameter.contains("(") && !parameter.contains("<") && !parameter.contains("&") && !parameter.contains("\"") && !parameter.contains("'") && !parameter.contains("//");
    }

    private boolean checkSDIFormParam(String parameter) {
        String regex = "((.)+[0-9]+)_(.|\\p{Punct}|\\p{Space})*";
        try {
            if (parameter.matches(regex)) {
                return true;
            }
        }
        catch (Exception e) {
            Trace.log("Error:" + e.getMessage());
        }
        return false;
    }

    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values != null) {
            for (int i = 0; i < values.length; ++i) {
                String value = values[i];
                values[i] = value = this.checkParameterValue(parameter, value);
            }
            return values;
        }
        return null;
    }

    public Map getParameterMap() {
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        Enumeration e = super.getParameterNames();
        while (e.hasMoreElements()) {
            String parameter = (String)e.nextElement();
            String[] values = this.getParameterValues(parameter);
            map.put(parameter, values);
        }
        return map;
    }

    private String checkParameterValue(String parameter, String value) {
        if ((parameter.equals("querywhere") || parameter.equals("restrictivewhere")) && value != null && value.length() > 1) {
            value = this.servletRequest instanceof HistoryWrapper ? value : EncryptDecrypt.unobfsql((ServletRequest)this.servletRequest, value, true);
        }
        try {
            block17: {
                if (this.sqlLoggingEnabled && value != null && value.length() > 1 && (parameter.toLowerCase().contains("from") || parameter.toLowerCase().contains("where") || parameter.toLowerCase().contains("orderby") || parameter.toLowerCase().contains("sql") && !parameter.equalsIgnoreCase("sqlcode") || value.toLowerCase().contains("sql") && !value.equalsIgnoreCase("sqlcode") || value.toLowerCase().contains("queryfrom") || value.toLowerCase().contains("querywhere") || value.toLowerCase().contains("restrictivewhere") || value.toLowerCase().startsWith("select ") || value.toLowerCase().startsWith("update ") || value.toLowerCase().startsWith("delete ") || value.toLowerCase().contains(" from ") || value.toLowerCase().contains(" union ") || value.toLowerCase().contains(" where ")) && !parameter.toLowerCase().contains("mergewhere") && !parameter.toLowerCase().contains("mergequerywhere") && !parameter.equalsIgnoreCase("fromsearchbar") && !parameter.equalsIgnoreCase("emptymessage")) {
                    SecurityPolicyUtil.handleFilterViolation(this.connectionid, this.filterOptions.policy, false, this.servletRequest, parameter, "Param may contain SQL snippet", value);
                }
                if (!this.hasEL(parameter, value) && (SecurityPolicyUtil.isSafeAjaxRequest(super.getParameter("ajaxclass")) || SecurityPolicyUtil.isValidParam(parameter.toLowerCase()) || this.filterOptions.doNotFilterParamList.contains(parameter.toLowerCase()))) {
                    return value;
                }
                if (!this.checkParameterName(parameter)) {
                    try {
                        SecurityPolicyUtil.handleFilterViolation(this.connectionid, this.filterOptions.policy, false, this.servletRequest, parameter, "Param name is vulnerable", parameter);
                    }
                    catch (ServletException se) {
                        if (parameter.contains("(UNDEFINED)") || parameter.toLowerCase().contains("select")) break block17;
                        throw new RuntimeException("Error: Not Allowed value by SecurityPolicy: " + SafeHTML.encodeForHTML(parameter));
                    }
                }
            }
            if (this.getMethod().toUpperCase().equals("GET") && this.filterOptions.blockedGetParamList.contains(parameter)) {
                SecurityPolicyUtil.handleFilterViolation(this.connectionid, this.filterOptions.policy, false, this.servletRequest, parameter, "Param not permitted in GET request", parameter);
            }
            if (value == null) {
                return null;
            }
            if (this.hasJsTags(value) || this.hasEL(parameter, value)) {
                if (this.checkSDIFormParam(parameter)) {
                    if (parameter.indexOf("_calcrule") < 0 && parameter.indexOf("_processingscript") < 0 && value.indexOf("$G{") < 0) {
                        throw new RuntimeException("Error: Not Allowed value by SecurityPolicy: " + parameter + "=" + SafeHTML.encodeForHTML(value));
                    }
                } else {
                    SecurityPolicyUtil.handleFilterViolation(this.connectionid, this.filterOptions.policy, this.filterOptions.sanitizeRequest, this.servletRequest, parameter, "URL has JS tags or EL", value);
                    if (this.filterOptions.sanitizeRequest) {
                        value = this.sanitizeJsTagsEL(parameter, value);
                    }
                }
            }
            if (this.filterOptions.checkForJSFunctionParamList.contains(parameter.toLowerCase()) && this.hasJsFunction(value)) {
                SecurityPolicyUtil.handleFilterViolation(this.connectionid, this.filterOptions.policy, this.filterOptions.sanitizeRequest, this.servletRequest, parameter, "Vulnerable parameter has JS function", value);
                if (this.filterOptions.sanitizeRequest) {
                    value = this.sanitizeJsFunction(value);
                }
            }
        }
        catch (ServletException servletException) {
            // empty catch block
        }
        return value;
    }

    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        value = this.checkParameterValue(parameter, value);
        return value;
    }

    private boolean hasJsFunction(String value) {
        boolean r = false;
        if (value.contains("(") && value.indexOf(")", value.indexOf("(")) > -1) {
            String s = StringUtil.replaceAll(value, "(null)", "");
            if ((s = StringUtil.replaceAll(s, ";(", "")).contains("(") && s.indexOf(")", s.indexOf("(")) > -1) {
                r = true;
            }
        }
        return r;
    }

    private String sanitizeJsFunction(String value) {
        String ret = value;
        ret = StringUtil.replaceAll(ret, "(", "");
        ret = StringUtil.replaceAll(ret, ")", "");
        return ret;
    }

    private boolean hasJsTags(String value) {
        for (int i = 0; i < this.filterOptions.jsInjectionTags.length; ++i) {
            if (this.filterOptions.jsInjectionTags[i].trim().length() <= 0 || !value.toLowerCase().contains(this.filterOptions.jsInjectionTags[i].toLowerCase())) continue;
            if (this.filterOptions.jsInjectionTags[i].trim().toLowerCase().indexOf("on") == 0) {
                return value.indexOf("=") > 0;
            }
            return true;
        }
        return false;
    }

    private boolean hasEL(String parameter, String value) {
        return value != null && !parameter.equalsIgnoreCase("commandrequest") && parameter.indexOf("_calcrule") < 0 && parameter.indexOf("_processingscript") < 0 && !"groovyscript".equals(parameter) && value.indexOf("$G{") < 0 && value.indexOf("${") >= 0 && value.indexOf("}") > 0;
    }

    private String sanitizeJsTagsEL(String parameter, String value) {
        for (int i = 0; i < this.filterOptions.jsInjectionTags.length; ++i) {
            value = value.replaceAll("(?i)" + this.filterOptions.jsInjectionTags[i], this.filterOptions.jsReplaceTags[i]);
        }
        if (this.hasEL(parameter, value)) {
            value = StringUtil.replaceAll(value, "${", "$ {");
        }
        return value;
    }
}

