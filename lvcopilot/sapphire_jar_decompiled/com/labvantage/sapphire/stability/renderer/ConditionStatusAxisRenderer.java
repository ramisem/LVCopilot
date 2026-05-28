/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.renderer.ConditionAxisRenderer;
import sapphire.SapphireException;

public class ConditionStatusAxisRenderer
extends ConditionAxisRenderer {
    @Override
    public String getTitleHTML(BaseAxis axis, int row) throws SapphireException {
        StringBuffer output = new StringBuffer();
        output.append(this.getStatusLabel(axis, row));
        output.append(this.getDetails(axis, row));
        return output.toString();
    }
}

