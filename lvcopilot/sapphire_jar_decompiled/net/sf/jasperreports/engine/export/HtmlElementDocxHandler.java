/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.ooxml.GenericElementDocxHandler
 *  net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
 *  net.sf.jasperreports.engine.export.ooxml.JRDocxExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.ooxml.GenericElementDocxHandler;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementDocxHandler
implements GenericElementDocxHandler {
    public void exportElement(JRDocxExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell) {
        JRDocxExporter exporter = (JRDocxExporter)exporterContext.getExporterRef();
        try {
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(exporterContext.getTableHelper(), htmlPrintElement.createImageFromElement(element), gridCell);
        }
        catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

