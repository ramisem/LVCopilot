/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.renderer.AxisRenderer;
import java.math.BigDecimal;
import java.util.ArrayList;
import sapphire.xml.PropertyList;

public class ConditionAxisInventoryRenderer
implements AxisRenderer {
    public String getForm(BaseAxis axis, int row) {
        StringBuffer output = new StringBuffer();
        output.append("Enter the Condition label: <input name=\"conditionlabel\" " + (row >= 0 ? "value=\"" + axis.items.getValue(row, "conditionlabel") + "\"" : "") + "/>");
        return output.toString();
    }

    @Override
    public void init(PropertyList propertyList) {
    }

    @Override
    public String getTitleHTML(BaseAxis axis, int row) {
        String id = axis.items.getString(row, axis.idColumn);
        String title = axis.items.getString(row, "conditionlabel");
        StringBuffer output = new StringBuffer(title + "<br>");
        BigDecimal pullQuantity = axis.items.getBigDecimal(row, "qtypull");
        String pullQuantityUnits = axis.items.getValue(row, "qtypullunits");
        ArrayList items = axis.grid.planItems.findByCondition(id);
        double totalRequires = 0.0;
        for (PlanItem planItem : items) {
            if (planItem.getInstancePullAmount() == null) continue;
            double requires = planItem.getInstancePullAmount().getQuantity(planItem);
            String units = planItem.getInstancePullAmount().getUnits(planItem);
            totalRequires += requires;
        }
        BigDecimal calculated = new BigDecimal(123);
        output.append("<table>");
        output.append("<tr><td>Total Pull Amount: </td><td><input size=\"10\" name=\"initialload_" + id + "\" value=\"" + (pullQuantity == null || pullQuantity.compareTo(calculated) < 0 ? "" : "" + pullQuantity) + "\" > " + pullQuantityUnits + "</td></tr>");
        output.append("<tr><td>(Calculated): </td><td>" + totalRequires + " g</td></tr>");
        output.append("</table>");
        return output.toString();
    }

    @Override
    public String getTitleTip(BaseAxis axis, int row) {
        return null;
    }
}

