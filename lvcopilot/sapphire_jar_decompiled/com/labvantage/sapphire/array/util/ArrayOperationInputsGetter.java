/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.array.util;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ArrayOperationInputsGetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.arraryOp_AjaxCallback");
        String arrayOp = ajaxResponse.getRequestParameter("arrayoperation");
        String arrayId = ajaxResponse.getRequestParameter("arrayid");
        String returntolistpage = ajaxResponse.getRequestParameter("returntolistpage");
        String layoutfilter = ajaxResponse.getRequestParameter("layoutfilter");
        String methodfilter = ajaxResponse.getRequestParameter("methodfilter");
        String redirectPage = "";
        String location = "top";
        String label = "";
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditsignedflag = ajaxResponse.getRequestParameter("auditsignedflag", "");
        boolean valid = true;
        if (returntolistpage == null || returntolistpage.length() == 0) {
            returntolistpage = "LV_ArrayList";
        }
        try {
            if (!arrayOp.equals("AddArray")) {
                String[] arrayIds = StringUtil.split(arrayId, DELIMITER);
                for (int i = 0; i < arrayIds.length; ++i) {
                    valid = ArrayUtil.validateArrayOperation(this.getQueryProcessor(), arrayIds[i], arrayOp);
                    redirectPage = "ERROR: Invalid Status. Cannot apply operation " + arrayOp + " on Array " + arrayId;
                }
            }
        }
        catch (SapphireException e) {
            valid = false;
            this.logger.error("Failed to validate arrayoperation", e);
            redirectPage = "ERROR: Failed to validate array operation.";
        }
        if (!valid) {
            ajaxResponse.addCallbackArgument("label", "Error");
            ajaxResponse.addCallbackArgument("redirectto", redirectPage);
            ajaxResponse.addCallbackArgument("location", "blank");
            ajaxResponse.addCallbackArgument("arrayoperation", arrayOp);
            ajaxResponse.print();
            return;
        }
        if (arrayOp.equals("AddArray")) {
            redirectPage = "rc?command=file&file=WEB-CORE/modules/array/ArrayOperationInputs.jsp&arrayoperation=AddArray&methodfilter=" + methodfilter + "&layoutfilter=" + layoutfilter + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag;
            location = "blank";
            label = "Add Array";
        } else if (arrayOp.equals("ApplyArrayMethod")) {
            redirectPage = "rc?command=file&file=WEB-CORE/modules/array/ArrayOperationInputs.jsp&arrayoperation=ApplyArrayMethod&arrayid=" + arrayId + "&methodfilter=" + methodfilter + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag;
            location = "blank";
            label = "Apply Method";
        } else if (arrayOp.equals("LoadArray") || arrayOp.equals("LoadArrayZone")) {
            String item = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayId);
            String status = "";
            String arraymethodid = "";
            String arraymethodversionid = "1";
            String arraymethodinstance = "1";
            label = "Load";
            if (item.length() > 0) {
                String[] tokens = StringUtil.split(item, "|");
                arraymethodid = tokens[0];
                arraymethodversionid = tokens[4];
                status = tokens[2];
                arraymethodinstance = tokens[1];
            }
            if (item.length() == 0 || status.equals("Cancelled") || status.equals("Completed")) {
                label = "Apply Array Method";
                redirectPage = "rc?command=file&file=WEB-CORE/modules/array/ArrayOperationInputs.jsp&arrayoperation=" + arrayOp + "&arrayid=" + arrayId + "&methodfilter=" + methodfilter + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag;
                location = "blank";
            } else {
                redirectPage = arrayOp.equals("LoadArray") ? "rc?command=page&page=ArrayLoadingMaint&keyid1=" + arrayId + "&methodid=" + arraymethodid + "&methodversionid=" + arraymethodversionid + "&methodinstance=" + arraymethodinstance + "&returntolistpage=" + returntolistpage + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag : "rc?command=page&page=LoadArrayZone&keyid1=" + arrayId + "&methodid=" + arraymethodid + "&methodversionid=" + arraymethodversionid + "&returntolistpage=" + returntolistpage + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag;
                location = "top";
            }
        }
        ajaxResponse.addCallbackArgument("label", label);
        ajaxResponse.addCallbackArgument("redirectto", redirectPage);
        ajaxResponse.addCallbackArgument("location", location);
        ajaxResponse.addCallbackArgument("arrayoperation", arrayOp);
        ajaxResponse.print();
    }
}

