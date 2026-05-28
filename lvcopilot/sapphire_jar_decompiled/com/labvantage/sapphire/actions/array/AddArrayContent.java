/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.util.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddArrayContent
extends BaseAction
implements sapphire.action.AddArrayContent {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String ARRAYITEMCONTENTLINK = "Array Item Content";
    private static final String ARRAYZONECONTENTLINK = "Array Zone Content";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean isLevelZone = properties.getProperty("level", "item").toLowerCase().equalsIgnoreCase("zone");
        if (isLevelZone) {
            this.populateArrayZoneContent(properties);
        } else {
            this.populateArrayItemContent(properties);
        }
    }

    private void populateArrayZoneContent(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayzoneid = properties.getProperty("arrayzoneid", "");
        if ("zone".equals(properties.getProperty("level", "item")) && arrayzoneid.length() == 0) {
            String zone = properties.getProperty("zone", "");
            if (zone.trim().length() == 0) {
                throw new SapphireException("Either arrayzoneid or zone need to be specified to add content at zone level");
            }
            String sql = "SELECT arrayzoneid FROM arrayzone WHERE arrayid = ? AND zone=?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(properties.getProperty("arrayid"));
            safeSQL.addVar(zone);
            DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ret.getRowCount() == 0) {
                throw new SapphireException("Zone ( " + zone + " ) does not exist in the array");
            }
            arrayzoneid = ret.getString(0, "arrayzoneid");
        }
        String arrayzonecontentid = properties.getProperty("arrayzonecontentid", "");
        String contentSDCID = properties.getProperty("contentsdcid");
        String contentitem = properties.getProperty("content", properties.getProperty("contentitem"));
        String contenttype = properties.getProperty("contenttype");
        if (contenttype == null || contenttype.length() == 0) {
            contenttype = contentSDCID;
        }
        contenttype = contenttype.replaceAll("TrackItemSDC", "LV_ReagentLot");
        String sql = "SELECT * FROM arrayzonecontent WHERE arrayzoneid=?  AND contentitem=?  AND contenttype=?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayzoneid);
        safeSQL.addVar(contentitem);
        safeSQL.addVar(contenttype);
        DataSet zonecontentinfo = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (arrayzonecontentid.length() == 0) {
            if (zonecontentinfo.getRowCount() == 0) {
                throw new SapphireException("Invalid contentitem and contenttype defined for arrayzone");
            }
            arrayzonecontentid = zonecontentinfo.getString(0, "arrayzonecontentid");
        }
        String contentkeyid1 = properties.getProperty("contentkeyid1");
        String contentkeyid2 = properties.getProperty("contentkeyid2");
        String contentkeyid3 = properties.getProperty("contentkeyid3");
        String targetvolume = properties.getProperty("volume");
        String targetvolumeunits = properties.getProperty("volumeunits");
        String targetconcentration = properties.getProperty("concentration");
        String targetconcentrationunits = properties.getProperty("concentrationunits");
        String contentlabel = properties.getProperty("contentlabel", "");
        if (contentlabel.length() == 0) {
            contentlabel = ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, contenttype, contentkeyid1, zonecontentinfo.getString(0, "reagenttypeid"));
        }
        PropertyList arrayprops = new PropertyList();
        arrayprops.setProperty("sdcid", "LV_ArrayZone");
        arrayprops.setProperty("linkid", ARRAYZONECONTENTLINK);
        arrayprops.setProperty("keyid1", arrayzoneid);
        arrayprops.setProperty("arrayzoneid", arrayzoneid);
        arrayprops.setProperty("contentsdcid", contentSDCID);
        arrayprops.setProperty("contentkeyid1", contentkeyid1);
        arrayprops.setProperty("contentkeyid2", contentkeyid2);
        arrayprops.setProperty("contentkeyid3", contentkeyid3);
        arrayprops.setProperty("volume", targetvolume);
        arrayprops.setProperty("volumeunits", targetvolumeunits);
        arrayprops.setProperty("concentration", targetconcentration);
        arrayprops.setProperty("concentrationunits", targetconcentrationunits);
        arrayprops.setProperty("contentitem", contentitem);
        arrayprops.setProperty("contenttype", contenttype);
        arrayprops.setProperty("contentlabel", contentlabel);
        arrayprops.setProperty("arrayzonecontentid", arrayzonecontentid);
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        this.getActionProcessor().processAction("EditSDIDetail", "1", arrayprops);
    }

    private void populateArrayItemContent(PropertyList properties) throws SapphireException {
        String sql;
        String arrayzoneid;
        String arrayid = properties.getProperty("arrayid");
        String arrayitemid = properties.getProperty("arrayitemid");
        String arrayitemlabellist = properties.getProperty("arrayitemlabel");
        String contentSDCIDs = properties.getProperty("contentsdcid");
        String contentkeyid1s = properties.getProperty("contentkeyid1");
        String contentkeyid2s = properties.getProperty("contentkeyid2");
        String contentkeyid3s = properties.getProperty("contentkeyid3");
        String targetvolume = properties.getProperty("volume");
        String targetvolumeunits = properties.getProperty("volumeunits");
        String targetconcentration = properties.getProperty("concentration");
        String targetconcentrationunits = properties.getProperty("concentrationunits");
        String contentitem = properties.getProperty("content", properties.getProperty("contentitem"));
        String parentStorageUnitId = properties.getProperty("parentstorageunitid", "");
        String contenttypes = properties.getProperty("contenttype");
        if (contenttypes == null || contenttypes.length() == 0) {
            contenttypes = contentSDCIDs;
        }
        contenttypes = contenttypes.replaceAll("TrackItemSDC", "LV_ReagentLot");
        String contentlabel = properties.getProperty("contentlabel", "");
        if (contentlabel.length() == 0) {
            String[] contentsdcid = StringUtil.split(contentSDCIDs, ";");
            String[] contenttype = StringUtil.split(contenttypes, ";");
            String[] contentkeyid1 = StringUtil.split(contentkeyid1s, ";");
            String[] parentStorageUnitIdList = StringUtil.split(parentStorageUnitId, ";");
            for (int i = 0; i < contentsdcid.length; ++i) {
                if (contentsdcid[i].equals("TrackItemSDC")) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql2 = "select reagenttypeid from  reagentlot where reagentlotid = ( select linkkeyid1 FROM trackitem where trackitemid = ? )";
                    safeSQL.addVar(contentkeyid1[i]);
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql2, safeSQL.getValues());
                    String reagenttype = ret.getValue(0, "reagenttypeid");
                    if (contentlabel.length() > 0) {
                        contentlabel = contentlabel + ";";
                    }
                    contentlabel = contentlabel + ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, contenttype[i], contentkeyid1[i], reagenttype);
                    continue;
                }
                if (contentlabel.length() > 0) {
                    contentlabel = contentlabel + ";";
                }
                contentlabel = contentlabel + ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, contenttype[i], contentkeyid1[i], "", parentStorageUnitId.length() > 0 ? parentStorageUnitIdList[i] : "");
            }
        }
        if ((arrayzoneid = properties.getProperty("arrayzoneid", "")).length() == 0) {
            String zone = properties.getProperty("zone", "");
            if (zone.trim().length() == 0) {
                throw new SapphireException("Either arrayzoneid or zone need to be specified to add content");
            }
            String sql3 = "SELECT arrayzoneid FROM arrayzone WHERE arrayid =  ?  AND zone = ? ";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(properties.getProperty("arrayid"));
            safeSQL.addVar(zone);
            DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql3, safeSQL.getValues());
            if (ret.getRowCount() == 0) {
                throw new SapphireException("Zone ( " + zone + " ) does not exist in the array");
            }
            arrayzoneid = ret.getString(0, "arrayzoneid");
        }
        String parentArrayItemId = properties.getProperty("parentarrayitemid", "");
        String diluentvol = properties.getProperty("diluentvolume", "");
        String diluentvolunits = properties.getProperty("diluentvolumeunits", "");
        String dilutionfactor = properties.getProperty("dilutionfactor", "");
        String repeatnum = properties.getProperty("repeatnum", "");
        if ((arrayitemid == null || arrayitemid.length() == 0) && arrayitemlabellist != null && arrayitemlabellist.length() > 0) {
            String[] arrayitemlabels = StringUtil.split(arrayitemlabellist, ";");
            for (int i = 0; i < arrayitemlabels.length; ++i) {
                sql = "select arrayitemid, xpos, ypos from arrayitem where arrayid = ?  and itemlabel = ?";
                SafeSQL safeSQL = new SafeSQL();
                safeSQL.addVar(arrayid);
                safeSQL.addVar(arrayitemlabels[i]);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (arrayitemid.length() > 0) {
                    arrayitemid = arrayitemid + ";";
                }
                if (ds.getRowCount() <= 0) continue;
                arrayitemid = arrayitemid + ds.getString(0, "arrayitemid");
            }
        }
        String[] items = StringUtil.split(arrayitemid, ";");
        if (arrayid == null || arrayid.length() == 0) {
            throw new SapphireException("Arrayid is empty");
        }
        SafeSQL safeSQL = new SafeSQL();
        sql = "SELECT max(usersequence) maxseq FROM arrayitemcontent WHERE arrayitemid like " + safeSQL.addVar(arrayid + "_%");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String usersequence = "";
        int maxcount = ds.getInt(0, "maxseq", 0);
        for (int i = 0; i < items.length; ++i) {
            if (usersequence.length() > 0) {
                usersequence = usersequence + ";";
            }
            usersequence = usersequence + (maxcount + i + 1);
        }
        PropertyList arrayprops = new PropertyList();
        arrayprops.setProperty("sdcid", "LV_ArrayItem");
        arrayprops.setProperty("linkid", ARRAYITEMCONTENTLINK);
        arrayprops.setProperty("keyid1", arrayitemid);
        arrayprops.setProperty("arrayitemid", arrayitemid);
        arrayprops.setProperty("sourcearrayzoneid", arrayzoneid);
        arrayprops.setProperty("contentsdcid", contentSDCIDs);
        arrayprops.setProperty("contentkeyid1", contentkeyid1s);
        arrayprops.setProperty("contentkeyid2", contentkeyid2s);
        arrayprops.setProperty("contentkeyid3", contentkeyid3s);
        arrayprops.setProperty("volume", targetvolume);
        arrayprops.setProperty("volumeunits", targetvolumeunits);
        arrayprops.setProperty("concentration", targetconcentration);
        arrayprops.setProperty("concentrationunits", targetconcentrationunits);
        arrayprops.setProperty("contentitem", contentitem);
        arrayprops.setProperty("contenttype", contenttypes);
        arrayprops.setProperty("contentlabel", contentlabel);
        arrayprops.setProperty("diluentvolume", diluentvol);
        arrayprops.setProperty("diluentvolumeunits", diluentvolunits);
        if (dilutionfactor.length() > 0) {
            arrayprops.setProperty("dilutionfactor", dilutionfactor);
        }
        if (repeatnum.length() > 0) {
            arrayprops.setProperty("repeatnum", repeatnum);
        }
        arrayprops.setProperty("usersequence", usersequence);
        if (parentArrayItemId.length() > 0) {
            arrayprops.setProperty("parentarrayitemid", parentArrayItemId);
        }
        if (parentStorageUnitId.length() > 0) {
            arrayprops.setProperty("parentstorageunitid", parentStorageUnitId);
        }
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        this.getActionProcessor().processAction("AddSDIDetail", "1", arrayprops);
    }
}

