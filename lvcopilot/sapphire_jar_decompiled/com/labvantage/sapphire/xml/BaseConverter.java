/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.JndiConverterJBoss;
import com.labvantage.sapphire.xml.JndiConverterWebLogic;
import com.labvantage.sapphire.xml.JndiConverterWebSphere;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import sapphire.SapphireException;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public abstract class BaseConverter {
    public static BaseConverter getInstance(String applicationServer) throws SapphireException {
        if (applicationServer == null || applicationServer.length() == 0) {
            throw new SapphireException("Application server not specified! Options: WebLogic or WebSphere.");
        }
        BaseConverter converter = null;
        if ("WebLogic".equalsIgnoreCase(applicationServer)) {
            converter = new JndiConverterWebLogic();
        } else if ("WebSphere".equalsIgnoreCase(applicationServer)) {
            converter = new JndiConverterWebSphere();
        } else if ("JBoss".equalsIgnoreCase(applicationServer) || "JBoss6x".equalsIgnoreCase(applicationServer) || "JBoss7x".equalsIgnoreCase(applicationServer)) {
            converter = new JndiConverterJBoss();
        } else {
            throw new SapphireException("Application server '" + applicationServer + "' not recognized.");
        }
        return converter;
    }

    public abstract void convertEjbJndiName(File var1, File var2, String var3) throws SapphireException;

    public void changeSapphireHome(File source, File target, String sapphireHome) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false, "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN");
        this.changeSapphireHome(dom, sapphireHome);
        DOMUtil.saveRawXml(dom, target != null ? target : source);
    }

    private Document changeSapphireHome(Document dom, String sapphireHome) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("context-param");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                String paramName = BaseConverter.getTextValue(el, "param-name");
                if (!"SAPPHIRE_HOME".equals(paramName)) continue;
                this.setTextValue(el, "param-value", sapphireHome, dom);
            }
        }
        return dom;
    }

    public void changeApplicationName(File source, File target, String applicationName) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false, "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN");
        this.changeApplicationName(dom, applicationName);
        DOMUtil.saveRawXml(dom, target != null ? target : source);
    }

    private Document changeApplicationName(Document dom, String applicationName) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("context-param");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                String paramName = BaseConverter.getTextValue(el, "param-name");
                if (!"applicationid".equals(paramName)) continue;
                this.setTextValue(el, "param-value", applicationName, dom);
            }
        }
        return dom;
    }

    public void changeWARName(File source, File target, String applicationName) throws SapphireException {
    }

    public void changeWebContextName(File source, File target, String applicationWebContext, String applicationWarName) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false, "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN");
        this.changeWebContextName(dom, applicationWebContext, applicationWarName);
        DOMUtil.save(dom, target != null ? target : source);
    }

    private Document changeWebContextName(Document dom, String sapphireWeb, String sapphireWarName) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("web");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                String warName = BaseConverter.getTextValue(el, "web-uri");
                if (!"labvantage.war".equals(warName)) continue;
                this.setTextValue(el, "web-uri", sapphireWarName);
                this.setTextValue(el, "context-root", sapphireWeb);
            }
        }
        return dom;
    }

    public void configurePortalXML(File source, File target, List<PropertyList> portalProperties) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false, "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN");
        this.configurePortalXML(dom, portalProperties);
        DOMUtil.saveRawXml(dom, target != null ? target : source);
    }

    private void configurePortalXML(Document dom, List<PropertyList> portalProperties) throws SapphireException {
        NodeList nodes;
        Element docEle = dom.getDocumentElement();
        try {
            nodes = docEle.getElementsByTagName("session-config");
            if (nodes.getLength() == 1) {
                Element el = (Element)nodes.item(0);
                NodeList cookieConfigNodelist = el.getElementsByTagName("cookie-config");
                if (cookieConfigNodelist.getLength() == 1) {
                    String secureParam;
                    Element cookieEle = (Element)cookieConfigNodelist.item(0);
                    String httpParam = BaseConverter.getTextValue(cookieEle, "http-only");
                    if (!httpParam.equals("true")) {
                        this.setTextValue(cookieEle, "http-only", "true");
                    }
                    if (!(secureParam = BaseConverter.getTextValue(cookieEle, "secure")).equals("true")) {
                        this.setTextValue(cookieEle, "secure", "true");
                    }
                } else {
                    Element cookieConfig = dom.createElement("cookie-config");
                    BaseConverter.addTextNode(cookieConfig, "http-only", "true", dom);
                    BaseConverter.addTextNode(cookieConfig, "secure", "true", dom);
                    el.appendChild(cookieConfig);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Error configuring web.xml for Portal. Couldn't configure cookie-config: ", e.getMessage(), e);
        }
        Element lastServletElement = null;
        nodes = docEle.getElementsByTagName("servlet");
        for (int i = 0; i < nodes.getLength(); ++i) {
            Element currentEl = (Element)nodes.item(i);
            String servletName = BaseConverter.getTextValue(currentEl, "servlet-name");
            if (!servletName.equals("PortalController")) continue;
            lastServletElement = currentEl;
        }
        if (lastServletElement == null) {
            if (nodes.getLength() > 0) {
                lastServletElement = (Element)nodes.item(nodes.getLength() - 1);
            } else {
                throw new SapphireException("Error configuring web.xml. Servlet not found.");
            }
        }
        ArrayList<String> portalServletList = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Element el = (Element)nodes.item(i);
            String servletName = BaseConverter.getTextValue(el, "servlet-name");
            String initParam = BaseConverter.getInitParam(el, "portal");
            if (servletName.isEmpty() || initParam.isEmpty()) continue;
            portalServletList.add(servletName);
        }
        for (PropertyList portalConfig : portalProperties) {
            int i;
            Element servletMappingElement;
            String servletName;
            int i2;
            String servlet = portalConfig.getProperty("servlet", "");
            Object portal = portalConfig.getProperty("portal", "");
            String database = portalConfig.getProperty("databaseid", "");
            String urlpattern = portalConfig.getProperty("urlpattern", "");
            String debug = portalConfig.getProperty("debug", "");
            String encrypt = portalConfig.getProperty("encrypt", "");
            String encryptCookies = portalConfig.getProperty("encryptcookies", "");
            String autorefreshproperties = portalConfig.getProperty("autorefreshproperties", "");
            if (servlet.isEmpty() && ((String)portal).isEmpty() && database.isEmpty() && urlpattern.isEmpty()) {
                servlet = "PortalController";
                portal = "[the portal id here]";
                database = "[the database id here]";
                urlpattern = "/portal/*";
            } else if (servlet.isEmpty() || ((String)portal).isEmpty() || database.isEmpty() || urlpattern.isEmpty()) {
                throw new SapphireException("Portal configuration error. Required parameter missing.");
            }
            boolean servletExist = false;
            for (i2 = 0; i2 < nodes.getLength(); ++i2) {
                Element el = (Element)nodes.item(i2);
                servletName = BaseConverter.getTextValue(el, "servlet-name");
                if (!servlet.equalsIgnoreCase(servletName)) continue;
                servletExist = true;
                portalServletList.remove(servletName);
            }
            if (!servletExist) {
                try {
                    Element servletElement = dom.createElement("servlet");
                    BaseConverter.addTextNode(servletElement, "display-name", servlet, dom);
                    BaseConverter.addTextNode(servletElement, "servlet-name", servlet, dom);
                    BaseConverter.addTextNode(servletElement, "servlet-class", "com.labvantage.stellar.servlet.PortalController", dom);
                    BaseConverter.addTextNode(servletElement, "load-on-startup", "1", dom);
                    Element parent = (Element)lastServletElement.getParentNode();
                    parent.insertBefore(servletElement, lastServletElement.getNextSibling());
                    servletMappingElement = dom.createElement("servlet-mapping");
                    BaseConverter.addTextNode(servletMappingElement, "servlet-name", servlet, dom);
                    BaseConverter.addTextNode(servletMappingElement, "url-pattern", urlpattern, dom);
                    parent.insertBefore(servletMappingElement, servletElement.getNextSibling());
                    lastServletElement = servletMappingElement;
                }
                catch (Exception e) {
                    throw new SapphireException("Error configuring web.xml for Portal. Couldn't add servlet: " + e.getMessage(), e);
                }
            }
            try {
                for (i2 = 0; i2 < nodes.getLength(); ++i2) {
                    Element servletElement = (Element)nodes.item(i2);
                    servletName = BaseConverter.getTextValue(servletElement, "servlet-name");
                    if (!servletName.equals(servlet)) continue;
                    this.configureInitParam(servletElement, "portal", (String)portal, dom);
                    this.configureInitParam(servletElement, "database", database, dom);
                    this.configureInitParam(servletElement, "debug", debug, dom);
                    this.configureInitParam(servletElement, "encrypt", encrypt, dom);
                    this.configureInitParam(servletElement, "encryptcookies", encryptCookies, dom);
                    this.configureInitParam(servletElement, "autorefreshproperties", autorefreshproperties, dom);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Error configuring web.xml for Portal. Couldn't configure servlet: " + e.getMessage(), e);
            }
            try {
                NodeList servletMappingNodes = docEle.getElementsByTagName("servlet-mapping");
                for (i = 0; i < servletMappingNodes.getLength(); ++i) {
                    servletMappingElement = (Element)servletMappingNodes.item(i);
                    String servletName2 = BaseConverter.getTextValue(servletMappingElement, "servlet-name");
                    if (!servletName2.equals(servlet)) continue;
                    this.setTextValue(servletMappingElement, "url-pattern", urlpattern);
                    break;
                }
            }
            catch (Exception e) {
                throw new SapphireException("Error configuring web.xml for Portal. Couldn't configure servlet-mapping: " + e.getMessage(), e);
            }
            try {
                NodeList filterMappingNodes = docEle.getElementsByTagName("filter-mapping");
                block36: for (i = 0; i < filterMappingNodes.getLength(); ++i) {
                    String filterName;
                    Element filterMappingElement = (Element)filterMappingNodes.item(i);
                    switch (filterName = BaseConverter.getTextValue(filterMappingElement, "filter-name")) {
                        case "AddHeaders": 
                        case "AddHeaders_X_FRAME_OPTIONS": {
                            String filterUrlPattern = BaseConverter.getTextValue(filterMappingElement, "url-pattern");
                            if (filterUrlPattern == null || !filterUrlPattern.equals("/portal/*") || urlpattern.equals(BaseConverter.findTextValue(filterMappingElement, "url-pattern", urlpattern))) continue block36;
                            BaseConverter.addTextNode(filterMappingElement, "url-pattern", urlpattern, dom);
                            continue block36;
                        }
                        case "Compression": {
                            String compressionServletName = BaseConverter.getTextValue(filterMappingElement, "servlet-name");
                            if (compressionServletName == null || !compressionServletName.equals("PortalController") || servlet.equals(BaseConverter.findTextValue(filterMappingElement, "servlet-name", servlet))) continue block36;
                            BaseConverter.addTextNode(filterMappingElement, "servlet-name", servlet, dom);
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException("Error configuring web.xml for Portal. Couldn't configure filter-mapping: " + e.getMessage(), e);
            }
        }
        NodeList servletMappingNodes = docEle.getElementsByTagName("servlet-mapping");
        NodeList filterMappingNodes = docEle.getElementsByTagName("filter-mapping");
        String urlPattern = "";
        for (String portal : portalServletList) {
            for (int i = 0; i < nodes.getLength(); ++i) {
                int j;
                Element el = (Element)nodes.item(i);
                String servletName = BaseConverter.getTextValue(el, "servlet-name");
                if (!servletName.equals(portal)) continue;
                el.getParentNode().removeChild(el);
                for (j = 0; j < servletMappingNodes.getLength(); ++j) {
                    Element servletMappingElement = (Element)servletMappingNodes.item(j);
                    String servletMappingName = BaseConverter.getTextValue(servletMappingElement, "servlet-name");
                    if (!servletMappingName.equals(servletName)) continue;
                    urlPattern = BaseConverter.getTextValue(servletMappingElement, "url-pattern");
                    servletMappingElement.getParentNode().removeChild(servletMappingElement);
                }
                for (j = 0; j < filterMappingNodes.getLength(); ++j) {
                    String filterMappingName;
                    Element filterMappingElement = (Element)filterMappingNodes.item(j);
                    switch (filterMappingName = BaseConverter.getTextValue(filterMappingElement, "filter-name")) {
                        case "AddHeaders": 
                        case "AddHeaders_X_FRAME_OPTIONS": {
                            String textVal;
                            Element ele;
                            int k;
                            NodeList nl = filterMappingElement.getElementsByTagName("url-pattern");
                            for (k = 0; k < nl.getLength(); ++k) {
                                ele = (Element)nl.item(k);
                                textVal = ele.getFirstChild().getNodeValue();
                                if (!textVal.equals(urlPattern)) continue;
                                ele.getParentNode().removeChild(ele);
                            }
                            break;
                        }
                        case "Compression": {
                            String textVal;
                            Element ele;
                            int k;
                            NodeList nl = filterMappingElement.getElementsByTagName("servlet-name");
                            for (k = 0; k < nl.getLength(); ++k) {
                                ele = (Element)nl.item(k);
                                textVal = ele.getFirstChild().getNodeValue();
                                if (!textVal.equals(servletName)) continue;
                                ele.getParentNode().removeChild(ele);
                            }
                            break;
                        }
                    }
                    if (!filterMappingName.equals(servletName)) continue;
                    filterMappingElement.getParentNode().removeChild(filterMappingElement);
                }
            }
        }
        if (portalProperties.size() > 0) {
            try {
                String portalonly = portalProperties.get(0).getProperty("portalonly", "N");
                NodeList filterNodes = docEle.getElementsByTagName("filter");
                for (int i = 0; i < filterNodes.getLength(); ++i) {
                    Element filterElement = (Element)filterNodes.item(i);
                    String filterName = BaseConverter.getTextValue(filterElement, "filter-name");
                    if (!filterName.equals("PortalRequest")) continue;
                    this.configureInitParam(filterElement, "enabled", portalonly, dom);
                    break;
                }
            }
            catch (Exception e) {
                throw new SapphireException("Error configuring web.xml for Portal. Couldn't configure Portal-only setting: ", e.getMessage(), e);
            }
        }
    }

    protected static void addTextNode(Element newElement, String text, String servlet, Document dom) {
        Element node = dom.createElement(text);
        Text textNode = dom.createTextNode(servlet);
        node.appendChild(textNode);
        newElement.appendChild(node);
    }

    public static String getInitParam(Element ele, String tagName) {
        if (tagName.isEmpty()) {
            return "";
        }
        NodeList initParamNodelist = ele.getElementsByTagName("init-param");
        if (initParamNodelist.getLength() > 0) {
            for (int i = 0; i < initParamNodelist.getLength(); ++i) {
                Element servletEle = (Element)initParamNodelist.item(i);
                String initParamName = BaseConverter.getTextValue(servletEle, "param-name");
                if (!initParamName.equals(tagName)) continue;
                return BaseConverter.getTextValue(servletEle, "param-value");
            }
        }
        return "";
    }

    protected void configureInitParam(Element ele, String tagName, String newValue, Document doc) {
        if (tagName.isEmpty()) {
            return;
        }
        boolean paramFound = false;
        NodeList initParamNodelist = ele.getElementsByTagName("init-param");
        if (initParamNodelist.getLength() > 0) {
            for (int i = 0; i < initParamNodelist.getLength(); ++i) {
                Element servletEle = (Element)initParamNodelist.item(i);
                String initParamName = BaseConverter.getTextValue(servletEle, "param-name");
                if (!initParamName.equals(tagName)) continue;
                this.setTextValue(servletEle, "param-value", newValue);
                paramFound = true;
            }
        }
        if (!paramFound) {
            Element initElement = doc.createElement("init-param");
            Element node = doc.createElement("param-name");
            Text text = doc.createTextNode(tagName);
            node.appendChild(text);
            initElement.appendChild(node);
            ele.appendChild(initElement);
            node = doc.createElement("param-value");
            text = doc.createTextNode(newValue);
            node.appendChild(text);
            initElement.appendChild(node);
            ele.appendChild(initElement);
        }
    }

    public static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    public static String findTextValue(Element ele, String tagName, String value) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                for (Node child = el.getFirstChild(); child != null; child = child.getNextSibling()) {
                    textVal = child.getNodeValue();
                    if (!textVal.equals(value)) continue;
                    return value;
                }
            }
        }
        return textVal;
    }

    protected void setTextValue(Element ele, String tagName, String newValue) {
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            el.getFirstChild().setNodeValue(newValue);
        }
    }

    protected void setTextValue(Element ele, String tagName, String newValue, Document doc) {
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            Node temp = el.getFirstChild();
            if (temp == null) {
                el.setNodeValue(newValue);
                Text t = doc.createTextNode(newValue);
                el.appendChild(t);
            } else {
                temp.setNodeValue(newValue);
            }
        }
    }
}

