/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Attribute
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import java.util.Iterator;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class StorageImporter {
    private String trackitemallowedflag = "N";
    private String spaceavailflag = "N";
    private String moveableflag = "N";
    private String lastnodeflag = "N";
    private String maxtiallowed = "0";
    private int storagesequence = 8000;
    private DataSet containerDataSet;
    private DataSet restrictionsDataSet;
    private String importXML;
    private SequenceProcessor sequenceProcessor;
    private static final String SDC_PHYSICALSTORE = "PhysicalStore";
    private static final String SDC_STORAGEUNITSDC = "StorageUnitSDC";

    public StorageImporter(String importXML, SequenceProcessor sequenceProcessor) {
        this.importXML = importXML;
        this.sequenceProcessor = sequenceProcessor;
    }

    public DataSet getImportDataSet() {
        int storageunitsize = 0;
        DataSet storageDataSet = new DataSet();
        storageDataSet.addColumn("storageunitid", 0);
        storageDataSet.addColumn("ancestorid", 0);
        storageDataSet.addColumn("storageunitdesc", 0);
        storageDataSet.addColumn("parentid", 0);
        storageDataSet.addColumn("storageenvid", 0);
        storageDataSet.addColumn("storageunittype", 0);
        storageDataSet.addColumn("storageunitlabel", 0);
        storageDataSet.addColumn("trackitemallowedflag", 0);
        storageDataSet.addColumn("spaceavailflag", 0);
        storageDataSet.addColumn("moveableflag", 0);
        storageDataSet.addColumn("linksdcid", 0);
        storageDataSet.addColumn("linkkeyid1", 0);
        storageDataSet.addColumn("notes", 0);
        storageDataSet.addColumn("templateflag", 0);
        storageDataSet.addColumn("labelpath", 0);
        storageDataSet.addColumn("storageunitsize", 1);
        storageDataSet.addColumn("maxtiallowed", 1);
        storageDataSet.addColumn("storageunitindex", 1);
        storageDataSet.addColumn("propertytreeid", 0);
        storageDataSet.addColumn("freezethawcandidateflag", 0);
        storageDataSet.addColumn("lastnodeflag", 0);
        storageDataSet.addColumn("activeflag", 0);
        storageDataSet.addColumn("lastnodecapacity", 1);
        storageDataSet.addColumn("enteredspecimencapacity", 1);
        storageDataSet.addColumn("unmanagedflag", 0);
        storageDataSet.addColumn("numrows", 1);
        storageDataSet.addColumn("labelrow", 0);
        storageDataSet.addColumn("numcol", 1);
        storageDataSet.addColumn("labelcol", 0);
        storageDataSet.addColumn("arraylayoutid", 0);
        storageDataSet.addColumn("arraylayoutversionid", 0);
        storageDataSet.addColumn("arraylayoutzone", 0);
        storageDataSet.addColumn("arraylayoutzonecolor", 0);
        this.containerDataSet = new DataSet();
        this.restrictionsDataSet = new DataSet();
        try {
            Document document = DocumentHelper.parseText((String)this.importXML);
            Element root = document.getRootElement();
            Iterator attributes = root.attributeIterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                String name = attribute.getName();
                String value = attribute.getValue();
                switch (name) {
                    case "size": {
                        storageunitsize = Integer.parseInt(value);
                        break;
                    }
                    case "trackitemallowedflag": {
                        this.trackitemallowedflag = value;
                        break;
                    }
                    case "spaceavailflag": {
                        this.spaceavailflag = value;
                        break;
                    }
                    case "moveableflag": {
                        this.moveableflag = value;
                        break;
                    }
                    case "lastnodeflag": {
                        this.lastnodeflag = value;
                        break;
                    }
                    case "maxtiallowed": {
                        this.maxtiallowed = value;
                    }
                }
            }
            Iterator iterator = root.elementIterator();
            while (iterator.hasNext()) {
                Element storageunitElement = (Element)iterator.next();
                this.storagesequence = this.sequenceProcessor.getSequence(SDC_STORAGEUNITSDC, "mainkey", storageunitsize);
                this.addStorageUnitElementToDataSet(storageunitElement, storageDataSet, null, null);
            }
        }
        catch (DocumentException e) {
            e.printStackTrace();
        }
        return storageDataSet;
    }

    public DataSet getContainerDataSet() {
        return this.containerDataSet;
    }

    public DataSet getRestrictionsDataSet() {
        return this.restrictionsDataSet;
    }

    private void addStorageUnitElementToDataSet(Element storageunitElement, DataSet storageDataSet, String parentid, String parentLabelPath) {
        block6: {
            String storageunitkey;
            block7: {
                block5: {
                    if (!"storagerestriction".equals(storageunitElement.getName())) break block5;
                    int row = this.restrictionsDataSet.addRow();
                    this.restrictionsDataSet.setString(row, "storageunitid", parentid);
                    this.restrictionsDataSet.setString(row, "storagerestrictionid", this.sequenceProcessor.getUUID());
                    this.restrictionsDataSet.setString(row, "restrictionbasedon", storageunitElement.attributeValue("restrictionbasedon"));
                    this.restrictionsDataSet.setString(row, "propertyid", storageunitElement.attributeValue("propertyid"));
                    this.restrictionsDataSet.setString(row, "propertyvalue", storageunitElement.attributeValue("propertyvalue"));
                    this.restrictionsDataSet.setString(row, "operator", storageunitElement.attributeValue("operator"));
                    this.restrictionsDataSet.setString(row, "failuremessage", storageunitElement.attributeValue("failuremessage"));
                    this.restrictionsDataSet.setNumber(row, "usersequence", storageunitElement.attributeValue("usersequence"));
                    break block6;
                }
                int row = storageDataSet.addRow();
                storageunitkey = "SU-" + StringUtil.padLeft(String.valueOf(this.storagesequence++), 10, '0');
                storageDataSet.setString(row, "storageunitid", storageunitkey);
                storageDataSet.setNumber(row, "storageunitindex", 1);
                Iterator attributes = storageunitElement.attributeIterator();
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute)attributes.next();
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    storageDataSet.setValue(row, name, this.deSanitizeText(value));
                }
                storageDataSet.setString(row, "trackitemallowedflag", storageunitElement.attributeValue("trackitemallowedflag") != null ? storageunitElement.attributeValue("trackitemallowedflag") : this.trackitemallowedflag);
                storageDataSet.setString(row, "spaceavailflag", storageunitElement.attributeValue("spaceavailflag") != null ? storageunitElement.attributeValue("spaceavailflag") : this.spaceavailflag);
                storageDataSet.setString(row, "moveableflag", storageunitElement.attributeValue("moveableflag") != null ? storageunitElement.attributeValue("moveableflag") : this.moveableflag);
                storageDataSet.setString(row, "lastnodeflag", storageunitElement.attributeValue("lastnodeflag") != null ? storageunitElement.attributeValue("lastnodeflag") : this.lastnodeflag);
                storageDataSet.setNumber(row, "maxtiallowed", storageunitElement.attributeValue("maxtiallowed") != null ? storageunitElement.attributeValue("maxtiallowed") : this.maxtiallowed);
                if (OpalUtil.isNotEmpty(parentid)) {
                    storageDataSet.setString(row, "parentid", parentid);
                }
                parentLabelPath = (parentLabelPath == null ? "/" : parentLabelPath + "/") + storageDataSet.getString(row, "storageunitlabel");
                storageDataSet.setString(row, "labelpath", parentLabelPath);
                if (!"storagecontainer".equals(storageunitElement.getName())) break block7;
                String link_sdcid = storageunitElement.attributeValue("link_sdcid");
                String link_linkkeyid1 = storageunitElement.attributeValue("link_linkkeyid1");
                int containerRow = this.containerDataSet.addRow();
                this.containerDataSet.setString(containerRow, "storageunitid", storageunitkey);
                this.containerDataSet.setString(containerRow, "parentid", parentid);
                this.containerDataSet.setString(containerRow, "sdcid", link_sdcid);
                this.containerDataSet.setString(containerRow, "keyid1", link_linkkeyid1);
                Iterator attributes2 = storageunitElement.attributeIterator();
                while (attributes2.hasNext()) {
                    Attribute attribute = (Attribute)attributes2.next();
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    if (!name.startsWith("link_") || name.equals("link_sdcid") || name.equals("link_linkkeyid1")) continue;
                    this.containerDataSet.setString(containerRow, name, value);
                }
                int childStorageUnitSize = Integer.parseInt(storageunitElement.attributeValue("storageunitsize"));
                if (childStorageUnitSize <= 0) break block6;
                Element childStorageUnit = (Element)storageunitElement.elementIterator().next();
                String childStorageUnitType = childStorageUnit.attributeValue("storageunittype");
                String childPropertyTreeID = childStorageUnit.attributeValue("propertytreeid");
                String childLabelRow = childStorageUnit.attributeValue("labelrow");
                String childLabelCol = childStorageUnit.attributeValue("labelcol");
                String childMaxTIAllowed = childStorageUnit.attributeValue("maxtiallowed");
                String[] childStorageUnitLabelArray = StringUtil.split(childStorageUnit.attributeValue("storageunitlabel"), ";");
                String[] childLabelRowArray = childLabelRow != null ? StringUtil.split(childLabelRow, ";") : null;
                String[] childLabelColArray = childLabelCol != null ? StringUtil.split(childLabelCol, ";") : null;
                String[] childMaxTIAllowedArray = childMaxTIAllowed != null ? StringUtil.split(childMaxTIAllowed, ";") : null;
                for (int i = 0; i < childStorageUnitSize; ++i) {
                    int childrow = storageDataSet.addRow();
                    String childstorageunitkey = "SU-" + StringUtil.padLeft(String.valueOf(this.storagesequence++), 10, '0');
                    storageDataSet.setString(childrow, "storageunitid", childstorageunitkey);
                    storageDataSet.setString(childrow, "parentid", storageunitkey);
                    storageDataSet.setString(childrow, "storageunittype", childStorageUnitType);
                    storageDataSet.setString(childrow, "propertytreeid", childPropertyTreeID);
                    storageDataSet.setString(childrow, "storageunitlabel", childStorageUnitLabelArray[i]);
                    storageDataSet.setString(childrow, "labelrow", childLabelRowArray != null && childLabelRowArray.length > i ? childLabelRowArray[i] : "");
                    storageDataSet.setString(childrow, "labelcol", childLabelColArray != null && childLabelColArray.length > i ? childLabelColArray[i] : "");
                    storageDataSet.setNumber(childrow, "storageunitindex", i + 1);
                    storageDataSet.setString(childrow, "trackitemallowedflag", "Y");
                    storageDataSet.setString(childrow, "spaceavailflag", "Y");
                    storageDataSet.setString(childrow, "moveableflag", "N");
                    storageDataSet.setString(childrow, "lastnodeflag", "N");
                    storageDataSet.setNumber(childrow, "maxtiallowed", childMaxTIAllowedArray != null && childMaxTIAllowedArray.length > i ? childMaxTIAllowedArray[i] : "");
                    storageDataSet.setString(childrow, "labelpath", parentLabelPath + "/" + childStorageUnitLabelArray[i]);
                }
                break block6;
            }
            Iterator childiterator = storageunitElement.elementIterator();
            while (childiterator.hasNext()) {
                this.addStorageUnitElementToDataSet((Element)childiterator.next(), storageDataSet, storageunitkey, parentLabelPath);
            }
        }
    }

    private String deSanitizeText(String text) {
        text = StringUtil.replaceAll(text, "||quot||", "\"");
        text = StringUtil.replaceAll(text, "||newline||", "\n");
        return text;
    }
}

