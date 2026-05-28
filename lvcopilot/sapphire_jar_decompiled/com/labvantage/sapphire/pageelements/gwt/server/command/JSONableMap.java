/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JSONableMap
extends HashMap<String, JSONable>
implements JSONable {
    public JSONableMap() {
    }

    @Override
    public JSONable put(String key, String value) {
        return super.put(key, new JSONableString(value));
    }

    public JSONableMap(Map map) {
        for (Object key : map.keySet()) {
            if (!(key instanceof String) || map.get(key) == null) continue;
            if (map.get(key) instanceof JSONable) {
                this.put((String)key, (JSONable)map.get(key));
                continue;
            }
            if (map.get(key) instanceof String) {
                this.put((String)key, new JSONableString((String)map.get(key)));
                continue;
            }
            this.put((String)key, new JSONableString(map.get(key).toString()));
        }
    }

    public JSONable get(String name) {
        String type;
        if (name.endsWith("_type")) {
            return (JSONable)super.get(name);
        }
        String string = type = super.containsKey(name + "_type") ? ((JSONable)super.get(name + "_type")).toJSONString() : "";
        if (type.equals(SDIRequest.class.getSimpleName())) {
            return this.getSDIRequest(name);
        }
        if (type.equals(PropertyList.class.getSimpleName())) {
            return this.getPropertyList(name);
        }
        if (type.equals(PropertyListCollection.class.getSimpleName())) {
            return this.getCollection(name);
        }
        if (type.equals(Form.class.getSimpleName())) {
            return this.getForm(name);
        }
        if (type.equals(SDIMaint.class.getSimpleName())) {
            return this.getSDIMaint(name);
        }
        if (type.equals(SDIList.class.getSimpleName())) {
            return this.getSDIList(name);
        }
        if (type.equals(DataSet.class.getSimpleName())) {
            return this.getDataSet(name);
        }
        if (type.equals(JSONableMap.class.getSimpleName())) {
            return this.getJSONableMap(name);
        }
        return (JSONable)super.get(name);
    }

    public String getJSONString(String name) {
        return super.containsKey(name) ? ((JSONable)super.get(name)).toJSONString() : "";
    }

    public String getString(String name) {
        return this.getString(name, "");
    }

    public String getString(String name, String defaultValue) {
        String value = super.containsKey(name) ? ((JSONable)super.get(name)).toJSONString() : "";
        return value != null && value.length() > 0 ? value : defaultValue;
    }

    public PropertyListCollection getCollection(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof PropertyListCollection) {
                return (PropertyListCollection)super.get(name);
            }
            PropertyListCollection collection = new PropertyListCollection();
            try {
                if (super.containsKey(name)) {
                    collection.setJSONString(((JSONable)super.get(name)).toJSONString());
                }
            }
            catch (JSONException e) {
                Trace.logError("Failed getCollection in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return collection;
        }
        return null;
    }

    public PropertyListCollection getCollectionNotNull(String name) {
        PropertyListCollection ret = this.getCollection(name);
        return ret == null ? new PropertyListCollection() : ret;
    }

    public PropertyList getPropertyList(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof PropertyList) {
                return (PropertyList)super.get(name);
            }
            PropertyList propertyList = new PropertyList();
            try {
                if (super.containsKey(name)) {
                    propertyList.setJSONString(((JSONable)super.get(name)).toJSONString());
                }
            }
            catch (JSONException e) {
                Trace.logError("Failed getPropertyList in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return propertyList;
        }
        return null;
    }

    public PropertyList getPropertyListNotNull(String name) {
        PropertyList ret = this.getPropertyList(name);
        return ret == null ? new PropertyList() : ret;
    }

    public FormValue getForm(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof FormValue) {
                return (FormValue)super.get(name);
            }
            return new FormValue(((JSONable)super.get(name)).toJSONString());
        }
        return null;
    }

    public SDIMaint getSDIMaint(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof SDIMaint) {
                return (SDIMaint)super.get(name);
            }
            return new SDIMaint(((JSONable)super.get(name)).toJSONString());
        }
        return null;
    }

    public SDIRequest getSDIRequest(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof SDIRequest) {
                return (SDIRequest)super.get(name);
            }
            return JSONUtil.getSDIRequest(((JSONable)super.get(name)).toJSONString());
        }
        return null;
    }

    public SDIList getSDIList(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof SDIList) {
                return (SDIList)super.get(name);
            }
            SDIList sdiList = new SDIList();
            try {
                sdiList.setJSONObject(new JSONObject(((JSONable)super.get(name)).toJSONString()));
            }
            catch (JSONException e) {
                Trace.logError("Failed getSDIList in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return sdiList;
        }
        return null;
    }

    public TaskContext getTaskContext(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof TaskContext) {
                return (TaskContext)super.get(name);
            }
            TaskContext taskContext = new TaskContext();
            try {
                taskContext.setJSONObject(new JSONObject(((JSONable)super.get(name)).toJSONString()));
            }
            catch (JSONException e) {
                Trace.logError("Failed getTaslContext in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return taskContext;
        }
        return null;
    }

    public DataSet getDataSet(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof DataSet) {
                return (DataSet)super.get(name);
            }
            DataSet dataset = new DataSet();
            try {
                dataset.setJSONObject(new JSONObject(((JSONable)super.get(name)).toJSONString()));
            }
            catch (JSONException e) {
                Trace.logError("Failed getDataSet in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return dataset;
        }
        return null;
    }

    public JSONableMap getJSONableMap(String name) {
        if (super.containsKey(name)) {
            if (super.get(name) instanceof JSONableMap) {
                return (JSONableMap)super.get(name);
            }
            JSONableMap map = new JSONableMap();
            try {
                JSONableMap jsonStringMap = new JSONableMap();
                jsonStringMap.setJSONObject(new JSONObject(((JSONable)super.get(name)).toJSONString()));
                for (String key : jsonStringMap.keySet()) {
                    if (key.endsWith("_type")) continue;
                    map.put(key, jsonStringMap.get(key));
                }
            }
            catch (JSONException e) {
                Trace.logError("Failed getJSONableMap in JSONableMap. Reason: " + e.getMessage(), e);
            }
            return map;
        }
        return null;
    }

    public void setJSONObject(JSONObject jsonObject) throws JSONException {
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String name = (String)it.next();
            this.put(name, new JSONableString(jsonObject.getString(name)));
        }
    }

    public Map toMap() {
        HashMap<String, Object> stringMap = new HashMap<String, Object>();
        Iterator it = this.keySet().iterator();
        while (it.hasNext()) {
            String name;
            stringMap.put(name, this.get(name = (String)it.next()) instanceof JSONableString ? ((JSONableString)this.get(name)).toString() : this.get(name));
        }
        return stringMap;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonMap = new JSONObject();
        try {
            Iterator it = this.keySet().iterator();
            while (it.hasNext()) {
                String key;
                JSONable value = this.get(key = (String)it.next());
                jsonMap.put(key, value != null && value.toJSONString() != null ? value.toJSONString() : null);
                jsonMap.put(key + "_type", value != null && value.toJSONString() != null ? (value.getClass().getSimpleName().equals("FormValue") ? "Form" : value.getClass().getName().substring(value.getClass().getName().lastIndexOf(".") + 1)) : null);
            }
        }
        catch (JSONException e) {
            Trace.logError("Failed toJSONObject in JSONableMap. Reason: " + e.getMessage(), e);
        }
        return jsonMap;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }
}

