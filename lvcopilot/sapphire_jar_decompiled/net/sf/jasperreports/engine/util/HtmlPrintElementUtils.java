/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.DefaultJasperReportsContext
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRPropertiesUtil
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.engine.util.JRSingletonCache
 */
package net.sf.jasperreports.engine.util;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.util.DefaultHtmlPrintElementFactory;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementFactory;
import net.sf.jasperreports.engine.util.JRSingletonCache;

public final class HtmlPrintElementUtils {
    public static final String PROPERTY_HTML_PRINTELEMENT_FACTORY = "net.sf.jasperreports.html.printelement.factory";
    private static final JRSingletonCache<HtmlPrintElementFactory> cache = new JRSingletonCache(HtmlPrintElementFactory.class);

    public static HtmlPrintElementFactory getHtmlPrintElementFactory() throws JRException {
        String factoryClassName = JRPropertiesUtil.getInstance((JasperReportsContext)DefaultJasperReportsContext.getInstance()).getProperty(PROPERTY_HTML_PRINTELEMENT_FACTORY);
        if (factoryClassName == null) {
            factoryClassName = DefaultHtmlPrintElementFactory.class.getName();
        }
        return (HtmlPrintElementFactory)cache.getCachedInstance(factoryClassName);
    }

    public static HtmlPrintElement getHtmlPrintElement() throws JRException {
        HtmlPrintElementFactory printElementFactory = HtmlPrintElementUtils.getHtmlPrintElementFactory();
        return printElementFactory.getHtmlPrintElement();
    }

    private HtmlPrintElementUtils() {
    }
}

