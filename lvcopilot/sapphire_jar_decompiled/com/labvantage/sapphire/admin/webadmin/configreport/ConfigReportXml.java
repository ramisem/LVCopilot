/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.sapphire.admin.webadmin.configreport.BaseConfigReport;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sapphire.xml.DOMUtil;

public class ConfigReportXml
extends BaseConfigReport {
    private Document document;
    private Element root;
    private Element webpagesNode;
    private Element webpageNode;
    private Element buttonsNode;
    private Element listColumnsNode;
    private Element maintColumnsNode;

    @Override
    public void beginReport() throws Exception {
        super.initialize(this.folderLocation, this.fileName, this.imageRoot, this.reportTitle, this.reportStyles);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            DocumentBuilder loader = factory.newDocumentBuilder();
            this.document = loader.newDocument();
            this.root = this.document.createElement("sapphireconfiguration");
            this.document.appendChild(this.root);
            this.webpagesNode = this.document.createElement("webpages");
            this.root.appendChild(this.webpagesNode);
        }
        catch (ParserConfigurationException e) {
            throw new Exception(e);
        }
    }

    @Override
    public void nextWebPage(ConfigReportRequestHandler.WebPage webpage, boolean includeDetails) {
        this.webpageNode = this.document.createElement("webpage");
        this.webpagesNode.appendChild(this.webpageNode);
        this.webpageNode.setAttribute("id", webpage.webpageid);
        this.webpageNode.setAttribute("productedition", webpage.productedition);
        if (includeDetails) {
            this.addTextElement(this.webpageNode, "description", webpage.description);
            this.addTextElement(this.webpageNode, "pagetype", webpage.pagetype);
            this.addTextElement(this.webpageNode, "virtualpage", "" + webpage.virtualpage);
            this.addTextElement(this.webpageNode, "rolelist", webpage.rolelist);
        }
    }

    @Override
    public void beginButtons(boolean hasButtons) {
        this.buttonsNode = this.document.createElement("buttons");
        this.webpageNode.appendChild(this.buttonsNode);
    }

    @Override
    public void nextButton(ConfigReportRequestHandler.Button button) {
        Element buttonNode = this.document.createElement("button");
        this.buttonsNode.appendChild(buttonNode);
        if (button.id.length() > 0) {
            buttonNode.setAttribute("id", button.id);
        }
        if (button.type.length() > 0) {
            buttonNode.setAttribute("type", button.type);
        }
        this.addTextElement(buttonNode, "text", button.text);
        this.addTextElement(buttonNode, "image", button.image);
        this.addTextElement(buttonNode, "rolelist", button.rolelist);
        this.addTextElement(buttonNode, "operation", button.operation);
    }

    @Override
    public void endButtons(boolean hasButtons) {
    }

    @Override
    public void beginListColumns(boolean hasColumns) {
        this.listColumnsNode = this.document.createElement("listcolumns");
        this.webpageNode.appendChild(this.listColumnsNode);
    }

    @Override
    public void nextListColumn(ConfigReportRequestHandler.ListColumn column) {
        Element columnNode = this.document.createElement("column");
        this.listColumnsNode.appendChild(columnNode);
        if (column.id.length() > 0) {
            columnNode.setAttribute("id", column.id);
        }
        this.addTextElement(columnNode, "title", column.title);
        this.addTextElement(columnNode, "sdcid", column.sdcid);
        this.addTextElement(columnNode, "columnid", column.columnid);
        this.addTextElement(columnNode, "link", column.link);
        this.addTextElement(columnNode, "rolelist", column.rolelist);
    }

    @Override
    public void endListColumns(boolean hasColumns) {
    }

    @Override
    public void beginMaintColumns(boolean hasColumns) {
        this.maintColumnsNode = this.document.createElement("maintcolumns");
        this.webpageNode.appendChild(this.maintColumnsNode);
    }

    @Override
    public void nextMaintColumn(ConfigReportRequestHandler.MaintColumn column) {
        Element columnNode = this.document.createElement("column");
        this.maintColumnsNode.appendChild(columnNode);
        if (column.id.length() > 0) {
            columnNode.setAttribute("id", column.id);
        }
        this.addTextElement(columnNode, "title", column.title);
        this.addTextElement(columnNode, "sdcid", column.sdcid);
        this.addTextElement(columnNode, "columnid", column.columnid);
        this.addTextElement(columnNode, "displaytype", column.mode);
        this.addTextElement(columnNode, "validationrule", column.validationrule);
        this.addTextElement(columnNode, "defaultvalue", column.defaultvalue);
        this.addTextElement(columnNode, "rolelist", column.rolelist);
    }

    @Override
    public void endMaintColumns(boolean hasColumns) {
    }

    private void addTextElement(Element parentNode, String elementType, String text) {
        if (text.trim().length() > 0) {
            Element element = this.document.createElement(elementType);
            element.appendChild(this.document.createTextNode(text));
            parentNode.appendChild(element);
        }
    }

    @Override
    public void endReport() {
    }

    @Override
    public String getFinalOutput() {
        return DOMUtil.toString(this.root);
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }
}

