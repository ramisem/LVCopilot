/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GetBoxStorageUnitType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String boxid = ajaxResponse.getRequestParameter("boxid", "").trim();
        String storageunittype = "";
        String boxdesc = "";
        String sstudyid = "";
        if (boxid.length() > 0 && OpalUtil.isNotEmpty(ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_box.boxdesc, s_box.sstudyid, storageunit.storageunittype from storageunit, s_box where storageunit.linksdcid = 'LV_Box' and storageunit.linkkeyid1 = s_box.s_boxid and s_box.s_boxid = ?", (Object[])new String[]{boxid}))) {
            storageunittype = ds.getString(0, "storageunittype", "");
            boxdesc = ds.getString(0, "boxdesc", "");
            sstudyid = ds.getString(0, "sstudyid", "");
        }
        PropertyList list = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
        ajaxResponse.addCallbackArgument("storageunittype", storageunittype);
        ajaxResponse.addCallbackArgument("boxtype", "No Layout".equals(list.getProperty("__propertytreeid")) ? "Unsorted" : "Sorted");
        ajaxResponse.addCallbackArgument("boxdesc", boxdesc);
        ajaxResponse.addCallbackArgument("sstudyid", sstudyid);
        ajaxResponse.print();
    }
}

