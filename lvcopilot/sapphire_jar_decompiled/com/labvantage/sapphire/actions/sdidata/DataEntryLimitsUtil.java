/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DataEntryLimitsUtil {
    private ConnectionInfo connectionInfo;
    private TranslationProcessor translationProcessor;
    private DBAccess database;
    private Logger logger;
    private SDCProcessor sdcProcessor;
    public static final String NULL = "(null)";

    public DataEntryLimitsUtil(SDCProcessor sdcProcessor, TranslationProcessor translationProcessor, DBAccess database, Logger logger, ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.translationProcessor = translationProcessor;
        this.database = database;
        this.logger = logger;
        this.sdcProcessor = sdcProcessor;
    }

    public void manipulateLimits(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.manipulateLimits("paramlimits", sdiData, actionProps);
    }

    public void manipulateLimits(String tablename, SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet data;
        if (actionProps.containsKey("value1") && actionProps.containsKey("value2") && (data = sdiData.getDataset(tablename)) != null) {
            if (!data.isValidColumn("value1num")) {
                data.addColumn("value1num", 1);
            }
            if (!data.isValidColumn("value2num")) {
                data.addColumn("value2num", 1);
            }
            try {
                for (int row = 0; row < data.getRowCount(); ++row) {
                    String operator1 = "";
                    String dataType = data.getValue(row, "datatypes");
                    if (data.isValidColumn("operator")) {
                        operator1 = data.getValue(row, "operator", "");
                    } else if (data.isValidColumn("operator1")) {
                        operator1 = data.getValue(row, "operator1", "");
                    }
                    if (operator1.length() <= 0) continue;
                    if (!(operator1.equalsIgnoreCase("Not In") || operator1.equalsIgnoreCase("In") || operator1.equalsIgnoreCase("NIN") || dataType.equalsIgnoreCase("D") || dataType.equalsIgnoreCase("DC") || dataType.equalsIgnoreCase("O") || dataType.equalsIgnoreCase("OC"))) {
                        if (operator1.equals("=")) {
                            try {
                                FormatUtil formatUtil = FormatUtil.getInstance(this.connectionInfo);
                                formatUtil.parseBigDecimal(data.getValue(row, "value1", ""));
                                DataEntryLimitsUtil.processLimitValue(data.getValue(row, "value1", ""), row, data, 1, this.connectionInfo);
                                DataEntryLimitsUtil.processLimitValue(data.getValue(row, "value2", ""), row, data, 2, this.connectionInfo);
                            }
                            catch (Exception e) {
                                data.setValue(row, "value1num", "");
                                data.setValue(row, "value2num", "");
                            }
                            continue;
                        }
                        DataEntryLimitsUtil.processLimitValue(data.getValue(row, "value1", ""), row, data, 1, this.connectionInfo);
                        DataEntryLimitsUtil.processLimitValue(data.getValue(row, "value2", ""), row, data, 2, this.connectionInfo);
                        continue;
                    }
                    data.setValue(row, "value1num", "");
                    data.setValue(row, "value2num", "");
                }
            }
            catch (Exception e) {
                throw new SapphireException(this.translationProcessor.translate(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId()))), e);
            }
        }
    }

    public String getLimitValue(String datatype, String value, String valuenum) {
        if (datatype.equalsIgnoreCase("N") || datatype.equalsIgnoreCase("NC") || datatype.equalsIgnoreCase("A")) {
            if (valuenum.length() > 0 && !valuenum.equalsIgnoreCase(NULL)) {
                return valuenum;
            }
            return value;
        }
        return value;
    }

    public boolean isLimitUndetermined(Object thevalue, String theoperator, String stringvalue1, String stringvalue2, String datatype, String enteredOperator) throws SapphireException {
        boolean undeterminedLimit;
        block10: {
            BigDecimal value1;
            BigDecimal value;
            FormatUtil formatUtil;
            block37: {
                BigDecimal value2;
                block41: {
                    block40: {
                        block39: {
                            block38: {
                                boolean value2null;
                                boolean value1null;
                                block32: {
                                    block36: {
                                        block35: {
                                            block34: {
                                                block33: {
                                                    block27: {
                                                        block31: {
                                                            block30: {
                                                                block29: {
                                                                    block28: {
                                                                        block22: {
                                                                            block26: {
                                                                                block25: {
                                                                                    block24: {
                                                                                        block23: {
                                                                                            block21: {
                                                                                                block20: {
                                                                                                    block19: {
                                                                                                        block18: {
                                                                                                            block17: {
                                                                                                                block16: {
                                                                                                                    block15: {
                                                                                                                        block14: {
                                                                                                                            block13: {
                                                                                                                                block12: {
                                                                                                                                    block11: {
                                                                                                                                        undeterminedLimit = false;
                                                                                                                                        if (enteredOperator == null || enteredOperator.length() <= 0 || thevalue == null || theoperator == null || !datatype.equals("N") && !datatype.equals("NC")) break block10;
                                                                                                                                        formatUtil = FormatUtil.getInstance();
                                                                                                                                        value = (BigDecimal)thevalue;
                                                                                                                                        value1 = null;
                                                                                                                                        value2 = null;
                                                                                                                                        try {
                                                                                                                                            value1 = formatUtil.parseBigDecimal(stringvalue1);
                                                                                                                                        }
                                                                                                                                        catch (Exception exception) {
                                                                                                                                            // empty catch block
                                                                                                                                        }
                                                                                                                                        try {
                                                                                                                                            value2 = formatUtil.parseBigDecimal(stringvalue2);
                                                                                                                                        }
                                                                                                                                        catch (Exception exception) {
                                                                                                                                            // empty catch block
                                                                                                                                        }
                                                                                                                                        value1null = value1 == null;
                                                                                                                                        boolean bl = value2null = value2 == null;
                                                                                                                                        if (!theoperator.equals("=") || !enteredOperator.equals(">=") && !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 0) break block11;
                                                                                                                                        undeterminedLimit = true;
                                                                                                                                        break block10;
                                                                                                                                    }
                                                                                                                                    if (!theoperator.equals("<>") && !theoperator.equals("=") || !enteredOperator.equals(">") && !enteredOperator.equals(">=") || value1null || value.compareTo(value1) != -1) break block12;
                                                                                                                                    undeterminedLimit = true;
                                                                                                                                    break block10;
                                                                                                                                }
                                                                                                                                if (!theoperator.equals("<>") && !theoperator.equals("=") || !enteredOperator.equals("<") && !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 1) break block13;
                                                                                                                                undeterminedLimit = true;
                                                                                                                                break block10;
                                                                                                                            }
                                                                                                                            if (!theoperator.equals(">") && !theoperator.equals(">=") || !enteredOperator.equals("<") && !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 1) break block14;
                                                                                                                            undeterminedLimit = true;
                                                                                                                            break block10;
                                                                                                                        }
                                                                                                                        if (!theoperator.equals(">=") || !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 0) break block15;
                                                                                                                        undeterminedLimit = true;
                                                                                                                        break block10;
                                                                                                                    }
                                                                                                                    if (!theoperator.equals("<=") || !enteredOperator.equals(">=") || value1null || value.compareTo(value1) != 0) break block16;
                                                                                                                    undeterminedLimit = true;
                                                                                                                    break block10;
                                                                                                                }
                                                                                                                if (!theoperator.equals("<") && !theoperator.equals("<=") || !enteredOperator.equals(">") && !enteredOperator.equals(">=") || value1null || value.compareTo(value1) != -1) break block17;
                                                                                                                undeterminedLimit = true;
                                                                                                                break block10;
                                                                                                            }
                                                                                                            if (!theoperator.equals("<") && !theoperator.equals("<=") || !enteredOperator.equals("<") && !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 1) break block18;
                                                                                                            undeterminedLimit = true;
                                                                                                            break block10;
                                                                                                        }
                                                                                                        if (!theoperator.equals("<") || !enteredOperator.equals("<=") || value1null || value.compareTo(value1) != 0) break block19;
                                                                                                        undeterminedLimit = true;
                                                                                                        break block10;
                                                                                                    }
                                                                                                    if (!theoperator.equals(">") || !enteredOperator.equals(">=") || value1null || value.compareTo(value1) != 0) break block20;
                                                                                                    undeterminedLimit = true;
                                                                                                    break block10;
                                                                                                }
                                                                                                if (!theoperator.equals(">") && !theoperator.equals(">=") || !enteredOperator.equals(">") && !enteredOperator.equals(">=") || value1null || value.compareTo(value1) != -1) break block21;
                                                                                                undeterminedLimit = true;
                                                                                                break block10;
                                                                                            }
                                                                                            if (!theoperator.equals("Exclusive Outside") && !theoperator.equals("EO") || value1null || value2null) break block22;
                                                                                            if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") && !enteredOperator.equals("<=") || value.compareTo(value1) != 0) break block23;
                                                                                            undeterminedLimit = true;
                                                                                            break block10;
                                                                                        }
                                                                                        if (!enteredOperator.equals(">=") && !enteredOperator.equals("<") && !enteredOperator.equals("<=") || value.compareTo(value2) != 0) break block24;
                                                                                        undeterminedLimit = true;
                                                                                        break block10;
                                                                                    }
                                                                                    if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) > 0) break block25;
                                                                                    undeterminedLimit = true;
                                                                                    break block10;
                                                                                }
                                                                                if (!enteredOperator.equals("<=") && !enteredOperator.equals("<") || value.compareTo(value2) < 0) break block26;
                                                                                undeterminedLimit = true;
                                                                                break block10;
                                                                            }
                                                                            if (value.compareTo(value1) != 1 || value.compareTo(value2) != -1) break block10;
                                                                            undeterminedLimit = true;
                                                                            break block10;
                                                                        }
                                                                        if (!theoperator.equals("Inclusive Outside") && !theoperator.equals("IO") || value1null || value2null) break block27;
                                                                        if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) != 0) break block28;
                                                                        undeterminedLimit = true;
                                                                        break block10;
                                                                    }
                                                                    if (!enteredOperator.equals("<") && !enteredOperator.equals("<=") || value.compareTo(value2) != 0) break block29;
                                                                    undeterminedLimit = true;
                                                                    break block10;
                                                                }
                                                                if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) > 0) break block30;
                                                                undeterminedLimit = true;
                                                                break block10;
                                                            }
                                                            if (!enteredOperator.equals("<=") && !enteredOperator.equals("<") || value.compareTo(value2) < 0) break block31;
                                                            undeterminedLimit = true;
                                                            break block10;
                                                        }
                                                        if (value.compareTo(value1) != 1 || value.compareTo(value2) != -1) break block10;
                                                        undeterminedLimit = true;
                                                        break block10;
                                                    }
                                                    if (!theoperator.equals("Exclusive Between") && !theoperator.equals("EB") || value1null || value2null) break block32;
                                                    if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) != 0) break block33;
                                                    undeterminedLimit = true;
                                                    break block10;
                                                }
                                                if (!enteredOperator.equals("<") && !enteredOperator.equals("<=") || value.compareTo(value2) != 0) break block34;
                                                undeterminedLimit = true;
                                                break block10;
                                            }
                                            if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) > 0) break block35;
                                            undeterminedLimit = true;
                                            break block10;
                                        }
                                        if (!enteredOperator.equals("<=") && !enteredOperator.equals("<") || value.compareTo(value2) < 0) break block36;
                                        undeterminedLimit = true;
                                        break block10;
                                    }
                                    if (value.compareTo(value1) != 1 || value.compareTo(value2) != -1) break block10;
                                    undeterminedLimit = true;
                                    break block10;
                                }
                                if (!theoperator.equals("Inclusive Between") && !theoperator.equals("IB") || value1null || value2null) break block37;
                                if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") && !enteredOperator.equals("<=") || value.compareTo(value1) != 0) break block38;
                                undeterminedLimit = true;
                                break block10;
                            }
                            if (!enteredOperator.equals("<") && !enteredOperator.equals("<=") && !enteredOperator.equals(">=") || value.compareTo(value2) != 0) break block39;
                            undeterminedLimit = true;
                            break block10;
                        }
                        if (!enteredOperator.equals(">=") && !enteredOperator.equals(">") || value.compareTo(value1) > 0) break block40;
                        undeterminedLimit = true;
                        break block10;
                    }
                    if (!enteredOperator.equals("<=") && !enteredOperator.equals("<") || value.compareTo(value2) < 0) break block41;
                    undeterminedLimit = true;
                    break block10;
                }
                if (value.compareTo(value1) != 1 || value.compareTo(value2) != -1) break block10;
                undeterminedLimit = true;
                break block10;
            }
            if (theoperator.equalsIgnoreCase("IN") || theoperator.equalsIgnoreCase("NIN") || theoperator.equalsIgnoreCase("NOT IN")) {
                String[] valuelist = StringUtil.split(stringvalue1, ";");
                int items = valuelist.length;
                for (int item = 0; item < items; ++item) {
                    try {
                        value1 = formatUtil.parseBigDecimal(valuelist[item]);
                        if ((enteredOperator.equals(">=") || enteredOperator.equals("<=")) && value.compareTo(value1) == 0) {
                            undeterminedLimit = true;
                            break;
                        }
                        if ((enteredOperator.equals(">=") || enteredOperator.equals(">")) && value.compareTo(value1) == -1) {
                            undeterminedLimit = true;
                            break;
                        }
                        if (!enteredOperator.equals("<=") && !enteredOperator.equals("<") || value.compareTo(value1) != 1) continue;
                        undeterminedLimit = true;
                        break;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        return undeterminedLimit;
    }

    public BigDecimal convertUnits(StringHolder unitsidholder, String displayunits, BigDecimal value) throws SapphireException {
        BigDecimal returnvalue = null;
        this.database.createPreparedResultSet("ConvertUnits", "SELECT unitsid, expression FROM unitconversion WHERE lower(unitsid) = ? and tounits = ?", new Object[]{unitsidholder.value.toLowerCase(), displayunits});
        if (this.database.getNext("ConvertUnits")) {
            unitsidholder.value = this.database.getString("ConvertUnits", "unitsid");
            HashMap<String, BigDecimal> expressionParams = new HashMap<String, BigDecimal>();
            expressionParams.put("this", value);
            String result = ExpressionUtil.evaluate(this.database.getString("ConvertUnits", "expression"), expressionParams);
            try {
                returnvalue = FormatUtil.getInstance().parseBigDecimal(result);
            }
            catch (NumberFormatException nfe) {
                this.logger.error("Result from units conversion expression generated an invalid number", nfe);
            }
        } else {
            this.logger.error("Failed to convert units from '" + unitsidholder.value + "' to '" + displayunits + "'");
        }
        return returnvalue;
    }

    public boolean isLimitMet(Object thevalue, String theoperator, String stringvalue1, String stringvalue2, String datatype, String referencesdi, String enteredOperator) throws SapphireException {
        boolean limitmet = false;
        if (thevalue != null && theoperator != null) {
            if (datatype.equals("N") || datatype.equals("NC")) {
                boolean value2null;
                FormatUtil formatUtil = FormatUtil.getInstance();
                BigDecimal value = (BigDecimal)thevalue;
                BigDecimal value1 = null;
                BigDecimal value2 = null;
                try {
                    value1 = formatUtil.parseBigDecimal(stringvalue1);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    value2 = formatUtil.parseBigDecimal(stringvalue2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                BigDecimal enteredTextVal = this.checkEnteredText(value, enteredOperator, value1, value2);
                if (enteredTextVal != null) {
                    value = enteredTextVal;
                }
                boolean value1null = value1 == null;
                boolean bl = value2null = value2 == null;
                if (theoperator.equals("=") && !value1null && value.compareTo(value1) == 0) {
                    limitmet = true;
                } else if (theoperator.equals("<>") && !value1null && value.compareTo(value1) != 0) {
                    limitmet = true;
                } else if (theoperator.equals(">") && !value1null && value.compareTo(value1) > 0) {
                    limitmet = true;
                } else if (theoperator.equals("<") && !value1null && value.compareTo(value1) < 0) {
                    limitmet = true;
                } else if (theoperator.equals(">=") && !value1null && value.compareTo(value1) >= 0) {
                    limitmet = true;
                } else if (theoperator.equals("<=") && !value1null && value.compareTo(value1) <= 0) {
                    limitmet = true;
                } else if ((theoperator.equalsIgnoreCase("IB") || theoperator.equalsIgnoreCase("Inclusive Between")) && !value1null && !value2null && value.compareTo(value1) >= 0 && value.compareTo(value2) <= 0) {
                    limitmet = true;
                } else if ((theoperator.equalsIgnoreCase("EB") || theoperator.equalsIgnoreCase("Exclusive Between")) && !value1null && !value2null && value.compareTo(value1) > 0 && value.compareTo(value2) < 0) {
                    limitmet = true;
                } else if (!(!theoperator.equalsIgnoreCase("IO") && !theoperator.equalsIgnoreCase("Inclusive Outside") || value1null || value2null || value.compareTo(value1) > 0 && value.compareTo(value2) < 0)) {
                    limitmet = true;
                } else if (!(!theoperator.equalsIgnoreCase("EO") && !theoperator.equalsIgnoreCase("Exclusive Outside") || value1null || value2null || value.compareTo(value1) >= 0 && value.compareTo(value2) <= 0)) {
                    limitmet = true;
                } else if (theoperator.equalsIgnoreCase("IN")) {
                    String[] valuelist = StringUtil.split(stringvalue1, ";");
                    int items = valuelist.length;
                    for (int item = 0; item < items; ++item) {
                        try {
                            value1 = formatUtil.parseBigDecimal(valuelist[item]);
                            if (value.compareTo(value1) != 0 || enteredOperator != null && enteredOperator.length() != 0 && !enteredOperator.equals("=")) continue;
                            limitmet = true;
                            break;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                } else if (theoperator.equalsIgnoreCase("NIN") || theoperator.equalsIgnoreCase("NOT IN")) {
                    String[] valuelist = StringUtil.split(stringvalue1, ";");
                    int items = valuelist.length;
                    limitmet = true;
                    for (int item = 0; item < items; ++item) {
                        try {
                            value1 = formatUtil.parseBigDecimal(valuelist[item]);
                            if (value.compareTo(value1) != 0 || enteredOperator != null && enteredOperator.length() != 0 && !enteredOperator.equals("=")) continue;
                            limitmet = false;
                            break;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            } else if (datatype.equals("T") || datatype.equals("R") || datatype.equals("V") || datatype.equals("S") || datatype.equals("TC")) {
                String value = (String)thevalue;
                if (theoperator.equals("=") && value.equals(stringvalue1)) {
                    limitmet = true;
                } else if (theoperator.equals("<>") && !value.equals(stringvalue1)) {
                    limitmet = true;
                } else if (theoperator.equalsIgnoreCase("IN")) {
                    String[] valuelist = StringUtil.split(stringvalue1, ";");
                    int items = valuelist.length;
                    for (int item = 0; item < items; ++item) {
                        if (!value.equals(valuelist[item])) continue;
                        limitmet = true;
                        break;
                    }
                } else if (theoperator.equalsIgnoreCase("NIN") || theoperator.equalsIgnoreCase("NOT IN")) {
                    String[] valuelist = StringUtil.split(stringvalue1, ";");
                    limitmet = true;
                    int items = valuelist.length;
                    for (int item = 0; item < items; ++item) {
                        if (!value.equals(valuelist[item])) continue;
                        limitmet = false;
                        break;
                    }
                } else if (datatype.equals("R") || datatype.equals("V")) {
                    QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
                    DataSet limitvalues = qp.getRefTypeDataSet(referencesdi);
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("refvalueid", value);
                    int valuepos = limitvalues.findRow(filter);
                    filter.put("refvalueid", stringvalue1);
                    int value1pos = limitvalues.findRow(filter);
                    filter.put("refvalueid", stringvalue2);
                    int value2pos = limitvalues.findRow(filter);
                    if ((theoperator.equalsIgnoreCase("IB") || theoperator.equalsIgnoreCase("Inclusive Between")) && valuepos >= value1pos && valuepos <= value2pos) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("EB") || theoperator.equalsIgnoreCase("Exclusive Between")) && valuepos > value1pos && valuepos < value2pos) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("IO") || theoperator.equalsIgnoreCase("Inclusive Outside")) && (valuepos <= value1pos || valuepos >= value2pos)) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("EO") || theoperator.equalsIgnoreCase("Exclusive Outside")) && (valuepos < value1pos || valuepos > value2pos)) {
                        limitmet = true;
                    }
                } else if (datatype.equals("S")) {
                    PropertyList refProperties = this.sdcProcessor.getProperties(referencesdi);
                    String keycolid1 = refProperties.getProperty("keycolid1");
                    this.database.createPreparedResultSet("loadsdivalues", "SELECT " + keycolid1 + " FROM " + refProperties.getProperty("tableid") + " WHERE " + keycolid1 + " in ( ?, ?, ? ) ORDER BY usersequence," + keycolid1, new Object[]{value, stringvalue1, stringvalue2});
                    DataSet limitvalues = new DataSet(this.database.getResultSet("loadsdivalues"));
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put(keycolid1, value);
                    int valuepos = limitvalues.findRow(filter);
                    filter.put(keycolid1, stringvalue1);
                    int value1pos = limitvalues.findRow(filter);
                    filter.put(keycolid1, stringvalue2);
                    int value2pos = limitvalues.findRow(filter);
                    if ((theoperator.equalsIgnoreCase("IB") || theoperator.equalsIgnoreCase("Inclusive Between")) && valuepos >= value1pos && valuepos <= value2pos) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("EB") || theoperator.equalsIgnoreCase("Exclusive Between")) && valuepos > value1pos && valuepos < value2pos) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("IO") || theoperator.equalsIgnoreCase("Inclusive Outside")) && (valuepos <= value1pos || valuepos >= value2pos)) {
                        limitmet = true;
                    } else if ((theoperator.equalsIgnoreCase("EO") || theoperator.equalsIgnoreCase("Exclusive Outside")) && (valuepos < value1pos || valuepos > value2pos)) {
                        limitmet = true;
                    }
                }
            }
        }
        return limitmet;
    }

    public boolean convertUnits(StringHolder unitsidholder, String displayunits, DataSet sdidataitems, int row) throws SapphireException {
        BigDecimal newvalue;
        boolean rc = false;
        BigDecimal originalvalue = sdidataitems.getBigDecimal(row, "enteredvalue");
        if (unitsidholder.value != null && displayunits != null && unitsidholder.value.equals(displayunits)) {
            sdidataitems.setNumber(row, "transformvalue", originalvalue);
            rc = true;
        } else if (originalvalue != null && (newvalue = this.convertUnits(unitsidholder, displayunits, originalvalue)) != null) {
            sdidataitems.setNumber(row, "transformvalue", newvalue);
            rc = true;
        }
        return rc;
    }

    private BigDecimal checkEnteredText(BigDecimal transformvalue, String operator, BigDecimal stringvalue1, BigDecimal stringvalue2) {
        BigDecimal enteredtextVal = null;
        if (transformvalue != null && operator != null && (operator.equals("<") || operator.equals(">"))) {
            if (stringvalue1 != null && stringvalue2 != null) {
                if (transformvalue.compareTo(stringvalue1) == 0 || transformvalue.compareTo(stringvalue2) == 0) {
                    if (operator.equals(">")) {
                        enteredtextVal = transformvalue.add(new BigDecimal("1"));
                    } else if (operator.equals("<")) {
                        enteredtextVal = transformvalue.subtract(new BigDecimal("1"));
                    }
                }
            } else if (stringvalue1 != null && stringvalue2 == null) {
                if (transformvalue.compareTo(stringvalue1) == 0) {
                    if (operator.equals(">")) {
                        enteredtextVal = transformvalue.add(new BigDecimal("1"));
                    } else if (operator.equals("<")) {
                        enteredtextVal = transformvalue.subtract(new BigDecimal("1"));
                    }
                }
            } else if (stringvalue1 == null && stringvalue2 != null && transformvalue.compareTo(stringvalue2) == 0) {
                if (operator.equals(">")) {
                    enteredtextVal = transformvalue.add(new BigDecimal("1"));
                } else if (operator.equals("<")) {
                    enteredtextVal = transformvalue.subtract(new BigDecimal("1"));
                }
            }
        }
        return enteredtextVal;
    }

    public static String getApparentDatatype(String entry, DateTimeUtil dtu, FormatUtil formatutil) {
        String datatypes;
        try {
            formatutil.parseBigDecimal(entry);
            datatypes = "N";
        }
        catch (NumberFormatException nfe) {
            Calendar c = dtu.getCalendar(entry);
            datatypes = c != null ? "D" : "T";
        }
        return datatypes;
    }

    private static void processLimitValue(String value, int row, DataSet data, int num, ConnectionInfo connectionInfo) throws SapphireException {
        StringBuffer value1 = new StringBuffer();
        StringBuffer value1num = new StringBuffer();
        try {
            M18NUtil m18n = new M18NUtil(connectionInfo);
            MiscUtil.MiscString.parseComplexNumber(value, value1num, value1, m18n, data.getLocale().equals(m18n.getLocale()));
            data.setValue(row, "value" + num + "", value1.toString());
            data.setValue(row, "value" + num + "num", value1num.toString());
        }
        catch (NumberFormatException e) {
            throw new SapphireException("Number entered for value" + num + " invalid", e);
        }
    }
}

