/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBuilder
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.aspose.words.DocumentBuilder;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.eln.WordWorksheet;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFieldsField;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemTokenResolver;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class WorksheetItem
implements ELNConstants {
    protected Logger logger = new Logger(new LogContext());
    private SapphireConnection sapphireConnection;
    private DBUtil database;
    private ActionProcessor actionProcessor;
    private ConnectionProcessor connectionProcessor;
    private DAMProcessor damProcessor;
    private QueryProcessor queryProcessor;
    private SDIProcessor sdiProcessor;
    private SDCProcessor sdcProcessor;
    private SequenceProcessor sequenceProcessor;
    private TranslationProcessor translationProcessor;
    protected PropertyList config = new PropertyList();
    protected WorksheetItemIncludes worksheetItemIncludes;
    protected WorksheetItemOptions worksheetItemOptions;
    private HashMap rowdata;
    private int width;
    private String clientRenderer = "Generic";
    protected LogContext logContext;
    private String logName = this.getClass().getSimpleName().toUpperCase();
    private DataSet worksheetitemparams = null;
    private HashSet<String> configParamExcludes = new HashSet();
    private WorksheetItemFields worksheetItemFields;
    private boolean template = false;
    private String availability = "OK";
    private String editRSet;
    private String elementid;
    private DataSet worksheetFields;
    private WorksheetItemTokenResolver worksheetItemTokenResolver = null;
    DataSet dependencyList = new DataSet();

    static WorksheetItem getInstance(SapphireConnection sapphireConnection, String classname) throws SapphireException {
        WorksheetItem worksheetitemClass = null;
        try {
            Class<?> c = Class.forName(classname);
            worksheetitemClass = (WorksheetItem)c.newInstance();
            worksheetitemClass.sapphireConnection = sapphireConnection;
            worksheetitemClass.worksheetItemIncludes = new WorksheetItemIncludes();
            worksheetitemClass.setupIncludes(worksheetitemClass.worksheetItemIncludes);
            worksheetitemClass.logger = new Logger(new LogContext(sapphireConnection.getConnectionId(), worksheetitemClass.getClass().getSimpleName()));
            return worksheetitemClass;
        }
        catch (Exception e) {
            Trace.logError("WorksheetItem", (Object)("Failed to create WorksheetItem class " + classname), e);
            throw new SapphireException("Failed to create WorksheetItem class " + classname);
        }
    }

    static WorksheetItem getInstance(SapphireConnection sapphireConnection, DBUtil database, HashMap rowdata, String width) throws SapphireException {
        WorksheetItem worksheetitemClass = null;
        try {
            String classname;
            if (sapphireConnection != null) {
                WebAdminProcessor webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
                classname = webAdminProcessor.getPropertyTreeObject((String)rowdata.get("propertytreeid"));
            } else {
                database.createPreparedResultSet("SELECT objectname FROM propertytree WHERE propertytreeid = ?", new Object[]{(String)rowdata.get("propertytreeid")});
                database.getNext();
                classname = database.getValue("objectname");
            }
            Class<?> c = Class.forName(classname);
            worksheetitemClass = (WorksheetItem)c.newInstance();
            worksheetitemClass.logContext = sapphireConnection != null ? new LogContext(sapphireConnection.getConnectionId()) : new LogContext();
            worksheetitemClass.logName = worksheetitemClass.getClass().getSimpleName().toUpperCase();
            worksheetitemClass.sapphireConnection = sapphireConnection;
            worksheetitemClass.database = database;
            worksheetitemClass.rowdata = rowdata;
            worksheetitemClass.configParamExcludes.add("taskdef");
            int wInt = 0;
            try {
                wInt = Integer.parseInt(width);
            }
            catch (Exception exception) {
                // empty catch block
            }
            worksheetitemClass.width = wInt;
            worksheetitemClass.config.setPropertyList((String)rowdata.get("config"));
            worksheetitemClass.worksheetItemIncludes = new WorksheetItemIncludes();
            worksheetitemClass.setupIncludes(worksheetitemClass.worksheetItemIncludes);
            worksheetitemClass.worksheetItemOptions = new WorksheetItemOptions((String)rowdata.get("options"));
            worksheetitemClass.worksheetItemOptions.setName(worksheetitemClass.config.getProperty("name"));
            worksheetitemClass.worksheetItemOptions.setIncludes(worksheetitemClass.config.getCollection("includes"));
            worksheetitemClass.worksheetItemOptions.setEditorObjectName(worksheetitemClass.worksheetItemIncludes.getJSObjectName());
            worksheetitemClass.setupOptions(worksheetitemClass.worksheetItemOptions);
            worksheetitemClass.logger = new Logger(new LogContext(sapphireConnection != null ? sapphireConnection.getConnectionId() : "", worksheetitemClass.getName(true)));
            return worksheetitemClass;
        }
        catch (Exception e) {
            Trace.logError("WorksheetItem", (Object)("Failed to create WorksheetItem class for " + rowdata.get("propertytreeid")), e);
            throw new SapphireException("Failed to create WorksheetItem class for " + rowdata.get("propertytreeid"));
        }
    }

    public SapphireConnection getSapphireConnection() throws SapphireException {
        if (this.sapphireConnection != null) {
            return this.sapphireConnection;
        }
        throw new SapphireException("SapphireConnection not available in this mode");
    }

    protected String getDetokenizedConfigProperty(String propertyId) {
        return this.getDetokenizedConfigProperty(propertyId, "");
    }

    protected String getDetokenizedConfigProperty(String propertyId, String defaultValue) {
        return this.getDetokenizedConfigProperty(this.config, propertyId, defaultValue);
    }

    protected String getDetokenizedConfigProperty(PropertyList propertyList, String propertyId) {
        return this.getDetokenizedConfigProperty(propertyList, propertyId, "");
    }

    protected String getDetokenizedConfigProperty(PropertyList propertyList, String propertyId, String defaultValue) {
        String val = propertyList.getProperty(propertyId, defaultValue);
        if (this.isTemplate() && !val.contains("$P{")) {
            return val;
        }
        return this.getWorksheetItemTokenResolver().resolveTokens(this.getWorksheetItemId(), this.getWorksheetItemVersionId(), val, this.dependencyList);
    }

    private WorksheetItemTokenResolver getWorksheetItemTokenResolver() {
        if (this.worksheetItemTokenResolver == null) {
            try {
                this.worksheetItemTokenResolver = new WorksheetItemTokenResolver(this.getWorksheetId(), this.getWorksheetVersionId(), this.getQueryProcessor(), this.getSapphireConnection());
            }
            catch (SapphireException e) {
                Trace.logWarn("Unable to instantiate WorksheetItemTokenResolver: " + e.getMessage());
                this.worksheetItemTokenResolver = new WorksheetItemTokenResolver(this.getWorksheetId(), this.getWorksheetVersionId());
                return this.worksheetItemTokenResolver;
            }
        }
        return this.worksheetItemTokenResolver;
    }

    public DataSet getDependencyList() {
        return this.dependencyList;
    }

    protected String getValue(String columnid) {
        String value = (String)this.rowdata.get(columnid);
        return value == null ? "" : value;
    }

    protected String getWorksheetId() {
        return this.rowdata.get("worksheetid") != null ? (String)this.rowdata.get("worksheetid") : "";
    }

    protected String getWorksheetVersionId() {
        return this.rowdata.get("worksheetversionid") != null ? (String)this.rowdata.get("worksheetversionid") : "";
    }

    protected String getWorksheetSectionId() {
        return this.rowdata.get("worksheetsectionid") != null ? (String)this.rowdata.get("worksheetsectionid") : "";
    }

    protected String getWorksheetSectionVersionId() {
        return this.rowdata.get("worksheetsectionversionid") != null ? (String)this.rowdata.get("worksheetsectionversionid") : "";
    }

    protected String getWorksheetItemId() {
        return this.rowdata.get("worksheetitemid") != null ? (String)this.rowdata.get("worksheetitemid") : "";
    }

    public void setWorksheetItemId(String worksheetitemid) {
        this.rowdata.put("worksheetitemid", worksheetitemid);
    }

    protected String getWorksheetItemVersionId() {
        return this.rowdata.get("worksheetitemversionid") != null ? (String)this.rowdata.get("worksheetitemversionid") : "";
    }

    public void setWorksheetItemVersionId(String worksheetitemversionid) {
        this.rowdata.put("worksheetitemversionid", worksheetitemversionid);
    }

    public String getElementId() {
        if (this.elementid == null) {
            String id = (String)this.rowdata.get("worksheetitemid");
            this.elementid = "WSI" + (id == null ? "___" : Integer.valueOf(Math.abs(id.hashCode())));
        }
        return this.elementid;
    }

    protected void setEditRSet(String rsetid) {
        this.editRSet = rsetid;
    }

    public String getEditRSet() {
        return this.editRSet != null ? this.editRSet : "";
    }

    protected int getWidth() {
        return this.width - 10;
    }

    public boolean hasContents() {
        return this.rowdata.get("contents") != null && ((String)this.rowdata.get("contents")).length() > 0;
    }

    public String getContentsForEdit() {
        return this.getContents();
    }

    public String getContents() {
        return this.rowdata.get("contents") != null ? (String)this.rowdata.get("contents") : "";
    }

    public String getDefaultContents() {
        return "";
    }

    public PropertyList getConfig() {
        return this.config;
    }

    public void setConfig(PropertyList config) {
        this.config = config;
    }

    public String getName(boolean initCap) {
        return initCap ? StringUtil.initCaps(this.worksheetItemOptions.getOption("name", (String)this.rowdata.get("propertytreeid"))) : this.worksheetItemOptions.getOption("name", (String)this.rowdata.get("propertytreeid"));
    }

    public WorksheetItemIncludes getWorksheetItemIncludes() {
        return this.worksheetItemIncludes;
    }

    public WorksheetItemOptions getWorksheetItemOptions() {
        return this.worksheetItemOptions;
    }

    public void setClientRenderer(String clientRenderer) {
        this.clientRenderer = clientRenderer;
    }

    public String getClientRenderer() {
        return this.clientRenderer;
    }

    public boolean isTemplate() {
        return this.template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public String getAvailability() {
        return this.availability;
    }

    public void setAvailability() throws SapphireException {
        this.getViewHTML();
    }

    protected void setAvailability(String availability) {
        this.availability = availability;
    }

    public abstract void setupOptions(WorksheetItemOptions var1);

    public abstract void setupIncludes(WorksheetItemIncludes var1);

    public void validateAdd(PropertyList properties) throws SapphireException {
    }

    public void postAdd() throws SapphireException {
    }

    public void validateEdit(PropertyList properties) throws SapphireException {
    }

    public void postEdit() throws SapphireException {
    }

    public void validateConfig(PropertyList config) throws SapphireException {
    }

    public void postConfig() throws SapphireException {
    }

    public void validateReference(int referenceid) throws SapphireException {
    }

    public String validateContents(String contents) throws SapphireException {
        return contents;
    }

    public void postContents() throws SapphireException {
    }

    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        return "";
    }

    public String validateStateChange(String fromStatus, String toStatus) throws SapphireException {
        return "";
    }

    public boolean readyToSaveHtml() throws SapphireException {
        return true;
    }

    public String checkCompleteness(String fromStatus) throws SapphireException {
        String message = BaseELNAction.validateMetaData(this.getQueryProcessor(), "LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId());
        if (message.length() == 0) {
            message = this.validateStateChange(fromStatus, "Complete");
        }
        return message;
    }

    public abstract String getViewHTML() throws SapphireException;

    public String getCopyHTML() throws SapphireException {
        return this.getViewHTML();
    }

    public abstract String getEditorHTML() throws SapphireException;

    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getViewHTML();
    }

    public String getDockViewHTML(String prefix) throws SapphireException {
        return this.getViewHTML();
    }

    public String getDiffHTML(String contentCurrent, String contentPrior, String prefix) throws SapphireException {
        return null;
    }

    public LinkedHashMap<String, String> getMetaData() {
        return null;
    }

    public String getStatus() {
        return this.getValue("itemstatus");
    }

    public String getIndexingText() {
        String status = this.getValue("itemstatus");
        String completeHTML = this.getValue("html");
        if (status.equals("Complete") && completeHTML.length() > 0) {
            Document jdoc = Jsoup.parse((String)completeHTML);
            return jdoc.body().text();
        }
        return this.getLiveIndexingText();
    }

    public String getLiveIndexingText() {
        return "";
    }

    protected SDIRequest getSDIRequest(String source, String sourcesdcid, PropertyListCollection columns, PropertyListCollection secondaryColumns) throws SapphireException {
        return this.getSDIRequest(source, sourcesdcid, columns, secondaryColumns, false);
    }

    protected SDIRequest getSDIRequest(String source, String sourcesdcid, PropertyListCollection columns, PropertyListCollection secondaryColumns, boolean setWorksheettableOrderBy) throws SapphireException {
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getProperties(sourcesdcid);
        int keyCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        if (columns == null) {
            columns = new PropertyListCollection();
        }
        if (columns.size() == 0) {
            try {
                columns.setJSONString("[{columnid: '" + sdcProps.getProperty("keycolid1") + "', title: 'Id'}," + (keyCount > 1 ? "{columnid: '" + sdcProps.getProperty("keycolid2") + "', title: 'Id2'}," : "") + (keyCount > 2 ? "{columnid: '" + sdcProps.getProperty("keycolid3") + "', title: 'Id3'}," : "") + "{columnid: '" + sdcProps.getProperty("desccol") + "', title: 'Description'}]");
            }
            catch (JSONException e) {
                throw new SapphireException("Failed to create default column list", e);
            }
        }
        StringBuffer requesetItems = new StringBuffer();
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            column.setProperty("columnid", this.getWorksheetItemTokenResolver().resolveTokens(this.getWorksheetId(), this.getWorksheetVersionId(), columnid, this.dependencyList));
            if (columnid.startsWith("keyid1.")) {
                column.setProperty("columnid", columnid.substring(7));
                if (secondaryColumns == null) continue;
                secondaryColumns.add(column);
                continue;
            }
            if (columnid.length() <= 0) continue;
            requesetItems.append(",").append(column.getProperty("columnid"));
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sourcesdcid);
        sdiRequest.setRequestItem("primary[" + requesetItems.substring(1) + "]");
        sdiRequest.setRetainRsetid(secondaryColumns != null && secondaryColumns.size() > 0);
        if (source.equalsIgnoreCase("control") || source.equalsIgnoreCase("worksheet")) {
            String tableid = sdcProps.getProperty("tableid");
            int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
            String worksheettable = source.equalsIgnoreCase("control") ? "worksheetitemsdi" : "worksheetsdi";
            String worksheettableid = source.equalsIgnoreCase("control") ? "worksheetitemid" : "worksheetid";
            String worksheettablever = source.equalsIgnoreCase("control") ? "worksheetitemversionid" : "worksheetversionid";
            String param1 = source.equalsIgnoreCase("control") ? this.getWorksheetItemId() : this.getWorksheetId();
            String param2 = source.equalsIgnoreCase("control") ? this.getWorksheetItemVersionId() : this.getWorksheetVersionId();
            String param3 = sourcesdcid;
            sdiRequest.setQueryFrom(tableid + "," + worksheettable);
            sdiRequest.setQueryWhere(worksheettable + "." + worksheettableid + " = '" + SafeSQL.encodeForSQL(param1, this.sapphireConnection.isOracle()) + "' AND " + worksheettable + "." + worksheettablever + " = '" + SafeSQL.encodeForSQL(param2, this.sapphireConnection.isOracle()) + "'   AND " + worksheettable + ".sdcid = '" + SafeSQL.encodeForSQL(param3, this.sapphireConnection.isOracle()) + "' AND " + worksheettable + ".keyid1 = " + tableid + "." + sdcProps.getProperty("keycolid1") + " " + (keycols >= 2 ? "  AND " + worksheettable + ".keyid2 = " + tableid + "." + sdcProps.getProperty("keycolid2") : "") + " " + (keycols >= 3 ? "  AND " + worksheettable + ".keyid3 = " + tableid + "." + sdcProps.getProperty("keycolid3") : ""));
            if (setWorksheettableOrderBy) {
                sdiRequest.setQueryOrderBy(worksheettable + ".usersequence, " + worksheettable + ".keyid1, " + worksheettable + ".keyid2, " + worksheettable + ".keyid3");
            } else {
                sdiRequest.setQueryOrderBy("1");
            }
        } else {
            sdiRequest.setQueryid(this.getDetokenizedConfigProperty("queryid"));
            PropertyListCollection queryparams = this.config.getCollection("queryparams");
            if (queryparams != null) {
                String[] params = new String[queryparams.size()];
                for (int i = 0; i < queryparams.size(); ++i) {
                    params[i] = this.getDetokenizedConfigProperty(queryparams.getPropertyList(i), "paramid");
                }
                sdiRequest.setQueryParams(params);
            }
            sdiRequest.setQueryFrom(this.getDetokenizedConfigProperty("queryfrom"));
            sdiRequest.setQueryWhere(this.getDetokenizedConfigProperty("querywhere"));
        }
        return sdiRequest;
    }

    protected String getTableHTML(boolean export, PropertyListCollection columns, HashSet<String> skipColumns, PropertyListCollection operations, DataSet controlData, String plural, String keycolid1, String keycolid2, String keycolid3) throws SapphireException {
        int col;
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        String tableStyling = this.getTableStylingProperty("tablestyling", export, "lv_elntable_opentable  ");
        html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
        html.append("<tr>");
        HashMap<String, PropertyList> columnPropertyList = new HashMap<String, PropertyList>();
        for (col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colId = columns.getPropertyList(col).getProperty("columnid");
            if (colId.indexOf(" ") > 0) {
                colId = RequestParser.parseAlias(colId);
            }
            columnPropertyList.put(colId, column);
            if (skipColumns.contains(colId)) continue;
            String tip = tp.translate(column.getProperty("tip"));
            html.append("<td title=\"" + tip + "\" class=\"title\">").append(tp.translate(column.getProperty("title"))).append("</td>");
        }
        html.append("</tr>");
        if (controlData.size() > 0) {
            PropertyListCollection groupby = this.config.getCollectionNotNull("groupby");
            StringBuilder groupCols = new StringBuilder();
            HashMap<String, PropertyList> colProperties = new HashMap<String, PropertyList>();
            for (int g = 0; g < groupby.size(); ++g) {
                PropertyList pl = groupby.getPropertyList(g);
                String colId = pl.getProperty("columnid");
                if (colId.length() > 0 && !columnPropertyList.containsKey(colId)) {
                    throw new SapphireException(tp.translate("All Group-by columns must be defined in the master column collection."));
                }
                if (colId.length() > 0) {
                    groupCols.append(",").append(colId);
                }
                colProperties.put(colId, pl);
            }
            ArrayList<Object> sdiGroups = new ArrayList();
            if (groupCols.length() > 0) {
                controlData.sort(groupCols.substring(1));
                sdiGroups = controlData.getGroupedDataSets(groupCols.substring(1));
            }
            if (sdiGroups.size() > 0) {
                String[] groupColumns = StringUtil.split(groupCols.substring(1), ",");
                int offset = 0;
                for (int g = 0; g < sdiGroups.size(); ++g) {
                    DataSet groupData = (DataSet)sdiGroups.get(g);
                    StringBuilder grpHeader = new StringBuilder();
                    for (int col2 = 0; col2 < groupColumns.length; ++col2) {
                        String linkToOperation;
                        String column = groupColumns[col2];
                        PropertyList plColumnProps = (PropertyList)columnPropertyList.get(column);
                        String value = this.getColumnDisplayValue(groupData, 0, plColumnProps, tp);
                        if (!export && (linkToOperation = plColumnProps.getProperty("linktooperation")).length() > 0) {
                            value = this.linkToOperation(value, groupData, g, linkToOperation, operations, groupData.getValue(g, keycolid1), groupData.getValue(g, keycolid2), groupData.getValue(g, keycolid3));
                        }
                        PropertyList colPropertyList = (PropertyList)colProperties.get(column);
                        grpHeader.append(colPropertyList.getProperty("title", "")).append(" ").append(value != null && value.length() > 0 ? value : "&nbsp;").append(" ");
                    }
                    html.append("<tr><td colspan=\"").append(columns.size()).append("\" class=\"worksheetgroup_header \">" + grpHeader + "<span style=\"font-style:italic;font-weight:bold\">(" + groupData.getRowCount() + ")&lrm;</span></td></tr>");
                    this.getRowHtml(export, groupData, html, operations, columns, skipColumns, tp, keycolid1, keycolid2, keycolid3, offset);
                    offset += groupData.size();
                }
            } else {
                this.getRowHtml(export, controlData, html, operations, columns, skipColumns, tp, keycolid1, keycolid2, keycolid3);
            }
        } else {
            this.setAvailability("NoData");
            if (this.isTemplate()) {
                html.append("<tr>");
                for (col = 0; col < columns.size(); ++col) {
                    html.append("<td class=\"value\">").append(columns.getPropertyList(col).getProperty("columnid")).append("</td>");
                }
                html.append("</tr>");
            } else {
                String noSDIMsg = this.getDetokenizedConfigProperty("nosdiavailablemessage");
                html.append("<tr><td colspan=\"").append(columns.size()).append("\" class=\"value\">" + (noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : tp.translate("No " + plural + " found")) + "</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    protected String linkToOperation(String value, DataSet controlData, int row, String operationid, PropertyListCollection operations, String keyid1, String keyid2, String keyid3) {
        PropertyList find;
        if (keyid1 == null || keyid1.length() == 0) {
            return value;
        }
        boolean showlink = true;
        if (operationid.length() > 0 && (find = operations.find("id", operationid)) != null) {
            PropertyList filter = find.getPropertyListNotNull("sdifilter");
            String columnid = filter.getProperty("columnid");
            boolean in = filter.getProperty("operator", "in").equalsIgnoreCase("in");
            String valuelist = filter.getProperty("valuelist");
            if (columnid.length() > 0 && controlData.getColumnType(columnid) == 0 && valuelist.length() > 0) {
                String thisvalue = controlData.getValue(row, columnid);
                if (in) {
                    if (!(";" + valuelist + ";").contains(thisvalue)) {
                        showlink = false;
                    }
                } else if ((";" + valuelist + ";").contains(thisvalue)) {
                    showlink = false;
                }
            }
        }
        return showlink ? "<a href=\"javascript:sapphire.worksheet.linkToOperation('" + this.getWorksheetItemId() + "','" + this.getWorksheetItemVersionId() + "','" + operationid + "','" + keyid1 + "'" + (keyid2 != null && keyid2.length() > 0 ? ",'" + keyid2 + "'" : "") + (keyid3 != null && keyid3.length() > 0 ? ",'" + keyid3 + "'" : "") + ")\">" + value + "</a>" : value;
    }

    private void getRowHtml(boolean export, DataSet controlData, StringBuffer html, PropertyListCollection operations, PropertyListCollection columns, HashSet<String> skipColumns, TranslationProcessor tp, String keycolid1, String keycolid2, String keycolid3) {
        this.getRowHtml(export, controlData, html, operations, columns, skipColumns, tp, keycolid1, keycolid2, keycolid3, 0);
    }

    private void getRowHtml(boolean export, DataSet controlData, StringBuffer html, PropertyListCollection operations, PropertyListCollection columns, HashSet<String> skipColumns, TranslationProcessor tp, String keycolid1, String keycolid2, String keycolid3, int offset) {
        for (int row = 0; row < controlData.size(); ++row) {
            html.append("<tr id=\"").append(this.getElementId()).append("_").append(row + offset).append("\" onmouseover=\"sdiViewerEditor.mouseOverRow('").append(this.getElementId()).append("_").append(row + offset).append("')\" onmouseout=\"sdiViewerEditor.mouseOutRow('").append(this.getElementId()).append("_").append(row + offset).append("')\">");
            for (int col = 0; col < columns.size(); ++col) {
                String linkToOperation;
                PropertyList column = columns.getPropertyList(col);
                String colId = column.getProperty("columnid");
                if (colId.indexOf(" ") > 0) {
                    colId = RequestParser.parseAlias(colId);
                }
                if (skipColumns.contains(colId)) continue;
                String value = this.getColumnDisplayValue(controlData, row, column, tp);
                if (!export && (linkToOperation = column.getProperty("linktooperation")).length() > 0) {
                    value = this.linkToOperation(value, controlData, row, linkToOperation, operations, controlData.getValue(row, keycolid1), controlData.getValue(row, keycolid2), controlData.getValue(row, keycolid3));
                }
                html.append("<td class=\"value\">").append(value).append("</td>");
            }
            html.append("</tr>");
        }
    }

    protected String getFormHTML(boolean export, PropertyListCollection columns, PropertyListCollection operations, DataSet controlData, int row, int formCols, boolean hideColumnIfNull, String keycolid1, String keycolid2, String keycolid3) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuilder html = new StringBuilder();
        int colPos = 0;
        for (int j = 0; j < columns.size(); ++j) {
            String linkToOperation;
            boolean forceStartAtColumn1;
            PropertyList column = columns.getPropertyList(j);
            if (!column.getProperty("show", "Y").equals("Y")) continue;
            String value = this.getColumnDisplayValue(controlData, row, column, tp);
            String title = tp.translate(column.getProperty("title", column.getProperty("columnid")));
            String tip = tp.translate(column.getProperty("tip"));
            if (hideColumnIfNull && value.length() <= 0) continue;
            int span = 1;
            try {
                span = Integer.parseInt(column.getProperty("colspan", "1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            boolean bl = forceStartAtColumn1 = span < 0 && colPos != 0;
            if (span < 0) {
                span = -1 * span;
            }
            if ((colPos += span) > formCols || forceStartAtColumn1) {
                html.append("<td nowrap class=\"value\" colspan=\"").append((formCols - colPos + span) * 2).append("\">&nbsp;</td></tr>");
                if (j < columns.size()) {
                    html.append("<tr>");
                }
                colPos = span;
            }
            html.append("<td title=\"" + tip + "\" class=\"title\">").append(title).append("</td>");
            if (!export && (linkToOperation = column.getProperty("linktooperation")).length() > 0) {
                value = this.linkToOperation(value, controlData, row, linkToOperation, operations, controlData.getValue(row, keycolid1), controlData.getValue(row, keycolid2), controlData.getValue(row, keycolid3));
            }
            html.append("<td nowrap class=\"value\" colspan=\"").append(2 * (span - 1) + 1).append("\">").append(value).append("</td>");
            if (colPos % formCols == 0) {
                html.append("</tr>");
                if (j < columns.size() - 1) {
                    html.append("<tr>");
                }
            }
            colPos %= formCols;
        }
        if (colPos != 0) {
            html.append("<td nowrap class=\"value\" colspan=\"").append(2 * (formCols - colPos)).append("\">&nbsp;</td></tr>");
        }
        if (controlData.size() > 1 && row < controlData.size() - 1) {
            html.append("<tr height=\"3\"><td colspan=\"100\">&nbsp;</td></tr>");
        }
        return html.toString();
    }

    protected String getColumnDisplayValue(DataSet controlData, int i, PropertyList column, TranslationProcessor translationProcessor) {
        String value;
        String columnid = column.getProperty("columnid");
        if (columnid.indexOf(" ") > 0) {
            columnid = RequestParser.parseAlias(columnid);
        }
        if (columnid.length() == 0) {
            columnid = "(UNDEFINED)";
        }
        String string = value = i >= 0 ? controlData.getValue(i, columnid) : columnid;
        if (!column.getProperty("_skipsanitize").equals("Y")) {
            value = ListColumn.sanitizeHTMLValue(value);
        }
        String displayValue = column.getProperty("displayvalue");
        if ((displayValue = WorksheetItem.replaceSubstitutionTokens(displayValue, controlData, i)).length() > 0) {
            value = SDITagUtil.getDisplayValue(value, displayValue);
        }
        if ("Y".equals(column.getProperty("translatevalue"))) {
            value = translationProcessor.translate(value);
        }
        return value;
    }

    public static String replaceSubstitutionTokens(String displayValue, DataSet ds, int idx) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(displayValue, "[", "]")) {
            if (!ds.isValidColumn(token)) continue;
            displayValue = StringUtil.replaceAll(displayValue, "[" + token + "]", ds.getString(idx, token, ""));
        }
        return displayValue;
    }

    public void addWordContent(WordWorksheet wordWorksheet, com.aspose.words.Document document, DocumentBuilder builder, PropertyList exportOptions) throws Exception {
        StringBuffer out = new StringBuffer();
        out.append("<style>");
        out.append(this.getExportCSS(wordWorksheet));
        out.append("</style>");
        out.append("<body>");
        String exportHTML = (String)this.rowdata.get("exporthtml");
        if (exportHTML == null || exportHTML.length() == 0) {
            exportHTML = (String)this.rowdata.get("html");
        }
        out.append("Complete".equals(this.rowdata.get("itemstatus")) && exportHTML != null && exportHTML.length() > 0 ? exportHTML : this.getExportHTML(exportOptions));
        out.append("</body>");
        HTMLEditorControl.injectImageURI(out, Configuration.getInstance().getServerHttpURL(), this.getSapphireConnection().getConnectionId());
        HTMLEditorControl.removeAnchors(out);
        builder.insertHtml(out.toString(), true);
    }

    protected String getExportCSS(WordWorksheet wordWorksheet) throws IOException {
        String name = this.getClass().getName();
        String css = wordWorksheet.getCachedCSS(name);
        if (css == null || css.isEmpty()) {
            InputStream is;
            css = wordWorksheet.getCachedBaseCSS();
            if (css == null || css.isEmpty()) {
                is = this.getClass().getResourceAsStream("/com/labvantage/sapphire/modules/eln/gwt/server/worksheetitem/worksheet.css");
                if (is != null) {
                    css = css + FileUtil.getInputStreamString(is);
                    is.close();
                }
                css = css + Worksheet.getTableStyling(wordWorksheet.getPolicy());
                wordWorksheet.setCachedBaseCSS(css);
            }
            name = "/" + StringUtil.replaceAll(name, ".", "/") + ".css";
            is = this.getClass().getResourceAsStream(name);
            if (is != null) {
                css = FileUtil.getInputStreamString(is);
                is.close();
            }
            wordWorksheet.setCachedCSS(name, css);
        }
        return css;
    }

    public void extractConfigParams(PropertyList config) throws SapphireException {
        ArrayList<String> params = this.findConfigParams(config);
        if (params.size() > 0) {
            DataSet worksheetitemparams = this.getQueryProcessor().getPreparedSqlDataSet("SELECT paramname FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
            for (int i = 0; i < worksheetitemparams.size(); ++i) {
                if (params.contains(worksheetitemparams.getValue(i, "paramname"))) {
                    params.remove(worksheetitemparams.getValue(i, "paramname"));
                    continue;
                }
                this.database.executePreparedUpdate("DELETE FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND paramname = ?", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId(), worksheetitemparams.getValue(i, "paramname")});
            }
            try {
                PreparedStatement stmt = this.database.prepareStatement("INSERT INTO worksheetitemparam ( worksheetitemid, worksheetitemversionid, paramname ) VALUES ( ?, ?, ? )");
                for (int i = 0; i < params.size(); ++i) {
                    stmt.setString(1, this.getWorksheetItemId());
                    stmt.setString(2, this.getWorksheetItemVersionId());
                    stmt.setString(3, params.get(i));
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to add worksheetitem params", e);
            }
        } else {
            this.database.executePreparedUpdate("DELETE FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
        }
    }

    protected void addConfigParamExclude(String propertyid) {
        this.configParamExcludes.add(propertyid);
    }

    private ArrayList<String> findConfigParams(PropertyList config) {
        ArrayList<String> params = new ArrayList<String>();
        for (String propertyid : config.keySet()) {
            Object propertyvalue = config.get(propertyid);
            if (propertyvalue instanceof String) {
                if (this.configParamExcludes.contains(propertyid)) continue;
                params.addAll(Arrays.asList(StringUtil.getTokens(config.getProperty(propertyid), "$P{", "}")));
                continue;
            }
            if (propertyvalue instanceof PropertyList) {
                params.addAll(this.findConfigParams(config.getPropertyList(propertyid)));
                continue;
            }
            if (!(propertyvalue instanceof PropertyListCollection)) continue;
            for (int i = 0; i < config.getCollection(propertyid).size(); ++i) {
                params.addAll(this.findConfigParams(config.getCollection(propertyid).getPropertyList(i)));
            }
        }
        return params;
    }

    public String[] getDependentWorksheetItems() throws SapphireException {
        String[] stringArray;
        DataSet ref;
        StringBuffer worksheetitemid = new StringBuffer();
        StringBuffer worksheetitemversionid = new StringBuffer();
        if (this.worksheetItemFields != null) {
            StringBuffer fields = new StringBuffer();
            for (String fieldid : this.worksheetItemFields.fieldMap.keySet()) {
                if (!this.worksheetItemFields.fieldMap.get((Object)fieldid).added && !this.worksheetItemFields.fieldMap.get((Object)fieldid).edited && !this.worksheetItemFields.fieldMap.get((Object)fieldid).delete) continue;
                fields.append("','").append(fieldid);
            }
            if (fields.length() > 0) {
                DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'field' AND valuelabel IN ('" + fields.substring(3) + "')", new Object[]{"LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
                worksheetitemid.append(dependents.getColumnValues("worksheetitemid", ";"));
                worksheetitemversionid.append(dependents.getColumnValues("worksheetitemversionid", ";"));
            }
        }
        if ((ref = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemreference WHERE refworksheetid = ? AND refworksheetversionid = ? AND refkeyid1 = ? AND refkeyid2 = ? AND reffunction IN ('link','field','ref' )", new Object[]{this.getWorksheetId(), this.getWorksheetVersionId(), this.getWorksheetItemId(), this.getWorksheetItemVersionId()})).size() > 0) {
            worksheetitemid.append(worksheetitemid.length() > 0 ? ";" : "").append(ref.getColumnValues("worksheetitemid", ";"));
            worksheetitemversionid.append(worksheetitemversionid.length() > 0 ? ";" : "").append(ref.getColumnValues("worksheetitemversionid", ";"));
        }
        if (worksheetitemid.length() > 0) {
            String[] stringArray2 = new String[2];
            stringArray2[0] = worksheetitemid.toString();
            stringArray = stringArray2;
            stringArray2[1] = worksheetitemversionid.toString();
        } else {
            String[] stringArray3 = new String[2];
            stringArray3[0] = "";
            stringArray = stringArray3;
            stringArray3[1] = "";
        }
        return stringArray;
    }

    protected WorksheetItemFields getWorksheetItemFields() throws SapphireException {
        this.worksheetItemFields = new WorksheetItemFields(new M18NUtil(this.sapphireConnection), FormatUtil.getInstance(this.sapphireConnection), this.getQueryProcessor(), this.getActionProcessor(), this.getWorksheetId(), this.getWorksheetVersionId(), this.getWorksheetItemId(), this.getWorksheetItemVersionId());
        DataSet fields = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemfield WHERE worksheetitemid = ? AND worksheetitemversionid = ? ORDER BY usersequence, fieldname, fieldinstance", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
        for (int i = 0; i < fields.size(); ++i) {
            String fieldid = fields.getValue(i, "fieldname");
            WorksheetItemFieldsField field = this.worksheetItemFields.getField(fieldid);
            if (field == null) {
                field = this.worksheetItemFields.createField(fieldid);
                field.loaded = true;
                field.fieldtitle = fields.getValue(i, "fieldtitle");
                field.datatype = fields.getValue(i, "datatype");
                field.sequence = fields.getInt(i, "usersequence");
                field.fieldContext = fields.getValue(i, "fieldcontext");
                field.fielddef = new PropertyList();
                field.fielddef.setPropertyList(fields.getClob(i, "fielddef", ""));
                this.worksheetItemFields.addField(field);
            }
            field.setInstance(fields.getInt(i, "fieldinstance"), fields.getValue(i, "enteredtext"), fields.getBigDecimal(i, "numericvalue"), fields.getCalendar(i, "datevalue"), fields.getValue(i, "displayvalue"));
        }
        return this.worksheetItemFields;
    }

    public void setWorksheetFields(DataSet fields) {
        this.worksheetFields = fields;
    }

    public DataSet getWorksheetFields() {
        if (this.worksheetFields == null) {
            try {
                RequestProcessor requestProcessor = new RequestProcessor(this.getSapphireConnection().getConnectionId());
                CommandRequest commandRequest = new CommandRequest("lf");
                commandRequest.set("worksheetid", this.getWorksheetId());
                commandRequest.set("worksheetversionid", this.getWorksheetVersionId());
                CommandResponse commandResponse = new CommandResponse();
                HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
                requestMap.put("commandrequest", commandRequest);
                requestMap.put("commandresponse", commandResponse);
                HashMap returnMap = requestProcessor.processRequest("com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest", requestMap);
                if (commandResponse.containsKey("fields") && commandResponse.get("fields") instanceof DataSet) {
                    this.worksheetFields = (DataSet)commandResponse.get("fields");
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return this.worksheetFields;
    }

    public String getCompleteHTML() throws SapphireException {
        return this.getViewHTML();
    }

    protected String unescapeChars(String text) {
        return text.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    protected void logError(Object out) {
        Trace.logError(this.logName, out, this.logContext);
    }

    protected void logError(Object out, Throwable t) {
        Trace.logError(this.logName, out, t, this.logContext);
    }

    protected void logWarn(Object out) {
        Trace.logWarn(this.logName, out, this.logContext);
    }

    protected void logInfo(Object out) {
        Trace.logInfo(this.logName, out, this.logContext);
    }

    protected void logDebug(Object out) {
        Trace.logDebug(this.logName, out, this.logContext);
    }

    protected ActionProcessor getActionProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.actionProcessor == null) {
                this.actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.actionProcessor;
        }
        throw new SapphireException("ActionProcessor not available in this mode");
    }

    protected ConnectionProcessor getConnectionProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.connectionProcessor == null) {
                this.connectionProcessor = new ConnectionProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.connectionProcessor;
        }
        throw new SapphireException("ConnectionProcessor not available in this mode");
    }

    protected DAMProcessor getDAMProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.damProcessor == null) {
                this.damProcessor = new DAMProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.damProcessor;
        }
        throw new SapphireException("DAMProcessor not available in this mode");
    }

    protected QueryProcessor getQueryProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.queryProcessor == null) {
                this.queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.queryProcessor;
        }
        throw new SapphireException("QueryProcessor not available in this mode");
    }

    protected SDCProcessor getSDCProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.sdcProcessor == null) {
                this.sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.sdcProcessor;
        }
        throw new SapphireException("SDCProcessor not available in this mode");
    }

    protected SDIProcessor getSDIProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.sdiProcessor == null) {
                this.sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.sdiProcessor;
        }
        throw new SapphireException("SDIProcessor not available in this mode");
    }

    protected SequenceProcessor getSequenceProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.sequenceProcessor == null) {
                this.sequenceProcessor = new SequenceProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.sequenceProcessor;
        }
        throw new SapphireException("SequenceProcessor not available in this mode");
    }

    protected TranslationProcessor getTranslationProcessor() throws SapphireException {
        if (this.sapphireConnection != null) {
            if (this.translationProcessor == null) {
                this.translationProcessor = new TranslationProcessor(this.sapphireConnection.getConnectionId());
            }
            return this.translationProcessor;
        }
        throw new SapphireException("TranslationProcessor not available in this mode");
    }

    protected static int getInt(String value, int def) {
        int ret;
        try {
            ret = Integer.parseInt(value);
        }
        catch (Exception e) {
            ret = def;
        }
        return ret;
    }

    protected String getTableStylingProperty(String propertyid, boolean export, String defaultValue) {
        String value;
        String originalproperty = propertyid;
        if (export) {
            propertyid = propertyid + "publish";
        }
        if ((value = this.config.getProperty(propertyid, this.config.getProperty(originalproperty)).trim()).equals("X")) {
            value = this.config.getProperty(originalproperty);
        }
        if (value.length() == 0) {
            return defaultValue;
        }
        if (value.equalsIgnoreCase("open table")) {
            return "lv_elntable_opentable  ";
        }
        if (value.equalsIgnoreCase("grid")) {
            return "lv_elntable_grid  ";
        }
        if (value.equalsIgnoreCase("form")) {
            return "lv_elntable_form ";
        }
        return value;
    }

    protected boolean isPublishTableStylingDifferent(String propertyid) {
        String tablestyling1 = this.config.getProperty(propertyid);
        String tablestyling2 = this.config.getProperty(propertyid + "publish");
        return tablestyling2.length() > 0 && !tablestyling2.equals("X") && !tablestyling2.equals(tablestyling1);
    }

    public DataSet getQuerySDIs(String filterWhere) throws SapphireException {
        String source = this.config.getProperty("source", "Control");
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        PropertyListCollection sortby = this.config.getCollectionNotNull("sortby");
        String sort = "";
        for (int i = 0; i < sortby.size(); ++i) {
            sort = sort + "," + sortby.getPropertyList(i).getProperty("columnid") + " " + sortby.getPropertyList(i).getProperty("asc_desc");
        }
        if (source.length() > 0 && sourcesdcid.length() > 0) {
            String keycolid3;
            PropertyListCollection columns = new PropertyListCollection();
            String keycolid1 = this.getSDCProcessor().getProperty(sourcesdcid, "keycolid1");
            this.addKeyCol(columns, keycolid1, 1);
            String keycolid2 = this.getSDCProcessor().getProperty(sourcesdcid, "keycolid2");
            if (keycolid2.length() > 0) {
                this.addKeyCol(columns, keycolid2, 2);
            }
            if ((keycolid3 = this.getSDCProcessor().getProperty(sourcesdcid, "keycolid3")).length() > 0) {
                this.addKeyCol(columns, keycolid3, 3);
            }
            SDIRequest sdiRequest = this.getSDIRequest(source, sourcesdcid, columns, null, true);
            sdiRequest.setUseRSetOrderBy(true);
            if (filterWhere.length() > 0) {
                String where = sdiRequest.getQueryWhere();
                if (where.length() > 0) {
                    sdiRequest.setQueryWhere(where + " AND ( " + filterWhere + ")");
                } else {
                    sdiRequest.setQueryWhere(filterWhere);
                }
            }
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet ds = sdiData.getDataset("primary");
            ds.sort(sort);
            return ds;
        }
        return null;
    }

    private void addKeyCol(PropertyListCollection columns, String keycolid, int i) {
        PropertyList pl1 = new PropertyList();
        pl1.setProperty("columnid", keycolid);
        columns.add(pl1);
        PropertyList pl2 = new PropertyList();
        pl1.setProperty("columnid", keycolid + " keyid" + i);
        columns.add(pl2);
    }
}

