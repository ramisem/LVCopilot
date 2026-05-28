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

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
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

public class PhysicalStoreViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimary(SDISnapshotItem sdiSnapshotItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, String status, boolean isChild) throws SapphireException {
        return this.renderPrimaryDiff(sdiSnapshotItem, sdiSnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild);
    }

    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean ignoreClobs) throws SapphireException {
        ConfigReportContent out = super.renderPrimaryDiff(source, ref, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, ignoreClobs);
        out.startSubSection("Freezer Hierarchy", "");
        SDIData srcSDIData = source.getSDIData();
        String srcfreezerxml = this.getPrimaryValue(srcSDIData, "__viewxml");
        try {
            Document src = DocumentHelper.parseText((String)srcfreezerxml);
            Element e = src.getRootElement();
            Element topElement = (Element)e.elementIterator().next();
            SDISnapshotViewer.NodeItem top = this.renderNode(topElement, 1);
            JSONArray rootArray = new JSONArray();
            rootArray.put(top);
            out.append("<script>");
            out.append("\nvar dataStr =" + rootArray.toString(4) + ";");
            out.append("</script>");
            out.append("<P>");
            out.append("<P>");
            out.append("<P>");
            out.append("   <iframe  id=\"freezerframe\"\n                             name=\"freezerframe\"\n                             src=\"rc?command=file&file=WEB-OPAL/pagetypes/misc/physicalstoreviewer.jsp\"\n                             height=400pt\n                             width=100%\n                             frameborder=0\n                             scrolling=auto\n                             style=\"padding-left:2px;border:0 solid black\">\n                        Iframes not supported\n                    </iframe>");
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

