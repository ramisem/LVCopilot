/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.actions.storageunit.PopulateStorageUnitStats;
import com.labvantage.opal.actions.storageunit.SyncLastNodeCapacity;
import com.labvantage.opal.actions.storageunit.SyncStorageLabelPath;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.rules.ConnectByLoopRule;
import com.labvantage.sapphire.admin.ddt.rules.EventLog;
import com.labvantage.sapphire.admin.ddt.rules.StorageCustodyModifiedRule;
import com.labvantage.sapphire.admin.ddt.rules.StorageUnitValidChildrenRule;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.storage.BaseStorageUnitType;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StorageUnitSDC
extends BaseSDCRules {
    protected final String LABVANTAGE_CVS_ID = "$Revision: 104202 $";
    public static final String SDC_PHYSICALSTORE = "PhysicalStore";
    public static final String SDCID = "StorageUnitSDC";
    public static Boolean childAddMode = false;
    public static Integer lastIndex;
    public static String parentLabel;
    public static String parentid;
    public static Integer parentStorageUnitSize;
    public static final String STORAGEUNITTYPE_CIRCULAR = "Circular";
    public static final String STORAGEUNITTYPE_GRID = "Grid";
    public static final String STORAGEUNITTYPE_LINEAR = "Linear";
    public static final String STORAGEUNITTYPE_NOLAYOUT = "No Layout";
    Map<String, PropertyListCollection> restrictionsCache = new HashMap<String, PropertyListCollection>();
    HashMap<String, String> indexmap = new HashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        tp = this.getTranslationProcessor();
        sql = new StringBuilder();
        syncParentSet = new HashSet<String>();
        inputStorageunitList = OpalUtil.toUniqueList(actionProps.getProperty("keyid1"), ";");
        clearReservations = false;
        linksdcds = new DataSet();
        linksdcds.addColumn("linksdcid", 0);
        linksdcds.addColumn("linkkeyid1", 0);
        linksdcds.addColumn("linkkeyid2", 0);
        linksdcds.addColumn("linkkeyid3", 0);
        ds = new DataSet();
        if (this.connectionInfo.isOracle()) {
            sql.setLength(0);
            sql.append("select level, su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.linkkeyid2, su.linkkeyid3, su.storageenvid, su.ancestorid, su.labelpath");
            sql.append(", (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = su.storageunitid and not exists ( select su1.storageunitid from storageunit su1 where su1.linksdcid = t.linksdcid and su1.linkkeyid1 = t.linkkeyid1 ) ) ticount");
            sql.append(", (select rsu.storageunitid from reservestorageunit rsu where rsu.storageunitid = su.storageunitid) reservestorageunitid");
            sql.append(" from storageunit su");
            sql.append(" connect by prior su.storageunitid = su.parentid");
            sql.append(" start with su.storageunitid in (SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ?)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
        } else if (this.connectionInfo.isSqlServer()) {
            sql.setLength(0);
            sql.append("WITH StorageUnitTree (storageunitid)");
            sql.append(" AS (");
            sql.append("    SELECT su.storageunitid");
            sql.append("    FROM storageunit AS su");
            sql.append("    WHERE su.storageunitid in (SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ?)");
            sql.append("    UNION ALL");
            sql.append("    SELECT su.storageunitid");
            sql.append("   FROM storageunit AS su");
            sql.append("    INNER JOIN StorageUnitTree AS d");
            sql.append("    ON su.parentid = d.storageunitid");
            sql.append(" )");
            sql.append("select '1' level, su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.linkkeyid2, su.linkkeyid3, su.storageenvid, su.ancestorid, su.labelpath");
            sql.append(" , (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = su.storageunitid and not exists ( select su1.storageunitid from storageunit su1 where su1.linksdcid = t.linksdcid and su1.linkkeyid1 = t.linkkeyid1 ) ) ticount");
            sql.append(" , (select rsu.storageunitid from reservestorageunit rsu where rsu.storageunitid = su.storageunitid) reservestorageunitid");
            sql.append(" from storageunit su");
            sql.append(" where su.storageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
        }
        if (OpalUtil.isNotEmpty(ds)) {
            contentcount = 0;
            for (i = 0; i < ds.size(); ++i) {
                contentcount += ds.getInt(i, "ticount", 0);
            }
            if (contentcount > 0) {
                throw new SapphireException(tp.translate("Delete not allowed"), "VALIDATION", contentcount + " " + tp.translate("items found in Storage Unit.") + tp.translate("\nDeleting a Storageunit with items is not allowed. Please move the items first."));
            }
            for (i = 0; i < ds.size(); ++i) {
                storageunitid = ds.getString(i, "storageunitid");
                reservestorageunitid = ds.getString(i, "reservestorageunitid");
                if (StringUtil.getLen(reservestorageunitid) > 0L) {
                    if ("Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
                        clearReservations = true;
                    } else {
                        throw new SapphireException(tp.translate("Clear Reservations"), "CONFIRM", tp.translate("Storageunit have reservations. Continuing will clear all reservations. Would you like to continue?"));
                    }
                }
                linksdcid = ds.getString(i, "linksdcid");
                linkkeyid1 = ds.getString(i, "linkkeyid1");
                linkkeyid2 = ds.getString(i, "linkkeyid2");
                linkkeyid3 = ds.getString(i, "linkkeyid3");
                if (StringUtil.getLen(linksdcid) > 0L && StringUtil.getLen(linkkeyid1) > 0L) {
                    row = linksdcds.addRow();
                    linksdcds.setString(row, "linksdcid", linksdcid);
                    linksdcds.setString(row, "linkkeyid1", linkkeyid1);
                    linksdcds.setString(row, "linkkeyid2", linkkeyid2);
                    linksdcds.setString(row, "linkkeyid3", linkkeyid3);
                }
                if (!inputStorageunitList.contains(storageunitid) || ds.getString(i, "parentid", "").length() <= 0) continue;
                syncParentSet.add(ds.getString(i, "parentid"));
            }
            if (ds.size() > 1000) {
                dsrsetid = null;
                try {
                    dsrsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", ds.getColumnValues("storageunitid", ";"), null, null);
                    if (StringUtil.getLen(dsrsetid) <= 0L) ** GOTO lbl128
                    this.database.executePreparedUpdate("update s_eventlog set newstorageunitid = null where newstorageunitid in ( SELECT rsetitems.keyid1 FROM rsetitems WHERE rsetitems.rsetid = ? )", new Object[]{dsrsetid});
                    this.database.executePreparedUpdate("update s_eventlog set oldstorageunitid = null where oldstorageunitid in ( SELECT rsetitems.keyid1 FROM rsetitems WHERE rsetitems.rsetid = ? )", new Object[]{dsrsetid});
                    if (!clearReservations) ** GOTO lbl128
                    this.database.executePreparedUpdate("DELETE FROM reservestorageunit WHERE storageunitid in ( SELECT rsetitems.keyid1 FROM rsetitems WHERE rsetitems.rsetid = ? )", new Object[]{dsrsetid});
                }
                finally {
                    if (StringUtil.getLen(dsrsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(dsrsetid);
                    }
                }
            } else {
                suid = ds.getColumnValues("storageunitid", "','");
                safeSQL = new SafeSQL();
                placeholder = safeSQL.addIn(suid);
                this.database.executePreparedUpdate("update s_eventlog set newstorageunitid = null where newstorageunitid in ( " + placeholder + " )", safeSQL.getValues());
                this.database.executePreparedUpdate("update s_eventlog set oldstorageunitid = null where oldstorageunitid in ( " + placeholder + " )", safeSQL.getValues());
                if (clearReservations) {
                    this.database.executePreparedUpdate("DELETE FROM reservestorageunit WHERE storageunitid in ( " + placeholder + " )", safeSQL.getValues());
                }
            }
lbl128:
            // 6 sources

            if (syncParentSet.size() > 0) {
                actionProps.setProperty("__syncparentid", OpalUtil.toDelimitedString(syncParentSet, ";"));
                actionProps.setProperty("__syncsizeflag", "Y");
                syncParentSet.clear();
            }
            if (linksdcds.size() > 0) {
                actionProps.put("__linksdcds", linksdcds);
            }
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String parentid;
        PropertyList props;
        DataSet linksdcds = (DataSet)actionProps.get("__linksdcds");
        if (linksdcds != null && linksdcds.size() > 0) {
            StringBuilder sql = new StringBuilder();
            props = new PropertyList();
            linksdcds.sort("linksdcid");
            ArrayList<DataSet> linkSdcGroup = linksdcds.getGroupedDataSets("linksdcid");
            try {
                for (DataSet ds : linkSdcGroup) {
                    String linksdcid;
                    if (ds == null || ds.size() <= 0 || StringUtil.getLen(linksdcid = ds.getString(0, "linksdcid")) <= 0L) continue;
                    HashMap sdcProps = this.getSDCProcessor().getSDCProperties(linksdcid);
                    String tableid = (String)sdcProps.get("tableid");
                    String keycolid1 = (String)sdcProps.get("keycolid1");
                    SafeSQL safeSQL = new SafeSQL();
                    sql.setLength(0);
                    sql.append("select ").append(keycolid1);
                    sql.append(" from ").append(tableid);
                    sql.append(" where ").append(keycolid1).append(" in (").append(safeSQL.addIn(ds.getColumnValues("linkkeyid1", "','"))).append(")");
                    DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (_ds == null || _ds.size() <= 0) continue;
                    props.clear();
                    props.setProperty("sdcid", linksdcid);
                    props.setProperty("keyid1", _ds.getColumnValues(keycolid1, ";"));
                    props.setProperty("__sudeleteflag", "Y");
                    props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
                    if (!this.getActionProcessor().hasInfoErrors()) continue;
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
                this.logger.error("Error", e);
            }
        }
        if ("Y".equals(actionProps.getProperty("__syncsizeflag")) && StringUtil.getLen(parentid = actionProps.getProperty("__syncparentid")) > 0L) {
            this.processSizeChangeRule(parentid, actionProps);
            if (this.updateStorageUnitStats()) {
                props = new PropertyList();
                props.setProperty("storageunitid", parentid);
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
            }
        }
    }

    public static void populateStorageUnitHeirarchy(QueryProcessor queryProcessor, String storageunitid, DataSet child, boolean isOra) {
        block4: {
            StringBuilder sql;
            block3: {
                sql = new StringBuilder();
                if (!isOra) break block3;
                sql.append("select storageunitid, parentid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, 'N' selected, storageenvid, ancestorid");
                sql.append(" from storageunit");
                sql.append(" where storageunitid <> ?");
                sql.append(" connect by prior storageunitid = parentid");
                sql.append(" start with storageunitid = ?");
                DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid, storageunitid});
                if (ds == null) break block4;
                for (int i = 0; i < ds.size(); ++i) {
                    child.copyRow(ds, i, 1);
                }
                break block4;
            }
            sql.append("select s.storageunitid, s.parentid, s.storageunittype, s.linksdcid, s.linkkeyid1, s.linkkeyid2, s.linkkeyid3, 'N' selected, storageenvid, ancestorid");
            sql.append(", (select count(child.storageunitid) from storageunit child where child.parentid = s.storageunitid) childcount");
            sql.append(" from storageunit s");
            sql.append(" where s.parentid = ?");
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    child.copyRow(ds, i, 1);
                    if (ds.getInt(i, "childcount", 0) <= 0) continue;
                    StorageUnitSDC.populateStorageUnitHeirarchy(queryProcessor, ds.getValue(i, "storageunitid"), child, isOra);
                }
            }
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) {
        if ("Y".equals(actionProps.getProperty("__cmtimportflag"))) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        String fromAction = actionProps.getProperty("fromaction");
        if ("createstorageunits".equalsIgnoreCase(fromAction)) {
            this.initializePrimaryDS(primary, actionProps);
            HashMap parentMap = this.getParentMap(primary);
            this.assignParentIds(primary, parentMap);
        }
        for (int i = 0; i < primary.size(); ++i) {
            int maxtiallowed = primary.getInt(i, "maxtiallowed", -1);
            primary.setNumber(i, "maxtiallowed", maxtiallowed);
            primary.setString(i, "trackitemallowedflag", maxtiallowed == 0 ? "N" : "Y");
            primary.setString(i, "spaceavailflag", maxtiallowed == 0 ? "N" : "Y");
            primary.setString(i, "moveableflag", primary.getString(i, "moveableflag", "Y"));
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__cmtimportflag"))) {
            return;
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataAdd");
        }
        DataSet primary = sdiData.getDataset("primary");
        String storageunitid = primary.getColumnValues("storageunitid", ";");
        String fromAction = actionProps.getProperty("fromaction");
        childAddMode = "Y".equalsIgnoreCase(actionProps.getProperty("childAddMode"));
        if ("createstorageunits".equalsIgnoreCase(fromAction)) {
            SafeSQL safeSQL;
            String rsetid;
            List<String> distinctPropTreeids = this.getDistinctColumnValues(primary, "_propertytreeid");
            Map<String, PropertyTree> propTreeMap = this.getPropertyTreeMap(distinctPropTreeids, "StorageUnitType");
            List<String> distinctNodeids = this.getDistinctColumnValues(primary, "_propnodeid");
            Map<String, PropertyList> nodePropTreeMap = this.getNodeTreeMap(distinctNodeids, propTreeMap);
            String createLinkedSUFlag = actionProps.getProperty("_createlinkedsuflag");
            if ("N".equalsIgnoreCase(createLinkedSUFlag)) {
                this.assignLinkedSDIs(primary, actionProps);
            } else {
                this.createAssociatedSDIs(primary, actionProps, distinctNodeids, nodePropTreeMap);
            }
            Map<String, String> useSDIIdAsLabelMap = this.getUseSDIIdAsLabelMap(distinctNodeids, nodePropTreeMap);
            Map<String, String> propTreeClassMap = this.getPropertyTreeClassMap(distinctPropTreeids, "StorageUnitType");
            this.assignSizeAndLabels(primary, propTreeClassMap, nodePropTreeMap, useSDIIdAsLabelMap);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", storageunitid);
            props.setProperty("linksdcid", primary.getColumnValues("linksdcid", ";"));
            props.setProperty("linkkeyid1", primary.getColumnValues("linkkeyid1", ";"));
            props.setProperty("linkkeyid2", primary.getColumnValues("linkkeyid2", ";"));
            props.setProperty("linkkeyid3", primary.getColumnValues("linkkeyid3", ";"));
            props.setProperty("storageunittype", primary.getColumnValues("storageunittype", ";"));
            props.setProperty("storageunitlabel", primary.getColumnValues("storageunitlabel", ";"));
            props.setProperty("storageunitindex", primary.getColumnValues("storageunitindex", ";"));
            props.setProperty("storageunitsize", primary.getColumnValues("storageunitsize", ";"));
            props.setProperty("labelpath", primary.getColumnValues("labelpath", ";"));
            props.setProperty("ancestorid", primary.getColumnValues("ancestorid", ";"));
            props.setProperty("labelrow", primary.getColumnValues("labelrow", ";"));
            props.setProperty("labelcol", primary.getColumnValues("labelcol", ";"));
            props.setProperty("propsmatch", "Y");
            props.setProperty("__sdcruleconfirm", "Y");
            props.setProperty("__createstorageunit", "Y");
            HashSet<String> lastNodeStorageUnitSet = new HashSet<String>();
            List<String> lastNodeList = StorageUnitTypeDef.getInstance().getLastNodeList(this.getConnectionProcessor().getSapphireConnection());
            if (lastNodeList != null && lastNodeList.size() > 0) {
                for (int i = 0; i < primary.size(); ++i) {
                    if (lastNodeList.contains(primary.getString(i, "storageunittype", ""))) {
                        lastNodeStorageUnitSet.add(primary.getString(i, "storageunitid"));
                        primary.setString(i, "lastnodeflag", "Y");
                        continue;
                    }
                    primary.setString(i, "lastnodeflag", "N");
                }
            }
            props.setProperty("lastnodeflag", primary.getColumnValues("lastnodeflag", ";"));
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            if ("New Box Added".equals(actionProps.getProperty("auditreason"))) {
                props.setProperty("spaceavailflag", "Y");
            }
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            if (this.updateStorageUnitStats() && lastNodeStorageUnitSet.size() > 0) {
                String lastNodeStorageUnitIDs = OpalUtil.toDelimitedString(lastNodeStorageUnitSet, ";");
                props.clear();
                props.setProperty("storageunitid", lastNodeStorageUnitIDs);
                props.setProperty("__postAddUpdate", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
                if (!this.isCMTImport()) {
                    DataSet topds;
                    String toplevelfunction;
                    String string = toplevelfunction = this.database.isOracle() ? "LV_SUS.FindTopLevel" : "dbo.LV_SUS_FindTopLevel";
                    if (lastNodeStorageUnitSet.size() > 1000) {
                        rsetid = this.getDAMProcessor().createRSet(SDCID, lastNodeStorageUnitIDs, null, null);
                        topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                        this.getDAMProcessor().clearRSet(rsetid);
                    } else {
                        safeSQL = new SafeSQL();
                        topds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, " + toplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in (" + safeSQL.addIn(lastNodeStorageUnitSet) + ")", safeSQL.getValues());
                    }
                    if (topds != null) {
                        HashSet<String> topLevelSet = new HashSet<String>();
                        for (int i = 0; i < topds.size(); ++i) {
                            topLevelSet.add(topds.getString(i, "toplevelid"));
                        }
                        if (topLevelSet.size() > 0) {
                            props.clear();
                            props.setProperty("storageunitid", OpalUtil.toDelimitedString(topLevelSet, ";"));
                            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                            this.getActionProcessor().processActionClass(PopulateStorageUnitStats.class.getName(), props);
                        }
                    }
                }
            }
            if (childAddMode.booleanValue()) {
                PropertyList parentprops = new PropertyList();
                parentprops.setProperty("sdcid", SDCID);
                parentprops.setProperty("keyid1", parentid);
                parentprops.setProperty("storageunitsize", parentStorageUnitSize.toString());
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), parentprops);
                String departmentid = this.getPhysicalStoreDepartment(parentid);
                if (StringUtil.getLen(departmentid) > 0L) {
                    DataSet ds;
                    String sql;
                    if (primary.size() > 1000) {
                        rsetid = this.getDAMProcessor().createRSet(SDCID, storageunitid, null, null);
                        sql = "select storageunitid, storageunittype, (select t.trackitemid from trackitem t where t.linksdcid = storageunit.linksdcid and t.linkkeyid1 = storageunit.linkkeyid1) trackitemid from storageunit where parentid = ? and storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)";
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{parentid, rsetid});
                        this.getDAMProcessor().clearRSet(rsetid);
                    } else {
                        safeSQL = new SafeSQL();
                        sql = "select storageunitid, storageunittype, (select t.trackitemid from trackitem t where t.linksdcid = storageunit.linksdcid and t.linkkeyid1 = storageunit.linkkeyid1) trackitemid from storageunit where parentid = " + safeSQL.addVar(parentid) + " and storageunitid in (" + safeSQL.addIn(storageunitid, ";") + ")";
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    }
                    if (OpalUtil.isNotEmpty(ds)) {
                        StorageUnitTypeDef storageUnitTypeDef = StorageUnitTypeDef.getInstance();
                        Map<String, PropertyList> typeMap = storageUnitTypeDef.getTypeMap(this.getQueryProcessor());
                        HashSet<String> trackitemSet = new HashSet<String>();
                        for (int i = 0; i < ds.size(); ++i) {
                            String storageunittype = ds.getString(i, "storageunittype", "");
                            if (storageUnitTypeDef.isStorageContainer(storageunittype, typeMap)) {
                                String trackitemid = ds.getString(i, "trackitemid", "");
                                if (trackitemid.length() <= 0) continue;
                                trackitemSet.add(trackitemid);
                                continue;
                            }
                            String trackitemids = this.getAllTrackitemsInStorageWithNoDepartment(ds.getString(i, "storageunitid"));
                            if (StringUtil.getLen(trackitemids) <= 0L) continue;
                            trackitemSet.addAll(OpalUtil.toList(trackitemids, ";"));
                        }
                        if (trackitemSet.size() > 0) {
                            props.clear();
                            props.setProperty("sdcid", "TrackItemSDC");
                            props.setProperty("keyid1", OpalUtil.toDelimitedString(trackitemSet, ";"));
                            props.setProperty("custodialdepartmentid", departmentid);
                            props.setProperty("__sdcruleignore", "Y");
                            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        }
                    }
                }
            }
            HashMap<String, String> boxKeyIDMap = new HashMap<String, String>();
            HashMap<String, String> boxStorageUnitIDMap = new HashMap<String, String>();
            HashSet<String> boxTemplateSet = new HashSet<String>();
            DataSet updateBoxTypeDS = new DataSet();
            DataSet updateMaxTIAllowedDS = new DataSet();
            DataSet updateArrayLayoutDS = new DataSet();
            DataSet dsrestrictions = new DataSet();
            for (int i = 0; i < primary.size(); ++i) {
                String _storageunitid;
                String _storageunittype = primary.getString(i, "storageunittype");
                PropertyListCollection restrictions = this.getStorageRestrictions(_storageunittype, _storageunitid = primary.getString(i, "storageunitid"));
                if (restrictions != null && restrictions.size() > 0) {
                    for (int j = 0; j < restrictions.size(); ++j) {
                        PropertyList restriction = restrictions.getPropertyList(j);
                        String basedon = restriction.getProperty("restrictionbasedon");
                        if (StringUtil.getLen(basedon) <= 0L) continue;
                        String propertyid = "";
                        if ("Current User".equals(basedon)) {
                            propertyid = restriction.getProperty("propertyid_user");
                        } else if ("Sample".equals(basedon)) {
                            propertyid = restriction.getProperty("propertyid_sample");
                        } else if ("Sample Family".equals(basedon)) {
                            propertyid = restriction.getProperty("propertyid_samplefamily");
                        } else if ("TrackItem".equals(basedon)) {
                            propertyid = restriction.getProperty("propertyid_trackitem");
                        }
                        if (StringUtil.getLen(propertyid) <= 0L) continue;
                        int row = dsrestrictions.addRow();
                        dsrestrictions.setString(row, "storageunitid", primary.getString(i, "storageunitid"));
                        dsrestrictions.setString(row, "restrictionbasedon", basedon);
                        dsrestrictions.setString(row, "propertyid", propertyid);
                        dsrestrictions.setString(row, "operator", restriction.getProperty("operator"));
                        dsrestrictions.setString(row, "propertyvalue", restriction.getProperty("propertyvalue"));
                        dsrestrictions.setString(row, "failuremessage", restriction.getProperty("failuremessage"));
                        dsrestrictions.setString(row, "activeflag", restriction.getProperty("activeflag"));
                    }
                }
                if (!"LV_Box".equals(primary.getString(i, "linksdcid"))) continue;
                String boxtemplateid = actionProps.getProperty("boxtemplateid", _storageunittype);
                boxStorageUnitIDMap.put(_storageunitid, boxtemplateid);
                boxKeyIDMap.put(_storageunitid, primary.getString(i, "linkkeyid1"));
                boxTemplateSet.add(boxtemplateid);
            }
            if (boxStorageUnitIDMap.size() > 0) {
                SafeSQL safeSQL2 = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunit.linkkeyid1, storageunit.storageunittype, storageunit.propertytreeid, storageunit.maxtiallowed, storageunit.arraylayoutid, storageunit.arraylayoutversionid, storagerestriction.restrictionbasedon, storagerestriction.propertyid, storagerestriction.propertyvalue, storagerestriction.operator, storagerestriction.failuremessage, storagerestriction.activeflag from storageunit left outer join storagerestriction on storagerestriction.storageunitid = storageunit.storageunitid where storageunit.linksdcid = 'LV_Box' and storageunit.linkkeyid1 in (" + safeSQL2.addIn(boxTemplateSet) + ") order by storagerestriction.usersequence", safeSQL2.getValues());
                if (ds.size() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (String boxstorageunitid : boxStorageUnitIDMap.keySet()) {
                        String boxtemplateid = (String)boxStorageUnitIDMap.get(boxstorageunitid);
                        filter.clear();
                        filter.put("linkkeyid1", boxtemplateid);
                        DataSet restrictionsDS = ds.getFilteredDataSet(filter);
                        for (int i = 0; i < restrictionsDS.size(); ++i) {
                            int row;
                            int maxtiallowed;
                            String restrictionbasedon = restrictionsDS.getString(i, "restrictionbasedon", "");
                            if (restrictionbasedon.length() > 0) {
                                int row2 = dsrestrictions.addRow();
                                dsrestrictions.setString(row2, "storageunitid", boxstorageunitid);
                                dsrestrictions.setString(row2, "restrictionbasedon", restrictionbasedon);
                                dsrestrictions.setString(row2, "propertyid", restrictionsDS.getString(i, "propertyid", ""));
                                dsrestrictions.setString(row2, "operator", restrictionsDS.getString(i, "operator", ""));
                                dsrestrictions.setString(row2, "propertyvalue", restrictionsDS.getString(i, "propertyvalue", ""));
                                dsrestrictions.setString(row2, "failuremessage", restrictionsDS.getString(i, "failuremessage", ""));
                                dsrestrictions.setString(row2, "activeflag", restrictionsDS.getString(i, "activeflag", "Y"));
                            }
                            if (i != 0) continue;
                            String arraylayoutid = restrictionsDS.getString(i, "arraylayoutid", "");
                            String arraylayoutversionid = restrictionsDS.getString(i, "arraylayoutversionid", "");
                            if (arraylayoutid.length() > 0 && arraylayoutversionid.length() > 0) {
                                int row3 = updateArrayLayoutDS.addRow();
                                updateArrayLayoutDS.setString(row3, "storageunitid", boxstorageunitid);
                                updateArrayLayoutDS.setString(row3, "arraylayoutid", arraylayoutid);
                                updateArrayLayoutDS.setString(row3, "arraylayoutversionid", arraylayoutversionid);
                            }
                            if ((maxtiallowed = restrictionsDS.getInt(i, "maxtiallowed", 0)) > 0) {
                                row = updateMaxTIAllowedDS.addRow();
                                updateMaxTIAllowedDS.setString(row, "storageunitid", boxstorageunitid);
                                updateMaxTIAllowedDS.setString(row, "maxtiallowed", String.valueOf(maxtiallowed));
                            }
                            row = updateBoxTypeDS.addRow();
                            updateBoxTypeDS.setString(row, "boxid", (String)boxKeyIDMap.get(boxstorageunitid));
                            updateBoxTypeDS.setString(row, "boxtype", STORAGEUNITTYPE_GRID.equals(restrictionsDS.getString(i, "propertytreeid")) ? "Sorted" : "Unsorted");
                        }
                    }
                }
            }
            if (dsrestrictions.size() > 0) {
                String sequencekey = "SUR-" + new SimpleDateFormat("yyyy").format(new Date()) + "-";
                int sequence = this.getSequenceProcessor().getSequence(SDCID, "storagerestriction.businessrule", dsrestrictions.size());
                Calendar now = DateTimeUtil.getNowCalendar();
                for (int i = 0; i < dsrestrictions.size(); ++i) {
                    dsrestrictions.setString(i, "storagerestrictionid", sequencekey + sequence++);
                }
                dsrestrictions.setDate(-1, "createdt", now);
                dsrestrictions.setDate(-1, "createby", this.connectionInfo.getSysuserId());
                dsrestrictions.setDate(-1, "createtool", "CreateStorageUnits");
                dsrestrictions.setDate(-1, "moddt", now);
                dsrestrictions.setDate(-1, "modby", this.connectionInfo.getSysuserId());
                dsrestrictions.setDate(-1, "modtool", "CreateStorageUnits");
                DataSetUtil.insert(this.database, dsrestrictions, "storagerestriction");
            }
            if (updateArrayLayoutDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", updateArrayLayoutDS.getColumnValues("storageunitid", ";"));
                props.setProperty("arraylayoutid", updateArrayLayoutDS.getColumnValues("arraylayoutid", ";"));
                props.setProperty("arraylayoutversionid", updateArrayLayoutDS.getColumnValues("arraylayoutversionid", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (updateMaxTIAllowedDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", updateMaxTIAllowedDS.getColumnValues("storageunitid", ";"));
                props.setProperty("maxtiallowed", updateMaxTIAllowedDS.getColumnValues("maxtiallowed", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("__createstorageunit", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            if (updateBoxTypeDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "LV_Box");
                props.setProperty("keyid1", updateBoxTypeDS.getColumnValues("boxid", ";"));
                props.setProperty("boxtype", updateBoxTypeDS.getColumnValues("boxtype", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("__sdcruleignore", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (!"New Box Added".equals(actionProps.getProperty("auditreason"))) {
            ArrayList<String> list = new ArrayList<String>();
            int size = primary.size();
            for (int i = 0; i < size; ++i) {
                if (!"Y".equals(primary.getValue(i, "spaceavailflag"))) continue;
                list.add(primary.getValue(i, "storageunitid"));
            }
            if (list.size() > 0) {
                this.syncSpaceAvailFlag(list, actionProps);
            }
        }
    }

    private String getAllTrackitemsInStorageWithNoDepartment(String storageunitid) {
        StringBuilder sql = new StringBuilder();
        if (this.connectionInfo.isOracle()) {
            sql.append("SELECT T.TRACKITEMID, T.LINKKEYID1, T.CUSTODIALDEPARTMENTID FROM TRACKITEM T");
            sql.append(" WHERE T.CURRENTSTORAGEUNITID IN (SELECT SU.STORAGEUNITID ");
            sql.append(" FROM STORAGEUNIT SU");
            sql.append(" CONNECT BY PRIOR SU.STORAGEUNITID = SU.PARENTID");
            sql.append(" START WITH SU.STORAGEUNITID = ?)");
            sql.append(" AND T.CUSTODIALDEPARTMENTID IS NULL");
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
            sql.append(" SELECT T.TRACKITEMID FROM TRACKITEM T");
            sql.append(" WHERE T.CURRENTSTORAGEUNITID IN (SELECT st.storageunitid FROM StorageUnitTree st)");
            sql.append(" AND T.CUSTODIALDEPARTMENTID IS NULL");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            return ds.getColumnValues("trackitemid", ";");
        }
        return "";
    }

    private String getPhysicalStoreDepartment(String storageunitid) {
        StringBuilder sql = new StringBuilder();
        String departmentid = "";
        if (this.connectionInfo.isOracle()) {
            sql.append("SELECT SU.STORAGEUNITID, SU.LINKSDCID, (select ps.departmentid from s_physicalstore ps where ps.s_physicalstoreid = SU.linkkeyid1 and SU.linksdcid = 'PhysicalStore') departmentid");
            sql.append(" FROM STORAGEUNIT SU");
            sql.append(" CONNECT BY PRIOR SU.PARENTID = SU.STORAGEUNITID");
            sql.append(" START WITH SU.STORAGEUNITID = ?");
        } else {
            sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1, storageunittype)");
            sql.append(" AS (");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.storageunittype");
            sql.append("    FROM storageunit AS su");
            sql.append("    WHERE su.storageunitid = ?");
            sql.append("    UNION ALL");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.storageunittype");
            sql.append("   FROM storageunit AS su");
            sql.append("    INNER JOIN StorageUnitTree AS d");
            sql.append("    ON su.storageunitid = d.parentid");
            sql.append(" )");
            sql.append(" SELECT st.storageunitid, st.linksdcid, (select ps.departmentid from s_physicalstore ps where ps.s_physicalstoreid = st.linkkeyid1 and st.linksdcid = 'PhysicalStore') departmentid");
            sql.append(" FROM StorageUnitTree st");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getString(i, "departmentid", "").length() <= 0) continue;
                departmentid = ds.getString(i, "departmentid");
                break;
            }
        }
        return departmentid;
    }

    private PropertyListCollection getStorageRestrictions(String storageunittype, String storageunitid) {
        if (!this.restrictionsCache.containsKey(storageunittype)) {
            this.restrictionsCache.put(storageunittype, StorageUnitSDC.getSUValueTree(this.getQueryProcessor(), storageunitid).getCollectionNotNull("restrictions"));
        }
        return this.restrictionsCache.get(storageunittype);
    }

    private int getNextChildIndex(String parentid) {
        int index = 1;
        if (this.indexmap.containsKey(parentid)) {
            index = Integer.parseInt(this.indexmap.get(parentid));
        }
        this.indexmap.put(parentid, String.valueOf(index + 1));
        return index;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__createstorageunit"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__syncoperation"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            return;
        }
        if (actionProps.getProperty("importInstructions").length() > 0) {
            return;
        }
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataEdit");
        }
        DataSet primary = sdiData.getDataset("primary");
        HashMap<String, String> parentMap = new HashMap<String, String>();
        ConnectByLoopRule rule = new ConnectByLoopRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (this.hasPrimaryValueChanged(primary, i, "maxtiallowed")) {
                if (primary.getValue(i, "maxtiallowed", "").length() == 0) {
                    primary.setNumber(i, "maxtiallowed", 0);
                }
                if (primary.getInt(i, "maxtiallowed") < -1) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Invalid MaxTIAllowed Value"), "VALIDATION", this.getTranslationProcessor().translate("Valid value for maxtiallowed is -1 (for unlimited), 0 (for none), or n (n number of trackitems)"));
                }
                primary.setString(i, "trackitemallowedflag", "0".equals(primary.getValue(i, "maxtiallowed")) ? "N" : "Y");
                primary.setString(i, "spaceavailflag", "0".equals(primary.getValue(i, "maxtiallowed")) ? "N" : "Y");
            }
            if (this.hasPrimaryValueChanged(primary, i, "parentid")) {
                String storageunitid = primary.getString(i, "storageunitid");
                String parentid = primary.getString(i, "parentid");
                if (StringUtil.getLen(parentid) > 0L) {
                    this.processStorageUnitMoveRule(primary, i);
                    rule.processRule("storageunit", storageunitid, parentid, "storageunitid", "parentid");
                    parentMap.put(storageunitid, parentid);
                }
            }
            if (!STORAGEUNITTYPE_GRID.equals(this.getOldPrimaryValue(primary, i, "propertytreeid")) || !this.hasPrimaryValueChanged(primary, i, "arraylayoutid") && !this.hasPrimaryValueChanged(primary, i, "arraylayoutversionid")) continue;
            String arraylayoutid = primary.getString(i, "arraylayoutid", "");
            String arraylayoutversionid = primary.getString(i, "arraylayoutversionid", "");
            if (arraylayoutid.length() <= 0) continue;
            if (arraylayoutversionid.length() > 0) {
                String arraytypeid;
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select arraylayoutid, arraylayoutversionid, versionstatus, arraytypeid from arraylayout where arraylayoutid=? and arraylayoutversionid=?", (Object[])new String[]{arraylayoutid, arraylayoutversionid});
                if (ds == null || ds.size() <= 0 || (arraytypeid = ds.getString(0, "arraytypeid", "")).equals("ASL " + this.getOldPrimaryValue(primary, i, "storageunittype"))) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("Invalid Array Layout"), "VALIDATION", this.getTranslationProcessor().translate("Array Layout must have Array Type") + "<br><b>" + arraytypeid + "</b>");
            }
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Array Layout"), "VALIDATION", this.getTranslationProcessor().translate("Missing Array Layout Version"));
        }
        if (parentMap.size() > 0) {
            new StorageUnitValidChildrenRule(this.database, this.connectionInfo).processRule(parentMap);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        PropertyList props;
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataEdit");
        }
        DataSet primary = sdiData.getDataset("primary");
        if ("Y".equals(actionProps.getProperty("__createstorageunit"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__syncoperation"))) {
            return;
        }
        if (actionProps.getProperty("importInstructions").length() > 0) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            if (!"Y".equals(actionProps.getProperty("__susecuritydepartmentedit"))) {
                boolean isOra = this.getConnectionProcessor().getSapphireConnection().isOracle();
                for (int i = 0; i < primary.size(); ++i) {
                    DataSet ds;
                    String storageunitid = primary.getString(i, "storageunitid");
                    String securitydepartment = primary.getString(i, "securitydepartment");
                    StringBuilder sql = new StringBuilder();
                    if (isOra) {
                        sql.append("SELECT STORAGEUNITID, ? dept");
                        sql.append(" FROM STORAGEUNIT");
                        sql.append(" where ( securitydepartment != ? or securitydepartment is null )");
                        sql.append(" CONNECT BY PRIOR STORAGEUNITID = PARENTID");
                        sql.append(" START WITH STORAGEUNITID = ?");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{securitydepartment, securitydepartment, storageunitid});
                    } else {
                        sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1, securitydepartment)");
                        sql.append(" AS (");
                        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.securitydepartment");
                        sql.append("    FROM storageunit AS su");
                        sql.append("    WHERE su.storageunitid = ?");
                        sql.append("    UNION ALL");
                        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.securitydepartment");
                        sql.append("   FROM storageunit AS su");
                        sql.append("    INNER JOIN StorageUnitTree AS d");
                        sql.append("    ON su.parentid = d.storageunitid");
                        sql.append(" )");
                        sql.append(" SELECT storageunitid, ? dept");
                        sql.append(" FROM StorageUnitTree");
                        sql.append(" where ( securitydepartment != ? or securitydepartment is null)");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid, securitydepartment, securitydepartment});
                    }
                    if (ds == null || ds.size() <= 0) continue;
                    PropertyList props2 = new PropertyList();
                    props2.setProperty("sdcid", SDCID);
                    props2.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                    props2.setProperty("securitydepartment", ds.getColumnValues("dept", ";"));
                    props2.setProperty("__securitydepartmentedit", "Y");
                    props2.setProperty("__susecuritydepartmentedit", "Y");
                    props2.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                    props2.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                    props2.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                    props2.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props2);
                }
            }
            return;
        }
        DataSet syncds = new DataSet();
        syncds.addColumn("storageunitid", 0);
        TreeSet<String> parents = new TreeSet<String>();
        HashSet<String> storageunitset = new HashSet<String>();
        int primarySize = primary.size();
        ArrayList<String> labelList = new ArrayList<String>();
        HashSet<String> parentModifiedSet = new HashSet<String>();
        DataSet parentModifiedSetDS = new DataSet();
        ArrayList<String> specimenCapacityModifiedList = new ArrayList<String>();
        ArrayList<String> maxTIAllowedModifiedList = new ArrayList<String>();
        for (int i = 0; i < primarySize; ++i) {
            String parentid;
            String storageunitid = primary.getString(i, "storageunitid");
            boolean parentmodified = this.hasPrimaryValueChanged(primary, i, "parentid");
            if (this.hasPrimaryValueChanged(primary, i, "storageenvid") || parentmodified) {
                int row = syncds.addRow();
                syncds.setValue(row, "storageunitid", primary.getValue(i, "storageunitid"));
            }
            if (this.hasPrimaryValueChanged(primary, i, "enteredspecimencapacity")) {
                specimenCapacityModifiedList.add(storageunitid);
            }
            if (this.hasPrimaryValueChanged(primary, i, "maxtiallowed") && OpalUtil.isNotEmpty(parentid = this.getOldPrimaryValue(primary, i, "parentid"))) {
                maxTIAllowedModifiedList.add(parentid);
                specimenCapacityModifiedList.add(storageunitid);
            }
            if (parentmodified) {
                String oldParentId = this.getOldPrimaryValue(primary, i, "parentid");
                if (oldParentId.trim().length() > 0) {
                    parents.add(oldParentId);
                }
                parents.add(primary.getValue(i, "parentid"));
                parentModifiedSet.add(storageunitid);
                int row = parentModifiedSetDS.addRow();
                parentModifiedSetDS.setString(row, "storageunitid", storageunitid);
                parentModifiedSetDS.setString(row, "tracelogid", StringUtil.split(actionProps.getProperty("tracelogid"), ";")[i]);
            }
            if (this.hasPrimaryValueChanged(primary, i, "storageunitlabel")) {
                labelList.add(primary.getValue(i, "storageunitid"));
            }
            if (this.hasPrimaryValueChanged(primary, i, "maxtiallowed") && "LV_Box".equals(this.getOldPrimaryValue(primary, i, "linksdcid"))) {
                storageunitset.add(primary.getValue(i, "storageunitid"));
            }
            if (!STORAGEUNITTYPE_GRID.equals(this.getOldPrimaryValue(primary, i, "propertytreeid")) || !this.hasPrimaryValueChanged(primary, i, "arraylayoutid") && !this.hasPrimaryValueChanged(primary, i, "arraylayoutversionid")) continue;
            String arraylayoutid = primary.getString(i, "arraylayoutid", "");
            String arraylayoutversionid = primary.getString(i, "arraylayoutversionid", "");
            DataSet childDS = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, maxtiallowed from storageunit where parentid = ? order by storageunitindex", (Object[])new String[]{storageunitid});
            if (arraylayoutid.length() > 0 && arraylayoutversionid.length() > 0) {
                DataSet arrayLayoutDS = this.getQueryProcessor().getPreparedSqlDataSet("select " + (this.getConnectionInfo().isOracle() ? "to_char(arraylayoutzoneitem.xpos)" : "convert(varchar, arraylayoutzoneitem.xpos)") + " xpos, " + (this.getConnectionInfo().isOracle() ? "to_char(arraylayoutzoneitem.ypos)" : "convert(varchar, arraylayoutzoneitem.ypos)") + " ypos, arraylayoutzoneitem.arraylayoutid, arraylayoutzoneitem.arraylayoutversionid, arraylayoutzoneitem.arraylayoutzone, arraylayoutzone.color from arraylayoutzoneitem, arraylayoutzone where arraylayoutzoneitem.arraylayoutid = ? and arraylayoutzoneitem.arraylayoutversionid = ? and arraylayoutzoneitem.arraylayoutzone != '(FullArray)' and arraylayoutzone.arraylayoutid = arraylayoutzoneitem.arraylayoutid and arraylayoutzone.arraylayoutversionid = arraylayoutzoneitem.arraylayoutversionid and arraylayoutzone.arraylayoutzone = arraylayoutzoneitem.arraylayoutzone order by arraylayoutzoneitem.usersequence", (Object[])new String[]{arraylayoutid, arraylayoutversionid});
                if (arrayLayoutDS != null && arrayLayoutDS.size() > 0) {
                    int rows = Integer.parseInt(this.getOldPrimaryValue(primary, i, "numrows"));
                    int cols = Integer.parseInt(this.getOldPrimaryValue(primary, i, "numcol"));
                    if (childDS != null) {
                        int _row;
                        int col;
                        int row;
                        HashMap<String, String> filter = new HashMap<String, String>();
                        int storageunitindex = 0;
                        PropertyList sutypeProps = StorageUnitTypeDef.getInstance().getTypeDefinitionByID(this.getQueryProcessor(), storageunitid);
                        String indexOrderHorizontal = sutypeProps.getPropertyListNotNull("indexorder").getProperty("horizontal", "Left->Right");
                        String indexOrderVertical = sutypeProps.getPropertyListNotNull("indexorder").getProperty("vertical", "Top->Bottom");
                        if (indexOrderHorizontal.equals("Left->Right")) {
                            if (indexOrderVertical.equals("Top->Bottom")) {
                                for (row = 0; row < rows; ++row) {
                                    for (col = 0; col < cols; ++col) {
                                        filter.put("xpos", String.valueOf(row));
                                        filter.put("ypos", String.valueOf(col));
                                        _row = arrayLayoutDS.findRow(filter);
                                        if (_row != -1) {
                                            childDS.setString(storageunitindex, "arraylayoutzone", arrayLayoutDS.getString(_row, "arraylayoutzone", ""));
                                            childDS.setString(storageunitindex, "arraylayoutzonecolor", arrayLayoutDS.getString(_row, "color", ""));
                                            childDS.setNumber(storageunitindex, "maxtiallowed", 1);
                                            childDS.setString(storageunitindex, "spaceavailflag", "Y");
                                        } else {
                                            childDS.setString(storageunitindex, "arraylayoutzone", "(null)");
                                            childDS.setString(storageunitindex, "arraylayoutzonecolor", "(null)");
                                            childDS.setNumber(storageunitindex, "maxtiallowed", 0);
                                            childDS.setString(storageunitindex, "spaceavailflag", "N");
                                        }
                                        ++storageunitindex;
                                    }
                                }
                            } else {
                                for (row = rows - 1; row >= 0; --row) {
                                    for (col = 0; col < cols; ++col) {
                                        filter.put("xpos", String.valueOf(row));
                                        filter.put("ypos", String.valueOf(col));
                                        _row = arrayLayoutDS.findRow(filter);
                                        if (_row != -1) {
                                            childDS.setString(storageunitindex, "arraylayoutzone", arrayLayoutDS.getString(_row, "arraylayoutzone", ""));
                                            childDS.setString(storageunitindex, "arraylayoutzonecolor", arrayLayoutDS.getString(_row, "color", ""));
                                            childDS.setNumber(storageunitindex, "maxtiallowed", 1);
                                            childDS.setString(storageunitindex, "spaceavailflag", "Y");
                                        } else {
                                            childDS.setString(storageunitindex, "arraylayoutzone", "(null)");
                                            childDS.setString(storageunitindex, "arraylayoutzonecolor", "(null)");
                                            childDS.setNumber(storageunitindex, "maxtiallowed", 0);
                                            childDS.setString(storageunitindex, "spaceavailflag", "N");
                                        }
                                        ++storageunitindex;
                                    }
                                }
                            }
                        } else if (indexOrderVertical.equals("Top->Bottom")) {
                            for (row = 0; row < rows; ++row) {
                                for (col = cols - 1; col >= 0; --col) {
                                    filter.put("xpos", String.valueOf(row));
                                    filter.put("ypos", String.valueOf(col));
                                    _row = arrayLayoutDS.findRow(filter);
                                    if (_row != -1) {
                                        childDS.setString(storageunitindex, "arraylayoutzone", arrayLayoutDS.getString(_row, "arraylayoutzone", ""));
                                        childDS.setString(storageunitindex, "arraylayoutzonecolor", arrayLayoutDS.getString(_row, "color", ""));
                                        childDS.setNumber(storageunitindex, "maxtiallowed", 1);
                                        childDS.setString(storageunitindex, "spaceavailflag", "Y");
                                    } else {
                                        childDS.setString(storageunitindex, "arraylayoutzone", "(null)");
                                        childDS.setString(storageunitindex, "arraylayoutzonecolor", "(null)");
                                        childDS.setNumber(storageunitindex, "maxtiallowed", 0);
                                        childDS.setString(storageunitindex, "spaceavailflag", "N");
                                    }
                                    ++storageunitindex;
                                }
                            }
                        } else {
                            for (row = rows - 1; row >= 0; --row) {
                                for (col = cols - 1; col >= 0; --col) {
                                    filter.put("xpos", String.valueOf(row));
                                    filter.put("ypos", String.valueOf(col));
                                    _row = arrayLayoutDS.findRow(filter);
                                    if (_row != -1) {
                                        childDS.setString(storageunitindex, "arraylayoutzone", arrayLayoutDS.getString(_row, "arraylayoutzone", ""));
                                        childDS.setString(storageunitindex, "arraylayoutzonecolor", arrayLayoutDS.getString(_row, "color", ""));
                                        childDS.setNumber(storageunitindex, "maxtiallowed", 1);
                                        childDS.setString(storageunitindex, "spaceavailflag", "Y");
                                    } else {
                                        childDS.setString(storageunitindex, "arraylayoutzone", "(null)");
                                        childDS.setString(storageunitindex, "arraylayoutzonecolor", "(null)");
                                        childDS.setNumber(storageunitindex, "maxtiallowed", 0);
                                        childDS.setString(storageunitindex, "spaceavailflag", "N");
                                    }
                                    ++storageunitindex;
                                }
                            }
                        }
                    }
                }
            } else {
                childDS.setString(-1, "arraylayoutid", "(null)");
                childDS.setString(-1, "arraylayoutversionid", "(null)");
                childDS.setNumber(-1, "maxtiallowed", 1);
                childDS.setString(-1, "arraylayoutzone", "(null)");
                childDS.setString(-1, "arraylayoutzonecolor", "(null)");
                childDS.setString(-1, "spaceavailflag", "Y");
            }
            if (childDS == null || childDS.size() <= 0) continue;
            PropertyList props3 = new PropertyList();
            props3.setProperty("sdcid", SDCID);
            props3.setProperty("keyid1", childDS.getColumnValues("storageunitid", ";"));
            props3.setProperty("maxtiallowed", childDS.getColumnValues("maxtiallowed", ";"));
            props3.setProperty("arraylayoutzone", childDS.getColumnValues("arraylayoutzone", ";"));
            props3.setProperty("arraylayoutzonecolor", childDS.getColumnValues("arraylayoutzonecolor", ";"));
            props3.setProperty("spaceavailflag", childDS.getColumnValues("spaceavailflag", ";"));
            props3.setProperty("__syncoperation", "Y");
            props3.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props3.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props3.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props3.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props3);
        }
        boolean updateStorageUnitStats = this.updateStorageUnitStats();
        if (labelList.size() > 0) {
            props = new PropertyList();
            props.setProperty("storageunitid", OpalUtil.toDelimitedString(labelList, ";"));
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(SyncStorageLabelPath.class.getName(), props);
        }
        if (syncds.size() > 0) {
            this.processAncestorChangeRule(syncds, actionProps);
        }
        if (parents.size() > 0) {
            this.processSizeChangeRule(OpalUtil.toDelimitedString(parents, ";"), actionProps);
            if (updateStorageUnitStats) {
                props = new PropertyList();
                props.setProperty("storageunitid", OpalUtil.toDelimitedString(parents, ";"));
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
            }
        }
        if (storageunitset.size() > 0) {
            this.updateSpaceAvailableAndStatus(storageunitset, actionProps);
        }
        if (parentModifiedSet.size() > 0) {
            String tracelogid = primary.size() > 0 ? primary.getString(0, "tracelogid", "") : "";
            new StorageCustodyModifiedRule(this.database, this.connectionInfo, tracelogid, actionProps.getProperty("auditreason", ""), actionProps.getProperty("auditactivity", ""), actionProps.getProperty("auditsignedflag", "")).processRule(parentModifiedSetDS);
            SafeSQL safeSQL = new SafeSQL();
            DataSet storageUnitDataSet = this.getQueryProcessor().getPreparedSqlDataSet("select su.storageunitid, su.parentid, su.labelpath, '' oldparentid, '' oldlabelpath, '' tracelogid from storageunit su left outer join trackitem ti on ti.linksdcid = su.linksdcid and ti.linkkeyid1 = su.linkkeyid1 where su.storageunitid in (" + safeSQL.addIn(parentModifiedSet) + ") and ti.trackitemid is null", safeSQL.getValues());
            if (storageUnitDataSet != null && storageUnitDataSet.size() > 0) {
                for (int i = 0; i < storageUnitDataSet.size(); ++i) {
                    String storageunitid = storageUnitDataSet.getString(i, "storageunitid");
                    int row = primary.findRow("storageunitid", storageunitid);
                    if (row == -1) continue;
                    storageUnitDataSet.setString(i, "oldparentid", this.getOldPrimaryValue(primary, row, "parentid"));
                    storageUnitDataSet.setString(i, "oldlabelpath", this.getOldPrimaryValue(primary, row, "labelpath"));
                    storageUnitDataSet.setString(i, "tracelogid", primary.getString(row, "tracelogid", ""));
                }
                this.processEventLogRule(storageUnitDataSet);
            }
        }
        if (updateStorageUnitStats && maxTIAllowedModifiedList.size() > 0) {
            props = new PropertyList();
            props.setProperty("storageunitid", OpalUtil.toDelimitedString(maxTIAllowedModifiedList, ";"));
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(SyncLastNodeCapacity.class.getName(), props);
        }
        if (updateStorageUnitStats && specimenCapacityModifiedList.size() > 0) {
            String findtoplevelfunction = this.database.isOracle() ? "LV_SUS.FindTopLevel" : "dbo.LV_SUS_FindTopLevel";
            DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDCID, "select storageunitid, " + findtoplevelfunction + "(storageunitid) toplevelid from storageunit where storageunitid in ([])", specimenCapacityModifiedList);
            if (ds != null && ds.size() > 0) {
                String topLevelStorageUnits = OpalUtil.getUniqueValues(ds.getColumnValues("toplevelid", ";"), ";");
                PropertyList props4 = new PropertyList();
                props4.setProperty("storageunitid", topLevelStorageUnits);
                props4.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props4.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props4.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props4.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(PopulateStorageUnitStats.class.getName(), props4);
            }
        }
    }

    private void processEventLogRule(DataSet storageUnitDataSet) throws SapphireException {
        if (storageUnitDataSet != null && storageUnitDataSet.size() > 0) {
            EventLog eventLogParent = new EventLog(this.database, this.getSequenceProcessor());
            eventLogParent.setCurrentUser(this.getConnectionInfo().getSysuserId());
            for (int i = 0; i < storageUnitDataSet.size(); ++i) {
                String storageunitid = storageUnitDataSet.getString(i, "storageunitid");
                String currentstoragelabelpath = storageUnitDataSet.getString(i, "labelpath");
                String oldstorageunitid = storageUnitDataSet.getString(i, "storageunitid");
                String oldstoragelabelpath = storageUnitDataSet.getString(i, "oldlabelpath");
                String tracelogid = storageUnitDataSet.getString(i, "tracelogid", "");
                DataSet ds = StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), storageunitid, this.connectionInfo.isOracle());
                if (!OpalUtil.isNotEmpty(ds)) continue;
                for (int j = 0; j < ds.size(); ++j) {
                    int row = eventLogParent.addRow();
                    String currentlabelpath = ds.getString(j, "labelpath", "");
                    String oldlabelpath = StringUtil.replaceAll(currentlabelpath, currentstoragelabelpath, oldstoragelabelpath);
                    if (OpalUtil.isNotEmpty(currentlabelpath) && currentlabelpath.length() > 80) {
                        currentlabelpath = currentlabelpath.substring(0, 76) + "...";
                    }
                    if (OpalUtil.isNotEmpty(oldlabelpath) && oldlabelpath.length() > 80) {
                        oldlabelpath = oldlabelpath.substring(0, 76) + "...";
                    }
                    eventLogParent.setString(row, "eventtype", "LocationChange");
                    eventLogParent.setString(row, "trackitemid", ds.getString(j, "trackitemid", ""));
                    eventLogParent.setString(row, "departmentid", ds.getString(j, "custodialdepartmentid", ""));
                    eventLogParent.setString(row, "oldvalue", oldlabelpath);
                    eventLogParent.setString(row, "newvalue", currentlabelpath);
                    eventLogParent.setString(row, "oldstorageunitid", oldstorageunitid);
                    eventLogParent.setString(row, "oldlabelpath", oldlabelpath);
                    eventLogParent.setString(row, "newstorageunitid", storageunitid);
                    eventLogParent.setString(row, "newlabelpath", currentlabelpath);
                    eventLogParent.setString(row, "eventsdcid", ds.getString(j, "linksdcid", ""));
                    eventLogParent.setString(row, "eventkeyid1", ds.getString(j, "linkkeyid1", ""));
                    eventLogParent.setString(row, "eventkeyid2", ds.getString(j, "linkkeyid2", ""));
                    eventLogParent.setString(row, "eventkeyid3", ds.getString(j, "linkkeyid3", ""));
                    eventLogParent.setString(row, "tracelogid", tracelogid);
                }
            }
            eventLogParent.process();
        }
    }

    private void updateSpaceAvailableAndStatus(Set<String> storageUnitSet, PropertyList actionProps) throws SapphireException {
        if (storageUnitSet.size() > 0) {
            DataSet spaceAvail = new DataSet();
            spaceAvail.addColumn("storageunitid", 0);
            spaceAvail.addColumn("spaceavailflag", 0);
            DataSet boxds = new DataSet();
            boxds.addColumn("boxid", 0);
            boxds.addColumn("status", 0);
            HashSet<String> boxes = new HashSet<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select storageunitid, storageunittype, maxtiallowed, linksdcid, linkkeyid1, parentid, spaceavailflag, propertytreeid,");
            sql.append(" (select b.boxstatus from s_box b where b.s_boxid = linkkeyid1) boxstatus,");
            sql.append(" (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = storageunit.storageunitid) childticount,");
            sql.append(" (select s.linksdcid from storageunit s where s.storageunitid = storageunit.parentid) parentsdcid,");
            sql.append(" (select s.linkkeyid1 from storageunit s where s.storageunitid = storageunit.parentid) parentkeyid1");
            sql.append(" from storageunit");
            sql.append(" where storageunitid in ( [] )");
            DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDCID, sql.toString(), OpalUtil.toDelimitedString(storageUnitSet, ";"));
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    String storageunitid = ds.getValue(i, "storageunitid");
                    int maxtiallowed = ds.getInt(i, "maxtiallowed", 0);
                    int childticount = ds.getInt(i, "childticount", 0);
                    String spaceavailflag = ds.getValue(i, "spaceavailflag", "N");
                    if (maxtiallowed > 0) {
                        if (childticount >= maxtiallowed && "Y".equals(spaceavailflag)) {
                            int row = spaceAvail.addRow();
                            spaceAvail.setValue(row, "storageunitid", storageunitid);
                            spaceAvail.setValue(row, "spaceavailflag", "N");
                        } else if (childticount < maxtiallowed && "N".equals(spaceavailflag)) {
                            int row = spaceAvail.addRow();
                            spaceAvail.setValue(row, "storageunitid", storageunitid);
                            spaceAvail.setValue(row, "spaceavailflag", "Y");
                        }
                    }
                    String linksdcid = ds.getValue(i, "linksdcid");
                    String linkkeyid1 = ds.getValue(i, "linkkeyid1");
                    String parentsdcid = ds.getValue(i, "parentsdcid");
                    if ("LV_Box".equals(linksdcid)) {
                        int row;
                        String boxstatus = ds.getValue(i, "boxstatus");
                        if (maxtiallowed == -1) {
                            if (childticount > 0) {
                                if ("Partial".equals(boxstatus)) continue;
                                row = boxds.addRow();
                                boxds.setValue(row, "boxid", linkkeyid1);
                                boxds.setValue(row, "status", "Partial");
                                continue;
                            }
                            if ("Empty".equals(boxstatus)) continue;
                            row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Empty");
                            continue;
                        }
                        if (childticount <= 0) {
                            if ("Empty".equals(boxstatus)) continue;
                            row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Empty");
                            continue;
                        }
                        if (childticount < maxtiallowed) {
                            if ("Partial".equals(boxstatus)) continue;
                            row = boxds.addRow();
                            boxds.setValue(row, "boxid", linkkeyid1);
                            boxds.setValue(row, "status", "Partial");
                            continue;
                        }
                        if (childticount < maxtiallowed || "Full".equals(boxstatus)) continue;
                        row = boxds.addRow();
                        boxds.setValue(row, "boxid", linkkeyid1);
                        boxds.setValue(row, "status", "Full");
                        continue;
                    }
                    if (!"LV_Box".equals(parentsdcid)) continue;
                    boxes.add(ds.getValue(i, "parentkeyid1"));
                }
            }
            if (spaceAvail.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", spaceAvail.getColumnValues("storageunitid", ";"));
                props.setProperty("spaceavailflag", spaceAvail.getColumnValues("spaceavailflag", ";"));
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("__syncoperation", "Y");
                props.setProperty("propsmatch", "Y");
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
                    sql.append(" and su.linkkeyid1 = ? )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{boxid});
                    if (ds == null) continue;
                    int countN = 0;
                    int countY = 0;
                    for (int i = 0; i < ds.size(); ++i) {
                        if ("N".equals(ds.getValue(i, "spaceavailflag", "N"))) {
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
                props.setProperty("__overrule", "Y");
                props.setProperty("__sdcruleconfirm", "Y");
                props.setProperty("propsmatch", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void syncSpaceAvailFlag(List<String> storageunits, PropertyList actionProps) throws SapphireException {
        String storageunitid;
        int i;
        if (storageunits.size() <= 0) return;
        String rsetid = this.getDAMProcessor().createRSet(SDCID, OpalUtil.toDelimitedString(storageunits, ";"), null, null);
        if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[StorageUnitSDC.syncSpaceAvailFlag] Unable to create RSET for " + OpalUtil.toDelimitedString(storageunits, ";"));
        StringBuilder sql = new StringBuilder();
        sql.append("select storageunitid, linksdcid, linkkeyid1, storageunittype, maxtiallowed, spaceavailflag, parentid,");
        sql.append(" (select count(trackitemid) from trackitem where currentstorageunitid = storageunit.storageunitid) contentcount");
        sql.append(" from storageunit, rsetitems");
        sql.append(" where storageunit.storageunitid = rsetitems.keyid1");
        sql.append(" and rsetitems.sdcid = 'StorageUnitSDC'");
        sql.append(" and rsetitems.rsetid = ?");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
        this.getDAMProcessor().clearRSet(rsetid);
        if (ds == null) return;
        HashSet<String> boxunits = new HashSet<String>();
        DataSet spaceds = new DataSet();
        spaceds.addColumn("storageunitid", 0);
        spaceds.addColumn("spaceavailflag", 0);
        for (i = 0; i < ds.size(); ++i) {
            String spaceavailable;
            int maxtiallowed;
            storageunitid = ds.getValue(i, "storageunitid");
            String storageunittype = ds.getValue(i, "storageunittype");
            if ("BoxPos".equals(storageunittype)) {
                boxunits.add(ds.getValue(i, "parentid"));
            }
            if ((maxtiallowed = ds.getInt(i, "maxtiallowed", 0)) == -1) continue;
            int contentcount = ds.getInt(i, "contentcount", 0);
            String string = spaceavailable = contentcount >= maxtiallowed ? "N" : "Y";
            if (spaceavailable.equals(ds.getValue(i, "spaceavailflag", "Y"))) continue;
            int row = spaceds.addRow();
            spaceds.setValue(row, "storageunitid", storageunitid);
            spaceds.setValue(row, "spaceavailflag", spaceavailable);
        }
        if (boxunits.size() > 0) {
            sql.setLength(0);
            sql.append("select storageunit.storageunitid, linksdcid, linkkeyid1, spaceavailflag,");
            sql.append(" (select count( trackitem.trackitemid ) from trackitem where trackitem.currentstorageunitid in (select storageunit2.storageunitid from storageunit storageunit2 where storageunit2.parentid = storageunit.storageunitid ) ) contentcount,");
            sql.append(" (select count(storageunit2.storageunitid) from storageunit storageunit2 where storageunit2.parentid = storageunit.storageunitid ) childcount");
            sql.append(" from storageunit");
            if (boxunits.size() > 750) {
                rsetid = this.getDAMProcessor().createRSet(SDCID, OpalUtil.toDelimitedString(boxunits, ";"), null, null);
                if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[StorageUnitSDC.syncSpaceAvailFlag] Unable to create RSET for " + OpalUtil.toDelimitedString(boxunits, ";"));
                sql.append(" where storageunit.storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append(" where storageunit.storageunitid in (").append(safeSQL.addIn(boxunits)).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                for (i = 0; i < ds.size(); ++i) {
                    String spaceavailable;
                    storageunitid = ds.getValue(i, "storageunitid");
                    int childcount = ds.getInt(i, "childcount", 0);
                    int contentcount = ds.getInt(i, "contentcount", 0);
                    String string = spaceavailable = contentcount >= childcount ? "N" : "Y";
                    if (spaceavailable.equals(ds.getValue(i, "spaceavailflag", "Y"))) continue;
                    int row = spaceds.addRow();
                    spaceds.setValue(row, "storageunitid", storageunitid);
                    spaceds.setValue(row, "spaceavailflag", spaceavailable);
                }
            }
        }
        if (spaceds.size() <= 0) return;
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDCID);
        props.setProperty("keyid1", spaceds.getColumnValues("storageunitid", ";"));
        props.setProperty("spaceavailflag", spaceds.getColumnValues("spaceavailflag", ";"));
        props.setProperty("propsmatch", "Y");
        props.setProperty("__sdcruleconfirm", "Y");
        props.setProperty("__createstorageunit", "Y");
        props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
    }

    private void initializePrimaryDS(DataSet primary, PropertyList actionProps) {
        primary.addColumn("linksdcid", 0);
        primary.addColumn("linkkeyid1", 0);
        primary.addColumn("linkkeyid2", 0);
        primary.addColumn("linkkeyid3", 0);
        primary.addColumn("parentid", 0);
        primary.addColumn("storageunitsize", 1);
        primary.addColumn("storageunitindex", 0);
        primary.addColumn("generatedlabel", 0);
        primary.addColumn("labelpath", 0);
        primary.addColumn("ancestorid", 0);
        primary.addColumn("storageunitlabel", 0);
        primary.addColumn("labelrow", 0);
        primary.addColumn("labelcol", 0);
        String suHierarchyid = actionProps.getProperty("suhierarchyid");
        String suParentid = actionProps.getProperty("suparentid");
        String suPropertyTreeid = actionProps.getProperty("propertytreeid");
        String suNodeid = actionProps.getProperty("nodeid");
        String suPropNodeid = actionProps.getProperty("propnodeid");
        primary.addColumnValues("_suhierarchyid", 0, suHierarchyid, ";");
        primary.addColumnValues("_suparentid", 0, suParentid, ";");
        primary.addColumnValues("_propertytreeid", 0, suPropertyTreeid, ";");
        primary.addColumnValues("_nodeid", 0, suNodeid, ";");
        primary.addColumnValues("_propnodeid", 0, suPropNodeid, ";");
        if (actionProps.containsKey("arraylayoutid")) {
            primary.addColumnValues("arraylayoutid", 0, actionProps.getProperty("arraylayoutid"), ";");
            primary.addColumnValues("arraylayoutversionid", 0, actionProps.getProperty("arraylayoutversionid"), ";");
        }
        WebAdminProcessor wap = new WebAdminProcessor(this.getConnectionid());
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.isValidColumn("maxtiallowed") && StringUtil.getLen(primary.getValue(i, "maxtiallowed")) == 0L) {
                primary.setValue(i, "maxtiallowed", "0");
            }
            String propertytreeid = primary.getString(i, "_propertytreeid");
            String nodeid = primary.getString(i, "_nodeid");
            String arraylayoutid = "";
            String arraylayoutversionid = "";
            if (STORAGEUNITTYPE_GRID.equals(propertytreeid)) {
                PropertyList storageunittype = StorageUnitUtil.getDefinition(wap, nodeid, propertytreeid);
                int rows = 0;
                int columns = 0;
                try {
                    rows = Integer.parseInt(storageunittype.getProperty("rows", "0"));
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                try {
                    columns = Integer.parseInt(storageunittype.getProperty("columns", "0"));
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                primary.setNumber(i, "numrows", rows);
                primary.setNumber(i, "numcol", columns);
                arraylayoutid = primary.getString(i, "arraylayoutid", "");
                arraylayoutversionid = OpalUtil.isNotEmpty(arraylayoutid) ? primary.getString(i, "arraylayoutversionid", "") : "";
            }
            primary.setString(i, "arraylayoutid", arraylayoutid);
            primary.setString(i, "arraylayoutversionid", arraylayoutversionid);
        }
    }

    private void assignParentIds(DataSet primary, HashMap parentMap) {
        int primarySize = primary.size();
        for (int count = 0; count < primarySize; ++count) {
            String suHierarchyParentId = primary.getValue(count, "_suparentid");
            String parentid = (String)parentMap.get(suHierarchyParentId);
            if (parentid == null || parentid.trim().length() <= 0) continue;
            primary.setValue(count, "parentid", parentid);
        }
    }

    private HashMap getParentMap(DataSet primary) {
        HashMap<String, String> parentMap = new HashMap<String, String>();
        int primarySize = primary.size();
        for (int count = 0; count < primarySize; ++count) {
            String storageUnitid = primary.getValue(count, "storageunitid");
            String suHierarchyid = primary.getValue(count, "_suhierarchyid");
            parentMap.put(suHierarchyid, storageUnitid);
        }
        return parentMap;
    }

    private void createAssociatedSDIs(DataSet primary, PropertyList actionProps, List<String> distinctNodeids, Map<String, PropertyList> nodePropTreeMap) {
        String forceUpdateFlag = actionProps.getProperty("__sdcruleconfirm");
        Map<String, String> useSDIIdAsLabelMap = this.getUseSDIIdAsLabelMap(distinctNodeids, nodePropTreeMap);
        HashMap<String, DataSet> nodeFilteredDSMap = new HashMap<String, DataSet>();
        int nodeCount = distinctNodeids.size();
        ActionBlock ab = new ActionBlock();
        int actionCount = 0;
        HashMap<String, String> filterMap = new HashMap<String, String>();
        ArrayList<String> addSDINodeList = new ArrayList<String>();
        try {
            for (String distinctNodeid : distinctNodeids) {
                String propnodeid = distinctNodeid;
                PropertyList nodePropertyList = nodePropTreeMap.get(propnodeid);
                PropertyList template = nodePropertyList.getPropertyList("template");
                if (template == null) continue;
                String templateSDCId = template.getProperty("sdcid");
                String templateKeyid1 = template.getProperty("keyid1");
                String templateKeyid2 = template.getProperty("keyid2");
                String templateKeyid3 = template.getProperty("keyid3");
                if (templateSDCId.trim().length() <= 0) continue;
                if (!this.isSDCKeysAutoGenerated(templateSDCId)) {
                    this.setError("Associating SDI(s)", "VALIDATION", "SDC: " + templateSDCId + " Keys not autogenerated. Cannot create SDI(s).");
                    continue;
                }
                filterMap.clear();
                filterMap.put("_propnodeid", propnodeid);
                DataSet filteredNodeDS = primary.getFilteredDataSet(filterMap);
                PropertyList addSDIProps = new PropertyList();
                addSDIProps.setProperty("sdcid", templateSDCId);
                addSDIProps.setProperty("templatekeyid1", templateKeyid1);
                addSDIProps.setProperty("templatekeyid2", templateKeyid2);
                addSDIProps.setProperty("templatekeyid3", templateKeyid3);
                addSDIProps.setProperty("__sdcruleconfirm", forceUpdateFlag);
                if (SDC_PHYSICALSTORE.equals(templateSDCId)) {
                    addSDIProps.setProperty("physicalstoredesc", filteredNodeDS.getColumnValues("storageunitdesc", ";"));
                    addSDIProps.setProperty("freezername", filteredNodeDS.getColumnValues("storageunitlabel", ";"));
                }
                String parentids = filteredNodeDS.getColumnValues("parentid", ";");
                addSDIProps.setProperty("currentstoragelocation", parentids);
                addSDIProps.setProperty("copies", Integer.toString(filteredNodeDS.size()));
                if ("LV_Box".equals(templateSDCId)) {
                    String boxdata = actionProps.getProperty("primaryboxdata");
                    if (OpalUtil.isNotEmpty(boxdata)) {
                        JSONObject json = new JSONObject(boxdata);
                        Iterator iterator = json.keys();
                        while (iterator.hasNext()) {
                            String key = (String)iterator.next();
                            addSDIProps.setProperty(key, json.getString(key));
                        }
                    }
                    addSDIProps.setProperty("boxstatus", "Empty");
                    String auditreason = actionProps.getProperty("auditreason", "");
                    if (auditreason.length() == 0 || auditreason.equals("DataAdd")) {
                        actionProps.setProperty("auditreason", "New " + this.getSDCProcessor().getProperty("LV_Box", "singular") + " Added");
                    }
                    if (addSDIProps.getProperty("keyid1").equals(addSDIProps.getProperty("templatekeyid1")) || "Y".equals(addSDIProps.getProperty("templateflag")) && !"Y".equals(addSDIProps.getProperty("overrideautokey"))) {
                        addSDIProps.remove("templatekeyid1");
                    }
                }
                addSDIProps.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                addSDIProps.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                addSDIProps.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                addSDIProps.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                ab.setAction("action_" + actionCount, "AddSDI", "1", addSDIProps);
                addSDINodeList.add(propnodeid);
                nodeFilteredDSMap.put(propnodeid, filteredNodeDS);
                ++actionCount;
            }
            if (actionCount > 0) {
                try {
                    this.getActionProcessor().processActionBlock(ab);
                    ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                    if (errorHandler != null && errorHandler.hasInfoErrors()) {
                        this.setErrors(this.getActionProcessor().getErrorHandler());
                    }
                }
                catch (ActionException e) {
                    this.setErrors(e.getErrorHandler());
                }
            }
            for (int count = 0; count < actionCount; ++count) {
                HashMap props = ab.getActionProperties("action_" + count);
                String sdcid = (String)props.get("sdcid");
                String newKeyid1 = (String)props.get("newkeyid1");
                String newKeyid2 = (String)props.get("newkeyid2");
                String newKeyid3 = (String)props.get("newkeyid3");
                String propnodeid = (String)addSDINodeList.get(count);
                String useSDIIdAsLabel = useSDIIdAsLabelMap.get(propnodeid);
                DataSet filteredNodeDS = (DataSet)nodeFilteredDSMap.get(propnodeid);
                String[] newKeyid1Arr = StringUtil.split(newKeyid1, ";");
                String[] newKeyid2Arr = null;
                String[] newKeyid3Arr = null;
                boolean isNewKeyid2Present = false;
                if (newKeyid2 != null && newKeyid2.trim().length() > 0) {
                    newKeyid2Arr = StringUtil.split(newKeyid2, ";");
                }
                boolean isNewKeyid3Present = false;
                if (newKeyid3 != null && newKeyid3.trim().length() > 0) {
                    newKeyid3Arr = StringUtil.split(newKeyid3, ";");
                }
                for (int dsRow = 0; dsRow < filteredNodeDS.size(); ++dsRow) {
                    filteredNodeDS.setValue(dsRow, "linksdcid", sdcid);
                    filteredNodeDS.setValue(dsRow, "linkkeyid1", newKeyid1Arr[dsRow]);
                    if ("Y".equalsIgnoreCase(useSDIIdAsLabel)) {
                        filteredNodeDS.setValue(dsRow, "storageunitlabel", newKeyid1Arr[dsRow]);
                    }
                    if (isNewKeyid2Present) {
                        filteredNodeDS.setValue(dsRow, "linkkeyid2", newKeyid2Arr[dsRow]);
                    }
                    if (!isNewKeyid3Present) continue;
                    filteredNodeDS.setValue(dsRow, "linkkeyid3", newKeyid3Arr[dsRow]);
                }
            }
        }
        catch (ActionException aex) {
            this.logger.stackTrace(aex);
            this.setErrors(aex.getErrorHandler());
            this.setError("Associating SDI(s)", "VALIDATION", this.getTranslationProcessor().translate("Failed to create associated SDI(s)") + " ");
        }
        catch (Exception ex) {
            this.logger.stackTrace(ex);
            this.setError("Associating SDI(s)", "VALIDATION", this.getTranslationProcessor().translate("Failed to create associated SDI(s)") + " ");
        }
    }

    private List<String> getDistinctColumnValues(DataSet ds, String columnid) {
        ArrayList<String> distinctColValues = new ArrayList<String>();
        if (ds != null && columnid != null && ds.isValidColumn(columnid)) {
            String columnValues = ds.getColumnValues(columnid, ";");
            String[] columnValuesArr = StringUtil.split(columnValues, ";");
            for (int count = 0; count < ds.size(); ++count) {
                if (distinctColValues.contains(columnValuesArr[count])) continue;
                distinctColValues.add(columnValuesArr[count]);
            }
        }
        return distinctColValues;
    }

    private Map<String, PropertyTree> getPropertyTreeMap(List<String> proptreeids, String proptreetype) {
        HashMap<String, PropertyTree> propTreeMap = new HashMap<String, PropertyTree>();
        if (proptreeids != null && proptreeids.size() > 0 && proptreetype != null) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT propertytreeid FROM propertytree WHERE propertytreetype = " + safeSQL.addVar(proptreetype) + " and propertytreeid in ( " + safeSQL.addIn(proptreeids) + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null) {
                try {
                    for (int count = 0; count < ds.getRowCount(); ++count) {
                        String propertytreeid = ds.getValue(count, "propertytreeid");
                        PropertyTree propertyTree = new WebAdminProcessor(this.getConnectionId()).getPropertyTree(propertytreeid);
                        propTreeMap.put(propertytreeid, propertyTree);
                    }
                }
                catch (Exception sapEx) {
                    this.logger.stackTrace(sapEx);
                    this.setError("Property Retrieval", "VALIDATION", "Failed to retrieve property tree for storage unit type.");
                }
            }
        }
        return propTreeMap;
    }

    private Map<String, PropertyList> getNodeTreeMap(List<String> distinctNodeids, Map<String, PropertyTree> propTreeMap) {
        HashMap<String, PropertyList> nodeTreeMap = new HashMap<String, PropertyList>();
        if (distinctNodeids == null || propTreeMap == null) {
            return nodeTreeMap;
        }
        try {
            for (String propnode : distinctNodeids) {
                String[] propnodeArr = StringUtil.split(propnode, "|");
                String propertyTreeid = propnodeArr[0];
                String nodeid = propnodeArr[1];
                PropertyTree propertyTree = propTreeMap.get(propertyTreeid);
                PropertyList nodePropertyList = propertyTree.getNodePropertyList(nodeid, true);
                nodeTreeMap.put(propnode, nodePropertyList);
            }
        }
        catch (SapphireException sapEx) {
            this.setError("Property Retrieval", "VALIDATION", "Failed to retrieve property tree for storage unit type node.");
        }
        return nodeTreeMap;
    }

    private boolean isSDCKeysAutoGenerated(String sdcid) {
        String keyGenerationRule;
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        boolean autoKeyGenerationFlag = false;
        HashMap sdcProps = sdcProcessor.getSDCProperties(sdcid);
        if (sdcProps != null && (keyGenerationRule = (String)sdcProps.get("keygenerationrule")) != null && keyGenerationRule.trim().length() > 0) {
            autoKeyGenerationFlag = true;
        }
        return autoKeyGenerationFlag;
    }

    private void assignSizeAndLabels(DataSet primary, Map<String, String> propTreeClassMap, Map<String, PropertyList> nodePropTreeMap, Map<String, String> useSDIIdAsLabelMap) {
        List<String> distinctParentids = this.getDistinctColumnValues(primary, "parentid");
        HashMap propertyTreeLabelGenerators = new HashMap();
        HashMap<String, String> filterMap = new HashMap<String, String>();
        if (childAddMode.booleanValue()) {
            this.initializeParentStorageUnit(primary.getValue(0, "parentid"), primary, useSDIIdAsLabelMap);
        }
        ArrayList<String> suWithZoneList = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String storageUnitid = primary.getValue(i, "storageunitid");
            String arraylayoutid = primary.getString(i, "arraylayoutid", "");
            if (arraylayoutid.length() > 0) {
                suWithZoneList.add(storageUnitid);
            }
            int childSize = 0;
            String propnodeid = primary.getValue(i, "_propnodeid");
            PropertyList storageUnitTypeProps = nodePropTreeMap.get(propnodeid);
            if (distinctParentids.contains(storageUnitid)) {
                String labelpath;
                String parentid;
                filterMap.clear();
                filterMap.put("parentid", storageUnitid);
                DataSet child = primary.getFilteredDataSet(filterMap);
                childSize = child.size();
                String proptreeid = primary.getValue(i, "_propertytreeid");
                String className = propTreeClassMap.get(proptreeid);
                if (StringUtil.getLen(className) > 0L && childSize > 0) {
                    this.assignLabels(child, className, proptreeid, propnodeid, propertyTreeLabelGenerators, storageUnitTypeProps);
                }
                if ((parentid = primary.getValue(i, "parentid")).trim().length() == 0) {
                    labelpath = "/" + primary.getValue(i, "storageunitlabel");
                    primary.setValue(i, "labelpath", labelpath);
                } else {
                    labelpath = primary.getValue(i, "labelpath");
                }
                String storageenvid = primary.getValue(i, "storageenvid");
                String ancestorid = storageenvid.trim().length() > 0 ? storageUnitid : primary.getValue(i, "ancestorid");
                this.setLabelPathAndAncestor(child, labelpath, ancestorid, useSDIIdAsLabelMap);
            } else {
                String parentid = primary.getValue(i, "parentid");
                if (parentid.trim().length() == 0) {
                    String labelpath = "/" + primary.getValue(i, "storageunitlabel");
                    primary.setValue(i, "labelpath", labelpath);
                }
            }
            primary.setNumber(i, "storageunitsize", childSize);
        }
    }

    private void initializeParentStorageUnit(String parentid, DataSet primary, Map<String, String> SDIIdAsLabelMap) {
        StorageUnitSDC.parentid = parentid;
        QueryProcessor queryProcessor = this.getQueryProcessor();
        lastIndex = StorageUnitSDC.getLastIndex(queryProcessor, parentid);
        HashMap parentInfo = StorageUnitSDC.getParentInfo(queryProcessor, parentid);
        parentLabel = (String)parentInfo.get("labelpath");
        parentStorageUnitSize = Integer.parseInt(parentInfo.get("storageunitsize").toString());
        String nodeid = (String)parentInfo.get("storageunittype");
        String proptreeid = (String)parentInfo.get("propertytreeid");
        String ancestorid = (String)parentInfo.get("ancestorid");
        String propnodeid = proptreeid + "|" + nodeid;
        ArrayList<String> distinctPropTreeids = new ArrayList<String>();
        distinctPropTreeids.add(proptreeid);
        Map<String, PropertyTree> propTreeMap = this.getPropertyTreeMap(distinctPropTreeids, "StorageUnitType");
        ArrayList<String> distinctNodeids = new ArrayList<String>();
        distinctNodeids.add(propnodeid);
        Map<String, PropertyList> nodePropTreeMap = this.getNodeTreeMap(distinctNodeids, propTreeMap);
        Map<String, String> useSDIIdAsLabelMap = this.getUseSDIIdAsLabelMap(distinctNodeids, nodePropTreeMap);
        for (String key : SDIIdAsLabelMap.keySet()) {
            useSDIIdAsLabelMap.put(key, SDIIdAsLabelMap.get(key));
        }
        Map<String, String> propTreeClassMap = this.getPropertyTreeClassMap(distinctPropTreeids, "StorageUnitType");
        PropertyList storageUnitTypeProps = nodePropTreeMap.get(propnodeid);
        HashMap propertyTreeLabelGenerators = new HashMap();
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.clear();
        filterMap.put("parentid", parentid);
        DataSet child = primary.getFilteredDataSet(filterMap);
        int childSize = child.size();
        parentStorageUnitSize = parentStorageUnitSize + childSize;
        String className = propTreeClassMap.get(proptreeid);
        if (StringUtil.getLen(className) > 0L && childSize > 0) {
            this.assignLabels(child, className, proptreeid, propnodeid, propertyTreeLabelGenerators, storageUnitTypeProps);
        }
        this.setLabelPathAndAncestor(child, parentLabel, ancestorid, useSDIIdAsLabelMap);
    }

    private void assignLabels(DataSet child, String className, String proptreeid, String parentPropNodeId, HashMap propertyTreeLabelGenerators, PropertyList storageUnitTypeProps) {
        if (child == null || child.size() == 0 || StringUtil.getLen(className) == 0L) {
            return;
        }
        try {
            int childrenSize = child.size();
            String parentid = child.getValue(0, "parentid");
            String nodePropSize = parentPropNodeId + "|" + Integer.toString(childrenSize);
            BaseStorageUnitType storageUnitType = (BaseStorageUnitType)propertyTreeLabelGenerators.get(nodePropSize);
            if (childAddMode.booleanValue() && parentid.equalsIgnoreCase(StorageUnitSDC.parentid) && storageUnitType != null) {
                storageUnitType.setStartIndex(lastIndex);
                storageUnitType.initialize(child.size());
            }
            if (storageUnitType == null) {
                storageUnitType = (BaseStorageUnitType)Class.forName(className).newInstance();
                storageUnitType.setStorageUnitType(storageUnitTypeProps);
                if (childAddMode.booleanValue() && parentid.equalsIgnoreCase(StorageUnitSDC.parentid)) {
                    storageUnitType.setStartIndex(lastIndex);
                }
                storageUnitType.initialize(child.size());
                propertyTreeLabelGenerators.put(nodePropSize, storageUnitType);
            }
            String storageUnitIndices = storageUnitType.getStroageUnitIndices();
            String storageUnitLabels = storageUnitType.getStorageUnitLabels();
            child.addColumnValues("storageunitindex", 0, storageUnitIndices, ";");
            child.addColumnValues("generatedlabel", 0, storageUnitLabels, ";");
            child.addColumnValues("labelrow", 0, storageUnitType.getStorageUnitRowLabels(), ";");
            child.addColumnValues("labelcol", 0, storageUnitType.getStorageUnitColumnLabels(), ";");
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            this.setError("Label Generation", "VALIDATION", "Cannot generate Labels for " + proptreeid);
        }
    }

    private void setLabelPathAndAncestor(DataSet childrenDS, String parentLabelPath, String ancestorid, Map<String, String> useSDIIdAsLabelMap) {
        boolean setAncestorFlag = ancestorid.trim().length() > 0;
        for (int count = 0; count < childrenDS.size(); ++count) {
            String storageUnitLabel = childrenDS.getValue(count, "storageunitlabel");
            String generatedLabel = childrenDS.getValue(count, "generatedlabel");
            String propnodeid = childrenDS.getValue(count, "_propnodeid");
            String useSDIIdAsLabel = useSDIIdAsLabelMap.get(propnodeid);
            if (!"Y".equalsIgnoreCase(useSDIIdAsLabel)) {
                storageUnitLabel = storageUnitLabel + generatedLabel;
                childrenDS.setValue(count, "storageunitlabel", storageUnitLabel);
            }
            String labelpath = parentLabelPath + "/" + storageUnitLabel;
            childrenDS.setValue(count, "labelpath", labelpath);
            String storageenvid = childrenDS.getValue(count, "storageenvid");
            if (!setAncestorFlag || storageenvid.trim().length() != 0) continue;
            childrenDS.setValue(count, "ancestorid", ancestorid);
        }
    }

    private Map<String, String> getPropertyTreeClassMap(List<String> proptreeids, String proptreetype) {
        HashMap<String, String> propTreeClassMap = new HashMap<String, String>();
        if (proptreeids != null && proptreeids.size() > 0 && proptreetype != null) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT propertytreeid, objectname FROM propertytree WHERE propertytreetype = " + safeSQL.addVar(proptreetype) + " and propertytreeid in (" + safeSQL.addIn(proptreeids) + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    String propertytreeid = ds.getValue(i, "propertytreeid");
                    String className = ds.getValue(i, "objectname");
                    propTreeClassMap.put(propertytreeid, className);
                }
            }
        }
        return propTreeClassMap;
    }

    private Map<String, String> getUseSDIIdAsLabelMap(List<String> distinctNodeids, Map<String, PropertyList> nodePropTreeMap) {
        HashMap<String, String> useSDIIdAsLabelMap = new HashMap<String, String>();
        for (String propnodeid : distinctNodeids) {
            PropertyList templateProps;
            PropertyList nodePropList = nodePropTreeMap.get(propnodeid);
            String useSDIIdAsLabel = "N";
            if (nodePropList != null && (templateProps = nodePropList.getPropertyList("template")) != null) {
                useSDIIdAsLabel = templateProps.getProperty("usesdiidaslabel");
            }
            if (useSDIIdAsLabel.trim().length() == 0) {
                useSDIIdAsLabel = "N";
            }
            useSDIIdAsLabelMap.put(propnodeid, useSDIIdAsLabel);
        }
        return useSDIIdAsLabelMap;
    }

    private void processStorageUnitMoveRule(DataSet primary, int count) throws SapphireException {
        String storageunitid = primary.getValue(count, "storageunitid");
        if (!StorageUnitSDC.isMoveable(this.getQueryProcessor(), storageunitid)) {
            throw new SapphireException("StorageUnit Move Rule", "VALIDATION", this.getTranslationProcessor().translate("StorageUnit is not moveable") + ". (" + storageunitid + " - " + primary.getValue(count, "labelpath") + ")");
        }
    }

    private void processAncestorChangeRule(DataSet ds, PropertyList actionProps) {
        if (ds != null && ds.size() > 0) {
            TranslationProcessor tp = this.getTranslationProcessor();
            String storageUnitIds = ds.getColumnValues("storageunitid", ";");
            PropertyList props = new PropertyList();
            props.setProperty("storageunitid", storageUnitIds);
            props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
            try {
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
                props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
                this.getActionProcessor().processAction("SyncStorageAncestor", "1", props);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException aex) {
                this.setErrors(aex.getErrorHandler());
                this.setError("AncestorAndLabel Change Rule", "VALIDATION", tp.translate("Failed to update Ancestor or Label path"));
            }
            catch (Exception ex) {
                this.logger.stackTrace(ex);
                this.setError("AncestorAndLabel Change Rule", "VALIDATION", tp.translate("Failed to update Ancestor or Label path"));
            }
        }
    }

    private void assignLinkedSDIs(DataSet primary, PropertyList actionProps) {
        String linksdcid = actionProps.getProperty("_linksdcid");
        String linkkeyid1 = actionProps.getProperty("_linkkeyid1");
        String linkPropNodeId = actionProps.getProperty("_linkpropnodeid");
        String[] linkkeyid1Arr = null;
        HashMap<String, String> filterMap = new HashMap<String, String>();
        if (primary == null) {
            return;
        }
        filterMap.put("_propnodeid", linkPropNodeId);
        DataSet filterDS = primary.getFilteredDataSet(filterMap);
        if (linkkeyid1 != null) {
            linkkeyid1Arr = StringUtil.split(linkkeyid1, ";");
        }
        if (linkkeyid1Arr != null && filterDS != null && linkkeyid1Arr.length == filterDS.size()) {
            String useSDIIdAsLabelFlag = "Y";
            for (int count = 0; count < filterDS.size(); ++count) {
                String linkedsdiid1 = linkkeyid1Arr[count];
                filterDS.setValue(count, "linksdcid", linksdcid);
                filterDS.setValue(count, "linkkeyid1", linkedsdiid1);
                if (!"Y".equalsIgnoreCase(useSDIIdAsLabelFlag)) continue;
                filterDS.setValue(count, "storageunitlabel", linkedsdiid1);
            }
        } else {
            this.setError("AssignLinkedSDIs", "VALIDATION", this.getTranslationProcessor().translate("Number of created Storageunits donot match the passed in linked sdis"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void processSizeChangeRule(String storageUnitIds, PropertyList actionProps) throws SapphireException {
        if (StringUtil.getLen(storageUnitIds) <= 0L) return;
        String rsetid = this.getDAMProcessor().createRSet(SDCID, storageUnitIds, null, null);
        if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("[StorageUnitSDC.processSizeChangeRule] Unable to create RSET for " + storageUnitIds);
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT T1.storageunitid, T1.storageunitsize, ");
            sql.append(" (SELECT COUNT( T2.storageunitid) FROM storageunit T2 WHERE T2.parentid = T1.storageunitid ) calculatedsize ");
            sql.append(" FROM storageunit T1 ");
            sql.append(" WHERE T1.storageunitid IN (SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ?) ");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            if (ds == null || ds.size() <= 0) return;
            StringBuilder modifiedSizeSDIs = new StringBuilder();
            StringBuilder calculatedSizes = new StringBuilder();
            for (int count = 0; count < ds.getRowCount(); ++count) {
                int calculatedSize;
                int storageUnitSize = ds.getInt(count, "storageunitsize");
                if (storageUnitSize == (calculatedSize = ds.getInt(count, "calculatedsize"))) continue;
                modifiedSizeSDIs.append(ds.getValue(count, "storageunitid")).append(";");
                calculatedSizes.append(ds.getValue(count, "calculatedsize")).append(";");
            }
            if (modifiedSizeSDIs.toString().trim().length() <= 0) return;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", modifiedSizeSDIs.toString());
            props.setProperty("storageunitsize", calculatedSizes.toString());
            props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm"));
            props.setProperty("tracelogid", actionProps.getProperty("tracelogid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler == null || !errorHandler.hasInfoErrors()) return;
            this.setErrors(this.getActionProcessor().getErrorHandler());
            return;
        }
        catch (ActionException aex) {
            this.setErrors(aex.getErrorHandler());
            this.setError("StorageUnit Size Rule", "VALIDATION", this.getTranslationProcessor().translate("Failed to set Storage Unit size"));
            return;
        }
        catch (Exception ex) {
            this.logger.stackTrace(ex);
            this.setError("StorageUnit Size Rule", "VALIDATION", this.getTranslationProcessor().translate("Failed to set Storage Unit size"));
            return;
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    public static String getAllStorageUnits(QueryProcessor queryProcessor, boolean isOracle, String storageunitid) {
        String allStorageUnitIds = "";
        DataSet ds = isOracle ? queryProcessor.getPreparedSqlDataSet("SELECT STORAGEUNITID FROM STORAGEUNIT CONNECT BY PRIOR STORAGEUNITID = PARENTID START WITH STORAGEUNITID = ?", (Object[])new String[]{storageunitid}) : StorageUnitSDC.populateChild(queryProcessor, storageunitid, null, isOracle);
        if (ds != null) {
            allStorageUnitIds = ds.getColumnValues("storageunitid", ";");
        }
        return allStorageUnitIds;
    }

    public static String getReseredStorageUnits(DBAccess database, String storageunitid) throws SapphireException {
        String reservedStorageUnits = "";
        String inclauseStorageUnitIds = StringUtil.replaceAll(storageunitid, ";", "','");
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT STORAGEUNITID ").append("FROM RESERVESTORAGEUNIT ").append("WHERE STORAGEUNITID IN ( ").append(safeSQL.addIn(inclauseStorageUnitIds)).append(" ) ");
        database.createPreparedResultSet(sqlStmt.toString(), safeSQL.getValues());
        DataSet ds = new DataSet();
        ds.setResultSet(database.getResultSet());
        if (ds != null && ds.size() > 0) {
            reservedStorageUnits = ds.getColumnValues("storageunitid", ";");
        }
        database.closeResultSet();
        return reservedStorageUnits;
    }

    public static void clearReservedStorageUnits(DBAccess database, String storageunitid) throws SapphireException {
        String inclauseStorageUnitIds = StringUtil.replaceAll(storageunitid, ";", "','");
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("DELETE  ");
        sqlStmt.append("FROM RESERVESTORAGEUNIT ");
        sqlStmt.append("WHERE STORAGEUNITID IN ( ").append(safeSQL.addIn(inclauseStorageUnitIds)).append(" ) ");
        database.executePreparedUpdate(sqlStmt.toString(), safeSQL.getValues());
    }

    public static String getStorageUntitidByLinkKeyid(QueryProcessor queryProcessor, String linksdcid, String linkkeyid1) {
        String storageunitid = "";
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT STORAGEUNITID FROM STORAGEUNIT WHERE LINKSDCID = ? AND LINKKEYID1 = ?", (Object[])new String[]{linksdcid, linkkeyid1});
        if (ds != null && ds.size() > 0) {
            storageunitid = ds.getValue(0, "storageunitid");
        }
        return storageunitid;
    }

    public static String getStorageNodeBySDC(QueryProcessor qp, String storageunitid, String linksdcid) {
        return StorageUnitSDC.getParentStorageUnitBySDC(qp, storageunitid, linksdcid, "storageunitid", new ConnectionProcessor(qp.getConnectionid()).isOra());
    }

    public static String getParentStorageUnitBySDC(QueryProcessor queryProcessor, String storageunitid, String sdcid, String columnid, boolean isOra) {
        String columnvalue = "";
        StringBuilder sql = new StringBuilder();
        if (isOra) {
            sql.append("select storageunitid, parentid, linksdcid, linkkeyid1, level");
            sql.append(" from storageunit");
            sql.append(" connect by prior parentid = storageunitid");
            sql.append(" start with storageunitid = ?");
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    if (!sdcid.equals(ds.getString(i, "linksdcid"))) continue;
                    columnvalue = ds.getString(i, columnid);
                }
            }
        } else {
            sql.append("select storageunitid, parentid, linksdcid, linkkeyid1");
            sql.append(" from storageunit where storageunitid = ?");
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds.size() > 0) {
                columnvalue = sdcid.equals(ds.getString(0, "linksdcid")) ? ds.getString(0, columnid) : StorageUnitSDC.getParentStorageUnitBySDC(queryProcessor, ds.getValue(0, "parentid"), sdcid, columnid, isOra);
            }
        }
        return columnvalue;
    }

    public static String getParentSDIBySDC(QueryProcessor qp, String storageunitid, String linksdcid) {
        return StorageUnitSDC.getParentStorageUnitBySDC(qp, storageunitid, linksdcid, "linkkeyid1", new ConnectionProcessor(qp.getConnectionid()).isOra());
    }

    public static String getLinkKeyid1ByStorageUnitId(DBAccess database, String storageunitid) throws SapphireException {
        String linkkeyid1 = "";
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT T1.STORAGEUNITID, T1.linkkeyid1");
        sql.append(" FROM STORAGEUNIT T1");
        sql.append(" WHERE STORAGEUNITID = ?");
        database.createPreparedResultSet(sql.toString(), new Object[]{storageunitid});
        DataSet ds = new DataSet();
        ds.setResultSet(database.getResultSet());
        if (ds != null && ds.size() > 0) {
            linkkeyid1 = ds.getValue(0, "linkkeyid1");
        }
        database.closeResultSet();
        return linkkeyid1;
    }

    public static String getLinkKeyid1ByStorageUnitId(QueryProcessor qp, String storageunitid) {
        return OpalUtil.getColumnValue(qp, "storageunit", "linkkeyid1", "storageunitid=?", new String[]{storageunitid});
    }

    public static String getLinkSDCIdByStorageUnitId(DBAccess database, String storageunitid) throws SapphireException {
        String linksdcid = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.STORAGEUNITID, T1.linkkeyid1, T1.linksdcid");
        sql.append(" FROM STORAGEUNIT T1");
        sql.append(" WHERE STORAGEUNITID = ?");
        database.createPreparedResultSet(sql.toString(), new Object[]{storageunitid});
        DataSet ds = new DataSet();
        ds.setResultSet(database.getResultSet());
        if (ds != null && ds.size() > 0) {
            linksdcid = ds.getValue(0, "linksdcid");
        }
        database.closeResultSet();
        return linksdcid;
    }

    public static String getLinkSDCIdByStorageUnitId(QueryProcessor qp, String storageunitid) {
        return OpalUtil.getColumnValue(qp, "storageunit", "linksdcid", "storageunitid=?", new String[]{storageunitid});
    }

    public static int getTrackItemCount(QueryProcessor qp, String storageunitid) {
        int count = 0;
        DataSet ds = qp.getPreparedSqlDataSet("select count(trackitemid) ticount from trackitem where currentstorageunitid = ?", (Object[])new String[]{storageunitid});
        if (ds != null) {
            count = ds.getInt(0, "ticount");
        }
        return count;
    }

    public static int getMaxTIAllowed(QueryProcessor qp, String storageunitid) {
        int count = 0;
        DataSet ds = qp.getPreparedSqlDataSet("select maxtiallowed from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            count = ds.getInt(0, "maxtiallowed");
        }
        return count;
    }

    public static DataSet getChildSUDetails(QueryProcessor qp, String storageunitid) {
        DataSet ds = new DataSet();
        if (storageunitid == null || storageunitid.trim().length() == 0) {
            return ds;
        }
        ds = qp.getPreparedSqlDataSet("SELECT STORAGEUNITID, STORAGEUNITTYPE, PROPERTYTREEID FROM STORAGEUNIT WHERE PARENTID = ?", (Object[])new String[]{storageunitid});
        return ds;
    }

    public static PropertyList getSUValueTree(QueryProcessor queryProcessor, String storageunitid) {
        return StorageUnitTypeDef.getInstance().getTypeDefinitionByID(queryProcessor, storageunitid);
    }

    public static String getStorageUnitSDIInfo(QueryProcessor qp, String storageunitid) {
        String msg;
        StringBuilder sb = new StringBuilder();
        DataSet ds = qp.getPreparedSqlDataSet("select storageunitid, linksdcid, linkkeyid1, storageunittype, parentid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            String type = ds.getValue(0, "storageunittype");
            if ("BoxPos".equals(type)) {
                sb.append(StorageUnitSDC.getStorageUnitSDIInfo(qp, ds.getValue(0, "parentid")));
            } else {
                sb.append(ds.getValue(0, "linksdcid")).append(" \"").append(ds.getValue(0, "linkkeyid1")).append("\"");
            }
        }
        if ("\"\"".equals((msg = sb.toString()).trim())) {
            msg = "Storageunit " + storageunitid;
        }
        return msg;
    }

    public static String getLabelPath(QueryProcessor qp, String storageunitid) {
        ArrayList list = new ArrayList();
        StorageUnitSDC.getLabelPath(qp, storageunitid, list);
        StringBuffer sb = new StringBuffer();
        int size = list.size();
        for (int i = size - 1; i >= 0; --i) {
            sb.append("/").append(list.get(i));
        }
        return sb.toString();
    }

    public static List getLabelPath(QueryProcessor qp, String storageunitid, List list) {
        DataSet ds = qp.getPreparedSqlDataSet("select storageunitid, storageunitlabel, parentid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            list.add(ds.getValue(0, "storageunitlabel"));
            String parentid = ds.getValue(0, "parentid");
            if (parentid != null && parentid.trim().length() > 0) {
                StorageUnitSDC.getLabelPath(qp, parentid, list);
            }
        }
        return list;
    }

    public static PropertyList getStorageUnitProps(QueryProcessor queryProcessor, String storageUnitId) {
        DataSet ds;
        StringBuilder sql;
        PropertyList props = new PropertyList();
        String storageSDC = "LV_Box";
        String parent = StorageUnitSDC.getStorageNodeBySDC(queryProcessor, storageUnitId, "LV_Box");
        if (StringUtil.getLen(parent) == 0L) {
            storageSDC = "Plate";
            parent = StorageUnitSDC.getStorageNodeBySDC(queryProcessor, storageUnitId, "Plate");
            if (StringUtil.getLen(parent) == 0L) {
                storageSDC = SDC_PHYSICALSTORE;
                parent = StorageUnitSDC.getStorageNodeBySDC(queryProcessor, storageUnitId, SDC_PHYSICALSTORE);
                if (StringUtil.getLen(parent) == 0L) {
                    storageSDC = "LV_Package";
                    parent = StorageUnitSDC.getStorageNodeBySDC(queryProcessor, storageUnitId, "LV_Package");
                }
            }
        }
        if (StringUtil.getLen(parent) > 0L) {
            sql = new StringBuilder();
            if (SDC_PHYSICALSTORE.equals(storageSDC)) {
                sql.append("select s.storageunitid, s.linksdcid, s.linkkeyid1, s.maxtiallowed, s.propertytreeid, s.labelpath");
                sql.append(" from storageunit s");
                sql.append(" where s.storageunitid = ?");
                ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageUnitId});
                if (ds != null && ds.size() > 0) {
                    String physicalstoreid = ds.getValue(0, "linkkeyid1");
                    props.setProperty("storageunitid", ds.getValue(0, "storageunitid"));
                    props.setProperty("linksdcid", ds.getValue(0, "linksdcid"));
                    props.setProperty("linkkeyid1", physicalstoreid);
                    props.setProperty("custodialdepartmentid", PhysicalStore.getCustodialDepartmentId(queryProcessor, physicalstoreid));
                    props.setProperty("maxtiallowed", ds.getValue(0, "maxtiallowed"));
                    props.setProperty("propertytreeid", ds.getValue(0, "propertytreeid"));
                    props.setProperty("labelpath", ds.getValue(0, "labelpath"));
                }
            } else {
                sql.append("select s.storageunitid, s.linksdcid, s.linkkeyid1, s.labelpath, t.custodialdepartmentid, t.custodialuserid, s.maxtiallowed, s.propertytreeid");
                sql.append(" from trackitem t, storageunit s");
                sql.append(" where t.linksdcid = s.linksdcid");
                sql.append(" and t.linkkeyid1 = s.linkkeyid1");
                sql.append(" and s.storageunitid = ?");
                ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{parent});
                if (ds != null && ds.size() > 0) {
                    props.setProperty("storageunitid", ds.getValue(0, "storageunitid"));
                    props.setProperty("linksdcid", ds.getValue(0, "linksdcid"));
                    props.setProperty("linkkeyid1", ds.getValue(0, "linkkeyid1"));
                    props.setProperty("labelpath", ds.getValue(0, "labelpath"));
                    props.setProperty("custodialdepartmentid", ds.getValue(0, "custodialdepartmentid"));
                    props.setProperty("custodialuserid", ds.getValue(0, "custodialuserid"));
                    props.setProperty("maxtiallowed", ds.getValue(0, "maxtiallowed"));
                    props.setProperty("propertytreeid", ds.getValue(0, "propertytreeid"));
                    props.setProperty("labelpath", ds.getValue(0, "labelpath"));
                }
            }
        }
        if (props.size() == 0) {
            sql = new StringBuilder();
            sql.append("select s.storageunitid, s.linksdcid, s.linkkeyid1, s.maxtiallowed, s.propertytreeid, s.labelpath");
            sql.append(" from storageunit s");
            sql.append(" where s.storageunitid = ?");
            ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageUnitId});
            if (ds != null && ds.size() > 0) {
                props.setProperty("storageunitid", ds.getValue(0, "storageunitid"));
                props.setProperty("linksdcid", ds.getValue(0, "linksdcid"));
                props.setProperty("linkkeyid1", ds.getValue(0, "linkkeyid1"));
                props.setProperty("maxtiallowed", ds.getValue(0, "maxtiallowed"));
                props.setProperty("propertytreeid", ds.getValue(0, "propertytreeid"));
                props.setProperty("labelpath", ds.getValue(0, "labelpath"));
            }
        }
        return props;
    }

    public static String getStorageUnitId(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        return OpalUtil.getColumnValue(queryProcessor, "storageunit", "storageunitid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, keyid1});
    }

    public static DataSet populateChild(QueryProcessor queryProcessor, String storageunitid, String sdcid, boolean isOra) {
        DataSet ds;
        StringBuilder sql = new StringBuilder();
        if (isOra) {
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select storageunitid, parentid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, 'N' selected, storageenvid, ancestorid");
            sql.append(" from storageunit");
            if (StringUtil.getLen(sdcid) > 0L) {
                sql.append(" where linksdcid = ").append(safeSQL.addVar(sdcid));
            }
            sql.append(" connect by prior storageunitid = parentid");
            sql.append(" start with storageunitid = ").append(safeSQL.addVar(storageunitid));
            ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            sql.append("select s.storageunitid, s.parentid, s.linksdcid, s.linkkeyid1, s.linkkeyid2, s.linkkeyid3, 'N' selected, storageenvid, ancestorid");
            sql.append(", (select count(child.storageunitid) from storageunit child where child.parentid = s.storageunitid) childcount");
            sql.append(" from storageunit s");
            sql.append(" where s.storageunitid = ?");
            ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                StorageUnitSDC.populateStorageUnitHeirarchy(queryProcessor, storageunitid, ds, isOra);
            }
            if (StringUtil.getLen(sdcid) > 0L) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("linksdcid", sdcid);
                ds = ds.getFilteredDataSet(map);
            }
        }
        return ds;
    }

    public static boolean isMoveable(QueryProcessor queryProcessor, String storageunitid) {
        return "Y".equals(OpalUtil.getColumnValue(queryProcessor, "storageunit", "moveableflag", "storageunitid = ?", new String[]{storageunitid}));
    }

    public static DataSet getAllTrackitemsInStorageUnitHeirarchy(QueryProcessor queryProcessor, String storageunitid, boolean oracle) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        if (oracle) {
            sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.custodialuserid, t.custodialdepartmentid,");
            sql.append(" t.currentstorageunitid, t.freezethawflag, t.freezethawcount, t.freezethawcountwarn, t.freezethawcountmax,");
            sql.append(" (select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid) labelpath,");
            sql.append(" (select su.storageunittype from storageunit su where su.storageunitid = t.currentstorageunitid) storageunittype,");
            sql.append(" (select su.propertytreeid from storageunit su where su.storageunitid = t.currentstorageunitid) propertytreeid,");
            sql.append(" (select su.storageunittype from storageunit su where su.storageunitid = (select p.parentid from storageunit p where p.storageunitid = t.currentstorageunitid)) parentstorageunittype,");
            sql.append(" (select su.propertytreeid from storageunit su where su.storageunitid = (select p.parentid from storageunit p where p.storageunitid = t.currentstorageunitid)) parentpropertytreeid,");
            sql.append(" s_sample.s_sampleid, s_sample.confirmedby, s_sample.storagestatus");
            sql.append(" from trackitem t left outer join s_sample on s_sample.s_sampleid = t.linkkeyid1 and t.linksdcid = 'Sample'");
            sql.append(" where t.currentstorageunitid in ( select s.storageunitid from storageunit s connect by prior s.storageunitid = s.parentid");
            sql.append(" start with s.storageunitid = ?)");
        } else {
            sql.append("WITH storageunittree (storageunitid, parentid, linksdcid, linkkeyid1)");
            sql.append(" AS");
            sql.append(" (");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
            sql.append("    FROM storageunit AS su");
            sql.append("    WHERE su.storageunitid = ?");
            sql.append("    UNION ALL");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
            sql.append("   FROM storageunit AS su");
            sql.append("    INNER JOIN StorageUnitTree AS d");
            sql.append("    ON su.parentid = d.storageunitid");
            sql.append(" )");
            sql.append(" select t.trackitemid, t.linksdcid, t.linkkeyid1, t.custodialuserid, t.custodialdepartmentid,");
            sql.append(" t.currentstorageunitid, t.freezethawflag, t.freezethawcount, t.freezethawcountwarn, t.freezethawcountmax,");
            sql.append(" (select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid) labelpath,");
            sql.append(" (select su.storageunittype from storageunit su where su.storageunitid = t.currentstorageunitid) storageunittype,");
            sql.append(" (select su.propertytreeid from storageunit su where su.storageunitid = t.currentstorageunitid) propertytreeid,");
            sql.append(" (select su.storageunittype from storageunit su where su.storageunitid = (select p.parentid from storageunit p where p.storageunitid = t.currentstorageunitid)) parentstorageunittype,");
            sql.append(" (select su.propertytreeid from storageunit su where su.storageunitid = (select p.parentid from storageunit p where p.storageunitid = t.currentstorageunitid)) parentpropertytreeid,");
            sql.append(" s_sample.s_sampleid, s_sample.confirmedby, s_sample.storagestatus");
            sql.append(" from trackitem t left outer join s_sample on s_sample.s_sampleid = t.linkkeyid1 and t.linksdcid = 'Sample'");
            sql.append(" where t.currentstorageunitid in (select storageunittree.storageunitid from storageunittree)");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        return ds == null ? new DataSet() : ds;
    }

    public static List getValidChildList(QueryProcessor queryProcessor, String storageunitid) throws SapphireException {
        return (List)StorageUnitSDC.getValidChildCollection(queryProcessor, storageunitid).get(storageunitid);
    }

    public static HashMap getValidChildCollection(QueryProcessor queryProcessor, String storageunitid) throws SapphireException {
        HashMap childMap = new HashMap();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select storageunit.storageunitid, storageunit.storageunittype, storageunit.propertytreeid, propertytree.valuetree, propertytree.definitiontree");
        sql.append(" from storageunit, propertytree ");
        sql.append(" where propertytree.propertytreeid = storageunit.propertytreeid");
        sql.append(" and storageunit.storageunitid in (").append(safeSQL.addIn(storageunitid, ";")).append(")");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String id = ds.getValue(i, "storageunitid");
                String storageunittype = ds.getValue(i, "storageunittype");
                String propertytreeid = ds.getValue(i, "propertytreeid");
                String xml = ds.getClob(i, "valuetree");
                String definitionTree = ds.getClob(i, "definitiontree");
                PropertyTree propertyTree = new PropertyTree(propertytreeid);
                propertyTree.setValueXML(xml);
                propertyTree.setDefinitionXML(definitionTree);
                PropertyList nodePropertyList = propertyTree.getNodePropertyList(storageunittype, true);
                PropertyListCollection childtypes = nodePropertyList.getCollectionNotNull("childrentypes");
                ArrayList<String> childList = new ArrayList<String>();
                for (int child = 0; child < childtypes.size(); ++child) {
                    childList.add(childtypes.getPropertyList(child).getProperty("type"));
                }
                childMap.put(id, childList);
            }
        }
        return childMap;
    }

    private static Integer getLastIndex(QueryProcessor queryProcessor, String parentStorageUnitId) {
        return queryProcessor.getPreparedSqlDataSet("select max(storageunitindex) as max from storageunit where parentid = ?", (Object[])new String[]{parentStorageUnitId}).getInt(0, "max", 0);
    }

    private static HashMap getParentInfo(QueryProcessor queryProcessor, String parentid) {
        HashMap<String, Object> parentInfo = new HashMap<String, Object>();
        DataSet sqlDataSet = queryProcessor.getPreparedSqlDataSet("select storageunittype, labelpath, propertytreeid, ancestorid, storageunitsize, storageenvid from storageunit where storageunitid = ?", (Object[])new String[]{parentid});
        parentInfo.put("storageunittype", sqlDataSet.getValue(0, "storageunittype"));
        parentInfo.put("labelpath", sqlDataSet.getValue(0, "labelpath"));
        parentInfo.put("propertytreeid", sqlDataSet.getValue(0, "propertytreeid"));
        parentInfo.put("ancestorid", sqlDataSet.getValue(0, "ancestorid", sqlDataSet.getString(0, "storageenvid", "").length() > 0 ? parentid : ""));
        parentInfo.put("storageunitsize", sqlDataSet.getInt(0, "storageunitsize", 0));
        return parentInfo;
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet restrictions = sdiData.getDataset("storagerestriction");
        if (restrictions != null && restrictions.getRowCount() > 0) {
            this.validateStorageRestrictions(restrictions);
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet restrictions = sdiData.getDataset("storagerestriction");
        if (restrictions != null && restrictions.getRowCount() > 0) {
            this.validateStorageRestrictions(restrictions);
        }
    }

    public static String getRootStorageUnitID(QueryProcessor queryProcessor, String storageunitid) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH StorageUnitTree (storageunitid, parentid)");
        sql.append(" AS (");
        sql.append("    SELECT storageunit.storageunitid, storageunit.parentid");
        sql.append("    FROM storageunit");
        sql.append("    WHERE storageunit.storageunitid = ?");
        sql.append("    UNION ALL");
        sql.append("    SELECT storageunit.storageunitid, storageunit.parentid");
        sql.append("   FROM storageunit");
        sql.append("    INNER JOIN StorageUnitTree");
        sql.append("    ON storageunit.storageunitid = StorageUnitTree.parentid");
        sql.append(" )");
        sql.append(" SELECT st.storageunitid FROM StorageUnitTree st");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            return ds.getString(ds.size() - 1, "storageunitid", "");
        }
        return "";
    }

    private void validateStorageRestrictions(DataSet restrictions) {
        ArrayList<String> errors = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();
        for (int row = 0; row < restrictions.size(); ++row) {
            String storageunitid = restrictions.getString(row, "storageunitid");
            DataSet ds = null;
            sql.setLength(0);
            if (this.getConnectionProcessor().isOra()) {
                sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.currentstorageunitid");
                sql.append("  from trackitem t");
                sql.append("  where t.currentstorageunitid in (");
                sql.append("    select s.storageunitid ");
                sql.append("    from storageunit s");
                sql.append("    connect by prior s.storageunitid = s.parentid");
                sql.append("    start with s.storageunitid = ?)");
                sql.append(" and not exists ( select su1.storageunitid from storageunit su1 where su1.linksdcid = t.linksdcid and su1.linkkeyid1 = t.linkkeyid1 )");
            } else {
                sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1)");
                sql.append(" AS");
                sql.append(" (");
                sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
                sql.append("    FROM storageunit AS su");
                sql.append("    WHERE su.storageunitid = ?");
                sql.append("    UNION ALL");
                sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
                sql.append("   FROM storageunit AS su");
                sql.append("    INNER JOIN StorageUnitTree AS d");
                sql.append("    ON su.parentid = d.storageunitid");
                sql.append(" )");
                sql.append("select t.trackitemid, t.linksdcid, t.linkkeyid1, t.currentstorageunitid  ");
                sql.append("  from trackitem t  ");
                sql.append("  where t.currentstorageunitid in ( ");
                sql.append(" SELECT storageunitid");
                sql.append(" FROM StorageUnitTree )");
                sql.append(" and not exists ( select su1.storageunitid from storageunit su1 where su1.linksdcid = t.linksdcid and su1.linkkeyid1 = t.linkkeyid1 )");
            }
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds == null || ds.size() <= 0) continue;
            ds.sort("currentstorageunitid");
            ArrayList<DataSet> list = ds.getGroupedDataSets("currentstorageunitid");
            if (list == null) continue;
            for (DataSet dataset : list) {
                String _storageunitid = dataset.getString(0, "currentstorageunitid");
                String _trackitemid = dataset.getColumnValues("trackitemid", ";");
                errors.addAll(StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), _storageunitid, _trackitemid, this.getConnectionProcessor().getSapphireConnection()));
            }
        }
        if (errors.size() > 0) {
            int index = 1;
            int maxErrorDisplayCount = 5;
            StringBuilder sb = new StringBuilder();
            sb.append("Existing items in Storage fails modified Storage Restriction");
            for (String s : errors) {
                s = StringUtil.replaceAll(s, "\"", "&quot;");
                sb.append("\\\n- ").append(s.startsWith("Storage Restrictions Failure: ") ? s.substring(30) : s);
                if (++index <= maxErrorDisplayCount) continue;
                break;
            }
            if (errors.size() > maxErrorDisplayCount) {
                sb.append("\\\n- ").append(errors.size() - maxErrorDisplayCount).append(" more errors...");
            }
            this.setError("Storage Restrictions", "VALIDATION", sb.toString());
        }
    }

    private boolean updateStorageUnitStats() throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        return !"Manual Only".equals(policy.getPropertyListNotNull("storageexplorer").getProperty("refreshstatistics"));
    }

    public static boolean isFreezeThawCandidate(QueryProcessor queryProcessor, boolean isORA, String storageunitid) {
        boolean freezeThawCandidate = false;
        String sql = "";
        sql = isORA ? "select storageunitid, (select se.freezethawcandidateflag from storageenv se where se.storageenvid = storageunit.storageenvid) freezethawcandidate from storageunit connect by prior parentid=storageunitid start with storageunitid = ?" : "WITH StorageUnitTree (storageunitid, parentid) AS (  SELECT su.storageunitid, su.parentid  FROM storageunit AS su  WHERE su.storageunitid = ?  UNION ALL  SELECT su.storageunitid, su.parentid  FROM storageunit AS su  INNER JOIN StorageUnitTree AS d  ON d.parentid = su.storageunitid ) select storageunit.storageunitid,  (select se.freezethawcandidateflag from storageenv se where se.storageenvid = storageunit.storageenvid) freezethawcandidate from storageunit where storageunit.storageunitid in (select storageunittree.storageunitid from storageunittree)";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if (!"Y".equals(ds.getString(i, "freezethawcandidate", ""))) continue;
                freezeThawCandidate = true;
                break;
            }
        }
        return freezeThawCandidate;
    }
}

