/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CDL {
    private static String getValue(JSONTokener x) throws JSONException {
        char c;
        while ((c = x.next()) <= ' ' && c != '\u0000') {
        }
        switch (c) {
            case '\u0000': {
                return null;
            }
            case '\"': 
            case '\'': {
                return x.nextString(c);
            }
            case ',': {
                x.back();
                return "";
            }
        }
        x.back();
        return x.nextTo(',');
    }

    public static JSONArray rowToJSONArray(JSONTokener x) throws JSONException {
        char c;
        JSONArray ja = new JSONArray();
        block0: while (true) {
            String value;
            if ((value = CDL.getValue(x)) == null) {
                return null;
            }
            ja.put(value);
            do {
                if ((c = x.next()) == ',') continue block0;
            } while (c == ' ');
            break;
        }
        if (c == '\n' || c == '\r' || c == '\u0000') {
            return ja;
        }
        throw x.syntaxError("Bad character '" + c + "' (" + c + ").");
    }

    public static JSONObject rowToJSONObject(JSONArray names, JSONTokener x) throws JSONException {
        JSONArray ja = CDL.rowToJSONArray(x);
        return ja != null ? ja.toJSONObject(names) : null;
    }

    public static JSONArray toJSONArray(String string) throws JSONException {
        return CDL.toJSONArray(new JSONTokener(string));
    }

    public static JSONArray toJSONArray(JSONTokener x) throws JSONException {
        return CDL.toJSONArray(CDL.rowToJSONArray(x), x);
    }

    public static JSONArray toJSONArray(JSONArray names, String string) throws JSONException {
        return CDL.toJSONArray(names, new JSONTokener(string));
    }

    public static JSONArray toJSONArray(JSONArray names, JSONTokener x) throws JSONException {
        JSONObject jo;
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        while ((jo = CDL.rowToJSONObject(names, x)) != null) {
            ja.put(jo);
        }
        if (ja.length() == 0) {
            return null;
        }
        return ja;
    }

    public static String rowToString(JSONArray ja) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ja.length(); ++i) {
            Object o;
            if (i > 0) {
                sb.append(',');
            }
            if ((o = ja.opt(i)) == null) continue;
            String s = o.toString();
            if (s.indexOf(44) >= 0) {
                if (s.indexOf(34) >= 0) {
                    sb.append('\'');
                    sb.append(s);
                    sb.append('\'');
                    continue;
                }
                sb.append('\"');
                sb.append(s);
                sb.append('\"');
                continue;
            }
            sb.append(s);
        }
        sb.append('\n');
        return sb.toString();
    }

    public static String toString(JSONArray ja) throws JSONException {
        JSONArray names;
        JSONObject jo = ja.optJSONObject(0);
        if (jo != null && (names = jo.names()) != null) {
            return CDL.rowToString(names) + CDL.toString(names, ja);
        }
        return null;
    }

    public static String toString(JSONArray names, JSONArray ja) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ja.length(); ++i) {
            JSONObject jo = ja.optJSONObject(i);
            if (jo == null) continue;
            sb.append(CDL.rowToString(jo.toJSONArray(names)));
        }
        return sb.toString();
    }
}

