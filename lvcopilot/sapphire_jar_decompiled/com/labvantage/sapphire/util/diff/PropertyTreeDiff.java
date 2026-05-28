/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.BaseDiff;
import com.labvantage.sapphire.util.diff.NodeListDiff;
import com.labvantage.sapphire.util.diff.PropertyDefaultListDiff;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import sapphire.util.DataSet;

public class PropertyTreeDiff
extends BaseDiff {
    public PropertyTreeDiff(PropertyTree sourceTree, PropertyTree targetTree) {
        NodeListDiff nld = new NodeListDiff(sourceTree, targetTree);
        DataSet nodeDiffs = nld.getResults();
        PropertyDefaultListDiff propdefaultlistdiff = new PropertyDefaultListDiff(sourceTree.getPropertyDefaultList(), targetTree.getPropertyDefaultList());
        DataSet defaultListDiffs = propdefaultlistdiff.getResults();
        if (defaultListDiffs.getRowCount() > 0) {
            int newrow = nodeDiffs.addRow();
            nodeDiffs.setString(newrow, "level", "(ROOT)");
            nodeDiffs.setString(newrow, "propertystatus", "Changed");
            ArrayList<String> titles = new ArrayList<String>();
            titles.add("Identifier");
            titles.add("Item");
            titles.add("Status");
            titles.add("From");
            titles.add("To");
            nodeDiffs.setString(newrow, "description", defaultListDiffs.toHTML(titles, "prop"));
        }
        this.results = nodeDiffs;
    }
}

