/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotGenerator;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.StringLogger;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class SnapshotFactory
extends BaseCustom
implements Cloneable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private static final String LOGNAME = "SnapshotUtil";
    private HashMap<SnapshotItem, SDIData> sdiDataCache = new HashMap();
    Set<SDISnapshotItem> allRetrievedSnapshotItems = new HashSet<SDISnapshotItem>();
    Set<SDISnapshotItem> allReqPackageItems = new HashSet<SDISnapshotItem>();
    private int recursionLevel = 0;
    StringLogger stringLogger = new StringLogger();
    StringLogger stringLoggerRuntime = new StringLogger();
    private String policyValueTreeXML = "";
    private String policyDefTreeXML = "";
    private SDISnapshot basedOnSDISnapshot = null;
    private boolean isGenerateSnapshotLinkTree = false;
    private boolean isVerbose = false;
    private boolean isIndentLog = true;
    private boolean isDevMode = false;
    private String compCode = "";
    private static PropertyList devModeCols = null;
    private static String devModeCol = "devmodecol";
    private static String customModeCol = "custommodecol";
    private static String componentModeCol = "compmodecol";
    private static HashMap<String, SnapshotGenerator> generatorThreads = new HashMap();
    private SnapshotGenerator currGenerator;

    private void init() {
        DataSet ds = this.getQueryProcessor().getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'devmode' AND propertyvalue = 'Y'");
        this.isDevMode = ds != null && ds.getRowCount() > 0;
        ds = this.getQueryProcessor().getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid='compcode'");
        if (ds != null && ds.getRowCount() > 0) {
            String compCode;
            this.compCode = compCode = ds.getString(0, "propertyvalue", "");
        }
    }

    public SnapshotFactory(String connectionId) throws SapphireException {
        this(connectionId, null);
    }

    public SnapshotFactory(String connectionId, File rakFile) throws SapphireException {
        if (rakFile != null) {
            this.setRakFile(rakFile);
        }
        this.setConnectionId(connectionId);
        this.logger.setLoggerName(LOGNAME);
        this.init();
    }

    public boolean isVerbose() {
        return this.isVerbose;
    }

    public void setVerbose(boolean verbose) {
        this.isVerbose = verbose;
    }

    public boolean isIndentLog() {
        return this.isIndentLog;
    }

    public void setIndentLog(boolean indentLog) {
        this.isIndentLog = indentLog;
    }

    public void setPolicyValueTreeXML(String policyValueTreeXML, String policyDefTreeXML) {
        this.policyValueTreeXML = policyValueTreeXML;
        this.policyDefTreeXML = policyDefTreeXML;
    }

    private CMTPolicy getConfigPolicy(String sdcId, String nodeId) {
        CMTPolicy policyObj = this.basedOnSDISnapshot == null ? CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdcId, nodeId, this.policyValueTreeXML, this.policyDefTreeXML) : CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdcId, nodeId, this.basedOnSDISnapshot.getPolicyNodeMap());
        return policyObj;
    }

    public SDISnapshot generateSDISnapshot(String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        return this.generateSDISnapshot(sdcId, keyId1, keyId2, keyId3, sdcId);
    }

    public SDISnapshot generateSDISnapshot(String sdcId, String keyId1, String keyId2, String keyId3, SDISnapshotItem basedOnSDISnapshotItem) throws SapphireException {
        this.basedOnSDISnapshot = basedOnSDISnapshotItem.getSnapshot();
        SDISnapshot sdiSnapshot = this.generateSDISnapshot(sdcId, keyId1, keyId2, keyId3, basedOnSDISnapshotItem.getPolicyNodeId());
        this.basedOnSDISnapshot = null;
        return sdiSnapshot;
    }

    public SDISnapshot generateSDISnapshot(String sdcId, String keyId1, String keyId2, String keyId3, String policyNodeId) throws SapphireException {
        return this.generateSDISnapshots(sdcId, keyId1, keyId2, keyId3, policyNodeId, false, false).get(0);
    }

    public List<SDISnapshot> generateSDISnapshots(String sdcIdList, String keyId1List, String keyId2List, String keyId3List) throws SapphireException {
        return this.generateSDISnapshots(sdcIdList, keyId1List, keyId2List, keyId3List, sdcIdList);
    }

    public List<SDISnapshot> generateSDISnapshots(String sdcIdList, String keyId1List, String keyId2List, String keyId3List, String policyNodeIdList) throws SapphireException {
        return this.generateSDISnapshots(sdcIdList, keyId1List, keyId2List, keyId3List, policyNodeIdList, false, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List<SDISnapshot> generateSDISnapshots(String sdcIdList, String keyId1List, String keyId2List, String keyId3List, String policyNodeIdList, boolean isFullExport, boolean isPackaging) throws SapphireException {
        ArrayList<SDISnapshot> snapshotList = new ArrayList<SDISnapshot>();
        try {
            DataSet requestedItems = new DataSet();
            requestedItems.addColumnValues("sdcid", 0, sdcIdList, ";");
            requestedItems.addColumnValues("keyid1", 0, keyId1List, ";");
            requestedItems.addColumnValues("keyid2", 0, keyId2List, ";");
            requestedItems.addColumnValues("keyid3", 0, keyId3List, ";");
            requestedItems.addColumnValues("policynodeid", 0, policyNodeIdList, ";");
            requestedItems.padColumn("sdcid");
            requestedItems.padColumn("policynodeid");
            this.sdiDataCache.clear();
            this.recursionLevel = 0;
            for (int i = 0; i < requestedItems.getRowCount(); ++i) {
                String sdcId = requestedItems.getString(i, "sdcid", "");
                String keyId1 = requestedItems.getString(i, "keyid1", "");
                String keyId2 = requestedItems.getString(i, "keyid2", "");
                String keyId3 = requestedItems.getString(i, "keyid3", "");
                if ("(null)".equalsIgnoreCase(keyId2)) {
                    keyId2 = "";
                }
                if ("(null)".equalsIgnoreCase(keyId3)) {
                    keyId3 = "";
                }
                SDISnapshotItem requestedSnapshotItem = new SDISnapshotItem(sdcId, keyId1, keyId2, keyId3, requestedItems.getString(i, "policynodeid", ""));
                this.allRetrievedSnapshotItems.clear();
                long startTime = System.currentTimeMillis();
                SDISnapshot sdiSnapshot = this.buildSDISnapshot(requestedSnapshotItem, isPackaging, isFullExport);
                long endTime = System.currentTimeMillis();
                this.logger.info("Time taken to Generate Snapshot (ms): " + (endTime - startTime));
                SDISnapshotItem sdiSnapshotItem = sdiSnapshot.getSnapshotItem();
                if (!sdiSnapshotItem.isLoadedSuccessfully()) {
                    throw new SapphireException("SnapshotGenerationError", "FAILURE", sdiSnapshotItem.getStatusMessage());
                }
                PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcId);
                DBUtil dbAccess = null;
                try {
                    dbAccess = (DBUtil)CMTUtil.getDBAccess(this.getConnectionId(), this.getRakFile());
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(dbAccess.getConnection(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionid())), new ErrorHandler(), sdcId, sdcProps, "PostGenerateSnapshot");
                    if (this.getRakFile() != null) {
                        sdcPreRules.setRakFile(this.getRakFile());
                    }
                    sdcPreRules.postGenerateSnapshot(sdiSnapshot, isPackaging);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.postGenerateSnapshot(sdiSnapshot, isPackaging);
                    }
                    sdcPreRules.endRule();
                }
                finally {
                    if (dbAccess != null) {
                        dbAccess.releaseConnection();
                        dbAccess.reset();
                    }
                }
                snapshotList.add(sdiSnapshot);
            }
            this.sdiDataCache.clear();
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return snapshotList;
    }

    public void generateSnapshotLinkTree(Snapshot snapshot) throws SapphireException {
        SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
        this.recursionLevel = 0;
        this.isGenerateSnapshotLinkTree = true;
        if (Snapshot.Type.SDI.equals((Object)snapshot.getType())) {
            this.gatherSDISnapshotLinkData(sdiSnapshot, sdiSnapshot.getSnapshotItem(), true, true);
        }
        this.isGenerateSnapshotLinkTree = false;
    }

    public PropertyTreeSnapshot generatePropertyTreeSnapshot(String propertyTreeId, String propertyTreeNodeId) throws SapphireException {
        PropertyTreeSnapshot snapshot = null;
        this.sdiDataCache.clear();
        snapshot = propertyTreeNodeId == null || propertyTreeNodeId.length() == 0 || "__FULL".equals(propertyTreeNodeId) ? this.generatePropertyTreeSnapshot(propertyTreeId, "__FULL", false, false, false) : ("__DEFINITION".equals(propertyTreeNodeId) ? this.generatePropertyTreeSnapshot(propertyTreeId, "__DEFINITION", false, false, false) : this.generatePropertyTreeSnapshot(propertyTreeId, propertyTreeNodeId, false, false, false));
        return snapshot;
    }

    public PropertyTreeSnapshot generatePropertyTreeSnapshot(String propertyTreeId, String nodeId, boolean includeAncestorNodes, boolean includeDefinition, boolean overwriteNode) throws SapphireException {
        this.log("generatePropertyTreeSnapshot", "Start generating Snapshot for PropertyTree: " + propertyTreeId + ", Node: " + nodeId, false);
        PropertyTreeSnapshot snapshot = null;
        PropertyTreeSnapshotItem snapshotItem = new PropertyTreeSnapshotItem();
        try {
            snapshotItem.setPropertyTreeId(propertyTreeId);
            SDIData sdiData = null;
            if (this.sdiDataCache.containsKey(snapshotItem)) {
                this.log("generatePropertyTreeSnapshot", "PropertyTree SDIData found in Cache. Reusing.", true);
                sdiData = this.sdiDataCache.get(snapshotItem);
            } else {
                this.log("generatePropertyTreeSnapshot", "Retrieving PropertyTree SDIData.", true);
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("PropertyTree");
                sdiRequest.setKeyid1List(propertyTreeId);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("category");
                sdiRequest.setRequestItem("role");
                sdiRequest.setExtendedDataTypes(true);
                sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                this.sdiDataCache.put(snapshotItem, sdiData);
            }
            snapshot = new PropertyTreeSnapshot(snapshotItem);
            snapshot.addSDIData(snapshotItem, sdiData);
            DataSet primary = sdiData.getDataset("primary");
            if (this.isDevMode) {
                this.log("generatePropertyTreeSnapshot", "Set Dev Mode = Y", true);
                snapshot.setDevMode(true);
            }
            if (this.compCode.length() > 0) {
                this.log("generatePropertyTreeSnapshot", "Component Code = " + this.compCode, true);
                snapshot.setCompCode(this.compCode);
            }
            if ("__FULL".equals(nodeId)) {
                snapshotItem.setNodeId("__FULL");
            } else if ("__DEFINITION".equals(nodeId)) {
                primary.removeColumn("valuetree");
                this.log("generatePropertyTreeSnapshot", "Definition Mode. Ommitting the Value Tree.", true);
                snapshotItem.setNodeId("__DEFINITION");
            } else {
                this.log("generatePropertyTreeSnapshot", "Node Mode. Extracting existing Value Tree.", true);
                PropertyTree loadedPropertyTree = new PropertyTree(propertyTreeId);
                snapshotItem.setNodeId(nodeId);
                loadedPropertyTree.setValueXML(primary.getClob(0, "valuetree"));
                primary.removeColumn("valuetree");
                primary.removeColumn("definitiontree");
                PropertyTree pTree = new PropertyTree(snapshot.getKeyId1());
                snapshot.setPropertyTree(pTree);
                if ("__root".equals(nodeId)) {
                    this.log("gatherPropertyTreeSnapshotNodeData", "Extracting Root Node.", true);
                    PropertyDefaultList propertyDefaultList = loadedPropertyTree.getPropertyDefaultList();
                    pTree.setPropertyDefaultList(propertyDefaultList);
                } else {
                    Node node = loadedPropertyTree.getNode(nodeId);
                    if (node == null) {
                        throw new SapphireException("InvalidNodeId", "FAILURE", "No Node found with this name: " + nodeId);
                    }
                    node.setNodeList(null);
                    Node parent = SnapshotFactory.getNonComponentParentNode(node);
                    node.setExtendsNodeId(parent != null ? parent.getNodeId() : "root");
                    NodeList nodeList1 = new NodeList();
                    nodeList1.add(node);
                    pTree.setNodeList(nodeList1);
                    snapshot.setExtendsNodeId(node.getExtendsNodeId());
                    if (includeAncestorNodes) {
                        this.log("generatePropertyTreeSnapshot", "Include Ancestor Nodes Heirarchy: Yes.", false);
                        this.setNodeImportAttributes(snapshot, overwriteNode);
                        if (includeDefinition) {
                            this.log("gatherPropertyTreeSnapshotNodeData", "Including Definition.", true);
                            ++this.recursionLevel;
                            PropertyTreeSnapshot defSnapshot = this.generatePropertyTreeSnapshot(snapshot.getKeyId1(), "__DEFINITION", false, false, false);
                            --this.recursionLevel;
                            this.setNodeImportAttributes(defSnapshot, overwriteNode);
                            defSnapshot.getSnapshotItem().setIncludedForTransfer(true);
                            snapshot.getSnapshotItem().addLink(defSnapshot.getSnapshotItem(), SnapshotItem.LinkType.SQL, "Definition_Node", null);
                            snapshot.addTransferSnapshot(defSnapshot);
                        }
                        this.log("gatherPropertyTreeSnapshotNodeData", "Including Root node.", true);
                        ++this.recursionLevel;
                        PropertyTreeSnapshot rootSnapshot = this.generatePropertyTreeSnapshot(snapshot.getKeyId1(), "__root", false, false, false);
                        --this.recursionLevel;
                        this.setNodeImportAttributes(rootSnapshot, overwriteNode);
                        rootSnapshot.getSnapshotItem().setIncludedForTransfer(true);
                        snapshot.getSnapshotItem().addLink(rootSnapshot.getSnapshotItem(), SnapshotItem.LinkType.SQL, "Ancestor_Root", null);
                        snapshot.addTransferSnapshot(rootSnapshot);
                        this.log("generatePropertyTreeSnapshot", "Including all Ancestor nodes. If any.", true);
                        NodeList ancestors = loadedPropertyTree.getNodeAncestorList(nodeId);
                        for (int i = 0; i < ancestors.size(); ++i) {
                            Node ancestor = (Node)ancestors.get(i);
                            if (ancestor.isProduct() && !this.isDevMode || ancestor.getCompCode().length() > 0 && this.compCode.length() == 0) continue;
                            ++this.recursionLevel;
                            PropertyTreeSnapshot ancestorSnapshot = this.generatePropertyTreeSnapshot(snapshot.getKeyId1(), ancestor.getNodeId(), false, false, false);
                            --this.recursionLevel;
                            this.setNodeImportAttributes(ancestorSnapshot, overwriteNode);
                            ancestorSnapshot.getSnapshotItem().setIncludedForTransfer(true);
                            snapshot.getSnapshotItem().addLink(ancestorSnapshot.getSnapshotItem(), SnapshotItem.LinkType.SQL, "Ancestor_Node", null);
                            snapshot.addTransferSnapshot(ancestorSnapshot);
                        }
                    }
                }
            }
            snapshotItem.setLoadedSuccessfully(true);
        }
        catch (SapphireException e) {
            if ("INFORMATION".equals(e.getErrorType())) {
                this.log("generatePropertyTreeSnapshot", snapshotItem, e.getMessage(), false);
            } else {
                this.log("generatePropertyTreeSnapshot", snapshotItem, e.getErrorType() + ": " + e.getMessage(), false);
            }
            snapshotItem.setLoadedSuccessfully(false);
            snapshotItem.setStatusMessage(e.getMessage());
        }
        this.log("generatePropertyTreeSnapshot", "Snapshot generation done.", true);
        return snapshot;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SnapshotPackage packageSnapshot(String sdcIdList, String keyId1List, String keyId2List, String keyId3List, String policyNodeIdList, boolean isFullExport) throws SapphireException {
        this.log("packageSnapshot", "Started", false);
        try {
            DataSet requestedItems = new DataSet();
            requestedItems.addColumnValues("sdcid", 0, sdcIdList, ";");
            requestedItems.addColumnValues("keyid1", 0, keyId1List, ";");
            requestedItems.addColumnValues("keyid2", 0, keyId2List, ";");
            requestedItems.addColumnValues("keyid3", 0, keyId3List, ";");
            requestedItems.addColumnValues("policynodeid", 0, policyNodeIdList, ";");
            requestedItems.padColumn("sdcid");
            requestedItems.padColumn("policynodeid");
            SnapshotPackage snapshotPackage = new SnapshotPackage(false);
            this.allReqPackageItems.clear();
            LinkedHashSet<SDISnapshotItem> reqSnapshotItems = new LinkedHashSet<SDISnapshotItem>();
            for (int i = 0; i < requestedItems.getRowCount(); ++i) {
                SDISnapshotItem reqSnapshotItem;
                String sdcId = requestedItems.getString(i, "sdcid", "");
                String keyId1 = requestedItems.getString(i, "keyid1", "");
                String keyId2 = requestedItems.getString(i, "keyid2", "");
                String keyId3 = requestedItems.getString(i, "keyid3", "");
                if ("(null)".equalsIgnoreCase(keyId2)) {
                    keyId2 = "";
                }
                if ("(null)".equalsIgnoreCase(keyId3)) {
                    keyId3 = "";
                }
                if ((reqSnapshotItem = new SDISnapshotItem(sdcId, keyId1, keyId2, keyId3, requestedItems.getString(i, "policynodeid", ""))).isExistsDifferentPolicy(reqSnapshotItems)) {
                    throw new SapphireException("Same SDI is being extracted using different Policy Node. Process terminated: " + reqSnapshotItem.toString(true, true));
                }
                reqSnapshotItems.add(reqSnapshotItem);
            }
            this.sdiDataCache.clear();
            this.recursionLevel = -1;
            ArrayList<SDISnapshot> sdiSnapshots = new ArrayList<SDISnapshot>();
            HashSet<SDISnapshotItem> generatedSnapshotItems = new HashSet<SDISnapshotItem>();
            for (SDISnapshotItem requestedSnapshotItem : reqSnapshotItems) {
                this.allRetrievedSnapshotItems.clear();
                long startTime = System.currentTimeMillis();
                SDISnapshot sdiSnapshot = this.buildSDISnapshot(requestedSnapshotItem, true, isFullExport);
                long endTime = System.currentTimeMillis();
                this.logger.info("Time taken to Generate Snapshot (ms): " + (endTime - startTime));
                SDISnapshotItem sdiSnapshotItem = sdiSnapshot.getSnapshotItem();
                if (!sdiSnapshotItem.isLoadedSuccessfully()) {
                    throw new SapphireException("SnapshotPackagingError", "FAILURE", sdiSnapshotItem.getStatusMessage());
                }
                PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdiSnapshot.getSDCId());
                DBUtil dbAccess = null;
                try {
                    dbAccess = (DBUtil)CMTUtil.getDBAccess(this.getConnectionId(), this.getRakFile());
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(dbAccess.getConnection(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionid())), new ErrorHandler(), sdiSnapshot.getSDCId(), sdcProps, "PostGenerateSnapshot");
                    if (this.getRakFile() != null) {
                        sdcPreRules.setRakFile(this.getRakFile());
                    }
                    sdcPreRules.postGenerateSnapshot(sdiSnapshot, true);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.postGenerateSnapshot(sdiSnapshot, true);
                    }
                    sdcPreRules.endRule();
                }
                finally {
                    if (dbAccess != null) {
                        dbAccess.releaseConnection();
                        dbAccess.reset();
                    }
                }
                sdiSnapshots.add(sdiSnapshot);
                generatedSnapshotItems.add(requestedSnapshotItem);
                snapshotPackage.addSnapshot(sdiSnapshot.getSnapshotItem(), sdiSnapshot, true);
            }
            this.sdiDataCache.clear();
            ArrayDeque<SDISnapshot> transferSnapshots = new ArrayDeque<SDISnapshot>();
            for (SDISnapshot snapshot : sdiSnapshots) {
                for (SDISnapshot sdiSnapshot : snapshot.getTransferSnapshots()) {
                    transferSnapshots.addLast(sdiSnapshot);
                    while (!transferSnapshots.isEmpty()) {
                        SDISnapshot transferSnapshot = (SDISnapshot)transferSnapshots.removeFirst();
                        transferSnapshot.getSnapshotItem().setParent(null);
                        transferSnapshot.getSnapshotItem().setContainer(transferSnapshot);
                        if (!transferSnapshot.getSnapshotItem().isExists(generatedSnapshotItems, true)) {
                            snapshotPackage.addSnapshot(transferSnapshot.getSnapshotItem(), transferSnapshot, false);
                        }
                        for (SDISnapshot transferTransferSnapshot : transferSnapshot.getTransferSnapshots()) {
                            transferSnapshots.addFirst(transferTransferSnapshot);
                        }
                    }
                }
            }
            return snapshotPackage;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public SnapshotGenerator packageSnapshotAsync(final String sdcIdList, final String keyId1List, final String keyId2List, final String keyId3List, final String policyNodeIdList, final boolean isfull) throws SapphireException {
        this.clearOldGenerators();
        final SnapshotGenerator run = this.createNewGenerator();
        Thread runThread = new Thread(run.id){

            @Override
            public void run() {
                try {
                    run.status = SnapshotGenerator.Status.INPROGRESS;
                    run.snapshotPackage = SnapshotFactory.this.packageSnapshot(sdcIdList, keyId1List, keyId2List, keyId3List, policyNodeIdList, isfull);
                    run.status = SnapshotGenerator.Status.COMPLETE;
                }
                catch (SapphireException e) {
                    run.status = SnapshotGenerator.Status.ERROR;
                    run.statusMessage = e.getMessage();
                    SnapshotFactory.this.logger.error("Exception occurred in initiating Async Snapshot Generator", e);
                }
            }
        };
        runThread.start();
        return run;
    }

    public SnapshotPackage packagePropertyTreeSnapshot(String propertyTreeId, String nodeIds) throws SapphireException {
        Snapshot snapshot;
        DataSet requested = new DataSet();
        requested.addColumnValues("propertytreeid", 0, propertyTreeId, ";");
        requested.addColumnValues("nodeid", 0, nodeIds, ";");
        requested.padColumn("propertytreeid");
        String sql = "SELECT DISTINCT categoryid, sdcid FROM categoryitem WHERE sdcid = 'PropertyTree' AND keyid1 = ?";
        SnapshotPackage snapshotPackage = new SnapshotPackage(false);
        PropertyList categoryLinkProps = null;
        for (int i = 0; i < requested.getRowCount(); ++i) {
            DataSet categories;
            String reqestedNodeId = requested.getString(i, "nodeid", "__FULL");
            snapshot = this.generatePropertyTreeSnapshot(requested.getString(i, "propertytreeid"), reqestedNodeId);
            PropertyTreeSnapshotItem snapshotItem = ((PropertyTreeSnapshot)snapshot).getSnapshotItem();
            if ("__FULL".equals(reqestedNodeId) && (categories = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{requested.getString(i, "propertytreeid")})).getRowCount() > 0) {
                if (categoryLinkProps == null) {
                    CMTPolicy cmtPolicy = this.getConfigPolicy("PropertyTree", "");
                    PropertyListCollection sdixxxFKCollection = cmtPolicy.getSDIDetailFKLinksProps("categoryitem", true);
                    categoryLinkProps = sdixxxFKCollection.find("linkid_fk", "Category;category");
                }
                List<SDISnapshot> categorySnapshots = this.generateSDISnapshots("Category", categories.getColumnValues("categoryid", ";"), categories.getColumnValues("sdcid", ";"), "", "Category Custom", true, true);
                int catCount = 0;
                for (SDISnapshot categorySnapshot : categorySnapshots) {
                    SDISnapshotItem categorySnapshotItem = categorySnapshot.getSnapshotItem();
                    categorySnapshotItem.setIncludedForTransfer(true);
                    snapshotItem.addLink(categorySnapshotItem, SnapshotItem.LinkType.SQL, "Category;" + ++catCount, categoryLinkProps);
                    ((SDISnapshot)snapshot).addTransferSnapshot(categorySnapshot);
                }
            }
            snapshotPackage.addSnapshot(((PropertyTreeSnapshot)snapshot).getSnapshotItem(), snapshot, true);
        }
        ArrayDeque<SDISnapshot> transferSnapshots = new ArrayDeque<SDISnapshot>();
        Iterator<Snapshot> iterator = snapshotPackage.getRequestedSnapshots().iterator();
        while (iterator.hasNext()) {
            Snapshot pSnapshot = snapshot = iterator.next();
            for (SDISnapshot pTransferSnapshot : ((SDISnapshot)pSnapshot).getTransferSnapshots()) {
                transferSnapshots.addLast(pTransferSnapshot);
                while (!transferSnapshots.isEmpty()) {
                    SDISnapshot transferSnapshot = (SDISnapshot)transferSnapshots.removeFirst();
                    transferSnapshot.getSnapshotItem().setParent(null);
                    transferSnapshot.getSnapshotItem().setContainer(transferSnapshot);
                    snapshotPackage.addSnapshot(transferSnapshot.getSnapshotItem(), transferSnapshot, false);
                    for (SDISnapshot transferTransferSnapshot : transferSnapshot.getTransferSnapshots()) {
                        transferSnapshots.addFirst(transferTransferSnapshot);
                    }
                }
            }
        }
        return snapshotPackage;
    }

    public SnapshotGenerator packagePropertyTreeSnapshotAsync(final String propertyTreeId, final String nodeIds) throws SapphireException {
        this.clearOldGenerators();
        final SnapshotGenerator run = this.createNewGenerator();
        Thread runThread = new Thread(run.id){

            @Override
            public void run() {
                try {
                    run.status = SnapshotGenerator.Status.INPROGRESS;
                    run.snapshotPackage = SnapshotFactory.this.packagePropertyTreeSnapshot(propertyTreeId, nodeIds);
                    run.status = SnapshotGenerator.Status.COMPLETE;
                }
                catch (SapphireException e) {
                    run.status = SnapshotGenerator.Status.ERROR;
                    run.statusMessage = e.getMessage();
                    SnapshotFactory.this.logger.error("Exception occurred in initiating Async Snapshot Generator", e);
                }
            }
        };
        runThread.start();
        return run;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SnapshotPackage packageFromChangeLog(String requestedChangeLogIdList, String preChangeLogIdList) throws SapphireException {
        SnapshotPackage snapshotPackage;
        block27: {
            this.recursionLevel = 0;
            this.log("packageFromChangeLog", "Start generating Snapshot Package from ChangeLog.", false);
            this.log("packageFromChangeLog", "Requested Change Log Ids: " + requestedChangeLogIdList, false);
            this.log("packageFromChangeLog", "Pre Image Change Log Ids: " + preChangeLogIdList, false);
            snapshotPackage = new SnapshotPackage(true);
            if (requestedChangeLogIdList == null || requestedChangeLogIdList.length() == 0) {
                throw new SapphireException("Atleast one Requested Change Log Id is required.");
            }
            String rsetId = null;
            this.log("packageFromChangeLog", "Start", false);
            this.log("", "Snapshot from Change Log Id:" + requestedChangeLogIdList, false);
            this.log("", "Pre Image from Change Log Id:" + requestedChangeLogIdList, false);
            try {
                this.log("packageFromChangeLog", "Generating RSet for all Change Log Ids.", false);
                rsetId = this.getDAMProcessor().createRSet("LV_ChangeLog", requestedChangeLogIdList + (preChangeLogIdList != null && preChangeLogIdList.length() > 0 ? ";" + preChangeLogIdList : ""), "", "");
                this.log("packageFromChangeLog", "RSet Id: " + rsetId, false);
                String sql = "SELECT c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.propertytreenodeid, primarychangelogid, (SELECT c1.propertytreenodeid FROM changelog c1 WHERE c1.changelogid = c.primarychangelogid AND c.changelogstatus = 'Renamed' ) renamednodeid, c.changelogstatus, c.originalsnapshot, c.modifiedsnapshot FROM changelog c, rsetitems r WHERE c.changelogid = r.keyid1 AND r.sdcid = 'LV_ChangeLog' AND r.rsetid = ?";
                DataSet changeLogInfo = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetId}, true);
                this.log("packageFromChangeLog", "Retrieved Change Log info for all the provided Change Log Ids.", false);
                for (int i = 0; i < changeLogInfo.getRowCount(); ++i) {
                    SDIData linkSDIData;
                    SnapshotItem linkItem;
                    ArrayDeque<SnapshotItem> queue;
                    List<SnapshotItem> linkItems;
                    String changeLogId = changeLogInfo.getString(i, "changelogid");
                    String linkSDCid = changeLogInfo.getString(i, "linksdcid");
                    String linkKeyId1 = changeLogInfo.getString(i, "linkkeyid1");
                    String linkKeyId2 = changeLogInfo.getString(i, "linkkeyid2");
                    String linkKeyId3 = changeLogInfo.getString(i, "linkkeyid3");
                    String changeLogStatus = changeLogInfo.getString(i, "changelogstatus", "");
                    this.log("packageFromChangeLog", "Processing Change Log Id: " + changeLogId, false);
                    if ("(null)".equalsIgnoreCase(linkKeyId2)) {
                        linkKeyId2 = "";
                    }
                    if ("(null)".equalsIgnoreCase(linkKeyId3)) {
                        linkKeyId3 = "";
                    }
                    SDISnapshotItem snapshotItem = null;
                    if ("PropertyTree".equals(linkSDCid)) {
                        this.log("packageFromChangeLog", "Generating SnapshotItem for PropertyTree: " + linkKeyId1, false);
                        String nodeId = changeLogInfo.getString(i, "propertytreenodeid", "");
                        PropertyTreeSnapshotItem pSnapshotItem = new PropertyTreeSnapshotItem();
                        pSnapshotItem.setPropertyTreeId(linkKeyId1);
                        if (nodeId.length() == 0 || "(null)".equalsIgnoreCase(nodeId) || "__FULL".equals(nodeId)) {
                            pSnapshotItem.setNodeId("__FULL");
                        } else if ("__DEFINITION".equals(nodeId)) {
                            pSnapshotItem.setNodeId("__DEFINITION");
                        } else {
                            pSnapshotItem.setNodeId(nodeId);
                        }
                        snapshotItem = pSnapshotItem;
                    } else {
                        this.log("packageFromChangeLog", "Generating SnapshotItem for SDI: " + linkKeyId1 + ", " + linkKeyId2 + ", " + linkKeyId3, false);
                        CMTPolicy policy = this.getConfigPolicy(linkSDCid, linkSDCid);
                        snapshotItem = new SDISnapshotItem(linkSDCid, linkKeyId1, linkKeyId2, linkKeyId3, policy.getActualPolicyNodeId());
                    }
                    boolean isPreSnapshotNull = false;
                    boolean isPostSnapshotNull = false;
                    if ((";" + preChangeLogIdList + ";").indexOf(";" + changeLogId + ";") > -1) {
                        this.log("packageFromChangeLog", "Pre-Snapshot requested. Including Original Snapshot from Changelog.", false);
                        Snapshot preSnapshot = null;
                        String preSnapshotStr = changeLogInfo.getClob(i, "originalsnapshot", "");
                        if (preSnapshotStr.length() > 0) {
                            preSnapshot = Snapshot.fromXML(preSnapshotStr, this.getConnectionId(), this.getRakFile());
                            this.log("packageFromChangeLog", "Remove excluded table/column from snapshot: " + preSnapshot.getSnapshotItem(), true);
                            this.excludeSpecifiedColumns(preSnapshot.getSDIData(), CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), preSnapshot.getSnapshotItem().getSDCId(), ((SDISnapshotItem)preSnapshot.getSnapshotItem()).getPolicyNodeId()), true);
                            if (Snapshot.Type.SDI.equals((Object)preSnapshot.getType())) {
                                linkItems = ((SDISnapshot)preSnapshot).getSnapshotItem().getLinkItems();
                                queue = new ArrayDeque<SnapshotItem>(linkItems);
                                while (queue.size() > 0) {
                                    linkItem = queue.pollFirst();
                                    if (!Snapshot.Type.SDI.equals((Object)linkItem.getType())) continue;
                                    linkSDIData = linkItem.getSDIData();
                                    if (linkSDIData != null) {
                                        this.log("packageFromChangeLog", "Remove excluded table/column from embedded snapshot: " + linkItem, true);
                                        this.excludeSpecifiedColumns(linkSDIData, CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionId(), linkItem.getSDCId(), linkItem.getPolicyNodeId()), true);
                                    }
                                    linkItem.getLinkItems().forEach(queue::addLast);
                                }
                            }
                            ((SDISnapshotItem)preSnapshot.getSnapshotItem()).setDeleted("Deleted".equals(changeLogStatus));
                            snapshotPackage.addPreSnapshot(preSnapshot.getSnapshotItem(), preSnapshot);
                        } else {
                            this.log("packageFromChangeLog", "No Pre-Snapshot found.", false);
                            ((SDISnapshotItem)snapshotItem).setDeleted("Deleted".equals(changeLogStatus));
                            snapshotPackage.addPreSnapshot(snapshotItem, null);
                            isPreSnapshotNull = true;
                        }
                    }
                    if ((";" + requestedChangeLogIdList + ";").indexOf(";" + changeLogId + ";") > -1) {
                        this.log("packageFromChangeLog", "Post-Snapshot requested. Including Modified Snapshot from Changelog.", false);
                        Snapshot requestedSnapshot = null;
                        String requestedSnapshotStr = changeLogInfo.getClob(i, "modifiedsnapshot", "");
                        if (requestedSnapshotStr.length() > 0) {
                            requestedSnapshot = Snapshot.fromXML(requestedSnapshotStr, this.getConnectionId(), this.getRakFile());
                            this.log("packageFromChangeLog", "Remove excluded table/column from snapshot: " + requestedSnapshot.getSnapshotItem(), true);
                            this.excludeSpecifiedColumns(requestedSnapshot.getSDIData(), CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), requestedSnapshot.getSnapshotItem().getSDCId(), ((SDISnapshotItem)requestedSnapshot.getSnapshotItem()).getPolicyNodeId()), true);
                            if (Snapshot.Type.SDI.equals((Object)requestedSnapshot.getType())) {
                                linkItems = ((SDISnapshot)requestedSnapshot).getSnapshotItem().getLinkItems();
                                queue = new ArrayDeque<SnapshotItem>(linkItems);
                                while (queue.size() > 0) {
                                    linkItem = queue.pollFirst();
                                    if (!Snapshot.Type.SDI.equals((Object)linkItem.getType())) continue;
                                    linkSDIData = linkItem.getSDIData();
                                    if (linkSDIData != null) {
                                        this.log("packageFromChangeLog", "Remove excluded table/column from embedded snapshot: " + linkItem, true);
                                        this.excludeSpecifiedColumns(linkSDIData, CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionId(), linkItem.getSDCId(), linkItem.getPolicyNodeId()), true);
                                    }
                                    linkItem.getLinkItems().forEach(queue::addLast);
                                }
                            }
                            ((SDISnapshotItem)requestedSnapshot.getSnapshotItem()).setDeleted("Deleted".equals(changeLogStatus));
                            snapshotPackage.addSnapshot(requestedSnapshot.getSnapshotItem(), requestedSnapshot, true);
                        } else {
                            this.log("packageFromChangeLog", "No Post-Snapshot found.", false);
                            ((SDISnapshotItem)snapshotItem).setDeleted("Deleted".equals(changeLogStatus));
                            if (Snapshot.Type.PROPERTYTREE.equals((Object)snapshotItem.getType()) && "Renamed".equals(changeLogStatus)) {
                                PropertyTreeSnapshotItem psi = (PropertyTreeSnapshotItem)snapshotItem;
                                psi.setRenamed(true);
                                String renamedNodeId = changeLogInfo.getString(i, "renamednodeid", "");
                                if (renamedNodeId.length() == 0) {
                                    throw new SapphireException("RenamedNodeError", "VALIDATION", "No Link ChangeLog found for Rename operation: " + changeLogId);
                                }
                                psi.setRenamedNodeId(renamedNodeId);
                            }
                            snapshotPackage.addSnapshot(snapshotItem, null, true);
                            isPostSnapshotNull = true;
                        }
                    }
                    if (!isPreSnapshotNull || !isPostSnapshotNull) continue;
                    throw new SapphireException("NULLSnapshot", "FAILURE", "Pre & Post Snapshot undefined for item: " + ((SDISnapshotItem)snapshotItem).toString(true, true));
                }
                if (rsetId == null) break block27;
                this.getDAMProcessor().clearRSet(rsetId);
            }
            catch (Throwable throwable) {
                if (rsetId != null) {
                    this.getDAMProcessor().clearRSet(rsetId);
                }
                throw throwable;
            }
        }
        this.log("packageFromChangeLog", "End", false);
        return snapshotPackage;
    }

    public SnapshotGenerator packageFromChangeLogAsync(final String requestedChangeLogIdList, final String preChangeLogIdList) throws SapphireException {
        this.clearOldGenerators();
        final SnapshotGenerator run = this.createNewGenerator();
        Thread runThread = new Thread(run.id){

            @Override
            public void run() {
                try {
                    run.status = SnapshotGenerator.Status.INPROGRESS;
                    run.snapshotPackage = SnapshotFactory.this.packageFromChangeLog(requestedChangeLogIdList, preChangeLogIdList);
                    run.status = SnapshotGenerator.Status.COMPLETE;
                }
                catch (SapphireException e) {
                    run.status = SnapshotGenerator.Status.ERROR;
                    run.statusMessage = e.getMessage();
                    SnapshotFactory.this.logger.error("Exception occurred in initiating Async Snapshot Generator", e);
                }
            }
        };
        runThread.start();
        return run;
    }

    private SDISnapshot buildSDISnapshot(SDISnapshotItem reqSnapshotItem, boolean isPackaging, boolean isFullExport) throws SapphireException {
        if (!isPackaging && isFullExport) {
            String message = "Full Export only allowed when packaging.";
            throw new SapphireException("SDC_SDI_Check", "INFORMATION", message);
        }
        ++this.recursionLevel;
        this.log("buildSDISnapshot", reqSnapshotItem.toString(true, true) + ": Start generating Snapshot. isPackaging: " + isPackaging + ". isFullExport: " + isFullExport, true);
        SDISnapshot sdiSnapshot = new SDISnapshot(reqSnapshotItem);
        this.gatherSDISnapshotData(sdiSnapshot, reqSnapshotItem, isPackaging, isFullExport);
        --this.recursionLevel;
        return sdiSnapshot;
    }

    private void gatherSDISnapshotData(SDISnapshot sdiSnapshot, SDISnapshotItem reqSDISnapshotItem, boolean isPackaging, boolean isFullExport) throws SapphireException {
        ++this.recursionLevel;
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Start gathering Data for: " + reqSDISnapshotItem.toString(true, true), false);
        try {
            SDIData primarySDIData;
            if ("SDC".equals(reqSDISnapshotItem.getSDCId()) && "SDC".equals(reqSDISnapshotItem.getKeyId1())) {
                String message = "The SDC SDI of SDC SDC is not allowed to be checked out.";
                throw new SapphireException("SDC_SDI_Check", "INFORMATION", message);
            }
            if (reqSDISnapshotItem.isExists(this.allRetrievedSnapshotItems, false)) {
                String message = "Extracted already. Skip.";
                throw new SapphireException("AlreadyExtractedCheck", "INFORMATION", message);
            }
            if (this.recursionLevel > 1 && isPackaging && reqSDISnapshotItem.isExists(this.allReqPackageItems, true)) {
                String message = ": This SDI is also a requested item for this Package. Skipped.";
                throw new SapphireException("RequestedItemCheck", "INFORMATION", message);
            }
            CMTPolicy configPolicy = this.getConfigPolicy(reqSDISnapshotItem.getSDCId(), reqSDISnapshotItem.getPolicyNodeId());
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Requested Policy Node: " + configPolicy.getRequestedPolicyNodeId() + ". Actual Policy Node: " + configPolicy.getActualPolicyNodeId(), true);
            reqSDISnapshotItem.setPolicyNodeId(configPolicy.getActualPolicyNodeId());
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PropertyList sdcProps = sdcProcessor.getProperties(reqSDISnapshotItem.getSDCId());
            String versionedFlag = sdcProps.getProperty("versionedflag");
            if ("Y".equals(versionedFlag) && (reqSDISnapshotItem.getKeyId2() == null || reqSDISnapshotItem.getKeyId2().length() == 0 || "C".equalsIgnoreCase(reqSDISnapshotItem.getKeyId2()))) {
                SdiInfo sdiInfo = new SdiInfo();
                sdiInfo.setConnectionId(this.getConnectionId());
                sdiInfo.setRakFile(this.getRakFile());
                String keyId2 = sdiInfo.getCurrentVersion(reqSDISnapshotItem.getSDCId(), reqSDISnapshotItem.getKeyId1(), reqSDISnapshotItem.getKeyId3());
                if (keyId2 == null || keyId2.length() == 0) {
                    throw new SapphireException("VersionIdCheck", "INFORMATION", "Unable to determine Version id.");
                }
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Versioned SDC. Key Id2 (" + sdcProps.getProperty("keycolid2") + ") is '" + reqSDISnapshotItem.getKeyId2() + "'. New KeyId2 value: " + keyId2, true);
                reqSDISnapshotItem.setKeyId2(keyId2);
                reqSDISnapshotItem.setCurrentLink(true);
            }
            if (this.sdiDataCache.containsKey(reqSDISnapshotItem)) {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Found SDIData in Cache. Re-using.", true);
                primarySDIData = this.sdiDataCache.get(reqSDISnapshotItem);
            } else {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No cache found. Retrieving fresh SDIData.", true);
                SDIRequest primarySDIRequest = configPolicy.getSDIRequest(isPackaging);
                primarySDIRequest.setKeyid1List(reqSDISnapshotItem.getKeyId1());
                primarySDIRequest.setKeyid2List(reqSDISnapshotItem.getKeyId2());
                primarySDIRequest.setKeyid3List(reqSDISnapshotItem.getKeyId3());
                primarySDIData = this.getSDIProcessor().getSDIData(primarySDIRequest);
                this.handleDevModeColumns(primarySDIData, sdcProps.getProperty("tableid"), sdcProps.getProperty("keycolid1"));
                this.collectCertificationData(primarySDIRequest, primarySDIData, configPolicy, sdcProps);
                this.excludeSpecifiedColumns(primarySDIData, configPolicy, isPackaging);
                this.sdiDataCache.put(reqSDISnapshotItem, primarySDIData);
            }
            this.allRetrievedSnapshotItems.add(reqSDISnapshotItem);
            if (primarySDIData == null) {
                throw new SapphireException("SDIDataFailure", "FAILURE", "Exception occurred when trying to retrieve SDIData." + reqSDISnapshotItem);
            }
            DataSet primary = primarySDIData.getDataset("primary");
            if (primary.getRowCount() == 0) {
                String message = "No Rows found.";
                throw new SapphireException("NoRowsFound", "INFORMATION", message);
            }
            if ("Sapphire Custom".equals(reqSDISnapshotItem.getPolicyNodeId())) {
                sdiSnapshot.addPolicyProps("Sapphire Custom;" + reqSDISnapshotItem.getSDCId(), configPolicy.getPolicyPropertyList());
            } else {
                sdiSnapshot.addPolicyProps(reqSDISnapshotItem.getPolicyNodeId(), configPolicy.getPolicyPropertyList());
            }
            sdiSnapshot.addPolicyProps("Sapphire Custom", configPolicy.getDefaultPolicyPropertyList());
            sdiSnapshot.addSDIData(reqSDISnapshotItem, primarySDIData);
            this.gatherSDISnapshotLinkData(sdiSnapshot, reqSDISnapshotItem, isPackaging, isFullExport);
            reqSDISnapshotItem.setLoadedSuccessfully(true);
        }
        catch (SapphireException e) {
            if ("INFORMATION".equals(e.getErrorType())) {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, e.getMessage(), false);
            } else {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, e.getErrorType() + ": " + e.getMessage(), false);
            }
            reqSDISnapshotItem.setLoadedSuccessfully(false);
            reqSDISnapshotItem.setStatusMessage(e.getMessage());
        }
        catch (Exception e) {
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Exception: " + e.getMessage(), false);
            reqSDISnapshotItem.setLoadedSuccessfully(false);
            reqSDISnapshotItem.setStatusMessage(e.getMessage());
        }
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "End gathering Data for: " + reqSDISnapshotItem.toString(true, true), false);
        --this.recursionLevel;
    }

    private void gatherSDISnapshotLinkData(SDISnapshot sdiSnapshot, SDISnapshotItem reqSDISnapshotItem, boolean isPackaging, boolean isFullExport) throws SapphireException {
        int i;
        PropertyList primarySDCProps = this.getSDCProcessor().getProperties(reqSDISnapshotItem.getSDCId());
        int primarySDCKeyColCount = Integer.parseInt(primarySDCProps.getProperty("keycolumns"));
        CMTPolicy cmtPolicy = this.getConfigPolicy(reqSDISnapshotItem.getSDCId(), reqSDISnapshotItem.getPolicyNodeId());
        SDIData primarySDIData = sdiSnapshot.getSDIData(reqSDISnapshotItem);
        DataSet primary = primarySDIData.getDataset("primary");
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for FK Links.", true);
        PropertyListCollection fkCollection = cmtPolicy.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.FK);
        for (i = 0; i < fkCollection.size(); ++i) {
            String linkKeyId3;
            PropertyList fkLinkPolicyProps = fkCollection.getPropertyList(i);
            boolean isIncludedForTransfer = cmtPolicy.isIncludedForTransfer(fkLinkPolicyProps);
            if (this.isGenerateSnapshotLinkTree && !isIncludedForTransfer) continue;
            String fkLinkInfo = CMTPolicy.getAssociatedSDILinkInfo(fkLinkPolicyProps);
            String fkLinkSDCid = CMTPolicy.getAssociatedSDILinkSDCId(fkLinkPolicyProps);
            String fkLinkId = CMTPolicy.getAssociatedSDILinkLinkId(fkLinkPolicyProps);
            PropertyList fkLinkSDCLinkProps = new PropertyList(this.getSDCProcessor().getLinkProperties(reqSDISnapshotItem.getSDCId(), fkLinkId));
            String linkColId1 = fkLinkSDCLinkProps.getProperty("sdccolumnid");
            String linkColId2 = fkLinkSDCLinkProps.getProperty("sdccolumnid2");
            String linkColId3 = fkLinkSDCLinkProps.getProperty("sdccolumnid3");
            String linkKeyId1 = primary.getString(0, linkColId1, "");
            String linkKeyId2 = linkColId2.length() > 0 ? primary.getString(0, linkColId2, "") : "";
            String string = linkKeyId3 = linkColId3.length() > 0 ? primary.getString(0, linkColId3, "") : "";
            if (linkKeyId1.length() <= 0) continue;
            if ("PropertyTree".equals(fkLinkSDCid) && isFullExport) {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "PropertyTree FK Link found to: " + linkKeyId1, true);
                PropertyList propertyTreeOptions = fkLinkPolicyProps.getPropertyListNotNull("propertytreeprops");
                boolean isExportNodeHeirarchy = "Y".equals(propertyTreeOptions.getProperty("exportnodeancestors", "N"));
                if (!isExportNodeHeirarchy) continue;
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Export PropertyTree Node Hierarchy.", true);
                boolean isExpDefinition = "Y".equals(propertyTreeOptions.getProperty("exportdefinition", "N"));
                String extendNodeColId = propertyTreeOptions.getProperty("extendnodecolid");
                if (extendNodeColId.length() == 0) {
                    throw new SapphireException("InvalidPolicySettings", "INFORMATION", "Invalid Policy setting. Extend Node Col Id field is mandatory for PropertyTree FK Links.");
                }
                String extendNodeId = primary.getString(0, extendNodeColId, "");
                if (extendNodeId.length() <= 0) continue;
                if (this.isGenerateSnapshotLinkTree) {
                    PropertyTreeSnapshotItem propertyTreeSnapshotItem = new PropertyTreeSnapshotItem();
                    propertyTreeSnapshotItem.setPropertyTreeId(linkKeyId1);
                    propertyTreeSnapshotItem.setNodeId(extendNodeId);
                    propertyTreeSnapshotItem.setIncludedForTransfer(true);
                    propertyTreeSnapshotItem.setLoadedSuccessfully(true);
                    reqSDISnapshotItem.addLink(propertyTreeSnapshotItem, SnapshotItem.LinkType.FK, fkLinkInfo, null);
                    continue;
                }
                String existsAction = propertyTreeOptions.getProperty("nodeimportaction", "ignore");
                ++this.recursionLevel;
                PropertyTreeSnapshot propertyTreeSnapshot = this.generatePropertyTreeSnapshot(linkKeyId1, extendNodeId, true, isExpDefinition, "replace".equals(existsAction));
                --this.recursionLevel;
                PropertyTreeSnapshotItem propertyTreeSnapshotItem = propertyTreeSnapshot.getSnapshotItem();
                propertyTreeSnapshotItem.setIncludedForTransfer(true);
                reqSDISnapshotItem.addLink(propertyTreeSnapshotItem, SnapshotItem.LinkType.FK, fkLinkInfo, null);
                if (!propertyTreeSnapshotItem.isLoadedSuccessfully()) continue;
                sdiSnapshot.addTransferSnapshot(propertyTreeSnapshot);
                continue;
            }
            String fkPolicyNodeId = fkLinkPolicyProps.getProperty("refpolicynodeid");
            SDISnapshotItem fkSnapshotItem = new SDISnapshotItem(fkLinkSDCid, linkKeyId1, linkKeyId2, linkKeyId3, fkPolicyNodeId);
            reqSDISnapshotItem.addLink(fkSnapshotItem, SnapshotItem.LinkType.FK, fkLinkInfo, fkLinkPolicyProps);
            if (isIncludedForTransfer && isPackaging) {
                fkSnapshotItem.setIncludedForTransfer(true);
                if (this.isGenerateSnapshotLinkTree) {
                    fkSnapshotItem.setLoadedSuccessfully(true);
                    continue;
                }
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "FK Link found: " + fkSnapshotItem.toString(true, true), true);
                SDISnapshot fkSDISnapshot = this.buildSDISnapshot(fkSnapshotItem, isPackaging, isFullExport);
                if (!fkSnapshotItem.isLoadedSuccessfully()) continue;
                sdiSnapshot.addTransferSnapshot(fkSDISnapshot);
                continue;
            }
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Embedded FK Link found: " + fkSnapshotItem.toString(true, true), true);
            this.gatherSDISnapshotData(sdiSnapshot, fkSnapshotItem, isPackaging, isFullExport);
        }
        if (isFullExport) {
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for detail FK Links.", true);
            ArrayList<String> detailTables = cmtPolicy.getDetailDataSetList(true);
            for (String detailTableId : detailTables) {
                int i2;
                DataSet detailTableData = primarySDIData.getDataset(detailTableId);
                if (detailTableData == null || detailTableData.getRowCount() == 0) continue;
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Detail table: " + detailTableId, true);
                DataSet linksData = this.getSDCProcessor().getLinksData(reqSDISnapshotItem.getSDCId());
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("linktableid", detailTableId);
                findMap.put("linktype", "D");
                String linkId = "";
                String detailLinkId = "";
                int findRow = linksData.findRow(findMap);
                if (findRow == -1) {
                    findMap.clear();
                    findMap.put("linktableid", detailTableId);
                    findMap.put("linktype", "M");
                    findRow = linksData.findRow(findMap);
                    if (findRow == -1) {
                        PropertyListCollection detailLinksColl = this.getSDCProcessor().getDetailLinks(reqSDISnapshotItem.getSDCId());
                        for (i2 = 0; i2 < detailLinksColl.size(); ++i2) {
                            PropertyList detailLinkProps = detailLinksColl.getPropertyList(i2);
                            if (!"D".equals(detailLinkProps.getProperty("linktype")) || !detailLinkProps.getProperty("linktableid").equalsIgnoreCase(detailTableId)) continue;
                            linkId = detailLinkProps.getProperty("linkid");
                            detailLinkId = detailLinkProps.getProperty("detaillinkid");
                            break;
                        }
                    } else {
                        linkId = linksData.getString(findRow, "linkid", "");
                    }
                } else {
                    linkId = linksData.getString(findRow, "linkid", "");
                }
                if (linkId.length() <= 0) continue;
                PropertyListCollection detailFKLinks = cmtPolicy.getDetailFKLinksProps(detailTableId, true);
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No. of cofigured FK Links: " + detailFKLinks.size(), true);
                for (i2 = 0; i2 < detailFKLinks.size(); ++i2) {
                    PropertyList detailFKLinkPolicyProps = detailFKLinks.getPropertyList(i2);
                    String fkPolicyNodeId = detailFKLinkPolicyProps.getProperty("refpolicynodeid");
                    String detailFKLinkInfo = CMTPolicy.getDetailFKLinkInfo(detailFKLinkPolicyProps);
                    String detailFKLinkSDCId = CMTPolicy.getDetailFKLinkSDCId(detailFKLinkPolicyProps);
                    String detailFKLinkId = CMTPolicy.getDetailFKLinkLinkId(detailFKLinkPolicyProps);
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Link: " + detailFKLinkInfo, true);
                    PropertyList detailFKLinkSDCProps = this.getSDCProcessor().getProperties(detailFKLinkSDCId);
                    PropertyList detailFKLinkSDCLinkProps = new PropertyList(this.getSDCProcessor().getDetailLinkProperties(reqSDISnapshotItem.getSDCId(), linkId + ";" + detailFKLinkId));
                    if (detailFKLinkSDCLinkProps == null || detailFKLinkSDCLinkProps.size() <= 0) continue;
                    int fkKeyColCount = Integer.parseInt(detailFKLinkSDCProps.getProperty("keycolumns"));
                    String linkColId1 = detailFKLinkSDCLinkProps.getProperty("sdccolumnid");
                    String linkColId2 = detailFKLinkSDCLinkProps.getProperty("sdccolumnid2");
                    String linkColId3 = detailFKLinkSDCLinkProps.getProperty("sdccolumnid3");
                    for (int j = 0; j < detailTableData.getRowCount(); ++j) {
                        String linkColId3Value;
                        String linkColId1Value = detailTableData.getString(j, linkColId1, "");
                        String linkColId2Value = fkKeyColCount > 1 ? detailTableData.getString(j, linkColId2, "") : "";
                        String string = linkColId3Value = fkKeyColCount > 2 ? detailTableData.getString(j, linkColId3, "") : "";
                        if (linkColId1Value.length() <= 0) continue;
                        if ("PropertyTree".equals(detailFKLinkSDCId) && isFullExport) {
                            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "PropertyTree FK Link found to: " + linkColId1Value, true);
                            PropertyList propertyTreeOptions = detailFKLinkPolicyProps.getPropertyListNotNull("propertytreeprops");
                            boolean isExportNodeHeirarchy = "Y".equals(propertyTreeOptions.getProperty("exportnodeancestors", "N"));
                            if (!isExportNodeHeirarchy) continue;
                            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Export PropertyTree Node Hierarchy.", true);
                            boolean isExportDefinition = "Y".equals(propertyTreeOptions.getProperty("exportdefinition", "N"));
                            String extendNodeColId = propertyTreeOptions.getProperty("extendnodecolid");
                            if (extendNodeColId.length() == 0) {
                                throw new SapphireException("InvalidPolicySettings", "INFORMATION", "Invalid Policy setting. Extend Node Col Id field is mandatory for PropertyTree FK Links.");
                            }
                            String extendNodeId = detailTableData.getString(j, extendNodeColId, "");
                            if (extendNodeId.length() <= 0) continue;
                            if (this.isGenerateSnapshotLinkTree) {
                                PropertyTreeSnapshotItem propertyTreeSnapshotItem = new PropertyTreeSnapshotItem();
                                propertyTreeSnapshotItem.setPropertyTreeId(linkColId1Value);
                                propertyTreeSnapshotItem.setNodeId(extendNodeId);
                                propertyTreeSnapshotItem.setIncludedForTransfer(true);
                                propertyTreeSnapshotItem.setLoadedSuccessfully(true);
                                reqSDISnapshotItem.addLink(propertyTreeSnapshotItem, SnapshotItem.LinkType.FK, linkId + ";" + detailFKLinkId + ";" + j, null);
                                continue;
                            }
                            String existsAction = propertyTreeOptions.getProperty("nodeimportaction", "ignore");
                            ++this.recursionLevel;
                            PropertyTreeSnapshot propertyTreeSnapshot = this.generatePropertyTreeSnapshot(linkColId1Value, extendNodeId, true, isExportDefinition, "replace".equals(existsAction));
                            --this.recursionLevel;
                            PropertyTreeSnapshotItem propertyTreeSnapshotItem = propertyTreeSnapshot.getSnapshotItem();
                            propertyTreeSnapshotItem.setIncludedForTransfer(true);
                            reqSDISnapshotItem.addLink(propertyTreeSnapshotItem, SnapshotItem.LinkType.FK, linkId + ";" + detailFKLinkId + ";" + j, null);
                            if (!propertyTreeSnapshotItem.isLoadedSuccessfully()) continue;
                            sdiSnapshot.addTransferSnapshot(propertyTreeSnapshot);
                            continue;
                        }
                        SDISnapshotItem fkSnapshotItem = new SDISnapshotItem(detailFKLinkSDCId, linkColId1Value, linkColId2Value, linkColId3Value, fkPolicyNodeId);
                        fkSnapshotItem.setIncludedForTransfer(true);
                        reqSDISnapshotItem.addLink(fkSnapshotItem, SnapshotItem.LinkType.FK, linkId + ";" + detailFKLinkId + ";" + j, detailFKLinkPolicyProps);
                        if (this.isGenerateSnapshotLinkTree) {
                            reqSDISnapshotItem.setLoadedSuccessfully(true);
                            continue;
                        }
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Detail FK Link found: " + fkSnapshotItem, true);
                        SDISnapshot fkSDISnapshot = this.buildSDISnapshot(fkSnapshotItem, isPackaging, isFullExport);
                        if (!fkSnapshotItem.isLoadedSuccessfully()) continue;
                        sdiSnapshot.addTransferSnapshot(fkSDISnapshot);
                    }
                }
            }
        }
        if (isFullExport) {
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for SDIxxx detail FK Links.", true);
            ArrayList<String> sdixxxTables = cmtPolicy.getSDIDetailDataSetList(true);
            for (String sdixxxTableId : sdixxxTables) {
                DataSet sdixxxTableData = primarySDIData.getDataset(SDIData.getDatasetNameByTableName(sdixxxTableId));
                if (sdixxxTableData == null || sdixxxTableData.getRowCount() == 0) continue;
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing SDIxxx Detail table: " + sdixxxTableId, true);
                PropertyListCollection sdixxxFKCollection = cmtPolicy.getSDIDetailFKLinksProps(sdixxxTableId, true);
                if (sdixxxFKCollection == null || sdixxxFKCollection.size() <= 0) continue;
                for (int i3 = 0; i3 < sdixxxFKCollection.size(); ++i3) {
                    boolean isSDIAttributeDefLink;
                    PropertyList fkLinkPolicyProps = sdixxxFKCollection.getPropertyList(i3);
                    String fkLinkInfo = CMTPolicy.getDetailFKLinkInfo(fkLinkPolicyProps);
                    String fkLinkSDCid = CMTPolicy.getDetailFKLinkSDCId(fkLinkPolicyProps);
                    String fkLinkId = CMTPolicy.getDetailFKLinkLinkId(fkLinkPolicyProps);
                    String fkLinkAlias = fkLinkPolicyProps.getProperty("linkalias", "");
                    String fkPolicyNodeId = fkLinkPolicyProps.getProperty("refpolicynodeid");
                    PropertyList fkLinkSDCProps = this.getSDCProcessor().getPropertyList(fkLinkSDCid);
                    int fkKeyColCount = Integer.parseInt(fkLinkSDCProps.getProperty("keycolumns"));
                    PropertyList fkLinkSDCLinkProps = null;
                    boolean isCategoryItemCategoryLink = "categoryitem".equalsIgnoreCase(sdixxxTableId) && "category".equals(fkLinkId) && "Category".equals(fkLinkAlias);
                    boolean bl = isSDIAttributeDefLink = "sdiattribute".equalsIgnoreCase(sdixxxTableId) && "attributedef".equals(fkLinkId) && "Attribute Def".equals(fkLinkAlias);
                    if (isCategoryItemCategoryLink) {
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Export Category for categoryitems.", true);
                        fkLinkSDCLinkProps = new PropertyList();
                        fkLinkSDCLinkProps.setProperty("sdccolumnid", "categoryid");
                        fkLinkSDCLinkProps.setProperty("sdccolumnid2", "sdcid");
                        fkLinkSDCLinkProps.setProperty("sdccolumnid3", "");
                    } else if (isSDIAttributeDefLink) {
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Export Attribute Def for sdiattribute.", true);
                        fkLinkSDCLinkProps = new PropertyList();
                        fkLinkSDCLinkProps.setProperty("sdccolumnid", "attributeid");
                        fkLinkSDCLinkProps.setProperty("sdccolumnid2", "attributesdcid");
                        fkLinkSDCLinkProps.setProperty("sdccolumnid3", "");
                    } else {
                        DataSet dSDCInfo = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid FROM sdc WHERE tableid = ? AND sdctype = 'D'", new Object[]{sdixxxTableId});
                        if (dSDCInfo.getRowCount() > 0) {
                            String sdixxxSDCId = dSDCInfo.getString(0, "sdcid", "");
                            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "D-SDC found: " + sdixxxSDCId, true);
                            fkLinkSDCLinkProps = new PropertyList(this.getSDCProcessor().getLinkProperties(sdixxxSDCId, fkLinkId));
                        } else {
                            DataSet sdixxxLinkDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdixxxlink WHERE tableid = ? AND linkid = ?", new Object[]{sdixxxTableId, fkLinkId});
                            if (sdixxxLinkDS.getRowCount() > 0) {
                                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "SDI XXX Link info found", true);
                                fkLinkSDCLinkProps = new PropertyList();
                                fkLinkSDCLinkProps.setProperty("sdccolumnid", sdixxxLinkDS.getString(0, "sdccolumnid", ""));
                                fkLinkSDCLinkProps.setProperty("sdccolumnid2", sdixxxLinkDS.getString(0, "sdccolumnid2", ""));
                                fkLinkSDCLinkProps.setProperty("sdccolumnid3", sdixxxLinkDS.getString(0, "sdccolumnid3", ""));
                            }
                        }
                    }
                    if (fkLinkSDCLinkProps != null) {
                        String linkColId1 = fkLinkSDCLinkProps.getProperty("sdccolumnid");
                        String linkColId2 = fkLinkSDCLinkProps.getProperty("sdccolumnid2");
                        String linkColId3 = fkLinkSDCLinkProps.getProperty("sdccolumnid3");
                        for (int j = 0; j < sdixxxTableData.getRowCount(); ++j) {
                            String linkColId3Value;
                            String linkColId1Value = sdixxxTableData.getString(j, linkColId1, "");
                            String linkColId2Value = fkKeyColCount > 1 ? sdixxxTableData.getString(j, linkColId2, "") : "";
                            String string = linkColId3Value = fkKeyColCount > 2 ? sdixxxTableData.getString(j, linkColId3, "") : "";
                            if (linkColId1Value.length() <= 0) continue;
                            SDISnapshotItem fkSnapshotItem = new SDISnapshotItem(fkLinkSDCid, linkColId1Value, linkColId2Value, linkColId3Value, fkPolicyNodeId);
                            fkSnapshotItem.setIncludedForTransfer(true);
                            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "SDIxxx Detail FK Link found: " + fkSnapshotItem, true);
                            if (isCategoryItemCategoryLink) {
                                reqSDISnapshotItem.addLink(fkSnapshotItem, SnapshotItem.LinkType.SQL, "Category;" + j, fkLinkPolicyProps);
                            } else if (isSDIAttributeDefLink) {
                                reqSDISnapshotItem.addLink(fkSnapshotItem, SnapshotItem.LinkType.SQL, "LV_AttributeDef;" + j, fkLinkPolicyProps);
                            } else {
                                reqSDISnapshotItem.addLink(fkSnapshotItem, SnapshotItem.LinkType.FK, sdixxxTableId + ";" + fkLinkId + ";" + j, fkLinkPolicyProps);
                            }
                            if (this.isGenerateSnapshotLinkTree) {
                                fkSnapshotItem.setLoadedSuccessfully(true);
                                continue;
                            }
                            SDISnapshot fkSDISnapshot = this.buildSDISnapshot(fkSnapshotItem, isPackaging, isFullExport);
                            if (!fkSnapshotItem.isLoadedSuccessfully()) continue;
                            sdiSnapshot.addTransferSnapshot(fkSDISnapshot);
                        }
                        continue;
                    }
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Link Info not found: " + fkLinkInfo, true);
                }
            }
        }
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for Rev-FK Links.", true);
        PropertyListCollection rfkCollection = cmtPolicy.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.REVFK);
        for (i = 0; i < rfkCollection.size(); ++i) {
            PropertyList rfkLinkPolicyProps = rfkCollection.getPropertyList(i);
            String rfkPolicyNodeId = rfkLinkPolicyProps.getProperty("refpolicynodeid");
            String rfkLinkInfo = CMTPolicy.getAssociatedSDILinkInfo(rfkLinkPolicyProps);
            String rfkLinkSDCid = CMTPolicy.getAssociatedSDILinkSDCId(rfkLinkPolicyProps);
            String rfkLinkId = CMTPolicy.getAssociatedSDILinkLinkId(rfkLinkPolicyProps);
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Link: " + rfkLinkInfo, false);
            PropertyList rfkLinkSDCProps = this.getSDCProcessor().getProperties(rfkLinkSDCid);
            PropertyList rfkLinkSDCLinkProps = new PropertyList(this.getSDCProcessor().getLinkProperties(rfkLinkSDCid, rfkLinkId));
            int rfkSDCKeyColCount = Integer.parseInt(rfkLinkSDCProps.getProperty("keycolumns"));
            String rfkQuery = "SELECT " + rfkLinkSDCProps.getProperty("keycolid1") + (rfkSDCKeyColCount > 1 ? ", " + rfkLinkSDCProps.getProperty("keycolid2") : "") + (rfkSDCKeyColCount > 2 ? ", " + rfkLinkSDCProps.getProperty("keycolid3") : "") + " FROM " + rfkLinkSDCProps.getProperty("tableid") + " WHERE " + rfkLinkSDCLinkProps.getProperty("sdccolumnid") + " = '" + reqSDISnapshotItem.getKeyId1() + "'" + (primarySDCKeyColCount > 1 ? " AND " + rfkLinkSDCLinkProps.getProperty("sdccolumnid2") + " = '" + reqSDISnapshotItem.getKeyId2() + "'" : "") + (primarySDCKeyColCount > 2 ? " AND " + rfkLinkSDCLinkProps.getProperty("sdccolumnid3") + " = '" + reqSDISnapshotItem.getKeyId3() + "'" : "");
            if (rfkLinkPolicyProps.getProperty("querywhere").trim().length() > 0) {
                rfkQuery = rfkQuery + " AND (" + rfkLinkPolicyProps.getProperty("querywhere") + " ) ";
            }
            rfkQuery = rfkQuery + " ORDER BY usersequence, " + rfkLinkSDCProps.getProperty("keycolid1") + (rfkSDCKeyColCount > 1 ? ", " + rfkLinkSDCProps.getProperty("keycolid2") : "") + (rfkSDCKeyColCount > 2 ? ", " + rfkLinkSDCProps.getProperty("keycolid3") : "");
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Query for Rev-FK Data: " + rfkQuery, true);
            DataSet rfkSDIs = this.getQueryProcessor().getSqlDataSet(rfkQuery);
            if (rfkSDIs == null) {
                throw new SapphireException("RevFKError", "INFORMATION", "Exception occurred when trying to retrive list of Rev-FK rows for: " + reqSDISnapshotItem + ":" + rfkLinkId);
            }
            if (rfkSDIs.getRowCount() > 0) {
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No. of Rows: " + rfkSDIs.getRowCount(), true);
                for (int j = 0; j < rfkSDIs.getRowCount(); ++j) {
                    SDISnapshotItem rfkSnapshotItem = new SDISnapshotItem(rfkLinkSDCid, rfkSDIs.getString(j, rfkLinkSDCProps.getProperty("keycolid1"), ""), rfkSDCKeyColCount > 1 ? rfkSDIs.getString(j, rfkLinkSDCProps.getProperty("keycolid2"), "") : "", rfkSDCKeyColCount > 2 ? rfkSDIs.getString(j, rfkLinkSDCProps.getProperty("keycolid3"), "") : "", rfkPolicyNodeId);
                    reqSDISnapshotItem.addLink(rfkSnapshotItem, SnapshotItem.LinkType.REVFK, rfkLinkInfo, rfkLinkPolicyProps);
                    if (cmtPolicy.isIncludedForTransfer(rfkLinkPolicyProps) && isPackaging) {
                        rfkSnapshotItem.setIncludedForTransfer(true);
                        if (this.isGenerateSnapshotLinkTree) {
                            rfkSnapshotItem.setLoadedSuccessfully(true);
                            continue;
                        }
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Rev-FK Link found: " + rfkSnapshotItem.toString(true, true), true);
                        SDISnapshot rfkSDISnapshot = this.buildSDISnapshot(rfkSnapshotItem, isPackaging, isFullExport);
                        if (!rfkSnapshotItem.isLoadedSuccessfully()) continue;
                        sdiSnapshot.addTransferSnapshot(rfkSDISnapshot);
                        continue;
                    }
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Embedded Rev-FK Link found: " + rfkSnapshotItem.toString(true, true), true);
                    this.gatherSDISnapshotData(sdiSnapshot, rfkSnapshotItem, isPackaging, isFullExport);
                }
                continue;
            }
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No Rev-FK Linked Item found.", true);
        }
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for Many-to-Many Links.", true);
        PropertyListCollection m2mCollection = cmtPolicy.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.M2M);
        for (i = 0; i < m2mCollection.size(); ++i) {
            PropertyList m2mLinkPolicyProps = m2mCollection.getPropertyList(i);
            String m2mPolicyNodeId = m2mLinkPolicyProps.getProperty("refpolicynodeid");
            String m2mLinkInfo = CMTPolicy.getAssociatedSDILinkInfo(m2mLinkPolicyProps);
            String m2mLinkSDCid = CMTPolicy.getAssociatedSDILinkSDCId(m2mLinkPolicyProps);
            String m2mLinkId = CMTPolicy.getAssociatedSDILinkLinkId(m2mLinkPolicyProps);
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Link: " + m2mLinkInfo, false);
            PropertyList m2mLinkSDCProps = this.getSDCProcessor().getProperties(m2mLinkSDCid);
            PropertyList m2mLinkSDCLinkProps = new PropertyList(this.getSDCProcessor().getLinkProperties(reqSDISnapshotItem.getSDCId(), m2mLinkId));
            String m2mLinkTableId = m2mLinkSDCLinkProps.getProperty("linktableid");
            int m2mSDCKeyColCount = Integer.parseInt(m2mLinkSDCProps.getProperty("keycolumns"));
            DataSet m2mMapTable = primarySDIData.getDataset(m2mLinkTableId);
            if (m2mMapTable == null) {
                String message = "Table not found in Snapshot. Skip this link.";
                throw new SapphireException("M2MTableError", "INFORMATION", message + ": " + m2mLinkTableId);
            }
            if (m2mMapTable.getRowCount() > 0) {
                for (int j = 0; j < m2mMapTable.getRowCount(); ++j) {
                    SDISnapshotItem m2mSnapshotItem = new SDISnapshotItem(m2mLinkSDCid, m2mMapTable.getString(j, m2mLinkSDCProps.getProperty("keycolid1"), ""), m2mSDCKeyColCount > 1 ? m2mMapTable.getString(j, m2mLinkSDCProps.getProperty("keycolid2"), "") : "", m2mSDCKeyColCount > 2 ? m2mMapTable.getString(j, m2mLinkSDCProps.getProperty("keycolid3"), "") : "", m2mPolicyNodeId);
                    reqSDISnapshotItem.addLink(m2mSnapshotItem, SnapshotItem.LinkType.M2M, m2mLinkInfo, m2mLinkPolicyProps);
                    if (cmtPolicy.isIncludedForTransfer(m2mLinkPolicyProps) && isPackaging) {
                        m2mSnapshotItem.setIncludedForTransfer(true);
                        if (this.isGenerateSnapshotLinkTree) {
                            m2mSnapshotItem.setLoadedSuccessfully(true);
                            continue;
                        }
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "M2M Link found: " + m2mSnapshotItem.toString(true), true);
                        SDISnapshot m2mSDISnapshot = this.buildSDISnapshot(m2mSnapshotItem, isPackaging, isFullExport);
                        if (!m2mSnapshotItem.isLoadedSuccessfully()) continue;
                        sdiSnapshot.addTransferSnapshot(m2mSDISnapshot);
                        continue;
                    }
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Embedded M2M Link found: " + m2mSnapshotItem.toString(true), true);
                    this.gatherSDISnapshotData(sdiSnapshot, m2mSnapshotItem, isPackaging, isFullExport);
                }
                continue;
            }
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No M2M Linked Item found.", true);
        }
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for Rev Soft Links.", true);
        PropertyListCollection rslCollection = cmtPolicy.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.REVSOFTLINK);
        for (i = 0; i < rslCollection.size(); ++i) {
            PropertyList rslLinkPolicyProps = rslCollection.getPropertyList(i);
            String rslPolicyNodeId = rslLinkPolicyProps.getProperty("refpolicynodeid");
            String rslLinkInfo = CMTPolicy.getAssociatedSDILinkInfo(rslLinkPolicyProps);
            String rslLinkSDCid = CMTPolicy.getAssociatedSDILinkSDCId(rslLinkPolicyProps);
            if (rslLinkSDCid.length() <= 0) continue;
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Link: " + rslLinkInfo, true);
            PropertyList revSLProps = rslLinkPolicyProps.getPropertyList("revsoftlinkprops");
            String restrictiveWhere = rslLinkPolicyProps.getProperty("restrictivewhere");
            if (restrictiveWhere.length() > 0) {
                String[] tokens;
                for (String token : tokens = StringUtil.getTokens(restrictiveWhere, "[", "]")) {
                    String tokenValue = "";
                    if ("keyid1".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid1"));
                    } else if ("keyid2".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid2"));
                    } else if ("keyid3".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid3"));
                    } else if (primary.isValidColumn(token)) {
                        tokenValue = primary.getValue(0, token);
                    }
                    restrictiveWhere = StringUtil.replaceAll(restrictiveWhere, "[" + token + "]", tokenValue);
                }
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Query Where: " + restrictiveWhere, true);
            }
            PropertyList rslLinkSDCProps = this.getSDCProcessor().getProperties(rslLinkSDCid);
            int rslSDCKeyColCount = Integer.parseInt(rslLinkSDCProps.getProperty("keycolumns"));
            SDIRequest rslLinkRequest = new SDIRequest();
            rslLinkRequest.setSDCid(rslLinkSDCid);
            rslLinkRequest.setQueryFrom(rslLinkSDCProps.getProperty("tableid"));
            rslLinkRequest.setRequestItem("primary");
            String linkSDCIdCol = revSLProps.getProperty("linksdcidcol");
            String linkKeyId1Col = revSLProps.getProperty("linkkeyid1col");
            String linkKeyId2Col = revSLProps.getProperty("linkkeyid2col");
            String linkKeyId3Col = revSLProps.getProperty("linkkeyid3col");
            if (linkSDCIdCol.length() == 0 || linkKeyId1Col.length() == 0 || primarySDCKeyColCount > 1 && linkKeyId2Col.length() == 0 || primarySDCKeyColCount > 2 && linkKeyId3Col.length() == 0) {
                throw new SapphireException("InvalidPolicySettings", "INFORMATION", "Missing mandatory link columns.");
            }
            String sqlWhere = "(" + linkSDCIdCol + " = '" + reqSDISnapshotItem.getSDCId() + "' AND " + linkKeyId1Col + " = '" + primary.getString(0, primarySDCProps.getProperty("keycolid1")) + "'" + (primarySDCKeyColCount > 1 ? " AND " + linkKeyId2Col + " = '" + primary.getString(0, primarySDCProps.getProperty("keycolid2")) + "'" : "") + (primarySDCKeyColCount > 2 ? " AND " + linkKeyId3Col + " = '" + primary.getString(0, primarySDCProps.getProperty("keycolid3")) + "'" : "") + ")";
            if (restrictiveWhere.trim().length() > 0) {
                sqlWhere = sqlWhere + "AND (" + restrictiveWhere + ")";
            }
            rslLinkRequest.setQueryWhere(sqlWhere);
            SDIData rslSDIData = this.getSDIProcessor().getSDIData(rslLinkRequest);
            if (rslSDIData == null) {
                throw new SapphireException("InvalidPolicySettings", "INFORMATION", "Exception occurred when trying to retrieve Soft link SDIs: " + reqSDISnapshotItem.getSDCId() + ";" + rslLinkSDCid);
            }
            DataSet rslSDIs = rslSDIData.getDataset("primary");
            if (rslSDIs.getRowCount() > 0) {
                for (int j = 0; j < rslSDIs.getRowCount(); ++j) {
                    SDISnapshotItem rslSnapshotItem = new SDISnapshotItem(rslLinkSDCid, rslSDIs.getString(j, rslLinkSDCProps.getProperty("keycolid1"), ""), rslSDCKeyColCount > 1 ? rslSDIs.getString(j, rslLinkSDCProps.getProperty("keycolid2"), "") : "", rslSDCKeyColCount > 2 ? rslSDIs.getString(j, rslLinkSDCProps.getProperty("keycolid3"), "") : "", rslPolicyNodeId);
                    reqSDISnapshotItem.addLink(rslSnapshotItem, SnapshotItem.LinkType.REVSOFTLINK, rslLinkInfo, rslLinkPolicyProps);
                    if (cmtPolicy.isIncludedForTransfer(rslLinkPolicyProps) && isPackaging) {
                        rslSnapshotItem.setIncludedForTransfer(true);
                        if (this.isGenerateSnapshotLinkTree) {
                            rslSnapshotItem.setLoadedSuccessfully(true);
                            continue;
                        }
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Special Link found: " + rslSnapshotItem.toString(true), true);
                        SDISnapshot rslSDISnapshot = this.buildSDISnapshot(rslSnapshotItem, isPackaging, isFullExport);
                        if (!rslSnapshotItem.isLoadedSuccessfully()) continue;
                        sdiSnapshot.addTransferSnapshot(rslSDISnapshot);
                        continue;
                    }
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Embedded Special Link found: " + rslSnapshotItem.toString(true), true);
                    this.gatherSDISnapshotData(sdiSnapshot, rslSnapshotItem, isPackaging, isFullExport);
                }
                continue;
            }
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No Soft Linked Item found.", true);
        }
        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Scanning Policy for Special Links.", true);
        PropertyListCollection bySQLCollection = cmtPolicy.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.SQL);
        for (i = 0; i < bySQLCollection.size(); ++i) {
            PropertyList bySQLLinkPolicyProps = bySQLCollection.getPropertyList(i);
            String bySQLPolicyNodeId = bySQLLinkPolicyProps.getProperty("refpolicynodeid");
            String bySQLLinkInfo = CMTPolicy.getAssociatedSDILinkInfo(bySQLLinkPolicyProps);
            String bySQLLinkSDCid = CMTPolicy.getAssociatedSDILinkSDCId(bySQLLinkPolicyProps);
            if (bySQLLinkSDCid.length() <= 0) continue;
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Processing Link: " + bySQLLinkInfo, true);
            String queryWhere = bySQLLinkPolicyProps.getProperty("querywhere");
            if (queryWhere.trim().length() == 0) {
                throw new SapphireException("InvalidPolicySettings", "INFORMATION", "By SQL Links need a 'Query Where' to be defined: " + reqSDISnapshotItem.getSDCId() + ";" + bySQLLinkSDCid);
            }
            if (queryWhere.length() > 0) {
                String[] tokens;
                for (String token : tokens = StringUtil.getTokens(queryWhere, "[", "]")) {
                    String tokenValue = "";
                    if ("keyid1".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid1"));
                    } else if ("keyid2".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid2"));
                    } else if ("keyid3".equalsIgnoreCase(token)) {
                        tokenValue = primary.getString(0, primarySDCProps.getProperty("keycolid3"));
                    } else if (primary.isValidColumn(token)) {
                        tokenValue = primary.getValue(0, token);
                    }
                    queryWhere = StringUtil.replaceAll(queryWhere, "[" + token + "]", tokenValue);
                }
                this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Query Where: " + queryWhere, true);
            }
            PropertyList bySQLLinkSDCProps = this.getSDCProcessor().getProperties(bySQLLinkSDCid);
            int bySQLSDCKeyColCount = Integer.parseInt(bySQLLinkSDCProps.getProperty("keycolumns"));
            SDIRequest bySQLLinkRequest = new SDIRequest();
            bySQLLinkRequest.setSDCid(bySQLLinkSDCid);
            bySQLLinkRequest.setQueryFrom(bySQLLinkSDCProps.getProperty("tableid"));
            bySQLLinkRequest.setRequestItem("primary");
            bySQLLinkRequest.setQueryWhere(queryWhere);
            SDIData bySQLSDIData = this.getSDIProcessor().getSDIData(bySQLLinkRequest);
            if (bySQLSDIData == null) {
                throw new SapphireException("InvalidPolicySettings", "INFORMATION", "Exception occurred when trying to retrieve Soft link SDIs: " + reqSDISnapshotItem.getSDCId() + ";" + bySQLLinkSDCid);
            }
            DataSet bySQLSDIs = bySQLSDIData.getDataset("primary");
            if (bySQLSDIs.getRowCount() > 0) {
                for (int j = 0; j < bySQLSDIs.getRowCount(); ++j) {
                    SDISnapshotItem bySQLSnapshotItem = new SDISnapshotItem(bySQLLinkSDCid, bySQLSDIs.getString(j, bySQLLinkSDCProps.getProperty("keycolid1"), ""), bySQLSDCKeyColCount > 1 ? bySQLSDIs.getString(j, bySQLLinkSDCProps.getProperty("keycolid2"), "") : "", bySQLSDCKeyColCount > 2 ? bySQLSDIs.getString(j, bySQLLinkSDCProps.getProperty("keycolid3"), "") : "", bySQLPolicyNodeId);
                    reqSDISnapshotItem.addLink(bySQLSnapshotItem, SnapshotItem.LinkType.SQL, bySQLLinkInfo, bySQLLinkPolicyProps);
                    if (cmtPolicy.isIncludedForTransfer(bySQLLinkPolicyProps) && isPackaging) {
                        bySQLSnapshotItem.setIncludedForTransfer(true);
                        if (this.isGenerateSnapshotLinkTree) {
                            bySQLSnapshotItem.setLoadedSuccessfully(true);
                            continue;
                        }
                        this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Special Link found: " + bySQLSnapshotItem.toString(true), true);
                        SDISnapshot bySQLSDISnapshot = this.buildSDISnapshot(bySQLSnapshotItem, isPackaging, isFullExport);
                        if (!bySQLSnapshotItem.isLoadedSuccessfully()) continue;
                        sdiSnapshot.addTransferSnapshot(bySQLSDISnapshot);
                        continue;
                    }
                    this.log("gatherSDISnapshotData", reqSDISnapshotItem, "Embedded Special Link found: " + bySQLSnapshotItem.toString(true), true);
                    this.gatherSDISnapshotData(sdiSnapshot, bySQLSnapshotItem, isPackaging, isFullExport);
                }
                continue;
            }
            this.log("gatherSDISnapshotData", reqSDISnapshotItem, "No Soft Linked Item found.", true);
        }
    }

    public static Node getNonComponentParentNode(Node node) {
        Node parent = node.getParent();
        if (!node.getId().endsWith(" Custom") || node.getCompCode().length() <= 0) {
            boolean done = false;
            while (!done && parent != null && !parent.getId().endsWith(" Custom") && parent.getCompCode().length() > 0) {
                if ((parent = parent.getParent()) != null && !parent.isCustom()) continue;
                parent = node.getParent();
                done = true;
            }
        }
        return parent;
    }

    private void setNodeImportAttributes(PropertyTreeSnapshot snapshot, boolean overwriteNode) {
        if (overwriteNode) {
            snapshot.setExists("replace");
        } else {
            snapshot.setExists("ignore");
        }
        snapshot.setNotExists("add");
    }

    private void collectCertificationData(SDIRequest primarySDIRequest, SDIData primarySDIData, CMTPolicy configPolicy, PropertyList sdcProps) {
        PropertyList sdiCertProps;
        if (primarySDIRequest.isRequestItem("s_sdicertification") && (sdiCertProps = configPolicy.getSDIDetailProps("s_sdicertification")) != null) {
            String retrieveBy = sdiCertProps.getProperty("retrieveby");
            String colNamePrefix = "";
            if ("resourcesdc".equals(retrieveBy)) {
                colNamePrefix = "resource";
            } else if ("certifiedforsdc".equals(retrieveBy)) {
                colNamePrefix = "certifiedfor";
            }
            SafeSQL safeSQL = new SafeSQL();
            int keyColCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
            String sql = "SELECT * FROM s_sdicertification WHERE " + colNamePrefix + "sdcid = " + safeSQL.addVar(primarySDIRequest.getSDCid()) + " AND " + colNamePrefix + "keyid1 = " + safeSQL.addVar(primarySDIRequest.getKeyid1List());
            if (keyColCount > 1) {
                sql = sql + " AND " + colNamePrefix + "keyid2 = " + safeSQL.addVar(primarySDIRequest.getKeyid2List());
            }
            if (keyColCount > 2) {
                sql = sql + " AND " + colNamePrefix + "keyid3 = " + safeSQL.addVar(primarySDIRequest.getKeyid3List());
            }
            DataSet certData = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            primarySDIData.setDataset("s_sdicertification", certData);
        }
    }

    private void handleDevModeColumns(SDIData sdiData, String primaryTableId, String keyColId1) {
        if (sdiData == null) {
            return;
        }
        Set datasets = sdiData.getDatasets();
        for (Object obj : datasets) {
            String datasetName = (String)obj;
            if ("primary".equals(datasetName)) {
                this.handleDevModeColumns(sdiData.getDataset(datasetName), primaryTableId, keyColId1);
                continue;
            }
            this.handleDevModeColumns(sdiData.getDataset(datasetName), datasetName, keyColId1);
        }
    }

    private void excludeSpecifiedColumns(SDIData sdiData, CMTPolicy configPolicy, boolean isPackaging) {
        Object[] datasetnames;
        if (sdiData == null) {
            return;
        }
        ArrayList<String> excludedTables = configPolicy.getExcludedTableList();
        for (Object datasetNameObj : datasetnames = sdiData.getDatasets().toArray()) {
            String dataSetName = (String)datasetNameObj;
            if (isPackaging && excludedTables.contains(SDIData.getDatasetTablename(dataSetName))) {
                sdiData.removeDataset(dataSetName);
                continue;
            }
            DataSet dataSet = sdiData.getDataset(dataSetName);
            if (dataSet != null) {
                ArrayList<String> columnList = configPolicy.getExcludedColumnList(SDIData.getDatasetTablename(dataSetName));
                for (String colId : dataSet.getColumns()) {
                    if (!colId.toLowerCase().startsWith("__") && (!columnList.contains(colId) || !isPackaging)) continue;
                    dataSet.removeColumn(colId);
                }
            }
            if (!dataSetName.equals("attachment")) continue;
            HashMap<String, BigDecimal> filtermap = new HashMap<String, BigDecimal>();
            filtermap.put("attachmentnum", new BigDecimal("0"));
            sdiData.setDataset("attachment", dataSet.getFilteredDataSet(filtermap, true));
        }
    }

    private void handleDevModeColumns(DataSet dataset, String tableId, String keyColId1) {
        if (devModeCols.containsKey(tableId)) {
            PropertyListCollection colGroups = devModeCols.getCollectionNotNull(tableId);
            for (int i = 0; i < colGroups.size(); ++i) {
                PropertyList colGroup = colGroups.getPropertyList(i);
                String prodColId = colGroup.getProperty(devModeCol);
                String customColId = colGroup.getProperty(customModeCol);
                String compColId = colGroup.getProperty(componentModeCol);
                if (this.isDevMode) {
                    dataset.removeColumn(customColId);
                    dataset.removeColumn(compColId);
                    continue;
                }
                if (this.compCode.length() == 0) {
                    dataset.removeColumn(compColId);
                    continue;
                }
                dataset.removeColumn(customColId);
                if (dataset.getString(0, keyColId1, "").startsWith(this.compCode + "_")) {
                    dataset.removeColumn(compColId);
                    continue;
                }
                dataset.removeColumn(prodColId);
            }
        }
    }

    private void clearOldGenerators() {
        String[] runIds;
        Calendar now = Calendar.getInstance();
        for (String runId : runIds = generatorThreads.keySet().toArray(new String[0])) {
            SnapshotGenerator run = generatorThreads.get(runId);
            long elapsedMillis = now.getTime().getTime() - run.startDateCal.getTime().getTime();
            if (elapsedMillis / 60000L <= 30L) continue;
            generatorThreads.remove(runId);
        }
    }

    private SnapshotGenerator createNewGenerator() throws SapphireException {
        if (this.currGenerator != null && (this.currGenerator.status == SnapshotGenerator.Status.INITIATED || this.currGenerator.status == SnapshotGenerator.Status.INPROGRESS)) {
            throw new SapphireException("This Factory instance is busy running another Generator thread. Please create a new instance.");
        }
        SnapshotGenerator run = new SnapshotGenerator();
        generatorThreads.put(run.id, run);
        run.stringLoggerRuntime = this.stringLoggerRuntime;
        this.currGenerator = run;
        return run;
    }

    public static SnapshotGenerator getSnapshotGenerator(String runId) {
        return generatorThreads.get(runId);
    }

    public static void removeSnapshotGenerator(String runId) {
        generatorThreads.remove(runId);
    }

    private void log(String method, SnapshotItem snapshotItem, String text, boolean isVerboseLog) {
        if (snapshotItem == null) {
            this.log(method, text, isVerboseLog);
        } else if (Snapshot.Type.SDI.equals((Object)snapshotItem.getType())) {
            this.log(method, text, isVerboseLog);
        } else {
            this.log(method, text, isVerboseLog);
        }
    }

    private void log(String method, String text, boolean isVerboseLog) {
        String indent = "";
        if (this.isIndentLog()) {
            indent = StringUtil.repeat("\t", this.recursionLevel > -1 ? this.recursionLevel : 0);
        }
        if (!isVerboseLog || this.isVerbose()) {
            this.stringLogger.log(indent + text);
            this.stringLoggerRuntime.log(indent + text);
            this.logger.info(indent + method + ": " + text);
        }
    }

    public String getLog() {
        return this.stringLogger.getLog();
    }

    public StringLogger getStringLogger() {
        return this.stringLogger;
    }

    static {
        devModeCols = new PropertyList();
        PropertyList colGroup = new PropertyList();
        PropertyListCollection colGroups = new PropertyListCollection();
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "productvaluetree");
        colGroup.setProperty(customModeCol, "valuetree");
        colGroup.setProperty(componentModeCol, "componentvaluetree");
        colGroups.add(colGroup);
        devModeCols.setProperty("webpagepropertytree", colGroups);
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "productvaluetree");
        colGroup.setProperty(customModeCol, "valuetree");
        colGroup.setProperty(componentModeCol, "");
        colGroups.add(colGroup);
        devModeCols.setProperty("gizmodef", colGroups);
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "");
        colGroup.setProperty(customModeCol, "taskdefoverride");
        colGroup.setProperty(componentModeCol, "");
        colGroups.add(colGroup);
        devModeCols.setProperty("taskdef", colGroups);
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "productvaluetree");
        colGroup.setProperty(customModeCol, "valuetree");
        colGroup.setProperty(componentModeCol, "");
        colGroups.add(colGroup);
        devModeCols.setProperty("taskdefstep", colGroups);
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "productvaluetree");
        colGroup.setProperty(customModeCol, "valuetree");
        colGroup.setProperty(componentModeCol, "componentvaluetree");
        colGroups.add(colGroup);
        devModeCols.setProperty("app", colGroups);
        colGroups = new PropertyListCollection();
        colGroup = new PropertyList();
        colGroup.setProperty(devModeCol, "productvaluetree");
        colGroup.setProperty(customModeCol, "valuetree");
        colGroup.setProperty(componentModeCol, "componentvaluetree");
        colGroups.add(colGroup);
        devModeCols.setProperty("portal", colGroups);
    }
}

