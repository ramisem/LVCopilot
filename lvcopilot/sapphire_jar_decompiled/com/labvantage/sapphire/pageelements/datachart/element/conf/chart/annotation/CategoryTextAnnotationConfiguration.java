/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.CategoryAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.CategoryAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BigDecimalExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAnchor;
import sapphire.xml.PropertyList;

public final class CategoryTextAnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_CATEGORY = "";
    private static final String DEFAULT_VALUE = "1";
    private static final String DEFAULT_TEXT = "";
    private static final String DEFAULT_CATEGORY_ANCHOR = CategoryAnchorType.fromCategoryAnchor(CategoryAnchor.MIDDLE).getName();
    private final CategoryAnnotationConfiguration parent;
    private final StringExpression text;
    private final StringExpression category;
    private final BigDecimalExpression value;
    private final CategoryAnchor categoryAnchor;

    public CategoryTextAnnotationConfiguration(PropertyList categoryTextAnnotationProps, CategoryAnnotationConfiguration parent) {
        if (categoryTextAnnotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.text = new StringExpression(categoryTextAnnotationProps.getProperty("text", ""));
        this.category = new StringExpression(categoryTextAnnotationProps.getProperty("category", ""));
        this.value = new BigDecimalExpression(categoryTextAnnotationProps.getProperty("value", DEFAULT_VALUE));
        this.categoryAnchor = CategoryAnchorType.fromString(categoryTextAnnotationProps.getProperty("categoryanchor", DEFAULT_CATEGORY_ANCHOR)).getCategoryAnchor();
    }

    public CategoryTextAnnotationConfiguration(CategoryTextAnnotationConfiguration copy, CategoryAnnotationConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.text = new StringExpression(copy.text);
        this.category = new StringExpression(copy.category);
        this.value = new BigDecimalExpression(copy.value);
        this.categoryAnchor = copy.categoryAnchor;
    }

    public CategoryAnchor getCategoryAnchor() {
        return this.categoryAnchor;
    }

    public StringExpression getText() {
        return this.text;
    }

    public StringExpression getCategory() {
        return this.category;
    }

    public BigDecimalExpression getValue() {
        return this.value;
    }

    public CategoryAnnotationConfiguration getParent() {
        return this.parent;
    }

    public static enum CategoryAnchorType {
        END("End", CategoryAnchor.END),
        MIDDLE("Middle", CategoryAnchor.MIDDLE),
        START("Start", CategoryAnchor.START);

        private final String name;
        private final CategoryAnchor categoryAnchor;

        private CategoryAnchorType(String name, CategoryAnchor categoryAnchor) {
            this.name = name;
            this.categoryAnchor = categoryAnchor;
        }

        public static CategoryAnchorType fromString(String name) {
            if (name != null) {
                for (CategoryAnchorType type : CategoryAnchorType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public static CategoryAnchorType fromCategoryAnchor(CategoryAnchor categoryAnchor) {
            if (categoryAnchor != null) {
                for (CategoryAnchorType type : CategoryAnchorType.values()) {
                    if (categoryAnchor != type.categoryAnchor) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown category anchor: " + categoryAnchor);
        }

        public String getName() {
            return this.name;
        }

        public CategoryAnchor getCategoryAnchor() {
            return this.categoryAnchor;
        }
    }
}

