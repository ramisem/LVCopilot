/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.JRGridLayout
 *  net.sf.jasperreports.engine.export.oasis.GenericElementOdsHandler
 *  net.sf.jasperreports.engine.export.oasis.JROdsExporter
 *  net.sf.jasperreports.engine.export.oasis.JROdsExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRGridLayout;
import net.sf.jasperreports.engine.export.oasis.GenericElementOdsHandler;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementOdsHandler
implements GenericElementOdsHandler {
    public void exportElement(JROdsExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell, int colIndex, int rowIndex, int emptyCols, int yCutsRow, JRGridLayout layout) {
        try {
            JROdsExporter exporter = (JROdsExporter)exporterContext.getExporterRef();
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(htmlPrintElement.createImageFromElement(element), gridCell, colIndex, rowIndex, emptyCols, yCutsRow, layout);
        }
        catch (JRException e) {
            throw new JRRuntimeException((Throwable)e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

