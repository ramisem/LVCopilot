/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.array.util.ArrayVolumeHandlingUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class IntraArrayTransfer
extends BaseAction
implements ArrayConstants,
sapphire.action.IntraArrayTransfer {
    M18NUtil m18NUtil = null;
    AuditInputs auditInput = null;
    DataSet arrayitems = null;
    DataSet arrayitemcontents = null;
    boolean isAdhocZone;
    private DataSet dimensions = null;
    private String arrayid = "";
    private String arrayzoneid = "";
    private String loadingDirection = "Horizontal";
    private boolean retainshape = false;
    DataSet cumulativeArrayItemContentDS = new DataSet();
    DataSet editTargetArrayItemsList = null;
    DataSet editSourceArrayItemsList = null;
    private DataSet arrayzoneitems = null;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String targetarrayitemids;
        String sourcearrayitemids;
        this.m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        this.arrayid = properties.getProperty("arrayid", "");
        if (this.arrayid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array IO is mandatory"));
        }
        String zone = properties.getProperty("zone", "");
        if (zone.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Zone is mandatory for intra-array transfer"));
        }
        if (zone.contains(";")) {
            zone = StringUtil.split(zone, ";")[0];
            Trace.log("Action supports transfers within a single zone");
            throw new SapphireException(this.getTranslationProcessor().translate("Action supports transfers within a single zone"));
        }
        if (this.arrayid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array ID is not specified"));
        }
        String atv = ArrayUtil.getArrayType(this.getQueryProcessor(), this.arrayid);
        String arrayTypeId = "";
        String arrayTypeVersionId = "1";
        if (atv.length() > 0) {
            String[] toks = StringUtil.split(atv, "|");
            arrayTypeId = toks[0];
            arrayTypeVersionId = toks[1];
        }
        this.dimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), arrayTypeId, arrayTypeVersionId);
        String arrayStatus = ArrayUtil.getArrayStatus(this.getQueryProcessor(), this.arrayid);
        if (!arrayStatus.equals("Loaded")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array [arrayid] should have \"Loaded\" status ", "arrayid=" + this.arrayid));
        }
        this.loadingDirection = properties.getProperty("loadingdirection", "Horizontal");
        this.auditInput = new AuditInputs();
        this.auditInput.auditactivity = properties.getProperty("auditactivity", "");
        this.auditInput.auditactivity = properties.getProperty("auditactivity", "");
        this.auditInput.auditsignedflag = properties.getProperty("auditsignedflag", "");
        String targetvolume = properties.getProperty("targetvolume", "");
        String targetvolumeunits = properties.getProperty("targetvolumeunit", "");
        String targetconcentration = properties.getProperty("targetconcentration", "");
        String targetconcentrationunits = properties.getProperty("targetconcentrationunit", "");
        boolean moveentirecontent = false;
        String moveEntireContentVal = properties.getProperty("moveentirecontent", "N");
        if (moveEntireContentVal.equals("Y")) {
            moveentirecontent = true;
        }
        if (targetvolume.length() == 0) {
            if (!moveentirecontent) {
                throw new SapphireException(this.getTranslationProcessor().translate("Target Volume is mandatory unless Move Entire Content is specified as Y"));
            }
        } else {
            if (moveentirecontent) {
                throw new SapphireException(this.getTranslationProcessor().translate("Target Volume should not be specified if Move Entire Content is specified as Y"));
            }
            if (targetvolumeunits.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Target Volume Units not specified"));
            }
        }
        if ((sourcearrayitemids = properties.getProperty("sourcearrayitemid", "")).length() == 0) {
            String sourcearrayitemlabels = properties.getProperty("sourcearrayitemlabel", "");
            if (sourcearrayitemlabels.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Either Source Array Item ID or Source Array Item Label needs to be specified"));
            }
            sourcearrayitemids = ArrayUtil.getArrayItemIdsByLabels(this.getQueryProcessor(), this.arrayid, sourcearrayitemlabels);
        }
        if ((targetarrayitemids = properties.getProperty("targetarrayitemid", "")).length() == 0) {
            String targetlabels = properties.getProperty("targetarrayitemlabel", "");
            if (targetlabels.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Either Target Array Item ID or Target Array Item Label needs to be specified"));
            }
            targetarrayitemids = ArrayUtil.getArrayItemIdsByLabels(this.getQueryProcessor(), this.arrayid, targetlabels);
        }
        int repeatCount = Integer.parseInt(properties.getProperty("repeatcount", "1"));
        int dilutionCount = Integer.parseInt(properties.getProperty("dilutioncount", "1"));
        float dilutionFactor = Float.parseFloat(properties.getProperty("dilutionfactor", "1"));
        if (repeatCount > 1 && dilutionCount > 1) {
            throw new SapphireException(this.getTranslationProcessor().translate("Both repeatcount and dilutioncount cannot be greater than 1"));
        }
        if (dilutionCount > 1 && dilutionFactor <= 1.0f) {
            throw new SapphireException(this.getTranslationProcessor().translate("Dilution Factor should be greater than 1"));
        }
        if (dilutionCount > 1 && moveentirecontent) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid to choose Move Entire Content when Dilution Count is specified"));
        }
        if (dilutionCount > 1 && targetconcentration != null && targetconcentration.length() > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Target Concentration should not be specified when Dilution Count is specified"));
        }
        if ((repeatCount > 1 || dilutionCount > 1) && this.loadingDirection.equals("RetainShape")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot specify retain shape option for repeats/dilutions"));
        }
        String[] sourcearrayitemidlist = StringUtil.split(sourcearrayitemids, ";");
        String[] targetarrayitemidlist = StringUtil.split(targetarrayitemids, ";");
        this.arrayzoneid = ArrayUtil.findArrayZoneId(this.getQueryProcessor(), this.arrayid, zone);
        if (this.arrayzoneid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array does not have the specified zone"));
        }
        this.isAdhocZone = ArrayUtil.isAdhocZone(this.getQueryProcessor(), this.arrayid, this.arrayzoneid);
        String childSampleType = properties.getProperty("childsampletype", "");
        String createChild = properties.getProperty("createchild", "N");
        if (childSampleType.length() > 0 && createChild.equals("N")) {
            throw new SapphireException(this.getTranslationProcessor().translate("createchild flag has to be specified as S or Y"));
        }
        if (repeatCount == 1 && dilutionCount == 1) {
            if (targetarrayitemidlist.length == sourcearrayitemidlist.length) {
                String[] targetvolumeslist = null;
                String[] targetvolumeunitslist = null;
                String[] targetconcentrationslist = null;
                String[] targetconcentrationunitslist = null;
                if (targetvolume.contains(";")) {
                    targetvolumeslist = StringUtil.split(targetvolume, ";");
                    if (targetvolumeslist.length != targetarrayitemidlist.length) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Target volume needs to be specified for each target"));
                    }
                    targetvolumeunitslist = StringUtil.split(targetvolumeunits, ";");
                    if (targetvolumeslist.length != targetvolumeunitslist.length) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Target volume units needs to be specified for each target"));
                    }
                    if (targetconcentration.length() > 0 && targetconcentration.contains(";")) {
                        targetconcentrationslist = StringUtil.split(targetconcentration, ";");
                        if (targetconcentrationslist.length != targetvolumeslist.length) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Target concentration needs to be specified for each target"));
                        }
                        targetconcentrationunitslist = StringUtil.split(targetconcentrationunits, ";");
                        if (targetconcentrationunitslist.length != targetconcentrationslist.length) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Target concentration units needs to be specified for each target"));
                        }
                    }
                } else {
                    targetvolumeslist = StringUtil.split(PropertyUtil.repeat(targetvolume, sourcearrayitemidlist.length, ";"), ";");
                    targetvolumeunitslist = StringUtil.split(PropertyUtil.repeat(targetvolumeunits, sourcearrayitemidlist.length, ";"), ";");
                    targetconcentrationslist = StringUtil.split(PropertyUtil.repeat(targetconcentration, sourcearrayitemidlist.length, ";"), ";");
                    targetconcentrationunitslist = StringUtil.split(PropertyUtil.repeat(targetconcentrationunits, sourcearrayitemidlist.length, ";"), ";");
                }
                if (this.isAdhocZone) {
                    this.processAdhocZoneItems(targetarrayitemids, this.arrayzoneid);
                }
                for (int i = 0; i < sourcearrayitemidlist.length; ++i) {
                    VolumeDetails updatedVolumeDetails = this.moveSourceToTarget(this.arrayid, sourcearrayitemidlist[i], targetarrayitemidlist[i], this.arrayzoneid, repeatCount, dilutionCount, moveentirecontent, targetvolumeslist[i], targetvolumeunitslist[i], targetconcentrationslist == null ? "" : targetconcentrationslist[i], targetconcentrationunitslist == null ? "" : targetconcentrationunitslist[i]);
                    this.adjustTargetArrayItems(this.arrayid, sourcearrayitemidlist[i], targetarrayitemidlist[i], updatedVolumeDetails);
                    this.validateAndAdjustSourceArrayItems(this.arrayid, sourcearrayitemidlist[i], targetarrayitemidlist[i], repeatCount, updatedVolumeDetails);
                }
                this.processSourceEditArrayItem(this.arrayid);
                this.processTargetEditArrayItem(this.arrayid);
                this.addTargetArrayContent(this.arrayid, this.arrayzoneid, this.cumulativeArrayItemContentDS, childSampleType, createChild);
            } else {
                Set<String> targetWells = null;
                targetWells = this.loadingDirection.equals("RetainShape") ? this.findRelativeTargetWells(sourcearrayitemids, targetarrayitemids, this.isAdhocZone) : this.findTargetWells(sourcearrayitemids, targetarrayitemids, 1, this.isAdhocZone);
                Iterator<String> iter = targetWells.iterator();
                if (this.isAdhocZone) {
                    this.processAdhocZoneItems(targetarrayitemids, this.arrayzoneid);
                }
                if (targetWells.size() == sourcearrayitemidlist.length) {
                    for (int i = 0; i < sourcearrayitemidlist.length; ++i) {
                        String targetwellid = iter.next();
                        VolumeDetails updateVolumeDetails = this.moveSourceToTarget(this.arrayid, sourcearrayitemidlist[i], targetwellid, this.arrayzoneid, repeatCount, dilutionCount, moveentirecontent, targetvolume, targetvolumeunits, targetconcentration, targetconcentrationunits);
                        this.adjustTargetArrayItems(this.arrayid, sourcearrayitemidlist[i], targetwellid, updateVolumeDetails);
                        this.validateAndAdjustSourceArrayItems(this.arrayid, sourcearrayitemidlist[i], targetwellid, repeatCount, updateVolumeDetails);
                    }
                } else {
                    throw new SapphireException("Unexpected exception, targetwell count does not match the number of sources");
                }
                this.processSourceEditArrayItem(this.arrayid);
                this.processTargetEditArrayItem(this.arrayid);
                this.addTargetArrayContent(this.arrayid, this.arrayzoneid, this.cumulativeArrayItemContentDS, childSampleType, createChild);
            }
        } else {
            if (targetarrayitemidlist.length > 1) {
                throw new SapphireException(this.getTranslationProcessor().translate("When specifying repeats you can only specify the starting position"));
            }
            Set<String> targetWells = this.findTargetWells(sourcearrayitemids, targetarrayitemids, Math.max(repeatCount, dilutionCount), this.isAdhocZone);
            Iterator<String> iter = targetWells.iterator();
            if (this.isAdhocZone) {
                this.processAdhocZoneItems(targetarrayitemids, this.arrayzoneid);
            }
            for (int i = 0; i < sourcearrayitemidlist.length; ++i) {
                double dsource = 0.0;
                for (int j = 0; j < Math.max(repeatCount, dilutionCount); ++j) {
                    String targetwellid = iter.next();
                    if (dilutionCount > 1) {
                        dsource += this.moveSourceDilutionToTarget(this.arrayid, sourcearrayitemidlist[i], targetwellid, targetvolume, targetvolumeunits, this.arrayzoneid, j, dilutionFactor, moveentirecontent);
                        this.adjustTargetDilutionArrayItems(this.arrayid, sourcearrayitemidlist[i], targetwellid, j, dilutionFactor, targetvolume, targetvolumeunits);
                        continue;
                    }
                    VolumeDetails updateVolumeDetails = this.moveSourceToTarget(this.arrayid, sourcearrayitemidlist[i], targetwellid, this.arrayzoneid, repeatCount, dilutionCount, moveentirecontent, targetvolume, targetvolumeunits, targetconcentration, targetconcentrationunits);
                    this.adjustTargetArrayItems(this.arrayid, sourcearrayitemidlist[i], targetwellid, updateVolumeDetails);
                    this.validateAndAdjustSourceArrayItems(this.arrayid, sourcearrayitemidlist[i], targetwellid, repeatCount, updateVolumeDetails);
                }
                if (dilutionCount <= 1 || !(dsource > 0.0)) continue;
                this.decrementSourceArrayItem(this.arrayid, sourcearrayitemidlist[i], dsource, targetvolumeunits);
            }
            this.processSourceEditArrayItem(this.arrayid);
            this.processTargetEditArrayItem(this.arrayid);
            this.addTargetArrayContent(this.arrayid, this.arrayzoneid, this.cumulativeArrayItemContentDS, childSampleType, createChild);
        }
    }

    private DataSet getArrayItemContents(String arrayid, String arrayitemid) {
        if (this.arrayitemcontents == null) {
            String sql = "SELECT * FROM arrayitemcontent WHERE arrayitemid in (SELECT arrayitemid FROM arrayitem WHERE arrayid = ? )";
            this.arrayitemcontents = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        }
        if (this.arrayitemcontents != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayitemid", arrayitemid);
            return this.arrayitemcontents.getFilteredDataSet(filter);
        }
        return new DataSet();
    }

    private DataSet getArrayItem(String arrayid, String arrayitemid) {
        if (this.arrayitems == null) {
            String sql = "SELECT * FROM arrayitem WHERE arrayid = ?";
            this.arrayitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        }
        if (this.arrayitems != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayitemid", arrayitemid);
            return this.arrayitems.getFilteredDataSet(filter);
        }
        return new DataSet();
    }

    private VolumeDetails moveSourceToTarget(String arrayid, String sourcearrayitemid, String targetarrayitemid, String arrayzoneid, int repeatCount, int dilutionCount, boolean moveentirecontent, String targetvolume, String targetvolumeunits, String targetconcentration, String targetconcentrationunits) throws SapphireException {
        if (sourcearrayitemid.equals(targetarrayitemid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot move an item onto itself"));
        }
        if (!(ArrayUtil.checkInZone(this.getQueryProcessor(), targetarrayitemid, arrayzoneid) && ArrayUtil.checkInZone(this.getQueryProcessor(), sourcearrayitemid, arrayzoneid) || this.isAdhocZone)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Both source array item and target array item should belong to the specified zone"));
        }
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
        int row = arrayItemContentDS.addRow();
        arrayItemContentDS.setValue(row, "arrayitemid", targetarrayitemid);
        arrayItemContentDS.setValue(row, "contentsdcid", "LV_ArrayItem");
        arrayItemContentDS.setValue(row, "contentkeyid1", sourcearrayitemid);
        if (moveentirecontent) {
            DataSet sourceArrayItem = this.getArrayItem(arrayid, sourcearrayitemid);
            if (sourceArrayItem != null && sourceArrayItem.getRowCount() > 0) {
                if (repeatCount > 1) {
                    double sourceVolume = this.m18NUtil.parseBigDecimal(sourceArrayItem.getValue(0, "totalvolume", "")).doubleValue();
                    double perTarget = sourceVolume / (double)repeatCount;
                    targetvolume = this.m18NUtil.format(new BigDecimal(perTarget));
                } else {
                    targetvolume = sourceArrayItem.getValue(0, "totalvolume", "");
                }
                targetvolumeunits = sourceArrayItem.getValue(0, "totalvolumeunits", "");
                targetconcentration = sourceArrayItem.getValue(0, "concentration", "");
                targetconcentrationunits = sourceArrayItem.getValue(0, "concentrationunits", "");
            } else {
                throw new SapphireException(this.getTranslationProcessor().translate("Failed to fetch sourcearrayitem"));
            }
        }
        arrayItemContentDS.setNumber(row, "volume", targetvolume != null && targetvolume.length() > 0 ? this.m18NUtil.parseBigDecimal(targetvolume) : null);
        arrayItemContentDS.setValue(row, "volumeunits", targetvolumeunits);
        arrayItemContentDS.setNumber(row, "concentration", targetconcentration != null && targetconcentration.length() > 0 ? this.m18NUtil.parseBigDecimal(targetconcentration) : null);
        arrayItemContentDS.setValue(row, "concentrationunits", targetconcentrationunits);
        arrayItemContentDS.setValue(row, "contenttype", "LV_ArrayItem");
        arrayItemContentDS.setValue(row, "contentitem", "Transfer");
        arrayItemContentDS.setValue(row, "arrayid", arrayid);
        arrayItemContentDS.setValue(row, "sourcearrayzoneid", arrayzoneid);
        String contentLabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, "LV_ArrayItem", sourcearrayitemid, "");
        arrayItemContentDS.setValue(row, "contentlabel", contentLabel);
        DataSet sourcecontentdetails = this.getArrayItemContents(arrayid, sourcearrayitemid);
        if (sourcecontentdetails != null) {
            for (int contents = 0; contents < sourcecontentdetails.getRowCount(); ++contents) {
                String sourceitemcontent = sourcecontentdetails.getString(contents, "contentitem");
                Trace.log("Check propagatecontent returned true for:" + sourceitemcontent);
                row = arrayItemContentDS.addRow();
                arrayItemContentDS.setString(row, "arrayitemid", targetarrayitemid);
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
            }
        }
        this.cumulativeArrayItemContentDS.copyRow(arrayItemContentDS, -1, 1);
        VolumeDetails ret = new VolumeDetails();
        ret.targetvolume = targetvolume;
        ret.targetvolumeunits = targetvolumeunits;
        ret.targetconcentration = targetconcentration;
        ret.targetconcentrationunits = targetconcentrationunits;
        return ret;
    }

    private double moveSourceDilutionToTarget(String arrayid, String sourcearrayitemid, String targetarrayitemid, String targetvolume, String targetvolumeunits, String arrayzoneid, int dilutionNum, float dilutionFactor, boolean moveentirecontent) throws SapphireException {
        DataSet sourcecontentdetails;
        if (sourcearrayitemid.equals(targetarrayitemid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot move an item onto itself"));
        }
        if (!ArrayUtil.checkInZone(this.getQueryProcessor(), targetarrayitemid, arrayzoneid) || !ArrayUtil.checkInZone(this.getQueryProcessor(), sourcearrayitemid, arrayzoneid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Both source array item and target array item should belong to the specified zone"));
        }
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
        arrayItemContentDS.addColumn("contentlabel", 0);
        int row = arrayItemContentDS.addRow();
        arrayItemContentDS.setValue(row, "arrayitemid", targetarrayitemid);
        arrayItemContentDS.setValue(row, "contentsdcid", "LV_ArrayItem");
        arrayItemContentDS.setValue(row, "contentkeyid1", sourcearrayitemid);
        if (moveentirecontent) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid to choose Move Entire Content when Dilution Count is specified"));
        }
        DataSet sourceArrayItem = this.getArrayItem(arrayid, sourcearrayitemid);
        String sourceVolumeUnits = sourceArrayItem.getValue(0, "totalvolumeunits", "");
        String sourceConc = sourceArrayItem.getValue(0, "concentration", "").trim();
        String sourceConcUnits = sourceArrayItem.getValue(0, "concentrationunits", "").trim();
        String targetConcentration = "";
        if (sourceConc.length() > 0) {
            double sourceConcNum = this.m18NUtil.parseBigDecimal(sourceConc).doubleValue();
            targetConcentration = String.valueOf(sourceConcNum / Math.pow(dilutionFactor, dilutionNum));
        }
        arrayItemContentDS.setValue(row, "volume", targetvolume);
        arrayItemContentDS.setValue(row, "volumeunits", targetvolumeunits);
        arrayItemContentDS.setValue(row, "concentration", targetConcentration);
        arrayItemContentDS.setValue(row, "concentrationunits", sourceConcUnits);
        arrayItemContentDS.setValue(row, "contenttype", "LV_ArrayItem");
        arrayItemContentDS.setValue(row, "contentitem", "Transfer");
        arrayItemContentDS.setValue(row, "arrayid", arrayid);
        arrayItemContentDS.setValue(row, "sourcearrayzoneid", arrayzoneid);
        String contentLabel = com.labvantage.sapphire.util.array.ArrayUtil.getContentLabels(this.getQueryProcessor(), this.getDAMProcessor(), arrayid, "LV_ArrayItem", sourcearrayitemid, "");
        arrayItemContentDS.setValue(row, "contentlabel", contentLabel);
        ArrayVolumeHandlingUtil util = new ArrayVolumeHandlingUtil(this.m18NUtil);
        double dsourcevolume = util.findAmountToBeDecremented(1, dilutionFactor, "N", targetConcentration, sourceConcUnits, sourceConc, sourceConcUnits, this.m18NUtil.parseBigDecimal(targetvolume).doubleValue(), this.connectionInfo);
        double diluentVolume = this.m18NUtil.parseBigDecimal(targetvolume).doubleValue() - dsourcevolume;
        if (diluentVolume > 0.0) {
            arrayItemContentDS.setValue(row, "diluentvolume", "" + diluentVolume);
            arrayItemContentDS.setValue(row, "diluentvolumeunits", targetvolumeunits);
        }
        if ((sourcecontentdetails = this.getArrayItemContents(arrayid, sourcearrayitemid)) != null) {
            for (int contents = 0; contents < sourcecontentdetails.getRowCount(); ++contents) {
                String sourceitemcontent = sourcecontentdetails.getString(contents, "contentitem");
                Trace.log("Check propagatecontent returned true for:" + sourceitemcontent);
                row = arrayItemContentDS.addRow();
                arrayItemContentDS.setString(row, "arrayitemid", targetarrayitemid);
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
            }
        }
        this.cumulativeArrayItemContentDS.copyRow(arrayItemContentDS, -1, 1);
        return dsourcevolume;
    }

    private void addTargetArrayContent(String arrayid, String arrayzoneid, DataSet arrayItemContentDS, String childSampleType, String createChild) throws SapphireException {
        if (childSampleType.length() > 0 || !createChild.equals("N")) {
            int rowCount = this.dimensions.getInt(0, "numrows");
            int colCount = this.dimensions.getInt(0, "numcolumns");
            arrayItemContentDS = ArrayUtil.processChildSamples(this.getConnectionid(), createChild, childSampleType, arrayItemContentDS, this.loadingDirection, rowCount, colCount);
        }
        PropertyList propertyList = new PropertyList();
        propertyList.setProperty("arrayid", arrayid);
        propertyList.setProperty("arrayzoneid", arrayzoneid);
        propertyList.setProperty("arrayitemid", arrayItemContentDS.getColumnValues("arrayitemid", ";"));
        propertyList.setProperty("contentsdcid", arrayItemContentDS.getColumnValues("contentsdcid", ";"));
        propertyList.setProperty("contentkeyid1", arrayItemContentDS.getColumnValues("contentkeyid1", ";"));
        propertyList.setProperty("volume", arrayItemContentDS.getColumnValues("volume", ";"));
        propertyList.setProperty("volumeunits", arrayItemContentDS.getColumnValues("volumeunits", ";"));
        propertyList.setProperty("concentration", arrayItemContentDS.getColumnValues("concentration", ";"));
        propertyList.setProperty("concentrationunits", arrayItemContentDS.getColumnValues("concentrationunits", ";"));
        propertyList.setProperty("arrayitemcontentid", arrayItemContentDS.getColumnValues("arrayitemcontentid", ";"));
        propertyList.setProperty("contenttype", arrayItemContentDS.getColumnValues("contenttype", ";"));
        propertyList.setProperty("contentitem", arrayItemContentDS.getColumnValues("contentitem", ";"));
        propertyList.setProperty("contentlabel", arrayItemContentDS.getColumnValues("contentlabel", ";"));
        propertyList.setProperty("parentarrayitemid", arrayItemContentDS.getColumnValues("parentarrayitemid", ";"));
        propertyList.setProperty("diluentvolume", arrayItemContentDS.getColumnValues("diluentvolume", ";"));
        propertyList.setProperty("diluentvolumeunits", arrayItemContentDS.getColumnValues("diluentvolumeunits", ";"));
        propertyList.setProperty("auditsignedflag", this.auditInput.auditsignedflag);
        propertyList.setProperty("auditactivity", this.auditInput.auditactivity);
        propertyList.setProperty("auditreason", this.auditInput.auditreason);
        this.getActionProcessor().processAction("AddArrayContent", "1", propertyList);
    }

    private void adjustTargetArrayItems(String arrayid, String sourcearrayitemid, String targetarrayitemid, VolumeDetails volumeDetails) throws SapphireException {
        DataSet targetai = this.getArrayItem(arrayid, targetarrayitemid);
        BigDecimal targetTotalVolume = null;
        String targetTotalVolumeUnits = "";
        if (targetai != null && targetai.getRowCount() > 0) {
            targetTotalVolume = targetai.getBigDecimal(0, "totalvolume", new BigDecimal(0.0));
            targetTotalVolumeUnits = targetai.getValue(0, "totalvolumeunits", volumeDetails.targetvolumeunits);
            if (targetTotalVolumeUnits.equals(volumeDetails.targetvolumeunits)) {
                if (volumeDetails.targetvolume != null && volumeDetails.targetvolume.length() > 0) {
                    BigDecimal increment = FormatUtil.getInstance(this.connectionInfo).parseBigDecimal(volumeDetails.targetvolume);
                    targetTotalVolume = targetTotalVolume.add(increment);
                }
            } else if (volumeDetails.targetvolume != null && volumeDetails.targetvolume.length() > 0) {
                BigDecimal increment = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(volumeDetails.targetvolume), volumeDetails.targetvolumeunits, targetTotalVolumeUnits);
                targetTotalVolume = targetTotalVolume.add(increment);
            }
        }
        this.addToTargetEditArrayItemList(targetarrayitemid, this.m18NUtil.format(targetTotalVolume), targetTotalVolumeUnits, volumeDetails.targetconcentration, volumeDetails.targetconcentrationunits);
    }

    private void adjustTargetDilutionArrayItems(String arrayid, String sourcearrayitemid, String targetarrayitemid, int dilutionNum, float dilutionFactor, String targetvolume, String targetvolumeunits) throws SapphireException {
        DataSet targetai = this.getArrayItem(arrayid, targetarrayitemid);
        BigDecimal targetTotalVolume = null;
        String targetTotalVolumeUnits = "";
        if (targetai != null && targetai.getRowCount() > 0) {
            BigDecimal increment;
            targetTotalVolume = targetai.getBigDecimal(0, "totalvolume", new BigDecimal(0.0));
            targetTotalVolumeUnits = targetai.getValue(0, "totalvolumeunits", targetvolumeunits);
            if (targetTotalVolumeUnits.equals(targetvolumeunits)) {
                if (targetvolume != null && targetvolume.length() > 0) {
                    increment = new BigDecimal(targetvolume);
                    targetTotalVolume = targetTotalVolume.add(increment);
                }
            } else if (targetvolume != null && targetvolume.length() > 0) {
                increment = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(targetvolume), targetvolumeunits, targetTotalVolumeUnits);
                targetTotalVolume = targetTotalVolume.add(increment);
            }
        }
        DataSet sourceArrayItem = this.getArrayItem(arrayid, sourcearrayitemid);
        String sourceConc = sourceArrayItem.getValue(0, "concentration", "").trim();
        String sourceConcUnits = sourceArrayItem.getValue(0, "concentrationunits", "").trim();
        String dilutionConcentration = "";
        if (sourceConc.length() > 0) {
            dilutionConcentration = String.valueOf(this.m18NUtil.parseBigDecimal(sourceConc).doubleValue() / Math.pow(dilutionFactor, dilutionNum));
        }
        this.addToTargetEditArrayItemList(targetarrayitemid, this.m18NUtil.format(targetTotalVolume), targetTotalVolumeUnits, dilutionConcentration, sourceConcUnits);
    }

    private void validateAndAdjustSourceArrayItems(String arrayid, String sourcearrayitemid, String targetarrayitemid, int repeatCount, VolumeDetails volumeDetails) throws SapphireException {
        DataSet sourceArrayItem = this.getArrayItem(arrayid, sourcearrayitemid);
        if (sourceArrayItem != null && sourceArrayItem.getRowCount() > 0) {
            BigDecimal sourceTotalVolume = sourceArrayItem.getBigDecimal(0, "totalvolume", new BigDecimal(0.0));
            if (sourceTotalVolume.compareTo(BigDecimal.ZERO) == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Source well is empty") + "(" + sourcearrayitemid + ")");
            }
            String sourceTotalVolumeUnits = sourceArrayItem.getValue(0, "totalvolumeunits", "");
            BigDecimal sourceConcentration = sourceArrayItem.getBigDecimal(0, "concentration");
            String sourceConcUnits = sourceArrayItem.getValue(0, "concentrationunits", "");
            BigDecimal sourceDecrement = null;
            BigDecimal decrementPerRepeat = null;
            String decrementUnits = "";
            if (sourceTotalVolumeUnits.equals(volumeDetails.targetvolumeunits)) {
                decrementUnits = sourceTotalVolumeUnits;
                if (volumeDetails.targetvolume != null) {
                    if (volumeDetails.targetconcentration.equals(sourceConcentration) && volumeDetails.targetconcentrationunits.equals(sourceConcUnits)) {
                        decrementPerRepeat = FormatUtil.getInstance(this.connectionInfo).parseBigDecimal(volumeDetails.targetvolume);
                        sourceDecrement = decrementPerRepeat.multiply(new BigDecimal(repeatCount));
                        sourceTotalVolume = sourceTotalVolume.subtract(sourceDecrement);
                    } else {
                        BigDecimal targetConc;
                        volumeDetails.targetconcentration = OpalUtil.isNotEmpty(volumeDetails.targetconcentration) ? this.m18NUtil.format(OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(volumeDetails.targetconcentration), volumeDetails.targetconcentrationunits, sourceConcUnits)) : "";
                        volumeDetails.targetconcentrationunits = sourceConcUnits;
                        BigDecimal bigDecimal = targetConc = volumeDetails.targetconcentration != null && volumeDetails.targetconcentration.length() > 0 ? this.m18NUtil.parseBigDecimal(volumeDetails.targetconcentration) : sourceConcentration;
                        if (sourceConcentration != null) {
                            BigDecimal dilutionRatio = targetConc.divide(sourceConcentration);
                            decrementPerRepeat = FormatUtil.getInstance(this.connectionInfo).parseBigDecimal(volumeDetails.targetvolume).multiply(dilutionRatio);
                            sourceDecrement = decrementPerRepeat.multiply(new BigDecimal(repeatCount));
                            sourceTotalVolume = sourceTotalVolume.subtract(sourceDecrement);
                        } else {
                            decrementPerRepeat = FormatUtil.getInstance(this.connectionInfo).parseBigDecimal(volumeDetails.targetvolume);
                            sourceDecrement = decrementPerRepeat.multiply(new BigDecimal(repeatCount));
                            sourceTotalVolume = sourceTotalVolume.subtract(sourceDecrement);
                        }
                    }
                    if (sourceTotalVolume.compareTo(BigDecimal.ZERO) < 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Source volume is insufficient"));
                    }
                }
            } else {
                if (volumeDetails.targetvolume != null && volumeDetails.targetvolume.length() > 0) {
                    decrementPerRepeat = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(volumeDetails.targetvolume), volumeDetails.targetvolumeunits, sourceTotalVolumeUnits);
                    decrementUnits = sourceTotalVolumeUnits;
                    sourceDecrement = decrementPerRepeat.multiply(new BigDecimal(repeatCount));
                    sourceTotalVolume = sourceTotalVolume.subtract(sourceDecrement);
                }
                if (sourceTotalVolume.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Source volume is insufficient"));
                }
            }
            if (sourceDecrement != null) {
                String diluentvolume = "";
                String diluentvolumeunits = "";
                if (volumeDetails.targetvolumeunits.equals(decrementUnits)) {
                    diluentvolume = this.m18NUtil.format(this.m18NUtil.parseBigDecimal(volumeDetails.targetvolume).subtract(decrementPerRepeat));
                    diluentvolumeunits = sourceTotalVolumeUnits;
                } else {
                    BigDecimal decrementPerRepeatConverted = OpalUtil.convertUnit(this.connectionInfo, decrementPerRepeat, decrementUnits, volumeDetails.targetvolumeunits);
                    decrementUnits = volumeDetails.targetvolumeunits;
                    diluentvolume = this.m18NUtil.format(this.m18NUtil.parseBigDecimal(volumeDetails.targetvolume).subtract(decrementPerRepeatConverted));
                    diluentvolumeunits = volumeDetails.targetvolumeunits;
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("arrayitemid", targetarrayitemid);
                filter.put("contentitem", "Transfer");
                int row = this.cumulativeArrayItemContentDS.findRow(filter);
                if (row != -1 && !diluentvolume.startsWith("-") && this.m18NUtil.parseBigDecimal(diluentvolume).doubleValue() > 0.0) {
                    this.cumulativeArrayItemContentDS.setValue(row, "diluentvolume", diluentvolume);
                    this.cumulativeArrayItemContentDS.setValue(row, "diluentvolumeunits", diluentvolumeunits);
                }
            }
            this.addToSourceEditArrayItemList(sourcearrayitemid, this.m18NUtil.format(sourceTotalVolume), sourceTotalVolumeUnits);
        }
    }

    private void decrementSourceArrayItem(String arrayid, String sourcearrayitemid, double decrementSourceInTargetUnits, String targetvolumeunits) throws SapphireException {
        DataSet sourceArrayItem = this.getArrayItem(arrayid, sourcearrayitemid);
        if (sourceArrayItem != null && sourceArrayItem.getRowCount() > 0) {
            BigDecimal sourceTotalVolume = sourceArrayItem.getBigDecimal(0, "totalvolume", new BigDecimal("0"));
            String sourceTotalVolumeUnits = sourceArrayItem.getValue(0, "totalvolumeunits", "");
            if (targetvolumeunits.equals(sourceTotalVolumeUnits)) {
                BigDecimal netTotalVolume = sourceTotalVolume.subtract(BigDecimal.valueOf(decrementSourceInTargetUnits));
                this.addToSourceEditArrayItemList(sourcearrayitemid, this.m18NUtil.format(netTotalVolume), sourceTotalVolumeUnits);
            } else {
                BigDecimal decrementConverted = OpalUtil.convertUnit(this.connectionInfo, BigDecimal.valueOf(decrementSourceInTargetUnits), targetvolumeunits, sourceTotalVolumeUnits);
                BigDecimal netTotalVolume = sourceTotalVolume.subtract(decrementConverted);
                this.addToSourceEditArrayItemList(sourcearrayitemid, this.m18NUtil.format(netTotalVolume), sourceTotalVolumeUnits);
            }
        }
    }

    private void processTargetEditArrayItem(String arrayid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("arrayid", arrayid);
        props.setProperty("arrayitemid", this.editTargetArrayItemsList.getColumnValues("arrayitemid", ";"));
        props.setProperty("totalvol", this.editTargetArrayItemsList.getColumnValues("totalvol", ";"));
        props.setProperty("totalvolunits", this.editTargetArrayItemsList.getColumnValues("totalvolunits", ";"));
        props.setProperty("totalconc", this.editTargetArrayItemsList.getColumnValues("totalconc", ";"));
        props.setProperty("totalconcunits", this.editTargetArrayItemsList.getColumnValues("totalconcunits", ";"));
        this.getActionProcessor().processAction("EditArrayItem", "1", props);
    }

    private void processSourceEditArrayItem(String arrayid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("arrayid", arrayid);
        props.setProperty("arrayitemid", this.editSourceArrayItemsList.getColumnValues("arrayitemid", ";"));
        props.setProperty("totalvol", this.editSourceArrayItemsList.getColumnValues("totalvol", ";"));
        props.setProperty("totalvolunits", this.editSourceArrayItemsList.getColumnValues("totalvolunits", ";"));
        this.getActionProcessor().processAction("EditArrayItem", "1", props);
    }

    private void addToTargetEditArrayItemList(String arrayitemid, String totalVolume, String totalVolumeUnits, String concentration, String concentrationunits) {
        if (this.editTargetArrayItemsList == null) {
            this.editTargetArrayItemsList = new DataSet();
            this.editTargetArrayItemsList.addColumn("arrayitemid", 0);
            this.editTargetArrayItemsList.addColumn("totalvol", 0);
            this.editTargetArrayItemsList.addColumn("totalvolunits", 0);
            this.editTargetArrayItemsList.addColumn("totalconc", 0);
            this.editTargetArrayItemsList.addColumn("totalconcunits", 0);
        }
        int row = this.editTargetArrayItemsList.addRow();
        this.editTargetArrayItemsList.setValue(row, "arrayitemid", arrayitemid);
        this.editTargetArrayItemsList.setValue(row, "totalvol", totalVolume);
        this.editTargetArrayItemsList.setValue(row, "totalvolunits", totalVolumeUnits);
        if (concentration.length() > 0) {
            this.editTargetArrayItemsList.setValue(row, "totalconc", concentration);
            this.editTargetArrayItemsList.setValue(row, "totalconcunits", concentrationunits);
        }
    }

    private void addToSourceEditArrayItemList(String arrayitemid, String totalVolume, String totalVolumeUnits) {
        if (this.editSourceArrayItemsList == null) {
            this.editSourceArrayItemsList = new DataSet();
            this.editSourceArrayItemsList.addColumn("arrayitemid", 0);
            this.editSourceArrayItemsList.addColumn("totalvol", 0);
            this.editSourceArrayItemsList.addColumn("totalvolunits", 0);
        }
        int row = this.editSourceArrayItemsList.addRow();
        this.editSourceArrayItemsList.setValue(row, "arrayitemid", arrayitemid);
        this.editSourceArrayItemsList.setValue(row, "totalvol", totalVolume);
        this.editSourceArrayItemsList.setValue(row, "totalvolunits", totalVolumeUnits);
    }

    private Set<String> findTargetWells(String fromList, String to, int repeatOrDilutionCount, boolean isAdhocZone) {
        LinkedHashSet<String> toSet = new LinkedHashSet<String>();
        String[] from = StringUtil.split(fromList, ";");
        String curr = to;
        for (String f : from) {
            for (int i = 0; i < repeatOrDilutionCount; ++i) {
                String next;
                toSet.add(curr);
                curr = next = this.findNextWell(curr, isAdhocZone, this.loadingDirection);
            }
        }
        return toSet;
    }

    private Set<String> findRelativeTargetWells(String fromList, String to, boolean isAdhocZone) throws SapphireException {
        LinkedHashSet<String> toSet = new LinkedHashSet<String>();
        String[] from = StringUtil.split(fromList, ";");
        String firstSource = null;
        for (String src : from) {
            if (toSet.size() == 0) {
                toSet.add(to);
                firstSource = src;
                continue;
            }
            toSet.add(this.findRelativeWell(firstSource, src, to, isAdhocZone));
        }
        return toSet;
    }

    private String findNextWell(String coordinate, boolean isAdhocZone, String loadingdirection) {
        int rowCount = this.dimensions.getInt(0, "numrows");
        int colCount = this.dimensions.getInt(0, "numcolumns");
        if (loadingdirection.equals("Horizontal")) {
            return this.findNextHorizontalPosition(coordinate, rowCount, colCount, isAdhocZone);
        }
        if (loadingdirection.equals("Vertical")) {
            return this.findNextVerticalPosition(coordinate, rowCount, colCount, isAdhocZone);
        }
        if (loadingdirection.equals("HorizontalSnaking")) {
            return this.findNextHorizontalSnakingPosition(coordinate, rowCount, colCount, isAdhocZone);
        }
        if (loadingdirection.equals("VerticalSnaking")) {
            return this.findNextVerticalSnakingPosition(coordinate, rowCount, colCount, isAdhocZone);
        }
        return this.findNextHorizontalPosition(coordinate, rowCount, colCount, isAdhocZone);
    }

    private String findRelativeWell(String firstSource, String currentSource, String firstTarget, boolean isAdhocZone) throws SapphireException {
        String[] tokens = StringUtil.split(firstSource, "_");
        int firstsourcerow = Integer.parseInt(tokens[1]);
        int firstsourcecolumn = Integer.parseInt(tokens[2]);
        tokens = StringUtil.split(currentSource, "_");
        int currentsourcerow = Integer.parseInt(tokens[1]);
        int currentsourcecolumn = Integer.parseInt(tokens[2]);
        int rowoffset = currentsourcerow - firstsourcerow;
        int columnoffset = currentsourcecolumn - firstsourcecolumn;
        tokens = StringUtil.split(firstTarget, "_");
        int firsttargetrow = Integer.parseInt(tokens[1]);
        int firsttargetcolumn = Integer.parseInt(tokens[2]);
        int currenttargetrow = firsttargetrow + rowoffset;
        int currenttargetcol = firsttargetcolumn + columnoffset;
        String currentwellid = this.arrayid + "_" + currenttargetrow + "_" + currenttargetcol;
        if (isAdhocZone || ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) {
            return currentwellid;
        }
        throw new SapphireException(this.getTranslationProcessor().translate("Invalid target well specified, cannot retain shape in zone"));
    }

    private String findNextHorizontalPosition(String currentwellid, int rowCount, int colCount, boolean isAdhocZone) {
        String[] tokens = StringUtil.split(currentwellid, "_");
        int currentrow = Integer.parseInt(tokens[1]);
        int currentcol = Integer.parseInt(tokens[2]);
        for (int row = currentrow; row < rowCount; ++row) {
            for (int col = 0; col < colCount; ++col) {
                if (row == currentrow && col <= currentcol || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                return this.arrayid + "_" + row + "_" + col;
            }
        }
        return null;
    }

    private String findNextVerticalPosition(String currentwellid, int rowCount, int colCount, boolean isAdhocZone) {
        int currenty;
        String[] tokens = StringUtil.split(currentwellid, "_");
        int currentx = Integer.parseInt(tokens[1]);
        for (int col = currenty = Integer.parseInt(tokens[2]); col < colCount; ++col) {
            for (int row = 0; row < rowCount; ++row) {
                if (col == currenty && row <= currentx || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                return this.arrayid + "_" + row + "_" + col;
            }
        }
        return null;
    }

    private String findNextHorizontalSnakingPosition(String currentwellid, int rowCount, int colCount, boolean isAdhocZone) {
        String[] tokens = StringUtil.split(currentwellid, "_");
        int currentx = Integer.parseInt(tokens[1]);
        int currenty = Integer.parseInt(tokens[2]);
        for (int row = currentx; row < rowCount; ++row) {
            int col;
            boolean forwards;
            boolean bl = forwards = row % 2 == 0;
            if (forwards) {
                for (col = 0; col < colCount; ++col) {
                    if (row == currentx && col <= currenty || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                    return this.arrayid + "_" + row + "_" + col;
                }
                continue;
            }
            for (col = colCount - 1; col >= 0; --col) {
                if (row == currentx && col >= currenty || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                return this.arrayid + "_" + row + "_" + col;
            }
        }
        return null;
    }

    private String findNextVerticalSnakingPosition(String currentwellid, int rowCount, int colCount, boolean isAdhocZone) {
        int currenty;
        String[] tokens = StringUtil.split(currentwellid, "_");
        int currentx = Integer.parseInt(tokens[1]);
        for (int col = currenty = Integer.parseInt(tokens[2]); col < colCount; ++col) {
            int row;
            boolean forwards;
            int currentslice = col;
            boolean bl = forwards = currentslice % 2 == 0;
            if (forwards) {
                for (row = 0; row < rowCount; ++row) {
                    if (col == currenty && row <= currentx || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                    return this.arrayid + "_" + row + "_" + col;
                }
                continue;
            }
            for (row = rowCount - 1; row >= 0; --row) {
                if (col == currenty && row >= currentx || !isAdhocZone && !ArrayUtil.checkInZone(this.getQueryProcessor(), currentwellid, this.arrayzoneid)) continue;
                return this.arrayid + "_" + row + "_" + col;
            }
        }
        return null;
    }

    public void processAdhocZoneItems(String targetarrayitemidlist, String currentzoneid) throws SapphireException {
        String[] targetarrayitemidarr;
        for (String toarrayitemid : targetarrayitemidarr = StringUtil.split(targetarrayitemidlist, ";")) {
            DataSet match;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayitemid", toarrayitemid);
            filter.put("arrayzoneid", currentzoneid);
            if (this.arrayzoneitems == null) {
                String sql = "select * from arrayitemarrayzone where arrayzoneid = ?";
                this.arrayzoneitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{currentzoneid});
            }
            if ((match = this.arrayzoneitems.getFilteredDataSet(filter)) != null && match.getRowCount() != 0 || !this.isAdhocZone) continue;
            int rowInd = this.arrayzoneitems.addRow();
            if (!this.arrayzoneitems.isValidColumn("new")) {
                this.arrayzoneitems.addColumn("new", 0);
            }
            this.arrayzoneitems.setValue(rowInd, "arrayitemid", toarrayitemid);
            this.arrayzoneitems.setValue(rowInd, "arrayzoneid", currentzoneid);
            this.arrayzoneitems.setValue(rowInd, "new", "Y");
            this.arrayzoneitems.setValue(rowInd, "usersequence", String.valueOf(rowInd + 1));
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("new", "Y");
        DataSet match = this.arrayzoneitems.getFilteredDataSet(filter);
        PropertyList addZoneItems = new PropertyList();
        addZoneItems.setProperty("sdcid", "LV_ArrayItem");
        addZoneItems.setProperty("linkid", "Array Item Zone");
        addZoneItems.setProperty("arrayzoneid", match.getColumnValues("arrayzoneid", ";"));
        addZoneItems.setProperty("arrayitemid", match.getColumnValues("arrayitemid", ";"));
        addZoneItems.setProperty("auditsignedflag", this.auditInput.auditsignedflag);
        addZoneItems.setProperty("auditactivity", this.auditInput.auditactivity);
        addZoneItems.setProperty("auditreason", this.auditInput.auditreason);
        this.getActionProcessor().processAction("AddSDIDetail", "1", addZoneItems);
    }

    class VolumeDetails {
        String targetvolume;
        String targetvolumeunits;
        String targetconcentration;
        String targetconcentrationunits;

        VolumeDetails() {
        }
    }

    class AuditInputs {
        String auditreason;
        String auditactivity;
        String auditsignedflag;

        AuditInputs() {
        }
    }
}

