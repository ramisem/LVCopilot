/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetTotalConsumableQuantity
extends BaseSDIAction
implements sapphire.action.GetTotalConsumableQuantity {
    private static final String COLUMN_QTYCURRENT = "qtycurrent";
    private static final String COLUMN_QTYUNIT = "qtyunits";
    private static final String COLUMN_QTYCURRENTTYPE = "qtycurrenttype";
    private static final String COLUMN_CONTAINERSIZE = "sizevalue";
    private static final String COLUMN_CONTAINERUNITS = "sizeunits";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String reagenttypeid = properties.getProperty("reagenttypeid", "");
        String reagenttypeversionid = properties.getProperty("reagenttypeversionid", "");
        String reagentlotid = properties.getProperty("reagentlotid", "");
        String totalQty = "0";
        boolean considerVersion = ReagentUtil.considerVersionForThresholdNotification(this.getConfigurationProcessor());
        if (reagenttypeid.trim().length() > 0 || reagentlotid.trim().length() > 0) {
            if (reagenttypeid.trim().length() > 0 && reagentlotid.trim().length() > 0) {
                totalQty = "(Failed to calculate total Quantity: Invalid parameters in Consumable Event Plan - Both Consumable Type and Consumable Lot are passed in. Please pass either Consumable Type or Consumable Lot.)";
            } else if (considerVersion && reagenttypeid.trim().length() > 0 && reagenttypeversionid.trim().length() == 0) {
                totalQty = "(Failed to calculate total Quantity: As per ConsumablePolicy Setting, Consumable Type Version must be passed in the Consumable Event Plan.)";
            } else {
                String defaultDept = this.connectionInfo.getDefaultDepartment();
                String departmentid = properties.getProperty("departmentid", defaultDept);
                BigDecimal totalAvailableQuantity = new BigDecimal("0.0");
                FormatUtil formatUtil = FormatUtil.getInstance(this.connectionInfo);
                DataSet ds = new DataSet();
                if (reagenttypeid.trim().length() > 0) {
                    ds = ReagentUtil.getDSForTotalQuantityPerType(this.getConfigurationProcessor(), this.getSDCProcessor(), this.getQueryProcessor(), reagenttypeid, reagenttypeversionid, departmentid);
                } else if (reagentlotid.trim().length() > 0) {
                    ds = ReagentUtil.getDSForTotalQuantityPerLot(this.getConfigurationProcessor(), this.getSDCProcessor(), this.getQueryProcessor(), reagentlotid, departmentid);
                }
                if (ds != null && ds.getRowCount() > 0) {
                    QuantityUnitsInfo thresholdInfo = this.getReagentThresholdInfo(reagenttypeid, reagenttypeversionid, reagentlotid);
                    if (thresholdInfo.units.length() > 0 || thresholdInfo.unitType.equalsIgnoreCase("C")) {
                        for (int row = 0; row < ds.getRowCount(); ++row) {
                            String trackitemid = ds.getString(row, "trackitemid");
                            QuantityUnitsInfo tiQty = new QuantityUnitsInfo();
                            tiQty.quantity = ds.getBigDecimal(row, COLUMN_QTYCURRENT, new BigDecimal(0)).toString();
                            tiQty.units = ds.getString(row, COLUMN_QTYUNIT);
                            tiQty.unitType = ds.getString(row, COLUMN_QTYCURRENTTYPE);
                            tiQty.containerSize = ds.getValue(row, COLUMN_CONTAINERSIZE);
                            tiQty.containerUnits = ds.getString(row, COLUMN_CONTAINERUNITS);
                            QuantityUnitsInfo convertedTI = null;
                            try {
                                convertedTI = this.convertTIQtyToThresholdUnits(tiQty, thresholdInfo);
                            }
                            catch (SapphireException e) {
                                this.logger.error("Failed to convert trackitem to threshold units: " + trackitemid);
                            }
                            if (convertedTI == null) continue;
                            totalAvailableQuantity = totalAvailableQuantity.add(formatUtil.parseBigDecimal(convertedTI.quantity));
                        }
                        totalQty = ReagentUtil.removeLastZerosAferDecimal(totalAvailableQuantity.doubleValue(), formatUtil.getDecimalSeparator());
                        totalQty = UnitsUtil.convertToLocateSeperated(totalQty, formatUtil.getDecimalSeparator() + "");
                        totalQty = thresholdInfo.unitType.equalsIgnoreCase("C") ? totalQty + " (Containers)" : totalQty + " " + thresholdInfo.units;
                    } else {
                        totalQty = "(Failed to calculate total Quantity: Missing threshold unit)";
                    }
                }
            }
        } else {
            totalQty = "(Failed to calculate total Quantity: Missing parameters - Please pass either Consumable Type or Consumable Lot.)";
        }
        properties.setProperty("totalavailablequantity", totalQty);
    }

    private QuantityUnitsInfo getReagentThresholdInfo(String reagenttypeid, String reagenttypeversionid, String reagentlotid) {
        QuantityUnitsInfo thresholdInfo = new QuantityUnitsInfo();
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        if (reagenttypeid.trim().length() > 0) {
            sql.append("SELECT reagenttype.reorderthresholdunittype, reagenttype.reorderthresholdunits,containertype.sizevalue, containertype.sizeunits ");
            sql.append("FROM reagenttype ");
            sql.append("LEFT OUTER JOIN containertype on reagenttype.containertypeid=containertype.containertypeid ");
            sql.append("WHERE reagenttypeid =").append(safeSQL.addVar(reagenttypeid));
            if (reagenttypeversionid.trim().length() > 0) {
                sql.append(" AND reagenttypeversionid = ").append(safeSQL.addVar(reagenttypeversionid));
            } else {
                sql.append(" AND reagenttypeversionid = ").append(this.resolveCurrentVersionCluase());
            }
        } else {
            sql.append("SELECT reagenttype.reorderthresholdunittype, reagenttype.reorderthresholdunits,containertype.sizevalue, containertype.sizeunits ");
            sql.append(" FROM reagentlot,reagenttype ");
            sql.append(" LEFT OUTER JOIN containertype on reagenttype.containertypeid=containertype.containertypeid ");
            sql.append(" WHERE reagentlotid =").append(safeSQL.addVar(reagentlotid));
            sql.append(" and reagentlot.reagenttypeid=reagenttype.reagenttypeid");
            sql.append(" and reagentlot.reagenttypeversionid=reagenttype.reagenttypeversionid");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            thresholdInfo.units = ds.getString(0, "reorderthresholdunits", "");
            thresholdInfo.unitType = ds.getString(0, "reorderthresholdunittype", "U");
            thresholdInfo.containerSize = ds.getValue(0, COLUMN_CONTAINERSIZE);
            thresholdInfo.containerUnits = ds.getString(0, COLUMN_CONTAINERUNITS);
        }
        return thresholdInfo;
    }

    private String resolveCurrentVersionCluase() {
        return "coalesce( " + this.getCurrentRT() + "," + this.getMaxProvisionalRT() + ")";
    }

    private String getCurrentRT() {
        return "(select rt1.reagenttypeversionid from reagenttype rt1 where  rt1.reagenttypeid=reagenttype.reagenttypeid and rt1.versionstatus='C')";
    }

    private String getMaxProvisionalRT() {
        return "(select cast(max(cast(rt2.reagenttypeversionid as integer)) as varchar(40)) from reagenttype rt2 where rt2.reagenttypeid=reagenttype.reagenttypeid and rt2.versionstatus='P')";
    }

    private QuantityUnitsInfo convertTIQtyToThresholdUnits(QuantityUnitsInfo tiQty, QuantityUnitsInfo threshold) throws SapphireException {
        if (tiQty.unitType == null || tiQty.unitType.length() == 0) {
            tiQty.unitType = "U";
        }
        if (threshold.unitType == null || threshold.unitType.length() == 0) {
            threshold.unitType = "U";
        }
        if (tiQty.unitType.equalsIgnoreCase("C") && threshold.unitType.equalsIgnoreCase("C")) {
            return tiQty;
        }
        if (tiQty.unitType.equalsIgnoreCase("U") && threshold.unitType.equalsIgnoreCase("U")) {
            if (!(tiQty.units != null && tiQty.units.length() != 0 || threshold.units != null && threshold.units.length() != 0)) {
                return tiQty;
            }
            if (tiQty.units.equalsIgnoreCase(threshold.units)) {
                return tiQty;
            }
            tiQty.quantity = UnitsUtil.getConvertedValue(this.getQueryProcessor(), tiQty.units, threshold.units, tiQty.quantity);
            tiQty.units = threshold.units;
            return tiQty;
        }
        if (tiQty.unitType.equalsIgnoreCase("C")) {
            double convertedQty = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), tiQty.containerSize, tiQty.containerUnits, tiQty.quantity, threshold.units);
            tiQty.quantity = Double.toString(convertedQty);
            tiQty.unitType = "U";
            tiQty.units = threshold.units;
            tiQty.containerSize = "";
            tiQty.containerUnits = "";
            return tiQty;
        }
        if (threshold.unitType.equalsIgnoreCase("C")) {
            double convertedQty = UnitsUtil.covertToContainersFromUnits(this.getQueryProcessor(), threshold.containerSize, threshold.containerUnits, tiQty.quantity, tiQty.units);
            tiQty.quantity = Double.toString(convertedQty);
            tiQty.units = "";
            tiQty.unitType = "C";
            tiQty.containerSize = threshold.containerSize;
            tiQty.containerUnits = threshold.containerUnits;
            return tiQty;
        }
        return tiQty;
    }

    private class QuantityUnitsInfo {
        String quantity;
        String units;
        String unitType;
        String containerSize;
        String containerUnits;

        private QuantityUnitsInfo() {
        }
    }
}

