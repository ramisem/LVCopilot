/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.actions.cmt.ExportSnapshot;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.ExternalHandlerProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class CRIAjaxUtil
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision:  $";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_WARN = "WARN";
    private String sessionId = "";
    private CRISession session;
    private Map<String, CRISession> sessionCache;
    private static final String CRI_CACHE_NAME = "CRI_CACHE";
    private static Timer CRI_TIMER = new Timer();
    private static boolean CRI_TIMER_RUNNING = false;
    private static Map<String, Long> CRI_TIMER_INTERVAL_MILLIS_ALL = new HashMap<String, Long>();
    private static final long CRI_TIMER_INTERVAL_MILLIS_DEFAULT = 60000L;
    private static Map<String, Long> CRI_SESSION_LIFE_MAX_MILLIS_ALL = new HashMap<String, Long>();
    private static final long CRI_SESSION_LIFE_MAX_MILLIS_DEFAULT = 60000L;
    private static Map<String, Map<String, CRISession>> ALL_SESSION_CACHES = new ConcurrentHashMap<String, Map<String, CRISession>>();
    public long index = 1L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block18: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String method = ajaxResponse.getRequestParameter("method", "");
            String promiseId = ajaxResponse.getRequestParameter("__promiseId", "");
            try {
                this.logger.info("Method: " + method);
                this.getUserCRICache(request);
                if ("doInit".equalsIgnoreCase(method)) {
                    this.doInit(ajaxResponse);
                    break block18;
                }
                this.sessionId = ajaxResponse.getRequestParameter("sessionId");
                this.logger.info("Session Id: " + this.sessionId);
                if (!"doInit".equals(method) && this.sessionId == null || this.sessionId.length() == 0) {
                    throw new SapphireException("Active Session not found for sessionId: " + this.sessionId);
                }
                this.session = this.getCRISession(this.sessionId);
                if ("doLoadCRInfo".equalsIgnoreCase(method)) {
                    this.doLoadCRInfo(ajaxResponse);
                    break block18;
                }
                if ("doLoadRemoteHosts".equalsIgnoreCase(method)) {
                    this.doLoadRemoteHosts(ajaxResponse);
                    break block18;
                }
                if ("doTestRemoteHost".equalsIgnoreCase(method)) {
                    this.doTestRemoteHost(ajaxResponse);
                    break block18;
                }
                if ("doSessionPing".equalsIgnoreCase(method)) {
                    this.doSessionPing(ajaxResponse);
                    break block18;
                }
                if ("doLoadUniqueCLs".equalsIgnoreCase(method)) {
                    this.doLoadUniqueCLs(ajaxResponse);
                    break block18;
                }
                if ("doLoadCLInfo".equalsIgnoreCase(method)) {
                    this.doLoadCLInfo(ajaxResponse);
                    break block18;
                }
                if ("doInspectCLs".equalsIgnoreCase(method)) {
                    this.doInspectCLs(ajaxResponse);
                    break block18;
                }
                if ("doInspectCLsInRemote".equalsIgnoreCase(method)) {
                    this.doInspectCLsInRemote(ajaxResponse);
                    break block18;
                }
                throw new SapphireException("No Method specified for execution.");
            }
            catch (Exception e) {
                if (e.getMessage() == null || e.getMessage().length() == 0) {
                    ajaxResponse.setError(e.getClass().getSimpleName());
                } else {
                    ajaxResponse.setError(e.getMessage(), e);
                }
            }
            finally {
                if (promiseId.length() > 0) {
                    ajaxResponse.addCallbackArgument("__promiseId", promiseId);
                }
                ajaxResponse.print();
            }
        }
    }

    private void doInit(AjaxResponse ajaxResponse) throws SapphireException {
        this.session = this.createCRISession();
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        PropertyList criProps = this.getCRIPolicyProps();
        try {
            CRI_TIMER_INTERVAL_MILLIS_ALL.put(connectionInfo.getDatabaseId(), Long.parseLong(criProps.getProperty("criGCTimerIntervalMillis", String.valueOf(60000L))));
        }
        catch (NumberFormatException e) {
            CRI_TIMER_INTERVAL_MILLIS_ALL.put(connectionInfo.getDatabaseId(), 60000L);
        }
        try {
            CRI_SESSION_LIFE_MAX_MILLIS_ALL.put(connectionInfo.getDatabaseId(), Long.parseLong(criProps.getProperty("criMaxLifeMillis", String.valueOf(60000L))));
        }
        catch (NumberFormatException e) {
            CRI_SESSION_LIFE_MAX_MILLIS_ALL.put(connectionInfo.getDatabaseId(), 60000L);
        }
        ajaxResponse.addCallbackArgument("sessionId", this.session.id);
        ajaxResponse.addCallbackArgument("criProps", criProps.toJSONObject());
    }

    private void doLoadCRInfo(AjaxResponse ajaxResponse) throws SapphireException {
        String changeRequestIds = ajaxResponse.getRequestParameter("changeRequestIds");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT changerequestid, longdescription, changerequeststatus, assigneduserid, assigneddepartmentid FROM changerequest WHERE changerequestid IN (" + safeSQL.addIn(changeRequestIds, ";") + ") ORDER BY changerequestid", safeSQL.getValues());
        ajaxResponse.addCallbackArgument("changeRequestInfo", ds.toJSONObject(true, false, false));
    }

    private void doLoadRemoteHosts(AjaxResponse ajaxResponse) {
        ajaxResponse.addCallbackArgument("hosts", this.getRemoteHosts().toJSONArray());
    }

    private void doTestRemoteHost(AjaxResponse ajaxResponse) throws SapphireException {
        int remoteHostIdx = -1;
        try {
            remoteHostIdx = Integer.parseInt(ajaxResponse.getRequestParameter("hostIdx", "-1"));
        }
        catch (NumberFormatException e) {
            throw new SapphireException("Invalid host Index provided: " + ajaxResponse.getRequestParameter("hostIdx"));
        }
        if (remoteHostIdx == -1) {
            throw new SapphireException("Invalid Host: " + remoteHostIdx);
        }
        PropertyListCollection hosts = this.getRemoteHosts();
        if (remoteHostIdx > hosts.size()) {
            throw new SapphireException("Invalid Host Idx: " + remoteHostIdx);
        }
        try {
            PropertyList returnPL = this.callCMTExthandler(remoteHostIdx, "COMMAND_CHECK_CONNECTION", new PropertyList());
            this.session.remoteHosts.getPropertyList(remoteHostIdx).setProperty("status", STATUS_SUCCESS);
            this.session.remoteHosts.getPropertyList(remoteHostIdx).setProperty("statusMessage", returnPL.toJSONString());
            ajaxResponse.addCallbackArgument("connectionInfo", returnPL);
        }
        catch (SapphireException e) {
            this.session.remoteHosts.getPropertyList(remoteHostIdx).setProperty("status", STATUS_FAILED);
            this.session.remoteHosts.getPropertyList(remoteHostIdx).setProperty("statusMessage", e.getMessage());
            throw e;
        }
    }

    private void doSessionPing(AjaxResponse ajaxResponse) throws SapphireException {
        if (this.session == null) {
            throw new SapphireException("No Session Found for Session Id: " + this.sessionId);
        }
        this.session.ping();
        ajaxResponse.addCallbackArgument("status", true);
        ajaxResponse.addCallbackArgument("cacheSize", this.sessionCache.size());
    }

    private void doLoadUniqueCLs(AjaxResponse ajaxResponse) throws SapphireException {
        DataSet ds;
        block3: {
            String changeRequestIds = ajaxResponse.getRequestParameter("changeRequestIds");
            StringBuilder postImageCLIds = new StringBuilder();
            StringBuilder preImageCLIds = new StringBuilder();
            ExportSnapshot.determineExportableCLs("LV_ChangeRequest", changeRequestIds, postImageCLIds, preImageCLIds, false, this.getDAMProcessor(), this.getQueryProcessor(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).isOracle());
            ds = new DataSet(this.getConnectionId());
            if (postImageCLIds.length() > 0) {
                String rsetId = "";
                try {
                    rsetId = this.getDAMProcessor().createRSet("LV_ChangeLog", postImageCLIds.toString(), "", "", true, 1);
                    SafeSQL safeSQL = new SafeSQL();
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT changelogid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid, changerequestid, changelogstatus, checkedinby, s.sysuserdesc checkedinbyname, checkedindt FROM changelog c, rsetitems r, sysuser s WHERE c.changelogid = r.keyid1 AND r.rsetid = " + safeSQL.addVar(rsetId) + " AND s.sysuserid = c.checkedinby ORDER BY linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid", safeSQL.getValues());
                }
                catch (SapphireException e) {
                    if (rsetId == null || rsetId.length() <= 0) break block3;
                    this.getDAMProcessor().clearRSet(rsetId);
                }
            }
        }
        this.session.changeLogs = ds;
        JSONObject changeLogJ = ds.toJSONObject(true, false, false);
        ajaxResponse.addCallbackArgument("changeLogs", changeLogJ);
    }

    private void doLoadCLInfo(AjaxResponse ajaxResponse) throws SapphireException {
        String changeLogIds = ajaxResponse.getRequestParameter("changeLogIds");
        String[] changeLogIdsArr = StringUtil.split(changeLogIds, ";");
        JSONObject response = new JSONObject();
        for (String changeLogId : changeLogIdsArr) {
            try {
                JSONObject clInfo = new JSONObject();
                response.put(changeLogId, clInfo);
                int findRow = this.session.changeLogs.findRow("changelogid", changeLogId);
                String linkKeyId2 = this.session.changeLogs.getString(findRow, "linkkeyid2", "");
                if (linkKeyId2.length() <= 0 || "(null)".equals(linkKeyId2)) continue;
                try {
                    Integer.parseInt(linkKeyId2);
                    SDISnapshotItem snapshotItem = (SDISnapshotItem)this.getSnapshotItem(changeLogId);
                    String versionStatus = CMTUtil.getSnapshotVersionStatus(snapshotItem, this.getSDCProcessor());
                    if (versionStatus.length() <= 0) continue;
                    this.session.changeLogs.setString(findRow, "versionstatus", versionStatus);
                    clInfo.put("versionstatus", versionStatus);
                }
                catch (NumberFormatException numberFormatException) {}
            }
            catch (JSONException e) {
                throw new SapphireException(e);
            }
        }
        ajaxResponse.addCallbackArgument("clInfo", response);
    }

    private void doInspectCLs(AjaxResponse ajaxResponse) throws SapphireException {
        if (this.session.changeLogs == null) {
            throw new SapphireException("ChangeLogs not found in Session Cache.");
        }
        String changeLogIds = ajaxResponse.getRequestParameter("changeLogIds");
        this.logger.info("Inspecting ChangeLog(s): " + changeLogIds);
        int remoteHostIdx = -1;
        try {
            remoteHostIdx = Integer.parseInt(ajaxResponse.getRequestParameter("remoteHostIdx"));
        }
        catch (NumberFormatException e) {
            this.logger.warn("Remote Host Index not found: " + remoteHostIdx);
        }
        JSONObject allCLInspResult = new JSONObject();
        try {
            for (String changeLogId : StringUtil.split(changeLogIds, ";")) {
                SnapshotItem snapshotItem = this.getSnapshotItem(changeLogId);
                this.injectLinks(changeLogId, snapshotItem);
                DataSet linkInspResult = new DataSet();
                this.inspectLinks(changeLogId, snapshotItem, linkInspResult, remoteHostIdx);
                linkInspResult.sort("sdcid,keyid1,keyid2,keyid3,propertytreenodeid");
                String inspStatus = "";
                inspStatus = linkInspResult.findRow("finalstatus", STATUS_FAILED) > -1 ? STATUS_FAILED : STATUS_SUCCESS;
                JSONObject inspResult = new JSONObject();
                inspResult.put("inspStatus", inspStatus);
                inspResult.put("linkTree", snapshotItem.toJSONObject());
                inspResult.put("linkInspResult", linkInspResult.toJSONObject(true, false, false));
                allCLInspResult.put(changeLogId, inspResult);
            }
        }
        catch (JSONException | SapphireException e) {
            this.logger.error("Exception occured when trying to determine Links for ChangeLog: " + changeLogIds, e);
            throw new SapphireException("Exception occured when trying to determine Links:" + e.getMessage());
        }
        ajaxResponse.addCallbackArgument("inspectionResult", allCLInspResult);
    }

    private void doInspectCLsInRemote(AjaxResponse ajaxResponse) throws SapphireException, JSONException {
        if (this.session.changeLogs == null) {
            throw new SapphireException("ChangeLogs not found in Session Cache.");
        }
        String linkItem = ajaxResponse.getRequestParameter("linkItem");
        JSONObject linkItemJsonObj = new JSONObject(linkItem);
        String changeLogId = linkItemJsonObj.get("0").toString();
        String sdcid = linkItemJsonObj.get("1").toString();
        String keyid1 = linkItemJsonObj.get("2").toString();
        String keyid2 = linkItemJsonObj.get("3").toString();
        String keyid3 = linkItemJsonObj.get("4").toString();
        String propertytreenodeid = linkItemJsonObj.get("5").toString();
        String localStatus = linkItemJsonObj.get("6").toString();
        String parentsdc = "";
        String parentkeyid1 = "";
        String parentkeyid2 = "";
        String parentkeyid3 = "";
        if (sdcid.equalsIgnoreCase("LV_AttributeDef") && keyid2.equals("LV_WorksheetItem")) {
            parentsdc = linkItemJsonObj.get("12").toString();
            parentkeyid1 = linkItemJsonObj.get("13").toString();
            parentkeyid2 = linkItemJsonObj.get("14").toString();
            parentkeyid3 = linkItemJsonObj.get("15").toString();
        }
        String index = linkItemJsonObj.get("11").toString();
        String remotestatus = "";
        String remotemessage = "";
        String finalstatus = "";
        this.logger.info("Inspecting linkItem(s): " + linkItem);
        int remoteHostIdx = -1;
        try {
            remoteHostIdx = Integer.parseInt(ajaxResponse.getRequestParameter("remoteHostIdx"));
        }
        catch (NumberFormatException e) {
            this.logger.warn("Remote Host Index not found: " + remoteHostIdx);
        }
        JSONObject allCLInspResult = new JSONObject();
        String inspStatus = "";
        try {
            PropertyList linkItemProps = new PropertyList();
            linkItemProps.setProperty("sdcid", sdcid);
            linkItemProps.setProperty("changeLogId", changeLogId);
            linkItemProps.setProperty("keyid1", keyid1);
            linkItemProps.setProperty("keyid2", keyid2);
            linkItemProps.setProperty("keyid3", keyid3);
            linkItemProps.setProperty("propertytreenodeid", propertytreenodeid);
            linkItemProps.setProperty("localStatus", localStatus);
            linkItemProps.setProperty("parentsdc", parentsdc);
            linkItemProps.setProperty("parentkeyid1", parentkeyid1);
            linkItemProps.setProperty("parentkeyid2", parentkeyid2);
            linkItemProps.setProperty("parentkeyid3", parentkeyid3);
            DataSet linkInspResult = new DataSet();
            PropertyList remoteResp = this.inspectRemotely(linkItemProps, remoteHostIdx);
            InspectionStatus remoteStatus = InspectionStatus.valueOf(remoteResp.getProperty("status"));
            remotemessage = remoteResp.getProperty("message");
            inspStatus = InspectionStatus.NOT_FOUND.equals(localStatus) && (remoteHostIdx > -1 && InspectionStatus.NOT_FOUND.equals((Object)remoteStatus) || remoteHostIdx == -1) ? STATUS_FAILED : STATUS_SUCCESS;
            JSONObject inspResult = new JSONObject();
            inspResult.put("inspStatus", inspStatus);
            inspResult.put("sdcid", sdcid);
            inspResult.put("keyid1", keyid1);
            inspResult.put("keyid2", keyid2);
            inspResult.put("keyid3", keyid3);
            inspResult.put("remotestatus", remoteResp.getProperty("status"));
            inspResult.put("remotemessage", remotemessage);
            inspResult.put("finalstatus", inspStatus);
            inspResult.put("changeLogId", changeLogId);
            inspResult.put("propertytreenodeid", propertytreenodeid);
            inspResult.put("index", index);
            inspResult.put("linkInspResult", linkInspResult.toJSONObject(true, false, false));
            allCLInspResult.put("0", inspResult);
        }
        catch (JSONException e) {
            this.logger.error("Exception occured when trying to determine Links for ChangeLog: " + e);
            throw new SapphireException("Exception occured when trying to determine Links:" + e.getMessage());
        }
        ajaxResponse.addCallbackArgument("inspectionResult", allCLInspResult);
    }

    private void injectLinks(String changeLogId, SnapshotItem snapshotItem) throws SapphireException {
        if (Snapshot.Type.PROPERTYTREE.equals((Object)snapshotItem.getType())) {
            PropertyTreeSnapshotItem pItem = (PropertyTreeSnapshotItem)snapshotItem;
            if (pItem.isRenamed()) {
                String renamedNodeId = pItem.getRenamedNodeId();
                PropertyTreeSnapshotItem linkItem = new PropertyTreeSnapshotItem();
                linkItem.setPropertyTreeId(pItem.getPropertyTreeId());
                linkItem.setNodeId(renamedNodeId);
                linkItem.setIncludedForTransfer(true);
                pItem.addLink(linkItem, SnapshotItem.LinkType.SQL, "RenamedNodeId", null);
            } else if (!pItem.isDeleted()) {
                PropertyTreeSnapshot pSnapshot = (PropertyTreeSnapshot)pItem.getSnapshot();
                if (!("__FULL".equals(pItem.getNodeId()) || "__DEFINITION".equals(pItem.getNodeId()) || "__root".equals(pItem.getNodeId()))) {
                    String extendsNodeId = pSnapshot.getExtendsNodeId();
                    PropertyTreeSnapshotItem linkItem = new PropertyTreeSnapshotItem();
                    linkItem.setPropertyTreeId(pItem.getPropertyTreeId());
                    linkItem.setNodeId(extendsNodeId);
                    linkItem.setIncludedForTransfer(true);
                    pItem.addLink(linkItem, SnapshotItem.LinkType.SQL, "ExtendsNodeId", null);
                }
            }
        }
    }

    private void inspectLinks(String changeLogId, SnapshotItem item, DataSet inspStatus, int remoteHostIdx) throws SapphireException {
        for (SnapshotItem linkItem : item.getLinkItems()) {
            if (linkItem.isIncludedForTransfer()) {
                ++this.index;
                boolean isPropertyTree = Snapshot.Type.PROPERTYTREE.equals((Object)linkItem.getType());
                String propertyTreeNodeId = isPropertyTree ? ((PropertyTreeSnapshotItem)linkItem).getNodeId() : "";
                int newRowInspStatus = inspStatus.addRow();
                inspStatus.setString(newRowInspStatus, "changelogid", changeLogId);
                inspStatus.setString(newRowInspStatus, "sdcid", linkItem.getSDCId());
                inspStatus.setString(newRowInspStatus, "keyid1", linkItem.getKeyId1());
                inspStatus.setString(newRowInspStatus, "keyid2", linkItem.getKeyId2());
                inspStatus.setString(newRowInspStatus, "keyid3", linkItem.getKeyId3());
                inspStatus.setString(newRowInspStatus, "propertytreenodeid", propertyTreeNodeId);
                PropertyList localInspProps = this.inspectLocally(changeLogId, linkItem);
                inspStatus.setString(newRowInspStatus, "localstatus", localInspProps.getProperty("status"));
                inspStatus.setString(newRowInspStatus, "localmessage", localInspProps.getProperty("message"));
                inspStatus.setString(newRowInspStatus, "remotestatus", "Processing");
                inspStatus.setString(newRowInspStatus, "remotemessage", "Processing");
                InspectionStatus localStatus = InspectionStatus.valueOf(localInspProps.getProperty("status"));
                if (InspectionStatus.NOT_FOUND.equals((Object)localStatus)) {
                    inspStatus.setString(newRowInspStatus, "finalstatus", STATUS_FAILED);
                } else {
                    inspStatus.setString(newRowInspStatus, "finalstatus", STATUS_SUCCESS);
                }
                inspStatus.setString(newRowInspStatus, "index", String.valueOf(this.index));
                if (!linkItem.getSDCId().equalsIgnoreCase("LV_AttributeDef") || !linkItem.getKeyId2().equals("LV_WorksheetItem")) continue;
                SDISnapshotItem templinkItem = (SDISnapshotItem)linkItem;
                SDISnapshotItem parent = templinkItem.getParent();
                inspStatus.setString(newRowInspStatus, "parentsdcid", parent.getSDCId());
                inspStatus.setString(newRowInspStatus, "parentkeyid1", parent.getKeyId1());
                inspStatus.setString(newRowInspStatus, "parentkeyid2", parent.getKeyId2());
                inspStatus.setString(newRowInspStatus, "parentkeyid3", parent.getKeyId3());
                continue;
            }
            this.inspectLinks(changeLogId, linkItem, inspStatus, remoteHostIdx);
        }
    }

    private PropertyList inspectLocally(String changelogid, SnapshotItem linkItem) throws SapphireException {
        boolean isPropertyTree = Snapshot.Type.PROPERTYTREE.equals((Object)linkItem.getType());
        String propertyTreeNodeId = isPropertyTree ? ((PropertyTreeSnapshotItem)linkItem).getNodeId() : "";
        String linkItemKey = linkItem.getSDCId() + ";" + linkItem.getKeyId1() + ";" + linkItem.getKeyId2() + ";" + linkItem.getKeyId3() + ";" + propertyTreeNodeId;
        if (!this.session.localCache.containsKey(linkItemKey)) {
            TranslationProcessor tp = this.getTranslationProcessor();
            InspectionStatus status = InspectionStatus.PENDING;
            String message = "Pending Inspection";
            if (isPropertyTree) {
                PropertyTreeSnapshotItem pLinkItem = (PropertyTreeSnapshotItem)linkItem;
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("linksdcid", linkItem.getSDCId());
                findMap.put("linkkeyid1", linkItem.getKeyId1());
                findMap.put("propertytreenodeid", pLinkItem.getNodeId());
                int findRow = this.session.changeLogs.findRow(findMap);
                if (findRow == -1) {
                    findMap.put("propertytreenodeid", "__FULL");
                    if (this.session.changeLogs.findRow(findMap) == -1) {
                        if ("__root".equals(pLinkItem.getNodeId())) {
                            findMap.put("propertytreenodeid", "__DEFINITION");
                            if (this.session.changeLogs.findRow(findMap) == -1) {
                                status = InspectionStatus.NOT_FOUND;
                                message = "Missing in package.";
                            } else {
                                status = InspectionStatus.FOUND;
                                message = "PropertyTree Def found in package.";
                            }
                        } else {
                            status = InspectionStatus.NOT_FOUND;
                            message = "Missing in package.";
                        }
                    } else if (!("__FULL".equals(pLinkItem.getNodeId()) || "__DEFINITION".equals(pLinkItem.getNodeId()) || "__root".equals(pLinkItem.getNodeId()))) {
                        if (pLinkItem.isDeleted()) {
                            status = InspectionStatus.NOT_FOUND;
                            message = "Scheduled for deletion.";
                        } else {
                            PropertyTree tree = pLinkItem.getPropertyTree();
                            Node node = tree.getNode(pLinkItem.getNodeId());
                            if (node == null) {
                                status = InspectionStatus.NOT_FOUND;
                                message = "Missing in package.";
                            } else {
                                status = InspectionStatus.FOUND;
                                message = "Found in package.";
                            }
                        }
                    }
                } else if ("Deleted".equals(this.session.changeLogs.getString(findRow, "changelogstatus"))) {
                    status = InspectionStatus.NOT_FOUND;
                    message = "Scheduled for deletion.";
                } else if ("Renamed".equals(this.session.changeLogs.getString(findRow, "changelogstatus"))) {
                    status = InspectionStatus.NOT_FOUND;
                    message = "Scheduled for renaming.";
                } else {
                    status = InspectionStatus.FOUND;
                    message = "Found in package.";
                }
            } else {
                boolean isVersionedSDC = "Y".equals(this.getSDCProcessor().getProperty(linkItem.getSDCId(), "versionedflag"));
                boolean isCurrentRef = isVersionedSDC && (linkItem.getKeyId2().length() == 0 || "C".equals(linkItem.getKeyId2()));
                HashMap<String, String> findMap = new HashMap<String, String>();
                if (linkItem.getSDCId().equals("LV_AttributeDef")) {
                    SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)linkItem;
                    findMap.put("linksdcid", sdiSnapshotItem.getParent().getSDCId());
                    findMap.put("linkkeyid1", sdiSnapshotItem.getParent().getKeyId1());
                    if (sdiSnapshotItem.getParent().getKeyId2().length() > 0 && !isCurrentRef) {
                        findMap.put("linkkeyid2", sdiSnapshotItem.getParent().getKeyId2());
                        if (sdiSnapshotItem.getParent().getKeyId3().length() > 0) {
                            findMap.put("linkkeyid3", sdiSnapshotItem.getParent().getKeyId3());
                        }
                    }
                } else {
                    findMap.put("linksdcid", linkItem.getSDCId());
                    findMap.put("linkkeyid1", linkItem.getKeyId1());
                    if (linkItem.getKeyId2().length() > 0 && !isCurrentRef) {
                        findMap.put("linkkeyid2", linkItem.getKeyId2());
                    }
                    if (linkItem.getKeyId3().length() > 0) {
                        findMap.put("linkkeyid3", linkItem.getKeyId3());
                    }
                }
                DataSet filterDS = this.session.changeLogs.getFilteredDataSet(findMap);
                if (filterDS.getRowCount() == 0) {
                    status = InspectionStatus.NOT_FOUND;
                    message = "Missing in package.";
                } else {
                    findMap.put("changelogstatus", "Deleted");
                    filterDS = filterDS.getFilteredDataSet(findMap, true);
                    if (filterDS.getRowCount() > 0) {
                        if (isCurrentRef) {
                            status = InspectionStatus.FOUND;
                            message = "A Version is Found in package.";
                        } else {
                            status = InspectionStatus.FOUND;
                            message = "Found in package.";
                        }
                    } else {
                        status = InspectionStatus.NOT_FOUND;
                        message = "Scheduled for deletion.";
                    }
                }
            }
            String message2 = "";
            if (InspectionStatus.NOT_FOUND.equals((Object)status)) {
                boolean isOracle = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).isOracle();
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT changelogid, changerequestid FROM ( SELECT" + (!isOracle ? " TOP 1 " : "") + " changelogid, changerequestid FROM changelog WHERE linksdcid = " + safeSQL.addVar(linkItem.getSDCId()) + " AND linkkeyid1 = " + safeSQL.addVar(linkItem.getKeyId1()) + "" + (linkItem.getKeyId2().length() > 0 ? " AND linkkeyid2 = " + safeSQL.addVar(linkItem.getKeyId2()) : "") + (linkItem.getKeyId3().length() > 0 ? " AND linkkeyid3 = " + safeSQL.addVar(linkItem.getKeyId3()) : "") + ("PropertyTree".equals(linkItem.getSDCId()) ? " AND propertytreenodeid = " + safeSQL.addVar(((PropertyTreeSnapshotItem)linkItem).getNodeId()) : "") + " AND changelogstatus = '" + "Checked In" + "' ORDER BY checkedindt DESC )" + (isOracle ? " WHERE rownum < 2" : "");
                DataSet existingCR = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (existingCR != null && existingCR.getRowCount() > 0) {
                    message2 = "<br/>" + tp.translate("The most recent ChangeLog") + ": <a href='JavaScript:;' onclick=\"crInspectorObj.openCLInList('" + existingCR.getString(0, "changelogid") + "')\">" + existingCR.getString(0, "changelogid") + "</a>" + tp.translate(" is found under ChangeRequest") + ": <a href='JavaScript:;' onclick=\"crInspectorObj.openCRInList('" + existingCR.getString(0, "changerequestid", "") + "')\">" + existingCR.getString(0, "changerequestid", "") + "</a>.";
                }
            }
            PropertyList localInspProps = new PropertyList();
            localInspProps.setProperty("status", status.toString());
            localInspProps.setProperty("message", tp.translate(message) + message2);
            this.session.localCache.put(linkItemKey, localInspProps);
        }
        return (PropertyList)this.session.localCache.get(linkItemKey);
    }

    private PropertyList inspectRemotely(PropertyList props, int remoteHostIdx) {
        if (remoteHostIdx == -1) {
            PropertyList remoteInspProps = new PropertyList();
            remoteInspProps.setProperty("status", STATUS_PENDING);
            remoteInspProps.setProperty("message", "Not Checked");
            return remoteInspProps;
        }
        String linkItemKey = props.getProperty("sdcid") + ";" + props.getProperty("keyid1") + ";" + props.getProperty("keyid2") + ";" + props.getProperty("keyid3") + ";" + props.getProperty("propertytreenodeid") + ";" + remoteHostIdx;
        if (!this.session.remoteCache.containsKey(linkItemKey)) {
            TranslationProcessor tp = this.getTranslationProcessor();
            InspectionStatus status = InspectionStatus.PENDING;
            String message = "Pending Inspection";
            try {
                PropertyList commandProps = new PropertyList();
                if ("LV_AttributeDef".equals(props.getProperty("sdcid"))) {
                    if (props.getProperty("keyid2").equals("LV_WorksheetItem")) {
                        commandProps.setProperty("sdcid", "LV_AttributeDef");
                        commandProps.setProperty("parentsdcid", props.getProperty("parentsdc"));
                        commandProps.setProperty("keyid1", props.getProperty("parentkeyid1"));
                        commandProps.setProperty("keyid2", props.getProperty("parentkeyid2"));
                        commandProps.setProperty("keyid3", props.getProperty("parentkeyid3"));
                        commandProps.setProperty("attributeid", props.getProperty("keyid1"));
                        commandProps.setProperty("attributesdcid", props.getProperty("keyid2"));
                        commandProps.setProperty("attributeinstance", "1");
                    } else {
                        commandProps.setProperty("sdcid", "LV_AttributeDef");
                        commandProps.setProperty("keyid1", props.getProperty("keyid1"));
                        commandProps.setProperty("keyid2", props.getProperty("keyid2"));
                    }
                } else {
                    commandProps.setProperty("sdcid", props.getProperty("sdcid"));
                    commandProps.setProperty("keyid1", props.getProperty("keyid1"));
                    commandProps.setProperty("keyid2", props.getProperty("keyid2"));
                    commandProps.setProperty("keyid3", props.getProperty("keyid3"));
                    commandProps.setProperty("propertytreenodeid", props.getProperty("propertytreenodeid"));
                }
                PropertyList returnPL = this.callCMTExthandler(remoteHostIdx, "COMMAND_INSPECTSDI", commandProps);
                if ("Y".equals(returnPL.getProperty("found"))) {
                    status = InspectionStatus.FOUND;
                    message = "Found in remote server";
                } else {
                    status = InspectionStatus.NOT_FOUND;
                    message = "Missing in remote server";
                }
            }
            catch (SapphireException e) {
                status = InspectionStatus.NOT_FOUND;
                message = e.getMessage();
                this.logger.error("Exception when checking for SDI '" + linkItemKey + "' in remote server index: " + remoteHostIdx, e);
            }
            PropertyList temp = new PropertyList();
            temp.setProperty("status", status.toString());
            temp.setProperty("message", tp.translate(message));
            this.session.remoteCache.put(linkItemKey, temp);
        }
        return (PropertyList)this.session.remoteCache.get(linkItemKey);
    }

    private PropertyList callCMTExthandler(int remoteHostIdx, String command, PropertyList commandProps) throws SapphireException {
        try {
            if (remoteHostIdx == -1) {
                throw new SapphireException("Invalid Host: " + remoteHostIdx);
            }
            PropertyListCollection hosts = this.getRemoteHosts();
            if (remoteHostIdx > hosts.size()) {
                throw new SapphireException("Invalid Host: " + remoteHostIdx);
            }
            PropertyList host = this.getRemoteHosts().getPropertyList(remoteHostIdx);
            if (!STATUS_SUCCESS.equals(host.getProperty("status")) && !"COMMAND_CHECK_CONNECTION".equals(command)) {
                throw new SapphireException("Remote Host not accessible due to: " + host.getProperty("statusMessage"));
            }
            String hostURL = host.getProperty("url");
            String authToken = host.getProperty("authtoken");
            ExternalHandlerProcessor externalHandlerProcessor = new ExternalHandlerProcessor(authToken, hostURL);
            PropertyList returnPL = externalHandlerProcessor.sendCommandToLIMS(command, commandProps);
            return returnPL;
        }
        catch (SapphireException e) {
            String error = e.getMessage();
            String str = "Unable to validate token";
            int index = error.indexOf(str);
            if (index > -1) {
                error = error.substring(0, index + str.length());
            }
            this.logError("Exception when trying to Perform Command '" + command + "' in Remote Server Idx: " + remoteHostIdx, e);
            throw new SapphireException(error);
        }
    }

    private SnapshotItem getSnapshotItem(String changeLogId) throws SapphireException {
        if (!this.session.snapshotCache.containsKey(changeLogId)) {
            List<SnapshotItem> snapshotItems;
            Snapshot snapshot;
            SnapshotItem snapshotItem = null;
            Optional<List<SnapshotItem>> snapshotItemsOpt = CMTUtil.getChangeLogSnapshotItem(changeLogId, this.getConnectionId());
            if (snapshotItemsOpt.isPresent() && (snapshot = (snapshotItem = (snapshotItems = snapshotItemsOpt.get()).get(0)).getSnapshot()) != null) {
                SnapshotFactory snapshotFactory = new SnapshotFactory(this.getConnectionId());
                snapshotFactory.generateSnapshotLinkTree(snapshot);
            }
            this.session.snapshotCache.put(changeLogId, snapshotItem);
        }
        return (SnapshotItem)this.session.snapshotCache.get(changeLogId);
    }

    private PropertyListCollection getRemoteHosts() {
        if (this.session.remoteHosts == null) {
            CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "LV_ChangeRequest");
            PropertyListCollection hosts = cmtPolicy.getHosts();
            for (int i = hosts.size() - 1; i >= 0; --i) {
                String hostURL;
                PropertyList host = hosts.getPropertyList(i);
                if (host.getProperty("alias").length() == 0 || "N".equals(host.getProperty("allowforcrinspection", "N"))) {
                    hosts.remove(i);
                }
                if (!(hostURL = host.getProperty("url")).endsWith("/sc")) {
                    hostURL = hostURL + "/sc";
                }
                host.setProperty("url", hostURL);
                host.setProperty("status", STATUS_PENDING);
                host.setProperty("statusMessage", "Remote Test Pending.");
            }
            this.session.remoteHosts = hosts;
        }
        return this.session.remoteHosts;
    }

    private PropertyList getCRIPolicyProps() {
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "LV_ChangeRequest");
        PropertyList defaultProps = cmtPolicy.getDefaultPolicyPropertyList();
        PropertyList changeRequestProps = defaultProps.getPropertyList("changerequest");
        PropertyList criProps = changeRequestProps.getPropertyList("crinspectorprops");
        return criProps;
    }

    private synchronized CRISession createCRISession() {
        final ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        CRISession criSession = new CRISession();
        criSession.maxLifeMillis = CRI_SESSION_LIFE_MAX_MILLIS_ALL.getOrDefault(connectionInfo.getDatabaseId(), 60000L);
        this.sessionCache.put(criSession.id, criSession);
        this.logger.info("createCRISession: New Session created: " + criSession.id);
        if (!CRI_TIMER_RUNNING) {
            this.logger.info("createNewCRInspectorSession: Cleaner task is Not running. Re-initiate.");
            long delay = CRI_TIMER_INTERVAL_MILLIS_ALL.getOrDefault(connectionInfo.getDatabaseId(), 60000L);
            TimerTask task = new TimerTask(){

                @Override
                public void run() {
                    try {
                        for (String httpSessionId : ALL_SESSION_CACHES.keySet()) {
                            Map criCache = (Map)ALL_SESSION_CACHES.get(httpSessionId);
                            Set sessions = criCache.keySet();
                            for (String sessionId : sessions) {
                                if (((CRISession)criCache.get(sessionId)).isDeletable()) {
                                    CRIAjaxUtil.this.logger.info("CRITimer: Checking CR Inspector session: " + sessionId + ". Outdated. Cleaning from cache.");
                                    criCache.remove(sessionId);
                                    continue;
                                }
                                CRIAjaxUtil.this.logger.info("CRITimer: Checking CR Inspector session: " + sessionId + ". Ok.");
                            }
                            if (criCache.size() > 0) {
                                CRIAjaxUtil.this.logger.info("CRITimer: More CRI Sessions still exists for this user " + connectionInfo.getSysuserId() + ": " + criCache.size());
                                continue;
                            }
                            ALL_SESSION_CACHES.remove(httpSessionId);
                            CRIAjaxUtil.this.logger.info("CRITimer: No More Session Cache found for this user. Remove CRI Caches.");
                        }
                        if (ALL_SESSION_CACHES.size() > 0) {
                            CRIAjaxUtil.this.logger.info("CRITimer: More Session Cache still exists: " + ALL_SESSION_CACHES.size());
                        } else {
                            CRIAjaxUtil.this.logger.info("CRITimer: No More Session Cache found for any user. Cancel Timer.");
                            CRI_TIMER_RUNNING = false;
                            CRI_TIMER.cancel();
                        }
                    }
                    catch (Exception e) {
                        CRIAjaxUtil.this.logger.error("CRITimer: Exception during TimerTask Run: ", e);
                    }
                }
            };
            CRI_TIMER = new Timer();
            CRI_TIMER.scheduleAtFixedRate(task, delay, delay);
            CRI_TIMER_RUNNING = true;
        }
        return criSession;
    }

    private CRISession getCRISession(String sessionId) throws SapphireException {
        CRISession criSession = this.sessionCache.get(sessionId);
        if (criSession == null) {
            throw new SapphireException("Session not found: " + sessionId);
        }
        return criSession;
    }

    private Map<String, CRISession> getUserCRICache(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        ConcurrentHashMap<String, CRISession> criCache = (ConcurrentHashMap<String, CRISession>)httpSession.getAttribute(CRI_CACHE_NAME);
        if (criCache == null) {
            this.logger.info("Creating new CRI Session Cache for User.");
            criCache = new ConcurrentHashMap<String, CRISession>();
            httpSession.setAttribute(CRI_CACHE_NAME, criCache);
        } else {
            this.logger.info("User CRI Session Cache found. No. of Cache Items: " + criCache.size());
        }
        ALL_SESSION_CACHES.put(httpSession.getId(), criCache);
        this.sessionCache = (Map)httpSession.getAttribute(CRI_CACHE_NAME);
        return criCache;
    }

    static enum InspectionStatus {
        PENDING,
        FOUND,
        NOT_FOUND;

    }

    class CRISession
    implements Serializable {
        String id;
        long lastPingMillis;
        long maxLifeMillis = 60000L;
        private DataSet changeLogs;
        private PropertyListCollection remoteHosts;
        private Map<String, PropertyList> localCache = new HashMap<String, PropertyList>();
        private Map<String, PropertyList> remoteCache = new HashMap<String, PropertyList>();
        private Map<String, SnapshotItem> snapshotCache = new HashMap<String, SnapshotItem>();

        CRISession() {
            this(String.valueOf(UUID.randomUUID()));
        }

        CRISession(String id) {
            this.id = id;
            this.lastPingMillis = System.currentTimeMillis();
        }

        void ping() {
            this.lastPingMillis = System.currentTimeMillis();
        }

        boolean isDeletable() {
            return System.currentTimeMillis() - this.lastPingMillis > this.maxLifeMillis;
        }
    }
}

