/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.array.EvaluateArrayLayoutRules;
import com.labvantage.sapphire.array.WellValues;
import com.labvantage.sapphire.array.util.ArrayVolumeHandlingUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LoadArray
extends BaseAction
implements sapphire.action.LoadArray {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    M18NUtil m18NUtil;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int determineArrayCount;
        String sql;
        SafeSQL safeSQL;
        String zone = properties.getProperty("zone", properties.getProperty("zonename", ""));
        String arrayid = properties.getProperty("arrayid", "");
        String arraymethodid = properties.getProperty("arraymethodid", "");
        boolean validateQty = "Y".equals(properties.getProperty("validatesourcevolume", "N"));
        this.m18NUtil = new M18NUtil(this.connectionInfo);
        if (zone.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Zone is mandatory for loading an array"));
        }
        if (zone.contains(";")) {
            zone = StringUtil.split(zone, ";")[0];
            Trace.log("Multiple duplicate zones specified");
        }
        if (arrayid.length() == 0 && arraymethodid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Either arrayid or arraymethodid has to be specified to load an array"));
        }
        if (arrayid.length() > 0) {
            String sql2 = "SELECT adhocmodeflag FROM arrayzone WHERE arrayid = ? AND zone = ?";
            SafeSQL safeSQL2 = new SafeSQL();
            safeSQL2.addVar(arrayid);
            safeSQL2.addVar(zone);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, safeSQL2.getValues());
            if (ds != null && "Y".equalsIgnoreCase(ds.getString(0, "adhocmodeflag", "N"))) {
                throw new SapphireException(this.getTranslationProcessor().translate("Adhoc mode loading cannot use this action"));
            }
        }
        String arraymethodversionid = properties.getProperty("arraymethodversionid", "");
        String contentitem = properties.getProperty("content", "");
        String contentsdcid = properties.getProperty("contentsdcid", "");
        String contentkeyid1 = properties.getProperty("contentkeyid1", "");
        String overflowflag = properties.getProperty("overflowflag", "I");
        String level = properties.getProperty("level", "item");
        if (arraymethodid.length() > 0) {
            if (arraymethodversionid.length() == 0) {
                arraymethodversionid = ArrayUtil.getArrayMethodCurrentVersion(this.getQueryProcessor(), arraymethodid);
            }
            SafeSQL safeSQL3 = new SafeSQL();
            String sql3 = "SELECT count(*) FROM arraymethodcontent WHERE arraymethodid=" + safeSQL3.addVar(arraymethodid) + " AND arraymethodversionid=" + safeSQL3.addVar(arraymethodversionid) + " AND zone=" + safeSQL3.addVar(zone);
            int match = this.getQueryProcessor().getPreparedCount(sql3, safeSQL3.getValues());
            if (match == 0) {
                throw new SapphireException("Array Method " + arraymethodid + " does not have Content Details defined for Zone: " + zone);
            }
        }
        if (contentitem.length() == 0 || contentsdcid.length() == 0 || contentkeyid1.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("content, contentsdcid and contentkeyid1 have to be specified to load an array"));
        }
        if (level.equals("item") && (contentitem.equals("MasterMix") || contentitem.equals("Operation"))) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid content:" + contentitem + " specified for item level loading."));
        }
        if (level.equals("zone") && (contentitem.equals("Unknown") || contentitem.equals("Transfer") || contentitem.equals("Control") && contentsdcid.equals("Sample"))) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid content:" + contentitem + " contentsdcid:" + contentsdcid + " specified for zone level loading."));
        }
        if (arrayid.length() == 0 && !"C".equals(overflowflag)) {
            throw new SapphireException("Either arrayid has to be specified or overflowflag should be C");
        }
        if ("C".equals(overflowflag) && (properties.getProperty("startrowlabel", "").length() > 0 || properties.getProperty("startcolumnlabel", "").length() > 0 || properties.getProperty("startitemlabel", "").length() > 0 || properties.getProperty("startitemid", "").length() > 0)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Start position should not be specified when overflowflag  = 'C'"));
        }
        if (arraymethodid.length() == 0) {
            String str = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayid);
            String[] tokens = StringUtil.split(str, "|");
            arraymethodid = tokens[0];
            arraymethodversionid = tokens[4];
        }
        int zonesize = 0;
        String newarrayidlist = "";
        DataSet arraylayoutdetails = this.getLayoutDetails(arrayid, arraymethodid, arraymethodversionid);
        if (arraylayoutdetails == null) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to fetch arraylayout information"));
        }
        properties.setProperty("arraylayoutid", arraylayoutdetails.getString(0, "arraylayoutid"));
        properties.setProperty("arraylayoutversionid", arraylayoutdetails.getString(0, "arraylayoutversionid"));
        if (properties.getProperty("arraylayoutid", "").length() == 0 && arrayid.length() > 0) {
            safeSQL = new SafeSQL();
            sql = "SELECT count(*) FROM arrayitemarrayzone WHERE arrayzoneid = (SELECT arrayzoneid FROM arrayzone WHERE zone=" + safeSQL.addVar(zone) + " AND arrayid = " + safeSQL.addVar(arrayid) + ")";
            zonesize = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
            properties.setProperty("zonesize", "" + zonesize);
        } else if (properties.getProperty("arraylayoutid", "").length() > 0) {
            safeSQL = new SafeSQL();
            sql = "SELECT count(*) FROM arraylayoutzoneitem WHERE arraylayoutzone = " + safeSQL.addVar(zone) + "  AND arraylayoutid = " + safeSQL.addVar(properties.getProperty("arraylayoutid")) + "  AND arraylayoutversionid = " + safeSQL.addVar(properties.getProperty("arraylayoutversionid")) + " ";
            zonesize = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
            properties.setProperty("zonesize", "" + zonesize);
            if (zonesize == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("The zone specified has no wells associated with it. Cannot perform this action."));
            }
        }
        if (!"zone".equals(level) && (determineArrayCount = this.determineAdditionalArrayCount(arrayid, contentkeyid1, zone, properties.getProperty("arraylayoutid"), properties.getProperty("arraylayoutversionid"), zonesize)) > 0) {
            if ("E".equals(overflowflag)) {
                throw new SapphireException(this.getTranslationProcessor().translate("Cannot fit the specified content into the specified array."));
            }
            try {
                if ("C".equals(overflowflag)) {
                    properties.setProperty("copies", Integer.toString(determineArrayCount));
                    this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.array.CreateArray", properties);
                    newarrayidlist = properties.getProperty("arrayid");
                }
            }
            catch (SapphireException e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
        if (arrayid.length() > 0) {
            String methodinfo = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayid, arraymethodid);
            if (methodinfo == null || methodinfo.length() == 0) {
                throw new SapphireException("Failed to determine the array method instance to load for arrayid:" + arrayid + " arraymethodid:" + arraymethodid);
            }
            String[] toks = StringUtil.split(methodinfo, "|");
            arraymethodid = toks[0];
            arraymethodversionid = toks[4];
            properties.setProperty("arraymethodid", arraymethodid);
            properties.setProperty("arraymethodversionid", arraymethodversionid);
            String currentItemStatus = toks[2];
            String promoteresultsflag = toks[3];
            String arrayStatus = ArrayUtil.getArrayStatus(this.getQueryProcessor(), arrayid);
            if (!ArrayUtil.validateStatus(arrayStatus, currentItemStatus, "LoadArray", promoteresultsflag)) {
                throw new SapphireException(this.getTranslationProcessor().translate("Invalid AMI Status: " + currentItemStatus + " Cannot load array:") + arrayid);
            }
        }
        if (arrayid.length() > 0) {
            if (newarrayidlist.length() > 0) {
                arrayid = arrayid + ";" + newarrayidlist;
            }
        } else if (newarrayidlist.length() > 0) {
            arrayid = newarrayidlist;
        } else {
            throw new SapphireException("Need an arrayid when loading by zone");
        }
        properties.setProperty("arrayid", arrayid);
        boolean isZoneLevel = properties.getProperty("level", "item").equalsIgnoreCase("zone");
        DataSet arraymethodcontent = LoadArray.fetchArrayMethodContent(this.getSDIProcessor(), arraymethodid, arraymethodversionid);
        DataSet arraymethoddefaults = ArrayUtil.getDefaultsFromArrayMethod(this.getQueryProcessor(), arraymethodid, arraymethodversionid, zone, contentitem, "zone".equals(level) ? "Z" : "I");
        PropertyList inProperties = properties.copy();
        String volume = inProperties.getProperty("volume");
        if (volume != null && volume.length() > 0) {
            String volumeunits = inProperties.getProperty("volumeunits");
            if (volumeunits == null || volumeunits.length() == 0) {
                throw new SapphireException("Volumeunits need to be specified when volume is specified");
            }
        } else if (arraymethoddefaults != null && arraymethoddefaults.getRowCount() == 1) {
            BigDecimal methodTargetVolume = arraymethoddefaults.getBigDecimal(0, "volumetarget", BigDecimal.ZERO);
            inProperties.setProperty("volume", this.m18NUtil.format(methodTargetVolume));
            String volumeunits = arraymethoddefaults.getValue(0, "volumetargetunits");
            if (volumeunits == null || volumeunits.length() == 0) {
                throw new SapphireException("Volumeunits not defined in arraymethod");
            }
            inProperties.setProperty("volumeunits", volumeunits);
        } else {
            throw new SapphireException("Cannot match contents for zone:" + zone + " loading at level:" + level + " in arraymethod:" + arraymethodid);
        }
        String conc = inProperties.getProperty("concentration");
        if (conc != null && conc.length() > 0) {
            String concunits = inProperties.getProperty("concentrationunits");
            if (concunits == null || concunits.length() == 0) {
                throw new SapphireException("Concentrationunits need to be specified when concentration is specified");
            }
        } else if (arraymethoddefaults != null && arraymethoddefaults.getRowCount() == 1) {
            BigDecimal methodTargetConcentration = arraymethoddefaults.getBigDecimal(0, "concentrationtarget");
            if (methodTargetConcentration != null) {
                inProperties.setProperty("concentration", this.m18NUtil.format(methodTargetConcentration));
                String concunits = arraymethoddefaults.getValue(0, "concentrationtargetunits");
                if (concunits == null || concunits.length() == 0) {
                    throw new SapphireException("Concenration units not defined in arraymethod");
                }
                inProperties.setProperty("concentrationunits", concunits);
            }
        } else {
            throw new SapphireException("Cannot match contents for zone:" + zone + " loading at level:" + level + " in arraymethod:" + arraymethodid);
        }
        if (isZoneLevel) {
            if (properties.getProperty("overflowflag", "I").equals("C")) {
                throw new SapphireException("Do not support overflowflag='C' for zone level loading");
            }
            this.fillZone(inProperties, arrayid, validateQty, arraymethodid, arraymethodversionid);
        } else {
            String[] arrayids = StringUtil.split(arrayid, ";");
            if (properties.getProperty("zonesize", "").length() == 0) {
                SafeSQL safeSQL4 = new SafeSQL();
                String sql4 = "SELECT count(*) FROM arraylayoutzoneitem WHERE arraylayoutzone = " + safeSQL4.addVar(zone) + "  AND arraylayoutid = " + safeSQL4.addVar(properties.getProperty("arraylayoutid")) + "  AND arraylayoutversionid = " + safeSQL4.addVar(properties.getProperty("arraylayoutversionid")) + " ";
                zonesize = this.getQueryProcessor().getPreparedCount(sql4, safeSQL4.getValues());
            }
            int contentitemsPerArray = this.determineContentItemsPerArray(zone, properties.getProperty("arraylayoutid"), properties.getProperty("arraylayoutversionid"), zonesize);
            for (int i = 0; i < arrayids.length; ++i) {
                PropertyList processProperties = this.getBatchProperties(contentitemsPerArray, inProperties);
                processProperties.setProperty("arrayid", arrayids[i]);
                this.fillIndividualWells(properties, arraymethoddefaults, properties.getProperty("content"), processProperties, arraymethodcontent, validateQty);
            }
        }
    }

    private PropertyList getBatchProperties(int itemsPerArray, PropertyList inProperties) {
        DataSet contentprops = new DataSet();
        contentprops.addColumnValues("content", 0, inProperties.getProperty("content"), ";");
        contentprops.addColumnValues("contentsdcid", 0, inProperties.getProperty("contentsdcid"), ";");
        contentprops.addColumnValues("contentkeyid1", 0, inProperties.getProperty("contentkeyid1"), ";");
        contentprops.addColumnValues("contentkeyid2", 0, inProperties.getProperty("contentkeyid2"), ";");
        contentprops.addColumnValues("contentkeyid3", 0, inProperties.getProperty("contentkeyid3"), ";");
        contentprops.addColumnValues("volume", 0, inProperties.getProperty("volume"), ";");
        contentprops.addColumnValues("volumeunits", 0, inProperties.getProperty("volumeunits"), ";");
        contentprops.addColumnValues("concentration", 0, inProperties.getProperty("concentration"), ";");
        contentprops.addColumnValues("concentrationunits", 0, inProperties.getProperty("concentrationunits"), ";");
        contentprops.addColumnValues("sourcevolume", 0, inProperties.getProperty("sourcevolume"), ";");
        contentprops.addColumnValues("sourcevolumeunit", 0, inProperties.getProperty("sourcevolumeunit"), ";");
        contentprops.addColumnValues("zone", 0, inProperties.getProperty("zone"), ";");
        contentprops.addColumnValues("zonename", 0, inProperties.getProperty("zonename"), ";");
        contentprops.padColumn("content");
        contentprops.padColumn("contentsdcid");
        contentprops.padColumn("zone");
        contentprops.padColumn("zonename");
        DataSet subset = new DataSet();
        for (int i = 0; i < Math.min(itemsPerArray, contentprops.size()); ++i) {
            subset.copyRow(contentprops, i, 1);
        }
        DataSet remaining = new DataSet();
        for (int i = itemsPerArray; i < contentprops.size(); ++i) {
            remaining.copyRow(contentprops, i, 1);
        }
        if (remaining.getValue(0, "volume", "").length() == 0) {
            remaining.setString(0, "volume", subset.getString(0, "volume"));
        }
        if (remaining.getValue(0, "volumeunits", "").length() == 0) {
            remaining.setString(0, "volumeunits", subset.getString(0, "volumeunits"));
        }
        if (remaining.getValue(0, "concentration", "").length() == 0) {
            remaining.setString(0, "concentration", subset.getString(0, "concentration"));
        }
        if (remaining.getValue(0, "concentrationunits", "").length() == 0) {
            remaining.setString(0, "concentrationunits", subset.getString(0, "concentrationunits"));
        }
        if (remaining.getValue(0, "sourcevolume", "").length() == 0) {
            remaining.setString(0, "sourcevolume", subset.getString(0, "sourcevolume"));
        }
        if (remaining.getValue(0, "sourcevolumeunit", "").length() == 0) {
            remaining.setString(0, "sourcevolumeunit", subset.getString(0, "sourcevolumeunit"));
        }
        inProperties.setProperty("contentsdcid", remaining.getColumnValues("contentsdcid", ";"));
        inProperties.setProperty("contentkeyid1", remaining.getColumnValues("contentkeyid1", ";"));
        inProperties.setProperty("contentkeyid2", remaining.getColumnValues("contentkeyid2", ";"));
        inProperties.setProperty("contentkeyid3", remaining.getColumnValues("contentkeyid3", ";"));
        inProperties.setProperty("volume", remaining.getColumnValues("volume", ";"));
        inProperties.setProperty("volumeunits", remaining.getColumnValues("volumeunits", ";"));
        inProperties.setProperty("sourcevolume", remaining.getColumnValues("sourcevolume", ";"));
        inProperties.setProperty("sourcevolumeunit", remaining.getColumnValues("sourcevolumeunit", ";"));
        inProperties.setProperty("concentration", remaining.getColumnValues("concentration", ";"));
        inProperties.setProperty("concentrationunits", remaining.getColumnValues("concentrationunits", ";"));
        inProperties.setProperty("content", remaining.getColumnValues("content", ";"));
        PropertyList ret = inProperties.copy();
        ret.setProperty("content", subset.getColumnValues("content", ";"));
        ret.setProperty("contentsdcid", subset.getColumnValues("contentsdcid", ";"));
        ret.setProperty("contentkeyid1", subset.getColumnValues("contentkeyid1", ";"));
        ret.setProperty("contentkeyid2", subset.getColumnValues("contentkeyid2", ";"));
        ret.setProperty("contentkeyid3", subset.getColumnValues("contentkeyid3", ";"));
        ret.setProperty("volume", subset.getColumnValues("volume", ";"));
        ret.setProperty("volumeunits", subset.getColumnValues("volumeunits", ";"));
        ret.setProperty("sourcevolume", subset.getColumnValues("sourcevolume", ";"));
        ret.setProperty("sourcevolumeunit", subset.getColumnValues("sourcevolumeunit", ";"));
        ret.setProperty("concentration", subset.getColumnValues("concentration", ";"));
        ret.setProperty("concentrationunits", subset.getColumnValues("concentrationunits", ";"));
        return ret;
    }

    private void fillZone(PropertyList properties, String arrayid, boolean validateQty, String arraymethodid, String arraymethodversionid) throws SapphireException {
        String contentitems = properties.getProperty("content");
        String contentkeyid1s = properties.getProperty("contentkeyid1");
        String contentkeyid2s = properties.getProperty("contentkeyid2", "");
        String contentkeyid3s = properties.getProperty("contentkeyid3", "");
        String targetvolumes = properties.getProperty("volume", "0");
        String targetvolumeunits = properties.getProperty("volumeunits");
        String sourcevolume = properties.getProperty("sourcevolume", "");
        String sourcevolumeunits = properties.getProperty("sourcevolumeunit", "");
        String targetconcs = properties.getProperty("concentration");
        String targetconcunits = properties.getProperty("concentrationunits");
        String contentSDCIDs = properties.getProperty("contentsdcid");
        String zonename = properties.getProperty("zone", properties.getProperty("zonename", ""));
        if (zonename.contains(";")) {
            zonename = zonename.substring(0, zonename.indexOf(";"));
        }
        DataSet contentsInZoneDS = new DataSet();
        contentsInZoneDS.addColumnValues("contentitem", 0, contentitems, ";");
        contentsInZoneDS.addColumnValues("contentsdcid", 0, contentSDCIDs, ";");
        contentsInZoneDS.addColumnValues("contentkeyid1", 0, contentkeyid1s, ";");
        contentsInZoneDS.addColumnValues("contentkeyid2", 0, contentkeyid2s, ";");
        contentsInZoneDS.addColumnValues("contentkeyid3", 0, contentkeyid3s, ";");
        contentsInZoneDS.addColumnValues("targetvolume", 0, targetvolumes, ";");
        contentsInZoneDS.addColumnValues("targetvolumeunits", 0, targetvolumeunits, ";");
        contentsInZoneDS.addColumnValues("targetconc", 0, targetconcs, ";");
        contentsInZoneDS.addColumnValues("targetconcunits", 0, targetconcunits, ";");
        contentsInZoneDS.padColumn("contentsdcid");
        for (int i = 0; i < contentsInZoneDS.getRowCount(); ++i) {
            if ("TrackItemSDC".equals(contentsInZoneDS.getString(i, "contentsdcid"))) {
                String reagentsql = "SELECT reagenttypeid, reagenttypeversionid FROM reagentlot WHERE reagentlotid IN (SELECT linkkeyid1 FROM trackitem WHERE linksdcid='LV_ReagentLot' and trackitemid = ? )";
                DataSet reagentInfo = this.getQueryProcessor().getPreparedSqlDataSet(reagentsql, new Object[]{contentsInZoneDS.getString(i, "contentkeyid1")});
                if (reagentInfo.getRowCount() > 0) {
                    contentsInZoneDS.setString(i, "reagenttypeid", reagentInfo.getValue(0, "reagenttypeid"));
                    contentsInZoneDS.setString(i, "reagenttypeversionid", reagentInfo.getValue(0, "reagenttypeversionid"));
                    continue;
                }
                throw new SapphireException("ReagentType cannot be determined for one or more trackitems. Please check contentkeyid1");
            }
            if (!"LV_Treatment".equals(contentsInZoneDS.getString(i, "contentsdcid"))) continue;
            contentsInZoneDS.setString(i, "treatmenttypeid", contentsInZoneDS.getString(i, "contentkeyid1"));
        }
        if (zonename == null || zonename.trim().length() == 0) {
            throw new SapphireException("Zone Name mandatory");
        }
        String sql = "SELECT arrayid, arrayzone.zone,        contenttype,contentitem,        arrayzonecontent.arrayzoneid,        arrayzonecontent.arrayzonecontentid, arrayzonecontent.reagenttypeid, arrayzonecontent.reagenttypeversionid, arrayzonecontent.treatmenttypeid,        arrayzonecontent.volumehandlingflag,        (select count(*) from arrayitemarrayzone where arrayzoneid=arrayzonecontent.arrayzoneid) itemscount FROM   arrayzone        JOIN arrayzonecontent          ON arrayzone.arrayzoneid = arrayzonecontent.arrayzoneid   WHERE  arrayid = ?        AND zone = ?        AND levelflag = 'Z' AND arraymethodid=? AND arraymethodversionid=?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        safeSQL.addVar(zonename);
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodversionid);
        DataSet zoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (zoneDS == null || zoneDS.isEmpty()) {
            throw new SapphireException("Zone level loading cannot be performed on the named zone: " + zonename + " for arrayid:" + arrayid);
        }
        String arrayzoneid = "";
        for (int currentcontent = 0; currentcontent < contentsInZoneDS.getRowCount(); ++currentcontent) {
            DataSet filteredDataSet;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("zone", zonename);
            filter.put("contentitem", contentsInZoneDS.getValue(currentcontent, "contentitem"));
            filter.put("arrayid", arrayid);
            if (contentsInZoneDS.getString(currentcontent, "treatmenttypeid", "").length() > 0) {
                filter.put("treatmenttypeid", contentsInZoneDS.getString(currentcontent, "treatmenttypeid", ""));
            }
            if ((filteredDataSet = zoneDS.getFilteredDataSet(filter)).isEmpty()) {
                throw new SapphireException("Invalid contents specified for zone:" + zonename);
            }
            if (filteredDataSet.size() > 1) {
                throw new SapphireException("Multiple Contents of same type exist");
            }
            String arrayzonecontentid = filteredDataSet.getString(0, "arrayzonecontentid");
            String contentType = filteredDataSet.getValue(0, "contenttype");
            String reagenttypeid = filteredDataSet.getValue(0, "reagenttypeid");
            String contentLabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, filteredDataSet.getValue(0, "contenttype"), contentsInZoneDS.getValue(currentcontent, "contentkeyid1"), reagenttypeid);
            arrayzoneid = filteredDataSet.getValue(0, "arrayzoneid");
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("arrayid", arrayid);
            propertyList.setProperty("contentsdcid", contentsInZoneDS.getValue(currentcontent, "contentsdcid"));
            propertyList.setProperty("contentkeyid1", contentsInZoneDS.getValue(currentcontent, "contentkeyid1"));
            propertyList.setProperty("contentkeyid2", contentsInZoneDS.getValue(currentcontent, "contentkeyid2"));
            propertyList.setProperty("contentkeyid3", contentsInZoneDS.getValue(currentcontent, "contentkeyid3"));
            propertyList.setProperty("volume", contentsInZoneDS.getValue(currentcontent, "targetvolume"));
            propertyList.setProperty("volumeunits", contentsInZoneDS.getValue(currentcontent, "targetvolumeunits"));
            propertyList.setProperty("concentration", contentsInZoneDS.getValue(currentcontent, "targetconc"));
            propertyList.setProperty("concentrationunits", contentsInZoneDS.getValue(currentcontent, "targetconcunits"));
            propertyList.setProperty("arrayzoneid", arrayzoneid);
            propertyList.setProperty("arrayzonecontentid", arrayzonecontentid);
            propertyList.setProperty("level", "zone");
            propertyList.setProperty("contenttype", contentType);
            propertyList.setProperty("contentitem", contentsInZoneDS.getValue(currentcontent, "contentitem"));
            propertyList.setProperty("contentlabel", contentLabel);
            propertyList.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            propertyList.setProperty("auditactivity", properties.getProperty("auditactivity"));
            propertyList.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("AddArrayContent", "1", propertyList);
            if (arrayzoneid.length() <= 0) continue;
            PropertyList arrayItemProps = new PropertyList();
            String itemSQL = "SELECT arrayitemid, arrayzoneid from arrayitemarrayzone where arrayzoneid IN ('" + arrayzoneid + "')";
            DataSet arrayItemList = this.getQueryProcessor().getSqlDataSet(itemSQL);
            if (arrayItemList == null || arrayItemList.getRowCount() <= 0) continue;
            String vol = contentsInZoneDS.getValue(currentcontent, "targetvolume", "");
            if (vol.length() > 0) {
                vol = "+" + vol;
            }
            arrayItemProps.setProperty("arrayitemid", arrayItemList.getColumnValues("arrayitemid", ";"));
            arrayItemProps.setProperty("totalvol", vol);
            arrayItemProps.setProperty("totalvolunits", contentsInZoneDS.getValue(currentcontent, "targetvolumeunits"));
            arrayItemProps.setProperty("totalconc", contentsInZoneDS.getValue(currentcontent, "targetconc"));
            arrayItemProps.setProperty("totalconcunits", contentsInZoneDS.getValue(currentcontent, "targetconcunits"));
            arrayItemProps.setProperty("validatetotalvolume", validateQty ? "Y" : "N");
            arrayItemProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayItemProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayItemProps.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditArrayItem", "1", arrayItemProps);
        }
        M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
        ArrayVolumeHandlingUtil arrayVolumeHandlingUtil = new ArrayVolumeHandlingUtil(m18NUtil);
        DataSet contentDS = new DataSet();
        contentDS.addColumnValues("contentsdcid", 0, contentSDCIDs, ";");
        contentDS.addColumnValues("contentkeyid1", 0, contentkeyid1s, ";");
        contentDS.addColumnValues("targetvolume", 1, targetvolumes, ";");
        contentDS.addColumnValues("volumeunits", 0, targetvolumeunits, ";");
        contentDS.addColumnValues("volumehandlingflag", 0, zoneDS.getString(0, "volumehandlingflag"), ";");
        contentDS.addColumnValues("repeatcount", 1, zoneDS.getValue(0, "itemscount"), ";");
        contentDS.addColumnValues("sourcevolume", 0, sourcevolume, ";");
        contentDS.addColumnValues("sourcevolumeunit", 0, sourcevolumeunits, ";");
        contentDS.padColumns();
        contentDS.addColumn("volume", 0);
        arrayVolumeHandlingUtil.adjustSourceInv(contentDS, this.getActionProcessor(), this.getQueryProcessor(), validateQty, this.connectionInfo);
        this.updateArrayArrayMethodItemStatus(properties, arrayid, "Loaded");
    }

    private void updateArrayArrayMethodItemStatus(PropertyList properties, String arrayid, String status) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus FROM arrayarraymethoditem WHERE arrayid=? order by usersequence desc";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0 && !ds.getString(0, "arraymethoditemstatus").equals(status)) {
            PropertyList arrayarraymethoditemprops = new PropertyList();
            arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
            arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
            arrayarraymethoditemprops.setProperty("arrayid", arrayid);
            arrayarraymethoditemprops.setProperty("arraymethodid", ds.getString(0, "arraymethodid"));
            arrayarraymethoditemprops.setProperty("arraymethodversionid", ds.getString(0, "arraymethodversionid"));
            arrayarraymethoditemprops.setProperty("arraymethodinstance", "" + ds.getInt(0, "arraymethodinstance"));
            arrayarraymethoditemprops.setProperty("arraymethoditemstatus", status);
            arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
        }
    }

    private void fillIndividualWells(PropertyList properties, DataSet arraymethoddefaults, String contentitem, PropertyList blockproperties, DataSet arraymethodcontent, boolean validateQty) throws SapphireException {
        DataSet arrayitemarrayzone;
        DataSet filteredDataSet;
        DataSet arrayitemDS;
        String arrayid = blockproperties.getProperty("arrayid");
        String contentkeyid1s = blockproperties.getProperty("contentkeyid1");
        String contentkeyid2s = blockproperties.getProperty("contentkeyid2", "");
        String contentkeyid3s = blockproperties.getProperty("contentkeyid3", "");
        String targetvolume = blockproperties.getProperty("volume", "0");
        String targetvolumeunits = blockproperties.getProperty("volumeunits");
        String sourcevolume = blockproperties.getProperty("sourcevolume", "");
        String sourcevolumeunits = blockproperties.getProperty("sourcevolumeunit");
        String targetconcentration = blockproperties.getProperty("concentration", "");
        String targetconcentrationunits = blockproperties.getProperty("concentrationunits");
        String contentSDCID = blockproperties.getProperty("contentsdcid");
        String loadingDirection = blockproperties.getProperty("loaddirection");
        String rowLabeldeprecated = blockproperties.getProperty("startrowlabel", "");
        String colLabeldeprecated = blockproperties.getProperty("startcolumnlabel", "");
        String startitemlabel = blockproperties.getProperty("startitemlabel", "");
        String startitemid = blockproperties.getProperty("startitemid", "");
        String zonename = blockproperties.getProperty("zone", blockproperties.getProperty("zonename"));
        HashMap<String, String> filtermap = new HashMap<String, String>();
        DataSet content = new DataSet();
        content.addColumnValues("contentsdcid", 0, contentSDCID, ";");
        content.addColumnValues("contentkeyid1", 0, contentkeyid1s, ";");
        content.addColumnValues("contentkeyid2", 0, contentkeyid2s, ";");
        content.addColumnValues("contentkeyid3", 0, contentkeyid3s, ";");
        content.addColumnValues("targetvolume", 0, targetvolume, ";");
        content.addColumnValues("targetvolumeunits", 0, targetvolumeunits, ";");
        content.addColumnValues("sourcevolume", 0, sourcevolume, ";");
        content.addColumnValues("sourcevolumeunit", 0, sourcevolumeunits, ";");
        content.addColumnValues("targetconcentration", 0, targetconcentration, ";");
        content.addColumnValues("targetconcentrationunits", 0, targetconcentrationunits, ";");
        content.padColumn("contentsdcid");
        content.padColumn("targetvolume");
        content.padColumn("targetvolumeunits");
        content.padColumn("targetconcentration");
        content.padColumn("targetconcentrationunits");
        String defaultForCreateChild = arraymethoddefaults.getString(0, "createchildsampleflag", "N");
        String defaultForChildSampleType = arraymethoddefaults.getString(0, "sampletypeid", "");
        String createChild = blockproperties.getProperty("createchild", defaultForCreateChild);
        String childSampleType = blockproperties.getProperty("childsampletype", defaultForChildSampleType);
        boolean derivative = false;
        if ("Y".equals(createChild) && childSampleType.length() > 0) {
            derivative = true;
        }
        int colIndex = 0;
        int rowIndex = 0;
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_Array");
        sdiRequest.setKeyid1List(arrayid);
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        SDIData arrayitemData = this.fetchArrayItemData(arrayid);
        SDIData arrayzoneData = this.fetchArrayZoneData(arrayid);
        DataSet arrayzoneDS = arrayzoneData.getDataset("primary");
        filtermap.clear();
        filtermap.put("arrayid", arrayid);
        filtermap.put("zone", zonename);
        DataSet currentZoneData = arrayzoneDS.getFilteredDataSet(filtermap);
        if (currentZoneData == null || currentZoneData.getRowCount() == 0) {
            throw new SapphireException("Invalid Zone Name");
        }
        if (startitemlabel.length() > 0) {
            arrayitemDS = arrayitemData.getDataset("primary");
            filtermap.clear();
            filtermap.put("arrayid", arrayid);
            filtermap.put("itemlabel", startitemlabel);
            filteredDataSet = arrayitemDS.getFilteredDataSet(filtermap);
            if (filteredDataSet.isEmpty()) {
                throw new SapphireException("Invalid startitemlabel specified");
            }
            rowIndex = filteredDataSet.getInt(0, "xpos");
            colIndex = filteredDataSet.getInt(0, "ypos");
            filtermap.clear();
            filtermap.put("arrayitemid", filteredDataSet.getString(0, "arrayitemid"));
            filtermap.put("arrayzoneid", currentZoneData.getString(0, "arrayzoneid"));
            arrayitemarrayzone = arrayzoneData.getDataset("arrayitemarrayzone").getFilteredDataSet(filtermap);
            if (arrayitemarrayzone.getRowCount() == 0 || arrayitemarrayzone.getString(0, "contentstring", "").length() == 0) {
                throw new SapphireException("The start position does not have arrangement rules defined.");
            }
        } else if (startitemid.length() > 0) {
            arrayitemDS = arrayitemData.getDataset("primary");
            filtermap.clear();
            filtermap.put("arrayid", arrayid);
            filtermap.put("arrayitemid", startitemid);
            filteredDataSet = arrayitemDS.getFilteredDataSet(filtermap);
            if (filteredDataSet.isEmpty()) {
                throw new SapphireException("Invalid startitemid specified");
            }
            rowIndex = filteredDataSet.getInt(0, "xpos");
            colIndex = filteredDataSet.getInt(0, "ypos");
            filtermap.clear();
            filtermap.put("arrayitemid", filteredDataSet.getString(0, "arrayitemid"));
            filtermap.put("arrayzoneid", currentZoneData.getString(0, "arrayzoneid"));
            arrayitemarrayzone = arrayzoneData.getDataset("arrayitemarrayzone").getFilteredDataSet(filtermap);
            if (arrayitemarrayzone.getRowCount() == 0 || arrayitemarrayzone.getString(0, "contentstring", "").length() == 0) {
                throw new SapphireException("The start position does not have arrangement rules defined.");
            }
        } else if (rowLabeldeprecated.length() > 0 && colLabeldeprecated.length() > 0) {
            arrayitemDS = arrayitemData.getDataset("primary");
            filtermap.clear();
            filtermap.put("arrayid", arrayid);
            filtermap.put("horizontallabel", colLabeldeprecated);
            filtermap.put("verticallabel", rowLabeldeprecated);
            filteredDataSet = arrayitemDS.getFilteredDataSet(filtermap);
            if (filteredDataSet.isEmpty()) {
                throw new SapphireException("Invalid Row, Column Label combination");
            }
            rowIndex = filteredDataSet.getInt(0, "xpos");
            colIndex = filteredDataSet.getInt(0, "ypos");
        } else if (zonename.length() > 0) {
            String startPosition;
            if (loadingDirection.equals("")) {
                arrayzoneDS = arrayzoneData.getDataset("primary");
                filtermap.clear();
                filtermap.put("arrayid", arrayid);
                filtermap.put("zone", zonename);
                if (arrayzoneDS != null) {
                    int row = arrayzoneDS.findRow(filtermap);
                    if (row < 0) {
                        throw new SapphireException("Invalid Zone Name");
                    }
                    String contentdirection = arrayzoneDS.getValue(row, "contentdirection", "H");
                    loadingDirection = "H".equalsIgnoreCase(contentdirection) ? "Horizontal" : "Vertical";
                } else {
                    throw new SapphireException("Invalid zone name");
                }
            }
            if ((startPosition = this.getFirstWell(arrayzoneData, arrayid, zonename, loadingDirection)).length() == 0) {
                throw new SapphireException("No Empty cell found for the zone provided");
            }
            String[] split = StringUtil.split(startPosition, ";");
            rowIndex = Integer.valueOf(split[0]);
            colIndex = Integer.valueOf(split[1]);
        } else {
            throw new SapphireException("Provide either the rowlabel, columnlabel combination or the zonename in order to perform loading");
        }
        DataSet arrayDS = sdiData.getDataset("primary");
        String arraytypeid = arrayDS.getValue(0, "arraytypeid");
        String versionid = arrayDS.getValue(0, "arraytypeversionid");
        DataSet arrayzone = arrayzoneData.getDataset("primary");
        DataSet arrayzoneitem = arrayzoneData.getDataset("arrayitemarrayzone");
        filtermap.clear();
        String arrayitemid = arrayid + "_" + rowIndex + "_" + colIndex;
        filtermap.put("arrayitemid", arrayitemid);
        if (zonename.trim().length() > 0) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayid", arrayid);
            filter.put("zone", zonename);
            int row = arrayzone.findRow(filter);
            filtermap.put("arrayzoneid", arrayzone.getValue(row, "arrayzoneid"));
        }
        DataSet zoneitemDS = arrayzoneitem.getFilteredDataSet(filtermap);
        String zoneid = zoneitemDS.getValue(0, "arrayzoneid");
        filtermap.clear();
        filtermap.put("arrayid", arrayid);
        filtermap.put("arrayzoneid", zoneid);
        DataSet zoneDS = arrayzone.getFilteredDataSet(filtermap);
        if (loadingDirection.equals("")) {
            loadingDirection = "H".equalsIgnoreCase(zoneDS.getValue(0, "contentdirection", "H")) ? "Horizontal" : "Vertical";
        }
        filtermap.clear();
        filtermap.put("arrayzoneid", zoneid);
        DataSet filteredZoneItem = arrayzoneitem.getFilteredDataSet(filtermap);
        Set<String> filledItems = this.getFilledItems(arrayitemData);
        DataSet filteredZoneContent = arrayzoneData.getDataset("arrayzonecontent").getFilteredDataSet(filtermap);
        HashMap<String, String> coorContentMap = new HashMap<String, String>();
        String availableCells = "";
        for (int i = 0; i < filteredZoneItem.size(); ++i) {
            String arrayitmid = filteredZoneItem.getValue(i, "arrayitemid");
            String[] arrayitemidsplit = StringUtil.split(arrayitmid, "_");
            String xpos = arrayitemidsplit[1];
            String ypos = arrayitemidsplit[2];
            int rIndex = (int)Float.parseFloat(xpos);
            int cIndex = (int)Float.parseFloat(ypos);
            if (!filledItems.contains(arrayitmid)) {
                availableCells = availableCells + ";" + rIndex + "," + cIndex;
            }
            String contentstring = filteredZoneItem.getValue(i, "contentstring");
            String key = xpos + "," + ypos;
            coorContentMap.put(key, contentstring);
        }
        availableCells = availableCells.length() > 0 ? availableCells.substring(1) : availableCells;
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraytypeid);
        safeSQL.addVar(versionid);
        DataSet arraytypeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * from arraytype where arraytypeid=? and arraytypeversionid=?", safeSQL.getValues());
        int rows = arraytypeDS.getInt(0, "numrows");
        int cols = arraytypeDS.getInt(0, "numcolumns");
        int rowCount = rows;
        int colCount = cols;
        WellValues[][] layoutMap = new WellValues[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                WellValues wellValue;
                String key = i + "," + j;
                String contentstring = (String)coorContentMap.get(key);
                if (contentstring != null && contentstring.length() > 0) {
                    wellValue = new WellValues();
                    String[] contents = StringUtil.split(contentstring, ";");
                    wellValue.sample = Integer.parseInt(contents[0]);
                    wellValue.repeat = Integer.parseInt(contents[1]);
                    wellValue.treatment = Integer.parseInt(contents[2]);
                    wellValue.dilutionfactor = Float.parseFloat(contents[3]);
                    wellValue.dilution = Integer.parseInt(contents[4]);
                    layoutMap[i][j] = wellValue;
                    continue;
                }
                wellValue = new WellValues();
                wellValue.sample = -1;
                wellValue.repeat = 1;
                wellValue.treatment = 0;
                wellValue.dilutionfactor = 0.0f;
                wellValue.dilution = 0;
                layoutMap[i][j] = wellValue;
            }
        }
        EvaluateArrayLayoutRules evalRules = new EvaluateArrayLayoutRules();
        WellValues[][] loadedWellValues = evalRules.applyLoadingDirection(contentkeyid1s, rowIndex + "," + colIndex, availableCells, loadingDirection, layoutMap);
        DataSet arrayitemcontentDS = new DataSet();
        arrayitemcontentDS.addColumn("arrayitemid", 0);
        arrayitemcontentDS.addColumn("contentsdcid", 0);
        arrayitemcontentDS.addColumn("contentkeyid1", 0);
        arrayitemcontentDS.addColumn("contentkeyid2", 0);
        arrayitemcontentDS.addColumn("contentkeyid3", 0);
        arrayitemcontentDS.addColumn("volume", 1);
        arrayitemcontentDS.addColumn("volumeunits", 0);
        arrayitemcontentDS.addColumn("concentration", 1);
        arrayitemcontentDS.addColumn("concentrationunits", 0);
        arrayitemcontentDS.addColumn("arrayitemcontentid", 0);
        arrayitemcontentDS.addColumn("contenttype", 0);
        arrayitemcontentDS.addColumn("contentlabel", 0);
        arrayitemcontentDS.addColumn("arrayzoneid", 0);
        arrayitemcontentDS.addColumn("repeatnum", 1);
        arrayitemcontentDS.addColumn("dilutionfactor", 1);
        arrayitemcontentDS.addColumn("diluentvolume", 1);
        arrayitemcontentDS.addColumn("diluentvolumeunits", 0);
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        for (int h = 0; h < loadedWellValues.length; ++h) {
            for (int v = 0; v < loadedWellValues[0].length; ++v) {
                DataSet sourceConcDetails;
                if (loadedWellValues[h][v] == null || loadedWellValues[h][v].sampleid == null || loadedWellValues[h][v].sampleid.length() <= 0) continue;
                HashMap<String, String> match = new HashMap<String, String>();
                match.put("contentkeyid1", loadedWellValues[h][v].sampleid);
                DataSet matchContentDetails = content.getFilteredDataSet(match);
                if (matchContentDetails == null || matchContentDetails.getRowCount() == 0) {
                    throw new SapphireException("Failed to find content details");
                }
                String currtargetvolume = matchContentDetails.getString(0, "targetvolume", "");
                String currtargetvolumeunits = matchContentDetails.getString(0, "targetvolumeunits", "");
                BigDecimal currtargetconc = null;
                if (matchContentDetails.getString(0, "targetconcentration", "").trim().length() > 0) {
                    currtargetconc = this.m18NUtil.parseBigDecimal(matchContentDetails.getString(0, "targetconcentration"));
                }
                String currtargetconcunits = matchContentDetails.getString(0, "targetconcentrationunits", "");
                String currcontensdcid = matchContentDetails.getString(0, "contentsdcid", "");
                if (currcontensdcid.equals("LV_Treatment")) {
                    Trace.logDebug("No volume management for Treatment:" + loadedWellValues[h][v].sampleid);
                    String arrayitmid = arrayid + "_" + h + "_" + v;
                    int rowindex = arrayitemcontentDS.addRow();
                    arrayitemcontentDS.setString(rowindex, "arrayitemid", arrayitmid);
                    arrayitemcontentDS.setString(rowindex, "arrayzoneid", zoneid);
                    arrayitemcontentDS.setString(rowindex, "contentsdcid", currcontensdcid);
                    arrayitemcontentDS.setString(rowindex, "contentkeyid1", loadedWellValues[h][v].sampleid);
                    arrayitemcontentDS.setString(rowindex, "contentkeyid2", "");
                    arrayitemcontentDS.setString(rowindex, "contentkeyid3", "");
                    if (currtargetvolume.length() > 0) {
                        arrayitemcontentDS.setNumber(rowindex, "volume", this.m18NUtil.parseBigDecimal(currtargetvolume));
                    }
                    arrayitemcontentDS.setString(rowindex, "volumeunits", currtargetvolumeunits);
                    arrayitemcontentDS.setNumber(rowindex, "concentration", "");
                    arrayitemcontentDS.setString(rowindex, "concentrationunits", currtargetconcunits);
                    arrayitemcontentDS.setString(rowindex, "contenttype", "LV_Treatment");
                    arrayitemcontentDS.setString(rowindex, "contentitem", contentitem);
                    arrayitemcontentDS.setString(rowindex, "parentarrayitemid", "");
                    String contentlabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, "LV_Treatment", loadedWellValues[h][v].sampleid, "");
                    if (contentlabel == null || contentlabel.length() == 0) {
                        throw new SapphireException("Failed to create treatment label for  " + loadedWellValues[h][v].sampleid);
                    }
                    arrayitemcontentDS.setString(rowindex, "contentlabel", contentlabel);
                    continue;
                }
                ArrayVolumeHandlingUtil util = new ArrayVolumeHandlingUtil(this.m18NUtil);
                BigDecimal sourceConcentration = null;
                String sourceConcentrationUnits = "";
                if (!contentSDCID.startsWith("TrackItemSDC") && (sourceConcDetails = util.findSourceConcentration(contentSDCID, loadedWellValues[h][v].sampleid, this.getDAMProcessor(), this.getQueryProcessor())) != null) {
                    sourceConcentration = sourceConcDetails.getBigDecimal(0, "concentration");
                    sourceConcentrationUnits = sourceConcDetails.getString(0, "concentrationunits", "");
                }
                if (currtargetconc == null && !derivative) {
                    currtargetconc = sourceConcentration;
                    currtargetconcunits = sourceConcentrationUnits;
                }
                int row = arrayitemcontentDS.addRow();
                Double evaluateConcentration = null;
                if (currtargetconc != null) {
                    double d = currtargetconc.doubleValue();
                    evaluateConcentration = d;
                    if (loadedWellValues[h][v].dilutionfactor > 0.0f) {
                        evaluateConcentration = d / (double)loadedWellValues[h][v].dilutionfactor;
                    }
                }
                String arrayitmid = arrayid + "_" + h + "_" + v;
                arrayitemcontentDS.setString(row, "arrayitemid", arrayitmid);
                arrayitemcontentDS.setString(row, "arrayzoneid", zoneid);
                arrayitemcontentDS.setString(row, "contentsdcid", currcontensdcid);
                arrayitemcontentDS.setString(row, "contentkeyid1", loadedWellValues[h][v].sampleid);
                arrayitemcontentDS.setString(row, "contentkeyid2", "");
                arrayitemcontentDS.setString(row, "contentkeyid3", "");
                if (currtargetvolume.length() > 0) {
                    arrayitemcontentDS.setNumber(row, "volume", this.m18NUtil.parseBigDecimal(currtargetvolume));
                }
                arrayitemcontentDS.setString(row, "volumeunits", currtargetvolumeunits);
                if (evaluateConcentration != null) {
                    arrayitemcontentDS.setNumber(row, "concentration", df.format(evaluateConcentration));
                }
                arrayitemcontentDS.setString(row, "concentrationunits", currtargetconcunits);
                arrayitemcontentDS.setString(row, "contenttype", currcontensdcid.equalsIgnoreCase("TrackItemSDC") ? "LV_ReagentLot" : currcontensdcid);
                arrayitemcontentDS.setString(row, "contentitem", contentitem);
                arrayitemcontentDS.setString(row, "parentarrayitemid", "");
                if (loadedWellValues[h][v].dilutionfactor > 0.0f) {
                    arrayitemcontentDS.setNumber(row, "dilutionfactor", loadedWellValues[h][v].dilutionfactor);
                    if (arrayitemcontentDS.getDouble(row, "dilutionfactor") != (double)loadedWellValues[h][v].dilutionfactor) {
                        throw new SapphireException("Failed to set dilution factor");
                    }
                }
                if (loadedWellValues[h][v].repeat > 0) {
                    arrayitemcontentDS.setNumber(row, "repeatnum", loadedWellValues[h][v].repeat);
                    if (arrayitemcontentDS.getInt(row, "repeatnum") != loadedWellValues[h][v].repeat) {
                        throw new SapphireException("Failed to set repeatnum");
                    }
                }
                String contentlabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, currcontensdcid.equalsIgnoreCase("TrackItemSDC") ? "LV_ReagentLot" : currcontensdcid, loadedWellValues[h][v].sampleid, "");
                arrayitemcontentDS.setString(row, "contentlabel", contentlabel);
                double currtargetvolumeN = this.m18NUtil.parseBigDecimal(currtargetvolume).doubleValue();
                double dsourcevolume = util.findAmountToBeDecremented(loadedWellValues[h][v].repeat, loadedWellValues[h][v].dilutionfactor, zoneDS.getValue(0, "dilutefirstflag", "Y"), currtargetconc != null ? this.m18NUtil.format(currtargetconc) : null, currtargetconcunits, sourceConcentration != null ? this.m18NUtil.format(sourceConcentration) : null, sourceConcentrationUnits, currtargetvolumeN, this.connectionInfo);
                double diluentVolume = ArrayUtil.getDilutentVolume(this.connectionInfo, this.m18NUtil, sourceConcentration != null ? this.m18NUtil.format(sourceConcentration) : null, sourceConcentrationUnits, dsourcevolume, currtargetconc != null ? this.m18NUtil.format(currtargetconc) : null, currtargetconcunits, currtargetvolumeN);
                if (diluentVolume >= 0.0) {
                    arrayitemcontentDS.setNumber(row, "diluentvolume", diluentVolume);
                    arrayitemcontentDS.setString(row, "diluentvolumeunits", currtargetvolumeunits);
                }
                arrayitemcontentDS.setString(row, "arrayitemcontentid", "");
                if (!contentSDCID.contains("LV_ArrayItem")) continue;
                String sourcearrayitemid = loadedWellValues[h][v].sampleid;
                String sourcecontentsql = "SELECT * FROM arrayitemcontent WHERE arrayitemid = ?";
                SafeSQL safeSQL1 = new SafeSQL();
                safeSQL1.addVar(sourcearrayitemid);
                DataSet sourcecontentdetails = this.getQueryProcessor().getPreparedSqlDataSet(sourcecontentsql, safeSQL1.getValues());
                if (sourcecontentdetails != null) {
                    for (int contents = 0; contents < sourcecontentdetails.getRowCount(); ++contents) {
                        String sourceitemcontent = sourcecontentdetails.getString(contents, "contentitem");
                        if (LoadArray.checkPropagateContent(contentitem, sourceitemcontent, arraymethodcontent, zonename)) {
                            Trace.log("Check propagatecontent returned true for:" + sourceitemcontent);
                            row = arrayitemcontentDS.addRow();
                            arrayitemcontentDS.setString(row, "arrayitemid", arrayitmid);
                            arrayitemcontentDS.setString(row, "arrayzoneid", zoneid);
                            arrayitemcontentDS.setString(row, "contentsdcid", sourcecontentdetails.getString(contents, "contentsdcid"));
                            arrayitemcontentDS.setString(row, "contentkeyid1", sourcecontentdetails.getString(contents, "contentkeyid1"));
                            arrayitemcontentDS.setString(row, "contentkeyid2", sourcecontentdetails.getString(contents, "contentkeyid2"));
                            arrayitemcontentDS.setString(row, "contentkeyid3", sourcecontentdetails.getString(contents, "contentkeyid3"));
                            arrayitemcontentDS.setNumber(row, "volume", sourcecontentdetails.getBigDecimal(contents, "volume"));
                            arrayitemcontentDS.setString(row, "volumeunits", sourcecontentdetails.getString(contents, "volumeunits"));
                            arrayitemcontentDS.setNumber(row, "concentration", sourcecontentdetails.getBigDecimal(contents, "concentration"));
                            arrayitemcontentDS.setString(row, "concentrationunits", sourcecontentdetails.getString(contents, "concentrationunits"));
                            arrayitemcontentDS.setString(row, "parentarrayitemid", sourcearrayitemid);
                            arrayitemcontentDS.setString(row, "contenttype", sourcecontentdetails.getString(contents, "contenttype"));
                            arrayitemcontentDS.setString(row, "contentitem", sourcecontentdetails.getString(contents, "contentitem"));
                            arrayitemcontentDS.setString(row, "contentlabel", sourcecontentdetails.getString(contents, "contentlabel"));
                            if (sourcecontentdetails.getValue(contents, "dilutionfactor") != null && sourcecontentdetails.getValue(contents, "dilutionfactor").length() > 0) {
                                arrayitemcontentDS.setNumber(row, "dilutionfactor", sourcecontentdetails.getDouble(contents, "dilutionfactor"));
                            }
                            if (sourcecontentdetails.getValue(contents, "repeatnum") == null || sourcecontentdetails.getValue(contents, "repeatnum").length() <= 0) continue;
                            arrayitemcontentDS.setNumber(row, "repeatnum", sourcecontentdetails.getInt(contents, "repeatnum"));
                            continue;
                        }
                        Trace.log("Check propagatecontent returned false for:" + sourceitemcontent);
                    }
                }
                String sourcezonecontentsql = "select * from arrayzonecontent where arrayzoneid in ( select distinct arrayzoneid from arrayitemarrayzone where arrayitemid = ? ) and contentkeyid1 is not null";
                SafeSQL safeSQL2 = new SafeSQL();
                safeSQL2.addVar(sourcearrayitemid);
                DataSet sourcezonecontentdetails = this.getQueryProcessor().getPreparedSqlDataSet(sourcezonecontentsql, safeSQL2.getValues());
                if (sourcezonecontentdetails == null) continue;
                for (int contents = 0; contents < sourcezonecontentdetails.getRowCount(); ++contents) {
                    String sourcezonecontent = sourcezonecontentdetails.getString(contents, "contentitem");
                    if (LoadArray.checkPropagateContent(contentitem, sourcezonecontent, arraymethodcontent, zonename)) {
                        Trace.log("Check propagatecontent returned true for:" + sourcezonecontent);
                        row = arrayitemcontentDS.addRow();
                        arrayitemcontentDS.setString(row, "arrayitemid", arrayitmid);
                        arrayitemcontentDS.setString(row, "arrayzoneid", zoneid);
                        arrayitemcontentDS.setString(row, "contentsdcid", sourcezonecontentdetails.getString(contents, "contentsdcid"));
                        arrayitemcontentDS.setString(row, "contentkeyid1", sourcezonecontentdetails.getString(contents, "contentkeyid1"));
                        arrayitemcontentDS.setString(row, "contentkeyid2", sourcezonecontentdetails.getString(contents, "contentkeyid2"));
                        arrayitemcontentDS.setString(row, "contentkeyid3", sourcezonecontentdetails.getString(contents, "contentkeyid3"));
                        arrayitemcontentDS.setNumber(row, "volume", sourcezonecontentdetails.getBigDecimal(contents, "volume"));
                        arrayitemcontentDS.setString(row, "volumeunits", sourcezonecontentdetails.getString(contents, "volumeunits"));
                        arrayitemcontentDS.setNumber(row, "concentration", sourcezonecontentdetails.getBigDecimal(contents, "concentration"));
                        arrayitemcontentDS.setString(row, "concentrationunits", sourcezonecontentdetails.getString(contents, "concentrationunits"));
                        arrayitemcontentDS.setString(row, "parentarrayitemid", sourcearrayitemid);
                        arrayitemcontentDS.setString(row, "contenttype", sourcezonecontentdetails.getString(contents, "contenttype"));
                        arrayitemcontentDS.setString(row, "contentitem", sourcezonecontentdetails.getString(contents, "contentitem"));
                        arrayitemcontentDS.setString(row, "contentlabel", sourcezonecontentdetails.getString(contents, "contentlabel"));
                        continue;
                    }
                    Trace.log("Check propagatecontent returned false for:" + sourcezonecontent);
                }
            }
        }
        arrayitemcontentDS = ArrayUtil.processChildSamples(this.getConnectionid(), createChild, childSampleType, arrayitemcontentDS, loadingDirection, rowCount, colCount);
        if (arrayitemcontentDS.getRowCount() <= 0) {
            throw new SapphireException("No contents loaded");
        }
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("arrayid", arrayid);
        propertyList.setProperty("contentsdcid", arrayitemcontentDS.getColumnValues("contentsdcid", ";"));
        propertyList.setProperty("contentkeyid1", arrayitemcontentDS.getColumnValues("contentkeyid1", ";"));
        propertyList.setProperty("contentkeyid2", arrayitemcontentDS.getColumnValues("contentkeyid2", ";"));
        propertyList.setProperty("contentkeyid3", arrayitemcontentDS.getColumnValues("contentkeyid3", ";"));
        propertyList.setProperty("volume", arrayitemcontentDS.getColumnValues("volume", ";"));
        propertyList.setProperty("volumeunits", arrayitemcontentDS.getColumnValues("volumeunits", ";"));
        propertyList.setProperty("concentration", arrayitemcontentDS.getColumnValues("concentration", ";"));
        propertyList.setProperty("concentrationunits", arrayitemcontentDS.getColumnValues("concentrationunits", ";"));
        propertyList.setProperty("arrayzoneid", arrayitemcontentDS.getColumnValues("arrayzoneid", ";"));
        propertyList.setProperty("diluentvolume", arrayitemcontentDS.getColumnValues("diluentvolume", ";"));
        propertyList.setProperty("diluentvolumeunits", arrayitemcontentDS.getColumnValues("diluentvolumeunits", ";"));
        propertyList.setProperty("contentitem", arrayitemcontentDS.getColumnValues("contentitem", ";"));
        propertyList.setProperty("arrayitemid", arrayitemcontentDS.getColumnValues("arrayitemid", ";"));
        propertyList.setProperty("parentarrayitemid", arrayitemcontentDS.getColumnValues("parentarrayitemid", ";"));
        propertyList.setProperty("dilutionfactor", arrayitemcontentDS.getColumnValues("dilutionfactor", ";"));
        propertyList.setProperty("repeatnum", arrayitemcontentDS.getColumnValues("repeatnum", ";"));
        propertyList.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        propertyList.setProperty("auditactivity", properties.getProperty("auditactivity"));
        propertyList.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("AddArrayContent", "1", propertyList);
        this.updateArrayArrayMethodItemStatus(properties, arrayid, "Loaded");
        PropertyList arrayItemProps = new PropertyList();
        DataSet arrayItemList = new DataSet();
        for (int aic = 0; aic < arrayitemcontentDS.getRowCount(); ++aic) {
            String currentarrayitemid = arrayitemcontentDS.getString(aic, "arrayitemid");
            String currentparentid = arrayitemcontentDS.getString(aic, "parentarrayitemid", "");
            if (currentparentid.length() != 0) continue;
            HashMap<String, String> find = new HashMap<String, String>();
            find.put("arrayitemid", currentarrayitemid);
            int match = arrayItemList.findRow(find);
            if (match > -1) {
                arrayItemList.setString(match, "totalvolume", this.getNewTotalVolume(arrayitemcontentDS, match, aic));
                arrayItemList.setString(match, "concentration", this.getNewConcentration(arrayitemcontentDS, match, aic));
                continue;
            }
            int row = arrayItemList.addRow();
            arrayItemList.setString(row, "arrayitemid", currentarrayitemid);
            arrayItemList.setString(row, "totalvolume", "+" + arrayitemcontentDS.getValue(aic, "volume"));
            arrayItemList.setString(row, "totalvolumeunits", arrayitemcontentDS.getValue(aic, "volumeunits", ""));
            arrayItemList.setString(row, "concentration", arrayitemcontentDS.getValue(aic, "concentration", ""));
            arrayItemList.setString(row, "concentrationunits", arrayitemcontentDS.getValue(aic, "concentrationunits", ""));
        }
        arrayItemProps.setProperty("arrayitemid", arrayItemList.getColumnValues("arrayitemid", ";"));
        arrayItemProps.setProperty("totalvol", arrayItemList.getColumnValues("totalvolume", ";"));
        arrayItemProps.setProperty("totalvolunits", arrayItemList.getColumnValues("totalvolumeunits", ";"));
        arrayItemProps.setProperty("totalconc", arrayItemList.getColumnValues("concentration", ";"));
        arrayItemProps.setProperty("totalconcunits", arrayItemList.getColumnValues("concentrationunits", ";"));
        arrayItemProps.setProperty("validatetotalvolume", validateQty ? "Y" : "N");
        arrayItemProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayItemProps.setProperty("auditreason", properties.getProperty("auditreason"));
        arrayItemProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        this.getActionProcessor().processAction("EditArrayItem", "1", arrayItemProps);
        if (!contentitem.equals("Treatment")) {
            int repeatCount = zoneDS.getInt(0, "repeatcount");
            int contentItemCountForZone = filteredZoneItem.size();
            if (repeatCount > 1) {
                contentItemCountForZone /= repeatCount;
            }
            if (zoneDS.getString(0, "dilutiondirection", "").length() > 0 && zoneDS.getInt(0, "dilutionsteps") > 1) {
                contentItemCountForZone /= zoneDS.getInt(0, "dilutionsteps");
            }
            String quantityFlag = filteredZoneContent.getColumnValues("volumehandlingflag", ";");
            String[] contentKeyid1Arr = StringUtil.split(contentkeyid1s, ";");
            ArrayVolumeHandlingUtil arrayVolumeHandlingUtil = new ArrayVolumeHandlingUtil(this.m18NUtil);
            DataSet sourceConcDS = arrayVolumeHandlingUtil.findSourceConcentration(contentSDCID, contentkeyid1s, this.getDAMProcessor(), this.getQueryProcessor());
            DataSet contentDS = new DataSet();
            contentDS.addColumn("contentsdcid", 0);
            contentDS.addColumn("contentkeyid1", 0);
            contentDS.addColumn("volume", 1);
            contentDS.addColumn("volumeunits", 0);
            contentDS.addColumn("volumehandlingflag", 0);
            int contentItemIndex = contentKeyid1Arr.length;
            if (contentKeyid1Arr.length > contentItemCountForZone) {
                contentItemIndex = contentItemCountForZone;
            }
            for (int i = 0; i < contentItemIndex; ++i) {
                String contentKeyID1 = contentKeyid1Arr[i];
                contentDS.addRow();
                contentDS.setString(i, "contentkeyid1", contentKeyID1);
                if (contentSDCID.startsWith("TrackItemSDC")) continue;
                int sourceIndexInDS = sourceConcDS.findRow("sourceid", contentKeyID1);
                if (sourceConcDS.getValue(sourceIndexInDS, "concentration", "").length() > 0) {
                    Double sourceContentConc = sourceConcDS.getDouble(sourceIndexInDS, "concentration");
                    contentDS.setNumber(i, "sourceconcentration", sourceContentConc);
                }
                String sourceContentConcUnits = sourceConcDS.getValue(sourceIndexInDS, "concentrationunits", "");
                contentDS.setString(i, "sourceconcentrationunit", sourceContentConcUnits);
            }
            contentDS.addColumnValues("repeatcount", 1, zoneDS.getColumnValues("repeatcount", ";"), ";");
            contentDS.addColumnValues("dilutionfactor", 1, zoneDS.getColumnValues("dilutionfactor", ";"), ";");
            contentDS.addColumnValues("dilutefirstflag", 0, zoneDS.getColumnValues("dilutefirstflag", ";"), ";", "false");
            contentDS.addColumnValues("targetconcentration", 1, targetconcentration, ";");
            contentDS.addColumnValues("targetconcentrationunit", 0, targetconcentrationunits, ";");
            contentDS.addColumnValues("volumeunits", 0, targetvolumeunits, ";");
            contentDS.addColumnValues("targetvolume", 1, targetvolume, ";");
            contentDS.addColumnValues("volumehandlingflag", 0, quantityFlag, ";", "D");
            contentDS.addColumnValues("contentsdcid", 0, contentSDCID, ";");
            contentDS.addColumnValues("sourcevolume", 0, sourcevolume, ";");
            contentDS.addColumnValues("sourcevolumeunit", 0, sourcevolumeunits, ";");
            contentDS.padColumns();
            try {
                arrayVolumeHandlingUtil.adjustSourceInv(contentDS, this.getActionProcessor(), this.getQueryProcessor(), validateQty, this.connectionInfo);
            }
            catch (SapphireException e) {
                if (e.getMessage().contains("decrement")) {
                    throw new SapphireException("One or more wells cannot be loaded because of insufficient source volumes.");
                }
                throw e;
            }
        }
    }

    private String getNewTotalVolume(DataSet arrayitemcontentDS, int row1, int row2) throws SapphireException {
        String row1VolStr = arrayitemcontentDS.getValue(row1, "volume", "0");
        String row1VolStrUnits = arrayitemcontentDS.getValue(row1, "volumeunits", "");
        String row2VolStr = arrayitemcontentDS.getValue(row2, "volume", "0");
        String row2VolStrUnits = arrayitemcontentDS.getValue(row2, "volumeunits", "");
        BigDecimal bd = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(row2VolStr), row2VolStrUnits, row1VolStrUnits);
        return "+" + bd.add(this.m18NUtil.parseBigDecimal(row1VolStr)).doubleValue();
    }

    private String getNewConcentration(DataSet arrayitemcontentDS, int row1, int row2) throws SapphireException {
        BigDecimal bd;
        double row2val;
        String row1ConcStr = arrayitemcontentDS.getValue(row1, "concentrationtarget", "");
        String row1ConcStrUnits = arrayitemcontentDS.getValue(row1, "concentrationtargetunits", "");
        String row2ConcStr = arrayitemcontentDS.getValue(row2, "concentrationtarget", "");
        String row2ConcStrUnits = arrayitemcontentDS.getValue(row2, "concentrationtargetunits", "");
        double row1val = this.m18NUtil.parseBigDecimal(row1ConcStr).doubleValue();
        if (row1val != (row2val = (bd = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(row2ConcStr), row2ConcStrUnits, row1ConcStrUnits)).doubleValue())) {
            return "";
        }
        return row1ConcStr;
    }

    private Set<String> getFilledItems(SDIData arrayitemData) {
        HashSet<String> filledItems = new HashSet<String>();
        DataSet arrayitemcontent = arrayitemData.getDataset("arrayitemcontent");
        for (int i = 0; i < arrayitemcontent.size(); ++i) {
        }
        return filledItems;
    }

    private String getFirstWell(SDIData arrayzoneData, String arrayid, String zonename, String loadingDirection) throws SapphireException {
        DataSet arrayzoneDS = arrayzoneData.getDataset("primary");
        HashMap<String, String> filtermap = new HashMap<String, String>();
        filtermap.put("arrayid", arrayid);
        filtermap.put("zone", zonename);
        int frow = arrayzoneDS.findRow(filtermap);
        if (frow == -1) {
            throw new SapphireException("Zone does not exist in array.");
        }
        String arrayzoneid = arrayzoneDS.getValue(frow, "arrayzoneid");
        DataSet arrayzoneitem = arrayzoneData.getDataset("arrayitemarrayzone");
        filtermap.clear();
        filtermap.put("arrayzoneid", arrayzoneid);
        DataSet arrayzoneitemFilteredDataSet = arrayzoneitem.getFilteredDataSet(filtermap);
        if (arrayzoneitemFilteredDataSet.isEmpty()) {
            throw new SapphireException("Zone contains no cell");
        }
        if (loadingDirection.contains("Horizontal")) {
            int startrow = -1;
            for (Object data : arrayzoneitemFilteredDataSet) {
                Map datamap = (Map)data;
                String arrayitemid = (String)datamap.get("arrayitemid");
                String[] arrayitemidsplit = StringUtil.split(arrayitemid, "_");
                int hrow = Integer.valueOf(arrayitemidsplit[1]);
                if (startrow != -1 && startrow <= hrow) continue;
                startrow = hrow;
            }
            int starcolumn = -1;
            for (Object data : arrayzoneitemFilteredDataSet) {
                Map datamap = (Map)data;
                String arrayitemid = (String)datamap.get("arrayitemid");
                String[] arrayitemidsplit = StringUtil.split(arrayitemid, "_");
                int hrow = Integer.parseInt(arrayitemidsplit[1]);
                int hcol = Integer.parseInt(arrayitemidsplit[2]);
                if (hrow != startrow || starcolumn != -1 && starcolumn <= hcol) continue;
                starcolumn = hcol;
            }
            return startrow + ";" + starcolumn;
        }
        int startcolumn = -1;
        for (Object data : arrayzoneitemFilteredDataSet) {
            Map datamap = (Map)data;
            String arrayitemid = (String)datamap.get("arrayitemid");
            String[] arrayitemidsplit = StringUtil.split(arrayitemid, "_");
            int vcol = Integer.valueOf(arrayitemidsplit[2]);
            if (startcolumn != -1 && startcolumn <= vcol) continue;
            startcolumn = vcol;
        }
        int startrow = -1;
        for (Object data : arrayzoneitemFilteredDataSet) {
            Map datamap = (Map)data;
            String arrayitemid = (String)datamap.get("arrayitemid");
            String[] arrayitemidsplit = StringUtil.split(arrayitemid, "_");
            int vrow = Integer.valueOf(arrayitemidsplit[1]);
            int vcol = Integer.valueOf(arrayitemidsplit[2]);
            if (vcol != startcolumn || startrow != -1 && startrow <= vrow) continue;
            startrow = vrow;
        }
        return startrow + ";" + startcolumn;
    }

    private SDIData fetchArrayZoneData(String arrayid) throws SapphireException {
        String sql = "select arrayzoneid from arrayzone where arrayid = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet arrayzoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (arrayzoneDS != null) {
            String arrayzoneids = arrayzoneDS.getColumnValues("arrayzoneid", ";");
            String rsetid_arrayzone = this.getDAMProcessor().createRSet("LV_ArrayZone", arrayzoneids, null, null);
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_ArrayZone");
            sdiRequest.setRsetid(rsetid_arrayzone);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("arrayzonecontent");
            sdiRequest.setRequestItem("arrayitemarrayzone");
            SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
            return sdiData;
        }
        throw new SapphireException("Failed to fetch Array zone data.");
    }

    private SDIData fetchArrayItemData(String arrayid) throws SapphireException {
        String sql = "select arrayitemid from arrayitem where arrayid = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet arrayitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String arrayitemids = arrayitemDS.getColumnValues("arrayitemid", ";");
        String rsetid_arrayitem = this.getDAMProcessor().createRSet("LV_ArrayItem", arrayitemids, null, null);
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayItem");
        sdiRequest.setRsetid(rsetid_arrayitem);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("arrayitemcontent");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        return sdiData;
    }

    public static DataSet fetchArrayMethodContent(SDIProcessor sdiProcessor, String arraymethodid, String arraymethodversionid) throws SapphireException {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayMethod");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("arraymethodparamitem");
        sdiRequest.setRequestItem("arraymethodcontent");
        sdiRequest.setKeyid1List(arraymethodid);
        sdiRequest.setKeyid2List(arraymethodversionid);
        sdiRequest.setExtendedDataTypes(true);
        SDIData arrayMethod = sdiProcessor.getSDIData(sdiRequest);
        return arrayMethod.getDataset("arraymethodcontent");
    }

    private String getDelimitedString(Collection data, String delimiter, String wrapper) {
        String delimitedstr = "";
        for (Object value : data) {
            delimitedstr = delimitedstr + delimiter + wrapper + value + wrapper;
        }
        return delimitedstr.substring(1);
    }

    private int determineAdditionalArrayCount(String arrayid, String contentkeyid1, String zone, String arraylayoutid, String arraylayoutversionid, int zonesize) throws SapphireException {
        if (contentkeyid1.length() > 0) {
            String[] keys = StringUtil.split(contentkeyid1, ";");
            DataSet zoneloadingdetails = this.getZoneLoadingDetails(zone, arraylayoutid, arraylayoutversionid);
            String dilutiondirection = zoneloadingdetails.getString(0, "dilutiondirection", "");
            int wellcountperitem = 0;
            wellcountperitem = "".equals(dilutiondirection) ? zoneloadingdetails.getBigDecimal(0, "repeatcount", new BigDecimal(1)).intValue() : zoneloadingdetails.getBigDecimal(0, "repeatcount", new BigDecimal(1)).intValue() * zoneloadingdetails.getBigDecimal(0, "dilutionsteps").intValue();
            double contentitemsperzone = Math.floor(zonesize / wellcountperitem);
            double numofarrays = Math.ceil((double)keys.length / contentitemsperzone);
            if (arrayid.length() == 0) {
                return Double.valueOf(numofarrays).intValue();
            }
            return Double.valueOf(numofarrays - 1.0).intValue();
        }
        throw new SapphireException("No content specified to load.");
    }

    private int determineContentItemsPerArray(String zone, String arraylayoutid, String arraylayoutversionid, int zonesize) throws SapphireException {
        DataSet zoneloadingdetails = this.getZoneLoadingDetails(zone, arraylayoutid, arraylayoutversionid);
        String dilutiondirection = zoneloadingdetails.getString(0, "dilutiondirection", "");
        int wellcountperitem = 0;
        wellcountperitem = "".equals(dilutiondirection) ? zoneloadingdetails.getBigDecimal(0, "repeatcount", new BigDecimal(1)).intValue() : zoneloadingdetails.getBigDecimal(0, "repeatcount", new BigDecimal(1)).intValue() * zoneloadingdetails.getBigDecimal(0, "dilutionsteps").intValue();
        return Double.valueOf(Math.floor(zonesize / wellcountperitem)).intValue();
    }

    private DataSet getLayoutDetails(String arrayid, String arraymethodid, String arraymethodversionid) {
        if (arrayid.length() > 0) {
            String sql = "SELECT arraylayoutid, arraylayoutversionid FROM array WHERE arrayid = ?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(arrayid);
            return this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        String sql = "SELECT arraylayoutid, arraylayoutversionid FROM arraymethod WHERE arraymethodid = ? AND arraymethodversionid=? ";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodversionid);
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    private DataSet getZoneLoadingDetails(String zone, String arraylayoutid, String arraylayoutversionid) {
        String sql = "SELECT arraylayoutzone, dilutiondirection, dilutefirstflag, repeatcount, dilutionsteps FROM arraylayoutzone WHERE arraylayoutzone=? AND arraylayoutid=? AND arraylayoutversionid=? ";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(zone);
        safeSQL.addVar(arraylayoutid);
        safeSQL.addVar(arraylayoutversionid);
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    public static boolean checkPropagateContent(String contentitem, String sourceitemcontent, DataSet arraymethodcontent) throws SapphireException {
        return LoadArray.checkPropagateContent(contentitem, sourceitemcontent, arraymethodcontent, "");
    }

    public static boolean checkPropagateContent(String contentitem, String sourceitemcontent, DataSet arraymethodcontent, String targetZone) throws SapphireException {
        DataSet matchrow;
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("contentitem", contentitem);
        if (targetZone != null && targetZone.length() > 0) {
            filter.put("zone", targetZone);
        }
        if ((matchrow = arraymethodcontent.getFilteredDataSet(filter)) == null || matchrow.getRowCount() == 0) {
            throw new SapphireException("No content of type " + contentitem + " defined on arraymethodid:" + arraymethodcontent.getString(0, "arraymethodid"));
        }
        if (matchrow.getRowCount() > 1) {
            throw new SapphireException("Found more than one matching content details for " + contentitem + " and zone " + targetZone + " in arraymethod: " + matchrow.getString(0, "arraymethodid"));
        }
        String contenttransferrule = matchrow.getString(0, "contenttransferrule", "");
        if (contenttransferrule.length() > 0) {
            PropertyList rules = new PropertyList();
            rules.setPropertyList(contenttransferrule);
            if (sourceitemcontent.equals("Unknown")) {
                Trace.logDebug("Check propagatecontent flag for Unknown is: " + rules.getProperty("propagateunknown"));
                return "Y".equals(rules.getProperty("propagateunknown"));
            }
            if (sourceitemcontent.equals("Control")) {
                Trace.logDebug("Check propagatecontent flag for Control is: " + rules.getProperty("propagatecontrol"));
                return "Y".equals(rules.getProperty("propagatecontrol"));
            }
            if (sourceitemcontent.equals("Transfer")) {
                Trace.log("Check propagatecontent flag for Transfer is: " + rules.getProperty("propagatetransfer"));
                return "Y".equals(rules.getProperty("propagatetransfer"));
            }
            if (sourceitemcontent.equals("Treatment")) {
                Trace.log("Check propagatecontent flag for Treatment is: " + rules.getProperty("propagatetreatment"));
                return "Y".equals(rules.getProperty("propagatetreatment"));
            }
            if (sourceitemcontent.equals("Operation")) {
                Trace.log("Check propagatecontent flag for Operation is: " + rules.getProperty("propagateoperation"));
                return "Y".equals(rules.getProperty("propagateoperation"));
            }
            if (sourceitemcontent.equals("MasterMix")) {
                Trace.log("Check propagatecontent flag for Mastermix is: " + rules.getProperty("propagatemastermix"));
                return "Y".equals(rules.getProperty("propagatemastermix"));
            }
        }
        return true;
    }
}

