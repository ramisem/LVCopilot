/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.ElementInfo;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Audit {
    private String LABVANTAGE_CVS_ID = "$Revision: 102603 $";

    public static String getSelectClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String auditTablePrefix, String tracelogPrefix, QueryProcessor queryprocessor) throws Exception {
        return "";
    }

    public static String getFromClause(ElementInfo elementInfo, String auditTablePrefix, String realTablePrefix) throws Exception {
        StringBuffer fromClause = new StringBuffer();
        String auditTableId = null;
        auditTableId = elementInfo.getAuditTableId();
        String tableId = elementInfo.getElementTableId();
        LinkedList keyColumns = elementInfo.getKeyColumns();
        int keyColumnCount = keyColumns.size();
        PropertyList advProps = elementInfo.getElementProperties().getPropertyList("advancedconfig");
        String advFrom = "";
        if (advProps != null && elementInfo.isInAdvancedMode()) {
            advFrom = advProps.getProperty("fromclause", "").trim();
        }
        if (advFrom.startsWith(",")) {
            advFrom = advFrom.substring(1);
        }
        if (advFrom.endsWith(",")) {
            advFrom = advFrom.substring(0, advFrom.length() - 1);
        }
        fromClause.append("FROM ").append(auditTableId.toUpperCase() + " " + auditTablePrefix + " ");
        if (advFrom.length() > 0) {
            fromClause.append(" , " + advFrom + " ");
        } else {
            fromClause.append(" LEFT OUTER JOIN ").append(tableId).append(" ").append(realTablePrefix).append(" ON ");
            for (int count = 0; count < keyColumnCount; ++count) {
                if (count > 0) {
                    fromClause.append(" AND ");
                }
                String keyColumnId = (String)keyColumns.get(count);
                fromClause.append(realTablePrefix).append(".").append(keyColumnId).append("=").append(auditTablePrefix).append(".").append(keyColumnId);
            }
        }
        return fromClause.toString();
    }

    public static String getWhereClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String auditTablePrefix, QueryProcessor qp, PropertyList pageData, HashMap extraFilter) throws SapphireException {
        StringBuffer whereClause = new StringBuffer();
        StringBuffer finalWhereClause = new StringBuffer(" ");
        String keyColumnId = null;
        String sdcIdTopLevel = null;
        String sdcIdCurrent = null;
        String parentId = null;
        String auditTableIdCurrent = null;
        String inclausePropValue = null;
        LinkedList keyColumns = null;
        int keyColumnCount = 0;
        try {
            keyColumns = elementInfo.getKeyColumns();
            keyColumnCount = keyColumns.size();
            sdcIdTopLevel = pageData.getProperty("sdcid");
            auditTableIdCurrent = elementInfo.getElementProperties().getProperty("tableid");
            sdcIdCurrent = elementInfo.getTableSdcid();
            parentId = elementInfo.getElementProperties().getProperty("parentid");
            Logger.logInfo("Audit", "getWhereClause(): " + elementInfo.getElementid());
            PropertyList advProps = elementInfo.getElementProperties().getPropertyList("advancedconfig");
            String advWhere = "";
            if (advProps != null && elementInfo.isInAdvancedMode()) {
                advWhere = advProps.getProperty("whereclause", "").trim();
            }
            if (advWhere.length() > 0) {
                advWhere = OpalUtil.searchAndReplaceTokens(advWhere, OpalUtil.getKeywordTokens(advWhere), OpalUtil.getRequestParameters(pageData), false);
                advWhere = advWhere.replaceAll(";", "', '");
                whereClause.append(advWhere);
            } else {
                int count;
                if ((parentId == null || "".equals(parentId)) && sdcIdCurrent != null && sdcIdCurrent.trim().length() > 0) {
                    Logger.logInfo("Audit", "getWhereClause(): Top Level SDC");
                    for (count = 0; count < keyColumnCount; ++count) {
                        keyColumnId = (String)keyColumns.get(count);
                        inclausePropValue = Audit.getInclauseString(pageData, "keyid" + (count + 1));
                        if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                        whereClause.append(auditTablePrefix).append(".");
                        whereClause.append(keyColumnId).append(" IN ( ");
                        whereClause.append(inclausePropValue);
                        whereClause.append(")  ");
                        if (count + 1 >= keyColumnCount) continue;
                        whereClause.append("  AND ");
                    }
                } else if (elementInfo.isSDIDetailTable()) {
                    SDCProcessor sdcProcessor = new SDCProcessor(qp.getConnectionid());
                    PropertyList topSDCProps = sdcProcessor.getProperties(sdcIdTopLevel);
                    int sdcKeyColCount = Integer.parseInt(topSDCProps.getProperty("keycolumns"));
                    Logger.logInfo("Audit", "getWhereClause(): SDI Detail Table");
                    boolean needAnd = false;
                    for (int count2 = 0; count2 < keyColumnCount; ++count2) {
                        keyColumnId = (String)keyColumns.get(count2);
                        if (sdcKeyColCount < 2 && "keyid2".equalsIgnoreCase(keyColumnId) || sdcKeyColCount < 3 && "keyid3".equalsIgnoreCase(keyColumnId) || (inclausePropValue = Audit.getInclauseString(pageData, keyColumnId)) == null || inclausePropValue.trim().length() <= 0) continue;
                        if (needAnd) {
                            whereClause.append("  AND ");
                        }
                        whereClause.append(auditTablePrefix).append(".");
                        whereClause.append(keyColumnId).append(" IN ( ");
                        whereClause.append(inclausePropValue);
                        whereClause.append(") ");
                        needAnd = true;
                    }
                } else {
                    Logger.logInfo("Audit", "getWhereClause(): Checking for D/M/RevF Type");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    int row = -1;
                    boolean revFKeyChkRequired = true;
                    ElementInfo parentElementInfo = elementsContainer.getElementInfo(parentId);
                    LinkedList parentKeyColumns = parentElementInfo.getKeyColumns();
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer qry = new StringBuffer();
                    qry.append("SELECT * FROM sdclink WHERE").append(" (sdcid = " + safeSQL.addVar(sdcIdTopLevel) + " AND (linktype = 'D' OR linktype = 'M'))").append(" OR ").append(" (sdcid = " + safeSQL.addVar(parentElementInfo.getTableSdcid()) + " AND linktype = 'F')").append(" OR ").append(" (sdcid = " + safeSQL.addVar(sdcIdCurrent) + " AND linktype = 'F')");
                    DataSet sdcLinkDS = qp.getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
                    filterMap.put("sdcid", sdcIdTopLevel);
                    DataSet filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                    if (filterDS.getRowCount() > 0) {
                        findMap.put("linktableid", auditTableIdCurrent);
                        row = filterDS.findRow(findMap);
                        if (row != -1) {
                            Logger.logInfo("Audit", "getWhereClause(): D/M Type");
                            boolean needAnd = false;
                            for (int count3 = 0; count3 < parentKeyColumns.size(); ++count3) {
                                keyColumnId = (String)parentKeyColumns.get(count3);
                                inclausePropValue = Audit.getInclauseString(pageData, "keyid" + (count3 + 1));
                                if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                                if (needAnd) {
                                    whereClause.append(" AND ");
                                }
                                whereClause.append(auditTablePrefix).append(".");
                                whereClause.append(keyColumnId).append(" IN ( ");
                                whereClause.append(inclausePropValue);
                                whereClause.append(") ");
                                needAnd = true;
                            }
                            revFKeyChkRequired = false;
                            elementInfo.setLinkType(filterDS.getValue(0, "linktype"));
                        }
                    }
                    if (revFKeyChkRequired) {
                        filterMap.clear();
                        filterMap.put("sdcid", sdcIdCurrent);
                        filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                        String topElementId = elementsContainer.getTopElementId();
                        ElementInfo topElementInfo = elementsContainer.getElementInfo(topElementId);
                        parentElementInfo = elementsContainer.getElementInfo(parentId);
                        findMap.clear();
                        findMap.put("linksdcid", sdcIdTopLevel);
                        row = filterDS.findRow(findMap);
                        if (row == -1) {
                            findMap.clear();
                            findMap.put("linksdcid", parentElementInfo.getElementProperties().getProperty("sdcid"));
                            filterDS = sdcLinkDS.getFilteredDataSet(findMap);
                            row = filterDS.findRow(findMap);
                        }
                        if (row != -1) {
                            Logger.logInfo("Audit", "getWhereClause(): Rev F Type");
                            LinkedHashMap<String, String> fKeyColumnsCurrent = new LinkedHashMap<String, String>();
                            fKeyColumnsCurrent.put("sdccolumnid", filterDS.getString(row, "sdccolumnid"));
                            fKeyColumnsCurrent.put("sdccolumnid2", filterDS.getString(row, "sdccolumnid2", ""));
                            fKeyColumnsCurrent.put("sdccolumnid3", filterDS.getString(row, "sdccolumnid3", ""));
                            elementInfo.setRevFKeyColumns(fKeyColumnsCurrent);
                            int fKeyColumnCount = fKeyColumnsCurrent.size();
                            boolean needAnd = false;
                            Iterator itr = fKeyColumnsCurrent.values().iterator();
                            for (int count4 = 0; count4 < fKeyColumnCount; ++count4) {
                                String parentkeyid;
                                String colId = (String)itr.next();
                                if (colId == null || colId.length() == 0 || (inclausePropValue = (parentkeyid = pageData.getProperty("parentkeyid")) != null ? Audit.getInclauseString(pageData, "parentkeyid") : Audit.getInclauseString(pageData, "keyid" + (count4 + 1))) == null || inclausePropValue.trim().length() <= 0) continue;
                                if (needAnd) {
                                    whereClause.append(" AND ");
                                }
                                whereClause.append(auditTablePrefix).append(".");
                                whereClause.append(colId).append(" IN ( ");
                                whereClause.append(inclausePropValue);
                                whereClause.append(") ");
                                needAnd = true;
                            }
                            elementInfo.setLinkType("RF");
                        }
                    }
                }
                if (extraFilter != null && extraFilter.size() > 0) {
                    count = extraFilter.size();
                    Iterator itr = extraFilter.keySet().iterator();
                    for (int i = 0; i < count; ++i) {
                        if (whereClause.length() > 0) {
                            whereClause.append(" AND ");
                        }
                        String colId = (String)itr.next();
                        whereClause.append(auditTablePrefix).append(".").append(colId).append(" = '").append(extraFilter.get(colId)).append("'");
                    }
                }
                if (whereClause.length() == 0) {
                    Logger.logInfo("Audit", "getWhereClause(): F/V Type - Not Supported.");
                }
            }
            if (whereClause.toString().trim().length() > 0) {
                finalWhereClause.append(" WHERE ").append(whereClause.toString()).append(" ");
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("Could not build Where clause: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(qp.getConnectionid())));
        }
        Logger.logInfo("Audit", "getWhereClause(): " + finalWhereClause.toString());
        return finalWhereClause.toString();
    }

    public static String getOrderByClause(ElementInfo elementInfo, String auditTablePrefix, String realTablePrefix) throws Exception {
        StringBuffer orderByClause = new StringBuffer();
        String keyColumnId = null;
        String order = "ASC";
        String orderby = null;
        LinkedList keyColumns = null;
        int keyColumnCount = 0;
        keyColumns = elementInfo.getKeyColumns();
        keyColumnCount = keyColumns.size();
        orderby = elementInfo.getElementProperties().getProperty("order");
        if (orderby != null && orderby.equalsIgnoreCase("reversechronological")) {
            order = "DESC";
        }
        orderByClause.append(" ORDER BY ");
        if (elementInfo.isInAdvancedMode()) {
            PropertyList advProps = elementInfo.getElementProperties().getPropertyList("advancedconfig");
            if (advProps != null) {
                orderByClause.append(advProps.getProperty("orderbyclause"));
            }
        } else {
            orderByClause.append(" ").append(realTablePrefix);
            orderByClause.append(".").append("USERSEQUENCE ");
            for (int count = 0; count < keyColumnCount; ++count) {
                orderByClause.append(" , ");
                keyColumnId = (String)keyColumns.get(count);
                orderByClause.append(auditTablePrefix).append(".");
                orderByClause.append(keyColumnId.toUpperCase());
            }
        }
        orderByClause.append(", ").append(auditTablePrefix);
        orderByClause.append(".").append("AUDITSEQUENCE  ");
        orderByClause.append(" ").append(order).append(" ");
        return orderByClause.toString();
    }

    public static String createColumnList(Set columns, String tablePrefix) {
        StringBuffer columnlist = new StringBuffer();
        Iterator it = null;
        String columnid = null;
        it = columns.iterator();
        while (it.hasNext()) {
            columnid = (String)it.next();
            columnlist.append(tablePrefix).append(".");
            columnlist.append(columnid.toUpperCase());
            if (!it.hasNext()) continue;
            columnlist.append(", ");
        }
        return columnlist.toString();
    }

    public static String createTraceLogColumnList(ArrayList traceLogColumns, String auditTablePrefix, String tracelogTablePrefix) {
        StringBuffer traceLogColumnList = new StringBuffer();
        Iterator it = null;
        String columnid = null;
        it = traceLogColumns.iterator();
        while (it.hasNext()) {
            columnid = (String)it.next();
            traceLogColumnList.append("( SELECT ");
            traceLogColumnList.append(tracelogTablePrefix).append(".");
            traceLogColumnList.append(columnid.toUpperCase());
            traceLogColumnList.append(" FROM TRACELOG ").append(tracelogTablePrefix);
            traceLogColumnList.append(" WHERE ").append(tracelogTablePrefix);
            traceLogColumnList.append(".tracelogid = ").append(auditTablePrefix);
            traceLogColumnList.append(".tracelogid ) ").append(("tracelog_" + columnid).toUpperCase());
            traceLogColumnList.append(" ");
            if (it.hasNext()) {
                traceLogColumnList.append(", ");
            }
            traceLogColumnList.append(" ");
        }
        traceLogColumnList.append(",  ( SELECT sysuserdesc FROM sysuser WHERE sysuserid = T1.modby ) tracelog_modbyname");
        return traceLogColumnList.toString();
    }

    public static String getInclauseString(PropertyList pagedata, String propid) {
        String inclauseString = null;
        inclauseString = pagedata.getProperty("inclause" + propid);
        return inclauseString;
    }

    public static String getDynamicAuditSelectClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String activityTablePrefix, String fkTablePrefix, QueryProcessor qp, PropertyList pageData) throws Exception {
        StringBuffer selectClause = new StringBuffer();
        String sdcIdTopLevel = null;
        String sdcIdCurrent = null;
        String parentId = null;
        String auditTableIdCurrent = null;
        LinkedList keyColumns = null;
        int keyColumnCount = 0;
        try {
            keyColumns = elementInfo.getKeyColumns();
            keyColumnCount = keyColumns.size();
            sdcIdTopLevel = pageData.getProperty("sdcid");
            auditTableIdCurrent = elementInfo.getElementProperties().getProperty("tableid");
            sdcIdCurrent = elementInfo.getTableSdcid();
            parentId = elementInfo.getElementProperties().getProperty("parentid");
            PropertyList advProps = elementInfo.getElementProperties().getPropertyList("advancedconfig");
            String advSelect = "";
            if (advProps != null && elementInfo.isInAdvancedMode() && elementInfo.isInAdvancedMode4DynamicAudit()) {
                advSelect = advProps.getProperty("dynamicselectclause", "");
            }
            selectClause.append("SELECT ");
            if (advSelect.length() > 0) {
                selectClause.append(advSelect).append(", ");
            }
            selectClause.append(activityTablePrefix + ".* ");
            selectClause.append(", ( SELECT sysuserdesc FROM sysuser WHERE sysuserid = " + activityTablePrefix + ".activityby ) activitybyname");
            if ((parentId != null && !"".equals(parentId) || sdcIdCurrent == null || sdcIdCurrent.trim().length() <= 0) && !elementInfo.isSDIDetailTable()) {
                Logger.logInfo("Audit", "getDynamicAuditSelectClause(): Checking for D/M/RevF Type");
                HashMap<String, String> filterMap = new HashMap<String, String>();
                HashMap<String, String> findMap = new HashMap<String, String>();
                int row = -1;
                boolean revFKeyChkRequired = true;
                ElementInfo parentElementInfo = elementsContainer.getElementInfo(parentId);
                LinkedList parentKeyColumns = parentElementInfo.getKeyColumns();
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer qry = new StringBuffer();
                qry.append("SELECT * FROM sdclink WHERE").append(" (sdcid = " + safeSQL.addVar(sdcIdTopLevel) + " AND (linktype = 'D' OR linktype = 'M'))").append(" OR ").append(" (sdcid = " + safeSQL.addVar(sdcIdCurrent) + " AND linktype = 'F')");
                DataSet sdcLinkDS = qp.getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
                filterMap.put("sdcid", sdcIdTopLevel);
                DataSet filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                if (filterDS.getRowCount() > 0) {
                    findMap.put("linktableid", auditTableIdCurrent);
                    row = filterDS.findRow(findMap);
                    if (row != -1) {
                        Logger.logInfo("Audit", "getDynamicAuditSelectClause(): D/M Type");
                        revFKeyChkRequired = false;
                        elementInfo.setLinkType(filterDS.getValue(0, "linktype"));
                    }
                }
                if (revFKeyChkRequired) {
                    filterMap.clear();
                    filterMap.put("sdcid", sdcIdCurrent);
                    filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                    String topElementId = elementsContainer.getTopElementId();
                    ElementInfo topElementInfo = elementsContainer.getElementInfo(topElementId);
                    findMap.clear();
                    findMap.put("linksdcid", sdcIdTopLevel);
                    row = filterDS.findRow(findMap);
                    if (row == -1) {
                        findMap.clear();
                        findMap.put("linktableid", topElementInfo.getElementProperties().getProperty("tableid").trim());
                        row = filterDS.findRow(findMap);
                    }
                    if (row != -1) {
                        Logger.logInfo("Audit", "getDynamicAuditWhereClause(): Rev F Type");
                        LinkedHashMap<String, String> fKeyColumnsCurrent = new LinkedHashMap<String, String>();
                        fKeyColumnsCurrent.put("sdccolumnid", filterDS.getString(row, "sdccolumnid"));
                        fKeyColumnsCurrent.put("sdccolumnid2", filterDS.getString(row, "sdccolumnid2"));
                        fKeyColumnsCurrent.put("sdccolumnid3", filterDS.getString(row, "sdccolumnid3"));
                        elementInfo.setRevFKeyColumns(fKeyColumnsCurrent);
                        int fKeyColumnCount = fKeyColumnsCurrent.size();
                        Iterator itr = fKeyColumnsCurrent.values().iterator();
                        for (int count = 0; count < fKeyColumnCount; ++count) {
                            String colId = (String)itr.next();
                            if (colId == null || colId.length() <= 0) continue;
                            selectClause.append(", " + fkTablePrefix + "." + colId);
                        }
                        elementInfo.setLinkType("RF");
                    }
                }
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("Could not build Dynamic Audit Select clause: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(qp.getConnectionid())));
        }
        return selectClause.toString();
    }

    public static String getDynamicAuditFromClause(ElementInfo elementInfo, String activityTablePrefix, String fkTablePrefix, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor qp) throws Exception {
        StringBuffer fromClause = new StringBuffer();
        fromClause.append(" FROM activitylog " + activityTablePrefix);
        String sdcIdTopLevel = null;
        String sdcIdCurrent = null;
        String parentId = null;
        String auditTableIdCurrent = null;
        LinkedList keyColumns = null;
        int keyColumnCount = 0;
        try {
            keyColumns = elementInfo.getKeyColumns();
            keyColumnCount = keyColumns.size();
            sdcIdTopLevel = pageData.getProperty("sdcid");
            auditTableIdCurrent = elementInfo.getElementProperties().getProperty("tableid");
            sdcIdCurrent = elementInfo.getTableSdcid();
            parentId = elementInfo.getElementProperties().getProperty("parentid");
            PropertyList advProps = elementInfo.getElementProperties().getPropertyList("advancedconfig");
            String advFrom = "";
            if (elementInfo.isInAdvancedMode()) {
                if (advProps != null && elementInfo.isInAdvancedMode() && elementInfo.isInAdvancedMode4DynamicAudit()) {
                    advFrom = advProps.getProperty("dynamicfromclause", "").trim();
                }
                if (advFrom.startsWith(",")) {
                    advFrom = advFrom.substring(1);
                }
                if (advFrom.endsWith(",")) {
                    advFrom = advFrom.substring(0, advFrom.length() - 1);
                }
                if (advFrom.length() > 0) {
                    fromClause.append(" , " + advFrom + " ");
                }
            } else if ((parentId != null && !"".equals(parentId) || sdcIdCurrent == null || sdcIdCurrent.trim().length() <= 0) && !elementInfo.isSDIDetailTable()) {
                Logger.logInfo("Audit", "getDynamicAuditFromClause(): Checking for D/M/RevF Type");
                HashMap<String, String> filterMap = new HashMap<String, String>();
                HashMap<String, String> findMap = new HashMap<String, String>();
                int row = -1;
                boolean revFKeyChkRequired = true;
                ElementInfo parentElementInfo = elementsContainer.getElementInfo(parentId);
                LinkedList parentKeyColumns = parentElementInfo.getKeyColumns();
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer qry = new StringBuffer();
                qry.append("SELECT * FROM sdclink WHERE").append(" (sdcid = " + safeSQL.addVar(sdcIdTopLevel) + " AND (linktype = 'D' OR linktype = 'M'))").append(" OR ").append(" (sdcid = " + safeSQL.addVar(sdcIdCurrent) + " AND linktype = 'F')");
                DataSet sdcLinkDS = qp.getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
                filterMap.put("sdcid", sdcIdTopLevel);
                DataSet filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                if (filterDS.getRowCount() > 0) {
                    findMap.put("linktableid", auditTableIdCurrent);
                    row = filterDS.findRow(findMap);
                    if (row != -1) {
                        Logger.logInfo("Audit", "getDynamicAuditFromClause(): D/M Type");
                        revFKeyChkRequired = false;
                        elementInfo.setLinkType(filterDS.getValue(0, "linktype"));
                    }
                }
                if (revFKeyChkRequired) {
                    fromClause.append(", ").append(auditTableIdCurrent).append(" " + fkTablePrefix);
                }
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("Could not build Dynamic Audit From clause: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(qp.getConnectionid())));
        }
        return fromClause.toString();
    }

    public static String getDynamicAuditWhereClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String activityTablePrefix, String fkTablePrefix, QueryProcessor qp, PropertyList pageData, HashMap extraFilter) throws SapphireException {
        StringBuffer whereClause = new StringBuffer();
        StringBuffer finalWhereClause = new StringBuffer(" ");
        String keyColumnId = null;
        String sdcIdTopLevel = null;
        String sdcIdCurrent = null;
        String parentId = null;
        String auditTableIdCurrent = null;
        String inclausePropValue = null;
        LinkedList keyColumns = null;
        int keyColumnCount = 0;
        try {
            keyColumns = elementInfo.getKeyColumns();
            keyColumnCount = keyColumns.size();
            sdcIdTopLevel = pageData.getProperty("sdcid");
            auditTableIdCurrent = elementInfo.getElementProperties().getProperty("tableid");
            sdcIdCurrent = elementInfo.getTableSdcid();
            parentId = elementInfo.getElementProperties().getProperty("parentid");
            Logger.logInfo("Audit", "getDynamicAuditWhereClause(): " + elementInfo.getElementid());
            if (elementInfo.isInAdvancedMode() && elementInfo.isInAdvancedMode4DynamicAudit()) {
                PropertyList advProps = elementInfo.getElementProperties().getPropertyListNotNull("advancedconfig");
                String advWhere = advProps.getProperty("dynamicwhereclause", "").trim();
                advWhere = OpalUtil.searchAndReplaceTokens(advWhere, OpalUtil.getKeywordTokens(advWhere), OpalUtil.getRequestParameters(pageData), false);
                advWhere = StringUtil.replaceAll(advWhere, ";", "', '");
                whereClause.append(advWhere);
            } else {
                if ((parentId == null || "".equals(parentId)) && sdcIdCurrent != null && sdcIdCurrent.trim().length() > 0) {
                    Logger.logInfo("Audit", "getDynamicAuditWhereClause(): Top Level SDC");
                    whereClause.append(" " + activityTablePrefix + ".sdcid = '").append(sdcIdCurrent).append("'");
                    for (int count = 0; count < keyColumnCount; ++count) {
                        keyColumnId = (String)keyColumns.get(count);
                        inclausePropValue = Audit.getInclauseString(pageData, "keyid" + (count + 1));
                        if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                        whereClause.append(" AND " + activityTablePrefix + ".keyid" + (count + 1)).append(" IN ( ").append(inclausePropValue).append(")");
                    }
                    whereClause.append(" AND " + activityTablePrefix + ".detailtableid IS NULL");
                } else if (elementInfo.isSDIDetailTable()) {
                    Logger.logInfo("Audit", "getDynamicAuditWhereClause(): SDI Detail Table");
                    whereClause.append(" " + activityTablePrefix + ".detailtableid = '" + elementInfo.getElementTableId() + "'");
                    inclausePropValue = Audit.getInclauseString(pageData, "sdcid");
                    whereClause.append(" AND " + activityTablePrefix + ".sdcid IN ( " + inclausePropValue + ") ");
                    DataSet topSDCKeyColsCountDS = qp.getPreparedSqlDataSet("SELECT keycolumns FROM sdc WHERE sdcid = ?", new Object[]{sdcIdTopLevel});
                    int topSDCKeyColsCount = topSDCKeyColsCountDS.getInt(0, "keycolumns", 1);
                    for (int count = 1; count <= topSDCKeyColsCount; ++count) {
                        inclausePropValue = Audit.getInclauseString(pageData, "keyid" + count);
                        if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                        whereClause.append(" AND " + activityTablePrefix + ".keyid" + count + " IN ( " + inclausePropValue + ") ");
                    }
                } else {
                    Logger.logInfo("Audit", "getDynamicAuditWhereClause(): Checking for D/M/RevF Type");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    int row = -1;
                    boolean revFKeyChkRequired = true;
                    ElementInfo parentElementInfo = elementsContainer.getElementInfo(parentId);
                    LinkedList parentKeyColumns = parentElementInfo.getKeyColumns();
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer qry = new StringBuffer();
                    qry.append("SELECT * FROM sdclink WHERE").append(" (sdcid = " + safeSQL.addVar(sdcIdTopLevel) + " AND (linktype = 'D' OR linktype = 'M'))").append(" OR ").append(" (sdcid = " + safeSQL.addVar(parentElementInfo.getTableSdcid()) + " AND linktype = 'F')").append(" OR ").append(" (sdcid = " + safeSQL.addVar(sdcIdCurrent) + " AND linktype = 'F')");
                    DataSet sdcLinkDS = qp.getPreparedSqlDataSet(qry.toString(), safeSQL.getValues());
                    filterMap.put("sdcid", sdcIdTopLevel);
                    DataSet filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                    String parentSDCId = parentElementInfo.getTableSdcid();
                    if (filterDS.getRowCount() == 0) {
                        filterMap.clear();
                        filterMap.put("sdcid", parentSDCId);
                        filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                    }
                    if (filterDS.getRowCount() > 0) {
                        findMap.put("linktableid", auditTableIdCurrent);
                        row = filterDS.findRow(findMap);
                        if (row != -1) {
                            Logger.logInfo("Audit", "getDynamicAuditWhereClause(): D/M Type");
                            whereClause.append(" " + activityTablePrefix + ".detailtableid = '" + elementInfo.getElementTableId() + "'");
                            inclausePropValue = Audit.getInclauseString(pageData, "sdcid");
                            whereClause.append(" AND " + activityTablePrefix + ".sdcid IN ( " + inclausePropValue + ") ");
                            for (int count = 0; count < parentKeyColumns.size(); ++count) {
                                keyColumnId = (String)parentKeyColumns.get(count);
                                inclausePropValue = Audit.getInclauseString(pageData, "keyid" + (count + 1));
                                if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                                whereClause.append("AND " + activityTablePrefix + ".keyid").append(count + 1).append(" IN ( ").append(inclausePropValue).append(") ");
                            }
                            revFKeyChkRequired = false;
                            elementInfo.setLinkType(filterDS.getValue(0, "linktype"));
                        }
                    }
                    if (revFKeyChkRequired) {
                        filterMap.clear();
                        filterMap.put("sdcid", sdcIdCurrent);
                        filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
                        String topElementId = elementsContainer.getTopElementId();
                        ElementInfo topElementInfo = elementsContainer.getElementInfo(topElementId);
                        findMap.clear();
                        findMap.put("linksdcid", sdcIdTopLevel);
                        row = filterDS.findRow(findMap);
                        if (row == -1) {
                            findMap.clear();
                            findMap.put("linksdcid", parentElementInfo.getTableSdcid());
                            row = filterDS.findRow(findMap);
                        }
                        if (row != -1) {
                            Logger.logInfo("Audit", "getDynamicAuditWhereClause(): Rev F Type");
                            LinkedHashMap<String, String> fKeyColumnsCurrent = new LinkedHashMap<String, String>();
                            fKeyColumnsCurrent.put("sdccolumnid", filterDS.getString(row, "sdccolumnid"));
                            fKeyColumnsCurrent.put("sdccolumnid2", filterDS.getString(row, "sdccolumnid2"));
                            fKeyColumnsCurrent.put("sdccolumnid3", filterDS.getString(row, "sdccolumnid3"));
                            elementInfo.setRevFKeyColumns(fKeyColumnsCurrent);
                            StringBuffer revFKSql = new StringBuffer();
                            revFKSql.append("SELECT");
                            LinkedList elementKeyCols = elementInfo.getKeyColumns();
                            for (int i = 0; i < elementKeyCols.size(); ++i) {
                                if (i > 0) {
                                    revFKSql.append(",");
                                }
                                revFKSql.append(" ").append(elementKeyCols.get(i));
                            }
                            revFKSql.append(" FROM ").append(elementInfo.getElementTableId());
                            revFKSql.append(" WHERE ");
                            int fKeyColumnCount = fKeyColumnsCurrent.size();
                            boolean needAnd = false;
                            Iterator itr = fKeyColumnsCurrent.values().iterator();
                            for (int count = 0; count < fKeyColumnCount; ++count) {
                                String colId = (String)itr.next();
                                inclausePropValue = Audit.getInclauseString(pageData, "keyid" + (count + 1));
                                if (inclausePropValue == null || inclausePropValue.trim().length() <= 0) continue;
                                if (needAnd) {
                                    revFKSql.append(" AND ");
                                }
                                revFKSql.append(colId).append(" IN ( ");
                                revFKSql.append(inclausePropValue).append(") ");
                                needAnd = true;
                            }
                            DataSet revFKRows = qp.getSqlDataSet(revFKSql.toString());
                            whereClause.append(" " + activityTablePrefix + ".sdcid = '" + elementInfo.getTableSdcid() + "'");
                            for (int count = 0; count < keyColumns.size(); ++count) {
                                String keyColId = (String)keyColumns.get(count);
                                whereClause.append(" AND " + fkTablePrefix + ".").append(keyColId).append(" = " + activityTablePrefix + ".keyid" + (count + 1));
                            }
                            for (int i = 0; i < elementKeyCols.size(); ++i) {
                                whereClause.append(" AND ").append(Audit.formMultipleInWithOrForWhereClause(activityTablePrefix + ".keyid" + (i + 1), revFKRows.getColumnValues((String)elementKeyCols.get(i), ";"), ";", 1000));
                            }
                            whereClause.append(" AND " + activityTablePrefix + ".detailtableid IS NULL");
                            elementInfo.setLinkType("RF");
                        }
                    }
                }
                if (whereClause.length() == 0) {
                    Logger.logInfo("Audit", "getDynamicAuditWhereClause(): F/V Type - Not Supported.");
                }
            }
            if (whereClause.toString().trim().length() > 0) {
                finalWhereClause.append(" WHERE ").append(whereClause.toString()).append(" ");
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("Could not build Dynamic Audit Where clause: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(qp.getConnectionid())));
        }
        Logger.logInfo("Audit", "getDynamicAuditWhereClause(): " + finalWhereClause.toString());
        return finalWhereClause.toString();
    }

    public static String getDynamicAuditOrderByClause(ElementInfo elementInfo, String activityTablePrefix) throws Exception {
        StringBuffer orderByClause = new StringBuffer();
        orderByClause.append(" ORDER BY " + activityTablePrefix + ".sdcid, " + activityTablePrefix + ".keyid1, " + activityTablePrefix + ".keyid2, " + activityTablePrefix + ".keyid3, " + activityTablePrefix + ".detailtableid, " + activityTablePrefix + ".detailkeyvalues, " + activityTablePrefix + ".activitydt, " + activityTablePrefix + ".columnid");
        return orderByClause.toString();
    }

    public static String formMultipleInWithOrForWhereClause(String columnid, String colValue, String valSeparator, int size) {
        String ret = "";
        String[] arrColValues = StringUtil.split(colValue, valSeparator);
        int colValuesSize = arrColValues.length;
        int colValuesSizeQ = colValuesSize / size;
        int colValuesSizeR = colValuesSize % size;
        if (colValuesSizeR != 0) {
            colValuesSizeR = 1;
        }
        int totLoop = colValuesSizeQ + colValuesSizeR;
        for (int idx = 0; idx < totLoop; ++idx) {
            int startIndex = size * idx;
            int endIndex = size * idx + size;
            ret = ret + columnid + " IN (";
            for (int i = startIndex; i < endIndex; ++i) {
                if (i >= arrColValues.length) continue;
                ret = ret + "'" + arrColValues[i];
                if (i != endIndex - 1) {
                    if (i + 1 == arrColValues.length) {
                        ret = ret + "')";
                        continue;
                    }
                    ret = ret + "',";
                    continue;
                }
                ret = ret + "')";
                if (idx == totLoop - 1) continue;
                ret = ret + " OR ";
            }
        }
        ret = " (" + ret + ") ";
        return ret;
    }
}

