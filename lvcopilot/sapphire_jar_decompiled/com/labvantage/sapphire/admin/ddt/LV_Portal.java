/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.portal.PortalValueTree;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseWebSSOHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Portal
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            primary.setString(-1, "coreflag", "N");
        } else {
            primary.setString(-1, "coreflag", "Y");
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.loadPortalSSOProps(sdiData.getDataset("primary"));
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId()) || Configuration.getCompcode(this.connectionInfo.getDatabaseId()).length() == 0) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String sql = "SELECT p.portalid, p.coreflag, p.compcode FROM portal p WHERE p.portalid = ?";
                DataSet modifiedApp = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{primary.getString(i, "portalid")});
                if ((!modifiedApp.getString(0, "coreflag", "").equalsIgnoreCase("Y") || Configuration.isDevmode(this.connectionInfo.getDatabaseId())) && (modifiedApp.getString(0, "compcode", "").isEmpty() || !Configuration.getCompcode(this.connectionInfo.getDatabaseId()).equalsIgnoreCase(modifiedApp.getString(0, "compcode"))) || !this.hasPrimaryValueChanged(primary, i, "coreflag") && !this.hasPrimaryValueChanged(primary, i, "compcode")) continue;
                throw new SapphireException("CorePortalCheck", "VALIDATION", this.getTranslationProcessor().translate("You are not allowed to modify any of the Core columns."));
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String sql;
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            sql = "SELECT p.portalid, p.coreflag FROM portal p, rsetitems r WHERE p.portalid = r.keyid1 AND r.rsetid = ? AND p.coreflag = 'Y'";
            DataSet deletedPortals = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
            if (deletedPortals.getRowCount() > 0) {
                throw new SapphireException("DeleteCorePortal", "VALIDATION", this.getTranslationProcessor().translate("Core Portal SDIs cannot be deleted."));
            }
        }
        sql = "SELECT p.portalid FROM portal p, rsetitems r WHERE p.portalid = r.keyid1 AND r.rsetid = ? AND p.compcode is not null AND p.compcode != ?";
        DataSet deletedCompPortals = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid, Configuration.getCompcode(this.connectionInfo.getDatabaseId())});
        if (deletedCompPortals.getRowCount() > 0) {
            throw new SapphireException("DeleteComponentPortal", "VALIDATION", this.getTranslationProcessor().translate("Component Portal SDIs cannot be deleted."));
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.flushPortalCaches(sdiData.getDataset("primary"));
        this.loadPortalSSOProps(sdiData.getDataset("primary"));
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.flushPortalCaches(actionProps);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.flushPortalCaches(sdiData.getDataset("portalapp"));
        if (sdiData.getDatasets().contains("portaldepartment")) {
            this.addGlobalApps(sdiData.getDataset("portaldepartment"));
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.flushPortalCaches(sdiData.getDataset("portalapp"));
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.flushPortalCaches(actionProps);
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
        this.flushPortalCaches(attachment.getKeyId1());
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
        this.flushPortalCaches(attachment.getKeyId1());
    }

    @Override
    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
        this.flushPortalCaches(attachment.getKeyId1());
    }

    private void flushPortalCaches(DataSet ds) {
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String portalid = ds.getString(i, "portalid", ds.getString(i, "keyid1"));
                this.flushPortalCaches(portalid);
            }
        }
    }

    private void flushPortalCaches(PropertyList actionProps) {
        String[] portals;
        for (String portalid : portals = StringUtil.split(actionProps.getProperty("portalid", actionProps.getProperty("keyid1")), ";")) {
            this.flushPortalCaches(portalid);
        }
    }

    private void flushPortalCaches(String portalid) {
        CacheUtil.remove(this.getDatabaseid(), "PortalDetails", portalid);
        CacheUtil.remove(this.getDatabaseid(), "PortalApps", portalid);
        CacheUtil.remove(this.getDatabaseid(), "PortalProps", portalid);
        CacheUtil.remove(this.getDatabaseid(), "PortalCommands", portalid);
    }

    private void addGlobalApps(DataSet portalClients) throws ActionException {
        String portalId = portalClients.getString(0, "portalid");
        String clientIds = portalClients.getColumnValues("departmentid", ";");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT pd.portalid, pd.departmentid, pa.appid  FROM portaldepartment pd, portalapp pa  WHERE pd.portalid=" + safeSQL.addVar(portalId) + " AND pd.departmentid IN (" + safeSQL.addIn(clientIds, ";") + ") AND pa.globalappflag = 'Y' AND NOT EXISTS (SELECT 1 FROM appdepartment ad WHERE ad.appid = pa.appid AND ad.departmentid = pd.departmentid) ORDER BY pd.departmentid";
        DataSet missingGlobalApps = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (missingGlobalApps.getRowCount() > 0) {
            ArrayList<DataSet> deptGroup = missingGlobalApps.getGroupedDataSets("departmentid");
            for (int i = 0; i < deptGroup.size(); ++i) {
                DataSet deptApps = deptGroup.get(i);
                PropertyList addProps = new PropertyList();
                addProps.setProperty("sdcid", "Department");
                addProps.setProperty("linkid", "Department Apps");
                addProps.setProperty("keyid1", deptApps.getColumnValues("departmentid", ";"));
                addProps.setProperty("appid", deptApps.getColumnValues("appid", ";"));
                this.getActionProcessor().processAction("AddSDIDetail", "1", addProps);
            }
        }
    }

    private void loadPortalSSOProps(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "productvaluetree") && !this.hasPrimaryValueChanged(primary, i, "componentvaluetree") && !this.hasPrimaryValueChanged(primary, i, "valuetree")) continue;
            LV_Portal.loadPortalSSOPropsToCache((DBUtil)this.database, primary.getString(i, "portalid"), this.getDatabaseid());
        }
    }

    public static void loadPortalSSOPropsToCache(DBUtil db, String portalId, String databaseid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        db.createPreparedResultSet("PortalDefs", "SELECT portalid, productvaluetree, componentvaluetree, valuetree FROM portal" + (portalId != null && portalId.length() > 0 ? " WHERE portalid = " + safeSQL.addVar(portalId) : ""), safeSQL.getValues());
        DataSet ds = new DataSet();
        ds.setResultSet(db.getResultSet("PortalDefs"), true, db.getDbms());
        for (int i = 0; i < ds.getRowCount(); ++i) {
            PropertyList portalProps = PortalValueTree.getPortalPropertyList(ds.getClob(i, "productvaluetree", ""), ds.getClob(i, "componentvaluetree", ""), ds.getClob(i, "valuetree", ""));
            BaseWebSSOHandler.setPortalSSOProps(databaseid, ds.getString(i, "portalid"), portalProps);
        }
    }
}

