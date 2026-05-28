/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.Aggregation;
import com.labvantage.sapphire.report.jasper.DisplayConstants;
import com.labvantage.sapphire.report.jasper.DisplayProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportGrouping
implements Serializable,
Cloneable {
    private String groupingField;
    private String groupingFieldDesc;
    private String titleExpression;
    private DisplayProperties titleDisplayProperties;
    private Map aggregations;

    public String getGroupingField() {
        return this.groupingField;
    }

    public void setGroupingField(String groupingField) {
        this.groupingField = groupingField;
    }

    public String getTitleExpression() {
        return this.titleExpression;
    }

    public void setTitleExpression(String titleExpression) {
        this.titleExpression = titleExpression;
    }

    public DisplayProperties getTitleDisplayProperties() {
        if (this.titleDisplayProperties == null) {
            this.titleDisplayProperties = DisplayProperties.createDefault().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setFontSize(10);
                    displayProperties.setDecoration((byte)11);
                    displayProperties.setAlign(DisplayConstants.ALIGN_RIGHT);
                }
            });
        }
        return this.titleDisplayProperties;
    }

    public void setTitleDisplayProperties(DisplayProperties titleDisplayProperties) {
        this.titleDisplayProperties = titleDisplayProperties;
    }

    public void setAggregations(Map aggregations) {
        this.aggregations = aggregations;
    }

    public Map getAggregations() {
        if (this.aggregations == null) {
            this.aggregations = new LinkedHashMap();
        }
        return this.aggregations;
    }

    public List getAggregationsList() {
        return this.aggregations != null ? new ArrayList(this.aggregations.values()) : new ArrayList();
    }

    public boolean addAggregator(Aggregation aggregation) {
        if (aggregation == null) {
            return false;
        }
        if (aggregation.getAggregatee().equals(this.groupingField)) {
            return false;
        }
        this.getAggregations().put(aggregation.getHashKey(), aggregation);
        return true;
    }

    public void removeAggregator(String key) {
        this.getAggregations().remove(key);
    }

    public String getGroupingFieldDesc() {
        return this.groupingFieldDesc;
    }

    public void setGroupingFieldDesc(String groupingFieldDesc) {
        this.groupingFieldDesc = groupingFieldDesc;
    }

    public Object clone() throws CloneNotSupportedException {
        ReportGrouping reportGrouping = (ReportGrouping)super.clone();
        reportGrouping.setAggregations(new LinkedHashMap());
        if (this.aggregations != null) {
            for (Aggregation aggregation : this.aggregations.values()) {
                reportGrouping.addAggregator((Aggregation)aggregation.clone());
            }
        }
        return reportGrouping;
    }
}

