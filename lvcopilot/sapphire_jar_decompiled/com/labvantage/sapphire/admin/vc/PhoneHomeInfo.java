/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.vc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PhoneHomeInfo {
    public static final String PROPERTY1_platform = "platform";
    public static final String PROPERTY2_platformversion = "platformversion";
    public static final String PROPERTY3_hostname = "hostname";
    public static final String PROPERTY4_serverid = "serverid";
    public static final String PROPERTY5_applicationid = "applicationid";
    public static final String PROPERTY6_build = "build";
    public static final String PROPERTY7_patch = "patch";
    public static final String PROPERTY8_javaversion = "javaversion";
    public static final String PROPERTY9_locale = "locale";
    public static final String PROPERTY10_tracing = "tracing";
    public static final String PROPERTY11_startupdt = "startupdt";
    public static final String PROPERTY12_startuperror = "startuperror";
    public static final String PROPERTY15_databases = "databases";
    public static final String Database_databaseid = "databaseid";
    public static final String Database_dbms = "dbms";
    public static final String Database_jdbcdriver = "jdbcdriver";
    public static final String Database_jdbcdriverversion = "jdbcdriver";
    public static final String Database_status = "status";
    private PropertyList startupProps = new PropertyList();
    public static String lastInfoSend = "";
    private boolean phoneHomeAllowed = true;
    private static PhoneHomeInfo me = new PhoneHomeInfo();

    public static PhoneHomeInfo getInstance() {
        return me;
    }

    public void collectInfo(String propertyid, String propertyvalue) {
        this.startupProps.setProperty(propertyid, propertyvalue);
    }

    public void collectDatabaseInfo(String databaseid, String propertyid, String propertyvalue) {
        if (this.startupProps.getCollection(PROPERTY15_databases) == null) {
            this.startupProps.setProperty(PROPERTY15_databases, new PropertyListCollection());
        }
        PropertyListCollection databases = this.startupProps.getCollection(PROPERTY15_databases);
        PropertyList databasePL = null;
        for (int i = 0; i < databases.size(); ++i) {
            if (databases.getPropertyList(i) == null || !databaseid.equals(databases.getPropertyList(i).getProperty(Database_databaseid))) continue;
            databasePL = databases.getPropertyList(i);
            break;
        }
        if (databasePL == null) {
            databasePL = new PropertyList();
            databasePL.setProperty(Database_databaseid, databaseid);
            databases.add(databasePL);
        }
        databasePL.setProperty(propertyid, propertyvalue);
    }

    public void setInfoFromJSONString(String jsonString) {
        try {
            this.startupProps.setJSONString(jsonString);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public PropertyList getInfo() {
        return this.startupProps;
    }

    public String toFormatedString() {
        StringBuilder sb = new StringBuilder();
        Set keys = this.startupProps.keySet();
        List<Object> list = Arrays.asList(keys.toArray());
        Collections.sort(list);
        sb.append("<table>");
        for (Object id : list) {
            String propertyid = (String)id;
            if (this.startupProps.get(propertyid) instanceof String) {
                sb.append("<tr><td>" + propertyid + "</td><td>" + this.startupProps.getProperty(propertyid) + "</td></tr>");
                continue;
            }
            if (!(this.startupProps.get(propertyid) instanceof PropertyListCollection)) continue;
            PropertyListCollection databases = this.startupProps.getCollection(propertyid);
            for (int i = 0; i < databases.size(); ++i) {
                sb.append("<tr><td>database" + i + "</td><td>" + databases.getPropertyList(i).toJSONString(false, false) + "</td></tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    public boolean isPhoneHomeAllowed() {
        return this.phoneHomeAllowed;
    }

    public void setPhoneHomeAllowed(boolean phoneHomeAllowed) {
        this.phoneHomeAllowed = phoneHomeAllowed;
    }
}

