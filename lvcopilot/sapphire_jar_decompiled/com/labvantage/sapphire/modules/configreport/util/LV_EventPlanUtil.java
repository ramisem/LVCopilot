/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.LV_EventPlanRO;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyListCollection;

public class LV_EventPlanUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderEventPlanTree(LV_EventPlanRO eventPlanRO, boolean configreport) {
        ConfigReportContent content = new ConfigReportContent("Event Plan", this.getTranslationProcessor());
        DataSet nodes = eventPlanRO.getDataSet("eventplanitem");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("itemtypeflag", "P");
        DataSet eventPlanRow = nodes.getFilteredDataSet(filter);
        if (eventPlanRow == null || eventPlanRow.getRowCount() == 0) {
            content.append("No eventplan item found");
        }
        content.append("<table>");
        content.append("<tr>");
        content.append("<td>");
        if (configreport) {
            content.append("- <img src=\"../images/WEB-CORE/modules/eventmanager/images/eventplan.gif\"/> <A HREF=\"#EVENT_PLAN_DETAILS\">" + eventPlanRow.getString(0, "eventplanid") + "</A>");
        } else {
            content.append("- <img src=\"WEB-CORE/modules/eventmanager/images/eventplan.gif\"/> <A HREF=\"#EVENT_PLAN_DETAILS\">" + eventPlanRow.getString(0, "eventplanid") + "</A>");
        }
        content.append("</td>");
        content.append("</tr>");
        filter.put("itemtypeflag", "E");
        DataSet events = nodes.getFilteredDataSet(filter);
        events.sort("eventplanitemid");
        for (int i = 0; i < events.getRowCount(); ++i) {
            content.append("<tr>");
            content.append("<td>");
            content.append("&nbsp;&nbsp;&nbsp;");
            String anchor = ConfigReportContent.generateSectionAnchor("Event (" + events.getString(i, "eventplanitemdesc") + ")");
            if (configreport) {
                content.append("- <img src=\"../images/WEB-CORE/modules/eventmanager/images/event.gif\"/> <A HREF=\"#" + anchor + "\">" + events.getString(i, "eventplanitemdesc") + "</A>");
            } else {
                content.append("- <img src=\"WEB-CORE/modules/eventmanager/images/event.gif\"/> <A HREF=\"#" + anchor + "\">" + events.getString(i, "eventplanitemdesc") + "</A>");
            }
            content.append("</td>");
            content.append("</tr>");
            HashMap<String, String> filter1 = new HashMap<String, String>();
            filter1.put("itemtypeflag", "F");
            filter1.put("parenteventplanitemid", events.getString(i, "eventplanitemid"));
            DataSet functions = nodes.getFilteredDataSet(filter1);
            for (int f = 0; f < functions.getRowCount(); ++f) {
                content.append("<tr>");
                content.append("<td>");
                content.append("&nbsp;&nbsp;&nbsp;");
                content.append("&nbsp;&nbsp;&nbsp;");
                content.append("&nbsp;&nbsp;&nbsp;");
                String anchor1 = ConfigReportContent.generateSectionAnchor("Function (" + events.getString(i, "eventplanitemdesc") + ")");
                if (configreport) {
                    content.append("<img src=\"../images/WEB-CORE/modules/eventmanager/images/function.gif\"/><A HREF=\"#" + anchor1 + "\">" + functions.getString(f, "eventplanitemdesc") + "</A>");
                } else {
                    content.append("<img src=\"WEB-CORE/modules/eventmanager/images/function.gif\"/><A HREF=\"#" + anchor1 + "\">" + functions.getString(f, "eventplanitemdesc") + "</A>");
                }
                content.append("</td>");
                content.append("</tr>");
            }
        }
        content.append("</table>");
        return content;
    }

    public ConfigReportContent renderEventPlanTreeDiff(LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO, boolean configreport) {
        ConfigReportContent content = new ConfigReportContent("Event Plan", this.getTranslationProcessor());
        DataSet srcnodes = eventPlanRO.getDataSet("eventplanitem");
        DataSet refnodes = refEventPlanRO.getDataSet("eventplanitem");
        if (refnodes == null) {
            refnodes = new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("itemtypeflag", "P");
        DataSet srcEventPlanRow = new DataSet();
        if (srcnodes != null) {
            srcEventPlanRow = srcnodes.getFilteredDataSet(filter);
        }
        DataSet refEventPlanRow = new DataSet();
        if (refnodes != null) {
            refEventPlanRow = refnodes.getFilteredDataSet(filter);
        }
        content.append("<table>");
        content.append("<tr>");
        content.append("<td>");
        if (configreport) {
            content.append("- <img src=\"../images/WEB-CORE/modules/eventmanager/images/eventplan.gif\"/> <A HREF=\"#EVENT_PLAN_DETAILS\">" + srcEventPlanRow.getString(0, "eventplanid") + "</A>");
        } else {
            content.append("- <img src=\"WEB-CORE/modules/eventmanager/images/eventplan.gif\"/> <A HREF=\"#EVENT_PLAN_DETAILS\">" + srcEventPlanRow.getString(0, "eventplanid") + "</A>");
        }
        content.append("</td>");
        content.append("</tr>");
        filter.put("itemtypeflag", "E");
        DataSet srcevents = srcnodes.getFilteredDataSet(filter);
        DataSet refevents = refnodes.getFilteredDataSet(filter);
        srcevents.sort("eventplanitemid");
        refevents.sort("eventplanitemid");
        for (int i = 0; i < srcevents.getRowCount(); ++i) {
            content.append("<tr>");
            content.append("<td>");
            content.append("&nbsp;&nbsp;&nbsp;");
            String anchor = ConfigReportContent.generateSectionAnchor("Event (" + srcevents.getString(i, "eventplanitemdesc") + ")");
            String eventstr = ConfigReportContent.getDiffString(srcevents.getString(i, "eventplanitemdesc"), refevents.getString(i, "eventplanitemdesc"));
            if (configreport) {
                content.append("- <img src=\"../images/WEB-CORE/modules/eventmanager/images/event.gif\"/> <A HREF=\"#" + anchor + "\">" + eventstr + "</A>");
            } else {
                content.append("- <img src=\"WEB-CORE/modules/eventmanager/images/event.gif\"/> <A HREF=\"#" + anchor + "\">" + eventstr + "</A>");
            }
            content.append("</td>");
            content.append("</tr>");
            HashMap<String, String> filter1 = new HashMap<String, String>();
            filter1.put("itemtypeflag", "F");
            filter1.put("parenteventplanitemid", srcevents.getString(i, "eventplanitemid"));
            DataSet srcfunctions = srcnodes.getFilteredDataSet(filter1);
            filter1.put("parenteventplanitemid", refevents.getString(i, "eventplanitemid"));
            DataSet reffunctions = refnodes.getFilteredDataSet(filter1);
            for (int f = 0; f < srcfunctions.getRowCount(); ++f) {
                content.append("<tr>");
                content.append("<td>");
                content.append("&nbsp;&nbsp;&nbsp;");
                content.append("&nbsp;&nbsp;&nbsp;");
                String anchor1 = ConfigReportContent.generateSectionAnchor("Function (" + srcfunctions.getString(i, "eventplanitemdesc") + ")");
                String functionstr = ConfigReportContent.getDiffString(srcfunctions.getString(f, "eventplanitemdesc"), reffunctions.getString(f, "eventplanitemdesc"));
                if (configreport) {
                    content.append("<img src=\"../images/WEB-CORE/modules/eventmanager/images/function.gif\"/><A HREF=\"#" + anchor1 + "\">" + functionstr + "</A>");
                } else {
                    content.append("<img src=\"WEB-CORE/modules/eventmanager/images/function.gif\"/><A HREF=\"#" + anchor1 + "\">" + functionstr + "</A>");
                }
                content.append("</td>");
                content.append("</tr>");
            }
        }
        content.append("</table>");
        return content;
    }

    public ConfigReportContent renderEventPlanInfo(LV_EventPlanRO eventPlanRO) {
        ConfigReportContent content = new ConfigReportContent("Event Plan", this.getTranslationProcessor());
        content.startTable();
        content.startRow();
        content.addRowItem("Event Plan", eventPlanRO.getKeyid1());
        content.addRowItem("Description", eventPlanRO.getDescription());
        content.endRow();
        content.startRow();
        content.addRowItem("Scope", "L".equals(eventPlanRO.getDataSet("primary").getString(0, "scopeflag")) ? "Local" : "Global");
        content.addRowItem("SDC", eventPlanRO.getDataSet("primary").getString(0, "eventplansdcid"));
        content.endRow();
        content.startRow();
        content.addRowItem("Status", this.getStatus(eventPlanRO));
        content.addRowItem("Allow Modifications", this.getAllowModifications(eventPlanRO));
        content.endRow();
        content.endTable();
        return content;
    }

    public ConfigReportContent renderEventPlanInfoDiff(LV_EventPlanRO srcEventPlanRO, LV_EventPlanRO refEventPlanRO) {
        ConfigReportContent content = new ConfigReportContent("Event Plan", this.getTranslationProcessor());
        content.startTable();
        content.startRow();
        content.addDiffRowItem("Event Plan", srcEventPlanRO.getKeyid1(), refEventPlanRO.getKeyid1());
        content.addDiffRowItem("Description", srcEventPlanRO.getDescription(), refEventPlanRO.getDescription(), this.ignoreDiff("eventplandesc"), this.getTranslationProcessor());
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Scope", this.getScope(srcEventPlanRO), this.getScope(refEventPlanRO), this.ignoreDiff("scopeflag"), this.getTranslationProcessor());
        content.addDiffRowItem("SDC", srcEventPlanRO.getDataSet("primary").getString(0, "eventplansdcid"), refEventPlanRO.getDataSet("primary").getString(0, "eventplansdcid"), this.ignoreDiff("eventplansdcid"), this.getTranslationProcessor());
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Status", this.getStatus(srcEventPlanRO), this.getStatus(refEventPlanRO), this.ignoreDiff("statusflag"), this.getTranslationProcessor());
        content.addDiffRowItem("Allow Modifications", this.getAllowModifications(srcEventPlanRO), this.getAllowModifications(refEventPlanRO), this.ignoreDiff("modifyflag"), this.getTranslationProcessor());
        content.endRow();
        content.endTable();
        return content;
    }

    public String getScope(LV_EventPlanRO ro) {
        DataSet ds = ro.getDataSet("primary");
        if (ds != null) {
            String scope = ds.getString(0, "scopeflag", "");
            if ("L".equals(scope)) {
                return "Local";
            }
            if ("G".equals(scope)) {
                return "Global";
            }
            return "";
        }
        return "";
    }

    public String getStatus(LV_EventPlanRO ro) {
        String flag = ro.getDataSet("primary").getString(0, "statusflag", "");
        if ("A".equals(flag)) {
            return "Active";
        }
        if ("I".equals(flag)) {
            return "Inactive";
        }
        return flag;
    }

    public String getAllowModifications(LV_EventPlanRO ro) {
        String allow = ro.getDataSet("primary").getString(0, "modifyflag", "");
        if ("Y".equals(allow)) {
            return "Yes";
        }
        if ("N".equals(allow)) {
            return "No";
        }
        return allow;
    }

    public ConfigReportContent renderEventInfo(int i, DataSet events, LV_EventPlanRO eventPlanRO) {
        return this.renderEventInfo(i, events, eventPlanRO, "");
    }

    public ConfigReportContent renderEventInfo(int i, DataSet events, LV_EventPlanRO eventPlanRO, String status) {
        ConfigReportContent content = new ConfigReportContent("Event Plan", this.getTranslationProcessor());
        DataSet conditions = eventPlanRO.getDataSet("eventplanitemcondition");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("eventplanitemid", events.getString(i, "eventplanitemid"));
        DataSet currConditions = conditions == null ? new DataSet() : conditions.getFilteredDataSet(filter);
        content.startTable();
        content.startRow();
        if (status.equals("New")) {
            content.addNewRowItem("Description", events.getString(i, "eventplanitemdesc"), this.getTranslationProcessor());
            content.addNewRowItem("Event Type", events.getString(i, "eventtypeid"), this.getTranslationProcessor());
        } else {
            content.addRowItem("Description", events.getString(i, "eventplanitemdesc"));
            content.addRowItem("Event Type", events.getString(i, "eventtypeid"));
        }
        content.endRow();
        DataSet conditionsDisplay = new DataSet();
        for (int c = 0; c < currConditions.getRowCount(); ++c) {
            String cond = currConditions.getString(c, "conditionitem");
            cond = cond + " " + currConditions.getString(c, "operator1");
            cond = cond + "  '" + currConditions.getString(c, "value1") + "'";
            if (currConditions.getString(c, "operator2") != null) {
                cond = cond + " " + currConditions.getString(c, "operator2");
                cond = cond + "  '" + currConditions.getString(c, "value2") + "'";
            }
            conditionsDisplay.addRow();
            conditionsDisplay.setString(c, "Condition", cond);
        }
        content.startRow();
        ConfigReportContent cond = new ConfigReportContent("Conditions", this.getTranslationProcessor());
        cond.renderListTable(conditionsDisplay, false, this.getTranslationProcessor(), status, true);
        content.addRowItem("Conditions", cond.toString(), 3);
        content.endRow();
        content.endTable();
        content.startSubHeading("Event Properties", "", "Event Properties");
        content.append(this.renderEventPlanItemProperties(events.getString(i, "eventplanitemid"), eventPlanRO, status).toString());
        ConfigReportContent options = new ConfigReportContent("Event Options", this.getTranslationProcessor());
        options.startTable();
        options.startRow();
        if (status.equals("New")) {
            options.addNewRowItem("Event Firing", this.getFiring(events.getString(i, "eventfireflag")), this.getTranslationProcessor());
        } else {
            options.addRowItem("Event Firing", this.getFiring(events.getString(i, "eventfireflag")));
        }
        options.endRow();
        options.startRow();
        if (status.equals("New")) {
            options.addNewRowItem("Event Logging", this.getLogging(events.getString(i, "loggingflag")), this.getTranslationProcessor());
        } else {
            options.addRowItem("Event Logging", this.getLogging(events.getString(i, "loggingflag")));
        }
        options.endRow();
        options.endTable();
        content.startSubHeading("Event Options", "", "Event Options");
        content.append(options.toString());
        return content;
    }

    public DataSet getConditionsDisplay(DataSet currConditions) {
        DataSet conditionsDisplay = new DataSet();
        for (int c = 0; c < currConditions.getRowCount(); ++c) {
            String cond = currConditions.getString(c, "conditionitem");
            cond = cond + " " + currConditions.getString(c, "operator1");
            cond = cond + "  '" + currConditions.getString(c, "value1") + "'";
            if (currConditions.getString(c, "operator2") != null) {
                cond = cond + " " + currConditions.getString(c, "operator2");
                cond = cond + "  '" + currConditions.getString(c, "value2") + "'";
            }
            conditionsDisplay.addRow();
            conditionsDisplay.setString(c, "Condition", cond);
        }
        return conditionsDisplay;
    }

    public ConfigReportContent renderEventInfoDiff(int i, DataSet events, DataSet refEvents, LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO) {
        ConfigReportContent content = new ConfigReportContent(this.config, "Event Plan");
        DataSet conditions = eventPlanRO.getDataSet("eventplanitemcondition");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("eventplanitemid", events.getString(i, "eventplanitemid"));
        DataSet currConditions = new DataSet();
        if (conditions != null) {
            currConditions = conditions.getFilteredDataSet(filter);
        }
        filter = new HashMap();
        filter.put("eventplanitemdesc", events.getString(i, "eventplanitemdesc"));
        DataSet refEventMatching = refEvents.getFilteredDataSet(filter);
        if (refEventMatching == null || refEventMatching.getRowCount() == 0) {
            content.append(this.renderEventInfo(i, events, eventPlanRO, "New").toString());
        } else {
            HashMap<String, String> filter1 = new HashMap<String, String>();
            filter1.put("eventplanitemid", refEventMatching.getString(0, "eventplanitemid"));
            DataSet refConditions = refEventPlanRO.getDataSet("eventplanitemcondition");
            DataSet currRefConditions = new DataSet();
            if (refConditions != null) {
                currRefConditions = refConditions.getFilteredDataSet(filter1);
            }
            content.startTable();
            content.startRow();
            content.addRowItem("Description", events.getString(i, "eventplanitemdesc"));
            content.addRowItem("Event Type", events.getString(i, "eventtypeid"));
            content.endRow();
            DataSet srcConditionsDisplay = this.getConditionsDisplay(currConditions);
            DataSet refConditionsDisplay = this.getConditionsDisplay(currRefConditions);
            content.startRow();
            ConfigReportContent cond = new ConfigReportContent("conditions", this.getTranslationProcessor());
            cond.renderDiffListTable(srcConditionsDisplay, refConditionsDisplay, new String[]{"condition"}, false, this.getTranslationProcessor(), true);
            content.addRowItem("Conditions", cond.toString(), 3);
            content.endRow();
            content.endTable();
            content.startSubHeading("Event Properties", "", "Event Properties");
            content.append(this.renderEventPlanItemPropertiesDiff(events.getString(i, "eventplanitemid"), refEventMatching.getString(0, "eventplanitemid"), eventPlanRO, refEventPlanRO).toString());
            ConfigReportContent options = new ConfigReportContent("Event Options", this.getTranslationProcessor());
            options.startTable();
            options.startRow();
            options.addRowItem("Event Firing", this.getFiring(events.getString(i, "eventfireflag")));
            options.endRow();
            options.startRow();
            options.addRowItem("Event Logging", this.getLogging(events.getString(i, "loggingflag")));
            options.endRow();
            options.endTable();
            content.startSubHeading("Event Options", "", "Event Options");
            content.append(options.toString());
        }
        return content;
    }

    public String getFiring(String eventFireFlag) {
        if ("A".equals(eventFireFlag)) {
            return "Always";
        }
        if ("O".equals(eventFireFlag)) {
            return "Once";
        }
        if ("S".equals(eventFireFlag)) {
            return "After First";
        }
        if ("D".equals(eventFireFlag)) {
            return "Disable";
        }
        return "";
    }

    public String getMissingMandatoryProperties(String loggingflag) {
        if ("E".equals(loggingflag)) {
            return "Generate error";
        }
        if ("I".equals(loggingflag)) {
            return "Ignore function processing";
        }
        return "";
    }

    private String getTransactionScope(String loggingflag) {
        if ("E".equals(loggingflag)) {
            return "Use Existing";
        }
        if ("N".equals(loggingflag)) {
            return "Create New";
        }
        return "";
    }

    private String getGroupProcessing(String loggingflag) {
        if ("G".equals(loggingflag)) {
            return "Grouped";
        }
        if ("P".equals(loggingflag)) {
            return "Grouped with matching properties";
        }
        if ("I".equals(loggingflag)) {
            return "Individually";
        }
        return "";
    }

    private String getLogging(String loggingflag) {
        if ("N".equals(loggingflag)) {
            return "None";
        }
        if ("S".equals(loggingflag)) {
            return "Summary";
        }
        if ("D".equals(loggingflag)) {
            return "Debug";
        }
        return "";
    }

    private ConfigReportContent renderEvent(int i, DataSet events, LV_EventPlanRO eventPlanRO) {
        ConfigReportContent content = new ConfigReportContent("Event Details", this.getTranslationProcessor());
        content.startSubSection("Event (" + events.getString(i, "eventplanitemdesc") + ")", "");
        ConfigReportContent eventinfo = this.renderEventInfo(i, events, eventPlanRO);
        content.appendSubSection(eventinfo, "Event (" + events.getString(i, "eventplanitemdesc") + ")", this.diffOnly);
        return content;
    }

    private ConfigReportContent renderEventDiff(int i, DataSet events, DataSet refevents, LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO) {
        ConfigReportContent content = new ConfigReportContent("Event Details", this.getTranslationProcessor());
        content.startSubSection("Event (" + events.getString(i, "eventplanitemdesc") + ")", "");
        ConfigReportContent eventinfo = this.renderEventInfoDiff(i, events, refevents, eventPlanRO, refEventPlanRO);
        content.appendSubSection(eventinfo, "Event (" + events.getString(i, "eventplanitemdesc") + ")", this.diffOnly);
        return content;
    }

    public ConfigReportContent renderEventDetails(String applicationRoot, String folder, LV_EventPlanRO eventPlanRO, boolean configreport) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Properties", this.getTranslationProcessor());
        DataSet nodes = eventPlanRO.getDataSet("eventplanitem");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("itemtypeflag", "E");
        DataSet events = nodes.getFilteredDataSet(filter);
        events.sort("eventplanitemid");
        for (int i = 0; i < events.getRowCount(); ++i) {
            content.appendSpecialContent(this.renderEvent(i, events, eventPlanRO), this.diffOnly);
            HashMap<String, String> filter1 = new HashMap<String, String>();
            filter1.put("itemtypeflag", "F");
            filter1.put("parenteventplanitemid", events.getString(i, "eventplanitemid"));
            DataSet functions = nodes.getFilteredDataSet(filter1);
            for (int f = 0; f < functions.getRowCount(); ++f) {
                ConfigReportContent function = new ConfigReportContent("Function", this.getTranslationProcessor());
                function.startSubSection("Function (" + functions.getString(f, "eventplanitemdesc") + ")", "");
                function.renderProcessingScript(applicationRoot, folder, functions.getString(f, "processingscript"), this.getTranslationProcessor(), configreport);
                function.startSubHeading("Function Properties", "");
                function.append(this.renderEventPlanItemProperties(functions.getString(f, "eventplanitemid"), eventPlanRO, "").toString());
                content.append(function.toString());
                ConfigReportContent options = new ConfigReportContent("Function Options", this.getTranslationProcessor());
                options.startTable();
                options.startRow();
                options.addRowItem("Group Processing", this.getGroupProcessing(functions.getString(f, "groupprocessingflag")));
                options.endRow();
                options.startRow();
                options.addRowItem("Missing Mandatory Properties", this.getMissingMandatoryProperties(functions.getString(f, "missingmandatoryflag")));
                options.endRow();
                options.startRow();
                options.addRowItem("Transaction Scope", this.getTransactionScope(functions.getString(f, "transactionflag")));
                options.endRow();
                options.startRow();
                options.addRowItem("Suppress Error", "Y".equals(functions.getString(f, "suppresserrorflag")) ? "Yes" : "No");
                options.endRow();
                options.startRow();
                options.addRowItem("Asynchronous Processing", "Y".equals(functions.getString(f, "asynchronousflag")) ? "Yes" : "No");
                options.endRow();
                options.endTable();
                content.startSubHeading("Function Options", "", "Function Options");
                content.append(options.toString());
            }
        }
        return content;
    }

    private DataSet getFunctions(String eventplanitemid, DataSet nodes) {
        HashMap<String, String> filter1 = new HashMap<String, String>();
        filter1.put("itemtypeflag", "F");
        filter1.put("parenteventplanitemid", eventplanitemid);
        return nodes.getFilteredDataSet(filter1);
    }

    public ConfigReportContent renderEventDetailsDiff(String applicationRoot, String folder, LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO, boolean configreport) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Properties", this.getTranslationProcessor());
        DataSet srcnodes = eventPlanRO.getDataSet("eventplanitem");
        DataSet refnodes = refEventPlanRO.getDataSet("eventplanitem");
        if (refnodes == null) {
            refnodes = new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("itemtypeflag", "E");
        DataSet srcevents = srcnodes.getFilteredDataSet(filter);
        srcevents.sort("eventplanitemid");
        DataSet refevents = refnodes.getFilteredDataSet(filter);
        refevents.sort("eventplanitemid");
        for (int i = 0; i < srcevents.getRowCount(); ++i) {
            content.appendSpecialContent(this.renderEventDiff(i, srcevents, refevents, eventPlanRO, refEventPlanRO), this.diffOnly);
            DataSet srcfunctions = this.getFunctions(srcevents.getString(i, "eventplanitemid"), srcnodes);
            filter = new HashMap();
            filter.put("eventplanitemdesc", srcevents.getString(i, "eventplanitemdesc"));
            DataSet refEventMatching = refevents.getFilteredDataSet(filter);
            DataSet reffunctions = this.getFunctions(refEventMatching.getString(0, "eventplanitemid"), refnodes);
            for (int f = 0; f < srcfunctions.getRowCount(); ++f) {
                ConfigReportContent function = new ConfigReportContent(this.config, "Function");
                function.startSubSection("Function (" + srcfunctions.getString(f, "eventplanitemdesc") + ")", "");
                HashMap<String, String> functionfilter = new HashMap<String, String>();
                functionfilter.put("eventplanitemdesc", srcfunctions.getString(f, "eventplanitemdesc"));
                DataSet refmatchingfunction = reffunctions.getFilteredDataSet(functionfilter);
                if (refmatchingfunction != null && refmatchingfunction.getRowCount() > 0) {
                    String srcprocessingscript = srcfunctions.getString(f, "processingscript", "");
                    String refprocessingscript = refmatchingfunction.getString(0, "processingscript", "");
                    function.renderProcessingScriptDiff(applicationRoot, folder, srcprocessingscript, refprocessingscript, true, this.getTranslationProcessor(), configreport);
                    function.startSubHeading("Function Properties", "");
                    function.append(this.renderEventPlanItemPropertiesDiff(srcfunctions.getString(f, "eventplanitemid"), refmatchingfunction.getString(0, "eventplanitemid"), eventPlanRO, refEventPlanRO).toString());
                    content.appendSpecialContent(function, this.diffOnly);
                    ConfigReportContent options = new ConfigReportContent("Function Options", this.getTranslationProcessor());
                    options.startTable();
                    options.startRow();
                    options.addDiffRowItem("Group Processing", this.getGroupProcessing(srcfunctions.getString(f, "groupprocessingflag")), this.getGroupProcessing(refmatchingfunction.getString(0, "groupprocessingflag")));
                    options.endRow();
                    options.startRow();
                    options.addDiffRowItem("Missing Mandatory Properties", this.getMissingMandatoryProperties(srcfunctions.getString(f, "missingmandatoryflag")), this.getMissingMandatoryProperties(refmatchingfunction.getString(0, "missingmandatoryflag")));
                    options.endRow();
                    options.startRow();
                    options.addDiffRowItem("Transaction Scope", this.getTransactionScope(srcfunctions.getString(f, "transactionflag")), this.getTransactionScope(refmatchingfunction.getString(0, "transactionflag")));
                    options.endRow();
                    options.startRow();
                    options.addDiffRowItem("Suppress Error", "Y".equals(srcfunctions.getString(f, "suppresserrorflag")) ? "Yes" : "No", "Y".equals(refmatchingfunction.getString(0, "suppresserrorflag")) ? "Yes" : "No");
                    options.endRow();
                    options.startRow();
                    options.addDiffRowItem("Asynchronous Processing", "Y".equals(srcfunctions.getString(f, "asynchronousflag")) ? "Yes" : "No", "Y".equals(refmatchingfunction.getString(0, "asynchronousflag")) ? "Yes" : "No");
                    options.endRow();
                    options.endTable();
                    content.startSubHeading("Function Options", "", "Function Options");
                    content.append(options.toString());
                    continue;
                }
                function.renderProcessingScriptDiff(applicationRoot, folder, srcfunctions.getString(f, "processingscript"), "", true, this.getTranslationProcessor(), configreport);
                function.startSubHeading("Function Properties", "");
                function.append(this.renderEventPlanItemProperties(srcfunctions.getString(f, "eventplanitemid"), eventPlanRO, "New").toString());
                content.append(function.toString());
                ConfigReportContent options = new ConfigReportContent("Function Options", this.getTranslationProcessor());
                options.startTable();
                options.startRow();
                options.addNewRowItem("Group Processing", this.getGroupProcessing(srcfunctions.getString(f, "groupprocessingflag")), this.getTranslationProcessor());
                options.endRow();
                options.startRow();
                options.addNewRowItem("Missing Mandatory Properties", this.getMissingMandatoryProperties(srcfunctions.getString(f, "missingmandatoryflag")), this.getTranslationProcessor());
                options.endRow();
                options.startRow();
                options.addNewRowItem("Transaction Scope", this.getTransactionScope(srcfunctions.getString(f, "transactionflag")), this.getTranslationProcessor());
                options.endRow();
                options.startRow();
                options.addNewRowItem("Suppress Error", "Y".equals(srcfunctions.getString(f, "suppresserrorflag")) ? "Yes" : "No", this.getTranslationProcessor());
                options.endRow();
                options.startRow();
                options.addNewRowItem("Asynchronous Processing", "Y".equals(srcfunctions.getString(f, "asynchronousflag")) ? "Yes" : "No", this.getTranslationProcessor());
                options.endRow();
                options.endTable();
                content.startSubHeading("Function Options", "", "Function Options");
                content.append(options.toString());
            }
        }
        if (content.length() == 0) {
            content.append("None");
        }
        return content;
    }

    public ConfigReportContent renderEventPlanProperties(LV_EventPlanRO eventPlanRO) {
        return this.renderEventPlanItemProperties("1", eventPlanRO, "");
    }

    public ConfigReportContent renderEventPlanPropertiesDiff(LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO) {
        return this.renderEventPlanItemPropertiesDiff("1", "1", eventPlanRO, refEventPlanRO);
    }

    public ConfigReportContent renderEventPlanItemProperties(String eventPlanItemId, LV_EventPlanRO eventPlanRO, String status) {
        ConfigReportContent content = new ConfigReportContent("Properties", this.getTranslationProcessor());
        DataSet properties = eventPlanRO.getDataSet("eventplanitemproperty");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("eventplanitemid", eventPlanItemId);
        properties = properties == null ? new DataSet() : properties.getFilteredDataSet(filter);
        DataSet formatted = new DataSet();
        formatted.addColumn("Property", 0);
        formatted.addColumn("EditorStyle", 0);
        formatted.addColumn("Prompt", 0);
        formatted.addColumn("DefaultValue", 0);
        formatted.addColumn("Mandatory", 0);
        formatted.addColumn("ReadOnly", 0);
        formatted.addColumn("Hidden", 0);
        for (int i = 0; i < properties.getRowCount(); ++i) {
            formatted.addRow();
            formatted.setString(i, "Property", properties.getString(i, "propertyid"));
            formatted.setString(i, "EditorStyle", properties.getString(i, "editorstyleid", ""));
            formatted.setString(i, "Prompt", properties.getString(i, "prompttext", ""));
            formatted.setString(i, "DefaultValue", properties.getString(i, "propertyvalue", ""));
            formatted.setString(i, "Mandatory", properties.getString(i, "mandatoryflag", ""));
            formatted.setString(i, "ReadOnly", properties.getString(i, "readonlyflag", ""));
            formatted.setString(i, "Hidden", properties.getString(i, "hiddenflag", ""));
        }
        if (status.length() > 0) {
            content.renderListTable(formatted, true, this.getTranslationProcessor(), status, true);
        } else {
            content.renderListTable(formatted, this.getTranslationProcessor());
        }
        if (content.length() == 0) {
            content.append("&nbsp;&nbsp;&nbsp;None");
        }
        return content;
    }

    private ConfigReportContent renderEventPlanItemPropertiesDiff(String srcEventPlanItemId, String refEventPlanItemId, LV_EventPlanRO eventPlanRO, LV_EventPlanRO refEventPlanRO) {
        ConfigReportContent content = new ConfigReportContent("Properties", this.getTranslationProcessor());
        DataSet srcproperties = eventPlanRO.getDataSet("eventplanitemproperty");
        DataSet refproperties = refEventPlanRO.getDataSet("eventplanitemproperty");
        if (srcproperties == null) {
            srcproperties = new DataSet();
        }
        if (refproperties == null) {
            refproperties = new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("eventplanitemid", srcEventPlanItemId);
        srcproperties = srcproperties.getFilteredDataSet(filter);
        if (srcproperties == null) {
            srcproperties = new DataSet();
        }
        filter.put("eventplanitemid", refEventPlanItemId);
        refproperties = refproperties.getFilteredDataSet(filter);
        if (refproperties == null) {
            refproperties = new DataSet();
        }
        DataSet srcformatted = this.getDisplayProperties(srcproperties);
        DataSet refformatted = this.getDisplayProperties(refproperties);
        String[] keycols = new String[]{"property"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("eventplanitemproperty");
        content.renderDiffListTable(srcformatted, refformatted, keycols, ignoreDiffs, this.getTranslationProcessor());
        if (content.length() == 0) {
            content.append("None");
        }
        return content;
    }

    private DataSet getDisplayProperties(DataSet properties) {
        DataSet formatted = new DataSet();
        formatted.addColumn("Property", 0);
        formatted.addColumn("EditorStyle", 0);
        formatted.addColumn("Prompt", 0);
        formatted.addColumn("DefaultValue", 0);
        formatted.addColumn("Mandatory", 0);
        formatted.addColumn("ReadOnly", 0);
        formatted.addColumn("Hidden", 0);
        for (int i = 0; i < properties.getRowCount(); ++i) {
            formatted.addRow();
            formatted.setString(i, "Property", properties.getString(i, "propertyid"));
            formatted.setString(i, "EditorStyle", properties.getString(i, "editorstyleid", ""));
            formatted.setString(i, "Prompt", properties.getString(i, "prompttext", ""));
            formatted.setString(i, "DefaultValue", properties.getString(i, "propertyvalue", ""));
            formatted.setString(i, "Mandatory", properties.getString(i, "mandatoryflag", ""));
            formatted.setString(i, "ReadOnly", properties.getString(i, "readonlyflag", ""));
            formatted.setString(i, "Hidden", properties.getString(i, "hiddenflag", ""));
        }
        return formatted;
    }
}

