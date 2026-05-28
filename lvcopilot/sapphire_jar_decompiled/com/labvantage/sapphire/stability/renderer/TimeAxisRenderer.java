/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.renderer.AxisRenderer;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class TimeAxisRenderer
implements AxisRenderer {
    @Override
    public void init(PropertyList propertyList) {
    }

    @Override
    public String getTitleHTML(BaseAxis axis, int row) {
        return SafeHTML.encodeForHTML(axis.items.getValue(row, axis.labelColumn, "No Label"));
    }

    @Override
    public String getTitleTip(BaseAxis axis, int row) {
        return null;
    }
}

