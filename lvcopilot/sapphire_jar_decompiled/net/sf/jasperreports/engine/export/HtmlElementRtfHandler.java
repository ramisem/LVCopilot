/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementRtfHandler
 *  net.sf.jasperreports.engine.export.JRRtfExporter
 *  net.sf.jasperreports.engine.export.JRRtfExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementRtfHandler;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementRtfHandler
implements GenericElementRtfHandler {
    public void exportElement(JRRtfExporterContext exporterContext, JRGenericPrintElement element) {
        try {
            JRRtfExporter exporter = (JRRtfExporter)exporterContext.getExporterRef();
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

