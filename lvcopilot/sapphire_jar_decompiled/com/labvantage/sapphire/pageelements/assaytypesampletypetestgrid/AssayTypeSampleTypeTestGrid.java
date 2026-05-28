/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.assaytypesampletypetestgrid;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AssayTypeSampleTypeTestGrid
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89509 $";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private String trashBinGif = "WEB-CORE/elements/images/close.gif";
    private String propertyHandlerClass = "com.labvantage.sapphire.pageelements.assaytypesampletypetestgrid.AssayTypeSampleTypeTestGridPropertyHandler";
    private String elementJSScript = "WEB-CORE/elements/assaytypesampletypetestgrid/scripts/assaytypesampletypetestgrid.js";
    private static final String CONSTANT_Y = "Y";

    @Override
    public String getHtml() {
        int i;
        String text_sampleTypes = this.getTranslationProcessor().translate("Sample Types");
        String text_assaytypes = this.getTranslationProcessor().translate("Assay Types");
        String text_testmethod = this.getTranslationProcessor().translate("Test Method");
        String text_specimentype = this.getTranslationProcessor().translate("Specimen Type");
        String text_arrivalorder = this.getTranslationProcessor().translate("Arrival Order");
        String text_remove = "Remove";
        DataSet primary = this.sdiInfo.getDataSet("primary");
        boolean locked = false;
        String disabled = "";
        String ddOnFocus = "";
        boolean viewOnly = this.element.getProperty("viewonly", "N").equalsIgnoreCase(CONSTANT_Y);
        String sampleTypeLink = this.element.getProperty("sampletypelink", "");
        String assayTypeLink = this.element.getProperty("assaytypelink", "");
        if (sampleTypeLink.length() > 0) {
            sampleTypeLink = "rc?command=page&page=" + sampleTypeLink + "&keyid1=";
        }
        if (assayTypeLink.length() > 0) {
            assayTypeLink = "rc?command=page&page=" + assayTypeLink + "&keyid1=";
        }
        if (primary != null) {
            boolean bl = locked = primary.getValue(0, "__lockedby", "").length() > 0;
            if (locked) {
                viewOnly = true;
            }
        }
        if (viewOnly) {
            disabled = "Disabled";
            ddOnFocus = "onFocus=\"this.disabled=true;\"";
        }
        this.keyid1 = this.requestContext.getProperty("keyid1");
        this.keyid2 = this.requestContext.getProperty("keyid2");
        this.keyid3 = this.requestContext.getProperty("keyid3");
        String elementId = this.element.getId();
        if (this.keyid1 == null || this.keyid1.trim().equals("") || this.keyid2 == null || this.keyid2.trim().equals("") || this.keyid3 == null || this.keyid3.trim().equals("")) {
            String errorMsg = this.getTranslationProcessor().translate("Element could not be rendered on the page. Mandatory inputs not found in the request.") + " ";
            return "<table cellspacing=0 cellpadding=10  border=0 bordercolor=\"#b0c4de\" style=\"Border: 1px solid; margin:10px\"><tr><td>" + errorMsg + "</td></tr></table>";
        }
        QueryProcessor qp = this.getQueryProcessor();
        StringBuffer html = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        DataSet cpAssayTypes = qp.getPreparedSqlDataSet("SELECT cpat.s_assaytypeid, cpat.usersequence FROM s_cpassaytype cpat WHERE cpat.s_clinicalprotocolid = " + safeSQL.addVar(this.keyid1) + "  AND cpat.s_clinicalprotocolversionid = " + safeSQL.addVar(this.keyid2) + " AND cpat.s_clinicalprotocolrevision=" + safeSQL.addVar(this.keyid3) + " ORDER BY cpat.usersequence", safeSQL.getValues());
        safeSQL.reset();
        DataSet cpSampleTypes = qp.getPreparedSqlDataSet("SELECT DISTINCT st.s_sampletypeid, st.usersequence  FROM s_cpsampletype st  WHERE st.s_clinicalprotocolid = " + safeSQL.addVar(this.keyid1) + "  AND st.s_clinicalprotocolversionid = " + safeSQL.addVar(this.keyid2) + " AND st.s_clinicalprotocolrevision=" + safeSQL.addVar(this.keyid3) + " ORDER BY st.usersequence", safeSQL.getValues());
        String assayTypeIds = cpAssayTypes.getColumnValues("s_assaytypeid", ";");
        String sampleTypeIds = cpSampleTypes.getColumnValues("s_sampletypeid", ";");
        StringBuffer sampTypeUseq = new StringBuffer();
        StringBuffer assayTypeUseq = new StringBuffer();
        for (i = 0; i < cpAssayTypes.getRowCount(); ++i) {
            assayTypeUseq.append(",{\"assaytypeid\":\"" + cpAssayTypes.getString(i, "s_assaytypeid") + "\",");
            assayTypeUseq.append("\"usersequence\":\"" + cpAssayTypes.getInt(i, "usersequence", 1) + "\"}");
        }
        for (i = 0; i < cpSampleTypes.getRowCount(); ++i) {
            sampTypeUseq.append(",{\"sampletypeid\":\"" + cpSampleTypes.getString(i, "s_sampletypeid") + "\",");
            sampTypeUseq.append("\"usersequence\":\"" + cpSampleTypes.getInt(i, "usersequence", 1) + "\"}");
        }
        String jsonATPUseqArray = "useqAssayTypes:[" + (assayTypeUseq.length() > 0 ? assayTypeUseq.substring(1) : "") + "]";
        String jsonSTPUseqArray = "useqSampleTypes:[" + (sampTypeUseq.length() > 0 ? sampTypeUseq.substring(1) : "") + "]";
        safeSQL.reset();
        DataSet samTypeAssayTypes = qp.getPreparedSqlDataSet("SELECT s_sampletypeid, s_assaytypeid, usersequence FROM s_assaytypesampletype WHERE s_assaytypeid IN (" + safeSQL.addIn(assayTypeIds, ";") + ")", safeSQL.getValues());
        String samTypeAssayTypeIds = samTypeAssayTypes.getColumnValues("s_assaytypeid", ";");
        String samTypeSampleTypeIds = samTypeAssayTypes.getColumnValues("s_sampletypeid", ";");
        safeSQL.reset();
        DataSet assayTypeWorkItems = qp.getPreparedSqlDataSet("SELECT DISTINCT s_assaytypeid, workitemid FROM s_assaytypeworkitem WHERE s_assaytypeid IN (" + safeSQL.addIn(samTypeAssayTypeIds, ";") + ") ORDER BY workitemid", safeSQL.getValues());
        safeSQL.reset();
        DataSet sampleTypeContType = qp.getPreparedSqlDataSet("SELECT s_sampletypeid, containertypeid FROM s_sampletypecontainertype WHERE  s_sampletypeid IN (" + safeSQL.addIn(samTypeSampleTypeIds, ";") + ") AND ACTIVEFLAG = 'Y' ORDER BY containertypeid", safeSQL.getValues());
        safeSQL.reset();
        DataSet cpAssaySampleTypeWItems = qp.getPreparedSqlDataSet("SELECT s_assaytypeid, s_sampletypeid, workitemid, specimentype, arrivalorder FROM s_cpassaytypesampletype  WHERE s_clinicalprotocolid = " + safeSQL.addVar(this.keyid1) + " AND s_clinicalprotocolversionid = " + safeSQL.addVar(this.keyid2) + " AND s_clinicalprotocolrevision=" + safeSQL.addVar(this.keyid3), safeSQL.getValues());
        DataSet ordinalList = qp.getRefTypeDataSet("ArrivalOrder");
        ordinalList.sort("refvalueid d");
        String ordinals = "";
        String ordinalDisplay = "";
        if (ordinalList != null && ordinalList.size() > 0) {
            ordinals = SafeHTML.encodeForJavaScript(ordinalList.getColumnValues("refvalueid", ";"));
            ordinalDisplay = SafeHTML.encodeForJavaScript(ordinalList.getColumnValues("refdisplayvalue", ";"));
        }
        html.append("\n<script type=\"text/javascript\" src=\"" + this.elementJSScript + "\"></script>");
        html.append("\n<input type=\"hidden\" name=\"__propertyhandler_" + elementId + "\" id=\"" + "__propertyhandler_" + elementId + "\" value=\"" + this.propertyHandlerClass + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__assatypesampletypetestgrid_elementid\" id=\"assatypesampletypetest_elementid\" value=\"" + elementId + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + elementId + "_keyid1\" id=\"__" + elementId + "_keyid1\" value=\"" + this.keyid1 + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + elementId + "_keyid2\" id=\"__" + elementId + "_keyid2\" value=\"" + this.keyid2 + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + elementId + "_keyid3\" id=\"__" + elementId + "_keyid3\" value=\"" + this.keyid3 + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + elementId + "_jsonString\" value=\"\" id=\"__" + elementId + "_jsonString\" value=\"\" />");
        html.append("\n<script>");
        html.append("\n var " + elementId + "_jsonObj = {sampleTypes:[").append(sampleTypeIds.length() > 0 ? "'" + sampleTypeIds.replaceAll(";", "','") + "'" : "").append("], assayTypes:[").append(assayTypeIds.length() > 0 ? "'" + assayTypeIds.replaceAll(";", "','") + "'" : "").append("], deletedAssayTypes:[], deletedSampleTypes:[], addedSampleTypes:[], addedAssayTypes:[]," + jsonATPUseqArray + "," + jsonSTPUseqArray + ",  modifiedWorkItems:[], ordinals:['" + ordinals.replaceAll(";", "','") + "'], ordinalsDisplay:['" + ordinalDisplay.replaceAll(";", "','") + "'] };");
        html.append("\n</script>");
        if (!viewOnly) {
            html.append("\n<Table width=100% cellspacing=\"0\"><tr><td align=left>");
            html.append(this.getButtons());
            html.append("</td></tr></table>");
        }
        String divDisplay = cpAssayTypes != null && cpAssayTypes.getRowCount() > 0 || cpSampleTypes != null && cpSampleTypes.getRowCount() > 0 ? "display:block" : "display:none";
        html.append("<div id=\"" + elementId + "_maintable_div\" style=\"" + divDisplay + "\">");
        html.append("<Table class=\"gridmaint_table\" width=100% cellspacing=\"0\" border=1 >");
        html.append("<tr  height=\"30\"><td bgcolor=#b0c4de colspan=2 style=\"text-align:center\" ><b>" + text_sampleTypes + "</b></td></tr>");
        String vert_Text_Style = this.browser.isIE() ? "vertical-align:middle;writing-mode: bt-rl;filter: fliph flipv;" : (this.browser.isWebkit() ? "width: 100px; margin-left: -35px; position:relative; -webkit-transform: rotate(-90deg);" : "width: 150px; margin-left: -60px;  position:relative; -moz-transform: rotate(-90deg);");
        html.append("<tr  height=\"30\">");
        html.append(this.browser.isIE() ? "<td bgcolor=#b0c4de nowrap valign=center align=center width=30 style=\"" + vert_Text_Style + "\">&nbsp;&nbsp;&nbsp;&nbsp;<b>" + text_assaytypes + "</b>&nbsp;</td>" : "<td bgcolor=#b0c4de nowrap valign=center align=center width=30><table><tr><td style=\"" + vert_Text_Style + "\">&nbsp;&nbsp;&nbsp;&nbsp;<b>" + text_assaytypes + "</b>&nbsp;</td></tr></table></td>");
        html.append("<td>");
        html.append("<Table id= 'table_assaytypesampletypetestgrid' class=\"gridmaint_table\" height=100% width=100% cellspacing=\"0\">");
        html.append("<tr  id = \"" + elementId + "sampletype_header\" height=\"30\"><td class=\"gridmaint_fieldtitle\" width= '*'  style=\"background-color:khaki\">&nbsp;</td>");
        for (int c = 0; c < cpSampleTypes.size(); ++c) {
            String sampleTypeId = cpSampleTypes.getValue(c, "s_sampletypeid");
            html.append("<td id=\"" + elementId + "_td_" + sampleTypeId + "\"  width=250px  nowrap align=center valign=center class=\"gridmaint_fieldtitle\" style=\"background-color:khaki\"><table  width=100% border=0>");
            if (!viewOnly) {
                html.append("<tr><td align=right  width=100% nowrap><a style=cursor:pointer onClick=\"asaTypSamTypTestGrid.deleteSampleType('" + sampleTypeId + "','" + elementId + "')\"><img src=\"" + this.trashBinGif + "\" title=\"" + "Remove" + " " + sampleTypeId + "\"></a></td></tr>");
            }
            html.append("<tr><td align=left  width=100% nowrap>");
            html.append(viewOnly ? "&nbsp;" : "<input  type=\"checkbox\" onClick=\"asaTypSamTypTestGrid.selectDeselectAll('" + sampleTypeId + "','" + this.element.getId() + "', 'sampletype', this )\" >&nbsp;");
            html.append(sampleTypeLink.length() > 0 ? "<a style=cursor:pointer onClick=\"asaTypSamTypTestGrid.openPage('" + sampleTypeLink + HttpUtil.encodeURIComponent(sampleTypeId) + "')\">" + sampleTypeId + "</a>" : sampleTypeId);
            html.append("</td></tr>");
            html.append("</table></td>");
        }
        html.append("</tr>");
        HashMap<String, String> findMap = new HashMap<String, String>();
        HashMap<String, String> filterMap = new HashMap<String, String>();
        StringBuffer ddHTML = new StringBuffer();
        for (int row = 0; row < cpAssayTypes.size(); ++row) {
            String assayTypeId = cpAssayTypes.getValue(row, "s_assaytypeid");
            html.append("<tr id=\"" + elementId + "_tr_" + assayTypeId + "\" height=50px>");
            html.append("<td width=\"*\" nowrap class=\"gridmaint_fieldtitle\" align=center  style=\"background-color:khaki\">");
            html.append("<table width=100% border=0>");
            if (!viewOnly) {
                html.append("<tr><td align=right valign=top colspan=2 nowrap>");
                html.append("<a style=cursor:pointer onClick=\"asaTypSamTypTestGrid.deleteAssayType('" + assayTypeId + "','" + this.element.getId() + "')\"><img src=\"" + this.trashBinGif + "\" title=\"" + "Remove" + " " + assayTypeId + "\"></a>");
                html.append("</td></tr>");
            }
            html.append("<tr><td valign=top align=left nowrap>");
            html.append(viewOnly ? "&nbsp;" : "<input  type=\"checkbox\" onClick=\"asaTypSamTypTestGrid.selectDeselectAll('" + assayTypeId + "','" + this.element.getId() + "', 'assaytype', this )\" >&nbsp;");
            html.append(assayTypeLink.length() > 0 ? "<a style=cursor:pointer onClick=\"asaTypSamTypTestGrid.openPage('" + assayTypeLink + HttpUtil.encodeURIComponent(assayTypeId, "UTF-8") + "')\">" + assayTypeId + "</a>" : assayTypeId);
            html.append("</td>");
            html.append(viewOnly ? "&nbsp;" : "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>");
            html.append("</table></td>");
            for (int col = 0; col < cpSampleTypes.size(); ++col) {
                String sampTypeId = cpSampleTypes.getValue(col, "s_sampletypeid");
                findMap.put("s_sampletypeid", sampTypeId);
                findMap.put("s_assaytypeid", assayTypeId);
                int findRow = samTypeAssayTypes.findRow(findMap);
                filterMap.clear();
                filterMap.put("s_assaytypeid", assayTypeId);
                DataSet workItems = assayTypeWorkItems.getFilteredDataSet(filterMap);
                if (findRow > -1 && workItems != null && workItems.size() > 0) {
                    filterMap.put("s_sampletypeid", sampTypeId);
                    DataSet currentWorkItemDS = cpAssaySampleTypeWItems.getFilteredDataSet(filterMap);
                    String currentWorkItemId = "";
                    String checked = "";
                    String status = "";
                    String currentSpecimenType = "";
                    String currentArrivalOrder = "";
                    String ddDisabled = currentWorkItemDS != null && currentWorkItemDS.getRowCount() > 0 ? "" : "disabled";
                    ddHTML.setLength(0);
                    ddHTML.append("<td><table cellpadding=1 cellspacing=0  width=\"100%\" height=\"100%\" >");
                    ddHTML.append("<tr><td nowrap>" + text_testmethod + ": </td><td  align=left valign=top colspan=2 >");
                    ddHTML.append("<select " + ddDisabled + " title=\"" + text_testmethod + "\" name=\"_" + elementId + "_dd_" + sampTypeId + "_" + assayTypeId + "\" id=\"_" + elementId + "_dd_" + sampTypeId + "_" + assayTypeId + "\" " + ddOnFocus + " onChange =\"asaTypSamTypTestGrid.ddSelection('" + elementId + "','" + sampTypeId + "','" + assayTypeId + "')\">");
                    ddHTML.append("<option value=\"\" ></option>");
                    for (int wi = 0; wi < workItems.size(); ++wi) {
                        String selected = "";
                        String workItemId = workItems.getValue(wi, "workitemid", "");
                        if (currentWorkItemDS != null) {
                            for (int cpwi = 0; cpwi < currentWorkItemDS.size(); ++cpwi) {
                                if (!workItemId.equals(currentWorkItemDS.getValue(cpwi, "workitemid", ""))) continue;
                                selected = "selected";
                                checked = "checked";
                                status = "S";
                                currentWorkItemId = workItemId;
                                currentSpecimenType = currentWorkItemDS.getValue(cpwi, "specimentype", "");
                                currentArrivalOrder = currentWorkItemDS.getValue(cpwi, "arrivalorder", "");
                                break;
                            }
                        }
                        ddHTML.append("<option value=\"" + workItemId + "\" " + selected + ">" + workItemId + "</option>");
                    }
                    ddHTML.append("</select></td></tr>");
                    ddHTML.append("<tr>");
                    DataSet specimenTypes = null;
                    filterMap.clear();
                    filterMap.put("s_sampletypeid", sampTypeId);
                    if (sampleTypeContType != null) {
                        specimenTypes = sampleTypeContType.getFilteredDataSet(filterMap);
                    }
                    if (specimenTypes != null && specimenTypes.size() > 0) {
                        ddHTML.append("<td nowrap>" + text_specimentype + ": </td><td align=left valign=top >");
                    } else {
                        ddHTML.append("<td nowrap>&nbsp;</td><td align=left valign=top >");
                    }
                    if (ordinalList != null && ordinalList.size() > 0) {
                        ddHTML.append("<select " + ddDisabled + " title=\"" + text_arrivalorder + "\" name=\"_" + elementId + "_dd_arrivalorder" + sampTypeId + "_" + assayTypeId + "\" id=\"_" + elementId + "_dd_arrivalorder" + sampTypeId + "_" + assayTypeId + "\" " + ddOnFocus + " onChange =\"asaTypSamTypTestGrid.ddSelection('" + elementId + "','" + sampTypeId + "','" + assayTypeId + "')\">");
                        ddHTML.append("<option value=\"\" ></option>");
                        for (int ai = 0; ai < ordinalList.size(); ++ai) {
                            String arrivalOrder = ordinalList.getValue(ai, "refvalueid", "");
                            String dispValue = ordinalList.getValue(ai, "refdisplayvalue", "");
                            if (dispValue.equals("")) {
                                dispValue = arrivalOrder;
                            }
                            ddHTML.append("<option value=\"" + arrivalOrder + "\" " + (arrivalOrder.equals(currentArrivalOrder) || "All".equalsIgnoreCase(arrivalOrder) ? "selected" : "") + " ><span>" + dispValue + "</span></option>");
                        }
                        ddHTML.append("</select>");
                    } else {
                        ddHTML.append("&nbsp;");
                    }
                    ddHTML.append("</td>");
                    ddHTML.append("<td align=left valign=top >");
                    if (specimenTypes != null && specimenTypes.size() > 0) {
                        ddHTML.append("<select " + ddDisabled + " title=\"" + text_specimentype + "\" name=\"_" + elementId + "_dd_specimentype" + sampTypeId + "_" + assayTypeId + "\" id=\"_" + elementId + "_dd_specimentype" + sampTypeId + "_" + assayTypeId + "\" " + ddOnFocus + " onChange =\"asaTypSamTypTestGrid.ddSelection('" + elementId + "','" + sampTypeId + "','" + assayTypeId + "')\">");
                        ddHTML.append("<option value=\"\" ></option>");
                        for (int ci = 0; ci < specimenTypes.size(); ++ci) {
                            String specimen = specimenTypes.getValue(ci, "containertypeid", "");
                            ddHTML.append("<option value=\"" + specimen + "\" " + (specimen.equals(currentSpecimenType) ? "selected" : "") + " >" + specimen + "</option>");
                        }
                        ddHTML.append("</select>");
                    } else {
                        ddHTML.append("&nbsp;");
                    }
                    ddHTML.append("</td>");
                    ddHTML.append("</tr></table></td>");
                    html.append("<td valign=top nowrap class=\"gridmaint_field\" id=\"" + elementId + "_td_" + sampTypeId + "\"><table cellspacing=0 cellpadding=0  width=100% ><tr>");
                    html.append("<td  valign=top  nowrap ><input  type=\"checkbox\" " + checked + " " + disabled + " onClick=\" asaTypSamTypTestGrid.ckbSelection('" + elementId + "','" + sampTypeId + "','" + assayTypeId + "', this ) \" name=\"_" + elementId + "_ckb_" + sampTypeId + "_" + assayTypeId + "\" id=\"_" + elementId + "_ckb_" + sampTypeId + "_" + assayTypeId + "\"></td>");
                    html.append("<input type=hidden name=\"_" + elementId + "_status_" + sampTypeId + "_" + assayTypeId + "\" id=\"_" + elementId + "_status_" + sampTypeId + "_" + assayTypeId + "\" value =\"" + status + "\">");
                    html.append(ddHTML.toString());
                    html.append("</tr></table></td>");
                    continue;
                }
                html.append("<td id=\"" + elementId + "_td_" + sampTypeId + "\" nowrap class=\"gridmaint_field\" style=\"background-color:ebebea \">&nbsp;</td>");
            }
            html.append("</tr>");
        }
        html.append("</Table>");
        html.append("</td></tr></table>");
        html.append("</div>");
        return html.toString();
    }

    private String getButtons() {
        String buttonHtml = "";
        StringBuffer sb = new StringBuffer();
        PropertyListCollection buttonscollection = this.element.getCollection("buttons");
        sb.append("<table cellspacing=1 cellpadding=0 width='*' ><tr>");
        StringBuffer function = new StringBuffer();
        for (int i = 0; i < buttonscollection.size(); ++i) {
            PropertyList pl = buttonscollection.getPropertyList(i);
            sb.append("<td>");
            if (pl.getProperty("show").equals(CONSTANT_Y)) {
                this.setDefaultPropertyValue(pl, "show", CONSTANT_Y);
                this.setDefaultPropertyValue(pl, "width", "80");
                this.setDefaultPropertyValue(pl, "appearance", "standard");
                this.setDefaultPropertyValue(pl, "highlight", CONSTANT_Y);
                this.setDefaultPropertyValue(pl, "id", this.element.getId() + "_button_" + i);
                function.setLength(0);
                function.append(pl.getProperty("js", ""));
                if (function.length() > 0) {
                    pl.setProperty("js", "try{" + function.toString() + "}catch(e){}");
                }
                Button button = new Button(this.pageContext);
                ElementUtil.setButtonProperties(button, pl);
                sb.append(button.getHtml());
            }
            sb.append("</td>");
        }
        sb.append("</tr></table>");
        buttonHtml = sb.toString();
        return buttonHtml;
    }

    protected void setDefaultPropertyValue(PropertyList propertyList, String property, String defaultValue) {
        if (propertyList.getProperty(property).length() == 0) {
            propertyList.setProperty(property, defaultValue);
        }
    }
}

