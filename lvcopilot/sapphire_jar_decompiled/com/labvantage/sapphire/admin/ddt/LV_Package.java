/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAddress;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sms.CreateStorageUnit;
import com.labvantage.sapphire.actions.storage.AddTrackItem;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.rules.PackageCDRule;
import com.labvantage.sapphire.admin.ddt.rules.SMSUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Package
extends BaseSDCRules {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 100507 $";
    public static final String SDC_LV_PACKAGE = "LV_Package";
    public static final String TABLEID = "s_package";
    public static final String STATUS_CREATED = "Created";
    public static final String STATUS_PACKED = "Packed";
    public static final String STATUS_UNPACKED = "UnPacked";
    public static final String STATUS_EMPTIED = "Emptied";
    public static final String STATUS_SHIPPED = "Shipped";
    public static final String STATUS_RECEIVED = "Received";
    public static final String STATUS_EXPECTED = "Expected";
    public static final String STATUS_ONHOLD = "On Hold";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String TYPE_CDT = "CDT";
    public static final String TYPE_PKG = "PKG";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            primary.addColumn("SENDERDEPARTMENTID", 0);
            primary.addColumn("SENDERADDRESSID", 0);
            primary.addColumn("SENDERADDRESSTYPE", 0);
            String defaultdepartment = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
            Map<String, String> addressMap = SMSUser.getContactAddress(this.getQueryProcessor(), this.connectionInfo.getSysuserId());
            for (int i = 0; i < primary.size(); ++i) {
                DataSet requestDS;
                if (TYPE_CDT.equals(primary.getValue(i, "packagetype"))) {
                    primary.setValue(i, "SENDERDEPARTMENTID", defaultdepartment);
                    if (addressMap != null) {
                        primary.setValue(i, "SENDERADDRESSID", addressMap.get("addressid"));
                        primary.setValue(i, "SENDERADDRESSTYPE", addressMap.get("addresstype"));
                    }
                }
                if (primary.getValue(i, "requestid").length() <= 0 || primary.getValue(i, "recipientdepartmentid").length() != 0 && primary.getValue(i, "senderdepartmentid").length() != 0 || (requestDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sitedepartmentid, submitbydepartmentid, requestclass FROM s_request WHERE s_requestid=?", (Object[])new String[]{primary.getValue(i, "requestid")})).size() != 1) continue;
                String siteDepartmentid = requestDS.getString(0, "sitedepartmentid");
                String submitByDepartmentid = requestDS.getString(0, "submitbydepartmentid");
                String requestClass = requestDS.getValue(0, "requestclass");
                boolean isSubmission = requestClass.equals("Submission");
                boolean isKit = requestClass.equals("Kit");
                if (primary.getValue(i, "senderdepartmentid").length() == 0) {
                    primary.setString(i, "senderdepartmentid", isSubmission ? submitByDepartmentid : (isKit ? siteDepartmentid : null));
                }
                if (primary.getValue(i, "recipientdepartmentid").length() != 0) continue;
                primary.setString(i, "recipientdepartmentid", isSubmission ? siteDepartmentid : (isKit ? submitByDepartmentid : null));
            }
            if ("D".equals(this.getSDCProcessor().getProperty(SDC_LV_PACKAGE, "accesscontrolledflag"))) {
                this.setSecurityDepartment(sdiData);
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        DataSet primary = sdiData.getDataset("primary");
        this.checkPackageCDRule(primary);
        PropertyList props = new PropertyList();
        String templocation = actionProps.getProperty("__templocation", "");
        boolean updatePackageContactInformation = true;
        boolean handlePortalRequestData = "Y".equals(actionProps.getProperty("_fromportal"));
        boolean shipPackage = "Y".equals(actionProps.getProperty("ship"));
        for (int i = 0; i < primary.size(); ++i) {
            String packageid = primary.getValue(i, "s_packageid");
            try {
                props.clear();
                props.setProperty("sdcid", SDC_LV_PACKAGE);
                props.setProperty("keyid1", primary.getValue(i, "s_packageid"));
                props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                if (OpalUtil.isNotEmpty(templocation)) {
                    props.setProperty("location", templocation);
                }
                this.getActionProcessor().processActionClass(AddTrackItem.class.getName(), props);
                actionProps.setProperty("trackitemid", props.getProperty("newkeyid1"));
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                updatePackageContactInformation = false;
                this.setErrors(e.getErrorHandler());
            }
            if (updatePackageContactInformation) {
                this.checkPackageColumns(primary.getValue(i, "s_packageid"), primary.getValue(i, "packagestatus"), forceUpdate, "", actionProps);
            }
            if (!handlePortalRequestData) continue;
            String requestid = primary.getValue(i, "requestid", "");
            this.handlePortalPackageData(packageid, requestid, shipPackage, actionProps);
        }
    }

    private void handlePortalPackageData(String packageid, String requestid, boolean shipPackage, PropertyList actionProps) throws ActionException {
        PropertyList props = new PropertyList();
        String packagestorageunitid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linksdcid=? and linkkeyid1=?", new String[]{SDC_LV_PACKAGE, packageid});
        if (packagestorageunitid.length() == 0) {
            props.setProperty("createsu", "Y");
            props.setProperty("newkeyid1", packageid);
            props.setProperty("linksdcid", SDC_LV_PACKAGE);
            props.setProperty("linkpropnodeid", "No Layout|Package");
            props.setProperty("nodeid", "Package");
            props.setProperty("propertytreeid", "No Layout");
            props.setProperty("size", "0");
            props.setProperty("maxtiallowed", "-1");
            props.setProperty("moveableflag", "Y");
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(CreateStorageUnit.class.getName(), props);
            packagestorageunitid = props.getProperty("storageunitid");
        }
        if (requestid.length() > 0) {
            ArrayList<String> trackitemList = new ArrayList<String>();
            String sql = "select d.s_requestitemdetailid, d.requestid, d.linksdcid, d.linkkeyid1, d.requestitemdetailstatus,       (select su.linkkeyid1 from storageunit su, trackitem t       where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package'         and t.linksdcid = d.linksdcid and t.linkkeyid1 = d.linkkeyid1) packageid1,       (select su.linkkeyid1 from storageunit su, trackitem t        where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package'          and d.linksdcid = 'TrackItemSDC' and t.trackitemid = d.linkkeyid1) packageid2,       (case when d.linksdcid = 'TrackItemSDC' then d.linkkeyid1 else (select t.trackitemid        from trackitem t where t.linksdcid = d.linksdcid and t.linkkeyid1 = d.linkkeyid1) end) trackitemid from s_requestitemdetail d where d.requestid = ? and requestitemdetailstatus = 'Pending'";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{requestid});
            if (OpalUtil.isNotEmpty(ds)) {
                DataSet addTrackItemDS = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    if (ds.getString(i, "packageid1", "").length() != 0 || ds.getString(i, "packageid2", "").length() != 0) continue;
                    if (ds.getString(i, "trackitemid", "").length() == 0) {
                        int row = addTrackItemDS.addRow();
                        addTrackItemDS.setString(row, "linksdcid", ds.getString(i, "linksdcid"));
                        addTrackItemDS.setString(row, "linkkeyid1", ds.getString(i, "linkkeyid1"));
                        continue;
                    }
                    trackitemList.add(ds.getString(i, "trackitemid", ""));
                }
                if (addTrackItemDS.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("linksdcid", addTrackItemDS.getColumnValues("linksdcid", ";"));
                    props.setProperty("linkkeyid1", addTrackItemDS.getColumnValues("linkkeyid1", ";"));
                    props.setProperty("copies", String.valueOf(addTrackItemDS.size()));
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    String[] trackitemids = StringUtil.split(props.getProperty("newkeyid1"), ";");
                    trackitemList.addAll(Arrays.asList(trackitemids));
                }
            }
            if (trackitemList.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(trackitemList, ";"));
                props.setProperty("currentstorageunitid", packagestorageunitid);
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (shipPackage) {
            props.clear();
            props.setProperty("sdcid", SDC_LV_PACKAGE);
            props.setProperty("keyid1", packageid);
            props.setProperty("packagestatus", STATUS_SHIPPED);
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            return;
        }
        boolean packageStatusModified = false;
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String destination;
            String packageid;
            if (!this.hasPrimaryValueChanged(primary, i, "packagestatus")) continue;
            packageStatusModified = true;
            if (STATUS_SHIPPED.equals(primary.getValue(i, "packagestatus", ""))) {
                packageid = primary.getValue(i, "s_packageid");
                destination = OpalUtil.getColumnValue(this.getQueryProcessor(), TABLEID, "recipientdepartmentid", "s_packageid=?", new String[]{packageid});
                if (StringUtil.getLen(destination) != 0L) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("No Destination"), "VALIDATION", this.getTranslationProcessor().translate("Unable to Ship. Package does not have a destination."));
            }
            if (!STATUS_ONHOLD.equals(primary.getValue(i, "packagestatus", ""))) continue;
            packageid = primary.getValue(i, "s_packageid");
            if (STATUS_RECEIVED.equals(this.getOldPrimaryValue(primary, i, "packagestatus"))) {
                destination = OpalUtil.getColumnValue(this.getQueryProcessor(), TABLEID, "recipientdepartmentid", "s_packageid=?", new String[]{packageid});
                if (this.connectionInfo.isDepartmentMember(destination)) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("Package On Hold"), "VALIDATION", this.getTranslationProcessor().translate("Package not in user's department"));
            }
            throw new SapphireException(this.getTranslationProcessor().translate("Package On Hold"), "VALIDATION", this.getTranslationProcessor().translate("Only received package can be placed on hold"));
        }
        if (packageStatusModified && "D".equals(this.getSDCProcessor().getProperty(SDC_LV_PACKAGE, "accesscontrolledflag"))) {
            this.setSecurityDepartment(sdiData);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            return;
        }
        boolean forceUpdate = actionProps.getProperty("__sdcruleconfirm").equals("Y");
        DataSet primary = sdiData.getDataset("primary");
        this.checkPackageCDRule(primary);
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        boolean shipPackage = "Y".equals(actionProps.getProperty("ship"));
        HashSet<String> shipPackageSet = new HashSet<String>();
        DataSet setCustodialDepartmentDS = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String packageid = primary.getValue(i, "s_packageid");
            String oldpackagestatus = this.getOldPrimaryValue(primary, i, "packagestatus");
            String string = oldpackagestatus = oldpackagestatus == null ? "" : oldpackagestatus;
            if (this.hasPrimaryValueChanged(primary, i, "packagestatus")) {
                DataSet ds;
                PropertyList props;
                String packagestatus = primary.getValue(i, "packagestatus", "");
                this.checkPackageColumns(packageid, packagestatus, forceUpdate, oldpackagestatus, actionProps);
                if (STATUS_SHIPPED.equals(packagestatus)) {
                    boolean isSubmissionRequestPackage = false;
                    String requestid = this.getOldPrimaryValue(primary, i, "requestid");
                    if (OpalUtil.isNotEmpty(requestid)) {
                        String requestclass = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_request", "requestclass", "s_requestid=?", new String[]{requestid});
                        isSubmissionRequestPackage = "Submission".equals(requestclass);
                    }
                    if (!isSubmissionRequestPackage) {
                        StringBuilder sql = new StringBuilder();
                        sql.append("select t.trackitemid");
                        sql.append(" from trackitem t, storageunit s, reagentlot r");
                        sql.append(" where t.linksdcid = 'LV_ReagentLot'");
                        sql.append(" and t.currentstorageunitid = s.storageunitid");
                        sql.append(" and s.linkkeyid1 = ?");
                        sql.append(" and s.linksdcid = 'LV_Package'");
                        sql.append(" and r.reagentlotid = t.linkkeyid1");
                        sql.append(" and r.contentflag = 'K'");
                        DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
                        if (ds2 != null && ds2.size() > 0) {
                            try {
                                PropertyList props2 = new PropertyList();
                                props2.setProperty("sdcid", "TrackItemSDC");
                                props2.setProperty("keyid1", ds2.getColumnValues("trackitemid", ";"));
                                props2.setProperty("trackitemstatus", STATUS_SHIPPED);
                                props2.setProperty("__sdcruleignore", "Y");
                                ap.processActionClass(EditSDI.class.getName(), props2);
                                if (ap.hasInfoErrors()) {
                                    this.setErrors(ap.getErrorHandler());
                                }
                            }
                            catch (ActionException e) {
                                this.setErrors(e.getErrorHandler());
                                this.logger.error("Error running " + EditTrackItem.class.getName(), e);
                            }
                        }
                        sql.setLength(0);
                        if (this.getConnectionProcessor().getSapphireConnection().isOracle()) {
                            sql.append("SELECT t.trackitemid, t.linksdcid, t.linkkeyid1, r.s_requestitemdetailid");
                            sql.append(" ,(select s_request.requeststatus from s_request where s_request.s_requestid = r.requestid) requeststatus");
                            sql.append(" FROM trackitem t, s_requestitemdetail r");
                            sql.append(" WHERE t.currentstorageunitid IN (");
                            sql.append(" SELECT su.storageunitid FROM storageunit su");
                            sql.append(" CONNECT BY PRIOR su.storageunitid = su.parentid");
                            sql.append(" START WITH su.linksdcid = 'LV_Package' AND su.linkkeyid1 = ?)");
                            sql.append(" and t.linksdcid = 'Sample'");
                            sql.append(" and r.linksdcid = t.linksdcid and r.linkkeyid1 = t.linkkeyid1 and r.requestitemdetailstatus = 'Pending'");
                        } else {
                            sql.append("WITH StorageUnitTree (storageunitid) AS (");
                            sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                            sql.append(" WHERE su.linksdcid = 'LV_Package' and su.linkkeyid1 = ?");
                            sql.append(" UNION ALL");
                            sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                            sql.append(" INNER JOIN StorageUnitTree AS d ON su.parentid = d.storageunitid");
                            sql.append(")");
                            sql.append(" select t.trackitemid, t.linksdcid, t.linkkeyid1, r.s_requestitemdetailid");
                            sql.append(" ,(select s_request.requeststatus from s_request where s_request.s_requestid = r.requestid) requeststatus");
                            sql.append(" from trackitem t, s_requestitemdetail r");
                            sql.append(" where t.currentstorageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)");
                            sql.append(" and t.linksdcid = 'Sample'");
                            sql.append(" and r.linksdcid = t.linksdcid and r.linkkeyid1 = t.linkkeyid1 and r.requestitemdetailstatus = 'Pending'");
                        }
                        DataSet sampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
                        if (sampleDs != null && sampleDs.size() > 0) {
                            HashSet<String> detailIdSet = new HashSet<String>();
                            for (int row = 0; row < sampleDs.size(); ++row) {
                                String requeststatus = sampleDs.getString(row, "requeststatus", "");
                                if ("Completed".equals(requeststatus) || "Released".equals(requeststatus)) continue;
                                detailIdSet.add(sampleDs.getString(row, "s_requestitemdetailid", ""));
                            }
                            if (!detailIdSet.isEmpty()) {
                                PropertyList props3 = new PropertyList();
                                props3.setProperty("sdcid", "LV_RequestItemDetail");
                                props3.setProperty("keyid1", OpalUtil.toDelimitedString(detailIdSet, ";"));
                                props3.setProperty("operation", "shipdetail");
                                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props3);
                            }
                        } else {
                            sql.setLength(0);
                            if (this.getConnectionProcessor().getSapphireConnection().isOracle()) {
                                sql.append("SELECT t.trackitemid, t.linksdcid, t.linkkeyid1, r.s_requestitemdetailid");
                                sql.append(" ,(select s_request.requeststatus from s_request where s_request.s_requestid = r.requestid) requeststatus");
                                sql.append(" FROM trackitem t, s_requestitemdetail r");
                                sql.append(" WHERE t.currentstorageunitid IN (");
                                sql.append(" SELECT su.storageunitid FROM storageunit su");
                                sql.append(" CONNECT BY PRIOR su.storageunitid = su.parentid");
                                sql.append(" START WITH su.linksdcid = 'LV_Package' AND su.linkkeyid1 = ?)");
                                sql.append(" and r.linkkeyid1 = t.trackitemid and r.requestitemdetailstatus = 'Pending'");
                            } else {
                                sql.append("WITH StorageUnitTree (storageunitid) AS (");
                                sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                                sql.append(" WHERE su.linksdcid = 'LV_Package' and su.linkkeyid1 = ?");
                                sql.append(" UNION ALL");
                                sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                                sql.append(" INNER JOIN StorageUnitTree AS d ON su.parentid = d.storageunitid");
                                sql.append(")");
                                sql.append(" select t.trackitemid, t.linksdcid, t.linkkeyid1, r.s_requestitemdetailid");
                                sql.append(" ,(select s_request.requeststatus from s_request where s_request.s_requestid = r.requestid) requeststatus");
                                sql.append(" from trackitem t, s_requestitemdetail r");
                                sql.append(" where t.currentstorageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)");
                                sql.append(" and r.linkkeyid1 = t.trackitemid and r.requestitemdetailstatus = 'Pending'");
                            }
                            DataSet trackItemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
                            if (trackItemDS != null) {
                                HashSet<String> detailIdSet = new HashSet<String>();
                                for (int row = 0; row < trackItemDS.size(); ++row) {
                                    String requeststatus = trackItemDS.getString(row, "requeststatus", "");
                                    if ("Completed".equals(requeststatus) || "Released".equals(requeststatus)) continue;
                                    detailIdSet.add(trackItemDS.getString(row, "s_requestitemdetailid", ""));
                                }
                                if (!detailIdSet.isEmpty()) {
                                    PropertyList props4 = new PropertyList();
                                    props4.setProperty("sdcid", "LV_RequestItemDetail");
                                    props4.setProperty("keyid1", OpalUtil.toDelimitedString(detailIdSet, ";"));
                                    props4.setProperty("operation", "shipdetail");
                                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props4);
                                }
                            }
                        }
                    }
                } else if (STATUS_CREATED.equals(packagestatus)) {
                    DataSet ds3;
                    if (oldpackagestatus.equals(STATUS_SHIPPED) && (ds3 = qp.getPreparedSqlDataSet("select t.trackitemid from trackitem t, storageunit s, reagentlot r where t.linksdcid = 'LV_ReagentLot' and t.currentstorageunitid = s.storageunitid and s.linkkeyid1 = ? and s.linksdcid = 'LV_Package' and r.reagentlotid = t.linkkeyid1 and r.contentflag = 'K'", (Object[])new String[]{packageid})) != null && ds3.size() > 0) {
                        try {
                            props = new PropertyList();
                            props.setProperty("sdcid", "TrackItemSDC");
                            props.setProperty("keyid1", ds3.getColumnValues("trackitemid", ";"));
                            props.setProperty("trackitemstatus", "Valid");
                            props.setProperty("__sdcruleignore", "Y");
                            ap.processAction("EditSDI", "1", props);
                            if (ap.hasInfoErrors()) {
                                this.setErrors(ap.getErrorHandler());
                            }
                        }
                        catch (ActionException e) {
                            this.setErrors(e.getErrorHandler());
                            this.logger.error("Error running " + EditTrackItem.class.getName(), e);
                        }
                    }
                } else if (STATUS_CANCELLED.equals(packagestatus)) {
                    String packagetype;
                    if (STATUS_RECEIVED.equals(oldpackagestatus)) {
                        this.setError("CancelPackage", "VALIDATION", "A Received Package is not allowed to be Cancelled");
                    }
                    if (STATUS_CANCELLED.equals(oldpackagestatus)) {
                        this.setError(this.getTranslationProcessor().translate("Cancel Package"), "VALIDATION", this.getTranslationProcessor().translate("Package is already Cancelled, cannot be Cancelled again."));
                    }
                    if (TYPE_PKG.equals(packagetype = primary.getString(i, "packagetype", this.getOldPrimaryValue(primary, i, "packagetype")))) {
                        if (this.database.getPreparedCount("select count(trackitemid) from trackitem where currentstorageunitid = (select su.storageunitid from storageunit su where su.linksdcid = 'LV_Package' and su.linkkeyid1 = ?)", new String[]{packageid}) != 0) {
                            this.setError(this.getTranslationProcessor().translate("Cancel Package"), "VALIDATION", this.getTranslationProcessor().translate("Only empty package can be cancelled"));
                        }
                    } else if (TYPE_CDT.equals(packagetype)) {
                        String senderdepartmentid;
                        List<String> userDepartmentList = OpalUtil.toList(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";");
                        if (!userDepartmentList.contains(senderdepartmentid = primary.getString(i, "senderdepartmentid", this.getOldPrimaryValue(primary, i, "senderdepartmentid")))) {
                            this.setError(this.getTranslationProcessor().translate("Cancel Transfer"), "VALIDATION", this.getTranslationProcessor().translate("User is not a member of Transfer's origination department"));
                        }
                        if (STATUS_SHIPPED.equals(oldpackagestatus)) {
                            int row = setCustodialDepartmentDS.addRow();
                            setCustodialDepartmentDS.setString(row, "trackitemid", OpalUtil.getColumnValue(this.getQueryProcessor(), "trackitem", "trackitemid", "linksdcid='LV_Package' and linkkeyid1=?", new String[]{packageid}));
                            setCustodialDepartmentDS.setString(row, "custodialdepartmentid", senderdepartmentid);
                        }
                    }
                } else if (STATUS_RECEIVED.equals(packagestatus)) {
                    StringBuilder sql = new StringBuilder();
                    if (this.getConnectionProcessor().getSapphireConnection().isOracle()) {
                        sql.append("SELECT t.trackitemid, t.linksdcid, t.linkkeyid1");
                        sql.append(" FROM trackitem t, s_sample s");
                        sql.append(" WHERE t.currentstorageunitid IN (");
                        sql.append(" SELECT su.storageunitid FROM storageunit su");
                        sql.append(" CONNECT BY PRIOR su.storageunitid = su.parentid");
                        sql.append(" START WITH su.linksdcid = 'LV_Package' AND su.linkkeyid1 = ?)");
                        sql.append(" and t.linksdcid = 'Sample'");
                        sql.append(" and s.s_sampleid = t.linkkeyid1 and s.samplestatus = 'Initial'");
                    } else {
                        sql.append("WITH StorageUnitTree (storageunitid) AS (");
                        sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                        sql.append(" WHERE su.linksdcid = 'LV_Package' and su.linkkeyid1 = ?");
                        sql.append(" UNION ALL");
                        sql.append(" SELECT su.storageunitid FROM storageunit AS su");
                        sql.append(" INNER JOIN StorageUnitTree AS d ON su.parentid = d.storageunitid");
                        sql.append(")");
                        sql.append(" select t.trackitemid, t.linksdcid, t.linkkeyid1");
                        sql.append(" from trackitem t, s_sample s");
                        sql.append(" where t.currentstorageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)");
                        sql.append(" and t.linksdcid = 'Sample'");
                        sql.append(" and s.s_sampleid = t.linkkeyid1 and s.samplestatus = 'Initial'");
                    }
                    DataSet sampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
                    if (sampleDs != null && sampleDs.size() > 0) {
                        PropertyList props5 = new PropertyList();
                        props5.setProperty("sdcid", "Sample");
                        props5.setProperty("keyid1", sampleDs.getColumnValues("linkkeyid1", ";"));
                        props5.setProperty("samplestatus", STATUS_RECEIVED);
                        props5.setProperty("receiveddt", "n");
                        props5.setProperty("receivedby", "(system)".equals(this.getConnectionInfo().getSysuserId()) ? "" : this.getConnectionInfo().getSysuserId());
                        props5.setProperty("__sdcruleconfirm", "Y");
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props5);
                    }
                } else if (STATUS_EMPTIED.equals(packagestatus) && (ds = qp.getPreparedSqlDataSet("select t.trackitemid from trackitem t where t.currentstorageunitid = (select storageunit.storageunitid from storageunit where storageunit.linkkeyid1 = ? )", (Object[])new String[]{packageid})) != null && ds.size() > 0) {
                    try {
                        props = new PropertyList();
                        props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
                        props.setProperty("currentstorageunitid", "");
                        props.setProperty("auditreason", "Package is marked empty");
                        ap.processActionClass(EditTrackItem.class.getName(), props);
                        if (ap.hasInfoErrors()) {
                            this.setErrors(ap.getErrorHandler());
                        }
                    }
                    catch (ActionException e) {
                        this.setErrors(e.getErrorHandler());
                        this.logger.error("Error running " + EditTrackItem.class.getName(), e);
                    }
                }
            }
            if (!shipPackage || !STATUS_CREATED.equals(oldpackagestatus)) continue;
            shipPackageSet.add(packageid);
        }
        if (setCustodialDepartmentDS.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", setCustodialDepartmentDS.getColumnValues("trackitemid", ";"));
            props.setProperty("custodialdepartmentid", setCustodialDepartmentDS.getColumnValues("custodialdepartmentid", ";"));
            props.setProperty("propsmatch", "Y");
            props.setProperty("auditreason", "Transferred cancelled by " + this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        }
        if (shipPackageSet.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_LV_PACKAGE);
            props.setProperty("keyid1", OpalUtil.toDelimitedString(shipPackageSet, ";"));
            props.setProperty("packagestatus", STATUS_SHIPPED);
            props.setProperty("__sdcruleconfirm", "Y");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    @Override
    public void preDelete(String string, PropertyList list) throws SapphireException {
    }

    @Override
    public void postDelete(String string, PropertyList actionProps) throws SapphireException {
        PropertyList props;
        String packageid = actionProps.getProperty("keyid1");
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select trackitemid from trackitem");
        sql.append(" where linksdcid = '").append(SDC_LV_PACKAGE).append("'");
        sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(packageid, ";")).append(")");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }
        if (!"Y".equals(actionProps.getProperty("__sudeleteflag"))) {
            sql.setLength(0);
            safeSQL.reset();
            sql.append("select t.currentstorageunitid ");
            sql.append(" from trackitem t");
            sql.append(" where t.currentstorageunitid in ( ");
            sql.append("select storageunitid from storageunit");
            sql.append(" where linksdcid = '").append(SDC_LV_PACKAGE).append("'");
            sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(packageid, ";")).append("))");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                boolean errorFlag = false;
                for (int i = 0; i < ds.size(); ++i) {
                    if (StringUtil.getLen(ds.getValue(i, "linkstorageunitid")) != 0L) continue;
                    errorFlag = true;
                }
                if (errorFlag) {
                    throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Deleting a Package with Trackitems is not allowed."));
                }
            }
            safeSQL.reset();
            sql.setLength(0);
            sql.append("select storageunitid from storageunit");
            sql.append(" where linksdcid = '").append(SDC_LV_PACKAGE).append("'");
            sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(packageid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                props.setProperty("_deletelinksdi", "N");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void checkPackageColumns(String packageId, String packageStatus, boolean forceUpdate, String oldpackagestatus, PropertyList list) throws SapphireException {
        String custodialuserid = "";
        String custodialdepartmentid = "";
        String newcustodialuserid = "";
        String newcustodialdepartmentid = "";
        String trackitemid = "";
        String useraddressid = "";
        String useraddresstype = "";
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select t.trackitemid, t.custodialuserid, t.custodialdepartmentid from trackitem t where t.linksdcid = 'LV_Package' and t.linkkeyid1 = " + safeSQL.addVar(packageId);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            custodialuserid = ds.getValue(0, "custodialuserid");
            custodialdepartmentid = ds.getValue(0, "custodialdepartmentid");
            trackitemid = ds.getString(0, "trackitemid");
            if (custodialuserid == null) {
                custodialuserid = "";
            }
            if (custodialdepartmentid == null) {
                custodialdepartmentid = "";
            }
        }
        String contactfunction = null;
        switch (packageStatus) {
            case "Created": 
            case "Expected": {
                String senderdepartmentid = LV_Package.getSenderDepartmentId(this.getQueryProcessor(), packageId);
                contactfunction = "CreatedBy";
                newcustodialuserid = "";
                if (oldpackagestatus.equals(STATUS_SHIPPED)) {
                    newcustodialdepartmentid = senderdepartmentid;
                    break;
                }
                if (packageStatus.equals(STATUS_CREATED)) {
                    newcustodialdepartmentid = senderdepartmentid;
                    break;
                }
                if (Department.isCustodialDomain(this.getQueryProcessor(), senderdepartmentid)) {
                    newcustodialdepartmentid = senderdepartmentid;
                    break;
                }
                newcustodialdepartmentid = "";
                break;
            }
            case "Packed": {
                contactfunction = "PackedBy";
                newcustodialuserid = this.getConnectionInfo().getSysuserId();
                newcustodialdepartmentid = "";
                break;
            }
            case "Shipped": {
                contactfunction = "ShippedBy";
                newcustodialuserid = "";
                newcustodialdepartmentid = "Transit";
                break;
            }
            case "Received": {
                contactfunction = "ReceivedBy";
                newcustodialuserid = this.getConnectionInfo().getSysuserId();
                newcustodialdepartmentid = LV_Package.getRecipientDepartmentId(this.getQueryProcessor(), packageId);
                break;
            }
            case "UnPacked": {
                contactfunction = "UnpackedBy";
                newcustodialuserid = "";
                newcustodialdepartmentid = "";
                break;
            }
            case "Emptied": {
                contactfunction = "EmptiedBy";
                newcustodialuserid = "";
                newcustodialdepartmentid = "";
                break;
            }
            case "On Hold": {
                contactfunction = "OnHoldBy";
                newcustodialuserid = "";
                newcustodialdepartmentid = "";
                break;
            }
            case "Cancelled": {
                newcustodialuserid = this.getConnectionInfo().getSysuserId();
                newcustodialdepartmentid = this.getConnectionInfo().getDefaultDepartment();
            }
        }
        if (contactfunction != null) {
            String usertype = this.getConnectionInfo().getUserType();
            if ("Q".equals(usertype) || "P".equals(usertype)) {
                if (!this.database.checkPreparedExists("select addressid from address where addressid = ? and addresstype = 'Contact'", new String[]{"LV Portal Contact"})) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Address");
                    props.setProperty("keyid1", "LV Portal Contact");
                    props.setProperty("keyid2", "Contact");
                    props.setProperty("copies", "1");
                    props.setProperty("externalflag", "Y");
                    props.setProperty("activeflag", "N");
                    props.setProperty("tracelogid", list.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", list.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", list.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", list.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                }
                useraddressid = "LV Portal Contact";
                useraddresstype = "Contact";
            } else {
                Map<String, String> useraddressmap = LV_Package.getUserAddressInfo(this.database, this.getConnectionInfo().getSysuserId());
                useraddressid = useraddressmap.get("addressid");
                useraddresstype = useraddressmap.get("addresstype");
                if (OpalUtil.isEmpty(useraddressid)) {
                    if (!this.database.checkPreparedExists("select addressid from address where addressid = ? and addresstype = 'Contact'", new String[]{"LV LIMS Contact"})) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "Address");
                        props.setProperty("keyid1", "LV LIMS Contact");
                        props.setProperty("keyid2", "Contact");
                        props.setProperty("copies", "1");
                        props.setProperty("externalflag", "N");
                        props.setProperty("activeflag", "Y");
                        props.setProperty("tracelogid", list.getProperty("tracelogid", ""));
                        props.setProperty("auditreason", list.getProperty("auditreason", ""));
                        props.setProperty("auditactivity", list.getProperty("auditactivity", ""));
                        props.setProperty("auditsignedflag", list.getProperty("auditsignedflag", ""));
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    }
                    useraddressid = "LV LIMS Contact";
                    useraddresstype = "Contact";
                }
            }
            this.setPackageSDIAddress(packageId, useraddressid, useraddresstype, contactfunction, forceUpdate);
        }
        if (!custodialuserid.equals(newcustodialuserid) || !custodialdepartmentid.equals(newcustodialdepartmentid)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", trackitemid);
            props.setProperty("custodialuserid", newcustodialuserid);
            props.setProperty("custodialdepartmentid", newcustodialdepartmentid);
            props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
            if (newcustodialuserid.length() == 0 && newcustodialdepartmentid.length() == 0) {
                props.setProperty("__sdcruleignore", "Y");
            }
            props.setProperty("tracelogid", list.getProperty("tracelogid", ""));
            props.setProperty("auditreason", list.getProperty("auditreason", ""));
            props.setProperty("auditactivity", list.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", list.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    private void checkPackageCDRule(DataSet primary) {
        PackageCDRule rule = new PackageCDRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "packagetype") && !this.hasPrimaryValueChanged(primary, i, "packagestatus") && !this.hasPrimaryValueChanged(primary, i, "senderdepartmentid") && !this.hasPrimaryValueChanged(primary, i, "recipientdepartmentid")) continue;
            String packageid = primary.getString(i, "s_packageid");
            try {
                rule.processRule(packageid);
                continue;
            }
            catch (SapphireException se) {
                this.setError(rule.getClass().getName(), "VALIDATION", se.getMessage());
            }
        }
    }

    public void setPackageSDIAddress(String packageid, String addressid, String addresstype, String contactfunction, boolean forceUpdate) throws SapphireException {
        if (addressid != null && addressid.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_LV_PACKAGE);
            props.setProperty("keyid1", packageid);
            props.setProperty("addressid", addressid);
            props.setProperty("addresstype", addresstype);
            props.setProperty("contactfunction", contactfunction);
            props.setProperty("functiondt", "n");
            props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select contactfunction from sdiaddress where sdcid='LV_Package' and keyid1=" + safeSQL.addVar(packageid) + " and contactfunction=" + safeSQL.addVar(contactfunction);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() <= 0) {
                try {
                    this.getActionProcessor().processActionClass(AddSDIAddress.class.getName(), props);
                    ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                    if (errorHandler != null && errorHandler.hasInfoErrors()) {
                        this.setErrors(this.getActionProcessor().getErrorHandler());
                    }
                }
                catch (ActionException e) {
                    this.setErrors(e.getErrorHandler());
                }
            }
        }
    }

    public static Map<String, String> getUserAddressInfo(DBAccess database, String userid) throws SapphireException {
        String addressid = "";
        String addresstype = "";
        database.createPreparedResultSet("select addressid, addresstype from sdiaddress where sdcid='User' and keyid1=? order by usersequence", new Object[]{userid});
        if (database.getNext()) {
            addressid = database.getString("addressid");
            addresstype = database.getString("addresstype");
        }
        database.closeResultSet();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("addressid", addressid);
        map.put("addresstype", addresstype);
        return map;
    }

    public static String getStatus(DBAccess database, String packageId) throws SapphireException {
        String packageStatus = "";
        String sql = "SELECT packagestatus FROM s_package WHERE s_packageid=?";
        database.createPreparedResultSet(sql, new Object[]{packageId});
        if (database.getNext()) {
            packageStatus = database.getString("packagestatus");
        }
        database.closeResultSet();
        return packageStatus;
    }

    public static String getStatus(QueryProcessor queryProcessor, String packageid) {
        return OpalUtil.getColumnValue(queryProcessor, TABLEID, "packagestatus", "s_packageid=?", new String[]{packageid});
    }

    public static String getRecipientDepartmentId(DBAccess database, String packageId) throws SapphireException {
        String destCD = "";
        String sql = "SELECT recipientdepartmentid FROM s_package WHERE s_packageid=?";
        database.createPreparedResultSet(sql, new Object[]{packageId});
        if (database.getNext()) {
            destCD = database.getString("recipientdepartmentid");
        }
        database.closeResultSet();
        return destCD;
    }

    public static String getSenderDepartmentId(DBAccess database, String packageId) throws SapphireException {
        String destCD = "";
        String sql = "SELECT senderdepartmentid FROM s_package WHERE s_packageid=?";
        database.createPreparedResultSet(sql, new Object[]{packageId});
        if (database.getNext()) {
            destCD = database.getString("senderdepartmentid");
        }
        database.closeResultSet();
        return destCD;
    }

    public static String getSenderDepartmentId(QueryProcessor queryProcessor, String packageid) {
        return OpalUtil.getColumnValue(queryProcessor, TABLEID, "senderdepartmentid", "s_packageid=?", new String[]{packageid});
    }

    public static String getRecipientDepartmentId(QueryProcessor queryProcessor, String packageid) {
        return OpalUtil.getColumnValue(queryProcessor, TABLEID, "recipientdepartmentid", "s_packageid=?", new String[]{packageid});
    }

    public static String getPackageType(DBAccess database, String packageId) throws SapphireException {
        String packageType = "";
        String sql = "SELECT packagetype FROM s_package WHERE s_packageid=?";
        database.createPreparedResultSet(sql, new Object[]{packageId});
        if (database.getNext() && (packageType = database.getString("packagetype")) == null) {
            packageType = "";
        }
        database.closeResultSet();
        return packageType;
    }

    public static String getPackageType(QueryProcessor queryProcessor, String packageid) {
        return OpalUtil.getColumnValue(queryProcessor, TABLEID, "packagetype", "s_packageid=?", new String[]{packageid});
    }

    public static String getContentType(QueryProcessor queryProcessor, String packageid) {
        return OpalUtil.getColumnValue(queryProcessor, TABLEID, "contenttype", "s_packageid=?", new String[]{packageid});
    }

    public static boolean hasTILSample(QueryProcessor queryProcessor, String packageId) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.LINKKEYID1");
        sql.append(" FROM STORAGEUNIT T1, TRACKITEM T2, S_SAMPLE T3");
        sql.append(" WHERE T1.LINKSDCID = 'LV_Package'");
        sql.append(" AND T1.LINKKEYID1 = ").append(safeSQL.addVar(packageId));
        sql.append(" AND T2.CURRENTSTORAGEUNITID = T1.STORAGEUNITID");
        sql.append(" AND T2.LINKSDCID = 'Sample' ");
        sql.append(" AND T3.S_SAMPLEID = T2.LINKKEYID1");
        sql.append(" AND T3.STORAGESTATUS = '").append("Temporary In Lab").append("'");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return ds != null && ds.size() > 0;
    }

    private void setSecurityDepartment(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        primary.addColumn("securitydepartment", 0);
        SafeSQL safeSQL = new SafeSQL();
        String getPackageDetailsSQL = "SELECT s_packageid, senderdepartmentid, recipientdepartmentid, securitydepartment FROM s_package WHERE s_packageid IN (" + safeSQL.addVar(primary.getColumnValues("s_packageid", "','")) + ")";
        DataSet packageDetails = this.getQueryProcessor().getPreparedSqlDataSet(getPackageDetailsSQL, safeSQL.getValues());
        for (int i = 0; i < primary.size(); ++i) {
            String packageid = primary.getValue(i, "s_packageid");
            String packagestatus = primary.getString(i, "packagestatus", "");
            if (packagestatus.length() <= 0) continue;
            String senderdepartmentid = primary.getString(i, "senderdepartmentid", "");
            String recipientdepartmentid = primary.getString(i, "recipientdepartmentid", "");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("s_packageid", packageid);
            DataSet dsfilter = packageDetails.getFilteredDataSet(filter);
            if (dsfilter != null && dsfilter.size() > 0) {
                if (senderdepartmentid.length() == 0) {
                    senderdepartmentid = dsfilter.getString(0, "senderdepartmentid", "");
                }
                if (recipientdepartmentid.length() == 0) {
                    recipientdepartmentid = dsfilter.getString(0, "recipientdepartmentid");
                }
            }
            if (packagestatus.equalsIgnoreCase(STATUS_CREATED)) {
                primary.setValue(i, "securitydepartment", senderdepartmentid);
                continue;
            }
            if (recipientdepartmentid.length() <= 0) continue;
            boolean isCustodialDepartment = Department.isCustodialDomain(this.getQueryProcessor(), recipientdepartmentid);
            primary.setString(i, "securitydepartment", isCustodialDepartment ? recipientdepartmentid : senderdepartmentid);
        }
    }
}

