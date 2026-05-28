/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditArrayContent
extends BaseAction
implements sapphire.action.EditArrayContent {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String ITEMLINKID = "Array Item Content";
    private static final String ZONELINKID = "Array Zone Content";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayitemidlistprop = properties.getProperty("arrayitemid", "");
        String arrayitemlabel = properties.getProperty("arrayitemlabel", "");
        String level = properties.getProperty("level", "item");
        M18NUtil m18nUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        if ("item".equals(level)) {
            if (arrayitemidlistprop.length() == 0) {
                int i;
                if (arrayitemlabel.length() > 0) {
                    String[] labellist = StringUtil.split(arrayitemlabel, ";");
                    SafeSQL safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayid + "%");
                    String sql = "SELECT arrayitemid, itemlabel FROM arrayitem WHERE arrayitemid like ? ";
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    for (i = 0; i < labellist.length; ++i) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put("itemlabel", labellist[i]);
                        DataSet match = ret.getFilteredDataSet(filter);
                        if (match.getRowCount() == 0) {
                            throw new SapphireException("Invalid label specified:" + labellist[i]);
                        }
                        if (arrayitemidlistprop.length() > 0) {
                            arrayitemidlistprop = arrayitemidlistprop + ";";
                        }
                        arrayitemidlistprop = arrayitemidlistprop + match.getString(0, "arrayitemid");
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
                        if (arrayitemidlistprop.length() > 0) {
                            arrayitemidlistprop = arrayitemidlistprop + ";";
                        }
                        arrayitemidlistprop = arrayitemidlistprop + arrayid + "_" + yposition[i] + "_" + xposition[i];
                    }
                }
            }
            properties.setProperty("arrayitemid", arrayitemidlistprop);
            String contentsdcid = properties.getProperty("contentsdcid");
            String contentkeyid1 = properties.getProperty("contentkeyid1");
            String contentkeyid2 = properties.getProperty("contentkeyid2", "");
            String contentkeyid3 = properties.getProperty("contentkeyid3", "");
            String deltaChanges = properties.getProperty("isdelta");
            String inputvolume = properties.getProperty("volume");
            String inputvolumeunits = properties.getProperty("volumeunits");
            String concentration = properties.getProperty("concentration", "");
            String concentrationunits = properties.getProperty("concentrationunits", "");
            String diluentvolume = properties.getProperty("diluentvolume", "");
            String diluentvolumeunits = properties.getProperty("diluentvolumeunits", "");
            int maxkeycombcount = 1;
            maxkeycombcount = contentkeyid2.equalsIgnoreCase("") ? maxkeycombcount : 2;
            maxkeycombcount = contentkeyid3.equalsIgnoreCase("") ? maxkeycombcount : 3;
            String[] arrayitemids = StringUtil.split(arrayitemidlistprop, ";");
            String[] contentsdcids = StringUtil.split(contentsdcid, ";");
            String[] contentkeyid1s = StringUtil.split(contentkeyid1, ";");
            String[] contentkeyid2s = StringUtil.split(contentkeyid2, ";");
            String[] contentkeyid3s = StringUtil.split(contentkeyid3, ";");
            String[] changes = StringUtil.split(deltaChanges, ";");
            String[] volsInUserLocale = StringUtil.split(inputvolume, ";");
            String[] volunits = StringUtil.split(inputvolumeunits, ";");
            String[] concentrationInUserLocale = StringUtil.split(concentration, ";");
            String[] diluentVolumeInUserLocale = StringUtil.split(diluentvolume, ";");
            String[] diluentVolUnits = StringUtil.split(diluentvolumeunits, ";");
            int expectedcount = arrayitemids.length;
            if (contentsdcids.length == 1) {
                contentsdcid = PropertyUtil.repeat(contentsdcid, expectedcount);
                contentsdcids = StringUtil.split(contentsdcid, ";");
            }
            if (contentkeyid1s.length != expectedcount || contentsdcids.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of items specified");
            }
            if (volsInUserLocale.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of volumes specified");
            }
            if (concentration.length() != 0 && concentrationInUserLocale.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of concentrations specified");
            }
            if (diluentvolume.length() != 0 && diluentVolumeInUserLocale.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of diluentvolumes specified");
            }
            if (changes.length != expectedcount) {
                throw new SapphireException("Check your input properties. Mismatch in number of isDelta specified");
            }
            String exactVolumes = "";
            String exactConcentrations = "";
            String exactDiluentVolumes = "";
            String contentIds = "";
            for (int i = 0; i < arrayitemids.length; ++i) {
                DataSet data;
                SafeSQL safeSQL;
                String keyid2;
                String contsdcid = contentsdcids[i];
                String keyid1 = contentkeyid1s[i];
                String string = !contentkeyid2.equalsIgnoreCase("") ? (contentkeyid2s[i].equalsIgnoreCase("") ? null : contentkeyid2s[i]) : (keyid2 = null);
                String keyid3 = !contentkeyid3.equalsIgnoreCase("") ? (contentkeyid3s[i].equalsIgnoreCase("") ? null : contentkeyid3s[i]) : null;
                String volInUserLocale = volsInUserLocale[i];
                BigDecimal vol = m18nUtil.parseBigDecimal(volInUserLocale);
                String volumeUnits = volunits[i];
                String selectEditContentSQL = "select * from arrayitemcontent where arrayitemid = ?  and contentsdcid = ? and contentkeyid1 = ? ";
                if (maxkeycombcount == 1) {
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                } else if (maxkeycombcount == 2) {
                    if (keyid2 != null) {
                        selectEditContentSQL = selectEditContentSQL + "and contentkeyid2 = ? ";
                        safeSQL = new SafeSQL();
                        safeSQL.addVar(arrayitemids[i]);
                        safeSQL.addVar(contsdcid);
                        safeSQL.addVar(keyid1);
                        safeSQL.addVar(keyid2);
                        data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                    } else {
                        safeSQL = new SafeSQL();
                        safeSQL.addVar(arrayitemids[i]);
                        safeSQL.addVar(contsdcid);
                        safeSQL.addVar(keyid1);
                        data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                    }
                } else if (keyid3 != null) {
                    selectEditContentSQL = selectEditContentSQL + "and contentkeyid2 = ? and contentkeyid3 = ?";
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    safeSQL.addVar(keyid2);
                    safeSQL.addVar(keyid3);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                } else if (keyid2 != null) {
                    selectEditContentSQL = selectEditContentSQL + "and contentkeyid2 = ? ";
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    safeSQL.addVar(keyid2);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                } else {
                    safeSQL = new SafeSQL();
                    safeSQL.addVar(arrayitemids[i]);
                    safeSQL.addVar(contsdcid);
                    safeSQL.addVar(keyid1);
                    data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                }
                double finalVol = vol.doubleValue();
                if (changes[i].equalsIgnoreCase("Y")) {
                    if (volumeUnits.length() != 0 && !data.getString(0, "volumeunits").equals(volumeUnits)) {
                        throw new SapphireException("Specified units do not match contents for:" + arrayitemids[i]);
                    }
                    finalVol += data.getDouble(0, "volume");
                }
                exactVolumes = exactVolumes + ";" + finalVol;
                if (concentration.length() > 0) {
                    double conc = m18nUtil.parseBigDecimal(concentrationInUserLocale[i]).doubleValue();
                    exactConcentrations = exactConcentrations + ";" + conc;
                }
                if (diluentvolume.length() > 0) {
                    double diluentvol = m18nUtil.parseBigDecimal(diluentVolumeInUserLocale[i]).doubleValue();
                    exactDiluentVolumes = exactDiluentVolumes + ";" + diluentvol;
                }
                if (data == null || data.getRowCount() <= 0) {
                    throw new SapphireException("Cannot find matching content to edit for arrayitemid:" + arrayitemids[i] + " sdcid:" + contsdcid + " keyid1:" + keyid1);
                }
                contentIds = contentIds + ";" + data.getValue(0, "arrayitemcontentid");
            }
            if (exactVolumes != null && exactVolumes.length() > 0) {
                exactVolumes = exactVolumes.substring(1);
            }
            if (contentIds != null && contentIds.length() > 0) {
                contentIds = contentIds.substring(1);
            }
            if (exactConcentrations != null && exactConcentrations.length() > 0) {
                exactConcentrations = exactConcentrations.substring(1);
            }
            if (exactDiluentVolumes != null && exactDiluentVolumes.length() > 0) {
                exactDiluentVolumes = exactDiluentVolumes.substring(1);
            }
            properties.setProperty("arrayitemcontentid", contentIds);
            this.editArrayItemContent(properties, exactVolumes, exactConcentrations, exactDiluentVolumes);
        } else {
            String[] changes;
            String selectEditContentSQL = "select * from arrayzonecontent where arrayzoneid in (SELECT arrayzoneid FROM arrayzone WHERE arrayid=? AND zone=?)  and contentsdcid = ? and contentkeyid1 = ? ";
            String zone = properties.getProperty("zone", "");
            if (zone.length() == 0) {
                throw new SapphireException("zone needs to be specified for editing at level zone");
            }
            String contentsdcids = properties.getProperty("contentsdcid", "");
            String contentkeyid1s = properties.getProperty("contentkeyid1", "");
            String volumes = properties.getProperty("volume", "");
            String volumeUnits = properties.getProperty("volumeunits", "");
            String concentrations = properties.getProperty("concentration", "");
            String concentrationunits = properties.getProperty("concentrationunits", "");
            String diluentvolume = properties.getProperty("diluentvolume", "");
            String diluentvolumeunits = properties.getProperty("diluentvolumeunits", "");
            String[] contentsdcid = StringUtil.split(contentsdcids, ";");
            String[] contentkeyid1 = StringUtil.split(contentkeyid1s, ";");
            String[] volume = StringUtil.split(volumes, ";");
            String[] volumeunit = StringUtil.split(volumeUnits, ";");
            String[] concentration = StringUtil.split(concentrations, ";");
            String[] concentrationunit = StringUtil.split(concentrationunits, ";");
            String[] diluentVolumes = StringUtil.split(diluentvolume, ";");
            String[] diluentVolumeUnits = StringUtil.split(diluentvolumeunits, ";");
            String deltaChanges = properties.getProperty("isdelta", "");
            if (deltaChanges.length() == 0) {
                changes = new String[contentsdcid.length];
                for (int i = 0; i < changes.length; ++i) {
                    changes[i] = volume[i].charAt(0) == '+' || volume[i].charAt(0) == '-' ? "Y" : "N";
                }
            } else {
                changes = StringUtil.split(deltaChanges, ";");
                if (changes.length != contentsdcid.length) {
                    throw new SapphireException("isDeleta needs to be specified for all items");
                }
            }
            String exactVolumes = "";
            String exactDiluentVolumes = "";
            String exactConcentrations = "";
            String arrayZoneContentIds = "";
            String arrayZoneIds = "";
            for (int i = 0; i < contentsdcid.length; ++i) {
                SafeSQL safeSQL = new SafeSQL();
                safeSQL.addVar(arrayid);
                safeSQL.addVar(zone);
                safeSQL.addVar(contentsdcid[i]);
                safeSQL.addVar(contentkeyid1[i]);
                DataSet data = this.getQueryProcessor().getPreparedSqlDataSet(selectEditContentSQL, safeSQL.getValues());
                String volInUserLocale = volume[i];
                BigDecimal vol = m18nUtil.parseBigDecimal(volInUserLocale);
                double finalVol = vol.doubleValue();
                if (changes[i].equalsIgnoreCase("Y")) {
                    if (volumeUnits.length() != 0 && !data.getString(0, "volumeunits").equals(volumeUnits)) {
                        throw new SapphireException("Specified units do not match contents");
                    }
                    finalVol += data.getDouble(0, "volume");
                }
                exactVolumes = exactVolumes + ";" + finalVol;
                if (concentrations.length() > 0 && concentration[i].length() > 0) {
                    double conc = m18nUtil.parseBigDecimal(concentration[i]).doubleValue();
                    exactConcentrations = exactConcentrations + ";" + conc;
                }
                if (diluentvolume.length() > 0 && diluentVolumes[i].length() > 0) {
                    double diluentvol = m18nUtil.parseBigDecimal(diluentVolumes[i]).doubleValue();
                    exactDiluentVolumes = exactDiluentVolumes + ";" + diluentvol;
                }
                if (data == null || data.getRowCount() <= 0) {
                    throw new SapphireException("Cannot find matching content to edit for sdcid:" + contentsdcid[i] + " keyid1:" + contentkeyid1[i]);
                }
                arrayZoneContentIds = arrayZoneContentIds + ";" + data.getValue(0, "arrayzonecontentid");
                arrayZoneIds = arrayZoneIds + ";" + data.getValue(0, "arrayzoneid");
            }
            if (exactVolumes != null && exactVolumes.length() > 0) {
                exactVolumes = exactVolumes.substring(1);
            }
            if (arrayZoneContentIds != null && arrayZoneContentIds.length() > 0) {
                arrayZoneContentIds = arrayZoneContentIds.substring(1);
            }
            if (arrayZoneIds != null && arrayZoneIds.length() > 0) {
                arrayZoneIds = arrayZoneIds.substring(1);
            }
            if (exactConcentrations != null && exactConcentrations.length() > 0) {
                exactConcentrations = exactConcentrations.substring(1);
            }
            if (exactDiluentVolumes != null && exactDiluentVolumes.length() > 0) {
                exactDiluentVolumes = exactDiluentVolumes.substring(1);
            }
            properties.setProperty("arrayzoneid", arrayZoneIds);
            properties.setProperty("arrayzonecontentid", arrayZoneContentIds);
            this.editArrayZoneContent(properties, exactVolumes, exactConcentrations, exactDiluentVolumes);
        }
    }

    private void editArrayItemContent(PropertyList properties, String exactvolumes, String exactconcentrations, String exactdiluentvolumes) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayitemids = properties.getProperty("arrayitemid");
        String contentsdcid = properties.getProperty("contentsdcid");
        String contentkeyid1 = properties.getProperty("contentkeyid1");
        String contentkeyid2 = properties.getProperty("contentkeyid2");
        String contentkeyid3 = properties.getProperty("contentkeyid3");
        String contentid = properties.getProperty("arrayitemcontentid");
        String volumeunits = properties.getProperty("volumeunits");
        String concunits = properties.getProperty("concentrationunits");
        String diluentvolumeunits = properties.getProperty("diluentvolumeunits", "");
        DataSet allData = new DataSet();
        allData.addColumnValues("arrayitemid", 0, arrayitemids, ";");
        allData.addColumnValues("contentsdcid", 0, contentsdcid, ";");
        allData.addColumnValues("contentkeyid1", 0, contentkeyid1, ";");
        allData.addColumnValues("contentkeyid2", 0, contentkeyid2, ";");
        allData.addColumnValues("contentkeyid3", 0, contentkeyid3, ";");
        allData.addColumnValues("arrayitemcontentid", 0, contentid, ";");
        PropertyList arrayprops = new PropertyList();
        arrayprops.setProperty("sdcid", "LV_ArrayItem");
        arrayprops.setProperty("keyid1", arrayitemids);
        arrayprops.setProperty("linkid", ITEMLINKID);
        arrayprops.setProperty("arrayitemid", allData.getColumnValues("arrayitemid", ";"));
        arrayprops.setProperty("arrayitemcontentid", properties.getProperty("arrayitemcontentid"));
        arrayprops.setProperty("volume", exactvolumes);
        arrayprops.setProperty("volumeunits", volumeunits);
        if (exactdiluentvolumes.length() > 0) {
            arrayprops.setProperty("diluentvolume", exactdiluentvolumes);
        }
        if (diluentvolumeunits.length() > 0) {
            arrayprops.setProperty("diluentvolumeunits", diluentvolumeunits);
        }
        if (exactconcentrations.length() > 0) {
            arrayprops.setProperty("concentration", exactconcentrations);
        }
        if (concunits.length() > 0) {
            arrayprops.setProperty("concentrationunits", concunits);
        }
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("EditSDIDetail", "1", arrayprops);
    }

    private void editArrayZoneContent(PropertyList properties, String exactvolumes, String exactconcentrations, String exactdiluentvolumes) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        String arrayzoneid = properties.getProperty("arrayzoneid");
        String contentsdcid = properties.getProperty("contentsdcid");
        String contentkeyid1 = properties.getProperty("contentkeyid1");
        String contentkeyid2 = properties.getProperty("contentkeyid2");
        String contentkeyid3 = properties.getProperty("contentkeyid3");
        String contentid = properties.getProperty("arrayzonecontentid");
        String volumeunits = properties.getProperty("volumeunits");
        String concunits = properties.getProperty("concentrationunits");
        String diluentvolumeunits = properties.getProperty("diluentvolumeunits", "");
        DataSet allData = new DataSet();
        allData.addColumnValues("arrayzoneid", 0, arrayzoneid, ";");
        allData.addColumnValues("contentsdcid", 0, contentsdcid, ";");
        allData.addColumnValues("contentkeyid1", 0, contentkeyid1, ";");
        allData.addColumnValues("contentkeyid2", 0, contentkeyid2, ";");
        allData.addColumnValues("contentkeyid3", 0, contentkeyid3, ";");
        allData.addColumnValues("arrayzonecontentid", 0, contentid, ";");
        PropertyList arrayprops = new PropertyList();
        arrayprops.setProperty("sdcid", "LV_ArrayZone");
        arrayprops.setProperty("keyid1", arrayzoneid);
        arrayprops.setProperty("linkid", ZONELINKID);
        arrayprops.setProperty("arrayzoneid", allData.getColumnValues("arrayzoneid", ";"));
        arrayprops.setProperty("arrayzonecontentid", properties.getProperty("arrayzonecontentid"));
        arrayprops.setProperty("volume", exactvolumes);
        arrayprops.setProperty("volumeunits", volumeunits);
        if (exactconcentrations.length() > 0) {
            arrayprops.setProperty("concentration", exactconcentrations);
        }
        if (concunits.length() > 0) {
            arrayprops.setProperty("concentrationunits", concunits);
        }
        if (exactdiluentvolumes.length() > 0) {
            arrayprops.setProperty("diluentvolume", exactdiluentvolumes);
        }
        if (diluentvolumeunits.length() > 0) {
            arrayprops.setProperty("diluentvolumeunits", diluentvolumeunits);
        }
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("EditSDIDetail", "1", arrayprops);
    }
}

