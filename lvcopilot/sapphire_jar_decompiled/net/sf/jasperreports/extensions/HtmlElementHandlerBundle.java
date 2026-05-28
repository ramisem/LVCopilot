/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.export.GenericElementHandler
 *  net.sf.jasperreports.engine.export.GenericElementHandlerBundle
 */
package net.sf.jasperreports.extensions;

import net.sf.jasperreports.engine.export.GenericElementHandler;
import net.sf.jasperreports.engine.export.GenericElementHandlerBundle;
import net.sf.jasperreports.engine.export.HtmlElementDocxHandler;
import net.sf.jasperreports.engine.export.HtmlElementGraphics2DHandler;
import net.sf.jasperreports.engine.export.HtmlElementOdsHandler;
import net.sf.jasperreports.engine.export.HtmlElementOdtHandler;
import net.sf.jasperreports.engine.export.HtmlElementPdfHandler;
import net.sf.jasperreports.engine.export.HtmlElementPptxHandler;
import net.sf.jasperreports.engine.export.HtmlElementRtfHandler;
import net.sf.jasperreports.engine.export.HtmlElementXhtmlHandler;
import net.sf.jasperreports.engine.export.HtmlElementXlsHandler;
import net.sf.jasperreports.engine.export.HtmlElementXlsxHandler;

public final class HtmlElementHandlerBundle
implements GenericElementHandlerBundle {
    public static final String NAMESPACE = "http://jasperreports.sourceforge.net/jasperreports/html";
    public static final String NAME = "htmlelement";
    private static final HtmlElementHandlerBundle INSTANCE = new HtmlElementHandlerBundle();

    public static HtmlElementHandlerBundle getInstance() {
        return INSTANCE;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public GenericElementHandler getHandler(String elementName, String exporterKey) {
        if (NAME.equals(elementName) && "net.sf.jasperreports.html".equals(exporterKey)) {
            return new HtmlElementXhtmlHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.pdf".equals(exporterKey)) {
            return new HtmlElementPdfHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.xls".equals(exporterKey)) {
            return new HtmlElementXlsHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.graphics2d".equals(exporterKey)) {
            return new HtmlElementGraphics2DHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.docx".equals(exporterKey)) {
            return new HtmlElementDocxHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.pptx".equals(exporterKey)) {
            return new HtmlElementPptxHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.xlsx".equals(exporterKey)) {
            return new HtmlElementXlsxHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.rtf".equals(exporterKey)) {
            return new HtmlElementRtfHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.odt".equals(exporterKey)) {
            return new HtmlElementOdtHandler();
        }
        if (NAME.equals(elementName) && "net.sf.jasperreports.ods".equals(exporterKey)) {
            return new HtmlElementOdsHandler();
        }
        return null;
    }
}

