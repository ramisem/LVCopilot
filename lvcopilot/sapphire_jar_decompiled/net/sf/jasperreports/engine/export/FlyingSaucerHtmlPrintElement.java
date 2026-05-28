/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRComponentElement
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.JRPrintImage
 *  net.sf.jasperreports.engine.Renderable
 *  net.sf.jasperreports.engine.base.JRBasePrintImage
 *  net.sf.jasperreports.engine.type.HorizontalImageAlignEnum
 *  net.sf.jasperreports.engine.type.ScaleImageEnum
 *  net.sf.jasperreports.engine.type.VerticalImageAlignEnum
 *  net.sf.jasperreports.engine.util.JRExpressionUtil
 *  org.w3c.tidy.Tidy
 */
package net.sf.jasperreports.engine.export;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.Renderable;
import net.sf.jasperreports.engine.base.JRBasePrintImage;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.JRExpressionUtil;
import net.sf.jasperreports.renderers.FlyingSaucerXhtmlToImageRenderer;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

public class FlyingSaucerHtmlPrintElement
implements HtmlPrintElement {
    static int previousYPosition = 0;

    @Override
    public JRPrintImage createImageFromElement(JRGenericPrintElement element) throws JRException {
        String htmlContent = (String)element.getParameterValue("htmlContent");
        String scaleType = (String)element.getParameterValue("scaleType");
        String horizontalAlignment = (String)element.getParameterValue("horizontalAlign");
        String verticalAlignment = (String)element.getParameterValue("verticalAlign");
        Boolean hasOverflowed = (Boolean)element.getParameterValue("hasOverflowed");
        Boolean clipOnOverflow = (Boolean)element.getParameterValue("clipOnOverflow");
        String addLine = (String)element.getParameterValue("addLine");
        JRBasePrintImage printImage = new JRBasePrintImage(element.getDefaultStyleProvider());
        printImage.setStyle(element.getStyle());
        printImage.setMode(element.getModeValue());
        printImage.setBackcolor(element.getBackcolor());
        printImage.setForecolor(element.getForecolor());
        printImage.setX(element.getX());
        if (element.getPrintElementId() == 1) {
            printImage.setY(element.getY());
        } else {
            printImage.setY(previousYPosition);
        }
        printImage.setWidth(element.getWidth());
        printImage.setScaleImage(ScaleImageEnum.getByName((String)scaleType));
        printImage.setHorizontalImageAlign(HorizontalImageAlignEnum.getByName((String)horizontalAlignment));
        printImage.setVerticalImageAlign(VerticalImageAlignEnum.getByName((String)verticalAlignment));
        FlyingSaucerXhtmlToImageRenderer renderer = new FlyingSaucerXhtmlToImageRenderer(this.getHtmlDocument(htmlContent, addLine), element.getWidth(), element.getHeight());
        if (printImage.getScaleImageValue() == ScaleImageEnum.REAL_HEIGHT || printImage.getScaleImageValue() == ScaleImageEnum.REAL_SIZE) {
            boolean canClip;
            boolean bl = canClip = hasOverflowed != null ? hasOverflowed : false;
            if (canClip) {
                printImage.setHeight(element.getHeight());
                if (clipOnOverflow.booleanValue()) {
                    printImage.setScaleImage(ScaleImageEnum.CLIP);
                }
            } else {
                printImage.setHeight(renderer.getComputedSize().height);
                previousYPosition = renderer.getComputedSize().height + element.getY();
            }
        } else {
            printImage.setHeight(element.getHeight());
        }
        printImage.setRenderable((Renderable)renderer);
        return printImage;
    }

    @Override
    public JRPrintImage createImageFromComponentElement(JRComponentElement componentElement) throws JRException {
        HtmlComponent html = (HtmlComponent)componentElement.getComponent();
        String htmlContent = "";
        if (html.getHtmlContentExpression() != null) {
            htmlContent = JRExpressionUtil.getExpressionText((JRExpression)html.getHtmlContentExpression());
        }
        JRBasePrintImage printImage = new JRBasePrintImage(componentElement.getDefaultStyleProvider());
        printImage.setStyle(componentElement.getStyle());
        printImage.setMode(componentElement.getModeValue());
        printImage.setBackcolor(componentElement.getBackcolor());
        printImage.setForecolor(componentElement.getForecolor());
        printImage.setX(componentElement.getX());
        printImage.setY(componentElement.getY());
        printImage.setWidth(componentElement.getWidth());
        printImage.setHeight(componentElement.getHeight());
        printImage.setScaleImage(html.getScaleType());
        printImage.setHorizontalImageAlign(html.getHorizontalImageAlign());
        printImage.setVerticalImageAlign(html.getVerticalImageAlign());
        FlyingSaucerXhtmlToImageRenderer renderer = new FlyingSaucerXhtmlToImageRenderer(this.getHtmlDocument(htmlContent), componentElement.getWidth(), componentElement.getHeight());
        printImage.setRenderable((Renderable)renderer);
        return printImage;
    }

    @Override
    public Dimension getComputedSize(JRGenericPrintElement element) {
        String htmlContent = (String)element.getParameterValue("htmlContent");
        FlyingSaucerXhtmlToImageRenderer renderer = new FlyingSaucerXhtmlToImageRenderer(this.getHtmlDocument(htmlContent), element.getWidth(), element.getHeight());
        return renderer.getComputedSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Document getHtmlDocument(String htmlContent, String addLine) {
        StringBuffer buf = new StringBuffer();
        buf.append("<html>");
        buf.append("<head>");
        buf.append("<style language='text/css'>");
        String msg = "";
        try (InputStream inputStream2 = this.getClass().getResourceAsStream("style.css");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream2);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);){
            String line = bufferedReader.readLine();
            while (line != null) {
                buf.append(line).append("\n");
                line = bufferedReader.readLine();
            }
        }
        catch (Exception inputStream2) {
            // empty catch block
        }
        buf.append("</style>");
        buf.append("</head>");
        buf.append("<body>");
        buf.append(htmlContent);
        if (addLine != null && "Y".equalsIgnoreCase(addLine)) {
            buf.append("<hr>");
        }
        buf.append("</body>");
        buf.append("</html>");
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        return tidy.parseDOM((InputStream)new ByteArrayInputStream(buf.toString().getBytes()), null);
    }

    private Document getHtmlDocument(String htmlContent) {
        return this.getHtmlDocument(htmlContent, null);
    }
}

