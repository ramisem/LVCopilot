/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONString;
import org.json.JSONTokener;

public class JSONObject {
    private HashMap myHashMap;
    public static final Object NULL = new Null();

    public JSONObject() {
        this.myHashMap = new HashMap();
    }

    public JSONObject(JSONObject jo, String[] sa) throws JSONException {
        this();
        for (int i = 0; i < sa.length; ++i) {
            this.putOpt(sa[i], jo.opt(sa[i]));
        }
    }

    public JSONObject(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        block8: while (true) {
            char c = x.nextClean();
            switch (c) {
                case '\u0000': {
                    throw x.syntaxError("A JSONObject text must end with '}'");
                }
                case '}': {
                    return;
                }
            }
            x.back();
            String key = x.nextValue().toString();
            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.put(key, x.nextValue());
            switch (x.nextClean()) {
                case ',': 
                case ';': {
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    continue block8;
                }
                case '}': {
                    return;
                }
            }
            break;
        }
        throw x.syntaxError("Expected a ',' or '}'");
    }

    public JSONObject(Map map) {
        this.myHashMap = map == null ? new HashMap() : new HashMap(map);
    }

    public JSONObject(Object object, String[] names) {
        this();
        Class<?> c = object.getClass();
        for (int i = 0; i < names.length; ++i) {
            try {
                String name = names[i];
                Field field = c.getField(name);
                Object value = field.get(object);
                this.put(name, value);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public JSONObject(String string) throws JSONException {
        this(new JSONTokener(string));
    }

    public JSONObject accumulate(String key, Object value) throws JSONException {
        JSONObject.testValidity(value);
        Object o = this.opt(key);
        if (o == null) {
            this.put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
        } else if (o instanceof JSONArray) {
            ((JSONArray)o).put(value);
        } else {
            this.put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }

    public JSONObject append(String key, Object value) throws JSONException {
        JSONObject.testValidity(value);
        Object o = this.opt(key);
        if (o == null) {
            this.put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            this.put(key, ((JSONArray)o).put(value));
        } else {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        }
        return this;
    }

    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }
        String s = Double.toString(d);
        if (s.indexOf(46) > 0 && s.indexOf(101) < 0 && s.indexOf(69) < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    public Object get(String key) throws JSONException {
        Object o = this.opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] not found.");
        }
        return o;
    }

    public boolean getBoolean(String key) throws JSONException {
        Object o = this.get(key);
        if (o.equals(Boolean.FALSE) || o instanceof String && ((String)o).equalsIgnoreCase("false")) {
            return false;
        }
        if (o.equals(Boolean.TRUE) || o instanceof String && ((String)o).equalsIgnoreCase("true")) {
            return true;
        }
        throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] is not a Boolean.");
    }

    public double getDouble(String key) throws JSONException {
        Object o = this.get(key);
        try {
            return o instanceof Number ? ((Number)o).doubleValue() : Double.valueOf((String)o).doubleValue();
        }
        catch (Exception e) {
            throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] is not a number.");
        }
    }

