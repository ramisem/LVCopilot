/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageExporter;
import com.labvantage.opal.util.StorageImporter;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class PhysicalStore
extends BaseSDCRules {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 85844 $";
    public static final String SDC_PHYSICALSTORE = "PhysicalStore";

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "departmentid") || !"D".equals(this.getSDCProcessor().getProperty(SDC_PHYSICALSTORE, "accesscontrolledflag"))) continue;
            if (!"Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
                throw new SapphireException(this.getTranslationProcessor().translate("Confirm Department Update"), "CONFIRM", this.getTranslationProcessor().translate("Updating department will update the department on all contents of this Physical Store. Are you sure?"));
            }
            primary.setString(i, "securitydepartment", primary.getString(i, "departmentid", ""));
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean forceUpdate = actionProps.getProperty("__sdcruleconfirm").equals("Y");
        for (int i = 0; i < primary.size(); ++i) {
            PropertyList props;
            if (!this.hasPrimaryValueChanged(primary, i, "departmentid")) continue;
            String physicalStoreId = primary.getValue(i, "s_physicalstoreid");
            String departmentid = primary.getString(i, "departmentid", "");
            String storageunitid = StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), SDC_PHYSICALSTORE, physicalStoreId);
            DataSet trackitemds = StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), storageunitid, this.connectionInfo.isOracle());
            if (trackitemds != null && trackitemds.size() > 0) {
                props = new PropertyList();
                props.setProperty("trackitemid", trackitemds.getColumnValues("trackitemid", ";"));
                props.setProperty("custodialdepartmentid", departmentid);
                props.setProperty("__sdcruleignore", "Y");
                props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "");
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            if (!"D".equals(this.getSDCProcessor().getProperty("StorageUnitSDC", "accesscontrolledflag"))) continue;
            props = new PropertyList();
            props.setProperty("sdcid", "StorageUnitSDC");
            props.setProperty("keyid1", storageunitid);
            props.setProperty("securitydepartment", departmentid);
            props.setProperty("__sdcruleignore", "Y");
            props.setProperty("__securitydepartmentedit", "Y");
            props.setProperty("auditreason", "Department changed on Physical Store");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkGLPRule(primary, forceUpdate);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.storageunitid");
        sql.append(" from storageunit t");
        sql.append(" where t.linksdcid = '").append(SDC_PHYSICALSTORE).append("'");
        sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "StorageUnitSDC");
            props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
            props.setProperty("_deletelinksdi", "N");
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }
    }

    private void checkGLPRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        GLPRule rule = new GLPRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "glpflag") || !"N".equals(primary.getValue(i, "glpflag"))) continue;
            rule.processPhysicalStoreGLPRule(primary.getString(i, "s_physicalstoreid"), forceUpdate);
        }
    }

    public static boolean isGLP(DBAccess database, String physicalstoreid) throws SapphireException {
        String sql = "SELECT count(s_physicalstore.s_physicalstoreid) FROM s_physicalstore left outer join department on department.departmentid = s_physicalstore.departmentid WHERE s_physicalstore.s_physicalstoreid=? AND s_physicalstore.glpflag='Y' and department.glpflag = 'Y'";
        return database.getPreparedCount(sql, new Object[]{physicalstoreid}) > 0;
    }

    protected List getItemsList(String physicalStoreid, String sdcid) {
        List<String> items = new ArrayList();
        if (this.connectionInfo.isOracle()) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT LINKKEYID1 ");
            sql.append(" FROM STORAGEUNIT ");
            sql.append(" WHERE LINKSDCID = ").append(safeSQL.addVar(sdcid));
            sql.append(" CONNECT BY PRIOR STORAGEUNITID = PARENTID ");
            sql.append(" START WITH LINKSDCID = 'PhysicalStore' ");
            sql.append(" AND LINKKEYID1 = ").append(safeSQL.addVar(physicalStoreid)).append(" ");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    items.add(ds.getValue(i, "linkkeyid1"));
                }
            }
        } else {
            items = this.getItems(physicalStoreid, sdcid);
        }
        return items;
    }

    protected List getItems(String physicaltoreid, String sdcid) {
        ArrayList<String> list = new ArrayList<String>();
        if (StringUtil.getLen(physicaltoreid) > 0L) {
            String storageunitid = StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), SDC_PHYSICALSTORE, physicaltoreid);
            DataSet child = StorageUnitSDC.populateChild(this.getQueryProcessor(), storageunitid, sdcid, this.connectionInfo.isOracle());
            if (child != null) {
                for (int i = 0; i < child.size(); ++i) {
                    list.add(child.getValue(i, "linkkeyid1"));
                }
            }
        }
        return list;
    }

    public static String getCustodialDepartmentId(DBAccess database, String physicalstoreid) throws SapphireException {
        String cd = "";
        String sql = "SELECT departmentid FROM s_physicalstore WHERE s_physicalstoreid=?";
        database.createPreparedResultSet(sql, new Object[]{physicalstoreid});
        if (database.getNext()) {
            cd = database.getString("departmentid");
        }
        database.closeResultSet();
        return cd;
    }

    public static String getCustodialDepartmentId(QueryProcessor queryProcessor, String physicalstoreid) {
        return OpalUtil.getColumnValue(queryProcessor, "s_physicalstore", "departmentid", "s_physicalstoreid=?", new String[]{physicalstoreid});
    }

    public static boolean isTemporaryStorage(QueryProcessor qp, String physicalstoreid) {
        String storageclass;
        return StringUtil.getLen(physicalstoreid) > 0L && "Temporary".equals(storageclass = OpalUtil.getColumnValue(qp, "s_physicalstore", "storageclass", "s_physicalstoreid=?", new String[]{physicalstoreid}));
    }

    public static boolean isRepository(QueryProcessor qp, String physicalstoreid) {
        String custodialdepartmentid;
        return StringUtil.getLen(physicalstoreid) > 0L && StringUtil.getLen(custodialdepartmentid = PhysicalStore.getCustodialDepartmentId(qp, physicalstoreid)) > 0L && Department.isRepository(qp, custodialdepartmentid);
    }

    @Override
    public void postGenerateSnapshot(Snapshot snapshot, boolean isPackaging) throws SapphireException {
        SDISnapshotItem snapshotItem = (SDISnapshotItem)snapshot.getSnapshotItem();
        SDISnapshot primarySnapshot = (SDISnapshot)snapshot;
        if (snapshotItem != null) {
            DataSet primary = snapshotItem.getSDIData().getDataset("primary");
            String physicalstoreid = snapshotItem.getKeyId1();
            String storageunitid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linksdcid='PhysicalStore' and linkkeyid1=?", new String[]{physicalstoreid});
            boolean withoutBoxes = snapshotItem.getPolicyNodeId().startsWith("WithoutBoxes");
            if (!isPackaging) {
                StorageExporter storageExporter = new StorageExporter(storageunitid, 0, this.getQueryProcessor(), this.getSDCProcessor(), this.getDAMProcessor(), this.getConnectionInfo().isOracle(), withoutBoxes);
                primary.addColumn("__exportxml", 3);
                primary.addColumn("__viewxml", 3);
                primary.setString(0, "__exportxml", storageExporter.getExportXML());
                primary.setString(0, "__viewxml", storageExporter.getViewXML());
            } else {
                StorageExporter storageExporter = new StorageExporter(storageunitid, this.getQueryProcessor(), this.getSDCProcessor(), this.getDAMProcessor(), this.getConnectionInfo().isOracle(), withoutBoxes);
                primary.addColumn("__exportxml", 3);
                primary.addColumn("__viewxml", 3);
                primary.setString(0, "__exportxml", storageExporter.getExportXML());
                primary.setString(0, "__viewxml", storageExporter.getViewXML());
                DataSet referenceDataSet = storageExporter.getReferenceDataSet();
                if (OpalUtil.isNotEmpty(referenceDataSet)) {
                    for (int i = 0; i < referenceDataSet.size(); ++i) {
                        String sdcid = referenceDataSet.getString(i, "sdcid", "");
                        String keyid1 = referenceDataSet.getString(i, "keyid1", "");
                        String keyid2 = referenceDataSet.getString(i, "keyid2", null);
                        String keyid3 = referenceDataSet.getString(i, "keyid3", null);
                        if (sdcid.length() <= 0 || keyid1.length() <= 0) continue;
                        if ("PropertyTree".equals(sdcid)) {
                            SnapshotFactory snapshotUtil = new SnapshotFactory(this.getConnectionId(), this.getRakFile());
                            PropertyTreeSnapshot propertyTreeSnapshot = snapshotUtil.generatePropertyTreeSnapshot(keyid1, keyid2, true, false, true);
                            snapshotItem.addLink(propertyTreeSnapshot.getSnapshotItem(), SnapshotItem.LinkType.SQL, "PropertyTree", null);
                            propertyTreeSnapshot.getSnapshotItem().setIncludedForTransfer(true);
                            primarySnapshot.addTransferSnapshot(propertyTreeSnapshot);
                            continue;
                        }
                        PropertyList parentLinkProps = new PropertyList();
                        parentLinkProps.setProperty("importbeforeprimary", "N");
                        PropertyList transferOptions = new PropertyList();
                        parentLinkProps.setProperty("transferoption", transferOptions);
                        transferOptions.setProperty("importoption", "Override Existing");
                        SnapshotFactory snapshotUtil = new SnapshotFactory(this.getConnectionId(), this.getRakFile());
                        SDISnapshot sdiSnapshot = snapshotUtil.generateSDISnapshot(sdcid, keyid1, keyid2, keyid3);
                        snapshotItem.addLink(sdiSnapshot.getSnapshotItem(), SnapshotItem.LinkType.SQL, sdcid, parentLinkProps);
                        primarySnapshot.addTransferSnapshot(sdiSnapshot);
                    }
                }
            }
        }
    }

    @Override
    public void postCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null && primary.size() > 0) {
            PropertyList props = new PropertyList();
            for (int i = 0; i < primary.size(); ++i) {
                StorageImporter storageImporter;
                DataSet dataSet;
                String physicalstoreid = primary.getString(i, "s_physicalstoreid");
                String exportxml = primary.getValue(i, "__exportxml");
                if (exportxml.length() <= 0 || !OpalUtil.isNotEmpty(dataSet = (storageImporter = new StorageImporter(exportxml, this.getSequenceProcessor())).getImportDataSet())) continue;
                DataSet containerDatSet = storageImporter.getContainerDataSet();
                if (containerDatSet.size() > 0) {
                    containerDatSet.sort("sdcid");
                    ArrayList<DataSet> list = containerDatSet.getGroupedDataSets("sdcid");
                    for (DataSet ds : list) {
                        String sdcid = ds.getString(0, "sdcid");
                        props.clear();
                        props.setProperty("sdcid", sdcid);
                        props.setProperty("copies", String.valueOf(ds.size()));
                        props.setProperty("auditreason", "Physical Store import");
                        props.setProperty("currentstoragelocation", ds.getColumnValues("parentid", ";"));
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String columnid = ds.getColumnId(col);
                            if (!columnid.startsWith("link_")) continue;
                            props.setProperty(columnid.substring(5), ds.getColumnValues(columnid, ";"));
                        }
                        props.setProperty("__cmtimportflag", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        String containerid = props.getProperty("newkeyid1");
                        ds.addColumnValues("newkeyid1", 0, containerid, ";");
                        for (int row = 0; row < ds.size(); ++row) {
                            String storageunitid = ds.getString(row, "storageunitid");
                            String newkeyid1 = ds.getString(row, "newkeyid1");
                            int rowindex = dataSet.findRow("storageunitid", storageunitid);
                            if (rowindex == -1) continue;
                            dataSet.setString(rowindex, "linksdcid", sdcid);
                            dataSet.setString(rowindex, "linkkeyid1", newkeyid1);
                            dataSet.setString(rowindex, "storageunitlabel", newkeyid1);
                        }
                    }
                }
                dataSet.setString(0, "linksdcid", SDC_PHYSICALSTORE);
                dataSet.setString(0, "linkkeyid1", physicalstoreid);
                for (int row = 0; row < dataSet.size(); ++row) {
                    String parentid = dataSet.getString(row, "parentid", "");
                    if (parentid.length() == 0) {
                        dataSet.setString(row, "labelpath", "/" + dataSet.getString(row, "storageunitlabel"));
                        continue;
                    }
                    int parentRowIndex = dataSet.findRow("storageunitid", parentid);
                    if (parentRowIndex == -1) continue;
                    String parentlabelpath = dataSet.getString(parentRowIndex, "labelpath");
                    dataSet.setString(row, "labelpath", parentlabelpath + "/" + dataSet.getString(row, "storageunitlabel"));
                    dataSet.setString(row, "ancestorid", dataSet.getString(parentRowIndex, "storageenvid", "").length() > 0 ? dataSet.getString(parentRowIndex, "storageunitid") : dataSet.getString(parentRowIndex, "ancestorid", ""));
                }
                props.clear();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("overrideautokey", "Y");
                for (int col = 0; col < dataSet.getColumnCount(); ++col) {
                    String columnid = dataSet.getColumnId(col);
                    if (columnid.startsWith("link_")) continue;
                    props.setProperty(columnid, dataSet.getColumnValues(columnid, ";"));
                }
                props.setProperty("__cmtimportflag", "Y");
                props.setProperty("auditreason", "Physical Store import");
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                DataSet restrictionsDataSet = storageImporter.getRestrictionsDataSet();
                if (!OpalUtil.isNotEmpty(restrictionsDataSet)) continue;
                props.clear();
                restrictionsDataSet.setString(-1, "activeflag", "Y");
                restrictionsDataSet.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                restrictionsDataSet.setString(-1, "createby", this.getConnectionInfo().getSysuserId());
                restrictionsDataSet.setString(-1, "createtool", "CMTImport");
                restrictionsDataSet.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                restrictionsDataSet.setString(-1, "modby", this.getConnectionInfo().getSysuserId());
                restrictionsDataSet.setString(-1, "modtool", "CMTImport");
                DataSetUtil.insert(this.database, restrictionsDataSet, "storagerestriction");
            }
        }
    }
}

