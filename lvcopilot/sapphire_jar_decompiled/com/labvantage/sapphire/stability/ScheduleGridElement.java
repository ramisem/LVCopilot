/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.renderer.AxisRenderer;
import com.labvantage.sapphire.stability.renderer.CellRenderer;
import com.labvantage.sapphire.stability.renderer.TaskStatusCellRenderer;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class ScheduleGridElement
extends BaseElement {
    public static final boolean debug = false;
    static Cache defptreenodeCache = new Cache("defptreenode ");
    static Cache ptreenodeCache = new Cache("ptreenode ");

    @Override
    public String getHtml() {
        try {
            String buttonText;
            TranslationProcessor tp = new TranslationProcessor(this.pageContext);
            HttpSession session = this.pageContext.getSession();
            ScheduleGrid grid = (ScheduleGrid)session.getAttribute("ScheduleGrid");
            grid.initGrid(this.pageContext);
            boolean timeAcross = this.element.getProperty("timeacross").equals("Y");
            boolean forceBlankRow = this.element.getProperty("forceblankrow").equals("Y");
            boolean alignTasks = this.element.getProperty("aligntasks").equals("Y");
            String defaultTask = this.element.getProperty("defaulttask");
            PropertyList timePropertyList = this.element.getPropertyList("timeaxis");
            PropertyList conditionPropertyList = this.element.getPropertyList("conditionaxis");
            PropertyList cellRendererPropertyList = this.element.getPropertyList("cells");
            BaseAxis xAxis = timeAcross ? grid.timeAxis : grid.conditionAxis;
            BaseAxis yAxis = timeAcross ? grid.conditionAxis : grid.timeAxis;
            PropertyList xPropertyList = timeAcross ? timePropertyList : conditionPropertyList;
            PropertyList yPropertyList = timeAcross ? conditionPropertyList : timePropertyList;
            AxisRenderer xRenderer = (AxisRenderer)Class.forName(xPropertyList.getProperty("renderer")).newInstance();
            xRenderer.init(xPropertyList);
            boolean xshowAddButton = xPropertyList.getProperty("showaddbutton").equals("Y");
            boolean xshowMenuButton = xPropertyList.getProperty("showmenubutton").equals("Y");
            boolean xtrapTitleClick = xPropertyList.getProperty("traptitleclick").equals("Y");
            AxisRenderer yRenderer = (AxisRenderer)Class.forName(yPropertyList.getProperty("renderer")).newInstance();
            yRenderer.init(yPropertyList);
            boolean yshowAddButton = yPropertyList.getProperty("showaddbutton").equals("Y");
            boolean yshowMenuButton = yPropertyList.getProperty("showmenubutton").equals("Y");
            boolean ytrapTitleClick = yPropertyList.getProperty("traptitleclick").equals("Y");
            CellRenderer cellRenderer = (CellRenderer)Class.forName(cellRendererPropertyList.getProperty("renderer")).newInstance();
            cellRenderer.init(grid, cellRendererPropertyList);
            Button xButton = null;
            Button yButton = null;
            ArrayList<String> timePoints = new ArrayList<String>();
            if (xshowAddButton) {
                xButton = new Button(this.pageContext);
                xButton.setAction("addAxisItem ( '" + (timeAcross ? "time" : "condition") + "' )");
                xButton.setId("xaddbutton");
                buttonText = xAxis.items.size() > 0 ? "Add " + xAxis.buttonLabel : "Start Adding " + xAxis.buttonLabel + "s";
                xButton.setText(tp.translate(buttonText));
            }
            if (yshowAddButton) {
                yButton = new Button(this.pageContext);
                yButton.setAction("addAxisItem ( '" + (timeAcross ? "condition" : "time") + "' )");
                yButton.setId("yaddbutton");
                buttonText = yAxis.items.size() > 0 ? "Add " + yAxis.buttonLabel : "Start Adding " + yAxis.buttonLabel + "s";
                yButton.setText(tp.translate(buttonText));
            }
            StringBuffer output = new StringBuffer();
            StringBuffer gridArray = new StringBuffer("var __cells = new Array();\n");
            StringBuffer reviewFlagsArray = new StringBuffer("var __reviewflags = new Array();\n");
            ArrayList<Row> rows = new ArrayList<Row>();
            int[] yCount = new int[yAxis.items.size()];
            if (defaultTask.length() == 0) {
                defaultTask = grid.taskTypes.size() > 0 ? (String)grid.taskTypes.get(0) : "";
            }
            for (int y = 0; y < yAxis.items.size(); ++y) {
                ArrayList planItems;
                boolean ychange = true;
                String yid = yAxis.items.getString(y, yAxis.idColumn);
                if (alignTasks) {
                    boolean hasItems = false;
                    for (int type = 0; type < grid.taskTypes.size(); ++type) {
                        String propertytreeid = (String)grid.taskTypes.get(type);
                        Row row = null;
                        if (grid.taskTypes.getShow(propertytreeid)) {
                            for (int x = 0; x < xAxis.items.size(); ++x) {
                                String conditionid;
                                String xid = xAxis.items.getString(x, xAxis.idColumn);
                                String timeruleid = timeAcross ? xid : yid;
                                planItems = grid.planItems.findByTimeConditionPropertyTree(timeruleid, conditionid = timeAcross ? yid : xid, propertytreeid);
                                if (planItems.size() <= 0) continue;
                                hasItems = true;
                                if (row == null) {
                                    row = new Row(xAxis.items.size());
                                    row.y = y;
                                    row.yid = yid;
                                    row.propertyTreeid = propertytreeid;
                                    row.yChange = ychange;
                                    ychange = false;
                                    rows.add(row);
                                }
                                row.planItems[x] = planItems;
                                if (planItems.size() <= row.copies) continue;
                                row.copies = planItems.size();
                            }
                        }
                        if (row == null) continue;
                        int n = y;
                        yCount[n] = yCount[n] + row.copies;
                    }
                    if (hasItems && !forceBlankRow) continue;
                    Row row = new Row(xAxis.items.size());
                    row.y = y;
                    row.yid = yid;
                    row.propertyTreeid = defaultTask;
                    row.yChange = ychange;
                    ychange = false;
                    row.copies = 1;
                    rows.add(row);
                    int n = y;
                    yCount[n] = yCount[n] + 1;
                    continue;
                }
                Row row = new Row(xAxis.items.size());
                row.y = y;
                row.yid = yid;
                row.yChange = ychange;
                ychange = false;
                rows.add(row);
                boolean hasItems = false;
                for (int x = 0; x < xAxis.items.size(); ++x) {
                    row.planItems[x] = new ArrayList();
                    String xid = xAxis.items.getString(x, xAxis.idColumn);
                    String timeruleid = timeAcross ? xid : yid;
                    String conditionid = timeAcross ? yid : xid;
                    for (int type = 0; type < grid.taskTypes.size(); ++type) {
                        String propertytreeid = (String)grid.taskTypes.get(type);
                        if (!grid.taskTypes.getShow(propertytreeid)) continue;
                        planItems = grid.planItems.findByTimeConditionPropertyTree(timeruleid, conditionid, propertytreeid);
                        row.planItems[x].addAll(planItems);
                    }
                    if (row.planItems[x].size() > row.copies) {
                        row.copies = row.planItems[x].size();
                    }
                    if (row.planItems[x].size() <= 0) continue;
                    hasItems = true;
                }
                if (hasItems && forceBlankRow) {
                    ++row.copies;
                }
                int n = y;
                yCount[n] = yCount[n] + row.copies;
            }
            boolean emptyGrid = rows.size() == 0 && xAxis.items.size() == 0;
            output.append("<table>\n");
            output.append("<tr>\n");
            output.append("<td>\n");
            output.append("<table " + (emptyGrid ? "style=\"margin-left: 10px\"" : "class=\"maintable\"") + " cellpadding=\"0\" cellspacing=\"0\">\n");
            output.append("<tr><td></td>");
            for (int x = 0; x < xAxis.items.size(); ++x) {
                String scheduleconditionId = xAxis.items.getValue(x, "scheduleconditionid");
                this.drawAxisTitle(output, xRenderer, xAxis, x, "X", timeAcross ? "time" : "condition", 1, x, xshowMenuButton, xtrapTitleClick, xAxis.isReadOnly(x), scheduleconditionId);
            }
            output.append("</tr>\n");
            int arrayRowCount = 0;
            int planItemCount = 0;
            for (int rowCount = 0; rowCount < rows.size(); ++rowCount) {
                Row row = (Row)rows.get(rowCount);
                Row nextRow = rowCount < rows.size() - 1 ? (Row)rows.get(rowCount + 1) : null;
                String yid = row.yid;
                for (int copy = 0; copy < row.copies; ++copy) {
                    output.append("<tr>\n");
                    if (row.yChange && copy == 0) {
                        this.drawAxisTitle(output, yRenderer, yAxis, row.y, "Y", timeAcross ? "condition" : "time", yCount[row.y], arrayRowCount, yshowMenuButton, ytrapTitleClick, yAxis.isReadOnly(row.y), "");
                    }
                    gridArray.append("__cells[" + arrayRowCount++ + "] = new Array( ");
                    for (int x = 0; x < xAxis.items.size(); ++x) {
                        String className;
                        String xid = xAxis.items.getString(x, xAxis.idColumn);
                        String timeruleid = timeAcross ? xid : yid;
                        String timeLabel = timeAcross ? xAxis.getLabel(xid) : yAxis.getLabel(yid);
                        String conditionLabel = timeAcross ? yAxis.getLabel(yid) : xAxis.getLabel(xid);
                        String modCondLabel = "";
                        int startindex = 0;
                        if (conditionLabel != null && conditionLabel.indexOf("<a") > -1) {
                            while (conditionLabel.indexOf("<a", startindex) > -1) {
                                int idx = conditionLabel.indexOf("<a", startindex);
                                if (idx > 0) {
                                    modCondLabel = conditionLabel.substring(0, idx);
                                }
                                modCondLabel = modCondLabel + conditionLabel.substring(idx, idx + 3) + " rel=\"noopener noreferrer nofollower\" target=\"_blank\" " + conditionLabel.substring(idx + 4);
                                startindex = idx + 3;
                                conditionLabel = modCondLabel;
                            }
                        }
                        if (modCondLabel.length() == 0) {
                            modCondLabel = conditionLabel;
                        }
                        String conditionid = timeAcross ? yid : xid;
                        PlanItem item = row.planItems[x] != null && copy < row.planItems[x].size() ? (PlanItem)row.planItems[x].get(copy) : null;
                        String propertyTreeid = row.propertyTreeid;
                        if (item != null) {
                            propertyTreeid = item.propertyTreeid;
                        }
                        if (propertyTreeid == null || propertyTreeid.length() == 0) {
                            propertyTreeid = defaultTask;
                        }
                        String id = rowCount + "_" + copy + "_" + x;
                        String string = nextRow == null || nextRow.yChange && copy == row.copies - 1 ? (row.yChange && copy == 0 ? "cellsingle" : "cellbottom") : (className = row.yChange && copy == 0 ? "celltop" : "cellnormal");
                        if (item == null) {
                            output.append("  <td id=\"" + id + "\" valign=\"top\" class=\"" + className + "\"  ");
                            output.append(" planitemid=\"\" propertytreeid=\"" + propertyTreeid + "\" timeruleid=\"" + timeruleid + "\" conditionid=\"" + conditionid + "\" timelabel=\"" + timeLabel + "\" conditionlabel=\"" + SafeHTML.encodeForHTMLAttribute(modCondLabel) + "\">\n" + "" + "    &nbsp;\n  </td>\n");
                        } else {
                            item.gridCellId = id;
                            String reviewDisposition = item.getReviewDispositionFlag();
                            if (!timePoints.contains(item.scheduleplanid + ";" + item.timeruleid + ";" + item.conditionid)) {
                                timePoints.add(item.scheduleplanid + ";" + item.timeruleid + ";" + item.conditionid);
                                item.firstPlanItemInCell = true;
                                String itemId = item.scheduleplanid + item.timeruleid + item.conditionid;
                                reviewFlagsArray.append("\n__reviewflags[" + planItemCount++ + "] = new Array( \"" + itemId + "\",\"" + reviewDisposition + "\",\"" + reviewDisposition + "\");");
                            }
                            String detailLevel = cellRenderer.getDetailLevel(item);
                            output.append("  <td id=\"" + id + "\" valign=\"top\" class=\"" + className + "\" style=\"background-color: " + cellRenderer.getCellColor(item) + "\" ");
                            output.append(" title=\"" + cellRenderer.getTip(item, detailLevel) + "\" \n");
                            output.append(" propertytreeid=\"" + propertyTreeid + "\" planitemid=\"" + item.planItemid + "\" timeruleid=\"" + timeruleid + "\" conditionid=\"" + conditionid + "\" timelabel=\"" + timeLabel + "\" conditionlabel=\"" + SafeHTML.encodeForHTMLAttribute(modCondLabel) + "\">\n");
                            if (cellRenderer.getShowTitle(item)) {
                                output.append("   <Table width=100%><tr><td align=left nowrap style=\"font-weight: bold\">" + "" + cellRenderer.getTitle(item) + "</td>");
                                if (cellRenderer instanceof TaskStatusCellRenderer && item.firstPlanItemInCell && reviewDisposition.length() > 0) {
                                    output.append(((TaskStatusCellRenderer)cellRenderer).getReviewInfoButtonHtml(item));
                                }
                                output.append("</tr></table>\n");
                            }
                            try {
                                output.append("    " + cellRenderer.getCellHTML(item, detailLevel) + "\n");
                            }
                            catch (Exception e) {
                                output.append("<span style=\"color: red; font-weight: bold\">Error: " + e.getMessage() + "</span>");
                            }
                            output.append("  </td>\n");
                        }
                        gridArray.append((x > 0 ? "," : "") + "\"" + id + "\"");
                    }
                    gridArray.append(");\n");
                    output.append("</tr>\n");
                }
            }
            output.append("\t\t\t</table>\n");
            output.append("\t\t</td>\n");
            output.append("\t\t<td valign=\"top\">\n");
            if (xButton != null) {
                output.append(xButton.getHtml());
            }
            output.append("\t\t</td>\n");
            output.append("\t</tr>\n");
            output.append("\t<tr>\n");
            output.append("\t\t<td>\n");
            output.append("\t\t\t<table cellpadding=\"0\" cellspacing=\"0\" class=\"maintablebottombutton\"><tr><td>");
            if (yButton != null) {
                output.append(yButton.getHtml());
            }
            output.append("</td></tr></table>\n");
            output.append("\t\t</td>\n");
            output.append("\t\t<td></td>\n");
            output.append("\t</tr>\n");
            output.append("</table>\n");
            output.append("<script>").append(gridArray).append("</script>");
            output.append("\n<script>");
            output.append(reviewFlagsArray.toString());
            output.append("\n</script>");
            return output.toString();
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            return "Exception caught drawing grid:<br> " + e.getMessage();
        }
    }

    private void drawAxisTitle(StringBuffer output, AxisRenderer renderer, BaseAxis axis, int item, String axisDirection, String axisType, int rowSpan, int clickIndex, boolean showMenuButton, boolean trapTitleClick, boolean readonly, String scheduleConditionId) throws SapphireException {
        output.append("<td ");
        if (trapTitleClick) {
            output.append("onClick=\"on" + axisDirection + "AxisClicked( " + clickIndex + ", " + rowSpan + " );sapphire.events.cancelEvent(event, false);\"");
        }
        output.append(" rowSpan=\"" + rowSpan + "\" class=\"" + axisDirection + "title\" valign=\"top\">\n");
        output.append("    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        output.append("<td width=\"99%\" valign=\"top\" nowrap>" + renderer.getTitleHTML(axis, item) + "</td>");
        output.append("<td width=\"1%\" nowrap width=\"1%\" align=\"right\">&nbsp;");
        if (showMenuButton) {
            if ("X".equalsIgnoreCase(axisDirection)) {
                output.append("<img class=\"axisbutton\" readonlyflag=" + (readonly ? "true" : "false") + " id=\"axisdd_" + axisDirection + item + "\" axisType=\"" + axisType + "\" item=\"" + item + "\" onMouseOver=\"this.className='axisbutton_highlight'\" onMouseOut=\"this.className='axisbutton'\" onClick=\"show" + axisDirection + "Menu( this, event, '" + scheduleConditionId + "' );sapphire.events.cancelEvent(event, false);\" name=\"menubutton\" style=\"cursor: pointer\" src=\"WEB-OPAL/pagetypes/stability/images/transparentdropdown.gif\">");
            } else {
                output.append("<img class=\"axisbutton\" readonlyflag=" + (readonly ? "true" : "false") + " id=\"axisdd_" + axisDirection + item + "\" axisType=\"" + axisType + "\" item=\"" + item + "\" onMouseOver=\"this.className='axisbutton_highlight'\" onMouseOut=\"this.className='axisbutton'\" onClick=\"show" + axisDirection + "Menu( this, event );sapphire.events.cancelEvent(event, false);\" name=\"menubutton\" style=\"cursor: pointer\" src=\"WEB-OPAL/pagetypes/stability/images/transparentdropdown.gif\">");
            }
        } else {
            output.append("<img style=\"cursor: pointer; width: 13px; height: 13px\" src=\"WEB-OPAL/pagetypes/stability/images/transparentpixel.gif\">");
        }
        output.append("</td>");
        output.append("</tr></table>\n");
        output.append("  </td>\n");
    }

    class Row {
        String yid;
        int y;
        String propertyTreeid = null;
        ArrayList[] planItems;
        int copies = 1;
        boolean yChange = false;

        Row(int columns) {
            this.planItems = new ArrayList[columns];
        }
    }
}

