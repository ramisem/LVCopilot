/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Workorder
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), "WorkOrderSDC");
        worksheetItemOptions.setSupportsDataAvailablity(true);
        worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("tablestyling"));
        String source = this.config.getProperty("source", "Control");
        worksheetItemOptions.setSupportsQuerySDIs(source.equalsIgnoreCase("query"));
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/workorder.js");
        worksheetItemIncludes.setJSObjectName("workorderEditor");
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
        String tableStyling = this.getTableStylingProperty("tablestyling", export, "lv_elntable_form ");
        String source = this.config.getProperty("source", "Control");
        PropertyListCollection columns = this.config.getCollection("columns");
        PropertyListCollection operations = this.config.getCollection("operations");
        if (source.length() > 0) {
            if (columns == null) {
                columns = new PropertyListCollection();
            }
            SDIRequest sdiRequest = this.getSDIRequest(source, "WorkOrderSDC", columns, null);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet controlData = sdiData.getDataset("primary");
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
            html.append("<tr height=\"8\">");
            if (controlData.size() == 0) {
                this.setAvailability("NoData");
            }
            html.append(this.getFormHTML(export, columns, operations, controlData, controlData.size() == 0 ? -1 : 0, Integer.parseInt(this.config.getProperty("formcolumns", "2")), this.config.getProperty("hidecolumnifnull").equals("Y"), "workorderid", "", ""));
            html.append("</table>");
            return html.toString();
        }
        this.worksheetItemOptions.setRequiresConfig(true, "Workorder Control requires configuration - click to configure");
        return "";
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    @Override
    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        if (toStatus.equals("Approve") || toStatus.equals("Complete")) {
            QueryProcessor qp = this.getQueryProcessor();
            TranslationProcessor tp = this.getTranslationProcessor();
            ActionProcessor ap = this.getActionProcessor();
            String sql = "SELECT sdcid, keyid1 FROM worksheetsdi  WHERE worksheetid = ? AND worksheetversionid = ? AND sdcid = ?";
            DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{this.getWorksheetId(), this.getWorksheetVersionId(), "WorkOrderSDC"});
            if (ds.getRowCount() > 0) {
                String workorderid = ds.getValue(0, "keyid1");
                DataSet dsWO = qp.getPreparedSqlDataSet("select disposition, requesteddispositionflag from workorder where workorderid = ?", (Object[])new String[]{workorderid});
                String requestedDisposition = dsWO.getValue(0, "requesteddispositionflag", "").trim();
                String disposition = dsWO.getValue(0, "disposition").trim();
                if (disposition.length() == 0 && requestedDisposition.length() == 0 && this.isRequestedDispositionConfigured()) {
                    return tp.translate("To complete please fill in WorkOrder \"Requested Disposition\".");
                }
                PropertyList props = new PropertyList();
                props.clear();
                props.put("sdcid", "WorkOrderSDC");
                props.put("keyid1", workorderid);
                props.put("workorderstatus", "Complete");
                props.put("disposition", "R".equalsIgnoreCase(requestedDisposition) ? "Rejected" : "Accepted");
                ap.processActionClass("com.labvantage.sapphire.actions.sdi.EditSDI", props, false);
            } else {
                return tp.translate("Worksheet Item SDI not Found!");
            }
        }
        return "";
    }

    private boolean isRequestedDispositionConfigured() {
        PropertyList requesteddispositionflagPL;
        boolean flag = false;
        PropertyListCollection columns = this.config.getCollection("columns");
        if (columns != null && (requesteddispositionflagPL = columns.find("columnid", "requesteddispositionflag")) != null && requesteddispositionflagPL.size() > 0) {
            flag = true;
        }
        return flag;
    }

    @Override
    public DataSet getQuerySDIs(String filterWhere) throws SapphireException {
        this.config.setProperty("sourcesdcid", "WorkOrderSDC");
        return super.getQuerySDIs(filterWhere);
    }
}

