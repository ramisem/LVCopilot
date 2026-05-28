/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.ajax.util.SetActionProgressStatus;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sms.CreateStorageUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddPackage
extends BaseAction
implements sapphire.action.AddPackage {
    static final String LABVANTAGE_CVS_ID = "$Revision: 87059 $";
    public static final String ID = "AddPackage";
    public static final String VERSION = "1";
    static List<String> addExcludeProperties = new ArrayList<String>();

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String packageType;
        String forceUpdate = actionProps.getProperty("__sdcruleconfirm", "N");
        String trackProgressID = actionProps.getProperty("__trackprogressid", "").trim();
        String tracelogid = actionProps.getProperty("tracelogid", "");
        String auditreason = actionProps.getProperty("auditreason", "");
        if (tracelogid.length() == 0 && auditreason.length() == 0) {
            actionProps.setProperty("auditreason", ID);
        }
        String packageStatus = "PKG".equals(packageType = actionProps.getProperty("packagetype", "PKG")) && "Y".equals(actionProps.getProperty("expected", "N")) ? "Expected" : "Created";
        try {
            SafeSQL safeSQL;
            String transitlocation;
            CharSequence sql;
            ActionProcessor ap = this.getActionProcessor();
            String templocation = "";
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Package");
            props.setProperty("packagetype", packageType);
            props.setProperty("packagestatus", packageStatus);
            props.setProperty("__sdcruleconfirm", forceUpdate);
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            for (Object o : actionProps.keySet()) {
                String property = (String)o;
                if (addExcludeProperties.contains(property)) continue;
                if ("__templocation".equals(property)) {
                    templocation = actionProps.getProperty(property);
                }
                props.setProperty(property, actionProps.getProperty(property, ""));
            }
            this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Adding Package..."));
            ap.processAction("AddSDI", VERSION, props);
            String packageid = props.getProperty("newkeyid1");
            String packagetrackitemid = props.getProperty("trackitemid");
            ErrorHandler errorHandler = ap.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
            props.clear();
            props.setProperty("createsu", "Y");
            props.setProperty("newkeyid1", packageid);
            props.setProperty("linksdcid", "LV_Package");
            props.setProperty("linkpropnodeid", "No Layout|Package");
            props.setProperty("nodeid", "Package");
            props.setProperty("propertytreeid", "No Layout");
            props.setProperty("size", "0");
            props.setProperty("maxtiallowed", "-1");
            props.setProperty("moveableflag", "Y");
            props.setProperty("__sdcruleconfirm", forceUpdate);
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            ap.processActionClass(CreateStorageUnit.class.getName(), props);
            String storageunitid = props.getProperty("storageunitid");
            errorHandler = ap.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
            if (OpalUtil.isNotEmpty(templocation)) {
                props.clear();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", storageunitid);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                props.setProperty("parentid", templocation);
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Adding items into Package..."));
            ArrayList<String> trackitemList = new ArrayList<String>();
            if (StringUtil.getLen(actionProps.getProperty("trackitemid", "")) > 0L) {
                String trackitemid = actionProps.getProperty("trackitemid", "");
                sql = "select t.trackitemid, (select su.linkkeyid1 from storageunit su where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package') packageid";
                sql = (String)sql + " from trackitem t";
                sql = (String)sql + " where t.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)";
                String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet((String)sql, (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                for (int i = 0; i < ds.size(); ++i) {
                    if (ds.getString(i, "packageid", "").length() != 0) continue;
                    trackitemList.add(ds.getString(i, "trackitemid"));
                }
                if (trackitemList.size() == 0) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("All Items already exists in another Package"));
                }
            } else if (StringUtil.getLen(actionProps.getProperty("contentsdcid", "")) > 0L && StringUtil.getLen(actionProps.getProperty("contentkeyid1", "")) > 0L) {
                String contentsdcid = actionProps.getProperty("contentsdcid", "");
                String contentkeyid1 = actionProps.getProperty("contentkeyid1", "");
                String contentkeyid2 = actionProps.getProperty("contentkeyid2", "");
                String contentkeyid3 = actionProps.getProperty("contentkeyid3", "");
                String tableid = this.getSDCProcessor().getProperty(contentsdcid, "tableid");
                String keycolid1 = this.getSDCProcessor().getProperty(contentsdcid, "keycolid1");
                String keycolid2 = this.getSDCProcessor().getProperty(contentsdcid, "keycolid2");
                String keycolid3 = this.getSDCProcessor().getProperty(contentsdcid, "keycolid3");
                int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(contentsdcid, "keycolumns"));
                String rsetid = this.getDAMProcessor().createRSet(contentsdcid, contentkeyid1, contentkeyid2.length() > 0 ? contentkeyid1 : null, contentkeyid3.length() > 0 ? contentkeyid3 : null);
                StringBuilder sql2 = new StringBuilder();
                sql2.append("select s.").append(keycolid1).append(",");
                if (keycolumns > 1) {
                    sql2.append("s.").append(keycolid2).append(",");
                    if (keycolumns > 2) {
                        sql2.append("s.").append(keycolid3).append(",");
                    }
                }
                sql2.append(" t.trackitemid, t.custodialuserid,");
                sql2.append(" (select su.linkkeyid1 from storageunit su where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package') packageid");
                sql2.append(" from ").append(tableid).append(" s left outer join trackitem t on t.linksdcid = ?");
                sql2.append(" and t.linkkeyid1 = s.").append(keycolid1);
                if (keycolumns > 1) {
                    sql2.append(" and t.linkkeyid2 = s.").append(keycolid2);
                    if (keycolumns > 2) {
                        sql2.append(" and t.linkkeyid3 = s.").append(keycolid3);
                    }
                }
                sql2.append(" , rsetitems r");
                sql2.append(" where r.sdcid = ? and r.keyid1 = s.").append(keycolid1);
                if (keycolumns > 1) {
                    sql2.append(" and r.keyid2 = s.").append(keycolid2);
                    if (keycolumns > 2) {
                        sql2.append(" and r.keyid3 = s.").append(keycolid3);
                    }
                }
                sql2.append(" and r.rsetid = ?");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2.toString(), (Object[])new String[]{contentsdcid, contentsdcid, rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                if (OpalUtil.isNotEmpty(ds)) {
                    DataSet addTrackItemDS = new DataSet();
                    for (int i = 0; i < ds.size(); ++i) {
                        if (ds.getString(i, "trackitemid", "").length() == 0) {
                            int row = addTrackItemDS.addRow();
                            addTrackItemDS.setString(row, "linksdcid", contentsdcid);
                            addTrackItemDS.setString(row, "linkkeyid1", ds.getString(i, keycolid1));
                            if (keycolumns <= 1) continue;
                            addTrackItemDS.setString(row, "linkkeyid2", ds.getString(i, keycolid2));
                            if (keycolumns <= 2) continue;
                            addTrackItemDS.setString(row, "linkkeyid3", ds.getString(i, keycolid3));
                            continue;
                        }
                        if (ds.getString(i, "packageid", "").length() != 0) continue;
                        trackitemList.add(ds.getString(i, "trackitemid", ""));
                    }
                    if (addTrackItemDS.size() > 0) {
                        props.clear();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("linksdcid", addTrackItemDS.getColumnValues("linksdcid", ";"));
                        props.setProperty("linkkeyid1", addTrackItemDS.getColumnValues("linkkeyid1", ";"));
                        if (keycolumns > 1) {
                            props.setProperty("linkkeyid2", addTrackItemDS.getColumnValues("linkkeyid2", ";"));
                            if (keycolumns > 2) {
                                props.setProperty("linkkeyid3", addTrackItemDS.getColumnValues("linkkeyid3", ";"));
                            }
                        }
                        props.setProperty("custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
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
                if (trackitemList.size() == 0) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("All Items already exists in another Package"));
                }
            } else if (StringUtil.getLen(actionProps.getProperty("requestid")) > 0L) {
                String sql3 = "select d.s_requestitemdetailid, d.requestid, d.linksdcid, d.linkkeyid1, d.requestitemdetailstatus,       (select su.linkkeyid1 from storageunit su, trackitem t       where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package'         and t.linksdcid = d.linksdcid and t.linkkeyid1 = d.linkkeyid1) packageid1,       (select su.linkkeyid1 from storageunit su, trackitem t        where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package'          and d.linksdcid = 'TrackItemSDC' and t.trackitemid = d.linkkeyid1) packageid2,       (case when d.linksdcid = 'TrackItemSDC' then d.linkkeyid1 else (select t.trackitemid        from trackitem t where t.linksdcid = d.linksdcid and t.linkkeyid1 = d.linkkeyid1) end) trackitemid from s_requestitemdetail d where d.requestid = ? and requestitemdetailstatus = 'Pending'";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql3, (Object[])new String[]{actionProps.getProperty("requestid")});
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
                        props.setProperty("custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
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
                if (trackitemList.size() == 0) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("All Items already exists in another Package"));
                }
            }
            if (trackitemList.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(trackitemList, ";"));
                props.setProperty("currentstorageunitid", storageunitid);
                props.setProperty("__sdcruleconfirm", forceUpdate);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                ap.processAction("EditSDI", VERSION, props);
                errorHandler = ap.getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            if ("Y".equals(actionProps.getProperty("ship", "N"))) {
                this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Shipping Package..."));
                props.clear();
                props.setProperty("sdcid", "LV_Package");
                props.setProperty("keyid1", packageid);
                props.setProperty("packagestatus", "Shipped");
                props.setProperty("__sdcruleconfirm", forceUpdate);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                ap.processActionClass(EditSDI.class.getName(), props);
                errorHandler = ap.getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
                this.setInfoError("Package Shipped", this.getTranslationProcessor().translate("Package has been successfully created and shipped.") + " (" + packageid + ")");
                if ("CDT".equals(packageType) && StringUtil.getLen(transitlocation = actionProps.getProperty("transitlocation", "")) > 0L) {
                    String transitstorageunitid;
                    sql = new StringBuilder();
                    safeSQL = new SafeSQL();
                    ((StringBuilder)sql).append("select storageunitid from storageunit where (storageunitid = ").append(safeSQL.addVar(transitlocation)).append(" or");
                    ((StringBuilder)sql).append(" ( linksdcid = 'PhysicalStore' and linkkeyid1 = ").append(safeSQL.addVar(transitlocation)).append(" ))");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(((StringBuilder)sql).toString(), safeSQL.getValues());
                    if (ds != null && ds.size() == 1 && StringUtil.getLen(transitstorageunitid = ds.getValue(0, "storageunitid", "")) > 0L) {
                        props.clear();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("keyid1", packagetrackitemid);
                        props.setProperty("currentstorageunitid", transitstorageunitid);
                        props.setProperty("__sdcruleconfirm", forceUpdate);
                        props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                        ap.processActionClass(EditSDI.class.getName(), props);
                        errorHandler = ap.getErrorHandler();
                        if (errorHandler != null && errorHandler.hasInfoErrors()) {
                            this.setErrors(this.getActionProcessor().getErrorHandler());
                        }
                    }
                }
            } else {
                if ("CDT".equals(packageType) && StringUtil.getLen(transitlocation = actionProps.getProperty("transitlocation", "")) > 0L) {
                    String transitstorageunitid;
                    this.trackActionProgress(trackProgressID, this.getTranslationProcessor().translate("Putting Package in Transit location..."));
                    sql = new StringBuilder();
                    safeSQL = new SafeSQL();
                    ((StringBuilder)sql).append("select storageunitid from storageunit where (storageunitid = ").append(safeSQL.addVar(transitlocation)).append(" or");
                    ((StringBuilder)sql).append(" ( linksdcid = 'PhysicalStore' and linkkeyid1 = ").append(safeSQL.addVar(transitlocation)).append(" ))");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(((StringBuilder)sql).toString(), safeSQL.getValues());
                    if (ds != null && ds.size() == 1 && StringUtil.getLen(transitstorageunitid = ds.getValue(0, "storageunitid", "")) > 0L) {
                        props.clear();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("keyid1", packagetrackitemid);
                        props.setProperty("currentstorageunitid", transitstorageunitid);
                        props.setProperty("__sdcruleconfirm", forceUpdate);
                        props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                        ap.processActionClass(EditSDI.class.getName(), props);
                        errorHandler = ap.getErrorHandler();
                        if (errorHandler != null && errorHandler.hasInfoErrors()) {
                            this.setErrors(this.getActionProcessor().getErrorHandler());
                        }
                    }
                }
                this.setInfoError("Package Created", this.getTranslationProcessor().translate("Package has been successfully created.") + " (" + packageid + ")");
            }
            actionProps.setProperty("newkeyid1", packageid);
            actionProps.setProperty("packageid", packageid);
            actionProps.setProperty("packagestorageunitid", storageunitid);
            actionProps.setProperty("packagetrackitemid", packagetrackitemid);
            if ("Y".equals(actionProps.getProperty("ship", "N"))) {
                this.trackActionProgress(trackProgressID, "||COMPLETED||" + this.getTranslationProcessor().translate("Package has been successfully created and shipped.") + " (" + packageid + ")");
            } else {
                this.trackActionProgress(trackProgressID, "||COMPLETED||" + this.getTranslationProcessor().translate("Package has been successfully created.") + " (" + packageid + ")");
            }
        }
        catch (ActionException e) {
            this.trackActionProgress(trackProgressID, "||ERROR||" + e.getMessage());
            this.setErrors(e.getErrorHandler());
            throw new SapphireException(e);
        }
    }

    private void trackActionProgress(String trackProgressID, String message) {
        if (trackProgressID.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("actionprogressid", trackProgressID);
            props.setProperty("message", message);
            try {
                this.getActionProcessor().processActionClass(SetActionProgressStatus.class.getName(), props, true);
            }
            catch (ActionException e) {
                e.printStackTrace();
            }
        }
    }

    static {
        addExcludeProperties.add("packagetype");
        addExcludeProperties.add("packagestatus");
        addExcludeProperties.add("trackitemid");
        addExcludeProperties.add("ship");
        addExcludeProperties.add("auditreason");
        addExcludeProperties.add("auditactivity");
        addExcludeProperties.add("auditsignedflag");
        addExcludeProperties.add("expected");
        addExcludeProperties.add("__sdcruleconfirm");
    }
}

