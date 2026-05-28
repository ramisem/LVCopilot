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

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;

public class ValidateUseAmount
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89664 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        FormatUtil formatUtil = FormatUtil.getInstance(connectionInfo);
        char decimalSeperator = formatUtil.getDecimalSeparator();
        TranslationProcessor tp = this.getTranslationProcessor();
        String recAmt = ajaxResponse.getRequestParameter("recamount", "");
        boolean skiptolerancecheck = ajaxResponse.getRequestParameter("skiptolerancecheck", "N").equalsIgnoreCase("Y");
        String recAmtUnits = ajaxResponse.getRequestParameter("recamountunits");
        String recAmtUnitsType = ajaxResponse.getRequestParameter("recamountunitstype");
        String useAmt = ajaxResponse.getRequestParameter("useamount", "");
        String useAmtunit = ajaxResponse.getRequestParameter("useamountunit", "");
        String useAmtunittype = ajaxResponse.getRequestParameter("useamountunittype", "");
        String reagentTypeId = ajaxResponse.getRequestParameter("reagenttypeid");
        String reagentTypeVersionId = ajaxResponse.getRequestParameter("reagenttypeversionid");
        String includeReagentTypeId = ajaxResponse.getRequestParameter("includereagenttypeid");
        String currentTrackitemid = ajaxResponse.getRequestParameter("currenttrackitemid", "");
        String currentUseAmount = ajaxResponse.getRequestParameter("currentuseamount", "");
        String currentUseAmountUnit = ajaxResponse.getRequestParameter("currentuseamountunit", "");
        String currentUseAmountUnitType = ajaxResponse.getRequestParameter("currentuseamountunittype", "");
        String prevAmount = ajaxResponse.getRequestParameter("prevamount", "");
        String prevAmountunit = ajaxResponse.getRequestParameter("prevamountunit", "");
        String prevAmountunitType = ajaxResponse.getRequestParameter("prevamountunittype", "");
        String prevTrackitemid = ajaxResponse.getRequestParameter("prevtrackitemid", "");
        String tolerancemsg = ajaxResponse.getRequestParameter("tolerancemsg", "");
        String amountscope = ajaxResponse.getRequestParameter("amountscope", "");
        String keyid1 = ajaxResponse.getRequestParameter("primarykey", "");
        String colindx = ajaxResponse.getRequestParameter("colindx", "");
        String warningmsgHtml = tolerancemsg;
        String cancelButtonNotes = tp.translate("Use <b>Cancel</b> to revert the quantity back to the original value.");
        int multifactor = 1;
        String availQuantityUnit = "";
        String availQuantityUnitType = "";
        String containerSize = "";
        String containerUnits = "";
        try {
            String stageid;
            String includeReagentTypeVersionId;
            String sqlRT;
            DataSet ds;
            String okButtonNotes;
            String warningmsg;
            if (currentTrackitemid.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT trackitemid, trackitemstatus, qtyunits, qtycurrent,qtycurrenttype");
                sql.append(",trackitem.containertypeid, sizevalue, sizeunits ");
                sql.append(" FROM trackitem ");
                sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
                sql.append(" WHERE trackitem.trackitemid = " + safeSQL.addVar(currentTrackitemid));
                DataSet trackitemDS = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (trackitemDS != null && trackitemDS.size() > 0) {
                    String trackitemstatus = trackitemDS.getString(0, "trackitemstatus", "");
                    if (trackitemstatus.equalsIgnoreCase("depleted")) {
                        warningmsg = tp.translate("Warning, container already Depleted.  Used Amount &nbsp;adjustments will not automatically adjust the container's &nbsp;inventory.  However, you may adjust the container inventory &nbsp;manually.");
                        okButtonNotes = tp.translate("Use <b>OK</b> to accept the changes without adjusting the container's inventory.");
                        warningmsgHtml = "<table style='margin-top: 8px'>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + warningmsg + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + okButtonNotes + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + cancelButtonNotes + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "</table>";
                    } else {
                        double newValue;
                        String[] totalAmountArr;
                        String totalAmount;
                        BigDecimal availQuantity = trackitemDS.getBigDecimal(0, "qtycurrent", new BigDecimal(0));
                        availQuantityUnit = trackitemDS.getString(0, "qtyunits", "");
                        availQuantityUnitType = trackitemDS.getString(0, "qtycurrenttype", "");
                        containerSize = trackitemDS.getValue(0, "sizevalue", "");
                        containerUnits = trackitemDS.getString(0, "sizeunits", "");
                        if (currentUseAmount.contains(";")) {
                            totalAmount = ReagentUtil.getTotalValue(qp, formatUtil, currentUseAmount, currentUseAmountUnit, currentUseAmountUnitType, containerSize, containerUnits);
                            totalAmountArr = StringUtil.split(totalAmount, ";");
                            currentUseAmount = totalAmountArr[0];
                            currentUseAmountUnit = totalAmountArr[1];
                            currentUseAmountUnitType = totalAmountArr[2];
                        }
                        if (prevAmount.contains(";")) {
                            totalAmount = ReagentUtil.getTotalValue(qp, formatUtil, prevAmount, prevAmountunit, prevAmountunitType, containerSize, containerUnits);
                            totalAmountArr = StringUtil.split(totalAmount, ";");
                            prevAmount = totalAmountArr[0];
                            prevAmountunit = totalAmountArr[1];
                            prevAmountunitType = totalAmountArr[2];
                        }
                        String currentUseAmount_temp = currentUseAmount;
                        String prevAmount_temp = prevAmount;
                        if (!currentUseAmountUnit.equalsIgnoreCase(availQuantityUnit)) {
                            if (currentUseAmountUnitType.equalsIgnoreCase("C")) {
                                newValue = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, currentUseAmount_temp, availQuantityUnit);
                                currentUseAmount_temp = Double.toString(newValue);
                            } else if (availQuantityUnitType.equalsIgnoreCase("C")) {
                                newValue = UnitsUtil.covertFromUnitsToContainer(this.getQueryProcessor(), containerSize, containerUnits, currentUseAmount_temp, currentUseAmountUnit);
                                currentUseAmount_temp = Double.toString(newValue);
                            } else {
                                currentUseAmount_temp = UnitsUtil.getConvertedValue(qp, currentUseAmountUnit, availQuantityUnit, currentUseAmount_temp.replace(decimalSeperator, '.'));
                            }
                        }
                        multifactor = this.getMultiplicationForInventory(keyid1, amountscope);
                        if (prevAmount != null && prevAmount.length() > 0 && currentTrackitemid.equalsIgnoreCase(prevTrackitemid)) {
                            if (!prevAmountunit.equalsIgnoreCase(availQuantityUnit)) {
                                if (prevAmountunitType.equalsIgnoreCase("C")) {
                                    newValue = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, prevAmount, availQuantityUnit);
                                    prevAmount_temp = Double.toString(newValue);
                                } else if (availQuantityUnitType.equalsIgnoreCase("C")) {
                                    newValue = UnitsUtil.covertFromUnitsToContainer(this.getQueryProcessor(), containerSize, containerUnits, prevAmount, prevAmountunit);
                                    prevAmount_temp = Double.toString(newValue);
                                } else {
                                    prevAmount_temp = UnitsUtil.getConvertedValue(qp, prevAmountunit, availQuantityUnit, prevAmount_temp.replace(decimalSeperator, '.'));
                                }
                            }
                            prevAmount_temp = UnitsUtil.convertToLocateSeperated(prevAmount_temp, "" + decimalSeperator);
                            availQuantity = availQuantity.add(formatUtil.parseBigDecimal(prevAmount_temp));
                        }
                        currentUseAmount_temp = UnitsUtil.convertToLocateSeperated(currentUseAmount_temp, "" + decimalSeperator);
                        currentUseAmount = UnitsUtil.convertToLocateSeperated(currentUseAmount, "" + decimalSeperator);
                        if (currentUseAmount_temp != null && formatUtil.parseBigDecimal(currentUseAmount_temp).doubleValue() * (double)multifactor > availQuantity.doubleValue()) {
                            int currentUseAmountScale = ReagentUtil.getMaxScale(ajaxResponse.getRequestParameter("currentuseamount", ""), decimalSeperator);
                            double currentUsedAmountDouble = formatUtil.parseBigDecimal(currentUseAmount).doubleValue();
                            String enterAmountStr = Double.toString(currentUsedAmountDouble * (double)multifactor).replace('.', decimalSeperator);
                            String enterAmount = formatUtil.parseBigDecimal(enterAmountStr).setScale(currentUseAmountScale, 4) + " " + (currentUseAmountUnit.length() > 0 ? currentUseAmountUnit : currentUseAmountUnitType);
                            String availAmount = ReagentUtil.removeLastZerosAferDecimal(availQuantity.doubleValue(), decimalSeperator) + " " + (availQuantityUnit.length() > 0 ? availQuantityUnit : availQuantityUnitType);
                            warningmsg = tp.translate("Warning, entered Used Amount") + " (" + enterAmount.replace('.', decimalSeperator) + ") " + tp.translate("exceeds the current &nbsp;container's quantity.  Upon Saving, the container will be &nbsp;Depleted with a quantity of 0.  Otherwise correct the Used &nbsp;Amount") + " <= " + availAmount.replace('.', decimalSeperator);
                            okButtonNotes = tp.translate("Use <b>OK</b> to accept the changes to deplete the container.");
                            warningmsgHtml = "<table style='margin-top: 8px'>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>" + warningmsg + "</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + okButtonNotes + "</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + cancelButtonNotes + "</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                            warningmsgHtml = warningmsgHtml + "</table>";
                        }
                    }
                }
            }
            if (!skiptolerancecheck && warningmsgHtml.equals("") && reagentTypeId.length() > 0 && (ds = qp.getPreparedSqlDataSet(sqlRT = "SELECT * FROM reagenttyperecipe WHERE reagenttypeid = ? AND reagenttypeversionid = ? AND includereagenttypeid = ? AND coalesce(includereagenttypeversionid,' ') = ? AND coalesce(reagenttypestageid,' ') = ?", new Object[]{reagentTypeId, reagentTypeVersionId, includeReagentTypeId, includeReagentTypeVersionId = ajaxResponse.getRequestParameter("includereagenttypeversionid", " "), stageid = ajaxResponse.getRequestParameter("stageid", " ")})) != null && ds.size() > 0) {
                boolean toleranceRangeFound = false;
                String amount = ds.getString(0, "amounttext", "0");
                BigDecimal amountLowerTolerance = ds.getBigDecimal(0, "amountlowertolerance", new BigDecimal(0));
                BigDecimal amountUpperTolerance = ds.getBigDecimal(0, "amountuppertolerance", new BigDecimal(0));
                if (amountLowerTolerance.doubleValue() > 0.0 || amountUpperTolerance.doubleValue() > 0.0) {
                    if (recAmt.trim().length() == 0) {
                        recAmt = amount;
                    }
                    if (recAmtUnits.trim().length() == 0 && recAmtUnitsType.trim().length() == 0) {
                        recAmtUnits = ds.getString(0, "amountunits", "");
                        recAmtUnitsType = ds.getString(0, "amountunitstype", "");
                    }
                    int scale = ReagentUtil.getMaxScale(recAmt + ";" + amountLowerTolerance + ";" + amountUpperTolerance, decimalSeperator);
                    recAmt = recAmt.replace(decimalSeperator, '.');
                    amount = amount.replace(decimalSeperator, '.');
                    double multiplicationFactor = Double.valueOf(recAmt) / Double.valueOf(amount);
                    double recAmtLowerTolerance = amountLowerTolerance.doubleValue() * multiplicationFactor;
                    double recAmtUpperTolerance = amountUpperTolerance.doubleValue() * multiplicationFactor;
                    double lowerLimit = Double.valueOf(recAmt) - recAmtLowerTolerance;
                    double upperLimit = Double.valueOf(recAmt) + recAmtUpperTolerance;
                    String totalUsedAmount = ReagentUtil.getTotalValue(qp, formatUtil, useAmt, useAmtunit, useAmtunittype, containerSize, containerUnits);
                    String[] totalUsedAmountArr = StringUtil.split(totalUsedAmount, ";");
                    double resolvedUsedAmount = Double.valueOf(totalUsedAmountArr[0].replace(decimalSeperator, '.'));
                    String resolvedUsedAmountUnits = totalUsedAmountArr[1];
                    String resolvedUsedAmountUnitsType = totalUsedAmountArr[2];
                    double resolvedUsedAmount_temp = resolvedUsedAmount;
                    String toleranceCheckmsg = "";
                    if (!resolvedUsedAmountUnits.equalsIgnoreCase(recAmtUnits)) {
                        if (resolvedUsedAmountUnitsType.equalsIgnoreCase("C")) {
                            resolvedUsedAmount_temp = containerSize != null && containerSize.length() > 0 ? UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(resolvedUsedAmount), recAmtUnits) : resolvedUsedAmount_temp;
                        } else {
                            String amountStr = UnitsUtil.getConvertedValue(this.getQueryProcessor(), resolvedUsedAmountUnits, recAmtUnits, Double.toString(resolvedUsedAmount_temp));
                            resolvedUsedAmount_temp = Double.valueOf(amountStr.replace(decimalSeperator, '.'));
                        }
                    }
                    if (resolvedUsedAmount_temp * (double)multifactor < lowerLimit) {
                        toleranceCheckmsg = "below";
                        warningmsgHtml = "lower";
                    } else if (resolvedUsedAmount_temp * (double)multifactor > upperLimit) {
                        toleranceCheckmsg = "above";
                        warningmsgHtml = "upper";
                    }
                    if (toleranceCheckmsg.length() > 0) {
                        BigDecimal lt = this.setProperScale(lowerLimit, scale, decimalSeperator);
                        BigDecimal ut = this.setProperScale(upperLimit, scale, decimalSeperator);
                        int useAmountScale = ReagentUtil.getMaxScale(ajaxResponse.getRequestParameter("useamount", ""), decimalSeperator);
                        String resolvedUsedAmountStr = UnitsUtil.convertToLocateSeperated(Double.toString(resolvedUsedAmount * (double)multifactor), "" + decimalSeperator);
                        String enterAmount = formatUtil.parseBigDecimal(resolvedUsedAmountStr).setScale(useAmountScale, 4) + " " + (resolvedUsedAmountUnits.length() > 0 ? resolvedUsedAmountUnits : resolvedUsedAmountUnitsType);
                        String toleranceUnit = recAmtUnits.length() > 0 ? recAmtUnits : recAmtUnitsType;
                        String limit = (lt + " to " + ut + " " + toleranceUnit).replace('.', decimalSeperator);
                        enterAmount = enterAmount.replace('.', decimalSeperator);
                        warningmsg = tp.translate("Warning, entered Used Amount") + " (" + enterAmount + ") is " + toleranceCheckmsg + " " + tp.translate("the Tolerance Limit (" + limit + ").");
                        okButtonNotes = tp.translate("Use <b>OK</b> to accept the changes.");
                        warningmsgHtml = "<table style='margin-top: 8px'>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>" + warningmsg + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + okButtonNotes + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;" + cancelButtonNotes + "</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "<tr><td>&nbsp;</td> </tr>";
                        warningmsgHtml = warningmsgHtml + "</table>";
                    }
                }
            }
        }
        catch (Exception e) {
            warningmsgHtml = "<font color=\"red\">Failed to validate Use Amount/Unit. Exception: " + e.getMessage() + "</font>";
        }
        finally {
            ajaxResponse.addCallbackArgument("message", warningmsgHtml);
            ajaxResponse.addCallbackArgument("currenttrackitemid", currentTrackitemid);
            ajaxResponse.addCallbackArgument("colindx", colindx);
            ajaxResponse.print();
        }
    }

    private BigDecimal setProperScale(double value, int scale, char decimalSeperator) {
        int actualScale = ReagentUtil.getMaxScale(ReagentUtil.removeLastZerosAferDecimal(value, decimalSeperator), decimalSeperator);
        int resolvedScale = scale > 0 ? scale : (actualScale > 0 ? (actualScale > 2 ? 2 : actualScale) : scale);
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(resolvedScale, 4);
        return bd;
    }

    private int getMultiplicationForInventory(String keyid1, String amountscope) {
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
        if (sql.length() > 0) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            count = ds.getRowCount();
        }
        return count;
    }
}

