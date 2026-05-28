/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.BaseDiff;
import com.labvantage.sapphire.util.diff.PropertyListDiff;
import com.labvantage.sapphire.xml.Node;
import java.util.ArrayList;
import sapphire.util.DataSet;

public class NodeDiff
extends BaseDiff {
    public NodeDiff(Node sourceNode, Node targetNode) {
        String description;
        Node sourceParent = null;
        if (sourceNode.getParentNodeList() != null) {
            sourceParent = sourceNode.getParentNodeList().getParentNode();
        }
        Node targtParent = null;
        if (targetNode.getParentNodeList() != null) {
            targtParent = targetNode.getParentNodeList().getParentNode();
        }
        if (sourceParent == null || targtParent == null) {
            if (sourceParent != null) {
                description = "Parent node changed from (" + sourceNode.getNodeId() + ") to (ROOT)";
                this.addNodeDiffResult(sourceNode.getNodeId(), "Moved", description);
            } else if (targtParent != null) {
                description = "Parent node changed from (ROOT) to (" + targtParent.getNodeId() + ")";
                this.addNodeDiffResult(sourceNode.getNodeId(), "Moved", description);
            }
        } else if (!sourceParent.getNodeId().equals(targtParent.getNodeId())) {
            description = "Parent node changed from (" + sourceParent.getNodeId() + ") to (" + targtParent.getNodeId() + ")";
            this.addNodeDiffResult(sourceNode.getNodeId(), "Moved", description);
        }
        PropertyListDiff pld = new PropertyListDiff(sourceNode.getPropertyList(), targetNode.getPropertyList());
        if (pld.hasDifferences()) {
            DataSet pldDiffs = pld.getResults();
            DataSet formatResults = new DataSet();
            for (int i = 0; i < pldDiffs.getRowCount(); ++i) {
                formatResults.addRow();
                formatResults.setString(i, "Property Id", pldDiffs.getString(i, "propertyid"));
                formatResults.setString(i, "DiffItem", pldDiffs.getString(i, "level"));
                formatResults.setString(i, "Status", pldDiffs.getString(i, "propertystatus"));
                formatResults.setString(i, "From", pldDiffs.getString(i, "oldvalue"));
                formatResults.setString(i, "To", pldDiffs.getString(i, "newvalue"));
            }
            ArrayList<String> titles = new ArrayList<String>();
            titles.add("Property");
            titles.add("DiffItem");
            titles.add("Status");
            titles.add("From");
            titles.add("To");
            this.addNodeDiffResult(sourceNode.getNodeId(), "Changed", formatResults.toHTML(titles, "prop"));
        }
    }
}

