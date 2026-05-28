/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;

public class LV_JobType
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet sysuserjobtype;
        if (list.getProperty("templatekeyid1").length() > 0 && (sysuserjobtype = sdiData.getDataset("sysuserjobtype")) != null) {
            sysuserjobtype.clear();
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary;
        String templateid;
        String string = templateid = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        if (templateid != null && templateid.length() > 0 && (primary = sdiData.getDataset("primary")) != null) {
            this.database.createPreparedResultSet("SELECT * from sdcjobtypesecurity where jobtypeid=?", new Object[]{templateid});
            DataSet sdcsecurity = new DataSet(this.database.getResultSet());
            if (sdcsecurity.getRowCount() > 0) {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    for (int j = 0; j < sdcsecurity.getRowCount(); ++j) {
                        sdcsecurity.setValue(j, "jobtypeid", primary.getString(i, "jobtypeid"));
                        sdcsecurity.setDate(j, "createdt", DateTimeUtil.getNowCalendar());
                        sdcsecurity.setValue(j, "createby", this.connectionInfo.getSysuserId());
                    }
                    DataSetUtil.insert(this.database, sdcsecurity, "sdcjobtypesecurity");
                }
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String jobtypeids = actionProps.getProperty("keyid1");
        String[] ids = StringUtil.split(jobtypeids, ";");
        for (int i = 0; i < ids.length; ++i) {
            String jobtypeid = ids[i];
            SafeSQL safeSQL = new SafeSQL();
            DataSet sysuserJobtypeDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sysuserid from sysuserjobtype where jobtypeid=" + safeSQL.addVar(jobtypeid), safeSQL.getValues());
            if (sysuserJobtypeDs != null && sysuserJobtypeDs.getRowCount() > 0) {
                this.throwError("JobTypeUsed", "VALIDATION", "Cannot delete Jobtype " + jobtypeid + " as it is assigned to user " + sysuserJobtypeDs.getValue(0, "sysuserid"));
            }
            safeSQL.reset();
            DataSet sdcjobtypesecurityDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * from sdcjobtypesecurity where jobtypeid=" + safeSQL.addVar(jobtypeid), safeSQL.getValues());
            if (sdcjobtypesecurityDs == null || sdcjobtypesecurityDs.getRowCount() <= 0) continue;
            this.throwError("JobTypeUsed", "VALIDATION", "Cannot delete Jobtype " + jobtypeid + " as it is assigned to SDC security for SDC " + sdcjobtypesecurityDs.getValue(0, "sdcid") + " operation " + sdcjobtypesecurityDs.getValue(0, "operationid"));
        }
    }

    @Override
    public void postGenerateSnapshot(Snapshot snapshot, boolean isPackaging) throws SapphireException {
        if (snapshot == null) {
            return;
        }
        SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
        SDISnapshotItem item = sdiSnapshot.getSnapshotItem();
        SDIData sdiData = sdiSnapshot.getSDIData(item);
        String jobtypeId = item.getKeyId1();
        String sql = "SELECT * FROM sdcjobtypesecurity WHERE jobtypeid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{jobtypeId});
        if (ds != null && ds.getRowCount() > 0) {
            sdiData.setDataset("sdcjobtypesecurity", ds);
        }
    }

    @Override
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        SDIData templateData;
        DataSet sdcjobtypesecurity;
        if (actionProps.get("sdidata") != null && actionProps.get("sdidata") instanceof SDIData && (sdcjobtypesecurity = (templateData = (SDIData)actionProps.get("sdidata")).getDataset("sdcjobtypesecurity")) != null && sdcjobtypesecurity.getRowCount() > 0) {
            sdiData.setDataset("sdcjobtypesecurity", sdcjobtypesecurity.copy());
        }
    }
}

