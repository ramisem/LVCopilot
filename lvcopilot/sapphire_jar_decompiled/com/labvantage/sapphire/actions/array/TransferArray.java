/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.actions.MultiSampleChild;
import com.labvantage.opal.actions.storageunit.AddStorageUnit;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sms.CreateSamplePool;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.array.ArraysPolicy;
import com.labvantage.sapphire.array.TargetArrayMap;
import com.labvantage.sapphire.array.util.ArrayVolumeHandlingUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.array.ArrayUtil;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TransferArray
extends BaseAction
implements ArrayConstants,
sapphire.action.TransferArray {
    private String additionalSampleData = "";
    M18NUtil m18NUtil = null;
    AuditInputs auditInput = null;
    String defaultchildsamplestoragestatus = "";
    String defaultchildsamplestatus = "";
    String defaultDisposeSource = "";
    public static final String EXECUTIONMAP_PRIMARY = "primary";
    public static final String EXECUTIONMAP_DETAIL = "detail";
    public static final String EXECUTIONMAP_DETAILITEM = "detailitem";
    public static final String EXECUTIONMAP_INSTRUMENT = "instrument";
    public static final String EXECUTIONMAP_REAGENT = "reagent";
    public static final String EXECUTIONMAP_WORKITEM = "workitem";
    public static final String SDC_ARRAY = "LV_Array";
    public static final String SDC_ARRAYITEM = "LV_ArrayItem";
    Set<String> arrayItemContentSet = new HashSet<String>();
    private final Map<String, Map<String, String>> zoneMapCache = new HashMap<String, Map<String, String>>();
    private final Map<String, Map<String, String>> zoneIDMapCache = new HashMap<String, Map<String, String>>();
    HashMap<String, DataSet> allArrayItem = new HashMap();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet primary;
        String targetIdList;
        String childSampleTypeId;
        String createChildSampleFlag;
        DataSet transferMethodDS;
        String transferMethodId = properties.getProperty("arraytransfermethodid", "");
        String transferMethodVersionId = properties.getProperty("arraytransfermethodversionid", "");
        this.additionalSampleData = properties.getProperty("additionalsampledata");
        this.m18NUtil = new M18NUtil(this.connectionInfo);
        PropertyList policy = this.getConfigurationProcessor().getPolicy("ArraysPolicy", "Sapphire Custom");
        ArraysPolicy policyDef = new ArraysPolicy(policy);
        this.defaultchildsamplestoragestatus = policyDef.getChildSampleStorageStatus();
        this.defaultchildsamplestatus = policyDef.getChildSampleStatus();
        this.defaultDisposeSource = policyDef.getDisposeSourceWhenConsumed();
        this.checkBasicProperties(transferMethodId);
        if (transferMethodVersionId.isEmpty()) {
            transferMethodVersionId = ArrayUtil.getTransferMethodCurrentVersion(this.getQueryProcessor(), transferMethodId);
            properties.setProperty("arraytransfermethodversionid", transferMethodVersionId);
        }
        if ((transferMethodDS = ArrayUtil.getTransferMethodDetails(this.getQueryProcessor(), transferMethodId, transferMethodVersionId)) == null || transferMethodDS.getRowCount() == 0) {
            throw new SapphireException("Invalid Transfer Method specified:" + transferMethodId + "(Version: " + transferMethodVersionId + ")");
        }
        int sourceCount = transferMethodDS.getInt(0, "numsourcearrays");
        int targetCount = transferMethodDS.getInt(0, "numtargetarrays");
        String targetVolume = properties.getProperty("targetvolume", "").trim();
        String targetVolumeUnits = properties.getProperty("targetvolumeunit", "");
        String targetConc = properties.getProperty("targetconcentration", "");
        String targetConcUnits = properties.getProperty("targetconcentrationunit", "");
        if ("S".equals(properties.getProperty("quantityhandlingflag", "D")) && !targetVolume.isEmpty()) {
            throw new SapphireException(this.getTranslationProcessor().translate("Target Volume must not be specified when Quantity Handling Flag is set to \"S\""));
        }
        String sourceArrayTypeId = transferMethodDS.getValue(0, "sourcearraytypeid");
        String sourceArrayTypeVersionId = transferMethodDS.getValue(0, "sourcearraytypeversionid");
        String targetArrayTypeId = transferMethodDS.getValue(0, "targetarraytypeid");
        String targetArrayTypeVersionId = transferMethodDS.getValue(0, "targetarraytypeversionid");
        if (targetVolume.isEmpty()) {
            targetVolume = transferMethodDS.getValue(0, "targetvolume", "");
            targetVolumeUnits = transferMethodDS.getValue(0, "targetvolumeunits", "");
        }
        if (targetVolume.startsWith("-")) {
            throw new SapphireException("Target volume cannot be a negative number");
        }
        if (targetConc.isEmpty()) {
            targetConc = transferMethodDS.getValue(0, "targetconcentration", "");
            targetConcUnits = transferMethodDS.getValue(0, "targetconcentrationunits", "");
        }
        if ((createChildSampleFlag = properties.getProperty("createchild", "")).isEmpty()) {
            createChildSampleFlag = transferMethodDS.getValue(0, "createchildsampleflag", "N");
        }
        if ((childSampleTypeId = properties.getProperty("childsampletype", "")).isEmpty()) {
            childSampleTypeId = transferMethodDS.getValue(0, "sampletypeid", "");
        }
        if (!transferMethodDS.getString(0, "targetarraytypeid", "").equals(targetArrayTypeId) || !transferMethodDS.getString(0, "targetarraytypeversionid", "").equals(targetArrayTypeVersionId)) {
            throw new SapphireException("Target array type does not match the one specified in the transfer method");
        }
        this.auditInput = new AuditInputs();
        this.auditInput.auditactivity = properties.getProperty("auditactivity", "");
        this.auditInput.auditactivity = properties.getProperty("auditactivity", "");
        this.auditInput.auditsignedflag = properties.getProperty("auditsignedflag", "");
        TRANSFERTYPE transfertype = this.determineTransferType(sourceArrayTypeId, targetArrayTypeId);
        if ((transfertype.name().equals("ARRAYTOASL") || transfertype.name().equals("ASLTOASL")) && "(system)".equals(this.getConnectionProcessor().getSapphireConnection().getSysuserId())) {
            throw new SapphireException(this.getTranslationProcessor().translate("When target is an ASL container then TransferArray action must be executed as an application user. Use \"processassysuserid\" property if executing action as a task."));
        }
        String sourceIdList = this.determineSourceList(transfertype, sourceArrayTypeId, sourceArrayTypeVersionId, properties);
        String targetarraylayoutid = properties.getProperty("targetarraylayoutid", "");
        String usesourcelayoutfortarget = properties.getProperty("usesourcelayoutfortarget", "N");
        String[] sources = StringUtil.split(sourceIdList, ";");
        if (targetarraylayoutid.isEmpty() && "Y".equals(usesourcelayoutfortarget)) {
            String targetarraylayoutversionid;
            DataSet ds;
            String findsourcelayout;
            if (transfertype == TRANSFERTYPE.ARRAYTOARRAY) {
                findsourcelayout = "SELECT arraylayoutid, arraylayoutversionid FROM array WHERE arrayid=? ";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(findsourcelayout, new Object[]{sources[0]});
                if (ds.getRowCount() > 0) {
                    targetarraylayoutid = ds.getString(0, "arraylayoutid", "");
                    targetarraylayoutversionid = ds.getValue(0, "arraylayoutversionid", "");
                    properties.setProperty("targetarraylayoutid", targetarraylayoutid);
                    properties.setProperty("targetarraylayoutversionid", targetarraylayoutversionid);
                }
            } else {
                findsourcelayout = "SELECT arraylayoutid, arraylayoutversionid FROM storageunit WHERE storageunitid=? ";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(findsourcelayout, new Object[]{sources[0]});
                if (ds.getRowCount() > 0) {
                    targetarraylayoutid = ds.getString(0, "arraylayoutid", "");
                    targetarraylayoutversionid = ds.getValue(0, "arraylayoutversionid", "");
                    properties.setProperty("targetarraylayoutid", targetarraylayoutid);
                    properties.setProperty("targetarraylayoutversionid", targetarraylayoutversionid);
                }
            }
        }
        String sourceindexstring = properties.getProperty("sourceindex", "");
        int sourceindex = 1;
        String[] sourceList = StringUtil.split(sourceIdList, ";");
        if (!(sourceindexstring.isEmpty() || (sourceindex = Integer.parseInt(sourceindexstring)) >= 1 && sourceindex <= sourceCount)) {
            throw new SapphireException("Source index is invalid.");
        }
        DataSet arrayItemContentDS = new DataSet();
        DataSet arraySourceInventoryDS = new DataSet();
        HashMap<String, Object> targetArrayMap = new HashMap<String, Object>();
        HashMap<String, DataSet> transferExecutionMap = new HashMap<String, DataSet>();
        transferExecutionMap.put(EXECUTIONMAP_PRIMARY, new DataSet());
        transferExecutionMap.put(EXECUTIONMAP_DETAIL, new DataSet());
        transferExecutionMap.put(EXECUTIONMAP_DETAILITEM, new DataSet());
        transferExecutionMap.put(EXECUTIONMAP_INSTRUMENT, new DataSet());
        transferExecutionMap.put(EXECUTIONMAP_REAGENT, new DataSet());
        transferExecutionMap.put(EXECUTIONMAP_WORKITEM, new DataSet());
        if (sourceList.length > sourceCount) {
            String[] targetList;
            String[] tb;
            String[] sb = this.getBatchIds(sourceList, sourceCount);
            if (sb.length != (tb = this.getBatchIds(targetList = StringUtil.split(targetIdList = this.determineTargetList(transfertype, sb.length, targetCount, targetArrayTypeId, targetArrayTypeVersionId, properties), ";"), targetCount)).length) {
                throw new SapphireException("Source/target count mismatch");
            }
            for (int i = 0; i < sb.length; ++i) {
                this.processBatch(transfertype, transferMethodDS, properties, sb[i], sourceindex, tb[i], targetVolume, targetVolumeUnits, targetConc, targetConcUnits, childSampleTypeId, createChildSampleFlag, arrayItemContentDS, targetArrayMap, transferExecutionMap);
            }
        } else {
            targetIdList = this.determineTargetList(transfertype, 1, targetCount, targetArrayTypeId, targetArrayTypeVersionId, properties);
            this.processBatch(transfertype, transferMethodDS, properties, sourceIdList, sourceindex, targetIdList, targetVolume, targetVolumeUnits, targetConc, targetConcUnits, childSampleTypeId, createChildSampleFlag, arrayItemContentDS, targetArrayMap, transferExecutionMap);
        }
        if (OpalUtil.isNotEmpty(arrayItemContentDS)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            String sequenceid = sdf.format(Calendar.getInstance().getTime()) + "-";
            int sequence = this.getSequenceProcessor().getSequence(SDC_ARRAYITEM, sequenceid, arrayItemContentDS.size());
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            DataSet insertDS = new DataSet();
            for (int i = 0; i < arrayItemContentDS.size(); ++i) {
                int row = insertDS.addRow();
                insertDS.setString(row, "arrayitemcontentid", "AC-" + sequenceid + sequence++);
                insertDS.setString(row, "arrayitemid", arrayItemContentDS.getString(i, "arrayitemid"));
                insertDS.setString(row, "contentsdcid", arrayItemContentDS.getString(i, "contentsdcid", ""));
                insertDS.setString(row, "contentkeyid1", arrayItemContentDS.getString(i, "contentkeyid1", ""));
                insertDS.setString(row, "contentkeyid1", arrayItemContentDS.getString(i, "contentkeyid1", ""));
                insertDS.setNumber(row, "volume", arrayItemContentDS.getValue(i, "volume", ""));
                insertDS.setString(row, "volumeunits", arrayItemContentDS.getString(i, "volumeunits", ""));
                insertDS.setNumber(row, "concentration", arrayItemContentDS.getValue(i, "concentration", ""));
                insertDS.setString(row, "concentrationunits", arrayItemContentDS.getString(i, "concentrationunits", ""));
                insertDS.setString(row, "contenttype", arrayItemContentDS.getString(i, "contenttype", ""));
                insertDS.setString(row, "contentitem", arrayItemContentDS.getString(i, "contentitem", ""));
                insertDS.setString(row, "contentlabel", arrayItemContentDS.getString(i, "contentlabel", ""));
                insertDS.setString(row, "parentarrayitemid", arrayItemContentDS.getString(i, "parentarrayitemid", ""));
                insertDS.setNumber(row, "diluentvolume", arrayItemContentDS.getValue(i, "diluentvolume", ""));
                insertDS.setString(row, "diluentvolumeunits", arrayItemContentDS.getString(i, "diluentvolumeunits", ""));
                insertDS.setString(row, "sourcearrayzoneid", arrayItemContentDS.getString(i, "sourcearrayzoneid", ""));
            }
            insertDS.setString(-1, "createby", sysuserid);
            insertDS.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
            insertDS.setDate(-1, "createtool", "TransferArray");
            insertDS.setString(-1, "modby", sysuserid);
            insertDS.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
            insertDS.setDate(-1, "modtool", "TransferArray");
            DataSetUtil.insert(this.database, insertDS, "arrayitemcontent");
        }
        if (!targetArrayMap.isEmpty()) {
            String[] targetArrayIDs = StringUtil.split(targetIdList, ";");
            String sourcevolumetodecrement = properties.getProperty("sourcevolume", "");
            String sourcevolumetodecrementunits = properties.getProperty("sourcevolumeunit", "");
            String quantityhandlingflag = properties.getProperty("quantityhandlingflag", "D");
            boolean validatesourcevolume = "Y".equals(properties.getProperty("validatesourcevolume", "N"));
            DataSet filteredInventoryDS = new DataSet();
            for (String targetArrayID : targetArrayIDs) {
                ArrayList targetArrayMapArrayList = (ArrayList)targetArrayMap.get(targetArrayID);
                DataSet inventoryDS = (DataSet)targetArrayMap.get(targetArrayID + "_inventoryDS");
                DataSet ret = this.updateTargetArrayVolumeConc(targetVolume, targetVolumeUnits, targetConc, targetConcUnits, targetArrayID, targetArrayMapArrayList, validatesourcevolume, arraySourceInventoryDS);
                if (ret == null) continue;
                for (int j = 0; j < inventoryDS.getRowCount(); ++j) {
                    String sourcearrayitemid = inventoryDS.getString(j, "contentkeyid1");
                    if (!this.checkSourceVolume(sourcearrayitemid, sourcearrayitemid.substring(0, sourcearrayitemid.indexOf("_") - 1))) continue;
                    String targetarrayitemid = inventoryDS.getString(j, "targetarrayitemid");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sourcearrayitemid", sourcearrayitemid);
                    filter.put("arrayitemid", targetarrayitemid);
                    DataSet match = ret.getFilteredDataSet(filter);
                    if (match.getRowCount() <= 0) continue;
                    inventoryDS.setString(j, "totalvolume", match.getValue(0, "totalvolume"));
                    inventoryDS.setString(j, "volumeunits", match.getValue(0, "totalvolumeunits"));
                    filteredInventoryDS.copyRow(inventoryDS, j, 1);
                }
            }
            if (filteredInventoryDS.getRowCount() > 0) {
                this.adjustSourceInventory(sourcevolumetodecrement, sourcevolumetodecrementunits, quantityhandlingflag, validatesourcevolume, targetVolume, targetVolumeUnits, targetConc, targetConcUnits, filteredInventoryDS);
            }
            if (OpalUtil.isNotEmpty(arraySourceInventoryDS)) {
                PropertyList arrayItemProps = new PropertyList();
                arrayItemProps.setProperty("arrayitemid", arraySourceInventoryDS.getColumnValues("arrayitemid", ";"));
                arrayItemProps.setProperty("totalvol", arraySourceInventoryDS.getColumnValues("totalvolume", ";"));
                arrayItemProps.setProperty("totalvolunits", arraySourceInventoryDS.getColumnValues("totalvolumeunits", ";"));
                arrayItemProps.setProperty("totalconc", arraySourceInventoryDS.getColumnValues("concentration", ";"));
                arrayItemProps.setProperty("totalconcunits", arraySourceInventoryDS.getColumnValues("concentrationunits", ";"));
                arrayItemProps.setProperty("validatetotalvolume", "N");
                arrayItemProps.setProperty("auditsignedflag", this.auditInput.auditsignedflag);
                arrayItemProps.setProperty("auditactivity", this.auditInput.auditactivity);
                arrayItemProps.setProperty("auditreason", this.auditInput.auditreason);
                this.getActionProcessor().processAction("EditArrayItem", "1", arrayItemProps);
            }
        }
        if (TRANSFERTYPE.ARRAYTOARRAY == transfertype || TRANSFERTYPE.ASLTOARRAY.equals((Object)transfertype)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_ARRAY);
            props.setProperty("keyid1", targetIdList);
            props.setProperty("arraystatus", "Loaded");
            props.setProperty("auditsignedflag", this.auditInput.auditsignedflag);
            props.setProperty("auditactivity", this.auditInput.auditactivity);
            props.setProperty("auditreason", this.auditInput.auditreason);
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (OpalUtil.isNotEmpty(primary = (DataSet)transferExecutionMap.get(EXECUTIONMAP_PRIMARY))) {
            String transferexecutionid;
            String transferexecutiondetailid;
            String sysuserid = this.connectionInfo.getSysuserId();
            Calendar now = DateTimeUtil.getNowCalendar();
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_TransferExecution");
            props.setProperty("copies", "1");
            props.setProperty("copies", String.valueOf(primary.size()));
            props.setProperty("volume", primary.getColumnValues("volume", ";"));
            props.setProperty("volumeunits", primary.getColumnValues("volumeunits", ";"));
            props.setProperty("concentration", primary.getColumnValues("concentration", ";"));
            props.setProperty("concentrationunits", primary.getColumnValues("concentrationunits", ";"));
            props.setProperty("sampletypeid", primary.getColumnValues("sampletypeid", ";"));
            props.setProperty("preptypeid", primary.getColumnValues("preptypeid", ";"));
            props.setProperty("treatmenttypeid", primary.getColumnValues("treatmenttypeid", ";"));
            props.setProperty("deductparentvolume", primary.getColumnValues("deductparentvolume", ";"));
            props.setProperty("deductparentvolumeunits", primary.getColumnValues("deductparentvolumeunits", ";"));
            props.setProperty("arraytransfermethodid", primary.getColumnValues("arraytransfermethodid", ";"));
            props.setProperty("arraytransfermethodversionid", primary.getColumnValues("arraytransfermethodversionid", ";"));
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            String addtransferexecutionid = props.getProperty("newkeyid1");
            properties.setProperty("transferexecutionid", addtransferexecutionid);
            List<String> transferExecutionIDList = OpalUtil.toList(addtransferexecutionid, ";");
            DataSet detailDS = (DataSet)transferExecutionMap.get(EXECUTIONMAP_DETAIL);
            DataSet detailItemDS = (DataSet)transferExecutionMap.get(EXECUTIONMAP_DETAILITEM);
            DataSet instrumentsDS = (DataSet)transferExecutionMap.get(EXECUTIONMAP_INSTRUMENT);
            DataSet reagentsDS = (DataSet)transferExecutionMap.get(EXECUTIONMAP_REAGENT);
            DataSet workitemDS = (DataSet)transferExecutionMap.get(EXECUTIONMAP_WORKITEM);
            if (OpalUtil.isNotEmpty(detailDS)) {
                for (int i = 0; i < detailDS.size(); ++i) {
                    int index = Integer.parseInt(detailDS.getString(i, "transferexecutionid"));
                    String transferexecutionid2 = transferExecutionIDList.get(index);
                    detailDS.setValue(i, "transferexecutionid", transferexecutionid2);
                    transferexecutiondetailid = detailDS.getString(i, "transferexecutiondetailid");
                    transferexecutiondetailid = transferexecutionid2 + transferexecutiondetailid.substring(transferexecutiondetailid.lastIndexOf("-"));
                    detailDS.setValue(i, "transferexecutiondetailid", transferexecutiondetailid);
                    detailDS.setDate(i, "createdt", now);
                    detailDS.setString(i, "createby", sysuserid);
                    detailDS.setString(i, "createtool", "TransferArray");
                    detailDS.setDate(i, "moddt", now);
                    detailDS.setString(i, "modby", sysuserid);
                    detailDS.setString(i, "modtool", "TransferArray");
                }
                DataSetUtil.insert(this.database, detailDS, "transferexecutiondetail");
            }
            if (OpalUtil.isNotEmpty(detailItemDS)) {
                for (int i = 0; i < detailItemDS.size(); ++i) {
                    int index = Integer.parseInt(detailItemDS.getString(i, "transferexecutionid"));
                    String transferexecutionid3 = transferExecutionIDList.get(index);
                    detailItemDS.setValue(i, "transferexecutionid", transferexecutionid3);
                    transferexecutiondetailid = detailItemDS.getString(i, "transferexecutiondetailid");
                    transferexecutiondetailid = transferexecutionid3 + transferexecutiondetailid.substring(transferexecutiondetailid.lastIndexOf("-"));
                    detailItemDS.setValue(i, "transferexecutiondetailid", transferexecutiondetailid);
                    String transferexecutiondetailitemid = detailItemDS.getString(i, "transferexecutiondetailitemid");
                    transferexecutiondetailitemid = transferexecutiondetailid + transferexecutiondetailitemid.substring(transferexecutiondetailitemid.lastIndexOf("-"));
                    detailItemDS.setValue(i, "transferexecutiondetailitemid", transferexecutiondetailitemid);
                    detailItemDS.setDate(i, "createdt", now);
                    detailItemDS.setString(i, "createby", sysuserid);
                    detailItemDS.setString(i, "createtool", "TransferArray");
                    detailItemDS.setDate(i, "moddt", now);
                    detailItemDS.setString(i, "modby", sysuserid);
                    detailItemDS.setString(i, "modtool", "TransferArray");
                }
                DataSetUtil.insert(this.database, detailItemDS, "transferexecutiondetailitem");
            }
            if (OpalUtil.isNotEmpty(instrumentsDS)) {
                ArrayList<DataSet> dataSetList = instrumentsDS.getGroupedDataSets("transferexecutionid");
                instrumentsDS.reset();
                for (DataSet ds : dataSetList) {
                    int index = Integer.parseInt(ds.getString(0, "transferexecutionid"));
                    transferexecutionid = transferExecutionIDList.get(index);
                    for (int i = 0; i < ds.size(); ++i) {
                        ds.setString(i, "transferexecutionid", transferexecutionid);
                        ds.setString(i, "transferexecutioninstrumentid", transferexecutionid + "-" + (i + 1));
                        ds.setDate(i, "createdt", now);
                        ds.setString(i, "createby", sysuserid);
                        ds.setString(i, "createtool", "TransferArray");
                        ds.setDate(i, "moddt", now);
                        ds.setString(i, "modby", sysuserid);
                        ds.setString(i, "modtool", "TransferArray");
                        instrumentsDS.copyRow(ds, i, 1);
                    }
                }
                DataSetUtil.insert(this.database, instrumentsDS, "transferexecutioninstrument");
            }
            if (OpalUtil.isNotEmpty(reagentsDS)) {
                ArrayList<DataSet> dataSetList = reagentsDS.getGroupedDataSets("transferexecutionid");
                reagentsDS.reset();
                for (DataSet ds : dataSetList) {
                    int index = Integer.parseInt(ds.getString(0, "transferexecutionid"));
                    transferexecutionid = transferExecutionIDList.get(index);
                    for (int i = 0; i < ds.size(); ++i) {
                        ds.setString(i, "transferexecutionid", transferexecutionid);
                        ds.setString(i, "transferexecutionreagentid", transferexecutionid + "-" + (i + 1));
                        ds.setDate(i, "createdt", now);
                        ds.setString(i, "createby", sysuserid);
                        ds.setString(i, "createtool", "TransferArray");
                        ds.setDate(i, "moddt", now);
                        ds.setString(i, "modby", sysuserid);
                        ds.setString(i, "modtool", "TransferArray");
                        reagentsDS.copyRow(ds, i, 1);
                    }
                }
                DataSetUtil.insert(this.database, reagentsDS, "transferexecutionreagent");
            }
            if (OpalUtil.isNotEmpty(workitemDS)) {
                ArrayList<DataSet> dataSetList = workitemDS.getGroupedDataSets("transferexecutionid");
                workitemDS.reset();
                for (DataSet ds : dataSetList) {
                    int index = Integer.parseInt(ds.getString(0, "transferexecutionid"));
                    transferexecutionid = transferExecutionIDList.get(index);
                    for (int i = 0; i < ds.size(); ++i) {
                        ds.setString(i, "transferexecutionid", transferexecutionid);
                        workitemDS.copyRow(ds, i, 1);
                    }
                }
                props.clear();
                props.setProperty("sdcid", "LV_TransferExecution");
                props.setProperty("keyid1", workitemDS.getColumnValues("transferexecutionid", ";"));
                props.setProperty("workitemid", workitemDS.getColumnValues("workitemid", ";"));
                props.setProperty("workitemversionid", workitemDS.getColumnValues("workitemversionid", ";"));
                props.setProperty("applyonaddflag", workitemDS.getColumnValues("applyonaddflag", ";"));
                props.setProperty("s_sampletypeid", workitemDS.getColumnValues("s_sampletypeid", ";"));
                props.setProperty("applyworkitem", "N");
                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
            }
        }
    }

    private String[] getBatchIds(String[] list, int count) {
        int i = 0;
        StringBuilder all = new StringBuilder();
        while (i < list.length) {
            StringBuilder temp = new StringBuilder();
            for (int j = 0; j < count && i < list.length; ++j) {
                if (temp.length() > 0) {
                    temp.append(";");
                }
                temp.append(list[i++]);
            }
            if (all.length() > 0) {
                all.append("|");
            }
            all.append((CharSequence)temp);
        }
        return StringUtil.split(all.toString(), "|");
    }

    private void processBatch(TRANSFERTYPE transfertype, DataSet transferMethodDS, PropertyList properties, String sourceIdList, int sourceindex, String targetIdList, String targetVolume, String targetVolumeUnits, String targetConc, String targetConcUnits, String childSampleTypeId, String createChildSampleFlag, DataSet arrayItemContentDS, Map<String, Object> targetArrayMap, Map<String, DataSet> transferExecutionMap) throws SapphireException {
        String sourceArrayTypeId = transferMethodDS.getValue(0, "sourcearraytypeid");
        String sourceArrayTypeVersionId = transferMethodDS.getValue(0, "sourcearraytypeversionid");
        String targetArrayTypeId = transferMethodDS.getValue(0, "targetarraytypeid");
        String targetArrayTypeVersionId = transferMethodDS.getValue(0, "targetarraytypeversionid");
        int sourceCount = transferMethodDS.getInt(0, "numsourcearrays");
        int targetCount = transferMethodDS.getInt(0, "numtargetarrays");
        SourceInputs sourceInputs = new SourceInputs();
        sourceInputs.sourceid = sourceIdList;
        sourceInputs.sourceindex = sourceindex;
        sourceInputs.sourcearraytypeid = sourceArrayTypeId;
        sourceInputs.sourcearraytypeversionid = sourceArrayTypeVersionId;
        sourceInputs.maxsourcecount = sourceCount;
        sourceInputs.sourcevolumetodecrement = properties.getProperty("sourcevolume", "");
        sourceInputs.sourcevolumetodecrementunits = properties.getProperty("sourcevolumeunit", "");
        sourceInputs.validatesourcevolume = "Y".equals(properties.getProperty("validatesourcevolume", "N"));
        sourceInputs.quantityhandlingflag = properties.getProperty("quantityhandlingflag", "D");
        TargetInputs t = new TargetInputs();
        t.targetid = targetIdList;
        t.targetarraytypeid = targetArrayTypeId;
        t.targetarraytypeversionid = targetArrayTypeVersionId;
        t.targetvolume = targetVolume;
        t.targetvolumeunits = targetVolumeUnits;
        t.targetconc = targetConc;
        t.targetconcunits = targetConcUnits;
        t.maxtargetcount = targetCount;
        t.targetarraylayoutid = properties.getProperty("targetarraylayoutid", "");
        t.targetarraylayoutversionid = properties.getProperty("targetarraylayoutversionid", "1");
        DataSet transferDataSet = new DataSet();
        if (transfertype == TRANSFERTYPE.ASLTOASL) {
            boolean disposeSourceWhenConsumed = "Y".equals(properties.getProperty("disposesourcewhenconsumed", this.defaultDisposeSource));
            transferDataSet = this.doASLToASLTransfer(sourceInputs, t, transferMethodDS, createChildSampleFlag.equals("Y"), childSampleTypeId, disposeSourceWhenConsumed);
        } else if (transfertype == TRANSFERTYPE.ASLTOARRAY) {
            boolean disposeSourceWhenConsumed = "Y".equals(properties.getProperty("disposesourcewhenconsumed", this.defaultDisposeSource));
            transferDataSet = this.doASLToArrayTransfer(sourceInputs, t, transferMethodDS, createChildSampleFlag, childSampleTypeId, disposeSourceWhenConsumed);
        } else if (transfertype == TRANSFERTYPE.ARRAYTOASL) {
            transferDataSet = this.doArrayToASLTransfer(sourceInputs, t, transferMethodDS, createChildSampleFlag.equals("Y"), childSampleTypeId);
        } else {
            String mode = "new";
            if (targetIdList.equals(properties.getProperty("targetarrayid", ""))) {
                mode = "edit";
            }
            this.doArrayToArrayTransfer(mode, sourceInputs, t, this.auditInput, transferMethodDS, createChildSampleFlag, childSampleTypeId, arrayItemContentDS, targetArrayMap);
        }
        try {
            if (transferDataSet.getRowCount() > 0 || transfertype == TRANSFERTYPE.ARRAYTOARRAY) {
                properties.setProperty("__sourceidlist", sourceIdList);
                properties.setProperty("__targetidlist", targetIdList);
                this.addTransferExecution(properties, transferDataSet, transfertype, transferExecutionMap);
            }
        }
        catch (SapphireException e) {
            Trace.log("Error creating Transfer Execution. " + e.getMessage());
            throw new SapphireException("Failed to add TransferExecution");
        }
    }

    private void addTransferExecution(PropertyList properties, DataSet transferDataSet, TRANSFERTYPE transfertype, Map<String, DataSet> transferExecutionMap) throws SapphireException {
        DataSet dsWorkitems;
        DataSet dsReagentTypes;
        DataSet dsInstruments;
        long start = System.currentTimeMillis();
        String transferMethodId = properties.getProperty("arraytransfermethodid");
        String transferMethodVersionId = properties.getProperty("arraytransfermethodversionid");
        String sourceVolume = properties.getProperty("sourcevolume");
        String sourceVolumeUnits = properties.getProperty("sourcevolumeunit");
        if (transferMethodId.length() == 0 || transferMethodVersionId.length() == 0) {
            return;
        }
        String inputsampletypeid = "";
        String preptypeid = "";
        String treatmenttypeid = "";
        if (OpalUtil.isNotEmpty(this.additionalSampleData)) {
            try {
                JSONArray array = new JSONArray(this.additionalSampleData);
                block12: for (int i = 0; i < array.length(); ++i) {
                    JSONObject o = array.getJSONObject(i);
                    if (!o.has("transfercolumn") || !o.has("value")) continue;
                    String transfercolumn = o.getString("transfercolumn");
                    String value = o.getString("value");
                    switch (transfercolumn) {
                        case "sampletypeid": {
                            inputsampletypeid = value;
                            continue block12;
                        }
                        case "preptypeid": {
                            preptypeid = value;
                            continue block12;
                        }
                        case "treatmenttypeid": {
                            treatmenttypeid = value;
                        }
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        DataSet primary = transferExecutionMap.get(EXECUTIONMAP_PRIMARY);
        int primaryRow = primary.addRow();
        primary.setString(primaryRow, "volume", properties.getProperty("targetvolume", ""));
        primary.setString(primaryRow, "volumeunits", properties.getProperty("targetvolumeunit", ""));
        primary.setString(primaryRow, "concentration", properties.getProperty("targetconcentration", ""));
        primary.setString(primaryRow, "concentrationunits", properties.getProperty("targetconcentrationunit", ""));
        primary.setString(primaryRow, "sampletypeid", inputsampletypeid);
        primary.setString(primaryRow, "preptypeid", preptypeid);
        primary.setString(primaryRow, "treatmenttypeid", treatmenttypeid);
        primary.setString(primaryRow, "deductparentvolume", sourceVolume);
        primary.setString(primaryRow, "deductparentvolumeunits", sourceVolumeUnits);
        primary.setString(primaryRow, "arraytransfermethodid", properties.getProperty("arraytransfermethodid", ""));
        primary.setString(primaryRow, "arraytransfermethodversionid", properties.getProperty("arraytransfermethodversionid", ""));
        String transferExecutionID = String.valueOf(primaryRow);
        SafeSQL safeSQL = new SafeSQL();
        HashSet<String> childSampleSet = new HashSet<String>();
        DataSet detailDS = new DataSet();
        DataSet detailItemDS = new DataSet();
        if (TRANSFERTYPE.ASLTOASL == transfertype) {
            String trackitems = transferDataSet.getColumnValues("trackitemid", ";") + ";" + transferDataSet.getColumnValues("childtrackitemid", ";");
            String sql = "select t.trackitemid, t.linksdcid trackitemsdcid, t.linkkeyid1 trackitemkeyid1, su.storageunitid, t.qtycurrent, t.qtyunits, t.diluentvolume, t.diluentvolumeunits, su.storageunitlabel, su.parentid suparentid, t.currentstorageunitid, (select sup.linksdcid from storageunit sup where sup.storageunitid = su.parentid) containersdcid, (select sup.linkkeyid1 from storageunit sup where sup.storageunitid = su.parentid) containerkeyid1 from trackitem t left outer join storageunit su on t.currentstorageunitid = su.storageunitid where t.trackitemid in (" + safeSQL.addIn(trackitems, ";") + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int i = 0; i < transferDataSet.size(); ++i) {
                String parenttrackitemid = transferDataSet.getString(i, "trackitemid");
                String childtrackitemid = transferDataSet.getString(i, "childtrackitemid");
                int parentTrackItemRow = ds.findRow("trackitemid", parenttrackitemid);
                int childTrackItemRow = ds.findRow("trackitemid", childtrackitemid);
                if (parentTrackItemRow == -1 || childTrackItemRow == -1) continue;
                String parentContainerSDCID = ds.getString(parentTrackItemRow, "containersdcid", "");
                String parentContainerKeyID = ds.getString(parentTrackItemRow, "containerkeyid1", "");
                if (parentContainerSDCID.length() == 0 || parentContainerKeyID.length() == 0) {
                    sql = "select e.oldstorageunitid, su.storageunitlabel, coalesce(sup.linksdcid, su.linksdcid) containersdcid, coalesce(sup.linkkeyid1, su.linkkeyid1) containerkeyid1 from s_eventlog e, storageunit su, storageunit sup where su.storageunitid = e.oldstorageunitid and sup.storageunitid = su.parentid and e.trackitemid = ? and e.eventtype = 'LocationChange' and e.newstorageunitid is null order by e.createdt desc";
                    DataSet d = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{parenttrackitemid});
                    if (d == null || d.size() <= 0) continue;
                    parentContainerSDCID = d.getString(0, "containersdcid");
                    parentContainerKeyID = d.getString(0, "containerkeyid1");
                    ds.setString(parentTrackItemRow, "containersdcid", parentContainerSDCID);
                    ds.setString(parentTrackItemRow, "containerkeyid1", parentContainerKeyID);
                    ds.setString(parentTrackItemRow, "storageunitid", d.getString(0, "oldstorageunitid"));
                    ds.setString(parentTrackItemRow, "storageunitlabel", d.getString(0, "storageunitlabel"));
                }
                String childContainerSDCID = ds.getString(childTrackItemRow, "containersdcid", "");
                String childContainerKeyID = ds.getString(childTrackItemRow, "containerkeyid1", "");
                String transferExecutionDetailID = "";
                findMap.clear();
                findMap.put("sourcesdcid", parentContainerSDCID);
                findMap.put("sourcekeyid1", parentContainerKeyID);
                findMap.put("targetsdcid", childContainerSDCID);
                findMap.put("targetkeyid1", childContainerKeyID);
                int detailRow = detailDS.findRow(findMap);
                if (detailRow == -1) {
                    detailRow = detailDS.addRow();
                    transferExecutionDetailID = transferExecutionID + '-' + detailDS.size();
                    detailDS.setString(detailRow, "transferexecutionid", transferExecutionID);
                    detailDS.setString(detailRow, "transferexecutiondetailid", transferExecutionDetailID);
                    detailDS.setString(detailRow, "sourcesdcid", parentContainerSDCID);
                    detailDS.setString(detailRow, "sourcekeyid1", parentContainerKeyID);
                    detailDS.setString(detailRow, "targetsdcid", childContainerSDCID);
                    detailDS.setString(detailRow, "targetkeyid1", childContainerKeyID);
                    detailDS.setNumber(detailRow, "usersequence", detailDS.size());
                } else {
                    transferExecutionDetailID = detailDS.getString(detailRow, "transferexecutiondetailid");
                }
                int detailItemRow = detailItemDS.addRow();
                detailItemDS.setString(detailItemRow, "transferexecutionid", transferExecutionID);
                detailItemDS.setString(detailItemRow, "transferexecutiondetailid", transferExecutionDetailID);
                detailItemDS.setString(detailItemRow, "transferexecutiondetailitemid", transferExecutionDetailID + '-' + detailItemDS.size());
                detailItemDS.setString(detailItemRow, "sourcepositionid", ds.getString(parentTrackItemRow, "storageunitid"));
                detailItemDS.setString(detailItemRow, "sourcepositionlabel", ds.getString(parentTrackItemRow, "storageunitlabel"));
                detailItemDS.setString(detailItemRow, "targetpositionid", ds.getString(childTrackItemRow, "storageunitid"));
                detailItemDS.setString(detailItemRow, "targetpositionlabel", ds.getString(childTrackItemRow, "storageunitlabel"));
                detailItemDS.setString(detailItemRow, "sourcesdcid", ds.getString(parentTrackItemRow, "trackitemsdcid"));
                detailItemDS.setString(detailItemRow, "sourcekeyid1", ds.getString(parentTrackItemRow, "trackitemkeyid1"));
                detailItemDS.setString(detailItemRow, "targetitemsdcid", ds.getString(childTrackItemRow, "trackitemsdcid"));
                detailItemDS.setString(detailItemRow, "targetitemkeyid1", ds.getString(childTrackItemRow, "trackitemkeyid1"));
                double qtycurrent = ds.getDouble(childTrackItemRow, "qtycurrent", 0.0);
                String qtyunits = ds.getString(childTrackItemRow, "qtyunits", "");
                double diluentvolume = ds.getDouble(childTrackItemRow, "diluentvolume");
                String diluentvolumeunits = ds.getString(childTrackItemRow, "diluentvolumeunits", "");
                if (diluentvolume >= 0.0) {
                    detailItemDS.setNumber(detailItemRow, "diluentvolume", this.convertToBigDecimal(diluentvolume));
                    detailItemDS.setString(detailItemRow, "diluentvolumeunits", diluentvolumeunits);
                }
                if (sourceVolume.length() == 0) {
                    double sourcedecrementedvolume = qtycurrent - (diluentvolume > 0.0 ? diluentvolume : 0.0);
                    detailItemDS.setNumber(detailItemRow, "sourcedecrementedvolume", this.convertToBigDecimal(sourcedecrementedvolume));
                    detailItemDS.setString(detailItemRow, "sourcedecrementedvolumeunits", qtyunits);
                } else {
                    detailItemDS.setNumber(detailItemRow, "sourcedecrementedvolume", this.convertToBigDecimal(sourceVolume));
                    detailItemDS.setString(detailItemRow, "sourcedecrementedvolumeunits", sourceVolumeUnits);
                }
                detailItemDS.setNumber(detailItemRow, "usersequence", detailItemDS.size());
                if (!"Sample".equals(ds.getString(childTrackItemRow, "trackitemsdcid"))) continue;
                childSampleSet.add(ds.getString(childTrackItemRow, "trackitemkeyid1"));
            }
        } else if (TRANSFERTYPE.ARRAYTOARRAY == transfertype) {
            String targetArrayID = properties.getProperty("__targetidlist");
            List<String> targetArrayList = OpalUtil.toUniqueList(targetArrayID, ";");
            String sql = "select parent.arrayid parentarrayid, child.arrayid childarrayid, {fn concat(parent.arrayid, child.arrayid)} idkey from arrayitemcontent, arrayitem parent, arrayitem child where parent.arrayitemid = arrayitemcontent.parentarrayitemid   and child.arrayitemid = arrayitemcontent.arrayitemid   and arrayitemcontent.arrayitemid in (select a.arrayitemid from arrayitem a where a.arrayid in (" + safeSQL.addIn(targetArrayID, ";") + "))";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null) {
                HashSet<String> keyset = new HashSet<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    String idkey = ds.getString(i, "idkey", "");
                    if (!keyset.add(idkey)) continue;
                    String parentArrayID = ds.getString(i, "parentarrayid");
                    String childArrayID = ds.getString(i, "childarrayid");
                    int detailRow = detailDS.addRow();
                    String transferExecutionDetailID = transferExecutionID + '-' + detailDS.size();
                    detailDS.setString(detailRow, "transferexecutionid", transferExecutionID);
                    detailDS.setString(detailRow, "transferexecutiondetailid", transferExecutionDetailID);
                    detailDS.setString(detailRow, "sourcesdcid", SDC_ARRAY);
                    detailDS.setString(detailRow, "sourcekeyid1", parentArrayID);
                    detailDS.setString(detailRow, "targetsdcid", SDC_ARRAY);
                    detailDS.setString(detailRow, "targetkeyid1", childArrayID);
                    detailDS.setNumber(detailRow, "usersequence", detailDS.size());
                    targetArrayList.remove(childArrayID);
                }
                if (targetArrayList.size() > 0) {
                    String sourceArrayID = properties.getProperty("__sourceidlist");
                    List<String> sourceArrayList = OpalUtil.toUniqueList(sourceArrayID, ";");
                    for (String aSourceArrayList : sourceArrayList) {
                        for (String aTargetArrayList : targetArrayList) {
                            int detailRow = detailDS.addRow();
                            String transferExecutionDetailID = transferExecutionID + '-' + detailDS.size();
                            detailDS.setString(detailRow, "transferexecutionid", transferExecutionID);
                            detailDS.setString(detailRow, "transferexecutiondetailid", transferExecutionDetailID);
                            detailDS.setString(detailRow, "sourcesdcid", SDC_ARRAY);
                            detailDS.setString(detailRow, "sourcekeyid1", aSourceArrayList);
                            detailDS.setString(detailRow, "targetsdcid", SDC_ARRAY);
                            detailDS.setString(detailRow, "targetkeyid1", aTargetArrayList);
                            detailDS.setNumber(detailRow, "usersequence", detailDS.size());
                        }
                    }
                }
            }
        } else if (TRANSFERTYPE.ASLTOARRAY == transfertype) {
            String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", transferDataSet.getColumnValues("parentstorageunitid", ";"), null, null);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select childsu.storageunitid, childsu.storageunitlabel, parentsu.linksdcid, parentsu.linkkeyid1 from storageunit childsu, storageunit parentsu where parentsu.storageunitid = childsu.parentid and childsu.storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
            HashMap<String, String> detailMap = new HashMap<String, String>();
            for (int i = 0; i < transferDataSet.size(); ++i) {
                String parentstorageunitid = transferDataSet.getString(i, "parentstorageunitid");
                String gridid = transferDataSet.getString(i, "gridid");
                String sampleid = transferDataSet.getString(i, "sampleid");
                String childsampleid = transferDataSet.getString(i, "childsampleid");
                String labelrow = transferDataSet.getString(i, "labelrow");
                String labelcol = transferDataSet.getString(i, "labelcol");
                String childqty = transferDataSet.getValue(i, "childqty", "0");
                String childqtyunits = transferDataSet.getValue(i, "childqtyunits");
                double diluentVolume = transferDataSet.getDouble(i, "__trackitem_diluentvolume");
                String diluentVolumeUnits = transferDataSet.getString(i, "__trackitem_diluentvolumeunits", "");
                double childqtyN = this.m18NUtil.parseBigDecimal(childqty).doubleValue();
                String sourcedecrementedvolume = String.valueOf(childqtyN - (diluentVolume > 0.0 ? diluentVolume : 0.0));
                String sourcedecrementedvolumeunits = childqtyunits;
                int row = ds.findRow("storageunitid", parentstorageunitid);
                String sourcestorageunitsdc = ds.getString(row, "linksdcid", "");
                String sourcestorageunitkey = ds.getString(row, "linkkeyid1", "");
                String childstorageunitlabel = ds.getString(row, "storageunitlabel", "");
                String transferExecutionDetailID = "";
                String key = sourcestorageunitsdc + sourcestorageunitkey + gridid;
                if (detailMap.containsKey(key)) {
                    transferExecutionDetailID = (String)detailMap.get(key);
                } else {
                    int detailRow = detailDS.addRow();
                    transferExecutionDetailID = transferExecutionID + '-' + detailDS.size();
                    detailDS.setString(detailRow, "transferexecutionid", transferExecutionID);
                    detailDS.setString(detailRow, "transferexecutiondetailid", transferExecutionDetailID);
                    detailDS.setString(detailRow, "sourcesdcid", sourcestorageunitsdc);
                    detailDS.setString(detailRow, "sourcekeyid1", sourcestorageunitkey);
                    detailDS.setString(detailRow, "targetsdcid", SDC_ARRAY);
                    detailDS.setString(detailRow, "targetkeyid1", gridid);
                    detailDS.setNumber(detailRow, "usersequence", detailDS.size());
                    detailMap.put(key, transferExecutionDetailID);
                }
                int detailItemRow = detailItemDS.addRow();
                detailItemDS.setString(detailItemRow, "transferexecutionid", transferExecutionID);
                detailItemDS.setString(detailItemRow, "transferexecutiondetailid", transferExecutionDetailID);
                detailItemDS.setString(detailItemRow, "transferexecutiondetailitemid", transferExecutionDetailID + '-' + detailItemDS.size());
                detailItemDS.setString(detailItemRow, "sourcepositionid", parentstorageunitid);
                detailItemDS.setString(detailItemRow, "sourcepositionlabel", childstorageunitlabel);
                detailItemDS.setString(detailItemRow, "targetpositionid", transferDataSet.getString(i, "arrayitemid"));
                detailItemDS.setString(detailItemRow, "targetpositionlabel", labelcol + labelrow);
                detailItemDS.setString(detailItemRow, "sourcesdcid", "Sample");
                detailItemDS.setString(detailItemRow, "sourcekeyid1", sampleid);
                detailItemDS.setString(detailItemRow, "targetitemsdcid", "Sample");
                detailItemDS.setString(detailItemRow, "targetitemkeyid1", childsampleid);
                detailItemDS.setNumber(detailItemRow, "sourcedecrementedvolume", this.convertToBigDecimal(sourcedecrementedvolume));
                detailItemDS.setString(detailItemRow, "sourcedecrementedvolumeunits", sourcedecrementedvolumeunits);
                if (diluentVolume >= 0.0) {
                    detailItemDS.setNumber(detailItemRow, "diluentvolume", this.convertToBigDecimal(diluentVolume));
                    detailItemDS.setString(detailItemRow, "diluentvolumeunits", diluentVolumeUnits);
                }
                detailItemDS.setNumber(detailItemRow, "usersequence", detailItemDS.size());
            }
        } else if (TRANSFERTYPE.ARRAYTOASL == transfertype) {
            HashMap<String, String> detailMap = new HashMap<String, String>();
            if (transferDataSet.size() > 0) {
                int i;
                HashMap<String, String> boxidMap = new HashMap<String, String>();
                String[] targetboxstorageunitids = StringUtil.split(properties.getProperty("newsuid"), ";");
                String[] targetboxids = StringUtil.split(properties.getProperty("newboxid"), ";");
                for (i = 0; i < targetboxstorageunitids.length; ++i) {
                    boxidMap.put(targetboxstorageunitids[i], targetboxids[i]);
                }
                for (i = 0; i < transferDataSet.size(); ++i) {
                    String sourcearrayid = transferDataSet.getString(i, "sourcearrayid");
                    String sourcearrayitemid = transferDataSet.getString(i, "sourcearrayitemid");
                    String sourcesampleid = transferDataSet.getString(i, "sourcesampleid");
                    String sourcelabel = transferDataSet.getString(i, "sourcelabel");
                    String targetstorageunitid = transferDataSet.getString(i, "targetstorageunitid");
                    String targetboxid = (String)boxidMap.get(transferDataSet.getString(i, "targetboxstorageunitid"));
                    String childsampleid = transferDataSet.getString(i, "childsampleid");
                    String targetlabel = transferDataSet.getString(i, "targetlabel");
                    String childqty = transferDataSet.getValue(i, "childqty", "0");
                    String childqtyunits = transferDataSet.getValue(i, "childqtyunits");
                    double diluentVolume = transferDataSet.getDouble(i, "__trackitem_diluentvolume");
                    String diluentVolumeUnits = transferDataSet.getString(i, "__trackitem_diluentvolumeunits", "");
                    double childqtyN = this.m18NUtil.parseBigDecimal(childqty).doubleValue();
                    String sourcedecrementedvolume = String.valueOf(childqtyN - (diluentVolume > 0.0 ? diluentVolume : 0.0));
                    String sourcedecrementedvolumeunits = childqtyunits;
                    String transferExecutionDetailID = "";
                    String key = sourcearrayid + targetboxid;
                    if (detailMap.containsKey(key)) {
                        transferExecutionDetailID = (String)detailMap.get(key);
                    } else {
                        int detailRow = detailDS.addRow();
                        transferExecutionDetailID = transferExecutionID + '-' + detailDS.size();
                        detailDS.setString(detailRow, "transferexecutionid", transferExecutionID);
                        detailDS.setString(detailRow, "transferexecutiondetailid", transferExecutionDetailID);
                        detailDS.setString(detailRow, "sourcesdcid", SDC_ARRAY);
                        detailDS.setString(detailRow, "sourcekeyid1", sourcearrayid);
                        detailDS.setString(detailRow, "targetsdcid", "LV_Box");
                        detailDS.setString(detailRow, "targetkeyid1", targetboxid);
                        detailDS.setNumber(detailRow, "usersequence", detailDS.size());
                        detailMap.put(key, transferExecutionDetailID);
                    }
                    int detailItemRow = detailItemDS.addRow();
                    detailItemDS.setString(detailItemRow, "transferexecutionid", transferExecutionID);
                    detailItemDS.setString(detailItemRow, "transferexecutiondetailid", transferExecutionDetailID);
                    detailItemDS.setString(detailItemRow, "transferexecutiondetailitemid", transferExecutionDetailID + '-' + detailItemDS.size());
                    detailItemDS.setString(detailItemRow, "sourcepositionid", sourcearrayitemid);
                    detailItemDS.setString(detailItemRow, "sourcepositionlabel", sourcelabel);
                    detailItemDS.setString(detailItemRow, "targetpositionid", targetstorageunitid);
                    detailItemDS.setString(detailItemRow, "targetpositionlabel", targetlabel);
                    detailItemDS.setString(detailItemRow, "sourcesdcid", "Sample");
                    detailItemDS.setString(detailItemRow, "sourcekeyid1", sourcesampleid);
                    detailItemDS.setString(detailItemRow, "targetitemsdcid", "Sample");
                    detailItemDS.setString(detailItemRow, "targetitemkeyid1", childsampleid);
                    detailItemDS.setNumber(detailItemRow, "sourcedecrementedvolume", this.convertToBigDecimal(sourcedecrementedvolume));
                    detailItemDS.setString(detailItemRow, "sourcedecrementedvolumeunits", sourcedecrementedvolumeunits);
                    if (diluentVolume >= 0.0) {
                        detailItemDS.setNumber(detailItemRow, "diluentvolume", this.convertToBigDecimal(diluentVolume));
                        detailItemDS.setString(detailItemRow, "diluentvolumeunits", diluentVolumeUnits);
                    }
                    detailItemDS.setNumber(detailItemRow, "usersequence", detailItemDS.size());
                    if (!OpalUtil.isNotEmpty(childsampleid)) continue;
                    childSampleSet.add(childsampleid);
                }
            }
        }
        if (OpalUtil.isNotEmpty(detailDS)) {
            int i;
            DataSet ds = transferExecutionMap.get(EXECUTIONMAP_DETAIL);
            for (i = 0; i < detailDS.size(); ++i) {
                ds.copyRow(detailDS, i, 1);
            }
            if (OpalUtil.isNotEmpty(detailItemDS)) {
                for (i = 0; i < detailItemDS.size(); ++i) {
                    detailItemDS.setNumber(i, "sourcedecrementedvolume", this.convertToBigDecimal(detailItemDS.getDouble(i, "sourcedecrementedvolume", 0.0)));
                }
                DataSet itemds = transferExecutionMap.get(EXECUTIONMAP_DETAILITEM);
                for (int i2 = 0; i2 < detailItemDS.size(); ++i2) {
                    itemds.copyRow(detailItemDS, i2, 1);
                }
            }
        }
        if (OpalUtil.isNotEmpty(dsInstruments = this.getQueryProcessor().getPreparedSqlDataSet("select instrumenttypeid, instrumentmodelid, instrumentid, instrumentcount recommendedcount, usersequence from arraytransfermethodinstrument  where arraytransfermethodid = ? and arraytransfermethodversionid = ? order by usersequence", (Object[])new String[]{transferMethodId, transferMethodVersionId}))) {
            dsInstruments.setString(-1, "transferexecutionid", transferExecutionID);
            DataSet instrumentds = transferExecutionMap.get(EXECUTIONMAP_INSTRUMENT);
            for (int i = 0; i < dsInstruments.size(); ++i) {
                instrumentds.copyRow(dsInstruments, i, 1);
            }
        }
        if (OpalUtil.isNotEmpty(dsReagentTypes = this.getQueryProcessor().getPreparedSqlDataSet("select reagenttypeid, reagenttypeversionid,reagenttypeid originalreagenttypeid, reagenttypeversionid originalreagenttypeversionid, amount recommendedamount,amount useamountadjusted, amountunits recommendedamountunits, usersequence from arraytransfermethodreagenttype where arraytransfermethodid = ? and arraytransfermethodversionid = ? order by usersequence", (Object[])new String[]{transferMethodId, transferMethodVersionId}))) {
            dsReagentTypes.setString(-1, "transferexecutionid", transferExecutionID);
            DataSet reagentds = transferExecutionMap.get(EXECUTIONMAP_REAGENT);
            for (int i = 0; i < dsReagentTypes.size(); ++i) {
                reagentds.copyRow(dsReagentTypes, i, 1);
            }
        }
        DataSet workitem = transferExecutionMap.get(EXECUTIONMAP_WORKITEM);
        if ((TRANSFERTYPE.ASLTOASL == transfertype || TRANSFERTYPE.ARRAYTOASL == transfertype) && OpalUtil.isNotEmpty(dsWorkitems = this.getQueryProcessor().getPreparedSqlDataSet("select workitemid, workitemversionid, applyonaddflag, s_sampletypeid from sdiworkitem where sdcid = 'LV_ArrayTransferMethod' and keyid1 = ? and keyid2 = ? order by usersequence", (Object[])new String[]{transferMethodId, transferMethodVersionId}))) {
            DataSet sampleDS = null;
            if (OpalUtil.isNotEmpty(childSampleSet)) {
                safeSQL.reset();
                sampleDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, sampletypeid from s_sample where s_sampleid in (" + safeSQL.addIn(childSampleSet) + ")", safeSQL.getValues());
            } else {
                HashSet<String> boxSet = new HashSet<String>();
                for (int i = 0; i < detailDS.size(); ++i) {
                    if (!"LV_Box".equals(detailDS.getString(i, "targetsdcid"))) continue;
                    boxSet.add(detailDS.getString(i, "targetkeyid1"));
                }
                if (OpalUtil.isNotEmpty(boxSet)) {
                    safeSQL.reset();
                    sampleDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.s_sampleid, s_sample.sampletypeid from s_sample, trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid and trackitem.currentstorageunitid in (select storageunit.storageunitid from storageunit where storageunit.parentid in (select su.storageunitid from storageunit su where su.linksdcid = 'LV_Box' and su.linkkeyid1 in (" + safeSQL.addIn(boxSet) + ")))", safeSQL.getValues());
                }
            }
            if (OpalUtil.isNotEmpty(sampleDS)) {
                for (int i = 0; i < dsWorkitems.size(); ++i) {
                    String childSamples = "";
                    String workitemid = dsWorkitems.getString(i, "workitemid", "");
                    String workitemversionid = dsWorkitems.getString(i, "workitemversionid", "");
                    String applyonaddflag = dsWorkitems.getString(i, "applyonaddflag", "N");
                    String sampletypeid = dsWorkitems.getString(i, "s_sampletypeid", "");
                    if (!sampletypeid.isEmpty()) {
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("sampletypeid", sampletypeid);
                        DataSet d = sampleDS.getFilteredDataSet(filterMap);
                        if (d != null && d.size() > 0) {
                            childSamples = d.getColumnValues("s_sampleid", ";");
                        }
                    } else {
                        childSamples = sampleDS.getColumnValues("s_sampleid", ";");
                    }
                    if (!OpalUtil.isNotEmpty(childSamples) || "null".equals(childSamples)) continue;
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", childSamples);
                    props.setProperty("workitemid", workitemid);
                    props.setProperty("workitemversionid", workitemversionid);
                    props.setProperty("applyworkitem", applyonaddflag);
                    this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                }
            }
            for (int i = 0; i < dsWorkitems.size(); ++i) {
                int row = workitem.addRow();
                workitem.setString(row, "transferexecutionid", transferExecutionID);
                workitem.setString(row, "workitemid", dsWorkitems.getString(i, "workitemid"));
                workitem.setString(row, "workitemversionid", dsWorkitems.getString(i, "workitemversionid"));
                workitem.setString(row, "applyonaddflag", dsWorkitems.getString(i, "applyonaddflag"));
                workitem.setString(row, "s_sampletypeid", dsWorkitems.getString(i, "s_sampletypeid"));
            }
        }
        Trace.logDebug("Transfer Execution took " + (System.currentTimeMillis() - start) + " ms.");
    }

    private void doArrayToArrayTransfer(String mode, SourceInputs sourceInputs, TargetInputs targetInputs, AuditInputs auditInputs, DataSet transferMethodDS, String createChild, String childSampleTypeId, DataSet arrayItemContentDS, Map<String, Object> targetArrayMap) throws SapphireException {
        String[] targetArrayList;
        String[] arrayidlist;
        String arrayid = sourceInputs.sourceid;
        for (String anArrayidlist : arrayidlist = StringUtil.split(arrayid, ";")) {
            String arrayStatus = ArrayUtil.getArrayStatus(this.getQueryProcessor(), anArrayidlist);
            if (!(arrayStatus.equals("Loaded") || arrayStatus.equals("SentToInstrument") || arrayStatus.equals("DataEntered") || arrayStatus.equals("Reviewed"))) {
                throw new SapphireException("ERROR: Invalid status. " + this.getTranslationProcessor().translate("Cannot perform transfer on array: ") + anArrayidlist);
            }
            this.logger.info("Array Status valid for transfer operation");
        }
        String contentTransferRule = transferMethodDS.getValue(0, "contenttransferrule", "");
        HashMap<String, HashMap<String, String>> zoneContentRuleMap = this.parseContentTransferRule(contentTransferRule);
        String transferMap = transferMethodDS.getValue(0, "transfermap", "");
        if (transferMap.length() == 0) {
            throw new SapphireException("Transfer Map not defined in the Transfer method");
        }
        String[] transferMapStrArr = StringUtil.split(transferMap, "|");
        ArrayList<TargetArrayMap> targetArrayMapArrayList = this.parseTransferMapString(transferMapStrArr, StringUtil.split(sourceInputs.sourceid, ";").length, "" + sourceInputs.sourceindex, this.getTranslationProcessor());
        DataSet targetZoneDS = new DataSet();
        targetZoneDS.addColumn("arrayid", 0);
        targetZoneDS.addColumn("zone", 0);
        targetZoneDS.addColumn("color", 0);
        targetZoneDS.addColumn("keyid1", 0);
        ArrayList<String> targetZoneItemAL = this.populateTargetZoneDataSet(targetArrayMapArrayList, sourceInputs.sourceid, targetInputs.targetid, targetZoneDS, mode, sourceInputs.sourceindex, zoneContentRuleMap, sourceInputs.validatesourcevolume);
        HashMap<String, String> targetArrayZoneMap = this.populateTargetZones(targetZoneDS, auditInputs);
        DataSet targetZoneItemDS = new DataSet();
        targetZoneItemDS.addColumn("arrayitemid", 0);
        targetZoneItemDS.addColumn("arrayzoneid", 0);
        targetZoneItemDS.addColumn("zone", 0);
        this.populateArrayItemArrayZoneDataSet(targetArrayMapArrayList, targetArrayZoneMap, targetZoneItemDS, targetZoneItemAL);
        this.populateAIAZTable(targetZoneItemDS);
        DataSet targetAIContentDS = new DataSet();
        targetAIContentDS.addColumn("arrayitemid", 0);
        targetAIContentDS.addColumn("contentsdcid", 0);
        targetAIContentDS.addColumn("contentkeyid1", 0);
        targetAIContentDS.addColumn("volume", 0);
        targetAIContentDS.addColumn("volumeunits", 0);
        targetAIContentDS.addColumn("concentration", 0);
        targetAIContentDS.addColumn("concentrationunits", 0);
        targetAIContentDS.addColumn("parentarrayitemid", 0);
        targetAIContentDS.addColumn("contentitem", 0);
        targetAIContentDS.addColumn("contenttype", 0);
        targetAIContentDS.addColumn("contentlabel", 0);
        targetAIContentDS.addColumn("arrayid", 0);
        targetAIContentDS.addColumn("reagenttypeid", 0);
        targetAIContentDS.addColumn("diluentvolume", 1);
        targetAIContentDS.addColumn("diluentvolumeunits", 0);
        targetAIContentDS.addColumn("sourcearrayzoneid", 0);
        DataSet inventoryDS = new DataSet();
        inventoryDS.addColumn("contentkeyid1", 0);
        inventoryDS.addColumn("sourceconcentration", 1);
        inventoryDS.addColumn("sourceconcentrationunit", 0);
        inventoryDS.addColumn("quantity", 0);
        inventoryDS.addColumn("targetarrayitemid", 0);
        inventoryDS.addColumn("sourcevolumeunit", 0);
        HashMap sourceArrayItemContentHM = this.findSourceArrayItemContent(sourceInputs.sourceid);
        this.populateArrayItemContentDSForTarget(sourceInputs, targetInputs, auditInputs, targetAIContentDS, sourceArrayItemContentHM, targetArrayMapArrayList, inventoryDS, mode, zoneContentRuleMap, createChild, childSampleTypeId);
        this.populateContentLabels(this.getQueryProcessor(), targetAIContentDS);
        if (OpalUtil.isNotEmpty(targetAIContentDS)) {
            for (int i = 0; i < targetAIContentDS.size(); ++i) {
                arrayItemContentDS.copyRow(targetAIContentDS, i, 1);
            }
        }
        for (String s : targetArrayList = StringUtil.split(targetInputs.targetid, ";")) {
            targetArrayMap.put(s, targetArrayMapArrayList);
            targetArrayMap.put(s + "_inventoryDS", inventoryDS);
        }
    }

    private void updateTargetArrayStatus(TargetInputs targetInputs, AuditInputs auditInputs) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC_ARRAY);
        props.setProperty("keyid1", targetInputs.targetid);
        props.setProperty("arraystatus", "Loaded");
        props.setProperty("auditsignedflag", auditInputs.auditsignedflag);
        props.setProperty("auditactivity", auditInputs.auditactivity);
        props.setProperty("auditreason", auditInputs.auditreason);
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
    }

    private void populateContentLabels(QueryProcessor qp, DataSet targetAIContentDS) {
        targetAIContentDS.sort("contenttype");
        ArrayList<DataSet> targetAIContentAL = targetAIContentDS.getGroupedDataSets("contenttype");
        for (DataSet contentDS : targetAIContentAL) {
            String contentType = contentDS.getValue(0, "contenttype", "");
            String contentLabels = "";
            if (contentType.isEmpty()) continue;
            switch (contentType) {
                case "Sample": {
                    contentLabels = ArrayUtil.getContentLabels(qp, this.getDAMProcessor(), "", contentType, contentDS.getColumnValues("contentkeyid1", ";"), "");
                    break;
                }
                case "LV_ReagentLot": {
                    for (int j = 0; j < contentDS.getRowCount(); ++j) {
                        if (!contentDS.getString(j, "reagenttypeid", "").isEmpty()) continue;
                        String reagenttypeid = "";
                        DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet("select reagenttypeid from reagentlot where reagentlotid = ( select linkkeyid1 FROM trackitem where trackitemid = ? )", (Object[])new String[]{contentDS.getString(j, "contentkeyid1")});
                        if (ret != null && ret.getRowCount() > 0) {
                            reagenttypeid = ret.getValue(0, "reagenttypeid");
                        }
                        contentDS.setString(j, "reagenttypeid", reagenttypeid);
                        contentDS.setString(j, "reagenttypeversionid", "1");
                    }
                    contentLabels = ArrayUtil.getContentLabels(qp, this.getDAMProcessor(), "", contentType, contentDS.getColumnValues("contentkeyid1", ";"), contentDS.getColumnValues("reagenttypeid", ";"));
                    break;
                }
                case "LV_Treatment": {
                    contentLabels = ArrayUtil.getContentLabels(qp, this.getDAMProcessor(), "", contentType, contentDS.getColumnValues("contentkeyid1", ";"), "");
                    break;
                }
                case "LV_ArrayItem": {
                    contentLabels = ArrayUtil.getContentLabels(qp, this.getDAMProcessor(), contentDS.getColumnValues("arrayid", ";"), contentType, contentDS.getColumnValues("contentkeyid1", ";"), "");
                    break;
                }
            }
            contentDS.addColumnValues("contentlabel", 0, contentLabels, ";");
        }
    }

    private void addASLContentItems(DataSet targetContentInfoAll, AuditInputs auditInputs, boolean createChildSamples) throws SapphireException {
        if (OpalUtil.isNotEmpty(targetContentInfoAll)) {
            ArrayList<DataSet> perZone = targetContentInfoAll.getGroupedDataSets("arraylayoutzone");
            for (DataSet dataSet : perZone) {
                PropertyList propertyList = new PropertyList();
                String currentZone = dataSet.getString(0, "arraylayoutzone", "");
                propertyList.setProperty("arrayid", dataSet.getValue(0, "gridid"));
                propertyList.setProperty("arrayitemid", dataSet.getColumnValues("arrayitemid", ";"));
                propertyList.setProperty("contentsdcid", PropertyUtil.repeat("Sample", dataSet.size()));
                propertyList.setProperty("contenttype", PropertyUtil.repeat("Sample", dataSet.size()));
                propertyList.setProperty("contentitem", PropertyUtil.repeat("Unknown", dataSet.size()));
                if (createChildSamples) {
                    propertyList.setProperty("contentkeyid1", dataSet.getColumnValues("childsampleid", ";"));
                    propertyList.setProperty("volume", dataSet.getColumnValues("childqty", ";"));
                    propertyList.setProperty("volumeunits", dataSet.getColumnValues("childqtyunits", ";"));
                    propertyList.setProperty("concentration", dataSet.getColumnValues("childconc", ";"));
                    propertyList.setProperty("concentrationunits", dataSet.getColumnValues("childconcunits", ";"));
                } else {
                    propertyList.setProperty("contentkeyid1", dataSet.getColumnValues("sampleid", ";"));
                    propertyList.setProperty("volume", dataSet.getColumnValues("volume", ";"));
                    propertyList.setProperty("volumeunits", dataSet.getColumnValues("volumeunits", ";"));
                    propertyList.setProperty("concentration", dataSet.getColumnValues("concentration", ";"));
                    propertyList.setProperty("concentrationunits", dataSet.getColumnValues("concentrationunits", ";"));
                }
                propertyList.setProperty("parentstorageunitid", dataSet.getColumnValues("parentstorageunitid", ";"));
                propertyList.setProperty("diluentvolume", dataSet.getColumnValues("diluentvolume", ";"));
                propertyList.setProperty("diluentvolumeunits", dataSet.getColumnValues("diluentvolumeunits", ";"));
                propertyList.setProperty("zone", currentZone);
                propertyList.setProperty("auditsignedflag", auditInputs.auditsignedflag);
                propertyList.setProperty("auditactivity", auditInputs.auditactivity);
                propertyList.setProperty("auditreason", auditInputs.auditreason);
                this.getActionProcessor().processAction("AddArrayContent", "1", propertyList);
            }
        }
    }

    private void incrementTargetArrayItemVolumes(DataSet targetContentInfo, AuditInputs auditInputs) throws SapphireException {
        if (OpalUtil.isNotEmpty(targetContentInfo)) {
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("arrayid", targetContentInfo.getValue(0, "gridid"));
            propertyList.setProperty("arrayitemid", targetContentInfo.getColumnValues("arrayitemid", ";"));
            propertyList.setProperty("totalvol", targetContentInfo.getColumnValues("totalvolumeincrement", ";"));
            propertyList.setProperty("totalvolunits", targetContentInfo.getColumnValues("volumeunits", ";"));
            propertyList.setProperty("totalconc", targetContentInfo.getColumnValues("concentration", ";"));
            propertyList.setProperty("totalconcunits", targetContentInfo.getColumnValues("concentrationunits", ";"));
            propertyList.setProperty("auditsignedflag", auditInputs.auditsignedflag);
            propertyList.setProperty("auditactivity", auditInputs.auditactivity);
            propertyList.setProperty("auditreason", auditInputs.auditreason);
            this.getActionProcessor().processAction("EditArrayItem", "1", propertyList);
        }
    }

    private void adjustSourceInventory(String sourcevolumetodecrement, String sourcevolumetodecrementunits, String quantityhandlingflag, boolean validatesourcevolume, String targetvolume, String targetvolumeunits, String targetconc, String targetconcunits, DataSet inventoryDS) throws SapphireException {
        ArrayVolumeHandlingUtil arrayVolumeHandlingUtil = new ArrayVolumeHandlingUtil(this.m18NUtil);
        inventoryDS.addColumnValues("repeatcount", 1, "1", ";");
        inventoryDS.addColumnValues("dilutionfactor", 1, "1", ";");
        if (targetconc != null && !targetconc.isEmpty()) {
            inventoryDS.setNumber(0, "targetconcentration", this.m18NUtil.parseBigDecimal(targetconc));
            inventoryDS.setString(0, "targetconcentrationunit", targetconcunits);
        }
        if (targetvolume != null && !targetvolume.isEmpty()) {
            inventoryDS.setNumber(0, "targetvolume", this.m18NUtil.parseBigDecimal(targetvolume));
            inventoryDS.setString(0, "volumeunits", targetvolumeunits);
        }
        inventoryDS.addColumnValues("volumehandlingflag", 0, quantityhandlingflag, ";", "D");
        inventoryDS.addColumnValues("contentsdcid", 0, SDC_ARRAYITEM, ";");
        if (sourcevolumetodecrement != null && !sourcevolumetodecrement.isEmpty()) {
            inventoryDS.setNumber(0, "sourcevolume", this.m18NUtil.parseBigDecimal(sourcevolumetodecrement));
            inventoryDS.setString(0, "sourcevolumeunit", sourcevolumetodecrementunits);
        }
        inventoryDS.padColumns();
        arrayVolumeHandlingUtil.adjustSourceInv(inventoryDS, this.getActionProcessor(), this.getQueryProcessor(), validatesourcevolume, this.connectionInfo);
    }

    private void populateArrayItemContentDSForTarget(SourceInputs sourceInputs, TargetInputs targetInputs, AuditInputs auditInputs, DataSet targetAIContentDS, HashMap sourceArrayItemContentHM, ArrayList<TargetArrayMap> targetArrayMapArrayList, DataSet inventoryDS, String mode, HashMap<String, HashMap<String, String>> zoneContentRuleMap, String createChild, String sampleTypeId) throws SapphireException {
        ArrayList<String> unknownSamplesList = new ArrayList<String>();
        ArrayList<Integer> childCount = new ArrayList<Integer>();
        for (TargetArrayMap targetArrayMap : targetArrayMapArrayList) {
            String sourceArrayItemId;
            if (targetArrayMap.targetarrayid == null) continue;
            Map<String, String> targetZoneMap = this.getTargetZones(targetArrayMap.targetarrayid);
            if (targetArrayMap.sourcearray < sourceInputs.sourceindex || (sourceArrayItemId = targetArrayMap.sourcearrayitemid) == null || sourceArrayItemId.isEmpty()) continue;
            int inventoryRow = inventoryDS.addRow();
            inventoryDS.setValue(inventoryRow, "contentkeyid1", targetArrayMap.sourcearrayitemid);
            inventoryDS.setValue(inventoryRow, "sourceconcentration", targetArrayMap.sourceconcentration);
            inventoryDS.setValue(inventoryRow, "sourceconcentrationunit", targetArrayMap.sourceconcentrationunits);
            inventoryDS.setValue(inventoryRow, "targetarrayitemid", targetArrayMap.targetarrayitemid);
            ArrayList sourceContentAL = (ArrayList)sourceArrayItemContentHM.get(sourceArrayItemId);
            String targetConcentration = targetInputs.targetconc;
            String targetConcentrationUnit = targetInputs.targetconcunits;
            if (targetConcentration == null || targetConcentration.trim().isEmpty()) {
                targetConcentration = targetArrayMap.sourceconcentration == null ? "" : targetArrayMap.sourceconcentration;
                String string = targetConcentrationUnit = targetArrayMap.sourceconcentrationunits == null ? "" : targetArrayMap.sourceconcentrationunits;
            }
            if (sourceInputs.quantityhandlingflag.equals("S") && targetArrayMap.sourcetotalquantity != null) {
                targetInputs.targetvolume = targetArrayMap.sourcetotalquantity;
                targetInputs.targetvolumeunits = targetArrayMap.sourcetotalquantityunits;
            }
            String targetAIId = "";
            targetAIId = mode.equals("edit") ? targetArrayMap.targetarrayid + "_" + targetArrayMap.targetrowindex + "_" + targetArrayMap.targetcolindex : targetArrayMap.targetarrayitemid;
            if (!OpalUtil.isNotEmpty(sourceContentAL)) continue;
            for (int j = 0; j < sourceContentAL.size(); ++j) {
                ArrayItemContent sourceContent = (ArrayItemContent)sourceContentAL.get(j);
                String contentsdcid = sourceContent.contentSdcId;
                String cItem = sourceContent.contentItem;
                String cType = sourceContent.contentType;
                String zone = sourceContent.zone;
                if (contentsdcid == null) continue;
                for (int z = 0; z < 1; ++z) {
                    int position;
                    if (!sourceContent.contentKeyId1.isEmpty() && z == 0 && j == 0) {
                        this.addContentToTargetArray(targetAIContentDS, targetAIId, SDC_ARRAYITEM, sourceArrayItemId, targetInputs.targetvolume, targetInputs.targetvolumeunits, targetConcentration, targetConcentrationUnit, "", "Transfer", SDC_ARRAYITEM, targetArrayMap.targetarrayid, "", targetArrayMap.sourceconcentration, targetArrayMap.sourceconcentrationunits, targetZoneMap.get("(FullArray)"));
                    }
                    if (!cItem.equals("Transfer") && this.copySourceContent(zone, zoneContentRuleMap, cItem).get(1).booleanValue()) {
                        this.addContentToTargetArray(targetAIContentDS, targetAIId, contentsdcid, sourceContent.contentKeyId1, "", "", "", "", sourceArrayItemId, cItem, cType, targetArrayMap.targetarrayid, sourceContent.reagentTypeId, targetArrayMap.sourceconcentration, targetArrayMap.sourceconcentrationunits, targetZoneMap.get(zone));
                    }
                    if (createChild.equals("N") || !cItem.equals("Unknown")) continue;
                    if (unknownSamplesList.contains(sourceContent.contentKeyId1)) {
                        position = unknownSamplesList.indexOf(sourceContent.contentKeyId1);
                        int currentcount = (Integer)childCount.get(position);
                        if (createChild.equals("S")) continue;
                        childCount.set(position, currentcount + 1);
                        continue;
                    }
                    position = unknownSamplesList.size();
                    unknownSamplesList.add(position, sourceContent.contentKeyId1);
                    childCount.add(position, 1);
                }
            }
        }
        if (OpalUtil.isNotEmpty(unknownSamplesList)) {
            HashMap<String, String> sourceMap = this.multiSampleChild(unknownSamplesList, childCount, sampleTypeId, auditInputs);
            for (int i = 0; i < targetAIContentDS.size(); ++i) {
                String parentsampleid;
                if (!targetAIContentDS.getValue(i, "contentsdcid", "").equals("Sample") || !sourceMap.containsKey(parentsampleid = targetAIContentDS.getValue(i, "contentkeyid1", ""))) continue;
                String childsamplelist = sourceMap.get(parentsampleid);
                String[] childsampleids = StringUtil.split(childsamplelist, ";");
                targetAIContentDS.setString(i, "contentkeyid1", childsampleids[0]);
                if (childsampleids.length <= 1) continue;
                StringBuilder newchildlist = new StringBuilder();
                for (int c = 1; c < childsampleids.length; ++c) {
                    if (newchildlist.length() > 0) {
                        newchildlist.append(";");
                    }
                    newchildlist.append(childsampleids[c]);
                }
                sourceMap.put(parentsampleid, newchildlist.toString());
            }
        }
    }

    private void addContentToTargetArray(DataSet targetAIContentDS, String arrayitemid, String contentsdcid, String contentkeyid1, String quantity, String quantityunits, String concentration, String concentrationunits, String parentarrayitemid, String contentItem, String contentType, String arrayId, String reagentTypeID, String sourceAIConc, String sourceAIConcUnits, String zoneId) throws SapphireException {
        boolean addcontent = true;
        if (!"Sample".equals(contentsdcid)) {
            String key = arrayitemid + "_" + contentsdcid + "_" + contentkeyid1;
            if (this.arrayItemContentSet.contains(key)) {
                addcontent = false;
            } else {
                this.arrayItemContentSet.add(key);
            }
        }
        if (addcontent) {
            int row = targetAIContentDS.addRow();
            targetAIContentDS.setValue(row, "arrayitemid", arrayitemid);
            targetAIContentDS.setValue(row, "contentsdcid", contentsdcid);
            targetAIContentDS.setValue(row, "contentkeyid1", contentkeyid1);
            targetAIContentDS.setValue(row, "volume", quantity);
            targetAIContentDS.setValue(row, "volumeunits", quantityunits);
            targetAIContentDS.setValue(row, "concentration", concentration);
            targetAIContentDS.setValue(row, "concentrationunits", concentrationunits);
            targetAIContentDS.setValue(row, "contenttype", contentType);
            targetAIContentDS.setValue(row, "contentitem", contentItem);
            targetAIContentDS.setValue(row, "reagenttypeid", reagentTypeID);
            targetAIContentDS.setValue(row, "arrayid", arrayId);
            targetAIContentDS.setValue(row, "sourcearrayzoneid", zoneId);
            if (parentarrayitemid != null && !parentarrayitemid.isEmpty()) {
                targetAIContentDS.setValue(row, "parentarrayitemid", parentarrayitemid);
            }
            if (sourceAIConc != null && !sourceAIConc.isEmpty() && concentration != null && !concentration.isEmpty()) {
                double targetconc;
                double concN = this.m18NUtil.parseBigDecimal(concentration).doubleValue();
                double sourceAIConcN = this.m18NUtil.parseBigDecimal(sourceAIConc).doubleValue();
                double quantityN = this.m18NUtil.parseBigDecimal(quantity).doubleValue();
                if (concentrationunits != null && concentrationunits.equals(sourceAIConcUnits) && concN <= sourceAIConcN) {
                    double dilutentVol = quantityN * (1.0 - concN / sourceAIConcN);
                    targetAIContentDS.setNumber(row, "diluentvolume", String.valueOf(!Double.isNaN(dilutentVol) ? this.convertToBigDecimal(dilutentVol) : Integer.valueOf(0)));
                    targetAIContentDS.setValue(row, "diluentvolumeunits", quantityunits);
                } else if (concentrationunits != null && !concentrationunits.equals(sourceAIConcUnits) && (targetconc = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(concentration), concentrationunits, sourceAIConcUnits).doubleValue()) < sourceAIConcN) {
                    double dilutentVol = quantityN * (1.0 - concN / sourceAIConcN);
                    targetAIContentDS.setNumber(row, "diluentvolume", this.convertToBigDecimal(dilutentVol));
                    targetAIContentDS.setValue(row, "diluentvolumeunits", quantityunits);
                }
            } else {
                targetAIContentDS.setValue(targetAIContentDS.size() - 1, "diluentvolume", "");
            }
        }
    }

    private HashMap findSourceArrayItemContent(String sourceArray) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT distinct ai.xpos, ai.ypos, ai.arrayitemid, ai.arrayid, ai.itemlabel, aic.contentsdcid aicontentsdcid, aic.contentkeyid1 aicontentkeyid1, aic.contenttype aiccontenttype, aic.contentitem aiccontentitem, aic.sourcearrayzoneid aicarrayzone, ").append("azc.levelflag, azc.contentsdcid azcontentsdcid, azc.contentkeyid1 azccontentkeyid1, azc.contentitem azccontentitem , azc.contenttype azccontenttype, azc.arrayzoneid azcarrayzone, ").append("azc.reagenttypeid, azc.reagenttypeversionid, azc.treatmenttypeid ").append("FROM array a, arrayitem ai ").append(" left outer join arrayitemcontent aic on ai.arrayitemid = aic.arrayitemid ").append(" left outer join arrayitemarrayzone aiaz on ai.arrayitemid = aiaz.arrayitemid ").append(" left outer join arrayzone az on aiaz.arrayzoneid = az.arrayzoneid ").append(" left outer join arrayzonecontent azc on az.arrayzoneid = azc.arrayzoneid and (azc.contentkeyid1 is not null) ").append(" WHERE a.arrayid = ai.arrayid ").append(" AND az.arrayid = a.arrayid ").append(" AND a.arrayid IN (").append(safeSQL.addIn(sourceArray.replaceAll(";", "','"))).append(")");
        this.database.createPreparedResultSet("sourcearray", sql.toString(), safeSQL.getValues());
        HashMap<String, ArrayList> sourceMap = new HashMap<String, ArrayList>();
        while (this.database.getNext("sourcearray")) {
            String aicContentKeyId1 = this.database.getString("sourcearray", "aicontentkeyid1");
            String azcContentKeyId1 = this.database.getString("sourcearray", "azccontentkeyid1");
            if ((aicContentKeyId1 == null || aicContentKeyId1.length() <= 0) && (azcContentKeyId1 == null || azcContentKeyId1.length() <= 0)) continue;
            String key = this.database.getString("sourcearray", "arrayitemid");
            ArrayList sourceAL = new ArrayList();
            if (sourceMap.containsKey(key)) {
                sourceAL = (ArrayList)sourceMap.get(key);
            }
            Map<String, String> sourceZoneMap = this.getZoneMap(sourceArray);
            ArrayItemContent arrayItemContent = new ArrayItemContent();
            if (this.database.getString("sourcearray", "levelflag") != null && this.database.getString("sourcearray", "levelflag").equals("Z")) {
                arrayItemContent.contentSdcId = this.database.getString("sourcearray", "azcontentsdcid");
                arrayItemContent.contentKeyId1 = azcContentKeyId1;
                arrayItemContent.contentItem = this.database.getString("sourcearray", "azccontentitem");
                arrayItemContent.contentType = this.database.getString("sourcearray", "azccontenttype");
                arrayItemContent.zone = sourceZoneMap.get(this.database.getString("sourcearray", "azcarrayzone"));
            } else {
                arrayItemContent.contentSdcId = this.database.getString("sourcearray", "aicontentsdcid");
                arrayItemContent.contentKeyId1 = aicContentKeyId1;
                arrayItemContent.contentItem = this.database.getString("sourcearray", "aiccontentitem");
                arrayItemContent.contentType = this.database.getString("sourcearray", "aiccontenttype");
                arrayItemContent.zone = sourceZoneMap.get(this.database.getString("sourcearray", "aicarrayzone"));
            }
            arrayItemContent.reagentTypeId = this.database.getString("sourcearray", "reagenttypeid");
            arrayItemContent.reagentTypeVersionId = this.database.getString("sourcearray", "reagenttypeversionid");
            arrayItemContent.treatmentTypeId = this.database.getString("sourcearray", "treatmenttypeid");
            arrayItemContent.arrayId = this.database.getString("sourcearray", "arrayid");
            arrayItemContent.itemLabel = this.database.getString("sourcearray", "itemlabel");
            sourceAL.add(arrayItemContent);
            sourceMap.put(key, sourceAL);
        }
        this.database.closeResultSet("sourcearray");
        return sourceMap;
    }

    private void populateAIAZTable(DataSet targetZoneItemDS) throws SapphireException {
        DataSet arrayItemZoneInsertDS = new DataSet();
        DataSet dataSet = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_ArrayZone", "select arrayitemid, arrayzoneid from arrayitemarrayzone where arrayitemid in ([])", targetZoneItemDS.getColumnValues("arrayitemid", ";"));
        if (dataSet != null) {
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            Calendar now = DateTimeUtil.getNowCalendar();
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < targetZoneItemDS.size(); ++i) {
                String arrayzoneid = targetZoneItemDS.getValue(i, "arrayzoneid", "");
                String arrayitemid = targetZoneItemDS.getValue(i, "arrayitemid", "");
                if (arrayzoneid.isEmpty() || arrayitemid.isEmpty()) continue;
                filter.clear();
                filter.put("arrayzoneid", arrayzoneid);
                filter.put("arrayitemid", arrayitemid);
                if (dataSet.findRow(filter) != -1) continue;
                int z = arrayItemZoneInsertDS.addRow();
                arrayItemZoneInsertDS.setString(z, "arrayzoneid", targetZoneItemDS.getValue(i, "arrayzoneid"));
                arrayItemZoneInsertDS.setString(z, "arrayitemid", targetZoneItemDS.getValue(i, "arrayitemid"));
                arrayItemZoneInsertDS.setString(z, "createby", sysuserid);
                arrayItemZoneInsertDS.setDate(z, "createdt", now);
                arrayItemZoneInsertDS.setString(z, "createtool", "TransferArray");
                arrayItemZoneInsertDS.setString(z, "modby", sysuserid);
                arrayItemZoneInsertDS.setDate(z, "moddt", now);
                arrayItemZoneInsertDS.setString(z, "modtool", "TransferArray");
            }
        }
        if (OpalUtil.isNotEmpty(arrayItemZoneInsertDS)) {
            DataSetUtil.insert(this.database, arrayItemZoneInsertDS, "arrayitemarrayzone");
        }
    }

    private void populateArrayItemArrayZoneDataSet(ArrayList<TargetArrayMap> targetArrayMapArrayList, HashMap<String, String> targetZoneMap, DataSet targetZoneItemDS, ArrayList<String> targetZoneItemAL) {
        ArrayList<String> zoneItemAL = new ArrayList<String>();
        for (TargetArrayMap targetArrayMap : targetArrayMapArrayList) {
            if (targetArrayMap.targetarrayid == null || targetArrayMap.targetarrayzonename == null || targetArrayMap.targetarrayzonename.isEmpty()) continue;
            String[] zones = StringUtil.split(targetArrayMap.targetarrayzonename, ";");
            String targetArrayId = targetArrayMap.targetarrayid;
            for (String zone : zones) {
                String itemid;
                String zoneId;
                if (!targetZoneItemAL.isEmpty() && !targetZoneItemAL.contains(targetArrayMap.targetarrayitemid + ";" + zone)) {
                    zoneId = this.getTargetZones(targetArrayMap.targetarrayid).get(zone);
                    itemid = targetArrayMap.targetarrayitemid;
                    if (zoneItemAL.contains(zoneId + ";" + itemid)) continue;
                    targetZoneItemDS.addRow();
                    targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "zone", zone);
                    targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "arrayitemid", targetArrayMap.targetarrayid + "_" + targetArrayMap.targetrowindex + "_" + targetArrayMap.targetcolindex);
                    targetArrayMap.targetarrayzoneid = targetArrayMap.targetarrayzoneid != null && !targetArrayMap.targetarrayzoneid.isEmpty() ? targetArrayMap.targetarrayzoneid + ";" + zoneId : zoneId;
                    targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "arrayzoneid", zoneId);
                    zoneItemAL.add(zoneId + ";" + itemid);
                    continue;
                }
                if (!targetZoneItemAL.isEmpty() || !targetZoneMap.containsKey(targetArrayId + ";" + zone)) continue;
                zoneId = targetZoneMap.get(targetArrayId + ";" + zone);
                itemid = targetArrayMap.targetarrayitemid;
                if (zoneItemAL.contains(zoneId + ";" + itemid)) continue;
                targetZoneItemDS.addRow();
                targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "zone", zone);
                targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "arrayitemid", targetArrayMap.targetarrayitemid);
                targetArrayMap.targetarrayzoneid = targetArrayMap.targetarrayzoneid != null && !targetArrayMap.targetarrayzoneid.isEmpty() ? targetArrayMap.targetarrayzoneid + ";" + zoneId : zoneId;
                targetZoneItemDS.setValue(targetZoneItemDS.size() - 1, "arrayzoneid", zoneId);
                zoneItemAL.add(zoneId + ";" + itemid);
            }
        }
    }

    private HashMap<String, String> populateTargetZones(DataSet targetZoneDS, AuditInputs auditInputs) throws ActionException {
        HashMap<String, String> hm = new HashMap<String, String>();
        if (OpalUtil.isNotEmpty(targetZoneDS)) {
            String targetZone;
            int row;
            String sql = "select arrayzoneid, zone from arrayzone where arrayid = ?";
            String arrayid = targetZoneDS.getString(0, "arrayid");
            DataSet arrayZoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayid});
            DataSet addDataSet = new DataSet();
            for (row = 0; row < targetZoneDS.size(); ++row) {
                targetZone = targetZoneDS.getString(row, "zone", "");
                if (arrayZoneDS.findRow("zone", targetZone) != -1) continue;
                int addRow = addDataSet.addRow();
                addDataSet.setString(addRow, "arrayid", targetZoneDS.getString(row, "arrayid"));
                addDataSet.setString(addRow, "zone", targetZoneDS.getString(row, "zone"));
                addDataSet.setString(addRow, "color", targetZoneDS.getString(row, "color"));
            }
            if (!addDataSet.isEmpty()) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_ArrayZone");
                props.setProperty("arrayid", addDataSet.getColumnValues("arrayid", ";"));
                props.setProperty("zone", addDataSet.getColumnValues("zone", ";"));
                props.setProperty("color", addDataSet.getColumnValues("color", ";"));
                props.setProperty("copies", String.valueOf(addDataSet.size()));
                props.setProperty("auditreason", auditInputs.auditreason);
                props.setProperty("auditactivity", auditInputs.auditactivity);
                props.setProperty("auditsignedflag", auditInputs.auditsignedflag);
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                arrayZoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayid});
            }
            for (row = 0; row < targetZoneDS.size(); ++row) {
                targetZone = targetZoneDS.getString(row, "zone", "");
                int dsRow = arrayZoneDS.findRow("zone", targetZone);
                if (dsRow == -1) continue;
                String arrayzoneid = arrayZoneDS.getString(dsRow, "arrayzoneid");
                hm.put(targetZoneDS.getValue(row, "arrayid") + ";" + targetZoneDS.getValue(row, "zone"), arrayzoneid);
            }
        }
        return hm;
    }

    private ArrayList<String> populateTargetZoneDataSet(ArrayList<TargetArrayMap> targetArrayMapArrayList, String sourceArray, String targetArray, DataSet targetZoneDS, String mode, int sourceIndex, HashMap<String, HashMap<String, String>> zoneContentRuleMap, boolean validatesourcevolume) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT aiaz.arrayzoneid, ai.arrayid, ai.arrayitemid, ai.xpos, ai.ypos,  ai.totalvolume, ai.totalvolumeunits, ai.concentration, ai.concentrationunits,  az.zone, az.color, azc.contenttype, azc.contentitem  FROM arrayitem ai, arrayitemarrayzone aiaz, arrayzone az left outer join arrayzonecontent azc on az.arrayzoneid = azc.arrayzoneid  WHERE ai.arrayid IN (" + safeSQL.addIn(sourceArray.replaceAll(";", "','")) + ")  AND ai.arrayitemid = aiaz.arrayitemid AND aiaz.arrayzoneid = az.arrayzoneid";
        this.database.createPreparedResultSet("sourcearray", sql, safeSQL.getValues());
        HashMap sourceMap = new HashMap();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (validatesourcevolume && this.checkSourcesArrayToArray(sourceArray, ds)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Source array(s) do not have any content to be transferred."));
        }
        while (this.database.getNext("sourcearray")) {
            String key = this.database.getString("sourcearray", "arrayid") + ";" + this.database.getInt("sourcearray", "xpos") + ";" + this.database.getInt("sourcearray", "ypos");
            if (sourceMap.containsKey(key)) {
                ArrayList value = (ArrayList)sourceMap.get(key);
                value.set(1, (String)value.get(1) + ";" + this.database.getString("sourcearray", "arrayzoneid"));
                value.set(2, (String)value.get(2) + ";" + this.database.getString("sourcearray", "zone"));
                value.set(3, (String)value.get(3) + ";" + this.database.getString("sourcearray", "color"));
                value.set(8, (String)value.get(8) + ";" + this.database.getString("sourcearray", "contenttype"));
                value.set(9, (String)value.get(9) + ";" + this.database.getString("sourcearray", "contentitem"));
                sourceMap.put(key, value);
                continue;
            }
            ArrayList<String> sourceAL = new ArrayList<String>();
            sourceAL.add(this.database.getString("sourcearray", "arrayitemid"));
            sourceAL.add(this.database.getString("sourcearray", "arrayzoneid"));
            sourceAL.add(this.database.getString("sourcearray", "zone"));
            sourceAL.add(this.database.getString("sourcearray", "color"));
            BigDecimal totalvolume = this.database.getBigDecimal("sourcearray", "totalvolume");
            sourceAL.add(totalvolume != null ? this.m18NUtil.format(totalvolume) : "");
            sourceAL.add(this.database.getString("sourcearray", "totalvolumeunits"));
            BigDecimal concentration = this.database.getBigDecimal("sourcearray", "concentration");
            sourceAL.add(concentration != null ? this.m18NUtil.format(concentration) : "");
            sourceAL.add(this.database.getString("sourcearray", "concentrationunits"));
            sourceAL.add(this.database.getString("sourcearray", "contenttype"));
            sourceAL.add(this.database.getString("sourcearray", "contentitem"));
            sourceMap.put(key, sourceAL);
        }
        this.database.closeResultSet("sourcearray");
        StringBuffer sqlForTargetArray = new StringBuffer();
        safeSQL.reset();
        if (mode.equals("edit")) {
            sqlForTargetArray.append("SELECT ai.arrayid, ai.arrayitemid, ai.xpos, ai.ypos, az.zone, az.color FROM ").append("arrayitem ai, arrayitemarrayzone aiaz, arrayzone az  WHERE ").append(" ai.arrayid IN (").append(safeSQL.addIn(targetArray.replaceAll(";", "','"))).append(") AND ").append("ai.arrayitemid = aiaz.arrayitemid AND aiaz.arrayzoneid = az.arrayzoneid");
        } else {
            sqlForTargetArray.append("SELECT ai.arrayid, ai.arrayitemid, ai.xpos, ai.ypos FROM arrayitem ai WHERE ").append(" ai.arrayid IN (").append(safeSQL.addIn(targetArray.replaceAll(";", "','"))).append(")  ");
        }
        this.database.createPreparedResultSet("targetarray", sqlForTargetArray.toString(), safeSQL.getValues());
        HashMap<String, String> targetMap = new HashMap<String, String>();
        ArrayList<String> targetZoneItemAL = new ArrayList<String>();
        HashSet<String> targetZonesSet = new HashSet<String>();
        while (this.database.getNext("targetarray")) {
            String aiId = this.database.getString("targetarray", "arrayitemid");
            String key = this.database.getString("targetarray", "arrayid") + ";" + this.database.getInt("targetarray", "xpos") + ";" + this.database.getInt("targetarray", "ypos");
            targetMap.put(key, aiId);
            if (!mode.equals("edit")) continue;
            targetZoneItemAL.add(aiId + ";" + this.database.getString("targetarray", "zone"));
            targetZonesSet.add(this.database.getString("targetarray", "zone"));
        }
        this.database.closeResultSet("targetarray");
        String[] targetArrayArr = StringUtil.split(targetArray, ";");
        HashMap targetIdtoIndexMap = this.populateArrayIDtoIndexMap(targetArrayArr, targetArrayArr.length, 1);
        String[] sourceArrayArr = StringUtil.split(sourceArray, ";");
        HashMap sourceIdtoIndexMap = this.populateArrayIDtoIndexMap(sourceArrayArr, sourceArrayArr.length, sourceIndex);
        ArrayList<String> targetZoneAL = new ArrayList<String>();
        SequenceProcessor sp = this.getSequenceProcessor();
        for (TargetArrayMap targetArrayMap : targetArrayMapArrayList) {
            String targetArrayId;
            String sourceArrayId;
            if (!sourceIdtoIndexMap.containsKey(targetArrayMap.sourcearray)) continue;
            targetArrayMap.sourcearrayid = sourceArrayId = (String)sourceIdtoIndexMap.get(targetArrayMap.sourcearray);
            String keyForSourceDetailsMap = sourceArrayId + ";" + targetArrayMap.sourcerowindex + ";" + targetArrayMap.sourcecolindex;
            ArrayList sourceDetailsAL = (ArrayList)sourceMap.get(keyForSourceDetailsMap);
            if (sourceDetailsAL == null || sourceDetailsAL.size() <= 0) continue;
            String zonename = (String)sourceDetailsAL.get(2);
            targetArrayMap.sourcearrayitemid = (String)sourceDetailsAL.get(0);
            targetArrayMap.sourcearrayzoneid = (String)sourceDetailsAL.get(1);
            targetArrayMap.sourcearrayzonename = zonename;
            targetArrayMap.sourcetotalquantity = (String)sourceDetailsAL.get(4);
            targetArrayMap.sourcetotalquantityunits = (String)sourceDetailsAL.get(5);
            targetArrayMap.sourceconcentration = (String)sourceDetailsAL.get(6);
            targetArrayMap.sourceconcentrationunits = (String)sourceDetailsAL.get(7);
            targetArrayMap.sourcecontenttype = (String)sourceDetailsAL.get(8);
            targetArrayMap.sourcecontentitem = (String)sourceDetailsAL.get(9);
            targetArrayMap.targetarrayid = targetArrayId = (String)targetIdtoIndexMap.get(targetArrayMap.targetarray);
            String[] zones = StringUtil.split((String)sourceDetailsAL.get(2), ";");
            for (int z = 0; z < zones.length; ++z) {
                if (targetZoneAL.contains(targetArrayId + "," + zones[z]) || !targetZonesSet.isEmpty() && (targetZonesSet.isEmpty() || targetZonesSet.contains(zones[z]) || !this.createZoneDefintion(zones[z], zoneContentRuleMap))) continue;
                int row = targetZoneDS.addRow();
                targetZoneDS.setValue(row, "arrayid", targetArrayId);
                targetZoneDS.setValue(row, "zone", zones[z]);
                String colors = (String)sourceDetailsAL.get(3);
                String[] colorsplit = StringUtil.split(colors, ";");
                if (colors != null && !colors.isEmpty() && colorsplit.length > z) {
                    targetZoneDS.setValue(row, "color", colorsplit[z]);
                }
                int nextSequence = sp.getSequence("LV_ArrayZone", "mainkey");
                targetZoneDS.setValue(row, "keyid1", targetArrayId + "_" + StringUtil.padLeft("" + nextSequence, 6, '0'));
                targetZoneAL.add(targetArrayId + "," + zones[z]);
            }
            String keyForTargetDetailsMap = targetArrayId + ";" + targetArrayMap.targetrowindex + ";" + targetArrayMap.targetcolindex;
            targetArrayMap.targetarrayitemid = (String)targetMap.get(keyForTargetDetailsMap);
            targetArrayMap.targetarrayzonename = zonename;
            targetArrayMap.targetarrayzonecolor = (String)sourceDetailsAL.get(3);
        }
        return targetZoneItemAL;
    }

    private boolean checkSourcesArrayToArray(String soureArrays, DataSet ds) {
        String[] arrayids = StringUtil.split(soureArrays, ";");
        boolean allempty = true;
        block0: for (String arrayid : arrayids) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayid", arrayid);
            DataSet contents = ds.getFilteredDataSet(filter);
            if (contents.getRowCount() <= 0) continue;
            for (int c = 0; c < contents.getRowCount(); ++c) {
                if (!(contents.getBigDecimal(c, "totalvolume", BigDecimal.valueOf(0.0)).doubleValue() > 0.0)) continue;
                allempty = false;
                continue block0;
            }
        }
        return allempty;
    }

    private boolean checkSourcesASL(DataSet ds) {
        boolean allempty = true;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.getBigDecimal(i, "qtycurrent") != null && !(ds.getBigDecimal(i, "qtycurrent", BigDecimal.valueOf(0.0)).doubleValue() > 0.0)) continue;
            allempty = false;
            break;
        }
        return allempty;
    }

    private HashMap populateArrayIDtoIndexMap(String[] arrayIdArr, int length, int sourceIndex) {
        HashMap<Integer, String> idToIndexMap = new HashMap<Integer, String>();
        int sourceArrIndex = sourceIndex;
        for (int i = 1; i < length + 1; ++i) {
            idToIndexMap.put(sourceArrIndex, arrayIdArr[i - 1]);
            ++sourceArrIndex;
        }
        return idToIndexMap;
    }

    private String createTargetArray(PropertyList properties, int countOfTargetArrays, String targetarraytypeid, String targetarraytypeversionid, String targetarraylayoutid, String targetarraylayoutversionid) throws ActionException {
        PropertyList actionProps = new PropertyList();
        if (!targetarraylayoutid.isEmpty() && !targetarraylayoutversionid.isEmpty()) {
            actionProps.setProperty("arraylayoutid", targetarraylayoutid);
            actionProps.setProperty("arraylayoutversionid", targetarraylayoutversionid);
        } else {
            actionProps.setProperty("arraytypeid", targetarraytypeid);
            actionProps.setProperty("arraytypeversionid", targetarraytypeversionid);
        }
        actionProps.setProperty("copies", String.valueOf(countOfTargetArrays));
        actionProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        actionProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
        actionProps.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("CreateArray", "1", actionProps);
        properties.setProperty("newarrayid", actionProps.getProperty("arrayid"));
        return actionProps.getProperty("arrayid");
    }

    private ArrayList<TargetArrayMap> parseTransferMapString(String[] transferMapStrArr, int inputSourceArraysCount, String sourceIndex, TranslationProcessor tp) throws SapphireException {
        int srcArrIndex = Integer.parseInt(sourceIndex);
        int targetArrIndex = 1;
        int countOfSrcArraysInMap = 1;
        ArrayList<TargetArrayMap> targetArrayMapArrayList = new ArrayList<TargetArrayMap>();
        for (String s : transferMapStrArr) {
            TargetArrayMap tm = new TargetArrayMap();
            String[] transferMapPerAI = StringUtil.split(s, "-");
            String[] sourceArr = StringUtil.split(transferMapPerAI[1], ",");
            String[] targetArr = StringUtil.split(transferMapPerAI[0], ",");
            tm.sourcearray = Integer.parseInt(sourceArr[0]);
            if (tm.sourcearray < srcArrIndex) continue;
            if (tm.sourcearray > srcArrIndex) {
                srcArrIndex = tm.sourcearray;
                ++countOfSrcArraysInMap;
            }
            tm.sourcerowindex = Integer.parseInt(sourceArr[1]);
            tm.sourcecolindex = Integer.parseInt(sourceArr[2]);
            tm.targetarray = Integer.parseInt(targetArr[0]);
            if (tm.targetarray != targetArrIndex && countOfSrcArraysInMap <= inputSourceArraysCount) {
                targetArrIndex = tm.targetarray;
            } else if (countOfSrcArraysInMap > inputSourceArraysCount) break;
            tm.targetrowindex = Integer.parseInt(targetArr[1]);
            tm.targetcolindex = Integer.parseInt(targetArr[2]);
            targetArrayMapArrayList.add(tm);
        }
        if (countOfSrcArraysInMap < inputSourceArraysCount) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Invalid input sourcearrayid - Transfer Map in the Array Transfer Method is not defined to support the specified number of source arrays "));
        }
        return targetArrayMapArrayList;
    }

    private Map<String, String> getTargetZones(String targetArrayId) {
        if (!this.zoneMapCache.containsKey(targetArrayId)) {
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT az.arrayzoneid, az.zone FROM arrayzone az WHERE az.arrayid = ?", (Object[])new String[]{targetArrayId});
            if (OpalUtil.isNotEmpty(ds)) {
                for (int i = 0; i < ds.size(); ++i) {
                    map.put(ds.getString(i, "zone"), ds.getString(i, "arrayzoneid"));
                }
            }
            this.zoneMapCache.put(targetArrayId, map);
        }
        return this.zoneMapCache.get(targetArrayId);
    }

    private Map<String, String> getZoneMap(String arrayId) {
        if (!this.zoneIDMapCache.containsKey(arrayId)) {
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT az.arrayzoneid, az.zone FROM arrayzone az WHERE az.arrayid = ?", (Object[])new String[]{arrayId});
            if (OpalUtil.isNotEmpty(ds)) {
                for (int i = 0; i < ds.size(); ++i) {
                    map.put(ds.getString(i, "arrayzoneid"), ds.getString(i, "zone"));
                }
            }
            this.zoneIDMapCache.put(arrayId, map);
        }
        return this.zoneIDMapCache.get(arrayId);
    }

    private HashMap<String, HashMap<String, String>> parseContentTransferRule(String contentTransferRule) throws SapphireException {
        HashMap<String, HashMap<String, String>> zoneRulesMap = new HashMap<String, HashMap<String, String>>();
        PropertyList contentTransferRulePL = new PropertyList();
        contentTransferRulePL.setPropertyList(contentTransferRule);
        PropertyListCollection plc = contentTransferRulePL.getCollectionNotNull("contenttransferrule");
        for (int i = 0; i < plc.size(); ++i) {
            PropertyList pl = plc.getPropertyList(i);
            HashMap<String, String> zoneMap = new HashMap<String, String>();
            zoneMap.put("definition", pl.getProperty("definition"));
            zoneMap.put("content", pl.getProperty("content"));
            zoneMap.put("propagateunknown", pl.getProperty("propagateunknown"));
            zoneMap.put("propagateoperation", pl.getProperty("propagateoperation"));
            zoneMap.put("propagatetreatment", pl.getProperty("propagatetreatment"));
            zoneMap.put("propagatetransfer", pl.getProperty("propagatetransfer"));
            zoneMap.put("propagateControl", pl.getProperty("propagateControl"));
            zoneMap.put("propagateMM", pl.getProperty("propagateMM"));
            zoneRulesMap.put(pl.getProperty("zone"), zoneMap);
        }
        return zoneRulesMap;
    }

    private boolean createZoneDefintion(String zone, HashMap<String, HashMap<String, String>> zoneContentRuleMap) {
        boolean createZone;
        HashMap<String, String> value = zoneContentRuleMap.get(zone);
        if (value != null) {
            createZone = Boolean.valueOf(value.get("definition"));
        } else {
            HashMap<String, String> fullArrayRules = zoneContentRuleMap.get("(FullArray)");
            createZone = Boolean.valueOf(fullArrayRules.get("definition"));
        }
        return createZone;
    }

    private ArrayList<Boolean> copySourceContent(String zone, HashMap<String, HashMap<String, String>> zoneContentRuleMap, String contentItem) {
        ArrayList<Boolean> sourceRefAL = new ArrayList<Boolean>();
        boolean copyContent = false;
        boolean copySourceRef = false;
        HashMap<String, String> value = zoneContentRuleMap.get(zone);
        if (value == null && (value = zoneContentRuleMap.get("(FullArray)")) != null && Boolean.valueOf(value.get("definition")).booleanValue()) {
            copySourceRef = true;
        }
        if (value != null && Boolean.valueOf(value.get("definition")).booleanValue() && contentItem != null && !contentItem.isEmpty()) {
            copySourceRef = Boolean.valueOf(value.get("content"));
        }
        if (copySourceRef && contentItem.equals("Unknown")) {
            copyContent = Boolean.valueOf(value.get("propagateunknown"));
        } else if (copySourceRef && contentItem.equals("Operation")) {
            copyContent = Boolean.valueOf(value.get("propagateoperation"));
        } else if (copySourceRef && contentItem.equals("Treatment")) {
            copyContent = Boolean.valueOf(value.get("propagatetreatment"));
        } else if (copySourceRef && contentItem.equals("Transfer")) {
            copyContent = true;
        } else if (copySourceRef && contentItem.equals("Control")) {
            copyContent = Boolean.valueOf(value.get("propagateControl"));
        } else if (copySourceRef && contentItem.equals("MasterMix")) {
            copyContent = Boolean.valueOf(value.get("propagateMM"));
        }
        sourceRefAL.add(copySourceRef);
        sourceRefAL.add(copyContent);
        return sourceRefAL;
    }

    private HashMap<String, String> multiSampleChild(List<String> sourceSampleList, List<Integer> childCountList, String sampletypeid, AuditInputs auditInputs) throws SapphireException {
        StringBuilder sourceSampleIds = new StringBuilder();
        StringBuilder childcounts = new StringBuilder();
        for (int i = 0; i < sourceSampleList.size(); ++i) {
            if (i != 0) {
                sourceSampleIds.append(";");
                childcounts.append(";");
            }
            sourceSampleIds.append(sourceSampleList.get(i));
            childcounts.append(childCountList.get(i));
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("parent_depleteflag", "N");
        actionProps.setProperty("parent_sampleid", sourceSampleIds.toString());
        actionProps.setProperty("child_copies", childcounts.toString());
        if (!this.defaultchildsamplestoragestatus.equals("Use Biobanking Policy")) {
            actionProps.setProperty("child_storagestatus", StringUtil.repeat(this.defaultchildsamplestoragestatus, sourceSampleList.size(), ";"));
            actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
            actionProps.setProperty("childcolumn_receiverequiredflag", "N");
        }
        actionProps.setProperty("child_quantity", StringUtil.repeat("0", sourceSampleList.size(), ";"));
        if (this.defaultchildsamplestatus != null && !"Use Biobanking Policy".equals(this.defaultchildsamplestatus)) {
            if (!this.defaultchildsamplestatus.equals("Inherit from Parent")) {
                actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(this.defaultchildsamplestatus, sourceSampleList.size(), ";"));
            } else {
                actionProps.setProperty("childsamplestatus", StringUtil.repeat("inherit", sourceSampleList.size(), ";"));
            }
        }
        if (sampletypeid != null && sampletypeid.length() > 0) {
            actionProps.setProperty("mode", "Derivative");
            actionProps.setProperty("child_sampletypeid", StringUtil.repeat(sampletypeid, sourceSampleList.size(), ";"));
        } else {
            actionProps.setProperty("mode", "Aliquot");
        }
        actionProps.setProperty("propsmatch", "Y");
        actionProps.setProperty("auditsignedflag", auditInputs.auditsignedflag);
        actionProps.setProperty("auditactivity", auditInputs.auditactivity);
        actionProps.setProperty("auditreason", auditInputs.auditreason);
        this.getActionProcessor().processActionClass(MultiSampleChild.class.getName(), actionProps);
        String newSampleIds = actionProps.getProperty("newkeyid1");
        String[] newSampleIdArr = StringUtil.split(newSampleIds, ";");
        HashMap<String, String> sampleMap = new HashMap<String, String>();
        int pos = 0;
        for (int i = 0; i < sourceSampleList.size(); ++i) {
            int count = childCountList.get(i);
            String parentsampleid = sourceSampleList.get(i);
            StringBuilder childSampleIds = new StringBuilder();
            for (int c = 0; c < count; ++c) {
                if (childSampleIds.length() > 0) {
                    childSampleIds.append(";");
                }
                childSampleIds.append(newSampleIdArr[pos++]);
            }
            sampleMap.put(parentsampleid, childSampleIds.toString());
        }
        return sampleMap;
    }

    private DataSet getTargetVolumes(String targetarrayitemidlist, String targetVol, String targetVolUnits, ArrayList<TargetArrayMap> targetMap, boolean validateQty) throws SapphireException {
        String[] arrayitemids;
        DataSet tv = new DataSet();
        tv.addColumn("targetarrayitemid", 0);
        tv.addColumn("sourcearrayitemid", 0);
        tv.addColumn("totalvolume", 0);
        tv.addColumn("totalvolumeunits", 0);
        block0: for (String arrayitemid : arrayitemids = StringUtil.split(targetarrayitemidlist, ";")) {
            for (TargetArrayMap map : targetMap) {
                double dval;
                String sourceTotalQuantity;
                String currvol = targetVol;
                if (map.targetarrayid == null || !map.targetarrayitemid.equals(arrayitemid)) continue;
                if (map.sourcetotalquantityunits != null && map.sourcetotalquantityunits.equals(targetVolUnits) ? this.m18NUtil.parseBigDecimal(map.sourcetotalquantity).doubleValue() < this.m18NUtil.parseBigDecimal(targetVol).doubleValue() && validateQty : OpalUtil.isNotEmpty(sourceTotalQuantity = map.sourcetotalquantity) && OpalUtil.isNotEmpty(targetVol) && (dval = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(sourceTotalQuantity), map.sourcetotalquantityunits, targetVolUnits).doubleValue()) < this.m18NUtil.parseBigDecimal(targetVol).doubleValue() && validateQty) {
                    throw new SapphireException("Source volume for " + map.sourcearrayitemid + "(" + map.sourcetotalquantity + " " + map.sourcetotalquantityunits + ") is lesser than target volume for " + map.targetarrayitemid + " (" + targetVol + " " + targetVolUnits + ")");
                }
                int row = tv.addRow();
                tv.setString(row, "sourcearrayitemid", map.sourcearrayitemid);
                tv.setString(row, "arrayitemid", arrayitemid);
                if (!validateQty || this.checkSourceVolume(map.sourcearrayitemid, map.sourcearrayid)) {
                    if (currvol != null) {
                        tv.setString(row, "totalvolume", currvol);
                    }
                } else {
                    tv.setString(row, "totalvolume", "0");
                }
                tv.setString(row, "totalvolumeunits", targetVolUnits);
                continue block0;
            }
        }
        return tv;
    }

    boolean checkSourceVolume(String arrayitemid, String array) {
        DataSet currArrayItems = this.allArrayItem.get(array);
        if (this.allArrayItem.get(array) == null) {
            String sql = "SELECT arrayitemid, totalvolume FROM arrayitem WHERE arrayitemid like '" + array + "%'";
            currArrayItems = this.getQueryProcessor().getSqlDataSet(sql);
            this.allArrayItem.put(array, currArrayItems);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("arrayitemid", arrayitemid);
        DataSet match = currArrayItems.getFilteredDataSet(filter);
        return match.getRowCount() > 0 && !match.getValue(0, "totalvolume", "").isEmpty() && match.getBigDecimal(0, "totalvolume").doubleValue() > 0.0;
    }

    private DataSet updateTargetArrayVolumeConc(String targetvolume, String targetvolumeunits, String targetconc, String targetconcunits, String targetArray, ArrayList<TargetArrayMap> targetArrayMapArrayList, boolean validateQty, DataSet arraySourceInventoryDS) throws SapphireException {
        String sql = "select arrayitemid from arrayitem ai where arrayitemid in ( select arrayitemid from arrayitemcontent where arrayitemid in ( select arrayitemid from arrayitem where arrayid = ? ) ) order by createdt";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{targetArray});
        if (ds != null && ds.getRowCount() > 0) {
            ds.addColumn("totalvolume", 1);
            ds.addColumn("totalvolumeunits", 0);
            ds.addColumn("concentration", 0);
            ds.addColumn("concentrationunits", 0);
            String targetarrayitemids = ds.getColumnValues("arrayitemid", ";");
            DataSet tv = this.getTargetVolumes(targetarrayitemids, targetvolume, targetvolumeunits, targetArrayMapArrayList, validateQty);
            if (targetconc != null && !targetconc.isEmpty()) {
                tv.setString(0, "concentration", targetconc);
                tv.setString(0, "concentrationunits", targetconcunits);
                tv.padColumns();
            } else {
                PropertyList src = this.findSourceConc(targetarrayitemids, targetArrayMapArrayList);
                tv.addColumnValues("concentration", 0, src.getProperty("concentration"), ";");
                tv.addColumnValues("concentrationunits", 0, src.getProperty("concentrationunits"), ";");
            }
            for (int i = 0; i < tv.size(); ++i) {
                arraySourceInventoryDS.copyRow(tv, i, 1);
            }
            return tv;
        }
        return null;
    }

    private String determineTargetList(TRANSFERTYPE transfertype, int batchCount, int targetCount, String targetArrayTypeId, String targetArrayTypeVersionId, PropertyList properties) throws SapphireException {
        if (transfertype == TRANSFERTYPE.ASLTOASL || transfertype == TRANSFERTYPE.ARRAYTOASL) {
            String targetboxids = properties.getProperty("targetboxid", "");
            String targetsuids = properties.getProperty("targetsuid", "");
            if (targetboxids.length() == 0 && targetsuids.length() == 0) {
                String targetarraylayoutid = properties.getProperty("targetarraylayoutid", "");
                String targetarraylayoutversionid = properties.getProperty("targetarraylayoutversionid", "");
                return this.createTargetASLGrid(properties, targetCount * batchCount, targetArrayTypeId, targetarraylayoutid, targetarraylayoutversionid);
            }
            if (targetboxids.length() > 0) {
                String[] boxlist = StringUtil.split(targetboxids, ";");
                for (int i = 0; i < boxlist.length; ++i) {
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet("SELECT storageunitid FROM storageunit where linksdcid = 'LV_Box' and linkkeyid1 = ?", (Object[])new String[]{boxlist[i]});
                    String currentStorageUnitId = ret.getValue(0, "storageunitid");
                    if (targetsuids.length() > 0) {
                        targetsuids = targetsuids + ";";
                    }
                    targetsuids = targetsuids + currentStorageUnitId;
                }
            }
            return targetsuids;
        }
        String targetarrayids = properties.getProperty("targetarrayid", "");
        if (targetarrayids.length() == 0) {
            String targetarraylayoutid = properties.getProperty("targetarraylayoutid", "");
            String targetarraylayoutversionid = properties.getProperty("targetarraylayoutversionid", "");
            targetarrayids = this.createTargetArray(properties, targetCount * batchCount, targetArrayTypeId, targetArrayTypeVersionId, targetarraylayoutid, targetarraylayoutversionid);
        }
        return targetarrayids;
    }

    private TRANSFERTYPE determineTransferType(String sourceArrayTypeId, String targetArrayTypeId) {
        boolean isSourceASLGrid = ArrayUtil.isArrayTypeASLGrid(this.getQueryProcessor(), sourceArrayTypeId);
        boolean isTargetASLGrid = ArrayUtil.isArrayTypeASLGrid(this.getQueryProcessor(), targetArrayTypeId);
        if (isSourceASLGrid && isTargetASLGrid) {
            return TRANSFERTYPE.ASLTOASL;
        }
        if (isSourceASLGrid && !isTargetASLGrid) {
            return TRANSFERTYPE.ASLTOARRAY;
        }
        if (!isSourceASLGrid && isTargetASLGrid) {
            return TRANSFERTYPE.ARRAYTOASL;
        }
        return TRANSFERTYPE.ARRAYTOARRAY;
    }

    private String determineSourceList(TRANSFERTYPE transfertype, String sourceArrayTypeId, String sourceArrayTypeVersionId, PropertyList properties) throws SapphireException {
        if (transfertype == TRANSFERTYPE.ASLTOASL || transfertype == TRANSFERTYPE.ASLTOARRAY) {
            String sourceboxids = properties.getProperty("sourceboxid", "");
            String sourcesuids = properties.getProperty("sourcesuid", "");
            if (sourceboxids.isEmpty() && sourcesuids.isEmpty()) {
                throw new SapphireException("Either source box ids or source storageunit ids need to be specified.");
            }
            if (!sourceboxids.isEmpty()) {
                String[] boxlist = StringUtil.split(sourceboxids, ";");
                StringBuilder sb = new StringBuilder();
                for (String boxid : boxlist) {
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet("SELECT storageunitid, storageunittype FROM storageunit where linksdcid = 'LV_Box' and linkkeyid1 = ?", (Object[])new String[]{boxid});
                    String currentStorageUnitId = ret.getValue(0, "storageunitid");
                    if (!sourceArrayTypeId.endsWith(ret.getValue(0, "storageunittype"))) {
                        throw new SapphireException("Source array type does not match the one specified in the transfer method");
                    }
                    sb.append(currentStorageUnitId).append(";");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                    sourcesuids = sb.toString();
                }
            } else {
                String[] suidlist;
                for (String storageunitid : suidlist = StringUtil.split(sourcesuids, ";")) {
                    DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet("SELECT storageunitid, storageunittype FROM storageunit where linksdcid = 'LV_Box' and storageunitid = ?", (Object[])new String[]{storageunitid});
                    if (sourceArrayTypeId.endsWith(ret.getValue(0, "storageunittype"))) continue;
                    throw new SapphireException("Source array type does not match the one specified in the transfer method");
                }
            }
            return sourcesuids;
        }
        String sourcearrayids = properties.getProperty("sourcearrayid", "");
        if (sourcearrayids.isEmpty()) {
            throw new SapphireException("Source array ids are mandatory to perform the transfer");
        }
        DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDC_ARRAY, "select arrayid, arraytypeid, arraytypeversionid, arraystatus from array where arrayid in ([])", sourcearrayids);
        if (ds != null && !ds.isEmpty()) {
            for (int i = 0; i < ds.size(); ++i) {
                String arrayStatus = ds.getString(i, "arraystatus", "");
                if (!(arrayStatus.equals("Loaded") || arrayStatus.equals("SentToInstrument") || arrayStatus.equals("DataEntered") || arrayStatus.equals("Reviewed"))) {
                    throw new SapphireException("ERROR: Invalid array status. Array status must be \"Loaded\", \"SentToInstrument\", \"DataEntered\", or \"Reviewed\" to perform array transfer");
                }
                String arraytypeid = ds.getString(i, "arraytypeid", "");
                String arraytypeversionid = ds.getString(i, "arraytypeversionid", "");
                if (arraytypeid.equals(sourceArrayTypeId) && arraytypeversionid.equals(sourceArrayTypeVersionId)) continue;
                throw new SapphireException("ERROR: Invalid status. " + this.getTranslationProcessor().translate("Selected list of arrays do not have matching array types."));
            }
        } else {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalis source array. Unable to perform array transfer."));
        }
        return sourcearrayids;
    }

    private void checkBasicProperties(String transferMethodId) throws SapphireException {
        if (transferMethodId.isEmpty()) {
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Transfer method is mandatory to perform the transfer operation"));
        }
        if (StringUtil.split(transferMethodId, ";").length > 1) {
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Invalid transfer method specified to perform the transfer operation"));
        }
    }

    private DataSet doASLToASLTransfer(SourceInputs sourceInputs, TargetInputs targetInputs, DataSet transferMethodDetails, boolean createChildSamples, String childSampleTypeId, boolean disposeSourceWhenConsumed) throws SapphireException {
        DataSet sourceDimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), sourceInputs.sourcearraytypeid, sourceInputs.sourcearraytypeversionid);
        DataSet sourceContent = this.getASLGridContentInfo(sourceInputs.sourceindex, sourceInputs.sourceid, sourceDimensions, sourceInputs.maxsourcecount);
        if (this.checkSourcesASL(sourceContent)) {
            throw new SapphireException("Source boxes(s) do not have any content to be transferred.");
        }
        DataSet targetDimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), targetInputs.targetarraytypeid, targetInputs.targetarraytypeversionid);
        DataSet targetContent = this.getASLGridContentInfo(1, targetInputs.targetid, targetDimensions, targetInputs.maxtargetcount);
        if (!createChildSamples && !targetInputs.targetvolume.isEmpty()) {
            throw new SapphireException("Do not specify target volume when not creating child samples during transfer.");
        }
        if (!"S".equals(sourceInputs.quantityhandlingflag) && createChildSamples) {
            boolean targetVolumeNeeded = false;
            for (int i = 0; i < sourceContent.getRowCount(); ++i) {
                if (sourceContent.getValue(i, "qtycurrent").isEmpty()) continue;
                targetVolumeNeeded = true;
            }
            if (targetVolumeNeeded && targetInputs.targetvolume.isEmpty()) {
                throw new SapphireException("Target volume should be specified either in action properties or Transfer Method");
            }
            if (targetVolumeNeeded && targetInputs.targetvolumeunits.isEmpty()) {
                throw new SapphireException("Target volume units should be specified either in action properties or Transfer Method ");
            }
        }
        String sourceindexmap = this.determineSourceIndexMap(sourceInputs.sourceid, sourceInputs.sourceindex, sourceInputs.maxsourcecount);
        return this.doASLToASLUpdates(sourceindexmap, sourceContent, targetContent, transferMethodDetails, createChildSamples, childSampleTypeId, sourceInputs, targetInputs, disposeSourceWhenConsumed);
    }

    private String determineSourceIndexMap(String sourcesuids, int sourceindex, int sourcecount) {
        StringBuilder sourceindexmap = new StringBuilder();
        int index = sourceindex;
        for (int i = 0; i < StringUtil.split(sourcesuids, ";").length; ++i) {
            if (index <= sourcecount) {
                sourceindexmap.append(index).append(";");
            } else {
                index = 1;
                sourceindexmap.append(index).append(";");
            }
            ++index;
        }
        if (sourceindexmap.length() > 0) {
            sourceindexmap.setLength(sourceindexmap.length() - 1);
        }
        return sourceindexmap.toString();
    }

    private DataSet getASLGridContentInfo(int index, String suids, DataSet dimensions, int maxcount) {
        DataSet aslContents = new DataSet();
        aslContents.addColumn("index", 1);
        aslContents.addColumn("item", 1);
        aslContents.addColumn("gridid", 0);
        aslContents.addColumn("gridtype", 0);
        aslContents.addColumn("positionid", 0);
        aslContents.addColumn("labelrow", 0);
        aslContents.addColumn("labelcol", 0);
        aslContents.addColumn("sampleid", 0);
        aslContents.addColumn("trackitemid", 0);
        aslContents.addColumn("qtycurrent", 1);
        aslContents.addColumn("qtyunits", 0);
        aslContents.addColumn("arraylayoutzone", 0);
        aslContents.addColumn("concentration", 1);
        aslContents.addColumn("concentrationunits", 0);
        aslContents.addColumn("childqty", 1);
        aslContents.addColumn("childqtyunits", 0);
        aslContents.addColumn("childconc", 1);
        aslContents.addColumn("childconcunits", 0);
        aslContents.addColumn("childtrackitemid", 0);
        int numcolumns = dimensions.getInt(0, "numcolumns");
        String[] suidList = StringUtil.split(suids, ";");
        for (int i = 0; i < suidList.length; ++i) {
            String sql = "select su.storageunitid, su.parentid, su.labelrow, su.labelcol, su.storageunitindex, su.arraylayoutzone, su.arraylayoutzonecolor, ti.trackitemid, ti.linkkeyid1, ti.qtycurrent, ti.qtyunits, (select s.concentration from s_sample s where s.s_sampleid = ti.linkkeyid1 and ti.linksdcid = 'Sample') concentration, (select s.concentrationunits from s_sample s where s.s_sampleid = ti.linkkeyid1 and ti.linksdcid = 'Sample') concentrationunits from storageunit su left outer join trackitem ti on ti.currentstorageunitid = su.storageunitid where su.parentid = ? order by su.labelrow, su.labelcol";
            DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{suidList[i]});
            for (int su = 0; su < ret.size(); ++su) {
                int row = aslContents.addRow();
                aslContents.setNumber(row, "item", row);
                aslContents.setString(row, "gridid", suidList[i]);
                if (index + i <= maxcount) {
                    aslContents.setNumber(row, "index", index + i);
                } else {
                    aslContents.setNumber(row, "index", index - maxcount + i);
                }
                aslContents.setString(row, "gridtype", "storageunit");
                aslContents.setString(row, "positionid", ret.getString(su, "storageunitid"));
                aslContents.setString(row, "labelrow", ret.getString(su, "labelrow"));
                aslContents.setString(row, "labelcol", ret.getString(su, "labelcol"));
                aslContents.setString(row, "arraylayoutzone", ret.getString(su, "arraylayoutzone", ""));
                aslContents.setString(row, "arraylayoutzonecolor", ret.getString(su, "arraylayoutzonecolor", ""));
                int storageunitindex = ret.getInt(su, "storageunitindex") - 1;
                aslContents.setNumber(row, "row", storageunitindex / numcolumns);
                aslContents.setNumber(row, "column", storageunitindex % numcolumns);
                aslContents.setString(row, "trackitemid", ret.getString(su, "trackitemid"));
                aslContents.setString(row, "sampleid", ret.getString(su, "linkkeyid1"));
                aslContents.setNumber(row, "qtycurrent", ret.getValue(su, "qtycurrent"));
                aslContents.setString(row, "qtyunits", ret.getString(su, "qtyunits"));
                aslContents.setNumber(row, "concentration", ret.getValue(su, "concentration", ""));
                aslContents.setString(row, "concentrationunits", ret.getValue(su, "concentrationunits", ""));
            }
        }
        return aslContents;
    }

    private DataSet getArrayItemInfo(String arrayids, int startindex, int maxcount) {
        String[] arrayidList = StringUtil.split(arrayids, ";");
        DataSet arrayContents = new DataSet();
        arrayContents.addColumn("item", 1);
        arrayContents.addColumn("gridtype", 0);
        arrayContents.addColumn("arrayitemid", 0);
        arrayContents.addColumn("labelrow", 0);
        arrayContents.addColumn("labelcol", 0);
        arrayContents.addColumn("row", 1);
        arrayContents.addColumn("column", 1);
        arrayContents.addColumn("sampleid", 0);
        arrayContents.addColumn("trackitemid", 0);
        arrayContents.addColumn("sdcid", 0);
        arrayContents.addColumn("parentstorageunitid", 0);
        arrayContents.addColumn("totalvolumeincrement", 0);
        arrayContents.addColumn("totalvolumeunits", 0);
        arrayContents.addColumn("concentration", 1);
        arrayContents.addColumn("concentrationunits", 0);
        arrayContents.addColumn("index", 1);
        arrayContents.addColumn("volume", 1);
        arrayContents.addColumn("volumeunits", 0);
        arrayContents.addColumn("childqty", 1);
        arrayContents.addColumn("childqtyunits", 0);
        arrayContents.addColumn("childconc", 1);
        arrayContents.addColumn("childconcunits", 0);
        arrayContents.addColumn("childtrackitemid", 0);
        arrayContents.addColumn("contentsdcid", 0);
        arrayContents.addColumn("multiplecontent", 0);
        int index = startindex;
        for (String arrayid : arrayidList) {
            String sql = "select arrayitemarrayzone.arrayitemid, arrayzone.zone, arrayzone.color from arrayitemarrayzone, arrayzone where arrayzone.arrayzoneid = arrayitemarrayzone.arrayzoneid and arrayzone.zone != '(FullArray)' and arrayitemarrayzone.arrayitemid in (select arrayitem.arrayitemid from arrayitem where arrayitem.arrayid=?) order by arrayitemarrayzone.arrayitemid";
            DataSet zoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayid});
            HashMap<String, String> filter = new HashMap<String, String>();
            sql = "SELECT ai.arrayitemid, ai.horizontallabel, ai.verticallabel, ai.xpos, ai.ypos, ai.totalvolume, ai.totalvolumeunits, ai.concentration, ai.concentrationunits FROM arrayitem ai WHERE ai.arrayid = ? order by ai.usersequence";
            DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayid});
            for (int ai = 0; ai < ret.size(); ++ai) {
                String arrayitemid = ret.getValue(ai, "arrayitemid");
                int row = arrayContents.addRow();
                arrayContents.setNumber(row, "item", row);
                arrayContents.setString(row, "gridid", arrayid);
                arrayContents.setString(row, "gridtype", "array");
                arrayContents.setString(row, "arrayitemid", arrayitemid);
                arrayContents.setString(row, "labelrow", ret.getValue(ai, "horizontallabel"));
                arrayContents.setString(row, "labelcol", ret.getValue(ai, "verticallabel"));
                arrayContents.setNumber(row, "row", ret.getValue(ai, "xpos"));
                arrayContents.setNumber(row, "column", ret.getValue(ai, "ypos"));
                arrayContents.setNumber(row, "totalvolume", ret.getValue(ai, "totalvolume"));
                arrayContents.setString(row, "totalvolumeunits", ret.getValue(ai, "totalvolumeunits"));
                arrayContents.setNumber(row, "concentration", ret.getValue(ai, "concentration"));
                arrayContents.setString(row, "concentrationunits", ret.getValue(ai, "concentrationunits", ""));
                arrayContents.setNumber(row, "index", index);
                String arraylayoutzone = "";
                String arraylayoutzonecolor = "";
                filter.clear();
                filter.put("arrayitemid", arrayitemid);
                DataSet ds = zoneDS.getFilteredDataSet(filter);
                if (ds != null && ds.size() > 0) {
                    if (ds.size() > 1) {
                        for (int i = 0; i < ds.size(); ++i) {
                            if (!"Unknown".equalsIgnoreCase(ds.getString(i, "zone", ""))) continue;
                            arraylayoutzone = ds.getString(i, "zone", "");
                            arraylayoutzonecolor = ds.getString(i, "color", "");
                            break;
                        }
                    } else {
                        arraylayoutzone = ds.getString(0, "zone", "");
                        arraylayoutzonecolor = ds.getString(0, "color", "");
                    }
                }
                arrayContents.setString(row, "arraylayoutzone", arraylayoutzone);
                arrayContents.setString(row, "arraylayoutzonecolor", arraylayoutzonecolor);
                String sql2 = "SELECT arrayitemcontent.contentsdcid, trackitem.trackitemid, arrayitemcontent.contentkeyid1, arrayitemcontent.contenttype, arrayitemcontent.volume, arrayitemcontent.volumeunits, arrayitemcontent.concentration, arrayitemcontent.concentrationunits FROM arrayitemcontent, trackitem WHERE trackitem.linksdcid = arrayitemcontent.contentsdcid and trackitem.linkkeyid1 = arrayitemcontent.contentkeyid1 and arrayitemcontent.arrayitemid=? and arrayitemcontent.contentsdcid='Sample'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, new Object[]{ret.getValue(ai, "arrayitemid")});
                if (ds == null || ds.getRowCount() == 0) continue;
                arrayContents.setString(row, "sampleid", ds.getValue(0, "contentkeyid1"));
                arrayContents.setString(row, "trackitemid", ds.getValue(0, "trackitemid"));
                arrayContents.setString(row, "contentsdcid", "Sample");
                arrayContents.setString(row, "multiplecontent", "N");
                if (ds.getRowCount() <= 1) continue;
                for (int j = 1; j < ds.getRowCount(); ++j) {
                    row = arrayContents.addRow();
                    arrayContents.setNumber(row, "item", row);
                    arrayContents.setString(row, "gridid", arrayid);
                    arrayContents.setString(row, "gridtype", "array");
                    arrayContents.setString(row, "arrayitemid", ret.getValue(ai, "arrayitemid"));
                    arrayContents.setString(row, "labelrow", ret.getValue(ai, "horizontallabel"));
                    arrayContents.setString(row, "labelcol", ret.getValue(ai, "verticallabel"));
                    arrayContents.setNumber(row, "row", ret.getValue(ai, "xpos"));
                    arrayContents.setNumber(row, "column", ret.getValue(ai, "ypos"));
                    arrayContents.setNumber(row, "totalvolume", ret.getValue(ai, "totalvolume"));
                    arrayContents.setNumber(row, "index", index);
                    arrayContents.setString(row, "sampleid", ds.getValue(j, "contentkeyid1"));
                    arrayContents.setString(row, "trackitemid", ds.getValue(j, "trackitemid"));
                    arrayContents.setString(row, "multiplecontent", "Y");
                }
            }
            ++index;
        }
        return arrayContents;
    }

    private DataSet doASLToArrayTransfer(SourceInputs sourceInputs, TargetInputs targetInputs, DataSet transferMethodDetails, String createChildSamples, String childSampleTypeId, boolean disposeSourceWhenConsumed) throws SapphireException {
        DataSet sourceDimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), sourceInputs.sourcearraytypeid, sourceInputs.sourcearraytypeversionid);
        DataSet sourceContent = this.getASLGridContentInfo(sourceInputs.sourceindex, sourceInputs.sourceid, sourceDimensions, sourceInputs.maxsourcecount);
        if (this.checkSourcesASL(sourceContent)) {
            throw new SapphireException("Source boxes(s) do not have any content to be transferred.");
        }
        DataSet targetDimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), targetInputs.targetarraytypeid, targetInputs.targetarraytypeversionid);
        DataSet targetArrayItems = this.getArrayItemInfo(targetInputs.targetid, 1, targetInputs.maxtargetcount);
        if (!"S".equals(sourceInputs.quantityhandlingflag) && !createChildSamples.equals("N")) {
            boolean targetVolumeNeeded = false;
            for (int i = 0; i < sourceContent.getRowCount(); ++i) {
                if (sourceContent.getValue(i, "qtycurrent").equals("")) continue;
                targetVolumeNeeded = true;
                break;
            }
            if (targetVolumeNeeded && targetInputs.targetvolume.length() == 0) {
                throw new SapphireException("Target volume should be specified either in action properties or Transfer Method");
            }
            if (targetVolumeNeeded && targetInputs.targetvolumeunits.length() == 0) {
                throw new SapphireException("Target volume units should be specified either in action properties or Transfer Method ");
            }
        }
        String sourceindexmap = this.determineSourceIndexMap(sourceInputs.sourceid, sourceInputs.sourceindex, sourceInputs.maxsourcecount);
        DataSet ds = this.doAslToArrayUpdates(targetInputs.targetid, sourceindexmap, sourceContent, targetArrayItems, transferMethodDetails, createChildSamples, childSampleTypeId, sourceInputs, targetInputs, disposeSourceWhenConsumed);
        this.updateTargetArrayStatus(targetInputs, this.auditInput);
        return ds;
    }

    private DataSet doArrayToASLTransfer(SourceInputs sourceInputs, TargetInputs targetInputs, DataSet transferMethodDetails, boolean createChildSamples, String childSampleTypeId) throws SapphireException {
        DataSet sourceArrayItems = this.getArrayItemInfo(sourceInputs.sourceid, sourceInputs.sourceindex, sourceInputs.maxsourcecount);
        if (!createChildSamples) {
            throw new SapphireException("Array to ASL transfer requires creation of child samples.");
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("contentsdcid", "Sample");
        DataSet aiwithsamples = sourceArrayItems.getFilteredDataSet(filter);
        if (aiwithsamples.getRowCount() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Source array(s) do not have any content to be transferred."));
        }
        DataSet targetDimensions = ArrayUtil.getArrayTypeDimensions(this.getQueryProcessor(), targetInputs.targetarraytypeid, targetInputs.targetarraytypeversionid);
        DataSet targetContent = this.getASLGridContentInfo(1, targetInputs.targetid, targetDimensions, targetInputs.maxtargetcount);
        if (!"S".equals(sourceInputs.quantityhandlingflag) && createChildSamples) {
            boolean targetVolumeNeeded = false;
            for (int i = 0; i < sourceArrayItems.getRowCount(); ++i) {
                if (sourceArrayItems.getValue(i, "totalvolume").equals("")) continue;
                targetVolumeNeeded = true;
                break;
            }
            if (targetVolumeNeeded && targetInputs.targetvolume.length() == 0) {
                throw new SapphireException("Target volume should be specified either in action properties or Transfer Method");
            }
            if (targetVolumeNeeded && targetInputs.targetvolumeunits.length() == 0) {
                throw new SapphireException("Target volume units should be specified either in action properties or Transfer Method ");
            }
        }
        String sourceindexmap = this.determineSourceIndexMap(sourceInputs.sourceid, sourceInputs.sourceindex, sourceInputs.maxsourcecount);
        return this.doArrayToASLUpdates(sourceindexmap, sourceArrayItems, targetContent, transferMethodDetails, createChildSamples, childSampleTypeId, sourceInputs, targetInputs);
    }

    private HashMap<String, Integer> determineTargetContentCounts(String[] transferMapItems) {
        HashMap<String, Integer> targetItemCount = new HashMap<String, Integer>();
        for (String currMapItem : transferMapItems) {
            String[] currMap = StringUtil.split(currMapItem, "-");
            String targetMap = currMap[0];
            if (targetItemCount.get(targetMap) == null) {
                targetItemCount.put(targetMap, 1);
                continue;
            }
            int currentCount = targetItemCount.get(targetMap);
            targetItemCount.put(targetMap, currentCount + 1);
        }
        return targetItemCount;
    }

    private DataSet doASLToASLUpdates(String sourceindexmap, DataSet sourceContent, DataSet targetContent, DataSet transferMethodDetails, boolean createChildSamples, String childSampleTypeId, SourceInputs sourceInputs, TargetInputs targetInputs, boolean disposeSourceWhenConsumed) throws SapphireException {
        String transferMap = transferMethodDetails.getValue(0, "transfermap", "");
        String[] transferMapItems = StringUtil.split(transferMap, "|");
        DataSet updateTargetContent = new DataSet();
        String[] sourceindices = StringUtil.split(sourceindexmap, ";");
        DataSet targetArrayLayoutZoneItems = null;
        if (!targetInputs.targetarraylayoutid.isEmpty()) {
            String sql = "select xpos, ypos from arraylayoutzoneitem where arraylayoutid = ? and arraylayoutversionid = ?";
            targetArrayLayoutZoneItems = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{targetInputs.targetarraylayoutid, targetInputs.targetarraylayoutversionid});
        }
        DataSet updateZoneDS = new DataSet();
        HashMap<String, Integer> targetContentCounts = this.determineTargetContentCounts(transferMapItems);
        for (String currMapItem : transferMapItems) {
            String[] currMap = StringUtil.split(currMapItem, "-");
            String targetItem = currMap[0];
            String[] targetItemParts = StringUtil.split(currMap[0], ",");
            int targetGrid = Integer.parseInt(targetItemParts[0]);
            int targetRow = Integer.parseInt(targetItemParts[1]);
            int targetCol = Integer.parseInt(targetItemParts[2]);
            String[] sourceItem = StringUtil.split(currMap[1], ",");
            int sourceGrid = Integer.parseInt(sourceItem[0]);
            int sourceRow = Integer.parseInt(sourceItem[1]);
            int sourceCol = Integer.parseInt(sourceItem[2]);
            boolean validSourceGrid = false;
            for (String currsi : sourceindices) {
                if (!sourceItem[0].equals(currsi)) continue;
                validSourceGrid = true;
                break;
            }
            if (!validSourceGrid) continue;
            HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
            filter.put("index", new BigDecimal(sourceGrid));
            filter.put("row", new BigDecimal(sourceRow));
            filter.put("column", new BigDecimal(sourceCol));
            DataSet matchSourceContent = sourceContent.getFilteredDataSet(filter);
            filter.clear();
            filter.put("index", new BigDecimal(targetGrid));
            filter.put("row", new BigDecimal(targetRow));
            filter.put("column", new BigDecimal(targetCol));
            DataSet matchTargetContent = targetContent.getFilteredDataSet(filter);
            if (OpalUtil.isNotEmpty(matchSourceContent) && OpalUtil.isNotEmpty(matchTargetContent)) {
                if (!matchSourceContent.getString(0, "arraylayoutzone", "").isEmpty()) {
                    int row = updateZoneDS.addRow();
                    updateZoneDS.setString(row, "storageunitid", matchTargetContent.getString(0, "positionid"));
                    updateZoneDS.setString(row, "arraylayoutzone", matchSourceContent.getValue(0, "arraylayoutzone"));
                    updateZoneDS.setString(row, "arraylayoutzonecolor", matchSourceContent.getValue(0, "arraylayoutzonecolor"));
                }
                if (matchSourceContent.getValue(0, "sampleid", "").isEmpty()) continue;
                if (matchSourceContent.getDouble(0, "qtycurrent") == 0.0) {
                    Trace.log("Source position:" + sourceRow + "," + sourceCol + " is empty and ignored");
                    continue;
                }
                if (!matchTargetContent.getValue(0, "sampleid", "").isEmpty()) {
                    Trace.log("Target position " + targetRow + "," + targetCol + " already has contents. Cannot perform transfer");
                    throw new SapphireException(this.getTranslationProcessor().translate("One or more target positions already have contents. Cannot perform transfer."));
                }
                if (!targetInputs.targetarraylayoutid.isEmpty() && targetArrayLayoutZoneItems != null) {
                    filter.clear();
                    filter.put("xpos", new BigDecimal(targetRow));
                    filter.put("ypos", new BigDecimal(targetCol));
                    DataSet ds = targetArrayLayoutZoneItems.getFilteredDataSet(filter);
                    if (ds.getRowCount() == 1) {
                        throw new SapphireException("One or more target positions do not belong to any zone and attempts to transfer to those wells are invalid.");
                    }
                }
                updateTargetContent.copyRow(matchTargetContent, 0, 1);
                int row = updateTargetContent.getRowCount() - 1;
                updateTargetContent.setString(row, "sampleid", matchSourceContent.getValue(0, "sampleid"));
                updateTargetContent.setString(row, "trackitemid", matchSourceContent.getValue(0, "trackitemid"));
                updateTargetContent.setNumber(row, "qtycurrent", targetInputs.targetvolume);
                updateTargetContent.setString(row, "qtyunits", targetInputs.targetvolumeunits);
                int matchingsourceitem = matchSourceContent.getInt(0, "item");
                String sourceqtyunits = sourceContent.getValue(matchingsourceitem, "qtyunits", "");
                if (!createChildSamples || sourceContent.getValue(matchingsourceitem, "qtycurrent") == null) continue;
                String sourceConcentration = sourceContent.getValue(matchingsourceitem, "concentration", "");
                String sourceConcentrationUnits = sourceContent.getValue(matchingsourceitem, "concentrationunits", "");
                int numOfContents = targetContentCounts.get(targetItem);
                VolumeParams volumeParams = this.determineSourceVolumeToDecrement(sourceContent.getValue(matchingsourceitem, "qtycurrent"), sourceqtyunits, sourceConcentration, sourceConcentrationUnits, sourceInputs, targetInputs, numOfContents);
                double sourcevolumetodecrement = volumeParams.sourceVolumeToDecrement;
                double targetvolumetoupdate = volumeParams.targetVolumeToUpdate;
                if (OpalUtil.isNotEmpty(sourceConcentration) && OpalUtil.isNotEmpty(sourceConcentrationUnits)) {
                    double sourceConcentrationN;
                    double targetConcentrationN;
                    String targetConcentration = String.valueOf(volumeParams.targetConcentration);
                    String targetConcentrationUnits = volumeParams.targetConcUnits;
                    if (OpalUtil.isNotEmpty(targetConcentration) && OpalUtil.isNotEmpty(targetConcentrationUnits) && (targetConcentrationN = this.m18NUtil.parseBigDecimal(targetConcentration).doubleValue()) != (sourceConcentrationN = this.m18NUtil.parseBigDecimal(sourceConcentration).doubleValue())) {
                        double diluentVolume = targetvolumetoupdate - sourcevolumetodecrement;
                        if (diluentVolume < 0.0) {
                            diluentVolume = 0.0;
                        }
                        updateTargetContent.setNumber(row, "__trackitem_diluentvolume", this.convertToBigDecimal(diluentVolume));
                        updateTargetContent.setString(row, "__trackitem_diluentvolumeunits", volumeParams.targetVolumeToUpdateUnits);
                    }
                }
                if (!sourceContent.getValue(matchingsourceitem, "qtycurrent", "").isEmpty()) {
                    double sourcevolume = sourceContent.getDouble(matchingsourceitem, "qtycurrent");
                    sourceContent.setNumber(matchingsourceitem, "qtycurrent", new BigDecimal(sourcevolume - sourcevolumetodecrement));
                }
                sourceContent.setString(matchingsourceitem, "qtyunits", sourceqtyunits);
                if (targetvolumetoupdate == -1.0) continue;
                updateTargetContent.setNumber(row, "childqty", targetvolumetoupdate);
                updateTargetContent.setString(row, "childqtyunits", volumeParams.targetVolumeToUpdateUnits);
                if (volumeParams.targetConcentration == -1.0) continue;
                updateTargetContent.setNumber(row, "childconc", volumeParams.targetConcentration);
                updateTargetContent.setString(row, "childconcunits", volumeParams.targetConcUnits);
                continue;
            }
            Trace.log("Did not find matching source content to be transferred: index:" + sourceGrid + " row::" + sourceRow + "  col::" + sourceCol);
        }
        if (!createChildSamples) {
            PropertyList edittrackitemProps = new PropertyList();
            edittrackitemProps.setProperty("trackitemid", updateTargetContent.getColumnValues("trackitemid", ";"));
            edittrackitemProps.setProperty("currentstorageunitid", updateTargetContent.getColumnValues("positionid", ";"));
            this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), edittrackitemProps);
        } else {
            if (updateTargetContent.getRowCount() > 0) {
                this.handleRegularChildren(updateTargetContent, childSampleTypeId);
                updateTargetContent = this.handlePooledSamples(updateTargetContent, true);
            }
            this.updateSourceASLVolumes(sourceContent, disposeSourceWhenConsumed);
        }
        if (OpalUtil.isNotEmpty(updateZoneDS)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "StorageUnitSDC");
            props.setProperty("keyid1", updateZoneDS.getColumnValues("storageunitid", ";"));
            props.setProperty("arraylayoutzone", updateZoneDS.getColumnValues("arraylayoutzone", ";"));
            props.setProperty("arraylayoutzonecolor", updateZoneDS.getColumnValues("arraylayoutzonecolor", ";"));
            props.setProperty("__syncoperation", "Y");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        return updateTargetContent;
    }

    private void handleRegularChildren(DataSet updateTargetContent, String sampleTypeId) throws SapphireException {
        String allPositions = PropertyUtil.getUniqueItems(updateTargetContent.getColumnValues("positionid", ";"));
        String[] positions = StringUtil.split(allPositions, ";");
        updateTargetContent.addColumn("pooled", 0);
        updateTargetContent.addColumn("poolsamplelist", 0);
        for (int i = 0; i < updateTargetContent.size(); ++i) {
            updateTargetContent.setNumber(i, "index", i);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        for (String position : positions) {
            filter.clear();
            filter.put("positionid", position);
            DataSet currpos = updateTargetContent.getFilteredDataSet(filter);
            if (currpos.size() != 1) continue;
            int index = currpos.getInt(0, "index");
            updateTargetContent.setString(index, "pooled", "N");
        }
        filter.clear();
        filter.put("pooled", "N");
        DataSet createChildList = updateTargetContent.getFilteredDataSet(filter);
        if (createChildList.getRowCount() > 0) {
            this.createChildSamplesToTransfer("Y", createChildList, sampleTypeId, false);
        }
        for (int i = 0; i < createChildList.size(); ++i) {
            int index = createChildList.getInt(i, "index");
            updateTargetContent.setString(index, "childsampleid", createChildList.getString(i, "childsampleid"));
            updateTargetContent.setString(index, "childtrackitemid", createChildList.getString(i, "childtrackitemid"));
        }
    }

    private DataSet doAslToArrayUpdates(String targetarraylist, String sourceindexmap, DataSet sourceASLContent, DataSet targetArrayItems, DataSet transferMethodDetails, String createChildSamples, String childSampleTypeId, SourceInputs sourceInputs, TargetInputs targetInputs, boolean disposeSourceWhenConsumed) throws SapphireException {
        String transferMap = transferMethodDetails.getValue(0, "transfermap", "");
        String[] transferMapItems = StringUtil.split(transferMap, "|");
        DataSet updateTargetAIContent = new DataSet();
        String[] sourceindices = StringUtil.split(sourceindexmap, ";");
        DataSet updateZoneDS = new DataSet();
        targetArrayItems.addColumn("arraylayoutzone", 0);
        HashMap<String, Integer> targetContentCounts = this.determineTargetContentCounts(transferMapItems);
        for (String currMapItem : transferMapItems) {
            int row;
            String[] currMap = StringUtil.split(currMapItem, "-");
            String[] targetItem = StringUtil.split(currMap[0], ",");
            int targetGrid = Integer.parseInt(targetItem[0]);
            int targetRow = Integer.parseInt(targetItem[1]);
            int targetCol = Integer.parseInt(targetItem[2]);
            String[] sourceItem = StringUtil.split(currMap[1], ",");
            int sourceGrid = Integer.parseInt(sourceItem[0]);
            int sourceRow = Integer.parseInt(sourceItem[1]);
            int sourceCol = Integer.parseInt(sourceItem[2]);
            boolean validSourceGrid = false;
            for (String currsi : sourceindices) {
                if (!sourceItem[0].equals(currsi)) continue;
                validSourceGrid = true;
                break;
            }
            if (!validSourceGrid) continue;
            HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
            filter.put("index", new BigDecimal(sourceGrid));
            filter.put("row", new BigDecimal(sourceRow));
            filter.put("column", new BigDecimal(sourceCol));
            DataSet matchSourceASLContent = sourceASLContent.getFilteredDataSet(filter);
            filter.clear();
            filter.put("index", new BigDecimal(targetGrid));
            filter.put("row", new BigDecimal(targetRow));
            filter.put("column", new BigDecimal(targetCol));
            DataSet matchTargetAI = targetArrayItems.getFilteredDataSet(filter);
            if (!OpalUtil.isNotEmpty(matchSourceASLContent) || !OpalUtil.isNotEmpty(matchTargetAI)) continue;
            String arrayid = matchTargetAI.getString(0, "gridid");
            String arraylayoutzone = matchSourceASLContent.getString(0, "arraylayoutzone");
            if (OpalUtil.isNotEmpty(arrayid) && OpalUtil.isNotEmpty(arraylayoutzone)) {
                row = updateZoneDS.addRow();
                updateZoneDS.setString(row, "arrayid", arrayid);
                updateZoneDS.setString(row, "arrayitemid", matchTargetAI.getString(0, "arrayitemid"));
                updateZoneDS.setString(row, "arraylayoutzone", arraylayoutzone);
                updateZoneDS.setString(row, "arraylayoutzonecolor", matchSourceASLContent.getString(0, "arraylayoutzonecolor", ""));
            }
            if (!matchSourceASLContent.getValue(0, "sampleid", "").isEmpty()) {
                if (matchSourceASLContent.getDouble(0, "qtycurrent") == 0.0) {
                    Trace.log("Source position:" + sourceRow + "," + sourceCol + " is empty and ignored");
                    continue;
                }
                updateTargetAIContent.copyRow(matchTargetAI, 0, 1);
                row = updateTargetAIContent.getRowCount() - 1;
                updateTargetAIContent.setString(row, "sampleid", matchSourceASLContent.getValue(0, "sampleid"));
                updateTargetAIContent.setString(row, "parentstorageunitid", matchSourceASLContent.getValue(0, "positionid"));
                updateTargetAIContent.setString(row, "arraylayoutzone", matchSourceASLContent.getValue(0, "arraylayoutzone", "(FullArray)"));
                updateTargetAIContent.setString(row, "arraylayoutzonecolor", matchSourceASLContent.getValue(0, "arraylayoutzonecolor", ""));
                int matchingsourceitem = matchSourceASLContent.getInt(0, "item");
                String sourceqtyunits = sourceASLContent.getValue(matchingsourceitem, "qtyunits", "");
                if (sourceASLContent.getValue(matchingsourceitem, "qtycurrent") != null) {
                    Trace.logDebug("Source ASL content qtycurrent is:" + sourceASLContent.getValue(matchingsourceitem, "qtycurrent"));
                    String sourceConcentration = sourceASLContent.getValue(matchingsourceitem, "concentration", "");
                    String sourceConcentrationUnits = sourceASLContent.getValue(matchingsourceitem, "concentrationunits", "");
                    int numOfContents = targetContentCounts.get(currMap[0]);
                    VolumeParams volumeParams = this.determineSourceVolumeToDecrement(sourceASLContent.getValue(matchingsourceitem, "qtycurrent"), sourceqtyunits, sourceConcentration, sourceConcentrationUnits, sourceInputs, targetInputs, numOfContents);
                    double sourcevolume = sourceASLContent.getDouble(matchingsourceitem, "qtycurrent", 0.0);
                    double sourcevolumetodecrement = volumeParams.sourceVolumeToDecrement;
                    double targetvolumetoupdate = volumeParams.targetVolumeToUpdate;
                    if (OpalUtil.isNotEmpty(sourceConcentration) && OpalUtil.isNotEmpty(sourceConcentrationUnits)) {
                        double sourceConcentrationN;
                        double targetConcentrationN;
                        String targetConcentration = String.valueOf(volumeParams.targetConcentration);
                        String targetConcentrationUnits = volumeParams.targetConcUnits;
                        if (OpalUtil.isNotEmpty(targetConcentration) && OpalUtil.isNotEmpty(targetConcentrationUnits) && (targetConcentrationN = this.m18NUtil.parseBigDecimal(targetConcentration).doubleValue()) != (sourceConcentrationN = this.m18NUtil.parseBigDecimal(sourceConcentration).doubleValue())) {
                            double diluentVolume = targetvolumetoupdate - sourcevolumetodecrement;
                            if (diluentVolume < 0.0) {
                                diluentVolume = 0.0;
                            }
                            updateTargetAIContent.setNumber(row, "diluentvolume", this.convertToBigDecimal(diluentVolume));
                            updateTargetAIContent.setString(row, "diluentvolumeunits", volumeParams.targetVolumeToUpdateUnits);
                            updateTargetAIContent.setNumber(row, "__trackitem_diluentvolume", updateTargetAIContent.getBigDecimal(row, "diluentvolume"));
                            updateTargetAIContent.setString(row, "__trackitem_diluentvolumeunits", volumeParams.targetVolumeToUpdateUnits);
                        }
                    }
                    if (matchSourceASLContent.getBigDecimal(0, "qtycurrent") != null) {
                        sourceASLContent.setNumber(matchingsourceitem, "qtycurrent", new BigDecimal(sourcevolume - sourcevolumetodecrement));
                        sourceASLContent.setString(matchingsourceitem, "qtyunits", sourceqtyunits);
                    }
                    if (targetvolumetoupdate == -1.0) continue;
                    updateTargetAIContent.setNumber(row, "volume", this.convertToBigDecimal(targetvolumetoupdate));
                    updateTargetAIContent.setValue(row, "totalvolumeincrement", "+" + targetvolumetoupdate);
                    updateTargetAIContent.setValue(row, "volumeunits", targetInputs.targetvolumeunits);
                    updateTargetAIContent.setValue(row, "totalvolumeunits", targetInputs.targetvolumeunits);
                    if (volumeParams.targetConcentration != -1.0) {
                        updateTargetAIContent.setNumber(row, "concentration", this.convertToBigDecimal(volumeParams.targetConcentration));
                        updateTargetAIContent.setValue(row, "concentrationunits", volumeParams.targetConcUnits);
                    }
                    if (createChildSamples.equals("N")) continue;
                    updateTargetAIContent.setNumber(row, "childqty", targetvolumetoupdate);
                    updateTargetAIContent.setString(row, "childqtyunits", targetInputs.targetvolumeunits);
                    if (volumeParams.targetConcentration == -1.0) continue;
                    updateTargetAIContent.setNumber(row, "childconc", this.convertToBigDecimal(volumeParams.targetConcentration));
                    updateTargetAIContent.setString(row, "childconcunits", volumeParams.targetConcUnits);
                    continue;
                }
                Trace.log("Did not find a matching item in Target map.");
                continue;
            }
            Trace.log("Did not find matching source content to be transferred: index:" + sourceGrid + " row::" + sourceRow + "  col::" + sourceCol);
        }
        if (!createChildSamples.equals("N") && updateTargetAIContent.getRowCount() > 0) {
            String newSampleIds = this.createChildSamplesToTransfer(createChildSamples, updateTargetAIContent, childSampleTypeId, true);
            updateTargetAIContent.addColumnValues("childsampleid", 0, newSampleIds, ";");
        }
        if (updateZoneDS.size() > 0) {
            DataSet addArrayZoneDS = new DataSet();
            HashSet<String> arrayZoneSet = new HashSet<String>();
            for (int i = 0; i < updateZoneDS.size(); ++i) {
                String arrayid = updateZoneDS.getString(i, "arrayid");
                String arraylayoutzone = updateZoneDS.getString(i, "arraylayoutzone");
                String key = arrayid + ";" + arraylayoutzone;
                if (!arrayZoneSet.add(key)) continue;
                int row = addArrayZoneDS.addRow();
                addArrayZoneDS.setString(row, "arrayid", arrayid);
                addArrayZoneDS.setString(row, "zone", arraylayoutzone);
                addArrayZoneDS.setString(row, "color", updateZoneDS.getString(i, "arraylayoutzonecolor"));
            }
            if (addArrayZoneDS.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_ArrayZone");
                props.setProperty("arrayid", addArrayZoneDS.getColumnValues("arrayid", ";"));
                props.setProperty("zone", addArrayZoneDS.getColumnValues("zone", ";"));
                props.setProperty("color", addArrayZoneDS.getColumnValues("color", ";"));
                props.setProperty("copies", String.valueOf(addArrayZoneDS.size()));
                props.setProperty("auditreason", this.auditInput.auditreason);
                props.setProperty("auditactivity", this.auditInput.auditactivity);
                props.setProperty("auditsignedflag", this.auditInput.auditsignedflag);
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                String arrayZoneId = props.getProperty("newkeyid1");
                addArrayZoneDS.addColumnValues("arrayzoneid", 0, arrayZoneId, ";");
                HashMap<String, String> filter = new HashMap<String, String>();
                DataSet arrayItemZoneInsertDS = new DataSet();
                String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                for (int i = 0; i < updateZoneDS.size(); ++i) {
                    String arrayid = updateZoneDS.getString(i, "arrayid");
                    String arraylayoutzone = updateZoneDS.getString(i, "arraylayoutzone");
                    filter.clear();
                    filter.put("arrayid", arrayid);
                    filter.put("zone", arraylayoutzone);
                    int row = addArrayZoneDS.findRow(filter);
                    if (row == -1) continue;
                    int z = arrayItemZoneInsertDS.addRow();
                    arrayItemZoneInsertDS.setString(z, "arrayzoneid", addArrayZoneDS.getString(row, "arrayzoneid"));
                    arrayItemZoneInsertDS.setString(z, "arrayitemid", updateZoneDS.getString(i, "arrayitemid"));
                    arrayItemZoneInsertDS.setString(z, "createby", sysuserid);
                    arrayItemZoneInsertDS.setDate(z, "createdt", DateTimeUtil.getNowCalendar());
                    arrayItemZoneInsertDS.setString(z, "createtool", "TransferArray");
                    arrayItemZoneInsertDS.setString(z, "modby", sysuserid);
                    arrayItemZoneInsertDS.setDate(z, "moddt", DateTimeUtil.getNowCalendar());
                    arrayItemZoneInsertDS.setString(z, "modtool", "TransferArray");
                }
                if (arrayItemZoneInsertDS.size() > 0) {
                    DataSetUtil.insert(this.database, arrayItemZoneInsertDS, "arrayitemarrayzone");
                }
            }
        }
        this.addASLContentItems(updateTargetAIContent, this.auditInput, !createChildSamples.equals("N"));
        this.incrementTargetArrayItemVolumes(updateTargetAIContent, this.auditInput);
        this.updateSourceASLVolumes(sourceASLContent, disposeSourceWhenConsumed);
        return updateTargetAIContent;
    }

    private DataSet doArrayToASLUpdates(String sourceindexmap, DataSet sourceContent, DataSet targetContent, DataSet transferMethodDetails, boolean createChildSamples, String childSampleTypeId, SourceInputs sourceInputs, TargetInputs targetInputs) throws SapphireException {
        String transferMap = transferMethodDetails.getValue(0, "transfermap", "");
        String[] transferMapItems = StringUtil.split(transferMap, "|");
        DataSet updateTargetContent = new DataSet();
        String[] sourceindices = StringUtil.split(sourceindexmap, ";");
        DataSet targetArrayLayoutZoneItems = null;
        if (!targetInputs.targetarraylayoutid.isEmpty()) {
            String sql = "select xpos, ypos from arraylayoutzoneitem where arraylayoutid = ? and arraylayoutversionid = ?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(targetInputs.targetarraylayoutid);
            safeSQL.addVar(targetInputs.targetarraylayoutversionid);
            targetArrayLayoutZoneItems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        DataSet updateZoneDS = new DataSet();
        HashMap<String, Integer> targetContentCounts = this.determineTargetContentCounts(transferMapItems);
        for (String currMapItem : transferMapItems) {
            String[] currMap = StringUtil.split(currMapItem, "-");
            String[] targetItem = StringUtil.split(currMap[0], ",");
            int targetGrid = Integer.parseInt(targetItem[0]);
            int targetRow = Integer.parseInt(targetItem[1]);
            int targetCol = Integer.parseInt(targetItem[2]);
            String[] sourceItem = StringUtil.split(currMap[1], ",");
            int sourceGrid = Integer.parseInt(sourceItem[0]);
            int sourceRow = Integer.parseInt(sourceItem[1]);
            int sourceCol = Integer.parseInt(sourceItem[2]);
            boolean validSourceGrid = false;
            for (String currsi : sourceindices) {
                if (!sourceItem[0].equals(currsi)) continue;
                validSourceGrid = true;
                break;
            }
            if (!validSourceGrid) continue;
            HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
            filter.put("index", new BigDecimal(sourceGrid));
            filter.put("row", new BigDecimal(sourceRow));
            filter.put("column", new BigDecimal(sourceCol));
            DataSet matchSourceContent = sourceContent.getFilteredDataSet(filter);
            filter.clear();
            filter.put("index", new BigDecimal(targetGrid));
            filter.put("row", new BigDecimal(targetRow));
            filter.put("column", new BigDecimal(targetCol));
            DataSet matchTargetContent = targetContent.getFilteredDataSet(filter);
            if (OpalUtil.isNotEmpty(matchSourceContent) && OpalUtil.isNotEmpty(matchTargetContent)) {
                if (!matchSourceContent.getString(0, "arraylayoutzone", "").isEmpty()) {
                    int row = updateZoneDS.addRow();
                    updateZoneDS.setString(row, "storageunitid", matchTargetContent.getString(0, "positionid"));
                    updateZoneDS.setString(row, "arraylayoutzone", matchSourceContent.getValue(0, "arraylayoutzone"));
                    updateZoneDS.setString(row, "arraylayoutzonecolor", matchSourceContent.getValue(0, "arraylayoutzonecolor"));
                }
                if (matchSourceContent.getValue(0, "sampleid", "").isEmpty()) continue;
                if (matchSourceContent.getDouble(0, "totalvolume") == 0.0) {
                    Trace.log("Source position:" + sourceRow + "," + sourceCol + " is empty and ignored");
                    continue;
                }
                if (matchTargetContent.getRowCount() <= 0) continue;
                if (!matchTargetContent.getValue(0, "sampleid", "").isEmpty()) {
                    Trace.log("Target position " + targetRow + "," + targetCol + " already has contents. Cannot perform transfer");
                    throw new SapphireException(this.getTranslationProcessor().translate("One or more target positions already have contents. Cannot perform transfer."));
                }
                if (!targetInputs.targetarraylayoutid.isEmpty() && targetArrayLayoutZoneItems != null) {
                    filter.clear();
                    filter.put("xpos", new BigDecimal(targetRow));
                    filter.put("ypos", new BigDecimal(targetCol));
                    DataSet ds = targetArrayLayoutZoneItems.getFilteredDataSet(filter);
                    if (ds.getRowCount() == 1) {
                        throw new SapphireException(this.getTranslationProcessor().translate("One or more target positions do not belong to any zone and attempts to transfer to those wells are invalid."));
                    }
                }
                for (int ms = 0; ms < matchSourceContent.getRowCount(); ++ms) {
                    updateTargetContent.copyRow(matchTargetContent, 0, 1);
                    int row = updateTargetContent.getRowCount() - 1;
                    updateTargetContent.setString(row, "sampleid", matchSourceContent.getValue(ms, "sampleid"));
                    updateTargetContent.setString(row, "trackitemid", matchSourceContent.getValue(ms, "trackitemid"));
                    updateTargetContent.setNumber(row, "qtycurrent", this.convertToBigDecimal(targetInputs.targetvolume));
                    updateTargetContent.setString(row, "qtyunits", targetInputs.targetvolumeunits);
                    updateTargetContent.setString(row, "sourcearrayid", matchSourceContent.getValue(ms, "gridid"));
                    updateTargetContent.setString(row, "sourcearrayitemid", matchSourceContent.getValue(ms, "arrayitemid"));
                    updateTargetContent.setString(row, "sourcelabel", matchSourceContent.getValue(ms, "labelcol") + matchSourceContent.getValue(0, "labelrow"));
                    updateTargetContent.setString(row, "sourcesampleid", matchSourceContent.getValue(ms, "sampleid"));
                    updateTargetContent.setString(row, "sourcetrackitemid", matchSourceContent.getValue(ms, "trackitemid"));
                    updateTargetContent.setString(row, "targetboxstorageunitid", matchTargetContent.getValue(0, "gridid"));
                    updateTargetContent.setString(row, "targetstorageunitid", matchTargetContent.getValue(0, "positionid"));
                    updateTargetContent.setString(row, "targetlabel", matchTargetContent.getValue(0, "labelrow") + matchTargetContent.getValue(0, "labelcol"));
                    if (ms > 0) {
                        updateTargetContent.setString(row, "multiplesourcecontent", "Y");
                    }
                    int matchingsourceitem = matchSourceContent.getInt(ms, "item");
                    String sourceqtyunits = sourceContent.getValue(matchingsourceitem, "totalvolumeunits", "");
                    if (sourceContent.getValue(matchingsourceitem, "totalvolume") == null) continue;
                    String sourceConcentration = sourceContent.getValue(matchingsourceitem, "concentration", "");
                    String sourceConcentrationUnits = sourceContent.getValue(matchingsourceitem, "concentrationunits", "");
                    int numOfContents = targetContentCounts.get(currMap[0]);
                    VolumeParams volumeParams = this.determineSourceVolumeToDecrement(sourceContent.getValue(matchingsourceitem, "totalvolume"), sourceqtyunits, sourceConcentration, sourceConcentrationUnits, sourceInputs, targetInputs, numOfContents);
                    double sourceVolume = sourceContent.getDouble(matchingsourceitem, "totalvolume", 0.0);
                    double sourceVolumeToDecrement = volumeParams.sourceVolumeToDecrement;
                    String sourceVolumeToDecrementUnits = volumeParams.sourceVolumeToDecrementUnits;
                    double targetVolumeToUpdate = volumeParams.targetVolumeToUpdate;
                    String targetVolumeToUpdateUnits = volumeParams.targetVolumeToUpdateUnits;
                    double diluentVolume = -1.0;
                    String diluentVolumeUnits = "";
                    if (OpalUtil.isNotEmpty(sourceConcentration) && OpalUtil.isNotEmpty(sourceConcentrationUnits)) {
                        double sourceConcentrationN;
                        double targetConcentrationN;
                        String targetConcentration = String.valueOf(volumeParams.targetConcentration);
                        String targetConcentrationUnits = volumeParams.targetConcUnits;
                        if (OpalUtil.isNotEmpty(targetConcentration) && OpalUtil.isNotEmpty(targetConcentrationUnits) && (targetConcentrationN = this.m18NUtil.parseBigDecimal(targetConcentration).doubleValue()) != (sourceConcentrationN = this.m18NUtil.parseBigDecimal(sourceConcentration).doubleValue())) {
                            double diluentVolumeToDecrement = sourceVolumeToDecrement;
                            if (!targetVolumeToUpdateUnits.equals(sourceVolumeToDecrementUnits)) {
                                diluentVolumeToDecrement = this.m18NUtil.parseBigDecimal(ConvertUnits.convertUnits(this.getQueryProcessor(), sourceVolumeToDecrementUnits, targetVolumeToUpdateUnits, String.valueOf(sourceVolumeToDecrement))).doubleValue();
                            }
                            diluentVolume = targetVolumeToUpdate - diluentVolumeToDecrement;
                            diluentVolumeUnits = volumeParams.targetVolumeToUpdateUnits;
                        }
                    }
                    if (diluentVolume >= 0.0) {
                        updateTargetContent.setNumber(row, "__trackitem_diluentvolume", this.convertToBigDecimal(diluentVolume));
                        updateTargetContent.setString(row, "__trackitem_diluentvolumeunits", diluentVolumeUnits);
                    }
                    sourceContent.setNumber(matchingsourceitem, "totalvolume", new BigDecimal(sourceVolume - sourceVolumeToDecrement));
                    sourceContent.setString(matchingsourceitem, "totalvolumeunits", sourceqtyunits);
                    if (targetVolumeToUpdate == -1.0 || !createChildSamples) continue;
                    updateTargetContent.setNumber(row, "childqty", this.convertToBigDecimal(targetVolumeToUpdate));
                    updateTargetContent.setString(row, "childqtyunits", targetInputs.targetvolumeunits);
                    if (volumeParams.targetConcentration == -1.0) continue;
                    updateTargetContent.setNumber(row, "childconc", this.convertToBigDecimal(volumeParams.targetConcentration));
                    updateTargetContent.setString(row, "childconcunits", volumeParams.targetConcUnits);
                }
                continue;
            }
            Trace.log("Did not find matching source content to be transferred: index:" + sourceGrid + " row::" + sourceRow + "  col::" + sourceCol);
        }
        if (updateTargetContent.getRowCount() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Transfer failed, no contents to be transferred"));
        }
        if (!createChildSamples) {
            PropertyList edittrackitemProps = new PropertyList();
            edittrackitemProps.setProperty("trackitemid", updateTargetContent.getColumnValues("trackitemid", ";"));
            edittrackitemProps.setProperty("currentstorageunitid", updateTargetContent.getColumnValues("positionid", ";"));
            edittrackitemProps.setProperty("qtycurrent", updateTargetContent.getColumnValues("qtycurrent", ";"));
            edittrackitemProps.setProperty("qtyunits", updateTargetContent.getColumnValues("qtyunits", ";"));
            this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), edittrackitemProps);
            this.updateSourceArrayItemVolumes(sourceContent);
        } else {
            if (updateTargetContent.getRowCount() > 0) {
                this.handleRegularChildren(updateTargetContent, childSampleTypeId);
                updateTargetContent = this.handlePooledSamples(updateTargetContent, false);
            }
            this.updateSourceArrayItemVolumes(sourceContent);
        }
        if (OpalUtil.isNotEmpty(updateZoneDS)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "StorageUnitSDC");
            props.setProperty("keyid1", updateZoneDS.getColumnValues("storageunitid", ";"));
            props.setProperty("arraylayoutzone", updateZoneDS.getColumnValues("arraylayoutzone", ";"));
            props.setProperty("arraylayoutzonecolor", updateZoneDS.getColumnValues("arraylayoutzonecolor", ";"));
            props.setProperty("__syncoperation", "Y");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        return updateTargetContent;
    }

    private DataSet handlePooledSamples(DataSet updateTargetContent, boolean decrementparentsamples) throws SapphireException {
        String allPositions = PropertyUtil.getUniqueItems(updateTargetContent.getColumnValues("positionid", ";"));
        String[] positions = StringUtil.split(allPositions, ";");
        DataSet pooledTargetContent = new DataSet();
        StringBuilder pooledSampleIds = new StringBuilder();
        StringBuilder decrementQuantities = new StringBuilder();
        StringBuilder pooledVolume = new StringBuilder();
        StringBuilder pooledConc = new StringBuilder();
        StringBuilder pooledVolumeUnits = new StringBuilder();
        StringBuilder pooledConcUnits = new StringBuilder();
        StringBuilder indexlist = new StringBuilder();
        for (String position : positions) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("positionid", position);
            DataSet currpos = updateTargetContent.getFilteredDataSet(map);
            if (currpos.size() > 1) {
                indexlist.append(pooledTargetContent.size()).append("|");
                pooledSampleIds.append(currpos.getColumnValues("sampleid", ";")).append("|");
                for (int c = 0; c < currpos.getRowCount(); ++c) {
                    if (c != 0) {
                        decrementQuantities.append(";").append("|");
                    }
                    if (decrementparentsamples) {
                        decrementQuantities.append(currpos.getValue(c, "childqty", "0")).append("|");
                        continue;
                    }
                    decrementQuantities.append("0").append("|");
                }
                double totalQuantity = this.calcTotalVolume(currpos);
                if (totalQuantity != -1.0) {
                    pooledVolume.append(this.m18NUtil.format(BigDecimal.valueOf(totalQuantity))).append("|");
                } else {
                    pooledVolume.append("|");
                }
                pooledVolumeUnits.append(currpos.getString(0, "qtyunits")).append("|");
                double netConc = this.calcNetConcentration(currpos);
                if (netConc != -1.0) {
                    pooledConc.append(this.m18NUtil.format(BigDecimal.valueOf(netConc))).append("|");
                } else {
                    pooledConc.append("|");
                }
                pooledConcUnits.append(currpos.getString(0, "childconcunits", "")).append("|");
                pooledTargetContent.copyRow(currpos, 0, 1);
                int c = pooledTargetContent.getRowCount() - 1;
                pooledTargetContent.setNumber(c, "qtycurrent", this.convertToBigDecimal(totalQuantity));
                pooledTargetContent.setNumber(c, "childconc", this.convertToBigDecimal(netConc));
                continue;
            }
            pooledTargetContent.copyRow(currpos, 0, 1);
        }
        if (pooledSampleIds.length() > 0) {
            pooledSampleIds.setLength(pooledSampleIds.length() - 1);
        }
        if (decrementQuantities.length() > 0) {
            decrementQuantities.setLength(decrementQuantities.length() - 1);
        }
        if (pooledVolume.length() > 0) {
            pooledVolume.setLength(pooledVolume.length() - 1);
        }
        if (pooledConc.length() > 0) {
            pooledConc.setLength(pooledConc.length() - 1);
        }
        if (pooledVolumeUnits.length() > 0) {
            pooledVolumeUnits.setLength(pooledVolumeUnits.length() - 1);
        }
        if (pooledConcUnits.length() > 0) {
            pooledConcUnits.setLength(pooledConcUnits.length() - 1);
        }
        if (indexlist.length() > 0) {
            indexlist.setLength(indexlist.length() - 1);
        }
        if (!pooledSampleIds.toString().isEmpty()) {
            String[] ret = this.createPooledSample(pooledSampleIds.toString(), decrementQuantities.toString(), pooledVolume.toString(), pooledVolumeUnits.toString(), pooledConc.toString(), pooledConcUnits.toString(), decrementparentsamples);
            String pooledChildren = ret[0];
            String[] indices = StringUtil.split(indexlist.toString(), "|");
            String[] pooledChildSampleId = StringUtil.split(pooledChildren, "|");
            String trackitemid = ret[1];
            String[] ti = StringUtil.split(trackitemid, "|");
            for (int i = 0; i < pooledChildSampleId.length; ++i) {
                int pos = Integer.parseInt(indices[i]);
                pooledTargetContent.setString(pos, "childsampleid", pooledChildSampleId[i]);
                pooledTargetContent.setString(pos, "childtrackitemid", ti[i]);
            }
        }
        for (int i = 0; i < pooledTargetContent.size(); ++i) {
            updateTargetContent.copyRow(pooledTargetContent, i, 1);
        }
        PropertyList props = new PropertyList();
        props.setProperty("trackitemid", pooledTargetContent.getColumnValues("childtrackitemid", ";"));
        props.setProperty("currentstorageunitid", pooledTargetContent.getColumnValues("positionid", ";"));
        this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
        return pooledTargetContent;
    }

    private PropertyList findSourceConc(String targetarrayitemidlist, ArrayList targetMap) {
        String[] arrayitemid = StringUtil.split(targetarrayitemidlist, ";");
        ArrayList<String> sourceConcsList = new ArrayList<String>();
        ArrayList<String> sourceConcUnitsList = new ArrayList<String>();
        for (String anArrayitemid : arrayitemid) {
            String currConc = "";
            String currConcUnits = "";
            for (Object aTargetMap : targetMap) {
                TargetArrayMap map = (TargetArrayMap)aTargetMap;
                if (map == null || map.targetarrayitemid == null || !map.targetarrayitemid.equals(anArrayitemid)) continue;
                currConc = map.sourceconcentration != null ? map.sourceconcentration : "";
                if (map.sourceconcentrationunits != null) {
                    currConcUnits = map.sourceconcentrationunits;
                    break;
                }
                currConcUnits = "";
                break;
            }
            sourceConcsList.add(currConc);
            sourceConcUnitsList.add(currConcUnits);
        }
        PropertyList ret = new PropertyList();
        ret.setProperty("concentration", OpalUtil.toDelimitedString(sourceConcsList, ";"));
        ret.setProperty("concentrationunits", OpalUtil.toDelimitedString(sourceConcUnitsList, ";"));
        return ret;
    }

    private String createTargetASLGrid(PropertyList properties, int targetCount, String targetarraytypeid, String targetarraylayoutid, String targetarraylayoutversionid) throws ActionException {
        PropertyList props = new PropertyList();
        Trace.logDebug("Target Arraytype id used to create storageunit:" + targetarraytypeid);
        String storageunittype = targetarraytypeid.substring(4);
        props.setProperty("storageunittype", storageunittype);
        props.setProperty("copies", String.valueOf(targetCount));
        props.setProperty("trackitem_custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        props.setProperty("trackitem_custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
        if (!targetarraylayoutid.isEmpty() && !targetarraylayoutversionid.isEmpty()) {
            props.setProperty("arraylayoutid", targetarraylayoutid);
            props.setProperty("arraylayoutversionid", targetarraylayoutversionid);
        }
        this.getActionProcessor().processActionClass(AddStorageUnit.class.getName(), props);
        properties.setProperty("newsuid", props.getProperty("storageunitid"));
        properties.setProperty("newboxid", props.getProperty("linkkeyid1"));
        return props.getProperty("newkeyid1");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private VolumeParams determineSourceVolumeToDecrement(String sourceCurrentQty, String sourceCurrentQtyUnits, String sourceConc, String sourceConcUnits, SourceInputs sourceInputs, TargetInputs targetInputs, int numOfContents) throws SapphireException {
        M18NUtil m18nUtil = new M18NUtil(this.connectionInfo);
        VolumeParams volumeParams = new VolumeParams();
        double overrideSourceVolumeToDecrement = -1.0;
        if (sourceInputs.sourcevolumetodecrement != null && !sourceInputs.sourcevolumetodecrementunits.isEmpty()) {
            if (sourceCurrentQtyUnits.isEmpty() || sourceInputs.sourcevolumetodecrementunits.equals(sourceCurrentQtyUnits)) {
                overrideSourceVolumeToDecrement = m18nUtil.parseBigDecimal(sourceInputs.sourcevolumetodecrement).doubleValue();
            } else if (OpalUtil.isNotEmpty(sourceInputs.sourcevolumetodecrement)) {
                BigDecimal bdval = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(sourceInputs.sourcevolumetodecrement), sourceInputs.sourcevolumetodecrementunits, sourceCurrentQtyUnits);
                overrideSourceVolumeToDecrement = bdval.doubleValue();
                sourceInputs.sourcevolumetodecrement = this.m18NUtil.format(bdval);
                sourceInputs.sourcevolumetodecrementunits = sourceCurrentQtyUnits;
            }
        }
        String targetconc = targetInputs.targetconc;
        String targetconcunits = targetInputs.targetconcunits;
        if (targetInputs.targetconc.isEmpty()) {
            targetconc = sourceConc;
            targetconcunits = sourceConcUnits;
        }
        if (sourceConc.isEmpty()) {
            double dconvertedtargetvolume = -1.0;
            if (!targetInputs.targetvolume.isEmpty()) {
                dconvertedtargetvolume = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(targetInputs.targetvolume), targetInputs.targetvolumeunits, sourceCurrentQtyUnits).doubleValue();
            }
            double dsourcevolume = -1.0;
            if (!sourceCurrentQty.isEmpty()) {
                dsourcevolume = m18nUtil.parseBigDecimal(sourceCurrentQty).doubleValue();
            }
            if (dsourcevolume == -1.0) {
                volumeParams.sourceVolumeToDecrement = 0.0;
                volumeParams.targetVolumeToUpdate = m18nUtil.parseBigDecimal(targetInputs.targetvolume).doubleValue();
                volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                if (targetconc.isEmpty()) return volumeParams;
                volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                volumeParams.targetConcUnits = targetInputs.targetconcunits;
                return volumeParams;
            } else if (dsourcevolume >= dconvertedtargetvolume / (double)numOfContents) {
                switch (sourceInputs.quantityhandlingflag) {
                    case "D": {
                        if (overrideSourceVolumeToDecrement < 0.0) {
                            volumeParams.sourceVolumeToDecrement = dconvertedtargetvolume / (double)numOfContents;
                            volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        } else {
                            volumeParams.sourceVolumeToDecrement = overrideSourceVolumeToDecrement;
                            volumeParams.sourceVolumeToDecrementUnits = sourceInputs.sourcevolumetodecrementunits;
                        }
                        volumeParams.targetVolumeToUpdate = m18nUtil.parseBigDecimal(targetInputs.targetvolume).doubleValue() / (double)numOfContents;
                        volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                        if (targetconc.isEmpty()) {
                            volumeParams.targetConcentration = -1.0;
                            return volumeParams;
                        }
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                        volumeParams.targetConcUnits = targetconcunits;
                        return volumeParams;
                    }
                    case "0": {
                        volumeParams.sourceVolumeToDecrement = dsourcevolume;
                        volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        volumeParams.targetVolumeToUpdate = m18nUtil.parseBigDecimal(targetInputs.targetvolume).doubleValue() / (double)numOfContents;
                        volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                        if (targetconc.isEmpty()) {
                            volumeParams.targetConcentration = -1.0;
                            return volumeParams;
                        }
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                        volumeParams.targetConcUnits = targetconcunits;
                        return volumeParams;
                    }
                    case "S": {
                        volumeParams.sourceVolumeToDecrement = dsourcevolume;
                        volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        volumeParams.targetVolumeToUpdate = dsourcevolume;
                        volumeParams.targetVolumeToUpdateUnits = sourceCurrentQtyUnits;
                        if (targetconc.isEmpty()) {
                            volumeParams.targetConcentration = -1.0;
                            return volumeParams;
                        }
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                        volumeParams.targetConcUnits = targetconcunits;
                        return volumeParams;
                    }
                }
                return volumeParams;
            } else {
                if (sourceInputs.validatesourcevolume) throw new SapphireException("Source volume insufficient to meet target volume requirements");
                if (overrideSourceVolumeToDecrement < 0.0) {
                    volumeParams.sourceVolumeToDecrement = dsourcevolume;
                    volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                } else {
                    volumeParams.sourceVolumeToDecrement = overrideSourceVolumeToDecrement;
                    volumeParams.sourceVolumeToDecrementUnits = sourceInputs.sourcevolumetodecrementunits;
                }
                volumeParams.targetVolumeToUpdate = dsourcevolume;
                volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                if (targetconc.isEmpty()) {
                    volumeParams.targetConcentration = -1.0;
                    return volumeParams;
                } else {
                    volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                    volumeParams.targetConcUnits = sourceConcUnits;
                }
            }
            return volumeParams;
        } else {
            String convertedtargetvolume = "";
            double dconvertedtargetvolume = -1.0;
            if (!targetInputs.targetvolume.isEmpty()) {
                convertedtargetvolume = m18nUtil.format(OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(targetInputs.targetvolume), targetInputs.targetvolumeunits, sourceCurrentQtyUnits));
                if (OpalUtil.isNotEmpty(targetconc)) {
                    double dtargetConc = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                    double dsourceConc = OpalUtil.convertUnit(this.connectionInfo, this.m18NUtil.parseBigDecimal(sourceConc), sourceConcUnits, targetconcunits).doubleValue();
                    dconvertedtargetvolume = m18nUtil.parseBigDecimal(convertedtargetvolume).doubleValue() * dtargetConc / dsourceConc;
                }
            }
            double dsourcevolume = -1.0;
            if (!sourceCurrentQty.isEmpty()) {
                dsourcevolume = m18nUtil.parseBigDecimal(sourceCurrentQty).doubleValue();
            }
            if (dsourcevolume == -1.0) {
                volumeParams.sourceVolumeToDecrement = 0.0;
                volumeParams.targetVolumeToUpdate = dconvertedtargetvolume;
                volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                if (!OpalUtil.isNotEmpty(targetconc)) return volumeParams;
                volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                volumeParams.targetConcUnits = targetInputs.targetconcunits;
                return volumeParams;
            } else if (dsourcevolume >= dconvertedtargetvolume / (double)numOfContents) {
                switch (sourceInputs.quantityhandlingflag) {
                    case "D": {
                        if (overrideSourceVolumeToDecrement < 0.0) {
                            volumeParams.sourceVolumeToDecrement = dconvertedtargetvolume / (double)numOfContents;
                            volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        } else {
                            volumeParams.sourceVolumeToDecrement = overrideSourceVolumeToDecrement;
                            volumeParams.sourceVolumeToDecrementUnits = sourceInputs.sourcevolumetodecrementunits;
                        }
                        if (targetInputs.targetvolume.isEmpty()) {
                            volumeParams.targetVolumeToUpdate = -1.0;
                        } else {
                            volumeParams.targetVolumeToUpdate = m18nUtil.parseBigDecimal(targetInputs.targetvolume).doubleValue() / (double)numOfContents;
                            volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                        }
                        if (targetconc.isEmpty()) {
                            if (sourceConc.isEmpty()) {
                                volumeParams.targetConcentration = -1.0;
                                return volumeParams;
                            }
                            volumeParams.targetConcentration = m18nUtil.parseBigDecimal(sourceConc).doubleValue();
                            volumeParams.targetConcUnits = sourceConcUnits;
                            return volumeParams;
                        }
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                        volumeParams.targetConcUnits = targetconcunits;
                        return volumeParams;
                    }
                    case "O": {
                        volumeParams.sourceVolumeToDecrement = dsourcevolume;
                        volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        volumeParams.targetVolumeToUpdate = m18nUtil.parseBigDecimal(targetInputs.targetvolume).doubleValue();
                        volumeParams.targetVolumeToUpdateUnits = targetInputs.targetvolumeunits;
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(targetconc).doubleValue();
                        volumeParams.targetConcUnits = targetconcunits;
                        return volumeParams;
                    }
                    case "S": {
                        volumeParams.sourceVolumeToDecrement = dsourcevolume;
                        volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                        volumeParams.targetVolumeToUpdate = dsourcevolume;
                        volumeParams.targetVolumeToUpdateUnits = sourceCurrentQtyUnits;
                        volumeParams.targetConcentration = m18nUtil.parseBigDecimal(sourceConc).doubleValue();
                        volumeParams.targetConcUnits = sourceConcUnits;
                        return volumeParams;
                    }
                }
                return volumeParams;
            } else {
                if (sourceInputs.validatesourcevolume) throw new SapphireException("Source volume insufficient to meet target volume requirements");
                if (overrideSourceVolumeToDecrement < 0.0) {
                    volumeParams.sourceVolumeToDecrement = Math.max(dsourcevolume, 0.0);
                    volumeParams.sourceVolumeToDecrementUnits = sourceCurrentQtyUnits;
                } else {
                    volumeParams.sourceVolumeToDecrement = overrideSourceVolumeToDecrement;
                    volumeParams.sourceVolumeToDecrementUnits = sourceInputs.sourcevolumetodecrementunits;
                }
                volumeParams.targetVolumeToUpdate = dsourcevolume;
                volumeParams.targetVolumeToUpdateUnits = sourceCurrentQtyUnits;
                volumeParams.targetConcentration = m18nUtil.parseBigDecimal(sourceConc).doubleValue();
                volumeParams.targetConcUnits = sourceConcUnits;
                if (dsourcevolume != -1.0) return volumeParams;
                volumeParams.targetVolumeToUpdate = -1.0;
            }
        }
        return volumeParams;
    }

    private void updateSourceASLVolumes(DataSet sourceASLContent, boolean disposeSampleWhenConsumed) throws SapphireException {
        PropertyList editTrackItem = new PropertyList();
        editTrackItem.setProperty("trackitemid", sourceASLContent.getColumnValues("trackitemid", ";"));
        editTrackItem.setProperty("sdcid", "Sample");
        editTrackItem.setProperty("keyid1", sourceASLContent.getColumnValues("sampleid", ";"));
        editTrackItem.setProperty("qtycurrent", sourceASLContent.getColumnValues("qtycurrent", ";"));
        editTrackItem.setProperty("qtyunits", sourceASLContent.getColumnValues("qtyunits", ";"));
        this.getActionProcessor().processAction("EditTrackItem", "1", editTrackItem);
        Trace.log("Updated source trackitems volume by contents transferred");
        DataSet emptySamples = new DataSet();
        for (int i = 0; i < sourceASLContent.getRowCount(); ++i) {
            double vol = sourceASLContent.getDouble(i, "qtycurrent");
            if (vol != 0.0) continue;
            emptySamples.copyRow(sourceASLContent, i, 1);
        }
        if (disposeSampleWhenConsumed && emptySamples.getRowCount() > 0) {
            List<String> emptySampleList = OpalUtil.toUniqueList(emptySamples.getColumnValues("sampleid", ";"), ";");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT trackitemid FROM trackitem WHERE linkkeyid1 in ( " + safeSQL.addIn(emptySampleList) + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            PropertyList tiEdit = new PropertyList();
            tiEdit.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
            tiEdit.setProperty("currentstorageunitid", StringUtil.repeat("(null)", ds.size(), ";"));
            this.getActionProcessor().processAction("EditTrackItem", "1", tiEdit);
            PropertyList sampleEdit = new PropertyList();
            sampleEdit.setProperty("sdcid", "Sample");
            sampleEdit.setProperty("keyid1", emptySamples.getColumnValues("sampleid", ";"));
            sampleEdit.setProperty("storagestatus", "Disposed");
            this.getActionProcessor().processAction("EditSDI", "1", sampleEdit);
        }
    }

    private void updateSourceArrayItemVolumes(DataSet sourceContent) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("multiplecontent", "N");
        sourceContent = sourceContent.getFilteredDataSet(filter);
        PropertyList editArrayItems = new PropertyList();
        editArrayItems.setProperty("arrayid", sourceContent.getColumnValues("gridid", ";"));
        editArrayItems.setProperty("arrayitemid", sourceContent.getColumnValues("arrayitemid", ";"));
        editArrayItems.setProperty("totalvol", sourceContent.getColumnValues("totalvolume", ";"));
        editArrayItems.setProperty("totalvolunits", sourceContent.getColumnValues("totalvolumeunits", ";"));
        editArrayItems.setProperty("totalconc", sourceContent.getColumnValues("concentration", ";"));
        editArrayItems.setProperty("totalconcunits", sourceContent.getColumnValues("concentrationunits", ";"));
        this.getActionProcessor().processAction("EditArrayItem", "1", editArrayItems);
    }

    private String createChildSamplesToTransfer(String createChildSamples, DataSet targetContentList, String childSampleTypeId, boolean inArray) throws SapphireException {
        String parentSampleIds = targetContentList.getColumnValues("sampleid", ";");
        if (createChildSamples.equals("Y")) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("parent_depleteflag", "N");
            actionProps.setProperty("parent_sampleid", parentSampleIds);
            actionProps.setProperty("child_copies", StringUtil.repeat("1", targetContentList.getRowCount(), ";"));
            if (inArray && !this.defaultchildsamplestoragestatus.equals("Use Biobanking Policy")) {
                actionProps.setProperty("child_storagestatus", StringUtil.repeat(this.defaultchildsamplestoragestatus, targetContentList.getRowCount(), ";"));
                actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                actionProps.setProperty("childcolumn_receiverequiredflag", "N");
            }
            actionProps.setProperty("child_quantity", targetContentList.getColumnValues("childqty", ";"));
            actionProps.setProperty("child_unit", targetContentList.getColumnValues("childqtyunits", ";"));
            if (inArray && this.defaultchildsamplestatus != null && !"Use Biobanking Policy".equals(this.defaultchildsamplestatus)) {
                if (!this.defaultchildsamplestatus.equals("Inherit from Parent")) {
                    actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(this.defaultchildsamplestatus, targetContentList.getRowCount(), ";"));
                } else {
                    actionProps.setProperty("childsamplestatus", "inherit");
                }
            }
            actionProps.setProperty("childcolumn_concentration", targetContentList.getColumnValues("childconc", ";"));
            actionProps.setProperty("childcolumn_concentrationunits", targetContentList.getColumnValues("childconcunits", ";"));
            actionProps.setProperty("__trackitem_diluentvolume", targetContentList.getColumnValues("__trackitem_diluentvolume", ";"));
            actionProps.setProperty("__trackitem_diluentvolumeunits", targetContentList.getColumnValues("__trackitem_diluentvolumeunits", ";"));
            actionProps.setProperty("__trackitem_currentstorageunitid", targetContentList.getColumnValues("positionid", ";"));
            if (childSampleTypeId != null && childSampleTypeId.length() > 0) {
                actionProps.setProperty("mode", "Derivative");
                actionProps.setProperty("child_sampletypeid", StringUtil.repeat(childSampleTypeId, targetContentList.getRowCount(), ";"));
            } else {
                actionProps.setProperty("mode", "Aliquot");
            }
            actionProps.setProperty("propsmatch", "Y");
            if (OpalUtil.isNotEmpty(this.additionalSampleData)) {
                try {
                    JSONArray array = new JSONArray(this.additionalSampleData);
                    for (int i = 0; i < array.length(); ++i) {
                        JSONObject o = array.getJSONObject(i);
                        if (!o.has("column") || !o.has("value")) continue;
                        String column = o.getString("column");
                        String value = o.getString("value");
                        if (!OpalUtil.isNotEmpty(column) || !OpalUtil.isNotEmpty(value)) continue;
                        String columnid = column.substring(column.indexOf(".") + 1);
                        if (column.startsWith("s_sample.")) {
                            actionProps.setProperty("childcolumn_" + columnid, value);
                            continue;
                        }
                        if (column.startsWith("trackitem.")) {
                            actionProps.setProperty("__trackitem_" + columnid, value);
                            continue;
                        }
                        if (!column.startsWith("s_samplefamily")) continue;
                        actionProps.setProperty("__samplefamily_" + columnid, value);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.getActionProcessor().processActionClass(MultiSampleChild.class.getName(), actionProps);
            targetContentList.addColumnValues("childsampleid", 0, actionProps.getProperty("newkeyid1"), ";");
            targetContentList.addColumnValues("childtrackitemid", 0, actionProps.getProperty("newtrackitemid"), ";");
            return actionProps.getProperty("newkeyid1");
        }
        List<String> distinctParentList = OpalUtil.toUniqueList(parentSampleIds, ";");
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("parent_depleteflag", "N");
        actionProps.setProperty("parent_sampleid", OpalUtil.toDelimitedString(distinctParentList, ";"));
        actionProps.setProperty("child_copies", StringUtil.repeat("1", distinctParentList.size(), ";"));
        if (inArray && !this.defaultchildsamplestoragestatus.equals("Use Biobanking Policy")) {
            actionProps.setProperty("child_storagestatus", StringUtil.repeat(this.defaultchildsamplestoragestatus, distinctParentList.size(), ";"));
            actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
            actionProps.setProperty("childcolumn_receiverequiredflag", "N");
        }
        if (inArray && this.defaultchildsamplestatus != null && !"Use Biobanking Policy".equals(this.defaultchildsamplestatus)) {
            if (!this.defaultchildsamplestatus.equals("Inherit from Parent")) {
                actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(this.defaultchildsamplestatus, distinctParentList.size(), ";"));
            } else {
                actionProps.setProperty("childsamplestatus", "inherit");
            }
        }
        if (childSampleTypeId != null && !childSampleTypeId.isEmpty()) {
            actionProps.setProperty("mode", "Derivative");
            actionProps.setProperty("child_sampletypeid", StringUtil.repeat(childSampleTypeId, distinctParentList.size(), ";"));
        } else {
            actionProps.setProperty("mode", "Aliquot");
        }
        actionProps.setProperty("propsmatch", "Y");
        if (OpalUtil.isNotEmpty(this.additionalSampleData)) {
            try {
                JSONArray array = new JSONArray(this.additionalSampleData);
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject o = array.getJSONObject(i);
                    if (!o.has("column") || !o.has("value")) continue;
                    String column = o.getString("column");
                    String value = o.getString("value");
                    if (!OpalUtil.isNotEmpty(column) || !OpalUtil.isNotEmpty(value)) continue;
                    String columnid = column.substring(column.indexOf(".") + 1);
                    if (column.startsWith("s_sample.")) {
                        actionProps.setProperty("childcolumn_" + columnid, value);
                        continue;
                    }
                    if (column.startsWith("trackitem.")) {
                        actionProps.setProperty("__trackitem_" + columnid, value);
                        continue;
                    }
                    if (!column.startsWith("s_samplefamily")) continue;
                    actionProps.setProperty("__samplefamily_" + columnid, value);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        this.getActionProcessor().processActionClass(MultiSampleChild.class.getName(), actionProps);
        String childsamplelist = actionProps.getProperty("newkeyid1");
        String[] childSampleListItems = StringUtil.split(childsamplelist, ";");
        String[] trackItems = StringUtil.split(actionProps.getProperty("newtrackitemid", ""), ";");
        for (int i = 0; i < targetContentList.getRowCount(); ++i) {
            String parentsampleid = targetContentList.getString(i, "sampleid", "");
            if (parentsampleid.isEmpty()) continue;
            int num = distinctParentList.indexOf(parentsampleid);
            String childsampleid = childSampleListItems[num];
            targetContentList.setString(i, "childsampleid", childsampleid);
            targetContentList.setString(i, "childtrackitemid", trackItems[num]);
        }
        return targetContentList.getColumnValues("childsampleid", ";");
    }

    private double calcTotalVolume(DataSet sampleInfo) throws SapphireException {
        double totalqty = -1.0;
        try {
            String units = sampleInfo.getString(0, "childqtyunits");
            if (sampleInfo.getValue(0, "childqty", "").length() > 0) {
                totalqty = sampleInfo.getDouble(0, "childqty");
            }
            if (totalqty != -1.0) {
                for (int i = 1; i < sampleInfo.getRowCount(); ++i) {
                    if (sampleInfo.getValue(i, "multiplesourcecontent", "").equals("Y")) continue;
                    String currunits = sampleInfo.getString(i, "childqtyunits");
                    if (units.equals(currunits)) {
                        totalqty += sampleInfo.getDouble(i, "childqty");
                        continue;
                    }
                    double currentqty = sampleInfo.getDouble(i, "childqty");
                    totalqty += UnitsUtil.unitConv(this.getConnectionId(), new BigDecimal(currentqty), currunits, units).doubleValue();
                }
            }
        }
        catch (NumberFormatException e) {
            return -1.0;
        }
        return totalqty;
    }

    private double calcNetConcentration(DataSet sampleInfo) throws SapphireException {
        String units = sampleInfo.getString(0, "childconcunits", "");
        double netconc = -1.0;
        if (sampleInfo.getDouble(0, "childconc") > 0.0) {
            netconc = sampleInfo.getDouble(0, "childconc");
        }
        if (netconc > 0.0) {
            for (int i = 1; i < sampleInfo.getRowCount(); ++i) {
                String currunits = sampleInfo.getString(i, "childconcunits", "");
                if (units.equals(currunits) || units.isEmpty() || currunits.isEmpty()) {
                    if (netconc == sampleInfo.getDouble(i, "childconc")) continue;
                    netconc = -1.0;
                    break;
                }
                double currentconc = sampleInfo.getDouble(i, "childconc");
                if (currentconc < 0.0) {
                    netconc = -1.0;
                    break;
                }
                currentconc = UnitsUtil.unitConv(this.getConnectionId(), new BigDecimal(currentconc), currunits, units).doubleValue();
                if (netconc == currentconc) continue;
                netconc = -1.0;
                break;
            }
        }
        return netconc;
    }

    private String[] createPooledSample(String pooledSampleIds, String parentQuantity, String pooledQuantity, String pooledQuantityUnit, String pooledConc, String pooledConcUnits, boolean decrementparentsamples) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sampleid", pooledSampleIds);
        props.setProperty("poolcopies", "1");
        if (decrementparentsamples) {
            props.setProperty("quantity", parentQuantity);
        }
        props.setProperty("poolquantity", pooledQuantity);
        props.setProperty("poolunitid", pooledQuantityUnit);
        props.setProperty("poolmode", "BioBank");
        props.setProperty("enablequantitycalculation", "N");
        if (pooledConc != null) {
            props.setProperty("childcolumn_concentration", pooledConc);
        }
        if (pooledConcUnits != null) {
            props.setProperty("childcolumn_concentrationunits", pooledConcUnits);
        }
        this.getActionProcessor().processActionClass(CreateSamplePool.class.getName(), props);
        String[] ret = new String[]{props.getProperty("newkeyid1"), props.getProperty("newtrackitemid")};
        return ret;
    }

    private BigDecimal convertToBigDecimal(double number) {
        return BigDecimal.valueOf(number).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToBigDecimal(String number) {
        BigDecimal bd;
        try {
            bd = this.m18NUtil.parseBigDecimal(number);
        }
        catch (Exception e) {
            bd = null;
        }
        return bd != null ? bd.setScale(3, RoundingMode.HALF_UP) : null;
    }

    static class AuditInputs {
        String auditreason;
        String auditactivity;
        String auditsignedflag;

        AuditInputs() {
        }
    }

    static class VolumeParams {
        double sourceVolumeToDecrement;
        String sourceVolumeToDecrementUnits;
        double targetVolumeToUpdate;
        String targetVolumeToUpdateUnits;
        double targetConcentration;
        String targetConcUnits;

        VolumeParams() {
        }
    }

    static class TargetInputs {
        String targetid;
        String targetarraytypeid;
        String targetarraytypeversionid;
        String targetvolume;
        String targetconc;
        String targetvolumeunits;
        String targetconcunits;
        int maxtargetcount;
        String targetarraylayoutid;
        String targetarraylayoutversionid;

        TargetInputs() {
        }
    }

    static class SourceInputs {
        String sourceid;
        int sourceindex;
        String sourcearraytypeid;
        String sourcearraytypeversionid;
        int maxsourcecount;
        String sourcevolumetodecrement;
        String sourcevolumetodecrementunits;
        boolean validatesourcevolume;
        String quantityhandlingflag;

        SourceInputs() {
        }
    }

    static class ArrayItemContent {
        String contentSdcId;
        String contentKeyId1;
        String contentItem;
        String contentType;
        String reagentTypeId;
        String reagentTypeVersionId;
        String treatmentTypeId;
        String arrayId;
        String itemLabel;
        String zone;

        ArrayItemContent() {
        }
    }

    public static enum TRANSFERTYPE {
        TBD,
        ASLTOASL,
        ASLTOARRAY,
        ARRAYTOASL,
        ARRAYTOARRAY;

    }

    static class UserMessages {
        static final String MISSINGREQARGS = "Missing Required Arguments: arraytransfermethodid and arraytransfermethodversionid )";
        static final String INVALIDINPUTPARAMETER = "Invalid input sourcearrayid - Transfer Map in the Array Transfer Method is not defined to support the specified number of source arrays ";
        static final String INVALIDTRANSFERMETHODID = "Invalid input transfermethodid - Action supports only 1 value of the transfermethodid";
        static final String INVALIDTRANSFERMETHODVERSIONID = "Invalid input transfermethodversionid - Action supports only 1 value of the transfermethodversionid";
        static final String MISSINGSOURCE = "Missing Required Arguments: sourcearrayid )";
        static final String MISSINGTARGETVOLUME = "TargetVolume and TargetVolume units are required if the quantityhandlingflag is D or O";
        static final String INVALIDSOURCE = "Invalid source array- The arraytype of the sourcearray must match the arraytype of the source defined in the arraytransfermethod.";

        UserMessages() {
        }
    }
}

