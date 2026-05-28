/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.specifications;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SpecLimits
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 92635 $";
    static final String PROPERTY_EXPANDED = "expanded";
    static final String PROPERTY_ENABLEROWSPAN = "enablerowspan";
    static final String PROPERTY_SHOWCHECKBOXES = "showcheckboxes";
    static final String PROPERTY_BUTTONPLACEMENT = "buttonplacement";
    static final String PROPERTY_BUTTONS = "buttons";
    static final String PROPERTY_ID = "id";
    static final String PROPERTY_TEXT = "text";
    static final String PROPERTY_IMG = "img";
    static final String PROPERTY_TIP = "tip";
    static final String PROPERTY_APPEARANCE = "appearance";
    static final String PROPERTY_MARGIN = "margin";
    static final String PROPERTY_STYLE = "style";
    static final String PROPERTY_WIDTH = "width";
    static final String PROPERTY_JS = "js";
    static final String PROPERTY_KEYID1 = "keyid1";
    static final String PROPERTY_KEYID2 = "keyid2";
    static final String PROPERTY_VIEWONLY = "viewonly";
    static final String PROPERTY_FIELD_WIDTH = "width";
    static final String BANDEDSPECTYPE = "B";
    static final String STRUCTUREDSPECTYPE = "S";
    static final String NAMEPREFIX = "SpecLimits";
    static final String PROPERTYHANDLERCLASS = "com.labvantage.sapphire.pageelements.specifications.SpecLimitsPropertyHandler";
    static final String JSFILE = "WEB-CORE/elements/scripts/speclimits.js";
    static final String JSOBJECT = "specLimits";
    static final String JSONCHANGE_STRUCTURED = "doChange_Structured";
    static final String JSONBLUR_STRUCTURED = "doBlur_Structured";
    static final String JSONCLICK_CHECKBOX = "doCheckboxClick";
    static final String JSEXPANDCOLLAPSE_STRUCTURED = "doExandCollapseClick";
    static final String JSONMOUSEOVER = "doShowHint";
    static final String JSONMOUSEOUT = "doHideHint";
    static final String JSLOOKUPCLICK = "doLookupClick";
    static final String JSCONNECTIONID = "sConnectionId";
    static final String JSELEMENTID = "sElementId";
    static final String JSDATASETNAME = "sDataSetName";
    static final String JSSPECID = "sSpecId";
    static final String JSSPECVERSIONID = "sSpecVersionId";
    static final String IMG_COLLAPSE = "WEB-CORE/elements/images/minus.gif";
    static final String IMG_EXPAND = "WEB-CORE/elements/images/plus.gif";
    static final String SPEC_SPECTYPEFLAG_COL = "spectypeflag";
    static final String SPECLIMITTYPE_LIMITTYPEID_COL = "limittypeid";
    static final String SPECLIMITTYPE_CONDITION_COL = "condition";
    static final String SPECLIMITTYPE_LIMITTYPESEQUENCE_COL = "limittypesequence";
    static final String SPECLIMITTYPE_SEPARATORDISPLAY_COL = "separatordisplay";
    static final String SPECLIMITTYPE_OPERATOR1DISPLAY_COL = "operator1display";
    static final String SPECLIMITTYPE_OPERATOR2DISPLAY_COL = "operator2display";
    static final String SPECLIMITTYPE_VALUE1DISPLAY_COL = "value1display";
    static final String SPECLIMITTYPE_VALUE2DISPLAY_COL = "value2display";
    static final String SPECPARAMITEMS_PARAMLISTID_COL = "paramlistid";
    static final String SPECPARAMITEMS_PARAMLISTVERSIONID_COL = "paramlistversionid";
    static final String SPECPARAMITEMS_VARIANTID_COL = "variantid";
    static final String SPECPARAMITEMS_PARAMID_COL = "paramid";
    static final String SPECPARAMITEMS_PARAMTYPE_COL = "paramtype";
    static final String SPECPARAMITEMS_ALLOWANYPARAMLISTFLAG_COL = "allowanyparamlistflag";
    static final String SPECPARAMITEMS_DATATYPES_COL = "datatypes";
    static final String SPECPARAMITEMS_ENTRYSDCID_COL = "entrysdcid";
    static final String SPECPARAMITEMS_ENTRYREFTYPEID_COL = "entryreftypeid";
    static final String SPECPARAMITEMS_DISPLAYFORMAT_COL = "displayformat";
    static final String SPECPARAMITEMS_UNITSID_COL = "unitsid";
    static final String SPECPARAMLIMITS_SPECID_COL = "specid";
    static final String SPECPARAMLIMITS_SPECVERSIONID_COL = "specversionid";
    static final String SPECPARAMLIMITS_OPERATOR_COL = "operator";
    static final String SPECPARAMLIMITS_VALUE_COL = "value";
    static final String SPECPARAMLIMITS_PARAMLISTID_COL = "paramlistid";
    static final String SPECPARAMLIMITS_PARAMLISTVERSIONID_COL = "paramlistversionid";
    static final String SPECPARAMLIMITS_VARIANTID_COL = "variantid";
    static final String SPECPARAMLIMITS_PARAMID_COL = "paramid";
    static final String SPECPARAMLIMITS_PARAMTYPE_COL = "paramtype";
    static final String SPECPARAMLIMITS_LIMITTYPESEQUENCE_COL = "limittypesequence";
    static final String SPECPARAMLIMITS_DATATYPES_COL = "datatypes";
    static final String ALL_ROWSTATUS_COL = "__rowstatus";
    static final String ALL_ROWID_COL = "__rowid";
    static final String DATASET_SPECPARAMLIMITS = "specparamlimits";
    static final String DATASET_SPECLIMITTYPE = "speclimittype";
    static final String DATASET_SPECPARAMITEMS = "specparamitems";
    static final String DATASET_NUMBEROPERATORS = "RefTypeData:numberoperators";
    static final String DATASET_STRINGOPERATORS = "RefTypeData:stringoperators";
    private String specType;
    private String keyId1;
    private String keyId2;
    private boolean expanded;
    private boolean enableRowSpan;
    private boolean showCheckboxes;
    private String buttonPlacement;
    private boolean viewOnly;
    private M18NUtil m18client;
    private M18NUtil m18server;
    private String fieldWidth;

    private boolean loadProperties() {
        boolean theReturn = false;
        PageTagInfo pageInfo = new PageTagInfo(this.pageContext, this.requestContext);
        this.keyId1 = pageInfo.getProperty(PROPERTY_KEYID1);
        this.keyId2 = pageInfo.getProperty(PROPERTY_KEYID2);
        if (this.keyId1 != null && this.keyId1.length() > 0 && this.keyId2 != null && this.keyId2.length() > 0) {
            theReturn = true;
        } else {
            this.logger.error("Keyid1 and Keyid2 not provided.");
        }
        String temp = this.element.getProperty(PROPERTY_EXPANDED);
        this.expanded = temp == null || temp.length() == 0 || !temp.equalsIgnoreCase("N");
        this.logger.debug("expanded = " + this.expanded);
        temp = this.element.getProperty(PROPERTY_ENABLEROWSPAN);
        this.enableRowSpan = temp != null && temp.length() != 0 && temp.equalsIgnoreCase("Y");
        this.logger.debug("enableRowSpan = " + this.enableRowSpan);
        temp = this.element.getProperty(PROPERTY_SHOWCHECKBOXES);
        this.showCheckboxes = temp == null || temp.length() == 0 || !temp.equalsIgnoreCase("N");
        this.logger.debug("showCheckboxes = " + this.showCheckboxes);
        this.buttonPlacement = this.element.getProperty(PROPERTY_BUTTONPLACEMENT);
        if (this.buttonPlacement == null || this.buttonPlacement.length() == 0) {
            this.buttonPlacement = "none";
        }
        this.logger.debug("buttonPlacement = " + this.buttonPlacement);
        temp = this.element.getProperty(PROPERTY_VIEWONLY);
        this.viewOnly = temp == null || temp.length() == 0 ? false : temp.equalsIgnoreCase("y");
        this.logger.debug("viewOnly = " + this.viewOnly);
        this.fieldWidth = this.element.getProperty("width");
        if (this.fieldWidth == null || this.fieldWidth.length() == 0) {
            this.fieldWidth = "130";
        }
        this.logger.debug("fieldWidth = " + this.fieldWidth);
        return theReturn;
    }

    private String getSpecType() {
        String theReturn;
        if (this.specType == null || this.specType.length() == 0) {
            try {
                this.specType = this.sdiInfo.getDataSet("primary").getString(0, SPEC_SPECTYPEFLAG_COL);
                if (this.specType == null || this.specType.length() == 0) {
                    this.logger.warn("No spec type flag set therefore default (1)");
                    this.specType = STRUCTUREDSPECTYPE;
                }
            }
            catch (Exception e) {
                this.logger.warn("No spec type flag set therefore default (2)");
                this.specType = STRUCTUREDSPECTYPE;
            }
            theReturn = this.specType;
        } else {
            theReturn = this.specType;
        }
        return theReturn;
    }

    private DataSet getParamItemData(String specId, String specVersionId) {
        DataSet returnData = null;
        if (this.sdiInfo != null) {
            returnData = this.sdiInfo.getDataSet(DATASET_SPECPARAMITEMS);
        }
        if (returnData == null) {
            this.logger.info("No dataset for specparamitems in page thus obtain...");
            SafeSQL safeSQL = new SafeSQL();
            String theSQL = "SELECT paramlistid, paramlistversionid, variantid, paramid, paramtype, unitsid, allowanyparamlistflag, datatypes, entrysdcid, entryreftypeid, displayformat, limitlabel FROM specparamitems WHERE specid = " + safeSQL.addVar(specId) + " and specversionid = " + safeSQL.addVar(specVersionId) + " ORDER BY usersequence ";
            this.logger.debug("theSQL = " + theSQL);
            returnData = this.getQueryProcessor().getPreparedSqlDataSet(theSQL, safeSQL.getValues());
        }
        return returnData;
    }

    private DataSet getLimitTypeData(String specId, String specVersionId) {
        DataSet returnData = null;
        if (this.sdiInfo != null) {
            returnData = this.sdiInfo.getDataSet(DATASET_SPECLIMITTYPE);
        }
        if (returnData == null) {
            this.logger.info("No dataset for speclimittype in page thus obtain...");
            SafeSQL safeSQL = new SafeSQL();
            String theSQL = "SELECT limittypeid, condition, limittypesequence, operator1default, operator2default, operator1display, operator2display, value1display, value2display, separatordisplay FROM speclimittype WHERE specid = " + safeSQL.addVar(specId) + " and specversionid = " + safeSQL.addVar(specVersionId) + " ORDER BY usersequence ";
            this.logger.debug("theSQL = " + theSQL);
            returnData = this.getQueryProcessor().getPreparedSqlDataSet(theSQL, safeSQL.getValues());
        }
        return returnData;
    }

    private DataSet getSpecParamLimitData() {
        DataSet returnData = null;
        if (this.sdiInfo != null) {
            returnData = this.sdiInfo.getDataSet(DATASET_SPECPARAMLIMITS);
        }
        return returnData;
    }

    private void fillData(String specid, String specversionid, DataSet specparamitemsData, DataSet speclimittypeData, DataSet specparamlimitsData) {
        if (Trace.isDebugEnabled()) {
            specparamlimitsData.showData();
        }
        for (int paramIndex = 0; paramIndex < specparamitemsData.getRowCount(); ++paramIndex) {
            String parameterlistid = specparamitemsData.getValue(paramIndex, "paramlistid", "");
            String parameterlistversionid = specparamitemsData.getValue(paramIndex, "paramlistversionid", "");
            String parameterlistvariant = specparamitemsData.getValue(paramIndex, "variantid", "");
            String parameterid = specparamitemsData.getValue(paramIndex, "paramid", "");
            String parametertype = specparamitemsData.getValue(paramIndex, "paramtype", "");
            String datatypes = specparamitemsData.getValue(paramIndex, "datatypes", "T");
            if (parameterlistid.length() > 0 && parameterlistversionid.length() > 0 && parameterlistvariant.length() > 0 && parameterid.length() > 0 && parametertype.length() > 0) {
                for (int limitsIndex = 0; limitsIndex < speclimittypeData.getRowCount(); ++limitsIndex) {
                    BigDecimal limittypesequence = speclimittypeData.getBigDecimal(limitsIndex, "limittypesequence");
                    if (limittypesequence != null && limittypesequence.intValue() > 0) {
                        if (this.getSpecParamLimitsRow(specparamlimitsData, parameterlistid, parameterlistversionid, parameterlistvariant, parameterid, parametertype, limittypesequence.toString()) > -1) {
                            this.logger.debug("Row exists in specparamlimts data.");
                            continue;
                        }
                        this.logger.debug("Row not found in specparamlimits data thus append to data.");
                        int row = specparamlimitsData.addRow();
                        specparamlimitsData.setString(row, SPECPARAMLIMITS_SPECID_COL, specid);
                        specparamlimitsData.setString(row, SPECPARAMLIMITS_SPECVERSIONID_COL, specversionid);
                        specparamlimitsData.setString(row, "paramlistid", parameterlistid);
                        specparamlimitsData.setString(row, "paramlistversionid", parameterlistversionid);
                        specparamlimitsData.setString(row, "variantid", parameterlistvariant);
                        specparamlimitsData.setString(row, "paramid", parameterid);
                        specparamlimitsData.setString(row, "paramtype", parametertype);
                        specparamlimitsData.setNumber(row, "limittypesequence", limittypesequence);
                        specparamlimitsData.setString(row, "datatypes", datatypes);
                        specparamlimitsData.setString(row, ALL_ROWID_COL, "" + row);
                        specparamlimitsData.setString(row, ALL_ROWSTATUS_COL, "I");
                        continue;
                    }
                    this.logger.warn("Blank limit type data found in specparamitems.");
                }
                continue;
            }
            this.logger.warn("Blank parameter data found in specparamitems.");
        }
        if (Trace.isDebugEnabled()) {
            this.logger.debug("Data after: ");
            specparamlimitsData.showData();
        }
    }

    private boolean renderHTML(StringBuffer theHTMLBuffer, String specId, String specVersionId, boolean locked, boolean isExpanded, boolean isRowSpanEnabled, boolean isCheckboxesAllowed, String theButtonPlacement, boolean isViewOnly) {
        boolean theReturn = false;
        ArrayList gridArrayList = new ArrayList();
        DataSet paramData = this.getParamItemData(specId, specVersionId);
        if (paramData != null) {
            this.logger.debug("SpecParamItem data found. Row count: " + paramData.getRowCount());
            DataSet limitData = this.getLimitTypeData(specId, specVersionId);
            if (limitData != null) {
                this.logger.debug("SpecLimitType data found. Row count: " + limitData.getRowCount());
                DataSet paramLimitData = this.getSpecParamLimitData();
                if (paramLimitData != null) {
                    this.logger.debug("SpecParamLimit data found. Row count: " + paramLimitData.getRowCount());
                    if (limitData.getRowCount() > 0 && paramData.getRowCount() > 0) {
                        if (paramLimitData.getRowCount() == limitData.getRowCount() * paramData.getRowCount()) {
                            this.logger.debug("SpecParamLimit data contains full data for all limts and parameters.");
                        } else {
                            this.logger.info("Missing rows have been found in SpecParamLimit data.");
                            this.fillData(specId, specVersionId, paramData, limitData, paramLimitData);
                        }
                    } else {
                        this.logger.debug("No parameters and/or limit types present.");
                    }
                    String specType = this.getSpecType();
                    if (theButtonPlacement.equals("none") || isViewOnly) {
                        if (specType.equalsIgnoreCase(BANDEDSPECTYPE)) {
                            theReturn = this.renderBandedHTML(theHTMLBuffer);
                        } else if (specType.equalsIgnoreCase(STRUCTUREDSPECTYPE)) {
                            theReturn = this.renderStructuredHTML(theHTMLBuffer, specId, specVersionId, paramData, limitData, paramLimitData, locked, isExpanded, isRowSpanEnabled, isCheckboxesAllowed, isViewOnly, gridArrayList);
                        }
                    } else if (theButtonPlacement.equals("topleft") || theButtonPlacement.equals("topmiddle") || theButtonPlacement.equals("topright")) {
                        theHTMLBuffer.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
                        this.renderButtons(theHTMLBuffer, theButtonPlacement);
                        theHTMLBuffer.append("</td>\n</tr>\n<tr>\n<td>\n");
                        if (specType.equalsIgnoreCase(BANDEDSPECTYPE)) {
                            theReturn = this.renderBandedHTML(theHTMLBuffer);
                        } else if (specType.equalsIgnoreCase(STRUCTUREDSPECTYPE)) {
                            theReturn = this.renderStructuredHTML(theHTMLBuffer, specId, specVersionId, paramData, limitData, paramLimitData, locked, isExpanded, isRowSpanEnabled, isCheckboxesAllowed, false, gridArrayList);
                        }
                        theHTMLBuffer.append("</td>\n</tr>\n</table>\n");
                    } else if (theButtonPlacement.equals("bottomleft") || theButtonPlacement.equals("bottommiddle") || theButtonPlacement.equals("bottomright")) {
                        theHTMLBuffer.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
                        if (specType.equalsIgnoreCase(BANDEDSPECTYPE)) {
                            theReturn = this.renderBandedHTML(theHTMLBuffer);
                        } else if (specType.equalsIgnoreCase(STRUCTUREDSPECTYPE)) {
                            theReturn = this.renderStructuredHTML(theHTMLBuffer, specId, specVersionId, paramData, limitData, paramLimitData, locked, isExpanded, isRowSpanEnabled, isCheckboxesAllowed, false, gridArrayList);
                        }
                        theHTMLBuffer.append("</td>\n</tr>\n<tr>\n<td>\n");
                        this.renderButtons(theHTMLBuffer, theButtonPlacement);
                        theHTMLBuffer.append("</td>\n</tr>\n</table>\n");
                    } else {
                        theHTMLBuffer.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
                        this.renderButtons(theHTMLBuffer, theButtonPlacement);
                        theHTMLBuffer.append("</td>\n</tr>\n<tr>\n<td>\n");
                        if (specType.equalsIgnoreCase(BANDEDSPECTYPE)) {
                            theReturn = this.renderBandedHTML(theHTMLBuffer);
                        } else if (specType.equalsIgnoreCase(STRUCTUREDSPECTYPE)) {
                            theReturn = this.renderStructuredHTML(theHTMLBuffer, specId, specVersionId, paramData, limitData, paramLimitData, locked, isExpanded, isRowSpanEnabled, isCheckboxesAllowed, false, gridArrayList);
                        }
                        theHTMLBuffer.append("</td>\n</tr>\n<tr>\n<td>\n");
                        this.renderButtons(theHTMLBuffer, theButtonPlacement);
                        theHTMLBuffer.append("</td>\n</tr>\n</table>\n");
                    }
                    this.renderGridHandler(theHTMLBuffer, gridArrayList);
                } else {
                    this.logger.error("No SpecParamLimits data could be found. Please make sure the element is defined correctly.");
                }
            } else {
                this.logger.error("Could not obtain limit types data.");
            }
        } else {
            this.logger.error("Could not obtain the parameter data.");
        }
        return theReturn;
    }

    private void renderGridHandler(StringBuffer html, ArrayList grid) {
        this.logger.debug("grid.size = " + grid.size());
        html.append("\n").append(SDITagUtil.getGrid(grid, this.elementid, -1, -1, true, this.browser, this.getTranslationProcessor())).append("\n");
    }

    private boolean isLimitTypeAreaVisible(DataSet limitTypeData, int row, String columnId) {
        String value = limitTypeData.getValue(row, columnId, "I");
        boolean theReturn = value.equalsIgnoreCase("V") || value.equalsIgnoreCase("D");
        return theReturn;
    }

    private boolean isLimitTypeAreaDisabled(DataSet limitTypeData, int row, String columnId) {
        String value = limitTypeData.getValue(row, columnId, "I");
        boolean theReturn = value.equalsIgnoreCase("D");
        return theReturn;
    }

    private String trans(String in) {
        if (this.pageContext != null) {
            TranslationProcessor tp = this.getTranslationProcessor();
            if (tp != null) {
                return tp.translate(in);
            }
            return in;
        }
        return in;
    }

    private void renderTableHeaderRow_Structured(StringBuffer theHTMLBuffer, DataSet specLimitTypesData, int iRowSpan, boolean isCheckboxesAllowed) {
        theHTMLBuffer.append("<tr class='gridmaint_tablehead'>\n");
        if (isCheckboxesAllowed) {
            theHTMLBuffer.append("<td nowrap class='gridmaint_fieldtitle'>");
            theHTMLBuffer.append("<input type=\"checkbox\" ");
            theHTMLBuffer.append(" onclick=\"").append(JSOBJECT).append(".").append(JSONCLICK_CHECKBOX).append("('").append(this.elementid).append("', '").append(DATASET_SPECPARAMLIMITS).append("', -1 );\" ");
            theHTMLBuffer.append(" id=\"").append(this.elementid).append("_selectAll\">");
            theHTMLBuffer.append("</td>");
        }
        theHTMLBuffer.append("<th class='gridmaint_fieldtitle' nowrap align=left><b>").append(this.trans("Param List (Ver) Variant")).append("</b></th>\n");
        theHTMLBuffer.append("<th class='gridmaint_fieldtitle' nowrap align=left><b>").append(this.trans("Param (Param Type)")).append("</b></th>\n");
        if (this.getTranslationProcessor().isRTL()) {
            theHTMLBuffer.append("<th class='gridmaint_fieldtitle' nowrap align=right><b>").append(this.trans("Limit Label")).append("</b></th>\n");
        } else {
            theHTMLBuffer.append("<th class='gridmaint_fieldtitle' nowrap align=left><b>").append(this.trans("Limit Label")).append("</b></th>\n");
        }
        for (int gridCol = 0; gridCol < specLimitTypesData.getRowCount(); ++gridCol) {
            int colSpan = 0;
            int width = 0;
            if (this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_SEPARATORDISPLAY_COL)) {
                ++colSpan;
                width += 7;
            }
            if (this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR1DISPLAY_COL)) {
                ++colSpan;
                width += 66;
            }
            if (this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR2DISPLAY_COL)) {
                ++colSpan;
                width += 66;
            }
            if (this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE1DISPLAY_COL)) {
                ++colSpan;
                width += 73;
            }
            if (this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE2DISPLAY_COL)) {
                ++colSpan;
                width += 73;
            }
            String expandedId = "__" + this.elementid + "_header_collapsed" + gridCol;
            String collapsedId = "__" + this.elementid + "_header_expanded" + gridCol;
            theHTMLBuffer.append("<th rowspan=").append(iRowSpan).append(" class='gridmaint_fieldtitle speclimitscollapsed").append(gridCol).append("'  width=30 ");
            theHTMLBuffer.append(" id='").append(collapsedId).append("' ");
            theHTMLBuffer.append(" style='cursor: pointer;' ");
            theHTMLBuffer.append(" onmouseover='").append(JSOBJECT).append(".").append(JSONMOUSEOVER).append("(\"").append(this.elementid).append("\", ").append(gridCol).append(");' ");
            theHTMLBuffer.append(" onmouseout='").append(JSOBJECT).append(".").append(JSONMOUSEOUT).append("(\"").append(this.elementid).append("\", ").append(gridCol).append(");' ");
            theHTMLBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSEXPANDCOLLAPSE_STRUCTURED).append("( \"").append(this.elementid).append("\", ").append(gridCol).append(" );' ");
            theHTMLBuffer.append(" >");
            String vert_Text_Style = this.browser.isIE() ? "writing-mode: tb-rl;filter: fliph flipv;" : (this.browser.isWebkit() ? "position:relative; -webkit-transform: rotate(-90deg);" : "position:relative; -moz-transform: rotate(-90deg);");
            theHTMLBuffer.append("<table border=0 cellpadding=0 cellspacing=0 height='100%' ").append(!this.browser.isIE() ? "style='table-layout:fixed;width:30px;'" : "width='100%'").append("><tr><th nowrap valign=middle align=center style='").append(vert_Text_Style).append("' class='gridmaint_fieldtitle_inner'><b>");
            theHTMLBuffer.append(this.trans(specLimitTypesData.getValue(gridCol, SPECLIMITTYPE_LIMITTYPEID_COL, "&nbsp;")));
            theHTMLBuffer.append("</b></th><th width=10 valign=top align=right style='padding-top:2px;'>");
            theHTMLBuffer.append("<img align=right src='").append(IMG_EXPAND).append("' title='" + this.trans("Click to expand") + "' >");
            theHTMLBuffer.append("</th>");
            theHTMLBuffer.append("</tr></table>");
            theHTMLBuffer.append("</th>");
            theHTMLBuffer.append("<th colspan=").append(colSpan).append(" class='gridmaint_fieldtitle speclimitsexpanded").append(gridCol).append("' ");
            theHTMLBuffer.append(" id='").append(expandedId).append("' valign=middle ");
            theHTMLBuffer.append(" style='cursor: pointer;' ");
            theHTMLBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSEXPANDCOLLAPSE_STRUCTURED).append("( \"").append(this.elementid).append("\", ").append(gridCol).append(" );' ");
            theHTMLBuffer.append(" >");
            String title = specLimitTypesData.getValue(gridCol, SPECLIMITTYPE_LIMITTYPEID_COL, "&nbsp;");
            String sCond = specLimitTypesData.getValue(gridCol, SPECLIMITTYPE_CONDITION_COL, "");
            if (sCond.length() > 0) {
                title = title + " (" + sCond + ")";
            }
            if (this.getTranslationProcessor().isRTL()) {
                if (isCheckboxesAllowed) {
                    theHTMLBuffer.append("<table border=0 cellpadding=0 cellspacing=0 width='").append(width).append("' height='22'><tr><th valign=middle align=right class='gridmaint_fieldtitle_inner' nowrap><b>");
                } else {
                    theHTMLBuffer.append("<table border=0 cellpadding=0 cellspacing=0 width='").append(width).append("' height='100%'><tr><th valign=middle align=right class='gridmaint_fieldtitle_inner' nowrap><b>");
                }
            } else if (isCheckboxesAllowed) {
                theHTMLBuffer.append("<table border=0 cellpadding=0 cellspacing=0 width='").append(width).append("' height='22'><tr><th valign=middle align=left class='gridmaint_fieldtitle_inner' nowrap><b>");
            } else {
                theHTMLBuffer.append("<table border=0 cellpadding=0 cellspacing=0 width='").append(width).append("' height='100%'><tr><th valign=middle align=left class='gridmaint_fieldtitle_inner' nowrap><b>");
            }
            theHTMLBuffer.append("<div style=\"width:").append(width - 10).append("px;overflow:hidden;vertical-align:middle;\" title=\"").append(this.trans(title)).append("\">").append(this.trans(title)).append("</div>");
            if (isCheckboxesAllowed) {
                theHTMLBuffer.append("</b></th><th width=10 valign=top align=right style='padding-top:2px;'>");
            } else {
                theHTMLBuffer.append("</b></th><th width=10 valign=top align=right style='padding-top:1px;'>");
            }
            theHTMLBuffer.append("<img align=right src='").append(IMG_COLLAPSE).append("' title='" + this.trans("Click to collapse") + "' >");
            theHTMLBuffer.append("</th></tr></table>");
            theHTMLBuffer.append("</th>\n");
        }
        theHTMLBuffer.append("</tr>\n");
    }

    private void renderTableColumns_Structured(StringBuffer theHTMLBuffer, DataSet specParamLimitsData, DataSet specLimitTypesData, int gridCol, int gridRow, int dataRow, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String limitTypeSequence, String dataType, String entrySDCId, String entryRefType, boolean locked, boolean isViewOnly, ArrayList gridrowArrayList) {
        String columnId;
        boolean disabledVal2;
        boolean disabledOp2;
        boolean disabledVal1;
        boolean disabledOp1;
        String value2;
        String operator2;
        String value1;
        String operator1;
        if (specParamLimitsData != null && specParamLimitsData.getRowCount() > 0) {
            dataRow = this.getSpecParamLimitsRow(specParamLimitsData, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence);
            this.logger.debug("dataRow = " + dataRow);
            if (dataRow > -1) {
                this.logger.debug("Specparamlimts row found at " + dataRow);
                operator1 = this.getSpecParamLimitsOperator(specParamLimitsData, 1, dataRow);
                this.logger.debug("operator1 = " + operator1);
                value1 = this.getSpecParamLimitsValue(specParamLimitsData, 1, dataRow, dataType);
                this.logger.debug("value1 = " + value1);
                if (dataType.equalsIgnoreCase("n") || dataType.equalsIgnoreCase("nc") || dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("a") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
                    this.logger.debug("Data type is of type number or any therefore use value2 and operator2.");
                    operator2 = this.getSpecParamLimitsOperator(specParamLimitsData, 2, dataRow);
                    this.logger.debug("operator2 = " + operator2);
                    value2 = this.getSpecParamLimitsValue(specParamLimitsData, 2, dataRow, dataType);
                    this.logger.debug("value2 = " + value2);
                } else {
                    this.logger.debug("Data type is of type text, ref or sdc therefore do not use value2 and operator2.");
                    operator2 = "";
                    value2 = "";
                }
            } else {
                this.logger.debug("Could not find matching specparamlimts row therefore default to '' for all.");
                operator1 = "";
                operator2 = "";
                value1 = "";
                value2 = "";
            }
        } else {
            this.logger.debug("No specparamlimts data therefore default '' for all.");
            operator1 = "";
            operator2 = "";
            value1 = "";
            value2 = "";
        }
        boolean area1Vis = this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR1DISPLAY_COL);
        boolean area2Vis = this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE1DISPLAY_COL);
        boolean area3Vis = this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR2DISPLAY_COL);
        boolean area4Vis = this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE2DISPLAY_COL);
        boolean area5Vis = this.isLimitTypeAreaVisible(specLimitTypesData, gridCol, SPECLIMITTYPE_SEPARATORDISPLAY_COL);
        if (locked || isViewOnly) {
            disabledOp1 = true;
            disabledVal1 = true;
            disabledOp2 = true;
            disabledVal2 = true;
        } else {
            disabledOp1 = this.isLimitTypeAreaDisabled(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR1DISPLAY_COL);
            disabledVal1 = this.isLimitTypeAreaDisabled(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE1DISPLAY_COL);
            if (area1Vis && area2Vis && area3Vis && area4Vis) {
                this.logger.debug("All columns are visible therefore can use rules for disabling and enabling.");
                if (operator1.length() > 0) {
                    if (dataType.equalsIgnoreCase("n") || dataType.equalsIgnoreCase("nc") || dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("a") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
                        if (value1.length() > 0 && (operator1.equals(">") || operator1.equals(">="))) {
                            disabledOp2 = false;
                            disabledVal2 = operator2.length() <= 0 || !operator2.equals("<") && !operator2.equals("<=");
                        } else {
                            disabledOp2 = true;
                            disabledVal2 = true;
                            operator2 = "";
                            value2 = "";
                        }
                    } else {
                        disabledOp2 = true;
                        disabledVal2 = true;
                        operator2 = "";
                        value2 = "";
                    }
                    if (this.isLimitTypeAreaDisabled(specLimitTypesData, gridCol, SPECLIMITTYPE_OPERATOR2DISPLAY_COL)) {
                        disabledOp2 = true;
                    }
                    if (this.isLimitTypeAreaDisabled(specLimitTypesData, gridCol, SPECLIMITTYPE_VALUE2DISPLAY_COL)) {
                        disabledVal2 = true;
                    }
                } else {
                    disabledOp2 = true;
                    disabledVal2 = true;
                    value1 = "";
                    operator2 = "";
                    value2 = "";
                }
            } else {
                this.logger.debug("Not all columns are visible therefore cannot use normal rules for managing disabling and enabling.");
                disabledOp2 = true;
                disabledVal2 = true;
            }
        }
        if (area1Vis) {
            theHTMLBuffer.append("<td class='gridmaint_field speclimitsexpanded").append(gridCol).append("' valign='middle' nowrap ");
            columnId = "__" + this.elementid + "_col" + gridCol + "_row" + gridRow + "_item0";
            theHTMLBuffer.append(" id='").append(columnId).append("'>");
            if (dataRow > -1) {
                theHTMLBuffer.append(this.getSelectElement_Structured(dataRow, 1, operator1, dataType, disabledOp1, locked || isViewOnly, gridrowArrayList));
            }
            theHTMLBuffer.append("</td>\n");
        } else if (dataRow > -1) {
            theHTMLBuffer.append(this.getHiddenElement_Structured(dataRow, 1, "", SPECPARAMLIMITS_OPERATOR_COL));
        }
        if (area2Vis) {
            theHTMLBuffer.append("<td class='gridmaint_field speclimitsexpanded").append(gridCol).append("' valign='middle' nowrap ");
            columnId = "__" + this.elementid + "_col" + gridCol + "_row" + gridRow + "_item1";
            theHTMLBuffer.append(" id='").append(columnId).append("'>");
            if (dataRow > -1) {
                theHTMLBuffer.append(this.getInputElement_Structured(dataRow, 1, value1, dataType, entrySDCId, entryRefType, disabledVal1, locked || isViewOnly, gridrowArrayList));
            }
            theHTMLBuffer.append("</td>\n");
        } else if (dataRow > -1) {
            theHTMLBuffer.append(this.getHiddenElement_Structured(dataRow, 1, "", SPECPARAMLIMITS_VALUE_COL));
        }
        if (area3Vis) {
            theHTMLBuffer.append("<td class='gridmaint_field speclimitsexpanded").append(gridCol).append("' valign='middle' nowrap ");
            columnId = "__" + this.elementid + "_col" + gridCol + "_row" + gridRow + "_item2";
            theHTMLBuffer.append(" id='").append(columnId).append("'>");
            if (dataRow > -1) {
                theHTMLBuffer.append(this.getSelectElement_Structured(dataRow, 2, operator2, dataType, disabledOp2, locked || isViewOnly, gridrowArrayList));
            }
            theHTMLBuffer.append("</td>\n");
        } else if (dataRow > -1) {
            theHTMLBuffer.append(this.getHiddenElement_Structured(dataRow, 2, "", SPECPARAMLIMITS_OPERATOR_COL));
        }
        if (area4Vis) {
            theHTMLBuffer.append("<td class='gridmaint_field speclimitsexpanded").append(gridCol).append("' valign='middle' nowrap ");
            columnId = "__" + this.elementid + "_col" + gridCol + "_row" + gridRow + "_item3";
            theHTMLBuffer.append(" id='").append(columnId).append("'>");
            if (dataRow > -1) {
                theHTMLBuffer.append(this.getInputElement_Structured(dataRow, 2, value2, dataType, entrySDCId, entryRefType, disabledVal2, locked || isViewOnly, gridrowArrayList));
            }
            theHTMLBuffer.append("</td>\n");
        } else if (dataRow > -1) {
            theHTMLBuffer.append(this.getHiddenElement_Structured(dataRow, 2, "", SPECPARAMLIMITS_VALUE_COL));
        }
        if (area5Vis) {
            columnId = "__" + this.elementid + "_col" + gridCol + "_row" + gridRow + "_item4";
            theHTMLBuffer.append("<td class='gridmaint_fieldtitle speclimitsexpanded").append(gridCol).append("' style='width:5px;' id='");
            theHTMLBuffer.append(columnId).append("'>&nbsp;</td>");
        }
        theHTMLBuffer.append(this.getHiddenElement_Structured(dataRow, -1, dataType, "datatypes"));
    }

    private int findParameterListRowSpan(DataSet specParamItemsData, int row, String currentKey) {
        this.logger.debug("currentKey = " + currentKey);
        int theReturn = 0;
        for (int index = row; index < specParamItemsData.getRowCount(); ++index) {
            String paramListId = specParamItemsData.getValue(index, "paramlistid", "");
            String paramListVersionId = specParamItemsData.getValue(index, "paramlistversionid", "");
            String variantId = specParamItemsData.getValue(index, "variantid", "");
            String anyFlag = specParamItemsData.getValue(index, SPECPARAMITEMS_ALLOWANYPARAMLISTFLAG_COL, "N");
            String key = paramListId + '-' + paramListVersionId + '-' + variantId + '-' + anyFlag;
            this.logger.debug("key = " + key);
            if (!key.equalsIgnoreCase(currentKey)) break;
            ++theReturn;
        }
        this.logger.debug("theReturn = " + theReturn);
        return theReturn;
    }

    private void renderTableColumnHeader_Structured(StringBuffer theHTMLBuffer, DataSet specParamItemsData, int row, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String any, String currentKey, String prevKey, boolean isRowSpanEnabled, boolean isCheckboxesAllowed, boolean isLocked) {
        if (isCheckboxesAllowed) {
            if (!isLocked) {
                theHTMLBuffer.append("<td nowrap class='gridmaint_field'>");
                theHTMLBuffer.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(DATASET_SPECPARAMLIMITS).append(row).append("\" ");
                theHTMLBuffer.append(" value=\"__").append(DATASET_SPECPARAMLIMITS).append(row).append("\" ");
                theHTMLBuffer.append(" onclick=\"").append(JSOBJECT).append(".").append(JSONCLICK_CHECKBOX).append("('").append(this.elementid).append("', '").append(DATASET_SPECPARAMLIMITS).append("', ").append(row).append(" );\" >");
                theHTMLBuffer.append("</td>");
            } else {
                theHTMLBuffer.append("<td nowrap class='maint_lockedfield'>");
                theHTMLBuffer.append("&nbsp;");
                theHTMLBuffer.append("</td>");
            }
        }
        String className = isLocked ? "maint_lockedfield" : "maintform_field_blue";
        if (isRowSpanEnabled) {
            this.logger.debug("Row spanning in use....");
            if (prevKey.length() == 0 || !currentKey.equals(prevKey)) {
                this.logger.debug("Previous key is empty or diffent to current key. Therefore refind row span and draw column...");
                int iRowSpan = this.findParameterListRowSpan(specParamItemsData, row, currentKey);
                theHTMLBuffer.append("<th class='").append(className).append("' style='text-align:left;'");
                theHTMLBuffer.append(" rowspan=").append(iRowSpan);
                theHTMLBuffer.append(" id='__").append(this.elementid).append("_paramlist_row").append(row).append("' ");
                theHTMLBuffer.append(" nowrap valign=middle ");
                String version = "<font style='color:black;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
                String variant = "<font style='color:black;'>" + SafeHTML.encodeForHTML(variantId) + "</font>";
                if (any.equalsIgnoreCase("Y")) {
                    theHTMLBuffer.append(" style='color:gray;' ");
                    version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
                } else if (any.equalsIgnoreCase("V")) {
                    theHTMLBuffer.append(" style='color:black;' ");
                    version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
                } else if (any.equalsIgnoreCase("A")) {
                    theHTMLBuffer.append(" style='color:black;' ");
                    version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
                    variant = "<font style='color:gray;'>" + SafeHTML.encodeForHTML(variantId) + "</font>";
                } else {
                    theHTMLBuffer.append(" style='color:black;' ");
                }
                theHTMLBuffer.append(" >");
                theHTMLBuffer.append(SafeHTML.encodeForHTML(paramListId)).append(" ").append(version).append(" ").append(variant);
                theHTMLBuffer.append("</th>\n");
            } else {
                this.logger.debug("Previous key is not empty and is the same as the current key therefore do not draw column as we are still in a row span.");
            }
        } else {
            this.logger.debug("No row spanning in use....");
            theHTMLBuffer.append("<th class='").append(className).append("' style='text-align:left;'");
            theHTMLBuffer.append(" id='__").append(this.elementid).append("_paramlist_row").append(row).append("' ");
            theHTMLBuffer.append(" nowrap valign=middle ");
            String version = "<font style='color:black;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
            String variant = "<font style='color:black;'>" + SafeHTML.encodeForHTML(variantId) + "</font>";
            if (any.equalsIgnoreCase("Y")) {
                theHTMLBuffer.append(" style='color:gray;' ");
                version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
            } else if (any.equalsIgnoreCase("V")) {
                theHTMLBuffer.append(" style='color:black;' ");
                version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
            } else if (any.equalsIgnoreCase("A")) {
                theHTMLBuffer.append(" style='color:black;' ");
                version = "<font style='color:gray;'>(v" + SafeHTML.encodeForHTML(paramListVersionId) + ")</font>";
                variant = "<font style='color:gray;'>" + SafeHTML.encodeForHTML(variantId) + "</font>";
            } else {
                theHTMLBuffer.append(" style='color:black;' ");
            }
            theHTMLBuffer.append(" >");
            theHTMLBuffer.append(SafeHTML.encodeForHTML(paramListId)).append(" ").append(version).append(" ").append(variant);
            theHTMLBuffer.append("</th>\n");
        }
        theHTMLBuffer.append("<th class='").append(className).append("' style='text-align:left;' valign=middle ");
        theHTMLBuffer.append(" id='__").append(this.elementid).append("_param_row").append(row).append("' ");
        theHTMLBuffer.append(" nowrap >");
        theHTMLBuffer.append(SafeHTML.encodeForHTML(paramId)).append(" (").append(SafeHTML.encodeForHTML(paramType)).append(")");
        theHTMLBuffer.append("</th>\n");
    }

    private void renderHintDiv(StringBuffer theHTMLBuffer) {
        theHTMLBuffer.append("<div id='__").append(this.elementid).append("_hint' ");
        theHTMLBuffer.append("style='position:absolute;display:none;width=10;height=10;border:solid 1px black;background-color:#FFFFE1;'>");
        theHTMLBuffer.append("</div>");
    }

    private void renderScriptAndStyle(StringBuffer theHTMLBuffer, String specid, String specver, boolean isExpanded, int limittypecount) {
        theHTMLBuffer.append("<script type='text/javascript' src='").append(JSFILE).append("'></script>\n");
        theHTMLBuffer.append("<script type='text/javascript'>\n");
        theHTMLBuffer.append(JSOBJECT).append(".").append(JSSPECID).append(" = ").append("'").append(specid).append("';\n");
        theHTMLBuffer.append(JSOBJECT).append(".").append(JSSPECVERSIONID).append(" = ").append("'").append(specver).append("';\n");
        theHTMLBuffer.append(JSOBJECT).append(".").append(JSCONNECTIONID).append(" = ").append("'").append(this.requestContext.getConnectionId()).append("';\n");
        theHTMLBuffer.append(JSOBJECT).append(".").append(JSELEMENTID).append(" = ").append("'").append(this.elementid).append("';\n");
        theHTMLBuffer.append(JSOBJECT).append(".").append(JSDATASETNAME).append(" = ").append("'").append(DATASET_SPECPARAMLIMITS).append("';\n");
        theHTMLBuffer.append("var fieldWidth = '").append(this.fieldWidth).append("';");
        theHTMLBuffer.append("</script>\n");
        theHTMLBuffer.append("<style id='speclimitscss'>\n");
        for (int index = 0; index < limittypecount; ++index) {
            theHTMLBuffer.append(".speclimitsexpanded").append(index).append("{\n");
            if (isExpanded) {
                theHTMLBuffer.append("display:" + this.getTableCellDisplayStyle() + ";\n");
            } else {
                theHTMLBuffer.append("display:none;\n");
            }
            theHTMLBuffer.append("}\n");
            theHTMLBuffer.append(".speclimitscollapsed").append(index).append("{\n");
            if (isExpanded) {
                theHTMLBuffer.append("display:none;\n");
            } else {
                theHTMLBuffer.append("display:" + this.getTableCellDisplayStyle() + ";\n");
            }
            theHTMLBuffer.append("}\n");
        }
        theHTMLBuffer.append("</style>\n");
    }

    private String getTableCellDisplayStyle() {
        String style = "";
        style = this.browser.isIE() && this.browser.getVersion() < 9.0 ? "block" : "table-cell";
        return style;
    }

    private void prepareCombos() {
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("dropdownvalues", "<;<=;=;>=;>");
        inputProps.setProperty("dropdowncomboid", "numericoperators");
        sdiTagUtil.collectDropDownComboInfo(inputProps, this.sdiInfo, this.pageContext);
        inputProps = new PropertyList();
        inputProps.setProperty("dropdownvalues", "In;Not In");
        inputProps.setProperty("dropdowncomboid", "textoperators");
        sdiTagUtil.collectDropDownComboInfo(inputProps, this.sdiInfo, this.pageContext);
        inputProps = new PropertyList();
        inputProps.setProperty("dropdownvalues", "<;<=;=;>=;>;In;Not In");
        inputProps.setProperty("dropdowncomboid", "anyoperators");
        sdiTagUtil.collectDropDownComboInfo(inputProps, this.sdiInfo, this.pageContext);
    }

    private boolean renderStructuredHTML(StringBuffer theHTMLBuffer, String specId, String specVersionId, DataSet specParamItemsData, DataSet specLimitTypesData, DataSet specParamLimitsData, boolean locked, boolean isExpanded, boolean isRowSpanEnabled, boolean isCheckboxesAllowed, boolean isViewOnly, ArrayList gridArrayList) {
        this.renderScriptAndStyle(theHTMLBuffer, specId, specVersionId, isExpanded, specLimitTypesData.getRowCount());
        this.renderHintDiv(theHTMLBuffer);
        this.prepareCombos();
        this.renderHiddenTableElements_All(theHTMLBuffer, specParamLimitsData);
        theHTMLBuffer.append("<table border=1 cellSpacing='0' id='").append(this.elementid).append("_table' class='gridmaint_table'>\n");
        this.renderTableHeaderRow_Structured(theHTMLBuffer, specLimitTypesData, specParamItemsData.getRowCount() + 1, isCheckboxesAllowed);
        String prevParamListKey = "";
        for (int gridRow = 0; gridRow < specParamItemsData.getRowCount(); ++gridRow) {
            ArrayList gridrowArrayList = new ArrayList();
            String paramListId = specParamItemsData.getValue(gridRow, "paramlistid", "");
            String paramListVersionId = specParamItemsData.getValue(gridRow, "paramlistversionid", "");
            String variantId = specParamItemsData.getValue(gridRow, "variantid", "");
            String paramId = specParamItemsData.getValue(gridRow, "paramid", "");
            String paramType = specParamItemsData.getValue(gridRow, "paramtype", "");
            String anyFlag = specParamItemsData.getValue(gridRow, SPECPARAMITEMS_ALLOWANYPARAMLISTFLAG_COL, "N");
            String dataType = specParamItemsData.getValue(gridRow, "datatypes", "");
            String entryRefTypeId = specParamItemsData.getValue(gridRow, SPECPARAMITEMS_ENTRYREFTYPEID_COL, "");
            String entrySDCId = specParamItemsData.getValue(gridRow, SPECPARAMITEMS_ENTRYSDCID_COL, "");
            String currParamListKey = paramListId + '-' + paramListVersionId + '-' + variantId + '-' + anyFlag;
            this.logger.debug("currParamListKey = " + currParamListKey);
            theHTMLBuffer.append("<tr valign='top' height=8 id='__").append(this.elementid).append(gridRow).append("_row' >\n");
            this.renderTableColumnHeader_Structured(theHTMLBuffer, specParamItemsData, gridRow, paramListId, paramListVersionId, variantId, paramId, paramType, anyFlag, currParamListKey, prevParamListKey, isRowSpanEnabled, isCheckboxesAllowed, locked);
            theHTMLBuffer.append(" <td valign='middle' ").append(" id='__").append(this.elementid).append("_limitlabel_row").append(gridRow).append("'> ");
            theHTMLBuffer.append("<input type=text value=\"").append(HttpUtil.htmlEncode(specParamItemsData.getString(gridRow, "limitlabel", ""))).append("\" ");
            if (locked || isViewOnly) {
                theHTMLBuffer.append("readonly ");
                theHTMLBuffer.append("style=\"border:solid 1px #C9C7BA;\"");
            } else {
                theHTMLBuffer.append("onchange= \"").append(JSOBJECT).append(".").append(JSONCHANGE_STRUCTURED);
                theHTMLBuffer.append("( '', '").append(this.elementid).append("', '").append(DATASET_SPECPARAMITEMS).append("' );\" ");
            }
            theHTMLBuffer.append(" name='specparamitems").append(gridRow).append("_limitlabel'  id='specparamitems").append(gridRow).append("_limitlabel' >");
            theHTMLBuffer.append("</td>\n");
            for (int gridCol = 0; gridCol < specLimitTypesData.getRowCount(); ++gridCol) {
                String limitTypeSequence = specLimitTypesData.getValue(gridCol, "limittypesequence", "");
                int dataRow = this.getSpecParamLimitsRow(specParamLimitsData, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence);
                this.renderHiddenRowElements_All(theHTMLBuffer, dataRow);
                this.renderTableColumns_Structured(theHTMLBuffer, specParamLimitsData, specLimitTypesData, gridCol, gridRow, dataRow, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence, dataType, entrySDCId, entryRefTypeId, locked, isViewOnly, gridrowArrayList);
            }
            theHTMLBuffer.append("</tr>\n");
            gridArrayList.add(gridrowArrayList);
            prevParamListKey = currParamListKey;
        }
        theHTMLBuffer.append("</table>");
        theHTMLBuffer.append("<script>\n");
        theHTMLBuffer.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
        theHTMLBuffer.append("\n</script>\n");
        this.renderNoRows(theHTMLBuffer, specParamItemsData);
        boolean theReturn = true;
        return theReturn;
    }

    private void renderNoRows(StringBuffer html, DataSet dataset) {
        String display = dataset.getRowCount() == 0 ? "block" : "none";
        html.append("<div id=\"__").append(this.elementid).append("_norows\" style=\"display:").append(display).append(";\" >");
        if (this.pageContext != null) {
            html.append(this.getTranslationProcessor().translate("No records found"));
        } else {
            html.append("No records found");
        }
        html.append("</div>");
    }

    private String getSpecParamLimitsOperator(DataSet specParamLimitsData, int num, int row) {
        String theReturn;
        if (row > -1) {
            theReturn = specParamLimitsData.getValue(row, SPECPARAMLIMITS_OPERATOR_COL + num, "");
        } else {
            this.logger.debug("Could not find operator therefore default to ''.");
            theReturn = "";
        }
        return theReturn;
    }

    private String getSpecParamLimitsValue(DataSet specParamLimitsData, int num, int row, String datatype) {
        String sReturn = null;
        if (row > -1) {
            String valuestring = specParamLimitsData.getValue(row, SPECPARAMLIMITS_VALUE_COL + num, "");
            if (datatype.equalsIgnoreCase("N") && !specParamLimitsData.getValue(row, "operator1", "").equals("In") && !specParamLimitsData.getValue(row, "operator1", "").equals("Not In") || datatype.equalsIgnoreCase("NC") || datatype.equalsIgnoreCase("A")) {
                String valuenum = specParamLimitsData.getValue(row, SPECPARAMLIMITS_VALUE_COL + num + "num", "");
                if (valuestring.length() > 0) {
                    if (valuenum.length() > 0) {
                        if (valuestring.indexOf("/") > -1 && valuestring.length() > 2) {
                            sReturn = valuestring;
                        } else {
                            try {
                                BigDecimal bs = this.m18server.parseBigDecimal(valuestring);
                                BigDecimal bn = specParamLimitsData.getBigDecimal(row, SPECPARAMLIMITS_VALUE_COL + num + "num");
                                sReturn = this.m18client.format(bn.setScale(bs.scale()), false, false);
                            }
                            catch (Exception e) {
                                this.logger.warn("Could not convert " + valuestring + " precision to number " + valuenum + ".");
                                sReturn = valuenum;
                            }
                        }
                    } else {
                        sReturn = valuestring;
                    }
                } else {
                    sReturn = valuenum;
                }
            } else if (datatype.equalsIgnoreCase("N") && (specParamLimitsData.getValue(row, "operator1", "").equals("In") || specParamLimitsData.getValue(row, "operator1", "").equals("Not In"))) {
                if (valuestring.length() > 0) {
                    String[] valueArr = valuestring.split(";");
                    for (int i = 0; i < valueArr.length; ++i) {
                        if (valueArr[i].length() <= 0) continue;
                        if (valueArr[i].indexOf("/") > -1 && valueArr[i].length() > 2) {
                            if (i == 0) {
                                sReturn = valueArr[i];
                                continue;
                            }
                            sReturn = sReturn + ";" + valueArr[i];
                            continue;
                        }
                        try {
                            BigDecimal bs = this.m18server.parseBigDecimal(valueArr[i]);
                            if (i == 0) {
                                sReturn = this.m18client.format(bs, false, false);
                                continue;
                            }
                            sReturn = sReturn + ";" + this.m18client.format(bs, false, false);
                            continue;
                        }
                        catch (Exception e) {
                            this.logger.warn("Could not convert " + valueArr[i] + " precision to number.");
                            sReturn = valueArr[i];
                        }
                    }
                } else {
                    sReturn = "";
                }
            } else {
                sReturn = valuestring;
            }
        } else {
            sReturn = "";
            this.logger.debug("Could not find value therefore default to ''.");
        }
        return sReturn;
    }

    private int getSpecParamLimitsRow(DataSet specParamLimitsData, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String limitTypeSequence) {
        int row;
        boolean found = false;
        for (row = 0; row < specParamLimitsData.getRowCount(); ++row) {
            String curr_ParamListId = specParamLimitsData.getValue(row, "paramlistid", "");
            String curr_ParamListVersionId = specParamLimitsData.getValue(row, "paramlistversionid", "");
            String curr_VariantId = specParamLimitsData.getValue(row, "variantid", "");
            String curr_ParamId = specParamLimitsData.getValue(row, "paramid", "");
            String curr_ParamType = specParamLimitsData.getValue(row, "paramtype", "");
            String curr_LimitTypeSequence = specParamLimitsData.getValue(row, "limittypesequence", "");
            if (!curr_ParamListId.equals(paramListId) || !curr_ParamListVersionId.equals(paramListVersionId) || !curr_VariantId.equals(variantId) || !curr_ParamId.equals(paramId) || !curr_ParamType.equals(paramType) || !curr_LimitTypeSequence.equals(limitTypeSequence)) continue;
            found = true;
            this.logger.debug("Found match at row " + row);
            break;
        }
        if (!found) {
            this.logger.warn("Could not find matching row!");
            row = -1;
        }
        return row;
    }

    private void renderHiddenRowElements_All(StringBuffer buffer, int dataRow) {
        this.logger.debug("element.getId() = " + this.element.getId());
        if (dataRow > -1) {
            this.sdiInfo.getQueryData(DATASET_SPECPARAMLIMITS).setCurrentRow(dataRow);
            buffer.append(SDITagUtil.getRepeatedRowInputs(DATASET_SPECPARAMLIMITS, new String[]{SPECPARAMLIMITS_SPECID_COL, SPECPARAMLIMITS_SPECVERSIONID_COL, "paramlistid", "paramlistversionid", "variantid", "paramid", "paramtype", "limittypesequence"}, this.sdiInfo.getQueryData(DATASET_SPECPARAMLIMITS), "", "", 0));
        } else {
            this.logger.info("No specparamlimits row therefore cannot add dataset hidden row elements.");
        }
    }

    private void renderHiddenTableElements_All(StringBuffer buffer, DataSet specParamLimitsData) {
        buffer.append(SDITagUtil.getFixedRowInputs(DATASET_SPECPARAMLIMITS, specParamLimitsData.getColumns(), specParamLimitsData.getRowCount(), "", "|"));
    }

    private String getSelectElement_Structured(int rowNum, int num, String value, String dataType, boolean readOnly, boolean locked, ArrayList gridrowArrayList) {
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("data", DATASET_SPECPARAMLIMITS);
        inputProps.setProperty("mode", "dropdowncombo");
        inputProps.setProperty(SPECPARAMLIMITS_VALUE_COL, value);
        inputProps.setProperty("row", "" + rowNum);
        if (!locked) {
            inputProps.setProperty("onchange", "specLimits.doChange_Structured( '', '" + this.elementid + "', '" + DATASET_SPECPARAMLIMITS + "' );");
        }
        inputProps.setProperty("columnid", SPECPARAMLIMITS_OPERATOR_COL + num);
        inputProps.setProperty("readonly", "true");
        if (readOnly) {
            inputProps.setProperty(PROPERTY_STYLE, "width:45px;background-color:#DCDCDC;border:solid 1px #C9C7BA;color:#686868;" + (locked ? "-webkit-text-fill-color::#686868; opacity: 1;" : ""));
        } else {
            inputProps.setProperty(PROPERTY_STYLE, "width:45px;");
        }
        if (locked) {
            inputProps.setProperty("disabled", "true");
        }
        SDITagUtil.setIdentifierAttributes(inputProps, this.sdiInfo);
        gridrowArrayList.add(inputProps.getProperty("name"));
        if (dataType.equalsIgnoreCase("nc") || dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
            this.logger.debug("Data type numeric (nc, n or d or o) thus use full expressions...");
            inputProps.setProperty("dropdownvalues", "<;<=;=;>=;>");
            inputProps.setProperty("dropdowncomboid", "numericoperators");
        } else if (dataType.equalsIgnoreCase("a") || dataType.equalsIgnoreCase("n")) {
            if (num == 1) {
                this.logger.debug("Data type any (a) or (n) on num 1 thus use full expressions plus text expressions...");
                inputProps.setProperty("dropdownvalues", "<;<=;=;>=;>;In;Not In");
                inputProps.setProperty("dropdowncomboid", "anyoperators");
            } else {
                this.logger.debug("Data type any (a) on num 2 thus use full expressions minus text expressions...");
                inputProps.setProperty("dropdownvalues", "<;<=;=;>=;>");
                inputProps.setProperty("dropdowncomboid", "numericoperators");
            }
        } else {
            this.logger.debug("Data type text based (t, s, v or r) thus use text expressions...");
            inputProps.setProperty("dropdownvalues", "In;Not In");
            inputProps.setProperty("dropdowncomboid", "textoperators");
        }
        StringBuffer returnBuffer = new StringBuffer();
        returnBuffer.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
        return returnBuffer.toString();
    }

    private String getInputElement_Structured(int rowNum, int num, String value, String dataType, String entrySDCId, String entryRefType, boolean readOnly, boolean locked, ArrayList gridrowArrayList) {
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("data", DATASET_SPECPARAMLIMITS);
        inputProps.setProperty(SPECPARAMLIMITS_VALUE_COL, value);
        inputProps.setProperty("columnid", SPECPARAMLIMITS_VALUE_COL + num);
        inputProps.setProperty("row", "" + rowNum);
        SDITagUtil.setIdentifierAttributes(inputProps, this.sdiInfo);
        if (!locked) {
            inputProps.setProperty("onchange", "specLimits.doChange_Structured( '" + dataType + "', '" + this.elementid + "', '" + DATASET_SPECPARAMLIMITS + "' );");
            inputProps.setProperty("onblur", "specLimits.doBlur_Structured( '" + dataType + "' );");
        }
        gridrowArrayList.add(inputProps.getProperty("name"));
        if (dataType.equalsIgnoreCase("nc") || dataType.equalsIgnoreCase("n")) {
            this.logger.debug("Data type numeric (nc, n) thus use full expressions...");
            inputProps.setProperty("mode", "input");
            if (readOnly || locked) {
                inputProps.setProperty("readonly", "true");
                inputProps.setProperty(PROPERTY_STYLE, "width:" + this.fieldWidth + "px;background-color:#DCDCDC;border:solid 1px #C9C7BA;color:#686868;");
            } else {
                inputProps.setProperty(PROPERTY_STYLE, "width:" + this.fieldWidth + "px;");
            }
        } else {
            this.logger.debug("Data type text based (t, s, v, r or a) thus use text expressions...");
            inputProps.setProperty("mode", "input");
            if (dataType.equalsIgnoreCase("t") || dataType.equalsIgnoreCase("tc") || dataType.equalsIgnoreCase("a")) {
                if (readOnly || locked) {
                    inputProps.setProperty("readonly", "true");
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + this.fieldWidth + "px;background-color:#DCDCDC;border:solid 1px #C9C7BA;color:#686868;");
                } else {
                    inputProps.setProperty("readonly", "false");
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + this.fieldWidth + "px;");
                }
            } else if (dataType.equalsIgnoreCase("r") || dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
                if (readOnly || locked) {
                    inputProps.setProperty("readonly", "true");
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + (Integer.parseInt(this.fieldWidth) - 24) + "px;background-color:#DCDCDC;border:solid 1px #C9C7BA;color:#686868;");
                } else {
                    inputProps.setProperty("readonly", "false");
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + (Integer.parseInt(this.fieldWidth) - 24) + "px;");
                }
            } else {
                inputProps.setProperty("readonly", "true");
                if (readOnly || locked) {
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + (Integer.parseInt(this.fieldWidth) - 24) + "px;background-color:#DCDCDC;border:solid 1px #C9C7BA;color:#686868;");
                } else {
                    inputProps.setProperty(PROPERTY_STYLE, "width:" + (Integer.parseInt(this.fieldWidth) - 24) + "px;");
                }
            }
        }
        inputProps.setProperty("extraattributes", "u_datatype=" + dataType + ";u_ref=" + entryRefType + ";u_sdc=" + entrySDCId);
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        StringBuffer returnBuffer = new StringBuffer();
        returnBuffer.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
        if (dataType.equalsIgnoreCase("r") || dataType.equalsIgnoreCase("v") || dataType.equalsIgnoreCase("s")) {
            String name = inputProps.getProperty("name");
            if (readOnly) {
                returnBuffer.append("<img id='").append(name).append("_image").append("' border=0 u_disabled='Y' width=24 height=16 src='WEB-CORE/elements/images/lookup.gif' style='").append(this.browser.isIE() ? "filter:gray" : "opacity: 0.3;").append(";cursor:default;' ");
                returnBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSLOOKUPCLICK).append("(\"").append(name).append("\", \"").append(dataType).append("\", \"").append(entrySDCId).append("\", \"").append(entryRefType).append("\");'>");
            } else {
                returnBuffer.append("<img id='").append(name).append("_image").append("' border=0 u_disabled='N' width=24 height=16 src='WEB-CORE/elements/images/lookup.gif' style='cursor: pointer;' ");
                returnBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSLOOKUPCLICK).append("(\"").append(name).append("\", \"").append(dataType).append("\", \"").append(entrySDCId).append("\", \"").append(entryRefType).append("\");'>");
            }
        } else if (dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
            String name = inputProps.getProperty("name");
            if (readOnly) {
                returnBuffer.append("<img id='").append(name).append("_image").append("' border=0 u_disabled='Y' width=24 height=16 src='WEB-CORE/elements/images/lookup_date.gif' style='").append(this.browser.isIE() ? "filter:gray" : "opacity: 0.3;").append(";cursor:default;' ");
                returnBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSLOOKUPCLICK).append("(\"").append(name).append("\", \"").append(dataType).append("\", \"\", \"\");'>");
            } else {
                returnBuffer.append("<img id='").append(name).append("_image").append("' border=0 u_disabled='N' width=24 height=16 src='WEB-CORE/elements/images/lookup_date.gif' style='cursor: pointer;' ");
                returnBuffer.append(" onclick='").append(JSOBJECT).append(".").append(JSLOOKUPCLICK).append("(\"").append(name).append("\", \"").append(dataType).append("\", \"\", \"\");'>");
            }
        }
        return returnBuffer.toString();
    }

    private String getHiddenElement_Structured(int rowNum, int num, String value, String columnid) {
        this.logger.debug("getHiddenElement_Structured called...");
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("data", DATASET_SPECPARAMLIMITS);
        inputProps.setProperty(SPECPARAMLIMITS_VALUE_COL, value);
        if (num > 0) {
            inputProps.setProperty("columnid", columnid + num);
        } else {
            inputProps.setProperty("columnid", columnid);
        }
        inputProps.setProperty("row", "" + rowNum);
        inputProps.setProperty("mode", "hidden");
        SDITagUtil.setIdentifierAttributes(inputProps, this.sdiInfo);
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        StringBuffer returnBuffer = new StringBuffer();
        returnBuffer.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
        return returnBuffer.toString();
    }

    private boolean renderBandedHTML(StringBuffer theHTMLBuffer) {
        theHTMLBuffer.append("Banded");
        return true;
    }

    private boolean checkLockState() {
        boolean theReturn;
        try {
            DataSet data = this.sdiInfo.getDataSet(DATASET_SPECPARAMLIMITS);
            String lockedBy = data.getValue(0, "__lockedby", "");
            theReturn = lockedBy != null && lockedBy.length() != 0;
        }
        catch (Exception e) {
            theReturn = true;
            this.logger.warn("Could not obtain lock information therefore default to locked.");
        }
        return theReturn;
    }

    private void renderButtons(StringBuffer html, String theButtonPlacement) {
        PropertyListCollection buttons = this.element.getCollection(PROPERTY_BUTTONS);
        if (buttons != null && buttons.size() > 0) {
            this.logger.debug("About to render " + buttons.size() + " buttons...");
            if (theButtonPlacement.endsWith("middle")) {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=center>\n<tr>\n");
            } else if (theButtonPlacement.endsWith("right")) {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=right>\n<tr>\n");
            } else {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=left>\n<tr>\n");
            }
            for (int i = 0; i < buttons.size(); ++i) {
                PropertyList buttonProps = buttons.getPropertyList(i);
                if (!buttonProps.getProperty("show", "Y").equals("Y")) continue;
                html.append("<td>\n");
                Button button = new Button(this.pageContext);
                button.setId(buttonProps.getProperty(PROPERTY_ID));
                button.setText(buttonProps.getProperty(PROPERTY_TEXT));
                button.setImg(buttonProps.getProperty(PROPERTY_IMG));
                button.setTip(buttonProps.getProperty(PROPERTY_TIP));
                button.setAppearance(buttonProps.getProperty(PROPERTY_APPEARANCE));
                button.setMargin(buttonProps.getProperty(PROPERTY_MARGIN));
                button.setStyle(buttonProps.getProperty(PROPERTY_STYLE));
                button.setWidth(buttonProps.getProperty("width"));
                String js = buttonProps.getProperty(PROPERTY_JS);
                this.logger.debug("js = " + js);
                if (js.length() == 0) {
                    js = "noop();";
                }
                button.setAction(js);
                html.append(button.getHtml());
                html.append("</td>\n");
            }
            html.append("</tr>\n</table>\n");
        } else {
            this.logger.info("No buttons found.");
        }
    }

    @Override
    public String getHtml() {
        StringBuffer theHTMLBuffer = new StringBuffer();
        String theReturn = "";
        if (this.loadProperties()) {
            this.m18client = new M18NUtil(this.connectionInfo);
            this.m18server = new M18NUtil();
            boolean locked = this.checkLockState();
            if (this.renderHTML(theHTMLBuffer, this.keyId1, this.keyId2, locked, this.expanded, this.enableRowSpan, this.showCheckboxes, this.buttonPlacement, this.viewOnly)) {
                if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
                    if (theHTMLBuffer.length() > 0) {
                        theReturn = theHTMLBuffer.toString();
                    }
                } else {
                    theReturn = this.getError();
                }
            } else {
                theReturn = this.getError();
            }
        } else {
            this.logger.error("Could not load required properties.");
            theReturn = this.getError();
        }
        return theReturn;
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }
}

