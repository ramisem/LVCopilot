/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.PropertyTreeRO;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.json.JSONException;
import org.w3c.dom.Element;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class PropertyTreeUtil
extends BaseSDCRenderer {
    public HashMap nodeHtmlMap = new HashMap();
    public SDISnapshotViewer.NodeItem rootNodeItem;

    public ConfigReportContent drawNodeTree(PropertyTreeRO ro, org.w3c.dom.Node node, int indent, String status) throws Exception {
        ConfigReportContent output = new ConfigReportContent(this.config, "PropertyTree node tree:");
        String label = ro.getPropertyTreeId();
        output.append("<P>Root");
        if ("N".equals(status)) {
            label = ConfigReportContent.getNewString(label);
        } else if ("D".equals(status)) {
            label = ConfigReportContent.getDeletedString(label);
        }
        output.append(this.drawSubNodeTree(node, indent + 1, status, ro).toString());
        return output;
    }

    public ConfigReportContent drawSubNodeTree(org.w3c.dom.Node node, int indent, String status, PropertyTreeRO sdcRO) throws TransformerException {
        ConfigReportContent output = new ConfigReportContent(this.config, "PropertyTree node subtree:");
        String parentnodeid = ((Element)node).getAttribute("id");
        boolean isParentProductOrComponent = parentnodeid.endsWith(" Product") || parentnodeid.contains(" Comp ");
        ArrayList subnodes = sdcRO.getSubNodes(node);
        if (subnodes.size() > 0) {
            for (int i = 0; i < subnodes.size(); ++i) {
                Element childnode = (Element)subnodes.get(i);
                String nodeid = childnode.getAttribute("id");
                boolean locked = "Y".equals(childnode.getAttribute("locked"));
                output.append("<P>");
                for (int j = 0; j < indent; ++j) {
                    if (j == indent - 1) {
                        output.append("|________");
                        continue;
                    }
                    output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                String label = PropertyTreeUtil.getNodeLabel(nodeid, locked, isParentProductOrComponent);
                if (this.checkNodeNotEmpty(nodeid)) {
                    label = ConfigReportContent.createHyperLink(label, nodeid);
                }
                String nodeAnchor = ConfigReportContent.generateNodeAnchor(nodeid);
                label = "<A id=\"" + nodeAnchor + "\"></A>" + label;
                output.append(label);
                output.append(this.drawSubNodeTree(childnode, indent + 1, "S", sdcRO).toString());
            }
        }
        return output;
    }

    public String drawNodeTreeDiff(PropertyTreeRO srcRO, org.w3c.dom.Node srcNode, org.w3c.dom.Node refNode, int indent) throws Exception {
        StringBuffer output = new StringBuffer();
        String uniqueid = srcRO.getPropertyTreeId();
        output.append("<P>Root");
        output.append(this.drawSubNodeTreeDiff(uniqueid, srcNode, refNode, indent + 1));
        output.append("</table>");
        return output.toString();
    }

    public SDISnapshotViewer.NodeItem createDiffNodeTree(PropertyTreeRO srcRO, org.w3c.dom.Node srcNode, org.w3c.dom.Node refNode, int indent) throws Exception {
        SDISnapshotViewer.NodeItem rootNodeItem = new SDISnapshotViewer.NodeItem("root", "Root", true);
        this.createSubNodeTreeDiff(rootNodeItem, srcNode, refNode, indent + 1);
        return rootNodeItem;
    }

    public static String getNodeLabel(String nodeid, boolean locked, boolean isParentProductOrComponent) {
        String label = nodeid;
        if (isParentProductOrComponent && (nodeid.endsWith(" Custom") || nodeid.endsWith(" ImplCustom"))) {
            label = "(Custom)";
        } else if (locked && nodeid.endsWith(" Product")) {
            label = nodeid.substring(0, label.length() - 8) + " (P)";
        } else if (locked && nodeid.endsWith(" Impl")) {
            label = nodeid.substring(0, label.length() - 5) + " (I)";
        }
        return label;
    }

    public ConfigReportContent renderNodeHierarchy(PropertyTreeRO propertyTreeRO, String status) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "PropertyTree node hierarchy:");
        try {
            org.w3c.dom.Node node = propertyTreeRO.getNodeTree();
            if (node == null) {
                configReportContent.append("<P>No node tree defined.");
                return configReportContent;
            }
            ArrayList allnodes = DOMUtil.getAllNodes(node);
            PropertyTree tree = propertyTreeRO.getPropertyTree();
            if (tree == null || tree.getNodeList() == null || tree.getNodeList().size() == 0) {
                configReportContent.append("<P>The tree has no nodes.");
                return configReportContent;
            }
            ConfigReportContent detailsbuffer = new ConfigReportContent(this.config, "PropertyTree details:");
            for (int i = 0; i < allnodes.size(); ++i) {
                String nodename = allnodes.get(i).toString();
                Node currnode = tree.getNode(nodename);
                PropertyList pl = currnode.getPropertyList();
                ConfigReportContent ret = configReportContent.renderPropertyList(pl, tree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor());
                if (ret.length() <= 0) continue;
                if (nodename.endsWith("Product")) {
                    nodename.replace("Product", "(P)");
                }
                String nodeAnchor = ConfigReportContent.generateNodeAnchor(nodename);
                detailsbuffer.startSubHeading(nodename + " Details", "", ConfigReportContent.generateSectionAnchor(nodename));
                detailsbuffer.append("<A HREF=#" + nodeAnchor + ">Back To Node Hierarchy </A>");
                detailsbuffer.append(ret.toString());
                this.nodeHtmlMap.put(currnode.getId(), ret.toString());
            }
            ConfigReportContent treecontent = this.drawNodeTree(propertyTreeRO, node, 0, status);
            configReportContent.append(treecontent.toString());
            configReportContent.append(detailsbuffer.toString());
        }
        catch (Exception e) {
            configReportContent.append("Error:" + e.getMessage());
            throw new SapphireException("Failed to draw the node tree: ", e);
        }
        return configReportContent;
    }

    public ConfigReportContent renderNodeHierarchyDiff(PropertyTreeRO propertyTreeRO, PropertyTreeRO refPropertyTreeRO, boolean hideInheritedProperties, boolean isconfigreport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "PropertyTree node tree:");
        try {
            String nodeAnchor;
            ConfigReportContent ret;
            String nodename;
            int i;
            org.w3c.dom.Node srcNode = propertyTreeRO.getNodeTree();
            org.w3c.dom.Node refNode = refPropertyTreeRO.getNodeTree();
            PropertyTree srcpropertytree = propertyTreeRO.getPropertyTree();
            PropertyTree refpropertytree = refPropertyTreeRO.getPropertyTree();
            if (refpropertytree == null) {
                refpropertytree = new PropertyTree();
            }
            ConfigReportContent detailsBuffer = new ConfigReportContent(this.config, "PropertyTree node tree details:");
            ArrayList allsrcnodes = srcpropertytree.getAllNodes();
            ArrayList allrefnodes = refpropertytree.getAllNodes();
            this.nodeHtmlMap = new HashMap();
            for (i = 0; i < allsrcnodes.size(); ++i) {
                PropertyList srcpl;
                nodename = allsrcnodes.get(i).toString();
                Node currPropertyTreeNode = srcpropertytree.getNode(nodename);
                Node refpropertytreeNode = refpropertytree.getNode(nodename);
                if (refpropertytreeNode == null) {
                    srcpl = srcpropertytree.getNodePropertyList(nodename, true);
                    if (srcpl == null) {
                        throw new SapphireException("currPropertyTreeNode propertylist is null");
                    }
                    ret = configReportContent.renderPropertyListDiff(nodename, true, hideInheritedProperties, srcpl, new PropertyList(), srcpropertytree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor(), true);
                    if (ret.length() <= 0) continue;
                    if (nodename.endsWith("Product")) {
                        nodename.replace("Product", "(P)");
                    }
                    detailsBuffer.startSubHeading(nodename + " Details", "", ConfigReportContent.generateSectionAnchor(nodename));
                    nodeAnchor = ConfigReportContent.generateNodeAnchor(nodename);
                    detailsBuffer.append("<A HREF=#" + nodeAnchor + ">Back To Node Hierarchy</A>");
                    this.nodeHtmlMap.put(currPropertyTreeNode.getId(), "<H4 id=\"" + nodeAnchor + "\">RETURN</H4>" + ret.toString());
                    detailsBuffer.appendNodeContent(ret, nodename, nodename);
                    continue;
                }
                srcpl = srcpropertytree.getNodePropertyList(nodename, true);
                PropertyList refpl = refpropertytree.getNodePropertyList(nodename, true);
                if (srcpl == null || refpl == null) {
                    throw new SapphireException(" src or ref pl is null ");
                }
                ConfigReportContent ret2 = configReportContent.renderPropertyListDiff(nodename, true, hideInheritedProperties, srcpl, refpl, srcpropertytree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor(), true);
                if (ret2 == null) continue;
                if (nodename.endsWith("Product")) {
                    nodename.replace("Product", "(P)");
                }
                detailsBuffer.startSubHeading(nodename + " Details", "", ConfigReportContent.generateSectionAnchor(nodename));
                String nodeAnchor2 = ConfigReportContent.generateNodeAnchor(nodename);
                detailsBuffer.append("<A HREF=#" + nodeAnchor2 + ">Back To Node Hierarchy </A>");
                this.nodeHtmlMap.put(currPropertyTreeNode.getId(), ret2.toString());
                if (ret2.length() == 0) {
                    ret2.append("No properties");
                }
                detailsBuffer.appendNodeContent(ret2, nodename, nodename);
            }
            for (i = 0; i < allrefnodes.size(); ++i) {
                PropertyList refpl;
                nodename = allrefnodes.get(i).toString();
                Node currnode = refpropertytree.getNode(nodename);
                Node findsrcnode = srcpropertytree.getNode(nodename);
                if (findsrcnode != null || (ret = configReportContent.renderPropertyListDiff(nodename, true, hideInheritedProperties, new PropertyList(), refpl = refpropertytree.getNodePropertyList(nodename, true), refpropertytree.getPropertyDefinitionList(), true, true, this.getTranslationProcessor(), true)).length() <= 0) continue;
                if (nodename.endsWith("Product")) {
                    nodename.replace("Product", "(P)");
                }
                detailsBuffer.startSubHeading(nodename + " Details", "", ConfigReportContent.generateSectionAnchor(nodename));
                detailsBuffer.appendNodeContent(ret, nodename, nodename);
                nodeAnchor = ConfigReportContent.generateNodeAnchor(nodename);
                detailsBuffer.append("<A HREF=#" + nodeAnchor + ">Back To Node Hierarchy </A>");
                this.nodeHtmlMap.put(currnode.getId(), ret.toString());
            }
            this.rootNodeItem = this.createDiffNodeTree(propertyTreeRO, srcNode, refNode, 0);
            if (isconfigreport) {
                configReportContent.append(this.drawNodeTreeDiff(propertyTreeRO, srcNode, refNode, 0));
            }
            configReportContent.appendNodeContent(detailsBuffer, "All Details", "All Details");
        }
        catch (Exception e) {
            configReportContent.append("Error:" + e.getMessage());
            Trace.logError("Failed to draw diff tree", e);
            throw new SapphireException("Failed to draw diff tree:", e);
        }
        return configReportContent;
    }

    public ConfigReportContent renderPropertyTreeInfo(PropertyTreeRO propertyTreeRO) {
        ConfigReportContent info = new ConfigReportContent(this.config, "PropertyTree node tree info:");
        info.startTable();
        info.startRow();
        info.addRowItem("PropertyTree", propertyTreeRO.getPropertyTreeId());
        info.addRowItem("Type", propertyTreeRO.getPropertyTreeType());
        info.endRow();
        info.startRow();
        info.addRowItem("Object Name", propertyTreeRO.getObjectName(), 3);
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderPropertyTreeInfoDiff(PropertyTreeRO sdcRO, PropertyTreeRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "PropertyTree node tree info:");
        PropertyTreeRO propertyTreeRO = sdcRO;
        PropertyTreeRO refPropertyTreeRO = null;
        if (refSdcRO != null) {
            refPropertyTreeRO = refSdcRO;
            refPropertyTreeRO.currentSDI = refSdcRO.currentSDI;
        }
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("PropertyTree", propertyTreeRO.getPropertyTreeId(), refSdcRO == null ? "" : refSdcRO.getPropertyTreeId());
        configReportContent.addDiffRowItem("Type", propertyTreeRO.getPropertyTreeType(), refPropertyTreeRO.getPropertyTreeType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Description", propertyTreeRO.getPropertyTreeDesc(), refSdcRO == null ? "" : refSdcRO.getPropertyTreeDesc(), 3, this.getTranslationProcessor());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Object Name", propertyTreeRO.getObjectName(), refPropertyTreeRO.getObjectName(), 3, false, this.getTranslationProcessor(), false);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public int findNode(String nodeid, ArrayList nodes) {
        for (int i = 0; i < nodes.size(); ++i) {
            String currnodeid = ((Element)nodes.get(i)).getAttribute("id");
            if (!nodeid.equals(currnodeid)) continue;
            return i;
        }
        return -1;
    }

    boolean checkNodeModified(String nodeid) {
        String html;
        return this.nodeHtmlMap.get(nodeid) != null && (html = this.nodeHtmlMap.get(nodeid).toString()).indexOf("diffreport") > -1;
    }

    boolean checkNodeNotEmpty(String nodeid) {
        String html;
        return this.nodeHtmlMap.get(nodeid) != null && (html = this.nodeHtmlMap.get(nodeid).toString()).length() > 0;
    }

    public StringBuffer drawSubNodeTreeDiff(String uniqueid, org.w3c.dom.Node srcNode, org.w3c.dom.Node refNode, int indent) throws TransformerException {
        String label;
        boolean locked;
        String nodeid;
        Element childnode;
        int i;
        StringBuffer output = new StringBuffer();
        String srcParentnodeid = ((Element)srcNode).getAttribute("id");
        boolean isSrcParentProductOrComponent = srcParentnodeid.endsWith(" Product") || srcParentnodeid.contains(" Comp ");
        String refParentnodeid = ((Element)refNode).getAttribute("id");
        boolean isRefParentProductOrComponent = refParentnodeid.endsWith(" Product") || srcParentnodeid.contains(" Comp ");
        ArrayList srcSubnodes = ((PropertyTreeRO)this.sdcRO).getSubNodes(srcNode);
        ArrayList refSubnodes = ((PropertyTreeRO)this.refSdcRO).getSubNodes(refNode);
        if (srcSubnodes.size() > 0) {
            for (i = 0; i < srcSubnodes.size(); ++i) {
                childnode = (Element)srcSubnodes.get(i);
                nodeid = childnode.getAttribute("id");
                int indexInRefSubNodes = this.findNode(nodeid, refSubnodes);
                locked = "Y".equals(childnode.getAttribute("locked"));
                output.append("<P>");
                for (int j = 0; j < indent; ++j) {
                    if (j == indent - 1) {
                        output.append("|________");
                        continue;
                    }
                    output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                label = PropertyTreeUtil.getNodeLabel(nodeid, locked, isSrcParentProductOrComponent);
                if (indexInRefSubNodes == -1) {
                    label = ConfigReportContent.getNewString(label);
                } else if (this.checkNodeModified(nodeid)) {
                    label = ConfigReportContent.getModifiedString(label);
                }
                if (this.checkNodeNotEmpty(nodeid)) {
                    label = ConfigReportContent.createHyperLink(label, nodeid);
                }
                String nodeAnchor = ConfigReportContent.generateNodeAnchor(nodeid);
                label = "<A id=\"" + nodeAnchor + "\"></A>" + label;
                output.append(label);
                if (indexInRefSubNodes == -1) {
                    output.append(this.drawSubNodeTree(childnode, indent + 1, "N", (PropertyTreeRO)this.sdcRO));
                    continue;
                }
                Element refchildnode = (Element)refSubnodes.get(indexInRefSubNodes);
                output.append(this.drawSubNodeTreeDiff(uniqueid, childnode, refchildnode, indent + 1));
            }
        }
        if (refSubnodes.size() > 0) {
            for (i = 0; i < refSubnodes.size(); ++i) {
                childnode = (Element)refSubnodes.get(i);
                nodeid = childnode.getAttribute("id");
                int indexInSrcSubNodes = this.findNode(nodeid, srcSubnodes);
                if (indexInSrcSubNodes != -1) continue;
                locked = "Y".equals(childnode.getAttribute("locked"));
                label = PropertyTreeUtil.getNodeLabel(nodeid, locked, isRefParentProductOrComponent);
                label = ConfigReportContent.getDeletedString(label);
                if (this.checkNodeNotEmpty(nodeid)) {
                    label = ConfigReportContent.createHyperLink(label, nodeid);
                }
                output.append("<P>");
                for (int j = 0; j < indent; ++j) {
                    if (j == indent - 1) {
                        output.append("|________");
                        continue;
                    }
                    output.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                output.append(label);
                output.append(this.drawSubNodeTree(childnode, indent + 1, "D", (PropertyTreeRO)this.refSdcRO));
            }
        }
        return output;
    }

    public SDISnapshotViewer.NodeItem createSubNodeTreeDiff(SDISnapshotViewer.NodeItem parent, org.w3c.dom.Node srcNode, org.w3c.dom.Node refNode, int indent) throws JSONException {
        SDISnapshotViewer.NodeItem childNodeItem;
        String image;
        String label;
        boolean locked;
        String nodeid;
        Element childnode;
        int i;
        String srcParentnodeid = ((Element)srcNode).getAttribute("id");
        boolean isSrcParentProductOrComponent = srcParentnodeid.endsWith(" Product") || srcParentnodeid.contains(" Comp ");
        String refParentnodeid = "";
        if (refNode != null) {
            refParentnodeid = ((Element)refNode).getAttribute("id");
        }
        boolean isRefParentProductOrComponent = refParentnodeid.endsWith(" Product") || srcParentnodeid.contains(" Comp ");
        ArrayList srcSubnodes = ((PropertyTreeRO)this.sdcRO).getSubNodes(srcNode);
        ArrayList refSubnodes = refNode == null ? new ArrayList() : ((PropertyTreeRO)this.refSdcRO).getSubNodes(refNode);
        ArrayList<SDISnapshotViewer.NodeItem> childItemList = new ArrayList<SDISnapshotViewer.NodeItem>();
        if (srcSubnodes.size() > 0) {
            for (i = 0; i < srcSubnodes.size(); ++i) {
                childnode = (Element)srcSubnodes.get(i);
                nodeid = childnode.getAttribute("id");
                int indexInRefSubNodes = this.findNode(nodeid, refSubnodes);
                locked = "Y".equals(childnode.getAttribute("locked"));
                label = PropertyTreeUtil.getNodeLabel(nodeid, locked, isSrcParentProductOrComponent);
                if (indexInRefSubNodes == -1) {
                    label = ConfigReportContent.getNewString(label);
                } else if (this.checkNodeModified(nodeid)) {
                    label = ConfigReportContent.getModifiedString(label);
                }
                if (this.checkNodeNotEmpty(nodeid)) {
                    label = ConfigReportContent.createHyperLink(label, nodeid);
                }
                image = "";
                if (label.contains("diffreportnewitem")) {
                    image = "WEB-CORE/images/png/Add.png";
                } else if (label.contains("diffreportdeleteditem")) {
                    image = "WEB-CORE/images/png/Delete.png";
                } else if (label.contains("diffreportmodifieditem")) {
                    image = "rc?command=image&image=NoteEdit&color=%23FF4300";
                }
                childNodeItem = new SDISnapshotViewer.NodeItem(nodeid, label, false, image);
                if (indexInRefSubNodes == -1) {
                    this.createSubNodeTreeDiff(childNodeItem, childnode, childnode, indent + 1);
                    childItemList.add(childNodeItem);
                    Trace.logDebug("Added " + nodeid + " to " + srcParentnodeid + " childItemList size:" + childItemList.size());
                    continue;
                }
                Element refchildnode = (Element)refSubnodes.get(indexInRefSubNodes);
                this.createSubNodeTreeDiff(childNodeItem, childnode, refchildnode, indent + 1);
                childItemList.add(childNodeItem);
                Trace.log("Added " + nodeid + " to " + srcParentnodeid + " childItemList size:" + childItemList.size());
            }
        }
        if (refSubnodes.size() > 0) {
            for (i = 0; i < refSubnodes.size(); ++i) {
                childnode = (Element)refSubnodes.get(i);
                nodeid = childnode.getAttribute("id");
                int indexInSrcSubNodes = this.findNode(nodeid, srcSubnodes);
                if (indexInSrcSubNodes != -1) continue;
                locked = "Y".equals(childnode.getAttribute("locked"));
                label = PropertyTreeUtil.getNodeLabel(nodeid, locked, isRefParentProductOrComponent);
                label = ConfigReportContent.getDeletedString(label);
                if (this.checkNodeNotEmpty(nodeid)) {
                    label = ConfigReportContent.createHyperLink(label, nodeid);
                }
                image = "";
                if (label.contains("diffreportnewitem")) {
                    image = "WEB-CORE/images/png/Add.png";
                } else if (label.contains("diffreportdeleteditem")) {
                    image = "WEB-CORE/images/png/Delete.png";
                } else if (label.contains("diffreportmodifieditem")) {
                    image = "rc?command=image&image=NoteEdit&color=%23FF4300";
                }
                childNodeItem = new SDISnapshotViewer.NodeItem(nodeid, label, false, image);
                this.createSubNodeTreeDiff(childNodeItem, childnode, childnode, indent + 1);
                childItemList.add(childNodeItem);
            }
        }
        parent.addChildItems(childItemList);
        return parent;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        if (this.sdcRO != null && this.sdcRO.currentSDIData != null) {
            FileOutputStream defTreeFile;
            FileOutputStream valueTreeFile;
            String xmlValueTree = this.sdcRO.getPrimaryValue("valuetree");
            String xmlDefintionTree = this.sdcRO.getPrimaryValue("definitiontree");
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            String xmlValueTreeFileName = xmlSdiFileName.replace(".xml", "_valuetree.xml");
            String xmlDefTreeFileName = xmlSdiFileName.replace(".xml", "_deftree.xml");
            try {
                valueTreeFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlValueTreeFileName);
                defTreeFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlDefTreeFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
            }
            try {
                valueTreeFile.write(xmlValueTree.getBytes());
                valueTreeFile.close();
                defTreeFile.write(xmlDefintionTree.getBytes());
                defTreeFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }
}

