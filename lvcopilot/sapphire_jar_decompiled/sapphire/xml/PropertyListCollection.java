/*
 * Decompiled with CFR 0.152.
 */
package sapphire.xml;

import com.labvantage.sapphire.gwt.shared.JSONable;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.xml.PropertyList;

public class PropertyListCollection
extends ArrayList
implements JSONable {
    private String id;
    private String propertyTreeNodeId;
    private HashMap attributes;
    private HashMap<String, Integer> index = new HashMap();
    private String fieldName;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setPropertyTreeNodeId(String propertyTreeNodeId) {
        this.propertyTreeNodeId = propertyTreeNodeId;
    }

    public String getPropertyTreeNodeId() {
        return this.propertyTreeNodeId;
    }

    public PropertyList getPropertyList(int index) {
        return (PropertyList)this.get(index);
    }

    public PropertyList getPropertyList(String id) {
        for (int i = 0; i < this.size(); ++i) {
            if (!((PropertyList)this.get(i)).getId().equals(id)) continue;
            return (PropertyList)this.get(i);
        }
        return null;
    }

    public void setAttributes(HashMap attributes) {
        this.attributes = attributes;
    }

    public HashMap getAttributes() {
        return this.attributes;
    }

    public String getAttribute(String attributeId) {
        if (this.attributes == null) {
            return "";
        }
        Object o = this.attributes.get(attributeId);
        return o != null && o instanceof String ? (String)o : "";
    }

    public String getUniqueId() {
        String propertylistid;
        long idseed = System.currentTimeMillis();
        while (this.getPropertyList(propertylistid = Long.toString(idseed++)) != null) {
        }
        return propertylistid;
    }

    public PropertyList find(String propertyid, String value) {
        return this.find(propertyid, value, false);
    }

    public PropertyList find(String propertyid, String value, boolean caseInsensitive) {
        for (Object o : this) {
            PropertyList pl = (PropertyList)o;
            if (pl == null || !(caseInsensitive ? pl.getProperty(propertyid).equalsIgnoreCase(value) : pl.getProperty(propertyid).equals(value))) continue;
            return pl;
        }
        return null;
    }

    public PropertyList getIndexedPropertyList(String value) {
        return this.index.containsKey(value) ? this.getPropertyList(this.index.get(value)) : null;
    }

    public int getIndex(String value) {
        return this.index.containsKey(value) ? this.index.get(value) : -1;
    }

    public void index(String propertyid) {
        for (int i = 0; i < this.size(); ++i) {
            this.index.put(this.getPropertyList(i).getProperty(propertyid), i);
        }
    }

    public void index(String[] propertyid, String separator) {
        for (int i = 0; i < this.size(); ++i) {
            PropertyList propertyList = this.getPropertyList(i);
            StringBuffer indexkey = new StringBuffer();
            for (int j = 0; j < propertyid.length; ++j) {
                indexkey.append(separator).append(propertyList.getProperty(propertyid[j]));
            }
            this.index.put(indexkey.substring(separator.length()), i);
        }
    }

    public JSONArray toJSONArray() {
        return this.toJSONArray(true, true);
    }

    public JSONArray toJSONArray(boolean includeAttributes, boolean includeEmpties) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < this.size(); ++i) {
                JSONObject jsonObject = new JSONObject();
                PropertyList.addPropertyListToJSonObject(jsonObject, this.getPropertyList(i), includeAttributes, includeEmpties);
                jsonArray.put(i, jsonObject);
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        return jsonArray;
    }

    @Override
    public String toJSONString() {
        return this.toJSONArray().toString();
    }

    public String toJSONString(boolean includeAttributes, boolean includeEmpties) {
        return this.toJSONArray(includeAttributes, includeEmpties).toString();
    }

    public void setJSONString(String jsonString) throws JSONException {
        JSONArray jarr = new JSONArray(jsonString);
        for (int i = 0; i < jarr.length(); ++i) {
            this.add(new PropertyList(jarr.getJSONObject(i)));
        }
    }

    public void setJSONArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); ++i) {
                this.add(new PropertyList(jsonArray.getJSONObject(i)));
            }
        }
    }
}

