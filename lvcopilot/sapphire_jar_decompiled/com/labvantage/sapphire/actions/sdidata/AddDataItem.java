/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddDataItem
extends BaseSDIDataAction
implements sapphire.action.AddDataItem {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String rsetid;
        String sdcid = properties.getProperty("sdcid");
        String paramlistcheck = properties.getProperty("paramlistcheck");
        String propsmatch = properties.getProperty("propsmatch");
        String separator = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        HashMap<String, String[]> dataitemvalues = null;
        DataSet sdidatasets = new DataSet();
        String rsetKeyid1 = properties.getProperty("keyid1");
        String rsetKeyid2 = properties.getProperty("keyid2");
        String rsetKeyid3 = properties.getProperty("keyid3");
        if (!";".equals(separator)) {
            rsetKeyid1 = StringUtil.replaceAll(rsetKeyid1, separator, ";");
            rsetKeyid2 = StringUtil.replaceAll(rsetKeyid2, separator, ";");
            rsetKeyid3 = StringUtil.replaceAll(rsetKeyid3, separator, ";");
        }
        if ((rsetid = this.getRSet(properties.getProperty("sdcid"), rsetKeyid1, rsetKeyid2, rsetKeyid3, properties.getProperty("applylock").equals("Y"))).length() <= 0) throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET whilst adding dataitems");
        this.loadSDIDataSets(rsetid, sdidatasets);
        Calendar now = DateTimeUtil.getNowCalendar();
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
        String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), separator);
        String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), separator);
        String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), separator);
        String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), separator);
        String[] paramids = null;
        String[] paramtypes = null;
        String[] numreplicates = null;
        DataSet dataitem = new DataSet(this.connectionInfo);
        DataSet datalimits = new DataSet();
        if (paramlistcheck.equalsIgnoreCase("N")) {
            int sdccols;
            paramids = StringUtil.split(properties.getProperty("paramid"), separator);
            paramtypes = StringUtil.split(properties.getProperty("paramtype"), separator);
            numreplicates = StringUtil.split(properties.getProperty("numreplicate"), separator);
            dataitemvalues = new HashMap<String, String[]>();
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PropertyListCollection columns = sdcProcessor.getColumns("DataItem");
            int n = sdccols = columns != null ? columns.size() : 0;
            if (sdccols <= 0) throw new SapphireException("INVALID_PROPERTY", "SDC Information not found for " + sdcid);
            String columnid = "";
            String pkflag = "";
            String datatype = "";
            String value = "";
            long length = 0L;
            Object[] noneditable = new String[]{"displayvalue", "enteredqualifier", "enteredoperator", "enteredtext", "enteredunits", "enteredvalue", "textcolor", "transformdt", "transformtext", "transformvalue", "valuestatus"};
            for (int sdccol = 0; sdccol < sdccols; ++sdccol) {
                PropertyList column = columns.getPropertyList(sdccol);
                columnid = column.getProperty("columnid").toLowerCase();
                pkflag = column.getProperty("pkflag");
                datatype = column.getProperty("datatype");
                value = "";
                if (pkflag.equals("Y") || (length = StringUtil.getLen(value = properties.getProperty(columnid))) <= 0L || Arrays.binarySearch(noneditable, value) >= 0) continue;
                dataitemvalues.put(columnid, StringUtil.split(value, separator));
                if (datatype.equals("C")) {
                    dataitem.addColumn(columnid, 0);
                    continue;
                }
                if (datatype.equals("N") || datatype.equals("R")) {
                    dataitem.addColumn(columnid, 1);
                    continue;
                }
                if (!datatype.equals("D")) continue;
                dataitem.addColumn(columnid, 2);
                if (!"Y".equals(this.getSDCProcessor().getSDCColumnProperty("DataItem", columnid, "timezoneindependent"))) continue;
                dataitem.setTimeZoneInsensitive(columnid);
            }
        }
        DataSet paramlist = new DataSet();
        DataSet paramlistitem = new DataSet();
        DataSet paramlimits = new DataSet();
        DataSet approvalsteps = new DataSet();
        DataSet sdidatacrosssdicalcRules = new DataSet();
        HashMap<String, Object> findmap = new HashMap<String, Object>();
        HashMap<String, String> filtermap = new HashMap<String, String>();
        HashMap replicateMap = this.getReplicateMap(rsetid);
        if (propsmatch.equalsIgnoreCase("Y") && paramlistcheck.equalsIgnoreCase("N") && properties.getProperty("keyid1").indexOf(separator) > 0) {
            this.addDataitemsNolist(dataitem, sdidatasets, sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), properties.getProperty("paramlistid"), properties.getProperty("paramlistversionid"), properties.getProperty("variantid"), properties.getProperty("dataset"), paramids, paramtypes, numreplicates, now, replicateMap, dataitemvalues, separator);
        } else {
            boolean enterDefaultValue = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("enterdefaultvaluesonadd").equals("Y");
            for (int pl = 0; pl < paramlistidprop.length; ++pl) {
                String paramlistid = paramlistidprop[pl];
                String paramlistversionid = paramlistversionidprop.length == 0 || paramlistversionidprop.length < paramlistidprop.length || paramlistversionidprop[pl].length() == 0 ? "1" : paramlistversionidprop[pl];
                String variantid = variantidprop.length == 0 || variantidprop.length < paramlistidprop.length || variantidprop[pl].length() == 0 ? "" : variantidprop[pl];
                String datasetstr = datasetprop.length == 0 || datasetprop.length < paramlistidprop.length || datasetprop[pl].length() == 0 ? "0" : datasetprop[pl];
                int datasetnum = Integer.parseInt(datasetstr);
                findmap.put("paramlistid", paramlistid);
                findmap.put("paramlistversionid", paramlistversionid);
                findmap.put("variantid", variantid);
                findmap.put("dataset", new BigDecimal(datasetnum));
                for (int i = 0; i < keyid1prop.length; ++i) {
                    String keyid3;
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String string = keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    if (paramlistcheck.equalsIgnoreCase("N")) {
                        this.addDataitemsNolist(dataitem, sdidatasets, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetstr, paramids, paramtypes, numreplicates, now, replicateMap, dataitemvalues, separator);
                    } else {
                        this.loadParamList(paramlistid, paramlistversionid, variantid, paramlist, paramlistitem, paramlimits, approvalsteps, null, sdidatacrosssdicalcRules, sdcid);
                        if (datasetnum > 0) {
                            findmap.put("keyid1", keyid1);
                            findmap.put("keyid2", keyid2);
                            findmap.put("keyid3", keyid3);
                            int findrow = sdidatasets.findRow(findmap);
                            if (findrow < 0) throw new SapphireException("INVALID_DATASETNUM", "Failed to find dataset with number: " + String.valueOf(datasetnum));
                            this.logger.info("Adding dataitems to dataset: " + String.valueOf(datasetnum));
                            this.addDataitems(dataitem, datalimits, paramlistitem, paramlimits, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetnum, properties.getProperty("paramid"), properties.getProperty("paramtype"), now, replicateMap, enterDefaultValue);
                        } else {
                            this.logger.info("Adding dataitems to all datasets..." + String.valueOf(sdidatasets.getRowCount()));
                            filtermap.put("keyid1", keyid1);
                            filtermap.put("keyid2", keyid2);
                            filtermap.put("keyid3", keyid3);
                            DataSet filteredsdidatasets = sdidatasets.getFilteredDataSet(filtermap);
                            for (int ds = 0; ds < filteredsdidatasets.getRowCount(); ++ds) {
                                this.addDataitems(dataitem, datalimits, paramlistitem, paramlimits, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, filteredsdidatasets.getInt(ds, "dataset"), properties.getProperty("paramid"), properties.getProperty("paramtype"), now, replicateMap, enterDefaultValue);
                            }
                        }
                    }
                    paramlist.reset();
                    paramlistitem.reset();
                    paramlimits.reset();
                    approvalsteps.reset();
                }
            }
        }
        if (properties.getProperty("tracelogid", "").trim().length() == 0) {
            dataitem.setString(-1, "tracelogid", this.getTracelogid(sdcid, "Added dataitems", properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt")));
        } else {
            dataitem.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
        }
        DataSetUtil.insert(this.database, dataitem, "sdidataitem");
        DataSetUtil.insert(this.database, datalimits, "sdidataitemlimits");
        StringBuffer sql = new StringBuffer(" INSERT INTO sdidataitemspec (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, ");
        sql.append(" variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, usersequence, reportflag ) ");
        sql.append(" ( ");
        sql.append(" SELECT sdidataitem.sdcid,sdidataitem.keyid1,sdidataitem.keyid2,sdidataitem.keyid3, ");
        sql.append(" sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, ");
        sql.append(" sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid, ");
        sql.append(" sdispec.specid, sdispec.specversionid, specparamitems.usersequence, specparamitems.reportflag ");
        sql.append(" FROM sdidataitem, sdispec, specparamitems ");
        sql.append(" WHERE sdidataitem.sdcid = '").append(sdcid).append("' ");
        sql.append(" AND sdidataitem.keyid1=? ");
        sql.append(" AND sdidataitem.keyid2=? ");
        sql.append(" AND sdidataitem.keyid3=? ");
        sql.append(" AND sdidataitem.paramlistid=? ");
        sql.append(" AND sdidataitem.paramlistversionid=? ");
        sql.append(" AND sdidataitem.variantid=? ");
        sql.append(" AND sdidataitem.dataset=? ");
        sql.append(" AND sdidataitem.paramid=? ");
        sql.append(" AND sdidataitem.paramtype=? ");
        sql.append(" AND sdidataitem.replicateid=? ");
        sql.append(" AND sdispec.sdcid  = sdidataitem.sdcid ");
        sql.append(" AND sdispec.keyid1 = sdidataitem.keyid1 ");
        sql.append(" AND sdispec.keyid2 = sdidataitem.keyid2 ");
        sql.append(" AND sdispec.keyid3 = sdidataitem.keyid3 ");
        sql.append(" AND specparamitems.specid = sdispec.specid ");
        sql.append(" AND specparamitems.specversionid = sdispec.specversionid ");
        sql.append(" AND sdidataitem.paramid = specparamitems.paramid ");
        sql.append(" AND sdidataitem.paramtype = specparamitems.paramtype ");
        sql.append(" AND ");
        sql.append(" ( ");
        sql.append(" ( ");
        sql.append(" specparamitems.allowanyparamlistflag = 'Y' ");
        sql.append(" ) ");
        sql.append(" OR ");
        sql.append(" ( ");
        sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
        sql.append(" AND sdidataitem.paramlistversionid = specparamitems.paramlistversionid ");
        sql.append(" AND sdidataitem.variantid = specparamitems.variantid ");
        sql.append(" AND (specparamitems.allowanyparamlistflag = 'N' OR specparamitems.allowanyparamlistflag is null OR specparamitems.allowanyparamlistflag = '') ");
        sql.append(" ) ");
        sql.append(" OR ");
        sql.append(" ( ");
        sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
        sql.append(" AND sdidataitem.variantid = specparamitems.variantid  ");
        sql.append(" AND specparamitems.allowanyparamlistflag = 'V' ");
        sql.append(" ) ");
        sql.append(" OR ");
        sql.append(" ( ");
        sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
        sql.append(" AND specparamitems.allowanyparamlistflag = 'A' ");
        sql.append(" ) ");
        sql.append(" ) ");
        sql.append(" ) ");
        this.logger.debug("sql = " + sql.toString());
        try {
            PreparedStatement ps = this.database.prepareStatement(sql.toString());
            int rows = dataitem.getRowCount();
            for (int row = 0; row < rows; ++row) {
                ps.setString(1, dataitem.getString(row, "keyid1"));
                ps.setString(2, dataitem.getString(row, "keyid2"));
                ps.setString(3, dataitem.getString(row, "keyid3"));
                ps.setString(4, dataitem.getString(row, "paramlistid"));
                ps.setString(5, dataitem.getString(row, "paramlistversionid"));
                ps.setString(6, dataitem.getString(row, "variantid"));
                ps.setBigDecimal(7, dataitem.getBigDecimal(row, "dataset"));
                ps.setString(8, dataitem.getString(row, "paramid"));
                ps.setString(9, dataitem.getString(row, "paramtype"));
                ps.setBigDecimal(10, dataitem.getBigDecimal(row, "replicateid"));
                try {
                    ps.executeUpdate();
                    continue;
                }
                catch (Exception e) {
                    this.logger.info("Failed to add dataitem spec into " + dataitem.getString(row, "keyid1") + " for " + dataitem.getString(row, "paramlistid") + "[" + dataitem.getString(row, "paramid") + "]" + e.getMessage() + " " + e);
                }
            }
            this.database.closeStatement();
        }
        catch (SQLException e) {
            throw new SapphireException("RECONCILE_DI_SPECS", "Exception generated trying to add dataitem spec information", e);
        }
        this.getDAMProcessor().clearRSet(rsetid);
    }

    private HashMap getReplicateMap(String rsetid) throws SapphireException {
        HashMap<String, String> replicateMap = null;
        String selectDatasets = "SELECT\tsdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3, sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid FROM\tsdidataitem, rsetitems WHERE\trsetitems.rsetid = ? AND \t\trsetitems.sdcid  = sdidataitem.sdcid AND \t\trsetitems.keyid1 = sdidataitem.keyid1 AND \t\trsetitems.keyid2 = sdidataitem.keyid2 AND \t\trsetitems.keyid3 = sdidataitem.keyid3 ORDER BY replicateid";
        this.database.createPreparedResultSet("SelectDataSets", selectDatasets, new Object[]{rsetid});
        DataSet ds = new DataSet(this.database.getResultSet("SelectDataSets"));
        if (ds.size() > 0) {
            replicateMap = new HashMap<String, String>();
            for (int i = 0; i < ds.size(); ++i) {
                replicateMap.put(ds.getValue(i, "keyid1") + ds.getValue(i, "keyid2") + ds.getValue(i, "keyid3") + ds.getValue(i, "paramlistid") + ds.getValue(i, "paramlistversionid") + ds.getValue(i, "variantid") + ds.getValue(i, "dataset") + ds.getValue(i, "paramid") + ds.getValue(i, "paramtype"), ds.getValue(i, "replicateid"));
            }
        }
        return replicateMap;
    }

    private void addDataitemsNolist(DataSet dataitem, DataSet sdidatasets, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String[] paramids, String[] paramtypes, String[] numreplicates, Calendar now, HashMap replicateMap, HashMap dataitemvalues, String delimeter) {
        boolean propsmatch = false;
        DataSet propsds = null;
        if (keyid1.indexOf(delimeter) > 0) {
            propsmatch = true;
            propsds = new DataSet();
            propsds.addColumnValues("keyid1", 0, keyid1, delimeter);
            propsds.addColumnValues("keyid2", 0, keyid2, delimeter, "(null)");
            propsds.addColumnValues("keyid3", 0, keyid3, delimeter, "(null)");
            propsds.addColumnValues("paramlistid", 0, paramlistid, delimeter);
            propsds.addColumnValues("paramlistversionid", 0, paramlistversionid, delimeter);
            propsds.addColumnValues("variantid", 0, variantid, delimeter);
            propsds.addColumnValues("dataset", 0, dataset, delimeter);
            propsds.padColumns();
        }
        Set keySet = dataitemvalues.keySet();
        if (paramids.length > 0 && paramids[0].length() > 0 && paramtypes.length > 0 && paramids.length == paramtypes.length) {
            for (int dataitems = 0; dataitems < paramids.length; ++dataitems) {
                if (propsmatch) {
                    keyid1 = propsds.getString(dataitems, "keyid1");
                    keyid2 = propsds.getString(dataitems, "keyid2");
                    keyid3 = propsds.getString(dataitems, "keyid3");
                    paramlistid = propsds.getString(dataitems, "paramlistid");
                    paramlistversionid = propsds.getString(dataitems, "paramlistversionid");
                    variantid = propsds.getString(dataitems, "variantid");
                    dataset = propsds.getString(dataitems, "dataset");
                }
                boolean itemNotExist = true;
                if (replicateMap != null) {
                    String repid = "";
                    repid = (String)replicateMap.get(keyid1 + keyid2 + keyid3 + paramlistid + paramlistversionid + variantid + dataset + paramids[dataitems] + paramtypes[dataitems]);
                    if (repid != null && repid.length() > 0) {
                        itemNotExist = false;
                    }
                }
                int reps = 1;
                try {
                    reps = numreplicates.length > dataitems ? Integer.parseInt(numreplicates[dataitems]) : Integer.parseInt(numreplicates[numreplicates.length - 1]);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                if (itemNotExist) {
                    HashMap<String, Object> findmap = new HashMap<String, Object>();
                    findmap.put("keyid1", keyid1);
                    findmap.put("keyid2", keyid2);
                    findmap.put("keyid3", keyid3);
                    findmap.put("paramlistid", paramlistid);
                    findmap.put("paramlistversionid", paramlistversionid);
                    findmap.put("variantid", variantid);
                    findmap.put("dataset", new BigDecimal(dataset));
                    if (sdidatasets.findRow(findmap) >= 0) {
                        for (int rep = 0; rep < reps; ++rep) {
                            int newdi = dataitem.addRow();
                            dataitem.setString(newdi, "sdcid", sdcid);
                            dataitem.setString(newdi, "keyid1", keyid1);
                            dataitem.setString(newdi, "keyid2", keyid2);
                            dataitem.setString(newdi, "keyid3", keyid3);
                            dataitem.setString(newdi, "paramlistid", paramlistid);
                            dataitem.setString(newdi, "paramlistversionid", paramlistversionid);
                            dataitem.setString(newdi, "variantid", variantid);
                            dataitem.setNumber(newdi, "dataset", new BigDecimal(dataset));
                            dataitem.setString(newdi, "paramid", paramids[dataitems]);
                            dataitem.setString(newdi, "paramtype", paramtypes[dataitems]);
                            dataitem.setNumber(newdi, "replicateid", rep + 1);
                            dataitem.setString(newdi, "mandatoryflag", "N");
                            dataitem.setString(newdi, "datatypes", "N");
                            dataitem.setString(newdi, "releasedflag", "N");
                            dataitem.setString(newdi, "transformdeferflag", "N");
                            for (String columnid : keySet) {
                                String[] values = (String[])dataitemvalues.get(columnid);
                                String value = values.length > dataitems ? values[dataitems] : values[values.length - 1];
                                dataitem.setValue(newdi, columnid, value);
                            }
                            dataitem.setDate(newdi, "createdt", now);
                            dataitem.setString(newdi, "createby", this.connectionInfo.getSysuserId());
                            dataitem.setString(newdi, "createtool", this.connectionInfo.getTool());
                            dataitem.setDate(newdi, "moddt", now);
                            dataitem.setString(newdi, "modby", this.connectionInfo.getSysuserId());
                            dataitem.setString(newdi, "modtool", this.connectionInfo.getTool());
                            this.logger.info("Adding " + paramids[dataitems] + " rep " + (rep + 1) + " to " + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset);
                        }
                        continue;
                    }
                    this.logger.info("DataSet not exist. Not Adding " + paramids[dataitems] + " to " + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset);
                    continue;
                }
                this.logger.info("DataItem already exist. Not Adding " + paramids[dataitems] + " to " + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset);
            }
        } else {
            this.logger.info("No dataitems specified to add for " + paramlistid);
        }
    }
}

