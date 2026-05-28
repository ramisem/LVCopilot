/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.Trace;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class HTMLInputFilter {
    protected static final boolean ALWAYS_MAKE_TAGS = false;
    protected static final boolean STRIP_COMMENTS = true;
    protected static final int REGEX_FLAGS_SI = 34;
    protected Map<String, List<String>> vAllowed;
    protected Map<String, Integer> vTagCounts;
    protected String[] vSelfClosingTags;
    protected String[] vNeedClosingTags;
    protected String[] vProtocolAtts;
    protected String[] vAllowedProtocols;
    protected String[] vRemoveBlanks;
    protected String[] vAllowedEntities;
    protected boolean vDebug;
    protected boolean vRejectRequest;

    public HTMLInputFilter() {
        this(false, false);
    }

    public HTMLInputFilter(boolean debug, boolean rejectRequest) {
        this.vDebug = debug;
        this.vRejectRequest = rejectRequest;
        this.vAllowed = new HashMap<String, List<String>>();
        this.vTagCounts = new HashMap<String, Integer>();
        ArrayList<String> a_atts = new ArrayList<String>();
        a_atts.add("href");
        a_atts.add("target");
        this.vAllowed.put("a", a_atts);
        ArrayList<String> img_atts = new ArrayList<String>();
        img_atts.add("src");
        img_atts.add("width");
        img_atts.add("height");
        img_atts.add("alt");
        this.vAllowed.put("img", img_atts);
        ArrayList no_atts = new ArrayList();
        this.vAllowed.put("b", no_atts);
        this.vAllowed.put("strong", no_atts);
        this.vAllowed.put("i", no_atts);
        this.vAllowed.put("em", no_atts);
        this.vSelfClosingTags = new String[]{"img"};
        this.vNeedClosingTags = new String[]{"a", "b", "strong", "i", "em"};
        this.vAllowedProtocols = new String[]{"http", "mailto"};
        this.vProtocolAtts = new String[]{"src", "href"};
        this.vRemoveBlanks = new String[]{"a", "b", "strong", "i", "em"};
        this.vAllowedEntities = new String[]{"amp", "gt", "lt", "quot"};
    }

    protected void reset() {
        this.vTagCounts = new HashMap<String, Integer>();
    }

    protected void debug(String msg) {
        if (this.vDebug) {
            Trace.log(msg);
        }
    }

    public static String chr(int decimal) {
        return String.valueOf((char)decimal);
    }

    public static String htmlSpecialChars(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public synchronized String filter(String input, String parameter, HttpServletRequest req) {
        this.reset();
        String s = input;
        s = this.balanceHTML(s);
        if (!s.equals(input)) {
            HTMLInputFilter.logFailedParam(parameter, req, "Balance HTML", input);
        }
        if (!(s = this.checkTags(s)).equals(input)) {
            HTMLInputFilter.logFailedParam(parameter, req, "Check Tags", input);
        }
        if (!(s = this.processRemoveBlanks(s)).equals(input)) {
            HTMLInputFilter.logFailedParam(parameter, req, "Remove Blanks", input);
        }
        if (s.equals(input)) return input;
        this.debug("**********Rejected Parameter:" + parameter + " = " + input + ". Value After Filter:" + s);
        try {
            if (!this.vRejectRequest) return input;
        }
        catch (Exception e) {
            Trace.log("failed to write to filter log" + e.getMessage());
        }
        return input;
    }

    protected String escapeComments(String s) {
        Pattern p = Pattern.compile("<!--(.*?)-->", 32);
        Matcher m = p.matcher(s);
        StringBuffer buf = new StringBuffer();
        if (m.find()) {
            String match = m.group(1);
            m.appendReplacement(buf, "<!--" + HTMLInputFilter.htmlSpecialChars(match) + "-->");
        }
        m.appendTail(buf);
        return buf.toString();
    }

    protected String balanceHTML(String s) {
        s = this.regexReplace("<([^>]*?)(?=<|$)", "&lt;$1", s);
        s = this.regexReplace("(^|>)([^<]*?)(?=>)", "$1$2&gt;<", s);
        s = s.replaceAll("<>", "");
        return s;
    }

    protected String checkTags(String s) {
        Pattern p = Pattern.compile("<(.*?)>", 32);
        Matcher m = p.matcher(s);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            String replaceStr = m.group(1);
            replaceStr = this.processTag(replaceStr);
            m.appendReplacement(buf, replaceStr);
        }
        m.appendTail(buf);
        s = buf.toString();
        for (String key : this.vTagCounts.keySet()) {
            for (int ii = 0; ii < this.vTagCounts.get(key); ++ii) {
                s = s + "</" + key + ">";
            }
        }
        return s;
    }

    protected String processRemoveBlanks(String s) {
        for (String tag : this.vRemoveBlanks) {
            s = this.regexReplace("<" + tag + "(\\s[^>]*)?></" + tag + ">", "", s);
            s = this.regexReplace("<" + tag + "(\\s[^>]*)?/>", "", s);
        }
        return s;
    }

    protected String regexReplace(String regex_pattern, String replacement, String s) {
        Pattern p = Pattern.compile(regex_pattern);
        Matcher m = p.matcher(s);
        return m.replaceAll(replacement);
    }

    protected String processTag(String s) {
        String name;
        if (!this.vAllowed.containsKey(s.toLowerCase()) && s.toLowerCase().indexOf("script") < 0) {
            return "<" + s + ">";
        }
        Pattern p = Pattern.compile("^/([a-z0-9]+)", 34);
        Matcher m = p.matcher(s);
        if (m.find() && this.vAllowed.containsKey(name = m.group(1).toLowerCase()) && !this.inArray(name, this.vSelfClosingTags) && this.vTagCounts.containsKey(name)) {
            this.vTagCounts.put(name, this.vTagCounts.get(name) - 1);
            return "</" + name + ">";
        }
        p = Pattern.compile("^([a-z0-9]+)(.*?)(/?)$", 34);
        m = p.matcher(s);
        if (m.find()) {
            name = m.group(1).toLowerCase();
            String body = m.group(2);
            String ending = m.group(3);
            if (this.vAllowed.containsKey(name)) {
                String params = "";
                Pattern p2 = Pattern.compile("([a-z0-9]+)=([\"'])(.*?)\\2", 34);
                Pattern p3 = Pattern.compile("([a-z0-9]+)(=)([^\"\\s']+)", 34);
                Matcher m2 = p2.matcher(body);
                Matcher m3 = p3.matcher(body);
                ArrayList<String> paramNames = new ArrayList<String>();
                ArrayList<String> paramValues = new ArrayList<String>();
                while (m2.find()) {
                    paramNames.add(m2.group(1));
                    paramValues.add(m2.group(3));
                }
                while (m3.find()) {
                    paramNames.add(m3.group(1));
                    paramValues.add(m3.group(3));
                }
                for (int ii = 0; ii < paramNames.size(); ++ii) {
                    String paramName = ((String)paramNames.get(ii)).toLowerCase();
                    String paramValue = (String)paramValues.get(ii);
                    if (!this.vAllowed.get(name).contains(paramName)) continue;
                    if (this.inArray(paramName, this.vProtocolAtts)) {
                        paramValue = this.processParamProtocol(paramValue);
                    }
                    params = params + " " + paramName + "=\"" + paramValue + "\"";
                }
                if (this.inArray(name, this.vSelfClosingTags)) {
                    ending = " /";
                }
                if (this.inArray(name, this.vNeedClosingTags)) {
                    ending = "";
                }
                if (ending == null || ending.length() < 1) {
                    if (this.vTagCounts.containsKey(name)) {
                        this.vTagCounts.put(name, this.vTagCounts.get(name) + 1);
                    } else {
                        this.vTagCounts.put(name, 1);
                    }
                } else {
                    ending = " /";
                }
                return "<" + name + params + ending + ">";
            }
            return "";
        }
        p = Pattern.compile("^!--(.*)--$", 34);
        m = p.matcher(s);
        if (m.find()) {
            String comment = m.group();
            return "";
        }
        return "";
    }

    protected String processParamProtocol(String s) {
        String protocol;
        s = this.decodeEntities(s);
        Pattern p = Pattern.compile("^([^:]+):", 34);
        Matcher m = p.matcher(s);
        if (m.find() && !this.inArray(protocol = m.group(1), this.vAllowedProtocols) && (s = "#" + s.substring(protocol.length() + 1, s.length())).startsWith("#//")) {
            s = "#" + s.substring(3, s.length());
        }
        return s;
    }

    protected String decodeEntities(String s) {
        int decimal;
        String match;
        StringBuffer buf = new StringBuffer();
        Pattern p = Pattern.compile("&#(\\d+);?");
        Matcher m = p.matcher(s);
        while (m.find()) {
            match = m.group(1);
            decimal = Integer.decode(match);
            m.appendReplacement(buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();
        buf = new StringBuffer();
        p = Pattern.compile("&#x([0-9a-f]+);?");
        m = p.matcher(s);
        while (m.find()) {
            match = m.group(1);
            decimal = Integer.decode(match);
            m.appendReplacement(buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();
        buf = new StringBuffer();
        p = Pattern.compile("%([0-9a-f]{2});?");
        m = p.matcher(s);
        while (m.find()) {
            match = m.group(1);
            decimal = Integer.decode(match);
            m.appendReplacement(buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();
        s = this.validateEntities(s);
        return s;
    }

    protected String validateEntities(String s) {
        Pattern p = Pattern.compile("&([^&;]*)(?=(;|&|$))");
        Matcher m = p.matcher(s);
        if (m.find()) {
            String one = m.group(1);
            String two = m.group(2);
            s = this.checkEntity(one, two);
        }
        p = Pattern.compile("(>|^)([^<]+?)(<|$)", 32);
        m = p.matcher(s);
        StringBuffer buf = new StringBuffer();
        if (m.find()) {
            String one = m.group(1);
            String two = m.group(2);
            String three = m.group(3);
            m.appendReplacement(buf, one + two.replaceAll("\"", "&quot;") + three);
        }
        m.appendTail(buf);
        return s;
    }

    public static void logFailedParam(String parameter, HttpServletRequest req, String reason, String input) {
        try {
            FileOutputStream log = new FileOutputStream("c:\\filterlog\\filterout.log", true);
            String command = HTMLInputFilter.getCommand(req);
            log.write(("\n" + parameter + "\t" + command + "\t" + HTMLInputFilter.getContext(command, req) + "\t" + reason + "\t" + input + "\n").getBytes());
            log.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String getCommand(HttpServletRequest req) {
        Enumeration params = req.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement().toString();
            if (!param.equals("command")) continue;
            return req.getParameter(param);
        }
        return "";
    }

    private static String getContext(String command, HttpServletRequest req) {
        Enumeration params = req.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement().toString();
            if (command.equals("page") && param.equals("page")) {
                return req.getParameter(param);
            }
            if (command.equals("file") && param.equals("file")) {
                return req.getParameter(param);
            }
            if (command.equals("ajax") && param.equals("ajaxclass")) {
                return req.getParameter(param);
            }
            if (command.equals("operation") && param.equals("operationclass")) {
                return req.getParameter(param);
            }
            if (command.equals("action") && param.equals("actionclass")) {
                return req.getParameter(param);
            }
            if (!command.equals("wizard") || !param.equals("wizard")) continue;
            return req.getParameter(param);
        }
        return "";
    }

    protected String checkEntity(String preamble, String term) {
        if (!term.equals(";")) {
            return "&amp;" + preamble;
        }
        if (this.isValidEntity(preamble)) {
            return "&" + preamble;
        }
        return "&amp;" + preamble;
    }

    protected boolean isValidEntity(String entity) {
        return this.inArray(entity, this.vAllowedEntities);
    }

    private boolean inArray(String s, String[] array) {
        for (String item : array) {
            if (item == null || !item.equals(s)) continue;
            return true;
        }
        return false;
    }
}

