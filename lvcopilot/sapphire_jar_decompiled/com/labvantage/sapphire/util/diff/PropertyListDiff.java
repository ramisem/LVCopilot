/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyListDiff {
    public static final String PROPERTY_ADDED = "Added";
    public static final String PROPERTY_DELETED = "Deleted";
    public static final String PROPERTY_CHANGED = "Changed";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_PROPERTYSTATUS = "propertystatus";
    public static final String COLUMN_PROPERTYID = "propertyid";
    public static final String COLUMN_OLDVALUE = "oldvalue";
    public static final String COLUMN_NEWVALUE = "newvalue";
    PropertyList original;
    PropertyList modified;
    PropertyDefinitionList definitionList;
    DataSet results;

    public PropertyListDiff(PropertyDefinitionList definitionList, PropertyList original, PropertyList modified) {
        this.original = original == null ? new PropertyList() : original.copy();
        this.modified = modified;
        this.definitionList = definitionList;
        this.results = new DataSet();
        this.performDiff();
    }

    public PropertyListDiff(PropertyList original, PropertyList modified) {
        this.original = original == null ? new PropertyList() : original.copy();
        this.modified = modified;
        this.results = new DataSet();
        this.definitionList = null;
        this.performDiff();
    }

    private void performDiff() {
        this.performDiffPropertList("Top", null, this.original, this.modified, true);
    }

    public boolean hasDifferences() {
        return this.results.size() > 0;
    }

    private void performDiffPropertList(String root, PropertyDefinitionList definitionList, PropertyList original, PropertyList modified, boolean cascade) {
        if (original == null) {
            original = new PropertyList();
        }
        if (modified == null) {
            modified = new PropertyList();
        }
        String originalModuleList = original.getAttribute("modulelist");
        String modifiedModuleList = modified.getAttribute("modulelist");
        if (originalModuleList == null) {
            originalModuleList = "";
        }
        if (modifiedModuleList == null) {
            modifiedModuleList = "";
        }
        if (!originalModuleList.equals(modifiedModuleList)) {
            this.addPropertyChange(root, "modulelist", originalModuleList, modifiedModuleList);
        }
        String originalRoleList = original.getAttribute("rolelist");
        String modifiedRoleList = modified.getAttribute("rolelist");
        if (originalRoleList == null) {
            originalRoleList = "";
        }
        if (modifiedRoleList == null) {
            modifiedRoleList = "";
        }
        if (!originalRoleList.equals(modifiedRoleList)) {
            this.addPropertyChange(root, "rolelist", originalRoleList, modifiedRoleList);
        }
        if (definitionList == null) {
            definitionList = new PropertyDefinitionList("");
            this.addPropertyDefinitions(definitionList, original);
            this.addPropertyDefinitions(definitionList, modified);
        }
        for (PropertyDefinition propertyDefinition : definitionList) {
            String originalValue;
            String modifiedValue;
            PropertyDefinitionList subDefinitionList;
            String propertyid = propertyDefinition.getId();
            if (cascade && propertyDefinition.getType().equals("collection")) {
                PropertyListCollection originalCollection = original.getCollectionNotNull(propertyid);
                PropertyListCollection modifiedCollection = modified.getCollectionNotNull(propertyid);
                subDefinitionList = propertyDefinition.getPropertyDefinitionList();
                String title = subDefinitionList.getLabelSingular();
                if (title == null || title.length() == 0) {
                    title = propertyDefinition.getTitle();
                }
                if (title == null || title.length() == 0) {
                    title = propertyid;
                }
                int newColumnCount = 1;
                Iterator iterator1 = modifiedCollection.iterator();
                while (iterator1.hasNext()) {
                    boolean nextCascade = true;
                    PropertyList modifiedSubPropertyList = (PropertyList)iterator1.next();
                    String id = modifiedSubPropertyList.getId();
                    String idprop = modifiedSubPropertyList.getProperty("id");
                    PropertyList originalSubPropertyList = originalCollection.getPropertyList(id);
                    String label = root + "/Changed " + title + ": " + idprop;
                    if (originalSubPropertyList == null) {
                        if (idprop.length() > 0) {
                            originalSubPropertyList = originalCollection.find("id", idprop);
                            label = root + "/Changed " + title + ": " + idprop;
                        } else {
                            String matchProperty = propertyDefinition.getMatchProperty();
                            String matchValue = modifiedSubPropertyList.getProperty(matchProperty);
                            if (matchValue.length() > 0) {
                                originalSubPropertyList = originalCollection.find(matchProperty, matchValue);
                                label = root + "/Changed " + title + ": " + matchValue;
                            } else {
                                String modifiedString = modifiedSubPropertyList.toXMLString();
                                for (PropertyList propertyList : originalCollection) {
                                    if (!modifiedString.equals(propertyList.toXMLString())) continue;
                                    originalSubPropertyList = propertyList;
                                    label = "";
                                    break;
                                }
                            }
                        }
                    }
                    if (originalSubPropertyList == null) {
                        label = modifiedSubPropertyList.getProperty("id");
                        if (label.length() == 0 && (label = modifiedSubPropertyList.getProperty(propertyDefinition.getMatchProperty())).length() == 0) {
                            label = Integer.toString(newColumnCount++);
                        }
                        label = root + "/New " + title + ": " + label;
                        nextCascade = false;
                    } else {
                        originalCollection.remove(originalSubPropertyList);
                    }
                    this.performDiffPropertList(label, subDefinitionList, originalSubPropertyList, modifiedSubPropertyList, nextCascade);
                }
                int deletedColumnCount = 1;
                for (PropertyList originalSubPropertyList : originalCollection) {
                    String label = originalSubPropertyList.getProperty("id");
                    if (label.length() == 0 && (label = originalSubPropertyList.getProperty(propertyDefinition.getMatchProperty())).length() == 0) {
                        label = Integer.toString(deletedColumnCount++);
                    }
                    String level = root + "/Deleted " + title + ": " + label;
                    this.performDiffPropertList(level, subDefinitionList, originalSubPropertyList, null, false);
                }
                continue;
            }
            if (cascade && propertyDefinition.getType().equals("propertylist")) {
                PropertyList originalPropertyList = original.getPropertyListNotNull(propertyid);
                PropertyList modifiedPropertyList = modified.getPropertyListNotNull(propertyid);
                subDefinitionList = propertyDefinition.getPropertyDefinitionList();
                this.performDiffPropertList(root + "/" + propertyDefinition.getTitle(), subDefinitionList, originalPropertyList, modifiedPropertyList, true);
                continue;
            }
            if (!propertyDefinition.getType().equals("simple") || (modifiedValue = modified.getProperty(propertyid)).equals(originalValue = original.getProperty(propertyid))) continue;
            this.addPropertyChange(root, propertyid, originalValue, modifiedValue);
        }
    }

    private void addPropertyDefinitions(PropertyDefinitionList definitionList, PropertyList propertyList) {
        Set s = propertyList.keySet();
        for (String propertyid : s) {
            if (propertyList.isCollection(propertyid)) {
                PropertyListCollection c = propertyList.getCollection(propertyid);
                for (PropertyList subPropertyList : c) {
                    PropertyDefinitionList pdl;
                    PropertyDefinition pd = definitionList.getPropertyDef(propertyid);
                    if (pd == null) {
                        pd = new PropertyDefinition();
                        pd.setId(propertyid);
                        pd.setTitle(propertyid);
                        pd.setType("collection");
                        if (subPropertyList != null) {
                            subPropertyList.keySet();
                        }
                        definitionList.addPropertyDef(pd);
                    }
                    if ((pdl = pd.getPropertyDefinitionList()) == null) {
                        pdl = new PropertyDefinitionList("");
                        pd.setPropertyDefinitionList(pdl);
                    }
                    this.addPropertyDefinitions(pdl, subPropertyList);
                }
                continue;
            }
            if (propertyList.isPropertyList(propertyid)) {
                PropertyDefinitionList pdl;
                PropertyList subPropertyList = propertyList.getPropertyList(propertyid);
                PropertyDefinition pd = definitionList.getPropertyDef(propertyid);
                if (pd == null) {
                    pd = new PropertyDefinition();
                    pd.setId(propertyid);
                    pd.setTitle(propertyid);
                    pd.setType("propertylist");
                    definitionList.addPropertyDef(pd);
                }
                if ((pdl = pd.getPropertyDefinitionList()) == null) {
                    pdl = new PropertyDefinitionList("");
                    pd.setPropertyDefinitionList(pdl);
                }
                this.addPropertyDefinitions(pdl, subPropertyList);
                continue;
            }
            PropertyDefinition pd = definitionList.getPropertyDef(propertyid);
            if (pd != null) continue;
            pd = new PropertyDefinition();
            pd.setId(propertyid);
            pd.setTitle(propertyid);
            pd.setType("simple");
            definitionList.addPropertyDef(pd);
        }
    }

    public String toHTML(String stylePrefix) {
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("Level");
        titles.add("Property Status");
        titles.add("Property");
        titles.add("Old Value");
        titles.add("New Value");
        return this.results.toHTML(titles, stylePrefix);
    }

    private void addPropertyChange(String level, String propertyid, String originalValue, String modifiedValue) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, COLUMN_LEVEL, level);
        String status = PROPERTY_CHANGED;
        if (originalValue.length() == 0) {
            status = PROPERTY_ADDED;
        }
        if (modifiedValue.length() == 0) {
            status = PROPERTY_DELETED;
        }
        this.results.setString(newRow, COLUMN_PROPERTYSTATUS, status);
        this.results.setString(newRow, COLUMN_PROPERTYID, propertyid);
        this.results.setString(newRow, COLUMN_OLDVALUE, originalValue);
        this.results.setString(newRow, COLUMN_NEWVALUE, modifiedValue);
    }

    public DataSet getResults() {
        return this.results;
    }
}

