/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.AbstractPropertyTree;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.PropertyTreeHandler;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.File;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyTree
extends AbstractPropertyTree {
    public PropertyTree() {
    }

    public PropertyTree(String properytreeid) {
        this.id = properytreeid;
    }

    public void setValueXML(String xml) throws SapphireException {
        this.setValueXML(xml, false);
    }

    public void setValueXML(String xml, boolean createTransferableObjects) throws SapphireException {
        try {
            PropertyTreeHandler handler = new PropertyTreeHandler(this);
            handler.setXMLString(xml);
            handler.setPrintStream(null);
            handler.setCreateTransferableObjects(createTransferableObjects);
            SaxUtil.parseString(handler);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public void setValueXMLFile(String filename) throws SapphireException {
        try {
            PropertyTreeHandler handler = new PropertyTreeHandler(this);
            handler.setXMLFile(new File(filename));
            handler.setLogFile(new File(filename + "_log"));
            SaxUtil.parseFile(handler);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public void setDefinitionXML(String xml) throws SapphireException {
        try {
            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(this);
            handler.setXMLString(xml);
            handler.setPrintStream(null);
            SaxUtil.parseString(handler);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public void setDefinitionXMLFile(String filename) throws SapphireException {
        try {
            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(this);
            handler.setXMLFile(new File(filename));
            handler.setLogFile(new File(filename + "_log"));
            SaxUtil.parseFile(handler);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public PropertyTreeTransfer getPropertyTreeTransfer() {
        PropertyTreeTransfer propertyTreeTransfer = new PropertyTreeTransfer(this.id);
        propertyTreeTransfer.setNodeList((NodeList)this.nodeList.clone());
        return propertyTreeTransfer;
    }

    public void merge(PropertyTree mergePropertyTree) throws SapphireException {
        this.merge(mergePropertyTree, false);
    }

    public void merge(PropertyTree mergePropertyTree, boolean replaceRoot) throws SapphireException {
        PropertyDefaultList mergeDefaultList;
        boolean debug = false;
        NodeList mergeNodeList = mergePropertyTree.getNodeList();
        for (int j = 0; mergeNodeList != null && j < mergeNodeList.size(); ++j) {
            PropertyList collapsedPropertyList;
            PropertyList nodePropertyList;
            boolean merge;
            Node mergeNode = (Node)mergeNodeList.get(j);
            String nodeid = mergeNode.getId();
            String extendsnodeid = mergeNode.getExtendsNodeId();
            Trace.logDebug("Processing node: " + nodeid);
            PropertyListTransfer mergePropertyList = (PropertyListTransfer)mergeNode.getPropertyList();
            if (mergePropertyList == null) {
                throw new SapphireException("No propertylist found at node " + nodeid + " in propetry tree " + mergePropertyTree.getId());
            }
            Node currentNode = this.getNode(nodeid);
            boolean nodeExists = currentNode != null;
            boolean ignore = !nodeExists && mergeNode.getNotexists().equals("ignore") || nodeExists && mergeNode.getExists().equals("ignore");
            boolean bl = merge = nodeExists && mergeNode.getExists().equals("merge") && !mergeNode.isLocked();
            if (ignore) continue;
            if (!nodeExists) {
                Node parentNode = null;
                Node customNode = null;
                if (extendsnodeid != null && extendsnodeid.length() > 0 && !extendsnodeid.equals("root")) {
                    parentNode = this.getNode(extendsnodeid);
                    if (parentNode.isProduct() && mergeNode.getCompCode().length() > 0) {
                        customNode = this.getNode(extendsnodeid.substring(0, extendsnodeid.lastIndexOf(" ")) + " Custom");
                        if (customNode == null) {
                            customNode = this.getNode(nodeid.substring(0, nodeid.lastIndexOf(" Comp ")) + " Custom");
                        }
                        if (customNode == null) {
                            customNode = this.getNode(nodeid.substring(0, nodeid.lastIndexOf(" Comp ")) + " Custom", false);
                        }
                    } else if (parentNode.getCompCode().length() > 0 && mergeNode.getCompCode().length() > 0) {
                        customNode = this.getNode(extendsnodeid.substring(0, extendsnodeid.lastIndexOf(" ")) + " Custom");
                    }
                    if (parentNode == null) {
                        throw new SapphireException("Parent node " + extendsnodeid + " not found in property tree " + this.getId());
                    }
                }
                if (debug) {
                    Trace.logDebug("Node does not exist - creating new node, extending " + (extendsnodeid != null && extendsnodeid.length() > 0 ? extendsnodeid : "root") + "...");
                }
                if (customNode != null && customNode.getParent() != null) {
                    currentNode = this.createNode(nodeid, customNode.getParent());
                    customNode.setParentNodeList(currentNode.getNodeList());
                } else {
                    currentNode = this.createNode(nodeid, parentNode);
                }
            }
            if (!merge) {
                PropertyList propertyList = new PropertyList();
                propertyList.setUsePropertyValues(true);
                currentNode.setPropertyList(propertyList);
                currentNode.setLocked(mergeNode.isLocked());
                currentNode.setCategoryList(mergeNode.getCategoryList());
            }
            if ((nodePropertyList = this.getNodePropertyList(nodeid, false)) == null) {
                nodePropertyList = new PropertyList();
            }
            if ((collapsedPropertyList = this.getNodePropertyList(nodeid, true)) == null) {
                collapsedPropertyList = new PropertyList();
            }
            this.mergeNode(nodePropertyList, collapsedPropertyList, mergePropertyList, this.propertyDefinitionList);
        }
        if (replaceRoot && (mergeDefaultList = mergePropertyTree.getPropertyDefaultList()) != null) {
            this.setPropertyDefaultList(mergeDefaultList);
        }
    }

    private void mergeNode(PropertyList nodePropertyList, PropertyList collapsedPropertyList, PropertyListTransfer mergePropertyList, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        if (mergePropertyList != null) {
            nodePropertyList.mergeAttributes(mergePropertyList.getAttributes());
        }
        if (propertyDefinitionList == null) {
            nodePropertyList.setPropertyList(mergePropertyList.toXMLString());
        } else if (mergePropertyList != null && mergePropertyList.size() > 0) {
            String exists = mergePropertyList.getExists();
            String notexists = mergePropertyList.getNotexists();
            for (PropertyDefinition propertyDefinition : propertyDefinitionList) {
                String propertyid = propertyDefinition.getId();
                if (propertyDefinition.getType().equals("simple")) {
                    String mergePropertyValue = mergePropertyList.getProperty(propertyid);
                    String collapsedPropertyValue = collapsedPropertyList.getProperty(propertyid);
                    if (mergePropertyValue.length() <= 0 || mergePropertyValue.equals(collapsedPropertyValue)) continue;
                    if (collapsedPropertyValue.length() > 0) {
                        if (!exists.equals("replace")) continue;
                        nodePropertyList.setProperty(propertyid, mergePropertyValue);
                        continue;
                    }
                    if (!notexists.equals("add")) continue;
                    nodePropertyList.setProperty(propertyid, mergePropertyValue);
                    continue;
                }
                if (propertyDefinition.getType().equals("propertylist")) {
                    PropertyList subNodePropertyList = nodePropertyList.getPropertyList(propertyid);
                    PropertyList subCollapsedPropertyList = collapsedPropertyList.getPropertyList(propertyid);
                    PropertyListTransfer subMergePropertyList = (PropertyListTransfer)mergePropertyList.getPropertyList(propertyid);
                    if (subNodePropertyList == null) {
                        subNodePropertyList = new PropertyList();
                    }
                    if (subCollapsedPropertyList == null) {
                        subCollapsedPropertyList = new PropertyList();
                    }
                    this.mergeNode(subNodePropertyList, subCollapsedPropertyList, subMergePropertyList, propertyDefinition.getPropertyDefinitionList());
                    nodePropertyList.setProperty(propertyid, subNodePropertyList);
                    continue;
                }
                if (propertyDefinition.getType().equals("collection")) {
                    PropertyListCollection mergeCollection = mergePropertyList.getCollection(propertyid);
                    if (mergeCollection == null) continue;
                    PropertyListCollection collapsedCollection = collapsedPropertyList.getCollection(propertyid);
                    PropertyListCollection nodeCollection = nodePropertyList.getCollection(propertyid);
                    if (nodeCollection == null) {
                        nodeCollection = new PropertyListCollection();
                    }
                    for (int i = 0; i < mergeCollection.size(); ++i) {
                        PropertyListTransfer subMergePropertyList = (PropertyListTransfer)mergeCollection.getPropertyList(i);
                        if (subMergePropertyList.size() <= 0 && subMergePropertyList.getAttribute("rolelist").length() <= 0 && subMergePropertyList.getAttribute("modulelist").length() <= 0) continue;
                        String subMergeIdValue = subMergePropertyList.getProperty("id");
                        String subMergePropertyListid = subMergePropertyList.getId();
                        boolean matchOnProperty = subMergeIdValue.length() > 0;
                        PropertyList foundSubNodePropertyList = null;
                        if (collapsedCollection == null) {
                            PropertyList dummySubNodePropertyList = new PropertyList(subMergePropertyListid);
                            PropertyList dummySubCollapsedPropertyList = new PropertyList(subMergePropertyListid);
                            this.mergeNode(dummySubNodePropertyList, dummySubCollapsedPropertyList, subMergePropertyList, propertyDefinition.getPropertyDefinitionList());
                            nodeCollection.add(dummySubNodePropertyList);
                            continue;
                        }
                        boolean foundCollapsedPropertyList = false;
                        for (int j = 0; j < collapsedCollection.size() && !foundCollapsedPropertyList; ++j) {
                            PropertyList subCollapsedPropertyList = collapsedCollection.getPropertyList(j);
                            String subCollapsedPropertyListid = subCollapsedPropertyList.getId();
                            foundCollapsedPropertyList = matchOnProperty ? subCollapsedPropertyList.getProperty("id").equals(subMergeIdValue) : subCollapsedPropertyListid.equals(subMergePropertyListid);
                            if (!foundCollapsedPropertyList) continue;
                            foundSubNodePropertyList = nodeCollection.getPropertyList(subCollapsedPropertyListid);
                            if (foundSubNodePropertyList != null) {
                                this.mergeNode(foundSubNodePropertyList, subCollapsedPropertyList, subMergePropertyList, propertyDefinition.getPropertyDefinitionList());
                                continue;
                            }
                            PropertyList dummySubNodePropertyList = new PropertyList(subCollapsedPropertyListid);
                            this.mergeNode(dummySubNodePropertyList, subCollapsedPropertyList, subMergePropertyList, propertyDefinition.getPropertyDefinitionList());
                            if (dummySubNodePropertyList.size() <= 0 && dummySubNodePropertyList.getAttribute("rolelist").length() <= 0 && dummySubNodePropertyList.getAttribute("modulelist").length() <= 0) continue;
                            nodeCollection.add(dummySubNodePropertyList);
                        }
                        if (foundCollapsedPropertyList) continue;
                        PropertyList dummySubNodePropertyList = new PropertyList(subMergePropertyListid);
                        PropertyList dummySubCollapsedPropertyList = new PropertyList(subMergePropertyListid);
                        this.mergeNode(dummySubNodePropertyList, dummySubCollapsedPropertyList, subMergePropertyList, propertyDefinition.getPropertyDefinitionList());
                        nodeCollection.add(dummySubNodePropertyList);
                    }
                    if (nodeCollection.size() <= 0) continue;
                    nodePropertyList.setProperty(propertyid, nodeCollection);
                    continue;
                }
                throw new SapphireException("Unrecognized propertydef type '" + propertyDefinition.getType() + "' in propertydeflist");
            }
        }
        if (nodePropertyList == null) {
            nodePropertyList = new PropertyList(collapsedPropertyList != null ? collapsedPropertyList.getId() : "");
        }
    }
}

