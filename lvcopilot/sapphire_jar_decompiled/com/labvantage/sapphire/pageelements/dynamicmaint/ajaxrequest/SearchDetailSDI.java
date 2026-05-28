/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.ExtraColumnUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class SearchDetailSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String detailSDC = ar.getRequestParameter("detailsdcid", "");
        String detailKeyid = ar.getRequestParameter("detailkeyid", "");
        String datasetName = ar.getRequestParameter("datasetName", "");
        String pageId = ar.getRequestParameter("pageid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        String[] arrDetailKeyid = detailKeyid.split("\\|");
        String detailKeyid1 = arrDetailKeyid[0];
        String detailKeyid2 = arrDetailKeyid.length > 1 ? arrDetailKeyid[1] : "";
        String detailKeyid3 = arrDetailKeyid.length > 2 ? arrDetailKeyid[2] : "";
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(detailSDC);
        sdiRequest.setKeyid1List(detailKeyid1);
        if (!detailKeyid2.equals("")) {
            sdiRequest.setKeyid2List(detailKeyid2);
        }
        if (!detailKeyid3.equals("")) {
            sdiRequest.setKeyid3List(detailKeyid3);
        }
        sdiRequest.setRetainRsetid(false);
        sdiRequest.setRequestItem("primary");
        SDIData sdi = this.getSDIProcessor().getSDIData(sdiRequest);
        DataSet ds = sdi.getDataset("primary");
        DataSet headerDs = ds.copy();
        Utils.fillKeyids(ds, keyid1, keyid2, keyid3);
        HttpSession session = req.getSession();
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        ExtraColumnUtil extraColumnUtil = new ExtraColumnUtil(this.getQueryProcessor(), this.logger);
        Map<String, List<String>> allExtraColumns = extraColumnUtil.parseExtraColumns(pageProps);
        List<String> extraColumns = allExtraColumns.get(datasetName);
        if (extraColumns != null) {
            extraColumnUtil.fillExtraColumnData(extraColumns, ds, datasetName);
        }
        ar.addCallbackArgument("data", ds);
        ar.addCallbackArgument("headerData", headerDs);
        ar.print();
    }
}

