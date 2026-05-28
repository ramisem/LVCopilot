/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRCloneable
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.base.JRBaseObjectFactory
 *  net.sf.jasperreports.engine.component.BaseComponentContext
 *  net.sf.jasperreports.engine.component.ComponentContext
 *  net.sf.jasperreports.engine.component.ContextAwareComponent
 *  net.sf.jasperreports.engine.design.events.JRChangeEventsSupport
 *  net.sf.jasperreports.engine.design.events.JRPropertyChangeSupport
 *  net.sf.jasperreports.engine.type.EvaluationTimeEnum
 *  net.sf.jasperreports.engine.type.HorizontalAlignEnum
 *  net.sf.jasperreports.engine.type.HorizontalImageAlignEnum
 *  net.sf.jasperreports.engine.type.ScaleImageEnum
 *  net.sf.jasperreports.engine.type.VerticalAlignEnum
 *  net.sf.jasperreports.engine.type.VerticalImageAlignEnum
 *  net.sf.jasperreports.engine.util.JRCloneUtils
 */
package net.sf.jasperreports.components.html;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import net.sf.jasperreports.engine.JRCloneable;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.base.JRBaseObjectFactory;
import net.sf.jasperreports.engine.component.BaseComponentContext;
import net.sf.jasperreports.engine.component.ComponentContext;
import net.sf.jasperreports.engine.component.ContextAwareComponent;
import net.sf.jasperreports.engine.design.events.JRChangeEventsSupport;
import net.sf.jasperreports.engine.design.events.JRPropertyChangeSupport;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.util.JRCloneUtils;

