/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.renderer.CellRenderer;
import com.labvantage.sapphire.stability.task.GridTaskStatus;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class TaskStatusCellRenderer
implements CellRenderer {
    Button acceptButton;
    Button rejectButton;
    Button infoButton;
    TranslationProcessor tp;
    public static final String acceptColor = "#b5da8b";
    public static final String rejectColor = "#ffb2b2";
    public static final String acceptSelectedCellColor = "#9DCE64";
    public static final String rejectSelectedCellColor = "#ff9999";

    @Override
    public void init(ScheduleGrid grid, PropertyList propertylist) {
        this.tp = new TranslationProcessor(grid.getPageContext());
        this.acceptButton = new Button(grid.getPageContext());
        this.acceptButton.setText(this.tp.translate("Accept"));
        this.acceptButton.setMargin("thin");
        this.acceptButton.setAppearance("standard");
        this.acceptButton.setModern(false);
        this.acceptButton.setTip(this.tp.translate("Click to approve this time point"));
        this.acceptButton.setImg("WEB-CORE/images/png/acceptcell.png");
        this.rejectButton = new Button(grid.getPageContext());
        this.rejectButton.setText(this.tp.translate("Reject"));
        this.rejectButton.setMargin("thin");
        this.rejectButton.setAppearance("standard");
        this.rejectButton.setModern(false);
        this.rejectButton.setTip(this.tp.translate("Click to reject this time point"));
        this.rejectButton.setImg("WEB-CORE/images/png/rejectplate.png");
        this.infoButton = new Button(grid.getPageContext());
        this.infoButton.setImg("WEB-CORE/imageref/flat/16/flat_black_information2.svg");
        this.infoButton.setMargin("thin");
        this.infoButton.setAppearance("standard");
        this.infoButton.setModern(false);
        this.infoButton.setTip(this.tp.translate("View Review Information of this time point"));
        for (String propertyTreeid : grid.taskTypes) {
            GridTaskStatus taskStatus = null;
            try {
                taskStatus = grid.taskTypes.getGridTaskStatus(propertyTreeid);
                if (taskStatus == null) continue;
                taskStatus.statusInit(grid, propertylist);
            }
            catch (SapphireException sapphireException) {}
        }
    }

    @Override
    public String getCellColor(PlanItem planItem) {
        String color = planItem.getInstanceGridTask().getColor();
        if (planItem.hasEvent) {
            if ("A".equalsIgnoreCase(planItem.reviewDispositionFlag)) {
                color = acceptColor;
            } else if ("R".equalsIgnoreCase(planItem.reviewDispositionFlag)) {
                color = rejectColor;
            } else if (planItem.eventStatus.equals("X") || planItem.eventStatus.equals("D") || planItem.eventStatus.equals("E") || planItem.eventStatus.equals("I")) {
                color = "buttonface";
            }
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
        String status;
        String statusText;
        String image;
        StringBuffer output = new StringBuffer();
        boolean completed = false;
        String color = planItem.getInstanceGridTask().getColor();
        if (planItem.hasEvent) {
            M18NUtil m18n = new M18NUtil(new ConnectionProcessor(planItem.grid.getConnectionId()).getConnectionInfo(planItem.grid.getConnectionId()));
            String date = m18n.format(planItem.eventdt, true);
            if (planItem.eventStatus.equals("D")) {
                image = "WEB-OPAL/pagetypes/stability/images/complete.gif";
                statusText = "<COMPLETE />Completed " + date;
                status = "Completed";
                completed = true;
                if (planItem.firstPlanItemInCell) {
                    try {
                        output.append(this.getTimePointReviewDivHtml(planItem));
                    }
                    catch (Exception e) {
                        output.append("<font color=\"red\">Exception Occurred: " + e.getMessage() + "</font>");
                    }
                }
            } else if (planItem.eventStatus.equals("E")) {
                image = "WEB-OPAL/pagetypes/stability/images/error.gif";
                statusText = "<COMPLETE />Error on " + date;
                status = "Error";
                completed = true;
            } else if (planItem.eventStatus.equals("I")) {
                image = "WEB-OPAL/pagetypes/stability/images/error.gif";
                statusText = "<COMPLETE />Acknowledged Error on <br/>" + date;
                status = "Acknowledged Error";
                completed = true;
            } else if (planItem.eventStatus.equals("X")) {
                image = "WEB-OPAL/pagetypes/stability/images/cancelled.gif";
                statusText = "<COMPLETE />Cancelled";
                status = "Cancelled";
                completed = true;
            } else {
                image = "WEB-OPAL/pagetypes/stability/images/trafficlight_green.gif";
                statusText = "Scheduled for " + date;
                status = "Scheduled";
            }
        } else if (planItem.conditionStatus.equals("C")) {
            image = "WEB-OPAL/pagetypes/stability/images/cancelled.gif";
            statusText = "<COMPLETE />Incomplete";
            status = "Incomplete";
        } else if (planItem.conditionStatus.equals("X")) {
            image = "WEB-OPAL/pagetypes/stability/images/cancelled.gif";
            statusText = "<COMPLETE />Cancelled";
            status = "Cancelled";
        } else {
            image = "WEB-OPAL/pagetypes/stability/images/noentry.gif";
            statusText = "Not Scheduled";
            status = "Not Scheduled";
        }
        if (planItem.status.equals("D")) {
            image = "WEB-OPAL/pagetypes/stability/images/disabled.gif";
            statusText = statusText != null && statusText.length() > 0 ? "Inactive <BR>" + statusText : "Inactive";
            status = "Inactive";
        }
        output.append("<img style=\"border: none\" src=\"" + image + "\" /> ");
        output.append(statusText + "<br>");
        if (completed) {
            GridTaskStatus taskStatus = null;
            try {
                taskStatus = planItem.grid.taskTypes.getGridTaskStatus(planItem.propertyTreeid);
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
            if (taskStatus != null) {
                output.append(taskStatus.getStatusHTML(planItem, detailLevel));
            } else {
                output.append("No summary data available");
            }
        } else {
            output.append(planItem.getInstanceGridTask().getSummaryHTML(planItem.getCollapsedPropertyList(), detailLevel));
        }
        return output.toString();
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

    public String getReviewInfoButtonHtml(PlanItem planItem) {
        StringBuffer output = new StringBuffer();
        String infoButtonId = "infobutton_" + planItem.gridCellId;
        this.infoButton.setId(infoButtonId);
        this.infoButton.setAction("showReviewInfo( '" + infoButtonId + "', 'infodiv_" + planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid + "', event)");
        String cellColor = this.getCellColor(planItem);
        this.infoButton.setStyle("background-color:" + cellColor + ";height:17px;width:18px; border: 1px ; padding:0px 0px 0px 0px;");
        output.append("<td width='*' align=right>" + this.infoButton.getHtml() + "</td>");
        return output.toString();
    }

    public String getTimePointReviewDivHtml(PlanItem planItem) throws SapphireException {
        StringBuffer output = new StringBuffer();
        M18NUtil m18n = new M18NUtil(new ConnectionProcessor(planItem.grid.getConnectionId()).getConnectionInfo(planItem.grid.getConnectionId()));
        DataSet dsSamples = StabilityUtil.getTimePointSamples(planItem.grid.queryProcessor, planItem);
        String reviewDisposition = planItem.getReviewDispositionFlag();
        boolean hasReviewRole = planItem.grid.connectionProcessor.getRoleList().contains("StabilityTimepointReview");
        if (hasReviewRole && StabilityUtil.isTimePointReadyForAcceptance(dsSamples, planItem, planItem.grid.queryProcessor)) {
            this.acceptButton.setAction("accept('" + planItem.scheduleplanid + "', '" + planItem.timeruleid + "', '" + planItem.conditionid + "', " + StabilityUtil.getTimePointPlanItemCount(planItem.grid.queryProcessor, planItem) + ", '" + planItem.gridCellId + "')");
            this.rejectButton.setAction("reject('" + planItem.scheduleplanid + "', '" + planItem.timeruleid + "', '" + planItem.conditionid + "', " + StabilityUtil.getTimePointPlanItemCount(planItem.grid.queryProcessor, planItem) + ", '" + planItem.gridCellId + "')");
            this.acceptButton.setId("acceptbutton_" + planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid);
            this.rejectButton.setId("rejectbutton_" + planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid);
            this.acceptButton.setStyle("height:25px;width:70px; border: 1px solid; border-radius:6px; padding:2px 2px 2px 2px;background-color:#ffffff");
            this.rejectButton.setStyle("height:25px;width:70px; border: 1px solid; border-radius:6px; padding:2px 2px 2px 2px;background-color:#ffffff");
            this.acceptButton.setDisabled(false);
            this.rejectButton.setDisabled(false);
            String trStyle = "style= 'display:block' ";
            if ("A".equalsIgnoreCase(reviewDisposition) || "R".equalsIgnoreCase(reviewDisposition)) {
                this.acceptButton.setStyle("height:25px;width:70px; border: 1px solid; border-radius:6px; padding:2px 2px 2px 2px;background-color:#ffffff;display:none");
                this.rejectButton.setStyle("height:25px;width:70px; border: 1px solid; border-radius:6px; padding:2px 2px 2px 2px;background-color:#ffffff;display:none");
                trStyle = "style= 'display:none' ";
            }
            output.append("<table border=0 cellspacing = 1 cellpadding = 1 width = '160px'>");
            output.append("<tr " + trStyle + "id=\"tr_").append(planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid).append("\">");
            output.append("<td width = '20px'>&nbsp;</td>");
            output.append("<td  nowrap>" + this.acceptButton.getHtml() + "</td><td>" + this.rejectButton.getHtml() + "</td>");
            output.append(" </tr></table>");
        }
        if (dsSamples.getRowCount() > 1) {
            output.append("<table border=0 cellspacing=1 cellpadding=1 width='160px'><tr><td width ='20px'>&nbsp;</td><td   nowrap><a target='taskdetails' title='" + this.tp.translate("View All Samples of this time point") + "' href='rc?command=page&page=SampleGridStatus&queryid=SamplesByTimePoint&param1=" + HttpUtil.encodeURIComponent(planItem.scheduleplanid) + "&param2=" + HttpUtil.encodeURIComponent(planItem.timeruleid) + "&param3=" + HttpUtil.encodeURIComponent(planItem.conditionid) + "'>" + this.tp.translate("View All Samples") + "</a></td></tr></table>");
        }
        if (reviewDisposition.length() > 0) {
            String user = planItem.reviewByDesc.length() > 0 ? planItem.reviewBy + " ( " + planItem.reviewByDesc + " )" : planItem.reviewBy;
            output.append("<div id=\"infodiv_" + planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid + "\" style='position: absolute; display: none;z-index: 9999;'>");
            output.append("<table style = 'background:#eeffff;border:2px solid black;border-radius:8px' cellpadding = 5 >");
            output.append("<tr class='info_message_row'><td width='10px' valign='top' rowspan='2'><img src='WEB-CORE/images/dialog_info.png' widtht='20x' height='20px'></td>");
            output.append("<td width='30px' valign=top align=left>").append("A".equalsIgnoreCase(reviewDisposition) ? "Accepted" : "Rejected").append(" By:</td><td nowrap align=left valign=top >" + SafeHTML.encodeForHTML(user) + "</td></tr>");
            output.append("<tr class=info_message_row><td nowrap align=left valign=top>").append("A".equalsIgnoreCase(reviewDisposition) ? "Accepted" : "Rejection").append(" Date:</td><td nowrap align=left valign=top>" + m18n.format(planItem.reviewDt, true) + "</td></tr>");
            output.append("<tr><td>&nbsp;</td>").append("<td colspan=2 class=\"alert_button\" >").append(hasReviewRole ? "<button title=\"" + this.tp.translate("Reset Review Disposition of this time point") + "\" style=\"width:120px;height:25px;white-space:nowrap;border-radius:4px;\" onclick=\"resetReviewtDisposition( '" + planItem.scheduleplanid + "', '" + planItem.timeruleid + "', '" + planItem.conditionid + "', " + StabilityUtil.getTimePointPlanItemCount(planItem.grid.queryProcessor, planItem) + ", '" + planItem.gridCellId + "')\"><img src=\"WEB-CORE/images/png/Reset.png\" alt=\"Reset & Close\"/>&nbsp;" + this.tp.translate("Reset & Close") + "</button>" : "").append("&nbsp;&nbsp;<button class=\"dialog_contents_btn\"  title=\"" + this.tp.translate("Close") + "\" style=\"width:68px;height:25px;white-space:nowrap;border-radius:4px\" onclick=\"closeInfoDiv('infodiv_" + planItem.scheduleplanid + "_" + planItem.timeruleid + "_" + planItem.conditionid + "')\"><img src=\"WEB-CORE/images/png/Cancel.png\" alt=\"Close\"/>&nbsp;" + this.tp.translate("Close") + "</button></td>").append("</tr>");
            output.append("</table></div>");
        }
        return output.toString();
    }
}

