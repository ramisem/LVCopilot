/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SampleListAjaxGetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "downloadMappingPage.doAdd_AjaxCallback");
        String lookupRet = ajaxResponse.getRequestParameter("lookupInfo");
        Trace.logDebug("Lookup returns:" + lookupRet);
        String inClause = "";
        String[] rows = StringUtil.split(lookupRet, "%3B");
        if (rows.length == 0) {
            ajaxResponse.setError("No sdiworkitems identified.");
        }
        DataSet ret = new DataSet();
        for (int i = 0; i < rows.length; ++i) {
            int currrow = ret.addRow();
            String[] tokens = StringUtil.split(rows[i], "|");
            String sampleid = "";
            String sdiworkitemid = "";
            if (tokens[0].equals("Sample")) {
                sampleid = tokens[1];
                sdiworkitemid = tokens[2];
            } else {
                sampleid = tokens[0];
                sdiworkitemid = tokens[1];
            }
            ret.setString(currrow, "keyid1", sampleid);
            ret.setString(currrow, "sdiworkitemid", sdiworkitemid);
            inClause = inClause.length() == 0 ? "" + sdiworkitemid + "" : inClause + DELIMITER + sdiworkitemid + "";
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "Select wi.sdiworkitemid,ds.sdidataid sdidataids, wi.keyid1, wi.workitemid, wi.workiteminstance \nFrom sdiworkitem wi, sdiworkitemitem wii, sdidata ds\nWhere wi.sdcid = wii.sdcid and wi.keyid1= wii.keyid1 and wi.keyid2= wii.keyid2 and wi.keyid3=wii.keyid3 and  wi.workitemid = wii.workitemid and wi.workiteminstance = wii.workiteminstance and\nwii.sdcid = ds.sdcid and wii.keyid1= ds.keyid1 and wii.keyid2= ds.keyid2 and wii.keyid3=ds.keyid3 and\nwii.itemsdcid = 'ParamList' and wii.itemkeyid1= ds.paramlistid and wii.itemkeyid2 = ds.paramlistversionid and wii.itemkeyid3 = ds.variantid and wii.iteminstance = ds.dataset\nand wi.sdiworkitemid in (" + safeSQL.addIn(inClause, DELIMITER) + ") order by wi.sdiworkitemid ";
        DataSet sdiWIDetails = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < ret.getRowCount(); ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("keyid1", ret.getString(i, "keyid1"));
            filter.put("sdiworkitemid", ret.getString(i, "sdiworkitemid"));
            DataSet match = sdiWIDetails.getFilteredDataSet(filter);
            if (match == null || match.getRowCount() <= 0) {
                ajaxResponse.setError("Error building response");
                break;
            }
            ret.setString(i, "workitemid", match.getString(0, "workitemid"));
            String key = "SDCID=Sample;KEYID1=" + ret.getString(i, "keyid1") + ";WORKITEMID=" + match.getString(0, "workitemid") + ";WORKITEMINSTANCE=" + match.getValue(0, "workiteminstance");
            ret.setString(i, "datasetkeyes", key);
        }
        if (sdiWIDetails.getRowCount() == 0) {
            ajaxResponse.setError("Cannot find any matching workitems");
        } else {
            ajaxResponse.addCallbackArgument("oSDIWorkItemInfo", ret.toXML());
        }
        ajaxResponse.print();
    }
}

