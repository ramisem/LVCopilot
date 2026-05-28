/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.admin.ddt.DDTUtil;
import com.labvantage.sapphire.admin.ddt.InstrumentUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class Instrument
extends BaseSDCRules {
    private String LABVANTAGE_CVS_ID = "$Revision: 86953 $";
    private int newSDMSInstrCount = 0;

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String certReqFlag = "";
        for (int i = 0; i < primary.getRowCount(); ++i) {
            certReqFlag = primary.getString(i, "certificationreqflag");
            if (certReqFlag != null && (certReqFlag.equalsIgnoreCase("Y") || certReqFlag.equalsIgnoreCase("P"))) {
                primary.setString(i, "instrumentstatus", "Initial");
            } else {
                primary.setString(i, "instrumentstatus", "Available");
            }
            this.updateSDMSInstrumentCount(primary, i, true);
            if (!this.isCMTImport()) {
                this.checkLabWorkArea(primary, i);
            }
            this.setCapturerTypeFromModel(primary, i);
        }
        this.validateSDMSInstrumentCount();
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.addPlanItemFromModel(primary);
        String templateKeyId = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1", ""));
        if (templateKeyId.length() > 0) {
            SchedulerUtil util = new SchedulerUtil(this.getConnectionId());
            util.copySchedulePlanItemsOnSource(this.getSdcid(), templateKeyId, null, null, this.getSdcid(), primary.getColumnValues("instrumentid", ";"), null, null, false);
            InstrumentUtil.copyCaptureOperationPropertiesFromTemplate(primary, actionProps, "Instrument", this.getQueryProcessor(), this.database);
        } else {
            this.copyCaptureOperationsFromModel(primary, false);
        }
        HashSet<String> checkCollectors = new HashSet<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "sdmscollectorid").length() <= 0) continue;
            checkCollectors.add(primary.getValue(i, "sdmscollectorid"));
        }
        if (checkCollectors.size() > 0) {
            PropertyList checkRebootProps = new PropertyList();
            checkRebootProps.setProperty("collectorid", String.join((CharSequence)";", checkCollectors));
            this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
        }
    }

    private void setCapturerTypeFromModel(DataSet primary, int i) throws SapphireException {
        String modelid = primary.getString(i, "instrumentmodelid", "");
        String type = primary.getString(i, "instrumenttype", "");
        if ((this.hasPrimaryValueChanged(primary, i, "instrumentmodelid") || this.hasPrimaryValueChanged(primary, i, "instrumenttype")) && modelid.length() > 0 && type.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select collectorpropertytreeid,collectorextendnodeid, postponeprocessingflag from instrumentmodel where instrumentmodelid = ").append(safeSQL.addVar(modelid)).append(" and instrumenttypeid=").append(safeSQL.addVar(type));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
            if (ds != null && ds.size() > 0) {
                primary.setString(i, "collectorpropertytreeid", ds.getString(0, "collectorpropertytreeid", ""));
                primary.setString(i, "collectorextendnodeid", ds.getString(0, "collectorextendnodeid", ""));
                primary.setString(i, "postponeprocessingflag", ds.getString(0, "postponeprocessingflag", ""));
            }
        }
    }

    private void addPlanItemFromModel(DataSet primary) {
        try {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String model = primary.getValue(i, "instrumentmodelid", "");
                String modelType = primary.getValue(i, "instrumenttype", "");
                String instrumentid = primary.getValue(i, "instrumentid", "");
                if (model.length() <= 0) continue;
                SchedulerUtil util = new SchedulerUtil(this.getConnectionId());
                util.copySchedulePlanItemsOnSource("LV_InstrumentModel", model, modelType, null, this.getSdcid(), instrumentid, null, null, false);
            }
        }
        catch (SapphireException e) {
            this.logger.error("Failed to copy SchedulePlanItems from Instrument Model");
        }
    }

    private void copyCaptureOperationsFromModel(DataSet primary, boolean edit) throws SapphireException {
        String sdcid_model = "LV_InstrumentModel";
        String sdcid_inst = "Instrument";
        DataSet toinsert = new DataSet();
        toinsert.addColumn("sdcid", 0);
        toinsert.addColumn("keyid1", 0);
        toinsert.addColumn("keyid2", 0);
        toinsert.addColumn("keyid3", 0);
        toinsert.addColumn("attachmentoperationid", 0);
        toinsert.addColumn("operationsdcid", 0);
        toinsert.addColumn("operationkeyid1", 0);
        toinsert.addColumn("attachmentclass", 0);
        toinsert.addColumn("synchronousflag", 0);
        toinsert.addColumn("processinggroupname", 0);
        toinsert.addColumn("usersequence", 1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer instrumentIds = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String model = primary.getValue(i, "instrumentmodelid", "");
            String modelType = primary.getValue(i, "instrumenttype", "");
            String instrumentid = primary.getValue(i, "instrumentid", "");
            if (edit) {
                if (!this.hasPrimaryValueChanged(primary, i, "instrumentmodelid") && !this.hasPrimaryValueChanged(primary, i, "instrumenttype")) continue;
                instrumentIds.append(",'").append(primary.getValue(i, "instrumentid")).append("'");
            }
            if (model.length() <= 0) continue;
            safeSQL.reset();
            StringBuffer sql = new StringBuffer("select * from sdiattachmentoperation sdico");
            sql.append(" where sdico.sdcid=").append(safeSQL.addVar(sdcid_model));
            sql.append(" and sdico.keyid1=").append(safeSQL.addVar(model));
            sql.append(" and sdico.keyid2=").append(safeSQL.addVar(modelType));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
            if (ds == null || ds.size() <= 0) continue;
            for (int row = 0; row < ds.size(); ++row) {
                int r = toinsert.addRow();
                toinsert.setValue(r, "sdcid", sdcid_inst);
                toinsert.setValue(r, "keyid1", instrumentid);
                toinsert.setValue(r, "keyid2", "(null)");
                toinsert.setValue(r, "keyid3", "(null)");
                toinsert.setValue(r, "attachmentoperationid", ds.getValue(row, "attachmentoperationid", ""));
                toinsert.setValue(r, "operationsdcid", ds.getValue(row, "operationsdcid", ""));
                toinsert.setValue(r, "operationkeyid1", ds.getValue(row, "operationkeyid1", ""));
                toinsert.setValue(r, "attachmentclass", ds.getValue(row, "attachmentclass", ""));
                toinsert.setValue(r, "synchronousflag", ds.getValue(row, "synchronousflag", ""));
                toinsert.setValue(r, "processinggroupname", ds.getValue(row, "processinggroupname", ""));
                toinsert.setNumber(r, "usersequence", ds.getInt(row, "usersequence", 1));
            }
        }
        if (instrumentIds.length() > 0) {
            this.deleteCaptureOperation(null, instrumentIds.substring(1));
        }
        if (toinsert.getRowCount() > 0) {
            try {
                DataSetUtil.insert(this.database, toinsert, "sdiattachmentoperation");
            }
            catch (Exception e) {
                this.logger.warn("Failed to add capture operation. Error - " + e.getMessage());
                throw new SapphireException("Failed to add capture operation.", e);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (DDTUtil.checkTableExists(this.database, "s_sdicertification")) {
            this.database.executePreparedUpdate("DELETE FROM s_sdicertification WHERE resourcesdcid = 'Instrument' AND resourcekeyid1 IN ( SELECT keyid1 from rsetitems WHERE rsetid = ? ) ", new Object[]{rsetid});
        }
        String updatePartsSql = "update instrument set parentinstrumentid = null where parentinstrumentid in ( SELECT keyid1 from rsetitems WHERE rsetid = ? ) ";
        this.database.executePreparedUpdate(updatePartsSql, new Object[]{rsetid});
        this.instrumentDetail(rsetid, null);
        this.deleteCaptureOperation(rsetid, null);
        SafeSQL safeSQL = new SafeSQL();
        DataSet collectors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT distinct sdmscollectorid FROM instrument WHERE instrumentid in ( " + safeSQL.addIn(actionProps.getProperty("keyid1"), ";") + ")", safeSQL.getValues());
        HashSet<String> checkCollectors = new HashSet<String>();
        for (int i = 0; i < collectors.size(); ++i) {
            if (collectors.getValue(i, "sdmscollectorid").length() <= 0) continue;
            checkCollectors.add(collectors.getValue(i, "sdmscollectorid"));
        }
        if (checkCollectors.size() > 0) {
            PropertyList checkRebootProps = new PropertyList();
            checkRebootProps.setProperty("collectorid", String.join((CharSequence)";", checkCollectors));
            this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
        }
    }

    private void deleteCaptureOperation(String rsetid, String instrumentid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sdiCOSql = new StringBuffer();
        if (rsetid == null) {
            sdiCOSql.append("DELETE from sdiattachmentoperation where sdcid='Instrument' and keyid1 IN ( " + safeSQL.addIn(instrumentid) + " )");
        } else {
            sdiCOSql.append("DELETE from sdiattachmentoperation where sdcid='Instrument' and keyid1 IN ( SELECT keyid1 from rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )");
        }
        this.database.executePreparedUpdate(sdiCOSql.toString(), safeSQL.getValues());
    }

    private void instrumentDetail(String rsetid, String instrumentIds) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sqlPendingWOs = rsetid != null ? "UPDATE workorder SET workorderstatus='Cancelled', scheduleplanid = null, scheduleplanitemid = null  where sourcesdcid='Instrument' AND  workorderstatus='Pending' AND sourcekeyid1 IN ( SELECT keyid1 from rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "UPDATE workorder SET workorderstatus='Cancelled', scheduleplanid = null, scheduleplanitemid = null where sourcesdcid='Instrument' AND  workorderstatus='Pending' AND sourcekeyid1 in (" + safeSQL.addIn(instrumentIds) + ")";
        this.database.executePreparedUpdate(sqlPendingWOs, safeSQL.getValues());
        safeSQL.reset();
        String sqlSample = rsetid != null ? "select s_sampleid from s_sample where  instrumentid in ( SELECT keyid1 from rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) and  (((reviewrequiredflag ='N' or reviewrequiredflag is null) and samplestatus NOT IN ( 'Completed','Reviewed' )) or  (reviewrequiredflag ='Y' and samplestatus <> 'Reviewed'))" : "select s_sampleid from s_sample where  instrumentid in(" + safeSQL.addIn(instrumentIds) + ") and  (((reviewrequiredflag ='N' or reviewrequiredflag is null) and samplestatus NOT IN ( 'Completed','Reviewed' )) or  (reviewrequiredflag ='Y' and samplestatus <> 'Reviewed'))";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlSample, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
            props.setProperty("samplestatus", "Cancelled");
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        safeSQL.reset();
        String sqlPlanitems = rsetid != null ? "select scheduleplanid, scheduleplanitemid from scheduleplanitem where linksdcid='Instrument' and linkkeyid1 IN ( SELECT keyid1 from rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "select scheduleplanid, scheduleplanitemid from scheduleplanitem where linksdcid='Instrument' and linkkeyid1 in (" + safeSQL.addIn(instrumentIds) + ")";
        DataSet planItems = this.getQueryProcessor().getPreparedSqlDataSet(sqlPlanitems, safeSQL.getValues());
        if (planItems.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "SchedulePlanItem");
            props.setProperty("keyid1", planItems.getColumnValues("scheduleplanid", ";"));
            props.setProperty("keyid2", planItems.getColumnValues("scheduleplanitemid", ";"));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                this.updateSDMSInstrumentCount(primary, i, false);
                this.checkLabWorkArea(primary, i);
                this.setCapturerTypeFromModel(primary, i);
            }
            this.validateSDMSInstrumentCount();
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        SDIData sdiDataBEI = this.getBeforeEditImage();
        InstrumentUtil.instrumentPostEdit(this, sdiData, sdiDataBEI, actionProps, this.getQueryProcessor(), this.getActionProcessor(), this.getTranslationProcessor());
        PropertyList props = new PropertyList();
        String instrumentId = "";
        String resetflagvalue = "";
        DataSet primary = sdiData.getDataset("primary");
        boolean resetUsage = false;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "usagebytimeflag")) continue;
            resetUsage = true;
            instrumentId = instrumentId + ";" + primary.getValue(i, "instrumentid");
            resetflagvalue = resetflagvalue + ";N";
        }
        if (resetUsage) {
            props.setProperty("instrumentid", instrumentId.substring(1));
            props.setProperty("resettotalusagetime", resetflagvalue.substring(1));
            props.setProperty("resettotalusagecount", resetflagvalue.substring(1));
            props.setProperty("resetinstrumentparts", resetflagvalue.substring(1));
            this.getActionProcessor().processAction("ResetInstrumentUsage", "1", props);
        }
        DataSet newPrimary = sdiData.getDataset("primary");
        DataSet oldPrimary = sdiDataBEI.getDataset("primary");
        String instrumentIds = "";
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String oldCertificationRequiredFlag = oldPrimary.getValue(i, "certificationreqflag", "");
            String newCertificationRequiredFlag = newPrimary.getValue(i, "certificationreqflag", "");
            if (!oldCertificationRequiredFlag.equalsIgnoreCase("P") || !newCertificationRequiredFlag.equalsIgnoreCase("Y") && !newCertificationRequiredFlag.equalsIgnoreCase("N")) continue;
            instrumentIds = instrumentIds + ",'" + primary.getValue(i, "instrumentid") + "'";
        }
        if (instrumentIds.length() > 0) {
            this.instrumentDetail(null, instrumentIds.substring(1));
        }
        this.copyCaptureOperationsFromModel(primary, true);
        if (!actionProps.getProperty("skipconfighash").equals("Y")) {
            String[] cols = new String[]{"instrumentmodelid", "collectorpropertytreeid", "collectorextendnodeid", "collectorvaluetree"};
            HashSet<String> checkInstruments = new HashSet<String>();
            HashSet<String> checkCollectors = new HashSet<String>();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (this.hasPrimaryValueChanged(primary, i, "sdmscollectorid")) {
                    checkCollectors.add(primary.getValue(i, "sdmscollectorid"));
                    checkCollectors.add(this.getOldPrimaryValue(primary, i, "sdmscollectorid"));
                    continue;
                }
                for (String columnid : cols) {
                    if (!this.hasPrimaryValueChanged(primary, i, columnid)) continue;
                    checkInstruments.add(primary.getValue(i, "instrumentid"));
                }
            }
            if (checkInstruments.size() > 0) {
                PropertyList checkRebootProps = new PropertyList();
                checkRebootProps.setProperty("instrumentid", String.join((CharSequence)";", checkInstruments));
                this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
            }
            if (checkCollectors.size() > 0) {
                PropertyList checkRebootProps = new PropertyList();
                checkRebootProps.setProperty("collectorid", String.join((CharSequence)";", checkCollectors));
                this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
            }
        }
    }

    private void checkLabWorkArea(DataSet primary, int i) throws SapphireException {
        String labid = this.getColumnValue(primary, i, "testingdepartmentid");
        String workarea = this.getColumnValue(primary, i, "workareadepartmentid");
        if ((this.hasPrimaryValueChanged(primary, i, "workareadepartmentid") || this.hasPrimaryValueChanged(primary, i, "testingdepartmentid")) && workarea.length() > 0 && labid.length() > 0 && !labid.equals(workarea)) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(workarea)).append(" and parentdepartmentid=").append(safeSQL.addVar(labid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Work Area (" + workarea + ") is not in the Testing Lab (" + labid + ")."));
            }
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        return value;
    }

    private void updateSDMSInstrumentCount(DataSet primary, int i, boolean add) throws SapphireException {
        if (add) {
            String sdmscollectorid = primary.getString(i, "sdmscollectorid", "");
            String activeflag = primary.getString(i, "activeflag", "Y");
            if (sdmscollectorid.length() > 0 && activeflag.equalsIgnoreCase("Y")) {
                ++this.newSDMSInstrCount;
            }
        } else if (this.hasPrimaryValueChanged(primary, i, "sdmscollectorid") || this.hasPrimaryValueChanged(primary, i, "activeflag")) {
            boolean isSDMSInstrument;
            String newsdmscollectorid = this.getColumnValue(primary, i, "sdmscollectorid");
            String oldsdmscollectorid = this.getOldPrimaryValue(primary, i, "sdmscollectorid");
            String newActiveflag = this.getColumnValue(primary, i, "activeflag");
            String oldActiveflag = this.getOldPrimaryValue(primary, i, "activeflag");
            boolean wasSDMSInstrument = oldsdmscollectorid.length() > 0 && !oldActiveflag.equalsIgnoreCase("N");
            boolean bl = isSDMSInstrument = newsdmscollectorid.length() > 0 && !newActiveflag.equalsIgnoreCase("N");
            if (wasSDMSInstrument && !isSDMSInstrument) {
                --this.newSDMSInstrCount;
            } else if (!wasSDMSInstrument && isSDMSInstrument) {
                ++this.newSDMSInstrCount;
            }
        }
    }

    private void validateSDMSInstrumentCount() throws SapphireException {
        if (this.newSDMSInstrCount > 0) {
            int maxSdmsInstrCount = Configuration.getInstance().getLicense(this.getDatabaseid()).getSDMSInstrumentCount();
            this.database.createResultSet("SELECT count(*) count FROM instrument WHERE ( ACTIVEFLAG = 'Y' OR ACTIVEFLAG IS NULL ) AND sdmscollectorid is not null");
            int existingSDMSInstrCount = 0;
            if (this.database.getNext()) {
                existingSDMSInstrCount = this.database.getInt("count");
            }
            if (this.newSDMSInstrCount + existingSDMSInstrCount > maxSdmsInstrCount) {
                this.throwError("SDMSInstrumentCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of allowable SDMS Instruments."));
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

