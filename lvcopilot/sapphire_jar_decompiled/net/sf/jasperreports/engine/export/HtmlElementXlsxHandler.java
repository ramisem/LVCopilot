/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.JRPrintImage
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.ooxml.GenericElementXlsxHandler
 *  net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
 *  net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.ooxml.GenericElementXlsxHandler;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementXlsxHandler
implements GenericElementXlsxHandler {
    public void exportElement(JRXlsxExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell, int colIndex, int rowIndex) {
        try {
            JRXlsxExporter exporter = (JRXlsxExporter)exporterContext.getExporterRef();
            exporter.exportImage(this.getImage(exporterContext, element), gridCell, colIndex, rowIndex, 0, 0, null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }

    public JRPrintImage getImage(JRXlsxExporterContext exporterContext, JRGenericPrintElement element) throws JRException {
        HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
        return htmlPrintElement.createImageFromElement(element);
    }
}

