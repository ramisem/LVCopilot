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

import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetContainersAdjustedQuantity
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        FormatUtil formatUtil = FormatUtil.getInstance(connectionInfo);
        char decimalSeperator = formatUtil.getDecimalSeparator();
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String prevAmount = ajaxResponse.getRequestParameter("preamount");
        String prevAmount_orinal = ajaxResponse.getRequestParameter("preamount");
        String prevAmountUnit = ajaxResponse.getRequestParameter("preamountunit");
        String prevAmountunitType = ajaxResponse.getRequestParameter("preamountunittype");
        String trackitemindexid = ajaxResponse.getRequestParameter("trackitemindexid");
        String finalValue = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT trackitemid,linkkeyid1, trackitemstatus, qtyunits, qtycurrent,qtycurrenttype");
        sql.append(",trackitem.containertypeid, sizevalue, sizeunits ");
        sql.append(",(select reagentlot.managecontainerinventoryflag from reagentlot where reagentlot.reagentlotid=trackitem.linkkeyid1) managecontainerinventoryflag ");
        sql.append(" FROM trackitem ");
        sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
        sql.append(" WHERE trackitem.trackitemid = " + safeSQL.addVar(trackitemid));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String managecontainerinventoryflag = ds.getString(0, "managecontainerinventoryflag", "Y");
            if (managecontainerinventoryflag.equalsIgnoreCase("Y")) {
                String trackitemstatus = ds.getString(0, "trackitemstatus", "");
                String reagentlot = ds.getString(0, "linkkeyid1");
                BigDecimal availQuantity = ds.getBigDecimal(0, "qtycurrent");
                String availQuantityUnit = ds.getString(0, "qtyunits", "");
                String availQuantityUnitType = ds.getString(0, "qtycurrenttype", "U");
                String containerSize = ds.getValue(0, "sizevalue", "");
                String containerUnits = ds.getString(0, "sizeunits", "");
                try {
                    if (!prevAmountUnit.equalsIgnoreCase(availQuantityUnit)) {
                        double value;
                        if (prevAmountunitType.equalsIgnoreCase("C")) {
                            value = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, prevAmount, availQuantityUnit);
                            prevAmount = Double.toString(value);
                        } else if (availQuantityUnitType.equalsIgnoreCase("C")) {
                            value = UnitsUtil.covertFromUnitsToContainer(this.getQueryProcessor(), containerSize, containerUnits, prevAmount, prevAmountUnit);
                            prevAmount = Double.toString(value);
                        } else {
                            prevAmount = UnitsUtil.getConvertedValue(qp, prevAmountUnit, availQuantityUnit, prevAmount);
                        }
                    }
                    prevAmount = UnitsUtil.convertToLocateSeperated(prevAmount, "" + decimalSeperator);
                    finalValue = availQuantity.doubleValue() + formatUtil.parseBigDecimal(prevAmount).doubleValue() + "";
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
                finalValue = UnitsUtil.convertToLocateSeperated(finalValue, "" + decimalSeperator);
                int maxScale = ReagentUtil.getMaxScale(availQuantity + ";" + prevAmount, decimalSeperator);
                finalValue = formatUtil.parseBigDecimal(finalValue).setScale(maxScale, 4) + "";
                finalValue = ReagentUtil.removeLastZerosAferDecimal(finalValue, decimalSeperator);
                finalValue = UnitsUtil.convertToLocateSeperated(finalValue, "" + decimalSeperator);
                ajaxResponse.addCallbackArgument("managecontainerinventoryflag", managecontainerinventoryflag);
                ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
                ajaxResponse.addCallbackArgument("reagentlot", reagentlot);
                ajaxResponse.addCallbackArgument("trackitemstatus", trackitemstatus);
                ajaxResponse.addCallbackArgument("currentquantity", StringUtil.replaceAll(availQuantity.toString(), ".", decimalSeperator + ""));
                ajaxResponse.addCallbackArgument("prevquantity", StringUtil.replaceAll(prevAmount_orinal, ".", decimalSeperator + "") + " " + (prevAmountunitType.equalsIgnoreCase("C") ? "C" : prevAmountUnit));
                ajaxResponse.addCallbackArgument("finalquantity", finalValue);
                ajaxResponse.addCallbackArgument("currentquantityunit", availQuantityUnit);
                ajaxResponse.addCallbackArgument("currentquantityunittype", availQuantityUnitType);
                ajaxResponse.addCallbackArgument("trackitemindexid", trackitemindexid);
            } else {
                ajaxResponse.addCallbackArgument("managecontainerinventoryflag", "N");
            }
            ajaxResponse.print();
        }
    }
}

