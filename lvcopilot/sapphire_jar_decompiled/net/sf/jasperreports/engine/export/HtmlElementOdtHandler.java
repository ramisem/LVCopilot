/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.JRExporterGridCell
 *  net.sf.jasperreports.engine.export.oasis.GenericElementOdtHandler
 *  net.sf.jasperreports.engine.export.oasis.JROdtExporter
 *  net.sf.jasperreports.engine.export.oasis.JROdtExporterContext
 */
package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.oasis.GenericElementOdtHandler;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporterContext;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementOdtHandler
implements GenericElementOdtHandler {
    public void exportElement(JROdtExporterContext exporterContext, JRGenericPrintElement element, JRExporterGridCell gridCell) {
        try {
            JROdtExporter exporter = (JROdtExporter)exporterContext.getExporterRef();
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            exporter.exportImage(exporterContext.getTableBuilder(), htmlPrintElement.createImageFromElement(element), gridCell);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

