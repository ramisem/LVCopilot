/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_QCBatchInstrument
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private String fieldName_qcbatchinstrumentid = "s_qcbatchinstrumentid";
    private String fieldName_qcbatchid = "qcbatchid";
    private String fieldName_instrumenttypeid = "instrumenttypeid";
    private String fieldName_instrumentmodelid = "instrumentmodelid";
    private String fieldName_instrumentid = "instrumentid";
    private String fieldName_sourceflag = "sourceflag";
    private String fieldName_instrumentinstance = "instrumentinstance";
    private String fieldName_instrumentcount = "instrumentcount";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, this.fieldName_instrumentid) && !this.hasPrimaryValueChanged(primary, i, this.fieldName_instrumentcount)) continue;
            String qcbatchinstrumentid = primary.getString(i, this.fieldName_qcbatchinstrumentid, "");
            String instrumentid = primary.getString(i, this.fieldName_instrumentid, "");
            String instrumentcount = primary.getValue(i, this.fieldName_instrumentcount, "");
            String oldInstrumentid = this.getOldPrimaryValue(primary, i, this.fieldName_instrumentid);
            boolean instrumentreplaced = this.hasPrimaryValueChanged(primary, i, this.fieldName_instrumentid) && oldInstrumentid != null && oldInstrumentid.length() > 0;
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select * from s_qcbatchinstrument where s_qcbatchinstrumentid = ").append(safeSQL.addVar(qcbatchinstrumentid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() <= 0) continue;
            String qcbatchid = ds.getValue(0, this.fieldName_qcbatchid, "");
            String instrumenttypeid = ds.getValue(0, this.fieldName_instrumenttypeid, "");
            String instrumentmodelid = ds.getValue(0, this.fieldName_instrumentmodelid, "");
            String instrumentinstance = ds.getValue(0, this.fieldName_instrumentinstance, "");
            String sourceflag = ds.getValue(0, this.fieldName_sourceflag, "");
            this.syncDataRelation(qcbatchinstrumentid, qcbatchid, instrumenttypeid, instrumentmodelid, instrumentinstance, sourceflag, instrumentid, oldInstrumentid, instrumentcount, instrumentreplaced, true);
        }
    }

    private void syncDataRelation(String qcbatchinstrumentid, String qcbatchid, String instrumenttypeid, String instrumentmodelid, String instrumentinstance, String sourceflag, String instrumentid, String oldInstrumentid, String instrumentcount, boolean instrumentreplaced, boolean edit) throws SapphireException {
        if (sourceflag.equalsIgnoreCase("W")) {
            this.syncSDIWorkItemRelation(qcbatchid, instrumenttypeid, instrumentmodelid, instrumentinstance, instrumentid, oldInstrumentid, instrumentcount, instrumentreplaced, edit);
        } else if (sourceflag.equalsIgnoreCase("S")) {
            this.syncSDIDataRelation(qcbatchinstrumentid, qcbatchid, instrumentmodelid, instrumentinstance, instrumentid, oldInstrumentid, instrumentcount, instrumentreplaced, edit);
        } else if (sourceflag.equalsIgnoreCase("D")) {
            this.syncSDIData(qcbatchid, instrumenttypeid, instrumentid, edit);
        }
    }

    private void syncSDIData(String qcbatchid, String instrumenttypeid, String instrumentid, boolean edit) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT  sd.sdcid,sd.keyid1,sd.keyid2,sd.keyid3,sd.paramlistid,sd.paramlistversionid,sd.variantid,sd.dataset,'" + instrumentid + "' instrumentid");
        sql.append("  FROM sdidata sd,paramlist pl");
        sql.append(" where sd.s_qcbatchid=").append(safeSQL.addVar(qcbatchid));
        sql.append(" and sd.paramlistid=pl.paramlistid");
        sql.append(" and sd.paramlistversionid=pl.paramlistversionid");
        sql.append(" and sd.variantid=pl.variantid");
        sql.append(" and pl.s_instrumenttype=").append(safeSQL.addVar(instrumenttypeid));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", ds.getString(0, "sdcid"));
            props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
            if (edit) {
                props.setProperty("s_instrumentid", ds.getColumnValues("instrumentid", ";"));
            } else {
                props.setProperty("s_instrumentid", "");
            }
            props.setProperty("propsmatch", "Y");
            this.getActionProcessor().processAction("EditDataSet", "1", props);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProperties) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String[] keyid1 = StringUtil.split(actionProperties.getProperty("keyid1"), ";");
        for (int i = 0; i < keyid1.length; ++i) {
            String qcbatchinstrumentid = keyid1[i];
            if (StringUtil.getLen(qcbatchinstrumentid) <= 0L) continue;
            SafeSQL safeSQL = new SafeSQL();
            sql.setLength(0);
            sql.append("select * from s_qcbatchinstrument where s_qcbatchinstrumentid = ").append(safeSQL.addVar(qcbatchinstrumentid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() <= 0) continue;
            String qcbatchid = ds.getValue(0, this.fieldName_qcbatchid);
            String instrumenttypeid = ds.getValue(0, this.fieldName_instrumenttypeid);
            String instrumentinstance = ds.getValue(0, this.fieldName_instrumentinstance);
            String sourceflag = ds.getValue(0, this.fieldName_sourceflag);
            String instrumentid = ds.getValue(0, this.fieldName_instrumentid, "");
            String instrumentcount = ds.getValue(0, this.fieldName_instrumentcount, "");
            if (StringUtil.getLen(instrumentid) <= 0L && StringUtil.getLen(instrumentcount) <= 0L) continue;
            this.syncDataRelation(qcbatchinstrumentid, qcbatchid, instrumenttypeid, "", instrumentinstance, sourceflag, instrumentid, instrumentid, instrumentcount, false, false);
        }
    }

    private void syncSDIDataRelation(String qcbatchinstrumentid, String qcbatchid, String instrumentmodelid, String instrumentinstance, String instrumentid, String oldInstrumentid, String instrumentcount, boolean instrumentreplaced, boolean edit) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        if (edit) {
            DataSet dslSDIdatarelation = this.getEligibleSDIDataRelations(qcbatchinstrumentid, instrumentmodelid, instrumentinstance, oldInstrumentid, false);
            if (instrumentreplaced && (dslSDIdatarelation == null || dslSDIdatarelation.size() == 0)) {
                dslSDIdatarelation = this.getEligibleSDIDataRelations(qcbatchinstrumentid, instrumentmodelid, instrumentinstance, oldInstrumentid, true);
            }
            if (dslSDIdatarelation != null && dslSDIdatarelation.size() > 0) {
                StringBuilder sqlSDIdatarelation = new StringBuilder();
                safeSQL.reset();
                sqlSDIdatarelation.append("update sdidatarelation set tosdcid = 'Instrument'");
                sqlSDIdatarelation.append(",tokeyid1 = ").append(safeSQL.addVar(instrumentid));
                if (instrumentcount != null && instrumentcount.length() > 0) {
                    sqlSDIdatarelation.append(",amount = ").append(safeSQL.addVar(instrumentcount));
                }
                sqlSDIdatarelation.append(" where relationid in (").append(safeSQL.addIn(dslSDIdatarelation.getColumnValues("relationid", "','"))).append(")");
                this.database.executePreparedUpdate(sqlSDIdatarelation.toString(), safeSQL.getValues());
            }
        } else {
            DataSet ds;
            StringBuilder sql = new StringBuilder();
            safeSQL.reset();
            sql.append("select DISTINCT s1.relationid");
            sql.append(" from sdidatarelation s1, sdidata s2");
            sql.append(" where s1.sdcid = s2.sdcid");
            sql.append(" and s1.keyid1 = s2.keyid1");
            sql.append(" and s1.paramlistid = s2.paramlistid");
            sql.append(" and s1.paramlistversionid = s2.paramlistversionid");
            sql.append(" and s1.variantid = s2.variantid");
            sql.append(" and s1.dataset = s1.dataset");
            sql.append(" and s2.s_qcbatchid = ").append(safeSQL.addVar(qcbatchid));
            sql.append(" and s1.tosdcid = 'Instrument'");
            sql.append(" and s1.tokeyid1 = ").append(safeSQL.addVar(instrumentid));
            if (instrumentinstance != null && instrumentinstance.length() > 0) {
                sql.append(" and s1.relationinstance = ").append(safeSQL.addVar(instrumentinstance));
            }
            if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 0) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("update sdidatarelation set tosdcid = '',tokeyid1 = '',amount=null where relationid in (").append(safeSQL.addIn(ds.getColumnValues("relationid", "','"))).append(")");
                this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
            }
        }
    }

    private void syncSDIWorkItemRelation(String qcbatchid, String instrumenttypeid, String instrumentmodelid, String instrumentinstance, String instrumentid, String oldInstrumentid, String instrumentcount, boolean instrumentreplaced, boolean edit) throws SapphireException {
        DataSet ds = this.getEligibleSDIWorkItemRelations(qcbatchid, instrumenttypeid, instrumentmodelid, instrumentinstance, oldInstrumentid, false);
        if (instrumentreplaced && (ds == null || ds.size() == 0)) {
            ds = this.getEligibleSDIWorkItemRelations(qcbatchid, instrumenttypeid, instrumentmodelid, instrumentinstance, oldInstrumentid, true);
        }
        if (ds != null && ds.size() > 0) {
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("update sdiworkitemrelation set");
            if (edit) {
                sql.append(" tosdcid = 'Instrument'");
                sql.append(",tokeyid1 = ").append(safeSQL.addVar(instrumentid));
                if (instrumentcount != null && instrumentcount.length() > 0) {
                    sql.append(",amount = ").append(safeSQL.addVar(instrumentcount));
                }
            } else {
                sql.append(" tosdcid = '',tokeyid1 = '',amount = null ");
            }
            sql.append(" where relationid in (").append(safeSQL.addIn(ds.getColumnValues("relationid", "','"))).append(")");
            this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
        }
    }

    private DataSet getEligibleSDIDataRelations(String qcbatchinstrumentid, String instrumentmodelid, String instrumentinstance, String oldinstrumentid, boolean checkInstrument) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select DISTINCT sdr.relationid ");
        sql.append(" from sdidata sd,sdidatarelation sdr,s_qcbatchinstrument qcbr");
        sql.append(" where sd.sdcid =sdr.sdcid ");
        sql.append(" and sd.keyid1 = sdr.keyid1 ");
        sql.append(" and sd.keyid2 = sdr.keyid2 ");
        sql.append(" and sd.keyid3 = sdr.keyid3 ");
        sql.append(" and sd.paramlistid = sdr.paramlistid");
        sql.append(" and sd.paramlistversionid = sdr.paramlistversionid");
        sql.append(" and sd.variantid = sdr.variantid");
        sql.append(" and sd.dataset = sdr.dataset");
        sql.append(" and sdr.relationfunction = 'Instrument'");
        sql.append(" and sd.s_qcbatchid = qcbr.qcbatchid");
        sql.append(" and sdr.relationtype = qcbr.instrumenttypeid");
        if (instrumentinstance != null && instrumentinstance.length() > 0) {
            sql.append(" and sdr.relationinstance = ").append(safeSQL.addVar(instrumentinstance));
        }
        if (checkInstrument) {
            sql.append(" and sdr.tosdcid = 'Instrument'");
            sql.append(" and sdr.tokeyid1 = ").append(safeSQL.addVar(oldinstrumentid));
            sql.append(" and sdr.sourcesdcid = 'Instrument'");
        } else if (instrumentmodelid.length() > 0) {
            sql.append(" and sdr.sourcesdcid = 'LV_InstrumentModel'");
            sql.append(" and sdr.sourcekeyid1 = qcbr.instrumentmodelid");
        }
        sql.append(" and qcbr.s_qcbatchinstrumentid = ").append(safeSQL.addVar(qcbatchinstrumentid));
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getEligibleSDIWorkItemRelations(String qcbatchid, String instrumenttypeid, String instrumentmodelid, String instrumentinstance, String oldinstrumentid, boolean checkInstrument) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select DISTINCT swir.relationid ");
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
        sql.append(" and swir.RELATIONTYPE = ").append(safeSQL.addVar(instrumenttypeid));
        sql.append(" and swir.relationfunction = 'Instrument'");
        if (instrumentinstance != null && instrumentinstance.length() > 0) {
            sql.append(" and swir.relationinstance = ").append(safeSQL.addVar(instrumentinstance));
        }
        if (checkInstrument) {
            sql.append(" and swir.tosdcid = 'Instrument'");
            sql.append(" and swir.tokeyid1 = ").append(safeSQL.addVar(oldinstrumentid));
            sql.append(" and swir.sourcesdcid = 'Instrument'");
        } else if (instrumentmodelid.length() > 0) {
            sql.append(" and swir.sourcesdcid = 'LV_InstrumentModel'");
            sql.append(" and swir.sourcekeyid1 = ").append(safeSQL.addVar(instrumentmodelid));
        }
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

