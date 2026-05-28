/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class UnitsUtil {
    public static double convertFromContainersToUnits(QueryProcessor qp, String containerSize, String containerUnits, String quantity, String convertUnits) throws SapphireException {
        quantity = UnitsUtil.convertToPointSeperated(quantity);
        containerSize = UnitsUtil.convertToPointSeperated(containerSize);
        double updateQty = Double.parseDouble(quantity);
        double containerCapacity = Double.parseDouble(containerSize);
        double qtyInContainerUnits = updateQty * containerCapacity;
        String qtyInSpecifiedUnits = UnitsUtil.getConvertedValue(qp, containerUnits, convertUnits, new Double(qtyInContainerUnits).toString());
        return Double.parseDouble(qtyInSpecifiedUnits);
    }

    public static double covertToContainersFromUnits(QueryProcessor qp, String containerSize, String containerUnits, String quantity, String quantityUnits) throws SapphireException {
        double containerCapacity;
        if (!quantityUnits.equalsIgnoreCase(containerUnits) && (quantity = UnitsUtil.getConvertedValue(qp, quantityUnits, containerUnits, quantity)) == null) {
            throw new SapphireException("No unit conversion found between " + quantityUnits + " and " + containerUnits);
        }
        quantity = UnitsUtil.convertToPointSeperated(quantity);
        containerSize = UnitsUtil.convertToPointSeperated(containerSize);
        double updateQty = Double.parseDouble(quantity);
        double ret = updateQty / (containerCapacity = Double.parseDouble(containerSize));
        if (ret < 0.0) {
            return -Math.ceil(-ret);
        }
        return Math.ceil(ret);
    }

    public static double covertFromUnitsToContainer(QueryProcessor qp, String containerSize, String containerUnits, String quantity, String quantityUnits) throws SapphireException {
        if (!quantityUnits.equalsIgnoreCase(containerUnits) && (quantity = UnitsUtil.getConvertedValue(qp, quantityUnits, containerUnits, quantity)) == null) {
            throw new SapphireException("No unit conversion found between " + quantityUnits + " and " + containerUnits);
        }
        quantity = UnitsUtil.convertToPointSeperated(quantity);
        containerSize = UnitsUtil.convertToPointSeperated(containerSize);
        double updateQty = Double.parseDouble(quantity);
        double containerCapacity = Double.parseDouble(containerSize);
        return updateQty / containerCapacity;
    }

    public static String getConvertedValue(QueryProcessor qp, String fromUnit, String toUnit, String value) throws SapphireException {
        if (fromUnit == null) {
            fromUnit = "";
        }
        if (toUnit == null) {
            toUnit = "";
        }
        if (fromUnit.equalsIgnoreCase(toUnit) || fromUnit.length() == 0 || toUnit.length() == 0) {
            return value;
        }
        value = UnitsUtil.convertToPointSeperated(value);
        String returnValue = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT expression FROM unitconversion WHERE unitsid=");
        sql.append(safeSQL.addVar(fromUnit));
        sql.append(" and tounits=");
        sql.append(safeSQL.addVar(toUnit));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.getRowCount() <= 0) {
            Trace.log("Failed to convert units between " + fromUnit + " to " + toUnit);
            throw new SapphireException("Failed to convert units between " + fromUnit + " to " + toUnit);
        }
        String expression = ds.getString(0, "expression");
        expression = StringUtil.replaceAll(expression, "[this]", value);
        returnValue = ExpressionUtil.evaluate(expression, new HashMap());
        returnValue = UnitsUtil.convertToPointSeperated(returnValue);
        return returnValue;
    }

    public static boolean isWholeContainers(QueryProcessor qp, String containerSize, String containerUnits, String quantity, String quantityUnits) throws SapphireException {
        double containerCapacity;
        if (!quantityUnits.equalsIgnoreCase(containerUnits) && (quantity = UnitsUtil.getConvertedValue(qp, quantityUnits, containerUnits, quantity)) == null) {
            throw new SapphireException("No unit conversion found between " + quantityUnits + " and " + containerUnits);
        }
        quantity = UnitsUtil.convertToPointSeperated(quantity);
        containerSize = UnitsUtil.convertToPointSeperated(containerSize);
        double updateQty = Double.parseDouble(quantity);
        double ret = updateQty / (containerCapacity = Double.parseDouble(containerSize));
        return Math.floor(ret) == ret;
    }

    public static String getContainerUnit(DBAccess db, String containerTypeId) throws SapphireException {
        String containerUnit = "";
        db.createPreparedResultSet("getcontainerunit", "SELECT sizeunits FROM containertype WHERE containertypeid=?", new Object[]{containerTypeId});
        if (db.getNext("getcontainerunit")) {
            containerUnit = db.getString("getcontainerunit", "sizeunits");
        }
        return containerUnit;
    }

    public static boolean isUnitCompatible(QueryProcessor qp, String fromUnit, String toUnit) throws SapphireException {
        DataSet ds = qp.getPreparedSqlDataSet("checkunitcompatibility1", "SELECT expression FROM unitconversion WHERE unitsid=? AND tounits=?", new Object[]{fromUnit, toUnit});
        return ds != null && ds.getRowCount() > 0;
    }

    public static String getCompatibleUnits(QueryProcessor qp, String toUnit) throws SapphireException {
        DataSet ds = qp.getPreparedSqlDataSet("compatibleunits", "SELECT unitsid FROM unitconversion WHERE tounits=? ", new Object[]{toUnit});
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getColumnValues("unitsid", ",");
        }
        return "";
    }

    public static void popupulateUnitConversationCache(String connectionid, String databaseid) {
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet ds = qp.getSqlDataSet("SELECT unitsid, tounits, expression FROM unitconversion");
        for (int i = 0; i < ds.size(); ++i) {
            String expression = ds.getValue(i, "expression");
            if (expression.length() <= 0) continue;
            CacheUtil.put(databaseid, "UnitConversion", ds.getString(i, "unitsid") + ";" + ds.getString(i, "tounits"), expression);
        }
    }

    public static void popupulateUnitConversationCache(QueryProcessor qp, String databaseid) {
        DataSet ds = qp.getSqlDataSet("SELECT unitsid, tounits, expression FROM unitconversion");
        for (int i = 0; i < ds.size(); ++i) {
            String expression = ds.getValue(i, "expression");
            if (expression.length() <= 0) continue;
            CacheUtil.put(databaseid, "UnitConversion", ds.getString(i, "unitsid") + ";" + ds.getString(i, "tounits"), expression);
        }
    }

    public static void updateUnitConversationCache(String databaseid, QueryProcessor qp, String unitsid) {
        CacheUtil.removeAllStartWith(databaseid, "UnitConversion", unitsid + ";", false);
        DataSet ds = qp.getPreparedSqlDataSet("SELECT unitsid, tounits, expression FROM unitconversion WHERE unitsid=?", (Object[])new String[]{unitsid});
        for (int i = 0; i < ds.size(); ++i) {
            String expression = ds.getValue(i, "expression");
            if (expression.length() <= 0) continue;
            CacheUtil.put(databaseid, "UnitConversion", unitsid + ";" + ds.getString(i, "tounits"), expression);
        }
    }

    public static BigDecimal basicUnitConv(String databaseid, BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        if (value == null) {
            return null;
        }
        if (fromUnit == null || fromUnit.equals("") || toUnit == null || toUnit.equals("")) {
            throw new SapphireException("Unit conversion failed, From/To unit not supplied!");
        }
        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return value;
        }
        String expression = (String)CacheUtil.get(databaseid, "UnitConversion", fromUnit + ";" + toUnit);
        BigDecimal returnValue = null;
        if (expression != null && expression.length() > 0) {
            HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
            params.put("this", value);
            String returnStr = ExpressionUtil.evaluate(expression, params);
            if (returnStr != null && returnStr.length() > 0) {
                returnValue = new BigDecimal(returnStr);
            } else {
                throw new SapphireException("Unit conversion failed");
            }
        }
        return returnValue;
    }

    public static BigDecimal basicUnitConv(ConnectionInfo connectionInfo, String databaseid, BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        if (value == null) {
            return null;
        }
        if (fromUnit == null || fromUnit.equals("") || toUnit == null || toUnit.equals("")) {
            throw new SapphireException("Unit conversion failed, From/To unit not supplied!");
        }
        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return value;
        }
        String expression = (String)CacheUtil.get(databaseid, "UnitConversion", fromUnit + ";" + toUnit);
        BigDecimal returnValue = null;
        if (expression != null && expression.length() > 0) {
            HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
            params.put("this", value);
            String returnStr = ExpressionUtil.evaluateSecure(connectionInfo, expression, params);
            if (returnStr != null && returnStr.length() > 0) {
                returnValue = FormatUtil.getInstance(connectionInfo).parseBigDecimal(returnStr);
            } else {
                throw new SapphireException("Unit conversion failed");
            }
        }
        return returnValue;
    }

    public static BigDecimal unitConvDataItem(String databaseid, Map dataitem, String toUnit) throws SapphireException {
        BigDecimal value = (BigDecimal)dataitem.get("transformvalue");
        String fromUnit = (String)dataitem.get("displayunits");
        return UnitsUtil.unitConv(databaseid, value, fromUnit, toUnit);
    }

    public static BigDecimal unitConvDataItemWithDensity(String databaseid, Map dataitem, String toUnit, BigDecimal densityValue, String densityUnit) throws SapphireException {
        BigDecimal value = (BigDecimal)dataitem.get("transformvalue");
        String fromUnit = (String)dataitem.get("displayunits");
        return UnitsUtil.unitConvWithDensity(databaseid, value, fromUnit, toUnit, densityValue, densityUnit);
    }

    public static BigDecimal basicUnitConvWithDensity(String databaseid, BigDecimal value, String fromUnit, String toUnit, BigDecimal densityValue, String densityUnit) throws SapphireException {
        BigDecimal returnValue = UnitsUtil.basicUnitConv(databaseid, value, fromUnit, toUnit);
        if (returnValue == null) {
            boolean volUnitSupplied;
            if (densityValue == null || densityUnit == null || densityUnit.equals("")) {
                throw new SapphireException("Unit conversion failed. Density value/unit not supplied!");
            }
            String[] densityUnits = UnitsUtil.splitUnit(densityUnit);
            String densityWtUnit = densityUnits[0];
            String densityVolUnit = densityUnits[1];
            if (densityVolUnit.equals("")) {
                throw new SapphireException("Unit conversion failed. Density volume unit not supplied!");
            }
            String expression = (String)CacheUtil.get(databaseid, "UnitConversion", fromUnit + ";" + densityVolUnit);
            boolean bl = volUnitSupplied = fromUnit.equalsIgnoreCase(densityVolUnit) || expression != null && expression.length() > 0;
            if (volUnitSupplied) {
                if (fromUnit.equalsIgnoreCase(densityVolUnit)) {
                    returnValue = value.multiply(densityValue);
                    if (!toUnit.equalsIgnoreCase(densityWtUnit)) {
                        returnValue = UnitsUtil.basicUnitConv(databaseid, returnValue, densityWtUnit, toUnit);
                    }
                } else {
                    returnValue = UnitsUtil.basicUnitConv(databaseid, value, fromUnit, densityVolUnit);
                    if (returnValue != null) {
                        returnValue = returnValue.multiply(densityValue);
                        if (!toUnit.equalsIgnoreCase(densityWtUnit)) {
                            returnValue = UnitsUtil.basicUnitConv(databaseid, returnValue, densityWtUnit, toUnit);
                        }
                    }
                }
            } else if (fromUnit.equalsIgnoreCase(densityWtUnit)) {
                HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
                params.put("param1", value);
                params.put("param2", densityValue);
                returnValue = UnitsUtil.evaluate("[param1]/[param2]", params);
                if (!toUnit.equalsIgnoreCase(densityVolUnit)) {
                    returnValue = UnitsUtil.basicUnitConv(databaseid, returnValue, densityVolUnit, toUnit);
                }
            } else {
                returnValue = UnitsUtil.basicUnitConv(databaseid, value, fromUnit, densityWtUnit);
                if (returnValue != null) {
                    HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
                    params.put("param1", returnValue);
                    params.put("param2", densityValue);
                    returnValue = UnitsUtil.evaluate("[param1]/[param2]", params);
                    if (!toUnit.equalsIgnoreCase(densityVolUnit)) {
                        returnValue = UnitsUtil.basicUnitConv(databaseid, returnValue, densityVolUnit, toUnit);
                    }
                }
            }
        }
        return returnValue;
    }

    public static BigDecimal evaluate(String expression, HashMap params) throws SapphireException {
        String returnStr = ExpressionUtil.evaluate(expression, params);
        BigDecimal returnValue = null;
        if (returnStr != null && returnStr.length() > 0) {
            returnValue = new BigDecimal(returnStr);
        }
        return returnValue;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static BigDecimal unitConv(String databaseid, BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        BigDecimal returnValue = UnitsUtil.basicUnitConv(databaseid, value, fromUnit, toUnit);
        if (returnValue != null) return returnValue;
        String[] fromUnits = UnitsUtil.splitUnit(fromUnit);
        String[] toUnits = UnitsUtil.splitUnit(toUnit);
        if (!fromUnits[1].equals("") && !toUnits[1].equals("")) {
            if (!fromUnits[0].equalsIgnoreCase(toUnits[0])) throw new SapphireException("Unit conversion failed. Mismatch between From Unit numerator and Target Unit numerator! ");
            BigDecimal factor = UnitsUtil.basicUnitConv(databaseid, new BigDecimal(1), fromUnits[1], toUnits[1]);
            if (factor != null) {
                HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
                params.put("param1", value);
                params.put("param2", factor);
                return UnitsUtil.evaluate("[param1]/[param2]", params);
            }
            factor = UnitsUtil.basicUnitConv(databaseid, new BigDecimal(1), toUnits[1], fromUnits[1]);
            if (factor == null) return returnValue;
            return value.multiply(factor);
        }
        if (!fromUnits[1].equals("")) throw new SapphireException("Unit conversion failed. Invalid From/Target Unit! ");
        if (toUnits[1].equals("")) return returnValue;
        throw new SapphireException("Unit conversion failed. Invalid From/Target Unit! ");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static BigDecimal unitConvWithDensity(String databaseid, BigDecimal value, String fromUnit, String toUnit, BigDecimal densityValue, String densityUnit) throws SapphireException {
        BigDecimal returnValue = UnitsUtil.basicUnitConv(databaseid, value, fromUnit, toUnit);
        if (returnValue != null) return returnValue;
        String[] fromUnits = UnitsUtil.splitUnit(fromUnit);
        String[] toUnits = UnitsUtil.splitUnit(toUnit);
        if (fromUnits[1].equals("") && toUnits[1].equals("")) {
            return UnitsUtil.basicUnitConvWithDensity(databaseid, value, fromUnit, toUnit, densityValue, densityUnit);
        }
        if (fromUnits[1].equals("")) throw new SapphireException("Unit conversion failed. Invalid From/Target Unit! ");
        if (toUnits[1].equals("")) throw new SapphireException("Unit conversion failed. Invalid From/Target Unit! ");
        if (!fromUnits[0].equalsIgnoreCase(toUnits[0])) throw new SapphireException("Unit conversion failed. Mismatch between From Unit numerator and Target Unit numerator! ");
        returnValue = UnitsUtil.unitConv(databaseid, value, fromUnit, toUnit);
        if (returnValue != null) return returnValue;
        returnValue = UnitsUtil.basicUnitConvWithDensity(databaseid, new BigDecimal(1), toUnits[1], fromUnits[1], densityValue, densityUnit);
        if (returnValue != null) {
            return returnValue.multiply(value);
        }
        returnValue = UnitsUtil.basicUnitConvWithDensity(databaseid, new BigDecimal(1), fromUnits[1], toUnits[1], densityValue, densityUnit);
        if (returnValue == null) return returnValue;
        HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
        params.put("param1", returnValue);
        params.put("param2", value);
        ExpressionUtil.evaluate("(1/[param1])*[param2]", params);
        return UnitsUtil.evaluate("[param1]/[param2]", params);
    }

    public static String[] splitUnit(String unit) {
        String[] unitArray = new String[2];
        if (unit.indexOf("/") > -1) {
            unitArray[0] = StringUtil.split(unit, "/")[0].trim();
            unitArray[1] = StringUtil.split(unit, "/")[1].trim();
        } else if (unit.indexOf("\\") > -1) {
            unitArray[0] = StringUtil.split(unit, "\\")[0].trim();
            unitArray[1] = StringUtil.split(unit, "\\")[1].trim();
        } else if (unit.indexOf(" per ") > -1) {
            unitArray[0] = StringUtil.split(unit, "per")[0].trim();
            unitArray[1] = StringUtil.split(unit, "per")[1].trim();
        } else {
            unitArray[0] = unit;
            unitArray[1] = "";
        }
        return unitArray;
    }

    public static String convertToPointSeperated(String value) {
        if (value != null && value.length() > 0) {
            value = StringUtil.replaceAll(value, ",", ".");
        }
        return value;
    }

    public static String convertToLocateSeperated(String value, String decimalSeperator) {
        if (value != null && value.length() > 0) {
            value = StringUtil.replaceAll(value, ",", decimalSeperator);
            value = StringUtil.replaceAll(value, ".", decimalSeperator);
        }
        return value;
    }

    public class ContainerSizeInfo {
        String containertypeid;
        String sizevalue;
        String sizeunits;
    }
}

