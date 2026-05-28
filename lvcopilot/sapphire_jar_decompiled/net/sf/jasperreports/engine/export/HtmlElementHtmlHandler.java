/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.export.GenericElementHtmlHandler
 *  net.sf.jasperreports.engine.export.JRHtmlExporterContext
 *  net.sf.jasperreports.engine.type.ModeEnum
 *  net.sf.jasperreports.engine.util.JRColorUtil
 */
package net.sf.jasperreports.engine.export;

import java.awt.Color;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementHtmlHandler;
import net.sf.jasperreports.engine.export.JRHtmlExporterContext;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;

public class HtmlElementHtmlHandler
implements GenericElementHtmlHandler {
    public String getHtmlFragment(JRHtmlExporterContext exporterContext, JRGenericPrintElement element) {
        StringBuilder script = new StringBuilder(128);
        String htmlContent = (String)element.getParameterValue("htmlContent");
        script.append("<div style='width:" + (element.getWidth() - 0) + "px;height:" + (element.getHeight() - 0) + "px;");
        if (element.getModeValue() == ModeEnum.OPAQUE) {
            script.append("background-color: #");
            script.append(JRColorUtil.getColorHexa((Color)element.getBackcolor()));
            script.append("; ");
        }
        script.append("overflow:hidden;'>");
        script.append(htmlContent);
        script.append("</div>");
        return script.toString();
    }

    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }
}

