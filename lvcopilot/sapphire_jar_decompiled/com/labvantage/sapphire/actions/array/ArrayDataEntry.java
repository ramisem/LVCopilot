/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ArrayDataEntry
extends BaseAction
implements sapphire.action.ArrayDataEntry {
    private QueryProcessor queryProcessor;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.queryProcessor = this.getQueryProcessor();
        String arrayid = properties.getProperty("arrayid", "");
        if (arrayid.length() == 0) {
            throw new SapphireException("Array ID is mandatory to perform Data Entry.");
        }
        String arraymethodid = properties.getProperty("arraymethodid", "");
        String arraymethodinstance = properties.getProperty("arraymethodinstance", "");
        String arraymethodversionid = "";
        String currentItemStatus = "";
        String promoteresultsflag = "";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("arrayid", arrayid);
        map.put("arraymethodid", arraymethodid);
        map.put("arraymethodinstance", arraymethodinstance);
        String arraymethodinfo = ArrayUtil.getCurrentArrayMethodItemDetails(this.getQueryProcessor(), arrayid, arraymethodid, arraymethodinstance);
        if (arraymethodinfo == null || arraymethodinfo.length() <= 0) {
            map.put("arraymethodid", arraymethodid);
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot perform DataEntry on [arrayid] before ArrayMethod [arraymethodid] is applied", map));
        }
        String[] tokens = StringUtil.split(arraymethodinfo, "|");
        arraymethodid = tokens[0];
        arraymethodinstance = tokens[1];
        currentItemStatus = tokens[2];
        promoteresultsflag = tokens[3];
        arraymethodversionid = tokens[4];
        map.put("arraymethodid", arraymethodid);
        map.put("arraymethodinstance", arraymethodinstance);
        if (arraymethodversionid.length() == 0) {
            arraymethodversionid = ArrayUtil.getArrayMethodCurrentVersion(this.getQueryProcessor(), arraymethodid);
        }
        String arrayStatus = ArrayUtil.getArrayStatus(this.queryProcessor, arrayid);
        properties.setProperty("arraymethodinstance", arraymethodinstance);
        map.put("arraystatus", arrayStatus);
        if (!ArrayUtil.validateStatus(arrayStatus, currentItemStatus, "ArrayDataEntry", promoteresultsflag)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array status is [arraystatus]. Cannot perform Data Entry on array: [arrayid]", map));
        }
        String level = properties.getProperty("level", "item");
        String itemlabel = properties.getProperty("arrayitemlabel", properties.getProperty("itemlabel", ""));
        String rownumber = properties.getProperty("rownumber", "");
        String columnnumber = properties.getProperty("columnnumber", "");
        String zone = properties.getProperty("zone", "");
        String paramid = properties.getProperty("paramid", "");
        String paramtype = properties.getProperty("paramtype", "");
        String results = properties.getProperty("results", "");
        String itemsequence = properties.getProperty("itemsequence", "rowthencolumn");
        if (level.equals("item")) {
            DataSet arrayitems = this.getArrayItemIds(arrayid, itemlabel, rownumber, columnnumber, itemsequence, paramid, paramtype, results);
            arrayitems = this.updateItemDataSet(arrayid, arrayitems, arraymethodid, arraymethodversionid, arraymethodinstance);
            properties.deleteProperty("arrayid");
            properties.deleteProperty("arraymethodid");
            properties.deleteProperty("arraymethodinstance");
            properties.deleteProperty("level");
            properties.deleteProperty("itemlabel");
            properties.deleteProperty("arrayitemlabel");
            properties.deleteProperty("rownumber");
            properties.deleteProperty("columnnumber");
            properties.deleteProperty("zone");
            properties.deleteProperty("paramid");
            properties.deleteProperty("results");
            properties.deleteProperty("itemsequence");
            properties.setProperty("sdcid", "LV_ArrayItem");
            properties.setProperty("keyid1", arrayitems.getColumnValues("keyid1", ";"));
            properties.setProperty("paramlistid", arrayitems.getColumnValues("paramlistid", ";"));
            properties.setProperty("paramlistversionid", arrayitems.getColumnValues("paramlistversionid", ";"));
            properties.setProperty("variantid", arrayitems.getColumnValues("variantid", ";"));
            properties.setProperty("paramid", arrayitems.getColumnValues("paramid", ";"));
            properties.setProperty("paramtype", arrayitems.getColumnValues("paramtype", ";"));
            properties.setProperty("dataset", arrayitems.getColumnValues("dataset", ";"));
            properties.setProperty("replicateid", arrayitems.getColumnValues("replicateid", ";"));
            properties.setProperty("enteredtext", arrayitems.getColumnValues("enteredtext", ";"));
            properties.setProperty("arrayid", arrayid);
            properties.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            properties.setProperty("auditactivity", properties.getProperty("auditactivity"));
            properties.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EnterDataItem", "1", properties);
        } else if (level.equals("zone")) {
            DataSet arrayzoneitems = this.getZoneDataItems(arrayid, zone, paramid, paramtype, results);
            arrayzoneitems = this.updateZoneDataSet(arrayid, arrayzoneitems, arraymethodid, arraymethodversionid, arraymethodinstance);
            properties.deleteProperty("arrayid");
            properties.deleteProperty("arraymethodid");
            properties.deleteProperty("arraymethodinstance");
            properties.deleteProperty("level");
            properties.deleteProperty("itemlabel");
            properties.deleteProperty("arrayitemlabel");
            properties.deleteProperty("rownumber");
            properties.deleteProperty("columnnumber");
            properties.deleteProperty("zone");
            properties.deleteProperty("paramid");
            properties.deleteProperty("results");
            properties.deleteProperty("itemsequence");
            properties.setProperty("sdcid", "LV_ArrayZone");
            properties.setProperty("keyid1", arrayzoneitems.getColumnValues("keyid1", ";"));
            properties.setProperty("paramlistid", arrayzoneitems.getColumnValues("paramlistid", ";"));
            properties.setProperty("paramlistversionid", arrayzoneitems.getColumnValues("paramlistversionid", ";"));
            properties.setProperty("variantid", arrayzoneitems.getColumnValues("variantid", ";"));
            properties.setProperty("paramid", arrayzoneitems.getColumnValues("paramid", ";"));
            properties.setProperty("paramtype", arrayzoneitems.getColumnValues("paramtype", ";"));
            properties.setProperty("dataset", arrayzoneitems.getColumnValues("dataset", ";"));
            properties.setProperty("replicateid", arrayzoneitems.getColumnValues("replicateid", ";"));
            properties.setProperty("enteredtext", arrayzoneitems.getColumnValues("enteredtext", ";"));
            properties.setProperty("arrayid", arrayid);
            properties.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            properties.setProperty("auditactivity", properties.getProperty("auditactivity"));
            properties.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EnterDataItem", "1", properties);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private DataSet updateItemDataSet(String arrayid, DataSet arrayItems, String arraymethodid, String arraymethodversionid, String arraymethodinstance) throws SapphireException {
        if (arraymethodid.length() > 0) {
            if (arraymethodinstance.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Array method instance not specified"));
            String sql = "SELECT keyid1, dataset FROM sdidata, arrayitem WHERE sdcid='LV_ArrayItem' and arraymethodid=?  AND arraymethodversionid=? AND arraymethodinstance = ?  and keyid1 = arrayitem.arrayitemid and arrayitem.arrayid=?  ORDER BY keyid1";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(arraymethodid);
            safeSQL.addVar(arraymethodversionid);
            safeSQL.addVar(arraymethodinstance);
            safeSQL.addVar(arrayid);
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds == null || ds.getRowCount() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Failed to fetch matching DataSets for specified arrayitems"));
            int pos = 0;
            arrayItems.addColumn("dataset", 1);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                while (pos < arrayItems.getRowCount() && arrayItems.getString(pos, "keyid1", "").equals(ds.getString(i, "keyid1"))) {
                    arrayItems.setNumber(pos++, "dataset", ds.getBigDecimal(i, "dataset"));
                }
            }
        }
        for (int i = 0; i < arrayItems.getRowCount(); ++i) {
            if (arrayItems.getValue(i, "dataset") != null && arrayItems.getValue(i, "dataset").length() != 0) continue;
            throw new SapphireException("Failed to find matching dataset for arrayid=" + arrayid + " arraymethodid=" + arraymethodid + " version=" + arraymethodversionid + " instance=" + arraymethodinstance);
        }
        return arrayItems;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private DataSet updateZoneDataSet(String arrayid, DataSet arrayZones, String arraymethodid, String arraymethodversionid, String arraymethodinstance) throws SapphireException {
        if (arraymethodid.length() <= 0) return arrayZones;
        if (arraymethodinstance.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Array method instance not specified"));
        String sql = "SELECT keyid1, dataset FROM sdidata, arrayzone WHERE sdcid='LV_ArrayZone' and arraymethodid=?  AND arraymethodversionid=? AND arraymethodinstance = ? and keyid1 = arrayzone.arrayzoneid and arrayzone.arrayid=?  ORDER BY keyid1";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodversionid);
        safeSQL.addVar(arraymethodinstance);
        safeSQL.addVar(arrayid);
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds == null || ds.getRowCount() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Failed to fetch matching DataSets for specified arrayzones"));
        boolean pos = false;
        arrayZones.addColumn("dataset", 0);
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String currZone = ds.getString(i, "keyid1");
            for (int z = 0; z < arrayZones.getRowCount(); ++z) {
                if (!arrayZones.getString(z, "arrayzoneid").equals(currZone)) continue;
                arrayZones.setString(z, "keyid1", arrayZones.getString(z, "arrayzoneid"));
                arrayZones.setString(z, "dataset", ds.getValue(i, "dataset"));
            }
        }
        return arrayZones;
    }

    private DataSet getArrayItemIds(String arrayid, String itemlabel, String rownumber, String columnnumber, String itemsequence, String paramid, String paramtype, String results) throws SapphireException {
        if (itemlabel.length() > 0) {
            return this.getArrayItemIdsByLabels(arrayid, itemlabel, paramid, paramtype, results);
        }
        if (rownumber.length() > 0 && columnnumber.length() > 0) {
            return this.getArrayItemIdsByPosition(arrayid, rownumber, columnnumber, paramid, paramtype, results);
        }
        return this.getArrayItemsBySequence(arrayid, itemsequence, paramid, paramtype, results);
    }

    private DataSet getZoneDataItems(String arrayid, String zone, String paramid, String paramtype, String results) throws SapphireException {
        if (zone != null && zone.length() > 0) {
            DataSet dataitems = new DataSet();
            dataitems.addColumnValues("zone", 0, zone, ";");
            dataitems.setString(0, "paramlistid", "ArrayData");
            dataitems.setString(0, "paramlistversionid", "1");
            dataitems.setString(0, "variantid", "1");
            dataitems.addColumnValues("paramid", 0, paramid, ";");
            if (paramtype.length() == 0) {
                dataitems.setString(0, "paramtype", "Standard");
            } else {
                dataitems.addColumnValues("paramtype", 0, paramtype, ";");
            }
            dataitems.setString(0, "replicateid", "1");
            dataitems.padColumns();
            dataitems.addColumnValues("enteredtext", 0, results, ";");
            String[] tokens = StringUtil.split(zone, ";");
            String inClause = "";
            for (int i = 0; i < tokens.length; ++i) {
                if (inClause.length() > 0) {
                    inClause = inClause + ",";
                }
                inClause = inClause + "'" + tokens[i] + "'";
            }
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(arrayid);
            String sql = "SELECT distinct arrayzoneid, zone FROM arrayzone WHERE arrayid=?  and zone IN ( " + safeSQL.addIn(inClause) + " )";
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                for (int z = 0; z < dataitems.getRowCount(); ++z) {
                    String currzone = dataitems.getString(z, "zone");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("zone", currzone);
                    DataSet match = ds.getFilteredDataSet(filter);
                    if (match == null || match.getRowCount() == 0) {
                        throw new SapphireException("Zone " + currzone + " zone details could not be found");
                    }
                    dataitems.setString(z, "arrayzoneid", match.getString(0, "arrayzoneid"));
                }
            }
            return dataitems;
        }
        return null;
    }

    private DataSet getArrayItemIdsByLabels(String arrayid, String itemlabel, String paramid, String paramtype, String results) throws SapphireException {
        DataSet arrayitems = new DataSet();
        arrayitems.addColumnValues("itemlabel", 0, itemlabel, ";");
        arrayitems.setString(0, "paramlistid", "ArrayData");
        arrayitems.setString(0, "paramlistversionid", "1");
        arrayitems.setString(0, "variantid", "1");
        arrayitems.addColumnValues("paramid", 0, paramid, ";");
        if (paramtype.length() == 0) {
            arrayitems.setString(0, "paramtype", "Standard");
        } else {
            arrayitems.addColumnValues("paramtype", 0, paramtype, ";");
        }
        arrayitems.setString(0, "replicateid", "1");
        arrayitems.padColumns();
        arrayitems.addColumnValues("enteredtext", 0, results, ";");
        arrayitems.sort("itemlabel");
        String sql = "SELECT arrayitemid, itemlabel FROM arrayitem WHERE arrayid=?  ORDER by itemlabel";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            int pos = 0;
            for (int i = 0; i < ds.getRowCount(); ++i) {
                while (pos < arrayitems.getRowCount() && ds.getString(i, "itemlabel", "").equals(arrayitems.getString(pos, "itemlabel"))) {
                    arrayitems.setString(pos++, "keyid1", ds.getString(i, "arrayitemid"));
                }
            }
        } else {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to get arrayitemids for one or more itemlabels specified."));
        }
        arrayitems.sort("keyid1");
        return arrayitems;
    }

    private DataSet getArrayItemIdsByPosition(String arrayid, String rownumber, String columnnumber, String paramid, String paramtype, String results) throws SapphireException {
        DataSet items = new DataSet();
        items.addColumnValues("rownumber", 1, rownumber, ";");
        items.addColumnValues("columnnumber", 1, columnnumber, ";");
        items.setString(0, "paramlistid", "ArrayData");
        items.setString(0, "paramlistversionid", "1");
        items.setString(0, "variantid", "1");
        items.addColumnValues("paramid", 0, paramid, ";");
        if (paramtype.length() == 0) {
            items.setString(0, "paramtype", "Standard");
        } else {
            items.addColumnValues("paramtype", 0, paramtype, ";");
        }
        items.setString(0, "replicateid", "1");
        items.padColumns();
        items.addColumnValues("enteredtext", 0, results, ";");
        items.sort("rownumber,columnnumber");
        String sql = "SELECT arrayitemid, xpos, ypos FROM arrayitem WHERE arrayid=?  ORDER by xpos, ypos";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            int pos = 0;
            for (int i = 0; i < ds.getRowCount(); ++i) {
                int ds_xpos = ds.getBigDecimal(i, "xpos").intValue();
                int ds_ypos = ds.getBigDecimal(i, "ypos").intValue();
                while (pos < items.getRowCount() && items.getBigDecimal(pos, "rownumber").intValue() - 1 == ds_xpos && items.getBigDecimal(pos, "columnnumber").intValue() - 1 == ds_ypos) {
                    items.setString(pos++, "keyid1", ds.getString(i, "arrayitemid"));
                }
            }
        } else {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to get arrayitemids for specified positions."));
        }
        items.sort("keyid1");
        return items;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private DataSet getArrayItemsBySequence(String arrayid, String itemsequence, String paramid, String paramtype, String results) throws SapphireException {
        String sql = "SELECT numrows, numcolumns FROM array, arraytype WHERE array.arraytypeid=arraytype.arraytypeid and array.arraytypeversionid = arraytype.arraytypeversionid and array.arrayid=?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        DataSet items = new DataSet();
        items.addColumn("rownumber", 1);
        items.addColumn("columnumber", 1);
        if (ds != null && ds.getRowCount() > 0) {
            int numrows = ds.getBigDecimal(0, "numrows").intValue();
            int numcolumns = ds.getBigDecimal(0, "numcolumns").intValue();
            if (itemsequence.equals("rowthencolumn")) {
                for (int row = 0; row < numrows; ++row) {
                    for (int col = 0; col < numcolumns; ++col) {
                        int count = items.addRow();
                        items.setNumber(count, "rownumber", row);
                        items.setNumber(count, "columnnumber", col);
                    }
                }
                sql = "SELECT arrayitemid, xpos, ypos FROM arrayitem WHERE arrayid=?  ORDER by xpos, ypos";
                ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds == null || ds.getRowCount() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Failed to get arrayitemids for specified positions."));
                int pos = 0;
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    int ds_xpos = ds.getBigDecimal(i, "xpos").intValue();
                    int ds_ypos = ds.getBigDecimal(i, "ypos").intValue();
                    while (pos < items.getRowCount() && items.getBigDecimal(pos, "rownumber").intValue() == ds_xpos && items.getBigDecimal(pos, "columnnumber").intValue() == ds_ypos) {
                        items.setString(pos++, "keyid1", ds.getString(i, "arrayitemid"));
                    }
                }
            } else {
                for (int col = 0; col < numcolumns; ++col) {
                    for (int row = 0; row < numrows; ++row) {
                        int count = items.addRow();
                        items.setNumber(count, "rownumber", row);
                        items.setNumber(count, "columnnumber", col);
                    }
                }
                sql = "SELECT arrayitemid, xpos, ypos FROM arrayitem WHERE arrayid=?  ORDER by ypos, xpos";
                ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds == null || ds.getRowCount() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Failed to get arrayitemids for specified positions."));
                int pos = 0;
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    int ds_xpos = ds.getBigDecimal(i, "xpos").intValue();
                    int ds_ypos = ds.getBigDecimal(i, "ypos").intValue();
                    while (pos < items.getRowCount() && items.getBigDecimal(pos, "rownumber").intValue() == ds_xpos && items.getBigDecimal(pos, "columnnumber").intValue() == ds_ypos) {
                        items.setString(pos++, "keyid1", ds.getString(i, "arrayitemid"));
                    }
                }
            }
        }
        items.setString(0, "paramlistid", "ArrayData");
        items.setString(0, "paramlistversionid", "1");
        items.setString(0, "variantid", "1");
        items.addColumnValues("paramid", 0, paramid, ";");
        if (paramtype.length() == 0) {
            items.setString(0, "paramtype", "Standard");
        } else {
            items.addColumnValues("paramtype", 0, paramtype, ";");
        }
        items.setString(0, "replicateid", "1");
        items.padColumns();
        items.addColumnValues("enteredtext", 0, results, ";");
        items.sort("keyid1");
        return items;
    }
}

