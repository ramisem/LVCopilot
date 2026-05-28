/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.JsonObject;

public class JsonArray
implements Iterable,
Serializable {
    private ArrayList<Serializable> array = new ArrayList();

    public JsonArray() {
    }

    public void sort(String propertyid) {
        Collections.sort(this.array, (o1, o2) -> {
            if (o1 instanceof JsonObject && o2 instanceof JsonObject) {
                Serializable p1 = ((JsonObject)o1).get(propertyid);
                Serializable p2 = ((JsonObject)o2).get(propertyid);
                if (p1 != null && p2 != null) {
                    return ((Comparable)((Object)p1)).compareTo(p2);
                }
                return p1 == null ? 1 : (p2 == null ? -1 : 0);
            }
            return 0;
        });
    }

    public JsonArray(String json) throws SapphireException {
        try {
            JSONArray array = new JSONArray(json);
            this.setJSONArray(array);
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
    }

    public JsonArray(JSONArray jsonArray) throws SapphireException {
        this.setJSONArray(jsonArray);
    }

    private void setJSONArray(JSONArray jsonArray) throws SapphireException {
        for (int i = 0; i < jsonArray.length(); ++i) {
            try {
                Object o = jsonArray.get(i);
                if (o instanceof JSONObject) {
                    JSONObject jso = (JSONObject)jsonArray.get(i);
                    this.array.add(new JsonObject(jso));
                    continue;
                }
                if (!(o instanceof Serializable)) continue;
                this.array.add((Serializable)o);
                continue;
            }
            catch (JSONException e) {
                throw new SapphireException(e);
            }
        }
    }

    public JsonObject findJsonObject(String key, Object value) {
        for (Serializable o : this.array) {
            Serializable foundValue;
            JsonObject jsonObject;
            if (!(o instanceof JsonObject) || (jsonObject = (JsonObject)o) == null || (foundValue = jsonObject.get(key)) == null || !foundValue.equals(value)) continue;
            return jsonObject;
        }
        return null;
    }

    public Iterator iterator() {
        return this.array.iterator();
    }

    public boolean contains(Object c) {
        return this.array.contains(c);
    }

    public <T> T[] toArray(T[] a) {
        return this.array.toArray(a);
    }

    public ArrayList<Serializable> toArray() {
        return this.array;
    }

    public String[] toStringArray() {
        return this.array.toArray(new String[0]);
    }

    public JsonObject[] toJsonObjectArray() {
        return this.array.toArray(new JsonObject[0]);
    }

    public JsonArray(List<String> values) {
        this.array.addAll(values);
    }

    public int size() {
        return this.array.size();
    }

    public int length() {
        return this.array.size();
    }

    public JsonObject getJsonObject(int index) {
        if (this.array.get(index) instanceof JsonObject) {
            return (JsonObject)this.array.get(index);
        }
        return null;
    }

    public String getString(int index) {
        Serializable o = this.array.get(index);
        return o == null ? "" : o.toString();
    }

    public String toString() {
        return this.getJSONArray().toString();
    }

    public void put(JsonObject jso) {
        this.array.add(jso);
    }

    public void put(String value) {
        this.array.add((Serializable)((Object)value));
    }

    public JSONArray getJSONArray() {
        JSONArray out = new JSONArray();
        for (Serializable jso : this.array) {
            if (jso instanceof JsonObject) {
                out.put(((JsonObject)jso).toJSONObject());
                continue;
            }
            out.put(jso);
        }
        return out;
    }
}

