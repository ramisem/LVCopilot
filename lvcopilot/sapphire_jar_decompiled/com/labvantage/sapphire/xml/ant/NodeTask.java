/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.ant.PropertyListTask;
import org.apache.tools.ant.Task;

public class NodeTask
extends Task {
    private String nodeId;
    private String collapseancestors;
    private String includeancestors;
    private String includedescendants;
    private String extendsnodeid;
    private String categoryid;
    private PropertyListTask propertyList;
    private String exists = "replace";
    private String notexists = "add";

    public String getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    private boolean collapseAncestors() {
        return this.collapseancestors != null && this.collapseancestors.equals("true");
    }

    public void setCollapseancestors(String collapseancestors) {
        this.collapseancestors = collapseancestors;
    }

    private boolean includeDescendants() {
        return this.includedescendants == null || this.includedescendants.equals("true");
    }

    public void setIncludedescendants(String includedescendants) {
        this.includedescendants = includedescendants;
    }

    public void setIncludedescendents(String includedescendents) {
        this.includedescendants = includedescendents;
    }

    public void setIncludeancestors(String includeancestors) {
        this.includeancestors = includeancestors;
    }

    private boolean includeAncestors() {
        return this.includeancestors == null || this.includeancestors.equals("true");
    }

    public void setExtendsnodeid(String extendsnodeid) {
        this.extendsnodeid = extendsnodeid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public void setNotexists(String notexists) {
        this.notexists = notexists;
    }

    public void addConfiguredPropertyList(PropertyListTask propertyList) {
        this.propertyList = propertyList;
    }

    public Node getNode() {
        Node node = new Node(this.nodeId);
        node.setCollapseAncestors(this.collapseAncestors());
        node.setIncludeAncestors(this.includeAncestors());
        node.setIncludeDescendants(this.includeDescendants());
        node.setExists(this.exists);
        node.setNotexists(this.notexists);
        node.setExtendsNodeId(this.extendsnodeid);
        node.setCategoryList(this.categoryid);
        if (this.propertyList != null) {
            node.setPropertyList(this.propertyList.getPropertyListTransfer());
        }
        return node;
    }
}

