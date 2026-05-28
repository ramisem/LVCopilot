/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XMLTokener;

public class XML {
    public static final Character AMP = new Character('&');
    public static final Character APOS = new Character('\'');
    public static final Character BANG = new Character('!');
    public static final Character EQ = new Character('=');
    public static final Character GT = new Character('>');
    public static final Character LT = new Character('<');
    public static final Character QUEST = new Character('?');
    public static final Character QUOT = new Character('\"');
    public static final Character SLASH = new Character('/');

    public static String escape(String string) {
        StringBuffer sb = new StringBuffer();
        int len = string.length();
        block6: for (int i = 0; i < len; ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '&': {
                    sb.append("&amp;");
                    continue block6;
                }
                case '<': {
                    sb.append("&lt;");
                    continue block6;
                }
                case '>': {
                    sb.append("&gt;");
                    continue block6;
                }
                case '\"': {
                    sb.append("&quot;");
                    continue block6;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static boolean parse(XMLTokener x, JSONObject context, String name) throws JSONException {
        String s;
        JSONObject o = null;
        Object t = x.nextToken();
        if (t == BANG) {
            char c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                t = x.nextToken();
                if (t.equals("CDATA") && x.next() == '[') {
                    String s2 = x.nextCDATA();
                    if (s2.length() > 0) {
                        context.accumulate("content", s2);
                    }
                    return false;
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            int i = 1;
            do {
                if ((t = x.nextMeta()) == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                }
                if (t == LT) {
                    ++i;
                    continue;
                }
                if (t != GT) continue;
                --i;
            } while (i > 0);
            return false;
        }
        if (t == QUEST) {
            x.skipPast("?>");
            return false;
        }
        if (t == SLASH) {
            t = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag" + t);
            }
            if (!t.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + t);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;
        }
        if (t instanceof Character) {
            throw x.syntaxError("Misshaped tag");
        }
        String n = (String)t;
        t = null;
        o = new JSONObject();
        while (true) {
            if (t == null) {
                t = x.nextToken();
            }
            if (!(t instanceof String)) break;
            s = (String)t;
            t = x.nextToken();
            if (t == EQ) {
                t = x.nextToken();
                if (!(t instanceof String)) {
                    throw x.syntaxError("Missing value");
                }
                o.accumulate(s, t);
                t = null;
                continue;
            }
            o.accumulate(s, "");
        }
        if (t == SLASH) {
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped tag");
            }
            context.accumulate(n, o);
            return false;
        }
        if (t == GT) {
            while (true) {
                if ((t = x.nextContent()) == null) {
                    if (n != null) {
                        throw x.syntaxError("Unclosed tag " + n);
                    }
                    return false;
                }
                if (t instanceof String) {
                    s = (String)t;
                    if (s.length() <= 0) continue;
                    o.accumulate("content", s);
                    continue;
                }
                if (t == LT && XML.parse(x, o, n)) break;
            }
            if (o.length() == 0) {
                context.accumulate(n, "");
            } else if (o.length() == 1 && o.opt("content") != null) {
                context.accumulate(n, o.opt("content"));
            } else {
                context.accumulate(n, o);
            }
            return false;
        }
        throw x.syntaxError("Misshaped tag");
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject o = new JSONObject();
        XMLTokener x = new XMLTokener(string);
        while (x.more() && x.skipPast("<")) {
            XML.parse(x, o, null);
        }
        return o;
    }

    public static String toString(Object o) throws JSONException {
        return XML.toString(o, null);
    }

    public static String toString(Object o, String tagName) throws JSONException {
        String s;
        StringBuffer b = new StringBuffer();
        if (o instanceof JSONObject) {
            if (tagName != null) {
                b.append('<');
                b.append(tagName);
                b.append('>');
            }
            JSONObject jo = (JSONObject)o;
            Iterator keys = jo.keys();
            while (keys.hasNext()) {
                int i;
                int len;
                JSONArray ja;
                String k = keys.next().toString();
                Object v = jo.get(k);
                String s2 = v instanceof String ? (String)v : null;
                if (k.equals("content")) {
                    if (v instanceof JSONArray) {
                        ja = (JSONArray)v;
                        len = ja.length();
                        for (i = 0; i < len; ++i) {
                            if (i > 0) {
                                b.append('\n');
                            }
                            b.append(XML.escape(ja.get(i).toString()));
                        }
                        continue;
                    }
                    b.append(XML.escape(v.toString()));
                    continue;
                }
                if (v instanceof JSONArray) {
                    ja = (JSONArray)v;
                    len = ja.length();
                    for (i = 0; i < len; ++i) {
                        b.append(XML.toString(ja.get(i), k));
                    }
                    continue;
                }
                if (v.equals("")) {
                    b.append('<');
                    b.append(k);
                    b.append("/>");
                    continue;
                }
                b.append(XML.toString(v, k));
            }
            if (tagName != null) {
                b.append("</");
                b.append(tagName);
                b.append('>');
            }
            return b.toString();
        }
        if (o instanceof JSONArray) {
            JSONArray ja = (JSONArray)o;
            int len = ja.length();
            for (int i = 0; i < len; ++i) {
                b.append(XML.toString(ja.opt(i), tagName == null ? "array" : tagName));
            }
            return b.toString();
        }
        String string = s = o == null ? "null" : XML.escape(o.toString());
        return tagName == null ? "\"" + s + "\"" : (s.length() == 0 ? "<" + tagName + "/>" : "<" + tagName + ">" + s + "</" + tagName + ">");
    }
}

