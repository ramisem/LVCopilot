/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class IdEditor
implements TypeSimple {
    public static final String COLUMNID_CATEGORYLIST = "_categorylist";
    public static final String COLUMNID_SDIWORKITEMLIST = "_sdiworkitemlist";

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        boolean changed;
        String originalValue;
        String thisPropertyListId;
        PropertyList thisPropertyList;
        StringBuffer out;
        block31: {
            String columnid;
            String columnType;
            block29: {
                block33: {
                    block32: {
                        block30: {
                            String actualValue;
                            propertyValue.value = DOMUtil.convertChars(propertyValue.value);
                            out = new StringBuffer();
                            thisPropertyList = propertyValue.getParentPropertyList();
                            if (thisPropertyList == null) {
                                out.append("N/A");
                                return out.toString();
                            }
                            thisPropertyListId = thisPropertyList.getId();
                            originalValue = propertyValue.value;
                            String string = actualValue = ancestorValue ? propertyValue.toString().substring("{|".length(), propertyValue.toString().length() - "|}".length()) : propertyValue.toString();
                            if (ancestorValue) {
                                out.append("<input type=\"hidden\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" value=\"" + propertyValue + "\"/>");
                                out.append("<span title=\"Internal propertylistid: " + thisPropertyListId + "\" style=\"color: blue\"><i>" + propertyValue + "</i></span>");
                                return out.toString();
                            }
                            if (!"Y".equals(attributes.get("stellarlistcolumn"))) break block29;
                            columnType = thisPropertyList.getProperty("columntype");
                            columnid = actualValue;
                            if (!columnType.equals("primary")) break block30;
                            String primarycolumnid = thisPropertyList.getProperty("primarycolumnid").trim();
                            if (primarycolumnid.length() > 0) {
                                propertyValue.value = primarycolumnid.toLowerCase();
                            }
                            break block31;
                        }
                        if (!columnType.equals("fk")) break block32;
                        String fkcolumnid = thisPropertyList.getProperty("fkcolumnid").trim();
                        if (fkcolumnid.length() > 0) {
                            propertyValue.value = fkcolumnid.toLowerCase();
                        }
                        break block31;
                    }
                    if (!columnType.equals("nested")) break block33;
                    String nestedsql = thisPropertyList.getProperty("nestedsql");
                    if (nestedsql.startsWith("(")) {
                        String alias = nestedsql.substring(nestedsql.lastIndexOf(" ") + 1);
                        if (alias.length() > 0 && nestedsql.endsWith(") " + alias)) {
                            propertyValue.value = alias;
                        }
                        break block31;
                    } else if (columnid.length() == 0) {
                        propertyValue.value = "(?) Enter an alias";
                    }
                    break block31;
                }
                if (!columnType.equals("attribute")) {
                    // empty if block
                }
                break block31;
            }
            if ("Y".equals(attributes.get("stellarmaintcolumn"))) {
                columnType = thisPropertyList.getProperty("columntype");
                if (columnType.equals("category")) {
                    propertyValue.value = COLUMNID_CATEGORYLIST;
                } else if (columnType.equals("testmethod")) {
                    propertyValue.value = COLUMNID_SDIWORKITEMLIST;
                } else if (columnType.equals("detaillist")) {
                    propertyValue.value = "_" + thisPropertyList.getProperty("dlinkid").toLowerCase();
                } else if (columnType.equals("primary")) {
                    columnid = thisPropertyList.getProperty("primarycolumnid", thisPropertyList.getProperty("detailcolumnid", thisPropertyList.getProperty("sdidetailcolumnid"))).trim();
                    propertyValue.value = columnid.contains(" ") ? columnid.substring(columnid.lastIndexOf(" ") + 1).toLowerCase() : columnid.toLowerCase();
                } else if (columnType.equals("attribute")) {
                    String attributeid = thisPropertyList.getProperty("attributeid").trim();
                    propertyValue.value = "att_" + attributeid.toLowerCase();
                } else if (columnType.equals("display")) {
                    String sql = thisPropertyList.getPropertyListNotNull("displaycolumnprops").getProperty("displaycolumnsql", thisPropertyList.getPropertyListNotNull("displaycolumnprops").getProperty("displaycolumndotsyntax")).trim().toLowerCase();
                    propertyValue.value = sql.length() == 0 ? "(?) Enter display properties" : ((sql = StringUtil.replaceAll(sql, "select ", "").trim()).contains(" ") ? sql.substring(0, sql.indexOf(" ")) : sql);
                }
            }
        }
        StringBuffer blocklist = new StringBuffer(";");
        try {
            PropertyValue parentPropertyValue = thisPropertyList.getParentPropertyValue();
            String propertyid = parentPropertyValue.getId();
            PropertyListCollection collection = parentPropertyValue.getParentPropertyList().getCollectionNotNull(propertyid);
            if (collection != null) {
                for (PropertyList propertyList : collection) {
                    if (propertyList.getId().equals(thisPropertyListId)) continue;
                    blocklist.append(propertyList.getProperty(propertyValue.getId())).append(";");
                }
            }
        }
        catch (Throwable parentPropertyValue) {
            // empty catch block
        }
        boolean duplicate = propertyValue.value != null && propertyValue.value.length() > 0 && blocklist.indexOf(";" + propertyValue.value + ";") >= 0;
        boolean bl = changed = !propertyValue.value.equals(originalValue);
        if (changed) {
            attributes.put("__showwarning", "An identifer column has changed and needs saving.");
        }
        out.append("<input title=\"Internal propertylistid: " + thisPropertyListId + "\" onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:200px " + (duplicate || changed ? ";color: red;font-weight: bold" : "") + "\" onchange=\"this.style.color='black';checkEvent( this );this.style.fontWeight='normal'; " + (blocklist.length() > 1 ? "if ( this.value.length > 0 && '" + blocklist + "'.indexOf( ';' + this.value + ';' ) >= 0 ) { sapphire.alert( 'Identifiers must be unique. Please enter another value.' ); this.value = '';this.focus();return false;}" : "") + "\" value=\"" + propertyValue.value + "\"/>");
        if (changed) {
            if (propertyValue.value.contains("?")) {
                out.append(" <span style=\"color: red\">(Add more properties then save.)</span>");
            } else {
                out.append(" <span style=\"color: red\">(Generated value. Please save)</span>");
            }
        }
        if (!duplicate) return out.toString();
        out.append(" <span style=\"color: red\">(Duplicate. Please change one)</span>");
        return out.toString();
    }
}

