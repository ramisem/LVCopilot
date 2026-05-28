/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.util.Iterator;
import org.json.HTTPTokener;
import org.json.JSONException;
import org.json.JSONObject;

public class HTTP {
    public static final String CRLF = "\r\n";

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject o = new JSONObject();
        HTTPTokener x = new HTTPTokener(string);
        String t = x.nextToken();
        if (t.toUpperCase().startsWith("HTTP")) {
            o.put("HTTP-Version", t);
            o.put("Status-Code", x.nextToken());
            o.put("Reason-Phrase", x.nextTo('\u0000'));
            x.next();
        } else {
            o.put("Method", t);
            o.put("Request-URI", x.nextToken());
            o.put("HTTP-Version", x.nextToken());
        }
        while (x.more()) {
            String name = x.nextTo(':');
            x.next(':');
            o.put(name, x.nextTo('\u0000'));
            x.next();
        }
        return o;
    }

    public static String toString(JSONObject o) throws JSONException {
        Iterator keys = o.keys();
        StringBuffer sb = new StringBuffer();
        if (o.has("Status-Code") && o.has("Reason-Phrase")) {
            sb.append(o.getString("HTTP-Version"));
            sb.append(' ');
            sb.append(o.getString("Status-Code"));
            sb.append(' ');
            sb.append(o.getString("Reason-Phrase"));
        } else if (o.has("Method") && o.has("Request-URI")) {
            sb.append(o.getString("Method"));
            sb.append(' ');
            sb.append('\"');
            sb.append(o.getString("Request-URI"));
            sb.append('\"');
            sb.append(' ');
            sb.append(o.getString("HTTP-Version"));
        } else {
            throw new JSONException("Not enough material for an HTTP header.");
        }
        sb.append(CRLF);
        while (keys.hasNext()) {
            String s = keys.next().toString();
            if (s.equals("HTTP-Version") || s.equals("Status-Code") || s.equals("Reason-Phrase") || s.equals("Method") || s.equals("Request-URI") || o.isNull(s)) continue;
            sb.append(s);
            sb.append(": ");
            sb.append(o.getString(s));
            sb.append(CRLF);
        }
        sb.append(CRLF);
        return sb.toString();
    }
}

