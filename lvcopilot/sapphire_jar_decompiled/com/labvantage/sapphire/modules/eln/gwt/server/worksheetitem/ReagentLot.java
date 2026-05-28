/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReagentLot
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), "LV_ReagentLot");
        worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        worksheetItemOptions.setSupportsDataAvailablity(true);
        worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("tablestyling"));
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/reagentlot.js");
        worksheetItemIncludes.setJSObjectName("reagentLotEditor");
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
        String source = this.config.getProperty("source", "Control");
        String tableStyling = this.getTableStylingProperty("tablestyling", export, "lv_elntable_form ");
        if (source.length() > 0) {
            boolean hidecolumnifnull = "Y".equals(this.config.getProperty("hidecolumnifnull"));
            boolean showcontainers = "Y".equals(this.config.getProperty("showcontainers", "Y"));
            boolean showreagentparams = "Y".equals(this.config.getProperty("showreagentparams", "Y"));
            String reagentparamstitle = this.config.getProperty("reagentparamstitle", "Consumable Params");
            String containerstitle = this.config.getProperty("containerstitle", "Containers");
            String[] requiredColumns = new String[]{"reagentlotid", "reagentlotdesc", "reagenttypeid", "reagenttypeversionid", "reagentstatus", "containertypeid", "amountinitial", "amountinitialunits", "amountinitialunitstype"};
            PropertyListCollection columns = this.config.getCollection("columns");
            PropertyListCollection operations = this.config.getCollection("operations");
            if (columns == null && (columns = new PropertyListCollection()).size() == 0) {
                try {
                    PropertyList sdcProps = this.getSDCProcessor().getProperties("LV_ReagentLot");
                    columns.setJSONString("[{columnid: '" + sdcProps.getProperty("keycolid1") + "', title: 'Id'},{columnid: '" + sdcProps.getProperty("desccol") + "', title: 'Description'}]");
                }
                catch (JSONException e) {
                    throw new SapphireException("Failed to create default column list", e);
                }
            }
            this.populateRequiredColumns(requiredColumns, columns);
            SDIRequest sdiRequest = this.getSDIRequest(source, "LV_ReagentLot", columns, null);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet controlData = sdiData.getDataset("primary");
            String reagentLotid = "";
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
            html.append("<tr height=\"8\">");
            if (controlData.size() == 0) {
                this.setAvailability("NoData");
            } else {
                reagentLotid = controlData.getValue(0, "reagentlotid");
                this.createPopupDiv(html, controlData);
            }
            html.append(this.getFormHTML(export, columns, operations, controlData, controlData.size() == 0 ? -1 : 0, Integer.parseInt(this.config.getProperty("formcolumns", "2")), hidecolumnifnull, "reagentlotid", "", ""));
            html.append("</table>");
            if (showcontainers) {
                html.append("<br/>");
                html.append(this.getContainerHtml(export, reagentLotid, hidecolumnifnull, containerstitle));
            }
            if (showreagentparams) {
                html.append("<br/>");
                html.append(this.getReagentParamsHtml(export, reagentLotid, hidecolumnifnull, reagentparamstitle));
            }
            return html.toString();
        }
        this.worksheetItemOptions.setRequiresConfig(true, "Consumable Lot Control requires configuration - click to configure");
        return "";
    }

    private String getContainerHtml(boolean export, String reagentLotid, boolean hidecolumnifnull, String containerstitle) throws SapphireException {
        StringBuilder html = new StringBuilder();
        PropertyListCollection containercolumns = this.config.getCollection("containercolumns");
        PropertyListCollection operations = this.config.getCollection("operations");
        if (containercolumns == null || containercolumns.size() == 0) {
            containercolumns = new PropertyListCollection();
            try {
                containercolumns.setJSONString("[{columnid: 'trackitemid', title: 'TrackItemId'},{columnid: 'trackitemstatus', title: 'Status'},{columnid: 'qtycurrent', title: 'Amount'},{columnid: 'qtyunits', title: 'Unit'}]");
            }
            catch (JSONException e) {
                throw new SapphireException("Failed to create default column list", e);
            }
        }
        DataSet trackItemDS = new DataSet();
        HashSet<String> skipColumns = new HashSet<String>();
        String[] requiredColumns = new String[]{"trackitemid"};
        this.populateRequiredColumns(requiredColumns, containercolumns);
        if (containercolumns.size() > 0) {
            String columnid;
            PropertyList colProps;
            int col;
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            for (col = 0; col < containercolumns.size(); ++col) {
                colProps = containercolumns.getPropertyList(col);
                columnid = colProps.getProperty("columnid");
                if (columnid.contains("(") && columnid.contains(")")) {
                    sql.append(columnid);
                    columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                    colProps.setProperty("columnid", columnid);
                } else if (!columnid.contains(".")) {
                    sql.append(columnid);
                }
                sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" FROM trackitem");
            sql.append(" WHERE linksdcid = 'LV_ReagentLot'");
            sql.append(" and linkkeyid1 = ?");
            sql.append(" order by trackitemid");
            trackItemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{reagentLotid});
            for (col = 0; col < containercolumns.size(); ++col) {
                colProps = containercolumns.getPropertyList(col);
                columnid = colProps.getProperty("columnid");
                if (!colProps.getProperty("show", "Y").equals("N") && (!hidecolumnifnull || trackItemDS.getColumnValues(columnid, "").length() != 0)) continue;
                skipColumns.add(columnid);
            }
            html.append("<font class=viewtitletext><strong>" + containerstitle + "</strong></font><br>");
            if (trackItemDS != null && trackItemDS.size() > 0) {
                html.append(this.getTableHTML(export, containercolumns, skipColumns, operations, trackItemDS, containerstitle, "reagentlotid", "", ""));
            } else if (this.isTemplate()) {
                html.append("<tr>");
                for (col = 0; col < containercolumns.size(); ++col) {
                    html.append("<td class=\"value\">").append(containercolumns.getPropertyList(col).getProperty("columnid")).append("</td>");
                }
                html.append("</tr>");
            } else {
                String noSDIMsg = this.config.getProperty("nocontaineravailablemessage");
                String msg = noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : this.getTranslationProcessor().translate("No " + containerstitle + " found");
                html.append("<tr><td colspan=\"").append(containercolumns.size()).append("\" class=\"value\">&nbsp&nbsp&nbsp ").append(msg).append(" </td></tr>");
            }
        }
        return html.toString();
    }

    private String getReagentParamsHtml(boolean export, String reagentLotid, boolean hidecolumnifnull, String reagentparamstitle) throws SapphireException {
        StringBuilder html = new StringBuilder();
        PropertyListCollection reagentparamscolumns = this.config.getCollection("reagentparamscolumns");
        PropertyListCollection operations = this.config.getCollection("operations");
        if (reagentparamscolumns == null || reagentparamscolumns.size() == 0) {
            reagentparamscolumns = new PropertyListCollection();
            try {
                reagentparamscolumns.setJSONString("[{columnid: 'PARAMID', title: 'Param ID'},{columnid: 'DISPLAYVALUE', title: 'Value'},{columnid: 'DISPLAYUNITS', title: 'Unit'}]");
            }
            catch (JSONException e) {
                throw new SapphireException("Failed to create default column list", e);
            }
        }
        DataSet paramsDS = new DataSet();
        HashSet<String> skipColumns = new HashSet<String>();
        String[] requiredColumns = new String[]{"paramid", "paramtype"};
        this.populateRequiredColumns(requiredColumns, reagentparamscolumns);
        if (reagentparamscolumns.size() > 0) {
            String columnid;
            PropertyList colProps;
            int col;
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            for (col = 0; col < reagentparamscolumns.size(); ++col) {
                colProps = reagentparamscolumns.getPropertyList(col);
                columnid = colProps.getProperty("columnid");
                if (columnid.contains("(") && columnid.contains(")")) {
                    sql.append(columnid);
                    columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                    colProps.setProperty("columnid", columnid);
                } else if (!columnid.contains(".")) {
                    sql.append(columnid);
                }
                sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" FROM sdidataitem");
            sql.append(" WHERE sdcid = 'LV_ReagentLot'");
            sql.append(" and keyid1 = ?");
            sql.append(" order by usersequence");
            paramsDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{reagentLotid});
            ReagentUtil.populateMatchingDataItemsValue(paramsDS, reagentLotid, this.getQueryProcessor());
            for (col = 0; col < reagentparamscolumns.size(); ++col) {
                colProps = reagentparamscolumns.getPropertyList(col);
                columnid = colProps.getProperty("columnid");
                if (!colProps.getProperty("show", "Y").equals("N") && (!hidecolumnifnull || paramsDS.getColumnValues(columnid, "").length() != 0)) continue;
                skipColumns.add(columnid);
            }
            html.append("<font class=viewtitletext><strong>" + reagentparamstitle + "</strong></font><br>");
            if (paramsDS != null && paramsDS.size() > 0) {
                html.append(this.getTableHTML(export, reagentparamscolumns, skipColumns, operations, paramsDS, reagentparamstitle, "reagentlotid", "", ""));
            } else if (this.isTemplate()) {
                html.append("<tr>");
                for (col = 0; col < reagentparamscolumns.size(); ++col) {
                    html.append("<td class=\"value\">").append(reagentparamscolumns.getPropertyList(col).getProperty("columnid")).append("</td>");
                }
                html.append("</tr>");
            } else {
                String noSDIMsg = this.config.getProperty("noreagentparamavailablemessage");
                String msg = noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : this.getTranslationProcessor().translate("No " + reagentparamstitle + " found");
                html.append("<tr><td colspan=\"").append(reagentparamscolumns.size()).append("\" class=\"value\">&nbsp&nbsp&nbsp ").append(msg).append(" </td></tr>");
            }
        }
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    private void populateRequiredColumns(String[] requiredColumns, PropertyListCollection columns) {
        for (int r = 0; r < requiredColumns.length; ++r) {
            boolean exists = false;
            for (int col = 0; col < columns.size(); ++col) {
                PropertyList colProps = columns.getPropertyList(col);
                String columnid = colProps.getProperty("columnid");
                if (!requiredColumns[r].equalsIgnoreCase(columnid)) continue;
                exists = true;
                break;
            }
            if (exists) continue;
            PropertyList colProps = new PropertyList();
            colProps.setProperty("columnid", requiredColumns[r]);
            colProps.setProperty("show", "N");
            columns.add(colProps);
        }
    }

    private void createPopupDiv(StringBuffer html, DataSet controldata) {
        String reagentLotid = controldata.getValue(0, "reagentlotid");
        html.append("<div id=\"relationlist_" + this.getElementId() + "\" style=\"display:none\">{");
        html.append("\"reagentlotid\":\"" + reagentLotid + "\"");
        html.append("} </div>");
    }

    @Override
    public String validateStateChange(String fromStatus, String toStatus) {
        return "";
    }

    @Override
    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        boolean reviewMode = "Review".equals(this.config.getProperty("mode"));
        if (!fromStatus.equals(toStatus) && reviewMode) {
            SafeSQL safeSQL = new SafeSQL();
            QueryProcessor qp = this.getQueryProcessor();
            TranslationProcessor tp = this.getTranslationProcessor();
            ActionProcessor ap = this.getActionProcessor();
            if (toStatus.equals("Complete") || toStatus.equals("Approve") || toStatus.equals("Reject")) {
                StringBuffer sql = new StringBuffer("select worksheetsdi.keyid1,s_sample.s_sampleid from worksheetsdi,s_sample");
                sql.append(" where worksheetsdi.sdcid= 'LV_ReagentLot'");
                sql.append(" and worksheetsdi.worksheetid=").append(safeSQL.addVar(this.getWorksheetId()));
                sql.append(" and worksheetsdi.worksheetversionid=").append(safeSQL.addVar(this.getWorksheetVersionId()));
                sql.append(" and s_sample.reagentlotid =worksheetsdi.keyid1");
                sql.append(" and s_sample.samplestatus in ('Completed','InProgress')");
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                HashMap<String, String> actionProps = new HashMap<String, String>();
                String reagentlotid = "";
                if (ds != null && ds.size() > 0) {
                    String sampleid = ds.getString(0, "s_sampleid");
                    reagentlotid = ds.getString(0, "keyid1", "");
                    actionProps.put("sdcid", "Sample");
                    actionProps.put("keyid1", sampleid);
                    actionProps.put("samplestatus", "Reviewed");
                    actionProps.put("reviewdisposition", toStatus.equals("Reject") ? "Rejected" : "Approved");
                    ap.processAction("EditSDI", "1", actionProps);
                } else {
                    safeSQL.reset();
                    sql.setLength(0);
                    sql.append("select keyid1 from worksheetsdi");
                    sql.append(" where sdcid= 'LV_ReagentLot'");
                    sql.append(" and worksheetid=").append(safeSQL.addVar(this.getWorksheetId()));
                    sql.append(" and worksheetversionid=").append(safeSQL.addVar(this.getWorksheetVersionId()));
                    DataSet dsWs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (dsWs != null && dsWs.size() > 0) {
                        reagentlotid = dsWs.getString(0, "keyid1", "");
                    }
                }
                actionProps.clear();
                actionProps.put("sdcid", "LV_ReagentLot");
                actionProps.put("keyid1", reagentlotid);
                actionProps.put("reagentstatus", toStatus.equals("Reject") ? "Rejected" : "Active");
                ap.processAction("EditSDI", "1", actionProps);
            } else if (toStatus.equals("Cancelled")) {
                safeSQL.reset();
                StringBuilder sql = new StringBuilder("select keyid1 from worksheetsdi");
                sql.append(" where sdcid= 'LV_ReagentLot'");
                sql.append(" and worksheetid=").append(safeSQL.addVar(this.getWorksheetId()));
                sql.append(" and worksheetversionid=").append(safeSQL.addVar(this.getWorksheetVersionId()));
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    String reagentlotid = ds.getString(0, "keyid1");
                    HashMap<String, String> actionProps = new HashMap<String, String>();
                    actionProps.put("sdcid", "LV_ReagentLot");
                    actionProps.put("keyid1", reagentlotid);
                    actionProps.put("reagentstatus", "Cancelled");
                    ap.processAction("EditSDI", "1", actionProps);
                }
            }
        }
        return "";
    }
}

