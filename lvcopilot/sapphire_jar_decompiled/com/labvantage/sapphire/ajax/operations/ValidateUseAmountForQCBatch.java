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

public class ValidateUseAmountForQCBatch
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

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
        TranslationProcessor tp = this.getTranslationProcessor();
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String amountscope = ajaxResponse.getRequestParameter("amountscope", "");
        String currentTrackitemid = ajaxResponse.getRequestParameter("currenttrackitemid", "");
        String currentUseAmount = ajaxResponse.getRequestParameter("currentuseamount", "");
        String currentUseAmountUnit = ajaxResponse.getRequestParameter("currentuseamountunit", "");
        String currentUseAmountUnitType = ajaxResponse.getRequestParameter("currentuseamountunittype", "");
        String prevAmount = ajaxResponse.getRequestParameter("prevamount", "");
        String prevAmountunit = ajaxResponse.getRequestParameter("prevamountunit", "");
        String prevAmountunitType = ajaxResponse.getRequestParameter("prevamountunittype", "");
        String elementid = ajaxResponse.getRequestParameter("elementid", "");
        String fieldname = ajaxResponse.getRequestParameter("fieldname", "");
        String warningmsgHtml = "";
        String cancelButtonNotes = tp.translate("Use <b>Cancel</b> to revert the quantity back to the original value.");
        char decimalSeperator = formatUtil.getDecimalSeparator();
        try {
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
                    String warningmsg = tp.translate("Warning, container already Depleted.  Used Amount &nbsp;adjustments will not automatically adjust the container's &nbsp;inventory.  However, you may adjust the container inventory &nbsp;manually.");
                    String okButtonNotes = tp.translate("Use <b>OK</b> to accept the changes without adjusting the container's inventory.");
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
                    currentUseAmount = this.getActualValue(currentUseAmount, keyid1, amountscope, formatUtil);
                    prevAmount = this.getActualValue(prevAmount, keyid1, amountscope, formatUtil);
                    Double availQuantity = trackitemDS.getDouble(0, "qtycurrent", 0.0);
                    String availQuantityUnit = trackitemDS.getString(0, "qtyunits", "");
                    String availQuantityUnitType = trackitemDS.getString(0, "qtycurrenttype", "");
                    String containerSize = trackitemDS.getValue(0, "sizevalue", "");
                    String containerUnits = trackitemDS.getString(0, "sizeunits", "");
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
                    if (prevAmount != null && prevAmount.length() > 0) {
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
                        availQuantity = availQuantity + formatUtil.parseBigDecimal(prevAmount_temp).doubleValue();
                    }
                    if (currentUseAmount_temp != null && formatUtil.parseBigDecimal(currentUseAmount_temp).doubleValue() > availQuantity) {
                        int currentUseAmountScale = ReagentUtil.getMaxScale(ajaxResponse.getRequestParameter("currentuseamount", ""), decimalSeperator);
                        String enterAmount = formatUtil.parseBigDecimal(currentUseAmount).setScale(currentUseAmountScale, 4) + " " + (currentUseAmountUnit.length() > 0 ? currentUseAmountUnit : currentUseAmountUnitType);
                        String warningmsg = tp.translate("Warning, entered Used Amount ( " + enterAmount.replace('.', decimalSeperator) + " ) exceeds the current &nbsp;container's quantity.  Upon Saving, the container will be &nbsp;Depleted with a quantity of 0.  Otherwise correct the Used &nbsp;Amount <= ") + availQuantity + " " + availQuantityUnit;
                        String okButtonNotes = tp.translate("Use <b>OK</b> to accept the changes to deplete the container.");
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
            ajaxResponse.addCallbackArgument("elementid", elementid);
            ajaxResponse.addCallbackArgument("currenttrackitemid", currentTrackitemid);
            ajaxResponse.addCallbackArgument("fieldname", fieldname);
            ajaxResponse.print();
        }
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

    private String getActualValue(String amount, String keyid1, String amountscope, FormatUtil formatUtil) {
        String value = "";
        boolean first = true;
        String[] amountArr = StringUtil.split(amount, ";");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] amountscopeArr = StringUtil.split(amountscope, ";");
        for (int i = 0; i < amountArr.length; ++i) {
            String amtScope = amountscope.contains(";") ? amountscopeArr[i] : amountscope;
            String key = keyid1.contains(";") ? keyid1Arr[i] : keyid1;
            int multifactor = this.getMultiplicationForInventory(key, amtScope);
            String amt = this.getMultipliedValue(multifactor, amountArr[i], formatUtil);
            value = first ? amt : value + ";" + amt;
            first = false;
        }
        return value;
    }

    private String getMultipliedValue(int multifactor, String value, FormatUtil formatUtil) {
        if (multifactor > 1 && value.length() > 0) {
            double doubleValue = Double.parseDouble(value.replace(formatUtil.getDecimalSeparator(), '.'));
            value = Double.toString(doubleValue *= (double)multifactor);
            value = value.replace('.', formatUtil.getDecimalSeparator());
        }
        return value;
    }
}

