/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.array.EvaluateArrayLayoutRules;
import com.labvantage.sapphire.array.WellValues;
import com.labvantage.sapphire.array.util.ArrayVolumeHandlingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

public class LoadArrayByPosition
extends BaseAction
implements sapphire.action.LoadArrayByPosition {
    private final Map<String, DataSet> arrayItemMap = new HashMap<String, DataSet>();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String volumeunits;
        String volume;
        String arrayid = properties.getProperty("arrayid", "");
        boolean validateQty = "Y".equals(properties.getProperty("validatesourcevolume", "N"));
        this.validateInputs(properties);
        String methodinfo = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayid);
        if (methodinfo == null || methodinfo.length() == 0) {
            throw new SapphireException("Failed to determine the array method instance to load for arrayid:" + arrayid);
        }
        String[] toks = StringUtil.split(methodinfo, "|");
        String arrayMethodId = toks[0];
        String currentItemStatus = toks[2];
        String promoteresultsflag = toks[3];
        String arrayMethodVersionId = toks[4];
        DataSet arraymethodcontent = LoadArrayByPosition.fetchArrayMethodContent(this.getSDIProcessor(), arrayMethodId, arrayMethodVersionId);
        this.validateStatus(arrayid, arraymethodcontent, currentItemStatus, promoteresultsflag);
        String content = properties.getProperty("content", "");
        String zone = properties.getProperty("zone", properties.getProperty("zonename", ""));
        String sql = "select arraymethod.sampletypeid, arraymethod.createchildsampleflag, arraymethodcontent.zone, arraymethodcontent.contentitem, arraymethodcontent.volumetarget, arraymethodcontent.volumetargetunits, arraymethodcontent.concentrationtarget, arraymethodcontent.concentrationtargetunits, arraymethodcontent.amounttarget, arraymethodcontent.amounttargetunits from arraymethod, arraymethodcontent where arraymethodcontent.arraymethodid = arraymethod.arraymethodid and arraymethodcontent.arraymethodversionid = arraymethod.arraymethodversionid and arraymethod.arraymethodid = ? and arraymethod.arraymethodversionid = ? and arraymethodcontent.zone = ? and arraymethodcontent.contentitem = ?";
        DataSet arraymethoddefaults = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        if (content.contains(";") || zone.contains(";")) {
            String[] zoneArray;
            String[] contentArray = StringUtil.split(content, ";");
            if (contentArray.length != (zoneArray = StringUtil.split(zone, ";")).length) {
                throw new SapphireException(this.getTranslationProcessor().translate("Number of zone and content properties must be same"));
            }
            HashSet<String> set = new HashSet<String>();
            for (int i = 0; i < contentArray.length; ++i) {
                String contentid = contentArray[i];
                String zoneid = zoneArray[i];
                String key = contentid + "_" + zoneid;
                if (!set.add(key)) continue;
                safeSQL.reset();
                safeSQL.addVar(arrayMethodId);
                safeSQL.addVar(arrayMethodVersionId);
                safeSQL.addVar(zoneid);
                safeSQL.addVar(contentid);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds == null) continue;
                for (int row = 0; row < ds.size(); ++row) {
                    arraymethoddefaults.copyRow(ds, row, 1);
                }
            }
        } else {
            safeSQL.addVar(arrayMethodId);
            safeSQL.addVar(arrayMethodVersionId);
            safeSQL.addVar(zone);
            safeSQL.addVar(content);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null) {
                for (int row = 0; row < ds.size(); ++row) {
                    arraymethoddefaults.copyRow(ds, row, 1);
                }
            }
        }
        if ((volume = properties.getProperty("volume")) != null && volume.length() > 0) {
            volumeunits = properties.getProperty("volumeunits");
            if (volumeunits == null || volumeunits.length() == 0) {
                throw new SapphireException("Volumeunits need to be specified when volume is specified");
            }
        } else if (arraymethoddefaults != null && arraymethoddefaults.size() == 1) {
            volume = arraymethoddefaults.getValue(0, "volumetarget");
            if (volume == null || volume.length() == 0) {
                volume = "0";
            }
            properties.setProperty("volume", volume);
            volumeunits = arraymethoddefaults.getValue(0, "volumetargetunits");
            if (volumeunits == null || volumeunits.length() == 0) {
                throw new SapphireException("Volumeunits not defined in arraymethod");
            }
            properties.setProperty("volumeunits", volumeunits);
        } else {
            throw new SapphireException("Cannot match contents for in arraymethod:" + arrayMethodId);
        }
        this.fillItemContents(properties, arraymethodcontent, arraymethoddefaults, validateQty);
        this.updateArrayArrayMethodItemStatus(arrayid, "Loaded");
    }

    private void validateInputs(PropertyList properties) throws SapphireException {
        String zone = properties.getProperty("zone", properties.getProperty("zonename", ""));
        String arrayid = properties.getProperty("arrayid", "");
        if (zone.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Zone is mandatory for loading an array"));
        }
        if (arrayid.length() == 0 || arrayid.indexOf(";") > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Either arrayid or arraymethodid has to be specified to load an array"));
        }
        String rowlabelsold = properties.getProperty("rowlabel", "");
        String columnlabelsold = properties.getProperty("columnlabel", "");
        String arrayitemlabels = properties.getProperty("arrayitemlabel", "");
        String arrayitemids = properties.getProperty("arrayitemid", "");
        if (arrayitemlabels.length() == 0 && arrayitemids.length() == 0 && (rowlabelsold.length() == 0 || columnlabelsold.length() == 0) && arrayitemlabels.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Specify arrayitemids/arrayitemlabel to identify positions in the array"));
        }
        String contentitem = properties.getProperty("content", "");
        String contentsdcid = properties.getProperty("contentsdcid", "");
        String contentkeyid1 = properties.getProperty("contentkeyid1", "");
        if (contentitem.length() == 0 || contentsdcid.length() == 0 || contentkeyid1.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("content, contentsdcid and contentkeyid1 have to be specified to load an array"));
        }
        if (contentitem.equals("MasterMix") || contentitem.equals("Operation")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid content:" + contentitem + " specified for item level loading."));
        }
    }

    private void validateStatus(String arrayid, DataSet arraymethodcontent, String currentItemStatus, String promoteresultsflag) throws SapphireException {
        String arrayStatus = ArrayUtil.getArrayStatus(this.getQueryProcessor(), arrayid);
        if (!ArrayUtil.validateStatus(arrayStatus, currentItemStatus, "LoadArray", promoteresultsflag)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid AMI Status: " + currentItemStatus + " Cannot load array:") + arrayid);
        }
    }

    private void updateArrayArrayMethodItemStatus(String arrayid, String status) throws SapphireException {
        String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus FROM arrayarraymethoditem WHERE arrayid=? order by usersequence desc";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        if (ds != null && ds.getRowCount() > 0 && !ds.getString(0, "arraymethoditemstatus").equals(status)) {
            PropertyList arrayarraymethoditemprops = new PropertyList();
            arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
            arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
            arrayarraymethoditemprops.setProperty("arrayid", arrayid);
            arrayarraymethoditemprops.setProperty("arraymethodid", ds.getString(0, "arraymethodid"));
            arrayarraymethoditemprops.setProperty("arraymethodversionid", ds.getString(0, "arraymethodversionid"));
            arrayarraymethoditemprops.setProperty("arraymethodinstance", "" + ds.getInt(0, "arraymethodinstance"));
            arrayarraymethoditemprops.setProperty("arraymethoditemstatus", status);
            this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
        }
    }

    private DataSet parseInputProperties(PropertyList properties) throws SapphireException {
        String contentitem = properties.getProperty("content");
        String contentkeyid1s = properties.getProperty("contentkeyid1");
        String contentkeyid2s = properties.getProperty("contentkeyid2", "");
        String contentkeyid3s = properties.getProperty("contentkeyid3", "");
        String targetvolume = properties.getProperty("volume", "");
        String targetvolumeunits = properties.getProperty("volumeunits");
        String sourcevolume = properties.getProperty("sourcevolume", "");
        String sourcevolumeunits = properties.getProperty("sourcevolumeunit");
        String targetconcentration = properties.getProperty("concentration", "");
        String targetconcentrationunits = properties.getProperty("concentrationunits");
        String contentSDCID = properties.getProperty("contentsdcid");
        String rowLabelsDeprecated = properties.getProperty("rowlabel", "");
        String colLabelsDeprecated = properties.getProperty("columnlabel", "");
        String arrayItemLabels = properties.getProperty("arrayitemlabel", "");
        String arrayItemIds = properties.getProperty("arrayitemid", "");
        String zonenames = properties.getProperty("zone", properties.getProperty("zonename"));
        int itemcount = StringUtil.split(contentkeyid1s, ";").length;
        if (arrayItemLabels.length() > 0) {
            if (itemcount != StringUtil.split(arrayItemLabels, ";").length) {
                throw new SapphireException("\"Number of arrayitemlabels specified does not match the number of content items");
            }
        } else if (arrayItemIds.length() > 0) {
            if (itemcount != StringUtil.split(arrayItemIds, ";").length) {
                throw new SapphireException("\"Number of arrayitemids specified does not match the number of content items");
            }
        } else if (itemcount != StringUtil.split(rowLabelsDeprecated, ";").length || itemcount != StringUtil.split(colLabelsDeprecated, ";").length) {
            throw new SapphireException("Number of positions specified does not match the number of content items");
        }
        DataSet content = new DataSet();
        content.addColumnValues("contentitem", 0, contentitem, ";");
        content.addColumnValues("contentsdcid", 0, contentSDCID, ";");
        content.addColumnValues("contentkeyid1", 0, contentkeyid1s, ";");
        content.addColumnValues("contentkeyid2", 0, contentkeyid2s, ";");
        content.addColumnValues("contentkeyid3", 0, contentkeyid3s, ";");
        content.addColumnValues("rowlabel", 0, rowLabelsDeprecated, ";");
        content.addColumnValues("columnlabel", 0, colLabelsDeprecated, ";");
        content.addColumnValues("arrayitemlabel", 0, arrayItemLabels, ";");
        content.addColumnValues("arrayitemid", 0, arrayItemIds, ";");
        content.addColumnValues("zonename", 0, zonenames, ";");
        content.addColumnValues("targetvolume", 0, targetvolume, ";");
        content.addColumnValues("targetvolumeunits", 0, targetvolumeunits, ";");
        content.addColumnValues("sourcevolume", 0, sourcevolume, ";");
        content.addColumnValues("sourcevolumeunit", 0, sourcevolumeunits, ";");
        content.addColumnValues("targetconcentration", 0, targetconcentration, ";");
        content.addColumnValues("targetconcentrationunits", 0, targetconcentrationunits, ";");
        content.padColumn("contentitem");
        content.padColumn("contentsdcid");
        content.padColumn("targetvolume");
        content.padColumn("targetvolumeunits");
        content.padColumn("targetconcentration");
        content.padColumn("targetconcentrationunits");
        content.padColumn("sourcevolume");
        content.padColumn("sourcevolumeunit");
        content.padColumn("zonename");
        content.padColumn("itemsequence");
        String arrayid = properties.getProperty("arrayid");
        content = this.addZoneDetailsToContent(arrayid, content);
        return content;
    }

    private void fillItemContents(PropertyList properties, DataSet arraymethodcontent, DataSet arraymethoddefaults, boolean validateQty) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        DataSet content = this.parseInputProperties(properties);
        ArrayList<DataSet> contentByZone = content.getGroupedDataSets("zonename");
        for (int zonenum = 0; zonenum < contentByZone.size(); ++zonenum) {
            this.processByZone(properties, arrayid, contentByZone.get(zonenum), arraymethodcontent, arraymethoddefaults, validateQty);
        }
    }

    private DataSet addZoneDetailsToContent(String arrayid, DataSet content) throws SapphireException {
        SDIData zoneData = this.fetchArrayZoneData(arrayid);
        DataSet arrayZones = zoneData.getDataset("primary");
        for (int i = 0; i < content.getRowCount(); ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("zone", content.getString(i, "zonename"));
            DataSet match = arrayZones.getFilteredDataSet(filter);
            if (match.getRowCount() <= 0) {
                throw new SapphireException("Zone specified does not exist in the array");
            }
            content.setString(i, "arrayzoneid", match.getString(0, "arrayzoneid"));
            content.setString(i, "adhocmodeflag", match.getString(0, "adhocmodeflag", "N"));
            content.setNumber(i, "repeatnumber", match.getInt(0, "repeatnumber", 1));
            content.setNumber(i, "dilutionsteps", match.getInt(0, "dilutionsteps", 1));
            content.setNumber(i, "dilutionfactor", match.getDouble(0, "dilutionfactor", 1.0));
            content.setString(i, "dilutefirstflag", match.getString(0, "dilutefirstflag", ""));
            content.setString(i, "horizontalpriority", match.getString(0, "horizontalpriority", ""));
            content.setString(i, "verticalpriority", match.getString(0, "verticalpriority", ""));
            content.setString(i, "contentdirection", match.getString(0, "contentdirection", ""));
            DataSet arrayiteminfo = this.getArrayItemInfo(arrayid, content.getString(i, "rowlabel"), content.getString(i, "columnlabel"), content.getString(i, "arrayitemlabel"), content.getString(i, "arrayitemid"));
            content.setString(i, "arrayitemid", arrayiteminfo.getString(0, "arrayitemid"));
            content.setNumber(i, "xpos", arrayiteminfo.getInt(0, "xpos"));
            content.setNumber(i, "ypos", arrayiteminfo.getInt(0, "ypos"));
        }
        return content;
    }

    private WellValues[][] loadWellsByZone(PropertyList inputPropertyList, String arrayid, DataSet inputContentByZone) throws SapphireException {
        boolean adhocLoad = "Y".equals(inputContentByZone.getString(0, "adhocmodeflag"));
        String arrayzoneid = inputContentByZone.getString(0, "arrayzoneid");
        String contentdirection = inputContentByZone.getString(0, "contentdirection", "H");
        String loadingDirection = "H".equalsIgnoreCase(contentdirection) ? "Horizontal" : "Vertical";
        String availableCells = "";
        EvaluateArrayLayoutRules evalRules = new EvaluateArrayLayoutRules();
        String sql = "SELECT arraytypeid, arraytypeversionid FROM array WHERE arrayid = ?";
        DataSet arrayTypeInfo = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        String arraytypeid = arrayTypeInfo.getValue(0, "arraytypeid");
        String versionid = arrayTypeInfo.getValue(0, "arraytypeversionid");
        SafeSQL safeSQL1 = new SafeSQL();
        safeSQL1.addVar(arraytypeid);
        safeSQL1.addVar(versionid);
        DataSet arraytypeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * from arraytype where arraytypeid=? and arraytypeversionid=? ", safeSQL1.getValues());
        int rows = arraytypeDS.getInt(0, "numrows");
        int cols = arraytypeDS.getInt(0, "numcolumns");
        HashMap<String, String> coorContentMap = new HashMap<String, String>();
        if (!adhocLoad) {
            DataSet arrayitemarrayzone = this.fetchArrayZoneData(arrayid).getDataset("arrayitemarrayzone");
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("arrayzoneid", arrayzoneid);
            DataSet currentZoneItems = arrayitemarrayzone.getFilteredDataSet(filtermap);
            for (int i = 0; i < currentZoneItems.getRowCount(); ++i) {
                String arrayitemid = currentZoneItems.getString(i, "arrayitemid");
                String[] split = StringUtil.split(arrayitemid, "_");
                String xpos = split[1];
                String ypos = split[2];
                int rowIndex = (int)Float.parseFloat(xpos);
                int colIndex = (int)Float.parseFloat(ypos);
                availableCells = availableCells + ";" + rowIndex + "," + colIndex;
                String contentstring = currentZoneItems.getValue(i, "contentstring");
                if (contentstring == null || contentstring.length() <= 0) continue;
                String key = xpos + "," + ypos;
                coorContentMap.put(key, contentstring);
            }
        } else {
            this.addArrayItemArrayZone(inputPropertyList, arrayid, arrayzoneid, inputContentByZone);
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    availableCells = availableCells + ";" + i + "," + j;
                }
            }
        }
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
                String currcell = arrayid + "_" + i + "_" + j;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("arrayitemid", currcell);
                DataSet match = inputContentByZone.getFilteredDataSet(filter);
                if (match.getRowCount() > 0) {
                    wellValue.sampleid = match.getString(0, "contentkeyid1");
                    wellValue.sample = !adhocLoad ? -1 : 1;
                } else if (!adhocLoad) {
                    wellValue.sampleid = "-1";
                    wellValue.sample = -1;
                } else {
                    wellValue.sample = 1;
                }
                wellValue.repeat = 1;
                wellValue.treatment = 0;
                wellValue.dilutionfactor = 0.0f;
                wellValue.dilution = 0;
                layoutMap[i][j] = wellValue;
            }
        }
        if (!adhocLoad) {
            ArrayList<String> coordinates = new ArrayList<String>();
            for (int i = 0; i < inputContentByZone.getRowCount(); ++i) {
                String val = inputContentByZone.getInt(i, "xpos") + "," + inputContentByZone.getInt(i, "ypos");
                coordinates.add(i, val);
            }
            return evalRules.applyMultipleLoadingDirection(inputContentByZone.getColumnValues("contentkeyid1", ";"), coordinates, availableCells.substring(1), loadingDirection, layoutMap);
        }
        return layoutMap;
    }

    private void processByZone(PropertyList inputPropertyList, String arrayid, DataSet inputContentForZone, DataSet arraymethodcontent, DataSet arraymethoddefaults, boolean validateQty) throws SapphireException {
        int i;
        String defaultForCreateChild = arraymethoddefaults.getString(0, "createchildsampleflag", "N");
        String defaultForChildSampleType = arraymethoddefaults.getString(0, "sampletypeid", "");
        String createChild = inputPropertyList.getProperty("createchild", defaultForCreateChild);
        String childSampleType = inputPropertyList.getProperty("childsampletype", defaultForChildSampleType);
        String zonename = inputPropertyList.getProperty("zone", inputPropertyList.getProperty("zonename", ""));
        WellValues[][] loadedWellValues = this.loadWellsByZone(inputPropertyList, arrayid, inputContentForZone);
        String contentdirection = inputContentForZone.getString(0, "contentdirection", "H");
        String loadingDirection = "H".equalsIgnoreCase(contentdirection) ? "Horizontal" : "Vertical";
        String sql = "SELECT arraytypeid, arraytypeversionid FROM array WHERE arrayid = ?";
        DataSet arrayTypeInfo = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        String arraytypeid = arrayTypeInfo.getValue(0, "arraytypeid");
        String versionid = arrayTypeInfo.getValue(0, "arraytypeversionid");
        SafeSQL safeSQL1 = new SafeSQL();
        safeSQL1.addVar(arraytypeid);
        safeSQL1.addVar(versionid);
        DataSet arraytypeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * from arraytype where arraytypeid=? and arraytypeversionid=? ", safeSQL1.getValues());
        int rowCount = arraytypeDS.getInt(0, "numrows");
        int colCount = arraytypeDS.getInt(0, "numcolumns");
        DataSet arrayItemContentDS = new DataSet();
        arrayItemContentDS.addColumn("arrayitemid", 0);
        arrayItemContentDS.addColumn("arrayzoneid", 0);
        arrayItemContentDS.addColumn("zone", 0);
        arrayItemContentDS.addColumn("contentsdcid", 0);
        arrayItemContentDS.addColumn("contentkeyid1", 0);
        arrayItemContentDS.addColumn("volume", 1);
        arrayItemContentDS.addColumn("volumeunits", 0);
        arrayItemContentDS.addColumn("concentration", 1);
        arrayItemContentDS.addColumn("concentrationunits", 0);
        arrayItemContentDS.addColumn("contenttype", 0);
        arrayItemContentDS.addColumn("contentitem", 0);
        arrayItemContentDS.addColumn("parentarrayitemid", 0);
        arrayItemContentDS.addColumn("dilutionfactor", 1);
        arrayItemContentDS.addColumn("diluentvolume", 1);
        arrayItemContentDS.addColumn("diluentvolumeunits", 0);
        boolean derivate = false;
        if ("Y".equals(createChild) && childSampleType.length() > 0) {
            derivate = true;
        }
        DataSet sourceVolumeDecrDS = new DataSet();
        sourceVolumeDecrDS.addColumn("contentsdcid", 0);
        sourceVolumeDecrDS.addColumn("contentkeyid1", 0);
        sourceVolumeDecrDS.addColumn("targetvolume", 1);
        sourceVolumeDecrDS.addColumn("volumeunits", 0);
        sourceVolumeDecrDS.addColumn("volumehandlingflag", 0);
        sourceVolumeDecrDS.addColumn("targetconcentration", 1);
        sourceVolumeDecrDS.addColumn("sourceconcentration", 1);
        sourceVolumeDecrDS.addColumn("sourceconcentrationunit", 0);
        sourceVolumeDecrDS.addColumn("targetconcentrationunit", 0);
        sourceVolumeDecrDS.addColumn("repeatcount", 1);
        sourceVolumeDecrDS.addColumn("dilutionfactor", 1);
        sourceVolumeDecrDS.addColumn("dilutefirstflag", 0);
        sourceVolumeDecrDS.addColumn("sourcevolume", 1);
        sourceVolumeDecrDS.addColumn("sourcevolumeunit", 0);
        String reagenttypeid = "";
        for (int h = 0; h < loadedWellValues.length; ++h) {
            for (int v = 0; v < loadedWellValues[0].length; ++v) {
                DataSet sourceConcDS;
                if (loadedWellValues[h][v] == null || loadedWellValues[h][v].sampleid == null || loadedWellValues[h][v].sampleid.length() <= 0) continue;
                HashMap<String, String> match = new HashMap<String, String>();
                match.put("contentkeyid1", loadedWellValues[h][v].sampleid);
                DataSet matchInputContentDetails = inputContentForZone.getFilteredDataSet(match);
                if (matchInputContentDetails == null || matchInputContentDetails.getRowCount() == 0) {
                    throw new SapphireException("Failed to find content details");
                }
                int pos = 0;
                if (matchInputContentDetails.getRowCount() > 1) {
                    for (int i2 = 0; i2 < matchInputContentDetails.getRowCount(); ++i2) {
                        int currxpos = matchInputContentDetails.getInt(i2, "xpos");
                        int currypos = matchInputContentDetails.getInt(i2, "ypos");
                        if (h != currxpos || v != currypos) continue;
                        pos = i2;
                        break;
                    }
                }
                String contentitem = matchInputContentDetails.getString(pos, "contentitem");
                String contentsdcid = matchInputContentDetails.getString(pos, "contentsdcid");
                String contentkeyid1 = matchInputContentDetails.getString(pos, "contentkeyid1");
                String arrayitemid = matchInputContentDetails.getString(pos, "arrayitemid");
                String arrayzoneid = matchInputContentDetails.getString(pos, "arrayzoneid");
                String targetvolume = matchInputContentDetails.getString(pos, "targetvolume", "");
                String targetvolumeunits = matchInputContentDetails.getString(pos, "targetvolumeunits");
                String sourcevolume = matchInputContentDetails.getString(pos, "sourcevolume");
                String sourcevolumeunits = matchInputContentDetails.getString(pos, "sourcevolumeunit");
                String targetconc = matchInputContentDetails.getString(pos, "targetconcentration", arraymethoddefaults.getValue(0, "concentrationtarget", ""));
                String targetconcunits = matchInputContentDetails.getString(pos, "targetconcentrationunits", arraymethoddefaults.getValue(0, "concentrationtargetunits", ""));
                if (contentsdcid.equals("LV_Treatment")) {
                    Trace.logDebug("No volume management for Treatment:");
                    int rowindex = arrayItemContentDS.addRow();
                    String currarrayitemid = arrayid + "_" + h + "_" + v;
                    arrayItemContentDS.setString(rowindex, "arrayitemid", currarrayitemid);
                    arrayItemContentDS.setString(rowindex, "arrayzoneid", arrayzoneid);
                    arrayItemContentDS.setString(rowindex, "zone", inputContentForZone.getValue(0, "zonename"));
                    arrayItemContentDS.setString(rowindex, "contentsdcid", contentsdcid);
                    arrayItemContentDS.setString(rowindex, "contentkeyid1", contentkeyid1);
                    arrayItemContentDS.setString(rowindex, "contentkeyid2", "");
                    arrayItemContentDS.setString(rowindex, "contentkeyid3", "");
                    arrayItemContentDS.setValue(rowindex, "volume", targetvolume);
                    arrayItemContentDS.setString(rowindex, "volumeunits", targetvolumeunits);
                    if (targetconc.length() > 0) {
                        arrayItemContentDS.setValue(rowindex, "concentration", "");
                    }
                    arrayItemContentDS.setString(rowindex, "concentrationunits", targetconcunits);
                    arrayItemContentDS.setString(rowindex, "contenttype", "LV_Treatment");
                    arrayItemContentDS.setString(rowindex, "contentitem", contentitem);
                    arrayItemContentDS.setString(rowindex, "parentarrayitemid", "");
                    String contentlabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, "LV_Treatment", contentkeyid1, reagenttypeid);
                    if (contentlabel == null || contentlabel.length() == 0) {
                        throw new SapphireException("Failed to create treatment label for  " + contentkeyid1);
                    }
                    arrayItemContentDS.setString(rowindex, "contentlabel", contentlabel);
                } else {
                    ArrayVolumeHandlingUtil util = new ArrayVolumeHandlingUtil(new M18NUtil(this.connectionInfo));
                    String sourceConcentration = "";
                    String sourceConcentrationUnits = targetconcunits;
                    if (!contentsdcid.equalsIgnoreCase("TrackItemSDC")) {
                        DataSet sourceConcDetails = util.findSourceConcentration(contentsdcid, contentkeyid1, this.getDAMProcessor(), this.getQueryProcessor());
                        if (sourceConcDetails != null) {
                            sourceConcentration = sourceConcDetails.getValue(0, "concentration", "");
                            sourceConcentrationUnits = sourceConcDetails.getString(0, "concentrationunits", "");
                        }
                    } else {
                        sql = "select reagenttypeid from  reagentlot where reagentlotid = ( select linkkeyid1 FROM trackitem where trackitemid = ? )";
                        SafeSQL safeSQL = new SafeSQL();
                        safeSQL.addVar(contentkeyid1);
                        DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ret != null && ret.getRowCount() > 0) {
                            reagenttypeid = ret.getValue(0, "reagenttypeid");
                        }
                    }
                    if (!(targetconc != null && targetconc.length() != 0 || derivate)) {
                        targetconc = sourceConcentration;
                        targetconcunits = sourceConcentrationUnits;
                    }
                    M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
                    int row = arrayItemContentDS.addRow();
                    Double evaluateConcentration = null;
                    if (targetconc != null && targetconc.length() > 0) {
                        double d = m18NUtil.parseBigDecimal(targetconc).doubleValue();
                        evaluateConcentration = d;
                        if (loadedWellValues[h][v].dilutionfactor > 0.0f) {
                            evaluateConcentration = d / (double)loadedWellValues[h][v].dilutionfactor;
                        }
                    }
                    String currarrayitemid = arrayid + "_" + h + "_" + v;
                    double dsourcevolume = util.findAmountToBeDecremented(loadedWellValues[h][v].repeat, loadedWellValues[h][v].dilutionfactor, "Y", targetconc, targetconcunits, sourceConcentration, sourceConcentrationUnits, m18NUtil.parseBigDecimal(targetvolume).doubleValue(), this.connectionInfo);
                    double diluentVolume = ArrayUtil.getDilutentVolume(this.connectionInfo, m18NUtil, sourceConcentration, sourceConcentrationUnits, dsourcevolume, targetconc, targetconcunits, m18NUtil.parseBigDecimal(targetvolume).doubleValue());
                    if (diluentVolume >= 0.0) {
                        arrayItemContentDS.setNumber(row, "diluentvolume", diluentVolume);
                        arrayItemContentDS.setString(row, "diluentvolumeunits", targetvolumeunits);
                    }
                    arrayItemContentDS.setString(row, "arrayitemid", currarrayitemid);
                    arrayItemContentDS.setString(row, "arrayzoneid", arrayzoneid);
                    arrayItemContentDS.setString(row, "zone", inputContentForZone.getValue(0, "zonename"));
                    arrayItemContentDS.setString(row, "contentsdcid", contentsdcid);
                    arrayItemContentDS.setString(row, "contentkeyid1", contentkeyid1);
                    arrayItemContentDS.setString(row, "contentkeyid2", "");
                    arrayItemContentDS.setString(row, "contentkeyid3", "");
                    arrayItemContentDS.setValue(row, "volume", targetvolume);
                    arrayItemContentDS.setString(row, "volumeunits", targetvolumeunits);
                    if (evaluateConcentration != null) {
                        arrayItemContentDS.setValue(row, "concentration", "" + evaluateConcentration);
                    }
                    arrayItemContentDS.setString(row, "concentrationunits", targetconcunits);
                    arrayItemContentDS.setString(row, "contenttype", contentsdcid.equalsIgnoreCase("TrackItemSDC") ? "LV_ReagentLot" : contentsdcid);
                    arrayItemContentDS.setString(row, "contentitem", contentitem);
                    arrayItemContentDS.setString(row, "parentarrayitemid", "");
                    if (loadedWellValues[h][v].dilutionfactor > 0.0f) {
                        arrayItemContentDS.setNumber(row, "dilutionfactor", loadedWellValues[h][v].dilutionfactor);
                        if (arrayItemContentDS.getDouble(row, "dilutionfactor") != (double)loadedWellValues[h][v].dilutionfactor) {
                            throw new SapphireException("Failed to set dilution factor");
                        }
                    }
                    if (loadedWellValues[h][v].repeat > 0) {
                        arrayItemContentDS.setNumber(row, "repeatnum", loadedWellValues[h][v].repeat);
                        if (arrayItemContentDS.getInt(row, "repeatnum") != loadedWellValues[h][v].repeat) {
                            throw new SapphireException("Failed to set repeatnum");
                        }
                    }
                    String contentlabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, contentsdcid.equalsIgnoreCase("TrackItemSDC") ? "LV_ReagentLot" : contentsdcid, contentkeyid1, reagenttypeid);
                    arrayItemContentDS.setString(row, "contentlabel", contentlabel);
                }
                if (contentsdcid.contains("LV_ArrayItem")) {
                    String currarrayitemid = arrayid + "_" + h + "_" + v;
                    String sourcearrayitemid = loadedWellValues[h][v].sampleid;
                    String sourcecontentsql = "SELECT * FROM arrayitemcontent WHERE arrayitemid = ?";
                    DataSet sourcecontentdetails = this.getQueryProcessor().getPreparedSqlDataSet(sourcecontentsql, new Object[]{sourcearrayitemid});
                    if (sourcecontentdetails != null) {
                        for (int contents = 0; contents < sourcecontentdetails.getRowCount(); ++contents) {
                            String sourceitemcontent = sourcecontentdetails.getString(contents, "contentitem");
                            if (LoadArrayByPosition.checkPropagateContent(contentitem, sourceitemcontent, arraymethodcontent, zonename)) {
                                Trace.log("Check propagatecontent returned true for:" + sourceitemcontent);
                                int row = arrayItemContentDS.addRow();
                                arrayItemContentDS.setString(row, "arrayitemid", currarrayitemid);
                                arrayItemContentDS.setString(row, "arrayzoneid", arrayzoneid);
                                arrayItemContentDS.setString(row, "contentsdcid", sourcecontentdetails.getString(contents, "contentsdcid"));
                                arrayItemContentDS.setString(row, "contentkeyid1", sourcecontentdetails.getString(contents, "contentkeyid1"));
                                arrayItemContentDS.setString(row, "contentkeyid2", sourcecontentdetails.getString(contents, "contentkeyid2"));
                                arrayItemContentDS.setString(row, "contentkeyid3", sourcecontentdetails.getString(contents, "contentkeyid3"));
                                arrayItemContentDS.setValue(row, "volume", sourcecontentdetails.getValue(contents, "volume"));
                                arrayItemContentDS.setString(row, "volumeunits", sourcecontentdetails.getString(contents, "volumeunits"));
                                arrayItemContentDS.setValue(row, "concentration", sourcecontentdetails.getValue(contents, "concentration"));
                                arrayItemContentDS.setString(row, "concentrationunits", sourcecontentdetails.getString(contents, "concentrationunits"));
                                arrayItemContentDS.setString(row, "parentarrayitemid", sourcearrayitemid);
                                arrayItemContentDS.setString(row, "contenttype", sourcecontentdetails.getString(contents, "contenttype"));
                                arrayItemContentDS.setString(row, "contentitem", sourcecontentdetails.getString(contents, "contentitem"));
                                arrayItemContentDS.setString(row, "contentlabel", sourcecontentdetails.getString(contents, "contentlabel"));
                                if (sourcecontentdetails.getValue(contents, "dilutionfactor") != null && sourcecontentdetails.getValue(contents, "dilutionfactor").length() > 0) {
                                    arrayItemContentDS.setNumber(row, "dilutionfactor", sourcecontentdetails.getInt(contents, "dilutionfactor"));
                                }
                                if (sourcecontentdetails.getValue(contents, "repeatnum") == null || sourcecontentdetails.getValue(contents, "repeatnum").length() <= 0) continue;
                                arrayItemContentDS.setNumber(row, "repeatnum", sourcecontentdetails.getInt(contents, "repeatnum"));
                                continue;
                            }
                            Trace.log("Check propagatecontent returned false for:" + sourceitemcontent);
                        }
                    }
                }
                M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
                if (contentsdcid.equals("LV_Treatment")) continue;
                ArrayVolumeHandlingUtil arrayVolumeHandlingUtil = new ArrayVolumeHandlingUtil(m18NUtil);
                if (targetvolume.length() <= 0 || !(m18NUtil.parseBigDecimal(targetvolume).doubleValue() > 0.0)) continue;
                int row = sourceVolumeDecrDS.addRow();
                sourceVolumeDecrDS.setString(row, "contentkeyid1", contentkeyid1);
                if (!contentsdcid.equals("TrackItemSDC") && (sourceConcDS = arrayVolumeHandlingUtil.findSourceConcentration(contentsdcid, contentkeyid1, this.getDAMProcessor(), this.getQueryProcessor())) != null) {
                    if (sourceConcDS.getValue(0, "concentration", "").length() > 0) {
                        Double sourceContentConc = sourceConcDS.getDouble(0, "concentration");
                        sourceVolumeDecrDS.setNumber(row, "sourceconcentration", sourceContentConc);
                    }
                    String sourceContentConcUnits = sourceConcDS.getValue(0, "concentrationunits", "");
                    sourceVolumeDecrDS.setString(row, "sourceconcentrationunit", sourceContentConcUnits);
                }
                sourceVolumeDecrDS.setValue(row, "repeatcount", "1");
                sourceVolumeDecrDS.setValue(row, "dilutionfactor", "1");
                sourceVolumeDecrDS.setString(row, "dilutefirstflag", "");
                sourceVolumeDecrDS.setValue(row, "targetconcentration", targetconc);
                sourceVolumeDecrDS.setString(row, "targetconcentrationunit", targetconcunits);
                sourceVolumeDecrDS.setString(row, "volumeunits", targetvolumeunits);
                sourceVolumeDecrDS.setValue(row, "targetvolume", targetvolume);
                sourceVolumeDecrDS.setString(row, "contentsdcid", contentsdcid);
                sourceVolumeDecrDS.setValue(row, "sourcevolume", sourcevolume);
                sourceVolumeDecrDS.setString(row, "sourcevolumeunit", sourcevolumeunits);
            }
        }
        if (createChild.length() == 0) {
            createChild = arraymethoddefaults.getString(0, "createchildsampleflag", "N");
            if (childSampleType.length() == 0) {
                childSampleType = arraymethoddefaults.getString(0, "sampletypeid", "");
            }
        }
        arrayItemContentDS = ArrayUtil.processChildSamples(this.getConnectionid(), createChild, childSampleType, arrayItemContentDS, loadingDirection, rowCount, colCount);
        for (int i3 = 0; i3 < arrayItemContentDS.getRowCount(); ++i3) {
            String inputVolume = arrayItemContentDS.getValue(i3, "volume", "");
            String inputVolumeUnits = arrayItemContentDS.getValue(i3, "volume", "");
            String inputConc = arrayItemContentDS.getValue(i3, "concentration", "");
            String inputConcUnits = arrayItemContentDS.getValue(i3, "concentrationunits", "");
            String currentContentItem = arrayItemContentDS.getString(i3, "contentitem");
            if (arrayItemContentDS.getString(i3, "parentarrayitemid", "").length() != 0) continue;
            String currentZone = arrayItemContentDS.getString(i3, "zone", "");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("contentitem", currentContentItem);
            filter.put("zone", currentZone);
            DataSet match = arraymethoddefaults.getFilteredDataSet(filter);
            if (match.getRowCount() != 1) {
                throw new SapphireException("Cannot find matching arraymethod contents for one or more items trying to load");
            }
            if (inputVolume.length() == 0) {
                inputVolume = match.getValue(0, "volumetarget");
                inputVolumeUnits = match.getValue(0, "volumetargetunits");
                if (inputVolume.length() == 0) {
                    throw new SapphireException("Volume not specified in input or arraymethod for one or more contents being loaded");
                }
                arrayItemContentDS.setValue(i3, "volume", inputVolume);
                arrayItemContentDS.setValue(i3, "volumeunits", inputVolumeUnits);
            }
            if (inputConc.length() != 0) continue;
            inputConc = match.getValue(0, "concentrationtarget", "");
            inputConcUnits = match.getValue(0, "concentrationtargetunits", "");
            arrayItemContentDS.setValue(i3, "concentration", inputConc);
            arrayItemContentDS.setValue(i3, "concentrationunits", inputConcUnits);
        }
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("arrayid", arrayid);
        propertyList.setProperty("contentsdcid", arrayItemContentDS.getColumnValues("contentsdcid", ";"));
        propertyList.setProperty("contentkeyid1", arrayItemContentDS.getColumnValues("contentkeyid1", ";"));
        propertyList.setProperty("contentkeyid2", arrayItemContentDS.getColumnValues("contentkeyid2", ";"));
        propertyList.setProperty("contentkeyid3", arrayItemContentDS.getColumnValues("contentkeyid3", ";"));
        propertyList.setProperty("volume", arrayItemContentDS.getColumnValues("volume", ";"));
        propertyList.setProperty("volumeunits", arrayItemContentDS.getColumnValues("volumeunits", ";"));
        propertyList.setProperty("concentration", arrayItemContentDS.getColumnValues("concentration", ";"));
        propertyList.setProperty("concentrationunits", arrayItemContentDS.getColumnValues("concentrationunits", ";"));
        propertyList.setProperty("arrayzoneid", arrayItemContentDS.getColumnValues("arrayzoneid", ";"));
        propertyList.setProperty("diluentvolume", arrayItemContentDS.getColumnValues("diluentvolume", ";"));
        propertyList.setProperty("diluentvolumeunits", arrayItemContentDS.getColumnValues("diluentvolumeunits", ";"));
        propertyList.setProperty("contentitem", arrayItemContentDS.getColumnValues("contentitem", ";"));
        propertyList.setProperty("arrayitemid", arrayItemContentDS.getColumnValues("arrayitemid", ";"));
        propertyList.setProperty("parentarrayitemid", arrayItemContentDS.getColumnValues("parentarrayitemid", ";"));
        propertyList.setProperty("dilutionfactor", arrayItemContentDS.getColumnValues("dilutionfactor", ";"));
        propertyList.setProperty("repeatnum", arrayItemContentDS.getColumnValues("repeatnum", ";"));
        propertyList.setProperty("auditsignedflag", inputPropertyList.getProperty("auditsignedflag"));
        propertyList.setProperty("auditactivity", inputPropertyList.getProperty("auditactivity"));
        propertyList.setProperty("auditreason", inputPropertyList.getProperty("auditreason"));
        this.getActionProcessor().processAction("AddArrayContent", "1", propertyList);
        PropertyList arrayItemProps = new PropertyList();
        DataSet arrayItemList = new DataSet();
        for (i = 0; i < arrayItemContentDS.getRowCount(); ++i) {
            if (arrayItemContentDS.getValue(i, "parentarrayitemid", "").length() != 0) continue;
            int row = arrayItemList.addRow();
            arrayItemList.setString(row, "arrayitemid", arrayItemContentDS.getValue(i, "arrayitemid", ""));
            arrayItemList.setString(row, "totalvolume", "+" + arrayItemContentDS.getValue(i, "volume", ""));
            arrayItemList.setString(row, "totalvolumeunits", arrayItemContentDS.getValue(i, "volumeunits", ""));
            arrayItemList.setString(row, "concentration", arrayItemContentDS.getValue(i, "concentration", ""));
            arrayItemList.setString(row, "concentrationunits", arrayItemContentDS.getValue(i, "concentrationunits", ""));
        }
        if (arrayItemList.getRowCount() > 0) {
            arrayItemProps.setProperty("arrayitemid", arrayItemList.getColumnValues("arrayitemid", ";"));
            arrayItemProps.setProperty("totalvol", arrayItemList.getColumnValues("totalvolume", ";"));
            arrayItemProps.setProperty("totalvolunits", arrayItemList.getColumnValues("totalvolumeunits", ";"));
            arrayItemProps.setProperty("totalconc", arrayItemList.getColumnValues("concentration", ";"));
            arrayItemProps.setProperty("totalconcunits", arrayItemList.getColumnValues("concentrationunits", ";"));
            arrayItemProps.setProperty("validatetotalvolume", validateQty ? "Y" : "N");
            arrayItemProps.setProperty("auditsignedflag", inputPropertyList.getProperty("auditsignedflag"));
            arrayItemProps.setProperty("auditactivity", inputPropertyList.getProperty("auditactivity"));
            arrayItemProps.setProperty("auditreason", inputPropertyList.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditArrayItem", "1", arrayItemProps);
        }
        arrayItemList = new DataSet();
        for (i = 0; i < arrayItemContentDS.getRowCount(); ++i) {
            if (arrayItemContentDS.getValue(i, "parentarrayitemid", "").length() <= 0) continue;
            int row = arrayItemList.addRow();
            arrayItemList.setString(row, "arrayitemid", arrayItemContentDS.getValue(i, "arrayitemid", ""));
            arrayItemList.setString(row, "totalvolume", arrayItemContentDS.getValue(i, "volume", ""));
            arrayItemList.setString(row, "totalvolumeunits", arrayItemContentDS.getValue(i, "volumeunits", ""));
            arrayItemList.setString(row, "concentration", arrayItemContentDS.getValue(i, "concentration", ""));
            arrayItemList.setString(row, "concentrationunits", arrayItemContentDS.getValue(i, "concentrationunits", ""));
        }
        ArrayVolumeHandlingUtil arrayVolumeHandlingUtil = new ArrayVolumeHandlingUtil(new M18NUtil());
        if (sourceVolumeDecrDS.getRowCount() > 0) {
            try {
                arrayVolumeHandlingUtil.adjustSourceInv(sourceVolumeDecrDS, this.getActionProcessor(), this.getQueryProcessor(), validateQty, this.connectionInfo);
            }
            catch (SapphireException e) {
                if (e.getMessage().contains("decrement")) {
                    throw new SapphireException("One or more wells cannot be loaded because of insufficient source volumes.");
                }
                throw e;
            }
        }
    }

    private DataSet getArrayItemInfo(String arrayid, String rowlabel, String columnlabel, String arrayitemlabel, String arrayitemid) throws SapphireException {
        int row;
        if (!this.arrayItemMap.containsKey(arrayid)) {
            String sql = "select arrayitemid, xpos, ypos, itemlabel, verticallabel, horizontallabel from arrayitem where arrayid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayid});
            this.arrayItemMap.put(arrayid, ds);
        }
        DataSet dataSet = this.arrayItemMap.get(arrayid);
        if (arrayitemlabel != null && !arrayitemlabel.isEmpty()) {
            row = dataSet.findRow("itemlabel", arrayitemlabel);
        } else if (arrayitemid != null && !arrayitemid.isEmpty()) {
            row = dataSet.findRow("arrayitemid", arrayitemid);
        } else {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("verticallabel", rowlabel);
            filter.put("horizontallabel", columnlabel);
            row = dataSet.findRow(filter);
        }
        DataSet data = new DataSet();
        if (row != -1) {
            data.copyRow(dataSet, row, 1);
        }
        if (data.isEmpty()) {
            throw new SapphireException("The specified position does not exist in the array");
        }
        return data;
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

    private void addArrayItemArrayZone(PropertyList inputPropertyList, String arrayid, String arrayzoneid, DataSet inputContentByZone) throws SapphireException {
        String sql = "SELECT * FROM arrayitemarrayzone WHERE arrayitemid = ? AND arrayzoneid = ?";
        for (int i = 0; i < inputContentByZone.getRowCount(); ++i) {
            DataSet aiaz = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{inputContentByZone.getString(i, "arrayitemid"), arrayzoneid});
            if (aiaz.getRowCount() != 0) continue;
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("sdcid", "LV_ArrayItem");
            propertyList.setProperty("linkid", "Array Item Zone");
            propertyList.setProperty("arrayzoneid", arrayzoneid);
            propertyList.setProperty("arrayitemid", inputContentByZone.getString(i, "arrayitemid"));
            propertyList.setProperty("auditsignedflag", inputPropertyList.getProperty("auditsignedflag"));
            propertyList.setProperty("auditactivity", inputPropertyList.getProperty("auditactivity"));
            propertyList.setProperty("auditreason", inputPropertyList.getProperty("auditreason"));
            this.getActionProcessor().processAction("AddSDIDetail", "1", propertyList);
        }
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

