/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.components.AbstractComponentXmlWriter
 *  net.sf.jasperreports.engine.DefaultJasperReportsContext
 *  net.sf.jasperreports.engine.JRComponentElement
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.engine.component.Component
 *  net.sf.jasperreports.engine.component.ComponentKey
 *  net.sf.jasperreports.engine.type.EvaluationTimeEnum
 *  net.sf.jasperreports.engine.type.JREnum
 *  net.sf.jasperreports.engine.type.NamedEnum
 *  net.sf.jasperreports.engine.util.JRXmlWriteHelper
 *  net.sf.jasperreports.engine.util.XmlNamespace
 *  net.sf.jasperreports.engine.xml.JRXmlWriter
 */
package net.sf.jasperreports.components.html;

import java.io.IOException;
import net.sf.jasperreports.components.AbstractComponentXmlWriter;
import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.component.Component;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.JREnum;
import net.sf.jasperreports.engine.type.NamedEnum;
import net.sf.jasperreports.engine.util.JRXmlWriteHelper;
import net.sf.jasperreports.engine.util.XmlNamespace;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

public class HtmlComponentXmlWriter
extends AbstractComponentXmlWriter {
    public HtmlComponentXmlWriter() {
        super((JasperReportsContext)DefaultJasperReportsContext.getInstance());
    }

    public HtmlComponentXmlWriter(JasperReportsContext jasperReportsContext) {
        super(jasperReportsContext);
    }

    public boolean isToWrite(JRComponentElement componentElement, JRXmlWriter reportWriter) {
        return true;
    }

    public void writeToXml(JRComponentElement componentElement, JRXmlWriter reportWriter) throws IOException {
        Component component = componentElement.getComponent();
        if (component instanceof HtmlComponent) {
            this.writeHtmlComponent(componentElement, reportWriter);
        }
    }

    protected void writeHtmlComponent(JRComponentElement componentElement, JRXmlWriter reportWriter) throws IOException {
        Component component = componentElement.getComponent();
        HtmlComponent htmlComponent = (HtmlComponent)component;
        ComponentKey componentKey = componentElement.getComponentKey();
        XmlNamespace namespace = new XmlNamespace("http://jasperreports.sourceforge.net/htmlcomponent", componentKey.getNamespacePrefix(), "http://jasperreports.sourceforge.net/xsd/htmlcomponent.xsd");
        JRXmlWriteHelper writer = reportWriter.getXmlWriteHelper();
        writer.startElement("html", namespace);
        writer.addAttribute("scaleType", (JREnum)htmlComponent.getScaleType());
        writer.addAttribute("horizontalAlign", (NamedEnum)htmlComponent.getHorizontalImageAlign());
        writer.addAttribute("verticalAlign", (NamedEnum)htmlComponent.getVerticalImageAlign());
        this.writeExpression("htmlContentExpression", htmlComponent.getHtmlContentExpression(), false, componentElement, reportWriter);
        if (htmlComponent.getEvaluationTime() != EvaluationTimeEnum.NOW) {
            writer.addAttribute("evaluationTime", (JREnum)htmlComponent.getEvaluationTime());
        }
        writer.addAttribute("evaluationGroup", htmlComponent.getEvaluationGroup());
        writer.addAttribute("clipOnOverflow", (Object)htmlComponent.getClipOnOverflow());
        writer.closeElement();
    }
}

