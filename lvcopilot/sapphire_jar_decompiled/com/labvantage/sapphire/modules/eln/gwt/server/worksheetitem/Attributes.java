/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.MaintAttribute;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.tagext.QueryData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Attributes
extends BaseWorksheetItem {
    private static final String DEFAULT_GROUP = "NA";
    private static final String JSOBJECT = "attributesEditor";
    private String templateMessage;
    private boolean fullwidth = true;
    private int instructionOnlyCount = 0;
    private String attributesdcid = "";
    private DataSet sdiattribute = null;
    private PropertyListCollection primaryCols;
    private PropertyListCollection sdiwiCols;
    private PropertyListCollection datasetCols;
    private String keyColId1 = "";
    private String keyColId2 = "";
    private String keyColId3 = "";
    private String sourceSDC = "";
    public static final String COLUMN_VALIDFLAG = "__validflag";

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        String tableStyling = this.getTableStylingProperty("displaystyle", false, "lv_elntable_grid  ");
        try {
            this.sdiattribute = this.getAttributeData(tableStyling);
        }
        catch (Exception e) {
            Logger.logError("Could not obtain attribute data.", e);
        }
        String mode = this.config.getProperty("attributemode");
        String source = this.config.getProperty("source", "Control");
        worksheetItemOptions.setSupportsDataAvailablity(true);
        worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("displaystyle"));
        if (this.config.getProperty("sourcesdcid").length() > 0) {
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), this.config.getProperty("sourcesdcid"));
            worksheetItemOptions.setSupportsQuerySDIs(source.equalsIgnoreCase("query"));
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        }
        if (mode.equalsIgnoreCase("worksheet")) {
            try {
                this.getViewHTML();
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.sdiattribute != null && this.sdiattribute.getRowCount() > 0 && this.sdiattribute.getRowCount() == this.instructionOnlyCount) {
                worksheetItemOptions.setViewOnly(true);
            }
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/attributes.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/scripts/tags.js");
        try {
            boolean devMode = Configuration.isDevmode(this.getSapphireConnection().getDatabaseId());
            worksheetItemIncludes.getScriptIncludes().addAll(MaintAttribute.getScriptIncludes(this.getSapphireConnection().getUseFullIncludes() || devMode));
        }
        catch (Exception exception) {
            // empty catch block
        }
        worksheetItemIncludes.getStyleIncludes().addAll(MaintAttribute.getStyleIncludes());
        worksheetItemIncludes.setJSObjectName(JSOBJECT);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.renderHTML(false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.renderHTML(true);
    }

    private String renderHTML(boolean export) throws SapphireException {
        String tableStyling = this.getTableStylingProperty("displaystyle", export, "lv_elntable_grid  ");
        this.instructionOnlyCount = 0;
        StringBuilder html = new StringBuilder();
        if (this.sdiattribute == null) {
            this.sdiattribute = this.getAttributeData(tableStyling);
        }
        if (this.primaryCols == null) {
            this.primaryCols = this.config.getCollectionNotNull("primarycolumns");
        }
        if (this.sdiwiCols == null) {
            this.sdiwiCols = this.config.getCollectionNotNull("sdiworkitemcolumns");
        }
        if (this.datasetCols != null) {
            this.datasetCols = this.config.getCollectionNotNull("sdidatacolumns");
        }
        this.fullwidth = this.config.getProperty("fullwidth", "Y").equals("Y");
        if (!this.isTemplate()) {
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            if (this.sdiattribute != null) {
                if (!this.sdiattribute.isValidColumn("__attributegroup")) {
                    this.sdiattribute.addColumn("__attributegroup", 0);
                }
                MaintAttribute.sortAttributeData(this.sdiattribute, this.attributesdcid, this.getSapphireConnection().getConnectionId());
                PropertyList sdc = sdcProcessor.getPropertyList(this.attributesdcid);
                if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
                    PropertyListCollection attributesdef = sdc.getCollection("attributes");
                    if (attributesdef != null) {
                        int i;
                        if (attributesdef.size() == 0) {
                            this.logWarn("No attributes have been defined for SDC " + sdc.getProperty("sdcid") + " in master list");
                        }
                        boolean grouped = false;
                        int rowcount = 0;
                        this.sdiattribute.addColumn("_attributedef", -1);
                        String c1 = "";
                        String c2 = "";
                        String c3 = "";
                        boolean multiplePrimary = false;
                        ArrayList<String> attributeids = new ArrayList<String>();
                        DataSet attributeidset = new DataSet();
                        attributeidset.addColumn("attributeid", 0);
                        attributeidset.addColumn("attributeinstance", 1);
                        for (i = 0; i < this.sdiattribute.getRowCount(); ++i) {
                            String rs;
                            BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(this.sdiattribute.getValue(i, "attributesourcetype"));
                            if (type == BaseSDIAttributeAction.AttributeType.linkdef || (rs = this.sdiattribute.getValue(i, "__rowstatus", "S")).equalsIgnoreCase("D")) continue;
                            String attributeid = this.sdiattribute.getValue(i, "attributeid", "");
                            String attributeinstance = this.sdiattribute.getValue(i, "attributeinstance", "");
                            String key = attributeid + ";" + attributeinstance;
                            if (attributeids.size() == 0 || !attributeids.contains(key)) {
                                attributeids.add(key);
                                int r = attributeidset.addRow();
                                attributeidset.setString(r, "attributeid", attributeid);
                                try {
                                    attributeidset.setNumber(r, "attributeinstance", Integer.parseInt(attributeinstance));
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            String k1 = this.sdiattribute.getValue(i, "keyid1");
                            String k2 = this.sdiattribute.getValue(i, "keyid2");
                            String k3 = this.sdiattribute.getValue(i, "keyid3");
                            if (!(c1.length() == 0 || c1.equals(k1) && c2.equals(k2) && c3.equals(k3))) {
                                multiplePrimary = true;
                            }
                            c1 = k1;
                            c2 = k2;
                            c3 = k3;
                            ++rowcount;
                            PropertyList attributedef = attributesdef.find("attributeid", attributeid);
                            this.sdiattribute.setObject(i, "_attributedef", attributedef);
                            if (attributedef != null) {
                                if (!this.sdiattribute.isValidColumn("__attributegroup")) continue;
                                String groupid = attributedef.getProperty("attributegroup", "");
                                if (groupid.length() > 0) {
                                    grouped = true;
                                    this.sdiattribute.setValue(i, "__attributegroup", groupid);
                                    continue;
                                }
                                this.sdiattribute.setValue(i, "__attributegroup", "");
                                continue;
                            }
                            this.sdiattribute.setValue(i, "__attributegroup", "");
                        }
                        attributeidset.sort("attributeid, attributeinstance");
                        attributeids = new ArrayList();
                        for (i = 0; i < attributeidset.getRowCount(); ++i) {
                            String attributeid = attributeidset.getValue(i, "attributeid", "");
                            String attributeinstance = attributeidset.getValue(i, "attributeinstance", "");
                            String key = attributeid + ";" + attributeinstance;
                            attributeids.add(key);
                        }
                        if (multiplePrimary) {
                            ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId());
                            String gridLayout = this.config.getProperty("gridlayout", "Attributes Across");
                            if (!"SDI Across".equals(gridLayout)) {
                                Attributes.renderStandardModeAttributeAcross(html, sdc, this.sdiattribute, rowcount, attributeids, this.config, "maintAttribute", this.getElementId(), this.instructionOnlyCount, this.fullwidth, this, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), new Logger(this.logContext), true, null, null, null, connectionInfo, this.getSDCProcessor(), false, tableStyling);
                            } else {
                                Attributes.renderStandardModeSDIAcross(html, sdc, this.sdiattribute, grouped, this.config, JSOBJECT, this.getElementId(), this.instructionOnlyCount, this.fullwidth, this, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), new Logger(this.logContext), true, null, null, null, this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId()), null, true, attributeids, tableStyling);
                            }
                        } else {
                            String gridLayout = this.config.getProperty("gridlayout", "SDI Across");
                            if ("SDI Across".equals(gridLayout)) {
                                this.renderSingleSDI(html, sdc, this.sdiattribute, grouped, rowcount, true, attributeids, tableStyling);
                            } else {
                                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId());
                                Attributes.renderStandardModeAttributeAcross(html, sdc, this.sdiattribute, rowcount, attributeids, this.config, "maintAttribute", this.getElementId(), this.instructionOnlyCount, this.fullwidth, this, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), new Logger(this.logContext), true, null, null, null, connectionInfo, this.getSDCProcessor(), true, tableStyling);
                            }
                        }
                    } else {
                        html.append("No attributes defined for SDC " + this.attributesdcid + ".");
                    }
                } else {
                    html.append("Attributes not switched on for SDC " + this.attributesdcid + ".");
                }
            } else {
                html.append("Attributes could not be retrieved.");
            }
        } else {
            html.append("<table width=\"100%\">");
            html.append("<tr><td>").append(this.templateMessage).append("</td></tr>");
            html.append("</table>");
        }
        return html.toString();
    }

    @Override
    public String getIndexingText() {
        return "";
    }

    /*
     * Unable to fully structure code
     */
    private static DataSet renderStandardModeAttributeAcross(StringBuilder areahtml, PropertyList sdc, DataSet attributedata, int rowcount, ArrayList<String> attributeids, PropertyList config, String jsobject, String elementid, int instructOnly, boolean fullwidth, Attributes attributes, TranslationProcessor tp, QueryProcessor qp, ConnectionProcessor cp, Logger logger, boolean viewOnly, PageContext pageContext, SDITagInfo sdiInfo, M18NUtil m18nServer, ConnectionInfo connectionInfo, SDCProcessor sdcp, boolean singleSDI, String tableStyling) throws SapphireException {
        primarySortBy = config.getCollectionNotNull("sortby");
        wiSortBy = config.getCollectionNotNull("sdiworkitemsortby");
        dsSortBy = config.getCollectionNotNull("sdidatasortby");
        sortByStr = new StringBuffer();
        sortByPrimary = new StringBuffer();
        sortByWorkItem = new StringBuffer();
        sortByDataSet = new StringBuffer();
        primaryCols = config.getCollectionNotNull("primarycolumns");
        sdiwiCols = config.getCollectionNotNull("sdiworkitemcolumns");
        datasetCols = config.getCollectionNotNull("sdidatacolumns");
        finishedSDIAttributeColor = config.getProperty("highlightfinishedsdiattribute");
        cancelSDIAttributeColor = config.getProperty("highlightcancelledsdiattribute");
        protectFinished = "Y".equalsIgnoreCase(config.getProperty("protectfinishedsdiattribute"));
        protectCancelled = "Y".equalsIgnoreCase(config.getProperty("protectcancelledsdiattribute"));
        attributesdcid = sdc.getProperty("sdcid");
        attrType = config.getProperty("attributetype", "Primary");
        validations = new JSONObject();
        Attributes.getSortByString(primarySortBy, "primary", tp, attributedata, sortByStr, sortByPrimary);
        sortByStr.append(sortByStr.length() > 0 ? "," : "").append("primary1, primary2, primary3");
        sortByPrimary.append(sortByPrimary.length() > 0 ? "," : "").append("primary1, primary2, primary3");
        if (attributedata.isValidColumn("sdiworkitem_workiteminstance")) {
            Attributes.getSortByString(wiSortBy, "sdiworkitem", tp, attributedata, sortByStr, sortByWorkItem);
            sortByStr.append(",sdiworkitem_usersequence");
            sortByWorkItem.append(sortByWorkItem.length() > 0 ? "," : "").append("sdiworkitem_usersequence");
        }
        if (attributedata.isValidColumn("sdidata_dataset")) {
            Attributes.getSortByString(dsSortBy, "sdidata", tp, attributedata, sortByStr, sortByDataSet);
            sortByStr.append(",sdidata_usersequence");
            sortByDataSet.append(sortByDataSet.length() > 0 ? "," : "").append("sdidata_usersequence");
        }
        Attributes.updateInsertedRows(attributedata);
        sortByStr.append(sortByStr.length() == 0 ? "" : ",").append("keyid1,keyid2,keyid3");
        attributedata.sort("keyid1,keyid2,keyid3,attributeid,attributeinstance");
        MaintAttribute.renumberAttributeRows(attributedata, 1);
        attributesdef = sdc.getCollection("attributes");
        allowRequired = sdc.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
        grid = new DataSet();
        grid.addColumn("keyid1", 0);
        grid.addColumn("keyid2", 0);
        grid.addColumn("keyid3", 0);
        grid.addColumn("primary1", 0);
        grid.addColumn("primary2", 0);
        grid.addColumn("primary3", 0);
        areahtml.append("<table class=\"" + tableStyling + "\" style=\"border-spacing: 0px\" " + (fullwidth != false ? "width=\"100%\"" : "") + " border=1 bordercolor='#92a8d1'>");
        areahtml.append("<thead>");
        areahtml.append("<tr>");
        sourceSDC = config.getProperty("sourcesdcid");
        displaySDCTitle = "";
        if (!"SDIWorkItem".equalsIgnoreCase(sourceSDC) && attributedata.getRowCount() > 0) {
            displaySDCTitle = sdcp.getProperty(sourceSDC, "singular", sourceSDC);
        }
        primaryColumns = new ArrayList<E>();
        workitemColumns = new ArrayList<E>();
        datasetColumns = new ArrayList<E>();
        if (attributeids.size() > 0) {
            if (!singleSDI && primaryCols.size() == 0 && sdiwiCols.size() == 0 && datasetCols.size() == 0) {
                areahtml.append("<th class=\"title\">").append(displaySDCTitle.length() > 0 ? displaySDCTitle : "&nbsp;").append("</th>");
            }
            areahtml.append(Attributes.getColumnHtml(attributedata, primaryColumns, workitemColumns, datasetColumns, primaryCols, sdiwiCols, datasetCols, false, false, false, null));
        }
        for (a = 0; a < attributeids.size(); ++a) {
            attributeid = StringUtil.split(attributeids.get(a), ";");
            attributedef = attributesdef.find("attributeid", attributeid[0]);
            find = attributedata.findRow("attributeid", attributeid[0]);
            groupid = "";
            if (find > -1) {
                groupid = attributedata.getValue(find, "__attributegroup", "NA");
            }
            if (attributedef != null) {
                areahtml.append("<th class=\"title\" title=\"").append(attributeid[0]).append(" (").append(attributeid[1]).append(")").append("\">");
                areahtml.append(SafeHTML.encodeForHTMLAttribute(attributedef.getProperty("attributetitle", attributeid[0]))).append(" (").append(attributeid[1]).append(")");
            } else {
                areahtml.append("<th class=\"" + tableStyling + "\">");
                areahtml.append(SafeHTML.encodeForHTMLAttribute(attributeid[0]) + " (" + attributeid[1] + ")");
            }
            areahtml.append("</th>");
            grid.addColumn(attributeids.get(a), 0);
        }
        if (!viewOnly) {
            areahtml.append("<th class=\"title\" nowrap\">");
            areahtml.append(tp.translate("Add New")).append("</th>");
        }
        areahtml.append("</tr>");
        areahtml.append("</thead>");
        areahtml.append("<tbody>");
        c_keyid1 = "";
        c_keyid2 = "";
        c_keyid3 = "";
        rendered = false;
        gridrow = 0;
        allColumns = attributedata.getColumns();
        ddComboValuesLst = new ArrayList<String>();
        for (i = 0; i < attributedata.getRowCount(); ++i) {
            type = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype"));
            cancelled_sdi = false;
            finished_sdi = false;
            statusColumn = attributesdcid.equalsIgnoreCase("DataSet") != false ? "s_datasetstatus" : (attributesdcid.equalsIgnoreCase("SDIWorkItem") != false ? "workitemstatus" : "primary_status");
            status = attributedata.getValue(i, statusColumn);
            if (type == BaseSDIAttributeAction.AttributeType.linkdef || (rs = attributedata.getValue(i, "__rowstatus", "S")).equalsIgnoreCase("D")) continue;
            row = Integer.parseInt(attributedata.getString(i, "__rowid", i + ""));
            hidden = attributedata.getValue(i, "hiddenflag", "N").equalsIgnoreCase("Y");
            mandatory = allowRequired != false && attributedata.getValue(i, "mandatoryflag", "N").equalsIgnoreCase("Y") != false;
            attributeid = attributedata.getValue(i, "attributeid", "");
            attributeinstance = attributedata.getValue(i, "attributeinstance", "");
            attributetype = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
            attributedef = (PropertyList)attributedata.getObject(i, "_attributedef");
            attributedata.setObject(i, "_attributedef", null);
            readOnly = false;
            current_instructionflag = attributedata.getValue(i, "instructionflag", "N");
            if (attributedef != null) {
                def_instructionflag = attributedef.getProperty("instructionflag", attributeid);
                groupid = attributedata.getValue(i, "__attributegroup", "NA");
                title = tp.translate(attributedef.getProperty("attributetitle", ""));
                tip = tp.translate(attributedef.getProperty("helptext", ""));
                if (tip.length() == 0) {
                    tip = title.length() > 0 ? title : attributeid;
                }
                tip = tip + " (" + attributeid + " ";
                switch (2.$SwitchMap$com$labvantage$sapphire$actions$sdi$BaseSDIAttributeAction$AttributeType[attributetype.ordinal()]) {
                    case 1: {
                        tip = tip + " " + tp.translate("adhoc added");
                        break;
                    }
                    case 2: {
                        tip = tip + " " + tp.translate("from") + " " + attributedata.getValue(i, "sourcesdcid", "");
                        break;
                    }
                    case 3: {
                        tip = tip + " " + tp.translate("auto added");
                    }
                }
                tip = tip + ")";
                defaultEditorStyle = attributedef.getProperty("editorstyleid", "");
            } else if (current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O")) {
                def_instructionflag = "N";
                groupid = "NA";
                tip = "";
                title = "";
                defaultEditorStyle = "";
            } else {
                if (current_instructionflag.equalsIgnoreCase("R")) {
                    title = "";
                    tip = "";
                    current_instructionflag = "R";
                } else {
                    readOnly = true;
                    title = attributeid;
                    tip = "Attribute no longer defined in master list.";
                    current_instructionflag = "N";
                }
                def_instructionflag = "N";
                defaultEditorStyle = "";
                groupid = "NA";
            }
            keyid1 = attributedata.getValue(i, "keyid1", "");
            keyid2 = attributedata.getValue(i, "keyid2", "");
            keyid3 = attributedata.getValue(i, "keyid3", "");
            primary1 = attributedata.getValue(i, "primary1", keyid1);
            primary2 = attributedata.getValue(i, "primary2", keyid2);
            primary3 = attributedata.getValue(i, "primary3", keyid3);
            if (c_keyid1.length() == 0) {
                gridrow = grid.addRow();
                grid.setValue(gridrow, "keyid1", keyid1.equalsIgnoreCase("(null)") != false ? "" : keyid1);
                grid.setValue(gridrow, "keyid2", keyid2.equalsIgnoreCase("(null)") != false ? "" : keyid2);
                grid.setValue(gridrow, "keyid3", keyid3.equalsIgnoreCase("(null)") != false ? "" : keyid3);
                for (a = 0; a < allColumns.length; ++a) {
                    if (allColumns[a].equalsIgnoreCase("keyid1") || allColumns[a].equalsIgnoreCase("keyid2") || allColumns[a].equalsIgnoreCase("keyid3")) continue;
                    column = allColumns[a];
                    grid.setString(gridrow, column, attributedata.getValue(i, column));
                }
                c_keyid1 = keyid1;
                c_keyid2 = keyid2;
                c_keyid3 = keyid3;
            } else if (!(c_keyid1.equals(keyid1) && c_keyid2.equals(keyid2) && c_keyid3.equals(keyid3))) {
                gridrow = grid.addRow();
                grid.setValue(gridrow, "keyid1", keyid1.equalsIgnoreCase("(null)") != false ? "" : keyid1);
                grid.setValue(gridrow, "keyid2", keyid2.equalsIgnoreCase("(null)") != false ? "" : keyid2);
                grid.setValue(gridrow, "keyid3", keyid3.equalsIgnoreCase("(null)") != false ? "" : keyid3);
                for (a = 0; a < allColumns.length; ++a) {
                    if (allColumns[a].equalsIgnoreCase("keyid1") || allColumns[a].equalsIgnoreCase("keyid2") || allColumns[a].equalsIgnoreCase("keyid3")) continue;
                    column = allColumns[a];
                    grid.setString(gridrow, column, attributedata.getValue(i, column));
                }
                c_keyid1 = keyid1;
                c_keyid2 = keyid2;
                c_keyid3 = keyid3;
            }
            grid.setValue(gridrow, "primary1", primary1.equalsIgnoreCase("(null)") != false ? "" : primary1);
            grid.setValue(gridrow, "primary2", primary2.equalsIgnoreCase("(null)") != false ? "" : primary2);
            grid.setValue(gridrow, "primary3", primary3.equalsIgnoreCase("(null)") != false ? "" : primary3);
            instructiontext = "";
            if (current_instructionflag.equalsIgnoreCase("R") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("A")) {
                sql = new StringBuffer();
                safeSQL = new SafeSQL();
                sql.append("SELECT instructiontext FROM sdiattribute ");
                sql.append("WHERE ");
                sql.append("attributeid = ").append(safeSQL.addVar(attributeid));
                sql.append(" AND ");
                sql.append(" sdcid = ").append(safeSQL.addVar(attributedata.getValue(i, "sourcesdcid", "")));
                sql.append(" AND ");
                sql.append(" keyid1 = ").append(safeSQL.addVar(attributedata.getValue(i, "sourcekeyid1", "")));
                k2 = attributedata.getValue(i, "sourcekeyid2", "");
                if (k2.length() > 0) {
                    sql.append(" AND ");
                    sql.append(" keyid2 = ").append(safeSQL.addVar(k2));
                }
                if ((k3 = attributedata.getValue(i, "sourcekeyid3", "")).length() > 0) {
                    sql.append(" AND ");
                    sql.append(" keyid3 = ").append(safeSQL.addVar(k3));
                }
                sql.append(" AND ");
                sql.append("attributeinstance = ").append(safeSQL.addVar(attributedata.getValue(i, "sourceattributeinstance", "")));
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (ds != null && ds.size() > 0) {
                    instructiontext = ds.getValue(0, "instructiontext", "");
                } else {
                    Logger.logWarn("Could not obtain source of adhoc attribute.");
                }
            } else if (def_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("O")) {
                instructiontext = attributedef.getProperty("instructiontext");
            }
            showtitle = true;
            if (title.length() == 0) {
                if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                    showtitle = false;
                } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                    showtitle = false;
                } else if (attributedef == null) {
                    showtitle = false;
                } else {
                    title = attributeid;
                }
            }
            cellhtml = new StringBuffer();
            siid = elementid + row + "_value";
            cellhtml.append("<div id=\"").append(siid).append("\" style=\"display:block;\" class=\"attribute_value\"");
            hint = "";
            if (showtitle) {
                title = title + "<p>" + instructiontext;
            }
            cellhtml.append(">");
            if (instructiontext.length() > 0) {
                iiid = siid + "_title";
                cellhtml.append("<div id=\"").append(iiid).append("\" style=\"display:none;\">");
                cellhtml.append(SafeHTML.encodeForHTMLAttribute(title));
                cellhtml.append("</div>");
            }
            if (!viewOnly && !readOnly) {
                if (protectCancelled) {
                    v0 = cancelled_sdi = "Cancelled".equalsIgnoreCase(status) != false || "Cancel".equalsIgnoreCase(status) != false;
                    if (cancelled_sdi) {
                        readOnly = true;
                    }
                }
                if (protectFinished && (finished_sdi = Attributes.isSDIFinished(attributesdcid, attributedata, i, status))) {
                    readOnly = true;
                }
            }
            if (!def_instructionflag.equalsIgnoreCase("O") && !current_instructionflag.equalsIgnoreCase("O")) {
                editorstyle = attributedata.getValue(i, "editorstyleid", defaultEditorStyle);
                toadd = new StringBuffer();
                if (readOnly || viewOnly) {
                    sdcid = attributedata.getValue(row, "editsdcid", "");
                    reftypeid = attributedata.getValue(row, "editreftypeid", "");
                    try {
                        editorstyleprops = editorstyle.length() > 0 ? EditorStyleField.getEditorStyleProperties(editorstyle, sdcid, reftypeid, cp.getSapphireConnection(), qp) : null;
                    }
                    catch (Exception e) {
                        editorstyleprops = null;
                    }
                    v = MaintAttribute.getAttributeValue(m18nServer, connectionInfo, attributedata, i, false, false);
                    if (editorstyleprops != null && editorstyleprops.getProperty("mode").equalsIgnoreCase("checkbox")) {
                        cellhtml.append("<input type=\"checkbox\" ").append(v.equalsIgnoreCase("Y") != false ? " checked" : "").append(" onclick=\"return false;\">");
                    } else {
                        cellhtml.append(v.length() > 0 ? SafeHTML.encodeForHTMLAttribute(v) : "<i>" + tp.translate("Undefined") + "</i>");
                    }
                } else {
                    try {
                        updateable = attributedata.getValue(i, "updateableflag", "Y").equalsIgnoreCase("Y");
                        onChangeEvent = jsobject + ".change(event,'" + elementid + "'," + row + ")";
                        esf = Attributes.getAttributeField(pageContext, sdiInfo, m18nServer, connectionInfo, elementid, editorstyle, attributeid, title, i, row, attributedata, false, hidden, updateable, viewOnly != false || readOnly != false, onChangeEvent);
                        if (attributedata.getValue(i, "__validflag", "Y").equalsIgnoreCase("N")) {
                            esf.setColumnProperty("class", "validationfail");
                        }
                        validation = esf.getColumnProperty("validation");
                        if (mandatory) {
                            validation = validation.length() > 0 ? "Mandatory;" + validation : "Mandatory";
                            esf.setColumnProperty("validation", validation);
                        }
                        if (validation.length() > 0 && !hidden) {
                            valid = new JSONObject();
                            valid.put("text", validation);
                            valid.put("row", row);
                            validations.put(elementid + row + "_" + attributeid, valid);
                        }
                        esfHtml = esf.getHtml();
                        cellhtml.append(esfHtml);
                        if (pageContext.getAttribute("dd_dropdownvalues") == null) ** GOTO lbl353
                        comboValue = pageContext.getAttribute("dd_dropdownvalues").toString();
                        comboValueArray = StringUtil.split(comboValue, ";");
                        for (a = 0; a < comboValueArray.length; ++a) {
                            if (comboValueArray[a].length() <= 0 || ddComboValuesLst.contains(comboValueArray[a])) continue;
                            ddComboValuesLst.add(comboValueArray[a]);
                        }
                    }
                    catch (Exception e) {
                        areahtml.append("<td colspan=7 class=\"gridmaint_field\">");
                        Attributes.renderError(areahtml, tp.translate("Could not render editor style. Error:") + e.getMessage());
                        areahtml.append("</td>");
                    }
                }
            } else {
                cellhtml.append("<i>Information</i>");
            }
