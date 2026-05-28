/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.Sample;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.ddt.rules.SMSUser;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GLPRule
extends BaseBioBankRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 99076 $";
    public static final String NAME = "GLP Rule";

    public GLPRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return this.getTranslationProcessor().translate(NAME);
    }

    public void processStudyGLPRule(String studyid, boolean forceUpdate) throws SapphireException {
        DataSet ds;
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && StringUtil.getLen(studyid) > 0L && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid from s_sample s, s_study ss where ss.s_studyid = ? and ss.s_studyid = s.sstudyid and ss.defaultglpflag = 'N' and s.glpflag = 'Y'", (Object[])new String[]{studyid})) != null) {
            ArrayList<String> glpsamples = new ArrayList<String>();
            for (int i = 0; i < ds.size(); ++i) {
                glpsamples.add(ds.getValue(i, "s_sampleid"));
            }
            if (glpsamples.size() > 0) {
                if (forceUpdate) {
                    Sample.clearGLPFlag(this.getActionProcessor(), glpsamples, forceUpdate);
                } else {
                    throw new SapphireException(this.getRuleId(), "CONFIRM", glpsamples.size() + " " + this.getTranslationProcessor().translate("Sample(s) in Study will lose their GLP status. Continue?"));
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processDepartmentGLPRule(String departmentid, boolean forceUpdate) throws SapphireException {
        DataSet ds;
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && StringUtil.getLen(departmentid) > 0L && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid from s_sample s, trackitem t, department d where s.s_sampleid = t.linkkeyid1 and t.linksdcid = 'Sample' and t.custodialdepartmentid = d.departmentid and d.departmentid = ? and s.glpflag = 'Y' and d.glpflag = 'N'", (Object[])new String[]{departmentid})) != null) {
            ArrayList<String> glpsamples = new ArrayList<String>();
            for (int i = 0; i < ds.size(); ++i) {
                glpsamples.add(ds.getValue(i, "s_sampleid"));
            }
            if (glpsamples.size() > 0) {
                if (forceUpdate) {
                    Sample.clearGLPFlag(this.getActionProcessor(), glpsamples, forceUpdate);
                } else {
                    throw new SapphireException(this.getRuleId(), "CONFIRM", glpsamples.size() + " " + this.getTranslationProcessor().translate("Sample(s) in Custodial Department will lose their GLP status. Continue?"));
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processPhysicalStoreGLPRule(String physicalstoreid, boolean forceUpdate) throws SapphireException {
        ArrayList<String> glpsamples;
        String storageunitid;
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && StringUtil.getLen(physicalstoreid) > 0L && StringUtil.getLen(physicalstoreid) > 0L && !PhysicalStore.isGLP(this.database, physicalstoreid) && this.isAnySampleGLP(storageunitid = StorageUnitSDC.getStorageUntitidByLinkKeyid(this.getQueryProcessor(), "PhysicalStore", physicalstoreid), glpsamples = new ArrayList<String>())) {
            if (forceUpdate) {
                Sample.clearGLPFlag(this.getActionProcessor(), glpsamples, forceUpdate);
            } else {
                throw new SapphireException(this.getRuleId(), "CONFIRM", glpsamples.size() + " " + this.getTranslationProcessor().translate("Sample(s) in Physical Store will lose their GLP status. Continue?"));
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processSampleFamilyGLPRule(String samplefamilyid, boolean forceUpdate) throws SapphireException {
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && StringUtil.getLen(samplefamilyid) > 0L) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select sf.s_samplefamilyid, s.s_studyid, s.defaultglpflag, s_sample.s_sampleid, s_sample.glpflag");
            sql.append(" from s_study s, s_samplefamily sf, s_sample ");
            sql.append(" where s.s_studyid = sf.sstudyid");
            sql.append(" and s_sample.samplefamilyid = sf.s_samplefamilyid");
            sql.append(" and s_sample.glpflag = 'Y'");
            sql.append(" and s.defaultglpflag = 'N'");
            sql.append(" and sf.s_samplefamilyid in ( ").append(safeSQL.addIn(samplefamilyid, ";")).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                if (forceUpdate) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
                    props.setProperty("glpflag", "N");
                    props.setProperty("__sdcruleconfirm", "Y");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                } else {
                    throw new SapphireException(this.getRuleId(), "CONFIRM", ds.size() + " " + this.getTranslationProcessor().translate("Sample(s) in Sample Family will lose their GLP status. Continue?"));
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processBoxGLPRule(String boxid, boolean forceUpdate) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && StringUtil.getLen(boxid) > 0L) {
            boolean checksampleglp = false;
            StringBuilder sql = new StringBuilder();
            sql.append("select t1.linkkeyid1, t1.custodialdepartmentid, t1.custodialuserid, t1.currentstorageunitid, ");
            sql.append(" (select s1.storageunitid from storageunit s1 where s1.linksdcid = t1.linksdcid and s1.linkkeyid1 = t1.linkkeyid1 ) boxstorageunitid ");
            sql.append(" from trackitem t1");
            sql.append(" where t1.linksdcid = 'LV_Box'");
            sql.append(" and t1.linkkeyid1 = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{boxid});
            if (ds != null && ds.size() > 0) {
                ArrayList<String> glpsamples;
                String physicalstoreid = StorageUnitSDC.getParentSDIBySDC(this.getQueryProcessor(), ds.getValue(0, "currentstorageunitid"), "PhysicalStore");
                String boxstorageunitid = ds.getValue(0, "boxstorageunitid");
                String custodialdepartmentid = ds.getValue(0, "custodialdepartmentid");
                String custodialuserid = ds.getValue(0, "custodialuserid");
                if (StringUtil.getLen(physicalstoreid) > 0L && !PhysicalStore.isGLP(this.database, physicalstoreid)) {
                    checksampleglp = true;
                } else if (StringUtil.getLen(custodialuserid) > 0L) {
                    if (!SMSUser.isGLP(this.getQueryProcessor(), custodialuserid)) {
                        checksampleglp = true;
                    }
                } else if (StringUtil.getLen(custodialdepartmentid) > 0L && !Department.isGLP(this.getQueryProcessor(), custodialdepartmentid)) {
                    checksampleglp = true;
                }
                if (checksampleglp && this.isAnySampleGLP(boxstorageunitid, glpsamples = new ArrayList<String>())) {
                    if (forceUpdate) {
                        Sample.clearGLPFlag(this.getActionProcessor(), glpsamples, forceUpdate);
                    } else {
                        throw new SapphireException(this.getRuleId(), "CONFIRM", glpsamples.size() + " " + this.getTranslationProcessor().translate("Sample(s) in Box will lose their GLP status. Continue?"));
                    }
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processRule(List samples, boolean forceUpdate) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (this.isRuleActive() && samples != null && samples.size() > 0) {
            StringBuilder sql = new StringBuilder();
            if (samples.size() <= 750) {
                DataSet ds;
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select s.s_sampleid, s.glpflag, s.sstudyid, t.custodialuserid, t.custodialdepartmentid, t.currentstorageunitid,");
                sql.append(" (select ss.defaultglpflag from s_study ss where ss.s_studyid = s.sstudyid) studyglpflag,");
                sql.append(" (select su.glpflag from sysuser su where su.sysuserid = t.custodialuserid) sysuserglpflag,");
                sql.append(" su.parentid storageparentid,");
                sql.append(" su.linksdcid storagelinksdcid,");
                sql.append(" su.linkkeyid1 storagelinkkeyid1,");
                sql.append(" (select s1.linksdcid from storageunit s1 where s1.storageunitid = su.parentid) parentlinksdcid,");
                sql.append(" (select s1.linkkeyid1 from storageunit s1 where s1.storageunitid = su.parentid) parentlinkkeyid1");
                sql.append(" from trackitem t LEFT OUTER JOIN storageunit su on su.storageunitid = t.currentstorageunitid, s_sample s ");
                sql.append(" where t.linksdcid = 'Sample'");
                sql.append(" and s.s_sampleid = t.linkkeyid1");
                if (samples.size() <= 750) {
                    sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(OpalUtil.toDelimitedString(samples, "','"))).append(")");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(samples, ";"), null, null);
                    if (StringUtil.getLen(rsetid) > 0L) {
                        sql.append(" and s.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                        if (StringUtil.getLen(rsetid) > 0L) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    } else {
                        throw new SapphireException("[GLPRule.ProcessRule] Unable to create RSET for " + OpalUtil.toDelimitedString(samples, ";"));
                    }
                }
                if (ds != null) {
                    ArrayList<String> glpsamples = new ArrayList<String>();
                    ArrayList<String> parentsamples = new ArrayList<String>();
                    DataSet parentSampleDS = null;
                    for (int i = 0; i < ds.size(); ++i) {
                        int row;
                        boolean check = true;
                        String sampleid = ds.getValue(i, "s_sampleid");
                        String glpflag = ds.getValue(i, "glpflag");
                        String currentstorageunitid = ds.getValue(i, "currentstorageunitid");
                        if ("Y".equals(glpflag)) {
                            String physicalstoreid;
                            String storagelinksdcid = ds.getValue(i, "storagelinksdcid", ds.getValue(i, "parentlinksdcid"));
                            String custodialuserid = ds.getValue(i, "custodialuserid");
                            if (OpalUtil.isNotEmpty(custodialuserid)) {
                                String sysuserglpflag = ds.getValue(i, "sysuserglpflag");
                                if (!"Y".equals(sysuserglpflag)) {
                                    glpsamples.add(sampleid);
                                }
                                check = false;
                            }
                            if (check && "LV_Box".equals(storagelinksdcid)) {
                                String boxid = ds.getValue(i, "storagelinkkeyid1", ds.getValue(i, "parentlinkkeyid1"));
                                String departmentid = this.getCustodialDepartmentId("LV_Box", boxid);
                                if (StringUtil.getLen(departmentid) > 0L && !this.isGLP("Department", departmentid)) {
                                    glpsamples.add(sampleid);
                                }
                                check = false;
                            }
                            if (check && StringUtil.getLen(physicalstoreid = this.getParentSDIBySDC(currentstorageunitid, "PhysicalStore")) > 0L) {
                                if (!this.isGLP("PhysicalStore", physicalstoreid)) {
                                    glpsamples.add(sampleid);
                                }
                                check = false;
                            }
                            if (!check) continue;
                            String studyid = ds.getValue(i, "sstudyid");
                            String studyglpflag = ds.getValue(i, "studyglpflag");
                            if (StringUtil.getLen(studyid) <= 0L || "Y".equals(studyglpflag)) continue;
                            glpsamples.add(sampleid);
                            continue;
                        }
                        if (parentSampleDS == null) {
                            if (ds.size() > 1000) {
                                String rsetid = this.getDAMProcessor().createRSet("Sample", ds.getColumnValues("s_sampleid", ";"), null, null);
                                parentSampleDS = this.getQueryProcessor().getPreparedSqlDataSet("select sm.sourcesampleid, sm.destsampleid from s_samplemap sm, s_sample s where sm.destsampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?) and sm.sourcesampleid = s.s_sampleid and s.glpflag = 'Y'", (Object[])new String[]{rsetid});
                                this.getDAMProcessor().clearRSet(rsetid);
                            } else {
                                safeSQL.reset();
                                parentSampleDS = this.getQueryProcessor().getPreparedSqlDataSet("select sm.sourcesampleid, sm.destsampleid from s_samplemap sm, s_sample s where sm.destsampleid in (" + safeSQL.addIn(ds.getColumnValues("s_sampleid", "','")) + ") and sm.sourcesampleid = s.s_sampleid and s.glpflag = 'Y'", safeSQL.getValues());
                            }
                        }
                        if (!OpalUtil.isNotEmpty(parentSampleDS) || (row = parentSampleDS.findRow("destsampleid", sampleid)) == -1) continue;
                        parentsamples.add(parentSampleDS.getString(row, "sourcesampleid"));
                    }
                    if (glpsamples.size() > 0) {
                        if (forceUpdate) {
                            Sample.clearGLPFlag(this.getActionProcessor(), glpsamples, forceUpdate);
                        } else {
                            throw new SapphireException(this.getRuleId(), "CONFIRM", this.getTranslationProcessor().translate("Following sample(s) will lose their GLP status. Continue?") + "<br><ul><li>" + OpalUtil.toDelimitedString(glpsamples, "</li><li>") + "</li></ul>");
                        }
                    }
                    if (parentsamples.size() > 0) {
                        Sample.clearGLPFlag(this.getActionProcessor(), parentsamples, forceUpdate);
                    }
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private boolean isAnySampleGLP(String storageunitid, List<String> glpsamples) throws SapphireException {
        boolean glp = false;
        StringBuilder sql = new StringBuilder();
        if (this.connectionInfo.isOracle()) {
            sql.append("SELECT S.S_SAMPLEID, S.GLPFLAG");
            sql.append(" FROM TRACKITEM T, S_SAMPLE S");
            sql.append(" WHERE T.CURRENTSTORAGEUNITID IN (SELECT SU.STORAGEUNITID");
            sql.append(" FROM STORAGEUNIT SU");
            sql.append(" CONNECT BY PRIOR SU.STORAGEUNITID = SU.PARENTID");
            sql.append(" START WITH SU.STORAGEUNITID = ?)");
            sql.append(" AND T.LINKSDCID = 'Sample'");
            sql.append(" AND T.LINKKEYID1 = S.S_SAMPLEID");
            sql.append(" AND S.GLPFLAG = 'Y'");
        } else {
            sql.append("WITH StorageUnitTree (storageunitid)");
            sql.append(" AS (");
            sql.append("    SELECT su.storageunitid");
            sql.append("    FROM storageunit AS su");
            sql.append("    WHERE su.storageunitid = ?");
            sql.append("    UNION ALL");
            sql.append("    SELECT su.storageunitid");
            sql.append("   FROM storageunit AS su");
            sql.append("    INNER JOIN StorageUnitTree AS d");
            sql.append("    ON su.parentid = d.storageunitid");
            sql.append(" )");
            sql.append(" SELECT S.S_SAMPLEID, S.GLPFLAG FROM TRACKITEM T, S_SAMPLE S");
            sql.append(" WHERE T.CURRENTSTORAGEUNITID IN (SELECT st.storageunitid FROM StorageUnitTree st)");
            sql.append(" AND T.LINKSDCID = 'Sample'");
            sql.append(" AND T.LINKKEYID1 = S.S_SAMPLEID");
            sql.append(" AND S.GLPFLAG = 'Y'");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            glp = true;
            for (int i = 0; i < ds.size(); ++i) {
                glpsamples.add(ds.getValue(i, "s_sampleid"));
            }
        }
        return glp;
    }
}

