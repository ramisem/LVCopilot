/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyDependency;
import com.labvantage.sapphire.xml.PropertyDependencyList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PropertyTreeDefHandler
extends SapphireSaxHandler {
    private PropertyTree propertyTree;
    private StringBuffer currentElementChars = new StringBuffer();
    private ArrayList propertyDefLists = new ArrayList();
    private String lastPropertyDefId = "";
    private PropertyDefinition lastPropertyDef = null;

    public PropertyTreeDefHandler(PropertyTree propertyTree) {
        this.propertyTree = propertyTree;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        String err = "";
        Properties attr = this.getAttributes(attributes);
        if (qName.equalsIgnoreCase("PROPERTYTREE")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("id") != null ? attr.getProperty("propertytreeid") : "");
            this.log("Start PROPERTYTREE " + id + "...");
        } else if (qName.equalsIgnoreCase("PROPERTYDEFLIST")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("propertydeflistid") != null ? attr.getProperty("propertydeflistid") : "");
            this.log("Start PROPERTYDEFLIST " + id + "...");
            PropertyDefinitionList propertyDefinitionList = new PropertyDefinitionList(this.lastPropertyDefId);
            propertyDefinitionList.setColor(attr.getProperty("color") != null ? attr.getProperty("color") : "");
            propertyDefinitionList.setDirection(attr.getProperty("direction") != null ? attr.getProperty("direction") : "");
            propertyDefinitionList.setLabelPlural(attr.getProperty("labelplural") != null ? attr.getProperty("labelplural") : "");
            propertyDefinitionList.setLabelSingular(attr.getProperty("labelsingular") != null ? attr.getProperty("labelsingular") : "");
            propertyDefinitionList.setTitlePropertyId(attr.getProperty("titlepropertyid") != null ? attr.getProperty("titlepropertyid") : "");
            propertyDefinitionList.setFlaggedPropertyId(attr.getProperty("flaggedpropertyid") != null ? attr.getProperty("flaggedpropertyid") : "");
            propertyDefinitionList.setHiddenPropertyId(attr.getProperty("hiddenpropertyid") != null ? attr.getProperty("hiddenpropertyid") : "");
            propertyDefinitionList.setUniqueIdPropertyId(attr.getProperty("uniqueidpropertyid") != null ? attr.getProperty("uniqueidpropertyid") : "");
            propertyDefinitionList.setTableStyle(attr.getProperty("tablestyle") != null ? attr.getProperty("tablestyle") : "");
            propertyDefinitionList.setAllowRoles("Y".equals(attr.getProperty("allowroles")) || "S".equals(attr.getProperty("allowroles")));
            propertyDefinitionList.setAppRolesOnly("S".equals(attr.getProperty("allowroles")));
            propertyDefinitionList.setShowhide("Y".equals(attr.getProperty("showhide")));
            propertyDefinitionList.setDeprecated("Y".equals(attr.getProperty("deprecate")));
            propertyDefinitionList.setAdvanced("Y".equals(attr.getProperty("advanced")));
            propertyDefinitionList.setAddMethod(attr.getProperty("addmethod") != null ? attr.getProperty("addmethod") : "");
            this.propertyDefLists.add(propertyDefinitionList);
        } else if (qName.equalsIgnoreCase("PROPERTYDEF")) {
            PropertyDefinition propertyDefinition;
            this.log("Start PROPERTYDEF " + attr.getProperty("id") + "...");
            this.lastPropertyDefId = attr.getProperty("id");
            this.lastPropertyDef = propertyDefinition = new PropertyDefinition();
            propertyDefinition.setId(attr.getProperty("id") != null ? attr.getProperty("id") : "");
            propertyDefinition.setType(attr.getProperty("type") != null ? attr.getProperty("type") : "");
            propertyDefinition.setTitle(attr.getProperty("title") != null ? attr.getProperty("title") : "");
            propertyDefinition.setEditor(attr.getProperty("editor") != null ? attr.getProperty("editor") : "");
            propertyDefinition.setShowIf(attr.getProperty("showif") != null ? attr.getProperty("showif") : "");
            propertyDefinition.setHelp(attr.getProperty("help") != null ? attr.getProperty("help") : "");
            propertyDefinition.setSdcid(attr.getProperty("sdcid") != null ? attr.getProperty("sdcid") : "");
            propertyDefinition.setExtendedWhere(attr.getProperty("extendedwhere") != null ? attr.getProperty("extendedwhere") : "");
            propertyDefinition.setValues(attr.getProperty("values") != null ? attr.getProperty("values") : "");
            propertyDefinition.setTranslate(attr.getProperty("translate") != null ? attr.getProperty("translate") : "");
            propertyDefinition.setMatchProperty(attr.getProperty("matchproperty") != null ? attr.getProperty("matchproperty") : "title");
            propertyDefinition.setDeprecated("Y".equals(attr.getProperty("deprecate")));
            propertyDefinition.setExpression("Y".equals(attr.getProperty("expression")));
            propertyDefinition.setResolution("Y".equals(attr.getProperty("resolution")));
            propertyDefinition.setAdvanced("Y".equals(attr.getProperty("advanced")));
            propertyDefinition.setDefaultValue(attr.getProperty("defaultvalue") != null ? attr.getProperty("defaultvalue") : "");
            propertyDefinition.setAttributes(new HashMap<Object, Object>(attr));
            ((PropertyDefinitionList)this.propertyDefLists.get(this.propertyDefLists.size() - 1)).addPropertyDef(propertyDefinition);
        } else if (qName.equalsIgnoreCase("PROPERTYDEPLIST")) {
            this.log("Start PROPERTYDEPLIST " + attr.getProperty("id") + "...");
            if (this.propertyTree.propertyDependencyList == null) {
                this.propertyTree.propertyDependencyList = new PropertyDependencyList();
            }
        } else if (qName.equalsIgnoreCase("PROPERTYDEP")) {
            this.log("Start PROPERTYDEPLIST " + attr.getProperty("id") + "...");
            PropertyDependency propertyDep = new PropertyDependency();
            propertyDep.setElementid(attr.getProperty("elementid"));
            propertyDep.setPropertytreeid(attr.getProperty("propertytreeid"));
            propertyDep.setMandatory(attr.getProperty("mandatory"));
            propertyDep.setDescription(attr.getProperty("description"));
            this.propertyTree.propertyDependencyList.addPropertyDep(propertyDep);
        } else {
            err = "Unrecognized element " + qName + " found in document " + this._xmlFile.getName();
            this.log(err);
        }
        if (err.length() > 0) {
            this.println(err);
            throw new SAXException(err);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String err = "";
        if (qName.equalsIgnoreCase("PROPERTYTREE")) {
            this.log("End PROPERTYTREE");
        } else if (qName.equalsIgnoreCase("PROPERTYDEFLIST")) {
            this.log("End PROPERTYDEFLIST ");
            String propertyDefId = ((PropertyDefinitionList)this.propertyDefLists.get(this.propertyDefLists.size() - 1)).getPropertyDefId();
            if (propertyDefId.length() > 0) {
                PropertyDefinition propertyDefinition = ((PropertyDefinitionList)this.propertyDefLists.get(this.propertyDefLists.size() - 2)).getPropertyDef(propertyDefId);
                propertyDefinition.setPropertyDefinitionList((PropertyDefinitionList)this.propertyDefLists.get(this.propertyDefLists.size() - 1));
                this.propertyDefLists.remove(this.propertyDefLists.size() - 1);
            } else {
                this.propertyTree.setPropertyDefinitionList((PropertyDefinitionList)this.propertyDefLists.get(this.propertyDefLists.size() - 1));
            }
        } else if (qName.equalsIgnoreCase("PROPERTYDEF")) {
            String defaultValue = this.currentElementChars.toString().trim();
            if (defaultValue.length() > 0) {
                this.lastPropertyDef.setDefaultValue(defaultValue);
            }
            this.log("End PROPERTYDEF");
            this.lastPropertyDefId = "";
        } else if (qName.equalsIgnoreCase("PROPERTYDEPLIST")) {
            this.log("End PROPERTYDEPLIST");
        } else if (qName.equalsIgnoreCase("PROPERTYDEP")) {
            this.log("End PROPERTYDEPLIST");
        } else {
            err = "Unrecognized element " + qName + " found in document " + this._xmlFile.getName();
            this.log(err);
        }
        if (err.length() > 0) {
            this.println(err);
            throw new SAXException(err);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

