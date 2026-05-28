/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xpath.XPathAPI
 */
package sapphire.xml;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.FormatUtil;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class PropertyList
extends HashMap
implements Cloneable,
Comparable,
JSONable {
    private String propertyValuePrefix = "__";
    protected String id = "";
    private String propertyTreeNodeId;
    protected HashMap attributes = null;
    private String previousSequence = null;
    private String previousRoleList = null;
    private String previousModuleList = null;
    private String roleListNodeid = null;
    private String moduleListNodeid = null;
    private FormatUtil formatUtil = FormatUtil.getInstance();
    private DateFormat dateFormat = DateFormat.getInstance();
    private TimeZone timezone = null;
    private String guiMode = "";
    protected boolean usePropertyValues = false;
    private String language = null;
    private transient TranslationProcessor translationProcessor = null;
    private String databaseid = null;
    private String dbms = "ORA";
    private PropertyValue parentPropertyValue = null;
    private static final String SEQUENCE = "sequence";
    public static final String ALL_ROLES = "<ALL>";
    public static final String ALL_MODULES = "<ALL>";
    public static final String ENCRYPT_PREFIX = "__!ENC!__";
    public static final String JSON_PROPERTYLISTID = "__propertylistid";
    public static final String JSON_PROPERTYLISTSEQUENCE = "__propertylistsequence";
    public static final String JSON_PROPERTYLISTATTRIBUTES = "__propertylistattributes";

    public PropertyList() {
    }

    public PropertyList(String id) {
        this.setId(id);
    }

    public PropertyList(HashMap mapProps) {
        this.putAll(mapProps);
    }

    public PropertyList(com.labvantage.sapphire.PropertyList propertyList) {
        this.addPropertyList(propertyList);
    }

    public PropertyList(JSONObject jsonObject) {
        this.addJSONObjectToPropertyList(this, jsonObject);
    }

    public PropertyList(JsonObject jsonObject) {
        this.addJsonObjectToPropertyList(this, jsonObject);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setUsePropertyValues(boolean usePropertyValues) {
        this.usePropertyValues = usePropertyValues;
    }

    public void setGuiMode(String guiMode) {
        this.guiMode = guiMode;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public PropertyValue getParentPropertyValue() {
        return this.parentPropertyValue;
    }

    private void setParentPropertyValue(PropertyValue parentPropertyValue) {
        this.parentPropertyValue = parentPropertyValue;
    }

    public void setTranslationProcessor(TranslationProcessor tp) {
        this.translationProcessor = tp;
    }

    private void setPropertyTreeNodeId(String propertyTreeNodeId) {
        this.propertyTreeNodeId = propertyTreeNodeId;
    }

    public String getPropertyTreeNodeId() {
        return this.propertyTreeNodeId;
    }

    public void setAttributes(HashMap attributes) {
        this.setAttributes(attributes, this.getPropertyTreeNodeId());
    }

    private void setAttributes(HashMap attributes, String propertyTreeNodeid) {
        if (this.usePropertyValues && this.attributes != null) {
            String sequence;
            String modulelist;
            String rolelist = (String)this.attributes.get("rolelist");
            if (rolelist != null && rolelist.length() > 0) {
                this.previousRoleList = rolelist;
            }
            if ((modulelist = (String)this.attributes.get("modulelist")) != null && modulelist.length() > 0) {
                this.previousModuleList = modulelist;
            }
            if ((sequence = (String)this.attributes.get(SEQUENCE)) != null && sequence.length() > 0) {
                this.previousSequence = sequence;
            }
        }
        this.attributes = attributes;
        if (this.getAttribute("rolelist").length() > 0) {
            this.roleListNodeid = propertyTreeNodeid;
        }
        if (this.getAttribute("modulelist").length() > 0) {
            this.moduleListNodeid = propertyTreeNodeid;
        }
    }

    public void setAttribute(String id, String value) {
        this.setAttribute(id, value, false);
    }

    public void setAttribute(String id, String value, boolean deep) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        if (value == null) {
            this.attributes.remove(id);
        } else if (SEQUENCE.equals(id)) {
            if (value != null && value.length() > 0 && !value.equals("-1")) {
                String sequence = (String)this.attributes.get(SEQUENCE);
                if (sequence != null && sequence.length() > 0) {
                    this.previousSequence = sequence;
                }
                this.attributes.put(SEQUENCE, value);
            }
        } else {
            if (id.equalsIgnoreCase("rolelist") && value.length() > 0) {
                this.roleListNodeid = this.getPropertyTreeNodeId();
            } else if (id.equalsIgnoreCase("modulelist") && value.length() > 0) {
                this.moduleListNodeid = value;
            }
            this.attributes.put(id, value);
        }
        if (deep) {
            Set s = this.keySet();
            for (String propertyid : s) {
                if (this.isCollection(propertyid)) {
                    PropertyListCollection c = this.getCollection(propertyid);
                    for (PropertyList propertyList : c) {
                        propertyList.setAttribute(id, value, true);
                    }
                    continue;
                }
                if (!this.isPropertyList(propertyid)) continue;
                PropertyList propertyList = this.getPropertyList(propertyid);
                propertyList.setAttribute(id, value, true);
            }
        }
    }

    public void mergeAttributes(HashMap attributes) {
        this.mergeAttributes(attributes, this.getPropertyTreeNodeId());
    }

    public void mergeAttributes(HashMap attributes, String propertyTreeNodeid) {
        if (this.usePropertyValues && this.attributes != null) {
            String sequence;
            String modulelist;
            String rolelist = (String)this.attributes.get("rolelist");
            if (rolelist != null && rolelist.length() > 0) {
                this.previousRoleList = rolelist;
            }
            if ((modulelist = (String)this.attributes.get("modulelist")) != null && modulelist.length() > 0) {
                this.previousModuleList = modulelist;
            }
            if ((sequence = (String)this.attributes.get(SEQUENCE)) != null && sequence.length() > 0) {
                this.previousSequence = sequence;
            }
        }
        if (attributes.size() > 0) {
            if (this.attributes == null) {
                this.attributes = new HashMap();
            }
            this.attributes.putAll(attributes);
        }
        if (attributes.get("rolelist") != null && ((String)attributes.get("rolelist")).length() > 0) {
            this.roleListNodeid = propertyTreeNodeid;
        }
        if (attributes.get("modulelist") != null && ((String)attributes.get("modulelist")).length() > 0) {
            this.moduleListNodeid = propertyTreeNodeid;
        }
    }

    public HashMap getAttributes() {
        return this.attributes;
    }

    public String getAttribute(String attributeId) {
        if (this.attributes == null) {
            return "";
        }
        Object o = this.attributes.get(attributeId);
        return o != null && o instanceof String ? (String)o : "";
    }

    public String getPreviousSequence() {
        return this.previousSequence;
    }

    public String getPreviousRoleList() {
        return this.previousRoleList;
    }

    public String getPreviousModuleList() {
        return this.previousModuleList;
    }

    public String getRoleListNodeid() {
        return this.roleListNodeid;
    }

    public String getModuleListNodeid() {
        return this.moduleListNodeid;
    }

    public void setSequence(long seq) {
        if (seq >= 0L) {
            this.setAttribute(SEQUENCE, Long.toString(seq));
        }
    }

    public void setSequence(String seq) {
        if (seq == null || seq.length() == 0) {
            this.setSequence(-1L);
        }
        try {
            this.setSequence(Long.parseLong(seq));
        }
        catch (Exception e) {
            this.setSequence(-1L);
        }
    }

    public long getSequence() {
        String sequence = this.getAttribute(SEQUENCE);
        if (sequence == null || sequence.length() == 0) {
            return -1L;
        }
        try {
            return Long.parseLong(sequence);
        }
        catch (Exception e) {
            return -1L;
        }
    }

    public void setPropertyList(File file) throws SapphireException {
        this.setPropertyList(file, false);
    }

    public void setPropertyList(File file, boolean merge) throws SapphireException {
        this.setPropertyList(file, merge, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyList(File file, boolean merge, boolean cache) throws SapphireException {
        if (file != null && file.exists()) {
            Element propertyList;
            Element element = propertyList = DOMUtil.getNewDocument(file, cache).getDocumentElement();
            synchronized (element) {
                this.addPropertyList(propertyList, merge, "");
            }
        }
    }

    public void setPropertyList(String xml) throws SapphireException {
        this.setPropertyList(xml, false, true);
    }

    public void setPropertyList(String xml, boolean merge) throws SapphireException {
        this.setPropertyList(xml, merge, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyList(String xml, boolean merge, String propertyTreeNodeid, boolean cache) throws SapphireException {
        if (xml != null && xml.length() > 0) {
            Element propertyList;
            Element element = propertyList = DOMUtil.getNewDocument(xml, cache).getDocumentElement();
            synchronized (element) {
                this.addPropertyList(propertyList, merge, propertyTreeNodeid);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyList(String xml, boolean merge, boolean cache) throws SapphireException {
        if (xml != null && xml.length() > 0) {
            Element propertyList;
            Element element = propertyList = DOMUtil.getNewDocument(xml, cache).getDocumentElement();
            synchronized (element) {
                this.addPropertyList(propertyList, merge, "");
            }
        }
    }

    @Deprecated
    public void setPropertyTree(File file, String nodeId) throws SapphireException {
        this.setPropertyTree(file, nodeId, null);
    }

    public void setPropertyTree(File file, String nodeId, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        this.setPropertyTree(file, nodeId, true, propertyDefinitionList);
    }

    @Deprecated
    public void setPropertyTree(String xml, String nodeId) throws SapphireException {
        this.setPropertyTree(xml, nodeId, null);
    }

    public void setPropertyTree(String xml, String nodeId, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        this.setPropertyTree(xml, nodeId, true, propertyDefinitionList);
    }

    @Deprecated
    public void setPropertyTree(File file, String nodeId, boolean setDefaults) throws SapphireException {
        this.setPropertyTree(file, nodeId, setDefaults, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyTree(File file, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        Element contextNode;
        Element element = contextNode = DOMUtil.getNewDocument(file).getDocumentElement();
        synchronized (element) {
            this.addPropertyTreeNode(contextNode, nodeId, setDefaults, propertyDefinitionList);
        }
    }

    @Deprecated
    public void setPropertyTree(String xml, String nodeId, boolean setDefaults) throws SapphireException {
        this.setPropertyTree(xml, nodeId, setDefaults, null);
    }

    public void setPropertyTree(String xml, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        this.setPropertyTree(xml, nodeId, setDefaults, true, propertyDefinitionList);
    }

    @Deprecated
    public void setPropertyTree(String xml, String nodeId, boolean setDefaults, boolean cache) throws SapphireException {
        this.setPropertyTree(xml, nodeId, setDefaults, cache, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyTree(String xml, String nodeId, boolean setDefaults, boolean cache, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        Element contextNode;
        Element element = contextNode = DOMUtil.getNewDocument(xml, cache).getDocumentElement();
        synchronized (element) {
            this.addPropertyTreeNode(contextNode, nodeId, setDefaults, propertyDefinitionList);
        }
    }

    @Deprecated
    public void setPropertyTree(Node contextNode, String nodeId, boolean setDefaults) throws SapphireException {
        this.setPropertyTree(contextNode, nodeId, setDefaults, null);
    }

    public void setPropertyTree(Node contextNode, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        this.addPropertyTreeNode(contextNode, nodeId, setDefaults, propertyDefinitionList);
    }

    public void addPropertyList(com.labvantage.sapphire.PropertyList propertyList) {
        for (int i = 0; i < propertyList.propertyid.length; ++i) {
            this.put(propertyList.propertyid[i].toLowerCase(), propertyList.propertyvalue[i]);
        }
    }

    public void setProperty(String propertyId, String value) {
        if (propertyId != null && propertyId.length() > 0) {
            if (this.usePropertyValues) {
                if (this.get(propertyId) == null || value == null || !value.equals("[" + this.get(propertyId) + "]")) {
                    this.put(propertyId, value);
                    PropertyValue pv = new PropertyValue(propertyId, false, this);
                    pv.value = value;
                    this.put(this.propertyValuePrefix + propertyId, pv);
                }
            } else {
                this.put(propertyId, value);
            }
        }
    }

    public void deleteProperty(String propertyid) {
        this.remove(propertyid);
        this.remove(this.propertyValuePrefix + propertyid);
    }

    public void setProperty(String propertyId, PropertyList value) {
        if (propertyId != null && propertyId.length() > 0) {
            this.put(propertyId, value);
        }
    }

    public void setProperty(String propertyId, PropertyListCollection value) {
        if (propertyId != null && propertyId.length() > 0) {
            this.put(propertyId, value);
        }
    }

    private void addProperty(Node property, boolean merge, String propertyTreeNodeId) throws SapphireException {
        String id = ((Element)property).getAttribute("id");
        String type = ((Element)property).getAttribute("type");
        if (type.equals("") || type.equals("simple")) {
            PropertyValue pv = null;
            if (this.usePropertyValues) {
                pv = new PropertyValue(id, false, this);
                pv.setPropertyTreeNodeId(propertyTreeNodeId);
                pv.setAttributes(DOMUtil.getAttributes((Element)property));
            }
            if (property.getFirstChild() == null) {
                this.put(id, null);
                if (this.usePropertyValues) {
                    this.put(this.propertyValuePrefix + id, pv);
                }
            } else if (this.usePropertyValues) {
                String value = property.getFirstChild().getNodeValue();
                String currentvalue = (String)this.get(id);
                if (currentvalue == null || value == null || !value.equals("[" + currentvalue + "]")) {
                    pv.value = value = value.replaceAll("!]!]!>", "]]>");
                    this.put(this.propertyValuePrefix + id, pv);
                    this.put(id, value);
                }
            } else {
                String valstr = property.getFirstChild().getNodeValue();
                if (valstr.indexOf("!]!]!>") != -1) {
                    valstr = valstr.replaceAll("!]!]!>", "]]>");
                }
                this.put(id, valstr);
            }
        } else if (type.equals("propertylist")) {
            Element propertyList;
            PropertyValue pv = null;
            if (this.usePropertyValues) {
                pv = new PropertyValue(id, false, this);
                pv.setPropertyTreeNodeId(propertyTreeNodeId);
                pv.setAttributes(DOMUtil.getAttributes((Element)property));
                this.put(this.propertyValuePrefix + id, pv);
            }
            if ((propertyList = (Element)DOMUtil.getChildElement(property, "propertylist")) == null) {
                propertyList = property.getOwnerDocument().createElement("propertylist");
            }
            if (merge) {
                PropertyList currentPropertyList = this.getPropertyList(id);
                if (currentPropertyList == null || currentPropertyList.size() == 0) {
                    this.put(id, this.createNewPropertyList(propertyList, propertyTreeNodeId, pv));
                } else {
                    currentPropertyList.addPropertyList(propertyList, true, propertyTreeNodeId);
                }
            } else {
                this.put(id, this.createNewPropertyList(propertyList, propertyTreeNodeId, pv));
            }
        } else if (type.equals("collection")) {
            PropertyValue pv = null;
            if (this.usePropertyValues) {
                pv = new PropertyValue(id, false, this);
                pv.setPropertyTreeNodeId(propertyTreeNodeId);
                pv.setAttributes(DOMUtil.getAttributes((Element)property));
                this.put(this.propertyValuePrefix + id, pv);
            }
            if (merge) {
                PropertyListCollection currentCollection = this.getCollection(id);
                if (currentCollection == null || currentCollection.size() == 0) {
                    this.put(id, this.createNewCollection(property, propertyTreeNodeId, pv));
                } else {
                    for (Element propertyListNode : DOMUtil.getChildElements(DOMUtil.getChildElement(property, "collection"), "propertylist")) {
                        boolean found = false;
                        for (int i = 0; i < currentCollection.size() && !found; ++i) {
                            PropertyList currentPL = currentCollection.getPropertyList(i);
                            if (currentPL.getId().length() <= 0 || propertyListNode.getAttribute("id").length() <= 0 || !currentPL.getId().equals(propertyListNode.getAttribute("id"))) continue;
                            found = true;
                            currentPL.addPropertyList(propertyListNode, true, propertyTreeNodeId);
                        }
                        if (found) continue;
                        int seq = -1;
                        try {
                            seq = Integer.parseInt(propertyListNode.getAttribute(SEQUENCE));
                        }
                        catch (NumberFormatException currentPL) {
                            // empty catch block
                        }
                        if (seq == -1) {
                            currentCollection.add(this.createNewPropertyList(propertyListNode, propertyTreeNodeId, pv));
                            continue;
                        }
                        found = false;
                        for (int i = 0; i < currentCollection.size() && !found; ++i) {
                            PropertyList currentPL = currentCollection.getPropertyList(i);
                            if (currentPL.getSequence() <= (long)seq) continue;
                            currentCollection.add(i, this.createNewPropertyList(propertyListNode, propertyTreeNodeId, pv));
                            found = true;
                        }
                        if (found) continue;
                        currentCollection.add(this.createNewPropertyList(propertyListNode, propertyTreeNodeId, pv));
                    }
                    Collections.sort(currentCollection);
                    this.put(id, currentCollection);
                }
            } else {
                this.put(id, this.createNewCollection(property, propertyTreeNodeId, pv));
            }
        }
    }

    private PropertyListCollection createNewCollection(Node property, String propertyTreeNodeId, PropertyValue parentPropertyValue) throws SapphireException {
        Element collectionNode = (Element)DOMUtil.getChildElement(property, "collection");
        PropertyListCollection newCollection = new PropertyListCollection();
        newCollection.setId(collectionNode.getAttribute("id"));
        newCollection.setPropertyTreeNodeId(propertyTreeNodeId);
        newCollection.setAttributes(DOMUtil.getAttributes(collectionNode));
        for (Element propertyList : DOMUtil.getChildElements(collectionNode, "propertylist")) {
            PropertyList newPL = this.createNewPropertyList(propertyList, propertyTreeNodeId, parentPropertyValue);
            if (newPL == null) continue;
            boolean found = false;
            for (int i = 0; i < newCollection.size() && !found; ++i) {
                PropertyList currentPL = newCollection.getPropertyList(i);
                if (currentPL.getSequence() <= newPL.getSequence()) continue;
                newCollection.add(i, newPL);
                found = true;
            }
            if (found) continue;
            newCollection.add(newPL);
        }
        return newCollection;
    }

    private PropertyList createNewPropertyList(Element propertyListNode, String propertyTreeNodeId, PropertyValue parentPropertyValue) throws SapphireException {
        PropertyList newPropertyList;
        if (this instanceof PropertyListTransfer) {
            newPropertyList = new PropertyListTransfer(propertyListNode.getAttribute("id"));
            ((PropertyListTransfer)newPropertyList).setExists(((PropertyListTransfer)this).getExists());
            ((PropertyListTransfer)newPropertyList).setNotexists(((PropertyListTransfer)this).getNotexists());
        } else {
            newPropertyList = new PropertyList(propertyListNode.getAttribute("id"));
        }
        newPropertyList.setUsePropertyValues(this.usePropertyValues);
        newPropertyList.setLanguage(this.language);
        newPropertyList.setPropertyTreeNodeId(propertyTreeNodeId);
        newPropertyList.setSequence(propertyListNode.getAttribute(SEQUENCE));
        newPropertyList.setAttribute("nodelist", propertyListNode.getAttribute("nodelist"));
        newPropertyList.addPropertyList(propertyListNode, false, propertyTreeNodeId);
        newPropertyList.setParentPropertyValue(parentPropertyValue);
        return newPropertyList;
    }

    public void addPropertyList(Node propertyList, boolean merge, String propertyTreeNodeId) throws SapphireException {
        if (propertyList.getNodeName().equals("propertylist")) {
            if (merge) {
                this.mergeAttributes(DOMUtil.getAttributes((Element)propertyList), propertyTreeNodeId);
            } else {
                this.setAttributes(DOMUtil.getAttributes((Element)propertyList), propertyTreeNodeId);
            }
            for (Node property = propertyList.getFirstChild(); property != null; property = property.getNextSibling()) {
                if (property.getNodeType() != 1 || !property.getNodeName().equals("property")) continue;
                this.addProperty(property, merge, propertyTreeNodeId);
            }
        } else {
            throw new SapphireException("addPropertyList root node must be <propertylist>");
        }
    }

    private void addPropertyTreeNode(Node contextNode, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        try {
            Node node = XPathAPI.selectSingleNode((Node)contextNode, (String)("//node[@id='" + nodeId + "']"));
            if (node != null) {
                ArrayList<Node> nodeList = new ArrayList<Node>();
                nodeList.add(node);
                for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
                    if (!parent.getNodeName().equals("node")) continue;
                    nodeList.add(parent);
                }
                for (int i = nodeList.size() - 1; i >= 0; --i) {
                    Element treeNode = (Element)nodeList.get(i);
                    Iterator it = DOMUtil.getChildElements(treeNode, "propertylist").iterator();
                    while (it.hasNext()) {
                        this.addPropertyList((Node)it.next(), true, treeNode.getAttribute("id"));
                    }
                }
                Node defNode = XPathAPI.selectSingleNode((Node)contextNode, (String)"//propertydefaultlist");
                if (setDefaults) {
                    this.setPropertyTreeDefaults(defNode, propertyDefinitionList);
                }
            }
        }
        catch (TransformerException te) {
            throw new SapphireException("TransformerException: " + te.getMessage());
        }
    }

    @Deprecated
    public void setPropertyTreeDefaults(String propertyTree) throws SapphireException {
        this.setPropertyTreeDefaults(propertyTree, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPropertyTreeDefaults(String propertyTree, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        try {
            Document doc;
            Document document = doc = DOMUtil.getNewDocument(propertyTree);
            synchronized (document) {
                Element propertyList = doc.getDocumentElement();
                Node defNode = XPathAPI.selectSingleNode((Node)propertyList, (String)"propertydefaultlist");
                this.setPropertyTreeDefaults(defNode, propertyDefinitionList);
            }
        }
        catch (TransformerException te) {
            throw new SapphireException("TransformerException: " + te.getMessage());
        }
    }

    @Deprecated
    public boolean setPropertyTreeDefaults(Node propertyListDef) throws SapphireException {
        return this.setPropertyTreeDefaults(propertyListDef, null);
    }

    public boolean setPropertyTreeDefaults(Node propertyListDef, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        if (propertyDefinitionList == null) {
            return this.setPropertyTreeDefaultsByPropertyDefaults(propertyListDef);
        }
        return this.setPropertyTreeDefaultsByPropertyDefinition(propertyListDef, propertyDefinitionList);
    }

    private boolean setPropertyTreeDefaultsByPropertyDefaults(Node propertyListDef) {
        List propertyDefs;
        boolean hasdefaults = false;
        List list = propertyDefs = propertyListDef == null ? null : DOMUtil.getChildElements(propertyListDef, "propertydefault");
        if (propertyDefs != null && propertyDefs.size() > 0) {
            for (Element propertyDef : propertyDefs) {
                String id = propertyDef.getAttribute("id");
                String type = propertyDef.getAttribute("type");
                if (type == null || type.equals("simple")) {
                    PropertyValue value;
                    String translate;
                    String def;
                    String string = def = propertyDef.getFirstChild() == null ? "" : propertyDef.getFirstChild().getNodeValue();
                    if (def == null || def.length() == 0) {
                        def = propertyDef.getAttribute("default");
                    }
                    if (def != null && def.length() > 0 && this.getProperty(id).length() == 0) {
                        this.put(id, def);
                        hasdefaults = true;
                        if (this.usePropertyValues) {
                            PropertyValue pv = new PropertyValue(id, true, this);
                            pv.value = def;
                            this.put(this.propertyValuePrefix + id, pv);
                        }
                    }
                    if ((translate = propertyDef.getAttribute("translate")) == null || translate.length() <= 0 || "N".equals(translate)) continue;
                    this.setAttribute(id + "_translationcontext", "Y".equals(translate) ? "W" : translate);
                    if (!this.usePropertyValues || (value = this.getPropertyValue(id)).getAttributes() == null) continue;
                    value.getAttributes().put("translate", translate);
                    continue;
                }
                if (type.equals("propertylist")) {
                    boolean newpropertylist = false;
                    PropertyList propertyList = this.getPropertyList(id);
                    if (propertyList == null) {
                        newpropertylist = true;
                        propertyList = new PropertyList();
                        propertyList.setUsePropertyValues(this.usePropertyValues);
                    }
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    hasdefaults = propertyList.setPropertyTreeDefaultsByPropertyDefaults(DOMUtil.getChildElement(propertyDef, "propertydefaultlist")) | hasdefaults;
                    if (!newpropertylist || !hasdefaults) continue;
                    this.put(id, propertyList);
                    continue;
                }
                if (!type.equals("collection")) continue;
                PropertyListCollection collection = this.getCollection(id);
                if (collection == null) {
                    collection = new PropertyListCollection();
                }
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList propertyList = (PropertyList)collection.get(i);
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    propertyList.setPropertyTreeDefaultsByPropertyDefaults(DOMUtil.getChildElement(propertyDef, "propertydefaultlist"));
                }
            }
        }
        return hasdefaults;
    }

    private boolean setPropertyTreeDefaultsByPropertyDefinition(Node propertyDefaultListNode, PropertyDefinitionList propertyDefinitionList) {
        boolean hasdefaults = false;
        if (propertyDefinitionList != null && propertyDefinitionList.size() > 0) {
            List propertyDefaultList = propertyDefaultListNode == null ? null : DOMUtil.getChildElements(propertyDefaultListNode, "propertydefault");
            for (PropertyDefinition propertyDefinition : propertyDefinitionList) {
                String id = propertyDefinition.getId();
                String type = propertyDefinition.getType();
                Element propertyDefault = DOMUtil.getElementByAttribute("id", id, propertyDefaultList);
                if (type == null || "simple".equals(type)) {
                    PropertyValue value;
                    String translate;
                    String defaultValue;
                    String string = propertyDefault == null ? "" : (defaultValue = propertyDefault.getFirstChild() == null ? "" : propertyDefault.getFirstChild().getNodeValue());
                    if ((defaultValue == null || defaultValue.length() == 0) && propertyDefault != null) {
                        defaultValue = propertyDefault.getAttribute("default");
                    }
                    if (defaultValue == null || defaultValue.length() == 0) {
                        defaultValue = propertyDefinition.getDefaultValue();
                    }
                    if (defaultValue != null && defaultValue.length() > 0 && this.getProperty(id).length() == 0) {
                        this.put(id, defaultValue);
                        hasdefaults = true;
                        if (this.usePropertyValues) {
                            PropertyValue pv = new PropertyValue(id, true, this);
                            pv.value = defaultValue;
                            this.put(this.propertyValuePrefix + id, pv);
                        }
                    }
                    if (propertyDefinition == null || !"Y".equalsIgnoreCase(propertyDefinition.getTranslate())) continue;
                    String string2 = translate = propertyDefault == null ? "" : propertyDefault.getAttribute("translate");
                    if (translate == null || "".equals(translate)) {
                        translate = "W";
                    }
                    if ("N".equals(translate)) continue;
                    this.setAttribute(id + "_translationcontext", "Y".equals(translate) ? "W" : translate);
                    if (!this.usePropertyValues || (value = this.getPropertyValue(id)).getAttributes() == null) continue;
                    value.getAttributes().put("translate", translate);
                    continue;
                }
                if (type.equals("propertylist")) {
                    boolean newpropertylist = false;
                    PropertyList propertyList = this.getPropertyList(id);
                    if (propertyList == null) {
                        newpropertylist = true;
                        propertyList = new PropertyList();
                        propertyList.setUsePropertyValues(this.usePropertyValues);
                    }
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    PropertyDefinitionList subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                    hasdefaults = propertyList.setPropertyTreeDefaultsByPropertyDefinition(propertyDefault == null ? null : DOMUtil.getChildElement(propertyDefault, "propertydefaultlist"), subPropertyDefinitionList) | hasdefaults;
                    if (!newpropertylist || !hasdefaults) continue;
                    this.put(id, propertyList);
                    continue;
                }
                if (!type.equals("collection")) continue;
                PropertyListCollection collection = this.getCollection(id);
                if (collection == null) {
                    collection = new PropertyListCollection();
                }
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList propertyList = (PropertyList)collection.get(i);
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    PropertyDefinitionList subPropertyDefinitionList = propertyDefinition == null ? null : propertyDefinition.getPropertyDefinitionList();
                    propertyList.setPropertyTreeDefaultsByPropertyDefinition(propertyDefault == null ? null : DOMUtil.getChildElement(propertyDefault, "propertydefaultlist"), subPropertyDefinitionList);
                }
            }
        }
        return hasdefaults;
    }

    @Deprecated
    public boolean setPropertyTreeDefaults(PropertyDefaultList propertyDefaultList) throws SapphireException {
        return this.setPropertyTreeDefaults(propertyDefaultList, null);
    }

    public boolean setPropertyTreeDefaults(PropertyDefaultList propertyDefaultList, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        if (propertyDefinitionList == null) {
            return this.setPropertyTreeDefaultsByPropertyDefaults(propertyDefaultList);
        }
        return this.setPropertyTreeDefaultsByPropertyDefinition(propertyDefaultList, propertyDefinitionList);
    }

    private boolean setPropertyTreeDefaultsByPropertyDefaults(PropertyDefaultList propertyDefaultList) {
        boolean hasdefaults = false;
        if (propertyDefaultList != null) {
            for (PropertyDefault propertyDefault : propertyDefaultList.values()) {
                String id = propertyDefault.getId();
                String type = propertyDefault.getType();
                if (type == null || type.equals("simple")) {
                    PropertyValue value;
                    String defaultValue = propertyDefault.getValue();
                    if (defaultValue != null && defaultValue.length() > 0 && this.getProperty(id).length() == 0) {
                        this.put(id, defaultValue);
                        hasdefaults = true;
                        if (this.usePropertyValues) {
                            PropertyValue pv = new PropertyValue(id, true, this);
                            pv.value = defaultValue;
                            this.put(this.propertyValuePrefix + id, pv);
                        }
                    }
                    if (propertyDefault.getTranslate() == null || propertyDefault.getTranslate().length() <= 0 || "N".equals(propertyDefault.getTranslate())) continue;
                    this.setAttribute(id + "_translationcontext", "Y".equals(propertyDefault.getTranslate()) ? "W" : propertyDefault.getTranslate());
                    if (!this.usePropertyValues || (value = this.getPropertyValue(id)).getAttributes() == null) continue;
                    value.getAttributes().put("translate", propertyDefault.getTranslate());
                    continue;
                }
                if (type.equals("propertylist")) {
                    boolean newpropertylist = false;
                    PropertyList propertyList = this.getPropertyList(id);
                    if (propertyList == null) {
                        newpropertylist = true;
                        propertyList = new PropertyList();
                        propertyList.setUsePropertyValues(this.usePropertyValues);
                    }
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    hasdefaults = propertyList.setPropertyTreeDefaultsByPropertyDefaults(propertyDefault.getPropertyDefaultList()) | hasdefaults;
                    if (!newpropertylist || !hasdefaults) continue;
                    this.put(id, propertyList);
                    continue;
                }
                if (!type.equals("collection")) continue;
                PropertyListCollection collection = this.getCollection(id);
                if (collection == null) {
                    collection = new PropertyListCollection();
                }
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList propertyList = (PropertyList)collection.get(i);
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    propertyList.setPropertyTreeDefaultsByPropertyDefaults(propertyDefault.getPropertyDefaultList());
                }
            }
        }
        return hasdefaults;
    }

    private boolean setPropertyTreeDefaultsByPropertyDefinition(PropertyDefaultList propertyDefaultList, PropertyDefinitionList propertyDefinitionList) {
        boolean hasdefaults = false;
        if (propertyDefinitionList != null && propertyDefinitionList.size() > 0) {
            for (PropertyDefinition propertyDefinition : propertyDefinitionList) {
                PropertyDefault propertyDefault;
                String id = propertyDefinition.getId();
                String type = propertyDefinition.getType();
                PropertyDefault propertyDefault2 = propertyDefault = propertyDefaultList == null ? null : propertyDefaultList.getPropertyDefault(id);
                if (type == null || type.equals("simple")) {
                    PropertyValue value;
                    String translate;
                    String defaultValue;
                    String string = defaultValue = propertyDefault == null ? "" : propertyDefault.getValue();
                    if (defaultValue == null || defaultValue.length() == 0) {
                        defaultValue = propertyDefinition.getDefaultValue();
                    }
                    if (defaultValue != null && defaultValue.length() > 0 && this.getProperty(id).length() == 0) {
                        this.put(id, defaultValue);
                        hasdefaults = true;
                        if (this.usePropertyValues) {
                            PropertyValue pv = new PropertyValue(id, true, this);
                            pv.value = defaultValue;
                            this.put(this.propertyValuePrefix + id, pv);
                        }
                    }
                    if (propertyDefinition == null || !"Y".equalsIgnoreCase(propertyDefinition.getTranslate()) || "N".equals(translate = propertyDefault == null ? "" : propertyDefault.getTranslate())) continue;
                    this.setAttribute(id + "_translationcontext", "Y".equals(translate) ? "W" : translate);
                    if (!this.usePropertyValues || (value = this.getPropertyValue(id)).getAttributes() == null) continue;
                    value.getAttributes().put("translate", translate);
                    continue;
                }
                if (type.equals("propertylist")) {
                    boolean newpropertylist = false;
                    PropertyList propertyList = this.getPropertyList(id);
                    if (propertyList == null) {
                        newpropertylist = true;
                        propertyList = new PropertyList();
                        propertyList.setUsePropertyValues(this.usePropertyValues);
                    }
                    PropertyDefinitionList subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    hasdefaults = propertyList.setPropertyTreeDefaultsByPropertyDefinition(propertyDefault == null ? null : propertyDefault.getPropertyDefaultList(), subPropertyDefinitionList) | hasdefaults;
                    if (!newpropertylist || !hasdefaults) continue;
                    this.put(id, propertyList);
                    continue;
                }
                if (!type.equals("collection")) continue;
                PropertyDefinitionList subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                PropertyListCollection collection = this.getCollection(id);
                if (collection == null) {
                    collection = new PropertyListCollection();
                }
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList propertyList = (PropertyList)collection.get(i);
                    propertyList.setLanguage(this.language);
                    propertyList.setTranslationProcessor(this.translationProcessor);
                    propertyList.setPropertyTreeDefaultsByPropertyDefinition(propertyDefault == null ? null : propertyDefault.getPropertyDefaultList(), subPropertyDefinitionList);
                }
            }
        }
        return hasdefaults;
    }

    public String toXMLString() {
        String xml = this.toXMLString("<ALL>", "<ALL>");
        if (xml == null || xml.length() == 0) {
            xml = "<propertylist/>";
        }
        return xml;
    }

    public String toXMLString(int level) {
        String xml = this.toXMLString("<ALL>", "<ALL>", level);
        if (xml == null || xml.length() == 0) {
            xml = "<propertylist/>";
        }
        return xml;
    }

    public String toXMLString(String roleList, String moduleList) {
        String xml = this.toXMLString(roleList, moduleList, 0);
        if (xml == null || xml.length() == 0) {
            xml = "<propertylist/>";
        }
        return xml;
    }

    private String toXMLString(String roleList, String moduleList, int level) {
        String indent;
        String thisRoleList = this.attributes == null ? null : (String)this.attributes.get("rolelist");
        String thisModuleList = this.attributes == null ? null : (String)this.attributes.get("modulelist");
        String string = indent = level > 0 ? StringUtil.repeat("\t", level) : "";
        if ((this.attributes == null || thisModuleList == null || thisModuleList.length() == 0 || this.hasAccess(moduleList, thisModuleList, null)) && (this.attributes == null || thisRoleList == null || thisRoleList.length() == 0 || this.hasAccess(roleList, thisRoleList, null))) {
            StringBuffer xml = new StringBuffer(indent + "<propertylist" + (this.id.length() > 0 ? " id=\"" + this.id + "\"" : "") + (this.attributes == null ? "" : " " + this.getAttributesText(this.attributes, "id;propertylistid")) + (this.size() > 0 ? ">\n" : ""));
            Set keyset = this.keySet();
            for (String propertyid : keyset) {
                Object value = this.get(propertyid);
                if (value instanceof PropertyValue) continue;
                PropertyValue pv = null;
                if (this.usePropertyValues) {
                    pv = this.getPropertyValue(propertyid);
                }
                this.propertyToXML(xml, propertyid, value, pv, roleList, moduleList, level + 1);
            }
            String propertyList = xml.toString() + (this.size() > 0 ? indent + "</propertylist>\n" : "/>\n");
            return propertyList.equals(indent + "<propertylist/>\n") || propertyList.equals(indent + "<propertylist />\n") ? "" : propertyList;
        }
        return "";
    }

    protected void propertyToXML(StringBuffer xml, String propertyid, Object value, PropertyValue pv, String roleList, String moduleList, int level) {
        String indent = level > 0 ? StringUtil.repeat("\t", level) : "";
        xml.append(indent).append("<property id=\"").append(propertyid).append("\" type=\"").append(value instanceof ArrayList ? "collection" : (value instanceof PropertyList ? "propertylist" : "simple")).append("\"").append(pv == null ? "" : " " + this.getAttributesText(pv.getAttributes(), "id;type")).append(">");
        if (value instanceof ArrayList) {
            ArrayList collection = (ArrayList)value;
            if (collection.size() > 0) {
                StringBuffer propertylists = new StringBuffer();
                for (int i = 0; i < collection.size(); ++i) {
                    propertylists.append(((PropertyList)collection.get(i)).toXMLString(roleList, moduleList, level + 2));
                }
                if (propertylists.length() > 0) {
                    xml.append("\n").append(indent).append("\t<collection>\n");
                    xml.append(propertylists);
                    xml.append(indent).append("\t</collection>\n").append(indent);
                } else {
                    xml.append("<collection/>");
                }
            } else {
                xml.append("<collection/>");
            }
        } else if (value instanceof PropertyList) {
            String propertylistvalues = ((PropertyList)value).toXMLString(roleList, moduleList, level + 1);
            if (propertylistvalues != null && propertylistvalues.length() > 0) {
                xml.append("\n").append(propertylistvalues).append(indent);
            }
        } else {
            if (value != null && value.toString().indexOf("]]>") != -1) {
                value = value.toString().replaceAll("]]>", "!]!]!>");
            }
            value = PropertyList.getGuiModeValue((String)value, this.guiMode, "");
            xml.append("<![CDATA[").append(value != null ? (String)value : "").append("]]>");
        }
        xml.append("</property>\n");
    }

    protected String getAttributesText(HashMap attribs, String ignorelist) {
        if (attribs == null) {
            return "";
        }
        ignorelist = ";" + ignorelist + ";";
        StringBuffer text = new StringBuffer();
        for (String id : attribs.keySet()) {
            if (ignorelist.indexOf(";" + id + ";") != -1 || id.indexOf("_translationcontext") >= 0) continue;
            text.append(" ").append(id).append("=\"").append(attribs.get(id)).append("\"");
        }
        return text.length() > 0 ? text.substring(1) : "";
    }

    private boolean hasAccess(String userlist, String propertylist, Set<String> inactiveRoles) {
        if (userlist != null && userlist.equals("<ALL>") || propertylist == null || propertylist.length() == 0) {
            return true;
        }
        if (inactiveRoles != null && inactiveRoles.size() > 0) {
            propertylist = ";" + propertylist + ";";
            for (String role : inactiveRoles) {
                propertylist = propertylist.replaceAll(";" + role + ";", ";");
            }
        }
        if (propertylist.replaceAll(";", "").length() == 0) {
            return true;
        }
        if (userlist == null || userlist.length() == 0) {
            return false;
        }
        propertylist = ";" + propertylist + ";";
        String[] list = StringUtil.split(userlist, ";");
        int listlength = list.length;
        for (int i = 0; i < listlength; ++i) {
            if (propertylist.indexOf(";" + list[i] + ";") < 0) continue;
            return true;
        }
        return false;
    }

    public String getDecryptedProperty(String propertyId) {
        if (this.databaseid == null) {
            throw new RuntimeException("Call getDecryptedProperty is not supported due to databaseid not set. Please call setDatabaseid to set correct databaseid");
        }
        String value = this.getProperty(propertyId, "");
        if (value.startsWith(ENCRYPT_PREFIX)) {
            value = value.substring(ENCRYPT_PREFIX.length());
            return EncryptDecrypt.decrypt(value, this.databaseid);
        }
        return value;
    }

    public String getProperty(String propertyId) {
        return this.getProperty(propertyId, "");
    }

    public String getProperty(String propertyId, String defaultValue) {
        if (propertyId == null) {
            propertyId = "";
        }
        Object value = null;
        int pos = propertyId.indexOf("/");
        if (pos > 0) {
            PropertyList temp = this.getPropertyList(propertyId.substring(0, pos));
            if (temp != null) {
                value = temp.get(propertyId.substring(pos + 1));
            }
        } else {
            value = this.get(propertyId);
        }
        if (value != null) {
            if (value instanceof String) {
                return PropertyList.getGuiModeValue((String)value, this.guiMode, defaultValue);
            }
            if (value instanceof Integer) {
                return ((Integer)value).toString();
            }
            if (value instanceof BigDecimal) {
                return this.formatUtil.format((BigDecimal)value);
            }
            if (value instanceof Calendar) {
                return this.dateFormat.format(((Calendar)value).getTime());
            }
            return value.toString();
        }
        return defaultValue;
    }

    public PropertyValue getPropertyValue(String propertyId) {
        PropertyValue pv = (PropertyValue)this.get(this.propertyValuePrefix + propertyId);
        if (pv == null) {
            pv = new PropertyValue(this.id, false, this);
        }
        return pv;
    }

    public PropertyList getPropertyList(String propertyId) {
        Object value = this.get(propertyId);
        if (value != null && value instanceof PropertyList) {
            if (this.guiMode.length() > 0) {
                ((PropertyList)value).setGuiMode(this.guiMode);
            }
            return (PropertyList)value;
        }
        return null;
    }

    public PropertyListCollection getCollection(String propertyId) {
        Object value = this.get(propertyId);
        if (value != null && value instanceof PropertyListCollection) {
            PropertyListCollection temp = (PropertyListCollection)value;
            for (int i = 0; i < temp.size(); ++i) {
                PropertyList pl = temp.getPropertyList(i);
                if (this.guiMode.length() <= 0) continue;
                pl.setGuiMode(this.guiMode);
            }
            return temp;
        }
        return null;
    }

    public PropertyList getPropertyListNotNull(String propertyId) {
        PropertyList value;
        Object o = this.get(propertyId);
        if (o == null || !(o instanceof PropertyList)) {
            value = new PropertyList();
            value.setUsePropertyValues(this.usePropertyValues);
            value.setGuiMode(this.guiMode);
            this.setProperty(propertyId, value);
        } else {
            value = (PropertyList)o;
            value.setGuiMode(this.guiMode);
        }
        return value;
    }

    public PropertyListCollection getCollectionNotNull(String propertyId) {
        PropertyListCollection value;
        Object o = this.get(propertyId);
        if (o == null || !(o instanceof PropertyListCollection)) {
            value = new PropertyListCollection();
            this.setProperty(propertyId, value);
        } else {
            value = (PropertyListCollection)o;
            for (int i = 0; i < value.size(); ++i) {
                PropertyList pl = value.getPropertyList(i);
                pl.setGuiMode(this.guiMode);
            }
        }
        return value;
    }

    public boolean isSimple(String propertyId) {
        Object value = this.get(propertyId);
        return value != null && value instanceof String;
    }

    public boolean isPropertyList(String propertyId) {
        Object value = this.get(propertyId);
        return value != null && value instanceof PropertyList;
    }

    public boolean isCollection(String propertyId) {
        Object value = this.get(propertyId);
        return value != null && value instanceof PropertyListCollection;
    }

    public String getPropertyTreeNodeId(String propertyId) {
        String propertyTreeNodeId = "";
        Object o = this.get(propertyId);
        if (o instanceof String) {
            propertyTreeNodeId = ((PropertyValue)this.get(this.propertyValuePrefix + propertyId)).getPropertyTreeNodeId();
        } else if (o instanceof PropertyValue) {
            propertyTreeNodeId = ((PropertyValue)o).getPropertyTreeNodeId();
        } else if (o instanceof PropertyList) {
            propertyTreeNodeId = ((PropertyList)o).getPropertyTreeNodeId();
        } else if (o instanceof PropertyListCollection) {
            propertyTreeNodeId = ((PropertyListCollection)o).getPropertyTreeNodeId();
        }
        return propertyTreeNodeId;
    }

    public ArrayList getFilteredCollections(String propertyId, HashMap filterMap) {
        ArrayList<PropertyList> returnValue = new ArrayList<PropertyList>();
        Object value = this.get(propertyId);
        if (value != null && !(value instanceof String)) {
            Set filterSet = filterMap.entrySet();
            ArrayList collection = (ArrayList)value;
            for (int i = 0; i < collection.size(); ++i) {
                PropertyList temp = (PropertyList)collection.get(i);
                Set entrySet = temp.entrySet();
                if (!entrySet.containsAll(filterSet)) continue;
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    public PropertyList copy() {
        return this.copy(this, null, null, "<ALL>", "<ALL>", null, "");
    }

    public PropertyList copy(String rolelist, String modulelist) {
        return this.copy(this, null, null, rolelist, modulelist, null, "");
    }

    public PropertyList copy(String rolelist, String modulelist, Set<String> inactiveRoles) {
        return this.copy(this, null, null, rolelist, modulelist, inactiveRoles, "");
    }

    public PropertyList copy(String languageid, TranslationProcessor tp) {
        return this.copy(this, languageid, tp, "<ALL>", "<ALL>", null, "");
    }

    public PropertyList copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles) {
        return this.copy(this, languageid, tp, rolelist, modulelist, inactiveRoles, "");
    }

    public PropertyList copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode) {
        return this.copy(this, languageid, tp, rolelist, modulelist, inactiveRoles, guimode, false);
    }

    public PropertyList copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode, boolean forceTranslation) {
        return this.copy(this, languageid, tp, rolelist, modulelist, inactiveRoles, guimode, forceTranslation);
    }

    private void processCopyValue(String propertyFrom, String propertyTo, String value, PropertyList newPropertyList, PropertyList copyPropertyList, HashMap copyAttributes, String languageid, TranslationProcessor tp, boolean forceTranslation) {
        value = PropertyList.getDBSyntax(value, this.dbms);
        if (languageid == null || tp == null) {
            newPropertyList.put(propertyTo, value);
        } else {
            String context;
            String string = context = copyAttributes != null && copyAttributes.get(propertyFrom + "_translationcontext") != null ? (String)copyAttributes.get(propertyFrom + "_translationcontext") : "";
            if (context.length() > 0) {
                if (forceTranslation) {
                    context = "W".equals(context) ? tp.getTextType() : context + (!"W".equals(tp.getTextType()) ? "." + tp.getTextType() : "");
                    newPropertyList.put(propertyTo, tp.translate(value, languageid, context));
                } else if (!("Hidden Value".equals(copyPropertyList.get("mode")) || "N".equals(copyPropertyList.get("show")) || "Do Not Retrieve".equals(copyPropertyList.get("mode")) || propertyFrom.equals("text") && value.indexOf(";") > -1 && copyPropertyList.containsKey("js"))) {
                    context = "W".equals(context) ? tp.getTextType() : context + (!"W".equals(tp.getTextType()) ? "." + tp.getTextType() : "");
                    newPropertyList.put(propertyTo, tp.translate(value, languageid, context));
                } else {
                    newPropertyList.put(propertyTo, value);
                }
            } else {
                newPropertyList.put(propertyTo, value);
            }
            String translatedvalue = (String)newPropertyList.get(propertyTo);
            if (translatedvalue.indexOf("{{") >= 0 && translatedvalue.indexOf("}}") > 0) {
                newPropertyList.put(propertyTo, tp.translatePartial(translatedvalue, languageid));
            }
        }
    }

    private PropertyList copy(PropertyList copyPropertyList, String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode) {
        return this.copy(copyPropertyList, languageid, tp, rolelist, modulelist, inactiveRoles, guimode, false);
    }

    private PropertyList copy(PropertyList copyPropertyList, String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode, boolean forceTranslation) {
        String copyModuleList;
        PropertyList newPropertyList = null;
        HashMap copyAttributes = copyPropertyList.getAttributes();
        if (tp != null && languageid != null && languageid.length() > 0) {
            String sdcid = copyPropertyList.getProperty("sdcid");
            if (sdcid.length() == 0) {
                sdcid = this.getProperty("sdcid");
            }
            if (sdcid.length() > 0) {
                tp.setTextType(sdcid);
            }
        }
        String copyRoleList = copyAttributes == null ? null : (String)copyAttributes.get("rolelist");
        String string = copyModuleList = copyAttributes == null ? null : (String)copyAttributes.get("modulelist");
        if ((copyAttributes == null || copyModuleList == null || copyModuleList.length() == 0 || this.hasAccess(modulelist, copyModuleList, null)) && (copyAttributes == null || copyRoleList == null || copyRoleList.length() == 0 || this.hasAccess(rolelist, copyRoleList, inactiveRoles))) {
            newPropertyList = new PropertyList(copyPropertyList.getId());
            newPropertyList.setUsePropertyValues(this.usePropertyValues);
            newPropertyList.setSequence(copyPropertyList.getSequence());
            newPropertyList.roleListNodeid = copyPropertyList.roleListNodeid;
            newPropertyList.moduleListNodeid = copyPropertyList.moduleListNodeid;
            if (copyAttributes != null) {
                newPropertyList.setAttributes(new HashMap(copyAttributes));
            }
            Set keySet = copyPropertyList.keySet();
            for (String property : keySet) {
                Object value = copyPropertyList.get(property);
                if (value == null) continue;
                if (value instanceof String) {
                    if (((String)value).startsWith("$R{") && ((String)value).endsWith("}") && ((String)value).contains(":|:")) {
                        value = PropertyList.getGuiModeValues((String)value, guimode, "");
                    }
                    if (value instanceof PropertyList) {
                        Iterator guimodeit = ((PropertyList)value).keySet().iterator();
                        while (guimodeit.hasNext()) {
                            String breakpoint = guimodeit.next().toString();
                            if (!(((PropertyList)value).getProperty(breakpoint) instanceof String)) continue;
                            this.processCopyValue(property, breakpoint, ((PropertyList)value).getProperty(breakpoint), (PropertyList)value, copyPropertyList, copyAttributes, languageid, tp, forceTranslation);
                        }
                        newPropertyList.setProperty(property, (PropertyList)value);
                        continue;
                    }
                    this.processCopyValue(property, property, value.toString(), newPropertyList, copyPropertyList, copyAttributes, languageid, tp, forceTranslation);
                    continue;
                }
                if (value instanceof PropertyList) {
                    PropertyList copy = this.copy((PropertyList)value, languageid, tp, rolelist, modulelist, inactiveRoles, guimode, forceTranslation);
                    copy.setDbms(this.dbms);
                    copy.setDatabaseid(this.databaseid);
                    if (copy == null) continue;
                    newPropertyList.put(property, copy);
                    continue;
                }
                if (value instanceof PropertyListCollection) {
                    PropertyListCollection newCollection = new PropertyListCollection();
                    newCollection.setId(((PropertyListCollection)value).getId());
                    boolean filtered = false;
                    for (int i = 0; i < ((PropertyListCollection)value).size(); ++i) {
                        PropertyList copy = this.copy(((PropertyListCollection)value).getPropertyList(i), languageid, tp, rolelist, modulelist, inactiveRoles, guimode, forceTranslation);
                        if (copy != null) {
                            newCollection.add(copy);
                        } else {
                            filtered = true;
                        }
                        HashMap<String, String> atts = newCollection.getAttributes();
                        if (!filtered || atts != null && atts.containsKey("filtered")) continue;
                        if (atts == null) {
                            atts = new HashMap<String, String>();
                            newCollection.setAttributes(atts);
                        }
                        atts.put("filtered", "Y");
                    }
                    newPropertyList.put(property, newCollection);
                    continue;
                }
                if (!(value instanceof PropertyValue)) continue;
                PropertyValue from = (PropertyValue)value;
                PropertyValue to = new PropertyValue(from.getId(), from.isDefault(), newPropertyList);
                to.setPropertyTreeNodeId(from.getPropertyTreeNodeId());
                to.value = from.value;
                HashMap fromAttribs = from.getAttributes();
                if (fromAttribs != null) {
                    to.setAttributes(new HashMap(fromAttribs));
                }
                newPropertyList.put(property, to);
            }
        }
        return newPropertyList;
    }

    public Element toElement(Document doc) {
        Element propertyList = doc.createElement("propertylist");
        this.toElement(doc, propertyList);
        return propertyList;
    }

    private void toElement(Document doc, Element newPropertyList) {
        String modulelist;
        newPropertyList.setAttribute("id", this.id);
        newPropertyList.setAttribute(SEQUENCE, "" + this.getSequence());
        String rolelist = this.getAttribute("rolelist");
        if (rolelist.length() > 0) {
            newPropertyList.setAttribute("rolelist", rolelist);
        }
        if ((modulelist = this.getAttribute("modulelist")).length() > 0) {
            newPropertyList.setAttribute("modulelist", modulelist);
        }
        Set keySet = this.keySet();
        for (String propertyid : keySet) {
            if (propertyid.startsWith(this.propertyValuePrefix)) continue;
            Object value = this.get(propertyid);
            Element newProperty = doc.createElement("property");
            newPropertyList.appendChild(newProperty);
            newProperty.setAttribute("id", propertyid);
            if (value == null) continue;
            if (value instanceof String) {
                newProperty.setAttribute("type", "simple");
                newProperty.appendChild(doc.createTextNode((String)value));
                continue;
            }
            if (value instanceof PropertyList) {
                newProperty.setAttribute("type", "propertylist");
                Element subPropertyList = doc.createElement("propertylist");
                newProperty.appendChild(subPropertyList);
                ((PropertyList)value).toElement(doc, subPropertyList);
                continue;
            }
            if (!(value instanceof PropertyListCollection)) continue;
            newProperty.setAttribute("type", "collection");
            Element collection = doc.createElement("collection");
            newProperty.appendChild(collection);
            for (int i = 0; i < ((PropertyListCollection)value).size(); ++i) {
                Element subPropertyList = doc.createElement("propertylist");
                collection.appendChild(subPropertyList);
                ((PropertyListCollection)value).getPropertyList(i).toElement(doc, subPropertyList);
            }
        }
    }

    public String findProperty(String findId) {
        String find = null;
        for (Object k : this.keySet()) {
            String propertyid = (String)k;
            if (this.isCollection(propertyid)) {
                PropertyListCollection c = this.getCollection(propertyid);
                for (int i = 0; i < c.size(); ++i) {
                    PropertyList propertyList = c.getPropertyList(i);
                    find = propertyList.findProperty(findId);
                }
            } else if (this.isPropertyList(propertyid)) {
                PropertyList propertylist = this.getPropertyList(propertyid);
                find = propertylist.findProperty(findId);
            } else if (propertyid.equals(findId)) {
                find = this.getProperty(propertyid, "");
            }
            if (find == null) continue;
            break;
        }
        return find;
    }

    public PropertyList findPropertyList(String findId) {
        if (this.id.equals(findId)) {
            return this;
        }
        PropertyList find = null;
        Set s = this.keySet();
        Iterator it = s.iterator();
        while (find == null && it.hasNext()) {
            String propertyid = (String)it.next();
            if (this.isCollection(propertyid)) {
                PropertyListCollection c = this.getCollection(propertyid);
                Iterator it2 = c.iterator();
                while (find == null && it2.hasNext()) {
                    PropertyList propertyList = (PropertyList)it2.next();
                    find = propertyList.findPropertyList(findId);
                }
                continue;
            }
            if (!this.isPropertyList(propertyid)) continue;
            PropertyList propertylist = this.getPropertyList(propertyid);
            find = propertylist.findPropertyList(findId);
        }
        return find;
    }

    public boolean equals(PropertyList compare) {
        if (compare == null || this.size() != compare.size()) {
            return false;
        }
        for (String property : this.keySet()) {
            Object value = this.get(property);
            if (value == null) {
                if (compare.get(property) == null) continue;
                return false;
            }
            if (value instanceof String) {
                if (compare.get(property) != null && compare.get(property).equals(value)) continue;
                return false;
            }
            if (value instanceof PropertyList) {
                if (compare.getPropertyList(property) != null && compare.getPropertyList(property).equals((PropertyList)value)) continue;
                return false;
            }
            if (value instanceof PropertyListCollection) {
                PropertyListCollection thisCollection = (PropertyListCollection)value;
                PropertyListCollection compareCollection = (PropertyListCollection)compare.get(property);
                if (thisCollection.size() != compareCollection.size()) {
                    return false;
                }
                for (int i = 0; i < compareCollection.size(); ++i) {
                    if (compareCollection.getPropertyList(i).equals(thisCollection.getPropertyList(i))) continue;
                    return false;
                }
                continue;
            }
            if (value instanceof PropertyValue) continue;
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.id != null && this.id.length() > 0 ? this.id : "[PropertyList]";
    }

    private void addJSONObjectToPropertyList(PropertyList proplist, JSONObject jsonObj) {
        this.addJSONObjectToPropertyList(proplist, jsonObj, false);
    }

    private void addJSONObjectToPropertyList(PropertyList proplist, JSONObject jsonObj, boolean blankIsPropertyList) {
        Iterator it = jsonObj.keys();
        while (it.hasNext()) {
            String key = (String)it.next();
            try {
                Object value = jsonObj.get(key);
                if (value instanceof String) {
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTID)) {
                        proplist.setId(jsonObj.getString(JSON_PROPERTYLISTID));
                        continue;
                    }
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTSEQUENCE)) {
                        proplist.setSequence(jsonObj.getString(JSON_PROPERTYLISTSEQUENCE));
                        continue;
                    }
                    proplist.setProperty(key, value.toString());
                    continue;
                }
                if (value instanceof Long || value instanceof Integer) {
                    if (!key.equalsIgnoreCase(JSON_PROPERTYLISTSEQUENCE)) continue;
                    proplist.setSequence(jsonObj.getLong(JSON_PROPERTYLISTSEQUENCE));
                    continue;
                }
                if (value instanceof JSONObject) {
                    Cloneable plc;
                    JSONObject job = (JSONObject)value;
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTATTRIBUTES)) {
                        if (job.length() <= 0) continue;
                        Iterator keys = job.keys();
                        while (keys.hasNext()) {
                            String attribute = (String)keys.next();
                            proplist.setAttribute(attribute, job.getString(attribute));
                        }
                        continue;
                    }
                    if (job.length() == 0) {
                        if (blankIsPropertyList) {
                            plc = new PropertyList();
                            proplist.setProperty(key, (PropertyList)plc);
                            continue;
                        }
                        plc = new PropertyListCollection();
                        proplist.setProperty(key, (PropertyListCollection)plc);
                        continue;
                    }
                    if (job.has("0") && job.get("0") instanceof JSONObject) {
                        plc = new PropertyListCollection();
                        int count = 0;
                        for (int index = 0; index < job.length(); ++index) {
                            String colkey = "" + index;
                            if (!job.has(colkey)) continue;
                            PropertyList pl = new PropertyList();
                            this.addJSONObjectToPropertyList(pl, job.getJSONObject(colkey), blankIsPropertyList);
                            ((ArrayList)plc).add(count, pl);
                            ++count;
                        }
                        proplist.setProperty(key, (PropertyListCollection)plc);
                        continue;
                    }
                    PropertyList pl = new PropertyList();
                    this.addJSONObjectToPropertyList(pl, (JSONObject)value, blankIsPropertyList);
                    proplist.setProperty(key, pl);
                    continue;
                }
                if (!(value instanceof JSONArray)) continue;
                PropertyListCollection plc = new PropertyListCollection();
                JSONArray jsona = (JSONArray)value;
                for (int i = 0; i < jsona.length(); ++i) {
                    PropertyList pl = new PropertyList();
                    this.addJSONObjectToPropertyList(pl, jsona.getJSONObject(i), blankIsPropertyList);
                    plc.add(i, pl);
                }
                proplist.setProperty(key, plc);
            }
            catch (Exception exception) {}
        }
    }

    private void addJsonObjectToPropertyList(PropertyList proplist, JsonObject jsonObj) {
        this.addJsonObjectToPropertyList(proplist, jsonObj, false);
    }

    private void addJsonObjectToPropertyList(PropertyList proplist, JsonObject jsonObj, boolean blankIsPropertyList) {
        for (String key : jsonObj.keys()) {
            try {
                Serializable value = jsonObj.get(key);
                if (value instanceof String) {
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTID)) {
                        proplist.setId(jsonObj.getString(JSON_PROPERTYLISTID));
                        continue;
                    }
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTSEQUENCE)) {
                        proplist.setSequence(jsonObj.getString(JSON_PROPERTYLISTSEQUENCE));
                        continue;
                    }
                    proplist.setProperty(key, value.toString());
                    continue;
                }
                if (value instanceof Long || value instanceof Integer) {
                    if (!key.equalsIgnoreCase(JSON_PROPERTYLISTSEQUENCE)) continue;
                    proplist.setSequence(jsonObj.getLong(JSON_PROPERTYLISTSEQUENCE, 0L));
                    continue;
                }
                if (value instanceof JSONObject) {
                    Cloneable plc;
                    JSONObject job = (JSONObject)((Object)value);
                    if (key.equalsIgnoreCase(JSON_PROPERTYLISTATTRIBUTES)) {
                        if (job.length() <= 0) continue;
                        Iterator keys = job.keys();
                        while (keys.hasNext()) {
                            String attribute = (String)keys.next();
                            proplist.setAttribute(attribute, job.getString(attribute));
                        }
                        continue;
                    }
                    if (job.length() == 0) {
                        if (blankIsPropertyList) {
                            plc = new PropertyList();
                            proplist.setProperty(key, (PropertyList)plc);
                            continue;
                        }
                        plc = new PropertyListCollection();
                        proplist.setProperty(key, (PropertyListCollection)plc);
                        continue;
                    }
                    if (job.has("0") && job.get("0") instanceof JSONObject) {
                        plc = new PropertyListCollection();
                        int count = 0;
                        for (int index = 0; index < job.length(); ++index) {
                            String colkey = "" + index;
                            if (!job.has(colkey)) continue;
                            PropertyList pl = new PropertyList();
                            this.addJSONObjectToPropertyList(pl, job.getJSONObject(colkey), blankIsPropertyList);
                            ((ArrayList)plc).add(count, pl);
                            ++count;
                        }
                        proplist.setProperty(key, (PropertyListCollection)plc);
                        continue;
                    }
                    PropertyList pl = new PropertyList();
                    this.addJSONObjectToPropertyList(pl, (JSONObject)((Object)value), blankIsPropertyList);
                    proplist.setProperty(key, pl);
                    continue;
                }
                if (!(value instanceof JSONArray)) continue;
                PropertyListCollection plc = new PropertyListCollection();
                JSONArray jsona = (JSONArray)((Object)value);
                for (int i = 0; i < jsona.length(); ++i) {
                    PropertyList pl = new PropertyList();
                    this.addJSONObjectToPropertyList(pl, jsona.getJSONObject(i), blankIsPropertyList);
                    plc.add(i, pl);
                }
                proplist.setProperty(key, plc);
            }
            catch (Exception exception) {}
        }
    }

    static void addPropertyListToJSonObject(JSONObject jsonObj, PropertyList proplist, boolean includeAttributes, boolean includeEmpties) {
        if (includeAttributes) {
            try {
                String id = proplist.getId();
                if (id == null || id.length() == 0) {
                    id = "root_0";
                }
                if (proplist.getAttributes() != null) {
                    JSONObject attributes = new JSONObject();
                    for (String attribute : proplist.getAttributes().keySet()) {
                        String value;
                        if (attribute.equalsIgnoreCase(SEQUENCE) || (value = proplist.getAttribute(attribute)).length() <= 0) continue;
                        attributes.put(attribute, value);
                    }
                    if (attributes.length() > 0) {
                        jsonObj.put(JSON_PROPERTYLISTATTRIBUTES, attributes);
                    }
                }
                jsonObj.put(JSON_PROPERTYLISTID, id);
                jsonObj.put(JSON_PROPERTYLISTSEQUENCE, proplist.getSequence());
            }
            catch (Exception id) {
                // empty catch block
            }
        }
        for (String key : proplist.keySet()) {
            Object value = proplist.get(key);
            try {
                PropertyListCollection plcvalue;
                if (value instanceof String) {
                    jsonObj.put(key, PropertyList.getGuiModeValue((String)value, proplist.guiMode, ""));
                    continue;
                }
                if (value instanceof PropertyList) {
                    PropertyList pvalue = (PropertyList)value;
                    if (pvalue.size() <= 0 && !includeEmpties) continue;
                    JSONObject jsonp = new JSONObject();
                    PropertyList.addPropertyListToJSonObject(jsonp, pvalue, includeAttributes, includeEmpties);
                    jsonObj.put(key, jsonp);
                    continue;
                }
                if (!(value instanceof PropertyListCollection) || (plcvalue = (PropertyListCollection)value).size() <= 0 && !includeEmpties) continue;
                JSONArray jsonarr = new JSONArray();
                for (int i = 0; i < plcvalue.size(); ++i) {
                    JSONObject jsonp = new JSONObject();
                    PropertyList.addPropertyListToJSonObject(jsonp, plcvalue.getPropertyList(i), includeAttributes, includeEmpties);
                    jsonarr.put(i, jsonp);
                }
                jsonObj.put(key, jsonarr);
            }
            catch (Exception exception) {}
        }
    }

    static Object getGuiModeValues(String value, String modeid, String defaultValue) {
        if (value != null && value.startsWith("$R{") && value.endsWith("}") && value.contains(":|:")) {
            if (modeid.equals("(Responsive)")) {
                PropertyList retvalue = new PropertyList();
                String[] parts = StringUtil.split(value.substring(3, value.length() - 1), ":|:");
                String partFound = null;
                for (int i = 0; i < parts.length; ++i) {
                    String guimode = parts[i].substring(0, parts[i].indexOf(":"));
                    String partvalue = parts[i].substring(parts[i].indexOf(":") + 1);
                    if (partFound == null) {
                        partFound = partvalue;
                    }
                    retvalue.setProperty(guimode, partvalue.length() > 0 ? partvalue : defaultValue);
                }
                if (retvalue.size() == 0) {
                    retvalue.setProperty("xs", partFound != null && partFound.length() > 0 ? partFound : defaultValue);
                }
                return retvalue;
            }
            return PropertyList.getGuiModeValue(value, modeid, defaultValue);
        }
        return value != null && value.length() > 0 ? value : defaultValue;
    }

    static String getGuiModeValue(String value, String modeid, String defaultValue) {
        if (value != null && modeid.length() > 0 && value.startsWith("$R{") && value.endsWith("}") && value.contains(":|:")) {
            String[] parts = StringUtil.split(value.substring(3, value.length() - 1), ":|:");
            for (int i = 0; i < parts.length; ++i) {
                if (!parts[i].startsWith(modeid + ":")) continue;
                value = parts[i].substring(modeid.length() + 1);
            }
        }
        return value != null && value.length() > 0 ? value : defaultValue;
    }

    public JSONObject toJSONObject() {
        return this.toJSONObject(true, true);
    }

    public JSONObject toJSONObject(boolean includeAttributes) {
        return this.toJSONObject(includeAttributes, true);
    }

    public JSONObject toJSONObject(boolean includeAttributes, boolean includeEmpties) {
        JSONObject jsonObj = new JSONObject();
        PropertyList.addPropertyListToJSonObject(jsonObj, this, includeAttributes, includeEmpties);
        return jsonObj;
    }

    @Override
    public String toJSONString() {
        return this.toJSONString(true, true);
    }

    public String toJSONString(boolean includeAttributes, boolean includeEmpties) {
        return this.toJSONObject(includeAttributes, includeEmpties).toString();
    }

    public String toJSONString(boolean includeAttributes) {
        return this.toJSONObject(includeAttributes, true).toString();
    }

    public void setJSONString(String jsonString) throws JSONException {
        this.addJSONObjectToPropertyList(this, new JSONObject(jsonString));
    }

    public void setJSONString(String jsonString, boolean blankIsPropertyList) throws JSONException {
        this.addJSONObjectToPropertyList(this, new JSONObject(jsonString), blankIsPropertyList);
    }

    public int compareTo(Object o) {
        if (o instanceof PropertyList && o != null) {
            return (int)(this.getSequence() - ((PropertyList)o).getSequence());
        }
        return 0;
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            this.formatUtil = FormatUtil.getInstance(locale);
            this.dateFormat = DateFormat.getDateTimeInstance(3, 3, locale);
        }
    }

    public void setTimeZone(TimeZone timezone) {
        if (timezone != null) {
            this.timezone = timezone;
            this.dateFormat.setTimeZone(timezone);
        }
    }

    private static String getDBSyntax(String input, String dbms) {
        String temp = input;
        if (temp != null && (temp.indexOf("{{ORA:") >= 0 || temp.indexOf("{{MSS:") >= 0) && temp.indexOf("}}") > 0) {
            String[] tokens = StringUtil.getTokens(temp, "{{", "}}");
            for (int t = 0; t < tokens.length; ++t) {
                String replacevalue;
                if (tokens[t].indexOf("ORA:") >= 0 && tokens[t].indexOf("MSS:") >= 0) {
                    if ("MSS".equals(dbms)) {
                        replacevalue = tokens[t].substring(tokens[t].indexOf("MSS:") + 4);
                        if (replacevalue.indexOf("ORA:") >= 0) {
                            replacevalue = replacevalue.substring(0, replacevalue.indexOf("ORA:"));
                        }
                        temp = StringUtil.replaceAll(temp, "{{" + tokens[t] + "}}", replacevalue);
                        continue;
                    }
                    replacevalue = tokens[t].substring(tokens[t].indexOf("ORA:") + 4);
                    if (replacevalue.indexOf("MSS:") >= 0) {
                        replacevalue = replacevalue.substring(0, replacevalue.indexOf("MSS:"));
                    }
                    temp = StringUtil.replaceAll(temp, "{{" + tokens[t] + "}}", replacevalue);
                    continue;
                }
                if (tokens[t].indexOf("ORA:") >= 0) {
                    replacevalue = tokens[t].substring(tokens[t].indexOf("ORA:") + 4);
                    temp = StringUtil.replaceAll(temp, "{{" + tokens[t] + "}}", "ORA".equals(dbms) ? replacevalue : "");
                    continue;
                }
                if (tokens[t].indexOf("MSS:") < 0) continue;
                replacevalue = tokens[t].substring(tokens[t].indexOf("MSS:") + 4);
                temp = StringUtil.replaceAll(temp, "{{" + tokens[t] + "}}", "MSS".equals(dbms) ? replacevalue : "");
            }
        }
        return temp;
    }

    private void addSimpleJsonProperty(String editor, HashMap attributes, String id, Serializable value, JsonObject jso) {
        if (value instanceof String) {
            String svalue = value.toString();
            if (editor.equals("YesNoEditor")) {
                if ("Y".equals(attributes.get("defaulttrue"))) {
                    jso.put(id, !svalue.equals("N"));
                } else {
                    jso.put(id, svalue.equals("Y"));
                }
            } else if (svalue.length() > 0) {
                if (editor.equals("NumberEditor")) {
                    try {
                        jso.put(id, Double.parseDouble(svalue));
                    }
                    catch (Exception exception) {}
                } else {
                    jso.put(id, svalue);
                }
            }
        } else {
            jso.putAnyType(id, value);
        }
    }

    public JsonObject toSimpleJSON(PropertyDefinitionList definition) throws SapphireException {
        return this.toSimpleJSON(definition, null);
    }

    public JsonObject toSimpleJSON() throws SapphireException {
        return new JsonObject(this.toJSONObject(false, false));
    }

    public JsonObject toSimpleJSON(PropertyDefinitionList definition, HashMap<String, String> breakpoints) throws SapphireException {
        if (definition != null) {
            JsonObject jso = new JsonObject();
            for (Object oDef : definition) {
                PropertyDefinitionList subDef;
                PropertyDefinition def = (PropertyDefinition)oDef;
                String id = def.getId();
                if (def.getType().equals("propertylist")) {
                    PropertyList subPL = this.getPropertyListNotNull(id);
                    subDef = def.getPropertyDefinitionList();
                    JsonObject subJSO = subPL.toSimpleJSON(subDef, breakpoints);
                    jso.put(id, subJSO);
                    continue;
                }
                if (def.getType().equals("collection")) {
                    PropertyListCollection collection = this.getCollectionNotNull(id);
                    subDef = def.getPropertyDefinitionList();
                    JsonArray array = new JsonArray();
                    for (int i = 0; i < collection.size(); ++i) {
                        PropertyList subPL = collection.getPropertyList(i);
                        JsonObject subJSO = subPL.toSimpleJSON(subDef, breakpoints);
                        array.put(subJSO);
                    }
                    jso.put(id, array);
                    continue;
                }
                String editor = def.getEditor();
                if (def.isResolution()) {
                    String value;
                    Iterator it;
                    JsonObject breakpointsOut = new JsonObject();
                    if (!(this.get(id) instanceof PropertyList)) {
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", (Serializable)((Object)this.getProperty(id)), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", (Serializable)((Object)this.getProperty(id)), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "md", (Serializable)((Object)this.getProperty(id)), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", (Serializable)((Object)this.getProperty(id)), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", (Serializable)((Object)this.getProperty(id)), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "breakpoints", new JsonArray(), breakpointsOut);
                    } else if (breakpoints != null) {
                        it = this.getPropertyListNotNull(id).keySet().iterator();
                        while (it.hasNext()) {
                            String breakpoint = it.next().toString();
                            value = this.getPropertyListNotNull(id).getProperty(breakpoint);
                            if (!breakpoints.containsKey(breakpoint) || breakpoints.get(breakpoint).length() <= 0) continue;
                            this.addSimpleJsonProperty(editor, def.getAttributes(), breakpoints.get(breakpoint), (Serializable)((Object)value), breakpointsOut);
                        }
                        if (!breakpointsOut.has("xl")) {
                            if (breakpointsOut.has("lg")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", breakpointsOut.get("lg"), breakpointsOut);
                            } else if (breakpointsOut.has("md")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", breakpointsOut.get("md"), breakpointsOut);
                            } else if (breakpointsOut.has("sm")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", breakpointsOut.get("sm"), breakpointsOut);
                            } else if (breakpointsOut.has("xs")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", breakpointsOut.get("xs"), breakpointsOut);
                            }
                        }
                        if (!breakpointsOut.has("lg")) {
                            if (breakpointsOut.has("md")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", breakpointsOut.get("md"), breakpointsOut);
                            } else if (breakpointsOut.has("sm")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", breakpointsOut.get("sm"), breakpointsOut);
                            } else if (breakpointsOut.has("xs")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", breakpointsOut.get("xs"), breakpointsOut);
                            } else if (breakpointsOut.has("xl")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", breakpointsOut.get("xl"), breakpointsOut);
                            }
                        }
                        if (!breakpointsOut.has("md")) {
                            if (breakpointsOut.has("sm")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "md", breakpointsOut.get("sm"), breakpointsOut);
                            } else if (breakpointsOut.has("xs")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "md", breakpointsOut.get("xs"), breakpointsOut);
                            } else if (breakpointsOut.has("lg")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "md", breakpointsOut.get("lg"), breakpointsOut);
                            } else if (breakpointsOut.has("xl")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "md", breakpointsOut.get("xl"), breakpointsOut);
                            }
                        }
                        if (!breakpointsOut.has("sm")) {
                            if (breakpointsOut.has("xs")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", breakpointsOut.get("xs"), breakpointsOut);
                            } else if (breakpointsOut.has("md")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", breakpointsOut.get("md"), breakpointsOut);
                            } else if (breakpointsOut.has("lg")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", breakpointsOut.get("lg"), breakpointsOut);
                            } else if (breakpointsOut.has("xl")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", breakpointsOut.get("xl"), breakpointsOut);
                            }
                        }
                        if (!breakpointsOut.has("xs")) {
                            if (breakpointsOut.has("sm")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", breakpointsOut.get("sm"), breakpointsOut);
                            } else if (breakpointsOut.has("md")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", breakpointsOut.get("md"), breakpointsOut);
                            } else if (breakpointsOut.has("lg")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", breakpointsOut.get("lg"), breakpointsOut);
                            } else if (breakpointsOut.has("xl")) {
                                this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", breakpointsOut.get("xl"), breakpointsOut);
                            }
                        }
                        JsonArray breakpointsUsed = new JsonArray();
                        if (breakpointsOut.has("xs")) {
                            breakpointsUsed.put("xs");
                        } else if (breakpointsOut.has("sm")) {
                            breakpointsUsed.put("sm");
                        } else if (breakpointsOut.has("md")) {
                            breakpointsUsed.put("md");
                        } else if (breakpointsOut.has("lg")) {
                            breakpointsUsed.put("lg");
                        } else if (breakpointsOut.has("xl")) {
                            breakpointsUsed.put("xl");
                        }
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "breakpoints", breakpointsUsed, breakpointsOut);
                    } else {
                        it = this.getPropertyListNotNull(id).keySet().iterator();
                        String breakpoint = it.next().toString();
                        value = this.getPropertyListNotNull(id).getProperty(breakpoint);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "xs", (Serializable)((Object)value), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "sm", (Serializable)((Object)value), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "md", (Serializable)((Object)value), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "lg", (Serializable)((Object)value), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "xl", (Serializable)((Object)value), breakpointsOut);
                        this.addSimpleJsonProperty(editor, def.getAttributes(), "breakpoints", new JsonArray(), breakpointsOut);
                    }
                    jso.put(id, breakpointsOut);
                    continue;
                }
                String value = this.getProperty(id);
                this.addSimpleJsonProperty(editor, def.getAttributes(), id, (Serializable)((Object)value), jso);
            }
            return jso;
        }
        return this.toSimpleJSON();
    }
}

