/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRComponentElement
 *  net.sf.jasperreports.engine.JRPrintElement
 *  net.sf.jasperreports.engine.component.ComponentDesignConverter
 *  net.sf.jasperreports.engine.convert.ReportConverter
 */
package net.sf.jasperreports.components.html;

import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.component.ComponentDesignConverter;
import net.sf.jasperreports.engine.convert.ReportConverter;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlComponentDesignConverter
implements ComponentDesignConverter {
    public JRPrintElement convert(ReportConverter reportConverter, JRComponentElement element) {
        if (element.getComponent() == null) {
            return null;
        }
        try {
            HtmlPrintElement htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
            return htmlPrintElement.createImageFromComponentElement(element);
        }
        catch (Exception e) {
            return null;
        }
    }
}

