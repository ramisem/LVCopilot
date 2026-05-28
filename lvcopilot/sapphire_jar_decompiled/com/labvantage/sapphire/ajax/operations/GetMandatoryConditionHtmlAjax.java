/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.datafile.ValidationEditorUtil;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class GetMandatoryConditionHtmlAjax
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "mandatoryConditionHtml_Callback");
        String rowcount = ajaxResponse.getRequestParameter("rowcount");
        String deleterow = ajaxResponse.getRequestParameter("deleterow");
        String condition = ajaxResponse.getRequestParameter("condition");
        String fielddatatype = ajaxResponse.getRequestParameter("fielddatatype");
        String rule = ajaxResponse.getRequestParameter("rule");
        String fieldid = ajaxResponse.getRequestParameter("fieldid");
        PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String conditionshtml = "";
        if (rule.equals("ValueList")) {
            int deleteRowNumber = -1;
            if (deleterow.length() > 0) {
                deleteRowNumber = Integer.parseInt(deleterow);
            }
            String[] conditionlist = null;
            if (condition.length() > 0) {
                conditionlist = StringUtil.split(condition, "|");
            }
            conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, rule) + "</font></td></tr>";
            int lastrow = Integer.parseInt(rowcount);
            int displaycount = 0;
            for (int currrow = 0; currrow < lastrow; ++currrow) {
                String currentcondition = "";
                if (conditionlist != null && conditionlist.length > currrow) {
                    currentcondition = conditionlist[currrow];
                }
                try {
                    if (deleteRowNumber == currrow) continue;
                    String curritemhtml = ValidationEditorUtil.getValueListRow(currentcondition, currrow);
                    String sRowHtml = "<table><tr>\n";
                    sRowHtml = displaycount > 0 ? sRowHtml + "<td width=\"10px\"><label>|</label></td>" : sRowHtml + "<td width=\"10px\"><label> </label></td>";
                    sRowHtml = sRowHtml + "<td>" + curritemhtml + "</td></tr></table>\n";
                    conditionshtml = conditionshtml + "<tr><td>" + sRowHtml + "</td></tr>";
                    ++displaycount;
                    continue;
                }
                catch (SapphireException e) {
                    ajaxResponse.addCallbackArgument("response", "<P>Failed to fetch dropdown!");
                    ajaxResponse.print();
                    return;
                }
            }
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td><table><tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addValueListItem( )\">" + tp.translate("Add Item") + "</button></td>";
            conditionshtml = conditionshtml + "</tr></table></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
        } else if (rule.equals("DistinctCheck")) {
            int deleteRowNumber = -1;
            if (deleterow.length() > 0) {
                deleteRowNumber = Integer.parseInt(deleterow);
            }
            String[] conditionlist = null;
            if (condition.length() > 0) {
                conditionlist = StringUtil.split(condition, "|");
            } else {
                condition = fieldid;
                conditionlist = StringUtil.split(condition, "|");
            }
            conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, rule) + "</font></td></tr>";
            int lastrow = Integer.parseInt(rowcount);
            int displaycount = 0;
            for (int currrow = 0; currrow < lastrow; ++currrow) {
                String currentcondition = "";
                if (conditionlist != null && conditionlist.length > currrow) {
                    currentcondition = conditionlist[currrow];
                }
                try {
                    if (deleteRowNumber == currrow) continue;
                    String curritemhtml = ValidationEditorUtil.getDistinctFieldItemHtml(pageContext, currentcondition, currrow);
                    String sRowHtml = "<table><tr>\n";
                    sRowHtml = displaycount > 0 ? sRowHtml + "<td width=\"10px\"><label>&</label></td>" : sRowHtml + "<td width=\"10px\"><label> </label></td>";
                    sRowHtml = sRowHtml + "<td>" + curritemhtml + "</td></tr></table>\n";
                    conditionshtml = conditionshtml + "<tr><td>" + sRowHtml + "</td></tr>";
                    ++displaycount;
                    continue;
                }
                catch (SapphireException e) {
                    ajaxResponse.addCallbackArgument("response", "<P>Failed to fetch dropdown!");
                    ajaxResponse.print();
                    return;
                }
            }
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td><table><tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"addDistinctFieldItem( )\">" + tp.translate("Add Field") + "</button></td>";
            conditionshtml = conditionshtml + "</tr></table></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
        } else {
            if (fielddatatype == null || fielddatatype.length() == 0) {
                fielddatatype = "any";
            }
            int deleteRowNumber = -1;
            if (deleterow.length() > 0) {
                deleteRowNumber = Integer.parseInt(deleterow);
            }
            String[] conditionlist = null;
            if (condition.length() > 0) {
                conditionlist = StringUtil.split(condition, "|");
            }
            conditionshtml = "<table>";
            conditionshtml = conditionshtml + "<tr><td colspan=4><font color=blue>" + ValidationEditorUtil.getRuleDescription(tp, rule) + "</font></td></tr>";
            int lastrow = Integer.parseInt(rowcount);
            int displaycount = 0;
            for (int currrow = 0; currrow < lastrow; ++currrow) {
                String currentcondition = "";
                if (conditionlist != null && conditionlist.length > currrow) {
                    currentcondition = conditionlist[currrow];
                }
                try {
                    String[] tokens;
                    if (deleteRowNumber == currrow) continue;
                    String lhsvalue = "";
                    String rhsvalue = "";
                    String op = "";
                    if (currentcondition.length() > 0 && (tokens = StringUtil.split(currentcondition, ":")).length == 3) {
                        lhsvalue = tokens[0];
                        op = tokens[1];
                        rhsvalue = tokens[2];
                    }
                    String deleteButtonHtml = "\n<img src=\"WEB-CORE/images/png/Delete.png\" onclick=\"delete" + rule + "Condition(" + displaycount + ");\"/>";
                    String listoffieldslhs = "";
                    listoffieldslhs = rule.startsWith("ValueCheck") ? ValidationEditorUtil.getFieldValueLengthDropdown(rule.toLowerCase() + "dropdownlhs" + displaycount, lhsvalue) : ValidationEditorUtil.getFieldsComboBox(pageContext, rule.toLowerCase() + "dropdownlhs" + displaycount, lhsvalue);
                    String comparisonop = ValidationEditorUtil.getComparisonOperatorDropdown(tp, lhsvalue, fielddatatype, rule.toLowerCase() + "conditionop" + displaycount, op);
                    String listoffieldsrhs = ValidationEditorUtil.getFieldsComboBox(pageContext, rule.toLowerCase() + "dropdownrhs" + displaycount, rhsvalue);
                    String sRowHtml = "<table><tr>\n";
                    sRowHtml = displaycount > 0 ? sRowHtml + "<td width=\"10px\"><label>&amp;</label></td>" : sRowHtml + "<td width=\"10px\"><label> </label></td>";
                    sRowHtml = sRowHtml + "<td>" + listoffieldslhs + "\n" + comparisonop + "\n" + listoffieldsrhs + "\n" + deleteButtonHtml + "</td></tr></table>\n";
                    conditionshtml = conditionshtml + "<tr><td>" + sRowHtml + "</td></tr>";
                    ++displaycount;
                    continue;
                }
                catch (SapphireException e) {
                    ajaxResponse.addCallbackArgument("response", "<P>Failed to fetch dropdown!");
                    ajaxResponse.print();
                    return;
                }
            }
            conditionshtml = conditionshtml + "<tr>";
            conditionshtml = conditionshtml + "<td><table><tr>";
            conditionshtml = conditionshtml + "<td width=\"10px\"> </td>";
            conditionshtml = conditionshtml + "<td><button style=\"height:20px;width:100px;font-size:8pt;\" onclick=\"add" + rule + "Condition( '" + fielddatatype + "' )\">" + tp.translate("Add Condition") + "</button></td>";
            conditionshtml = conditionshtml + "</tr></table></td>";
            conditionshtml = conditionshtml + "</tr>";
            conditionshtml = conditionshtml + "</table>";
        }
        ajaxResponse.addCallbackArgument("response", conditionshtml);
        ajaxResponse.print();
    }
}

