/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.TableProperty;
import java.util.ArrayList;
import java.util.HashMap;

public class TableObject {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static HashMap typeMapping = new HashMap();
    private String name;
    private String idProperty;
    private ArrayList tablecolumns = new ArrayList();

    public TableObject(String name, String idProperty) {
        this.name = name;
        this.idProperty = idProperty;
    }

    public void addTableProperty(TableProperty tableProperty) {
        this.tablecolumns.add(tableProperty);
    }

    public String getName() {
        return this.name;
    }

    public String getIdProperty() {
        return this.idProperty;
    }

    public ArrayList getTablecolumns() {
        return this.tablecolumns;
    }

    public static String getType(String sapphireType) {
        if (typeMapping.get(sapphireType) == null) {
            throw new RuntimeException("Unrecognized Sapphire Type: " + sapphireType);
        }
        return (String)typeMapping.get(sapphireType);
    }

    static {
        typeMapping.put("C", "string");
        typeMapping.put("N", "big_decimal");
        typeMapping.put("R", "big_decimal");
        typeMapping.put("D", "timestamp");
    }
}

