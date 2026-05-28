/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_TransferExecution
extends BaseSDCRules {
    public String ruleid = "TransferExecutionRule";
    public static final String SDCID = "LV_TransferExecution";
    public static final String KEYID1 = "keyid1";
    private static final String transferexecutionreagent = "transferexecutionreagent";
    private static final String fieldName_transferexecutionid = "transferexecutionid";
    private static final String fieldName_transferexecutionreagentid = "transferexecutionreagentid";
    private static final String fieldName_reagentlotid = "reagentlotid";
    private static final String fieldName_trackitemid = "trackitemid";
    private static final String fieldName_useamount = "useamount";
    private static final String fieldName_useamountunits = "useamountunits";
    private static final String fieldName_useamountunittype = "useamountunitstype";

    @Override
    public void postAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset(transferexecutionreagent);
        if (actionProps == null || actionProps.get(fieldName_trackitemid) == null || actionProps.get(fieldName_useamount) == null || actionProps.get(fieldName_useamountunits) == null || actionProps.get(fieldName_useamountunittype) == null || actionProps.get(fieldName_transferexecutionid) == null || actionProps.get(fieldName_transferexecutionreagentid) == null) {
            return;
        }
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        String currTIIds = actionProps.get(fieldName_trackitemid).toString();
        String[] allTIIds = StringUtil.split(currTIIds, ";");
        String[] prevAmountArr = null;
        if (actionProps.containsKey("prevamount")) {
            prevAmountArr = StringUtil.split(actionProps.getProperty("prevamount", ""), ";");
        }
        String currUseAmountList = actionProps.getProperty(fieldName_useamount, "0.0").replace(decimalSeparator, '.');
        String[] allUseAmounts = StringUtil.split(currUseAmountList, ";");
        String currUseAmountUnitList = actionProps.get(fieldName_useamountunits).toString();
        String[] allUseAmountUnits = StringUtil.split(currUseAmountUnitList, ";");
        String currUseAmountUnitsTypeList = actionProps.get(fieldName_useamountunittype).toString();
        String[] allUseAmountUnitsTypes = StringUtil.split(currUseAmountUnitsTypeList, ";");
        String transferExecutionIdList = actionProps.get(fieldName_transferexecutionid).toString();
        String[] allTransferExecutionIds = StringUtil.split(transferExecutionIdList, ";");
        String transferExecutionReagentIdList = actionProps.get(fieldName_transferexecutionreagentid).toString();
        String[] allTransferExecutionReagentIds = StringUtil.split(transferExecutionReagentIdList, ";");
        HashMap<String, String> currKeys = new HashMap<String, String>();
        for (int noOfTIs = 0; noOfTIs < allTIIds.length; ++noOfTIs) {
            String currTIId = allTIIds[noOfTIs];
            if (currTIId.equals("(null)")) continue;
            String currUseAmount = allUseAmounts[noOfTIs];
            String currUseAmountUnit = allUseAmountUnits[noOfTIs];
            String currUseAmountUnitsType = allUseAmountUnitsTypes[noOfTIs];
            if (currUseAmount == null || currUseAmount.length() == 0 || currUseAmount.equalsIgnoreCase("(null)")) {
                currUseAmount = "0.0";
            }
            if (currUseAmountUnit == null || currUseAmountUnit.equalsIgnoreCase("(null)")) {
                currUseAmountUnit = "";
            }
            if (currUseAmountUnitsType == null || currUseAmountUnitsType.equalsIgnoreCase("(null)")) {
                currUseAmountUnitsType = "";
            }
            currKeys.put(fieldName_transferexecutionid, allTransferExecutionIds[noOfTIs]);
            currKeys.put(fieldName_transferexecutionreagentid, allTransferExecutionReagentIds[noOfTIs]);
            int filteredRowNumber = oldPrimary.findRow(currKeys);
            PropertyList trackItemProperties = new PropertyList();
            String preUseReagentTIId = oldPrimary.getValue(filteredRowNumber, fieldName_trackitemid);
            if (preUseReagentTIId == null) {
                preUseReagentTIId = "";
            }
            if (preUseReagentTIId.length() == 0) {
                if (currTIId.length() > 0) {
                    trackItemProperties.setProperty(fieldName_trackitemid, currTIId);
                    trackItemProperties.setProperty("quantity", "" + this.negate(currUseAmount));
                    trackItemProperties.setProperty("quantityunit", currUseAmountUnit);
                    trackItemProperties.setProperty("quantitytype", currUseAmountUnitsType);
                    try {
                        this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
                    }
                    catch (SapphireException e) {
                        this.setError(this.ruleid, "FAILURE", "Failed to adjust container amount. Check the use amount and units.");
                        return;
                    }
                }
            } else {
                String preUseAmount = oldPrimary.getBigDecimal(filteredRowNumber, fieldName_useamount, new BigDecimal("0.0")).toString();
                if (prevAmountArr != null && prevAmountArr.length > noOfTIs) {
                    preUseAmount = prevAmountArr[noOfTIs];
                }
                String preUseAmountUnits = oldPrimary.getValue(filteredRowNumber, fieldName_useamountunits);
                String preUseAmountUnitsType = oldPrimary.getValue(filteredRowNumber, fieldName_useamountunittype);
                if (preUseAmount == null || preUseAmount.length() == 0 || preUseAmount.equalsIgnoreCase("(null)")) {
                    preUseAmount = "0.0";
                }
                if (preUseAmountUnits == null || preUseAmountUnits.equalsIgnoreCase("(null)")) {
                    preUseAmountUnits = "";
                }
                if (preUseAmountUnitsType == null || preUseAmountUnitsType.equalsIgnoreCase("(null)")) {
                    preUseAmountUnitsType = "";
                }
                if (currTIId.length() > 0) {
                    double dcurrUseAmt;
                    if (!preUseReagentTIId.equals(currTIId)) {
                        trackItemProperties.setProperty(fieldName_trackitemid, preUseReagentTIId + ";" + currTIId);
                        trackItemProperties.setProperty("quantity", preUseAmount + ";" + this.negate(currUseAmount));
                        trackItemProperties.setProperty("quantityunit", preUseAmountUnits + ";" + currUseAmountUnit);
                        trackItemProperties.setProperty("quantitytype", preUseAmountUnitsType + ";" + currUseAmountUnitsType);
                        try {
                            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
                        }
                        catch (SapphireException e) {
                            this.setError(this.ruleid, "FAILURE", "Failed to adjust container amount. Check the use amount and units.");
                            return;
                        }
                    }
                    double dpreUseAmt = Double.parseDouble(preUseAmount);
                    if (dpreUseAmt == (dcurrUseAmt = Double.parseDouble(currUseAmount)) && preUseAmountUnits.equalsIgnoreCase(currUseAmountUnit) && preUseAmountUnitsType.equalsIgnoreCase(currUseAmountUnitsType)) continue;
                    if (dpreUseAmt != 0.0) {
                        trackItemProperties.setProperty(fieldName_trackitemid, currTIId);
                        trackItemProperties.setProperty("quantity", preUseAmount);
                        trackItemProperties.setProperty("quantityunit", preUseAmountUnits);
                        trackItemProperties.setProperty("quantitytype", preUseAmountUnitsType);
                        try {
                            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
                        }
                        catch (SapphireException e) {
                            this.setError(this.ruleid, "FAILURE", "Failed to adjust container amount. Check the use amount and units.");
                            return;
                        }
                    }
                    if (dcurrUseAmt != 0.0) {
                        trackItemProperties.setProperty(fieldName_trackitemid, currTIId);
                        trackItemProperties.setProperty("quantity", this.negate(currUseAmount));
                        trackItemProperties.setProperty("quantityunit", currUseAmountUnit);
                        trackItemProperties.setProperty("quantitytype", currUseAmountUnitsType);
                        try {
                            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
                        }
                        catch (SapphireException e) {
                            this.setError(this.ruleid, "FAILURE", "Failed to adjust container amount. Check the use amount and units.");
                            return;
                        }
                    }
                }
            }
            currKeys.clear();
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditDetailImage() {
        return true;
    }

    private String negate(String amount) {
        if (amount == null || amount.length() == 0) {
            return "0.0";
        }
        double amt = Double.parseDouble(amount);
        amt = 0.0 - amt;
        return "" + amt;
    }
}

