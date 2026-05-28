/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.scheduleplan;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.scheduleplan.PlanItemHelper;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PlanItemManager
extends BaseElement {
    private static final String PROPERTY_HANDLER_CLASS = "com.labvantage.sapphire.pageelements.scheduleplan.PlanItemManagerPropertyHandler";
    private static final String SCHEDULE_PLAN_TITLE_COLUMN_ALIAS = "scheduleplantitlecolumn";
    private static final String SCHEDULE_PLAN_NODE_TITLE_COLUMN_ALIAS = "scheduleplannodetitlecolumn";
    public static final String ROOT_NODE_ID = "all";
    private static final String DEFAULT_VIEW_ONLY = "N";
    private static final String DEFAULT_SHOW_BUTTON = "Y";
    private static final String DEFAULT_SHOW_BUTTON_VIEW_ONLY = "N";

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        PlanItemHelper planItemHelper = null;
        DataSet planItems = null;
        String datasetName = this.elementid + "ds";
        try {
            planItemHelper = new PlanItemHelper(this.getConnectionId(), this.requestContext, this.element, datasetName);
            planItems = planItemHelper.getPlanItems();
        }
        catch (SapphireException e) {
            this.logger.error("Cannot get plan items", e);
            html.append("Cannot get plan items: ").append(e);
        }
        if (planItems != null) {
            PropertyList selectedNodes = planItemHelper.getSelectedNodes(sapphire.util.HttpUtil.decodeURIComponent(this.requestContext.getProperty("_" + this.elementid + "_selectednodes")));
            html.append(JavaScriptAPITag.getJQueryAPI(true, this.browser.isMobile(), this.getJQueryPlugins(), this.pageContext));
            html.append("<script type='text/javascript' language='JavaScript' src='WEB-CORE/elements/scheduler/scripts/planitemmanager.js'></script>");
            html.append("<script type='text/javascript'>");
            html.append("var __unmanagedSDCArray = \"").append(planItemHelper.getUnmanagedSDCs()).append("\".split(\";\");");
            html.append("</script>");
            html.append("<link rel='stylesheet' href='" + HttpUtil.getCSS("WEB-CORE/elements/scheduler/stylesheets/planitemmanager.css", this.pageContext) + "' type='text/css'>");
            html.append("<div id='").append(this.elementid).append("_container'></div>");
            String javaScriptVarName = this.elementid.replaceAll("\\W", "");
            html.append("<input type='hidden' id='__propertyhandler_").append(this.elementid).append("' name='").append("__propertyhandler_").append(this.elementid).append("' value=\"").append(PROPERTY_HANDLER_CLASS).append("\"/>");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_buttonhtml' name='_").append(this.elementid).append("_buttonhtml' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.getButtonsHtml())).append("\">");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_taskcategoryjson' name='_").append(this.elementid).append("_taskcategoryjson' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.getScheduleTaskCategories().toJSONString(false))).append("\">");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_treedatajson' name='_").append(this.elementid).append("_treedatajson' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.getPlanNodeTreeDataJson(planItems, selectedNodes))).append("\">");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_elementpropsjson' name='_").append(this.elementid).append("_elementpropsjson' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.element.toJSONString(false))).append("\">");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_selectednodes' name='_").append(this.elementid).append("_selectednodes' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(selectedNodes.toJSONString(false, false))).append("\">");
            html.append("<input type='hidden' id='_").append(this.elementid).append("_lookuphtml' name='_").append(this.elementid).append("_lookuphtml' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.getLookupHtml(datasetName, javaScriptVarName, planItems).toJSONString(false, false))).append("\">");
            html.append("<input type='hidden' id='__").append(this.elementid).append("_rsetid' name='__").append(this.elementid).append("_rsetid' value=\"\">");
            html.append("<input type='hidden' id='__").append(this.elementid).append("_excludedcolumns' name='__").append(this.elementid).append("_excludedcolumns' value=\"").append(this.element.getProperty("excludedcolumns")).append("\">");
            html.append("<script type='text/javascript' language='JavaScript'>");
            html.append("var ").append(javaScriptVarName).append(" = new PIM.PlanItemManager('").append(javaScriptVarName).append("', '").append(this.elementid).append("', '").append(this.getSDIFormId()).append("', '").append(datasetName).append("'); ");
            html.append("$(window).ready(").append(javaScriptVarName).append(".render);");
            html.append("</script>");
        }
        return html.toString();
    }

    private PropertyListCollection getLookupHtml(String datasetName, String javaScriptVarName, DataSet planItems) {
        PropertyList lookupProps;
        int i;
        String sdcId;
        PropertyList sdcProps;
        String sdcId2;
        PropertyListCollection lookupHtmlCollection = new PropertyListCollection();
        PropertyListCollection scheduleTemplateSdcCollection = this.element.getPropertyListNotNull("scheduletaskprops").getCollectionNotNull("scheduletemplatesdccollection");
        PropertyListCollection sourceSdcCollection = this.element.getPropertyListNotNull("scheduletaskprops").getCollectionNotNull("sourcesdccollection");
        for (int i2 = 0; i2 < planItems.getRowCount(); ++i2) {
            String sourceSdcId = planItems.getString(i2, "linksdcid", "");
            String templateSdcId = planItems.getString(i2, "scheduletemplatesdcid", "");
            boolean foundTemplateSdc = false;
            for (int j = 0; j < scheduleTemplateSdcCollection.size(); ++j) {
                PropertyList sdcProps2 = scheduleTemplateSdcCollection.getPropertyList(j);
                sdcId2 = sdcProps2.getProperty("sdcid");
                if (!sdcId2.equals(templateSdcId)) continue;
                foundTemplateSdc = true;
                break;
            }
            if (!foundTemplateSdc) {
                PropertyList sdcProps3 = new PropertyList();
                sdcProps3.setProperty("sdcid", templateSdcId);
                scheduleTemplateSdcCollection.add(sdcProps3);
            }
            boolean foundSourceSdc = false;
            for (int j = 0; j < sourceSdcCollection.size(); ++j) {
                sdcProps = sourceSdcCollection.getPropertyList(j);
                sdcId = sdcProps.getProperty("sdcid");
                if (!sdcId.equals(sourceSdcId)) continue;
                foundSourceSdc = true;
                break;
            }
            if (foundSourceSdc) continue;
            PropertyList sdcProps4 = new PropertyList();
            sdcProps4.setProperty("sdcid", sourceSdcId);
            sourceSdcCollection.add(sdcProps4);
        }
        String sql = "SELECT sdcid FROM sdc WHERE scheduleableflag = 'Y'";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        for (i = 0; i < ds.getRowCount(); ++i) {
            String scheduleableSdcId = ds.getString(i, "sdcid", "");
            boolean foundScheduleableSdc = false;
            for (int j = 0; j < sourceSdcCollection.size(); ++j) {
                sdcProps = sourceSdcCollection.getPropertyList(j);
                sdcId = sdcProps.getProperty("sdcid");
                if (!sdcId.equals(scheduleableSdcId)) continue;
                foundScheduleableSdc = true;
                break;
            }
            if (foundScheduleableSdc) continue;
            PropertyList sdcProps5 = new PropertyList();
            sdcProps5.setProperty("sdcid", scheduleableSdcId);
            sourceSdcCollection.add(sdcProps5);
        }
        for (i = 0; i < scheduleTemplateSdcCollection.size(); ++i) {
            PropertyList sdcLookupProps = new PropertyList();
            PropertyList templateLookupHtmlProps = new PropertyList();
            sdcLookupProps.setProperty("template", templateLookupHtmlProps);
            PropertyList sdcProps6 = scheduleTemplateSdcCollection.getPropertyList(i);
            sdcId2 = sdcProps6.getProperty("sdcid");
            sdcLookupProps.setProperty("sdcid", sdcId2);
            lookupProps = sdcProps6.getPropertyListNotNull("lookupprops");
            lookupProps.setProperty("sdcid", sdcId2);
            templateLookupHtmlProps.setProperty("html", this.getInput(datasetName, javaScriptVarName, sdcId2, lookupProps, "scheduletemplatekeyid1editor" + sdcId2, "scheduleTemplateLookupOnChange"));
            lookupHtmlCollection.add(sdcLookupProps);
        }
        for (i = 0; i < sourceSdcCollection.size(); ++i) {
            PropertyList sdcLookupProps = new PropertyList();
            PropertyList sourceLookupHtmlProps = new PropertyList();
            sdcLookupProps.setProperty("source", sourceLookupHtmlProps);
            PropertyList sdcProps7 = sourceSdcCollection.getPropertyList(i);
            sdcId2 = sdcProps7.getProperty("sdcid");
            sdcLookupProps.setProperty("sdcid", sdcId2);
            sdcLookupProps.setProperty("singular", new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId2)).getProperty("singular", sdcId2));
            sdcLookupProps.setProperty("scheduleableflag", new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId2)).getProperty("scheduleableflag", sdcId2));
            lookupProps = sdcProps7.getPropertyListNotNull("lookupprops");
            lookupProps.setProperty("sdcid", sdcId2);
            lookupProps.setProperty("dialogtype", "Sapphire Dialog");
            sourceLookupHtmlProps.setProperty("html", this.getInput(datasetName, javaScriptVarName, sdcId2, lookupProps, "linkkeyid1editor" + sdcId2, "sourceLookupOnChange"));
            lookupHtmlCollection.add(sdcLookupProps);
        }
        return lookupHtmlCollection;
    }

    private String getInput(String datasetName, String javaScriptVarName, String sdcId, PropertyList lookupProps, String columnId, String onChange) {
        PropertyList sourceInputProps = new PropertyList();
        sourceInputProps.setProperty("sdcid", sdcId);
        sourceInputProps.setProperty("img", "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg");
        sourceInputProps.setProperty("data", datasetName);
        sourceInputProps.setProperty("columnid", columnId);
        sourceInputProps.setProperty("mode", "lookup");
        sourceInputProps.setProperty("validation", "Mandatory;");
        sourceInputProps.setProperty("title", "Source");
        sourceInputProps.setProperty("value", "[__value]");
        sourceInputProps.setProperty("name", datasetName + "[__rowid]_" + columnId);
        sourceInputProps.setProperty("lookuplink", lookupProps);
        sourceInputProps.setProperty("img_cssstyle", "lookup_img");
        sourceInputProps.setProperty("lookupfieldid", datasetName + "[__rowid]_" + columnId);
        sourceInputProps.setProperty("maxlen", "40");
        sourceInputProps.setProperty("onchange", "sapphire.page.getTop().sapphire.page.maint.getMaintFrame()." + javaScriptVarName + "." + onChange + "();");
        sourceInputProps.setProperty("rowindex", "[__rowid]");
        sourceInputProps.setProperty("datasetname", datasetName);
        sourceInputProps.setProperty("readonly", "true");
        sourceInputProps.setProperty("lookuppagedirectives", "oLUPD_" + datasetName + "_" + columnId);
        return SDITagUtil.getInstance(this.pageContext).getInputHtml(sourceInputProps, this.sdiInfo);
    }

    private PropertyList getScheduleTaskCategories() {
        String sql = "SELECT pt.propertytreeid, ci.categoryid FROM propertytree pt LEFT JOIN categoryitem ci ON pt.propertytreeid = ci.keyid1 AND ci.sdcid = 'PropertyTree' WHERE propertytreetype = 'ScheduleTask' ORDER BY pt.propertytreeid";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        PropertyList taskProps = new PropertyList();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String scheduleTaskId = ds.getString(i, "propertytreeid", "");
            String categoryId = ds.getString(i, "categoryid", "");
            if (scheduleTaskId.isEmpty()) continue;
            String taskCategories = taskProps.getProperty(scheduleTaskId);
            taskCategories = taskCategories + ";" + categoryId;
            taskProps.setProperty(scheduleTaskId, taskCategories);
        }
        return taskProps;
    }

    private String getPlanNodeTreeDataJson(DataSet planItems, PropertyList selectedNodes) {
        PropertyListCollection tree = new PropertyListCollection();
        PropertyList schedulePlanNodeProps = this.element.getPropertyListNotNull("scheduleplannodeprops");
        boolean visible = schedulePlanNodeProps.getProperty("visible", DEFAULT_SHOW_BUTTON).toLowerCase().startsWith("y");
        String schedulePlanItemFilter = this.element.getPropertyListNotNull("scheduleplanitemprops").getProperty("filter");
        if (visible) {
            String schedulePlanTitleColumn = schedulePlanNodeProps.getProperty(SCHEDULE_PLAN_TITLE_COLUMN_ALIAS, "scheduleplanid");
            String schedulePlanNodeTitleColumn = schedulePlanNodeProps.getProperty(SCHEDULE_PLAN_NODE_TITLE_COLUMN_ALIAS, "scheduleplannodedesc");
            boolean expanded = schedulePlanNodeProps.getProperty("expanded", DEFAULT_SHOW_BUTTON).toLowerCase().startsWith("y");
            HashSet planItemSet = new HashSet();
            HashSet<String> planIdSet = new HashSet<String>();
            for (int i = 0; i < planItems.getRowCount(); ++i) {
                String planId = planItems.getString(i, "scheduleplanid");
                String planItemId = planItems.getString(i, "scheduleplanitemid");
                planIdSet.add(planId);
                ArrayList<String> planItemKeyList = new ArrayList<String>();
                planItemKeyList.add(planId);
                planItemKeyList.add(planItemId);
                planItemSet.add(planItemKeyList);
            }
            String unManagedSchedulePlanId = "";
            Object unManagedSchedulePlanTypeFlag = "";
            String unManagedSchedulePlanStatus = "";
            if (planIdSet.isEmpty() && schedulePlanItemFilter.equals("Plan") && this.getSDIInfo().getSdcid().equals("SchedulePlan")) {
                DataSet schedulePlanPrimary = this.getSDIInfo().getDataSet("primary");
                String schedulePlanId = schedulePlanPrimary.getString(0, "scheduleplanid", "");
                String schedulePlanTypeFlag = schedulePlanPrimary.getString(0, "scheduleplantypeflag", "");
                String planStatus = schedulePlanPrimary.getString(0, "planstatus", "A");
                if (!schedulePlanTypeFlag.equals("M")) {
                    planIdSet.add(schedulePlanId);
                    unManagedSchedulePlanId = schedulePlanId;
                    unManagedSchedulePlanTypeFlag = schedulePlanTypeFlag;
                    unManagedSchedulePlanStatus = planStatus;
                }
            }
            ArrayList<String> getPlanNodeTreeParams = new ArrayList<String>();
            StringBuilder getPlanNodeTreeWhereFragment = new StringBuilder();
            for (String planId : planIdSet) {
                if (planId.isEmpty()) continue;
                getPlanNodeTreeWhereFragment.append(" OR spn.scheduleplanid = ?");
                getPlanNodeTreeParams.add(planId);
            }
            if (getPlanNodeTreeWhereFragment.length() > 0) {
                String schedulePlanNodeId;
                String getPlanNodeTreeSql = "SELECT sp.scheduleplantypeflag, spn.scheduleplanid, spn.scheduleplannodeid, spn.parentnodeid, spn.usersequence, spn.scheduleplannodedesc, spn.nodestatus, spn." + schedulePlanNodeTitleColumn + " AS " + SCHEDULE_PLAN_NODE_TITLE_COLUMN_ALIAS + ", spn.refsdcid, spn.refkeyid1, spn.refkeyid2, spn.refkeyid3, sp.planstatus FROM scheduleplannode spn JOIN scheduleplan sp ON sp.scheduleplanid = spn.scheduleplanid WHERE " + getPlanNodeTreeWhereFragment.substring(4) + " ORDER BY spn.usersequence, spn.scheduleplannodeid";
                DataSet getPlanNodeTreeDs = this.getQueryProcessor().getPreparedSqlDataSet(getPlanNodeTreeSql, getPlanNodeTreeParams.toArray());
                DataSet getPlanItemNodesDs = null;
                ArrayList<String> getPlanItemNodesParams = new ArrayList<String>();
                StringBuilder getPlanItemNodesWhereFragment = new StringBuilder();
                for (List list : planItemSet) {
                    String planId = (String)list.get(0);
                    String planItemId = (String)list.get(1);
                    getPlanItemNodesWhereFragment.append(" OR (scheduleplanitem.scheduleplanid = ? AND scheduleplanitem.scheduleplanitemid = ?)");
                    getPlanItemNodesParams.add(planId);
                    getPlanItemNodesParams.add(planItemId);
                    if (getPlanItemNodesParams.size() <= 1900 && getPlanItemNodesWhereFragment.length() <= 1900) continue;
                    getPlanItemNodesDs = this.getPlanNodesDs(schedulePlanTitleColumn, getPlanItemNodesDs, getPlanItemNodesParams, getPlanItemNodesWhereFragment);
                    getPlanItemNodesWhereFragment = new StringBuilder();
                    getPlanItemNodesParams = new ArrayList();
                }
                if (getPlanItemNodesWhereFragment.length() > 0) {
                    getPlanItemNodesDs = this.getPlanNodesDs(schedulePlanTitleColumn, getPlanItemNodesDs, getPlanItemNodesParams, getPlanItemNodesWhereFragment);
                } else if (getPlanItemNodesDs == null) {
                    getPlanItemNodesDs = new DataSet();
                }
                PropertyList data = new PropertyList();
                data.setProperty("allowadd", "N");
                data.setProperty("allowremove", "N");
                data.setProperty("isplan", "N");
                data.setProperty("nodestatus", "");
                if (selectedNodes.isEmpty() && schedulePlanNodeProps.getProperty("defaultnode", "None").equals("All")) {
                    PropertyListCollection propertyListCollection = new PropertyListCollection();
                    PropertyList selectedNode = new PropertyList();
                    selectedNode.setProperty("id", ROOT_NODE_ID);
                    propertyListCollection.add(selectedNode);
                    selectedNodes.setProperty("nodecollection", propertyListCollection);
                }
                tree.add(this.createNode(selectedNodes, expanded, ROOT_NODE_ID, this.getTranslationProcessor().translate("All"), "#", data));
                if (!schedulePlanItemFilter.equals("Plan")) {
                    HashSet<String> hashSet = new HashSet<String>();
                    for (int i = 0; i < getPlanItemNodesDs.getRowCount(); ++i) {
                        schedulePlanNodeId = getPlanItemNodesDs.getString(i, "scheduleplannodeid", "");
                        String schedulePlanId = getPlanItemNodesDs.getString(i, "scheduleplanid", "");
                        String schedulePlanTypeFlag = getPlanItemNodesDs.getString(i, "scheduleplantypeflag", "");
                        String planStatus = getPlanItemNodesDs.getString(i, "planstatus", "A");
                        this.addPlanNode(selectedNodes, tree, expanded, getPlanItemNodesDs, hashSet, schedulePlanId, schedulePlanTypeFlag, planStatus);
                        if (schedulePlanNodeId.isEmpty() || hashSet.contains(schedulePlanNodeId)) continue;
                        this.addNode(getPlanNodeTreeDs, tree, hashSet, schedulePlanNodeId, selectedNodes);
                    }
                } else {
                    int i;
                    HashSet<String> hashSet = new HashSet<String>();
                    if (!unManagedSchedulePlanId.isEmpty()) {
                        data = new PropertyList();
                        data.setProperty("allowremove", "N");
                        data.setProperty("allowadd", DEFAULT_SHOW_BUTTON);
                        data.setProperty("isplan", DEFAULT_SHOW_BUTTON);
                        data.setProperty("scheduleplanid", unManagedSchedulePlanId);
                        data.setProperty("scheduleplantype", (String)unManagedSchedulePlanTypeFlag);
                        data.setProperty("nodestatus", unManagedSchedulePlanStatus);
                        PropertyList node = this.createNode(selectedNodes, expanded, this.createPlanNodeId(unManagedSchedulePlanId), this.getSchedulePlanTitle(unManagedSchedulePlanId, getPlanItemNodesDs), ROOT_NODE_ID, data);
                        tree.add(node);
                        hashSet.add(this.createPlanNodeId(unManagedSchedulePlanId));
                    }
                    for (i = 0; i < getPlanItemNodesDs.getRowCount(); ++i) {
                        String schedulePlanId = getPlanItemNodesDs.getString(i, "scheduleplanid", "");
                        String schedulePlanTypeFlag = getPlanItemNodesDs.getString(i, "scheduleplantypeflag", "");
                        String planStatus = getPlanItemNodesDs.getString(i, "planstatus", "A");
                        this.addPlanNode(selectedNodes, tree, expanded, getPlanItemNodesDs, hashSet, schedulePlanId, schedulePlanTypeFlag, planStatus);
                    }
                    for (i = 0; i < getPlanNodeTreeDs.getRowCount(); ++i) {
                        schedulePlanNodeId = getPlanNodeTreeDs.getString(i, "scheduleplannodeid", "");
                        String schedulePlanId = getPlanNodeTreeDs.getString(i, "scheduleplanid", "");
                        String schedulePlanTypeFlag = getPlanNodeTreeDs.getString(i, "scheduleplantypeflag", "");
                        String planStatus = getPlanNodeTreeDs.getString(i, "planstatus", "A");
                        this.addPlanNode(selectedNodes, tree, expanded, getPlanItemNodesDs, hashSet, schedulePlanId, schedulePlanTypeFlag, planStatus);
                        if (schedulePlanNodeId.isEmpty() || hashSet.contains(schedulePlanNodeId)) continue;
                        this.addNode(getPlanNodeTreeDs, tree, hashSet, schedulePlanNodeId, selectedNodes);
                    }
                }
            }
        } else if (selectedNodes.isEmpty()) {
            PropertyListCollection selectedNodeCollection = new PropertyListCollection();
            PropertyList selectedNode = new PropertyList();
            selectedNode.setProperty("id", ROOT_NODE_ID);
            selectedNodeCollection.add(selectedNode);
            selectedNodes.setProperty("nodecollection", selectedNodeCollection);
        }
        this.fillNodeItemCounts(planItems, tree);
        return tree.toJSONString(false, false);
    }

    private DataSet getPlanNodesDs(String schedulePlanTitleColumn, DataSet getPlanItemNodesDs, List<String> getPlanItemNodesParams, StringBuilder getPlanItemNodesWhereFragment) {
        boolean isShowHidden = DEFAULT_SHOW_BUTTON.equals(this.getConfigurationProcessor().getProfileProperty("viewhidden", "N"));
        String hiddenSQL = "";
        if (isShowHidden) {
            hiddenSQL = " coalesce(scheduleplan.activeflag, 'Y') = 'Y' AND coalesce(scheduleplanitem.activeflag, 'Y') = 'Y' AND ";
        }
        String getPlanItemNodesSql = "SELECT DISTINCT scheduleplanitem.scheduleplannodeid, scheduleplan.scheduleplantypeflag, scheduleplan.scheduleplanid, " + schedulePlanTitleColumn + " AS " + SCHEDULE_PLAN_TITLE_COLUMN_ALIAS + ", scheduleplan.planstatus FROM scheduleplanitem JOIN scheduleplan ON scheduleplanitem.scheduleplanid = scheduleplan.scheduleplanid WHERE " + hiddenSQL + getPlanItemNodesWhereFragment.substring(4);
        DataSet newPlanNodesDs = this.getQueryProcessor().getPreparedSqlDataSet(getPlanItemNodesSql, getPlanItemNodesParams.toArray());
        if (getPlanItemNodesDs == null) {
            getPlanItemNodesDs = newPlanNodesDs;
        } else {
            for (int j = 0; j < newPlanNodesDs.getRowCount(); ++j) {
                getPlanItemNodesDs.copyRow(newPlanNodesDs, j, 1);
            }
        }
        return getPlanItemNodesDs;
    }

    private void addPlanNode(PropertyList selectedNodes, PropertyListCollection tree, boolean expanded, DataSet getPlanItemNodesDs, Set<String> addedNodeSet, String schedulePlanId, String schedulePlanTypeFlag, String planStatus) {
        if (!addedNodeSet.contains(this.createPlanNodeId(schedulePlanId))) {
            PropertyList data = new PropertyList();
            data.setProperty("allowremove", "N");
            data.setProperty("allowadd", DEFAULT_SHOW_BUTTON);
            data.setProperty("isplan", DEFAULT_SHOW_BUTTON);
            data.setProperty("scheduleplanid", schedulePlanId);
            data.setProperty("scheduleplantype", schedulePlanTypeFlag);
            data.setProperty("nodestatus", planStatus);
            data.setProperty("usersequence", "-1");
            PropertyList node = this.createNode(selectedNodes, expanded, this.createPlanNodeId(schedulePlanId), this.getSchedulePlanTitle(schedulePlanId, getPlanItemNodesDs), ROOT_NODE_ID, data);
            tree.add(node);
            addedNodeSet.add(this.createPlanNodeId(schedulePlanId));
        }
    }

    private void fillNodeItemCounts(DataSet planItems, PropertyListCollection tree) {
        int i;
        HashMap<String, Integer> nodeItemCount = new HashMap<String, Integer>();
        for (i = 0; i < planItems.getRowCount(); ++i) {
            String schedulePlanNodeId = planItems.getString(i, "scheduleplannodeid", this.createPlanNodeId(planItems.getString(i, "scheduleplanid")));
            this.addItemCount(schedulePlanNodeId, tree, nodeItemCount);
        }
        for (i = 0; i < tree.size(); ++i) {
            PropertyList node = tree.getPropertyList(i);
            String nodeId = node.getProperty("id");
            String text = node.getProperty("text");
            Integer count = (Integer)nodeItemCount.get(nodeId);
            if (count == null) {
                count = 0;
            }
            node.setProperty("text", text + " &#8206;(&#8206;" + count + "&#8206;)&#8206;");
        }
    }

    private void addItemCount(String schedulePlanNodeId, PropertyListCollection tree, Map<String, Integer> nodeItemCount) {
        for (int i = 0; i < tree.size(); ++i) {
            PropertyList node = tree.getPropertyList(i);
            String nodeId = node.getProperty("id");
            String parentNodeId = node.getProperty("parent");
            if (!nodeId.equals(schedulePlanNodeId)) continue;
            this.addItemCount(parentNodeId, tree, nodeItemCount);
            Integer count = nodeItemCount.get(schedulePlanNodeId);
            if (count == null) {
                count = 1;
            } else {
                Integer n = count;
                Integer n2 = count = Integer.valueOf(count + 1);
            }
            nodeItemCount.put(schedulePlanNodeId, count);
        }
    }

    private PropertyList createNode(PropertyList selectedNodes, boolean expanded, String id, String text, String parent, PropertyList data) {
        PropertyList node = new PropertyList();
        node.setProperty("id", id);
        node.setProperty("text", text);
        node.setProperty("parent", parent);
        String nodeStatus = data.getProperty("nodestatus", "A");
        if (data.getProperty("isplan").equals(DEFAULT_SHOW_BUTTON)) {
            if (!nodeStatus.equals("A")) {
                node.setProperty("icon", "WEB-CORE/imageref/society_and_culture/arts_and_design/3d_shapes/16/cubes_red.png");
            } else {
                node.setProperty("icon", "WEB-CORE/imageref/society_and_culture/arts_and_design/3d_shapes/16/cubes_yellow.png");
            }
        } else if (!nodeStatus.equals("A")) {
            node.setProperty("icon", "WEB-CORE/imageref/basic_application_icons/file_system/folders/16/folder_red.png");
        } else {
            node.setProperty("icon", "WEB-CORE/imageref/basic_application_icons/file_system/folders/16/folder.png");
        }
        PropertyList state = new PropertyList();
        if (expanded) {
            state.setProperty("opened", "true");
        }
        this.setSelectedState(selectedNodes, node, state);
        node.setProperty("state", state);
        data.setProperty("text", text);
        node.setProperty("data", data);
        PropertyList attributes = new PropertyList();
        StringBuilder title = new StringBuilder();
        if (!data.getProperty("refsdcid").isEmpty()) {
            PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(data.getProperty("refsdcid")));
            title.append(this.getTranslationProcessor().translate(sdcProps.getProperty("singular")));
            title.append(": ");
            title.append(data.getProperty("refkeyid1"));
            if (!data.getProperty("refkeyid2").isEmpty()) {
                title.append(" (").append(data.getProperty("refkeyid2")).append(")");
            }
        } else if (data.getProperty("isplan").equals(DEFAULT_SHOW_BUTTON)) {
            PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties("SchedulePlan"));
            title.append(this.getTranslationProcessor().translate(sdcProps.getProperty("singular"))).append(": ").append(data.getProperty("scheduleplanid"));
        } else if (id.equals(ROOT_NODE_ID)) {
            title.append(this.getTranslationProcessor().translate("All"));
        } else {
            title.append(this.getTranslationProcessor().translate("No source"));
        }
        if (!id.equals(ROOT_NODE_ID)) {
            title.append("\n");
            if (nodeStatus.equals("A")) {
                title.append(this.getTranslationProcessor().translate("Turned On"));
            } else {
                title.append(this.getTranslationProcessor().translate("Turned Off"));
            }
        }
        attributes.setProperty("title", title.toString());
        node.setProperty("li_attr", attributes);
        return node;
    }

    private String getSchedulePlanTitle(String planId, DataSet ds) {
        String schedulePlanTitle = planId;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (!planId.equals(ds.getString(i, "scheduleplanid"))) continue;
            schedulePlanTitle = this.getTranslationProcessor().translate(ds.getString(i, SCHEDULE_PLAN_TITLE_COLUMN_ALIAS, planId));
        }
        return SafeHTML.encodeForHTML(schedulePlanTitle);
    }

    private void addNode(DataSet getPlanNodeTreeDs, PropertyListCollection tree, Set<String> addedNodeSet, String newNodeId, PropertyList selectedNodes) {
        for (int j = 0; j < getPlanNodeTreeDs.getRowCount(); ++j) {
            String schedulePlanNodeId = getPlanNodeTreeDs.getString(j, "scheduleplannodeid");
            PropertyList schedulePlanNodeProps = this.element.getPropertyListNotNull("scheduleplannodeprops");
            boolean expanded = schedulePlanNodeProps.getProperty("expanded", DEFAULT_SHOW_BUTTON).toLowerCase().startsWith("y");
            if (!newNodeId.equals(schedulePlanNodeId)) continue;
            String planId = getPlanNodeTreeDs.getString(j, "scheduleplanid");
            String schedulePlanType = getPlanNodeTreeDs.getString(j, "scheduleplantypeflag");
            String nodeId = getPlanNodeTreeDs.getString(j, "scheduleplannodeid");
            String parentNodeId = getPlanNodeTreeDs.getString(j, "parentnodeid");
            String referenceSdcId = getPlanNodeTreeDs.getString(j, "refsdcid", "");
            String referenceKeyId1 = getPlanNodeTreeDs.getString(j, "refkeyid1", "");
            String referenceKeyId2 = getPlanNodeTreeDs.getString(j, "refkeyid2", "");
            String referenceKeyId3 = getPlanNodeTreeDs.getString(j, "refkeyid3", "");
            String nodeStatus = getPlanNodeTreeDs.getString(j, "nodestatus", "A");
            String userSequence = getPlanNodeTreeDs.getBigDecimal(j, "usersequence", BigDecimal.ONE).toString();
            String schedulePlanNodeTitle = this.getTranslationProcessor().translate(this.getSchedulePlanNodeTitle(referenceSdcId, referenceKeyId1, referenceKeyId2, referenceKeyId3, getPlanNodeTreeDs.getString(j, SCHEDULE_PLAN_NODE_TITLE_COLUMN_ALIAS, nodeId)));
            if (parentNodeId.equals("root")) {
                parentNodeId = this.createPlanNodeId(planId);
            } else {
                this.addNode(getPlanNodeTreeDs, tree, addedNodeSet, parentNodeId, selectedNodes);
            }
            if (addedNodeSet.contains(nodeId)) continue;
            PropertyList data = new PropertyList();
            data.setProperty("refsdcid", referenceSdcId);
            data.setProperty("refkeyid1", referenceKeyId1);
            data.setProperty("refkeyid2", referenceKeyId2);
            data.setProperty("refkeyid3", referenceKeyId3);
            data.setProperty("scheduleplanid", planId);
            data.setProperty("allowremove", DEFAULT_SHOW_BUTTON);
            data.setProperty("allowadd", DEFAULT_SHOW_BUTTON);
            data.setProperty("isplan", "N");
            data.setProperty("scheduleplantype", schedulePlanType);
            data.setProperty("nodestatus", nodeStatus);
            data.setProperty("usersequence", userSequence);
            tree.add(this.createNode(selectedNodes, expanded, nodeId, schedulePlanNodeTitle, parentNodeId, data));
            addedNodeSet.add(nodeId);
            break;
        }
    }

    private String getSchedulePlanNodeTitle(String referenceSdcId, String referenceKeyId1, String referenceKeyId2, String referenceKeyId3, String defaultSchedulePlanNodeTitle) {
        String returnValue = defaultSchedulePlanNodeTitle;
        PropertyListCollection nodeSDCCollection = this.element.getPropertyListNotNull("scheduleplannodeprops").getCollectionNotNull("nodesdccollection");
        for (int i = 0; i < nodeSDCCollection.size(); ++i) {
            DataSet ds;
            PropertyList nodeSDCProps = nodeSDCCollection.getPropertyList(i);
            if (!nodeSDCProps.getProperty("sdcid").equals(referenceSdcId) || !nodeSDCProps.getProperty("enabled").equals(DEFAULT_SHOW_BUTTON)) continue;
            String titleColumn = nodeSDCProps.getProperty("nodetitlecolumn");
            PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(referenceSdcId));
            String table = sdcProps.getProperty("tableid");
            String keyId1Column = sdcProps.getProperty("keycolid1");
            String keyId2Column = sdcProps.getProperty("keycolid2");
            String keyId3Column = sdcProps.getProperty("keycolid3");
            ArrayList<String> sqlParams = new ArrayList<String>();
            String sql = "SELECT " + titleColumn + " FROM " + table + " WHERE " + keyId1Column + " = ?";
            sqlParams.add(referenceKeyId1);
            if (!referenceKeyId2.isEmpty() && !referenceKeyId2.equals("(null)")) {
                sql = sql + " AND " + keyId2Column + " = ?";
                sqlParams.add(referenceKeyId2);
                if (!referenceKeyId3.isEmpty() && !referenceKeyId3.equals("(null)")) {
                    sql = sql + " AND " + keyId3Column + " = ?";
                    sqlParams.add(referenceKeyId3);
                }
            }
            if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray())) == null || ds.getRowCount() <= 0) continue;
            returnValue = ds.getString(0, titleColumn, defaultSchedulePlanNodeTitle);
        }
        return SafeHTML.encodeForHTML(returnValue);
    }

    private String createPlanNodeId(String planId) {
        return "plan" + planId;
    }

    private void setSelectedState(PropertyList selectedNodes, PropertyList node, PropertyList state) {
        PropertyListCollection selectedNodeCollection = selectedNodes.getCollectionNotNull("nodecollection");
        for (int i = 0; i < selectedNodeCollection.size(); ++i) {
            PropertyList selectedNode = selectedNodeCollection.getPropertyList(i);
            String selectedNodeId = selectedNode.getProperty("id");
            if (!selectedNodeId.equals(node.getProperty("id"))) continue;
            state.setProperty("selected", "true");
        }
    }

    private String getButtonsHtml() {
        String returnValue = "";
        StringBuilder html = new StringBuilder();
        boolean viewOnly = this.element.getProperty("viewonly", "N").equals(DEFAULT_SHOW_BUTTON);
        PropertyListCollection buttons = this.element.getCollection("buttons");
        if (buttons != null && buttons.size() > 0) {
            for (int i = 0; i < buttons.size(); ++i) {
                PropertyList buttonProps = buttons.getPropertyList(i);
                String show = buttonProps.getProperty("show", DEFAULT_SHOW_BUTTON);
                String showViewOnly = buttonProps.getProperty("showviewonly", "N");
                if (!show.equals(DEFAULT_SHOW_BUTTON) || viewOnly && !showViewOnly.equals(DEFAULT_SHOW_BUTTON)) continue;
                html.append("<td class='pimButtonCell'>");
                Button button = new Button(this.pageContext);
                String id = buttonProps.getProperty("id");
                if (id != null && id.length() > 0) {
                    button.setId(StringUtil.replaceAll(id, "[elementid]", this.elementid, false));
                }
                button.setText(buttonProps.getProperty("text"));
                button.setImg(buttonProps.getProperty("img"));
                button.setTip(buttonProps.getProperty("tip"));
                button.setAppearance(buttonProps.getProperty("appearance"));
                button.setMargin(buttonProps.getProperty("margin"));
                button.setStyle(buttonProps.getProperty("style", "standard"));
                button.setWidth(buttonProps.getProperty("width"));
                button.setAction(buttonProps.getProperty("js"));
                html.append(button.getHtml());
                html.append("</td>");
            }
        }
        if (html.length() > 0) {
            returnValue = "<table><tr>";
            returnValue = returnValue + html.toString();
            returnValue = returnValue + "</tr></table>";
        }
        return returnValue;
    }

    private PropertyListCollection getJQueryPlugins() {
        PropertyListCollection pluginCollection = new PropertyListCollection();
        PropertyList pluginProps = new PropertyList();
        pluginCollection.add(pluginProps);
        pluginProps.setProperty("pluginid", "jstree");
        pluginProps.setProperty("css", DEFAULT_SHOW_BUTTON);
        pluginProps.setProperty("allowminimized", DEFAULT_SHOW_BUTTON);
        return pluginCollection;
    }
}

