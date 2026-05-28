/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.LegendItem
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import java.text.AttributedString;
import org.jfree.chart.LegendItem;

public class MutableLegendItem
extends LegendItem {
    private String label = null;

    public MutableLegendItem(String label, LegendItem legendItem) {
        super(label, legendItem.getDescription(), legendItem.getToolTipText(), legendItem.getURLText(), legendItem.isShapeVisible(), legendItem.getShape(), legendItem.isShapeFilled(), legendItem.getFillPaint(), legendItem.isShapeOutlineVisible(), legendItem.getOutlinePaint(), legendItem.getOutlineStroke(), legendItem.isLineVisible(), legendItem.getLine(), legendItem.getLineStroke(), legendItem.getLinePaint());
    }

    public MutableLegendItem(AttributedString label, LegendItem legendItem) {
        super(label, legendItem.getDescription(), legendItem.getToolTipText(), legendItem.getURLText(), legendItem.isShapeVisible(), legendItem.getShape(), legendItem.isShapeFilled(), legendItem.getFillPaint(), legendItem.isShapeOutlineVisible(), legendItem.getOutlinePaint(), legendItem.getOutlineStroke(), legendItem.isLineVisible(), legendItem.getLine(), legendItem.getLineStroke(), legendItem.getLinePaint());
    }

    public String getLabel() {
        String returnLabel = this.label;
        if (returnLabel == null) {
            returnLabel = super.getLabel();
        }
        return returnLabel;
    }

    public void setLabel(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Label is null");
        }
        this.label = label;
    }
}

