/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.TableObject;
import com.labvantage.sapphire.modules.adhocbrowser.TableProperty;
import java.util.ArrayList;

public class HibernateMappingXMLUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static String getTableMapping(TableObject tableObject) {
        StringBuffer xml = new StringBuffer();
        String tableid = tableObject.getName();
        String idColumn = tableObject.getIdProperty();
        ArrayList columns = tableObject.getTablecolumns();
        xml.append(HibernateMappingXMLUtil.getStandardHeader(tableid));
        xml.append(HibernateMappingXMLUtil.getIdElement(idColumn, "string"));
        for (int i = 0; i < columns.size(); ++i) {
            TableProperty tableProperty = (TableProperty)columns.get(i);
            xml.append(HibernateMappingXMLUtil.getPropertyElement(tableProperty));
        }
        if ("worksheetsdi_Sample".equals(tableid)) {
            xml.append(HibernateMappingXMLUtil.getSetElement("sdidata", "keyid1", "sdidata"));
            xml.append(HibernateMappingXMLUtil.getSetElement("sdidataitem", "keyid1", "sdidataitem"));
        }
        xml.append(HibernateMappingXMLUtil.getStandardFooter());
        return xml.toString();
    }

    public static String getPropertyElement(TableProperty tableProperty) {
        return HibernateMappingXMLUtil.getPropertyElement(tableProperty.getPropertyid(), tableProperty.getType());
    }

    public static String getPropertyElement(String columnid, String type) {
        return "\n<property name=\"" + columnid + "\" type=\"" + type + "\" column=\"" + columnid.toUpperCase() + "\" insert=\"false\" update=\"false\"/>";
    }

    public static String getFKColumnPropertyElement(String columnid, String type) {
        return "\n<property name=\"" + columnid + "_column\" type=\"" + type + "\" column=\"" + columnid.toUpperCase() + "\" insert=\"false\" update=\"false\"/>";
    }

    public static String getSetElement(String onetomanytableid, String keycolumnid) {
        return HibernateMappingXMLUtil.getSetElement(onetomanytableid, keycolumnid, onetomanytableid);
    }

    public static String getSetElement(String bagname, String keycolumn, String onetomanyentityname) {
        return "\n<set name=\"" + bagname + "\">\n<key column=\"" + keycolumn + "\"/>\n<one-to-many entity-name=\"" + onetomanyentityname + "\"/>\n</set>";
    }

    public static String getManyToOneElement(String columnid, String linktableid) {
        return "\n<many-to-one name=\"" + columnid + "\" column=\"" + columnid.toUpperCase() + "\" entity-name=\"" + linktableid + "\" insert=\"false\" update=\"false\"/>";
    }

    public static String getIdElement(String columnid, String type) {
        return "\n<id name=\"" + columnid + "\" type=\"" + type + "\" column=\"" + columnid + "\"></id>";
    }

    protected static String getStandardHeader(String tableid) {
        return "\n<class mutable=\"false\" entity-name=\"" + tableid + "\">";
    }

    protected static String getStandardFooter() {
        return "\n</class>\n";
    }
}

