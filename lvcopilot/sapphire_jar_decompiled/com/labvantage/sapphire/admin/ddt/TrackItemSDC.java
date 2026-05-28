/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.opal.validation.pkg.PackageValidate;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDISecurityDept;
import com.labvantage.sapphire.actions.sdi.AddSDISecuritySet;
import com.labvantage.sapphire.actions.sdi.DeleteSDISecurityDep;
import com.labvantage.sapphire.actions.sdi.DeleteSDISecuritySet;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.LV_Box;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BoxReservationFlushRule;
import com.labvantage.sapphire.admin.ddt.rules.EventLog;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.admin.ddt.rules.PackageCanReceiveSampleRule;
import com.labvantage.sapphire.admin.ddt.rules.SampleStateRule;
import com.labvantage.sapphire.admin.ddt.rules.StorageCustodyModifiedRule;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.array.ArrayUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TrackItemSDC
extends BaseSDCRules {
    public String LABVANTAGE_CVS_ID = "$Revision: 104200 $";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String SDCID = "TrackItemSDC";
    public static final String COLUMN_TRACKITEMID = "trackitemid";
    public static final String COLUMN_CURRENTSTORAGEUNITID = "currentstorageunitid";
    public static final String COLUMN_LINKSDCID = "linksdcid";
    public static final String COLUMN_LINKKEYID1 = "linkkeyid1";
    public static final String COLUMN_LINKKEYID2 = "linkkeyid2";
    public static final String COLUMN_LINKKEYID3 = "linkkeyid3";
    public static final String COLUMN_STORAGEUNITTYPE = "storageunittype";
    public static final String COLUMN_STORAGEUNITLABEL = "storageunitlabel";
    public static final String COLUMN_PARENTID = "parentid";
    public static final String COLUMN_CUSTODIALDEPARTMENTID = "custodialdepartmentid";
    public static final String COLUMN_CUSTODIALUSERID = "custodialuserid";
    public static final String COLUMN_QTYCURRENT = "qtycurrent";
    public static final String COLUMN_QTYUNIT = "qtyunits";
    public static final String COLUMN_QTYCURRENTTYPE = "qtycurrenttype";
    public static final String COLUMN_CONTAINERTYPEID = "containertypeid";
    public static final String COLUMN_CONTAINERSIZE = "sizevalue";
    public static final String COLUMN_CONTAINERUNITS = "sizeunits";
    public static final String COLUMN_TRACKITEMSTATUS = "trackitemstatus";
    public static final String COLUMN_USECOUNT = "usecount";
    public static final String COLUMN_EXPIRYDT = "expirydt";
    public static final String COLUMN_FIRTSUSEDDT = "firstusedt";
    public static final String COLUMN_REAGENT_TYPE = "reagenttypeid";
    public static final String COLUMN_REAGENT_TYPE_VERSION = "reagenttypeversionid";
    public static final String OP_EDIT = "Edit";
    public static final String OP_ADD = "Add";
    public static final String OP_DELETE = "Delete";
    public static final String REORDER_NOTIFICATION = "ReorderNotification";
    public static final String TRANSIT = "Transit";
    private HashMap storageUnitMap = null;
    private boolean ASL = false;
    private Map<String, String> storageNameCache = new HashMap<String, String>();
    HashMap kitstudyaliasmap = new HashMap();
    HashMap deptmap = new HashMap();
    private HashMap deptrepomap = new HashMap();
    private Map<String, Map<String, String>> packagemap = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> suinfomap = new HashMap<String, Map<String, String>>();
    private HashMap boxCDMap = new HashMap();
    private HashMap storagenodebysdcmap = new HashMap();
    private HashMap linkmap = new HashMap();
    private HashMap psrepomap = new HashMap();
    private HashMap userDepartments = null;
    private HashMap targetstoragemap = new HashMap();
    private HashMap sdcnamemap = new HashMap();
    private Map<String, Map<String, String>> departmentCache = new HashMap<String, Map<String, String>>();
    private HashMap<String, DataSet> validateUnitsCache = new HashMap();
    private HashMap<String, DataSet> reagentLotCache = new HashMap();
    private HashMap<String, DataSet> thresoldDetailsByReagentTypeCache = new HashMap();
    private HashMap<String, DataSet> freezThawCach = new HashMap();
    private String specialDelimer = "^^^";

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (YES.equals(actionProps.getProperty("__sdcruleignore"))) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: TrackItemSDC.preEdit() [" + start + "]");
        DataSet primary = sdiData.getDataset("primary");
        this.validateStorageRestrictions(primary);
        this.ASL = this.connectionInfo.hasModule("ASL");
        if (primary.isValidColumn(COLUMN_CURRENTSTORAGEUNITID)) {
            DataSet move = new DataSet();
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID)) continue;
                String value = primary.getValue(i, COLUMN_CURRENTSTORAGEUNITID);
                if (value != null && value.trim().length() == 0) {
                    primary.setValue(i, COLUMN_CURRENTSTORAGEUNITID, "");
                }
                move.copyRow(primary, i, 1);
            }
            if (!YES.equals(actionProps.getProperty("__bypasscustodyrules"))) {
                DataSet dsStudyTrackItems = new DataSet();
                if (primary.size() <= 750) {
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer sql = new StringBuffer("select sp.studyid, sct.trackitemid from study_scheduleplan sp, schedulecondition_trackitem sct");
                    sql.append(" where sp.scheduleplanid = sct.scheduleplanid and sct.trackitemid  in (").append(safeSQL.addIn(primary.getColumnValues(COLUMN_TRACKITEMID, "','"))).append(")");
                    dsStudyTrackItems = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid = this.getDAMProcessor().createRSet(SDCID, primary.getColumnValues(COLUMN_TRACKITEMID, ";"), null, null);
                    dsStudyTrackItems = this.getQueryProcessor().getPreparedSqlDataSet("select sp.studyid, sct.trackitemid from study_scheduleplan sp, schedulecondition_trackitem sct, rsetitems  where sp.scheduleplanid = sct.scheduleplanid and sct.trackitemid = rsetitems.keyid1 and rsetitems.rsetid = ?", (Object[])new String[]{rsetid});
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
                this.validateMovement(move, YES.equals(actionProps.getProperty("__sdcruleconfirm")), dsStudyTrackItems);
            }
        }
        SDIData before = this.getBeforeEditImage();
        DataSet oldPrimary = before.getDataset("primary");
        QueryProcessor qp = this.getQueryProcessor();
        HashMap trackitemdata = this.getTrackItemData(primary);
        boolean addColumns = true;
        BigDecimal bigDecimalOne = new BigDecimal(1.0);
        for (int i = 0; i < primary.size(); ++i) {
            BigDecimal bd;
            String trackitemid = primary.getValue(i, COLUMN_TRACKITEMID);
            if (!trackitemdata.containsKey(trackitemid)) continue;
            HashMap map = (HashMap)trackitemdata.get(trackitemid);
            String linkSDC = (String)map.get(COLUMN_LINKSDCID);
            if (linkSDC == null) {
                linkSDC = "";
            }
            if ("LV_ReagentLot".equals(linkSDC)) {
                this.validateTrackitemStatus(primary, i, map);
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_QTYUNIT)) {
                    String qtyunits = primary.getString(i, COLUMN_QTYUNIT, "");
                    if (qtyunits.equalsIgnoreCase("(Containers)")) {
                        primary.setString(i, COLUMN_QTYUNIT, "");
                        primary.setString(i, COLUMN_QTYCURRENTTYPE, "C");
                    } else if (qtyunits.length() > 0) {
                        primary.setString(i, COLUMN_QTYCURRENTTYPE, "U");
                    }
                    String error = this.validateUnit(primary, i, false);
                    if (error.length() > 0) {
                        throw new SapphireException(error);
                    }
                }
                String reagentLotId = (String)map.get(COLUMN_LINKKEYID1);
                String trackItemStatus = (String)map.get(COLUMN_TRACKITEMSTATUS);
                if ("Valid".equals(trackItemStatus)) {
                    if (primary.size() > 1 && addColumns) {
                        if (!primary.isValidColumn(COLUMN_EXPIRYDT)) {
                            primary.addColumnValues(COLUMN_EXPIRYDT, oldPrimary.getColumnType(COLUMN_EXPIRYDT), oldPrimary.getColumnValues(COLUMN_EXPIRYDT, ";"), ";");
                        }
                        if (!primary.isValidColumn(COLUMN_USECOUNT)) {
                            primary.addColumnValues(COLUMN_USECOUNT, oldPrimary.getColumnType(COLUMN_USECOUNT), oldPrimary.getColumnValues(COLUMN_USECOUNT, ";"), ";");
                        }
                        if (!primary.isValidColumn(COLUMN_TRACKITEMSTATUS)) {
                            primary.addColumnValues(COLUMN_TRACKITEMSTATUS, oldPrimary.getColumnType(COLUMN_TRACKITEMSTATUS), oldPrimary.getColumnValues(COLUMN_TRACKITEMSTATUS, ";"), ";");
                        }
                        addColumns = false;
                    }
                    this.updateUseCountAndStatus(qp, primary, oldPrimary, i, reagentLotId);
                }
            }
            if ("Batch".equals(linkSDC) || linkSDC.length() == 0) {
                boolean linkKeyNull = linkSDC.length() == 0;
                this.updateTrackItemStatus(qp, primary, oldPrimary, i, linkKeyNull);
            }
            if (!this.getConnectionProcessor().isMSS() || !this.hasPrimaryValueChanged(primary, i, COLUMN_QTYCURRENT) || (bd = primary.getBigDecimal(i, COLUMN_QTYCURRENT)) == null) continue;
            primary.setNumber(i, COLUMN_QTYCURRENT, bd.divide(bigDecimalOne, 10, 4));
        }
        String specimentype = actionProps.getProperty("specimentype");
        if (StringUtil.getLen(specimentype) > 0L) {
            String[] specimen = StringUtil.split(specimentype, ";");
            if (primary.size() == specimen.length) {
                for (int i = 0; i < primary.size(); ++i) {
                    String specimentypeid = specimen[i];
                    if (StringUtil.getLen(specimentypeid) <= 0L) continue;
                    primary.setString(i, COLUMN_CONTAINERTYPEID, specimentypeid);
                }
            }
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                DataSet dsReason = new DataSet();
                for (int i = 0; i < primary.size(); ++i) {
                    String reason = "DataEdit";
                    String linksdcid = this.getOldPrimaryValue(primary, i, COLUMN_LINKSDCID);
                    String sdcdisplayname = OpalUtil.isNotEmpty(linksdcid) ? this.getSDCProcessor().getProperty(linksdcid, "singular") : "TrackItem";
                    String custodialuserid = primary.getString(i, COLUMN_CUSTODIALUSERID, "");
                    if (this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID)) {
                        String currentstorageunitid = primary.getString(i, COLUMN_CURRENTSTORAGEUNITID, "");
                        reason = currentstorageunitid.length() > 0 ? sdcdisplayname + " filed in " + this.getStorageDisplayName(currentstorageunitid) : (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID) && custodialuserid.length() > 0 ? "User took custody of " + sdcdisplayname : sdcdisplayname + " removed from " + this.getStorageDisplayName(currentstorageunitid));
                    } else if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID)) {
                        reason = custodialuserid.length() > 0 ? "User took custody of " + sdcdisplayname : sdcdisplayname + " removed from the custody of User";
                    } else if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) {
                        reason = primary.getString(i, COLUMN_CUSTODIALDEPARTMENTID, "").length() > 0 ? sdcdisplayname + " moved in department" : sdcdisplayname + " removed from department";
                    }
                    int row = dsReason.addRow();
                    dsReason.setString(row, COLUMN_TRACKITEMID, primary.getString(i, COLUMN_TRACKITEMID));
                    dsReason.setString(row, "reason", reason);
                }
                int tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(SDCID, dsReason.getColumnValues(COLUMN_TRACKITEMID, ";"), null, null, dsReason.getColumnValues("reason", ";"), "", NO, "n", "Data editing", true));
                actionProps.setProperty("tracelogid", String.valueOf(tracelogid));
                for (int i = 0; i < primary.size(); ++i) {
                    primary.setString(i, "tracelogid", String.valueOf(tracelogid + i));
                }
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add tracelog entry", e);
            }
        }
        Trace.logInfo("END: TrackItemSDC.preEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private String getStorageDisplayName(String storageunitid) {
        String storagesdcname = "Storage";
        if (OpalUtil.isEmpty(storageunitid)) {
            return storagesdcname;
        }
        if (!this.storageNameCache.containsKey(storageunitid)) {
            String storagesdcid;
            DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select (case when linksdcid is null then (select p.linksdcid from storageunit p where p.storageunitid = storageunit.parentid) else linksdcid end) storagesdcid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
            if (OpalUtil.isNotEmpty(dataSet) && (storagesdcid = dataSet.getString(0, "storagesdcid", "")).length() > 0) {
                storagesdcname = this.getSDCProcessor().getProperty(storagesdcid, "singular");
            }
            this.storageNameCache.put(storageunitid, storagesdcname);
        }
        return this.storageNameCache.get(storageunitid);
    }

    private void updateTrackItemStatus(QueryProcessor qp, DataSet primary, DataSet oldPrimary, int row, boolean linkkeyNull) {
        char decimalSeparator;
        String newCurrentQty = TrackItemSDC.getPrimaryValue(qp, primary, row, COLUMN_QTYCURRENT);
        if (newCurrentQty != null && newCurrentQty.trim().length() > 0 && Double.parseDouble(newCurrentQty.replace(decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator(), '.')) <= 0.0) {
            if (primary.size() > 1 && !primary.isValidColumn(COLUMN_TRACKITEMSTATUS)) {
                this.populateColumnValue(primary, oldPrimary, COLUMN_TRACKITEMSTATUS);
            }
            primary.setString(row, COLUMN_TRACKITEMSTATUS, "Depleted");
            if (linkkeyNull) {
                if (primary.size() > 1 && !primary.isValidColumn(COLUMN_CURRENTSTORAGEUNITID)) {
                    this.populateColumnValue(primary, oldPrimary, COLUMN_CURRENTSTORAGEUNITID);
                }
                primary.setString(row, COLUMN_CURRENTSTORAGEUNITID, "");
            }
        }
    }

    private void validateStorageRestrictions(DataSet primary) throws SapphireException {
        DataSet ds = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String storageunitid;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID) || StringUtil.getLen(storageunitid = primary.getString(i, COLUMN_CURRENTSTORAGEUNITID, "")) <= 0L) continue;
            int row = ds.addRow();
            ds.setString(row, "storageunitid", storageunitid);
            ds.setString(row, COLUMN_TRACKITEMID, primary.getString(i, COLUMN_TRACKITEMID));
        }
        if (ds.size() > 0) {
            ds.sort("storageunitid");
            ArrayList<String> errors = new ArrayList<String>();
            ArrayList<DataSet> list = ds.getGroupedDataSets("storageunitid");
            Iterator<DataSet> iterator = list.iterator();
            while (iterator.hasNext()) {
                DataSet o;
                DataSet d = o = iterator.next();
                String storageunitid = d.getString(0, "storageunitid");
                errors.addAll(StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), storageunitid, d.getColumnValues(COLUMN_TRACKITEMID, ";"), this.getConnectionProcessor().getSapphireConnection()));
            }
            if (errors.size() > 0) {
                throw new SapphireException("Storage Restrictions - Found " + errors.size() + " restriction failures", "VALIDATION", OpalUtil.toDelimitedString(errors, ", "));
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        long start = System.currentTimeMillis();
        Trace.logInfo("START: TrackItemSDC.postEdit() [" + start + "]");
        DataSet primary = sdiData.getDataset("primary");
        this.processSDCSecurity(primary, actionProps);
        boolean syncSpecimenType = false;
        DataSet syncSecurityDepartment = new DataSet();
        DataSet removeSDISecurityDepartment = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String sdcid;
            String newdepartmentid;
            if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID) && !TRANSIT.equals(newdepartmentid = primary.getString(i, COLUMN_CUSTODIALDEPARTMENTID, "")) && OpalUtil.isNotEmpty(sdcid = this.getOldPrimaryValue(primary, i, COLUMN_LINKSDCID)) && "D".equals(this.getSDCProcessor().getProperty(sdcid, "accesscontrolledflag"))) {
                String linkkeyid1 = this.getOldPrimaryValue(primary, i, COLUMN_LINKKEYID1);
                if (StringUtil.getLen(sdcid) > 0L && StringUtil.getLen(linkkeyid1) > 0L) {
                    int row = syncSecurityDepartment.addRow();
                    syncSecurityDepartment.setString(row, "sdcid", sdcid);
                    syncSecurityDepartment.setString(row, "keyid1", linkkeyid1);
                    syncSecurityDepartment.setString(row, "securitydepartment", newdepartmentid);
                    syncSecurityDepartment.setString(row, "securityuser", this.getOldPrimaryValue(primary, i, "securityuser"));
                    if (this.database.checkPreparedExists("select keyid1 from sdisecuritydepartment where securitydepartment = ? and sdcid = ? and keyid1 = ?", new Object[]{newdepartmentid, sdcid, linkkeyid1})) {
                        row = removeSDISecurityDepartment.addRow();
                        removeSDISecurityDepartment.setString(row, "sdcid", sdcid);
                        removeSDISecurityDepartment.setString(row, "keyid1", linkkeyid1);
                        removeSDISecurityDepartment.setString(row, "securitydepartment", newdepartmentid);
                    }
                }
            }
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CONTAINERTYPEID)) continue;
            syncSpecimenType = true;
        }
        if (syncSpecimenType) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s.s_sampleid, s.specimentype, t.containertypeid");
            sql.append(" from s_sample s, trackitem t");
            sql.append(" where t.linksdcid = 'Sample'");
            sql.append(" and s.s_sampleid = t.linkkeyid1");
            sql.append(" and t.trackitemid in ([])");
            DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDCID, sql.toString(), primary.getColumnValues(COLUMN_TRACKITEMID, ";"));
            if (ds != null && ds.size() > 0) {
                DataSet updateds = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    String containertypeid;
                    String specimentype = ds.getString(i, "specimentype", "");
                    if (specimentype.equals(containertypeid = ds.getString(i, COLUMN_CONTAINERTYPEID, ""))) continue;
                    int row = updateds.addRow();
                    updateds.setString(row, "keyid1", ds.getString(i, "s_sampleid"));
                    updateds.setString(row, "specimentype", containertypeid);
                }
                if (updateds.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", updateds.getColumnValues("keyid1", ";"));
                    props.setProperty("specimentype", updateds.getColumnValues("specimentype", ";"));
                    props.setProperty("auditreason", "Synching Container Type with Specimen Type");
                    props.setProperty("__samplePreEditRuleIgnore", YES);
                    props.setProperty("__samplePostEditRuleIgnore", YES);
                    props.setProperty("__sdcruleconfirm", YES);
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
        }
        if (syncSecurityDepartment.size() > 0) {
            syncSecurityDepartment.sort("sdcid");
            ArrayList<DataSet> list = syncSecurityDepartment.getGroupedDataSets("sdcid");
            for (DataSet dataSet : list) {
                String sdcid;
                if (dataSet.size() <= 0 || (sdcid = dataSet.getString(0, "sdcid", "")).length() <= 0) continue;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dataSet.getColumnValues("keyid1", ";"));
                props.setProperty("securitydepartment", dataSet.getColumnValues("securitydepartment", ";"));
                props.setProperty("__securitydepartmentedit", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                if (!"D".equals(this.getSDCProcessor().getProperty("StorageUnitSDC", "accesscontrolledflag")) || this.database.getPreparedCount("select count(linksdcid) from storageunit where linksdcid = ?", new Object[]{sdcid}) <= 0) continue;
                String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
                String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), sdcid, "select su.storageunitid, su.linkkeyid1, p.securitydepartment from storageunit su, " + tableid + " p where su.linksdcid = '" + sdcid + "' and su.linkkeyid1 = p." + keycolid1 + " and p." + keycolid1 + " in ([])", dataSet.getColumnValues("keyid1", ";"));
                if (ds == null || ds.size() <= 0) continue;
                props.clear();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                props.setProperty("securitydepartment", ds.getColumnValues("securitydepartment", ";"));
                props.setProperty("__securitydepartmentedit", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (removeSDISecurityDepartment.size() > 0) {
            removeSDISecurityDepartment.sort("sdcid");
            ArrayList<DataSet> list = removeSDISecurityDepartment.getGroupedDataSets("sdcid");
            for (DataSet dataSet : list) {
                String sdcid;
                if (dataSet.size() <= 0 || (sdcid = dataSet.getString(0, "sdcid", "")).length() <= 0) continue;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dataSet.getColumnValues("keyid1", ";"));
                props.setProperty("departmentid", dataSet.getColumnValues("securitydepartment", ";"));
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(DeleteSDISecurityDep.class.getName(), props);
            }
        }
        HashMap trackitemdata = null;
        if (YES.equals(actionProps.getProperty("__sdcruleignore"))) {
            try {
                if (primary.isValidColumn(COLUMN_CUSTODIALUSERID) || primary.isValidColumn(COLUMN_CUSTODIALDEPARTMENTID)) {
                    trackitemdata = this.getTrackItemData(primary);
                    this.processEventLogRule(primary, trackitemdata);
                }
            }
            catch (Exception e) {
                this.logger.error("[Event Logging Error] An error occured while logging events for Trackitem(s) (" + primary.getColumnValues(COLUMN_TRACKITEMID, ";") + "): " + e.getMessage(), e);
            }
            Trace.logInfo("END: TrackItemSDC.postEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms. __sdcruleignore is Yes.");
            return;
        }
        if (trackitemdata == null) {
            trackitemdata = this.getTrackItemData(primary);
        }
        boolean sampleOnlyFlag = true;
        this.ASL = this.connectionInfo.hasModule("ASL");
        boolean forceUpdate = YES.equals(actionProps.getProperty("__sdcruleconfirm"));
        boolean hasCurrentStorageUnitChanged = false;
        DataSet storageParentModifiedSet = new DataSet();
        DataSet storageCustodyModifiedSet = new DataSet();
        HashSet<String> storedArraySet = new HashSet<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String linkstorageunitid;
            int suticount;
            String trackitemid = primary.getString(i, COLUMN_TRACKITEMID, "");
            if (!trackitemdata.containsKey(trackitemid)) continue;
            hasCurrentStorageUnitChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID);
            HashMap datamap = (HashMap)trackitemdata.get(trackitemid);
            int maxtiallowed = (Integer)datamap.get("maxtiallowed");
            if (maxtiallowed != -1 && (suticount = ((Integer)datamap.get("suticount")).intValue()) > maxtiallowed) {
                String storageunitid = (String)datamap.get(COLUMN_CURRENTSTORAGEUNITID);
                throw new SapphireException(this.getTranslationProcessor().translate("MaxTrackItemAllowedRule"), "VALIDATION", this.getTranslationProcessor().translate("No space available in") + " " + StorageUnitSDC.getStorageUnitSDIInfo(this.getQueryProcessor(), storageunitid));
            }
            String linkSDC = (String)datamap.get(COLUMN_LINKSDCID);
            if ("LV_ReagentLot".equals(linkSDC)) {
                this.validateTrackitemStatus(primary, i, datamap);
                String reagentLotId = (String)datamap.get(COLUMN_LINKKEYID1);
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_QTYCURRENT) || this.hasPrimaryValueChanged(primary, i, COLUMN_QTYUNIT) || this.hasPrimaryValueChanged(primary, i, COLUMN_TRACKITEMSTATUS)) {
                    String currDepletedTI = null;
                    if (this.isCurrentTrackItemDepleted(primary, i)) {
                        currDepletedTI = primary.getString(i, COLUMN_TRACKITEMID, "");
                    }
                    this.checkReagentThreshold(reagentLotId, OP_EDIT, null, currDepletedTI);
                }
                this.checkIfReagentLotDepleted(primary, i, reagentLotId);
            }
            if (sampleOnlyFlag && !"Sample".equals(linkSDC)) {
                sampleOnlyFlag = false;
            }
            if (OpalUtil.isNotEmpty(linkstorageunitid = (String)datamap.get("linkstorageunitid"))) {
                if (hasCurrentStorageUnitChanged) {
                    int row = storageParentModifiedSet.addRow();
                    storageParentModifiedSet.setString(row, "storageunitid", linkstorageunitid);
                    storageParentModifiedSet.setString(row, COLUMN_PARENTID, primary.getString(i, COLUMN_CURRENTSTORAGEUNITID, "(null)"));
                    storageParentModifiedSet.setString(row, "tracelogid", primary.getString(i, "tracelogid", ""));
                } else if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID) || this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) {
                    int row = storageCustodyModifiedSet.addRow();
                    storageCustodyModifiedSet.setString(row, "storageunitid", linkstorageunitid);
                }
            }
            if (!"LV_Array".equals(linkSDC) || !this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID) || primary.getValue(i, COLUMN_CURRENTSTORAGEUNITID, "").length() <= 0) continue;
            storedArraySet.add((String)datamap.get(COLUMN_LINKKEYID1));
        }
        if (hasCurrentStorageUnitChanged) {
            this.updateSpaceAvailableAndStatus(primary, null, actionProps);
        }
        if (storedArraySet.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Array");
            props.setProperty("keyid1", OpalUtil.toDelimitedString(storedArraySet, ";"));
            props.setProperty("arraystatus", "Stored");
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (storageParentModifiedSet.size() > 0) {
            try {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", storageParentModifiedSet.getColumnValues("storageunitid", ";"));
                props.setProperty(COLUMN_PARENTID, storageParentModifiedSet.getColumnValues(COLUMN_PARENTID, ";"));
                props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm", NO));
                props.setProperty("tracelogid", storageParentModifiedSet.getColumnValues("tracelogid", ";"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
            catch (Exception ex) {
                this.setError("Storage Parent Modify Error", "VALIDATION", this.getTranslationProcessor().translate("Failed to sync trackitem current storage location and storage unit parent"));
            }
        }
        if (this.ASL) {
            Object sql;
            this.checkOutTI(primary, actionProps);
            try {
                this.processEventLogRule(primary, trackitemdata);
            }
            catch (Exception e) {
                this.logger.error("[Event Logging Error] An error occured while logging events for Trackitem(s) (" + primary.getColumnValues(COLUMN_TRACKITEMID, ";") + "): " + e.getMessage(), e);
            }
            if (!YES.equals(actionProps.getProperty("__bypasscustodyrules"))) {
                DataSet dsStudyTrackItems = new DataSet();
                if (primary.size() <= 750) {
                    SafeSQL safeSQL = new SafeSQL();
                    sql = new StringBuffer("select sp.studyid, sct.trackitemid from study_scheduleplan sp, schedulecondition_trackitem sct");
                    ((StringBuffer)sql).append(" where sp.scheduleplanid = sct.scheduleplanid and sct.trackitemid  in (").append(safeSQL.addIn(primary.getColumnValues(COLUMN_TRACKITEMID, "','"))).append(")");
                    dsStudyTrackItems = this.getQueryProcessor().getPreparedSqlDataSet(((StringBuffer)sql).toString(), safeSQL.getValues());
                } else {
                    String rsetid = this.getDAMProcessor().createRSet(SDCID, primary.getColumnValues(COLUMN_TRACKITEMID, ";"), null, null);
                    dsStudyTrackItems = this.getQueryProcessor().getPreparedSqlDataSet("select sp.studyid, sct.trackitemid from study_scheduleplan sp, schedulecondition_trackitem sct, rsetitems  where sp.scheduleplanid = sct.scheduleplanid and sct.trackitemid = rsetitems.keyid1 and rsetitems.rsetid = ?", (Object[])new String[]{rsetid});
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
                this.processCustodyRule(primary, actionProps, trackitemdata, dsStudyTrackItems);
            } else if (storageCustodyModifiedSet.size() > 0) {
                String tracelogid = primary.size() > 0 ? primary.getString(0, "tracelogid", "") : "";
                new StorageCustodyModifiedRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", "")).processRule(storageCustodyModifiedSet);
            }
            if (this.connectionInfo.hasModule("SMS")) {
                if (primary.isValidColumn(COLUMN_CURRENTSTORAGEUNITID)) {
                    this.syncSampleStatus(primary, trackitemdata, forceUpdate, actionProps);
                }
                if (YES.equals(actionProps.getProperty("__clinicalflag"))) {
                    String clinicalreceiveaction;
                    ArrayList<String> ignorelist = new ArrayList<String>();
                    ignorelist.add("__sdcruleconfirm");
                    ignorelist.add("__clinicalflag");
                    ignorelist.add("propsmatch");
                    ignorelist.add("auditreason");
                    ignorelist.add("auditactivity");
                    ignorelist.add("auditsignedflag");
                    ignorelist.add("sdcid");
                    DataSet temp = new DataSet();
                    for (Object o : actionProps.keySet()) {
                        String property = (String)o;
                        if (ignorelist.contains(property)) continue;
                        String[] value = StringUtil.split(actionProps.getProperty(property), ";");
                        if (!temp.isValidColumn(property)) {
                            temp.addColumn(property, 0);
                        }
                        for (int row = 0; row < value.length; ++row) {
                            if (temp.size() == row) {
                                temp.addRow();
                            }
                            temp.setValue(row, property, value[row]);
                        }
                    }
                    PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                    if (policy != null && StringUtil.getLen(clinicalreceiveaction = policy.getProperty("clinicalsamplereceiveaction")) > 0L) {
                        String actionid = clinicalreceiveaction;
                        String actionversionid = "1";
                        int commaindex = clinicalreceiveaction.indexOf(",");
                        if (commaindex != -1) {
                            actionid = clinicalreceiveaction.substring(0, commaindex);
                            actionversionid = clinicalreceiveaction.substring(commaindex + 1);
                        }
                        DataSet clinicalds = new DataSet();
                        clinicalds.addColumn("sampleid", 0);
                        for (int i = 0; i < temp.size(); ++i) {
                            String trackitemid = temp.getValue(i, "keyid1");
                            Map map = (Map)trackitemdata.get(trackitemid);
                            if (map == null) continue;
                            String storagestatus = (String)map.get("samplestoragestatus");
                            if (!"Sample".equals(map.get(COLUMN_LINKSDCID)) || !YES.equalsIgnoreCase(String.valueOf(map.get("studyclinicalflag"))) || !"Allocated".equals(storagestatus) && !"Received".equals(storagestatus)) continue;
                            clinicalds.copyRow(temp, i, 1);
                            clinicalds.setValue(clinicalds.size() - 1, "sampleid", (String)map.get("sampleid"));
                        }
                        if (clinicalds.size() > 0) {
                            try {
                                PropertyList clinicalprops = new PropertyList();
                                for (int col = 0; col < clinicalds.getColumnCount(); ++col) {
                                    String columnid = clinicalds.getColumnId(col);
                                    clinicalprops.setProperty(columnid, clinicalds.getColumnValues(columnid, ";"));
                                }
                                clinicalprops.setProperty("suppresserror", YES);
                                this.getActionProcessor().processAction(actionid, actionversionid, clinicalprops);
                                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                                    this.setErrors(errorHandler);
                                }
                            }
                            catch (Exception e) {
                                this.logger.info("Register Protocol Sample Action failed to execute from TISM page. This is NOT an error.");
                            }
                        }
                    }
                }
                ArrayList<String> sampleList = new ArrayList<String>();
                for (int i = 0; i < primary.size(); ++i) {
                    if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CONTAINERTYPEID) || !"Sample".equals(this.getOldPrimaryValue(primary, i, COLUMN_LINKSDCID))) continue;
                    sampleList.add(this.getOldPrimaryValue(primary, i, COLUMN_LINKKEYID1));
                }
                if (sampleList.size() > 0) {
                    DataSet sampleds;
                    sql = new StringBuilder();
                    ((StringBuilder)sql).append("select s.s_sampleid, s.samplefamilyid,");
                    if (this.connectionInfo.isOracle()) {
                        ((StringBuilder)sql).append(" (select s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid");
                        ((StringBuilder)sql).append(" and sd.s_sampletypeid = s.sampletypeid and sd.specimentype = t.containertypeid and rownum = 1) specimendefid,");
                    } else {
                        ((StringBuilder)sql).append(" (select top(1) s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid");
                        ((StringBuilder)sql).append(" and sd.s_sampletypeid = s.sampletypeid and sd.specimentype = t.containertypeid) specimendefid,");
                    }
                    ((StringBuilder)sql).append(" sf.specimendefid familyspecimendefid");
                    ((StringBuilder)sql).append(" from s_sample s, s_samplefamily sf, trackitem t, s_participantevent pe");
                    ((StringBuilder)sql).append(" where sf.s_samplefamilyid = s.samplefamilyid");
                    ((StringBuilder)sql).append(" and t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid");
                    ((StringBuilder)sql).append(" and pe.s_participanteventid = sf.participanteventid");
                    if (sampleList.size() > 1000) {
                        String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(sampleList, ";"), null, null);
                        ((StringBuilder)sql).append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ? )");
                        sampleds = this.getQueryProcessor().getPreparedSqlDataSet(((StringBuilder)sql).toString(), (Object[])new String[]{rsetid});
                        this.getDAMProcessor().clearRSet(rsetid);
                    } else {
                        SafeSQL safeSQL = new SafeSQL();
                        ((StringBuilder)sql).append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleList)).append(" )");
                        sampleds = this.getQueryProcessor().getPreparedSqlDataSet(((StringBuilder)sql).toString(), safeSQL.getValues());
                    }
                    if (sampleds != null && sampleds.size() > 0) {
                        DataSet familyds = new DataSet();
                        HashSet<String> familySet = new HashSet<String>();
                        for (int i = 0; i < sampleds.size(); ++i) {
                            String samplefamilyid = sampleds.getString(i, "samplefamilyid", "");
                            if (familySet.contains(samplefamilyid)) continue;
                            familySet.add(samplefamilyid);
                            String specimendefid = sampleds.getString(i, "specimendefid", "");
                            String familyspecimendefid = sampleds.getString(i, "familyspecimendefid", "");
                            if (specimendefid.length() <= 0 || familyspecimendefid.length() != 0) continue;
                            int row = familyds.addRow();
                            familyds.setString(row, "samplefamilyid", samplefamilyid);
                            familyds.setString(row, "specimendefid", specimendefid);
                        }
                        if (familyds.size() > 0) {
                            PropertyList props = new PropertyList();
                            props.setProperty("sdcid", "LV_SampleFamily");
                            props.setProperty("keyid1", familyds.getColumnValues("samplefamilyid", ";"));
                            props.setProperty("specimendefid", familyds.getColumnValues("specimendefid", ";"));
                            props.setProperty("__participanteditflag", YES);
                            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        }
                    }
                }
            }
        }
        this.validateStabilityUnits(primary, this.getQueryProcessor(), this.getTranslationProcessor());
        Trace.logInfo("END: TrackItemSDC.postEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void processPostAddSDCSecurity(DataSet primary, PropertyList actionProps) throws ActionException {
        DataSet security = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String departmentid = primary.getString(i, COLUMN_CUSTODIALDEPARTMENTID);
            if (!OpalUtil.isNotEmpty(departmentid)) continue;
            int row = security.addRow();
            security.setString(row, COLUMN_TRACKITEMID, primary.getString(i, COLUMN_TRACKITEMID));
            security.setString(row, "sdcid", primary.getString(i, COLUMN_LINKSDCID, ""));
            security.setString(row, "keyid1", primary.getString(i, COLUMN_LINKKEYID1, ""));
            security.setString(row, "keyid2", primary.getString(i, COLUMN_LINKKEYID2, ""));
            security.setString(row, "keyid3", primary.getString(i, COLUMN_LINKKEYID3, ""));
            security.setString(row, COLUMN_CUSTODIALDEPARTMENTID, departmentid);
        }
        if (security.size() > 0) {
            security.sort("sdcid");
            ArrayList<DataSet> dslist = security.getGroupedDataSets("sdcid");
            for (DataSet ds : dslist) {
                if (ds.size() <= 0) continue;
                PropertyList props = new PropertyList();
                String sdcid = ds.getString(0, "sdcid", "");
                if (sdcid.length() <= 0 || !OpalUtil.isSDISecurityEnabled(this.getSDCProcessor(), sdcid)) continue;
                DataSet addds = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    String currentcustodysecurityset;
                    String custodialdepartmentid = ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID);
                    if (StringUtil.getLen(custodialdepartmentid) <= 0L || StringUtil.getLen(currentcustodysecurityset = this.getDepartmentProperty(custodialdepartmentid, "currentcustodysecurityset")) <= 0L) continue;
                    int row = addds.addRow();
                    addds.setString(row, "keyid1", ds.getString(i, "keyid1"));
                    addds.setString(row, "keyid2", ds.getString(i, "keyid2"));
                    addds.setString(row, "keyid3", ds.getString(i, "keyid3"));
                    addds.setString(row, "securityset", currentcustodysecurityset);
                }
                if (addds.size() <= 0) continue;
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", addds.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", addds.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", addds.getColumnValues("keyid3", ";"));
                props.setProperty("securityset", addds.getColumnValues("securityset", ";"));
                props.setProperty("propsmatch", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(AddSDISecuritySet.class.getName(), props);
            }
        }
    }

    private void processSDCSecurity(DataSet primary, PropertyList actionProps) throws ActionException {
        DataSet security = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) continue;
            int row = security.addRow();
            security.setString(row, COLUMN_TRACKITEMID, primary.getString(i, COLUMN_TRACKITEMID));
            security.setString(row, "sdcid", this.getOldPrimaryValue(primary, i, COLUMN_LINKSDCID));
            security.setString(row, "keyid1", this.getOldPrimaryValue(primary, i, COLUMN_LINKKEYID1));
            security.setString(row, "keyid2", this.getOldPrimaryValue(primary, i, COLUMN_LINKKEYID2));
            security.setString(row, "keyid3", this.getOldPrimaryValue(primary, i, COLUMN_LINKKEYID3));
            security.setString(row, "oldcustodialdepartmentid", this.getOldPrimaryValue(primary, i, COLUMN_CUSTODIALDEPARTMENTID));
            security.setString(row, COLUMN_CUSTODIALDEPARTMENTID, primary.getString(i, COLUMN_CUSTODIALDEPARTMENTID));
        }
        if (security.size() > 0) {
            security.sort("sdcid");
            ArrayList<DataSet> dslist = security.getGroupedDataSets("sdcid");
            for (DataSet ds : dslist) {
                if (ds.size() <= 0) continue;
                PropertyList props = new PropertyList();
                String sdcid = ds.getString(0, "sdcid", "");
                if (sdcid.length() <= 0) continue;
                if (OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), sdcid)) {
                    DataSet actionds = new DataSet();
                    for (int i = 0; i < ds.size(); ++i) {
                        String olddepartmentid = ds.getString(i, "oldcustodialdepartmentid");
                        String custodialdepartmentid = ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID);
                        if (StringUtil.getLen(olddepartmentid) <= 0L || olddepartmentid.equals(custodialdepartmentid) || !YES.equals(this.getDepartmentProperty(olddepartmentid, "retainaccessflag"))) continue;
                        int row = actionds.addRow();
                        actionds.setString(row, "keyid1", ds.getString(i, "keyid1"));
                        actionds.setString(row, "keyid2", ds.getString(i, "keyid2"));
                        actionds.setString(row, "keyid3", ds.getString(i, "keyid3"));
                        actionds.setString(row, "departmentid", olddepartmentid);
                    }
                    if (actionds.size() <= 0) continue;
                    props.clear();
                    props.setProperty("sdcid", sdcid);
                    props.setProperty("keyid1", actionds.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", actionds.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", actionds.getColumnValues("keyid3", ";"));
                    props.setProperty("departmentid", actionds.getColumnValues("departmentid", ";"));
                    props.setProperty("propsmatch", YES);
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(AddSDISecurityDept.class.getName(), props);
                    continue;
                }
                if (!OpalUtil.isSDISecurityEnabled(this.getSDCProcessor(), sdcid)) continue;
                DataSet addds = new DataSet();
                DataSet deleteds = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    String relinquishcustodysecurityset;
                    String custodialdepartmentid;
                    int row;
                    String olddepartmentid = ds.getString(i, "oldcustodialdepartmentid");
                    if (StringUtil.getLen(olddepartmentid) > 0L && !olddepartmentid.equals(ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID))) {
                        String relinquishcustodysecurityset2;
                        String currentcustodysecurityset = this.getDepartmentProperty(olddepartmentid, "currentcustodysecurityset");
                        if (StringUtil.getLen(currentcustodysecurityset) > 0L) {
                            int row2 = deleteds.addRow();
                            deleteds.setString(row2, "keyid1", ds.getString(i, "keyid1"));
                            deleteds.setString(row2, "keyid2", ds.getString(i, "keyid2"));
                            deleteds.setString(row2, "keyid3", ds.getString(i, "keyid3"));
                            deleteds.setString(row2, "securityset", currentcustodysecurityset);
                        }
                        if (StringUtil.getLen(relinquishcustodysecurityset2 = this.getDepartmentProperty(olddepartmentid, "relinquishcustodysecurityset")) > 0L) {
                            row = addds.addRow();
                            addds.setString(row, "keyid1", ds.getString(i, "keyid1"));
                            addds.setString(row, "keyid2", ds.getString(i, "keyid2"));
                            addds.setString(row, "keyid3", ds.getString(i, "keyid3"));
                            addds.setString(row, "securityset", relinquishcustodysecurityset2);
                        }
                    }
                    if (StringUtil.getLen(custodialdepartmentid = ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID)) <= 0L) continue;
                    String currentcustodysecurityset = this.getDepartmentProperty(custodialdepartmentid, "currentcustodysecurityset");
                    if (StringUtil.getLen(currentcustodysecurityset) > 0L) {
                        row = addds.addRow();
                        addds.setString(row, "keyid1", ds.getString(i, "keyid1"));
                        addds.setString(row, "keyid2", ds.getString(i, "keyid2"));
                        addds.setString(row, "keyid3", ds.getString(i, "keyid3"));
                        addds.setString(row, "securityset", currentcustodysecurityset);
                    }
                    if (StringUtil.getLen(relinquishcustodysecurityset = this.getDepartmentProperty(custodialdepartmentid, "relinquishcustodysecurityset")) <= 0L) continue;
                    int row3 = deleteds.addRow();
                    deleteds.setString(row3, "keyid1", ds.getString(i, "keyid1"));
                    deleteds.setString(row3, "keyid2", ds.getString(i, "keyid2"));
                    deleteds.setString(row3, "keyid3", ds.getString(i, "keyid3"));
                    deleteds.setString(row3, "securityset", relinquishcustodysecurityset);
                }
                if (addds.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", sdcid);
                    props.setProperty("keyid1", addds.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", addds.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", addds.getColumnValues("keyid3", ";"));
                    props.setProperty("securityset", addds.getColumnValues("securityset", ";"));
                    props.setProperty("propsmatch", YES);
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(AddSDISecuritySet.class.getName(), props);
                }
                if (deleteds.size() <= 0) continue;
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", deleteds.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", deleteds.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", deleteds.getColumnValues("keyid3", ";"));
                props.setProperty("securityset", deleteds.getColumnValues("securityset", ";"));
                props.setProperty("propsmatch", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(DeleteSDISecuritySet.class.getName(), props);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    private void updateUseCountAndStatus(QueryProcessor qp, DataSet primary, DataSet oldPrimary, int row, String reagentLotId) throws SapphireException {
        if (this.checkIfQuantityDecremented(primary, row)) {
            useCount = TrackItemSDC.getPrimaryValue(qp, primary, row, "usecount");
            if (useCount == null || useCount.length() == 0) {
                iUseCount = 1;
                if (primary.size() > 1 && !primary.isValidColumn("firstusedt")) {
                    this.populateFirstUseDt(primary, oldPrimary);
                }
                if ((firstusedtStr = TrackItemSDC.getPrimaryValue(qp, primary, row, "firstusedt")).length() == 0) {
                    primary.setDate(row, "firstusedt", "n");
                    try {
                        expiryDate = this.calculateExpirationDate(reagentLotId);
                        if (expiryDate == null) ** GOTO lbl21
                        primary.setDate(row, "expirydt", expiryDate);
                    }
                    catch (SapphireException e) {
                        this.logger.error("Failed to fetch ExpiryFirstUsePeriod details", e);
                        return;
                    }
                }
            } else {
                iUseCount = Integer.parseInt(useCount) + 1;
            }
lbl21:
            // 4 sources

            primary.setNumber(row, "usecount", iUseCount);
            sql = "SELECT expiryusecount from reagenttype, reagentlot  WHERE reagenttype.reagenttypeid=reagentlot.reagenttypeid  AND reagenttype.reagenttypeversionid=reagentlot.reagenttypeversionid  AND reagentlotid=?";
            try {
                this.database.createPreparedResultSet(sql, new Object[]{reagentLotId});
                if (!this.database.getNext() || iUseCount != (expiryUseCount = this.database.getInt("expiryusecount"))) ** GOTO lbl45
                primary.setString(row, "trackitemstatus", "Expired");
                primary.setDate(row, "expirydt", "n");
            }
            catch (SapphireException e) {
                this.logger.error("Failed to fetch expiryusecount", e);
                return;
            }
        } else if (primary.isValidColumn("firstusedt") && this.hasPrimaryValueChanged(primary, row, "firstusedt")) {
            try {
                expiryDate = this.calculateExpirationDate(reagentLotId, primary.getCalendar(row, "firstusedt"));
                if (expiryDate != null) {
                    primary.setDate(row, "expirydt", expiryDate);
                }
            }
            catch (SapphireException e) {
                this.logger.error("Failed to set Expiry date:", e);
                return;
            }
        }
lbl45:
        // 5 sources

        if ((newCurrentQty = TrackItemSDC.getPrimaryValue(qp, primary, row, "qtycurrent")) != null && !newCurrentQty.trim().isEmpty() && Double.parseDouble(newCurrentQty.replace(decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator(), '.')) <= 0.0) {
            if (primary.size() > 1 && !primary.isValidColumn("trackitemstatus")) {
                this.populateColumnValue(primary, oldPrimary, "trackitemstatus");
            }
            primary.setString(row, "trackitemstatus", "Depleted");
        }
    }

    private void populateFirstUseDt(DataSet primary, DataSet oldPrimary) {
        for (int i = 0; i < primary.size(); ++i) {
            String trackitemid = primary.getString(i, COLUMN_TRACKITEMID, "");
            int rowid = oldPrimary.findRow(COLUMN_TRACKITEMID, trackitemid);
            if (rowid < 0) continue;
            primary.setDate(i, COLUMN_FIRTSUSEDDT, oldPrimary.getValue(rowid, COLUMN_FIRTSUSEDDT, ""));
        }
    }

    private void populateColumnValue(DataSet primary, DataSet oldPrimary, String columnid) {
        for (int i = 0; i < primary.size(); ++i) {
            String trackitemid = primary.getString(i, COLUMN_TRACKITEMID, "");
            int rowid = oldPrimary.findRow(COLUMN_TRACKITEMID, trackitemid);
            if (rowid < 0) continue;
            primary.setString(i, columnid, oldPrimary.getString(rowid, columnid, ""));
        }
    }

    private Calendar calculateExpirationDate(String reagentLotId) throws SapphireException {
        return this.calculateExpirationDate(reagentLotId, null);
    }

    private Calendar calculateExpirationDate(String reagentLotId, Calendar firstuseddate) throws SapphireException {
        Calendar c = null;
        String sql = "SELECT expiryfirstuseperiod, expiryfirstuseperiodunits,reagentlot.expirydt FROM reagenttype, reagentlot";
        sql = sql + " WHERE reagentlot.reagenttypeid=reagenttype.reagenttypeid";
        sql = sql + " AND reagentlot.reagenttypeversionid=reagenttype.reagenttypeversionid";
        sql = sql + " AND reagentlotid=?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{reagentLotId});
        if (ds != null && ds.size() > 0) {
            String expiryfirstuseperiodStr = ds.getValue(0, "expiryfirstuseperiod");
            String expiryfirstuseperiodunits = ds.getString(0, "expiryfirstuseperiodunits");
            if (expiryfirstuseperiodStr == null || expiryfirstuseperiodStr.length() == 0 || expiryfirstuseperiodunits == null || expiryfirstuseperiodunits.length() == 0) {
                this.logger.info("firstUse information is not set");
                return c;
            }
            BigDecimal expiryfirstuseperiod = ds.getBigDecimal(0, "expiryfirstuseperiod", new BigDecimal("0"));
            Calendar dt = firstuseddate == null ? DateTimeUtil.getNowCalendar() : firstuseddate;
            Calendar calculatedExpirydt = DateTimeUtil.getOffsetDate(dt, expiryfirstuseperiodunits, expiryfirstuseperiod);
            Calendar reagentlotExpirydt = ds.getCalendar(0, COLUMN_EXPIRYDT);
            if (reagentlotExpirydt == null || reagentlotExpirydt.after(calculatedExpirydt)) {
                return calculatedExpirydt;
            }
            return c;
        }
        return c;
    }

    boolean checkIfQuantityDecremented(DataSet primary, int row) throws SapphireException {
        if (!primary.isValidColumn(COLUMN_QTYCURRENT)) {
            return false;
        }
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("primary");
        double oldQuantity = oldPrimary.getBigDecimal(row, COLUMN_QTYCURRENT, new BigDecimal(0)).doubleValue();
        String oldQuantityUnit = oldPrimary.getString(row, COLUMN_QTYUNIT, "");
        String oldQuantityUnitType = oldPrimary.getString(row, COLUMN_QTYCURRENTTYPE, "");
        String containerTypeid = oldPrimary.getString(row, COLUMN_CONTAINERTYPEID, "");
        double newQuantity = primary.getBigDecimal(row, COLUMN_QTYCURRENT, new BigDecimal(0)).doubleValue();
        String newQuantityUnit = primary.getString(row, COLUMN_QTYUNIT, "");
        String newQuantityUnitType = primary.getString(row, COLUMN_QTYCURRENTTYPE, "");
        try {
            if (!oldQuantityUnit.equalsIgnoreCase(newQuantityUnit)) {
                if (oldQuantityUnitType.equalsIgnoreCase(newQuantityUnitType)) {
                    newQuantity = ReagentUtil.getConvertedValue(newQuantity, newQuantityUnit, oldQuantityUnit, this.database);
                } else if (oldQuantityUnitType.equalsIgnoreCase("C")) {
                    oldQuantity = this.getConvertFromContainersToUnits(oldQuantity, containerTypeid, newQuantityUnit);
                } else if (newQuantityUnitType.equalsIgnoreCase("C")) {
                    newQuantity = this.getConvertFromContainersToUnits(newQuantity, containerTypeid, oldQuantityUnit);
                }
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to mark the reagentlot as depleted", e);
        }
        return oldQuantity > newQuantity;
    }

    private double getConvertFromContainersToUnits(double amount, String containertypeid, String convertedUnit) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select sizevalue,sizeunits from containertype where containertypeid=" + safeSQL.addVar(containertypeid));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String containerSize = ds.getValue(0, COLUMN_CONTAINERSIZE, "");
        String containerUnits = ds.getString(0, COLUMN_CONTAINERUNITS, "");
        return UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), containerSize, containerUnits, Double.toString(amount), convertedUnit);
    }

    private void checkIfReagentLotDepleted(DataSet primary, int row, String reagentLotId) throws SapphireException {
        String trackItemStatus = primary.getValue(row, COLUMN_TRACKITEMSTATUS, "");
        if ("Depleted".equalsIgnoreCase(trackItemStatus) && this.checkRLTrackItemsStatus(reagentLotId, "Depleted")) {
            this.setReagentLotStatus(reagentLotId, "Depleted");
        }
        if ("Disposed".equalsIgnoreCase(trackItemStatus) && this.checkRLTrackItemsStatus(reagentLotId, "Disposed")) {
            this.setReagentLotStatus(reagentLotId, "Disposed");
        }
    }

    private boolean checkRLTrackItemsStatus(String reagentLotId, String status) throws SapphireException {
        String sql = "SELECT trackitemid FROM trackitem WHERE linksdcid = 'LV_ReagentLot' AND  linkkeyid1 = ? AND trackitemstatus <> ? ";
        this.database.createPreparedResultSet(sql, new Object[]{reagentLotId, status});
        return !this.database.getNext();
    }

    private void setReagentLotStatus(String reagentLotId, String status) throws SapphireException {
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_ReagentLot");
            actionProps.put("keyid1", reagentLotId);
            actionProps.put("reagentstatus", status);
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
        catch (ActionException e) {
            throw new SapphireException("Failed to mark the reagentlot as depleted", e);
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (YES.equals(actionProps.getProperty("__cmtimportflag"))) {
            return;
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataAdd");
        }
        DataSet primary = sdiData.getDataset("primary");
        StringBuilder sql = new StringBuilder();
        HashSet<String> sampleSet = new HashSet<String>();
        BigDecimal bigDecimalOne = new BigDecimal(1.0);
        for (int i = 0; i < primary.size(); ++i) {
            BigDecimal bd;
            String linkkeyid1;
            String linkSDC = primary.getString(i, COLUMN_LINKSDCID);
            if ("LV_ReagentLot".equals(linkSDC)) {
                String qtyunits = primary.getString(i, COLUMN_QTYUNIT, "");
                if (qtyunits.equalsIgnoreCase("(Containers)")) {
                    primary.setString(i, COLUMN_QTYUNIT, "");
                    primary.setString(i, COLUMN_QTYCURRENTTYPE, "C");
                } else if (qtyunits.length() > 0) {
                    primary.setString(i, COLUMN_QTYCURRENTTYPE, "U");
                }
                String error = this.validateUnit(primary, i, true);
                if (error.length() > 0) {
                    throw new SapphireException(error);
                }
                String reagentLotId = primary.getString(i, COLUMN_LINKKEYID1);
                DataSet ds = this.getFreezThawDetails(reagentLotId);
                if (ds != null && ds.size() == 1) {
                    String contentflag = ds.getValue(0, "contentflag");
                    primary.addColumn(COLUMN_CONTAINERTYPEID, 0);
                    primary.setValue(i, COLUMN_CONTAINERTYPEID, ds.getString(0, COLUMN_CONTAINERTYPEID, ""));
                    if ("R".equals(contentflag)) {
                        String maxFTCount = ds.getValue(0, "maxfreezethawcount", "");
                        String warnFTCount = ds.getValue(0, "warnfreezethawcount", "");
                        if (StringUtil.getLen(maxFTCount) > 0L || StringUtil.getLen(warnFTCount) > 0L) {
                            primary.addColumn("freezethawflag", 0);
                            primary.addColumn("freezethawcountmax", 0);
                            primary.addColumn("freezethawcountwarn", 0);
                            primary.setValue(i, "freezethawflag", YES);
                            primary.setValue(i, "freezethawcountmax", maxFTCount);
                            primary.setValue(i, "freezethawcountwarn", warnFTCount);
                        }
                    } else if ("K".equals(contentflag) && "Complete Kit".equals(ds.getValue(0, "reagentclass"))) {
                        String sequence;
                        String kitlotid = primary.getValue(i, COLUMN_LINKKEYID1);
                        String studyalias = this.getKitLotStudyAlias(kitlotid);
                        if (StringUtil.getLen(studyalias) > 0L) {
                            String studyid = ds.getValue(i, "s_studyid");
                            int num = this.getSequenceProcessor().getSequence("Study", "kit_" + studyid);
                            sequence = studyalias + "-" + StringUtil.padLeft(String.valueOf(num), 8, '0');
                        } else {
                            int num = this.getSequenceProcessor().getSequence(SDCID, "kit");
                            sequence = "KIT-" + StringUtil.padLeft(String.valueOf(num), 8, '0');
                        }
                        primary.addColumn("trackitemlabel", 0);
                        primary.setValue(i, "trackitemlabel", sequence);
                    }
                }
            } else if ("Sample".equals(linkSDC) && (linkkeyid1 = primary.getString(i, COLUMN_LINKKEYID1, "")).length() > 0) {
                sampleSet.add(linkkeyid1);
            }
            if (!this.connectionInfo.getDbms().equals("MSS") || !actionProps.containsKey(COLUMN_QTYCURRENT) || (bd = primary.getBigDecimal(i, COLUMN_QTYCURRENT)) == null) continue;
            primary.setNumber(i, COLUMN_QTYCURRENT, bd.divide(bigDecimalOne, 10, 4));
        }
        if (!YES.equals(actionProps.getProperty("__sampletrackitemrule", NO)) && actionProps.getProperty("freezethawflag").length() == 0 && sampleSet.size() > 0) {
            DataSet ds;
            sql.setLength(0);
            sql.append("select s.s_sampleid, s.sampletypeid, st.freezethawflag, st.freezethawcountwarn, st.freezethawcountmax");
            sql.append(" from s_sample s, s_sampletype st");
            sql.append(" where st.s_sampletypeid = s.sampletypeid");
            if (sampleSet.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("Sample", OpalUtil.toDelimitedString(sampleSet, ";"), null, null);
                sql.append(" and s.s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(sampleSet)).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int i = 0; i < primary.size(); ++i) {
                    String freezethawflag;
                    String sampleid = primary.getString(i, COLUMN_LINKKEYID1, "");
                    filter.clear();
                    filter.put("s_sampleid", sampleid);
                    int row = ds.findRow(filter);
                    if (row == -1 || !YES.equals(freezethawflag = ds.getString(row, "freezethawflag", NO))) continue;
                    primary.setString(i, "freezethawflag", YES);
                    primary.setNumber(i, "freezethawcountwarn", ds.getInt(row, "freezethawcountwarn", 0));
                    primary.setNumber(i, "freezethawcountmax", ds.getInt(row, "freezethawcountmax", 0));
                }
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (YES.equals(actionProps.getProperty("__cmtimportflag"))) {
            return;
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataAdd");
        }
        HashSet<String> storageunitSet = new HashSet<String>();
        sdiData.getDataset("primary").showData();
        String scheduleConditionId = actionProps.getProperty("scheduleconditionid");
        String schedulePlanId = actionProps.getProperty("scheduleplanid");
        DataSet primary = sdiData.getDataset("primary");
        if (scheduleConditionId.length() > 0 && schedulePlanId.length() > 0) {
            this.manageScheduleConditionTrackitems(schedulePlanId, scheduleConditionId, primary, actionProps, OP_ADD);
        }
        for (int i = 0; i < primary.size(); ++i) {
            String currentstorageunitid;
            String linkSDC = primary.getString(i, COLUMN_LINKSDCID);
            if ("LV_ReagentLot".equals(linkSDC)) {
                String reagentLotId = primary.getString(i, COLUMN_LINKKEYID1);
                this.checkReagentThreshold(reagentLotId, OP_ADD, null, null);
            }
            if (StringUtil.getLen(currentstorageunitid = primary.getValue(i, COLUMN_CURRENTSTORAGEUNITID, "")) <= 0L) continue;
            storageunitSet.add(currentstorageunitid);
        }
        if (storageunitSet.size() > 0) {
            DataSet ds;
            StringBuilder sql = new StringBuilder();
            if (storageunitSet.size() < 750) {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select s.storageunitid, s.maxtiallowed, s.labelpath, (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = s.storageunitid) ticount");
                sql.append(" from storageunit s");
                sql.append(" where s.storageunitid in (").append(safeSQL.addIn(storageunitSet)).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            } else {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", OpalUtil.toDelimitedString(storageunitSet, ";"), null, null);
                sql.append("select s.storageunitid, s.maxtiallowed, s.labelpath, (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = s.storageunitid) ticount");
                sql.append(" from storageunit s");
                sql.append(" where s.storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            }
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String storageunitid = ds.getString(i, "storageunitid");
                    int maxtiallowed = ds.getInt(i, "maxtiallowed");
                    int ticount = ds.getInt(i, "ticount");
                    if (-1 == maxtiallowed || ticount <= maxtiallowed) continue;
                    String error = this.getTranslationProcessor().translate("No space available in") + " ";
                    error = error + StorageUnitSDC.getStorageUnitSDIInfo(this.getQueryProcessor(), storageunitid);
                    error = error + "<br>[" + ds.getValue(i, "labelpath") + "]";
                    throw new SapphireException("MaxTrackItem Allowed Rule", "VALIDATION", error);
                }
                this.updateSpaceAvailableAndStatus(primary, null, actionProps);
            }
        }
        this.processPostAddSDCSecurity(primary, actionProps);
    }

    private String getKitLotStudyAlias(String kitlotid) {
        if (!this.kitstudyaliasmap.containsKey(kitlotid)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s.studyalias from s_study s where s.s_studyid = (select r.sstudyid from reagentlot r where r.reagentlotid = ?)", (Object[])new String[]{kitlotid});
            if (ds != null && ds.size() > 0) {
                this.kitstudyaliasmap.put(kitlotid, ds.getValue(0, "studyalias"));
            } else {
                this.kitstudyaliasmap.put(kitlotid, "");
            }
        }
        return (String)this.kitstudyaliasmap.get(kitlotid);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.ASL = this.connectionInfo.hasModule("ASL");
        ArrayUtil.checkExistenceInArray(rsetid, actionProps, this.getQueryProcessor(), this.getTranslationProcessor(), false);
        this.database.executePreparedUpdate("update s_eventlog set parenteventlogid = null where parenteventlogid in ( select s.s_eventlogid from s_eventlog s where s.trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?) )", new Object[]{rsetid});
        this.database.executePreparedUpdate("delete from s_eventlog where trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)", new Object[]{rsetid});
        String trackitemid = actionProps.getProperty("keyid1");
        String[] list = StringUtil.split(trackitemid, ";");
        String sql = "SELECT trackitemid, linksdcid, linkkeyid1, currentstorageunitid FROM trackitem WHERE trackitemid in ";
        sql = sql + "(select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
        int rows = ds.getRowCount();
        HashSet<String> storageUnitSet = new HashSet<String>();
        for (int row = 0; row < rows; ++row) {
            String linksdcid = ds.getString(row, COLUMN_LINKSDCID);
            String linkkeyid1 = ds.getString(row, COLUMN_LINKKEYID1);
            String currentStorageUnitId = ds.getString(row, COLUMN_CURRENTSTORAGEUNITID, "");
            if (linksdcid != null && linksdcid.equalsIgnoreCase("LV_ReagentLot")) {
                this.checkReagentThreshold(linkkeyid1, OP_DELETE, list, null);
            }
            if (currentStorageUnitId.length() <= 0) continue;
            storageUnitSet.add(currentStorageUnitId);
        }
        String scheduleConditionId = actionProps.getProperty("scheduleconditionid");
        String schedulePlanId = actionProps.getProperty("scheduleplanid");
        if (scheduleConditionId.length() > 0 && schedulePlanId.length() > 0) {
            DataSet dsTI = new DataSet();
            dsTI.addColumnValues(COLUMN_TRACKITEMID, 0, trackitemid, ";");
            this.manageScheduleConditionTrackitems(schedulePlanId, scheduleConditionId, dsTI, actionProps, OP_DELETE);
        } else {
            this.delScheduleConditionTrackitem(actionProps);
        }
        if (storageUnitSet.size() > 0) {
            this.updateSpaceAvailableAndStatus(null, storageUnitSet, actionProps);
        }
    }

    private void delScheduleConditionTrackitem(PropertyList actionProps) throws SapphireException {
        String trackitemid = actionProps.getProperty("keyid1");
        String[] list = StringUtil.split(trackitemid, ";");
        if (list != null) {
            String delSql = "DELETE FROM schedulecondition_trackitem where trackitemid = ?";
            PreparedStatement psmtDel = this.database.prepareStatement("delSCTI", delSql);
            try {
                for (int i = 0; i < list.length; ++i) {
                    psmtDel.setString(1, list[i]);
                    psmtDel.addBatch();
                }
                psmtDel.executeBatch();
                this.database.closeStatement("delSCTI");
            }
            catch (Exception e) {
                throw new SapphireException("Unable to delete link from schedulecondition_trackitem table: " + e.getMessage());
            }
        }
    }

    private void processEventLogRule(DataSet primary, HashMap trackitemdata) throws SapphireException {
        if (primary != null) {
            EventLog eventLog = new EventLog(this.database, this.getSequenceProcessor());
            eventLog.setCurrentUser(this.getConnectionInfo().getSysuserId());
            EventLog eventLogParent = new EventLog(this.database, this.getSequenceProcessor());
            eventLogParent.setCurrentUser(this.getConnectionInfo().getSysuserId());
            for (int i = 0; i < primary.size(); ++i) {
                String trackitemid = primary.getString(i, COLUMN_TRACKITEMID);
                if (!trackitemdata.containsKey(trackitemid)) continue;
                HashMap primaryInfo = (HashMap)trackitemdata.get(trackitemid);
                String tracelogid = primary.getString(i, "tracelogid");
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID)) {
                    String oldstorageunitid = this.getOldPrimaryValue(primary, i, COLUMN_CURRENTSTORAGEUNITID);
                    String currentstorageunitid = primary.getValue(i, COLUMN_CURRENTSTORAGEUNITID);
                    HashMap oldSUInfo = this.getStorageUnitInfo(oldstorageunitid);
                    int row = eventLogParent.addRow();
                    String linksdcid = (String)primaryInfo.get("currentstoragesdcid");
                    String eventtype = "LocationChange" + linksdcid;
                    String oldLabelPath = (String)oldSUInfo.get("labelpath");
                    if (OpalUtil.isNotEmpty(oldLabelPath) && oldLabelPath.length() > 80) {
                        oldLabelPath = oldLabelPath.substring(0, 76) + "...";
                    }
                    linksdcid = linksdcid == null ? "" : linksdcid;
                    eventLogParent.setString(row, "eventtype", eventtype);
                    eventLogParent.setString(row, COLUMN_TRACKITEMID, trackitemid);
                    eventLogParent.setString(row, "departmentid", (String)primaryInfo.get(COLUMN_CUSTODIALDEPARTMENTID));
                    eventLogParent.setString(row, "oldvalue", oldLabelPath);
                    eventLogParent.setString(row, "newvalue", (String)primaryInfo.get("currentstoragelabelpath"));
                    eventLogParent.setString(row, "oldstorageunitid", oldstorageunitid);
                    eventLogParent.setString(row, "oldlabelpath", oldLabelPath);
                    eventLogParent.setString(row, "oldlinksdcid", (String)oldSUInfo.get(COLUMN_LINKSDCID));
                    eventLogParent.setString(row, "oldlinkkeyid1", (String)oldSUInfo.get(COLUMN_LINKKEYID1));
                    eventLogParent.setString(row, "oldlinkkeyid2", (String)oldSUInfo.get(COLUMN_LINKKEYID2));
                    eventLogParent.setString(row, "oldlinkkeyid3", (String)oldSUInfo.get(COLUMN_LINKKEYID3));
                    eventLogParent.setString(row, "newstorageunitid", currentstorageunitid);
                    eventLogParent.setString(row, "newlabelpath", (String)primaryInfo.get("currentstoragelabelpath"));
                    eventLogParent.setString(row, "newlinksdcid", linksdcid);
                    eventLogParent.setString(row, "newlinkkeyid1", (String)primaryInfo.get("currentstoragekeyid1"));
                    eventLogParent.setString(row, "newlinkkeyid2", (String)primaryInfo.get("currentstoragekeyid2"));
                    eventLogParent.setString(row, "newlinkkeyid3", (String)primaryInfo.get("currentstoragekeyid3"));
                    eventLogParent.setString(row, "eventsdcid", (String)primaryInfo.get(COLUMN_LINKSDCID));
                    eventLogParent.setString(row, "eventkeyid1", (String)primaryInfo.get(COLUMN_LINKKEYID1));
                    eventLogParent.setString(row, "eventkeyid2", (String)primaryInfo.get(COLUMN_LINKKEYID2));
                    eventLogParent.setString(row, "eventkeyid3", (String)primaryInfo.get(COLUMN_LINKKEYID3));
                    eventLogParent.setString(row, "tracelogid", tracelogid);
                    if (!"Sample".equals(primaryInfo.get(COLUMN_LINKSDCID))) {
                        String linkstorageunitid = (String)primaryInfo.get("linkstorageunitid");
                        String linkstoragemoveableflag = (String)primaryInfo.get("linkstoragemoveableflag");
                        if (YES.equals(linkstoragemoveableflag)) {
                            eventLogParent.setRecordParentEventLogID(true);
                            List<Map<String, String>> childtrackitems = this.getChildTrackItems(linkstorageunitid);
                            for (Map<String, String> childtrackitem : childtrackitems) {
                                row = eventLogParent.addRow();
                                String oldPath = oldLabelPath + "/" + childtrackitem.get(COLUMN_STORAGEUNITLABEL);
                                if (OpalUtil.isNotEmpty(oldPath) && oldPath.length() > 80) {
                                    oldPath = oldPath.substring(0, 76) + "...";
                                }
                                eventLogParent.setString(row, "eventtype", eventtype);
                                eventLogParent.setString(row, COLUMN_TRACKITEMID, childtrackitem.get(COLUMN_TRACKITEMID));
                                eventLogParent.setString(row, "departmentid", childtrackitem.get(COLUMN_CUSTODIALDEPARTMENTID));
                                eventLogParent.setString(row, "oldvalue", oldPath);
                                eventLogParent.setString(row, "newvalue", childtrackitem.get("labelpath"));
                                eventLogParent.setString(row, "oldstorageunitid", childtrackitem.get(COLUMN_CURRENTSTORAGEUNITID));
                                eventLogParent.setString(row, "oldlabelpath", oldPath);
                                eventLogParent.setString(row, "newstorageunitid", childtrackitem.get(COLUMN_CURRENTSTORAGEUNITID));
                                eventLogParent.setString(row, "newlabelpath", childtrackitem.get("labelpath"));
                                eventLogParent.setString(row, "eventsdcid", childtrackitem.get("tilinksdcid"));
                                eventLogParent.setString(row, "eventkeyid1", childtrackitem.get("tilinkkeyid1"));
                                eventLogParent.setString(row, "eventkeyid2", childtrackitem.get("tilinkkeyid2"));
                                eventLogParent.setString(row, "eventkeyid3", childtrackitem.get("tilinkkeyid3"));
                                eventLogParent.setString(row, "tracelogid", tracelogid);
                            }
                        }
                    }
                }
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) {
                    int row = eventLog.addRow();
                    eventLog.setString(row, "eventtype", "CustodialDomainChange");
                    eventLog.setString(row, COLUMN_TRACKITEMID, trackitemid);
                    eventLog.setString(row, "oldvalue", this.getOldPrimaryValue(primary, i, COLUMN_CUSTODIALDEPARTMENTID));
                    eventLog.setString(row, "newvalue", (String)primaryInfo.get(COLUMN_CUSTODIALDEPARTMENTID));
                    eventLog.setString(row, "eventsdcid", (String)primaryInfo.get(COLUMN_LINKSDCID));
                    eventLog.setString(row, "eventkeyid1", (String)primaryInfo.get(COLUMN_LINKKEYID1));
                    eventLog.setString(row, "eventkeyid2", (String)primaryInfo.get(COLUMN_LINKKEYID2));
                    eventLog.setString(row, "eventkeyid3", (String)primaryInfo.get(COLUMN_LINKKEYID3));
                    eventLog.setString(row, "tracelogid", tracelogid);
                    eventLog.setString(row, "departmentid", (String)primaryInfo.get(COLUMN_CUSTODIALDEPARTMENTID));
                }
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID)) continue;
                int row = eventLog.addRow();
                eventLog.setString(row, "eventtype", "CustodyChange");
                eventLog.setString(row, COLUMN_TRACKITEMID, trackitemid);
                eventLog.setString(row, "oldvalue", this.getOldPrimaryValue(primary, i, COLUMN_CUSTODIALUSERID));
                eventLog.setString(row, "newvalue", (String)primaryInfo.get(COLUMN_CUSTODIALUSERID));
                eventLog.setString(row, "eventsdcid", (String)primaryInfo.get(COLUMN_LINKSDCID));
                eventLog.setString(row, "eventkeyid1", (String)primaryInfo.get(COLUMN_LINKKEYID1));
                eventLog.setString(row, "eventkeyid2", (String)primaryInfo.get(COLUMN_LINKKEYID2));
                eventLog.setString(row, "eventkeyid3", (String)primaryInfo.get(COLUMN_LINKKEYID3));
                eventLog.setString(row, "tracelogid", tracelogid);
                eventLog.setString(row, "departmentid", (String)primaryInfo.get(COLUMN_CUSTODIALDEPARTMENTID));
            }
            eventLog.process();
            eventLogParent.process();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void validateMovement(DataSet move, boolean forceUpdate, DataSet stabilityTrackItems) throws SapphireException {
        DataSet ds;
        StringBuilder confirm = new StringBuilder();
        StringBuilder error = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        String userid = this.connectionInfo.getSysuserId();
        List userDepartmentList = this.getDepartmentList();
        HashMap<String, String> parentmap = new HashMap<String, String>();
        String currentstorageunitid = OpalUtil.getUniqueValues(move.getColumnValues(COLUMN_CURRENTSTORAGEUNITID, ";"), ";");
        StringBuilder sql = new StringBuilder();
        sql.append("select s1.storageunitid, s1.linksdcid, s1.linkkeyid1, s1.storageunittype,");
        sql.append(" ( select s2.storageunitid from storageunit s2 where s2.storageunitid = s1.parentid) parentstorageunitid,");
        sql.append(" ( select s2.linksdcid from storageunit s2 where s2.storageunitid = s1.parentid) parentlinksdcid, ");
        sql.append(" ( select s2.linkkeyid1 from storageunit s2 where s2.storageunitid = s1.parentid) parentlinkkeyid1");
        if (StringUtil.split(currentstorageunitid, ";").length > 750) {
            String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", currentstorageunitid, null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[TrackItemSDC.validateMovement] Unable to create RSET for " + move.getColumnValues(COLUMN_CURRENTSTORAGEUNITID, ";"));
            sql.append(" from storageunit s1, rsetitems r");
            sql.append(" where s1.storageunitid = r.keyid1");
            sql.append(" and r.rsetid = ?");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" from storageunit s1");
            sql.append(" where s1.storageunitid in (").append(safeSQL.addIn(currentstorageunitid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String storageunitid = ds.getValue(i, "storageunitid");
                if ("BoxPos".equals(ds.getValue(i, COLUMN_STORAGEUNITTYPE))) {
                    parentmap.put(storageunitid, ds.getValue(i, "parentstorageunitid"));
                    continue;
                }
                parentmap.put(storageunitid, storageunitid);
            }
        }
        HashMap trackitemdata = this.getTrackItemData(move);
        PackageCanReceiveSampleRule packageCanReceiveSampleRule = null;
        for (int i = 0; i < move.size(); ++i) {
            String packagestatus;
            String packagetype;
            String e;
            String trackitemid = move.getValue(i, COLUMN_TRACKITEMID);
            if (!trackitemdata.containsKey(trackitemid)) continue;
            boolean stabilityTI = false;
            if (stabilityTrackItems.findRow(COLUMN_TRACKITEMID, trackitemid) > -1) {
                stabilityTI = true;
            }
            HashMap datamap = (HashMap)trackitemdata.get(trackitemid);
            String linksdcid = (String)datamap.get(COLUMN_LINKSDCID);
            String linkkeyid1 = (String)datamap.get(COLUMN_LINKKEYID1);
            String custodialdomain = move.getValue(i, COLUMN_CUSTODIALDEPARTMENTID, (String)datamap.get(COLUMN_CUSTODIALDEPARTMENTID));
            String newStorageUnitId = move.getValue(i, COLUMN_CURRENTSTORAGEUNITID, "");
            String oldStorageUnitId = (String)datamap.get(COLUMN_CURRENTSTORAGEUNITID);
            String sdcname = OpalUtil.getSDCName(linksdcid);
            if (this.ASL && ("Sample".equals(linksdcid) || "LV_Box".equals(linksdcid))) {
                error.append(this.validateSDIMovement(datamap, userid, userDepartmentList, StringUtil.getLen(newStorageUnitId) > 0L));
            }
            if (StringUtil.getLen(newStorageUnitId) > 0L && (e = this.batchValidateTargetStorageUnit(qp, tp, newStorageUnitId = (String)parentmap.get(newStorageUnitId), userid, userDepartmentList, stabilityTI)).length() > 0) {
                throw new SapphireException("Validation", "VALIDATION", e);
            }
            if (this.ASL && StringUtil.getLen(oldStorageUnitId) > 0L) {
                String oldCustodialDepartmentID = this.getOldPrimaryValue(move, i, COLUMN_CUSTODIALDEPARTMENTID);
                if (this.isDepartmentCustodialDomain(oldCustodialDepartmentID) && StringUtil.getLen(oldCustodialDepartmentID) > 0L && !userDepartmentList.contains(oldCustodialDepartmentID)) {
                    error.append("{{User is not a member of the custodial department of}} ");
                    error.append(sdcname).append(" ").append(linkkeyid1);
                }
                if (StringUtil.getLen(newStorageUnitId) > 0L) {
                    String oldStorageLinkSDCId = this.getStorageUnitColumnValue(oldStorageUnitId, COLUMN_LINKSDCID);
                    String oldStorageLinkKeyid1 = this.getStorageUnitColumnValue(oldStorageUnitId, COLUMN_LINKKEYID1);
                    if (TRANSIT.equals(custodialdomain)) {
                        error.append(sdcname).append(" ").append(linkkeyid1);
                        error.append(" {{exists in}} ");
                        error.append(OpalUtil.getSDCName(oldStorageLinkSDCId)).append(" ").append(oldStorageLinkKeyid1);
                        error.append(" {{and is in Transit and cannot be moved.<BR>");
                    }
                    if ("LV_Package".equals(oldStorageLinkSDCId) && "LV_Package".equals(this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_LINKSDCID)) && !forceUpdate) {
                        confirm.append(sdcname).append(" ").append(linkkeyid1);
                        confirm.append(" {{exists in}} ");
                        if (StringUtil.getLen(oldStorageLinkSDCId) == 0L) {
                            confirm.append("<b>").append(this.getStorageUnitColumnValue(oldStorageUnitId, "labelpath")).append("</b>.<BR>");
                        } else {
                            confirm.append("<b>").append(OpalUtil.getSDCName(oldStorageLinkSDCId)).append(" ").append(oldStorageLinkKeyid1).append("</b>.<BR>");
                        }
                    }
                }
            }
            String storageUnitType = this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_STORAGEUNITTYPE);
            String newStorageSDCId = this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_LINKSDCID);
            String parentid = "";
            if (storageUnitType.equalsIgnoreCase("BoxPos")) {
                parentid = OpalUtil.getColumnValue(qp, "storageunit", COLUMN_PARENTID, "storageunitid=?", new String[]{newStorageUnitId});
                newStorageSDCId = this.getStorageUnitColumnValue(parentid, COLUMN_LINKSDCID);
            }
            if ("Sample".equals(linksdcid)) {
                String packageid;
                String sampleid = TrackItemSDC.getPrimaryValue(qp, move, i, COLUMN_LINKKEYID1);
                String sampleStatus = (String)datamap.get("samplestatus");
                String sampleStorageStatus = (String)datamap.get("samplestoragestatus");
                if ("Cancelled".equals(sampleStatus) || "Disposed".equals(sampleStatus) || "Cancelled".equals(sampleStorageStatus) || "Disposed".equals(sampleStorageStatus)) {
                    if ("LV_Package".equals(newStorageSDCId)) {
                        error.append("{{").append(sampleStatus).append(" sample can not be placed in a Package.}} (").append(sampleid).append(")<BR>");
                    } else {
                        error.append("{{").append(sampleStatus).append(" sample can not be filed.}} (").append(sampleid).append(")<BR>");
                    }
                }
                if ("BoxPos".equals(storageUnitType)) {
                    String boxid = this.getStorageUnitColumnValue(parentid, COLUMN_LINKKEYID1);
                    String activeFlag = OpalUtil.getColumnValue(qp, "s_box", "activeflag", "s_boxid=?", new String[]{boxid});
                    if (!NO.equals(activeFlag)) continue;
                    error.append("Sample can not be filed in a disposed box \"").append(boxid).append("\".<BR>");
                    continue;
                }
                if (!"LV_Package".equals(newStorageSDCId) && !"LV_Box".equals(newStorageSDCId)) continue;
                if (this.ASL && "Disposed".equals(sampleStorageStatus)) {
                    error.append("Sample \"").append(sampleid).append("\" is Disposed and can not be filed.<BR>");
                }
                if (!"LV_Package".equals(newStorageSDCId)) continue;
                if (this.ASL && "3rd Party Transfer".equals(sampleStorageStatus)) {
                    error.append("Sample \"").append(sampleid).append("\" is 3rd Party Transfer and can not be filed.<BR>");
                }
                if (StringUtil.getLen(packageid = this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_LINKKEYID1)) <= 0L || !this.ASL) continue;
                String packagestatus2 = this.getPackageProperty(packageid, "packagestatus");
                String senderdepartmentid = this.getPackageProperty(packageid, "senderdepartmentid");
                String recipientdepartmentid = this.getPackageProperty(packageid, "recipientdepartmentid");
                if (packageCanReceiveSampleRule == null) {
                    packageCanReceiveSampleRule = new PackageCanReceiveSampleRule(this.database, this.connectionInfo);
                }
                packageCanReceiveSampleRule.processRule(packageid, packagestatus2, senderdepartmentid, recipientdepartmentid);
                continue;
            }
            if ("LV_Box".equals(linksdcid)) {
                boolean TILSampleExist = false;
                if (this.ASL && this.connectionInfo.hasModule("SMS")) {
                    TILSampleExist = PackageValidate.isAnySampleTemporaryInLab(qp, "LV_Box", linkkeyid1);
                }
                if ("LV_Package".equals(newStorageSDCId)) {
                    String recipientDepartmentId;
                    String packageid = this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_LINKKEYID1);
                    if (!TILSampleExist || (recipientDepartmentId = this.getPackageProperty(packageid, "recipientdepartmentid")) == null || YES.equals(this.getPackageProperty(packageid, "recipientrepositoryflag"))) continue;
                    error.append("One or more samples in the box(es) are in status 'Temporary In Lab'<br> and package's (");
                    error.append(packageid);
                    error.append(") destination Custodial Department (");
                    error.append(recipientDepartmentId);
                    error.append(") is not a Repository.<BR>");
                    continue;
                }
                if (!"PhysicalStore".equals(newStorageSDCId)) continue;
                String storageKeyid1 = this.getStorageUnitColumnValue(newStorageUnitId, COLUMN_LINKKEYID1);
                String cd = PhysicalStore.getCustodialDepartmentId(this.getQueryProcessor(), storageKeyid1);
                if (cd == null) continue;
                move.addColumn(COLUMN_CUSTODIALDEPARTMENTID, 0);
                move.setValue(i, COLUMN_CUSTODIALDEPARTMENTID, cd);
                continue;
            }
            if (!"LV_Package".equals(linksdcid) || !"PKG".equals(packagetype = this.getPackageProperty(linkkeyid1, "packagetype")) || !"Shipped".equals(packagestatus = this.getPackageProperty(linkkeyid1, "packagestatus")) && !"Cancelled".equals(packagestatus)) continue;
            error.append("Cancelled or Shipped packages cannot be assigned to Storage");
        }
        if (error.length() > 0) {
            throw new SapphireException("TrackItem Validation", "VALIDATION", tp.translatePartial(error.toString()));
        }
        if (confirm.length() <= 0) return;
        confirm.append("{{Do you want to continue}}?");
        this.setError("Confirm movement", "CONFIRM", tp.translatePartial(confirm.toString()));
    }

    private boolean isDepartmentCustodialDomain(String departmentid) {
        if (!this.deptmap.containsKey(departmentid)) {
            this.deptmap.put(departmentid, Department.isCustodialDomain(this.getQueryProcessor(), departmentid) ? YES : NO);
        }
        return YES.equals(this.deptmap.get(departmentid));
    }

    private boolean isDepartmentRepository(String departmentid) {
        if (!this.deptrepomap.containsKey(departmentid)) {
            this.deptrepomap.put(departmentid, Department.isRepository(this.getQueryProcessor(), departmentid) ? YES : NO);
        }
        return YES.equals(this.deptrepomap.get(departmentid));
    }

    private String getPackageProperty(String packageid, String columnid) {
        HashMap map;
        if (!this.packagemap.containsKey(packageid)) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s_packageid, packagestatus, packagetype, senderdepartmentid, recipientdepartmentid");
            sql.append(", ( select department.repositoryflag from department where department.departmentid = s_package.recipientdepartmentid ) recipientrepositoryflag");
            sql.append(" from s_package");
            sql.append(" where s_packageid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
            if (ds != null && ds.size() > 0) {
                HashMap<String, String> map2 = new HashMap<String, String>();
                map2.put("s_packageid", ds.getValue(0, "s_packageid", ""));
                map2.put("packagestatus", ds.getValue(0, "packagestatus", ""));
                map2.put("packagetype", ds.getValue(0, "packagetype", ""));
                map2.put("senderdepartmentid", ds.getValue(0, "senderdepartmentid", ""));
                map2.put("recipientdepartmentid", ds.getValue(0, "recipientdepartmentid", ""));
                map2.put("recipientrepositoryflag", ds.getValue(0, "recipientrepositoryflag", NO));
                this.packagemap.put(packageid, map2);
            }
        }
        if ((map = (HashMap)this.packagemap.get(packageid)) != null && map.containsKey(columnid)) {
            return (String)map.get(columnid);
        }
        return "";
    }

    private HashMap getTrackItemData(DataSet primary) throws SapphireException {
        HashMap dataMap = new HashMap();
        if (primary.size() > 0) {
            DataSet ds;
            StringBuilder sql = new StringBuilder();
            sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.linkkeyid2, t.linkkeyid3, t.currentstorageunitid, t.custodialdepartmentid, t.custodialuserid, t.usecount,t.trackitemstatus,");
            sql.append(" su.storageunittype, su.linksdcid currentstoragesdcid, su.linkkeyid1 currentstoragekeyid1,");
            sql.append(" su.linkkeyid2 currentstoragekeyid2, su.linkkeyid3 currentstoragekeyid3,");
            sql.append(" su.moveableflag currentstoragemoveableflag, su.labelpath currentstoragelabelpath, t.linkkeyid1 sampleid,");
            sql.append(" su.maxtiallowed, (select count(tt.trackitemid) from trackitem tt where tt.currentstorageunitid = su.storageunitid) suticount,");
            sql.append(" s_sample.samplestatus, s_sample.storagestatus samplestoragestatus,");
            sql.append(" (select s_study.clinicalflag from s_study where s_study.s_studyid = s_sample.s_sampleid) studyclinicalflag,");
            sql.append(" linkedstorage.storageunitid linkstorageunitid, linkedstorage.moveableflag linkstoragemoveableflag,");
            sql.append(" (select reagentstatus from reagentlot where reagentlot.reagentlotid=t.linkkeyid1) reagentstatus");
            sql.append(" from trackitem t LEFT OUTER JOIN storageunit su ON t.currentstorageunitid = su.storageunitid");
            sql.append(" LEFT OUTER JOIN storageunit linkedstorage ON linkedstorage.LINKSDCID = t.LINKSDCID and linkedstorage.LINKKEYID1 = t.LINKKEYID1");
            sql.append(" LEFT OUTER JOIN s_sample ON s_sample.s_sampleid = t.linkkeyid1 and t.linksdcid = 'Sample'");
            if (primary.size() <= 750) {
                SafeSQL safeSQL = new SafeSQL();
                sql.append(" where t.trackitemid in (").append(safeSQL.addIn(primary.getColumnValues(COLUMN_TRACKITEMID, "','"))).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            } else {
                String rsetid = this.getDAMProcessor().createRSet(SDCID, primary.getColumnValues(COLUMN_TRACKITEMID, ";"), null, null);
                sql.append(" where t.trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    String trackitemid = ds.getValue(i, COLUMN_TRACKITEMID);
                    map.put(COLUMN_TRACKITEMID, trackitemid);
                    map.put(COLUMN_TRACKITEMSTATUS, ds.getValue(i, COLUMN_TRACKITEMSTATUS));
                    map.put(COLUMN_LINKSDCID, ds.getValue(i, COLUMN_LINKSDCID));
                    map.put(COLUMN_LINKKEYID1, ds.getValue(i, COLUMN_LINKKEYID1));
                    map.put(COLUMN_LINKKEYID2, ds.getValue(i, COLUMN_LINKKEYID2));
                    map.put(COLUMN_LINKKEYID3, ds.getValue(i, COLUMN_LINKKEYID3));
                    map.put(COLUMN_CURRENTSTORAGEUNITID, ds.getValue(i, COLUMN_CURRENTSTORAGEUNITID));
                    map.put(COLUMN_CUSTODIALDEPARTMENTID, ds.getValue(i, COLUMN_CUSTODIALDEPARTMENTID));
                    map.put(COLUMN_CUSTODIALUSERID, ds.getValue(i, COLUMN_CUSTODIALUSERID));
                    map.put("currentstoragesdcid", ds.getValue(i, "currentstoragesdcid"));
                    map.put("currentstoragekeyid1", ds.getValue(i, "currentstoragekeyid1"));
                    map.put("currentstoragekeyid2", ds.getValue(i, "currentstoragekeyid2"));
                    map.put("currentstoragekeyid3", ds.getValue(i, "currentstoragekeyid3"));
                    map.put("currentstoragemoveableflag", ds.getValue(i, "currentstoragemoveableflag"));
                    map.put("currentstoragelabelpath", ds.getValue(i, "currentstoragelabelpath"));
                    map.put("samplestatus", ds.getValue(i, "samplestatus"));
                    map.put("samplestoragestatus", ds.getValue(i, "samplestoragestatus"));
                    map.put("studyclinicalflag", ds.getValue(i, "studyclinicalflag"));
                    map.put("sampleid", ds.getValue(i, "sampleid"));
                    map.put("currentstorageunittype", ds.getValue(i, COLUMN_STORAGEUNITTYPE));
                    map.put("maxtiallowed", new Integer(ds.getInt(i, "maxtiallowed", 0)));
                    map.put("suticount", new Integer(ds.getInt(i, "suticount", 0)));
                    map.put("linkstorageunitid", ds.getString(i, "linkstorageunitid", ""));
                    map.put("linkstoragemoveableflag", ds.getString(i, "linkstoragemoveableflag", ""));
                    map.put("reagentstatus", ds.getString(i, "reagentstatus", ""));
                    dataMap.put(trackitemid, map);
                }
            }
        }
        return dataMap;
    }

    public static HashMap getStorageUnitInfoByTrackItem(QueryProcessor qp, String trackitemid) {
        HashMap<String, String> map = new HashMap<String, String>();
        StringBuilder sql = new StringBuilder();
        sql.append("select su.storageunitid, su.moveableflag, su.linksdcid, su.linkkeyid1, su.storageunitlabel");
        sql.append(" from storageunit su, trackitem ti");
        sql.append(" where ti.trackitemid = ?");
        sql.append(" and su.linksdcid = ti.linksdcid");
        sql.append(" and su.linkkeyid1 = ti.linkkeyid1");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{trackitemid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                map.put("storageunitid", ds.getValue(i, "storageunitid"));
                map.put("moveableflag", ds.getValue(i, "moveableflag"));
                map.put(COLUMN_STORAGEUNITLABEL, ds.getValue(i, COLUMN_STORAGEUNITLABEL));
            }
        }
        return map;
    }

    private HashMap getStorageUnitInfo(String storageunitid) {
        if (!this.suinfomap.containsKey(storageunitid)) {
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, labelpath from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                map.put(COLUMN_LINKSDCID, ds.getValue(0, COLUMN_LINKSDCID));
                map.put(COLUMN_LINKKEYID1, ds.getValue(0, COLUMN_LINKKEYID1));
                map.put(COLUMN_LINKKEYID2, ds.getValue(0, COLUMN_LINKKEYID2));
                map.put(COLUMN_LINKKEYID3, ds.getValue(0, COLUMN_LINKKEYID3));
                map.put("labelpath", ds.getValue(0, "labelpath"));
            }
            this.suinfomap.put(storageunitid, map);
        }
        return (HashMap)this.suinfomap.get(storageunitid);
    }

    private List<Map<String, String>> getChildTrackItems(String storageunitid) {
        String sql;
        ArrayList<Map<String, String>> trackitems = new ArrayList<Map<String, String>>();
        if (this.connectionInfo.isOracle()) {
            sql = "select t.trackitemid, t.custodialuserid, t.custodialdepartmentid, su.storageunitid, su.storageunitlabel, su.labelpath, su.linksdcid, su.linkkeyid1, t.trackitemid, t.linksdcid tilinksdcid, t.linkkeyid1 tilinkkeyid1 from trackitem t, storageunit su where t.currentstorageunitid in (select s.storageunitid from storageunit s connect by prior s.storageunitid = s.parentid start with s.storageunitid = ?) and su.storageunitid = t.currentstorageunitid";
        } else {
            sql = "WITH storageunittree (storageunitid)";
            sql = sql + " AS";
            sql = sql + " (";
            sql = sql + "   SELECT su.storageunitid";
            sql = sql + "   FROM storageunit AS su";
            sql = sql + "   WHERE su.storageunitid = ?";
            sql = sql + "   UNION ALL";
            sql = sql + "   SELECT su.storageunitid";
            sql = sql + "   FROM storageunit AS su";
            sql = sql + "   INNER JOIN StorageUnitTree AS d";
            sql = sql + "   ON su.parentid = d.storageunitid";
            sql = sql + " )";
            sql = sql + " select t.trackitemid, t.custodialuserid, t.custodialdepartmentid, su.storageunitid, su.storageunitlabel, su.labelpath, su.linksdcid, su.linkkeyid1, t.trackitemid, t.linksdcid tilinksdcid, t.linkkeyid1 tilinkkeyid1 from trackitem t, storageunit su where t.currentstorageunitid in (select storageunittree.storageunitid from storageunittree) and su.storageunitid = t.currentstorageunitid";
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String trackitemid = ds.getString(i, COLUMN_TRACKITEMID);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_TRACKITEMID, trackitemid);
                map.put(COLUMN_STORAGEUNITLABEL, ds.getString(i, COLUMN_STORAGEUNITLABEL, ""));
                map.put(COLUMN_CURRENTSTORAGEUNITID, ds.getString(i, "storageunitid", ""));
                map.put("labelpath", ds.getString(i, "labelpath", ""));
                map.put(COLUMN_LINKSDCID, ds.getString(i, COLUMN_LINKSDCID, ""));
                map.put(COLUMN_LINKKEYID1, ds.getString(i, COLUMN_LINKKEYID1, ""));
                map.put(COLUMN_LINKKEYID2, ds.getString(i, COLUMN_LINKKEYID2, ""));
                map.put(COLUMN_LINKKEYID3, ds.getString(i, COLUMN_LINKKEYID3, ""));
                map.put("tilinksdcid", ds.getString(i, "tilinksdcid", ""));
                map.put("tilinkkeyid1", ds.getString(i, "tilinkkeyid1", ""));
                map.put("tilinkkeyid2", ds.getString(i, "tilinkkeyid2", ""));
                map.put("tilinkkeyid3", ds.getString(i, "tilinkkeyid3", ""));
                map.put(COLUMN_CUSTODIALDEPARTMENTID, ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID, ""));
                trackitems.add(map);
            }
        }
        return trackitems;
    }

    private void processCustodyRule(DataSet primary, PropertyList actionProps, HashMap trackitemdata, DataSet stabilityTrackItems) throws SapphireException {
        if ("New Box Added".equals(actionProps.getProperty("auditreason"))) {
            return;
        }
        boolean forceUpdate = YES.equalsIgnoreCase(actionProps.getProperty("__sdcruleconfirm"));
        ArrayList<String> sampleSet = new ArrayList<String>();
        HashMap<String, Integer> trackitemMap = new HashMap<String, Integer>();
        HashSet<String> currentStorageSet = new HashSet<String>();
        DataSet storageCustodyModifiedSet = new DataSet();
        HashSet<String> boxReserveRuleSet = new HashSet<String>();
        String tracelogid = primary != null && primary.size() > 0 ? primary.getString(0, "tracelogid", "") : "";
        for (int i = 0; i < primary.size(); ++i) {
            HashMap datamap;
            String trackitemid = primary.getValue(i, COLUMN_TRACKITEMID);
            if (stabilityTrackItems.findRow(COLUMN_TRACKITEMID, trackitemid) > -1 || trackitemid == null || trackitemid.length() <= 0 || (datamap = (HashMap)trackitemdata.get(trackitemid)) == null || datamap.size() <= 0) continue;
            if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID) || this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) {
                String custodialUserid = (String)datamap.get(COLUMN_CUSTODIALUSERID);
                String custodialDepartmentid = (String)datamap.get(COLUMN_CUSTODIALDEPARTMENTID);
                if (StringUtil.getLen(custodialUserid) > 0L && StringUtil.getLen(custodialDepartmentid) > 0L && this.isDepartmentCustodialDomain(custodialDepartmentid) && !this.getUserDepartments(custodialUserid).contains(custodialDepartmentid)) {
                    throw new SapphireException("TI Custody Rule", "VALIDATION", "User must belong to custodial department \"" + custodialDepartmentid + "\"");
                }
            }
            String linksdcid = (String)datamap.get(COLUMN_LINKSDCID);
            String linkkeyid1 = (String)datamap.get(COLUMN_LINKKEYID1);
            String linkstorageunitid = (String)datamap.get("linkstorageunitid");
            if (OpalUtil.isNotEmpty(linkstorageunitid)) {
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID) || this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALDEPARTMENTID)) {
                    int row = storageCustodyModifiedSet.addRow();
                    storageCustodyModifiedSet.setString(row, "storageunitid", linkstorageunitid);
                }
                if (!"LV_Box".equalsIgnoreCase(linksdcid)) continue;
                if (this.connectionInfo.hasModule("SMS")) {
                    new GLPRule(this.database, this.connectionInfo).processBoxGLPRule(linkkeyid1, forceUpdate);
                }
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CUSTODIALUSERID) && !this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID)) continue;
                boxReserveRuleSet.add(linkkeyid1);
                continue;
            }
            if ("Sample".equalsIgnoreCase(linksdcid)) {
                sampleSet.add(linkkeyid1);
                continue;
            }
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID) || primary.getString(i, COLUMN_CURRENTSTORAGEUNITID, "").length() <= 0) continue;
            trackitemMap.put(trackitemid, i);
            currentStorageSet.add(primary.getString(i, COLUMN_CURRENTSTORAGEUNITID));
        }
        if (boxReserveRuleSet.size() > 0) {
            BoxReservationFlushRule rule = new BoxReservationFlushRule(this.database, this.connectionInfo);
            rule.processRule(boxReserveRuleSet, forceUpdate);
        }
        if (sampleSet.size() > 0) {
            new GLPRule(this.database, this.connectionInfo).processRule(sampleSet, forceUpdate);
            new SampleStateRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", "")).processRule(sampleSet, forceUpdate);
        }
        if (trackitemMap.size() > 0) {
            DataSet ds;
            String sql;
            if (currentStorageSet.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", OpalUtil.toDelimitedString(currentStorageSet, ";"), null, null);
                sql = "select su.storageunitid, su.parentid, s_physicalstore.departmentid, trackitem.custodialuserid, trackitem.custodialdepartmentid,  (select p.departmentid from s_physicalstore p where p.s_physicalstoreid = pu.linkkeyid1 and pu.linksdcid = 'PhysicalStore') parentdepartmentid,  (select pt.custodialuserid from trackitem pt where pt.linksdcid = pu.linksdcid and pt.linkkeyid1 = pu.linkkeyid1) parentcustodialuserid  from storageunit su    left outer join s_physicalstore on su.linksdcid = 'PhysicalStore' and su.linkkeyid1 = s_physicalstore.s_physicalstoreid    left outer join trackitem on trackitem.linksdcid = su.linksdcid and trackitem.linkkeyid1 = su.linkkeyid1    left outer join storageunit pu on pu.storageunitid = su.parentid  where su.storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql = "select su.storageunitid, su.parentid, s_physicalstore.departmentid, trackitem.custodialuserid, trackitem.custodialdepartmentid,  (select p.departmentid from s_physicalstore p where p.s_physicalstoreid = pu.linkkeyid1 and pu.linksdcid = 'PhysicalStore') parentdepartmentid,  (select pt.custodialuserid from trackitem pt where pt.linksdcid = pu.linksdcid and pt.linkkeyid1 = pu.linkkeyid1) parentcustodialuserid  from storageunit su    left outer join s_physicalstore on su.linksdcid = 'PhysicalStore' and su.linkkeyid1 = s_physicalstore.s_physicalstoreid    left outer join trackitem on trackitem.linksdcid = su.linksdcid and trackitem.linkkeyid1 = su.linkkeyid1    left outer join storageunit pu on pu.storageunitid = su.parentid  where su.storageunitid in (" + safeSQL.addIn(currentStorageSet) + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (ds != null) {
                HashMap storageDepartmentMap = new HashMap();
                for (int i = 0; i < ds.size(); ++i) {
                    String storageunitid = ds.getString(i, "storageunitid");
                    String departmentid = ds.getString(i, "departmentid", "");
                    String custodialdepartmentid = ds.getString(i, COLUMN_CUSTODIALDEPARTMENTID, "");
                    String parentdepartmentid = ds.getString(i, "parentdepartmentid", "");
                    String storagedepartment = "";
                    if (departmentid.length() > 0) {
                        storagedepartment = departmentid;
                    } else if (custodialdepartmentid.length() > 0) {
                        storagedepartment = custodialdepartmentid;
                    } else if (parentdepartmentid.length() > 0) {
                        storagedepartment = parentdepartmentid;
                    } else {
                        String parentid = ds.getString(i, COLUMN_PARENTID, "");
                        if (parentid.length() > 0) {
                            storagedepartment = StorageUnitUtil.getPhysicalStoreDepartment(this.getQueryProcessor(), parentid, this.getConnectionProcessor().isOra());
                        }
                    }
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put(COLUMN_CUSTODIALDEPARTMENTID, storagedepartment);
                    props.put(COLUMN_CUSTODIALUSERID, ds.getString(i, COLUMN_CUSTODIALUSERID, ds.getString(i, "parentcustodialuserid", "")));
                    storageDepartmentMap.put(storageunitid, props);
                }
                DataSet custodyUpdateDS = new DataSet();
                for (String trackitemid : trackitemMap.keySet()) {
                    int row = (Integer)trackitemMap.get(trackitemid);
                    String currentstorageunitid = primary.getString(row, COLUMN_CURRENTSTORAGEUNITID);
                    Map map = (Map)storageDepartmentMap.get(currentstorageunitid);
                    if (map == null) continue;
                    String storagecustodialuserid = (String)map.get(COLUMN_CUSTODIALUSERID);
                    String storagecustodialdepartmentid = (String)map.get(COLUMN_CUSTODIALDEPARTMENTID);
                    String custodialuserid = primary.getString(row, COLUMN_CUSTODIALUSERID, this.getOldPrimaryValue(primary, row, COLUMN_CUSTODIALUSERID));
                    String custodialdepartmentid = primary.getString(row, COLUMN_CUSTODIALDEPARTMENTID, this.getOldPrimaryValue(primary, row, COLUMN_CUSTODIALDEPARTMENTID));
                    custodialuserid = custodialuserid == null ? "" : custodialuserid;
                    String string = custodialdepartmentid = custodialdepartmentid == null ? "" : custodialdepartmentid;
                    if (custodialuserid.equals(storagecustodialuserid) && custodialdepartmentid.equals(storagecustodialdepartmentid)) continue;
                    int dsrow = custodyUpdateDS.addRow();
                    custodyUpdateDS.setString(dsrow, COLUMN_TRACKITEMID, trackitemid);
                    custodyUpdateDS.setString(dsrow, COLUMN_CUSTODIALUSERID, storagecustodialuserid);
                    custodyUpdateDS.setString(dsrow, COLUMN_CUSTODIALDEPARTMENTID, storagecustodialdepartmentid);
                }
                if (custodyUpdateDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", SDCID);
                    props.setProperty("keyid1", custodyUpdateDS.getColumnValues(COLUMN_TRACKITEMID, ";"));
                    props.setProperty(COLUMN_CUSTODIALUSERID, custodyUpdateDS.getColumnValues(COLUMN_CUSTODIALUSERID, ";"));
                    props.setProperty(COLUMN_CUSTODIALDEPARTMENTID, custodyUpdateDS.getColumnValues(COLUMN_CUSTODIALDEPARTMENTID, ";"));
                    props.setProperty("__sdcruleignore", YES);
                    props.setProperty("tracelogid", tracelogid);
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
        }
        if (storageCustodyModifiedSet.size() > 0) {
            new StorageCustodyModifiedRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", "")).processRule(storageCustodyModifiedSet);
        }
    }

    private void checkOutTI(DataSet primary, PropertyList actionProps) {
        String reserveLocation = actionProps.getProperty("reservelocation");
        String forceUpdate = actionProps.getProperty("__sdcruleconfirm");
        String auditReason = actionProps.getProperty("auditreason");
        String auditActivity = actionProps.getProperty("auditactivity", "");
        String auditSignedFlag = actionProps.getProperty("auditsignedflag", NO);
        StringBuilder reserveTI = new StringBuilder();
        StringBuilder reserveCurrSU = new StringBuilder();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID) || !YES.equalsIgnoreCase(reserveLocation)) continue;
            reserveTI.append(";").append(primary.getValue(i, COLUMN_TRACKITEMID));
            reserveCurrSU.append(";").append(this.getOldPrimaryValue(primary, i, COLUMN_CURRENTSTORAGEUNITID));
        }
        if (reserveTI.length() > 0) {
            try {
                TrackItemSDC.reserveTrackItem(this.getActionProcessor(), reserveTI.substring(1), reserveCurrSU.substring(1), forceUpdate, auditReason, auditActivity, auditSignedFlag);
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
    }

    private static String getPrimaryValue(QueryProcessor qp, DataSet primary, int row, String columnid) {
        String value = primary.isValidColumn(columnid) ? primary.getValue(row, columnid) : OpalUtil.getColumnValue(qp, "trackitem", columnid, "trackitemid=?", new String[]{primary.getValue(row, COLUMN_TRACKITEMID)});
        return value == null ? "" : value;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    public static void reserveTrackItem(ActionProcessor ap, String trackItemId, String currSU, String __sdcruleconfirm, String auditreason, String auditActivity, String auditSignedFlag) throws ActionException {
        HashMap<String, String> props = new HashMap<String, String>();
        if (__sdcruleconfirm != null && __sdcruleconfirm.trim().length() > 0) {
            props.put("__sdcruleconfirm", __sdcruleconfirm);
        }
        props.put(COLUMN_TRACKITEMID, trackItemId);
        props.put("storageunitid", currSU);
        props.put("propsmatch", YES);
        props.put("auditreason", auditreason);
        props.put("auditactivity", auditActivity);
        props.put("auditsignedflag", auditSignedFlag);
        ap.processAction("ReserveTrackItem", "1", props);
    }

    public static DataSet getTrackItemsInSU(QueryProcessor queryProcessor, String sdcid, String sdiid) {
        StringBuilder sql = new StringBuilder();
        sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.linkkeyid2, t.linkkeyid3, t.currentstorageunitid");
        sql.append(" from trackitem t");
        sql.append(" where t.currentstorageunitid = (select storageunitid s");
        sql.append(" from storageunit s");
        sql.append(" where s.linksdcid = ?");
        sql.append(" and s.linkkeyid1 = ?)");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sdcid, sdiid});
        if ("LV_Box".equals(sdcid) || "Plate".equals(sdcid)) {
            sql.setLength(0);
            sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.linkkeyid2, t.linkkeyid3, t.currentstorageunitid");
            sql.append(" from trackitem t");
            sql.append(" where t.currentstorageunitid in (select s1.storageunitid ");
            sql.append(" from storageunit s1 where s1.parentid = (select s2.storageunitid from storageunit s2 where s2.linksdcid = ? and s2.linkkeyid1 = ?))");
            DataSet dsBox = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sdcid, sdiid});
            if (dsBox != null) {
                for (int i = 0; i < dsBox.size(); ++i) {
                    ds.copyRow(dsBox, i, 1);
                }
            }
        }
        return ds;
    }

    public static DataSet getTIDetailsByLinkKeyid(DBAccess database, String linksdcid, String linkkeyid1, boolean alltiflag) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT TRACKITEMID, CUSTODIALUSERID, CURRENTSTORAGEUNITID, CUSTODIALDEPARTMENTID");
        sql.append(" FROM TRACKITEM ");
        sql.append(" WHERE LINKSDCID = ").append(safeSQL.addVar(linksdcid));
        sql.append(" AND LINKKEYID1 = ").append(safeSQL.addVar(linkkeyid1));
        if (!alltiflag) {
            sql.append(" AND ( CUSTODIALUSERID IS NOT NULL OR CURRENTSTORAGEUNITID IS NOT NULL )");
        }
        database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet();
        ds.setResultSet(database.getResultSet());
        if (ds != null && ds.size() > 0 && !alltiflag && ds.size() > 1) {
            StringBuffer msg = new StringBuffer();
            msg.append(linksdcid).append("  ").append(linkkeyid1).append(" has more than one trackitem either with custodians or in storage unit.");
            throw new SapphireException(msg.toString());
        }
        database.closeResultSet();
        return ds;
    }

    public static void setCustodyDetails(ConnectionInfo connectionInfo, String trackitemid, PropertyList editTrackItemProps) throws ActionException {
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        editTrackItemProps.setProperty(COLUMN_TRACKITEMID, trackitemid);
        ap.processActionClass(EditTrackItem.class.getName(), editTrackItemProps);
    }

    private String getStorageUnitColumnValue(String storageunitid, String columnid) {
        HashMap map;
        if (this.storageUnitMap == null) {
            this.storageUnitMap = new HashMap();
        }
        if (!this.storageUnitMap.containsKey(storageunitid)) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s.storageunitid, s.storageunittype, s.linksdcid, s.linkkeyid1, s.parentid, s.storageunitlabel, s.labelpath,");
            sql.append(" (select p.linksdcid from storageunit p where p.storageunitid = s.parentid ) parentsdcid,");
            sql.append(" (select p.linkkeyid1 from storageunit p where p.storageunitid = s.parentid ) parentkeyid1");
            sql.append(" from storageunit s");
            sql.append(" where s.storageunitid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                HashMap<String, String> map2 = new HashMap<String, String>();
                String linksdcid = ds.getValue(0, COLUMN_LINKSDCID);
                String linkkeyid1 = ds.getValue(0, COLUMN_LINKKEYID1);
                if (StringUtil.getLen(linksdcid) == 0L) {
                    linksdcid = ds.getValue(0, "parentsdcid");
                    linkkeyid1 = ds.getValue(0, "parentkeyid1");
                }
                map2.put("storageunitid", ds.getValue(0, "storageunitid"));
                map2.put(COLUMN_STORAGEUNITTYPE, ds.getValue(0, COLUMN_STORAGEUNITTYPE));
                map2.put(COLUMN_STORAGEUNITLABEL, ds.getValue(0, COLUMN_STORAGEUNITLABEL));
                map2.put("labelpath", ds.getValue(0, "labelpath"));
                map2.put(COLUMN_LINKSDCID, linksdcid);
                map2.put(COLUMN_LINKKEYID1, linkkeyid1);
                this.storageUnitMap.put(storageunitid, map2);
            }
        }
        if ((map = (HashMap)this.storageUnitMap.get(storageunitid)) != null && map.containsKey(columnid)) {
            return (String)map.get(columnid);
        }
        return "";
    }

    private void updateSpaceAvailableAndStatus(DataSet primary, HashSet<String> storageUnitSet, PropertyList actionProps) throws SapphireException {
        boolean receivePackage = actionProps != null && YES.equals(actionProps.getProperty("__takecustodyoperation"));
        HashSet<Object> storageunits = new HashSet();
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_CURRENTSTORAGEUNITID)) continue;
                storageunits.add(primary.getValue(i, COLUMN_CURRENTSTORAGEUNITID));
                storageunits.add(this.getOldPrimaryValue(primary, i, COLUMN_CURRENTSTORAGEUNITID));
            }
        } else if (storageUnitSet != null) {
            storageunits = storageUnitSet;
        }
        if (storageunits.size() > 0) {
            DataSet spaceAvail = new DataSet();
            spaceAvail.addColumn("storageunitid", 0);
            spaceAvail.addColumn("spaceavailflag", 0);
            DataSet boxds = new DataSet();
            boxds.addColumn("boxid", 0);
            boxds.addColumn("status", 0);
            HashSet<String> boxes = new HashSet<String>();
            HashSet<String> receivedPackages = new HashSet<String>();
            HashSet<String> emptiedPackages = new HashSet<String>();
            HashSet<String> cancelledPackages = new HashSet<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select storageunitid, storageunittype, maxtiallowed, linksdcid, linkkeyid1, parentid, spaceavailflag, propertytreeid,");
            sql.append(" (select p.packagetype from s_package p where p.s_packageid = linkkeyid1) packagetype,");
            sql.append(" (select p.packagestatus from s_package p where p.s_packageid = linkkeyid1) packagestatus,");
            sql.append(" (select p.senderdepartmentid from s_package p where p.s_packageid = linkkeyid1) senderdepartmentid,");
            sql.append(" (select p.recipientdepartmentid from s_package p where p.s_packageid = linkkeyid1) recipientdepartmentid,");
            sql.append(" (select b.boxstatus from s_box b where b.s_boxid = linkkeyid1) boxstatus,");
            sql.append(" (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = storageunit.storageunitid) childticount,");
            sql.append(" (select s.linksdcid from storageunit s where s.storageunitid = storageunit.parentid) parentsdcid,");
            sql.append(" (select s.linkkeyid1 from storageunit s where s.storageunitid = storageunit.parentid) parentkeyid1");
            sql.append(" from storageunit");
            sql.append(" where storageunitid in ( [] )");
            DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "StorageUnitSDC", sql.toString(), OpalUtil.toDelimitedString(storageunits, ";"));
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    String storageunitid = ds.getValue(i, "storageunitid");
                    int maxtiallowed = ds.getInt(i, "maxtiallowed", 0);
                    int childticount = ds.getInt(i, "childticount", 0);
                    String spaceavailflag = ds.getValue(i, "spaceavailflag", NO);
                    if (maxtiallowed > 0) {
                        if (primary == null && storageUnitSet != null) {
                            --childticount;
                        }
                        if (childticount >= maxtiallowed && YES.equals(spaceavailflag)) {
                            int row = spaceAvail.addRow();
                            spaceAvail.setValue(row, "storageunitid", storageunitid);
                            spaceAvail.setValue(row, "spaceavailflag", NO);
                        } else if (childticount < maxtiallowed && NO.equals(spaceavailflag)) {
                            int row = spaceAvail.addRow();
                            spaceAvail.setValue(row, "storageunitid", storageunitid);
                            spaceAvail.setValue(row, "spaceavailflag", YES);
                        }
                    }
                    String linksdcid = ds.getValue(i, COLUMN_LINKSDCID);
                    String linkkeyid1 = ds.getValue(i, COLUMN_LINKKEYID1);
                    String parentsdcid = ds.getValue(i, "parentsdcid");
                    if ("LV_Box".equals(linksdcid)) {
                        String boxstatus = ds.getValue(i, "boxstatus");
                        if (maxtiallowed == -1) {
                            if (childticount > 0) {
                                if ("Partial".equals(boxstatus)) continue;
                                int row = boxds.addRow();
                                boxds.setValue(row, "boxid", linkkeyid1);
                                boxds.setValue(row, "status", "Partial");
                                continue;
                            }
                            if ("Empty".equals(boxstatus)) continue;
                            int row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Empty");
                            continue;
                        }
                        if (childticount <= 0) {
                            if ("Empty".equals(boxstatus)) continue;
                            int row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Empty");
                            continue;
                        }
                        if (childticount < maxtiallowed) {
                            if ("Partial".equals(boxstatus)) continue;
                            int row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Partial");
                            continue;
                        }
                        if (childticount < maxtiallowed || "Full".equals(boxstatus)) continue;
                        int row = boxds.addRow();
                        boxds.setValue(row, "boxid", linkkeyid1);
                        boxds.setValue(row, "status", "Full");
                        continue;
                    }
                    if ("LV_Package".equals(linksdcid)) {
                        String packageStatus = ds.getValue(i, "packagestatus");
                        if (receivePackage) {
                            if ("CDT".equals(ds.getValue(i, "packagetype", "PKG"))) {
                                String custodialdepartmentid = actionProps != null ? actionProps.getProperty(COLUMN_CUSTODIALDEPARTMENTID) : "";
                                String senderdepartmentid = ds.getString(i, "senderdepartmentid", "");
                                String recipientdepartmentid = ds.getString(i, "recipientdepartmentid", "");
                                if (custodialdepartmentid.equals(recipientdepartmentid)) {
                                    if (childticount > 0) {
                                        receivedPackages.add(linkkeyid1);
                                        continue;
                                    }
                                    receivedPackages.add(linkkeyid1);
                                    emptiedPackages.add(linkkeyid1);
                                    continue;
                                }
                                if (!custodialdepartmentid.equals(senderdepartmentid) || childticount != 0) continue;
                                cancelledPackages.add(linkkeyid1);
                                continue;
                            }
                            if (!"Shipped".equals(packageStatus) && !"Expected".equals(packageStatus)) continue;
                            if (childticount > 0) {
                                receivedPackages.add(linkkeyid1);
                                continue;
                            }
                            receivedPackages.add(linkkeyid1);
                            emptiedPackages.add(linkkeyid1);
                            continue;
                        }
                        if (!"Received".equals(packageStatus) || childticount > 0) continue;
                        emptiedPackages.add(linkkeyid1);
                        continue;
                    }
                    if (!"LV_Box".equals(parentsdcid)) continue;
                    boxes.add(ds.getValue(i, "parentkeyid1"));
                }
            }
            if (actionProps == null) {
                actionProps = new PropertyList();
            }
            if (spaceAvail.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", spaceAvail.getColumnValues("storageunitid", ";"));
                props.setProperty("spaceavailflag", spaceAvail.getColumnValues("spaceavailflag", ";"));
                props.setProperty("__sdcruleconfirm", YES);
                props.setProperty("__syncoperation", YES);
                props.setProperty("propsmatch", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (receivedPackages.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_Package");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(receivedPackages, ";"));
                props.setProperty("packagestatus", "Received");
                props.setProperty("__sdcruleconfirm", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (emptiedPackages.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_Package");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(emptiedPackages, ";"));
                props.setProperty("packagestatus", "Emptied");
                props.setProperty("__sdcruleconfirm", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (cancelledPackages.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_Package");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(cancelledPackages, ";"));
                props.setProperty("packagestatus", "Cancelled");
                props.setProperty("__sdcruleconfirm", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (boxes.size() > 0) {
                for (String boxid : boxes) {
                    String boxstatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_box", "boxstatus", "s_boxid = ?", new String[]{boxid});
                    sql.setLength(0);
                    sql.append("select storageunitid, spaceavailflag");
                    sql.append(" from storageunit");
                    sql.append(" where parentid = ( select su.storageunitid from storageunit su");
                    sql.append(" where su.linksdcid = 'LV_Box'");
                    sql.append(" and su.linkkeyid1 = ?)");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{boxid});
                    if (ds == null) continue;
                    int countN = 0;
                    int countY = 0;
                    for (int i = 0; i < ds.size(); ++i) {
                        if (NO.equals(ds.getValue(i, "spaceavailflag", NO))) {
                            ++countN;
                            continue;
                        }
                        ++countY;
                    }
                    String newboxstatus = "Empty";
                    if (countN > 0 && countY > 0) {
                        newboxstatus = "Partial";
                    } else if (countY == 0) {
                        newboxstatus = "Full";
                    }
                    if (newboxstatus.equals(boxstatus)) continue;
                    int row = boxds.addRow();
                    boxds.setValue(row, "boxid", boxid);
                    boxds.setValue(row, "status", newboxstatus);
                }
            }
            if (boxds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_Box");
                props.setProperty("keyid1", boxds.getColumnValues("boxid", ";"));
                props.setProperty("boxstatus", boxds.getColumnValues("status", ";"));
                props.setProperty("__overrule", YES);
                props.setProperty("__sdcruleconfirm", YES);
                props.setProperty("propsmatch", YES);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }

    private void syncSampleStatus(DataSet primary, HashMap trackitemdata, boolean forceUpdate, PropertyList actionProps) throws ActionException {
        if (primary != null) {
            QueryProcessor qp = this.getQueryProcessor();
            StringBuilder sb = new StringBuilder();
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            for (int i = 0; i < primary.size(); ++i) {
                HashMap map;
                String sdcid;
                String trackitemid = primary.getValue(i, COLUMN_TRACKITEMID);
                if (!trackitemdata.containsKey(trackitemid) || !"Sample".equals(sdcid = (String)(map = (HashMap)trackitemdata.get(trackitemid)).get(COLUMN_LINKSDCID))) continue;
                String keyid1 = (String)map.get(COLUMN_LINKKEYID1);
                String currentstorageunitid = (String)map.get(COLUMN_CURRENTSTORAGEUNITID);
                String storageStatus = (String)map.get("samplestoragestatus");
                String custodialuserid = (String)map.get(COLUMN_CUSTODIALUSERID);
                if ("Temporary In Lab".equals(storageStatus)) {
                    String custodialDepartmentID;
                    if (StringUtil.getLen(currentstorageunitid) <= 0L) continue;
                    String storageunitid = this.getStorageNodeBySDC(qp, currentstorageunitid, "PhysicalStore");
                    if (StringUtil.getLen(storageunitid) > 0L) {
                        String physicalStoreID = this.getLinkKeyid1ByStorageUnitId(qp, storageunitid);
                        if (StringUtil.getLen(physicalStoreID) <= 0L || !this.isPhysicalStoreRepository(qp, physicalStoreID)) continue;
                        sb.append(";").append(keyid1);
                        continue;
                    }
                    storageunitid = this.getStorageNodeBySDC(qp, currentstorageunitid, "LV_Box");
                    String boxid = this.getLinkKeyid1ByStorageUnitId(qp, storageunitid);
                    if (StringUtil.getLen(boxid) <= 0L || StringUtil.getLen(custodialDepartmentID = this.getBoxCustodialDepartment(boxid)) <= 0L || !this.isDepartmentRepository(custodialDepartmentID)) continue;
                    sb.append(";").append(keyid1);
                    continue;
                }
                if (!"Allocated".equals(storageStatus) || !sysuserid.equals(custodialuserid)) continue;
                sb.append(";").append(keyid1);
            }
            if (sb.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sb.substring(1));
                props.setProperty("storagestatus", "Received");
                props.setProperty("receiveddt", "n");
                props.setProperty("receivedby", sysuserid);
                props.setProperty("__sdcruleconfirm", forceUpdate ? YES : NO);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }

    private String getBoxCustodialDepartment(String boxid) {
        if (!this.boxCDMap.containsKey(boxid)) {
            this.boxCDMap.put(boxid, LV_Box.getCustodialDepartmentId(this.getQueryProcessor(), boxid));
        }
        return (String)this.boxCDMap.get(boxid);
    }

    private String getStorageNodeBySDC(QueryProcessor qp, String storageunitid, String sdcid) {
        String value;
        String key = storageunitid + sdcid;
        if (!this.storagenodebysdcmap.containsKey(key)) {
            this.storagenodebysdcmap.put(key, StorageUnitSDC.getStorageNodeBySDC(qp, storageunitid, sdcid));
        }
        return (value = (String)this.storagenodebysdcmap.get(key)) == null ? "" : value;
    }

    private String getLinkKeyid1ByStorageUnitId(QueryProcessor qp, String storageunitid) {
        String value;
        if (!this.linkmap.containsKey(storageunitid)) {
            this.linkmap.put(storageunitid, StorageUnitSDC.getLinkKeyid1ByStorageUnitId(qp, storageunitid));
        }
        return (value = (String)this.linkmap.get(storageunitid)) == null ? "" : value;
    }

    private boolean isPhysicalStoreRepository(QueryProcessor qp, String physicalstoreid) {
        if (!this.psrepomap.containsKey(physicalstoreid)) {
            this.psrepomap.put(physicalstoreid, PhysicalStore.isRepository(qp, physicalstoreid) ? YES : NO);
        }
        String value = (String)this.psrepomap.get(physicalstoreid);
        return YES.equals(value);
    }

    private List getUserDepartments(String sysuserid) {
        if (this.userDepartments == null) {
            this.userDepartments = new HashMap();
        }
        if (!this.userDepartments.containsKey(sysuserid)) {
            this.userDepartments.put(sysuserid, OpalUtil.getUserDepartments(this.getQueryProcessor(), sysuserid));
        }
        return (List)this.userDepartments.get(sysuserid);
    }

    public static String getSDICustodialDepartment(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        DataSet ds = queryProcessor.getPreparedSqlDataSet("select custodialdepartmentid from trackitem where linksdcid = ? and linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1});
        if (ds != null && ds.size() > 0) {
            return ds.getValue(0, COLUMN_CUSTODIALDEPARTMENTID);
        }
        return "";
    }

    public static String getSDICustodialUser(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        DataSet ds = queryProcessor.getPreparedSqlDataSet("select custodialuserid from trackitem where linksdcid = ? and linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1});
        if (ds != null && ds.size() > 0) {
            return ds.getValue(0, COLUMN_CUSTODIALUSERID);
        }
        return "";
    }

    private String batchValidateTargetStorageUnit(QueryProcessor queryProcessor, TranslationProcessor translationProcessor, String storageunitid, String userid, List userDepartments, boolean stabilityTI) {
        if (!this.targetstoragemap.containsKey(storageunitid)) {
            String error = TrackItemSDC.validateTargetStorageUnit(queryProcessor, translationProcessor, storageunitid, userid, userDepartments, this.ASL, stabilityTI);
            this.targetstoragemap.put(storageunitid, error);
        }
        return (String)this.targetstoragemap.get(storageunitid);
    }

    public static String validateTargetStorageUnit(QueryProcessor queryProcessor, TranslationProcessor translationProcessor, String storageunitid, String userid, List userDepartments, boolean ASL, boolean stabilityTI) {
        StringBuilder error = new StringBuilder();
        PropertyList props = StorageUnitSDC.getStorageUnitProps(queryProcessor, storageunitid);
        if (props.size() > 0) {
            String storageUnitSDC = props.getProperty(COLUMN_LINKSDCID);
            String storageUnitKeyid1 = props.getProperty(COLUMN_LINKKEYID1);
            String storageLabelPath = props.getProperty("labelpath");
            if ("LV_Box".equals(storageUnitSDC)) {
                String boxstatus = OpalUtil.getColumnValue(queryProcessor, "s_box", "boxstatus", "s_boxid = ?", new String[]{storageUnitKeyid1});
                if ("Full".equals(boxstatus)) {
                    error.append("{{Box is full}}");
                    error.append(" (").append(storageUnitKeyid1).append(")<br>");
                } else if (ASL && !stabilityTI) {
                    TrackItemSDC.validateTargetStorageCustodialDepartment(error, queryProcessor, userDepartments, storageunitid, props, userid, storageLabelPath);
                }
            } else if ("LV_Package".equals(storageUnitSDC)) {
                HashMap<String, String> packageMap = PackageValidate.getPackageInfo(queryProcessor, storageUnitKeyid1);
                String status = (String)packageMap.get("status");
                String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
                String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
                if (!PackageValidate.allowedManageStatus.contains(status)) {
                    error.append("{{Filing items into Package with status of}} ").append(status).append(" {{is not allowed}}");
                }
                if (ASL && !stabilityTI) {
                    if (status.equals("Created")) {
                        if (!userDepartments.contains(senderdepartmentid)) {
                            if (error.length() > 0) {
                                error.append("<br>");
                            }
                            error.append("{{You are not a member of selected Package's origination Custodial Department}}.");
                        }
                    } else if (!userDepartments.contains(recipientdepartmentid)) {
                        if (error.length() > 0) {
                            error.append("<br>");
                        }
                        error.append("{{You are not a member of selected Package's destination Custodial Department}}.");
                    }
                }
            } else {
                if ("No Layout".equals(props.getProperty("propertytreeid")) && "0".equals(props.getProperty("maxtiallowed"))) {
                    error.append("{{The selected Storageunit does not allow filing in any items}}.");
                }
                if (ASL && !stabilityTI) {
                    TrackItemSDC.validateTargetStorageCustodialDepartment(error, queryProcessor, userDepartments, storageunitid, props, userid, storageLabelPath);
                }
            }
        } else {
            error.append("{{No storage unit found}} (").append(storageunitid).append(").");
        }
        return error.length() > 0 ? translationProcessor.translatePartial(error.toString()) : "";
    }

    private static void validateTargetStorageCustodialDepartment(StringBuilder error, QueryProcessor queryProcessor, List userDepartments, String storageunitid, PropertyList props, String userid, String storageLabelPath) {
        String newCustodialDepartmentID;
        String newCustodialUserID = props.getProperty(COLUMN_CUSTODIALUSERID);
        if (newCustodialUserID.length() > 0 && !newCustodialUserID.equals(userid)) {
            error.append("{{User is not the Custodian of target}} ").append(storageLabelPath).append("<br>");
        }
        if (StringUtil.getLen(newCustodialDepartmentID = props.getProperty(COLUMN_CUSTODIALDEPARTMENTID)) > 0L) {
            if (!userDepartments.contains(newCustodialDepartmentID)) {
                error.append("{{User is not a member of the custodial department of}} ").append(storageLabelPath).append("<br>");
            }
        } else {
            String physicalstoreid = StorageUnitSDC.getParentSDIBySDC(queryProcessor, storageunitid, "PhysicalStore");
            if (StringUtil.getLen(physicalstoreid) > 0L) {
                newCustodialDepartmentID = PhysicalStore.getCustodialDepartmentId(queryProcessor, physicalstoreid);
                if (StringUtil.getLen(newCustodialDepartmentID) > 0L) {
                    if (!userDepartments.contains(newCustodialDepartmentID)) {
                        error.append("{{User is not a member of the custodial department of}} ").append(storageLabelPath).append("<br>");
                    }
                } else {
                    error.append("{{The target Storage Unit does not belong to any Custodial Department}} (").append(storageLabelPath).append(")<br>");
                }
            } else {
                error.append("{{The target Storage Unit does not belong to any Custodial Department}} (").append(storageLabelPath).append(")<br>");
            }
        }
    }

    public String validateSDIMovement(HashMap datamap, String userid, List userdepartmentlist, boolean validatecustodian) {
        String custodialuserid = (String)datamap.get(COLUMN_CUSTODIALUSERID);
        String sdcid = (String)datamap.get(COLUMN_LINKSDCID);
        String keyid1 = (String)datamap.get(COLUMN_LINKKEYID1);
        if (validatecustodian && StringUtil.getLen(custodialuserid) > 0L && !userid.equals(custodialuserid)) {
            return "User is not the custodian of " + this.parseSDCName(sdcid) + " " + keyid1 + "<br>";
        }
        String custodialdepartmentid = (String)datamap.get(COLUMN_CUSTODIALDEPARTMENTID);
        if (StringUtil.getLen(custodialdepartmentid) > 0L && this.isDepartmentCustodialDomain(custodialdepartmentid) && !userdepartmentlist.contains(custodialdepartmentid)) {
            return this.parseSDCName(sdcid) + " " + keyid1 + " is not in user's Custodial Department<br>";
        }
        return "";
    }

    private String parseSDCName(String sdcid) {
        if (!this.sdcnamemap.containsKey(sdcid)) {
            this.sdcnamemap.put(sdcid, OpalUtil.getSDCName(sdcid));
        }
        return (String)this.sdcnamemap.get(sdcid);
    }

    private ThresholdInfo findReagentThreshold(String reagentLotId, String reagentTypeId, String reagentVersion) throws SapphireException {
        DataSet lotDS;
        ThresholdInfo threshold = null;
        if (reagentTypeId == null || reagentTypeId.length() == 0 || reagentVersion == null || reagentVersion.length() == 0) {
            this.logger.error("ReagentTypeID::" + reagentTypeId + "  or version " + reagentVersion + " is null");
            return threshold;
        }
        DataSet ds = this.getThresoldDetailsByReagentType(reagentTypeId, reagentVersion);
        if (ds == null) {
            throw new SapphireException("Failed to fetch threshold details");
        }
        QuantityUnitsInfo ret = new QuantityUnitsInfo();
        String thresholdValue = ds.getValue(0, "reorderthreshold", "");
        if (thresholdValue == null || thresholdValue.length() == 0) {
            throw new SapphireException("Failed to fetch threshold details, threshold quantity is not set");
        }
        ret.quantity = ds.getBigDecimal(0, "reorderthreshold", new BigDecimal(0)).toString();
        ret.units = ds.getString(0, "reorderthresholdunits");
        ret.unitType = ds.getString(0, "reorderthresholdunittype");
        ret.containerSize = ds.getValue(0, COLUMN_CONTAINERSIZE);
        ret.containerUnits = ds.getString(0, COLUMN_CONTAINERUNITS);
        threshold = new ThresholdInfo();
        threshold.threshold = ret;
        threshold.reorderScopeFlag = ds.getString(0, "reorderthresholdscopeflag");
        if (threshold.reorderScopeFlag == null || threshold.reorderScopeFlag.length() == 0) {
            threshold.reorderScopeFlag = "T";
        }
        if ((lotDS = this.getReagentLotDetails(reagentLotId)) != null && lotDS.getRowCount() > 0) {
            threshold.reorderedFlag = lotDS.getString(0, "reorderedflag");
            if (threshold.reorderedFlag == null || threshold.reorderedFlag.length() == 0) {
                threshold.reorderedFlag = NO;
            }
        }
        return threshold;
    }

    private QuantityUnitsInfo findTotalReagentAmount(String reagentLotId, String reagentTypeId, String reagentVersion, ThresholdInfo thresholdInfo, String[] exclusionList, String currDepletedTI) {
        QuantityUnitsInfo ret = new QuantityUnitsInfo();
        ret.units = thresholdInfo.threshold.units;
        ret.unitType = thresholdInfo.threshold.unitType;
        ret.containerSize = thresholdInfo.threshold.containerSize;
        ret.containerUnits = thresholdInfo.threshold.containerUnits;
        String departmentid = this.connectionInfo.getDefaultDepartment();
        DataSet ds = thresholdInfo.reorderScopeFlag == null || thresholdInfo.reorderScopeFlag.equalsIgnoreCase("T") ? ReagentUtil.getDSForTotalQuantityPerType(this.getConfigurationProcessor(), this.getSDCProcessor(), this.getQueryProcessor(), reagentTypeId, reagentVersion, departmentid) : ReagentUtil.getDSForTotalQuantityPerLot(this.getConfigurationProcessor(), this.getSDCProcessor(), this.getQueryProcessor(), reagentLotId, departmentid);
        SafeSQL safeSQL = new SafeSQL();
        if ((ds == null || ds.size() == 0) && currDepletedTI != null && currDepletedTI.length() > 0) {
            String sql = " SELECT trackitemid, qtycurrent, qtyunits, qtycurrenttype,trackitem.containertypeid, sizevalue,sizeunits FROM trackitem  LEFT OUTER JOIN containertype on trackitem.containertypeid=containertype.containertypeid  WHERE trackitem.linksdcid = 'LV_ReagentLot' AND trackitem.trackitemid = " + safeSQL.addVar(currDepletedTI);
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        if (ds == null || ds.size() == 0) {
            ret.quantity = null;
            return ret;
        }
        double totalQty = 0.0;
        for (int row = 0; row < ds.getRowCount(); ++row) {
            String trackitemid = ds.getString(row, COLUMN_TRACKITEMID);
            boolean excludedItem = false;
            if (exclusionList != null) {
                for (int curitem = 0; curitem < exclusionList.length; ++curitem) {
                    if (!trackitemid.equalsIgnoreCase(exclusionList[curitem])) continue;
                    excludedItem = true;
                    break;
                }
            }
            if (!excludedItem) {
                QuantityUnitsInfo tiQty = new QuantityUnitsInfo();
                tiQty.quantity = ds.getBigDecimal(row, COLUMN_QTYCURRENT, new BigDecimal(0)).toString();
                tiQty.units = ds.getString(row, COLUMN_QTYUNIT);
                tiQty.unitType = ds.getString(row, COLUMN_QTYCURRENTTYPE);
                tiQty.containerSize = ds.getValue(row, COLUMN_CONTAINERSIZE);
                tiQty.containerUnits = ds.getString(row, COLUMN_CONTAINERUNITS);
                QuantityUnitsInfo convertedTI = null;
                try {
                    convertedTI = this.convertTIQtyToThresholdUnits(tiQty, thresholdInfo.threshold);
                }
                catch (SapphireException e) {
                    this.logger.error("Failed to convert trackitem to threshold units: " + trackitemid);
                }
                totalQty += Double.parseDouble(convertedTI.quantity);
                continue;
            }
            this.logger.info("trackitemid:" + trackitemid + " excluded from calculation");
        }
        ret.quantity = Double.toString(totalQty);
        return ret;
    }

    private QuantityUnitsInfo convertTIQtyToThresholdUnits(QuantityUnitsInfo tiQty, QuantityUnitsInfo threshold) throws SapphireException {
        if (tiQty.unitType == null || tiQty.unitType.length() == 0) {
            tiQty.unitType = "U";
        }
        if (threshold.unitType == null || threshold.unitType.length() == 0) {
            threshold.unitType = "U";
        }
        if (tiQty.unitType.equalsIgnoreCase("C") && threshold.unitType.equalsIgnoreCase("C")) {
            return tiQty;
        }
        if (tiQty.unitType.equalsIgnoreCase("U") && threshold.unitType.equalsIgnoreCase("U")) {
            if (!(tiQty.units != null && tiQty.units.length() != 0 || threshold.units != null && threshold.units.length() != 0)) {
                return tiQty;
            }
            if (tiQty.units.equalsIgnoreCase(threshold.units)) {
                return tiQty;
            }
            tiQty.quantity = UnitsUtil.getConvertedValue(this.getQueryProcessor(), tiQty.units, threshold.units, tiQty.quantity);
            tiQty.units = threshold.units;
            return tiQty;
        }
        if (tiQty.unitType.equalsIgnoreCase("C")) {
            double convertedQty = UnitsUtil.convertFromContainersToUnits(this.getQueryProcessor(), tiQty.containerSize, tiQty.containerUnits, tiQty.quantity, threshold.units);
            tiQty.quantity = Double.toString(convertedQty);
            tiQty.unitType = "U";
            tiQty.units = threshold.units;
            tiQty.containerSize = "";
            tiQty.containerUnits = "";
            return tiQty;
        }
        if (threshold.unitType.equalsIgnoreCase("C")) {
            double convertedQty = UnitsUtil.covertToContainersFromUnits(this.getQueryProcessor(), threshold.containerSize, threshold.containerUnits, tiQty.quantity, tiQty.units);
            tiQty.quantity = Double.toString(convertedQty);
            tiQty.units = "";
            tiQty.unitType = "C";
            tiQty.containerSize = threshold.containerSize;
            tiQty.containerUnits = threshold.containerUnits;
            return tiQty;
        }
        return tiQty;
    }

    private void checkReagentThreshold(String reagentLotId, String operation, String[] exclusionList, String currDepletedTI) throws SapphireException {
        ThresholdInfo thresholdInfo;
        DataSet ds = this.getReagentLotDetails(reagentLotId);
        String reagentTypeId = ds.getString(0, COLUMN_REAGENT_TYPE);
        String reagentVersion = ds.getString(0, COLUMN_REAGENT_TYPE_VERSION, "");
        try {
            thresholdInfo = this.findReagentThreshold(reagentLotId, reagentTypeId, reagentVersion);
        }
        catch (SapphireException e) {
            this.logger.info("Failed to fetch reagent threshold details");
            return;
        }
        if (operation.equalsIgnoreCase(OP_ADD) && this.isReordered(thresholdInfo)) {
            this.checkIfQtyAboveThreshold(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        }
        if (operation.equalsIgnoreCase(OP_EDIT) && this.isReordered(thresholdInfo)) {
            this.checkIfQtyAboveThreshold(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        }
        if (operation.equalsIgnoreCase(OP_EDIT) && !this.isReordered(thresholdInfo)) {
            this.checkIfQtyBelowThreshold(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        }
        if (operation.equalsIgnoreCase(OP_DELETE) && !this.isReordered(thresholdInfo)) {
            this.checkIfQtyBelowThreshold(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        }
    }

    private boolean isReordered(ThresholdInfo thresholdInfo) throws SapphireException {
        if (thresholdInfo.reorderedFlag == null || thresholdInfo.reorderedFlag.length() == 0) {
            if (thresholdInfo.reorderScopeFlag.equalsIgnoreCase("L")) {
                thresholdInfo.reorderedFlag = NO;
            } else {
                throw new SapphireException("reordered flag found to be null or empty");
            }
        }
        return !NO.equalsIgnoreCase(thresholdInfo.reorderedFlag);
    }

    private void checkIfQtyAboveThreshold(String reagentLotId, String reagentTypeId, String reagentVersion, ThresholdInfo thresholdInfo, String[] exclusionList, String currDepletedTI) {
        QuantityUnitsInfo remainingReagentInfo = this.findTotalReagentAmount(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        if (remainingReagentInfo.quantity == null) {
            return;
        }
        if (Double.parseDouble(remainingReagentInfo.quantity) >= Double.parseDouble(thresholdInfo.threshold.quantity)) {
            this.clearReorderNotification(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo.reorderScopeFlag);
        }
    }

    private void checkIfQtyBelowThreshold(String reagentLotId, String reagentTypeId, String reagentVersion, ThresholdInfo thresholdInfo, String[] exclusionList, String currDepletedTI) {
        QuantityUnitsInfo remainingReagentInfo = this.findTotalReagentAmount(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo, exclusionList, currDepletedTI);
        if (remainingReagentInfo.quantity == null) {
            return;
        }
        if (Double.parseDouble(remainingReagentInfo.quantity) < Double.parseDouble(thresholdInfo.threshold.quantity)) {
            this.setReorderNotification(reagentLotId, reagentTypeId, reagentVersion, thresholdInfo.reorderScopeFlag);
        }
    }

    private void clearReorderNotification(String reagentLotId, String reagentTypeId, String reagentVersion, String reorderScope) {
        this.updateReorderFlag(NO, reagentLotId, reagentTypeId, reagentVersion, reorderScope);
    }

    private void setReorderNotification(String reagentLotId, String reagentTypeId, String reagentVersion, String reorderScope) {
        this.updateReorderFlag(YES, reagentLotId, reagentTypeId, reagentVersion, reorderScope);
    }

    private void updateReorderFlag(String reorderFlagValue, String reagentLotId, String reagentTypeId, String reagentVersion, String reorderScope) {
        HashMap<String, String> props = new HashMap<String, String>();
        if (reorderScope == null || "T".equalsIgnoreCase(reorderScope)) {
            String reagentLotIdList = "";
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT reagentlotid FROM reagentlot");
            sql.append(" WHERE reagenttypeid = ").append(safeSQL.addVar(reagentTypeId));
            String departmentid = this.connectionInfo.getDefaultDepartment();
            if (ReagentUtil.considerVersionForThresholdNotification(this.getConfigurationProcessor()) && reagentVersion.trim().length() > 0) {
                sql.append(" AND reagenttypeversionid = ").append(safeSQL.addVar(reagentVersion));
            }
            if (ReagentUtil.considerDeptForThresholdNotification(this.getConfigurationProcessor(), this.getSDCProcessor()) && departmentid.trim().length() > 0) {
                sql.append(" AND (securitydepartment in (").append(safeSQL.addIn(departmentid, ";")).append(")").append(" OR securitydepartment IS NULL)");
            }
            sql.append(" AND reagentstatus = 'Active'");
            DataSet rlds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            for (int row = 0; row < rlds.getRowCount(); ++row) {
                reagentLotIdList = reagentLotIdList + rlds.getValue(row, "reagentlotid");
                if (row == rlds.getRowCount() - 1) continue;
                reagentLotIdList = reagentLotIdList + ";";
            }
            reagentLotIdList = reagentLotIdList.length() > 0 ? reagentLotIdList + ";" + reagentLotId : reagentLotId;
            props.put("sdcid", "LV_ReagentLot");
            props.put("keyid1", reagentLotIdList);
            props.put("applylock", YES);
            props.put("reorderedflag", reorderFlagValue);
        } else {
            props.put("sdcid", "LV_ReagentLot");
            props.put("keyid1", reagentLotId);
            props.put("keyid2", reagentVersion);
            props.put("applylock", YES);
            props.put("reorderedflag", reorderFlagValue);
        }
        try {
            if (reorderScope == null || "T".equalsIgnoreCase(reorderScope)) {
                if (NO.equalsIgnoreCase(reorderFlagValue)) {
                    props.put("reorderthresholdnotifyflag", reorderFlagValue);
                } else {
                    PropertyList props1 = new PropertyList();
                    props1.put("sdcid", "LV_ReagentLot");
                    props1.put("keyid1", reagentLotId);
                    props1.put("keyid2", reagentVersion);
                    props1.put("reorderthresholdnotifyflag", reorderFlagValue);
                    this.getActionProcessor().processAction("EditSDI", "1", props1);
                }
            } else {
                props.put("reorderthresholdnotifyflag", reorderFlagValue);
            }
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        catch (ActionException e) {
            this.logger.error("EditSDI failed for updated reorderedflag on Reagent Lots", e);
        }
    }

    private String getDepartmentProperty(String departmentid, String propertyid) {
        String value;
        Map<Object, Object> map = null;
        if (!this.departmentCache.containsKey(departmentid)) {
            map = new HashMap();
            DataSet dscolumns = this.getSDCProcessor().getColumnData("Department");
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(dscolumns.getColumnValues("columnid", ",")).append(" from department where departmentid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{departmentid});
            if (ds != null && ds.size() > 0) {
                for (int col = 0; col < ds.getColumnCount(); ++col) {
                    map.put(ds.getColumnId(col), ds.getValue(0, ds.getColumnId(col)));
                }
            }
            this.departmentCache.put(departmentid, map);
        }
        if (map == null) {
            map = this.departmentCache.get(departmentid);
        }
        return (value = (String)map.get(propertyid)) == null ? "" : value;
    }

    private void validateStabilityUnits(DataSet ds, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("primary");
        for (int i = 0; i < ds.getRowCount(); ++i) {
            DataSet planItems;
            String trackItemId = ds.getValue(i, COLUMN_TRACKITEMID);
            if ("C".equals(ds.getValue(i, COLUMN_QTYCURRENTTYPE))) continue;
            String qtyUnits = ds.getValue(i, COLUMN_QTYUNIT);
            String oldUnit = oldPrimary.getValue(i, COLUMN_QTYUNIT);
            if (qtyUnits.length() <= 0 || qtyUnits.equals(oldUnit) || (planItems = StabilityUtil.getPlanItemsByTrackItem(trackItemId, qp)) == null || planItems.getRowCount() <= 0) continue;
            String planId = planItems.getValue(0, "scheduleplanid");
            String containerUnit = ScheduleGridUtil.getContainerUnit(planId, qp);
            if (containerUnit != null && containerUnit.length() > 0 && !containerUnit.equals(qtyUnits) && !UnitsUtil.isUnitCompatible(qp, containerUnit, qtyUnits)) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("containerUnit", "'<b>" + containerUnit + "</b>'");
                valueMap.put("qtyUnits", "<b>'" + qtyUnits + "'</b>");
                throw new SapphireException("VALIDATION", "\n " + tp.translate("Unit conversion not defined between the Container Unit  [containerUnit]  and the Trackitem Unit  [qtyUnits].", valueMap));
            }
            StabilityUtil.validatePlanItemPullAmountUnits(planItems, qtyUnits, qp, tp, "TrackItem");
        }
    }

    private void manageScheduleConditionTrackitems(String schedulePlanId, String scheduleConditionId, DataSet primary, PropertyList actionProps, String operation) throws SapphireException {
        if (OP_ADD.equalsIgnoreCase(operation)) {
            StringBuffer traceLogIds = new StringBuffer();
            StringBuffer planIds = new StringBuffer();
            StringBuffer conditionIds = new StringBuffer();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                traceLogIds.append(";").append(actionProps.getProperty("tracelogid"));
                planIds.append(";").append(schedulePlanId);
                conditionIds.append(";").append(scheduleConditionId);
            }
            PropertyList props = new PropertyList();
            ActionProcessor ap = this.getActionProcessor();
            props.setProperty("sdcid", "SchedulePlan");
            props.setProperty("linkid", "PlanConditionTI");
            props.setProperty("scheduleplanid", planIds.substring(1));
            props.setProperty("scheduleconditionid", conditionIds.substring(1));
            props.setProperty(COLUMN_TRACKITEMID, primary.getColumnValues(COLUMN_TRACKITEMID, ";"));
            props.setProperty("tracelogid", traceLogIds.substring(1));
            props.setProperty("separator", ";");
            if (Trace.on) {
                Trace.log("Inserting into schedulecondition_trackitem ");
            }
            ap.processAction("AddSDIDetail", "1", props);
        } else if (OP_DELETE.equalsIgnoreCase(operation)) {
            StringBuffer traceLogIds = new StringBuffer();
            StringBuffer planIds = new StringBuffer();
            StringBuffer conditionIds = new StringBuffer();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                traceLogIds.append(";").append(actionProps.getProperty("tracelogid"));
                planIds.append(";").append(schedulePlanId);
                conditionIds.append(";").append(scheduleConditionId);
            }
            PropertyList props = new PropertyList();
            ActionProcessor ap = this.getActionProcessor();
            props.setProperty("sdcid", "SchedulePlan");
            props.setProperty("linkid", "PlanConditionTI");
            props.setProperty("scheduleplanid", planIds.substring(1));
            props.setProperty("scheduleconditionid", conditionIds.substring(1));
            props.setProperty(COLUMN_TRACKITEMID, primary.getColumnValues(COLUMN_TRACKITEMID, ";"));
            props.setProperty("tracelogid", traceLogIds.substring(1));
            props.setProperty("separator", ";");
            if (Trace.on) {
                Trace.log("Deleting schedulecondition_trackitem ");
            }
            ap.processAction("DeleteSDIDetail", "1", props);
        }
    }

    private boolean isCurrentTrackItemDepleted(DataSet primary, int i) {
        boolean isDepleted = false;
        if (this.hasPrimaryValueChanged(primary, i, COLUMN_TRACKITEMSTATUS) && "Depleted".equalsIgnoreCase(primary.getString(i, COLUMN_TRACKITEMSTATUS, ""))) {
            isDepleted = true;
        }
        return isDepleted;
    }

    private String validateUnit(DataSet primary, int row, boolean isAdd) {
        String error = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        String inputParam = isAdd ? primary.getString(row, COLUMN_LINKKEYID1, "") : primary.getString(row, COLUMN_CONTAINERTYPEID, "");
        DataSet ds = this.getUnitsDetails(inputParam, isAdd);
        if (ds != null && ds.size() > 0) {
            String selectedUnit = primary.getString(row, COLUMN_QTYUNIT, "");
            String selectedUnitType = primary.getString(row, COLUMN_QTYCURRENTTYPE, "");
            String sizeunit = ds.getString(0, COLUMN_CONTAINERUNITS, "");
            if (!(selectedUnit.length() == 0 && !selectedUnitType.equalsIgnoreCase("C") || selectedUnit.length() == 0 && selectedUnitType.equalsIgnoreCase("C") || selectedUnit.equalsIgnoreCase(sizeunit) || selectedUnit.equalsIgnoreCase(sizeunit))) {
                safeSQL.reset();
                sql.delete(0, sql.length());
                sql.append("SELECT unitsid,tounits FROM unitconversion");
                sql.append(" WHERE unitsid=").append(safeSQL.addVar(selectedUnit));
                sql.append(" AND tounits=").append(safeSQL.addVar(sizeunit));
                sql.append(" union");
                sql.append(" SELECT unitsid,tounits FROM unitconversion");
                sql.append(" WHERE unitsid=").append(safeSQL.addVar(sizeunit));
                sql.append(" AND tounits=").append(safeSQL.addVar(selectedUnit));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds.getRowCount() < 2) {
                    error = this.getTranslationProcessor().translate("Unit conversion not defined between " + selectedUnit + " and " + sizeunit);
                }
            }
        }
        return error;
    }

    private void validateTrackitemStatus(DataSet primary, int i, HashMap map) throws SapphireException {
        String reagentstatus = (String)map.get("reagentstatus");
        String trackitemstatus = primary.getString(i, COLUMN_TRACKITEMSTATUS, "");
        if (!"Active".equalsIgnoreCase(reagentstatus) && this.hasPrimaryValueChanged(primary, i, COLUMN_TRACKITEMSTATUS) && trackitemstatus.equalsIgnoreCase("Valid")) {
            throw new SapphireException("Trackitem Status cannot be set to Valid because ConsumableLot is not in Active.");
        }
    }

    private DataSet getUnitsDetails(String inputParam, boolean isAdd) {
        DataSet ds;
        if (this.validateUnitsCache.containsKey(inputParam)) {
            ds = this.validateUnitsCache.get(inputParam);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            if (isAdd) {
                sql.append("select ct.sizevalue,ct.sizeunits from reagentlot rl,containertype ct");
                sql.append(" where rl.reagentlotid=").append(safeSQL.addVar(inputParam));
                sql.append(" and rl.containertypeid=ct.containertypeid");
            } else {
                sql.append("select sizevalue,sizeunits from containertype");
                sql.append(" where containertypeid=").append(safeSQL.addVar(inputParam));
            }
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.validateUnitsCache.put(inputParam, ds);
        }
        return ds;
    }

    private DataSet getFreezThawDetails(String reagentLotId) {
        DataSet ds;
        if (this.freezThawCach.containsKey(reagentLotId)) {
            ds = this.freezThawCach.get(reagentLotId);
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("select rt.warnfreezethawcount, rt.maxfreezethawcount, rt.reagentclass, r.contentflag,r.containertypeid");
            sql.append(" from reagenttype rt, reagentlot r");
            sql.append(" where r.reagenttypeid = rt.reagenttypeid ");
            sql.append(" and r.reagenttypeversionid = rt.reagenttypeversionid");
            sql.append(" and r.reagentlotid = ?");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{reagentLotId});
            this.freezThawCach.put(reagentLotId, ds);
        }
        return ds;
    }

    private DataSet getReagentLotDetails(String reagentLotId) {
        DataSet ds;
        if (this.reagentLotCache.containsKey(reagentLotId)) {
            ds = this.reagentLotCache.get(reagentLotId);
        } else {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT reagenttypeid, reagenttypeversionid,reorderedflag FROM reagentlot WHERE reagentlotid = ?", (Object[])new String[]{reagentLotId});
            this.reagentLotCache.put(reagentLotId, ds);
        }
        return ds;
    }

    private DataSet getThresoldDetailsByReagentType(String reagentTypeId, String reagentVersion) {
        DataSet ds;
        String key = reagentTypeId + this.specialDelimer + reagentVersion;
        if (this.thresoldDetailsByReagentTypeCache.containsKey(key)) {
            ds = this.thresoldDetailsByReagentTypeCache.get(key);
        } else {
            String sql = "SELECT reagenttypeid, reagenttypeversionid,reorderthreshold, reorderthresholdunittype, reorderthresholdunits, reorderthresholdscopeflag, reagenttype.containertypeid, sizevalue, sizeunits FROM reagenttype LEFT OUTER JOIN containertype on reagenttype.containertypeid=containertype.containertypeid WHERE reagenttypeid = ? AND reagenttypeversionid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{reagentTypeId, reagentVersion});
            this.thresoldDetailsByReagentTypeCache.put(key, ds);
        }
        return ds;
    }

    private class ThresholdInfo {
        String reorderScopeFlag;
        String reorderedFlag;
        QuantityUnitsInfo threshold;

        private ThresholdInfo() {
        }
    }

    private class QuantityUnitsInfo {
        String quantity;
        String units;
        String unitType;
        String containerSize;
        String containerUnits;

        private QuantityUnitsInfo() {
        }
    }
}

