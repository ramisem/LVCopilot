/*
 * Decompiled with CFR 0.152.
 */
package net.sf.jasperreports.engine.util;

import net.sf.jasperreports.engine.export.FlyingSaucerHtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementFactory;

public class FlyingSaucerHtmlPrintElementFactory
implements HtmlPrintElementFactory {
    @Override
    public HtmlPrintElement getHtmlPrintElement() {
        return new FlyingSaucerHtmlPrintElement();
    }
}

