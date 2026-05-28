/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.actions.sdidata.EnterDataItem;
import com.labvantage.sapphire.actions.sdidata.ExtendDataSet;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCBatchSampleType
extends BaseSDCRules {
    public static String LABVANTAGE_CVS_ID = "$Revision: 93092 $";
    private final String PROPERTY_REAGENTLOTID = "reagentlotid";
    private final String PROPERTY_TRACKITEMID = "trackitemid";
    private final String PROPERTY_AMOUNT = "amount";
    private final String PROPERTY_AMOUNTUNITS = "amountunits";
    private final String PROPERTY_AMOUNTUNITSTYPE = "amountunitstype";
    private final String PROPERTY_AMOUNTSCOPEFLAG = "amountscopeflag";

    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setDefaultUnit(primary);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList list) throws SapphireException {
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setDefaultUnit(primary);
        DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
        QCUtil.setOriginalReagent(primary, oldPrimary);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "reagentlotid")) continue;
            this.syncReagentLotParams(primary.getValue(i, "reagentlotid"), primary.getValue(i, "s_qcbatchsampletypeid"));
        }
        this.updateSamplesWithReagentLot(primary);
        this.updateTrackItemInventory(primary, list);
    }

    private void syncReagentLotParams(String reagentlotid, String qcbatchsampletypeid) {
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select distinct paramid, paramtype, '").append(qcbatchsampletypeid).append("' qcbatchsampletypeid,");
            sql.append(" 'QCParams' paramlistid, '1' paramlistversionid, '1' variantid, '1' dataset");
            sql.append(" from sdidataitem ");
            sql.append(" where sdcid = 'LV_ReagentLot'");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(reagentlotid));
            sql.append(" and paramid not in (select distinct di.paramid ");
            sql.append(" from sdidataitem di ");
            sql.append(" where di.sdcid = 'QCBatchSampleType'");
            sql.append(" and di.keyid1 = ").append(safeSQL.addVar(qcbatchsampletypeid)).append(")");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                ds.setString(-1, "paramtype", "Concentration");
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "QCBatchSampleType");
                props.setProperty("keyid1", ds.getColumnValues("qcbatchsampletypeid", ";"));
                props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
                props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
                props.setProperty("paramid", ds.getColumnValues("paramid", ";"));
                props.setProperty("paramtype", ds.getColumnValues("paramtype", ";"));
                props.setProperty("propsmatch", "Y");
                this.getActionProcessor().processActionClass(ExtendDataSet.class.getName(), props);
            }
            sql.setLength(0);
            safeSQL.reset();
            sql.append("select paramid, enteredtext");
            sql.append(" from sdidataitem  ");
            sql.append(" where sdcid = 'LV_ReagentLot' ");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(reagentlotid));
            sql.append(" and enteredtext is not null");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                DataSet data = new DataSet();
                data.addColumn("keyid1", 0);
                data.addColumn("paramlistid", 0);
                data.addColumn("paramlistversionid", 0);
                data.addColumn("variantid", 0);
                data.addColumn("dataset", 1);
                data.addColumn("paramid", 0);
                data.addColumn("paramtype", 0);
                data.addColumn("replicateid", 1);
                data.addColumn("enteredtext", 0);
                for (int i = 0; i < ds.size(); ++i) {
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("select keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, enteredtext");
                    sql.append(" from sdidataitem");
                    sql.append(" where sdcid = 'QCBatchSampleType'");
                    sql.append(" and keyid1 = ").append(safeSQL.addVar(qcbatchsampletypeid));
                    sql.append(" and paramid = ").append(safeSQL.addVar(ds.getValue(i, "paramid")));
                    DataSet de = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds == null || ds.size() <= 0 || StringUtil.getLen(de.getValue(i, "enteredtext", "")) != 0L) continue;
                    data.copyRow(de, 0, 1);
                    data.setValue(data.size() - 1, "enteredtext", ds.getValue(i, "enteredtext"));
                }
                if (data.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "QCBatchSampleType");
                    props.setProperty("keyid1", data.getColumnValues("keyid1", ";"));
                    props.setProperty("paramlistid", data.getColumnValues("paramlistid", ";"));
                    props.setProperty("paramlistversionid", data.getColumnValues("paramlistversionid", ";"));
                    props.setProperty("variantid", data.getColumnValues("variantid", ";"));
                    props.setProperty("dataset", data.getColumnValues("dataset", ";"));
                    props.setProperty("paramid", data.getColumnValues("paramid", ";"));
                    props.setProperty("paramtype", data.getColumnValues("paramtype", ";"));
                    props.setProperty("replicateid", data.getColumnValues("replicateid", ";"));
                    props.setProperty("enteredtext", data.getColumnValues("enteredtext", ";"));
                    props.setProperty("propsmatch", "Y");
                    this.getActionProcessor().processActionClass(EnterDataItem.class.getName(), props);
                }
            }
        }
        catch (ActionException e) {
            this.logger.warn("WARNING: FAILED TO ADD PARAMETERS TO QCBATCHSAMPLETYPE (" + e.getMessage() + ")");
        }
    }

    @Override
    public void preDelete(String string, PropertyList list) throws SapphireException {
    }

    @Override
    public void postDelete(String string, PropertyList list) throws SapphireException {
    }

    private void updateSamplesWithReagentLot(DataSet primary) throws SapphireException {
        String sampleIds = "";
        String reagentlotIds = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT ds.keyid1 ").append(" FROM sdidata ds, s_qcbatchitem qbi ").append(" WHERE ds.sdcid = 'Sample' AND ds.s_qcbatchid = qbi.s_qcbatchid AND ds.s_qcbatchitemid = qbi.s_qcbatchitemid AND qbi.qcbatchsampletypeid = ? ");
        try {
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "reagentlotid")) continue;
                String qcBatchSampleTypeId = primary.getValue(i, "s_qcbatchsampletypeid");
                this.database.createPreparedResultSet("sampleRS", sql.toString(), new String[]{qcBatchSampleTypeId});
                while (this.database.getNext("sampleRS")) {
                    sampleIds = sampleIds + ";" + this.database.getValue("sampleRS", "keyid1");
                    reagentlotIds = reagentlotIds + ";" + primary.getValue(i, "reagentlotid");
                }
            }
            if (sampleIds.trim().length() > 1) {
                ActionProcessor ap = this.getActionProcessor();
                PropertyList editProp = new PropertyList();
                editProp.setProperty("sdcid", "Sample");
                editProp.setProperty("keyid1", sampleIds.substring(1));
                editProp.setProperty("reagentlotid", reagentlotIds.substring(1));
                ap.processAction("EditSDI", "1", editProp);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to update Sample with ReagentLot. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeResultSet("sampleRS");
        }
    }

    private void updateTrackItemInventory(DataSet primary, PropertyList actionProps) throws SapphireException {
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("primary");
        String[] prevAmountArr = null;
        if (actionProps.containsKey("prevamount")) {
            prevAmountArr = StringUtil.split(actionProps.getProperty("prevamount", ""), ";");
        }
        for (int i = 0; i < primary.size(); ++i) {
            String oldamount;
            if (!this.hasPrimaryValueChanged(primary, i, "reagentlotid") && !this.hasPrimaryValueChanged(primary, i, "trackitemid") && !this.hasPrimaryValueChanged(primary, i, "amount") && !this.hasPrimaryValueChanged(primary, i, "amountunits")) continue;
            String qcbatchsampletypeid = primary.getValue(i, "s_qcbatchsampletypeid");
            String amountscope = primary.isValidColumn("amountscopeflag") ? primary.getValue(i, "amountscopeflag") : oldPrimary.getValue(i, "amountscopeflag");
            int batchitemcount = this.getMultiplicationForInventory(qcbatchsampletypeid, amountscope);
            String oldtrackitemid = this.getOldPrimaryValue(primary, i, "trackitemid");
            if (prevAmountArr != null && prevAmountArr.length > i) {
                oldamount = prevAmountArr[i];
            } else {
                oldamount = this.getOldPrimaryValue(primary, i, "amount");
                oldamount = this.getMultipliedValue(batchitemcount, oldamount);
            }
            String oldamountunits = this.getOldPrimaryValue(primary, i, "amountunits");
            String oldamountunitstype = this.getOldPrimaryValue(primary, i, "amountunitstype");
            String trackitemid = primary.getString(i, "trackitemid", "");
            String amount = primary.getValue(i, "amount", "");
            amount = this.getMultipliedValue(batchitemcount, amount);
            String amountunits = primary.getString(i, "amountunits", "");
            String amountunitstype = primary.getString(i, "amountunitstype", "");
            ReagentUtil.updateTrackItemInventory(oldtrackitemid, oldamount, oldamountunits, oldamountunitstype, trackitemid, amount, amountunits, amountunitstype, this.getQueryProcessor(), this.getActionProcessor());
        }
    }

    private String getMultipliedValue(int multifactor, String value) {
        if (multifactor > 1 && value.length() > 0) {
            char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
            double doubleValue = Double.parseDouble(value.replace(decimalSeparator, '.'));
            value = Double.toString(doubleValue *= (double)multifactor);
            value = value.replace('.', decimalSeparator);
        }
        return value;
    }

    private int getMultiplicationForInventory(String qcbatchsampletypeid, String amountscope) {
        int count = 1;
        if (amountscope.equalsIgnoreCase("S")) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select s_qcbatchitemid FROM s_qcbatchitem");
            sql.append(" WHERE qcbatchsampletypeid = ").append(safeSQL.addVar(qcbatchsampletypeid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            count = ds.getRowCount();
        }
        return count;
    }

    private void setDefaultUnit(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            if ((!this.hasPrimaryValueChanged(primary, i, "trackitemid") || primary.getString(i, "trackitemid", "").length() <= 0) && (!this.hasPrimaryValueChanged(primary, i, "amount") || primary.getValue(i, "amount", "").length() <= 0)) continue;
            ReagentUtil.setDefaultUsedAmountUnit(primary, i, this.getQueryProcessor());
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

