/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.elements.auditdetails.AuditConstants;
import com.labvantage.opal.elements.auditdetails.AuditDetails;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ElementInfo
implements AuditConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90637 $";
    private AuditDetails __Element = null;
    private String __Elementid = null;
    private String __TableSdcid = null;
    private String databaseId = "";
    private String enabled = "";
    private LinkedList __KeyColumns = null;
    private HashMap __KeyColumnsTypeMap;
    private PropertyList __ElementProperties = null;
    private PropertyListCollection __UnifiedColumns = null;
    private ArrayList __TableColumns = null;
    private ArrayList __TracelogColumns = null;
    public CSS css = new CSS();
    QueryProcessor queryProcessor = null;
    TranslationProcessor tp = null;
    private static Map<String, Set<String>> allSDITablesList = new HashMap<String, Set<String>>();

    public ElementInfo(QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        this.__KeyColumns = new LinkedList();
        this.__KeyColumnsTypeMap = new HashMap();
        this.queryProcessor = queryProcessor;
        this.tp = translationProcessor;
        String connectionId = queryProcessor.getConnectionid();
        ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionId);
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionId);
        this.databaseId = connectionInfo.getDatabaseId();
        this.loadSDIxxxTables(queryProcessor);
    }

    private void loadSDIxxxTables(QueryProcessor qp) {
        if (!allSDITablesList.containsKey(this.databaseId)) {
            try {
                DataSet ds = qp.getSqlDataSet("SELECT tableid \nFROM sdcsoftlink\nWHERE tableid LIKE 'sdi%'      -- #1  name like sdi%\nAND inpkflag = 'Y'             -- #4  softlink part of PK\nAND keyid2columnid = 'keyid2'  -- #4  no prefix, and elimate any without full softlink\nAND tableid NOT IN\n(\n  SELECT tableid               -- #2  not in list of SDC tables that aren't D-type\n  FROM sdc\n  WHERE sdctype <> 'D'\n  UNION\n  SELECT sl.linktableid        -- #3 not 1st level detail under SDC that is not D-type\n  FROM sdclink sl,sdc s\n  WHERE sl.linktype = 'D'\n  AND sl.sdcid = s.sdcid\n  AND s.sdctype <> 'D'\n  UNION\n  SELECT linktableid           -- #3 not nth level detail under SDC that is not D-type\n  FROM sdcdetaillink sl,sdc s\n  WHERE sl.linktype = 'D'\n  AND sl.sdcid = s.sdcid\n  AND s.sdctype <> 'D'\n)\nORDER BY 1\n");
                HashSet<String> sdiTables = new HashSet<String>();
                allSDITablesList.put(this.databaseId, sdiTables);
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    sdiTables.add(ds.getString(i, "tableid").toLowerCase());
                }
            }
            catch (Exception e) {
                Trace.logError("Exception occurred when trying to retrieve list of supported sdixxx tables. Audit view page will not load these elements properly.", e);
            }
        }
    }

    public LinkedHashMap getRevFKeyColumns() {
        return (LinkedHashMap)this.__ElementProperties.get("_revFKeyCols");
    }

    public void setRevFKeyColumns(LinkedHashMap revFKeyColumns) {
        this.__ElementProperties.put("_revFKeyCols", revFKeyColumns);
    }

    public String getLinkType() {
        return this.__ElementProperties.getProperty("linkType", "");
    }

    public void setLinkType(String linkType) {
        this.__ElementProperties.setProperty("linkType", linkType);
    }

    public PropertyList getElementProperties() {
        return this.__ElementProperties;
    }

    public void setElementProperties(PropertyList elementProperties) {
        this.__ElementProperties = elementProperties;
    }

    public AuditDetails getElement() {
        return this.__Element;
    }

    public void setElement(AuditDetails element) {
        this.__Element = element;
    }

    public String getElementid() {
        return this.__Elementid;
    }

    public void setElementid(String elementid) {
        this.__Elementid = elementid;
    }

    public String getTableSdcid() {
        return this.__TableSdcid;
    }

    public void setTableSdcid(String tableSdcid) {
        this.__TableSdcid = tableSdcid;
    }

    public LinkedList getKeyColumns() {
        return this.__KeyColumns;
    }

    public void setKeyColumns(LinkedList keyColumns) {
        this.__KeyColumns = keyColumns;
    }

    public void setKeyColumn(String columnid, String type) {
        this.__KeyColumnsTypeMap.put(columnid, type);
        if (!this.__KeyColumns.contains(columnid)) {
            this.__KeyColumns.add(columnid);
        }
    }

    public String getKeyColumnType(String columnid) {
        return (String)this.__KeyColumnsTypeMap.get(columnid);
    }

    public boolean isKeyColumnNumeric(String columnid) {
        return "N".equals(this.getKeyColumnType(columnid));
    }

    public PropertyListCollection getUnifiedColumns() {
        return this.__UnifiedColumns;
    }

    public ArrayList getTableColumns() {
        return this.__TableColumns;
    }

    public ArrayList getTracelogColumns() {
        return this.__TracelogColumns;
    }

    public boolean isSDIDetailTable() {
        String tableid = this.__ElementProperties.getProperty("tableid");
        return tableid != null && allSDITablesList.containsKey(this.databaseId) && allSDITablesList.get(this.databaseId).contains(tableid.toLowerCase());
    }

    public boolean isDetailTable() {
        String childLinkType = this.getLinkType();
        return "D".equals(childLinkType) || "M".equals(childLinkType);
    }

    public boolean isSDC() {
        boolean retFlag = false;
        String tableid = this.__ElementProperties.getProperty("tableid");
        String parentid = this.__ElementProperties.getProperty("parentid");
        if (tableid != null && !this.isSDIDetailTable() && this.__TableSdcid != null) {
            retFlag = true;
        }
        return retFlag;
    }

    public boolean isInAdvancedMode() {
        boolean retFlag = false;
        PropertyList advProps = this.__ElementProperties.getPropertyList("advancedconfig");
        if (advProps != null) {
            retFlag = advProps.getProperty("useadvancedconfig", "N").equalsIgnoreCase("Y");
        }
        return retFlag;
    }

    public boolean isInAdvancedMode4DynamicAudit() {
        boolean retFlag = false;
        PropertyList advProps = this.__ElementProperties.getPropertyList("advancedconfig");
        if (advProps != null) {
            retFlag = advProps.getProperty("useadvancedconfig4dynamic", "N").equalsIgnoreCase("Y");
        }
        return retFlag;
    }

    public String getAuditTableId() {
        String tableid = null;
        tableid = this.__ElementProperties.getProperty("tableid");
        return "a_" + tableid;
    }

    public String getElementTableId() {
        String tableid = null;
        tableid = this.__ElementProperties.getProperty("tableid");
        return tableid;
    }

    public void setColumns() throws Exception {
        if (this.__ElementProperties == null) {
            throw new Exception("Element properties are not set.");
        }
        if (this.__KeyColumns == null) {
            throw new Exception("keycolumns not set.");
        }
        this.createUnifiedColumns();
        this.createTableColumns();
        this.createTraceLogColumns();
    }

    private void createUnifiedColumns() throws Exception {
        PropertyListCollection tempTableColumns = new PropertyListCollection();
        this.__UnifiedColumns = new PropertyListCollection();
        tempTableColumns = this.getAllColumns(tempTableColumns);
        PropertyListCollection tempTracelogColumns = this.__ElementProperties.getCollection("tracelogcolumns");
        if (tempTableColumns != null) {
            this.__UnifiedColumns.addAll(tempTableColumns);
        }
        this.__UnifiedColumns.addAll(tempTracelogColumns);
        if (this.__UnifiedColumns.size() == 0) {
            throw new Exception("ElementId: " + this.__Elementid + " No columns to display. ");
        }
    }

    private PropertyListCollection getAllColumns(PropertyListCollection tempTableColumns) {
        block8: {
            DataSet tableColumns;
            block7: {
                boolean useAllColumns = this.__ElementProperties.getProperty("displayalltablecolumns", "N").equalsIgnoreCase("Y");
                tableColumns = new DataSet();
                if (!useAllColumns) break block7;
                tableColumns = this.getSyscolumns();
                for (int i = 0; i < this.__KeyColumns.size(); ++i) {
                    tableColumns.remove(this.__KeyColumns.get(i));
                }
                for (int i = 0; i < tableColumns.size(); ++i) {
                    PropertyList pl = new PropertyList();
                    pl.setProperty("columnid", tableColumns.getValue(i, "columnid"));
                    pl.setProperty("title", this.tp.translate(tableColumns.getValue(i, "columnlabel", "")));
                    tempTableColumns.add(pl);
                }
                PropertyListCollection plc = this.__ElementProperties.getCollectionNotNull("columns");
                if (plc.size() <= 0) break block8;
                for (int i = 0; i < plc.size(); ++i) {
                    PropertyList plCols = plc.getPropertyList(i);
                    PropertyList plFind = tempTableColumns.find("columnid", plCols.getProperty("columnid"));
                    if (plFind == null) continue;
                    String colTitle = plCols.getProperty("title");
                    if (colTitle.length() > 0) {
                        plFind.setProperty("title", colTitle);
                    }
                    plFind.setProperty("width", plCols.getProperty("width"));
                    plFind.setProperty("align", plCols.getProperty("align"));
                    plFind.setProperty("displayvalue", plCols.getProperty("displayvalue"));
                    plFind.setProperty("enabled", plCols.getProperty("enabled"));
                }
                break block8;
            }
            tempTableColumns = this.__ElementProperties.getCollection("columns");
            if (tempTableColumns != null) {
                for (int i = 0; i < tempTableColumns.size(); ++i) {
                    String colLabel;
                    int findRow;
                    PropertyList colProps = tempTableColumns.getPropertyList(i);
                    String colId = colProps.getProperty("columnid", "");
                    String colTitle = colProps.getProperty("title", "");
                    if (colTitle.trim().length() != 0) continue;
                    if (tableColumns == null) {
                        tableColumns = this.getSyscolumns();
                    }
                    if ((findRow = tableColumns.findRow("columnid", colId)) <= -1 || (colLabel = tableColumns.getValue(findRow, "columnlabel", "")).length() <= 0) continue;
                    colProps.setProperty("title", this.tp.translate(colLabel));
                }
            }
        }
        return tempTableColumns;
    }

    private void createTableColumns() {
        PropertyListCollection tempTableColumns = new PropertyListCollection();
        String columnid = null;
        int keyColumnCount = 0;
        this.__TableColumns = new ArrayList();
        if ((tempTableColumns = this.getAllColumns(tempTableColumns)) != null) {
            this.__TableColumns.addAll(this.getColumnsList(tempTableColumns));
        }
        if (!this.isInAdvancedMode()) {
            keyColumnCount = this.__KeyColumns.size();
            for (int count = 0; count < keyColumnCount; ++count) {
                columnid = (String)this.__KeyColumns.get(count);
                if (this.__TableColumns.contains(columnid)) continue;
                this.__TableColumns.add(columnid);
            }
        }
    }

    private void createTraceLogColumns() {
        PropertyListCollection tempTableColumns = null;
        this.__TracelogColumns = new ArrayList();
        tempTableColumns = this.__ElementProperties.getCollection("tracelogcolumns");
        this.__TracelogColumns.addAll(this.getColumnsList(tempTableColumns));
        if (!this.__TracelogColumns.contains("tracelogid")) {
            this.__TracelogColumns.add(new String("tracelogid"));
        }
        if (!this.__TracelogColumns.contains("reason")) {
            this.__TracelogColumns.add(new String("reason"));
        }
    }

    public DataSet getSyscolumns() {
        ArrayList<String> auditColumns = new ArrayList<String>();
        auditColumns.add("createby");
        auditColumns.add("createdt");
        auditColumns.add("createtool");
        auditColumns.add("modby");
        auditColumns.add("moddt");
        auditColumns.add("modtool");
        auditColumns.add("usersequence");
        auditColumns.add("auditsequence");
        auditColumns.add("tracelogid");
        String tableid = this.__ElementProperties.getProperty("tableid");
        DataSet dataset1 = this.queryProcessor.getPreparedSqlDataSet("SELECT columnid, columnlabel FROM syscolumn WHERE tableid = ?", new Object[]{tableid});
        return dataset1;
    }

    public void removeAuditColumns(PropertyListCollection columnIDs) {
        ArrayList<PropertyList> auditColumns = new ArrayList<PropertyList>();
        auditColumns.add(columnIDs.find("columnid", "createby"));
        auditColumns.add(columnIDs.find("columnid", "createdt"));
        auditColumns.add(columnIDs.find("columnid", "createtool"));
        auditColumns.add(columnIDs.find("columnid", "modby"));
        auditColumns.add(columnIDs.find("columnid", "moddt"));
        auditColumns.add(columnIDs.find("columnid", "modtool"));
        auditColumns.add(columnIDs.find("columnid", "usersequence"));
        auditColumns.add(columnIDs.find("columnid", "auditsequence"));
        auditColumns.add(columnIDs.find("columnid", "tracelogid"));
        columnIDs.removeAll(auditColumns);
    }

    public boolean isExpanded() {
        String expanded = this.__ElementProperties.getProperty("initiallyexpanded", "");
        return "Y".equals(expanded);
    }

    public void setExpanded(boolean isExpanded) {
        if (isExpanded) {
            this.__ElementProperties.setProperty("initiallyexpanded", "Y");
        } else {
            this.__ElementProperties.setProperty("initiallyexpanded", "N");
        }
    }

    private ArrayList getColumnsList(PropertyListCollection columns) {
        ArrayList<String> columnsList = null;
        int columnCount = 0;
        PropertyList column = null;
        String columnid = null;
        if (columns != null) {
            columnsList = new ArrayList<String>();
            columnCount = columns.size();
            for (int count = 0; count < columnCount; ++count) {
                column = columns.getPropertyList(count);
                columnid = column.getProperty("columnid");
                if (columnid == null || columnid.trim().length() <= 0) continue;
                columnsList.add(columnid);
            }
        }
        return columnsList;
    }

    public ArrayList getModificationInfoColumns(String renderingMode) {
        Object pagedata = null;
        ArrayList<String> modificationInfoColumns = new ArrayList<String>();
        String tableid = this.__ElementProperties.getProperty("tableid");
        if (renderingMode.equalsIgnoreCase("Changed")) {
            ArrayList<String> columnIDs = new ArrayList<String>();
            DataSet dataset1 = this.queryProcessor.getPreparedSqlDataSet("SELECT COLUMNID FROM SYSCOLUMN WHERE TABLEID = ?", new Object[]{tableid});
            String strTemp = dataset1.getColumnValues("COLUMNID", ",");
            columnIDs.addAll(Arrays.asList(strTemp.split(",")));
            if (columnIDs.contains("modby") && columnIDs.contains("moddt")) {
                modificationInfoColumns.add("modby");
                modificationInfoColumns.add("moddt");
            }
        }
        return modificationInfoColumns;
    }

    public void appendToTableColumns(ArrayList columns) {
        if (columns != null && columns.size() > 0) {
            this.__TableColumns.addAll(columns);
        }
    }

    public String getEnabled() {
        return this.enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return "Y".equals(StringUtil.getYN(this.enabled, "Y"));
    }

    public class CSS {
        private String styleScheme = "standard";
        private String titleBand = "audit_title";
        private String dataRow = "audit_datarow";
        private String auditRowBand = "audit_auditrow_band";
        private String textStyle = "audit_text";
        private String auditTable = "audit_audittable";
        private String auditTableHeader = "audit_auditheader";
        private String auditEvenRow = "audit_evenauditrow";
        private String auditOddRow = "audit_oddauditrow";
        private String auditDeletedRow = "audit_deletedauditrow_standard";
        private String auditNoChangeRow = "audit_nochangeauditrow";
        private String auditCellHighlight = "audit_auditcell_highlight";
        private String auditCell = "audit_auditcell";
        private String dynamicAuditHeader = "audit_dynamic_auditheader";
        private String dynamicBandNormal = "audit_dynamic_band_normal";
        private String dynamicBandSpecial = "audit_dynamic_band_special";
        private String dynamicAuditEvenRow = "audit_dynamic_evenauditrow";
        private String dynamicAuditOddRow = "audit_dynamic_oddauditrow";
        private String dynamicAuditNoDataRow = "audit_dynamic_nodataauditrow";

        public String getTitleStyle() {
            return this.titleBand + "_" + this.styleScheme;
        }

        public String getDataRowStyle() {
            return this.dataRow + "_" + this.styleScheme;
        }

        public String getAuditTableHeaderStyle() {
            return this.auditTableHeader + "_" + this.styleScheme;
        }

        public String getDynamicAuditHeaderStyle() {
            return this.dynamicAuditHeader + "_" + this.styleScheme;
        }

        public String getAuditRowStyle(int auditRowCount) {
            String style = null;
            style = auditRowCount == -1 ? this.auditNoChangeRow + "_" + this.styleScheme : (auditRowCount % 2 == 0 ? this.auditEvenRow + "_" + this.styleScheme : this.auditOddRow + "_" + this.styleScheme);
            return style;
        }

        public String getAuditDeletedRowStyle() {
            return this.auditDeletedRow;
        }

        public String getDynamicAuditRowStyle(int auditRowCount) {
            String style = null;
            style = auditRowCount == -1 ? this.dynamicAuditNoDataRow + "_" + this.styleScheme : (auditRowCount % 2 == 0 ? this.dynamicAuditEvenRow + "_" + this.styleScheme : this.dynamicAuditOddRow + "_" + this.styleScheme);
            return style;
        }

        public String getTextStyle() {
            return this.textStyle + "_" + this.styleScheme;
        }

        public String getDynamicBandNormal() {
            return this.dynamicBandNormal + "_" + this.styleScheme;
        }

        public String getDynamicBandSpecial() {
            return this.dynamicBandSpecial + "_" + this.styleScheme;
        }

        public String getAuditRowBand() {
            return this.auditRowBand + "_" + this.styleScheme;
        }

        public String getAuditTable() {
            return this.auditTable + "_" + this.styleScheme;
        }

        public String getAuditCellHighlight() {
            return this.auditCellHighlight + "_" + this.styleScheme;
        }

        public String getAuditCell() {
            return this.auditCell + "_" + this.styleScheme;
        }
    }
}

