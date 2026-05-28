/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementXmlHandler
 *  net.sf.jasperreports.engine.export.JRXmlExporter
 *  net.sf.jasperreports.engine.export.JRXmlExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementXmlHandler;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.JRXmlExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementXmlHandler
implements GenericElementXmlHandler {
    public void exportElement(JRXmlExporterContext exporterContext, JRGenericPrintElement element) {
        try {
            JRXmlExporter exporter = (JRXmlExporter)exporterContext.getExporterRef();
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(htmlPrintElement.createImageFromElement(element));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

