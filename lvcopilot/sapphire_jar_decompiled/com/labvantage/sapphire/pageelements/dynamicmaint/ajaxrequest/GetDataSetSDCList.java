/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetDataSetSDCList
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String paramlistid = ar.getRequestParameter("paramlistid", "");
        String paramlistversionid = ar.getRequestParameter("paramlistversionid", "");
        String variantid = ar.getRequestParameter("variantid", "");
        String paramid = ar.getRequestParameter("paramid", "");
        String paramtype = ar.getRequestParameter("paramtype", "");
        String sql = "select entrysdcid, extendedsql from paramlistitem where paramlistid=? and paramlistversionid=? and variantid=? and paramid=? and paramtype=?";
        Object[] params = new String[]{paramlistid, paramlistversionid, variantid, paramid, paramtype};
        DataSet sdcDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        String sdcid = sdcDs.getString(0, "entrysdcid", "");
        String restrictiveWhere = sdcDs.getString(0, "extendedsql", "");
        String keyColId1 = "";
        if (sdcid.equals("")) {
            ds = new DataSet();
        } else {
            keyColId1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            SDIRequest r = new SDIRequest();
            r.setSDCid(sdcid);
            r.setQueryFrom(this.getSDCProcessor().getProperty(sdcid, "tableid"));
            r.setQueryWhere(restrictiveWhere);
            r.setRequestItem("primary");
            r.setRetainRsetid(false);
            SDIData sdiData = this.getSDIProcessor().getSDIData(r);
            ds = sdiData.getDataset("primary");
        }
        PropertyListCollection coll = new PropertyListCollection();
        PropertyList plEmpty = new PropertyList();
        plEmpty.setProperty("key", "");
        plEmpty.setProperty("value", "");
        coll.add(0, plEmpty);
        try {
            if (ds != null && ds.getRowCount() > 0) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    PropertyList pl = new PropertyList();
                    pl.setProperty("key", ds.getValue(i, keyColId1));
                    pl.setProperty("value", ds.getValue(i, keyColId1));
                    coll.add(i + 1, pl);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ar.addCallbackArgument("optionlist", coll.toJSONArray());
        ar.addCallbackArgument("reftypeid", paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + paramid + ";" + paramtype);
        ar.print();
    }
}

