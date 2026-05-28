/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import java.util.ArrayList;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TreeNodeRenderer {
    public static PropertyList getRootNodePropertyList(TreeNode node) {
        PropertyList nPL = new PropertyList();
        String img = node.getNodeimage() != null && node.getNodeimage().length() > 0 ? node.getNodeimage() : "WEB-CORE/pagetypes/adhocbrowser/images/sdc.gif";
        nPL.setProperty("image", img);
        nPL.setProperty("label", node.getNodelabel() + node.getAdditionalLabelHtml());
        PropertyListCollection childnodes = TreeNodeRenderer.getChildNodesCollection(node.getChildNodes());
        nPL.setProperty("childnodes", childnodes);
        return nPL;
    }

    public static PropertyListCollection getChildNodesCollection(ArrayList nodes) {
        PropertyListCollection out = new PropertyListCollection();
        if (nodes != null && nodes.size() > 0) {
            for (int i = 0; i < nodes.size(); ++i) {
                TreeNode treeNode = (TreeNode)nodes.get(i);
                if (i == nodes.size() - 1) {
                    treeNode.setLastNode(true);
                }
                out.add(TreeNodeRenderer.getNodePropertyList(treeNode));
            }
        }
        return out;
    }

    private static PropertyList getNodePropertyList(TreeNode treeNode) {
        PropertyList out = new PropertyList();
        String nodeid = treeNode.getNodeid();
        String nodeimage = treeNode.getNodeimage();
        String nodeimage2 = treeNode.getNodeimage2();
        String nodelabel = treeNode.getNodelabel();
        String nodelabeltrans = treeNode.getNodelabeltrans();
        out.setProperty("nodeid", nodeid);
        out.setProperty("candrilldown", treeNode.isCanDrillDown() ? "Y" : "N");
        out.setProperty("image", nodeimage);
        if (nodeimage2 != null && nodeimage2.length() > 0) {
            out.setProperty("image2", nodeimage2);
        }
        out.setProperty("isdragable", treeNode.isDragable() ? "Y" : "N");
        out.setProperty("iscriteriaonly", treeNode.isCriteriaOnly() ? "Y" : "N");
        out.setProperty("label", nodelabel);
        out.setProperty("labeltrans", nodelabeltrans);
        out.setProperty("columntitle", treeNode.getColumntitle());
        out.setProperty("columnid", nodeid);
        out.setProperty("labelhtml", treeNode.getLabelHtml());
        out.setProperty("columntype", treeNode.getColumntype());
        if (treeNode.getChildNodes() != null && treeNode.getChildNodes().size() > 0) {
            out.setProperty("childnodes", TreeNodeRenderer.getChildNodesCollection(treeNode.getChildNodes()));
        }
        return out;
    }
}

