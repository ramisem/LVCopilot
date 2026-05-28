/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_QCBatchReagent
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 93092 $";
    private final String PROPERTY_REAGENTLOTID = "reagentlotid";
    private final String PROPERTY_TRACKITEMID = "trackitemid";
    private final String PROPERTY_AMOUNT = "amount";
    private final String PROPERTY_AMOUNTUNITS = "amountunits";
    private final String PROPERTY_AMOUNTUNITSTYPE = "amountunitstype";
    private final String PROPERTY_INSTRUMENTID = "instrumentid";
    private final String PROPERTY_REAGENTTYPEID = "reagenttypeid";
    private final String PROPERTY_SOURCEFLAG = "sourceflag";

    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setDefaultUnit(primary);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setDefaultUnit(primary);
        DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
        QCUtil.setOriginalReagent(primary, oldPrimary);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.updateConsumables(primary, actionProps);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.updateConsumables(primary, actionProps);
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

    private int getMultiplicationForInventory(String qcbatchreagentid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select s_qcbatchitemid FROM s_qcbatchitem bi,s_qcbatchreagent br");
        sql.append(" WHERE s_qcbatchreagentid = ").append(safeSQL.addVar(qcbatchreagentid));
        sql.append(" and bi.s_qcbatchid = br.qcbatchid");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return ds.getRowCount();
    }

    private void syncDataRelation(String qcbatchreagentid, String qcbatchid, String reagenttypeid, String sourceflag, String reagentlotid, String trackitemid, String amount, String amountunits, String amountunitstype, String instrumentid, boolean edit) throws SapphireException {
        if (sourceflag.equalsIgnoreCase("W")) {
            this.syncSDIWorkItemRelation(qcbatchid, reagenttypeid, reagentlotid, trackitemid, amount, amountunits, amountunitstype, instrumentid, edit);
        } else if (sourceflag.equalsIgnoreCase("S")) {
            this.syncSDIDataRelation(qcbatchreagentid, qcbatchid, reagentlotid, trackitemid, amount, amountunits, amountunitstype, instrumentid, edit);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProperties) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String[] keyid1 = StringUtil.split(actionProperties.getProperty("keyid1"), ";");
        for (int i = 0; i < keyid1.length; ++i) {
            String qcbatchreagentid = keyid1[i];
            if (StringUtil.getLen(qcbatchreagentid) <= 0L) continue;
            SafeSQL safeSQL = new SafeSQL();
            sql.setLength(0);
            sql.append("select * from s_qcbatchreagent where s_qcbatchreagentid = ").append(safeSQL.addVar(qcbatchreagentid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() <= 0) continue;
            String qcbatchid = ds.getValue(0, "qcbatchid");
            String reagenttypeid = ds.getValue(0, "reagenttypeid");
            String sourceflag = ds.getValue(0, "sourceflag");
            String reagentlotid = ds.getValue(0, "reagentlotid");
            String trackitemid = ds.getValue(0, "trackitemid");
            if (StringUtil.getLen(reagentlotid) <= 0L || StringUtil.getLen(trackitemid) <= 0L) continue;
            this.syncDataRelation(qcbatchreagentid, qcbatchid, reagenttypeid, sourceflag, reagentlotid, trackitemid, "", "", "", "", false);
        }
    }

    private void syncSDIDataRelation(String qcbatchreagentid, String qcbatchid, String reagentlotid, String trackitemid, String amount, String amountunits, String amountunitstype, String instrumentid, boolean edit) throws SapphireException {
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        SafeSQL safeSQL = new SafeSQL();
        if (edit) {
            StringBuffer sqlSDIdatarelation = new StringBuffer();
            sqlSDIdatarelation.setLength(0);
            sqlSDIdatarelation.append("select distinct sdr.relationid ");
            sqlSDIdatarelation.append(" from sdidata sd,sdidatarelation sdr,s_qcbatchreagent qcbr");
            sqlSDIdatarelation.append(" where sd.sdcid =sdr.sdcid ");
            sqlSDIdatarelation.append(" and sd.keyid1 = sdr.keyid1 ");
            sqlSDIdatarelation.append(" and sd.keyid2 = sdr.keyid2 ");
            sqlSDIdatarelation.append(" and sd.keyid3 = sdr.keyid3 ");
            sqlSDIdatarelation.append(" and sd.paramlistid = sdr.paramlistid");
            sqlSDIdatarelation.append(" and sd.paramlistversionid = sdr.paramlistversionid");
            sqlSDIdatarelation.append(" and sd.variantid = sdr.variantid");
            sqlSDIdatarelation.append(" and sd.dataset = sdr.dataset");
            sqlSDIdatarelation.append(" and sdr.relationfunction = 'Reagent'");
            sqlSDIdatarelation.append(" and sd.s_qcbatchid = qcbr.qcbatchid");
            sqlSDIdatarelation.append(" and sdr.relationtype = qcbr.reagenttypeid");
            sqlSDIdatarelation.append(" and qcbr.s_qcbatchreagentid = ").append(safeSQL.addVar(qcbatchreagentid));
            DataSet dslSDIdatarelation = this.getQueryProcessor().getPreparedSqlDataSet(sqlSDIdatarelation.toString(), safeSQL.getValues());
            if (dslSDIdatarelation != null && dslSDIdatarelation.size() > 0) {
                sqlSDIdatarelation.setLength(0);
                safeSQL.reset();
                sqlSDIdatarelation.append("update sdidatarelation set tosdcid = 'LV_ReagentLot'");
                sqlSDIdatarelation.append(",tokeyid1 = ").append(safeSQL.addVar(reagentlotid));
                sqlSDIdatarelation.append(",refsdcid = 'TrackItemSDC'");
                sqlSDIdatarelation.append(",refkeyid1 = ").append(safeSQL.addVar(trackitemid));
                if (amount != null && amount.length() > 0) {
                    sqlSDIdatarelation.append(",amount = ").append(safeSQL.addVar(amount.replace(decimalSeparator, '.')));
                }
                sqlSDIdatarelation.append(",amountunits = ").append(safeSQL.addVar(amountunits));
                sqlSDIdatarelation.append(",amountunitstype = ").append(safeSQL.addVar(amountunitstype));
                if (instrumentid != null && instrumentid.length() > 0) {
                    sqlSDIdatarelation.append(",instrumentid = ").append(safeSQL.addVar(instrumentid));
                }
                sqlSDIdatarelation.append(" where relationid in (").append(safeSQL.addIn(dslSDIdatarelation.getColumnValues("relationid", "','"))).append(")");
                this.database.executePreparedUpdate(sqlSDIdatarelation.toString(), safeSQL.getValues());
            }
        } else {
            StringBuffer sql = new StringBuffer();
            safeSQL.reset();
            sql.append("select distinct s1.relationid");
            sql.append(" from sdidatarelation s1, sdidata s2");
            sql.append(" where s1.sdcid = s2.sdcid");
            sql.append(" and s1.keyid1 = s2.keyid1");
            sql.append(" and s1.paramlistid = s2.paramlistid");
            sql.append(" and s1.paramlistversionid = s2.paramlistversionid");
            sql.append(" and s1.variantid = s2.variantid");
            sql.append(" and s1.dataset = s1.dataset");
            sql.append(" and s2.s_qcbatchid = ").append(safeSQL.addVar(qcbatchid));
            sql.append(" and s1.tosdcid = 'LV_ReagentLot'");
            sql.append(" and s1.tokeyid1 = ").append(safeSQL.addVar(reagentlotid));
            sql.append(" and s1.refsdcid = 'TrackItemSDC'");
            sql.append(" and s1.refkeyid1 = ").append(safeSQL.addVar(trackitemid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("update sdidatarelation set tosdcid = '', tokeyid1 = '', refsdcid = '', refkeyid1 = '',amount = null, amountunits = '', amountunitstype = '' where relationid in (").append(safeSQL.addIn(ds.getColumnValues("relationid", "','"))).append(")");
                this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
            }
        }
    }

    private void syncSDIWorkItemRelation(String qcbatchid, String reagenttypeid, String reagentlotid, String trackitemid, String amount, String amountunits, String amountunitstype, String instrumentid, boolean edit) throws SapphireException {
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct swir.relationid ");
        sql.append(" FROM sdidata sdid, SDIWORKITEM sdiw,sdiworkitemrelation swir");
        sql.append(" WHERE sdid.s_qcbatchid = ").append(safeSQL.addVar(qcbatchid));
        sql.append(" AND sdid.SDCID = sdiw.SDCID");
        sql.append(" AND sdid.KEYID1 = sdiw.KEYID1");
        sql.append(" AND sdid.SOURCEWORKITEMID = sdiw.WORKITEMID");
        sql.append(" AND sdid.SOURCEWORKITEMINSTANCE = sdiw.WORKITEMINSTANCE");
        sql.append(" AND swir.SDCID=sdid.SDCID");
        sql.append(" and swir.KEYID1=sdid.KEYID1");
        sql.append(" and swir.WORKITEMID= sdid.SOURCEWORKITEMID");
        sql.append(" and swir.WORKITEMINSTANCE=sdid.SOURCEWORKITEMINSTANCE");
        sql.append(" and swir.RELATIONTYPE = ").append(safeSQL.addVar(reagenttypeid));
        sql.append(" and swir.relationfunction = 'Reagent'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            sql.setLength(0);
            safeSQL.reset();
            sql.append("update sdiworkitemrelation set");
            if (edit) {
                sql.append(" tosdcid = 'LV_ReagentLot'");
                sql.append(",tokeyid1 = ").append(safeSQL.addVar(reagentlotid));
                sql.append(",refsdcid = 'TrackItemSDC'");
                sql.append(",refkeyid1 = ").append(safeSQL.addVar(trackitemid));
                if (amount != null && amount.length() > 0) {
                    sql.append(",amount = ").append(safeSQL.addVar(amount.replace(decimalSeparator, '.')));
                }
                sql.append(",amountunits = ").append(safeSQL.addVar(amountunits));
                sql.append(",amountunitstype = ").append(safeSQL.addVar(amountunitstype));
                if (instrumentid != null && instrumentid.length() > 0) {
                    sql.append(",instrumentid = ").append(safeSQL.addVar(instrumentid));
                }
            } else {
                sql.append(" tosdcid = '', tokeyid1 = '', refsdcid = '', refkeyid1 = '', amount = null, amountunits = '', amountunitstype = ''");
            }
            sql.append(" where relationid in (").append(safeSQL.addIn(ds.getColumnValues("relationid", "','"))).append(")");
            this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
        }
    }

    private void setDefaultUnit(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            if ((!this.hasPrimaryValueChanged(primary, i, "trackitemid") || primary.getString(i, "trackitemid", "").length() <= 0) && (!this.hasPrimaryValueChanged(primary, i, "amount") || primary.getValue(i, "amount", "").length() <= 0)) continue;
            ReagentUtil.setDefaultUsedAmountUnit(primary, i, this.getQueryProcessor());
        }
    }

    private void updateConsumables(DataSet primary, PropertyList actionProps) throws SapphireException {
        String[] prevAmountArr = null;
        if (actionProps.containsKey("prevamount")) {
            prevAmountArr = StringUtil.split(actionProps.getProperty("prevamount", ""), ";");
        }
        for (int i = 0; i < primary.size(); ++i) {
            String oldamount;
            if (!this.hasPrimaryValueChanged(primary, i, "reagentlotid") && !this.hasPrimaryValueChanged(primary, i, "trackitemid") && !this.hasPrimaryValueChanged(primary, i, "amount") && !this.hasPrimaryValueChanged(primary, i, "amountunits") && !this.hasPrimaryValueChanged(primary, i, "amountunitstype")) continue;
            String qcbatchid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_qcbatchreagent", "qcbatchid", "s_qcbatchreagentid = ?", new String[]{primary.getValue(i, "s_qcbatchreagentid", "")});
            String qcbatchreagentid = primary.getString(i, "s_qcbatchreagentid", "");
            String reagenttypeid = primary.getString(i, "reagenttypeid", "");
            reagenttypeid = reagenttypeid.trim().length() > 0 ? reagenttypeid : this.getOldPrimaryValue(primary, i, "reagenttypeid");
            String sourceflag = this.getOldPrimaryValue(primary, i, "sourceflag");
            String oldreagentlotid = this.getOldPrimaryValue(primary, i, "reagentlotid");
            String oldtrackitemid = this.getOldPrimaryValue(primary, i, "trackitemid");
            String oldamountunits = this.getOldPrimaryValue(primary, i, "amountunits");
            String oldamountunitstype = this.getOldPrimaryValue(primary, i, "amountunitstype");
            String reagentlotid = primary.getString(i, "reagentlotid", "");
            String trackitemid = primary.getString(i, "trackitemid", "");
            String amount = primary.getValue(i, "amount", "");
            String amountunits = primary.getString(i, "amountunits", "");
            String amountunitstype = primary.getString(i, "amountunitstype", "");
            String instrumentid = primary.getString(i, "instrumentid", "");
            if (StringUtil.getLen(reagentlotid) > 0L && StringUtil.getLen(trackitemid) > 0L) {
                this.syncDataRelation(qcbatchreagentid, qcbatchid, reagenttypeid, sourceflag, reagentlotid, trackitemid, amount, amountunits, amountunitstype, instrumentid, true);
            } else if (StringUtil.getLen(trackitemid) == 0L && StringUtil.getLen(oldreagentlotid) > 0L && StringUtil.getLen(oldtrackitemid) > 0L) {
                this.syncDataRelation(qcbatchreagentid, qcbatchid, reagenttypeid, sourceflag, oldreagentlotid, oldtrackitemid, "", "", "", "", false);
            }
            int batchitemcount = this.getMultiplicationForInventory(qcbatchreagentid);
            if (prevAmountArr != null && prevAmountArr.length > i) {
                oldamount = prevAmountArr[i];
            } else {
                oldamount = this.getOldPrimaryValue(primary, i, "amount");
                oldamount = this.getMultipliedValue(batchitemcount, oldamount);
            }
            amount = this.getMultipliedValue(batchitemcount, amount);
            ReagentUtil.updateTrackItemInventory(oldtrackitemid, oldamount, oldamountunits, oldamountunitstype, trackitemid, amount, amountunits, amountunitstype, this.getQueryProcessor(), this.getActionProcessor());
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

