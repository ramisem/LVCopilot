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

import com.labvantage.opal.util.OpalUtil;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetCMTParentKeyID
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String parentsdcid = "";
        String parentkeyid1 = "";
        String parentkeyid2 = "";
        String parentkeyid3 = "";
        if (sdcid.length() > 0 && keyid1.length() > 0) {
            if (keyid1.contains(";")) {
                keyid1 = keyid1.substring(0, keyid1.indexOf(";"));
            }
            if (keyid2.contains(";")) {
                keyid2 = keyid2.substring(0, keyid2.indexOf(";"));
            }
            switch (sdcid) {
                case "LV_RestClass": {
                    parentsdcid = "Study";
                    parentkeyid1 = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_restrictclass", "sstudyid", "s_restrictclassid = ?", new String[]{keyid1});
                    break;
                }
                case "LV_EventDef": {
                    parentsdcid = "Study";
                    parentkeyid1 = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "clinicalprotocolid", "s_eventdefid = ?", new String[]{keyid1});
                    break;
                }
                case "Request": {
                    parentsdcid = "Request";
                    parentkeyid1 = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_requestitem", "requestid", "s_requestitemid = ?", new String[]{keyid1});
                    break;
                }
                case "Product": {
                    parentsdcid = "Product";
                    Map<String, String> map = OpalUtil.getMultiColumnValue(this.getQueryProcessor(), "s_prodvariant", "productid,productversionid", "s_prodvariantid = ?", new String[]{keyid1});
                    if (map == null) break;
                    parentkeyid1 = map.getOrDefault("productid", "");
                    parentkeyid2 = map.getOrDefault("productversionid", "");
                    break;
                }
                case "QCMethod": {
                    parentsdcid = "QCMethod";
                    Map<String, String> map = OpalUtil.getMultiColumnValue(this.getQueryProcessor(), "s_qcmethodsampletype", "qcmethodid,qcmethodversionid", "s_qcmethodsampletypeid = ?", new String[]{keyid1});
                    if (map == null) break;
                    parentkeyid1 = map.getOrDefault("qcmethodid", "");
                    parentkeyid2 = map.getOrDefault("qcmethodversionid", "");
                    break;
                }
                case "LV_ChildSamplePlan": {
                    parentsdcid = "WorkItem";
                    Map<String, String> map = OpalUtil.getMultiColumnValue(this.getQueryProcessor(), "workitem", "workitemid,workitemversionid", "embedchildsampleplanid=? and embedchildsampleplanversionid=?", new String[]{keyid1, keyid2});
                    if (map == null) break;
                    parentkeyid1 = map.getOrDefault("workitemid", "");
                    parentkeyid2 = map.getOrDefault("workitemversionid", "");
                }
            }
        }
        ajaxResponse.addCallbackArgument("parentsdcid", parentsdcid);
        ajaxResponse.addCallbackArgument("parentkeyid1", parentkeyid1);
        ajaxResponse.addCallbackArgument("parentkeyid2", parentkeyid2);
        ajaxResponse.addCallbackArgument("parentkeyid3", parentkeyid3);
        ajaxResponse.addCallbackArgument("callbackfunction", ajaxResponse.getRequestParameter("callbackfunction"));
        ajaxResponse.addCallbackArgument("cmttoolbaruuid", ajaxResponse.getRequestParameter("cmttoolbaruuid"));
        ajaxResponse.print();
    }
}

