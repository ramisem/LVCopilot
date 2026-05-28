/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRComponentElement
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRGenericElementType
 *  net.sf.jasperreports.engine.JRGenericPrintElement
 *  net.sf.jasperreports.engine.JRPrintElement
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.component.BaseFillComponent
 *  net.sf.jasperreports.engine.component.FillPrepareResult
 *  net.sf.jasperreports.engine.fill.JRFillCloneFactory
 *  net.sf.jasperreports.engine.fill.JRFillCloneable
 *  net.sf.jasperreports.engine.fill.JRTemplateGenericElement
 *  net.sf.jasperreports.engine.fill.JRTemplateGenericPrintElement
 *  net.sf.jasperreports.engine.type.EvaluationTimeEnum
 *  net.sf.jasperreports.engine.type.ScaleImageEnum
 */
package net.sf.jasperreports.components.html;

import java.awt.Dimension;
import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericElementType;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.component.BaseFillComponent;
import net.sf.jasperreports.engine.component.FillPrepareResult;
import net.sf.jasperreports.engine.fill.JRFillCloneFactory;
import net.sf.jasperreports.engine.fill.JRFillCloneable;
import net.sf.jasperreports.engine.fill.JRTemplateGenericElement;
import net.sf.jasperreports.engine.fill.JRTemplateGenericPrintElement;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.util.HtmlPrintElement;
import net.sf.jasperreports.engine.util.HtmlPrintElementUtils;

public class HtmlComponentFill
extends BaseFillComponent {
    public static final JRGenericElementType HTML_COMPONENT_PRINT_TYPE = new JRGenericElementType("http://jasperreports.sourceforge.net/jasperreports/html", "htmlelement");
    private final HtmlComponent htmlComponent;
    private String htmlContent;
    private JRTemplateGenericElement template;
    private JRTemplateGenericPrintElement printElement;
    private boolean hasOverflowed;

    public HtmlComponentFill(HtmlComponent htmlComponent) {
        this.htmlComponent = htmlComponent;
    }

    protected HtmlComponent getHtmlComponent() {
        return this.htmlComponent;
    }

    protected boolean isEvaluateNow() {
        return this.htmlComponent.getEvaluationTime() == EvaluationTimeEnum.NOW;
    }

    public void evaluate(byte evaluation) throws JRException {
        if (this.isEvaluateNow()) {
            this.hasOverflowed = false;
            this.evaluateHtmlComponent(evaluation);
        }
    }

    protected void evaluateHtmlComponent(byte evaluation) throws JRException {
        this.htmlContent = (String)this.fillContext.evaluate(this.htmlComponent.getHtmlContentExpression(), evaluation);
    }

    public JRPrintElement fill() {
        this.printElement.setY(this.fillContext.getElementPrintY());
        return this.printElement;
    }

    public FillPrepareResult prepare(int availableHeight) {
        int realWidth;
        FillPrepareResult result = null;
        JRComponentElement element = this.fillContext.getComponentElement();
        if (this.template == null) {
            this.template = new JRTemplateGenericElement(this.fillContext.getElementOrigin(), this.fillContext.getDefaultStyleProvider(), HTML_COMPONENT_PRINT_TYPE);
            this.template.setMode(this.htmlComponent.getContext().getComponentElement().getModeValue());
            this.template.setBackcolor(this.htmlComponent.getContext().getComponentElement().getBackcolor());
            this.template.setForecolor(this.htmlComponent.getContext().getComponentElement().getForecolor());
        }
        this.printElement = new JRTemplateGenericPrintElement(this.template, this.printElementOriginator);
        this.printElement.setX(element.getX());
        this.printElement.setWidth(element.getWidth());
        this.printElement.setHeight(element.getHeight());
        if (this.isEvaluateNow()) {
            this.copy((JRGenericPrintElement)this.printElement);
        } else {
            this.fillContext.registerDelayedEvaluation((JRPrintElement)this.printElement, this.htmlComponent.getEvaluationTime(), null);
        }
        Dimension realSize = this.computeSizeOfPrintElement(this.printElement);
        int realHeight = realSize.height;
        int imageWidth = realWidth = realSize.width;
        int imageHeight = realHeight;
        if (this.htmlComponent.getScaleType() == ScaleImageEnum.REAL_SIZE || this.htmlComponent.getScaleType() == ScaleImageEnum.REAL_HEIGHT) {
            if (realWidth > element.getWidth()) {
                double wRatio = (double)element.getWidth() / (double)realWidth;
                imageHeight = (int)(wRatio * (double)realHeight);
                imageWidth = element.getWidth();
            }
            int printElementHeight = Math.max(imageHeight, element.getHeight());
            if (imageHeight <= availableHeight) {
                result = FillPrepareResult.printStretch((int)imageHeight, (boolean)false);
            } else if (this.hasOverflowed) {
                result = FillPrepareResult.printStretch((int)availableHeight, (boolean)false);
                if (this.htmlComponent.getScaleType() == ScaleImageEnum.REAL_SIZE) {
                    this.printElement.setWidth(imageWidth);
                } else {
                    this.printElement.setWidth(element.getWidth());
                }
                this.printElement.setHeight(availableHeight);
                this.printElement.setParameterValue("hasOverflowed", (Object)Boolean.TRUE);
            } else {
                result = FillPrepareResult.noPrintOverflow((int)printElementHeight);
                this.hasOverflowed = true;
            }
        } else {
            result = FillPrepareResult.PRINT_NO_STRETCH;
        }
        return result;
    }

    private Dimension computeSizeOfPrintElement(JRTemplateGenericPrintElement printElement) {
        HtmlPrintElement htmlPrintElement = null;
        try {
            htmlPrintElement = HtmlPrintElementUtils.getHtmlPrintElement();
        }
        catch (JRException e) {
            throw new JRRuntimeException((Throwable)e);
        }
        return htmlPrintElement.getComputedSize((JRGenericPrintElement)printElement);
    }

    public JRFillCloneable createClone(JRFillCloneFactory factory) {
        throw new UnsupportedOperationException();
    }

    public void evaluateDelayedElement(JRPrintElement element, byte evaluation) throws JRException {
        this.evaluateHtmlComponent(evaluation);
        this.copy((JRGenericPrintElement)element);
    }

    protected void copy(JRGenericPrintElement printElement) {
        printElement.setParameterValue("htmlContent", (Object)this.htmlContent);
        printElement.setParameterValue("scaleType", (Object)this.htmlComponent.getScaleType().getName());
        printElement.setParameterValue("horizontalAlign", (Object)this.htmlComponent.getHorizontalImageAlign().getName());
        printElement.setParameterValue("verticalAlign", (Object)this.htmlComponent.getVerticalImageAlign().getName());
        printElement.setParameterValue("clipOnOverflow", (Object)this.htmlComponent.getClipOnOverflow());
    }
}

