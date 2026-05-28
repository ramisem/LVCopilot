/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.SnapshotItem;

public class LV_WorksheetSnapshotViewer
extends SDISnapshotViewer {
    public LV_WorksheetSnapshotViewer() {
    }

    public LV_WorksheetSnapshotViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection) {
        super.initialize(sapphireConnection);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot) throws SapphireException {
        return LV_WorksheetSnapshotViewer.getHtml(sapphireConnection, srcSDISnapshot, true, true, false, true);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, boolean showAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        return LV_WorksheetSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, srcSDISnapshot, showAuditColumns, hideEmptyColumns, hideInheritedProperties);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot) throws SapphireException {
        return LV_WorksheetSnapshotViewer.hasDiff(sapphireConnection, srcSDISnapshot, refSDISnapshot, false);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean includeAuditColumns) throws SapphireException {
        LV_WorksheetSnapshotViewer ssViewer = new LV_WorksheetSnapshotViewer(sapphireConnection);
        return ssViewer.hasDiff(srcSDISnapshot, refSDISnapshot, includeAuditColumns);
    }

    @Override
    public boolean hasDiff(SDISnapshot srcSnapshot, SDISnapshot refSnapshot, boolean showAuditColumns) throws SapphireException {
        boolean hasDiff = false;
        try {
            hasDiff = true;
        }
        catch (Throwable t) {
            Trace.logError("Server Error", t);
        }
        return hasDiff;
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot) throws SapphireException {
        return LV_WorksheetSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, refSDISnapshot, true, true, false);
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        LV_WorksheetSnapshotViewer ssViewer = new LV_WorksheetSnapshotViewer(sapphireConnection);
        ConfigReportContent snapshotContent = ssViewer.getHtml(srcSDISnapshot, refSDISnapshot, showAuditColumns, hideInheritedProperties, hideEmptyColumns);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(snapshotContent);
        return stringBuilder.toString();
    }

    @Override
    public ConfigReportContent getHtml(SDISnapshot srcSnapshot, SDISnapshot refSnapshot, boolean showAuditColumns, boolean hideInheritedProperties, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent out = new ConfigReportContent("LV_Worksheet", this.translationProcessor);
        ConfigReportContent rhs = new ConfigReportContent("rhs", this.translationProcessor);
        rhs = this.renderTemplateContents(srcSnapshot, refSnapshot, showAuditColumns, hideEmptyColumns);
        JSONArray rootArray = new JSONArray();
        DataSet nodeInfo = rhs.getNodeInfo();
        String primarynodeid = srcSnapshot.getSnapshotItem().toString();
        String label = SDISnapshotViewer.NodeItem.getNodeLabel(primarynodeid, nodeInfo);
        if (label.length() == 0) {
            label = ConfigReportContent.generateSDISectionTitle(LV_WorksheetSnapshotViewer.getSDI(srcSnapshot.getSnapshotItem().getSDIData()));
        }
        SDISnapshotViewer.NodeItem nodeItem = null;
        try {
            String srctemplatetypeflag = this.getPrimaryValue(srcSnapshot.getSnapshotItem().getSDIData(), "templatetypeflag");
            if ("W".equals(srctemplatetypeflag)) {
                nodeItem = new SDISnapshotViewer.NodeItem(primarynodeid, label, false);
            }
            nodeItem = this.addSectionNodes(nodeItem, srcSnapshot, refSnapshot, nodeInfo);
        }
        catch (Exception e) {
            out.append("Failed to add child sections");
        }
        rootArray.put(nodeItem);
        try {
            out.append("\n<script>");
            out.append("\nvar initialContextData=" + rootArray.toString(4) + ";");
            out.append("\nvar mode=\"snapshotview\"");
            out.append("\nvar navigator_props=" + new PropertyList().toJSONString(false, false) + ";");
            out.append("\nsapphire.gwt.addGWTElement('navigator','navigator', navigator_props );");
            out.append("\n</script>");
            out.append("\n<div style=\"display:none\" id=\"" + srcSnapshot.getSnapshotItem().toString() + "\">" + rhs.toString() + "</div>");
        }
        catch (Exception e) {
            out.append("<P>Failed to diff");
        }
        out.append("\n<script>");
        out.append("\nvar currentRootNodeid;");
        out.append("\nvar currentEl;");
        out.append("\nfunction snapshotNodeSelected(nodeid, rootnodeid) {//called when a node is clicked");
        out.append("\n   var isRootNode = nodeid == rootnodeid;");
        out.append("\n if (isRootNode) { ");
        out.append("\nshowSnapshotHTML(rootnodeid);//Snapshot Navigator API method to replace right panel with the innerHtml from element with the id;");
        out.append("\n} else {");
        out.append("\nif (rootnodeid != currentRootNodeid) {");
        out.append("\nshowSnapshotHTML(rootnodeid)");
        out.append("\ncurrentRootNodeid = rootnodeid;");
        out.append("\n}");
        out.append("\nif (currentEl != null) {");
        out.append("\ncurrentEl.style.backgroundColor = '';");
        out.append("\n}");
        out.append("\ncurrentEl = document.getElementById(nodeid);");
        out.append("\ncurrentEl.style.backgroundColor = 'lightblue';");
        out.append("\ncurrentEl.scrollIntoView();");
        out.append("\n} //API from Navigator to update right panel");
        out.append("\n}");
        out.append("\n</script>");
        return out;
    }

    private ConfigReportContent renderTemplateContents(SDISnapshot srcSnapshot, SDISnapshot refSnapshot, boolean showAuditColumns, boolean hideEmptyColumns) throws SapphireException {
        ArrayList<SnapshotItem> refSections;
        SDISnapshotItem srcSDISnapshotItem = srcSnapshot.getSnapshotItem();
        SDISnapshotItem refSDISnapshotItem = refSnapshot == null ? null : refSnapshot.getSnapshotItem();
        String worksheetsectionlink = "LV_WorksheetSection;Worksheet";
        ConfigReportContent templateContent = new ConfigReportContent("Template", this.translationProcessor);
        String srctemplatetypeflag = this.getPrimaryValue(srcSDISnapshotItem.getSDIData(), "templatetypeflag");
        String reftemplatetypeflag = this.getPrimaryValue(refSDISnapshotItem == null ? new SDIData() : refSDISnapshotItem.getSDIData(), "templatetypeflag");
        if (reftemplatetypeflag.length() > 0 && !srctemplatetypeflag.equals(reftemplatetypeflag)) {
            templateContent.append("<P>Cannot compare different template types. Both should either be Worksheets, Sections or Controls");
            return templateContent;
        }
        List<SnapshotItem> srcSections = srcSDISnapshotItem.getLinkItemsByLinkId(SnapshotItem.LinkType.REVFK, worksheetsectionlink);
        List<Object> list = refSections = refSDISnapshotItem == null ? new ArrayList() : refSDISnapshotItem.getLinkItemsByLinkId(SnapshotItem.LinkType.REVFK, worksheetsectionlink);
        if (srctemplatetypeflag.equals("I")) {
            ConfigReportContent controlContent = new ConfigReportContent("Control", this.translationProcessor);
            SDISnapshotItem srcSection0 = this.findSection0(srcSections);
            SDISnapshotItem refSection0 = this.findSection0(refSections);
            ArrayList<SnapshotItem> srcLevel1Items = new ArrayList();
            if (srcSection0 != null) {
                srcLevel1Items = this.findSectionChildItems(srcSection0);
            }
            List<SnapshotItem> refLevel1Items = new ArrayList<SnapshotItem>();
            if (refSection0 != null) {
                refLevel1Items = this.findSectionChildItems(refSection0);
            }
            ConfigReportContent childNodes = this.renderChildItems(srcLevel1Items, refLevel1Items, showAuditColumns, hideEmptyColumns);
            controlContent.appendSpecialContent(childNodes);
            templateContent.appendNodeContent(controlContent, srcSDISnapshotItem.toString(), "Control content label");
            return templateContent;
        }
        if (srctemplatetypeflag.equals("S")) {
            SDISnapshotItem sectionSnapshotitem = (SDISnapshotItem)srcSections.get(0);
            String[] pLabelInfo = this.getSDITableLabelInfo(sectionSnapshotitem);
            String tablelabel = pLabelInfo[1];
            String itemLabel = LV_WorksheetSnapshotViewer.getSDI(sectionSnapshotitem.getSDIData()).toString();
            if (tablelabel != null && tablelabel.length() > 0) {
                itemLabel = this.getFormattedItemLabel(sectionSnapshotitem.getSDIData(), tablelabel);
            }
            ConfigReportContent sectionContent = new ConfigReportContent("WorksheetSection", this.translationProcessor);
            SDISnapshotItem srcSection = (SDISnapshotItem)srcSections.get(0);
            ConfigReportContent content = this.renderWorksheetSection(srcSection, srcSections, refSections, showAuditColumns, hideEmptyColumns);
            sectionContent.appendNodeContent(content, sectionSnapshotitem.toString(), itemLabel);
            return sectionContent;
        }
        String[] pLabelInfo = this.getSDITableLabelInfo(srcSDISnapshotItem);
        String tablelabel = pLabelInfo[1];
        String itemLabel = LV_WorksheetSnapshotViewer.getSDI(srcSDISnapshotItem.getSDIData()).toString();
        if (tablelabel != null && tablelabel.length() > 0) {
            itemLabel = this.getFormattedItemLabel(srcSDISnapshotItem.getSDIData(), tablelabel);
        }
        ConfigReportContent worksheetContent = new ConfigReportContent("Worksheet", this.translationProcessor);
        ConfigReportContent childNodes = new ConfigReportContent("Childnodes", this.translationProcessor);
        worksheetContent.appendNodeContent(this.renderPrimaryDiff(srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, true, hideEmptyColumns, false, false), srcSDISnapshotItem.toString(), itemLabel);
        SDISnapshotItem srcsection0 = this.findSection0(srcSections);
        SDISnapshotItem refsection0 = this.findSection0(refSections);
        ArrayList<SnapshotItem> srcLevel1Items = new ArrayList();
        if (srcsection0 != null) {
            srcLevel1Items = this.findSectionChildItems(srcsection0);
        }
        List<SnapshotItem> refLevel1Items = new ArrayList<SnapshotItem>();
        if (refsection0 != null) {
            refLevel1Items = this.findSectionChildItems(refsection0);
        }
        childNodes = this.renderChildItems(srcLevel1Items, refLevel1Items, showAuditColumns, hideEmptyColumns);
        int i = 0;
        int processingsectionlevel = 1;
        while (i < srcSections.size()) {
            SDISnapshotItem srcSection = (SDISnapshotItem)srcSections.get(i);
            i = this.renderSection(childNodes, i, processingsectionlevel, srcSection, srcSections, refSections, showAuditColumns, hideEmptyColumns);
        }
        worksheetContent.appendSpecialContent(childNodes);
        templateContent.appendNodeContent(worksheetContent, srcSDISnapshotItem.toString(), itemLabel);
        return templateContent;
    }

    private SDISnapshotViewer.NodeItem addSectionNodes(SDISnapshotViewer.NodeItem root, SDISnapshot srcSnapshot, SDISnapshot refSnapshot, DataSet nodeInfo) throws Exception {
        SDISnapshotItem srcSDISnapshotItem = srcSnapshot.getSnapshotItem();
        SDISnapshotItem refSDISnapshotItem = refSnapshot == null ? null : refSnapshot.getSnapshotItem();
        String worksheetsectionlink = "LV_WorksheetSection;Worksheet";
        List<SnapshotItem> srcSections = srcSDISnapshotItem.getLinkItemsByLinkId(SnapshotItem.LinkType.REVFK, worksheetsectionlink);
        ArrayList<SnapshotItem> refSections = refSDISnapshotItem == null ? new ArrayList() : refSDISnapshotItem.getLinkItemsByLinkId(SnapshotItem.LinkType.REVFK, worksheetsectionlink);
        String templateflagtype = this.getPrimaryValue(srcSDISnapshotItem.getSDIData(), "templatetypeflag");
        if (!templateflagtype.equals("S")) {
            SDISnapshotItem srcsection0 = this.findSection0(srcSections);
            SDISnapshotItem refsection0 = this.findSection0(refSections);
            ArrayList<SnapshotItem> srcLevel1Items = new ArrayList();
            if (srcsection0 != null) {
                srcLevel1Items = this.findSectionChildItems(srcsection0);
            }
            List<SnapshotItem> refLevel1Items = new ArrayList<SnapshotItem>();
            if (refsection0 != null) {
                refLevel1Items = this.findSectionChildItems(refsection0);
            }
            ArrayList<SDISnapshotViewer.NodeItem> childNodes = this.processChildItems(srcLevel1Items, refLevel1Items, nodeInfo);
            int i = 0;
            int processingsectionlevel = 1;
            while (i < srcSections.size()) {
                SDISnapshotItem srcSection = (SDISnapshotItem)srcSections.get(i);
                i = this.processSection(childNodes, i, processingsectionlevel, srcSection, srcSections, refSections, nodeInfo);
            }
            if (root != null) {
                root.addChildItems(childNodes);
                return root;
            }
            return childNodes.get(0);
        }
        ArrayList<SDISnapshotViewer.NodeItem> childNodes = new ArrayList<SDISnapshotViewer.NodeItem>();
        int i = 0;
        int processingsectionlevel = 0;
        SDISnapshotItem srcSection = (SDISnapshotItem)srcSections.get(0);
        this.processSection(childNodes, i, processingsectionlevel, srcSection, srcSections, refSections, nodeInfo);
        if (root != null) {
            root.addChildItems(childNodes);
            return root;
        }
        return childNodes.get(0);
    }

    private SDISnapshotItem findSection0(List<SnapshotItem> sections) throws SapphireException {
        for (int i = 0; i < sections.size(); ++i) {
            String srcLevel;
            SDISnapshotItem srcItem = (SDISnapshotItem)sections.get(i);
            SDIData srcItemSDIData = srcItem.getSDIData();
            if (srcItemSDIData == null || !(srcLevel = this.getPrimaryValue(srcItemSDIData, "sectionlevel")).equals("0")) continue;
            return srcItem;
        }
        return null;
    }

    private List<SnapshotItem> findSectionChildItems(SDISnapshotItem sectionSnapshotItem) throws SapphireException {
        if (sectionSnapshotItem != null) {
            String worksheetitemlink = "LV_WorksheetItem;Worksheet Section";
            return sectionSnapshotItem.getLinkItemsByLinkId(SnapshotItem.LinkType.REVFK, worksheetitemlink);
        }
        return new ArrayList<SnapshotItem>();
    }

    private int processSection(ArrayList<SDISnapshotViewer.NodeItem> childItems, int i, int processingsectionlevel, SDISnapshotItem srcSection, List<SnapshotItem> srcSections, List<SnapshotItem> refSections, DataSet nodeInfo) throws Exception {
        String currentsectionlevel = this.getPrimaryValue(srcSection.getSDIData(), "sectionlevel");
        String currentSectionVersion = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionversionid");
        if (currentsectionlevel.equals("" + processingsectionlevel)) {
            SDISnapshotItem nextSection;
            SDIData nextSectionSDIData;
            String nextSectionLevel;
            String currentsectionid = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionid");
            String currentsectiondesc = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectiondesc");
            if (currentsectiondesc.length() == 0) {
                currentsectiondesc = currentsectionid;
            }
            String label = SDISnapshotViewer.NodeItem.getNodeLabel(currentsectionid, nodeInfo);
            SDISnapshotViewer.NodeItem sectionNode = new SDISnapshotViewer.NodeItem(currentsectionid, label, false);
            SDISnapshotItem refSection = this.findSectionSnapshot(currentsectionid, refSections);
            ArrayList<SnapshotItem> srcSectionChildItems = new ArrayList();
            if (srcSection != null) {
                srcSectionChildItems = this.findSectionChildItems(srcSection);
            }
            ArrayList<SnapshotItem> refSectionChildItems = new ArrayList<SnapshotItem>();
            if (refSection != null) {
                this.findSectionChildItems(refSection);
            }
            ArrayList<SDISnapshotViewer.NodeItem> allChildren = new ArrayList<SDISnapshotViewer.NodeItem>();
            allChildren.addAll(this.processChildItems(srcSectionChildItems, refSectionChildItems, nodeInfo));
            int next = i + 1;
            ArrayList<SDISnapshotViewer.NodeItem> childSectionNodes = new ArrayList<SDISnapshotViewer.NodeItem>();
            while (next < srcSections.size() && !(nextSectionLevel = this.getPrimaryValue(nextSectionSDIData = (nextSection = (SDISnapshotItem)srcSections.get(next)).getSDIData(), "sectionlevel")).equals("" + processingsectionlevel) && nextSectionLevel.equals("" + (processingsectionlevel + 1))) {
                next = this.processSection(childSectionNodes, next, processingsectionlevel + 1, nextSection, srcSections, refSections, nodeInfo);
            }
            allChildren.addAll(childSectionNodes);
            sectionNode.addChildItems(allChildren);
            childItems.add(sectionNode);
            return next;
        }
        return i + 1;
    }

    private int renderSection(ConfigReportContent cumulativeSectionContent, int i, int processingsectionlevel, SDISnapshotItem srcSection, List<SnapshotItem> srcSections, List<SnapshotItem> refSections, boolean showAuditColumns, boolean hideEmptyColumns) throws SapphireException {
        String currentsectionid = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionid");
        String currentsectiondesc = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectiondesc");
        String currentSectionVersion = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionversionid");
        String currentsectionlevel = this.getPrimaryValue(srcSection.getSDIData(), "sectionlevel");
        if (currentsectionlevel.equals("" + processingsectionlevel)) {
            SDISnapshotItem refSection;
            if (currentsectiondesc.length() == 0) {
                currentsectiondesc = currentsectionid;
            }
            if ((refSection = this.findSectionSnapshot(currentsectionid, refSections)) == null) {
                cumulativeSectionContent.appendNodeContent(this.renderPrimary(srcSection, showAuditColumns, true, hideEmptyColumns, false, "New", false), currentsectionid, currentsectiondesc, "New");
            } else {
                cumulativeSectionContent.appendNodeContent(this.renderPrimaryDiff(srcSection, refSection, showAuditColumns, true, hideEmptyColumns, false, false, false), currentsectionid, currentsectiondesc);
            }
            if (currentsectionlevel.equals("" + processingsectionlevel)) {
                SDISnapshotItem nextSection;
                SDIData nextSectionSDIData;
                String nextSectionLevel;
                ConfigReportContent sectionNode = new ConfigReportContent(currentsectionid, this.translationProcessor);
                ArrayList<SnapshotItem> sectionChildItems = new ArrayList();
                if (srcSection != null) {
                    sectionChildItems = this.findSectionChildItems(srcSection);
                }
                ArrayList<SnapshotItem> refChildItems = new ArrayList();
                if (refSection != null) {
                    refChildItems = this.findSectionChildItems(refSection);
                }
                sectionNode.appendNodeContent(this.renderChildItems(sectionChildItems, refChildItems, showAuditColumns, hideEmptyColumns), currentsectionid, currentsectiondesc);
                int next = i + 1;
                ConfigReportContent childSectionNodes = new ConfigReportContent("childsections", this.translationProcessor);
                while (next < srcSections.size() && !(nextSectionLevel = this.getPrimaryValue(nextSectionSDIData = (nextSection = (SDISnapshotItem)srcSections.get(next)).getSDIData(), "sectionlevel")).equals("" + processingsectionlevel) && nextSectionLevel.equals("" + (processingsectionlevel + 1))) {
                    next = this.renderSection(childSectionNodes, next, processingsectionlevel + 1, nextSection, srcSections, refSections, showAuditColumns, hideEmptyColumns);
                }
                sectionNode.appendNodeContent(childSectionNodes, currentsectionid, currentsectiondesc);
                cumulativeSectionContent.appendSpecialContent(sectionNode);
                return next;
            }
        }
        return i + 1;
    }

    private ConfigReportContent renderWorksheetSection(SDISnapshotItem srcSection, List<SnapshotItem> srcSections, List<SnapshotItem> refSections, boolean showAuditColumns, boolean hideEmptyColumns) throws SapphireException {
        SDISnapshotItem nextSection;
        SDIData nextSectionSDIData;
        String nextSectionLevel;
        ConfigReportContent content = new ConfigReportContent("Section", this.translationProcessor);
        String currentsectionid = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionid");
        String currentsectiondesc = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectiondesc");
        String currentSectionVersion = this.getPrimaryValue(srcSection.getSDIData(), "worksheetsectionversionid");
        if (currentsectiondesc.length() == 0) {
            currentsectiondesc = currentsectionid;
        }
        SDISnapshotItem refSection = this.findSectionSnapshot(currentsectionid, refSections);
        ConfigReportContent primary = new ConfigReportContent("primary", this.translationProcessor);
        primary = refSection == null ? this.renderPrimary(srcSection, showAuditColumns, true, hideEmptyColumns, false, "New", true) : this.renderPrimaryDiff(srcSection, refSection, showAuditColumns, true, hideEmptyColumns, false, false, true);
        ConfigReportContent sectionChildItemsContent = new ConfigReportContent(currentsectionid, this.translationProcessor);
        ArrayList<SnapshotItem> sectionChildItems = new ArrayList();
        if (srcSection != null) {
            sectionChildItems = this.findSectionChildItems(srcSection);
        }
        ArrayList<SnapshotItem> refChildItems = new ArrayList();
        if (refSection != null) {
            refChildItems = this.findSectionChildItems(refSection);
        }
        sectionChildItemsContent.appendSpecialContent(this.renderChildItems(sectionChildItems, refChildItems, showAuditColumns, hideEmptyColumns));
        int processingsectionlevel = 0;
        int next = 1;
        ConfigReportContent childSectionNodes = new ConfigReportContent("childsections", this.translationProcessor);
        while (next < srcSections.size() && !(nextSectionLevel = this.getPrimaryValue(nextSectionSDIData = (nextSection = (SDISnapshotItem)srcSections.get(next)).getSDIData(), "sectionlevel")).equals("" + processingsectionlevel) && nextSectionLevel.equals("" + (processingsectionlevel + 1))) {
            next = this.renderSection(childSectionNodes, next, processingsectionlevel + 1, nextSection, srcSections, refSections, showAuditColumns, hideEmptyColumns);
        }
        sectionChildItemsContent.appendSpecialContent(childSectionNodes);
        primary.appendSpecialContent(sectionChildItemsContent);
        content.appendNodeContent(primary, currentsectionid, currentsectiondesc);
        return content;
    }

    private SnapshotItem findChildItem(String childitemid, List<SnapshotItem> childItems) throws SapphireException {
        for (int i = 0; i < childItems.size(); ++i) {
            String currentitemid = this.getPrimaryValue(childItems.get(i).getSDIData(), "worksheetitemid");
            if (!childitemid.equals(currentitemid)) continue;
            return childItems.get(i);
        }
        return null;
    }

    private ArrayList<SDISnapshotViewer.NodeItem> processChildItems(List<SnapshotItem> srcSectionChildItems, List<SnapshotItem> refSectionChildItems, DataSet nodeInfo) throws SapphireException {
        SDISnapshotViewer.NodeItem n;
        String label;
        String itemname;
        String currentitemid;
        int c;
        ArrayList<SDISnapshotViewer.NodeItem> secChildItemNodes = new ArrayList<SDISnapshotViewer.NodeItem>();
        if (srcSectionChildItems != null) {
            for (c = 0; c < srcSectionChildItems.size(); ++c) {
                currentitemid = this.getPrimaryValue(srcSectionChildItems.get(c).getSDIData(), "worksheetitemid");
                itemname = this.getPrimaryValue(srcSectionChildItems.get(c).getSDIData(), "worksheetitemdesc");
                if (itemname.length() == 0) {
                    itemname = currentitemid;
                }
                if ((label = SDISnapshotViewer.NodeItem.getNodeLabel(currentitemid, nodeInfo)).length() == 0) {
                    label = itemname;
                }
                n = new SDISnapshotViewer.NodeItem(currentitemid, label, false);
                secChildItemNodes.add(n);
            }
        }
        if (refSectionChildItems != null && srcSectionChildItems != null) {
            for (c = 0; c < refSectionChildItems.size(); ++c) {
                currentitemid = this.getPrimaryValue(refSectionChildItems.get(c).getSDIData(), "worksheetitemid");
                itemname = this.getPrimaryValue(refSectionChildItems.get(c).getSDIData(), "worksheetitemdesc");
                if (itemname.length() == 0) {
                    itemname = currentitemid;
                }
                if (this.findChildItem(currentitemid, srcSectionChildItems) != null) continue;
                label = SDISnapshotViewer.NodeItem.getNodeLabel(currentitemid, nodeInfo);
                if (label.length() == 0) {
                    label = itemname;
                }
                n = new SDISnapshotViewer.NodeItem(currentitemid, label, false);
                secChildItemNodes.add(n);
            }
        }
        return secChildItemNodes;
    }

    private ConfigReportContent renderChildItems(List<SnapshotItem> srcSectionChildItems, List<SnapshotItem> refSectionChildItems, boolean showAuditColumns, boolean hideEmptyColumns) throws SapphireException {
        String currentitemid;
        int c;
        ConfigReportContent secChildItemNodes = new ConfigReportContent("worksheetItems", this.translationProcessor);
        if (srcSectionChildItems != null) {
            for (c = 0; c < srcSectionChildItems.size(); ++c) {
                SDISnapshotItem srcSectionChildItem = (SDISnapshotItem)srcSectionChildItems.get(c);
                currentitemid = this.getPrimaryValue(srcSectionChildItem.getSDIData(), "worksheetitemid");
                SDISnapshotItem refSectionChildItem = this.findSectionSnapshotItem(currentitemid, refSectionChildItems);
                secChildItemNodes.appendSpecialContent(this.renderChildItem(srcSectionChildItem, refSectionChildItem, showAuditColumns, hideEmptyColumns));
            }
        }
        if (refSectionChildItems != null) {
            for (c = 0; c < refSectionChildItems.size(); ++c) {
                SDISnapshotItem refSectionChildItem = (SDISnapshotItem)refSectionChildItems.get(c);
                currentitemid = this.getPrimaryValue(refSectionChildItem.getSDIData(), "worksheetitemid");
                if (srcSectionChildItems != null) {
                    SDISnapshotItem srcSectionChildItem = this.findSectionSnapshotItem(currentitemid, srcSectionChildItems);
                    if (srcSectionChildItem != null) continue;
                    secChildItemNodes.appendSpecialContent(this.renderChildItem(srcSectionChildItem, refSectionChildItem, showAuditColumns, hideEmptyColumns));
                    continue;
                }
                secChildItemNodes.appendSpecialContent(this.renderChildItem(null, refSectionChildItem, showAuditColumns, hideEmptyColumns));
            }
        }
        return secChildItemNodes;
    }

    private SDISnapshotItem findSectionSnapshot(String sectionid, List<SnapshotItem> snapshotSections) throws SapphireException {
        if (snapshotSections != null) {
            for (int i = 0; i < snapshotSections.size(); ++i) {
                SDISnapshotItem current = (SDISnapshotItem)snapshotSections.get(i);
                String currentitemid = this.getPrimaryValue(current.getSDIData(), "worksheetsectionid");
                if (!currentitemid.equals(sectionid)) continue;
                return current;
            }
        }
        return null;
    }

    private SDISnapshotItem findSectionSnapshotItem(String itemid, List<SnapshotItem> snapshotItems) throws SapphireException {
        if (snapshotItems != null) {
            for (int i = 0; i < snapshotItems.size(); ++i) {
                SDISnapshotItem current = (SDISnapshotItem)snapshotItems.get(i);
                String currentitemid = this.getPrimaryValue(current.getSDIData(), "worksheetitemid");
                if (!currentitemid.equals(itemid)) continue;
                return current;
            }
        }
        return null;
    }

    private ConfigReportContent renderChildItem(SDISnapshotItem srcsnapshotitem, SDISnapshotItem refsnapshotitem, boolean showAuditColumns, boolean hideEmptyColumns) throws SapphireException {
        if (srcsnapshotitem == null) {
            SDIData refsnapshotitemSDIData = refsnapshotitem.getSDIData();
            ConfigReportContent primary = this.renderPrimary(refsnapshotitem, showAuditColumns, true, hideEmptyColumns, false, "Deleted", true);
            ConfigReportContent allsubsections = new ConfigReportContent("itemcontents", this.translationProcessor);
            allsubsections.appendNodeContent(primary, this.getPrimaryValue(refsnapshotitemSDIData, "worksheetitemid"), this.getPrimaryValue(refsnapshotitemSDIData, "worksheetitemdesc"), "Deleted");
            return allsubsections;
        }
        if (refsnapshotitem == null) {
            SDIData srcsnapshotitemSDIData = srcsnapshotitem.getSDIData();
            ConfigReportContent primary = this.renderPrimary(srcsnapshotitem, showAuditColumns, true, hideEmptyColumns, false, "New", true);
            ConfigReportContent allsubsections = new ConfigReportContent("itemcontents", this.translationProcessor);
            allsubsections.appendNodeContent(primary, this.getPrimaryValue(srcsnapshotitemSDIData, "worksheetitemid"), this.getPrimaryValue(srcsnapshotitemSDIData, "worksheetitemdesc"), "New");
            return allsubsections;
        }
        SDIData srcsnapshotitemSDIData = srcsnapshotitem.getSDIData();
        SDIData refsnapshotitemSDIData = refsnapshotitem.getSDIData();
        ConfigReportContent primary = this.renderPrimaryDiff(srcsnapshotitem, refsnapshotitem, showAuditColumns, true, hideEmptyColumns, false, false);
        ConfigReportContent allsubsections = new ConfigReportContent("itemcontents", this.translationProcessor);
        allsubsections.appendNodeContent(primary, this.getPrimaryValue(srcsnapshotitemSDIData, "worksheetitemid"), this.getPrimaryValue(refsnapshotitemSDIData, "worksheetitemdesc"));
        return allsubsections;
    }
}

