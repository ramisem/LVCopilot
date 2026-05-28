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
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Equipment
extends BaseSDIRelationControl {
    private DataSet equipmentData = new DataSet();

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
                    this.equipmentData = this.fetchControlData("Instrument", "", "", "", "", "", source, sourcesdcid, sourcerelation, workitemid, paramlistid, paramlistversionid, variantid);
                }
                catch (Exception e) {
                    Logger.logError("Could not obtain Equipment data.", e);
                }
            }
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/equipment.js");
        worksheetItemIncludes.setJSObjectName("equipmentEditor");
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
        String source = this.config.getProperty("source", "Control");
        String sourcesdcid = this.config.getProperty("sourcesdcid");
        String sourcerelation = this.config.getProperty("sourcerelation");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (source.length() > 0 && sourcesdcid.length() > 0 && sourcerelation.length() > 0) {
            DataSet controldata;
            String workitemid = this.getDetokenizedConfigProperty("workitemid");
            String paramlistid = "";
            String paramlistversionid = "";
            String variantid = "";
            if (sourcerelation.equalsIgnoreCase("paramlist")) {
                paramlistid = this.getDetokenizedConfigProperty("paramlistid");
                paramlistversionid = this.getDetokenizedConfigProperty("paramlistversionid");
                variantid = this.getDetokenizedConfigProperty("variantid");
            }
            String extraColumnsForSDIData = "";
            String extraColumnsForParamlist = "( SELECT unmanagedflag FROM instrumentmodel WHERE instrumentmodel.instrumentmodelid=sdidatarelation.sourcekeyid1 AND instrumentmodel.instrumenttypeid=sdidatarelation.sourcekeyid2 AND sdidatarelation.sourcesdcid='LV_InstrumentModel') unmanagedmodel,( SELECT unmanagedflag FROM instrumenttype WHERE instrumenttype.instrumenttypeid=sdidatarelation.sourcekeyid1 AND sdidatarelation.sourcesdcid='LV_InstrumentType') unmanagedtype";
            String extraColumnsForWorkitem = "( SELECT unmanagedflag FROM instrumentmodel WHERE instrumentmodel.instrumentmodelid=sdiworkitemrelation.sourcekeyid1 AND instrumentmodel.instrumenttypeid=sdiworkitemrelation.sourcekeyid2 AND sdiworkitemrelation.sourcesdcid='LV_InstrumentModel') unmanagedmodel,( SELECT unmanagedflag FROM instrumenttype WHERE instrumenttype.instrumenttypeid=sdiworkitemrelation.sourcekeyid1 AND sdiworkitemrelation.sourcesdcid='LV_InstrumentType') unmanagedtype";
            String primarysdcid = this.config.getProperty("primarysdcid", "Sample");
            PropertyListCollection primaryColumns = this.config.getCollectionNotNull("primarycolumns");
            if (primarysdcid.length() > 0 && primaryColumns.size() > 0) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(primarysdcid, "tableid");
                String keycolid1 = sdcp.getProperty(primarysdcid, "keycolid1");
                for (int i = 0; i < primaryColumns.size(); ++i) {
                    PropertyList colunm = primaryColumns.getPropertyList(i);
                    String columnid = colunm.getProperty("columnid");
                    extraColumnsForSDIData = extraColumnsForSDIData + ",( SELECT " + tableid + "." + columnid + " FROM " + tableid + " WHERE " + tableid + "." + keycolid1 + "=sdidata.keyid1 ) " + columnid;
                    extraColumnsForParamlist = extraColumnsForParamlist + ",( SELECT " + tableid + "." + columnid + " FROM " + tableid + " WHERE " + tableid + "." + keycolid1 + "=sdidatarelation.keyid1 ) " + columnid;
                    extraColumnsForWorkitem = extraColumnsForWorkitem + ",( SELECT " + tableid + "." + columnid + " FROM " + tableid + " WHERE " + tableid + "." + keycolid1 + "=sdiworkitemrelation.keyid1 ) " + columnid;
                }
            }
            StringBuffer detailColumns = new StringBuffer();
            PropertyListCollection plDetailColumns = this.config.getCollectionNotNull("extracolumns");
            for (int i = 0; i < plDetailColumns.size(); ++i) {
                PropertyList column = plDetailColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0) continue;
                detailColumns.append(detailColumns.length() > 0 ? "," : "").append("instrument." + columnid).append(" instrument_" + columnid);
            }
            String detailJoin = "";
            if (detailColumns.length() > 0) {
                detailJoin = "instrument on instrument.instrumentid";
            }
            if ((controldata = this.fetchControlData("Instrument", extraColumnsForParamlist, extraColumnsForWorkitem, detailColumns.toString(), detailJoin, "", source, sourcesdcid, sourcerelation, workitemid, paramlistid, paramlistversionid, variantid)) == null) {
                this.worksheetItemOptions.setRequiresConfig(true, "ERROR: Failed to configure control - click to edit configuration.");
                return "Control Error";
            }
            DataSet sdidata = new DataSet();
            if (sourcerelation.equalsIgnoreCase("paramlist") || sourcerelation.equalsIgnoreCase("both")) {
                sdidata = this.fetchSDIData(extraColumnsForSDIData, detailColumns.toString(), detailJoin, source, sourcesdcid, workitemid, paramlistid, paramlistversionid, variantid);
            }
            if (controldata != null && controldata.size() > 0 || sdidata != null && sdidata.size() > 0) {
                this.createPopupDiv(html, sourcerelation, "instrumentsource", controldata, sdidata);
                if (!sourcerelation.equalsIgnoreCase("both")) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sourcetype", sourcerelation.equalsIgnoreCase("paramlist") ? "P" : "W");
                    controldata = controldata.getFilteredDataSet(filter);
                }
                if (controldata.size() > 0 || sdidata.size() > 0) {
                    String model;
                    Object type;
                    String[] parts;
                    PropertyListCollection sortby = this.config.getCollectionNotNull("sortby");
                    String sort = "";
                    for (int i = 0; i < sortby.size(); ++i) {
                        sort = sort + "," + sortby.getPropertyList(i).getProperty("columnid") + " " + sortby.getPropertyList(i).getProperty("asc_desc");
                    }
                    sort = sort + ",keyid1, sourcetype d, usersequence, title, tokeyid1 d";
                    DataSet allSamples = new DataSet();
                    allSamples.copyRow(sdidata, -1, 1);
                    allSamples.copyRow(controldata, -1, 1);
                    allSamples.sort(sort);
                    controldata.sort(sort);
                    sdidata.sort(sort);
                    ArrayList<String> samples = new ArrayList<String>();
                    for (int i = 0; i < allSamples.size(); ++i) {
                        String keyid1 = allSamples.getString(i, "keyid1");
                        if (samples.contains(keyid1)) continue;
                        samples.add(keyid1);
                    }
                    ArrayList<String> sdidatarelationInstTypes = new ArrayList<String>();
                    for (int i = 0; i < controldata.size(); ++i) {
                        String type2 = controldata.getValue(i, "relationtype") + ";" + controldata.getValue(i, "sourcekeyid1");
                        if (sdidatarelationInstTypes.contains(type2)) continue;
                        sdidatarelationInstTypes.add(type2);
                    }
                    ArrayList<String> sdidataInstTypes = new ArrayList<String>();
                    for (int i = 0; i < sdidata.size(); ++i) {
                        String type3 = sdidata.getValue(i, "s_instrumenttype") + ";" + sdidata.getValue(i, "s_instrumentmodel");
                        if (sdidataInstTypes.contains(type3)) continue;
                        sdidataInstTypes.add(type3);
                    }
                    controldata.sort("relationtype, sourcekeyid1");
                    boolean showMerged = true;
                    StringBuilder multiTable = new StringBuilder();
                    StringBuilder singleTable = new StringBuilder();
                    multiTable.append("<table class=\"" + tableStyling + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
                    singleTable.append("<table class=\"" + tableStyling + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
                    multiTable.append("<tr>");
                    singleTable.append("<tr>");
                    if (primaryColumns.size() > 0) {
                        for (int i = 0; i < primaryColumns.size(); ++i) {
                            PropertyList column = primaryColumns.getPropertyList(i);
                            String title = column.getProperty("title");
                            if (!column.getProperty("show", "Y").equals("Y")) continue;
                            multiTable.append("<td class=\"title\">" + title + "</td>");
                        }
                    } else {
                        multiTable.append("<td class=\"title\">" + primarysdcid + "</td>");
                    }
                    singleTable.append("<td class=\"title\">" + primarysdcid + "</td>");
                    for (String instType : sdidataInstTypes) {
                        parts = StringUtil.split(instType, ";");
                        type = parts[0];
                        model = parts[1];
                        multiTable.append("<td class=\"title\">");
                        multiTable.append((String)type);
                        multiTable.append(model.length() > 0 && !model.equals(type) ? "<br>(" + model + ")" : "");
                        multiTable.append("*");
                        multiTable.append("</td>");
                        singleTable.append("<td class=\"title\">");
                        singleTable.append("SDIDATA: " + (String)type);
                        singleTable.append(model.length() > 0 && !model.equals(type) ? "<br>(" + model + ")" : "");
                        singleTable.append("*");
                        singleTable.append("</td>");
                    }
                    for (String instType : sdidatarelationInstTypes) {
                        parts = StringUtil.split(instType, ";");
                        type = parts[0];
                        model = parts[1];
                        multiTable.append("<td class=\"title\">");
                        multiTable.append((String)type);
                        multiTable.append(model.length() > 0 && !model.equals(type) ? "<br>(" + model + ")" : "");
                        multiTable.append("</td>");
                        singleTable.append("<td class=\"title\">");
                        singleTable.append((String)type);
                        singleTable.append(model.length() > 0 && !model.equals(type) ? "<br>(" + model + ")" : "");
                        singleTable.append("</td>");
                    }
                    multiTable.append("</tr>");
                    singleTable.append("</tr>");
                    String lastRestOfRow = "";
                    boolean firstSample = true;
                    int samplecount = samples.size();
                    for (String keyid1 : samples) {
                        int i;
                        boolean showTitle;
                        DataSet cells;
                        HashMap<String, String> filter;
                        String model2;
                        String type4;
                        multiTable.append("<tr valign=\"top\">");
                        if (firstSample) {
                            singleTable.append("<tr valign=\"top\">");
                        }
                        if (primaryColumns.size() > 0) {
                            for (int i2 = 0; i2 < primaryColumns.size(); ++i2) {
                                String linkToOperation;
                                PropertyList column = primaryColumns.getPropertyList(i2);
                                if (!column.getProperty("show", "Y").equals("Y")) continue;
                                String display = "";
                                int row = controldata.findRow("keyid1", keyid1);
                                if (row >= 0) {
                                    display = this.getColumnDisplayValue(controldata, row, column, tp);
                                } else {
                                    row = sdidata.findRow("keyid1", keyid1);
                                    display = this.getColumnDisplayValue(sdidata, row, column, tp);
                                }
                                if (!export && (linkToOperation = column.getProperty("linktooperation")).length() > 0) {
                                    display = this.linkToOperation(display, sdidata, row, linkToOperation, operations, keyid1, "", "");
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
                        for (String instType : sdidataInstTypes) {
                            String[] typeParts = StringUtil.split(instType, ";");
                            type4 = typeParts[0];
                            model2 = typeParts[1];
                            filter = new HashMap<String, String>();
                            filter.put("keyid1", keyid1);
                            filter.put("s_instrumenttype", type4);
                            filter.put("s_instrumentmodel", model2.length() > 0 ? model2 : null);
                            cells = sdidata.getFilteredDataSet(filter);
                            restOfRow.append("<td class=\"value\">");
                            if (cells.size() > 0) {
                                showTitle = false;
                                if (cells.size() > 1) {
                                    showTitle = Collections.frequency(Arrays.asList(StringUtil.split(cells.getColumnValues("title", "||"), "||")), cells.getValue(0, "title")) != cells.size();
                                }
                                for (i = 0; i < cells.size(); ++i) {
                                    String title = "";
                                    if (cells.size() > 1 && showTitle) {
                                        String[] parts2 = StringUtil.split(cells.getValue(i, "title"), ";");
                                        title = parts2[1] + " (" + parts2[3] + ") [" + parts2[4] + "]";
                                    }
                                    String instrumentid = cells.getValue(i, "s_instrumentid");
                                    restOfRow.append(i > 0 ? "<hr>" : "");
                                    if (instrumentid.length() > 0) {
                                        restOfRow.append("<span>" + instrumentid).append(title.length() > 0 ? " for " + title : "").append("</span>");
                                        this.renderDetailColumns(tp, plDetailColumns, restOfRow, cells, i, "instrument_");
                                        continue;
                                    }
                                    restOfRow.append("<i>").append(tp.translate("Not assigned")).append(title.length() > 0 ? " for " + title : "").append("</i>");
                                }
                            } else {
                                restOfRow.append("&nbsp;");
                            }
                            restOfRow.append("</td>");
                        }
                        for (String instType : sdidatarelationInstTypes) {
                            String[] typeParts = StringUtil.split(instType, ";");
                            type4 = typeParts[0];
                            model2 = typeParts[1];
                            filter = new HashMap();
                            filter.put("keyid1", keyid1);
                            filter.put("relationtype", type4);
                            filter.put("sourcekeyid1", model2.length() > 0 ? model2 : null);
                            cells = controldata.getFilteredDataSet(filter);
                            restOfRow.append("<td class=\"value\">");
                            if (cells.size() > 0) {
                                showTitle = false;
                                if (cells.size() > 1) {
                                    showTitle = Collections.frequency(Arrays.asList(StringUtil.split(cells.getColumnValues("title", "||"), "||")), cells.getValue(0, "title")) != cells.size();
                                }
                                for (i = 0; i < cells.size(); ++i) {
                                    String sourcetype = cells.getValue(i, "sourcetype");
                                    String title = "";
                                    if (cells.size() > 1 && showTitle) {
                                        String[] parts3 = StringUtil.split(cells.getValue(i, "title"), ";");
                                        title = sourcetype.equals("P") ? parts3[1] + " (" + parts3[3] + ") [" + parts3[4] + "]" : "Test " + parts3[0];
                                    }
                                    boolean unmanaged = "Y".equals(cells.getValue(i, "unmanagedmodel")) || "Y".equals(cells.getValue(i, "unmanagedtype"));
                                    String amount = cells.getValue(i, "amount");
                                    String requiredamount = cells.getValue(i, "requiredamount");
                                    String instrumentid = cells.getValue(i, "tokeyid1");
                                    restOfRow.append(i > 0 ? "<hr>" : "");
                                    if (unmanaged) {
                                        if (amount.length() > 0) {
                                            restOfRow.append(amount + " used").append(!amount.equals(requiredamount) ? " (Expected " + requiredamount + ")" : "");
                                        } else {
                                            restOfRow.append("Requires " + requiredamount);
                                        }
                                        if (title.length() <= 0) continue;
                                        restOfRow.append(" for " + title);
                                        continue;
                                    }
                                    if (instrumentid.length() > 0) {
                                        restOfRow.append("<span>" + instrumentid).append(title.length() > 0 ? " for " + title : "").append("</span>");
                                        this.renderDetailColumns(tp, plDetailColumns, restOfRow, cells, i, "instrument_");
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
                        if (unavailable != null && unavailable.getRowCount() == controldata.size() + sdidata.size()) {
                            this.setAvailability("Unavailable");
                        }
                    }
                } else {
                    this.setAvailability("NoData");
                    html.append((CharSequence)this.renderNoAvailability(primarysdcid, primaryColumns, "Instrument", "equipment", tableStyling, fullwidth, this.getTranslationProcessor()));
                }
            } else {
                this.setAvailability("NoData");
                html.append((CharSequence)this.renderNoAvailability(primarysdcid, primaryColumns, "Instrument", "equipment", tableStyling, fullwidth, this.getTranslationProcessor()));
            }
        } else {
            this.worksheetItemOptions.setRequiresConfig(true, "Equipment Control requires configuration - click to configure");
            return "";
        }
        return html.toString();
    }

    private DataSet fetchSDIData(String extraDataSetColumns, String extraColumns, String extraJoin, String source, String sourcesdcid, String workitemid, String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        String dicols = extraColumns + extraDataSetColumns + (extraColumns.length() > 0 || extraDataSetColumns.length() > 0 ? "," : "") + " sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset, sdidata.usersequence, sdidata.s_instrumentid, sdidata.availabilityflag, paramlist.s_instrumenttype, paramlist.s_instrumentmodel, " + Equipment.concatFields(this.getSapphireConnection().isOracle(), "sdidata.keyid1", "sdidata.paramlistid", "sdidata.paramlistversionid", "sdidata.variantid", "sdidata.dataset") + " title";
        String wstable = source.equalsIgnoreCase("Worksheet") ? "worksheetsdi" : "worksheetitemsdi";
        StringBuilder sql = new StringBuilder();
        ArrayList<String> args = new ArrayList<String>();
        sql.append("SELECT " + dicols);
        sql.append(" FROM sdidata ");
        if (extraJoin.length() > 0) {
            sql.append("left outer join " + extraJoin + " = sdidata.s_instrumentid");
        }
        sql.append(", sdiworkitem, sdiworkitemitem, paramlist ");
        sql.append((CharSequence)this.getItemJoins(sourcesdcid, wstable, args));
        sql.append(" AND paramlist.s_instrumenttype is not null  AND sdiworkitem.sdcid = sdiworkitemitem.sdcid AND sdiworkitem.keyid1 = sdiworkitemitem.keyid1 AND sdiworkitem.keyid2 = sdiworkitemitem.keyid2 AND sdiworkitem.keyid3 = sdiworkitemitem.keyid3  AND sdiworkitem.workitemid = sdiworkitemitem.workitemid AND sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance  AND sdiworkitemitem.sdcid = sdidata.sdcid AND sdiworkitemitem.keyid1 = sdidata.keyid1 AND sdiworkitemitem.keyid2 = sdidata.keyid2 AND sdiworkitemitem.keyid3 = sdidata.keyid3 AND sdiworkitemitem.itemsdcid = 'ParamList'  AND sdiworkitemitem.itemkeyid1 = sdidata.paramlistid AND sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid AND sdiworkitemitem.itemkeyid3 = sdidata.variantid AND sdiworkitemitem.iteminstance = sdidata.dataset  AND sdidata.paramlistid = paramlist.paramlistid AND sdidata.paramlistversionid=paramlist.paramlistversionid AND sdidata.variantid = paramlist.variantid");
        if (workitemid.length() > 0) {
            sql.append(" AND sdiworkitem.workitemid = ?");
            args.add(workitemid);
        }
        if (paramlistid.length() > 0) {
            sql.append(" AND sdidata.paramlistid=?");
            args.add(paramlistid);
        }
        if (paramlistversionid.length() > 0) {
            sql.append(" AND sdidata.paramlistversionid=?");
            args.add(paramlistversionid);
        }
        if (variantid.length() > 0) {
            sql.append(" AND sdidata.variantid=?");
            args.add(variantid);
        }
        DataSet sdidata = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), args.toArray());
        return sdidata;
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
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
            DataSet ds_mandatory = this.equipmentData.getFilteredDataSet(map);
            for (int i = 0; i < ds_mandatory.getRowCount(); ++i) {
                String tokeyid1 = ds_mandatory.getValue(i, "tokeyid1");
                if (tokeyid1.length() != 0) continue;
                dsIncomplete.copyRow(ds_mandatory, i, 1);
            }
            if (dsIncomplete.getRowCount() > 0) {
                message.append(" Mandatory Equipment not filled in for relation type ");
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
}

