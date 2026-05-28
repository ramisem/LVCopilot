/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import sapphire.util.StringUtil;

public class PropertyList
implements Serializable {
    public String[] propertyid = new String[0];
    public String[] propertyvalue = new String[0];
    private static final String DELIM = "|!|";

    public PropertyList() {
    }

    public PropertyList(String list) {
        this.setDelimeteredProperties(list);
    }

    public void setDelimeteredProperties(String props) {
        String[] p;
        if (props != null && props.length() > 0 && (p = StringUtil.split(props, DELIM)).length > 0 && p[0].indexOf(61) != -1) {
            this.propertyid = new String[p.length];
            this.propertyvalue = new String[p.length];
            for (int i = 0; i < p.length; ++i) {
                int j = p[i].indexOf(61);
                if (j <= 0) continue;
                this.propertyid[i] = p[i].substring(0, j);
                this.propertyvalue[i] = p[i].substring(j + 1);
            }
        }
    }

    public PropertyList(HashMap hashmap) {
        if (hashmap.size() > 0) {
            Set keys = hashmap.keySet();
            this.propertyid = new String[hashmap.size()];
            this.propertyvalue = new String[hashmap.size()];
            int i = 0;
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                this.propertyid[i] = (String)iterator.next();
                if (!(hashmap.get(this.propertyid[i]) instanceof String)) continue;
                this.propertyvalue[i] = (String)hashmap.get(this.propertyid[i]);
                ++i;
            }
        }
    }

    public String toString() {
        StringBuffer props = new StringBuffer("{");
        for (int i = 0; i < this.propertyid.length; ++i) {
            if (this.propertyid[i] == null || this.propertyid[i].length() <= 0) continue;
            props.append(this.propertyid[i] + "=" + this.propertyvalue[i] + ", ");
        }
        props.append("}");
        return props.toString();
    }

    public String concatenate() {
        StringBuffer props = new StringBuffer(100);
        for (int i = 0; i < this.propertyid.length; ++i) {
            if (this.propertyid[i] == null || this.propertyid[i].length() <= 0) continue;
            props.append(DELIM + this.propertyid[i] + "=" + this.propertyvalue[i]);
        }
        String s = props.toString();
        if (s.length() > 0) {
            s = s.substring(DELIM.length());
        }
        return s;
    }

    public String getProperty(String propertyid) {
        int position;
        String rc = "";
        if (propertyid == null) {
            propertyid = "";
        }
        if ((position = this.findProperty(propertyid)) >= 0) {
            rc = this.propertyvalue[position];
        }
        if (rc == null) {
            rc = "";
        }
        return rc;
    }

    public String getProperty(String propertyid, String defaultValue) {
        String rc = this.getProperty(propertyid);
        if (rc.length() == 0) {
            rc = defaultValue;
        }
        return rc;
    }

    public void setProperty(String propertyid, String propertyvalue) {
        if (propertyid != null && !propertyid.equals("")) {
            int position = this.findProperty(propertyid);
            if (position == -1) {
                int len = this.propertyid.length;
                String[] temppropertyid = new String[len + 1];
                String[] temppropertyvalue = new String[len + 1];
                System.arraycopy(this.propertyid, 0, temppropertyid, 0, len);
                System.arraycopy(this.propertyvalue, 0, temppropertyvalue, 0, len);
                this.propertyid = temppropertyid;
                this.propertyvalue = temppropertyvalue;
                this.propertyid[len] = propertyid;
                this.propertyvalue[len] = propertyvalue;
            } else {
                this.propertyvalue[position] = propertyvalue;
            }
        }
    }

    private int findProperty(String propertyid) {
        int position = -1;
        if (propertyid != null && this.propertyid != null) {
            String target = propertyid.toUpperCase();
            for (int i = 0; i < this.propertyid.length && position == -1; ++i) {
                if (!this.propertyid[i].toUpperCase().equals(target)) continue;
                position = i;
            }
        }
        return position;
    }

    public void deleteProperty(String propertyid) {
        int position;
        if (propertyid != null && (position = this.findProperty(propertyid)) >= 0) {
            this.propertyid[position] = "";
            this.propertyvalue[position] = "";
        }
    }

    public void setProperties(HashMap hashmap) {
        if (hashmap.size() > 0) {
            Set keys = hashmap.keySet();
            this.propertyid = new String[hashmap.size()];
            this.propertyvalue = new String[hashmap.size()];
            int i = 0;
            for (String key : keys) {
                if (!(hashmap.get(key) instanceof String) && hashmap.get(key) != null) continue;
                this.propertyid[i] = key;
                this.propertyvalue[i] = (String)hashmap.get(key);
                ++i;
            }
        }
    }

    public HashMap getProperties() {
        HashMap<String, String> hashmap = null;
        if (this.propertyid.length > 0) {
            hashmap = new HashMap(this.propertyid.length);
            for (int i = 0; i < this.propertyid.length; ++i) {
                hashmap.put(this.propertyid[i], this.propertyvalue[i]);
            }
        } else {
            hashmap = new HashMap<String, String>();
        }
        return hashmap;
    }
}

