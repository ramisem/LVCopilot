/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.AnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.CategoryTextAnnotationConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class CategoryAnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_CATEGORY_ANNOTATION_TYPE = CategoryAnnotationType.TEXT.getName();
    private final AnnotationConfiguration parent;
    private final CategoryAnnotationType categoryAnnotationType;
    private final CategoryTextAnnotationConfiguration categoryTextAnnotationConf;

    public CategoryAnnotationConfiguration(PropertyList categoryAnnotationProps, AnnotationConfiguration parent) {
        if (categoryAnnotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.categoryAnnotationType = CategoryAnnotationType.fromString(categoryAnnotationProps.getProperty("categoryannotationtype", DEFAULT_CATEGORY_ANNOTATION_TYPE));
        this.categoryTextAnnotationConf = this.categoryAnnotationType == CategoryAnnotationType.TEXT ? new CategoryTextAnnotationConfiguration(categoryAnnotationProps.getPropertyListNotNull("categorytextannotationprops"), this) : null;
    }

    public CategoryAnnotationConfiguration(CategoryAnnotationConfiguration copy, AnnotationConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.categoryAnnotationType = copy.categoryAnnotationType;
        this.categoryTextAnnotationConf = copy.categoryTextAnnotationConf != null ? new CategoryTextAnnotationConfiguration(copy.categoryTextAnnotationConf, this) : null;
    }

    public CategoryTextAnnotationConfiguration getCategoryTextAnnotationConfiguration() {
        if (this.categoryTextAnnotationConf == null) {
            throw new IllegalStateException("Category annotation type is: " + (Object)((Object)this.categoryAnnotationType));
        }
        return this.categoryTextAnnotationConf;
    }

    public CategoryAnnotationType getCategoryAnnotationType() {
        return this.categoryAnnotationType;
    }

    public AnnotationConfiguration getParent() {
        return this.parent;
    }

    public static enum CategoryAnnotationType {
        TEXT("Text");

        private final String name;

        private CategoryAnnotationType(String name) {
            this.name = name;
        }

        public static CategoryAnnotationType fromString(String name) {
            if (name != null) {
                for (CategoryAnnotationType type : CategoryAnnotationType.values()) {
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

