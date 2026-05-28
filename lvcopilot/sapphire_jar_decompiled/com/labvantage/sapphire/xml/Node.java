/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.NodeList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Node
implements Serializable {
    public static final String IMPORT_ADD = "add";
    public static final String IMPORT_IGNORE = "ignore";
    public static final String IMPORT_MERGE = "merge";
    public static final String IMPORT_REPLACE = "replace";
    public static final String EXPORT_DEFAULT = "default";
    private String exists = "replace";
    private String notexists = "add";
    private PropertyList propertyList;
    private String id;
    private String originalNodeId;
    private String categoryList;
    private boolean locked;
    private NodeList parentNodeList;
    private String extendsNodeId;
    private NodeList nodeList;
    private boolean collapseAncestors;
    private boolean includeDescendants;
    private boolean includeAncestors;
    private HashMap propertyListRenames = new HashMap();
    private HashMap propertyListRenamesReverseMap = new HashMap();

    public Node(Node node) {
        this.id = node.getId();
        this.originalNodeId = node.getId();
        this.propertyList = node.getPropertyList(true).copy();
        this.categoryList = node.getCategoryList();
        this.locked = node.isLocked();
        this.collapseAncestors = node.isCollapseAncestors();
        this.includeDescendants = node.isIncludeDescendants();
        this.includeAncestors = node.isIncludeAncestors();
        this.exists = node.exists;
        this.notexists = node.notexists;
        this.extendsNodeId = node.extendsNodeId;
    }

    public Node(String id) {
        this.id = id;
        this.originalNodeId = id;
    }

    public String getId() {
        return this.id;
    }

    public String getNodeId() {
        return this.id;
    }

    public String getExtendsNodeId() {
        return this.extendsNodeId;
    }

    public void setExtendsNodeId(String extendsNodeId) {
        this.extendsNodeId = extendsNodeId;
    }

    public String getCategoryList() {
        return this.categoryList != null ? this.categoryList : "";
    }

    public void setCategoryList(String categoryList) {
        this.categoryList = categoryList;
    }

    public boolean isCollapseAncestors() {
        return this.collapseAncestors;
    }

    public void setCollapseAncestors(boolean collapseAncestors) {
        this.collapseAncestors = collapseAncestors;
    }

    public void setIncludeAncestors(boolean includeAncestors) {
        this.includeAncestors = includeAncestors;
    }

    public boolean isIncludeAncestors() {
        return this.includeAncestors;
    }

    public boolean isIncludeDescendants() {
        return this.includeDescendants;
    }

    public void setIncludeDescendants(boolean includeDescendants) {
        this.includeDescendants = includeDescendants;
    }

    public Node getParent() {
        return this.getParentNodeList() != null ? this.getParentNodeList().getParentNode() : null;
    }

    public NodeList getParentNodeList() {
        return this.parentNodeList;
    }

    public void setParentNodeList(NodeList parentNodeList) {
        if (this.parentNodeList != null) {
            this.parentNodeList.remove(this);
        }
        if (!parentNodeList.contains(this)) {
            parentNodeList.add(this);
        }
        this.parentNodeList = parentNodeList;
    }

    public void setNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    public NodeList getNodeList() {
        if (this.nodeList == null) {
            this.nodeList = new NodeList();
        }
        this.nodeList.setParentNode(this);
        return this.nodeList;
    }

    public void setPropertyList(PropertyList propertyList) {
        this.propertyList = propertyList;
    }

    public PropertyList getPropertyList(boolean create) {
        if (this.propertyList == null) {
            this.propertyList = new PropertyList();
        }
        return this.propertyList;
    }

    public PropertyList getPropertyList() {
        return this.getPropertyList(false);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isProduct() {
        return this.locked && this.id.endsWith(" Product");
    }

    public boolean isCustom() {
        return !this.locked && this.id.endsWith(" Custom");
    }

    public String getCompCode() {
        return this.locked && this.id.contains(" Comp ") ? this.id.substring(this.id.lastIndexOf(" ") + 1) : "";
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getExists() {
        return this.exists;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public String getNotexists() {
        return this.notexists;
    }

    public void setNotexists(String notexists) {
        this.notexists = notexists;
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        StringBuffer out = new StringBuffer(StringUtil.repeat("\t", level) + "<node " + (this.locked ? "locked=\"Y\" " : "") + "id=\"" + this.id + "\" categorylist=\"" + this.getCategoryList() + "\">\n");
        if (this.nodeList != null) {
            out.append(this.nodeList.toXMLString(level + 1));
        }
        if (this.propertyList != null) {
            out.append(this.propertyList.toXMLString(level + 1));
        }
        return out.toString() + "\n" + StringUtil.repeat("\t", level) + "</node>\n";
    }

    public void renameNode(String newNodeid) {
        this.id = newNodeid;
    }

    public String getOriginalNodeId() {
        return this.originalNodeId;
    }

    public boolean isRenamed() {
        return this.id != null && !this.id.equals(this.originalNodeId);
    }

    public void renamePropertyList(String from, String to) {
        String original;
        PropertyList pl = this.propertyList.findPropertyList(from);
        if (pl != null) {
            pl.setId(to);
        }
        if (this.nodeList != null) {
            for (Node node : this.nodeList) {
                node.renamePropertyList(from, to);
            }
        }
        if ((original = (String)this.propertyListRenamesReverseMap.get(from)) == null) {
            this.propertyListRenames.put(from, to);
            this.propertyListRenamesReverseMap.put(to, from);
        } else {
            this.propertyListRenames.put(original, to);
            this.propertyListRenamesReverseMap.remove(from);
            this.propertyListRenamesReverseMap.put(to, original);
        }
    }

    public boolean hasPropertyListChanges() {
        return this.propertyListRenames.size() > 0;
    }

    public HashMap getPropertyListRenames() {
        return this.propertyListRenames;
    }

    public void resolveProperytListInheritance(PropertyList parentPropertyList) {
        if (parentPropertyList != null && parentPropertyList.size() > 0 && this.propertyList != null && this.propertyList.size() > 0) {
            this.recurseResolveProperytListInheritance(parentPropertyList, this.propertyList);
        }
    }

    public void recurseResolveProperytListInheritance(PropertyList parentPropertyList, PropertyList thisPropertyList) {
        Set s = thisPropertyList.keySet();
        for (String propertyid : s) {
            PropertyListCollection parentCollection;
            Object value = thisPropertyList.get(propertyid);
            if (value == null) continue;
            if (value instanceof String) {
                if (!((String)value).equals(parentPropertyList.getProperty(propertyid))) continue;
                thisPropertyList.deleteProperty(propertyid);
                continue;
            }
            if (value instanceof PropertyList) {
                this.recurseResolveProperytListInheritance(parentPropertyList.getPropertyList(propertyid), (PropertyList)value);
                continue;
            }
            if (!(value instanceof PropertyListCollection) || (parentCollection = parentPropertyList.getCollection(propertyid)) == null || parentCollection.size() <= 0) continue;
            for (PropertyList thisSubPropertyList : (PropertyListCollection)value) {
                PropertyList parentSubPropertyList = parentCollection.getPropertyList(thisSubPropertyList.getId());
                this.recurseResolveProperytListInheritance(parentSubPropertyList, thisSubPropertyList);
            }
        }
    }

    public void fillWithAncestors(NodeList ancestorList) {
        Node parent = this.getParent();
        if (parent != null) {
            parent.fillWithAncestors(ancestorList);
        }
        ancestorList.add(this);
    }

    public void fillWithDescendants(NodeList descendantList) {
        descendantList.add(this);
        if (this.nodeList != null) {
            for (Node node : this.nodeList) {
                node.fillWithDescendants(descendantList);
            }
        }
    }

    public String toString() {
        return this.id;
    }
}

