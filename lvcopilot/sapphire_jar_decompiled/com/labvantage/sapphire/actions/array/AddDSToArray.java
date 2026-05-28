/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddDSToArray
extends BaseAction
implements ArrayConstants {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String ID = "AddDSToArray";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_ARRAYID = "arrayid";
    public static final String PROPERTY_ARRAYMETHODID = "arraymethodid";
    public static final String PROPERTY_ARRAYMETHODVERSIONID = "arraymethodversionid";
    public static final String PROPERTY_ARRAYMETHODINSTANCE = "arraymethodinstance";
    public static final String PROPERTY_AUDITREASON = "auditreason";
    public static final String PROPERTY_AUDITACTIVITY = "auditactivity";
    public static final String PROPERTY_AUDITSIGNEDFLAG = "auditsignedflag";
    public static final String ARRAY_PARAMLIST = "ArrayData";
    public static final String ARRAY_PARAMLISTVERSION = "1";
    public static final String ARRAY_VARIANT = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty(PROPERTY_ARRAYID);
        String methodid = properties.getProperty(PROPERTY_ARRAYMETHODID);
        String methodversionid = properties.getProperty(PROPERTY_ARRAYMETHODVERSIONID, "1");
        String methodinstance = properties.getProperty(PROPERTY_ARRAYMETHODINSTANCE, "1");
        String adhoczoneSql = "SELECT * FROM arrayzone WHERE arrayid = ?  AND adhocmodeflag = 'Y'";
        SafeSQL safeSQL0 = new SafeSQL();
        String arrayzoneitemSql = "SELECT * FROM arrayitemarrayzone WHERE  arrayitemid LIKE " + safeSQL0.addVar(arrayid + "%") + " ORDER BY arrayzoneid";
        String arraymethodparamsSql = "SELECT * FROM arraymethodparamitem WHERE arraymethodid = ? and arraymethodversionid = ?";
        String zoneSql = "SELECT arrayzoneid, zone FROM arrayzone WHERE arrayid=?";
        SafeSQL safeSQL1 = new SafeSQL();
        safeSQL1.addVar(methodid);
        safeSQL1.addVar(methodversionid);
        DataSet arrayMethodParamsDS = this.getQueryProcessor().getPreparedSqlDataSet(arraymethodparamsSql, safeSQL1.getValues());
        SafeSQL safeSQL2 = new SafeSQL();
        safeSQL2.addVar(arrayid);
        DataSet adhocZoneInfo = this.getQueryProcessor().getPreparedSqlDataSet(adhoczoneSql, safeSQL2.getValues());
        DataSet arrayZoneItemDS = this.getQueryProcessor().getPreparedSqlDataSet(arrayzoneitemSql, safeSQL0.getValues());
        if (adhocZoneInfo.getRowCount() > 0) {
            DataSet arrayItems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT arrayitemid FROM arrayitem WHERE arrayid=?", safeSQL2.getValues());
            for (int zonenum = 0; zonenum < adhocZoneInfo.getRowCount(); ++zonenum) {
                for (int ai = 0; ai < arrayItems.getRowCount(); ++ai) {
                    int row = arrayZoneItemDS.addRow();
                    arrayZoneItemDS.setString(row, "arrayzoneid", adhocZoneInfo.getString(zonenum, "arrayzoneid"));
                    arrayZoneItemDS.setString(row, "arrayitemid", arrayItems.getString(ai, "arrayitemid"));
                }
            }
        }
        DataSet sdidataDS = this.getSDIDataDS();
        DataSet sdidataitemDS = this.getSDIDataItemDS();
        DataSet arrayZones = this.getQueryProcessor().getPreparedSqlDataSet(zoneSql, safeSQL2.getValues());
        DataSet sdidatazoneDS = this.getSDIDataDS();
        DataSet sdidataitemzoneDS = this.getSDIDataItemDS();
        int dataset = this.getMaxDataset(arrayid);
        ++dataset;
        ArrayList<DataSet> groupedByZoneDS = arrayZoneItemDS.getGroupedDataSets("arrayzoneid");
        HashSet<String> itemidsSet = new HashSet<String>();
        HashSet<String> zoneidsSet = new HashSet<String>();
        if (!arrayMethodParamsDS.isEmpty()) {
            for (int i = 0; i < arrayMethodParamsDS.size(); ++i) {
                String zone = arrayMethodParamsDS.getValue(i, "zone");
                String paramid = arrayMethodParamsDS.getValue(i, "paramid");
                String levelflag = arrayMethodParamsDS.getValue(i, "levelflag");
                String paramtype = arrayMethodParamsDS.getValue(i, "paramtype");
                String datatypes = arrayMethodParamsDS.getValue(i, "datatypes");
                String entryreftypeid = arrayMethodParamsDS.getValue(i, "entryreftypeid");
                String calcrule = arrayMethodParamsDS.getValue(i, "calcrule");
                String displayformat = arrayMethodParamsDS.getValue(i, "displayformat");
                String displayunits = arrayMethodParamsDS.getValue(i, "unitsid");
                HashMap<String, String> paramdata = new HashMap<String, String>();
                paramdata.put("paramid", paramid);
                paramdata.put("paramtype", paramtype);
                paramdata.put("replicateid", "1");
                paramdata.put("datatypes", datatypes);
                paramdata.put("entryreftypeid", entryreftypeid);
                paramdata.put("calcrule", calcrule);
                paramdata.put("displayformat", displayformat);
                paramdata.put("displayunits", displayunits);
                DataSet zoneitemdata = new DataSet();
                for (int j = 0; j < groupedByZoneDS.size(); ++j) {
                    DataSet temp = groupedByZoneDS.get(j);
                    String arrayzoneid = temp.getValue(0, "arrayzoneid");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("zone", zone);
                    DataSet match = arrayZones.getFilteredDataSet(filter);
                    if (match == null || !match.getString(0, "arrayzoneid").equalsIgnoreCase(arrayzoneid)) continue;
                    zoneitemdata = temp;
                    break;
                }
                if (zoneitemdata.isEmpty()) continue;
                if ("I".equalsIgnoreCase(levelflag)) {
                    if (zone.equals("(FullArray)")) {
                        this.addDSFullArrayItemLevel(arrayid, sdidataDS, sdidataitemDS, paramdata, methodid, methodversionid, methodinstance, dataset, itemidsSet);
                        continue;
                    }
                    this.addDSItemLevel(sdidataDS, sdidataitemDS, paramdata, zoneitemdata, methodid, methodversionid, methodinstance, dataset, itemidsSet);
                    continue;
                }
                if (!"Z".equalsIgnoreCase(levelflag)) continue;
                this.addDSZoneLevel(sdidatazoneDS, sdidataitemzoneDS, paramdata, zoneitemdata, methodid, methodversionid, methodinstance, dataset, zoneidsSet);
            }
        }
        if (!sdidataDS.isEmpty()) {
            this.populateDataSet(properties, sdidataDS, sdidataitemDS, "LV_ArrayItem");
        }
        if (!sdidatazoneDS.isEmpty()) {
            this.populateDataSet(properties, sdidatazoneDS, sdidataitemzoneDS, "LV_ArrayZone");
        }
    }

    private int getMaxDataset(String arrayid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select max(distinct dataset) maxi from sdidata where keyid1 like " + safeSQL.addVar(arrayid + "%") + " and paramlistid = 'ArrayData' and paramlistversionid = '1' and variantid = '1'";
        DataSet sqlDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String maxi = sqlDataSet.getValue(0, "maxi", "");
        int max = 0;
        if (!"".equalsIgnoreCase(maxi)) {
            max = Integer.parseInt(maxi);
        }
        return max;
    }

    private void populateDataSet(PropertyList properties, DataSet sdidataDS, DataSet sdidataitemDS, String sdcid) throws ActionException {
        PropertyList sdidataprops = new PropertyList();
        sdidataprops.setProperty("sdcid", sdcid);
        sdidataprops.setProperty("keyid1", sdidataDS.getColumnValues("keyid1", ";"));
        sdidataprops.setProperty("paramlistid", sdidataDS.getColumnValues("paramlistid", ";"));
        sdidataprops.setProperty("paramlistversionid", sdidataDS.getColumnValues("paramlistversionid", ";"));
        sdidataprops.setProperty("variantid", sdidataDS.getColumnValues("variantid", ";"));
        sdidataprops.setProperty("dataset", sdidataDS.getColumnValues("dataset", ";"));
        sdidataprops.setProperty(PROPERTY_ARRAYMETHODID, sdidataDS.getColumnValues(PROPERTY_ARRAYMETHODID, ";"));
        sdidataprops.setProperty(PROPERTY_ARRAYMETHODVERSIONID, sdidataDS.getColumnValues(PROPERTY_ARRAYMETHODVERSIONID, ";"));
        sdidataprops.setProperty(PROPERTY_ARRAYMETHODINSTANCE, sdidataDS.getColumnValues(PROPERTY_ARRAYMETHODINSTANCE, ";"));
        sdidataprops.setProperty("propsmatch", "Y");
        sdidataprops.setProperty(PROPERTY_AUDITREASON, properties.getProperty(PROPERTY_AUDITREASON, ""));
        sdidataprops.setProperty(PROPERTY_AUDITACTIVITY, properties.getProperty(PROPERTY_AUDITACTIVITY, ""));
        sdidataprops.setProperty(PROPERTY_AUDITSIGNEDFLAG, properties.getProperty(PROPERTY_AUDITSIGNEDFLAG, ""));
        this.getActionProcessor().processAction("AddDataSet", "1", sdidataprops);
        PropertyList sdidataitemprops = new PropertyList();
        sdidataitemprops.setProperty("sdcid", sdcid);
        sdidataitemprops.setProperty("keyid1", sdidataitemDS.getColumnValues("keyid1", ";"));
        sdidataitemprops.setProperty("paramlistid", sdidataitemDS.getColumnValues("paramlistid", ";"));
        sdidataitemprops.setProperty("paramlistversionid", sdidataitemDS.getColumnValues("paramlistversionid", ";"));
        sdidataitemprops.setProperty("variantid", sdidataitemDS.getColumnValues("variantid", ";"));
        sdidataitemprops.setProperty("dataset", sdidataitemDS.getColumnValues("dataset", ";"));
        sdidataitemprops.setProperty("paramid", sdidataitemDS.getColumnValues("paramid", ";"));
        sdidataitemprops.setProperty("paramtype", sdidataitemDS.getColumnValues("paramtype", ";"));
        sdidataitemprops.setProperty("replicateid", sdidataitemDS.getColumnValues("replicateid", ";"));
        sdidataitemprops.setProperty("datatypes", sdidataitemDS.getColumnValues("datatypes", ";"));
        sdidataitemprops.setProperty("entryreftypeid", sdidataitemDS.getColumnValues("entryreftypeid", ";"));
        sdidataitemprops.setProperty("calcrule", sdidataitemDS.getColumnValues("calcrule", ";"));
        sdidataitemprops.setProperty("displayformat", sdidataitemDS.getColumnValues("displayformat", ";"));
        sdidataitemprops.setProperty("displayunits", sdidataitemDS.getColumnValues("displayunits", ";"));
        sdidataitemprops.setProperty("propsmatch", "Y");
        sdidataitemprops.setProperty(PROPERTY_AUDITREASON, properties.getProperty(PROPERTY_AUDITREASON, ""));
        sdidataitemprops.setProperty(PROPERTY_AUDITACTIVITY, properties.getProperty(PROPERTY_AUDITACTIVITY, ""));
        sdidataitemprops.setProperty(PROPERTY_AUDITSIGNEDFLAG, properties.getProperty(PROPERTY_AUDITSIGNEDFLAG, ""));
        sdidataitemprops.setProperty("usersequence", sdidataitemDS.getColumnValues("usersequence", ";"));
        this.getActionProcessor().processAction("ExtendDataSet", "1", sdidataitemprops);
    }

    private void addDSFullArrayItemLevel(String arrayid, DataSet sdidataDS, DataSet sdidataitemDS, Map<String, String> paramdata, String methodid, String methodversionid, String methodinstance, int dataset, Set<String> itemidsSet) {
        String ds = String.valueOf(dataset);
        String sql = "SELECT arrayitemid FROM arrayitem WHERE arrayid=? ";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet arrayitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < arrayitems.size(); ++i) {
            String arrayitemid = arrayitems.getValue(i, "arrayitemid");
            if (!itemidsSet.contains(arrayitemid)) {
                int row = sdidataDS.addRow();
                sdidataDS.setValue(row, "sdcid", "LV_ArrayItem");
                sdidataDS.setValue(row, "keyid1", arrayitemid);
                sdidataDS.setValue(row, "paramlistid", ARRAY_PARAMLIST);
                sdidataDS.setValue(row, "paramlistversionid", "1");
                sdidataDS.setValue(row, "variantid", "1");
                sdidataDS.setValue(row, "dataset", ds);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODID, methodid);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODVERSIONID, methodversionid);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODINSTANCE, methodinstance);
                itemidsSet.add(arrayitemid);
            }
            this.addDataItem(sdidataitemDS, arrayitemid, paramdata, ds);
        }
    }

    private void addDataItem(DataSet sdidataitemDS, String arrayitemid, Map<String, String> paramdata, String ds) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", arrayitemid);
        filter.put("paramid", paramdata.get("paramid"));
        filter.put("paramtype", paramdata.get("paramtype"));
        if (sdidataitemDS.getFilteredDataSet(filter).getRowCount() == 0) {
            int index = sdidataitemDS.addRow();
            String unitsid = paramdata.containsKey("displayunits") ? paramdata.get("displayunits") : (paramdata.containsKey("unitsid") ? paramdata.get("unitsid") : "");
            sdidataitemDS.setValue(index, "sdcid", "LV_ArrayItem");
            sdidataitemDS.setValue(index, "keyid1", arrayitemid);
            sdidataitemDS.setValue(index, "paramlistid", ARRAY_PARAMLIST);
            sdidataitemDS.setValue(index, "paramlistversionid", "1");
            sdidataitemDS.setValue(index, "variantid", "1");
            sdidataitemDS.setValue(index, "dataset", ds);
            sdidataitemDS.setValue(index, "paramid", paramdata.get("paramid"));
            sdidataitemDS.setValue(index, "paramtype", paramdata.get("paramtype"));
            sdidataitemDS.setValue(index, "replicateid", "1");
            sdidataitemDS.setValue(index, "datatypes", paramdata.get("datatypes"));
            sdidataitemDS.setValue(index, "entryreftypeid", paramdata.get("entryreftypeid"));
            sdidataitemDS.setValue(index, "calcrule", paramdata.get("calcrule"));
            sdidataitemDS.setValue(index, "displayformat", paramdata.get("displayformat"));
            sdidataitemDS.setValue(index, "displayunits", unitsid == null ? "" : unitsid.trim());
            sdidataitemDS.setNumber(index, "usersequence", index + 1);
        }
    }

    private void addDSItemLevel(DataSet sdidataDS, DataSet sdidataitemDS, Map<String, String> paramdata, DataSet zoneitemdata, String methodid, String methodversionid, String methodinstance, int dataset, Set<String> itemidsSet) {
        String ds = String.valueOf(dataset);
        for (int i = 0; i < zoneitemdata.size(); ++i) {
            String arrayitemid = zoneitemdata.getValue(i, "arrayitemid");
            if (!itemidsSet.contains(arrayitemid)) {
                int row = sdidataDS.addRow();
                sdidataDS.setValue(row, "sdcid", "LV_ArrayItem");
                sdidataDS.setValue(row, "keyid1", arrayitemid);
                sdidataDS.setValue(row, "paramlistid", ARRAY_PARAMLIST);
                sdidataDS.setValue(row, "paramlistversionid", "1");
                sdidataDS.setValue(row, "variantid", "1");
                sdidataDS.setValue(row, "dataset", ds);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODID, methodid);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODVERSIONID, methodversionid);
                sdidataDS.setValue(row, PROPERTY_ARRAYMETHODINSTANCE, methodinstance);
                itemidsSet.add(arrayitemid);
            }
            this.addDataItem(sdidataitemDS, arrayitemid, paramdata, ds);
        }
    }

    private void addDSZoneLevel(DataSet sdidataDS, DataSet sdidataitemDS, Map<String, String> paramdata, DataSet zoneitemdata, String methodid, String methodversionid, String methodinstance, int dataset, Set<String> zoneidsSet) {
        String ds = String.valueOf(dataset);
        String arrayzoneid = zoneitemdata.getValue(0, "arrayzoneid");
        if (!zoneidsSet.contains(arrayzoneid)) {
            int row = sdidataDS.addRow();
            sdidataDS.setValue(row, "sdcid", "LV_ArrayZone");
            sdidataDS.setValue(row, "keyid1", arrayzoneid);
            sdidataDS.setValue(row, "paramlistid", ARRAY_PARAMLIST);
            sdidataDS.setValue(row, "paramlistversionid", "1");
            sdidataDS.setValue(row, "variantid", "1");
            sdidataDS.setValue(row, "dataset", ds);
            sdidataDS.setValue(row, PROPERTY_ARRAYMETHODID, methodid);
            sdidataDS.setValue(row, PROPERTY_ARRAYMETHODVERSIONID, methodversionid);
            sdidataDS.setValue(row, PROPERTY_ARRAYMETHODINSTANCE, methodinstance);
            zoneidsSet.add(arrayzoneid);
        }
        int index = sdidataitemDS.addRow();
        sdidataitemDS.setValue(index, "sdcid", "LV_ArrayZone");
        sdidataitemDS.setValue(index, "keyid1", arrayzoneid);
        sdidataitemDS.setValue(index, "paramlistid", ARRAY_PARAMLIST);
        sdidataitemDS.setValue(index, "paramlistversionid", "1");
        sdidataitemDS.setValue(index, "variantid", "1");
        sdidataitemDS.setValue(index, "dataset", ds);
        sdidataitemDS.setValue(index, "paramid", paramdata.get("paramid"));
        sdidataitemDS.setValue(index, "paramtype", paramdata.get("paramtype"));
        sdidataitemDS.setValue(index, "replicateid", paramdata.get("replicateid"));
        sdidataitemDS.setValue(index, "datatypes", paramdata.get("datatypes"));
        sdidataitemDS.setValue(index, "entryreftypeid", paramdata.get("entryreftypeid"));
        sdidataitemDS.setValue(index, "calcrule", paramdata.get("calcrule"));
        sdidataitemDS.setValue(index, "displayformat", paramdata.get("displayformat"));
        sdidataitemDS.setValue(index, "displayunits", paramdata.get("displayunits"));
        sdidataitemDS.setNumber(index, "usersequence", index + 1);
    }

    private DataSet getSDIDataDS() {
        DataSet sdidataDS = new DataSet();
        sdidataDS.addColumn("sdcid", 0);
        sdidataDS.addColumn("keyid1", 0);
        sdidataDS.addColumn("keyid2", 0);
        sdidataDS.addColumn("keyid3", 0);
        sdidataDS.addColumn("paramlistid", 0);
        sdidataDS.addColumn("paramlistversionid", 0);
        sdidataDS.addColumn("variantid", 0);
        sdidataDS.addColumn("dataset", 1);
        sdidataDS.addColumn(PROPERTY_ARRAYMETHODID, 0);
        sdidataDS.addColumn(PROPERTY_ARRAYMETHODVERSIONID, 0);
        sdidataDS.addColumn(PROPERTY_ARRAYMETHODINSTANCE, 1);
        return sdidataDS;
    }

    private DataSet getSDIDataItemDS() {
        DataSet sdidataitemDS = new DataSet();
        sdidataitemDS.addColumn("sdcid", 0);
        sdidataitemDS.addColumn("keyid1", 0);
        sdidataitemDS.addColumn("keyid2", 0);
        sdidataitemDS.addColumn("keyid3", 0);
        sdidataitemDS.addColumn("paramlistid", 0);
        sdidataitemDS.addColumn("paramlistversionid", 0);
        sdidataitemDS.addColumn("variantid", 0);
        sdidataitemDS.addColumn("dataset", 0);
        sdidataitemDS.addColumn("paramid", 0);
        sdidataitemDS.addColumn("paramtype", 0);
        sdidataitemDS.addColumn("replicateid", 1);
        sdidataitemDS.addColumn("datatypes", 0);
        sdidataitemDS.addColumn("entryreftypeid", 0);
        sdidataitemDS.addColumn("displayvalue", 0);
        sdidataitemDS.addColumn("displayunits", 0);
        sdidataitemDS.addColumn("displayformat", 0);
        sdidataitemDS.addColumn("calcrule", 0);
        return sdidataitemDS;
    }
}

