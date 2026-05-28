/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyDependencyList;
import java.io.Serializable;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class AbstractPropertyTree
implements Serializable {
    public static final String ROOT = "root";
    protected String id;
    protected NodeList nodeList;
    protected PropertyDefaultList propertyDefaultList;
    protected PropertyDefinitionList propertyDefinitionList;
    protected PropertyDependencyList propertyDependencyList;
    protected ArrayList<ImportDirective> importDirectives = new ArrayList();

    public String getId() {
        return this.id;
    }

    public String getPropertyTreeId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    public ArrayList getAllNodes() {
        ArrayList list = new ArrayList();
        if (this.nodeList != null) {
            this.nodeList.getAllNodes(list);
        }
        return list;
    }

    public void setPropertyDefinitionList(PropertyDefinitionList propertyDefinitionList) {
        this.propertyDefinitionList = propertyDefinitionList;
    }

    public PropertyDefinitionList getPropertyDefinitionList() {
        return this.propertyDefinitionList;
    }

    public void setPropertyDependencyList(PropertyDependencyList propertyDependencyList) {
        this.propertyDependencyList = propertyDependencyList;
    }

    public PropertyDependencyList getPropertyDependencyList() {
        return this.propertyDependencyList;
    }

    public PropertyDefaultList getPropertyDefaultList() {
        return this.propertyDefaultList;
    }

    public void setPropertyDefaultList(PropertyDefaultList propertyDefaultList) {
        this.propertyDefaultList = propertyDefaultList;
    }

    public void setImportDirectives(ArrayList<ImportDirective> importDirectives) {
        this.importDirectives = importDirectives;
    }

    public Node getNode(String nodeid) {
        return this.nodeList == null ? null : this.nodeList.findNode(nodeid, true);
    }

    public Node getNode(String nodeid, boolean caseSensitive) {
        return this.nodeList == null ? null : this.nodeList.findNode(nodeid, caseSensitive);
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        StringBuffer out = new StringBuffer(StringUtil.repeat("\t", level) + "<propertytree>\n");
        if (this.nodeList != null) {
            out.append(this.nodeList.toXMLString(level + 1));
        }
        if (this.propertyDefaultList != null) {
            out.append(this.propertyDefaultList.toXMLString(level + 1));
        }
        return out.toString() + StringUtil.repeat("\t", level) + "\n</propertytree>";
    }

    public PropertyList getNodePropertyList(String nodeId, boolean collapseAncestors) throws SapphireException {
        return this.getNodePropertyList(nodeId, collapseAncestors, true);
    }

    public PropertyList getNodePropertyList(String nodeId, boolean collapseAncestors, boolean setPropertyTreeDefaults) throws SapphireException {
        PropertyList propertyList = null;
        Node node = this.getNode(nodeId);
        if (node != null) {
            if (collapseAncestors) {
                propertyList = new PropertyList();
                propertyList.setUsePropertyValues(true);
                ArrayList<Node> nodeList = new ArrayList<Node>();
                nodeList.add(node);
                Node parent = node.getParent();
                if (parent != null) {
                    nodeList.add(parent);
                }
                while (parent != null) {
                    if ((parent = parent.getParent()) == null) continue;
                    nodeList.add(parent);
                }
                for (int i = nodeList.size() - 1; i >= 0; --i) {
                    Node tempnode = (Node)nodeList.get(i);
                    propertyList.setPropertyList(tempnode.getPropertyList().toXMLString(), true, tempnode.getId(), true);
                }
                if (setPropertyTreeDefaults) {
                    if (this.propertyDefaultList == null) {
                        Trace.logWarn("Property Default List missing. Cannot set Root node values to PropertyList.");
                    }
                    if (this.propertyDefinitionList == null) {
                        Trace.logWarn("Property Definition List missing. Cannot set Definition Default values to PropertyList.");
                    }
                    propertyList.setPropertyTreeDefaults(this.propertyDefaultList, this.propertyDefinitionList);
                }
            } else {
                propertyList = node.getPropertyList();
            }
        } else {
            throw new SapphireException("Node id " + nodeId + " not found in propertytree.");
        }
        return propertyList;
    }

    public NodeList getNodeList() {
        return this.nodeList;
    }

    public NodeList getNodeDescendantList(String nodeId) throws SapphireException {
        NodeList descendantList = new NodeList();
        Node node = this.getNode(nodeId);
        if (node == null) {
            throw new SapphireException("Node id " + nodeId + " not found in propertytree.");
        }
        node.fillWithDescendants(descendantList);
        descendantList.remove(node);
        return descendantList;
    }

    public NodeList getNodeAncestorList(String nodeId) throws SapphireException {
        NodeList ancestorList = new NodeList();
        Node node = this.getNode(nodeId);
        if (node == null) {
            throw new SapphireException("Node id " + nodeId + " not found in propertytree.");
        }
        node.fillWithAncestors(ancestorList);
        ancestorList.remove(node);
        return ancestorList;
    }

    public String getNodeXML(String nodeId, boolean collapseAncestors, boolean includeDescendants, int level) throws SapphireException {
        NodeList nodeList;
        StringBuffer out = new StringBuffer();
        PropertyList propertyList = this.getNodePropertyList(nodeId, collapseAncestors);
        if (propertyList != null) {
            out.append(propertyList.toXMLString(level));
        }
        if (includeDescendants && (nodeList = this.getNodeDescendantList(nodeId)) != null) {
            out.append(nodeList.toXMLString(level));
        }
        return out.toString();
    }

    public void renameNode(String nodeid, String newnodeid) {
        Node node = this.getNode(nodeid);
        if (node != null) {
            node.renameNode(newnodeid);
        }
    }

    public void deleteNode(String nodeid) {
        if (this.nodeList != null) {
            this.nodeList.deleteNode(nodeid);
        }
    }

    public void moveNode(String nodeid, String newParentid) {
        Node node = this.getNode(nodeid);
        if (node != null) {
            Node newParent;
            NodeList currentParentPropertyList = node.getParentNodeList();
            if (currentParentPropertyList != null) {
                currentParentPropertyList.remove(node);
            }
            if ((newParent = this.getNode(newParentid)) == null) {
                node.setParentNodeList(this.nodeList);
            } else {
                node.setParentNodeList(newParent.getNodeList());
            }
        }
    }

    public boolean propertyListIdExists(String propertylistid) {
        ArrayList allnodes = this.getAllNodes();
        for (Node node : allnodes) {
            PropertyList propertyList = node.getPropertyList();
            if (propertyList == null || propertyList.findPropertyList(propertylistid) == null) continue;
            return true;
        }
        return false;
    }

    public long getUniquePropertyListId(long propertylistid) {
        while (this.propertyListIdExists(Long.toString(propertylistid))) {
            ++propertylistid;
        }
        return propertylistid;
    }

    public Node createNode(String nodeid, Node parentNode) throws SapphireException {
        NodeList nodeList;
        this.deleteNode(nodeid);
        if (parentNode == null) {
            NodeList propertyTreeNodeList = this.getNodeList();
            if (propertyTreeNodeList == null) {
                nodeList = new NodeList();
                this.setNodeList(nodeList);
            } else {
                nodeList = propertyTreeNodeList;
            }
        } else {
            nodeList = parentNode.getNodeList();
        }
        if (nodeList == null) {
            throw new SapphireException("Parent nodelist not found while creating new node " + nodeid);
        }
        Node newNode = new Node(nodeid);
        newNode.setParentNodeList(nodeList);
        return newNode;
    }
}

