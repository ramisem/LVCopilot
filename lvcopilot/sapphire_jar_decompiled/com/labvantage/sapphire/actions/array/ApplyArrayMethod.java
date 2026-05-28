/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.array.AddDSToArray;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sdi.EditSDIDetail;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApplyArrayMethod
extends BaseAction
implements sapphire.action.ApplyArrayMethod {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String columnid;
        int i;
        String arrayidlist = properties.getProperty("arrayid");
        String arraymethodid = properties.getProperty("arraymethodid");
        if (StringUtil.getLen(arrayidlist) == 0L) {
            throw new SapphireException(this.getTranslationProcessor().translate("Arrayid is mandatory"));
        }
        if (StringUtil.getLen(arraymethodid) == 0L) {
            throw new SapphireException(this.getTranslationProcessor().translate("ArrayMethod is mandatory"));
        }
        String arraymethodversionid = properties.getProperty("arraymethodversionid", "");
        if (StringUtil.getLen(arraymethodversionid) == 0L) {
            arraymethodversionid = ArrayUtil.getArrayMethodCurrentVersion(this.getQueryProcessor(), arraymethodid);
        }
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayMethod");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("arraymethodparamitem");
        sdiRequest.setRequestItem("arraymethodcontent");
        sdiRequest.setKeyid1List(arraymethodid);
        sdiRequest.setKeyid2List(arraymethodversionid);
        sdiRequest.setExtendedDataTypes(true);
        SDIData arrayMethod = sdiProcessor.getSDIData(sdiRequest);
        DataSet primary = arrayMethod.getDataset("primary");
        DataSet arraymethodcontent = arrayMethod.getDataset("arraymethodcontent");
        DataSet arraymethodparamitem = arrayMethod.getDataset("arraymethodparamitem");
        String arrayMethodLayoutid = primary.getString(0, "arraylayoutid", "");
        String arrayMethodLayoutVersionid = primary.getString(0, "arraylayoutversionid", "");
        String promoteresultsflag = primary.getString(0, "promoteresultsflag", "D");
        String executecalcflag = primary.getString(0, "executecalcflag", "N");
        String createchildsampleflag = primary.getString(0, "createchildsampleflag", "N");
        String sampletypeid = primary.getString(0, "sampletypeid", "");
        String instrumenttypeid = primary.getString(0, "instrumenttypeid", "");
        String instrumentmodelid = primary.getString(0, "instrumentmodelid", "");
        DataSet arrayMethodItemDS = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Array", "select arrayarraymethoditem.arrayid, arrayarraymethoditem.arraymethodid, arrayarraymethoditem.arraymethodversionid, arrayarraymethoditem.arraymethodinstance, arrayarraymethoditem.arraymethoditemstatus, arrayarraymethoditem.usersequence from arrayarraymethoditem where arrayarraymethoditem.arrayid in ([]) order by arrayarraymethoditem.arrayid, arrayarraymethoditem.usersequence desc", arrayidlist);
        DataSet arrayZoneDS = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Array", "select arrayid, arrayzoneid, zone from arrayzone where arrayid in ([])", arrayidlist);
        DataSet zoneContentDS = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Array", "select arrayzone.arrayid, count(arrayzonecontent.arrayzonecontentid) arrayzonecontentcount from arrayzonecontent, arrayzone where arrayzonecontent.arraymethodid = '" + arraymethodid + "' and arrayzonecontent.arraymethodversionid = '" + arraymethodversionid + "' and arrayzonecontent.arrayzoneid = arrayzone.arrayzoneid and arrayzone.arrayid in ([]) group by arrayzone.arrayid", arrayidlist);
        DataSet paramItemsDS = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Array", "select arrayzone.arrayid, count(arrayparamitem.itemid) arrayparamitemcount from arrayparamitem, arrayzone where arrayparamitem.arraymethodid = '" + arraymethodid + "' AND arrayparamitem.arraymethodversionid = '" + arraymethodversionid + "' and arrayparamitem.arrayzoneid = arrayzone.arrayzoneid and arrayzone.arrayid in ([]) group by arrayzone.arrayid", arrayidlist);
        String sql = "select array.arrayid, array.arraystatus, array.arraylayoutid, array.arraylayoutversionid FROM array where array.arrayid in ([]) order by array.arrayid";
        DataSet arrayDS = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Array", sql, arrayidlist);
        DataSet cancelArrayMethodDataSet = new DataSet();
        DataSet addArrayMethodDataSet = new DataSet();
        DataSet arrayMethodContentDS = new DataSet();
        DataSet arrayMathodParamItemsDS = new DataSet();
        DataSet addDSToArrayDS = new DataSet();
        DataSet addArrayLayoutToArrayDS = new DataSet();
        for (i = 0; i < arrayDS.size(); ++i) {
            String arraymethoditemstatus;
            int row;
            String arrayid = arrayDS.getString(i, "arrayid");
            String arraylayoutid = arrayDS.getString(i, "arraylayoutid", "");
            String arraylayoutversionid = arrayDS.getString(i, "arraylayoutversionid", "");
            if ("Disposed".equals(arrayDS.getString(i, "arraystatus"))) {
                throw new SapphireException(this.getTranslationProcessor().translate("Array method can not be applied to a Disposed Array") + " [" + arrayid + "]");
            }
            if (OpalUtil.isNotEmpty(arrayMethodItemDS) && (row = arrayMethodItemDS.findRow("arrayid", arrayid)) != -1 && !(arraymethoditemstatus = arrayMethodItemDS.getString(row, "arraymethoditemstatus", "")).isEmpty()) {
                if (!(arraymethoditemstatus.equals("Initial") || arraymethoditemstatus.equals("Loaded") || arraymethoditemstatus.equals("DataEntered") || arraymethoditemstatus.equals("Completed") || arraymethoditemstatus.equals("Cancelled") || arraymethoditemstatus.equals("Reviewed") || arraymethoditemstatus.equals("SentToInstrument"))) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Invalid status. Cannot apply method:") + arraymethodid + "(" + arraymethodversionid + ")");
                }
                if (arraymethoditemstatus.equals("Initial") || arraymethoditemstatus.equals("SentToInstrument") || arraymethoditemstatus.equals("DataEntered")) {
                    String _arraymethodid = arrayMethodItemDS.getString(row, "arraymethodid", "");
                    String _arraymethodversionid = arrayMethodItemDS.getValue(row, "arraymethodversionid", "");
                    String _arraymethodinstance = arrayMethodItemDS.getValue(row, "arraymethodinstance", "");
                    if (!(_arraymethodid.isEmpty() || _arraymethodversionid.isEmpty() || _arraymethodinstance.isEmpty())) {
                        int cancelDSRow = cancelArrayMethodDataSet.addRow();
                        cancelArrayMethodDataSet.setString(cancelDSRow, "arrayid", arrayid);
                        cancelArrayMethodDataSet.setString(cancelDSRow, "arraymethodid", _arraymethodid);
                        cancelArrayMethodDataSet.setString(cancelDSRow, "arraymethodversionid", _arraymethodversionid);
                        cancelArrayMethodDataSet.setString(cancelDSRow, "arraymethodinstance", _arraymethodinstance);
                    }
                }
            }
            if (!arraylayoutid.isEmpty() && !arraylayoutversionid.isEmpty()) {
                if (!arrayMethodLayoutid.equals(arraylayoutid) || !arrayMethodLayoutVersionid.equals(arraylayoutversionid)) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Array method must have same Array Layout to be applied to the Array") + " [" + arrayid + "]");
                }
            } else {
                row = addArrayLayoutToArrayDS.addRow();
                addArrayLayoutToArrayDS.setString(row, "arrayid", arrayid);
                addArrayLayoutToArrayDS.setString(row, "arraylayoutid", arrayMethodLayoutid);
                addArrayLayoutToArrayDS.setString(row, "arraylayoutversionid", arrayMethodLayoutVersionid);
            }
            int arraymethodinstance = 1;
            int usersequence = 1;
            if (OpalUtil.isNotEmpty(arrayMethodItemDS)) {
                int row2;
                for (row2 = 0; row2 < arrayMethodItemDS.size(); ++row2) {
                    String _arrayid = arrayMethodItemDS.getString(row2, "arrayid", "");
                    String _arraymethodid = arrayMethodItemDS.getString(row2, "arraymethodid", "");
                    String _arraymethodversionid = arrayMethodItemDS.getValue(row2, "arraymethodversionid", "");
                    if (!_arrayid.equals(arrayid) || !_arraymethodid.equals(arraymethodid) || !_arraymethodversionid.equals(arraymethodversionid)) continue;
                    arraymethodinstance = arrayMethodItemDS.getInt(row2, "arraymethodinstance") + 1;
                    break;
                }
                for (row2 = 0; row2 < arrayMethodItemDS.size(); ++row2) {
                    if (!arrayMethodItemDS.getString(row2, "arrayid", "").equals(arrayid)) continue;
                    ++usersequence;
                }
            }
            int row3 = addArrayMethodDataSet.addRow();
            addArrayMethodDataSet.setString(row3, "arrayid", arrayid);
            addArrayMethodDataSet.setString(row3, "arraymethodid", arraymethodid);
            addArrayMethodDataSet.setString(row3, "arraymethodversionid", arraymethodversionid);
            addArrayMethodDataSet.setString(row3, "arraymethodinstance", String.valueOf(arraymethodinstance));
            addArrayMethodDataSet.setString(row3, "arraymethoditemstatus", "Initial");
            addArrayMethodDataSet.setString(row3, "usersequence", String.valueOf(usersequence));
            addArrayMethodDataSet.setString(row3, "promoteresultsflag", promoteresultsflag);
            addArrayMethodDataSet.setString(row3, "executecalcflag", executecalcflag);
            addArrayMethodDataSet.setString(row3, "createchildsampleflag", createchildsampleflag);
            addArrayMethodDataSet.setString(row3, "sampletypeid", sampletypeid);
            addArrayMethodDataSet.setString(row3, "instrumenttypeid", instrumenttypeid);
            addArrayMethodDataSet.setString(row3, "instrumentmodelid", instrumentmodelid);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("arrayid", arrayid);
            DataSet zoneDS = arrayZoneDS.getFilteredDataSet(filter);
            if (arraymethodcontent.getRowCount() > 0) {
                int zoneContentCount = 0;
                int zoneContentRow = zoneContentDS.findRow("arrayid", arrayid);
                if (zoneContentRow != -1) {
                    zoneContentCount = zoneContentDS.getInt(zoneContentRow, "arrayzonecontentcount", 0);
                }
                if (zoneContentCount == 0) {
                    this.copyArrayMethodContent(arrayid, arraymethodcontent, zoneDS, arrayMethodContentDS);
                }
            }
            if (!OpalUtil.isNotEmpty(arraymethodparamitem)) continue;
            int paramItemCount = 0;
            int paramItemRow = paramItemsDS.findRow("arrayid", arrayid);
            if (paramItemRow != -1) {
                paramItemCount = paramItemsDS.getInt(paramItemRow, "arrayparamitemcount", 0);
            }
            if (paramItemCount == 0) {
                this.copyArrayMethodParamItems(arrayid, arraymethodparamitem, zoneDS, arrayMathodParamItemsDS);
            }
            int dsRow = addDSToArrayDS.addRow();
            addDSToArrayDS.setString(dsRow, "arrayid", arrayid);
            addDSToArrayDS.setString(dsRow, "arraymethodid", arraymethodid);
            addDSToArrayDS.setString(dsRow, "arraymethodversionid", arraymethodversionid);
            addDSToArrayDS.setString(dsRow, "arraymethodinstance", String.valueOf(arraymethodinstance));
        }
        if (OpalUtil.isNotEmpty(addArrayLayoutToArrayDS)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Array");
            props.setProperty("keyid1", addArrayLayoutToArrayDS.getColumnValues("arrayid", ";"));
            props.setProperty("arraylayoutid", addArrayLayoutToArrayDS.getColumnValues("arraylayoutid", ";"));
            props.setProperty("arraylayoutversionid", addArrayLayoutToArrayDS.getColumnValues("arraylayoutversionid", ";"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            List<String> arrayList = OpalUtil.toUniqueList(addArrayLayoutToArrayDS.getColumnValues("arrayid", ";"), ";");
            DataSet arrayItemZoneInsertDS = new DataSet();
            for (String arrayid : arrayList) {
                String arrayzoneid;
                int i2;
                DataSet arrayItemDS = this.getQueryProcessor().getPreparedSqlDataSet("select arrayitemid from arrayitem where arrayid = ? order by xpos, ypos", (Object[])new String[]{arrayid});
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select arrayzoneid, zone from arrayzone where arrayid = ?", (Object[])new String[]{arrayid});
                if (ds == null || ds.isEmpty()) continue;
                HashSet<String> insertSet = new HashSet<String>();
                DataSet existingDS = this.getQueryProcessor().getSqlDataSet("select arrayzoneid, arrayitemid from arrayitemarrayzone where arrayitemid like '" + arrayid + "%'");
                if (existingDS != null) {
                    for (i2 = 0; i2 < existingDS.size(); ++i2) {
                        arrayzoneid = existingDS.getString(i2, "arrayzoneid");
                        String arrayitemid = existingDS.getString(i2, "arrayitemid");
                        insertSet.add(arrayzoneid + "-" + arrayitemid);
                    }
                }
                for (i2 = 0; i2 < ds.size(); ++i2) {
                    arrayzoneid = ds.getString(i2, "arrayzoneid");
                    for (int j = 0; j < arrayItemDS.size(); ++j) {
                        String arrayitemid = arrayItemDS.getString(j, "arrayitemid");
                        String insertKey = arrayzoneid + "-" + arrayitemid;
                        if (insertSet.contains(insertKey)) continue;
                        insertSet.add(insertKey);
                        int row = arrayItemZoneInsertDS.addRow();
                        arrayItemZoneInsertDS.setString(row, "arrayzoneid", arrayzoneid);
                        arrayItemZoneInsertDS.setString(row, "arrayitemid", arrayitemid);
                        arrayItemZoneInsertDS.setNumber(row, "usersequence", j + 1);
                        arrayItemZoneInsertDS.setString(row, "createby", sysuserid);
                        arrayItemZoneInsertDS.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
                        arrayItemZoneInsertDS.setString(row, "createtool", "ApplyArrayMethod");
                        arrayItemZoneInsertDS.setString(row, "modby", sysuserid);
                        arrayItemZoneInsertDS.setDate(row, "moddt", DateTimeUtil.getNowCalendar());
                        arrayItemZoneInsertDS.setString(row, "modtool", "ApplyArrayMethod");
                    }
                }
            }
            if (!arrayItemZoneInsertDS.isEmpty()) {
                DataSetUtil.insert(this.database, arrayItemZoneInsertDS, "arrayitemarrayzone");
            }
        }
        if (OpalUtil.isNotEmpty(cancelArrayMethodDataSet)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Array");
            props.setProperty("linkid", "Array ArrayMethod Item");
            props.setProperty("keyid1", cancelArrayMethodDataSet.getColumnValues("arrayid", ";"));
            props.setProperty("arraymethodid", cancelArrayMethodDataSet.getColumnValues("arraymethodid", ";"));
            props.setProperty("arraymethodversionid", cancelArrayMethodDataSet.getColumnValues("arraymethodversionid", ";"));
            props.setProperty("arraymethodinstance", cancelArrayMethodDataSet.getColumnValues("arraymethodinstance", ";"));
            props.setProperty("arraymethoditemstatus", "Cancelled");
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(EditSDIDetail.class.getName(), props);
        }
        if (OpalUtil.isNotEmpty(addArrayMethodDataSet)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Array");
            props.setProperty("linkid", "Array ArrayMethod Item");
            for (int i3 = 0; i3 < addArrayMethodDataSet.getColumnCount(); ++i3) {
                columnid = addArrayMethodDataSet.getColumnId(i3);
                props.setProperty(columnid, addArrayMethodDataSet.getColumnValues(columnid, ";"));
            }
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
        }
        if (OpalUtil.isNotEmpty(arrayMethodContentDS)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ArrayZone");
            props.setProperty("linkid", "Array Zone Content");
            for (int i4 = 0; i4 < arrayMethodContentDS.getColumnCount(); ++i4) {
                columnid = arrayMethodContentDS.getColumnId(i4);
                props.setProperty(columnid, arrayMethodContentDS.getColumnValues(columnid, ";"));
            }
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
        }
        if (OpalUtil.isNotEmpty(arrayMathodParamItemsDS)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Array");
            props.setProperty("linkid", "ParamItem");
            for (int i5 = 0; i5 < arrayMathodParamItemsDS.getColumnCount(); ++i5) {
                columnid = arrayMathodParamItemsDS.getColumnId(i5);
                props.setProperty(columnid, arrayMathodParamItemsDS.getColumnValues(columnid, ";"));
            }
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
        }
        if (!addDSToArrayDS.isEmpty()) {
            for (i = 0; i < addDSToArrayDS.size(); ++i) {
                PropertyList props = new PropertyList();
                props.setProperty("arrayid", addDSToArrayDS.getString(i, "arrayid"));
                props.setProperty("arraymethodid", addDSToArrayDS.getString(i, "arraymethodid"));
                props.setProperty("arraymethodversionid", addDSToArrayDS.getString(i, "arraymethodversionid"));
                props.setProperty("arraymethodinstance", addDSToArrayDS.getString(i, "arraymethodinstance"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                this.getActionProcessor().processActionClass(AddDSToArray.class.getName(), props);
            }
        }
    }

    private void copyArrayMethodContent(String arrayid, DataSet arraymethodcontent, DataSet arrayZones, DataSet arrayMethodContentDS) throws SapphireException {
        int i;
        HashMap<String, String> filter = new HashMap<String, String>();
        for (i = 0; i < arraymethodcontent.size(); ++i) {
            String zonename = arraymethodcontent.getString(i, "zone");
            filter.put("zone", zonename);
            DataSet match = arrayZones.getFilteredDataSet(filter);
            if (match == null || match.getRowCount() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Array Zone with name:" + zonename + " does not exist in Array:" + arrayid));
            }
            arraymethodcontent.setString(i, "arrayzoneid", match.getString(0, "arrayzoneid"));
            arraymethodcontent.setString(i, "arrayzonecontentid", "");
        }
        for (i = 0; i < arraymethodcontent.size(); ++i) {
            int row = arrayMethodContentDS.addRow();
            arrayMethodContentDS.setString(row, "keyid1", arraymethodcontent.getValue(i, "arrayzoneid", ""));
            arrayMethodContentDS.setString(row, "arrayzoneid", arraymethodcontent.getValue(i, "arrayzoneid", ""));
            arrayMethodContentDS.setString(row, "contentdescription", arraymethodcontent.getValue(i, "contentdescription", ""));
            arrayMethodContentDS.setString(row, "arrayzonecontentid", arraymethodcontent.getValue(i, "arrayzonecontentid", ""));
            arrayMethodContentDS.setString(row, "arraymethodid", arraymethodcontent.getValue(i, "arraymethodid", ""));
            arrayMethodContentDS.setString(row, "arraymethodversionid", arraymethodcontent.getValue(i, "arraymethodversionid", ""));
            arrayMethodContentDS.setString(row, "contenttype", arraymethodcontent.getValue(i, "contenttype", ""));
            arrayMethodContentDS.setString(row, "contentlabel", arraymethodcontent.getValue(i, "contentlabel", ""));
            arrayMethodContentDS.setString(row, "levelflag", arraymethodcontent.getValue(i, "levelflag", ""));
            arrayMethodContentDS.setString(row, "reagenttypeid", arraymethodcontent.getValue(i, "reagenttypeid", ""));
            arrayMethodContentDS.setString(row, "reagenttypeversionid", arraymethodcontent.getValue(i, "reagenttypeversionid", ""));
            arrayMethodContentDS.setString(row, "treatmenttypeid", arraymethodcontent.getValue(i, "treatmenttypeid", ""));
            arrayMethodContentDS.setString(row, "queryid", arraymethodcontent.getValue(i, "queryid", ""));
            arrayMethodContentDS.setString(row, "querybasedonid", arraymethodcontent.getValue(i, "querybasedonid", ""));
            arrayMethodContentDS.setString(row, "queryparams", arraymethodcontent.getValue(i, "queryparams", ""));
            arrayMethodContentDS.setString(row, "volumetarget", arraymethodcontent.getValue(i, "volumetarget", ""));
            arrayMethodContentDS.setString(row, "volumetargetunits", arraymethodcontent.getValue(i, "volumetargetunits", ""));
            arrayMethodContentDS.setString(row, "volumereqflag", arraymethodcontent.getValue(i, "volumereqflag", ""));
            arrayMethodContentDS.setString(row, "volumehandlingflag", arraymethodcontent.getValue(i, "volumehandlingflag", ""));
            arrayMethodContentDS.setString(row, "showvolumedialogflag", arraymethodcontent.getValue(i, "showvolumedialogflag", ""));
            arrayMethodContentDS.setString(row, "concentrationtarget", arraymethodcontent.getValue(i, "concentrationtarget", ""));
            arrayMethodContentDS.setString(row, "concentrationtargetunits", arraymethodcontent.getValue(i, "concentrationtargetunits", ""));
            arrayMethodContentDS.setString(row, "concentrationreqflag", arraymethodcontent.getValue(i, "concentrationreqflag", ""));
            arrayMethodContentDS.setString(row, "workflowqueueflag", arraymethodcontent.getValue(i, "workflowqueueflag", ""));
            arrayMethodContentDS.setString(row, "createsdiflag", arraymethodcontent.getValue(i, "createsdiflag", ""));
            arrayMethodContentDS.setString(row, "createtemplateid", arraymethodcontent.getValue(i, "createtemplateid", ""));
            arrayMethodContentDS.setString(row, "workitemid", arraymethodcontent.getValue(i, "workitemid", ""));
            arrayMethodContentDS.setString(row, "workitemversionid", arraymethodcontent.getValue(i, "workitemversionid", ""));
            arrayMethodContentDS.setString(row, "sourcezone", arraymethodcontent.getValue(i, "sourcezone", ""));
            arrayMethodContentDS.setString(row, "sourcearraytypeid", arraymethodcontent.getValue(i, "sourcearraytypeid", ""));
            arrayMethodContentDS.setString(row, "sourcearraytypeversionid", arraymethodcontent.getValue(i, "sourcearraytypeversionid", ""));
            arrayMethodContentDS.setString(row, "sourcesequencetype", arraymethodcontent.getValue(i, "sourcesequencetype", ""));
            arrayMethodContentDS.setString(row, "sourcesequencecustomrule", arraymethodcontent.getValue(i, "sourcesequencecustomrule", ""));
            arrayMethodContentDS.setString(row, "contenttransferrule", arraymethodcontent.getValue(i, "contenttransferrule", ""));
            arrayMethodContentDS.setString(row, "contentitem", arraymethodcontent.getValue(i, "contentitem", ""));
            arrayMethodContentDS.setString(row, "definition", arraymethodcontent.getValue(i, "definition", ""));
            arrayMethodContentDS.setString(row, "treatmentcategoryid", arraymethodcontent.getValue(i, "treatmentcategoryid", ""));
            arrayMethodContentDS.setString(row, "diluentvolume", arraymethodcontent.getValue(i, "diluentvolume", ""));
            arrayMethodContentDS.setString(row, "diluentvolumeunits", arraymethodcontent.getValue(i, "diluentvolumeunits", ""));
            arrayMethodContentDS.setString(row, "usersequence", arraymethodcontent.getValue(i, "usersequence", ""));
            arrayMethodContentDS.setString(row, "sourcedirection", arraymethodcontent.getValue(i, "sourcedirection", ""));
            arrayMethodContentDS.setString(row, "amounttarget", arraymethodcontent.getValue(i, "amounttarget", ""));
            arrayMethodContentDS.setString(row, "amounttargetunits", arraymethodcontent.getValue(i, "amounttargetunits", ""));
        }
    }

    private void copyArrayMethodParamItems(String arrayid, DataSet arraymethodparamitem, DataSet arrayZones, DataSet arrayMathodParamItemsDS) throws SapphireException {
        if (arraymethodparamitem != null && !arraymethodparamitem.isEmpty()) {
            int i;
            arraymethodparamitem.setString(0, "arrayid", arrayid);
            arraymethodparamitem.padColumn("arrayid");
            HashMap<String, String> filter = new HashMap<String, String>();
            for (i = 0; i < arraymethodparamitem.getRowCount(); ++i) {
                String zonename = arraymethodparamitem.getString(i, "zone");
                filter.put("zone", zonename);
                DataSet match = arrayZones.getFilteredDataSet(filter);
                if (match == null || match.getRowCount() == 0) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Array Zone with name:" + zonename + " does not exist in Array:" + arrayid));
                }
                arraymethodparamitem.setString(i, "arrayzoneid", match.getString(0, "arrayzoneid"));
            }
            for (i = 0; i < arraymethodparamitem.size(); ++i) {
                int row = arrayMathodParamItemsDS.addRow();
                arrayMathodParamItemsDS.setString(row, "arrayid", arrayid);
                arrayMathodParamItemsDS.setString(row, "arraymethodid", arraymethodparamitem.getValue(i, "arraymethodid", ""));
                arrayMathodParamItemsDS.setString(row, "arraymethodversionid", arraymethodparamitem.getValue(i, "arraymethodversionid", ""));
                arrayMathodParamItemsDS.setString(row, "itemid", arraymethodparamitem.getValue(i, "itemid", ""));
                arrayMathodParamItemsDS.setString(row, "arrayzoneid", arraymethodparamitem.getValue(i, "arrayzoneid", ""));
                arrayMathodParamItemsDS.setString(row, "paramid", arraymethodparamitem.getValue(i, "paramid", ""));
                arrayMathodParamItemsDS.setString(row, "levelflag", arraymethodparamitem.getValue(i, "levelflag", ""));
                arrayMathodParamItemsDS.setString(row, "paramtype", arraymethodparamitem.getValue(i, "paramtype", ""));
                arrayMathodParamItemsDS.setString(row, "datatypes", arraymethodparamitem.getValue(i, "datatypes", ""));
                arrayMathodParamItemsDS.setString(row, "entryreftypeid", arraymethodparamitem.getValue(i, "entryreftypeid", ""));
                arrayMathodParamItemsDS.setString(row, "calcrule", arraymethodparamitem.getValue(i, "calcrule", ""));
                arrayMathodParamItemsDS.setString(row, "displayformat", arraymethodparamitem.getValue(i, "displayformat", ""));
                arrayMathodParamItemsDS.setString(row, "operator", arraymethodparamitem.getValue(i, "operator", ""));
                arrayMathodParamItemsDS.setString(row, "value1", arraymethodparamitem.getValue(i, "value1", ""));
                arrayMathodParamItemsDS.setString(row, "value2", arraymethodparamitem.getValue(i, "value2", ""));
                arrayMathodParamItemsDS.setString(row, "value1num", arraymethodparamitem.getValue(i, "value1num", ""));
                arrayMathodParamItemsDS.setString(row, "value2num", arraymethodparamitem.getValue(i, "value2num", ""));
                arrayMathodParamItemsDS.setString(row, "unitsid", arraymethodparamitem.getValue(i, "unitsid", ""));
                arrayMathodParamItemsDS.setString(row, "actionflag", arraymethodparamitem.getValue(i, "actionflag", ""));
                arrayMathodParamItemsDS.setString(row, "usersequence", arraymethodparamitem.getValue(i, "usersequence", ""));
                arrayMathodParamItemsDS.setString(row, "propagationrule", arraymethodparamitem.getValue(i, "propagationrule", ""));
            }
        }
    }
}

