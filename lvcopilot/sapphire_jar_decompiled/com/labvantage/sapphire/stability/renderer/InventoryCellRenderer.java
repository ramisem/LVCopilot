/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.renderer.CellRenderer;
import sapphire.xml.PropertyList;

public class InventoryCellRenderer
implements CellRenderer {
    private String TEXT = "Text";
    public static final String GRAPHIC = "Graphic";
    public boolean showTitle;
    public String detailLevel;
    public int scale = 1;

    public void setScale(int scale) {
        this.scale = scale;
    }

    @Override
    public void init(ScheduleGrid grid, PropertyList propertyList) {
        this.showTitle = propertyList.getProperty("showtitle").equals("Y");
        this.detailLevel = propertyList.getProperty("detaillevel");
    }

    @Override
    public String getCellColor(PlanItem planItem) {
        return "white";
    }

    @Override
    public String getTitle(PlanItem planItem) {
        return "";
    }

    @Override
    public String[] getDetailLevels() {
        return new String[]{this.TEXT, GRAPHIC};
    }

    @Override
    public String getCellHTML(PlanItem planItem, String detailLevel) {
        String html = "";
        if (planItem.getInstancePullAmount() != null) {
            StringBuffer output = new StringBuffer();
            double requires = planItem.getInstancePullAmount().getQuantity(planItem);
            String units = planItem.getInstancePullAmount().getUnits(planItem);
            String display = requires + (units.length() > 0 ? " " + units : "");
            if (detailLevel.equals(GRAPHIC)) {
                output.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
                int i = 0;
                while ((double)i < requires / (double)this.scale) {
                    output.append("<img src='WEB-OPAL/pagetypes/stability/images/bottle.gif'>");
                    ++i;
                }
                output.append("</td><td align=\"right\">(" + display + ")</td></tr></table>");
            } else {
                output.append(display);
            }
            html = output.toString();
        }
        if (html.equals("")) {
            html = "&nbsp;";
        }
        return html;
    }

    @Override
    public String getTip(PlanItem planItem, String detailLevel) {
        return "";
    }

    @Override
    public boolean getShow(PlanItem planItem) {
        return true;
    }

    @Override
    public boolean getShowTitle(PlanItem planItem) {
        return this.showTitle;
    }

    @Override
    public String getDetailLevel(PlanItem planItem) {
        return this.detailLevel;
    }
}

