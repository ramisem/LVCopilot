/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.AbstractPropertyTree;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PropertyTreeTransfer
extends AbstractPropertyTree
implements Transferable,
TransferConstants,
Cloneable {
    private File file;
    private String exists = "merge";
    private String notexists = "add";
    private boolean verbose;
    private boolean parseOnly;
    private String commitScope = "table";
    private boolean definition = false;
    private boolean ignoreMissingObjects = false;
    private boolean rootDefaults = false;
    private NodeList currentNodeList;
    private Node currentNode;
    private StringBuffer propertyListBuffer = new StringBuffer();
    private int propertyListIndex = 0;
    private StringBuffer propertyDefListBuffer = new StringBuffer();
    private int propertyDefListIndex = 0;
    private boolean propertyDepList = false;
    private String currentPropertyType;
    private PropertyList transferOptions = new PropertyList();
    private Set exportedNodes = new HashSet();
    private String explode;
    private ArrayList propertyDefaultLists = new ArrayList();
    private String lastPropertyDefaultId = "";
    private boolean isDevMode = false;
    private String compCode = "";

    public PropertyTreeTransfer() {
    }

    public PropertyTreeTransfer(String properytreeid) {
        this.id = properytreeid;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setParseOnly(boolean parseOnly) {
        this.parseOnly = parseOnly;
    }

    @Override
    public void setCommitScope(String commitScope) {
        this.commitScope = commitScope;
    }

    @Override
    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public void setNotexists(String notexists) {
        this.notexists = notexists;
    }

    public String getExists() {
        return this.exists;
    }

    public String getNotexists() {
        return this.notexists;
    }

    @Override
    public List getReferencedItems() {
        return null;
    }

    public void setExplode(String explode) {
        this.explode = explode;
    }

    public String getExplode() {
        return this.explode;
    }

    @Override
    public void evalProperties(PropertyList props) {
    }

    @Override
    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws SapphireException {
        if (this.id == null || this.id.length() == 0) {
            throw new SapphireException("Propertytreeid not specified for property tree export");
        }
        if (!(this.exists.equals("add") || this.notexists.equals("ignore") || this.exists.equals("merge") || this.exists.equals("replace"))) {
            throw new SapphireException("Unrecognized exists attribute (must be one of add, ignore, merge or replace)");
        }
        if (!this.notexists.equals("add") && !this.notexists.equals("ignore")) {
            throw new SapphireException("Unrecognized notexists attribute (must be one of add or ignore)");
        }
        switch (this.getTransferOption("forcedevmode").length()) {
            case 0: {
                this.isDevMode = database.checkExists("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'devmode' AND propertyvalue = 'Y'");
                database.createResultSet("GetCompCode", "SELECT propertyvalue FROM sysconfig WHERE propertyid='compcode'");
                if (!database.getNext("GetCompCode")) break;
                this.compCode = database.getValue("GetCompCode", "propertyvalue");
                break;
            }
            case 1: {
                this.isDevMode = this.getTransferOption("forcedevmode").equals("Y");
                break;
            }
            default: {
                this.compCode = this.getTransferOption("forcedevmode");
            }
        }
        long start = System.currentTimeMillis();
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        if (this.explode == null || this.explode.length() == 0) {
            this.exportedNodes.clear();
            logger.log(level0 + "Exporting PROPERTYTREE " + this.id);
            logger.log(level1 + "Import options: " + this.exists + " existing nodes, " + this.notexists + " missing nodes.");
            PropertyTree loadedPropertyTree = new PropertyTree();
            loadedPropertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(database, this.getPropertyTreeId(), true));
            loadedPropertyTree.setDefinitionXML(PropertyTreeUtil.getPropertyTreeDefinition(database, this.getPropertyTreeId(), true));
            if (loadedPropertyTree.getNodeList() != null && loadedPropertyTree.getNodeList().size() > 0) {
                out.println(level0 + "<propertytree propertytreeid=\"" + this.id + "\" exists=\"" + this.exists + "\" notexists=\"" + this.notexists + "\">");
                if (this.nodeList == null) {
                    this.nodeList = new NodeList();
                } else if (this.nodeList.size() == 1) {
                    Node loadedNode;
                    int i;
                    ArrayList loadedNodeList;
                    Node node = (Node)this.nodeList.get(0);
                    String nodeid = node.getNodeId();
                    String categoryid = node.getCategoryList();
                    if (nodeid != null && nodeid.equals("*")) {
                        this.nodeList.clear();
                        loadedNodeList = loadedPropertyTree.getNodeList();
                        for (i = 0; i < loadedNodeList.size(); ++i) {
                            loadedNode = (Node)loadedNodeList.get(i);
                            loadedNode.setCollapseAncestors(false);
                            loadedNode.setIncludeDescendants(true);
                            loadedNode.setExists(node.getExists());
                            loadedNode.setNotexists(node.getNotexists());
                            this.nodeList.add(loadedNode);
                        }
                    } else if ((nodeid == null || nodeid.length() == 0) && categoryid.length() > 0) {
                        this.nodeList.clear();
                        loadedNodeList = loadedPropertyTree.getAllNodes();
                        for (i = 0; i < loadedNodeList.size(); ++i) {
                            loadedNode = (Node)loadedNodeList.get(i);
                            if ((";" + loadedNode.getCategoryList() + ";").indexOf(categoryid) <= -1) continue;
                            loadedNode.setCollapseAncestors(node.isCollapseAncestors());
                            loadedNode.setIncludeDescendants(node.isIncludeDescendants());
                            loadedNode.setExists(node.getExists());
                            loadedNode.setNotexists(node.getNotexists());
                            this.nodeList.add(loadedNode);
                        }
                    }
                }
                NodeList nodeList = this.getNodeList();
                if (nodeList.size() > 0) {
                    out.println(level1 + "<nodelist>");
                    for (int j = 0; j < nodeList.size(); ++j) {
                        Node node = (Node)nodeList.get(j);
                        if (loadedPropertyTree.getNode(node.getNodeId()) == null) continue;
                        String exists = node.getExists();
                        if (node.isIncludeAncestors()) {
                            NodeList ancestorNodeList = loadedPropertyTree.getNodeAncestorList(node.getNodeId());
                            for (Node loadedAncestorNode : ancestorNodeList) {
                                Node ancestorNode = new Node(loadedAncestorNode.getId());
                                ancestorNode.setPropertyList(loadedAncestorNode.getPropertyList());
                                ancestorNode.setExists(exists);
                                ancestorNode.setNotexists(node.getNotexists());
                                this.exportNode(out, zipOut, level + 1, ancestorNode, loadedPropertyTree, exportFile, database, logger, exported);
                            }
                        }
                        this.exportNode(out, zipOut, level + 1, node, loadedPropertyTree, exportFile, database, logger, exported);
                        if (!node.isIncludeDescendants()) continue;
                        NodeList descendantList = loadedPropertyTree.getNodeDescendantList(node.getNodeId());
                        for (Node loadedDescendantNode : descendantList) {
                            Node descendantNode = new Node(loadedDescendantNode.getId());
                            descendantNode.setPropertyList(loadedDescendantNode.getPropertyList());
                            descendantNode.setExists(exists);
                            descendantNode.setNotexists(node.getNotexists());
                            this.exportNode(out, zipOut, level + 1, descendantNode, loadedPropertyTree, exportFile, database, logger, exported);
                        }
                    }
                    out.println(level1 + "</nodelist>");
                } else {
                    logger.log(level1 + "Exporting all nodes");
                    out.println(loadedPropertyTree.getNodeList().toXMLString(level + 2));
                }
                out.println(level0 + "</propertytree>");
            } else {
                out.println(level0 + "<propertytree/>");
            }
        } else if (this.explode.equals("root")) {
            logger.log(level0 + "Exporting PROPERTYTREE root " + this.id);
            out.println(level0 + "<propertytree propertytreeid=\"" + this.id + "\" exists=\"replace\" notexists=\"add\">");
            if (this.propertyDefaultList != null) {
                out.println(this.propertyDefaultList.toXMLString(level + 1));
            } else {
                out.println("\t\t<propertydefaultlist>\n\t\t</propertydefaultlist>");
            }
            out.println(level0 + "</propertytree>");
        } else {
            logger.log(level0 + "Exporting PROPERTYTREE definition " + this.id);
            out.println(level0 + "<propertytree propertytreeid=\"" + this.id + "\" exists=\"replace\" notexists=\"add\">");
            if (this.propertyDependencyList != null) {
                out.println(this.propertyDependencyList.toXMLString(level + 1));
            }
            if (this.propertyDefinitionList != null) {
                out.println(this.propertyDefinitionList.toXMLString(level + 1));
            }
            out.println(level0 + "</propertytree>");
        }
        logger.log(level0 + "PropertyTree export took " + (System.currentTimeMillis() - start) + "ms");
    }

    private void exportNode(PrintStream out, ZipOutputStream zipOut, int level, Node node, PropertyTree loadedPropertyTree, File exportFile, DBAccess database, Logger logger, Map exported) throws SapphireException {
        Node loadedNode;
        if (!this.exportedNodes.contains(node.getId()) && (loadedNode = loadedPropertyTree.getNode(node.getNodeId())) != null) {
            this.exportedNodes.add(node.getId());
            if (node.getExists().equals("default")) {
                if (this.isDevMode) {
                    node.setExists(loadedNode.isProduct() ? "replace" : "ignore");
                } else if (this.compCode.length() > 0) {
                    node.setExists(this.compCode.equals(loadedNode.getCompCode()) ? "replace" : "ignore");
                } else {
                    node.setExists(loadedNode.isLocked() ? "ignore" : "replace");
                }
            }
            String level0 = StringUtil.repeat("\t", level);
            String level1 = StringUtil.repeat("\t", level + 1);
            Node parent = loadedNode.getParent();
            boolean done = false;
            while (!done && parent != null && !node.getId().endsWith(" Custom") && parent.getCompCode().length() > 0) {
                if ((parent = parent.getParent()) != null && !parent.isCustom()) continue;
                parent = loadedNode.getParent();
                done = true;
            }
            out.println(level0 + "<node nodeid=\"" + node.getNodeId() + "\" collapseancestor=\"" + (node.isCollapseAncestors() ? "true" : "false") + "\" includeancestor=\"" + (node.isIncludeAncestors() ? "true" : "false") + "\" includedescendants=\"" + (node.isIncludeDescendants() ? "true" : "false") + "\" extendsnodeid=\"" + (parent != null ? parent.getNodeId() : "root") + "\" locked=\"" + (loadedNode.isLocked() ? "true" : "false") + "\" categorylist=\"" + loadedNode.getCategoryList() + "\" exists=\"" + node.getExists() + "\" notexists=\"" + node.getNotexists() + "\" >");
            logger.log(level0 + "Exporting NODE " + node.getNodeId());
            logger.log(level1 + "Import options: " + this.exists + " existing propertylist, " + this.notexists + " missing propertylist.");
            logger.log(level1 + "Import options: " + node.getExists() + " existing node, " + node.getNotexists() + " missing node.");
            PropertyList nodePropertyList = node.getPropertyList();
            PropertyList loadedNodePropertyList = loadedPropertyTree.getNodePropertyList(node.getNodeId(), node.isCollapseAncestors());
            if (loadedNodePropertyList != null && nodePropertyList != null) {
                PropertyListTransfer propertyListTransfer = new PropertyListTransfer();
                propertyListTransfer.putAll(nodePropertyList);
                if (nodePropertyList instanceof PropertyListTransfer) {
                    propertyListTransfer.setExists(((PropertyListTransfer)nodePropertyList).getExists());
                    propertyListTransfer.setNotexists(((PropertyListTransfer)nodePropertyList).getNotexists());
                }
                propertyListTransfer.export(exportFile, out, zipOut, database, level + 1, logger, exported, loadedNodePropertyList.toXMLString());
            }
            out.println(level0 + "</node>");
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean startElementImport(DBAccess database, String elementName, Properties attributes, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("propertytree")) {
            logger.log("Importing PROPERTYTREE: " + this.id + "...");
            if (attributes.getProperty("exists") != null) {
                this.exists = attributes.getProperty("exists");
            }
            if (attributes.getProperty("notexists") == null) return true;
            this.notexists = attributes.getProperty("notexists");
            return true;
        } else if (elementName.equalsIgnoreCase("nodelist")) {
            NodeList nodeList;
            if (this.verbose) {
                logger.log("New NODELIST created");
            }
            this.currentNodeList = nodeList = new NodeList();
            if (this.currentNode == null) {
                this.setNodeList(nodeList);
                return true;
            } else {
                this.currentNode.setNodeList(nodeList);
                nodeList.setParentNode(this.currentNode);
            }
            return true;
        } else if (elementName.equalsIgnoreCase("node")) {
            String id;
            String string = attributes.getProperty("id") != null ? attributes.getProperty("id") : (id = attributes.getProperty("nodeid") != null ? attributes.getProperty("nodeid") : "");
            if (this.getNode(id) != null) throw new SapphireException("Duplicate node id '" + id + "' found in import file");
            Node node = new Node(id);
            node.setLocked("Y".equals(attributes.getProperty("locked")) || "true".equals(attributes.getProperty("locked")));
            node.setParentNodeList(this.currentNodeList);
            node.setExtendsNodeId(attributes.getProperty("extendsnodeid") != null ? attributes.getProperty("extendsnodeid") : "");
            node.setCategoryList(attributes.getProperty("categorylist") != null ? attributes.getProperty("categorylist") : "");
            if (attributes.getProperty("exists") != null) {
                node.setExists(attributes.getProperty("exists"));
            }
            if (attributes.getProperty("notexists") != null) {
                node.setNotexists(attributes.getProperty("notexists"));
            }
            this.currentNode = node;
            if (!this.verbose) return true;
            logger.log("New NODE added '" + id + "'");
            return true;
        } else if (elementName.equalsIgnoreCase("propertylist")) {
            String id;
            String string = attributes.getProperty("id") != null ? attributes.getProperty("id") : (id = attributes.getProperty("propertylistid") != null ? attributes.getProperty("propertylistid") : "");
            if (this.propertyListIndex == 0) {
                PropertyListTransfer propertyListTransfer = new PropertyListTransfer(id);
                propertyListTransfer.setUsePropertyValues(true);
                if (attributes.getProperty("exists") != null) {
                    propertyListTransfer.setExists(attributes.getProperty("exists"));
                }
                if (attributes.getProperty("notexists") != null) {
                    propertyListTransfer.setNotexists(attributes.getProperty("notexists"));
                }
                this.currentNode.setPropertyList(propertyListTransfer);
                this.propertyListBuffer.delete(0, this.propertyListBuffer.length());
                this.propertyListBuffer.append("<propertylist ").append(SaxUtil.getAttributesText(attributes)).append(">\n");
                this.propertyListIndex = 1;
                return true;
            } else {
                this.propertyListBuffer.append("\n<propertylist ").append(SaxUtil.getAttributesText(attributes)).append(">\n");
                ++this.propertyListIndex;
            }
            return true;
        } else if (elementName.equalsIgnoreCase("property")) {
            this.propertyListBuffer.append("<property ").append(SaxUtil.getAttributesText(attributes)).append(">");
            this.currentPropertyType = attributes.getProperty("type") != null ? attributes.getProperty("type") : "simple";
            return true;
        } else if (elementName.equalsIgnoreCase("collection")) {
            this.propertyListBuffer.append("\n<collection>\n");
            return true;
        } else if (elementName.equalsIgnoreCase("propertydeflist")) {
            if (this.propertyDefListIndex == 0) {
                if (!this.propertyDepList) {
                    this.propertyDefListBuffer.delete(0, this.propertyDefListBuffer.length());
                }
                this.propertyDefListBuffer.append("<propertydeflist ").append(SaxUtil.getAttributesText(attributes)).append(">\n");
                this.propertyDefListIndex = 1;
                return true;
            } else {
                this.propertyDefListBuffer.append("\n<propertydeflist ").append(SaxUtil.getAttributesText(attributes)).append(">\n");
                ++this.propertyDefListIndex;
            }
            return true;
        } else if (elementName.equalsIgnoreCase("propertydef")) {
            this.propertyDefListBuffer.append("<propertydef ").append(SaxUtil.getAttributesText(attributes)).append(">");
            return true;
        } else if (elementName.equalsIgnoreCase("propertydeplist")) {
            this.propertyDefListBuffer.append("<propertydeplist ").append(SaxUtil.getAttributesText(attributes)).append(">\n");
            this.propertyDepList = true;
            return true;
        } else if (elementName.equalsIgnoreCase("propertydep")) {
            this.propertyDefListBuffer.append("<propertydep ").append(SaxUtil.getAttributesText(attributes)).append("/>");
            return true;
        } else if (elementName.equalsIgnoreCase("propertydefaultlist")) {
            PropertyDefaultList propertyDefaultList = new PropertyDefaultList(this.lastPropertyDefaultId);
            this.propertyDefaultLists.add(propertyDefaultList);
            return true;
        } else {
            if (!elementName.equalsIgnoreCase("propertydefault")) return false;
            this.lastPropertyDefaultId = attributes.getProperty("id");
            PropertyDefault propertyDefault = new PropertyDefault();
            propertyDefault.setId(attributes.getProperty("id") != null ? attributes.getProperty("id") : "");
            propertyDefault.setType(attributes.getProperty("type") != null ? attributes.getProperty("type") : "");
            propertyDefault.setTranslate(attributes.getProperty(" translate ") != null ? attributes.getProperty("translate") : "");
            ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).setPropertyDefault(propertyDefault.getId(), propertyDefault);
        }
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean endElementImport(DBAccess database, String elementName, String elementCharacters, boolean isCDATA, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("propertytree")) {
            logger.log("Imported PROPERTYTREE");
            if (this.parseOnly) return true;
            try {
                if (this.definition) {
                    PropertyTreeUtil.setPropertyTreeDefinition(database, "(import)", this.id, "<propertytree>\n" + this.propertyDefListBuffer.toString() + "</propertytree>\n");
                } else if (this.rootDefaults) {
                    PropertyTree propertyTree = new PropertyTree();
                    propertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(database, this.id, true));
                    propertyTree.setPropertyDefaultList(this.propertyDefaultList);
                    PropertyTreeUtil.setPropertyTreeValue(database, "(import)", this.id, propertyTree.toXMLString());
                } else if (PropertyTreeUtil.propertyTreeExists(database, this.id)) {
                    if (this.exists.equals("merge")) {
                        PropertyTree propertyTree = new PropertyTree();
                        propertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(database, this.id, true));
                        propertyTree.setDefinitionXML(PropertyTreeUtil.getPropertyTreeDefinition(database, this.id, true));
                        propertyTree.merge(this.getPropertyTree());
                        PropertyTreeUtil.setPropertyTreeValue(database, "(import)", this.id, propertyTree.toXMLString());
                    } else if (this.exists.equals("replace")) {
                        PropertyTreeUtil.setPropertyTreeValue(database, "(import)", this.id, this.toXMLString());
                    }
                } else if (this.exists.equals("add")) {
                    PropertyTreeUtil.setPropertyTreeValue(database, "(import)", this.id, this.toXMLString());
                }
                if (!this.commitScope.equals("table")) return true;
                logger.log("Committing propertytree: " + this.id);
                if (database.getConnection().getAutoCommit()) return true;
                database.getConnection().commit();
                return true;
            }
            catch (Exception e) {
                throw new SapphireException("Unable to save propertytree '" + this.id + "' in file '" + this.getFile().getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
            }
        } else if (elementName.equalsIgnoreCase("nodelist")) {
            this.currentNode = this.currentNodeList.getParentNode();
            if (this.currentNode == null) return true;
            this.currentNodeList = this.currentNode.getParentNodeList();
            return true;
        } else if (elementName.equalsIgnoreCase("node")) {
            this.currentNode = null;
            return true;
        } else {
            if (elementName.equalsIgnoreCase("propertylist")) {
                Node parentNode;
                this.propertyListBuffer.append("</propertylist>\n");
                --this.propertyListIndex;
                if (this.propertyListIndex != 0) return true;
                this.currentNode.getPropertyList().setPropertyList(this.propertyListBuffer.toString());
                if (this.verbose) {
                    logger.log("New PROPERTYLIST added to NODE");
                }
                if ((parentNode = this.currentNode.getParent()) == null) return true;
            }
            if (elementName.equalsIgnoreCase("property")) {
                String propertyValue = elementCharacters.trim();
                if (elementCharacters.trim().length() == 0 && elementCharacters.length() > 0) {
                    propertyValue = StringUtil.repeat(" ", elementCharacters.length());
                }
                this.propertyListBuffer.append(this.currentPropertyType.equals("simple") && propertyValue.length() > 0 ? "<![CDATA[" + propertyValue + "]]>" : "").append("</property>\n");
                return true;
            } else if (elementName.equalsIgnoreCase("collection")) {
                this.propertyListBuffer.append("</collection>\n");
                return true;
            } else if (elementName.equalsIgnoreCase("propertydeflist")) {
                this.propertyDefListBuffer.append("</propertydeflist>\n");
                --this.propertyDefListIndex;
                if (this.propertyDefListIndex != 0) return true;
                this.definition = true;
                return true;
            } else if (elementName.equalsIgnoreCase("propertydef")) {
                this.propertyDefListBuffer.append("</propertydef>\n");
                return true;
            } else if (elementName.equalsIgnoreCase("propertydeplist")) {
                this.propertyDefListBuffer.append("</propertydeplist>\n");
                return true;
            } else {
                if (elementName.equalsIgnoreCase("propertydep")) return true;
                if (elementName.equalsIgnoreCase("propertydefaultlist")) {
                    String propertyDefaultId = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).getPropertyDefaultId();
                    if (propertyDefaultId.length() > 0) {
                        PropertyDefault propertyDefault = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 2)).getPropertyDefault(propertyDefaultId);
                        propertyDefault.setPropertyDefaultList((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1));
                        this.propertyDefaultLists.remove(this.propertyDefaultLists.size() - 1);
                        return true;
                    } else {
                        this.setPropertyDefaultList((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1));
                        this.rootDefaults = true;
                    }
                    return true;
                } else {
                    if (!elementName.equalsIgnoreCase("propertydefault")) return false;
                    PropertyDefault propertyDefault = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).getPropertyDefault(this.lastPropertyDefaultId);
                    if (propertyDefault != null) {
                        propertyDefault.setValue(elementCharacters.trim());
                    }
                    this.lastPropertyDefaultId = "";
                }
            }
        }
        return true;
    }

    public PropertyTree getPropertyTree() {
        PropertyTree propertyTree = new PropertyTree();
        propertyTree.setId(this.id);
        propertyTree.setNodeList((NodeList)this.nodeList.clone());
        return propertyTree;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PropertyTreeTransfer copy = new PropertyTreeTransfer(this.id);
        copy.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
        if (this.nodeList != null) {
            copy.setNodeList((NodeList)this.nodeList.clone());
        }
        if (this.propertyDefaultList != null) {
            copy.setPropertyDefaultList(this.propertyDefaultList);
        }
        if (this.propertyDefinitionList != null) {
            copy.setPropertyDefinitionList(this.propertyDefinitionList);
        }
        copy.setExplode(this.explode);
        return copy;
    }

    @Override
    public void generateAntTask(PrintStream out, int level) {
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        out.println(level0 + "<propertytree propertytreeid=\"" + this.id + "\"" + (this.file != null ? " file=\"" + this.file.getName() + "\"" : "") + " exists=\"" + this.exists + "\" notexists=\"" + this.notexists + "\"" + (this.nodeList.size() == 0 ? "/>" : ">"));
        if (this.nodeList.size() > 0) {
            for (Node node : this.nodeList) {
                PropertyList propertylist = node.getPropertyList();
                out.println(level1 + "<node nodeid=\"" + node.getId() + "\" includeancestors=\"" + node.isIncludeAncestors() + "\" includedescendants=\"" + node.isIncludeDescendants() + "\" exists=\"" + node.getExists() + "\" notexists=\"" + node.getNotexists() + "\" " + (propertylist instanceof PropertyListTransfer ? ">" : "/>"));
                if (!(propertylist instanceof PropertyListTransfer)) continue;
                ((PropertyListTransfer)propertylist).generateAntTask(out, level + 3);
                out.println(level1 + "</node>");
            }
            out.println(level0 + "</propertytree>");
        }
    }

    @Override
    public Object getParsedData() {
        return this;
    }

    @Override
    public void setTransferOption(String propertyid, String value) {
        this.transferOptions.setProperty(propertyid, value);
    }

    @Override
    public String getTransferOption(String propertyid) {
        return this.transferOptions.getProperty(propertyid);
    }

    public final String getTransferOption(String propertyid, String defaultvalue) {
        return this.transferOptions.getProperty(propertyid, defaultvalue);
    }

    @Override
    public void setImportTarget(int importTarget) {
    }

    @Override
    public void setImportObject(Object importObject) {
    }

    @Override
    public void setZipFile(File zipFile) {
    }

    @Override
    public void setZipFileEntry(String zipFileEntry) {
    }

    @Override
    public void setImportForceUpdate(boolean importForceUpdate) {
    }
}

