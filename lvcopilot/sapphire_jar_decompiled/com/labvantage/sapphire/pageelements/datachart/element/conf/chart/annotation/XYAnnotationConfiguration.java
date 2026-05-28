/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.AnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYTextAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYTitleAnnotationConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class XYAnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_XY_ANNOTATION_TYPE = XYAnnotationType.TEXT.getName();
    private final AnnotationConfiguration parent;
    private final XYAnnotationType xyAnnotationType;
    private final XYTextAnnotationConfiguration xyTextAnnotationConf;
    private final XYTitleAnnotationConfiguration xyTitleAnnotationConf;

    public XYAnnotationConfiguration(PropertyList xyAnnotationProps, AnnotationConfiguration parent) {
        if (xyAnnotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.xyAnnotationType = XYAnnotationType.fromString(xyAnnotationProps.getProperty("xyannotationtype", DEFAULT_XY_ANNOTATION_TYPE));
        this.xyTextAnnotationConf = this.xyAnnotationType == XYAnnotationType.TEXT ? new XYTextAnnotationConfiguration(xyAnnotationProps.getPropertyListNotNull("xytextannotationprops"), this) : null;
        this.xyTitleAnnotationConf = this.xyAnnotationType == XYAnnotationType.TITLE ? new XYTitleAnnotationConfiguration(xyAnnotationProps.getPropertyListNotNull("xytitleannotationprops"), this) : null;
    }

    public XYAnnotationConfiguration(XYAnnotationConfiguration copy, AnnotationConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.xyAnnotationType = copy.xyAnnotationType;
        this.xyTextAnnotationConf = copy.xyTextAnnotationConf != null ? new XYTextAnnotationConfiguration(copy.xyTextAnnotationConf, this) : null;
        this.xyTitleAnnotationConf = copy.xyTitleAnnotationConf != null ? new XYTitleAnnotationConfiguration(copy.xyTitleAnnotationConf, this) : null;
    }

    public XYTextAnnotationConfiguration getXYTextAnnotationConfiguration() {
        if (this.xyTextAnnotationConf == null) {
            throw new IllegalStateException("XY annotation type is: " + (Object)((Object)this.xyAnnotationType));
        }
        return this.xyTextAnnotationConf;
    }

    public XYTitleAnnotationConfiguration getXYTitleAnnotationConfiguration() {
        if (this.xyTitleAnnotationConf == null) {
            throw new IllegalStateException("XY annotation type is: " + (Object)((Object)this.xyAnnotationType));
        }
        return this.xyTitleAnnotationConf;
    }

    public XYAnnotationType getXYAnnotationType() {
        return this.xyAnnotationType;
    }

    public AnnotationConfiguration getParent() {
        return this.parent;
    }

    public static enum XYAnnotationType {
        TEXT("Text"),
        TITLE("Title");

        private final String name;

        private XYAnnotationType(String name) {
            this.name = name;
        }

        public static XYAnnotationType fromString(String name) {
            if (name != null) {
                for (XYAnnotationType type : XYAnnotationType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

