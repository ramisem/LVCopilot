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
 *  net.sf.jasperreports.renderers.AwtComponentRenderer
 */
package net.sf.jasperreports.engine.export;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
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
import net.sf.jasperreports.renderers.AwtComponentRenderer;

public class DefaultHtmlPrintElement
implements HtmlPrintElement {
    @Override
    public JRPrintImage createImageFromElement(JRGenericPrintElement element) {
        String htmlContent = (String)element.getParameterValue("htmlContent");
        String scaleType = (String)element.getParameterValue("scaleType");
        String horizontalAlignment = (String)element.getParameterValue("horizontalAlign");
        String verticalAlignment = (String)element.getParameterValue("verticalAlign");
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKitForContentType("text/html", new SynchronousImageLoaderKit());
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);
        editorPane.setBorder(null);
        editorPane.setSize(editorPane.getPreferredSize());
        JRBasePrintImage printImage = new JRBasePrintImage(element.getDefaultStyleProvider());
        printImage.setX(element.getX());
        printImage.setY(element.getY());
        printImage.setWidth(element.getWidth());
        printImage.setHeight(element.getHeight());
        printImage.setScaleImage(ScaleImageEnum.getByName((String)scaleType));
        printImage.setHorizontalImageAlign(HorizontalImageAlignEnum.getByName((String)horizontalAlignment));
        printImage.setVerticalImageAlign(VerticalImageAlignEnum.getByName((String)verticalAlignment));
        printImage.setStyle(element.getStyle());
        printImage.setMode(element.getModeValue());
        printImage.setBackcolor(element.getBackcolor());
        printImage.setForecolor(element.getForecolor());
        printImage.setRenderable((Renderable)new AwtComponentRenderer((Component)editorPane));
        return printImage;
    }

    @Override
    public JRPrintImage createImageFromComponentElement(JRComponentElement componentElement) throws JRException {
        HtmlComponent html = (HtmlComponent)componentElement.getComponent();
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKitForContentType("text/html", new SynchronousImageLoaderKit());
        editorPane.setContentType("text/html");
        String htmlContent = "";
        if (html.getHtmlContentExpression() != null) {
            htmlContent = JRExpressionUtil.getExpressionText((JRExpression)html.getHtmlContentExpression());
        }
        editorPane.setText(htmlContent);
        editorPane.setBorder(null);
        editorPane.setSize(editorPane.getPreferredSize());
        JRBasePrintImage printImage = new JRBasePrintImage(componentElement.getDefaultStyleProvider());
        printImage.setX(componentElement.getX());
        printImage.setY(componentElement.getY());
        printImage.setWidth(componentElement.getWidth());
        printImage.setHeight(componentElement.getHeight());
        printImage.setScaleImage(html.getScaleType());
        printImage.setHorizontalImageAlign(html.getHorizontalImageAlign());
        printImage.setVerticalImageAlign(html.getVerticalImageAlign());
        printImage.setStyle(componentElement.getStyle());
        printImage.setMode(componentElement.getModeValue());
        printImage.setBackcolor(componentElement.getBackcolor());
        printImage.setForecolor(componentElement.getForecolor());
        printImage.setRenderable((Renderable)new AwtComponentRenderer((Component)editorPane));
        return printImage;
    }

    @Override
    public Dimension getComputedSize(JRGenericPrintElement element) {
        String htmlContent = (String)element.getParameterValue("htmlContent");
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKitForContentType("text/html", new SynchronousImageLoaderKit());
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);
        editorPane.setBorder(null);
        return editorPane.getPreferredSize();
    }

    public class SynchronousImageLoaderKit
    extends HTMLEditorKit {
        private static final long serialVersionUID = 1L;

        @Override
        public ViewFactory getViewFactory() {
            return new HTMLEditorKit.HTMLFactory(){

                @Override
                public View create(Element elem) {
                    View view = super.create(elem);
                    if (view instanceof ImageView) {
                        ((ImageView)view).setLoadsSynchronously(true);
                    }
                    return view;
                }
            };
        }
    }
}

