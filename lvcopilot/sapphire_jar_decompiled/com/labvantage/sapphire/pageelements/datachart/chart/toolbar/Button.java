/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface Button {
    public PropertyList getProps(ChartBindingMap var1, PropertyList var2) throws SapphireException;

    public String getButtonId();

    public void setVisible(boolean var1);

    public boolean isVisible();
}

