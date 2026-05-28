/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SampleStateRule
extends BaseBioBankRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 86098 $";
    private String tracelogid = "";
    private String auditreason = "";
    private String auditactivity = "";
    private String auditsignedflag = "";
    private Map<String, String> packageReceiveDateCache = new HashMap<String, String>();
    PropertyList biobankingPolicy;
    HashMap<String, String> boxActiveMap = new HashMap();
    HashMap<String, String> plateActiveMap = new HashMap();
    private HashMap<String, HashMap<Object, String>> custodyMap = new HashMap();
    private HashMap<String, List> userDepartmentMap = new HashMap();

    public SampleStateRule(DBAccess database, ConnectionInfo connectionInfo, String tracelogid, String auditreason, String auditactivity, String auditsignedflag) {
        super(database, connectionInfo);
        this.tracelogid = tracelogid;
        this.auditreason = auditreason;
        this.auditactivity = auditactivity;
        this.auditsignedflag = auditsignedflag;
    }

    @Override
    public String getRuleId() {
        return "SampleStateRule";
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void processRule(List<String> samplelist, boolean forceUpdate) throws SapphireException {
        DataSet ds;
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        boolean hasSMSModule = this.connectionInfo.hasModule("SMS");
        if (samplelist == null || samplelist.size() == 0) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        HashMap<String, Object> trackitems = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.linkkeyid1, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.currentstorageunitid,");
        sql.append("s.linksdcid storagelinksdcid, s.linkkeyid1 storagelinkkeyid1, s.storageunittype, s.parentid,");
        sql.append("( select s2.linksdcid from storageunit s2 where s2.storageunitid = s.parentid ) parentlinksdcid,");
        sql.append("( select s2.linkkeyid1 from storageunit s2 where s2.storageunitid = s.parentid ) parentlinkkeyid1,");
        sql.append("( select s3.storagestatus from s_sample s3 where s3.s_sampleid = t.linkkeyid1 ) samplestoragestatus,");
        sql.append("( select count(trackitemid) from reservestorageunit rsu where rsu.trackitemid = t.trackitemid ) reservecount");
        sql.append(" from trackitem t, storageunit s");
        sql.append(" where t.linksdcid = 'Sample'");
        sql.append(" and t.currentstorageunitid = s.storageunitid");
        SafeSQL safeSQL = new SafeSQL();
        if (samplelist.size() <= 750) {
            sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(samplelist)).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(samplelist, ";"), null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[SampleStateRule.processRule] Unable to create RSET for " + OpalUtil.toDelimitedString(samplelist, ";"));
            sql.append(" and t.linkkeyid1 in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            String sampleid;
            for (int i = 0; i < ds.size(); ++i) {
                HashMap<String, String> map;
                String parentid;
                sampleid = ds.getValue(i, "linkkeyid1");
                String storagelinksdcid = ds.getValue(i, "storagelinksdcid");
                String storagelinkkeyid1 = ds.getValue(i, "storagelinkkeyid1");
                String custodialuserid = ds.getValue(i, "custodialuserid");
                String custodialdepartmentid = ds.getValue(i, "custodialdepartmentid");
                if (storagelinkkeyid1 == null || storagelinkkeyid1.trim().length() == 0) {
                    storagelinksdcid = ds.getValue(i, "parentlinksdcid");
                    storagelinkkeyid1 = ds.getValue(i, "parentlinkkeyid1");
                }
                if ((storagelinkkeyid1 == null || storagelinkkeyid1.trim().length() == 0) && StringUtil.getLen(parentid = ds.getValue(i, "parentid")) != 0L && (map = this.getStorageUnitLinkedSDC(parentid)) != null && map.size() > 0) {
                    storagelinksdcid = map.get("linksdcid");
                    storagelinkkeyid1 = map.get("linkkeyid1");
                }
                PropertyList trackitem = new PropertyList();
                trackitem.setProperty("sampleid", sampleid);
                trackitem.setProperty("samplestoragestatus", ds.getValue(i, "samplestoragestatus"));
                trackitem.setProperty("trackitemid", ds.getValue(i, "trackitemid"));
                trackitem.setProperty("custodialuserid", custodialuserid);
                trackitem.setProperty("custodialdepartmentid", custodialdepartmentid);
                trackitem.setProperty("storagelinksdcid", storagelinksdcid);
                trackitem.setProperty("storagelinkkeyid1", storagelinkkeyid1);
                trackitem.setProperty("reservecount", ds.getValue(i, "reservecount"));
                trackitem.setProperty("currentstorageunitid", ds.getValue(i, "currentstorageunitid"));
                trackitems.put(sampleid, trackitem);
            }
            if (ds.size() < samplelist.size()) {
                sql.setLength(0);
                sql.append("select t.linkkeyid1, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.currentstorageunitid,");
                sql.append("( select s3.storagestatus from s_sample s3 where s3.s_sampleid = t.linkkeyid1 ) samplestoragestatus,");
                sql.append("( select s3.confirmedby from s_sample s3 where s3.s_sampleid = t.linkkeyid1 ) confirmedby,");
                sql.append("( select count(trackitemid) from reservestorageunit rsu where rsu.trackitemid = t.trackitemid ) reservecount");
                sql.append(" from trackitem t");
                sql.append(" where t.linksdcid = 'Sample'");
                sql.append(" and ( t.currentstorageunitid is null or t.currentstorageunitid = '' )");
                sql.append(" and t.custodialuserid is not null");
                safeSQL.reset();
                if (samplelist.size() <= 750) {
                    sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(samplelist, "','"))).append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(samplelist, ";"), null, null);
                    if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[SampleStateRule.processRule] Unable to create RSET for " + OpalUtil.toDelimitedString(samplelist, ";"));
                    sql.append(" and t.linkkeyid1 in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                if (ds != null) {
                    for (int i = 0; i < ds.size(); ++i) {
                        sampleid = ds.getValue(i, "linkkeyid1");
                        if (trackitems.containsKey(sampleid)) continue;
                        PropertyList trackitem = new PropertyList();
                        trackitem.setProperty("sampleid", sampleid);
                        trackitem.setProperty("samplestoragestatus", ds.getValue(i, "samplestoragestatus"));
                        trackitem.setProperty("confirmedby", ds.getValue(i, "confirmedby"));
                        trackitem.setProperty("trackitemid", ds.getValue(i, "trackitemid"));
                        trackitem.setProperty("custodialuserid", ds.getValue(i, "custodialuserid"));
                        trackitem.setProperty("custodialdepartmentid", ds.getValue(i, "custodialdepartmentid"));
                        trackitem.setProperty("storagelinksdcid", "");
                        trackitem.setProperty("storagelinkkeyid1", "");
                        trackitem.setProperty("reservecount", ds.getValue(i, "reservecount"));
                        trackitem.setProperty("currentstorageunitid", ds.getValue(i, "currentstorageunitid"));
                        trackitems.put(sampleid, trackitem);
                    }
                }
            }
        }
        StringBuffer props_trackitemid = new StringBuffer();
        StringBuffer props_custodialdepartmentid = new StringBuffer();
        StringBuffer props_custodialuserid = new StringBuffer();
        String samplereceiveddt = "n";
        ArrayList<String> invalidStatus = new ArrayList<String>();
        HashMap<String, String> confirmMap = new HashMap<String, String>();
        for (String aSamplelist : samplelist) {
            String sampleid = aSamplelist;
            PropertyList trackitem = (PropertyList)trackitems.get(sampleid);
            if (trackitem == null) continue;
            String confirmedby = trackitem.getProperty("confirmedby");
            String samplestoragestatus = trackitem.getProperty("samplestoragestatus");
            String trackitemid = trackitem.getProperty("trackitemid");
            String custodialuserid = trackitem.getProperty("custodialuserid");
            String custodialdepartmentid = trackitem.getProperty("custodialdepartmentid");
            String storagelinksdcid = trackitem.getProperty("storagelinksdcid");
            String storagelinkkeyid1 = trackitem.getProperty("storagelinkkeyid1");
            String reservecount = trackitem.getProperty("reservecount", "0");
            String newcustodialuserid = null;
            String newcustodialdepartmentid = null;
            if (hasSMSModule && ("Disposed".equals(samplestoragestatus) || "Archived".equals(samplestoragestatus) || "3rd Party Transfer".equals(samplestoragestatus)) && (storagelinkkeyid1 != null && storagelinkkeyid1.length() > 0 || custodialuserid != null && custodialuserid.length() > 0)) {
                invalidStatus.add(sampleid);
            }
            if ("LV_Box".equals(storagelinksdcid) || "Plate".equals(storagelinksdcid)) {
                if (!this.isStorageUnitActive(storagelinksdcid, storagelinkkeyid1)) throw new SapphireException("SampleStateRule", "VALIDATION", this.getTranslationProcessor().translate("Samples can not be checked into an Inactive storage unit"));
                newcustodialuserid = this.getSDICustodialUser(storagelinksdcid, storagelinkkeyid1);
                newcustodialdepartmentid = this.getSDICustodialDepartment(storagelinksdcid, storagelinkkeyid1);
                if (hasSMSModule && ("Allocated".equals(samplestoragestatus) || "In Prep".equals(samplestoragestatus)) && confirmedby.length() == 0) {
                    if ("Allocated".equals(samplestoragestatus)) {
                        if (!this.isDepartmentRepository(newcustodialdepartmentid)) throw new SapphireException("SampleStateRule", "VALIDATION", "Allocated sample can only be checked into a repository");
                        confirmMap.put(sampleid, "Allocated");
                    } else {
                        confirmMap.put(sampleid, "In Prep");
                    }
                }
                if (!"0".equals(reservecount)) {
                    sql.setLength(0);
                    sql.append("delete from reservestorageunit");
                    sql.append(" where reservestorageunit.trackitemid = ?");
                    sql.append(" and reservestorageunit.storageunitid in ( select s.storageunitid");
                    sql.append(" from storageunit s");
                    sql.append(" where s.parentid = ( select ss.storageunitid");
                    sql.append(" from storageunit ss");
                    sql.append(" where ss.linkkeyid1 = ?) )");
                    this.database.executePreparedUpdate(sql.toString(), new Object[]{trackitemid, storagelinkkeyid1});
                }
            } else if ("LV_Package".equals(storagelinksdcid)) {
                String packagestatus = this.getColumnValue("LV_Package", storagelinkkeyid1, "packagestatus");
                String packageDestination = this.getColumnValue("LV_Package", storagelinkkeyid1, "recipientdepartmentid");
                if (packagestatus != null && packagestatus.length() > 0 && packageDestination != null && packageDestination.length() > 0) {
                    if (packagestatus.equals("Shipped")) {
                        newcustodialuserid = "";
                        newcustodialdepartmentid = "Transit";
                    } else if (packagestatus.equals("On Hold")) {
                        newcustodialuserid = "";
                        newcustodialdepartmentid = packageDestination;
                    } else if (packagestatus.equals("Received")) {
                        newcustodialuserid = "";
                        newcustodialdepartmentid = packageDestination;
                        samplereceiveddt = this.getPackageReceiveDate(storagelinkkeyid1);
                    } else if (packagestatus.equals("Created") || packagestatus.equals("Expected")) {
                        newcustodialuserid = "";
                        newcustodialdepartmentid = this.getColumnValue("LV_Package", storagelinkkeyid1, "senderdepartmentid");
                    }
                }
                if (hasSMSModule && ("Allocated".equals(samplestoragestatus) || "In Prep".equals(samplestoragestatus)) && confirmedby.length() == 0) {
                    if ("Allocated".equals(samplestoragestatus)) {
                        if (StringUtil.getLen(packageDestination) <= 0L) throw new SapphireException("SampleStateRule", "VALIDATION", "Allocated sample can not be added to a package with no destination");
                        if (!this.isDepartmentRepository(packageDestination)) {
                            throw new SapphireException("SampleStateRule", "VALIDATION", "Allocated sample can only be added to a package whose destination is a repository");
                        }
                        if ("Received".equals(packagestatus) && this.receiveSampleOnPackageReceive()) {
                            confirmMap.put(sampleid, "Allocated");
                        }
                    } else {
                        confirmMap.put(sampleid, "In Prep");
                    }
                }
            } else if ("PhysicalStore".equals(storagelinksdcid)) {
                String cd = this.getCustodialDepartmentId("PhysicalStore", storagelinkkeyid1);
                if (cd == null || cd.length() <= 0) throw new SapphireException("SampleStateRule", "VALIDATION", this.getTranslationProcessor().translate("Physical Store does not have a Custodial Department defined") + " (" + storagelinkkeyid1 + ")");
                newcustodialdepartmentid = cd;
                if (!this.isPhysicalStoreTemporaryStorage(storagelinkkeyid1)) {
                    newcustodialuserid = "";
                }
                if (hasSMSModule && ("Allocated".equals(samplestoragestatus) || "In Prep".equals(samplestoragestatus)) && confirmedby.length() == 0) {
                    if ("Allocated".equals(samplestoragestatus)) {
                        if (!this.isDepartmentRepository(newcustodialdepartmentid)) throw new SapphireException("SampleStateRule", "VALIDATION", "Allocated sample can only be checked into a repository");
                        confirmMap.put(sampleid, "Allocated");
                    } else {
                        confirmMap.put(sampleid, "In Prep");
                    }
                }
            } else if ("".equals(storagelinksdcid) && "".equals(storagelinkkeyid1)) {
                if (StringUtil.getLen(trackitem.getProperty("currentstorageunitid")) > 0L) {
                    newcustodialuserid = "";
                    newcustodialdepartmentid = custodialdepartmentid;
                } else if (StringUtil.getLen(custodialuserid) > 0L) {
                    if (StringUtil.getLen(custodialdepartmentid) > 0L && !this.isUserDepartmentMember(custodialuserid, custodialdepartmentid)) {
                        throw new SapphireException("SampleStateRule", "VALIDATION", this.getTranslationProcessor().translate("User is not a member of Sample's Custodial Department") + " (" + sampleid + ")");
                    }
                    if (hasSMSModule) {
                        if ("Allocated".equals(samplestoragestatus) && confirmedby.length() == 0) {
                            newcustodialdepartmentid = StringUtil.getLen(custodialdepartmentid) == 0L ? this.getDefaultDepartment(custodialuserid) : custodialdepartmentid;
                            if (!this.isDepartmentRepository(newcustodialdepartmentid)) throw new SapphireException("SampleStateRule", "VALIDATION", "Cannot take custody of an Allocated sample unless default Custodial Department is a repository");
                            confirmMap.put(sampleid, "Allocated");
                        }
                    } else {
                        newcustodialdepartmentid = StringUtil.getLen(custodialdepartmentid) == 0L ? this.getDefaultDepartment(custodialuserid) : custodialdepartmentid;
                    }
                }
            }
            if (newcustodialuserid == null || newcustodialdepartmentid == null || newcustodialuserid.equals(custodialuserid) && newcustodialdepartmentid.equals(custodialdepartmentid)) continue;
            props_trackitemid.append(";").append(trackitemid);
            props_custodialuserid.append(";").append(newcustodialuserid);
            props_custodialdepartmentid.append(";").append(newcustodialdepartmentid);
        }
        if (invalidStatus.size() > 0) {
            throw new SapphireException("SampleStateRule", "VALIDATION", "Samples in invalid storage state for filing: " + OpalUtil.toDelimitedString(invalidStatus, "; "));
        }
        if (props_trackitemid.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("trackitemid", props_trackitemid.substring(1));
            props.setProperty("custodialuserid", props_custodialuserid.substring(1));
            props.setProperty("custodialdepartmentid", props_custodialdepartmentid.substring(1));
            props.setProperty("propsmatch", "Y");
            props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
            props.setProperty("tracelogid", this.tracelogid);
            props.setProperty("auditreason", this.auditreason);
            props.setProperty("auditactivity", this.auditactivity);
            props.setProperty("auditsignedflag", this.auditsignedflag);
            try {
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props, false);
            }
            catch (ActionException e) {
                ErrorHandler eh = e.getErrorHandler();
                if (eh == null || eh.size() <= 0) throw e;
                ErrorDetail error = (ErrorDetail)eh.get(0);
                throw new SapphireException(error.getErrorid(), error.getErrorType(), error.getMessage());
            }
        }
        if (confirmMap.size() > 0) {
            String currentUser = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", OpalUtil.toDelimitedString(confirmMap.keySet(), ";"));
            props.setProperty("storagestatus", this.getStorageStatus(confirmMap));
            props.setProperty("confirmedby", currentUser);
            props.setProperty("confirmeddt", "n");
            props.setProperty("receivedby", currentUser);
            props.setProperty("receiveddt", samplereceiveddt);
            props.setProperty("propsmatch", "Y");
            props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
            props.setProperty("__samplestateruleedit", "Y");
            props.setProperty("tracelogid", this.tracelogid);
            props.setProperty("auditreason", this.auditreason);
            props.setProperty("auditactivity", this.auditactivity);
            props.setProperty("auditsignedflag", this.auditsignedflag);
            try {
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props, false);
            }
            catch (ActionException e) {
                ErrorHandler eh = e.getErrorHandler();
                if (eh == null || eh.size() <= 0) throw e;
                ErrorDetail error = (ErrorDetail)eh.get(0);
                throw new SapphireException(error.getErrorid(), error.getErrorType(), error.getMessage());
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private String getPackageReceiveDate(String packageid) {
        DataSet ds;
        if (!this.packageReceiveDateCache.containsKey(packageid) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select functiondt from sdiaddress where sdcid = 'LV_Package' and keyid1 = ? and contactfunction = 'ReceivedBy'", (Object[])new String[]{packageid})) != null && ds.size() > 0) {
            this.packageReceiveDateCache.put(packageid, ds.getValue(0, "functiondt", "n"));
        }
        return this.packageReceiveDateCache.get(packageid);
    }

    private boolean receiveSampleOnPackageReceive() throws SapphireException {
        if (this.biobankingPolicy == null) {
            this.biobankingPolicy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        }
        return "Y".equals(this.biobankingPolicy.getProperty("receivesampleonpackagereceive", "N"));
    }

    private boolean isStorageUnitActive(String storagelinksdcid, String storagelinkkeyid1) {
        if ("LV_Box".equals(storagelinksdcid)) {
            if (!this.boxActiveMap.containsKey(storagelinkkeyid1)) {
                this.boxActiveMap.put(storagelinkkeyid1, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_box", "activeflag", "s_boxid = ?", new String[]{storagelinkkeyid1}));
            }
            return "Y".equals(this.boxActiveMap.get(storagelinkkeyid1));
        }
        if ("Plate".equals(storagelinksdcid)) {
            if (!this.plateActiveMap.containsKey(storagelinkkeyid1)) {
                this.plateActiveMap.put(storagelinkkeyid1, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_plate", "activeflag", "s_plateid = ?", new String[]{storagelinkkeyid1}));
            }
            return "Y".equals(this.plateActiveMap.get(storagelinkkeyid1));
        }
        return false;
    }

    private String getStorageStatus(HashMap<String, String> confirm) {
        HashMap<String, String> search = new HashMap<String, String>();
        boolean TILs = false;
        boolean TIL = false;
        StringBuffer sb = new StringBuffer();
        if (confirm != null && confirm.size() > 0) {
            DataSet TILSamples = this.getTILSample(OpalUtil.toDelimitedString(confirm.keySet(), ";"));
            if (TILSamples != null && TILSamples.size() > 0) {
                TILs = true;
            }
            for (String s : confirm.keySet()) {
                String sample = s;
                String status = confirm.get(sample);
                if ("Allocated".equals(status)) {
                    sb.append("Received").append(";");
                    continue;
                }
                if (TILs) {
                    search.clear();
                    search.put("destsampleid", sample);
                    if (TILSamples.findRow(search) != -1) {
                        TIL = true;
                    }
                }
                if (TIL) {
                    sb.append("Temporary In Lab").append(";");
                    TIL = false;
                    continue;
                }
                sb.append("In Circulation").append(";");
            }
            return sb.substring(0, sb.length() - 1);
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DataSet getTILSample(String samples) {
        DataSet ds = null;
        StringBuffer sql = new StringBuffer();
        sql.append("select destsampleid");
        sql.append(" from s_sample s, s_samplemap smap");
        sql.append(" where s.s_sampleid = smap.sourcesampleid");
        sql.append(" and s.storagestatus = 'Temporary In Lab'");
        SafeSQL safeSQL = new SafeSQL();
        if (StringUtil.split(samples, ";").length > 750) {
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet("Sample", samples, null, null);
                sql.append(" and smap.destsampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
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
            sql.append(" and smap.destsampleid in (").append(safeSQL.addIn(samples, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds == null ? new DataSet() : ds;
    }

    private String getSDICustodialDepartment(String sdcid, String keyid1) {
        String key = sdcid + keyid1;
        if (!this.custodyMap.containsKey(key)) {
            this.populateCustodyMap(sdcid, keyid1);
        }
        return this.custodyMap.get(key).get("custodialdepartmentid");
    }

    private String getSDICustodialUser(String sdcid, String keyid1) {
        String key = sdcid + keyid1;
        if (!this.custodyMap.containsKey(key)) {
            this.populateCustodyMap(sdcid, keyid1);
        }
        return this.custodyMap.get(key).get("custodialuserid");
    }

    private void populateCustodyMap(String sdcid, String keyid1) {
        SafeSQL safeSQL = new SafeSQL();
        String key = sdcid + keyid1;
        StringBuilder sql = new StringBuilder();
        sql.append("select linksdcid, linkkeyid1, custodialuserid, custodialdepartmentid from trackitem");
        sql.append(" where linksdcid = ").append(safeSQL.addVar(sdcid));
        sql.append(" and linkkeyid1 = ").append(safeSQL.addVar(keyid1));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("linksdcid", ds.getValue(0, "linksdcid"));
            map.put("linkkeyid1", ds.getValue(0, "linkkeyid1"));
            map.put("custodialuserid", ds.getValue(0, "custodialuserid"));
            map.put("custodialdepartmentid", ds.getValue(0, "custodialdepartmentid"));
            this.custodyMap.put(key, map);
        } else {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("linksdcid", "");
            map.put("linkkeyid1", "");
            map.put("custodialuserid", "");
            map.put("custodialdepartmentid", "");
            this.custodyMap.put(key, map);
        }
    }

    private boolean isUserDepartmentMember(String userid, String custodialdepartmentid) {
        List departmentList;
        if (userid.equals(this.getConnectionProcessor().getSapphireConnection().getSysuserId())) {
            return this.getConnectionProcessor().getSapphireConnection().isDepartmentMember(custodialdepartmentid);
        }
        if (this.userDepartmentMap == null) {
            this.userDepartmentMap = new HashMap();
        }
        if (!this.userDepartmentMap.containsKey(userid)) {
            this.userDepartmentMap.put(userid, OpalUtil.getUserDepartments(this.getQueryProcessor(), userid));
        }
        return (departmentList = this.userDepartmentMap.get(userid)) != null && departmentList.contains(custodialdepartmentid);
    }

    private HashMap<String, String> getStorageUnitLinkedSDC(String storageunitid) {
        String parentid = null;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select parentid, linksdcid, linkkeyid1 from storageunit where storageunitid = " + safeSQL.addVar(storageunitid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String linksdcid = ds.getString(0, "linksdcid");
            parentid = ds.getString(0, "parentid");
            if (StringUtil.getLen(linksdcid) > 0L) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("linksdcid", ds.getString(0, "linksdcid"));
                map.put("linkkeyid1", ds.getString(0, "linkkeyid1"));
                return map;
            }
        }
        if (StringUtil.getLen(parentid) > 0L) {
            return this.getStorageUnitLinkedSDC(parentid);
        }
        return new HashMap<String, String>();
    }
}

