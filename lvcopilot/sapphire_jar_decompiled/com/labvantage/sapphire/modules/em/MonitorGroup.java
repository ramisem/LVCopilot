/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.em;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdiapproval.AddSDIApproval;
import com.labvantage.sapphire.admin.ddt.LV_MonitorGroup;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MonitorGroup
extends BaseCustom {
    private static final String DEFAULT_ENABLED = "N";
    private static final String PRIMARY_TABLE = "Primary";
    private static final String DEFAULT_TRANSLATE = "N";
    private static final String DEFAULT_FIRST_DAY_OF_WEEK = "Sunday";
    private final DateTimeUtil dateTimeUtil;
    private final PropertyList monitorGroupPolicy;
    private final String currentUser;
    private final ConnectionInfo connectionInfo;
    private final boolean isStartOfWeekMonday;
    private String parentSdcId;
    private String parentKeyid1;
    private String parentKeyid2;
    private String parentKeyid3;

    public MonitorGroup(String connectionId) throws SapphireException {
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection Id is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection Id is empty");
        }
        this.setConnectionId(connectionId);
        this.dateTimeUtil = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(connectionId));
        this.monitorGroupPolicy = this.getConfigurationProcessor().getPolicy("MonitorGroupPolicy", "Sapphire Custom");
        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(connectionId);
        this.currentUser = this.connectionInfo.getSysuserId();
        PropertyList dateFormatPolicy = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom");
        String firstDayOfWeek = dateFormatPolicy.getProperty("firstdayofweek", DEFAULT_FIRST_DAY_OF_WEEK);
        M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
        this.isStartOfWeekMonday = firstDayOfWeek.equals("User Locale") ? m18NUtil.getNowCalendar().getFirstDayOfWeek() == 2 : firstDayOfWeek.equals("Monday");
    }

    public MonitorGroup(File rakFile, String connectionId) throws SapphireException {
        if (rakFile == null) {
            throw new IllegalArgumentException("RAK file is null");
        }
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection Id is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection Id is empty");
        }
        this.setConnectionId(connectionId);
        this.setRakFile(rakFile);
        this.dateTimeUtil = new DateTimeUtil();
        this.monitorGroupPolicy = this.getConfigurationProcessor().getPolicy("MonitorGroupPolicy", "Sapphire Custom");
        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(connectionId);
        this.currentUser = this.connectionInfo.getSysuserId();
        PropertyList dateFormatPolicy = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom");
        String firstDayOfWeek = dateFormatPolicy.getProperty("firstdayofweek", DEFAULT_FIRST_DAY_OF_WEEK);
        M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
        this.isStartOfWeekMonday = firstDayOfWeek.equals("User Locale") ? m18NUtil.getNowCalendar().getFirstDayOfWeek() == 2 : firstDayOfWeek.equals("Monday");
    }

    public void linkMonitorGroup(String sdcId, String keyId1) throws SapphireException {
        this.linkMonitorGroup(sdcId, keyId1, false);
    }

    public void linkMonitorGroup(String sdcId, String keyId1, boolean copyDownDepartmentalSecurity) throws SapphireException {
        this.linkMonitorGroup(sdcId, keyId1, "", "", copyDownDepartmentalSecurity);
    }

    public void setParent(String sdcId, String keyid1, String keyid2, String keyid3) {
        this.parentSdcId = sdcId;
        this.parentKeyid1 = keyid1;
        this.parentKeyid2 = keyid2;
        this.parentKeyid3 = keyid3;
    }

    public void linkMonitorGroup(String sdcId, String keyId1, String keyId2, String keyId3, boolean copyDownDepartmentalSecurity) throws SapphireException {
        if (sdcId == null) {
            throw new IllegalArgumentException("SDC Id is null");
        }
        if (sdcId.isEmpty()) {
            throw new IllegalArgumentException("SDC Id is empty");
        }
        if (sdcId.contains(";")) {
            throw new IllegalArgumentException("Only single SDC allowed");
        }
        if (keyId1 == null) {
            throw new IllegalArgumentException("Key Id1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new IllegalArgumentException("Key Id1 is empty");
        }
        if (keyId2 == null) {
            throw new IllegalArgumentException("Key Id2 is null");
        }
        if (keyId3 == null) {
            throw new IllegalArgumentException("Key Id3 is null");
        }
        List<Object> keyId2List = new ArrayList();
        List<Object> keyId3List = new ArrayList();
        List<String> keyId1List = Arrays.asList(keyId1.split(";"));
        if (!keyId2.isEmpty()) {
            keyId2List = Arrays.asList(keyId2.split(";"));
            if (keyId1List.size() != keyId2List.size()) {
                throw new RuntimeException("Inconsistent number of keys");
            }
            if (!keyId3.isEmpty()) {
                keyId3List = Arrays.asList(keyId3.split(";"));
                if (keyId1List.size() != keyId3List.size()) {
                    throw new RuntimeException("Inconsistent number of keys");
                }
            }
        }
        if (!this.connectionInfo.hasModule("SampleMonitoring")) {
            return;
        }
        PropertyListCollection sdcCollection = this.monitorGroupPolicy.getCollectionNotNull("sdccollection");
        ArrayList monitorGroupSDIs = new ArrayList();
        boolean isMonitorGroupDepartMental = this.getSDCProcessor().getProperty("LV_MonitorGroup", "accesscontrolledflag").startsWith("D");
        boolean isTargetSdcDepartmental = this.getSDCProcessor().getProperty(sdcId, "accesscontrolledflag").startsWith("D");
        if (!(!copyDownDepartmentalSecurity || isMonitorGroupDepartMental && isTargetSdcDepartmental)) {
            copyDownDepartmentalSecurity = false;
        }
        for (int i = 0; i < sdcCollection.size(); ++i) {
            boolean monitorGroupsEnabled;
            PropertyList policySdcProps = sdcCollection.getPropertyList(i);
            if (!policySdcProps.getProperty("sdcid").equals(sdcId) || !(monitorGroupsEnabled = policySdcProps.getProperty("enabled", "N").toLowerCase().startsWith("y"))) continue;
            Set<String> primaryColumnList = this.createGroupByColumnList(policySdcProps.getCollectionNotNull("groupbycolumncollection"));
            primaryColumnList.addAll(this.createPrimaryLabelColumnList(policySdcProps.getCollectionNotNull("columncollection")));
            Map<String, String> filterByColumnMap = this.createFilterByColumnMap(policySdcProps.getCollectionNotNull("filtercolumncollection"));
            String referenceDateColumn = policySdcProps.getProperty("refdatecolumnid");
            String monitorGroupColumnId = this.findLinkToMonitorGroup(sdcId, policySdcProps.getProperty("monitorgroupcolumnid"));
            if (primaryColumnList.isEmpty() || monitorGroupColumnId.isEmpty()) continue;
            Calendar referenceDate = Calendar.getInstance();
            PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
            String tableId = sdcProps.getProperty("tableid");
            String keyColumnId1 = sdcProps.getProperty("keycolid1");
            String keyColumnId2 = sdcProps.getProperty("keycolid2");
            String keyColumnId3 = sdcProps.getProperty("keycolid3");
            String tableAlis = "p";
            String selectFragment = this.createSelectFragment(primaryColumnList, referenceDateColumn, filterByColumnMap, tableAlis, sdcId);
            String whereFragment = this.createWhereFragment(keyColumnId1, keyColumnId2, keyColumnId3, filterByColumnMap, tableAlis);
            String getPrimarySql = "SELECT " + selectFragment + " FROM " + tableId + " " + tableAlis + " WHERE " + whereFragment;
            HashMap<String, List<List<String>>> monitorGroupSdiMap = new HashMap<String, List<List<String>>>();
            for (int j = 0; j < keyId1List.size(); ++j) {
                DataSet primaryDs;
                ArrayList<String> params = new ArrayList<String>();
                StringBuilder monitorGroupBy = new StringBuilder();
                String primaryKeyId1 = keyId1List.get(j);
                String primaryKeyId2 = "";
                String primaryKeyId3 = "";
                String securityDepartment = "";
                String securityUser = "";
                params.add(primaryKeyId1);
                if (!keyColumnId2.isEmpty()) {
                    primaryKeyId2 = (String)keyId2List.get(j);
                    params.add(primaryKeyId2);
                    if (!keyColumnId3.isEmpty()) {
                        primaryKeyId3 = (String)keyId3List.get(j);
                        params.add(primaryKeyId3);
                    }
                }
                ArrayList<String> sdiKey = new ArrayList<String>();
                sdiKey.add(sdcId);
                sdiKey.add(primaryKeyId1);
                sdiKey.add(primaryKeyId2);
                sdiKey.add(primaryKeyId3);
                if (monitorGroupSDIs.contains(sdiKey) || (primaryDs = this.getQueryProcessor().getPreparedSqlDataSet(getPrimarySql, params.toArray())).getRowCount() <= 0) continue;
                monitorGroupSDIs.add(sdiKey);
                for (String groupByColumn : primaryColumnList) {
                    monitorGroupBy.append(";").append(primaryDs.getValue(0, groupByColumn, ""));
                }
                if (!referenceDateColumn.isEmpty()) {
                    Calendar date = primaryDs.getCalendar(0, referenceDateColumn);
                    if (date != null) {
                        referenceDate = date;
                    } else {
                        this.logger.warn("Reference date column " + referenceDateColumn + " nominated but value is empty. Defaulting to now");
                    }
                }
                String monitorGroupId = "";
                if (monitorGroupBy.length() > 0) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("SELECT mg.monitorgroupid");
                    if (copyDownDepartmentalSecurity) {
                        sql.append(", mg.securitydepartment, mg.securityuser");
                    }
                    sql.append(" FROM monitorgroup mg WHERE mg.monitorgroupby = ? AND mg.startdt <= ? AND mg.enddt > ? AND (mg.monitorgroupstatus <> '").append("Cancelled").append("' AND mg.monitorgroupstatus <> '").append("Rejected").append("' AND mg.monitorgroupstatus <> '").append("Released").append("')");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{monitorGroupBy.substring(1), new Timestamp(referenceDate.getTimeInMillis()), new Timestamp(referenceDate.getTimeInMillis())});
                    if (ds.getRowCount() > 0) {
                        monitorGroupId = ds.getString(0, "monitorgroupid", "");
                        if (copyDownDepartmentalSecurity) {
                            securityDepartment = ds.getString(0, "securitydepartment", "(null)");
                            securityUser = ds.getString(0, "securityuser", "(null)");
                        }
                    }
                }
                if (monitorGroupId.isEmpty()) {
                    String frequencyString = policySdcProps.getProperty("newgroupfrequency");
                    String frequencyUnits = policySdcProps.getProperty("newgroupfrequencyunits");
                    String frequencyOffsetString = policySdcProps.getProperty("frequencyoffset");
                    String frequencyOffsetUnits = policySdcProps.getProperty("frequencyoffsetunits");
                    int frequency = Integer.parseInt(frequencyString);
                    int frequencyOffset = 0;
                    if (!frequencyOffsetString.isEmpty()) {
                        frequencyOffset = Integer.parseInt(frequencyOffsetString);
                    }
                    Calendar startDate = this.getStartDate(referenceDate, frequencyUnits, frequencyOffset, frequencyOffsetUnits);
                    while (referenceDate.toInstant().isBefore(startDate.toInstant()) && frequency > 0 && !frequencyUnits.isEmpty()) {
                        this.addToDate(startDate, -frequency, frequencyUnits);
                    }
                    Calendar endDate = this.getEndDate(startDate, frequency, frequencyUnits, frequencyOffset, frequencyOffsetUnits);
                    String monitorGroupType = policySdcProps.getProperty("monitorgrouptype", "");
                    monitorGroupId = this.createNewMonitorGroup(startDate, endDate, monitorGroupBy.substring(1), monitorGroupType, policySdcProps, primaryDs);
                    if (copyDownDepartmentalSecurity) {
                        String sql = "SELECT securitydepartment, securityuser  FROM monitorgroup mg WHERE mg.monitorgroupid = ?";
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{monitorGroupId});
                        if (ds.getRowCount() > 0) {
                            securityDepartment = ds.getString(0, "securitydepartment", "(null)");
                            securityUser = ds.getString(0, "securityuser", "(null)");
                        }
                    }
                }
                ArrayList<String> keyList = new ArrayList<String>();
                keyList.add(primaryKeyId1);
                keyList.add(primaryKeyId2);
                keyList.add(primaryKeyId3);
                keyList.add(securityDepartment);
                keyList.add(securityUser);
                ArrayList monitorGroupSdiList = (ArrayList)monitorGroupSdiMap.get(monitorGroupId);
                if (monitorGroupSdiList == null) {
                    monitorGroupSdiList = new ArrayList();
                }
                monitorGroupSdiList.add(keyList);
                monitorGroupSdiMap.put(monitorGroupId, monitorGroupSdiList);
            }
            this.linkSDIToMonitorGroup(sdcId, monitorGroupColumnId, monitorGroupSdiMap, copyDownDepartmentalSecurity);
        }
    }

    private Set<String> createPrimaryLabelColumnList(PropertyListCollection columnCollection) {
        HashSet<String> primaryColumnList = new HashSet<String>();
        for (int i = 0; i < columnCollection.size(); ++i) {
            String columnId;
            PropertyList columnProps = columnCollection.getPropertyList(i);
            String table = columnProps.getProperty("table");
            if (!table.equals(PRIMARY_TABLE) || (columnId = columnProps.getProperty("columnid")).isEmpty()) continue;
            primaryColumnList.add(columnId);
        }
        return primaryColumnList;
    }

    private String getMonitorGroupLabel(PropertyList policySdcProps, DataSet primaryDs, DataSet monitorGroupDs) {
        PropertyList tokenValues = new PropertyList();
        tokenValues.setProperty("currentuser", this.currentUser);
        String monitorGroupLabelExpression = policySdcProps.getProperty("monitorgrouplabel");
        PropertyListCollection columnCollection = policySdcProps.getCollectionNotNull("columncollection");
        for (int i = 0; i < columnCollection.size(); ++i) {
            boolean translate;
            PropertyList labelColumnProps = columnCollection.getPropertyList(i);
            String identifier = labelColumnProps.getProperty("id");
            String table = labelColumnProps.getProperty("table");
            String columnId = labelColumnProps.getProperty("columnid");
            String columnValue = table.equals(PRIMARY_TABLE) ? this.getLabelColumnValue(primaryDs, labelColumnProps, columnId) : this.getLabelColumnValue(monitorGroupDs, labelColumnProps, columnId);
            String displayValueList = labelColumnProps.getProperty("displayvalue");
            if (!displayValueList.isEmpty()) {
                columnValue = SDITagUtil.getDisplayValue(columnValue, displayValueList);
            }
            if (translate = labelColumnProps.getProperty("translate", "N").equals("Y")) {
                columnValue = this.getTranslationProcessor().translate(columnValue);
            }
            tokenValues.setProperty(identifier, columnValue);
        }
        String label = this.replaceTokens(monitorGroupLabelExpression, tokenValues);
        return label;
    }

    private String getLabelColumnValue(DataSet ds, PropertyList labelColumnProps, String columnId) {
        String columnValue = "";
        if (ds.isValidColumn(columnId)) {
            int columnType = ds.getColumnType(columnId);
            if (columnType == 2) {
                String dateFormat = labelColumnProps.getProperty("dateformat");
                Calendar c = ds.getCalendar(0, columnId);
                if (c != null) {
                    DateFormat dateFormatter = this.dateTimeUtil.getDefaultDateFormat();
                    if (!dateFormat.isEmpty()) {
                        dateFormatter = new SimpleDateFormat(dateFormat);
                    }
                    columnValue = dateFormatter.format(c.getTime());
                }
            } else {
                columnValue = ds.getValue(0, columnId, "");
            }
        }
        return columnValue;
    }

    private String replaceTokens(String evalExpression, PropertyList tokenValues) {
        StringBuffer returnValue = new StringBuffer();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(evalExpression);
        while (matcher.find()) {
            String propertyId = matcher.group(1);
            String tokenValue = tokenValues.getProperty(propertyId);
            if (tokenValue.isEmpty()) continue;
            matcher.appendReplacement(returnValue, "");
            returnValue.append(tokenValue);
        }
        matcher.appendTail(returnValue);
        return returnValue.toString();
    }

    private String createWhereFragment(String keyColumnId1, String keyColumnId2, String keyColumnId3, Map<String, String> filterByColumnMap, String tableAlis) {
        StringBuilder whereFragment = new StringBuilder();
        whereFragment.append(tableAlis).append(".").append(keyColumnId1).append(" = ?");
        if (!keyColumnId2.isEmpty()) {
            whereFragment.append(" AND ").append(tableAlis).append(".").append(keyColumnId2).append(" = ?");
            if (!keyColumnId3.isEmpty()) {
                whereFragment.append(" AND ").append(tableAlis).append(".").append(keyColumnId3).append(" = ?");
            }
        }
        for (Map.Entry<String, String> entry : filterByColumnMap.entrySet()) {
            String columnId = entry.getKey();
            String columnValue = entry.getValue();
            whereFragment.append(" AND ").append(tableAlis).append(".").append(columnId);
            if (!columnValue.isEmpty()) {
                whereFragment.append(" = '").append(columnValue).append("'");
                continue;
            }
            whereFragment.append(" IS NULL");
        }
        return whereFragment.toString();
    }

    private Map<String, String> createFilterByColumnMap(PropertyListCollection filterColumnCollection) {
        if (filterColumnCollection == null) {
            throw new IllegalArgumentException("Filter by column collection is null");
        }
        HashMap<String, String> filterByColumnMap = new HashMap<String, String>();
        for (int j = 0; j < filterColumnCollection.size(); ++j) {
            PropertyList columnProps = filterColumnCollection.getPropertyList(j);
            String columnId = columnProps.getProperty("columnid");
            String columnValue = columnProps.getProperty("columnvalue");
            if (columnId.isEmpty()) continue;
            filterByColumnMap.put(columnId, columnValue);
        }
        return filterByColumnMap;
    }

    private Set<String> createGroupByColumnList(PropertyListCollection groupByColumnCollection) {
        if (groupByColumnCollection == null) {
            throw new IllegalArgumentException("Group by column collection is null");
        }
        HashSet<String> groupByColumnList = new HashSet<String>();
        for (int j = 0; j < groupByColumnCollection.size(); ++j) {
            PropertyList columnProps = groupByColumnCollection.getPropertyList(j);
            String columnId = columnProps.getProperty("columnid");
            if (columnId.isEmpty()) continue;
            groupByColumnList.add(columnId);
        }
        return groupByColumnList;
    }

    private void linkSDIToMonitorGroup(String sdcId, String monitorGroupColumnId, Map<String, List<List<String>>> monitorGroupSdiMap, boolean copyDownDepartmentalSecurity) throws ActionException {
        if (sdcId == null) {
            throw new IllegalArgumentException("SDC Id is null");
        }
        if (monitorGroupColumnId == null) {
            throw new IllegalArgumentException("Monitor group column id is null");
        }
        if (monitorGroupSdiMap == null) {
            throw new IllegalArgumentException("Monitor group SDI map is null");
        }
        StringBuilder keyId1s = new StringBuilder();
        StringBuilder keyId2s = new StringBuilder();
        StringBuilder keyId3s = new StringBuilder();
        StringBuilder monitorGroupIds = new StringBuilder();
        StringBuilder securityUsers = new StringBuilder();
        StringBuilder securityDepartments = new StringBuilder();
        for (Map.Entry<String, List<List<String>>> monitorGroupMapEntry : monitorGroupSdiMap.entrySet()) {
            String monitorGroupId = monitorGroupMapEntry.getKey();
            List<List<String>> monitorGroupSdiList = monitorGroupMapEntry.getValue();
            for (List<String> keyList : monitorGroupSdiList) {
                String keyId1 = keyList.get(0);
                String keyId2 = keyList.get(1);
                String keyId3 = keyList.get(2);
                String securityDepartment = keyList.get(3);
                String securityUser = keyList.get(4);
                keyId1s.append(";").append(keyId1);
                if (!keyId2.isEmpty()) {
                    keyId2s.append(";").append(keyId2);
                    if (!keyId3.isEmpty()) {
                        keyId3s.append(";").append(keyId3);
                    }
                }
                monitorGroupIds.append(";").append(monitorGroupId);
                securityDepartments.append(";").append(securityDepartment);
                securityUsers.append(";").append(securityUser);
            }
        }
        if (keyId1s.length() > 0) {
            PropertyList editSDIProps = new PropertyList();
            editSDIProps.setProperty("sdcid", sdcId);
            editSDIProps.setProperty("keyid1", keyId1s.substring(1));
            if (keyId2s.length() > 0) {
                editSDIProps.setProperty("keyid2", keyId2s.substring(1));
                if (keyId3s.length() > 0) {
                    editSDIProps.setProperty("keyid3", keyId3s.substring(1));
                }
            }
            if (copyDownDepartmentalSecurity) {
                editSDIProps.setProperty("securitydepartment", securityDepartments.substring(1));
                editSDIProps.setProperty("securityuser", securityUsers.substring(1));
            }
            editSDIProps.setProperty(monitorGroupColumnId, monitorGroupIds.substring(1));
            this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
        }
    }

    private String findLinkToMonitorGroup(String sdcId, String policyOverride) {
        if (sdcId == null) {
            throw new IllegalArgumentException("SDC Id is null");
        }
        if (policyOverride == null) {
            throw new IllegalArgumentException("Link to monitor group policy override is null");
        }
        String monitorGroupColumnId = policyOverride;
        if (monitorGroupColumnId.isEmpty()) {
            DataSet linksData = this.getSDCProcessor().getLinksData(sdcId);
            for (int j = 0; j < linksData.getRowCount(); ++j) {
                String linkSdcId = linksData.getString(j, "linksdcid", "");
                if (!linkSdcId.equals("LV_MonitorGroup")) continue;
                monitorGroupColumnId = linksData.getString(j, "sdccolumnid", "");
                break;
            }
        }
        return monitorGroupColumnId;
    }

    public String createMonitorGroup(String monitorGroupType, String monitorGroupBy, String monitorGroupLabel, String scheduleGroupId) throws SapphireException {
        if (monitorGroupType == null || monitorGroupType.isEmpty()) {
            throw new SapphireException("Monitor Group Type is mandatory!");
        }
        if (monitorGroupLabel == null || monitorGroupLabel.isEmpty()) {
            throw new SapphireException("Monitor Group Label is mandatory!");
        }
        if (monitorGroupBy == null || monitorGroupBy.isEmpty()) {
            throw new SapphireException("Monitor Group Group By is mandatory!");
        }
        String newMonitorGroupId = this.createNewMonitorGroup(Calendar.getInstance(), Calendar.getInstance(), monitorGroupBy, monitorGroupType, null, null, true, monitorGroupLabel, scheduleGroupId);
        return newMonitorGroupId;
    }

    private String createNewMonitorGroup(Calendar startDate, Calendar endDate, String monitorGroupBy, String monitorGroupType, PropertyList policySdcProps, DataSet primaryDs) throws SapphireException {
        return this.createNewMonitorGroup(startDate, endDate, monitorGroupBy, monitorGroupType, policySdcProps, primaryDs, false, null, null);
    }

    private String createNewMonitorGroup(Calendar startDate, Calendar endDate, String monitorGroupBy, String monitorGroupType, PropertyList policySdcProps, DataSet primaryDs, boolean isAdHoc, String monitorGroupLabel, String scheduleGroupId) throws SapphireException {
        DateFormat df = this.dateTimeUtil.getDefaultDateFormat();
        DataSet monitorGroupDs = new DataSet(this.connectionInfo);
        monitorGroupDs.addColumn("startdt", 2);
        monitorGroupDs.addColumn("enddt", 2);
        monitorGroupDs.addColumn("monitorgroupby", 0);
        int row = monitorGroupDs.addRow();
        monitorGroupDs.setDate(row, "startdt", startDate);
        monitorGroupDs.setDate(row, "enddt", endDate);
        monitorGroupDs.setDate(row, "monitorgroupby", monitorGroupBy);
        if (monitorGroupLabel == null || monitorGroupLabel.isEmpty()) {
            monitorGroupLabel = this.getMonitorGroupLabel(policySdcProps, primaryDs, monitorGroupDs);
        }
        PropertyList monitorGroupTypeSettings = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupType, this.getConfigurationProcessor());
        PropertyList securityProps = monitorGroupTypeSettings.getPropertyListNotNull("securityprops");
        boolean copySecurityDepartment = securityProps.getProperty("copysecuritydepartment", "N").startsWith("Y");
        String copyDepartmentalMode = "";
        copyDepartmentalMode = isAdHoc ? securityProps.getProperty("copyfromadhoc") : securityProps.getProperty("copyfrom");
        boolean isDepartmental = this.getSDCProcessor().getProperty("LV_MonitorGroup", "accesscontrolledflag", "").equals("D");
        PropertyList addMonitorGroupProps = new PropertyList();
        boolean emptifySecurityDepartment = false;
        if (isDepartmental && copySecurityDepartment && !copyDepartmentalMode.isEmpty()) {
            String securityDepartment = "";
            String securityUser = "";
            if (copyDepartmentalMode.equals("parent")) {
                boolean isParentDepartmental;
                if (this.parentSdcId != null && !this.parentSdcId.isEmpty() && (isParentDepartmental = this.getSDCProcessor().getProperty(this.parentSdcId, "accesscontrolledflag", "").equals("D"))) {
                    DataSet parentSDIds;
                    ArrayList<String> params = new ArrayList<String>();
                    String table = this.getSDCProcessor().getProperty(this.parentSdcId, "tableid");
                    String keyCol1 = this.getSDCProcessor().getProperty(this.parentSdcId, "keycolid1");
                    String keyCol2 = this.getSDCProcessor().getProperty(this.parentSdcId, "keycolid2");
                    String keyCol3 = this.getSDCProcessor().getProperty(this.parentSdcId, "keycolid3");
                    params.add(this.parentKeyid1);
                    String sql = "SELECT securitydepartment, securityuser FROM " + table + " WHERE " + keyCol1 + "=? ";
                    if (!keyCol2.equals("") && !this.parentKeyid2.equals("")) {
                        sql = sql + " AND " + keyCol2 + "= ? ";
                        params.add(this.parentKeyid2);
                    }
                    if (!keyCol3.equals("") && !this.parentKeyid3.equals("")) {
                        sql = sql + " AND " + keyCol3 + "= ? ";
                        params.add(this.parentKeyid3);
                    }
                    if ((parentSDIds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params.toArray())).getRowCount() > 0) {
                        securityDepartment = parentSDIds.getString(0, "securitydepartment", "");
                        securityUser = parentSDIds.getString(0, "securityuser", "");
                        if (securityDepartment.isEmpty()) {
                            emptifySecurityDepartment = true;
                        }
                    }
                }
            } else if (copyDepartmentalMode.equals("firstsdi") && !isAdHoc) {
                String sdcId = policySdcProps.getProperty("sdcid");
                boolean isPrimaryDepartmental = this.getSDCProcessor().getProperty(sdcId, "accesscontrolledflag", "").equals("D");
                if (isPrimaryDepartmental) {
                    securityDepartment = primaryDs.getString(0, "securitydepartment", "");
                    securityUser = primaryDs.getString(0, "securityuser", "");
                    if (securityDepartment.isEmpty() || securityUser.isEmpty()) {
                        emptifySecurityDepartment = true;
                    }
                }
            }
            if (!securityDepartment.isEmpty()) {
                addMonitorGroupProps.setProperty("securitydepartment", securityDepartment);
                if (!securityUser.isEmpty()) {
                    addMonitorGroupProps.setProperty("securityuser", securityUser);
                }
            }
        }
        addMonitorGroupProps.setProperty("sdcid", "LV_MonitorGroup");
        addMonitorGroupProps.setProperty("startdt", df.format(startDate.getTime()));
        addMonitorGroupProps.setProperty("enddt", df.format(endDate.getTime()));
        addMonitorGroupProps.setProperty("monitorgroupby", monitorGroupBy);
        addMonitorGroupProps.setProperty("monitorgrouplabel", monitorGroupLabel);
        addMonitorGroupProps.setProperty("monitorgrouptype", monitorGroupType);
        if (scheduleGroupId != null && !scheduleGroupId.isEmpty()) {
            addMonitorGroupProps.setProperty("schedulegroupid", scheduleGroupId);
        }
        this.getActionProcessor().processAction("AddSDI", "1", addMonitorGroupProps);
        String newMonitorGroup = addMonitorGroupProps.getProperty("newkeyid1");
        if (emptifySecurityDepartment) {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_MonitorGroup");
            editProps.setProperty("keyid1", newMonitorGroup);
            editProps.setProperty("securitydepartment", "(null)");
            editProps.setProperty("securityuser", "(null)");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
        this.addApprovalTypes(newMonitorGroup, monitorGroupType);
        return newMonitorGroup;
    }

    private void addApprovalTypes(String newMonitorGroup, String monitorGroupType) throws SapphireException {
        PropertyList monitorGroupTypeSettings = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupType, this.getConfigurationProcessor());
        PropertyListCollection approvalTypesCollection = monitorGroupTypeSettings.getCollectionNotNull("approvaltypes");
        StringBuilder approvalTypes = new StringBuilder();
        for (int i = 0; i < approvalTypesCollection.size(); ++i) {
            PropertyList approvalTypeProperties = approvalTypesCollection.getPropertyList(i);
            String approvalTypeId = approvalTypeProperties.getProperty("approvaltypeid", "");
            if (approvalTypeId.length() <= 0) continue;
            approvalTypes.append(";").append(approvalTypeId);
        }
        if (approvalTypes.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_MonitorGroup");
            props.setProperty("keyid1", newMonitorGroup);
            props.setProperty("approvaltypeid", approvalTypes.substring(1));
            this.getActionProcessor().processActionClass(AddSDIApproval.class.getName(), props);
        }
    }

    private Calendar getStartDate(Calendar referenceDate, String frequencyUnits, int frequencyOffset, String frequencyOffsetUnits) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date is null");
        }
        if (frequencyUnits == null) {
            throw new IllegalArgumentException("Frequency units is null");
        }
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(referenceDate.getTime());
        if (frequencyUnits.toLowerCase().startsWith("h")) {
            startDate.set(12, 0);
            startDate.set(13, 0);
        } else if (frequencyUnits.toLowerCase().startsWith("d")) {
            startDate.set(11, 0);
            startDate.set(12, 0);
            startDate.set(13, 0);
        } else if (frequencyUnits.toLowerCase().startsWith("w")) {
            if (this.isStartOfWeekMonday) {
                startDate.set(7, 2);
            } else {
                startDate.add(7, -(startDate.get(7) - 1));
            }
            startDate.set(11, 0);
            startDate.set(12, 0);
            startDate.set(13, 0);
        } else if (frequencyUnits.toLowerCase().startsWith("m")) {
            startDate.set(5, 1);
            startDate.set(11, 0);
            startDate.set(12, 0);
            startDate.set(13, 0);
        } else if (frequencyUnits.toLowerCase().startsWith("y")) {
            startDate.set(2, 0);
            startDate.set(5, 1);
            startDate.set(11, 0);
            startDate.set(12, 0);
            startDate.set(13, 0);
        } else {
            throw new IllegalArgumentException("Unknown frequency unit: " + frequencyUnits);
        }
        if (frequencyOffset != 0 && !frequencyUnits.toLowerCase().startsWith("h")) {
            this.addToDate(startDate, frequencyOffset, frequencyOffsetUnits);
        }
        return startDate;
    }

    private Calendar getEndDate(Calendar referenceDate, int frequency, String frequencyUnits, int frequencyOffset, String frequencyOffsetUnits) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date is null");
        }
        if (frequency < 0) {
            throw new IllegalArgumentException("Frequency is negative: " + frequency);
        }
        if (frequencyUnits == null) {
            throw new IllegalArgumentException("Frequency units is null");
        }
        GregorianCalendar endDate = new GregorianCalendar();
        endDate.setTime(referenceDate.getTime());
        this.addToDate(endDate, frequency, frequencyUnits);
        return endDate;
    }

    private void addToDate(Calendar date, int amount, String units) {
        if (units.toLowerCase().startsWith("h")) {
            date.add(11, amount);
        } else if (units.toLowerCase().startsWith("d")) {
            date.add(5, amount);
        } else if (units.toLowerCase().startsWith("w")) {
            date.add(3, amount);
        } else if (units.toLowerCase().startsWith("m")) {
            date.add(2, amount);
        } else if (units.toLowerCase().startsWith("y")) {
            date.add(1, amount);
        } else {
            throw new IllegalArgumentException("Unknown unit: " + units);
        }
    }

    private String createSelectFragment(Set<String> groupByColumnList, String referenceDateColumn, Map<String, String> filterByColumnMap, String tableAlias, String sdcId) {
        if (groupByColumnList == null) {
            throw new IllegalArgumentException("Group by column list is null");
        }
        if (referenceDateColumn == null) {
            throw new IllegalArgumentException("Reference date column is null");
        }
        if (tableAlias == null) {
            throw new IllegalArgumentException("Table alias is null");
        }
        if (sdcId == null) {
            throw new IllegalArgumentException("SDC Id is null");
        }
        String returnValue = "";
        StringBuilder columns = new StringBuilder();
        for (String groupByColumn : groupByColumnList) {
            columns.append(", ").append(tableAlias).append(".").append(groupByColumn);
        }
        for (String filterByColumn : filterByColumnMap.keySet()) {
            columns.append(", ").append(tableAlias).append(".").append(filterByColumn);
        }
        if (!referenceDateColumn.isEmpty() && !groupByColumnList.contains(referenceDateColumn)) {
            columns.append(", ").append(tableAlias).append(".").append(referenceDateColumn);
        }
        if (this.getSDCProcessor().getProperty(sdcId, "accesscontrolledflag", "").equals("D")) {
            columns.append(", ").append(tableAlias).append(".").append("securitydepartment");
            columns.append(", ").append(tableAlias).append(".").append("securityuser");
        }
        if (columns.length() > 1) {
            returnValue = columns.substring(2);
        }
        return returnValue;
    }
}

