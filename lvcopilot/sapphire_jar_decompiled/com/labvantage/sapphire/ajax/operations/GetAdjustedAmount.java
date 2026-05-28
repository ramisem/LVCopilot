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
import java.math.RoundingMode;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;

public class GetAdjustedAmount
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        FormatUtil formatUtil = FormatUtil.getInstance(connectionInfo);
        String tablename = ajaxResponse.getRequestParameter("tablename", "");
        String primarycol1name = ajaxResponse.getRequestParameter("primarycol1name", "");
        String primarycol2name = ajaxResponse.getRequestParameter("primarycol2name", "");
        String primarycol1value = ajaxResponse.getRequestParameter("primarycol1value", "");
        String primarycol2value = ajaxResponse.getRequestParameter("primarycol2value", "");
        String reagentlotid = ajaxResponse.getRequestParameter("reagentlotid", "");
        String amountrecommended = ajaxResponse.getRequestParameter("amountrecommended", "");
        String lowertolerance = ajaxResponse.getRequestParameter("lowertolerance", "");
        String uppertolerance = ajaxResponse.getRequestParameter("uppertolerance", "");
        String amountscope = ajaxResponse.getRequestParameter("amountscope", "");
        String extra_props = ajaxResponse.getRequestParameter("extra_props", "");
        String adjustedAmount = amountrecommended;
        String adjustedAmountorig = amountrecommended;
        String usedamount = "";
        char decimalSeparator = formatUtil.getDecimalSeparator();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select rl.actualconcentration,rl.targetconcentration,rt.targetconcentration typetargetconcentration");
        if (tablename.equalsIgnoreCase("sdidatarelation") || tablename.equalsIgnoreCase("sdiworkitemrelation")) {
            sql.append(",t.amount,t.requiredamount amountrecommended,t.requiredamount amountrecommendedtext,t.requiredamountunits amountrecommendedunits,t.requiredamountunitstype amountrecommendedunitstype");
        } else if (tablename.equalsIgnoreCase("transferexecutionreagent")) {
            sql.append(",t.useamount amount,t.recommendedamount amountrecommended,t.recommendedamount amountrecommendedtext,t.recommendedamountunits amountrecommendedunits,t.recommendedamountunitstype amountrecommendedunitstype");
        } else {
            sql.append(",t.amount,t.amountrecommended,t.amountrecommendedtext,t.amountrecommendedunits,t.amountrecommendedunitstype");
        }
        sql.append(" from reagentlot rl," + tablename + " t,reagenttype rt");
        sql.append(" where rl.reagentlotid=").append(safeSQL.addVar(reagentlotid));
        sql.append(" and t." + primarycol1name + "=").append(safeSQL.addVar(primarycol1value));
        if (primarycol2name.length() > 0) {
            sql.append(" and t." + primarycol2name + "=").append(safeSQL.addVar(primarycol2value));
        }
        sql.append(" and rt.reagenttypeid=t.originalreagenttypeid");
        sql.append(" and rt.reagenttypeversionid=coalesce( NULLIF( t.originalreagenttypeversionid, '' ),(select rt1.reagenttypeversionid from reagenttype rt1 where  rt1.reagenttypeid=rt.reagenttypeid and rt1.versionstatus='C'),(select max(rt2.reagenttypeversionid) from reagenttype rt2 where rt2.reagenttypeid=rt.reagenttypeid and rt2.versionstatus='P'))");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String actualconcentration = ds.getValue(0, "actualconcentration", "");
            String targetconcentration = ds.getValue(0, "targetconcentration", "");
            String typetargetconcentration = ds.getValue(0, "typetargetconcentration", "");
            String amountrecommendedtext = ds.getValue(0, "amountrecommendedtext", "");
            amountrecommended = ds.getValue(0, "amountrecommended", "");
            extra_props = extra_props + "||" + ds.getValue(0, "amountrecommendedunits", "") + "||" + ds.getValue(0, "amountrecommendedunitstype", "");
            if ((actualconcentration.length() > 0 || targetconcentration.length() > 0) && typetargetconcentration.length() > 0) {
                int scale;
                if (actualconcentration.length() == 0) {
                    actualconcentration = targetconcentration;
                }
                if ((scale = ReagentUtil.getMaxScale(amountrecommendedtext + ";" + lowertolerance + ";" + uppertolerance, decimalSeparator)) == 0) {
                    scale = 3;
                }
                amountrecommended = UnitsUtil.convertToLocateSeperated(amountrecommended, "" + decimalSeparator);
                actualconcentration = UnitsUtil.convertToLocateSeperated(actualconcentration, "" + decimalSeparator);
                typetargetconcentration = UnitsUtil.convertToLocateSeperated(typetargetconcentration, "" + decimalSeparator);
                double adjAmt = formatUtil.parseBigDecimal(amountrecommended).doubleValue() * formatUtil.parseBigDecimal(typetargetconcentration).doubleValue() / formatUtil.parseBigDecimal(actualconcentration).doubleValue();
                BigDecimal bd1 = BigDecimal.valueOf(adjAmt);
                bd1 = bd1.setScale(scale, RoundingMode.HALF_UP);
                adjustedAmountorig = ReagentUtil.removeLastZerosAferDecimal(bd1.toString(), decimalSeparator);
                BigDecimal bd = BigDecimal.valueOf(adjAmt *= (double)this.getMultiplicationForInventory(primarycol1value, amountscope));
                bd = bd.setScale(scale, RoundingMode.HALF_UP);
                adjustedAmount = ReagentUtil.removeLastZerosAferDecimal(bd.toString(), decimalSeparator);
            }
            usedamount = ds.getValue(0, "amount", "");
        }
        adjustedAmount = UnitsUtil.convertToLocateSeperated(adjustedAmount, "" + decimalSeparator);
        adjustedAmountorig = UnitsUtil.convertToLocateSeperated(adjustedAmountorig, "" + decimalSeparator);
        ajaxResponse.addCallbackArgument("amountadjusted", adjustedAmount);
        ajaxResponse.addCallbackArgument("adjustedAmountorig", adjustedAmountorig);
        ajaxResponse.addCallbackArgument("usedamount", usedamount);
        ajaxResponse.addCallbackArgument("extra_props", extra_props);
        ajaxResponse.print();
    }

    private int getMultiplicationForInventory(String keyid1, String amountscope) {
        DataSet ds;
        int count = 1;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        if (amountscope.equalsIgnoreCase("qbr")) {
            sql.append("select s_qcbatchitemid FROM s_qcbatchitem bi,s_qcbatchreagent br");
            sql.append(" WHERE s_qcbatchreagentid = ").append(safeSQL.addVar(keyid1));
            sql.append(" and bi.s_qcbatchid = br.qcbatchid");
        } else if (amountscope.equalsIgnoreCase("S")) {
            sql.append("select s_qcbatchitemid FROM s_qcbatchitem");
            sql.append(" WHERE qcbatchsampletypeid = ").append(safeSQL.addVar(keyid1));
        }
        if (sql.length() > 0 && (ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.getRowCount() > 0) {
            count = ds.getRowCount();
        }
        return count;
    }
}

