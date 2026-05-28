/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ArrayVolumeHandlingUtil {
    private M18NUtil m18NUtil;

    public ArrayVolumeHandlingUtil(M18NUtil m18NUtil) {
        this.m18NUtil = m18NUtil;
    }

    public double findAmountToBeDecremented(int repeatCount, float dilutionFactor, String dilutefirstflag, String targetConc, String targetConcUnits, String sourceconc, String sourceConcUnits, double targetVolume, ConnectionInfo connectionInfo) throws SapphireException {
        double decrementVol = targetVolume;
        if (sourceconc != null && targetConc != null && !targetConc.equals(sourceconc)) {
            if (!targetConcUnits.equals(sourceConcUnits)) {
                sourceConcUnits = OpalUtil.isEmpty(sourceConcUnits) ? "" : sourceConcUnits;
                String string = targetConcUnits = OpalUtil.isEmpty(targetConcUnits) ? "" : targetConcUnits;
                if (targetConc.length() > 0) {
                    targetConc = this.m18NUtil.format(OpalUtil.convertUnit(connectionInfo, this.m18NUtil.parseBigDecimal(targetConc), targetConcUnits, sourceConcUnits));
                }
            }
            if (targetConc.length() > 0 && sourceconc.length() > 0) {
                double sourceconcN = this.m18NUtil.parseBigDecimal(sourceconc).doubleValue();
                double targetConcN = this.m18NUtil.parseBigDecimal(targetConc).doubleValue();
                if (sourceconcN > 0.0) {
                    decrementVol *= targetConcN / sourceconcN;
                }
            }
        }
        if (repeatCount > 1) {
            decrementVol *= (double)repeatCount;
        }
        if (dilutionFactor > 1.0f) {
            decrementVol = dilutionFactor % 1.0f == 0.0f ? (dilutefirstflag.equalsIgnoreCase("false") || dilutefirstflag.equalsIgnoreCase("N") ? (decrementVol *= (double)(1 + 1 / ((int)dilutionFactor - 1))) : (decrementVol *= (double)(1 / ((int)dilutionFactor - 1)))) : (dilutefirstflag.equalsIgnoreCase("false") || dilutefirstflag.equalsIgnoreCase("N") ? (decrementVol *= (double)(1.0f + 1.0f / (dilutionFactor - 1.0f))) : (decrementVol *= (double)(1.0f / (dilutionFactor - 1.0f))));
        }
        return decrementVol;
    }

    private void findSourceVolumeAdjustment(DataSet contentDS, ConnectionInfo connectionInfo) throws SapphireException {
        for (int i = 0; i < contentDS.size(); ++i) {
            String volume = contentDS.getValue(i, "sourcevolume", "").trim();
            if (!volume.isEmpty()) {
                BigDecimal sourceVolume = new BigDecimal(volume);
                contentDS.setValue(i, "quantity", "-" + this.m18NUtil.format(sourceVolume));
                if (contentDS.getValue(i, "sourcevolumeunit").length() > 0) {
                    BigDecimal bd = OpalUtil.convertUnit(connectionInfo, sourceVolume, contentDS.getValue(i, "sourcevolumeunit", ""), contentDS.getValue(i, "volumeunits", ""));
                    contentDS.setValue(i, "quantity", "-" + this.m18NUtil.format(bd));
                }
                if (!contentDS.isValidColumn("override")) {
                    contentDS.addColumn("override", 0);
                }
                contentDS.setValue(i, "override", "Y");
                continue;
            }
            double updatedQty = this.findAmountToBeDecremented(contentDS.getInt(i, "repeatcount", 1), Float.parseFloat(contentDS.getValue(i, "dilutionfactor", "1")), contentDS.getString(i, "dilutefirstflag", ""), contentDS.getValue(i, "targetconcentration"), contentDS.getString(i, "targetconcentrationunit", ""), contentDS.getValue(i, "sourceconcentration"), contentDS.getString(i, "sourceconcentrationunit", ""), contentDS.getDouble(i, "targetvolume", 0.0), connectionInfo);
            contentDS.setValue(i, "quantity", "-" + this.m18NUtil.format(BigDecimal.valueOf(updatedQty)));
        }
    }

    public DataSet findSourceConcentration(String sourceSDCId, String sourceKeyId1, DAMProcessor damProcessor, QueryProcessor queryProcessor) throws SapphireException {
        DataSet sourceConcDS = new DataSet();
        String rsetid = "";
        String sql = "";
        if (sourceSDCId.indexOf("Sample") != -1) {
            sql = "SELECT s_sampleid sourceid, concentration, concentrationunits FROM s_sample WHERE  ";
            if (sourceKeyId1.split(";").length <= 750) {
                sql = sql + "s_sampleid IN ('" + sourceKeyId1.replace(";", "','") + "')";
            } else {
                rsetid = damProcessor.createRSet("Sample", sourceKeyId1, null, null);
                sql = sql + " s_sampleid IN (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = '" + rsetid + "' )";
            }
        } else if (sourceSDCId.indexOf("ArrayItem") != -1) {
            sql = "SELECT arrayitemid sourceid, concentration, concentrationunits FROM arrayitem WHERE  ";
            if (sourceKeyId1.split(";").length <= 750) {
                sql = sql + "arrayitemid IN ('" + sourceKeyId1.replace(";", "','") + "')";
            } else {
                rsetid = damProcessor.createRSet("LV_ArrayItem", sourceKeyId1, null, null);
                sql = sql + " arrayitemid IN (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = '" + rsetid + "' )";
            }
        } else {
            return sourceConcDS;
        }
        sourceConcDS = queryProcessor.getSqlDataSet(sql);
        if (StringUtil.getLen(rsetid) > 0L) {
            damProcessor.clearRSet(rsetid);
        }
        return sourceConcDS;
    }

    public void adjustSourceInv(DataSet inventoryDS, ActionProcessor ap, QueryProcessor qp, boolean validate, ConnectionInfo connectionInfo) throws SapphireException {
        ArrayList<DataSet> groupedDSAL = inventoryDS.getGroupedDataSets("contentsdcid,volumehandlingflag");
        PropertyList actionProps = new PropertyList();
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet finalDS;
            DataSet ds = groupedDSAL.get(i);
            String sdcid = ds.getValue(0, "contentsdcid", "");
            String quantityHandlingFlag = ds.getValue(0, "volumehandlingflag", "");
            if (sdcid.equals("TrackItemSDC")) {
                if (quantityHandlingFlag.length() == 0 || quantityHandlingFlag.equals("D") || quantityHandlingFlag.equals("N")) {
                    ds.addColumn("quantity", 0);
                    this.findSourceVolumeAdjustment(ds, connectionInfo);
                    finalDS = this.eliminateOverrides(ds);
                    actionProps.clear();
                    actionProps.setProperty("trackitemid", finalDS.getColumnValues("contentkeyid1", ";"));
                    actionProps.setProperty("quantity", finalDS.getColumnValues("quantity", ";"));
                    actionProps.setProperty("quantityunit", finalDS.getColumnValues("volumeunits", ";"));
                    actionProps.setProperty("quantitytype", "U");
                    actionProps.setProperty("validatequantity", validate ? "Y" : "N");
                    ap.processAction("AdjustTrackItemInv", "1", actionProps);
                    continue;
                }
                if (!quantityHandlingFlag.equals("O") && !quantityHandlingFlag.equals("Y") && !quantityHandlingFlag.equals("S")) continue;
                actionProps.clear();
                if (validate) {
                    finalDS = this.eliminateOverrides(ds);
                    if ((finalDS = this.eliminateNullQuantities(qp, finalDS)).getRowCount() > 0) {
                        actionProps.setProperty("trackitemid", finalDS.getColumnValues("contentkeyid1", ";"));
                        actionProps.setProperty("quantity", finalDS.getColumnValues("quantity", ";"));
                        actionProps.setProperty("quantityunit", finalDS.getColumnValues("volumeunits", ";"));
                        actionProps.setProperty("quantitytype", "U");
                        actionProps.setProperty("validatequantity", "Y");
                        ap.processAction("AdjustTrackItemInv", "1", actionProps);
                    }
                    actionProps.clear();
                }
                actionProps.setProperty("trackitemid", ds.getColumnValues("contentkeyid1", ";"));
                actionProps.setProperty("qtycurrent", "0");
                ap.processAction("EditTrackItem", "1", actionProps);
                continue;
            }
            if (sdcid.equals("Sample")) {
                ds = this.findTrackItemId(ds, qp);
                if (quantityHandlingFlag.length() == 0 || quantityHandlingFlag.equals("D") || quantityHandlingFlag.equals("N")) {
                    ds.addColumn("quantity", 0);
                    this.findSourceVolumeAdjustment(ds, connectionInfo);
                    finalDS = this.eliminateOverrides(ds);
                    actionProps.clear();
                    if (ds.getColumnValues("trackitemid", ";").length() == ds.getRowCount() - 1) {
                        Trace.log("No trackitems found for content");
                        return;
                    }
                    if ((finalDS = this.eliminateNullQuantities(qp, finalDS)).getRowCount() <= 0) continue;
                    actionProps.setProperty("trackitemid", finalDS.getColumnValues("trackitemid", ";"));
                    actionProps.setProperty("quantity", finalDS.getColumnValues("quantity", ";"));
                    actionProps.setProperty("quantityunit", finalDS.getColumnValues("volumeunits", ";"));
                    actionProps.setProperty("quantitytype", "U");
                    actionProps.setProperty("validatequantity", validate ? "Y" : "N");
                    ap.processAction("AdjustTrackItemInv", "1", actionProps);
                    continue;
                }
                if (!quantityHandlingFlag.equals("O") && !quantityHandlingFlag.equals("Y") && !quantityHandlingFlag.equals("S")) continue;
                actionProps.clear();
                if (validate) {
                    finalDS = this.eliminateOverrides(ds);
                    actionProps.setProperty("trackitemid", finalDS.getColumnValues("trackitemid", ";"));
                    actionProps.setProperty("quantity", finalDS.getColumnValues("quantity", ";"));
                    actionProps.setProperty("quantityunit", finalDS.getColumnValues("volumeunits", ";"));
                    actionProps.setProperty("quantitytype", "U");
                    actionProps.setProperty("validatequantity", "Y");
                    ap.processAction("AdjustTrackItemInv", "1", actionProps);
                    actionProps.clear();
                }
                actionProps.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
                actionProps.setProperty("qtycurrent", "0");
                ap.processAction("EditTrackItem", "1", actionProps);
                continue;
            }
            if (!sdcid.equals("LV_ArrayItem")) continue;
            if (quantityHandlingFlag.length() == 0 || quantityHandlingFlag.equals("D") || quantityHandlingFlag.equals("N")) {
                ds.addColumn("quantity", 0);
                this.findSourceVolumeAdjustment(ds, connectionInfo);
                actionProps.clear();
                finalDS = this.eliminateOverrides(ds);
                actionProps.setProperty("arrayitemid", finalDS.getColumnValues("contentkeyid1", ";"));
                actionProps.setProperty("totalvol", finalDS.getColumnValues("quantity", ";"));
                actionProps.setProperty("validatetotalvolume", validate ? "Y" : "N");
                if (finalDS.getColumnValues("volumeunits", ";").replaceAll(";", "").length() > 0) {
                    actionProps.setProperty("totalvolunits", finalDS.getColumnValues("volumeunits", ";"));
                }
                ap.processAction("EditArrayItem", "1", actionProps);
                continue;
            }
            if (!quantityHandlingFlag.equals("O") && !quantityHandlingFlag.equals("Y") && !quantityHandlingFlag.equals("S")) continue;
            actionProps.clear();
            finalDS = this.eliminateOverrides(ds);
            if (validate) {
                actionProps.setProperty("arrayitemid", finalDS.getColumnValues("contentkeyid1", ";"));
                actionProps.setProperty("totalvol", finalDS.getColumnValues("quantity", ";"));
                actionProps.setProperty("validatetotalvolume", "Y");
                if (ds.getColumnValues("volumeunits", ";").replaceAll(";", "").length() > 0) {
                    actionProps.setProperty("totalvolunits", finalDS.getColumnValues("volumeunits", ";"));
                }
                ap.processAction("EditArrayItem", "1", actionProps);
                actionProps.clear();
            }
            actionProps.setProperty("arrayitemid", ds.getColumnValues("contentkeyid1", ";"));
            actionProps.setProperty("totalvol", "0");
            ap.processAction("EditArrayItem", "1", actionProps);
        }
    }

    private DataSet findTrackItemId(DataSet contentDS, QueryProcessor queryProcessor) throws SapphireException {
        DataSet ds;
        if (contentDS.size() > 1000) {
            DAMProcessor damProcessor = new DAMProcessor(queryProcessor.getConnectionid());
            String sampleids = contentDS.getColumnValues("contentkeyid1", ";");
            String rsetid = damProcessor.createRSet("Sample", sampleids, null, null);
            ds = queryProcessor.getPreparedSqlDataSet("select linkkeyid1, trackitemid from trackitem where linksdcid = 'Sample' and linkkeyid1 in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)", (Object[])new String[]{rsetid});
            damProcessor.clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            ds = queryProcessor.getPreparedSqlDataSet("select linkkeyid1, trackitemid from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(contentDS.getColumnValues("contentkeyid1", "','")) + ")", safeSQL.getValues());
        }
        if (OpalUtil.isNotEmpty(ds)) {
            for (int i = 0; i < contentDS.size(); ++i) {
                int row;
                String sampleid = contentDS.getValue(i, "contentkeyid1", "");
                if (sampleid.isEmpty() || (row = ds.findRow("linkkeyid1", sampleid)) == -1) continue;
                contentDS.setString(i, "trackitemid", ds.getString(row, "trackitemid"));
            }
        }
        return contentDS;
    }

    private DataSet eliminateOverrides(DataSet ds) {
        DataSet finalDS = new DataSet();
        if (ds.isValidColumn("override")) {
            for (int row = 0; row < ds.getRowCount(); ++row) {
                if (!"Y".equals(ds.getString(row, "override", ""))) {
                    finalDS.copyRow(ds, row, 1);
                    continue;
                }
                String currentarrayitemid = ds.getString(row, "contentkeyid1");
                boolean found = false;
                for (int x = 0; x < finalDS.getRowCount(); ++x) {
                    if (!finalDS.getString(x, "contentkeyid1").equals(currentarrayitemid)) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                finalDS.copyRow(ds, row, 1);
            }
        } else {
            for (int i = 0; i < ds.size(); ++i) {
                finalDS.copyRow(ds, i, 1);
            }
        }
        return finalDS;
    }

    private DataSet eliminateNullQuantities(QueryProcessor qp, DataSet ds) throws SapphireException {
        DataSet dataSet;
        if (ds.size() > 1000) {
            DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
            String rsetid = damProcessor.createRSet("Sample", ds.getColumnValues("trackitemid", ";"), null, null);
            dataSet = qp.getPreparedSqlDataSet("select trackitemid from trackitem where qtycurrent is not null and trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)", (Object[])new String[]{rsetid});
            damProcessor.clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            dataSet = qp.getPreparedSqlDataSet("select trackitemid from trackitem where qtycurrent is not null and trackitemid in (" + safeSQL.addIn(ds.getColumnValues("trackitemid", "','")) + ")", safeSQL.getValues());
        }
        HashSet<String> notNullTISet = new HashSet<String>();
        if (OpalUtil.isNotEmpty(dataSet)) {
            for (int i = 0; i < dataSet.size(); ++i) {
                notNullTISet.add(dataSet.getString(i, "trackitemid"));
            }
        }
        DataSet finalDS = new DataSet();
        if (!notNullTISet.isEmpty()) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (!notNullTISet.contains(ds.getString(i, "trackitemid"))) continue;
                finalDS.copyRow(ds, i, 1);
            }
        }
        return finalDS;
    }

    public static void updateArrayItem(QueryProcessor qp, ActionProcessor ap, String arrayzoneid, BigDecimal targetvolume, String targetvolumeunits, BigDecimal targetconc, String targetconcunit) throws SapphireException {
        PropertyList arrayItemProps = new PropertyList();
        String itemSQL = "SELECT arrayitemid, arrayzoneid from arrayitemarrayzone where arrayzoneid = ?";
        DataSet arrayItemList = qp.getPreparedSqlDataSet(itemSQL, new Object[]{arrayzoneid});
        if (arrayItemList != null && arrayItemList.getRowCount() > 0) {
            arrayItemList.setString(0, "totalvolume", "+" + targetvolume);
            arrayItemList.setString(0, "totalvolumeunits", targetvolumeunits);
            arrayItemList.setString(0, "concentration", "" + targetconc);
            arrayItemList.setString(0, "concentrationunits", targetconcunit);
            arrayItemList.padColumns();
            arrayItemProps.setProperty("arrayitemid", arrayItemList.getColumnValues("arrayitemid", ";"));
            arrayItemProps.setProperty("totalvol", arrayItemList.getColumnValues("totalvolume", ";"));
            arrayItemProps.setProperty("totalvolunits", arrayItemList.getColumnValues("totalvolumeunits", ";"));
            arrayItemProps.setProperty("totalconc", arrayItemList.getColumnValues("concentration", ";"));
            arrayItemProps.setProperty("totalconcunits", arrayItemList.getColumnValues("concentrationunits", ";"));
            ap.processAction("EditArrayItem", "1", arrayItemProps);
        }
    }
}

