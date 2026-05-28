/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class TaskUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderTaskInfo(SDIData sdidata) {
        ConfigReportContent content = new ConfigReportContent(this.config, "Task info:" + TaskUtil.getPrimaryValue(sdidata, "taskid"));
        content.startTable();
        content.startRow();
        content.addRowItem("Task ID", TaskUtil.getPrimaryValue(sdidata, "taskid"));
        content.addRowItem("Task Type", this.getTaskType(sdidata));
        content.endRow();
        content.startRow();
        content.addRowItem("Description", TaskUtil.getPrimaryValue(sdidata, "taskdesc"), 3);
        content.endRow();
        content.startRow();
        String fklink = ConfigReportContent.createHyperLink("Action", this.getTaskActionId(sdidata), this.getTaskActionVersionId(sdidata), "", this.sdisIncluded, this.frames);
        content.addRowItem("Action ID", fklink);
        content.addRowItem("Action Version", this.getTaskActionVersionId(sdidata));
        content.endRow();
        content.startRow();
        content.addRowItem("Active", this.getTaskStatus(sdidata));
        content.addRowItem("Delete when finished", this.getDeleteWhenFinished(sdidata));
        content.endRow();
        content.startRow();
        content.addRowItem("Back-fill Missed Events", this.getBackFill(sdidata));
        content.addRowItem("Process Exclusively", this.getProcessExclusive(sdidata));
        content.endRow();
        content.startRow();
        content.addRowItem("Schedule Type", this.getTaskScheduleType(sdidata));
        content.endRow();
        content.endTable();
        return content;
    }

    public ConfigReportContent renderTaskInfoDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task info:" + TaskUtil.getPrimaryValue(srcSDIData, "taskid"));
        buffer.startTable();
        buffer.startRow();
        buffer.addDiffRowItem("Task ID", TaskUtil.getPrimaryValue(srcSDIData, "taskid"), TaskUtil.getPrimaryValue(refSDIData, "taskid"));
        buffer.addDiffRowItem("Task Type", this.getTaskType(srcSDIData), this.getTaskType(refSDIData), this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        String fklink = ConfigReportContent.createHyperLink("Action", this.getTaskActionId(srcSDIData), this.getTaskActionVersionId(srcSDIData), "", this.sdisIncluded, this.frames);
        buffer.addDiffRowItem("Action ID", this.getTaskActionId(srcSDIData), this.getTaskActionId(refSDIData));
        buffer.addDiffRowItem("Action Version", this.getTaskActionVersionId(srcSDIData), this.getTaskActionVersionId(refSDIData));
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Description", TaskUtil.getPrimaryValue(srcSDIData, "taskdesc"), TaskUtil.getPrimaryValue(refSDIData, "taskdesc"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Active", this.getTaskStatus(srcSDIData), this.getTaskStatus(refSDIData));
        buffer.addDiffRowItem("Delete when finished", this.getDeleteWhenFinished(srcSDIData), this.getDeleteWhenFinished(refSDIData));
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Back-fill Missed Events", this.getBackFill(srcSDIData), this.getBackFill(refSDIData), this.getTranslationProcessor());
        buffer.addDiffRowItem("Process Exclusively", this.getProcessExclusive(srcSDIData), this.getProcessExclusive(refSDIData), this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Schedule Type", this.getTaskScheduleType(srcSDIData), this.getTaskScheduleType(refSDIData));
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderTaskProperties(SDIData sdiData) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Task properties:" + TaskUtil.getPrimaryValue(sdiData, "taskid"));
        configReportContent.renderListTable(this.getTaskProperties(sdiData), this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderTaskPropertiesDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Task properties:" + TaskUtil.getPrimaryValue(srcSDIData, "taskid"));
        String[] keycols = new String[]{"Property ID"};
        DataSet taskProperties = this.getTaskProperties(srcSDIData);
        DataSet taskRefProperties = this.getTaskProperties(refSDIData);
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "actionproperty", taskProperties.getColumns());
        String tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "Task", "task properties", "taskproperty");
        String itemdisplay = "[Property ID]";
        buffer.renderDetailTablesDiff(columnTitleMap, "taskproperty", tablelabel, itemdisplay, taskProperties, taskRefProperties, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        return buffer;
    }

    public ConfigReportContent renderScheduleInfo(SDIData sdiData) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Task schedule info:" + TaskUtil.getPrimaryValue(sdiData, "taskid"));
        String si = this.getScheduleInfo(sdiData);
        configReportContent.append(si);
        return configReportContent;
    }

    public ConfigReportContent renderScheduleInfoDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Task schedule info:" + TaskUtil.getPrimaryValue(srcSDIData, "taskid"));
        String si = this.getScheduleInfo(srcSDIData);
        String refSi = this.getScheduleInfo(refSDIData);
        configReportContent.append(ConfigReportContent.getDiffString(si, refSi));
        return configReportContent;
    }

    public String getScheduleInfo(SDIData sdiData) {
        String ret = "";
        String taskTypeFlag = this.getTaskScheduleType(sdiData);
        String scheduleRule = this.getScheduleRule(sdiData);
        String scheduleDt = this.getScheduleDt(sdiData);
        if (sdiData.getDataset("primary") == null) {
            return "";
        }
        if (taskTypeFlag.length() == 0 || "Unscheduled".equals(taskTypeFlag)) {
            ret = "Unscheduled Task: No options.";
        } else if ("Scheduled".equals(taskTypeFlag)) {
            String plannedDt = TaskUtil.getPrimaryValue(sdiData, "planneddt");
            ret = "Scheduled date: " + plannedDt;
        } else {
            StringBuffer buffer = new StringBuffer();
            this.renderRecurringRule(buffer, scheduleRule);
            ret = buffer.toString();
        }
        return ret;
    }

    public StringBuffer renderRecurringRule(StringBuffer renderStr, String rule) {
        renderStr.append("&nbsp;&nbsp;");
        if (rule.length() > 0) {
            String period = rule.substring(0, 1);
            String[] parts = rule.substring(1).split(";");
            String periodrule = parts[0];
            if (period.equals("T")) {
                renderStr.append("Scheduled to run every: ");
                String[] timeparts = periodrule.split(":");
                renderStr.append(timeparts[0] + " Hours,");
                renderStr.append(timeparts[1] + " Minutes,");
                renderStr.append(timeparts[2] + " Seconds");
                renderStr.append(", &nbsp;&nbsp;");
            } else if (period.equals("D")) {
                renderStr.append("Scheduled to run ");
                if (periodrule.substring(0, 1).equals("A")) {
                    renderStr.append("every weekday");
                } else {
                    renderStr.append("once in every ");
                    renderStr.append(periodrule.substring(1) + " day(s)");
                }
                renderStr.append(", &nbsp;&nbsp;");
            } else if (period.equals("W")) {
                renderStr.append("Scheduled to run every ");
                renderStr.append(periodrule.substring(0, 2) + " weeks on the following days of the week: ");
                if ("Y".equals(periodrule.substring(2, 3))) {
                    renderStr.append("Sunday ");
                }
                if ("Y".equals(periodrule.substring(3, 4))) {
                    renderStr.append("Monday ");
                }
                if ("Y".equals(periodrule.substring(4, 5))) {
                    renderStr.append("Tuesday ");
                }
                if ("Y".equals(periodrule.substring(5, 6))) {
                    renderStr.append("Wednesday ");
                }
                if ("Y".equals(periodrule.substring(6, 7))) {
                    renderStr.append("Thursday ");
                }
                if ("Y".equals(periodrule.substring(7, 8))) {
                    renderStr.append("Friday ");
                }
                if ("Y".equals(periodrule.substring(8, 9))) {
                    renderStr.append("Saturday ");
                }
                renderStr.append("&nbsp;&nbsp;");
            } else if (period.equals("M")) {
                if (periodrule.substring(0, 1).equals("T")) {
                    renderStr.append("Scheduled to run on day " + periodrule.substring(1, 3) + " of the month, ");
                    renderStr.append(" every " + periodrule.substring(3) + " months");
                    renderStr.append("");
                } else {
                    renderStr.append("Scheduled to run on ");
                    String temp = this.getCount(periodrule.substring(1, 2));
                    renderStr.append(temp + " ");
                    String dayofweek = this.getWeekDay(periodrule.substring(2, 3));
                    renderStr.append(dayofweek);
                    renderStr.append(" , every " + periodrule.substring(3) + " months");
                    renderStr.append("");
                }
            } else if (period.equals("Y")) {
                if (periodrule.substring(0, 1).equals("T")) {
                    renderStr.append("Scheduled to run on day " + periodrule.substring(1, 3));
                    renderStr.append(" of " + this.getMonth(periodrule.substring(3, 5)) + ", every year");
                    renderStr.append("");
                } else {
                    renderStr.append("Scheduled to run on " + this.getCount(periodrule.substring(1, 2)) + " ");
                    renderStr.append(this.getWeekDay(periodrule.substring(2, 3)));
                    renderStr.append(" of " + this.getMonth(periodrule.substring(3)) + ", every year");
                    renderStr.append("");
                }
            }
            if (parts.length == 4) {
                renderStr.append("Start Date: " + parts[1]);
                renderStr.append(", &nbsp;&nbsp;Start Time: " + parts[2]);
                if (parts[3].substring(0, 1).equals("N")) {
                    renderStr.append(", No end date");
                } else if (parts[3].substring(0, 1).equals("O")) {
                    renderStr.append(", Ends after " + parts[3].substring(1) + " occurance(s).");
                } else if (parts[3].substring(0, 1).equals("D")) {
                    renderStr.append(", End Date: " + parts[3].substring(1));
                }
                renderStr.append("");
            }
            renderStr.append("");
        }
        return renderStr;
    }

    public String getMonth(String numStr) {
        if (numStr.equals("01")) {
            return "January";
        }
        if (numStr.equals("02")) {
            return "February";
        }
        if (numStr.equals("03")) {
            return "March";
        }
        if (numStr.equals("04")) {
            return "April";
        }
        if (numStr.equals("05")) {
            return "May";
        }
        if (numStr.equals("06")) {
            return "June";
        }
        if (numStr.equals("07")) {
            return "July";
        }
        if (numStr.equals("08")) {
            return "August";
        }
        if (numStr.equals("09")) {
            return "September";
        }
        if (numStr.equals("10")) {
            return "October";
        }
        if (numStr.equals("11")) {
            return "November";
        }
        if (numStr.equals("12")) {
            return "December";
        }
        return "ERROR";
    }

    public String getCount(String nth) {
        if (nth.equals("1")) {
            return "first";
        }
        if (nth.equals("2")) {
            return "second";
        }
        if (nth.equals("3")) {
            return "third";
        }
        if (nth.equals("4")) {
            return "fourth";
        }
        if (nth.equals("5")) {
            return "fifth";
        }
        return "ERROR";
    }

    public String getWeekDay(String dayofweek) {
        if (dayofweek.equals("1")) {
            return "Sunday";
        }
        if (dayofweek.equals("2")) {
            return "Monday";
        }
        if (dayofweek.equals("3")) {
            return "Tuesday";
        }
        if (dayofweek.equals("4")) {
            return "Wednesday";
        }
        if (dayofweek.equals("5")) {
            return "Thursday";
        }
        if (dayofweek.equals("6")) {
            return "Friday";
        }
        if (dayofweek.equals("7")) {
            return "Saturday";
        }
        return "ERROR";
    }

    public StringBuffer renderRecurringRuleDiff(StringBuffer renderStr, String rule) {
        String period = rule.substring(0, 1);
        String[] parts = rule.substring(1).split(";");
        String periodrule = parts[0];
        if (period.equals("T")) {
            renderStr.append("Scheduled to run every: ");
            String[] timeparts = periodrule.split(":");
            renderStr.append(timeparts[0] + " Hours,");
            renderStr.append(timeparts[1] + " Minutes,");
            renderStr.append(timeparts[2] + " Seconds");
        } else if (period.equals("D")) {
            renderStr.append("Scheduled to run ");
            if (periodrule.substring(0, 1).equals("A")) {
                renderStr.append("every weekday");
            } else {
                renderStr.append("once in every ");
                renderStr.append(periodrule.substring(1) + " day(s)");
            }
        } else if (period.equals("W")) {
            renderStr.append("Scheduled to run every ");
            renderStr.append(periodrule.substring(0, 2) + " weeks on the following days of the week: ");
            if ("Y".equals(periodrule.substring(2, 3))) {
                renderStr.append("Sunday ");
            }
            if ("Y".equals(periodrule.substring(3, 4))) {
                renderStr.append("Monday ");
            }
            if ("Y".equals(periodrule.substring(4, 5))) {
                renderStr.append("Tuesday ");
            }
            if ("Y".equals(periodrule.substring(5, 6))) {
                renderStr.append("Wednesday ");
            }
            if ("Y".equals(periodrule.substring(6, 7))) {
                renderStr.append("Thursday ");
            }
            if ("Y".equals(periodrule.substring(7, 8))) {
                renderStr.append("Friday ");
            }
            if ("Y".equals(periodrule.substring(8, 9))) {
                renderStr.append("Saturday ");
            }
            renderStr.append("");
        } else if (period.equals("M")) {
            if (periodrule.substring(0, 1).equals("T")) {
                renderStr.append("Scheduled to run on day " + periodrule.substring(1, 3) + " of the month, ");
                renderStr.append(" every " + periodrule.substring(3) + " months");
            } else {
                renderStr.append("Scheduled to run on ");
                String temp = this.getCount(periodrule.substring(1, 2));
                renderStr.append(temp + " ");
                String dayofweek = this.getWeekDay(periodrule.substring(2, 3));
                renderStr.append(dayofweek);
                renderStr.append(" , every " + periodrule.substring(3) + " months");
            }
        } else if (period.equals("Y")) {
            if (periodrule.substring(0, 1).equals("T")) {
                renderStr.append("Scheduled to run on day " + periodrule.substring(1, 3));
                renderStr.append(" of " + this.getMonth(periodrule.substring(3, 5)) + ", every year");
            } else {
                renderStr.append("Scheduled to run on " + this.getCount(periodrule.substring(1, 2)) + " ");
                renderStr.append(this.getWeekDay(periodrule.substring(2, 3)));
                renderStr.append(" of " + this.getMonth(periodrule.substring(3)) + ", every year");
            }
        }
        if (parts.length == 4) {
            renderStr.append("Start Date: " + parts[1]);
            renderStr.append(",&bsp;&nbsp;Start Time: " + parts[2]);
            if (parts[3].substring(0, 1).equals("N")) {
                renderStr.append(",&nbsp;&nbsp;No end date");
            } else if (parts[3].substring(0, 1).equals("O")) {
                renderStr.append(",&nbsp;&nbsp;Ends after " + parts[3].substring(1) + " occurance(s)");
            } else if (parts[3].substring(0, 1).equals("D")) {
                renderStr.append(",&nbsp;&nbsp;End Date: " + parts[3].substring(1));
            }
        }
        return renderStr;
    }

    public String getTaskActionId(SDIData sdidata) {
        return TaskUtil.getPrimaryValue(sdidata, "actionid");
    }

    public String getTaskActionVersionId(SDIData sdidata) {
        String ret = TaskUtil.getPrimaryValue(sdidata, "actionversionid");
        return ret;
    }

    public String getTaskStatus(SDIData sdiData) {
        String colVal = TaskUtil.getPrimaryValue(sdiData, "activeflag");
        if ("Y".equals(colVal)) {
            return "Yes";
        }
        if ("N".equals(colVal)) {
            return "No";
        }
        return "";
    }

    public String getTaskScheduleType(SDIData sdiData) {
        String colValue = TaskUtil.getPrimaryValue(sdiData, "tasktypeflag");
        if ("R".equals(colValue)) {
            return "Recurring";
        }
        if ("P".equals(colValue)) {
            return "Scheduled";
        }
        if ("U".equals(colValue)) {
            return "Unscheduled";
        }
        return "";
    }

    public String getDeleteWhenFinished(SDIData sdiData) {
        String colValue = TaskUtil.getPrimaryValue(sdiData, "deletetaskflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        if ("N".equals(colValue)) {
            return "No";
        }
        return "";
    }

    public String getBackFill(SDIData sdiData) {
        String colValue = TaskUtil.getPrimaryValue(sdiData, "backfillflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        if ("N".equals(colValue)) {
            return "No";
        }
        return "";
    }

    public String getProcessExclusive(SDIData sdiData) {
        String colValue = TaskUtil.getPrimaryValue(sdiData, "processexclusiveflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        if ("N".equals(colValue)) {
            return "No";
        }
        return "";
    }

    public String getTaskType(SDIData sdiData) {
        String colValue = TaskUtil.getPrimaryValue(sdiData, "csuflag");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if ("U".equals(colValue)) {
            return "User";
        }
        if (sdiData.getDataset("primary") != null) {
            return "User";
        }
        return "";
    }

    public DataSet getTaskProperties(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("taskproperty");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Property ID", 0);
        ret.addColumn("Property Value", 0);
        ret.addColumn("Extra Property", 0);
        if (ds != null && ds.getRowCount() > 0) {
            ret.addColumnValues("Property ID", 0, ds.getColumnValues("propertyid", ";"), ";");
            ret.addColumnValues("Property Value", 0, ds.getColumnValues("propertyvalue", ";"), ";");
            String[] extraPropsVals = StringUtil.split(ds.getColumnValues("extrapropertyflag", ";"), ";");
            for (int i = 0; i < extraPropsVals.length; ++i) {
                String val = "";
                val = "Y".equals(extraPropsVals[i]) ? "Yes" : "No";
                ret.setString(i, "Extra Property", val);
            }
        }
        return ret;
    }

    public String getScheduleRule(SDIData sdidata) {
        return TaskUtil.getPrimaryValue(sdidata, "schedulerule");
    }

    public String getScheduleDt(SDIData sdidata) {
        return TaskUtil.getPrimaryValue(sdidata, "scheduledt");
    }
}

