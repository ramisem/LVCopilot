/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.reagent;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CombineReagentUtil {
    private PropertyList pagedata;
    private QueryProcessor qp;
    private TranslationProcessor tp;
    private FormatUtil formatUtil;
    private char decimalSeperator;
    private PropertyListCollection rlColumnCollection;
    private PropertyListCollection tiColumnCollection;
    private String delimeterP3B = "%3B";
    private SafeSQL safeSQL = new SafeSQL();
    List excludeRLColumnlist = new ArrayList();
    List excludeTIColumnlist = new ArrayList();
    private String css_maintFormTable = "maintform_table";
    private String css_columnHeader = "gridmaint_fieldtitle gridmaint_ftmed";
    private String css_rowHeader = "maintform_fieldtitle";
    private String css_ContentCellRightOff = "border-right:0px solid #BDCCD4;background-color:white;";
    private String css_ContentCellLeftOff = "border-left:0px solid #BDCCD4;background-color:white;";
    private String css_EmptyContentCellStyle = "border:1px solid #BDCCD4;background-color: gainsboro;";
    private String css_MergeContentCellStyle = "border-right:1px solid #BDCCD4;";
    DataSet dsUnits;
    private double recomAmount;
    private String recomAmountStr;
    private String recomAmountUnit;
    private String recomAmountUnittype;
    private double usedAmount = 0.0;
    protected ConnectionInfo connectionInfo;
    protected SDCProcessor sdcProcessor;

    public CombineReagentUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request) {
        this.pagedata = pageinfo.getPropertyList("pagedata");
        this.tp = new TranslationProcessor(pageContext);
        this.formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(pageContext));
        this.decimalSeperator = this.formatUtil.getDecimalSeparator();
        this.qp = pageinfo.getQueryProcessor();
        this.tp = new TranslationProcessor(pageContext);
        this.pagedata = pageinfo.getPropertyList("pagedata");
        this.connectionInfo = pageinfo.getConnectionProcessor().getConnectionInfo(pageinfo.getConnectionId());
        this.sdcProcessor = new SDCProcessor(pageContext);
        this.rlColumnCollection = this.pagedata.getCollection("reagentlot");
        this.tiColumnCollection = this.pagedata.getCollection("trackitem");
        this.excludeRLColumnlist.add("reagentlotid");
        this.excludeTIColumnlist.add("trackitemid");
        this.excludeTIColumnlist.add("qtycurrent");
        this.excludeTIColumnlist.add("qtyunits");
        this.recomAmountStr = this.pagedata.getProperty("recomamount", "0");
        this.recomAmount = this.formatUtil.parseBigDecimal(this.recomAmountStr).doubleValue();
        this.recomAmountUnit = this.pagedata.getProperty("recomamountunit", "");
        this.recomAmountUnittype = this.pagedata.getProperty("recomamountunittype", "");
        this.setDSUnits(this.dsUnits);
    }

    private int getMaxLen(Map trackItemMap, DataSet trackitemDS) {
        Iterator it = trackItemMap.entrySet().iterator();
        int len = 0;
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String ti = (String)entry.getValue();
            String[] tiArr = StringUtil.split(ti, this.delimeterP3B);
            for (int i = 0; i < tiArr.length; ++i) {
                int indx = trackitemDS.findRow("trackitemid", tiArr[i]);
                if (indx <= -1) continue;
                String qtycurrent = trackitemDS.getValue(indx, "qtycurrent", "");
                String qtyunits = trackitemDS.getValue(indx, "qtyunits", "");
                String qtycurrenttype = trackitemDS.getValue(indx, "qtycurrenttype", "");
                String value = "(" + qtycurrent + "" + this.getDisplayUnit(qtyunits, qtycurrenttype) + ")";
                int valueLen = value.length();
                if (len >= valueLen) continue;
                len = valueLen;
            }
        }
        return len;
    }

    public String getHtml() throws SapphireException {
        StringBuffer html = new StringBuffer();
        String trackitemid = this.pagedata.getProperty("trackitemid", "");
        String reagentlotid = this.pagedata.getProperty("reagentlotid", "");
        String requiredAmount = "";
        String usedAmountStr = "";
        if (this.recomAmountStr != null && this.recomAmountStr.length() > 0) {
            requiredAmount = this.recomAmountStr + " " + this.getDisplayUnit(this.recomAmountUnit, this.recomAmountUnittype);
        }
        String[] reagenLotArr = StringUtil.split(reagentlotid, this.delimeterP3B);
        boolean allAccessibleLot = true;
        for (String rl : reagenLotArr) {
            if (ReagentUtil.hasDepartmentalSecurityAccess("LV_ReagentLot", rl, "", "", this.sdcProcessor, this.connectionInfo.getConnectionId())) continue;
            allAccessibleLot = false;
            break;
        }
        if (allAccessibleLot) {
            String paramsHtml;
            DataSet reagentLotDS = this.getReagentLotDataSet(reagentlotid);
            int reagentlotcount = reagentLotDS.size();
            DataSet trackitemDS = this.getTrackitemDataSet(trackitemid);
            Map trackItemMap = this.getReagentLotTrackItemMap(trackitemid, reagentlotid);
            Iterator it = trackItemMap.entrySet().iterator();
            int maxLen = this.getMaxLen(trackItemMap, trackitemDS);
            int tdLen = 70 + maxLen * 6;
            html.append("<div style='border-collapse:collapse;' id=dataentry_grid_container>");
            html.append("<table id='dataEntryTable' class='" + this.css_maintFormTable + "' border='0' cellpadding='2' cellspacing='0'>");
            html.append("<input type='hidden' id='reagentlotcount' name='reagentlotcount' value='" + reagentlotcount + "'>");
            html.append("<input type='hidden' id='reagenttypeid' name='reagenttypeid' value='" + reagentLotDS.getString(0, "reagenttypeid", "") + "'>");
            html.append("<input type='hidden' id='reagenttypeversionid' name='reagenttypeversionid' value='" + reagentLotDS.getString(0, "reagenttypeversionid", "") + "'>");
            int lotindx = -1;
            boolean managedInventory = true;
            boolean foundManagedContainer = false;
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                String rl = (String)entry.getKey();
                String ti = (String)entry.getValue();
                String[] tiArr = StringUtil.split(ti, this.delimeterP3B);
                html.append("<tr><td colspan=2 style='border:1px solid #B0C4DE; background-color: #CCCCCC; color: #000000; padding-left: 5px; padding-right: 5px;' nowrap>");
                html.append("<input type='hidden' id='reagentlot_" + ++lotindx + "' name='reagentlot_" + lotindx + "' value='" + rl + "'>");
                html.append("<input type='hidden' id='trackitemcount_" + lotindx + "' name='trackitemcount_" + lotindx + "' value='" + tiArr.length + "'>");
                html.append(this.getReagentLotDetailHTML(rl, reagentLotDS));
                managedInventory = reagentLotDS.getString(reagentLotDS.findRow("reagentlotid", rl), "managecontainerinventoryflag", "Y").equalsIgnoreCase("Y");
                if (!foundManagedContainer && managedInventory) {
                    foundManagedContainer = true;
                }
                html.append("</td></tr>");
                for (int i = 0; i < tiArr.length; ++i) {
                    html.append("<tr><td colspan=2 class='" + this.css_rowHeader + "'>");
                    html.append(this.getTrackItemDetailHTML(tiArr[i], trackitemDS, i, lotindx, tdLen, managedInventory));
                    html.append("</td></tr>");
                }
            }
            html.append("<tr style=\"display:" + (foundManagedContainer ? "inline" : "none") + "\"><td align='right' class='" + this.css_columnHeader + "' width='" + (tdLen - 6) + "px'>");
            html.append("<input name='foundmanagedcontainer' id='foundmanagedcontainer' type='hidden' value='" + (foundManagedContainer ? "Y" : "N") + "'>");
            html.append("<input name='totalusedamount' id='totalusedamount' type='hidden' value='" + this.usedAmount + "'>");
            html.append("<input name='totalusedamountunit' id='totalusedamountunit' type='hidden' value='" + this.recomAmountUnit + "'>");
            html.append("<input name='totalusedamountunittype' id='totalusedamountunittype' type='hidden' value='" + this.recomAmountUnittype + "'>");
            usedAmountStr = (ReagentUtil.removeLastZerosAferDecimal(this.usedAmount, this.decimalSeperator) + "").replace('.', this.decimalSeperator) + " " + this.getDisplayUnit(this.recomAmountUnit, this.recomAmountUnittype);
            html.append("Total:");
            html.append("</td><td align='left' class='" + this.css_columnHeader + "'>");
            html.append("<input class='input_field' name='totalusedamountstr' id='totalusedamountstr' style='border-style:none;' type='tex' readonly size='10' value='" + usedAmountStr + "'>");
            html.append(" ( Required Amount:&nbsp" + requiredAmount + " )");
            html.append("</td></tr>");
            html.append("</table>");
            if (reagentlotcount > 1 && (paramsHtml = this.getReagentParamsHTML(reagentlotid)).length() > 0) {
                html.append("<br>");
                html.append("<font size=2 color='blue'>As multiple Consumable Lots are being combined, you may choose the Consumable Parameter for the virtual Lot.  Either choose one value or the other or specify a value of your own.</font>");
                html.append("<br><br>");
                html.append("<table id='dataEntryTable' class='" + this.css_maintFormTable + "' border='0' cellpadding='2' cellspacing='0'>");
                html.append("<tr><td colspan=3 class='" + this.css_columnHeader + "'>");
                html.append("Consumable Parameters");
                html.append("</td></tr>");
                html.append(paramsHtml);
                html.append("</table>");
            }
            html.append("</div>");
        } else {
            html.append("<p style=\"color:red\">");
            html.append(this.tp.translate("Due to departmental security You are not allowed to access one or more selected container"));
            html.append("</p>");
            html.append("<input name='foundmanagedcontainer' id='foundmanagedcontainer' type='hidden' value='D'>");
        }
        return html.toString();
    }

    private String getReagentLotDetailHTML(String reagentlotid, DataSet reagentLotDS) {
        StringBuffer rlHtml = new StringBuffer();
        int indx = reagentLotDS.findRow("reagentlotid", reagentlotid);
        if (indx > -1) {
            rlHtml.append("<strong>" + reagentlotid + "</strong>");
            if (this.rlColumnCollection != null && this.rlColumnCollection.size() > 0) {
                for (int i = 0; i < this.rlColumnCollection.size(); ++i) {
                    PropertyList column = this.rlColumnCollection.getPropertyList(i);
                    String columnid = column.getProperty("columnid");
                    if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0 || this.excludeRLColumnlist.contains(columnid)) continue;
                    String title = column.getProperty("title", columnid);
                    String displayValue = column.getProperty("displayvalue", "");
                    boolean isTranslate = "Y".equals(column.getProperty("translatevalue"));
                    String value = reagentLotDS.getValue(indx, columnid, "");
                    String resolvedValue = this.getColumnDisplayValue(value, displayValue, isTranslate, this.tp);
                    rlHtml.append((rlHtml.length() > 0 ? "&nbsp&nbsp&nbsp&nbsp" : "") + title + ": " + "<strong>" + resolvedValue + "</strong>");
                }
            }
        }
        return rlHtml.toString();
    }

    private String getDisplayUnit(String qtyunits, String qtycurrenttype) {
        return qtyunits != null && qtyunits.length() > 0 ? qtyunits : (qtycurrenttype != null && qtycurrenttype.equalsIgnoreCase("C") ? qtycurrenttype : "");
    }

    private String getTrackItemDetailHTML(String trackitemid, DataSet trackitemDS, int tiIndx, int lotIndx, int tdLen, boolean managedInventory) throws SapphireException {
        StringBuffer tiHtml = new StringBuffer();
        int indx = trackitemDS.findRow("trackitemid", trackitemid);
        if (indx > -1) {
            String qtycurrent = trackitemDS.getValue(indx, "qtycurrent", "");
            String qtyunits = trackitemDS.getValue(indx, "qtyunits", "");
            String qtycurrenttype = trackitemDS.getValue(indx, "qtycurrenttype", "");
            String sizevalue = trackitemDS.getValue(indx, "sizevalue", "");
            String sizeunits = trackitemDS.getValue(indx, "sizeunits", "");
            String defaultUsedAmount = managedInventory ? this.getDefaultUsedAmount(trackitemDS, indx) : "0";
            String postIndx = "_" + lotIndx + "_" + tiIndx;
            tiHtml.append("<table>");
            tiHtml.append("<tr><td style='width:" + tdLen + "px'>");
            tiHtml.append("<input type='hidden' name='trackitem" + postIndx + "' id='trackitem" + postIndx + "' value='" + trackitemid + "' >");
            tiHtml.append("<input type='hidden' name='sizevalue" + postIndx + "' id='sizevalue" + postIndx + "' value='" + sizevalue + "' >");
            tiHtml.append("<input type='hidden' name='sizeunits" + postIndx + "' id='sizeunits" + postIndx + "' value='" + sizeunits + "' >");
            tiHtml.append("<input type='hidden' name='usedamount" + postIndx + "' id='usedamount" + postIndx + "' value='" + defaultUsedAmount + "' >");
            tiHtml.append("<input type='hidden' name='usedamountunit" + postIndx + "' id='usedamountunit" + postIndx + "' value='" + qtyunits + "' >");
            tiHtml.append("<input type='hidden' name='usedamountunittype" + postIndx + "' id='usedamountunittype" + postIndx + "' value='" + qtycurrenttype + "' >");
            tiHtml.append("<input type='hidden' name='availableamount" + postIndx + "' id='availableamount" + postIndx + "' value='" + qtycurrent + "' >");
            tiHtml.append("<input type='hidden' name='availableamountunit" + postIndx + "' id='availableamountunit" + postIndx + "' value='" + qtyunits + "' >");
            tiHtml.append("<input type='hidden' name='availableamountunittype" + postIndx + "' id='availableamountunittype" + postIndx + "' value='" + qtycurrenttype + "' >");
            tiHtml.append(trackitemid + (managedInventory ? " (" + qtycurrent + "" + this.getDisplayUnit(qtyunits, qtycurrenttype) + "): " : ""));
            tiHtml.append("</td><td style=\"display:" + (managedInventory ? "inline" : "none") + "\">");
            tiHtml.append("<input class='input_field' size='10' type='text' name='currusedamount" + postIndx + "' id='currusedamount" + postIndx + "' onchange=\"combineReagentUtil.checkAndSplitUnitValue(this)\" value='" + defaultUsedAmount + "'>");
            tiHtml.append("<input type='hidden' name='currusedamountunit" + postIndx + "' id='currusedamountunit" + postIndx + "' value='" + qtyunits + "'>");
            tiHtml.append("<input type='hidden' name='currusedamountunittype" + postIndx + "' id='currusedamountunittype" + postIndx + "' value='" + qtycurrenttype + "'>");
            tiHtml.append("<input name='currusedamountunitdisplay" + postIndx + "' id='currusedamountunitdisplay" + postIndx + "' style='border-style:none;background-color:#f9f9f9; color: #000000;' type='tex' readonly size='6' value='" + this.getDisplayUnit(qtyunits, qtycurrenttype) + "'>");
            tiHtml.append("</td></tr>");
            tiHtml.append("<tr><td colspan=2 nowrap>");
            boolean firstExtraField = true;
            if (this.tiColumnCollection != null && this.tiColumnCollection.size() > 0) {
                for (int i = 0; i < this.tiColumnCollection.size(); ++i) {
                    PropertyList column = this.tiColumnCollection.getPropertyList(i);
                    String columnid = column.getProperty("columnid");
                    if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0 || this.excludeTIColumnlist.contains(columnid)) continue;
                    String title = column.getProperty("title", columnid);
                    String displayValue = column.getProperty("displayvalue", "");
                    boolean isTranslate = "Y".equals(column.getProperty("translatevalue"));
                    String value = trackitemDS.getValue(indx, columnid, "");
                    String resolvedValue = this.getColumnDisplayValue(value, displayValue, isTranslate, this.tp);
                    tiHtml.append((firstExtraField ? "" : "&nbsp&nbsp&nbsp&nbsp") + title + ":&nbsp" + resolvedValue);
                    firstExtraField = false;
                }
            }
            tiHtml.append("</td></tr>");
            tiHtml.append("</table>");
        }
        return tiHtml.toString();
    }

    private String getDefaultUsedAmount(DataSet trackitemDS, int indx) throws SapphireException {
        String qtycurrent = trackitemDS.getValue(indx, "qtycurrent", "0");
        String qtyunits = trackitemDS.getValue(indx, "qtyunits", "");
        String qtyunitstype = trackitemDS.getValue(indx, "qtycurrenttype", "");
        String containerSize = trackitemDS.getValue(0, "sizevalue", "");
        String containerUnits = trackitemDS.getString(0, "sizeunits", "");
        String useAmountStr = "0";
        double qtycurrentd = this.formatUtil.parseBigDecimal(qtycurrent).doubleValue();
        if (this.recomAmountUnit.length() == 0 && this.recomAmountUnittype.length() == 0) {
            this.recomAmountUnit = qtyunits;
            this.recomAmountUnittype = qtyunitstype;
        }
        if (this.usedAmount < this.recomAmount) {
            double needMore = this.recomAmount - this.usedAmount;
            if (!qtyunits.equalsIgnoreCase(this.recomAmountUnit)) {
                double needMoreInQntyUnit = this.getConvertedValue(this.qp, this.recomAmountUnit, this.recomAmountUnittype, qtyunits, qtyunitstype, needMore, containerSize, containerUnits);
                if (needMoreInQntyUnit >= qtycurrentd) {
                    useAmountStr = qtycurrent;
                    double usedAmountInRecomUnit = this.getConvertedValue(this.qp, qtyunits, qtyunitstype, this.recomAmountUnit, this.recomAmountUnittype, qtycurrentd, containerSize, containerUnits);
                    this.usedAmount += usedAmountInRecomUnit;
                } else {
                    this.usedAmount += needMore;
                    useAmountStr = Double.toString(needMoreInQntyUnit).replace('.', this.decimalSeperator);
                }
            } else if (needMore >= qtycurrentd) {
                this.usedAmount += qtycurrentd;
                useAmountStr = qtycurrent;
            } else {
                this.usedAmount += needMore;
                useAmountStr = Double.toString(needMore).replace('.', this.decimalSeperator);
            }
        }
        return ReagentUtil.removeLastZerosAferDecimal(useAmountStr, this.decimalSeperator);
    }

    private double getConvertedValue(QueryProcessor qp, String fromUnit, String fromUnitType, String toUnit, String toUnitType, double amount, String containerSize, String containerUnits) throws SapphireException {
        double value;
        if (fromUnit == null) {
            fromUnit = "";
        }
        if (toUnit == null) {
            toUnit = "";
        }
        String amountStr = Double.toString(amount);
        if (fromUnitType.equalsIgnoreCase("C")) {
            value = UnitsUtil.convertFromContainersToUnits(qp, containerSize, containerUnits, amountStr, toUnit);
        } else if (toUnitType.equalsIgnoreCase("C")) {
            value = UnitsUtil.covertFromUnitsToContainer(qp, containerSize, containerUnits, amountStr, fromUnit);
        } else {
            String valueStr = UnitsUtil.getConvertedValue(qp, fromUnit, toUnit, amountStr);
            value = this.formatUtil.parseBigDecimal(valueStr).doubleValue();
        }
        return value;
    }

    private String getReagentParamsHTML(String reagentlotid) {
        StringBuffer htmlData = new StringBuffer();
        this.safeSQL.reset();
        StringBuffer sql = new StringBuffer("select keyid1, paramid,paramtype,enteredvalue,enteredtext,enteredunits from sdidataitem");
        sql.append(" where keyid1 in (").append(this.safeSQL.addIn(reagentlotid, this.delimeterP3B)).append(")");
        sql.append(" and datatypes not in ('NC','TC','DC','OC')");
        sql.append(" and sdcid='LV_ReagentLot'");
        DataSet ds = this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
        HashMap<String, String> hm = new HashMap<String, String>();
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getValue(i, "keyid1", "");
                String paramid = ds.getValue(i, "paramid", "");
                String paramtype = ds.getValue(i, "paramtype", "");
                String paramkey = paramid + this.delimeterP3B + paramtype;
                String enteredvalue = ds.getValue(i, "enteredvalue", "");
                String enteredunits = ds.getValue(i, "enteredunits", "");
                if (enteredvalue == null || enteredvalue.length() <= 0) continue;
                if (hm.containsKey(paramkey)) {
                    String existingValue = (String)hm.get(paramkey);
                    String[] existingValueArr = StringUtil.split(existingValue, ";");
                    String value = existingValueArr[1] + existingValueArr[2];
                    if (value.equalsIgnoreCase(enteredvalue + enteredunits)) continue;
                    hm.put(paramkey, hm.get(paramkey) + this.delimeterP3B + keyid1 + ";" + enteredvalue + ";" + enteredunits);
                    continue;
                }
                hm.put(paramkey, keyid1 + ";" + enteredvalue + ";" + enteredunits);
            }
        }
        if (hm.size() > 0) {
            int indx = 0;
            htmlData.append("<input name='paramcount' id='paramcount' style='border-style:none' type='hidden' value='" + hm.size() + "'>");
            for (Map.Entry entry : hm.entrySet()) {
                String paramkey = (String)entry.getKey();
                String[] paramkeyArr = StringUtil.split(paramkey, this.delimeterP3B);
                String paramid = paramkeyArr[0];
                String paramtype = paramkeyArr[1];
                String value = (String)entry.getValue();
                htmlData.append("<tr><td class=" + this.css_rowHeader + ">" + paramid + " (" + paramtype + ") </td>");
                htmlData.append("<input name='paramid_" + indx + "' id='paramid_" + indx + "' type=\"hidden\"  value=\"" + paramid + "\" />");
                htmlData.append("<input name='paramtype_" + indx + "' id='paramtype_" + indx + "' type=\"hidden\"  value=\"" + paramtype + "\" />");
                if (value.indexOf("%3B") > 0) {
                    htmlData.append("<td style='" + this.css_ContentCellRightOff + "' class=\"select-editable\">");
                    String[] valueArr = StringUtil.split(value, "%3B");
                    String[] valArr = StringUtil.split(valueArr[0], ";");
                    String first_amount = valArr[1];
                    String first_unit = valArr[2];
                    htmlData.append("<select onchange=\"this.nextElementSibling.value=this.value;sapphire.events.fireEvent( this.nextElementSibling, 'onchange' );\">");
                    for (int i = 0; i < valueArr.length; ++i) {
                        valArr = StringUtil.split(valueArr[i], ";");
                        String keyid1 = valArr[0];
                        String amount = valArr[1];
                        String unit = valArr[2];
                        htmlData.append("<option value='" + amount + "" + unit + "'>" + keyid1 + "(" + amount + "" + unit + ")</option>");
                    }
                    htmlData.append("</select>");
                    htmlData.append("<input name='paramvalue_" + indx + "' id='paramvalue_" + indx + "' onchange=\"combineReagentUtil.checkAndSplitUnitValueForParam(this.value," + indx + ")\" type=\"text\"  value=\"" + first_amount + "\" />");
                    htmlData.append("</td>");
                    htmlData.append("<td style='" + this.css_ContentCellLeftOff + "'>");
                    htmlData.append("<input name='paramunit_" + indx + "' id='paramunit_" + indx + "' style='border-style:none' type='text' readonly size='3' value='" + first_unit + "'>");
                    htmlData.append("</td>");
                } else {
                    String[] valArr = StringUtil.split(value, ";");
                    String amount = valArr[1];
                    String unit = valArr[2];
                    htmlData.append("<td style='" + this.css_ContentCellRightOff + "' class=\"select-editable\">");
                    htmlData.append("<input name='paramvalue_" + indx + "' id='paramvalue_" + indx + "' onchange=\"combineReagentUtil.checkAndSplitUnitValueForParam(this.value," + indx + ")\" type='text'value='" + amount + "'>");
                    htmlData.append("</td>");
                    htmlData.append("<td style='" + this.css_ContentCellLeftOff + "'>");
                    htmlData.append("<input name='paramunit_" + indx + "' id='paramunit_" + indx + "' style='border-style:none;font-size:10px' type='text' readonly size='3' value='" + unit + "'>");
                    htmlData.append("</td>");
                }
                htmlData.append("</tr>");
                ++indx;
            }
        }
        return htmlData.toString();
    }

    private Map getReagentLotTrackItemMap(String trackitemid, String reagentlotid) {
        HashMap<String, String> trackItemMap = new HashMap<String, String>();
        if (trackitemid != null && trackitemid.length() > 0) {
            String[] trackitemidArr = StringUtil.split(trackitemid, this.delimeterP3B);
            String[] reagentlotidArr = StringUtil.split(reagentlotid, this.delimeterP3B);
            for (int i = 0; i < trackitemidArr.length; ++i) {
                String RL = reagentlotidArr[i];
                if (trackItemMap.containsKey(RL)) {
                    trackItemMap.put(RL, trackItemMap.get(RL) + this.delimeterP3B + trackitemidArr[i]);
                    continue;
                }
                trackItemMap.put(RL, trackitemidArr[i]);
            }
        }
        return CombineReagentUtil.sortByValue(trackItemMap);
    }

    public static HashMap<String, String> sortByValue(HashMap<String, String> hm) {
        LinkedList<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(hm.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>(){

            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
        for (Map.Entry entry : list) {
            temp.put((String)entry.getKey(), (String)entry.getValue());
        }
        return temp;
    }

    private DataSet getReagentLotDataSet(String keyid1) {
        String selectClause = this.getSelectClause(this.rlColumnCollection);
        this.safeSQL.reset();
        StringBuffer sql = new StringBuffer("select managecontainerinventoryflag,reagentlotid,reagenttypeid,reagenttypeversionid ");
        sql.append((selectClause.length() > 0 ? "," : "") + selectClause);
        sql.append(" from reagentlot where reagentlotid in (");
        sql.append(this.safeSQL.addIn(keyid1, this.delimeterP3B));
        sql.append(")");
        return this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
    }

    private DataSet getTrackitemDataSet(String keyid1) {
        String selectClause = this.getSelectClause(this.tiColumnCollection);
        this.safeSQL.reset();
        StringBuffer sql = new StringBuffer("select trackitemid,qtycurrent,qtyunits,qtycurrenttype");
        sql.append(",trackitem.containertypeid, sizevalue, sizeunits ");
        sql.append((selectClause.length() > 0 ? "," : "") + selectClause);
        sql.append(" from trackitem ");
        sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
        sql.append(" WHERE trackitem.trackitemid in (");
        sql.append(this.safeSQL.addIn(keyid1, this.delimeterP3B));
        sql.append(")");
        return this.qp.getPreparedSqlDataSet(sql.toString(), this.safeSQL.getValues());
    }

    protected String getSelectClause(PropertyListCollection columnsCollection) {
        StringBuffer selectClause = new StringBuffer();
        if (columnsCollection != null && columnsCollection.size() > 0) {
            for (int i = 0; i < columnsCollection.size(); ++i) {
                PropertyList column = columnsCollection.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!column.getProperty("show", "Y").equals("Y") || columnid.length() <= 0) continue;
                selectClause.append(selectClause.length() > 0 ? "," : "").append(columnid);
            }
        }
        return selectClause.toString();
    }

    private String getColumnDisplayValue(String value, String displayValue, boolean isTranslate, TranslationProcessor translationProcessor) {
        value = ListColumn.sanitizeHTMLValue(value);
        if (displayValue.length() > 0) {
            value = SDITagUtil.getDisplayValue(value, displayValue);
        }
        if (isTranslate) {
            value = translationProcessor.translate(value);
        }
        return value;
    }

    public StringBuffer getToolBar(PageTagInfo pageinfo, PageContext pageContext) {
        String title = "New Virtual Lot";
        StringBuffer html = new StringBuffer();
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", title);
        toolbar.setProperty("showtitle", "Y");
        toolbar.setProperty("displaystyle", "Ribbon");
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyList btn = new PropertyList();
        btn.setProperty("id", "Save");
        btn.setProperty("buttontype", "User");
        PropertyList common = new PropertyList();
        common.setProperty("text", this.tp.translate("Combine & Return"));
        common.setProperty("image", "WEB-CORE/images/png/Save.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png32/Save.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        PropertyList user = new PropertyList();
        user.setProperty("action", "combineAndReturn()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("id", "btClose");
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        common.setProperty("text", this.tp.translate("Close"));
        common.setProperty("image", "WEB-CORE/images/png/Close.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png32/Close.png");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "doClose()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        AdvancedToolbar advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(pageContext);
        advancedToolbar.setElementid("advancedtoolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        return html;
    }

    private void setDSUnits(DataSet dsUnits) {
        dsUnits = this.qp.getSqlDataSet("SELECT DISTINCT keyid1 FROM categoryitem cat ,units un WHERE sdcid = 'Units' AND categoryid in ('MassUnits','VolumeUnits') AND cat.keyid1=un.unitsid order by keyid1");
    }
}

