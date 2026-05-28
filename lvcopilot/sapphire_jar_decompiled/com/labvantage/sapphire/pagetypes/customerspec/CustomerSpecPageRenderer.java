/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pagetypes.customerspec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CustomerSpecPageRenderer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    StringBuffer CustDetailsHtml = new StringBuffer();
    StringBuffer SpecDetailsHtml = new StringBuffer();
    public String page_Title = "";
    String plusImg = "WEB-CORE/pagetypes/list/images/plus.gif";
    String minusImg = "WEB-CORE/pagetypes/list/images/minus.gif";

    public String getHtml(PropertyList pagedata, PageContext pageContext, PageTagInfo pageinfo, String sdcid, String keyid1, String keyid2, String keyid3, String pageTitle) throws SapphireException {
        String width;
        String width2;
        PropertyList parent = pagedata.getPropertyList("parent");
        PropertyList primary = pagedata.getPropertyList("primary");
        PropertyList addressDetails = pagedata.getPropertyList("addressdetails");
        PropertyList specDetails = pagedata.getPropertyList("specdetails");
        LinkedHashSet<String> custType = new LinkedHashSet<String>();
        HashSet<String> specCondition = new HashSet<String>();
        String parentSdcId = parent.getProperty("sdcid");
        String primarySdcId = primary.getProperty("sdcid");
        StringBuffer bodyHtml = new StringBuffer();
        String rsetId = "";
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        QueryProcessor qp = pageinfo.getQueryProcessor();
        ActionProcessor ap = pageinfo.getActionProcessor();
        SDCProcessor sdp = new SDCProcessor(pageContext);
        DAMProcessor dp = new DAMProcessor(pageContext);
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String primarytableId = sdp.getProperty(primarySdcId, "tableid");
        DataSet dsPrimary = new DataSet();
        String primaryKeycolid1 = sdp.getProperty(primarySdcId, "keycolid1");
        DataSet dsRefValues = qp.getRefTypeDataSet("Spec Condition");
        dsRefValues.sort("usersequence");
        PropertyListCollection primaryColumns = primary.getCollection("columns");
        int primarycolSize = primaryColumns.size();
        String mode = pageinfo.getProperty("mode");
        String input = pageinfo.getProperty("customersinput");
        boolean parentSDIMode = false;
        if (parentSdcId.equals(sdcid)) {
            parentSDIMode = true;
        }
        if (mode.equalsIgnoreCase("dosave")) {
            try {
                CustomerSpecPageRenderer.doSave(input, pageinfo, primarySdcId, primarytableId, primaryKeycolid1, tp, parentSDIMode);
                bodyHtml.append("<div id=\"errordiv\">");
                bodyHtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\"><tbody><tr><td><table width=\"600px\" class=\"maintform_table\"><tbody><tr><td class=\"maintform_fieldtitle_blue\">").append("<img src=\"WEB-OPAL/images/minus.gif\" onclick=\"toggleErrorTable( this );\" style=\"cursor: pointer\">&nbsp;<b><strong>").append(tp.translate("Message")).append("</strong></b></td></tr><tr><td>").append("<table width=\"100%\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" id=\"__ruleErrorTable\" style=\"display: table;\">").append("<tbody><tr><td style=\"border:1px solid green;background:#c8ffc8\" valign=\"top\"><b><font color=\"green\">").append(tp.translate("Information")).append(" </font></b></td></tr>").append("<tr><td style=\"border:1px solid green\" valign=\"top\"><table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tbody><tr><td valign=\"top\" nowrap=\"\">").append("<b><font color=\"green\">1)").append(tp.translate("Save")).append("</font></b></td><td valign=\"top\">").append(tp.translate("Operation Successful")).append("</td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>").append("</td></tr></tbody></table>").append("<script language=\"javascript\">").append("function toggleErrorTable( e ) {  var errorTable = document.getElementById( '__ruleErrorTable' );   if ( errorTable.style.display == 'none' ) {  errorTable.style.display = sapphire.page.html5?'table':'block';  e.src = 'WEB-OPAL/images/minus.gif'; }").append("else {  errorTable.style.display = 'none';  e.src = 'WEB-OPAL/images/plus.gif';  }\tif( typeof( initScrollGrid ) != 'undefined' ) { initScrollGrid();}} document.getElementById( '__ruleErrorTable' ).style.display = sapphire.page.html5?'table':'block';</script>");
                bodyHtml.append("</div>");
            }
            catch (Exception e) {
                bodyHtml.append("<div id=\"errordiv\">").append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\"><tbody><tr><td><table width=\"600px\" style=\"border:1px solid black\">").append("<tbody><tr><td class=\"field_error\"><img src=\"WEB-OPAL/images/minus.gif\" onclick=\"toggleErrorTable( this );\" style=\"cursor: pointer\">&nbsp;<b><font color=\"red\"><strong>").append(tp.translate("Operation Unsuccessful")).append("</strong></font></b></td></tr><tr><td><table width=\"100%\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" id=\"__ruleErrorTable\" style=\"display: table;\">").append("<tbody><tr><td style=\"border:1px solid red;background:#FAEBD7\" valign=\"top\"><b><font color=\"red\">").append(tp.translate("Error")).append("</font></b></td></tr><tr><td style=\"border:1px solid red\" valign=\"top\">").append("<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"100%\">").append("<tbody><tr><td valign=\"top\" nowrap>").append(e.getMessage()).append("</td></tr></tbody>").append("</table>").append("</td></tr><tr><td ></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody>").append("</table><script language=\"javascript\">function toggleErrorTable( e ) {  var errorTable = document.getElementById( '__ruleErrorTable' ); if ( errorTable.style.display == 'none' ) { errorTable.style.display = sapphire.page.html5?'table':'block';  e.src = 'WEB-OPAL/images/minus.gif'; } else { errorTable.style.display = 'none'; e.src = 'WEB-OPAL/images/plus.gif'; }").append("if( typeof( initScrollGrid ) != 'undefined' ) {initScrollGrid();}} document.getElementById( '__ruleErrorTable' ).style.display = sapphire.page.html5?'table':'block';</script>").append("</div>");
            }
        }
        if (parentSdcId.equals(sdcid)) {
            parentSDIMode = true;
            String parentTable = sdp.getProperty(parentSdcId, "tableid");
            String parentkeycolid1 = sdp.getProperty(parentSdcId, "keycolid1");
            String parentkeycolid2 = sdp.getProperty(parentSdcId, "keycolid2");
            String parentkeycolid3 = sdp.getProperty(parentSdcId, "keycolid3");
            PropertyListCollection parentColumns = parent.getCollection("columns");
            int colSize = parentColumns.size();
            sql.append("SELECT ").append(parentTable).append(".*");
            for (int i = 0; i < colSize; ++i) {
                PropertyList column = parentColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!columnid.startsWith("(")) continue;
                sql.append(",").append(columnid).append(",");
            }
            if (sql.toString().endsWith(",")) {
                sql.setLength(sql.length() - 1);
            }
            sql.append(" FROM ").append(parentTable).append(" WHERE ").append(parentkeycolid1).append(" = ").append(safeSQL.addVar(keyid1));
            if (parentkeycolid2.length() > 0) {
                sql.append(" ").append(parentkeycolid2).append(" = ").append(safeSQL.addVar(keyid2));
            }
            if (parentkeycolid3.length() > 0) {
                sql.append(" ").append(parentkeycolid3).append(" = ").append(safeSQL.addVar(keyid3));
            }
            DataSet dsParent = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT sdccolumnid, sdccolumnid2, sdccolumnid3 FROM sdclink WHERE  sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND linksdcid = ").append(safeSQL.addVar(parentSdcId));
            DataSet dsLink = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            String sdccolumnid = dsLink.getValue(0, "sdccolumnid");
            String sdccolumnid2 = dsLink.getValue(0, "sdccolumnid2");
            String sdccolumnid3 = dsLink.getValue(0, "sdccolumnid3");
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT DISTINCT ");
            for (int i = 0; i < primarycolSize; ++i) {
                PropertyList column = primaryColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                sql.append(" p." + columnid);
                if (i >= primarycolSize - 1) continue;
                sql.append(",");
            }
            sql.append(" FROM ").append(primarytableId).append(" p, sdidataitem ").append(" WHERE p.").append(sdccolumnid).append(" = ").append(safeSQL.addVar(keyid1)).append(" AND sdidataitem.sdcid = '" + primarySdcId + "' AND sdidataitem.keyid1 = ").append("p." + primaryKeycolid1);
            if (sdccolumnid2.length() > 0) {
                sql.append(" ").append("p." + sdccolumnid2).append(" = ").append(safeSQL.addVar(keyid2));
            }
            if (sdccolumnid3.length() > 0) {
                sql.append(" ").append("p." + sdccolumnid3).append(" = ").append(safeSQL.addVar(keyid3));
            }
            sql.append(" ORDER BY ").append("1");
            dsPrimary = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (dsPrimary.getRowCount() == 0) {
                throw new SapphireException(tp.translate("No Sample with dataitem found in the ") + sdp.getProperty(parentSdcId, "singular") + ".");
            }
            keyid1 = dsPrimary.getColumnValues(primaryKeycolid1, ";");
            rsetId = dp.createRSet(primarySdcId, keyid1, null, null);
            this.page_Title = pageTitle = CustomerSpecPageRenderer.resolveTitle(pageTitle, dsParent);
            bodyHtml.append(this.getParentDetail(parentColumns, dsParent, dsRefValues, tp));
        } else if (primarySdcId.equals(sdcid)) {
            safeSQL.reset();
            rsetId = dp.createRSet(primarySdcId, keyid1, null, null);
            sql.setLength(0);
            sql.append("SELECT ").append(primarytableId).append(".*");
            for (int i = 0; i < primarycolSize; ++i) {
                PropertyList column = primaryColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!columnid.startsWith("(")) continue;
                sql.append(",").append(columnid).append(",");
            }
            if (sql.toString().endsWith(",")) {
                sql.setLength(sql.length() - 1);
            }
            sql.append(" FROM ").append(primarytableId).append(" , rsetitems r ").append(" WHERE r.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND ").append(primarytableId).append(".").append(primaryKeycolid1).append(" = r.keyid1").append(" ORDER BY ").append(primaryKeycolid1);
            dsPrimary = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.page_Title = pageTitle = CustomerSpecPageRenderer.resolveTitle(pageTitle, dsPrimary);
            bodyHtml.append(this.getParentDetail(primaryColumns, dsPrimary, dsRefValues, tp));
        }
        sql.setLength(0);
        safeSQL.reset();
        sql.append("SELECT address.addressid, address.addresstype, address.addressdesc, address.state, 'Primary' custtype, spec.specid, spec.specversionid, sdispec.sdcid, sdispec.keyid1, sdispec.appliedflag,  coalesce( nullif( sdispec.condition, ''), 'null') condition, sdispec.usersequence ").append(" FROM address, spec, sdispec, rsetitems r ").append(" WHERE spec.specid = sdispec.specid AND spec.specversionid = sdispec.specversionid AND spec.specusetype = ").append(safeSQL.addVar("Customer")).append(" AND address.addressid = spec.specuseaddressid and address.addresstype = spec.specuseaddresstype ").append(" AND spec.specuseaddresstype = ").append(safeSQL.addVar("Customer")).append(" AND sdispec.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND sdispec.keyid1 = r.keyid1 AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" UNION").append(" SELECT address.addressid, address.addresstype, address.addressdesc, address.state, 'Secondary' custtype, spec.specid, spec.specversionid, sdispec.sdcid, sdispec.keyid1, sdispec.appliedflag, coalesce( nullif( sdispec.condition, ''), 'null') condition, sdispec.usersequence ").append(" FROM address, spec, sdispec, sdiaddress, rsetitems r ").append(" WHERE address.addressid = sdiaddress.addressid AND address.addresstype = sdiaddress.addresstype ").append(" AND sdiaddress.sdcid = ").append(safeSQL.addVar("SpecSDC")).append(" AND sdiaddress.keyid1 = sdispec.specid AND sdiaddress.keyid2 = sdispec.specversionid").append(" AND sdiaddress.addresstype = ").append(safeSQL.addVar("Customer")).append(" AND sdispec.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND sdispec.keyid1 = r.keyid1 AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND spec.specid = sdispec.specid AND spec.specversionid = sdispec.specversionid AND spec.specusetype = ").append(safeSQL.addVar("Customer")).append(" ORDER BY 1");
        DataSet dsSampleSpecs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        dsSampleSpecs.getRowCount();
        sql.setLength(0);
        safeSQL.reset();
        sql.append("SELECT s.* from sdiaddress s, rsetitems r WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 AND r.rsetid = ").append(safeSQL.addVar(rsetId));
        DataSet dsSampleSDIAddresses = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("condition", "null");
        if (dsSampleSpecs.getRowCount() > 0) {
            dsSampleSpecs.sort("condition");
            DataSet dsEvaluateSpec = dsSampleSpecs.getFilteredDataSet(filter);
            if (dsEvaluateSpec.getRowCount() > 0) {
                DataSet actionProps = new DataSet();
                for (int i = 0; i < dsEvaluateSpec.getRowCount(); ++i) {
                    String keyId1 = dsEvaluateSpec.getValue(i, "keyid1");
                    String specId = dsEvaluateSpec.getValue(i, "specid");
                    String specVersionId = dsEvaluateSpec.getValue(i, "specversionid");
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("keyid1", keyId1);
                    find.put("specid", specId);
                    find.put("specversionid", specVersionId);
                    if (actionProps.findRow(find) >= 0) continue;
                    actionProps.copyRow(dsEvaluateSpec, i, 1);
                }
                PropertyList props = new PropertyList();
                actionProps.sort("keyid1");
                ArrayList<DataSet> keyid1Grps = actionProps.getGroupedDataSets("keyid1");
                for (int k = 0; k < keyid1Grps.size(); ++k) {
                    DataSet keyGroup = keyid1Grps.get(k);
                    props.setProperty("sdcid", primarySdcId);
                    props.setProperty("keyid1", keyGroup.getValue(0, "keyid1"));
                    props.setProperty("specid", keyGroup.getColumnValues("specid", ";"));
                    props.setProperty("specversionid", keyGroup.getColumnValues("specversionid", ";"));
                    ap.processAction("CheckSpecs", "1", props);
                    String outkeyid1 = props.getProperty("outkeyid1");
                    String outSpecId = props.getProperty("outspecid");
                    String outSpecVersionId = props.getProperty("outspecversionid");
                    String outCondition = props.getProperty("outcondition");
                    DataSet dsOutSpec = new DataSet();
                    dsOutSpec.addColumnValues("keyid1", 0, outkeyid1, ";");
                    dsOutSpec.addColumnValues("specid", 0, outSpecId, ";");
                    dsOutSpec.addColumnValues("specversionid", 0, outSpecVersionId, ";");
                    dsOutSpec.addColumnValues("condition", 0, outCondition, ";");
                    for (int i = 0; i < dsOutSpec.getRowCount(); ++i) {
                        HashMap<String, String> find = new HashMap<String, String>();
                        find.put("keyid1", dsOutSpec.getValue(i, "keyid1"));
                        find.put("specid", dsOutSpec.getValue(i, "specid"));
                        find.put("specversionid", dsOutSpec.getValue(i, "specversionid"));
                        DataSet dsSpecRows = dsEvaluateSpec.getFilteredDataSet(find);
                        if (dsSpecRows.getRowCount() <= 0) continue;
                        dsSpecRows.setValue(-1, "condition", dsOutSpec.getValue(i, "condition"));
                    }
                    props.clear();
                }
            }
        }
        DataSet specData = new DataSet();
        DataSet custDetails = new DataSet();
        StringBuffer jsonVar = new StringBuffer();
        jsonVar.append("\n var _jsonObj = {preferredcustomers:[], rejectedcustomers:[], speccustomers:[");
        StringBuffer jsonSpecSamples = new StringBuffer();
        StringBuffer specCustGridHtml = new StringBuffer();
        specCustGridHtml.append("<table width=\"99%\" align=\"center\"><tr><td><table id=\"speccustomersgrid\" class=\"gridmaint_table\" cellpadding=\"3\" cellspacing=\"0\"   align=\"left\" border=1>").append("<tbody><tr><td  class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0;").append(parentSDIMode ? "padding-left:16px" : "").append("\" width=26% nowrap>").append(tp.translate("Customer")).append("</td>").append("<td class=\"maintform_fieldtitle\"  style=\"background-color:#F0F0F0\" width=26% nowrap>").append(tp.translate("Specifications")).append("</td>").append("<td class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0\" width=10%  nowrap>").append(tp.translate("Condition")).append("</td>").append("<td  class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0\" width=12%  nowrap>").append(tp.translate("Choose Customer")).append("</td>").append("</tr>");
        dsSampleSpecs.sort("specid,specversionid");
        boolean showSecondary = false;
        ArrayList<DataSet> specCustomers = dsSampleSpecs.getGroupedDataSets("specid,specversionid");
        if ("Y".equalsIgnoreCase(pageinfo.getProperty("secondarycustomer"))) {
            showSecondary = true;
        }
        String selectedConditions = pageinfo.getProperty("selectedconditions");
        String[] selectedConds = StringUtil.split(selectedConditions, ",");
        String[] expandedSpecs = StringUtil.split(pageinfo.getProperty("expandedspecs"), ";");
        for (int i = 0; i < specCustomers.size(); ++i) {
            DataSet dsSpec = specCustomers.get(i);
            String specid = dsSpec.getValue(0, "specid");
            String specVersionid = dsSpec.getValue(0, "specversionid");
            String imageId = specid + "" + specVersionid;
            String imageSpec = specid + "|" + specVersionid;
            boolean expanded = false;
            DataSet dsSpecDetail = qp.getPreparedSqlDataSet("SELECT * from spec where specid = ? AND specversionid = ?", (Object[])new String[]{specid, specVersionid});
            String specDesc = dsSpecDetail.getValue(0, "specdesc");
            specData.copyRow(dsSpecDetail, -1, 1);
            String oos = dsSpec.getValue(0, "oosgeneratingflag");
            String condition = dsSpec.getValue(0, "condition");
            specCondition.add(condition);
            String keyId1 = dsSpec.getValue(0, "keyid1");
            boolean displayCondition = true;
            if (selectedConditions.length() > 0) {
                displayCondition = false;
                for (int se = 0; se < selectedConds.length; ++se) {
                    if (!condition.equalsIgnoreCase(selectedConds[se])) continue;
                    displayCondition = true;
                    break;
                }
            }
            if (!parentSDIMode) {
                jsonSpecSamples.append("\"Samples\":[");
                jsonSpecSamples.append("\"").append(keyId1).append("\",");
            }
            int refIndex = this.getRefDisplayIndex(parentSDIMode, condition, dsSpec, dsRefValues);
            condition = dsRefValues.getValue(refIndex, "refvalueid");
            String refDispIcon = dsRefValues.getValue(refIndex, "refdisplayicon");
            boolean applied = "Y".equalsIgnoreCase(dsSpec.getValue(0, "appliedflag"));
            if (i > 0) {
                jsonVar.append(",");
            }
            jsonVar.append("{\"spec\":\"").append(specid + ";" + specVersionid).append("\", \"customers\":[");
            dsSpec.sort("custtype,addressid,addresstype");
            ArrayList<DataSet> custGroups = dsSpec.getGroupedDataSets("custtype,addressid,addresstype");
            for (int g = 0; g < custGroups.size(); ++g) {
                DataSet custGrp = custGroups.get(g);
                String addressid = custGrp.getValue(0, "addressid");
                String addresstype = custGrp.getValue(0, "addresstype");
                String addressdesc = custGrp.getValue(0, "addressdesc", addressid);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("addressid", addressid);
                findMap.put("addresstype", addresstype);
                if (custDetails.findRow(findMap) < 0) {
                    DataSet dsAddress = qp.getPreparedSqlDataSet("SELECT * from address where addressid = ? AND addresstype = ?", (Object[])new String[]{addressid, addresstype});
                    custDetails.copyRow(dsAddress, -1, 1);
                }
                boolean preferred = false;
                boolean sdiaddressExists = false;
                findMap.put("sdcid", primarySdcId);
                findMap.put("keyid1", keyId1);
                specCustGridHtml.append("<tr  id=\"tr_" + keyId1 + ";" + specid + ";" + specVersionid + "\" condition=\"" + condition + "\" customertype=\"").append(g == 0 ? "Primary" : "Secondary").append("\"").append(" style=\"display:").append(!displayCondition || g > 0 && !showSecondary ? "none" : "table-row").append("\" >");
                String href = "<a href=\"javascript:displayMultiSampleData('" + primarySdcId + "','" + specid + specVersionid + "')\">";
                for (int ex = 0; ex < expandedSpecs.length; ++ex) {
                    if (!expandedSpecs[ex].equals(imageSpec)) continue;
                    expanded = true;
                    break;
                }
                if (g == 0) {
                    custType.add("Primary");
                    findMap.put("contactfunction", "CustomerSpec");
                    if (dsSampleSDIAddresses.findRow(findMap) > -1) {
                        sdiaddressExists = true;
                    }
                    if (sdiaddressExists && applied) {
                        preferred = true;
                    }
                    jsonVar.append("\"").append(addressid + ";" + addresstype + ";CustomerSpec;").append(preferred ? "Y" : "N").append("\",");
                    String imgHtml = parentSDIMode ? "<img src=\"" + (expanded ? this.minusImg : this.plusImg) + "\" id=\"" + imageId + "\" spec=\"" + imageSpec + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" alt=\"" + tp.translate(expanded ? "collapse" : "Expand") + "\" onclick=\"expandDetail( this )\">&nbsp;&nbsp;" : "";
                    specCustGridHtml.append("<td class=\"maintform_field\" nowrap title=\"").append(addressdesc).append("\" >").append(imgHtml).append(addressid).append("</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" nowrap title=\"").append(specDesc).append("\" >").append(specid + "(" + specVersionid + ")").append("</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" align=\"center\" nowrap>&nbsp;").append(refDispIcon.length() > 0 ? href + "<img src=\"" + refDispIcon + "\" title=\"" + condition + "\"/></a>" : condition).append("</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" align=\"center\" nowrap>").append("<input name=\"preferckbox\" id=").append(i).append(g).append("\" type=\"checkbox\" ").append(preferred ? "checked" : "").append(" onclick=\"addToArray(this, this.checked, " + preferred + ")\" value=\"").append(keyId1 + ";" + specid + ";" + specVersionid + ";" + oos + ";" + addressid + ";" + addresstype + ";CustomerSpec").append("\">").append("</td>");
                } else {
                    custType.add("Secondary");
                    findMap.put("contactfunction", "CustomerSpecSecondary");
                    if (dsSampleSDIAddresses.findRow(findMap) > -1) {
                        sdiaddressExists = true;
                    }
                    if (sdiaddressExists && applied) {
                        preferred = true;
                    }
                    jsonVar.append("\"").append(addressid + ";" + addresstype + ";CustomerSpecSecondary;").append(preferred ? "Y" : "N").append("\",");
                    specCustGridHtml.append("<td class=\"maintform_field\" style=\"padding-left:16px\" nowrap title=\"").append(addressdesc).append("\" >&emsp;(").append(addressid).append(")</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" nowrap style=\"font-style: italic;font-size:font-size:5.5pt\">&emsp;").append(specid + "(" + specVersionid + ")").append("</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" align=\"center\" nowrap>&nbsp;").append(refDispIcon.length() > 0 ? href + "<img src=\"" + refDispIcon + "\" title=\"" + condition + "\"/></a>" : condition).append("</td>");
                    specCustGridHtml.append("<td class=\"maintform_field\" align=\"center\" nowrap>").append("<input name=\"preferckbox\" id=").append(i).append(g).append("\" type=\"checkbox\" ").append(preferred ? "checked" : "").append(" onclick=\"addToArray(this, this.checked, " + preferred + ")\" value=\"").append(keyId1 + ";" + specid + ";" + specVersionid + ";" + oos + ";" + addressid + ";" + addresstype + ";CustomerSpecSecondary").append("\">").append("</td>");
                }
                specCustGridHtml.append("</tr>");
            }
            if (parentSDIMode) {
                jsonSpecSamples.append("\"Samples\":[");
                dsSpec.sort("keyid1");
                ArrayList<DataSet> sampleGroups = dsSpec.getGroupedDataSets("keyid1");
                specCustGridHtml.append("<tr condition=\"" + condition + "\" customertype=\"Primary\"><td colspan=5 style=\"padding:0px\">");
                int tableWidth = primarycolSize * 100 + 70;
                specCustGridHtml.append("<div id=\"div" + imageId + "\" style=\"display:").append(expanded ? "block" : "none").append(";padding-left:30px;padding-right:3px;padding-top:1px;padding-bottom:1px\"><table width=\"").append(tableWidth).append("px\" cellpadding=\"3\" cellspacing=\"0\" style=\"table-layout:fixed\" >").append("<tbody>");
                specCustGridHtml.append("<tr condition=\"" + condition + "\" customertype=\"Primary\" style=\"background-color: #FFF5EE\">");
                for (int col = 0; col < primarycolSize; ++col) {
                    PropertyList column = primaryColumns.getPropertyList(col);
                    String columnid = column.getProperty("columnid");
                    String title = column.getProperty("title", columnid);
                    if (columnid.startsWith("(")) {
                        columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                        column.setProperty("columnid", columnid);
                        if (title.startsWith("(")) {
                            title = columnid;
                        }
                    }
                    specCustGridHtml.append("<td width=\"100px\" class=\"cell_top_border\" >").append(title).append("</td>");
                }
                specCustGridHtml.append("<td width=\"65px\" class=\"cell_top_right_border\" >").append(tp.translate("Condition")).append("</td>");
                specCustGridHtml.append("</tr>");
                for (int g = 0; g < sampleGroups.size(); ++g) {
                    DataSet dsSamples = sampleGroups.get(g);
                    keyId1 = dsSamples.getValue(0, "keyid1");
                    jsonSpecSamples.append("\"").append(keyId1).append("\",");
                    String sampleCondition = dsSamples.getValue(0, "condition");
                    specCustGridHtml.append("<tr condition=\"" + condition + "\" customertype=\"Primary\">");
                    for (int col = 0; col < primarycolSize; ++col) {
                        PropertyList column = primaryColumns.getPropertyList(col);
                        String columnid = column.getProperty("columnid");
                        String dispValue = column.getProperty("displayvalue");
                        int sRow = dsPrimary.findRow(primaryKeycolid1, keyId1);
                        String colValue = "";
                        if (sRow > -1) {
                            colValue = dsPrimary.getValue(sRow, columnid);
                            if (dispValue.trim().length() > 0) {
                                colValue = CustomerSpecPageRenderer.resolveDisplayValue(dispValue, colValue);
                            }
                        }
                        specCustGridHtml.append("<td class=\"cell_border\">").append(colValue).append("</td>");
                    }
                    int f = dsRefValues.findRow("refvalueid", sampleCondition);
                    refDispIcon = f > -1 ? dsRefValues.getValue(f, "refdisplayicon") : "";
                    specCustGridHtml.append("<td class=\"cell_right_border\" align=\"center\">");
                    if (refDispIcon.length() > 0) {
                        specCustGridHtml.append("<a href=\"javascript:displaySampleData('" + primarySdcId + "','" + keyId1 + "','" + specid + "','" + specVersionid + "')\" title = \"" + tp.translate("View Data Details") + "\"> <img src=\"" + refDispIcon + "\" title=\"" + sampleCondition + "\" /></a>");
                    } else {
                        specCustGridHtml.append(sampleCondition);
                    }
                    specCustGridHtml.append("</td></tr>");
                }
                specCustGridHtml.append("</tbody></table></div></td></tr>");
            }
            if (jsonVar.toString().endsWith(",")) {
                jsonVar.setLength(jsonVar.length() - 1);
            }
            if (jsonSpecSamples.toString().endsWith(",")) {
                jsonSpecSamples.setLength(jsonSpecSamples.length() - 1);
            }
            jsonSpecSamples.append("]");
            jsonVar.append("]").append("," + jsonSpecSamples.toString());
            jsonSpecSamples.setLength(0);
            jsonVar.append("}");
        }
        jsonVar.append("]}");
        specCustGridHtml.append("</tbody></table>");
        specCustGridHtml.append("\n<script>");
        specCustGridHtml.append("\nvar plusImg =\"" + this.plusImg + "\";");
        specCustGridHtml.append("\nvar minusImg =\"" + this.minusImg + "\";");
        specCustGridHtml.append("\nvar expandedSpecifications = new Array()");
        for (int ex = 0; ex < expandedSpecs.length; ++ex) {
            if (expandedSpecs[ex].length() <= 0) continue;
            specCustGridHtml.append("\nexpandedSpecifications.push('" + expandedSpecs[ex] + "')");
        }
        specCustGridHtml.append(jsonVar).append("\n</script>");
        StringBuffer filterHtml = new StringBuffer();
        filterHtml.append("<br><table width=\"99%\" border=0 align=center>");
        filterHtml.append("<tr><td>");
        filterHtml.append("<table width=\"auto\">");
        String checked = "";
        if (showSecondary) {
            checked = "checked";
        }
        filterHtml.append("<td style=\"color: #000000; font-weight:600;font-family:font-size: 10px;\" align=right>").append(tp.translate("Show Secondary Customers")).append("</td>");
        filterHtml.append("<td><input type=\"checkbox\" id=\"customertype\" name=\"customertype\" name=\"customertype\" value=\"S\" ").append(checked).append(" onchange=\"doFilter();\" ></td>");
        filterHtml.append("</tr>");
        filterHtml.append("<tr>");
        filterHtml.append("<td style=\"color: #000000; font-weight:600;font-family:font-size: 12px;\">").append(tp.translate("Filter By Conditions")).append(" </td>");
        filterHtml.append("<td>");
        checked = "checked";
        if (selectedConditions.length() > 0 && selectedConds.length > 0) {
            checked = "";
        }
        filterHtml.append("<input type=\"checkbox\" id=\"allcondition\" name=\"allcondition\" value=\"All\" ").append(checked).append(" onClick=\"checkAllCondition(this);\">").append(tp.translate("All")).append("&nbsp;&nbsp;");
        for (int i = 0; i < dsRefValues.getRowCount(); ++i) {
            String refvalueId = dsRefValues.getValue(i, "refvalueid");
            checked = "checked";
            if (selectedConditions.length() > 0 && selectedConds.length > 0) {
                checked = "";
                for (int se = 0; se < selectedConds.length; ++se) {
                    if (!refvalueId.equalsIgnoreCase(selectedConds[se])) continue;
                    checked = "checked";
                    break;
                }
            }
            filterHtml.append("<input type=\"checkbox\" id=\"condition\" name=\"condition\" value=\"").append(SafeHTML.encodeForHTMLAttribute(refvalueId)).append("\" ").append(checked).append(" onClick=\"doFilter();\">").append(SafeHTML.encodeForHTMLAttribute(tp.translate(refvalueId))).append("&nbsp;&nbsp;");
        }
        filterHtml.append("</td></tr>");
        filterHtml.append("</table>");
        filterHtml.append("</td></tr></table>");
        bodyHtml.append(filterHtml.toString());
        bodyHtml.append(specCustGridHtml.toString());
        if (!parentSDIMode) {
            Button buttonAdd = new Button(pageContext);
            buttonAdd.setModern(true);
            buttonAdd.setWidth("80");
            buttonAdd.setMargin("thin");
            buttonAdd.setImg("WEB-CORE/images/gif/AddRow.gif");
            buttonAdd.setAction("addSpec()");
            buttonAdd.setText(tp.translate("View Additional Specs"));
            bodyHtml.append("<table cellpadding=1 cellspacing=1 width=99% border=0  align=left>").append("<tr><td width=\"30%\">").append("<table cellpadding=0 border=0 cellspacing=0>").append("<tr><td style=\"font-size:5pt\">").append(buttonAdd.getHtml());
            bodyHtml.append("</td><td width=8>&nbsp;</td>").append("</tr>").append("</table>").append("</td>").append("<td width=\"70%\" align=\"right\">&nbsp;").append("</td></tr>").append("</table>");
        }
        PropertyListCollection addressColumns = addressDetails.getCollection("columns");
        int colSize = addressColumns.size();
        this.CustDetailsHtml.append("<table cellspacing=0 cellpadding=3 border=0><tbody><tr >");
        for (int i = 0; i < colSize; ++i) {
            PropertyList column = addressColumns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String title = column.getProperty("title", columnid);
            width2 = column.getProperty("width", "130");
            this.CustDetailsHtml.append("<td class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0\" width=\"").append(width2).append("\"  height=\"26\" nowrap>").append(tp.translate(title)).append("</td>");
        }
        this.CustDetailsHtml.append("</tr>");
        for (int c = 0; c < custDetails.getRowCount(); ++c) {
            this.CustDetailsHtml.append("<tr>");
            for (int i = 0; i < colSize; ++i) {
                PropertyList column = addressColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                width2 = column.getProperty("width", "130");
                String alignment = column.getProperty("alignment", "left");
                String dispValue = column.getProperty("displayvalue");
                String colValue = custDetails.getValue(c, columnid);
                if (dispValue.trim().length() > 0) {
                    colValue = CustomerSpecPageRenderer.resolveDisplayValue(dispValue, colValue);
                }
                this.CustDetailsHtml.append("<td class=\"maintform_field\" width=\"").append(width2).append("\" align=\"").append(alignment).append("\" height=\"26\">").append(colValue).append("</td>");
            }
            this.CustDetailsHtml.append("</tr>");
        }
        this.CustDetailsHtml.append("</table>");
        PropertyListCollection specColumns = specDetails.getCollection("columns");
        int speccolSize = specColumns.size();
        this.SpecDetailsHtml.append("<table cellspacing=0 cellpadding=3 border=0><tbody><tr >");
        for (int i = 0; i < speccolSize; ++i) {
            PropertyList column = specColumns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String title = column.getProperty("title", columnid);
            width = column.getProperty("width", "130");
            this.SpecDetailsHtml.append("<td class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0\" width=\"").append(width).append("\"  height=\"26\" nowrap>").append(tp.translate(title)).append("</td>");
        }
        this.SpecDetailsHtml.append("</tr>");
        for (int c = 0; c < specData.getRowCount(); ++c) {
            this.SpecDetailsHtml.append("<tr>");
            for (int i = 0; i < speccolSize; ++i) {
                PropertyList column = specColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                width = column.getProperty("width", "130");
                String alignment = column.getProperty("alignment", "left");
                String dispValue = column.getProperty("displayvalue");
                String colValue = specData.getValue(c, columnid);
                if (dispValue.trim().length() > 0) {
                    colValue = CustomerSpecPageRenderer.resolveDisplayValue(dispValue, colValue);
                }
                this.SpecDetailsHtml.append("<td class=\"maintform_field\" width=\"").append(width).append("\" align=\"").append(alignment).append("\" height=\"26\">").append(colValue).append("</td>");
            }
            this.SpecDetailsHtml.append("</tr>");
        }
        this.SpecDetailsHtml.append("</table>");
        bodyHtml.append("</td></tr></table>");
        return bodyHtml.toString();
    }

    public String getCustomers() {
        return this.CustDetailsHtml.toString();
    }

    public String getSpecs() {
        return this.SpecDetailsHtml.toString();
    }

    private String getParentDetail(PropertyListCollection columns, DataSet ds, DataSet dsRefValues, TranslationProcessor tp) {
        StringBuffer bodyHtml = new StringBuffer();
        bodyHtml.append("<br>");
        bodyHtml.append("<table width=\"99%\" align=\"center\" border=0><tr><td> <table class=\"gridmaint_table\" cellpadding=\"3\" cellspacing=\"0\" style=\"width:auto;display:table;\" align=\"left\">");
        bodyHtml.append("<tbody>");
        int colSize = columns.size();
        for (int i = 0; i < colSize; ++i) {
            if (i % 2 == 0) {
                bodyHtml.append("<tr>");
            }
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String title = column.getProperty("title", columnid);
            String dispValue = column.getProperty("displayvalue");
            if (columnid.startsWith("(")) {
                columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                if (title.startsWith("(")) {
                    title = columnid;
                }
            }
            int colspan = 1;
            if (i == colSize - 1) {
                colspan = 3;
            }
            bodyHtml.append("<td  class=\"maintform_fieldtitle\" width=15% style=\"background-color:#F0F0F0\" nowrap>").append(tp.translate(title)).append("</td>");
            bodyHtml.append("<td  class=\"maintform_field\" width=35% nowrap colspan=").append(colspan).append(" >");
            if (columnid.contains("condition")) {
                String condition = ds.getValue(0, columnid);
                int f = dsRefValues.findRow("refvalueid", condition);
                String refDispIcon = "";
                if (f > -1) {
                    refDispIcon = dsRefValues.getValue(f, "refdisplayicon");
                }
                if (refDispIcon.length() > 0) {
                    bodyHtml.append("<img src=\"" + refDispIcon + "\" title=\"" + condition + "\"/>");
                } else {
                    bodyHtml.append(condition);
                }
            } else {
                String colValue = ds.getValue(0, columnid);
                if (dispValue.trim().length() > 0) {
                    colValue = CustomerSpecPageRenderer.resolveDisplayValue(dispValue, colValue);
                }
                bodyHtml.append(SafeHTML.encodeForHTMLAttribute(colValue));
            }
            bodyHtml.append("</td>");
            if (i % 2 > 0) {
                bodyHtml.append("</tr>");
                continue;
            }
            if (i != colSize - 1) continue;
            bodyHtml.append("</tr>");
        }
        bodyHtml.append("</tbody>");
        bodyHtml.append("</table>");
        bodyHtml.append("</td></tr></table>");
        return bodyHtml.toString();
    }

    private static String resolveTitle(String pageTitle, DataSet ds) {
        if (ds != null && ds.getRowCount() > 0) {
            String[] dsCols = ds.getColumns();
            for (int col = 0; col < dsCols.length; ++col) {
                if (!pageTitle.contains("[" + dsCols[col] + "]")) continue;
                pageTitle = StringUtil.replaceAll(pageTitle, "[" + dsCols[col] + "]", ds.getValue(0, dsCols[col]));
            }
        }
        return pageTitle;
    }

    public static void doSave(String jsonStr, PageTagInfo pageinfo, String primarySdcid, String primaryTableid, String primaryKeycolid1, TranslationProcessor tp, boolean parentSDIMode) throws SapphireException {
        try {
            int r;
            String addressid;
            Iterator itr;
            String auditReason = pageinfo.getProperty("auditreason");
            String auditActivity = pageinfo.getProperty("auditactivity");
            String auditSignedFlag = pageinfo.getProperty("auditsignedflag");
            QueryProcessor qp = pageinfo.getQueryProcessor();
            ActionProcessor ap = pageinfo.getActionProcessor();
            PropertyList actionProps = new PropertyList();
            JSONObject json = new JSONObject(jsonStr);
            JSONObject preferred = json.getJSONObject("preferredcustomers");
            JSONObject rejected = json.getJSONObject("rejectedcustomers");
            JSONObject specCustomers = json.getJSONObject("speccustomers");
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            DataSet dsCustomerSpec = new DataSet();
            dsCustomerSpec.addColumn("specid", 0);
            dsCustomerSpec.addColumn("specversionid", 0);
            dsCustomerSpec.addColumn("addressid", 0);
            dsCustomerSpec.addColumn("addresstype", 0);
            dsCustomerSpec.addColumn("contactfunction", 0);
            dsCustomerSpec.addColumn("preferred", 0);
            DataSet dsSampleSpec = new DataSet();
            dsSampleSpec.addColumn("specid", 0);
            dsSampleSpec.addColumn("specversionid", 0);
            dsSampleSpec.addColumn("sampleid", 0);
            if (specCustomers != null && (itr = specCustomers.keys()).hasNext()) {
                while (itr.hasNext()) {
                    JSONObject samples;
                    Iterator sampleitr;
                    JSONObject nestedjsonObj = specCustomers.getJSONObject((String)itr.next());
                    String spec = (String)nestedjsonObj.get("spec");
                    String specId = StringUtil.split(spec, ";")[0];
                    String specVersionId = StringUtil.split(spec, ";")[1];
                    JSONObject customers = nestedjsonObj.getJSONObject("customers");
                    Iterator custitr = customers.keys();
                    if (custitr.hasNext()) {
                        while (custitr.hasNext()) {
                            String customerdetail = customers.getString((String)custitr.next());
                            if (customerdetail.trim().length() <= 0) continue;
                            addressid = StringUtil.split(customerdetail, ";")[0];
                            String addressType = StringUtil.split(customerdetail, ";")[1];
                            String contactFunction = StringUtil.split(customerdetail, ";")[2];
                            String preference = StringUtil.split(customerdetail, ";")[3];
                            r = dsCustomerSpec.addRow();
                            dsCustomerSpec.setString(r, "specid", specId);
                            dsCustomerSpec.setString(r, "specversionid", specVersionId);
                            dsCustomerSpec.setString(r, "addressid", addressid);
                            dsCustomerSpec.setString(r, "addresstype", addressType);
                            dsCustomerSpec.setString(r, "contactfunction", contactFunction);
                            dsCustomerSpec.setString(r, "preferred", preference);
                        }
                    }
                    if (!parentSDIMode || !(sampleitr = (samples = nestedjsonObj.getJSONObject("Samples")).keys()).hasNext()) continue;
                    while (sampleitr.hasNext()) {
                        String sample = samples.getString((String)sampleitr.next());
                        int r2 = dsSampleSpec.addRow();
                        dsSampleSpec.setString(r2, "specid", specId);
                        dsSampleSpec.setString(r2, "specversionid", specVersionId);
                        dsSampleSpec.setString(r2, "sampleid", sample);
                    }
                }
            }
            if (preferred != null) {
                itr = preferred.keys();
                DataSet addSDISpec = new DataSet();
                DataSet applySDISpec = new DataSet();
                DataSet addSDIAddress = new DataSet();
                DataSet parentSDIAddress = new DataSet();
                if (itr.hasNext()) {
                    while (itr.hasNext()) {
                        String value = preferred.getString((String)itr.next());
                        if (value.trim().length() <= 0) continue;
                        String[] array = StringUtil.split(value, ";");
                        String keyid1 = array[0];
                        String specid = array[1];
                        String specversion = array[2];
                        String oos = array[3];
                        if (oos.equals("")) {
                            oos = "Y";
                        }
                        String addressid2 = array[4];
                        String addresstype = array[5];
                        String contactFunction = array[6];
                        DataSet dsSamples = new DataSet();
                        if (!parentSDIMode) {
                            int r3 = dsSamples.addRow();
                            dsSamples.setString(r3, "specid", specid);
                            dsSamples.setString(r3, "specversionid", specversion);
                            dsSamples.setString(r3, "sampleid", keyid1);
                        } else {
                            HashMap<String, String> findSamples = new HashMap<String, String>();
                            findSamples.put("specid", specid);
                            findSamples.put("specversionid", specversion);
                            dsSamples = dsSampleSpec.getFilteredDataSet(findSamples);
                        }
                        for (int s = 0; s < dsSamples.getRowCount(); ++s) {
                            int r4;
                            HashMap<String, String> find;
                            keyid1 = dsSamples.getValue(s, "sampleid");
                            sql.setLength(0);
                            safeSQL.reset();
                            sql.append("SELECT * FROM sdispec WHERE sdcid = ").append(safeSQL.addVar(primarySdcid)).append(" AND keyid1 = ").append(safeSQL.addVar(keyid1)).append(" AND specid = ").append(safeSQL.addVar(specid)).append(" AND specversionid = ").append(safeSQL.addVar(specversion));
                            DataSet sdispec = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            if (sdispec.getRowCount() == 0) {
                                find = new HashMap<String, String>();
                                find.put("keyid1", keyid1);
                                find.put("specid", specid);
                                find.put("specversionid", specversion);
                                if (addSDISpec.findRow(find) < 0) {
                                    r4 = addSDISpec.addRow();
                                    addSDISpec.setString(r4, "specid", specid);
                                    addSDISpec.setString(r4, "specversionid", specversion);
                                    addSDISpec.setString(r4, "oosgeneratingflag", oos);
                                    addSDISpec.setString(r4, "keyid1", keyid1);
                                    addSDISpec.setString(r4, "applyspec", "Y");
                                }
                            } else {
                                String appliedflag = sdispec.getValue(0, "appliedflag");
                                if (!appliedflag.equalsIgnoreCase("Y")) {
                                    HashMap<String, String> find2 = new HashMap<String, String>();
                                    find2.put("keyid1", keyid1);
                                    find2.put("specid", specid);
                                    find2.put("specversionid", specversion);
                                    if (applySDISpec.findRow(find2) < 0) {
                                        int r5 = applySDISpec.addRow();
                                        applySDISpec.setString(r5, "specid", specid);
                                        applySDISpec.setString(r5, "specversionid", specversion);
                                        applySDISpec.setString(r5, "keyid1", keyid1);
                                    }
                                }
                            }
                            find = new HashMap();
                            find.put("keyid1", keyid1);
                            find.put("addressid", addressid2);
                            find.put("addresstype", addresstype);
                            find.put("contactfunction", contactFunction);
                            if (addSDIAddress.findRow(find) < 0) {
                                r4 = addSDIAddress.addRow();
                                addSDIAddress.setString(r4, "addressid", addressid2);
                                addSDIAddress.setString(r4, "addresstype", addresstype);
                                addSDIAddress.setString(r4, "contactfunction", contactFunction);
                                addSDIAddress.setString(r4, "keyid1", keyid1);
                            }
                            if (!parentSDIMode || s != 0) continue;
                            find.put("keyid1", pageinfo.getProperty("keyid1"));
                            if (parentSDIAddress.findRow(find) >= 0) continue;
                            r4 = parentSDIAddress.addRow();
                            parentSDIAddress.setString(r4, "addressid", addressid2);
                            parentSDIAddress.setString(r4, "addresstype", addresstype);
                            parentSDIAddress.setString(r4, "contactfunction", contactFunction);
                            parentSDIAddress.setString(r4, "keyid1", pageinfo.getProperty("keyid1"));
                        }
                    }
                }
                if (addSDISpec.getRowCount() > 0) {
                    addSDISpec.sort("specid,specversionid");
                    ArrayList<DataSet> specGroups = addSDISpec.getGroupedDataSets("specid,specversionid");
                    for (int i = 0; i < specGroups.size(); ++i) {
                        DataSet ds = specGroups.get(i);
                        actionProps.clear();
                        actionProps.setProperty("sdcid", primarySdcid);
                        actionProps.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                        actionProps.setProperty("specid", ds.getValue(0, "specid"));
                        actionProps.setProperty("specversionid", ds.getValue(0, "specversionid"));
                        actionProps.setProperty("oosgeneratingflag", ds.getValue(0, "oosgeneratingflag"));
                        actionProps.setProperty("applyspec", ds.getValue(0, "applyspec"));
                        actionProps.setProperty("auditreason", auditReason);
                        actionProps.setProperty("auditactivity", auditActivity);
                        actionProps.setProperty("auditsignedflag", auditSignedFlag);
                        ap.processAction("AddSDISpec", "1", actionProps);
                    }
                }
                if (applySDISpec.getRowCount() > 0) {
                    actionProps.clear();
                    actionProps.setProperty("sdcid", primarySdcid);
                    actionProps.setProperty("keyid1", applySDISpec.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("specid", applySDISpec.getColumnValues("specid", ";"));
                    actionProps.setProperty("specversionid", applySDISpec.getColumnValues("specversionid", ";"));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    actionProps.setProperty("propsmatch", "Y");
                    ap.processAction("ApplySDISpecs", "1", actionProps);
                }
                CustomerSpecPageRenderer.addSDIAddress(addSDIAddress, qp, ap, primarySdcid, auditReason, auditActivity, auditSignedFlag);
                if (parentSDIMode) {
                    CustomerSpecPageRenderer.addSDIAddress(parentSDIAddress, qp, ap, pageinfo.getProperty("sdcid"), auditReason, auditActivity, auditSignedFlag);
                }
            }
            if (rejected != null) {
                HashMap<String, String> findAddress;
                DataSet deleteSDIAddressActionProps;
                DataSet deleteSDIAddress = new DataSet();
                DataSet deleteParentSDIAddress = new DataSet();
                Iterator itr2 = rejected.keys();
                if (itr2.hasNext()) {
                    while (itr2.hasNext()) {
                        String value = rejected.getString((String)itr2.next());
                        if (value.trim().length() <= 0) continue;
                        String[] array = StringUtil.split(value, ";");
                        String keyid1 = array[0];
                        String specid = array[1];
                        String specversion = array[2];
                        addressid = array[4];
                        String addresstype = array[5];
                        String contactfunction = array[6];
                        DataSet dsSamples = new DataSet();
                        if (!parentSDIMode) {
                            r = dsSamples.addRow();
                            dsSamples.setString(r, "specid", specid);
                            dsSamples.setString(r, "specversionid", specversion);
                            dsSamples.setString(r, "sampleid", keyid1);
                        } else {
                            HashMap<String, String> findSamples = new HashMap<String, String>();
                            findSamples.put("specid", specid);
                            findSamples.put("specversionid", specversion);
                            dsSamples = dsSampleSpec.getFilteredDataSet(findSamples);
                        }
                        for (int s = 0; s < dsSamples.getRowCount(); ++s) {
                            keyid1 = dsSamples.getValue(s, "sampleid");
                            int r6 = deleteSDIAddress.addRow();
                            deleteSDIAddress.setString(r6, "specid", specid);
                            deleteSDIAddress.setString(r6, "specversionid", specversion);
                            deleteSDIAddress.setString(r6, "addressid", addressid);
                            deleteSDIAddress.setString(r6, "addresstype", addresstype);
                            deleteSDIAddress.setString(r6, "contactfunction", contactfunction);
                            deleteSDIAddress.setString(r6, "keyid1", keyid1);
                            deleteSDIAddress.setString(r6, "preferred", "Y");
                        }
                        if (!parentSDIMode) continue;
                        r = deleteParentSDIAddress.addRow();
                        deleteParentSDIAddress.setString(r, "addressid", addressid);
                        deleteParentSDIAddress.setString(r, "addresstype", addresstype);
                        deleteParentSDIAddress.setString(r, "contactfunction", contactfunction);
                        deleteParentSDIAddress.setString(r, "keyid1", pageinfo.getProperty("keyid1"));
                    }
                }
                if (deleteSDIAddress.getRowCount() > 0) {
                    deleteSDIAddressActionProps = new DataSet();
                    for (int i = 0; i < deleteSDIAddress.getRowCount(); ++i) {
                        findAddress = new HashMap<String, String>();
                        findAddress.put("keyid1", deleteSDIAddress.getValue(i, "keyid1"));
                        findAddress.put("addressid", deleteSDIAddress.getValue(i, "addressid"));
                        findAddress.put("addresstype", deleteSDIAddress.getValue(i, "addresstype"));
                        findAddress.put("contactfunction", deleteSDIAddress.getValue(i, "contactfunction"));
                        if (deleteSDIAddressActionProps.findRow(findAddress) >= 0) continue;
                        deleteSDIAddressActionProps.copyRow(deleteSDIAddress, i, 1);
                    }
                    actionProps.clear();
                    actionProps.setProperty("sdcid", primarySdcid);
                    actionProps.setProperty("keyid1", deleteSDIAddressActionProps.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("addressid", deleteSDIAddressActionProps.getColumnValues("addressid", ";"));
                    actionProps.setProperty("addresstype", deleteSDIAddressActionProps.getColumnValues("addresstype", ";"));
                    actionProps.setProperty("contactfunction", deleteSDIAddressActionProps.getColumnValues("contactfunction", ";"));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    ap.processAction("DeleteSDIAddress", "1", actionProps);
                }
                if (deleteParentSDIAddress.getRowCount() > 0) {
                    deleteSDIAddressActionProps = new DataSet();
                    for (int i = 0; i < deleteParentSDIAddress.getRowCount(); ++i) {
                        findAddress = new HashMap();
                        findAddress.put("keyid1", deleteParentSDIAddress.getValue(i, "keyid1"));
                        findAddress.put("addressid", deleteParentSDIAddress.getValue(i, "addressid"));
                        findAddress.put("addresstype", deleteParentSDIAddress.getValue(i, "addresstype"));
                        findAddress.put("contactfunction", deleteParentSDIAddress.getValue(i, "contactfunction"));
                        if (deleteSDIAddressActionProps.findRow(findAddress) >= 0) continue;
                        deleteSDIAddressActionProps.copyRow(deleteParentSDIAddress, i, 1);
                    }
                    actionProps.clear();
                    actionProps.setProperty("sdcid", pageinfo.getProperty("sdcid"));
                    actionProps.setProperty("keyid1", deleteSDIAddressActionProps.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("addressid", deleteSDIAddressActionProps.getColumnValues("addressid", ";"));
                    actionProps.setProperty("addresstype", deleteSDIAddressActionProps.getColumnValues("addresstype", ";"));
                    actionProps.setProperty("contactfunction", deleteSDIAddressActionProps.getColumnValues("contactfunction", ";"));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    ap.processAction("DeleteSDIAddress", "1", actionProps);
                }
            }
        }
        catch (Exception e) {
            Trace.logError(e.getMessage(), e);
            e.printStackTrace();
            throw new SapphireException(e.getMessage());
        }
    }

    private static void addSDIAddress(DataSet addSDIAddress, QueryProcessor qp, ActionProcessor ap, String sdcId, String auditReason, String auditActivity, String auditSignedFlag) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (addSDIAddress.getRowCount() > 0) {
            addSDIAddress.sort("keyid1");
            ArrayList<DataSet> dsGroup = addSDIAddress.getGroupedDataSets("keyid1");
            for (int i = 0; i < dsGroup.size(); ++i) {
                DataSet ds = dsGroup.get(i);
                sql.append("SELECT * FROM sdiaddress WHERE sdcid = ").append(safeSQL.addVar(sdcId)).append(" AND keyid1 = ").append(safeSQL.addVar(ds.getValue(0, "keyid1")));
                DataSet sdiAddress = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                sql.setLength(0);
                safeSQL.reset();
                HashMap<String, String> findmap = new HashMap<String, String>();
                for (int a = 0; a < sdiAddress.getRowCount(); ++a) {
                    findmap.put("keyid1", sdiAddress.getValue(a, "keyid1"));
                    findmap.put("addressid", sdiAddress.getValue(a, "addressid"));
                    findmap.put("addresstype", sdiAddress.getValue(a, "addresstype"));
                    findmap.put("contactfunction", sdiAddress.getValue(a, "contactfunction"));
                    int findAddress = addSDIAddress.findRow(findmap);
                    if (findAddress <= -1) continue;
                    addSDIAddress.remove(findAddress);
                }
            }
            if (addSDIAddress.getRowCount() > 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", addSDIAddress.getColumnValues("keyid1", ";"));
                actionProps.setProperty("addressid", addSDIAddress.getColumnValues("addressid", ";"));
                actionProps.setProperty("addresstype", addSDIAddress.getColumnValues("addresstype", ";"));
                actionProps.setProperty("contactfunction", addSDIAddress.getColumnValues("contactfunction", ";"));
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("propsmatch", "Y");
                ap.processAction("AddSDIAddress", "1", actionProps);
            }
        }
    }

    public void setTabPropertyList(PropertyList tabPropertyList, PageContext pageContext, String id, TranslationProcessor tp) {
        if (tabPropertyList != null) {
            if (tabPropertyList.getProperty("expanded") != null && tabPropertyList.getProperty("expanded").equals("Y")) {
                tabPropertyList.setProperty("expanded", "true");
            } else {
                tabPropertyList.setProperty("expanded", "false");
            }
            if (tabPropertyList.getProperty("expandable") != null && tabPropertyList.getProperty("expandable").equals("N")) {
                tabPropertyList.setProperty("expandable", "false");
            } else {
                tabPropertyList.setProperty("expandable", "true");
            }
            if (tabPropertyList.getProperty("id") == null || tabPropertyList.getProperty("id").equals("")) {
                tabPropertyList.setProperty("id", id);
            }
            if (tabPropertyList.containsKey("tip")) {
                tabPropertyList.setProperty("tip", tp.translate(tabPropertyList.getProperty("tip")));
            }
            if (tabPropertyList.containsKey("text")) {
                tabPropertyList.setProperty("text", tp.translate(tabPropertyList.getProperty("text")));
            }
            pageContext.setAttribute(id, (Object)tabPropertyList);
        }
    }

    private static String resolveDisplayValue(String dispValue, String value) {
        String[] dispValueArray = StringUtil.split(dispValue, ";");
        for (int r = 0; r < dispValueArray.length; ++r) {
            String[] dispValues = StringUtil.split(dispValueArray[r], "=");
            if (!value.equals(dispValues[0])) continue;
            return dispValues[1];
        }
        Logger.logInfo("Display Value could not be resolved: " + dispValue);
        return value;
    }

    private int getRefDisplayIndex(boolean parentSDIMode, String condition, DataSet dsSpec, DataSet dsRefValues) {
        int maxUsersequence = -1;
        int rowIndex = -1;
        if (parentSDIMode) {
            for (int d = 0; d < dsSpec.getRowCount(); ++d) {
                int usersequence;
                condition = dsSpec.getValue(d, "condition");
                int f = dsRefValues.findRow("refvalueid", condition);
                if (f <= -1 || maxUsersequence >= (usersequence = dsRefValues.getInt(f, "usersequence"))) continue;
                maxUsersequence = usersequence;
            }
            int findWorst = dsRefValues.findRow("usersequence", Integer.toString(maxUsersequence));
            if (findWorst > -1) {
                rowIndex = findWorst;
            }
        } else {
            int f = dsRefValues.findRow("refvalueid", condition);
            if (f > -1) {
                rowIndex = f;
            }
        }
        return rowIndex;
    }
}