    public int getInt(String key) throws JSONException {
        Object o = this.get(key);
        return o instanceof Number ? ((Number)o).intValue() : (int)this.getDouble(key);
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object o = this.get(key);
        if (o instanceof JSONArray) {
            return (JSONArray)o;
        }
        throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        Object o = this.get(key);
        if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] is not a JSONObject.");
    }

    public long getLong(String key) throws JSONException {
        Object o = this.get(key);
        return o instanceof Number ? ((Number)o).longValue() : (long)this.getDouble(key);
    }

    public String getString(String key) throws JSONException {
        return this.get(key).toString();
    }

    public boolean has(String key) {
        return this.myHashMap.containsKey(key);
    }

    public boolean isNull(String key) {
        return NULL.equals(this.opt(key));
    }

    public Iterator keys() {
        return this.myHashMap.keySet().iterator();
    }

    public int length() {
        return this.myHashMap.size();
    }

    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    public static String numberToString(Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        JSONObject.testValidity(n);
        String s = n.toString();
        if (s.indexOf(46) > 0 && s.indexOf(101) < 0 && s.indexOf(69) < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    public Object opt(String key) {
        return key == null ? null : this.myHashMap.get(key);
    }

    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return this.getBoolean(key);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public JSONObject put(String key, Collection value) throws JSONException {
        this.put(key, new JSONArray(value));
        return this;
    }

    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    public double optDouble(String key, double defaultValue) {
        try {
            Object o = this.opt(key);
            return o instanceof Number ? ((Number)o).doubleValue() : new Double((String)o).doubleValue();
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        try {
            return this.getInt(key);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }

    public JSONObject optJSONObject(String key) {
        Object o = this.opt(key);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

    public long optLong(String key) {
        return this.optLong(key, 0L);
    }

    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public String optString(String key) {
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object o = this.opt(key);
        return o != null ? o.toString() : defaultValue;
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JSONObject put(String key, double value) throws JSONException {
        this.put(key, new Double(value));
        return this;
    }

    public JSONObject put(String key, int value) throws JSONException {
        this.put(key, new Integer(value));
        return this;
    }

    public JSONObject put(String key, long value) throws JSONException {
        this.put(key, new Long(value));
        return this;
    }

    public JSONObject put(String key, Map value) throws JSONException {
        this.put(key, new JSONObject(value));
        return this;
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            JSONObject.testValidity(value);
            this.myHashMap.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }

    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }

    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }
        char c = '\u0000';
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        sb.append('\"');
        block9: for (int i = 0; i < len; ++i) {
            char b = c;
            c = string.charAt(i);
            switch (c) {
                case '\"': 
                case '\\': {
                    sb.append('\\');
                    sb.append(c);
                    continue block9;
                }
                case '/': {
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    continue block9;
                }
                case '\b': {
                    sb.append("\\b");
                    continue block9;
                }
                case '\t': {
                    sb.append("\\t");
                    continue block9;
                }
                case '\n': {
                    sb.append("\\n");
                    continue block9;
                }
                case '\f': {
                    sb.append("\\f");
                    continue block9;
                }
                case '\r': {
                    sb.append("\\r");
                    continue block9;
                }
                default: {
                    if (c < ' ' || c >= '\u0080' && c < '\u00a0' || c >= '\u2000' && c < '\u2100') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                        continue block9;
                    }
                    sb.append(c);
                }
            }
        }
        sb.append('\"');
        return sb.toString();
    }

    public Object remove(String key) {
        return this.myHashMap.remove(key);
    }

    static void testValidity(Object o) throws JSONException {
        if (o != null && (o instanceof Double ? ((Double)o).isInfinite() || ((Double)o).isNaN() : o instanceof Float && (((Float)o).isInfinite() || ((Float)o).isNaN()))) {
            throw new JSONException("JSON does not allow non-finite numbers.");
        }
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); ++i) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    public String toString() {
        try {
            Iterator keys = this.keys();
            StringBuffer sb = new StringBuffer("{");
            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(JSONObject.quote(o.toString()));
                sb.append(':');
                sb.append(JSONObject.valueToString(this.myHashMap.get(o)));
            }
            sb.append('}');
            return sb.toString();
        }
        catch (Exception e) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        return this.toString(indentFactor, 0);
    }

    String toString(int indentFactor, int indent) throws JSONException {
        int n = this.length();
        if (n == 0) {
            return "{}";
        }
        Iterator keys = this.keys();
        StringBuffer sb = new StringBuffer("{");
        int newindent = indent + indentFactor;
        if (n == 1) {
            Object o = keys.next();
            sb.append(JSONObject.quote(o.toString()));
            sb.append(": ");
            sb.append(JSONObject.valueToString(this.myHashMap.get(o), indentFactor, indent));
        } else {
            int i;
            while (keys.hasNext()) {
                Object o = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; ++i) {
                    sb.append(' ');
                }
                sb.append(JSONObject.quote(o.toString()));
                sb.append(": ");
                sb.append(JSONObject.valueToString(this.myHashMap.get(o), indentFactor, newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; ++i) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            String o;
            try {
                o = ((JSONString)value).toJSONString();
            }
            catch (Exception e) {
                throw new JSONException(e);
            }
            if (o instanceof String) {
                return o;
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return JSONObject.numberToString((Number)value);
        }
        if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        return JSONObject.quote(value.toString());
    }

    static String valueToString(Object value, int indentFactor, int indent) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            String o;
            if (value instanceof JSONString && (o = ((JSONString)value).toJSONString()) instanceof String) {
                return o;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (value instanceof Number) {
            return JSONObject.numberToString((Number)value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return ((JSONObject)value).toString(indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return ((JSONArray)value).toString(indentFactor, indent);
        }
        return JSONObject.quote(value.toString());
    }

    public Writer write(Writer writer) throws JSONException {
        try {
            boolean b = false;
            Iterator keys = this.keys();
            writer.write(123);
            while (keys.hasNext()) {
                if (b) {
                    writer.write(44);
                }
                Object k = keys.next();
                writer.write(JSONObject.quote(k.toString()));
                writer.write(58);
                Object v = this.myHashMap.get(k);
                if (v instanceof JSONObject) {
                    ((JSONObject)v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray)v).write(writer);
                } else {
                    writer.write(JSONObject.valueToString(v));
                }
                b = true;
            }
            writer.write(125);
            return writer;
        }
        catch (IOException e) {
            throw new JSONException(e);
        }
    }

    private static final class Null {
        private Null() {
        }

        protected final Object clone() {
            return this;
        }

        public boolean equals(Object object) {
            return object == null || object == this;
        }

        public String toString() {
            return "null";
        }
    }
}

