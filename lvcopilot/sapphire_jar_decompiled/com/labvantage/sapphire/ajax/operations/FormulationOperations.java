/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.math.BigDecimal;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class FormulationOperations
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54062 $";
    public static final String OPERATION_GENERATEFORMULATIONLABEL = "generateformulationlabel";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String operation = ajaxResponse.getRequestParameter("operation", "");
        if (operation.equals(OPERATION_GENERATEFORMULATIONLABEL)) {
            this.generateFormulationLabel(ajaxResponse);
        }
        ajaxResponse.print();
    }

    private void generateFormulationLabel(AjaxResponse ajaxResponse) {
        String errorMessage = "";
        String newLabel = "";
        BigDecimal userSequence = new BigDecimal(1000000000);
        String formulationProjectId = ajaxResponse.getRequestParameter("formulationprojectid", "");
        boolean childMode = ajaxResponse.getRequestParameter("copymode", "PARENT").equals("CHILD");
        String parentProductId = ajaxResponse.getRequestParameter("parentproductid", "");
        String parentProductVersionId = ajaxResponse.getRequestParameter("parentproductversionid", "");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT formulationlabel, templateflag, usersequence, s_productid, s_productversionid, parentproductid, parentproductversionid FROM s_product WHERE formulationprojectid = " + safeSQL.addVar(formulationProjectId);
        DataSet formulationDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("s_productid", parentProductId);
        filter.put("s_productversionid", parentProductVersionId);
        DataSet formulationTemplateDs = formulationDs.getFilteredDataSet(filter);
        String copyMode = ajaxResponse.getRequestParameter("copymode", "PARENT");
        boolean childSiblingMode = false;
        if (copyMode.equals("SIBLING") && formulationTemplateDs.getRowCount() > 0 && formulationTemplateDs.getValue(0, "parentproductid", "").length() > 0) {
            childSiblingMode = true;
        }
        if (childSiblingMode) {
            copyMode = "CHILDSIBLING";
        }
        if (formulationDs.getRowCount() > 0) {
            if (!childMode && !childSiblingMode) {
                filter = new HashMap();
                filter.put("templateflag", "Y");
                formulationTemplateDs = formulationDs.getFilteredDataSet(filter);
            }
            if (formulationTemplateDs.getRowCount() > 0) {
                String labelTemplate = "";
                DataSet formulationIterationDs = null;
                if (childMode) {
                    labelTemplate = formulationTemplateDs.getValue(0, "formulationlabel", "").trim();
                    formulationIterationDs = formulationDs.getFilteredDataSet(filter, true);
                    filter = new HashMap();
                    filter.put("parentproductid", parentProductId);
                    filter.put("parentproductversionid", parentProductVersionId);
                    formulationIterationDs = formulationIterationDs.getFilteredDataSet(filter);
                } else if (childSiblingMode) {
                    String templateParentProductId = formulationTemplateDs.getValue(0, "parentproductid", "");
                    String templateParentProductVersionId = formulationTemplateDs.getValue(0, "parentproductversionid", "1");
                    filter = new HashMap();
                    filter.put("s_productid", templateParentProductId);
                    filter.put("s_productversionid", templateParentProductVersionId);
                    formulationTemplateDs = formulationDs.getFilteredDataSet(filter);
                    labelTemplate = formulationTemplateDs.getValue(0, "formulationlabel", "").trim();
                    filter = new HashMap();
                    filter.put("parentproductid", templateParentProductId);
                    filter.put("parentproductversionid", templateParentProductVersionId);
                    formulationIterationDs = formulationDs.getFilteredDataSet(filter);
                } else {
                    labelTemplate = formulationTemplateDs.getValue(0, "formulationlabel", "").trim();
                    formulationIterationDs = formulationDs.getFilteredDataSet(filter, true);
                    filter = new HashMap();
                    filter.put("parentproductid", null);
                    filter.put("parentproductversionid", null);
                    formulationIterationDs = formulationIterationDs.getFilteredDataSet(filter);
                }
                int index = 0;
                for (int i = 0; i < formulationIterationDs.getRowCount(); ++i) {
                    String iterationLabel = formulationIterationDs.getValue(i, "formulationlabel", labelTemplate);
                    if (!iterationLabel.startsWith(labelTemplate)) continue;
                    String indexString = iterationLabel.substring(labelTemplate.length()).trim();
                    if (indexString.startsWith(".")) {
                        indexString = indexString.substring(1);
                    }
                    try {
                        int temp = Integer.parseInt(indexString);
                        if (temp <= index) continue;
                        index = temp;
                        continue;
                    }
                    catch (NumberFormatException temp) {
                        // empty catch block
                    }
                }
                newLabel = childMode || childSiblingMode ? labelTemplate + "." + index : labelTemplate + " " + ++index;
                if (childMode || childSiblingMode) {
                    BigDecimal templateUserSequence = formulationTemplateDs.getBigDecimal(0, "usersequence");
                    if (templateUserSequence.remainder(new BigDecimal(100)).compareTo(BigDecimal.ZERO) != 0) {
                        errorMessage = this.getTranslationProcessor().translate("Only 3 levels of child formulations allowed");
                    } else {
                        BigDecimal significantNonZeroCounter = BigDecimal.ONE;
                        for (int i = 0; i < 3; ++i) {
                            BigDecimal significantDigit = templateUserSequence.remainder(significantNonZeroCounter = significantNonZeroCounter.multiply(new BigDecimal(100)));
                            if (significantDigit.compareTo(BigDecimal.ZERO) == 0) continue;
                            significantNonZeroCounter = significantNonZeroCounter.divide(new BigDecimal(100));
                            break;
                        }
                        userSequence = templateUserSequence.add(significantNonZeroCounter.divide(BigDecimal.TEN).multiply(new BigDecimal(formulationIterationDs.getRowCount() + 1)));
                    }
                } else {
                    userSequence = userSequence.multiply(new BigDecimal(formulationIterationDs.getRowCount() + 1));
                }
            } else {
                errorMessage = this.getTranslationProcessor().translate("Formulation-template not defined.");
            }
        } else {
            errorMessage = this.getTranslationProcessor().translate("Failed to fetch formulations and formulation-template.");
        }
        ajaxResponse.addCallbackArgument("newlabel", newLabel);
        ajaxResponse.addCallbackArgument("usersequence", userSequence.toString());
        ajaxResponse.addCallbackArgument("copymode", copyMode);
        ajaxResponse.addCallbackArgument("errormessage", errorMessage);
    }
}

