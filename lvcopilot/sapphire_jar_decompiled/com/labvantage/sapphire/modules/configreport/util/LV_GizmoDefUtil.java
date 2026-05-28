/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.LV_GizmoDefRO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_GizmoDefUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderGizmoInfo(LV_GizmoDefRO sdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Gizmo info:");
        LV_GizmoDefRO gizmoDefRO = sdcRO;
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addRowItem("Gizmo ID", gizmoDefRO.getGizmoDefId(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Description", gizmoDefRO.getDescription(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Type", gizmoDefRO.getGizmoTypeFromDB());
        configReportContent.addRowItem("Gizmo Node", gizmoDefRO.getGizmoNode());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Roles", gizmoDefRO.getRoles(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Notes", gizmoDefRO.getPrimaryValue("notes"), 3);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderGizmoPropertiesDiff(LV_GizmoDefRO sdcRO, LV_GizmoDefRO refSdcRO) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Gizmo info:");
        PropertyList srcBasicProperties = sdcRO.getElementProperties().copy();
        PropertyList refBasicProperties = refSdcRO.getElementProperties().copy();
        configReportContent.startSubHeading("Gizmo Details:", "");
        configReportContent.appendSubSection(configReportContent.renderPropertyListDiff(srcBasicProperties, refBasicProperties, sdcRO.fetchGizmoPropertyTreeDefinition(), true, true, this.getTranslationProcessor(), true), "Gizmo Details", this.diffOnly);
        String srcNotificationsXML = srcBasicProperties.getPropertyListNotNull("gizmoprops").getProperty("notifications", "");
        String refNotificationsXML = srcBasicProperties.getPropertyListNotNull("gizmoprops").getProperty("notifications", "");
        if (srcNotificationsXML.length() > 0 || refNotificationsXML.length() > 0) {
            if (refNotificationsXML.length() == 0) {
                PropertyList notificationspl = new PropertyList();
                notificationspl.setPropertyList(srcNotificationsXML);
                if (notificationspl != null && !notificationspl.isEmpty()) {
                    PropertyListCollection notifications = notificationspl.getCollectionNotNull("notifications");
                    configReportContent.startSubHeading("Notifications:", "");
                    configReportContent.append(ConfigReportContent.getDeletedString(configReportContent.renderCollection(notifications, true).toString()));
                }
            } else if (srcNotificationsXML.length() == 0) {
                PropertyList notificationspl = new PropertyList();
                notificationspl.setPropertyList(refNotificationsXML);
                if (notificationspl != null && !notificationspl.isEmpty()) {
                    PropertyListCollection notifications = notificationspl.getCollectionNotNull("notifications");
                    configReportContent.startSubHeading("Notifications:", "");
                    configReportContent.append(ConfigReportContent.getNewString(configReportContent.renderCollection(notifications, true).toString()));
                }
            } else {
                PropertyList srcnotificationspl = new PropertyList();
                srcnotificationspl.setPropertyList(srcNotificationsXML);
                PropertyList refnotificationspl = new PropertyList();
                refnotificationspl.setPropertyList(refNotificationsXML);
                if (srcnotificationspl != null && !srcnotificationspl.isEmpty()) {
                    if (srcNotificationsXML.equals(refNotificationsXML)) {
                        configReportContent.startSubHeading("Notifications:", "");
                        PropertyListCollection collection = srcnotificationspl.getCollection("notifications");
                        ConfigReportContent content = new ConfigReportContent(this.config, "diff");
                        content = content.renderCollection(collection, false);
                        configReportContent.append(content.toString());
                    } else {
                        configReportContent.append(this.renderNotificationsPropertyListDiff(srcnotificationspl, refnotificationspl, true, true).toString());
                    }
                }
            }
        }
        return configReportContent;
    }

    public ConfigReportContent renderGizmoDiff(LV_GizmoDefRO sdcRO, LV_GizmoDefRO refSdcRO, boolean hideEmptyColumns) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Gizmo info:");
        LV_GizmoDefRO gizmoDefRO = sdcRO;
        LV_GizmoDefRO refGizmoDefRO = null;
        if (refSdcRO != null) {
            refGizmoDefRO = refSdcRO;
            refGizmoDefRO.currentSDI = refSdcRO.currentSDI;
        }
        configReportContent.startTable();
        configReportContent.startRow();
        if (refSdcRO == null || refSdcRO.currentSDIData == null) {
            configReportContent.addDiffRowItem("Gizmo ID", gizmoDefRO.getGizmoDefId(), "", 3, this.getTranslationProcessor());
        } else {
            configReportContent.addDiffRowItem("Gizmo ID", gizmoDefRO.getGizmoDefId(), refGizmoDefRO.getGizmoDefId(), 3, this.getTranslationProcessor());
        }
        configReportContent.endRow();
        if (gizmoDefRO.getDescription().length() > 0 || refGizmoDefRO != null && refGizmoDefRO.currentSDI != null && refGizmoDefRO.getDescription().length() > 0 || !hideEmptyColumns) {
            configReportContent.startRow();
            if (refSdcRO == null || refSdcRO.currentSDIData == null) {
                configReportContent.addDiffRowItem("Description", gizmoDefRO.getDescription(), "", 3, false, this.getTranslationProcessor(), false);
            } else {
                configReportContent.addDiffRowItem("Description", gizmoDefRO.getDescription(), refGizmoDefRO.getDescription(), 3, false, this.getTranslationProcessor(), false);
            }
            configReportContent.endRow();
        }
        configReportContent.startRow();
        if (refSdcRO == null || refSdcRO.currentSDIData == null) {
            configReportContent.addDiffRowItem("Type", gizmoDefRO.getGizmoTypeFromDB(), "");
            configReportContent.addDiffRowItem("Gizmo Node", gizmoDefRO.getGizmoNode(), "");
        } else {
            configReportContent.addDiffRowItem("Type", gizmoDefRO.getGizmoTypeFromDB(), refGizmoDefRO.getGizmoTypeFromDB());
            configReportContent.addDiffRowItem("Gizmo Node", gizmoDefRO.getGizmoNode(), refGizmoDefRO.getGizmoNode());
        }
        configReportContent.endRow();
        if (gizmoDefRO.getRoles().length() > 0 || refGizmoDefRO.getRoles().length() > 0 || !hideEmptyColumns) {
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Roles", gizmoDefRO.getRoles(), refGizmoDefRO.getRoles(), 3, false, this.getTranslationProcessor(), false);
            configReportContent.endRow();
        }
        if (gizmoDefRO.getPrimaryValue("notes").length() > 0 || refGizmoDefRO.getPrimaryValue("notes").length() > 0 || !hideEmptyColumns) {
            configReportContent.startRow();
            if (refSdcRO == null || refSdcRO.currentSDIData == null) {
                configReportContent.addDiffRowItem("Notes", gizmoDefRO.getPrimaryValue("notes"), "", 3, false, this.getTranslationProcessor(), false);
            } else {
                configReportContent.addDiffRowItem("Notes", gizmoDefRO.getPrimaryValue("notes"), refGizmoDefRO.getPrimaryValue("notes"), 3, false, this.getTranslationProcessor(), false);
            }
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderGizmoProperties(LV_GizmoDefRO sdcRO) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Two Property Lists:");
        PropertyList basicProperties = sdcRO.getElementProperties().copy();
        configReportContent.startSubHeading("Gizmo Details:", "");
        configReportContent.appendSubSection(configReportContent.renderPropertyList(basicProperties, sdcRO.fetchGizmoPropertyTreeDefinition(), true, true, this.getTranslationProcessor()), "Gizmo Details", this.diffOnly);
        String notificationsXML = basicProperties.getPropertyListNotNull("gizmoprops").getProperty("notifications", "");
        if (notificationsXML.length() > 0) {
            PropertyList notificationspl = new PropertyList();
            notificationspl.setPropertyList(notificationsXML);
            if (notificationspl != null && !notificationspl.isEmpty()) {
                PropertyListCollection notifications = notificationspl.getCollectionNotNull("notifications");
                configReportContent.startSubHeading("Notifications:", "");
                ConfigReportContent coll = configReportContent.renderCollection(notifications, true);
                configReportContent.append(coll.toString());
            }
        }
        return configReportContent;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        if (this.sdcRO != null && this.sdcRO.currentSDIData != null) {
            FileOutputStream defTreeFile;
            FileOutputStream valueTreeFile;
            String xmlGizmoProperties = ((LV_GizmoDefRO)this.sdcRO).getElementProperties().toXMLString();
            String xmlDefintionTree = ((LV_GizmoDefRO)this.sdcRO).fetchGizmoPropertyTreeDefinition().toXMLString();
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            String xmlValueTreeFileName = xmlSdiFileName.replace(".xml", "_gizmoproperties.xml");
            String xmlDefTreeFileName = xmlSdiFileName.replace(".xml", "_deftree.xml");
            try {
                valueTreeFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlValueTreeFileName);
                defTreeFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlDefTreeFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
            }
            try {
                valueTreeFile.write(xmlGizmoProperties.getBytes());
                valueTreeFile.close();
                defTreeFile.write(xmlDefintionTree.getBytes());
                defTreeFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }

    public ConfigReportContent renderNotificationsDiff(String src, String ref, boolean top) throws SapphireException {
        int i;
        ConfigReportContent temp = new ConfigReportContent(this.config, "Notifications PropertyList");
        PropertyList srcpl = new PropertyList();
        PropertyList refpl = new PropertyList();
        srcpl.setPropertyList(src);
        refpl.setPropertyList(ref);
        PropertyListCollection srcColl = srcpl.getCollection("notifications");
        PropertyListCollection refColl = refpl.getCollection("notifications");
        ConfigReportContent content = new ConfigReportContent(this.config, "diff");
        content.startTableInner();
        for (i = 0; i < srcColl.size(); ++i) {
            PropertyList srcNotification = srcColl.getPropertyList(i);
            PropertyList refNotification = null;
            content.startRow();
            ConfigReportContent row = new ConfigReportContent(this.config, "Notifications Details");
            if (i < refColl.size()) {
                refNotification = refColl.getPropertyList(i);
            }
            if (refNotification != null) {
                row.renderPropertyListDiff(srcNotification, refNotification, top, this.getTranslationProcessor());
            } else {
                row.renderNewPropertyList(srcNotification, top, this.getTranslationProcessor());
            }
            content.addRowItem("" + i, row.toString());
            content.endRow();
        }
        if (refColl.size() > srcColl.size()) {
            for (i = srcColl.size(); i < refColl.size(); ++i) {
                PropertyList refNotification = refColl.getPropertyList(i);
                content.startRow();
                ConfigReportContent row = new ConfigReportContent(this.config, "Notifications Details");
                row.renderDeletedPropertyList(refNotification, top, this.getTranslationProcessor());
                content.addRowItem("" + i, row.toString());
                content.endRow();
            }
        }
        content.endTable();
        temp.append(content.toString());
        return temp;
    }

    public ConfigReportContent renderNotificationsPropertyListDiff(PropertyList pl, PropertyList refPl, boolean top, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent temp = new ConfigReportContent(this.config, " PropertyList");
        if (refPl == null) {
            refPl = new PropertyList();
        }
        if (pl.size() == 0) {
            return temp;
        }
        if (top) {
            temp.startTable();
        } else {
            temp.startTableInner();
        }
        Object[] keyes = pl.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            String p1;
            ConfigReportContent plcontent;
            if (pl.isPropertyList(keyes[i].toString())) {
                PropertyList childRefPl = refPl.getPropertyList(keyes[i].toString());
                plcontent = new ConfigReportContent(this.config, "pl content");
                String propertylist = plcontent.renderPropertyListDiff(pl.getPropertyList(keyes[i].toString()), childRefPl, true, this.getTranslationProcessor()).toString();
                if (propertylist.length() <= 0) continue;
                temp.startRow();
                temp.addRowItem(keyes[i].toString(), propertylist);
                temp.endRow();
                continue;
            }
            if (pl.isCollection(keyes[i].toString())) {
                if (keyes[i].toString().equals("notifications")) {
                    ConfigReportContent content = this.renderNotificationsDiff(pl.toXMLString(), refPl.toXMLString(), top);
                    temp.startRow();
                    temp.addRowItem(keyes[i].toString(), content.toString());
                    temp.endRow();
                    continue;
                }
                PropertyListCollection childRefCollection = refPl.getCollectionNotNull(keyes[i].toString());
                plcontent = new ConfigReportContent(this.config, "pl content");
                String collection = plcontent.renderCollectionDiff(pl.getCollection(keyes[i].toString()), childRefCollection, false, this.getTranslationProcessor(), hideEmptyColumns).toString();
                if (collection.length() <= 0) continue;
                temp.startRow();
                temp.addRowItem(keyes[i].toString(), collection);
                temp.endRow();
                continue;
            }
            if (refPl != null) {
                p1 = pl.getProperty(keyes[i].toString(), "");
                String p2 = refPl.getProperty(keyes[i].toString(), "");
                if (p1.length() <= 0 && p2.length() <= 0) continue;
                temp.startRow();
                temp.addDiffRowItem(keyes[i].toString(), p1, p2);
                temp.endRow();
                continue;
            }
            p1 = pl.getProperty(keyes[i].toString(), "");
            if (p1.length() <= 0) continue;
            temp.startRow();
            temp.addRowItem(keyes[i].toString(), p1);
            temp.endRow();
        }
        temp.endTable();
        return temp;
    }
}

