/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCBatchReagentSync
extends BaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 104454 $";
    public static final String PROPERTY_QCBATCHID = "qcbatchid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_VARIANTID = "variantid";
    public static final String PROPERTY_DATASET = "dataset";
    private final String PROPERTY_AMOUNT = "amount";
    private final String PROPERTY_AMOUNTADJUSTED = "amountadjusted";
    private final String PROPERTY_AMOUNTUNITS = "amountunits";
    private final String PROPERTY_AMOUNTUNITSTYPE = "amountunitstype";
    private final String PROPERTY_AMOUNTRECOMMENDED = "amountrecommended";
    private final String PROPERTY_AMOUNTRECOMMENDEDUNITS = "amountrecommendedunits";
    private final String PROPERTY_AMOUNTRECOMMENDEDUNITSTYPE = "amountrecommendedunitstype";
    private final String PROPERTY_AMOUNTSCOPEFLAG = "amountscopeflag";
    private final String PROPERTY_REAGENTTYPEID = "reagenttypeid";
    private final String PROPERTY_REAGENTTYPEVERSIONID = "reagenttypeversionid";
    private final String PROPERTY_ORIGINALREAGENTTYPEID = "originalreagenttypeid";
    private final String PROPERTY_ORIGINALREAGENTTYPEVERSIONID = "originalreagenttypeversionid";
    private final String PROPERTY_SOURCEFLAG = "sourceflag";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        block4: {
            String qcbatchid;
            block3: {
                qcbatchid = props.getProperty(PROPERTY_QCBATCHID);
                String keyid1 = props.getProperty(PROPERTY_KEYID1);
                String paramlistid = props.getProperty(PROPERTY_PARAMLISTID);
                String paramlistversionid = props.getProperty(PROPERTY_PARAMLISTVERSIONID);
                String variantid = props.getProperty(PROPERTY_VARIANTID);
                String dataset = props.getProperty(PROPERTY_DATASET);
                if (keyid1.length() <= 0) break block3;
                if (StringUtil.split(paramlistid, ";").length != StringUtil.split(keyid1, ";").length || StringUtil.split(paramlistversionid, ";").length != StringUtil.split(keyid1, ";").length || StringUtil.split(variantid, ";").length != StringUtil.split(keyid1, ";").length || StringUtil.split(dataset, ";").length != StringUtil.split(keyid1, ";").length || StringUtil.split(qcbatchid, ";").length != StringUtil.split(keyid1, ";").length) {
                    throw new SapphireException(this.getTranslationProcessor().translate("QCBatchId, Keyid1, ParamListId, VersionId, VariantId and Dataset are not specified for all QCBatch datasets"));
                }
                DataSet ds = new DataSet();
                ds.addColumnValues(PROPERTY_KEYID1, 0, keyid1, ";");
                ds.addColumnValues(PROPERTY_PARAMLISTID, 0, paramlistid, ";");
                ds.addColumnValues(PROPERTY_PARAMLISTVERSIONID, 0, paramlistversionid, ";");
                ds.addColumnValues(PROPERTY_VARIANTID, 0, variantid, ";");
                ds.addColumnValues(PROPERTY_DATASET, 1, dataset, ";");
                ds.addColumnValues(PROPERTY_QCBATCHID, 0, qcbatchid, ";");
                ds.sort(PROPERTY_QCBATCHID);
                ArrayList<DataSet> qcbGrps = ds.getGroupedDataSets(PROPERTY_QCBATCHID);
                for (int g = 0; g < qcbGrps.size(); ++g) {
                    DataSet qcbSDIData = (DataSet)qcbGrps.get(g);
                    String qcBatchId = qcbSDIData.getValue(0, PROPERTY_QCBATCHID);
                    if (qcBatchId.length() <= 0) continue;
                    this.processRuleForReagent(qcBatchId, qcbSDIData);
                    this.processRuleForInstrument(qcBatchId, qcbSDIData);
                }
                break block4;
            }
            if (StringUtil.getLen(qcbatchid) <= 0L) break block4;
            String[] s = StringUtil.split(qcbatchid, ";");
            for (int i = 0; i < s.length; ++i) {
                this.processRuleForReagent(s[i], null);
                this.processRuleForInstrument(s[i], null);
            }
        }
    }

    private void processRuleForReagent(String qcbatchid, DataSet sdidata) {
        try {
            DataSet ds;
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            if (sdidata == null) {
                sql.append("select q.s_qcbatchsampletypeid, m.reagenttypeid, m.reagenttypeversionid, m.amount, m.amountunits, m.amountunitstype,m.amountscopeflag");
                sql.append(" from s_qcmethodsampletype m, s_qcbatchsampletype q");
                sql.append(" where q.qcmethodsampletypeid = m.s_qcmethodsampletypeid");
                sql.append(" and q.qcbatchid = ").append(safeSQL.addVar(qcbatchid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "QCBatchSampleType");
                    props.setProperty(PROPERTY_KEYID1, ds.getColumnValues("s_qcbatchsampletypeid", ";"));
                    props.setProperty("reagenttypeid", ds.getColumnValues("reagenttypeid", ";"));
                    props.setProperty("reagenttypeversionid", ds.getColumnValues("reagenttypeversionid", ";"));
                    props.setProperty("originalreagenttypeid", ds.getColumnValues("reagenttypeid", ";"));
                    props.setProperty("originalreagenttypeversionid", ds.getColumnValues("reagenttypeversionid", ";"));
                    props.setProperty("amountadjusted", ds.getColumnValues("amount", ";"));
                    props.setProperty("amountrecommended", ds.getColumnValues("amount", ";"));
                    props.setProperty("amountrecommendedunits", ds.getColumnValues("amountunits", ";"));
                    props.setProperty("amountrecommendedunitstype", ds.getColumnValues("amountunitstype", ";"));
                    props.setProperty("amountscopeflag", ds.getColumnValues("amountscopeflag", ";"));
                    props.setProperty("propsmatch", "Y");
                    props.setProperty("__sdcruleconfirm", "Y");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
            safeSQL.reset();
            sql.setLength(0);
            sql.append("(");
            sql.append("SELECT DISTINCT wir.REAGENTTYPEID,wir.REAGENTTYPEVERSIONID,");
            sql.append(" CASE WHEN wir.WORKITEMITEMID is not null THEN 'S' ELSE 'W' End sourceflag,wir.amount,wir.amountunits,wir.amountunitstype");
            if (sdidata != null) {
                sql.append(" , sdid.keyid1, sdid.paramlistid, sdid.paramlistversionid, sdid.variantid, sdid.dataset ");
            }
            sql.append(" FROM sdidata sdid, SDIWORKITEM sdiw, WORKITEMREAGENTTYPE wir, sdiworkitemitem sdiwii");
            sql.append(" WHERE sdid.sdcid='Sample' and  sdid.s_qcbatchid  = ").append(safeSQL.addVar(qcbatchid));
            sql.append(" AND sdid.availabilityflag = 'Y'");
            sql.append(" AND sdiw.sdcid = 'Sample'");
            sql.append(" AND sdiw.keyid1 = sdid.keyid1");
            sql.append(" AND sdiw.keyid2 = sdid.keyid2");
            sql.append(" AND sdiw.keyid3 = sdid.keyid3");
            sql.append(" AND sdiw.WORKITEMID = sdid.SOURCEWORKITEMID ");
            sql.append(" AND sdiw.WORKITEMINSTANCE = sdid.SOURCEWORKITEMINSTANCE ");
            sql.append(" AND sdiwii.sdcid = 'Sample'");
            sql.append(" AND sdiwii.keyid1 = sdid.keyid1 ");
            sql.append(" AND sdiwii.keyid2 = sdid.keyid2 ");
            sql.append(" AND sdiwii.keyid3 = sdid.keyid3 ");
            sql.append(" AND sdiwii.workitemid  = sdid.sourceworkitemid AND sdiwii.workiteminstance = sdid.sourceworkiteminstance");
            sql.append(" AND sdiwii.itemsdcid = 'ParamList' AND sdiwii.itemkeyid1 = sdid.paramlistid");
            sql.append(" AND sdiwii.itemkeyid2 = sdid.paramlistversionid AND sdiwii.itemkeyid3 = sdid.variantid  AND sdiwii.iteminstance = sdid.dataset");
            sql.append(" AND wir.WORKITEMID = sdiw.WORKITEMID");
            sql.append(" AND wir.WORKITEMVERSIONID = sdiw.WORKITEMVERSIONID");
            sql.append(" AND (wir.WORKITEMITEMID is null OR ");
            if (this.database.isOracle()) {
                sql.append("  wir.workitemitemid = substr( sdiwii.workitemitemid, 1, instr( sdiwii.workitemitemid, '.', 1 ) - 1 ) ");
            } else {
                sql.append(" wir.workitemitemid = substring( sdiwii.workitemitemid, 1, charindex( '.', sdiwii.workitemitemid, 1 ) - 1 ) ");
            }
            sql.append(")");
            sql.append(" UNION ");
            sql.append(" select  DISTINCT plrt.reagenttypeid, plrt.reagenttypeversionid,'S' sourceflag,plrt.amount,plrt.amountunits,plrt.amountunitstype");
            if (sdidata != null) {
                sql.append(" ,ds.keyid1, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset ");
            }
            sql.append(" from sdidata ds, paramlistreagenttype plrt");
            sql.append(" where ds.sdcid='Sample' and ds.s_qcbatchid = ").append(safeSQL.addVar(qcbatchid)).append(" AND  ds.availabilityflag = 'Y'");
            sql.append(" and ds.paramlistid = plrt.paramlistid");
            sql.append(" and ds.paramlistversionid = plrt.paramlistversionid");
            sql.append(" and ds.variantid = plrt.variantid");
            sql.append(" ) ORDER by sourceflag desc");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            DataSet dsAllRgnt = new DataSet();
            int maxSequence = 0;
            if (sdidata != null) {
                DataSet dsRgnt = this.getQueryProcessor().getPreparedSqlDataSet("select * from  s_qcbatchreagent where qcbatchid = ? order by usersequence desc", (Object[])new String[]{qcbatchid});
                if (dsRgnt.getRowCount() > 0) {
                    maxSequence = dsRgnt.getInt(0, "usersequence");
                }
                for (int d = 0; d < sdidata.getRowCount(); ++d) {
                    HashMap<String, Object> findMap = new HashMap<String, Object>();
                    findMap.put(PROPERTY_KEYID1, sdidata.getValue(d, PROPERTY_KEYID1));
                    findMap.put(PROPERTY_PARAMLISTID, sdidata.getValue(d, PROPERTY_PARAMLISTID));
                    findMap.put(PROPERTY_PARAMLISTVERSIONID, sdidata.getValue(d, PROPERTY_PARAMLISTVERSIONID));
                    findMap.put(PROPERTY_VARIANTID, sdidata.getValue(d, PROPERTY_VARIANTID));
                    findMap.put(PROPERTY_DATASET, sdidata.getBigDecimal(d, PROPERTY_DATASET));
                    DataSet dsMatch = ds.getFilteredDataSet(findMap);
                    for (int k = 0; k < dsMatch.getRowCount(); ++k) {
                        HashMap<String, Object> findRgnt = new HashMap<String, Object>();
                        findRgnt.put("originalreagenttypeid", dsMatch.getString(k, "reagenttypeid"));
                        findRgnt.put("originalreagenttypeversionid", dsMatch.getString(k, "reagenttypeversionid"));
                        findRgnt.put("sourceflag", dsMatch.getString(k, "sourceflag"));
                        findRgnt.put("amountrecommended", dsMatch.getBigDecimal(k, "amount"));
                        findRgnt.put("amountrecommendedunits", dsMatch.getString(k, "amountunits"));
                        findRgnt.put("amountrecommendedunitstype", dsMatch.getString(k, "amountunitstype"));
                        if (dsRgnt.findRow(findRgnt) >= 0) continue;
                        findRgnt.remove("originalreagenttypeid");
                        findRgnt.remove("originalreagenttypeversionid");
                        findRgnt.remove("amountrecommended");
                        findRgnt.remove("amountrecommendedunits");
                        findRgnt.remove("amountrecommendedunitstype");
                        findRgnt.put("reagenttypeid", dsMatch.getString(k, "reagenttypeid"));
                        findRgnt.put("reagenttypeversionid", dsMatch.getString(k, "reagenttypeversionid"));
                        findRgnt.put("amount", dsMatch.getBigDecimal(k, "amount"));
                        findRgnt.put("amountunits", dsMatch.getString(k, "amountunits"));
                        findRgnt.put("amountunitstype", dsMatch.getString(k, "amountunitstype"));
                        if (dsAllRgnt.findRow(findRgnt) >= 0) continue;
                        dsAllRgnt.copyRow(dsMatch, k, 1);
                    }
                }
            } else if (ds != null && ds.getRowCount() > 0) {
                dsAllRgnt.copyRow(ds, -1, 1);
            }
            DataSet dsProps = new DataSet(this.connectionInfo);
            dsProps.copyRow(dsAllRgnt, -1, 1);
            if (dsProps.getRowCount() > 0) {
                dsProps.sort("sourceflag D, usersequence");
                StringBuffer userSequence = new StringBuffer();
                for (int p = 0; p < dsProps.size(); ++p) {
                    userSequence.append(";").append(maxSequence + (p + 1));
                }
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_QCBatchReagent");
                props.setProperty(PROPERTY_QCBATCHID, qcbatchid);
                props.setProperty("reagenttypeid", dsProps.getColumnValues("reagenttypeid", ";"));
                props.setProperty("reagenttypeversionid", dsProps.getColumnValues("reagenttypeversionid", ";"));
                props.setProperty("originalreagenttypeid", dsProps.getColumnValues("reagenttypeid", ";"));
                props.setProperty("originalreagenttypeversionid", dsProps.getColumnValues("reagenttypeversionid", ";"));
                props.setProperty("amountadjusted", dsProps.getColumnValues("amount", ";"));
                props.setProperty("amountrecommended", dsProps.getColumnValues("amount", ";"));
                props.setProperty("amountrecommendedunits", dsProps.getColumnValues("amountunits", ";"));
                props.setProperty("amountrecommendedunitstype", dsProps.getColumnValues("amountunitstype", ";"));
                props.setProperty("sourceflag", dsProps.getColumnValues("sourceflag", ";"));
                props.setProperty("usersequence", userSequence.substring(1));
                props.setProperty("copies", "" + dsProps.size());
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            }
        }
        catch (SapphireException e) {
            this.logger.error("WARNING: FAILED TO SYNC AQCREAGENTUSE (" + e.getMessage() + ")", e);
        }
    }

    private void processRuleForInstrument(String qcbatchid, DataSet sdidata) {
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("(");
            sql.append("SELECT DISTINCT sdid.s_qcbatchid,wins.INSTRUMENTTYPEID,wins.INSTRUMENTMODELID,wins.INSTRUMENTID,wins.instrumentcount,");
            sql.append(" CASE WHEN wins.WORKITEMITEMID IS NOT NULL THEN 'S' ELSE 'W' END sourceflag");
            if (sdidata != null) {
                sql.append(" ,sdid.keyid1, sdid.paramlistid, sdid.paramlistversionid, sdid.variantid, sdid.dataset ");
            }
            sql.append(" FROM sdidata sdid, SDIWORKITEM sdiw, WORKITEMINSTRUMENT wins, sdiworkitemitem sdiwii");
            sql.append(" WHERE sdid.sdcid='Sample' and sdid.s_qcbatchid  = ").append(safeSQL.addVar(qcbatchid));
            sql.append(" AND sdid.availabilityflag = 'Y'");
            sql.append(" AND sdiw.sdcid = sdid.sdcid");
            sql.append(" AND sdiw.keyid1 = sdid.keyid1");
            sql.append(" AND sdiw.keyid2 = sdid.keyid2");
            sql.append(" AND sdiw.keyid3 = sdid.keyid3");
            sql.append(" AND sdiw.WORKITEMID = sdid.SOURCEWORKITEMID");
            sql.append(" AND sdiw.WORKITEMINSTANCE = sdid.SOURCEWORKITEMINSTANCE");
            sql.append(" AND sdiwii.sdcid = sdid.SDCID ");
            sql.append(" AND sdiwii.keyid1 = sdid.keyid1 ");
            sql.append(" AND sdiwii.keyid2 = sdid.keyid2 ");
            sql.append(" AND sdiwii.keyid3 = sdid.keyid3 ");
            sql.append(" AND sdiwii.workitemid  = sdid.sourceworkitemid AND sdiwii.workiteminstance = sdid.sourceworkiteminstance");
            sql.append(" AND sdiwii.itemsdcid = 'ParamList' AND sdiwii.itemkeyid1 = sdid.paramlistid");
            sql.append(" AND sdiwii.itemkeyid2 = sdid.paramlistversionid AND sdiwii.itemkeyid3 = sdid.variantid  AND sdiwii.iteminstance = sdid.dataset");
            sql.append(" AND wins.WORKITEMID = sdiw.WORKITEMID");
            sql.append(" AND wins.WORKITEMVERSIONID = sdiw.WORKITEMVERSIONID");
            sql.append(" AND ( wins.WORKITEMITEMID is null OR ");
            if (this.database.isOracle()) {
                sql.append("  wins.workitemitemid = substr( sdiwii.workitemitemid, 1, instr( sdiwii.workitemitemid, '.', 1 ) - 1 ) ");
            } else {
                sql.append(" wins.workitemitemid = substring( sdiwii.workitemitemid, 1, charindex( '.', sdiwii.workitemitemid, 1 ) - 1 ) ");
            }
            sql.append(")");
            sql.append(" UNION ALL");
            sql.append(" SELECT DISTINCT  sd.s_qcbatchid, pl.s_instrumenttype INSTRUMENTTYPEID,pl.s_instrumentmodel INSTRUMENTMODELID,sd.s_instrumentid,1 instrumentcount,'D' sourceflag ");
            if (sdidata != null) {
                sql.append(" ,sd.keyid1, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset ");
            }
            sql.append("  FROM sdidata sd, paramlist pl");
            sql.append(" where sd.sdcid='Sample' and sd.s_qcbatchid=").append(safeSQL.addVar(qcbatchid)).append(" AND sd.availabilityflag = 'Y'");
            sql.append(" and sd.paramlistid = pl.paramlistid");
            sql.append(" and sd.paramlistversionid = pl.paramlistversionid");
            sql.append(" and sd.variantid = pl.variantid");
            sql.append(" and pl.s_instrumenttype is not null");
            sql.append(" )");
            sql.append(" ORDER by sourceflag");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            DataSet dsAllInst = new DataSet();
            int maxSequence = 0;
            if (sdidata != null) {
                DataSet dsInst = this.getQueryProcessor().getPreparedSqlDataSet("select * from  s_qcbatchinstrument where qcbatchid = ? order by usersequence desc", (Object[])new String[]{qcbatchid});
                if (dsInst.getRowCount() > 0) {
                    maxSequence = dsInst.getInt(0, "usersequence");
                }
                for (int d = 0; d < sdidata.getRowCount(); ++d) {
                    HashMap<String, Object> findMap = new HashMap<String, Object>();
                    findMap.put(PROPERTY_KEYID1, sdidata.getValue(d, PROPERTY_KEYID1));
                    findMap.put(PROPERTY_PARAMLISTID, sdidata.getValue(d, PROPERTY_PARAMLISTID));
                    findMap.put(PROPERTY_PARAMLISTVERSIONID, sdidata.getValue(d, PROPERTY_PARAMLISTVERSIONID));
                    findMap.put(PROPERTY_VARIANTID, sdidata.getValue(d, PROPERTY_VARIANTID));
                    findMap.put(PROPERTY_DATASET, sdidata.getBigDecimal(d, PROPERTY_DATASET));
                    DataSet dsMatch = ds.getFilteredDataSet(findMap);
                    for (int k = 0; k < dsMatch.getRowCount(); ++k) {
                        HashMap<String, String> findInstr = new HashMap<String, String>();
                        findInstr.put("instrumenttypeid", dsMatch.getValue(k, "instrumenttypeid"));
                        findInstr.put("instrumentmodelid", dsMatch.getString(k, "instrumentmodelid"));
                        findInstr.put("instrumentid", dsMatch.getString(k, "instrumentid"));
                        findInstr.put("sourceflag", dsMatch.getString(k, "sourceflag"));
                        if (dsInst.findRow(findInstr) >= 0 || dsAllInst.findRow(findInstr) >= 0) continue;
                        dsAllInst.copyRow(dsMatch, k, 1);
                    }
                }
            } else {
                dsAllInst.copyRow(ds, -1, 1);
            }
            DataSet dsProps = new DataSet();
            dsProps.copyRow(dsAllInst, -1, 1);
            if (dsProps != null && dsProps.size() > 0) {
                StringBuffer userSequences = new StringBuffer();
                StringBuffer instrumentTypeids = new StringBuffer();
                StringBuffer instrumentmodelids = new StringBuffer();
                StringBuffer instrumentids = new StringBuffer();
                StringBuffer sourceflags = new StringBuffer();
                StringBuffer instrumentcounts = new StringBuffer();
                StringBuffer instrumentinstances = new StringBuffer();
                int userSecNo = 0;
                for (int i = 0; i < dsProps.size(); ++i) {
                    int instruemntcount = dsProps.getInt(i, "instrumentcount", 1);
                    String instrumenttypeid = dsProps.getValue(i, "instrumenttypeid", "");
                    String instrumentmodelid = dsProps.getValue(i, "instrumentmodelid", "");
                    boolean isUnmanaged = this.isUnmanaged(this.getQueryProcessor(), instrumenttypeid, instrumentmodelid);
                    int count = instruemntcount > 1 && !isUnmanaged ? instruemntcount : 1;
                    instruemntcount = isUnmanaged ? instruemntcount : 1;
                    for (int c = 0; c < count; ++c) {
                        instrumentTypeids.append(";").append(dsProps.getValue(i, "instrumenttypeid", ""));
                        instrumentmodelids.append(";").append(dsProps.getValue(i, "instrumentmodelid", ""));
                        instrumentids.append(";").append(dsProps.getValue(i, "instrumentid", ""));
                        sourceflags.append(";").append(dsProps.getValue(i, "sourceflag", ""));
                        instrumentcounts.append(";").append("" + instruemntcount);
                        instrumentinstances.append(";").append("" + (c + 1));
                        userSequences.append(";").append(maxSequence + ++userSecNo);
                    }
                }
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_QCBatchInstrument");
                props.setProperty(PROPERTY_QCBATCHID, qcbatchid);
                props.setProperty("instrumenttypeid", instrumentTypeids.substring(1));
                props.setProperty("instrumentmodelid", instrumentmodelids.substring(1));
                props.setProperty("instrumentid", instrumentids.substring(1));
                props.setProperty("sourceflag", sourceflags.substring(1));
                props.setProperty("usersequence", userSequences.substring(1));
                props.setProperty("requiredinstrumentcount", instrumentcounts.substring(1));
                props.setProperty("instrumentinstance", instrumentinstances.substring(1));
                props.setProperty("copies", "" + userSecNo);
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            }
        }
        catch (SapphireException e) {
            this.logger.error("WARNING: FAILED TO SYNC AQCINSTRUMENTUSE (" + e.getMessage() + ")", e);
        }
    }

    private boolean isUnmanaged(QueryProcessor qp, String instrumenttypeid, String instrumentmodelid) {
        boolean unmanaged = false;
        if (instrumentmodelid.length() > 0) {
            DataSet instrumentModelDS = qp.getPreparedSqlDataSet("select unmanagedflag from instrumentmodel where instrumentmodelid = ?", (Object[])new String[]{instrumentmodelid});
            if (instrumentModelDS != null && instrumentModelDS.size() > 0) {
                unmanaged = instrumentModelDS.getString(0, "unmanagedflag", "").equalsIgnoreCase("Y");
            }
        } else {
            DataSet instrumentTypeDS = qp.getPreparedSqlDataSet("select unmanagedflag from instrumenttype where instrumenttypeid = ?", (Object[])new String[]{instrumenttypeid});
            if (instrumentTypeDS != null && instrumentTypeDS.size() > 0) {
                unmanaged = instrumentTypeDS.getString(0, "unmanagedflag", "").equalsIgnoreCase("Y");
            }
        }
        return unmanaged;
    }
}

