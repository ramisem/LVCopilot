/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.JsonArray;

public class JsonObject
implements Serializable {
    private HashMap<String, Serializable> values = new HashMap();

    public JsonObject() {
    }

    public boolean isEmpty() {
        return this.keys().isEmpty();
    }

    public JsonObject(String json) throws SapphireException {
        try {
            this.populateMap(new JSONObject(json));
        }
        catch (JSONException e) {
            throw new SapphireException("SimpleJSON cannot parse input");
        }
    }

    public String toString() {
        JSONObject out = this.toJSONObject();
        return out.toString();
    }

    protected JSONObject toJSONObject() {
        JSONObject out = new JSONObject();
        for (String key : this.values.keySet()) {
            try {
                Serializable o = this.values.get(key);
                if (o instanceof JsonObject) {
                    out.put(key, ((JsonObject)o).toJSONObject());
                    continue;
                }
                if (o instanceof JsonArray) {
                    out.put(key, ((JsonArray)o).getJSONArray());
                    continue;
                }
                out.put(key, o);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return out;
    }

    public JsonObject(JSONObject jsonObject) throws SapphireException {
        this.populateMap(jsonObject);
    }

    private void populateMap(JSONObject jsonObject) throws SapphireException {
        Iterator keys = jsonObject.keys();
        try {
            while (keys.hasNext()) {
                String key = (String)keys.next();
                Object o = jsonObject.get(key);
                if (o instanceof JSONObject) {
                    this.values.put(key, new JsonObject((JSONObject)o));
                    continue;
                }
                if (o.getClass().getName().contains("JSONObject$Null")) {
                    this.values.put(key, new JsonObject());
                    continue;
                }
                if (o instanceof JSONArray) {
                    this.values.put(key, new JsonArray((JSONArray)o));
                    continue;
                }
                if (o instanceof String) {
                    this.values.put(key, (Serializable)((Object)((String)o)));
                    continue;
                }
                if (o instanceof Integer) {
                    this.values.put(key, (Integer)o);
                    continue;
                }
                if (o instanceof Long) {
                    this.values.put(key, (Long)o);
                    continue;
                }
                if (o instanceof Double) {
                    this.values.put(key, (Double)o);
                    continue;
                }
                if (o instanceof Boolean) {
                    this.values.put(key, (Boolean)o);
                    continue;
                }
                throw new SapphireException("SimpleJSON cannot handle value " + key + " with type " + o.getClass());
            }
        }
        catch (Exception e) {
            throw new SapphireException("SimpleJSON cannot handle this JSON: " + e.getMessage());
        }
    }

    public void remove(String id) {
        if (this.values.containsKey(id)) {
            this.values.remove(id);
        }
    }

    public void put(String id, JsonObject jso) {
        this.values.put(id, jso);
    }

    public JsonObject put(String id, JsonArray array) {
        this.values.put(id, array);
        return this;
    }

    public JsonObject put(String id, String value) {
        this.values.put(id, (Serializable)((Object)value));
        return this;
    }

    public JsonObject put(String id, double value) {
        this.values.put(id, Double.valueOf(value));
        return this;
    }

    public JsonObject put(String id, int value) {
        this.values.put(id, Integer.valueOf(value));
        return this;
    }

    public JsonObject put(String id, boolean value) {
        this.values.put(id, Boolean.valueOf(value));
        return this;
    }

    public JsonObject putAnyType(String id, Serializable value) {
        this.values.put(id, value);
        return this;
    }

    public JsonObject putRes(String id, JsonObject value) {
        this.values.put(id, this.doPutRes(value));
        return this;
    }

    public JsonObject putRes(String id, JsonArray value) {
        this.values.put(id, this.doPutRes(value));
        return this;
    }

    public JsonObject putRes(String id, String value) {
        this.values.put(id, this.doPutRes((Serializable)((Object)value)));
        return this;
    }

    public JsonObject putRes(String id, double value) {
        this.values.put(id, this.doPutRes(Double.valueOf(value)));
        return this;
    }

    public JsonObject putRes(String id, int value) {
        this.values.put(id, this.doPutRes(Integer.valueOf(value)));
        return this;
    }

    public JsonObject putRes(String id, boolean value) {
        this.values.put(id, this.doPutRes(Boolean.valueOf(value)));
        return this;
    }

    private JsonObject doPutRes(Serializable value) {
        JsonObject res = new JsonObject();
        res.putAnyType("xs", value);
        res.putAnyType("sm", value);
        res.putAnyType("md", value);
        res.putAnyType("lg", value);
        res.putAnyType("xl", value);
        return res;
    }

    public String getString(String id) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public String getString(String id, String defaultValue) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return defaultValue;
        }
        if (value.toString().length() == 0) {
            return defaultValue;
        }
        return value.toString();
    }

    public boolean getBoolean(String id) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        return false;
    }

    public boolean getBoolean(String id, boolean defaultValue) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        return defaultValue;
    }

    public int getInt(String id, int defaultValue) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof Double) {
            return ((Double)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)((Object)value));
            }
            catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public long getLong(String id, long defaultValue) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof Long) {
            return (Long)value;
        }
        if (value instanceof Double) {
            return ((Double)value).intValue();
        }
        return defaultValue;
    }

    public double getDouble(String id, double defaultValue) {
        Serializable value = this.values.get(id);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof Double) {
            return (Double)value;
        }
        return defaultValue;
    }

    public JsonObject getJsonObject(String id) {
        Serializable value = this.values.get(id);
        if (value == null) {
            JsonObject newObject = new JsonObject();
            this.put(id, newObject);
            return newObject;
        }
        if (value instanceof JsonObject) {
            return (JsonObject)value;
        }
        return null;
    }

    public JsonArray getJsonArray(String id) {
        Serializable value = this.values.get(id);
        if (value == null) {
            JsonArray newArray = new JsonArray();
            this.put(id, newArray);
            return newArray;
        }
        if (value instanceof JsonArray) {
            return (JsonArray)value;
        }
        return null;
    }

    public Set<String> keys() {
        return this.values.keySet();
    }

    public int length() {
        return this.values.size();
    }

    public boolean has(String key) {
        return this.values.containsKey(key);
    }

    public Serializable get(String key) {
        return this.values.get(key);
    }

    public JsonArray toJsonArray(String keyid, String valueid) {
        JsonArray array = new JsonArray();
        for (String key : this.values.keySet()) {
            JsonObject jso = new JsonObject();
            jso.put(keyid, key);
            jso.putAnyType(valueid, this.values.get(key));
            array.put(jso);
        }
        return array;
    }
}

