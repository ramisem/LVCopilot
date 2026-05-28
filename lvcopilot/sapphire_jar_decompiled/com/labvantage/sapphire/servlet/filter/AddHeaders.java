/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.FilterChain
 *  javax.servlet.FilterConfig
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.filter.BaseServletFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.StringUtil;

public class AddHeaders
extends BaseServletFilter {
    static final String LABVANTAGE_CVS_ID = "$Revision: 78939 $";
    private static final int STRING_TYPE = 1;
    private static final int DATE_TYPE = 2;
    private HashMap responseHeaders;
    private DateTimeUtil dateTimeUtil;
    private static final String HEADER = "ADDHEADERS FILTER: ";
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AddHeaders.enabled = enabled;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (AddHeaders.isEnabled()) {
            super.init(filterConfig);
            Enumeration headers = filterConfig.getInitParameterNames();
            while (headers.hasMoreElements()) {
                String headerName = (String)headers.nextElement();
                String headerSource = filterConfig.getInitParameter(headerName);
                Header header = new Header(headerName, headerSource);
                if (this.responseHeaders == null) {
                    this.responseHeaders = new HashMap();
                }
                this.responseHeaders.put(headerName, header);
                this.log(HEADER, "Defining header " + headerName + " = " + header.getHeaderSource());
            }
            if (this.responseHeaders == null) {
                this.log(HEADER, "Warning: No values defined for AddHeaders filter");
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (AddHeaders.isEnabled() && this.responseHeaders != null && servletResponse instanceof HttpServletResponse) {
            for (String headerName : this.responseHeaders.keySet()) {
                Header header = (Header)this.responseHeaders.get(headerName);
                ((HttpServletResponse)servletResponse).setHeader(headerName, header.getHeaderValue());
            }
        }
        try {
            if (StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.addhstsheader"), "N").equals("Y") && servletResponse instanceof HttpServletResponse) {
                ((HttpServletResponse)servletResponse).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
        }
        catch (Exception e) {
            this.log(HEADER, "Warning: Unable to add HSTS Header to response: " + e.getMessage());
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private class Header {
        boolean dynamic = false;
        boolean overrideapplied = false;
        String headerName;
        String headerSource;
        String headerValue;

        Header(String headerName, String headerSource) {
            this.headerName = headerName;
            this.setHeaderSource(headerSource);
        }

        void setHeaderSource(String headerSource) {
            this.headerSource = headerSource;
            if (headerSource.toLowerCase().startsWith("static:")) {
                this.headerValue = headerSource.substring(7);
            } else if (headerSource.toLowerCase().startsWith("dynamic:")) {
                this.dynamic = true;
                AddHeaders.this.dateTimeUtil = new DateTimeUtil();
            } else {
                AddHeaders.this.log(AddHeaders.HEADER, "Warning: Unrecognized type of header in AddHeaders filter");
            }
        }

        String getHeaderValue() {
            if (!this.overrideapplied) {
                try {
                    String headerSource = ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.addheaders." + this.headerName, AddHeaders.this.filterConfig.getInitParameter(this.headerName));
                    if (!headerSource.equals(this.headerSource)) {
                        this.setHeaderSource(headerSource);
                        AddHeaders.this.log(AddHeaders.HEADER, "Overriding header " + this.headerName + " = " + headerSource);
                    }
                }
                catch (ServiceException serviceException) {
                    // empty catch block
                }
                this.overrideapplied = true;
            }
            if (!this.dynamic) {
                return this.headerValue;
            }
            return this.evaluateHeader();
        }

        String getHeaderSource() {
            return this.headerSource;
        }

        String evaluateHeader() {
            String parsedHeaderValue = this.headerSource;
            String[] tokens = StringUtil.getTokens(this.headerSource);
            if (tokens != null && tokens.length > 0) {
                for (int i = 0; i < tokens.length; ++i) {
                    String replacement;
                    String[] parts = StringUtil.split(tokens[i], "=");
                    if (parts != null && parts.length == 2) {
                        if (parts[0].equalsIgnoreCase("date")) {
                            Calendar cal = AddHeaders.this.dateTimeUtil.getCalendar(parts[1]);
                            TimeZone gmtZone = TimeZone.getTimeZone("GMT");
                            cal.setTimeZone(gmtZone);
                            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                            format.setTimeZone(gmtZone);
                            Date cDate = cal.getTime();
                            replacement = format.format(cDate);
                        } else {
                            replacement = tokens[i];
                        }
                    } else {
                        replacement = tokens[i];
                    }
                    parsedHeaderValue = StringUtil.replaceAll(this.headerSource, "[" + tokens[i] + "]", replacement);
                }
                return parsedHeaderValue.substring(8);
            }
            return parsedHeaderValue.substring(8);
        }

        private int checkType(String configVal) {
            int idx;
            int retVal = 0;
            if (configVal.length() < 5 || (idx = configVal.indexOf("&val:")) == -1) {
                return 0;
            }
            String type = configVal.substring(5, idx);
            if (type.equalsIgnoreCase("string")) {
                retVal = 1;
            } else if (type.equalsIgnoreCase("date")) {
                retVal = 2;
            }
            return retVal;
        }

        private String getDateValue(String configVal) {
            String delta = "";
            String retVal = null;
            int start = 0;
            int end = 0;
            int configValLen = configVal.length();
            TimeZone tz = TimeZone.getTimeZone("GMT");
            Calendar cTime = Calendar.getInstance(tz);
            if (configVal.length() < 5 || (start = configVal.indexOf("&val:")) == -1) {
                return retVal;
            }
            if (!(configVal = configVal.substring(start + 5)).equals("+0")) {
                if (configVal.startsWith("-")) {
                    delta = "-";
                    start = 1;
                } else if (configVal.startsWith("+")) {
                    start = 1;
                }
                end = configVal.indexOf("Y");
                if (end != -1) {
                    String year = configVal.substring(start, end);
                    cTime.add(1, this.getInt(delta + year));
                    start = end + 1;
                }
                if (start < configValLen && (end = configVal.indexOf("MO")) != -1) {
                    String month = configVal.substring(start, end);
                    cTime.add(2, this.getInt(delta + month));
                    start = end + 2;
                }
                if (start < configValLen && (end = configVal.indexOf("D")) != -1) {
                    String day = configVal.substring(start, end);
                    cTime.add(5, this.getInt(delta + day));
                    start = end + 1;
                }
                if (start < configValLen && (end = configVal.indexOf("H")) != -1) {
                    String hr = configVal.substring(start, end);
                    cTime.add(10, this.getInt(delta + hr));
                    start = end + 1;
                }
                if (start < configValLen && (end = configVal.indexOf("M")) != -1) {
                    String min = configVal.substring(start, end);
                    cTime.add(12, Integer.parseInt(delta + min));
                    start = end + 1;
                }
                if (start < configValLen && (end = configVal.indexOf("S")) != -1) {
                    String second = configVal.substring(start, end);
                    cTime.add(13, this.getInt(delta + second));
                }
            }
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            TimeZone gmtZone = TimeZone.getTimeZone("GMT");
            format.setTimeZone(gmtZone);
            Date cDate = cTime.getTime();
            return format.format(cDate);
        }

        private int getInt(String str) {
            try {
                return Integer.parseInt(str);
            }
            catch (Exception e) {
                AddHeaders.this.log("Exception: In AddHeadersFilter, check filter init-param");
                return 0;
            }
        }

        private String getStringValue(String configVal) {
            int idx = configVal.indexOf("&val:");
            return configVal.substring(idx + 5);
        }
    }
}

