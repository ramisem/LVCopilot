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

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetAssayTypeSampleTypeWorkitems
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60863 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String assayTypeId = ajaxResponse.getRequestParameter("assaytypeid");
        String sampleTypeId = ajaxResponse.getRequestParameter("sampletypeid");
        String responseData = this.getAssayTypeWorkItems(assayTypeId, sampleTypeId, this.getQueryProcessor());
        ajaxResponse.addCallbackArgument("list", responseData);
        ajaxResponse.print();
    }

    private String getAssayTypeWorkItems(String assayTypeIds, String sampleTypeIds, QueryProcessor qp) {
        StringBuffer responseData = new StringBuffer("{\"assaytypesampletypes\" :[");
        String[] assayTypes = StringUtil.split(assayTypeIds, ";");
        String[] sampleTypes = StringUtil.split(sampleTypeIds, ";");
        SafeSQL safeSQL = new SafeSQL();
        DataSet assayTypeSampleTypes = qp.getPreparedSqlDataSet("SELECT DISTINCT s_sampletypeid, s_assaytypeid FROM s_assaytypesampletype   WHERE s_assaytypeid IN (" + safeSQL.addIn(assayTypeIds, ";") + ")", safeSQL.getValues());
        safeSQL.reset();
        DataSet sampleTypeContType = qp.getPreparedSqlDataSet("SELECT s_sampletypeid, containertypeid FROM s_sampletypecontainertype WHERE  s_sampletypeid IN (" + safeSQL.addIn(sampleTypeIds, ";") + ") AND ACTIVEFLAG = 'Y' ORDER BY containertypeid", safeSQL.getValues());
        DataSet workItems = null;
        if (assayTypeSampleTypes != null && assayTypeSampleTypes.size() > 0) {
            safeSQL.reset();
            workItems = qp.getPreparedSqlDataSet("SELECT DISTINCT s_assaytypeid, workitemid FROM s_assaytypeworkitem  WHERE s_assaytypeid IN (" + safeSQL.addIn(assayTypeSampleTypes.getColumnValues("s_assaytypeid", "','")) + ") ORDER BY s_assaytypeid, workitemid", safeSQL.getValues());
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (String assayType : assayTypes) {
            for (String sampleType : sampleTypes) {
                responseData.append("{ \"assaytype\":\"").append(assayType).append("\"");
                responseData.append(",\"sampletype\":\"").append(sampleType).append("\"");
                responseData.append(",\"workitems\":[");
                String workitemVersionData = "";
                if (assayTypeSampleTypes != null && assayTypeSampleTypes.size() > 0) {
                    findMap.put("s_sampletypeid", sampleType);
                    findMap.put("s_assaytypeid", assayType);
                    int findRow = assayTypeSampleTypes.findRow(findMap);
                    if (findRow > -1 && workItems != null && workItems.size() > 0) {
                        filterMap.put("s_assaytypeid", assayType);
                        DataSet assayTypeWorkItems = workItems.getFilteredDataSet(filterMap);
                        if (assayTypeWorkItems.size() > 0) {
                            String workItemIds = assayTypeWorkItems.getColumnValues("workitemid", ",");
                            responseData.append("\"").append(workItemIds.replaceAll(",", "\",\"")).append("\"");
                            workitemVersionData = assayTypeWorkItems.getColumnValues("workitemversionid", ",");
                            workitemVersionData = "\"" + workitemVersionData.replaceAll(",", "\",\"") + "\"";
                        }
                    }
                }
                responseData.append("],\"workitemversions\":[").append(workitemVersionData);
                responseData.append("],\"specimentypes\":\"");
                if (sampleTypeContType != null && sampleTypeContType.size() > 0) {
                    filterMap.clear();
                    filterMap.put("s_sampletypeid", sampleType);
                    DataSet specimens = sampleTypeContType.getFilteredDataSet(filterMap);
                    if (specimens != null && specimens.size() > 0) {
                        String specimenTypes = specimens.getColumnValues("containertypeid", ";");
                        responseData.append(specimenTypes);
                    }
                }
                responseData.append("\"},");
                findMap.clear();
                filterMap.clear();
            }
        }
        if (responseData.toString().endsWith(",")) {
            responseData.setLength(responseData.length() - 1);
        }
        responseData.append("]}");
        return responseData.toString();
    }
}

