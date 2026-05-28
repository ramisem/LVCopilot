/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetStudyDisplayValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid");
        String displayformat = ajaxResponse.getRequestParameter("displayformat");
        String displayvalue = studyid;
        if (OpalUtil.isNotEmpty(displayformat) && displayformat.contains("[")) {
            List<String> list = Arrays.asList(StringUtil.getTokens(displayformat));
            ArrayList<String> columnList = new ArrayList<String>();
            for (String columnid : list) {
                String sdcolumnid = this.getSDCProcessor().getSDCColumnProperty("Study", columnid, "columnid");
                if (!OpalUtil.isNotEmpty(columnid) || !columnid.equalsIgnoreCase(sdcolumnid)) continue;
                columnList.add(columnid);
            }
            if (columnList.size() > 0) {
                String sql = "select " + OpalUtil.toDelimitedString(columnList, ",") + " from s_study where s_studyid = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{studyid});
                if (ds != null && ds.size() > 0) {
                    for (String token : list) {
                        displayformat = StringUtil.replaceAll(displayformat, "[" + token + "]", ds.getValue(0, token));
                    }
                    displayvalue = displayformat;
                }
            } else {
                displayvalue = "";
            }
        }
        ajaxResponse.addCallbackArgument("displayvalue", displayvalue);
        ajaxResponse.print();
    }
}

