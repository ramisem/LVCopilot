/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.BaseDiff;
import com.labvantage.sapphire.util.diff.NodeDiff;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;

public class NodeListDiff
extends BaseDiff {
    NodeListDiff(PropertyTree sourceTree, PropertyTree targetTree) {
        int i;
        NodeList sourceNodeList = sourceTree.getNodeList();
        NodeList targetNodeList = targetTree.getNodeList();
        if (sourceNodeList == null) {
            sourceNodeList = new NodeList();
        }
        if (targetNodeList == null) {
            targetNodeList = new NodeList();
        }
        for (i = 0; i < sourceNodeList.size(); ++i) {
            this.processSourceNode((Node)sourceNodeList.get(i), targetTree);
        }
        for (i = 0; i < targetNodeList.size(); ++i) {
            this.processTargetNode((Node)targetNodeList.get(i), sourceTree);
        }
    }

    void processSourceNode(Node sourceNode, PropertyTree targetTree) {
        Node match = this.findNodeInTree(sourceNode, targetTree);
        if (match != null) {
            NodeDiff nodeDiffs = new NodeDiff(sourceNode, match);
            this.results = this.addNodeDiffResults(this.results, nodeDiffs.getResults());
        } else {
            this.addNodeDiffResult(sourceNode.getNodeId(), "Deleted", "Node present only in source");
        }
        NodeList childList = sourceNode.getNodeList();
        for (int i = 0; i < childList.size(); ++i) {
            this.processSourceNode((Node)childList.get(i), targetTree);
        }
    }

    void processTargetNode(Node targetNode, PropertyTree sourceTree) {
        Node match = this.findNodeInTree(targetNode, sourceTree);
        if (match == null) {
            this.addNodeDiffResult(targetNode.getNodeId(), "Added", "Node present only in target");
        }
        NodeList childList = targetNode.getNodeList();
        for (int i = 0; i < childList.size(); ++i) {
            this.processTargetNode((Node)childList.get(i), sourceTree);
        }
    }

    Node findNodeInList(Node node, NodeList list) {
        Node curr;
        int i;
        if (list == null) {
            return null;
        }
        Node found = null;
        for (i = 0; i < list.size(); ++i) {
            curr = (Node)list.get(i);
            if (!curr.getNodeId().equals(node.getNodeId())) continue;
            found = curr;
            break;
        }
        if (found == null) {
            NodeList currList;
            for (i = 0; i < list.size() && (found = this.findNodeInList(node, currList = (curr = (Node)list.get(i)).getNodeList())) == null; ++i) {
            }
        }
        return found;
    }

    Node findNodeInTree(Node node, PropertyTree tree) {
        if (tree == null) {
            return null;
        }
        return this.findNodeInList(node, tree.getNodeList());
    }
}

