/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import com.labvantage.sapphire.SDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class GetSDIString
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String value = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String requestid = ajaxResponse.getRequestParameter("requestid");
        String sdcid = "";
        String columnid = "";
        switch (requestid) {
            case "0001": {
                sdcid = "Study";
                columnid = "defaultglpflag";
                break;
            }
            case "0002": {
                sdcid = "StorageUnitSDC";
                columnid = "labelpath";
                break;
            }
            case "0003": {
                sdcid = "Address";
                columnid = "externalflag";
                break;
            }
            case "0004": {
                sdcid = "Study";
                columnid = "clinicalflag";
                break;
            }
            case "0005": {
                sdcid = "LV_ClinicalProtocol";
                columnid = "versionstatus";
                break;
            }
        }
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3");
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        if (sdi.isValid()) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList(sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3());
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            DataSet primary = sdiData.getDataset("primary");
            if (primary != null && primary.size() > 0) {
                if (primary.isValidColumn(columnid)) {
                    if (0 == primary.getColumnType(columnid)) {
                        value = primary.getString(0, columnid, "");
                    } else {
                        message = this.getTranslationProcessor().translate("Column is not of type String") + " [" + columnid + "]";
                    }
                } else {
                    message = this.getTranslationProcessor().translate("Column is not valid") + " [" + columnid + "]";
                }
            } else {
                message = this.getTranslationProcessor().translate("No SDI found");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("value", value);
        ajaxResponse.addCallbackArgument("parameters", new JSONObject(ajaxResponse.getRequestParameters()));
        ajaxResponse.print();
    }
}

