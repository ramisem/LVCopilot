/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RemeasureArray
extends BaseAction
implements sapphire.action.RemeasureArray {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77312 $";
    private TranslationProcessor tp;
    private QueryProcessor qp;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.tp = this.getTranslationProcessor();
        this.qp = this.getQueryProcessor();
        String arrayidlist = properties.getProperty("arrayid");
        StringBuffer returnArrayMethodIds = new StringBuffer();
        StringBuffer returnArrayMethodVersionIds = new StringBuffer();
        StringBuffer returnArrayMethodInstances = new StringBuffer();
        if (StringUtil.getLen(arrayidlist) == 0L) {
            throw new SapphireException(this.tp.translate("Arrayid is mandatory"));
        }
        String[] arrayidarr = StringUtil.split(arrayidlist, ";");
        DataSet dsAAMI = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        if (arrayidarr.length > 750) {
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet("LV_Array", arrayidlist, null, null);
                dsAAMI = this.qp.getPreparedSqlDataSet("SELECT a.*, array.arraystatus FROM arrayarraymethoditem a, rsetitems r, array  WHERE array.arrayid = a.arrayid AND a.arrayid = r.keyid1 AND r.rsetid = " + safeSQL.addVar(rsetid), safeSQL.getValues());
            }
            catch (SapphireException e) {
                this.logger.error("Error", e);
            }
            finally {
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        } else {
            dsAAMI = this.qp.getPreparedSqlDataSet("SELECT a.*, array.arraystatus FROM arrayarraymethoditem a, array WHERE a.arrayid IN(" + safeSQL.addIn(arrayidlist, ";") + ") AND array.arrayid = a.arrayid ", safeSQL.getValues());
        }
        if (dsAAMI.getRowCount() == 0) {
            throw new SapphireException(this.tp.translate("No Array Method Instance could be found for the provided Array") + ": " + arrayidlist);
        }
        DataSet arrayArrayMethods = new DataSet();
        dsAAMI.sort("arrayid");
        ArrayList<DataSet> groups = dsAAMI.getGroupedDataSets("arrayid");
        for (int g = 0; g < groups.size(); ++g) {
            DataSet ds = (DataSet)groups.get(g);
            ds.sort("usersequence d");
            arrayArrayMethods.copyRow(ds, 0, 1);
        }
        for (int i = 0; i < arrayArrayMethods.getRowCount(); ++i) {
            String arrayMethodItemStatus;
            String arrayId = arrayArrayMethods.getValue(i, "arrayid");
            String arrayMethodId = arrayArrayMethods.getValue(i, "arraymethodid");
            String arrayMethodVersionId = arrayArrayMethods.getValue(i, "arraymethodversionid");
            String promoteResultsFlag = arrayArrayMethods.getValue(i, "promoteresultsflag", "D");
            String executeCalcFlag = arrayArrayMethods.getValue(i, "executecalcflag", "N");
            String createChildsampleFlag = arrayArrayMethods.getValue(i, "createchildsampleflag", "N");
            String sampleTypeId = arrayArrayMethods.getValue(i, "sampletypeid");
            String instrumentTypeId = arrayArrayMethods.getValue(i, "instrumenttypeid");
            String instrumentModelId = arrayArrayMethods.getValue(i, "instrumentmodelid");
            int arrayMethodInstance = arrayArrayMethods.getInt(i, "arraymethodinstance");
            int arrayMethodUserSequence = arrayArrayMethods.getInt(i, "usersequence");
            int newAMIUserSequence = arrayMethodUserSequence + 1;
            int newAMIInstance = arrayMethodInstance + 1;
            String arrayStatus = arrayArrayMethods.getValue(i, "arraystatus");
            if (!ArrayUtil.validateStatus(arrayStatus, arrayMethodItemStatus = arrayArrayMethods.getValue(i, "arraymethoditemstatus"), "RemeasureArray", promoteResultsFlag)) {
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("arrayid", arrayId);
                token.put("arraymethoditemstatus", arrayMethodItemStatus);
                throw new SapphireException(this.tp.translate("Array [arrayid] cannot be remeasured.", token) + "\n" + this.tp.translate("To remeasure an Array, latest ArrayMethod instance should be in Completed, DataEntered or Reviewed status and Array status should not be Disposed.", token));
            }
            this.updatePreviousArrayMethodInstanceAsRemeasured(properties, arrayId, arrayMethodId, arrayMethodVersionId, "" + arrayMethodInstance, arrayMethodItemStatus);
            PropertyList arrayarraymethoditemprops = new PropertyList();
            arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
            arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
            arrayarraymethoditemprops.setProperty("arrayid", arrayId);
            arrayarraymethoditemprops.setProperty("arraymethodid", arrayMethodId);
            arrayarraymethoditemprops.setProperty("arraymethodversionid", arrayMethodVersionId);
            arrayarraymethoditemprops.setProperty("arraymethodinstance", "" + newAMIInstance);
            arrayarraymethoditemprops.setProperty("arraymethoditemstatus", "Initial");
            arrayarraymethoditemprops.setProperty("promoteresultsflag", promoteResultsFlag);
            arrayarraymethoditemprops.setProperty("executecalcflag", executeCalcFlag);
            arrayarraymethoditemprops.setProperty("createchildsampleflag", createChildsampleFlag);
            arrayarraymethoditemprops.setProperty("sampletypeid", sampleTypeId);
            arrayarraymethoditemprops.setProperty("instrumenttypeid", instrumentTypeId);
            arrayarraymethoditemprops.setProperty("instrumentmodelid", instrumentModelId);
            arrayarraymethoditemprops.setProperty("usersequence", "" + newAMIUserSequence);
            arrayarraymethoditemprops.setProperty("remeasureinstance", "" + arrayMethodInstance);
            arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("AddSDIDetail", "1", arrayarraymethoditemprops);
            try {
                DataSet sdidataArrayItem = this.qp.getPreparedSqlDataSet("SELECT distinct sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset  FROM sdidata, arrayitem WHERE sdidata.sdcid = 'LV_ArrayItem' AND sdidata.keyid1 = arrayitem.arrayitemid AND arrayitem.arrayid = ?  AND sdidata.arraymethodid = ? AND sdidata.arraymethodversionid = ? AND sdidata.arraymethodinstance = ?", (Object[])new String[]{arrayId, arrayMethodId, arrayMethodVersionId, "" + arrayMethodInstance});
                DataSet sdidataArrayZone = this.qp.getPreparedSqlDataSet("SELECT distinct sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset FROM sdidata, arrayzone WHERE sdidata.sdcid = 'LV_ArrayZone' AND sdidata.keyid1 = arrayzone.arrayzoneid AND arrayzone.arrayid = ?  AND sdidata.arraymethodid = ? AND sdidata.arraymethodversionid = ? AND sdidata.arraymethodinstance = ?", (Object[])new String[]{arrayId, arrayMethodId, arrayMethodVersionId, "" + arrayMethodInstance});
                if (sdidataArrayItem.getRowCount() > 0) {
                    this.callRemeasureDataset(sdidataArrayItem, "LV_ArrayItem", properties);
                }
                if (sdidataArrayZone.getRowCount() > 0) {
                    this.callRemeasureDataset(sdidataArrayZone, "LV_ArrayZone", properties);
                }
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to apply array method. " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            returnArrayMethodIds.append(";").append(arrayMethodId);
            returnArrayMethodVersionIds.append(";").append(arrayMethodVersionId);
            returnArrayMethodInstances.append(";").append(newAMIInstance);
        }
        if (returnArrayMethodIds.length() > 0) {
            properties.setProperty("remeasuredarraymethodid", returnArrayMethodIds.substring(1));
        }
        if (returnArrayMethodVersionIds.length() > 0) {
            properties.setProperty("remeasuredarraymethodversionid", returnArrayMethodVersionIds.substring(1));
        }
        if (returnArrayMethodInstances.length() > 0) {
            properties.setProperty("returnarraymethodinstance", returnArrayMethodInstances.substring(1));
        }
    }

    private void callRemeasureDataset(DataSet sdidataArray, String sdcId, PropertyList props) throws SapphireException {
        PropertyList remeasureProps = new PropertyList();
        remeasureProps.put("sdcid", sdcId);
        remeasureProps.put("keyid1", sdidataArray.getColumnValues("keyid1", ";"));
        remeasureProps.put("keyid2", sdidataArray.getColumnValues("keyid2", ";"));
        remeasureProps.put("keyid3", sdidataArray.getColumnValues("keyid3", ";"));
        remeasureProps.put("paramlistid", sdidataArray.getColumnValues("paramlistid", ";"));
        remeasureProps.put("paramlistversionid", sdidataArray.getColumnValues("paramlistversionid", ";"));
        remeasureProps.put("variantid", sdidataArray.getColumnValues("variantid", ";"));
        remeasureProps.put("dataset", sdidataArray.getColumnValues("dataset", ";"));
        remeasureProps.put("newdsstatus", "Initial");
        remeasureProps.setProperty("auditreason", props.getProperty("auditreason"));
        remeasureProps.setProperty("auditactivity", props.getProperty("auditactivity"));
        remeasureProps.setProperty("auditsignedflag", props.getProperty("auditsignedflag"));
        remeasureProps.setProperty("propsmatch", "Y");
        this.getActionProcessor().processAction("RemeasureDataSet", "1", remeasureProps);
    }

    private void updatePreviousArrayMethodInstanceAsRemeasured(PropertyList properties, String arrayid, String arrayMethodId, String arrayMethodVersionId, String arrayMethodInstance, String arrayMethodStatus) throws SapphireException {
        PropertyList editStatus = new PropertyList();
        editStatus.setProperty("sdcid", "LV_Array");
        editStatus.setProperty("keyid1", arrayid);
        editStatus.setProperty("linkid", "Array ArrayMethod Item");
        editStatus.setProperty("arraymethodid", arrayMethodId);
        editStatus.setProperty("arraymethodversionid", arrayMethodVersionId);
        editStatus.setProperty("arraymethodinstance", arrayMethodInstance);
        editStatus.setProperty("arraymethoditemstatus", "Remeasured");
        editStatus.setProperty("remeasuredflag", "Y");
        editStatus.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        editStatus.setProperty("auditactivity", properties.getProperty("auditactivity"));
        editStatus.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("EditSDIDetail", "1", editStatus);
    }
}

