/*
 * Decompiled with CFR 0.152.
 */
package net.sf.jasperreports.engine.util;

import net.sf.jasperreports.engine.export.DefaultHtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementFactory;

public class DefaultHtmlPrintElementFactory
implements HtmlPrintElementFactory {
    @Override
    public HtmlPrintElement getHtmlPrintElement() {
        return new DefaultHtmlPrintElement();
    }
}

