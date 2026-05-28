/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import com.labvantage.sapphire.SDI;
import sapphire.util.SafeSQL;

public class Aqc {
    public static String getQCBatchSampleTypeIds(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHSAMPLETYPEID ");
        sql.append(" FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE QCBATCHID = '" + sdi.getKeyid1() + "'");
        sql.append(" ORDER BY S_QCBATCHSAMPLETYPEID ");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeIds(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHSAMPLETYPEID ");
        sql.append(" FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE QCBATCHID = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" ORDER BY S_QCBATCHSAMPLETYPEID ");
        return sql.toString();
    }

    public static String getQCBatchEvalRules(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHEVALRULEID, QCBATCHEVALRULEDESC, ");
        sql.append(" VIOLATIONCOUNT, WINDOWSIZE, SIGMAABOVECL, ");
        sql.append(" SIGMABELOWCL, RULEPATTERNFLAG, INSIDELIMITFLAG ");
        sql.append(" from S_QCBATCHEVALRULE ");
        sql.append(" WHERE S_QCBATCHSAMPLETYPEID = '" + sdi.getKeyid1() + "'");
        sql.append(" ORDER BY S_QCBATCHEVALRULEID ");
        return sql.toString();
    }

    public static SafeSQL getQCBatchParamSets(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" SELECT S_QCBATCHPARAMSETID, PARAMID, TARGETVALUE, ");
        sql.append(" TARGETUNITS, SD from S_QCBATCHPARAMSET ");
        sql.append(" WHERE S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" ORDER BY S_QCBATCHPARAMSETID ");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    public static String getQCBatchParamSets(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHPARAMSETID, PARAMID, TARGETVALUE, ");
        sql.append(" TARGETUNITS, SD from S_QCBATCHPARAMSET ");
        sql.append(" WHERE S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" ORDER BY S_QCBATCHPARAMSETID ");
        return sql.toString();
    }

    public static String getQCBatchEvalRuleParams(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHEVALRULEID, S_QCBATCHPARAMSETID ");
        sql.append(" from S_QCBATCHEVALRULEPARAM ");
        sql.append(" WHERE S_QCBATCHSAMPLETYPEID = '" + sdi.getKeyid1() + "'");
        sql.append(" ORDER BY S_QCBATCHEVALRULEID ");
        return sql.toString();
    }

    public static String getQCBatchEvalRuleParams(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHEVALRULEID, S_QCBATCHPARAMSETID ");
        sql.append(" from S_QCBATCHEVALRULEPARAM ");
        sql.append(" WHERE S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" ORDER BY S_QCBATCHEVALRULEID ");
        return sql.toString();
    }

    public static String getQCBatchItems(SDI sdi, String qcbatchitemids) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHITEMID,QCBATCHITEMDESC, ");
        sql.append(" LINKTOQCBATCHITEMID,USERSEQUENCE,QCBATCHSAMPLETYPEID, ");
        sql.append(" LINKEDTO,BATCHITEMTYPE from S_QCBATCHITEM ");
        sql.append(" WHERE S_QCBATCHID = '" + sdi.getKeyid1() + "'");
        if (qcbatchitemids != null && qcbatchitemids.length() > 0) {
            sql.append(" AND S_QCBATCHITEMID IN (");
            sql.append(qcbatchitemids);
            sql.append(" )");
        }
        sql.append(" ORDER BY USERSEQUENCE ");
        return sql.toString();
    }

    public static String getQCBatchItems(SDI sdi, String qcbatchitemids, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHITEMID,QCBATCHITEMDESC, ");
        sql.append(" LINKTOQCBATCHITEMID,USERSEQUENCE,QCBATCHSAMPLETYPEID, ");
        sql.append(" LINKEDTO,BATCHITEMTYPE from S_QCBATCHITEM ");
        sql.append(" WHERE S_QCBATCHID = " + safeSQL.addVar(sdi.getKeyid1()));
        if (qcbatchitemids != null && qcbatchitemids.length() > 0) {
            sql.append(" AND S_QCBATCHITEMID IN (");
            sql.append(safeSQL.addIn(qcbatchitemids));
            sql.append(" )");
        }
        sql.append(" ORDER BY USERSEQUENCE ");
        return sql.toString();
    }

    public static String getQCBatchSdidata(SDI sdi, String qcbatchitemids) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SDCID,KEYID1,KEYID2,KEYID3,PARAMLISTID, ");
        sql.append(" PARAMLISTVERSIONID,VARIANTID,");
        sql.append(" DATASET, S_QCBATCHITEMID  FROM SDIDATA ");
        sql.append(" WHERE S_QCBATCHID = '" + sdi.getKeyid1() + "'");
        if (qcbatchitemids != null && qcbatchitemids.length() > 0) {
            sql.append(" AND S_QCBATCHITEMID IN (");
            sql.append(qcbatchitemids);
            sql.append(" )");
        }
        sql.append(" ORDER BY KEYID1 ");
        return sql.toString();
    }

    public static String getQCBatchSdidata(SDI sdi, String qcbatchitemids, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SDCID,KEYID1,KEYID2,KEYID3,PARAMLISTID, ");
        sql.append(" PARAMLISTVERSIONID,VARIANTID,");
        sql.append(" DATASET, S_QCBATCHITEMID  FROM SDIDATA ");
        sql.append(" WHERE S_QCBATCHID = " + safeSQL.addVar(sdi.getKeyid1()));
        if (qcbatchitemids != null && qcbatchitemids.length() > 0) {
            sql.append(" AND S_QCBATCHITEMID IN (");
            sql.append(safeSQL.addIn(qcbatchitemids));
            sql.append(" )");
        }
        sql.append(" ORDER BY KEYID1 ");
        return sql.toString();
    }

    public static String getQCBatchCount(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT COUNT(*) COUNT FROM S_QCBATCH ");
        sql.append(" WHERE S_QCBATCHID = '" + sdi.getKeyid1() + "'");
        return sql.toString();
    }

    public static String getQCMethodIDAndQCBatchSDCID(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select qcmethodid, qcmethodversionid, qcbatchsdcid from s_qcbatch ");
        sql.append(" where s_qcbatch.s_qcbatchid = '" + sdi.getKeyid1() + "'");
        return sql.toString();
    }

    public static String getQCMethodIDAndQCBatchSDCID(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select * from s_qcbatch ");
        sql.append(" where s_qcbatch.s_qcbatchid = " + safeSQL.addVar(sdi.getKeyid1()));
        return sql.toString();
    }

    public static String getQCActionApply(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select qcbatchid, s_qcbatchsampletypeid, actionapply, workitemid, qctemplatekeyid1 from s_qcbatchsampletype");
        sql.append(" where s_qcbatchsampletype.qcbatchid = '" + sdi.getKeyid1() + "'");
        sql.append(" order by s_qcbatchsampletypeid ");
        return sql.toString();
    }

    public static String getQCActionApply(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select qcbatchid, s_qcbatchsampletypeid, actionapply, workitemid, qctemplatekeyid1 from s_qcbatchsampletype");
        sql.append(" where s_qcbatchsampletype.qcbatchid = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" order by s_qcbatchsampletypeid ");
        return sql.toString();
    }

    public static String getQCMethodDetails(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select methodsdcid , methodqueryid , methodquerybasedonid ,");
        sql.append(" methodquerycount , evaloption , actionsuccess , actionfailure,");
        sql.append(" maxbatchsize, reviewonpassflag , approveonreviewflag, paramlisttype, batchtype, createworksheetrule,");
        sql.append(" paramlistid, paramlistversionid, variantid, specid, specversionid ");
        sql.append(" from s_qcmethod ");
        sql.append(" where s_qcmethod.s_qcmethodid = '" + sdi.getKeyid1() + "' AND s_qcmethod.s_qcmethodversionid = '" + sdi.getKeyid2() + "'");
        return sql.toString();
    }

    public static String getQCMethodDetails(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select * from s_qcmethod ");
        sql.append(" where s_qcmethod.s_qcmethodid = " + safeSQL.addVar(sdi.getKeyid1()) + " AND s_qcmethod.s_qcmethodversionid = " + safeSQL.addVar(sdi.getKeyid2()));
        return sql.toString();
    }

    public static String getQCMethodSampleTypeDetails(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select s_qcmethodsampletypeid , qcmethoditemsampledesc ,");
        sql.append(" qcevalruleid , qcsampletype , workitemid , workitemversionid, actionapply , actioncalc,");
        sql.append(" actioneval, positiontype, positionstart, positionend, positionevery,");
        sql.append(" positioncount , linkedto , qctemplatesdcid , qctemplatekeyid1 ,");
        sql.append(" qctemplatekeyid2 , qctemplatekeyid3, evaluateparamtype, specid, specversionid, standardlevel, usersequence ");
        sql.append(" from s_qcmethodsampletype where qcmethodid = '" + sdi.getKeyid1() + "' AND qcmethodversionid = '" + sdi.getKeyid2() + "'");
        sql.append(" order by s_qcmethodsampletypeid ");
        return sql.toString();
    }

    public static String getQCMethodSampleTypeDetails(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select s_qcmethodsampletypeid , qcmethoditemsampledesc ,");
        sql.append(" qcevalruleid , qcsampletype , workitemid , workitemversionid, actionapply , actioncalc,");
        sql.append(" actioneval, positiontype, positionstart, positionend, positionevery,");
        sql.append(" positioncount , linkedto , qctemplatesdcid , qctemplatekeyid1 ,");
        sql.append(" qctemplatekeyid2 , qctemplatekeyid3, evaluateparamtype, specid, specversionid, standardlevel, usersequence ");
        sql.append(" from s_qcmethodsampletype where qcmethodid = " + safeSQL.addVar(sdi.getKeyid1()) + " AND qcmethodversionid = " + safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" order by s_qcmethodsampletypeid ");
        return sql.toString();
    }

    public static String getQCEvalRuleItemDetails(SDI sdi, String QCMethodId, String QCMethodVersionId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select a.violationcount, a.windowsize, a.sigmaabovecl, a.sigmabelowcl,");
        sql.append(" a.rulepatternflag , a.insidelimitflag, a.warningflag ");
        sql.append(" from s_qcevalruleitem a, s_qcevalrule b, s_qcmethodsampletype c ");
        sql.append(" where a.s_qcevalruleid = b.s_qcevalruleid ");
        sql.append(" and  b.s_qcevalruleid = c.qcevalruleid ");
        sql.append(" and c.s_qcmethodsampletypeid = '" + sdi.getKeyid1() + "'");
        sql.append(" and c.qcmethodid = '" + QCMethodId + "' AND c.qcmethodversionid='" + QCMethodVersionId + "' and enabledflag = 'Y' ");
        sql.append(" order by a.S_QCEVALRULEITEMID ");
        return sql.toString();
    }

    public static String getQCMethodSampleTypeLimitDetails(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select paramid, targetvalue, targetunits, sd ");
        sql.append(" from s_qcmethodsampletypelimit ");
        sql.append(" where s_qcmethodsampletypeid = '" + sdi.getKeyid1() + "'");
        sql.append(" order by S_QCMETHODSAMPLETYPELIMITID ");
        return sql.toString();
    }

    public static String getDistinctUnknownSampleIds(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select distinct keyid1, b.usersequence, b.qcbatchitemdesc from sdidata a, s_qcbatchitem b ");
        sql.append(" where a.s_qcbatchitemid = b.s_qcbatchitemid ");
        sql.append(" and a.s_qcbatchid = b.s_qcbatchid ");
        sql.append(" and b.s_qcbatchid = '" + sdi.getKeyid1() + "'");
        sql.append(" and b.qcbatchsampletypeid is null ");
        sql.append(" order by b.usersequence");
        return sql.toString();
    }

    public static String getDistinctUnknownSampleIds(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select distinct keyid1, b.usersequence, b.qcbatchitemdesc from sdidata a, s_qcbatchitem b ");
        sql.append(" where a.s_qcbatchitemid = b.s_qcbatchitemid ");
        sql.append(" and a.s_qcbatchid = b.s_qcbatchid ");
        sql.append(" and b.s_qcbatchid = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" and b.qcbatchsampletypeid is null ");
        sql.append(" order by b.usersequence");
        return sql.toString();
    }

    public static String getQCBatchitemId(SDI sdi, String sampleId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select s_qcbatchitemid from sdidata where keyid1 = '" + sampleId + "'");
        sql.append(" and s_qcbatchid = '" + sdi.getKeyid1() + "'");
        return sql.toString();
    }

    public static String getDistinctUnknownDatasets(SDI sdi, String batchSdcId, String sampleId, String paramListType) {
        StringBuffer sql = new StringBuffer();
        if (paramListType == null) {
            paramListType = "";
        }
        sql.append(" select distinct ");
        sql.append(" T1.sdcid, T1.keyid1, ");
        sql.append(" T1.keyid2, T1.keyid3, ");
        sql.append(" T1.paramlistid, ");
        sql.append(" T1.paramlistversionid, ");
        sql.append(" T1.variantid, T1.dataset ");
        sql.append(" from  s_qcmethod_workitem, workitemitem, sdidata T1, sdiworkitemitem");
        if (paramListType.trim().length() > 0) {
            sql.append(", paramlist");
        }
        sql.append(" where s_qcmethod_workitem.workitemid = workitemitem.workitemid and coalesce(s_qcmethod_workitem.workitemversionid,workitemitem.workitemversionid) = workitemitem.workitemversionid ");
        sql.append(" and workitemitem.keyid1 = T1.paramlistid and ");
        sql.append(" (workitemitem.keyid2 ='C' or workitemitem.keyid2 = T1.paramlistversionid)");
        sql.append(" and workitemitem.keyid3=T1.variantid and T1.sdcid ='" + batchSdcId + "'");
        if (paramListType.trim().length() > 0) {
            sql.append(" and T1.paramlistid = paramlist.paramlistid and T1.paramlistversionid = paramlist.paramlistversionid ");
            sql.append(" and T1.variantid = paramlist.variantid and  paramlist.s_paramlisttype='" + paramListType + "'");
        }
        sql.append(" and T1.keyid1='" + sampleId + "'");
        sql.append(" and T1.s_datasetstatus='Initial'");
        sql.append(" and ( T1.s_qcbatchitemid is null or T1.s_qcbatchitemid = '' )");
        sql.append(" and s_qcmethod_workitem.s_qcmethodid='" + sdi.getKeyid1() + "' and s_qcmethod_workitem.s_qcmethodversionid='" + sdi.getKeyid2() + "'");
        sql.append(" and  sdiworkitemitem.keyid1 = T1.keyid1 and sdiworkitemitem.keyid2 = T1.keyid2 and sdiworkitemitem.keyid3 = T1.keyid3 ");
        sql.append(" and sdiworkitemitem.workitemid = s_qcmethod_workitem.workitemid ");
        sql.append(" order by T1.dataset ");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeTemplate(SDI sdi, String QCBatchId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT T2.QCBATCHSDCID, T1.QCTEMPLATEKEYID1");
        sql.append(" FROM S_QCBATCHSAMPLETYPE T1, S_QCBATCH T2");
        sql.append(" WHERE T2.S_QCBATCHID = T1.QCBATCHID  ");
        sql.append(" AND T1.QCBATCHID = '" + QCBatchId + "'");
        sql.append(" AND T1.S_QCBATCHSAMPLETYPEID = '" + sdi.getKeyid1() + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeTemplate(SDI sdi, String QCBatchId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT T2.QCBATCHSDCID, T1.QCTEMPLATEKEYID1");
        sql.append(" FROM S_QCBATCHSAMPLETYPE T1, S_QCBATCH T2");
        sql.append(" WHERE T2.S_QCBATCHID = T1.QCBATCHID  ");
        sql.append(" AND T1.QCBATCHID = " + safeSQL.addVar(QCBatchId));
        sql.append(" AND T1.S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(sdi.getKeyid1()));
        return sql.toString();
    }

    public static String getWorkitemId(SDI sdi, String QCBatchItemId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT T1.WORKITEMID, T1.WORKITEMVERSIONID");
        sql.append(" FROM S_QCBATCHSAMPLETYPE T1, S_QCBATCHITEM T2");
        sql.append(" WHERE T1.S_QCBATCHSAMPLETYPEID = T2.QCBATCHSAMPLETYPEID");
        sql.append(" AND T2.S_QCBATCHITEMID = '" + QCBatchItemId + "'");
        sql.append(" AND T2.S_QCBATCHID = '" + sdi.getKeyid1() + "'");
        return sql.toString();
    }

    public static String getSDIWorkitemItemDetails(String sdcId, String keyId1, String workitemId, String paramListType) {
        if (paramListType == null) {
            paramListType = "";
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ITEMKEYID1, ITEMKEYID2, ITEMKEYID3, ITEMINSTANCE");
        sql.append(" FROM SDIWORKITEMITEM sdiwii");
        if (paramListType.trim().length() > 0) {
            sql.append(", PARAMLIST pl ");
        }
        sql.append(" WHERE sdiwii.SDCID = '" + sdcId + "'");
        sql.append(" AND sdiwii.KEYID1 = '" + keyId1 + "'");
        sql.append(" AND sdiwii.WORKITEMID = '" + workitemId + "'");
        sql.append(" AND sdiwii.WORKITEMINSTANCE = '1'");
        sql.append(" AND sdiwii.ITEMSDCID = 'ParamList'");
        if (paramListType.trim().length() > 0) {
            sql.append(" and sdiwii.itemkeyid1 = pl.paramlistid and sdiwii.itemkeyid2 = pl.paramlistversionid ");
            sql.append(" and sdiwii.itemkeyid3 = pl.variantid and  pl.s_paramlisttype='" + paramListType + "'");
        }
        sql.append(" ORDER BY sdiwii.WORKITEMITEMID ");
        return sql.toString();
    }

    public static String getDistinctWorkitemIds(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct swi.workitemid, swi.workitemversionid from sdiworkitem swi, sdiworkitemitem swii, sdidata ds, s_qcbatchitem qcbi ").append(" where swi.sdcid = swii.sdcid and swi.keyid1 = swii.keyid1 and swi.keyid2 = swii.keyid2 and swi.keyid3 = swii.keyid3").append(" and swi.workitemid = swii.workitemid and swi.workiteminstance = swii.workiteminstance ").append(" and  swii.sdcid = ds.sdcid").append(" and swii.keyid1 = ds.keyid1").append(" and swii.keyid2 = ds.keyid2").append(" and swii.keyid3 = ds.keyid3").append(" and swii.itemkeyid1 = ds.paramlistid").append(" and swii.itemkeyid2 = ds.paramlistversionid").append(" and swii.itemkeyid3 = ds.variantid").append(" and swii.iteminstance = ds.dataset").append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid and qcbi.s_qcbatchid ='" + sdi.getKeyid1() + "' and qcbi.qcbatchsampletypeid IS NULL").append(" order by swi.workitemid ");
        return sql.toString();
    }

    public static String getDistinctWorkitemIds(SDI sdi, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct swi.workitemid, swi.workitemversionid from sdiworkitem swi, sdiworkitemitem swii, sdidata ds, s_qcbatchitem qcbi ").append(" where swi.sdcid = swii.sdcid and swi.keyid1 = swii.keyid1 and swi.keyid2 = swii.keyid2 and swi.keyid3 = swii.keyid3").append(" and swi.workitemid = swii.workitemid and swi.workiteminstance = swii.workiteminstance ").append(" and  swii.sdcid = ds.sdcid").append(" and swii.keyid1 = ds.keyid1").append(" and swii.keyid2 = ds.keyid2").append(" and swii.keyid3 = ds.keyid3").append(" and swii.itemkeyid1 = ds.paramlistid").append(" and swii.itemkeyid2 = ds.paramlistversionid").append(" and swii.itemkeyid3 = ds.variantid").append(" and swii.iteminstance = ds.dataset").append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid and qcbi.s_qcbatchid = " + safeSQL.addVar(sdi.getKeyid1()) + " and qcbi.qcbatchsampletypeid IS NULL").append(" order by swi.workitemid ");
        return sql.toString();
    }

    public static String getDistinctWorkitemIdsForLinkedQCBatchItemId(SDI sdi, String linktoQcBatchItemId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct swi.workitemid, swi.workitemversionid ").append(" from sdiworkitem swi, sdiworkitemitem swii, sdidata ds, s_qcbatchitem qcbi ").append(" where swi.sdcid = swii.sdcid and swi.keyid1 = swii.keyid1 and swi.keyid2 = swii.keyid2 and swi.keyid3 = swii.keyid3").append(" and swi.workitemid = swii.workitemid and swi.workiteminstance = swii.workiteminstance ").append(" and swii.sdcid = ds.sdcid").append(" and swii.keyid1 = ds.keyid1").append(" and swii.keyid2 = ds.keyid2").append(" and swii.keyid3 = ds.keyid3").append(" and swii.itemkeyid1 = ds.paramlistid").append(" and swii.itemkeyid2 = ds.paramlistversionid").append(" and swii.itemkeyid3 = ds.variantid").append(" and swii.iteminstance = ds.dataset").append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid  and qcbi.s_qcbatchid= '" + sdi.getKeyid1() + "' and qcbi.s_qcbatchitemid='" + linktoQcBatchItemId + "'").append(" order by swi.workitemid ");
        return sql.toString();
    }

    public static String getDistinctWorkitemIdsForLinkedQCBatchItemId(SDI sdi, String linktoQcBatchItemId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct swi.workitemid, swi.workitemversionid ").append(" from sdiworkitem swi, sdiworkitemitem swii, sdidata ds, s_qcbatchitem qcbi ").append(" where swi.sdcid = swii.sdcid and swi.keyid1 = swii.keyid1 and swi.keyid2 = swii.keyid2 and swi.keyid3 = swii.keyid3").append(" and swi.workitemid = swii.workitemid and swi.workiteminstance = swii.workiteminstance ").append(" and swii.sdcid = ds.sdcid").append(" and swii.keyid1 = ds.keyid1").append(" and swii.keyid2 = ds.keyid2").append(" and swii.keyid3 = ds.keyid3").append(" and swii.itemkeyid1 = ds.paramlistid").append(" and swii.itemkeyid2 = ds.paramlistversionid").append(" and swii.itemkeyid3 = ds.variantid").append(" and swii.iteminstance = ds.dataset").append(" and ds.s_qcbatchid = qcbi.s_qcbatchid and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid  and qcbi.s_qcbatchid= " + safeSQL.addVar(sdi.getKeyid1()) + " and qcbi.s_qcbatchitemid = " + safeSQL.addVar(linktoQcBatchItemId)).append(" order by swi.workitemid ");
        return sql.toString();
    }

    public static String getSampleIDForQCBatchitemID(String qcBatchId, String qcBatchItemId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT keyid1, s_qcbatchitemid FROM sdidata ");
        sql.append("  WHERE sdidata.s_qcbatchitemid = '");
        sql.append(qcBatchItemId + "'");
        sql.append(" and sdidata.s_qcbatchid='" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getSampleIDForQCBatchitemID(String qcBatchId, String qcBatchItemId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT keyid1, s_qcbatchitemid FROM sdidata ");
        sql.append("  WHERE sdidata.s_qcbatchitemid = ");
        sql.append(safeSQL.addVar(qcBatchItemId));
        sql.append(" and sdidata.s_qcbatchid = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getLinkedQCBatchitemID(String qcBatchId, String qcBatchItemId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT linktoqcbatchitemid FROM s_qcbatchitem ");
        sql.append("  WHERE s_qcbatchitemid = '");
        sql.append(qcBatchItemId + "'");
        sql.append(" and s_qcbatchid='" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getLinkedQCBatchitemID(String qcBatchId, String qcBatchItemId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT linktoqcbatchitemid FROM s_qcbatchitem ");
        sql.append("  WHERE s_qcbatchitemid = ");
        sql.append(safeSQL.addVar(qcBatchItemId));
        sql.append(" and s_qcbatchid = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getSDAndTargetValueForQCBatchParameter(SDI sdi) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select sd, targetvalue from s_qcbatchparamset ");
        sql.append(" where s_qcbatchparamset.s_qcbatchsampletypeid = '" + sdi.getKeyid1() + "' and ");
        sql.append(" s_qcbatchparamset.s_qcbatchparamsetid = '" + sdi.getKeyid2() + "'");
        return sql.toString();
    }

    public static String getQCMethodSampleTypeLimitId(String batchSampleTypeId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT MSTL.S_QCMETHODSAMPLETYPEID, MSTL.S_QCMETHODSAMPLETYPELIMITID, MSTL.PARAMID, BPS.SD,");
        sql.append(" BPS.TARGETVALUE FROM S_QCMETHODSAMPLETYPELIMIT MSTL, S_QCBATCHPARAMSET BPS, S_QCBATCHSAMPLETYPE BST");
        sql.append(" WHERE MSTL.PARAMID = BPS.PARAMID AND MSTL.S_QCMETHODSAMPLETYPEID = BST.QCMETHODSAMPLETYPEID ");
        sql.append("  AND BPS.S_QCBATCHSAMPLETYPEID = BST.S_QCBATCHSAMPLETYPEID");
        sql.append(" AND BST.S_QCBATCHSAMPLETYPEID = '" + batchSampleTypeId + "'");
        sql.append(" ORDER BY MSTL.S_QCMETHODSAMPLETYPELIMITID ");
        return sql.toString();
    }

    public static String getQCMethodSampleTypeLimitId(String batchSampleTypeId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT MSTL.S_QCMETHODSAMPLETYPEID, MSTL.S_QCMETHODSAMPLETYPELIMITID, MSTL.PARAMID, BPS.SD,");
        sql.append(" BPS.TARGETVALUE FROM S_QCMETHODSAMPLETYPELIMIT MSTL, S_QCBATCHPARAMSET BPS, S_QCBATCHSAMPLETYPE BST");
        sql.append(" WHERE MSTL.PARAMID = BPS.PARAMID AND MSTL.S_QCMETHODSAMPLETYPEID = BST.QCMETHODSAMPLETYPEID ");
        sql.append("  AND BPS.S_QCBATCHSAMPLETYPEID = BST.S_QCBATCHSAMPLETYPEID");
        sql.append(" AND BST.S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(batchSampleTypeId));
        sql.append(" ORDER BY MSTL.S_QCMETHODSAMPLETYPELIMITID ");
        return sql.toString();
    }

    public static String getDataItemsForQCBatchItems(String qcBatchId, String qcBatchItemIds, String paramlistid, String paramlistversionid, String variantid) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, ");
        sql.append(" DI.DATASET, DI.PARAMTYPE, DI.PARAMID, DI.REPLICATEID,");
        sql.append(" DI.TRANSFORMVALUE, DI.DISPLAYFORMAT ");
        sql.append(" FROM SDIDATAITEM DI, SDIDATA DS WHERE DI.SDCID = DS.SDCID AND DI.KEYID1 = DS.KEYID1 ");
        sql.append(" AND DI.KEYID2 = DS.KEYID2 AND DI.KEYID3 = DS.KEYID3 AND DI.PARAMLISTID = DS.PARAMLISTID ");
        sql.append(" AND DI.PARAMLISTVERSIONID = DS.PARAMLISTVERSIONID AND DI.VARIANTID = DS.VARIANTID ");
        sql.append(" AND DI.DATASET = DS.DATASET ");
        sql.append(" AND DS.S_QCBATCHITEMID IN ( " + qcBatchItemIds + " ) ");
        sql.append(" AND DS.S_QCBATCHID = '" + qcBatchId + "'");
        sql.append(" AND DS.PARAMLISTID = '" + paramlistid + "'");
        sql.append(" AND DS.PARAMLISTVERSIONID = '" + paramlistversionid + "'");
        sql.append(" AND DS.VARIANTID = '" + variantid + "'");
        sql.append(" ORDER BY DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, DI.DATASET, DI.PARAMTYPE,");
        sql.append(" DI.PARAMID, DI.REPLICATEID");
        return sql.toString();
    }

    public static String getDataItemsForQCBatchItems(String qcBatchId, String qcBatchItemIds, String paramlistid, String paramlistversionid, String variantid, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, ");
        sql.append(" DI.DATASET, DI.PARAMTYPE, DI.PARAMID, DI.REPLICATEID,");
        sql.append(" DI.TRANSFORMVALUE, DI.DISPLAYFORMAT ");
        sql.append(" FROM SDIDATAITEM DI, SDIDATA DS WHERE DI.SDCID = DS.SDCID AND DI.KEYID1 = DS.KEYID1 ");
        sql.append(" AND DI.KEYID2 = DS.KEYID2 AND DI.KEYID3 = DS.KEYID3 AND DI.PARAMLISTID = DS.PARAMLISTID ");
        sql.append(" AND DI.PARAMLISTVERSIONID = DS.PARAMLISTVERSIONID AND DI.VARIANTID = DS.VARIANTID ");
        sql.append(" AND DI.DATASET = DS.DATASET ");
        sql.append(" AND DS.S_QCBATCHITEMID IN ( " + safeSQL.addIn(qcBatchItemIds) + " ) ");
        sql.append(" AND DS.S_QCBATCHID = " + safeSQL.addVar(qcBatchId));
        sql.append(" AND DS.PARAMLISTID = " + safeSQL.addVar(paramlistid));
        sql.append(" AND DS.PARAMLISTVERSIONID = " + safeSQL.addVar(paramlistversionid));
        sql.append(" AND DS.VARIANTID = " + safeSQL.addVar(variantid));
        sql.append(" ORDER BY DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, DI.DATASET, DI.PARAMTYPE,");
        sql.append(" DI.PARAMID, DI.REPLICATEID");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeFailCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Fail";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeFailCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Fail";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecFailCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Fail";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecFailCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Fail";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCBatchSampleTypePassCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Pass";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypePassCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Pass";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecPassCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Pass";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecPassCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Pass";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCBatchSampleTypeWarningCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Warning";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeWarningCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Warning";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE EVALSTATUS = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecWarningCount(String qcBatchId, String condition) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Warning";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = '").append(condition).append("'");
        sql.append(" AND QCBATCHID = '" + qcBatchId + "'");
        return sql.toString();
    }

    public static String getQCBatchSampleTypeSpecWarningCount(String qcBatchId, String condition, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        if (condition == null || condition.trim().equals("")) {
            condition = "Warning";
        }
        sql.append("SELECT COUNT( 1 ) COUNT FROM S_QCBATCHSAMPLETYPE ");
        sql.append(" WHERE SPECCONDITION = ").append(safeSQL.addVar(condition));
        sql.append(" AND QCBATCHID = " + safeSQL.addVar(qcBatchId));
        return sql.toString();
    }

    public static String getQCSampleBatchItemIdsForQCBatch(String qcBatchId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHITEMID FROM S_QCBATCHITEM WHERE QCBATCHSAMPLETYPEID IS NOT null ");
        sql.append(" AND S_QCBATCHID = '" + qcBatchId + "' ");
        sql.append(" ORDER BY USERSEQUENCE ");
        return sql.toString();
    }

    public static String getSampleIdForQCBatchItem(String qcBatchId, String qcBatchItemId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT SDCID, KEYID1 FROM SDIDATA WHERE S_QCBATCHID = '" + qcBatchId + "' ");
        sql.append(" AND S_QCBATCHITEMID = '" + qcBatchItemId + "' ");
        return sql.toString();
    }

    public static String getAllQCBatchItemIdsForQCBatch(String qcBatchId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT S_QCBATCHITEMID FROM S_QCBATCHITEM WHERE ");
        sql.append(" S_QCBATCHID = '" + qcBatchId + "' ORDER BY USERSEQUENCE ");
        return sql.toString();
    }

    public static String getParamsNotAddedInBatchParamSet(String qcBatchId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT T3.QCBATCHSAMPLETYPEID, T1.PARAMID");
        sql.append(" FROM SDIDATAITEM T1, SDIDATA T2, S_QCBATCHITEM T3");
        sql.append(" WHERE T1.SDCID = T2.SDCID");
        sql.append(" AND T1.KEYID1 = T2.KEYID1");
        sql.append(" AND T1.KEYID2 = T2.KEYID2");
        sql.append(" AND T1.KEYID3 = T2.KEYID3");
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T1.DATASET = T2.DATASET");
        sql.append(" AND T2.S_QCBATCHID = T3.S_QCBATCHID");
        sql.append(" AND T2.S_QCBATCHITEMID = T3.S_QCBATCHITEMID");
        sql.append(" AND T3.S_QCBATCHID = '" + qcBatchId + "'");
        sql.append(" AND T3.QCBATCHSAMPLETYPEID IS NOT NULL");
        sql.append(" AND 1 > ( SELECT COUNT( 1 ) FROM S_QCBATCHPARAMSET  T4 WHERE ");
        sql.append(" T4.S_QCBATCHSAMPLETYPEID = T3.QCBATCHSAMPLETYPEID ");
        sql.append(" AND  T4.PARAMID = T1.PARAMID ) ");
        sql.append(" ORDER BY T3.QCBATCHSAMPLETYPEID, T1.PARAMID");
        return sql.toString();
    }

    public static String getParamsNotAddedInBatchParamSet(String qcBatchId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT T3.QCBATCHSAMPLETYPEID, T1.PARAMID");
        sql.append(" FROM SDIDATAITEM T1, SDIDATA T2, S_QCBATCHITEM T3");
        sql.append(" WHERE T1.SDCID = T2.SDCID");
        sql.append(" AND T1.KEYID1 = T2.KEYID1");
        sql.append(" AND T1.KEYID2 = T2.KEYID2");
        sql.append(" AND T1.KEYID3 = T2.KEYID3");
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T1.DATASET = T2.DATASET");
        sql.append(" AND T2.S_QCBATCHID = T3.S_QCBATCHID");
        sql.append(" AND T2.S_QCBATCHITEMID = T3.S_QCBATCHITEMID");
        sql.append(" AND T3.S_QCBATCHID = " + safeSQL.addVar(qcBatchId));
        sql.append(" AND T3.QCBATCHSAMPLETYPEID IS NOT NULL");
        sql.append(" AND 1 > ( SELECT COUNT( 1 ) FROM S_QCBATCHPARAMSET  T4 WHERE ");
        sql.append(" T4.S_QCBATCHSAMPLETYPEID = T3.QCBATCHSAMPLETYPEID ");
        sql.append(" AND  T4.PARAMID = T1.PARAMID ) ");
        sql.append(" ORDER BY T3.QCBATCHSAMPLETYPEID, T1.PARAMID");
        return sql.toString();
    }

    public static String getEvalRuleParamToAddForQCBatch(String qcBatchId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT T1.S_QCBATCHEVALRULEID, T2.S_QCBATCHSAMPLETYPEID, T3.S_QCBATCHPARAMSETID");
        sql.append(" FROM S_QCBATCHEVALRULE T1, S_QCBATCHSAMPLETYPE T2, S_QCBATCHPARAMSET T3");
        sql.append(" WHERE T1.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID");
        sql.append(" AND T3.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID ");
        sql.append(" AND T2.QCBATCHID ='" + qcBatchId + "'");
        sql.append(" AND 1 > ( SELECT COUNT( 1 ) FROM S_QCBATCHEVALRULEPARAM T4 WHERE ");
        sql.append(" T4.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID AND ");
        sql.append(" T4.S_QCBATCHPARAMSETID = T3.S_QCBATCHPARAMSETID AND ");
        sql.append(" T4.S_QCBATCHEVALRULEID = T1.S_QCBATCHEVALRULEID )");
        return sql.toString();
    }

    public static String getEvalRuleParamToAddForQCBatch(String qcBatchId, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT T1.S_QCBATCHEVALRULEID, T2.S_QCBATCHSAMPLETYPEID, T3.S_QCBATCHPARAMSETID");
        sql.append(" FROM S_QCBATCHEVALRULE T1, S_QCBATCHSAMPLETYPE T2, S_QCBATCHPARAMSET T3");
        sql.append(" WHERE T1.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID");
        sql.append(" AND T3.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID ");
        sql.append(" AND T2.QCBATCHID =" + safeSQL.addVar(qcBatchId));
        sql.append(" AND 1 > ( SELECT COUNT( 1 ) FROM S_QCBATCHEVALRULEPARAM T4 WHERE ");
        sql.append(" T4.S_QCBATCHSAMPLETYPEID = T2.S_QCBATCHSAMPLETYPEID AND ");
        sql.append(" T4.S_QCBATCHPARAMSETID = T3.S_QCBATCHPARAMSETID AND ");
        sql.append(" T4.S_QCBATCHEVALRULEID = T1.S_QCBATCHEVALRULEID )");
        return sql.toString();
    }
}

