/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.PropertyTreeRO;
import com.labvantage.sapphire.modules.configreport.util.PropertyTreeUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.Set;
import org.json.JSONArray;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PropertyTreeSnapshotViewer
extends SDISnapshotViewer {
    public PropertyTreeSnapshotViewer() {
    }

    public PropertyTreeSnapshotViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection) {
        super.initialize(sapphireConnection);
    }

    public static String getHtml(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot) throws SapphireException {
        return PropertyTreeSnapshotViewer.getHtml(sapphireConnection, srcSDISnapshot, true, true, false, true);
    }

    public static String getHtml(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, boolean showAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        return PropertyTreeSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, srcSDISnapshot, showAuditColumns, hideEmptyColumns, hideInheritedProperties);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot) throws SapphireException {
        return PropertyTreeSnapshotViewer.hasDiff(sapphireConnection, srcSDISnapshot, refSDISnapshot, false);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot, boolean includeAuditColumns) throws SapphireException {
        PropertyTreeSnapshotViewer ssViewer = new PropertyTreeSnapshotViewer(sapphireConnection);
        return ssViewer.hasDiff(srcSDISnapshot, refSDISnapshot, includeAuditColumns);
    }

    public boolean hasDiff(PropertyTreeSnapshot srcSnapshot, PropertyTreeSnapshot refSnapshot, boolean showAuditColumns) throws SapphireException {
        boolean hasDiff = false;
        try {
            hasDiff = true;
        }
        catch (Throwable t) {
            Trace.logError("Server Error", t);
        }
        return hasDiff;
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot) throws SapphireException {
        return PropertyTreeSnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, refSDISnapshot, true, true, false);
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, PropertyTreeSnapshot srcSDISnapshot, PropertyTreeSnapshot refSDISnapshot, boolean showAuditColumns, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        PropertyTreeSnapshotViewer ssViewer = new PropertyTreeSnapshotViewer(sapphireConnection);
        ConfigReportContent snapshotContent = ssViewer.getHtml(srcSDISnapshot, refSDISnapshot, showAuditColumns, hideInheritedProperties, hideEmptyColumns);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(snapshotContent);
        return stringBuilder.toString();
    }

    public ConfigReportContent getHtml(PropertyTreeSnapshot srcSnapshot, PropertyTreeSnapshot refSnapshot, boolean showAuditColumns, boolean hideInheritedProperties, boolean hideEmptyColumns) throws SapphireException {
        if (srcSnapshot == null) {
            throw new SapphireException("Source snapshots is null");
        }
        ConfigReportContent out = new ConfigReportContent("PropertyTree:" + srcSnapshot.getPropertyTree().getPropertyTreeId(), this.translationProcessor);
        String nodeId = srcSnapshot.getSnapshotItem().getNodeId();
        try {
            if (nodeId.equals("__DEFINITION")) {
                SDIData refSDIData;
                SDIData srcSDIData = srcSnapshot.getSDIData();
                SDIData sDIData = refSDIData = refSnapshot == null ? new SDIData() : refSnapshot.getSDIData();
                if (srcSDIData != null && refSDIData != null) {
                    out.append("\t<div style=\"display:block;width:100%;height:95%;position:absolute;overflow:auto;\">\n");
                    out.append("<table><tr><td>");
                    PropertyTreeRO sdcRO = new PropertyTreeRO();
                    sdcRO.initialize("PropertyTree", this.sapphireConnection);
                    sdcRO.setCurrentSDIData(srcSnapshot.getSDIData());
                    PropertyTreeRO refSdcRO = new PropertyTreeRO();
                    refSdcRO.initialize("PropertyTree", this.sapphireConnection);
                    if (refSnapshot != null) {
                        refSdcRO.setCurrentSDIData(refSnapshot.getSDIData());
                    }
                    PropertyTreeUtil util = new PropertyTreeUtil();
                    util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
                    out.startSDISectionDiff(sdcRO.currentSDI, "", refSdcRO.getDescription());
                    out.startSubSection("PropertyTree Info", "");
                    out.appendSubSection(util.renderPropertyTreeInfoDiff(sdcRO, refSdcRO), "PropertyTree Info", false);
                    out.startSubSection("Property Definition", "");
                    DataSet srcprimary = srcSDIData.getDataset("primary");
                    String srcDefinitionTree = srcprimary.getValue(0, "definitiontree");
                    DataSet refprimary = refSDIData.getDataset("primary");
                    String refDefinitionTree = refprimary.getValue(0, "definitiontree");
                    PropertyTree srcTree = new PropertyTree();
                    srcTree.setDefinitionXML(srcDefinitionTree);
                    PropertyTree refTree = new PropertyTree();
                    refTree.setDefinitionXML(refDefinitionTree);
                    ConfigReportContent pd = out.renderPropertyDefinitionList("root", "", srcTree.getPropertyDefinitionList(), refTree.getPropertyDefinitionList(), this.translationProcessor, false);
                    out.appendSpecialContent(pd);
                    ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
                    this.renderCategores(str, srcSnapshot.getSnapshotItem(), refSnapshot == null ? null : refSnapshot.getSnapshotItem(), showAuditColumns, true, hideEmptyColumns);
                    out.appendSpecialContent(str);
                    out.append("</td></tr></table>");
                    out.append("</div>");
                }
            } else if (!nodeId.equals("__FULL")) {
                ConfigReportContent pldiff;
                PropertyTree srcPropertyTree = srcSnapshot.getPropertyTree();
                PropertyTree refPropertyTree = refSnapshot == null ? null : refSnapshot.getPropertyTree();
                String nodeid = srcSnapshot.getSnapshotItem().getNodeId();
                out.append("\t<div style=\"display:block;width:100%;height:95%;position:absolute;overflow:auto;\">\n");
                out.append("<table><tr><td>");
                out.startSDISection(PropertyTreeSnapshotViewer.getSDI(srcSnapshot.getSDIData()), "Node " + nodeid + " Properties");
                WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.sapphireConnection.getConnectionId());
                PropertyTree base = webAdminProcessor.getPropertyTree(srcSnapshot.getKeyId1());
                PropertyList srcNodePropertyList = null;
                srcNodePropertyList = nodeid.equals("__root") ? this.convertToPropertyList(srcPropertyTree.getPropertyDefaultList()) : srcPropertyTree.getNodePropertyList(nodeid, true);
                PropertyList refNodePropertyList = null;
                refNodePropertyList = nodeid.equals("__root") ? this.convertToPropertyList(refPropertyTree.getPropertyDefaultList()) : (refPropertyTree != null ? refPropertyTree.getNodePropertyList(nodeid, true) : new PropertyList());
                PropertyDefinitionList pd = null;
                if (srcPropertyTree.getPropertyTreeId() != null) {
                    pd = this.getPropertyDefinitionTree(this.queryProcessor, base.getPropertyTreeId());
                }
                if (pd == null && base != null && base.getPropertyTreeId() != null) {
                    pd = this.getPropertyDefinitionTree(this.queryProcessor, base.getPropertyTreeId());
                    hideInheritedProperties = true;
                }
                if (pd != null) {
                    pldiff = out.renderPropertyListDiff(nodeid, true, hideInheritedProperties, srcNodePropertyList, refNodePropertyList, pd, true, true, this.translationProcessor, hideEmptyColumns);
                    out.appendSpecialContent(pldiff);
                } else {
                    pldiff = out.renderPropertyListDiff(srcNodePropertyList, refNodePropertyList, true, this.translationProcessor);
                    out.appendSpecialContent(pldiff);
                }
                out.append("</td></tr></table>");
                Node srcNode = srcPropertyTree == null ? null : srcPropertyTree.getNode(nodeId);
                Node refNode = refPropertyTree == null ? null : refPropertyTree.getNode(nodeId);
                String[] srcCategories = srcNode == null ? null : StringUtil.split(srcNode.getCategoryList(), ";");
                String[] refCategories = refNode == null ? null : StringUtil.split(refNode.getCategoryList(), ";");
                DataSet sC = new DataSet();
                sC.addColumn("category", 0);
                if (srcCategories != null) {
                    for (int i = 0; i < srcCategories.length; ++i) {
                        sC.addRow();
                        sC.setString(i, "category", srcCategories[i]);
                    }
                }
                DataSet rC = new DataSet();
                if (refCategories != null) {
                    rC.addColumn("category", 0);
                    for (int i = 0; i < refCategories.length; ++i) {
                        rC.addRow();
                        rC.setString(i, "category", refCategories[i]);
                    }
                }
                out.renderDiffListTable(sC, rC, new String[]{"category"});
                out.append("</div>");
            } else {
                ConfigReportContent configReportContent = new ConfigReportContent("PropertyTree", this.translationProcessor);
                configReportContent.setFoundDiff(false);
                PropertyTreeRO sdcRO = new PropertyTreeRO();
                sdcRO.initialize("PropertyTree", this.sapphireConnection);
                sdcRO.setCurrentSDIData(srcSnapshot.getSDIData());
                PropertyTreeRO refSdcRO = new PropertyTreeRO();
                refSdcRO.initialize("PropertyTree", this.sapphireConnection);
                if (refSnapshot != null) {
                    refSdcRO.setCurrentSDIData(refSnapshot.getSDIData());
                }
                PropertyTreeUtil util = new PropertyTreeUtil();
                util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
                boolean diffOnly = false;
                configReportContent.append("<table><tr><td>");
                configReportContent.startSDISectionDiff(sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
                configReportContent.startSubSection("PropertyTree Info", "");
                configReportContent.appendSubSection(util.renderPropertyTreeInfoDiff(sdcRO, refSdcRO), "PropertyTree Info", diffOnly);
                configReportContent.appendNodeContent(util.renderNodeHierarchyDiff(sdcRO, refSdcRO, hideInheritedProperties, false), "NodeList", "Node List");
                configReportContent.append("</td></tr></table>");
                out.append("\n<div style=\"display:none\" id=\"root\">" + configReportContent.toString() + "</div>");
                JSONArray rootArray = new JSONArray();
                SDISnapshotViewer.NodeItem nodeItem = util.rootNodeItem;
                rootArray.put(nodeItem);
                out.append("\n<script>");
                out.append("\nvar initialContextData=" + rootArray.toString(4) + ";");
                out.append("\nvar mode=\"snapshotview\"");
                out.append("\nvar navigator_props=" + new PropertyList().toJSONString(false, false) + ";");
                out.append("\nsapphire.gwt.addGWTElement('navigator','navigator', navigator_props );");
                out.append("\n</script>");
            }
        }
        catch (Throwable t) {
            Trace.logError("Server Error", t);
            out.append("\n<P>Server Error:" + t.getMessage());
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

    private PropertyList convertToPropertyList(PropertyDefaultList defaultList) {
        PropertyList ret = new PropertyList();
        if (defaultList != null) {
            Set keys = defaultList.keySet();
            for (String key : keys) {
                PropertyDefault propertyDefault = defaultList.getPropertyDefault(key);
                if (propertyDefault.getType().equals("simple")) {
                    ret.setProperty(key, propertyDefault.getValue());
                    continue;
                }
                ret.setProperty(key, this.convertToPropertyList(propertyDefault.getPropertyDefaultList()));
            }
        }
        return ret;
    }
}

