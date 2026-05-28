/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.PlanItemList;
import com.labvantage.sapphire.stability.PlanWorkItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.UnitsUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ScheduleGridUtil
extends BaseClass {
    public static final String containerUnit = "(Containers)";
    public static final String percentUnit = "(% Total)";
    public static final String stabilityTimepointReviewRole = "StabilityTimepointReview";

    public static ScheduleGrid getGrid(PageContext pageContext, String planid, boolean forceNew) throws Exception {
        return ScheduleGridUtil.getGrid(pageContext, planid, forceNew, "");
    }

    public static ScheduleGrid getGrid(PageContext pageContext, String planid, boolean forceNew, String excludedList) throws SapphireException {
        HttpSession session = pageContext.getSession();
        ScheduleGrid grid = (ScheduleGrid)session.getAttribute("ScheduleGrid");
        if (grid != null && !forceNew && grid.planid.equals(planid)) {
            grid.initGrid(pageContext);
        } else if (grid == null || forceNew || grid != null && !grid.planid.equals(planid)) {
            grid = new ScheduleGrid(pageContext);
            if (excludedList != null && excludedList.length() > 0) {
                String[] tasks = StringUtil.split(excludedList, ";");
                for (int i = 0; i < tasks.length; ++i) {
                    grid.taskTypes.setExcluded(tasks[i], true);
                }
            }
            grid.retrieve(planid);
            session.setAttribute("ScheduleGrid", (Object)grid);
        }
        return grid;
    }

    public static ScheduleGrid refreshGrid(PageContext pageContext, String planId) throws SapphireException {
        ScheduleGrid grid = new ScheduleGrid(pageContext);
        grid.retrieve(planId);
        HttpSession session = pageContext.getSession();
        session.setAttribute("ScheduleGrid", (Object)grid);
        return grid;
    }

    public static ScheduleGrid performGridOperation(ScheduleGrid grid, PageContext pageContext) throws Exception {
        return ScheduleGridUtil.performGridOperation(grid, pageContext, "");
    }

    public static ScheduleGrid performGridOperation(ScheduleGrid grid, PageContext pageContext, String excludedList) throws Exception {
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        String dothis = request.getParameter("dothis");
        if (dothis != null) {
            if (dothis.equals("save")) {
                grid.auditReason = request.getParameter("auditreason");
                grid.auditActivity = request.getParameter("auditactivity");
                grid.auditSignedFlag = request.getParameter("auditsignedflag");
                PlanItemList planItems = grid.planItems;
                for (PlanItem planItem : planItems) {
                    String currentRevFlag = planItem.getReviewDispositionFlag();
                    String paramName = planItem.scheduleplanid + planItem.timeruleid + planItem.conditionid;
                    String reviewParam = request.getParameter(paramName);
                    if (reviewParam == null || reviewParam.equalsIgnoreCase(currentRevFlag)) continue;
                    planItem.setReviewDispositionFlag(reviewParam);
                    planItem.setReviewDispositionFlagUpdated(true);
                }
                SchedulerAdminProcessor planProcessor = new SchedulerAdminProcessor(pageContext);
                planProcessor.saveGrid(grid);
                grid = ScheduleGridUtil.getGrid(pageContext, grid.planid, true, excludedList);
            } else if (dothis.equals("editlabel")) {
                String desc = request.getParameter("scheduleplandesc");
                if (desc != null && desc.length() > 0) {
                    grid.label = desc;
                    grid.changesMade();
                }
            } else if (dothis.equals("editlabelandscheduleahead")) {
                String executeahead = request.getParameter("executeahead");
                String executeaheadunits = request.getParameter("executeaheadunits");
                String desc = request.getParameter("scheduleplandesc");
                if (!desc.equals(grid.label)) {
                    grid.label = desc;
                    grid.changesMade();
                }
                if (!executeahead.equals(grid.executeahead) || !executeaheadunits.equals(grid.executeaheadUnit)) {
                    grid.executeahead = executeahead;
                    grid.executeaheadUnit = executeaheadunits;
                    grid.changesMade();
                }
            } else if (dothis.equals("copyaxisitem")) {
                BaseAxis axis = request.getParameter("axis").equals("condition") ? grid.conditionAxis : grid.timeAxis;
                axis.moveItem(Integer.parseInt(request.getParameter("itemno")), Integer.parseInt(request.getParameter("steps")));
                grid.changesMade();
            } else if (dothis.equals("sorttimeaxis")) {
                grid.timeAxis.sort();
                grid.changesMade();
            } else if (dothis.equals("moveaxisitem")) {
                BaseAxis axis = request.getParameter("axis").equals("condition") ? grid.conditionAxis : grid.timeAxis;
                axis.moveItem(Integer.parseInt(request.getParameter("itemno")), Integer.parseInt(request.getParameter("steps")));
                grid.changesMade();
            } else if (dothis.equals("deleteaxisitem")) {
                BaseAxis axis = request.getParameter("axis").equals("condition") ? grid.conditionAxis : grid.timeAxis;
                axis.deleteItem(Integer.parseInt(request.getParameter("itemno")));
                grid.changesMade();
            }
        }
        return grid;
    }

    public static String copyPlan(String connectionid, String oldPlanid, String newplantype) throws Exception {
        ActionProcessor ap = new ActionProcessor(connectionid);
        HashMap<String, String> props = new HashMap<String, String>();
        QueryProcessor qp = new QueryProcessor(connectionid);
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet("select scheduleplandesc, executeahead, executeaheadunits from scheduleplan where scheduleplanid = " + safeSQL.addVar(oldPlanid), safeSQL.getValues());
        String plandesc = ds.size() == 1 ? ds.getValue(0, "scheduleplandesc", "New Plan") : "New Plan";
        String executeahead = "";
        String executeaheadUnits = "";
        if (ds.size() == 1) {
            executeahead = ds.getValue(0, "executeahead");
            executeaheadUnits = ds.getValue(0, "executeaheadunits");
        }
        if (newplantype.equals("")) {
            newplantype = "P";
        }
        SequenceProcessor sqm = new SequenceProcessor(connectionid);
        int sequence = sqm.getSequence("scheduleplan", "type_" + newplantype);
        String keyid1 = newplantype + "_" + Integer.toString(sequence);
        props.put("sdcid", "SchedulePlan");
        props.put("copies", "1");
        props.put("keyid1", keyid1);
        props.put("overrideautokey", "Y");
        props.put("scheduleplandesc", plandesc);
        props.put("scheduleplantypeflag", newplantype);
        props.put("executeahead", executeahead);
        props.put("executeaheadunits", executeaheadUnits);
        ap.processAction("AddSDI", "1", props);
        String newPlanid = (String)props.get("newkeyid1");
        if (newPlanid == null || newPlanid.length() <= 0) {
            throw new SapphireException("Unable to generate new plan.");
        }
        qp.execPreparedUpdate("INSERT INTO scheduleplandefaults ( scheduleplanid, propertytreeid, valuetree, extendnodeid ) SELECT ?, propertytreeid, valuetree, extendnodeid from scheduleplandefaults where scheduleplanid=?", new Object[]{newPlanid, oldPlanid});
        ScheduleGrid newGrid = new ScheduleGrid(connectionid);
        newGrid.retrieve(newPlanid);
        newGrid.conditionAxis.copyAllFromTemplate(oldPlanid);
        SchedulerAdminProcessor planProcessor = new SchedulerAdminProcessor(connectionid);
        planProcessor.saveGrid(newGrid);
        return newPlanid;
    }

    public static int lockSchedulePlan(PageContext pageContext, String schedulePlanid, boolean readOnly) {
        DAMProcessor dam = new DAMProcessor(pageContext);
        StringHolder rsetidHolder = new StringHolder();
        int status = readOnly ? dam.createRSet("SchedulePlan", schedulePlanid, "", "", rsetidHolder) : dam.createLockedRSet("SchedulePlan", schedulePlanid, "", "", rsetidHolder);
        if (status == 1) {
            pageContext.setAttribute("rsetid", (Object)rsetidHolder.value);
        }
        return status;
    }

    public static void savePlanItemWorkItems(ScheduleGrid grid, HashMap props, ArrayList planItems) throws SapphireException {
        TranslationProcessor tp = new TranslationProcessor(grid.getConnectionid());
        HashMap<String, String> containerPlan = new HashMap<String, String>();
        for (PlanItem planItem : planItems) {
            if (planItem.readonly || planItem.hasEvent && (planItem.eventStatus.equals("D") || planItem.eventStatus.equals("E") || planItem.eventStatus.equals("I") || planItem.eventStatus.equals("X"))) continue;
            String planId = planItem.scheduleplanid;
            String containerUnit = "";
            if (containerPlan.containsKey(planId)) {
                containerUnit = (String)containerPlan.get(planId);
            } else {
                containerUnit = ScheduleGridUtil.getContainerUnit(planId, grid.queryProcessor);
                containerPlan.put(planId, containerUnit);
            }
            for (int i = 0; i < grid.workItems.items.size(); ++i) {
                boolean exists;
                String workitemid = grid.workItems.items.getValue(i, "workitemid");
                String workitemversionid = grid.workItems.items.getValue(i, "workitemversionid");
                String usersequence = grid.workItems.items.getValue(i, "usersequence");
                String baseid = workitemid + "__" + planItem.planItemid + "__";
                boolean bl = exists = props.get(baseid + "exists") != null;
                if (exists) {
                    PlanWorkItem workitem = new PlanWorkItem();
                    workitem.workitemid = workitemid;
                    workitem.workitemversionid = workitemversionid;
                    workitem.quantity = (String)props.get(baseid + "quantity");
                    workitem.quantityUnit = (String)props.get(baseid + "quantityunit");
                    workitem.quantityType = (String)props.get(baseid + "quantitytype");
                    if (!(workitem.quantity == null || workitem.quantity.length() <= 0 || workitem.quantityUnit == null || workitem.quantityUnit.length() <= 0 || "C".equals(workitem.quantityType) || containerUnit.equals(workitem.quantityUnit) || containerUnit == null || containerUnit.length() <= 0 || containerUnit.equals(workitem.quantityUnit) || UnitsUtil.isUnitCompatible(grid.queryProcessor, workitem.quantityUnit, containerUnit))) {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
                        valueMap.put("workitem.quantityUnit", "<b>'" + workitem.quantityUnit + "'<b>");
                        valueMap.put("workitemid", " <b>'" + workitemid + "'</b>");
                        throw new SapphireException("VALIDATION", "\n" + tp.translate("Unit conversion not defined between Container Unit [containerUnit] and the Unit [workitem.quantityUnit] specified in Test Method [workitemid].", valueMap));
                    }
                    workitem.containerPerRepeatFlag = props.get(baseid + "containerperrepeatflag") != null;
                    workitem.containerPerTestFlag = props.get(baseid + "containerpertestflag") != null;
                    workitem.numRepeats = (String)props.get(baseid + "numrepeats");
                    workitem.destructiveTestFlag = props.get(baseid + "destructivetestflag") != null;
                    workitem.reuseContainerFlag = props.get(baseid + "reusecontainerflag") != null;
                    workitem.contingentFlag = props.get(baseid + "contingentflag") != null;
                    workitem.departmentid = (String)props.get(baseid + "departmentid");
                    workitem.duedtOffset = (String)props.get(baseid + "duedtoffset");
                    workitem.duedtOffsetTimeUnit = (String)props.get(baseid + "duedtoffsettimeunit");
                    workitem.usersequence = usersequence;
                    planItem.setWorkItem(workitem);
                    continue;
                }
                planItem.removeWorkItem(workitemid);
            }
        }
    }

    public static String getContainerUnit(String planId, QueryProcessor qp) {
        String sizeUnit = "";
        DataSet ds = qp.getPreparedSqlDataSet("getstudycontainertype", "select st.containertypeid, c.sizeunits from study st, study_scheduleplan stp, containertype c  where st.studyid = stp.studyid and stp.scheduleplanid = ? and st.containertypeid = c.containertypeid", new Object[]{planId});
        if (ds != null && ds.getRowCount() > 0) {
            sizeUnit = ds.getValue(0, "sizeunits");
        } else {
            ds = qp.getPreparedSqlDataSet("getprotocolprodcontainertype", "select pp.containertypeid, c.sizeunits from protocolproduct pp, protocolprod_scheduleplan ppsp, containertype c  where pp.protocolid = ppsp.protocolid and pp.protocolversionid = ppsp.protocolversionid  and pp.protocolproductid = ppsp.protocolproductid  and ppsp.scheduleplanid = ? and pp.containertypeid = c.containertypeid ", new Object[]{planId});
            if (ds != null && ds.getRowCount() > 0) {
                sizeUnit = ds.getValue(0, "sizeunits");
            }
        }
        return sizeUnit;
    }

    public static DataSet getConditionDefaults(String schedulePlanId, String conditionId, String propertyTreeId, QueryProcessor qp) {
        DataSet ds = qp.getPreparedSqlDataSet("getconditiondefaults", "select  propertytreeid, valuetree  from scheduleconditiondefaults  where scheduleplanid = ? and scheduleconditionid = ? and propertytreeid = ?", new Object[]{schedulePlanId, conditionId, propertyTreeId}, true);
        return ds;
    }

    public static DataSet getPlanDefaults(String schedulePlanId, String propertyTreeId, QueryProcessor qp) {
        DataSet ds = qp.getPreparedSqlDataSet("getPlandefaults", "select  propertytreeid, valuetree  from scheduleplandefaults  where scheduleplanid = ? and propertytreeid = ?", new Object[]{schedulePlanId, propertyTreeId}, true);
        return ds;
    }

    public static String getAmountUnitFromAncestors(String schedulePlanId, String conditionId, String propertyTreeId, QueryProcessor qp) throws SapphireException {
        DataSet dsConditionDefault = ScheduleGridUtil.getConditionDefaults(schedulePlanId, conditionId, propertyTreeId, qp);
        String amountUnit = "";
        PropertyList pl = new PropertyList();
        if (dsConditionDefault.getRowCount() > 0) {
            String valueTree = dsConditionDefault.getClob(0, "valuetree", "");
            pl.setPropertyList(valueTree);
            PropertyList pullAmount = pl.getPropertyListNotNull("pullamount");
            amountUnit = pullAmount.getProperty("units");
        }
        if (amountUnit.length() == 0) {
            DataSet dsPlanDefault = ScheduleGridUtil.getPlanDefaults(schedulePlanId, propertyTreeId, qp);
            String valueTree = dsPlanDefault.getClob(0, "valuetree", "");
            pl.setPropertyList(valueTree);
            PropertyList pullAmount = pl.getPropertyListNotNull("pullamount");
            amountUnit = pullAmount.getProperty("units");
        }
        return amountUnit;
    }

    public static void generatePlanItemWorkItemGrid(ScheduleGrid grid, JspWriter out, ArrayList planItems, boolean forCondition, String expandedList, QueryProcessor qp) throws IOException {
        int i;
        int r;
        SDIProcessor sdiProcessor = new SDIProcessor(qp.getConnectionid());
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Units");
        sdiRequest.setQueryFrom("units");
        sdiRequest.setQueryOrderBy("unitsid");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet units = sdiData.getDataset("primary");
        TranslationProcessor tp = new TranslationProcessor(grid.getConnectionid());
        DataSet timeunits = qp.getRefTypeDataSet("Grace Period Units");
        StringBuffer timeUnitValues = new StringBuffer();
        if (timeunits.findRow("refvalueid", "Hours") < 0) {
            r = timeunits.addRow();
            timeunits.setString(r, "refvalueid", "Hours");
        }
        if (timeunits.findRow("refvalueid", "Minutes") < 0) {
            r = timeunits.addRow();
            timeunits.setString(r, "refvalueid", "Minutes");
        }
        timeunits.sort("refvalueid");
        timeUnitValues.append("<option value=\"\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>");
        for (int i2 = 0; i2 < timeunits.size(); ++i2) {
            String unitid = timeunits.getString(i2, "refvalueid");
            timeUnitValues.append("<option value=\"" + unitid + "\">" + unitid + "</option>");
        }
        Locale locale = grid.conditionAxis.items.getLocale();
        TimeZone tz = grid.conditionAxis.items.getTimeZone();
        DateFormat dfmt = DateFormat.getDateInstance(2, locale);
        dfmt.setTimeZone(tz);
        out.println("<table style=\"margin-top: 5px\" border=\"1\" cellpadding=\"3\" cellspacing=\"0\">");
        out.println("<tr>");
        out.println("<td valign=\"bottom\" class=\"gridmaint_fieldtitle\">");
        out.println("<img expanded=\"N\" src=\"WEB-CORE/elements/images/plus.gif\" style=\"cursor: pointer\" onClick=\"allRowsClicked( this )\"><br>");
        out.println("</td>");
        StringBuffer fillRight = new StringBuffer();
        StringBuffer[] fillDown = new StringBuffer[planItems.size()];
        for (i = 0; i < planItems.size(); ++i) {
            fillDown[i] = new StringBuffer("function fillDown" + i + "(element){var x;");
            PlanItem planItem = (PlanItem)planItems.get(i);
            boolean readOnly = planItem.readonly;
            String label = forCondition ? grid.timeAxis.getLabel(planItem.timeruleid) : grid.conditionAxis.getLabel(planItem.conditionid);
            out.println("<td align=\"center\" class=\"gridmaint_fieldtitle\">");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\">");
            out.println("<tr>");
            String chkBoxHtml = readOnly ? "&nbsp;" : "<input type=\"checkbox\" onclick=\"fillDown" + i + "(this)\">";
            out.println("<td style=\"width:10%\" align=\"left\">" + chkBoxHtml + "</td>");
            out.println("<td style=\"width: 90%\" align=\"center\">" + label + "&nbsp;&nbsp;&nbsp;</td>");
            out.println("</tr>");
            out.println("<tr><td align=\"center\" colspan=\"2\">" + planItem.propertyTreeid + "</td></tr>");
            out.println("</table>");
            out.println("</td>");
        }
        out.println("</tr>");
        for (i = 0; i < grid.workItems.items.size(); ++i) {
            fillRight.append("function fillRight" + i + "(element){var x;");
            String workitemid = grid.workItems.items.getValue(i, "workitemid");
            int findrow = grid.workItems.planWorkItems.findRow("workitemid", workitemid);
            String workitemdesc = findrow > -1 ? grid.workItems.planWorkItems.getValue(findrow, "workitemdesc") : "";
            boolean expanded = expandedList != null && expandedList.indexOf("--" + workitemid + "--") >= 0;
            out.println("<tr>");
            out.println("<td nowrap valign=\"top\" class=\"gridmaint_fieldtitle\" title=\"" + workitemdesc + "\" >");
            out.println("<img expanded=\"" + (expanded ? "Y" : "N") + "\" rowcontrol=\"Y\" id=\"plusminus__" + workitemid + "\" workitemid=\"" + workitemid + "\" src=\"WEB-CORE/elements/images/" + (expanded ? "minus" : "plus") + ".gif\" style=\"cursor: pointer\" onClick=\"rowClicked( this )\">");
            out.println("<input type=\"checkbox\" onclick=\"fillRight" + i + "( this )\">");
            out.println(workitemid);
            out.println("</td>");
            for (int j = 0; j < planItems.size(); ++j) {
                int row;
                PlanItem planItem = (PlanItem)planItems.get(j);
                String quantity = grid.workItems.items.getValue(i, "quantity");
                String quantityUnit = grid.workItems.items.getValue(i, "quantityunit");
                String quantityType = grid.workItems.items.getValue(i, "quantitytype");
                if ("C".equals(quantityType)) {
                    quantityUnit = containerUnit;
                }
                boolean containerPerRepeatFlag = grid.workItems.items.getValue(i, "containerperrepeatflag").equals("Y");
                boolean containerPerTestFlag = grid.workItems.items.getValue(i, "containerpertestflag").equals("Y");
                String numRepeats = grid.workItems.items.getValue(i, "numrepeats");
                boolean destructiveTestFlag = grid.workItems.items.getValue(i, "destructivetestflag").equals("Y");
                boolean reuseContainerFlag = grid.workItems.items.getValue(i, "reusecontainerflag").equals("Y");
                boolean contingentFlag = grid.workItems.items.getValue(i, "contingentflag").equals("Y");
                String departmentid = grid.workItems.items.getValue(i, "departmentid");
                String duedtOffset = grid.workItems.items.getValue(i, "duedtoffset");
                String duedtOffsetTimeUnit = grid.workItems.items.getValue(i, "duedtoffsettimeunit");
                boolean readOnly = false;
                if (planItem.readonly || planItem.hasEvent && (planItem.eventStatus.equals("D") || planItem.eventStatus.equals("E") || planItem.eventStatus.equals("X"))) {
                    readOnly = true;
                }
                boolean exists = (row = planItem.findWorkItemRow(workitemid)) >= 0;
                String overrideTitle = "";
                if (exists) {
                    boolean contingentFlag2 = planItem.workitems.getValue(row, "contingentflag").equals("Y");
                    String quantity2 = planItem.workitems.getValue(row, "quantity");
                    String quantityUnit2 = planItem.workitems.getValue(row, "quantityunit");
                    boolean containerPerRepeatFlag2 = planItem.workitems.getValue(row, "containerperrepeatflag").equals("Y");
                    boolean containerPerTestFlag2 = planItem.workitems.getValue(row, "containerpertestflag").equals("Y");
                    String numRepeats2 = planItem.workitems.getValue(row, "numrepeats");
                    boolean destructiveTestFlag2 = planItem.workitems.getValue(row, "destructivetestflag").equals("Y");
                    boolean reuseContainerFlag2 = planItem.workitems.getValue(row, "reusecontainerflag").equals("Y");
                    String departmentid2 = planItem.workitems.getValue(row, "departmentid");
                    String duedtOffset2 = planItem.workitems.getValue(row, "duedtoffset");
                    String duedtOffsetTimeUnit2 = planItem.workitems.getValue(row, "duedtoffsettimeunit");
                    if (!quantity.equals(quantity2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Quantity") + ": " + quantity;
                    }
                    if (!quantityUnit.equals(quantityUnit2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Quantity Unit") + ": " + quantityUnit;
                    }
                    if (containerPerRepeatFlag != containerPerRepeatFlag2) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Container") + "/" + tp.translate("Repeat") + ": " + containerPerRepeatFlag;
                    }
                    if (containerPerTestFlag != containerPerTestFlag2) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Container") + "/" + tp.translate("Test") + ": " + containerPerTestFlag;
                    }
                    if (!numRepeats.equals(numRepeats2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Repeats") + ": " + numRepeats;
                    }
                    if (destructiveTestFlag != destructiveTestFlag2) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Destructive Test") + ": " + destructiveTestFlag;
                    }
                    if (reuseContainerFlag != reuseContainerFlag2) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Reuse Container") + ": " + reuseContainerFlag;
                    }
                    if (!departmentid.equals(departmentid2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Laboratory") + ": " + departmentid;
                    }
                    if (!duedtOffset.equals(duedtOffset2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Due Date Offset") + ": " + duedtOffset;
                    }
                    if (!duedtOffsetTimeUnit.equals(duedtOffsetTimeUnit2)) {
                        overrideTitle = overrideTitle + "\n" + tp.translate("Offset Unit") + ": " + duedtOffsetTimeUnit;
                    }
                    contingentFlag = contingentFlag2;
                    quantity = quantity2;
                    quantityUnit = quantityUnit2;
                    containerPerRepeatFlag = containerPerRepeatFlag2;
                    containerPerTestFlag = containerPerTestFlag2;
                    numRepeats = numRepeats2;
                    destructiveTestFlag = destructiveTestFlag2;
                    reuseContainerFlag = reuseContainerFlag2;
                    departmentid = departmentid2;
                    duedtOffset = duedtOffset2;
                    duedtOffsetTimeUnit = duedtOffsetTimeUnit2;
                }
                String baseid = workitemid + "__" + planItem.planItemid + "__";
                if (readOnly) {
                    out.println("<span id=\"" + baseid + "exists\" checked=\"" + exists + "\"/>");
                    if (exists) {
                        String image = "";
                        String statusText = "";
                        String date = "";
                        if (planItem.eventdt != null) {
                            date = dfmt.format(planItem.eventdt.getTime());
                        }
                        if (planItem.eventStatus != null && planItem.eventStatus.equals("D")) {
                            image = "WEB-OPAL/pagetypes/stability/images/complete.gif";
                            statusText = "<COMPLETE />Completed " + date;
                        } else if (planItem.eventStatus != null && planItem.eventStatus.equals("E")) {
                            image = "WEB-OPAL/pagetypes/stability/images/error.gif";
                            statusText = "<COMPLETE />Error on " + date;
                        } else if (planItem.eventStatus != null && planItem.eventStatus.equals("I")) {
                            image = "WEB-OPAL/pagetypes/stability/images/error.gif";
                            statusText = "<COMPLETE />Acknowledged Error on " + date;
                        } else if (planItem.eventStatus != null && planItem.eventStatus.equals("X")) {
                            image = "WEB-OPAL/pagetypes/stability/images/cancelled.gif";
                            statusText = "<COMPLETE />Cancelled";
                        } else if (planItem.conditionStatus != null && planItem.conditionStatus.equals("R")) {
                            image = "WEB-OPAL/pagetypes/stability/images/running.gif";
                            statusText = "<COMPLETE />Scheduled for " + date;
                        } else if (planItem.conditionStatus != null && planItem.conditionStatus.equals("N")) {
                            image = "WEB-OPAL/pagetypes/stability/images/notstarted.gif";
                            statusText = "<COMPLETE />Not Scheduled";
                        } else if (planItem.conditionStatus != null && planItem.conditionStatus.equals("S")) {
                            image = "WEB-OPAL/pagetypes/stability/images/suspended.gif";
                            statusText = "<COMPLETE /> Suspended";
                        }
                        out.println("<td nowrap class=\"cellReadonly\" valign=\"top\" id=\"" + baseid + "td\">");
                        if (image.length() > 0) {
                            out.println("<img style=\"border: none\" src=\"" + image + "\" />" + statusText + "<br>");
                        } else {
                            out.println("<input display:\"block\" disabled workitemid=\"" + workitemid + "\" baseid=\"" + baseid + "\" id=\"" + baseid + "exists\" name=\"" + baseid + "exists\" type=\"checkbox\" " + (exists ? "checked" : "") + ">");
                        }
                        out.println("<table cellspacing=\"0\" cellpadding=\"0\" workitemid=\"" + workitemid + "\" baseid=\"" + baseid + "\" id=\"" + baseid + "details\" style=\"margin-top: 5px; display: " + (exists && expanded ? "block" : "none") + "\">");
                        out.println("<tr><td title=\"" + tp.translate("Contingent tests will contribute to initial inventory but are not added to pulled samples") + "\">" + tp.translate("Contingent") + "</td><td>" + contingentFlag + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("The amount of material required to execute a single repeat") + "\">" + tp.translate("Quantity") + " </td><td>" + quantity + " " + quantityUnit + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("The number of times to repeat the test") + "\"># " + tp.translate("Repeats") + " </td><td>" + numRepeats + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("Whether this test requires separate containers for each repeat") + "\">" + tp.translate("Container / Repeat") + " </td><td>" + containerPerRepeatFlag + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("Whether this test requires it's own container that cannot be shared with other tests") + "\">" + tp.translate("Container / Test") + " </td><td>" + containerPerTestFlag + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("Whether this test consumes material but the remainder in the container can be used by other tests") + "\">" + tp.translate("Destructive Test") + " </td><td>" + destructiveTestFlag + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("Whether the same container is used for all time-points") + "\">" + tp.translate("Reuse Container") + " </td><td>" + reuseContainerFlag + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("The laboratory responsible for this test") + "\">" + tp.translate("Laboratory") + " </td><td>" + departmentid + "</td></tr>");
                        out.println("<tr><td title=\"" + tp.translate("The duration added to the sample pull date to determine when the test is due") + "\">" + tp.translate("Due Date Offset") + " </td><td>" + duedtOffset + " " + duedtOffsetTimeUnit + "</td></tr>");
                        out.println("</table>");
                        out.println("</td>");
                        continue;
                    }
                    out.println("<td>N/A</td>");
                    continue;
                }
                out.println("<td " + (exists ? (contingentFlag ? "class=\"cellSelectedContingent\"" : "class=\"cellSelected\"") : "") + "valign=\"top\" id=\"" + baseid + "td\">");
                out.println("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"></tr>");
                fillRight.append("x=document.getElementById( '" + baseid + "exists' );x.checked=element.checked;sapphire.events.fireEvent( x, 'onchange');");
                fillDown[j].append("x=document.getElementById( '" + baseid + "exists' );x.checked=element.checked;sapphire.events.fireEvent( x, 'onchange');");
                out.println("<td><input onChange=\"changesMade();existsClicked( this )\" workitemid=\"" + workitemid + "\" baseid=\"" + baseid + "\" id=\"" + baseid + "exists\" name=\"" + baseid + "exists\" type=\"checkbox\" " + (exists ? "checked" : "") + "></td>");
                out.println("<td nowrap title=\"" + tp.translate("Contingent tests will contribute to initial inventory but are not added to pulled samples") + "\" align=\"right\" ><span id=\"" + baseid + "contingentspan\" style=\"width: 100%; text-align: right; margin-left: 10px; visibility: " + (exists ? "visible" : "hidden") + "\">");
                out.println("(" + tp.translate("Contingent") + "<input onClick=\"changesMade();contingentClicked( this )\" baseid=\"" + baseid + "\" id=\"" + baseid + "contingentflag\" name=\"" + baseid + "contingentflag\" type=\"checkbox\" " + (contingentFlag ? "checked" : "") + ">)");
                if (overrideTitle.length() > 0) {
                    out.println("<img src=\"WEB-CORE/images/warning.gif\" title=\"" + tp.translate("Overridden values") + ": \n" + overrideTitle.substring(1) + "\">");
                }
                out.println("</span></td>");
                out.println("</tr></table>");
                out.println("<table cellspacing=\"0\" cellpadding=\"1\" workitemid=\"" + workitemid + "\" baseid=\"" + baseid + "\" id=\"" + baseid + "details\" style=\"margin-top: 5px; display: " + (exists && expanded ? "block" : "none") + "\">");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("The amount of material required to execute a single repeat") + "\" colspan=\"2\">");
                out.println(tp.translate("Quantity") + ": <input onPropertyChange=\"changesMade()\" oninput=\"changesMade()\" id=\"" + baseid + "quantity\" name=\"" + baseid + "quantity\" value=\"" + quantity + "\" style=\"width: 40px\" type=\"text\">");
                out.println("<select onChange=\"changesMade()\" id=\"" + baseid + "quantityunit\" name=\"" + baseid + "quantityunit\" value=\"" + quantityUnit + "\">");
                out.println("<option value=''>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>");
                out.println("<option value='(Containers)' ");
                out.println(quantityUnit.equals(containerUnit) ? "selected" : "");
                out.println(">(Containers)</option>");
                boolean valueExists = containerUnit.equals(quantityUnit);
                String selected = "";
                for (int u = 0; u < units.size(); ++u) {
                    String unitid = units.getString(u, "unitsid", "");
                    if (unitid.equals(quantityUnit)) {
                        selected = "selected";
                        valueExists = true;
                    }
                    out.println("<option value='" + unitid + "' " + selected + " >" + unitid + "</option>");
                    selected = "";
                }
                if (quantityUnit != null && quantityUnit.length() > 0 && !valueExists) {
                    out.println("<option value='" + quantityUnit + "' selected > ?-" + quantityUnit + "-? </option>");
                }
                out.println("</select>");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("The number of times to repeat the test") + "\"># " + tp.translate("Repeats") + ":</td><td><input onPropertyChange=\"changesMade()\" oninput=\"changesMade()\" id=\"" + baseid + "numrepeats\" name=\"" + baseid + "numrepeats\" value=\"" + numRepeats + "\" style=\"width: 50px\" type=\"text\"></td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("Whether this test requires separate containers for each repeat") + "\">");
                out.println(tp.translate("Container") + " / " + tp.translate("Repeat") + ":</td><td><input onClick=\"changesMade()\" id=\"" + baseid + "containerperrepeatflag\" name=\"" + baseid + "containerperrepeatflag\" " + (containerPerRepeatFlag ? "checked " : "") + " type=\"checkbox\">");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("Whether this test requires it's own container that cannot be shared with other tests") + "\">");
                out.println(tp.translate("Container") + " / " + tp.translate("Test") + ":</td><td><input onClick=\"changesMade()\" id=\"" + baseid + "containerpertestflag\" name=\"" + baseid + "containerpertestflag\" " + (containerPerTestFlag ? "checked " : "") + " type=\"checkbox\">");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("Whether this test consumes material but the remainder in the container can be used by other tests") + "\">");
                out.println(tp.translate("Destructive Test") + ":</td><td><input onClick=\"changesMade()\" id=\"" + baseid + "destructivetestflag\" name=\"" + baseid + "destructivetestflag\" " + (destructiveTestFlag ? "checked " : "") + " type=\"checkbox\">");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("Whether the same container is used for all time-points") + "\">");
                out.println(tp.translate("Reuse Container") + ":</td><td><input onClick=\"checkRepeatPerContainerFlag('" + baseid + "', this );changesMade()\" id=\"" + baseid + "reusecontainerflag\" name=\"" + baseid + "reusecontainerflag\" " + (reuseContainerFlag ? "checked " : "") + " type=\"checkbox\">");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("The laboratory responsible for this test") + "\" nowrap colspan=\"2\">");
                out.println(tp.translate("Laboratory") + ": <input onPropertyChange=\"changesMade()\" oninput=\"changesMade()\" onkeydown = 'if( event.keyCode != 46 ) {return false;} else { this.value = \"\"; changesMade();}'  id=\"" + baseid + "departmentid\" name=\"" + baseid + "departmentid\" value=\"" + departmentid + "\" style=\"width: 120px\" type=\"text\">");
                out.println("<a href='#' onclick=\"lookupDepartment( '" + baseid + "departmentid' );\"><img src='WEB-CORE/elements/images/lookup.gif' border=0></a>");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td title=\"" + tp.translate("The duration added to the sample pull date to determine when the test is due") + "\"colspan=\"2\">");
                out.println(tp.translate("Due Date Offset") + ": <input onPropertyChange=\"changesMade()\" oninput=\"changesMade()\" id=\"" + baseid + "duedtoffset\" name=\"" + baseid + "duedtoffset\" value=\"" + duedtOffset + "\" style=\"width: 40px\" type=\"text\">");
                out.println("<select onChange=\"changesMade()\" id=\"" + baseid + "duedtoffsettimeunit\" name=\"" + baseid + "duedtoffsettimeunit\" value=\"" + duedtOffsetTimeUnit + "\">");
                out.println(StringUtil.replaceAll(timeUnitValues.toString(), "value=\"" + duedtOffsetTimeUnit + "\"", "value=\"" + duedtOffsetTimeUnit + "\" selected"));
                out.println("</select>");
                out.println("</td>");
                out.println("</tr>");
                out.println("</table>");
                out.println("</td>");
            }
            out.println("</tr>");
            fillRight.append("}");
        }
        out.println("</table>");
        out.println("<script>");
        for (i = 0; i < planItems.size(); ++i) {
            fillDown[i].append("}");
            out.println((Object)fillDown[i]);
        }
        out.println((Object)fillRight);
        out.println("</script>");
    }

    public static void toggleCurrentWIVersion(DataSet workitems, boolean blankToCurrent) {
        int rows = workitems.getRowCount();
        for (int i = 0; i < rows; ++i) {
            String workitemVersionId = workitems.getString(i, "workitemversionid", "");
            if (blankToCurrent) {
                if (workitemVersionId.length() != 0) continue;
                workitems.setString(i, "workitemversionid", "C");
                continue;
            }
            if (!"C".equals(workitemVersionId)) continue;
            workitems.setString(i, "workitemversionid", "");
        }
    }
}

