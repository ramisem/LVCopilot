/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddOrEditSDI
extends BaseAction
implements sapphire.action.AddOrEditSDI {
    int countdbops = 0;
    String alternatekeycols = "";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean ignoreedits = "Y".equals(properties.getProperty("ignoreedits", "N"));
        this.alternatekeycols = properties.getProperty("alternatekeycol", "");
        properties.deleteProperty("alternatekeycol");
        DataSet ds = this.determineAddEditLists(properties);
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("__op", "Add");
        DataSet addList = ds.getFilteredDataSet(filter);
        if (addList != null && addList.getRowCount() > 0) {
            PropertyList addSDIProp = properties.copy();
            addSDIProp.setProperty("copies", addList.getRowCount() + "");
            String[] cols = addList.getColumns();
            for (int i = 0; i < cols.length; ++i) {
                if (cols[i].startsWith("__")) continue;
                addSDIProp.setProperty(cols[i], addList.getColumnValues(cols[i], ";"));
            }
            this.getActionProcessor().processAction("AddSDI", "1", addSDIProp);
            properties.setProperty("newkeyid1", addSDIProp.getProperty("newkeyid1", ""));
            properties.setProperty("newkeyid2", addSDIProp.getProperty("newkeyid2", ""));
            properties.setProperty("newkeyid3", addSDIProp.getProperty("newkeyid3", ""));
        }
        if (!ignoreedits) {
            filter.put("__op", "Edit");
            DataSet editList = ds.getFilteredDataSet(filter);
            if (editList != null && editList.getRowCount() > 0) {
                PropertyList editSDIProp = properties.copy();
                String[] cols = editList.getColumns();
                for (int i = 0; i < cols.length; ++i) {
                    if (cols[i].startsWith("__")) continue;
                    editSDIProp.setProperty(cols[i], editList.getColumnValues(cols[i], ";"));
                }
                editSDIProp.deleteProperty("templateid");
                editSDIProp.deleteProperty("templatekeyid1");
                editSDIProp.deleteProperty("templatekeyid2");
                editSDIProp.deleteProperty("templatekeyid3");
                editSDIProp.deleteProperty("templatekeyid3");
                editSDIProp.deleteProperty("applylock");
                editSDIProp.deleteProperty("templateflag");
                editSDIProp.deleteProperty("addtemplateroles");
                editSDIProp.deleteProperty("usetemplatedepartment");
                editSDIProp.deleteProperty("applyworkitems");
                editSDIProp.deleteProperty("forcenew");
                editSDIProp.deleteProperty("ignoreedits");
                this.getActionProcessor().processAction("EditSDI", "1", editSDIProp);
            }
        }
        if ("Y".equals(properties.getProperty("debug", "N"))) {
            properties.setProperty("dbopcount", "" + this.countdbops);
        }
    }

    private DataSet determineAddEditLists(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        this.logger.info("Getting SDC information");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
        if (sdc == null) {
            throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
        }
        PropertyListCollection columns = sdc.getCollection("columns");
        String columnids = "";
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            columnids = columnids + ";" + column.getProperty("columnid").toLowerCase();
        }
        columnids = columnids.length() > 0 ? columnids.substring(1) : columnids;
        DataSet ds = new DataSet();
        Set propnames = properties.keySet();
        Iterator iter = propnames.iterator();
        String curr = "";
        String val = "";
        while (iter.hasNext()) {
            curr = iter.next().toString();
            val = properties.getProperty(curr, "");
            if (val.length() <= 0 || !val.contains(";") && !curr.equals("keyid1") && !curr.equals("keyid2") && !curr.equals("keyid3") && Arrays.asList(columnids.split(";")).indexOf(curr) == -1) continue;
            ds.addColumnValues(curr, 0, val, ";");
            if (val.contains(";")) continue;
            ds.padColumn(curr);
        }
        ds.setSequence("__sequence");
        ds.setString(-1, "__op", "Add");
        int total = ds.getRowCount();
        int batchsize = 90;
        for (int startpos = 0; startpos < total; startpos += batchsize) {
            if (startpos + batchsize > total) {
                this.determineAddOrEdit(sdcid, ds, startpos, total - startpos);
                continue;
            }
            this.determineAddOrEdit(sdcid, ds, startpos, batchsize);
        }
        return ds;
    }

    private void determineAddOrEdit(String sdcid, DataSet ds, int startpos, int length) throws SapphireException {
        try {
            Trace.log("Calling validSDIList on items " + startpos + " to " + startpos + length);
            String keyid1 = this.getBlockValues(ds, "keyid1", startpos, length);
            String keyid2 = this.getBlockValues(ds, "keyid2", startpos, length);
            String keyid3 = this.getBlockValues(ds, "keyid3", startpos, length);
            ++this.countdbops;
            String callstmt = "";
            String retkeyid1 = "";
            String retkeyid2 = "";
            String retkeyid3 = "";
            if (!"".equalsIgnoreCase(this.alternatekeycols)) {
                this.logger.info("Getting SDC information");
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
                if (sdc == null) {
                    throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
                }
                String tableid = sdc.getProperty("tableid");
                String keycolid1 = sdc.getProperty("keycolid1");
                String[] alternatekeycolsArray = StringUtil.split(this.alternatekeycols, ";");
                String sql = "";
                if (alternatekeycolsArray.length > 1) {
                    String alternatekeycolsrowvalue = "";
                    String alternatekeycolsInWhereClause = "";
                    for (int i = startpos; i < startpos + length; ++i) {
                        String alternatekeycolsvalue = "";
                        for (int j = 0; j < alternatekeycolsArray.length; ++j) {
                            alternatekeycolsvalue = alternatekeycolsvalue + ds.getValue(i, alternatekeycolsArray[j]) + "#@#";
                        }
                        alternatekeycolsrowvalue = alternatekeycolsrowvalue + ";" + alternatekeycolsvalue.substring(0, alternatekeycolsvalue.length() - 3);
                    }
                    alternatekeycolsrowvalue = alternatekeycolsrowvalue.substring(1);
                    alternatekeycolsInWhereClause = StringUtil.replaceAll(alternatekeycolsrowvalue, ";", "','");
                    sql = "select count(*) count,min(" + keycolid1 + ") as keyid1 ," + StringUtil.replaceAll(this.alternatekeycols, ";", ",") + " from " + tableid + " where " + StringUtil.replaceAll(this.alternatekeycols, ";", this.connectionInfo.isOracle() ? "|| '#@#' ||" : "+ '#@#' +") + " in ('" + alternatekeycolsInWhereClause + "')  group by " + StringUtil.replaceAll(this.alternatekeycols, ";", ",") + " order by " + StringUtil.replaceAll(this.alternatekeycols, ";", ",");
                } else {
                    sql = "select count(*) count,min(" + keycolid1 + ") as keyid1 ," + this.alternatekeycols + " from " + tableid + " where " + this.alternatekeycols + " in ('" + StringUtil.replaceAll(ds.getColumnValues(this.alternatekeycols, startpos, startpos + length, ";"), ";", "','") + "')  group by " + this.alternatekeycols + " order by " + this.alternatekeycols;
                }
                DataSet dsAlternateKeyCols = this.getQueryProcessor().getSqlDataSet(sql);
                if (dsAlternateKeyCols != null && dsAlternateKeyCols.getRowCount() > 0) {
                    for (int i = 0; i < dsAlternateKeyCols.getRowCount(); ++i) {
                        int count = Integer.parseInt(dsAlternateKeyCols.getValue(i, "count"));
                        if (count > 1) {
                            throw new SapphireException("GENERAL_ERROR", "Duplicate Record found for specified alternate key column(s): " + this.alternatekeycols);
                        }
                        if (count != 1) continue;
                        HashMap<String, String> hmfilter = new HashMap<String, String>();
                        if (alternatekeycolsArray.length > 1) {
                            for (int k = 0; k < alternatekeycolsArray.length; ++k) {
                                hmfilter.put(alternatekeycolsArray[k], dsAlternateKeyCols.getValue(i, alternatekeycolsArray[k]));
                            }
                        } else {
                            hmfilter.put(this.alternatekeycols, dsAlternateKeyCols.getValue(i, this.alternatekeycols));
                        }
                        int rowidinolddataset = ds.findRow(hmfilter);
                        ds.addColumn("keyid1", 0);
                        ds.setValue(rowidinolddataset, "keyid1", dsAlternateKeyCols.getValue(i, "keyid1"));
                        retkeyid1 = retkeyid1 + ";" + dsAlternateKeyCols.getValue(i, "keyid1");
                    }
                }
                retkeyid1 = retkeyid1.length() > 0 ? retkeyid1.substring(1) : retkeyid1;
            } else {
                callstmt = "{? = call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "ValidSDIList( ?, ?, ?, ? ,?, ?, ?  ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.registerOutParameter(1, 2);
                cs.setString(2, sdcid);
                cs.setString(3, keyid1);
                cs.setString(4, keyid2);
                cs.setString(5, keyid3);
                cs.registerOutParameter(6, 12);
                cs.registerOutParameter(7, 12);
                cs.registerOutParameter(8, 12);
                cs.executeUpdate();
                retkeyid1 = cs.getString(6);
                retkeyid2 = cs.getString(7);
                retkeyid3 = cs.getString(8);
            }
            String[] retkeyid1list = null;
            if (retkeyid1 != null && retkeyid1.length() > 0) {
                retkeyid1list = StringUtil.split(retkeyid1, ";");
            }
            String[] retkeyid2list = null;
            if (retkeyid2 != null && retkeyid2.length() > 0) {
                retkeyid2list = StringUtil.split(retkeyid2, ";");
            }
            String[] retkeyid3list = null;
            if (retkeyid3 != null && retkeyid3.length() > 0) {
                retkeyid3list = StringUtil.split(retkeyid3, ";");
            }
            if (retkeyid1list != null) {
                block6: for (int i = startpos; i < startpos + length; ++i) {
                    String currkey1 = ds.getString(i, "keyid1").trim();
                    String currkey2 = ds.getString(i, "keyid2");
                    String currkey3 = ds.getString(i, "keyid3");
                    for (int j = 0; j < retkeyid1list.length; ++j) {
                        if (!retkeyid1list[j].equals(currkey1) || retkeyid2list != null && !retkeyid2list[j].equals(currkey2.trim()) || retkeyid3list != null && !retkeyid3list[j].equals(currkey3.trim())) continue;
                        ds.setString(i, "__op", "Edit");
                        continue block6;
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to check if validSDIList" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    private String getBlockValues(DataSet ds, String colname, int startpos, int length) {
        String all = ds.getColumnValues(colname, ";");
        String[] arr = StringUtil.split(all, ";");
        String ret = "";
        for (int i = startpos; i < startpos + length; ++i) {
            if (i != startpos) {
                ret = ret + ";";
            }
            ret = ret + arr[i].trim();
        }
        return ret;
    }
}

