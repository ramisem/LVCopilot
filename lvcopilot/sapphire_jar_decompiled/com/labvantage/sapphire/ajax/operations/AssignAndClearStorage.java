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

import com.labvantage.sapphire.actions.storage.AssignStrgAndReceive;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AssignAndClearStorage
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54556 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String clearStorageFlag = ajaxResponse.getRequestParameter("clearstorage", "N");
        String storageUnitID = ajaxResponse.getRequestParameter("storageunitid", "");
        String errormsg = "";
        boolean fireAction = true;
        if (StringUtil.getLen(trackitemid) > 0L) {
            trackitemid = StringUtil.replaceAll(trackitemid, "%3B", ";");
            try {
                if (clearStorageFlag.equalsIgnoreCase("Y")) {
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuilder sql = new StringBuilder();
                    sql.append("SELECT count(1) as count FROM trackitem ");
                    sql.append(" WHERE trackitemid IN (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                    sql.append(" AND currentstorageunitid is not null");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0 && ds.getInt(0, "count", 0) == 0) {
                        errormsg = this.getTranslationProcessor().translate("None of the selected item(s) are in storage.");
                        fireAction = false;
                    }
                }
                if (fireAction) {
                    PropertyList props = new PropertyList();
                    props.setProperty("trackitemid", trackitemid);
                    props.setProperty("clearstorage", clearStorageFlag);
                    props.setProperty("istrackitem", "Y");
                    props.setProperty("storageunitid", storageUnitID);
                    this.getActionProcessor().processActionClass(AssignStrgAndReceive.class.getName(), props);
                }
            }
            catch (ActionException e) {
                errormsg = e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("msg", errormsg);
        ajaxResponse.print();
    }
}

