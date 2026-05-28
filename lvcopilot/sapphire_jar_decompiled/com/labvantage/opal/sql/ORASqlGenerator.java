/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.sql;

import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.sql.DBColumn;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.opal.sql.common.Certification;
import com.labvantage.opal.sql.common.Chart;
import com.labvantage.opal.sql.common.OpalUtil;
import com.labvantage.opal.sql.common.SdiDetailView;
import com.labvantage.opal.sql.common.TableInfo;
import com.labvantage.opal.sql.common.Workitem;
import com.labvantage.opal.sql.ora.Audit;
import com.labvantage.opal.sql.ora.Coc;
import com.labvantage.opal.sql.ora.DataDetailView;
import com.labvantage.opal.sql.ora.Query;
import com.labvantage.opal.sql.ora.StorageUnit;
import com.labvantage.sapphire.SDI;
import java.util.HashMap;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ORASqlGenerator
implements SQLGenerator {
    final String LABVANTAGE_CVS_ID = "$Revision: 90286 $";
    protected PageContext __PageContext;
    protected String __ConnectionId;
    private String __DBMS;

    public ORASqlGenerator(String connectionId, String dbms) {
        this.__ConnectionId = connectionId;
        this.__DBMS = dbms;
    }

    public ORASqlGenerator(PageContext pageContext, String dbms) {
        this.__PageContext = pageContext;
        this.__DBMS = dbms;
    }

    public ORASqlGenerator() {
        this.__DBMS = "ORA";
    }

    @Override
    public String getDBMS() {
        return this.__DBMS;
    }

    @Override
    public SafeSQL getWorkitemDetails(String workitemid, String workitemversionid, String sdcid) {
        return Workitem.getWorkitemDetails(workitemid, workitemversionid, sdcid);
    }

    @Override
    public SafeSQL getSdiWorkitemDetails(SDI sdi, String workitemid, String workiteminstance, String itemSdcid) {
        return Workitem.getSdiWorkitemDetails(sdi, workitemid, workiteminstance, itemSdcid);
    }

    @Override
    public SafeSQL getReleasedCount(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT COUNT(*) RELEASEDCOUNT FROM SDIDATAITEM WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND RELEASEDFLAG = 'Y'");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getDatasetKey(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT PARAMLISTID || PARAMLISTVERSIONID || VARIANTID || DATASET DATASET");
        sql.append(" FROM SDIDATA WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getNotReleasedCount(SDI sdi, String dataset) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT COUNT(*) NORELEASEDCOUNT FROM SDIDATAITEM");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND RELEASEDFLAG <> 'Y'");
        sql.append(" AND PARAMLISTID || PARAMLISTVERSIONID || VARIANTID || DATASET = ").append(safeSQL.addVar(dataset));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getSdiDatasets(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET, NVL( S_DATASETSTATUS, '' ) S_DATASETSTATUS");
        sql.append(" FROM SDIDATA ");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getMaxSdiWorkitemInstance(SDI sdi, String workitemid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT MAX( WORKITEMINSTANCE ) WORKITEMINSTANCE");
        sql.append(" FROM SDIWORKITEM");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND WORKITEMID = ").append(safeSQL.addVar(workitemid));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getColumnValue(SDI sdi, String column, String tableid, String extraWhere, String orderby) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT ").append(column).append(" FROM ").append(tableid);
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        if (extraWhere != null && extraWhere.length() > 0) {
            sql.append(" AND ").append(extraWhere);
        }
        if (orderby != null && orderby.length() > 0) {
            sql.append(" ORDER BY ").append(orderby);
        }
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getParamlistWorkitems(SDI sdi, String paramlistid, String paramlistversionid, String variantid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT swii.WORKITEMID, swii.WORKITEMINSTANCE, swii.ITEMINSTANCE, swi.WORKITEMVERSIONID");
        sql.append(" FROM SDIWORKITEMITEM swii, SDIWORKITEM swi");
        sql.append(" WHERE swii.SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND swii.KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND swii.KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND swii.KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND swii.ITEMSDCID = 'ParamList'");
        sql.append(" AND swii.ITEMKEYID1 = ").append(safeSQL.addVar(paramlistid));
        sql.append(" AND swii.ITEMKEYID2 = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" AND swii.ITEMKEYID3 = ").append(safeSQL.addVar(variantid));
        sql.append(" AND swi.SDCID = swii.SDCID ");
        sql.append(" AND swi.KEYID1 = swii.KEYID1 ");
        sql.append(" AND swi.KEYID2 = swii.KEYID2 ");
        sql.append(" AND swi.KEYID3 = swii.KEYID3 ");
        sql.append(" AND swi.WORKITEMID = swii.WORKITEMID ");
        sql.append(" AND swi.WORKITEMINSTANCE = swii.WORKITEMINSTANCE ");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getRemeasureInstance(SDI sdi, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT NVL( T1.S_REMEASUREINSTANCE, '0' ) S_REMEASUREINSTANCE");
        sql.append(" FROM SDIDATA T1");
        sql.append(" WHERE T1.SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND T1.KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND T1.KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND T1.KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND T1.PARAMLISTID = ").append(safeSQL.addVar(paramlistid));
        sql.append(" AND T1.PARAMLISTVERSIONID = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" AND T1.VARIANTID = ").append(safeSQL.addVar(variantid));
        sql.append(" AND T1.DATASET = ").append(safeSQL.addVar(dataset));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getMaxSdiDataset(SDI sdi, String paramlistid, String paramlistversionid, String variantid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT MAX( DATASET ) DATASET FROM SDIDATA");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND PARAMLISTID = ").append(safeSQL.addVar(paramlistid));
        sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" AND VARIANTID = ").append(safeSQL.addVar(variantid));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public SafeSQL getSdiDataitemForTypeAndDs(String dataset, String paramType) {
        return com.labvantage.opal.sql.ora.Chart.getSdiDataitemForTypeAndDs(dataset, paramType);
    }

    @Override
    public SafeSQL getDataItems(String sampleid, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        return Chart.getDataItems(sampleid, paramlistid, paramlistversionid, variantid, dataset);
    }

    @Override
    public String getQueryArgsForTrendChartQuery() {
        return Chart.getQueryArgsForTrendChartQuery();
    }

    @Override
    public SafeSQL getQueryArgsForQuery(String queryid) {
        return Chart.getQueryArgsForQuery(queryid);
    }

    @Override
    public SafeSQL getSampleDetails(String sampleIdList) {
        return Certification.getSampleDetails(sampleIdList);
    }

    @Override
    public SafeSQL getParamlistDetails(String paramlistIdList) {
        return Certification.getParamlistDetails(paramlistIdList);
    }

    @Override
    public SafeSQL getUserCertificationDetails(String analystIdList, String paramlistIdList) {
        return com.labvantage.opal.sql.ora.Certification.getUserCertificationDetails(analystIdList, paramlistIdList);
    }

    @Override
    public SafeSQL getInstrumentDetails(String instrumentIdList) {
        return Certification.getInstrumentDetails(instrumentIdList);
    }

    @Override
    public SafeSQL getInstrumentCertificationDetails(String instrumentIdList) {
        return com.labvantage.opal.sql.ora.Certification.getInstrumentCertificationDetails(instrumentIdList);
    }

    @Override
    public String getCertifiedUsers() {
        return Coc.getCertifiedUsers();
    }

    @Override
    public String getUserCustodianInfo() {
        return Coc.getUserCustodianInfo();
    }

    @Override
    public SafeSQL getCustodianAndUserInfo(String userid) {
        return Coc.getCustodianAndUserInfo(userid);
    }

    @Override
    public SafeSQL getQueryDetails(String queryid) {
        return Query.getQueryDetails(queryid);
    }

    @Override
    public SafeSQL getQueryAndArgDetails(String queryid, String basedOnSdcId) {
        return Query.getQueryAndArgDetails(queryid, basedOnSdcId);
    }

    @Override
    public SafeSQL getQueryAndArgDetails2(String queryid, String basedOnSdcId) {
        return Query.getQueryAndArgDetails2(queryid, basedOnSdcId);
    }

    @Override
    public String getSelectSQL(List columns, String fromclause, List where, String orderclause, boolean distinct) {
        int i;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        if (distinct) {
            sql.append(" DISTINCT ");
        }
        for (i = 0; i < columns.size(); ++i) {
            sql.append(((DBColumn)columns.get(i)).getNvlImpl());
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" FROM ").append(fromclause);
        sql.append(" WHERE ");
        if (where.size() > 0) {
            sql.append(where.get(0));
            for (i = 1; i < where.size(); ++i) {
                sql.append(" AND ").append(where.get(i));
            }
        }
        if (orderclause != null) {
            sql.append(" ORDER BY ").append(orderclause);
        }
        return sql.toString();
    }

    @Override
    public SafeSQL getTableSDCSQL(String tableid) {
        return TableInfo.getTableSDCSQL(tableid);
    }

    @Override
    public SafeSQL getTableKeysSQL(String tableid) {
        return TableInfo.getTableKeysSQL(tableid);
    }

    @Override
    public String getAuditSQL(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor queryProcessor, Logger logger, HashMap extraFilter) throws Exception {
        return Audit.getSQLStmt(elementId, elementsContainer, pageData, queryProcessor, logger, extraFilter);
    }

    @Override
    public String getDynamicAuditSQL(String elementId, AuditElementsContainer elementsContainer, PropertyList pageData, QueryProcessor queryProcessor, Logger logger, HashMap extraFilter) throws Exception {
        return Audit.getDynamicAuditSQLStmt(elementId, elementsContainer, pageData, queryProcessor, logger, extraFilter);
    }

    @Override
    public String getDataitemSpecsAndLimits(String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid, String specid, String specversionid, String keyidwhere, String paramlistidwhere, String paramlistversionidwhere, String variantidwhere, String datasetwhere, String paramidwhere, String paramtypewhere, String replicateidwhere, String specidwhere, String specversionidwhere) {
        return DataDetailView.getDataitemSpecsAndLimits(paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, keyidwhere, paramlistidwhere, paramlistversionidwhere, variantidwhere, datasetwhere, paramidwhere, paramtypewhere, replicateidwhere, specidwhere, specversionidwhere);
    }

    @Override
    public String getPrimarySql(String primarySelectList, String sampleIdWhere, String _tableid, String _sdcId, String _keyColId1) {
        return SdiDetailView.getPrimarySql(primarySelectList, sampleIdWhere, _tableid, _sdcId, _keyColId1);
    }

    @Override
    public SafeSQL getSpecSql(String _keyid1, String _paramlistid, String _paramid) {
        return SdiDetailView.getSpecSql(_keyid1, _paramlistid, _paramid);
    }

    @Override
    public SafeSQL getDatasetSql(String _keyid1, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _specid) {
        return SdiDetailView.getDatasetSql(_keyid1, _paramlistid, _paramlistversionid, _variantid, _dataset, _specid);
    }

    @Override
    public SafeSQL getApprovalSql(String _sdcId, String _keyid1, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset) {
        return SdiDetailView.getApprovalSql(_sdcId, _keyid1, _paramlistid, _paramlistversionid, _variantid, _dataset);
    }

    @Override
    public SafeSQL getDataitemSql(String _sdcId, String _keyid1, String _specid, String _paramid, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _paramtype, String _replicateid) {
        return SdiDetailView.getDataitemSql(_sdcId, _keyid1, _specid, _paramid, _paramlistid, _paramlistversionid, _variantid, _dataset, _paramtype, _replicateid);
    }

    @Override
    public SafeSQL getSpecParamLimitsSql(String _keyid1) {
        return SdiDetailView.getSpecParamLimitsSql(_keyid1);
    }

    @Override
    public String getDataitemLimitSql(String dataitemlimitSelectList, String _sdcId, String keyidWhere, String _paramid, String _paramlistid, String _paramlistversionid, String _paramtype, String _variantid, String _dataset, String _replicateid) {
        return SdiDetailView.getDataitemLimitSql(dataitemlimitSelectList, _sdcId, keyidWhere, _paramid, _paramlistid, _paramlistversionid, _paramtype, _variantid, _dataset, _replicateid);
    }

    @Override
    public SafeSQL getDataItemSpecSql(String _sdcId, String _keyid1, String _keyid2, String _keyid3, String _paramid, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _paramtype, String _replicateid, String _specid, String _specversionid) {
        return SdiDetailView.getDataItemSpecSql(_sdcId, _keyid1, _keyid2, _keyid3, _paramid, _paramlistid, _paramlistversionid, _variantid, _dataset, _paramtype, _replicateid, _specid, _specversionid, true);
    }

    @Override
    public SafeSQL getKeysFromRSetItemsSQLStmt(String rsetid) {
        return OpalUtil.getKeysFromRSetItemsSQLStmt(rsetid);
    }

    @Override
    public SafeSQL getQCBatchParamSets(SDI sdi) {
        return Aqc.getQCBatchParamSets(sdi);
    }

    @Override
    public String getQCBatchItems(SDI sdi, String qcbatchitemids) {
        return Aqc.getQCBatchItems(sdi, qcbatchitemids);
    }

    @Override
    public SafeSQL getSdiWorkitemDataSets(SDI sdi, String workitemid, String workiteminstance) {
        return Workitem.getSdiWorkitemDataSets(sdi, workitemid, workiteminstance);
    }

    @Override
    public SafeSQL getQCDataPointsForEvaluation(String batchSampleTypeId, String batchQueryCount, String paramtype, String qcbatchids, String currentBatchId, boolean chart, String sdcId, String tableId, String keyColId) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" select ds.s_qcbatchid, qcbi.s_qcbatchitemid, ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ");
        sql.append(" ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, di.paramid, di.paramtype, ");
        sql.append(" di.replicateid, di.transformvalue, ");
        sql.append(" di.displayvalue, di.enteredvalue, di.S_QCEVALSTATUS, sss.createdt, qcbi.usersequence");
        sql.append(" from ( select ss." + keyColId + ", ss.createdt ");
        sql.append(" from ( select distinct s." + keyColId + ", s.createdt, bi.s_qcbatchid ");
        sql.append(" from " + tableId + " s, s_qcbatchitem bi, sdidata bids, s_qcbatchsampletype qcbst where s." + keyColId + " = bids.keyid1 ");
        sql.append(" and bids.sdcid = '" + sdcId + "' and bids.s_qcbatchitemid = bi.s_qcbatchitemid and bids.s_qcbatchid = bi.s_qcbatchid ");
        sql.append(" and ( bids.s_qcbatchid IN (").append(safeSQL.addIn(qcbatchids)).append(" ) OR bids.s_qcbatchid = ").append(safeSQL.addVar(currentBatchId)).append(" ) ");
        sql.append("  and bi.qcbatchsampletypeid = qcbst.s_qcbatchsampletypeid and qcbst.qcmethodsampletypeid  = ");
        sql.append(" ( select qcmethodsampletypeid from s_qcbatchsampletype where s_qcbatchsampletypeid = ").append(safeSQL.addVar(batchSampleTypeId)).append(" ) ");
        sql.append(" order by s.createdt desc ) ss ");
        sql.append(" where rownum <= ").append(safeSQL.addVar(batchQueryCount)).append(" ) sss,");
        sql.append(" sdidata ds, sdidataitem di, s_qcbatchitem qcbi, paramlist pl where ds.sdcid = di.sdcid and ds.sdcid = " + safeSQL.addVar(sdcId) + " ");
        sql.append(" and ds.keyid1 = sss." + keyColId + " and ds.keyid1 = di.keyid1 and ds.keyid2 = di.keyid2 and ");
        sql.append(" ds.keyid3 = di.keyid3 and ds.paramlistid = di.paramlistid and ds.paramlistversionid = di.paramlistversionid");
        sql.append(" and ds.variantid = di.variantid and ds.dataset = di.dataset and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid ");
        sql.append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and di.paramtype = ").append(safeSQL.addVar(paramtype)).append(" and pl.paramlistid = ds.paramlistid ");
        sql.append(" and pl.paramlistversionid = ds.paramlistversionid and pl.variantid = ds.variantid and ");
        sql.append(" nvl(s_paramlisttype,'XXX') <> 'Preparation' and di.calcexcludeflag != 'Y' and ds.s_datasetstatus != 'Cancelled'");
        if (chart) {
            sql.append(" order by ds.keyid1, ds.usersequence, ");
            sql.append(" di.usersequence, qcbi.usersequence, di.replicateid ");
        } else {
            sql.append(" order by di.paramid, sss.createdt,");
            sql.append(" qcbi.usersequence, ds.usersequence, di.replicateid ");
        }
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    @Override
    public String getQCDataPointsForEvaluation(String batchSampleTypeId, String batchQueryCount, String paramtype, String qcbatchids, String currentBatchId, boolean chart, String sdcId, String tableId, String keyColId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select ds.s_qcbatchid, qcbi.s_qcbatchitemid, ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ");
        sql.append(" ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, di.paramid, di.paramtype, ");
        sql.append(" di.replicateid, di.transformvalue, ");
        sql.append(" di.displayvalue, di.enteredvalue, di.S_QCEVALSTATUS, sss.createdt, qcbi.usersequence");
        sql.append(" from ( select ss." + keyColId + ", ss.createdt ");
        sql.append(" from ( select distinct s." + keyColId + ", s.createdt, bi.s_qcbatchid ");
        sql.append(" from " + tableId + " s, s_qcbatchitem bi, sdidata bids, s_qcbatchsampletype qcbst where s." + keyColId + " = bids.keyid1 ");
        sql.append(" and bids.sdcid = " + safeSQL.addVar(sdcId) + " and bids.s_qcbatchitemid = bi.s_qcbatchitemid and bids.s_qcbatchid = bi.s_qcbatchid ");
        sql.append(" and ( bids.s_qcbatchid IN (").append(safeSQL.addIn(qcbatchids)).append(" ) OR bids.s_qcbatchid = ").append(safeSQL.addVar(currentBatchId)).append(" ) ");
        sql.append("  and bi.qcbatchsampletypeid = qcbst.s_qcbatchsampletypeid and qcbst.qcmethodsampletypeid  = ");
        sql.append(" ( select qcmethodsampletypeid from s_qcbatchsampletype where s_qcbatchsampletypeid = ").append(safeSQL.addVar(batchSampleTypeId)).append(" ) ");
        sql.append(" order by s.createdt desc ) ss ");
        sql.append(" where rownum <= ").append(safeSQL.addVar(batchQueryCount)).append(" ) sss,");
        sql.append(" sdidata ds, sdidataitem di, s_qcbatchitem qcbi, paramlist pl where ds.sdcid = di.sdcid and ds.sdcid = " + safeSQL.addVar(sdcId));
        sql.append(" and ds.keyid1 = sss." + keyColId + " and ds.keyid1 = di.keyid1 and ds.keyid2 = di.keyid2 and ");
        sql.append(" ds.keyid3 = di.keyid3 and ds.paramlistid = di.paramlistid and ds.paramlistversionid = di.paramlistversionid");
        sql.append(" and ds.variantid = di.variantid and ds.dataset = di.dataset and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid ");
        sql.append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and di.paramtype = ").append(safeSQL.addVar(paramtype)).append(" and pl.paramlistid = ds.paramlistid ");
        sql.append(" and pl.paramlistversionid = ds.paramlistversionid and pl.variantid = ds.variantid and ");
        sql.append(" nvl(s_paramlisttype,'XXX') <> 'Preparation' and di.calcexcludeflag != 'Y' and ds.s_datasetstatus != 'Cancelled'");
        if (chart) {
            sql.append(" order by ds.keyid1, ds.usersequence, ");
            sql.append(" di.usersequence, qcbi.usersequence, di.replicateid ");
        } else {
            sql.append(" order by di.paramid, sss.createdt,");
            sql.append(" qcbi.usersequence, ds.usersequence, di.replicateid ");
        }
        return sql.toString();
    }

    @Override
    public String getQCDataSets(String qcBatchId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.SDCID, T1.KEYID1, T1.KEYID2, T1.KEYID3, T1.PARAMLISTID, T1.PARAMLISTVERSIONID, T1.VARIANTID, T1.DATASET");
        sql.append(" FROM SDIDATA T1, S_QCBATCHITEM T2");
        sql.append(" WHERE T1.S_QCBATCHID = T2.S_QCBATCHID");
        sql.append(" AND T1.S_QCBATCHITEMID = T2.S_QCBATCHITEMID");
        sql.append(" AND T2.S_QCBATCHID = ").append(safeSQL.addVar(qcBatchId));
        sql.append(" AND T2.QCBATCHSAMPLETYPEID IS NOT NULL");
        sql.append(" AND LENGTH( T2.QCBATCHSAMPLETYPEID ) > 0");
        sql.append(" ORDER BY T1.KEYID1");
        return sql.toString();
    }

    @Override
    public SafeSQL getStorageUnitHierarchySql(String storageUnit) {
        return StorageUnit.getStorageUnitHierarchySql(storageUnit);
    }

    @Override
    public SafeSQL getStorageEnvCondTypesSql(String keyid1) {
        return StorageUnit.getStorageEnvCondTypesSql(keyid1);
    }

    @Override
    public String getRefTypeSql() {
        return com.labvantage.opal.sql.common.StorageUnit.getRefTypeSql();
    }
}

