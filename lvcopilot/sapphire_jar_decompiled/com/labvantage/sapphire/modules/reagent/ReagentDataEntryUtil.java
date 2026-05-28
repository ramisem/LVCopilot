/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.reagent;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.ReagentInstrumentCommonUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReagentDataEntryUtil
extends ReagentInstrumentCommonUtil {
    private final String dataEntryPrefix = "reagentinventory_";
    private final String relationfunction = "Reagent";
    private final String fieldName_trackitemid = "trackitemid";
    private final String fieldName_reagentlotid = "reagentlotid";
    private final String fieldName_amount = "amount";
    private final String fieldName_amounttext = "amounttext";
    private final String fieldName_amountunits = "amountunits";
    private final String fieldName_amountunitstype = "amountunitstype";
    private final String fieldName_includereagenttypeid = "includereagenttypeid";
    private final String fieldName_includereagenttypeversionid = "includereagenttypeversionid";
    private final String fieldName_amountadjusted = "amountadjusted";
    private final String fieldName_prevamount = "prevamount";
    private final String fieldName_originalreagenttypeid = "originalreagenttypeid";
    private final String fieldName_originalreagenttypeversionid = "originalreagenttypeversionid";
    private final String fieldName_reagenttypeid = "reagenttypeid";
    private final String fieldName_reagenttypeversionid = "reagenttypeversionid";
    private final String fieldName_instrumentid = "instrumentid";
    private final String fieldName_instrumenttypeid = "instrumenttypeid";
    private String reagentsource = "";
    private String titleColumn;
    private String viewonlyOption;
    private String reagentLotTitle;
    private String usedconsumabletitle;
    private String adjustedamounttitle;
    private String trackitemTitle;
    private String amountUsedTitle;
    private String recomAmountTitle;
    private String availAmountTitle;
    private String unitTitle;
    private String trackitemSelectionMode;
    private String lookupurl;
    private String ajaxClass;
    private String fromClause;
    private String whereClauseOriginal;
    private String orderByClause;
    private String noReagents;
    private DataSet dsUnits = new DataSet();
    private SafeSQL safeSQL = new SafeSQL();
    private String lotSize;
    private String maxLotSize;
    private String keyid1;
    private String tableid;
    private PropertyListCollection extraConfigColumn = new PropertyListCollection();
    private boolean isUnmanaged = false;
    private boolean showAdjustedAmount = true;
    private String detialtable_primarykeyid1;
    private String detialtable_primarykeyid2;
    private String autopopulatecontainer = "Y";
    private boolean autopopulateusedamount = true;
    private String autoloadedContainerIndicator = "";
    private String autoloadedContainerTooltips = "";
    HashMap<String, String> currAutoloadedTI = new HashMap();
    HashMap<String, String> currAutoloadedTIIndicator = new HashMap();
    private boolean firstReagentLot = true;
    private FormatUtil formatUtil;
    private char decimalSeperator;
    String autoSeletedTrackitem = "N";
    boolean autoadvancetonextcell = true;
    private Map<String, String> tiAvailableQuantity = new HashMap<String, String>();

    public ReagentDataEntryUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request) {
        super(pageContext, pageinfo, request, "reagentsource");
        this.reagentsource = pageinfo.getProperty("reagentsource", "");
        this.extraColumFromcluase = " trackitem,reagentlot ";
        this.extraColumWherecluase = " trackitem.linkkeyid1=reagentlot.reagentlotid and trackitem.trackitemid=? ";
        this.table1 = "reagentlot";
        this.table2 = "trackitem";
        this.extraColumns = this.pagedata.getCollection("extracolumns");
        this.needToShowExtraColumns = this.isExtraColumnAvailable(this.extraColumns);
        this.autopopulateusedamount = this.autoPopulateUsedAmount();
        this.formatUtil = FormatUtil.getInstance(this.connectionInfo);
        this.decimalSeperator = this.formatUtil.getDecimalSeparator();
    }

    public String getHtml() {
        String usabeSize;
        this.keyid1 = this.pagedata.getProperty("keyid1", "");
        if (ReagentUtil.isInputEmpty(this.keyid1)) {
            return this.tp.translate("No keyid1 found in the request");
        }
        String defaultReagentTrackItemLookup = "rc?command=page&page=LV_VLRgntTrackLookup";
        this.tiAvailableQuantity.clear();
        this.fieldValueHM.clear();
        this.titleColumn = this.pagedata.getProperty("titlecolumn", "reagenttypeid");
        PropertyList inventoryInfo = this.pagedata.getPropertyList("InventoryInfo");
        this.viewonlyOption = this.pagedata.getProperty("viewonly", "No");
        this.reagentLotTitle = this.tp.translate(inventoryInfo.getProperty("ReagentLotTitle", ""));
        this.usedconsumabletitle = this.tp.translate(inventoryInfo.getProperty("usedconsumabletitle", "Alt Consumable"));
        this.adjustedamounttitle = this.tp.translate(inventoryInfo.getProperty("adjustedamounttitle", "Adjusted Amount"));
        this.trackitemTitle = this.tp.translate(inventoryInfo.getProperty("TrackitemTitle", ""));
        this.amountUsedTitle = this.tp.translate("Used Amount");
        this.recomAmountTitle = this.tp.translate("Reco. Amount");
        this.availAmountTitle = this.tp.translate("Avl. Amount");
        this.unitTitle = this.tp.translate("Unit");
        this.trackitemSelectionMode = inventoryInfo.getProperty("TrackitemSelectionMode", this.selectionMode_Lookup);
        this.lookupurl = inventoryInfo.getProperty("lookupurl", defaultReagentTrackItemLookup);
        this.lookupurl = this.lookupurl.trim().length() == 0 ? defaultReagentTrackItemLookup : this.lookupurl;
        this.ajaxClass = inventoryInfo.getProperty("AjaxClass", "");
        this.autopopulatecontainer = inventoryInfo.getProperty("AutoSelectReagentContainer", "Y");
        this.autoadvancetonextcell = (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanMode) || this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) && inventoryInfo.getProperty("autoadvancetonextcell", "Y").equalsIgnoreCase("Y");
        this.fromClause = inventoryInfo.getProperty("FromClause", "");
        this.whereClauseOriginal = inventoryInfo.getProperty("WhereClause", "");
        this.orderByClause = inventoryInfo.getProperty("OrderByClause", "");
        this.noReagents = this.tp.translate("No consumable found");
        if (this.autopopulatecontainer.equalsIgnoreCase("Y") && (this.whereClauseOriginal.trim().equals("") || this.fromClause.trim().equals("") || this.orderByClause.trim().equals(""))) {
            return "<table><tr><td><font style=\"color:Red\">If the Auto Load Consumable Container is Yes,Then the FromClause, WhereClause, and OrderByClause must be specified.</font></td></tr></table>";
        }
        this.dsUnits = this.qp.getSqlDataSet("SELECT DISTINCT keyid1 FROM categoryitem cat ,units un WHERE sdcid = 'Units' AND categoryid in ('MassUnits','VolumeUnits') AND cat.keyid1=un.unitsid order by keyid1");
        HashMap reagentLotProps = this.sdcProcessor.getSDCProperties("LV_ReagentLot");
        this.lotSize = usabeSize = (String)reagentLotProps.get("keyidusablesize");
        this.maxLotSize = usabeSize;
        this.fieldValueHM.put("autoadvancetonextcell", this.autoadvancetonextcell ? "Y" : "N");
        this.fieldValueHM.put("adhocconsumablepageid", this.promptForAdhocConsumable);
        this.fieldValueHM.put("adhoctablename", this.adhoctablename);
        if (this.reagentsource.equalsIgnoreCase(this.reagentlotrecipe) || this.reagentsource.equalsIgnoreCase(this.transferexecutionreagent)) {
            this.tableid = this.reagentsource;
            this.detialtable_primarykeyid1 = this.reagentsource.equalsIgnoreCase(this.transferexecutionreagent) ? "transferexecutionid" : "reagentlotid";
            this.detialtable_primarykeyid2 = this.reagentsource.equalsIgnoreCase(this.transferexecutionreagent) ? "transferexecutionreagentid" : "reagentlotrecipeitemid";
            return this.getHtmlForReagentDetailTable();
        }
        if (this.reagentsource.equalsIgnoreCase(this.qcbatch)) {
            this.tableid = this.sdcProcessor.getProperty(this.sdcid, "tableid");
            return this.getHtmlForQCBatchReagent();
        }
        return this.getHtmlForSDIRelation();
    }

    private String getHtmlForSDIRelation() {
        String mandatoryColumns;
        StringBuffer htmlData = new StringBuffer();
        String filldownTooltips = "To fill down the data for specified set of samples, please select starting row by CLICK on the " + this.reagentLotTitle + " or " + this.trackitemTitle + " or " + this.amountUsedTitle + " and the end row by SHIFT+CLICK in the same way. If only starting row is selected, the data will be filled down from starting row to end. If nothing is selected, the first row will be considered as the starting row for backward compatibility.";
        PropertyListCollection datasetInfo = this.pagedata.getCollection("DataSetInfo");
        String viewonlyOption = this.pagedata.getProperty("viewonly", "No");
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String dsSelectClause = mandatoryColumns = " sdidata.keyid1,sdidata.keyid2,sdidata.keyid3,sdidata.paramlistid,sdidata.paramlistversionid,sdidata.variantid,sdidata.dataset,sdidata.s_datasetstatus";
        String[] hiddenColumnArr = new String[datasetInfo.size()];
        int hiddenIndx = 0;
        HashMap<String, String> sdidataColumns = OpalUtil.getColumnDataTypeMap("sdidata", this.qp);
        for (int i = 0; i < datasetInfo.size(); ++i) {
            String columnMode;
            PropertyList columnProps = datasetInfo.getPropertyList(i);
            String columnid = columnProps.getProperty("columnid", "");
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
        String extraplcolumns = "( SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + "     ) unmanagedflag ";
        extraplcolumns = extraplcolumns + ", ( SELECT managecontainerinventoryflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + "     ) managecontainerinventoryflag ";
        extraplcolumns = extraplcolumns + ",(SELECT instrumenttypeid FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + " ) instrumenttypeid";
        extraplcolumns = extraplcolumns + ",(SELECT instrumentmodelid FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + " ) instrumentmodelid";
        extraplcolumns = extraplcolumns + ",(SELECT instrumentfieldid FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + " ) instrumentfieldid";
        extraplcolumns = extraplcolumns + ",( SELECT targetconcentration FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + " ) typetargetconcentration";
        String extrawicolumns = "( SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + " ) unmanagedflag";
        extrawicolumns = extrawicolumns + ", ( SELECT managecontainerinventoryflag FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + " ) managecontainerinventoryflag";
        extrawicolumns = extrawicolumns + ",(SELECT instrumenttypeid FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + " ) instrumenttypeid";
        extrawicolumns = extrawicolumns + ",(SELECT instrumentmodelid FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + " ) instrumentmodelid";
        extrawicolumns = extrawicolumns + ",(SELECT instrumentfieldid FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + " ) instrumentfieldid";
        extrawicolumns = extrawicolumns + ", ( SELECT targetconcentration FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + "     ) typetargetconcentration ";
        String titleColumn = this.pagedata.getProperty("titlecolumn", "");
        if (titleColumn.length() > 0) {
            extraplcolumns = extraplcolumns + ", ( SELECT " + titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=sdidatarelation.originalreagenttypeid     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdidatarelation", "originalreagenttypeversionid") + "     ) titlecolumn ";
            extrawicolumns = extrawicolumns + ", ( SELECT " + titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=sdiworkitemrelation.originalreagenttypeid     AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase("sdiworkitemrelation", "originalreagenttypeversionid") + "     ) titlecolumn ";
        } else {
            extraplcolumns = extraplcolumns + ", sdidatarelation.originalreagenttypeid titlecolumn";
            extrawicolumns = extrawicolumns + ", sdiworkitemrelation.originalreagenttypeid titlecolumn";
        }
        if (sdcid.equalsIgnoreCase("Sample")) {
            extraplcolumns = extraplcolumns + ", (select s_sample.samplestatus from s_sample where s_sample.s_sampleid=sdidatarelation.keyid1) samplestatus";
            extrawicolumns = extrawicolumns + ", (select s_sample.samplestatus from s_sample where s_sample.s_sampleid=sdiworkitemrelation.keyid1) samplestatus";
        }
        if (dsSelectClause.length() > 0) {
            extraplcolumns = extraplcolumns + "," + dsSelectClause;
            extrawicolumns = extrawicolumns + "," + dsSelectClause;
        }
        DataSet controldata = new DataSet();
        try {
            controldata = this.fetchControlData("Reagent", extraplcolumns, extrawicolumns, sdcid, keyid1, workitemid, workiteminstance, paramListId, paramListVersionId, variantId, dataset);
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        if (controldata != null && controldata.size() > 0) {
            this.firstReagentLot = true;
            this.fieldValueHM.put("currentparamlistid", paramListId);
            this.fieldValueHM.put("currentparamlistversionid", paramListVersionId);
            this.fieldValueHM.put("currentvariantid", variantId);
            this.fieldValueHM.put("selectedwi", selectedWis);
            this.fieldValueHM.put("selectedds", selectedDatasets);
            this.fieldValueHM.put("viewonly", viewonlyOption);
            controldata.sort("keyid1,paramlistid,paramlistversionid,variantid,dataset");
            ArrayList<DataSet> samples = controldata.getGroupedDataSets("keyid1,paramlistid,paramlistversionid,variantid,dataset");
            LinkedHashMap<String, String> consumableHeaderTitles = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> consumableHeaderTitlesTooltip = new LinkedHashMap<String, String>();
            ArrayList<String> uniqueSDIWI = new ArrayList<String>();
            ArrayList<String> consumbaleTypes = new ArrayList<String>();
            String filteredConsumablesStr = "";
            controldata.sort("sourcetype d,relationid");
            for (int i = 0; i < controldata.size(); ++i) {
                String consumbaleType = controldata.getValue(i, "originalreagenttypeid", "");
                if (consumbaleType.trim().length() <= 0 || consumbaleTypes.contains(consumbaleType)) continue;
                consumableHeaderTitles.put(consumbaleType, controldata.getValue(i, "titlecolumn", consumbaleType));
                consumableHeaderTitlesTooltip.put(consumbaleType, this.getToolTip(controldata, i));
                consumbaleTypes.add(consumbaleType);
                filteredConsumablesStr = filteredConsumablesStr + (filteredConsumablesStr.length() > 0 ? ";" : "") + consumbaleType;
            }
            StringBuffer strRLTIJson = new StringBuffer();
            htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
            htmlData.append("<table id=\"dataEntryTable\" class=\"" + this.css_maintFormTable + "\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
            htmlData.append("<tr style=\"" + this.getFixedPositionStyleForTop(0) + "\" >");
            htmlData.append(this.getConfiguredColumnsHeader(true, visibleColumns));
            int consumableTypeInx = -1;
            for (String consumbaleType : consumbaleTypes) {
                String reagentTitle = (String)consumableHeaderTitles.get(consumbaleType);
                htmlData.append("<th class=\"" + this.css_columnHeader + "\"><table width=100% border=0><tr><td style=\"font-weight:bold;\" align=center title='" + (String)consumableHeaderTitlesTooltip.get(consumbaleType) + "'>" + SafeHTML.encodeForHTML(reagentTitle) + "</td><td width=15>&nbsp;</td><td width=15><a onClick=\"javascript:reagentInventory.fillDown(" + ++consumableTypeInx + ");\"><img src='" + this.filldownImage + "' border=0 title='" + this.tp.translate(filldownTooltips) + "'></a></td></tr></table></th>");
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
                this.fieldValueHM.put("reagentinventory_keyid1_" + ++selectedDSIndx, key1);
                this.fieldValueHM.put("reagentinventory_keyid2_" + selectedDSIndx, key2);
                this.fieldValueHM.put("reagentinventory_keyid3_" + selectedDSIndx, key3);
                this.fieldValueHM.put("reagentinventory_paramlistid_" + selectedDSIndx, plid);
                this.fieldValueHM.put("reagentinventory_paramlistversionid_" + selectedDSIndx, plver);
                this.fieldValueHM.put("reagentinventory_variantid_" + selectedDSIndx, vrnt);
                this.fieldValueHM.put("reagentinventory_dataset_" + selectedDSIndx, dsno);
                this.fieldValueHM.put("reagentinventory_modifiableflag_" + selectedDSIndx, modifiableflag);
                this.fieldValueHM.put("reagentinventory_sourceworkitemid_" + selectedDSIndx, sourceworkitemid);
                this.fieldValueHM.put("reagentinventory_sourceworkiteminstance_" + selectedDSIndx, sourceworkiteminstance);
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    this.fieldValueHM.put("reagentinventory_" + columnid + "_" + selectedDSIndx, sdirelations.getValue(0, columnid, ""));
                }
                htmlData.append("<tr>");
                htmlData.append(this.getConfiguredColumnsValue(selectedDSIndx, true, visibleColumns, sdirelations, 0));
                htmlData.append(this.getContentCellHtml(consumbaleTypes, sdirelations, uniqueSDIWI, strRLTIJson, selectedDSIndx, filteredConsumablesStr));
                htmlData.append(this.getButtonForAdhocConsumable(selectedDSIndx));
                htmlData.append("</tr>");
            }
            htmlData.append(this.getNotesForRequiredConsumableInstrument());
            htmlData.append("</table>");
            htmlData.append("</div>");
            this.fieldValueHM.put("reagentinventory_sampleCount", selectedDSIndx + 1 + "");
            this.fieldValueHM.put("reagentinventory_reagentCount", consumbaleTypes.size() + "");
            this.fieldValueHM.put("autoselectedtrackitem", this.autoSeletedTrackitem);
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
            if (strRLTIJson.length() > 0) {
                htmlData.append("<script>");
                htmlData.append("var jsonObj={");
                htmlData.append(strRLTIJson.toString());
                htmlData.append("};");
                htmlData.append("</script>");
            }
        } else {
            htmlData.append(this.noReagents);
        }
        htmlData.append(this.addHiddenFields(this.fieldValueHM));
        return htmlData.toString();
    }

    private String getHtmlForReagentDetailTable() {
        StringBuffer htmlData = new StringBuffer();
        String reagentlotstageid = this.pagedata.getProperty("reagentlotstageid", "");
        String amountrecommended = "";
        String amountrecommendedunits = "";
        String amountrecommendedunitstype = "";
        String recoAmount = "";
        String recoAmountUnits = "";
        String avlamount = "";
        String sourcekeyid1 = "";
        String sourcekeyid2 = "";
        String amountadjusted = "";
        int tabindex = 1;
        HashMap<String, String> hm = new HashMap<String, String>();
        int selectedDSIndx = -1;
        ArrayList<String> uniqueReagentTypes = new ArrayList<String>();
        DataSet dsReagentLotManageInvtFlag = new DataSet();
        DataSet dsReagentTypeManageInvtFlag = new DataSet();
        DataSet controldata = new DataSet();
        DataSet dsReagentLot = new DataSet();
        StringBuffer sql = new StringBuffer();
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String titleColumnSelect = "";
        titleColumnSelect = "( SELECT " + this.titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) titlecolumn ";
        String mandatoryColumns = " " + this.detialtable_primarykeyid1 + "," + this.detialtable_primarykeyid2;
        mandatoryColumns = mandatoryColumns + ",originalreagenttypeid,originalreagenttypeversionid,trackitemid,instrumentid";
        String linkedColumns = "(SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) unmanagedflag";
        linkedColumns = linkedColumns + ",(SELECT instrumenttypeid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumenttypeid";
        linkedColumns = linkedColumns + ",(SELECT instrumentmodelid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumentmodelid";
        linkedColumns = linkedColumns + ",(SELECT instrumentfieldid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumentfieldid";
        linkedColumns = linkedColumns + ", ( SELECT targetconcentration FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.tableid + ".originalreagenttypeid AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) typetargetconcentration ";
        linkedColumns = linkedColumns + ",(select qtycurrent from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtycurrent";
        linkedColumns = linkedColumns + ",(select qtyunits from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtyunits";
        linkedColumns = linkedColumns + ",(select qtycurrenttype from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtycurrenttype";
        if (this.isTransferExecutionReagents) {
            mandatoryColumns = mandatoryColumns + ",reagenttypeid sourcekeyid1,reagenttypeversionid sourcekeyid2,reagentlotid trackitemreagentlotid,useamount amount,useamount amounttext,useamountunits amountunits,useamountunitstype amountunitstype,recommendedamount amountrecommendedtext,useamountadjusted amountadjusted,recommendedamountunits amountrecommendedunits,recommendedamountunitstype amountrecommendedunitstype, '' reagentlotstageid";
        } else {
            mandatoryColumns = mandatoryColumns + ",includereagenttypeid sourcekeyid1,includereagenttypeversionid sourcekeyid2,trackitemreagentlotid,amount,amounttext,amountunits,amountunitstype,amountrecommendedtext,amountadjusted,amountrecommendedunits,amountrecommendedunitstype,reagentlotstageid";
            linkedColumns = linkedColumns + ",(SELECT reagenttypeid FROM reagentlot WHERE reagentlot.reagentlotid=" + this.tableid + ".reagentlotid ) lotreagenttypeid";
            linkedColumns = linkedColumns + ",(SELECT reagenttypeversionid FROM reagentlot WHERE reagentlot.reagentlotid=" + this.tableid + ".reagentlotid ) lotreagenttypeversionid";
        }
        StringBuffer dsSelectClause = new StringBuffer(mandatoryColumns + "," + linkedColumns + "," + titleColumnSelect);
        String[] hiddenColumnArr = new String[this.extraConfigColumn.size()];
        this.populateExtraColumns(this.extraConfigColumn, mandatoryColumns, dsSelectClause, hiddenColumnArr, visibleColumns);
        this.fieldValueHM.put("viewonly", this.viewonlyOption);
        this.fieldValueHM.put("reagentlotstageid", reagentlotstageid);
        htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
        htmlData.append("<table id=\"dataEntryTable\" class=\"" + this.css_maintFormTable + "\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
        this.safeSQL.reset();
        sql.append("SELECT " + dsSelectClause + " FROM " + this.tableid);
        sql.append(" WHERE " + this.detialtable_primarykeyid1 + " = " + this.safeSQL.addVar(this.keyid1) + " ");
        if (this.reagentsource.equalsIgnoreCase(this.reagentlotrecipe)) {
            if (reagentlotstageid != null && reagentlotstageid.length() > 0) {
                sql.append(" AND reagentlotstageid=" + this.safeSQL.addVar(reagentlotstageid));
            }
            sql.append(" AND recipeitemtype='Reagent'");
        }
        controldata = this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
        controldata.sort("USERSEQUENCE");
        ArrayList<DataSet> reagentTypes = controldata.getGroupedDataSets("originalreagenttypeid,originalreagenttypeversionid");
        dsReagentLotManageInvtFlag = this.getReagentManageInvtFlagDS(controldata, "lot");
        dsReagentTypeManageInvtFlag = this.getReagentManageInvtFlagDS(controldata, "type");
        StringBuffer strRLTIJson = new StringBuffer();
        if (controldata.getRowCount() > 0) {
            this.firstReagentLot = true;
            htmlData.append("<tr>");
            htmlData.append(this.getVisibleColumnHtml(visibleColumns));
            ArrayList<Object> recipes = new ArrayList();
            controldata.sort(this.detialtable_primarykeyid1);
            recipes = controldata.getGroupedDataSets(this.detialtable_primarykeyid1);
            for (DataSet dataSet : reagentTypes) {
                String reagentTypeId = dataSet.getValue(0, "originalreagenttypeid", "");
                String titlevalue = dataSet.getValue(0, "titlecolumn", reagentTypeId);
                htmlData.append(this.getHeaderColumnHtml(titlevalue));
            }
            htmlData.append("</tr>");
            for (DataSet dataSet : recipes) {
                ++selectedDSIndx;
                String childlotid = dataSet.getString(0, this.detialtable_primarykeyid1, "");
                String reagentlotrecipeitemid = dataSet.getString(0, this.detialtable_primarykeyid2, "");
                hm.clear();
                hm.put(this.detialtable_primarykeyid1, childlotid);
                htmlData.append("<tr>");
                htmlData.append(this.getVisibleColumnValueHtml(visibleColumns, dataSet, 0));
                this.fieldValueHM.put("reagentinventory_" + this.detialtable_primarykeyid1 + "_" + selectedDSIndx, childlotid);
                this.fieldValueHM.put("reagentinventory_" + this.detialtable_primarykeyid2 + "_" + selectedDSIndx, reagentlotrecipeitemid);
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    this.fieldValueHM.put("reagentinventory_" + columnid + "_" + selectedDSIndx, dataSet.getValue(0, columnid, ""));
                }
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                DataSet filteredRows = controldata.getFilteredDataSet(hm);
                int regTypeIndex = -1;
                for (DataSet dsReagentType : reagentTypes) {
                    String tiAvailAmount;
                    String[] tiAvailAmountArr;
                    String mode;
                    String amount;
                    String originalreagenttypeid = dsReagentType.getValue(0, "originalreagenttypeid", "");
                    String originalreagenttypeversionid = dsReagentType.getValue(0, "originalreagenttypeversionid", "");
                    String lotreagenttypeid = dsReagentType.getValue(0, "lotreagenttypeid", "");
                    String lotreagenttypeversionid = dsReagentType.getValue(0, "lotreagenttypeversionid", "");
                    this.isUnmanaged = dsReagentType.getString(0, "unmanagedflag", "N").equalsIgnoreCase("Y");
                    String instrumenttypeid = dsReagentType.getString(0, "instrumenttypeid", "");
                    String instrumentmodelid = dsReagentType.getString(0, "instrumentmodelid", "");
                    String instrumentfieldid = dsReagentType.getString(0, "instrumentfieldid", "");
                    String colIndx = selectedDSIndx + "s" + ++regTypeIndex;
                    hm.clear();
                    hm.put("originalreagenttypeid", originalreagenttypeid);
                    hm.put("originalreagenttypeversionid", originalreagenttypeversionid.length() > 0 ? originalreagenttypeversionid : null);
                    int findRow = filteredRows.findRow(hm);
                    this.fieldValueHM.put("reagentinventory_sourceofrt_" + colIndx, findRow < 0 ? "skip" : this.reagentsource);
                    if (findRow < 0) {
                        htmlData.append("<td style=\"" + this.css_EmptyContentCellStyle + "\"></td>");
                        continue;
                    }
                    String instrumentid = filteredRows.getString(findRow, "instrumentid", "");
                    String primaryColumnValue1 = filteredRows.getString(findRow, this.detialtable_primarykeyid1, "");
                    String primaryColumnValue2 = filteredRows.getString(findRow, this.detialtable_primarykeyid2, "");
                    String trackitemid = filteredRows.getValue(findRow, "trackitemid");
                    String reagentlotid = filteredRows.getValue(findRow, "trackitemreagentlotid");
                    String amount_orig = amount = filteredRows.getValue(findRow, "amounttext", "");
                    String amountunits = filteredRows.getValue(findRow, "amountunits", "");
                    String amountunitsType = filteredRows.getValue(findRow, "amountunitstype", "");
                    amountrecommended = filteredRows.getValue(findRow, "amountrecommendedtext", "");
                    amountrecommendedunits = filteredRows.getValue(findRow, "amountrecommendedunits", "");
                    amountrecommendedunitstype = filteredRows.getValue(findRow, "amountrecommendedunitstype", "");
                    recoAmountUnits = filteredRows.getValue(findRow, "amountrecommendedunits", "");
                    String qtycurrent = filteredRows.getValue(findRow, "qtycurrent", "");
                    String qtyunits = filteredRows.getValue(findRow, "qtyunits", "");
                    String qtycurrenttype = filteredRows.getValue(findRow, "qtycurrenttype", "");
                    sourcekeyid1 = filteredRows.getString(findRow, "sourcekeyid1", "");
                    sourcekeyid2 = filteredRows.getString(findRow, "sourcekeyid2", "");
                    amountadjusted = filteredRows.getValue(findRow, "amountadjusted", "");
                    String typetargetconcentration = filteredRows.getValue(findRow, "typetargetconcentration", "");
                    avlamount = this.getDisplayValueUnit(qtycurrent, qtyunits, qtycurrenttype);
                    String hideUnmanagedRT = "display:" + (this.isUnmanaged ? "none" : "");
                    dsReagentLot = this.getReagentLot(filteredRows, findRow);
                    String jsonStr = this.buildJSONStr(reagentlotid, reagentlotrecipeitemid, "", "", "", "", "", originalreagenttypeid, originalreagenttypeversionid);
                    String string = mode = !this.isUnmanaged && reagentlotid.length() > 0 || this.isUnmanaged && amount.trim().length() > 0 ? "Edit" : "Add";
                    if (reagentlotid.length() == 0 && this.autopopulatecontainer.equalsIgnoreCase("Y") && !viewonly && (trackitemid = (tiAvailAmountArr = StringUtil.split(tiAvailAmount = this.getTrackItemWithAvailQuantity(dsReagentLot, typetargetconcentration, amountrecommended, amountrecommendedunits, amountrecommendedunitstype), this.specialDelimer))[0]).length() > 0) {
                        reagentlotid = tiAvailAmountArr[1];
                        avlamount = tiAvailAmountArr[2];
                        amountadjusted = tiAvailAmountArr[3];
                        sourcekeyid1 = tiAvailAmountArr[4];
                        sourcekeyid2 = tiAvailAmountArr[5];
                        this.autoSeletedTrackitem = "Y";
                    }
                    if (this.autopopulateusedamount && amount.length() == 0) {
                        amount = amountadjusted;
                        amountunits = amountrecommendedunits;
                        amountunitsType = amountrecommendedunitstype;
                        this.autoSeletedTrackitem = "Y";
                    }
                    this.buildRLTIJson(strRLTIJson, uniqueReagentTypes, originalreagenttypeid, originalreagenttypeversionid, dsReagentLot, dsReagentLotManageInvtFlag);
                    boolean isVirtuallot = this.isVirtualReagentLot(reagentlotid);
                    this.fieldValueHM.put("reagentinventory_virtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                    this.fieldValueHM.put("reagentinventory_mode_" + colIndx, mode);
                    this.fieldValueHM.put("reagentinventory_primarykey1_" + colIndx, primaryColumnValue1);
                    this.fieldValueHM.put("reagentinventory_primarykey2_" + colIndx, primaryColumnValue2);
                    this.fieldValueHM.put("reagentinventory_reagenttypeid_" + colIndx, originalreagenttypeid);
                    this.fieldValueHM.put("reagentinventory_reagenttypeversionid_" + colIndx, originalreagenttypeversionid);
                    this.fieldValueHM.put("reagentinventory_lotreagenttypeid_" + colIndx, lotreagenttypeid);
                    this.fieldValueHM.put("reagentinventory_lotreagenttypeversionid_" + colIndx, lotreagenttypeversionid);
                    HashMap<String, String> hmReagentLot = new HashMap<String, String>();
                    hmReagentLot.put("reagentlotid", reagentlotid);
                    DataSet dsRLMCFlag = dsReagentLotManageInvtFlag.getFilteredDataSet(hmReagentLot);
                    String managecontainerflagRL = dsRLMCFlag.getString(0, "managecontainerinventoryflag", "Y");
                    String managecontainerflagRT = this.getManageInvForType(dsReagentTypeManageInvtFlag, originalreagenttypeid, originalreagenttypeversionid);
                    htmlData.append("<td style=\"" + this.css_ContentCellStyle + "\">");
                    htmlData.append("<table>");
                    this.renderConsumableLotRow(htmlData, originalreagenttypeid, originalreagenttypeversionid, "", "", "", reagentlotid, trackitemid, avlamount, dsReagentLot, colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, true, amountrecommended, amountrecommendedunits, amountrecommendedunitstype, false);
                    htmlData.append(this.getAdjustedHtml(colIndx, sourcekeyid1, sourcekeyid2, originalreagenttypeid, originalreagenttypeversionid, amountadjusted, amountadjusted, recoAmountUnits));
                    this.renderContainerRow(htmlData, originalreagenttypeid, originalreagenttypeversionid, "", "", "", jsonStr, reagentlotid, trackitemid, amount, amountunits, amountunitsType, dsReagentLot, "Edit", colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, true, true, amountrecommended, amountrecommendedunits, amountrecommendedunitstype, instrumenttypeid, instrumentfieldid, false);
                    if (this.needToShowExtraColumns) {
                        htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_reagentinventory_extracolumns_" + colIndx + "\">");
                        htmlData.append(isVirtuallot ? "&nbsp" : this.renderExtraColumns(this.extraColumns, this.qp, trackitemid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                        htmlData.append("</td></tr>");
                    }
                    htmlData.append("</table></td>");
                    this.fieldValueHM.put("reagentinventory_unmanaged_" + colIndx, this.isUnmanaged ? "Y" : "N");
                    this.fieldValueHM.put("reagentinventory_currlasttrackitemid_" + colIndx, trackitemid);
                    this.fieldValueHM.put("reagentinventory_amountunitstype_" + colIndx, amountunitsType);
                    if (mode.equalsIgnoreCase("edit")) {
                        this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, amount_orig);
                        this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, trackitemid);
                        this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, reagentlotid);
                        this.fieldValueHM.put("reagentinventory_prevvirtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                    } else {
                        this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, "0");
                        this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, "");
                        this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, "");
                        this.fieldValueHM.put("reagentinventory_prevreagentlotdefault_" + colIndx, reagentlotid);
                        this.fieldValueHM.put("reagentinventory_prevamountdefault_" + colIndx, amount);
                        this.fieldValueHM.put("reagentinventory_prevtrackitemiddefault_" + colIndx, trackitemid);
                    }
                    this.fieldValueHM.put("reagentinventory_prevamountunits_" + colIndx, amountunits);
                    this.fieldValueHM.put("reagentinventory_prevamountunitstype_" + colIndx, amountunitsType);
                    this.fieldValueHM.put("reagentinventory_prevqtycurrent_" + colIndx, avlamount);
                    this.fieldValueHM.put("reagentinventory_instrumentid_" + colIndx, instrumentid);
                    this.fieldValueHM.put("reagentinventory_instrumenttypeid_" + colIndx, instrumenttypeid);
                    this.fieldValueHM.put("reagentinventory_instrumentmodelid_" + colIndx, instrumentmodelid);
                    this.fieldValueHM.put("reagentinventory_instrumentfieldid_" + colIndx, instrumentfieldid);
                }
                htmlData.append("</tr>");
            }
            this.fieldValueHM.put("reagentinventory_sampleCount", recipes.size() + "");
            this.fieldValueHM.put("reagentinventory_reagentCount", reagentTypes.size() + "");
            this.fieldValueHM.put("reagentinventory_sourceofrt", this.reagentsource);
            this.fieldValueHM.put("autoselectedtrackitem", this.autoSeletedTrackitem);
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
        } else {
            htmlData.append("<tr><td nowrap>" + this.noReagents + "</td></tr>");
            this.fieldValueHM.put("reagentinventory_" + this.detialtable_primarykeyid1 + "_0", this.keyid1);
        }
        htmlData.append(this.addHiddenFields(this.fieldValueHM));
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

    private String getHtmlForQCBatchReagent() {
        String fieldName_qcbatchid = "qcbatchid";
        String fieldName_qcbatchsampletypeid = "s_qcbatchsampletypeid";
        String fieldName_qcsampletype = "qcsampletype";
        StringBuffer htmlData = new StringBuffer();
        String avlamount = "";
        int tabindex = 1;
        HashMap<String, String> hm = new HashMap<String, String>();
        int selectedDSIndx = -1;
        ArrayList<String> uniqueReagentTypes = new ArrayList<String>();
        DataSet dsReagentLotManageInvtFlag = new DataSet();
        DataSet dsReagentTypeManageInvtFlag = new DataSet();
        DataSet controldata = new DataSet();
        DataSet dsReagentLot = new DataSet();
        StringBuffer sql = new StringBuffer();
        String mandatoryColumns = " qcbatchid,reagenttypeid,reagenttypeversionid,reagentlotid,trackitemid,amount,amounttext,amountunits,amountunitstype,amountrecommended,amountrecommendedunits,amountrecommendedunitstype,instrumentid,amountadjusted,(case when originalreagenttypeid is null then reagenttypeid else originalreagenttypeid end) originalreagenttypeid,(case when originalreagenttypeid is null then reagenttypeversionid else originalreagenttypeversionid end) originalreagenttypeversionid ";
        String linkedColumns = "(SELECT unmanagedflag FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) unmanagedflag";
        linkedColumns = linkedColumns + ",(SELECT instrumenttypeid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumenttypeid";
        linkedColumns = linkedColumns + ",(SELECT instrumentmodelid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumentmodelid";
        linkedColumns = linkedColumns + ",(SELECT instrumentfieldid FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) instrumentfieldid";
        linkedColumns = linkedColumns + ", ( SELECT targetconcentration FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) typetargetconcentration ";
        linkedColumns = linkedColumns + ",(select qtycurrent from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtycurrent";
        linkedColumns = linkedColumns + ",(select qtyunits from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtyunits";
        linkedColumns = linkedColumns + ",(select qtycurrenttype from trackitem where trackitem.trackitemid=" + this.tableid + ".trackitemid ) qtycurrenttype";
        String titleColumnSelect = "( SELECT " + this.titleColumn + " FROM reagenttype WHERE reagenttype.reagenttypeid=" + this.resloveReagentType(this.tableid) + " AND reagenttype.reagenttypeversionid=" + this.resolveCurrentVersionCluase(this.tableid, "originalreagenttypeversionid") + " ) titlecolumn ";
        String reagentsource = "";
        String primaryColumnId = "";
        if (this.isQCBatchReagent) {
            this.extraConfigColumn = this.pagedata.getCollection("qcbatchreagent");
            reagentsource = this.sdcid;
            primaryColumnId = "s_qcbatchreagentid";
            mandatoryColumns = mandatoryColumns + ",sourceflag ";
        } else {
            this.extraConfigColumn = this.pagedata.getCollection("qcbatchsampletype");
            reagentsource = this.sdcid;
            primaryColumnId = fieldName_qcbatchsampletypeid;
            mandatoryColumns = mandatoryColumns + ",amountscopeflag";
        }
        StringBuffer dsSelectClause = new StringBuffer(mandatoryColumns + "," + linkedColumns + "," + primaryColumnId + "," + titleColumnSelect);
        PropertyListCollection visibleColumns = new PropertyListCollection();
        String[] hiddenColumnArr = new String[this.extraConfigColumn.size()];
        this.populateExtraColumns(this.extraConfigColumn, mandatoryColumns, dsSelectClause, hiddenColumnArr, visibleColumns);
        this.fieldValueHM.put("viewonly", this.viewonlyOption);
        htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
        htmlData.append("<table id=\"dataEntryTable\" class=\"" + this.css_maintFormTable + "\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
        this.safeSQL.reset();
        sql.append("SELECT " + dsSelectClause + " FROM " + this.tableid);
        sql.append(" WHERE qcbatchid = " + this.safeSQL.addVar(this.keyid1) + " ");
        if (this.isQCBatchSampleType) {
            sql.append(" AND REAGENTTYPEID is not null");
        }
        controldata = this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
        controldata.sort("originalreagenttypeid,originalreagenttypeversionid");
        ArrayList<DataSet> reagentTypes = controldata.getGroupedDataSets("originalreagenttypeid,originalreagenttypeversionid");
        dsReagentLotManageInvtFlag = this.getReagentManageInvtFlagDS(controldata, "lot");
        dsReagentTypeManageInvtFlag = this.getReagentManageInvtFlagDS(controldata, "type");
        StringBuffer strRLTIJson = new StringBuffer();
        if (controldata.getRowCount() > 0) {
            this.firstReagentLot = true;
            htmlData.append("<tr style=\"" + this.getFixedPositionStyleForTop(0) + "\" >");
            htmlData.append(this.getConfiguredColumnsHeader(true, visibleColumns));
            ArrayList<Object> qcbatches = new ArrayList();
            if (this.isQCBatchSampleType) {
                controldata.sort(fieldName_qcbatchsampletypeid);
                qcbatches = controldata.getGroupedDataSets(fieldName_qcbatchsampletypeid);
            } else {
                controldata.sort(fieldName_qcbatchid);
                qcbatches = controldata.getGroupedDataSets(fieldName_qcbatchid);
            }
            for (DataSet dataSet : reagentTypes) {
                String reagentTypeId = dataSet.getValue(0, "originalreagenttypeid", "");
                String titlevalue = dataSet.getValue(0, "titlecolumn", reagentTypeId);
                htmlData.append(this.getHeaderColumnHtml(titlevalue));
            }
            htmlData.append("</tr>");
            for (DataSet dataSet : qcbatches) {
                String qcbatchid = dataSet.getString(0, fieldName_qcbatchid, "");
                String qcbatchsampletype = dataSet.getString(0, fieldName_qcsampletype, "");
                htmlData.append("<tr>");
                htmlData.append(this.getConfiguredColumnsValue(++selectedDSIndx, true, visibleColumns, dataSet, 0));
                this.fieldValueHM.put("reagentinventory_" + fieldName_qcbatchid + "_" + selectedDSIndx, qcbatchid);
                this.fieldValueHM.put("reagentinventory_" + fieldName_qcsampletype + "_" + selectedDSIndx, qcbatchsampletype);
                for (int i = 0; i < hiddenColumnArr.length; ++i) {
                    String columnid = hiddenColumnArr[i];
                    if ((columnid = this.getColumnAlias(columnid)) == null || columnid.length() <= 0 || mandatoryColumns.contains(columnid)) continue;
                    this.fieldValueHM.put("reagentinventory_" + columnid + "_" + selectedDSIndx, dataSet.getValue(0, columnid, ""));
                }
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                int regTypeIndex = -1;
                for (DataSet dsReagentType : reagentTypes) {
                    String tiAvailAmount;
                    String[] tiAvailAmountArr;
                    String amount;
                    String originalreagenttypeid = dsReagentType.getValue(0, "originalreagenttypeid", "");
                    String originalreagenttypeversionid = dsReagentType.getValue(0, "originalreagenttypeversionid", "");
                    this.isUnmanaged = dsReagentType.getString(0, "unmanagedflag", "N").equalsIgnoreCase("Y");
                    String instrumenttypeid = dsReagentType.getString(0, "instrumenttypeid", "");
                    String instrumentmodelid = dsReagentType.getString(0, "instrumentmodelid", "");
                    String instrumentfieldid = dsReagentType.getString(0, "instrumentfieldid", "");
                    String colIndx = selectedDSIndx + "s" + ++regTypeIndex;
                    hm.clear();
                    hm.put("originalreagenttypeid", originalreagenttypeid);
                    hm.put("originalreagenttypeversionid", originalreagenttypeversionid.length() > 0 ? originalreagenttypeversionid : null);
                    if (this.isQCBatchReagent) {
                        String sourceflag = dsReagentType.getValue(0, "sourceflag", "");
                        hm.put("sourceflag", sourceflag.length() > 0 ? sourceflag : null);
                    }
                    int findRow = dataSet.findRow(hm);
                    this.fieldValueHM.put("reagentinventory_sourceofrt_" + colIndx, findRow < 0 ? "skip" : reagentsource);
                    if (findRow < 0) {
                        htmlData.append("<td style=\"" + this.css_EmptyContentCellStyle + "\"></td>");
                        continue;
                    }
                    String primaryColumnValue = dataSet.getString(findRow, primaryColumnId, "");
                    String amountScope = "qbr";
                    if (this.isQCBatchSampleType) {
                        amountScope = dataSet.getString(findRow, "amountscopeflag", "B");
                    }
                    this.fieldValueHM.put("reagentinventory_amountscope_" + colIndx, amountScope);
                    String instrumentid = dataSet.getString(findRow, "instrumentid", "");
                    String trackitemid = dataSet.getValue(findRow, "trackitemid");
                    String reagentlotid = dataSet.getValue(findRow, "reagentlotid");
                    String amount_orig = amount = dataSet.getValue(findRow, "amounttext", "");
                    String amountunits = dataSet.getValue(findRow, "amountunits", "");
                    String amountunitsType = dataSet.getValue(findRow, "amountunitstype", "");
                    String amountrecommended = dataSet.getValue(findRow, "amountrecommended", "");
                    String amountrecommendedunits = dataSet.getValue(findRow, "amountrecommendedunits", "");
                    String amountrecommendedunitstype = dataSet.getValue(findRow, "amountrecommendedunitstype", "");
                    String qtycurrent = dataSet.getValue(findRow, "qtycurrent", "");
                    String qtyunits = dataSet.getValue(findRow, "qtyunits", "");
                    String qtycurrenttype = dataSet.getValue(findRow, "qtycurrenttype", "");
                    avlamount = this.getDisplayValueUnit(qtycurrent, qtyunits, qtycurrenttype);
                    String sourcekeyid1 = dataSet.getValue(findRow, "reagenttypeid", "");
                    String sourcekeyid2 = dataSet.getValue(findRow, "reagenttypeversionid", "");
                    String amountadjusted = dataSet.getValue(findRow, "amountadjusted", "");
                    String amountadjustedOrig = dataSet.getValue(findRow, "amountadjusted", "");
                    String typetargetconcentration = dataSet.getValue(findRow, "typetargetconcentration", "");
                    dsReagentLot = this.getReagentLot(dataSet, findRow);
                    String mode = !this.isUnmanaged && reagentlotid.length() > 0 || this.isUnmanaged && amount.trim().length() > 0 ? "Edit" : "Add";
                    DecimalFormat format = new DecimalFormat("0.######");
                    if (amountrecommended.length() > 0 || amountadjusted.length() > 0 || amount_orig.length() > 0) {
                        int m = this.getMultiplicationForInventory(primaryColumnValue, amountScope);
                        if (amountrecommended.length() > 0) {
                            amountrecommended = format.format(this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(amountrecommended, "" + this.decimalSeperator)).doubleValue() * (double)m);
                            amountrecommended = UnitsUtil.convertToLocateSeperated(amountrecommended, "" + this.decimalSeperator);
                        }
                        if (amountadjusted.length() > 0) {
                            amountadjusted = format.format(this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(amountadjusted, "" + this.decimalSeperator)).doubleValue() * (double)m);
                            amountadjusted = UnitsUtil.convertToLocateSeperated(amountadjusted, "" + this.decimalSeperator);
                        }
                        if (amount_orig.length() > 0) {
                            amount_orig = format.format(this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(amount_orig, "" + this.decimalSeperator)).doubleValue() * (double)m);
                            amount_orig = UnitsUtil.convertToLocateSeperated(amount_orig, "" + this.decimalSeperator);
                        }
                    }
                    String hideUnmanagedRT = "display:" + (this.isUnmanaged ? "none" : "");
                    String jsonStr = this.buildJSONStr(fieldName_qcbatchid, fieldName_qcsampletype, "", "", "", "", "", originalreagenttypeid, originalreagenttypeversionid);
                    if (this.autopopulateusedamount && amount.length() == 0) {
                        amount = amountadjustedOrig;
                        amountunits = amountrecommendedunits;
                        amountunitsType = amountrecommendedunitstype;
                        this.autoSeletedTrackitem = "Y";
                    }
                    if (reagentlotid.length() == 0 && this.autopopulatecontainer.equalsIgnoreCase("Y") && !viewonly && (trackitemid = (tiAvailAmountArr = StringUtil.split(tiAvailAmount = this.getTrackItemWithAvailQuantity(dsReagentLot, typetargetconcentration, amountrecommended, amountrecommendedunits, amountrecommendedunitstype), this.specialDelimer))[0]).length() > 0) {
                        reagentlotid = tiAvailAmountArr[1];
                        avlamount = tiAvailAmountArr[2];
                        amountadjusted = tiAvailAmountArr[3];
                        sourcekeyid1 = tiAvailAmountArr[4];
                        sourcekeyid2 = tiAvailAmountArr[5];
                        this.autoSeletedTrackitem = "Y";
                    }
                    this.buildRLTIJson(strRLTIJson, uniqueReagentTypes, originalreagenttypeid, originalreagenttypeversionid, dsReagentLot, dsReagentLotManageInvtFlag);
                    boolean isVirtuallot = this.isVirtualReagentLot(reagentlotid);
                    HashMap<String, String> hmReagentLot = new HashMap<String, String>();
                    hmReagentLot.put("reagentlotid", reagentlotid);
                    DataSet dsRLMCFlag = dsReagentLotManageInvtFlag.getFilteredDataSet(hmReagentLot);
                    String managecontainerflagRL = dsRLMCFlag.getString(0, "managecontainerinventoryflag", "Y");
                    String managecontainerflagRT = this.getManageInvForType(dsReagentTypeManageInvtFlag, originalreagenttypeid, originalreagenttypeversionid);
                    htmlData.append("<td style=\"" + this.css_ContentCellStyle + "\">");
                    htmlData.append("<table>");
                    this.renderConsumableLotRow(htmlData, originalreagenttypeid, originalreagenttypeversionid, "", "", "", reagentlotid, trackitemid, avlamount, dsReagentLot, colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, true, amountrecommended, amountrecommendedunits, amountrecommendedunitstype, false);
                    htmlData.append(this.getAdjustedHtml(colIndx, sourcekeyid1, sourcekeyid2, originalreagenttypeid, originalreagenttypeversionid, amountadjusted, amountadjustedOrig, amountrecommendedunits));
                    this.renderContainerRow(htmlData, originalreagenttypeid, originalreagenttypeversionid, "", "", "", jsonStr, reagentlotid, trackitemid, amount, amountunits, amountunitsType, dsReagentLot, "Edit", colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, true, true, amountrecommended, amountrecommendedunits, amountrecommendedunitstype, instrumenttypeid, instrumentfieldid, false);
                    if (this.needToShowExtraColumns) {
                        htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_reagentinventory_extracolumns_" + colIndx + "\">");
                        htmlData.append(isVirtuallot ? "&nbsp" : this.renderExtraColumns(this.extraColumns, this.qp, trackitemid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                        htmlData.append("</td></tr>");
                    }
                    htmlData.append("</table></td>");
                    this.fieldValueHM.put("reagentinventory_virtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                    this.fieldValueHM.put("reagentinventory_mode_" + colIndx, mode);
                    this.fieldValueHM.put("reagentinventory_primarykey_" + colIndx, primaryColumnValue);
                    this.fieldValueHM.put("reagentinventory_amountscopeflag_" + colIndx, amountScope);
                    this.fieldValueHM.put("reagentinventory_reagenttypeid_" + colIndx, originalreagenttypeid);
                    this.fieldValueHM.put("reagentinventory_unmanaged_" + colIndx, this.isUnmanaged ? "Y" : "N");
                    this.fieldValueHM.put("reagentinventory_currlasttrackitemid_" + colIndx, trackitemid);
                    this.fieldValueHM.put("reagentinventory_amountunitstype_" + colIndx, amountunitsType);
                    if (mode.equalsIgnoreCase("edit")) {
                        this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, amount_orig);
                        this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, trackitemid);
                        this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, reagentlotid);
                        this.fieldValueHM.put("reagentinventory_prevvirtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                    } else {
                        this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, "0");
                        this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, "");
                        this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, "");
                        this.fieldValueHM.put("reagentinventory_prevreagentlotdefault_" + colIndx, reagentlotid);
                        this.fieldValueHM.put("reagentinventory_prevamountdefault_" + colIndx, amount);
                        this.fieldValueHM.put("reagentinventory_prevtrackitemiddefault_" + colIndx, trackitemid);
                    }
                    this.fieldValueHM.put("reagentinventory_prevamountunits_" + colIndx, amountunits);
                    this.fieldValueHM.put("reagentinventory_prevamountunitstype_" + colIndx, amountunitsType);
                    this.fieldValueHM.put("reagentinventory_prevqtycurrent_" + colIndx, avlamount);
                    this.fieldValueHM.put("reagentinventory_instrumentid_" + colIndx, instrumentid);
                    this.fieldValueHM.put("reagentinventory_instrumenttypeid_" + colIndx, instrumenttypeid);
                    this.fieldValueHM.put("reagentinventory_instrumentmodelid_" + colIndx, instrumentmodelid);
                    this.fieldValueHM.put("reagentinventory_instrumentfieldid_" + colIndx, instrumentfieldid);
                }
                htmlData.append("</tr>");
            }
            this.fieldValueHM.put("reagentinventory_sampleCount", qcbatches.size() + "");
            this.fieldValueHM.put("reagentinventory_reagentCount", reagentTypes.size() + "");
            this.fieldValueHM.put("reagentinventory_sourceofrt", reagentsource);
            this.fieldValueHM.put("autoselectedtrackitem", this.autoSeletedTrackitem);
            htmlData.append(this.getExtraColumnHiddenField(this.extraColumns, this.table1, this.table2));
        } else {
            htmlData.append("<tr><td nowrap>" + this.noReagents + "</td></tr>");
        }
        htmlData.append(this.addHiddenFields(this.fieldValueHM));
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

    private void addToDataSet(JSONObject json, DataSet ds, DataSet ti) throws Exception {
        String sourceofrt = json.getString("sourceofrt");
        int row = ds.addRow();
        ds.setValue(row, "sdcid", json.getString("sdcid"));
        ds.setValue(row, "keyid1", json.getString("keyid1"));
        ds.setValue(row, "keyid2", json.getString("keyid2"));
        ds.setValue(row, "keyid3", json.getString("keyid3"));
        if (sourceofrt.equalsIgnoreCase("workitem")) {
            ds.setValue(row, "workitemid", json.getString("workitemid"));
            ds.setValue(row, "workiteminstance", json.getString("workiteminstance"));
        } else {
            ds.setValue(row, "paramlistid", json.getString("paramlistid"));
            ds.setValue(row, "paramlistversionid", json.getString("paramlistversionid"));
            ds.setValue(row, "variantid", json.getString("variantid"));
            ds.setValue(row, "dataset", json.getString("dataset"));
        }
        ds.setValue(row, "tosdcid", json.getString("tosdcid"));
        ds.setValue(row, "tokeyid1", this.replaceBlankWithNullBlank(json.getString("tokeyid1")));
        ds.setValue(row, "tokeyid2", json.getString("tokeyid2"));
        ds.setValue(row, "tokeyid3", json.getString("tokeyid3"));
        ds.setValue(row, "refsdcid", json.getString("refsdcid"));
        ds.setValue(row, "refkeyid1", this.replaceBlankWithNullBlank(json.getString("refkeyid1")));
        ds.setValue(row, "refkeyid2", json.getString("refkeyid2"));
        ds.setValue(row, "refkeyid3", json.getString("refkeyid3"));
        ds.setValue(row, "relationtype", json.getString("relationtype"));
        ds.setValue(row, "amount", this.replaceBlankWithNullBlank(json.getString("amount")));
        ds.setValue(row, "amountunits", this.replaceBlankWithNullBlank(json.getString("amountunits")));
        ds.setValue(row, "amountunitstype", this.replaceBlankWithNullBlank(json.getString("amountunitstype")));
        ds.setValue(row, "relationid", json.getString("relationid"));
        ds.setValue(row, "sourcekeyid1", json.getString("sourcekeyid1"));
        ds.setValue(row, "sourcekeyid2", json.getString("sourcekeyid2"));
        ds.setValue(row, "amountadjusted", json.getString("amountadjusted"));
        ds.setValue(row, "instrumentid", json.getString("instrumentid"));
        String amountunitsType = json.getString("amountunitstype");
        String unmanaged = json.getString("unmanaged");
        String prevamountunitstype = json.getString("prevamountunitstype");
        if (unmanaged.equalsIgnoreCase("N") && (prevamountunitstype != null && prevamountunitstype.length() > 0 || amountunitsType != null && amountunitsType.length() > 0)) {
            String trackamount;
            String trackitemid = json.getString("refkeyid1");
            String amount = json.getString("amount");
            amount = amount.trim().length() > 0 ? amount : "0";
            String amountunit = json.getString("amountunits");
            String prevtrackitemid = json.getString("prevtrackitemid");
            String prevamount = json.getString("prevamount");
            String prevamountunit = json.getString("prevamountunits");
            if (trackitemid.equals(prevtrackitemid) && trackitemid.trim().length() > 0 && prevamount.trim().length() > 0) {
                prevamount = this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(prevamount, "" + this.decimalSeperator)).toString();
                if (!amountunit.equals(prevamountunit)) {
                    try {
                        if (amountunitsType.equalsIgnoreCase("C") || prevamountunitstype.equalsIgnoreCase("C")) {
                            StringBuffer sql = new StringBuffer();
                            SafeSQL safeSQL = new SafeSQL();
                            sql.append("SELECT trackitemid, trackitemstatus, qtyunits, qtycurrent,qtycurrenttype");
                            sql.append(",trackitem.containertypeid, sizevalue, sizeunits ");
                            sql.append(" FROM trackitem ");
                            sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
                            sql.append(" WHERE trackitem.trackitemid = " + safeSQL.addVar(trackitemid));
                            DataSet trackitemDS = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            String containerSize = trackitemDS.getValue(0, "sizevalue", "");
                            String containerUnits = trackitemDS.getString(0, "sizeunits", "");
                            if (prevamountunitstype.equalsIgnoreCase("C")) {
                                double newValue = UnitsUtil.convertFromContainersToUnits(this.qp, containerSize, containerUnits, prevamount, amountunit);
                                prevamount = Double.toString(newValue);
                            } else if (amountunitsType.equalsIgnoreCase("C")) {
                                double newValue = UnitsUtil.convertFromContainersToUnits(this.qp, containerSize, containerUnits, amount, prevamountunit);
                                amount = Double.toString(newValue);
                                amountunit = prevamountunit;
                                amountunitsType = prevamountunitstype;
                            }
                        } else {
                            prevamount = UnitsUtil.getConvertedValue(this.qp, prevamountunit, amountunit, prevamount);
                        }
                        prevamount = prevamount.replace(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator(), '.');
                    }
                    catch (Exception e) {
                        throw new SapphireException(e.getMessage());
                    }
                }
                if (prevamount != null && prevamount.length() > 0) {
                    trackamount = Double.toString(Double.parseDouble(prevamount) - this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(amount, "" + this.decimalSeperator)).doubleValue());
                    trackamount = trackamount.replace('.', this.formatUtil.getDecimalSeparator());
                } else {
                    trackamount = "-" + amount;
                }
            } else {
                trackamount = "-" + amount;
                if (prevtrackitemid.length() > 0 && prevamount.trim().length() > 0) {
                    row = ti.addRow();
                    ti.setValue(row, "trackitemid", prevtrackitemid);
                    ti.setValue(row, "quantity", prevamount);
                    ti.setValue(row, "quantityunit", prevamountunit);
                    ti.setValue(row, "quantityunittype", prevamountunitstype);
                }
            }
            if (trackitemid.trim().length() > 0 && !trackamount.equalsIgnoreCase("-0")) {
                row = ti.addRow();
                ti.setValue(row, "trackitemid", trackitemid);
                ti.setValue(row, "quantity", trackamount);
                ti.setValue(row, "quantityunit", amountunit);
                ti.setValue(row, "quantityunittype", amountunitsType);
            }
        }
    }

    private PropertyList getTrackItemProps(DataSet ds) {
        PropertyList props = new PropertyList();
        props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
        props.setProperty("quantity", ds.getColumnValues("quantity", ";"));
        props.setProperty("quantityunit", ds.getColumnValues("quantityunit", ";"));
        props.setProperty("quantitytype", ds.getColumnValues("quantityunittype", ";"));
        this.setAuditProps(props);
        return props;
    }

    private PropertyList getActionProps(DataSet ds, String mode) {
        return this.getActionProps(ds, mode, "");
    }

    private PropertyList getActionProps(DataSet ds, String mode, String sourceofrt) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", ds.getValue(0, "sdcid"));
        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
        props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
        props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
        if ("workitem".equalsIgnoreCase(sourceofrt)) {
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
        props.setProperty("tokeyid2", ds.getColumnValues("tokeyid2", ";"));
        props.setProperty("tokeyid3", ds.getColumnValues("tokeyid3", ";"));
        props.setProperty("refsdcid", ds.getColumnValues("refsdcid", ";"));
        props.setProperty("refkeyid1", ds.getColumnValues("refkeyid1", ";"));
        props.setProperty("refkeyid2", ds.getColumnValues("refkeyid2", ";"));
        props.setProperty("refkeyid3", ds.getColumnValues("refkeyid3", ";"));
        if ("Edit".equalsIgnoreCase(mode)) {
            props.setProperty("relationid", ds.getColumnValues("relationid", ";"));
        }
        props.setProperty("relationfunction", this.getRelationFunctions(ds.size()));
        props.setProperty("relationtype", ds.getColumnValues("relationtype", ";"));
        props.setProperty("amount", ds.getColumnValues("amount", ";"));
        props.setProperty("amountunits", ds.getColumnValues("amountunits", ";"));
        props.setProperty("amountunitstype", ds.getColumnValues("amountunitstype", ";"));
        props.setProperty("sourcekeyid1", ds.getColumnValues("sourcekeyid1", ";"));
        props.setProperty("sourcekeyid2", ds.getColumnValues("sourcekeyid2", ";"));
        props.setProperty("amountadjusted", ds.getColumnValues("amountadjusted", ";"));
        props.setProperty("instrumentid", ds.getColumnValues("instrumentid", ";"));
        this.setAuditProps(props);
        return props;
    }

    private String getRelationFunctions(int n) {
        String relationFunction = "Reagent";
        for (int i = 2; i <= n; ++i) {
            relationFunction = relationFunction + ";Reagent";
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
        ds.addColumn("tokeyid2", 0);
        ds.addColumn("tokeyid3", 0);
        ds.addColumn("refsdcid", 0);
        ds.addColumn("refkeyid1", 0);
        ds.addColumn("refkeyid2", 0);
        ds.addColumn("refkeyid3", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("trackamount", 0);
        ds.addColumn("amountunits", 0);
        ds.addColumn("amountunitstype", 0);
        ds.addColumn("relationid", 0);
        ds.addColumn("sourcekeyid1", 0);
        ds.addColumn("sourcekeyid2", 0);
        ds.addColumn("amountadjusted", 0);
        ds.addColumn("instrumentid", 0);
    }

    private void populateWIDSColumns(DataSet ds) {
        ds.addColumn("relationtype", 0);
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("keyid2", 0);
        ds.addColumn("keyid3", 0);
        ds.addColumn("workitemid", 0);
        ds.addColumn("workiteminstance", 0);
        ds.addColumn("tosdcid", 0);
        ds.addColumn("tokeyid1", 0);
        ds.addColumn("tokeyid2", 0);
        ds.addColumn("tokeyid3", 0);
        ds.addColumn("refsdcid", 0);
        ds.addColumn("refkeyid1", 0);
        ds.addColumn("refkeyid2", 0);
        ds.addColumn("refkeyid3", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("trackamount", 0);
        ds.addColumn("amountunits", 0);
        ds.addColumn("amountunitstype", 0);
        ds.addColumn("relationid", 0);
        ds.addColumn("sourcekeyid1", 0);
        ds.addColumn("sourcekeyid2", 0);
        ds.addColumn("amountadjusted", 0);
        ds.addColumn("instrumentid", 0);
    }

    private PropertyList getActionPropsForTable(DataSet ds, String sourcesdc) {
        PropertyList props = new PropertyList();
        if (this.isQCBatch) {
            props.setProperty("sdcid", ds.getValue(0, "sdcid"));
            props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            props.setProperty("reagentlotid", ds.getColumnValues("reagentlotid", ";"));
            props.setProperty("reagenttypeid", ds.getColumnValues("reagenttypeid", ";"));
            props.setProperty("reagenttypeversionid", ds.getColumnValues("reagenttypeversionid", ";"));
        } else if (this.isReagentLotRecipe) {
            props.setProperty("sdcid", ds.getValue(0, "sdcid"));
            props.setProperty("reagentlotid", ds.getColumnValues("keyid1", ";"));
            props.setProperty("reagentlotrecipeitemid", ds.getColumnValues("keyid2", ";"));
            props.setProperty("linkid", this.reagentlotrecipe);
            props.setProperty("trackitemreagentlotid", ds.getColumnValues("reagentlotid", ";"));
            props.setProperty("includereagenttypeid", ds.getColumnValues("includereagenttypeid", ";"));
            props.setProperty("includereagenttypeversionid", ds.getColumnValues("includereagenttypeversionid", ";"));
            props.setProperty("instrumenttypeid", ds.getColumnValues("instrumenttypeid", ";"));
        } else if (this.isTransferExecutionReagents) {
            props.setProperty("sdcid", ds.getValue(0, "sdcid"));
            props.setProperty("linkid", ds.getValue(0, "linkid"));
            props.setProperty("transferexecutionid", ds.getColumnValues("keyid1", ";"));
            props.setProperty("transferexecutionreagentid", ds.getColumnValues("keyid2", ";"));
            props.setProperty("reagentlotid", ds.getColumnValues("reagentlotid", ";"));
            props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("reagenttypeid", ds.getColumnValues("includereagenttypeid", ";"));
            props.setProperty("reagenttypeversionid", ds.getColumnValues("includereagenttypeversionid", ";"));
            props.setProperty("useamount", ds.getColumnValues("amount", ";"));
            props.setProperty("useamountadjusted", ds.getColumnValues("amountadjusted", ";"));
            props.setProperty("useamountunits", ds.getColumnValues("amountunits", ";"));
            props.setProperty("useamountunitstype", ds.getColumnValues("amountunitstype", ";"));
        }
        if (!this.isTransferExecutionReagents) {
            props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("amount", ds.getColumnValues("amount", ";"));
            props.setProperty("amountadjusted", ds.getColumnValues("amountadjusted", ";"));
            props.setProperty("amounttext", ds.getColumnValues("amount", ";"));
            props.setProperty("amountunits", ds.getColumnValues("amountunits", ";"));
            props.setProperty("amountunitstype", ds.getColumnValues("amountunitstype", ";"));
        }
        props.setProperty("prevamount", ds.getColumnValues("prevamount", ";"));
        props.setProperty("instrumentid", ds.getColumnValues("instrumentid", ";"));
        this.setAuditProps(props);
        return props;
    }

    public void saveData(String data, String sourcesdc, String auditreason, String auditactivity, String auditsignedflag) {
        this.auditreason = auditreason;
        this.auditactivity = auditactivity;
        this.auditsignedflag = auditsignedflag;
        if (this.isQCBatch) {
            this.saveDataForQCBatch(data, sourcesdc);
        } else if (this.isReagentLotRecipe || this.isTransferExecutionReagents) {
            this.saveDataForDetailTable(data, sourcesdc);
        } else {
            this.saveDataForSDIRelation(data);
        }
        this.addActivityLog();
    }

    private void populateColumns(DataSet ds, String sourcesdc) {
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("reagenttypeid", 0);
        ds.addColumn("reagenttypeversionid", 0);
        ds.addColumn("reagentlotid", 0);
        ds.addColumn("trackitemid", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("amountadjusted", 0);
        ds.addColumn("amounttext", 0);
        ds.addColumn("amountunits", 0);
        ds.addColumn("amountunitstype", 0);
        ds.addColumn("instrumentid", 0);
        ds.addColumn("prevamount", 0);
    }

    private void populateColumnsForDetailTable(DataSet ds) {
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("keyid2", 0);
        ds.addColumn("keyid3", 0);
        ds.addColumn("linkid", 0);
        ds.addColumn("reagentlotid", 0);
        ds.addColumn("trackitemid", 0);
        ds.addColumn("amount", 0);
        ds.addColumn("amounttext", 0);
        ds.addColumn("amountunits", 0);
        ds.addColumn("amountunitstype", 0);
        ds.addColumn("includereagenttypeid", 0);
        ds.addColumn("includereagenttypeversionid", 0);
        ds.addColumn("amountadjusted", 0);
        ds.addColumn("instrumentid", 0);
        ds.addColumn("instrumenttypeid", 0);
        ds.addColumn("prevamount", 0);
    }

    private void addToEditDetailDS(String data, DataSet ds) throws Exception {
        JSONArray jsonArray = new JSONArray(data);
        for (int row = 0; row < jsonArray.length(); ++row) {
            JSONObject json = jsonArray.getJSONObject(row);
            int indx = ds.addRow();
            ds.setValue(indx, "sdcid", json.getString("sdcid"));
            ds.setValue(indx, "keyid1", json.getString("keyid1"));
            ds.setValue(indx, "keyid2", json.getString("keyid2"));
            ds.setValue(indx, "keyid3", json.getString("keyid3"));
            ds.setValue(indx, "linkid", json.getString("linkid"));
            ds.setValue(indx, "reagentlotid", this.replaceBlankWithNullBlank(json.getString("reagentlotid")));
            ds.setValue(indx, "trackitemid", this.replaceBlankWithNullBlank(json.getString("trackitemid")));
            ds.setValue(indx, "amount", this.replaceBlankWithNullBlank(json.getString("amount")));
            ds.setValue(indx, "amounttext", this.replaceBlankWithNullBlank(json.getString("amount")));
            ds.setValue(indx, "amountunits", this.replaceBlankWithNullBlank(json.getString("amountunits")));
            ds.setValue(indx, "amountunitstype", this.replaceBlankWithNullBlank(json.getString("amountunitstype")));
            ds.setValue(indx, "instrumentid", json.getString("instrumentid"));
            ds.setValue(indx, "includereagenttypeid", json.getString("sourcekeyid1"));
            ds.setValue(indx, "includereagenttypeversionid", json.getString("sourcekeyid2"));
            ds.setValue(indx, "amountadjusted", json.getString("amountadjusted"));
            ds.setValue(indx, "prevamount", json.getString("prevamount"));
            if (!this.isReagentLotRecipe) continue;
            ds.setValue(indx, "instrumenttypeid", json.getString("instrumenttypeid"));
        }
    }

    private void addToEditDS(String data, DataSet ds) throws Exception {
        JSONArray jsonArray = new JSONArray(data);
        for (int row = 0; row < jsonArray.length(); ++row) {
            JSONObject json = jsonArray.getJSONObject(row);
            int indx = ds.addRow();
            ds.setValue(indx, "sdcid", json.getString("sdcid"));
            ds.setValue(indx, "keyid1", json.getString("keyid1"));
            ds.setValue(indx, "reagentlotid", json.getString("reagentlotid"));
            ds.setValue(indx, "trackitemid", json.getString("trackitemid"));
            ds.setValue(indx, "reagenttypeid", json.getString("sourcekeyid1"));
            ds.setValue(indx, "reagenttypeversionid", json.getString("sourcekeyid2"));
            ds.setValue(indx, "amount", json.getString("amount"));
            ds.setValue(indx, "amountadjusted", json.getString("amountadjusted"));
            ds.setValue(indx, "amountunits", json.getString("amountunits"));
            ds.setValue(indx, "amountunitstype", json.getString("amountunitstype"));
            ds.setValue(indx, "instrumentid", json.getString("instrumentid"));
            ds.setValue(indx, "prevamount", json.getString("prevamount"));
        }
    }

    private void saveDataForQCBatch(String data, String sourcesdc) {
        DataSet editDS = new DataSet();
        this.populateColumns(editDS, sourcesdc);
        try {
            this.addToEditDS(data, editDS);
            if (editDS.size() > 0) {
                this.ap.processAction("EditSDI", "1", this.getActionPropsForTable(editDS, sourcesdc));
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void saveDataForDetailTable(String data, String sourcesdc) {
        DataSet editDS = new DataSet();
        this.populateColumnsForDetailTable(editDS);
        try {
            this.addToEditDetailDS(data, editDS);
            if (editDS.size() > 0) {
                this.ap.processAction("EditSDIDetail", "1", this.getActionPropsForTable(editDS, sourcesdc));
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void saveDataForSDIRelation(String data) {
        DataSet addDataSet = new DataSet();
        DataSet editDataSet = new DataSet();
        this.populateDSColumns(addDataSet);
        this.populateDSColumns(editDataSet);
        DataSet addWIDataSet = new DataSet();
        DataSet editWIDataSet = new DataSet();
        this.populateWIDSColumns(addWIDataSet);
        this.populateWIDSColumns(editWIDataSet);
        DataSet tiDataSet = new DataSet();
        tiDataSet.addColumn("trackitemid", 0);
        tiDataSet.addColumn("quantity", 0);
        tiDataSet.addColumn("quantityunit", 0);
        tiDataSet.addColumn("quantityunittype", 0);
        try {
            JSONArray jsonArray = new JSONArray(data);
            StringBuffer trackitemids = new StringBuffer();
            for (int row = 0; row < jsonArray.length(); ++row) {
                JSONObject jsonObject = jsonArray.getJSONObject(row);
                String data_mode = jsonObject.getString("mode").toLowerCase();
                String sourceofrt = jsonObject.getString("sourceofrt");
                String prevTrackitemid = jsonObject.getString("prevtrackitemid");
                String trackitemid = jsonObject.getString("refkeyid1");
                if (!trackitemid.equalsIgnoreCase(prevTrackitemid)) {
                    trackitemids.append(";").append(trackitemid);
                }
                if ("workitem".equalsIgnoreCase(sourceofrt)) {
                    if ("edit".equals(data_mode)) {
                        this.addToDataSet(jsonObject, editWIDataSet, tiDataSet);
                        continue;
                    }
                    if (!"add".equals(jsonObject.getString("mode").toLowerCase())) continue;
                    this.addToDataSet(jsonObject, addWIDataSet, tiDataSet);
                    continue;
                }
                if ("edit".equals(data_mode)) {
                    this.addToDataSet(jsonObject, editDataSet, tiDataSet);
                    continue;
                }
                if (!"add".equals(jsonObject.getString("mode").toLowerCase())) continue;
                this.addToDataSet(jsonObject, addDataSet, tiDataSet);
            }
            if (addDataSet.size() > 0) {
                this.ap.processAction("AddSDIDataRelation", "1", this.getActionProps(addDataSet, "Add"));
            }
            if (editDataSet.size() > 0) {
                this.ap.processAction("EditSDIDataRelation", "1", this.getActionProps(editDataSet, "Edit"));
            }
            if (addWIDataSet.size() > 0) {
                this.ap.processAction("AddSDIDataRelation", "1", this.getActionProps(addWIDataSet, "Add", "workitem"));
            }
            if (editWIDataSet.size() > 0) {
                this.ap.processAction("EditSDIWorkItemRelation", "1", this.getActionProps(editWIDataSet, "Edit", "workitem"));
            }
            if (tiDataSet.size() > 0) {
                this.ap.processAction("AdjustTrackItemInv", "1", this.getTrackItemProps(tiDataSet));
                this.checkVirtualTrackItem(tiDataSet);
            }
            if (editDataSet.size() > 0 || editWIDataSet.size() > 0) {
                OpalUtil.updateStatus(new JSONArray(data), this.qp, this.ap);
            }
            if (trackitemids.length() > 0) {
                ReagentUtil.updateUsedDetailsOfUnManagedTrackItemInv(trackitemids.substring(1), this.qp, this.ap);
            }
        }
        catch (Exception e) {
            Trace.log(e.getMessage());
        }
    }

    private void checkVirtualTrackItem(DataSet tiDataSet) throws SapphireException {
        String trackitemids = tiDataSet.getColumnValues("trackitemid", ";");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select ti.trackitemid,rl.reagentlotid from trackitem ti,reagentlot rl");
        sql.append(" where ti.trackitemid in (" + safeSQL.addIn(trackitemids, ";") + ") ");
        sql.append(" and ti.linkkeyid1=rl.reagentlotid and rl.contentflag='Y'");
        DataSet virtualLotDS = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (virtualLotDS != null && virtualLotDS.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ReagentLot");
            props.setProperty("keyid1", virtualLotDS.getColumnValues("reagentlotid", ";"));
            props.setProperty("reagentstatus", "Depleted");
            this.ap.processAction("EditSDI", "1", props);
        }
    }

    private void populateExtraColumns(PropertyListCollection extraConfigColumn, String mandatoryColumns, StringBuffer dsSelectClause, String[] hiddenColumnArr, PropertyListCollection visibleColumns) {
        int hiddenIndx = 0;
        for (int i = 0; i < extraConfigColumn.size(); ++i) {
            PropertyList columnProps = extraConfigColumn.getPropertyList(i);
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
                dsSelectClause.append("," + columnid);
            }
            if (columnMode.equalsIgnoreCase("Hidden")) {
                hiddenColumnArr[hiddenIndx] = columnid;
                ++hiddenIndx;
                continue;
            }
            this.increaseFixColumnsWidth(columnProps);
            visibleColumns.add(columnProps);
        }
    }

    private String getDisplayValueUnit(String amount, String amountunit, String amounttype) {
        String value = "";
        if (amount.length() > 0) {
            value = amounttype.equalsIgnoreCase("C") ? amount + " " + amounttype : amount + " " + amountunit;
        }
        return value;
    }

    private DataSet getReagentLot(DataSet filteredRelations, int indx) {
        String reagenttypeid = filteredRelations.getValue(indx, "originalreagenttypeid", "");
        String typetargetconcentration = filteredRelations.getValue(indx, "typetargetconcentration", "");
        String reagenttypeVersionid = filteredRelations.getValue(indx, "originalreagenttypeversionid", "");
        String paramListId = filteredRelations.getValue(indx, "paramlistid", "");
        String paramListVersionId = filteredRelations.getValue(indx, "paramlistversionid", "");
        String variantId = filteredRelations.getValue(indx, "variantid", "");
        String whereClause = this.whereClauseOriginal;
        whereClause = StringUtil.replaceAll(whereClause, "[reagenttypeid]", SafeSQL.encodeForSQL(reagenttypeid, this.isOracle));
        whereClause = StringUtil.replaceAll(whereClause, "[reagenttypeversionid]", SafeSQL.encodeForSQL(reagenttypeVersionid, this.isOracle));
        whereClause = StringUtil.replaceAll(whereClause, "[paramlistid]", SafeSQL.encodeForSQL(paramListId, this.isOracle));
        whereClause = StringUtil.replaceAll(whereClause, "[paramlistversionid]", SafeSQL.encodeForSQL(paramListVersionId, this.isOracle));
        whereClause = StringUtil.replaceAll(whereClause, "[variantid]", SafeSQL.encodeForSQL(variantId, this.isOracle));
        int openbracketPos = whereClause.indexOf("[");
        int closebracketPos = whereClause.indexOf("]");
        while (openbracketPos >= 0 && closebracketPos >= 0) {
            int indx1 = openbracketPos;
            int indx2 = closebracketPos;
            String str = whereClause.substring(indx1 + 1, indx2);
            if (filteredRelations.isValidColumn(str)) {
                String oldVal = "[" + str + "]";
                String newVal = filteredRelations.getValue(0, str);
                whereClause = StringUtil.replaceAll(whereClause, oldVal, SafeSQL.encodeForSQL(newVal, this.isOracle));
            }
            openbracketPos = whereClause.indexOf("[", indx2 + 1);
            closebracketPos = whereClause.indexOf("]", indx2 + 1);
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("TrackitemSDC");
        sdiRequest.setQueryFrom(this.fromClause);
        sdiRequest.setQueryWhere(whereClause);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryOrderBy(this.orderByClause);
        DataSet ds = this.sdip.getSDIData(sdiRequest).getDataset("primary");
        if (ds.getRowCount() > 0) {
            ds.addColumn("reagenttypeid", 0);
            ds.addColumn("reagenttypeversionid", 0);
            ds.addColumn("actualconcentration", 0);
            ds.addColumn("targetconcentration", 0);
            ds.addColumn("stopautoselectcontainerflag", 0);
            String reagentlotid = ReagentUtil.getUniqueValues(ds.getColumnValues("linkkeyid1", ";"), ";", ";");
            this.safeSQL.reset();
            StringBuffer sql = new StringBuffer();
            sql.append("select reagentlotid,reagenttypeid,reagenttypeversionid,actualconcentration,targetconcentration, ");
            sql.append(" (select stopautoselectcontainerflag from reagenttype where reagenttype.reagenttypeid=reagentlot.reagenttypeid");
            sql.append("  and reagenttype.reagenttypeversionid=reagentlot.reagenttypeversionid) stopautoselectcontainerflag");
            if (StringUtil.split(reagentlotid, ";").length > 750) {
                String rsetid = "";
                try {
                    rsetid = this.dp.createRSet("LV_ReagentLot", reagentlotid, "", "");
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
                sql.append(" from reagentlot,rsetitems ");
                sql.append(" where reagentlot.reagentlotid=rsetitems.keyid1");
                sql.append(" and rsetitems.rsetid=").append(this.safeSQL.addVar(rsetid));
            } else {
                sql.append(" from reagentlot ");
                sql.append(" where reagentlotid in (").append(this.safeSQL.addIn(reagentlotid, ";")).append(")");
            }
            DataSet concDS = this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
            HashMap<String, String> hm = new HashMap<String, String>();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String lot = ds.getString(i, "linkkeyid1", "");
                hm.clear();
                hm.put("reagentlotid", lot);
                int row = concDS.findRow(hm);
                if (row < 0) continue;
                ds.setString(i, "reagenttypeid", concDS.getValue(row, "reagenttypeid", ""));
                ds.setString(i, "reagenttypeversionid", concDS.getValue(row, "reagenttypeversionid", ""));
                ds.setString(i, "actualconcentration", concDS.getValue(row, "actualconcentration", ""));
                ds.setString(i, "targetconcentration", concDS.getValue(row, "targetconcentration", ""));
                ds.setString(i, "stopautoselectcontainerflag", concDS.getValue(row, "stopautoselectcontainerflag", "N"));
            }
        }
        return ds;
    }

    private DataSet getReagentManageInvtFlagDS(DataSet controldata, String type) {
        this.safeSQL.reset();
        String sql = "";
        String whereClause = " where reagenttypeid in (SELECT coalesce(NULLIF(rt2.reagenttypeid,'') ,rt1.reagenttypeid) FROM reagenttype rt1 left join reagenttype rt2 on rt1.activematerialid=rt2.activematerialid where rt1.reagenttypeid in (" + this.safeSQL.addIn(controldata.getColumnValues("originalreagenttypeid", "','")) + ")) ";
        if (type.equalsIgnoreCase("lot")) {
            sql = "select reagentlotid,managecontainerinventoryflag from reagentlot " + whereClause;
        } else if (type.equalsIgnoreCase("type")) {
            sql = "select reagenttypeid,reagenttypeversionid,versionstatus,managecontainerinventoryflag from reagenttype " + whereClause;
        }
        return this.qp.getPreparedSqlDataSet(sql, this.safeSQL.getValues());
    }

    private DataSet getReagentLotManageInvFlagByConsumables(String filteredConsumablesStr) {
        this.safeSQL.reset();
        String sqlReagentLotManageInvtFlag = "select reagentlotid,managecontainerinventoryflag from reagentlot where reagenttypeid in (SELECT coalesce(NULLIF(rt2.reagenttypeid,'') ,rt1.reagenttypeid) FROM reagenttype rt1 left join reagenttype rt2 on rt1.activematerialid=rt2.activematerialid where rt1.reagenttypeid in (" + this.safeSQL.addIn(filteredConsumablesStr, ";") + "))";
        return this.qp.getPreparedSqlDataSet(sqlReagentLotManageInvtFlag, this.safeSQL.getValues());
    }

    private int getMultiplicationForInventory(String keyid1, String amountscope) {
        DataSet ds;
        int count = 1;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        if (amountscope.equalsIgnoreCase("qbr")) {
            sql.append("select s_qcbatchitemid FROM s_qcbatchitem bi,s_qcbatchreagent br");
            sql.append(" WHERE s_qcbatchreagentid = ").append(safeSQL.addVar(keyid1));
            sql.append(" and bi.s_qcbatchid = br.qcbatchid");
        } else if (amountscope.equalsIgnoreCase("S")) {
            sql.append("select s_qcbatchitemid FROM s_qcbatchitem");
            sql.append(" WHERE qcbatchsampletypeid = ").append(safeSQL.addVar(keyid1));
        }
        if (sql.length() > 0 && (ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.getRowCount() > 0) {
            count = ds.getRowCount();
        }
        return count;
    }

    private boolean autoPopulateUsedAmount() {
        boolean autoPopulate = true;
        try {
            PropertyList policy = this.cp.getPolicy("ConsumablePolicy", "Sapphire Custom");
            if (policy != null && policy.size() > 0) {
                autoPopulate = policy.getProperty("autopopulateuseamount", "Y").equalsIgnoreCase("Y");
            }
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return autoPopulate;
    }

    private String getAdjustedHtml(String colIndx, String sourcekeyid1, String sourcekeyid2, String originalReagentType, String originalReagentTypeVersion, String amountadjusted, String amountadjustedOrig, String recoAmountunits) {
        StringBuffer htmlData = new StringBuffer();
        this.fieldValueHM.put("reagentinventory_sourcekeyid1_" + colIndx, sourcekeyid1);
        this.fieldValueHM.put("reagentinventory_sourcekeyid2_" + colIndx, sourcekeyid2);
        this.fieldValueHM.put("reagentinventory_originalreagenttypeid_" + colIndx, originalReagentType);
        this.fieldValueHM.put("reagentinventory_originalreagenttypeversionid_" + colIndx, originalReagentTypeVersion);
        this.fieldValueHM.put("reagentinventory_amountadjusted_" + colIndx, amountadjustedOrig);
        this.fieldValueHM.put("reagentinventory_usedconsumabletitle_" + colIndx, this.usedconsumabletitle);
        if (!this.isUnmanaged) {
            htmlData.append("<tr id=\"reagentinventory_adjustedamountrow_" + colIndx + "\" name=\"" + "reagentinventory_" + "" + colIndx + "\" style=\"display:" + (this.showAdjustedAmount ? "" : "none") + "\">");
            String uctt = originalReagentType.length() > 0 && !originalReagentType.equalsIgnoreCase(sourcekeyid1) ? this.usedconsumabletitle + ":" : "";
            String uct = originalReagentType.length() > 0 && !originalReagentType.equalsIgnoreCase(sourcekeyid1) ? sourcekeyid1 : "";
            String adjustedamountdisplayvalue = amountadjusted.length() > 0 ? amountadjusted + " " + recoAmountunits : "";
            htmlData.append("<td nowrap id=\"reagentinventory_uctt_" + colIndx + "\" name=\"" + "reagentinventory_" + "uctt_" + colIndx + "\">" + uctt + "</td>");
            htmlData.append("<td nowrap id=\"reagentinventory_uct_" + colIndx + "\" name=\"" + "reagentinventory_" + "uct_" + colIndx + "\">" + uct + "</td>");
            htmlData.append("<td nowrap id=\"reagentinventory_adjustedamounttitle_" + colIndx + "\" name=\"" + "reagentinventory_" + "adjustedamounttitle_" + colIndx + "\">" + this.adjustedamounttitle + ":</td>");
            htmlData.append("<td nowrap id=\"reagentinventory_adjustedamountdisplayvalue_" + colIndx + "\" name=\"" + "reagentinventory_" + "adjustedamountdisplayvalue_" + colIndx + "\">" + adjustedamountdisplayvalue + "</td>");
            htmlData.append("<td nowrap>&nbsp</td>");
            htmlData.append("<td nowrap>&nbsp</td>");
            htmlData.append("</tr>");
        }
        return htmlData.toString();
    }

    private String getInstrumentIconHTML(String dataEntryPrefix, String colIndx, String instrumenttypeid, String instrumentfieldid) {
        String html = "";
        if (instrumenttypeid.length() > 0 && instrumentfieldid.length() > 0) {
            html = "<a style=\"display:inline;\" id=\"" + dataEntryPrefix + "amounttext_img" + colIndx + "\" href=\"#\" title=\"/Lookup\" onclick=\"javascript:reagentUtil.openInstrConnectionDialog('" + dataEntryPrefix + "','" + colIndx + "');\"><img title=\"Lookup\" border=\"0\" src=\"WEB-CORE/images/gif/Instrument.gif\" class=\"lookup_img\"></a>";
        }
        return html;
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

    private String resloveReagentType(String tableid) {
        return "coalesce(NULLIF(" + tableid + ".originalreagenttypeid,'')," + tableid + ".reagenttypeid)";
    }

    protected String getContentCellHtml(ArrayList<String> consumbaleTypes, DataSet sdirelations, ArrayList<String> uniqueSDIWI, StringBuffer strRLTIJson, int selectedDSIndx, String filteredConsumablesStr) {
        StringBuffer htmlData = new StringBuffer();
        HashMap<String, String> hm = new HashMap<String, String>();
        int regTypeIndex = -1;
        int tabindex = 1;
        ArrayList<String> uniqueReagentTypes = new ArrayList<String>();
        DataSet dsReagentLotManageInvtFlag = this.getReagentLotManageInvFlagByConsumables(filteredConsumablesStr);
        for (String consumbaleType : consumbaleTypes) {
            String colIndx = selectedDSIndx + "s" + ++regTypeIndex;
            boolean foundInvRecord = false;
            boolean mergeInvRecord = false;
            String sourcetype = "";
            String sourceofrt = "";
            String avlamount = "";
            hm.clear();
            hm.put("originalreagenttypeid", consumbaleType);
            DataSet filteredRelations = sdirelations.getFilteredDataSet(hm);
            if (filteredRelations.getRowCount() > 0) {
                foundInvRecord = true;
                sourcetype = filteredRelations.getValue(0, "sourcetype");
                String string = sourceofrt = sourcetype.equalsIgnoreCase("W") ? "workitem" : "paramlist";
                if (sourcetype.equalsIgnoreCase("W")) {
                    String key1 = filteredRelations.getValue(0, "keyid1");
                    String swid = filteredRelations.getValue(0, "sourceworkitemid");
                    String swinstnace = filteredRelations.getValue(0, "sourceworkiteminstance");
                    String sdiwi = key1 + this.specialDelimer + swid + this.specialDelimer + swinstnace + this.specialDelimer + consumbaleType;
                    if (!uniqueSDIWI.contains(sdiwi)) {
                        uniqueSDIWI.add(sdiwi);
                    } else {
                        foundInvRecord = false;
                        mergeInvRecord = true;
                    }
                }
            }
            this.fieldValueHM.put("reagentinventory_sourceofrt_" + colIndx, foundInvRecord ? sourceofrt : "skip");
            if (foundInvRecord) {
                String mode;
                String amount;
                this.autoloadedContainerIndicator = "";
                boolean amountEditable = true;
                boolean containerEditable = true;
                boolean viewonly = this.viewonlyOption.equalsIgnoreCase("Yes");
                String keyid1 = filteredRelations.getValue(0, "keyid1", "");
                String samplestatus = filteredRelations.getValue(0, "samplestatus", "");
                String relationid = filteredRelations.getValue(0, "relationid", "");
                String requiredamount = filteredRelations.getValue(0, "requiredamount", "");
                String requiredamountunits = filteredRelations.getValue(0, "requiredamountunits", "");
                String requiredamountunitstype = filteredRelations.getValue(0, "requiredamountunitstype", "");
                String reagentlotid = filteredRelations.getString(0, "tokeyid1", "");
                String trackitemid = filteredRelations.getString(0, "refkeyid1", "");
                String amount_orig = amount = filteredRelations.getValue(0, "amount", "");
                String amountunits = filteredRelations.getValue(0, "amountunits", "");
                String amountunitsType = filteredRelations.getValue(0, "amountunitstype", "");
                String paramListId = filteredRelations.getValue(0, "paramlistid", "");
                String paramListVersionId = filteredRelations.getValue(0, "paramlistversionid", "");
                String variantId = filteredRelations.getValue(0, "variantid", "");
                BigDecimal dsnoBD = filteredRelations.getBigDecimal(0, "dataset");
                String datasetstatus = filteredRelations.getValue(0, "s_datasetstatus", "");
                this.isUnmanaged = filteredRelations.getString(0, "unmanagedflag", "N").equalsIgnoreCase("Y");
                String sourcekeyid1 = filteredRelations.getString(0, "sourcekeyid1", "");
                String sourcekeyid2 = filteredRelations.getString(0, "sourcekeyid2", "");
                String amountadjusted = filteredRelations.getValue(0, "amountadjusted", "");
                String originalReagentType = filteredRelations.getString(0, "originalreagenttypeid", "");
                String originalReagentTypeVersion = filteredRelations.getString(0, "originalreagenttypeversionid", "");
                String managecontainerflagRT = filteredRelations.getString(0, "managecontainerinventoryflag", "Y");
                String swid = filteredRelations.getValue(0, "sourceworkitemid");
                String swinstnace = filteredRelations.getValue(0, "sourceworkiteminstance");
                String instrumenttypeid = filteredRelations.getString(0, "instrumenttypeid", "");
                String instrumentmodelid = filteredRelations.getString(0, "instrumentmodelid", "");
                String instrumentfieldid = filteredRelations.getString(0, "instrumentfieldid", "");
                String instrumentid = filteredRelations.getString(0, "instrumentid", "");
                String typetargetconcentration = filteredRelations.getValue(0, "typetargetconcentration", "");
                boolean isMandatory = filteredRelations.getString(0, "mandatoryflag", "N").equalsIgnoreCase("Y");
                this.mandatoryFound = isMandatory || this.mandatoryFound;
                DataSet dsReagentLot = this.getReagentLot(filteredRelations, 0);
                String hideUnmanagedRT = "display:" + (this.isUnmanaged ? "none" : "");
                String string = mode = !this.isUnmanaged && reagentlotid.length() > 0 || this.isUnmanaged && amount.trim().length() > 0 ? "Edit" : "Add";
                if (datasetstatus.equalsIgnoreCase("Cancelled") || datasetstatus.equalsIgnoreCase("Completed") && this.viewonlyOption.equalsIgnoreCase("OnCompletion")) {
                    viewonly = true;
                }
                if ("Edit".equalsIgnoreCase(mode)) {
                    if (this.sdcid.equalsIgnoreCase("Sample") && this.viewonlyOption.equalsIgnoreCase("No") && !samplestatus.equalsIgnoreCase("initial") && !samplestatus.equalsIgnoreCase("received")) {
                        amountEditable = false;
                    }
                    if (this.viewonlyOption.equalsIgnoreCase("No")) {
                        containerEditable = false;
                    }
                    String avmAmountUnt = "";
                    avmAmountUnt = "C".equalsIgnoreCase(amountunitsType) ? filteredRelations.getValue(0, "qtyunits", "C") : filteredRelations.getValue(0, "qtyunits", "");
                    avlamount = filteredRelations.getValue(0, "qtycurrent", "") + " " + avmAmountUnt;
                } else {
                    String tiAvailAmount;
                    String[] tiAvailAmountArr;
                    if (this.autopopulatecontainer.equalsIgnoreCase("Y") && !viewonly && (trackitemid = (tiAvailAmountArr = StringUtil.split(tiAvailAmount = this.getTrackItemWithAvailQuantity(dsReagentLot, typetargetconcentration, requiredamount, requiredamountunits, requiredamountunitstype), this.specialDelimer))[0]).length() > 0) {
                        reagentlotid = tiAvailAmountArr[1];
                        avlamount = tiAvailAmountArr[2];
                        amountadjusted = tiAvailAmountArr[3];
                        sourcekeyid1 = tiAvailAmountArr[4];
                        sourcekeyid2 = tiAvailAmountArr[5];
                        String consumableKey = sourcekeyid1 + this.specialDelimer + sourcekeyid2;
                        if (this.currAutoloadedTI.containsKey(consumableKey)) {
                            String ti = this.currAutoloadedTI.get(consumableKey);
                            String indicator = this.currAutoloadedTIIndicator.get(consumableKey);
                            if (!trackitemid.equalsIgnoreCase(ti)) {
                                this.currAutoloadedTI.put(consumableKey, trackitemid);
                                this.autoloadedContainerIndicator = "A".equalsIgnoreCase(indicator) ? "B" : "A";
                                this.autoloadedContainerTooltips = "Quantity taken from the next available container due to insufficient volume available in previous container.";
                            } else {
                                this.autoloadedContainerIndicator = indicator;
                            }
                        } else {
                            this.autoloadedContainerTooltips = "Quantity taken from the first available container.";
                            this.currAutoloadedTI.put(consumableKey, trackitemid);
                            this.autoloadedContainerIndicator = "A";
                        }
                        this.currAutoloadedTIIndicator.put(consumableKey, this.autoloadedContainerIndicator);
                        this.autoSeletedTrackitem = "Y";
                    }
                    if (this.autopopulateusedamount && amount.trim().length() == 0) {
                        amount = amountadjusted;
                        amountunits = requiredamountunits;
                        amountunitsType = requiredamountunitstype;
                        this.autoSeletedTrackitem = "Y";
                    }
                }
                String jsonStr = this.buildJSONStr(keyid1, "", "", paramListId, paramListVersionId, variantId, dsnoBD.toString(), originalReagentType, originalReagentTypeVersion);
                HashMap<String, String> hmReagentLot = new HashMap<String, String>();
                hmReagentLot.put("reagentlotid", reagentlotid);
                DataSet dsRLMCFlag = dsReagentLotManageInvtFlag.getFilteredDataSet(hmReagentLot);
                String managecontainerflagRL = dsRLMCFlag.getString(0, "managecontainerinventoryflag", "Y");
                boolean isVirtuallot = this.isVirtualReagentLot(reagentlotid);
                this.buildRLTIJson(strRLTIJson, uniqueReagentTypes, originalReagentType, originalReagentTypeVersion, dsReagentLot, dsReagentLotManageInvtFlag);
                htmlData.append("<td style=\"" + this.css_ContentCellStyle + "\">");
                htmlData.append("<table>");
                this.renderConsumableLotRow(htmlData, originalReagentType, originalReagentTypeVersion, paramListId, paramListVersionId, variantId, reagentlotid, trackitemid, avlamount, dsReagentLot, colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, containerEditable, requiredamount, requiredamountunits, requiredamountunitstype, isMandatory);
                htmlData.append(this.getAdjustedHtml(colIndx, sourcekeyid1, sourcekeyid2, originalReagentType, originalReagentTypeVersion, amountadjusted, amountadjusted, requiredamountunits));
                this.renderContainerRow(htmlData, originalReagentType, originalReagentTypeVersion, paramListId, paramListVersionId, variantId, jsonStr, reagentlotid, trackitemid, amount, amountunits, amountunitsType, dsReagentLot, mode, colIndx, tabindex, isVirtuallot, managecontainerflagRL, managecontainerflagRT, hideUnmanagedRT, viewonly, containerEditable, amountEditable, requiredamount, requiredamountunits, requiredamountunitstype, instrumenttypeid, instrumentfieldid, isMandatory);
                if (this.needToShowExtraColumns) {
                    htmlData.append("<tr><td valign=top style=\"border=0\" colspan=\"6\" id=\"ec_reagentinventory_extracolumns_" + colIndx + "\">");
                    htmlData.append(isVirtuallot ? "&nbsp" : this.renderExtraColumns(this.extraColumns, this.qp, trackitemid, this.extraColumFromcluase, this.extraColumWherecluase, this.table1, this.table2));
                    htmlData.append("</td></tr>");
                }
                htmlData.append("</table>");
                htmlData.append("</td>");
                this.fieldValueHM.put("reagentinventory_containereditable_" + colIndx, !viewonly && containerEditable ? "Y" : "N");
                this.fieldValueHM.put("reagentinventory_amounteditable_" + colIndx, !viewonly && amountEditable ? "Y" : "N");
                this.fieldValueHM.put("reagentinventory_workitemid_" + colIndx, swid);
                this.fieldValueHM.put("reagentinventory_workiteminstance_" + colIndx, swinstnace);
                this.fieldValueHM.put("reagentinventory_virtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                this.fieldValueHM.put("reagentinventory_unmanaged_" + colIndx, this.isUnmanaged ? "Y" : "N");
                this.fieldValueHM.put("reagentinventory_unmanagedinventory_" + colIndx, managecontainerflagRT);
                this.fieldValueHM.put("reagentinventory_mode_" + colIndx, mode);
                this.fieldValueHM.put("reagentinventory_sdistatus_" + colIndx, datasetstatus);
                this.fieldValueHM.put("reagentinventory_reagenttypeid_" + colIndx, originalReagentType);
                this.fieldValueHM.put("reagentinventory_reagenttypeversionid_" + colIndx, originalReagentTypeVersion);
                this.fieldValueHM.put("reagentinventory_relationid_" + colIndx, relationid);
                this.fieldValueHM.put("reagentinventory_currlasttrackitemid_" + colIndx, trackitemid);
                this.fieldValueHM.put("reagentinventory_amountunitstype_" + colIndx, amountunitsType);
                if (mode.equalsIgnoreCase("edit")) {
                    this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, amount_orig);
                    this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, trackitemid);
                    this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, reagentlotid);
                    this.fieldValueHM.put("reagentinventory_prevvirtualflag_" + colIndx, isVirtuallot ? "Y" : "N");
                } else {
                    this.fieldValueHM.put("reagentinventory_prevamount_" + colIndx, "0");
                    this.fieldValueHM.put("reagentinventory_prevtrackitemid_" + colIndx, "");
                    this.fieldValueHM.put("reagentinventory_prevreagentlot_" + colIndx, "");
                    this.fieldValueHM.put("reagentinventory_prevreagentlotdefault_" + colIndx, reagentlotid);
                    this.fieldValueHM.put("reagentinventory_prevamountdefault_" + colIndx, amount);
                    this.fieldValueHM.put("reagentinventory_prevtrackitemiddefault_" + colIndx, trackitemid);
                }
                this.fieldValueHM.put("reagentinventory_prevamountunits_" + colIndx, amountunits);
                this.fieldValueHM.put("reagentinventory_prevamountunitstype_" + colIndx, amountunitsType);
                this.fieldValueHM.put("reagentinventory_prevqtycurrent_" + colIndx, avlamount);
                this.fieldValueHM.put("reagentinventory_instrumentid_" + colIndx, instrumentid);
                this.fieldValueHM.put("reagentinventory_instrumenttypeid_" + colIndx, instrumenttypeid);
                this.fieldValueHM.put("reagentinventory_instrumentmodelid_" + colIndx, instrumentmodelid);
                this.fieldValueHM.put("reagentinventory_instrumentfieldid_" + colIndx, instrumentfieldid);
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

    private void renderConsumableLotRow(StringBuffer htmlData, String reagenttypeid, String reagenttypeVersionid, String paramListId, String paramListVersionId, String variantId, String reagentlotid, String trackitemid, String avlamount, DataSet dsReagentLot, String colIndx, int tabindex, boolean isVirtuallot, String managecontainerflagRL, String managecontainerflagRT, String hideUnmanagedRT, boolean viewonly, boolean containerEditable, String requiredamount, String requiredamountunits, String requiredamountunitstype, boolean isMandatory) {
        String recommendedDisplayValue;
        htmlData.append("<tr>");
        htmlData.append("<td style=\"border=0;" + hideUnmanagedRT + "\" nowrap>" + this.reagentLotTitle + (isMandatory ? " (R)" : "") + ":&nbsp;</td><td nowrap style=\"" + hideUnmanagedRT + "\">");
        if (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_Dropdown) && !viewonly && containerEditable) {
            htmlData.append("<select tabindex=" + tabindex++ + " name=\"" + "reagentinventory_" + "reagentlotid_" + colIndx + "\" id=\"" + "reagentinventory_" + "reagentlotid_" + colIndx + "\" maxlength=\"" + this.maxLotSize + "\"  onchange=\"reagentUtil.populateTrackitem(this.id)\" onfocus=\"\" >");
            htmlData.append("<option value=\"\"></option>");
            ArrayList<DataSet> list = dsReagentLot.getGroupedDataSets("linkkeyid1");
            for (int i = 0; list != null && i < list.size(); ++i) {
                DataSet ds = (DataSet)list.get(i);
                String linkkeyid1 = ds.getString(0, "linkkeyid1", "");
                if (reagentlotid.equalsIgnoreCase(linkkeyid1)) {
                    htmlData.append("<option selected value=\"" + linkkeyid1 + "\">" + linkkeyid1 + "</option>");
                    continue;
                }
                htmlData.append("<option value=\"" + linkkeyid1 + "\">" + linkkeyid1 + "</option>");
            }
            htmlData.append("</select>");
        } else {
            htmlData.append("<input  class=\"input_field\"   tabindex=" + tabindex++ + " size=\"" + this.lotSize + "\" edit=\"lookup\" type=text readonly value=\"" + reagentlotid + "\" name=\"" + "reagentinventory_" + "reagentlotid_" + colIndx + "\" id=\"" + "reagentinventory_" + "reagentlotid_" + colIndx + "\"  maxlength=\"" + this.maxLotSize + "\"  onchange=\"reagentUtil.manageContainerInv(this.id)\" onfocus=\"\">");
            if ((this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_Lookup) || this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) && !viewonly && containerEditable) {
                htmlData.append("<a style=\"display:inline;\" id=\"reagentinventory_reagentlotid_" + colIndx + "\" href=\"/Lookup\" onClick=\"reagentUtil.openLookup(this.id,'" + this.lookupurl + "', '" + reagenttypeid + "','" + reagenttypeVersionid + "','" + paramListId + "','" + paramListVersionId + "','" + variantId + "','" + requiredamount + "','" + requiredamountunits + "','" + requiredamountunitstype + "');return false\" tabindex=\"0\"><img title=\"Lookup\" border=\"0\" src=\"" + this.lookupImage + "\" class=\"lookup_img\"></a>");
                String displayStyleValue = isVirtuallot ? "inline" : "none";
                htmlData.append("<a style=\"display:" + displayStyleValue + ";\" id=\"" + "reagentinventory_" + "reagentlotid_img_" + colIndx + "\" href=\"/Lookup\" onClick=\"reagentUtil.editReagentLot('" + this.reagentMaintPage + "','" + colIndx + "');return false\" tabindex=\"0\"><img title=\"Edit Virtual Lot\" border=\"0\" src=\"" + this.editImage + "\" class=\"lookup_img\"></a>");
                htmlData.append("<a style=\"display:none;\" id=\"reagentinventory_reagentlotids_img_" + colIndx + "\">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</a>");
            }
        }
        htmlData.append("</td>");
        String string = recommendedDisplayValue = requiredamount.length() > 0 ? requiredamount + " " + requiredamountunits : "";
        if (trackitemid.length() > 0 && "N".equalsIgnoreCase(managecontainerflagRL) || trackitemid.length() == 0 && "N".equalsIgnoreCase(managecontainerflagRT)) {
            this.showAdjustedAmount = false;
            htmlData.append("<td nowrap style=\"display:none\" id=\"reagentinventory_recomendedamounttitle_" + colIndx + "\">" + this.recomAmountTitle + ":&nbsp;</td><td><input style=\"border-style:none;display:none\" type=text readonly size=6 name=\"" + "reagentinventory_" + "recomendedamount_" + colIndx + "\" id=\"" + "reagentinventory_" + "recomendedamount_" + colIndx + "\" value='" + recommendedDisplayValue + "'></td>");
            htmlData.append("<td nowrap style=\"display:none\" id=\"reagentinventory_qtycurrenttitle_" + colIndx + "\">" + this.availAmountTitle + ":&nbsp;</td><td><input style=\"border-style:none;display:none\" type=text readonly size=6 name=\"" + "reagentinventory_" + "qtycurrent_" + colIndx + "\" id=\"" + "reagentinventory_" + "qtycurrent_" + colIndx + "\" value=\"" + avlamount + "\"></td>");
        } else {
            this.showAdjustedAmount = true;
            htmlData.append("<td nowrap style=\"border=0\" id=\"reagentinventory_recomendedamounttitle_" + colIndx + "\">" + this.recomAmountTitle + ":&nbsp;</td><td><input style=\"border-style:none;\" type=text readonly size=6 name=\"" + "reagentinventory_" + "recomendedamount_" + colIndx + "\" id=\"" + "reagentinventory_" + "recomendedamount_" + colIndx + "\" value='" + recommendedDisplayValue + "'></td>");
            htmlData.append("<td nowrap style=\"border=0;" + (isVirtuallot ? "display:none" : hideUnmanagedRT) + "\" id=\"" + "reagentinventory_" + "qtycurrenttitle_" + colIndx + "\">" + this.availAmountTitle + ":&nbsp;</td><td id=\"" + "reagentinventory_" + "qtycurrentvalue_" + colIndx + "\" style=\"" + (isVirtuallot ? "display:none" : hideUnmanagedRT) + "\"><input style=\"border-style:none;\" type=text readonly size=6 name=\"" + "reagentinventory_" + "qtycurrent_" + colIndx + "\" id=\"" + "reagentinventory_" + "qtycurrent_" + colIndx + "\" value=\"" + avlamount + "\"></td>");
        }
        htmlData.append("</tr>");
        this.fieldValueHM.put("reagentinventory_cellselected_" + colIndx, "N");
    }

    private void renderContainerRow(StringBuffer htmlData, String reagenttypeid, String reagenttypeVersionid, String paramListId, String paramListVersionId, String variantId, String jsonStr, String reagentlotid, String trackitemid, String amount, String amountunits, String amountunitsType, DataSet dsReagentLot, String mode, String colIndx, int tabindex, boolean isVirtuallot, String managecontainerflagRL, String managecontainerflagRT, String hideUnmanagedRT, boolean viewonly, boolean containerEditable, boolean amountEditable, String requiredamount, String requiredamountunits, String requiredamountunitstype, String instrumenttypeid, String instrumentfieldid, boolean isMandatory) {
        htmlData.append("<tr>");
        htmlData.append("<td id=\"reagentinventory_containertitletd_" + colIndx + "\" style=\"border=0;" + (isVirtuallot ? "display:none" : hideUnmanagedRT) + "\" nowrap>" + this.trackitemTitle + (isMandatory ? " (R)" : "") + ":&nbsp;&nbsp;&nbsp;&nbsp;" + this.getAutoLoadedIndicator() + "</td><td id=\"" + "reagentinventory_" + "containervaluetd_" + colIndx + "\" style=\"" + (isVirtuallot ? "display:none" : hideUnmanagedRT) + "\">");
        if (viewonly || !containerEditable) {
            htmlData.append("<input tabindex=" + tabindex++ + " size=\"" + this.lotSize + "\" type=text readonly value=\"" + trackitemid + "\" name=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" id=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\"  maxlength=\"" + this.maxLotSize + "\">");
        } else if (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_Lookup) || this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) {
            if (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanOrLookUp)) {
                htmlData.append("<input size=\"" + this.lotSize + "\" tabindex=" + tabindex++ + " style=\";border:1px solid gray;\" edit=\"lookup\" type=text  value=\"" + trackitemid + "\" name=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" id=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\"  maxlength=\"" + this.maxLotSize + "\" onKeyPress=\"reagentUtil.validateOnPressEnter(event,this.id," + jsonStr + " )\" onchange=\"reagentUtil.validateScannedTrackitem(this.id," + jsonStr + " )\" onclick=\"reagentUtil.selectTrackitem(this.id,true);\" ondblclick=\"reagentUtil.selectTrackitem(this.id,false)\" " + (viewonly ? "readonly" : "") + ">");
            } else {
                htmlData.append("<input class=\"input_field \"  size=\"" + this.lotSize + "\" tabindex=" + tabindex++ + " edit=\"lookup\" type=text readonly value=\"" + trackitemid + "\" name=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" id=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\"  maxlength=\"" + this.maxLotSize + "\" onchange=\"reagentUtil.validateScannedTrackitem(this.id," + jsonStr + ")\" onfocus=\"\">");
            }
            htmlData.append("<a id=\"reagentinventory_trackitemidimg_" + colIndx + "\" href=\"/Lookup\" onClick=\"reagentUtil.openLookup(this.id,'" + this.lookupurl + "', '" + reagenttypeid + "','" + reagenttypeVersionid + "','" + paramListId + "','" + paramListVersionId + "','" + variantId + "','" + requiredamount + "','" + requiredamountunits + "','" + requiredamountunitstype + "');return false\" tabindex=\"0\"><img title=\"Lookup\" border=\"0\" src=\"" + this.lookupImage + "\" class=\"lookup_img\"></a>");
        } else if (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_ScanMode)) {
            htmlData.append("<input size=\"" + this.lotSize + "\" tabindex=" + tabindex++ + " style=\"border:1px solid gray;\" edit=\"lookup\" type=text  value=\"" + trackitemid + "\" name=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" id=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\"  maxlength=\"" + this.maxLotSize + "\" onKeyPress=\"reagentUtil.validateOnPressEnter(event,this.id," + jsonStr + " )\" onchange=\"reagentUtil.validateScannedTrackitem(this.id," + jsonStr + " )\" onclick=\"reagentUtil.selectTrackitem(this.id,true);\" ondblclick=\"reagentUtil.selectTrackitem(this.id,false)\" " + (viewonly ? "readonly" : "") + ">");
        } else if (this.trackitemSelectionMode.equalsIgnoreCase(this.selectionMode_Dropdown)) {
            htmlData.append("<select tabindex=" + tabindex++ + " name=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" id=\"" + "reagentinventory_" + "trackitemid_" + colIndx + "\" maxlength=\"" + this.maxLotSize + "\"  onchange=\"reagentUtil.validateScannedTrackitem(this.id," + jsonStr + ",'Y')\" onfocus=\"\">");
            ArrayList<DataSet> list = dsReagentLot.getGroupedDataSets("linkkeyid1");
            if (list != null && list.size() > 0) {
                DataSet ds = (DataSet)list.get(0);
                if (reagentlotid.length() > 0) {
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("linkkeyid1", reagentlotid);
                    ds = dsReagentLot.getFilteredDataSet(hashMap);
                }
                htmlData.append("<option value=\"\"></option>");
                if (!trackitemid.equals("")) {
                    for (int i = 0; i < ds.size(); ++i) {
                        String trackitemID = ds.getString(i, "trackitemid", "");
                        if (trackitemID.equalsIgnoreCase(trackitemid)) {
                            htmlData.append("<option selected value=\"" + trackitemID + "\">" + trackitemID + "</option>");
                            continue;
                        }
                        htmlData.append("<option value=\"" + trackitemID + "\">" + trackitemID + "</option>");
                    }
                }
            }
            htmlData.append("</select>");
        }
        htmlData.append("</td>");
        if (trackitemid.length() > 0 && "N".equalsIgnoreCase(managecontainerflagRL) || trackitemid.length() == 0 && "N".equalsIgnoreCase(managecontainerflagRT)) {
            htmlData.append("<td nowrap style=\"display:none\" id=\"reagentinventory_amounttitle_" + colIndx + "\">" + this.amountUsedTitle + ":&nbsp;</td>");
            htmlData.append("<td nowrap style=\"display:none\" id=\"reagentinventory_amountusedtitle_" + colIndx + "\"><input class=\"input_field\" size=6 tabindex=" + tabindex++ + " class=\"dataentry_grid_cell\" type=text size=8 name=\"" + "reagentinventory_" + "amount_" + colIndx + "\" id=\"" + "reagentinventory_" + "amount_" + colIndx + "\" onchange=\"reagentInventory.validateAmount(this)\"  oninput=\"setChangesMade()\" value='0'>" + this.getInstrumentIconHTML("reagentinventory_", colIndx, instrumenttypeid, instrumentfieldid) + "</td>");
            htmlData.append("<td style=\"display:none\" colspan=2 id=\"reagentinventory_amountunitstitle_" + colIndx + "\" nowrap>" + this.unitTitle + ":&nbsp;&nbsp;&nbsp;");
            htmlData.append("<select tabindex=" + tabindex++ + " name=\"" + "reagentinventory_" + "amountunits_" + colIndx + "\" id=\"" + "reagentinventory_" + "amountunits_" + colIndx + "\" maxlength=\"" + this.maxLotSize + "\"  onchange=\"reagentUtil.setAmountUnitType(this,'amountunitstype')\" onfocus=\"\"><option value=\"\" </option>");
            if (mode.equalsIgnoreCase("edit") && amountunits.trim().length() == 0 && "C".equalsIgnoreCase(amountunitsType)) {
                htmlData.append("<option selected value=\"\">(Containers)</option>");
            } else {
                htmlData.append("<option value=\"(Containers)\">(Containers)</option>");
            }
            this.renderUnitsDD(htmlData, amountunits);
            htmlData.append("</select></td>");
        } else {
            htmlData.append("<td nowrap style=\"border=0\" id=\"reagentinventory_amounttitle_" + colIndx + "\">" + this.amountUsedTitle + ":&nbsp;</td>");
            htmlData.append("<td nowrap id=\"reagentinventory_amountusedtitle_" + colIndx + "\"><input class=\"input_field\" size=6 tabindex=" + tabindex++ + (amountEditable && !viewonly ? " class=\"dataentry_grid_cell\"" : " readonly style=\"border:1px solid gray;\"") + " type=text size=8 name=\"" + "reagentinventory_" + "amount_" + colIndx + "\" id=\"" + "reagentinventory_" + "amount_" + colIndx + "\" onchange=\"reagentInventory.validateAmount(this)\" oninput=\"setChangesMade()\" value=\"" + amount + "\">" + this.getInstrumentIconHTML("reagentinventory_", colIndx, instrumenttypeid, instrumentfieldid) + "</td>");
            htmlData.append("<td style=\"border=0\" colspan=2 id=\"reagentinventory_amountunitstitle_" + colIndx + "\" nowrap>" + this.unitTitle + ":&nbsp;&nbsp;&nbsp;");
            htmlData.append("<select tabindex=" + tabindex++ + " name=\"" + "reagentinventory_" + "amountunits_" + colIndx + "\" id=\"" + "reagentinventory_" + "amountunits_" + colIndx + "\" maxlength=\"" + this.maxLotSize + "\"  onchange=\"reagentUtil.setAmountUnitType(this,'amountunitstype')\" onfocus=\"\" " + (amountEditable && !viewonly ? "" : "disabled") + "><option value=\"\"></option>");
            if (mode.equalsIgnoreCase("edit") && amountunits.trim().length() == 0 && "C".equalsIgnoreCase(amountunitsType)) {
                htmlData.append("<option selected value=\"\">(Containers)</option>");
            } else {
                htmlData.append("<option value=\"(Containers)\">(Containers)</option>");
            }
            this.renderUnitsDD(htmlData, amountunits);
            htmlData.append("</select></td>");
        }
        htmlData.append("</tr>");
    }

    private void buildRLTIJson(StringBuffer strRLTIJson, ArrayList<String> uniqueReagentTypes, String reagenttypeid, String reagenttypeVersionid, DataSet dsReagentLot, DataSet dsReagentLotManageInvtFlag) {
        if (!uniqueReagentTypes.contains(reagenttypeid + "|" + reagenttypeVersionid)) {
            ArrayList<DataSet> list;
            ArrayList<DataSet> arrayList = list = dsReagentLot != null && dsReagentLot.size() > 0 ? dsReagentLot.getGroupedDataSets("linkkeyid1") : null;
            if (list != null && list.size() > 0) {
                for (int rIndx = 0; rIndx < list.size(); ++rIndx) {
                    DataSet ds = (DataSet)list.get(rIndx);
                    ds.sort("linkkeyid1,trackitemid");
                    HashMap<String, String> hmReagentLot = new HashMap<String, String>();
                    hmReagentLot.put("reagentlotid", ds.getString(0, "linkkeyid1", ""));
                    DataSet dsRLMCFlag = dsReagentLotManageInvtFlag.getFilteredDataSet(hmReagentLot);
                    String managecontainerflag = dsRLMCFlag.getString(0, "managecontainerinventoryflag", "Y");
                    if (this.firstReagentLot) {
                        strRLTIJson.append("\"" + ds.getString(0, "linkkeyid1", "").replaceAll("-", "") + "\":[");
                        this.firstReagentLot = false;
                    } else {
                        strRLTIJson.append(",\"" + ds.getString(0, "linkkeyid1", "").replaceAll("-", "") + "\":[");
                    }
                    strRLTIJson.append("\"" + managecontainerflag + "\"");
                    for (int tIndx = 0; ds != null && tIndx < ds.size(); ++tIndx) {
                        strRLTIJson.append(",\"" + ds.getString(tIndx, "trackitemid", "") + "\"");
                    }
                    strRLTIJson.append("]");
                }
            }
            uniqueReagentTypes.add(reagenttypeid + "|" + reagenttypeVersionid);
        }
    }

    private void renderUnitsDD(StringBuffer htmlData, String amountunits) {
        for (int i = 0; i < this.dsUnits.size(); ++i) {
            if (this.dsUnits.getValue(i, "keyid1").equalsIgnoreCase(amountunits)) {
                htmlData.append("<option selected value=\"" + this.dsUnits.getValue(i, "keyid1") + "\">" + this.dsUnits.getValue(i, "keyid1") + "</option>");
                continue;
            }
            htmlData.append("<option value=\"" + this.dsUnits.getValue(i, "keyid1") + "\">" + this.dsUnits.getValue(i, "keyid1") + "</option>");
        }
    }

    private String getTrackItemWithAvailQuantity(DataSet dsReagentLot, String typetargetconcentration, String requiredamount, String requiredamountunits, String requiredamountunitstype) {
        String trackitemid = "";
        String reagentlotid = "";
        String avlamount = "";
        String reagenttypeid = "";
        String reagenttypeversionid = "";
        ArrayList<DataSet> dsReagentLotArr = dsReagentLot.getGroupedDataSets("linkkeyid1");
        boolean trackitemWithEnoughAvailQuantityFound = false;
        for (DataSet ds : dsReagentLotArr) {
            if (ReagentUtil.hasDepartmentalSecurityAccess("LV_ReagentLot", ds.getString(0, "linkkeyid1", ""), "", "", this.sdcProcessor, this.connectionInfo.getConnectionId())) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    if (ds.getValue(i, "stopautoselectcontainerflag", "N").equalsIgnoreCase("Y")) continue;
                    trackitemid = ds.getValue(i, "trackitemid", "");
                    reagentlotid = ds.getValue(i, "linkkeyid1", "");
                    String containertypeid = ds.getValue(i, "containertypeid", "");
                    String qtycurrent = ds.getValue(i, "qtycurrent", "");
                    String qtyunits = ds.getValue(i, "qtyunits", "");
                    String qtycurrenttype = ds.getValue(i, "qtycurrenttype", "");
                    reagenttypeid = ds.getValue(i, "reagenttypeid", "");
                    reagenttypeversionid = ds.getValue(i, "reagenttypeversionid", "");
                    String actualconcentration = ds.getValue(i, "actualconcentration", "");
                    String targetconcentration = ds.getValue(i, "targetconcentration", "");
                    avlamount = qtycurrent + " " + ("C".equalsIgnoreCase(qtycurrenttype) ? qtycurrenttype : qtyunits);
                    if (this.tiAvailableQuantity.containsKey(trackitemid)) {
                        String currAvailQty = this.tiAvailableQuantity.get(trackitemid);
                        String[] currAvailQtyArr = StringUtil.split(currAvailQty, this.specialDelimer);
                        qtycurrent = currAvailQtyArr[0];
                        qtyunits = currAvailQtyArr[1];
                        qtycurrenttype = currAvailQtyArr[2];
                    }
                    if (qtycurrent == null || qtycurrent.trim().length() == 0) {
                        trackitemid = "";
                        reagentlotid = "";
                        continue;
                    }
                    double qtycurrentD = this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(qtycurrent, "" + this.decimalSeperator)).doubleValue();
                    if (qtycurrentD == 0.0) {
                        trackitemid = "";
                        reagentlotid = "";
                        continue;
                    }
                    if (requiredamount.length() == 0) {
                        trackitemWithEnoughAvailQuantityFound = true;
                        break;
                    }
                    if ((actualconcentration.length() > 0 || targetconcentration.length() > 0) && typetargetconcentration.length() > 0) {
                        if (actualconcentration.length() == 0) {
                            actualconcentration = targetconcentration;
                        }
                        actualconcentration = UnitsUtil.convertToLocateSeperated(actualconcentration, "" + this.decimalSeperator);
                        double multifactor = this.formatUtil.parseBigDecimal(typetargetconcentration = UnitsUtil.convertToLocateSeperated(typetargetconcentration, "" + this.decimalSeperator)).doubleValue() / this.formatUtil.parseBigDecimal(actualconcentration).doubleValue();
                        if (multifactor != 1.0) {
                            int scale = ReagentUtil.getMaxScale(requiredamount, this.decimalSeperator);
                            requiredamount = UnitsUtil.convertToLocateSeperated(requiredamount, "" + this.decimalSeperator);
                            double adjAmt = this.formatUtil.parseBigDecimal(requiredamount).doubleValue() * multifactor;
                            BigDecimal bd = BigDecimal.valueOf(adjAmt);
                            bd = bd.setScale(scale, 4);
                            requiredamount = ReagentUtil.removeLastZerosAferDecimal(bd.toString(), this.decimalSeperator);
                            requiredamount = requiredamount.replace('.', this.decimalSeperator);
                        }
                    }
                    double requiredamountD = this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(requiredamount, "" + this.decimalSeperator)).doubleValue();
                    try {
                        String containerSize = "";
                        String containerUnits = "";
                        if (!qtyunits.equalsIgnoreCase(requiredamountunits)) {
                            String qtycurrent_temp;
                            double newValue;
                            DataSet containerDS;
                            if ((requiredamountunitstype.equalsIgnoreCase("C") || qtycurrenttype.equalsIgnoreCase("C")) && (containerDS = this.getContainerDS(containertypeid)) != null && containerDS.getRowCount() > 0) {
                                containerSize = containerDS.getValue(0, "sizevalue", "");
                                containerUnits = containerDS.getString(0, "sizeunits", "");
                            }
                            if (qtycurrenttype.equalsIgnoreCase("C")) {
                                newValue = UnitsUtil.convertFromContainersToUnits(this.qp, containerSize, containerUnits, qtycurrent, requiredamountunits);
                                qtycurrent_temp = Double.toString(newValue);
                            } else if (requiredamountunitstype.equalsIgnoreCase("C")) {
                                newValue = UnitsUtil.covertFromUnitsToContainer(this.qp, containerSize, containerUnits, qtycurrent, qtyunits);
                                qtycurrent_temp = Double.toString(newValue);
                            } else {
                                qtycurrent_temp = UnitsUtil.getConvertedValue(this.qp, qtyunits, requiredamountunits, qtycurrent.replace(this.decimalSeperator, '.'));
                            }
                            qtycurrentD = this.formatUtil.parseBigDecimal(UnitsUtil.convertToLocateSeperated(qtycurrent_temp, "" + this.decimalSeperator)).doubleValue();
                        }
                        if (qtycurrentD >= requiredamountD) {
                            trackitemWithEnoughAvailQuantityFound = true;
                            String newAvailTI = qtycurrentD - requiredamountD + this.specialDelimer + requiredamountunits + this.specialDelimer + requiredamountunitstype;
                            this.tiAvailableQuantity.put(trackitemid, newAvailTI);
                            break;
                        }
                        trackitemid = "";
                        reagentlotid = "";
                        continue;
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!trackitemWithEnoughAvailQuantityFound) continue;
            break;
        }
        return trackitemid + this.specialDelimer + reagentlotid + this.specialDelimer + avlamount + this.specialDelimer + requiredamount + this.specialDelimer + reagenttypeid + this.specialDelimer + reagenttypeversionid;
    }

    private DataSet getContainerDS(String containertypeid) {
        this.safeSQL.reset();
        String sqlContainertype = "select sizevalue,sizeunits from containertype where containertypeid=" + this.safeSQL.addVar(containertypeid);
        return this.qp.getPreparedSqlDataSet(sqlContainertype, this.safeSQL.getValues());
    }

    private String getManageInvForType(DataSet ds, String reagenttypeid, String reagenttypeVersionid) {
        String managecontainerflagRT;
        HashMap<String, String> hmReagentType = new HashMap<String, String>();
        hmReagentType.put("reagenttypeid", reagenttypeid);
        if (reagenttypeVersionid.length() > 0) {
            hmReagentType.put("reagenttypeversionid", reagenttypeVersionid);
            DataSet dsRTMCFlag = ds.getFilteredDataSet(hmReagentType);
            managecontainerflagRT = dsRTMCFlag.getString(0, "managecontainerinventoryflag", "Y");
        } else {
            DataSet dsRTMCFlag = ds.getFilteredDataSet(hmReagentType);
            managecontainerflagRT = dsRTMCFlag.getString(0, "managecontainerinventoryflag", "Y");
            String versionstatus = dsRTMCFlag.getString(0, "versionstatus", "");
            if (!versionstatus.equalsIgnoreCase("C")) {
                int maxVersion = Integer.parseInt(dsRTMCFlag.getString(0, "reagenttypeversionid", "1"));
                for (int i = 1; i < dsRTMCFlag.getRowCount(); ++i) {
                    int version;
                    versionstatus = dsRTMCFlag.getString(i, "versionstatus", "");
                    if (versionstatus.equalsIgnoreCase("C")) {
                        managecontainerflagRT = dsRTMCFlag.getString(i, "managecontainerinventoryflag", "Y");
                        break;
                    }
                    if (versionstatus.equalsIgnoreCase("E") || (version = Integer.parseInt(dsRTMCFlag.getString(i, "reagenttypeversionid", "1"))) <= maxVersion) continue;
                    maxVersion = version;
                    managecontainerflagRT = dsRTMCFlag.getString(i, "managecontainerinventoryflag", "Y");
                }
            }
        }
        return managecontainerflagRT;
    }

    private String buildJSONStr(String keyid1, String keyid2, String keyid3, String paramListId, String paramListVersionId, String variantId, String dsnoBD, String originalReagentType, String originalReagentTypeVersion) {
        StringBuffer jsonStr = new StringBuffer();
        jsonStr.append("{'sdcid':'" + this.sdcid);
        jsonStr.append("','keyid1':'" + keyid1);
        jsonStr.append("','keyid2':'" + keyid2);
        jsonStr.append("','keyid3':'" + keyid3);
        jsonStr.append("','paramlistid':'" + paramListId);
        jsonStr.append("','paramlistversionid':'" + paramListVersionId);
        jsonStr.append("','variantid':'" + variantId);
        jsonStr.append("','dataset':'" + dsnoBD);
        jsonStr.append("','reagenttypeid':'" + originalReagentType);
        jsonStr.append("','reagenttypeversionid':'" + originalReagentTypeVersion);
        jsonStr.append("','trackitemtitle':'" + this.trackitemTitle);
        jsonStr.append("','reagentlottitle':'" + this.reagentLotTitle);
        jsonStr.append("','ajaxclass':'" + this.ajaxClass);
        jsonStr.append("'}");
        return jsonStr.toString();
    }

    private String getAutoLoadedIndicator() {
        String indicator = "";
        if (this.autoloadedContainerIndicator.equalsIgnoreCase("A")) {
            indicator = "<img title=\"" + this.autoloadedContainerTooltips + "\" src=\"rc?command=image&image=FlatBlackBookmark1\"/> ";
        } else if (this.autoloadedContainerIndicator.equalsIgnoreCase("B")) {
            indicator = "<img title=\"" + this.autoloadedContainerTooltips + "\" src=\"rc?command=image&image=FlatBlackBookmark2\"/>";
        }
        return indicator;
    }
}

