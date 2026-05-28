/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.BaseSDIRelationControl;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Reagents
extends BaseSDIRelationControl {
    private DataSet reagentData = new DataSet();

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        if (sourcesdcid.length() > 0) {
            String source = this.config.getProperty("source", "Control");
            String sourcerelation = this.config.getProperty("sourcerelation");
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), sourcesdcid);
            worksheetItemOptions.setSupportsDataAvailablity(true);
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
            worksheetItemOptions.setHasExportHTML(this.isPublishTableStylingDifferent("displaystyle"));
            if (source.length() > 0 && sourcesdcid.length() > 0 && sourcerelation.length() > 0) {
                worksheetItemOptions.setSupportsQuerySDIs(source.equalsIgnoreCase("query"));
                try {
                    String workitemid = this.getDetokenizedConfigProperty("workitemid");
                    String paramlistid = "";
                    String paramlistversionid = "";
                    String variantid = "";
                    if (sourcerelation.equalsIgnoreCase("paramlist")) {
                        paramlistid = this.getDetokenizedConfigProperty("paramlistid");
                        paramlistversionid = this.getDetokenizedConfigProperty("paramlistversionid");
                        variantid = this.getDetokenizedConfigProperty("variantid");
                    }
                    this.reagentData = this.fetchControlData("Reagent", "", "", "", "", "", source, sourcesdcid, sourcerelation, workitemid, paramlistid, paramlistversionid, variantid);
                }
                catch (Exception e) {
                    Logger.logError("Could not obtain reagent data.", e);
                }
            }
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/reagents.js");
        worksheetItemIncludes.setJSObjectName("reagentsEditor");
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
        String tableStyling = this.getTableStylingProperty("displaystyle", export, "lv_elntable_grid  ");
        boolean fullwidth = this.config.getProperty("fullwidth", "Y").equals("Y");
        boolean collapseDuplicates = this.config.getProperty("collapseduplicates", "N").equals("Y");
        PropertyListCollection operations = this.config.getCollectionNotNull("operations");
        TranslationProcessor tp = this.getTranslationProcessor();
        String source = this.config.getProperty("source", "Control");
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        String sourcerelation = this.config.getProperty("sourcerelation");
        if (source.length() > 0 && sourcesdcid.length() > 0 && sourcerelation.length() > 0) {
            DataSet controldata;
            String columnid;
            int i;
            String workitemid = this.getDetokenizedConfigProperty("workitemid");
            String paramlistid = "";
            String paramlistversionid = "";
            String variantid = "";
            if (sourcerelation.equalsIgnoreCase("paramlist")) {
                paramlistid = this.getDetokenizedConfigProperty("paramlistid");
                paramlistversionid = this.getDetokenizedConfigProperty("paramlistversionid");
                variantid = this.getDetokenizedConfigProperty("variantid");
            }
            String extraplcolumns = "( SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.sourcekeyid1     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "sourcekeyid2") + "     ) unmanagedflag ";
            String extrawicolumns = "( SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.sourcekeyid1 AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "sourcekeyid2") + " ) unmanagedflag";
            String titleColumn = this.config.getProperty("titlecolumn");
            if (titleColumn.length() > 0) {
                extraplcolumns = extraplcolumns + ", ( SELECT " + titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.sourcekeyid1     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "sourcekeyid2") + "     ) titlecolumn ";
                extrawicolumns = extrawicolumns + ", ( SELECT " + titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.sourcekeyid1     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "sourcekeyid2") + "     ) titlecolumn ";
            }
            String primarysdcid = this.config.getProperty("primarysdcid", "Sample");
            PropertyListCollection primaryColumns = this.config.getCollectionNotNull("primarycolumns");
            if (primarysdcid.length() > 0 && primaryColumns.size() > 0) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(primarysdcid, "tableid");
                String keycolid1 = sdcp.getProperty(primarysdcid, "keycolid1");
                for (i = 0; i < primaryColumns.size(); ++i) {
                    PropertyList colunm = primaryColumns.getPropertyList(i);
                    columnid = colunm.getProperty("columnid");
                    extraplcolumns = extraplcolumns + ",( SELECT " + tableid + "." + columnid + " FROM " + tableid + " WHERE " + tableid + "." + keycolid1 + "=sdidatarelation.keyid1 ) " + columnid;
                    extrawicolumns = extrawicolumns + ",( SELECT " + tableid + "." + columnid + " FROM " + tableid + " WHERE " + tableid + "." + keycolid1 + "=sdiworkitemrelation.keyid1 ) " + columnid;
                }
            }
            StringBuffer detailColumns = new StringBuffer();
            StringBuffer detailTIColumns = new StringBuffer();
            PropertyListCollection plDetailColumns = this.config.getCollectionNotNull("extracolumns");
            for (i = 0; i < plDetailColumns.size(); ++i) {
                PropertyList column = plDetailColumns.getPropertyList(i);
                columnid = column.getProperty("columnid");
                if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0) continue;
                if (columnid.contains("trackitem.") && columnid.contains("reagentlot_")) {
                    detailTIColumns.append(detailTIColumns.length() > 0 ? "," : "").append(columnid);
                    continue;
                }
                detailColumns.append(detailColumns.length() > 0 ? "," : "").append("reagentlot." + columnid).append(" reagentlot_" + columnid);
            }
            String detailJoin = "";
            String detailTIJoin = "";
            if (detailColumns.length() > 0) {
                detailJoin = "reagentlot on reagentlot.reagentlotid";
            }
            if (detailTIColumns.length() > 0) {
                detailTIJoin = "trackitem on trackitem.trackitemid";
                detailColumns.append(detailColumns.length() > 0 ? "," : "").append(detailTIColumns);
            }
            if ((controldata = this.fetchControlData("Reagent", extraplcolumns, extrawicolumns, detailColumns.toString(), detailJoin, detailTIJoin, source, sourcesdcid, sourcerelation, workitemid, paramlistid, paramlistversionid, variantid)) == null) {
                this.worksheetItemOptions.setRequiresConfig(true, "ERROR: Failed to configure control - click to edit configuration.");
                return "Control Error";
            }
            if (controldata != null && controldata.size() > 0) {
                this.createPopupDiv(html, sourcerelation, "reagentsource", controldata, null);
                if (!sourcerelation.equalsIgnoreCase("both")) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sourcetype", sourcerelation.equalsIgnoreCase("paramlist") ? "P" : "W");
                    controldata = controldata.getFilteredDataSet(filter);
                }
                if (controldata != null && controldata.size() > 0) {
                    PropertyListCollection sortby = this.config.getCollectionNotNull("sortby");
                    String sort = "";
                    for (int i2 = 0; i2 < sortby.size(); ++i2) {
                        sort = sort + "," + sortby.getPropertyList(i2).getProperty("columnid") + " " + sortby.getPropertyList(i2).getProperty("asc_desc");
                    }
                    sort = sort + ",keyid1, sourcetype d, usersequence, title, tokeyid1 d";
                    controldata.sort(sort);
                    ArrayList<DataSet> samples = controldata.getGroupedDataSets("keyid1");
                    boolean showMerged = true;
                    StringBuilder multiTable = new StringBuilder();
                    StringBuilder singleTable = new StringBuilder();
                    multiTable.append("<table class=\"" + tableStyling + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
                    singleTable.append("<table class=\"" + tableStyling + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
                    multiTable.append("<tr>");
                    singleTable.append("<tr>");
                    if (primaryColumns.size() > 0) {
                        for (int i3 = 0; i3 < primaryColumns.size(); ++i3) {
                            PropertyList column = primaryColumns.getPropertyList(i3);
                            String title = column.getProperty("title");
                            if (!column.getProperty("show", "Y").equals("Y")) continue;
                            multiTable.append("<td class=\"title\">" + title + "</td>");
                        }
                    } else {
                        multiTable.append("<td class=\"title\">" + primarysdcid + "</td>");
                    }
                    singleTable.append("<td class=\"title\">" + primarysdcid + "</td>");
                    HashMap<String, String> columnTitles = new HashMap<String, String>();
                    ArrayList<String> reagentTypes = new ArrayList<String>();
                    for (int i4 = 0; i4 < controldata.size(); ++i4) {
                        String reagentType = controldata.getValue(i4, "relationtype");
                        columnTitles.put(reagentType, controldata.getValue(i4, "titlecolumn", reagentType));
                        if (reagentTypes.contains(reagentType)) continue;
                        reagentTypes.add(reagentType);
                    }
                    for (String reagentType : reagentTypes) {
                        multiTable.append("<td class=\"title\">");
                        multiTable.append(columnTitles.get(reagentType));
                        multiTable.append("</td>");
                        singleTable.append("<td class=\"title\">");
                        singleTable.append(columnTitles.get(reagentType));
                        singleTable.append("</td>");
                    }
                    multiTable.append("</tr>");
                    singleTable.append("</tr>");
                    String lastRestOfRow = "";
                    boolean firstSample = true;
                    int samplecount = samples.size();
                    for (DataSet sample : samples) {
                        String keyid1 = sample.getValue(0, "keyid1");
                        multiTable.append("<tr>");
                        if (firstSample) {
                            singleTable.append("<tr>");
                        }
                        if (primaryColumns.size() > 0) {
                            for (int i5 = 0; i5 < primaryColumns.size(); ++i5) {
                                String linkToOperation;
                                PropertyList column = primaryColumns.getPropertyList(i5);
                                if (!column.getProperty("show", "Y").equals("Y")) continue;
                                String display = this.getColumnDisplayValue(sample, 0, column, tp);
                                if (!export && (linkToOperation = column.getProperty("linktooperation")).length() > 0) {
                                    display = this.linkToOperation(display, sample, 0, linkToOperation, operations, keyid1, "", "");
                                }
                                multiTable.append("<td class=\"value\">" + display + "</td>");
                            }
                        } else {
                            multiTable.append("<td class=\"value\">" + keyid1 + "</td>");
                        }
                        if (firstSample) {
                            singleTable.append("<td class=\"value\"><i>" + (samplecount == 1 ? keyid1 : "All " + primarysdcid + "s") + "</i></td>");
                        }
                        StringBuilder restOfRow = new StringBuilder();
                        for (String reagentType : reagentTypes) {
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("keyid1", keyid1);
                            filter.put("relationtype", reagentType);
                            DataSet cells = controldata.getFilteredDataSet(filter);
                            restOfRow.append("<td class=\"value\">");
                            if (cells.size() > 0) {
                                boolean showTitle = false;
                                if (cells.size() > 1) {
                                    showTitle = Collections.frequency(Arrays.asList(StringUtil.split(cells.getColumnValues("title", "||"), "||")), cells.getValue(0, "title")) != cells.size();
                                }
                                for (int i6 = 0; i6 < cells.size(); ++i6) {
                                    String amountdisplay;
                                    String sourcetype = cells.getValue(i6, "sourcetype");
                                    String title = "";
                                    if (cells.size() > 1 && showTitle) {
                                        String[] parts = StringUtil.split(cells.getValue(i6, "title"), ";");
                                        title = sourcetype.equals("P") ? parts[1] + " (" + parts[3] + ") [" + parts[4] + "]" : "Test " + parts[0];
                                    }
                                    boolean unmanaged = "Y".equals(cells.getValue(i6, "unmanagedflag"));
                                    String amount = cells.getValue(i6, "amount");
                                    String amountunits = cells.getValue(i6, "amountunits");
                                    String amountunitstype = cells.getValue(i6, "amountunitstype");
                                    String requiredamount = cells.getValue(i6, "requiredamount");
                                    String requiredamountunits = cells.getValue(i6, "requiredamountunits");
                                    String requiredamountunitstype = cells.getValue(i6, "requiredamountunitstype");
                                    String lotid = cells.getValue(i6, "tokeyid1");
                                    String containerid = cells.getValue(i6, "refkeyid1");
                                    String string = amount.length() > 0 ? amount + (amountunitstype.equals("C") ? " Containers" : amountunits) : (amountdisplay = "");
                                    String requireddisplay = requiredamount.length() > 0 ? requiredamount + (requiredamountunitstype.equals("C") ? " Containers" : requiredamountunits) : "";
                                    restOfRow.append(i6 > 0 ? "<br>" : "");
                                    if (unmanaged) {
                                        if (amount.length() > 0) {
                                            restOfRow.append(amountdisplay + " used").append(!amountdisplay.equals(requireddisplay) ? " (Expected " + requireddisplay + ")" : "");
                                        } else if (requireddisplay.length() > 0) {
                                            restOfRow.append("Requires " + requireddisplay);
                                        } else {
                                            restOfRow.append("<i>" + tp.translate("Amount not specified") + "</i>");
                                        }
                                        if (title.length() <= 0) continue;
                                        restOfRow.append(" for " + title);
                                        continue;
                                    }
                                    if (lotid.length() > 0) {
                                        if (amount.length() > 0) {
                                            restOfRow.append(amountdisplay + " of ");
                                        }
                                        restOfRow.append(lotid).append(containerid.length() > 0 && !lotid.startsWith("V") ? " (" + containerid + ")" : "").append(title.length() > 0 ? " for " + title : "");
                                        if (requireddisplay.length() > 0 && !requireddisplay.equals(amountdisplay)) {
                                            restOfRow.append("<br>&nbsp;&nbsp;(Expected " + requireddisplay + ")");
                                        }
                                        this.renderDetailColumns(tp, plDetailColumns, restOfRow, cells, i6, "reagentlot_");
                                        continue;
                                    }
                                    restOfRow.append("<i>").append(tp.translate("Not assigned")).append(title.length() > 0 ? " for " + title : "").append("</i>");
                                }
                            } else {
                                restOfRow.append("&nbsp;");
                            }
                            restOfRow.append("</td>");
                        }
                        if (lastRestOfRow.length() > 0) {
                            showMerged &= lastRestOfRow.equals(restOfRow.toString());
                        }
                        if (firstSample) {
                            singleTable.append((CharSequence)restOfRow);
                        }
                        lastRestOfRow = restOfRow.toString();
                        multiTable.append((CharSequence)restOfRow);
                        multiTable.append("</tr>");
                        firstSample = false;
                    }
                    multiTable.append("</table>");
                    singleTable.append("</tr></table>");
                    if (showMerged && collapseDuplicates) {
                        html.append((CharSequence)singleTable);
                    } else {
                        html.append((CharSequence)multiTable);
                    }
                    if (sourcerelation.equalsIgnoreCase("paramlist")) {
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("availabilityflag", "N");
                        DataSet unavailable = controldata.getFilteredDataSet(filterMap);
                        if (unavailable != null && unavailable.getRowCount() == controldata.size()) {
                            this.setAvailability("Unavailable");
                        }
                    }
                } else {
                    this.setAvailability("NoData");
                    html.append((CharSequence)this.renderNoAvailability(primarysdcid, primaryColumns, "Consumable", "consumables", tableStyling, fullwidth, this.getTranslationProcessor()));
                }
            } else {
                this.setAvailability("NoData");
                html.append((CharSequence)this.renderNoAvailability(primarysdcid, primaryColumns, "Consumable", "consumables", tableStyling, fullwidth, this.getTranslationProcessor()));
            }
        } else {
            this.worksheetItemOptions.setRequiresConfig(true, "Consumable Control requires configuration - click to configure");
            return "";
        }
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    @Override
    public boolean readyToSaveHtml() throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = this.getQueryProcessor();
        StringBuffer sql = new StringBuffer("select reagentlot.reagentstatus from worksheetsdi,reagentlot where worksheetsdi.keyid1=reagentlot.reagentlotid");
        sql.append(" and worksheetsdi.sdcid= 'LV_ReagentLot'");
        sql.append(" and worksheetsdi.worksheetid=").append(safeSQL.addVar(this.getWorksheetId()));
        sql.append(" and worksheetsdi.worksheetversionid=").append(safeSQL.addVar(this.getWorksheetVersionId()));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        boolean save = true;
        if (ds != null && ds.size() > 0) {
            String reagentstatus = ds.getString(0, "reagentstatus", "");
            save = reagentstatus.equalsIgnoreCase("Active");
        }
        return save;
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
        DataSet dsIncomplete = new DataSet();
        StringBuffer message = new StringBuffer();
        if (!fromStatus.equals(toStatus) && (toStatus.equals("Complete") || toStatus.equals("PendingApproval"))) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("mandatoryflag", "Y");
            map.put("availabilityflag", "Y");
            DataSet ds_mandatory = this.reagentData.getFilteredDataSet(map);
            for (int i = 0; i < ds_mandatory.getRowCount(); ++i) {
                String tokeyid1 = ds_mandatory.getValue(i, "tokeyid1");
                if (tokeyid1.length() != 0) continue;
                dsIncomplete.copyRow(ds_mandatory, i, 1);
            }
            if (dsIncomplete.getRowCount() > 0) {
                message.append(" Mandatory Consumable Lot not filled in for relation type ");
                ArrayList<String> items = new ArrayList<String>();
                String itemIds = "";
                for (int k = 0; k < dsIncomplete.getRowCount(); ++k) {
                    String relationType = dsIncomplete.getValue(k, "relationtype");
                    if (items.contains(relationType)) continue;
                    items.add(relationType);
                    itemIds = itemIds + ", " + relationType;
                }
                if (itemIds.length() > 0) {
                    message.append(itemIds.substring(1));
                }
            }
        }
        return message.toString();
    }

    private String resolveCurrentVersionCluase(String tableid, String versionid) {
        return "coalesce( NULLIF( " + tableid + "." + versionid + ", '' )," + this.getCurrentRT() + "," + this.getMaxProvisionalRT() + ")";
    }

    private String getCurrentRT() {
        return "(select rt1.reagenttypeversionid from reagenttype rt1 where  rt1.reagenttypeid=reagenttype.reagenttypeid and rt1.versionstatus='C')";
    }

    private String getMaxProvisionalRT() {
        return "(select cast(max(cast(rt2.reagenttypeversionid as integer)) as varchar(40)) from reagenttype rt2 where rt2.reagenttypeid=reagenttype.reagenttypeid and rt2.versionstatus='P')";
    }
}

