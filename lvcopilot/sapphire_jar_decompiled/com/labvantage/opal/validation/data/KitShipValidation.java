/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.data;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class KitShipValidation
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53250 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String kitlotid = ajaxResponse.getRequestParameter("kitlotid", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String message = "";
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select trackitemid, linkkeyid1, currentstorageunitid, trackitemstatus, trackitemlabel,");
        sql.append(" (select s.linksdcid from storageunit s where s.storageunitid = trackitem.currentstorageunitid) storagesdcid,");
        sql.append(" (select rl.reagentstatus from reagentlot rl where rl.reagentlotid = trackitem.linkkeyid1) kitlotstatus,");
        sql.append(" (select rl.sstudyid from reagentlot rl where rl.reagentlotid = trackitem.linkkeyid1) studyid");
        sql.append(" from trackitem");
        sql.append(" where linksdcid = 'LV_ReagentLot'");
        if (StringUtil.getLen(kitlotid) > 0L) {
            kitlotid = StringUtil.replaceAll(kitlotid, "%3B", ";");
            sql.append(" and linkkeyid1 in ( ").append(safeSQL.addIn(kitlotid, ";")).append(" )");
        } else if (StringUtil.getLen(trackitemid) > 0L) {
            trackitemid = StringUtil.replaceAll(trackitemid, "%3B", ";");
            sql.append(" and trackitemid in ( ").append(safeSQL.addIn(trackitemid, ";")).append(" )");
        }
        boolean inactive = false;
        boolean invalid = false;
        boolean shipped = false;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if (!"Active".equals(ds.getValue(i, "kitlotstatus"))) {
                    inactive = true;
                    break;
                }
                if ("LV_Package".equals(ds.getValue(i, "storagesdcid"))) {
                    shipped = true;
                    break;
                }
                if ("Valid".equals(ds.getValue(i, "trackitemstatus"))) continue;
                invalid = true;
                break;
            }
        }
        if (StringUtil.getLen(kitlotid) > 0L) {
            if (inactive) {
                message = "Kit Lot is not Active. Only Active Kit Lots can be shipped.";
            }
            if (invalid) {
                message = "Some of the Kits in selected Kit Lot are not Valid. Only Valid Kits can be shipped.";
            }
            if (shipped) {
                message = "Some of the Kits in selected Kit Lot have already been Shipped. Please select individual Kits to ship.";
            }
        } else if (StringUtil.getLen(trackitemid) > 0L) {
            if (inactive) {
                message = "Selected Kit's Kit Lot is not Active. Only Active Kit Lots can be shipped.";
            }
            if (invalid) {
                message = "One or more of the selected Kit is not Valid. Only Valid Kits can be shipped.";
            }
            if (shipped) {
                message = "One or more of the selected Kits have already been Shipped.";
            }
        } else {
            ajaxResponse.addCallbackArgument("message", message);
        }
        ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate(message));
        ajaxResponse.addCallbackArgument("trackitemid", ds.getColumnValues("trackitemid", ";"));
        ajaxResponse.addCallbackArgument("studyid", OpalUtil.getUniqueValues(ds.getColumnValues("studyid", ";"), ";"));
        ajaxResponse.addCallbackArgument("kitlotid", kitlotid);
        ajaxResponse.print();
    }
}

