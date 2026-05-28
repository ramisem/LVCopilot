/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.ooxml.GenericElementPptxHandler
 *  net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
 *  net.sf.jasperreports.engine.export.ooxml.JRPptxExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.ooxml.GenericElementPptxHandler;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementPptxHandler
implements GenericElementPptxHandler {
    public void exportElement(JRPptxExporterContext exporterContext, JRGenericPrintElement element) {
        JRPptxExporter exporter = (JRPptxExporter)exporterContext.getExporterRef();
        try {
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(htmlPrintElement.createImageFromElement(element));
        }
        catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

