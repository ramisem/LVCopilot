/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.scheduleplan;

import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SampleNodeViewer
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String ROOT_NODE_ID = "all";

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        Object planItemHelper = null;
        Object planItems = null;
        String datasetName = this.elementid + "ds";
        String sdcId = this.requestContext.getProperty("sdcid");
        String keyId1 = this.requestContext.getProperty("keyid1");
        PropertyList selectedNodes = new PropertyList();
        html.append(JavaScriptAPITag.getJQueryAPI(true, this.browser.isMobile(), this.getJQueryPlugins(), this.pageContext));
        html.append("<script type='text/javascript' language='JavaScript' src='WEB-CORE/elements/scheduler/scripts/samplenodeviewer.js'></script>");
        html.append("<link rel='stylesheet' href='" + HttpUtil.getCSS("WEB-CORE/elements/scheduler/stylesheets/planitemmanager.css", this.pageContext) + "' type='text/css'>");
        html.append("<div id='").append(this.elementid).append("_container'></div>");
        String javaScriptVarName = this.elementid.replaceAll("\\W", "");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_treedatajson' name='_").append(this.elementid).append("_treedatajson' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.getPlanNodeTreeDataJson(keyId1, selectedNodes))).append("\">");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_elementpropsjson' name='_").append(this.elementid).append("_elementpropsjson' value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(this.element.toJSONString(false))).append("\">");
        html.append("<script type='text/javascript' language='JavaScript'>");
        html.append("var ").append(javaScriptVarName).append(" = new SNV.SampleNodeViewer('").append(javaScriptVarName).append("', '").append(this.elementid).append("', '").append(this.getSDIFormId()).append("', '").append(datasetName).append("'); ");
        html.append("$(window).ready(").append(javaScriptVarName).append(".render);");
        html.append("</script>");
        return html.toString();
    }

    private String getPlanNodeTreeDataJson(String keyid1, PropertyList selectedNodes) {
        PropertyListCollection tree = new PropertyListCollection();
        PropertyList schedulePlanNodeProps = this.element.getPropertyListNotNull("scheduleplannodeprops");
        boolean visible = schedulePlanNodeProps.getProperty("visible", "Y").toLowerCase().startsWith("y");
        String schedulePlanItemFilter = this.element.getPropertyListNotNull("scheduleplanitemprops").getProperty("filter");
        String getLocationNodeHierarchySql = "WITH location(sdcid, keyid1, filtercolumn, label, locationtype, parentlocationid) AS ( SELECT 'Location' sdcid, s_locationid keyid1, 'locationid' filtercolumn, locationlabel label, locationtype,  parentlocationid FROM s_location    WHERE s_locationid IN (SELECT DISTINCT locationid FROM s_samplepoint WHERE               s_samplepointid IN ( SELECT samplepointid FROM s_sample WHERE monitorgroupid = ?))        or s_locationid IN (SELECT DISTINCT parentlocationid FROM s_location WHERE              s_locationid IN ( SELECT locationid FROM s_sample WHERE monitorgroupid = ? and samplepointid is null)) UNION ALL  SELECT 'Location' sdcid, l.s_locationid keyid1, 'locationid' filtercolumn, l.locationlabel label, l.locationtype,    l.parentlocationid FROM s_location l INNER JOIN location l2 ON l2.parentlocationid = l.s_locationid ) SELECT 'Location' sdcid, keyid1, filtercolumn, label , coalesce(locationtype, 'Location') locationtype, parentlocationid FROM location UNION ALL SELECT 'SamplePoint' sdcid, s_samplepointid keyid1, 'samplepointid' filtercolumn, locationlabel label, 'SamplePoint' AS locationtype, coalesce(locationid, 'all') parentlocationid FROM s_samplepoint WHERE s_samplepointid IN (                SELECT samplepointid FROM s_sample WHERE monitorgroupid = ?) UNION ALL  SELECT 'Location' sdcid, s_locationid keyid1, 'locationid' filtercolumn, locationlabel label, 'Location' AS locationtype, coalesce(parentlocationid, 'all') parentlocationid FROM s_location WHERE s_locationid IN (  SELECT locationid FROM s_sample WHERE monitorgroupid = ? and samplepointid is null)";
        DataSet getPlanNodeTreeDs = this.getQueryProcessor().getPreparedSqlDataSet(getLocationNodeHierarchySql, (Object[])new String[]{keyid1, keyid1, keyid1, keyid1});
        PropertyList data = new PropertyList();
        data.setProperty("allowadd", "N");
        data.setProperty("allowremove", "N");
        data.setProperty("isplan", "N");
        data.setProperty("nodestatus", "");
        boolean expanded = true;
        tree.add(this.createNode(selectedNodes, expanded, ROOT_NODE_ID, this.getTranslationProcessor().translate("All"), "#", data));
        HashSet<String> addedNodeSet = new HashSet<String>();
        HashMap<String, String> locationLabelMap = new HashMap<String, String>();
        this.addMainNodes(getPlanNodeTreeDs, tree, addedNodeSet, selectedNodes, locationLabelMap);
        String dateSqlPart = "";
        dateSqlPart = this.getConnectionProcessor().isMSS() ? "cast(eventdt as varchar)" : "to_char(eventdt, 'MM/DD/YY HH12:MI:SS AM')";
        String sql = "SELECT coalesce(samplepointid, locationid ) AS source, locationid, samplepointid, eventplan, eventplanitem, eventnum, samplepointinstance instance, eventdt, " + dateSqlPart + " eventdtstr FROM s_sample WHERE monitorgroupid = ?";
        DataSet sampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1});
        this.addSubSetNodes(sampleDs, tree, addedNodeSet, selectedNodes, locationLabelMap);
        return tree.toJSONString(false, false);
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
            node.setProperty("text", text + " (" + count + ")");
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
        String iconOpen;
        String icon;
        String type;
        PropertyList node;
        block12: {
            String defaultIcon;
            PropertyListCollection typeColl;
            PropertyList nodeProps;
            block11: {
                node = new PropertyList();
                node.setProperty("id", id);
                node.setProperty("text", SafeHTML.encodeForHTML(text));
                node.setProperty("parent", parent);
                type = data.getProperty("type", "");
                nodeProps = this.element.getPropertyListNotNull("nodeprops");
                typeColl = nodeProps.getCollectionNotNull("typecollection");
                defaultIcon = "WEB-CORE/imageref/basic_application_icons/file_system/folders/16/folder.png";
                if (!id.equals(ROOT_NODE_ID)) break block11;
                icon = this.validateIcon(nodeProps.getProperty("allicon", defaultIcon));
                iconOpen = this.validateIcon(nodeProps.getProperty("alliconopen", ""));
                if (!iconOpen.isEmpty()) break block12;
                iconOpen = icon;
                break block12;
            }
            icon = this.validateIcon(nodeProps.getProperty("defaulticon", defaultIcon));
            iconOpen = this.validateIcon(nodeProps.getProperty("defaulticonopen", defaultIcon));
            for (int i = 0; i < typeColl.size(); ++i) {
                PropertyList typeProps = typeColl.getPropertyList(i);
                String collectionType = typeProps.getProperty("type", "");
                if (!type.equals(collectionType)) continue;
                String newIcon = typeProps.getProperty("icon");
                String newIconOpen = typeProps.getProperty("iconopen");
                if (!newIcon.isEmpty()) {
                    icon = newIcon = this.validateIcon(newIcon);
                }
                if (!newIconOpen.isEmpty()) {
                    iconOpen = newIconOpen = this.validateIcon(newIconOpen);
                    break;
                }
                if (newIcon.isEmpty()) break;
                iconOpen = newIcon;
                break;
            }
        }
        node.setProperty("type", type);
        if (expanded) {
            node.setProperty("icon", iconOpen);
        } else {
            node.setProperty("icon", icon);
        }
        data.setProperty("icon", icon);
        data.setProperty("iconopen", iconOpen);
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
        if (!data.getProperty("sdcid").isEmpty()) {
            PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(data.getProperty("sdcid")));
            title.append(this.getTranslationProcessor().translate(sdcProps.getProperty("singular")));
            title.append(": ");
            title.append(data.getProperty("keyid1"));
            if (!data.getProperty("keyid2").isEmpty()) {
                title.append(" (").append(data.getProperty("keyid2")).append(")");
            }
        } else if (id.equals(ROOT_NODE_ID)) {
            title.append(this.getTranslationProcessor().translate("All"));
        }
        if (!id.equals(ROOT_NODE_ID)) {
            title.append("\n");
        }
        attributes.setProperty("title", title.toString());
        node.setProperty("li_attr", attributes);
        return node;
    }

    private String validateIcon(String newIcon) {
        if (newIcon.contains("rc?command=image&image=")) {
            if (!newIcon.startsWith("./")) {
                newIcon = "./" + newIcon;
            }
            if (!newIcon.contains("&size=")) {
                newIcon = newIcon + "&size=16";
            }
        }
        return newIcon;
    }

    private void addMainNodes(DataSet nodeDs, PropertyListCollection tree, Set<String> addedNodeSet, PropertyList selectedNodes, HashMap<String, String> locationLabelMap) {
        for (int j = 0; j < nodeDs.getRowCount(); ++j) {
            String nodeId;
            String parentNodeId;
            boolean expanded = true;
            String referenceSdcId = nodeDs.getString(j, "sdcid", "");
            String referenceKeyId1 = nodeDs.getString(j, "keyid1", "");
            String type = nodeDs.getString(j, "locationtype", "");
            String filtercolumn = nodeDs.getString(j, "filtercolumn", "");
            String label = nodeDs.getString(j, "label", "");
            locationLabelMap.put(referenceSdcId + "|" + referenceKeyId1, label);
            if (label.isEmpty()) {
                label = referenceKeyId1;
            }
            if (!(parentNodeId = nodeDs.getString(j, "parentlocationid", ROOT_NODE_ID)).equals(ROOT_NODE_ID)) {
                parentNodeId = "Location|" + parentNodeId;
            }
            if (addedNodeSet.contains(nodeId = referenceSdcId + "|" + referenceKeyId1)) continue;
            PropertyList data = new PropertyList();
            data.setProperty("sdcid", referenceSdcId);
            data.setProperty("keyid1", referenceKeyId1);
            data.setProperty("filtercolumnid", filtercolumn);
            data.setProperty("filtercolumnvalue", referenceKeyId1);
            data.setProperty("type", type);
            if (referenceSdcId.equals("SamplePoint")) {
                expanded = false;
            }
            tree.add(this.createNode(selectedNodes, expanded, nodeId, label, parentNodeId, data));
            addedNodeSet.add(nodeId);
        }
    }

    private void addSubSetNodes(DataSet nodeDs, PropertyListCollection tree, Set<String> addedNodeSet, PropertyList selectedNodes, HashMap<String, String> locationLabelMap) {
        for (int j = 0; j < nodeDs.getRowCount(); ++j) {
            boolean expanded = false;
            String samplePointId = nodeDs.getString(j, "samplepointid", "");
            String locationid = nodeDs.getString(j, "locationid", "");
            String label = nodeDs.getString(j, "label", "");
            String locationLabel = nodeDs.getString(j, "locationlabel", "");
            String eventplan = nodeDs.getString(j, "eventplan", "");
            String eventplanitem = nodeDs.getString(j, "eventplanitem", "");
            String eventDtStr = nodeDs.getValue(j, "eventdt", "");
            Calendar eventDt = nodeDs.getCalendar(j, "eventdt");
            String eventDtStrDb = nodeDs.getString(j, "eventdtstr", "");
            String eventnum = nodeDs.getValue(j, "eventnum", "");
            String instance = nodeDs.getValue(j, "instance", "");
            BigDecimal instanceBd = nodeDs.getBigDecimal(j, "instance", BigDecimal.ONE);
            BigDecimal eventNumBigDecimal = nodeDs.getBigDecimal(j, "eventnum", BigDecimal.ONE);
            String source = nodeDs.getValue(j, "source", "unknown");
            HashMap<String, Object> filterMap = new HashMap<String, Object>();
            filterMap.put("eventplan", eventplan);
            filterMap.put("eventdt", eventDt);
            filterMap.put("source", source);
            DataSet filterDs = nodeDs.getFilteredDataSet(filterMap);
            if (filterDs.getRowCount() <= 1) continue;
            if (label.isEmpty()) {
                label = eventplan + " " + eventDtStr;
            }
            String type = "";
            String referenceSdcId = "";
            String referenceKeyId1 = "";
            String parentNodeId = nodeDs.getString(j, "parentlocationid", "");
            if (parentNodeId.isEmpty()) {
                if (samplePointId.isEmpty() && !locationid.isEmpty()) {
                    referenceSdcId = "Location";
                    referenceKeyId1 = locationid;
                    parentNodeId = referenceSdcId + "|" + referenceKeyId1;
                    type = "LocationSubSet";
                } else if (!samplePointId.isEmpty()) {
                    referenceSdcId = "SamplePoint";
                    referenceKeyId1 = samplePointId;
                    parentNodeId = referenceSdcId + "|" + referenceKeyId1;
                    type = "SamplePointSubSet";
                } else {
                    parentNodeId = ROOT_NODE_ID;
                    type = "unknown";
                }
            }
            String filterNodeId = source + "|" + eventplan + "|" + eventDtStrDb;
            String subSetNodeId = parentNodeId + "|" + filterNodeId;
            if (!addedNodeSet.contains(subSetNodeId)) {
                PropertyList data = new PropertyList();
                data.setProperty("filtercolumnid", "eventplandate");
                data.setProperty("filtercolumnvalue", filterNodeId);
                data.setProperty("type", type);
                data.setProperty("sdcid", referenceSdcId);
                data.setProperty("keyid1", referenceKeyId1);
                data.setProperty("eventdt", eventDtStr);
                tree.add(this.createNode(selectedNodes, expanded, subSetNodeId, label, parentNodeId, data));
                addedNodeSet.add(subSetNodeId);
            }
            filterMap.put("instance", instanceBd);
            filterDs = nodeDs.getFilteredDataSet(filterMap);
            HashMap<String, String> transMap = new HashMap<String, String>();
            transMap.put("instance", instance);
            locationLabel = locationLabelMap.get(referenceSdcId + "|" + referenceKeyId1);
            label = this.getTranslationProcessor().translate(locationLabel) + " " + this.getTranslationProcessor().translate("[instance]", transMap);
            String subSubSetFilterValue = source + "|" + eventplan + "|" + eventDtStrDb + "|" + instance;
            String subSubSetNodeId = subSetNodeId + "|" + eventplan + "|" + instance + "|" + eventDtStr;
            if (addedNodeSet.contains(subSubSetNodeId)) continue;
            PropertyList data = new PropertyList();
            data.setProperty("filtercolumnid", "eventplaninstance");
            data.setProperty("filtercolumnvalue", subSubSetFilterValue);
            data.setProperty("type", "eventplaninstance");
            data.setProperty("sdcid", referenceSdcId);
            data.setProperty("keyid1", referenceKeyId1);
            data.setProperty("eventdt", eventDtStr);
            tree.add(this.createNode(selectedNodes, expanded, subSubSetNodeId, label, subSetNodeId, data));
            addedNodeSet.add(subSubSetNodeId);
        }
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

    private PropertyListCollection getJQueryPlugins() {
        PropertyListCollection pluginCollection = new PropertyListCollection();
        PropertyList pluginProps = new PropertyList();
        pluginCollection.add(pluginProps);
        pluginProps.setProperty("pluginid", "jstree");
        pluginProps.setProperty("css", "Y");
        pluginProps.setProperty("allowminimized", "Y");
        return pluginCollection;
    }
}

