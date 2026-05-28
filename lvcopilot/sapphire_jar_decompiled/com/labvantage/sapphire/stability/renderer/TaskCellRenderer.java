/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.renderer.CellRenderer;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class TaskCellRenderer
implements CellRenderer {
    @Override
    public void init(ScheduleGrid grid, PropertyList propertylist) {
    }

    @Override
    public String getCellColor(PlanItem planItem) {
        String color = planItem.getInstanceGridTask().getColor();
        if (planItem.readonly) {
            color = "buttonface";
        }
        return color;
    }

    @Override
    public String getTitle(PlanItem planItem) {
        return planItem.getInstanceGridTask().getTitle();
    }

    @Override
    public String[] getDetailLevels() {
        return new String[]{"None"};
    }

    @Override
    public String getCellHTML(PlanItem planItem, String detailLevel) {
        StringBuffer returnValue = new StringBuffer();
        if (planItem.status.equals("D")) {
            returnValue.append("<img src='WEB-OPAL/pagetypes/stability/images/disabled.gif'>");
        }
        returnValue.append(planItem.getInstanceGridTask().getSummaryHTML(planItem.getCollapsedPropertyList(), detailLevel));
        return returnValue.toString();
    }

    @Override
    public String getTip(PlanItem planItem, String detailLevel) {
        return planItem.getInstanceGridTask().getSummaryText(planItem.getCollapsedPropertyList(), detailLevel);
    }

    @Override
    public boolean getShow(PlanItem planItem) throws SapphireException {
        return planItem.grid.taskTypes.getShow(planItem.propertyTreeid);
    }

    @Override
    public boolean getShowTitle(PlanItem planItem) throws SapphireException {
        return planItem.grid.taskTypes.getShowTitle(planItem.propertyTreeid);
    }

    @Override
    public String getDetailLevel(PlanItem planItem) throws SapphireException {
        return planItem.grid.taskTypes.getDetailLevel(planItem.propertyTreeid);
    }
}

