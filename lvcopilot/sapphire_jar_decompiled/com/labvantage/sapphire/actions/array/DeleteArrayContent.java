/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteArrayContent
extends BaseAction
implements ArrayConstants,
sapphire.action.DeleteArrayContent {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String ITEMLINKID = "Array Item Content";
    private static final String ZONELINKID = "Array Zone Content";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.fetchArrayContentData(properties);
        this.deleteArrayContent(properties);
    }

    private String fetchArrayContentData(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayitemidlist = properties.getProperty("arrayitemid", "");
        String arrayitemlabel = properties.getProperty("arrayitemlabel", "");
        String level = properties.getProperty("level", "item");
        String contentIds = "";
        if ("item".equals(level)) {
            if (arrayitemidlist.length() == 0) {
                int i;
                if (arrayitemlabel.length() > 0) {
                    String[] labellist = StringUtil.split(arrayitemlabel, ";");
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT arrayitemid, itemlabel FROM arrayitem WHERE arrayitemid like " + safeSQL.addVar(arrayid + "%");
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    for (i = 0; i < labellist.length; ++i) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put("itemlabel", labellist[i]);
                        DataSet match = ret.getFilteredDataSet(filter);
                        if (match.getRowCount() == 0) {
                            throw new SapphireException("Invalid label specified:" + labellist[i]);
                        }
                        if (arrayitemidlist.length() > 0) {
                            arrayitemidlist = arrayitemidlist + ";";
                        }
                        arrayitemidlist = arrayitemidlist + match.getString(0, "arrayitemid");
                    }
                } else {
                    String yposs = properties.getProperty("ypos", "");
                    String xposs = properties.getProperty("xpos", "");
                    if (yposs.length() == 0 || xposs.length() == 0) {
                        throw new SapphireException("Arrayitemid/arrayitemlabel should be specified.");
                    }
                    String[] yposition = StringUtil.split(yposs, ";");
                    String[] xposition = StringUtil.split(xposs, ";");
                    for (i = 0; i < yposition.length; ++i) {
                        if (arrayitemidlist.length() > 0) {
                            arrayitemidlist = arrayitemidlist + ";";
                        }
                        arrayitemidlist = arrayitemidlist + arrayid + "_" + xposition[i] + "_" + yposition[i];
                    }
                }
            }
            String contentsdcid = properties.getProperty("contentsdcid");
            String contentkeyid1 = properties.getProperty("contentkeyid1");
            String contentkeyid2 = properties.getProperty("contentkeyid2", "");
            String contentkeyid3 = properties.getProperty("contentkeyid3", "");
            String[] arrayitemids = StringUtil.split(arrayitemidlist, ";");
            String[] contentsdcids = StringUtil.split(contentsdcid, ";");
            String[] contentkeyid1s = StringUtil.split(contentkeyid1, ";");
            String[] contentkeyid2s = StringUtil.split(contentkeyid2, ";");
            String[] contentkeyid3s = StringUtil.split(contentkeyid3, ";");
            String volumeunits = "";
            int expectedcount = arrayitemids.length;
            if (contentsdcids.length == 1) {
                contentsdcid = PropertyUtil.repeat(contentsdcid, expectedcount);
                contentsdcids = StringUtil.split(contentsdcid, ";");
            }
            if (contentkeyid1s.length != expectedcount || contentsdcids.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of items specified");
            }
            int maxkeycombcount = 1;
            maxkeycombcount = contentkeyid2.equalsIgnoreCase("") ? maxkeycombcount : 2;
            maxkeycombcount = contentkeyid3.equalsIgnoreCase("") ? maxkeycombcount : 3;
            HashMap coorVolMap = new HashMap();
            String volumes = "";
            for (int i = 0; i < arrayitemids.length; ++i) {
                DataSet data;
                SafeSQL safeSQL;
                String keyid2;
                String contsdcid = contentsdcids[i];
                String[] tokens = StringUtil.split(arrayitemids[i], "_");
                String keyid1 = contentkeyid1s[i];
                String string = !contentkeyid2.equalsIgnoreCase("") ? (contentkeyid2s[i].equalsIgnoreCase("") ? null : contentkeyid2s[i]) : (keyid2 = null);
                String keyid3 = !contentkeyid3.equalsIgnoreCase("") ? (contentkeyid3s[i].equalsIgnoreCase("") ? null : contentkeyid3s[i]) : null;
                String selectDeletedContentSQL = "select * from arrayitemcontent where arrayitemid = ?  and contentsdcid = ? and contentkeyid1 = ? ";
                if (maxkeycombcount == 1) {
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                } else if (maxkeycombcount == 2) {
                    if (keyid2 != null) {
                        selectDeletedContentSQL = selectDeletedContentSQL + "and contentkeyid2 = ? ";
                        safeSQL = new SafeSQL();
                        safeSQL.addVar(arrayitemids[i]);
                        safeSQL.addVar(contsdcid);
                        safeSQL.addVar(keyid1);
                        safeSQL.addVar(keyid2);
                        data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                    } else {
                        safeSQL = new SafeSQL();
                        safeSQL.addVar(arrayitemids[i]);
                        safeSQL.addVar(contsdcid);
                        safeSQL.addVar(keyid1);
                        data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                    }
                } else if (keyid3 != null) {
                    selectDeletedContentSQL = selectDeletedContentSQL + "and contentkeyid2 = ? and contentkeyid3 = ?";
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    safeSQL.addVar(keyid2);
                    safeSQL.addVar(keyid3);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                } else if (keyid2 != null) {
                    selectDeletedContentSQL = selectDeletedContentSQL + "and contentkeyid2 = ? ";
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    safeSQL.addVar(keyid2);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                } else {
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                }
                if (data != null) {
                    double deleltedQuantity = -data.getDouble(0, "volume", 0.0);
                    if (volumes.length() > 0) {
                        volumes = volumes + ";";
                    }
                    volumes = volumes + deleltedQuantity;
                    contentIds = contentIds + ";" + data.getValue(0, "arrayitemcontentid");
                    if (volumeunits.length() > 0) {
                        volumeunits = volumeunits + ";";
                    }
                    volumeunits = volumeunits + data.getValue(0, "volumeunits");
                }
                properties.setProperty("arrayitemid", arrayitemidlist);
            }
            if (contentIds != null && contentIds.length() > 0) {
                contentIds = contentIds.substring(1);
            }
        } else {
            String zone = properties.getProperty("zone", "");
            String contentsdcids = properties.getProperty("contentsdcid", "");
            String contentkeyid1s = properties.getProperty("contentkeyid1", "");
            String[] contentsdcid = StringUtil.split(contentsdcids, ";");
            String[] contentkeyid1 = StringUtil.split(contentkeyid1s, ";");
            String arrayzoneid = "";
            String volume = "";
            String volumeunits = "";
            for (int i = 0; i < contentsdcid.length; ++i) {
                String selectDeletedContentSQL = "select * from arrayzonecontent where arrayzoneid IN ( SELECT arrayzoneid FROM arrayzone WHERE arrayid = ? and zone = ? ) and contentsdcid = ? and contentkeyid1 = ? ";
                SafeSQL safeSQL = new SafeSQL();
                safeSQL.addVar(arrayid);
                safeSQL.addVar(zone);
                safeSQL.addVar(contentsdcid[i]);
                safeSQL.addVar(contentkeyid1[i]);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(selectDeletedContentSQL, safeSQL.getValues());
                if (arrayzoneid.length() > 0) {
                    arrayzoneid = arrayzoneid + ";";
                    volume = volume + ";";
                    volumeunits = volumeunits + ";";
                }
                arrayzoneid = arrayzoneid + ds.getString(0, "arrayzoneid", "");
                volume = volume + "-" + ds.getValue(0, "volume", "0.0");
                volumeunits = volumeunits + ds.getValue(0, "volumeunits", "");
            }
        }
        return contentIds;
    }

    private void deleteArrayContent(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayitemids = properties.getProperty("arrayitemid");
        String contentsdcid = properties.getProperty("contentsdcid");
        String contentkeyid1 = properties.getProperty("contentkeyid1");
        String contentkeyid2 = properties.getProperty("contentkeyid2");
        String contentkeyid3 = properties.getProperty("contentkeyid3");
        String level = properties.getProperty("level", "item");
        if ("item".equals(level)) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet allContent = this.getQueryProcessor().getPreparedSqlDataSet("SELECT arrayitemid, arrayitemcontentid, contentsdcid, contentkeyid1, contentkeyid2, contentkeyid3 FROM arrayitemcontent WHERE arrayitemid like " + safeSQL.addVar(arrayid + "%"), safeSQL.getValues());
            DataSet allData = new DataSet();
            allData.addColumnValues("arrayitemid", 0, arrayitemids, ";");
            allData.addColumnValues("contentsdcid", 0, contentsdcid, ";");
            allData.addColumnValues("contentkeyid1", 0, contentkeyid1, ";");
            allData.addColumnValues("contentkeyid2", 0, contentkeyid2, ";");
            allData.addColumnValues("contentkeyid3", 0, contentkeyid3, ";");
            allData.addColumn("arrayitemcontentid", 0);
            block0: for (int i = 0; i < allData.getRowCount(); ++i) {
                for (int j = 0; j < allContent.getRowCount(); ++j) {
                    if (!allContent.getString(j, "arrayitemid").equals(allData.getString(i, "arrayitemid")) || !allContent.getString(j, "contentsdcid").equals(allData.getString(i, "contentsdcid")) || !allContent.getString(j, "contentkeyid1").equals(allData.getString(i, "contentkeyid1"))) continue;
                    allData.setValue(i, "arrayitemcontentid", allContent.getValue(j, "arrayitemcontentid"));
                    continue block0;
                }
            }
            PropertyList arrayprops = new PropertyList();
            arrayprops.setProperty("sdcid", "LV_ArrayItem");
            arrayprops.setProperty("keyid1", arrayitemids);
            arrayprops.setProperty("linkid", ITEMLINKID);
            arrayprops.setProperty("arrayitemid", allData.getColumnValues("arrayitemid", ";"));
            arrayprops.setProperty("arrayitemcontentid", allData.getColumnValues("arrayitemcontentid", ";"));
            arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", arrayprops);
        } else {
            String zone = properties.getProperty("zone", "");
            if (zone.length() == 0) {
                throw new SapphireException("Zone needs to be specified if level is not \"item\"");
            }
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(arrayid);
            safeSQL.addVar(zone);
            DataSet allContent = this.getQueryProcessor().getPreparedSqlDataSet("SELECT arrayzoneid, arrayzonecontentid, contentsdcid, contentkeyid1, contentkeyid2, contentkeyid3 FROM arrayzonecontent WHERE arrayzoneid in  (SELECT arrayzoneid FROM arrayzone WHERE arrayid = ? AND zone=? )", safeSQL.getValues());
            allContent.addColumn("zone", 0);
            allContent.setString(0, "zone", zone);
            allContent.padColumn("zone");
            DataSet allData = new DataSet();
            allData.addColumnValues("contentsdcid", 0, contentsdcid, ";");
            allData.addColumnValues("contentkeyid1", 0, contentkeyid1, ";");
            allData.addColumnValues("contentkeyid2", 0, contentkeyid2, ";");
            allData.addColumnValues("contentkeyid3", 0, contentkeyid3, ";");
            allData.addColumn("arrayzoneid", 0);
            allData.addColumn("arrayzonecontentid", 0);
            for (int i = 0; i < allData.getRowCount(); ++i) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("zone", zone);
                filter.put("contentsdcid", allData.getString(i, "contentsdcid"));
                filter.put("contentkeyid1", allData.getString(i, "contentkeyid1"));
                DataSet match = allContent.getFilteredDataSet(filter);
                if (match == null || match.getRowCount() <= 0) continue;
                if (match.getRowCount() > 1) {
                    throw new SapphireException("Multiple matches found. change logic");
                }
                if (match.getValue(0, "arrayzonecontentid", "").length() == 0) {
                    throw new SapphireException("Failed to find matching arrayzonecontentid");
                }
                allData.setValue(i, "arrayzoneid", match.getValue(0, "arrayzoneid"));
                allData.setValue(i, "arrayzonecontentid", match.getValue(0, "arrayzonecontentid"));
            }
            PropertyList arrayprops = new PropertyList();
            arrayprops.setProperty("sdcid", "LV_ArrayZone");
            arrayprops.setProperty("keyid1", allData.getColumnValues("arrayzoneid", ";"));
            arrayprops.setProperty("linkid", ZONELINKID);
            arrayprops.setProperty("arrayzoneid", allData.getColumnValues("arrayzoneid", ";"));
            arrayprops.setProperty("arrayzonecontentid", allData.getColumnValues("arrayzonecontentid", ";"));
            arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", arrayprops);
        }
    }
}

