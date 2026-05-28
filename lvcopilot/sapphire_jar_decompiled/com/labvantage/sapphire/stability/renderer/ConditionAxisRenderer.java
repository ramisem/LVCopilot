/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.renderer.AxisRenderer;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class ConditionAxisRenderer
implements AxisRenderer {
    boolean showDetails = false;
    boolean showDefaults = false;
    boolean showStatus = false;

    @Override
    public void init(PropertyList propertyList) {
        this.showDetails = propertyList.getProperty("showdetails").equals("Y");
        this.showDefaults = propertyList.getProperty("showdefaults").equals("Y");
        this.showStatus = propertyList.getProperty("showstatus").equals("Y");
    }

    @Override
    public String getTitleHTML(BaseAxis axis, int row) throws SapphireException {
        StringBuffer output = new StringBuffer();
        output.append(this.showStatus ? this.getStatusLabel(axis, row) : this.getSimpleLabel(axis, row));
        output.append(this.getDetails(axis, row));
        return output.toString();
    }

    String getSimpleLabel(BaseAxis axis, int row) {
        TranslationProcessor tp = new TranslationProcessor(axis.connectionInfo.getConnectionId());
        String output = "";
        String label = axis.items.getValue(row, "conditionlabel");
        output = output + (label.length() == 0 ? "<span style=\"color:red\">" + tp.translate("Label not set") + "</span><br>" : SafeHTML.encodeForHTMLAttribute(label) + "<br>");
        String autoCalc = axis.items.getValue(row, "autocalcflag");
        String pullQuantity = axis.items.getValue(row, "qtypull");
        if (!autoCalc.equals("Y") && pullQuantity.length() == 0) {
            output = output + "<span style=\"color:red\">" + tp.translate("Pull quantitiy not set") + "</span><br>";
        }
        String storageenvid = axis.items.getValue(row, "storageenvid");
        output = output + (storageenvid.length() == 0 ? "<span style=\"color:red\">" + tp.translate("Environment not set") + "</span><br>" : "");
        return output;
    }

    String getStatusLabel(BaseAxis axis, int row) {
        TranslationProcessor tp = new TranslationProcessor(axis.connectionInfo.getConnectionId());
        StringBuffer output = new StringBuffer();
        String label = axis.items.getValue(row, "conditionlabel");
        if (label.length() == 0) {
            label = "<span style=\"color:red\">" + tp.translate("No label set") + "</span>";
        }
        String status = axis.items.getValue(row, "conditionstatus");
        Calendar startdt = axis.items.getCalendar(row, "startdt");
        Locale locale = axis.items.getLocale();
        TimeZone tz = axis.items.getTimeZone();
        DateFormat dfmt = DateFormat.getDateTimeInstance(2, 3, locale);
        dfmt.setTimeZone(tz);
        String date = startdt == null ? "" : dfmt.format(startdt.getTime());
        String image = this.getIcon(status);
        String statusText = this.getStatusText(status);
        output.append("<div align=\"center\" style=\"padding: 2\" width=\"100%\">");
        output.append("<b>" + SafeHTML.encodeForHTMLAttribute(label) + "</b><br>");
        output.append("<img style=\"border: none\" src=\"" + image + "\" /> " + tp.translate(statusText) + "<br>");
        if (date.length() > 0) {
            output.append(date + "<br>");
        }
        output.append("</div");
        return output.toString();
    }

    String getDetails(BaseAxis axis, int row) throws SapphireException {
        StringBuffer output = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(axis.connectionInfo.getConnectionId());
        if (this.showDetails) {
            String storageenvid = axis.items.getValue(row, "storageenvid");
            String orientation = axis.items.getValue(row, "orientation");
            String startcriteria = axis.items.getValue(row, "startcriteria");
            String pullQuantity = axis.items.getValue(row, "qtypull");
            String pullQuantityType = axis.items.getValue(row, "qtypulltype");
            String pullQuantityUnits = pullQuantityType.equals("C") ? " Containers" : axis.items.getValue(row, "qtypullunits");
            String autocalcflag = axis.items.getValue(row, "autocalcflag");
            String reserveQuantity = axis.items.getValue(row, "qtyreserve");
            String reserveQuantityType = axis.items.getValue(row, "qtyreservetype");
            String reserveQuantityUnits = reserveQuantityType.equals("C") ? " Containers" : (reserveQuantityType.equals("P") ? "% of Total" : axis.items.getValue(row, "qtyreserveunits"));
            String notSetMsg = tp.translate("Not set");
            String notes = axis.items.getValue(row, "notes");
            output.append(tp.translate("Environment") + ": " + (storageenvid.length() == 0 ? "<span style=\"color:red\">" + notSetMsg + "</span>" : storageenvid));
            output.append("<br>" + tp.translate("Orientation") + ": " + (orientation.length() == 0 ? "<span style=\"color:red\">" + notSetMsg + "</span>" : orientation));
            output.append("<br>" + tp.translate("Reserve") + ": " + (reserveQuantity.length() == 0 ? "<span style=\"color:red\">" + notSetMsg + "</span>" : reserveQuantity + reserveQuantityUnits));
            if (!autocalcflag.equals("Y")) {
                output.append("<br>" + tp.translate("Total Required") + ": " + (pullQuantity.length() == 0 ? "<span style=\"color:red\">" + notSetMsg + "</span>" : pullQuantity + pullQuantityUnits));
            }
            output.append("<br>" + (startcriteria.length() == 0 ? tp.translate("Manual start date") : tp.translate("Start") + ": STUDY." + startcriteria));
            if (notes.length() > 40) {
                notes = notes.substring(0, 39) + "...";
            }
            if (notes.length() > 0) {
                output.append("<br>" + tp.translate("Notes") + ":<i>" + notes + "</i>");
            }
        }
        if (this.showDefaults) {
            for (int i = 0; i < axis.grid.taskTypes.size(); ++i) {
                String propertytreeid = (String)axis.grid.taskTypes.get(i);
                PropertyTree tree = axis.grid.getPropertyTree(propertytreeid);
                Node planNode = tree.getNode("__plan");
                Node conditionNode = tree.createNode("__condition", planNode);
                conditionNode.setPropertyList(axis.grid.conditionAxis.getPropertyList(row, propertytreeid));
                PropertyList renderPropertyList = tree.getNodePropertyList("__condition", true);
                if (renderPropertyList == null || renderPropertyList.size() <= 0) continue;
                GridTask instance = axis.grid.taskTypes.getGridTask(propertytreeid);
                output.append("<div style=\"width: 100%; background-color: " + instance.getColor() + "\" ><b>" + instance.getTitle() + "</b><br> " + instance.getSummaryHTML(renderPropertyList, instance.getDetailLevels()[0]));
            }
        }
        return output.toString();
    }

    @Override
    public String getTitleTip(BaseAxis axis, int row) {
        return "";
    }

    public String getIcon(String status) {
        if (status.equals("R")) {
            return "WEB-OPAL/pagetypes/stability/images/running.gif";
        }
        if (status.equals("S")) {
            return "WEB-OPAL/pagetypes/stability/images/suspended.gif";
        }
        if (status.equals("C")) {
            return "WEB-OPAL/pagetypes/stability/images/completed.gif";
        }
        if (status.equals("X")) {
            return "WEB-OPAL/pagetypes/stability/images/cancelled.gif";
        }
        return "WEB-OPAL/pagetypes/stability/images/notstarted.gif";
    }

    public String getStatusText(String status) {
        if (status.equals("R")) {
            return "Running";
        }
        if (status.equals("S")) {
            return "Suspended";
        }
        if (status.equals("C")) {
            return "Completed";
        }
        if (status.equals("X")) {
            return "Cancelled";
        }
        return "Not Started ";
    }
}

