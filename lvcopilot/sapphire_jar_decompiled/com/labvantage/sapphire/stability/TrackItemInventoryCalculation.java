/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class TrackItemInventoryCalculation
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53874 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String strResponse = "";
        String strError = "";
        String ajaxCommand = request.getParameter("ajaxCommand");
        if (ajaxCommand != null && ajaxCommand.equalsIgnoreCase("DoTrackItemInvCalculation")) {
            String trackItemId = request.getParameter("trackitemid");
            String workOrderId = request.getParameter("workorderid");
            String currentQty = request.getParameter("currentqty");
            String currentQtyUnit = request.getParameter("currentqtyunit");
            String wopQty = request.getParameter("wopqty");
            String wopQtyUnit = request.getParameter("wopqtyunit");
            FormatUtil fmt = FormatUtil.getInstance(I18nUtil.getSessionLocale(request));
            BigDecimal bdCurrentQty = fmt.parseBigDecimal(currentQty);
            BigDecimal bdWopQty = fmt.parseBigDecimal(wopQty);
            try {
                strResponse = TrackItemInventoryCalculation.doTrackItemInvCalculation(this.getQueryProcessor(), trackItemId, bdCurrentQty.doubleValue(), currentQtyUnit, workOrderId, bdWopQty.doubleValue(), wopQtyUnit);
                strResponse = strResponse + ";" + workOrderId + "|" + trackItemId;
            }
            catch (Exception e) {
                strError = "Error :" + e.getMessage();
                if (Trace.on) {
                    Trace.logDebug(strError);
                }
            }
        } else {
            strError = "Error : Unrecognized ajax command!";
            if (Trace.on) {
                Trace.logDebug(strError);
            }
        }
        try {
            PrintWriter out = response.getWriter();
            if (strError.length() > 0) {
                if (Trace.on) {
                    Trace.logDebug(strError);
                }
                out.write(strError);
            } else {
                out.write(strResponse);
            }
        }
        catch (IOException e) {
            Logger.logStackTrace(e);
        }
    }

    public static String[] getTrackItemContainerQtyAndUnit(String trackItemId, int noOfContainers, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" SELECT ct.sizevalue, ct.sizeunits  FROM containertype ct, trackitem tr WHERE ct.containertypeid = tr.containertypeid AND tr.trackitemid = " + safeSQL.addVar(trackItemId));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String[] returnValues = new String[2];
            double sizeValue = ds.getDouble(0, "sizevalue");
            double rawQty = (double)noOfContainers * sizeValue;
            returnValues[0] = Double.toString(rawQty);
            returnValues[1] = ds.getValue(0, "sizeunits");
            return returnValues;
        }
        return null;
    }

    public static String[] getWOPContainerQtyAndUnit(String workOrderId, int noOfContainers, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" SELECT ct.sizevalue, ct.sizeunits  FROM containertype ct, workorder wo, study st  WHERE ct.containertypeid = st.containertypeid AND st.studyid = wo.studyid  AND wo.workorderid = " + safeSQL.addVar(workOrderId));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String[] returnValues = new String[2];
            double sizeValue = ds.getDouble(0, "sizevalue");
            double rawQty = (double)noOfContainers * sizeValue;
            returnValues[0] = Double.toString(rawQty);
            returnValues[1] = ds.getValue(0, "sizeunits");
            return returnValues;
        }
        return null;
    }

    public static String doTrackItemInvCalculation(QueryProcessor qp, String trackItemId, double currentQty, String currentQtyUnit, String workOrderId, double wopQty, String wopQtyUnit) throws SapphireException {
        String[] convertedValues = null;
        String returnValue = "";
        if (!wopQtyUnit.equals(currentQtyUnit)) {
            int noOfContainers;
            if ("(Containers)".equals(currentQtyUnit) || "C".equals(currentQtyUnit)) {
                noOfContainers = new Double(currentQty).intValue();
                convertedValues = TrackItemInventoryCalculation.getTrackItemContainerQtyAndUnit(trackItemId, noOfContainers, qp);
                if (convertedValues == null) {
                    throw new SapphireException("Container details not found for trackitem :" + trackItemId);
                }
                currentQty = new Double(convertedValues[0]);
                currentQtyUnit = convertedValues[1];
            }
            if ("(Containers)".equals(wopQtyUnit) || "C".equals(wopQtyUnit)) {
                noOfContainers = new Double(wopQty).intValue();
                convertedValues = TrackItemInventoryCalculation.getWOPContainerQtyAndUnit(workOrderId, noOfContainers, qp);
                if (convertedValues == null) {
                    throw new SapphireException("Container details not found for WorkOrder :" + workOrderId);
                }
                wopQty = new Double(convertedValues[0]);
                wopQtyUnit = convertedValues[1];
            }
            if (!currentQtyUnit.equals(wopQtyUnit)) {
                String strWopQty = "" + wopQty;
                strWopQty = ConvertUnits.convertUnits(qp, wopQtyUnit, currentQtyUnit, strWopQty);
                wopQty = new Double(strWopQty);
            }
        }
        String strCurrentQty = "" + (currentQty -= wopQty);
        returnValue = strCurrentQty + "|" + currentQtyUnit;
        return returnValue;
    }
}

