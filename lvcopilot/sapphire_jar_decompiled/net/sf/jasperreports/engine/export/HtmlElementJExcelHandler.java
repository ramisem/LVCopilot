/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementHandler
 *  net.sf.jasperreports.engine.export.JRExporterContext
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.JRGridLayout
 *  net.sf.jasperreports.engine.export.JRXlsExporter
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementHandler;
import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRGridLayout;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementJExcelHandler
implements GenericElementHandler {
    public void exportElement(JRExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell, int colIndex, int rowIndex, int emptyCols, int yCutsRow, JRGridLayout layout) {
        try {
            JRXlsExporter exporter = (JRXlsExporter)exporterContext.getExporter();
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(htmlPrintElement.createImageFromElement(element), gridCell, colIndex, rowIndex, emptyCols, yCutsRow, layout);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

