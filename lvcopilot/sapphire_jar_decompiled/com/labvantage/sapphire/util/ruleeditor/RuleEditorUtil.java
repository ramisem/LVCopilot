/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.ruleeditor;

import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RuleEditorUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private final Browser browser;
    private String configurationXML;
    private String initialDataJson;
    private String defaultJson;
    private Document document;
    private XPath xpath;
    private List<Block> blocks;
    private Map<String, Block> blockMap;
    private Map<String, String> defaultRuleMap;
    private Map<String, List<SubBlock>> subBlockMap;
    private Map<String, List<Element>> blockElements;
    private Map<String, Map<String, Element>> blockElementMap;
    private Map<String, PropertyList> policyPropertyListMap;
    private QueryProcessor qp;
    private TranslationProcessor tp;
    private ConfigurationProcessor cp;
    private PageContext pageContext;
    private RequestContext requestContext;
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private String sdcid = "";

    public RuleEditorUtil(PageContext pageContext, RequestContext requestContext, String initialDataJson) {
        this.tp = new TranslationProcessor(pageContext);
        this.qp = new QueryProcessor(pageContext);
        this.cp = new ConfigurationProcessor(pageContext);
        this.pageContext = pageContext;
        this.requestContext = requestContext;
        this.initialDataJson = initialDataJson;
        this.policyPropertyListMap = new HashMap<String, PropertyList>();
        this.keyid1 = requestContext.getProperty("keyid1");
        this.keyid1 = this.keyid1 == null ? "" : this.keyid1;
        this.keyid2 = requestContext.getProperty("keyid2");
        this.keyid2 = this.keyid2 == null ? "" : this.keyid2;
        this.keyid3 = requestContext.getProperty("keyid3");
        this.keyid3 = this.keyid3 == null ? "" : this.keyid3;
        this.sdcid = requestContext.getProperty("sdcid");
        this.sdcid = this.sdcid == null ? "Sample" : this.sdcid;
        this.browser = new Browser(pageContext);
    }

    public void setConfigurationXML(String configurationXML) throws SapphireException {
        this.configurationXML = configurationXML;
        this.document = DOMUtil.getNewDocument(configurationXML);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xpath = xPathFactory.newXPath();
    }

    public void compileConfigurationXML() throws SapphireException {
        try {
            this.defaultRuleMap = new HashMap<String, String>();
            this.blockMap = new HashMap<String, Block>();
            this.blocks = new ArrayList<Block>();
            this.subBlockMap = new HashMap<String, List<SubBlock>>();
            this.blockElements = new HashMap<String, List<Element>>();
            this.blockElementMap = new HashMap<String, Map<String, Element>>();
            XPathExpression xPathDefaultRuleInRuleEditorTagExpr = this.xpath.compile("ruleeditor/attribute::defaultrule");
            String defaultRuleInRuleEditorTag = (String)xPathDefaultRuleInRuleEditorTagExpr.evaluate(this.document, XPathConstants.STRING);
            if (defaultRuleInRuleEditorTag == null || defaultRuleInRuleEditorTag.length() == 0) {
                defaultRuleInRuleEditorTag = "{}";
            }
            this.defaultRuleMap.put("primarydefault", defaultRuleInRuleEditorTag);
            XPathExpression xPathDefaultRuleExpr = this.xpath.compile("ruleeditor/defaultrule/rule");
            NodeList defaultRuleNodes = (NodeList)xPathDefaultRuleExpr.evaluate(this.document, XPathConstants.NODESET);
            for (int i = 0; i < defaultRuleNodes.getLength(); ++i) {
                Node defaultRule = defaultRuleNodes.item(i);
                NamedNodeMap defaultRuleAttributeMap = defaultRule.getAttributes();
                String ruleId = "";
                String ruleJson = "";
                for (int j = 0; j < defaultRuleAttributeMap.getLength(); ++j) {
                    Node n = defaultRuleAttributeMap.item(j);
                    if ("id".equals(n.getNodeName())) {
                        ruleId = n.getNodeValue();
                        continue;
                    }
                    if (!"json".equals(n.getNodeName())) continue;
                    ruleJson = n.getNodeValue();
                }
                if (ruleId == null || ruleId.length() <= 0) continue;
                if (ruleJson == null || ruleJson.length() == 0) {
                    ruleJson = "{}";
                }
                this.defaultRuleMap.put(ruleId, ruleJson);
            }
            String defaultRuleIdInRequest = this.requestContext.getProperty("defaultrule");
            defaultRuleIdInRequest = defaultRuleIdInRequest == null || defaultRuleIdInRequest.trim().length() == 0 ? "primarydefault" : defaultRuleIdInRequest;
            this.defaultJson = this.defaultRuleMap.get(defaultRuleIdInRequest);
            if (this.initialDataJson == null || this.initialDataJson.length() == 0 || this.initialDataJson.trim().equals("{}")) {
                this.initialDataJson = this.defaultJson;
            }
            XPathExpression xPathBlockExpr = this.xpath.compile("ruleeditor/block");
            NodeList blockNodes = (NodeList)xPathBlockExpr.evaluate(this.document, XPathConstants.NODESET);
            for (int i = 0; i < blockNodes.getLength(); ++i) {
                Block b = new Block();
                Node block = blockNodes.item(i);
                NamedNodeMap blockAttributeMap = block.getAttributes();
                for (int j = 0; j < blockAttributeMap.getLength(); ++j) {
                    Node n = blockAttributeMap.item(j);
                    if ("id".equals(n.getNodeName())) {
                        b.id = n.getNodeValue();
                        continue;
                    }
                    if ("value".equals(n.getNodeName())) {
                        b.value = n.getNodeValue();
                        continue;
                    }
                    if (!"next".equals(n.getNodeName())) continue;
                    b.next = n.getNodeValue();
                }
                this.blockMap.put(b.id, b);
                this.blocks.add(b);
                XPathExpression xPathSubBlockExpr = this.xpath.compile("ruleeditor/block[@id='" + b.id + "']/subblock");
                ArrayList<SubBlock> subBlockList = new ArrayList<SubBlock>();
                NodeList subBlockNodes = (NodeList)xPathSubBlockExpr.evaluate(this.document, XPathConstants.NODESET);
                for (int j = 0; j < subBlockNodes.getLength(); ++j) {
                    SubBlock sb = new SubBlock(b);
                    Node subBlock = subBlockNodes.item(j);
                    NamedNodeMap subBlocktAttributeMap = subBlock.getAttributes();
                    for (int k = 0; k < subBlocktAttributeMap.getLength(); ++k) {
                        Node n = subBlocktAttributeMap.item(k);
                        if ("id".equals(n.getNodeName())) {
                            sb.id = n.getNodeValue();
                            continue;
                        }
                        if (!"sequence".equals(n.getNodeName())) continue;
                        sb.sequence = n.getNodeValue();
                    }
                    subBlockList.add(sb);
                }
                XPathExpression xPathBlockElementExpr = this.xpath.compile("ruleeditor/block[@id='" + b.id + "']/element");
                ArrayList<Element> elementList = new ArrayList<Element>();
                HashMap<String, Element> elementMap = new HashMap<String, Element>();
                NodeList blockElementNodes = (NodeList)xPathBlockElementExpr.evaluate(this.document, XPathConstants.NODESET);
                for (int j = 0; j < blockElementNodes.getLength(); ++j) {
                    Element e = new Element(b);
                    Node element = blockElementNodes.item(j);
                    NamedNodeMap blockElementAttributeMap = element.getAttributes();
                    for (int k = 0; k < blockElementAttributeMap.getLength(); ++k) {
                        Node n = blockElementAttributeMap.item(k);
                        if ("id".equals(n.getNodeName())) {
                            e.id = n.getNodeValue();
                            continue;
                        }
                        if ("value".equals(n.getNodeName())) {
                            e.value = n.getNodeValue();
                            continue;
                        }
                        if ("type".equals(n.getNodeName())) {
                            e.type = n.getNodeValue();
                            continue;
                        }
                        if ("lookuppage".equals(n.getNodeName())) {
                            e.lookuppage = n.getNodeValue();
                            continue;
                        }
                        if ("next".equals(n.getNodeName())) {
                            e.next = n.getNodeValue();
                            continue;
                        }
                        if ("propertyid".equals(n.getNodeName())) {
                            e.propertyid = n.getNodeValue();
                            continue;
                        }
                        if ("prefix".equals(n.getNodeName())) {
                            e.prefix = n.getNodeValue();
                            continue;
                        }
                        if ("min".equals(n.getNodeName())) {
                            e.min = n.getNodeValue();
                            continue;
                        }
                        if ("max".equals(n.getNodeName())) {
                            e.max = n.getNodeValue();
                            continue;
                        }
                        if ("mandatory".equals(n.getNodeName())) {
                            e.mandatory = n.getNodeValue();
                            continue;
                        }
                        if ("subblock".equals(n.getNodeName())) {
                            e.subblock = n.getNodeValue();
                            continue;
                        }
                        if ("editorstyleid".equals(n.getNodeName())) {
                            e.editorstyleid = n.getNodeValue();
                            continue;
                        }
                        if ("policy".equals(n.getNodeName())) {
                            e.policy = n.getNodeValue();
                            continue;
                        }
                        if ("policynode".equals(n.getNodeName())) {
                            e.policynode = n.getNodeValue();
                            continue;
                        }
                        if ("policyproperty".equals(n.getNodeName())) {
                            e.policyproperty = n.getNodeValue();
                            continue;
                        }
                        if ("multiselect".equals(n.getNodeName())) {
                            e.multiselect = n.getNodeValue();
                            continue;
                        }
                        if ("backgroundtext".equals(n.getNodeName())) {
                            e.backgroundtext = n.getNodeValue();
                            continue;
                        }
                        if ("translate".equals(n.getNodeName())) {
                            e.translate = n.getNodeValue();
                            continue;
                        }
                        if ("defaultvalue".equals(n.getNodeName())) {
                            e.defaultvalue = n.getNodeValue();
                            continue;
                        }
                        if (!"rendernextelement".equals(n.getNodeName())) continue;
                        e.rendernextelement = n.getNodeValue();
                    }
                    if (e.propertyid.length() == 0) {
                        e.propertyid = e.id;
                    }
                    XPathExpression xPathElementOptionsExpr = this.xpath.compile("ruleeditor/block[@id='" + b.id + "']/element[@id='" + e.id + "']/option");
                    NodeList optionNodes = (NodeList)xPathElementOptionsExpr.evaluate(this.document, XPathConstants.NODESET);
                    for (int k = 0; k < optionNodes.getLength(); ++k) {
                        Option op = new Option();
                        Node option = optionNodes.item(k);
                        NamedNodeMap optionAttributeMap = option.getAttributes();
                        for (int l = 0; l < optionAttributeMap.getLength(); ++l) {
                            Node n = optionAttributeMap.item(l);
                            if ("id".equals(n.getNodeName())) {
                                op.id = n.getNodeValue();
                                continue;
                            }
                            if ("value".equals(n.getNodeName())) {
                                op.value = n.getNodeValue();
                                continue;
                            }
                            if ("query".equals(n.getNodeName())) {
                                op.query = n.getNodeValue();
                                continue;
                            }
                            if ("requestparam".equals(n.getNodeName())) {
                                op.requestparam = n.getNodeValue();
                                continue;
                            }
                            if ("subparam1".equals(n.getNodeName())) {
                                op.subparam1 = n.getNodeValue();
                                continue;
                            }
                            if ("subparam2".equals(n.getNodeName())) {
                                op.subparam2 = n.getNodeValue();
                                continue;
                            }
                            if ("subparam3".equals(n.getNodeName())) {
                                op.subparam3 = n.getNodeValue();
                                continue;
                            }
                            if ("next".equals(n.getNodeName())) {
                                op.next = n.getNodeValue();
                                continue;
                            }
                            if ("policy".equals(n.getNodeName())) {
                                op.policy = n.getNodeValue();
                                continue;
                            }
                            if ("policynode".equals(n.getNodeName())) {
                                op.policynode = n.getNodeValue();
                                continue;
                            }
                            if ("policyproperty".equals(n.getNodeName())) {
                                op.policyproperty = n.getNodeValue();
                                continue;
                            }
                            if ("observers".equals(n.getNodeName())) {
                                op.observers = n.getNodeValue();
                                continue;
                            }
                            if ("observerquery".equals(n.getNodeName())) {
                                op.observerquery = n.getNodeValue();
                                continue;
                            }
                            if (!"observee".equals(n.getNodeName())) continue;
                            op.observee = n.getNodeValue();
                        }
                        e.options.add(op);
                    }
                    List<Element> resolvedElements = this.resolveElementExtras(e);
                    elementList.addAll(resolvedElements);
                    for (Element el : resolvedElements) {
                        elementMap.put(el.id, el);
                    }
                }
                this.subBlockMap.put(b.id, subBlockList);
                this.blockElements.put(b.id, elementList);
                this.blockElementMap.put(b.id, elementMap);
            }
        }
        catch (XPathExpressionException e) {
            throw new SapphireException("Failed to parse the configuration XML" + e.getMessage());
        }
    }

    private List<Element> resolveElementExtras(Element e) throws SapphireException {
        ArrayList<Element> finalElementList = new ArrayList<Element>();
        if (e.policy.length() > 0) {
            if (e.policynode.length() == 0) {
                e.policynode = "Sapphire Custom";
            }
            if (e.policyproperty.length() > 0) {
                int j;
                if (!this.policyPropertyListMap.containsKey(e.policy + "." + e.policynode)) {
                    this.policyPropertyListMap.put(e.policy + "." + e.policynode, this.cp.getPolicy(e.policy, e.policynode));
                }
                PropertyList policyPropertyList = this.policyPropertyListMap.get(e.policy + "." + e.policynode);
                String policyPropertyString = e.policyproperty;
                String[] policyProperty = StringUtil.split(policyPropertyString, ".");
                PropertyList propertyList = policyPropertyList;
                PropertyListCollection propertyListCollection = null;
                for (j = 0; j < policyProperty.length; ++j) {
                    propertyListCollection = propertyList.getCollection(policyProperty[j]);
                    propertyList = propertyList.getPropertyList(policyProperty[j]);
                }
                if (propertyList != null) {
                    String[] tempId = StringUtil.split(e.id, ".");
                    String elementId = e.id;
                    if (tempId.length > 1) {
                        elementId = propertyList.getProperty(tempId[1], "");
                        if ((tempId = StringUtil.split(tempId[0], "_")).length > 1) {
                            elementId = tempId[0] + elementId;
                        }
                    }
                    if ((elementId = StringUtil.replaceAll(elementId, " ", "")).trim().length() > 0) {
                        Element newElement = new Element(e.blockId);
                        newElement.id = elementId;
                        newElement.type = e.type;
                        newElement.mandatory = e.mandatory;
                        newElement.propertyid = e.propertyid;
                        newElement.prefix = e.prefix;
                        newElement.options = e.options;
                        newElement.translate = e.translate;
                        String[] tempValue = StringUtil.split(e.value, ".");
                        String[] tempNext = StringUtil.split(e.next, ".");
                        String[] tempEditorStyleId = StringUtil.split(e.editorstyleid, ".");
                        newElement.value = tempValue.length > 1 ? propertyList.getProperty(tempValue[1], "") : e.value;
                        if (tempNext.length > 1) {
                            newElement.next = propertyList.getProperty(tempNext[1], "");
                            if ((tempNext = StringUtil.split(tempNext[0], "_")).length > 1) {
                                newElement.next = tempNext[0] + newElement.next;
                            }
                            newElement.next = StringUtil.replaceAll(newElement.next, " ", "");
                        } else {
                            newElement.next = StringUtil.replaceAll(e.next, " ", "");
                        }
                        newElement.editorstyleid = tempEditorStyleId.length > 1 ? propertyList.getProperty(tempEditorStyleId[1], "") : e.editorstyleid;
                        finalElementList.add(newElement);
                    }
                } else if (propertyListCollection != null) {
                    for (j = 0; j < propertyListCollection.size(); ++j) {
                        propertyList = propertyListCollection.getPropertyList(j);
                        if (propertyList == null) continue;
                        String[] tempId = StringUtil.split(e.id, ".");
                        String elementId = e.id;
                        if (tempId.length > 1) {
                            elementId = propertyList.getProperty(tempId[1], "");
                            if ((tempId = StringUtil.split(tempId[0], "_")).length > 1) {
                                elementId = tempId[0] + elementId;
                            }
                        }
                        if ((elementId = StringUtil.replaceAll(elementId, " ", "")).trim().length() <= 0) continue;
                        Element newElement = new Element(e.blockId);
                        newElement.id = elementId;
                        newElement.type = e.type;
                        newElement.mandatory = e.mandatory;
                        newElement.propertyid = e.propertyid;
                        newElement.prefix = e.prefix;
                        newElement.options = e.options;
                        newElement.translate = e.translate;
                        String[] tempValue = StringUtil.split(e.value, ".");
                        String[] tempNext = StringUtil.split(e.next, ".");
                        String[] tempEditorStyleId = StringUtil.split(e.editorstyleid, ".");
                        newElement.value = tempValue.length > 1 ? propertyList.getProperty(tempValue[1], "") : e.value;
                        if (tempNext.length > 1) {
                            newElement.next = propertyList.getProperty(tempNext[1], "");
                            if ((tempNext = StringUtil.split(tempNext[0], "_")).length > 1) {
                                newElement.next = tempNext[0] + newElement.next;
                            }
                            newElement.next = StringUtil.replaceAll(newElement.next, " ", "");
                        } else {
                            newElement.next = StringUtil.replaceAll(e.next, " ", "");
                        }
                        newElement.editorstyleid = tempEditorStyleId.length > 1 ? propertyList.getProperty(tempEditorStyleId[1], "") : e.editorstyleid;
                        finalElementList.add(newElement);
                    }
                }
            }
        } else {
            e.id = StringUtil.replaceAll(e.id, " ", "");
            e.next = StringUtil.replaceAll(e.next, " ", "");
            finalElementList.add(e);
        }
        for (Element el : finalElementList) {
            for (Option o : el.options) {
                if (o != null && o.next.length() != 0) continue;
                o.next = e.next;
            }
        }
        return finalElementList;
    }

    public Map<String, Block> getBlocksAsMap() {
        return this.blockMap;
    }

    public List<Block> getBlocksAsList() {
        return this.blocks;
    }

    public List<Element> getBlockElementsAsList(String blockId) {
        return this.blockElements.get(blockId);
    }

    public Map<String, Element> getBlockElementsAsMap(String blockId) {
        return this.blockElementMap.get(blockId);
    }

    public String getJavascript() throws SapphireException {
        StringBuilder js = new StringBuilder("var blocks = [];").append("\n");
        for (int i = 0; i < this.blocks.size(); ++i) {
            Block b = this.blocks.get(i);
            js.append("blocks[").append(i).append("] = new Block()").append(";\n");
            js.append("blocks[").append(i).append("].id = '").append(b.id).append("'").append(";\n");
            js.append("blocks[").append(i).append("].value = '").append(b.value).append("'").append(";\n");
            js.append("blocks[").append(i).append("].next = '").append(b.next).append("'").append(";\n");
        }
        js.append("\n");
        js.append("var subblocks = [];").append("\n");
        js.append("var elements = [];").append("\n");
        for (Block b : this.blocks) {
            List<SubBlock> subBlocks = this.subBlockMap.get(b.id);
            if (subBlocks != null) {
                for (SubBlock sb : subBlocks) {
                    js.append("subblocks['").append(b.id + "_" + sb.id).append("'] = new SubBlock()").append(";\n");
                    js.append("subblocks['").append(b.id + "_" + sb.id).append("'].id = '").append(sb.id).append("'").append(";\n");
                    js.append("subblocks['").append(b.id + "_" + sb.id).append("'].blockid = '").append(sb.blockId).append("'").append(";\n");
                }
            }
            js.append("\n");
            List<Element> elements = this.blockElements.get(b.id);
            for (Element e : elements) {
                js.append("elements['").append(b.id + "_" + e.id).append("'] = new Element()").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].propertyid = '").append(e.propertyid).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].id = '").append(e.id).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].blockid = '").append(b.id).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].value = '").append(e.value).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].next = '").append(e.next).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].mandatory = '").append(e.mandatory).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].subblock = '").append(e.subblock).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].prefix = '").append(e.prefix).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].multiselect = '").append(e.multiselect).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].translate = '").append(e.translate).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].defaultvalue = '").append(e.defaultvalue).append("'").append(";\n");
                js.append("elements['").append(b.id + "_" + e.id).append("'].rendernextelement = '").append(e.rendernextelement).append("'").append(";\n");
                String templateHtml = e.getTemplateHtml();
                templateHtml = StringUtil.replaceAll(templateHtml, "\"", "\\\"");
                js.append("elements['").append(b.id + "_" + e.id).append("'].html = \"").append(templateHtml).append("\"").append(";\n");
                js.append("var options = [];").append("\n");
                for (int i = 0; i < e.options.size(); ++i) {
                    Option o = e.options.get(i);
                    o.id = StringUtil.replaceAll(o.id, " ", "");
                    o.next = StringUtil.replaceAll(o.next, " ", "");
                    js.append("options[").append(i).append("] = new Option();").append(";\n");
                    js.append("options[").append(i).append("].id = '").append(o.id).append("'").append(";\n");
                    js.append("options[").append(i).append("].value = '").append(o.value).append("'").append(";\n");
                    js.append("options[").append(i).append("].next = '").append(o.next).append("'").append(";\n");
                    if (o.observers.length() > 0 && o.observerDataSet.length() > 0) {
                        js.append("options[").append(i).append("].observers = '").append(o.observers).append("'").append(";\n");
                        js.append("options[").append(i).append("].observerdataset = '").append(o.observerDataSet).append("'").append(";\n");
                        js.append("options[").append(i).append("].observerdatasetoptionhtml = \"").append(o.observerDataSetOptionHtml).append("\"").append(";\n");
                    }
                    if (o.observee.length() <= 0) continue;
                    js.append("options[").append(i).append("].observee = '").append(o.observee).append("'").append(";\n");
                }
                js.append("elements['").append(b.id + "_" + e.id).append("'].options = options").append(";\n");
            }
            js.append("\n");
            js.append("\n");
        }
        js.append("var ruleeditor = new RuleEditor()").append(";\n");
        js.append("ruleeditor.elements = elements").append(";\n");
        js.append("ruleeditor.blocks = blocks").append(";\n");
        js.append("ruleeditor.subblocks = subblocks").append(";\n");
        js.append("ruleeditor.initialdatajson = ").append(this.initialDataJson).append(";\n");
        return js.toString();
    }

    public String getInitialHtml() throws SapphireException {
        StringBuilder html = new StringBuilder();
        try {
            JSONObject savedJson = new JSONObject(this.initialDataJson);
            html.append("<table id='maintable'>").append("\n");
            for (Block b : this.blocks) {
                JSONArray blockElementRows;
                html.append("<tr>").append("\n");
                html.append("<td nowrap>").append("\n");
                html.append("<table id='").append(b.id).append("_table").append("'>").append("\n");
                html.append("<tr>").append("\n");
                html.append("<td nowrap>").append("\n");
                if (savedJson.has(b.id) && (blockElementRows = (JSONArray)savedJson.get(b.id)) != null) {
                    for (int i = 0; i < blockElementRows.length(); ++i) {
                        HashMap subBlockHtml = new HashMap();
                        JSONObject row = (JSONObject)blockElementRows.get(i);
                        Map<String, Element> elements = this.getBlockElementsAsMap(b.id);
                        Element e = elements.get(b.next);
                        html.append("<table id='").append(b.id).append("_").append(i).append("_").append("subblock_primary").append("_table").append("'>").append("\n");
                        html.append("<tr>").append("\n");
                        boolean isEntryPoint = true;
                        do {
                            if (!isEntryPoint) {
                                e = elements.get(e.getNextId(row));
                            }
                            if ("primary".equals(e.subblock)) {
                                html.append("<td ").append("name='").append(i).append("'").append(" nowrap>").append("\n");
                                html.append(e.getHtml(row, i));
                                html.append("</td>").append("\n");
                            } else {
                                if (!subBlockHtml.containsKey(e.subblock)) {
                                    subBlockHtml.put(e.subblock, new ArrayList());
                                }
                                ((ArrayList)subBlockHtml.get(e.subblock)).add(e.getHtml(row, i));
                            }
                            isEntryPoint = false;
                        } while (e.hasNext(row));
                        html.append("</tr>").append("\n");
                        html.append("</table>").append("\n");
                        List<SubBlock> subBlockFromXML = this.subBlockMap.get(b.id);
                        for (SubBlock sb : subBlockFromXML) {
                            html.append("<table id='").append(b.id).append("_").append(i).append("_subblock_").append(sb.id).append("_table").append("' >").append("\n");
                            html.append("<tr>").append("\n");
                            html.append("<td ").append("name='").append(i).append("'").append(" nowrap>").append("\n");
                            html.append(sb.getBlankPrefix());
                            html.append("</td>").append("\n");
                            List subBlockHtmlList = (List)subBlockHtml.get(sb.id);
                            if (subBlockHtmlList != null) {
                                for (String sbHtml : subBlockHtmlList) {
                                    html.append("<td ").append("name='").append(i).append("'").append(" nowrap>").append("\n");
                                    html.append(sbHtml);
                                    html.append("</td>").append("\n");
                                }
                            }
                            html.append("</tr>").append("\n");
                            html.append("</table>").append("\n");
                        }
                    }
                }
                html.append("</td>").append("\n");
                html.append("</tr>").append("\n");
                html.append("</table>").append("\n");
                html.append("</td>").append("\n");
                html.append("</tr>").append("\n");
            }
            html.append("</table>").append("\n");
        }
        catch (JSONException e) {
            throw new SapphireException("The saved JSON is not proper");
        }
        return html.toString();
    }

    private String getDBSyntax(String input, String dbms) {
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

    public class Option {
        String id = "";
        String value = "";
        String next = "";
        String requestparam = "";
        String subparam1 = "";
        String subparam2 = "";
        String subparam3 = "";
        String query = "";
        String policy = "";
        String policynode = "";
        String policyproperty = "";
        String observers = "";
        String observerquery = "";
        String observee = "";
        String observerDataSet = "";
        String observerDataSetOptionHtml = "";

        public void resolveObserverData(String blockId, Option finalOption) {
            finalOption.observers = this.observers;
            finalOption.observerDataSet = "";
            finalOption.observerDataSetOptionHtml = "";
            String query = this.observerquery;
            query = StringUtil.replaceAll(query, "[keyid1]", RuleEditorUtil.this.keyid1);
            query = StringUtil.replaceAll(query, "[keyid2]", RuleEditorUtil.this.keyid2);
            query = StringUtil.replaceAll(query, "[keyid3]", RuleEditorUtil.this.keyid3);
            query = StringUtil.replaceAll(query, "[sdcid]", RuleEditorUtil.this.sdcid);
            query = StringUtil.replaceAll(query, "[requestparam]", finalOption.requestparam);
            query = StringUtil.replaceAll(query, "[subparam1]", finalOption.subparam1);
            query = StringUtil.replaceAll(query, "[subparam2]", finalOption.subparam2);
            query = StringUtil.replaceAll(query, "[subparam3]", finalOption.subparam3);
            DataSet queryDs = RuleEditorUtil.this.qp.getSqlDataSet(RuleEditorUtil.this.getDBSyntax(query, RuleEditorUtil.this.requestContext.getProperty("dbms")));
            StringBuilder html = new StringBuilder();
            JSONArray valuesJson = new JSONArray();
            String columnGroup = "";
            for (int j = 0; j < queryDs.getRowCount(); ++j) {
                String columnGroupCurrent = queryDs.getValue(j, "columngroup", "");
                if (!columnGroupCurrent.equals(columnGroup)) {
                    if (columnGroup.length() > 0) {
                        html.append("</optgroup>");
                    }
                    html.append("<optgroup").append(" label='").append(columnGroupCurrent).append("' >");
                    columnGroup = columnGroupCurrent;
                }
                String value = queryDs.getValue(j, "columndisplayvalue", queryDs.getValue(j, "columnvalue", ""));
                html.append("<option id='").append(blockId + "_" + this.observers + "_[row]_").append(j).append("'").append(" value='").append(value).append("'").append(">");
                html.append(value);
                html.append("</option>");
                valuesJson.put(value);
            }
            if (columnGroup.length() > 0) {
                html.append("</optgroup>");
            }
            finalOption.observerDataSetOptionHtml = html.toString();
            finalOption.observerDataSet = valuesJson.toString();
        }
    }

    public class Element {
        String propertyid = "";
        String id = "";
        String value = "";
        String type = "";
        String lookuppage = "";
        String next = "";
        String mandatory = "N";
        String prefix = "";
        String min = "";
        String max = "";
        String subblock = "primary";
        String editorstyleid = "";
        String policy = "";
        String policynode = "";
        String policyproperty = "";
        String multiselect = "N";
        String backgroundtext = "";
        String translate = "N";
        String defaultvalue = "";
        String rendernextelement = "";
        List<Option> options = new ArrayList<Option>();
        String templateHtml = "";
        String blockId = "";
        String selectedOptionObserverDataSet = "";
        String selectedOptionObserverDataSetOptionHtml = "";
        static final String TYPE_DROPDOWN = "dropdown";
        static final String TYPE_LOOKUP = "lookup";
        static final String TYPE_TEXT = "text";
        static final String TYPE_NUMBER = "number";
        static final String TYPE_EDITORSTYLE = "editorstyle";
        static final String RENDERNEXTELEMENT_ONDISPLAY = "ondisplay";

        public Element(Block b) {
            this.blockId = b.id;
        }

        public Element(String blockId) {
            this.blockId = blockId;
        }

        private String getTemplateHtml() throws SapphireException {
            if (this.templateHtml.length() == 0) {
                StringBuilder html = new StringBuilder();
                if (this.prefix.length() > 0) {
                    html.append("<label>").append(this.prefix).append("</label>");
                }
                String cssClass = "input_field";
                if ("Y".equals(this.mandatory)) {
                    cssClass = cssClass + " mandatoryfield";
                }
                if (TYPE_DROPDOWN.equals(this.type)) {
                    if (this.multiselect.equals("Y")) {
                        html.append("<select id='").append(this.blockId).append("_").append(this.id).append("_[row]' class='chosen-select' ");
                        html.append(" multiple ");
                        if (this.backgroundtext.length() > 0) {
                            html.append("data-placeholder='").append(RuleEditorUtil.this.tp.translate(this.backgroundtext)).append("' ");
                        }
                    } else {
                        html.append("<select id='").append(this.blockId).append("_").append(this.id).append("_[row]' class='").append(cssClass).append("' ");
                    }
                    html.append(" onchange='ruleeditor.renderNextElement(this, \"").append(this.blockId + "_" + this.id).append("\", [row]").append(")'");
                    html.append(">");
                    ArrayList<Option> finalOptionList = new ArrayList<Option>();
                    for (int i = 0; i < this.options.size(); ++i) {
                        Option o = this.options.get(i);
                        if (o.query.length() > 0) {
                            String elementQuery = o.query;
                            elementQuery = StringUtil.replaceAll(elementQuery, "[keyid1]", RuleEditorUtil.this.keyid1);
                            elementQuery = StringUtil.replaceAll(elementQuery, "[keyid2]", RuleEditorUtil.this.keyid2);
                            elementQuery = StringUtil.replaceAll(elementQuery, "[keyid3]", RuleEditorUtil.this.keyid3);
                            elementQuery = StringUtil.replaceAll(elementQuery, "[sdcid]", RuleEditorUtil.this.sdcid);
                            DataSet queryDs = RuleEditorUtil.this.qp.getSqlDataSet(RuleEditorUtil.this.getDBSyntax(elementQuery, RuleEditorUtil.this.requestContext.getProperty("dbms")));
                            for (int j = 0; j < queryDs.getRowCount(); ++j) {
                                Option finalOption = new Option();
                                finalOption.id = queryDs.getValue(j, "columnvalue", "");
                                finalOption.value = queryDs.getValue(j, "columndisplayvalue", finalOption.id);
                                finalOption.next = o.next;
                                if (o.observers.length() > 0 && o.observerquery.length() > 0) {
                                    finalOption.observers = o.observers;
                                    o.resolveObserverData(this.blockId, finalOption);
                                }
                                finalOptionList.add(finalOption);
                                html.append("<option id='").append(this.blockId + "_" + this.id + "_[row]_").append(j).append("'").append(" value='").append(finalOption.value).append("'").append(">");
                                html.append(this.translate.equals("Y") ? RuleEditorUtil.this.tp.translate(finalOption.value) : finalOption.value);
                                html.append("</option>");
                            }
                            continue;
                        }
                        if (o.requestparam.length() > 0) {
                            String valuesInRequest = RuleEditorUtil.this.requestContext.getProperty(o.requestparam);
                            String subParam1InRequest = RuleEditorUtil.this.requestContext.getProperty(o.subparam1);
                            String subParam2InRequest = RuleEditorUtil.this.requestContext.getProperty(o.subparam2);
                            String subParam3InRequest = RuleEditorUtil.this.requestContext.getProperty(o.subparam3);
                            if (valuesInRequest == null || valuesInRequest.length() <= 0) continue;
                            String[] values = StringUtil.split(valuesInRequest, ";");
                            String[] subParam1 = new String[]{};
                            if (subParam1InRequest != null && subParam1InRequest.length() > 0) {
                                subParam1 = StringUtil.split(subParam1InRequest, ";");
                            }
                            String[] subParam2 = new String[]{};
                            if (subParam2InRequest != null && subParam2InRequest.length() > 0) {
                                subParam2 = StringUtil.split(subParam2InRequest, ";");
                            }
                            String[] subParam3 = new String[]{};
                            if (subParam3InRequest != null && subParam3InRequest.length() > 0) {
                                subParam3 = StringUtil.split(subParam3InRequest, ";");
                            }
                            for (int j = 0; j < values.length; ++j) {
                                Option finalOption = new Option();
                                finalOption.id = values[j];
                                finalOption.value = values[j];
                                finalOption.next = o.next;
                                if (o.observers.length() > 0 && o.observerquery.length() > 0) {
                                    finalOption.observers = o.observers;
                                    finalOption.requestparam = values[j];
                                    if (subParam1.length == values.length) {
                                        finalOption.subparam1 = subParam1[j];
                                    }
                                    if (subParam2.length == values.length) {
                                        finalOption.subparam2 = subParam2[j];
                                    }
                                    if (subParam3.length == values.length) {
                                        finalOption.subparam3 = subParam3[j];
                                    }
                                    o.resolveObserverData(this.blockId, finalOption);
                                }
                                finalOptionList.add(finalOption);
                                html.append("<option id='").append(this.blockId + "_" + this.id + "_[row]_").append(j).append("'").append(" value='").append(finalOption.value).append("'").append(">");
                                html.append(this.translate.equals("Y") ? RuleEditorUtil.this.tp.translate(finalOption.value) : finalOption.value);
                                html.append("</option>");
                            }
                            continue;
                        }
                        if (o.policy.length() > 0) {
                            int j;
                            if (o.policynode.length() == 0) {
                                o.policynode = "Sapphire Custom";
                            }
                            if (o.policyproperty.length() <= 0) continue;
                            if (!RuleEditorUtil.this.policyPropertyListMap.containsKey(o.policy + "." + o.policynode)) {
                                RuleEditorUtil.this.policyPropertyListMap.put(o.policy + "." + o.policynode, RuleEditorUtil.this.cp.getPolicy(o.policy, o.policynode));
                            }
                            PropertyList policyPropertyList = (PropertyList)RuleEditorUtil.this.policyPropertyListMap.get(o.policy + "." + o.policynode);
                            String[] policyProperty = StringUtil.split(o.policyproperty, ".");
                            PropertyList propertyList = policyPropertyList;
                            PropertyListCollection propertyListCollection = null;
                            for (j = 0; j < policyProperty.length; ++j) {
                                propertyListCollection = propertyList.getCollection(policyProperty[j]);
                                propertyList = propertyList.getPropertyList(policyProperty[j]);
                            }
                            if (propertyList != null) {
                                String finalOptionValue;
                                String[] tempId = StringUtil.split(o.id, ".");
                                String finalOptionId = o.id;
                                if (tempId.length > 1) {
                                    finalOptionId = propertyList.getProperty(tempId[1], "");
                                    if ((tempId = StringUtil.split(tempId[0], "_")).length > 1) {
                                        finalOptionId = tempId[0] + finalOptionId;
                                    }
                                }
                                if (finalOptionId.trim().length() <= 0) continue;
                                Option finalOption = new Option();
                                finalOption.id = finalOptionId;
                                String[] tempValue = StringUtil.split(o.value, ".");
                                finalOption.value = tempValue.length > 1 ? ((finalOptionValue = propertyList.getProperty(tempValue[1], finalOptionId)).trim().length() > 0 ? finalOptionValue : finalOptionId) : finalOptionId;
                                String[] tempNext = StringUtil.split(o.next, ".");
                                if (tempNext.length > 1) {
                                    finalOption.next = propertyList.getProperty(tempNext[1], "");
                                    if ((tempNext = StringUtil.split(tempNext[0], "_")).length > 1) {
                                        finalOption.next = tempNext[0] + finalOption.next;
                                    }
                                    finalOption.next = StringUtil.replaceAll(finalOption.next, " ", "");
                                } else {
                                    finalOption.next = StringUtil.replaceAll(o.next, " ", "");
                                }
                                if (o.observers.length() > 0 && o.observerquery.length() > 0) {
                                    finalOption.observers = o.observers;
                                    o.resolveObserverData(this.blockId, finalOption);
                                }
                                finalOptionList.add(finalOption);
                                html.append("<option id='").append(this.blockId + "_" + this.id + "_[row]_").append(finalOption.id).append("'").append(" value='").append(finalOption.value).append("'").append(">");
                                html.append(this.translate.equals("Y") ? RuleEditorUtil.this.tp.translate(finalOption.value) : finalOption.value);
                                html.append("</option>");
                                continue;
                            }
                            if (propertyListCollection == null) continue;
                            for (j = 0; j < propertyListCollection.size(); ++j) {
                                String finalOptionValue;
                                propertyList = propertyListCollection.getPropertyList(j);
                                if (propertyList == null) continue;
                                String[] tempId = StringUtil.split(o.id, ".");
                                String finalOptionId = o.id;
                                if (tempId.length > 1) {
                                    finalOptionId = propertyList.getProperty(tempId[1], "");
                                    if ((tempId = StringUtil.split(tempId[0], "_")).length > 1) {
                                        finalOptionId = tempId[0] + finalOptionId;
                                    }
                                }
                                if (finalOptionId.trim().length() <= 0) continue;
                                Option finalOption = new Option();
                                finalOption.id = finalOptionId;
                                String[] tempValue = StringUtil.split(o.value, ".");
                                finalOption.value = tempValue.length > 1 ? ((finalOptionValue = propertyList.getProperty(tempValue[1], finalOptionId)).trim().length() > 0 ? finalOptionValue : finalOptionId) : finalOptionId;
                                String[] tempNext = StringUtil.split(o.next, ".");
                                if (tempNext.length > 1) {
                                    finalOption.next = propertyList.getProperty(tempNext[1], "");
                                    if ((tempNext = StringUtil.split(tempNext[0], "_")).length > 1) {
                                        finalOption.next = tempNext[0] + finalOption.next;
                                    }
                                    finalOption.next = StringUtil.replaceAll(finalOption.next, " ", "");
                                } else {
                                    finalOption.next = StringUtil.replaceAll(o.next, " ", "");
                                }
                                if (o.observers.length() > 0 && o.observerquery.length() > 0) {
                                    finalOption.observers = o.observers;
                                    o.resolveObserverData(this.blockId, finalOption);
                                }
                                finalOptionList.add(finalOption);
                                html.append("<option id='").append(this.blockId + "_" + this.id + "_[row]_").append(finalOption.id).append(j).append("'").append(" value='").append(finalOption.value).append("'").append(">");
                                html.append(this.translate.equals("Y") ? RuleEditorUtil.this.tp.translate(finalOption.value) : finalOption.value);
                                html.append("</option>");
                            }
                            continue;
                        }
                        if (o.observee.length() > 0) {
                            finalOptionList.add(o);
                            continue;
                        }
                        html.append("<option id='").append(this.blockId + "_" + this.id + "_[row]_").append(o.id).append("'").append(" value='").append(o.value).append("'").append(">");
                        html.append(this.translate.equals("Y") ? RuleEditorUtil.this.tp.translate(o.value) : o.value);
                        html.append("</option>");
                        if (o.observers.length() > 0 && o.observerquery.length() > 0) {
                            o.resolveObserverData(this.blockId, o);
                        }
                        finalOptionList.add(o);
                    }
                    this.options = finalOptionList;
                    html.append("</select>");
                    if (this.multiselect.equals("Y")) {
                        html.append("<script>");
                        html.append("   $( '#").append(this.blockId).append("_").append(this.id).append("_[row]").append("' ).chosen({width: \"200\", allow_single_deselect:true, display_selected_options:false });");
                        html.append("ENDOFSCRIPT");
                    }
                } else if (TYPE_EDITORSTYLE.equals(this.type)) {
                    if (this.editorstyleid.length() > 0) {
                        StringBuilder idBuilder = new StringBuilder();
                        idBuilder.append(this.blockId).append("_").append(this.id).append("_[row]");
                        String id = idBuilder.toString();
                        EditorStyleField editorStyleField = new EditorStyleField(RuleEditorUtil.this.pageContext);
                        editorStyleField.setEditorStyleId(this.editorstyleid);
                        editorStyleField.setFieldName(id);
                        PropertyList column = new PropertyList();
                        column.setProperty("columnid", id);
                        if (this.mandatory.equals("Y")) {
                            column.setProperty("mandatory", "Y");
                        }
                        StringBuilder fieldOnChange = new StringBuilder();
                        fieldOnChange.append("ruleeditor.renderNextElement(this, '");
                        fieldOnChange.append(this.blockId + "_" + id).append("', [row]").append(")");
                        editorStyleField.setChangeEvent(fieldOnChange.toString());
                        String editorStyleFieldHtml = editorStyleField.getHtml();
                        editorStyleFieldHtml = editorStyleFieldHtml.replaceAll("&#x5B;", "[").replaceAll("&#x5b;", "[");
                        editorStyleFieldHtml = editorStyleFieldHtml.replaceAll("&#x5D;", "]").replaceAll("&#x5d;", "]");
                        editorStyleFieldHtml = editorStyleFieldHtml.replaceAll("</script>", "ENDOFSCRIPT");
                        html.append(editorStyleFieldHtml);
                    }
                } else if (TYPE_LOOKUP.equals(this.type)) {
                    html.append("<input id='").append(this.blockId).append("_").append(this.id).append("_[row]'  class='").append(cssClass).append("' ");
                    html.append(" onchange='ruleeditor.renderNextElement(this, \"").append(this.blockId + "_" + this.id).append("\", [row]").append(")'");
                    html.append(">");
                    html.append("<img title=\"Lookup\" border=\"0\" src=\"WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg\" class=\"lookup_img\">");
                } else if ("duration".equals(this.id) && TYPE_TEXT.equals(this.type)) {
                    html.append("<input id='").append(this.blockId).append("_").append(this.id).append("_[row]' ").append("type = 'text'  size='6' class='").append(cssClass).append("' ");
                    html.append(" onchange='ruleeditor.validateDuration( this );ruleeditor.renderNextElement(this, \"").append(this.blockId + "_" + this.id).append("\", [row]").append(")'");
                    html.append(">");
                } else if ("workiteminstance".equals(this.id) && TYPE_TEXT.equals(this.type)) {
                    html.append("<input id='").append(this.blockId).append("_").append(this.id).append("_[row]' ").append("type = 'text'  size='3' class='").append(cssClass).append("' ");
                    html.append(" onchange='ruleeditor.validateInstance( this );ruleeditor.renderNextElement(this, \"").append(this.blockId + "_" + this.id).append("\", [row]").append(")'");
                    html.append(">");
                } else if (TYPE_NUMBER.equals(this.type)) {
                    html.append("<input id='").append(this.blockId).append("_").append(this.id).append("_[row]' ").append("type = 'number'  class='").append(cssClass).append("' ");
                    if (this.min.length() > 0) {
                        html.append(" min='").append(this.min).append("' ");
                    }
                    if (this.max.length() > 0) {
                        html.append(" max='").append(this.max).append("' ");
                    }
                    html.append(" onchange='ruleeditor.renderNextElement(this, \"").append(this.blockId + "_" + this.id).append("\", [row]").append(")'");
                    html.append(">");
                    if (RuleEditorUtil.this.browser.isIE()) {
                        int spinnerWidth = 0;
                        String spinnerAttributes = "spin: function(event, ui) {";
                        spinnerAttributes = spinnerAttributes + "$(this).val(ui.value);";
                        spinnerAttributes = spinnerAttributes + "$(this).change();";
                        spinnerAttributes = spinnerAttributes + "}";
                        if (this.min.length() > 0) {
                            spinnerAttributes = spinnerAttributes + ",";
                            spinnerAttributes = spinnerAttributes + "min: " + this.min;
                        }
                        if (this.max.length() > 0) {
                            spinnerAttributes = spinnerAttributes + ",";
                            spinnerAttributes = spinnerAttributes + "max: " + this.max;
                            spinnerWidth = this.max.length() * 8;
                        }
                        html.append("<script>");
                        html.append(" $(function() {");
                        html.append("   $( '#").append(this.blockId).append("_").append(this.id).append("_[row]").append("' ).spinner({");
                        html.append(spinnerAttributes);
                        html.append("});");
                        html.append(" });");
                        if (spinnerWidth > 0) {
                            html.append("   $( '#").append(this.blockId).append("_").append(this.id).append("_[row]").append("' ).width(").append(spinnerWidth).append(");");
                        }
                        html.append("ENDOFSCRIPT");
                    }
                }
                this.templateHtml = html.toString();
            }
            return this.templateHtml;
        }

        private boolean hasNext(JSONObject row) throws JSONException {
            boolean hasNext = false;
            if (this.next.length() > 0) {
                hasNext = true;
            } else {
                String optionValue = "";
                if (row.has(this.id)) {
                    optionValue = (String)row.get(this.id);
                } else if (row.has(this.propertyid)) {
                    optionValue = (String)row.get(this.propertyid);
                }
                for (Option o : this.options) {
                    if (!o.id.equals(optionValue) && (!"workitemid".equals(this.id) && !"paramlistid".equals(this.id) || !o.value.equals(optionValue))) continue;
                    hasNext = o.next.length() > 0;
                    break;
                }
            }
            return hasNext;
        }

        public String getNextId(JSONObject row) throws JSONException {
            String nextId = "";
            if (this.next.length() > 0) {
                nextId = this.next;
            } else {
                String optionValue = "";
                if (row.has(this.id)) {
                    optionValue = (String)row.get(this.id);
                } else if (row.has(this.propertyid)) {
                    optionValue = (String)row.get(this.propertyid);
                }
                for (Option o : this.options) {
                    if (!o.id.equals(optionValue) && (!"workitemid".equals(this.id) && !"paramlistid".equals(this.id) || !o.value.equals(optionValue))) continue;
                    nextId = o.next;
                    break;
                }
            }
            return nextId;
        }

        public String getHtml(JSONObject row, int rowNumber) throws JSONException {
            String html = StringUtil.replaceAll(this.templateHtml, "[row]", rowNumber + "");
            html = StringUtil.replaceAll(html, "ENDOFSCRIPT", "</script>");
            StringBuilder js = new StringBuilder();
            js.append("\n").append("<script>").append("\n");
            String selectedVal = "";
            JSONArray selectedValJSONArray = null;
            if (row.has(this.id)) {
                if (this.multiselect.equals("Y")) {
                    selectedValJSONArray = row.getJSONArray(this.id);
                } else {
                    selectedVal = (String)row.get(this.id);
                }
            } else if (row.has(this.propertyid)) {
                if (this.multiselect.equals("Y")) {
                    selectedValJSONArray = row.getJSONArray(this.propertyid);
                } else {
                    selectedVal = (String)row.get(this.propertyid);
                }
            }
            if (selectedValJSONArray == null) {
                selectedValJSONArray = new JSONArray("[]");
            }
            if (TYPE_DROPDOWN.equals(this.type)) {
                if (this.multiselect.equals("Y")) {
                    for (int i = 0; i < selectedValJSONArray.length(); ++i) {
                        String tempVal = selectedValJSONArray.getString(i);
                        for (Option o : this.options) {
                            if (!o.id.equals(tempVal)) continue;
                            selectedValJSONArray.put(i, o.value);
                            if (o.observers.length() <= 0) continue;
                            this.selectedOptionObserverDataSet = o.observerDataSet;
                            this.selectedOptionObserverDataSetOptionHtml = o.observerDataSetOptionHtml;
                        }
                    }
                } else {
                    for (Option o : this.options) {
                        if (!o.id.equals(selectedVal) && (!"parameter".equals(o.observers) || !o.value.equals(selectedVal))) continue;
                        selectedVal = o.value;
                        if (o.observers.length() <= 0) continue;
                        this.selectedOptionObserverDataSet = o.observerDataSet;
                        this.selectedOptionObserverDataSetOptionHtml = o.observerDataSetOptionHtml;
                    }
                }
            }
            js.append("$( document ).ready( function () {").append("\n");
            if (this.selectedOptionObserverDataSet.length() > 0) {
                js.append("for ( var key in ruleeditor.elements ) {").append("\n");
                js.append("var observerElement = ruleeditor.elements[key];").append("\n");
                js.append("$( observerElement.options ).each( function ( observerindex, observeroption ) {").append("\n");
                js.append("if ( observeroption.observee == '" + this.propertyid + "') {").append("\n");
                js.append("observeroption.observeecurrentdataset = '" + this.selectedOptionObserverDataSet + "';").append("\n");
                js.append("observeroption.observeecurrentdatasetoptionhtml = \"" + this.selectedOptionObserverDataSetOptionHtml + "\";").append("\n");
                js.append("}");
                js.append("} );").append("\n");
                js.append("}").append("\n");
            }
            RuleEditorUtil.this.getBlockElementsAsMap(this.blockId);
            for (Option o : this.options) {
                if (o.observee.length() <= 0) continue;
                Element parentElement = RuleEditorUtil.this.getBlockElementsAsMap(this.blockId).get(o.observee);
                if (parentElement.selectedOptionObserverDataSetOptionHtml.length() <= 0) continue;
                String optionHtml = StringUtil.replaceAll(parentElement.selectedOptionObserverDataSetOptionHtml, "[row]", rowNumber + "");
                js.append("$('#").append(StringUtil.replaceAll(this.blockId + "_" + this.id + "_" + rowNumber, "[row]", rowNumber + "")).append("').append(\"" + optionHtml + "\")").append(";\n");
            }
            if (this.multiselect.equals("Y")) {
                js.append("var values=").append(selectedValJSONArray.toString()).append(";\n");
                js.append("$.each(values, function(i,e){\n");
                js.append("$('#").append(StringUtil.replaceAll(this.blockId + "_" + this.id + "_" + rowNumber, "[row]", rowNumber + "")).append(" option[value=\"' + e + '\"]').prop('selected', true )").append(";\n");
                js.append("});").append("\n");
                js.append("$('#").append(StringUtil.replaceAll(this.blockId + "_" + this.id + "_" + rowNumber, "[row]", rowNumber + "")).append("').trigger('chosen:updated');");
            } else {
                js.append("$('#").append(StringUtil.replaceAll(this.blockId + "_" + this.id + "_" + rowNumber, "[row]", rowNumber + "")).append("').val('").append(selectedVal).append("');").append("\n");
            }
            js.append("} );").append("\n");
            js.append("</script>").append("\n");
            html = html + js.toString();
            return html;
        }
    }

    public class SubBlock {
        String id = "";
        String sequence = "1";
        String blockId = "";

        public SubBlock(Block b) {
            this.blockId = b.id;
        }

        private String getBlankPrefix() throws SapphireException {
            StringBuilder html = new StringBuilder();
            List<Element> elementList = RuleEditorUtil.this.getBlockElementsAsList(this.blockId);
            if (elementList.size() > 0) {
                Element firstElement = elementList.get(0);
                String templateHtml = firstElement.getTemplateHtml();
                templateHtml = StringUtil.replaceAll(templateHtml, "[row]", "subblock" + this.id);
                html.append(templateHtml);
                StringBuilder sb = new StringBuilder("<style>");
                sb.append("\n").append("#").append(this.blockId).append("_").append(firstElement.id).append("_subblock").append(this.id).append("{");
                sb.append("visibility: hidden;");
                sb.append("}");
                sb.append("</style>");
                html.append((CharSequence)sb);
            }
            return html.toString();
        }
    }

    public class Block {
        String id = "";
        String value = "";
        String next = "";
    }
}

