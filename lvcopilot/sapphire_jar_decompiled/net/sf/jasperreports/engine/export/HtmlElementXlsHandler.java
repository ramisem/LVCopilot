/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementXlsHandler
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.JRGridLayout
 *  net.sf.jasperreports.engine.export.JRXlsExporter
 *  net.sf.jasperreports.engine.export.JRXlsExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementXlsHandler;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRGridLayout;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementXlsHandler
implements GenericElementXlsHandler {
    public void exportElement(JRXlsExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell, int colIndex, int rowIndex, int emptyCols, int yCutsRow, JRGridLayout layout) {
        try {
            JRXlsExporter exporter = (JRXlsExporter)exporterContext.getExporterRef();
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

