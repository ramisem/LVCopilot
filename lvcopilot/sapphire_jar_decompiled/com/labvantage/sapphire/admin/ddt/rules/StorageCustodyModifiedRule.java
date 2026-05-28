/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class StorageCustodyModifiedRule
extends BaseBioBankRule {
    private String tracelogid = "";
    private String auditreason = "";
    private String auditactivity = "";
    private String auditsignedflag = "";
    private Map<String, String> updateContentCustodyMap = new HashMap<String, String>();
    private Map<String, Map<String, String>> physicalStoreMap = new HashMap<String, Map<String, String>>();

    public StorageCustodyModifiedRule(DBAccess database, ConnectionInfo connectionInfo, String tracelogid, String auditreason, String auditactivity, String auditsignedflag) {
        super(database, connectionInfo);
        this.tracelogid = tracelogid;
        this.auditreason = auditreason;
        this.auditactivity = auditactivity;
        this.auditsignedflag = auditsignedflag;
    }

    @Override
    public String getRuleId() {
        return "StorageCustodyModifiedRule";
    }

    public void processRule(DataSet storageunitCollection) throws SapphireException {
        DataSet ds;
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        StringBuilder sql = new StringBuilder();
        sql.append("select storageunit.storageunitid, storageunit.linksdcid, storageunit.linkkeyid1, storageunit.propertytreeid, storageunit.storageunittype, storageunit.parentid,");
        sql.append(" trackitem.trackitemid, trackitem.custodialuserid, trackitem.custodialdepartmentid, trackitem.currentstorageunitid,");
        sql.append(" (select psu.linksdcid from storageunit psu where psu.storageunitid = storageunit.parentid) parentstoragesdcid,");
        sql.append(" (select s_physicalstore.departmentid from s_physicalstore where s_physicalstore.s_physicalstoreid = (select cs.linkkeyid1 from storageunit cs where cs.storageunitid = storageunit.parentid)) physicalstoredepartment,");
        sql.append(" (select s_physicalstore.storageclass from s_physicalstore where s_physicalstore.s_physicalstoreid = (select cs.linkkeyid1 from storageunit cs where cs.storageunitid = storageunit.parentid)) physicalstoreclass,");
        sql.append(" s_package.packagestatus thispackagestatus, s_package.senderdepartmentid thispackageorigination, s_package.recipientdepartmentid thispackagedestination,");
        sql.append(" (select s_package.packagestatus from s_package where s_package.s_packageid = (select cs.linkkeyid1 from storageunit cs where cs.storageunitid = storageunit.parentid)) packagestatus,");
        sql.append(" (select s_package.senderdepartmentid from s_package where s_package.s_packageid = (select cs.linkkeyid1 from storageunit cs where cs.storageunitid = storageunit.parentid)) packageorigination,");
        sql.append(" (select s_package.recipientdepartmentid from s_package where s_package.s_packageid = (select cs.linkkeyid1 from storageunit cs where cs.storageunitid = storageunit.parentid)) packagedestination");
        sql.append(" from storageunit left outer join trackitem on trackitem.linksdcid = storageunit.linksdcid and trackitem.linkkeyid1 = storageunit.linkkeyid1");
        sql.append(" left outer join s_package on s_package.s_packageid = storageunit.linkkeyid1");
        if (storageunitCollection.size() > 1000) {
            String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", storageunitCollection.getColumnValues("storageunitid", ";"), null, null);
            sql.append(" where storageunit.storageunitid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ? )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" where storageunit.storageunitid in ( ").append(safeSQL.addIn(storageunitCollection.getColumnValues("storageunitid", "','"))).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null && ds.size() > 0) {
            boolean receiveSamplesOnPackageReceive = "Y".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("receivesampleonpackagereceive", "N"));
            String currentuserid = this.connectionInfo.getSysuserId();
            String defaultdepartment = this.connectionInfo.getDefaultDepartment();
            DataSet updateDS = new DataSet();
            DataSet clearCurrentStorageUnitDS = new DataSet();
            DataSet clearPackageParentDS = new DataSet();
            ArrayList<String> receiveSampleList = new ArrayList<String>();
            for (int i = 0; i < ds.size(); ++i) {
                DataSet allTrackitems;
                String storageunitid = ds.getString(i, "storageunitid");
                String parentid = ds.getString(i, "parentid", "");
                String trackitemid = ds.getString(i, "trackitemid", "");
                String custodialuserid = ds.getString(i, "custodialuserid", "");
                String custodialdepartmentid = ds.getString(i, "custodialdepartmentid", "");
                String storageunittype = ds.getString(i, "storageunittype", "");
                String propertytreeid = ds.getString(i, "propertytreeid", "");
                String currenttracelogid = this.tracelogid;
                if (ds.size() == storageunitCollection.size()) {
                    for (int j = 0; j < storageunitCollection.size(); ++j) {
                        if (!storageunitid.equals(storageunitCollection.getString(j, "storageunitid"))) continue;
                        currenttracelogid = storageunitCollection.getString(j, "tracelogid");
                    }
                }
                String targetcustodialuserid = "";
                String targetcustodialdepartmentid = "";
                boolean isPackage = "LV_Package".equals(ds.getString(i, "linksdcid"));
                boolean isPackageShipped = false;
                boolean isPackageReceived = false;
                boolean clearCurrentStorageUnit = false;
                boolean syncCustodyFromParent = false;
                if (isPackage) {
                    String packageorigination = ds.getString(i, "thispackageorigination", "");
                    String packagedestination = ds.getString(i, "thispackagedestination", "");
                    String packagestatus = ds.getString(i, "thispackagestatus", "");
                    if ("Shipped".equals(packagestatus)) {
                        isPackageShipped = true;
                        targetcustodialuserid = "";
                        targetcustodialdepartmentid = "Transit";
                        if (OpalUtil.isNotEmpty(parentid)) {
                            int row = clearPackageParentDS.addRow();
                            clearPackageParentDS.setString(row, "storageunitid", storageunitid);
                            clearPackageParentDS.setString(row, "parentid", "(null)");
                        }
                    } else if ("Received".equals(packagestatus) || "On Hold".equals(packagestatus)) {
                        targetcustodialuserid = "";
                        targetcustodialdepartmentid = packagedestination;
                        isPackageReceived = true;
                    } else if ("Created".equals(packagestatus)) {
                        targetcustodialuserid = "";
                        targetcustodialdepartmentid = packageorigination;
                    } else if ("Cancelled".equals(packagestatus)) {
                        clearCurrentStorageUnit = true;
                        targetcustodialuserid = this.connectionInfo.getSysuserId();
                        targetcustodialdepartmentid = defaultdepartment;
                    } else if ("Emptied".equals(packagestatus)) {
                        clearCurrentStorageUnit = true;
                        targetcustodialuserid = "";
                        targetcustodialdepartmentid = packagedestination;
                    }
                } else if (OpalUtil.isEmpty(parentid)) {
                    targetcustodialuserid = custodialuserid;
                    targetcustodialdepartmentid = custodialdepartmentid;
                } else {
                    syncCustodyFromParent = true;
                    String parentstoragesdcid = ds.getString(i, "parentstoragesdcid", "");
                    if ("LV_Package".equals(parentstoragesdcid)) {
                        String packageorigination = ds.getString(i, "packageorigination", "");
                        String packagedestination = ds.getString(i, "packagedestination", "");
                        String packagestatus = ds.getString(i, "packagestatus", "");
                        if ("Shipped".equals(packagestatus)) {
                            targetcustodialuserid = "";
                            targetcustodialdepartmentid = "Transit";
                        } else if ("Received".equals(packagestatus) || "On Hold".equals(packagestatus)) {
                            targetcustodialuserid = "";
                            targetcustodialdepartmentid = packagedestination;
                        } else if ("Created".equals(packagestatus)) {
                            targetcustodialuserid = "";
                            targetcustodialdepartmentid = packageorigination;
                        } else if ("Cancelled".equals(packagestatus)) {
                            targetcustodialuserid = this.connectionInfo.getSysuserId();
                            targetcustodialdepartmentid = defaultdepartment;
                        } else if (packagestatus.equals("Emptied")) {
                            targetcustodialuserid = "";
                            targetcustodialdepartmentid = packagedestination;
                        }
                    } else if ("PhysicalStore".equals(parentstoragesdcid)) {
                        String physicalstoredepartment = ds.getString(i, "physicalstoredepartment", "");
                        if (OpalUtil.isEmpty(physicalstoredepartment)) {
                            if (this.updateContentCustody(propertytreeid, storageunittype)) {
                                throw new SapphireException(this.getTranslationProcessor().translate("Missing Department"), "VALIDATION", this.getTranslationProcessor().translate("Unable to file Storage unit. Physical Store is missing Department information.") + " (" + OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "linkkeyid1", "parentid = '" + parentid + "'") + ")");
                            }
                        } else {
                            targetcustodialdepartmentid = physicalstoredepartment;
                        }
                        targetcustodialuserid = !"Temporary".equals(ds.getString(i, "physicalstoreclass")) ? "" : currentuserid;
                    } else {
                        Map<String, String> psData = this.getPhysicalStoreData(parentid);
                        if (psData != null && psData.size() > 0) {
                            String physicalStoreDepartment;
                            String physicalStoreClass = psData.get("storageclass");
                            if (!"Temporary".equals(physicalStoreClass)) {
                                targetcustodialuserid = "";
                            }
                            if (OpalUtil.isEmpty(physicalStoreDepartment = psData.get("departmentid"))) {
                                if (this.updateContentCustody(propertytreeid, storageunittype)) {
                                    throw new SapphireException(this.getTranslationProcessor().translate("Missing Department"), "VALIDATION", this.getTranslationProcessor().translate("Unable to file Storage unit. Physical Store is missing Department information.") + " (" + OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "linkkeyid1", "parentid = '" + parentid + "'") + ")");
                                }
                            } else {
                                targetcustodialdepartmentid = physicalStoreDepartment;
                            }
                        }
                    }
                }
                if (OpalUtil.isNotEmpty(trackitemid)) {
                    if (!isPackage) {
                        if (syncCustodyFromParent && !targetcustodialuserid.equals(custodialuserid)) {
                            int row = updateDS.addRow();
                            updateDS.setString(row, "trackitemid", trackitemid);
                            updateDS.setString(row, "custodialuserid", OpalUtil.isEmpty(targetcustodialuserid) ? "(null)" : targetcustodialuserid);
                            updateDS.setString(row, "custodialdepartmentid", targetcustodialdepartmentid);
                            updateDS.setString(row, "tracelogid", currenttracelogid);
                        }
                    } else if (isPackageShipped) {
                        int row = clearCurrentStorageUnitDS.addRow();
                        clearCurrentStorageUnitDS.setString(row, "trackitemid", trackitemid);
                        clearCurrentStorageUnitDS.setString(row, "currentstorageunitid", "(null)");
                        clearCurrentStorageUnitDS.setString(row, "custodialuserid", "(null)");
                        clearCurrentStorageUnitDS.setString(row, "custodialdepartmentid", targetcustodialdepartmentid);
                    }
                }
                if (!this.updateContentCustody(propertytreeid, storageunittype) || (allTrackitems = StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), storageunitid, this.connectionInfo.isOracle())) == null || allTrackitems.size() <= 0) continue;
                for (int j = 0; j < allTrackitems.size(); ++j) {
                    storageunittype = allTrackitems.getString(i, "storageunittype", "");
                    propertytreeid = allTrackitems.getString(i, "propertytreeid", "");
                    String parentstorageunittype = allTrackitems.getString(i, "parentstorageunittype", "");
                    String parentpropertytreeid = allTrackitems.getString(i, "parentpropertytreeid", "");
                    boolean updateContentCustody = this.updateContentCustody(propertytreeid, storageunittype);
                    if (updateContentCustody) {
                        updateContentCustody = this.updateContentCustody(parentpropertytreeid, parentstorageunittype);
                    }
                    if (!updateContentCustody) continue;
                    String ticustodialuserid = allTrackitems.getString(j, "custodialuserid", "");
                    String ticustodialdepartmentid = allTrackitems.getString(j, "custodialdepartmentid", "");
                    if (isPackage) {
                        String storagestatus;
                        String sampleid;
                        if (clearCurrentStorageUnit) {
                            if (!targetcustodialuserid.equals(ticustodialuserid) || !targetcustodialdepartmentid.equals(ticustodialdepartmentid)) {
                                int row = clearCurrentStorageUnitDS.addRow();
                                clearCurrentStorageUnitDS.setString(row, "trackitemid", allTrackitems.getString(j, "trackitemid"));
                                clearCurrentStorageUnitDS.setString(row, "currentstorageunitid", "(null)");
                                clearCurrentStorageUnitDS.setString(row, "custodialuserid", OpalUtil.isEmpty(targetcustodialuserid) ? "(null)" : targetcustodialuserid);
                                clearCurrentStorageUnitDS.setString(row, "custodialdepartmentid", targetcustodialdepartmentid);
                            }
                        } else if (!targetcustodialuserid.equals(ticustodialuserid) || !targetcustodialdepartmentid.equals(ticustodialdepartmentid)) {
                            int row = updateDS.addRow();
                            updateDS.setString(row, "trackitemid", allTrackitems.getString(j, "trackitemid"));
                            updateDS.setString(row, "custodialuserid", OpalUtil.isEmpty(targetcustodialuserid) ? "(null)" : targetcustodialuserid);
                            updateDS.setString(row, "custodialdepartmentid", targetcustodialdepartmentid);
                            updateDS.setString(row, "tracelogid", currenttracelogid);
                        }
                        if (!isPackageReceived || !OpalUtil.isNotEmpty(sampleid = allTrackitems.getString(j, "s_sampleid")) || !"Allocated".equals(storagestatus = allTrackitems.getString(j, "storagestatus", "")) || !receiveSamplesOnPackageReceive) continue;
                        receiveSampleList.add(sampleid);
                        continue;
                    }
                    if (targetcustodialuserid.equals(ticustodialuserid) && targetcustodialdepartmentid.equals(ticustodialdepartmentid)) continue;
                    int row = updateDS.addRow();
                    updateDS.setString(row, "trackitemid", allTrackitems.getString(j, "trackitemid"));
                    updateDS.setString(row, "custodialuserid", OpalUtil.isEmpty(targetcustodialuserid) ? "(null)" : targetcustodialuserid);
                    updateDS.setString(row, "custodialdepartmentid", targetcustodialdepartmentid);
                    updateDS.setString(row, "tracelogid", currenttracelogid);
                }
            }
            PropertyList props = new PropertyList();
            if (updateDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", updateDS.getColumnValues("trackitemid", ";"));
                props.setProperty("custodialuserid", updateDS.getColumnValues("custodialuserid", ";"));
                props.setProperty("custodialdepartmentid", updateDS.getColumnValues("custodialdepartmentid", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleignore", "Y");
                props.setProperty("tracelogid", updateDS.getColumnValues("tracelogid", ";"));
                props.setProperty("auditreason", this.auditreason);
                props.setProperty("auditactivity", this.auditactivity);
                props.setProperty("auditsignedflag", this.auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (clearCurrentStorageUnitDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", clearCurrentStorageUnitDS.getColumnValues("trackitemid", ";"));
                props.setProperty("currentstorageunitid", clearCurrentStorageUnitDS.getColumnValues("currentstorageunitid", ";"));
                props.setProperty("custodialuserid", clearCurrentStorageUnitDS.getColumnValues("custodialuserid", ";"));
                props.setProperty("custodialdepartmentid", clearCurrentStorageUnitDS.getColumnValues("custodialdepartmentid", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleignore", "Y");
                props.setProperty("tracelogid", this.tracelogid);
                props.setProperty("auditreason", this.auditreason);
                props.setProperty("auditactivity", this.auditactivity);
                props.setProperty("auditsignedflag", this.auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (clearPackageParentDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", clearPackageParentDS.getColumnValues("storageunitid", ";"));
                props.setProperty("parentid", clearPackageParentDS.getColumnValues("parentid", ";"));
                props.setProperty("__syncoperation", "Y");
                props.setProperty("tracelogid", this.tracelogid);
                props.setProperty("auditreason", this.auditreason);
                props.setProperty("auditactivity", this.auditactivity);
                props.setProperty("auditsignedflag", this.auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (receiveSampleList.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(receiveSampleList, ";"));
                props.setProperty("storagestatus", "Received");
                props.setProperty("previousstoragestatus", "Allocated");
                props.setProperty("samplestatus", "Received");
                props.setProperty("confirmedby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                props.setProperty("confirmeddt", "n");
                props.setProperty("receivedby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                props.setProperty("receiveddt", "n");
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("__samplePreEditRuleIgnore", "Y");
                props.setProperty("__samplePostEditRuleIgnore", "Y");
                props.setProperty("tracelogid", this.tracelogid);
                props.setProperty("auditreason", this.auditreason);
                props.setProperty("auditactivity", this.auditactivity);
                props.setProperty("auditsignedflag", this.auditsignedflag);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private boolean updateContentCustody(String propertytreeid, String storageunittype) {
        String key = propertytreeid + storageunittype;
        if (!this.updateContentCustodyMap.containsKey(key)) {
            try {
                PropertyList propertyList = StorageUnitUtil.getDefinition(new WebAdminProcessor(this.getConnectionProcessor().getConnectionid()), storageunittype, propertytreeid);
                this.updateContentCustodyMap.put(key, propertyList.getProperty("updatecontentcustody", "Y"));
            }
            catch (Exception e) {
                this.updateContentCustodyMap.put(key, "Y");
            }
        }
        return "Y".equals(this.updateContentCustodyMap.get(key));
    }

    private Map<String, String> getPhysicalStoreData(String storageunitid) {
        if (OpalUtil.isNotEmpty(storageunitid) && !this.physicalStoreMap.containsKey(storageunitid)) {
            DataSet ds;
            String sql;
            HashMap<String, String> psData = new HashMap<String, String>();
            if (this.getConnectionProcessor().isOra()) {
                sql = "select storageunit.storageunitid, storageunit.linksdcid, s_physicalstore.s_physicalstoreid, s_physicalstore.departmentid, s_physicalstore.storageclass from storageunit left outer join s_physicalstore on s_physicalstore.s_physicalstoreid = storageunit.linkkeyid1 and storageunit.linksdcid = 'PhysicalStore' connect by prior storageunit.parentid = storageunit.storageunitid start with storageunit.storageunitid = ?";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
            } else {
                sql = "WITH StorageUnitTree (storageunitid, linksdcid, linkkeyid1, parentid) AS (    SELECT su.storageunitid, su.linksdcid, su.linkkeyid1, su.parentid    FROM storageunit AS su    WHERE su.storageunitid = ?    UNION ALL    SELECT su.storageunitid, su.linksdcid, su.linkkeyid1, su.parentid   FROM storageunit AS su    INNER JOIN StorageUnitTree AS d    ON d.parentid = su.storageunitid ) select storageunittree.storageunitid, storageunittree.linksdcid, s_physicalstore.s_physicalstoreid, s_physicalstore.departmentid, s_physicalstore.storageclass from StorageUnitTree left outer join s_physicalstore on s_physicalstore.s_physicalstoreid = storageunittree.linkkeyid1 and storageunittree.linksdcid = 'PhysicalStore'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    if (!"PhysicalStore".equals(ds.getString(i, "linksdcid"))) continue;
                    psData.put("s_physicalstoreid", ds.getString(i, "s_physicalstoreid", ""));
                    psData.put("departmentid", ds.getString(i, "departmentid", ""));
                    psData.put("storageclass", ds.getString(i, "storageclass", ""));
                    break;
                }
            }
            this.physicalStoreMap.put(storageunitid, psData);
        }
        return this.physicalStoreMap.get(storageunitid);
    }
}

