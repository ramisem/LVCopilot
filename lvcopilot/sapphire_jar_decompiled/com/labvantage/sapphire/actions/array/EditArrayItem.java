/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditArrayItem
extends BaseAction
implements sapphire.action.EditArrayItem {
    Map<String, Object[]> convertUnitCache = new HashMap<String, Object[]>();
    private M18NUtil m18NUtil;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet arrayItemDataDS;
        String arrayitemsql;
        String arrayid = properties.getProperty("arrayid", "");
        String arrayitemid = properties.getProperty("arrayitemid", "");
        String arrayitemlabel = properties.getProperty("arrayitemlabel", "");
        String totalVol = properties.getProperty("totalvol");
        String totalVolUnits = properties.getProperty("totalvolunits");
        String totalConc = properties.getProperty("totalconc", "");
        String totalConcUnits = properties.getProperty("totalconcunits", "");
        boolean validateQuantity = "Y".equals(properties.getProperty("validatetotalvolume", "N"));
        if (arrayitemid.isEmpty()) {
            if (!arrayitemlabel.isEmpty() && !arrayid.isEmpty()) {
                String[] labellist = StringUtil.split(arrayitemlabel, ";");
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT arrayitemid, itemlabel FROM arrayitem WHERE arrayitemid like " + safeSQL.addVar(arrayid + "%");
                DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                HashMap<String, String> labelArrayItemIDMap = new HashMap<String, String>();
                if (OpalUtil.isNotEmpty(ret)) {
                    for (int i = 0; i < ret.size(); ++i) {
                        labelArrayItemIDMap.put(ret.getString(i, "itemlabel"), ret.getString(i, "arrayitemid"));
                    }
                }
                StringBuilder temp = new StringBuilder();
                for (String label : labellist) {
                    if (!labelArrayItemIDMap.containsKey(label)) {
                        throw new SapphireException("Invalid label specified:" + label);
                    }
                    temp.append((String)labelArrayItemIDMap.get(label)).append(";");
                }
                temp.setLength(temp.length() - 1);
                arrayitemid = temp.toString();
            } else {
                throw new SapphireException(this.getTranslationProcessor().translate("Either arrayitemids or arrayid/arrayitemlabels have to be specified to identify the items to edit."));
            }
        }
        String[] arrayitemids = StringUtil.split(arrayitemid, ";");
        String[] inputVolUnitsArr = StringUtil.split(totalVolUnits, ";");
        String[] deltaVols = StringUtil.split(totalVol, ";");
        if (arrayitemids.length != deltaVols.length && deltaVols.length == 1) {
            totalVol = PropertyUtil.repeat(totalVol, arrayitemids.length);
            totalVolUnits = PropertyUtil.repeat(totalVolUnits, arrayitemids.length);
            inputVolUnitsArr = StringUtil.split(totalVolUnits, ";");
            deltaVols = StringUtil.split(totalVol, ";");
        }
        DataSet data = new DataSet(this.connectionInfo);
        data.addColumn("arrayitemid", 0);
        data.addColumn("totalvolume", 1);
        int size = arrayitemids.length;
        if (size < 1000) {
            SafeSQL safeSQL = new SafeSQL();
            arrayitemsql = "SELECT ai.arrayitemid, ai.totalvolume, ai.totalvolumeunits, at.maxvolumeunits FROM arrayitem ai, array a, arraytype at WHERE ai.arrayitemid in (" + safeSQL.addIn(Arrays.asList(arrayitemids)) + ") AND ai.arrayid           = a.arrayid  AND a.arraytypeid        = at.arraytypeid  AND a.arraytypeversionid = at.arraytypeversionid";
            arrayItemDataDS = this.getQueryProcessor().getPreparedSqlDataSet(arrayitemsql, safeSQL.getValues());
        } else {
            String rsetid = this.getDAMProcessor().createRSet("LV_ArrayItem", arrayitemid, null, null);
            arrayitemsql = "SELECT ai.arrayitemid, ai.totalvolume, ai.totalvolumeunits, at.maxvolumeunits FROM arrayitem ai, array a, arraytype at WHERE ai.arrayitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?) AND ai.arrayid           = a.arrayid  AND a.arraytypeid        = at.arraytypeid  AND a.arraytypeversionid = at.arraytypeversionid";
            arrayItemDataDS = this.getQueryProcessor().getPreparedSqlDataSet(arrayitemsql, (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        }
        String maxQtyUnits = "";
        for (int i = 0; i < size; ++i) {
            double finalvalue;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayitemid", arrayitemids[i]);
            int row = data.findRow(filter);
            String inputUnits = "";
            inputUnits = inputVolUnitsArr.length == size ? inputVolUnitsArr[i] : inputVolUnitsArr[0];
            String aiVolStr = "";
            String arrayItemUnits = "";
            if (row == -1) {
                row = data.addRow();
                int rowindex = arrayItemDataDS.findRow(filter);
                if (rowindex != -1) {
                    maxQtyUnits = arrayItemDataDS.getValue(rowindex, "maxvolumeunits", "");
                    arrayItemUnits = arrayItemDataDS.getValue(rowindex, "totalvolumeunits", "");
                    aiVolStr = arrayItemDataDS.getValue(rowindex, "totalvolume", "");
                }
                data.setValue(row, "maxvolumeunits", maxQtyUnits);
            } else {
                maxQtyUnits = data.getValue(0, "maxvolumeunits", "");
                arrayItemUnits = data.getString(row, "totalvolumeunits", "");
                aiVolStr = data.getValue(row, "totalvolume", "0");
            }
            if (deltaVols[i].contains("+")) {
                data.setString(row, "arrayitemid", arrayitemids[i]);
                if (!aiVolStr.isEmpty()) {
                    Object[] convertedVol = this.convertToArrayTypeUnitsNumeric(deltaVols[i].substring(1), inputUnits, aiVolStr, arrayItemUnits, maxQtyUnits);
                    data.setNumber(row, "totalvolume", ((BigDecimal)convertedVol[0]).add((BigDecimal)convertedVol[1]));
                    data.setString(row, "totalvolumeunits", (String)convertedVol[2]);
                } else {
                    data.setNumber(row, "totalvolume", deltaVols[i].substring(1));
                    data.setString(row, "totalvolumeunits", inputUnits);
                }
            } else if (deltaVols[i].contains("-")) {
                if (!aiVolStr.isEmpty()) {
                    data.setString(row, "arrayitemid", arrayitemids[i]);
                    Object[] convertedVol = this.convertToArrayTypeUnitsNumeric(deltaVols[i].substring(1), inputUnits, aiVolStr, arrayItemUnits, maxQtyUnits);
                    BigDecimal aiVol = (BigDecimal)convertedVol[0];
                    BigDecimal volToDecrement = (BigDecimal)convertedVol[1];
                    if (aiVol != null && volToDecrement != null) {
                        if (volToDecrement.compareTo(aiVol) == 1) {
                            if (validateQuantity) {
                                throw new SapphireException("Amount to decrement is more than available volume for:" + arrayitemids[i]);
                            }
                            data.setNumber(row, "totalvolume", 0);
                        } else {
                            data.setNumber(row, "totalvolume", aiVol.subtract(volToDecrement));
                        }
                        data.setString(row, "totalvolumeunits", (String)convertedVol[2]);
                    }
                } else {
                    this.logger.error("No volume in arrayitem to decrement from");
                }
            } else {
                data.setString(row, "arrayitemid", arrayitemids[i]);
                Object[] convertedVol = this.convertToArrayTypeUnitsNumeric(deltaVols[i], inputUnits, aiVolStr, arrayItemUnits, maxQtyUnits);
                if (convertedVol[1] != null) {
                    data.setNumber(row, "totalvolume", (BigDecimal)convertedVol[1]);
                    data.setString(row, "totalvolumeunits", (String)convertedVol[2]);
                } else {
                    this.logger.error("Failed to convert units for total volume computation:" + arrayitemids[i]);
                }
            }
            if (data.getBigDecimal(row, "totalvolume") == null || !((finalvalue = data.getBigDecimal(row, "totalvolume").doubleValue()) < 0.0) || !validateQuantity) continue;
            throw new SapphireException("Arrayitem volume cannot go negative");
        }
        PropertyList arrayprops = new PropertyList();
        arrayprops.setProperty("sdcid", "LV_ArrayItem");
        arrayprops.setProperty("keyid1", data.getColumnValues("arrayitemid", ";"));
        arrayprops.setProperty("totalvolume", data.getColumnValues("totalvolume", ";"));
        arrayprops.setProperty("totalvolumeunits", data.getColumnValues("totalvolumeunits", ";"));
        if (!totalConc.isEmpty()) {
            arrayprops.setProperty("concentration", totalConc);
        }
        if (!totalConcUnits.isEmpty()) {
            arrayprops.setProperty("concentrationunits", totalConcUnits);
        }
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("EditSDI", "1", arrayprops);
    }

    private Object[] convertToArrayTypeUnitsNumeric(String inputVolStr, String inputUnits, String arrayItemVolStr, String arrayItemUnits, String arrayTypeUnits) throws SapphireException {
        String cachekey = "cachekey_" + inputVolStr + "_" + inputUnits + "_" + arrayItemVolStr + "_" + arrayItemUnits + "_" + arrayTypeUnits;
        if (!this.convertUnitCache.containsKey(cachekey)) {
            String units = "";
            BigDecimal inputVol = this.convertToBigDecimal(inputVolStr);
            BigDecimal arrayItemVol = this.convertToBigDecimal(arrayItemVolStr);
            if (!arrayTypeUnits.isEmpty()) {
                units = arrayTypeUnits;
                if (!inputUnits.isEmpty() && !inputUnits.equals(arrayTypeUnits)) {
                    inputVol = OpalUtil.convertUnit(this.connectionInfo, inputVol, inputUnits, arrayTypeUnits);
                }
                if (!arrayItemUnits.isEmpty() && !arrayItemUnits.equals(arrayTypeUnits)) {
                    arrayItemVol = OpalUtil.convertUnit(this.connectionInfo, arrayItemVol, arrayItemUnits, arrayTypeUnits);
                }
            } else if (!(inputUnits.isEmpty() || arrayItemUnits.isEmpty() || inputUnits.equals(arrayItemUnits))) {
                units = arrayItemUnits;
                inputVol = OpalUtil.convertUnit(this.connectionInfo, inputVol, inputUnits, arrayItemUnits);
            } else {
                units = inputUnits;
            }
            this.convertUnitCache.put(cachekey, new Object[]{arrayItemVol, inputVol, units});
        }
        return this.convertUnitCache.get(cachekey);
    }

    private BigDecimal convertToBigDecimal(String inputStr) {
        if (inputStr != null && !inputStr.isEmpty()) {
            if (this.m18NUtil == null) {
                this.m18NUtil = new M18NUtil(this.connectionInfo);
            }
            return this.m18NUtil.parseBigDecimal(inputStr).setScale(3, RoundingMode.HALF_UP);
        }
        return null;
    }
}

