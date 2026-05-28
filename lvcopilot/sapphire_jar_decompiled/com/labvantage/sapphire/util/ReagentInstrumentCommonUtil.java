/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.PageTagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReagentInstrumentCommonUtil {
    protected final String cFNS = "{fn concat(";
    protected final String cFNE = ")}";
    protected String specialDelimer = "^^^";
    protected String inClauseDelimeter = "','";
    protected boolean requirePL = false;
    protected boolean requireWI = false;
    protected boolean requireLinkPL = false;
    protected boolean workitemonly = false;
    protected boolean isOracle = true;
    protected String css_maintFormTable = "maintform_table";
    protected String css_columnHeader = "gridmaint_fieldtitle";
    protected String css_rowHeader = "maintform_fieldtitle";
    protected String selectionMode_Lookup = "Lookup";
    protected String selectionMode_ScanMode = "ScanMode";
    protected String selectionMode_ScanOrLookUp = "ScanOrLookUp";
    protected String selectionMode_Dropdown = "Dropdown";
    protected String lookupImage = "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg";
    protected String editImage = "WEB-CORE/images/png/Edit.png";
    protected String reagentMaintPage = "VirtualLotMaint";
    protected String filldownImage = "rc?command=image&amp;image=FlatBlackArrowDown&amp;size=16&amp;height=16&amp;width=16";
    protected QueryProcessor qp;
    protected ActionProcessor ap;
    protected TranslationProcessor tp;
    protected SDIProcessor sdip;
    protected SDCProcessor sdcProcessor;
    protected FormatUtil formatUtil;
    protected String datasource;
    protected String sdcid = "Sample";
    protected String sourcename = "";
    protected PropertyList pagedata;
    protected String css_ContentCellStyle = "border:1px solid #BDCCD4;background-color:white;";
    protected String css_EmptyContentCellStyle = "border:1px solid #BDCCD4;background-color: gainsboro;";
    protected String css_MergeContentCellStyle = "border:1px solid #BDCCD4;";
    protected boolean isQCBatchReagent = false;
    protected boolean isQCBatch = false;
    protected boolean isQCBatchSampleType = false;
    protected boolean isReagentLotRecipe = false;
    protected boolean isTransferExecutionReagents = false;
    protected HashMap<String, String> fieldValueHM = new HashMap();
    protected String extraColumFromcluase;
    protected String extraColumWherecluase;
    protected String table1;
    protected String table2;
    protected boolean needToShowExtraColumns = false;
    protected PropertyListCollection extraColumns;
    protected String qcbatch = "qcbatch";
    protected String reagentlotrecipe = "ReagentLotRecipe";
    protected String transferexecutionreagent = "transferexecutionreagent";
    protected boolean areAllSampleCancelled = false;
    protected ConfigurationProcessor cp;
    protected String auditreason = "";
    protected String auditactivity = "";
    protected String auditsignedflag = "";
    protected String worksheetid = "";
    protected String worksheetversionid = "";
    protected String worksheetitemid = "";
    protected String worksheetitemversionid = "";
    SequenceProcessor sp;
    protected ConnectionInfo connectionInfo;
    protected final String requiredNote = "(R) indicates required for Dataset Completion.";
    protected int headerColumnCount = 0;
    protected boolean mandatoryFound = false;
    protected boolean showCheckboxSelector;
    protected boolean showButtonForAdhocConsumable;
    protected String promptForAdhocConsumable;
    protected boolean showButtonForAdhocInstrument;
    protected String promptForAdhocInstrument;
    protected String adhoctablename;
    protected int fixColumnsTotalWidth;
    protected DAMProcessor dp;

    protected ReagentInstrumentCommonUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request, String srname) {
        this.sourcename = srname;
        this.datasource = pageinfo.getProperty(this.sourcename, "");
        this.formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(pageContext));
        this.ap = pageinfo.getActionProcessor();
        this.qp = pageinfo.getQueryProcessor();
        this.sp = new SequenceProcessor(pageContext);
        this.sdip = new SDIProcessor(pageContext);
        this.sdcProcessor = new SDCProcessor(pageContext);
        this.tp = new TranslationProcessor(pageContext);
        this.pagedata = pageinfo.getPropertyList("pagedata");
        this.cp = new ConfigurationProcessor(pageinfo.getConnectionid());
        this.dp = new DAMProcessor(pageContext);
        this.connectionInfo = pageinfo.getConnectionProcessor().getConnectionInfo(pageinfo.getConnectionId());
        this.isOracle = ((RequestContext)pageContext.getRequest().getAttribute("RequestContext")).getProperty("dbms").equals("ORA");
        if ("sdidatarelation".equalsIgnoreCase(this.datasource)) {
            this.requirePL = true;
            this.requireWI = false;
            this.requireLinkPL = true;
        } else if ("sdiwirelation".equalsIgnoreCase(this.datasource)) {
            this.requirePL = false;
            this.requireWI = true;
            this.requireLinkPL = false;
        } else {
            this.requirePL = true;
            this.requireWI = true;
            this.requireLinkPL = true;
        }
        String string = this.sdcid = request.getParameter("sdcid") == null ? "" : request.getParameter("sdcid");
        if (this.sdcid.trim().length() == 0) {
            this.sdcid = "Sample";
        }
        this.isQCBatchReagent = this.isQCBatchReagent(this.sdcid);
        this.isQCBatchSampleType = this.isQCBatchSampleType(this.sdcid);
        this.isReagentLotRecipe = this.sdcid != null && this.sdcid.equalsIgnoreCase("LV_ReagentLot");
        this.isTransferExecutionReagents = this.sdcid != null && this.sdcid.equalsIgnoreCase("LV_TransferExecution");
        this.isQCBatch = this.isQCBatchReagent || this.isQCBatchSampleType;
        String workitemid = pageinfo.getProperty("workitemid", "");
        String workiteminstance = pageinfo.getProperty("workiteminstance", "");
        this.workitemonly = false;
        if ("sdiwirelation".equalsIgnoreCase(this.datasource) && workitemid.length() > 0 && workiteminstance.length() > 0) {
            this.workitemonly = true;
        }
        this.areAllSampleCancelled = this.areAllSamplesCancelled();
        this.worksheetid = pageinfo.getProperty("worksheetid", "");
        this.worksheetversionid = pageinfo.getProperty("worksheetversionid", "");
        this.worksheetitemid = pageinfo.getProperty("worksheetitemid", "");
        this.worksheetitemversionid = pageinfo.getProperty("worksheetitemversionid", "");
        this.showCheckboxSelector = this.pagedata.getProperty("showcheckboxselector", "Y").equalsIgnoreCase("Y");
        this.fixColumnsTotalWidth = this.showCheckboxSelector ? 20 : 0;
        this.showButtonForAdhocConsumable = this.pagedata.getProperty("showbuttonforadhocconsumable", "Y").equalsIgnoreCase("Y");
        this.promptForAdhocConsumable = this.pagedata.getProperty("promptforadhocconsumable", "AddAdhocConsumable");
        this.showButtonForAdhocInstrument = this.pagedata.getProperty("showbuttonforadhocinstrument", "Y").equalsIgnoreCase("Y");
        this.promptForAdhocInstrument = this.pagedata.getProperty("promptforadhocinstrument", "AddAdhocInstrument");
        this.adhoctablename = this.pagedata.getProperty("adhocconsumablefor", "sdidatarelation");
    }

    public void setChooseDataSetDD(PropertyList advancedtoolbar, PageTagInfo pageinfo) {
        String uniquePlsDDHtml;
        if (!this.datasource.equalsIgnoreCase(this.qcbatch) && !"sdiwirelation".equalsIgnoreCase(this.datasource) && (uniquePlsDDHtml = this.getTestChooserDD(pageinfo)).length() > 0) {
            advancedtoolbar.setProperty("customgrouptext", this.tp.translate("Choose a Dataset"));
            advancedtoolbar.setProperty("customgroupcontent", uniquePlsDDHtml);
        }
        this.updateToolbarButtons(advancedtoolbar);
    }

    public void updatePageData(PageTagInfo pageinfo) {
        String selTest = pageinfo.getProperty("test");
        this.pagedata.setProperty(this.workitemonly ? "selectedwi" : "selectedds", this.getSelectedItems(pageinfo));
        if (selTest.length() > 0) {
            String[] selTestArr = StringUtil.split(selTest, "|");
            this.pagedata.setProperty("paramlistid", selTestArr[0]);
            this.pagedata.setProperty("paramlistversionid", selTestArr[1]);
            this.pagedata.setProperty("variantid", selTestArr[2]);
            this.pagedata.setProperty("keyid1", selTestArr[3]);
        } else if (this.datasource.equalsIgnoreCase("sdiwirelation")) {
            this.pagedata.setProperty("keyid1", pageinfo.getProperty("keyid1"));
            if (this.workitemonly) {
                this.pagedata.setProperty("workitemid", pageinfo.getProperty("workitemid"));
                this.pagedata.setProperty("workiteminstance", pageinfo.getProperty("workiteminstance"));
            } else {
                this.pagedata.setProperty("paramlistid", pageinfo.getProperty("paramlistid"));
                this.pagedata.setProperty("paramlistversionid", pageinfo.getProperty("paramlistversionid"));
                this.pagedata.setProperty("variantid", pageinfo.getProperty("variantid"));
            }
        } else {
            String pl = this.getFirstItem(pageinfo);
            String[] plArr = StringUtil.split(pl, this.specialDelimer);
            this.pagedata.setProperty("keyid1", plArr[0]);
            this.pagedata.setProperty("paramlistid", plArr[1]);
            this.pagedata.setProperty("paramlistversionid", plArr[2]);
            this.pagedata.setProperty("variantid", plArr[3]);
        }
    }

    protected String getHeaderColumnHtml(String columnValue) {
        return "<th nowrap class=\"" + this.css_columnHeader + "\">" + this.tp.translate(SafeHTML.encodeForHTML(columnValue)) + "</th>";
    }

    protected String getHeaderColumnHtml(String columnValue, String width) {
        return "<td width=\"" + width + "px\" style='font-weight:bold;'>" + this.tp.translate(SafeHTML.encodeForHTML(columnValue)) + "</td>";
    }

    protected String getRowColumnValueHtml(String value, String width) {
        return "<td width=\"" + width + "px\" align=center nowrap>" + SafeHTML.encodeForHTML(value) + "</td>";
    }

    protected String getVisibleColumnHtml(PropertyListCollection visibleColumns) {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < visibleColumns.size(); ++i) {
            PropertyList pl = visibleColumns.getPropertyList(i);
            String columnid = this.getColumnAlias(pl.getProperty("columnid", ""));
            if (this.workitemonly && !columnid.equalsIgnoreCase("keyid1")) continue;
            String width = pl.getProperty("width", "100").trim();
            html.append(this.getHeaderColumnHtml(pl.getProperty("title"), width));
            ++this.headerColumnCount;
        }
        return html.toString();
    }

    protected String getConfiguredColumnsHeader(boolean isReagent, PropertyListCollection visibleColumns) {
        StringBuffer html = new StringBuffer();
        if (visibleColumns.size() > 0) {
            html.append("<th style=\"" + this.getFixedPositionStyleForLeft(0, 2) + ";border:1px solid #B0C4DE; background-color: #CCCCCC; color: #000000; font-weight:bold; padding-left: 0px; padding-right: 0px;\">");
            html.append("<table width='" + this.fixColumnsTotalWidth + "px' style='table-layout: fixed;'><tr width='100%'>");
        }
        html.append(this.getCheckboxSelector(isReagent));
        html.append(this.getVisibleColumnHtml(visibleColumns));
        if (visibleColumns.size() > 0) {
            html.append("</tr></table></th>");
        }
        return html.toString();
    }

    protected String getConfiguredColumnsValue(int selectedDSIndx, boolean isReagent, PropertyListCollection visibleColumns, DataSet sdirelations, int indx) {
        StringBuffer html = new StringBuffer();
        if (visibleColumns.size() > 0) {
            html.append("<td class=\"" + this.css_rowHeader + "\" style=\"" + this.getFixedPositionStyleForLeft(0, 1) + "\">");
            html.append("<table width='" + this.fixColumnsTotalWidth + "px' style='table-layout: fixed;'><tr width='100%'>");
        }
        html.append(this.getCheckboxSelector(selectedDSIndx, isReagent));
        html.append(this.getVisibleColumnValueHtml(visibleColumns, sdirelations, indx));
        if (visibleColumns.size() > 0) {
            html.append("</tr></table></td>");
        }
        return html.toString();
    }

    protected String getCheckboxSelector(boolean isReagent) {
        if (this.showCheckboxSelector) {
            if (isReagent) {
                return "<td width=\"20px\"><input type=\"checkbox\" onclick=\"reagentUtil.checkAllCells( this, 'reagentinventory' )\" id=\"reagentinventory_selectall\"></th>";
            }
            return "<td width=\"20px\"><input type=\"checkbox\" onclick=\"instrumentUtil.checkAllCells( this, 'instrumentinventory' )\" id=\"instrumentinventory_selectall\"></th>";
        }
        return "";
    }

    protected String getCheckboxSelector(int selectedDSIndx, boolean isReagent) {
        if (this.showCheckboxSelector) {
            if (isReagent) {
                return "<td width=\"20px\" align=center nowrap><input onclick=\"reagentUtil.checkBoxOnClick(this, " + selectedDSIndx + ",event)\" type=\"checkbox\" name=\"reagentinventory_selector\" id=\"__pr" + selectedDSIndx + "\" value=\"__pr" + selectedDSIndx + "\"></td>";
            }
            return "<td width=\"20px\" align=center nowrap><input onclick=\"instrumentUtil.checkBoxOnClick(this, " + selectedDSIndx + ",event)\" type=\"checkbox\" name=\"instrumentinventory_selector\" id=\"__pr" + selectedDSIndx + "\" value=\"__pr" + selectedDSIndx + "\"></td>";
        }
        return "";
    }

    protected String getButtonForAdhocConsumable(int selectedDSIndx) {
        if (this.showButtonForAdhocConsumable) {
            return "<td class=\"" + this.css_rowHeader + "\"><img class=\"gwt-Image\" border=\"0\" title=\"Add Adhoc Consumable\" src=\"WEB-CORE/images/png/Add.png\" class=\"lookup_img\" onclick=\"reagentInventory.openPrompt(" + selectedDSIndx + ");\"></td>";
        }
        return "";
    }

    protected String getButtonForAdhocInstrument(int selectedDSIndx) {
        if (this.showButtonForAdhocInstrument) {
            return "<td class=\"" + this.css_rowHeader + "\"><img class=\"gwt-Image\" border=\"0\" title=\"Add Adhoc Instrument\" src=\"WEB-CORE/images/png/Add.png\" class=\"lookup_img\" onclick=\"instrumentInventory.openPrompt(" + selectedDSIndx + ");\"></td>";
        }
        return "";
    }

    protected String getVisibleColumnValueHtml(PropertyListCollection visibleColumns, DataSet ds, int indx) {
        StringBuffer htmlData = new StringBuffer();
        for (int i = 0; i < visibleColumns.size(); ++i) {
            PropertyList pl = visibleColumns.getPropertyList(i);
            String columnid = this.getColumnAlias(pl.getProperty("columnid", ""));
            if (this.workitemonly && !columnid.equalsIgnoreCase("keyid1")) continue;
            String width = pl.getProperty("width", "100").trim();
            String value = ds.getValue(indx, columnid, "");
            htmlData.append(this.getRowColumnValueHtml(value, width));
        }
        return htmlData.toString();
    }

    protected void addToSelecetdDataSet(DataSet sourceDS, DataSet targetDS) {
        if (sourceDS != null && sourceDS.size() > 0) {
            for (int i = 0; i < sourceDS.size(); ++i) {
                String keyid1 = sourceDS.getString(i, "keyid1", "");
                String itemkeyid1 = sourceDS.getString(i, "itemkeyid1", "");
                String itemkeyid2 = sourceDS.getString(i, "itemkeyid2", "");
                String itemkeyid3 = sourceDS.getString(i, "itemkeyid3", "");
                String iteminstance = sourceDS.getValue(i, "iteminstance", "");
                String workitemid = sourceDS.getString(i, "workitemid", "");
                String workiteminstance = sourceDS.getValue(i, "workiteminstance", "");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("keyid1", keyid1);
                if (this.workitemonly) {
                    hm.put("workitemid", workitemid);
                    hm.put("workiteminstance", workiteminstance);
                } else {
                    hm.put("paramlistid", itemkeyid1);
                    hm.put("paramlistversionid", itemkeyid2);
                    hm.put("variantid", itemkeyid3);
                    hm.put("dataset", iteminstance);
                }
                int findIndx = targetDS.findRow(hm);
                if (findIndx >= 0) continue;
                int indx = targetDS.addRow();
                targetDS.setString(indx, "keyid1", keyid1);
                if (this.workitemonly) {
                    targetDS.setString(indx, "workitemid", workitemid);
                    targetDS.setString(indx, "workiteminstance", workiteminstance);
                    continue;
                }
                targetDS.setString(indx, "paramlistid", itemkeyid1);
                targetDS.setString(indx, "paramlistversionid", itemkeyid2);
                targetDS.setString(indx, "variantid", itemkeyid3);
                targetDS.setString(indx, "dataset", iteminstance);
            }
        }
    }

    protected String getColumnAlias(String columnid) {
        String columnAlias = columnid;
        if (columnid != null) {
            if ((columnid = columnid.trim()).indexOf("(") == 0) {
                int lastIndx = columnid.lastIndexOf(")");
                columnAlias = columnid.substring(lastIndx + 1);
                columnAlias = columnAlias.trim();
            } else if (columnid.contains(" ")) {
                columnAlias = StringUtil.split(columnid, " ")[1].trim();
            } else if (columnid.contains(".")) {
                columnAlias = StringUtil.split(columnid, ".")[1].trim();
            }
        }
        return columnAlias;
    }

    protected boolean isColumnIdWithAlias(String columnid) {
        boolean flag = false;
        if (columnid != null && !columnid.contains("(") && !columnid.contains(")") && columnid.contains(" ")) {
            flag = true;
        }
        return flag;
    }

    private boolean isQCBatchSampleType(String sourcesdc) {
        return sourcesdc != null && sourcesdc.equalsIgnoreCase("QCBatchSampleType");
    }

    private boolean isQCBatchReagent(String sourcesdc) {
        return sourcesdc != null && sourcesdc.equalsIgnoreCase("LV_QCBatchReagent");
    }

    protected String concatFields(String ... fields) {
        String str = "";
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = this.isOracle ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = "{fn concat({fn concat(" + str + ",'" + this.specialDelimer + "'" + ")}" + "," + (this.isOracle ? f : "cast(" + f + " as nvarchar(100))") + ")}";
        }
        return str;
    }

    protected String getTestChooserDD(PageTagInfo pageinfo) {
        ArrayList<String> keyid1AndDispalyPLs;
        StringBuffer html = new StringBuffer();
        String keyid1 = pageinfo.getProperty("keyid1", "");
        String selTest = pageinfo.getProperty("test", "");
        String paramlistid = pageinfo.getProperty("paramlistid", "");
        String paramlistversionid = pageinfo.getProperty("paramlistversionid", "");
        String variantid = pageinfo.getProperty("variantid", "");
        if (ReagentUtil.isInputEmpty(keyid1)) {
            return "";
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select keyid1,paramlistid,paramlistversionid,variantid from sdidata");
        sql.append(" where keyid1 in (").append(safeSQL.addIn(ReagentUtil.getUniqueValues(keyid1, ";"))).append(")");
        sql.append(" and paramlistid in (").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistid, ";"))).append(")");
        sql.append(" and paramlistversionid in (").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistversionid, ";"))).append(")");
        sql.append(" and variantid in (").append(safeSQL.addIn(ReagentUtil.getUniqueValues(variantid, ";"))).append(")");
        sql.append(" and availabilityflag ='Y' ");
        DataSet sdidata = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String[] keyid1dArr = keyid1.split(";");
        String[] paramlistidArr = paramlistid.split(";");
        String[] paramlistversionidArr = paramlistversionid.split(";");
        String[] variantidArr = variantid.split(";");
        String pl = "";
        String displayPL = "";
        LinkedHashMap uniquePlsHM = new LinkedHashMap();
        HashMap<String, String> hm = new HashMap<String, String>();
        for (int i = 0; i < keyid1dArr.length; ++i) {
            hm.clear();
            hm.put("keyid1", keyid1dArr[i]);
            hm.put("paramlistid", paramlistidArr[i]);
            hm.put("paramlistversionid", paramlistversionidArr[i]);
            hm.put("variantid", variantidArr[i]);
            if (sdidata.findRow(hm) == -1) continue;
            keyid1AndDispalyPLs = new ArrayList<String>();
            displayPL = paramlistidArr[i] + " (" + paramlistversionidArr[i] + "/" + variantidArr[i] + ") ";
            pl = paramlistidArr[i] + "|" + paramlistversionidArr[i] + "|" + variantidArr[i];
            String key = keyid1dArr[i];
            if (uniquePlsHM.containsKey(pl)) {
                key = key + ";" + (String)((ArrayList)uniquePlsHM.get(pl)).get(0);
            }
            keyid1AndDispalyPLs.add(key);
            keyid1AndDispalyPLs.add(displayPL);
            uniquePlsHM.put(pl, keyid1AndDispalyPLs);
        }
        if (uniquePlsHM.size() > 1) {
            html.append("<select id=\"riTest\" name=\"riTest\" onChange=\"changeTest(this.options[this.selectedIndex].value);\">");
            for (Map.Entry entry : uniquePlsHM.entrySet()) {
                pl = (String)entry.getKey();
                keyid1AndDispalyPLs = (ArrayList<String>)entry.getValue();
                String keyid1s = (String)keyid1AndDispalyPLs.get(0);
                String text = (String)keyid1AndDispalyPLs.get(1);
                String test = pl + "|" + keyid1s;
                if (selTest == null || selTest.trim().length() == 0) {
                    selTest = test;
                }
                if (test.equalsIgnoreCase(selTest)) {
                    html.append("<option value=\"" + test + "\" selected style=\"cursor: pointer\">" + text + "</option>");
                    continue;
                }
                html.append("<option value=\"" + test + "\" style=\"cursor: pointer\">" + text + "</option>");
            }
            html.append("</select>");
        }
        return html.toString();
    }

    protected String getFirstItem(PageTagInfo pageinfo) {
        String keyid1 = pageinfo.getProperty("keyid1");
        String paramlistid = pageinfo.getProperty("paramlistid");
        String paramlistversionid = pageinfo.getProperty("paramlistversionid");
        String variantid = pageinfo.getProperty("variantid");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String firstPl = paramlistidArr[0] + this.specialDelimer + paramlistversionidArr[0] + this.specialDelimer + variantidArr[0];
        StringBuffer keyid = new StringBuffer(keyid1Arr[0]);
        for (int i = 1; i < keyid1Arr.length; ++i) {
            String pl = paramlistidArr[i] + this.specialDelimer + paramlistversionidArr[i] + this.specialDelimer + variantidArr[i];
            if (!firstPl.equals(pl)) continue;
            keyid.append(";").append(keyid1Arr[i]);
        }
        return keyid.toString() + this.specialDelimer + firstPl;
    }

    protected String getSelectedItems(PageTagInfo pageinfo) {
        String value = "";
        String keyid1 = pageinfo.getProperty("keyid1");
        String[] keyid1dArr = StringUtil.split(keyid1, ";");
        if (this.workitemonly) {
            String workitemid = pageinfo.getProperty("workitemid");
            String workiteminstance = pageinfo.getProperty("workiteminstance");
            String[] workitemidArr = StringUtil.split(workitemid, ";");
            String[] workiteminstanceArr = StringUtil.split(workiteminstance, ";");
            for (int i = 0; i < keyid1dArr.length; ++i) {
                value = value + "|" + keyid1dArr[i] + this.specialDelimer + workitemidArr[i] + this.specialDelimer + workiteminstanceArr[i];
            }
        } else {
            String paramlistid = pageinfo.getProperty("paramlistid");
            String paramlistversionid = pageinfo.getProperty("paramlistversionid");
            String variantid = pageinfo.getProperty("variantid");
            String dataset = pageinfo.getProperty("dataset");
            String[] paramlistidArr = StringUtil.split(paramlistid, ";");
            String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
            String[] variantidArr = StringUtil.split(variantid, ";");
            String[] datasetArr = StringUtil.split(dataset, ";");
            for (int i = 0; i < keyid1dArr.length; ++i) {
                value = value + "|" + keyid1dArr[i] + this.specialDelimer + paramlistidArr[i] + this.specialDelimer + paramlistversionidArr[i] + this.specialDelimer + variantidArr[i] + this.specialDelimer + datasetArr[i];
            }
        }
        return value.length() > 0 ? value.substring(1) : "";
    }

    protected String addHiddenFields(HashMap<String, String> hm) {
        StringBuffer htmlData = new StringBuffer();
        for (String key : hm.keySet()) {
            htmlData.append("<input type=hidden name=\"" + key + "\" id=\"" + key + "\" value=\"" + hm.get(key) + "\">");
        }
        return htmlData.toString();
    }

    protected String AddHiddenFields(String dataEntryPrefix, String colIndx, HashMap<String, String> hm) {
        StringBuffer htmlData = new StringBuffer();
        for (String key : hm.keySet()) {
            System.out.println("Key: " + key + ", Value: " + hm.get(key));
            htmlData.append("<input type=hidden name=\"" + dataEntryPrefix + key + "_" + colIndx + "\" id=\"" + dataEntryPrefix + key + "_" + colIndx + "\" value=\"" + hm.get(key) + "\">");
        }
        return htmlData.toString();
    }

    protected String getSelectClause(PropertyListCollection extraColumns, String table1, String table2) {
        StringBuffer selectClause = new StringBuffer();
        if (extraColumns != null && extraColumns.size() > 0) {
            for (int i = 0; i < extraColumns.size(); ++i) {
                PropertyList column = extraColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid", "");
                if (columnid.contains(" ") && columnid.contains("(") && columnid.contains(")")) {
                    int lastSpaceIndx = columnid.lastIndexOf(" ");
                    String alias = columnid.substring(lastSpaceIndx).trim();
                    String updatedAlias = columnid.contains(table1 + ".") ? "t1_" + alias : "t2_" + alias;
                    columnid = columnid.substring(0, lastSpaceIndx) + " " + updatedAlias;
                    selectClause.append(selectClause.length() > 0 ? "%3B" : "").append(columnid);
                    continue;
                }
                selectClause.append(selectClause.length() > 0 ? "%3B" : "").append(columnid.contains(table2 + ".") ? columnid + " t2_" + columnid.substring(table2.length() + 1) : table1 + "." + columnid + " t1_" + columnid);
            }
        }
        return selectClause.toString();
    }

    protected String getExtraColumns(PropertyListCollection extraColumns, String table2) {
        StringBuffer selectClause = new StringBuffer();
        if (extraColumns != null && extraColumns.size() > 0) {
            for (int i = 0; i < extraColumns.size(); ++i) {
                PropertyList column = extraColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0) continue;
                if (columnid.contains(" ") && columnid.contains("(") && columnid.contains(")")) {
                    int lastSpaceIndx = columnid.lastIndexOf(" ");
                    String alias = columnid.substring(lastSpaceIndx).trim();
                    selectClause.append(selectClause.length() > 0 ? "%3B" : "").append(columnid.contains(table2 + ".") ? "t2_" + alias : "t1_" + alias);
                    continue;
                }
                selectClause.append(selectClause.length() > 0 ? "%3B" : "").append(columnid.contains(table2 + ".") ? "t2_" + columnid.substring(table2.length() + 1) : "t1_" + columnid);
            }
        }
        return selectClause.toString();
    }

    protected String renderExtraColumns(PropertyListCollection extraColumns, QueryProcessor qp, String keyid1, String fromcluase, String wherecluase, String table1, String table2) {
        String extraColumnHtml = "";
        if (extraColumns != null && extraColumns.size() > 0) {
            String selectClauseExtraColumns = this.getSelectClause(extraColumns, table1, table2);
            selectClauseExtraColumns = StringUtil.replaceAll(selectClauseExtraColumns, "%3B", ",");
            DataSet ds = qp.getPreparedSqlDataSet("select " + selectClauseExtraColumns + " from " + fromcluase + " where " + wherecluase, (Object[])new String[]{keyid1});
            if (ds != null && ds.size() > 0) {
                extraColumnHtml = this.renderExtraColumns(extraColumns, ds, 0, false, table2);
            }
        }
        return extraColumnHtml;
    }

    protected String renderExtraColumns(PropertyListCollection extraColumns, DataSet ds, int idx, boolean isTemplate, String table2) {
        StringBuffer extraColumnsHTML = new StringBuffer("");
        String prefix = "rl_";
        if (extraColumns != null && extraColumns.size() > 0) {
            for (int j = 0; j < extraColumns.size(); ++j) {
                String columAlias;
                PropertyList column = extraColumns.getPropertyList(j);
                if (!column.getProperty("show", "Y").equals("Y")) continue;
                String coltitle = column.getProperty("title");
                String columnid = column.getProperty("columnid");
                boolean keepWithPrior = column.getProperty("keepwithpreviousline", "N").equals("Y");
                String string = prefix = columnid.contains(table2 + ".") ? "t2_" : "t1_";
                if (columnid.contains(" ") && columnid.contains("(") && columnid.contains(")")) {
                    int lastSpaceIndx = columnid.lastIndexOf(" ");
                    String alias = columnid.substring(lastSpaceIndx).trim();
                    columAlias = columnid.contains(table2 + ".") ? "t2_" + alias : "t1_" + alias;
                } else {
                    columAlias = prefix + (columnid.contains(table2 + ".") ? columnid.substring(table2.length() + 1) : columnid);
                }
                String resolvedValue = "";
                if (isTemplate) {
                    resolvedValue = "[" + columAlias + "]";
                } else {
                    String displayValue = column.getProperty("displayvalue", "");
                    displayValue = this.replaceSubstitutionTokens(displayValue, ds, idx);
                    boolean isTranslate = "Y".equals(column.getProperty("translatevalue"));
                    String value = ds.getValue(idx, columAlias, "");
                    resolvedValue = this.getColumnDisplayValue(value, displayValue, isTranslate, this.tp);
                }
                extraColumnsHTML.append(keepWithPrior ? "" : (extraColumnsHTML.length() > 0 ? (isTemplate ? "||br||" : "<br>") : "")).append(coltitle.length() > 0 ? (keepWithPrior && extraColumnsHTML.length() > 0 ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "") + coltitle + ": " : (extraColumnsHTML.length() > 0 ? "&nbsp;&nbsp;" : "")).append(resolvedValue).append(keepWithPrior ? "" : "");
            }
        }
        return extraColumnsHTML.toString();
    }

    private String replaceSubstitutionTokens(String displayValue, DataSet ds, int idx) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(displayValue, "[", "]")) {
            if (ds.isValidColumn("t1_" + token)) {
                displayValue = StringUtil.replaceAll(displayValue, "[" + token + "]", ds.getString(idx, "t1_" + token, ""));
                continue;
            }
            if (!ds.isValidColumn("t2_" + token)) continue;
            displayValue = StringUtil.replaceAll(displayValue, "[" + token + "]", ds.getString(idx, "t2_" + token, ""));
        }
        return displayValue;
    }

    protected String getColumnDisplayValue(String value, String displayValue, boolean isTranslate, TranslationProcessor translationProcessor) {
        value = ListColumn.sanitizeHTMLValue(value);
        if (displayValue.length() > 0) {
            value = SDITagUtil.getDisplayValue(value, displayValue);
        }
        if (isTranslate) {
            value = translationProcessor.translate(value);
        }
        return value;
    }

    protected String getExtraColumnHiddenField(PropertyListCollection extraColumns, String table1, String table2) {
        StringBuffer htmlData = new StringBuffer();
        htmlData.append("<input type=hidden name=\"ec_selectclause\" id=\"ec_selectclause\" value=\"" + this.getSelectClause(extraColumns, table1, table2) + "\">");
        htmlData.append("<input type=hidden name=\"ec_columns\" id=\"ec_columns\" value=\"" + this.getExtraColumns(extraColumns, table2) + "\">");
        htmlData.append("<input type=hidden name=\"ec_htmltemplate\" id=\"ec_htmltemplate\" value=\"" + this.renderExtraColumns(extraColumns, null, 0, true, table2) + "\">\n");
        return htmlData.toString();
    }

    protected boolean isExtraColumnAvailable(PropertyListCollection extraColumns) {
        boolean available = false;
        if (extraColumns != null && extraColumns.size() > 0) {
            for (int j = 0; j < extraColumns.size(); ++j) {
                PropertyList column = extraColumns.getPropertyList(j);
                if (!column.getProperty("show", "Y").equals("Y")) continue;
                available = true;
                break;
            }
        }
        return available;
    }

    protected boolean isVirtualReagentLot(String reagentlotid) {
        boolean contentflag = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT contentflag FROM reagentlot ");
        sql.append(" WHERE reagentlotid = " + safeSQL.addVar(reagentlotid) + " ");
        DataSet ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            contentflag = ds.getString(0, "contentflag", "").equalsIgnoreCase("V");
        }
        return contentflag;
    }

    protected boolean areAllSamplesCancelled() {
        boolean allCancelled = false;
        String keyid1 = this.pagedata.getProperty("keyid1", "");
        String sdcid = this.pagedata.getProperty("sdcid", "");
        if ("Sample".equalsIgnoreCase(sdcid) && keyid1.length() > 0) {
            allCancelled = true;
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT s_sampleid FROM s_sample ");
            sql.append(" WHERE s_sampleid in (" + safeSQL.addIn(keyid1, ";") + ") ");
            sql.append(" and samplestatus!='Cancelled'");
            DataSet ds = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                allCancelled = false;
            }
        }
        return allCancelled;
    }

    private void updateToolbarButtons(PropertyList advancedtoolbar) {
        if (advancedtoolbar != null && this.areAllSampleCancelled) {
            PropertyListCollection buttons = advancedtoolbar.getCollection("buttons");
            for (PropertyList button : buttons) {
                if (!"Save".equalsIgnoreCase(button.getProperty("id"))) continue;
                button.getPropertyList("commonprops").setProperty("show", "N");
            }
        }
    }

    protected void setAuditProps(PropertyList props) {
        if (this.auditreason != null && this.auditreason.length() > 0) {
            props.setProperty("auditreason", this.auditreason);
            props.setProperty("auditactivity", this.auditactivity);
            props.setProperty("auditsignedflag", this.auditsignedflag);
        }
    }

    protected void addActivityLog() {
        if (this.worksheetid != null && this.worksheetid.length() > 0) {
            String SDC_WORKSHEET = "LV_Worksheet";
            String activitytype = "SetContent";
            String targetsdcid = "LV_WorksheetItem";
            String targetkeyid1 = this.worksheetitemid;
            String targetkeyid2 = this.worksheetitemversionid;
            String targetkeyid3 = "";
            String activitylog = "Set worksheet item content";
            int activitylogid = this.sp.getSequence(SDC_WORKSHEET, "activitylog");
            String sysuserid = !this.connectionInfo.getSysuserId().equals("(system)") ? this.connectionInfo.getSysuserId() : "";
            String insertSQl = "INSERT INTO worksheetactivitylog ( worksheetid, worksheetversionid, activitylogid, activityby, activitydt, activitytype, targetsdcid, targetkeyid1, targetkeyid2, targetkeyid3, targetauditseq, activitylog ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,(SELECT auditsequence FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?), ? )";
            this.qp.execPreparedUpdate(insertSQl, new Object[]{this.worksheetid, this.worksheetversionid, activitylogid, sysuserid, DateTimeUtil.getNowTimestamp(), activitytype, targetsdcid, targetkeyid1, targetkeyid2, targetkeyid3, targetkeyid1, targetkeyid2, activitylog.length() > 4000 ? activitylog.substring(0, 4000) : activitylog});
            if (sysuserid.length() > 0) {
                insertSQl = "INSERT INTO worksheetcontributor ( worksheetid, worksheetversionid, contributorid, nominatedflag ) SELECT '" + this.worksheetid + "', '" + this.worksheetversionid + "', '" + sysuserid + "', 'N' FROM sdc WHERE sdcid = 'SDC' AND NOT EXISTS ( SELECT worksheetid, worksheetversionid, contributorid FROM worksheetcontributor wsc1 WHERE wsc1.worksheetid = ? AND wsc1.worksheetversionid = ? AND wsc1.contributorid = ? )";
                this.qp.execPreparedUpdate(insertSQl, new Object[]{this.worksheetid, this.worksheetversionid, sysuserid});
            }
        }
    }

    protected boolean isContains(String selectedDatasets, String dataset) {
        String[] selectedDatasetsArr = StringUtil.split(selectedDatasets, "|");
        boolean contains = false;
        for (String ds : selectedDatasetsArr) {
            if (!dataset.equals(ds)) continue;
            contains = true;
            break;
        }
        return contains;
    }

    protected DataSet fetchControlData(String relationFunction, String extraColumnsForParamlist, String extraColumnsForWorkitem, String sdcid, String keyids, String workitemid, String workiteminstance, String paramlistid, String paramlistversionid, String variantid, String datasets) throws SapphireException {
        DataSet data = new DataSet();
        boolean isReagent = relationFunction.equalsIgnoreCase("Reagent");
        boolean isBoth = this.datasource.equalsIgnoreCase("");
        if (ReagentUtil.isInputEmpty(keyids) || ReagentUtil.isInputEmpty(paramlistid) && ReagentUtil.isInputEmpty(workitemid)) {
            return data;
        }
        String dicols = " 'P' sourcetype " + (extraColumnsForParamlist.length() > 0 ? ", " + extraColumnsForParamlist : "") + ", cast(sdidatarelation.relationid as integer) relationid,sdidatarelation.keyid1,sdidatarelation.keyid2,sdidatarelation.keyid3, sdidata.sourceworkitemid,sdidata.sourceworkiteminstance, sdidatarelation.relationtype, sdidatarelation.usersequence, sdidatarelation.sourcesdcid, sdidatarelation.sourcekeyid1,sdidatarelation.sourcekeyid2,sdidatarelation.sourcekeyid3,sdidatarelation.tokeyid1, sdidatarelation.refkeyid1, sdidatarelation.amount, sdidatarelation.amountunits, sdidatarelation.amountunitstype, sdidatarelation.mandatoryflag, sdidatarelation.requiredamount, sdidatarelation.requiredamountunits, sdidatarelation.requiredamountunitstype, sdidatarelation.amountadjusted,sdidatarelation.originalreagenttypeid,sdidatarelation.originalreagenttypeversionid,sdidatarelation.mandatoryflag,sdidatarelation.instrumentid";
        String wicols = " 'W' sourcetype " + (extraColumnsForWorkitem.length() > 0 ? "," + extraColumnsForWorkitem : "") + ", cast(sdiworkitemrelation.relationid as integer) relationid,sdiworkitemrelation.keyid1,sdiworkitemrelation.keyid2,sdiworkitemrelation.keyid3, sdiworkitemitem.workitemid sourceworkitemid,sdiworkitemitem.workiteminstance sourceworkiteminstance, sdiworkitemrelation.relationtype, sdiworkitemrelation.usersequence, sdiworkitemrelation.sourcesdcid,  sdiworkitemrelation.sourcekeyid1,sdiworkitemrelation.sourcekeyid2,sdiworkitemrelation.sourcekeyid3,sdiworkitemrelation.tokeyid1, sdiworkitemrelation.refkeyid1, sdiworkitemrelation.amount, sdiworkitemrelation.amountunits, sdiworkitemrelation.amountunitstype,sdiworkitemrelation.mandatoryflag, sdiworkitemrelation.requiredamount, sdiworkitemrelation.requiredamountunits, sdiworkitemrelation.requiredamountunitstype, sdiworkitemrelation.amountadjusted,sdiworkitemrelation.originalreagenttypeid,sdiworkitemrelation.originalreagenttypeversionid,sdiworkitemrelation.mandatoryflag,sdiworkitemrelation.instrumentid";
        if (isReagent) {
            String ticols = "trackitem.trackitemid,trackitem.qtycurrent, trackitem.qtyunits,trackitem.trackitemstatus, trackitem.linksdcid, trackitem.linkkeyid1";
            dicols = dicols + "," + ticols;
            wicols = wicols + "," + ticols;
        } else {
            String diResolvedmodel = "(CASE WHEN sdidatarelation.sourcesdcid='Instrument' THEN (select instrument.instrumentmodelid  from instrument where  instrument.instrumentid=sdidatarelation.sourcekeyid1 ) WHEN sdidatarelation.sourcesdcid='LV_InstrumentModel' THEN sdidatarelation.sourcekeyid1 WHEN sdidatarelation.sourcesdcid='LV_InstrumentType' THEN null END) instrmodel";
            String wiResolvedmodel = "(CASE WHEN sdiworkitemrelation.sourcesdcid='Instrument' THEN (select instrument.instrumentmodelid  from instrument where  instrument.instrumentid=sdiworkitemrelation.sourcekeyid1 ) WHEN sdiworkitemrelation.sourcesdcid='LV_InstrumentModel' THEN sdiworkitemrelation.sourcekeyid1 WHEN sdiworkitemrelation.sourcesdcid='LV_InstrumentType' THEN null END) instrmodel";
            String diTestingDepartmentId = "(select sdiworkitem.testingdepartmentid from sdiworkitem  where sdiworkitem.sdcid=sdidata.sdcid AND sdiworkitem.keyid1=sdidata.keyid1  AND sdiworkitem.keyid2=sdidata.keyid2 AND sdiworkitem.keyid3=sdidata.keyid3  AND sdiworkitem.workitemid=sdidata.sourceworkitemid  AND sdiworkitem.workiteminstance=sdidata.sourceworkiteminstance ) testingdepartmentid";
            String wiTestingDepartmentId = "sdiworkitem.testingdepartmentid";
            dicols = dicols + "," + diResolvedmodel + ", 'Y' isrelationdata ," + diTestingDepartmentId;
            wicols = wicols + "," + wiResolvedmodel + ", 'Y' isrelationdata ," + wiTestingDepartmentId;
        }
        dicols = dicols + ",sdidata.modifiableflag";
        wicols = wicols + ",sdidata.modifiableflag";
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        if (this.datasource.equalsIgnoreCase("sdidatarelation") || isBoth) {
            sql.append("SELECT " + dicols);
            sql.append(" FROM sdidata  ");
            sql.append(" left outer join sdidatarelation ");
            sql.append(" on sdidatarelation.sdcid = sdidata.sdcid ");
            sql.append(" AND sdidatarelation.keyid1 = sdidata.keyid1 ");
            sql.append(" AND sdidatarelation.keyid2 = sdidata.keyid2 ");
            sql.append(" AND sdidatarelation.keyid3 = sdidata.keyid3 ");
            sql.append(" AND sdidatarelation.paramlistid = sdidata.paramlistid ");
            sql.append(" AND sdidatarelation.paramlistversionid = sdidata.paramlistversionid ");
            sql.append(" AND sdidatarelation.variantid = sdidata.variantid ");
            sql.append(" AND sdidatarelation.dataset = sdidata.dataset ");
            sql.append(" AND sdidatarelation.relationfunction = '" + relationFunction + "'");
            if (isReagent) {
                sql.append(" left outer join trackitem on sdidatarelation.refkeyid1 = trackitem.trackitemid ");
            }
            if (paramlistid.length() > 0) {
                sql.append(" where sdidata.sdcid=").append(safeSQL.addVar(sdcid));
                sql.append(" AND sdidata.keyid1 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(keyids, ";"))).append(")");
                sql.append(" AND sdidata.paramlistid in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistid, ";"))).append(")");
                if (paramlistversionid.length() > 0) {
                    sql.append(" AND sdidata.paramlistversionid in(").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistversionid, ";"))).append(")");
                }
                if (variantid.length() > 0) {
                    sql.append(" AND sdidata.variantid in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(variantid, ";"))).append(")");
                }
                if (datasets.length() > 0) {
                    sql.append(" AND sdidata.dataset in(").append(safeSQL.addIn(ReagentUtil.getUniqueValues(datasets, ";"))).append(")");
                }
            }
        }
        if (isBoth) {
            sql.append(" UNION ALL ");
        }
        if (this.datasource.equalsIgnoreCase("sdiwirelation") || isBoth) {
            sql.append("SELECT distinct " + wicols);
            sql.append(" FROM sdiworkitemitem, sdidata,sdiworkitem  ");
            sql.append(" left outer join sdiworkitemrelation ");
            sql.append(" on sdiworkitemrelation.sdcid = sdiworkitem.sdcid");
            sql.append(" AND sdiworkitemrelation.keyid1 = sdiworkitem.keyid1");
            sql.append(" AND sdiworkitemrelation.keyid2 = sdiworkitem.keyid2 ");
            sql.append(" AND sdiworkitemrelation.keyid3 = sdiworkitem.keyid3 ");
            sql.append(" AND sdiworkitemrelation.workitemid = sdiworkitem.workitemid");
            sql.append(" AND sdiworkitemrelation.workiteminstance = sdiworkitem.workiteminstance ");
            sql.append(" AND sdiworkitemrelation.relationfunction = '" + relationFunction + "'");
            if (isReagent) {
                sql.append(" left outer join trackitem on sdiworkitemrelation.refkeyid1 = trackitem.trackitemid ");
            }
            sql.append(" where sdiworkitem.sdcid = sdiworkitemitem.sdcid");
            sql.append(" AND sdiworkitem.keyid1 = sdiworkitemitem.keyid1");
            sql.append(" AND sdiworkitem.keyid2 = sdiworkitemitem.keyid2 ");
            sql.append(" AND sdiworkitem.keyid3 = sdiworkitemitem.keyid3 ");
            sql.append(" AND sdiworkitem.workitemid = sdiworkitemitem.workitemid");
            sql.append(" AND sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance ");
            sql.append(" AND sdiworkitemitem.sdcid = sdidata.sdcid");
            sql.append(" AND sdiworkitemitem.keyid1 = sdidata.keyid1 ");
            sql.append(" AND sdiworkitemitem.keyid2 = sdidata.keyid2 ");
            sql.append(" AND sdiworkitemitem.keyid3 = sdidata.keyid3 ");
            sql.append(" AND sdiworkitemitem.itemsdcid = 'ParamList' ");
            sql.append(" AND sdiworkitemitem.itemkeyid1 = sdidata.paramlistid ");
            sql.append(" AND sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid ");
            sql.append(" AND sdiworkitemitem.itemkeyid3 = sdidata.variantid ");
            sql.append(" AND sdiworkitemitem.iteminstance = sdidata.dataset ");
            if (workitemid.length() > 0) {
                sql.append(" AND sdiworkitem.sdcid=").append(safeSQL.addVar(sdcid));
                sql.append(" AND sdiworkitem.keyid1 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(keyids, ";"))).append(")");
                sql.append(" AND sdiworkitem.workitemid  in(  ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(workitemid, ";"))).append(")");
                if (workiteminstance.length() > 0) {
                    sql.append(" AND sdiworkitem.workiteminstance in (").append(safeSQL.addIn(ReagentUtil.getUniqueValues(workiteminstance, ";"))).append(")");
                }
            } else if (paramlistid.length() > 0) {
                sql.append(" AND sdiworkitemitem.sdcid=").append(safeSQL.addVar(sdcid));
                sql.append(" AND sdiworkitemitem.keyid1 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(keyids, ";"))).append(")");
                sql.append(" AND sdiworkitemitem.itemkeyid1 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistid, ";"))).append(")");
                if (paramlistversionid.length() > 0) {
                    sql.append(" AND sdiworkitemitem.itemkeyid2 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(paramlistversionid, ";"))).append(")");
                }
                if (variantid.length() > 0) {
                    sql.append(" AND sdiworkitemitem.itemkeyid3 in( ").append(safeSQL.addIn(ReagentUtil.getUniqueValues(variantid, ";"))).append(")");
                }
                if (datasets.length() > 0) {
                    sql.append(" AND sdiworkitemitem.iteminstance in(").append(safeSQL.addIn(ReagentUtil.getUniqueValues(datasets, ";"))).append(")");
                }
            }
        }
        sql.append(" order by relationid asc");
        try {
            data = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    protected String getNotesForRequiredConsumableInstrument() {
        StringBuffer htmlData = new StringBuffer();
        if (this.mandatoryFound) {
            htmlData.append("<tr>");
            htmlData.append("<td colspan='" + this.headerColumnCount + "' style=\"" + this.css_ContentCellStyle + "\" nowrap>" + "(R) indicates required for Dataset Completion." + "</td>");
            htmlData.append("</tr>");
        }
        return htmlData.toString();
    }

    protected String getFixedPositionStyleForLeft(int margin, int zindx) {
        return "z-index: " + zindx + ";position: -webkit-sticky;position:sticky;left:" + margin + "px";
    }

    protected String getFixedPositionStyleForTop(int margin) {
        return "z-index: 2;position: -webkit-sticky;position:sticky;top:" + margin + "px";
    }

    protected void increaseFixColumnsWidth(PropertyList columnProps) {
        try {
            String columnid = this.getColumnAlias(columnProps.getProperty("columnid", ""));
            if (!this.workitemonly || columnid.equalsIgnoreCase("keyid1")) {
                this.fixColumnsTotalWidth += Integer.parseInt(columnProps.getProperty("width", "100"));
            }
        }
        catch (Exception e) {
            this.fixColumnsTotalWidth += 100;
        }
    }

    protected String getToolTip(DataSet dsReagentType, int regTypeIndex) {
        String tooltip = "";
        String sourcetype = dsReagentType.getString(regTypeIndex, "sourcetype", "");
        if (sourcetype.equalsIgnoreCase("W")) {
            String workitemid = dsReagentType.getString(regTypeIndex, "sourceworkitemid", "");
            String workitemversionid = dsReagentType.getValue(regTypeIndex, "sourceworkiteminstance", "");
            tooltip = "This is defined for the WorkItem " + workitemid + " (" + workitemversionid + ") ";
        } else {
            String paramlistid = dsReagentType.getString(regTypeIndex, "paramlistid", "");
            String paramlistversionid = dsReagentType.getValue(regTypeIndex, "paramlistversionid", "");
            String variantid = dsReagentType.getValue(regTypeIndex, "variantid", "");
            tooltip = "This is defined for the ParamList " + paramlistid + " (" + paramlistversionid + "/" + variantid + ")";
        }
        return tooltip;
    }

    protected String replaceBlankWithNullBlank(String value) {
        if (value == null || value.trim().length() == 0) {
            value = "(null)";
        }
        return value;
    }
}

