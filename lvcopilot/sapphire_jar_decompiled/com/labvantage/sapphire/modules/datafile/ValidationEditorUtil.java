/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.datafile;

import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ValidationEditorUtil {
    public static final String RULES_SEPARATOR = ";";
    public static final String EXPRESSION_SEPARATOR = ":";
    public static final String LISTITEM_SEPARATOR = "|";

    public static String getFieldsComboBox(PageContext pageContext, String elementid, String currentValue) throws SapphireException {
        String html = "";
        html = html + "<div class=\"input_field\" style=\"white-space:nowrap;display:inline-block\" \n>";
        html = html + "<input  class=\"input_field\" type=\"text\" style=\"border:1px solid white;\" id=\"" + elementid + "\" ";
        html = html + "name=\"" + elementid + "\" onKeyDown=\"showDDComboFields(event, '" + elementid + "' );\"  onchange='ddComboPopupWindow.hide();setValidationValueDFD();' ";
        html = html + "onDblClick=\"showDDComboFields(event, '" + elementid + "' );\" value=\"" + currentValue + "\"></input>";
        html = html + "\n<img title=\"Lookup DFD Fields\" onClick=\"showDDComboFields(event, '" + elementid + "'  );\" src=\"WEB-CORE/imageref/flat/16/flat_black_triangle_down.svg\" tabindex=\"-1\" style=\"margin-top:4px;margin-right:4px;width:12px\">";
        html = html + "\n</div>";
        return html;
    }

    public static String getFieldValueLengthDropdown(String elementid, String currentValue) {
        String dropdownhtml = "<select id='" + elementid + "'  onchange=\"fieldSelected('" + elementid + "');setValidationValueDFD()\">";
        dropdownhtml = dropdownhtml + "<option value='[this.value]' " + (currentValue.equals("[this.value]") ? "selected" : "") + " >[this.value]</option>";
        dropdownhtml = dropdownhtml + "<option value='[this.length]' " + (currentValue.equals("[this.length]") ? "selected" : "") + " >[this.length]</option>";
        dropdownhtml = dropdownhtml + "</select>";
        return dropdownhtml;
    }

    public static String getFieldNamesDropdown(String elementid, String currentValue) {
        String html = "";
        html = html + "<div class=\"input_field\" style=\"white-space:nowrap;display:inline-block\" \n>";
        html = html + "<input  class=\"input_field\" type=\"text\" style=\"border:1px solid white;\" id=\"" + elementid + "\" ";
        html = html + "name=\"" + elementid + "\" onKeyDown=\"showDDFieldNames(event, '" + elementid + "' );\"  onchange='ddComboPopupWindow.hide();setValidationValueDFD();' ";
        html = html + "onDblClick=\"showDDFieldNames(event, '" + elementid + "' );\" value=\"" + currentValue + "\"></input>";
        html = html + "\n<img title=\"Lookup DFD Fields\" onClick=\"showDDFieldNames(event, '" + elementid + "'  );\" src=\"WEB-CORE/imageref/flat/16/flat_black_triangle_down.svg\" tabindex=\"-1\" style=\"margin-top:4px;margin-right:4px;width:12px\">";
        html = html + "\n</div>";
        return html;
    }

    public static String getComparisonOperatorDropdown(TranslationProcessor tp, String lhsval, String datatype, String elementid, String currentValue) {
        if (lhsval.contains(".length]")) {
            datatype = "number";
        }
        String comparisonop = "<select id='" + elementid + "'  onchange='setValidationValueDFD()'>";
        if (datatype.equals("number")) {
            comparisonop = comparisonop + "<option value='EQ' " + (currentValue.equals("EQ") ? "selected" : "") + " >" + tp.translate("Equals") + "</option>";
            comparisonop = comparisonop + "<option value='NE' " + (currentValue.equals("NE") ? "selected" : "") + " >" + tp.translate("Not Equals") + "</option>";
            comparisonop = comparisonop + "<option value='GT' " + (currentValue.equals("GT") ? "selected" : "") + " >" + tp.translate("Greater Than") + "</option>";
            comparisonop = comparisonop + "<option value='GE' " + (currentValue.equals("GE") ? "selected" : "") + " >" + tp.translate("Greater Than or Equals") + "</option>";
            comparisonop = comparisonop + "<option value='LT' " + (currentValue.equals("LT") ? "selected" : "") + " >" + tp.translate("Less Than") + "</option>";
            comparisonop = comparisonop + "<option value='LE' " + (currentValue.equals("LE") ? "selected" : "") + " >" + tp.translate("Less Than or Equals") + "</option>";
        } else if (datatype.equals("string")) {
            comparisonop = comparisonop + "<option value='EQ' " + (currentValue.equals("EQ") ? "selected" : "") + " >" + tp.translate("Equals") + "</option>";
            comparisonop = comparisonop + "<option value='NE' " + (currentValue.equals("NE") ? "selected" : "") + " >" + tp.translate("Not Equals") + "</option>";
            comparisonop = comparisonop + "<option value='CT' " + (currentValue.equals("CT") ? "selected" : "") + " >" + tp.translate("Contains") + "</option>";
            comparisonop = comparisonop + "<option value='DC' " + (currentValue.equals("DC") ? "selected" : "") + " >" + tp.translate("Does Not Contain") + "</option>";
            comparisonop = comparisonop + "<option value='SW' " + (currentValue.equals("SW") ? "selected" : "") + " >" + tp.translate("Starts With") + "</option>";
            comparisonop = comparisonop + "<option value='EW' " + (currentValue.equals("EW") ? "selected" : "") + " >" + tp.translate("Ends With") + "</option>";
        } else if (datatype.equals("date")) {
            comparisonop = comparisonop + "<option value='LT' " + (currentValue.equals("LT") ? "selected" : "") + " >" + tp.translate("Before") + "</option>";
            comparisonop = comparisonop + "<option value='GT' " + (currentValue.equals("GT") ? "selected" : "") + " >" + tp.translate("After") + "</option>";
            comparisonop = comparisonop + "<option value='LE' " + (currentValue.equals("LE") ? "selected" : "") + " >" + tp.translate("On or Before") + "</option>";
            comparisonop = comparisonop + "<option value='GE' " + (currentValue.equals("GE") ? "selected" : "") + " >" + tp.translate("On or After") + "</option>";
        } else {
            comparisonop = comparisonop + "<option value='EQ' " + (currentValue.equals("EQ") ? "selected" : "") + " >" + tp.translate("Equals") + "</option>";
            comparisonop = comparisonop + "<option value='NE' " + (currentValue.equals("NE") ? "selected" : "") + " >" + tp.translate("Not Equals") + "</option>";
        }
        comparisonop = comparisonop + "</select>";
        return comparisonop;
    }

    public static String getValueCheckValidationHtml(TranslationProcessor tp, PageContext pageContext, String currentfielddatatype, boolean valuecheckchecked, String[] conditions, String valuecheckerroraction, String valuechecksub) throws SapphireException {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td class='viewlistcol' width='200px'><input onclick=\"setValidationValueDFD()\" id=\"ValueCheck\" name=\"selector\" type=\"checkbox\" value=\"ValueCheck\" " + (valuecheckchecked ? "checked" : "") + "><label for=\"ValueCheck\">" + tp.translate("Value Check") + "</label></td>");
        html.append("<td  class='viewlistcol' width='600px'>");
        if (valuecheckchecked) {
            String conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "ValueCheck") + "</font></td></tr>";
            if (conditions != null && conditions.length > 0) {
                for (int row = 0; row < conditions.length; ++row) {
                    String[] conditiontokens = StringUtil.split(conditions[row], EXPRESSION_SEPARATOR);
                    if (conditiontokens.length != 3) continue;
                    String sRowHtml = ValidationEditorUtil.getValueCheckConditionRow(tp, pageContext, conditions[row], row, currentfielddatatype);
                    conditionshtml = row == 0 ? conditionshtml + "<tr><td width=\"10px\"> </td>" : conditionshtml + "<tr><td width=\"10px\">&amp;</td>";
                    conditionshtml = conditionshtml + "<td>" + sRowHtml + "</td></tr>";
                }
            }
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addValueCheckCondition( '" + currentfielddatatype + "' )\">" + tp.translate("Add Condition") + "</button></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
            html.append("<div id='valuecheckconditionsdiv'>");
            html.append(conditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append(ValidationEditorUtil.getErrorActionHtml(tp, valuecheckchecked, "ValueCheck", valuecheckerroraction, valuechecksub, currentfielddatatype));
        } else {
            html.append("<div id='valuecheckconditionsdiv'>");
            String conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "ValueCheck") + "</font></td></tr>";
            conditionshtml = conditionshtml + "<tr><td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addValueCheckCondition( '" + currentfielddatatype + "' )\">" + tp.translate("Add Condition") + "</button></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
            html.append(conditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append("<td  class='viewlistcol'>");
            html.append("<div id='valuecheckvalidationerror'>");
            html.append("</div>");
            html.append("</td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    public static String getValueListValidationHtml(TranslationProcessor tp, PageContext pageContext, boolean valuelistcheck, String valuelist, String valuelisterroraction, String valuelistsub) throws SapphireException {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td class='viewlistcol' width='200px'><input onclick=\"setValidationValueDFD()\" id=\"ValueList\" name=\"selector\" type=\"checkbox\" value=\"ValueCheck\" " + (valuelistcheck ? "checked" : "") + "><label for=\"ValueList\">" + tp.translate("Value List") + "</label></td>");
        html.append("<td  class='viewlistcol' width='600px'>");
        if (valuelistcheck) {
            String[] conditions = StringUtil.split(valuelist, LISTITEM_SEPARATOR);
            String conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "ValueList") + "</font></td></tr>";
            if (conditions.length > 0) {
                for (int row = 0; row < conditions.length; ++row) {
                    String[] conditiontokens = StringUtil.split(conditions[row], EXPRESSION_SEPARATOR);
                    if (conditiontokens.length != 2) continue;
                    String sRowHtml = ValidationEditorUtil.getValueListRow(conditions[row], row);
                    conditionshtml = row == 0 ? conditionshtml + "<tr><td width=\"10px\"> </td>" : conditionshtml + "<tr><td width=\"10px\">|</td>";
                    conditionshtml = conditionshtml + "<td>" + sRowHtml + "</td></tr>";
                }
            }
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addValueListItem( )\">" + tp.translate("Add Item") + "</button></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
            html.append("<div id='valuelistconditionsdiv'>");
            html.append(conditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append(ValidationEditorUtil.getErrorActionHtml(tp, valuelistcheck, "ValueList", valuelisterroraction, valuelistsub, "string"));
        } else {
            html.append("<div id='valuelistconditionsdiv'>");
            String conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "ValueList") + "</font></td></tr>";
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addValueListItem( )\">" + tp.translate("Add Item") + "</button></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
            html.append(conditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append("<td  class='viewlistcol'>");
            html.append("<div id='valuelistvalidationerror'>");
            html.append("</div>");
            html.append("</td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    public static String getValueCheckConditionRow(TranslationProcessor tp, PageContext pageContext, String condition, int row, String fieldDataType) throws SapphireException {
        String[] conditiontokens = StringUtil.split(condition, EXPRESSION_SEPARATOR);
        if (conditiontokens.length == 3) {
            String deleteButtonHtml = "\n<img src=\"WEB-CORE/images/png/Delete.png\" onclick=\"deleteValueCheckCondition(" + row + ");\"/>";
            String listoffieldslhs = ValidationEditorUtil.getFieldValueLengthDropdown("valuecheckdropdownlhs" + row, conditiontokens[0]);
            String comparisonop = ValidationEditorUtil.getComparisonOperatorDropdown(tp, conditiontokens[0], fieldDataType, "valuecheckconditionop" + row, conditiontokens[1] != null ? conditiontokens[1] : "");
            String listoffieldsrhs = ValidationEditorUtil.getFieldsComboBox(pageContext, "valuecheckdropdownrhs" + row, conditiontokens[2] != null ? conditiontokens[2] : "");
            String sRowHtml = "<table><tr><td>\n" + listoffieldslhs + "\n" + comparisonop + "\n" + listoffieldsrhs + deleteButtonHtml + "</td></tr></table>\n";
            return sRowHtml;
        }
        return "";
    }

    public static String getValueListRow(String condition, int row) throws SapphireException {
        String[] conditiontokens = StringUtil.split(condition, EXPRESSION_SEPARATOR);
        if (conditiontokens.length > 0) {
            String deleteButtonHtml = "\n<img src=\"WEB-CORE/images/png/Delete.png\" onclick=\"deleteValueListCondition(" + row + ");\"/>";
            String valueitemid = "valuelistitem" + row;
            String valueitemsubid = "valuelistitemsub" + row;
            String valueitemhtml = "<input type='text' onchange='setValidationValueDFD()' id='" + valueitemid + "' value='" + StringUtil.unescape(conditiontokens[0]) + "'/>";
            String valuesubhtml = "<input type='text'  onchange='setValidationValueDFD()' id='" + valueitemsubid + "' value='" + StringUtil.unescape(conditiontokens[1]) + "'/>";
            String sRowHtml = "";
            sRowHtml = row != 0 ? "<table><tr><td>\n" + valueitemhtml + "</td><td>" + valuesubhtml + "</td><td>" + deleteButtonHtml + "</td></tr></table>\n" : "<table><tr><td>Value</td><td>Substitute</td></tr><tr><td>\n" + valueitemhtml + "</td><td>" + valuesubhtml + "</td><td>" + deleteButtonHtml + "</td></tr></table>\n";
            return sRowHtml;
        }
        return "";
    }

    public static String getDistinctFieldItemHtml(PageContext pageContext, String fieldid, int row) throws SapphireException {
        String deleteButtonHtml = "\n<img src=\"WEB-CORE/images/png/Delete.png\" onclick=\"deleteDistinctFieldRow(" + row + ");\"/>";
        String distinctfielditem = "distinctfielditem" + row;
        String fieldhtml = ValidationEditorUtil.getFieldNamesDropdown("distinctcheckdropdown" + row, fieldid);
        String sRowHtml = "";
        sRowHtml = "<table><tr><td>\n" + fieldhtml + "</td><td>" + deleteButtonHtml + "</td></tr></table>\n";
        return sRowHtml;
    }

    public static String getMandatoryConditionRow(PageContext pageContext, String condition, int row) throws SapphireException {
        String[] conditiontokens = StringUtil.split(condition, EXPRESSION_SEPARATOR);
        if (conditiontokens.length == 3) {
            TranslationProcessor tp = new TranslationProcessor(pageContext);
            String deleteButtonHtml = "\n<img src=\"WEB-CORE/images/png/Delete.png\" onclick=\"deleteMandatoryCondition(" + row + ");\"/>";
            String listoffieldslhs = ValidationEditorUtil.getFieldsComboBox(pageContext, "mandatorydropdownlhs" + row, conditiontokens[0]);
            String comparisonop = ValidationEditorUtil.getComparisonOperatorDropdown(tp, "", "all", "mandatoryconditionop" + row, conditiontokens[1] != null ? conditiontokens[1] : "");
            String listoffieldsrhs = ValidationEditorUtil.getFieldsComboBox(pageContext, "mandatorydropdownrhs" + row, conditiontokens[2] != null ? conditiontokens[2] : "");
            String sRowHtml = "<table><tr><td>\n" + listoffieldslhs + "\n" + comparisonop + "\n" + listoffieldsrhs + deleteButtonHtml + "</td></tr></table>\n";
            return sRowHtml;
        }
        return "";
    }

    public static String getMandatoryValidationHtml(TranslationProcessor tp, PageContext pageContext, String fielddatatype, boolean mandatorycheck, String[] mandatoryconditions, String mandatoryerroraction, String mandatorysub) throws SapphireException {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td class='viewlistcol' width='200px'><input onchange=\"setValidationValueDFD()\"  onclick=\"setValidationValueDFD()\" id=\"Mandatory\" name=\"selector\" type=\"checkbox\" value=\"Mandatory\" " + (mandatorycheck ? "checked" : "") + "><label for=\"Mandatory\">" + tp.translate("Mandatory") + "</label></td>");
        html.append("<td  class='viewlistcol' width='600px'>");
        if (mandatorycheck) {
            String mandatoryconditionshtml = "<table>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "Mandatory") + "</font></td></tr>";
            if (mandatoryconditions != null && mandatoryconditions.length > 0) {
                for (int row = 0; row < mandatoryconditions.length; ++row) {
                    String[] conditiontokens = StringUtil.split(mandatoryconditions[row], EXPRESSION_SEPARATOR);
                    if (conditiontokens.length != 3) continue;
                    String sRowHtml = ValidationEditorUtil.getMandatoryConditionRow(pageContext, mandatoryconditions[row], row);
                    mandatoryconditionshtml = row == 0 ? mandatoryconditionshtml + "<tr><td width=\"10px\"> </td>" : mandatoryconditionshtml + "<tr><td width=\"10px\">&amp;</td>";
                    mandatoryconditionshtml = mandatoryconditionshtml + "<td>" + sRowHtml + "</td></tr>";
                }
            }
            mandatoryconditionshtml = mandatoryconditionshtml + "<tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<td width=\"10px\"> </td>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addMandatoryCondition( '" + fielddatatype + "' )\">" + tp.translate("Add Condition") + "</button></td>";
            mandatoryconditionshtml = mandatoryconditionshtml + "</tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "</table>";
            html.append("<div id='mandatoryconditionsdiv'>");
            html.append(mandatoryconditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append(ValidationEditorUtil.getErrorActionHtml(tp, mandatorycheck, "Mandatory", mandatoryerroraction, mandatorysub, fielddatatype));
        } else {
            html.append("<div id='mandatoryconditionsdiv'>");
            String mandatoryconditionshtml = "<table><tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, "Mandatory") + "</font></td></tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<td width=\"10px\"> </td>";
            mandatoryconditionshtml = mandatoryconditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addMandatoryCondition( '" + fielddatatype + "' )\">" + tp.translate("Add Condition") + "</button></td>";
            mandatoryconditionshtml = mandatoryconditionshtml + "</tr>";
            mandatoryconditionshtml = mandatoryconditionshtml + "</table>";
            html.append(mandatoryconditionshtml);
            html.append("</div>");
            html.append("</td>");
            html.append("<td  class='viewlistcol'>");
            html.append("<div id='mandatoryvalidationerror'>");
            html.append("</div>");
            html.append("</td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    public static String getValueReftypeValidationHtml(TranslationProcessor tp, QueryProcessor queryProcessor, boolean valuereftypecheck, String valuereftype, String valuereftypeoption, String valuereftypeaction, String valuereftypesub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"ValueRefType\" name=\"selector\" type=\"checkbox\" value=\"ValueRefType\" " + (valuereftypecheck ? "checked" : "") + ">");
        html.append("<label for=\"ValueRefType\">" + tp.translate("Value RefType") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table>");
        html.append("<tr><td colspan=4><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "ValueRefType")).append("</font></td></tr>");
        html.append("<tr>");
        html.append("<td>");
        html.append("<select onfocus='autoCheck(\"ValueRefType\");' onclick=setValidationValueDFD() id='ValueRefTypeId' name='ValueRefTypeId'>");
        String sql = "SELECT reftypeid FROM reftype order by reftypeid";
        DataSet reftypes = queryProcessor.getSqlDataSet(sql);
        html.append("<option value=''></option>");
        for (int i = 0; i < reftypes.getRowCount(); ++i) {
            String curroption = reftypes.getValue(i, "reftypeid");
            if (curroption.equals(valuereftype)) {
                html.append("<option value='" + curroption + "' selected>" + curroption + "</option>");
                continue;
            }
            html.append("<option value='" + curroption + "'>" + curroption + "</option>");
        }
        html.append("</select>");
        html.append("</td>");
        html.append("<td>");
        html.append("<table>");
        html.append("<tr><td>");
        html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"ValueRefTypeCheckOption\" id=\"ValueRefTypeCheckOption1\"" + (valuereftypeoption.contains("1") ? "checked" : "") + ">" + tp.translate("Field Contains RefType Value") + "</input>");
        html.append("</tr></td>");
        html.append("<tr><td>");
        html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"ValueRefTypeCheckOption\" id=\"ValueRefTypeCheckOption2\"" + (valuereftypeoption.contains("2") ? "checked" : "") + ">" + tp.translate("Field Contains RefType Display Value") + "</input>");
        html.append("</tr></td>");
        html.append("<tr><td>");
        html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"ValueRefTypeCheckOption\" id=\"ValueRefTypeCheckOption3\"" + (valuereftypeoption.contains("3") ? "checked" : "") + ">" + tp.translate("Field Contains RefType Display Value, Substitute with RefType Value") + "</input>");
        html.append("</tr></td>");
        html.append("</table>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, valuereftypecheck, "ValueRefType", valuereftypeaction, valuereftypesub, "string"));
        return html.toString();
    }

    public static String getGroovyValidationHtml(TranslationProcessor tp, boolean groovycheck, String groovyscript, String groovyerroraction, String groovysub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"GroovyCheck\" name=\"selector\" type=\"checkbox\" value=\"GroovyCheck\" " + (groovycheck ? "checked" : "") + ">");
        html.append("<label for=\"GroovyCheck\">" + tp.translate("Groovy Check") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table>");
        html.append("<tr><td><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "GroovyCheck")).append("</font></td></tr>");
        html.append("<tr><td>");
        html.append("<textarea name=\"GroovyText\" id=\"GroovyText\" onfocus=\"autoCheck( 'GroovyCheck');\" class=\"input_field\" rows=\"4\" cols=\"80\"  onchange=\"setValidationValueDFD()\">" + groovyscript + "</textarea></td></tr></table></td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, groovycheck, "GroovyCheck", groovyerroraction, groovysub, "string"));
        return html.toString();
    }

    public static String getDistinctValidationHtml(PageContext pageContext, TranslationProcessor tp, boolean distinctcheck, String fieldlist, String distincterroraction, String distinctsub) throws SapphireException {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"DistinctCheck\" name=\"selector\" type=\"checkbox\" value=\"DistinctCheck\" " + (distinctcheck ? "checked" : "") + ">");
        html.append("<label for=\"DistinctCheck\">" + tp.translate("Distinct Check") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<div id='distinctcheckfieldsdiv'>");
        html.append("<table>");
        html.append("<tr><td><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "DistinctCheck")).append("</font></td></tr>");
        String[] fields = StringUtil.split(fieldlist, LISTITEM_SEPARATOR);
        String fieldslisthtml = "<tr><td><table>";
        if (fields.length > 0) {
            for (int row = 0; row < fields.length; ++row) {
                if (fields[row].length() <= 0) continue;
                String sRowHtml = ValidationEditorUtil.getDistinctFieldItemHtml(pageContext, fields[row], row);
                fieldslisthtml = row == 0 ? fieldslisthtml + "<tr><td width=\"10px\"> </td>" : fieldslisthtml + "<tr><td width=\"10px\">&</td>";
                fieldslisthtml = fieldslisthtml + "<td>" + sRowHtml + "</td></tr>";
            }
        }
        fieldslisthtml = fieldslisthtml + "<tr>";
        fieldslisthtml = fieldslisthtml + "<td width=\"10px\"> </td>";
        fieldslisthtml = fieldslisthtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addDistinctFieldItem( )\">" + tp.translate("Add Field") + "</button></td>";
        fieldslisthtml = fieldslisthtml + "</tr>";
        fieldslisthtml = fieldslisthtml + "</td></table>";
        html.append(fieldslisthtml);
        html.append("</div>");
        html.append("</table>");
        html.append("</td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, distinctcheck, "DistinctCheck", distincterroraction, distinctsub, "string"));
        return html.toString();
    }

    public static String getRegExValidationHtml(TranslationProcessor tp, boolean regexcheck, String regex, String regexerroraction, String regexsub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"RegExCheck\" name=\"selector\" type=\"checkbox\" value=\"RegExCheck\" " + (regexcheck ? "checked" : "") + ">");
        html.append("<label for=\"RegExCheck\">" + tp.translate("Regular Expression") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table>");
        html.append("<tr><td><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "RegExCheck")).append("</font></td></tr>");
        html.append("<tr><td>");
        html.append("<textarea name=\"RegExText\" id=\"RegExText\"  onfocus=\"autoCheck( 'RegExCheck');\" class=\"input_field\" rows=\"4\" cols=\"80\"  onchange=\"setValidationValueDFD()\">" + regex + "</textarea></td></tr></table></td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, regexcheck, "RegExCheck", regexerroraction, regexsub, "string"));
        return html.toString();
    }

    public static String getDateFormatCheckValidationHtml(TranslationProcessor tp, QueryProcessor queryProcessor, boolean dateformatcheck, String datelocale, String formatstr, String timezone, String erroraction, String dateformatsub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"DateFormatCheck\" name=\"selector\" type=\"checkbox\" value=\"DateFormatCheck\" " + (dateformatcheck ? "checked" : "") + ">");
        html.append("<label for=\"DateFormatCheck\">" + tp.translate("Date Format") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table>");
        html.append("<tr><td><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "DateFormatCheck")).append("</font></td></tr>");
        html.append("<table>");
        html.append("<tr><td>");
        html.append(tp.translate("Locale")).append("</td><td>");
        html.append(ValidationEditorUtil.getLocaleSelection(queryProcessor, datelocale));
        html.append("</td></tr>");
        html.append("<tr><td>");
        html.append(tp.translate("Format")).append("</td><td>");
        html.append("<input id=\"DateFormatStr\" onchange=\"autoCheck('DateFormatCheck');\" value=\"" + formatstr + "\">");
        html.append("</td></tr>");
        html.append("<tr><td>");
        html.append(tp.translate("Time Zone")).append("</td><td>");
        html.append(ValidationEditorUtil.getTimeZoneSelection(queryProcessor, timezone));
        html.append("</td></tr>");
        html.append("</table>");
        html.append("</td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, dateformatcheck, "DateFormatCheck", erroraction, dateformatsub, "date"));
        return html.toString();
    }

    public static String getNumberFormatCheckValidationHtml(TranslationProcessor tp, boolean numberFormatCheck, String fielddecimalseparator, String fieldgroupseparator, String erroraction, String sub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"NumberFormatCheck\" name=\"selector\" type=\"checkbox\" value=\"NumberFormatCheck\" " + (numberFormatCheck ? "checked" : "") + ">");
        html.append("<label for=\"NumberFormatCheck\">" + tp.translate("Number Format") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table>");
        html.append("<tr><td><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "NumberFormatCheck")).append("</font></td></tr>");
        html.append("<tr><td>");
        html.append("<table><tr><td>");
        html.append(tp.translate("Decimal Separator")).append("</td><td>");
        html.append("<input type='input' id='FieldDecimalSeparator' onchange=\"autoCheck('NumberFormatCheck');\" value='" + fielddecimalseparator + "'/>");
        html.append("</td></tr>");
        html.append("<tr><td>");
        html.append(tp.translate("Group Separator")).append("</td><td>");
        html.append("<input id=\"FieldGroupSeparator\" onchange=\"autoCheck('NumberFormatCheck');\" value=\"" + fieldgroupseparator + "\">");
        html.append("</td></tr>");
        html.append("</table></td></tr>");
        html.append("</table>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, numberFormatCheck, "NumberFormatCheck", erroraction, sub, "number"));
        return html.toString();
    }

    private static String getTimeZoneSelection(QueryProcessor queryProcessor, String currentvalue) {
        String sql = "select refvalueid from refvalue where reftypeid='Time Zone' order by refvalueid";
        DataSet ds = queryProcessor.getSqlDataSet(sql);
        StringBuffer html = new StringBuffer("<select id=\"DateTimeZone\" onchange=\"setValidationValueDFD()\">");
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.getValue(i, "refvalueid").equals(currentvalue)) {
                html.append("<option value=\"" + ds.getValue(i, "refvalueid") + "\" selected>" + ds.getValue(i, "refvalueid") + "</option>");
                continue;
            }
            html.append("<option value=\"" + ds.getValue(i, "refvalueid") + "\">" + ds.getValue(i, "refvalueid") + "</option>");
        }
        return html.toString();
    }

    private static String getLocaleSelection(QueryProcessor queryProcessor, String currentvalue) {
        String sql = "SELECT localeid, localedesc FROM locale order by localedesc";
        DataSet ds = queryProcessor.getSqlDataSet(sql);
        StringBuffer html = new StringBuffer("<select id=\"DateFormatLocale\" onchange=\"autoCheck('DateFormatCheck');setValidationValueDFD()\">");
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.getValue(i, "localeid").equals(currentvalue)) {
                html.append("<option value=\"" + ds.getValue(i, "localeid") + "\" selected>" + ds.getValue(i, "localedesc") + "</option>");
                continue;
            }
            html.append("<option value=\"" + ds.getValue(i, "localeid") + "\">" + ds.getValue(i, "localedesc") + "</option>");
        }
        return html.toString();
    }

    public static String getSDICheckValidationHtml(TranslationProcessor tp, SDCProcessor sdcProcessor, QueryProcessor queryProcessor, boolean sdicheck, String sdichecktype, String sdcid, String column, String substitutekeycol, String from, String whereclause, String sdicheckerroraction, String sdichecksub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"autoCheck('DateFormatCheck');setValidationValueDFD()\" id=\"SDICheck\" name=\"selector\" type=\"checkbox\" value=\"SDICheck\" " + (sdicheck ? "checked" : "") + ">");
        html.append("<label for=\"SDICheck\">" + tp.translate("SDI Check") + "</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table style='border: 1px;' width='600px'>");
        html.append("<tr><td colspan=4><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "SDICheck")).append("</font></td></tr>");
        html.append("<tr>");
        html.append("<td style='border: 1px;'>" + tp.translate("Type") + "</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<input type=\"radio\" onclick=\"autoCheck('SDICheck');updateSDICheckType();setValidationValueDFD()\" name=\"SDICheckType\" id=\"SDICheckNotExists\"" + (sdichecktype.equals("NotExists") ? "checked" : "") + " >" + tp.translate("Not Exists") + "</input>");
        html.append("<input type=\"radio\" onclick=\"autoCheck('SDICheck');updateSDICheckType();setValidationValueDFD()\" name=\"SDICheckType\" id=\"SDICheckExists\"" + (sdichecktype.equals("Exists") ? "checked" : "") + " >" + tp.translate("Exists") + "</input>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td style='border: 1px;'>" + tp.translate("SDC") + "</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<select onfocus=\"autoCheck('SDICheck');\" onchange=\"document.getElementById('SDICheckKeyColumn').value= '';updateSDICheckColumnInfo( );setValidationValueDFD();\"  id='SDICheckSDCId' name='SDICheckSDCId'>");
        String sql = "SELECT sdcid FROM sdc order by sdcid";
        DataSet sdclist = queryProcessor.getSqlDataSet(sql);
        html.append("<option value=''></option>");
        for (int i = 0; i < sdclist.getRowCount(); ++i) {
            String curroption = sdclist.getValue(i, "sdcid");
            if (curroption.equals(sdcid)) {
                html.append("<option value='" + curroption + "' selected>" + curroption + "</option>");
                continue;
            }
            html.append("<option value='" + curroption + "'>" + curroption + "</option>");
        }
        html.append("</select>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td>");
        html.append(tp.translate("From"));
        html.append("</td>");
        html.append("<td> ");
        html.append("<input type='text' id='SDICheckFromClause' name='SDICheckFromClause' value='" + from + "'></input>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append(ValidationEditorUtil.getSDICheckColumnInfoHTML(tp, sdcProcessor, sdcid, column, substitutekeycol, sdichecktype));
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td style='border: 1px;'>" + tp.translate("Where") + "</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<textarea name=\"SDICheckWhereClause\" onfocus=\"autoCheck('SDICheck');\" id=\"SDICheckWhereClause\" class=\"input_field\" rows=\"4\" cols=\"80\"  onchange=\"setValidationValueDFD()\">" + whereclause + "</textarea></td>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("</td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, sdicheck, "SDICheck", sdicheckerroraction, sdichecksub, "string"));
        return html.toString();
    }

    public static StringBuffer getSDICheckColumnInfoHTML(TranslationProcessor tp, SDCProcessor sdcProcessor, String sdcid, String column, String substitutekeycol, String sdichecktype) {
        StringBuffer html = new StringBuffer();
        html.append("<td style='border: 1px;' nowrap>" + tp.translate("Field Column") + "</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<select onchange=\"updateSDICheckColumnInfo();setValidationValueDFD()\" id=\"SDICheckKeyColumn\">");
        PropertyListCollection columnList = new PropertyListCollection();
        if (sdcid.length() > 0) {
            columnList = sdcProcessor.getColumns(sdcid);
        }
        String pkcolumn = "";
        for (int i = 0; i < columnList.size(); ++i) {
            PropertyList currentColumn = columnList.getPropertyList(i);
            String currcolumnid = currentColumn.getProperty("columnid", "");
            String columnlabel = currentColumn.getProperty("columnlabel", "");
            String pkflag = currentColumn.getProperty("pkflag", "");
            String datatype = currentColumn.getProperty("datatype", "");
            int columnlength = Integer.parseInt(currentColumn.getProperty("columnlength", "0"));
            if (datatype.equals("C") && columnlength > 1) {
                if (currcolumnid.equals("modby") || currcolumnid.equals("modtool") || currcolumnid.equals("createby") || currcolumnid.equals("createtool") || currcolumnid.equals("templateflag") || currcolumnid.equals("tracelog") || currcolumnid.equals("notes")) continue;
                if (currcolumnid.equals(column)) {
                    html.append("<option value='" + currcolumnid + "' selected>" + columnlabel + ("Y".equals(pkflag) ? " (Primary)" : "") + "</option>");
                } else {
                    html.append("<option value='" + currcolumnid + "'>" + columnlabel + ("Y".equals(pkflag) ? " (Primary)" : "") + "</option>");
                }
            }
            if (!"Y".equals(pkflag)) continue;
            pkcolumn = currcolumnid;
        }
        html.append("</select>");
        String disabledstr = "";
        if (sdichecktype.equals("NotExists")) {
            disabledstr = "readonly";
        } else if (pkcolumn.equals(column)) {
            disabledstr = "readonly";
        }
        html.append("&nbsp;&nbsp;&nbsp;");
        html.append(tp.translate("Substitite Value"));
        html.append("&nbsp;<input type=\"input\" onchange=\"setValidationValueDFD()\" name=\"SDICheckSubstitute\" id=\"SDICheckSubstitute\"  value=\"" + substitutekeycol + "\" " + disabledstr + "></input>");
        html.append("</td>");
        return html;
    }

    public static StringBuffer getSDICheckColumnOptions(SDCProcessor sdcProcessor, String sdcid, String column) {
        StringBuffer html = new StringBuffer();
        PropertyListCollection columnList = new PropertyListCollection();
        if (sdcid.length() > 0) {
            columnList = sdcProcessor.getColumns(sdcid);
        }
        for (int i = 0; i < columnList.size(); ++i) {
            PropertyList currentColumn = columnList.getPropertyList(i);
            String currcolumnid = currentColumn.getProperty("columnid", "");
            String columnlabel = currentColumn.getProperty("columnlabel", currentColumn.getProperty("columnid"));
            String pkflag = currentColumn.getProperty("pkflag", "");
            String datatype = currentColumn.getProperty("datatype", "");
            int columnlength = Integer.parseInt(currentColumn.getProperty("columnlength", "0"));
            if (!datatype.equals("C") || columnlength <= 1 || currcolumnid.equals("modby") || currcolumnid.equals("modtool") || currcolumnid.equals("createby") || currcolumnid.equals("createtool") || currcolumnid.equals("templateflag") || currcolumnid.equals("tracelog") || currcolumnid.equals("notes")) continue;
            if (currcolumnid.equals(column)) {
                html.append("<option value='" + currcolumnid + "' selected>" + columnlabel + ("Y".equals(pkflag) ? " (Primary)" : "") + "</option>");
                continue;
            }
            html.append("<option value='" + currcolumnid + "'>" + columnlabel + ("Y".equals(pkflag) ? " (Primary)" : "") + "</option>");
        }
        return html;
    }

    public static String getDBCheckValidationHtml(TranslationProcessor tp, boolean dbcheck, String dbchecktype, String substitutefirstcol, String dbchecksql, String dbcheckerroraction, String dbchecksub) {
        StringBuffer html = new StringBuffer();
        html.append("<tr><td  class='viewlistcol'><input onclick=\"setValidationValueDFD()\" id=\"DBCheck\" name=\"selector\" type=\"checkbox\" value=\"DBCheck\" " + (dbcheck ? "checked" : "") + ">");
        html.append("<label for=\"DBCheck\">Database Check</label></td>");
        html.append("<td  class='viewlistcol'>");
        html.append("<table style='border: 1px;'>");
        html.append("<tr><td colspan=4><font color=blue>").append(ValidationEditorUtil.getRuleDescription(tp, "DBCheck")).append("</font></td></tr>");
        html.append("<tr>");
        html.append("<td style='border: 1px;'>Type</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<input type=\"radio\" onclick=\"autoCheck('DBCheck');setValidationValueDFD()\" name=\"DBCheckType\" id=\"DBCheckNotExists\"" + (dbchecktype.equals("NotExists") ? "checked" : "") + " >Not Exists</input>");
        html.append("<input type=\"radio\" onclick=\"autoCheck('DBCheck');setValidationValueDFD()\" name=\"DBCheckType\" id=\"DBCheckExists\"" + (dbchecktype.equals("Exists") ? "checked" : "") + " >Exists</input>");
        html.append("<input type=\"checkbox\" onclick=\"setValidationValueDFD()\" name=\"DBCheckType\" id=\"DBCheckSubKeyCol\"" + (substitutefirstcol.equals("Y") ? "checked" : "") + " >Substitute First Column</input>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td style='border: 1px;'>SQL</td>");
        html.append("<td style='border: 1px;'>");
        html.append("<textarea name=\"DBCheckSQL\" onfocus=\"autoCheck('DBCheck');\" id=\"DBCheckSQL\" class=\"input_field\" rows=\"4\" cols=\"80\"  onchange=\"setValidationValueDFD()\">" + StringUtil.unescape(dbchecksql) + "</textarea></td>");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("</table>");
        html.append("</td>");
        html.append(ValidationEditorUtil.getErrorActionHtml(tp, dbcheck, "DBCheck", dbcheckerroraction, dbchecksub, "string"));
        return html.toString();
    }

    private static String getErrorActionHtml(TranslationProcessor tp, boolean checked, String ruleid, String erroraction, String errorsub, String fielddatatype) {
        StringBuffer html = new StringBuffer();
        if (checked) {
            html.append("<td  class='viewlistcol'>");
            html.append("<div id='" + ruleid.toLowerCase() + "validationerror'>");
            html.append("<table style='border: 0px;'>");
            html.append("<tr><td>");
            if (erroraction.startsWith("Error")) {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Error\" checked>" + tp.translate("Error") + "</input>");
            } else {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Error\">" + tp.translate("Error") + "</input>");
            }
            html.append("</td></tr>");
            html.append("<tr><td>");
            if (erroraction.startsWith("Skip")) {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Skip\" checked>" + tp.translate("Skip") + "</input>");
            } else {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Skip\" >" + tp.translate("Skip") + "</input>");
            }
            html.append("</td></tr>");
            html.append("<tr><td>");
            if (erroraction.startsWith("Sub")) {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Sub\" checked>" + tp.translate("Substitute") + "</input><input type=\"input\" id=\"" + ruleid + "SubText\" onchange=\"checkType( '" + ruleid + "SubText', '" + fielddatatype + "' );setValidationValue()\" value=\"" + errorsub + "\"/></td>");
            } else {
                html.append("<input type=\"radio\" onclick=\"setValidationValueDFD()\" name=\"" + ruleid + "ErrorAction\" id=\"" + ruleid + "Sub\">" + tp.translate("Substitute") + "</input><input type=\"input\" id=\"" + ruleid + "SubText\" onchange=\"checkType( '" + ruleid + "SubText', '" + fielddatatype + "');setValidationValue()\" value=\"" + errorsub + "\"/></td>");
            }
            html.append("</td></tr>");
            html.append("</table>");
            html.append("</div>");
            html.append("</td>");
        } else {
            html.append("<td  class='viewlistcol'>");
            html.append("<div id='" + ruleid.toLowerCase() + "validationerror'>");
            html.append("</div>");
            html.append("</td>");
        }
        return html.toString();
    }

    public static String getRuleDescription(TranslationProcessor tp, String rule) {
        String str = "";
        if (rule.equals("Mandatory")) {
            str = "The Mandatory check verifies if it is acceptable for the this field to have empty values. If no criteria is specified, this field should always be non-empty. If a criteria is specified and true, then this field should always be non-empty.";
        } else if (rule.equals("DistinctCheck")) {
            str = "The Distinct check ensures that the value of the  field(s) specified, is unique across the file being imported.";
        } else if (rule.equals("ValueCheck")) {
            str = "The Value check ensures that the value of the this field meets the criteria defined. The criteria can check this field's value against constants or other field's values.   All conditions of the criteria must be true for the criteria to be true.  Date fields may be compared to relative date constants like Today, Tomorrow, etc";
        } else if (rule.equals("ValueList")) {
            str = "The Value List check ensures that this field's value matches any of the values enumerated.  Optionally, the enumerated values may define a substitute value used in the processing rules.";
        } else if (rule.equals("ValueRefType")) {
            str = "The Value Reftype check ensures that this field's value matches either the RefValue or RefDsplayValue of the specified RefType. Optionally, matched RefDisplayValue may be replaced with the corresponding RefValue used in the processing rules.";
        } else if (rule.equals("GroovyCheck")) {
            str = "The Groovy Value check evaluates to a boolean(true/false), and if false, this field's value is invalid. The Groovy expression may reference the current field's value using \"[this.value]\", or alternate fields using \"[fieldname.value]\".";
        } else if (rule.equals("RegExCheck")) {
            str = "The Regular Expression check confirms that this field's value matches the specified pattern.";
        } else if (rule.equals("SDICheck")) {
            str = "The SDI check determines if a matching object is found in the database or not. Optionally, specify if the value is to be substituted by the primary key.";
        } else if (rule.equals("DBCheck")) {
            str = "The Database check determines if the object is found, or is not found in the database.   This can be expressed either declaratively by specifying the SDC, and column value to check or can be written to be a full SQL statement.  Optionally, a substituted value can be chosen to be used in the processing rules.";
        } else if (rule.equals("DateFormatCheck")) {
            str = " The Date Format check assures that date values within string cells have the proper format. Furthermore, the time zone of the dates are specified so that the persisted date value in LIMS is correct.";
        } else if (rule.equals("NumberFormatCheck")) {
            str = "The Number Format check assures that numeric values within string cells have the proper decimal and grouping separators.";
        }
        return tp.translate(str);
    }

    public static String getValueCheckErrorMessage(TranslationProcessor translationProcessor, PropertyList fieldProps, String value, String row, String column, String conditionNotMet) {
        String str = "";
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldProps.getProperty("title"));
        valueMap.put("value", value);
        valueMap.put("cell", column + row);
        str = "[title] ([value]) in cell ([cell]), does not meet the validation condition ";
        str = translationProcessor.translate(str, valueMap);
        String[] tokens = StringUtil.split(conditionNotMet, EXPRESSION_SEPARATOR);
        if (tokens.length == 3) {
            str = str + "(" + translationProcessor.translate(ValidationEditorUtil.getOpStr(fieldProps.getProperty("datatype"), tokens[1])) + " " + tokens[2] + ")";
        }
        str = str + ".";
        return str;
    }

    public static String getParseErrorMessage(TranslationProcessor translationProcessor, String value, String datatype, String row, String column) {
        String str = "Cell [cell]([value]) does not have a valid [type].";
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("value", value);
        valueMap.put("cell", column + row);
        valueMap.put("type", datatype.equals("number") ? "number" : "date");
        str = translationProcessor.translate(str, valueMap);
        return str;
    }

    private static String getOpStr(String datatype, String op) {
        if (datatype.equals("number")) {
            if (op.equals("EQ")) {
                return "Equals";
            }
            if (op.equals("NE")) {
                return "Not Equals";
            }
            if (op.equals("GT")) {
                return "Greater Than";
            }
            if (op.equals("GE")) {
                return "Greater Than or Equals";
            }
            if (op.equals("LT")) {
                return "Less Than";
            }
            if (op.equals("LE")) {
                return "Less Than or Equals";
            }
        } else if (datatype.equals("string")) {
            if (op.equals("EQ")) {
                return "Equals";
            }
            if (op.equals("NE")) {
                return "Not Equals";
            }
            if (op.equals("CT")) {
                return "Contains";
            }
            if (op.equals("DC")) {
                return "Does Not Contain";
            }
            if (op.equals("SW")) {
                return "Starts With";
            }
            if (op.equals("EW")) {
                return "Ends With";
            }
        } else if (datatype.equals("date")) {
            if (op.equals("LT")) {
                return "Before";
            }
            if (op.equals("GT")) {
                return "After";
            }
            if (op.equals("LE")) {
                return "On or Before";
            }
            if (op.equals("GE")) {
                return "On or After";
            }
        } else {
            if (op.equals("EQ")) {
                return "Equals";
            }
            if (op.equals("NE")) {
                return "Not Equals";
            }
        }
        if (op.equals("EQ")) {
            return "Equals";
        }
        if (op.equals("NE")) {
            return "Not Equals";
        }
        if (op.equals("GT")) {
            return "Greater Than";
        }
        if (op.equals("GE")) {
            return "Greater Than or Equals";
        }
        if (op.equals("LT")) {
            return "Less Than";
        }
        if (op.equals("LE")) {
            return "Less Than or Equals";
        }
        return op;
    }

    public static String getErrorMessage(TranslationProcessor translationProcessor, String rule, PropertyList fieldProps, String value, String row, String column) {
        String str = "";
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldProps.getProperty("title"));
        valueMap.put("cell", column + row);
        valueMap.put("value", value);
        str = rule.equals("Mandatory") ? translationProcessor.translate("[title] in cell ([cell]) is empty and required.", valueMap) : (rule.equals("GroovyCheck") ? translationProcessor.translate("[title] ([value]) in cell ([cell]), does not meet the validation criteria specified in groovy.", valueMap) : (rule.equals("RegExCheck") ? translationProcessor.translate("[title] ([value]) in cell ([cell]), does not match the required pattern.", valueMap) : (rule.equals("DistinctCheck") ? translationProcessor.translate("[title] ([value]) in cell ([cell]), is not distinct.", valueMap) : translationProcessor.translate("[title] ([value]) in cell ([cell]), does not meet the validation criteria specified.", valueMap))));
        return str;
    }

    public static String getDistinctErrorMessage(TranslationProcessor translationProcessor, String fieldidList, String valueList, String row) {
        String str = "";
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldidList);
        valueMap.put("row", row);
        valueMap.put("value", valueList);
        str = translationProcessor.translate("[title] ([value]) in row ([row]), is not distinct.", valueMap);
        return str;
    }

    public static String getValueListErrorMessage(TranslationProcessor translationProcessor, String values, PropertyList fieldProps, String value, String row, String column) {
        String[] valueitems = StringUtil.split(values, LISTITEM_SEPARATOR);
        String valuesstr = "";
        for (int i = 0; i < valueitems.length; ++i) {
            if (valuesstr.length() > 0) {
                valuesstr = valuesstr + ", ";
            }
            String[] pars = StringUtil.split(valueitems[i], EXPRESSION_SEPARATOR);
            valuesstr = valuesstr + StringUtil.unescape(pars[0]);
        }
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldProps.getProperty("title"));
        valueMap.put("cell", column + row);
        valueMap.put("value", value);
        valueMap.put("valuelist", valuesstr);
        return translationProcessor.translate("[title] ([value]) in cell ([cell]), does not match any of the values in the list ([valuelist])", valueMap);
    }

    public static String getValueReftypeErrorMessage(TranslationProcessor translationProcessor, String reftype, String option, PropertyList fieldProps, String value, String row, String column) {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldProps.getProperty("title"));
        valueMap.put("cell", column + row);
        valueMap.put("value", value);
        valueMap.put("reftype", reftype);
        if (option.equals("Option1")) {
            return translationProcessor.translate("[title] ([value]) in cell ([cell]), does not match any of the RefType Values for [reftype]", valueMap);
        }
        return translationProcessor.translate("[title] ([value]) in cell ([cell]), does not match any of the RefType Display Values for [reftype]", valueMap);
    }

    public static String getSDICheckErrorMessage(TranslationProcessor translationProcessor, PropertyList fieldProps, String value, String row, String column, String type, String sdc, String keycolumn) {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("title", fieldProps.getProperty("title"));
        valueMap.put("cell", column + row);
        valueMap.put("value", value);
        String str = "";
        str = type.equals("Exists") ? translationProcessor.translate("[title] ([value]) in cell ([cell]) does not exist.", valueMap) : translationProcessor.translate("[title] ([value]) in cell ([cell]) already exists.", valueMap);
        return str;
    }

    public static String getValidationDescriptionHtml(String validationrule, String fieldtype, TranslationProcessor translationProcessor) {
        if (validationrule.length() > 0) {
            String[] rules = StringUtil.split(validationrule, RULES_SEPARATOR);
            String desc = "";
            for (int i = 0; i < rules.length; ++i) {
                if (rules[i].length() <= 0) continue;
                desc = desc + "<P>" + ValidationEditorUtil.interpretRule(rules[i], fieldtype, translationProcessor);
            }
            return desc;
        }
        return "";
    }

    public static String interpretRule(String rule, String fieldtype, TranslationProcessor translationProcessor) {
        String[] tokens;
        String checkstr;
        String criteria;
        String str = "";
        if (rule.startsWith("Mandatory")) {
            str = str + translationProcessor.translate("Mandatory");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0 && (criteria = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"))).length() > 0) {
                String[] mandatoryconditions = StringUtil.split(criteria, LISTITEM_SEPARATOR);
                for (int i = 0; i < mandatoryconditions.length; ++i) {
                    if (i == 0) {
                        str = str + translationProcessor.translate(" if");
                    }
                    String[] tokens2 = StringUtil.split(mandatoryconditions[i], EXPRESSION_SEPARATOR);
                    str = str + " " + tokens2[0] + " " + ValidationEditorUtil.getOpStr(fieldtype, tokens2[1]) + " " + tokens2[2];
                    if (i + 1 == mandatoryconditions.length) continue;
                    str = str + translationProcessor.translate(" and ");
                }
            }
        } else if (rule.startsWith("DistinctCheck")) {
            str = str + translationProcessor.translate("Distinct Check");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0) {
                String distinctfieldlist = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"));
                distinctfieldlist = distinctfieldlist.replaceAll("#SEMICOLON#", RULES_SEPARATOR);
                str = str + translationProcessor.translate(" On Field(s):") + distinctfieldlist;
            }
        } else if (rule.startsWith("ValueList")) {
            str = str + translationProcessor.translate("Value List");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0) {
                String valuelist = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"));
                valuelist = StringUtil.unescape(valuelist);
                valuelist = valuelist.replaceAll("#SEMICOLON#", RULES_SEPARATOR);
                String[] items = StringUtil.split(valuelist, LISTITEM_SEPARATOR);
                String liststr = "";
                for (int i = 0; i < items.length; ++i) {
                    if (i != 0) {
                        liststr = liststr + ", ";
                    }
                    if (items[i].contains(EXPRESSION_SEPARATOR)) {
                        String[] parts = StringUtil.split(items[i], EXPRESSION_SEPARATOR);
                        liststr = liststr + parts[0] + "(" + parts[1] + ")";
                        continue;
                    }
                    liststr = liststr + items[i];
                }
                str = str + ": " + liststr;
            }
        } else if (rule.startsWith("ValueRefType")) {
            str = str + translationProcessor.translate("Value RefType");
            String valuereftype = "";
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0) {
                valuereftype = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"));
            }
            String valuereftypeoption = "";
            if (rule.contains("Option")) {
                valuereftypeoption = rule.substring(rule.indexOf("Option"), rule.indexOf("Option") + 7);
                valuereftypeoption = valuereftypeoption.equals("Option1") ? "Field Contains RefType Value" : (valuereftypeoption.equals("Option2") ? "Field Contains RefType Display Value" : "Field Contains RefType Display Value, Substitute with RefType Value\n");
            }
            str = str + " :" + valuereftype + "(" + translationProcessor.translate(valuereftypeoption) + ")";
        } else if (rule.startsWith("ValueCheck")) {
            str = str + translationProcessor.translate("Value Check:");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0 && (criteria = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"))).length() > 0) {
                String[] valuecheckconditions = StringUtil.split(criteria, LISTITEM_SEPARATOR);
                for (int i = 0; i < valuecheckconditions.length; ++i) {
                    if (i != 0) {
                        str = str + ",";
                    }
                    String[] tokens3 = StringUtil.split(valuecheckconditions[i], EXPRESSION_SEPARATOR);
                    str = str + " " + tokens3[0] + " " + translationProcessor.translate(ValidationEditorUtil.getOpStr(fieldtype, tokens3[1])) + " " + tokens3[2];
                }
            }
        } else if (rule.startsWith("SDICheck")) {
            str = str + translationProcessor.translate("SDI Check");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0 && (checkstr = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"))).length() > 0) {
                tokens = StringUtil.split(checkstr, EXPRESSION_SEPARATOR);
                str = str + translationProcessor.translate("SDI Check Type:") + tokens[0];
                if (tokens.length > 1) {
                    str = str + ", " + translationProcessor.translate("SDC:") + tokens[1];
                }
                if (tokens.length > 2) {
                    str = str + ", " + translationProcessor.translate("Column:") + tokens[2];
                }
                if (tokens.length > 3) {
                    str = str + ", " + translationProcessor.translate("Substitute Column:") + tokens[3];
                }
                if (tokens.length > 4) {
                    str = str + ", " + translationProcessor.translate("From:") + tokens[4];
                }
                if (tokens.length > 5) {
                    String sdicheckwhereclause = StringUtil.unescape(tokens[5]);
                    str = str + ", " + translationProcessor.translate("Where:") + sdicheckwhereclause;
                }
            }
        } else if (rule.startsWith("DateFormatCheck")) {
            str = str + translationProcessor.translate("Date Format Check");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0 && (checkstr = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"))).length() > 0) {
                tokens = StringUtil.split(checkstr, EXPRESSION_SEPARATOR);
                str = str + translationProcessor.translate("Locale:") + tokens[0];
                if (tokens.length > 1) {
                    String dateformat = tokens[1].replaceAll("#COLON#", EXPRESSION_SEPARATOR);
                    str = str + ", " + translationProcessor.translate("Date Format: ") + dateformat;
                }
                if (tokens.length > 2) {
                    str = str + ", " + translationProcessor.translate("Time Zone: ") + tokens[2];
                }
            }
        } else if (rule.startsWith("NumberFormatCheck")) {
            str = str + translationProcessor.translate("Number Format Check: ");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0 && (checkstr = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"))).length() > 0) {
                tokens = StringUtil.split(checkstr, EXPRESSION_SEPARATOR);
                str = str + translationProcessor.translate("Decimal Separator:") + tokens[0];
                str = str + " " + translationProcessor.translate("Group Separator:") + tokens[1];
            }
        } else if (rule.startsWith("RegExCheck")) {
            str = str + translationProcessor.translate("Regular Expression Check");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0) {
                String regex = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"));
                str = str + ": " + regex;
            }
        } else if (rule.startsWith("GroovyCheck")) {
            str = str + translationProcessor.translate("Groovy Check: ");
            if (rule.length() > 0 && rule.indexOf("(") > 0 && rule.indexOf(")") > 0) {
                String groovyscript = rule.substring(rule.indexOf("(") + 1, rule.indexOf(")"));
                groovyscript = StringUtil.unescape(groovyscript);
                str = str + groovyscript;
            }
        }
        if (rule.length() > 0 && rule.contains("ErrorOp")) {
            String erroraction = rule.substring(rule.indexOf("ErrorOp=") + 8);
            if (erroraction.startsWith("Sub(")) {
                String rulesub = erroraction.substring(erroraction.indexOf("(") + 1, erroraction.indexOf(")"));
                str = str + " " + translationProcessor.translate("On Error Substitute with") + "(" + rulesub + ")";
            } else {
                str = erroraction.equals("Error") ? str + ", " + translationProcessor.translate("On Error Abort") : str + ", " + translationProcessor.translate("On Error Skip Item");
            }
        }
        return str;
    }
}

