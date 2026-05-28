/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.instrument;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.ReagentInstrumentCommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class InstrumentDataEntryUtil
extends ReagentInstrumentCommonUtil {
    private static final String dataEntryPrefix = "instrumentinventory_";
    private static final String workitemInstrument = "workitem";
    private static final String workitemInstrumentLinkToPL = "wiinslinktopl";
    private final String fieldName_qcbatchid = "qcbatchid";
    private final String fieldName_reagentlotid = "reagentlotid";
    private final String fieldName_reagentlotrecipeitemid = "reagentlotrecipeitemid";
    private String instrumentsource = "";
    private final String fieldName_qcbatchinstrumentid = "s_qcbatchinstrumentid";
    private final String fieldName_instrumenttypeid = "instrumenttypeid";
    private final String fieldName_instrumentmodelid = "instrumentmodelid";
    private final String fieldName_instrumentid = "instrumentid";
    private final String fieldName_sourceflag = "sourceflag";
    private final String fieldName_instrumentcount = "instrumentcount";
    private String viewonlyOption;
    private String requiredcounttitle;
    private String actualcounttitle;
    private String instrumentselectionmode;
    private String ajaxClass;
    private String whereClauseOriginal;
    private String fldSize;
    private String maxFldSize;
    private boolean showModelinHeader = false;
    private final String noInstruments = this.tp.translate("No rows found");

    public InstrumentDataEntryUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request) {
        super(pageContext, pageinfo, request, "instrumentsource");
        this.instrumentsource = pageinfo.getProperty("instrumentsource", "");
        this.extraColumFromcluase = " instrument ";
        this.extraColumWherecluase = " instrumentid=? ";
        this.table1 = "instrument";
        this.table2 = "instrumentmodel";
    }

    private void populateCommonProperties() {
        PropertyList inventoryInfo = this.pagedata.getPropertyList("InventoryInfo");
        this.viewonlyOption = this.pagedata.getProperty("viewonly", "No");
        this.requiredcounttitle = this.tp.translate(inventoryInfo.getProperty("requiredcounttitle", ""));
        this.actualcounttitle = this.tp.translate(inventoryInfo.getProperty("actualcounttitle", ""));
        this.instrumentselectionmode = inventoryInfo.getProperty("instrumentselectionmode", "Lookup");
        this.ajaxClass = inventoryInfo.getProperty("AjaxClass", "");
        this.whereClauseOriginal = inventoryInfo.getProperty("restrictivewhereclause", "");
        this.showModelinHeader = inventoryInfo.getProperty("showmodelinheader", "N").equalsIgnoreCase("Y");
        HashMap instrumentSdcProps = this.sdcProcessor.getSDCProperties("Instrument");
        String usabeSize = (String)instrumentSdcProps.get("keyidusablesize");
        this.extraColumns = this.pagedata.getCollection("extracolumns");
        this.needToShowExtraColumns = this.isExtraColumnAvailable(this.extraColumns);
        this.fldSize = usabeSize;
        this.maxFldSize = usabeSize;
        this.fieldValueHM.put("adhocinstrumentpageid", this.promptForAdhocInstrument);
        this.fieldValueHM.put("adhoctablename", this.adhoctablename);
    }

    public String getHtml() {
        String keyid1 = this.pagedata.getProperty("keyid1", "");
        if (ReagentUtil.isInputEmpty(keyid1)) {
            return this.tp.translate("No keyid1 found in the request");
        }
        this.fieldValueHM.clear();
        this.populateCommonProperties();
        if (this.instrumentsource.equalsIgnoreCase(this.reagentlotrecipe)) {
            return this.getHtmlForReagentLotIsntrument();
        }
        if (this.instrumentsource.equalsIgnoreCase(this.qcbatch)) {
            return this.getHtmlForQCBatchInstrument();
        }
        return this.getHtmlForSDIRelation();
    }

    private String getHtmlForReagentLotIsntrument() {
        String mandatoryColumns;
        StringBuffer htmlData = new StringBuffer();
        PropertyListCollection reagentlotEquipment = null;
        String reagentlotstageid = this.pagedata.getProperty("reagentlotstageid", "");
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String dsSelectClause = mandatoryColumns = " reagentlotid,reagentlotrecipeitemid,instrumenttype instrumenttypeid,instrumentmodelid,instrumentid,amount instrumentcount,amountrecommended requiredinstrumentcount";
        reagentlotEquipment = reagentlotEquipment == null ? new PropertyListCollection() : reagentlotEquipment;
        String[] hiddenColumnArr = new String[reagentlotEquipment.size()];
        int hiddenIndx = 0;
        for (int i = 0; i < reagentlotEquipment.size(); ++i) {
            PropertyList columnProps = reagentlotEquipment.getPropertyList(i);
            String columnid = columnProps.getProperty("columnid", "");
            String columnAlias = this.getColumnAlias(columnid);
            if (columnAlias.equals("")) {
                String title = columnProps.getProperty("title", "");
                title = title.replaceAll(" ", "");
                columnid = columnid + " " + title;
                columnProps.setProperty("columnid", columnid);
            }
            String columnMode = columnProps.getProperty("mode", "Visible");
            if (!mandatoryColumns.contains(columnid)) {
                dsSelectClause = dsSelectClause + "," + columnid;
            }
            if (columnMode.equalsIgnoreCase("Hidden")) {
                hiddenColumnArr[hiddenIndx] = columnid;
                ++hiddenIndx;
                continue;
            }
            this.increaseFixColumnsWidth(columnProps);
            visibleColumns.add(columnProps);
        }
        String sdcid = this.pagedata.getProperty("sdcid");
        String keyid1 = this.pagedata.getProperty("keyid1");
        String selectedDatasets = this.pagedata.getProperty("selectedds", "");
        String selectedWis = this.pagedata.getProperty("selectedwi", "");
        String postfixIndexWithUnderscore = "";
        String postfixIndexWithUnderscore_temp = "";
        String requiredcount = "";
        String mode = "";
        boolean isUnmanaged = false;
        String tableid = this.instrumentsource;
        String lookupImage = "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg";
        String noInstruments = this.tp.translate("No instrument found");
        int tabindex = 1;
        htmlData.append("<input type=hidden name=\"selectedds\" id=\"selectedds\" value=\"" + selectedDatasets + "\">\n");
        htmlData.append("<input type=hidden name=\"selectedwi\" id=\"selectedwi\" value=\"" + selectedWis + "\">\n");
        htmlData.append("<input type=hidden name=\"viewonly\" id=\"viewonly\" value=\"" + this.viewonlyOption + "\">\n");
        htmlData.append("<input type=hidden name=\"reagentlotstageid\" id=\"reagentlotstageid\" value=\"" + reagentlotstageid + "\">\n");
        htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
        htmlData.append("<table id=\"dataEntryTable\" class=\"maintform_table_blue\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
        DataSet controldata = new DataSet();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.reset();
        sql.append("SELECT " + dsSelectClause + " FROM " + tableid);
        sql.append(" WHERE reagentlotid = " + safeSQL.addVar(keyid1) + " ");
        if (reagentlotstageid != null && reagentlotstageid.length() > 0) {
            sql.append(" AND reagentlotstageid=" + safeSQL.addVar(reagentlotstageid));
        }
        sql.append(" AND recipeitemtype='Instrument'");
        sql.append(" order by USERSEQUENCE");
        controldata = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        ArrayList<DataSet> instrumentTypes = controldata.getGroupedDataSets("instrumenttypeid,instrumentmodelid");
        StringBuffer strRLTIJson = new StringBuffer();
        if (controldata.getRowCount() > 0) {
            if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_Lookup)) {
                htmlData.append("<input type=hidden name=instrumentinventory_instrumentselectionmode id=instrumentinventory_instrumentselectionmode value=\"20\">\n");
            } else {
                htmlData.append("<input type=hidden name=instrumentinventory_instrumentselectionmode id=instrumentinventory_instrumentselectionmode value=\"0\">\n");
            }
            htmlData.append("<tr>");
            htmlData.append(this.getVisibleColumnHtml(visibleColumns));
            ArrayList<String> uniqueTypeModel = new ArrayList<String>();
            for (int r = 0; r < controldata.getRowCount(); ++r) {
                String insTypeID = controldata.getString(r, "instrumenttypeid", "");
                String insModel = controldata.getString(r, "instrumentmodelid", "");
                String typeModel = insTypeID + this.specialDelimer + insModel;
                if (uniqueTypeModel.contains(typeModel)) continue;
                String showModelInHeaderRow = "";
                if (this.showModelinHeader && insModel.length() > 0) {
                    showModelInHeaderRow = " (" + insModel + ") ";
                }
                htmlData.append(this.getHeaderColumnHtml(insTypeID + showModelInHeaderRow));
                uniqueTypeModel.add(typeModel);
            }
            htmlData.append("</tr>");
            controldata.sort("reagentlotid");
            ArrayList<DataSet> consumablelots = controldata.getGroupedDataSets("reagentlotid");
            int selectedDSIndx = -1;
            HashMap<String, String> hm = new HashMap<String, String>();
            for (DataSet consumablelot : consumablelots) {
                ++selectedDSIndx;
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                String reagentlotid = consumablelot.getString(0, "reagentlotid", "");
                hm.clear();
                hm.put("reagentlotid", reagentlotid);
                DataSet filteredRows = controldata.getFilteredDataSet(hm);
                htmlData.append("<tr>");
                htmlData.append(this.getVisibleColumnValueHtml(visibleColumns, consumablelot, 0));
                htmlData.append("<input type=hidden name=\"instrumentinventory_reagentlotid_" + selectedDSIndx + "\" id=\"" + dataEntryPrefix + "reagentlotid" + "_" + selectedDSIndx + "\" value='" + reagentlotid + "'>");
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    htmlData.append("<input type=hidden name=\"instrumentinventory_" + columnid + "_" + selectedDSIndx + "\" id=\"" + dataEntryPrefix + columnid + "_" + selectedDSIndx + "\" value='" + consumablelot.getValue(0, columnid, "") + "'>");
                }
                int insTypeIndex = -1;
                for (String typeModel : uniqueTypeModel) {
                    postfixIndexWithUnderscore_temp = "" + selectedDSIndx + "_" + ++insTypeIndex;
                    postfixIndexWithUnderscore = "" + selectedDSIndx + "_" + insTypeIndex;
                    String[] typeModelArr = StringUtil.split(typeModel, this.specialDelimer);
                    String instrumenttypeid = typeModelArr[0];
                    String instrumentmodelid = typeModelArr[1];
                    hm.clear();
                    hm.put("instrumenttypeid", instrumenttypeid);
                    hm.put("instrumentmodelid", instrumentmodelid.trim().length() > 0 ? instrumentmodelid : null);
                    DataSet dsInstrument = filteredRows.getFilteredDataSet(hm);
                    if (dsInstrument.getRowCount() == 0) {
                        htmlData.append("<td style=\"" + this.css_EmptyContentCellStyle + "\"></td>");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_sourceofinstrument_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "sourceofinstrument_" + postfixIndexWithUnderscore + "\" value=\"skip\">");
                        continue;
                    }
                    htmlData.append("<input type=hidden name=\"instrumentinventory_sourceofinstrument_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "sourceofinstrument_" + postfixIndexWithUnderscore + "\" value='L'>");
                    boolean amountEditable = true;
                    String whereClause = this.whereClauseOriginal;
                    whereClause = StringUtil.replaceAll(whereClause, "[instrumenttype]", instrumenttypeid);
                    whereClause = StringUtil.replaceAll(whereClause, "[instrumentmodelid]", instrumentmodelid);
                    isUnmanaged = this.isUnmanaged(this.qp, instrumenttypeid, instrumentmodelid);
                    StringBuffer jsonStr = this.getJsonStr(instrumenttypeid, instrumentmodelid);
                    htmlData.append("<td nowrap style=\"" + this.css_ContentCellStyle + "\" valign='top'>");
                    htmlData.append("<table><tr><td><table >");
                    htmlData.append("");
                    htmlData.append("<input type=hidden name=\"instrumentinventory_instrumenttypeid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumenttypeid_" + postfixIndexWithUnderscore + "\" value=\"" + instrumenttypeid + "\">");
                    htmlData.append("<input type=hidden name=\"instrumentinventory_instrumentdscount_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentdscount_" + postfixIndexWithUnderscore + "\" value=\"" + dsInstrument.size() + "\">");
                    for (int findrow = 0; findrow < dsInstrument.size(); ++findrow) {
                        postfixIndexWithUnderscore = postfixIndexWithUnderscore_temp + "_" + findrow;
                        String primaryColumnValue1 = dsInstrument.getValue(findrow, "reagentlotid", "");
                        String primaryColumnValue2 = dsInstrument.getValue(findrow, "reagentlotrecipeitemid", "");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_primarykey1_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "primarykey1_" + postfixIndexWithUnderscore + "\"  value='" + primaryColumnValue1 + "'>");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_primarykey2_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "primarykey2_" + postfixIndexWithUnderscore + "\"  value='" + primaryColumnValue2 + "'>");
                        if (isUnmanaged) {
                            requiredcount = dsInstrument.getValue(findrow, "requiredinstrumentcount", "");
                            String count = dsInstrument.getValue(findrow, "instrumentcount", "");
                            String countInDB = dsInstrument.getValue(findrow, "instrumentcount", "");
                            if (count.length() > 0) {
                                mode = "Edit";
                            } else {
                                mode = "Add";
                                count = requiredcount;
                            }
                            htmlData.append(this.getUnManagedInstrumentCellHtml("requiredcount", "count", postfixIndexWithUnderscore, requiredcount, count, countInDB, viewonly, amountEditable, tabindex++, dataEntryPrefix));
                        } else {
                            instrumentmodelid = dsInstrument.getValue(findrow, "instrumentmodelid", "");
                            String instrumentid = dsInstrument.getValue(findrow, "instrumentid", "");
                            mode = instrumentid.length() > 0 ? "Edit" : "Add";
                            htmlData.append("<tr><td>");
                            if (findrow > 0 && this.needToShowExtraColumns) {
                                htmlData.append("<hr>");
                            }
                            htmlData.append(this.getManageInstrumentCellHtml(instrumenttypeid, instrumentmodelid, instrumentid, viewonly, tabindex++, postfixIndexWithUnderscore, jsonStr.toString(), whereClause, dataEntryPrefix, "", ""));
                            htmlData.append("</td>");
                            htmlData.append("</tr>");
                            if (this.needToShowExtraColumns) {
                                htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_instrumentinventory_extracolumns_" + postfixIndexWithUnderscore + "\">");
                                htmlData.append(this.renderExtraColumns(this.extraColumns, this.qp, instrumentid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                                htmlData.append("</td></tr>");
                            }
                        }
                        htmlData.append("<input type=hidden name=\"instrumentinventory_mode_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "mode_" + postfixIndexWithUnderscore + "\" value=\"" + mode + "\">");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_unmanaged_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "unmanaged_" + postfixIndexWithUnderscore + "\" value=\"" + isUnmanaged + "\">");
                    }
                    htmlData.append("</table></td></tr></table></td>");
                }
                htmlData.append("</tr>");
            }
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
            htmlData.append("<input type=hidden name=instrumentinventory_batchcount id=instrumentinventory_batchcount value=" + consumablelots.size() + ">\n");
            htmlData.append("<input type=hidden name=instrumentinventory_instrumenttypecount id=instrumentinventory_instrumenttypecount value=" + instrumentTypes.size() + ">\n");
            htmlData.append("<input type=hidden name=instrumentinventory_sourceofinstrument id=instrumentinventory_sourceofinstrument value='" + tableid + "'>\n");
        } else {
            htmlData.append("<tr><td nowrap>" + noInstruments + "</td></tr>");
            htmlData.append("<input type=hidden name='instrumentinventory_reagentlotid_0' id='instrumentinventory_reagentlotid_0' value='" + keyid1 + "'>\n");
        }
        htmlData.append("<input type=hidden name='adhocinstrumentpageid' id='adhocinstrumentpageid' value='" + this.promptForAdhocInstrument + "'>\n");
        htmlData.append("<input type=hidden name='adhoctablename' id='adhoctablename' value='" + this.adhoctablename + "'>\n");
        htmlData.append("</table>");
        htmlData.append("</div>");
        if (strRLTIJson.length() > 0) {
            htmlData.append("<script>");
            htmlData.append("var jsonObj={");
            htmlData.append(strRLTIJson.toString());
            htmlData.append("};");
            htmlData.append("</script>");
        }
        return htmlData.toString();
    }

    private String getHtmlForQCBatchInstrument() {
        String mandatoryColumns;
        StringBuffer htmlData = new StringBuffer();
        PropertyListCollection qcinstrument = this.pagedata.getCollection("qcbatchinstrument");
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String dsSelectClause = mandatoryColumns = " qcbatchid,s_qcbatchinstrumentid,instrumenttypeid,instrumentmodelid,instrumentid,sourceflag,instrumentcount,requiredinstrumentcount";
        qcinstrument = qcinstrument == null ? new PropertyListCollection() : qcinstrument;
        String[] hiddenColumnArr = new String[qcinstrument.size()];
        int hiddenIndx = 0;
        for (int i = 0; i < qcinstrument.size(); ++i) {
            PropertyList columnProps = qcinstrument.getPropertyList(i);
            String columnid = columnProps.getProperty("columnid", "");
            String columnAlias = this.getColumnAlias(columnid);
            if (columnAlias.equals("")) {
                String title = columnProps.getProperty("title", "");
                title = title.replaceAll(" ", "");
                columnid = columnid + " " + title;
                columnProps.setProperty("columnid", columnid);
            }
            String columnMode = columnProps.getProperty("mode", "Visible");
            if (!mandatoryColumns.contains(columnid)) {
                dsSelectClause = dsSelectClause + "," + columnid;
            }
            if (columnMode.equalsIgnoreCase("Hidden")) {
                hiddenColumnArr[hiddenIndx] = columnid;
                ++hiddenIndx;
                continue;
            }
            this.increaseFixColumnsWidth(columnProps);
            visibleColumns.add(columnProps);
        }
        String sdcid = this.pagedata.getProperty("sdcid");
        String keyid1 = this.pagedata.getProperty("keyid1");
        String selectedDatasets = this.pagedata.getProperty("selectedds", "");
        String selectedWis = this.pagedata.getProperty("selectedwi", "");
        String postfixIndexWithUnderscore = "";
        String postfixIndexWithUnderscore_temp = "";
        String requiredcount = "";
        String mode = "";
        boolean isUnmanaged = false;
        String tableid = SdcInfo.getTableId(sdcid, this.qp);
        String lookupImage = "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg";
        String noInstruments = this.tp.translate("No instrument found");
        int tabindex = 1;
        htmlData.append("<input type=hidden name=\"selectedds\" id=\"selectedds\" value=\"" + selectedDatasets + "\">\n");
        htmlData.append("<input type=hidden name=\"selectedwi\" id=\"selectedwi\" value=\"" + selectedWis + "\">\n");
        htmlData.append("<input type=hidden name=\"viewonly\" id=\"viewonly\" value=\"" + this.viewonlyOption + "\">\n");
        htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
        htmlData.append("<table id=\"dataEntryTable\" class=\"maintform_table_blue\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
        DataSet controldata = new DataSet();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.reset();
        sql.append("SELECT " + dsSelectClause + " FROM " + tableid);
        sql.append(" WHERE qcbatchid = " + safeSQL.addVar(keyid1) + " ");
        sql.append(" order by USERSEQUENCE");
        controldata = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        ArrayList<DataSet> instrumentTypes = controldata.getGroupedDataSets("instrumenttypeid,instrumentmodelid,sourceflag");
        StringBuffer strRLTIJson = new StringBuffer();
        if (controldata.getRowCount() > 0) {
            if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_Lookup)) {
                htmlData.append("<input type=hidden name=instrumentinventory_instrumentselectionmode id=instrumentinventory_instrumentselectionmode value=\"20\">\n");
            } else {
                htmlData.append("<input type=hidden name=instrumentinventory_instrumentselectionmode id=instrumentinventory_instrumentselectionmode value=\"0\">\n");
            }
            htmlData.append("<tr>");
            htmlData.append(this.getVisibleColumnHtml(visibleColumns));
            for (DataSet dsInstrument : instrumentTypes) {
                int insTypeIndex = 0;
                String sourceofinstrument = dsInstrument.getValue(insTypeIndex, "sourceflag", "");
                String string = dsInstrument.getString(insTypeIndex, "instrumenttypeid", "");
                String insModel = dsInstrument.getString(insTypeIndex, "instrumentmodelid", "");
                String showModelInHeaderRow = "";
                if (this.showModelinHeader && insModel.length() > 0) {
                    showModelInHeaderRow = " (" + insModel + ") ";
                }
                String starForPLInstrument = "";
                if (sourceofinstrument.equalsIgnoreCase("D")) {
                    starForPLInstrument = "*";
                }
                htmlData.append(this.getHeaderColumnHtml(string + showModelInHeaderRow + starForPLInstrument));
            }
            htmlData.append("</tr>");
            ArrayList<Object> qcbatches = new ArrayList();
            controldata.sort("qcbatchid");
            qcbatches = controldata.getGroupedDataSets("qcbatchid");
            int selectedDSIndx = -1;
            HashMap<String, String> hm = new HashMap<String, String>();
            for (DataSet dataSet : qcbatches) {
                ++selectedDSIndx;
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                String qcbatchid = dataSet.getString(0, "qcbatchid", "");
                hm.clear();
                hm.put("qcbatchid", qcbatchid);
                DataSet filteredRows = controldata.getFilteredDataSet(hm);
                htmlData.append("<tr>");
                htmlData.append(this.getVisibleColumnValueHtml(visibleColumns, dataSet, 0));
                htmlData.append("<input type=hidden name=\"instrumentinventory_qcbatchid_" + selectedDSIndx + "\" id=\"" + dataEntryPrefix + "qcbatchid" + "_" + selectedDSIndx + "\" value='" + qcbatchid + "'>");
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    htmlData.append("<input type=hidden name=\"instrumentinventory_" + columnid + "_" + selectedDSIndx + "\" id=\"" + dataEntryPrefix + columnid + "_" + selectedDSIndx + "\" value='" + dataSet.getValue(0, columnid, "") + "'>");
                }
                int insTypeIndex = -1;
                for (DataSet dsInstrument : instrumentTypes) {
                    postfixIndexWithUnderscore_temp = "" + selectedDSIndx + "_" + ++insTypeIndex;
                    postfixIndexWithUnderscore = "" + selectedDSIndx + "_" + insTypeIndex;
                    String instrumenttypeid = dsInstrument.getValue(0, "instrumenttypeid", "");
                    String sourceflag = dsInstrument.getValue(0, "sourceflag", "");
                    String instrumentmodelid = dsInstrument.getValue(0, "instrumentmodelid", "");
                    String instrumentid = dsInstrument.getValue(0, "instrumentid", "");
                    hm.clear();
                    hm.put("instrumenttypeid", instrumenttypeid);
                    hm.put("sourceflag", sourceflag.length() > 0 ? sourceflag : null);
                    if (filteredRows.findRow(hm) < 0) {
                        htmlData.append("<td style=\"" + this.css_EmptyContentCellStyle + "\"></td>");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_sourceofinstrument_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "sourceofinstrument_" + postfixIndexWithUnderscore + "\" value=\"skip\">");
                        continue;
                    }
                    htmlData.append("<input type=hidden name=\"instrumentinventory_sourceofinstrument_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "sourceofinstrument_" + postfixIndexWithUnderscore + "\" value=\"" + sourceflag + "\">");
                    boolean amountEditable = true;
                    String whereClause = this.whereClauseOriginal;
                    whereClause = StringUtil.replaceAll(whereClause, "[instrumenttype]", instrumenttypeid);
                    whereClause = StringUtil.replaceAll(whereClause, "[instrumentmodelid]", instrumentmodelid);
                    isUnmanaged = this.isUnmanaged(this.qp, instrumenttypeid, instrumentmodelid);
                    StringBuffer jsonStr = this.getJsonStr(instrumenttypeid, instrumentmodelid);
                    htmlData.append("<td nowrap style=\"" + this.css_ContentCellStyle + "\" valign='top'>");
                    htmlData.append("<table><tr><td><table >");
                    htmlData.append("");
                    htmlData.append("<input type=hidden name=\"instrumentinventory_instrumenttypeid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumenttypeid_" + postfixIndexWithUnderscore + "\" value=\"" + instrumenttypeid + "\">");
                    htmlData.append("<input type=hidden name=\"instrumentinventory_instrumentdscount_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentdscount_" + postfixIndexWithUnderscore + "\" value=\"" + dsInstrument.size() + "\">");
                    for (int findrow = 0; findrow < dsInstrument.size(); ++findrow) {
                        postfixIndexWithUnderscore = postfixIndexWithUnderscore_temp + "_" + findrow;
                        String primaryColumnValue = dsInstrument.getValue(findrow, "s_qcbatchinstrumentid", "");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_primarykey_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "primarykey_" + postfixIndexWithUnderscore + "\"  value='" + primaryColumnValue + "'>");
                        if (isUnmanaged) {
                            requiredcount = dsInstrument.getValue(findrow, "requiredinstrumentcount", "");
                            String count = dsInstrument.getValue(findrow, "instrumentcount", "");
                            String countInDB = dsInstrument.getValue(findrow, "instrumentcount", "");
                            if (count.length() > 0) {
                                mode = "Edit";
                            } else {
                                mode = "Add";
                                count = requiredcount;
                            }
                            htmlData.append(this.getUnManagedInstrumentCellHtml("requiredcount", "count", postfixIndexWithUnderscore, requiredcount, count, countInDB, viewonly, amountEditable, tabindex++, dataEntryPrefix));
                        } else {
                            instrumentmodelid = dsInstrument.getValue(findrow, "instrumentmodelid", "");
                            instrumentid = dsInstrument.getValue(findrow, "instrumentid", "");
                            mode = instrumentid.length() > 0 ? "Edit" : "Add";
                            htmlData.append("<tr><td>");
                            if (findrow > 0 && this.needToShowExtraColumns) {
                                htmlData.append("<hr>");
                            }
                            htmlData.append(this.getManageInstrumentCellHtml(instrumenttypeid, instrumentmodelid, instrumentid, viewonly, tabindex++, postfixIndexWithUnderscore, jsonStr.toString(), whereClause, dataEntryPrefix, "", ""));
                            htmlData.append("</td>");
                            htmlData.append("</tr>");
                            if (this.needToShowExtraColumns) {
                                htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_instrumentinventory_extracolumns_" + postfixIndexWithUnderscore + "\">");
                                htmlData.append(this.renderExtraColumns(this.extraColumns, this.qp, instrumentid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                                htmlData.append("</td></tr>");
                            }
                        }
                        htmlData.append("<input type=hidden name=\"instrumentinventory_mode_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "mode_" + postfixIndexWithUnderscore + "\" value=\"" + mode + "\">");
                        htmlData.append("<input type=hidden name=\"instrumentinventory_unmanaged_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "unmanaged_" + postfixIndexWithUnderscore + "\" value=\"" + isUnmanaged + "\">");
                    }
                    htmlData.append("</table></td></tr></table></td>");
                }
                htmlData.append("</tr>");
            }
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
            htmlData.append("<input type=hidden name=instrumentinventory_batchcount id=instrumentinventory_batchcount value=" + qcbatches.size() + ">\n");
            htmlData.append("<input type=hidden name=instrumentinventory_instrumenttypecount id=instrumentinventory_instrumenttypecount value=" + instrumentTypes.size() + ">\n");
            htmlData.append("<input type=hidden name=instrumentinventory_sourceofinstrument id=instrumentinventory_sourceofinstrument value='" + sdcid + "'>\n");
        } else {
            htmlData.append("<tr><td nowrap>" + noInstruments + "</td></tr>");
            htmlData.append("<input type=hidden name='instrumentinventory_qcbatchid_0' id='instrumentinventory_qcbatchid_0' value='" + keyid1 + "'>\n");
        }
        htmlData.append("<input type=hidden name='adhocinstrumentpageid' id='adhocinstrumentpageid' value='" + this.promptForAdhocInstrument + "'>\n");
        htmlData.append("<input type=hidden name='adhoctablename' id='adhoctablename' value='" + this.adhoctablename + "'>\n");
        htmlData.append("</table>");
        htmlData.append("</div>");
        if (strRLTIJson.length() > 0) {
            htmlData.append("<script>");
            htmlData.append("var jsonObj={");
            htmlData.append(strRLTIJson.toString());
            htmlData.append("};");
            htmlData.append("</script>");
        }
        return htmlData.toString();
    }

    private String getHtmlForSDIRelation() {
        String mandatoryColumns;
        StringBuffer htmlData = new StringBuffer();
        PropertyListCollection datasetInfo = this.pagedata.getCollection("DataSetInfo");
        String viewonlyOption = this.pagedata.getProperty("viewonly", "No");
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String dsSelectClause = mandatoryColumns = " sdidata.sdidataid,sdidata.keyid1,sdidata.keyid2,sdidata.keyid3,sdidata.paramlistid,sdidata.paramlistversionid,sdidata.variantid,sdidata.dataset,sdidata.s_datasetstatus,sdidata.sourceworkitemid,sdidata.sourceworkiteminstance,sdidata.s_instrumentid";
        String[] hiddenColumnArr = new String[datasetInfo.size()];
        int hiddenIndx = 0;
        HashMap<String, String> sdidataColumns = OpalUtil.getColumnDataTypeMap("sdidata", this.qp);
        for (int i = 0; i < datasetInfo.size(); ++i) {
            String columnMode;
            PropertyList columnProps = datasetInfo.getPropertyList(i);
            String columnid = columnProps.getProperty("columnid", "");
            if (columnid.length() == 0) continue;
            String columnAlias = this.getColumnAlias(columnid);
            if (columnAlias.equals("")) {
                String title = columnProps.getProperty("title", "");
                title = title.replaceAll(" ", "");
                columnid = columnid + " " + title;
                columnProps.setProperty("columnid", columnid);
            }
            if (sdidataColumns.containsKey(columnid) || this.isColumnIdWithAlias(columnid)) {
                columnid = "sdidata." + columnid;
            }
            if (!mandatoryColumns.contains(columnid)) {
                dsSelectClause = dsSelectClause + "," + columnid;
            }
            if ((columnMode = columnProps.getProperty("mode", "Visible")).equalsIgnoreCase("Hidden")) {
                hiddenColumnArr[hiddenIndx] = columnid;
                ++hiddenIndx;
                continue;
            }
            this.increaseFixColumnsWidth(columnProps);
            visibleColumns.add(columnProps);
        }
        String sdcid = this.pagedata.getProperty("sdcid");
        String keyid1 = this.pagedata.getProperty("keyid1");
        String workitemid = this.pagedata.getProperty("workitemid", "");
        String workiteminstance = this.pagedata.getProperty("workiteminstance", "");
        String paramListId = this.pagedata.getProperty("paramlistid");
        String paramListVersionId = this.pagedata.getProperty("paramlistversionid");
        String variantId = this.pagedata.getProperty("variantid");
        String dataset = this.pagedata.getProperty("dataset", "");
        String selectedDatasets = this.pagedata.getProperty("selectedds", "");
        String selectedWis = this.pagedata.getProperty("selectedwi", "");
        String extraColumnsForSDIData = "";
        String extraColumnsForParamlist = "( SELECT unmanagedflag FROM instrumentmodel WHERE instrumentmodel.instrumentmodelid=sdidatarelation.sourcekeyid1 AND instrumentmodel.instrumenttypeid=sdidatarelation.sourcekeyid2 AND sdidatarelation.sourcesdcid='LV_InstrumentModel') unmanagedmodel,( SELECT unmanagedflag FROM instrumenttype WHERE instrumenttype.instrumenttypeid=sdidatarelation.sourcekeyid1 AND sdidatarelation.sourcesdcid='LV_InstrumentType') unmanagedtype";
        String extraColumnsForWorkitem = "( SELECT unmanagedflag FROM instrumentmodel WHERE instrumentmodel.instrumentmodelid=sdiworkitemrelation.sourcekeyid1 AND instrumentmodel.instrumenttypeid=sdiworkitemrelation.sourcekeyid2 AND sdiworkitemrelation.sourcesdcid='LV_InstrumentModel') unmanagedmodel,( SELECT unmanagedflag FROM instrumenttype WHERE instrumenttype.instrumenttypeid=sdiworkitemrelation.sourcekeyid1 AND sdiworkitemrelation.sourcesdcid='LV_InstrumentType') unmanagedtype";
        if (dsSelectClause.length() > 0) {
            extraColumnsForSDIData = dsSelectClause;
            extraColumnsForParamlist = extraColumnsForParamlist + "," + dsSelectClause;
            extraColumnsForWorkitem = extraColumnsForWorkitem + "," + dsSelectClause;
        }
        DataSet controldata = new DataSet();
        DataSet controlSdidata = new DataSet();
        try {
            controldata = this.fetchControlData("Instrument", extraColumnsForParamlist, extraColumnsForWorkitem, sdcid, keyid1, workitemid, workiteminstance, paramListId, paramListVersionId, variantId, dataset);
            if (this.datasource.equalsIgnoreCase("sdidatarelation") || this.datasource.equalsIgnoreCase("")) {
                controlSdidata = this.fetchSDIData(extraColumnsForSDIData, keyid1, workitemid, workiteminstance, paramListId, paramListVersionId, variantId, dataset);
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        this.mergeControlData(controldata, controlSdidata, visibleColumns, hiddenColumnArr);
        if (controldata != null && controldata.size() > 0) {
            this.fieldValueHM.put("currentparamlistid", paramListId);
            this.fieldValueHM.put("currentparamlistversionid", paramListVersionId);
            this.fieldValueHM.put("currentvariantid", variantId);
            this.fieldValueHM.put("selectedwi", selectedWis);
            this.fieldValueHM.put("selectedds", selectedDatasets);
            this.fieldValueHM.put("viewonly", viewonlyOption);
            controldata.sort("keyid1,paramlistid,paramlistversionid,variantid,dataset");
            ArrayList<DataSet> samples = controldata.getGroupedDataSets("keyid1,paramlistid,paramlistversionid,variantid,dataset");
            LinkedHashMap<String, String> instrHeaderTitles = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> instrHeaderTitlesTooltips = new LinkedHashMap<String, String>();
            ArrayList<String> uniqueSDIWI = new ArrayList<String>();
            ArrayList<String> instrTypes = new ArrayList<String>();
            controldata.sort("sourcetype a,relationid");
            for (int insTypeIndex = 0; insTypeIndex < controldata.getRowCount(); ++insTypeIndex) {
                String isrelationdata = controldata.getString(insTypeIndex, "isrelationdata", "Y");
                String sourcetype = controldata.getString(insTypeIndex, "sourcetype", "");
                String instrMasterData = "";
                String title = "";
                String insTypeID = "";
                String insModel = "";
                insTypeID = controldata.getString(insTypeIndex, "relationtype", "");
                insModel = controldata.getString(insTypeIndex, "instrmodel", "");
                instrMasterData = (String)isrelationdata + this.specialDelimer + insTypeID + this.specialDelimer + insModel;
                if (insTypeID.trim().length() <= 0 || instrTypes.contains(instrMasterData)) continue;
                String star = sourcetype.equalsIgnoreCase("D") ? "*" : "";
                title = insTypeID + (this.showModelinHeader && insModel.length() > 0 ? " (" + insModel + ")" : "") + star;
                instrTypes.add(instrMasterData);
                instrHeaderTitles.put(instrMasterData, title);
                instrHeaderTitlesTooltips.put(instrMasterData, this.getToolTip(controldata, insTypeIndex));
            }
            htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
            htmlData.append("<table id=\"dataEntryTable\" class=\"" + this.css_maintFormTable + "\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
            htmlData.append("<tr style=\"" + this.getFixedPositionStyleForTop(0) + "\" >");
            htmlData.append(this.getConfiguredColumnsHeader(false, visibleColumns));
            int instrTypeInx = -1;
            for (String instrType : instrTypes) {
                htmlData.append("<th class=\"" + this.css_columnHeader + "\"><table width=100% border=0><tr><td style=\"font-weight:bold;\" align=center title='" + (String)instrHeaderTitlesTooltips.get(instrType) + "'>" + SafeHTML.encodeForHTML((String)instrHeaderTitles.get(instrType)) + "</td><td width=15>&nbsp;</td><td width=15><a onClick=\"javascript:instrumentInventory.fillDown(" + ++instrTypeInx + ");\"><img src='" + this.filldownImage + "' border=0 title='" + this.tp.translate("Fill Down..") + "'></a></td></tr></table></th>");
                ++this.headerColumnCount;
            }
            htmlData.append(this.showButtonForAdhocConsumable ? "<th class=\"" + this.css_columnHeader + "\"></th>" : "");
            htmlData.append("</tr>");
            int selectedDSIndx = -1;
            ArrayList<String> uniqueSDIWIRecords = new ArrayList<String>();
            for (DataSet sdirelations : samples) {
                String key1 = "";
                String key2 = "";
                String key3 = "";
                String plid = "";
                String plver = "";
                String vrnt = "";
                String dsno = "";
                String modifiableflag = "";
                String sourceworkitemid = "";
                String sourceworkiteminstance = "";
                if (this.workitemonly) {
                    sdirelations.sort("keyid1,keyid2,keyid3,sourceworkitemid,sourceworkiteminstance");
                    ArrayList<DataSet> filteredRelations = sdirelations.getGroupedDataSets("keyid1,keyid2,keyid3,sourceworkitemid,sourceworkiteminstance");
                    boolean foundSelectedRecord = false;
                    for (DataSet filteredRelation : filteredRelations) {
                        key1 = filteredRelation.getValue(0, "keyid1");
                        key2 = filteredRelation.getValue(0, "keyid2");
                        key3 = filteredRelation.getValue(0, "keyid3");
                        plid = filteredRelation.getValue(0, "paramlistid");
                        plver = filteredRelation.getValue(0, "paramlistversionid");
                        vrnt = filteredRelation.getValue(0, "variantid");
                        dsno = filteredRelation.getValue(0, "dataset");
                        modifiableflag = filteredRelation.getValue(0, "modifiableflag");
                        sourceworkitemid = filteredRelation.getValue(0, "sourceworkitemid");
                        sourceworkiteminstance = filteredRelation.getValue(0, "sourceworkiteminstance");
                        String woritem = key1 + this.specialDelimer + sourceworkitemid + this.specialDelimer + sourceworkiteminstance;
                        if (!selectedWis.contains(woritem)) continue;
                        foundSelectedRecord = true;
                        sdirelations = filteredRelation;
                        break;
                    }
                    if (!foundSelectedRecord) {
                        continue;
                    }
                } else {
                    key1 = sdirelations.getValue(0, "keyid1");
                    key2 = sdirelations.getValue(0, "keyid2");
                    key3 = sdirelations.getValue(0, "keyid3");
                    plid = sdirelations.getValue(0, "paramlistid");
                    plver = sdirelations.getValue(0, "paramlistversionid");
                    vrnt = sdirelations.getValue(0, "variantid");
                    dsno = sdirelations.getValue(0, "dataset");
                    modifiableflag = sdirelations.getValue(0, "modifiableflag");
                    sourceworkitemid = sdirelations.getValue(0, "sourceworkitemid");
                    sourceworkiteminstance = sdirelations.getValue(0, "sourceworkiteminstance");
                    String dset = key1 + this.specialDelimer + plid + this.specialDelimer + plver + this.specialDelimer + vrnt + this.specialDelimer + dsno;
                    if (!selectedDatasets.contains(dset)) continue;
                }
                if (this.workitemonly) {
                    String sdiwirecord = key1 + this.specialDelimer + sourceworkitemid + this.specialDelimer + sourceworkiteminstance;
                    if (uniqueSDIWIRecords.contains(sdiwirecord)) continue;
                    uniqueSDIWIRecords.add(sdiwirecord);
                }
                this.fieldValueHM.put("instrumentinventory_keyid1_" + ++selectedDSIndx, key1);
                this.fieldValueHM.put("instrumentinventory_keyid2_" + selectedDSIndx, key2);
                this.fieldValueHM.put("instrumentinventory_keyid3_" + selectedDSIndx, key3);
                this.fieldValueHM.put("instrumentinventory_paramlistid_" + selectedDSIndx, plid);
                this.fieldValueHM.put("instrumentinventory_paramlistversionid_" + selectedDSIndx, plver);
                this.fieldValueHM.put("instrumentinventory_variantid_" + selectedDSIndx, vrnt);
                this.fieldValueHM.put("instrumentinventory_dataset_" + selectedDSIndx, dsno);
                this.fieldValueHM.put("instrumentinventory_modifiableflag_" + selectedDSIndx, modifiableflag);
                this.fieldValueHM.put("instrumentinventory_sourceworkitemid_" + selectedDSIndx, sourceworkitemid);
                this.fieldValueHM.put("instrumentinventory_sourceworkiteminstance_" + selectedDSIndx, sourceworkiteminstance);
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    this.fieldValueHM.put(dataEntryPrefix + columnid + "_" + selectedDSIndx, sdirelations.getValue(0, columnid, ""));
                }
                htmlData.append("<tr>");
                htmlData.append(this.getConfiguredColumnsValue(selectedDSIndx, false, visibleColumns, sdirelations, 0));
                htmlData.append(this.getContentCellHtml(instrTypes, sdirelations, uniqueSDIWI, selectedDSIndx));
                htmlData.append(this.getButtonForAdhocInstrument(selectedDSIndx));
                htmlData.append("</tr>");
            }
            htmlData.append(this.getNotesForRequiredConsumableInstrument());
            htmlData.append("</table>");
            htmlData.append("</div>");
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
            this.fieldValueHM.put("instrumentinventory_samplecount", selectedDSIndx + 1 + "");
            this.fieldValueHM.put("instrumentinventory_instrumenttypecount", instrTypes.size() + "");
            this.fieldValueHM.put("instrumentinventory_sourceofinstrument", sdcid);
            htmlData.append(this.addHiddenFields(this.fieldValueHM));
        } else {
            htmlData.append(this.noInstruments);
        }
        return htmlData.toString();
    }

    private StringBuffer getJsonStr(String instrumenttypeid, String instrumentmodelid) {
        StringBuffer jsonStr = new StringBuffer();
        jsonStr.append("{'instrumenttypeid':'" + instrumenttypeid);
        jsonStr.append("','instrumentmodelid':'" + instrumentmodelid);
        jsonStr.append("','ajaxclass':'" + this.ajaxClass);
        jsonStr.append("'}");
        return jsonStr;
    }

    private StringBuffer getUnManagedInstrumentCellHtml(String requiredFldId, String actulaFldId, String postfixIndexWithUnderscore, String requiredamount, String amount, String amountInDB, boolean viewonly, boolean amountEditable, int tabindex, String dataEntryPrefix) {
        StringBuffer htmlData = new StringBuffer();
        htmlData.append("<input type=hidden name=\"" + dataEntryPrefix + "prev" + actulaFldId + "_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "prev" + actulaFldId + "_" + postfixIndexWithUnderscore + "\" value=\"" + amountInDB + "\">");
        htmlData.append("<tr>");
        htmlData.append("<td nowrap style=\"border=0\">" + this.requiredcounttitle + ":&nbsp;</td>");
        htmlData.append("<td><input style=\"border-style:none;\" type=text readonly size=6 name=\"" + dataEntryPrefix + requiredFldId + "_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + requiredFldId + "_" + postfixIndexWithUnderscore + "\" value='" + requiredamount + "'></td>");
        htmlData.append("</tr>");
        htmlData.append("<tr>");
        htmlData.append("<td nowrap style=\"border=0\">" + this.actualcounttitle + ":&nbsp;</td>");
        htmlData.append("<td ><input class=\"input_field\" size=6 tabindex=" + tabindex + (amountEditable && !viewonly ? " class=\"dataentry_grid_cell\"" : " readonly style=\"border:1px solid gray;\"") + " type=text size=8 name=\"" + dataEntryPrefix + actulaFldId + "_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + actulaFldId + "_" + postfixIndexWithUnderscore + "\" value=\"" + amount + "\" onchange='instrumentInventory.validateInstrumentCount(this.id)'></td>");
        htmlData.append("</tr>");
        return htmlData;
    }

    private StringBuffer getManageInstrumentCellHtml(String instrumenttypeid, String instrumentmodelid, String instrumentid, boolean viewonly, int tabindex, String postfixIndexWithUnderscore, String jsonStr, String whereClause, String dataEntryPrefix, String testingDeptId, String sdidataid) {
        StringBuffer htmlData = new StringBuffer();
        String instrumentlookuppage = "InstrumentDELookup";
        htmlData.append("<input type=hidden name=\"" + dataEntryPrefix + "previnstrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "previnstrumentid_" + postfixIndexWithUnderscore + "\" value=\"" + instrumentid + "\">");
        if (viewonly) {
            htmlData.append("<input tabindex=" + tabindex + " size=\"" + this.fldSize + "\" type=text readonly value=\"" + instrumentid + "\" name=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\"  maxlength=\"" + this.maxFldSize + "\">");
        } else {
            String string = testingDeptId != null && testingDeptId.length() > 0 ? (whereClause != null && whereClause.length() > 0 ? whereClause + " AND (testingdepartmentid='" + testingDeptId + "' or testingdepartmentid is null)" : "(testingdepartmentid='" + testingDeptId + "'  or testingdepartmentid is null)") : (whereClause = whereClause);
            if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_Lookup) || this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) {
                if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) {
                    htmlData.append("<input onKeyPress=\"instrumentUtil.validateOnPressEnter(event,this.id," + jsonStr + " )\" onchange=\"instrumentUtil.validateScannedInstrument(this.id," + jsonStr + ",'N','" + testingDeptId + "' )\" onclick=\"instrumentUtil.selectInstrument(this.id,true);\" ondblclick=\"instrumentUtil.selectInstrument(this.id,false)\" edit=\"lookup\" type=\"text\" class=\"input_field\" size=\"" + this.fldSize + "\" value=\"" + instrumentid + "\" name=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\"  maxlength=\"" + this.maxFldSize + "\" onfocus=\"\" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
                } else {
                    htmlData.append("<input style=\"border:1px solid green\" onchange=\"instrumentUtil.validateScannedInstrument(this.id," + jsonStr + ",'N','" + testingDeptId + "')\" readonly=\"\" edit=\"lookup\" type=\"text\" class=\"input_field\" onkeyup=\";showSuggestion()\" size=\"" + this.fldSize + "\" value=\"" + instrumentid + "\" name=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\"  maxlength=\"" + this.maxFldSize + "\" onfocus=\"\" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
                }
                htmlData.append(this.getJSScriptForDDD(dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore, whereClause));
                htmlData.append("<a style=\"display:inline;\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" href=\"/Lookup\" onClick=\"instrumentUtil.openLookup(this.id,'" + instrumentlookuppage + "', '" + instrumenttypeid + "','" + instrumentmodelid + "','" + testingDeptId + "','" + sdidataid + "');return false\" tabindex=\"0\"><img title=\"Instrument Lookup\" border=\"0\" src=\"" + this.lookupImage + "\" class=\"lookup_img\"></a>");
            } else if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_ScanMode)) {
                htmlData.append("<input onKeyPress=\"instrumentUtil.validateOnPressEnter(event,this.id," + jsonStr + " )\" onchange=\"instrumentUtil.validateScannedInstrument(this.id," + jsonStr + ",'N','" + testingDeptId + "' )\" onclick=\"instrumentUtil.selectInstrument(this.id,true);\" ondblclick=\"instrumentUtil.selectInstrument(this.id,false)\" edit=\"lookup\" type=\"text\" class=\"input_field\" size=\"" + this.fldSize + "\" value=\"" + instrumentid + "\" name=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\"  maxlength=\"" + this.maxFldSize + "\" onfocus=\"\" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
            } else if (this.instrumentselectionmode.equalsIgnoreCase(this.selectionMode_Dropdown)) {
                htmlData.append("<select onchange=\"instrumentUtil.validateScannedInstrument(this.id," + jsonStr + ",'Y','" + testingDeptId + "')\" tabindex=" + tabindex + " name=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" id=\"" + dataEntryPrefix + "instrumentid_" + postfixIndexWithUnderscore + "\" maxlength=\"" + this.maxFldSize + "\">");
                htmlData.append("<option value=\"\"></option>");
                DataSet instrumentIdDS = this.getInstrumentIds(this.qp, instrumenttypeid, instrumentmodelid, testingDeptId);
                if (instrumentIdDS != null && instrumentIdDS.size() > 0) {
                    for (int i = 0; i < instrumentIdDS.size(); ++i) {
                        String insId = instrumentIdDS.getString(i, "instrumentid", "");
                        if (insId.equalsIgnoreCase(instrumentid)) {
                            htmlData.append("<option selected value=\"" + insId + "\">" + insId + "</option>");
                            continue;
                        }
                        htmlData.append("<option value=\"" + insId + "\">" + insId + "</option>");
                    }
                }
                htmlData.append("</select>");
            }
        }
        return htmlData;
    }

    private String getToolTip(DataSet dsInstrument, int insTypeIndex, String sourceofinstrument) {
        String tooltip = "";
        if (sourceofinstrument.equalsIgnoreCase(workitemInstrument)) {
            String workitemid = dsInstrument.getString(insTypeIndex, "workitemid", "");
            String workitemversionid = dsInstrument.getString(insTypeIndex, "workitemversionid", "");
            tooltip = "This Instrument came from the WorkItem " + workitemid + " (" + workitemversionid + ") ";
        } else if (sourceofinstrument.equalsIgnoreCase(workitemInstrumentLinkToPL)) {
            String workitemid = dsInstrument.getString(insTypeIndex, "workitemid", "");
            String workitemversionid = dsInstrument.getString(insTypeIndex, "workitemversionid", "");
            tooltip = "This Instrument came from the WorkItem " + workitemid + " (" + workitemversionid + ") which is linked to the selecetd ParamList";
        } else {
            tooltip = "This Instrument came from the above selecetd ParamList";
        }
        return tooltip;
    }

    public void addToDataSet(JSONObject json, DataSet ds) throws Exception {
        String sourceofinstrument = json.getString("sourceofinstrument");
        int row = ds.addRow();
        ds.setValue(row, "sdcid", json.getString("sdcid"));
        ds.setValue(row, "keyid1", json.getString("keyid1"));
        ds.setValue(row, "keyid2", json.getString("keyid2"));
        ds.setValue(row, "keyid3", json.getString("keyid3"));
        if (sourceofinstrument.equalsIgnoreCase(workitemInstrument)) {
            ds.setValue(row, "workitemid", json.getString("workitemid"));
            ds.setValue(row, "workiteminstance", json.getString("workiteminstance"));
        } else {
            ds.setValue(row, "paramlistid", json.getString("paramlistid"));
            ds.setValue(row, "paramlistversionid", json.getString("paramlistversionid"));
            ds.setValue(row, "variantid", json.getString("variantid"));
            ds.setValue(row, "dataset", json.getString("dataset"));
        }
        ds.setValue(row, "relationid", json.getString("relationid"));
        ds.setValue(row, "relationtype", json.getString("relationtype"));
        ds.setValue(row, "tosdcid", json.getString("tosdcid"));
        ds.setValue(row, "tokeyid1", this.replaceBlankWithNullBlank(json.getString("tokeyid1")));
        ds.setValue(row, "amount", this.replaceBlankWithNullBlank(json.getString("amount")));
    }

    private boolean isUnmanaged(QueryProcessor qp, String instrumenttypeid, String instrumentmodelid) {
        boolean unmanaged = false;
        if (instrumentmodelid.length() > 0) {
            DataSet instrumentModelDS = qp.getPreparedSqlDataSet("select unmanagedflag from instrumentmodel where instrumentmodelid = ?", (Object[])new String[]{instrumentmodelid});
            if (instrumentModelDS != null && instrumentModelDS.size() > 0) {
                unmanaged = instrumentModelDS.getString(0, "unmanagedflag", "").equalsIgnoreCase("Y");
            }
        } else {
            DataSet instrumentTypeDS = qp.getPreparedSqlDataSet("select unmanagedflag from instrumenttype where instrumenttypeid = ?", (Object[])new String[]{instrumenttypeid});
            if (instrumentTypeDS != null && instrumentTypeDS.size() > 0) {
                unmanaged = instrumentTypeDS.getString(0, "unmanagedflag", "").equalsIgnoreCase("Y");
            }
        }
        return unmanaged;
    }

    private DataSet getInstrumentIds(QueryProcessor qp, String instrumenttypeid, String instrumentmodelid, String testingpartmentid) {
        return qp.getPreparedSqlDataSet("select instrumentid,instrumentdesc from instrument where instrumenttype=?  AND instrumentmodelid=coalesce(NULLIF(?,'') ,instrumentmodelid)  AND ( coalesce(testingdepartmentid,' ')=coalesce(NULLIF(?,'') ,testingdepartmentid,' ') or testingdepartmentid is null) AND  " + this.getInstrumentCertWhereClause("", this.isOracle), (Object[])new String[]{instrumenttypeid, instrumentmodelid, testingpartmentid});
    }

    private String getJSScriptForDDD(String id, String restrictivewhere) {
        String sdcid = "Instrument";
        return "<script type=\"text/javascript\">var oLUPD_" + id + " = {\"selectortype\":\"\",\"sdcid\":\"" + sdcid + "\", restrictivewhere: \"" + SafeHTML.encodeForJavaScript(this.getInstrumentCertWhereClause(restrictivewhere, this.isOracle)) + "\"};</script>";
    }

    private String getInstrumentCertWhereClause(String queryWhere, boolean isOracle) {
        return (queryWhere != null && queryWhere.length() > 0 ? "(" + queryWhere + ") AND " : "") + "  (instrument.inserviceflag is null OR instrument.inserviceflag!='N') AND ( instrument.certificationreqflag is null OR instrument.certificationreqflag='N' OR (instrument.certificationreqflag='P' AND instrument.instrumentstatus='Available') OR EXISTS (SELECT S_SDICERTIFICATION.RESOURCEKEYID1 FROM s_sdicertification   WHERE S_SDICERTIFICATION.CERTIFICATIONTYPE = 'Instrument'     AND S_SDICERTIFICATION.CERTIFICATIONSTATUS = 'Valid'     AND S_SDICERTIFICATION.RESOURCESDCID = 'Instrument'      AND S_SDICERTIFICATION.RESOURCEKEYID1 = INSTRUMENT.INSTRUMENTID      AND (S_SDICERTIFICATION.EXPIRATIONDT IS NULL          OR " + (isOracle ? "( SYSDATE < DECODE(S_SDICERTIFICATION.GRACEPERIODUNITS, 'Days', S_SDICERTIFICATION.EXPIRATIONDT+NVL(S_SDICERTIFICATION.GRACEPERIOD,0),                         'Weeks', S_SDICERTIFICATION.EXPIRATIONDT+7*NVL(S_SDICERTIFICATION.GRACEPERIOD,0),                        'Months', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, NVL(S_SDICERTIFICATION.GRACEPERIOD,0)),                         'Years', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, 12*NVL(S_SDICERTIFICATION.GRACEPERIOD,0)),                        S_SDICERTIFICATION.EXPIRATIONDT)            )" : "            ( GETDATE() <  CASE  S_SDICERTIFICATION.GRACEPERIODUNITS                                WHEN 'Days' THEN DATEADD( DAY, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Weeks' THEN DATEADD( WEEK, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Months' THEN DATEADD( MONTH, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    WHEN 'Years' THEN DATEADD( YEAR, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)                                    ELSE S_SDICERTIFICATION.EXPIRATIONDT                                   END                        )    ") + "    ) ) )";
    }

    private PropertyList getActionProps(DataSet ds, String mode) {
        return this.getActionProps(ds, mode, "");
    }

    private PropertyList getActionProps(DataSet ds, String mode, String sourceofinstrument) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", ds.getValue(0, "sdcid"));
        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
        props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
        props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
        if (workitemInstrument.equalsIgnoreCase(sourceofinstrument)) {
            props.setProperty("workitemid", ds.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", ds.getColumnValues("workiteminstance", ";"));
        } else {
            props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
        }
        props.setProperty("tosdcid", ds.getColumnValues("tosdcid", ";"));
        props.setProperty("tokeyid1", ds.getColumnValues("tokeyid1", ";"));
        if ("Edit".equalsIgnoreCase(mode)) {
            props.setProperty("relationid", ds.getColumnValues("relationid", ";"));
        }
        props.setProperty("relationfunction", this.getRelationFunctions(ds.size()));
        props.setProperty("relationtype", ds.getColumnValues("relationtype", ";"));
        props.setProperty("amount", ds.getColumnValues("amount", ";"));
        this.setAuditProps(props);
        return props;
    }

    private String getRelationFunctions(int n) {
        String relationFunction = "Instrument";
        for (int i = 2; i <= n; ++i) {
            relationFunction = relationFunction + ";Instrument";
        }
        return relationFunction;
    }

    private void populateDSColumns(DataSet ds) {
        ds.addColumn("relationtype", 0);
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("keyid2", 0);
        ds.addColumn("keyid3", 0);
        ds.addColumn("dataset", 0);
        ds.addColumn("paramlistid", 0);
        ds.addColumn("paramlistversionid", 0);
        ds.addColumn("variantid", 0);
        ds.addColumn("tosdcid", 0);
        ds.addColumn("tokeyid1", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("relationid", 0);
    }

    private void populateWIDSColumns(DataSet ds) {
        ds.addColumn("relationtype", 0);
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("keyid2", 0);
        ds.addColumn("keyid3", 0);
        ds.addColumn("workitemid", 0);
        ds.addColumn("workitemversionid", 0);
        ds.addColumn("workiteminstance", 0);
        ds.addColumn("tosdcid", 0);
        ds.addColumn("tokeyid1", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("relationid", 0);
    }

    public void saveData(String data, String sourcesdc, String auditreason, String auditactivity, String auditsignedflag) {
        this.auditreason = auditreason;
        this.auditactivity = auditactivity;
        this.auditsignedflag = auditsignedflag;
        if (sourcesdc != null && sourcesdc.equalsIgnoreCase("LV_QCBatchInstrument")) {
            this.saveDataForQCBatch(data, sourcesdc);
        } else if (sourcesdc != null && sourcesdc.equalsIgnoreCase(this.reagentlotrecipe)) {
            this.saveDataForDetailTable(data, sourcesdc);
        } else {
            this.saveDataForSDIRelation(data);
        }
        this.addActivityLog();
    }

    private void saveDataForQCBatch(String data, String sourcesdc) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.length() > 0) {
                StringBuffer keyids = new StringBuffer();
                StringBuffer instrumentids = new StringBuffer();
                StringBuffer instrumentcounts = new StringBuffer();
                for (int row = 0; row < jsonArray.length(); ++row) {
                    JSONObject json = jsonArray.getJSONObject(row);
                    keyids.append(";").append(json.getString("keyid1"));
                    instrumentids.append(";").append(json.getString("instrumentid"));
                    instrumentcounts.append(";").append(json.getString("count"));
                }
                if (keyids.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", sourcesdc);
                    props.setProperty("keyid1", keyids.substring(1));
                    props.setProperty("instrumentid", instrumentids.substring(1));
                    props.setProperty("instrumentcount", instrumentcounts.substring(1));
                    this.setAuditProps(props);
                    this.ap.processAction("EditSDI", "1", props);
                }
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void saveDataForDetailTable(String data, String sourcesdc) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.length() > 0) {
                StringBuffer keyid1s = new StringBuffer();
                StringBuffer keyid2s = new StringBuffer();
                StringBuffer instrumentids = new StringBuffer();
                StringBuffer instrumentcounts = new StringBuffer();
                for (int row = 0; row < jsonArray.length(); ++row) {
                    JSONObject json = jsonArray.getJSONObject(row);
                    keyid1s.append(";").append(json.getString("keyid1"));
                    keyid2s.append(";").append(json.getString("keyid2"));
                    instrumentids.append(";").append(json.getString("instrumentid"));
                    instrumentcounts.append(";").append(json.getString("count"));
                }
                if (keyid1s.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_ReagentLot");
                    props.setProperty("linkid", this.reagentlotrecipe);
                    props.setProperty("reagentlotid", keyid1s.substring(1));
                    props.setProperty("reagentlotrecipeitemid", keyid2s.substring(1));
                    props.setProperty("instrumentid", instrumentids.substring(1));
                    props.setProperty("amount", instrumentcounts.substring(1));
                    this.setAuditProps(props);
                    this.ap.processAction("EditSDIDetail", "1", props);
                }
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void saveDataForSDIRelation(String data) {
        DataSet addDataSet = new DataSet();
        DataSet editDataSet = new DataSet();
        DataSet sdidataDS = new DataSet();
        this.populateDSColumns(addDataSet);
        this.populateDSColumns(editDataSet);
        this.populateDSColumns(sdidataDS);
        DataSet addWIDataSet = new DataSet();
        DataSet editWIDataSet = new DataSet();
        this.populateWIDSColumns(addWIDataSet);
        this.populateWIDSColumns(editWIDataSet);
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int row = 0; row < jsonArray.length(); ++row) {
                JSONObject jsonObject = jsonArray.getJSONObject(row);
                String data_mode = jsonObject.getString("mode").toLowerCase();
                String sourceofinstrument = jsonObject.getString("sourceofinstrument");
                if (workitemInstrument.equalsIgnoreCase(sourceofinstrument)) {
                    if ("edit".equals(data_mode)) {
                        this.addToDataSet(jsonObject, editWIDataSet);
                        continue;
                    }
                    if (!"add".equals(jsonObject.getString("mode").toLowerCase())) continue;
                    this.addToDataSet(jsonObject, addWIDataSet);
                    continue;
                }
                if ("sdidata".equalsIgnoreCase(sourceofinstrument)) {
                    this.addToDataSet(jsonObject, sdidataDS);
                    continue;
                }
                if ("edit".equals(data_mode)) {
                    this.addToDataSet(jsonObject, editDataSet);
                    continue;
                }
                if (!"add".equals(jsonObject.getString("mode").toLowerCase())) continue;
                this.addToDataSet(jsonObject, addDataSet);
            }
            if (addDataSet.size() > 0) {
                this.ap.processAction("AddSDIDataRelation", "1", this.getActionProps(addDataSet, "Add"));
            }
            if (editDataSet.size() > 0) {
                this.ap.processAction("EditSDIDataRelation", "1", this.getActionProps(editDataSet, "Edit"));
            }
            if (addWIDataSet.size() > 0) {
                this.ap.processAction("AddSDIDataRelation", "1", this.getActionProps(addWIDataSet, "Add", workitemInstrument));
            }
            if (editWIDataSet.size() > 0) {
                this.ap.processAction("EditSDIWorkItemRelation", "1", this.getActionProps(editWIDataSet, "Edit", workitemInstrument));
            }
            if (editDataSet.size() > 0 || editWIDataSet.size() > 0) {
                OpalUtil.updateStatus(new JSONArray(data), this.qp, this.ap);
            }
            if (sdidataDS.size() > 0) {
                this.updateSDIData(sdidataDS);
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void updateSDIData(DataSet ds) throws Exception {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", this.sdcid);
        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
        props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
        props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
        props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
        props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
        props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
        props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
        props.setProperty("s_instrumentid", ds.getColumnValues("tokeyid1", ";"));
        props.setProperty("propsmatch", "Y");
        this.setAuditProps(props);
        this.ap.processAction("EditDataSet", "1", props);
    }

    protected String getContentCellHtml(ArrayList<String> instrTypes, DataSet sdirelations, ArrayList<String> uniqueSDIWI, int selectedDSIndx) {
        StringBuffer htmlData = new StringBuffer();
        HashMap<String, String> hm = new HashMap<String, String>();
        int insTypeIndex = -1;
        int tabindex = 1;
        String postfixIndexWithUnderscore = "";
        String postfixIndexWithUnderscore_temp = "";
        for (String instrSourceKey : instrTypes) {
            postfixIndexWithUnderscore_temp = "" + selectedDSIndx + "_" + ++insTypeIndex;
            boolean foundInvRecord = false;
            boolean mergeInvRecord = false;
            String sourcetype = "";
            String sourceofinstr = "";
            String[] instrSourceKeyArr = StringUtil.split(instrSourceKey, this.specialDelimer);
            String isrelationdata = instrSourceKeyArr[0];
            String instrtype = instrSourceKeyArr[1];
            String instrmodel = instrSourceKeyArr[2];
            hm.clear();
            hm.put("isrelationdata", isrelationdata);
            hm.put("relationtype", instrtype);
            hm.put("instrmodel", instrmodel.length() > 0 ? instrmodel : null);
            DataSet filteredRelations = sdirelations.getFilteredDataSet(hm);
            if (filteredRelations.getRowCount() > 0) {
                foundInvRecord = true;
                sourcetype = filteredRelations.getValue(0, "sourcetype");
                String string = sourcetype.equalsIgnoreCase("D") ? "sdidata" : (sourceofinstr = sourcetype.equalsIgnoreCase("W") ? workitemInstrument : "paramlist");
                if (sourcetype.equalsIgnoreCase("W")) {
                    String key1 = filteredRelations.getValue(0, "keyid1");
                    String swid = filteredRelations.getValue(0, "sourceworkitemid");
                    String swinstnace = filteredRelations.getValue(0, "sourceworkiteminstance");
                    String sdiwi = key1 + this.specialDelimer + swid + this.specialDelimer + swinstnace + this.specialDelimer + instrtype + this.specialDelimer + instrmodel;
                    if (!uniqueSDIWI.contains(sdiwi)) {
                        uniqueSDIWI.add(sdiwi);
                    } else {
                        foundInvRecord = false;
                        mergeInvRecord = true;
                    }
                }
            }
            this.fieldValueHM.put("instrumentinventory_sourceofinstrument_" + postfixIndexWithUnderscore_temp, foundInvRecord ? sourceofinstr : "skip");
            if (foundInvRecord) {
                int actualRow = -1;
                boolean amountEditable = true;
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                String mode = "";
                String requiredamount = filteredRelations.getValue(0, "requiredamount", "");
                String datasetstatus = filteredRelations.getValue(0, "s_datasetstatus", "");
                String unmanagedmodel = filteredRelations.getValue(0, "unmanagedmodel", "N");
                String unmanagedtype = filteredRelations.getValue(0, "unmanagedtype", "N");
                boolean isUnmanaged = instrmodel.length() > 0 ? unmanagedmodel.equalsIgnoreCase("Y") : unmanagedtype.equalsIgnoreCase("Y");
                String sdidataid = filteredRelations.getValue(0, "sdidataid", "");
                String swid = filteredRelations.getValue(0, "sourceworkitemid");
                String swinstnace = filteredRelations.getValue(0, "sourceworkiteminstance");
                String relationid = filteredRelations.getValue(0, "relationid", "");
                String amount = filteredRelations.getValue(0, "amount", "");
                boolean isMandatory = filteredRelations.getString(0, "mandatoryflag", "N").equalsIgnoreCase("Y");
                boolean bl = this.mandatoryFound = isMandatory && !this.mandatoryFound ? isMandatory : this.mandatoryFound;
                if (datasetstatus.equalsIgnoreCase("Cancelled") || datasetstatus.equalsIgnoreCase("Completed") && this.viewonlyOption.equalsIgnoreCase("OnCompletion")) {
                    viewonly = true;
                }
                String whereClause = this.whereClauseOriginal;
                whereClause = StringUtil.replaceAll(whereClause, "[instrumenttype]", instrtype);
                whereClause = StringUtil.replaceAll(whereClause, "[instrumentmodelid]", instrmodel);
                StringBuffer jsonStr = this.getJsonStr(instrtype, instrmodel);
                this.fieldValueHM.put("instrumentinventory_workitemid_" + postfixIndexWithUnderscore_temp, swid);
                this.fieldValueHM.put("instrumentinventory_workiteminstance_" + postfixIndexWithUnderscore_temp, swinstnace);
                this.fieldValueHM.put("instrumentinventory_instrumenttypeid_" + postfixIndexWithUnderscore_temp, instrtype);
                this.fieldValueHM.put("instrumentinventory_instrumentmodelid_" + postfixIndexWithUnderscore_temp, instrmodel);
                this.fieldValueHM.put("instrumentinventory_sdistatus_" + postfixIndexWithUnderscore_temp, datasetstatus);
                this.fieldValueHM.put("instrumentinventory_viewonly_" + postfixIndexWithUnderscore_temp, viewonly + "");
                htmlData.append("<td nowrap style=\"" + this.css_ContentCellStyle + "\" valign='top'><table>");
                htmlData.append("<tr>");
                htmlData.append("<td><table >");
                for (int i = 0; i < filteredRelations.getRowCount(); ++i) {
                    postfixIndexWithUnderscore = postfixIndexWithUnderscore_temp + "_" + ++actualRow;
                    if (isUnmanaged) {
                        mode = amount.length() > 0 ? "Edit" : "Add";
                        htmlData.append(this.getUnManagedInstrumentCellHtml("requireedamount", "amount", postfixIndexWithUnderscore, requiredamount, amount, amount, viewonly, amountEditable, tabindex++, dataEntryPrefix));
                    } else {
                        String instrumentid = filteredRelations.getString(i, "tokeyid1", "");
                        relationid = filteredRelations.getValue(i, "relationid", "");
                        String testingDeptId = filteredRelations.getValue(i, "testingdepartmentid", "");
                        mode = instrumentid.length() > 0 ? "Edit" : "Add";
                        htmlData.append("<tr><td nowrap>");
                        if (actualRow > 0 && this.needToShowExtraColumns) {
                            htmlData.append("<hr>");
                        }
                        htmlData.append(this.getManageInstrumentCellHtml(instrtype, instrmodel, instrumentid, viewonly, tabindex++, postfixIndexWithUnderscore, jsonStr.toString(), whereClause, dataEntryPrefix, testingDeptId, sdidataid));
                        htmlData.append((isMandatory ? " (R)" : "") + "</td>");
                        htmlData.append("</tr>");
                        if (this.needToShowExtraColumns) {
                            htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_instrumentinventory_extracolumns_" + postfixIndexWithUnderscore + "\">");
                            htmlData.append(this.renderExtraColumns(this.extraColumns, this.qp, instrumentid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                            htmlData.append("</td></tr>");
                        }
                    }
                    this.fieldValueHM.put("instrumentinventory_unmanaged_" + postfixIndexWithUnderscore, isUnmanaged + "");
                    this.fieldValueHM.put("instrumentinventory_mode_" + postfixIndexWithUnderscore, mode);
                    this.fieldValueHM.put("instrumentinventory_relationid_" + postfixIndexWithUnderscore, relationid);
                }
                this.fieldValueHM.put("instrumentinventory_cellselected_" + postfixIndexWithUnderscore_temp, "N");
                this.fieldValueHM.put("instrumentinventory_instrumentdscount_" + postfixIndexWithUnderscore_temp, filteredRelations.getRowCount() + "");
                htmlData.append("</table></td>");
                htmlData.append("</tr>");
                htmlData.append("</table></td>");
                continue;
            }
            if (mergeInvRecord) {
                htmlData.append("<td style=\"" + this.css_MergeContentCellStyle + "\"></td>");
                continue;
            }
            htmlData.append("<td style=\"" + this.css_EmptyContentCellStyle + "\"></td>");
        }
        return htmlData.toString();
    }

    private DataSet fetchSDIData(String extraDataSetColumns, String keyids, String workitemid, String workiteminstance, String paramlistid, String paramlistversionid, String variantid, String datasets) throws SapphireException {
        if (ReagentUtil.isInputEmpty(keyids) || ReagentUtil.isInputEmpty(paramlistid)) {
            return new DataSet();
        }
        String dicols = (extraDataSetColumns.length() > 0 ? extraDataSetColumns + "," : "") + " sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset, sdidata.usersequence, sdidata.s_instrumentid, sdidata.availabilityflag, paramlist.s_instrumenttype, paramlist.s_instrumentmodel ";
        dicols = dicols + ",(select sdiworkitem.testingdepartmentid from sdiworkitem  where sdiworkitem.sdcid=sdidata.sdcid AND sdiworkitem.keyid1=sdidata.keyid1  AND sdiworkitem.keyid2=sdidata.keyid2 AND sdiworkitem.keyid3=sdidata.keyid3  AND sdiworkitem.workitemid=sdidata.sourceworkitemid  AND sdiworkitem.workiteminstance=sdidata.sourceworkiteminstance ) testingdepartmentid";
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT " + dicols);
        sql.append(" FROM sdidata, paramlist ");
        sql.append(" WHERE paramlist.s_instrumenttype is not null ");
        sql.append(" AND sdidata.paramlistid = paramlist.paramlistid ");
        sql.append(" AND sdidata.paramlistversionid=paramlist.paramlistversionid ");
        sql.append(" AND sdidata.variantid = paramlist.variantid");
        if (paramlistid.length() > 0) {
            sql.append(" AND sdidata.keyid1 in( ").append(safeSQL.addIn(keyids, ";")).append(")");
            sql.append(" AND sdidata.paramlistid in(").append(safeSQL.addIn(paramlistid, ";")).append(")");
            if (paramlistversionid.length() > 0) {
                sql.append(" AND sdidata.paramlistversionid in(").append(safeSQL.addIn(paramlistversionid, ";")).append(")");
            }
            if (variantid.length() > 0) {
                sql.append(" AND sdidata.variantid in(").append(safeSQL.addIn(variantid, ";")).append(")");
            }
            if (datasets.length() > 0) {
                sql.append(" AND sdidata.dataset in(").append(safeSQL.addIn(datasets, ";")).append(")");
            }
        }
        return this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private void mergeControlData(DataSet controldata, DataSet controlSdidata, PropertyListCollection visibleColumns, String[] hiddenColumnArr) {
        if (controlSdidata != null && controlSdidata.getRowCount() > 0) {
            String fixedColumns = "sourcetype,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,relationtype,instrmodel,instrtype,tokeyid1,usersequence,availabilityflag,sourceworkitemid,sourceworkitemid,c_workitemversionid,sourceworkiteminstance";
            for (int i = 0; i < controlSdidata.getRowCount(); ++i) {
                int newRow = controldata.addRow();
                controldata.setString(newRow, "isrelationdata", "N");
                controldata.setString(newRow, "sourcetype", "D");
                controldata.setString(newRow, "keyid1", controlSdidata.getString(i, "keyid1", ""));
                controldata.setString(newRow, "keyid2", controlSdidata.getString(i, "keyid2", ""));
                controldata.setString(newRow, "keyid3", controlSdidata.getString(i, "keyid3", ""));
                controldata.setString(newRow, "paramlistid", controlSdidata.getString(i, "paramlistid", ""));
                controldata.setString(newRow, "paramlistversionid", controlSdidata.getString(i, "paramlistversionid", ""));
                controldata.setString(newRow, "variantid", controlSdidata.getString(i, "variantid", ""));
                controldata.setNumber(newRow, "dataset", controlSdidata.getBigDecimal(i, "dataset"));
                controldata.setString(newRow, "s_datasetstatus", controlSdidata.getString(i, "s_datasetstatus", ""));
                controldata.setString(newRow, "relationtype", controlSdidata.getString(i, "s_instrumenttype", ""));
                controldata.setString(newRow, "instrtype", controlSdidata.getString(i, "s_instrumenttype", ""));
                controldata.setString(newRow, "instrmodel", controlSdidata.getString(i, "s_instrumentmodel"));
                controldata.setString(newRow, "tokeyid1", controlSdidata.getString(i, "s_instrumentid", ""));
                controldata.setNumber(newRow, "usersequence", controlSdidata.getBigDecimal(i, "usersequence"));
                controldata.setString(newRow, "availabilityflag", controlSdidata.getString(i, "availabilityflag", ""));
                controldata.setString(newRow, "sourceworkitemid", controlSdidata.getString(i, "sourceworkitemid", ""));
                controldata.setString(newRow, "sourceworkitemid", controlSdidata.getString(i, "sourceworkitemid", ""));
                controldata.setNumber(newRow, "sourceworkiteminstance", controlSdidata.getBigDecimal(i, "sourceworkiteminstance"));
                controldata.setNumber(newRow, "sourceworkiteminstance", controlSdidata.getBigDecimal(i, "sourceworkiteminstance"));
                controldata.setString(newRow, "testingdepartmentid", controlSdidata.getString(i, "testingdepartmentid"));
                for (int v = 0; v < visibleColumns.size(); ++v) {
                    PropertyList pl = visibleColumns.getPropertyList(v);
                    String columnid = this.getColumnAlias(pl.getProperty("columnid", ""));
                    if (columnid == null || columnid.length() <= 0 || fixedColumns.contains(columnid)) continue;
                    String value = controlSdidata.getValue(i, columnid, "");
                    controldata.setValue(newRow, columnid, value);
                }
                for (String columnid : hiddenColumnArr) {
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || fixedColumns.contains(columnid)) continue;
                    String value = controlSdidata.getValue(i, columnid, "");
                    controldata.setValue(newRow, columnid, value);
                }
            }
        }
    }
}

