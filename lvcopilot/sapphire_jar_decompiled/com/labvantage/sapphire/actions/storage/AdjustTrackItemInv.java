/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AdjustTrackItemInv
extends BaseAction
implements sapphire.action.AdjustTrackItemInv {
    public static final String PROPERTY_SDCRULECONFIRM = "__sdcruleconfirm";
    private FormatUtil formatUtil;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.formatUtil = FormatUtil.getInstance(this.connectionInfo);
        String updateQtyItems = properties.getProperty("quantity");
        if (updateQtyItems == null || updateQtyItems.equalsIgnoreCase("0.0")) {
            return;
        }
        DataSet trackItems = this.parseProperties(properties);
        trackItems.addColumn("newValue", 0);
        trackItems.addColumn("newqtyunits", 0);
        trackItems.addColumn("newqtytype", 0);
        for (int row = 0; row < trackItems.getRowCount(); ++row) {
            String qtyPlusUnits;
            String[] toks;
            String trackitemstatus = trackItems.getString(row, "trackitemstatus", "");
            if (trackitemstatus.equalsIgnoreCase("Depleted")) {
                if ("Y".equals(properties.getProperty("validatequantity", "N"))) {
                    throw new SapphireException("Volume cannot be decremented because the TrackItem is already depleted.");
                }
                trackItems.deleteRow(row);
                --row;
                continue;
            }
            double newQuantity = 0.0;
            String updateQty = trackItems.getValue(row, "updateQty");
            String updateQtyType = trackItems.getValue(row, "updateQtyType");
            String updateQtyUnits = trackItems.getValue(row, "updateQtyUnits");
            String tiQtyUnits = trackItems.getValue(row, "qtyunits");
            String tiQtyCurrent = trackItems.getValue(row, "qtycurrent", "0");
            String tiQtyType = trackItems.getValue(row, "qtycurrenttype");
            String containerSize = trackItems.getValue(row, "sizevalue");
            String containerUnits = trackItems.getValue(row, "sizeunits");
            if (!(tiQtyType != null && tiQtyType.length() != 0 || tiQtyUnits != null && tiQtyUnits.length() != 0)) {
                if (updateQtyType != null && updateQtyType.equalsIgnoreCase("C")) {
                    tiQtyType = "C";
                    trackItems.setValue(row, "qtycurrenttype", tiQtyType);
                } else {
                    tiQtyUnits = updateQtyUnits;
                    trackItems.setValue(row, "qtyunits", tiQtyUnits);
                }
            }
            if ((newQuantity = Double.parseDouble((toks = StringUtil.split(qtyPlusUnits = this.addQuantities(tiQtyCurrent, tiQtyUnits, tiQtyType, updateQty, updateQtyUnits, updateQtyType, containerSize, containerUnits), "|"))[0])) < 0.0) {
                if ("Y".equals(properties.getProperty("validatequantity", "N"))) {
                    throw new SapphireException("Volume cannot be decremented since the decrement value is more than the available value");
                }
                newQuantity = 0.0;
            }
            String newQuantityStr = Double.toString(newQuantity);
            trackItems.setValue(row, "newValue", newQuantityStr.replace('.', this.formatUtil.getDecimalSeparator()));
            trackItems.setValue(row, "newqtyunits", toks[1]);
            trackItems.setValue(row, "newqtytype", toks[2]);
        }
        String newTrackitems = trackItems.getColumnValues("trackitemid", ";");
        String newQuantities = trackItems.getColumnValues("newValue", ";");
        String newQtyUnits = trackItems.getColumnValues("newqtyunits", ";");
        String newQtyTypes = trackItems.getColumnValues("newqtytype", ";");
        HashMap<String, String> actionProps = new HashMap<String, String>();
        actionProps.put("trackitemid", newTrackitems);
        actionProps.put("qtycurrent", newQuantities);
        actionProps.put("qtyunits", newQtyUnits);
        actionProps.put("qtycurrenttype", newQtyTypes);
        actionProps.put("auditreason", (String)properties.get("auditreason"));
        actionProps.put("auditactivity", properties.getProperty("auditactivity", ""));
        actionProps.put("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
        actionProps.put(PROPERTY_SDCRULECONFIRM, (String)properties.get(PROPERTY_SDCRULECONFIRM));
        try {
            this.getActionProcessor().processAction("EditTrackItem", "1", actionProps);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
        properties.put("updatequantity", newQuantities);
        properties.put("updatequantityunit", newQtyUnits);
        properties.put("updatequantitytype", newQtyTypes);
    }

    private DataSet parseProperties(PropertyList properties) throws SapphireException {
        int i;
        int rc = 1;
        String trackItemIdItems = properties.getProperty("trackitemid");
        String updateQtyItems = properties.getProperty("quantity");
        String updateQtyUnitItems = properties.getProperty("quantityunit");
        String updateQtyTypeItems = properties.getProperty("quantitytype");
        if (trackItemIdItems == null || trackItemIdItems.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", " trackitemid parameter is null or \"\"");
        }
        if (updateQtyItems == null || updateQtyItems.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "quantity parameter is null or \"\"");
        }
        DAMProcessor dam = this.getDAMProcessor();
        boolean deleterset = false;
        boolean applylock = properties.getProperty("applylock").equals("Y");
        StringHolder rsetidHolder = new StringHolder();
        rc = applylock ? dam.createLockedRSet("TrackItemSDC", properties.getProperty("trackitemid"), "", "", rsetidHolder) : dam.createRSet("TrackItemSDC", properties.getProperty("trackitemid"), "", "", rsetidHolder);
        if (rc != 1) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
        }
        String rsetid = rsetidHolder.value;
        deleterset = true;
        DataSet trackitems = new DataSet(this.connectionInfo);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT trackitemid, trackitemstatus, qtyunits, qtycurrent,qtycurrenttype");
        sql.append(",trackitem.containertypeid, sizevalue, sizeunits ");
        sql.append("FROM trackitem ");
        sql.append("JOIN rsetitems on rsetitems.keyid1 = trackitem.trackitemid ");
        sql.append("LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
        sql.append("WHERE rsetitems.sdcid = 'TrackItemSDC' ");
        sql.append("AND rsetitems.rsetid =?");
        try {
            this.database.createPreparedResultSet(sql.toString(), new Object[]{rsetid});
            trackitems.setResultSet(this.database.getResultSet());
        }
        catch (SapphireException e) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to get result set. Reason: " + sql.toString(), ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        if (trackitems.isEmpty()) {
            throw new SapphireException("INVALID_PARAMETER", "Failed to find any trackitems with the specified ids:" + trackItemIdItems);
        }
        int rowCount = trackitems.getRowCount();
        String[] ids = StringUtil.split(trackItemIdItems, ";");
        trackitems.addColumn("updateQty", 0);
        trackitems.addColumn("updateQtyUnits", 0);
        trackitems.addColumn("updateQtyType", 0);
        DataSet inputInfo = new DataSet();
        inputInfo.addColumnValues("trackitemid", 0, trackItemIdItems, ";");
        inputInfo.addColumnValues("updateQty", 0, updateQtyItems, ";", "");
        inputInfo.addColumnValues("updateQtyUnits", 0, updateQtyUnitItems, ";", "");
        inputInfo.addColumnValues("updateQtyType", 0, updateQtyTypeItems, ";", "U");
        inputInfo.padColumn("updateQty");
        HashMap<String, String> find = new HashMap<String, String>();
        for (i = 0; i < inputInfo.getRowCount(); ++i) {
            String trackitemid = inputInfo.getString(i, "trackitemid");
            find.put("trackitemid", trackitemid);
            int findRow = trackitems.findRow(find);
            if (findRow == -1) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Unrecognized trackitem: '" + trackitemid + "'");
            }
            String updateQty = inputInfo.getValue(i, "updateQty");
            String updateQtyUnits = inputInfo.getValue(i, "updateQtyUnits");
            String updateQtyType = inputInfo.getValue(i, "updateQtyType");
            String currentUpdateQty = trackitems.getValue(findRow, "updateQty");
            if (currentUpdateQty.equals("")) {
                trackitems.setString(findRow, "updateQty", updateQty);
                if ("C".equals(updateQtyType)) {
                    updateQtyUnits = "";
                }
                trackitems.setString(findRow, "updateQtyUnits", updateQtyUnits);
                trackitems.setString(findRow, "updateQtyType", updateQtyType);
                continue;
            }
            String currentUpdateQtyUnits = trackitems.getValue(findRow, "updateQtyUnits");
            String currentUpdateQtyType = trackitems.getValue(findRow, "updateQtyType");
            String containerSize = trackitems.getValue(findRow, "sizevalue");
            String containerUnits = trackitems.getValue(findRow, "sizeunits");
            String val = this.addQuantities(currentUpdateQty, currentUpdateQtyUnits, currentUpdateQtyType, updateQty, updateQtyUnits, updateQtyType, containerSize, containerUnits);
            String[] toks = StringUtil.split(val, "|");
            trackitems.setString(findRow, "updateQty", toks[0].replace('.', this.formatUtil.getDecimalSeparator()));
            trackitems.setString(findRow, "updateQtyUnits", toks[1]);
            trackitems.setString(findRow, "updateQtyType", toks[2]);
        }
        for (i = 0; i < rowCount; ++i) {
            String qtyCurrent = trackitems.getValue(i, "qtycurrent", "0");
            String qtyCurrentType = trackitems.getValue(i, "qtycurrenttype");
            String qtyUnits = trackitems.getValue(i, "qtyunits");
            String containerTypeId = trackitems.getValue(i, "containertypeid");
            String trackItemId = trackitems.getValue(i, "trackitemid");
            String updateQty = trackitems.getValue(i, "updateQty");
            String updateQtyUnits = trackitems.getValue(i, "updateQtyUnits");
            String updateQtyType = trackitems.getValue(i, "updateQtyType");
            if (qtyCurrent == null || qtyCurrent.length() == 0) {
                throw new SapphireException("INVALID_PARAMETER", "qtycurrent is null for (" + trackItemId + ")");
            }
            if (updateQty == null || updateQty.length() == 0) {
                throw new SapphireException("INVALID_PARAMETER", "Update quantity input not specified (" + trackItemId + ")");
            }
            if (qtyCurrentType != null && qtyCurrentType.equalsIgnoreCase("C") && qtyUnits != null && qtyUnits.length() != 0) {
                throw new SapphireException("INVALID_PARAMETERS", "qtycurrenttype and qtyunits are conflicting for trackitem" + trackItemId);
            }
            if (updateQtyType == null || updateQtyType.length() == 0) {
                trackitems.setValue(i, "updateQtyType", "U");
            }
            if (updateQtyType != null && updateQtyType.equalsIgnoreCase("C") && updateQtyUnits != null && updateQtyUnits.length() != 0) {
                throw new SapphireException("INVALID_PARAMETERS", "quantity units type and quantityunits. are conflicting for trackitem" + trackItemId);
            }
            if (updateQtyType == null || !updateQtyType.equalsIgnoreCase("U") || updateQtyUnits != null && updateQtyUnits.length() != 0) continue;
            trackitems.setValue(i, "updateQtyUnits", qtyUnits);
        }
        return trackitems;
    }

    private String addQuantities(String qty1, String units1, String type1, String qty2, String units2, String type2, String containerSize, String containerUnits) throws SapphireException {
        try {
            String finalUnits = units1;
            String finalType = type1;
            int scale = ReagentUtil.getMaxScale(qty1 + ";" + qty2, this.formatUtil.getDecimalSeparator(), 3);
            if (qty1 != null && qty1.length() > 0) {
                qty1 = qty1.replace('.', this.formatUtil.getDecimalSeparator());
            }
            if (qty2 != null && qty2.length() > 0) {
                qty2 = qty2.replace('.', this.formatUtil.getDecimalSeparator());
            }
            if (units1.equals(units2) && type1.equals(type2)) {
                double dval = this.formatUtil.parseBigDecimal(qty1).setScale(scale, 4).doubleValue() + this.formatUtil.parseBigDecimal(qty2).setScale(scale, 4).doubleValue();
                return new BigDecimal(dval).setScale(scale, 4).toString() + "|" + finalUnits + "|" + finalType;
            }
            double dqty1 = this.formatUtil.parseBigDecimal(qty1).setScale(scale, 4).doubleValue();
            double dqty2 = this.formatUtil.parseBigDecimal(qty2).setScale(scale, 4).doubleValue();
            if ("C".equals(type2)) {
                if ("".equals(containerSize) || "".equals(containerUnits)) {
                    throw new SapphireException("Trackitem does not have container information");
                }
                dqty2 = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(dqty2), units1);
            } else if ("C".equals(type1)) {
                if ("".equals(containerSize) || "".equals(containerUnits)) {
                    throw new SapphireException("Trackitem does not have container information");
                }
                if (!"".equals(units2) && UnitsUtil.isWholeContainers(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(dqty2), units2)) {
                    dqty2 = UnitsUtil.covertToContainersFromUnits(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(dqty2), units2);
                } else {
                    dqty1 = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(dqty1), containerUnits);
                    if (!"".equals(units2) && !units2.equals(containerUnits)) {
                        dqty2 = Double.parseDouble(UnitsUtil.getConvertedValue(this.getQueryProcessor(), units2, containerUnits, Double.toString(dqty2)));
                    }
                    finalUnits = containerUnits;
                    finalType = type2;
                }
            } else {
                String newqty2 = UnitsUtil.getConvertedValue(this.getQueryProcessor(), units2, units1, Double.toString(dqty2));
                dqty2 = Double.parseDouble(newqty2.replace(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator(), '.'));
            }
            double finalQuantity = BigDecimal.valueOf(dqty2).add(BigDecimal.valueOf(dqty1)).doubleValue();
            return finalQuantity + "|" + finalUnits + "|" + finalType;
        }
        catch (Exception e) {
            throw new SapphireException("INVALID_PARAMETERS", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }
}

