/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.stability;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class StudyPlanConditions
extends BaseElement {
    @Override
    public String getHtml() {
        this.logger.info("Initailizing study plan conditions from properties...");
        SDITagInfo sdiInfo = this.getSDIInfo();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (sdiInfo == null || sdiInfo.getQueryData("study_scheduleplan") == null || sdiInfo.getQueryData("study_scheduleplan").getQuerydata() == null || sdiInfo.getQueryData("primary") == null || sdiInfo.getQueryData("primary").getQuerydata() == null) {
            return tp.translate("Study data not found. StudyPlanConditions grid must be inside an SDI tag and include primary, study_scheduleplan in the request attribute.");
        }
        if (sdiInfo.getQueryData("primary").getQuerydata().size() != 1) {
            return tp.translate("No study found.");
        }
        PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        StringBuffer html = new StringBuffer();
        boolean readonly = this.element.getProperty("readonly") != null && this.element.getProperty("readonly").equals("Y");
        boolean showNextStatus = this.element.getProperty("shownextstatus") != null && this.element.getProperty("shownextstatus").equals("Y");
        boolean approvalmode = this.element.getProperty("approvalmode") != null && this.element.getProperty("approvalmode").equals("Y");
        boolean runningapproval = this.element.getProperty("setrunningapproval") != null && this.element.getProperty("setrunningapproval").equals("Y");
        boolean suspendedapproval = this.element.getProperty("setsuspendedapproval") != null && this.element.getProperty("setsuspendedapproval").equals("Y");
        boolean completeapproval = this.element.getProperty("setcompleteapproval") != null && this.element.getProperty("setcompleteapproval").equals("Y");
        boolean cancelledapproval = this.element.getProperty("setcancelledapproval") != null && this.element.getProperty("setcancelledapproval").equals("Y");
        String studyid = sdiInfo.getString("primary", 0, "studyid");
        String lockedBy = sdiInfo.getDataSet("primary").getValue(0, "__lockedby", "");
        if (lockedBy != null && lockedBy.length() > 0) {
            readonly = true;
            this.logger.debug("Study " + studyid + " is Locked by " + lockedBy + ".");
        }
        String enableChangeControl = "";
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("CMTPolicy", "StudySDC Custom");
            if (policy.size() == 0) {
                policy = this.getConfigurationProcessor().getPolicy("CMTPolicy", "Sapphire Custom");
            }
            enableChangeControl = policy.getProperty("enablechangecontrol");
        }
        catch (SapphireException e) {
            this.logger.error(e.getMessage());
        }
        if ("Y".equals(enableChangeControl)) {
            String code;
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.checkedoutbydepartmentid,c.checkedoutbyuserid,CASE WHEN c.checkedoutbydepartmentid IS NOT NULL AND c.checkedoutbydepartmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid = ? ) THEN '3' ");
            sql.append("WHEN c.checkedoutbydepartmentid IS NOT NULL AND c.checkedoutbydepartmentid NOT IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid = ? ) THEN '4'  ");
            sql.append("WHEN c.checkedoutbyuserid IS NOT NULL AND c.checkedoutbyuserid = ? THEN '1'  ");
            sql.append("WHEN c.checkedoutbyuserid IS NOT NULL AND c.checkedoutbyuserid != ? THEN '2'  ");
            sql.append("ELSE '' END checkinallowed  ");
            sql.append("FROM changelog c WHERE c.changelogstatus = 'Checked Out' and c.linksdcid='StudySDC' and c.linkkeyid1= ?   ");
            String currentUser = pagedata.getProperty("currentuser");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{currentUser, currentUser, currentUser, currentUser, studyid});
            if (ds != null && ds.getRowCount() > 0 && ("2".equals(code = ds.getValue(0, "checkinallowed")) || "4".equals(code))) {
                readonly = true;
            }
        }
        String trackingtypeflag = sdiInfo.getString("primary", 0, "trackingtypeflag") != null ? sdiInfo.getString("primary", 0, "trackingtypeflag") : "C";
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        if (trackingtypeflag.trim().length() == 0) {
            trackingtypeflag = "C";
        }
        DataSet studyDates = qp.getSqlDataSet("SELECT columnid, columndesc FROM\tsyscolumn WHERE\ttableid = 'study' AND datatype = 'D'");
        DataSet orientationRefType = qp.getRefTypeDataSet("StorageOrientation");
        String[] orientationValues = new String[orientationRefType.size() + 1];
        orientationValues[0] = "";
        for (int i = 0; i < orientationRefType.size(); ++i) {
            orientationValues[i + 1] = orientationRefType.getValue(i, "refvalueid");
        }
        DataSet studyPlans = qp.getPreparedSqlDataSet("SELECT study_scheduleplan.scheduleplanid, scheduleplan.scheduleplandesc FROM\tstudy_scheduleplan, scheduleplan WHERE\tstudy_scheduleplan.scheduleplanid = scheduleplan.scheduleplanid AND\tstudy_scheduleplan.studyid = ? ORDER BY study_scheduleplan.usersequence, study_scheduleplan.scheduleplanid", new Object[]{studyid});
        DataSet studyConditions = qp.getPreparedSqlDataSet("SELECT schedulecondition.* FROM\tschedulecondition, study_scheduleplan WHERE\tschedulecondition.scheduleplanid = study_scheduleplan.scheduleplanid AND\tstudy_scheduleplan.studyid = ? ", new Object[]{studyid});
        studyConditions.addColumn("__rowid", 1);
        M18NUtil m18nutil = new M18NUtil(this.connectionInfo);
        for (int i = 0; i < studyConditions.size(); ++i) {
            studyConditions.setObject(i, "__rowid", new BigDecimal(i));
        }
        DataSet studyConditionTrackItems = qp.getPreparedSqlDataSet("SELECT schedulecondition_trackitem.*, trackitem.trackitemdesc, trackitem.qtycurrent, trackitem.qtyunits, trackitem.currentstorageunitid, storageunit.labelpath FROM\tschedulecondition, study_scheduleplan, schedulecondition_trackitem, trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE\tschedulecondition.scheduleplanid = study_scheduleplan.scheduleplanid AND\tschedulecondition.scheduleplanid = schedulecondition_trackitem.scheduleplanid AND\tschedulecondition.scheduleconditionid = schedulecondition_trackitem.scheduleconditionid AND\tschedulecondition_trackitem.trackitemid = trackitem.trackitemid AND\tstudy_scheduleplan.studyid = ? ", new Object[]{studyid});
        DataSet studySamples = qp.getPreparedSqlDataSet("SELECT s.s_sampleid, s.schedulerulelabel, spi.scheduleplanid, spi.scheduleconditionid FROM s_sample s, scheduleplanitem spi where s.studyid = ?  AND  spi.scheduleplanid = s.eventplan AND spi.scheduleplanitemid = s.eventplanitem order by s.s_sampleid ", new Object[]{studyid});
        DataSet studyReferenceItems = null;
        String referenceItemsSdcid = this.element.getProperty("referenceitemsdcid");
        String referenceItemsTitle = "";
        String referenceItemsKeyColid = "";
        String referenceItemsDescColumn = "";
        if (referenceItemsSdcid.length() > 0) {
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            referenceItemsDescColumn = sdcProcessor.getProperty(referenceItemsSdcid, "desccol");
            String referenceItemsTableid = sdcProcessor.getProperty(referenceItemsSdcid, "tableid");
            referenceItemsKeyColid = sdcProcessor.getProperty(referenceItemsSdcid, "keycolid1");
            referenceItemsTitle = StringUtil.initCaps(sdcProcessor.getProperty(referenceItemsSdcid, "plural"));
            if (referenceItemsTitle.length() == 0) {
                referenceItemsTitle = referenceItemsSdcid;
            }
            referenceItemsTitle = tp.translate(referenceItemsTitle);
            studyReferenceItems = qp.getPreparedSqlDataSet("SELECT scri.scheduleplanid, scri.scheduleconditionid, " + referenceItemsTableid + "." + referenceItemsKeyColid + ", " + referenceItemsTableid + "." + referenceItemsDescColumn + " FROM\tscheduleconditionrefitem scri, study_scheduleplan, " + referenceItemsTableid + " WHERE\tscri.refitemsdcid = ? AND \tscri.refitemkeyid1 = " + referenceItemsTableid + "." + referenceItemsKeyColid + " AND \tscri.scheduleplanid = study_scheduleplan.scheduleplanid AND\tstudy_scheduleplan.studyid = ? ORDER BY scri.usersequence, scri.refitemkeyid1", new Object[]{referenceItemsSdcid, studyid});
        }
        StringBuffer addTrackitemRowsCall = new StringBuffer();
        StringBuffer addRefItemRowsCall = new StringBuffer();
        StringBuffer addAdhocWORowsCall = new StringBuffer();
        StringBuffer startDateChange = new StringBuffer("\nfunction startDateChange( startDateField, startDateValue ) {");
        StringBuffer recalculating = new StringBuffer("\nfunction recalculating() {");
        boolean showDataDivs = this.pageContext.getRequest().getParameter("debug") != null && this.pageContext.getRequest().getParameter("debug").equals("Y");
        html.append("<div style=\"display:").append(showDataDivs ? "block" : "none").append("\">");
        html.append(ElementUtil.getDetailHtml(new String[]{sdiInfo.getString("primary", 0, "studyid")}, new String[]{"scheduleplanid", "usersequence"}, "study_scheduleplan", sdiInfo, SDITagUtil.getInstance(this.pageContext), false, true));
        html.append("</div>\n");
        double containerSize = sdiInfo.getBigDecimal("primary", 0, "containertypeid.sizevalue") != null ? sdiInfo.getBigDecimal("primary", 0, "containertypeid.sizevalue").doubleValue() : -1.0;
        String containerSizeUnit = sdiInfo.getValue("primary", 0, "containertypeid.sizeunits");
        boolean partialPull = sdiInfo.getValue("primary", 0, "partialpullflag").equals("Y");
        String ppFlagType = sdiInfo.getValue("primary", 0, "partialpullflag");
        html.append("<input type=\"hidden\" name=\"__propertyhandler_").append(this.elementid).append("\" value=\"com.labvantage.sapphire.pageelements.stability.ScheduleConditionPropertyHandler\"/>");
        html.append("<input type=\"hidden\" name=\"__schedulecondition_rows\" id=\"__schedulecondition_rows\" value=\"").append(studyConditions.size()).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__trackitem_rows\" id=\"__trackitem_rows\" value=\"").append(studyConditionTrackItems.size()).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__refitem_rows\" id=\"__refitem_rows\" value=\"").append(studyReferenceItems == null ? 0 : studyReferenceItems.size()).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__setrunningapproval\" id=\"__setrunningapproval\" value=\"").append(this.element.getProperty("setrunningapproval")).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__setsuspendedapproval\" id=\"__setsuspendedapproval\" value=\"").append(this.element.getProperty("setsuspendedapproval")).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__setcompleteapproval\" id=\"__setcompleteapproval\" value=\"").append(this.element.getProperty("setcompleteapproval")).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__setcancelledapproval\" id=\"__setcancelledapproval\" value=\"").append(this.element.getProperty("setcancelledapproval")).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__studyid\" value=\"").append(sdiInfo.getValue("primary", 0, "studyid")).append("\"/>");
        html.append("<input type=\"hidden\" name=\"__approvalrequiredflag\" id=\"__approvalrequiredflag\" value=\"").append(sdiInfo.getValue("primary", 0, "approvalrequiredflag")).append("\"/>");
        html.append("<input type=\"hidden\" id=\"__containertypeid\" value=\"").append(sdiInfo.getValue("primary", 0, "containertypeid")).append("\"/>");
        HashMap<String, String> planFilter = new HashMap<String, String>();
        HashMap<String, String> trackitemFilter = new HashMap<String, String>();
        HashMap<String, String> referenceItemsFilter = new HashMap<String, String>();
        HashMap<String, String> sampleFilter = new HashMap<String, String>();
        Button edittibutton = new Button(this.pageContext);
        edittibutton.setText(tp.translate("Edit trackitems") + "...");
        edittibutton.setMargin("thin");
        Button addrefitems = new Button(this.pageContext);
        addrefitems.setMargin("thin");
        Button addAdhocWOs = new Button(this.pageContext);
        addAdhocWOs.setMargin("thin");
        Button showDiffButton = new Button(this.pageContext);
        showDiffButton.setImg("WEB-CORE/images/gif/Details.gif");
        showDiffButton.setTip(tp.translate("Show the changes made to the condition since it was suspended"));
        showDiffButton.setMargin("thin");
        Button approvebutton = new Button(this.pageContext);
        approvebutton.setText(tp.translate("Approve"));
        approvebutton.setMargin("thin");
        Button rejectbutton = new Button(this.pageContext);
        rejectbutton.setText(tp.translate("Reject"));
        rejectbutton.setMargin("thin");
        Button trackindivbutton = new Button(this.pageContext);
        trackindivbutton.setText(tp.translate("Individually"));
        trackindivbutton.setMargin("thin");
        Button trackgroupbutton = new Button(this.pageContext);
        trackgroupbutton.setText(tp.translate("Together"));
        trackgroupbutton.setMargin("thin");
        Button tabButton = new Button(this.pageContext);
        Tab tab = new Tab();
        tab.setExpandable("true");
        tab.setExpanded("true");
        tab.setAppearance("modern");
        tab.setCollapsedtext(tp.translate("Click the tab to show plan details"));
        StringBuffer tabContent = new StringBuffer();
        for (int plan = 0; plan < studyPlans.size(); ++plan) {
            String planid = studyPlans.getString(plan, "scheduleplanid");
            String planDesc = studyPlans.getString(plan, "scheduleplandesc");
            ScheduleGrid grid = new ScheduleGrid(this.pageContext);
            try {
                int i;
                String conditionid;
                int i2;
                int i3;
                int i4;
                grid.retrieve(planid);
                grid.setPartialDistribution("X".equalsIgnoreCase(ppFlagType));
                html.append("<div id=\"plantab_").append(planid).append("\">");
                tab.setId(planid);
                tab.setText(tp.translate("Plan:") + " " + (studyPlans.getString(plan, "scheduleplandesc") != null ? studyPlans.getString(plan, "scheduleplandesc") : planid));
                tabContent.delete(0, tabContent.length());
                tabContent.append("<table border=\"0\" style=\"padding:3px; margin:3px; border-collapse:collapse\">");
                tabContent.append("<tr><td>");
                planFilter.put("scheduleplanid", planid);
                trackitemFilter.put("scheduleplanid", planid);
                referenceItemsFilter.put("scheduleplanid", planid);
                sampleFilter.put("scheduleplanid", planid);
                DataSet conditions = studyConditions.getFilteredDataSet(planFilter);
                conditions.sort("usersequence");
                String[] conditionRowStatus = new String[conditions.size()];
                for (i4 = 0; i4 < conditions.size(); ++i4) {
                    conditionRowStatus[i4] = "S";
                }
                tabContent.append("<table border=\"0\" class=\"maintdetail_table\" style=\"padding:3px; margin:0; border-collapse:collapse\">");
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Condition") + "</td>");
                for (i4 = 0; i4 < conditions.size(); ++i4) {
                    tabContent.append("<td class=\"maintdetail_header\" style=\"text-align:center; padding:5px\">").append(SafeHTML.encodeForHTMLAttribute(conditions.getString(i4, "conditionlabel"))).append("</td>");
                }
                tabContent.append("</tr>");
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Current Status") + "</td>");
                boolean statusMatch = true;
                for (i4 = 0; i4 < conditions.size(); ++i4) {
                    String status = conditions.getValue(i4, "conditionstatus", "N");
                    int rowid = conditions.getInt(i4, "__rowid");
                    if (conditions.getValue(i4, "nextconditionstatus").length() == 0 && i4 > 0 && !status.equals(conditions.getValue(i4 - 1, "conditionstatus", "N"))) {
                        statusMatch = false;
                    }
                    tabContent.append("<td align=\"center\" class=\"maintdetail_field\">").append(this.getImage(status)).append("&nbsp;").append(tp.translate(this.getStatusText(status))).append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_conditionstatus\" id=\"schedulecondition").append(rowid).append("_conditionstatus\" value=\"").append(status).append("\"/>");
                    if ("S".equals(status)) {
                        tabContent.append("&nbsp;&nbsp;");
                        showDiffButton.setAction("showConditionDiff( '" + planid + "', " + i4 + " )");
                        tabContent.append(showDiffButton.getHtml());
                    }
                    tabContent.append("</td>");
                }
                tabContent.append("</tr>");
                if (showNextStatus) {
                    int rowid;
                    StringBuffer nextStatusHeader = new StringBuffer();
                    tabContent.append("<tr><td align=\"left\" valign=\"top\" class=\"maintdetail_header\">");
                    StringBuffer nextStatusCols = new StringBuffer();
                    for (i3 = 0; i3 < conditions.size(); ++i3) {
                        rowid = conditions.getInt(i3, "__rowid");
                        if (conditions.getValue(i3, "nextconditionstatus").length() == 0) {
                            String[] nextStatusText;
                            String[] nextStatus;
                            String status = conditions.getValue(i3, "conditionstatus");
                            if (status.equals("R")) {
                                nextStatus = new String[]{"S", "C", "X"};
                                nextStatusText = new String[]{(suspendedapproval ? "Request " : "") + "Suspended", (completeapproval ? "Request " : "") + "Complete", (cancelledapproval ? "Request " : "") + "Cancelled"};
                            } else if (status.equals("S")) {
                                nextStatus = new String[]{"R", "C", "X"};
                                nextStatusText = new String[]{(runningapproval ? "Request " : "") + "Running", (completeapproval ? "Request " : "") + "Complete", (cancelledapproval ? "Request " : "") + "Cancelled"};
                            } else if (status.equals("C")) {
                                nextStatus = new String[]{"R", "X"};
                                nextStatusText = new String[]{(runningapproval ? "Request " : "") + "Running", (cancelledapproval ? "Request " : "") + "Cancelled"};
                            } else if (status.equals("X")) {
                                nextStatus = new String[]{"R"};
                                nextStatusText = new String[]{(runningapproval ? "Request " : "") + "Running"};
                            } else {
                                nextStatus = new String[]{"R", "X"};
                                nextStatusText = new String[]{(runningapproval ? "Request " : "") + "Running", (cancelledapproval ? "Request " : "") + "Cancelled"};
                            }
                            nextStatusCols.append("<td class=\"maintdetail_field\" valign=\"top\">").append(statusMatch ? "<br/>" : "");
                            nextStatusCols.append(this.getCheckBoxHtml("schedulecondition" + rowid + "_nextconditionstatus", nextStatus, nextStatusText, conditions.getValue(i3, "nextconditionstatus"), readonly, rowid, planid, false)).append("<br/></td>");
                            if (!statusMatch || i3 != 0) continue;
                            nextStatusHeader.append(this.getCheckBoxHtml("schedulecondition" + rowid + "_nextconditionstatus_all", nextStatus, nextStatusText, "", readonly, rowid, planid, true));
                            continue;
                        }
                        statusMatch = false;
                        nextStatusCols.append("<td class=\"maintdetail_field\" valign=\"top\"><br/>");
                        nextStatusCols.append(this.getImage(conditions.getValue(i3, "nextconditionstatus"))).append("&nbsp;<i>");
                        HashMap<String, String> token = new HashMap<String, String>();
                        token.put("nextconditionstatus", this.getStatusText(conditions.getValue(i3, "nextconditionstatus")));
                        nextStatusCols.append(tp.translate("Set [nextconditionstatus] Requested", token));
                        nextStatusCols.append("</i>").append("<input type=\"hidden\" name=\"schedulecondition");
                        nextStatusCols.append(rowid).append("_nextconditionstatus").append("\" id=\"schedulecondition");
                        nextStatusCols.append(rowid).append("_nextconditionstatus").append("\" value=\"");
                        nextStatusCols.append(conditions.getValue(i3, "nextconditionstatus"));
                        nextStatusCols.append("\"/><br/><input type=\"text\" readonly style=\"border:none; font-weight: bold\" name=\"schedulecondition");
                        nextStatusCols.append(rowid).append("_nextconditionstatus_approval").append("\" id=\"").append("schedulecondition");
                        nextStatusCols.append(rowid).append("_nextconditionstatus_approval").append("\"/>").append("</td>");
                    }
                    if (statusMatch) {
                        tabContent.append(tp.translate("Change all statuses to") + ":<br/>").append(nextStatusHeader);
                    } else {
                        tabContent.append(tp.translate("Change Status To") + ":");
                    }
                    tabContent.append("</td>").append(nextStatusCols);
                    tabContent.append("</tr>");
                    if (approvalmode) {
                        tabContent.append("<tr><td align=\"right\" class=\"maintdetail_header\">" + tp.translate("Approval") + "</td>");
                        for (i3 = 0; i3 < conditions.size(); ++i3) {
                            rowid = conditions.getInt(i3, "__rowid");
                            if (conditions.getValue(i3, "nextconditionstatus").length() > 0) {
                                approvebutton.setAction("document.getElementById( 'schedulecondition" + rowid + "_rowstatus' ).value = 'U';setChangesMade( true );document.getElementById( 'schedulecondition" + rowid + "_nextconditionstatus_approval' ).value = 'Approve';");
                                rejectbutton.setAction("document.getElementById( 'schedulecondition" + rowid + "_rowstatus' ).value = 'U';setChangesMade( true );document.getElementById( 'schedulecondition" + rowid + "_nextconditionstatus_approval' ).value = 'Reject';");
                                tabContent.append("<td class=\"maintdetail_field\" valign=\"top\"><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>").append(approvebutton.getHtml()).append("</td><td>&nbsp;</td><td>").append(rejectbutton.getHtml()).append("</td></tr></table></td>");
                                continue;
                            }
                            tabContent.append("<td class=\"maintdetail_field\" valign=\"top\">&nbsp;</td>");
                        }
                        tabContent.append("</tr>");
                    }
                    tabContent.append("<tr height=\"20\"><td colspan=\"").append(conditions.size()).append("\"></td>");
                    tabContent.append("</tr>");
                }
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Start Criteria") + "</td>");
                for (i2 = 0; i2 < conditions.size(); ++i2) {
                    int rowid = conditions.getInt(i2, "__rowid");
                    String status = conditions.getValue(i2, "conditionstatus");
                    tabContent.append("<td class=\"maintdetail_field\">").append(this.getDateSelectHtml("schedulecondition" + rowid + "_startcriteria", studyDates, conditions.getValue(i2, "startcriteria"), readonly || conditions.getValue(i2, "nextconditionstatus").length() > 0 || status.length() != 0 && !status.equals("N"), "document.getElementById( 'schedulecondition" + rowid + "_rowstatus' ).value = 'U';setChangesMade( true );if ( document.getElementById( 'pr0_' + this.value ) != null ) startDateChange( this.value, document.getElementById( 'pr0_' + this.value ).value )", this.getTranslationProcessor())).append("</td>");
                    startDateChange.append("if ( document.getElementById( \"schedulecondition").append(rowid).append("_startcriteria\" ).value == startDateField ) {");
                    startDateChange.append("\tdocument.getElementById( \"schedulecondition").append(rowid).append("_startdt\" ).value = startDateValue;");
                    startDateChange.append("\tdocument.getElementById( \"schedulecondition").append(rowid).append("_rowstatus\" ).value = 'U';setChangesMade( true );");
                    startDateChange.append("}");
                }
                tabContent.append("</tr>");
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Start Date") + "</td>");
                for (i2 = 0; i2 < conditions.size(); ++i2) {
                    int rowid = conditions.getInt(i2, "__rowid");
                    boolean editable = !readonly && (conditions.getValue(i2, "conditionstatus").length() == 0 || conditions.getValue(i2, "conditionstatus").equals("N") || conditions.getValue(i2, "conditionstatus").equals("S")) && conditions.getValue(i2, "nextconditionstatus").length() == 0;
                    Calendar startDate = conditions.getCalendar(i2, "startdt");
                    String startDt = startDate == null ? "" : m18nutil.format(startDate);
                    tabContent.append("<td class=\"maintdetail_field\"><input type=\"text\" style=\"width:75%\" name=\"schedulecondition").append(rowid).append("_startdt\" id=\"schedulecondition").append(rowid).append("_startdt\" value=\"").append(startDt).append("\" ").append(editable ? "" : " readonly style=\"border: none\"").append(" onchange=\"document.getElementById( 'schedulecondition").append(rowid).append("_rowstatus' ).value = 'U';setChangesMade( true );\"/>");
                    if (editable) {
                        tabContent.append("<a href=\"/Lookup a date\" onClick=\"sapphire.lookup.date.open( 'schedulecondition").append(rowid).append("_startdt','', '', '0', 'startdt' );return false\" tabindex=\"0\"><img title=\"Lookup a date\" border=\"0\" src=\"WEB-CORE/elements/images/lookup_date.gif\"></a>");
                    }
                    tabContent.append("</td>");
                }
                tabContent.append("</tr>");
                int[] conditionPullContainers = new int[conditions.size()];
                int[] conditionTotalContainers = new int[conditions.size()];
                for (i3 = 0; i3 < conditions.size(); ++i3) {
                    String conditionid2 = conditions.getValue(i3, "scheduleconditionid");
                    StringBuffer inventoryLog = new StringBuffer();
                    conditionPullContainers[i3] = grid.conditionAxis.getContainersForCondition(conditionid2, containerSize, containerSizeUnit, partialPull, false, inventoryLog);
                    conditionTotalContainers[i3] = grid.conditionAxis.getContainersForCondition(conditionid2, containerSize, containerSizeUnit, partialPull, true, inventoryLog);
                }
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Initial Quantity") + "</td>");
                for (i3 = 0; i3 < conditions.size(); ++i3) {
                    int rowid = conditions.getInt(i3, "__rowid");
                    conditionid = conditions.getValue(i3, "scheduleconditionid");
                    String pullQuantity = conditions.getValue(i3, "qtypull", "0");
                    String pullType = conditions.getValue(i3, "qtypulltype");
                    String pullUnits = conditions.getValue(i3, "qtypullunits");
                    boolean autoCalc = !conditions.getValue(i3, "autocalcflag", "N").equals("N");
                    int pullQuantityN = conditions.getInt(i3, "qtypull", 0);
                    if (autoCalc && (!pullType.equals("C") || pullQuantityN != conditionTotalContainers[i3])) {
                        pullType = "C";
                        pullQuantity = "" + conditionTotalContainers[i3];
                        pullUnits = "";
                        conditionRowStatus[i3] = "U";
                    }
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_qtypulltype\" id=\"schedulecondition").append(rowid).append("_qtypulltype\" value=\"").append(pullType).append("\"/>");
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_qtypull\" id=\"schedulecondition").append(rowid).append("_qtypull\" value=\"").append(pullQuantity).append("\"/>");
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_qtypullunits\" id=\"schedulecondition").append(rowid).append("_qtypullunits\" value=\"").append(pullUnits).append("\"/>");
                    tabContent.append("<td class=\"maintdetail_field\">");
                    if (autoCalc) {
                        String id = "pullquantitydisplay_" + planid + ";" + conditionid;
                        tabContent.append("<span id=\"").append(id).append("\" rowid=\"").append(rowid).append("\">").append(conditionTotalContainers[i3]).append(" Containers</span>");
                        recalculating.append("document.getElementById( '").append(id).append("' ).innerHTML = '<font color=red>Recalculating...</font>';");
                    } else if ("C".equals(pullType)) {
                        tabContent.append(pullQuantity).append(" Containers");
                    } else {
                        tabContent.append(pullQuantity).append(" ").append(pullUnits);
                    }
                    tabContent.append("</td>");
                }
                tabContent.append("</tr>");
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Future Quantity Required") + "</td>");
                for (i3 = 0; i3 < conditions.size(); ++i3) {
                    int rowid = conditions.getInt(i3, "__rowid");
                    conditionid = conditions.getValue(i3, "scheduleconditionid");
                    tabContent.append("<td class=\"maintdetail_field\">");
                    String id = "quantityrequireddisplay_" + planid + ";" + conditionid;
                    tabContent.append("<span id=\"").append(id).append("\" rowid=\"").append(rowid).append("\">").append(conditionPullContainers[i3]).append(" Containers</span>");
                    recalculating.append("document.getElementById( '").append(id).append("' ).innerHTML = '<font color=red>Recalculating...</font>';");
                    tabContent.append("&nbsp;&nbsp;&nbsp;<img style=\"cursor: pointer\" src=\"WEB-CORE/images/gif/EditDocument.gif\" onClick=\"showInventoryReport( '").append(planid).append("', '").append(conditionid).append("', 'N')\">");
                    tabContent.append("</td>");
                }
                tabContent.append("</tr>");
                tabContent.append("<tr><td class=\"maintdetail_header\">" + tp.translate("Orientation") + "</td>");
                boolean nonEditableConditionExists = false;
                boolean editableConditionExists = false;
                for (i = 0; i < conditions.size(); ++i) {
                    int rowid = conditions.getInt(i, "__rowid");
                    boolean editable = !readonly && (conditions.getValue(i, "conditionstatus").length() == 0 || conditions.getValue(i, "conditionstatus").equals("N") || conditions.getValue(i, "conditionstatus").equals("S")) && conditions.getValue(i, "nextconditionstatus").length() == 0;
                    tabContent.append("<td class=\"maintdetail_field\">").append(this.getSelectHtml("schedulecondition" + rowid + "_orientation", orientationValues, orientationValues, conditions.getValue(i, "orientation"), !editable, "document.getElementById( 'schedulecondition" + rowid + "_rowstatus' ).value = 'U';setChangesMade( true );")).append("</td>");
                    if (!editable) {
                        nonEditableConditionExists = true;
                        continue;
                    }
                    editableConditionExists = true;
                }
                tabContent.append("</tr>");
                if (!trackingtypeflag.equals("N")) {
                    tabContent.append("<tr><td class=\"maintdetail_header\" valign=\"top\">" + tp.translate("Storage & Quantities") + "</td>");
                    for (i = 0; i < conditions.size(); ++i) {
                        String conditionid3 = conditions.getValue(i, "scheduleconditionid");
                        int rowid = conditions.getInt(i, "__rowid");
                        tabContent.append("<input type=\"hidden\" id=\"schedulecondition").append(rowid).append("_qtypull\" value=\"").append(conditions.getValue(i, "qtypull", "0")).append("\"/><input type=\"hidden\" id=\"schedulecondition").append(rowid).append("_qtypulltype\" value=\"").append(conditions.getValue(i, "qtypulltype")).append("\"/><input type=\"hidden\" id=\"schedulecondition").append(rowid).append("_qtypullunits\" value=\"").append(conditions.getValue(i, "qtypullunits")).append("\"/>");
                        tabContent.append("<td class=\"maintdetail_field\" style=\"padding:4px\" valign=\"top\">");
                        trackitemFilter.put("scheduleconditionid", conditionid3);
                        DataSet trackitems = studyConditionTrackItems.getFilteredDataSet(trackitemFilter);
                        tabContent.append("<table id=\"qtytable_").append(planid).append(";").append(conditionid3).append("\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" class=\"maintdetail_table\" trackitems=\"\" rowid=\"").append(rowid).append("\">");
                        String id = "recommendedstorage_" + planid + ";" + conditionid3;
                        tabContent.append("<tr class=\"maintdetail_header\"><td id=\"").append(id).append("\">" + tp.translate("Recommended Storage") + "</td></tr>");
                        recalculating.append("document.getElementById( '").append(id).append("' ).innerHTML = '<font color=red>" + tp.translate("Recalculating") + "...</font>';");
                        tabContent.append("<tr><td class=\"maintdetail_field\"><input type=\"text\" id=\"schedulecondition").append(rowid).append("_suggestedpullsize\" ").append(readonly ? "readonly " : "").append(" style=\"width: 50px\" value=\"").append(conditionTotalContainers[i]).append("\"/>&nbsp;Containers");
                        tabContent.append("&nbsp;&nbsp;&nbsp;<img style=\"cursor: pointer\" src=\"WEB-CORE/images/gif/EditDocument.gif\" onClick=\"showInventoryReport( '").append(planid).append("', '").append(conditionid3).append("', 'Y')\">");
                        tabContent.append("</td></tr>");
                        trackindivbutton.setAction("addIndividualTrackitems( '" + studyid + "', '" + planid + "', '" + studyPlans.getString(plan, "scheduleplandesc") + "', '" + conditionid3 + "', '" + conditions.getString(i, "conditionlabel") + "' );");
                        trackgroupbutton.setAction("addGroupTrackitems( '" + studyid + "', '" + planid + "', '" + studyPlans.getString(plan, "scheduleplandesc") + "', '" + conditionid3 + "', '" + conditions.getString(i, "conditionlabel") + "' );");
                        tabContent.append("<tr><td class=\"maintdetail_field\">" + tp.translate("Track") + ":<table><tr>");
                        if (readonly) {
                            tabContent.append("<td>&nbsp;</td><td>&nbsp;</td>");
                        } else {
                            if (trackingtypeflag.equals("C") || trackingtypeflag.equals("T")) {
                                tabContent.append("<td>").append(trackgroupbutton.getHtml()).append("</td>");
                            }
                            if (trackingtypeflag.equals("C") || trackingtypeflag.equals("I")) {
                                tabContent.append("<td>").append(trackindivbutton.getHtml()).append("</td>");
                            }
                        }
                        tabContent.append("</tr></table></td></tr>");
                        tabContent.append("</td></tr>");
                        tabContent.append("</table>");
                        tabContent.append("<table id=\"titable_").append(planid).append(";").append(conditionid3).append("\" style=\"display:none\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" class=\"maintdetail_table\" trackitems=\"\" rowid=\"").append(rowid).append("\">");
                        tabContent.append("<tr class=\"maintdetail_header\"><td style=\"border-right: 1px solid #B0C4DE;\" width=\"60%\" >" + tp.translate("Location") + "</td><td width=\"40%\">" + tp.translate("Quantity") + "</td></tr>");
                        if (trackitems.size() > 0) {
                            StringBuffer ti = new StringBuffer();
                            StringBuffer locations = new StringBuffer();
                            StringBuffer qtys = new StringBuffer();
                            StringBuffer units = new StringBuffer();
                            for (int j = 0; j < trackitems.size(); ++j) {
                                ti.append(";").append(trackitems.getValue(j, "trackitemid"));
                                locations.append(";").append(trackitems.getValue(j, "labelpath"));
                                qtys.append(";").append(trackitems.getValue(j, "qtycurrent"));
                                units.append(";").append(trackitems.getValue(j, "qtyunits"));
                            }
                            addTrackitemRowsCall.append("addTrackitemRows( '").append(planid).append("', '").append(conditionid3).append("', '").append(ti.substring(1)).append("', '").append(locations.substring(1).replaceAll("[']", "&quot;")).append("', '").append(qtys.substring(1)).append("', '").append(units.substring(1)).append("', 'S' );");
                        }
                        tabContent.append("<tr id=\"row_").append(planid).append(";").append(conditionid3).append("\" style=\"display:none\">").append("<td style=\"border-right: 1px solid #B0C4DE\" width=\"60%\"><input type=\"hidden\" name=\"ti[__row]_scheduleplanid\" id=\"ti[__row]_scheduleplanid\" value=\"");
                        tabContent.append(planid).append("\"/><input type=\"hidden\" name=\"ti[__row]_scheduleconditionid\" id=\"ti[__row]_scheduleconditionid\" value=\"").append(conditionid3);
                        tabContent.append("\"/>").append("<input type=\"hidden\" name=\"ti[__row]_trackitemid\" id=\"ti[__row]_trackitemid\"/>").append("<input type=\"hidden\" name=\"ti[__row]_rowstatus\" id=\"ti[__row]_rowstatus\"/>").append("<div style=\"width:100%;word-wrap:normal;\" id=\"ti[__row]_location\"></div></td><td width=\"40%\" valign=top><input style=\"width: 100%; border: none\" readonly type=\"text\" id=\"ti[__row]_qty\"/></td></tr>");
                        edittibutton.setAction("editTrackitems( '" + studyid + "', '" + planid + "', '" + studyPlans.getString(plan, "scheduleplandesc") + "', '" + conditionid3 + "', '" + conditions.getString(i, "conditionlabel") + "' );");
                        if (!readonly && conditions.getValue(i, "nextconditionstatus").length() == 0) {
                            tabContent.append("<tr><td>").append(edittibutton.getHtml()).append("</td><td>&nbsp;</td></tr>");
                        }
                        tabContent.append("</table>");
                        tabContent.append("</td>");
                    }
                    tabContent.append("</tr>");
                }
                if (studyReferenceItems != null) {
                    tabContent.append("<tr><td class=\"maintdetail_header\" nowrap valign=\"top\">T<sub>0</sub> ").append(referenceItemsTitle).append("</td>");
                    for (i = 0; i < conditions.size(); ++i) {
                        int rowid = conditions.getInt(i, "__rowid");
                        String conditionid4 = conditions.getValue(i, "scheduleconditionid");
                        tabContent.append("<td class=\"maintdetail_field\" style=\"padding:4px\" valign=\"top\">");
                        referenceItemsFilter.put("scheduleconditionid", conditionid4);
                        DataSet referenceItems = studyReferenceItems.getFilteredDataSet(referenceItemsFilter);
                        tabContent.append("<table id=\"ritable_").append(planid).append(";").append(conditionid4).append("\" style=\"display:block\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" ").append("class=\"maintdetail_table\" referenceitems=\"\" rowid=\"").append(rowid).append("\">");
                        tabContent.append("<tr class=\"maintdetail_header\"><td colspan=\"3\" style=\"border-right: 1px solid #B0C4DE\" >").append(referenceItemsTitle).append("</td></tr>");
                        if (referenceItems.size() > 0) {
                            StringBuffer ri = new StringBuffer();
                            StringBuffer ridesc = new StringBuffer();
                            for (int j = 0; j < referenceItems.size(); ++j) {
                                ri.append(";").append(SafeHTML.encodeForJavaScript(referenceItems.getValue(j, referenceItemsKeyColid)));
                                ridesc.append(";").append(SafeHTML.encodeForJavaScript(referenceItems.getValue(j, referenceItemsDescColumn)));
                            }
                            addRefItemRowsCall.append("addReferenceItemRows( '").append(planid).append("', '").append(conditionid4).append("', '").append(ri.substring(1)).append("', '").append(ridesc.substring(1).replaceAll("[']", "&quot;")).append("', 'S' );");
                        }
                        tabContent.append("<tr id=\"refitemtemplaterow_").append(planid).append(";").append(conditionid4);
                        tabContent.append("\" style=\"display:none\">").append("<td width=\"60%\">").append("<input type=\"hidden\" name=\"ri[__row]_scheduleplanid\" id=\"ri[__row]_scheduleplanid\" value=\"");
                        tabContent.append(planid).append("\"/>").append("<input type=\"hidden\" name=\"ri[__row]_scheduleconditionid\" id=\"ri[__row]_scheduleconditionid\" value=\"");
                        tabContent.append(conditionid4).append("\"/>").append("<input type=\"hidden\" name=\"ri[__row]_rowstatus\" id=\"ri[__row]_rowstatus\"/>").append("<input type=\"hidden\" name=\"ri[__row]_refitemsdcid\" id=\"ri[__row]_refitemsdcid\" value=\"");
                        tabContent.append(referenceItemsSdcid).append("\"/>").append("<input type=\"hidden\" name=\"ri[__row]_usersequence\" id=\"ri[__row]_usersequence\" />").append("<input type=\"text\" name=\"ri[__row]_refitemkeyid1\" id=\"ri[__row]_refitemkeyid1\" style=\"width: 100%; border: none; border-right: 1px solid #B0C4DE\" readonly /></td>").append("<td width=\"40%\">").append("<input style=\"width: 100%; border: none\" readonly type=\"text\" id=\"ri[__row]_refitemdesc\"/>").append("</td>");
                        if (!readonly && conditions.getValue(i, "nextconditionstatus").length() == 0) {
                            tabContent.append("<td><img style=\"cursor: pointer\" onclick=\"deleteRefItem( 'refitemtemplaterow_").append(planid).append(";").append(conditionid4).append("', '[__row]' );\" src=\"WEB-OPAL/pagetypes/stability/images/close.gif\"></td>");
                        }
                        tabContent.append("</tr>");
                        if (!readonly && conditions.getValue(i, "nextconditionstatus").length() == 0) {
                            addrefitems.setText(tp.translate("Add") + " " + referenceItemsTitle + "...");
                            addrefitems.setAction("addReferenceItems( '" + planid + "', '" + conditionid4 + "' );");
                            tabContent.append("<tr><td colspan=\"2\">").append(addrefitems.getHtml()).append("</td><td>&nbsp;</td></tr>");
                        }
                        tabContent.append("</table>");
                        tabContent.append("</td>");
                    }
                    tabContent.append("</tr>");
                }
                if (studySamples.getRowCount() > 0) {
                    tabContent.append("<tr><td class=\"maintdetail_header\" valign=\"top\">" + tp.translate("Adhoc WorkOrders") + "</td>");
                    for (i = 0; i < conditions.size(); ++i) {
                        int rowid = conditions.getInt(i, "__rowid");
                        String conditionid5 = conditions.getValue(i, "scheduleconditionid");
                        String conditionLabel = conditions.getValue(i, "conditionlabel", "");
                        sampleFilter.put("scheduleconditionid", conditionid5);
                        DataSet samples = studySamples.getFilteredDataSet(sampleFilter);
                        String sampleIds = samples.getColumnValues("s_sampleid", ";");
                        tabContent.append("<td class=\"maintdetail_field\" style=\"padding:4px\" valign=\"top\">");
                        tabContent.append("<table>");
                        String status = conditions.getValue(i, "conditionstatus", "");
                        if (!readonly && (status.equals("R") || status.equals("S") || status.equals("C")) && conditions.getValue(i, "nextconditionstatus").length() == 0) {
                            tabContent.append("<tr>");
                            if (samples.getRowCount() > 0) {
                                addAdhocWOs.setText(tp.translate("Add WorkOrder") + "..");
                                addAdhocWOs.setAction("addAdhocWorkOrders('" + studyid + "','" + planid + "', '" + conditionid5 + "','" + planDesc + "','" + conditionLabel + "','" + sampleIds + "')");
                                tabContent.append("<td>").append(addAdhocWOs.getHtml()).append("</td>");
                            } else {
                                tabContent.append("<td>&nbsp;</td>");
                            }
                            tabContent.append("<td>&nbsp;</td>");
                            tabContent.append("</tr>");
                        }
                        tabContent.append("</table>");
                        tabContent.append("</td>");
                    }
                    tabContent.append("</tr>");
                }
                tabContent.append("</table>");
                tabContent.append("</td></tr>");
                tabContent.append("</table>");
                tabContent.append("<table><tr>");
                tabContent.append("<td>");
                tabButton.setText(tp.translate("Review Plan"));
                tabButton.setImg("WEB-CORE/images/gif/Review.gif");
                tabButton.setAction("reviewPlan( '" + planid + "', " + (readonly ? "true" : "false") + ", " + nonEditableConditionExists + ", " + editableConditionExists + " );");
                tabContent.append(tabButton.getHtml());
                tabContent.append("</td>");
                tabContent.append("<td>");
                tabButton.setText(tp.translate("Delete Plan"));
                tabButton.setAction("deletePlanRow( '" + planid + "', " + plan + ", '" + studyPlans.getString(plan, "scheduleplandesc") + "' );");
                if (!readonly) {
                    tabContent.append(tabButton.getHtml());
                }
                tabContent.append("</td>");
                tabContent.append("</tr></table>");
                for (i = 0; i < conditions.size(); ++i) {
                    int rowid = conditions.getInt(i, "__rowid");
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_scheduleplanid\" id=\"schedulecondition").append(rowid).append("_scheduleplanid\" value=\"").append(conditions.getString(i, "scheduleplanid")).append("\"/>");
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_scheduleconditionid\" id=\"schedulecondition").append(rowid).append("_scheduleconditionid\" value=\"").append(conditions.getString(i, "scheduleconditionid")).append("\"/>");
                    tabContent.append("<input type=\"hidden\" name=\"schedulecondition").append(rowid).append("_rowstatus\" id=\"schedulecondition").append(rowid).append("_rowstatus\" value=\"").append(conditionRowStatus[i]).append("\"/>");
                }
                tab.setContent(tabContent.toString());
                html.append(tab.getHtml()).append("<br/>");
                html.append("</div>");
                continue;
            }
            catch (Exception e) {
                this.logger.stackTrace(e);
                html.append("<br><font color=\"red\">Unable to render plan ").append(planid).append("</font><br>");
            }
        }
        html.append("\n<script>");
        html.append("var nexturl = \"rc?command=" + pagedata.getProperty("command") + "&" + pagedata.getProperty("command") + "=" + pagedata.getProperty("page") + "&keyid1=" + HttpUtil.encodeURIComponent(pagedata.getProperty("keyid1")) + "\";\n");
        html.append(startDateChange.append("}").toString());
        html.append(recalculating.append("}").toString());
        this.getJavaScript(html, studyid);
        html.append(addTrackitemRowsCall.toString());
        html.append(addRefItemRowsCall.toString());
        html.append(addAdhocWORowsCall.toString());
        html.append("</script>");
        return html.toString();
    }

    private void getJavaScript(StringBuffer html, String studyid) {
        html.append("function updateRecommendedContainers(){");
        html.append("  recalculating();");
        html.append("  var containersize = document.getElementById( 'pr0_containertypeid.sizevalue' ).value;");
        html.append("\tvar containerunits = document.getElementById( 'pr0_containertypeid.sizeunits' ).value;");
        html.append("\tvar partialpullflag = document.getElementById( 'pr0_partialpullflag' ).value;");
        html.append("  var studyid = '").append(studyid).append("';");
        html.append("  sapphire.ajax.callClass( \"com.labvantage.sapphire.stability.GetStudyContainers\", \"getContainersHandler\", ");
        html.append("{ containersize: containersize, containerunits: containerunits, partialpullflag: partialpullflag, studyid: studyid } );");
        html.append("}");
        html.append("function getContainersHandler( containers ) {");
        html.append("  for ( var i = 0; i < containers.length; i ++ ) {");
        html.append("    setPullQuantityDisplay( containers[i].planid, containers[i].conditionid, containers[i].totalcontainers );");
        html.append("    setQuantityRequiredDisplay( containers[i].planid, containers[i].conditionid, containers[i].containers );");
        html.append("    setRecommendedQty( containers[i].planid, containers[i].conditionid, containers[i].totalcontainers );");
        html.append("  }");
        html.append("}");
        html.append("function setPullQuantityDisplay( planid, conditionid, containers ) {");
        html.append("  var display = document.getElementById( 'pullquantitydisplay_' + planid + ';' + conditionid );");
        html.append("  var displayRowId = sapphire.util.dom.getAttribute(display, 'rowid');");
        html.append("  if ( display != null ) {");
        html.append("    display.innerHTML = containers + ' Containers';");
        html.append("\t  document.getElementById( 'schedulecondition' + displayRowId + '_qtypull' ).value = containers;");
        html.append("\t  document.getElementById( 'schedulecondition' + displayRowId + '_qtypulltype' ).value = 'C';");
        html.append("\t  document.getElementById( 'schedulecondition' + displayRowId + '_rowstatus' ).value = 'U';");
        html.append("  }");
        html.append("}");
        html.append("function setQuantityRequiredDisplay( planid, conditionid, containers ) {");
        html.append("  var display = document.getElementById( 'quantityrequireddisplay_' + planid + ';' + conditionid );");
        html.append("  if ( display != null ) {");
        html.append("    display.innerHTML = containers + ' Containers';");
        html.append("  }");
        html.append("}");
        html.append("function setRecommendedQty( planid, conditionid, containers ) {");
        html.append("  var display = document.getElementById( 'recommendedstorage_' + planid + ';' + conditionid );");
        html.append("  if ( display != null ) display.innerHTML = 'Recommended Storage';");
        html.append("  var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("  if ( table != null ) {");
        html.append("\t  document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_suggestedpullsize' ).value = containers;");
        html.append("  }");
        html.append("}");
        html.append("\nfunction getContainerType() {");
        html.append("return document.getElementById( '__containertypeid' ).value;");
        html.append("}");
        html.append("\nfunction getTrackitems( planid, conditionid ) {");
        html.append("var table = document.getElementById( 'titable_' + planid + ';' + conditionid );");
        html.append("return (table != null && table.style.display == 'block')? sapphire.util.dom.getAttribute(table, 'trackitems').substring( 1 ) : '';");
        html.append("}");
        html.append("\nfunction getQty( planid, conditionid ) {");
        html.append("var qty = '';");
        html.append("var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("if ( table != null ) {");
        html.append("\tvar calc = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_suggestedpullsize' ).value;");
        html.append("\tif ( calc != null && calc.length > 0 ) {");
        html.append("\t\tqty = calc;");
        html.append("\t}");
        html.append("\telse {");
        html.append("\t\tqty = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_qtypull' ).value;");
        html.append("\t}");
        html.append("}");
        html.append("return qty;}");
        html.append("\nfunction getQtyUnits( planid, conditionid ) {");
        html.append("var qtyunits = '';");
        html.append("var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("if ( table != null ) {");
        html.append("\tvar calc = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_suggestedpullsize' ).value;");
        html.append("\tif ( calc != null && calc.length > 0 ) {");
        html.append("\t\tqtyunits = '(Containers)';");
        html.append("\t}");
        html.append("\telse {");
        html.append("\t\tqtyunits = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_qtypullunits' ).value;");
        html.append("\t}");
        html.append("}");
        html.append("return qtyunits;}");
        html.append("function showConditionDiff( planid, itemno ) {\n");
        html.append("\twindow.open( \"rc?command=file&file=WEB-OPAL/pagetypes/stability/diff/conditiondiff.jsp&planid=\" + planid + \"&itemno=\" + itemno, \"conditiondiff\", \"resizable=yes,scrollbars=yes,height=500,width=800\");\n");
        html.append("}");
        html.append("\nfunction getQtyType( planid, conditionid ) {");
        html.append("var qtytype = 'U';");
        html.append("var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("if ( table != null ) {");
        html.append("\tqtytype = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_qtypulltype' ).value;");
        html.append("}");
        html.append("return qtytype;}");
        html.append("\nfunction getRecommendedQty( planid, conditionid ) {");
        html.append("var qty = '';");
        html.append("var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("if ( table != null ) {");
        html.append("\tqty = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_suggestedpullsize' ).value;");
        html.append("}");
        html.append("return qty;}");
        html.append("\nfunction getConditionAttribute( planid, conditionid, attribute ) {");
        html.append("var value = '';");
        html.append("var table = document.getElementById( 'qtytable_' + planid + ';' + conditionid );");
        html.append("if ( table != null ) {");
        html.append("\tif ( document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_' + attribute ) != null ) {");
        html.append("\t\tvalue = document.getElementById( 'schedulecondition' + sapphire.util.dom.getAttribute(table, 'rowid') + '_' + attribute ).value;");
        html.append("\t} else { ");
        html.append("\t\tvalue = 'Unrecognized attribute!';");
        html.append("\t}");
        html.append("}");
        html.append("return value;}");
        html.append("\nfunction deletePlanRow( planid, rowid, plandesc ) {");
        html.append("if ( confirm( \"Delete plan '\" + plandesc + \"'?\" ) ) {");
        html.append("\tsdiDeleteTableRow( 'study_scheduleplan', 'study_scheduleplan', '__study_scheduleplan' + rowid, rowid );");
        html.append("\tdocument.getElementById( 'plantab_' + planid ).style.display = 'none';");
        html.append("\tdeletePlan( planid );");
        html.append("}");
        html.append("}");
        html.append("\nfunction addPlan( planid ) {");
        html.append("sdiAddTableRow( '").append(this.getSDIFormId()).append("', 'study_scheduleplan', 'study_scheduleplan' );");
        html.append("document.getElementById( 'study_scheduleplan' + __currentindex['study_scheduleplan'] + '_scheduleplanid' ).value = planid;");
        html.append("document.getElementById( 'study_scheduleplan' + __currentindex['study_scheduleplan'] + '_usersequence' ).value = __currentindex['study_scheduleplan'];");
        html.append("}");
        html.append("\nfunction postlookup() {}");
        html.append("\nvar __trackitemindex = 0;");
        html.append("\nfunction addTrackitemRows( planid, conditionid, trackitemid, location, qty, qtyunits, rowstatus ) {");
        html.append("if ( arguments.length < 7 ) rowstatus = 'U';");
        html.append("var reg = /\\[__row\\]/g;");
        html.append("var table = document.getElementById( 'titable_' + planid + ';' + conditionid );");
        html.append("var trackitems = trackitemid.split( ';' );");
        html.append("var locations = location.replace( /&quot;/, \"'\").split( ';' );");
        html.append("var qtys = qty.split( ';' );");
        html.append("var units = qtyunits.split( ';' );");
        html.append("for ( var i = table.rows.length - 2; i > 1 ; i-- ) {");
        html.append("\ttable.deleteRow( table.rows[i].rowIndex );");
        html.append("}");
        html.append("if ( trackitemid.length > 0 && trackitems.length == locations.length && locations.length == qtys.length && qtys.length == units.length ) {");
        html.append("\ttable.style.display = 'block';");
        html.append("\tdocument.getElementById( 'qtytable_' + planid + ';' + conditionid ).style.display = 'none';");
        html.append("\tfor ( var i = 0; i < trackitems.length; i++ ) {");
        html.append("\t\tvar clonerow = document.getElementById( 'row_' + planid + ';' + conditionid ).cloneNode( true );");
        html.append("\t\tif ( clonerow != null ) {");
        html.append("\t\t\t__trackitemindex++;");
        html.append("\t\t\tclonerow.style.display = '';");
        html.append("\t\t\tclonerow.id = 'new';");
        html.append("\t\t\tfor (var j = 0; j < clonerow.childNodes.length; j ++ ) {");
        html.append("\t\t\t\tclonerow.childNodes[j].innerHTML = clonerow.childNodes[j].innerHTML.replace( reg, __trackitemindex );");
        html.append("\t\t\t}");
        html.append("\t\t\tif (units[i]==null || units[i].length==0 ) units[i]='Cont.';");
        html.append("\t\t\ttable.childNodes[0].insertBefore( clonerow, table.rows[table.rows.length-1] );");
        html.append("\t\t\tdocument.getElementById( 'ti' + __trackitemindex + '_rowstatus' ).value = rowstatus;");
        html.append("\t\t\tdocument.getElementById( 'ti' + __trackitemindex + '_trackitemid' ).value = trackitems[i];");
        html.append("\t\t\tvar location =  locations[i];");
        html.append("\t\t\tvar locationDiv =  document.getElementById( 'ti' + __trackitemindex + '_location' );");
        html.append("\t\t\tlocationDiv.innerText = location;");
        html.append("\t\t\tdocument.getElementById( 'ti' + __trackitemindex + '_qty' ).value = qtys[i] + ' ' + units[i];");
        html.append("\t\t\tdocument.getElementById( '__trackitem_rows' ).value = __trackitemindex + 1;");
        html.append("          var tempTrackItems = sapphire.util.dom.getAttribute(table, 'trackitems'); tempTrackItems += ';' + trackitems[i];");
        html.append("\t\t\tsapphire.util.dom.setAttribute( table, 'trackitems', tempTrackItems );");
        html.append("\t\t}");
        html.append("\t}");
        html.append("}");
        html.append("else{");
        html.append("\tsapphire.util.dom.setAttribute( table, 'trackitems', '' );");
        html.append(" table.style.display = 'none';");
        html.append("\tdocument.getElementById( 'qtytable_' + planid + ';' + conditionid ).style.display = 'block';");
        html.append("}");
        html.append("}");
        html.append("\nvar __refitemindex = 0;");
        html.append("\nfunction addReferenceItemRows( planid, conditionid, refitemid, refitemdesc, rowstatus ) {");
        html.append("\tif ( arguments.length < 5 ) rowstatus = 'I';");
        html.append("\tvar reg = /\\[__row\\]/g;");
        html.append("\tvar table = document.getElementById( 'ritable_' + planid + ';' + conditionid );");
        html.append("\tvar refitems = refitemid.split( ';' );");
        html.append("\tvar refitemdescs = refitemdesc.replaceAll( \"&quot;\", \"'\").split( ';' );");
        html.append("\tif ( refitemid.length > 0 && refitems.length == refitemdescs.length ) {");
        html.append("\t\ttable.style.display = 'block';");
        html.append("\t\tfor ( var i = 0; i < refitems.length; i++ ) {");
        html.append("\t\t\tvar clonerow = document.getElementById( 'refitemtemplaterow_' + planid + ';' + conditionid ).cloneNode( true );");
        html.append("\t\t\tif ( clonerow != null ) {");
        html.append("\t\t\t\t__refitemindex++;");
        html.append("\t\t\t\tclonerow.style.display = '';");
        html.append("\t\t\t\tclonerow.id = clonerow.id + '_' + __refitemindex;");
        html.append("\t\t\t\tfor (var j = 0; j < clonerow.childNodes.length; j ++ ) {");
        html.append("\t\t\t\t\tclonerow.childNodes[j].innerHTML = clonerow.childNodes[j].innerHTML.replace( reg, __refitemindex );");
        html.append("\t\t\t\t}");
        html.append("\t\t\t\ttable.childNodes[0].insertBefore( clonerow, table.rows[table.rows.length-1] );");
        html.append("\t\t\t\tdocument.getElementById( 'ri' + __refitemindex + '_rowstatus' ).value = rowstatus;");
        html.append("\t\t\t\tdocument.getElementById( 'ri' + __refitemindex + '_refitemkeyid1' ).value = refitems[i];");
        html.append("\t\t\t\tdocument.getElementById( 'ri' + __refitemindex + '_usersequence' ).value = __refitemindex;");
        html.append("\t\t\t\tdocument.getElementById( 'ri' + __refitemindex + '_refitemdesc' ).value = refitemdescs[i];");
        html.append("\t\t\t\tdocument.getElementById( '__refitem_rows' ).value = __refitemindex + 1;");
        html.append("\t\t\t\ttable.refitems += ';' + refitems[i];");
        html.append("\t\t\t}");
        html.append("\t\t}");
        html.append("\t}");
        html.append("}");
        html.append("\nfunction deleteRefItem( rowid, index ) {");
        html.append("\tif ( confirm( \"Are you sure you want to remove this item?\" ) ) {");
        html.append("\t\tsetChangesMade( true );");
        html.append("\t\tvar element = document.getElementById( 'ri' + index + '_rowstatus' );");
        html.append("\t\tif ( element != null ) element.value = 'D';");
        html.append("\t\tvar rowelement = document.getElementById( rowid + '_' + index );");
        html.append("\t\tif ( rowelement != null ) rowelement.style.display='none';");
        html.append("\t}");
        html.append("}");
        html.append("function showInventoryReport( planid, conditionid, includereserve ) {");
        html.append("\tvar url = 'rc?command=page&page=InventoryReport&scheduleplanid=' + planid + '&conditionid=' + conditionid;");
        html.append("  url += '&studyid=").append(studyid).append("';");
        html.append("  url += '&containersize=' + document.getElementById( 'pr0_containertypeid.sizevalue' ).value;");
        html.append("\turl += '&containersizeunit=' + document.getElementById( 'pr0_containertypeid.sizeunits' ).value;");
        html.append("  var partialpullflag = document.getElementById( 'pr0_partialpullflag' );");
        html.append("  if( partialpullflag != null ) {");
        html.append("  url += '&partialpullflag=' + partialpullflag.value;");
        html.append("  }");
        html.append("\turl += '&includereserve=' + includereserve;");
        html.append("window.open(url, 'inventoryreportlookup', 'menubar=no,status=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=620,left=100,top=100');");
        html.append("}");
        html.append("var lastRefItemPlanid;");
        html.append("var lastRefItemConditionid;");
        String t0SampleLookupURL = this.element.getProperty("t0samplelookupurl");
        html.append("\nfunction addReferenceItems( scheduleplanid, conditionid ) {");
        if (t0SampleLookupURL.length() > 0) {
            html.append(" var url = '" + t0SampleLookupURL + "';");
            html.append(" while( url.indexOf('[')>-1) { ");
            html.append("  var x1 = url.indexOf('[');");
            html.append("  var x2 = url.indexOf(']');");
            html.append(" if ( x2 > -1 ) { ");
            html.append("  var col = url.substring( x1 + 1, x2 );");
            html.append("\t var colValue = '';");
            html.append("\t try{ colValue =document.getElementById( 'pr0_' + col).value; }catch(exp){} ");
            html.append("    url =  url.replaceAll('[' + col + ']', colValue);");
            html.append("  }");
            html.append("}");
        } else {
            html.append("\tvar url='rc?command=page&page=LV_T0SampleLookup';");
        }
        html.append("\turl += '&lookupcallback=refItemsCallback';");
        html.append("\tlastRefItemPlanid = scheduleplanid;");
        html.append("\tlastRefItemConditionid = conditionid;");
        html.append("\twindow.open( url, 'refitemlookup', 'resizable=yes,scrollbars=yes,height=500,width=800');\n");
        html.append("");
        html.append("");
        html.append("}");
        html.append("\nfunction refItemsCallback( newitemid ) {");
        html.append("\tsetChangesMade( true );");
        html.append("\taddReferenceItemRows( lastRefItemPlanid, lastRefItemConditionid, newitemid, 'New Item', 'I' );");
        html.append("}");
        html.append("\nfunction addAdhocWorkOrders( studyid, scheduleplanid, conditionid, plandesc, conditionlabel, sampleids ) {");
        html.append("if ( getChangesMade() ) { ");
        html.append("if ( !confirm( top.modificationsLoseMsg ) ) return;");
        html.append("}");
        html.append("\tvar url='rc?command=page&page=LV_AdhocWOMaintPopup';");
        html.append(" var inputs = new Array();");
        html.append("inputs.push({name:'studyid', id:'studyid', value:studyid});");
        html.append("inputs.push({name:'scheduleplanid', id:'scheduleplanid', value:scheduleplanid});");
        html.append("inputs.push({name:'conditionid', id:'conditionid', value:conditionid});");
        html.append("inputs.push({name:'plandesc', id:'plandesc', value:plandesc});");
        html.append("inputs.push({name:'conditionlabel', id:'conditionlabel', value:conditionlabel});");
        html.append("inputs.push({name:'sampleids', id:'sampleids', value:sampleids});");
        html.append("inputs.push({name:'mode', id:'mode', value:'Add'});");
        html.append(" var form = sapphire.ajax.util.createForm(url, 'POST', inputs, document.body);");
        html.append("var win= openWindow(url, 'AdhocWorkOrder', 850, 500, 'scrollbars=auto,resizable=yes');");
        html.append("form.target = 'AdhocWorkOrder';");
        html.append("form.action = url;");
        html.append("form.submit();");
        html.append("");
        html.append("");
        html.append("}");
        html.append("\nfunction nextStatusChange( id, checked, value, rowid, header, planid ) {");
        html.append("\tdocument.getElementById( 'schedulecondition' + rowid + '_rowstatus' ).value = 'U';setChangesMade( true );");
        html.append("\tvar cbxgroup = document.getElementsByName( 'cb_' + id );");
        html.append("\tif ( checked ) {");
        html.append("\t\tdocument.getElementById( id ).value = value;");
        html.append("\t\tfor ( var i = 0; i < cbxgroup.length; i++ ) {");
        html.append("\t\t\tif ( cbxgroup[i].value != value ) cbxgroup[i].checked = false;");
        html.append("\t\t}");
        html.append("\t} else {");
        html.append("\t\tdocument.getElementById( id ).value = '';");
        html.append("\t}");
        html.append("\tif ( header ) {");
        html.append("\t\tvar cbxgroup = document.getElementsByTagName( 'input' );");
        html.append("\t\tfor ( var i = 0; i < cbxgroup.length; i++ ) {");
        html.append("\t\t\tif ( cbxgroup[i].type == 'checkbox' && cbxgroup[i].value == value && sapphire.util.dom.getAttribute(cbxgroup[i],'planid') == planid && cbxgroup[i].name.indexOf( '_all' ) == -1 ) {");
        html.append("\t\t\t\tdocument.getElementById( cbxgroup[i].name + '_' + value ).checked = !checked;");
        html.append("\t\t\t\tdocument.getElementById( cbxgroup[i].name + '_' + value ).click();");
        html.append("\t\t\t}");
        html.append("\t\t}");
        html.append("\t}");
        html.append("}");
    }

    private String getSelectHtml(String name, String[] values, String[] options, String selected, boolean readonly, String onChange) {
        StringBuffer html = new StringBuffer();
        if (readonly) {
            html.append("<input type=\"text\" style=\"width:100%; border:none\" name=\"").append(name).append("\" id=\"").append(name).append("\" readonly value=\"").append(selected).append("\"/>");
        } else {
            html.append("<select style=\"width:100%\" name=\"").append(name).append("\" id=\"").append(name).append("\" ").append(onChange.length() > 0 ? "onchange=\"" + onChange + "\"" : "").append(readonly ? " disabled" : "").append(">");
            for (int i = 0; i < options.length; ++i) {
                html.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(values[i])).append("\"").append(values[i].equals(selected) ? " selected" : "").append(">").append(this.getTranslationProcessor().translate(SafeHTML.encodeForHTML(options[i]))).append("</option>");
            }
            html.append("</select>");
        }
        return html.toString();
    }

    private String getDateSelectHtml(String name, DataSet studyDates, String selected, boolean readonly, String onChange, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        if (readonly) {
            html.append("<input type=\"text\" style=\"width:100%; border:none\" name=\"").append(name).append("\" id=\"").append(name).append("\" readonly value=\"").append(selected != null && selected.length() > 0 ? selected : tp.translate("User Defined")).append("\"/>");
        } else {
            html.append("<select style=\"width:100%\" name=\"").append(name).append("\" id=\"").append(name).append("\" ").append(readonly ? "disabled " : "").append(onChange.length() > 0 ? "onchange=\"" + onChange + "\"" : "").append(">");
            html.append("<option value=\"UserDefined\"").append(selected == null || selected.equals("UserDefined") ? " selected" : "").append(">" + tp.translate("User Defined") + "</option>");
            for (int i = 0; i < studyDates.size(); ++i) {
                String displayValue = studyDates.getString(i, "columndesc") != null ? tp.translate(studyDates.getString(i, "columndesc")) : tp.translate(studyDates.getString(i, "columnid"));
                html.append("<option value=\"").append(studyDates.getString(i, "columnid")).append("\"").append(studyDates.getString(i, "columnid").equals(selected) ? " selected" : "").append(">").append(SafeHTML.encodeForHTML(displayValue)).append("</option>");
            }
            html.append("</select>");
        }
        return html.toString();
    }

    private String getCheckBoxHtml(String name, String[] values, String[] options, String selected, boolean readonly, int rowid, String planid, boolean header) {
        StringBuffer html = new StringBuffer("<input type=\"hidden\" id=\"" + name + "\" name=\"" + name + "\" value=\"" + selected + "\">");
        TranslationProcessor tp = this.getTranslationProcessor();
        for (int i = 0; i < options.length; ++i) {
            String optionText = tp.translate(options[i]);
            html.append(this.getImage(values[i])).append("<input type=\"checkbox\" name=\"cb_").append(name).append("\" id=\"cb_").append(name).append("_").append(values[i]).append("\" value=\"").append(values[i]).append("\" planid=\"").append(planid).append("\" ").append(readonly ? "disabled " : "").append("onclick=\"nextStatusChange( '").append(name).append("', this.checked, '").append(values[i]).append("', ").append(rowid).append(", ").append(header ? "true" : "false").append(", '").append(planid).append("' );\"").append(values[i].equals(selected) ? " selected" : "").append(">").append(optionText).append("<br/>");
        }
        return html.toString();
    }

    private String getImage(String status) {
        String image = status.equals("R") ? "WEB-CORE/images/gif/ActiveStudy.gif" : (status.equals("S") ? "WEB-CORE/images/gif/SuspendedStudy.gif" : (status.equals("C") ? "WEB-CORE/images/gif/CompletedStudy.gif" : (status.equals("X") ? "WEB-CORE/images/gif/CancelledStudy.gif" : "WEB-CORE/images/gif/NotStartedStudy.gif")));
        return "<img style=\"border: none\" src=\"" + image + "\" />";
    }

    private String getStatusText(String status) {
        return status.equals("R") ? "Running" : (status.equals("S") ? "Suspended" : (status.equals("C") ? "Complete" : (status.equals("X") ? "Cancelled" : "Not Started")));
    }
}

