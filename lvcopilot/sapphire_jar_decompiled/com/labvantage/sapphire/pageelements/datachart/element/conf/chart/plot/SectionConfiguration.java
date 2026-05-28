/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PiePlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.math.BigDecimal;
import sapphire.xml.PropertyList;

public class SectionConfiguration
implements Serializable {
    private final StringExpression sectionId;
    private final PaintConfiguration sectionPaintConf;
    private final PaintConfiguration sectionOutLinePaintConf;
    private final PiePlotConfiguration parent;
    private final Double sectionExplodePercent;

    public SectionConfiguration(PropertyList sectionProps, PiePlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (sectionProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.sectionId = new StringExpression(sectionProps.getProperty("sectionid", ""));
        this.sectionPaintConf = new PaintConfiguration(sectionProps.getPropertyListNotNull("paintprops"));
        this.sectionOutLinePaintConf = new PaintConfiguration(sectionProps.getPropertyListNotNull("outlinepaintprops"));
        String sectionExplodePercStr = sectionProps.getProperty("explodepercent", "");
        Double tmpSectionExplode = null;
        if (!sectionExplodePercStr.isEmpty()) {
            try {
                tmpSectionExplode = new BigDecimal(sectionExplodePercStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Pie Plot SectionConfiguration, section: " + this.sectionId + " not valid explode percent:" + sectionExplodePercStr);
            }
            if (tmpSectionExplode < 0.0 || tmpSectionExplode > 1.0) {
                throw new IllegalArgumentException("Pie Plot SectionConfiguration, section: " + this.sectionId + " not valid explode percent:" + sectionExplodePercStr);
            }
        }
        this.sectionExplodePercent = tmpSectionExplode;
        this.parent = parent;
    }

    protected SectionConfiguration(SectionConfiguration copy, PiePlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.parent = parent;
        this.sectionId = new StringExpression(copy.getSectionId());
        this.sectionPaintConf = new PaintConfiguration(copy.sectionPaintConf);
        this.sectionOutLinePaintConf = new PaintConfiguration(copy.sectionOutLinePaintConf);
        this.sectionExplodePercent = copy.sectionExplodePercent;
    }

    public StringExpression getSectionId() {
        return this.sectionId;
    }

    public PaintConfiguration getSectionPaintConfiguration() {
        return this.sectionPaintConf;
    }

    public PaintConfiguration getSectionOutLinePaintConfiguration() {
        return this.sectionOutLinePaintConf;
    }

    public Double getSectionExplodePercent() {
        return this.sectionExplodePercent;
    }

    public PiePlotConfiguration getParent() {
        return this.parent;
    }
}

