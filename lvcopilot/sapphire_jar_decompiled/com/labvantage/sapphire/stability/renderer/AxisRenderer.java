/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface AxisRenderer {
    public void init(PropertyList var1);

    public String getTitleHTML(BaseAxis var1, int var2) throws SapphireException;

    public String getTitleTip(BaseAxis var1, int var2);
}

