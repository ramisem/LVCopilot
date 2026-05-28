/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.auditdetails;

import com.labvantage.opal.elements.auditdetails.AuditConstants;
import com.labvantage.opal.elements.auditdetails.AuditDetails;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.ElementInfo;
import com.labvantage.opal.util.TableInfo;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.xml.PropertyTree;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AuditElementsContainer
implements AuditConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103005 $";
    private DataSet __ElementsList;
    private HashMap __ElementsInfoPool;
    private QueryProcessor queryProcessor;
    private TranslationProcessor tp;
    private String renderingMode;
    private String clobViewMode;
    private boolean lazyLoad;
    private int maxRowCount;
    private ArrayList<String> keyIdList;

    public AuditElementsContainer(QueryProcessor qp, TranslationProcessor tp) {
        this.queryProcessor = qp;
        this.tp = tp;
    }

    public boolean initAuditElementsHierarchy(PropertyList requestdata) throws Exception {
        boolean retFlag = true;
        this.__ElementsList = this.getAuditDetailElementsDS(requestdata);
        retFlag = this.validateAuditDetailElements();
        return retFlag;
    }

    public DataSet getElementsList() {
        return this.__ElementsList;
    }

    public void createAuditElementsInfoPool(PropertyList requestData, QueryProcessor qp) throws Exception {
        PropertyList pagedata = requestData.getPropertyList("pagedata");
        this.setRenderingMode(pagedata.getProperty("renderingmode", "Changed"));
        this.setClobViewMode(pagedata.getProperty("clobviewmode", "Flat"));
        this.setLazyLoad(pagedata.getProperty("lazyload"));
        this.setMaxRowCount(Integer.parseInt(pagedata.getProperty("maxrowcount")));
        this.setKeyIdList(AuditElementsContainer.getKeyIdList(pagedata));
        this.initAuditElementsHierarchy(requestData);
        if (this.__ElementsList != null) {
            this.__ElementsInfoPool = new HashMap();
            for (int i = 0; i < this.__ElementsList.getRowCount(); ++i) {
                String elementid = this.__ElementsList.getValue(i, "elementid");
                PropertyList elementProps = requestData.getPropertyList(elementid);
                AuditDetails element = this.createElement(requestData, elementid, "auditdetails", "com.labvantage.opal.elements.auditdetails.AuditDetails");
                ElementInfo elementInfo = this.createElementInfo(elementid, element, elementProps, requestData);
                this.__ElementsInfoPool.put(elementid, elementInfo);
            }
        }
        AuditElementsContainer.determineLinkType(this, pagedata, qp);
    }

    public static void populateAllElementsAuditData(AuditElementsContainer elementsContainer, PropertyList pageData, String dbms, QueryProcessor qp, Logger logger, ConnectionInfo connectionInfo, HashMap extraFilter) throws Exception {
        String topElementId = elementsContainer.getTopElementId();
        AuditElementsContainer.populateChainElementAuditData(topElementId, elementsContainer, pageData, dbms, qp, logger, connectionInfo, false, extraFilter);
    }

    public static void populateChainElementAuditData(String startingElementId, AuditElementsContainer elementsContainer, PropertyList pageData, String dbms, QueryProcessor qp, Logger logger, ConnectionInfo connectionInfo, boolean lazyLoadEnabled, HashMap extraFilter) throws Exception {
        ElementInfo startingElementInfo = elementsContainer.getElementInfo(startingElementId);
        if (!lazyLoadEnabled || lazyLoadEnabled && startingElementInfo.isExpanded()) {
            DataSet ds = AuditElementsContainer.getElementAuditData(startingElementId, elementsContainer, pageData, qp, logger, connectionInfo, dbms, extraFilter);
            int rowcount = ds.getRowCount();
            for (int i = 0; i < rowcount; ++i) {
                if (!ds.getValue(i, "propertytreeid").equals("DefaultLDAPAuthentic")) continue;
                String valuetree = ds.getString(i, "valuetree");
                valuetree = AuditElementsContainer.maskSensitiveData(valuetree, i);
                ds.setValue(i, "valuetree", valuetree);
            }
            startingElementInfo.getElement().setAuditData(ds);
            DataSet ds2 = AuditElementsContainer.getElementDynamicAuditData(startingElementId, elementsContainer, pageData, qp, logger, connectionInfo, dbms, extraFilter);
            startingElementInfo.getElement().setAuditDynamicData(ds2);
            if (ds.getRowCount() > 0) {
                DataSet childElementsList = elementsContainer.getChildElementsDS(startingElementId);
                for (int i = 0; i < childElementsList.getRowCount(); ++i) {
                    String childElementId = childElementsList.getString(i, "elementid");
                    AuditElementsContainer.populateChainElementAuditData(childElementId, elementsContainer, pageData, dbms, qp, logger, connectionInfo, lazyLoadEnabled, extraFilter);
                }
            }
        }
    }

    public static void populateElementAuditData(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, String dbms, QueryProcessor qp, Logger logger, ConnectionInfo connectionInfo, HashMap extraFilter) throws Exception {
        ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
        elementInfo.getElement().setAuditData(AuditElementsContainer.getElementAuditData(elementId, elementsContainer, pageData, qp, logger, connectionInfo, dbms, extraFilter));
        elementInfo.getElement().setAuditDynamicData(AuditElementsContainer.getElementDynamicAuditData(elementId, elementsContainer, pageData, qp, logger, connectionInfo, dbms, extraFilter));
    }

    public String getTopElementId() throws SapphireException {
        String topElementId = null;
        DataSet topElementDS = this.getChildElementsDS("");
        if (topElementDS != null) {
            if (topElementDS.getRowCount() == 0) {
                throw new SapphireException("Top level audit element not Found");
            }
            if (topElementDS.getRowCount() > 1) {
                throw new SapphireException("More than one top element found");
            }
        } else {
            throw new SapphireException("Top level audit element not Found");
        }
        topElementId = topElementDS.getValue(0, "elementid");
        return topElementId;
    }

    public AuditDetails getElement(String elementId) throws Exception {
        return this.getElementInfo(elementId).getElement();
    }

    public ElementInfo getElementInfo(String elementId) throws SapphireException {
        ElementInfo elementInfo = null;
        if (this.__ElementsInfoPool != null) {
            elementInfo = (ElementInfo)this.__ElementsInfoPool.get(elementId);
            elementInfo.getElement().setElementsContainer(this);
        }
        if (elementInfo == null) {
            throw new SapphireException("Element " + elementId + " Info not found.");
        }
        return elementInfo;
    }

    public String getParentElementId(String elementId) throws Exception {
        int row = this.__ElementsList.findRow("elementid", elementId);
        if (row != -1) {
            return this.__ElementsList.getString(row, "parentid");
        }
        return "";
    }

    public DataSet getChildElementsDS(String elementid) {
        DataSet childElements = null;
        HashMap<String, String> filter = null;
        if (this.__ElementsList != null) {
            filter = new HashMap<String, String>();
            filter.put("parentid", elementid);
            childElements = this.__ElementsList.getFilteredDataSet(filter);
            childElements.sort("title");
        }
        return childElements;
    }

    public String getElementIdByTableId(String tableid) throws Exception {
        String elementId = null;
        for (int count = 0; count < this.__ElementsList.getRowCount(); ++count) {
            String tempElementId = this.__ElementsList.getValue(count, "elementid");
            ElementInfo elementInfo = this.getElementInfo(tempElementId);
            PropertyList elementProperties = elementInfo.getElementProperties();
            String tempTableId = elementProperties.getProperty("tableid");
            if (tempTableId == null || !tempTableId.equalsIgnoreCase(tableid)) continue;
            elementId = tempElementId;
            break;
        }
        if (elementId == null) {
            Logger.logError("Element with tableid " + tableid + " not found. Cannot render Audit data.");
            throw new Exception("Element with tableid " + tableid + " not found. Cannot render Audit data.");
        }
        return elementId;
    }

    public static void determineLinkType(AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor qp) throws Exception {
        for (int i = 0; i < elementsContainer.__ElementsList.getRowCount(); ++i) {
            int row;
            String elementId = elementsContainer.__ElementsList.getString(i, "elementid");
            ElementInfo elementInfo = (ElementInfo)elementsContainer.__ElementsInfoPool.get(elementId);
            String auditTableIdCurrent = elementInfo.getElementProperties().getProperty("tableid");
            String sdcIdCurrent = elementInfo.getTableSdcid();
            String parentId = elementInfo.getElementProperties().getProperty("parentid");
            if ((parentId == null || parentId.isEmpty()) && sdcIdCurrent != null && !sdcIdCurrent.trim().isEmpty() || elementInfo.isSDIDetailTable()) continue;
            HashMap<String, String> filterMap = new HashMap<String, String>();
            HashMap<String, String> findMap = new HashMap<String, String>();
            boolean revFKeyChkRequired = true;
            ElementInfo parentElementInfo = elementsContainer.getElementInfo(parentId);
            String sdcIdParent = parentElementInfo.getTableSdcid();
            DataSet sdcLinkDS = qp.getPreparedSqlDataSet("SELECT * FROM sdclink WHERE (sdcid = ? AND (linktype = 'D' OR linktype = 'M')) OR (sdcid = ? AND linktype = 'F')", (Object[])new String[]{sdcIdParent, sdcIdCurrent});
            filterMap.put("sdcid", sdcIdParent);
            DataSet filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
            if (filterDS.getRowCount() > 0) {
                findMap.put("linktableid", auditTableIdCurrent);
                row = filterDS.findRow(findMap);
                if (row != -1) {
                    revFKeyChkRequired = false;
                    String linkType = filterDS.getValue(0, "linktype");
                    elementInfo.setLinkType("D".equals(linkType) ? "D" : "M");
                }
            }
            if (!revFKeyChkRequired) continue;
            filterMap.clear();
            filterMap.put("sdcid", sdcIdCurrent);
            filterDS = sdcLinkDS.getFilteredDataSet(filterMap);
            findMap.clear();
            findMap.put("linksdcid", sdcIdParent);
            row = filterDS.findRow(findMap);
            if (row == -1) {
                findMap.clear();
                findMap.put("linktableid", parentElementInfo.getElementProperties().getProperty("tableid").trim());
                row = filterDS.findRow(findMap);
            }
            if (row == -1) continue;
            LinkedHashMap<String, String> fKeyColumnsCurrent = new LinkedHashMap<String, String>();
            fKeyColumnsCurrent.put("sdccolumnid", filterDS.getString(row, "sdccolumnid"));
            fKeyColumnsCurrent.put("sdccolumnid2", filterDS.getString(row, "sdccolumnid2"));
            fKeyColumnsCurrent.put("sdccolumnid3", filterDS.getString(row, "sdccolumnid3"));
            elementInfo.setRevFKeyColumns(fKeyColumnsCurrent);
            elementInfo.setLinkType("RF");
        }
    }

    public void syncAdvKeyColsType() {
        for (String elementId : this.__ElementsInfoPool.keySet()) {
            ElementInfo elementInfo = (ElementInfo)this.__ElementsInfoPool.get(elementId);
            if (!elementInfo.isInAdvancedMode()) continue;
            AuditDetails auditDetails = elementInfo.getElement();
            int colType = 0;
            DataSet auditData = auditDetails.getAuditData();
            DataSet colTypeDS = null;
            if (auditData == null && this.isLazyLoad()) {
                String tableId = elementInfo.getElementProperties().getProperty("tableid");
                colTypeDS = this.queryProcessor.getPreparedSqlDataSet("SELECT tableid, columnid, datatype FROM syscolumn WHERE tableid = ?", new Object[]{tableId});
            }
            LinkedList keyColumns = elementInfo.getKeyColumns();
            for (int i = 0; i < keyColumns.size(); ++i) {
                String keyColumnId = (String)keyColumns.get(i);
                if (auditData != null) {
                    colType = auditData.getColumnType(keyColumnId);
                } else {
                    int row = colTypeDS.findRow("columnid", keyColumnId);
                    if (row > -1) {
                        String colTypeStr = colTypeDS.getString(row, "datatype");
                        colType = "N".equalsIgnoreCase(colTypeStr) ? 1 : 0;
                    }
                }
                elementInfo.setKeyColumn(keyColumnId, 1 == colType ? "N" : "C");
            }
        }
    }

    private DataSet getAuditDetailElementsDS(PropertyList requestdata) throws Exception {
        DataSet auditDetailElementsInfo = new DataSet();
        int auditElementCount = 0;
        auditDetailElementsInfo.addColumn("elementid", 0);
        auditDetailElementsInfo.addColumn("parentid", 0);
        auditDetailElementsInfo.addColumn("title", 0);
        String propertyTreelist = requestdata.getProperty("propertytreelist");
        if (propertyTreelist == null) {
            Logger.logError("OPAL-ERROR: Property tree is null. Cannot find audit details elements ");
            throw new Exception("Cannot find AuditDetails elements.");
        }
        String[] propertyTree = StringUtil.split(propertyTreelist, ";");
        for (int count = 0; count < propertyTree.length; ++count) {
            PropertyList tempPropertyList = requestdata.getPropertyList(propertyTree[count]);
            if (tempPropertyList == null || tempPropertyList.getProperty("propertytreetype") == null || !tempPropertyList.getProperty("propertytreetype").equals("Element") || tempPropertyList.getProperty("objectname") == null || !tempPropertyList.getProperty("objectname").equals("com.labvantage.opal.elements.auditdetails.AuditDetails")) continue;
            auditDetailElementsInfo.addRow();
            auditDetailElementsInfo.setString(auditElementCount, "elementid", tempPropertyList.getProperty("elementid"));
            auditDetailElementsInfo.setString(auditElementCount, "parentid", tempPropertyList.getProperty("parentid"));
            auditDetailElementsInfo.setString(auditElementCount, "title", tempPropertyList.getProperty("title").trim());
            ++auditElementCount;
        }
        if (auditDetailElementsInfo.getRowCount() == 0) {
            Logger.logError("OPAL-ERROR: There are not auditdetails elements. Cannot find audit details elements ");
            throw new Exception("Cannot find AuditDetails elements.");
        }
        return auditDetailElementsInfo;
    }

    private boolean validateAuditDetailElements() {
        boolean retFlag = false;
        retFlag = true;
        return retFlag;
    }

    private AuditDetails createElement(PropertyList requestdata, String elementid, String elementType, String elementClass) throws Exception {
        StringBuffer sqlStmt = new StringBuffer();
        AuditDetails element = null;
        DataSet ds = null;
        PropertyList elementProperties = null;
        if (elementClass.length() == 0) {
            SafeSQL safeSQL = new SafeSQL();
            sqlStmt.append("SELECT objectname FROM propertytree WHERE propertytreeid = ").append(safeSQL.addVar(elementType));
            ds = this.queryProcessor.getPreparedSqlDataSet(sqlStmt.toString(), safeSQL.getValues());
            if (ds != null && ds.size() == 1) {
                elementClass = ds.getString(0, "objectname");
            }
            if (elementClass.length() == 0) {
                throw new Exception("ElementTag exception: class not defined or not found using type '" + elementType + "'");
            }
        }
        try {
            element = (AuditDetails)Class.forName(elementClass).newInstance();
            element.setElementid(elementid);
            element.setElementType(elementType);
            element.setElementClass(elementClass);
            element.setConnectionId(requestdata.getProperty("connectionid"));
            elementProperties = requestdata.getPropertyList(elementid);
            if (elementProperties == null) {
                throw new Exception("Failed to find properties for elementid " + elementid);
            }
            element.setElementProperties(elementProperties);
            element.setElementsContainer(this);
        }
        catch (Exception e) {
            throw new Exception("ElementTag '" + elementid + "' exception: " + e.getMessage());
        }
        return element;
    }

    private ElementInfo createElementInfo(String elementid, AuditDetails element, PropertyList elementProps, PropertyList requestData) throws Exception {
        String tableid = elementProps.getProperty("tableid");
        String tableSdcid = TableInfo.getSDCId(tableid, this.queryProcessor, requestData.getProperty("dbms"));
        ElementInfo elementInfo = new ElementInfo(this.queryProcessor, this.tp);
        elementInfo.setElementid(elementid);
        elementInfo.setElement(element);
        elementInfo.setTableSdcid(tableSdcid);
        elementInfo.setEnabled(elementProps.getProperty("enabled"));
        elementInfo.setElementProperties(elementProps);
        if (!elementInfo.isInAdvancedMode()) {
            String sql = "SELECT src.columnid, src.columnsequence, sc.datatype FROM sysrefcolumn src  JOIN sysref sr ON src.refid = sr.refid JOIN syscolumn sc ON sc.tableid = sr.tableid AND sc.columnid = src.columnid WHERE sr.tableid = ? AND sr.reftypeflag = 'P' ORDER BY src.columnsequence";
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{tableid});
            if (ds != null) {
                for (int row = 0; row < ds.size(); ++row) {
                    elementInfo.setKeyColumn(ds.getValue(row, "columnid"), ds.getValue(row, "datatype"));
                }
            }
        } else {
            PropertyList advProps = elementProps.getPropertyList("advancedconfig");
            if (advProps != null) {
                PropertyListCollection plc = advProps.getCollectionNotNull("keycolumns");
                for (int j = 0; j < plc.size(); ++j) {
                    elementInfo.setKeyColumn(plc.getPropertyList(j).getProperty("columnid"), "C");
                }
            }
        }
        elementInfo.setColumns();
        ArrayList modificationInfoColumns = elementInfo.getModificationInfoColumns(this.renderingMode);
        elementInfo.appendToTableColumns(modificationInfoColumns);
        return elementInfo;
    }

    private static DataSet getElementAuditData(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor qp, Logger logger, ConnectionInfo connectionInfo, String dbms, HashMap extraFilter) throws Exception {
        ElementInfo elementInfo;
        SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator("ORA".equalsIgnoreCase(dbms));
        if (sqlGenerator == null) {
            throw new Exception("Could not get SQLGenerator. Unsupported database.");
        }
        String sqlStmt = sqlGenerator.getAuditSQL(elementId, elementsContainer, pageData, qp, logger, extraFilter);
        DataSet ds = qp.getSqlDataSet(sqlStmt, true);
        if (ds == null) {
            throw new SapphireException("Audit", "FAILURE", "Exception occurred while trying to retrieve audit data for element '" + elementId + "'. Please contact Administrator.");
        }
        if (ds != null && ds.getRowCount() > 0 && elementId.equalsIgnoreCase(elementsContainer.getTopElementId())) {
            AuditElementsContainer.removeUnWantedRows(ds, elementsContainer, elementId);
        }
        if ((elementInfo = elementsContainer.getElementInfo(elementId)) != null && !elementInfo.isInAdvancedMode() && elementInfo.isSDC()) {
            SapphireConnection sapphireConnection = new SapphireConnection();
            sapphireConnection.setConnectionInfo(connectionInfo);
            DataMaskUtil dataMaskUtil = new DataMaskUtil(sapphireConnection);
            dataMaskUtil.maskPrimaryDataSet(ds, elementInfo.getTableSdcid(), "primary", true);
        }
        AuditElementsContainer.evalDateFieldTypes(elementsContainer.getElementInfo(elementId).getElementTableId(), ds, qp, connectionInfo, logger);
        return ds;
    }

    private static DataSet getElementDynamicAuditData(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor qp, Logger logger, ConnectionInfo connectionInfo, String dbms, HashMap extraFilter) throws Exception {
        DataSet dynamicAuditData;
        ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
        if (elementInfo.isInAdvancedMode() && !elementInfo.isInAdvancedMode4DynamicAudit()) {
            dynamicAuditData = new DataSet();
        } else {
            SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator("ORA".equalsIgnoreCase(dbms));
            if (sqlGenerator == null) {
                throw new Exception("Could not get SQLGenerator. Unsupported database.");
            }
            String sqlStmt = sqlGenerator.getDynamicAuditSQL(elementId, elementsContainer, pageData, qp, logger, extraFilter);
            dynamicAuditData = qp.getSqlDataSet(sqlStmt, true);
            if (dynamicAuditData == null) {
                throw new SapphireException("Audit", "FAILURE", "Exception occurred while trying to retrieve dynamic audit data for element '" + elementId + "'. Please contact Administrator.");
            }
        }
        return dynamicAuditData;
    }

    private static void removeUnWantedRows(DataSet ds, AuditElementsContainer elementsContainer, String elementId) throws Exception {
        ElementInfo elementInfo = elementsContainer.getElementInfo(elementId);
        LinkedList keyList = elementInfo.getKeyColumns();
        for (int i = ds.getRowCount() - 1; i >= 0; --i) {
            String keyVal = AuditDetails.getKeyColVal(ds, i, keyList);
            if (elementsContainer.getKeyIdList().contains(keyVal)) continue;
            ds.deleteRow(i);
        }
    }

    private static String maskSensitiveData(String xmlstring, int rownumber) throws Exception {
        String sanitizedXML = xmlstring;
        PropertyTree valuetree = new PropertyTree("DefaultLDAPAuthentic");
        valuetree.setValueXML(xmlstring);
        if (!valuetree.getNode("Default").getPropertyList().getProperty("rootuserpassword").isEmpty()) {
            if (rownumber > 0 && rownumber % 2 > 0) {
                valuetree.getNode("Default").getPropertyList().setProperty("rootuserpassword", "**********");
            } else {
                valuetree.getNode("Default").getPropertyList().setProperty("rootuserpassword", "********");
            }
        }
        sanitizedXML = valuetree.toXMLString();
        return sanitizedXML;
    }

    private static void evalDateFieldTypes(String tableId, DataSet auditData, QueryProcessor qp, ConnectionInfo connectionInfo, Logger logger) {
        try {
            if (auditData != null && auditData.getRowCount() > 0) {
                String[] columns;
                DataSet dateCols = new DataSet();
                for (String column : columns = auditData.getColumns()) {
                    int colType = auditData.getColumnType(column);
                    if (2 != colType) continue;
                    int newRow = dateCols.addRow();
                    dateCols.setString(newRow, "columnid", column);
                }
                if (dateCols.getRowCount() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT tableid, columnid, propertyid, propertyvalue FROM syscolumnproperty WHERE tableid = " + safeSQL.addVar(tableId) + " AND columnid IN (" + safeSQL.addIn(dateCols.getColumnValues("columnid", "','")) + ") AND propertyid = 'timezoneindependent'";
                    DataSet dateColsProperty = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    DateTimeUtil dtu = new DateTimeUtil(connectionInfo);
                    DateFormat tzUnawareDF = dtu.getDefaultDateOnlyFormat();
                    tzUnawareDF.setTimeZone(TimeZone.getDefault());
                    for (int i = 0; i < dateCols.getRowCount(); ++i) {
                        int findRow;
                        String columnId = dateCols.getString(i, "columnid");
                        if (dateColsProperty == null || !"Y".equalsIgnoreCase(dateColsProperty.getValue(findRow = dateColsProperty.findRow("columnid", columnId), "propertyvalue", "N"))) continue;
                        auditData.setTimeZoneInsensitive(columnId);
                        auditData.setDateDisplayFormat(columnId, tzUnawareDF);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.info("Problem while evaluating date fields for date format. Continuing without evaluating.");
        }
    }

    private static ArrayList<String> getKeyIdList(PropertyList pagedata) {
        DataSet ds = new DataSet();
        ds.addColumnValues("sdcid", 0, pagedata.getProperty("sdcid"), ";");
        ds.addColumnValues("keyid1", 0, pagedata.getProperty("keyid1"), ";");
        ds.addColumnValues("keyid2", 0, pagedata.getProperty("keyid2"), ";");
        ds.addColumnValues("keyid3", 0, pagedata.getProperty("keyid3"), ";");
        ds.padColumns();
        ArrayList<String> keyIdList = new ArrayList<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String keyid1 = ds.getString(i, "keyid1", "");
            String keyid2 = ds.getString(i, "keyid2", "");
            String keyid3 = ds.getString(i, "keyid3", "");
            String val = keyid1;
            if (keyid2.length() > 0) {
                val = val + ";" + ds.getString(i, "keyid2", "");
            }
            if (keyid3.length() > 0) {
                val = val + ";" + ds.getString(i, "keyid3", "");
            }
            keyIdList.add(val);
        }
        return keyIdList;
    }

    public void setQueryProcessor(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
    }

    public String getRenderingMode() {
        return this.renderingMode;
    }

    public void setRenderingMode(String renderingMode) {
        this.renderingMode = renderingMode;
    }

    public String getClobViewMode() {
        return this.clobViewMode;
    }

    public void setClobViewMode(String clobViewMode) {
        this.clobViewMode = clobViewMode;
    }

    public boolean isLazyLoad() {
        return this.lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public void setLazyLoad(String lazyLoad) {
        this.lazyLoad = "Y".equalsIgnoreCase(lazyLoad);
    }

    public int getMaxRowCount() {
        return this.maxRowCount;
    }

    public void setMaxRowCount(int maxRowCount) {
        this.maxRowCount = maxRowCount;
    }

    public ArrayList<String> getKeyIdList() {
        return this.keyIdList;
    }

    public void setKeyIdList(ArrayList<String> keyIdList) {
        this.keyIdList = keyIdList;
    }
}

