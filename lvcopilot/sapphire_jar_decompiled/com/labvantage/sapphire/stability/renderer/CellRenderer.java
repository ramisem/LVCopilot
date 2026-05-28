/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface CellRenderer {
    public void init(ScheduleGrid var1, PropertyList var2);

    public String getCellColor(PlanItem var1);

    public String getTitle(PlanItem var1);

    public String[] getDetailLevels();

    public String getCellHTML(PlanItem var1, String var2);

    public String getTip(PlanItem var1, String var2);

    public boolean getShow(PlanItem var1) throws SapphireException;

    public boolean getShowTitle(PlanItem var1) throws SapphireException;

    public String getDetailLevel(PlanItem var1) throws SapphireException;
}

