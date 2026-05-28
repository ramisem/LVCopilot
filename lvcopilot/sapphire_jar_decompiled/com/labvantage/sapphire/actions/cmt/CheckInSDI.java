/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckInSDI
extends BaseAction
implements sapphire.action.CheckInSDI {
    public static final String ID = "CheckInSDI";
    public static final String VERSION = "1";
    public static final String PROPERTY_REMOTE_CHECKIN = "remotecheckin";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        DataSet ds;
        if ("(system)".equals(this.connectionInfo.getSysuserId())) {
            throw new SapphireException(ID, "VALIDATION", "CheckInSDI action does not support running under \"(system)\" user. If running action from a Task, please set the property \"processassysuserid\" to a valid application user.");
        }
        SafeSQL safeSQL = new SafeSQL();
        String sdcid = actionProps.getProperty("sdcid").trim();
        String inputrsetid = actionProps.getProperty("rsetid").trim();
        boolean isCMTAdmin = this.getConnectionProcessor().getSapphireConnection().hasRole(CMTPolicy.getPolicy(this.getConnectionId(), "").getCMTAdminRoleID());
        if (OpalUtil.isNotEmpty(inputrsetid)) {
            ds = this.getDataSet(inputrsetid);
        } else {
            String changelogid = StringUtil.replaceAll(actionProps.getProperty("changelogid").trim(), "%3B", ";");
            if (changelogid.length() > 0) {
                String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.checkedoutbyuserid, changelog.checkedoutbydepartmentid,";
                sql = sql + " changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid, changelog.originalsnapshot";
                sql = sql + " from changelog";
                sql = sql + " where changelog.changelogid in (" + safeSQL.addIn(changelogid, ";") + ")";
                sql = sql + " and changelog.changelogstatus = 'Checked Out'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
            } else {
                String keyid1 = StringUtil.replaceAll(actionProps.getProperty("keyid1").trim(), "%3B", ";");
                String keyid2 = StringUtil.replaceAll(actionProps.getProperty("keyid2").trim(), "%3B", ";");
                String keyid3 = StringUtil.replaceAll(actionProps.getProperty("keyid3").trim(), "%3B", ";");
                String propertyTreeNodeIds = StringUtil.replaceAll(actionProps.getProperty("propertytreenodeid").trim(), "%3B", ";");
                if (sdcid.length() == 0 || keyid1.length() == 0) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing action input SDI"));
                }
                String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                ds = this.getDataSet(rsetid);
                this.getDAMProcessor().clearRSet(rsetid);
                if ("PropertyTree".equals(sdcid)) {
                    DataSet inputDS = new DataSet();
                    inputDS.addColumnValues("propertytreeid", 0, keyid1, ";");
                    inputDS.addColumnValues("propertytreenodeid", 0, propertyTreeNodeIds, ";");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    for (int i = ds.getRowCount() - 1; i >= 0; --i) {
                        String linkSDCId = ds.getString(i, "linksdcid", "");
                        if (!"PropertyTree".equals(linkSDCId)) continue;
                        String propertyTreeId = ds.getString(i, "linkkeyid1", "");
                        String propertyTreeNodeId = ds.getString(i, "propertytreenodeid", "");
                        if (propertyTreeNodeId.length() > 0) {
                            filterMap.clear();
                            filterMap.put("propertytreeid", propertyTreeId);
                            filterMap.put("propertytreenodeid", propertyTreeNodeId);
                            DataSet filterInputDS = inputDS.getFilteredDataSet(filterMap);
                            if (filterInputDS.getRowCount() != 0) continue;
                            ds.deleteRow(i);
                            continue;
                        }
                        boolean inputEntryFound = false;
                        filterMap.clear();
                        filterMap.put("propertytreeid", propertyTreeId);
                        DataSet filterInputDS = inputDS.getFilteredDataSet(filterMap);
                        for (int j = 0; j < filterInputDS.getRowCount(); ++j) {
                            if (filterInputDS.getString(j, "propertytreenodeid", "").length() != 0) continue;
                            inputEntryFound = true;
                            break;
                        }
                        if (inputEntryFound) continue;
                        ds.deleteRow(i);
                    }
                }
                actionProps.setProperty("changelogid", ds.getColumnValues("changelogid", ";"));
            }
        }
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                this.checkInSDI(actionProps, ds, i, isCMTAdmin);
            }
        }
    }

    private DataSet getDataSet(String rsetid) {
        String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.checkedoutbyuserid, changelog.checkedoutbydepartmentid,";
        sql = sql + " changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid, changelog.originalsnapshot";
        sql = sql + " from changelog, rsetitems";
        sql = sql + " where changelog.linksdcid = rsetitems.sdcid";
        sql = sql + " and changelog.linkkeyid1 = rsetitems.keyid1";
        sql = sql + " and changelog.linkkeyid2 = rsetitems.keyid2";
        sql = sql + " and changelog.linkkeyid3 = rsetitems.keyid3";
        sql = sql + " and rsetitems.rsetid = ?";
        sql = sql + " and changelog.changelogstatus = 'Checked Out'";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid}, true);
    }

    private void checkInSDI(PropertyList actionProps, DataSet ds, int row, boolean isCMTAdmin) throws SapphireException {
        String checkedoutbyuserid = ds.getString(row, "checkedoutbyuserid", "");
        String sdcid = ds.getString(row, "linksdcid", "");
        String keyid1 = ds.getString(row, "linkkeyid1", "");
        String keyid2 = ds.getString(row, "linkkeyid2", "");
        String keyid3 = ds.getString(row, "linkkeyid3", "");
        String propertyTreeNodeId = ds.getString(row, "propertytreenodeid", "");
        String sdi = sdcid + "," + keyid1 + "," + keyid2 + "," + keyid3;
        if (!isCMTAdmin && !checkedoutbyuserid.equals(this.getConnectionProcessor().getSapphireConnection().getSysuserId())) {
            String checkedoutbydepartmentid = ds.getString(row, "checkedoutbydepartmentid", "");
            if (checkedoutbydepartmentid.length() > 0) {
                if (!(";" + this.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";").contains(";" + checkedoutbydepartmentid + ";")) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to Check In SDI:") + sdi);
                }
            } else {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to Check In SDI:") + sdi);
            }
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_ChangeLog");
        props.setProperty("keyid1", ds.getString(row, "changelogid"));
        props.setProperty("changelogstatus", "Checked In");
        props.setProperty("checkedindt", "n");
        props.setProperty("checkedinby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        if (!"Y".equals(actionProps.getProperty("deleteflag")) || "Y".equals(actionProps.getProperty("deleteflag")) && ds.getValue(row, "originalsnapshot").length() == 0) {
            SDISnapshot snapshot = "PropertyTree".equals(sdcid) ? new SnapshotFactory(this.getConnectionId()).generatePropertyTreeSnapshot(keyid1, propertyTreeNodeId) : new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(sdcid, keyid1, keyid2, keyid3);
            if (snapshot == null) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Failed to generate snapshot"));
            }
            if ("Y".equals(actionProps.getProperty("deleteflag"))) {
                props.setProperty("originalsnapshot", snapshot.toXML());
            } else {
                props.setProperty("modifiedsnapshot", snapshot.toXML());
            }
        }
        props.setProperty("notes", actionProps.getProperty("notes"));
        props.setProperty("transferlogid", actionProps.getProperty("transferlogid"));
        props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
        props.setProperty("auditreason", actionProps.getProperty("auditreason"));
        props.setProperty("auditdt", actionProps.getProperty("auditdt"));
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
    }
}

