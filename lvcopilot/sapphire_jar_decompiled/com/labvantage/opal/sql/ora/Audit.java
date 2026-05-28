/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.ora;

import com.labvantage.opal.elements.auditdetails.AuditConstants;
import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.util.ElementInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class Audit
implements AuditConstants {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 102381 $";

    public static String getSQLStmt(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor queryProcessor, Logger logger, HashMap extraFilter) throws Exception {
        StringBuffer sqlStmt = new StringBuffer();
        String auditTablePrefix = "T1";
        String tracelogPrefix = "T2";
        String realTablePrefix = "T3";
        ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
        elementInfo.getElementProperties().setProperty("parentkeyid", pageData.getProperty("parentkeyid"));
        sqlStmt.append(Audit.getSelectClause(elementInfo, elementsContainer, auditTablePrefix, tracelogPrefix, queryProcessor));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getFromClause(elementInfo, auditTablePrefix, realTablePrefix));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getWhereClause(elementInfo, elementsContainer, auditTablePrefix, queryProcessor, pageData, extraFilter));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getOrderByClause(elementInfo, auditTablePrefix, realTablePrefix));
        sqlStmt.append(" ");
        logger.info("Getting Audit Records for element: " + elementInfo.getElementid());
        logger.info("Using SQL: " + sqlStmt);
        return sqlStmt.toString();
    }

    private static String getSelectClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String auditTablePrefix, String tracelogPrefix, QueryProcessor queryprocessor) throws Exception {
        StringBuffer selectClause = new StringBuffer();
        String tableid = null;
        HashSet<String> tableColumns = null;
        ArrayList tracelogColumns = null;
        PropertyList currentElementProperties = null;
        currentElementProperties = elementInfo.getElementProperties();
        tableid = currentElementProperties.getProperty("tableid");
        tableColumns = new HashSet<String>(elementInfo.getTableColumns());
        String renderingmode = elementsContainer.getRenderingMode();
        PropertyList advProps = currentElementProperties.getPropertyList("advancedconfig");
        String advSelect = "";
        if (advProps != null && elementInfo.isInAdvancedMode()) {
            advSelect = advProps.getProperty("selectclause", "");
        }
        if ("Changed".equalsIgnoreCase(renderingmode)) {
            ArrayList<String> columnIDs = new ArrayList<String>();
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT COLUMNID FROM SYSCOLUMN WHERE TABLEID = " + safeSQL.addVar(tableid);
            DataSet dataset1 = queryprocessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            String strTemp = dataset1.getColumnValues("COLUMNID", ",");
            strTemp = strTemp.substring(0, strTemp.lastIndexOf(","));
            String[] strTempArray = strTemp.split(",");
            for (int i = 0; i < strTempArray.length; ++i) {
                columnIDs.add(strTempArray[i]);
            }
            if (columnIDs.contains("modby") && columnIDs.contains("moddt")) {
                tableColumns.add("modtool");
            }
        } else if ("AuditRows".equalsIgnoreCase(renderingmode)) {
            // empty if block
        }
        tableColumns.add("modby");
        tableColumns.add("moddt");
        tableColumns.add("auditsequence");
        tableColumns.add("auditopflag");
        tracelogColumns = elementInfo.getTracelogColumns();
        selectClause.append("SELECT ");
        if (advSelect.length() > 0) {
            selectClause.append(advSelect + ", ");
        }
        selectClause.append(com.labvantage.opal.sql.common.Audit.createColumnList(tableColumns, auditTablePrefix));
        if (tableid != null) {
            selectClause.append(",  ");
            selectClause.append(com.labvantage.opal.sql.common.Audit.createTraceLogColumnList(tracelogColumns, auditTablePrefix, tracelogPrefix));
        }
        return selectClause.toString();
    }

    private static String getFromClause(ElementInfo elementInfo, String auditTablePrefix, String realTablePrefix) throws Exception {
        return com.labvantage.opal.sql.common.Audit.getFromClause(elementInfo, auditTablePrefix, realTablePrefix);
    }

    private static String getWhereClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String auditTablePrefix, QueryProcessor qp, PropertyList pageData, HashMap extraFilter) throws SapphireException {
        String oraWhereClause = com.labvantage.opal.sql.common.Audit.getWhereClause(elementInfo, elementsContainer, auditTablePrefix, qp, pageData, extraFilter);
        oraWhereClause = oraWhereClause + " AND rownum <= " + elementsContainer.getMaxRowCount() + " ";
        return oraWhereClause;
    }

    private static String getOrderByClause(ElementInfo elementInfo, String auditTablePrefix, String realTablePrefix) throws Exception {
        return com.labvantage.opal.sql.common.Audit.getOrderByClause(elementInfo, auditTablePrefix, realTablePrefix);
    }

    public static String getDynamicAuditSQLStmt(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor queryProcessor, Logger logger, HashMap extraFilter) throws Exception {
        StringBuffer sqlStmt = new StringBuffer();
        String activityTablePrefix = "activitylog";
        String fkTablePrefix = "T3";
        ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
        sqlStmt.append(Audit.getDynamicAuditSelectClause(elementInfo, elementsContainer, activityTablePrefix, fkTablePrefix, queryProcessor, pageData));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getDynamicAuditFromClause(elementInfo, activityTablePrefix, fkTablePrefix, elementsContainer, pageData, queryProcessor));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getDynamicAuditWhereClause(elementInfo, elementsContainer, activityTablePrefix, fkTablePrefix, queryProcessor, pageData, extraFilter));
        sqlStmt.append(" ");
        sqlStmt.append(Audit.getDynamicAuditOrderByClause(elementInfo, activityTablePrefix));
        sqlStmt.append(" ");
        logger.info("Getting Dynamic Audit Records for element: " + elementInfo.getElementid());
        logger.info("Using SQL: " + sqlStmt);
        return sqlStmt.toString();
    }

    private static String getDynamicAuditSelectClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String activityTablePrefix, String fkTablePrefix, QueryProcessor queryprocessor, PropertyList pageData) throws Exception {
        return com.labvantage.opal.sql.common.Audit.getDynamicAuditSelectClause(elementInfo, elementsContainer, activityTablePrefix, fkTablePrefix, queryprocessor, pageData);
    }

    private static String getDynamicAuditFromClause(ElementInfo elementInfo, String activityTablePrefix, String fkTablePrefix, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor queryProcessor) throws Exception {
        return com.labvantage.opal.sql.common.Audit.getDynamicAuditFromClause(elementInfo, activityTablePrefix, fkTablePrefix, elementsContainer, pageData, queryProcessor);
    }

    private static String getDynamicAuditWhereClause(ElementInfo elementInfo, AuditElementsContainer elementsContainer, String activityTablePrefix, String fkTablePrefix, QueryProcessor qp, PropertyList pageData, HashMap extraFilter) throws SapphireException {
        String oraWhereClause = com.labvantage.opal.sql.common.Audit.getDynamicAuditWhereClause(elementInfo, elementsContainer, activityTablePrefix, fkTablePrefix, qp, pageData, extraFilter);
        oraWhereClause = oraWhereClause + " AND rownum <= " + elementsContainer.getMaxRowCount() + " ";
        return oraWhereClause;
    }

    private static String getDynamicAuditOrderByClause(ElementInfo elementInfo, String activityTablePrefix) throws Exception {
        return com.labvantage.opal.sql.common.Audit.getDynamicAuditOrderByClause(elementInfo, activityTablePrefix);
    }
}

