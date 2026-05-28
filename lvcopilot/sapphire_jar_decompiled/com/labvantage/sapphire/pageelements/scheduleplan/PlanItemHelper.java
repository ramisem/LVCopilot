/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.scheduleplan;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.Maint;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import com.labvantage.sapphire.tagext.QueryData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PlanItemHelper
extends BaseCustom {
    private static final int MAX_SQL_LENGTH = 4000;
    private final PropertyList primaryKeys;
    private final PropertyList element;
    private final String datasetName;
    private String rsetId;
    private List<String> unmanagedSDCList = new ArrayList<String>();

    public PlanItemHelper(String connectionId, RequestContext requestContext, PropertyList element, String datasetName) throws SapphireException {
        if (connectionId == null) {
            throw new SapphireException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new SapphireException("Connection ID is empty");
        }
        if (requestContext == null) {
            throw new SapphireException("Request context is null");
        }
        if (element == null) {
            throw new SapphireException("Element is null");
        }
        if (datasetName == null) {
            throw new SapphireException("Dataset name is null");
        }
        if (datasetName.isEmpty()) {
            throw new SapphireException("Dataset name is empty");
        }
        this.evaluateCMTPolicy(connectionId, element);
        this.element = element;
        this.primaryKeys = requestContext.getPropertyList();
        this.rsetId = "";
        this.datasetName = datasetName;
        this.setConnectionId(connectionId);
    }

    public PlanItemHelper(String connectionId, String sdcId, String keyId1, String keyId2, String keyId3, PropertyList element, String datasetName) throws SapphireException {
        if (connectionId == null) {
            throw new SapphireException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new SapphireException("Connection ID is empty");
        }
        if (element == null) {
            throw new SapphireException("Element is null");
        }
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID1 is empty");
        }
        if (datasetName == null) {
            throw new SapphireException("Dataset name is null");
        }
        if (datasetName.isEmpty()) {
            throw new SapphireException("Dataset name is empty");
        }
        PropertyList keys = new PropertyList();
        keys.setProperty("sdcid", sdcId);
        keys.setProperty("keyid1", keyId1);
        if (keyId2 != null) {
            keys.setProperty("keyid2", keyId2);
            if (keyId3 != null) {
                keys.setProperty("keyid3", keyId3);
            }
        }
        this.evaluateCMTPolicy(connectionId, element);
        this.element = element;
        this.primaryKeys = keys;
        this.rsetId = "";
        this.datasetName = datasetName;
        this.setConnectionId(connectionId);
    }

    public DataSet getPrimaryDataSet() {
        String primarySDCId = this.primaryKeys.getProperty("sdcid");
        String primaryKeyId1 = this.primaryKeys.getProperty("keyid1");
        String primaryKeyId2 = this.primaryKeys.getProperty("keyid2");
        String primaryKeyId3 = this.primaryKeys.getProperty("keyid3");
        SDIRequest getPrimaryDataSet = new SDIRequest();
        getPrimaryDataSet.setSDCid(primarySDCId);
        getPrimaryDataSet.setKeyid1List(primaryKeyId1);
        if (!primaryKeyId2.isEmpty()) {
            getPrimaryDataSet.setKeyid2List(primaryKeyId2);
            if (!primaryKeyId3.isEmpty()) {
                getPrimaryDataSet.setKeyid3List(primaryKeyId3);
            }
        }
        getPrimaryDataSet.setRequestItem("primary");
        return this.getSDIProcessor().getSDIData(getPrimaryDataSet).getDataset("primary");
    }

    public DataSet getPlanItems() throws SapphireException {
        return this.getPlanItems(null, false);
    }

    public DataSet getPlanItems(PropertyListCollection restrictionCollection, boolean lock) throws SapphireException {
        ArrayList<String> queryWhereArr = this.getQueryWhere();
        ArrayList allPlanItems = null;
        for (String queryWhere : queryWhereArr) {
            SDIData sdiData;
            DataSet planItemsDs;
            SDIRequest getPlanItemsDataSet = new SDIRequest();
            getPlanItemsDataSet.setSDCid("SchedulePlanItem");
            getPlanItemsDataSet.setQueryFrom(this.getQueryFrom());
            getPlanItemsDataSet.setQueryWhere(queryWhere);
            getPlanItemsDataSet.setRequestItem("primary[" + this.getRequestColumns() + "]");
            if (lock) {
                getPlanItemsDataSet.setPrimaryLockOption("LA");
                getPlanItemsDataSet.setRetainRsetid(true);
            }
            if ((planItemsDs = (sdiData = this.getSDIProcessor().getSDIData(getPlanItemsDataSet)).getDataset("primary")).getRowCount() > 0) {
                this.filterPlanItemsByNodeRestrictions(planItemsDs, restrictionCollection, lock);
                planItemsDs = this.applySchedulePlanSecurity(planItemsDs);
                if (lock) {
                    String rsetid = sdiData.getRsetid();
                    this.rsetId = this.rsetId.isEmpty() ? rsetid : this.rsetId + "|" + rsetid;
                }
            }
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            TimeZone userTimeZone = m18n.getTimezone();
            for (int i = 0; i < planItemsDs.getRowCount(); ++i) {
                Calendar stopDt;
                String timeZone = planItemsDs.getString(i, "timezone");
                TimeZone planTimeZone = timeZone != null && !timeZone.isEmpty() ? TimeZone.getTimeZone(timeZone) : TimeZone.getDefault();
                Calendar startDt = planItemsDs.getCalendar(i, "startdt");
                if (startDt != null) {
                    int timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, startDt);
                    startDt.add(14, timeZoneCorrection);
                }
                if ((stopDt = planItemsDs.getCalendar(i, "stopdt")) != null) {
                    int timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(userTimeZone, planTimeZone, stopDt);
                    stopDt.add(14, timeZoneCorrection);
                }
                if (allPlanItems == null) continue;
                String schedulePlanId = planItemsDs.getString(i, "scheduleplanid");
                String schedulePlanItemId = planItemsDs.getString(i, "scheduleplanitemid");
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("scheduleplanid", schedulePlanId);
                filterMap.put("scheduleplanitemid", schedulePlanItemId);
                DataSet filterDs = ((DataSet)allPlanItems).getFilteredDataSet(filterMap);
                if (filterDs.getRowCount() != 0) continue;
                ((DataSet)allPlanItems).copyRow(planItemsDs, i, 1);
            }
            if (allPlanItems != null) continue;
            allPlanItems = planItemsDs;
        }
        if (allPlanItems != null && this.unmanagedSDCList.size() > 0) {
            for (int i = 0; i < allPlanItems.size(); ++i) {
                if (this.unmanagedSDCList.contains(((DataSet)allPlanItems).getString(i, "linksdcid"))) {
                    ((DataSet)allPlanItems).setString(i, "unmanagedsdcflag", "Y");
                    continue;
                }
                ((DataSet)allPlanItems).setString(i, "unmanagedsdcflag", "N");
            }
        }
        return allPlanItems;
    }

    private String getQueryFrom() {
        String queryFrom = "scheduleplanitem";
        PropertyList schedulePlanItemProps = this.element.getPropertyListNotNull("scheduleplanitemprops");
        String filter = schedulePlanItemProps.getProperty("filter");
        if (filter.equals("SQL")) {
            String sdcId = this.primaryKeys.getProperty("sdcid");
            String keyId1 = this.primaryKeys.getProperty("keyid1");
            String keyId2 = this.primaryKeys.getProperty("keyid2");
            String keyId3 = this.primaryKeys.getProperty("keyid3");
            PropertyList sqlProps = schedulePlanItemProps.getPropertyListNotNull("sqlprops");
            String queryFromStr = sqlProps.getProperty("queryfrom");
            if (queryFromStr.isEmpty()) {
                queryFromStr = queryFromStr.replaceAll("[sdcid]", sdcId);
                queryFromStr = queryFromStr.replaceAll("[keyid1]", keyId1);
                queryFromStr = queryFromStr.replaceAll("[keyid2]", keyId2);
                queryFromStr = queryFromStr.replaceAll("[keyid3]", keyId3);
                queryFrom = queryFrom + " " + queryFromStr;
            }
        }
        return queryFrom;
    }

    private String getRequestColumns() {
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList("SchedulePlanItem");
        StringBuffer subQueryColumns = new StringBuffer();
        PropertyListCollection columnCollection = (PropertyListCollection)this.element.getCollection("columns").clone();
        String columnList = this.getColumnsList(columnCollection, sdcProps, subQueryColumns, false);
        columnList = columnList + ",scheduleplannodeid";
        return columnList;
    }

    private ArrayList<String> getQueryWhere() throws SapphireException {
        ArrayList<String> queryWheres = new ArrayList<String>();
        StringBuilder queryWhere = new StringBuilder();
        PropertyList schedulePlanItemProps = this.element.getPropertyListNotNull("scheduleplanitemprops");
        String filter = schedulePlanItemProps.getProperty("filter");
        if (filter.isEmpty()) {
            throw new SapphireException("Schedule plan item filter not defined");
        }
        String sdcId = this.primaryKeys.getProperty("sdcid");
        String keyId1 = this.primaryKeys.getProperty("keyid1");
        String keyId2 = this.primaryKeys.getProperty("keyid2");
        String keyId3 = this.primaryKeys.getProperty("keyid3");
        boolean isOracle = this.getConnectionProcessor().isOra();
        if (filter.equalsIgnoreCase("Plan")) {
            if (!sdcId.equals("SchedulePlan")) {
                throw new SapphireException("Only schedule plans allowed");
            }
            if (!keyId1.isEmpty()) {
                StringBuilder newQueryWhere = new StringBuilder();
                List<String> keyId1List = Arrays.asList(keyId1.split(";"));
                for (String schedulePlanId : keyId1List) {
                    newQueryWhere.append(" OR scheduleplanid = '").append(SafeSQL.encodeForSQL(schedulePlanId, isOracle)).append("'");
                }
                if (newQueryWhere.length() > 0) {
                    queryWhere.append("(").append(newQueryWhere.substring(4)).append(")");
                }
            }
        } else if (filter.equalsIgnoreCase("Scheduled Template")) {
            queryWhere.append("(scheduletemplatesdcid = '").append(SafeSQL.encodeForSQL(sdcId, isOracle)).append("' ");
            queryWhere.append("AND scheduletemplatekeyid1 = '").append(SafeSQL.encodeForSQL(keyId1, isOracle)).append("'");
            if (!keyId2.isEmpty()) {
                queryWhere.append("AND scheduletemplatekeyid2 = '").append(SafeSQL.encodeForSQL(keyId2, isOracle)).append("'");
                if (!keyId3.isEmpty()) {
                    queryWhere.append("AND scheduletemplatekeyid3 = '").append(SafeSQL.encodeForSQL(keyId3, isOracle)).append("'");
                }
            }
            queryWhere.append(")");
        } else if (filter.equalsIgnoreCase("Linked Scheduled Template")) {
            PropertyList linkedScheduledTemplateProps = schedulePlanItemProps.getPropertyListNotNull("linkedscheduledtemplateprops");
            PropertyListCollection foreignKeyCollection = linkedScheduledTemplateProps.getCollectionNotNull("foreignkeycollection");
            String linkedSdcId = linkedScheduledTemplateProps.getProperty("sdcid");
            if (!linkedSdcId.isEmpty()) {
                StringBuilder linkedQueryWhere = new StringBuilder();
                linkedQueryWhere.append("templateflag = 'Y'");
                for (int i = 0; i < foreignKeyCollection.size(); ++i) {
                    PropertyList foreignKeyProps = foreignKeyCollection.getPropertyList(i);
                    String column = foreignKeyProps.getProperty("column");
                    String value = foreignKeyProps.getProperty("value");
                    value = value.replaceAll("\\[sdcid\\]", sdcId);
                    value = value.replaceAll("\\[keyid1\\]", keyId1);
                    value = value.replaceAll("\\[keyid2\\]", keyId2);
                    value = value.replaceAll("\\[keyid3\\]", keyId3);
                    linkedQueryWhere.append(" AND ").append(column).append(" = '").append(SafeSQL.encodeForSQL(value, isOracle)).append("'");
                }
                PropertyList linkedSDCProps = new PropertyList(this.getSDCProcessor().getSDCProperties(linkedSdcId));
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(linkedSdcId);
                sdiRequest.setQueryFrom(linkedSDCProps.getProperty("tableid"));
                sdiRequest.setQueryWhere(linkedQueryWhere.toString());
                sdiRequest.setRequestItem("primary");
                sdiRequest.setShowTemplates(true);
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                DataSet linkedSDIs = sdiData.getDataset("primary");
                StringBuilder newQueryWhere = new StringBuilder();
                for (int i = 0; i < linkedSDIs.getRowCount(); ++i) {
                    String linkedKeyId1 = linkedSDIs.getString(i, linkedSDCProps.getProperty("keycolid1"), "");
                    String string = linkedSDIs.getString(i, linkedSDCProps.getProperty("keycolid2"), "");
                    String linkedKeyId3 = linkedSDIs.getString(i, linkedSDCProps.getProperty("keycolid3"), "");
                    newQueryWhere.append(" OR (scheduletemplatesdcid = '").append(SafeSQL.encodeForSQL(linkedSdcId, isOracle)).append("' ");
                    newQueryWhere.append("AND scheduletemplatekeyid1 = '").append(SafeSQL.encodeForSQL(linkedKeyId1, isOracle)).append("'");
                    if (!string.isEmpty()) {
                        newQueryWhere.append("AND scheduletemplatekeyid2 = '").append(SafeSQL.encodeForSQL(string, isOracle)).append("'");
                        if (!linkedKeyId3.isEmpty()) {
                            newQueryWhere.append("AND scheduletemplatekeyid3 = '").append(SafeSQL.encodeForSQL(linkedKeyId3, isOracle)).append("'");
                        }
                    }
                    newQueryWhere.append(")");
                }
                if (newQueryWhere.length() > 0) {
                    queryWhere.append("(").append(newQueryWhere.substring(4)).append(")");
                }
            }
        } else if (filter.equalsIgnoreCase("Source")) {
            PropertyList sourceProps = schedulePlanItemProps.getPropertyListNotNull("sourceprops");
            String showRandomized = sourceProps.getProperty("showrandomized", "Yes");
            queryWhere.append("(linksdcid = '").append(SafeSQL.encodeForSQL(sdcId, isOracle)).append("' ");
            queryWhere.append("AND linkkeyid1 = '").append(SafeSQL.encodeForSQL(keyId1, isOracle)).append("'");
            if (!keyId2.isEmpty()) {
                queryWhere.append("AND linkkeyid2 = '").append(SafeSQL.encodeForSQL(keyId2, isOracle)).append("'");
                if (!keyId3.isEmpty()) {
                    queryWhere.append("AND linkkeyid3 = '").append(SafeSQL.encodeForSQL(keyId3, isOracle)).append("'");
                }
            }
            if (showRandomized.equals("Only Randomized")) {
                queryWhere.append(" AND (randomizationtype = 'Random' OR randomizationtype = 'Rolling Random')");
            } else if (showRandomized.equals("No")) {
                queryWhere.append(" AND (randomizationtype is null)");
            }
            queryWhere.append(")");
            if (showRandomized.equals("Yes") || showRandomized.equals("Only Randomized")) {
                ArrayList<String> getRandomizedParams = new ArrayList<String>();
                String getRandomizedSql = "SELECT scheduleplanid, scheduleplanitemid FROM schedulerandomization WHERE randomizationsdcid = ? AND randomizationkeyid1 = ?";
                getRandomizedParams.add(sdcId);
                getRandomizedParams.add(keyId1);
                if (!keyId2.isEmpty()) {
                    getRandomizedSql = getRandomizedSql + " AND randomizationkeyid2 = ?";
                    getRandomizedParams.add(keyId2);
                    if (!keyId3.isEmpty()) {
                        getRandomizedSql = getRandomizedSql + " AND randomizationkeyid3 = ?";
                        getRandomizedParams.add(keyId3);
                    }
                }
                DataSet getRandomizedDs = this.getQueryProcessor().getPreparedSqlDataSet(getRandomizedSql, getRandomizedParams.toArray());
                StringBuilder randomizedQueryWhere = new StringBuilder();
                for (int i = 0; i < getRandomizedDs.getRowCount(); ++i) {
                    String schedulePlanId = getRandomizedDs.getString(i, "scheduleplanid", "");
                    String schedulePlanItemId = getRandomizedDs.getString(i, "scheduleplanitemid", "");
                    randomizedQueryWhere.append("OR (scheduleplanid = '").append(SafeSQL.encodeForSQL(schedulePlanId, isOracle)).append("' AND scheduleplanitemid = '").append(SafeSQL.encodeForSQL(schedulePlanItemId, isOracle)).append("')");
                }
                if (randomizedQueryWhere.length() > 0) {
                    if (queryWhere.length() > 0) {
                        queryWhere.append(" OR ");
                    }
                    queryWhere.append(randomizedQueryWhere.substring(3));
                }
            }
        } else if (filter.equalsIgnoreCase("Node")) {
            PropertyList nodeProps = schedulePlanItemProps.getPropertyListNotNull("nodeprops");
            boolean showOwnItems = nodeProps.getProperty("showownitems", "Y").toLowerCase().startsWith("y");
            String getAllNodesSql = "SELECT refsdcid, refkeyid1, refkeyid2, refkeyid3, scheduleplanid, scheduleplannodeid, parentnodeid FROM scheduleplannode";
            DataSet getAllNodesDs = this.getQueryProcessor().getSqlDataSet(getAllNodesSql);
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("refsdcid", sdcId);
            filterMap.put("refkeyid1", keyId1);
            if (!keyId2.isEmpty()) {
                filterMap.put("refkeyid2", keyId2);
                if (!keyId3.isEmpty()) {
                    filterMap.put("refkeyid3", keyId3);
                }
            }
            HashSet<List<String>> primaryNodeKeySet = new HashSet<List<String>>();
            DataSet refNodesDs = getAllNodesDs.getFilteredDataSet(filterMap);
            for (int i = 0; i < refNodesDs.getRowCount(); ++i) {
                String schedulePlanId = refNodesDs.getString(i, "scheduleplanid", "");
                String nodeId = refNodesDs.getString(i, "scheduleplannodeid", "");
                ArrayList<String> nodeKey = new ArrayList<String>();
                nodeKey.add(schedulePlanId);
                nodeKey.add(nodeId);
                primaryNodeKeySet.add(nodeKey);
            }
            HashSet<List<String>> childNodeKeySet = new HashSet<List<String>>();
            childNodeKeySet.addAll(this.addChildNodeKeys(getAllNodesDs, primaryNodeKeySet));
            HashSet<List<String>> nodeKeySet = new HashSet<List<String>>();
            if (showOwnItems) {
                nodeKeySet.addAll(primaryNodeKeySet);
            }
            nodeKeySet.addAll(childNodeKeySet);
            StringBuilder nodeQueryWhere = new StringBuilder();
            for (List list : nodeKeySet) {
                String schedulePlanId = (String)list.get(0);
                String nodeId = (String)list.get(1);
                StringBuilder queryPortion = new StringBuilder();
                queryPortion.append(" OR (scheduleplannodeid = '").append(SafeSQL.encodeForSQL(nodeId, isOracle)).append("' AND scheduleplanid = '").append(SafeSQL.encodeForSQL(schedulePlanId, isOracle)).append("')");
                if (nodeQueryWhere.length() + queryPortion.length() > 4000) {
                    queryWhere.append("(").append(nodeQueryWhere.substring(4)).append(")");
                    queryWheres.add(queryWhere.toString());
                    queryWhere = new StringBuilder();
                    nodeQueryWhere = new StringBuilder();
                }
                nodeQueryWhere.append((CharSequence)queryPortion);
            }
            if (nodeQueryWhere.length() > 0) {
                queryWhere.append("(").append(nodeQueryWhere.substring(4)).append(")");
            }
        } else if (filter.equals("SQL")) {
            PropertyList sqlProps = schedulePlanItemProps.getPropertyListNotNull("sqlprops");
            String queryWhereStr = sqlProps.getProperty("querywhere");
            queryWhereStr = queryWhereStr.replaceAll("\\[sdcid\\]", sdcId);
            queryWhereStr = queryWhereStr.replaceAll("\\[keyid1\\]", keyId1);
            queryWhereStr = queryWhereStr.replaceAll("\\[keyid2\\]", keyId2);
            queryWhereStr = queryWhereStr.replaceAll("\\[keyid3\\]", keyId3);
            queryWhere.append(queryWhereStr);
        } else if (filter.equals("Schedule Group")) {
            String sql = "SELECT scheduleplanid, scheduleplanitemid FROM schedulegroupitem WHERE schedulegroupid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyId1});
            StringBuilder queryWhereFragment = new StringBuilder();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                StringBuilder groupQueryWherePart = new StringBuilder();
                groupQueryWherePart.append(" OR ( scheduleplanid = '").append(SafeSQL.encodeForSQL(ds.getString(i, "scheduleplanid", ""), isOracle)).append("' AND scheduleplanitemid = '").append(SafeSQL.encodeForSQL(ds.getString(i, "scheduleplanitemid", ""), isOracle)).append("' )");
                if (groupQueryWherePart.length() + queryWhereFragment.length() > 4000) {
                    queryWhere.append("(").append(queryWhereFragment.substring(4)).append(")");
                    queryWheres.add(queryWhere.toString());
                    queryWhere = new StringBuilder();
                    queryWhereFragment = new StringBuilder();
                }
                queryWhereFragment.append((CharSequence)groupQueryWherePart);
            }
            if (queryWhereFragment.length() > 0) {
                queryWhere.append("(").append(queryWhereFragment.substring(4)).append(")");
            }
        }
        if (queryWheres.size() == 0 && queryWhere.length() == 0) {
            queryWhere.append("1 = 0");
        }
        if (queryWhere.length() > 0) {
            queryWheres.add(queryWhere.toString());
        }
        return queryWheres;
    }

    private Set<List<String>> addChildNodeKeys(DataSet refNodesDs, Set<List<String>> nodeKeySet) {
        HashSet<List<String>> childNodeKeySet = new HashSet<List<String>>();
        for (List<String> nodeKey : nodeKeySet) {
            String schedulePlanId = nodeKey.get(0);
            String nodeId = nodeKey.get(1);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("scheduleplanid", schedulePlanId);
            filter.put("parentnodeid", nodeId);
            DataSet childNodesDs = refNodesDs.getFilteredDataSet(filter);
            for (int i = 0; i < childNodesDs.getRowCount(); ++i) {
                String childSchedulePlanId = childNodesDs.getString(i, "scheduleplanid", "");
                String childNodeId = childNodesDs.getString(i, "scheduleplannodeid", "");
                ArrayList<String> childNodeKey = new ArrayList<String>();
                childNodeKey.add(childSchedulePlanId);
                childNodeKey.add(childNodeId);
                childNodeKeySet.add(childNodeKey);
            }
        }
        return childNodeKeySet;
    }

    public PropertyList getSelectedNodes(String selectedNodesJsonString) {
        if (selectedNodesJsonString == null) {
            throw new IllegalArgumentException("Selected nodes JSON is null");
        }
        PropertyList selectedNodes = new PropertyList();
        if (!selectedNodesJsonString.isEmpty()) {
            try {
                selectedNodes = new PropertyList(new JSONObject(selectedNodesJsonString));
            }
            catch (JSONException e) {
                this.logger.error("Selected nodes JSON is not valid: " + selectedNodesJsonString, e);
            }
        }
        return selectedNodes;
    }

    public String getDataViewHtml(String elementId, PropertyList element, PageContext pageContext, PropertyList selectedNodes) throws SapphireException {
        if (elementId == null) {
            throw new IllegalArgumentException("Element ID is null");
        }
        if (elementId.isEmpty()) {
            throw new IllegalArgumentException("Element ID is empty");
        }
        if (element == null) {
            throw new IllegalArgumentException("Element is null");
        }
        if (pageContext == null) {
            throw new IllegalArgumentException("Page context is null");
        }
        if (selectedNodes == null) {
            throw new IllegalArgumentException("Selected nodes is null");
        }
        StringBuilder html = new StringBuilder();
        DataSet primary = this.getPrimaryDataSet();
        DataSet planItems = this.getPlanItems(selectedNodes.getCollectionNotNull("nodecollection"), true);
        this.setDefaultStyle(element);
        QueryData queryData = new QueryData("primary", primary);
        HashMap<String, QueryData> queryDataMap = new HashMap<String, QueryData>();
        queryDataMap.put("primary", queryData);
        SDITagInfo sdiInfo = new SDITagInfo(queryDataMap);
        SDIData sdi = new SDIData();
        sdi.setSdcid(this.primaryKeys.getProperty("sdcid"));
        sdi.setRsetid("");
        sdiInfo.setSDIData(sdi);
        sdiInfo.setDataSet(this.getDatasetName(), planItems);
        sdiInfo.setChildTagData(this.getDatasetName(), planItems);
        DataView dataView = new DataView(pageContext, this.getDatasetName(), sdiInfo, "[default]", this.getConnectionid());
        dataView.setElementid(elementId);
        dataView.setElementProperties(element);
        dataView.setSDCId("SchedulePlanItem");
        dataView.setKeyCols(new String[]{"scheduleplanid", "scheduleplanitemid"});
        dataView.setRenderTagsJS(false);
        html.append(dataView.getHtml());
        if (planItems.getRowCount() == 0) {
            html.append(this.getNoRecordsFoundRow(elementId));
        }
        return html.toString();
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    private String getColumnsList(PropertyListCollection columns, PropertyList sdcOrLinkProps, StringBuffer colsToSelect, boolean appendTableName) {
        StringBuilder out = new StringBuilder();
        PropertyListCollection sdcColumns = sdcOrLinkProps.containsKey("columns") ? sdcOrLinkProps.getCollection("columns") : sdcOrLinkProps.getCollection("linkcolumns");
        if (sdcColumns != null && columns.size() > 0) {
            String colId = null;
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                if (colId == null) {
                    if (column.containsKey("columnid")) {
                        colId = "columnid";
                        if (column.getProperty(colId, "").length() != 0) continue;
                        columns.remove(i);
                        --i;
                        continue;
                    }
                    if (column.containsKey("linkcolumnid")) {
                        colId = "linkcolumnid";
                        if (column.getProperty(colId, "").length() != 0) continue;
                        columns.remove(i);
                        --i;
                        continue;
                    }
                    columns.remove(i);
                    --i;
                    continue;
                }
                if (column.containsKey(colId) && column.getProperty(colId, "").length() != 0) continue;
                columns.remove(i);
                --i;
            }
            if (colId == null) {
                colId = "linkcolumnid";
            }
            boolean keyCol1Found = false;
            boolean keyCol2Found = false;
            boolean keyCol3Found = false;
            String keyColId1 = sdcOrLinkProps.containsKey("keycolid1") ? sdcOrLinkProps.getProperty("keycolid1") : sdcOrLinkProps.getProperty("keycolid1");
            String keyColId2 = sdcOrLinkProps.containsKey("keycolid2") ? sdcOrLinkProps.getProperty("keycolid2") : sdcOrLinkProps.getProperty("keycolid2");
            String keyColId3 = sdcOrLinkProps.containsKey("keycolid3") ? sdcOrLinkProps.getProperty("keycolid3") : sdcOrLinkProps.getProperty("keycolid3");
            String tableName = sdcOrLinkProps.getProperty("tableid");
            for (int index = 0; index < columns.size(); ++index) {
                String columnname = columns.getPropertyList(index).getProperty("columnid", "");
                if (columnname.length() <= 0) continue;
                if (columnname.toLowerCase().contains("select ")) {
                    if (out.length() == 0) {
                        out.append(columnname);
                        colsToSelect.append(columnname);
                        continue;
                    }
                    out.append(appendTableName ? ", " : ",").append(columnname);
                    colsToSelect.append(appendTableName ? ", " : ",").append(columnname);
                    continue;
                }
                if (sdcColumns.find(colId, columnname) == null) continue;
                if (columnname.equalsIgnoreCase(keyColId1)) {
                    keyCol1Found = true;
                }
                if (keyColId2.length() > 0 && columnname.equalsIgnoreCase(keyColId2)) {
                    keyCol2Found = true;
                }
                if (keyColId3.length() > 0 && columnname.equalsIgnoreCase(keyColId3)) {
                    keyCol3Found = true;
                }
                if (out.length() == 0) {
                    out.append(columnname);
                    colsToSelect.append(appendTableName ? tableName : "").append(appendTableName ? "." : "").append(columnname);
                    continue;
                }
                out.append(appendTableName ? ", " : ",").append(columnname);
                colsToSelect.append(appendTableName ? ", " : ",").append(appendTableName ? tableName : "").append(appendTableName ? "." : "").append(columnname);
            }
            if (!keyCol1Found) {
                if (out.length() == 0) {
                    out.append(keyColId1);
                    colsToSelect.append(tableName).append(".").append(keyColId1);
                } else {
                    out.append(", ").append(keyColId1);
                    colsToSelect.append(",").append(tableName).append(".").append(keyColId1);
                }
                PropertyList keyCol = new PropertyList();
                keyCol.setId("keycolid1");
                keyCol.setProperty("columnid", keyColId1);
                keyCol.setProperty("mode", "hidden");
                columns.add(keyCol);
            }
            if (keyColId2.length() > 0 && !keyCol2Found) {
                if (out.length() == 0) {
                    out.append(keyColId2);
                    colsToSelect.append(tableName).append(".").append(keyColId2);
                } else {
                    out.append(", ").append(keyColId2);
                    colsToSelect.append(",").append(tableName).append(".").append(keyColId2);
                }
                PropertyList keyCol = new PropertyList();
                keyCol.setId("keycolid2");
                keyCol.setProperty("columnid", keyColId2);
                keyCol.setProperty("mode", "hidden");
                columns.add(keyCol);
            }
            if (keyColId3.length() > 0 && !keyCol3Found) {
                if (out.length() == 0) {
                    out.append(keyColId3);
                    colsToSelect.append(tableName).append(".").append(keyColId3);
                } else {
                    out.append(", ").append(keyColId3);
                    colsToSelect.append(",").append(tableName).append(".").append(keyColId3);
                }
                PropertyList keyCol = new PropertyList();
                keyCol.setId("keycolid3");
                keyCol.setProperty("columnid", keyColId3);
                keyCol.setProperty("mode", "hidden");
                columns.add(keyCol);
            }
        }
        if (out.length() > 0) {
            return out.toString();
        }
        return "";
    }

    private String getNoRecordsFoundRow(String elementId) {
        return "<div id='__" + elementId + "_norows' >" + this.getTranslationProcessor().translate("No records found");
    }

    private void setDefaultStyle(PropertyList element) {
        if (element.getProperty("style").isEmpty()) {
            element.setProperty("style", Maint.MaintStyle.GRIDWITHCHECKBOX.getName());
        }
    }

    private void filterPlanItemsByNodeRestrictions(DataSet planItems, PropertyListCollection restrictionCollection, boolean lock) {
        int i;
        HashSet<String> nodeList = new HashSet<String>();
        HashSet<String> planList = new HashSet<String>();
        if (restrictionCollection != null && !restrictionCollection.isEmpty()) {
            for (i = 0; i < restrictionCollection.size(); ++i) {
                ArrayList<String> getNodeSourcesParams = new ArrayList<String>();
                StringBuilder getNodeSourcesWhereFragment = new StringBuilder();
                PropertyList restrictionProps = restrictionCollection.getPropertyList(i);
                if (restrictionProps.getProperty("id").equals("all")) {
                    return;
                }
                if (!restrictionProps.getPropertyListNotNull("data").getProperty("isplan").equals("Y")) {
                    getNodeSourcesWhereFragment.append(" OR scheduleplannodeid = ?");
                    getNodeSourcesParams.add(restrictionProps.getProperty("id"));
                    if (restrictionProps.getPropertyList("children_d") != null) {
                        HashMap children = (HashMap)restrictionProps.get("children_d");
                        for (Object nodeIdObject : children.values()) {
                            String nodeId = (String)nodeIdObject;
                            getNodeSourcesWhereFragment.append(" OR scheduleplannodeid = ?");
                            getNodeSourcesParams.add(nodeId);
                        }
                    }
                    if (getNodeSourcesWhereFragment.length() <= 0) continue;
                    String getNodeSourcesSql = "SELECT scheduleplannodeid, scheduleplanid, refsdcid, refkeyid1, refkeyid2, refkeyid3 FROM scheduleplannode WHERE " + getNodeSourcesWhereFragment.substring(4);
                    DataSet getNodeSourceDs = this.getQueryProcessor().getPreparedSqlDataSet(getNodeSourcesSql, getNodeSourcesParams.toArray());
                    for (int j = 0; j < getNodeSourceDs.getRowCount(); ++j) {
                        String planId = getNodeSourceDs.getString(j, "scheduleplanid");
                        String nodeId = getNodeSourceDs.getString(j, "scheduleplannodeid", "");
                        nodeList.add(planId + ";" + nodeId);
                    }
                    continue;
                }
                planList.add(restrictionProps.getPropertyListNotNull("data").getProperty("scheduleplanid"));
            }
        } else if (lock) {
            for (i = planItems.getRowCount() - 1; i >= 0; --i) {
                planItems.remove(i);
            }
        }
        if (planItems.getRowCount() > 0) {
            for (i = planItems.getRowCount() - 1; i >= 0; --i) {
                String planId = planItems.getString(i, "scheduleplanid", "");
                String nodeId = planItems.getString(i, "scheduleplannodeid", "");
                String compare = planId + ";" + nodeId;
                if ((planList.isEmpty() || planList.contains(planId)) && (nodeList.isEmpty() || nodeList.contains(compare))) continue;
                planItems.remove(i);
            }
        }
    }

    private DataSet applySchedulePlanSecurity(DataSet planItems) {
        String orderByString;
        HashSet<String> schedulePlanIdSet = new HashSet<String>();
        for (int i = 0; i < planItems.getRowCount(); ++i) {
            String schedulePlanId = planItems.getString(i, "scheduleplanid");
            schedulePlanIdSet.add(schedulePlanId);
        }
        StringBuilder schedulePlanIds = new StringBuilder();
        for (String schedulePlanId : schedulePlanIdSet) {
            schedulePlanIds.append(";").append(schedulePlanId);
        }
        DataSet securePlanItems = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        if (schedulePlanIds.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("SchedulePlan");
            sdiRequest.setKeyid1List(schedulePlanIds.substring(1));
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet plans = sdiData.getDataset("primary");
            for (int i = 0; i < plans.getRowCount(); ++i) {
                String schedulePlanId = plans.getString(i, "scheduleplanid");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("scheduleplanid", schedulePlanId);
                DataSet tmp = planItems.getFilteredDataSet(filter);
                for (int j = 0; j < tmp.getRowCount(); ++j) {
                    securePlanItems.copyRow(tmp, j, 1);
                }
            }
        }
        if ((orderByString = this.element.getProperty("orderby")).isEmpty()) {
            orderByString = "scheduleplanid,scheduleplanitemid";
        }
        securePlanItems.sort(orderByString);
        return securePlanItems;
    }

    public String getRsetId() {
        return this.rsetId;
    }

    public String getUnmanagedSDCs() {
        return OpalUtil.toDelimitedString(this.unmanagedSDCList, ";");
    }

    private void evaluateCMTPolicy(String connectionId, PropertyList element) throws SapphireException {
        PropertyList policy;
        String excludecolumns;
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(connectionId);
        PropertyList cmtpolicy = configurationProcessor.getPolicy("CMTPolicy", "Sapphire Custom");
        if ("Y".equals(cmtpolicy.getProperty("enablechangecontrol")) && (excludecolumns = (policy = configurationProcessor.getPolicy("CMTPolicy", "SchedulePlanItem Custom")).getPropertyListNotNull("primary").getPropertyListNotNull("transferoption").getProperty("excludecolumnlist").trim()).length() > 0) {
            element.setProperty("excludedcolumns", excludecolumns);
            DataSet ds = new QueryProcessor(connectionId).getSqlDataSet("select sdcid from sdc where scheduleableflag = 'Y' order by sdcid");
            if (OpalUtil.isNotEmpty(ds)) {
                block0: for (int i = 0; i < ds.size(); ++i) {
                    String sdcid = ds.getString(i, "sdcid");
                    if (this.unmanagedSDCList.contains(sdcid)) continue;
                    PropertyList sdcCMTPolicy = configurationProcessor.getPolicy("CMTPolicy", sdcid + " Custom");
                    PropertyListCollection collection = sdcCMTPolicy.getCollectionNotNull("sdidatasets");
                    for (int c = 0; c < collection.size(); ++c) {
                        if (!"SchedulePlanItem".equals(collection.getPropertyList(c).getProperty("linksdcid"))) continue;
                        this.unmanagedSDCList.add(sdcid);
                        continue block0;
                    }
                }
            }
        }
    }
}

