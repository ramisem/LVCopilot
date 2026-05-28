/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.reagent;

import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluator;
import com.labvantage.sapphire.util.evaluator.ParseException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIList;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ReagentUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 104537 $";
    public static final String noInputsFound = "No keyid1 found in the request";

    public static DBAccess getReagentTypeDetail(DBAccess database, String reagentTypeid, String reagentTypeVersionid) throws SapphireException {
        if (reagentTypeid.trim().length() > 0 && reagentTypeVersionid.trim().length() > 0) {
            String sqlReagentType = "SELECT * FROM reagenttype WHERE reagenttypeid=? AND reagenttypeversionid=?";
            database.createPreparedResultSet(sqlReagentType, new Object[]{reagentTypeid, reagentTypeVersionid});
            if (!database.getNext()) {
                throw new SapphireException("No Reagent Type is defined");
            }
        }
        return database;
    }

    public static DataSet getReagentTypeDetail(QueryProcessor queryProcessor, String reagentTypeid, String reagentTypeVersionid) throws SapphireException {
        DataSet ds = null;
        if (reagentTypeid.trim().length() > 0 && reagentTypeVersionid.trim().length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT * FROM reagenttype WHERE reagenttypeid=" + safeSQL.addVar(reagentTypeid) + " AND reagenttypeversionid=" + safeSQL.addVar(reagentTypeVersionid) + "";
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds == null || ds.size() == 0) {
                throw new SapphireException("No Reagent Type is defined");
            }
        }
        return ds;
    }

    public static double getConvertedValue(double amount, String unitsid, String tounits, DBAccess database) throws SapphireException {
        return ReagentUtil.getConvertedValue(amount, unitsid, tounits, database, true);
    }

    public static double getConvertedValue(double amount, String unitsid, String tounits, DBAccess database, boolean noException) throws SapphireException {
        double targetvalue = 0.0;
        ExpressionEvaluator expeval = new ExpressionEvaluator(new StringReader(""));
        if (unitsid != null && unitsid.trim().length() > 0 && tounits != null && tounits.trim().length() > 0 && amount > 0.0) {
            String sqlUnit = "SELECT expression FROM unitconversion WHERE unitsid=? AND tounits=?";
            database.createPreparedResultSet(sqlUnit, new Object[]{unitsid, tounits});
            if (database.getNext()) {
                String expression = database.getValue("expression");
                if (expression.trim().length() > 0) {
                    String paramString = expression.substring(expression.indexOf("[") + 1, expression.indexOf("]"));
                    HashMap<String, BigDecimal> values = new HashMap<String, BigDecimal>();
                    values.put(paramString, new BigDecimal(amount));
                    try {
                        targetvalue = Double.parseDouble(expeval.evaluate(expression, values));
                    }
                    catch (ParseException e) {
                        throw new SapphireException("Unit Conversion failed", e);
                    }
                }
            } else if (!(noException || "(Containers)".equalsIgnoreCase(unitsid) || "(Containers)".equalsIgnoreCase(tounits))) {
                throw new SapphireException("Unit Conversion rule not found from " + unitsid + " to " + tounits);
            }
        }
        return targetvalue;
    }

    private static boolean checkQCTransferFlag(String sampleId, QueryProcessor qp) throws SapphireException {
        boolean qcTransferFlagExist = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sampleDISQL = new StringBuffer();
        sampleDISQL.append("SELECT qctransferflag FROM sdidataitem ");
        sampleDISQL.append(" WHERE sdcid='Sample'");
        sampleDISQL.append(" AND keyid1=" + safeSQL.addVar(sampleId));
        sampleDISQL.append(" AND qctransferflag='Y'");
        DataSet sampleDSItems = qp.getPreparedSqlDataSet(sampleDISQL.toString(), safeSQL.getValues());
        if (sampleDSItems != null && sampleDSItems.size() > 0) {
            qcTransferFlagExist = true;
        }
        return qcTransferFlagExist;
    }

    public static void copyMatchingDataItems(String reagentLotId, String sampleId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer rlDataSetSQL = new StringBuffer();
        rlDataSetSQL.append("SELECT sdidata.keyid1, paramid, paramtype, replicateid, sdidata.paramlistid,  ");
        rlDataSetSQL.append(" sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset FROM sdidata, sdidataitem ");
        rlDataSetSQL.append(" WHERE sdidata.sdcid='LV_ReagentLot' ");
        rlDataSetSQL.append(" AND sdidata.keyid1=" + safeSQL.addVar(reagentLotId));
        rlDataSetSQL.append(" AND sdidataitem.paramlistid=sdidata.paramlistid ");
        rlDataSetSQL.append(" AND sdidataitem.sdcid='LV_ReagentLot' ");
        rlDataSetSQL.append(" AND sdidataitem.paramlistversionid = sdidata.paramlistversionid ");
        rlDataSetSQL.append(" AND sdidataitem.variantid = sdidata.variantid ");
        rlDataSetSQL.append(" AND sdidataitem.keyid1=sdidata.keyid1");
        DataSet reagentLotDSItems = qp.getPreparedSqlDataSet(rlDataSetSQL.toString(), safeSQL.getValues());
        if (reagentLotDSItems == null || reagentLotDSItems.getRowCount() == 0) {
            return;
        }
        safeSQL.reset();
        boolean qcTransferFlagExist = ReagentUtil.checkQCTransferFlag(sampleId, qp);
        StringBuffer sampleDataSetSQL = new StringBuffer();
        sampleDataSetSQL.append("SELECT paramid, paramtype, replicateid, paramlistid,");
        sampleDataSetSQL.append(" paramlistversionid, variantid, dataset, enteredtext,displayunits,replicateid FROM sdidataitem sdidi");
        sampleDataSetSQL.append(" WHERE sdcid='Sample'");
        sampleDataSetSQL.append(" AND keyid1=" + safeSQL.addVar(sampleId));
        if (qcTransferFlagExist) {
            sampleDataSetSQL.append(" AND qctransferflag='Y'");
        } else {
            sampleDataSetSQL.append(" AND dataset=(select max(dataset) from sdidata ds");
            sampleDataSetSQL.append(" WHERE ds.sdcid='Sample'");
            sampleDataSetSQL.append(" AND ds.keyid1=" + safeSQL.addVar(sampleId));
            sampleDataSetSQL.append(" AND ds.paramlistid=sdidi.paramlistid");
            sampleDataSetSQL.append(" AND ds.paramlistversionid=sdidi.paramlistversionid");
            sampleDataSetSQL.append(" AND ds.variantid=sdidi.variantid )");
            sampleDataSetSQL.append(" AND replicateid=(select max(replicateid) from sdidataitem di");
            sampleDataSetSQL.append(" WHERE di.sdcid='Sample'");
            sampleDataSetSQL.append(" AND di.keyid1=" + safeSQL.addVar(sampleId));
            sampleDataSetSQL.append(" AND di.paramlistid=sdidi.paramlistid");
            sampleDataSetSQL.append(" AND di.paramlistversionid=sdidi.paramlistversionid");
            sampleDataSetSQL.append(" AND di.variantid=sdidi.variantid");
            sampleDataSetSQL.append(" AND di.paramid=sdidi.paramid");
            sampleDataSetSQL.append(" AND di.paramtype=sdidi.paramtype");
            sampleDataSetSQL.append(" AND di.dataset=sdidi.dataset )");
        }
        DataSet sampleDSItems = qp.getPreparedSqlDataSet(sampleDataSetSQL.toString(), safeSQL.getValues());
        if (sampleDSItems == null || sampleDSItems.getRowCount() == 0) {
            return;
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        DataSet filterDS = new DataSet();
        StringBuffer paramlistid = new StringBuffer();
        StringBuffer paramlistversionid = new StringBuffer();
        StringBuffer variantid = new StringBuffer();
        StringBuffer dataset = new StringBuffer();
        StringBuffer paramid = new StringBuffer();
        StringBuffer paramtype = new StringBuffer();
        StringBuffer replicateid = new StringBuffer();
        StringBuffer enteredtext = new StringBuffer();
        StringBuffer displayunits = new StringBuffer();
        reagentLotDSItems.addColumn("enteredtext", 0);
        reagentLotDSItems.addColumn("displayunits", 0);
        reagentLotDSItems.addColumn("currdataset", 1);
        boolean changed = false;
        for (int rlRow = 0; rlRow < reagentLotDSItems.getRowCount(); ++rlRow) {
            filterMap.put("paramid", reagentLotDSItems.getString(rlRow, "paramid"));
            if (!qcTransferFlagExist) {
                filterMap.put("paramtype", reagentLotDSItems.getString(rlRow, "paramtype"));
            }
            if ((filterDS = sampleDSItems.getFilteredDataSet(filterMap)).getRowCount() > 0) {
                paramlistid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistid"));
                paramlistversionid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistversionid"));
                variantid.append(";").append(reagentLotDSItems.getValue(rlRow, "variantid"));
                dataset.append(";").append(reagentLotDSItems.getValue(rlRow, "dataset"));
                paramid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramid"));
                paramtype.append(";").append(reagentLotDSItems.getValue(rlRow, "paramtype"));
                replicateid.append(";").append(reagentLotDSItems.getValue(rlRow, "replicateid"));
                enteredtext.append(";").append(filterDS.getValue(0, "enteredtext"));
                displayunits.append(";").append(filterDS.getValue(0, "displayunits"));
                changed = true;
            }
            filterMap.clear();
            filterDS.clear();
        }
        if (changed) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_ReagentLot");
            actionProps.put("keyid1", reagentLotId);
            actionProps.put("paramlistid", paramlistid.substring(1));
            actionProps.put("paramlistversionid", paramlistversionid.substring(1));
            actionProps.put("variantid", variantid.substring(1));
            actionProps.put("dataset", dataset.substring(1));
            actionProps.put("paramid", paramid.substring(1));
            actionProps.put("paramtype", paramtype.substring(1));
            actionProps.put("replicateid", replicateid.substring(1));
            actionProps.put("displayunits", displayunits.substring(1));
            actionProps.put("applylock", "Y");
            ap.processAction("EditDataItem", "1", actionProps);
            actionProps.remove("displayunits");
            actionProps.put("enteredtext", enteredtext.substring(1));
            ap.processAction("EnterDataItem", "1", actionProps);
        }
    }

    public static void populateMatchingDataItemsValue(DataSet paramsDS, String reagentLotId, QueryProcessor qp) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sampleId = "";
        StringBuffer qsSql = new StringBuffer();
        qsSql.append("SELECT s_sampleid from s_sample where reagentlotid=" + safeSQL.addVar(reagentLotId) + " order by createdt desc ");
        DataSet qsDS = qp.getPreparedSqlDataSet(qsSql.toString(), safeSQL.getValues());
        if (qsDS == null || qsDS.size() <= 0) {
            return;
        }
        sampleId = qsDS.getString(0, "s_sampleid", "");
        safeSQL.reset();
        boolean qcTransferFlagExist = ReagentUtil.checkQCTransferFlag(sampleId, qp);
        StringBuffer sampleDataSetSQL = new StringBuffer();
        sampleDataSetSQL.append("SELECT paramid, paramtype, replicateid, paramlistid,");
        sampleDataSetSQL.append(" paramlistversionid, variantid, dataset, displayvalue,displayunits,replicateid FROM sdidataitem sdidi");
        sampleDataSetSQL.append(" WHERE sdcid='Sample'");
        sampleDataSetSQL.append(" AND keyid1=" + safeSQL.addVar(sampleId));
        if (qcTransferFlagExist) {
            sampleDataSetSQL.append(" AND qctransferflag='Y'");
        } else {
            sampleDataSetSQL.append(" AND dataset=(select max(dataset) from sdidata ds");
            sampleDataSetSQL.append(" WHERE ds.sdcid='Sample'");
            sampleDataSetSQL.append(" AND ds.keyid1=" + safeSQL.addVar(sampleId));
            sampleDataSetSQL.append(" AND ds.paramlistid=sdidi.paramlistid");
            sampleDataSetSQL.append(" AND ds.paramlistversionid=sdidi.paramlistversionid");
            sampleDataSetSQL.append(" AND ds.variantid=sdidi.variantid )");
            sampleDataSetSQL.append(" AND replicateid=(select max(replicateid) from sdidataitem di");
            sampleDataSetSQL.append(" WHERE di.sdcid='Sample'");
            sampleDataSetSQL.append(" AND di.keyid1=" + safeSQL.addVar(sampleId));
            sampleDataSetSQL.append(" AND di.paramlistid=sdidi.paramlistid");
            sampleDataSetSQL.append(" AND di.paramlistversionid=sdidi.paramlistversionid");
            sampleDataSetSQL.append(" AND di.variantid=sdidi.variantid");
            sampleDataSetSQL.append(" AND di.paramid=sdidi.paramid");
            sampleDataSetSQL.append(" AND di.paramtype=sdidi.paramtype");
            sampleDataSetSQL.append(" AND di.dataset=sdidi.dataset )");
        }
        DataSet sampleDSItems = qp.getPreparedSqlDataSet(sampleDataSetSQL.toString(), safeSQL.getValues());
        if (sampleDSItems == null || sampleDSItems.getRowCount() == 0) {
            return;
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        DataSet filterDS = new DataSet();
        for (int rlRow = 0; rlRow < paramsDS.getRowCount(); ++rlRow) {
            String displayValue = paramsDS.getString(rlRow, "displayvalue", "");
            if (displayValue.length() != 0) continue;
            filterMap.put("paramid", paramsDS.getString(rlRow, "paramid"));
            if (!qcTransferFlagExist) {
                filterMap.put("paramtype", paramsDS.getString(rlRow, "paramtype"));
            }
            if ((filterDS = sampleDSItems.getFilteredDataSet(filterMap)).getRowCount() > 0) {
                paramsDS.setString(rlRow, "displayvalue", filterDS.getValue(0, "displayvalue", ""));
                paramsDS.setString(rlRow, "displayunits", filterDS.getValue(0, "displayunits", ""));
            }
            filterMap.clear();
            filterDS.clear();
        }
    }

    public static void setDefaultUsedAmountUnit(DataSet ds, QueryProcessor queryProcessor) {
        for (int i = 0; i < ds.size(); ++i) {
            String amountunits = ds.getString(i, "amountunits", "");
            String amountunitstype = ds.getString(i, "amountunitstype", "");
            if (amountunits.length() != 0 || amountunitstype.length() != 0) continue;
            String defaultUnit = ds.getString(i, "amountrecommendedunits", "");
            String defaultUnitType = ds.getString(i, "amountrecommendedunitstype", "");
            if (defaultUnit.length() == 0 && defaultUnitType.length() == 0) {
                String trackitemid = ds.getString(i, "trackitemid", "");
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT sizeunits from trackitem,containertype");
                sql.append(" where trackitemid=").append(safeSQL.addVar(trackitemid));
                sql.append("  and trackitem.containertypeid=containertype.containertypeid");
                DataSet trackitems = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (trackitems != null && trackitems.size() > 0) {
                    defaultUnit = trackitems.getString(0, "sizeunits", "");
                    defaultUnitType = "U";
                }
            }
            ds.setString(i, "amountunits", defaultUnit);
            ds.setString(i, "amountunitstype", defaultUnitType);
        }
    }

    public static void setDefaultUsedAmountUnit(DataSet ds, int i, QueryProcessor queryProcessor) {
        if (ds.getValue(i, "amount", "").length() > 0) {
            String amountunits = ds.getString(i, "amountunits", "");
            String amountunitstype = ds.getString(i, "amountunitstype", "");
            if (amountunits.length() == 0 && amountunitstype.length() == 0) {
                String defaultUnit = ds.getString(i, "amountrecommendedunits", "");
                String defaultUnitType = ds.getString(i, "amountrecommendedunitstype", "");
                if (defaultUnit.length() == 0 && defaultUnitType.length() == 0) {
                    String trackitemid = ds.getString(i, "trackitemid", "");
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer sql = new StringBuffer();
                    sql.append("SELECT sizeunits from trackitem,containertype");
                    sql.append(" where trackitemid=").append(safeSQL.addVar(trackitemid));
                    sql.append("  and trackitem.containertypeid=containertype.containertypeid");
                    DataSet trackitems = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (trackitems != null && trackitems.size() > 0) {
                        defaultUnit = trackitems.getString(0, "sizeunits", "");
                        defaultUnitType = "U";
                    }
                }
                ds.setString(i, "amountunits", defaultUnit);
                ds.setString(i, "amountunitstype", defaultUnitType);
            }
        }
    }

    public static int getDecimalScale(String qty, char decimalSeperator) {
        return ReagentUtil.getDecimalScale(qty, decimalSeperator, 0);
    }

    public static int getDecimalScale(String qty, char decimalSeperator, int defaultDecimal) {
        int decimal = defaultDecimal;
        qty = qty.replace(decimalSeperator, '.');
        int decimalPositionIndx = (qty = ReagentUtil.convertFromScientificNotation(qty)).indexOf(46);
        if (decimalPositionIndx >= 0) {
            decimal = qty.substring(decimalPositionIndx + 1).length();
        }
        return decimal;
    }

    public static String convertFromScientificNotation(String number) {
        if (number.length() > 0 && number.toLowerCase().contains("e")) {
            DecimalFormat formatter = new DecimalFormat();
            ((NumberFormat)formatter).setMaximumFractionDigits(25);
            return formatter.format(Double.parseDouble(number));
        }
        return number;
    }

    public static String removeLastZerosAferDecimal(double amount, char decimalSeperator) {
        return ReagentUtil.removeLastZerosAferDecimal(Double.toString(amount), decimalSeperator);
    }

    public static String removeLastZerosAferDecimal(String amountTextStr, char decimalSeperator) {
        String temp = amountTextStr;
        if (amountTextStr.indexOf(46) >= 0 || amountTextStr.indexOf(decimalSeperator) >= 0) {
            for (int i = amountTextStr.length(); i > 1; --i) {
                if (amountTextStr.charAt(i - 1) != '0') {
                    if (amountTextStr.charAt(i - 1) != '.' && amountTextStr.charAt(i - 1) != decimalSeperator) break;
                    temp = temp.substring(0, temp.length() - 1);
                    break;
                }
                temp = temp.substring(0, temp.length() - 1);
            }
        }
        return temp;
    }

    public static int getMaxScale(String amountStr, char decimalSeperator) {
        return ReagentUtil.getMaxScale(amountStr, decimalSeperator, 0);
    }

    public static int getMaxScale(String amountStr, char decimalSeperator, int defaultScale) {
        int maxScale = 0;
        if (amountStr.contains(";")) {
            String[] amountStrArr;
            for (String valueStr : amountStrArr = StringUtil.split(amountStr, ";")) {
                int scale = ReagentUtil.getDecimalScale(valueStr, decimalSeperator);
                if (maxScale >= scale) continue;
                maxScale = scale;
            }
        } else {
            amountStr = amountStr.replace(decimalSeperator, '.');
            maxScale = ReagentUtil.getDecimalScale(amountStr, decimalSeperator);
        }
        return maxScale > 0 ? maxScale : defaultScale;
    }

    public static String getTotalValue(QueryProcessor qp, FormatUtil formatUtil, String amounts, String units, String unitstype, String containerSize, String containerUnits) throws Exception {
        double totalAmount = 0.0;
        String[] amountsArr = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(amounts, ";");
        String[] unitsArr = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(units, ";");
        String[] unitstypeArr = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(unitstype, ";");
        String amount = "";
        String unit = "";
        String unitType = "";
        String[] preferUnitAndType = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(ReagentUtil.getPreferUnitAndType(units, unitstype), ";");
        String preferUnit = preferUnitAndType[0];
        String preferType = preferUnitAndType[1];
        for (int i = 0; i < amountsArr.length; ++i) {
            amount = amountsArr[i];
            unit = unitsArr[i];
            String string = i < unitstypeArr.length ? unitstypeArr[i] : (unitType = unit.length() > 0 ? "U" : "");
            if (amount == null || amount.length() <= 0) continue;
            if (!unit.equalsIgnoreCase(preferUnit)) {
                if (unitType.equalsIgnoreCase("C")) {
                    double newValue = UnitsUtil.convertFromContainersToUnits(qp, containerSize, containerUnits, amount, preferUnit);
                    amount = Double.toString(newValue);
                } else {
                    amount = UnitsUtil.getConvertedValue(qp, unit, preferUnit, amount.replace(formatUtil.getDecimalSeparator(), '.'));
                }
            }
            amount = UnitsUtil.convertToLocateSeperated(amount, "" + formatUtil.getDecimalSeparator());
            totalAmount += formatUtil.parseBigDecimal(amount).doubleValue();
        }
        return String.valueOf(totalAmount) + ";" + preferUnit + ";" + preferType;
    }

    public static String getPreferUnitAndType(String units, String unitstype) {
        String unit = units;
        String unittype = unitstype;
        if (units.contains(";")) {
            String[] unitsArr = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(units, ";");
            String[] unitstypeArr = com.labvantage.sapphire.gwt.shared.util.StringUtil.split(unitstype, ";");
            unit = unitsArr[0];
            unittype = unitstypeArr[0];
            for (int i = 0; i < unitstypeArr.length; ++i) {
                if (unitstypeArr[i].equalsIgnoreCase("C")) continue;
                unit = unitsArr[i];
                unittype = unitstypeArr[i];
                break;
            }
        }
        return unit + ";" + unittype;
    }

    public static void updateUsedDetailsOfUnManagedTrackItemInv(String trackitemids, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select distinct trackitem.trackitemid,trackitem.usecount,trackitem.firstusedt from trackitem,reagentlot ");
        sql.append(" where trackitemid in (" + safeSQL.addIn(trackitemids, ";") + ") ");
        sql.append(" and trackitem.linksdcid='LV_ReagentLot' ");
        sql.append(" and reagentlot.reagentlotid=trackitem.linkkeyid1 ");
        sql.append(" and (reagentlot.managecontainerinventoryflag='N' or reagentlot.managecontainerinventoryflag='' or reagentlot.managecontainerinventoryflag is null) ");
        DataSet unManageTIEnvds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (unManageTIEnvds != null && unManageTIEnvds.getRowCount() > 0) {
            StringBuffer unmanagedTI = new StringBuffer();
            StringBuffer firstusedt = new StringBuffer();
            StringBuffer usecount = new StringBuffer();
            for (int i = 0; i < unManageTIEnvds.getRowCount(); ++i) {
                unmanagedTI.append(";").append(unManageTIEnvds.getString(i, "trackitemid"));
                firstusedt.append(";").append(unManageTIEnvds.getValue(i, "firstusedt", "").length() > 0 ? unManageTIEnvds.getValue(i, "firstusedt", "") : "n");
                usecount.append(";").append(unManageTIEnvds.getInt(i, "usecount", 0) + 1);
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", unmanagedTI.substring(1));
            props.setProperty("firstusedt", firstusedt.substring(1));
            props.setProperty("usecount", usecount.substring(1));
            ap.processAction("EditSDI", "1", props);
        }
    }

    public static void updateTrackItemInventory(String oldtrackitemid, String oldamount, String oldamountunits, String oldamountunitstype, String newtrackitemid, String amount, String amountunits, String amountunitstype, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        PropertyList props = new PropertyList();
        amount = amount.length() > 0 ? amount : "0";
        oldamount = oldamount.length() > 0 ? oldamount : "0";
        StringBuffer trackitemids = new StringBuffer();
        if (oldtrackitemid.length() > 0 && newtrackitemid.length() > 0) {
            String ti = oldtrackitemid + ";" + newtrackitemid;
            String amt = oldamount + ";-" + amount;
            String amtUnits = oldamountunits + ";" + amountunits;
            String amtUnitType = oldamountunitstype + ";" + amountunitstype;
            props = ReagentUtil.getTrackItemProps(ti, amt, amtUnits, amtUnitType);
            if (!oldtrackitemid.equalsIgnoreCase(newtrackitemid)) {
                trackitemids.append(";").append(newtrackitemid);
            }
        } else if (oldtrackitemid.length() > 0) {
            props = ReagentUtil.getTrackItemProps(oldtrackitemid, oldamount, oldamountunits, oldamountunitstype);
        } else if (newtrackitemid.length() > 0) {
            props = ReagentUtil.getTrackItemProps(newtrackitemid, "-" + amount, amountunits, amountunitstype);
            trackitemids.append(";").append(newtrackitemid);
        }
        if (props.size() > 0) {
            ap.processAction("AdjustTrackItemInv", "1", props);
        }
        if (trackitemids.length() > 0) {
            ReagentUtil.updateUsedDetailsOfUnManagedTrackItemInv(trackitemids.substring(1), qp, ap);
        }
    }

    public static PropertyList getTrackItemProps(String trackitemid, String amount, String amountunits, String amountunitstype) {
        PropertyList props = new PropertyList();
        props.setProperty("trackitemid", trackitemid);
        props.setProperty("quantity", amount);
        props.setProperty("quantityunit", amountunits);
        props.setProperty("quantitytype", amountunitstype);
        return props;
    }

    public static boolean hasDepartmentalSecurityAccess(String sdcid, String keyid1, String keyid2, String keyid3, SDCProcessor sdcProcessor, String connectionid) {
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(sdcProcessor.getProperty(sdcid, "accesscontrolledflag"));
        boolean hasAccess = true;
        try {
            if (deptSecurityEnabled) {
                DAMProcessor damProcessor = new DAMProcessor(connectionid);
                SDIList sdiList = damProcessor.checkSDIAccess(sdcid, keyid1, keyid2, keyid3, true, "edit");
                DataSet accessibleSDIs = sdiList.toDataSet();
                boolean bl = hasAccess = accessibleSDIs.size() > 0;
                if (!hasAccess) {
                    sdiList = damProcessor.checkSDIAccess(sdcid, keyid1, keyid2, keyid3, true, "list");
                    accessibleSDIs = sdiList.toDataSet();
                    hasAccess = accessibleSDIs.size() > 0;
                }
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return hasAccess;
    }

    public static String getMaxRecipeItemId(String keyid1, String recipeitemtype, QueryProcessor queryProcessor) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select reagentlotrecipeitemid from reagentlotrecipe");
        sql.append(" WHERE reagentlotid = " + safeSQL.addVar(keyid1));
        sql.append(" AND recipeitemtype = " + safeSQL.addVar(recipeitemtype));
        int curr = 0;
        int max = 0;
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                curr = Integer.parseInt(ds.getString(i, "reagentlotrecipeitemid", "").substring(2));
                max = Math.max(curr, max);
            }
        }
        return (recipeitemtype.equalsIgnoreCase("Reagent") ? "R-" : "I-") + (max + 1);
    }

    public static boolean isInstrumentExist(String keyid1, String type, String model, QueryProcessor queryProcessor) {
        DataSet ds;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select reagentlotrecipeitemid from reagentlotrecipe");
        sql.append(" WHERE reagentlotid = " + safeSQL.addVar(keyid1));
        sql.append(" AND instrumenttype = " + safeSQL.addVar(type));
        if (model.trim().length() > 0) {
            sql.append(" AND instrumentmodelid = " + safeSQL.addVar(model));
        }
        return (ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.getRowCount() > 0;
    }

    public static String getUsedExpiredConsumableFlag(String trackitemid, QueryProcessor queryProcessor) {
        String usedexpiredconsumableflags = "N";
        if (trackitemid.trim().length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT trackitemid,expirydt tiexpirydate,trackitemstatus,(select reagentstatus from reagentlot where reagentlotid=trackitem.linkkeyid1) reagentstatus,(select expirydt from reagentlot where reagentlotid=trackitem.linkkeyid1) lotexpirydt FROM trackitem WHERE trackitemid in (" + safeSQL.addIn(ReagentUtil.getUniqueValues(trackitemid, ";")) + ")";
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (!ds.isEmpty()) {
                String[] trackItemArr = StringUtil.split(trackitemid, ";");
                boolean firtsItem = true;
                for (String s : trackItemArr) {
                    String flag = "N";
                    int findRow = ds.findRow("trackitemid", s);
                    if (findRow > -1) {
                        String trackitemstatus = ds.getString(findRow, "trackitemstatus", "");
                        Timestamp trackitemExpiredt = ds.getTimestamp(findRow, "tiexpirydate");
                        String reagentstatus = ds.getString(findRow, "reagentstatus", "");
                        Timestamp reagentExpiredt = ds.getTimestamp(findRow, "lotexpirydt");
                        Calendar trackitemExpireDate = Calendar.getInstance();
                        Calendar reagentExpireDate = Calendar.getInstance();
                        Calendar todayDate = Calendar.getInstance();
                        if (trackitemExpiredt != null) {
                            trackitemExpireDate.setTime(trackitemExpiredt);
                        }
                        if (reagentExpiredt != null) {
                            reagentExpireDate.setTime(reagentExpiredt);
                        }
                        todayDate.setTime(new Date());
                        if (trackitemExpiredt != null && todayDate.after(trackitemExpireDate) || "Expired".equalsIgnoreCase(trackitemstatus) || reagentExpiredt != null && todayDate.after(reagentExpireDate) || "Expired".equalsIgnoreCase(reagentstatus)) {
                            flag = "Y";
                        }
                    }
                    usedexpiredconsumableflags = firtsItem ? flag : usedexpiredconsumableflags + ";" + flag;
                    firtsItem = false;
                }
            }
        }
        return usedexpiredconsumableflags;
    }

    public static boolean considerDeptForThresholdNotification(ConfigurationProcessor cp, SDCProcessor sdcProcessor) {
        boolean considerDept = false;
        try {
            if ("D".equalsIgnoreCase(sdcProcessor.getProperty("LV_ReagentLot", "accesscontrolledflag"))) {
                PropertyList policy = cp.getPolicy("ConsumablePolicy", "Sapphire Custom");
                considerDept = policy != null && policy.getProperty("considerdeptforthresholdnotification", "N").equalsIgnoreCase("Y");
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return considerDept;
    }

    public static boolean considerVersionForThresholdNotification(ConfigurationProcessor cp) {
        boolean considerVersion = false;
        try {
            PropertyList policy = cp.getPolicy("ConsumablePolicy", "Sapphire Custom");
            considerVersion = policy != null && policy.getProperty("considerversionforthresholdnotification", "N").equalsIgnoreCase("Y");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return considerVersion;
    }

    public static DataSet getDSForTotalQuantityPerType(ConfigurationProcessor cp, SDCProcessor sdcProcessor, QueryProcessor qp, String reagenttypeid, String reagenttypeversionid, String departmentid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT trackitemid, qtycurrent, qtyunits, qtycurrenttype,");
        sql.append(" trackitem.containertypeid, sizevalue,sizeunits FROM trackitem ");
        sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
        sql.append(" WHERE trackitem.linksdcid = 'LV_ReagentLot'");
        sql.append(" AND trackitemstatus = 'Valid' ");
        sql.append(" AND trackitem.linkkeyid1 in (");
        sql.append(" SELECT reagentlotid FROM reagentlot ");
        sql.append(" WHERE reagenttypeid = ").append(safeSQL.addVar(reagenttypeid));
        if (ReagentUtil.considerVersionForThresholdNotification(cp) && reagenttypeversionid.trim().length() > 0) {
            sql.append(" AND reagenttypeversionid = ").append(safeSQL.addVar(reagenttypeversionid));
        }
        if (ReagentUtil.considerDeptForThresholdNotification(cp, sdcProcessor) && departmentid.trim().length() > 0) {
            sql.append(" AND (securitydepartment in (").append(safeSQL.addIn(departmentid, ";")).append(")").append(" OR securitydepartment IS NULL)");
        }
        sql.append(" AND reagentstatus = 'Active')");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static DataSet getDSForTotalQuantityPerLot(ConfigurationProcessor cp, SDCProcessor sdcProcessor, QueryProcessor qp, String reagentlotid, String departmentid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT trackitemid, qtycurrent, qtyunits, qtycurrenttype,");
        sql.append("trackitem.containertypeid, sizevalue,sizeunits FROM trackitem ");
        sql.append(" LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid ");
        sql.append(" WHERE trackitem.linksdcid = 'LV_ReagentLot'");
        sql.append(" AND trackitemstatus = 'Valid' ");
        if (ReagentUtil.considerDeptForThresholdNotification(cp, sdcProcessor) && departmentid.trim().length() > 0) {
            sql.append(" AND trackitem.linkkeyid1 in ( ");
            sql.append(" SELECT reagentlotid FROM reagentlot ");
            sql.append(" WHERE reagentlotid = ").append(safeSQL.addVar(reagentlotid));
            sql.append(" and (securitydepartment in(").append(safeSQL.addIn(departmentid, ";")).append(")").append(" or securitydepartment IS NULL)");
            sql.append(" )");
        } else {
            sql.append(" AND trackitem.linkkeyid1= ").append(safeSQL.addVar(reagentlotid));
        }
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static boolean isInputEmpty(String input) {
        if (input == null || input.trim().length() == 0) {
            return true;
        }
        String uniqueInput = ReagentUtil.getUniqueValues(input, ";");
        return uniqueInput.length() == 0;
    }

    public static boolean isNotInputEmpty(String input) {
        return !ReagentUtil.isInputEmpty(input);
    }

    public static String getUniqueValues(String values, String delimeter) {
        return ReagentUtil.getUniqueValues(values, delimeter, "','");
    }

    public static String getUniqueValues(String values, String delimeter, String retrunValueDelimeter) {
        HashMap<String, String> hm = new HashMap<String, String>();
        if (values != null && values.trim().length() > 0 && values.contains(delimeter)) {
            String[] valuesArr;
            for (String v : valuesArr = StringUtil.split(values, delimeter)) {
                if (v == null || v.trim().length() <= 0) continue;
                hm.put(v, "");
            }
            values = hm.size() > 0 ? String.join((CharSequence)retrunValueDelimeter, hm.keySet()) : "";
        }
        return values;
    }
}

