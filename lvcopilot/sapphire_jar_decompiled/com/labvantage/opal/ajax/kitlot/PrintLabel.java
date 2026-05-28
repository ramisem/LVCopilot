/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.kitlot;

import com.labvantage.sapphire.actions.label.GenerateLabel;
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

public class PrintLabel
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53386 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String kitlotid = ajaxResponse.getRequestParameter("kitlotid", "");
        String printerid = ajaxResponse.getRequestParameter("printerid", "");
        if (StringUtil.getLen(kitlotid) > 0L) {
            String labelmethodid = "";
            String labelmethodversionid = "";
            String trackitemid = "";
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select rt.labelmethodid, rt.labelmethodversionid, rt.reagenttypeid");
            sql.append(" from reagentlot rl, reagenttype rt ");
            sql.append(" where reagentlotid = ").append(safeSQL.addVar(kitlotid));
            sql.append(" and rt.reagenttypeid = rl.reagenttypeid");
            sql.append(" and rt.reagenttypeversionid = rl.reagenttypeversionid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                labelmethodid = ds.getValue(0, "labelmethodid");
                labelmethodversionid = ds.getValue(0, "labelmethodversionid", "");
            }
            if (StringUtil.getLen(labelmethodid) == 0L) {
                ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("No label method found for printing labels. Please make sure Kit Type has label method defined."));
            } else {
                safeSQL.reset();
                String sqlTrackitem = "select trackitemid from trackitem where linksdcid = 'LV_ReagentLot' and linkkeyid1 = " + safeSQL.addVar(kitlotid);
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlTrackitem, safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    trackitemid = ds.getColumnValues("trackitemid", ";");
                }
                if (StringUtil.getLen(trackitemid) == 0L) {
                    ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("No Kits found in Kit Lot to print labels"));
                } else {
                    try {
                        PropertyList props = new PropertyList();
                        props.setProperty("keyid1", trackitemid);
                        props.setProperty("labelmethodid", labelmethodid);
                        props.setProperty("labelmethodversionid", labelmethodversionid);
                        props.setProperty("labelsdcid", "TrackItemSDC");
                        props.setProperty("printeraddressid", printerid);
                        props.setProperty("printeraddresstype", "Device");
                        this.getActionProcessor().processActionClass(GenerateLabel.class.getName(), props);
                        ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("Labels have been printed successfully"));
                    }
                    catch (ActionException e) {
                        this.logger.error("Problem printing label", e);
                        ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("Error while printing labels.") + "<br>" + e.getMessage());
                    }
                }
            }
        } else {
            ajaxResponse.addCallbackArgument("message", "Unable to print labels. No Kit Lot ID found.");
        }
        ajaxResponse.print();
    }
}

