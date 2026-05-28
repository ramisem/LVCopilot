/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
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
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QCBatch
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), "QCBatch");
        worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        worksheetItemOptions.setSupportsDataAvailablity(true);
        worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("tablestyling"));
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/qcbatch.js");
        worksheetItemIncludes.setJSObjectName("qcBatchEditor");
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
        boolean hidecolumnifnull = "Y".equals(this.config.getProperty("hidecolumnifnull"));
        boolean showQCSamples = "Y".equals(this.config.getProperty("showqcsamples", "Y"));
        boolean showUnknownSamples = "Y".equals(this.config.getProperty("showunknownsamples", "Y"));
        String[] requiredColumns = new String[]{"qcbatchsdcid", "s_qcbatchid", "qcmethodid", "qcmethodversionid", "qcbatchstatus", "evaloption"};
        if (source.length() > 0) {
            if (columns == null && (columns = new PropertyListCollection()).size() == 0) {
                try {
                    PropertyList sdcProps = this.getSDCProcessor().getProperties("QCBatch");
                    columns.setJSONString("[{columnid: '" + sdcProps.getProperty("keycolid1") + "', title: 'Id'},{columnid: '" + sdcProps.getProperty("desccol") + "', title: 'Description'}]");
                }
                catch (JSONException e) {
                    throw new SapphireException("Failed to create default column list", e);
                }
            }
            this.populateRequiredColumns(requiredColumns, columns);
            SDIRequest sdiRequest = this.getSDIRequest(source, "QCBatch", columns, null);
            PropertyListCollection qcbatchitemcolumns = this.config.getCollection("qcbatchitemcolumns");
            PropertyListCollection operations = this.config.getCollection("operations");
            if (qcbatchitemcolumns == null || qcbatchitemcolumns.size() == 0) {
                qcbatchitemcolumns = new PropertyListCollection();
                try {
                    qcbatchitemcolumns.setJSONString("[{columnid: 'usersequence', title: 'Seq'},{columnid: 'qcbatchsampletypeid', title: 'Sample Type'},{columnid: 'batchitemtype', title: 'QC Sample Type'},{columnid: 'qcbatchitemdesc', title: 'Description'}]");
                }
                catch (JSONException e) {
                    throw new SapphireException("Failed to create default column list", e);
                }
            }
            sdiRequest.setRequestItem("s_qcbatchitem");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet controlData = sdiData.getDataset("primary");
            String qcMethodId = controlData.getValue(0, "qcmethodid");
            String qcMethodVersionId = controlData.getValue(0, "qcmethodversionid");
            String qcBatchId = controlData.getValue(0, "s_qcbatchid");
            String qcBatchStatus = controlData.getValue(0, "qcbatchstatus");
            String qcBatchSDC = controlData.getValue(0, "qcbatchsdcid", "Sample");
            String tableid = this.getSDCProcessor().getProperty(qcBatchSDC, "tableid");
            String keycolid1 = this.getSDCProcessor().getProperty(qcBatchSDC, "keycolid1");
            String evalOption = controlData.getValue(0, "evaloption");
            String assignedAnalyst = controlData.getValue(0, "assignedanalyst");
            String assignedDept = controlData.getValue(0, "assigneddepartment");
            String instrumentid = controlData.getValue(0, "instrumentid");
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"" + tableStyling + "\" width=\"100%\">");
            html.append("<tr height=\"8\">");
            if (controlData.size() == 0) {
                this.setAvailability("NoData");
            }
            html.append(this.getFormHTML(export, columns, operations, controlData, controlData.size() == 0 ? -1 : 0, Integer.parseInt(this.config.getProperty("formcolumns", "2")), hidecolumnifnull, "s_qcbatchid", "", ""));
            html.append("</table>");
            html.append("<br/>");
            html.append("<BR><div id=\"relationlist_" + this.getElementId() + "\" style=\"display:none\">{");
            html.append("\"qcbatchsdcid\":\"" + qcBatchSDC + "\",");
            html.append("\"qcbatchid\":\"" + qcBatchId + "\",");
            html.append("\"qcmethodid\":\"" + qcMethodId + "\",");
            html.append("\"qcmethodversionid\":\"" + qcMethodVersionId + "\",");
            html.append("\"qcbatchstatus\":\"" + qcBatchStatus + "\",");
            html.append("\"assignedanalyst\":\"" + assignedAnalyst + "\",");
            html.append("\"assigneddepartment\":\"" + assignedDept + "\",");
            html.append("\"instrumentid\":\"" + instrumentid + "\",");
            html.append("\"evaloption\":\"" + evalOption + "\"");
            html.append("}</div>");
            html.append("<BR>");
            DataSet batchItems = new DataSet();
            HashSet<String> skipColumns = new HashSet<String>();
            requiredColumns = new String[]{"s_qcbatchitemid", "qcbatchsampletypeid"};
            this.populateRequiredColumns(requiredColumns, qcbatchitemcolumns);
            if (qcbatchitemcolumns != null && qcbatchitemcolumns.size() > 0 && qcBatchId.length() > 0) {
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ");
                for (int col = 0; col < qcbatchitemcolumns.size(); ++col) {
                    PropertyList colProps = qcbatchitemcolumns.getPropertyList(col);
                    String columnid = colProps.getProperty("columnid");
                    if (columnid.indexOf("(") > -1 && columnid.indexOf(")") > -1) {
                        sql.append(columnid);
                        columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                        colProps.setProperty("columnid", columnid);
                    } else if (columnid.contains(".")) {
                        String[] splitColumn = StringUtil.split(columnid, ".");
                        if ("dataset".equalsIgnoreCase(splitColumn[0]) || "sdidata".equalsIgnoreCase(splitColumn[0])) {
                            sql.append("(SELECT max(sdidata.").append(splitColumn[1]).append(") FROM sdidata WHERE sdidata.s_qcbatchid = s_qcbatchitem.s_qcbatchid AND sdidata.s_qcbatchitemid = s_qcbatchitem.s_qcbatchitemid) ").append(splitColumn[1]);
                        } else if ("sampletype".equalsIgnoreCase(splitColumn[0]) || "batchsampletype".equalsIgnoreCase(splitColumn[0]) || "s_qcbatchsampletype".equalsIgnoreCase(splitColumn[0])) {
                            sql.append("(SELECT s_qcbatchsampletype.").append(splitColumn[1]).append(" FROM s_qcbatchsampletype WHERE s_qcbatchsampletype.s_qcbatchsampletypeid = s_qcbatchitem.qcbatchsampletypeid) ").append(splitColumn[1]);
                        } else if (tableid.equalsIgnoreCase(splitColumn[0]) || qcBatchSDC.equalsIgnoreCase(splitColumn[0])) {
                            sql.append("(SELECT DISTINCT t.").append(splitColumn[1]).append(" FROM ").append(tableid).append(" t, sdidata  WHERE sdidata.sdcid = '").append(qcBatchSDC).append("' AND sdidata.s_qcbatchid = s_qcbatchitem.s_qcbatchid AND sdidata.s_qcbatchitemid = s_qcbatchitem.s_qcbatchitemid AND t.").append(keycolid1).append(" = sdidata.keyid1) ").append(splitColumn[1]);
                        } else if ("linkto".equalsIgnoreCase(splitColumn[0])) {
                            sql.append("(SELECT ").append(splitColumn[1]).append(" FROM s_qcbatchitem qcbi WHERE qcbi.s_qcbatchitemid = s_qcbatchitem.linktoqcbatchitemid ) ").append("linkto" + splitColumn[1]);
                            splitColumn[1] = "linkto" + splitColumn[1];
                        } else if ("qcbatch".equalsIgnoreCase(splitColumn[0]) || "s_qcbatch".equalsIgnoreCase(splitColumn[0])) {
                            sql.append("(SELECT ").append(splitColumn[1]).append(" FROM s_qcbatch qcb WHERE qcb.s_qcbatchid = s_qcbatchitem.s_qcbatchid ) ").append(splitColumn[1]);
                        }
                        colProps.setProperty("columnid", splitColumn[1]);
                    } else {
                        sql.append(columnid);
                    }
                    sql.append(",");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(" FROM s_qcbatchitem");
                sql.append(" WHERE s_qcbatchid = ?");
                if (!showUnknownSamples) {
                    sql.append(" and batchitemtype != 'Unknown'");
                }
                if (!showQCSamples) {
                    sql.append(" and batchitemtype = 'Unknown'");
                }
                sql.append(" order by usersequence");
                batchItems = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{qcBatchId});
            } else {
                batchItems = sdiData.getDataset("s_qcbatchitem");
                batchItems.sort("usersequence");
            }
            for (int col = 0; col < qcbatchitemcolumns.size(); ++col) {
                PropertyList colProps = qcbatchitemcolumns.getPropertyList(col);
                String columnid = colProps.getProperty("columnid");
                if (!colProps.getProperty("show", "Y").equals("N") && (!hidecolumnifnull || batchItems.getColumnValues(columnid, "").length() != 0)) continue;
                skipColumns.add(columnid);
            }
            html.append(this.getTableHTML(export, qcbatchitemcolumns, skipColumns, operations, batchItems, "QC Batch Items", "s_qcbatchid", "", ""));
            return html.toString();
        }
        this.worksheetItemOptions.setRequiresConfig(true, "QC Batch Control requires configuration - click to configure");
        return "";
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

    @Override
    public String validateStateChange(String fromStatus, String toStatus) throws SapphireException {
        boolean reviewMode = "Review".equals(this.config.getProperty("mode"));
        if (!fromStatus.equals(toStatus) && reviewMode && toStatus.equals("Complete")) {
            QueryProcessor qp = this.getQueryProcessor();
            TranslationProcessor tp = this.getTranslationProcessor();
            String sql = "SELECT sdcid, keyid1 FROM worksheetsdi  WHERE worksheetid = ? AND worksheetversionid = ? AND sdcid = ?";
            DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{this.getWorksheetId(), this.getWorksheetVersionId(), "QCBatch"});
            if (ds.getRowCount() > 0) {
                String qcBatchId = ds.getValue(0, "keyid1");
                DataSet dsBatch = qp.getPreparedSqlDataSet("select reviewdisposition, requestedreviewdisposition from s_qcbatch where s_qcbatchid = ?", (Object[])new String[]{qcBatchId});
                if (dsBatch.getValue(0, "reviewdisposition").trim().length() == 0 && dsBatch.getValue(0, "requestedreviewdisposition").trim().length() == 0) {
                    return tp.translate("To complete, please fill in QCBatch \"Requested Review Disposition\".");
                }
            } else {
                throw new SapphireException(tp.translate("Worksheet Item SDI not Found!"));
            }
        }
        return "";
    }

    @Override
    public String validateWorksheetStateChange(String fromStatus, String toStatus) throws SapphireException {
        boolean reviewMode = "Review".equals(this.config.getProperty("mode"));
        if (reviewMode && (toStatus.equals("Approve") || toStatus.equals("Complete"))) {
            QueryProcessor qp = this.getQueryProcessor();
            TranslationProcessor tp = this.getTranslationProcessor();
            ActionProcessor ap = this.getActionProcessor();
            String sql = "SELECT sdcid, keyid1 FROM worksheetsdi  WHERE worksheetid = ? AND worksheetversionid = ? AND sdcid = ?";
            DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{this.getWorksheetId(), this.getWorksheetVersionId(), "QCBatch"});
            if (ds.getRowCount() > 0) {
                String qcBatchId = ds.getValue(0, "keyid1");
                DataSet dsBatch = qp.getPreparedSqlDataSet("select reviewdisposition, requestedreviewdisposition from s_qcbatch where s_qcbatchid = ?", (Object[])new String[]{qcBatchId});
                String requestedReviewDisposition = dsBatch.getValue(0, "requestedreviewdisposition").trim();
                String reviewDisposition = dsBatch.getValue(0, "reviewdisposition").trim();
                if (reviewDisposition.length() == 0 && requestedReviewDisposition.length() == 0) {
                    return tp.translate("To complete please fill in QCBatch \"Requested Review Disposition\".");
                }
                if (requestedReviewDisposition.length() > 0 && !requestedReviewDisposition.equals(reviewDisposition)) {
                    PropertyList props = new PropertyList();
                    props.clear();
                    props.put("sdcid", "QCBatch");
                    props.put("keyid1", qcBatchId);
                    props.put("reviewdisposition", "A".equalsIgnoreCase(requestedReviewDisposition) || "Approved".equalsIgnoreCase(requestedReviewDisposition) ? "Approved" : "Rejected");
                    props.put("qcbatchstatus", "Reviewed");
                    ap.processActionClass("com.labvantage.sapphire.actions.sdi.EditSDI", props, false);
                }
            } else {
                return tp.translate("Worksheet Item SDI not Found!");
            }
        }
        return "";
    }

    @Override
    public boolean readyToSaveHtml() throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = this.getQueryProcessor();
        StringBuffer sql = new StringBuffer("select qc.qcbatchstatus, qc.requestedreviewdisposition from worksheetsdi ws, s_qcbatch qc where ws.keyid1 = qc.s_qcbatchid");
        sql.append(" and ws.sdcid = 'QCBatch' ");
        sql.append(" and ws.worksheetid = ").append(safeSQL.addVar(this.getWorksheetId()));
        sql.append(" and ws.worksheetversionid = ").append(safeSQL.addVar(this.getWorksheetVersionId()));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        boolean save = true;
        if (ds != null && ds.size() > 0) {
            String qcbatchstatus = ds.getValue(0, "qcbatchstatus");
            save = "Reviewed".equalsIgnoreCase(qcbatchstatus);
        }
        return save;
    }
}

