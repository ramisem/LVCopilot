/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xpath.XPathAPI
 */
package sapphire.xml;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.Trace;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class DOMUtil
extends BaseClass {
    public static Cache cache = new Cache("DOM Util", 500);
    public static final String WEBLOGIC_EJB_DTD = "-//BEA Systems, Inc.//DTD WebLogic 7.0.0 EJB//EN";
    public static final String JBOSS_EJB_DTD = "-//JBoss//DTD JBOSS//EN";
    public static final String J2EE_APPLICATION_DTD = "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN";
    public static final String J2EE_WEBAPP_DTD = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String J2EE_EJB_DTD = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
    public static final String CDATAESCAPE = "!]!]!>";

    public static Document getNewDocument(Object xml, boolean cacheDocument, final String noValidatePublicId) throws SapphireException {
        Document document;
        Document document2 = document = cacheDocument ? (Document)cache.get(xml.toString()) : null;
        if (document == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                factory.setExpandEntityReferences(false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                if (noValidatePublicId != null && noValidatePublicId.length() > 0) {
                    builder.setEntityResolver(new EntityResolver(){

                        @Override
                        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                            if (publicId.equals(noValidatePublicId)) {
                                return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                            }
                            return null;
                        }
                    });
                }
                if (xml instanceof File) {
                    document = builder.parse((File)xml);
                } else if (xml instanceof String) {
                    if (((String)xml).indexOf("<!ENTITY ") >= 0) {
                        throw new SapphireException("ENTITY tag not allowed in XML:" + (((String)xml).length() < 1000 ? xml : ((String)xml).substring(0, 1000)));
                    }
                    if (xml == null || ((String)xml).length() == 0) {
                        xml = "<document/>";
                    }
                    StringReader reader = new StringReader((String)xml);
                    InputSource insource = new InputSource(reader);
                    document = builder.parse(insource);
                }
            }
            catch (ParserConfigurationException e) {
                throw new SapphireException("ParserConfigurationException: " + e.getMessage());
            }
            catch (SAXException e) {
                throw new SapphireException("SAXException: " + e.getMessage());
            }
            catch (IOException e) {
                throw new SapphireException("IOException: " + e.getMessage());
            }
            if (cacheDocument) {
                cache.put(xml.toString(), document);
            }
        }
        return document;
    }

    public static Document getNewDocument(Object xml) throws SapphireException {
        return DOMUtil.getNewDocument(xml, true);
    }

    public static Document getNewDocument(Object xml, boolean cacheDocument) throws SapphireException {
        return DOMUtil.getNewDocument(xml, cacheDocument, "");
    }

    public static Document getNewDocument() throws SapphireException {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new SapphireException("ParserConfigurationException: " + e.getMessage());
        }
        return document;
    }

    public static Node getChildElement(Node parentNode, String elementName) {
        Node child;
        for (child = parentNode.getFirstChild(); !(child == null || child.getNodeType() == 1 && child.getNodeName().equals(elementName)); child = child.getNextSibling()) {
        }
        return child;
    }

    public static List getChildElements(Node parentNode, String elementName) {
        ArrayList<Node> elementList = new ArrayList<Node>();
        if (parentNode != null) {
            for (Node element = parentNode.getFirstChild(); element != null; element = element.getNextSibling()) {
                if (element.getNodeType() != 1 || !element.getNodeName().equals(elementName)) continue;
                elementList.add(element);
            }
        }
        return elementList;
    }

    public static Element getElementByAttribute(String attributeId, String attributeValue, List<Element> elements) {
        if (elements != null) {
            for (Element element : elements) {
                if (!attributeValue.equals(element.getAttribute(attributeId))) continue;
                return element;
            }
        }
        return null;
    }

    public static HashMap getAttributes(Element element) {
        NamedNodeMap attr = element.getAttributes();
        HashMap<String, String> attributes = new HashMap<String, String>();
        if (attr != null && attr.getLength() > 0) {
            for (int i = 0; i < attr.getLength(); ++i) {
                Node a = attr.item(i);
                String nodevalue = a.getNodeValue();
                if (nodevalue == null || nodevalue.length() <= 0) continue;
                attributes.put(a.getNodeName(), a.getNodeValue());
            }
        }
        return attributes;
    }

    public static String convertChars(String text) {
        StringBuffer output = new StringBuffer(text.length());
        block7: for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '&': {
                    output.append("&amp;");
                    continue block7;
                }
                case '<': {
                    output.append("&lt;");
                    continue block7;
                }
                case '>': {
                    output.append("&gt;");
                    continue block7;
                }
                case '\"': {
                    output.append("&quot;");
                    continue block7;
                }
                case '\'': {
                    output.append("&#039;");
                    continue block7;
                }
                default: {
                    output.append(c);
                }
            }
        }
        return output.toString();
    }

    private static void writenode(Node node, StringBuffer out, int indent, boolean useCDATA) {
        try {
            String spaces = "                                                                                                                                                                                                                                                                                                                                                                                                  ";
            out.append("\n").append(spaces.substring(0, indent * 2)).append("<").append(node.getNodeName());
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); ++i) {
                Node a = attributes.item(i);
                out.append(" ").append(a.getNodeName()).append("=\"").append(a.getNodeValue()).append("\"");
            }
            NodeList nl = node.getChildNodes();
            int size = nl.getLength();
            if (size == 0) {
                out.append(" />");
            } else {
                int i;
                out.append(">");
                boolean haselements = false;
                for (i = 0; i < size; ++i) {
                    if (nl.item(i).getNodeType() != 1) continue;
                    haselements = true;
                }
                for (i = 0; i < size; ++i) {
                    Node item = nl.item(i);
                    if (item.getNodeType() == 1) {
                        DOMUtil.writenode(nl.item(i), out, indent + 1, useCDATA);
                        continue;
                    }
                    if (!haselements) {
                        if (item.getNodeType() == 3) {
                            if (useCDATA) {
                                out.append("<![CDATA[").append(StringUtil.replaceAll(item.getNodeValue(), "]]>", CDATAESCAPE)).append("]]>");
                                continue;
                            }
                            out.append(item.getNodeValue());
                            continue;
                        }
                    }
                    if (haselements) continue;
                    if (item.getNodeType() != 4) continue;
                    if (useCDATA) {
                        out.append("<![CDATA[").append(StringUtil.replaceAll(item.getNodeValue(), "]]>", CDATAESCAPE)).append("]]>");
                        continue;
                    }
                    out.append(item.getNodeValue());
                }
                if (haselements) {
                    out.append("\n").append(spaces.substring(0, indent * 2));
                }
                out.append("</").append(node.getNodeName()).append(">");
            }
        }
        catch (Exception e) {
            Trace.log("DOMUTIL", "ERROR: exception:" + e);
        }
    }

    public static Element findNode(Node ptreenode, String nodeid) throws TransformerException {
        return (Element)XPathAPI.selectSingleNode((Node)ptreenode, (String)("//node[@id='" + nodeid + "']"));
    }

    public static ArrayList getAllNodes(Node node) throws TransformerException {
        ArrayList<String> allnodes = new ArrayList<String>();
        if (node != null) {
            NodeList nl = XPathAPI.selectNodeList((Node)node, (String)".//node");
            for (int i = 0; i < nl.getLength(); ++i) {
                allnodes.add(((Element)nl.item(i)).getAttribute("id"));
            }
        }
        return allnodes;
    }

    public static String toString(Node node) {
        return DOMUtil.toString(node, false, true);
    }

    public static String toString(Node node, boolean addHeader, boolean useCDATA) {
        StringBuffer out = new StringBuffer(1024);
        if (addHeader) {
            out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.append("<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\" \"http://java.sun.com/dtd/application_1_3.dtd\">");
        }
        DOMUtil.writenode(node, out, 0, useCDATA);
        return out.toString();
    }

    public static String toString(Node node, String header, boolean useCDATA) {
        StringBuffer out = new StringBuffer(1024);
        out.append(header);
        DOMUtil.writenode(node, out, 0, useCDATA);
        return out.toString();
    }

    public static void saveRawXml(Document dom, File file) throws SapphireException {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "no");
            transformer.setOutputProperty("method", "xml");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(new DOMSource(dom), new StreamResult(sw));
            try (FileOutputStream fos = new FileOutputStream(file);){
                fos.write(sw.toString().getBytes());
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save document in '" + file.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
        }
    }

    public static void save(Document dom, File file) throws SapphireException {
        try {
            DocumentType dt = dom.getDoctype();
            StringBuffer header = new StringBuffer("<?xml version=\"1.0\"?>\n");
            if (dt != null) {
                header.append("<!DOCTYPE ").append(dt.getName()).append(" PUBLIC \"").append(dt.getPublicId()).append("\" \"").append(dt.getSystemId()).append("\" >\n");
            }
            Element contextNode = dom.getDocumentElement();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(DOMUtil.toString((Node)contextNode, header.toString(), false).getBytes());
            fos.close();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save document in '" + file.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
        }
    }
}

