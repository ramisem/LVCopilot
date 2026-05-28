/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.TransferConstants;
import java.util.ArrayList;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PropertyTreeHandler
extends SapphireSaxHandler
implements TransferConstants {
    private PropertyTree propertyTree;
    private NodeList currentNodeList;
    private Node currentNode;
    private StringBuffer propertyListBuffer = new StringBuffer();
    private int propertyListIndex = 0;
    private String currentPropertyType;
    private StringBuffer currentElementChars = new StringBuffer();
    private ArrayList propertyDefaultLists = new ArrayList();
    private String lastPropertyDefaultId = "";
    private boolean createTransferableObjects = false;

    public PropertyTreeHandler(PropertyTree propertyTree) {
        this.propertyTree = propertyTree;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        String err = "";
        Properties attr = this.getAttributes(attributes);
        if (qName.equalsIgnoreCase("propertytree")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("id") != null ? attr.getProperty("id") : "");
            this.log("Start PROPERTYTREE " + id + "...");
            if (id != null && id.length() > 0) {
                this.propertyTree.setId(id);
            }
        } else if (qName.equalsIgnoreCase("nodelist")) {
            NodeList nodeList;
            this.log("Start NODELIST...");
            this.currentNodeList = nodeList = new NodeList();
            if (this.currentNode == null) {
                this.propertyTree.setNodeList(nodeList);
            } else {
                this.currentNode.setNodeList(nodeList);
                nodeList.setParentNode(this.currentNode);
            }
        } else if (qName.equalsIgnoreCase("node")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("nodeid") != null ? attr.getProperty("nodeid") : "");
            this.log("Start NODE " + id + "...");
            if (this.propertyTree.getNode(id) == null) {
                Node node = new Node(id);
                node.setLocked("Y".equals(attr.getProperty("locked")) || "true".equals(attr.getProperty("locked")));
                if (attr.get("exists") != null) {
                    node.setExists((String)attr.get("exists"));
                }
                if (attr.get("notexists") != null) {
                    node.setNotexists((String)attr.get("notexists"));
                }
                node.setParentNodeList(this.currentNodeList);
                node.setExtendsNodeId(attr.getProperty("extendsnodeid") != null ? attr.getProperty("extendsnodeid") : "");
                node.setCategoryList(attr.getProperty("categorylist") != null ? attr.getProperty("categorylist") : "");
                this.currentNode = node;
            } else {
                err = this._xmlFile != null ? "Duplicate node id '" + id + "' found in document " + this._xmlFile.getName() : "Duplicate node id '" + id + "' found";
            }
        } else if (qName.equalsIgnoreCase("propertylist")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("propertylistid") != null ? attr.getProperty("propertylistid") : "");
            this.log("Start PROPERTYLIST " + id + "...");
            if (this.propertyListIndex == 0) {
                PropertyList propertyList;
                if (this.createTransferableObjects) {
                    propertyList = new PropertyListTransfer(id);
                    if (attr.getProperty("exists") != null) {
                        ((PropertyListTransfer)propertyList).setExists(attr.getProperty("exists"));
                    }
                    if (attr.getProperty("notexists") != null) {
                        ((PropertyListTransfer)propertyList).setNotexists(attr.getProperty("notexists"));
                    }
                } else {
                    propertyList = new PropertyList(id);
                }
                propertyList.setUsePropertyValues(true);
                if (attr.getProperty("modulelist") != null && attr.getProperty("modulelist").length() > 0) {
                    propertyList.setAttribute("modulelist", attr.getProperty("modulelist"));
                }
                if (attr.getProperty("rolelist") != null && attr.getProperty("rolelist").length() > 0) {
                    propertyList.setAttribute("modulelist", attr.getProperty("modulelist"));
                }
                this.currentNode.setPropertyList(propertyList);
                this.propertyListBuffer.delete(0, this.propertyListBuffer.length());
                this.propertyListBuffer.append("<propertylist ").append(this.getAttributesText(attr)).append(">\n");
                this.propertyListIndex = 1;
            } else {
                this.propertyListBuffer.append("\n<propertylist ").append(this.getAttributesText(attr)).append(">\n");
                ++this.propertyListIndex;
            }
        } else if (qName.equalsIgnoreCase("property")) {
            this.log("Start PROPERTY " + attr.getProperty("id") + "...");
            this.propertyListBuffer.append("<property ").append(this.getAttributesText(attr)).append(">");
            this.currentPropertyType = attr.getProperty("type") != null ? attr.getProperty("type") : "simple";
        } else if (qName.equalsIgnoreCase("collection")) {
            this.log("Start COLLECTION...");
            this.propertyListBuffer.append("\n<collection>\n");
        } else if (qName.equalsIgnoreCase("propertydefaultlist")) {
            String id = attr.getProperty("id") != null ? attr.getProperty("id") : (attr.getProperty("propertydefaultlistid") != null ? attr.getProperty("propertydefaultlistid") : "");
            this.log("Start PROPERTYDEFAULTLIST " + id + "...");
            PropertyDefaultList propertyDefaultList = new PropertyDefaultList(this.lastPropertyDefaultId);
            this.propertyDefaultLists.add(propertyDefaultList);
        } else if (qName.equalsIgnoreCase("propertydefault")) {
            this.log("Start PROPERTYDEFAULT " + attr.getProperty("id") + "...");
            this.lastPropertyDefaultId = attr.getProperty("id");
            PropertyDefault propertyDefault = new PropertyDefault();
            propertyDefault.setId(attr.getProperty("id") != null ? attr.getProperty("id") : "");
            propertyDefault.setType(attr.getProperty("type") != null ? attr.getProperty("type") : "");
            propertyDefault.setTranslate(attr.getProperty("translate") != null ? attr.getProperty("translate") : "");
            ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).setPropertyDefault(propertyDefault.getId(), propertyDefault);
        } else {
            err = "Unrecognized element " + qName + " found in document " + this._xmlFile.getName();
            this.log(err);
        }
        if (err.length() > 0) {
            this.println(err);
            throw new SAXException(err);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String err = "";
        if (qName.equalsIgnoreCase("propertytree")) {
            this.log("End PROPERTYTREE");
            this.returnHandler();
        } else if (qName.equalsIgnoreCase("nodelist")) {
            this.log("End NODELIST");
            this.currentNode = this.currentNodeList.getParentNode();
            if (this.currentNode != null) {
                this.currentNodeList = this.currentNode.getParentNodeList();
            }
        } else if (qName.equalsIgnoreCase("node")) {
            this.log("End NODE ");
            this.currentNode = null;
        } else if (qName.equalsIgnoreCase("propertylist")) {
            this.log("End PROPERTYLIST ");
            this.propertyListBuffer.append("</propertylist>\n");
            --this.propertyListIndex;
            if (this.propertyListIndex == 0) {
                try {
                    this.log(this.propertyListBuffer.toString());
                    this.currentNode.getPropertyList().setPropertyList(this.propertyListBuffer.toString());
                }
                catch (SapphireException se) {
                    throw new SAXException(se);
                }
            }
        } else if (qName.equalsIgnoreCase("property")) {
            this.log("End PROPERTY");
            String propertyValue = this.currentElementChars.toString().trim();
            if (this.currentElementChars.toString().trim().length() == 0 && this.currentElementChars.toString().length() > 0) {
                propertyValue = StringUtil.repeat(" ", this.currentElementChars.length());
            }
            this.propertyListBuffer.append(this.currentPropertyType.equals("simple") && propertyValue.length() > 0 ? "<![CDATA[" + propertyValue + "]]>" : "").append("</property>\n");
        } else if (qName.equalsIgnoreCase("collection")) {
            this.log("End COLLECTION");
            this.propertyListBuffer.append("</collection>\n");
        } else if (qName.equalsIgnoreCase("propertydefaultlist")) {
            this.log("End PROPERTYDEFAULTLIST");
            String propertyDefaultId = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).getPropertyDefaultId();
            if (propertyDefaultId.length() > 0) {
                PropertyDefault propertyDefault = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 2)).getPropertyDefault(propertyDefaultId);
                propertyDefault.setPropertyDefaultList((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1));
                this.propertyDefaultLists.remove(this.propertyDefaultLists.size() - 1);
            } else {
                this.propertyTree.setPropertyDefaultList((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1));
            }
        } else if (qName.equalsIgnoreCase("propertydefault")) {
            this.log("End PROPERTYDEFAULT");
            PropertyDefault propertyDefault = ((PropertyDefaultList)this.propertyDefaultLists.get(this.propertyDefaultLists.size() - 1)).getPropertyDefault(this.lastPropertyDefaultId);
            if (propertyDefault != null) {
                propertyDefault.setValue(this.currentElementChars.toString().trim());
            }
            this.lastPropertyDefaultId = "";
        } else {
            err = "Unrecognized element " + qName + " found in document " + this._xmlFile.getName();
            this.log(err);
        }
        if (err.length() > 0) {
            this.println(err);
            throw new SAXException(err);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    public void setCreateTransferableObjects(boolean createTransferableObjects) {
        this.createTransferableObjects = createTransferableObjects;
    }
}