public class HtmlComponent
implements ContextAwareComponent,
Serializable,
JRChangeEventsSupport,
JRCloneable {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_EVALUATION_TIME = "evaluationTime";
    public static final String PROPERTY_EVALUATION_GROUP = "evaluationGroup";
    public static final String PROPERTY_SCALE_TYPE = "scaleType";
    public static final String PROPERTY_HORIZONTAL_ALIGN = "horizontalAlign";
    public static final String PROPERTY_VERTICAL_ALIGN = "verticalAlign";
    public static final String PROPERTY_HTMLCONTENT_EXPRESSION = "htmlContentExpression";
    public static final String PROPERTY_CLIP_ON_OVERFLOW = "clipOnOverflow";
    private JRExpression htmlContentExpression;
    private ScaleImageEnum scaleType = ScaleImageEnum.RETAIN_SHAPE;
    private HorizontalImageAlignEnum horizontalImageAlign = HorizontalImageAlignEnum.LEFT;
    private VerticalImageAlignEnum verticalImageAlign = VerticalImageAlignEnum.MIDDLE;
    private EvaluationTimeEnum evaluationTime = EvaluationTimeEnum.NOW;
    private String evaluationGroup;
    private Boolean clipOnOverflow = Boolean.TRUE;
    private ComponentContext context;
    private transient JRPropertyChangeSupport eventSupport;
    private int PSEUDO_SERIAL_VERSION_UID = 61100;
    private HorizontalAlignEnum horizontalAlign;
    private VerticalAlignEnum verticalAlign;

    public HtmlComponent() {
    }

    public HtmlComponent(HtmlComponent component, JRBaseObjectFactory objectFactory) {
        this.scaleType = component.getScaleType();
        this.horizontalImageAlign = component.getHorizontalImageAlign();
        this.verticalImageAlign = component.getVerticalImageAlign();
        this.htmlContentExpression = objectFactory.getExpression(component.getHtmlContentExpression());
        this.context = new BaseComponentContext(component.getContext(), objectFactory);
        this.evaluationTime = component.getEvaluationTime();
        this.evaluationGroup = component.getEvaluationGroup();
        this.clipOnOverflow = component.getClipOnOverflow();
    }

    public void setContext(ComponentContext context) {
        this.context = context;
    }

    public ComponentContext getContext() {
        return this.context;
    }

    public JRExpression getHtmlContentExpression() {
        return this.htmlContentExpression;
    }

    public void setHtmlContentExpression(JRExpression htmlContentExpression) {
        JRExpression old = this.htmlContentExpression;
        this.htmlContentExpression = htmlContentExpression;
        this.getEventSupport().firePropertyChange(PROPERTY_HTMLCONTENT_EXPRESSION, (Object)old, (Object)this.htmlContentExpression);
    }

    public ScaleImageEnum getScaleType() {
        return this.scaleType;
    }

    public void setScaleType(ScaleImageEnum scaleType) {
        ScaleImageEnum old = this.scaleType;
        this.scaleType = scaleType;
        this.getEventSupport().firePropertyChange(PROPERTY_SCALE_TYPE, (Object)old, (Object)this.scaleType);
    }

    public HorizontalAlignEnum getHorizontalAlign() {
        return HorizontalAlignEnum.getHorizontalAlignEnum((HorizontalImageAlignEnum)this.getHorizontalImageAlign());
    }

    public void setHorizontalAlign(HorizontalAlignEnum horizontalAlign) {
        this.setHorizontalImageAlign(HorizontalAlignEnum.getHorizontalImageAlignEnum((HorizontalAlignEnum)horizontalAlign));
    }

    public VerticalAlignEnum getVerticalAlign() {
        return VerticalAlignEnum.getVerticalAlignEnum((VerticalImageAlignEnum)this.getVerticalImageAlign());
    }

    public void setVerticalAlign(VerticalAlignEnum verticalAlign) {
        this.setVerticalImageAlign(VerticalAlignEnum.getVerticalImageAlignEnum((VerticalAlignEnum)verticalAlign));
    }

    public HorizontalImageAlignEnum getHorizontalImageAlign() {
        return this.horizontalImageAlign;
    }

    public void setHorizontalImageAlign(HorizontalImageAlignEnum horizontalImageAlign) {
        HorizontalImageAlignEnum old = this.horizontalImageAlign;
        this.horizontalImageAlign = horizontalImageAlign;
        this.getEventSupport().firePropertyChange(PROPERTY_HORIZONTAL_ALIGN, (Object)old, (Object)this.horizontalImageAlign);
    }

    public VerticalImageAlignEnum getVerticalImageAlign() {
        return this.verticalImageAlign;
    }

    public void setVerticalImageAlign(VerticalImageAlignEnum verticalImageAlign) {
        VerticalImageAlignEnum old = this.verticalImageAlign;
        this.verticalImageAlign = verticalImageAlign;
        this.getEventSupport().firePropertyChange(PROPERTY_VERTICAL_ALIGN, (Object)old, (Object)this.verticalImageAlign);
    }

    public EvaluationTimeEnum getEvaluationTime() {
        return this.evaluationTime;
    }

    public void setEvaluationTime(EvaluationTimeEnum evaluationTime) {
        EvaluationTimeEnum old = this.evaluationTime;
        this.evaluationTime = evaluationTime;
        this.getEventSupport().firePropertyChange(PROPERTY_EVALUATION_TIME, (Object)old, (Object)this.evaluationTime);
    }

    public String getEvaluationGroup() {
        return this.evaluationGroup;
    }

    public void setEvaluationGroup(String evaluationGroup) {
        String old = this.evaluationGroup;
        this.evaluationGroup = evaluationGroup;
        this.getEventSupport().firePropertyChange(PROPERTY_EVALUATION_GROUP, (Object)old, (Object)this.evaluationGroup);
    }

    public Boolean getClipOnOverflow() {
        return this.clipOnOverflow;
    }

    public void setClipOnOverflow(Boolean clipOnOverflow) {
        Boolean old = this.clipOnOverflow;
        this.clipOnOverflow = clipOnOverflow;
        this.getEventSupport().firePropertyChange(PROPERTY_CLIP_ON_OVERFLOW, (Object)old, (Object)this.clipOnOverflow);
    }

    public Object clone() {
        HtmlComponent clone = null;
        try {
            clone = (HtmlComponent)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new JRRuntimeException((Throwable)e);
        }
        clone.htmlContentExpression = (JRExpression)JRCloneUtils.nullSafeClone((JRCloneable)this.htmlContentExpression);
        clone.eventSupport = null;
        return clone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JRPropertyChangeSupport getEventSupport() {
        HtmlComponent htmlComponent = this;
        synchronized (htmlComponent) {
            if (this.eventSupport == null) {
                this.eventSupport = new JRPropertyChangeSupport((Object)this);
            }
        }
        return this.eventSupport;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.PSEUDO_SERIAL_VERSION_UID < 60002) {
            this.horizontalImageAlign = HorizontalAlignEnum.getHorizontalImageAlignEnum((HorizontalAlignEnum)this.horizontalAlign);
            this.verticalImageAlign = VerticalAlignEnum.getVerticalImageAlignEnum((VerticalAlignEnum)this.verticalAlign);
            this.horizontalAlign = null;
            this.verticalAlign = null;
        }
    }
}