lbl353:
            // 6 sources

            if (!singleSDI) {
                deleteAttributeBehavior = config.getProperty("deleteattributebehavior", "deletenewattributes");
                flag = "N";
                columnID = "";
                if ("deleteanyattributes".equals(deleteAttributeBehavior)) {
                    dataType = attributedata.getValue(i, "datatype", "S");
                    columnID = "D".equals(dataType) != false ? "datevalue" : ("C".equals(dataType) != false ? "clobvalue" : ("N".equals(dataType) != false ? "numericvalue" : "textvalue"));
                    if ("I".equals(attributedata.getValue(i, "__rowstatus"))) {
                        flag = "Y";
                    } else if (attributedata.getValue(i, columnID) == null || "".equals(attributedata.getValue(i, columnID))) {
                        if (!"OldValNotBlank".equals(attributedata.getValue(i, "oldvalue", ""))) {
                            flag = "Y";
                        }
                    } else if (attributedata.isValidColumn("oldvalue") && "OldValBlank".equals(attributedata.getValue(i, "oldvalue", ""))) {
                        flag = "Y";
                    }
                }
                if (!"donotdelete".equals(deleteAttributeBehavior) && ("deletenewattributes".equals(deleteAttributeBehavior) && attributedata.getValue(i, "__rowstatus").equals("I") || "deleteanyattributes".equals(deleteAttributeBehavior) && "Y".equals(flag))) {
                    cellhtml.append("<a style=\"width:100%;height:100%;cursor:pointer;\" align=\"center\" onclick=\"").append(jsobject).append(".removeMSA('").append(elementid).append("','").append(row).append("')").append("\">");
                    image = new Image(pageContext);
                    image.setImageId("Delete");
                    image.setStyle("cursor:pointer");
                    image.setDimensions(10, 10);
                    image.setTitle("Click to remove attribute.");
                    cellhtml.append(image.getHtml());
                }
            }
            cellhtml.append("</div>");
            grid.setValue(gridrow, attributeid + ";" + attributeinstance, cellhtml.toString());
            if (rendered) continue;
            rendered = true;
        }
        if (ddComboValuesLst.size() > 0) {
            areahtml.append("\n<script>");
            itr = ddComboValuesLst.iterator();
            while (itr.hasNext()) {
                areahtml.append("\n" + itr.next() + ";");
            }
            areahtml.append("\n</script>\n");
        }
        grid.sort(sortByPrimary.toString());
        groups = grid.getGroupedDataSets(sortByPrimary.toString());
        for (grp = 0; grp < groups.size(); ++grp) {
            primary_Group = groups.get(grp);
            for (g = 0; g < primary_Group.getRowCount(); ++g) {
                keyid1 = primary_Group.getValue(g, "keyid1", "");
                keyid2 = primary_Group.getValue(g, "keyid2", "");
                keyid3 = primary_Group.getValue(g, "keyid3", "");
                pri1 = primary_Group.getValue(g, "primary1", keyid1);
                pri2 = primary_Group.getValue(g, "primary2", keyid2);
                pri3 = primary_Group.getValue(g, "primary3", keyid3);
                if (g != 0) continue;
                areahtml.append("<tr>");
                if (!singleSDI && primaryCols.size() == 0 && sdiwiCols.size() == 0 && datasetCols.size() == 0) {
                    areahtml.append("<th valign=top rowspan=" + primary_Group.getRowCount() + " nowrap class=\"value\">").append(pri1).append(pri2.length() > 0 ? "(" + pri2 + (pri3.length() > 0 ? " " + pri3 : "") + ")" : "").append("</th>");
                }
                for (a = 0; a < primaryColumns.size(); ++a) {
                    column = (String)primaryColumns.get(a);
                    cellhtml = primary_Group.getValue(0, column, "");
                    areahtml.append("<td valign=top rowspan=" + primary_Group.getRowCount() + " nowrap class=\"value\">");
                    areahtml.append(cellhtml);
                    areahtml.append("</td>");
                }
                sdiwiGrps = new ArrayList<DataSet>();
                if (sortByWorkItem.length() > 0) {
                    primary_Group.sort(sortByWorkItem.toString());
                    sdiwiGrps = primary_Group.getGroupedDataSets(sortByWorkItem.toString());
                } else {
                    sdiwiGrps.add(primary_Group);
                }
                for (wi = 0; wi < sdiwiGrps.size(); ++wi) {
                    sdiwis = (DataSet)sdiwiGrps.get(wi);
                    if (wi > 0) {
                        areahtml.append("<tr>");
                    }
                    for (swi = 0; swi < sdiwis.getRowCount(); ++swi) {
                        if (swi != 0) continue;
                        for (a = 0; a < workitemColumns.size(); ++a) {
                            column = (String)workitemColumns.get(a);
                            cellhtml = sdiwis.getValue(0, column, "");
                            areahtml.append("<td valign=top rowspan=" + sdiwis.getRowCount() + " nowrap class=\"value\">");
                            areahtml.append(cellhtml);
                            areahtml.append("</td>");
                        }
                        dsGrps = new ArrayList<DataSet>();
                        if (sortByDataSet.length() > 0) {
                            sdiwis.sort(sortByDataSet.toString());
                            dsGrps = sdiwis.getGroupedDataSets(sortByDataSet.toString());
                        } else {
                            dsGrps.add(sdiwis);
                        }
                        for (ds = 0; ds < dsGrps.size(); ++ds) {
                            sdidata = (DataSet)dsGrps.get(ds);
                            if (ds > 0) {
                                areahtml.append("<tr>");
                            }
                            for (s = 0; s < sdidata.getRowCount(); ++s) {
                                readOnly = false;
                                for (a = 0; a < datasetColumns.size(); ++a) {
                                    column = (String)datasetColumns.get(a);
                                    cellhtml = sdidata.getValue(s, column, "");
                                    areahtml.append("<td valign=top  nowrap class=\"value\">");
                                    areahtml.append(cellhtml);
                                    areahtml.append("</td>");
                                }
                                finished_sdi = false;
                                cancelled_sdi = false;
                                tooltip = new String();
                                statusColumn = attributesdcid.equalsIgnoreCase("DataSet") != false ? "s_datasetstatus" : (attributesdcid.equalsIgnoreCase("SDIWorkItem") != false ? "workitemstatus" : "primary_status");
                                status = sdidata.getValue(s, statusColumn);
                                cancelled_sdi = "Cancelled".equalsIgnoreCase(status) != false || "Cancel".equalsIgnoreCase(status) != false;
                                finished_sdi = Attributes.isSDIFinished(attributesdcid, sdidata, s, status);
                                tooltip = "Status is " + status;
                                if (protectCancelled && cancelled_sdi) {
                                    readOnly = true;
                                }
                                if (protectFinished && finished_sdi) {
                                    readOnly = true;
                                }
                                for (a = 0; a < attributeids.size(); ++a) {
                                    cellhtml = sdidata.getValue(s, attributeids.get(a), "");
                                    areahtml.append("<td nowrap class=\"value\" ");
                                    areahtml.append("valign=top");
                                    areahtml.append(" align=center ");
                                    areahtml.append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip, cancelSDIAttributeColor, finishedSDIAttributeColor));
                                    areahtml.append(">");
                                    if (!viewOnly && !readOnly && cellhtml.length() == 0) {
                                        attributeId = StringUtil.split(attributeids.get(a), ";")[0];
                                        attributedef = attributesdef.find("attributeid", attributeId);
                                        if (attributedef != null) {
                                            areahtml.append("<img style=\"cursor: pointer; \" width=\"10\" height=\"10\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\" " + tp.translate("Click to add attribute") + " '" + attributeId + "'\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + sdidata.getValue(s, "keyid1") + "','" + sdidata.getValue(s, "keyid2") + "','" + sdidata.getValue(s, "keyid3") + "','" + attributeId + "');>");
                                        } else {
                                            areahtml.append("<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" style=\"opacity:0.10;color:grey\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\"" + tp.translate("Attribute no longer defined in master list") + "\" imageid=\"NotAdd\" onClick=\"return false;\">");
                                        }
                                    }
                                    areahtml.append(cellhtml);
                                    areahtml.append("</td>");
                                }
                                if (!viewOnly) {
                                    areahtml.append("<td valign=top  align=center nowrap class=\"value\"");
                                    areahtml.append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip, cancelSDIAttributeColor, finishedSDIAttributeColor));
                                    areahtml.append(">");
                                    if (!readOnly) {
                                        areahtml.append("<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add.png\" title=\" " + tp.translate("Click to add new attribute") + ".\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + sdidata.getValue(s, "keyid1") + "','" + sdidata.getValue(s, "keyid2") + "','" + sdidata.getValue(s, "keyid3") + "');>");
                                    } else {
                                        areahtml.append("&nbsp;");
                                    }
                                    areahtml.append("</td>");
                                }
                                areahtml.append("</tr>");
                            }
                        }
                    }
                }
            }
        }
        areahtml.append("</tbody>");
        areahtml.append("</table>");
        if (!rendered) {
            areahtml.append("<div id=\"__").append(elementid).append("_norows\" style=\"display:").append("block").append(";\" >");
            noSDIMsg = config.getProperty("nosdiavailablemessage");
            areahtml.append(noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : tp.translate("No Attributes found"));
            areahtml.append("</div>");
        }
        return attributedata;
    }

    private static void updateInsertedRows(DataSet attributedata) {
        HashMap<String, String> insertmap = new HashMap<String, String>();
        insertmap.put("__rowstatus", "I");
        DataSet dsInserted = attributedata.getFilteredDataSet(insertmap);
        DataSet dsExisting = attributedata.getFilteredDataSet(insertmap, true);
        for (int i = 0; i < dsInserted.getRowCount(); ++i) {
            String sdcid = dsInserted.getValue(i, "sdcid");
            String keyid1 = dsInserted.getValue(i, "keyid1");
            String keyid2 = dsInserted.getValue(i, "keyid2");
            String keyid3 = dsInserted.getValue(i, "keyid3");
            HashMap<String, String> findAttribute = new HashMap<String, String>();
            findAttribute.put("sdcid", sdcid);
            findAttribute.put("keyid1", keyid1);
            findAttribute.put("keyid2", keyid2);
            findAttribute.put("keyid3", keyid3);
            int row = dsExisting.findRow(findAttribute);
            if (row <= -1) continue;
            String primary1 = dsExisting.getValue(row, "primary1");
            String primary2 = dsExisting.getValue(row, "primary2");
            String primary3 = dsExisting.getValue(row, "primary3");
            if (primary1.length() > 0) {
                dsInserted.setString(i, "primary1", primary1);
                dsInserted.setString(i, "primary2", primary2);
                dsInserted.setString(i, "primary3", primary3);
            }
            String[] updateColArray = dsExisting.getColumns();
            for (int col = 0; col < updateColArray.length; ++col) {
                if (!updateColArray[col].startsWith("primary_") && !updateColArray[col].startsWith("sdiworkitem_") && !updateColArray[col].startsWith("sdidata_")) continue;
                dsInserted.setString(i, updateColArray[col], dsExisting.getValue(row, updateColArray[col]));
            }
        }
    }

    private DataSet renderSingleSDI(StringBuilder areahtml, PropertyList sdc, DataSet attributedata, boolean grouped, int rowCount, boolean viewOnly, ArrayList<String> attributeids, String tableStyling) throws SapphireException {
        StringBuilder area = new StringBuilder();
        if ("Standard".equalsIgnoreCase(this.config.getProperty("attributemode", "worksheet")) && this.config.getProperty("sourcesdcid").length() > 0) {
            Attributes.renderStandardModeSDIAcross(area, sdc, attributedata, grouped, this.config, JSOBJECT, this.getElementId(), this.instructionOnlyCount, this.fullwidth, this, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), new Logger(this.logContext), viewOnly, null, null, null, this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId()), null, false, attributeids, tableStyling);
        } else {
            MaintAttribute.renderSingleSDIAttributes(area, sdc, attributedata, grouped, rowCount, this.config, tableStyling, "title", "value", JSOBJECT, this.getElementId(), this.instructionOnlyCount, this.fullwidth, this, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), this.getSapphireConnection(), new Logger(this.logContext));
        }
        areahtml.append((CharSequence)area);
        return attributedata;
    }

    static void getSortByString(PropertyListCollection sortBy, String dsName, TranslationProcessor tp, DataSet attributedata, StringBuffer sortByStr, StringBuffer sortByDataSet) throws SapphireException {
        String displayDSName;
        String string = "sdidata".equalsIgnoreCase(dsName) ? "DataSet" : (displayDSName = "sdiworkitem".equalsIgnoreCase(dsName) ? "SDIWorkItem" : "Primary");
        if (sortBy.size() > 0) {
            String colAlias = "";
            for (int g = 0; g < sortBy.size(); ++g) {
                PropertyList pl = sortBy.getPropertyList(g);
                String colId = pl.getProperty("columnid");
                if (colId.length() == 0) {
                    throw new SapphireException(tp.translate(displayDSName + " Sort By column not specified!"));
                }
                if (colId.contains(".")) {
                    String[] splitColumn = StringUtil.split(colId, ".");
                    String link = splitColumn[0];
                    String linkColumn = splitColumn[1];
                    colAlias = dsName + "_" + StringUtil.replaceAll(link, " ", "") + "_" + linkColumn;
                } else {
                    colAlias = dsName + "_" + colId;
                }
                if (!attributedata.isValidColumn(colAlias)) {
                    throw new SapphireException(tp.translate("Sort By column must be defined in the \"" + displayDSName + " Display Columns\" collection"));
                }
                String direction = pl.getProperty("direction");
                if (sortByStr.length() > 0) {
                    sortByStr.append(", ");
                }
                if (sortByDataSet.length() > 0) {
                    sortByDataSet.append(", ");
                }
                sortByStr.append(colAlias).append("D".equalsIgnoreCase(direction) ? " D" : "");
                sortByDataSet.append(colAlias).append("D".equalsIgnoreCase(direction) ? " D" : "");
            }
        }
    }

    public static int renderStandardModeSDIAcross(StringBuilder areahtml, PropertyList sdc, DataSet attributedata, boolean grouped, PropertyList config, String jsobject, String elementid, int instructOnly, boolean fullwidth, Attributes attributes, TranslationProcessor tp, QueryProcessor qp, ConnectionProcessor cp, Logger logger, boolean viewOnly, PageContext pageContext, SDITagInfo sdiInfo, M18NUtil m18nServer, ConnectionInfo connectionInfo, int[] selected, boolean multiSDI, ArrayList<String> attributeids, String tableStyling) throws SapphireException {
        String attributesdcid = sdc.getProperty("sdcid");
        int instructionOnlyCount = instructOnly;
        StringBuffer style = new StringBuffer();
        PropertyListCollection primaryCols = config.getCollectionNotNull("primarycolumns");
        PropertyListCollection sdiwiCols = config.getCollectionNotNull("sdiworkitemcolumns");
        PropertyListCollection datasetCols = config.getCollectionNotNull("sdidatacolumns");
        PropertyListCollection primarySortBy = config.getCollectionNotNull("sortby");
        PropertyListCollection wiSortBy = config.getCollectionNotNull("sdiworkitemsortby");
        PropertyListCollection dsSortBy = config.getCollectionNotNull("sdidatasortby");
        StringBuffer sortByStr = new StringBuffer();
        StringBuffer sortByPrimary = new StringBuffer();
        StringBuffer sortByWorkItem = new StringBuffer();
        StringBuffer sortByDataSet = new StringBuffer();
        ArrayList<String> ddComboValuesLst = new ArrayList<String>();
        Attributes.getSortByString(primarySortBy, "primary", tp, attributedata, sortByStr, sortByPrimary);
        sortByStr.append(sortByStr.length() > 0 ? "," : "").append("primary1, primary2, primary3");
        sortByPrimary.append(sortByPrimary.length() > 0 ? "," : "").append("primary1, primary2, primary3");
        if (attributedata.isValidColumn("sdiworkitem_workiteminstance")) {
            Attributes.getSortByString(wiSortBy, "sdiworkitem", tp, attributedata, sortByStr, sortByWorkItem);
            sortByStr.append(",sdiworkitem_usersequence");
            sortByWorkItem.append(sortByWorkItem.length() > 0 ? "," : "").append("sdiworkitem_usersequence");
        }
        if (attributedata.isValidColumn("sdidata_dataset")) {
            Attributes.getSortByString(dsSortBy, "sdidata", tp, attributedata, sortByStr, sortByDataSet);
            sortByStr.append(",sdidata_usersequence");
            sortByDataSet.append("sdidata_usersequence");
        }
        Attributes.updateInsertedRows(attributedata);
        sortByStr.append(sortByStr.length() == 0 ? "keyid1,keyid2,keyid3" : "");
        DataSet grid = new DataSet();
        attributedata.sort("sdcid,keyid1,keyid2,keyid3");
        ArrayList<DataSet> groups = attributedata.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
        for (int grp = 0; grp < groups.size(); ++grp) {
            DataSet sdiGrps = groups.get(grp);
            grid.copyRow(sdiGrps, 0, 1);
        }
        if (multiSDI && sortByStr.length() > 0) {
            grid.sort(sortByStr.toString());
        }
        PropertyListCollection attributesdef = sdc.getCollection("attributes");
        boolean allowRequired = sdc.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
        ArrayList<Object> multiSDISingleAttributeGroups = new ArrayList<DataSet>();
        attributedata.addColumn("_notexists", 0);
        if (grouped) {
            if (multiSDI) {
                attributedata.sort("__attributegroup, attributeid, attributeinstance");
                multiSDISingleAttributeGroups = attributedata.getGroupedDataSets("__attributegroup, attributeid, attributeinstance");
            } else {
                attributedata.sort("__attributegroup, usersequence");
                multiSDISingleAttributeGroups = attributedata.getGroupedDataSets("__attributegroup, usersequence");
            }
        } else if (multiSDI) {
            attributedata.sort("attributeid, attributeinstance");
            multiSDISingleAttributeGroups = attributedata.getGroupedDataSets("attributeid, attributeinstance");
        } else {
            attributedata.sort("usersequence");
            multiSDISingleAttributeGroups.add(attributedata);
        }
        if (multiSDI) {
            attributedata.sort("keyid1,keyid2,keyid3,attributeid,attributeinstance");
            MaintAttribute.renumberAttributeRows(attributedata, 1);
        }
        boolean rendered = false;
        TreeMap<String, StringBuffer> groupsofrows = new TreeMap<String, StringBuffer>(new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        if (config.getProperty("hidebordersinfoonly").equals("Y")) {
            boolean infoonly = true;
            for (int i = 0; i < attributedata.size() && infoonly; ++i) {
                infoonly = attributedata.getValue(i, "instructionflag").equals("O");
            }
            if (infoonly) {
                tableStyling = "";
            }
        }
        String finishedSDIAttributeColor = config.getProperty("highlightfinishedsdiattribute");
        String cancelSDIAttributeColor = config.getProperty("highlightcancelledsdiattribute");
        boolean protectFinished = "Y".equalsIgnoreCase(config.getProperty("protectfinishedsdiattribute"));
        boolean protectCancelled = "Y".equalsIgnoreCase(config.getProperty("protectcancelledsdiattribute"));
        boolean valuesonly = config.getProperty("valuesonly", "N").equalsIgnoreCase("Y");
        JSONObject validations = new JSONObject();
        boolean renderButtons = true;
        boolean origViewOnly = viewOnly;
        for (int m = 0; m < multiSDISingleAttributeGroups.size(); ++m) {
            DataSet singleattributedata = (DataSet)multiSDISingleAttributeGroups.get(m);
            String currentAttributeId = singleattributedata.getValue(0, "attributeid");
            String currentAttributeInstance = singleattributedata.getValue(0, "attributeinstance");
            if (multiSDI) {
                for (int g = 0; g < grid.getRowCount(); ++g) {
                    boolean attributeNotExists;
                    String sdcId = grid.getValue(g, "sdcid");
                    String keyid1 = grid.getValue(g, "keyid1");
                    String keyid2 = grid.getValue(g, "keyid2");
                    String keyid3 = grid.getValue(g, "keyid3");
                    HashMap<String, String> findSDIAttribute = new HashMap<String, String>();
                    findSDIAttribute.put("sdcid", sdcId);
                    findSDIAttribute.put("keyid1", keyid1);
                    findSDIAttribute.put("keyid2", keyid2);
                    findSDIAttribute.put("keyid3", keyid3);
                    boolean bl = attributeNotExists = singleattributedata.findRow(findSDIAttribute) < 0;
                    if (!attributeNotExists) continue;
                    singleattributedata.copyRow(grid, g, 1);
                    int r = singleattributedata.getRowCount() - 1;
                    singleattributedata.setString(r, "attributeid", currentAttributeId);
                    singleattributedata.setValue(r, "attributeinstance", currentAttributeInstance);
                    singleattributedata.setString(r, "_notexists", "Y");
                    singleattributedata.setString(r, "attributesourcetype", singleattributedata.getValue(0, "attributesourcetype"));
                    singleattributedata.setString(r, "__attributegroup", singleattributedata.getValue(0, "__attributegroup", DEFAULT_GROUP));
                    singleattributedata.setString(r, "mandatoryflag", singleattributedata.getValue(0, "mandatoryflag"));
                    singleattributedata.setString(r, "hiddenflag", singleattributedata.getValue(0, "hiddenflag"));
                    singleattributedata.setString(r, "sourcesdcid", singleattributedata.getValue(0, "sourcesdcid"));
                    singleattributedata.setString(r, "sourcekeyid1", singleattributedata.getValue(0, "sourcekeyid1"));
                    singleattributedata.setString(r, "sourcekeyid2", singleattributedata.getValue(0, "sourcekeyid2"));
                    singleattributedata.setString(r, "sourcekeyid3", singleattributedata.getValue(0, "sourcekeyid3"));
                    singleattributedata.setString(r, "sourcesdcid", singleattributedata.getValue(0, "sourcesdcid"));
                    singleattributedata.setString(r, "sourcesdcid", singleattributedata.getValue(0, "sourcesdcid"));
                    singleattributedata.setString(r, "updateableflag", singleattributedata.getValue(0, "updateableflag"));
                    singleattributedata.setString(r, COLUMN_VALIDFLAG, singleattributedata.getValue(0, COLUMN_VALIDFLAG));
                    PropertyList attributedef = (PropertyList)singleattributedata.getObject(0, "_attributedef");
                    singleattributedata.setObject(r, "_attributedef", attributedef);
                    singleattributedata.setString(r, "instructionflag", singleattributedata.getValue(0, "instructionflag"));
                }
                singleattributedata.sort(sortByStr.toString());
            }
            for (int i = 0; i < singleattributedata.getRowCount(); ++i) {
                boolean protectedSDI = false;
                renderButtons = true;
                DataSet dsTemp = new DataSet();
                if (multiSDI) {
                    HashMap<String, String> findMap2 = new HashMap<String, String>();
                    findMap2.put("__rowstatus", "D");
                    DataSet dsFindDelete = singleattributedata.getFilteredDataSet(findMap2);
                    if (dsFindDelete.getRowCount() != singleattributedata.getRowCount() && "D".equals(singleattributedata.getValue(i, "__rowstatus"))) {
                        findMap2.clear();
                        findMap2.put("__rowstatus", "I");
                        findMap2.put("sdcid", singleattributedata.getValue(i, "sdcid"));
                        findMap2.put("keyid1", singleattributedata.getValue(i, "keyid1"));
                        findMap2.put("keyid2", singleattributedata.getValue(i, "keyid2"));
                        findMap2.put("keyid3", singleattributedata.getValue(i, "keyid3"));
                        findMap2.put("attributeinstance", singleattributedata.getValue(i, "attributeinstance"));
                        int row = singleattributedata.findRow(findMap2);
                        if (row < 0) {
                            singleattributedata.setValue(i, "__rowstatus", DEFAULT_GROUP);
                            singleattributedata.setValue(i, "_notexists", "Y");
                            singleattributedata.setObject(i, "_attributedef", attributesdef.find("attributeid", currentAttributeId));
                        } else {
                            singleattributedata.setValue(i, "__rowstatus", "I");
                            singleattributedata.setValue(i, "_notexists", DEFAULT_GROUP);
                            singleattributedata.setObject(i, "_attributedef", attributesdef.find("attributeid", currentAttributeId));
                            dsTemp.copyRow(singleattributedata, row, 1);
                            singleattributedata.deleteRow(row);
                        }
                    }
                }
                boolean notexists = "Y".equals(singleattributedata.getValue(i, "_notexists"));
                boolean attributeNotInMasterList = false;
                BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(singleattributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef)));
                if (type != BaseSDIAttributeAction.AttributeType.linkdef) {
                    String rs = singleattributedata.getValue(i, "__rowstatus", "S");
                    boolean finished_sdi = false;
                    boolean cancelled_sdi = false;
                    String tooltip = new String();
                    String statusColumn = attributesdcid.equalsIgnoreCase("DataSet") ? "s_datasetstatus" : (attributesdcid.equalsIgnoreCase("SDIWorkItem") ? "workitemstatus" : "primary_status");
                    String status = singleattributedata.getValue(i, statusColumn);
                    cancelled_sdi = "Cancelled".equalsIgnoreCase(status) || "Cancel".equalsIgnoreCase(status);
                    tooltip = "Status is " + status;
                    finished_sdi = Attributes.isSDIFinished(attributesdcid, singleattributedata, i, status);
                    tooltip = "Status is " + status;
                    if (protectFinished && finished_sdi) {
                        protectedSDI = true;
                        renderButtons = false;
                    }
                    if (protectCancelled && cancelled_sdi) {
                        protectedSDI = true;
                        renderButtons = false;
                    }
                    if (protectedSDI) {
                        viewOnly = true;
                        HashMap<String, String> findSDI = new HashMap<String, String>();
                        findSDI.put("sdcid", singleattributedata.getValue(i, "sdcid"));
                        findSDI.put("keyid1", singleattributedata.getValue(i, "keyid1"));
                        findSDI.put("keyid2", singleattributedata.getValue(i, "keyid2"));
                        findSDI.put("keyid3", singleattributedata.getValue(i, "keyid3"));
                        int sdiRow = grid.findRow(findSDI);
                        if (sdiRow > -1) {
                            grid.setString(sdiRow, "_protected", "Y");
                        }
                    } else {
                        viewOnly = origViewOnly;
                    }
                    if (!rs.equalsIgnoreCase("D")) {
                        String siid;
                        boolean isselected;
                        String defaultEditorStyle;
                        String tip;
                        String title;
                        String groupid;
                        String def_instructionflag;
                        int row = Integer.parseInt(singleattributedata.getString(i, "__rowid", i + ""));
                        boolean hidden = singleattributedata.getValue(i, "hiddenflag", "N").equalsIgnoreCase("Y");
                        boolean mandatory = allowRequired && singleattributedata.getValue(i, "mandatoryflag", "N").equalsIgnoreCase("Y");
                        String rowStatus = singleattributedata.getValue(i, "__rowstatus");
                        String attributeid = singleattributedata.getValue(i, "attributeid", "");
                        BaseSDIAttributeAction.AttributeType attributetype = BaseSDIAttributeAction.getAttributeTypeFromString(singleattributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
                        PropertyList attributedef = (PropertyList)singleattributedata.getObject(i, "_attributedef");
                        singleattributedata.setObject(i, "_attributedef", null);
                        boolean readOnly = false;
                        String current_instructionflag = singleattributedata.getValue(i, "instructionflag", "N");
                        if (attributedef != null) {
                            def_instructionflag = attributedef.getProperty("instructionflag", attributeid);
                            groupid = grouped ? singleattributedata.getValue(i, "__attributegroup", DEFAULT_GROUP) : DEFAULT_GROUP;
                            title = tp.translate(attributedef.getProperty("attributetitle", ""));
                            tip = tp.translate(attributedef.getProperty("helptext", ""));
                            if (tip.length() == 0) {
                                tip = title.length() > 0 ? title : attributeid;
                            }
                            tip = tip + " (" + attributeid + " ";
                            switch (attributetype) {
                                case adhoc: {
                                    tip = tip + " " + tp.translate("adhoc added");
                                    break;
                                }
                                case link: {
                                    tip = tip + " " + tp.translate("from") + " " + singleattributedata.getValue(i, "sourcesdcid", "");
                                    break;
                                }
                                case sdc: {
                                    tip = tip + " " + tp.translate("auto added");
                                }
                            }
                            tip = tip + ")";
                            defaultEditorStyle = attributedef.getProperty("editorstyleid", "");
                            if (!viewOnly) {
                                defaultEditorStyle = attributedef.getProperty("editorstyleid", current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : "");
                            }
                        } else if (current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O")) {
                            def_instructionflag = "N";
                            groupid = DEFAULT_GROUP;
                            tip = "";
                            title = "";
                            defaultEditorStyle = "";
                            if (!viewOnly) {
                                defaultEditorStyle = current_instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : "";
                            }
                        } else {
                            if (current_instructionflag.equalsIgnoreCase("R")) {
                                title = "";
                                tip = "";
                                current_instructionflag = "R";
                            } else if (current_instructionflag.equalsIgnoreCase("M")) {
                                title = attributeid;
                                tip = attributeid;
                                current_instructionflag = "M";
                            } else {
                                readOnly = true;
                                title = attributeid;
                                tip = "Attribute no longer defined in master list.";
                                current_instructionflag = "N";
                                attributeNotInMasterList = true;
                            }
                            def_instructionflag = "N";
                            defaultEditorStyle = "";
                            groupid = DEFAULT_GROUP;
                        }
                        if (!groupsofrows.containsKey(groupid)) {
                            groupsofrows.put(groupid, new StringBuffer());
                        }
                        StringBuffer currentRowHTML = (StringBuffer)groupsofrows.get(groupid);
                        String prefix = "" + elementid + "_" + StringUtil.replaceAll(groupid, " ", "_") + "_";
                        String rowId = prefix + "row";
                        if (i == 0) {
                            currentRowHTML.append("<tr id=\"" + rowId + "\"").append(hidden ? " style=\"display:none;\"" : "").append(">");
                        }
                        boolean bl = isselected = selected != null && Arrays.binarySearch(selected, row) > -1;
                        if (!(viewOnly || valuesonly || multiSDI)) {
                            currentRowHTML.append("<td class=\"maintform_fieldtitle\">");
                            currentRowHTML.append("<input type=\"checkbox\" name=\"").append(elementid).append("_selector\" id=\"__").append(elementid).append("_").append(row).append("\" onclick=\"" + jsobject + ".select(event,'").append(elementid).append("',").append(row).append(")\"").append(isselected ? " checked" : "").append(" ").append(hidden ? " disabled" : "").append(">");
                            currentRowHTML.append("</td>");
                        }
                        String instructiontext = "";
                        if (current_instructionflag.equalsIgnoreCase("R") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("A")) {
                            String k3;
                            StringBuffer sql = new StringBuffer();
                            SafeSQL safeSQL = new SafeSQL();
                            sql.append("SELECT instructiontext FROM sdiattribute ");
                            sql.append(" WHERE ");
                            sql.append("attributeid = ").append(safeSQL.addVar(attributeid));
                            sql.append(" AND ");
                            sql.append("sdcid = ").append(safeSQL.addVar(singleattributedata.getValue(i, "sourcesdcid", "")));
                            sql.append(" AND ");
                            sql.append(" keyid1 = ").append(safeSQL.addVar(singleattributedata.getValue(i, "sourcekeyid1", "")));
                            String k2 = singleattributedata.getValue(i, "sourcekeyid2", "");
                            if (k2.length() > 0) {
                                sql.append(" AND ");
                                sql.append("keyid2 = ").append(safeSQL.addVar(k2));
                            }
                            if ((k3 = singleattributedata.getValue(i, "sourcekeyid3", "")).length() > 0) {
                                sql.append(" AND ");
                                sql.append(" keyid3 = ").append(safeSQL.addVar(k3));
                            }
                            sql.append(" AND ");
                            sql.append(" attributeinstance = ").append(safeSQL.addVar(singleattributedata.getValue(i, "sourceattributeinstance", "")));
                            DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                            if (ds != null && ds.size() > 0) {
                                instructiontext = ds.getValue(0, "instructiontext", "");
                            } else if (!"I".equalsIgnoreCase(rowStatus)) {
                                logger.warn("Could not obtain source of adhoc attribute.");
                            }
                        } else if (def_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("O")) {
                            instructiontext = attributedef.getProperty("instructiontext");
                        }
                        if (!viewOnly && (multiSDI && i == 0 || !multiSDI)) {
                            currentRowHTML.append("<td id=\"").append(elementid).append(row).append("_").append(attributeid).append("_titlecell\" title=\"").append(tip).append("\"  style=\"border-left-color:#CCCCCC;color:").append(readOnly ? "red" : "black").append(";\"");
                        }
                        boolean showtitle = true;
                        if (multiSDI && i > 0) {
                            showtitle = false;
                        }
                        if (title.length() == 0 && multiSDI) {
                            title = attributeid;
                        }
                        if (viewOnly) {
                            if (title.length() == 0) {
                                if (multiSDI) {
                                    title = attributeid;
                                } else if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                                    showtitle = false;
                                } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                                    showtitle = false;
                                    ++instructionOnlyCount;
                                } else if (attributedef == null) {
                                    showtitle = false;
                                } else {
                                    title = attributeid;
                                }
                            }
                            if (def_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("O")) {
                                if (multiSDI) {
                                    if (i == 0) {
                                        int instance;
                                        siid = elementid + row + "_title";
                                        currentRowHTML.append("<td class=\"value\" colspan=\"1\">");
                                        currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;font-weight:bold;\">");
                                        String attribTitle = attributeid;
                                        if (attributedef != null) {
                                            attribTitle = tp.translate(attributedef.getProperty("attributetitle", attributeid));
                                        }
                                        currentRowHTML.append(SafeHTML.encodeForHTMLAttribute(attribTitle));
                                        try {
                                            instance = Integer.parseInt(singleattributedata.getValue(i, "attributeinstance"));
                                        }
                                        catch (Exception e) {
                                            instance = -1;
                                        }
                                        if (instance > 1) {
                                            currentRowHTML.append(" (").append(instance).append(")");
                                        }
                                        currentRowHTML.append("</div></td>");
                                    }
                                    currentRowHTML.append("<td class=\"value\" colspan=\"1\"");
                                    currentRowHTML.append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip.toString(), cancelSDIAttributeColor, finishedSDIAttributeColor));
                                    currentRowHTML.append(">");
                                    showtitle = false;
                                } else {
                                    currentRowHTML.append("<td class=\"value\" colspan=\"2\"");
                                    currentRowHTML.append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip.toString(), cancelSDIAttributeColor, finishedSDIAttributeColor));
                                    currentRowHTML.append(">");
                                }
                            } else if (multiSDI) {
                                if (showtitle || def_instructionflag.equalsIgnoreCase("A") && multiSDI && i == 0) {
                                    currentRowHTML.append("<td class=\"value\">");
                                }
                            } else if (showtitle || def_instructionflag.equalsIgnoreCase("A")) {
                                currentRowHTML.append("<td class=\"value\">");
                            }
                            if (showtitle) {
                                int instance;
                                siid = elementid + row + "_title";
                                currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;font-weight:bold;\">");
                                currentRowHTML.append(SafeHTML.encodeForHTMLAttribute(title));
                                try {
                                    instance = Integer.parseInt(singleattributedata.getValue(i, "attributeinstance"));
                                }
                                catch (Exception e) {
                                    instance = -1;
                                }
                                if (instance > 1) {
                                    currentRowHTML.append(" (").append(instance).append(")");
                                }
                                currentRowHTML.append("</div>");
                            }
                            if (!current_instructionflag.equalsIgnoreCase("O") && !def_instructionflag.equalsIgnoreCase("O") && multiSDI && i > 0) {
                                instructiontext = "";
                            }
                            if (instructiontext.length() > 0) {
                                siid = elementid + row + "_instructiontext";
                                currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;\">");
                                if (!current_instructionflag.equalsIgnoreCase("O") && !def_instructionflag.equalsIgnoreCase("O")) {
                                    currentRowHTML.append(notexists ? "" : instructiontext);
                                } else {
                                    currentRowHTML.append(multiSDI ? (notexists ? "" : "<i>Information</i>") : instructiontext);
                                }
                                currentRowHTML.append("</div>");
                            }
                        } else {
                            String addAttribute;
                            if (multiSDI && i == 0 || !multiSDI) {
                                if (title.length() == 0) {
                                    if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                                        currentRowHTML.append(multiSDI ? " colspan=\"1\"" : " colspan=\"2\"");
                                        showtitle = false;
                                    } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                                        currentRowHTML.append(multiSDI ? " colspan=\"1\"" : " colspan=\"3\"");
                                        boolean bl2 = showtitle = i == 0 && multiSDI;
                                        if (multiSDI && i == 0 && attributedef != null) {
                                            title = tp.translate(attributedef.getProperty("attributetitle", attributeid));
                                        }
                                    } else if (current_instructionflag.equalsIgnoreCase("M") || def_instructionflag.equalsIgnoreCase("M")) {
                                        if (multiSDI) {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"1\"");
                                        } else {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"2\"");
                                        }
                                        title = attributeid;
                                    } else if (attributedef == null) {
                                        if (multiSDI) {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " colspan=\"1\"" : " colspan=\"1\"");
                                        } else {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " colspan=\"2\"" : " colspan=\"3\"");
                                        }
                                        showtitle = false;
                                    } else {
                                        if (multiSDI) {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"1\"");
                                        } else {
                                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"2\"");
                                        }
                                        title = attributeid;
                                    }
                                    currentRowHTML.append(" class=\"maintform_field\"");
                                } else if (instructiontext.length() == 0) {
                                    currentRowHTML.append(" class=\"maintform_fieldtitle\"");
                                    currentRowHTML.append(multiSDI ? " colspan=\"1\"" : " colspan=\"2\"");
                                } else {
                                    currentRowHTML.append(" class=\"maintform_fieldtitle\"");
                                }
                                currentRowHTML.append(">");
                                if (showtitle) {
                                    int instance;
                                    currentRowHTML.append(SafeHTML.encodeForHTMLAttribute(title));
                                    try {
                                        instance = Integer.parseInt(singleattributedata.getValue(i, "attributeinstance"));
                                    }
                                    catch (Exception e) {
                                        instance = -1;
                                    }
                                    if (instance > 1) {
                                        currentRowHTML.append(" (").append(instance).append(")");
                                    }
                                    if (!multiSDI || instructiontext.length() <= 0 || current_instructionflag.equalsIgnoreCase("O") || !def_instructionflag.equalsIgnoreCase("O")) {
                                        // empty if block
                                    }
                                    currentRowHTML.append("</td>");
                                }
                            }
                            if (showtitle && instructiontext.length() > 0) {
                                if (def_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("O")) {
                                    if (!multiSDI) {
                                        currentRowHTML.append("<td class=\"maintform_field\" colspan=\"2\">");
                                    }
                                } else if (instructiontext.length() == 0) {
                                    if (!multiSDI || !current_instructionflag.equalsIgnoreCase("A") && !def_instructionflag.equalsIgnoreCase("A")) {
                                        currentRowHTML.append("<td class=\"maintform_field\" >");
                                    }
                                } else if (!(multiSDI && (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("R")))) {
                                    currentRowHTML.append("<td class=\"maintform_field\">");
                                }
                            }
                            if (instructiontext.length() > 0) {
                                if (!multiSDI) {
                                    siid = elementid + row + "_instructiontext";
                                    currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;\">");
                                    currentRowHTML.append(instructiontext);
                                    currentRowHTML.append("</div>");
                                } else if (!(current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("R"))) {
                                    addAttribute = "";
                                    if (attributeNotInMasterList) {
                                        addAttribute = "<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" style=\"opacity:0.10;color:grey\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\"" + tp.translate("Attribute no longer defined in master list") + "\" imageid=\"NotAdd\" onClick=\"return false;\">";
                                    } else if (attributedef != null) {
                                        addAttribute = "<img style=\"cursor: pointer; \" width=\"10\" height=\"10\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\" " + tp.translate("Click to add attribute") + " '" + currentAttributeId + "'\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + singleattributedata.getValue(i, "keyid1") + "','" + singleattributedata.getValue(i, "keyid2") + "','" + singleattributedata.getValue(i, "keyid3") + "','" + currentAttributeId + "');>";
                                    }
                                    currentRowHTML.append("<td").append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip.toString(), cancelSDIAttributeColor, finishedSDIAttributeColor)).append(">").append(notexists ? addAttribute : "<i>Information</i>").append("</td>");
                                }
                            } else if (multiSDI && def_instructionflag.equalsIgnoreCase("O")) {
                                addAttribute = "";
                                if (attributeNotInMasterList) {
                                    addAttribute = "<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" style=\"opacity:0.10;color:grey\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\"" + tp.translate("Attribute no longer defined in master list") + "\" imageid=\"NotAdd\" onClick=\"return false;\">";
                                } else if (attributedef != null) {
                                    addAttribute = "<img style=\"cursor: pointer; \" width=\"10\" height=\"10\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\" " + tp.translate("Click to add attribute") + " '" + currentAttributeId + "'\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + singleattributedata.getValue(i, "keyid1") + "','" + singleattributedata.getValue(i, "keyid2") + "','" + singleattributedata.getValue(i, "keyid3") + "','" + currentAttributeId + "');>";
                                }
                                currentRowHTML.append("<td").append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip.toString(), cancelSDIAttributeColor, finishedSDIAttributeColor)).append(">").append(notexists ? addAttribute : "").append("</td>");
                            }
                        }
                        if (!def_instructionflag.equalsIgnoreCase("O") && !current_instructionflag.equalsIgnoreCase("O")) {
                            if (instructiontext.length() > 0) {
                                currentRowHTML.append("</td>");
                            }
                            String editorstyle = current_instructionflag.equals("A") ? "Yes No Checkbox" : singleattributedata.getValue(i, "editorstyleid", defaultEditorStyle);
                            StringBuffer toadd = new StringBuffer();
                            boolean valueExists = false;
                            if (readOnly || viewOnly) {
                                PropertyList editorstyleprops;
                                String sdcid = singleattributedata.getValue(row, "editsdcid", "");
                                String reftypeid = singleattributedata.getValue(row, "editreftypeid", "");
                                try {
                                    editorstyleprops = editorstyle.length() > 0 ? EditorStyleField.getEditorStyleProperties(editorstyle, sdcid, reftypeid, cp.getSapphireConnection(), qp) : null;
                                }
                                catch (Exception e) {
                                    editorstyleprops = null;
                                }
                                String v = MaintAttribute.getAttributeValue(m18nServer, connectionInfo, singleattributedata, i, false, false);
                                valueExists = v != null && v.length() > 0;
                                toadd.append("<div class=\"value_field\"").append(v.length() > 0 ? "" : " style=\"font-style: italic;\"").append(">");
                                if (notexists) {
                                    toadd.append("&nbsp;");
                                } else if (editorstyleprops != null && editorstyleprops.getProperty("mode").equalsIgnoreCase("checkbox")) {
                                    toadd.append("<input type=\"checkbox\" ").append(v.equalsIgnoreCase("Y") ? " checked" : "").append(" onclick=\"return false;\">");
                                } else {
                                    toadd.append(v.length() > 0 ? SafeHTML.encodeForHTMLAttribute(v) : tp.translate("Undefined"));
                                }
                                toadd.append("</div>");
                            } else {
                                try {
                                    boolean updateable = singleattributedata.getValue(i, "updateableflag", "Y").equalsIgnoreCase("Y");
                                    String onChangeEvent = jsobject + ".change(event,'" + elementid + "'," + row + ")";
                                    EditorStyleField esf = Attributes.getAttributeField(pageContext, sdiInfo, m18nServer, connectionInfo, elementid, editorstyle, attributeid, title, i, row, singleattributedata, false, hidden, updateable, viewOnly || readOnly, onChangeEvent);
                                    String value = esf.getFieldValue();
                                    boolean bl3 = valueExists = value.length() > 0;
                                    if (singleattributedata.getValue(i, COLUMN_VALIDFLAG, "Y").equalsIgnoreCase("N")) {
                                        esf.setColumnProperty("class", "validationfail");
                                    }
                                    String validation = esf.getColumnProperty("validation");
                                    if (mandatory) {
                                        validation = validation.length() > 0 ? "Mandatory;" + validation : "Mandatory";
                                        esf.setColumnProperty("validation", validation);
                                    }
                                    if (validation.length() > 0 && !hidden) {
                                        JSONObject valid = new JSONObject();
                                        valid.put("text", validation);
                                        valid.put("row", row);
                                        validations.put(elementid + row + "_" + attributeid, valid);
                                    }
                                    String esfHtml = esf.getHtml();
                                    String addAttribute = "";
                                    if (pageContext.getAttribute("dd_dropdownvalues") != null) {
                                        String comboValue = pageContext.getAttribute("dd_dropdownvalues").toString();
                                        String[] comboValueArray = StringUtil.split(comboValue, ";");
                                        for (int a = 0; a < comboValueArray.length; ++a) {
                                            if (comboValueArray[a].length() <= 0 || ddComboValuesLst.contains(comboValueArray[a])) continue;
                                            ddComboValuesLst.add(comboValueArray[a]);
                                        }
                                    }
                                    if (attributeNotInMasterList) {
                                        addAttribute = "<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" style=\"opacity:0.10;color:grey\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\"" + tp.translate("Attribute no longer defined in master list") + "\" imageid=\"NotAdd\" onClick=\"return false;\">";
                                    } else if (attributedef != null) {
                                        addAttribute = "<img style=\"cursor: pointer; \" width=\"10\" height=\"10\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add2.png\" title=\" " + tp.translate("Click to add attribute") + " '" + currentAttributeId + "'\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + singleattributedata.getValue(i, "primary1") + "','" + singleattributedata.getValue(i, "primary2") + "','" + singleattributedata.getValue(i, "primary3") + "','" + currentAttributeId + "');>";
                                    }
                                    toadd.append(notexists ? addAttribute : esfHtml);
                                }
                                catch (Exception e) {
                                    areahtml.append("<td colspan=7 class=\"gridmaint_field\">");
                                    Attributes.renderError(areahtml, tp.translate("Could not render editor style. Error:") + e.getMessage());
                                    areahtml.append("</td>");
                                }
                            }
                            if (multiSDI) {
                                currentRowHTML.append("<td nowrap valign=top").append(instructiontext.length() == 0 ? " colspan=1" : "");
                            } else {
                                currentRowHTML.append("<td nowrap valign=top").append(instructiontext.length() == 0 ? " colspan=2" : "");
                            }
                            currentRowHTML.append(Attributes.getAttributeCellHtmlStyle(cancelled_sdi, finished_sdi, tooltip.toString(), cancelSDIAttributeColor, finishedSDIAttributeColor)).append(">");
                            if (instructiontext.length() > 0) {
                                // empty if block
                            }
                            currentRowHTML.append(toadd);
                            if (mandatory && !valueExists) {
                                currentRowHTML.append("<span title=\"").append(tp.translate("Required")).append("\">(R)</span>");
                            }
                            if (!notexists && multiSDI) {
                                String deleteAttributeBehavior = config.getProperty("deleteattributebehavior", "deletenewattributes");
                                String flag = "N";
                                String columnID = "";
                                if ("deleteanyattributes".equals(deleteAttributeBehavior)) {
                                    String dataType = singleattributedata.getValue(i, "datatype", "S");
                                    columnID = "D".equals(dataType) ? "datevalue" : ("C".equals(dataType) ? "clobvalue" : ("N".equals(dataType) ? "numericvalue" : "textvalue"));
                                    if ("I".equals(singleattributedata.getValue(i, "__rowstatus"))) {
                                        flag = "Y";
                                    } else if (singleattributedata.getValue(i, columnID) == null || "".equals(singleattributedata.getValue(i, columnID))) {
                                        if (!"OldValNotBlank".equals(singleattributedata.getValue(i, "oldvalue", ""))) {
                                            flag = "Y";
                                        }
                                    } else if (singleattributedata.isValidColumn("oldvalue") && "OldValBlank".equals(singleattributedata.getValue(i, "oldvalue", ""))) {
                                        flag = "Y";
                                    }
                                }
                                if (!"donotdelete".equals(deleteAttributeBehavior) && ("deletenewattributes".equals(deleteAttributeBehavior) && singleattributedata.getValue(i, "__rowstatus").equals("I") || "deleteanyattributes".equals(deleteAttributeBehavior) && "Y".equals(flag))) {
                                    currentRowHTML.append("<a style=\"width:100%;height:100%;cursor:pointer;\" align=\"center\" onclick=\"").append(jsobject).append(".removeMSA('").append(elementid).append("','").append(row).append("')").append("\">");
                                    Image image = new Image(pageContext);
                                    image.setImageId("Delete");
                                    image.setStyle("cursor:pointer");
                                    image.setDimensions(10, 10);
                                    image.setTitle("Click to remove attribute.");
                                    currentRowHTML.append(image.getHtml());
                                }
                            }
                            currentRowHTML.append("</td>");
                        } else {
                            currentRowHTML.append("</td>");
                        }
                        if (multiSDI) {
                            if (i == singleattributedata.getRowCount() - 1) {
                                currentRowHTML.append("</tr>");
                            }
                        } else {
                            currentRowHTML.append("</tr>");
                        }
                        if (!rendered) {
                            rendered = true;
                        }
                    }
                }
                if (!multiSDI) continue;
                if (DEFAULT_GROUP.equals(singleattributedata.getValue(i, "__rowstatus")) && "Y".equals(singleattributedata.getValue(i, "_notexists"))) {
                    singleattributedata.setValue(i, "__rowstatus", "D");
                    singleattributedata.setValue(i, "_notexists", "");
                    continue;
                }
                if (!"I".equals(singleattributedata.getValue(i, "__rowstatus")) || !DEFAULT_GROUP.equals(singleattributedata.getValue(i, "_notexists"))) continue;
                singleattributedata.setValue(i, "__rowstatus", "D");
                singleattributedata.setValue(i, "_notexists", "");
            }
        }
        areahtml.append("<table class=\"" + tableStyling + "\" style=\"border-spacing: 0px\" " + (fullwidth ? "width=\"100%\"" : "") + " border=1 bordercolor='#92a8d1'>");
        areahtml.append("<tbody >");
        if (primaryCols.size() > 0 || sdiwiCols.size() > 0 || datasetCols.size() > 0) {
            areahtml.append(Attributes.getColumnHtml(attributedata, null, null, null, primaryCols, sdiwiCols, datasetCols, origViewOnly, valuesonly, multiSDI, grid));
        }
        for (Map.Entry groupRowEntry : groupsofrows.entrySet()) {
            String prefix = "" + elementid + "_" + StringUtil.replaceAll(groupRowEntry.getKey(), " ", "_") + "_";
            String rowId = prefix + "row";
            String divId = prefix + "div";
            String grouptitle = groupRowEntry.getKey();
            boolean def = grouptitle.equalsIgnoreCase(DEFAULT_GROUP);
            if (!def || groupsofrows.size() > 1) {
                boolean collapseGroups = false;
                if (def) {
                    grouptitle = tp.translate("No Grouping");
                }
                areahtml.append("<tr>");
                String click = "attributesEditor.toggleGroup('" + elementid + "','" + prefix + "',this)";
                int colspan = 2;
                if (multiSDI) {
                    colspan += grid.getRowCount();
                }
                if (!origViewOnly) {
                    colspan = multiSDI ? ++colspan : (valuesonly ? 3 : 4);
                }
                areahtml.append("<th class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\" colspan=" + colspan + " align=left><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\">");
                areahtml.append("&nbsp;").append(SafeHTML.encodeForHTMLAttribute(grouptitle)).append("</th>");
                areahtml.append("</tr>");
                style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
            }
            areahtml.append((StringBuffer)groupRowEntry.getValue());
        }
        if (multiSDI && !origViewOnly && rendered) {
            areahtml.append("<tr><th class=\"" + tableStyling + " nowrap\">").append(tp.translate("Add New")).append("</td>");
            for (int g = 0; g < grid.getRowCount(); ++g) {
                String keyid1 = grid.getValue(g, "keyid1");
                String keyid2 = grid.getValue(g, "keyid2");
                String keyid3 = grid.getValue(g, "keyid3");
                if (!"Y".equals(grid.getValue(g, "_protected"))) {
                    areahtml.append("<td valign=center><div>&nbsp;<img style=\"cursor: pointer; \" width=\"12\" height=\"12\" border=\"0\" src=\"WEB-CORE/imageref/basic_application_icons/toolbar/others/16/add.png\" title=\"" + tp.translate("Click to add new attribute") + ".\" imageid=\"Add\" onClick=attributesEditor.add('" + elementid + "','" + attributesdcid + "','" + keyid1 + "','" + keyid2 + "','" + keyid3 + "');>").append("</div></td>");
                    continue;
                }
                areahtml.append("<td>&nbsp;</td>");
            }
            areahtml.append("</tr>");
        }
        areahtml.append("</tbody>");
        areahtml.append("</table>");
        areahtml.append("</table></td></tr>");
        if (!rendered) {
            if (attributes != null) {
                attributes.setAvailability("NoData");
            }
            areahtml.append("<div id=\"__").append(elementid).append("_norows\" style=\"display:").append("block").append(";\" >");
            String noSDIMsg = config.getProperty("nosdiavailablemessage");
            areahtml.append(noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : tp.translate("No Attributes found"));
            areahtml.append("</div>");
        }
        if (ddComboValuesLst.size() > 0) {
            areahtml.append("\n<script>");
            Iterator itr = ddComboValuesLst.iterator();
            while (itr.hasNext()) {
                areahtml.append("\n" + itr.next() + ";");
            }
            areahtml.append("\n</script>\n");
        }
        if (renderButtons && !multiSDI && !origViewOnly && !valuesonly && pageContext != null) {
            Attributes.renderButtons(areahtml, "adhoc", origViewOnly || valuesonly, false, pageContext, jsobject, elementid);
        }
        areahtml.append("\n<style id=\"__").append(elementid).append("_style\">\n");
        areahtml.append(style);
        areahtml.append("</style>");
        return instructionOnlyCount;
    }

    public static void renderStandardModeEditorHtml(StringBuilder html, PropertyList sdc, DataSet sdiattribute, PropertyList config, String jsobject, String elementid, int instructOnly, boolean fullwidth, Attributes attributes, TranslationProcessor tp, QueryProcessor qp, ConnectionProcessor cp, Logger logger, boolean viewOnly, PageContext pageContext, SDITagInfo sdiInfo, M18NUtil m18nServer, ConnectionInfo connectionInfo, SDCProcessor sdcp, int[] selected, String tableStyling) throws SapphireException {
        if (sdiattribute != null) {
            int instructionOnly = 0;
            String attributesdcid = config.getProperty("sourcesdcid");
            if (!sdiattribute.isValidColumn("__attributegroup")) {
                sdiattribute.addColumn("__attributegroup", 0);
            }
            if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
                PropertyListCollection attributesdef = sdc.getCollection("attributes");
                if (attributesdef != null) {
                    if (attributesdef.size() == 0) {
                        Logger.logWarn("No attributes have been defined for SDC " + sdc.getProperty("sdcid") + " in master list");
                    }
                    boolean grouped = false;
                    int rowcount = 0;
                    sdiattribute.addColumn("_attributedef", -1);
                    String c1 = "";
                    String c2 = "";
                    String c3 = "";
                    boolean multiplePrimary = false;
                    ArrayList<String> attributeids = new ArrayList<String>();
                    DataSet attributeidset = new DataSet();
                    attributeidset.addColumn("attributeid", 0);
                    attributeidset.addColumn("attributeinstance", 1);
                    for (int i = 0; i < sdiattribute.getRowCount(); ++i) {
                        String rs;
                        BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(sdiattribute.getValue(i, "attributesourcetype"));
                        if (type == BaseSDIAttributeAction.AttributeType.linkdef || (rs = sdiattribute.getValue(i, "__rowstatus", "S")).equalsIgnoreCase("D")) continue;
                        String attributeid = sdiattribute.getValue(i, "attributeid", "");
                        String attributeinstance = sdiattribute.getValue(i, "attributeinstance", "");
                        if (OpalUtil.isNotEmpty(attributeid)) {
                            String key = attributeid + ";" + attributeinstance;
                            if (attributeids.size() == 0 || !attributeids.contains(key)) {
                                attributeids.add(key);
                                int r = attributeidset.addRow();
                                attributeidset.setString(r, "attributeid", attributeid);
                                try {
                                    attributeidset.setNumber(r, "attributeinstance", Integer.parseInt(attributeinstance));
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                        String k1 = sdiattribute.getValue(i, "keyid1");
                        String k2 = sdiattribute.getValue(i, "keyid2");
                        String k3 = sdiattribute.getValue(i, "keyid3");
                        if (!(c1.length() == 0 || c1.equals(k1) && c2.equals(k2) && c3.equals(k3))) {
                            multiplePrimary = true;
                        }
                        c1 = k1;
                        c2 = k2;
                        c3 = k3;
                        ++rowcount;
                        PropertyList attributedef = attributesdef.find("attributeid", attributeid);
                        sdiattribute.setObject(i, "_attributedef", attributedef);
                        if (attributedef != null) {
                            if (!sdiattribute.isValidColumn("__attributegroup")) continue;
                            String groupid = attributedef.getProperty("attributegroup", "");
                            if (groupid.length() > 0) {
                                grouped = true;
                                sdiattribute.setValue(i, "__attributegroup", groupid);
                                continue;
                            }
                            sdiattribute.setValue(i, "__attributegroup", "");
                            continue;
                        }
                        sdiattribute.setValue(i, "__attributegroup", "");
                    }
                    String gridLayout = config.getProperty("gridlayout", multiplePrimary ? "Attributes Across" : "SDI Across");
                    if (multiplePrimary || !"SDI Across".equals(gridLayout)) {
                        attributeidset.sort("attributeid, attributeinstance");
                        attributeids = new ArrayList();
                        for (int i = 0; i < attributeidset.getRowCount(); ++i) {
                            String attributeid = attributeidset.getValue(i, "attributeid", "");
                            String attributeinstance = attributeidset.getValue(i, "attributeinstance", "");
                            String key = attributeid + ";" + attributeinstance;
                            attributeids.add(key);
                        }
                    }
                    if (multiplePrimary) {
                        if ("SDI Across".equals(gridLayout)) {
                            Attributes.renderStandardModeSDIAcross(html, sdc, sdiattribute, grouped, config, jsobject, elementid, instructionOnly, fullwidth, attributes, tp, qp, cp, logger, viewOnly, pageContext, sdiInfo, m18nServer, connectionInfo, selected, true, attributeids, tableStyling);
                        } else {
                            Attributes.renderStandardModeAttributeAcross(html, sdc, sdiattribute, rowcount, attributeids, config, jsobject, elementid, instructionOnly, fullwidth, attributes, tp, qp, cp, logger, viewOnly, pageContext, sdiInfo, m18nServer, connectionInfo, sdcp, false, tableStyling);
                        }
                    } else if ("SDI Across".equals(gridLayout)) {
                        Attributes.renderStandardModeSDIAcross(html, sdc, sdiattribute, grouped, config, jsobject, elementid, instructionOnly, fullwidth, attributes, tp, qp, cp, logger, viewOnly, pageContext, sdiInfo, m18nServer, connectionInfo, selected, false, attributeids, tableStyling);
                    } else {
                        Attributes.renderStandardModeAttributeAcross(html, sdc, sdiattribute, rowcount, attributeids, config, jsobject, elementid, instructionOnly, fullwidth, attributes, tp, qp, cp, logger, viewOnly, pageContext, sdiInfo, m18nServer, connectionInfo, sdcp, true, tableStyling);
                    }
                } else {
                    html.append("No attributes defined for SDC " + attributesdcid + ".");
                }
            } else {
                html.append("Attributes not switched on for SDC " + attributesdcid + ".");
            }
        }
    }

    private static String getAttributeCellHtmlStyle(boolean cancelled_sdi, boolean finished_sdi, String tooltip, String cancelSDIAttributeColor, String finishedSDIAttributeColor) {
        StringBuffer html = new StringBuffer();
        if (cancelled_sdi && cancelSDIAttributeColor.length() > 0) {
            html.append(" title = \"").append(tooltip).append("\" style=\"background-color:").append(cancelSDIAttributeColor).append(";border: 1px solid darkgray;padding: 4px;\"");
        } else if (finished_sdi && finishedSDIAttributeColor.length() > 0) {
            html.append(" title = \"").append(tooltip).append("\" style=\"background-color:").append(finishedSDIAttributeColor).append(";border: 1px solid darkgray;padding: 4px;\"");
        } else {
            html.append(" title = \"").append(tooltip).append("\" class=\"value\" ");
        }
        return html.toString();
    }

    private static void renderError(StringBuilder html, String sMsg) throws SapphireException {
        html.append("<font color=\"red\">").append(sMsg).append("</font>");
        Logger.logInfo(sMsg);
    }

    private static boolean isSDIFinished(String attributesdcid, DataSet attributedata, int rowIdx, String status) {
        boolean finished_sdi = false;
        if ("Sample".equalsIgnoreCase(attributesdcid)) {
            String reviewreqdflag = attributedata.getValue(rowIdx, "primary_reviewreqdflag");
            finished_sdi = "Reviewed".equalsIgnoreCase(status) || !"Y".equalsIgnoreCase(reviewreqdflag) && "Completed".equalsIgnoreCase(status);
        } else {
            finished_sdi = "DataSet".equalsIgnoreCase(attributesdcid) || attributesdcid.equalsIgnoreCase("SDIWorkItem") ? "Completed".equalsIgnoreCase(status) || "Complete".equalsIgnoreCase(status) : "Released".equalsIgnoreCase(status) || "Release".equalsIgnoreCase(status);
        }
        return finished_sdi;
    }

    @Override
    public void setAvailability(String availability) {
        super.setAvailability(availability);
    }

    private static String getColumnHtml(DataSet attributedata, ArrayList primaryColumns, ArrayList workitemColumns, ArrayList datasetColumns, PropertyListCollection primaryCols, PropertyListCollection sdiwiCols, PropertyListCollection datasetCols, boolean viewOnly, boolean valuesOnly, boolean SDIAcross_multiSDI, DataSet grid) {
        String title;
        String[] splitColumn;
        String colAlias;
        String show;
        String colId;
        PropertyList pl;
        int g;
        StringBuilder areahtml = new StringBuilder();
        for (g = 0; g < primaryCols.size(); ++g) {
            pl = primaryCols.getPropertyList(g);
            colId = pl.getProperty("columnid");
            show = pl.getProperty("show");
            if ("N".equalsIgnoreCase(show) || colId.length() <= 0) continue;
            colAlias = "primary_" + colId;
            if (colId.trim().startsWith("(")) {
                colAlias = Attributes.getColumnAlias(colId, "primary");
            } else if (colId.contains(".")) {
                splitColumn = StringUtil.split(colId, ".");
                String link = splitColumn[0];
                String linkColumn = splitColumn[1];
                colAlias = "primary_" + StringUtil.replaceAll(link, " ", "") + "_" + linkColumn;
            }
            if (colAlias.length() <= 0 || !attributedata.isValidColumn(colAlias)) continue;
            title = pl.getProperty("title", colId);
            if (primaryColumns != null) {
                areahtml.append("<th class=\"title\" nowrap>").append(title).append("</th>");
                primaryColumns.add(colAlias);
                continue;
            }
            areahtml.append("<tr>");
            int colspan = 1;
            if (!viewOnly && !SDIAcross_multiSDI) {
                colspan = valuesOnly ? 2 : 3;
            }
            areahtml.append("<th class=\"title\" nowrap align=left colspan=\"" + colspan + "\">").append(colspan > 1 ? "&nbsp;" : "").append(title).append("</th>");
            if (grid != null && grid.getRowCount() > 0) {
                for (int r = 0; r < grid.size(); ++r) {
                    areahtml.append("<td class=\"value\" align=left>").append(grid.getValue(r, colAlias)).append("</td>");
                }
            } else {
                areahtml.append("<td class=\"value\" align=left>").append(attributedata.getValue(0, colAlias)).append("</td>");
            }
            areahtml.append("</tr>");
        }
        for (g = 0; g < sdiwiCols.size(); ++g) {
            pl = sdiwiCols.getPropertyList(g);
            colId = pl.getProperty("columnid");
            show = pl.getProperty("show");
            if ("N".equalsIgnoreCase(show) || colId.length() <= 0) continue;
            colAlias = "sdiworkitem_" + colId;
            if (colId.trim().startsWith("(")) {
                colAlias = Attributes.getColumnAlias(colId, "sdiworkitem");
            } else if (colId.contains(".")) {
                splitColumn = StringUtil.split(colId, ".");
                String link = splitColumn[0];
                String linkColumn = splitColumn[1];
                colAlias = "sdiworkitem_" + StringUtil.replaceAll(link, " ", "") + "_" + linkColumn;
            }
            if (colAlias.length() <= 0 || !attributedata.isValidColumn(colAlias)) continue;
            title = pl.getProperty("title", colId);
            if (workitemColumns != null) {
                areahtml.append("<th class=\"title\">").append(title).append("</th>");
                workitemColumns.add(colAlias);
                continue;
            }
            areahtml.append("<tr>");
            int colspan = 1;
            if (!viewOnly && !SDIAcross_multiSDI) {
                colspan = valuesOnly ? 2 : 3;
            }
            areahtml.append("<th class=\"title\" nowrap align=left colspan=\"" + colspan + "\">").append(colspan > 1 ? "&nbsp;" : "").append(SafeHTML.encodeForHTMLAttribute(title)).append("</th>");
            if (grid != null && grid.getRowCount() > 0) {
                for (int r = 0; r < grid.size(); ++r) {
                    areahtml.append("<td class=\"value\" align=left>").append(grid.getValue(r, colAlias)).append("</td>");
                }
            } else {
                areahtml.append("<td class=\"value\" align=left>").append(attributedata.getValue(0, colAlias)).append("</td>");
            }
            areahtml.append("</tr>");
        }
        for (g = 0; g < datasetCols.size(); ++g) {
            pl = datasetCols.getPropertyList(g);
            colId = pl.getProperty("columnid");
            show = pl.getProperty("show");
            if ("N".equalsIgnoreCase(show) || colId.length() <= 0) continue;
            colAlias = "sdidata_" + colId;
            if (colId.trim().startsWith("(")) {
                colAlias = Attributes.getColumnAlias(colId, "sdidata");
            } else if (colId.contains(".")) {
                splitColumn = StringUtil.split(colId, ".");
                String link = splitColumn[0];
                String linkColumn = splitColumn[1];
                colAlias = "sdidata_" + StringUtil.replaceAll(link, " ", "") + "_" + linkColumn;
            }
            if (colAlias.length() <= 0 || !attributedata.isValidColumn(colAlias)) continue;
            title = pl.getProperty("title", colId);
            if (datasetColumns != null) {
                areahtml.append("<th class=\"title\">").append(title).append("</th>");
                datasetColumns.add(colAlias);
                continue;
            }
            areahtml.append("<tr>");
            int colspan = 1;
            if (!viewOnly && !SDIAcross_multiSDI) {
                colspan = valuesOnly ? 2 : 3;
            }
            areahtml.append("<th class=\"title\" nowrap align=left colspan=\"" + colspan + "\">").append(colspan > 1 ? "&nbsp;" : "").append(title).append("</th>");
            if (grid != null && grid.getRowCount() > 0) {
                for (int r = 0; r < grid.size(); ++r) {
                    areahtml.append("<td class=\"value\" align=left>").append(grid.getValue(r, colAlias)).append("</td>");
                }
            } else {
                areahtml.append("<td class=\"value\" align=left>").append(attributedata.getValue(0, colAlias)).append("</td>");
            }
            areahtml.append("</tr>");
        }
        return areahtml.toString();
    }

    private static String getColumnAlias(String colId, String datasetName) {
        String colAlias = "";
        int endBracketIndex = colId.indexOf(")");
        if (endBracketIndex > 0 && (colAlias = colId.substring(endBracketIndex + 1).trim()).length() > 0) {
            colAlias = datasetName + "_" + colAlias;
        }
        return colAlias;
    }

    private String getNestedColumnSql(String colId, String datasetName, TranslationProcessor tp) throws SapphireException {
        String colAlias;
        colId = colId.replaceAll("primary.", "primarytable.");
        int endBracketIndex = colId.indexOf(")");
        String sql = "";
        if (endBracketIndex > 0) {
            colAlias = colId.substring(endBracketIndex + 1).trim();
            if (colAlias.length() <= 0) {
                throw new SapphireException(tp.translate("Column alias not specified: " + colId));
            }
        } else {
            throw new SapphireException(tp.translate("Invalid column syntax: " + colId));
        }
        sql = ", " + colId.substring(0, endBracketIndex + 1) + " " + datasetName + "_" + colAlias;
        return sql;
    }

    private void appendSourceClause(StringBuffer sql, boolean worksheet, String onKey1, String onKey2, String onKey3, String onSDC) {
        String joinClause;
        String string = joinClause = this.config.getProperty("showsdiswithoutattribute").equalsIgnoreCase("Y") ? " RIGHT OUTER JOIN " : " LEFT OUTER JOIN ";
        if (worksheet) {
            sql.append(joinClause).append(" worksheetsdi ").append("ON ").append(onKey1).append(" = worksheetsdi.keyid1 ");
            if (onKey2 != null && onKey2.length() > 0) {
                sql.append("AND ").append(onKey2).append(" = worksheetsdi.keyid2 ");
            }
            if (onKey3 != null && onKey3.length() > 0) {
                sql.append("AND ").append(onKey3).append(" = worksheetsdi.keyid3 ");
            }
            if (onSDC != null && onSDC.length() > 0) {
                sql.append("AND ").append(onSDC).append(" = worksheetsdi.sdcid ");
            }
            sql.append("WHERE worksheetsdi.sdcid = ? AND ");
            sql.append("worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? ");
        } else {
            sql.append(joinClause).append(" worksheetitemsdi ").append("ON ").append(onKey1).append(" = worksheetitemsdi.keyid1 ");
            if (onKey2 != null && onKey2.length() > 0) {
                sql.append("AND ").append(onKey2).append(" = worksheetitemsdi.keyid2 ");
            }
            if (onKey3 != null && onKey3.length() > 0) {
                sql.append("AND ").append(onKey3).append(" = worksheetitemsdi.keyid3 ");
            }
            if (onSDC != null && onSDC.length() > 0) {
                sql.append("AND ").append(onSDC).append(" = worksheetitemsdi.sdcid ");
            }
            sql.append("WHERE worksheetitemsdi.sdcid = ? AND ");
            sql.append("worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? ");
        }
    }

    private Object[] getParams(boolean worksheet, String sdcid, String workitemid, String paramlistid, String paramlistversionid, String variantid) {
        if (worksheet) {
            if (workitemid != null && paramlistid != null) {
                return new Object[]{sdcid, this.getWorksheetId(), this.getWorksheetVersionId(), workitemid, paramlistid, paramlistversionid, variantid};
            }
            if (workitemid != null) {
                return new Object[]{sdcid, this.getWorksheetId(), this.getWorksheetVersionId(), workitemid};
            }
            if (paramlistid != null) {
                return new Object[]{sdcid, this.getWorksheetId(), this.getWorksheetVersionId(), paramlistid, paramlistversionid, variantid};
            }
            return new Object[]{sdcid, this.getWorksheetId(), this.getWorksheetVersionId()};
        }
        if (workitemid != null && paramlistid != null) {
            return new Object[]{sdcid, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), workitemid, paramlistid, paramlistversionid, variantid};
        }
        if (workitemid != null) {
            return new Object[]{sdcid, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), workitemid};
        }
        if (paramlistid != null) {
            return new Object[]{sdcid, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), paramlistid, paramlistversionid, variantid};
        }
        return new Object[]{sdcid, this.getWorksheetItemId(), this.getWorksheetItemVersionId()};
    }

    private DataSet getAttributeData(String tableStyling) throws SapphireException {
        DataSet sdiattribute = null;
        String attributeid = this.config.getProperty("attributeid", "");
        String mode = this.config.getProperty("attributemode", "worksheet");
        this.primaryCols = this.config.getCollectionNotNull("primarycolumns");
        this.sdiwiCols = this.config.getCollectionNotNull("sdiworkitemcolumns");
        this.datasetCols = this.config.getCollectionNotNull("sdidatacolumns");
        boolean reqdPrimary = false;
        boolean reqdSDIWI = false;
        String primaryTable = "";
        SDCProcessor sdcp = this.getSDCProcessor();
        this.sourceSDC = this.config.getProperty("sourcesdcid");
        if (this.primaryCols.size() > 0) {
            reqdPrimary = true;
            if (this.sourceSDC.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Data Source SDC not specified!!"));
            }
            primaryTable = sdcp.getProperty(this.sourceSDC, "tableid");
            this.keyColId1 = sdcp.getProperty(this.sourceSDC, "keycolid1");
            this.keyColId2 = sdcp.getProperty(this.sourceSDC, "keycolid2");
            this.keyColId3 = sdcp.getProperty(this.sourceSDC, "keycolid3");
        }
        if (this.sdiwiCols.size() > 0) {
            reqdSDIWI = true;
        }
        String paramlistid = this.config.getProperty("paramlistid", "");
        if (("ParamList".equalsIgnoreCase(this.config.getProperty("sourcerelation", "")) || "sdidata".equalsIgnoreCase(this.config.getProperty("attributetype", "Primary"))) && paramlistid.length() > 0 && !paramlistid.startsWith("$S{")) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder("select distinct sdidata.paramlistid,sdidata.availabilityflag from sdidata,worksheetsdi");
            sql.append(" where worksheetsdi.worksheetid=").append(safeSQL.addVar(this.getWorksheetId()));
            sql.append(" and worksheetsdi.worksheetversionid=").append(safeSQL.addVar(this.getWorksheetVersionId()));
            sql.append(" and sdidata.sdcid=worksheetsdi.sdcid");
            sql.append(" and sdidata.keyid1=worksheetsdi.keyid1");
            sql.append(" and sdidata.sdcid='Sample'");
            sql.append(" and sdidata.paramlistid=").append(safeSQL.addVar(paramlistid));
            sql.append(" and sdidata.paramlistversionid=").append(safeSQL.addVar(this.config.getProperty("paramlistversionid", "")));
            sql.append(" and sdidata.variantid=").append(safeSQL.addVar(this.config.getProperty("variantid", "")));
            sql.append(" and sdidata.availabilityflag='Y'");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.getRowCount() == 0) {
                this.setAttributeSDCID();
                return new DataSet();
            }
        }
        if (mode.equalsIgnoreCase("standard")) {
            boolean worksheetAttributes = this.config.getProperty("source", "Control").equalsIgnoreCase("worksheet");
            String sourcesdcid = this.config.getProperty("sourcesdcid");
            String attributetype = this.config.getProperty("attributetype", "Primary");
            String workitemid = this.config.getProperty("workitemid", "");
            String paramlistversionid = this.config.getProperty("paramlistversionid", "");
            String variantid = this.config.getProperty("variantid", "");
            if (OpalUtil.isNotEmpty(sourcesdcid)) {
                if (sourcesdcid.equalsIgnoreCase("sdiworkitem")) {
                    switch (attributetype.toLowerCase()) {
                        case "sdiworkitem": {
                            sdiattribute = this.handleForSDIWorkItemForSourceSDC(attributeid, sdcp, worksheetAttributes, sourcesdcid, workitemid);
                            break;
                        }
                        case "sdidata": {
                            sdiattribute = this.handleForSDIDataForSourceSDC(attributeid, sdcp, worksheetAttributes, sourcesdcid, workitemid, paramlistid, paramlistversionid, variantid);
                            break;
                        }
                        default: {
                            this.templateMessage = "Template - <i>NOT SUPPORTED: Primary attributes against SDIWorkItem SDI's assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + ".</i>";
                            sdiattribute = null;
                            break;
                        }
                    }
                } else {
                    switch (attributetype.toLowerCase()) {
                        case "sdiworkitem": {
                            sdiattribute = this.handleForSDIWorkItem(attributeid, reqdPrimary, primaryTable, sdcp, worksheetAttributes, sourcesdcid, workitemid);
                            break;
                        }
                        case "sdidata": {
                            sdiattribute = this.handleForSDIData(attributeid, reqdPrimary, reqdSDIWI, primaryTable, sdcp, worksheetAttributes, sourcesdcid, workitemid, paramlistid, paramlistversionid, variantid);
                            break;
                        }
                        default: {
                            sdiattribute = this.handleForPrimary(attributeid, sdcp, worksheetAttributes, sourcesdcid);
                            break;
                        }
                    }
                }
            } else {
                this.logError("No sdcid provided.");
                this.worksheetItemOptions.setRequiresConfig(true, "Attribute Control requires configuration - click to configure");
            }
        } else {
            String id;
            String attributesource = this.config.getProperty("attributesource", "Control");
            this.attributesdcid = attributesource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (attributesource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem");
            this.config.setProperty("sourcesdcid", this.attributesdcid);
            String sourcerelation = this.config.getProperty("sourcerelation", "????");
            String context = this.config.getProperty("worksheetcontext", "????");
            DataSet display = this.getQueryProcessor().getPreparedSqlDataSet("SELECT refdisplayvalue FROM refvalue WHERE reftypeid='WorksheetContext' AND refvalueid=?", (Object[])new String[]{context});
            if (display.size() == 1) {
                context = display.getValue(0, "refdisplayvalue", context);
            }
            TranslationProcessor tp = this.getTranslationProcessor();
            String string = this.attributesdcid.equals("LV_Worksheet") ? this.getWorksheetId() : (id = this.attributesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionId() : this.getWorksheetItemId());
            String version = this.attributesdcid.equals("LV_Worksheet") ? this.getWorksheetVersionId() : (this.attributesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionVersionId() : this.getWorksheetItemVersionId());
            sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattribute.*, sdiattribute.keyid1 primary1, sdiattribute.keyid2 primary2, sdiattribute.keyid3 primary3 FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attributesdcid = ?", new Object[]{this.attributesdcid, id, version, this.attributesdcid}, true);
            this.templateMessage = "<table class=\"" + tableStyling + "\"><tr><td class=\"value\">" + tp.translate("Instructions or text for the") + " " + sourcerelation + " " + tp.translate("with position") + " \"" + context + "\"</td><td class=\"value\"><input type=\"checkbox\"></td></tr></table>";
        }
        return sdiattribute;
    }

    private DataSet handleForPrimary(String attributeid, SDCProcessor sdcp, boolean worksheetAttributes, String sourcesdcid) throws SapphireException {
        this.attributesdcid = sourcesdcid;
        String primaryTable = sdcp.getProperty(sourcesdcid, "tableid");
        this.keyColId1 = sdcp.getProperty(sourcesdcid, "keycolid1");
        this.keyColId2 = sdcp.getProperty(sourcesdcid, "keycolid2");
        this.keyColId3 = sdcp.getProperty(sourcesdcid, "keycolid3");
        String topt = "";
        StringBuffer sql = new StringBuffer();
        String statusColumn = "";
        if ("Sample".equalsIgnoreCase(this.sourceSDC)) {
            statusColumn = "samplestatus";
        } else if ("Batch".equalsIgnoreCase(this.sourceSDC)) {
            statusColumn = "batchstatus";
        } else if ("LV_BatchStage".equalsIgnoreCase(this.sourceSDC)) {
            statusColumn = "batchstagestatus";
        } else if ("LV_MonitorGroup".equalsIgnoreCase(this.sourceSDC)) {
            statusColumn = "monitorgroupstatus";
        } else if ("Request".equalsIgnoreCase(this.sourceSDC)) {
            statusColumn = "requeststatus";
        }
        if (worksheetAttributes) {
            sql.append("SELECT sdiattribute.*, worksheetsdi.sdcid attrsdcid, worksheetsdi.keyid1 primary1, worksheetsdi.keyid2 primary2, worksheetsdi.keyid3 primary3  ");
        } else {
            sql.append("SELECT sdiattribute.*, worksheetitemsdi.sdcid attrsdcid, worksheetitemsdi.keyid1 primary1, worksheetitemsdi.keyid2 primary2, worksheetitemsdi.keyid3 primary3  ");
        }
        if (statusColumn.length() > 0) {
            sql.append(",primarytable." + statusColumn + " primary_status");
        }
        sql.append("Sample".equalsIgnoreCase(this.sourceSDC) ? ", primarytable.reviewrequiredflag primary_reviewreqdflag " : "");
        sql.append(this.getColumnSQL(this.primaryCols, null, null, sdcp));
        sql.append(" FROM ");
        sql.append(primaryTable + " primarytable, ");
        sql.append(" sdiattribute ");
        this.appendSourceClause(sql, worksheetAttributes, "sdiattribute.keyid1", "sdiattribute.keyid2", "sdiattribute.keyid3", "sdiattribute.sdcid");
        if (attributeid.length() > 0) {
            sql.append(" AND sdiattribute.attributeid IN ('").append(StringUtil.replaceAll(attributeid, ";", "','")).append("') ");
            topt = topt + "Filtered by Attribute(s) " + attributeid + ".";
        }
        this.appendWhereClause(sql, worksheetAttributes);
        DataSet sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), this.getParams(worksheetAttributes, sourcesdcid, null, null, null, null));
        if (OpalUtil.isNotEmpty(sdiattribute)) {
            for (int count = 0; count < sdiattribute.size(); ++count) {
                sdiattribute.addColumn("emptyattribute", 0);
                boolean emptyAttribute = false;
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "sdcid"))) {
                    sdiattribute.setValue(count, "sdcid", sdiattribute.getValue(count, "attrsdcid"));
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid1"))) {
                    sdiattribute.setValue(count, "keyid1", sdiattribute.getValue(count, "primary1"));
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid2"))) {
                    sdiattribute.setValue(count, "keyid2", sdiattribute.getValue(count, "primary2"));
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid3"))) {
                    sdiattribute.setValue(count, "keyid3", sdiattribute.getValue(count, "primary3"));
                    emptyAttribute = true;
                }
                sdiattribute.setValue(count, "emptyattribute", String.valueOf(emptyAttribute));
            }
        }
        this.templateMessage = "Template - <i>Primary attributes against " + sourcesdcid + " SDI's assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + "." + (topt.length() > 0 ? " " + topt : "") + "</i>";
        return sdiattribute;
    }

    private DataSet handleForSDIData(String attributeid, boolean reqdPrimary, boolean reqdSDIWI, String primaryTable, SDCProcessor sdcp, boolean worksheetAttributes, String sourcesdcid, String workitemid, String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        DataSet sdiattribute;
        this.attributesdcid = "DataSet";
        Object[] params = this.getParams(worksheetAttributes, sourcesdcid, null, null, null, null);
        String topt = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT sdiattribute.*, sdidata.keyid1 primary1, sdidata.keyid2 primary2, sdidata.keyid3 primary3, sdidata.s_datasetstatus ");
        sql.append(this.getColumnSQL(this.primaryCols, this.sdiwiCols, this.datasetCols, sdcp));
        sql.append(" FROM ");
        if (reqdPrimary) {
            sql.append(primaryTable + " primarytable, ");
        }
        sql.append(" sdiattribute ");
        String joinClause = this.config.getProperty("showsdiswithoutattribute").equalsIgnoreCase("Y") ? " RIGHT OUTER JOIN " : " LEFT OUTER JOIN ";
        sql.append(joinClause).append(" sdidata ").append("ON sdiattribute.keyid1 = sdidata.sdidataid ");
        if (reqdSDIWI) {
            sql.append(" LEFT OUTER JOIN sdiworkitem ");
            sql.append("ON sdiworkitem.sdcid = sdidata.sdcid AND sdiworkitem.keyid1 = sdidata.keyid1  AND sdiworkitem.keyid2 = sdidata.keyid2  AND sdiworkitem.keyid3 = sdidata.keyid3 AND sdiworkitem.workitemid = sdidata.sourceworkitemid AND sdiworkitem.workiteminstance = sdidata.sourceworkiteminstance ");
        }
        if (workitemid.length() > 0) {
            sql.append(" INNER JOIN sdiworkitemitem ");
            sql.append(" ON sdiworkitemitem.sdcid = sdidata.sdcid AND sdiworkitemitem.keyid1 = sdidata.keyid1  AND sdiworkitemitem.keyid2 = sdidata.keyid2  AND sdiworkitemitem.keyid3 = sdidata.keyid3  ");
        }
        this.appendSourceClause(sql, worksheetAttributes, "sdidata.keyid1", "sdidata.keyid2", "sdidata.keyid3", "sdidata.sdcid");
        sql.append("AND worksheetitemsdi.keyid1 =  sdidata.keyid1 ");
        if (workitemid.length() > 0) {
            sql.append(" AND sdiworkitemitem.workitemid=? AND sdiworkitemitem.itemsdcid='ParamList' AND sdiworkitemitem.itemkeyid1=sdidata.paramlistid AND sdiworkitemitem.itemkeyid2=sdidata.paramlistversionid AND sdiworkitemitem.itemkeyid3=sdidata.variantid ");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid, null, null, null);
            topt = topt + "Filtered by Test " + workitemid + ".";
        }
        if (paramlistid.length() > 0 && paramlistversionid.length() > 0 && variantid.length() > 0) {
            sql.append(" AND sdidata.paramlistid = ? AND sdidata.paramlistversionid = ? AND sdidata.variantid = ? ");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid.length() > 0 ? workitemid : null, paramlistid, paramlistversionid, variantid);
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Parameter List " + paramlistid + "(" + paramlistversionid + " " + variantid + ").";
        }
        if (attributeid.length() > 0) {
            sql.append(" AND sdiattribute.attributeid IN ('").append(StringUtil.replaceAll(attributeid, ";", "','")).append("') ");
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Attribute(s) " + attributeid + ".";
        }
        if (reqdPrimary) {
            sql.append(" AND sdidata.sdcid =  '" + sourcesdcid + "'");
            sql.append(" AND primarytable." + this.keyColId1 + " =  sdidata.keyid1");
            if (this.keyColId2.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId2 + " =  sdidata.keyid2");
            }
            if (this.keyColId3.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId3 + " =  sdidata.keyid3");
            }
        }
        if (OpalUtil.isNotEmpty(sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params))) {
            for (int count = 0; count < sdiattribute.size(); ++count) {
                sdiattribute.addColumn("emptyattribute", 0);
                boolean emptyAttribute = false;
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "sdcid"))) {
                    sdiattribute.setValue(count, "sdcid", "Dataset");
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid1"))) {
                    sdiattribute.setValue(count, "keyid1", sdiattribute.getValue(count, "sdidataid"));
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid2"))) {
                    sdiattribute.setValue(count, "keyid2", "(null)");
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid3"))) {
                    sdiattribute.setValue(count, "keyid3", "(null)");
                    emptyAttribute = true;
                }
                sdiattribute.setValue(count, "emptyattribute", String.valueOf(emptyAttribute));
            }
        }
        this.templateMessage = "Template - <i>DataSet attributes against " + sourcesdcid + " SDI's assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + "." + (topt.length() > 0 ? " " + topt : "") + "</i>";
        return sdiattribute;
    }

    private DataSet handleForSDIWorkItem(String attributeid, boolean reqdPrimary, String primaryTable, SDCProcessor sdcp, boolean worksheetAttributes, String sourcesdcid, String workitemid) throws SapphireException {
        DataSet sdiattribute;
        this.attributesdcid = "SDIWorkItem";
        Object[] params = this.getParams(worksheetAttributes, sourcesdcid, null, null, null, null);
        String topt = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT sdiattribute.*, sdiworkitem.sdiworkitemid workitemid, sdiworkitem.keyid1 primary1, sdiworkitem.keyid2 primary2, sdiworkitem.keyid3 primary3, sdiworkitem.workitemstatus ");
        sql.append(this.getColumnSQL(this.primaryCols, this.sdiwiCols, null, sdcp));
        sql.append(" FROM ");
        if (reqdPrimary) {
            sql.append(primaryTable + " primarytable, ");
        }
        sql.append(" sdiattribute ");
        String joinClause = this.config.getProperty("showsdiswithoutattribute").equalsIgnoreCase("Y") ? " RIGHT OUTER JOIN " : " LEFT OUTER JOIN ";
        sql.append(joinClause).append(" sdiworkitem ").append("ON sdiattribute.keyid1 = sdiworkitem.sdiworkitemid ");
        this.appendSourceClause(sql, worksheetAttributes, "sdiworkitem.keyid1", "sdiworkitem.keyid2", "sdiworkitem.keyid3", "sdiworkitem.sdcid");
        sql.append("AND " + (worksheetAttributes ? "worksheetsdi" : "worksheetitemsdi") + ".keyid1 = sdiworkitem.keyid1 ");
        if (workitemid.length() > 0) {
            sql.append("AND sdiworkitem.workitemid = ? ");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid, null, null, null);
            topt = topt + "Filtered by Test " + workitemid + ".";
        }
        if (attributeid.length() > 0) {
            sql.append("AND sdiattribute.attributeid IN ('").append(StringUtil.replaceAll(attributeid, ";", "','")).append("') ");
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Attribute(s) " + attributeid + ".";
        }
        if (reqdPrimary) {
            sql.append(" AND sdiworkitem.sdcid =  '" + sourcesdcid + "'");
            sql.append(" AND primarytable." + this.keyColId1 + " =  sdiworkitem.keyid1");
            if (this.keyColId2.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId2 + " =  sdiworkitem.keyid2");
            }
            if (this.keyColId3.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId3 + " =  sdiworkitem.keyid3");
            }
        }
        if (OpalUtil.isNotEmpty(sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params))) {
            for (int count = 0; count < sdiattribute.size(); ++count) {
                sdiattribute.addColumn("emptyattribute", 0);
                boolean emptyAttribute = false;
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "sdcid"))) {
                    sdiattribute.setValue(count, "sdcid", "SDIWorkItem");
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid1"))) {
                    sdiattribute.setValue(count, "keyid1", sdiattribute.getValue(count, "workitemid"));
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid2"))) {
                    sdiattribute.setValue(count, "keyid2", "(null)");
                    emptyAttribute = true;
                }
                if (OpalUtil.isEmpty(sdiattribute.getValue(count, "keyid3"))) {
                    sdiattribute.setValue(count, "keyid3", "(null)");
                    emptyAttribute = true;
                }
                sdiattribute.setValue(count, "emptyattribute", String.valueOf(emptyAttribute));
            }
        }
        this.templateMessage = "Template - <i>Test attributes against " + sourcesdcid + " SDI's assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + "." + (topt.length() > 0 ? " " + topt : "") + "</i>";
        return sdiattribute;
    }

    private DataSet handleForSDIDataForSourceSDC(String attributeid, SDCProcessor sdcp, boolean worksheetAttributes, String sourcesdcid, String workitemid, String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        this.attributesdcid = "DataSet";
        Object[] params = this.getParams(worksheetAttributes, sourcesdcid, null, null, null, null);
        String topt = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT sdiattribute.*, sdidata.keyid1 primary1, sdidata.keyid2 primary2, sdidata.keyid3 primary3, sdidata.s_datasetstatus ");
        sql.append(this.getColumnSQL(null, this.sdiwiCols, this.datasetCols, sdcp));
        sql.append(" FROM ");
        sql.append(" sdiattribute ");
        sql.append("INNER JOIN sdidata ");
        sql.append("ON sdiattribute.keyid1 = sdidata.sdidataid ");
        sql.append("INNER JOIN sdiworkitemitem ");
        sql.append("ON sdidata.keyid1 = sdiworkitemitem.keyid1 AND sdidata.keyid2 = sdiworkitemitem.keyid2 AND sdidata.keyid3 = sdiworkitemitem.keyid3 AND sdidata.sdcid = sdiworkitemitem.sdcid ");
        sql.append("AND sdidata.paramlistid = sdiworkitemitem.itemkeyid1 AND sdidata.paramlistversionid = sdiworkitemitem.itemkeyid2 AND sdidata.variantid = sdiworkitemitem.itemkeyid3 ");
        sql.append("AND sdidata.dataset = sdiworkitemitem.iteminstance ");
        sql.append("INNER JOIN sdiworkitem ");
        sql.append("ON sdiworkitemitem.keyid1 = sdiworkitem.keyid1 AND sdiworkitemitem.keyid2 = sdiworkitem.keyid2 AND sdiworkitemitem.keyid3 = sdiworkitem.keyid3 AND sdiworkitemitem.sdcid = sdiworkitem.sdcid ");
        sql.append("AND sdiworkitemitem.workitemid = sdiworkitem.workitemid AND sdiworkitemitem.workiteminstance = sdiworkitem.workiteminstance ");
        this.appendSourceClause(sql, worksheetAttributes, "sdiworkitem.sdiworkitemid", null, null, null);
        sql.append("AND sdiattribute.sdcid = 'DataSet' ");
        sql.append("AND sdiworkitemitem.itemsdcid = 'ParamList' ");
        if (workitemid.length() > 0) {
            sql.append("AND sdiworkitem.workitemid = ? ");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid, null, null, null);
            topt = topt + "Filtered by Test " + workitemid + ".";
        }
        if (paramlistid.length() > 0 && paramlistversionid.length() > 0 && variantid.length() > 0) {
            sql.append("AND sdidata.paramlistid = ? AND sdidata.paramlistversionid = ? AND sdidata.variantid = ?");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid.length() > 0 ? workitemid : null, paramlistid, paramlistversionid, variantid);
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Parameter List " + paramlistid + "(" + paramlistversionid + " " + variantid + ").";
        }
        if (attributeid.length() > 0) {
            sql.append("AND sdiattribute.attributeid IN ('").append(StringUtil.replaceAll(attributeid, ";", "','")).append("') ");
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Attribute(s) " + attributeid + ".";
        }
        DataSet sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params);
        this.templateMessage = "Template - <i>Data attributes for SDIWorkItems assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + "." + (topt.length() > 0 ? " " + topt : "") + "</i>";
        return sdiattribute;
    }

    private DataSet handleForSDIWorkItemForSourceSDC(String attributeid, SDCProcessor sdcp, boolean worksheetAttributes, String sourcesdcid, String workitemid) throws SapphireException {
        this.attributesdcid = sourcesdcid;
        String topt = "";
        StringBuffer sql = new StringBuffer();
        Object[] params = this.getParams(worksheetAttributes, sourcesdcid, null, null, null, null);
        sql.append("SELECT sdiattribute.*, sdiworkitem.keyid1 primary1, sdiworkitem.keyid2 primary2, sdiworkitem.keyid3 primary3, sdiworkitem.workitemstatus ");
        sql.append(this.getColumnSQL(null, this.sdiwiCols, null, sdcp));
        sql.append(" FROM ");
        sql.append(" sdiattribute ");
        sql.append("INNER JOIN sdiworkitem ");
        sql.append("ON sdiattribute.keyid1 = sdiworkitem.sdiworkitemid ");
        this.appendSourceClause(sql, worksheetAttributes, "sdiattribute.keyid1", null, null, null);
        sql.append("AND sdiattribute.sdcid = 'SDIWorkItem' ");
        if (workitemid.length() > 0) {
            sql.append("AND sdiworkitem.workitemid = ? ");
            params = this.getParams(worksheetAttributes, sourcesdcid, workitemid, null, null, null);
            topt = topt + "Filtered by Test " + workitemid + ".";
        }
        if (attributeid.length() > 0) {
            sql.append("AND sdiattribute.attributeid IN ('").append(StringUtil.replaceAll(attributeid, ";", "','")).append("') ");
            topt = topt + (topt.length() > 0 ? " " : "") + "Filtered by Attribute(s) " + attributeid + ".";
        }
        DataSet sdiattribute = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params);
        this.templateMessage = "Template - <i>SDIWorkItem attributes for SDIWorkItems assigned to the " + (worksheetAttributes ? "Worksheet" : "Control") + "." + (topt.length() > 0 ? " " + topt : "") + "</i>";
        return sdiattribute;
    }

    private String getColumnSQL(PropertyListCollection primaryCols, PropertyListCollection sdiwiCols, PropertyListCollection datasetCols, SDCProcessor sdcp) throws SapphireException {
        String colId;
        PropertyList pl;
        int g;
        StringBuffer sql = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (primaryCols != null) {
            for (g = 0; g < primaryCols.size(); ++g) {
                pl = primaryCols.getPropertyList(g);
                colId = pl.getProperty("columnid").trim();
                if (colId.length() <= 0) continue;
                if (colId.trim().startsWith("(")) {
                    sql.append(this.getNestedColumnSql(colId, "primary", tp));
                    continue;
                }
                if (colId.contains(".")) {
                    sql.append(this.getLinkedColumnSQL(colId, this.sourceSDC, "primary", sdcp, tp, this.getQueryProcessor()));
                    continue;
                }
                if (sdcp.getColumns(this.sourceSDC).find("columnid", colId) == null) {
                    throw new SapphireException(tp.translate("Invalid column in Primary column configuration:") + colId);
                }
                sql.append(", primarytable." + colId + " primary_" + colId);
            }
        }
        if (sdiwiCols != null) {
            for (g = 0; g < sdiwiCols.size(); ++g) {
                pl = sdiwiCols.getPropertyList(g);
                colId = pl.getProperty("columnid").trim();
                if (colId.length() <= 0) continue;
                if (colId.trim().startsWith("(")) {
                    sql.append(this.getNestedColumnSql(colId, "sdiworkitem", tp));
                    continue;
                }
                if (colId.contains(".")) {
                    sql.append(this.getLinkedColumnSQL(colId, "SDIWorkItem", "sdiworkitem", sdcp, tp, this.getQueryProcessor()));
                    continue;
                }
                if (sdcp.getColumns("SDIWorkItem").find("columnid", colId) == null) {
                    throw new SapphireException(tp.translate("Invalid column in SDIWorkItem column configuration:") + colId);
                }
                sql.append(", sdiworkitem." + colId + " sdiworkitem_" + colId);
            }
            if (sql.toString().contains("sdiworkitem_")) {
                if (!sql.toString().contains("sdiworkitem.usersequence")) {
                    sql.append(", sdiworkitem.usersequence sdiworkitem_usersequence");
                }
                if (!sql.toString().contains("sdiworkitem.workiteminstance")) {
                    sql.append(", sdiworkitem.workiteminstance sdiworkitem_workiteminstance");
                }
            }
        }
        if (datasetCols != null) {
            for (g = 0; g < datasetCols.size(); ++g) {
                pl = datasetCols.getPropertyList(g);
                colId = pl.getProperty("columnid").trim();
                if (colId.length() <= 0) continue;
                if (colId.trim().startsWith("(")) {
                    sql.append(this.getNestedColumnSql(colId, "sdidata", tp));
                    continue;
                }
                if (colId.contains(".")) {
                    sql.append(this.getLinkedColumnSQL(colId, "DataSet", "sdidata", sdcp, this.getTranslationProcessor(), this.getQueryProcessor()));
                    continue;
                }
                if (sdcp.getColumns("DataSet").find("columnid", colId) == null) {
                    throw new SapphireException(tp.translate("Invalid column in SDIData column configuration:") + colId);
                }
                sql.append(", sdidata." + colId + " sdidata_" + colId);
            }
            if (sql.toString().contains("sdidata_")) {
                if (!sql.toString().contains("sdidata.usersequence")) {
                    sql.append(", sdidata.usersequence sdidata_usersequence");
                }
                if (!sql.toString().contains("sdidata.dataset")) {
                    sql.append(", sdidata.dataset sdidata_dataset");
                }
            }
        }
        return sql.append(" ").toString();
    }

    private String getLinkedColumnSQL(String colId, String sdcId, String datasetName, SDCProcessor sdcp, TranslationProcessor tp, QueryProcessor qp) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String tableName = "primary".equalsIgnoreCase(datasetName) ? "primarytable" : datasetName;
        String[] splitColumn = StringUtil.split(colId, ".");
        String link = splitColumn[0];
        String linkColumn = splitColumn[1];
        if (link.length() > 0 && linkColumn.length() > 0) {
            PropertyListCollection links = sdcp.getLinks(sdcId);
            for (int linkno = 0; linkno < links.size(); ++linkno) {
                PropertyList linkPL = links.getPropertyList(linkno);
                String linkSdcId = linkPL.getProperty("linksdcid");
                String linkId = linkPL.getProperty("linkid");
                String linkTable = linkPL.getProperty("linktableid");
                if (linkTable.length() == 0) {
                    if (linkSdcId.length() > 0) {
                        linkTable = sdcp.getProperty(linkSdcId, "tableid");
                    }
                    if (linkTable.length() == 0) {
                        throw new SapphireException(tp.translate("Invalid column syntax. Cannot resolve the linked table Id from sdclink table. " + colId));
                    }
                }
                if (!link.equalsIgnoreCase(linkSdcId) && !link.equalsIgnoreCase(linkId) && !link.equalsIgnoreCase(linkTable)) continue;
                if (linkSdcId.length() == 0) {
                    DataSet ds = qp.getPreparedSqlDataSet("select sdcid from sdc where tableid = ?", (Object[])new String[]{linkTable});
                    linkSdcId = ds.getValue(0, "sdcid");
                }
                String sdccolumnid = linkPL.getProperty("sdccolumnid");
                String sdccolumnid2 = linkPL.getProperty("sdccolumnid2");
                String sdccolumnid3 = linkPL.getProperty("sdccolumnid3");
                String linksdckeycolid1 = sdcp.getProperty(linkSdcId, "keycolid1");
                String linksdckeycolid2 = sdcp.getProperty(linkSdcId, "keycolid2");
                String linksdckeycolid3 = sdcp.getProperty(linkSdcId, "keycolid3");
                sql.append(", ( select ").append(linkColumn).append(" From ").append(linkTable).append(" where ").append(linksdckeycolid1).append(" = ").append(tableName + ".").append(sdccolumnid).append(linksdckeycolid2.length() > 0 ? " AND " + linksdckeycolid2 + " = " + tableName + "." + sdccolumnid2 : "").append(linksdckeycolid3.length() > 0 ? " AND " + linksdckeycolid3 + " = " + tableName + "." + sdccolumnid3 : "").append(" ) " + datasetName + "_" + StringUtil.replaceAll(link, " ", "") + "_" + linkColumn);
                break;
            }
        } else {
            throw new SapphireException(tp.translate("Invalid column syntax. " + colId));
        }
        return sql.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        String tableStyling = this.getTableStylingProperty("displaystyle", false, "lv_elntable_grid  ");
        StringBuffer html = new StringBuffer();
        if (this.sdiattribute == null) {
            this.sdiattribute = this.getAttributeData(tableStyling);
        }
        String c1 = "";
        String c2 = "";
        String c3 = "";
        boolean multiplePrimary = false;
        for (int i = 0; i < this.sdiattribute.getRowCount(); ++i) {
            String k1 = this.sdiattribute.getValue(i, "keyid1");
            String k2 = this.sdiattribute.getValue(i, "keyid2");
            String k3 = this.sdiattribute.getValue(i, "keyid3");
            if (!(c1.length() == 0 || c1.equals(k1) && c2.equals(k2) && c3.equals(k3))) {
                multiplePrimary = true;
            }
            c1 = k1;
            c2 = k2;
            c3 = k3;
        }
        if (!multiplePrimary) {
            String attributekeyid1 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getValue(0, "keyid1", "") : "";
            String attributekeyid2 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getValue(0, "keyid2", "") : "";
            String attributekeyid3 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getValue(0, "keyid3", "") : "";
            MaintAttribute ma = new MaintAttribute(this.attributesdcid, attributekeyid1, attributekeyid2, attributekeyid3, this.sdiattribute, this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId()), new Logger(this.logContext));
            ma.setRenderMode(MaintAttribute.RenderMode.instance);
            ma.setElementid(this.getElementId());
            PropertyList props = new PropertyList();
            props.setProperty("viewonly", "N");
            props.setProperty("valuesonly", this.config.getProperty("valuesonly", "Y"));
            props.setProperty("sdcid", this.attributesdcid);
            props.setProperty("mode", "Adhoc");
            props.setProperty("scrollable", "Y");
            ma.setElementProperties(props);
            html.append(ma.getHtml());
            html.append("<script>");
            html.append(this.addDropDownDiv());
            html.append("dd_offsetAjust.offset = document.getElementById('multiMaintGridDiv');");
            html.append("dd_offsetAjust.top = 5;");
            html.append("dd_offsetAjust.left = -5;");
            html.append(ma.getScript());
            html.append(this.getElementId() + "_multi = ").append("false").append(";");
            html.append("if (typeof(sapdateformat) == 'undefined'){");
            html.append("var sapdateformat = ").append(RegexConverter.getValidDateFormatExp(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())))).append(";\n");
            html.append("var decimalSeparator = sapphire.connection.decimalSeparator;");
            html.append("var groupingSeparator = sapphire.connection.groupingSeparator;");
            html.append("}");
            html.append("var sapdateformat4DigitYear = ").append(RegexConverter.getValidDateFormatExp4DigitYear(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())), true, "4")).append(";\n");
            html.append("var sapdateformat2DigitYear = ").append(RegexConverter.getValidDateFormatExp4DigitYear(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())), true, "2")).append(";\n");
            JSONObject configJson = this.config.toJSONObject();
            String jsonStr = configJson.toString();
            html.append(" var config_" + this.getElementId()).append(" = '").append(jsonStr).append("'");
            html.append("</script>");
        } else {
            int keyCount;
            try {
                keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(this.attributesdcid, "keycolumns"));
            }
            catch (Exception e) {
                keyCount = 1;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("sdcid", this.attributesdcid);
                jsonObject.put("keyid1", this.sdiattribute.getColumnValues("keyid1", ";"));
                if (keyCount > 1) {
                    jsonObject.put("keyid2", this.sdiattribute.getColumnValues("keyid2", ";"));
                    if (keyCount > 2) {
                        jsonObject.put("keyid3", this.sdiattribute.getColumnValues("keyid3", ";"));
                    }
                }
            }
            catch (Exception attributekeyid3) {
                // empty catch block
            }
            if ("Standard".equalsIgnoreCase(this.config.getProperty("attributemode", "worksheet")) && this.config != null && this.config.getProperty("sourcesdcid").length() > 0) {
                String attributekeyid1 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getColumnValues("keyid1", ";") : "";
                String attributekeyid2 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getColumnValues("keyid2", ";") : "";
                String attributekeyid3 = this.sdiattribute.getRowCount() > 0 ? this.sdiattribute.getColumnValues("keyid3", ";") : "";
                MaintAttribute ma = new MaintAttribute(this.attributesdcid, attributekeyid1, attributekeyid2, attributekeyid3, this.sdiattribute, this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId()), new Logger(this.logContext));
                ma.setRenderMode(MaintAttribute.RenderMode.instance);
                ma.setElementid(this.getElementId());
                PropertyList props = new PropertyList();
                props.setProperty("viewonly", "N");
                props.setProperty("valuesonly", this.config.getProperty("valuesonly", "Y"));
                props.setProperty("sdcid", this.attributesdcid);
                props.setProperty("mode", "Adhoc");
                props.setProperty("scrollable", "Y");
                ma.setElementProperties(props);
                html.append(ma.getHtml());
                html.append("<script>");
                html.append(this.addDropDownDiv());
                html.append("dd_offsetAjust.offset = document.getElementById('multiMaintGridDiv');");
                html.append("dd_offsetAjust.top = 5;");
                html.append("dd_offsetAjust.left = -5;");
                html.append(ma.getScript());
                html.append("if (typeof(sapdateformat) == 'undefined'){");
                html.append("var sapdateformat = ").append(RegexConverter.getValidDateFormatExp(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())))).append(";\n");
                html.append("var decimalSeparator = sapphire.connection.decimalSeparator;");
                html.append("var groupingSeparator = sapphire.connection.groupingSeparator;");
                html.append("}");
                html.append("var sapdateformat4DigitYear = ").append(RegexConverter.getValidDateFormatExp4DigitYear(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())), true, "4")).append(";\n");
                html.append("var sapdateformat2DigitYear = ").append(RegexConverter.getValidDateFormatExp4DigitYear(new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())), true, "2")).append(";\n");
                JSONObject configJson = this.config.toJSONObject();
                String jsonStr = configJson.toString();
                html.append(" \nvar config_" + this.getElementId()).append(" = '").append(jsonStr).append("';");
            } else {
                html.append("<script>");
                html.append(this.getElementId() + "_multidata = ").append(jsonObject.toString()).append(";");
                html.append(this.getElementId() + "_multi = ").append("true").append(";");
            }
            html.append("</script>");
        }
        return html.toString();
    }

    private String addDropDownDiv() {
        StringBuffer htmlDiv = new StringBuffer();
        htmlDiv.append(" var ddDiv = document.getElementById(\"dd_div\");");
        htmlDiv.append("if( ddDiv == null || ddDiv == undefined ) {");
        htmlDiv.append("\nvar elemDiv = document.createElement('div');");
        htmlDiv.append("\nelemDiv.id =\"dd_div\"");
        htmlDiv.append("\nelemDiv.style = \"position:absolute; display:none\"");
        htmlDiv.append("\nelemDiv.setAttribute(\"class\", \"dropdowndiv\");");
        htmlDiv.append("\nelemDiv.onkeydown =\"dd_divKeyPress()\"");
        htmlDiv.append("\nelemDiv.onmouseover =\"this.onblur = null\"");
        htmlDiv.append("\nelemDiv.onmouseout =\"this.onblur = dd_divBlur;\"");
        htmlDiv.append("\n document.body.appendChild(elemDiv);");
        htmlDiv.append("\n }");
        return htmlDiv.toString();
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        this.sdiattribute = null;
        this.instructionOnlyCount = 0;
        if (contents.length() > 0) {
            MaintAttribute.saveAttributeData(contents, this.getActionProcessor(), new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getSapphireConnection().getConnectionId())));
        }
        return super.validateContents("");
    }

    @Override
    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        if (toStatus.equals("Complete") || toStatus.equals("PendingApproval")) {
            return this.validateStateChange(fromStatus, toStatus);
        }
        return "";
    }

    @Override
    public String validateStateChange(String fromStatus, String toStatus) throws SapphireException {
        DataSet dsIncompleteAttribute = new DataSet();
        StringBuffer message = new StringBuffer();
        if (!fromStatus.equals(toStatus) && (toStatus.equals("Complete") || toStatus.equals("PendingApproval"))) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("mandatoryflag", "Y");
            DataSet ds_mandatoryAttributes = this.sdiattribute.getFilteredDataSet(map);
            for (int i = 0; i < ds_mandatoryAttributes.getRowCount(); ++i) {
                String dataType = ds_mandatoryAttributes.getValue(i, "datatype");
                String attributeColumn = "";
                if ("D".equals(dataType) || "O".equals(dataType)) {
                    attributeColumn = "datevalue";
                } else if ("S".equals(dataType)) {
                    attributeColumn = "textvalue";
                } else if ("C".equals(dataType)) {
                    attributeColumn = "clobvalue";
                } else if ("N".equals(dataType)) {
                    attributeColumn = "numericvalue";
                }
                String attributeValue = ds_mandatoryAttributes.getValue(i, attributeColumn);
                if (attributeValue.length() > 0) continue;
                dsIncompleteAttribute.copyRow(ds_mandatoryAttributes, i, 1);
            }
            if (dsIncompleteAttribute.getRowCount() > 0) {
                message.append(" Mandatory attributes in " + dsIncompleteAttribute.getValue(0, "sdcid") + " not filled in for attribute ");
                ArrayList<String> attributes = new ArrayList<String>();
                String attributeIds = "";
                for (int k = 0; k < dsIncompleteAttribute.getRowCount(); ++k) {
                    String attributeid = dsIncompleteAttribute.getValue(k, "attributeid");
                    if (attributes.contains(attributeid)) continue;
                    attributes.add(attributeid);
                    attributeIds = attributeIds + ", " + attributeid;
                }
                if (attributeIds.length() > 0) {
                    message.append(attributeIds.substring(1));
                }
            }
        }
        return message.toString();
    }

    public static EditorStyleField getAttributeField(PageContext pageContext, SDITagInfo sdiInfo, M18NUtil m18nServer, ConnectionInfo connectionInfo, String elementid, String editorstyle, String attributeid, String attributetitle, int datarow, int realrow, DataSet attributedata, boolean defaults, boolean hidden, boolean updateable, boolean viewonly, String changeEvent) throws Exception {
        EditorStyleField esf = new EditorStyleField(pageContext, sdiInfo, connectionInfo.getConnectionId());
        esf.setDatasetname("attribute");
        String atdt = attributedata.getValue(datarow, "datatype", "S");
        String sdcid = attributedata.getValue(datarow, "editsdcid", "");
        String reftypeid = attributedata.getValue(datarow, "editreftypeid", "");
        if (editorstyle != null && editorstyle.length() > 0) {
            esf.setEditorStyleId(editorstyle, sdcid, reftypeid);
        } else {
            esf.setDefaultEditorStyleProperties(atdt, sdcid, reftypeid);
        }
        String a = StringUtil.replaceAll(StringUtil.replaceAll(attributeid, " ", "_"), "/", "_");
        esf.setFieldName(elementid + realrow + "_" + a);
        if (changeEvent != null && changeEvent.length() > 0) {
            esf.setChangeEvent(changeEvent);
        }
        esf.setUseNoNameAttribute(true);
        boolean isDateOnly = atdt.equalsIgnoreCase("o");
        esf.setColumnDefinition(attributetitle, atdt, atdt.equalsIgnoreCase("c") ? 0 : 255, false, isDateOnly);
        if (atdt.equalsIgnoreCase("d") || atdt.equalsIgnoreCase("o")) {
            if (isDateOnly) {
                attributedata.setTimeZoneInsensitive(defaults ? "defaultdatevalue" : "datevalue");
                attributedata.setDateDisplayFormat(defaults ? "defaultdatevalue" : "datevalue", new M18NUtil(pageContext).getDefaultDateOnlyFormat(false));
                esf.getColumn().setProperty("format", "O");
            } else {
                attributedata.setDateDisplayFormat(defaults ? "defaultdatevalue" : "datevalue", new M18NUtil(pageContext).getDefaultDateFormat(true));
            }
        }
        String value = MaintAttribute.getAttributeValue(m18nServer, connectionInfo, attributedata, datarow, defaults, false);
        esf.setFieldValue(value);
        if (hidden) {
            esf.setColumnProperty("mode", "hidden");
        }
        if (viewonly || !updateable && value.length() > 0) {
            esf.setColumnProperty("mode", "readonly");
            if (esf.getColumn().getPropertyList("lookuplink") != null) {
                esf.getColumn().remove("lookuplink");
            }
        }
        return esf;
    }

    public static SDITagInfo createSDIInfo(String rsetid, String sdcid, String keyid1, String keyid2, String keyid3, DataSet attributedata, SDCProcessor sdcp) {
        HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
        DataSet primary = new DataSet();
        PropertyList sdc = sdcp.getPropertyList(sdcid);
        primary.addColumn(sdc.getProperty("keycolid1"), 0);
        if (keyid2 != null && keyid2.length() > 0) {
            primary.addColumn(sdc.getProperty("keycolid2"), 0);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            primary.addColumn(sdc.getProperty("keycolid3"), 0);
        }
        int row = primary.addRow();
        primary.setValue(row, sdc.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            primary.setValue(row, sdc.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            primary.setValue(row, sdc.getProperty("keycolid3"), keyid3);
        }
        querymap.put("primary", new QueryData("primary", primary));
        querymap.put("attribute", new QueryData("attribute", attributedata));
        SDITagInfo sdiinfo = new SDITagInfo(querymap);
        SDIData sdi = new SDIData();
        sdi.setSdcid(sdcid);
        sdiinfo.setSdcid(sdcid);
        sdi.setRsetid(rsetid);
        sdiinfo.setSDIData(sdi);
        return sdiinfo;
    }

    private static void renderButtons(StringBuilder html, String mode, boolean viewOnly, boolean informationMode, PageContext pageContext, String JS_OBJECT, String elementid) {
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        if (!viewOnly) {
            html.append("<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\">");
            html.append("<tbody><tr>");
            String modetext = mode.equals("adhoc") ? "adhoc" : "link";
            Button button = new Button(pageContext);
            button.setAction(JS_OBJECT + ".moveUp('" + elementid + "')");
            button.setTip(tp != null ? tp.translate("Move " + modetext + " attribute up") : "Move " + modetext + " attribute up");
            button.setWidth("40");
            button.setImg("WEB-CORE/images/png/MoveUp.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(pageContext);
            button.setAction(JS_OBJECT + ".moveDown('" + elementid + "')");
            button.setTip(tp != null ? tp.translate("Move " + modetext + " attribute down") : "Move " + modetext + " attribute down");
            button.setWidth("40");
            button.setImg("WEB-CORE/images/png/MoveDown.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(pageContext);
            button.setAction(JS_OBJECT + ".add('" + elementid + "')");
            if (informationMode) {
                button.setText(tp != null ? tp.translate("Add Existing") : "Add Existing");
                button.setTip(tp != null ? tp.translate("Add new existing defined " + modetext + " attribute") : "Add new existing defined " + modetext + " attribute");
            } else {
                button.setText(tp != null ? tp.translate("Add") : "Add");
                button.setTip(tp != null ? tp.translate("Add new " + modetext + " attribute") : "Add new " + modetext + " attribute");
            }
            button.setWidth("75");
            button.setImg("WEB-CORE/images/png/AddRow.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(pageContext);
            button.setAction(JS_OBJECT + ".remove('" + elementid + "')");
            button.setText(tp != null ? tp.translate("Remove") : "Remove");
            button.setTip(tp != null ? tp.translate("Remove " + modetext + " attribute") : "Remove " + modetext + " attribute");
            button.setWidth("75");
            button.setImg("WEB-CORE/images/png/RemoveRow.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            html.append("</tr></tbody>");
            html.append("</table>");
        }
    }

    private void appendWhereClause(StringBuffer sql, boolean worksheet) {
        if (worksheet) {
            sql.append(" AND primarytable." + this.keyColId1 + " =  worksheetsdi.keyid1");
            if (this.keyColId2.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId2 + " =  worksheetsdi.keyid2");
            }
            if (this.keyColId3.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId3 + " =  worksheetsdi.keyid3");
            }
        } else {
            sql.append(" AND primarytable." + this.keyColId1 + " =  worksheetitemsdi.keyid1");
            if (this.keyColId2.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId2 + " =  worksheetitemsdi.keyid2");
            }
            if (this.keyColId3.length() > 0) {
                sql.append(" AND primarytable." + this.keyColId3 + " =  worksheetitemsdi.keyid3");
            }
        }
    }

    private void setAttributeSDCID() {
        String mode = this.config.getProperty("attributemode", "worksheet");
        if (mode.equalsIgnoreCase("standard")) {
            String sourcesdcid = this.config.getProperty("sourcesdcid");
            String attributetype = this.config.getProperty("attributetype", "Primary");
            if (sourcesdcid.length() > 0) {
                if (sourcesdcid.equalsIgnoreCase("sdiworkitem")) {
                    if (attributetype.equalsIgnoreCase("sdiworkitem")) {
                        this.attributesdcid = sourcesdcid;
                    } else if (attributetype.equalsIgnoreCase("sdidata")) {
                        this.attributesdcid = "DataSet";
                    }
                } else {
                    this.attributesdcid = attributetype.equalsIgnoreCase("sdiworkitem") ? "SDIWorkItem" : (attributetype.equalsIgnoreCase("sdidata") ? "DataSet" : sourcesdcid);
                }
            }
        } else {
            String attributesource = this.config.getProperty("attributesource", "Control");
            this.attributesdcid = attributesource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (attributesource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem");
        }
    }
}

