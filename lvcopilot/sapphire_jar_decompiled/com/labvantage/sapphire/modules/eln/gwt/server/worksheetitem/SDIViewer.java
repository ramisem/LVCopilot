/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIViewer
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("tablestyling"));
        if (sourcesdcid.length() > 0) {
            worksheetItemOptions.setSupportsDataAvailablity(true);
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), sourcesdcid);
            worksheetItemOptions.setSupportsQuerySDIs(this.config.getProperty("source").equalsIgnoreCase("query"));
            worksheetItemOptions.setQuery(this.config.getProperty("queryid"), this.config.getCollection("params"), this.config.getProperty("queryfrom"), this.config.getProperty("querywhere"));
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/sdiviewer.js");
        worksheetItemIncludes.setJSObjectName("sdiViewerEditor");
    }

    public void createSDIViewerDiv(StringBuffer html, DataSet controldata) {
        StringBuilder qcbatchids = new StringBuilder();
        for (int rowid = 0; rowid < controldata.size(); ++rowid) {
            String value = controldata.getString(rowid, "qcbatchid");
            if (qcbatchids.indexOf(value) >= 0) continue;
            qcbatchids.append(";").append(controldata.getString(rowid, "qcbatchid"));
        }
        if (qcbatchids.length() > 0) {
            html.append("<div id=\"sdiviewerlist_" + this.getElementId() + "\" style=\"display:none\">{");
            html.append("\"qcbatchid\":\"" + qcbatchids.substring(1) + "\",");
            html.append("\"sourcesdcid\":\"" + this.config.getProperty("sourcesdcid") + "\"");
            html.append("}</div>");
        }
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(true);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML(false);
    }

    private String getHTML(boolean export) throws SapphireException {
        StringBuffer html = new StringBuffer();
        String source = this.config.getProperty("source", "Control");
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        PropertyListCollection columns = this.config.getCollectionNotNull("columns");
        PropertyListCollection operations = this.config.getCollectionNotNull("operations");
        PropertyListCollection sortby = this.config.getCollectionNotNull("sortby");
        String sort = "";
        for (int i = 0; i < sortby.size(); ++i) {
            sort = sort + "," + sortby.getPropertyList(i).getProperty("columnid") + " " + sortby.getPropertyList(i).getProperty("asc_desc");
        }
        if (source.length() > 0 && sourcesdcid.length() > 0) {
            String layoutStyle;
            int col;
            PropertyList column;
            DataSet controlData;
            PropertyList sdcProps = this.getSDCProcessor().getProperties(sourcesdcid);
            M18NUtil m18NUtil = new M18NUtil(this.getSapphireConnection());
            PropertyListCollection secondaryColumns = new PropertyListCollection();
            SDIRequest sdiRequest = this.getSDIRequest(source, sourcesdcid, columns, secondaryColumns, true);
            sdiRequest.setUseRSetOrderBy(true);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet dataSet = controlData = sdiData == null ? null : sdiData.getDataset("primary");
            if (controlData == null) {
                this.worksheetItemOptions.setRequiresConfig(true, "Unable to fetch data for the SDIViewer control - check your column definitions.");
                return "";
            }
            if (sort.length() > 0) {
                controlData.sort(sort);
            }
            if (controlData.size() > 0 && controlData.isValidColumn("qcbatchid")) {
                this.createSDIViewerDiv(html, controlData);
            }
            if (sourcesdcid.equals("SDIWorkItem") && controlData.size() > 0 && secondaryColumns.size() > 0) {
                PropertyList secondarySdcProps;
                String secondarySdcid = controlData.getValue(0, "sdcid");
                DataSet secondaryRows = this.fetchSecondarySDIs(secondarySdcid, secondarySdcProps = this.getSDCProcessor().getProperties(secondarySdcid), secondaryColumns, sdiData.getRsetid());
                if (secondaryRows.size() > 0) {
                    for (int i = 0; i < secondaryColumns.size(); ++i) {
                        column = secondaryColumns.getPropertyList(i);
                        String columnid = column.getProperty("columnid");
                        controlData.addColumn(columnid, secondaryRows.getColumnType(columnid));
                    }
                }
                for (col = 0; col < secondaryRows.getColumnCount(); ++col) {
                    HashMap<String, String> find = new HashMap<String, String>();
                    int keycols = Integer.parseInt(secondarySdcProps.getProperty("keycolumns"));
                    for (int controlRow = 0; controlRow < controlData.size(); ++controlRow) {
                        int secondaryRow;
                        find.put(secondarySdcProps.getProperty("keycolid1"), controlData.getString(controlRow, "keyid1"));
                        if (keycols > 1) {
                            find.put(secondarySdcProps.getProperty("keycolid2"), controlData.getString(controlRow, "keyid2"));
                        }
                        if (keycols > 2) {
                            find.put(secondarySdcProps.getProperty("keycolid3"), controlData.getString(controlRow, "keyid3"));
                        }
                        if ((secondaryRow = secondaryRows.findRow(find)) < 0) continue;
                        for (int i = 0; i < secondaryRows.getColumnCount(); ++i) {
                            String columnid = secondaryRows.getColumnId(i);
                            controlData.setObject(controlRow, columnid, secondaryRows.getObject(secondaryRow, columnid));
                        }
                    }
                }
                this.getDAMProcessor().clearRSet(sdiData.getRsetid());
            }
            if ((layoutStyle = this.config.getProperty("style", "Adaptive")).equals("Adaptive")) {
                layoutStyle = controlData.size() == 1 ? "Form" : "Table";
            }
            boolean hidecolumnifnull = "Y".equals(this.config.getProperty("hidecolumnifnull"));
            boolean groupcommonvalues = "Y".equals(this.config.getProperty("groupcommonvalues"));
            for (col = 0; col < columns.size(); ++col) {
                PropertyListCollection sdccolumns;
                PropertyList columnDef;
                column = columns.getPropertyList(col);
                String columnid = column.getProperty("columnid");
                String format = column.getProperty("format");
                if (format.length() <= 0) continue;
                boolean isDateOnly = false;
                if (sdcProps != null && (columnDef = (sdccolumns = this.getSDCProcessor().getColumns(sourcesdcid)).getPropertyList(columnid)) != null && "Y".equals(columnDef.getProperty("timezoneindependent"))) {
                    isDateOnly = true;
                }
                if (isDateOnly) {
                    controlData.setDateDisplayFormat(columnid, ElementUtil.getDateFormat("O" + format, true, m18NUtil, m18NUtil.getTimezone()));
                    continue;
                }
                controlData.setDateDisplayFormat(columnid, ElementUtil.getDateFormat("O" + format, true, m18NUtil, m18NUtil.getTimezone()));
            }
            if (layoutStyle.equals("Table")) {
                HashSet<String> skipColumns = new HashSet<String>();
                PropertyListCollection commonColumns = new PropertyListCollection();
                for (int col2 = 0; col2 < columns.size(); ++col2) {
                    PropertyList column2 = columns.getPropertyList(col2);
                    String columnid = column2.getProperty("columnid");
                    if (columnid.indexOf(" ") > 0) {
                        columnid = RequestParser.parseAlias(columnid);
                    }
                    if (column2.getProperty("show", "Y").equals("N") || hidecolumnifnull && controlData.getColumnValues(columnid, "").length() == 0) {
                        skipColumns.add(columnid);
                        continue;
                    }
                    if (column2.getProperty("linktooperation").length() != 0) continue;
                    HashSet<String> temp = new HashSet<String>();
                    temp.addAll(Arrays.asList(StringUtil.split(controlData.getColumnValues(columnid, ";"), ";")));
                    if (!groupcommonvalues || temp.size() != 1) continue;
                    commonColumns.add(column2);
                    skipColumns.add(columnid);
                }
                if (groupcommonvalues && commonColumns.size() > 0) {
                    int formCols = Integer.parseInt(this.config.getProperty("formcolumns", "2"));
                    String tableStyling = this.getTableStylingProperty("tablestyling", export, "lv_elntable_form ");
                    html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
                    html.append("<tr height=\"8\">");
                    html.append(this.getFormHTML(export, commonColumns, operations, controlData, 0, formCols, "Y".equals(this.config.getProperty("hidecolumnifnull")), sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3")));
                    html.append("</table>");
                }
                html.append(this.getTableHTML(export, columns, skipColumns, operations, controlData, sdcProps.getProperty("plural"), sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3")));
            } else {
                int formCols = Integer.parseInt(this.config.getProperty("formcolumns", "2"));
                String tableStyling = this.getTableStylingProperty("tablestyling", export, "lv_elntable_form ");
                html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
                html.append("<tr height=\"8\">");
                if (controlData.size() > 0) {
                    for (int i = 0; i < controlData.size(); ++i) {
                        html.append(this.getFormHTML(export, columns, operations, controlData, i, formCols, "Y".equals(this.config.getProperty("hidecolumnifnull")), sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3")));
                    }
                } else {
                    this.setAvailability("NoData");
                    html.append(this.getFormHTML(export, columns, operations, controlData, -1, formCols, "Y".equals(this.config.getProperty("hidecolumnifnull")), sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3")));
                }
                html.append("</table>");
            }
        } else {
            this.worksheetItemOptions.setRequiresConfig(true, "SDI Viewer Control requires configuration - click to configure");
            return "";
        }
        return html.toString();
    }

    private DataSet fetchSecondarySDIs(String sdcid, PropertyList sdcProps, PropertyListCollection columns, String rsetid) throws SapphireException {
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer requesetItems = new StringBuffer();
        for (int i = 0; i < columns.size(); ++i) {
            if (columns.getPropertyList(i).getProperty("columnid").length() <= 0) continue;
            requesetItems.append(",").append(columns.getPropertyList(i).getProperty("columnid"));
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem("primary[" + requesetItems.substring(1) + "]");
        sdiRequest.setQueryFrom(tableid + ", sdiworkitem, rsetitems");
        String where = "sdiworkitemid = rsetitems.keyid1 AND rsetitems.rsetid='" + rsetid + "' AND rsetitems.sdcid='SDIWorkItem'";
        where = where + " AND sdiworkitem.sdcid='" + sdcid + "' AND " + tableid + "." + sdcProps.getProperty("keycolid1") + " = sdiworkitem.keyid1";
        where = where + (keycols > 1 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid2") + " = sdiworkitem.keyid2" : "");
        where = where + (keycols > 2 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid3") + " = sdiworkitem.keyid3" : "");
        sdiRequest.setQueryWhere(where);
        sdiRequest.setQueryOrderBy("1");
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        DataSet secondaryData = sdiData.getDataset("primary");
        return secondaryData;
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }
}

