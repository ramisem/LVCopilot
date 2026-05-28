/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementPdfHandler
 *  net.sf.jasperreports.engine.export.JRPdfExporter
 *  net.sf.jasperreports.engine.export.JRPdfExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementPdfHandler;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementPdfHandler
implements GenericElementPdfHandler {
    public void exportElement(JRPdfExporterContext exporterContext, JRGenericPrintElement element) {
        try {
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            ((JRPdfExporter)exporterContext.getExporterRef()).exportImage(htmlPrintElement.createImageFromElement(element));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

