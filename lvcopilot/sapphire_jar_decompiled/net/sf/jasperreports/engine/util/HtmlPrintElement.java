/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRComponentElement
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.JRPrintImage
 */
package net.sf.jasperreports.engine.util;

import java.awt.Dimension;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;

public interface HtmlPrintElement {
    public static final String PARAMETER_HTML_CONTENT = "htmlContent";
    public static final String PARAMETER_SCALE_TYPE = "scaleType";
    public static final String PARAMETER_HORIZONTAL_ALIGN = "horizontalAlign";
    public static final String PARAMETER_VERTICAL_ALIGN = "verticalAlign";
    public static final String PARAMETER_CLIP_ON_OVERFLOW = "clipOnOverflow";
    public static final String PARAMETER_ADD_LINE = "addLine";
    public static final String BUILTIN_PARAMETER_HAS_OVERFLOWED = "hasOverflowed";

    public JRPrintImage createImageFromElement(JRGenericPrintElement var1) throws JRException;

    public JRPrintImage createImageFromComponentElement(JRComponentElement var1) throws JRException;

    public Dimension getComputedSize(JRGenericPrintElement var1);
}

