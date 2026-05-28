/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.DisplayConstants;
import com.labvantage.sapphire.report.jasper.DisplayProperties;
import java.io.Serializable;

public class Aggregation
implements Serializable,
Cloneable {
    private byte operator;
    private String aggregatee;
    private String aggregateeDesc;
    private DisplayProperties displayProperties;

    public byte getOperator() {
        return this.operator;
    }

    public void setOperator(byte operator) {
        this.operator = operator;
    }

    public String getAggregatee() {
        return this.aggregatee;
    }

    public void setAggregatee(String aggregatee) {
        this.aggregatee = aggregatee;
    }

    public String getAggregateeDesc() {
        return this.aggregateeDesc;
    }

    public void setAggregateeDesc(String aggregateeDesc) {
        this.aggregateeDesc = aggregateeDesc;
    }

    public DisplayProperties getDisplayProperties() {
        if (this.displayProperties == null) {
            this.displayProperties = DisplayProperties.createDefault().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setFontSize(10);
                    displayProperties.setAlign(DisplayConstants.ALIGN_RIGHT);
                }
            });
        }
        return this.displayProperties;
    }

    public void setDisplayProperties(DisplayProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    public String getHashKey() {
        return new String(this.operator + ":" + this.aggregatee);
    }

    protected Object clone() throws CloneNotSupportedException {
        Aggregation aggregation = (Aggregation)super.clone();
        aggregation.setDisplayProperties((DisplayProperties)this.displayProperties.clone());
        return aggregation;
    }
}

