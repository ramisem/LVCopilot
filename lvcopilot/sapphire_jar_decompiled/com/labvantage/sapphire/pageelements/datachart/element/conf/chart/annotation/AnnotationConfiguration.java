/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.CategoryAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYAnnotationConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class AnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_ANNOTATION_ID = "";
    private static final String DEFAULT_ANNOTATION_TYPE = AnnotationType.XY.getName();
    private final AnnotationType annotationType;
    private final String annotationId;
    private final XYAnnotationConfiguration xyAnnotationConf;
    private final CategoryAnnotationConfiguration categoryAnnotationConf;

    public AnnotationConfiguration(PropertyList annotationProps) {
        if (annotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.annotationId = annotationProps.getProperty("annotationid", DEFAULT_ANNOTATION_ID);
        this.annotationType = AnnotationType.fromString(annotationProps.getProperty("annotationtype", DEFAULT_ANNOTATION_TYPE));
        this.xyAnnotationConf = this.annotationType == AnnotationType.XY ? new XYAnnotationConfiguration(annotationProps.getPropertyListNotNull("xyannotationprops"), this) : null;
        this.categoryAnnotationConf = this.annotationType == AnnotationType.CATEGORY ? new CategoryAnnotationConfiguration(annotationProps.getPropertyListNotNull("categoryannotationprops"), this) : null;
    }

    public AnnotationConfiguration(AnnotationConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.annotationType = copy.annotationType;
        this.annotationId = copy.annotationId;
        this.xyAnnotationConf = copy.xyAnnotationConf != null ? new XYAnnotationConfiguration(copy.xyAnnotationConf, this) : null;
        this.categoryAnnotationConf = copy.categoryAnnotationConf != null ? new CategoryAnnotationConfiguration(copy.categoryAnnotationConf, this) : null;
    }

    public XYAnnotationConfiguration getXYAnnotationConfiguration() {
        if (this.xyAnnotationConf == null) {
            throw new IllegalStateException("Annotation type is: " + (Object)((Object)this.annotationType));
        }
        return this.xyAnnotationConf;
    }

    public CategoryAnnotationConfiguration getCategoryAnnotationConfiguration() {
        if (this.categoryAnnotationConf == null) {
            throw new IllegalStateException("Annotation type is: " + (Object)((Object)this.annotationType));
        }
        return this.categoryAnnotationConf;
    }

    public String getAnnotationId() {
        return this.annotationId;
    }

    public AnnotationType getAnnotationType() {
        return this.annotationType;
    }

    public static enum AnnotationType {
        XY("XY"),
        CATEGORY("Category");

        private final String name;

        private AnnotationType(String name) {
            this.name = name;
        }

        public static AnnotationType fromString(String name) {
            if (name != null) {
                for (AnnotationType type : AnnotationType.values()) {
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

