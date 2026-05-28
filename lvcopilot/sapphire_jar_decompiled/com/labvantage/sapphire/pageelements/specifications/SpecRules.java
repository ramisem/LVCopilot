/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.specifications;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SpecRules
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
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
    static final String SPECCONDITION_REFTYPE = "Spec Condition";
    static final String BANDEDRULETYPE = "B";
    static final String STRUCTUREDRULETYPE = "S";
    static final String CUSTOMRULETYPE = "C";
    static final String EXPRESSIONRULETYPE = "E";
    static final String JSFILE = "WEB-CORE/elements/scripts/specrules.js";
    static final String JSOBJECT = "specRules";
    static final String JSONCLICK_CHECKBOX = "doCheckClick";
    static final String JSONCHANGE_SELECT = "doSelectOnChange";
    static final String JSONCHANGE_INPUT = "doInputOnChange";
    static final String JSBUTTON_ADD = "addRule";
    static final String JSBUTTON_REMOVE = "removeRule";
    static final String JSBUTTON_MOVEUP = "moveRuleUp";
    static final String JSBUTTON_MOVEDOWN = "moveRuleDown";
    static final String JSBUTTON_LOCKED = "lockedCall";
    static final String JSBUTTON_INVALIDRULETYPE = "invalidRuleTypeCall";
    static final String JSBUTTON_NOOP = "noop";
    static final String JSRULETYPE = "sRuleType";
    static final String JSELEMENTID = "sElementId";
    static final String JSDATASETNAME = "sDataSetName";
    static final String DATASET_SPECRULE = "specrule";
    static final String SPECRULE_RULEDEF = "ruledef";
    static final String SPECRULE_RULENO = "ruleno";
    static final String[] OPTIONS_ALL = new String[]{"", "all spec results are entered and", "all mandatory spec results are entered and"};
    static final String[] VALUES_ALL = new String[]{"", "A", "M"};
    static final String[] OPTIONS_ITEMS = new String[]{"", "any", "at least 2", "at least 3", "at least 4", "at least 5", "all"};
    static final String[] VALUES_ITEMS = new String[]{"", "1", "2", "3", "4", "5", "A"};
    static final String[] OPTIONS_MANDATORY = new String[]{"", "mandatory"};
    static final String[] VALUES_MANDATORY = new String[]{"N", "Y"};
    static final String[] OPTIONS_OPERATOR = new String[]{"", "are", "are not"};
    static final String[] VALUES_OPERATOR = new String[]{"", "Y", "N"};
    private String ruleType;
    private String keyId1;
    private String keyId2;
    private String buttonPlacement;
    private boolean viewOnly;

    private boolean loadProperties() {
        this.logger.info("loadProperties called...");
        boolean theReturn = false;
        PageTagInfo pageInfo = new PageTagInfo(this.pageContext, this.requestContext);
        this.keyId1 = pageInfo.getProperty(PROPERTY_KEYID1);
        this.keyId2 = pageInfo.getProperty(PROPERTY_KEYID2);
        if (this.keyId1 != null && this.keyId1.length() > 0 && this.keyId2 != null && this.keyId2.length() > 0) {
            theReturn = true;
            this.logger.debug("keyId1 = " + this.keyId1);
            this.logger.debug("keyId2 = " + this.keyId2);
        } else {
            this.logger.error("Keyid1 and Keyid2 not provided.");
        }
        this.buttonPlacement = this.element.getProperty(PROPERTY_BUTTONPLACEMENT);
        if (this.buttonPlacement == null || this.buttonPlacement.length() == 0) {
            this.buttonPlacement = "none";
        }
        this.logger.debug("buttonPlacement = " + this.buttonPlacement);
        String temp = this.element.getProperty(PROPERTY_VIEWONLY);
        this.viewOnly = temp == null || temp.length() == 0 ? false : temp.equalsIgnoreCase("y");
        this.logger.debug("viewOnly = " + this.viewOnly);
        return theReturn;
    }

    private void renderScriptAndStyle(StringBuffer html, String theRuleType) {
        this.logger.info("renderScriptAndStyle called...");
        html.append("<script type='text/javascript' src='").append(JSFILE).append("'></script>\n");
        html.append("<script>\n");
        html.append(JSOBJECT).append(".").append(JSELEMENTID).append(" = '").append(this.elementid).append("';\n");
        html.append(JSOBJECT).append(".").append(JSDATASETNAME).append(" = '").append(DATASET_SPECRULE).append("';\n");
        if (theRuleType != null) {
            html.append(JSOBJECT).append(".").append(JSRULETYPE).append(" = '").append(theRuleType).append("';\n");
        } else {
            html.append(JSOBJECT).append(".").append(JSRULETYPE).append(" = '';\n");
        }
        html.append("</script>\n");
    }

    private void renderHiddenTableElements(StringBuffer html, DataSet ruleData) {
        this.logger.info("renderHiddenTableElements called...");
        this.logger.debug("element.getId() = " + this.element.getId());
        html.append(SDITagUtil.getFixedRowInputs(DATASET_SPECRULE, ruleData.getColumns(), ruleData.getRowCount(), "", "|"));
    }

    private void renderHiddenRowElements(StringBuffer buffer, int dataRow) {
        this.logger.info("renderHiddenRowElements called...");
        this.logger.debug("dataRow = " + dataRow);
        if (dataRow > -1) {
            buffer.append(SDITagUtil.getRepeatedRowInputs(DATASET_SPECRULE, new String[]{"specid", "specversionid", SPECRULE_RULENO}, this.sdiInfo.getQueryData(DATASET_SPECRULE), "", "", 0));
        } else {
            this.logger.info("No specrule row therefore cannot add dataset hidden row elements.");
        }
    }

    private void renderHiddenInputElement(StringBuffer html, int row, String columnId, String value) {
        this.logger.info("renderHiddenInputElement called...");
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("data", DATASET_SPECRULE);
        inputProps.setProperty("mode", PROPERTY_TEXT);
        inputProps.setProperty(PROPERTY_STYLE, "display:none;");
        inputProps.setProperty("value", value);
        inputProps.setProperty("columnid", columnId);
        inputProps.setProperty("row", "" + row);
        SDITagUtil.setIdentifierAttributes(inputProps, this.sdiInfo);
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        html.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
    }

    private void renderHiddenData(StringBuffer html, DataSet ruleData) {
        this.logger.info("renderHiddenData called...");
        for (int index = 0; index < ruleData.getRowCount(); ++index) {
            this.sdiInfo.getQueryData(DATASET_SPECRULE).setCurrentRow(index);
            ((HashMap)this.sdiInfo.getQueryData(DATASET_SPECRULE).get(index)).put("__rowid", "" + index);
            this.renderHiddenRowElements(html, index);
            html.append("\n");
            this.renderHiddenInputElement(html, index, SPECRULE_RULEDEF, ruleData.getValue(index, SPECRULE_RULEDEF, ""));
            this.renderHiddenInputElement(html, index, SPECRULE_RULENO, ruleData.getValue(index, SPECRULE_RULENO, ""));
            html.append("\n");
        }
        this.sdiInfo.getQueryData(DATASET_SPECRULE).setCurrentRow(-9999);
        html.append("\n");
        this.renderHiddenInputElement(html, -9999, SPECRULE_RULEDEF, "");
        this.renderHiddenInputElement(html, -9999, SPECRULE_RULENO, "");
        html.append("\n");
    }

    private boolean renderHTML(StringBuffer html, DataSet ruleData, String theRuleType, String theButtonPlacement, String specId, String specVersionId, boolean isViewOnly) {
        this.logger.info("renderHTML called...");
        boolean theReturn = true;
        this.logger.debug("ruleData.getRowCount = " + ruleData.getRowCount());
        this.logger.debug("ruleData.getColumnCount = " + ruleData.getColumnCount());
        this.renderScriptAndStyle(html, theRuleType);
        this.renderHiddenTableElements(html, ruleData);
        if (theRuleType != null && (theRuleType.equalsIgnoreCase(STRUCTUREDRULETYPE) || theRuleType.equalsIgnoreCase(BANDEDRULETYPE))) {
            this.renderHiddenData(html, ruleData);
        }
        boolean locked = this.checkLockState();
        if (theButtonPlacement.equals("none") || isViewOnly) {
            if (theRuleType == null || theRuleType.length() == 0) {
                this.renderNoRuleTypeHTML(html, ruleData.getRowCount());
            } else if (theRuleType.equals(STRUCTUREDRULETYPE)) {
                this.renderStructuredHTML(html, ruleData, locked, isViewOnly);
            } else if (theRuleType.equals(BANDEDRULETYPE)) {
                this.renderBandedHTML(html, ruleData, locked, isViewOnly);
            } else if (theRuleType.equals(CUSTOMRULETYPE)) {
                this.renderCustomHTML(html, ruleData, locked, isViewOnly, specId, specVersionId);
            } else {
                this.renderUnsupportedHTML(html, ruleData, specId, specVersionId);
            }
        } else if (theButtonPlacement.equals("topleft") || theButtonPlacement.equals("topmiddle") || theButtonPlacement.equals("topright")) {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            if (theRuleType == null || theRuleType.length() == 0) {
                this.renderNoRuleTypeHTML(html, ruleData.getRowCount());
            } else if (theRuleType.equals(STRUCTUREDRULETYPE)) {
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderStructuredHTML(html, ruleData, locked, isViewOnly);
            } else if (theRuleType.equals(BANDEDRULETYPE)) {
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderBandedHTML(html, ruleData, locked, isViewOnly);
            } else if (theRuleType.equals(CUSTOMRULETYPE)) {
                this.renderCustomHTML(html, ruleData, locked, isViewOnly, specId, specVersionId);
            } else {
                this.renderUnsupportedHTML(html, ruleData, specId, specVersionId);
            }
            html.append("</td>\n</tr>\n</table>\n");
        } else if (theButtonPlacement.equals("bottomleft") || theButtonPlacement.equals("bottommiddle") || theButtonPlacement.equals("bottomright")) {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            if (theRuleType == null || theRuleType.length() == 0) {
                this.renderNoRuleTypeHTML(html, ruleData.getRowCount());
            } else if (theRuleType.equals(STRUCTUREDRULETYPE)) {
                this.renderStructuredHTML(html, ruleData, locked, isViewOnly);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
            } else if (theRuleType.equals(BANDEDRULETYPE)) {
                this.renderBandedHTML(html, ruleData, locked, isViewOnly);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
            } else if (theRuleType.equals(CUSTOMRULETYPE)) {
                this.renderCustomHTML(html, ruleData, locked, isViewOnly, specId, specVersionId);
            } else {
                this.renderUnsupportedHTML(html, ruleData, specId, specVersionId);
            }
            html.append("</td>\n</tr>\n</table>\n");
        } else {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            if (theRuleType == null || theRuleType.length() == 0) {
                this.renderNoRuleTypeHTML(html, ruleData.getRowCount());
            } else if (theRuleType.equals(STRUCTUREDRULETYPE)) {
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderStructuredHTML(html, ruleData, locked, isViewOnly);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
            } else if (theRuleType.equals(BANDEDRULETYPE)) {
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderBandedHTML(html, ruleData, locked, isViewOnly);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
                this.renderButtons(html, theButtonPlacement, theRuleType, specId, specVersionId, locked);
            } else if (theRuleType.equals(CUSTOMRULETYPE)) {
                this.renderCustomHTML(html, ruleData, locked, isViewOnly, specId, specVersionId);
            } else {
                this.renderUnsupportedHTML(html, ruleData, specId, specVersionId);
            }
            html.append("</td>\n</tr>\n</table>\n");
        }
        return theReturn;
    }

    private boolean checkLockState() {
        boolean theReturn;
        this.logger.info("checkLockState called...");
        try {
            DataSet data = this.sdiInfo.getDataSet(DATASET_SPECRULE);
            String lockedBy = data.getValue(0, "__lockedby", "");
            if (lockedBy == null || lockedBy.length() == 0) {
                theReturn = false;
                this.logger.debug("Not locked.");
            } else {
                theReturn = true;
                this.logger.debug("Locked by " + lockedBy + ".");
            }
        }
        catch (Exception e) {
            theReturn = true;
            this.logger.warn("Could not obtain lock information therefore default to locked.");
        }
        return theReturn;
    }

    private void renderNoRuleTypeHTML(StringBuffer html, int rowCount) {
        this.logger.info("renderUnsupportedHTML called...");
        html.append(this.getTranslationProcessor().translate("No rule type has been specified. Please select a rule type for the specification to build your rule."));
        if (rowCount > 0) {
            html.append("<br>").append(this.getTranslationProcessor().translate("This specification may have been upgraded from LVX and if so will require the rules to removed and recreated."));
        }
    }

    private void renderUnsupportedHTML(StringBuffer html, DataSet ruleData, String specId, String specVersion) {
        this.logger.info("renderUnsupportedHTML called...");
        if (ruleData.getRowCount() > 0) {
            html.append(this.getTranslationProcessor().translate("The current rule type is unsupported in the Browser. Below is a readonly view of the rule data."));
        } else {
            html.append(this.getTranslationProcessor().translate("The current rule type is unsupported in the Browser. Please select a supported rule type and save the specification."));
        }
        html.append("<table width='100%' border=0 id='__").append(this.elementid).append("_table' class='' >");
        html.append("<tbody>");
        for (int index = 0; index < ruleData.getRowCount(); ++index) {
            String ruleDef = ruleData.getValue(index, SPECRULE_RULEDEF, "");
            String ruleNo = ruleData.getBigDecimal(0, SPECRULE_RULENO).toString();
            if (ruleDef != null && ruleDef.length() > 0) {
                html.append("<tr id='").append(this.elementid).append("_row").append(index).append("'>");
                html.append("<td nowrap>");
                this.renderInputElement(html, ruleDef, "specrule0_ruledef", true, true, true);
                this.renderInputElement(html, ruleNo, "specrule0_ruleno", false, false, false);
                this.renderInputElement(html, STRUCTUREDRULETYPE, "__specrule0_rs", false, false, false);
                this.renderInputElement(html, specId + ";" + specVersion + ";" + ruleNo, "__specrule0_key", false, false, false);
                html.append("</td>");
                html.append("</tr>");
                continue;
            }
            this.logger.debug("Blank rule def found.");
        }
        html.append("</tbody>");
        html.append("</table>");
    }

    private void renderCustomHTML(StringBuffer html, DataSet ruleData, boolean locked, boolean isViewOnly, String specId, String specVersion) {
        this.logger.info("renderCustomHTML called...");
        String style = locked ? " style='color:#C9C9C2;'" : "";
        html.append("<table width='100%' border=0 id='__").append(this.elementid).append("_table' class='' >");
        html.append("<tbody>");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (ruleData.getRowCount() > 0) {
            String ruleDef = ruleData.getValue(0, SPECRULE_RULEDEF, "");
            String ruleNo = ruleData.getBigDecimal(0, SPECRULE_RULENO).toString();
            html.append("<tr id='").append(this.elementid).append("_headrow").append("'>");
            html.append("<td nowrap ").append(style).append(" >").append(tp.translate("Spec conditioning processing class. The class must extend the BaseSpecRule class and return a condition.")).append("</td>");
            html.append("</tr>");
            html.append("<tr id='").append(this.elementid).append("_row").append(0).append("'>");
            html.append("<td nowrap>");
            this.renderInputElement(html, ruleDef, "specrule0_ruledef", locked, true, isViewOnly);
            this.renderInputElement(html, ruleNo, "specrule0_ruleno", false, false, false);
            this.renderInputElement(html, STRUCTUREDRULETYPE, "__specrule0_rs", false, false, false);
            this.renderInputElement(html, specId + ";" + specVersion + ";" + ruleNo, "__specrule0_key", false, false, false);
            html.append("</td>");
            html.append("</tr>");
        } else {
            html.append("<tr id='").append(this.elementid).append("_headrow").append("'>");
            html.append("<td nowrap ").append(style).append(" >").append(tp.translate("Spec conditioning processing class. The class must extend the BaseSpecRule class and return a condition.")).append("</td>");
            html.append("</tr>");
            html.append("<tr id='").append(this.elementid).append("_row").append(0).append("'>");
            html.append("<td nowrap>");
            this.renderInputElement(html, "", "specrule0_ruledef", locked, true, isViewOnly);
            this.renderInputElement(html, "1", "specrule0_ruleno", false, false, false);
            this.renderInputElement(html, "I", "__specrule0_rs", false, false, false);
            this.renderInputElement(html, specId + ";" + specVersion + ";1", "__specrule0_key", false, false, false);
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody>");
        html.append("</table>");
    }

    private void renderBandedHTML(StringBuffer html, DataSet ruleData, boolean locked, boolean isViewOnly) {
        this.logger.info("renderBandedHTML called...");
        String style = locked ? " style='color:#C9C9C2;'" : "";
        this.renderNoDataMsg(html, ruleData.size() == 0);
        html.append("<table width='100%' border=0 id='__").append(this.elementid).append("_table' class='' >");
        html.append("<tbody>");
        TranslationProcessor tp = this.getTranslationProcessor();
        for (int index = 0; index < ruleData.getRowCount(); ++index) {
            String specCondition;
            String limitCondition;
            String ruleDef = ruleData.getValue(index, SPECRULE_RULEDEF, "");
            if (ruleDef != null && ruleDef.length() > 0) {
                String[] ruleDefArray = ruleDef.split(";");
                if (ruleDefArray.length > 0 && ruleDefArray.length < 3) {
                    limitCondition = ruleDefArray[0];
                    specCondition = ruleDefArray.length > 1 ? ruleDefArray[1] : "";
                } else {
                    this.logger.warn("Rule Definition is not of banded type.");
                    limitCondition = "";
                    specCondition = "";
                }
            } else {
                this.logger.debug("Blank rule def found.");
                limitCondition = "";
                specCondition = "";
            }
            html.append("<tr id='").append(this.elementid).append("_row").append(index).append("'>");
            this.renderCheckBox(html, index, locked);
            if (index < ruleData.getRowCount() - 1) {
                html.append("<td nowrap ").append(style).append(" >").append(tp.translate("If there are any items set to")).append("</td>");
                html.append("<td nowrap>");
                this.renderSelectCondElement(html, limitCondition, this.elementid + index + "_limitcond", index, BANDEDRULETYPE, locked, true, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap ").append(style).append(" >").append(tp.translate("then set the spec condition to")).append("</td>");
                html.append("<td nowrap>");
                this.renderSelectCondElement(html, specCondition, this.elementid + index + "_speccond", index, BANDEDRULETYPE, locked, true, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap ").append(style).append(" >").append(tp.translate("otherwise")).append("</td>");
            } else {
                html.append("<td nowrap>&nbsp;</td>");
                html.append("<td nowrap>");
                this.renderSelectCondElement(html, limitCondition, this.elementid + index + "_limitcond", index, BANDEDRULETYPE, locked, false, isViewOnly);
                html.append("</td>");
                if (ruleData.getRowCount() == 1) {
                    html.append("<td nowrap ").append(style).append(" >").append(tp.translate("Set the spec condition to")).append("</td>");
                } else {
                    html.append("<td nowrap ").append(style).append(" >").append(tp.translate("then set the spec condition to")).append("</td>");
                }
                html.append("<td nowrap>");
                this.renderSelectCondElement(html, specCondition, this.elementid + index + "_speccond", index, BANDEDRULETYPE, locked, true, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>&nbsp;</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody>");
        html.append("</table>");
        html.append("<table style='display:none;' id='__").append(this.elementid).append("_templatetable' >");
        html.append("<tr id='").append(this.elementid).append("_row").append("[__row]").append("'>");
        this.renderCheckBox(html, -9999, locked);
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("If there are any items set to")).append("</td>");
        html.append("<td nowrap>");
        this.renderSelectCondElement(html, "", this.elementid + "[__row]_limitcond", -9999, BANDEDRULETYPE, locked, true, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("then set the spec condition to")).append("</td>");
        html.append("<td nowrap>");
        this.renderSelectCondElement(html, "", this.elementid + "[__row]_speccond", -9999, BANDEDRULETYPE, locked, true, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("otherwise")).append("</td>");
        html.append("</tr>");
        html.append("</table>");
    }

    private void renderInputElement(StringBuffer html, String selectedValue, String id, boolean locked, boolean visable, boolean isViewOnly) {
        this.logger.info("renderInputElement called...");
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("mode", "input");
        inputProps.setProperty("value", selectedValue);
        inputProps.setProperty("columnid", id);
        inputProps.setProperty("name", id);
        if (!isViewOnly && !locked && visable) {
            inputProps.setProperty("onchange", "sdiSetRowUpdate(event)");
        }
        if (!visable) {
            inputProps.setProperty(PROPERTY_STYLE, "display:none;");
        } else {
            inputProps.setProperty(PROPERTY_STYLE, "width:250px;");
        }
        if (locked || isViewOnly) {
            inputProps.setProperty("disabled", "true");
            inputProps.setProperty("readonly", "true");
        }
        html.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
    }

    private void renderSelectCondElement(StringBuffer html, String selectedValue, String id, int row, String theRuleType, boolean locked, boolean visable, boolean isViewOnly) {
        this.logger.info("renderSelectElement called...");
        SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("mode", "dropdownlist");
        inputProps.setProperty("value", selectedValue);
        inputProps.setProperty("columnid", id);
        inputProps.setProperty("name", id);
        inputProps.setProperty("reftypeid", SPECCONDITION_REFTYPE);
        inputProps.setProperty("onchange", "specRules.doSelectOnChange( '" + this.elementid + "', '" + DATASET_SPECRULE + "', " + row + ", '" + theRuleType + "' );");
        if (!visable) {
            inputProps.setProperty(PROPERTY_STYLE, "display:none;");
        }
        if (locked || isViewOnly) {
            inputProps.setProperty("disabled", "true");
            inputProps.setProperty("readonly", "true");
        }
        html.append(sdiTagUtil.getInputHtml(inputProps, this.sdiInfo));
    }

    private void renderSelectNormElement(StringBuffer html, String selectedValue, String id, int row, String[] values, String[] options, boolean locked, boolean visable, String theRuleType, boolean isViewOnly) {
        this.logger.info("renderSelectNormElement called...");
        html.append("<select name='").append(id).append("' id='").append(id).append("' ");
        html.append(" onchange='").append(JSOBJECT).append(".").append(JSONCHANGE_SELECT).append("( \"").append(this.elementid).append("\", \"").append(DATASET_SPECRULE).append("\", ").append(row).append(", \"").append(theRuleType).append("\" );'");
        if (locked || isViewOnly) {
            html.append(" disabled readonly ");
        }
        if (!visable) {
            html.append(" style='display:none;' ");
        }
        html.append(">");
        if (values.length == options.length) {
            for (int index = 0; index < options.length; ++index) {
                html.append("<option value='").append(values[index]).append("' ");
                if (selectedValue.equalsIgnoreCase(values[index])) {
                    html.append(" SELECTED ");
                }
                html.append(">").append(options[index]).append("</option>");
            }
        } else {
            this.logger.warn("Options and values do not match.");
        }
        html.append("</select>");
    }

    private void renderCheckBox(StringBuffer html, int row, boolean locked) {
        this.logger.info("renderCheckBox called...");
        if (!locked) {
            String rowId = row == -9999 ? "[__row]" : "" + row;
            html.append("<td nowrap>");
            html.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(this.elementid).append(rowId).append("\" ");
            html.append(" value=\"__").append(this.elementid).append(rowId).append("\" ");
            html.append(" onclick=\"").append(JSOBJECT).append(".").append(JSONCLICK_CHECKBOX).append("('").append(this.elementid).append("', '").append(DATASET_SPECRULE).append("', ").append(row).append(" );\" >");
            html.append("</td>");
        } else {
            html.append("<td nowrap>&nbsp;</td>");
        }
    }

    private void renderNoDataMsg(StringBuffer html, boolean visible) {
        html.append("<span id=\"__").append(this.elementid).append("_nodata\" ");
        html.append("style=\"display:").append(visible ? "block" : "none").append("\">");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (tp != null) {
            html.append(tp.translate("No rule data added."));
        } else {
            html.append("No rule data added.");
        }
        html.append("</span>");
    }

    private void renderStructuredHTML(StringBuffer html, DataSet ruleData, boolean locked, boolean isViewOnly) {
        this.logger.info("renderStructuredHTML called...");
        String style = locked ? " style='color:#C9C9C2;'" : "";
        this.renderNoDataMsg(html, ruleData.size() == 0);
        html.append("<table width='100%' border=0 id='__").append(this.elementid).append("_table' class='' >");
        html.append("<tbody>");
        TranslationProcessor tp = this.getTranslationProcessor();
        for (int index = 0; index < ruleData.getRowCount(); ++index) {
            boolean display;
            String limitCondition;
            String operation;
            String mandatory;
            String items;
            String all;
            String specCondition;
            String ruleDef = ruleData.getValue(index, SPECRULE_RULEDEF, "");
            if (ruleDef != null && ruleDef.length() > 0) {
                String[] ruleDefArray = ruleDef.split(";");
                if (ruleDefArray.length == 6) {
                    specCondition = ruleDefArray[0];
                    all = ruleDefArray[1];
                    items = ruleDefArray[2];
                    mandatory = ruleDefArray[3];
                    operation = ruleDefArray[4];
                    limitCondition = ruleDefArray[5];
                } else if (ruleDefArray.length == 5) {
                    specCondition = ruleDefArray[0];
                    all = ruleDefArray[1];
                    items = ruleDefArray[2];
                    mandatory = ruleDefArray[3];
                    operation = ruleDefArray[4];
                    limitCondition = "";
                } else if (ruleDefArray.length == 4) {
                    specCondition = ruleDefArray[0];
                    all = ruleDefArray[1];
                    items = ruleDefArray[2];
                    mandatory = ruleDefArray[3];
                    operation = "";
                    limitCondition = "";
                } else if (ruleDefArray.length == 3) {
                    specCondition = ruleDefArray[0];
                    all = ruleDefArray[1];
                    items = ruleDefArray[2];
                    mandatory = "";
                    operation = "";
                    limitCondition = "";
                } else if (ruleDefArray.length == 2) {
                    specCondition = ruleDefArray[0];
                    all = ruleDefArray[1];
                    items = "";
                    mandatory = "";
                    operation = "";
                    limitCondition = "";
                } else if (ruleDefArray.length == 1) {
                    specCondition = ruleDefArray[0];
                    all = "";
                    items = "";
                    mandatory = "";
                    operation = "";
                    limitCondition = "";
                } else {
                    specCondition = "";
                    all = "";
                    items = "";
                    mandatory = "";
                    operation = "";
                    limitCondition = "";
                }
            } else {
                this.logger.warn("Rule Definition is not set.");
                specCondition = "";
                all = "";
                items = "";
                mandatory = "";
                operation = "";
                limitCondition = "";
            }
            html.append("<tr id='").append(this.elementid).append("_row").append(index).append("'>");
            this.renderCheckBox(html, index, locked);
            html.append("<td nowrap ").append(style).append(" >").append(tp.translate("Set condition to")).append("</td>");
            html.append("<td nowrap>");
            this.renderSelectCondElement(html, specCondition, this.elementid + index + "_speccond", index, STRUCTUREDRULETYPE, locked, true, isViewOnly);
            html.append("</td>");
            boolean bl = display = specCondition.length() > 0;
            if (display) {
                html.append("<td nowrap ").append(style).append(" >").append(tp.translate("if")).append("</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, all, this.elementid + index + "_all", index, VALUES_ALL, OPTIONS_ALL, locked, true, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, items, this.elementid + index + "_items", index, VALUES_ITEMS, OPTIONS_ITEMS, locked, true, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                boolean bl2 = display = items.length() > 0;
                if (display) {
                    html.append("<td nowrap>");
                    this.renderSelectNormElement(html, mandatory, this.elementid + index + "_mandatory", index, VALUES_MANDATORY, OPTIONS_MANDATORY, locked, true, STRUCTUREDRULETYPE, isViewOnly);
                    html.append("</td>");
                    html.append("<td nowrap ").append(style).append(" >").append(tp.translate("item(s)")).append("</td>");
                    html.append("<td nowrap>");
                    this.renderSelectNormElement(html, operation, this.elementid + index + "_operation", index, VALUES_OPERATOR, OPTIONS_OPERATOR, locked, true, STRUCTUREDRULETYPE, isViewOnly);
                    html.append("</td>");
                    html.append("<td nowrap>");
                    this.renderSelectCondElement(html, limitCondition, this.elementid + index + "_limitcond", index, STRUCTUREDRULETYPE, locked, true, isViewOnly);
                    html.append("</td>");
                    if (ruleData.getRowCount() > 1 && index < ruleData.getRowCount() - 1) {
                        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("otherwise")).append("</td>");
                    } else {
                        html.append("<td nowrap>&nbsp;</td>");
                    }
                } else {
                    html.append("<td nowrap>");
                    this.renderSelectNormElement(html, mandatory, this.elementid + index + "_mandatory", index, VALUES_MANDATORY, OPTIONS_MANDATORY, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                    html.append("</td>");
                    html.append("<td nowrap>&nbsp;</td>");
                    html.append("<td nowrap>");
                    this.renderSelectNormElement(html, operation, this.elementid + index + "_operation", index, VALUES_OPERATOR, OPTIONS_OPERATOR, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                    html.append("</td>");
                    html.append("<td nowrap>");
                    this.renderSelectCondElement(html, limitCondition, this.elementid + index + "_limitcond", index, STRUCTUREDRULETYPE, locked, false, isViewOnly);
                    html.append("</td>");
                    html.append("<td nowrap>&nbsp;</td>");
                }
            } else {
                html.append("<td nowrap>&nbsp;</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, all, this.elementid + index + "_all", index, VALUES_ALL, OPTIONS_ALL, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, items, this.elementid + index + "_items", index, VALUES_ITEMS, OPTIONS_ITEMS, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, mandatory, this.elementid + index + "_mandatory", index, VALUES_MANDATORY, OPTIONS_MANDATORY, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>&nbsp;</td>");
                html.append("<td nowrap>");
                this.renderSelectNormElement(html, operation, this.elementid + index + "_operation", index, VALUES_OPERATOR, OPTIONS_OPERATOR, locked, false, STRUCTUREDRULETYPE, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>");
                this.renderSelectCondElement(html, limitCondition, this.elementid + index + "_limitcond", index, STRUCTUREDRULETYPE, locked, false, isViewOnly);
                html.append("</td>");
                html.append("<td nowrap>&nbsp;</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody>");
        html.append("</table>");
        html.append("<table style='display:none;' id='__").append(this.elementid).append("_templatetable' >");
        html.append("<tr id='").append(this.elementid).append("_row").append("[__row]").append("'>");
        this.renderCheckBox(html, -9999, locked);
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("Set condition to")).append("</td>");
        html.append("<td nowrap>");
        this.renderSelectCondElement(html, "", this.elementid + "[__row]_speccond", -9999, STRUCTUREDRULETYPE, locked, true, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("if")).append("</td>");
        html.append("<td nowrap>");
        this.renderSelectNormElement(html, "", this.elementid + "[__row]_all", -9999, VALUES_ALL, OPTIONS_ALL, locked, true, STRUCTUREDRULETYPE, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap>");
        this.renderSelectNormElement(html, "", this.elementid + "[__row]_items", -9999, VALUES_ITEMS, OPTIONS_ITEMS, locked, true, STRUCTUREDRULETYPE, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap>");
        this.renderSelectNormElement(html, "", this.elementid + "[__row]_mandatory", -9999, VALUES_MANDATORY, OPTIONS_MANDATORY, locked, true, STRUCTUREDRULETYPE, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("item(s)")).append("</td>");
        html.append("<td nowrap>");
        this.renderSelectNormElement(html, "", this.elementid + "[__row]_operation", -9999, VALUES_OPERATOR, OPTIONS_OPERATOR, locked, true, STRUCTUREDRULETYPE, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap>");
        this.renderSelectCondElement(html, "", this.elementid + "[__row]_limitcond", -9999, STRUCTUREDRULETYPE, locked, true, isViewOnly);
        html.append("</td>");
        html.append("<td nowrap ").append(style).append(" >").append(tp.translate("otherwise")).append("</td>");
        html.append("</tr>");
        html.append("</table>");
    }

    private void renderButtons(StringBuffer html, String theButtonPlacement, String theRuleType, String specId, String specVersionId, boolean locked) {
        this.logger.info("renderButtons called...");
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
                button.setWidth(buttonProps.getProperty(PROPERTY_WIDTH));
                String js = buttonProps.getProperty(PROPERTY_JS);
                this.logger.debug("js = " + js);
                if (js.length() == 0) {
                    js = "specRules.noop();";
                } else if (locked) {
                    js = StringUtil.replaceAll(js, "add()", "specRules.lockedCall();");
                    js = StringUtil.replaceAll(js, "remove()", "specRules.lockedCall();");
                    js = StringUtil.replaceAll(js, "moveUp()", "specRules.lockedCall();");
                    js = StringUtil.replaceAll(js, "moveDown()", "specRules.lockedCall();");
                } else if (theRuleType != null && (theRuleType.equalsIgnoreCase(STRUCTUREDRULETYPE) || theRuleType.equalsIgnoreCase(BANDEDRULETYPE))) {
                    js = StringUtil.replaceAll(js, "add()", "specRules.addRule( '" + this.elementid + "', '" + DATASET_SPECRULE + "', '" + theRuleType + "', '" + specId + "', '" + specVersionId + "' )");
                    js = StringUtil.replaceAll(js, "remove()", "specRules.removeRule( '" + this.elementid + "', '" + DATASET_SPECRULE + "', '" + theRuleType + "' )");
                    js = StringUtil.replaceAll(js, "moveUp()", "specRules.moveRuleUp( '" + this.elementid + "', '" + DATASET_SPECRULE + "', '" + theRuleType + "' )");
                    js = StringUtil.replaceAll(js, "moveDown()", "specRules.moveRuleDown( '" + this.elementid + "', '" + DATASET_SPECRULE + "', '" + theRuleType + "' )");
                } else {
                    js = StringUtil.replaceAll(js, "add()", "specRules.invalidRuleTypeCall( '" + theRuleType + "' )");
                    js = StringUtil.replaceAll(js, "remove()", "specRules.invalidRuleTypeCall( '" + theRuleType + "' )");
                    js = StringUtil.replaceAll(js, "moveUp()", "specRules.invalidRuleTypeCall( '" + theRuleType + "' )");
                    js = StringUtil.replaceAll(js, "moveDown()", "specRules.invalidRuleTypeCall( '" + theRuleType + "' )");
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

    private DataSet getRuleData() {
        this.logger.info("getSpecLimitData called...");
        DataSet returnData = null;
        if (this.sdiInfo != null && (returnData = this.sdiInfo.getDataSet(DATASET_SPECRULE)) != null && returnData.size() > 0) {
            returnData.sort(SPECRULE_RULENO);
        }
        return returnData;
    }

    private String getRuleType() {
        String theReturn;
        this.logger.info("getSepcType called...");
        if (this.ruleType == null || this.ruleType.length() == 0) {
            try {
                this.ruleType = this.sdiInfo.getDataSet("primary").getString(0, "ruletypeflag");
                if (this.ruleType == null || this.ruleType.length() == 0) {
                    this.ruleType = "";
                    this.logger.warn("No spec type flag set therefore leave (1)");
                }
            }
            catch (Exception e) {
                this.ruleType = "";
                this.logger.warn("No spec type flag set therefore leave (2)");
            }
            theReturn = this.ruleType;
        } else {
            theReturn = this.ruleType;
        }
        this.logger.debug("theReturn = " + theReturn);
        return theReturn;
    }

    @Override
    public String getHtml() {
        this.logger.info("getHtml called...");
        StringBuffer html = new StringBuffer();
        String theReturn = "";
        if (this.loadProperties()) {
            this.logger.debug("Spec Id = " + this.keyId1);
            this.logger.debug("Spec Version Id = " + this.keyId2);
            DataSet ruleData = this.getRuleData();
            if (ruleData != null) {
                this.getRuleType();
                if (this.renderHTML(html, ruleData, this.ruleType, this.buttonPlacement, this.keyId1, this.keyId2, this.viewOnly)) {
                    if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
                        if (html.length() > 0) {
                            theReturn = html.toString();
                        }
                    } else {
                        theReturn = this.getError();
                    }
                } else {
                    this.logger.error("Could not render HTML.");
                    theReturn = this.getError();
                }
            } else {
                this.logger.error("Could not obtain rule data.");
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

