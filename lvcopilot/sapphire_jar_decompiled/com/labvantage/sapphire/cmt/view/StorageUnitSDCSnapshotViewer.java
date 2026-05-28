/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Attribute
 *  org.dom4j.Document
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.Iterator;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class StorageUnitSDCSnapshotViewer
extends SDISnapshotViewer {
    public StorageUnitSDCSnapshotViewer() {
    }

    public StorageUnitSDCSnapshotViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection) {
        super.initialize(sapphireConnection);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot) throws SapphireException {
        return StorageUnitSDCSnapshotViewer.getHtml(sapphireConnection, srcSDISnapshot, true, true, false, true);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, boolean showAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        return StorageUnitSDCSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, srcSDISnapshot, showAuditColumns, hideEmptyColumns, hideInheritedProperties);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot) throws SapphireException {
        return StorageUnitSDCSnapshotViewer.hasDiff(sapphireConnection, srcSDISnapshot, refSDISnapshot, false);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot, boolean includeAuditColumns) throws SapphireException {
        StorageUnitSDCSnapshotViewer ssViewer = new StorageUnitSDCSnapshotViewer(sapphireConnection);
        return ssViewer.hasDiff(srcSDISnapshot, refSDISnapshot, includeAuditColumns);
    }

    public boolean hasDiff(PropertyTreeSnapshot srcSnapshot, PropertyTreeSnapshot refSnapshot, boolean showAuditColumns) throws SapphireException {
        return false;
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot) throws SapphireException {
        return StorageUnitSDCSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, refSDISnapshot, true, true, false);
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        StorageUnitSDCSnapshotViewer ssViewer = new StorageUnitSDCSnapshotViewer(sapphireConnection);
        ConfigReportContent snapshotContent = ssViewer.getHtml(srcSDISnapshot, refSDISnapshot, showAuditColumns, hideInheritedProperties, hideEmptyColumns);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(snapshotContent);
        return stringBuilder.toString();
    }

    @Override
    public ConfigReportContent getHtml(SDISnapshot srcSnapshot, SDISnapshot refSnapshot, boolean showAuditColumns, boolean hideInheritedProperties, boolean hideEmptyColumns) throws SapphireException {
        if (srcSnapshot == null) {
            throw new SapphireException("Snapshot is null");
        }
        if (srcSnapshot != refSnapshot) {
            throw new SapphireException("We currently do not support diffing of storage units");
        }
        ConfigReportContent out = new ConfigReportContent("StorageUnitSDC", this.translationProcessor);
        SDIData srcSDIData = srcSnapshot.getSnapshotItem().getSDIData();
        SDIData refSDIData = refSnapshot.getSnapshotItem().getSDIData();
        String srcfreezerxml = this.getPrimaryValue(srcSDIData, "__viewxml");
        ArrayList childNodeItems = new ArrayList();
        try {
            Document src = DocumentHelper.parseText((String)srcfreezerxml);
            Element e = src.getRootElement();
            Element topElement = (Element)e.elementIterator().next();
            SDISnapshotViewer.NodeItem top = this.renderNode(topElement, 1);
            JSONArray rootArray = new JSONArray();
            rootArray.put(top);
            out.append("\n<script>");
            out.append("\nvar initialContextData=" + rootArray.toString(4) + ";");
            out.append("\nvar mode=\"snapshotview\"");
            out.append("\nvar navigatorWidth=\"3000\"");
            out.append("\nvar navigator_props=" + new PropertyList().toJSONString(false, false) + ";");
            out.append("\nsapphire.gwt.addGWTElement('navigator','navigator', navigator_props );");
            out.append("\n</script>");
        }
        catch (Exception e) {
            out.append("<P>Failed to parse the viewxml content");
        }
        return out;
    }

    private SDISnapshotViewer.NodeItem renderNode(Element e, int level) throws JSONException {
        String suname;
        String displayString = suname = e.getName();
        Iterator attributes = e.attributeIterator();
        while (attributes.hasNext()) {
            Attribute next = (Attribute)attributes.next();
            String attributename = next.getName();
            String attributevalue = next.getValue();
            displayString = displayString + "," + attributename + ":" + attributevalue;
        }
        SDISnapshotViewer.NodeItem parent = new SDISnapshotViewer.NodeItem(suname, displayString, true);
        ArrayList<SDISnapshotViewer.NodeItem> childNodeItems = new ArrayList<SDISnapshotViewer.NodeItem>();
        Iterator children = e.elementIterator();
        while (children.hasNext()) {
            Element next = (Element)children.next();
            SDISnapshotViewer.NodeItem childnode = this.renderNode(next, level + 1);
            childNodeItems.add(childnode);
        }
        parent.addChildItems(childNodeItems);
        return parent;
    }
}

