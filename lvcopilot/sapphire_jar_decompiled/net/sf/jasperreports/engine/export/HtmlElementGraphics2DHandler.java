/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementGraphics2DHandler
 *  net.sf.jasperreports.engine.export.JRGraphics2DExporter
 *  net.sf.jasperreports.engine.export.JRGraphics2DExporterContext
 *  net.sf.jasperreports.engine.export.draw.ImageDrawer
 *  net.sf.jasperreports.engine.export.draw.Offset
 */
package net.sf.jasperreports.engine.export;

import java.awt.Graphics2D;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementGraphics2DHandler;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterContext;
import net.sf.jasperreports.engine.export.draw.ImageDrawer;
import net.sf.jasperreports.engine.export.draw.Offset;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlElementGraphics2DHandler
implements GenericElementGraphics2DHandler {
    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }

    public void exportElement(JRGraphics2DExporterContext exporterContext, JRGenericPrintElement element, Graphics2D grx, Offset offset) {
        try {
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            JRGraphics2DExporter exporter = (JRGraphics2DExporter)exporterContext.getExporterRef();
            ImageDrawer imageDrawer = exporter.getFrameDrawer().getDrawVisitor().getImageDrawer();
            imageDrawer.draw(grx, htmlPrintElement.createImageFromElement(element), offset.getX(), offset.getY());
        }
        catch (JRException e) {
            throw new RuntimeException(e);
        }
    }
}

