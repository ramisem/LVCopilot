/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.storageunit;

import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ResetProductStorageUnitType
extends BaseDiagnostic {
    private static List<String> storageUnitTypeList = new ArrayList<String>();
    private static List<String> OOBNodeList = new ArrayList<String>();

    public ResetProductStorageUnitType(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    public ResetProductStorageUnitType(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Move OOB Storage unit type definition node under the Product Node";
    }

    @Override
    public String getDescription() {
        return "Moves the OOB Storage unit type definition nodes under the Product Node, like move Box-9X9 under Box-9X9 Product";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        throw new DiagnosticException(1, "Move existing OOB Storage unit type nodes under Product node when upgrading from LV version prior to LV 8.2");
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        for (String storageunittype : storageUnitTypeList) {
            this.repairStorageUnitType(storageunittype);
        }
        return "Moved OOB Storage unit type definitions node under their respective Product Node successfully";
    }

    private void repairStorageUnitType(String storageunittype) {
        try {
            String nodeid;
            boolean updatePropertyTree = false;
            PropertyTree propertyTree = PropertyTreeUtil.getPropertyTree(this.database, storageunittype);
            ArrayList allNodes = propertyTree.getAllNodes();
            boolean insertProductNodes = true;
            for (Object node : allNodes) {
                if (!((Node)node).getNodeId().equals("Sapphire Product")) continue;
                insertProductNodes = false;
                break;
            }
            if (insertProductNodes) {
                Node productNode = propertyTree.createNode("Sapphire Product", null);
                propertyTree.createNode("Sapphire Custom", productNode);
                allNodes = propertyTree.getAllNodes();
            } else {
                ArrayList<Node> nodeList = new ArrayList<Node>();
                for (Node node : allNodes) {
                    if (node.getParent() != null || node.getId().endsWith(" Product") || node.getId().endsWith(" Custom")) continue;
                    nodeList.add(node);
                    if (!OOBNodeList.contains(node.getNodeId())) continue;
                    nodeList.add(this.getProductNode(node.getId(), allNodes));
                }
                allNodes.clear();
                allNodes.addAll(nodeList);
            }
            for (Object node : allNodes) {
                nodeid = ((Node)node).getNodeId();
                Node parentNode = ((Node)node).getParent();
                if (parentNode != null && (parentNode.isCustom() || !parentNode.getNodeId().equals("Sapphire Product") || parentNode.getNodeId().contains(" Comp ")) || ((Node)node).isProduct() || ((Node)node).isCustom()) continue;
                Node productNode = this.getProductNode(nodeid, allNodes);
                if (productNode != null) {
                    PropertyListCollection parentAllowedChildren = propertyTree.getNodePropertyList(productNode.getNodeId(), false).getCollectionNotNull("childrentypes");
                    PropertyListCollection childAllowedChildren = propertyTree.getNodePropertyList(nodeid, false).getCollectionNotNull("childrentypes");
                    if (parentAllowedChildren.size() > 0 && childAllowedChildren.size() > 0) {
                        boolean updateChildType = false;
                        for (int i = 0; i < parentAllowedChildren.size(); ++i) {
                            String parentType = parentAllowedChildren.getPropertyList(i).getProperty("type");
                            int removeIndex = -1;
                            for (int j = 0; j < childAllowedChildren.size(); ++j) {
                                String childType = childAllowedChildren.getPropertyList(j).getProperty("type");
                                if (!childType.equals(parentType)) continue;
                                removeIndex = j;
                                break;
                            }
                            if (removeIndex == -1) continue;
                            childAllowedChildren.remove(removeIndex);
                            updateChildType = true;
                        }
                        if (updateChildType) {
                            propertyTree.getNodePropertyList(nodeid, false).setProperty("childrentypes", childAllowedChildren);
                        }
                    }
                    propertyTree.moveNode(nodeid, nodeid + " Product");
                    updatePropertyTree = true;
                    continue;
                }
                if (parentNode != null && !parentNode.getNodeId().equals("Sapphire Product")) continue;
                propertyTree.moveNode(nodeid, "Sapphire Custom");
                updatePropertyTree = true;
            }
            if ("Grid".equals(storageunittype) || "No Layout".equals(storageunittype)) {
                allNodes = propertyTree.getAllNodes();
                for (Object node : allNodes) {
                    String keyid1;
                    String sdcid;
                    nodeid = ((Node)node).getNodeId();
                    if (nodeid.endsWith(" Product") || nodeid.endsWith(" Custom") || !"LV_Box".equals(sdcid = this.getNodePropertyValue((Node)node, "template", "sdcid")) || nodeid.equals(keyid1 = this.getNodePropertyValue((Node)node, "template", "keyid1"))) continue;
                    PropertyList templatePL = ((Node)node).getPropertyList().getPropertyListNotNull("template");
                    templatePL.setProperty("keyid1", nodeid);
                    updatePropertyTree = true;
                }
            }
            if (updatePropertyTree) {
                PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), storageunittype, propertyTree.toXMLString());
            }
        }
        catch (SapphireException e) {
            e.printStackTrace(System.out);
        }
    }

    private Node getProductNode(String nodeid, ArrayList<Node> allNodes) {
        Node productNode = null;
        for (Node node : allNodes) {
            if (!node.getNodeId().equals(nodeid + " Product")) continue;
            productNode = node;
            break;
        }
        return productNode;
    }

    private String getNodePropertyValue(Node node, String propertyListID, String propertyID) {
        Node parent;
        String value = "";
        if (propertyListID != null && !propertyListID.trim().isEmpty()) {
            PropertyList propertyList = node.getPropertyList().getPropertyListNotNull(propertyListID);
            value = propertyList.getProperty(propertyID, "");
        } else {
            value = node.getPropertyList().getProperty(propertyID, "");
        }
        if ((value == null || value.trim().isEmpty()) && (parent = node.getParent()) != null) {
            return this.getNodePropertyValue(node.getParent(), propertyListID, propertyID);
        }
        return value;
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }

    static {
        storageUnitTypeList.add("Circular");
        storageUnitTypeList.add("Grid");
        storageUnitTypeList.add("Linear");
        storageUnitTypeList.add("No Layout");
        OOBNodeList.add("Tank");
        OOBNodeList.add("Box-9X9");
        OOBNodeList.add("Plate-384");
        OOBNodeList.add("Plate-96");
        OOBNodeList.add("TubeRack-2X8");
        OOBNodeList.add("TubeRack-8X12");
        OOBNodeList.add("ArrayRack");
        OOBNodeList.add("ArraySlot");
        OOBNodeList.add("Freezer");
        OOBNodeList.add("Rack");
        OOBNodeList.add("Shelf");
        OOBNodeList.add("Slot");
        OOBNodeList.add("BoxPos");
        OOBNodeList.add("Package");
        OOBNodeList.add("UnSorted Box");
        OOBNodeList.add("Well");
    }
}

