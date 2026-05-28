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

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.ExtraColumnUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.SpecLimitHandler;
import java.util.ArrayList;
import java.util.HashMap;
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
import sapphire.xml.PropertyList;

public class ReloadResults
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcid = ar.getRequestParameter("sdcid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        String specId = ar.getRequestParameter("specid", "");
        String specVersionId = ar.getRequestParameter("specversionid", "");
        String pageId = ar.getRequestParameter("pageid", "");
        String elementid = ar.getRequestParameter("datasetelementid", "");
        HttpSession session = req.getSession();
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        String sql1 = "SELECT * FROM sdidata WHERE sdcid=? AND keyid1=? ";
        String sql2 = "SELECT sdidataitem.*, pli.displaywidth FROM sdidataitem JOIN paramlistitem pli ON   pli.paramlistid=sdidataitem.paramlistid   AND pli.paramlistversionid=sdidataitem.paramlistversionid   AND pli.variantid=sdidataitem.variantid   AND pli.paramid=sdidataitem.paramid   AND pli.paramtype=sdidataitem.paramtype WHERE sdidataitem.sdcid=? AND sdidataitem.keyid1=? ";
        String sql3 = "SELECT * FROM sdidataapproval WHERE sdcid=? AND keyid1=? ";
        ArrayList<String> queryArgs = new ArrayList<String>();
        queryArgs.add(sdcid);
        queryArgs.add(keyid1);
        if (!keyid2.equals("")) {
            sql1 = sql1 + "AND keyid2=? ";
            sql2 = sql2 + "AND keyid2=? ";
            sql3 = sql3 + "AND keyid2=? ";
            queryArgs.add(keyid2);
        }
        if (!keyid3.equals("")) {
            sql1 = sql1 + "AND keyid3=? ";
            sql2 = sql2 + "AND keyid3=? ";
            sql3 = sql3 + "AND keyid3=? ";
            queryArgs.add(keyid3);
        }
        Object[] sQueryArgs = queryArgs.toArray(new String[queryArgs.size()]);
        DataSet sdidata = this.getQueryProcessor().getPreparedSqlDataSet(sql1, sQueryArgs);
        DataSet sdidataitem = this.getQueryProcessor().getPreparedSqlDataSet(sql2, sQueryArgs);
        DataSet sdidataapproval = this.getQueryProcessor().getPreparedSqlDataSet(sql3, sQueryArgs);
        if (pageProps != null) {
            ExtraColumnUtil extraColumnUtil = new ExtraColumnUtil(this.getQueryProcessor(), this.logger);
            Map<String, List<String>> extraColumns = extraColumnUtil.parseExtraColumns(pageProps, elementid);
            for (Map.Entry<String, List<String>> entry : extraColumns.entrySet()) {
                HashMap<String, String> searchValues;
                String[] keyColumns;
                String datasource = entry.getKey();
                List<String> extracolumnList = entry.getValue();
                if (datasource.equals("sdidata")) {
                    keyColumns = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"};
                    searchValues = new HashMap<String, String>();
                    searchValues.put("sdcid", sdcid);
                    searchValues.put("keyid1", keyid1);
                    searchValues.put("keyid2", keyid2);
                    searchValues.put("keyid3", keyid3);
                    extraColumnUtil.fillExtraColumnData(extracolumnList, searchValues, datasource, sdidata, keyColumns);
                    continue;
                }
                if (!datasource.equals("sdidataitem")) continue;
                keyColumns = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                searchValues = new HashMap();
                searchValues.put("sdcid", sdcid);
                searchValues.put("keyid1", keyid1);
                searchValues.put("keyid2", keyid2);
                searchValues.put("keyid3", keyid3);
                extraColumnUtil.fillExtraColumnData(extracolumnList, searchValues, datasource, sdidataitem, keyColumns);
            }
        }
        new SpecLimitHandler(this.getQueryProcessor()).injectSpecLimits(sdidataitem, specId, specVersionId);
        I18nUtil.localizeDisplayValues(sdidataitem, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        ar.addCallbackArgument("sdidata", sdidata);
        ar.addCallbackArgument("sdidataitem", sdidataitem);
        ar.addCallbackArgument("sdidataapproval", sdidataapproval);
        ar.addCallbackArgument("elementid", ar.getRequestParameter("elementid", ""));
        ar.print();
    }
}

