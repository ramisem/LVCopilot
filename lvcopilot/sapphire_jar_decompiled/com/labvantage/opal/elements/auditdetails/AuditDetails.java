/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.auditdetails;

import com.labvantage.opal.elements.auditdetails.AuditConstants;
import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.util.ElementInfo;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefault;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyDependency;
import com.labvantage.sapphire.xml.PropertyDependencyList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.pageelements.BaseElement;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AuditDetails
extends BaseElement
implements AuditConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103004 $";
    private TranslationProcessor __Tp;
    private static final String IMG_PLUS = "WEB-OPAL/images/plus.gif";
    private static final String IMG_MINUS = "WEB-OPAL/images/minus.gif";
    private static final String IMG_PLUS_DYNAMIC_NORMAL = "rc?command=image&image=FlatBlackDoubleChevronRight&size=12&color=green";
    private static final String IMG_MINUS_DYNAMIC_NORMAL_AUDIT = "rc?command=image&image=FlatBlackDoubleChevronDown&size=12&color=green";
    private static final String IMG_MINUS_DYNAMIC_NORMAL_CHANGED = "rc?command=image&image=FlatBlackDoubleChevronUp&size=12&color=green";
    private static final String IMG_PLUS_DYNAMIC_SPECIAL = "rc?command=image&image=FlatBlackDoubleChevronRight&size=12&color=darkorange";
    private static final String IMG_MINUS_DYNAMIC_SPECIAL_AUDIT = "rc?command=image&image=FlatBlackDoubleChevronDown&size=12&color=darkorange";
    private static final String IMG_MINUS_DYNAMIC_SPECIAL_CHANGED = "rc?command=image&image=FlatBlackDoubleChevronUp&size=12&color=darkorange";
    private static final String IMG_PLUS_DYNAMIC_UNSAVED = "rc?command=image&image=FlatBlackDoubleChevronRight&size=12&color=red";
    private static final String IMG_MINUS_DYNAMIC_UNSAVED_AUDIT = "rc?command=image&image=FlatBlackDoubleChevronDown&size=12&color=red";
    private static final String IMG_MINUS_DYNAMIC_UNSAVED_CHANGED = "rc?command=image&image=FlatBlackDoubleChevronUp&size=12&color=red";
    private DataSet __AuditData = null;
    private DataSet __AuditDynamicData = null;
    private DataSet __FilteredAuditData = null;
    private DataSet __FilteredAuditDynamicData = null;
    private DataSet __CurrentAuditData = null;
    private DataSet __CurrentAuditDynamicData = null;
    private DataSet __tempMLinkAuditData = null;
    private ElementInfo elementInfo = null;
    private AuditElementsContainer elementsContainer = null;
    private HashMap __TopFilter = null;
    private String __TopKey = null;
    private static int staticValue = 0;
    Browser browser = null;
    boolean renderRowNum = false;
    private String renderingMode = "Changed";
    private String clobViewMode = "Flat";
    private static final String CLOB_DEFSOURCE_DB = "DATABASE";
    private static final String CLOB_DEFSOURCE_FILE = "FILE";
    private static final String CLOB_DEFSOURCE_NONE = "NODE";
    private static final String CLOB_TYPE_PROPERTYLIST = "PROPERTYLIST";
    private static final String CLOB_TYPE_PROPERTYTREE_VALUE = "PROPERTYTREE_VALUE";
    private static final String CLOB_TYPE_PROPERTYTREE_DEF = "PROPERTYTREE_DEF";
    private static final String CLOB_TYPE_DATASET = "DATASET";
    private static final PropertyList knownCLOBColumns = new PropertyList();
    private final HashMap<String, PropertyTree> PROPERTYDEF_CACHE = new HashMap();
    private final boolean isHideCLOBXMLInFlattenMode = true;
    private final boolean isDriveFromPropertyDef = false;
    private final String flattenColIdSeparator = ".";
    private final String flattenColTitleSeparatorPrefix = " > ";

    public void initData(TranslationProcessor tp, ConnectionInfo connectionInfo, Browser browser) throws Exception {
        String tempClobViewMode;
        this.__Tp = tp;
        this.connectionInfo = connectionInfo;
        this.setConnectionId(connectionInfo.getConnectionId());
        this.elementInfo = this.elementsContainer.getElementInfo(this.element.getId());
        this.browser = browser;
        String tempRenderingMode = this.elementsContainer.getRenderingMode();
        if (tempRenderingMode != null && tempRenderingMode.trim().length() > 0) {
            this.renderingMode = tempRenderingMode;
        }
        if ((tempClobViewMode = this.elementsContainer.getClobViewMode()) != null && tempClobViewMode.trim().length() > 0) {
            this.clobViewMode = tempClobViewMode;
        }
    }

    public void initData(TranslationProcessor tp, ConnectionInfo connectionInfo, Browser browser, String topKey, HashMap topKeyFilter) throws Exception {
        this.initData(tp, connectionInfo, browser);
        this.__TopKey = topKey;
        this.__TopFilter = topKeyFilter;
    }

    public void initData(TranslationProcessor tp, ConnectionInfo connectionInfo, Browser browser, String topKey, DataSet auditData, HashMap topKeyFilter) throws Exception {
        this.initData(tp, connectionInfo, browser, topKey, topKeyFilter);
        this.__AuditData = auditData;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        try {
            html.append(this.getElementAuditDataCell());
        }
        catch (Exception ex) {
            html.append("<font color='red'>Exception caught: ").append(ex.getMessage()).append("</font>");
            this.logger.error("AuditDetails: ", ex);
            this.logger.stackTrace(ex);
        }
        return html.toString();
    }

    private String getElementAuditDataCell() throws Exception {
        StringBuffer html = new StringBuffer();
        String dataRowsDivId = "";
        String elementTitle = "";
        boolean isExpanded = this.elementInfo.isExpanded();
        if ((!this.elementsContainer.isLazyLoad() || this.elementsContainer.isLazyLoad() && this.elementInfo.isExpanded()) && this.__AuditData == null) {
            throw new Exception(this.getTranslationProcessor().translate("Error in retrieving Audit data. Probably the Audit Method is Off or audit tables are missing. Please contact administrator."));
        }
        dataRowsDivId = this.getDataRowsDivId();
        elementTitle = this.element.getProperty("title");
        html.append("<table cellpadding='0' cellspacing='0' border='0' width='100%'>");
        html.append("<tr>");
        html.append(this.getCollapsyExpandyCell(isExpanded, dataRowsDivId));
        html.append("<td align='left' class='" + this.elementInfo.css.getTitleStyle() + "' onclick='showHideDiv(\"").append(dataRowsDivId).append("\")'>");
        html.append("<b>" + elementTitle + "</b>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td></td>");
        html.append("<td colspan='2' align='left' >");
        html.append(this.getDivTagHtml(isExpanded, dataRowsDivId, this.elementsContainer.isLazyLoad()));
        if (!this.elementsContainer.isLazyLoad() || this.elementsContainer.isLazyLoad() && this.elementInfo.isExpanded()) {
            html.append(this.getDataRows());
        }
        html.append("</div>");
        html.append("</td></tr>");
        html.append("</table>");
        html.append(this.renderPostScripts());
        return html.toString();
    }

    public String getDataRows() throws Exception {
        StringBuffer html = new StringBuffer();
        ArrayList filteredAuditDataKeys = null;
        String dataDivId = null;
        String dataRowtitle = null;
        HashMap filterMap = null;
        HashMap filterKey = null;
        int auditRowsCount = 0;
        this.prepareDynamicAuditData();
        this.flattenCLOBData2Columns();
        this.createFilteredAuditData();
        dataRowtitle = this.element.getProperty("datarowtitle");
        boolean isExpanded = this.elementInfo.isExpanded();
        filteredAuditDataKeys = this.getUniqueKeysFromFilteredAuditData();
        if (filteredAuditDataKeys == null || filteredAuditDataKeys.size() == 0) {
            html.append("<table width='100%' cellpadding='0' cellspacing='1' border='0' >");
            if (this.__AuditData.getRowCount() == this.elementsContainer.getMaxRowCount()) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("maxrowcount", "" + this.elementsContainer.getMaxRowCount());
                html.append("<tr>");
                html.append("<td colspan='3'>");
                html.append(" <font color=red>").append(this.__Tp.translate("Warning: Results may contain incomplete information. Maximum Row Count '[maxrowcount]' reached.", valueMap)).append("</font>");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("<tr>");
            html.append("<td colspan='3'>");
            html.append(this.__Tp.translate("No rows."));
            html.append("</td>");
            html.append("</tr>");
            html.append("</table>");
        } else {
            html.append("<table width='100%' cellpadding='0' cellspacing='1' border='0' >");
            if (this.__AuditData.getRowCount() == this.elementsContainer.getMaxRowCount()) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("maxrowcount", "" + this.elementsContainer.getMaxRowCount());
                html.append("<tr>");
                html.append("<td colspan='3'>");
                html.append(" <font color=red>").append(this.__Tp.translate("Warning: Results may contain incomplete information. Maximum Row Count '[maxrowcount]' reached.", valueMap)).append("</font>");
                html.append("</td>");
                html.append("</tr>");
            }
            for (int i = 0; i < filteredAuditDataKeys.size(); ++i) {
                filterMap = null;
                filterKey = null;
                auditRowsCount = 0;
                dataDivId = (String)filteredAuditDataKeys.get(i);
                filterMap = this.getSubstitutionParams(dataDivId);
                filterKey = this.getFilterKey(dataDivId);
                this.logger.debug("Filter Keys for In-element filtering for data row: " + (i + 1));
                filterKey.forEach((k, v) -> this.logger.debug("\t" + k + " = " + v));
                this.__CurrentAuditData = this.__FilteredAuditData.getFilteredDataSet(filterKey);
                this.__CurrentAuditDynamicData = this.__FilteredAuditDynamicData.getFilteredDataSet(filterKey);
                auditRowsCount = this.__CurrentAuditData.getRowCount();
                filterMap.put("count", "" + auditRowsCount);
                html.append("<tr>");
                html.append(this.getCollapsyExpandyCell(isExpanded, dataDivId));
                html.append("<td align='left'  onclick='showHideDiv(\"").append(dataDivId).append("\")' class='" + this.elementInfo.css.getDataRowStyle() + "' >");
                html.append("<b>" + this.getSubstitutedValue(dataRowtitle, filterMap) + "</b>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr>");
                html.append("<td></td>");
                html.append("<td colspan='2' >");
                html.append(this.getDivTagHtml(isExpanded, dataDivId, null));
                html.append(this.getDataTable(dataDivId));
                html.append("</div>");
                html.append("</td></tr>");
            }
            html.append("</table>");
        }
        return html.toString();
    }

    private String getDataTable(String key) throws Exception {
        StringBuffer html = new StringBuffer();
        html.append("<table cellpadding='0' cellspacing='0' border='0' width='100%'>");
        html.append(this.getAuditDataRow());
        html.append(this.getChildElementRows(key));
        html.append("</table>");
        return html.toString();
    }

    private String getAuditDataRow() throws Exception {
        StringBuffer html = new StringBuffer();
        html.append(this.getAuditDataCell());
        return html.toString();
    }

    private String getAuditDataCell() throws Exception {
        StringBuffer html = new StringBuffer();
        html.append(this.getCurrentAuditDataTable());
        return html.toString();
    }

    private String getCurrentAuditDataTable() throws Exception {
        boolean isExpanded = false;
        StringBuffer html = new StringBuffer();
        PropertyListCollection unifiedColumns = new PropertyListCollection();
        unifiedColumns = this.elementInfo.getUnifiedColumns();
        PropertyListCollection traceLogColumns = new PropertyListCollection();
        traceLogColumns = this.elementInfo.getElementProperties().getCollection("tracelogcolumns");
        isExpanded = this.elementInfo.isExpanded();
        this.renderRowNum = "Y".equals(StringUtil.getYN(this.elementInfo.getElementProperties().getProperty("showrownum", "N"), "N"));
        if ("Changed".equalsIgnoreCase(this.renderingMode)) {
            boolean isAscending = "chronological".equalsIgnoreCase(this.elementInfo.getElementProperties().getProperty("order", "chronological"));
            String[] listedColumns = this.__CurrentAuditData.getColumns();
            ArrayList columnIDs = new ArrayList();
            if (listedColumns != null && listedColumns.length > 0) {
                columnIDs = (ArrayList)OpalUtil.stringArrayToList(listedColumns);
            }
            int rowsInAuditData = this.__CurrentAuditData.getRowCount();
            for (int rowCount = 0; rowCount < rowsInAuditData; ++rowCount) {
                int auditSequence = this.__CurrentAuditData.getInt(rowCount, "auditsequence");
                html.append("<tr><td>");
                html.append(this.getChangedRowsTable(columnIDs, rowCount, isExpanded, traceLogColumns, unifiedColumns, isAscending, rowsInAuditData));
                html.append("</td></tr>");
                html.append("<tr><td>&nbsp;</td></tr>");
                if (rowCount != rowsInAuditData - 1) continue;
                String divUniqueCounter = String.valueOf(System.nanoTime());
                html.append("<tr><td>");
                html.append(this.getChangedRowsDynamicTable(auditSequence, divUniqueCounter, "div_" + divUniqueCounter, true, unifiedColumns));
                html.append("</td></tr>");
                html.append("<tr><td>&nbsp;</td></tr>");
            }
        } else if ("AuditRows".equalsIgnoreCase(this.renderingMode)) {
            ArrayList<String> tableColumnsList = new ArrayList<String>(this.elementInfo.getTableColumns());
            List<String> allNullValCols = this.getAllNullValueColumns(tableColumnsList, unifiedColumns);
            html.append("<tr><td>");
            html.append("<table cellpadding='1px' cellspacing='0px' width='100%' class='" + this.elementInfo.css.getAuditTable() + "' >");
            html.append("<tr class='").append(this.elementInfo.css.getAuditTableHeaderStyle()).append("'>");
            html.append(this.getAuditRowsTableHeader(tableColumnsList, traceLogColumns, unifiedColumns, allNullValCols));
            html.append("</tr>");
            for (int count = 0; count < this.__CurrentAuditData.getRowCount(); ++count) {
                int auditSequence = this.__CurrentAuditData.getInt(count, "auditsequence");
                String dynDivId = String.valueOf(System.nanoTime());
                html.append(this.getAuditRowsDynamicTableRow(auditSequence - 1, tableColumnsList, traceLogColumns, unifiedColumns, allNullValCols, dynDivId, false));
                html.append(this.getAuditRowsTableRow(count, tableColumnsList, traceLogColumns, unifiedColumns, allNullValCols, dynDivId));
                if (count != this.__CurrentAuditData.getRowCount() - 1) continue;
                dynDivId = String.valueOf(System.nanoTime());
                html.append(this.getAuditRowsDynamicTableRow(auditSequence, tableColumnsList, traceLogColumns, unifiedColumns, allNullValCols, dynDivId, true));
            }
            html.append("</table>");
            html.append("</td></tr>");
            html.append("<tr><td>&nbsp;</td></tr>");
        }
        return html.toString();
    }

    private StringBuffer getChangedRowsTable(ArrayList columnIDs, int rowCount, boolean isExpanded, PropertyListCollection traceLogColumns, PropertyListCollection unifiedColumns, boolean isAscending, int rowsInAuditData) throws SapphireException {
        boolean isFirstRow;
        StringBuffer html = new StringBuffer();
        String divUniqueCounter = String.valueOf(System.nanoTime());
        boolean isTableHeaderRendered = false;
        boolean isDeletedRow = false;
        String modDate = "";
        String modBy = "";
        String reason = "";
        String signedFlag = "";
        String activity = "";
        String activityDispVal = "";
        String modTool = "";
        String modByName = "";
        String auditSequence = "";
        String auditOpFlag = "";
        boolean skipSanitizeHTMLValue = false;
        if (columnIDs.contains("modby") && columnIDs.contains("moddt")) {
            modDate = this.__CurrentAuditData.getValue(rowCount, "moddt");
            modBy = this.__CurrentAuditData.getString(rowCount, "modby");
        }
        auditSequence = this.__CurrentAuditData.getValue(rowCount, "auditsequence");
        int refAuditSeqDyn = Integer.parseInt(auditSequence) - 1;
        reason = this.__CurrentAuditData.getString(rowCount, "tracelog_reason", "");
        signedFlag = this.__CurrentAuditData.getString(rowCount, "tracelog_signedflag", "N");
        activity = this.__CurrentAuditData.getString(rowCount, "tracelog_activity", "");
        activityDispVal = this.getActivityDisplayValue(this.__CurrentAuditData, rowCount, activity, this.__Tp);
        modTool = this.__CurrentAuditData.getString(rowCount, "modtool", "");
        isDeletedRow = this.isDeletedRow(this.__CurrentAuditData, rowCount);
        modByName = this.__CurrentAuditData.getString(rowCount, "tracelog_modbyname", "");
        auditOpFlag = this.__CurrentAuditData.getString(rowCount, "auditopflag", "");
        boolean dynamicAuditFound = this.getAuditDynamicDataForAuditSeq(refAuditSeqDyn).getRowCount() > 0;
        html.append("<table cellpadding='0' cellspacing='0' border='0' style='table-layout:fixed;width:100%'>");
        html.append("<tr class='").append(this.elementInfo.css.getAuditRowBand()).append("'").append(">");
        html.append("<td width='12px' valign='middle' align='center'>");
        html.append("<img id='img_").append(divUniqueCounter).append(String.valueOf(rowCount)).append("'");
        if (isExpanded) {
            html.append(" src='WEB-OPAL/images/minus.gif' ");
        } else {
            html.append(" src='WEB-OPAL/images/plus.gif' ");
        }
        html.append("onclick='showHideDivTag(\"").append(divUniqueCounter).append(String.valueOf(rowCount)).append("\")' ");
        html.append("class='Outline' style='cursor: pointer' width='12px' height='12px' />");
        html.append("</td>");
        html.append("<td width='16px' valign='middle' align='center'>");
        if (dynamicAuditFound) {
            boolean isDynAuditSpecial = this.isDynamicAuditDataSpecial(refAuditSeqDyn);
            if (isDynAuditSpecial) {
                html.append(this.getShowHideButtonDynamicChangedRowsHtml(IMG_PLUS_DYNAMIC_SPECIAL, "img_" + divUniqueCounter + String.valueOf(rowCount) + "_dynamic", "Show Special Activity.", "showHideDynamicDivTag('" + divUniqueCounter + String.valueOf(rowCount) + "')", "Special"));
            } else {
                html.append(this.getShowHideButtonDynamicChangedRowsHtml(IMG_PLUS_DYNAMIC_NORMAL, "img_" + divUniqueCounter + String.valueOf(rowCount) + "_dynamic", "Show General Activity.", "showHideDynamicDivTag('" + divUniqueCounter + String.valueOf(rowCount) + "')", "Normal"));
            }
        } else {
            html.append("&nbsp;");
        }
        html.append("</td>");
        html.append("<td valign='middle'>");
        html.append("<div id='div_").append(divUniqueCounter).append(String.valueOf(rowCount)).append("_1").append("' ").append("> ");
        html.append("<table cellpadding='3' cellspacing='0' border='0' width='100%'>");
        html.append("<tr onclick='showHideDivTag(\"").append(divUniqueCounter).append(String.valueOf(rowCount)).append("\")' >");
        html.append("<td width='20%'>");
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("auditSequence", this.renderRowNum ? String.valueOf(rowCount + 1) : auditSequence);
        valueMap.put("userid", modBy);
        String actionText = isDeletedRow ? " deleted by " : ("I".equalsIgnoreCase(auditOpFlag) ? " added by " : " modified by ");
        html.append(this.__Tp.translate("[auditSequence] " + actionText + " [userid]", valueMap));
        if (modByName.length() > 0 && "Y".equals(this.elementInfo.getElementProperties().getProperty("showmodbyname", "Y"))) {
            html.append(" (").append(SafeHTML.encodeForHTML(modByName)).append(")");
        }
        html.append("</td>");
        html.append("<td width='23%'>").append(" " + this.__Tp.translate("Date") + ": ").append(modDate).append("</td>");
        html.append("<td width='19%'>").append(this.getTraceLogColTitle(traceLogColumns, "reason", this.__Tp.translate("Reason")) + ": ").append(reason).append("</td>");
        if (this.__CurrentAuditData.isValidColumn("tracelog_signedflag")) {
            html.append("<td width='19%'>").append(this.getTraceLogColTitle(traceLogColumns, "signedflag", this.__Tp.translate("Signed Flag")) + ": ").append(this.__Tp.translate(signedFlag)).append("</td>");
        } else {
            html.append("<td width='19%'>&nbsp;</td>");
        }
        if (this.__CurrentAuditData.isValidColumn("tracelog_activity")) {
            html.append("<td width='19%'>").append(this.__Tp.translate(this.getTraceLogColTitle(traceLogColumns, "activity", "Activity")) + ": ").append(this.__Tp.translate(activityDispVal)).append("</td>");
        } else {
            html.append("<td width='19%'>&nbsp;</td>");
        }
        html.append("</tr>");
        html.append("</table></div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table></td></tr>");
        html.append("<tr><td>");
        String finalDivId = "div_" + divUniqueCounter + String.valueOf(rowCount);
        html.append("<div id='" + finalDivId + "' style='display:").append(isExpanded ? "block" : "none").append("' ").append(" style='border:red dashed none;' > ");
        boolean suppressFirstrow = "Y".equals(this.element.getProperty("suppressFirstRow", "N"));
        boolean bl = isFirstRow = isAscending && rowCount == 0 || !isAscending && rowCount == rowsInAuditData - 1;
        if (!isFirstRow || !suppressFirstrow) {
            html.append("<table cellpadding='0px' cellspacing='0px' width='100%' class='" + this.elementInfo.css.getAuditTable() + "' >");
            html.append(this.getChangedRowsDynamicTable(refAuditSeqDyn, divUniqueCounter + String.valueOf(rowCount), finalDivId, false, unifiedColumns));
            int toggleCount = 0;
            for (int columnCount = 0; columnCount < unifiedColumns.size(); ++columnCount) {
                String oldRowColumnValue;
                String presentRowColumnValue;
                boolean isKnownCLOBCol;
                PropertyList column = unifiedColumns.getPropertyList(columnCount);
                String columnId = column.getProperty("columnid");
                skipSanitizeHTMLValue = this.skipSanitizeHtmlValue(columnId, rowCount);
                String colTitle = column.getProperty("title");
                if (colTitle == null || colTitle.trim().length() == 0) {
                    colTitle = column.getProperty("columnid");
                }
                String colAlign = column.getProperty("align", "left");
                String displayValue = column.getProperty("displayvalue");
                boolean colEnabled = "Y".equals(StringUtil.getYN(column.getProperty("enabled"), "Y"));
                if (!colEnabled || columnId.equalsIgnoreCase("modby") || columnId.equalsIgnoreCase("moddt") || columnId.equalsIgnoreCase("signedflag") || columnId.equalsIgnoreCase("activity") || columnId.equalsIgnoreCase("reason")) continue;
                boolean bl2 = isKnownCLOBCol = this.getKnownCLOBColumnProps(this.elementInfo.getElementTableId(), columnId) != null && "Diff".equals(this.clobViewMode);
                if (isAscending) {
                    presentRowColumnValue = this.__CurrentAuditData.getValue(rowCount, columnId, "");
                    oldRowColumnValue = rowCount == 0 || this.isDeletedRow(this.__CurrentAuditData, rowCount - 1) || "I".equalsIgnoreCase(auditOpFlag) ? "" : this.__CurrentAuditData.getValue(rowCount - 1, columnId, "");
                } else {
                    presentRowColumnValue = this.__CurrentAuditData.getValue(rowCount, columnId, "");
                    oldRowColumnValue = rowCount == rowsInAuditData - 1 || this.isDeletedRow(this.__CurrentAuditData, rowCount + 1) || "I".equalsIgnoreCase(auditOpFlag) ? "" : this.__CurrentAuditData.getValue(rowCount + 1, columnId, "");
                }
                if (isKnownCLOBCol) {
                    try {
                        presentRowColumnValue = this.getCLOBDiffHTML(columnId, oldRowColumnValue, presentRowColumnValue, this.__CurrentAuditData, rowCount + 1);
                    }
                    catch (Exception e) {
                        this.logger.error("Couldn't de-serialize xml to object for Table: " + this.elementInfo.getElementTableId() + ", Column: " + columnId + ". Show basic audit view.", e);
                        presentRowColumnValue = this.convertXMLToString(presentRowColumnValue, skipSanitizeHTMLValue);
                        isKnownCLOBCol = false;
                    }
                } else {
                    presentRowColumnValue = this.convertXMLToString(presentRowColumnValue, skipSanitizeHTMLValue);
                    oldRowColumnValue = this.convertXMLToString(oldRowColumnValue, skipSanitizeHTMLValue);
                }
                if (isDeletedRow) {
                    if (presentRowColumnValue == oldRowColumnValue && presentRowColumnValue.length() == 0) continue;
                    if (!isTableHeaderRendered) {
                        html.append(this.getChangedRowsTableHeader(this.elementInfo));
                        isTableHeaderRendered = true;
                    }
                    if (isKnownCLOBCol) {
                        if (presentRowColumnValue.length() <= 0) continue;
                        html.append("<tr class='").append(this.elementInfo.css.getAuditRowStyle(toggleCount)).append("' > ");
                        html.append("<td>&nbsp;</td>");
                        html.append("<td>" + colTitle + "</td>");
                        html.append("<td align='" + colAlign + "' colspan=2>" + presentRowColumnValue + "</td>");
                        html.append("<td>&nbsp;</td>");
                        html.append("</tr>");
                        continue;
                    }
                    html.append(this.getChangedRowsTableData(this.elementInfo, toggleCount++, colAlign, "", presentRowColumnValue, displayValue, colTitle));
                    continue;
                }
                if (presentRowColumnValue.equals(oldRowColumnValue)) continue;
                if (!isTableHeaderRendered) {
                    html.append(this.getChangedRowsTableHeader(this.elementInfo));
                    isTableHeaderRendered = true;
                }
                if (isKnownCLOBCol) {
                    if (presentRowColumnValue.length() <= 0) continue;
                    html.append("<tr class='").append(this.elementInfo.css.getAuditRowStyle(toggleCount++)).append("' > ");
                    html.append("<td>&nbsp;</td>");
                    html.append("<td>" + colTitle + "</td>");
                    html.append("<td align='" + colAlign + "' colspan=2>" + presentRowColumnValue + "</td>");
                    html.append("<td>&nbsp;</td>");
                    html.append("</tr>");
                    continue;
                }
                html.append(this.getChangedRowsTableData(this.elementInfo, toggleCount++, colAlign, presentRowColumnValue, oldRowColumnValue, displayValue, colTitle));
            }
            if (toggleCount == 0) {
                StringBuffer noRowsFound = new StringBuffer();
                noRowsFound.append("<tr class='").append(this.elementInfo.css.getAuditRowStyle(-1)).append("'> ");
                noRowsFound.append("<td colspan='5'>").append(this.__Tp.translate("No changes found for the configured columns.")).append("</td>");
                noRowsFound.append("</tr>");
                html.append(noRowsFound);
            }
            html.append("<tr>");
            html.append("</tr>");
            html.append("</table>");
        }
        html.append("</div>");
        return html;
    }

    private StringBuffer getChangedRowsTableHeader(ElementInfo currentElementInfo) throws SapphireException {
        StringBuffer html = new StringBuffer();
        html.append("<tr class='").append(currentElementInfo.css.getAuditTableHeaderStyle()).append("'>");
        html.append("<td width='10%'>&nbsp;</td>");
        html.append("<td width='20%'><u><b>").append(this.__Tp.translate("Column Name")).append("</b></u></td>");
        html.append("<td width='20%'><u><b>").append(this.__Tp.translate("Old Value")).append("</b></u></td>");
        html.append("<td width='20%'><u><b>").append(this.__Tp.translate("New Value")).append("</b></u></td>");
        html.append("<td width='30%'>&nbsp;</td>");
        html.append("</tr>");
        return html;
    }

    private StringBuffer getChangedRowsTableData(ElementInfo currentElementInfo, int toggleCount, String align, String presentRowColumnValue, String oldRowColumnValue, String displayValue, String title) {
        StringBuffer html = new StringBuffer();
        html.append("<tr class='").append(currentElementInfo.css.getAuditRowStyle(toggleCount)).append("' > ");
        html.append("<td>&nbsp;</td>");
        html.append("<td>" + SafeHTML.encodeForHTML(title) + "</td>");
        html.append("<td align='" + align + "'>" + this.getValueToPrint(oldRowColumnValue, displayValue) + "</td>");
        html.append("<td align='" + align + "'>" + this.getValueToPrint(presentRowColumnValue, displayValue) + "</td>");
        html.append("<td>&nbsp;</td>");
        html.append("</tr>");
        return html;
    }

    private StringBuffer getChangedRowsDynamicTable(int refAuditSeq, String divUniqueCounter, String divId, boolean isLastAuditSeq, PropertyListCollection unifiedColumns) {
        StringBuffer html = new StringBuffer();
        HashSet tableColumns = new HashSet(this.elementInfo.getTableColumns());
        boolean showFullName = "Y".equals(this.elementInfo.getElementProperties().getProperty("showmodbyname", "Y"));
        DataSet filteredCurrentDynamicData = this.getAuditDynamicDataForAuditSeq(refAuditSeq);
        if (filteredCurrentDynamicData.getRowCount() > 0) {
            if (isLastAuditSeq) {
                html.append("<tr>");
                html.append("<td>");
                html.append("<table cellpadding='0' cellspacing='0' border='0' style='table-layout:fixed;width:100%'>");
                html.append("<tr>");
                html.append("<td width='12px' valign='middle' align='center'>&nbsp;</td>");
                html.append("<td valign='middle' align='left'>");
                html.append(this.getShowHideButtonDynamicChangedRowsHtml(IMG_PLUS_DYNAMIC_UNSAVED, "img_" + divUniqueCounter + "_dynamic", "Show Un-persisted Activity.", "showHideDynamicDivTag('" + divUniqueCounter + "')", "UnSaved"));
                html.append("</td>");
                html.append("</tr>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("<tr>");
            html.append("<td " + (isLastAuditSeq ? "" : " colspan='5'") + ">");
            html.append("<div id='" + divId + "_dynamic' style='display:none;'>");
            html.append("<table cellpadding='0' cellspacing='0' width='100%'>");
            html.append("<tr class='").append(this.elementInfo.css.getDynamicAuditHeaderStyle()).append("'>");
            html.append("<td width='10%'>&nbsp;</td>");
            html.append("<td width='20%'><u><b>").append(this.__Tp.translate("Column Name")).append("</b></u></td>");
            html.append("<td width='20%'><u><b>").append(this.__Tp.translate("Old Value")).append("</b></u></td>");
            html.append("<td width='20%'><u><b>").append(this.__Tp.translate("New Value")).append("</b></u></td>");
            html.append("<td width='10%'><u><b>").append(this.__Tp.translate("Mod. by")).append("</b></u></td>");
            html.append("<td width='10%'><u><b>").append(this.__Tp.translate("Mod. Dt.")).append("</b></u></td>");
            html.append("<td width='10%'><u><b>").append(this.__Tp.translate("Reason")).append("</b></u></td>");
            html.append("</tr>");
            DateFormat primaryModDtFormat = this.__CurrentAuditData.getDateDisplayFormat("moddt");
            for (int rowCount = 0; rowCount < filteredCurrentDynamicData.size(); ++rowCount) {
                String columnId = filteredCurrentDynamicData.getValue(rowCount, "columnid");
                String activityType = filteredCurrentDynamicData.getValue(rowCount, "activitytype");
                PropertyList column = this.getColumnProps(columnId, unifiedColumns);
                if (!"Y".equals(column.getProperty("enabled", "Y"))) continue;
                if (("Detail_Add".equalsIgnoreCase(activityType) || "Detail_Delete".equalsIgnoreCase(activityType)) && columnId.length() == 0) {
                    html.append("<tr class='").append(this.elementInfo.css.getDynamicAuditRowStyle(rowCount)).append("'>");
                    html.append("<td>&nbsp;</td>");
                    html.append("<td>").append(this.__Tp.translate(activityType)).append("</td>");
                    html.append("<td>").append("&nbsp;").append("</td>");
                    html.append("<td>").append(filteredCurrentDynamicData.getValue(rowCount, "detailkeyvalues")).append("</td>");
                } else {
                    if (!tableColumns.contains(columnId)) continue;
                    html.append("<tr class='").append(this.elementInfo.css.getDynamicAuditRowStyle(rowCount)).append("'>");
                    html.append("<td>&nbsp;</td>");
                    String colTitle = column.getProperty("title");
                    if (colTitle == null || colTitle.trim().length() == 0) {
                        colTitle = columnId;
                    }
                    html.append("<td>").append(colTitle).append("</td>");
                    html.append("<td>").append(this.getValueToPrint(filteredCurrentDynamicData.getValue(rowCount, "oldvalue"), "")).append("</td>");
                    html.append("<td>").append(this.getValueToPrint(filteredCurrentDynamicData.getValue(rowCount, "newvalue"), "")).append("</td>");
                }
                String modBy = filteredCurrentDynamicData.getValue(rowCount, "activityby");
                String modByName = filteredCurrentDynamicData.getValue(rowCount, "activitybyname", "");
                if (modByName.length() > 0 && showFullName) {
                    html.append("<td>" + modBy + " (" + modByName + ")</td>");
                } else {
                    html.append("<td>" + modBy + "</td>");
                }
                html.append("<td>").append(primaryModDtFormat.format(filteredCurrentDynamicData.getCalendar(rowCount, "activitydt").getTime())).append("</td>");
                html.append("<td>").append(filteredCurrentDynamicData.getValue(rowCount, "reason")).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
            html.append("</td>");
            html.append("</tr>");
        }
        return html;
    }

    private String getShowHideButtonDynamicChangedRowsHtml(String imageUrl, String id, String tip, String jsActionScript, String dynamicAuditType) {
        StringBuffer html = new StringBuffer();
        html.append("<img");
        html.append(" id='" + id + "'");
        html.append(" src='" + imageUrl + "'");
        html.append(" title='" + this.__Tp.translate(tip) + "'");
        html.append(" onclick=\"" + jsActionScript + "\"");
        html.append(" data-dynamicaudittype='" + dynamicAuditType + "'");
        html.append(" style='cursor: pointer;'");
        html.append(" width='12px' height='12px'");
        html.append(" />");
        return html.toString();
    }

    private String getAuditRowsTableHeader(ArrayList<String> tableColumnsList, PropertyListCollection traceLogColumns, PropertyListCollection unifiedColumns, List<String> allNullValCols) throws SapphireException {
        StringBuffer html = new StringBuffer();
        PropertyList columnProps = null;
        String colTitle = "";
        String nullColExceptionMsg = this.__Tp.translate("NULL columnid encountered. Cannot retrieve data. ElementId: ") + this.elementid;
        if (unifiedColumns != null) {
            html.append("<td nowrap width='1px'><b>#</b></td>");
            html.append("<td nowrap width='1px'>&nbsp;</td>");
            for (String colId : tableColumnsList) {
                if (colId == null) {
                    throw new SapphireException(nullColExceptionMsg.toString());
                }
                if ("moddt".equalsIgnoreCase(colId) || "modby".equalsIgnoreCase(colId) || "auditsequence".equalsIgnoreCase(colId) || allNullValCols.contains(colId) || !"Y".equals((columnProps = this.getColumnProps(colId, unifiedColumns)).getProperty("enabled", "Y"))) continue;
                colTitle = columnProps.getProperty("title");
                if (colTitle == null || colTitle.trim().length() == 0) {
                    colTitle = columnProps.getProperty("columnid");
                }
                html.append("<td nowrap>");
                html.append("<b>").append(SafeHTML.encodeForHTML(colTitle)).append("</b></td>");
            }
            colTitle = "";
            colTitle = this.__Tp.translate(" ");
            html.append("<td nowrap>");
            html.append("<b>").append(SafeHTML.encodeForHTML(colTitle)).append("</b></td>");
            colTitle = "";
            columnProps = unifiedColumns.getPropertyList("modby");
            if (columnProps != null) {
                colTitle = columnProps.getProperty("title");
            }
            if (colTitle == null || colTitle.trim().length() == 0) {
                colTitle = this.__Tp.translate("Mod. By.");
            }
            html.append("<td nowrap>");
            html.append("<b>").append(SafeHTML.encodeForHTML(colTitle)).append("</b></td>");
            colTitle = "";
            columnProps = unifiedColumns.getPropertyList("moddt");
            if (columnProps != null) {
                colTitle = columnProps.getProperty("title");
            }
            if (colTitle == null || colTitle.trim().length() == 0) {
                colTitle = this.__Tp.translate("Mod. Dt.");
            }
            html.append("<td nowrap>");
            html.append("<b>").append(SafeHTML.encodeForHTML(SafeHTML.encodeForHTML(colTitle))).append("</b></td>");
            for (int traceLogColCount = 0; traceLogColCount < traceLogColumns.size(); ++traceLogColCount) {
                String colId;
                columnProps = traceLogColumns.getPropertyList(traceLogColCount);
                colId = columnProps.getProperty("columnid");
                if (colId == null) {
                    throw new SapphireException(nullColExceptionMsg.toString());
                }
                colTitle = columnProps.getProperty("title");
                if (colTitle == null || colTitle.trim().length() == 0) {
                    colTitle = columnProps.getProperty("columnid");
                }
                html.append("<td nowrap>");
                html.append("<b>").append(SafeHTML.encodeForHTML(colTitle)).append("</b></td>");
            }
        }
        return html.toString();
    }

    private String getAuditRowsTableRow(int auditRowNum, ArrayList<String> tableColumnsList, PropertyListCollection traceLogColumns, PropertyListCollection unifiedColumns, List<String> allNullValCols, String idForDynDiv) throws SapphireException {
        StringBuffer html = new StringBuffer();
        String nullColExceptionMsg = this.__Tp.translate("NULL columnid encountered. Cannot retrieve data. ElementId: ") + this.elementid;
        boolean requiresHighlighting = false;
        boolean skipSanitizeHTMLValue = false;
        boolean isDeletedRow = this.isDeletedRow(this.__CurrentAuditData, auditRowNum);
        String auditOpFlag = this.__CurrentAuditData.getString(auditRowNum, "auditopflag", "");
        html.append("<tr class='").append(isDeletedRow ? this.elementInfo.css.getAuditDeletedRowStyle() : this.elementInfo.css.getAuditRowStyle(auditRowNum)).append("'>");
        String columnValue = this.renderRowNum ? String.valueOf(auditRowNum + 1) : this.__CurrentAuditData.getValue(auditRowNum, "auditsequence");
        PropertyList auditSeqColProps = this.getColumnProps("auditsequence", unifiedColumns);
        html.append(this.getAuditRowsTableCell(auditSeqColProps, columnValue, false));
        int auditSequence = this.__CurrentAuditData.getInt(auditRowNum, "auditsequence");
        boolean dynamicAuditFound = this.getAuditDynamicDataForAuditSeq(auditSequence - 1).getRowCount() > 0;
        html.append("<td>");
        if (dynamicAuditFound) {
            boolean isDynAuditSpecial = this.isDynamicAuditDataSpecial(auditSequence - 1);
            if (isDynAuditSpecial) {
                html.append(this.getShowHideButtonDynamicAuditRowsHtml(IMG_PLUS_DYNAMIC_SPECIAL, "img_" + idForDynDiv + "_dynamic", "Show Special Activity.", "showHideDynamicDivAuditRows(\"" + idForDynDiv + "\")", "Special"));
            } else {
                html.append(this.getShowHideButtonDynamicAuditRowsHtml(IMG_PLUS_DYNAMIC_NORMAL, "img_" + idForDynDiv + "_dynamic", "Show General Activity.", "showHideDynamicDivAuditRows(\"" + idForDynDiv + "\")", "Normal"));
            }
        } else {
            html.append("&nbsp;");
        }
        html.append("</td>");
        for (String colId : tableColumnsList) {
            boolean isKnownCLOBCol;
            if (colId == null) {
                throw new SapphireException(nullColExceptionMsg.toString());
            }
            if ("moddt".equalsIgnoreCase(colId) || "modby".equalsIgnoreCase(colId) || "auditsequence".equalsIgnoreCase(colId) || allNullValCols.contains(colId)) continue;
            skipSanitizeHTMLValue = this.skipSanitizeHtmlValue(colId, auditRowNum);
            PropertyList colProps = this.getColumnProps(colId, unifiedColumns);
            if (colProps == null || !"Y".equals(colProps.getProperty("enabled", "Y"))) continue;
            columnValue = this.__CurrentAuditData.getValue(auditRowNum, colId);
            String prevColumnValue = "";
            if (auditRowNum > 0 && !"I".equalsIgnoreCase(auditOpFlag)) {
                prevColumnValue = this.__CurrentAuditData.getValue(auditRowNum - 1, colId);
                requiresHighlighting = !prevColumnValue.equals(columnValue);
            }
            boolean bl = isKnownCLOBCol = this.getKnownCLOBColumnProps(this.elementInfo.getElementTableId(), colId) != null && "Diff".equals(this.clobViewMode);
            if (isKnownCLOBCol) {
                try {
                    columnValue = this.getCLOBDiffHTML(colId, prevColumnValue, columnValue, this.__CurrentAuditData, auditRowNum);
                }
                catch (Exception e) {
                    this.logger.error("Couldn't de-serialize xml to object for Table: " + this.elementInfo.getElementTableId() + ", Column: " + colId + ". Show basic audit view.", e);
                    columnValue = this.convertXMLToString(columnValue, skipSanitizeHTMLValue);
                }
            } else {
                columnValue = this.convertXMLToString(columnValue, skipSanitizeHTMLValue);
            }
            html.append(this.getAuditRowsTableCell(colProps, columnValue, requiresHighlighting));
        }
        columnValue = isDeletedRow ? "<img title='" + this.__Tp.translate("Row Deleted") + "' src='WEB-CORE/imageref/computer_communication_and_media/data/tables/columns/rows/16/table_row_delete.png' border=0></img>" : ("I".equalsIgnoreCase(auditOpFlag) ? "<img title='" + this.__Tp.translate("Row Inserted") + "' src='WEB-CORE/imageref/computer_communication_and_media/data/tables/columns/rows/16/table_row_add.png' border=0></img>" : "");
        PropertyList auditOpFlagColProps = this.getColumnProps("auditopflag", unifiedColumns);
        auditOpFlagColProps.setProperty("width", "1");
        auditOpFlagColProps.setProperty("align", "right");
        html.append(this.getAuditRowsTableCell(auditOpFlagColProps, columnValue, false));
        columnValue = this.__CurrentAuditData.getValue(auditRowNum, "modby");
        String modByName = this.__CurrentAuditData.getString(auditRowNum, "tracelog_modbyname", "");
        if (modByName.length() > 0 && "Y".equals(this.elementInfo.getElementProperties().getProperty("showmodbyname", "Y"))) {
            columnValue = columnValue + " (" + modByName + ")";
        }
        PropertyList modByColProps = this.getColumnProps("modby", unifiedColumns);
        html.append(this.getAuditRowsTableCell(modByColProps, columnValue, false));
        columnValue = this.__CurrentAuditData.getValue(auditRowNum, "moddt");
        PropertyList modDtColProps = this.getColumnProps("moddt", unifiedColumns);
        html.append(this.getAuditRowsTableCell(modDtColProps, columnValue, false));
        for (int traceLogColCount = 0; traceLogColCount < traceLogColumns.size(); ++traceLogColCount) {
            PropertyList colProps = traceLogColumns.getPropertyList(traceLogColCount);
            String colId = colProps.getProperty("columnid");
            if (colId == null) {
                throw new SapphireException(nullColExceptionMsg.toString());
            }
            if ("signedflag".equalsIgnoreCase(colId)) {
                columnValue = this.__CurrentAuditData.getString(auditRowNum, "tracelog_signedflag", "N");
            } else if ("activity".equalsIgnoreCase(colId)) {
                columnValue = this.__CurrentAuditData.getString(auditRowNum, "tracelog_activity", "");
                columnValue = this.getActivityDisplayValue(this.__CurrentAuditData, auditRowNum, columnValue, this.__Tp);
            } else {
                columnValue = this.__CurrentAuditData.getValue(auditRowNum, "tracelog_" + colId);
            }
            html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
        }
        html.append("</tr>");
        return html.toString();
    }

    private String getAuditRowsTableCell(PropertyList column, String value, boolean requiresHighlighting) {
        StringBuffer html = new StringBuffer();
        String width = null;
        String align = null;
        String displayValue = null;
        if (column != null) {
            width = column.getProperty("width");
            align = column.getProperty("align", "left");
            displayValue = column.getProperty("displayvalue");
            html.append("<td valign='top' width='" + width + "' align='" + align + "' class='" + (requiresHighlighting ? this.elementInfo.css.getAuditCellHighlight() : this.elementInfo.css.getAuditCell()) + "' >");
            value = this.getValueToPrint(value, displayValue);
            if (value.contains("<img") || value.contains("<div") || value.contains("<table") || value.contains("<font")) {
                html.append(value);
            } else {
                html.append(SafeHTML.encodeForHTML(value));
            }
            html.append("</td>");
        } else {
            html.append("<td/>");
        }
        return html.toString();
    }

    private String getAuditRowsDynamicTableRow(int refAuditSeq, ArrayList<String> tableColumnsList, PropertyListCollection traceLogColumns, PropertyListCollection unifiedColumns, List<String> allNullValCols, String dynDivId, boolean isLastAuditSeq) throws SapphireException {
        StringBuffer html = new StringBuffer();
        String nullColExceptionMsg = this.__Tp.translate("NULL columnid encountered. Cannot retrieve data. ElementId: ") + this.elementid;
        String columnValue = "";
        DataSet filteredCurrentDynamicData = this.getAuditDynamicDataForAuditSeq(refAuditSeq);
        if (filteredCurrentDynamicData.getRowCount() > 0) {
            DateFormat primaryModDtFormat = this.__CurrentAuditData.getDateDisplayFormat("moddt");
            filteredCurrentDynamicData.sort("activitygroup");
            ArrayList<DataSet> grpCurrDynDataArr = filteredCurrentDynamicData.getGroupedDataSets("activitygroup");
            for (int groupCount = 0; groupCount < grpCurrDynDataArr.size(); ++groupCount) {
                html.append("<tr class='" + this.elementInfo.css.getDynamicAuditRowStyle(groupCount) + " " + dynDivId + "' style='display:none;'>");
                DataSet grpCurrDynData = grpCurrDynDataArr.get(groupCount);
                PropertyList colProps = this.getColumnProps("auditsequence", unifiedColumns);
                html.append(this.getAuditRowsTableCell(colProps, "", false));
                html.append("<td>&nbsp;</td>");
                for (String colId : tableColumnsList) {
                    if (allNullValCols.contains(colId) || (colProps = this.getColumnProps(colId, unifiedColumns)) == null) continue;
                    if (colId == null) {
                        throw new SapphireException(nullColExceptionMsg.toString());
                    }
                    if ("modby".equalsIgnoreCase(colId) || "moddt".equalsIgnoreCase(colId) || "auditsequence".equalsIgnoreCase(colId) || !"Y".equalsIgnoreCase(colProps.getProperty("enabled", "Y"))) continue;
                    int findRow = grpCurrDynData.findRow("columnid", colId);
                    columnValue = findRow > -1 ? grpCurrDynData.getValue(findRow, "newvalue") : "";
                    html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
                }
                colProps = new PropertyList();
                columnValue = "";
                html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
                colProps = this.getColumnProps("modby", unifiedColumns);
                columnValue = grpCurrDynData.getValue(0, "activityby");
                String modByName = grpCurrDynData.getString(0, "activitybyname", "");
                if (modByName.length() > 0 && "Y".equals(this.elementInfo.getElementProperties().getProperty("showmodbyname", "Y"))) {
                    columnValue = columnValue + " (" + modByName + ")";
                }
                html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
                colProps = this.getColumnProps("moddt", unifiedColumns);
                columnValue = primaryModDtFormat.format(grpCurrDynData.getCalendar(0, "activitydt").getTime());
                html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
                for (int traceLogColCount = 0; traceLogColCount < traceLogColumns.size(); ++traceLogColCount) {
                    colProps = traceLogColumns.getPropertyList(traceLogColCount);
                    String colId = colProps.getProperty("columnid");
                    if (colId == null) {
                        throw new SapphireException(nullColExceptionMsg.toString());
                    }
                    columnValue = "reason".equalsIgnoreCase(colId) ? grpCurrDynData.getValue(0, "reason") : "";
                    html.append(this.getAuditRowsTableCell(colProps, columnValue, false));
                }
                html.append("</tr>");
            }
            if (isLastAuditSeq) {
                html.append("<tr>");
                html.append("<td>&nbsp;</td>");
                html.append("<td>");
                html.append(this.getShowHideButtonDynamicAuditRowsHtml(IMG_PLUS_DYNAMIC_UNSAVED, "img_" + dynDivId + "_dynamic", "Show Un-persisted Activity.", "showHideDynamicDivAuditRows(\"" + dynDivId + "\")", "UnSaved"));
                html.append("</td>");
                html.append("</tr>");
            }
        }
        return html.toString();
    }

    private String getShowHideButtonDynamicAuditRowsHtml(String imageUrl, String id, String tip, String jsActionScript, String dynamicAuditType) {
        StringBuffer html = new StringBuffer();
        html.append("<img");
        html.append(" id='" + id + "'");
        html.append(" src='" + imageUrl + "'");
        html.append(" title='" + this.__Tp.translate(tip) + "'");
        html.append(" onclick='" + jsActionScript + "'");
        html.append(" data-dynamicaudittype='" + dynamicAuditType + "'");
        html.append(" style='cursor: pointer;'");
        html.append(" width='12px' height='12px'");
        html.append(" />");
        return html.toString();
    }

    private String getCollapsyExpandyCell(boolean isExpanded, String divId) {
        StringBuffer html = new StringBuffer();
        html.append("<td width='1px' valign='top' align='left' >");
        html.append(this.getCollapsyExpandyCellContent(isExpanded, divId));
        html.append("</td>");
        return html.toString();
    }

    private String getCollapsyExpandyCellContent(boolean isExpanded, String divId) {
        StringBuffer html = new StringBuffer("<img ");
        html.append("id='").append(this.getDataRowsDivImgId(divId)).append("' ");
        if (isExpanded) {
            html.append("src='WEB-OPAL/images/minus.gif' ");
        } else {
            html.append("src='WEB-OPAL/images/plus.gif' ");
        }
        html.append("onclick='showHideDiv(\"");
        html.append(divId).append("\" )' ");
        html.append("class='Outline' style='cursor: pointer' width='12' ");
        html.append("height='12' ");
        html.append(" />");
        return html.toString();
    }

    private String getChildElementRows(String key) {
        StringBuffer html = new StringBuffer();
        DataSet childElements = null;
        ElementInfo childElementInfo = null;
        String childElementId = null;
        childElements = this.elementsContainer.getChildElementsDS(this.elementid);
        int rowCount = childElements.getRowCount();
        for (int count = 0; count < rowCount; ++count) {
            try {
                childElementId = childElements.getValue(count, "elementid");
                childElementInfo = this.elementsContainer.getElementInfo(childElementId);
                if (!childElementInfo.isEnabled()) continue;
                html.append("<tr>");
                try {
                    html.append("<td width='100%' align='left'>");
                    html.append(this.getChildElementDataCell(key, childElementInfo));
                    html.append("</td>");
                }
                catch (Exception ex) {
                    this.logger.error(ex.getMessage(), ex);
                    html.append("<td>").append(ex.getMessage()).append("</td>");
                }
                html.append("</tr>");
                continue;
            }
            catch (SapphireException e) {
                html.append("<tr>");
                this.logger.error(e.getMessage(), e);
                html.append("<td>").append(e.getMessage()).append("</td>");
                html.append("</tr>");
            }
        }
        return html.toString();
    }

    private String getChildElementDataCell(String key, ElementInfo childElementInfo) throws Exception {
        StringBuffer html = new StringBuffer();
        AuditDetails childElement = null;
        HashMap topKeyFilter = this.createTopKeyFilter(key, childElementInfo);
        childElement = childElementInfo.getElement();
        childElement.initData(this.__Tp, this.connectionInfo, this.browser, key, topKeyFilter);
        html.append(childElement.getHtml());
        return html.toString();
    }

    private String getDivTagHtml(boolean isExpanded, String divId, Boolean isLazyLoad) {
        StringBuffer html = new StringBuffer();
        html.append("<div id='").append(divId).append("' style=");
        if (isExpanded) {
            html.append("'display:block' ");
        } else {
            html.append("'display:none' ");
            if (isLazyLoad != null && isLazyLoad.booleanValue()) {
                html.append(" ajaxReq='ajaxReq' ");
            }
        }
        html.append(" > ");
        return html.toString();
    }

    private String getCLOBDiffHTML(String columnId, String oldValue, String newValue, DataSet auditData, int auditDataRow) throws Exception {
        PropertyList clobColumnInfo = this.getKnownCLOBColumnProps(this.elementInfo.getElementTableId(), columnId);
        String clobColType = clobColumnInfo.getProperty("clobtype");
        String diffHTML = "";
        if (CLOB_TYPE_PROPERTYLIST.equals(clobColType)) {
            PropertyTree propertyTree;
            PropertyList oldValueProps = new PropertyList();
            PropertyList newValueProps = new PropertyList();
            if (oldValue != null && oldValue.length() > 0) {
                oldValueProps.setPropertyList(oldValue);
            }
            if (newValue != null && newValue.length() > 0) {
                newValueProps.setPropertyList(newValue);
            }
            PropertyDefinitionList propertyDefinitionList = (propertyTree = this.getCLOBColumnPropertyTree(this.elementInfo.getElementTableId(), columnId, auditData, auditDataRow)) == null ? null : propertyTree.getPropertyDefinitionList();
            ConfigReportContent configReportContent = new ConfigReportContent("AuditView", this.getTranslationProcessor());
            ConfigReportContent renderedCRC = configReportContent.renderPropertyListDiff(newValueProps, oldValueProps, propertyDefinitionList, false, true, true);
            diffHTML = renderedCRC.toString();
        } else if (CLOB_TYPE_PROPERTYTREE_VALUE.equals(clobColType)) {
            PropertyTree propertyTree = this.getCLOBColumnPropertyTree(this.elementInfo.getElementTableId(), columnId, auditData, auditDataRow);
            PropertyDefinitionList propertyDefinitionList = propertyTree == null ? null : propertyTree.getPropertyDefinitionList();
            PropertyTree oldValueTree = new PropertyTree();
            PropertyTree newValueTree = new PropertyTree();
            if (oldValue != null && oldValue.length() > 0) {
                oldValueTree.setValueXML(oldValue);
            }
            if (newValue != null && newValue.length() > 0) {
                newValueTree.setValueXML(newValue);
            }
            ArrayList oldNodes = oldValueTree.getAllNodes();
            ArrayList newNodes = newValueTree.getAllNodes();
            HashSet allUniqueNodes = new HashSet();
            oldNodes.forEach(el -> {
                Node node = (Node)el;
                allUniqueNodes.add(node.getId());
            });
            newNodes.forEach(el -> {
                Node node = (Node)el;
                allUniqueNodes.add(node.getId());
            });
            StringBuilder nodeDiffHtml = new StringBuilder();
            for (String nodeId : allUniqueNodes) {
                PropertyList newNodePL;
                Node oldNode = oldValueTree.getNode(nodeId);
                Node newNode = newValueTree.getNode(nodeId);
                PropertyList oldNodePL = oldNode == null ? new PropertyList() : oldNode.getPropertyList();
                PropertyList propertyList = newNodePL = newNode == null ? new PropertyList() : newNode.getPropertyList();
                ConfigReportContent configReportContent = new ConfigReportContent("AuditView", this.getTranslationProcessor());
                ConfigReportContent renderedCRC = configReportContent.renderPropertyListDiff(newNodePL, oldNodePL, propertyDefinitionList, false, true, true);
                if (!renderedCRC.getFoundDiff()) continue;
                nodeDiffHtml.append("<b>Node: " + nodeId + "</b>");
                nodeDiffHtml.append(renderedCRC.toString());
            }
            diffHTML = nodeDiffHtml.toString();
        } else if (CLOB_TYPE_PROPERTYTREE_DEF.equals(clobColType)) {
            ConfigReportContent configReportContent;
            ConfigReportContent renderedContent;
            PropertyTree oldPtree = new PropertyTree();
            PropertyTree newPTree = new PropertyTree();
            if (oldValue != null && oldValue.length() > 0) {
                oldPtree.setDefinitionXML(oldValue);
            }
            if (newValue != null && newValue.length() > 0) {
                newPTree.setDefinitionXML(newValue);
            }
            if ((renderedContent = (configReportContent = new ConfigReportContent("AuditView", this.getTranslationProcessor())).renderPropertyDefinitionList("root", "", newPTree.getPropertyDefinitionList(), oldPtree.getPropertyDefinitionList(), this.getTranslationProcessor(), false)).getFoundDiff()) {
                diffHTML = renderedContent.toString();
            }
        }
        return diffHTML;
    }

    private void flattenCLOBData2Columns() throws Exception {
        if ("Flat".equalsIgnoreCase(this.clobViewMode) && "Changed".equalsIgnoreCase(this.renderingMode)) {
            String tableId = this.elementInfo.getElementTableId();
            if (this.__AuditData != null) {
                PropertyListCollection unifiedColumns = this.elementInfo.getUnifiedColumns();
                for (int i = 0; i < this.__AuditData.getRowCount(); ++i) {
                    String[] columns;
                    for (String columnId : columns = this.__AuditData.getColumns()) {
                        PropertyList knownCLOBColumnProps = this.getKnownCLOBColumnProps(tableId, columnId);
                        if (knownCLOBColumnProps == null) continue;
                        String clobType = knownCLOBColumnProps.getProperty("clobtype");
                        String colValue = this.__AuditData.getString(i, columnId);
                        if (colValue != null && colValue.length() > 0) {
                            PropertyDefinitionList propertyDefinitionList;
                            PropertyTree propertyTree;
                            PropertyList ancestorColProps = this.elementInfo.getUnifiedColumns().getPropertyList(columnId);
                            if (ancestorColProps == null) {
                                ancestorColProps = new PropertyList();
                                ancestorColProps.setProperty("columnid", columnId);
                                ancestorColProps.setProperty("title", columnId);
                            }
                            ArrayDeque<PropertyList> ancestorCols = new ArrayDeque<PropertyList>();
                            ancestorCols.push(ancestorColProps);
                            if (CLOB_TYPE_PROPERTYLIST.equals(clobType)) {
                                PropertyList valueProps = new PropertyList();
                                valueProps.setPropertyList(colValue);
                                if (valueProps != null) {
                                    PropertyTree propertyTree2 = this.getCLOBColumnPropertyTree(tableId, columnId, this.__AuditData, i);
                                    PropertyDefinitionList propertyDefinitionList2 = propertyTree2 == null ? null : propertyTree2.getPropertyDefinitionList();
                                    this.flattenCLOBPropertyList(valueProps, propertyDefinitionList2, this.__AuditData, i, ancestorCols);
                                } else {
                                    this.logger.error("PropertyList couldn't get parsed: " + columnId);
                                }
                            } else if (CLOB_TYPE_PROPERTYTREE_VALUE.equals(clobType)) {
                                propertyTree = this.getCLOBColumnPropertyTree(tableId, columnId, this.__AuditData, i);
                                propertyDefinitionList = propertyTree == null ? null : propertyTree.getPropertyDefinitionList();
                                PropertyTree valueTree = new PropertyTree();
                                valueTree.setValueXML(colValue);
                                if (valueTree != null) {
                                    ArrayList allNodes = valueTree.getAllNodes();
                                    for (int j = 0; j < allNodes.size(); ++j) {
                                        Node node = (Node)allNodes.get(j);
                                        ancestorColProps = new PropertyList();
                                        ancestorColProps.setProperty("columnid", node.getNodeId());
                                        ancestorColProps.setProperty("title", node.getNodeId());
                                        ancestorCols.push(ancestorColProps);
                                        this.flattenCLOBPropertyList(node.getPropertyList(), propertyDefinitionList, this.__AuditData, i, ancestorCols);
                                        ancestorCols.pop();
                                    }
                                    PropertyDefaultList propertyDefaultList = valueTree.getPropertyDefaultList();
                                    PropertyList propertyList = this.convertPropertyDefaultToPropertyList(propertyDefaultList);
                                    ancestorColProps = new PropertyList();
                                    ancestorColProps.setProperty("columnid", "__ROOT");
                                    ancestorColProps.setProperty("title", "__ROOT");
                                    ancestorCols.push(ancestorColProps);
                                    this.flattenCLOBPropertyList(propertyList, propertyDefinitionList, this.__AuditData, i, ancestorCols);
                                    ancestorCols.pop();
                                } else {
                                    this.logger.error("Unable to re-generate PropertyTree from valuetree clob: auditData rowNum: " + i);
                                }
                            } else if (CLOB_TYPE_PROPERTYTREE_DEF.equals(clobType)) {
                                propertyTree = new PropertyTree();
                                propertyTree.setDefinitionXML(colValue);
                                propertyDefinitionList = propertyTree.getPropertyDefinitionList();
                                ancestorColProps = new PropertyList();
                                ancestorColProps.setProperty("columnid", "__DEFINITIONLIST");
                                ancestorColProps.setProperty("title", "__DEFINITIONLIST");
                                ancestorCols.push(ancestorColProps);
                                this.flattenCLOBPropertyDefinitionList(propertyDefinitionList, this.__AuditData, i, ancestorCols);
                                ancestorCols.pop();
                                PropertyDependencyList propertyDependencyList = propertyTree.getPropertyDependencyList();
                                ancestorColProps = new PropertyList();
                                ancestorColProps.setProperty("columnid", "__DEPENDENCYLIST");
                                ancestorColProps.setProperty("title", "__DEPENDENCYLIST");
                                ancestorCols.push(ancestorColProps);
                                this.flattenCLOBPropertyDependencyList(propertyDependencyList, this.__AuditData, i, ancestorCols);
                                ancestorCols.pop();
                            }
                        }
                        unifiedColumns.remove(unifiedColumns.find("columnid", columnId));
                        this.elementInfo.getTableColumns().remove(columnId);
                    }
                }
            }
            if (this.__AuditDynamicData != null) {
                // empty if block
            }
        }
    }

    private void flattenCLOBPropertyList(PropertyList propertyList, PropertyDefinitionList propertyDefinitionList, DataSet auditData, int dataRow, ArrayDeque<PropertyList> ancestorPropDefStack) {
        if (propertyList != null) {
            for (Object propertyId : propertyList.keySet()) {
                Object propValue = propertyList.get(propertyId);
                this.flattenCLOBPropertyList((String)propertyId, propValue, propertyDefinitionList, auditData, dataRow, ancestorPropDefStack);
            }
        } else {
            this.logger.error("Invalid PropertyList found. Can't flatten row num: " + dataRow);
        }
    }

    private void flattenCLOBPropertyList(String currentPropId, Object currentPropValueObject, PropertyDefinitionList definitionList, DataSet auditData, int dataRow, ArrayDeque<PropertyList> ancestorCols) {
        block8: {
            PropertyDefinition currentPropDef;
            block7: {
                PropertyDefinition propertyDefinition = currentPropDef = definitionList == null ? null : definitionList.getPropertyDef(currentPropId);
                if (!(currentPropValueObject instanceof String)) break block7;
                String currentValue = (String)currentPropValueObject;
                if (currentValue.length() <= 0) break block8;
                StringBuffer ancestorIds = new StringBuffer();
                StringBuffer ancestorTitles = new StringBuffer();
                Iterator<PropertyList> it = ancestorCols.descendingIterator();
                while (it.hasNext()) {
                    PropertyList ancestorColPL = it.next();
                    ancestorIds.append(ancestorIds.length() > 0 ? "." : "").append(ancestorColPL.getProperty("columnid"));
                    ancestorTitles.append(ancestorTitles.length() > 0 ? " > " : "").append(ancestorColPL.getProperty("title"));
                }
                String finalColId = ancestorIds.append(".").append(currentPropDef == null ? currentPropId : currentPropDef.getId()).toString();
                String finalColTitle = ancestorTitles.append(" > ").append(currentPropDef == null ? currentPropId : currentPropDef.getTitle()).toString();
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, currentValue);
                break block8;
            }
            if (currentPropValueObject instanceof PropertyList) {
                PropertyList currentPL = (PropertyList)currentPropValueObject;
                for (Object nestedPropId : currentPL.keySet()) {
                    String nestedPropIdStr = (String)nestedPropId;
                    PropertyDefinitionList nestedDefList = currentPropDef == null ? null : currentPropDef.getPropertyDefinitionList();
                    PropertyList currentPropDefColumnProps = new PropertyList();
                    currentPropDefColumnProps.setProperty("columnid", currentPropDef == null ? currentPropId : currentPropDef.getId());
                    currentPropDefColumnProps.setProperty("title", currentPropDef == null ? currentPropId : currentPropDef.getTitle());
                    ancestorCols.addFirst(currentPropDefColumnProps);
                    this.flattenCLOBPropertyList(nestedPropIdStr, currentPL.get(nestedPropIdStr), nestedDefList, auditData, dataRow, ancestorCols);
                    ancestorCols.removeFirst();
                }
            } else if (currentPropValueObject instanceof PropertyListCollection) {
                String titlePropertyId;
                PropertyListCollection currentPLC = (PropertyListCollection)currentPropValueObject;
                PropertyDefinitionList nestedDefList = currentPropDef == null ? null : currentPropDef.getPropertyDefinitionList();
                String uniquePropertyId = nestedDefList == null ? "" : nestedDefList.getUniqueIdPropertyId();
                String string = titlePropertyId = nestedDefList == null ? "" : nestedDefList.getTitlePropertyId();
                String uniqueIdPropertyId = uniquePropertyId != null && uniquePropertyId.length() > 0 ? uniquePropertyId : (titlePropertyId != null && titlePropertyId.length() > 0 ? titlePropertyId : "");
                boolean uniqueIdProvided = uniqueIdPropertyId.length() > 0;
                String collectionUniqueSeparator = "";
                for (int i = 0; i < currentPLC.size(); ++i) {
                    PropertyList nestedPL = currentPLC.getPropertyList(i);
                    collectionUniqueSeparator = uniqueIdProvided ? nestedPL.getProperty(uniqueIdPropertyId) : String.valueOf(i);
                    for (Object nestedPropId : nestedPL.keySet()) {
                        String nestedPropIdStr = (String)nestedPropId;
                        Object nestedPropValue = nestedPL.get(nestedPropIdStr);
                        PropertyList currentPropDefColumnProps = new PropertyList();
                        currentPropDefColumnProps.setProperty("columnid", (currentPropDef == null ? currentPropId : currentPropDef.getId()) + "_" + collectionUniqueSeparator);
                        currentPropDefColumnProps.setProperty("title", (currentPropDef == null ? currentPropId : currentPropDef.getTitle()) + "_" + collectionUniqueSeparator);
                        ancestorCols.addFirst(currentPropDefColumnProps);
                        this.flattenCLOBPropertyList(nestedPropIdStr, nestedPropValue, nestedDefList, auditData, dataRow, ancestorCols);
                        ancestorCols.removeFirst();
                    }
                }
            }
        }
    }

    @Deprecated
    private void flattenCLOBPropertyList(PropertyDefinition currentPropDef, PropertyList valueList, DataSet auditData, int dataRow, ArrayDeque<PropertyList> ancestorPropDefQ) {
        block8: {
            block7: {
                if (!"simple".equals(currentPropDef.getType())) break block7;
                String value = valueList.getProperty(currentPropDef.getId());
                if (value.length() <= 0) break block8;
                StringBuffer ancestorIds = new StringBuffer();
                StringBuffer ancestorTitles = new StringBuffer();
                Iterator<PropertyList> it = ancestorPropDefQ.descendingIterator();
                while (it.hasNext()) {
                    PropertyList parentPropDef = it.next();
                    ancestorIds.append(ancestorIds.length() > 0 ? "." : "").append(parentPropDef.getProperty("columnid"));
                    ancestorTitles.append(ancestorTitles.length() > 0 ? " > " : "").append(parentPropDef.getProperty("title"));
                }
                String finalColId = ancestorIds.append(".").append(currentPropDef.getId()).toString();
                String finalColTitle = ancestorTitles.append(" > ").append(currentPropDef.getTitle()).toString();
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, value);
                break block8;
            }
            if ("propertylist".equals(currentPropDef.getType())) {
                PropertyDefinitionList nestedPropDefList = currentPropDef.getPropertyDefinitionList();
                for (int i = 0; i < nestedPropDefList.size(); ++i) {
                    PropertyDefinition nestedPropDef = (PropertyDefinition)nestedPropDefList.get(i);
                    PropertyList nestedValueList = valueList.getPropertyListNotNull(currentPropDef.getId());
                    PropertyList currentPropDefColumnProps = new PropertyList();
                    currentPropDefColumnProps.setProperty("columnid", currentPropDef.getId());
                    currentPropDefColumnProps.setProperty("title", currentPropDef.getTitle());
                    ancestorPropDefQ.addFirst(currentPropDefColumnProps);
                    this.flattenCLOBPropertyList(nestedPropDef, nestedValueList, auditData, dataRow, ancestorPropDefQ);
                    ancestorPropDefQ.removeFirst();
                }
            } else if ("collection".equals(currentPropDef.getType())) {
                PropertyDefinitionList nestedPropDefList = currentPropDef.getPropertyDefinitionList();
                PropertyListCollection nestedValueCollection = valueList.getCollectionNotNull(currentPropDef.getId());
                String uniquePropertyId = nestedPropDefList.getUniqueIdPropertyId();
                String titlePropertyId = nestedPropDefList.getTitlePropertyId();
                String uniqueIdPropertyId = uniquePropertyId != null && uniquePropertyId.length() > 0 ? uniquePropertyId : (titlePropertyId != null && titlePropertyId.length() > 0 ? titlePropertyId : "");
                boolean uniqueIdProvided = uniqueIdPropertyId.length() > 0;
                String collectionUniqueSeparator = "";
                for (int i = 0; i < nestedValueCollection.size(); ++i) {
                    PropertyList nestedValueList = nestedValueCollection.getPropertyList(i);
                    collectionUniqueSeparator = uniqueIdProvided ? nestedValueList.getProperty(uniqueIdPropertyId) : String.valueOf(i);
                    for (int j = 0; j < nestedPropDefList.size(); ++j) {
                        PropertyDefinition nestedPropDef = (PropertyDefinition)nestedPropDefList.get(j);
                        PropertyList currentPropDefColumnProps = new PropertyList();
                        currentPropDefColumnProps.setProperty("columnid", currentPropDef.getId() + "_" + collectionUniqueSeparator);
                        currentPropDefColumnProps.setProperty("title", currentPropDef.getTitle() + "_" + collectionUniqueSeparator);
                        ancestorPropDefQ.addFirst(currentPropDefColumnProps);
                        this.flattenCLOBPropertyList(nestedPropDef, nestedValueList, auditData, dataRow, ancestorPropDefQ);
                        ancestorPropDefQ.removeFirst();
                    }
                }
            }
        }
    }

    private void flattenCLOBPropertyDefinitionList(PropertyDefinitionList propertyDefinitionList, DataSet auditData, int dataRow, ArrayDeque<PropertyList> ancestorCols) {
        if (propertyDefinitionList != null) {
            StringBuffer ancestorIds = new StringBuffer();
            StringBuffer ancestorTitles = new StringBuffer();
            Iterator<PropertyList> it = ancestorCols.descendingIterator();
            while (it.hasNext()) {
                PropertyList ancestorColPL = it.next();
                ancestorIds.append(ancestorIds.length() > 0 ? "." : "").append(ancestorColPL.getProperty("columnid"));
                ancestorTitles.append(ancestorTitles.length() > 0 ? " > " : "").append(ancestorColPL.getProperty("title"));
            }
            for (Object el : propertyDefinitionList) {
                PropertyDefinition propDef = (PropertyDefinition)el;
                String finalPropId = ancestorIds + "." + propDef.getId();
                String finalPropTitle = ancestorTitles + " > " + propDef.getTitle();
                if ("simple".equalsIgnoreCase(propDef.getType())) {
                    String propDefAttrColId = finalPropId + "." + "id";
                    String propDefAttrColTitle = finalPropTitle + " > " + "Id";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getId());
                    propDefAttrColId = finalPropId + "." + "type";
                    propDefAttrColTitle = finalPropTitle + " > " + "Type";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getType());
                    propDefAttrColId = finalPropId + "." + "title";
                    propDefAttrColTitle = finalPropTitle + " > " + "Title";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getTitle());
                    propDefAttrColId = finalPropId + "." + "editor";
                    propDefAttrColTitle = finalPropTitle + " > " + "Editor";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getEditor());
                    propDefAttrColId = finalPropId + "." + "showif";
                    propDefAttrColTitle = finalPropTitle + " > " + "Show If";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getShowIf());
                    propDefAttrColId = finalPropId + "." + "help";
                    propDefAttrColTitle = finalPropTitle + " > " + "Help";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getHelp());
                    propDefAttrColId = finalPropId + "." + "sdcid";
                    propDefAttrColTitle = finalPropTitle + " > " + "SDC Id";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getSdcid());
                    propDefAttrColId = finalPropId + "." + "extendedwhere";
                    propDefAttrColTitle = finalPropTitle + " > " + "Extended Where";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getExtendedWhere());
                    propDefAttrColId = finalPropId + "." + "values";
                    propDefAttrColTitle = finalPropTitle + " > " + "Values";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getValues());
                    propDefAttrColId = finalPropId + "." + "translate";
                    propDefAttrColTitle = finalPropTitle + " > " + "Translate";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getTranslate());
                    propDefAttrColId = finalPropId + "." + "matchproperty";
                    propDefAttrColTitle = finalPropTitle + " > " + "Match Property";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, propDef.getMatchProperty());
                    propDefAttrColId = finalPropId + "." + "deprecated";
                    propDefAttrColTitle = finalPropTitle + " > " + "Deprecated";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, String.valueOf(propDef.isDeprecated()));
                    propDefAttrColId = finalPropId + "." + "expression";
                    propDefAttrColTitle = finalPropTitle + " > " + "Expression";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, String.valueOf(propDef.isExpression()));
                    propDefAttrColId = finalPropId + "." + "resolution";
                    propDefAttrColTitle = finalPropTitle + " > " + "Resolution";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, String.valueOf(propDef.isResolution()));
                    propDefAttrColId = finalPropId + "." + "advanced";
                    propDefAttrColTitle = finalPropTitle + " > " + "Advanced";
                    this.addNewUnifiedColumn(propDefAttrColId, propDefAttrColTitle);
                    auditData.setString(dataRow, propDefAttrColId, String.valueOf(propDef.isAdvanced()));
                    continue;
                }
                if (!"propertylist".equalsIgnoreCase(propDef.getType()) && !"collection".equalsIgnoreCase(propDef.getType())) continue;
                PropertyDefinitionList nestedPropDefList = propDef.getPropertyDefinitionList();
                String propDefDispOptsColId = finalPropId + "." + "color";
                String propDefDispOptsColTitle = finalPropTitle + " > " + "Color";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getColor());
                propDefDispOptsColId = finalPropId + "." + "labelSingular";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Singular Label";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getLabelSingular());
                propDefDispOptsColId = finalPropId + "." + "labelPlural";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Plural Label";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getLabelPlural());
                propDefDispOptsColId = finalPropId + "." + "direction";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Direction";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getDirecttion());
                propDefDispOptsColId = finalPropId + "." + "titlePropertyId";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Title Property";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getTitlePropertyId());
                propDefDispOptsColId = finalPropId + "." + "tableStyle";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Table Style";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getTableStyle());
                propDefDispOptsColId = finalPropId + "." + "showhide";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Show Hide Buttons";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, String.valueOf(nestedPropDefList.isShowhide()));
                propDefDispOptsColId = finalPropId + "." + "allowRoles";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Allow Roles Security";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, String.valueOf(nestedPropDefList.isAllowRoles()));
                propDefDispOptsColId = finalPropId + "." + "deprecated";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Deprecated";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, String.valueOf(nestedPropDefList.isDeprecated()));
                propDefDispOptsColId = finalPropId + "." + "advanced";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Advanced";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, String.valueOf(nestedPropDefList.isAdvanced()));
                propDefDispOptsColId = finalPropId + "." + "addMethod";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Add Method Method";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getAddMethod());
                propDefDispOptsColId = finalPropId + "." + "uniqueIdPropertyId";
                propDefDispOptsColTitle = finalPropTitle + " > " + "Check ID Property";
                this.addNewUnifiedColumn(propDefDispOptsColId, propDefDispOptsColTitle);
                auditData.setString(dataRow, propDefDispOptsColId, nestedPropDefList.getUniqueIdPropertyId());
                PropertyList currentPropDefColumnProps = new PropertyList();
                currentPropDefColumnProps.setProperty("columnid", propDef.getId());
                currentPropDefColumnProps.setProperty("title", propDef.getTitle());
                ancestorCols.addFirst(currentPropDefColumnProps);
                this.flattenCLOBPropertyDefinitionList(nestedPropDefList, auditData, dataRow, ancestorCols);
                ancestorCols.removeFirst();
            }
        }
    }

    private void flattenCLOBPropertyDependencyList(PropertyDependencyList propertyDependencyList, DataSet auditData, int dataRow, ArrayDeque<PropertyList> ancestorCols) {
        if (propertyDependencyList != null) {
            StringBuffer ancestorIds = new StringBuffer();
            StringBuffer ancestorTitles = new StringBuffer();
            Iterator<PropertyList> it = ancestorCols.descendingIterator();
            while (it.hasNext()) {
                PropertyList ancestorCol = it.next();
                ancestorIds.append(ancestorIds.length() > 0 ? "." : "").append(ancestorCol.getProperty("columnid"));
                ancestorTitles.append(ancestorTitles.length() > 0 ? " > " : "").append(ancestorCol.getProperty("title"));
            }
            for (int i = 0; i < propertyDependencyList.size(); ++i) {
                PropertyDependency dependency = (PropertyDependency)propertyDependencyList.get(i);
                String elementId = dependency.getElementid();
                String finalColId = ancestorIds + "." + elementId + "." + "elementid";
                String finalColTitle = ancestorTitles + "." + elementId + " > " + "Id";
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, dependency.getElementid());
                finalColId = ancestorIds + "." + elementId + " > " + "propertytreeid";
                finalColTitle = ancestorTitles + "." + elementId + " > " + "Element Type";
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, dependency.getPropertytreeid());
                finalColId = ancestorIds + "." + elementId + "." + "mandatory";
                finalColTitle = ancestorTitles + "." + elementId + " > " + "Mandatory";
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, String.valueOf(dependency.isMandatory()));
                finalColId = ancestorIds + "." + elementId + "." + "description";
                finalColTitle = ancestorTitles + "." + elementId + " > " + "Description";
                this.addNewUnifiedColumn(finalColId, finalColTitle);
                auditData.setString(dataRow, finalColId, dependency.getDescription());
            }
        }
    }

    private void addNewUnifiedColumn(String columnId, String title) {
        ArrayList tableColumns;
        PropertyListCollection unifiedColumns = this.elementInfo.getUnifiedColumns();
        PropertyList columnProps = unifiedColumns.find("columnid", columnId);
        if (columnProps == null) {
            columnProps = new PropertyList();
            columnProps.setProperty("columnid", columnId);
            columnProps.setProperty("title", title);
            unifiedColumns.add(columnProps);
        }
        if (!(tableColumns = this.elementInfo.getTableColumns()).contains(columnId)) {
            tableColumns.add(columnId);
        }
    }

    private PropertyList convertPropertyDefaultToPropertyList(PropertyDefaultList propertyDefaultList) {
        PropertyList pl = null;
        if (propertyDefaultList != null) {
            PropertyList finalPl = pl = new PropertyList();
            propertyDefaultList.values().forEach(el -> {
                PropertyDefault propertyDefault = (PropertyDefault)el;
                if ("simple".equalsIgnoreCase(propertyDefault.getType())) {
                    finalPl.setProperty(propertyDefault.getId(), propertyDefault.getValue());
                } else if ("propertylist".equalsIgnoreCase(propertyDefault.getType()) || "collection".equalsIgnoreCase(propertyDefault.getType())) {
                    PropertyDefaultList nestedPropDefaultList = propertyDefault.getPropertyDefaultList();
                    PropertyList nestedPL = this.convertPropertyDefaultToPropertyList(nestedPropDefaultList);
                    finalPl.setProperty(propertyDefault.getId(), nestedPL);
                }
            });
        }
        return pl;
    }

    private static void setupCLOBColumnsInfo() {
        PropertyList tableColumns = new PropertyList();
        PropertyList columnProps = new PropertyList();
        tableColumns = new PropertyList();
        knownCLOBColumns.put("app", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_FILE);
        columnProps.put("propertytreecolumnfilepath", "com/labvantage/sapphire/modules/apps/appdef.xml");
        columnProps = new PropertyList();
        tableColumns.setProperty("productvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_FILE);
        columnProps.put("propertytreecolumnfilepath", "com/labvantage/sapphire/modules/apps/appdef.xml");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("editorstyle", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("editordefinition", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_FILE);
        columnProps.put("propertytreecolumnfilepath", "com/labvantage/sapphire/pageelements/propertybuilder/editorstyledef.xml");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("gizmodef", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("productvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("instrument", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("collectorvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "collectorpropertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("instrumentmodel", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("collectorvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "collectorpropertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("propertytree", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYTREE_VALUE);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        columnProps = new PropertyList();
        tableColumns.setProperty("definitiontree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYTREE_DEF);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("portal", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_FILE);
        columnProps.put("propertytreecolumnfilepath", "com/labvantage/sapphire/modules/portal/portaldef.xml");
        columnProps = new PropertyList();
        tableColumns.setProperty("productvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_FILE);
        columnProps.put("propertytreecolumnfilepath", "com/labvantage/sapphire/modules/portal/portaldef.xml");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("scheduleplanitem", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("scheduleplandefaults", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("scheduleconditiondefaults", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("taskdefstep", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        columnProps = new PropertyList();
        tableColumns.setProperty("productvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("webpagepropertytree", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("valuetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        columnProps = new PropertyList();
        tableColumns.setProperty("productvaluetree", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        tableColumns = new PropertyList();
        knownCLOBColumns.put("worksheetitem", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("config", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_DB);
        columnProps.put("propertytreecolumnid", "propertytreeid");
        columnProps = new PropertyList();
        tableColumns.setProperty("options", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_NONE);
        tableColumns = new PropertyList();
        knownCLOBColumns.put("worksheetsection", tableColumns);
        columnProps = new PropertyList();
        tableColumns.setProperty("options", columnProps);
        columnProps.put("clobtype", CLOB_TYPE_PROPERTYLIST);
        columnProps.put("propertytreedefsource", CLOB_DEFSOURCE_NONE);
    }

    private PropertyList getKnownCLOBColumnProps(String tableId, String columnId) {
        if (knownCLOBColumns.containsKey(tableId)) {
            PropertyList tableInfo = knownCLOBColumns.getPropertyList(tableId);
            return tableInfo.getPropertyList(columnId);
        }
        return null;
    }

    private boolean isKnownCLOBColTable(String tableId) {
        return knownCLOBColumns.containsKey(tableId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PropertyTree getCLOBColumnPropertyTree(String tableId, String columnId, DataSet auditData, int dataRow) throws Exception {
        PropertyList columnInfo;
        String clobType;
        PropertyList tableInfo;
        PropertyTree propertyTree = null;
        PropertyDefinitionList propertyDefinitionList = null;
        if (knownCLOBColumns.containsKey(tableId.toLowerCase()) && (tableInfo = knownCLOBColumns.getPropertyList(tableId)).containsKey(columnId) && (CLOB_TYPE_PROPERTYTREE_VALUE.equals(clobType = (columnInfo = tableInfo.getPropertyList(columnId)).getProperty("clobtype")) || CLOB_TYPE_PROPERTYTREE_DEF.equals(clobType) || CLOB_TYPE_PROPERTYLIST.equals(clobType))) {
            String propertyTreeIdCol;
            String propertyTreeId;
            String defSource = columnInfo.getProperty("propertytreedefsource");
            if (CLOB_DEFSOURCE_FILE.equals(defSource)) {
                String propertyDefPath = columnInfo.getProperty("propertytreecolumnfilepath");
                if (this.PROPERTYDEF_CACHE.containsKey("FILE;" + propertyDefPath)) {
                    propertyTree = this.PROPERTYDEF_CACHE.get("FILE;" + propertyDefPath);
                } else {
                    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propertyDefPath);
                    if (inputStream != null) {
                        try {
                            PropertyTree tree = new PropertyTree();
                            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
                            handler.setXMLString(FileUtil.getInputStreamString(inputStream));
                            handler.setPrintStream(null);
                            SaxUtil.parseString(handler);
                            propertyTree = tree;
                            this.PROPERTYDEF_CACHE.put("FILE;" + propertyDefPath, propertyTree);
                            this.logger.info("Retrieved PropertyTree Definition from file (and cached): " + propertyDefPath);
                        }
                        finally {
                            inputStream.close();
                        }
                    } else {
                        this.logger.warn("Property definition could not be obtained from: " + propertyDefPath);
                    }
                }
            } else if (CLOB_DEFSOURCE_DB.equals(defSource) && (propertyTreeId = auditData.getString(dataRow, propertyTreeIdCol = columnInfo.getProperty("propertytreecolumnid"), "")).length() > 0) {
                if (this.PROPERTYDEF_CACHE.containsKey("DATABASE;" + propertyTreeId)) {
                    propertyTree = this.PROPERTYDEF_CACHE.get("DATABASE;" + propertyTreeId);
                } else {
                    propertyDefinitionList = new WebAdminProcessor(this.getConnectionId()).getPropertyDefinitionList(propertyTreeId);
                    propertyTree = new PropertyTree(propertyTreeId);
                    propertyTree.setPropertyDefinitionList(propertyDefinitionList);
                    this.PROPERTYDEF_CACHE.put("DATABASE;" + propertyTreeId, propertyTree);
                    this.logger.info("Retrieved PropertyTree Definition from db (and cached): " + propertyTreeId);
                }
            }
        }
        return propertyTree;
    }

    private List<String> getAllNullValueColumns(ArrayList<String> tableColumnsList, PropertyListCollection unifiedColumns) throws SapphireException {
        ArrayList<String> columnList = new ArrayList<String>();
        String nullColExceptionMsg = this.__Tp.translate("NULL columnid encountered. Cannot retrieve data. ElementId: ") + this.elementid;
        for (String colId : tableColumnsList) {
            boolean isKnownCLOBCol;
            if (colId == null) {
                throw new SapphireException(nullColExceptionMsg.toString());
            }
            boolean notNullValFound = false;
            boolean bl = isKnownCLOBCol = this.getKnownCLOBColumnProps(this.elementInfo.getElementTableId(), colId) != null && "AuditRows".equalsIgnoreCase(this.renderingMode);
            if (isKnownCLOBCol) {
                notNullValFound = false;
            } else {
                for (int count = 0; count < this.__CurrentAuditData.getRowCount(); ++count) {
                    PropertyList colProps = this.getColumnProps(colId, unifiedColumns);
                    if (colProps == null || this.__CurrentAuditData.getValue(count, colId).length() <= 0) continue;
                    notNullValFound = true;
                    break;
                }
            }
            if (notNullValFound || this.__CurrentAuditDynamicData.findRow("columnid", colId) != -1) continue;
            columnList.add(colId);
        }
        return columnList;
    }

    private String renderPostScripts() throws SapphireException {
        StringBuilder scripts = new StringBuilder();
        if (this.elementInfo.getElementProperties().getProperty("parentid", "").length() == 0) {
            boolean isKnownCLOBColTable = false;
            DataSet elements = this.elementsContainer.getElementsList();
            for (int i = 0; i < elements.getRowCount(); ++i) {
                String elementId = elements.getString(i, "elementid", "");
                ElementInfo elementInfo = this.elementsContainer.getElementInfo(elementId);
                String tableId = elementInfo.getElementTableId();
                if (!this.isKnownCLOBColTable(tableId)) continue;
                isKnownCLOBColTable = true;
                break;
            }
            if ("AuditRows".equalsIgnoreCase(this.renderingMode) || !isKnownCLOBColTable) {
                scripts.append("<script type='text/javascript'>hideCLOBButton();</script>");
            }
        }
        return scripts.toString();
    }

    private boolean isDynamicAuditDataSpecial(int refAuditSeq) {
        DataSet currentAuditSeqDynamicData = this.getAuditDynamicDataForAuditSeq(refAuditSeq);
        currentAuditSeqDynamicData.sort("columnid");
        ArrayList<DataSet> groupedArr = currentAuditSeqDynamicData.getGroupedDataSets("columnid");
        for (int i = 0; i < groupedArr.size(); ++i) {
            DataSet groupItem = groupedArr.get(i);
            if (groupItem.getRowCount() <= 1) continue;
            return true;
        }
        return false;
    }

    private DataSet getAuditDynamicDataForAuditSeq(int refAuditSeq) {
        HashMap<String, BigDecimal> filterMap = new HashMap<String, BigDecimal>();
        filterMap.put("refauditsequence", new BigDecimal(refAuditSeq));
        DataSet filteredCurrentDynamicData = this.__CurrentAuditDynamicData.getFilteredDataSet(filterMap);
        return filteredCurrentDynamicData;
    }

    private void prepareDynamicAuditData() throws SapphireException {
        block38: {
            block39: {
                int i;
                block37: {
                    int i2;
                    String parentElementId = this.elementInfo.getElementProperties().getProperty("parentid", "");
                    if (parentElementId.length() != 0) break block37;
                    LinkedList keyColumns = this.elementInfo.getKeyColumns();
                    int noOfKeyCols = keyColumns.size();
                    for (i2 = 0; i2 < noOfKeyCols; ++i2) {
                        String keyColId = (String)keyColumns.get(i2);
                        if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                            this.__AuditDynamicData.addColumn(keyColId, 1);
                            continue;
                        }
                        this.__AuditDynamicData.addColumn(keyColId, 0);
                    }
                    for (i2 = 0; i2 < this.__AuditDynamicData.getRowCount(); ++i2) {
                        for (int j = 0; j < noOfKeyCols; ++j) {
                            String keyColId = (String)keyColumns.get(j);
                            if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                                this.__AuditDynamicData.setNumber(i2, keyColId, this.__AuditDynamicData.getInt(i2, "keyid" + (j + 1)));
                                continue;
                            }
                            this.__AuditDynamicData.setString(i2, keyColId, this.__AuditDynamicData.getString(i2, "keyid" + (j + 1)));
                        }
                    }
                    break block38;
                }
                if (!this.elementInfo.isInAdvancedMode()) break block39;
                PropertyList elementProps = this.elementInfo.getElementProperties();
                PropertyList advancedConfigProps = elementProps.getPropertyListNotNull("advancedconfig");
                PropertyListCollection keyCols = advancedConfigProps.getCollectionNotNull("keycolumns");
                ArrayList<String> detailKeyColumns = new ArrayList<String>();
                for (i = 0; i < keyCols.size(); ++i) {
                    PropertyList keyColProps = keyCols.getPropertyList(i);
                    String keyColId = keyColProps.getProperty("columnid");
                    if (this.__AuditDynamicData.isValidColumn(keyColId)) continue;
                    detailKeyColumns.add(keyColId);
                    if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                        this.__AuditDynamicData.addColumn(keyColId, 1);
                        continue;
                    }
                    this.__AuditDynamicData.addColumn(keyColId, 0);
                }
                if (detailKeyColumns.size() <= 0) break block38;
                this.logger.debug("Extract following column values from activitylog.detailkeyvalues column (in respective sequence): ");
                detailKeyColumns.forEach(el -> this.logger.debug(el));
                for (i = 0; i < this.__AuditDynamicData.getRowCount(); ++i) {
                    String detailKeyValueList = this.__AuditDynamicData.getString(i, "detailkeyvalues", "");
                    if (detailKeyValueList.length() == 0) {
                        throw new SapphireException("Detail Key Values not populated in ActivityLog. (Advanced mode)");
                    }
                    String[] detailKeyValueArr = StringUtil.split(detailKeyValueList, ";");
                    int k = 0;
                    for (int j = 0; j < detailKeyColumns.size(); ++j) {
                        String keyColId = (String)detailKeyColumns.get(j);
                        if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                            this.__AuditDynamicData.setNumber(i, keyColId, detailKeyValueArr[k++]);
                            continue;
                        }
                        this.__AuditDynamicData.setString(i, keyColId, detailKeyValueArr[k++]);
                    }
                }
                break block38;
            }
            if (this.elementInfo.isSDIDetailTable()) {
                int i;
                LinkedList keyColumns = this.elementInfo.getKeyColumns();
                int noOfKeyCols = keyColumns.size();
                for (i = 4; i < noOfKeyCols; ++i) {
                    String keyColId = (String)keyColumns.get(i);
                    if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                        this.__AuditDynamicData.addColumn(keyColId, 1);
                        continue;
                    }
                    this.__AuditDynamicData.addColumn(keyColId, 0);
                }
                for (i = 0; i < this.__AuditDynamicData.getRowCount(); ++i) {
                    String detailKeyValueList = this.__AuditDynamicData.getString(i, "detailkeyvalues", "");
                    if (detailKeyValueList.length() == 0) {
                        throw new SapphireException("Detail Key Values not populated in ActivityLog. (SDIxxx Type)");
                    }
                    String[] detailKeyValueArr = StringUtil.split(detailKeyValueList, ";");
                    for (int j = 4; j < noOfKeyCols; ++j) {
                        String keyColId = (String)keyColumns.get(j);
                        if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                            this.__AuditDynamicData.setNumber(i, keyColId, detailKeyValueArr[j - 4]);
                            continue;
                        }
                        this.__AuditDynamicData.setString(i, keyColId, detailKeyValueArr[j - 4]);
                    }
                }
            } else if ("D".equals(this.elementInfo.getLinkType()) || "M".equals(this.elementInfo.getLinkType())) {
                int i;
                int i3;
                String topElementId = this.elementsContainer.getTopElementId();
                ElementInfo topElementInfo = this.elementsContainer.getElementInfo(topElementId);
                LinkedList topKeyColumns = topElementInfo.getKeyColumns();
                int noOfTopKeyColumns = topKeyColumns.size();
                for (i3 = 0; i3 < noOfTopKeyColumns; ++i3) {
                    String keyColId = (String)topKeyColumns.get(i3);
                    if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                        this.__AuditDynamicData.addColumn(keyColId, 1);
                        continue;
                    }
                    this.__AuditDynamicData.addColumn(keyColId, 0);
                }
                for (i3 = 0; i3 < this.__AuditDynamicData.getRowCount(); ++i3) {
                    for (int j = 0; j < noOfTopKeyColumns; ++j) {
                        String keyColId = (String)topKeyColumns.get(j);
                        if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                            this.__AuditDynamicData.setNumber(i3, keyColId, this.__AuditDynamicData.getInt(i3, "keyid" + (j + 1)));
                            continue;
                        }
                        this.__AuditDynamicData.setString(i3, keyColId, this.__AuditDynamicData.getString(i3, "keyid" + (j + 1)));
                    }
                }
                LinkedList currKeyColumns = this.elementInfo.getKeyColumns();
                int noOfCurrKeyCols = currKeyColumns.size();
                for (i = noOfTopKeyColumns; i < noOfCurrKeyCols; ++i) {
                    String keyColId = (String)currKeyColumns.get(i);
                    if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                        this.__AuditDynamicData.addColumn(keyColId, 1);
                        continue;
                    }
                    this.__AuditDynamicData.addColumn(keyColId, 0);
                }
                for (i = 0; i < this.__AuditDynamicData.getRowCount(); ++i) {
                    String detailKeyValueList = this.__AuditDynamicData.getString(i, "detailkeyvalues", "");
                    if (detailKeyValueList.length() == 0) {
                        throw new SapphireException("Detail Key Values not populated in ActivityLog. (D/M Type)");
                    }
                    String[] detailKeyValueArr = StringUtil.split(detailKeyValueList, ";");
                    for (int j = noOfTopKeyColumns; j < noOfCurrKeyCols; ++j) {
                        String keyColId = (String)currKeyColumns.get(j);
                        if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                            this.__AuditDynamicData.setNumber(i, keyColId, detailKeyValueArr[j - noOfTopKeyColumns]);
                            continue;
                        }
                        this.__AuditDynamicData.setString(i, keyColId, detailKeyValueArr[j - noOfTopKeyColumns]);
                    }
                }
            } else if ("RF".equals(this.elementInfo.getLinkType())) {
                int i;
                LinkedList keyColumns = this.elementInfo.getKeyColumns();
                int noOfKeyCols = keyColumns.size();
                for (i = 0; i < noOfKeyCols; ++i) {
                    String keyColId = (String)keyColumns.get(i);
                    if (this.elementInfo.isKeyColumnNumeric(keyColId)) {
                        this.__AuditDynamicData.addColumn(keyColId, 1);
                        continue;
                    }
                    this.__AuditDynamicData.addColumn(keyColId, 0);
                }
                for (i = 0; i < this.__AuditDynamicData.getRowCount(); ++i) {
                    for (int j = 0; j < noOfKeyCols; ++j) {
                        String keyColId = (String)keyColumns.get(j);
                        this.__AuditDynamicData.setValue(i, keyColId, this.__AuditDynamicData.getValue(i, "keyid" + (j + 1), ""));
                    }
                }
            }
        }
    }

    private PropertyList getColumnProps(String colId, PropertyListCollection unifiedColumns) {
        PropertyList columnProps = null;
        for (int i = 0; i < unifiedColumns.size(); ++i) {
            columnProps = unifiedColumns.getPropertyList(i);
            String columnId = columnProps.getProperty("columnid");
            if (!columnId.equalsIgnoreCase(colId)) continue;
            return columnProps;
        }
        return new PropertyList();
    }

    private String convertXMLToString(String columnValue) {
        return this.convertXMLToString(columnValue, false);
    }

    private String convertXMLToString(String columnValue, boolean skipSanitizeHTMLValue) {
        if (!skipSanitizeHTMLValue) {
            columnValue = this.isXMLString(columnValue) ? "<textarea style='border:1px solid grey; background-color:gainsboro; width:400px; height:150px; padding:5px'>" + ListColumn.sanitizeHTMLValue(columnValue) + "</textarea>" : ListColumn.sanitizeHTMLValue(columnValue);
        }
        return columnValue;
    }

    private boolean isXMLString(String value) {
        String valueInitials = value.length() > 20 ? value.substring(0, 20) : value;
        String valueTrim = value.trim();
        return valueInitials.toUpperCase().startsWith("<?XML") || valueInitials.toUpperCase().startsWith("<PROPERTYLIST ") || valueInitials.toUpperCase().startsWith("<PROPERTYTREE ") || valueInitials.toUpperCase().startsWith("<DATASET ") || valueInitials.toUpperCase().startsWith("<SDIDATA ") || valueInitials.toUpperCase().startsWith("<SNAPSHOT ") || valueInitials.toUpperCase().startsWith("<SNAPSHOTPACKAGE ") || valueTrim.startsWith("<") && valueTrim.endsWith(">");
    }

    private void createFilteredAuditData() {
        this.logger.debug("Filter Keys for Parent element filtering: ");
        if (this.__TopFilter == null) {
            this.logger.debug("Parent Element filter is not set. No Parent element filtering required.");
            this.__FilteredAuditData = this.__AuditData;
            this.__FilteredAuditDynamicData = this.__AuditDynamicData;
        } else {
            this.__TopFilter.forEach((k, v) -> this.logger.debug("\t" + k + " = " + v));
            this.__FilteredAuditData = this.__AuditData.getFilteredDataSet(this.__TopFilter);
            this.__FilteredAuditDynamicData = this.__AuditDynamicData.getFilteredDataSet(this.__TopFilter);
        }
    }

    private ArrayList getUniqueKeysFromFilteredAuditData() throws Exception {
        ArrayList<String> filteredAuditDataKeys = new ArrayList<String>();
        LinkedList keylist = null;
        String key = null;
        keylist = this.elementInfo.getKeyColumns();
        if (this.__FilteredAuditData != null) {
            for (int count = 0; count < this.__FilteredAuditData.getRowCount(); ++count) {
                key = AuditDetails.getKeyColVal(this.__FilteredAuditData, count, keylist);
                if (filteredAuditDataKeys.contains(key)) continue;
                filteredAuditDataKeys.add(key);
            }
        }
        return filteredAuditDataKeys;
    }

    public static String getKeyColVal(DataSet sourceDS, int row, List keyList) {
        StringBuffer key = new StringBuffer();
        for (int keycount = 0; keycount < keyList.size(); ++keycount) {
            key.append(sourceDS.getValue(row, (String)keyList.get(keycount)));
            if (keycount == keyList.size() - 1) continue;
            key.append(";");
        }
        return key.toString();
    }

    private String getDataRowsDivId() {
        String dataRowsDivId = null;
        dataRowsDivId = this.element.getId() + "|" + this.__TopKey;
        return dataRowsDivId;
    }

    private String getDataRowsDivImgId(String dataRowsDivId) {
        String dataRowsDivImgId = null;
        dataRowsDivImgId = "img_" + dataRowsDivId;
        return dataRowsDivImgId;
    }

    private String getIndentation() {
        StringBuffer html = new StringBuffer();
        html.append("<td width='6'>&nbsp;</td>");
        html.append("<td width='6'>&nbsp;</td>");
        return html.toString();
    }

    private HashMap getFilterKey(String key) throws Exception {
        HashMap<String, Object> filterKey = new HashMap<String, Object>();
        LinkedList keylist = this.elementInfo.getKeyColumns();
        String[] keyValues = StringUtil.split(key, ";");
        for (int i = 0; i < keylist.size(); ++i) {
            String keycolumn = (String)keylist.get(i);
            if (this.elementInfo.isKeyColumnNumeric(keycolumn)) {
                filterKey.put(keycolumn, new BigDecimal(keyValues[i]));
                continue;
            }
            filterKey.put(keycolumn, keyValues[i]);
        }
        return filterKey;
    }

    private HashMap getSubstitutionParams(String key) throws Exception {
        HashMap<String, String> sustitutionParams = new HashMap<String, String>();
        LinkedList keylist = null;
        String[] keyValues = null;
        String keycolumn = null;
        if (key != null) {
            keylist = this.elementInfo.getKeyColumns();
            keyValues = StringUtil.split(key, ";");
            for (int count = 0; count < keylist.size(); ++count) {
                keycolumn = (String)keylist.get(count);
                sustitutionParams.put(keycolumn, keyValues[count]);
            }
        }
        return sustitutionParams;
    }

    private String getSubstitutedValue(String value, HashMap substitutionParams) {
        String substitutedValue = value;
        ArrayList tokens = null;
        try {
            if (value != null && value.indexOf("[") >= 0) {
                DataSet ds = new DataSet();
                ds = (DataSet)this.__CurrentAuditData.clone();
                ds.sort("auditsequence D");
                for (int i = 0; i < ds.getColumnCount(); ++i) {
                    substitutionParams.put(ds.getColumnId(i), ds.getValue(0, ds.getColumnId(i), "[" + ds.getColumnId(i) + "]"));
                }
                tokens = OpalUtil.getKeywordTokens(value);
                substitutedValue = OpalUtil.searchAndReplaceTokens(value, tokens, substitutionParams, false);
            }
        }
        catch (Exception ex) {
            this.logger.error("Exception caught value: " + value + " HashMap: " + substitutionParams);
        }
        return substitutedValue;
    }

    public HashMap createTopKeyFilter(String key, ElementInfo childElementInfo) {
        HashMap<Object, Object> filter;
        block14: {
            LinkedList currentKeyList;
            String[] keyArray;
            block18: {
                block19: {
                    String keycolumn;
                    block17: {
                        block12: {
                            String childLinkType;
                            block16: {
                                block15: {
                                    block13: {
                                        filter = new HashMap<Object, Object>();
                                        keyArray = null;
                                        keycolumn = null;
                                        currentKeyList = null;
                                        keyArray = StringUtil.split(key, ";");
                                        currentKeyList = this.elementInfo.getKeyColumns();
                                        if (!this.elementInfo.isSDC()) break block12;
                                        if (!childElementInfo.isInAdvancedMode()) break block13;
                                        for (int count = 0; count < currentKeyList.size(); ++count) {
                                            filter.put(currentKeyList.get(count), keyArray[count]);
                                        }
                                        break block14;
                                    }
                                    if (!childElementInfo.isSDIDetailTable()) break block15;
                                    for (int count = 0; count < currentKeyList.size(); ++count) {
                                        filter.put("keyid" + (count + 1), keyArray[count]);
                                    }
                                    break block14;
                                }
                                childLinkType = childElementInfo.getLinkType();
                                if (!"D".equals(childLinkType) && !"M".equals(childLinkType)) break block16;
                                for (int count = 0; count < currentKeyList.size(); ++count) {
                                    filter.put(currentKeyList.get(count), keyArray[count]);
                                }
                                break block14;
                            }
                            if (!"RF".equals(childLinkType)) break block14;
                            LinkedHashMap _revFKeyLink = childElementInfo.getRevFKeyColumns();
                            int count = 0;
                            for (String colId : _revFKeyLink.values()) {
                                if (!(colId == null || colId.equals("(null)") && "".equals(colId))) {
                                    filter.put(colId, keyArray[count]);
                                }
                                ++count;
                            }
                            break block14;
                        }
                        if (!this.elementInfo.isInAdvancedMode()) break block17;
                        if (!childElementInfo.isInAdvancedMode()) break block14;
                        for (int count = 0; count < currentKeyList.size(); ++count) {
                            String columnId = (String)currentKeyList.get(count);
                            String columnType = this.elementInfo.getKeyColumnType(columnId);
                            if ("N".equals(columnType)) {
                                filter.put(columnId, new BigDecimal(keyArray[count]));
                                continue;
                            }
                            filter.put(columnId, keyArray[count]);
                        }
                        break block14;
                    }
                    if (!this.elementInfo.isSDIDetailTable()) break block18;
                    if (!childElementInfo.isSDIDetailTable()) break block19;
                    for (int count = 0; count < currentKeyList.size(); ++count) {
                        keycolumn = (String)currentKeyList.get(count);
                        if (keycolumn.equalsIgnoreCase(CLOB_TYPE_DATASET) || keycolumn.equalsIgnoreCase("REPLICATEID") || keycolumn.equalsIgnoreCase("WORKITEMINSTANCE")) {
                            filter.put(keycolumn, new BigDecimal(keyArray[count]));
                            continue;
                        }
                        filter.put(keycolumn, keyArray[count]);
                    }
                    break block14;
                }
                if (!childElementInfo.isSDC()) break block14;
                this.logger.info("OPAL-ERROR: Child element is SDC. Not handled. ");
                break block14;
            }
            if (this.elementInfo.isDetailTable() && (childElementInfo.isDetailTable() || childElementInfo.isInAdvancedMode())) {
                for (int count = 0; count < currentKeyList.size(); ++count) {
                    String columnId = (String)currentKeyList.get(count);
                    String columnType = this.elementInfo.getKeyColumnType(columnId);
                    if ("N".equals(columnType)) {
                        filter.put(columnId, new BigDecimal(keyArray[count]));
                        continue;
                    }
                    filter.put(columnId, keyArray[count]);
                }
            }
        }
        return filter;
    }

    private String getTraceLogColTitle(PropertyListCollection colCollection, String displayColumnId, String defaultText) {
        String title = defaultText;
        PropertyList columns = null;
        for (int i = 0; i < colCollection.size(); ++i) {
            columns = colCollection.getPropertyList(i);
            String columnId = columns.getProperty("columnid");
            if (!columnId.equalsIgnoreCase(displayColumnId)) continue;
            title = columns.getProperty("title", columnId);
            break;
        }
        return title;
    }

    private boolean isDeletedRow(DataSet auditData, int rowCount) {
        boolean isRowDeleted = false;
        String modTool = auditData.getString(rowCount, "modtool", "");
        String auditOPFlag = auditData.getString(rowCount, "auditopflag", "");
        String tracelogid = auditData.getString(rowCount, "tracelogid", "");
        if ("D".equalsIgnoreCase(auditOPFlag) || modTool.indexOf("Delete") > -1 || "DELETED".equals(tracelogid)) {
            isRowDeleted = true;
        }
        return isRowDeleted;
    }

    private String getActivityDisplayValue(DataSet auditData, int rowCount, String activityValue, TranslationProcessor tp) {
        String activityDispVal = "";
        if (activityValue.length() > 0) {
            return activityValue;
        }
        String modTool = auditData.getString(rowCount, "modtool", "");
        String auditOPFlag = auditData.getString(rowCount, "auditopflag", "");
        String tracelogid = auditData.getString(rowCount, "tracelogid", "");
        if ("D".equalsIgnoreCase(auditOPFlag) || modTool.indexOf("Delete") > -1 || "DELETED".equals(tracelogid)) {
            activityDispVal = "Deleted";
        } else if ("U".equalsIgnoreCase(auditOPFlag)) {
            activityDispVal = "Updated";
        } else if ("I".equalsIgnoreCase(auditOPFlag)) {
            activityDispVal = "Inserted";
        }
        return activityDispVal;
    }

    private String getValueToPrint(String value, String displayValue) {
        String finalValue = "";
        finalValue = value != null && value.trim().length() > 0 && displayValue != null && displayValue.trim().length() > 0 ? OpalUtil.parseDisplayValue(value, displayValue) : value;
        finalValue = this.replaceLeadNTrailSpaces(finalValue);
        return finalValue;
    }

    private String replaceLeadNTrailSpaces(String value) {
        if (value.length() == 0) {
            return value;
        }
        if (value.length() > 0 && value.trim().length() == 0) {
            return StringUtil.replaceAll(value, " ", "&nbsp;");
        }
        StringBuffer finalValue = new StringBuffer(value);
        if (finalValue.charAt(0) == ' ') {
            int noOfLeadingSpaces = this.getFirstNonSpaceIndex(finalValue);
            for (int i = noOfLeadingSpaces - 1; i >= 0; --i) {
                finalValue.replace(i, i + 1, "&nbsp;");
            }
        }
        if (finalValue.charAt(finalValue.length() - 1) == ' ') {
            int lastNonSpaceIndex = this.getLastNonSpaceIndex(finalValue);
            int currentLength = finalValue.length();
            for (int i = currentLength - 1; i > lastNonSpaceIndex; --i) {
                finalValue.replace(i, i + 1, "&nbsp;");
            }
        }
        return finalValue.toString();
    }

    private int getFirstNonSpaceIndex(StringBuffer str) {
        int firstNonSpaceCharIndex = -1;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == ' ') continue;
            firstNonSpaceCharIndex = i;
            break;
        }
        return firstNonSpaceCharIndex;
    }

    private int getLastNonSpaceIndex(StringBuffer str) {
        int lastNonSpaceCharIndex = -1;
        for (int i = str.length() - 1; i >= 0; --i) {
            if (str.charAt(i) == ' ') continue;
            lastNonSpaceCharIndex = i;
            break;
        }
        return lastNonSpaceCharIndex;
    }

    public ElementInfo getElementInfo() {
        return this.elementInfo;
    }

    public void setElementInfo(ElementInfo elementInfo) {
        this.elementInfo = elementInfo;
    }

    public DataSet getTempMLinkAuditData() {
        return this.__tempMLinkAuditData;
    }

    public void setTempMLinkAuditData(DataSet __tempMLinkAuditData) {
        this.__tempMLinkAuditData = __tempMLinkAuditData;
    }

    public DataSet getAuditData() {
        return this.__AuditData;
    }

    public void setAuditData(DataSet auditData) {
        this.__AuditData = auditData;
    }

    public HashMap getTopFilter() {
        return this.__TopFilter;
    }

    public void setTopFilter(HashMap topFilter) {
        this.__TopFilter = topFilter;
    }

    public String getTopKey() {
        return this.__TopKey;
    }

    public void setTopKey(String topKey) {
        this.__TopKey = topKey;
    }

    private void setTranslationProcessor(TranslationProcessor tp) {
        this.__Tp = tp;
    }

    public AuditElementsContainer getElementsContainer() {
        return this.elementsContainer;
    }

    public void setElementsContainer(AuditElementsContainer elementsContainer) {
        this.elementsContainer = elementsContainer;
    }

    public DataSet getAuditDynamicData() {
        return this.__AuditDynamicData;
    }

    public void setAuditDynamicData(DataSet auditDynamicData) {
        this.__AuditDynamicData = auditDynamicData;
    }

    private boolean skipSanitizeHtmlValue(String columnId, int rowNum) {
        return this.elementInfo.getElementTableId().equalsIgnoreCase("worksheetitem") && columnId.equalsIgnoreCase("contents") && this.__CurrentAuditData.getString(rowNum, "propertytreeid", "").equalsIgnoreCase("RichTextControl") || this.elementInfo.getElementTableId().equalsIgnoreCase("sdiattribute") && columnId.equalsIgnoreCase("instructiontext") && (this.__CurrentAuditData.getString(rowNum, "sdcid", "").equalsIgnoreCase("LV_WorksheetItem") || this.__CurrentAuditData.getString(rowNum, "sdcid", "").equalsIgnoreCase("LV_Worksheet"));
    }

    static {
        AuditDetails.setupCLOBColumnsInfo();
    }
}

