/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.policy;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.ConfigTransferOption;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.SnapshotItem;

public class CMTPolicy
extends BaseCustom {
    private static final String LOGNAME = "CMTPolicy";
    private static final String CMTPolicyNode_CacheName = "CMTPolicyNode";
    public static final String POLICY_ID = "CMTPolicy";
    public static final String DEFAULT_POLICY_NODE = "Sapphire Custom";
    public static final String CHECKEDOUTTOME_IMAGE = "WEB-CORE/images/svg/checkout_user.svg";
    public static final String CHECKEDOUTTOOTHER_IMAGE = "WEB-CORE/images/svg/checkout_others.svg";
    public static final String CHECKEDOUTTOMYDEPT_IMAGE = "WEB-CORE/images/svg/checkout_dept.svg";
    public static final String CHECKEDOUTTOOTHERDEPT_IMAGE = "WEB-CORE/images/svg/checkout_others_dept.svg";
    public static final String POLICY_TRANSFER_OPTIONS = "transferoption";
    public static final String POLICY_TRANSFER_OPTIONS_ALT_IDENTIFIER = "identifycolumn";
    public static final String IMPORT_OPTION_IMPORT = "Import";
    public static final String IMPORT_OPTION_DO_NOT_IMPORT = "Do Not Import";
    public static final String IMPORT_OPTION_OVERRIDE = "Override Existing";
    public static final String IMPORT_OPTION_MOVE_OVERRIDE = "Move & Override Existing";
    public static final String IMPORT_OPTION_DELETE = "Delete";
    public static final String IMPORT_OPTION_DO_NOT_DELETE = "Do Not Delete";
    public static final String IMPORT_OPTION_IGNORE_DELETE = "Ignore Delete";
    public static final String IMPORT_OPTION_RENAME = "Rename";
    public static final String IMPORT_OPTION_IGNORE_IF_EXIST = "Ignore If Exists";
    public static final String IMPORT_OPTION_REGENERATE_KEY = "Regenerate Auto Key";
    public static final String IMPORT_VERSIONED_OPTION_CREATE_NEW = "Create New Version";
    public static final String IMPORT_VERSIONED_OPTION_OVERRIDE_PROVISIONAL = "Override If Provisional";
    public static final String IMPORT_VERSIONED_OPTION_USER_CHOICE = "Allow User To Choose";
    public static final String IMPORT_VERSIONED_OPTION_OVERRIDE_AND_EXPIRE = "Override and Expire Version";
    public static final String CERTIFICATIONS_OWNER_RESOURCESDC = "resourcesdc";
    public static final String CERTIFICATIONS_OWNER_CERTIFIEDFORSDC = "certifiedforsdc";
    private PropertyList policyPropertyList = null;
    private PropertyList defaultPolicyPropertyList = null;
    private String databaseid = null;
    private String sdcid = null;
    private String requestedPolicyNodeId = null;
    private String actualPolicyNodeId = null;
    private String changeControlledFlag = null;
    private String policyValueTreeXML = null;
    private String policyDefTreeXML = null;
    private Map<String, PropertyList> policyNodeMap = null;

    private CMTPolicy() {
    }

    public CMTPolicy(PropertyList policyPropertyList) {
        this.policyPropertyList = policyPropertyList;
    }

    public static CMTPolicy getPolicy(String connectionid, String sdcid) {
        return CMTPolicy.getPolicy(connectionid, sdcid, sdcid);
    }

    public static CMTPolicy getPolicy(String connectionid, String sdcid, String nodeid) {
        return CMTPolicy.getPolicy(null, connectionid, sdcid, nodeid);
    }

    public static CMTPolicy getPolicy(File rakFile, String connectionid, String sdcid, String nodeid) {
        return CMTPolicy.getPolicy(rakFile, connectionid, sdcid, nodeid, "", "");
    }

    public static CMTPolicy getPolicy(File rakFile, String connectionid, String sdcid, String nodeid, String policyValueTreeXML, String policyDefTreeXML) {
        return CMTPolicy.getPolicy(rakFile, connectionid, sdcid, nodeid, policyValueTreeXML, policyDefTreeXML, null);
    }

    public static CMTPolicy getPolicy(File rakFile, String connectionid, String sdcid, String nodeid, Map<String, PropertyList> policyNodeMap) {
        return CMTPolicy.getPolicy(rakFile, connectionid, sdcid, nodeid, null, null, policyNodeMap);
    }

    public static CMTPolicy getSDISnapshotItemPolicy(String connectionid, SDISnapshotItem item) throws SapphireException {
        PropertyList linkTransferPL;
        PropertyList linkprops = item.getParentLinkProps();
        PropertyList nodeprops = item.getPolicyNodeProps();
        if (nodeprops == null) {
            nodeprops = linkprops;
        } else if (linkprops != null && (linkTransferPL = linkprops.getPropertyList(POLICY_TRANSFER_OPTIONS)) != null) {
            if (linkprops.getProperty("flush").length() > 0) {
                nodeprops.getPropertyList(POLICY_TRANSFER_OPTIONS).setProperty("flush", linkprops.getProperty("flush"));
            }
            if (linkprops.getProperty("importoption").length() > 0) {
                nodeprops.getPropertyList(POLICY_TRANSFER_OPTIONS).setProperty("importoption", linkprops.getProperty("importoption"));
            }
        }
        CMTPolicy itemPolicy = nodeprops != null ? new CMTPolicy(nodeprops) : CMTPolicy.getPolicy(connectionid, item.getSDCId());
        return itemPolicy;
    }

    private static CMTPolicy getPolicy(File rakFile, String connectionid, String sdcid, String nodeid, String policyValueTreeXML, String policyDefTreeXML, Map<String, PropertyList> policyNodeMap) {
        String reqPolicyNodeId;
        String databaseid;
        PropertyList defaultPropertyList;
        PropertyTree propertyTree = null;
        if (policyValueTreeXML != null && policyValueTreeXML.length() > 0) {
            try {
                propertyTree = new PropertyTree("CMTPolicy");
                propertyTree.setValueXML(policyValueTreeXML);
                propertyTree.setDefinitionXML(policyDefTreeXML);
            }
            catch (SapphireException e) {
                propertyTree = null;
                e.printStackTrace();
            }
        }
        boolean useProvidedPolicyData = false;
        if (propertyTree != null || policyNodeMap != null && policyNodeMap.size() > 0) {
            useProvidedPolicyData = true;
        }
        if ((defaultPropertyList = (PropertyList)CacheUtil.get(databaseid = SecurityService.getDatabaseId(connectionid), CMTPolicyNode_CacheName, databaseid + ";;" + DEFAULT_POLICY_NODE)) == null || useProvidedPolicyData) {
            try {
                if (propertyTree != null) {
                    defaultPropertyList = propertyTree.getNodePropertyList(DEFAULT_POLICY_NODE, true);
                } else if (policyNodeMap != null) {
                    defaultPropertyList = policyNodeMap.get(DEFAULT_POLICY_NODE);
                } else {
                    ConfigurationProcessor configProcessor = rakFile != null ? new ConfigurationProcessor(rakFile, connectionid) : new ConfigurationProcessor(connectionid);
                    defaultPropertyList = configProcessor.getPolicy("CMTPolicy", DEFAULT_POLICY_NODE);
                    assert (defaultPropertyList != null);
                }
                CMTPolicy.sanitizePolicyValues(defaultPropertyList, true, "", defaultPropertyList);
                if (!useProvidedPolicyData) {
                    CacheUtil.put(databaseid, CMTPolicyNode_CacheName, databaseid + ";;" + DEFAULT_POLICY_NODE, defaultPropertyList);
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to retrieve default node of CMTPolicy", e);
            }
        }
        HashMap nodePL = null;
        String actualPolicyNodeId = reqPolicyNodeId = nodeid;
        if (CacheUtil.get(databaseid, CMTPolicyNode_CacheName, databaseid + ";" + sdcid + ";" + reqPolicyNodeId) == null || useProvidedPolicyData) {
            ConfigurationProcessor configProcessor = rakFile != null ? new ConfigurationProcessor(rakFile, connectionid) : new ConfigurationProcessor(connectionid);
            try {
                if (reqPolicyNodeId.length() > 0) {
                    if (reqPolicyNodeId.endsWith(" Custom")) {
                        actualPolicyNodeId = reqPolicyNodeId;
                        if (propertyTree != null) {
                            try {
                                nodePL = propertyTree.getNodePropertyList(actualPolicyNodeId, true);
                            }
                            catch (SapphireException e) {
                                Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                            }
                        } else if (policyNodeMap != null) {
                            nodePL = policyNodeMap.get(actualPolicyNodeId);
                        } else {
                            try {
                                nodePL = configProcessor.getPolicy("CMTPolicy", actualPolicyNodeId);
                            }
                            catch (SapphireException e) {
                                Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                            }
                        }
                    } else {
                        actualPolicyNodeId = reqPolicyNodeId;
                        if (propertyTree != null) {
                            try {
                                nodePL = propertyTree.getNodePropertyList(actualPolicyNodeId, true);
                            }
                            catch (SapphireException e) {
                                Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                            }
                        } else if (policyNodeMap != null) {
                            nodePL = policyNodeMap.get(actualPolicyNodeId);
                        } else {
                            try {
                                nodePL = configProcessor.getPolicy("CMTPolicy", actualPolicyNodeId);
                            }
                            catch (SapphireException e) {
                                Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                            }
                        }
                        if (nodePL == null || nodePL.isEmpty()) {
                            actualPolicyNodeId = reqPolicyNodeId + " Custom";
                            if (propertyTree != null) {
                                try {
                                    nodePL = propertyTree.getNodePropertyList(actualPolicyNodeId, true);
                                }
                                catch (SapphireException e) {
                                    Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                                }
                            } else if (policyNodeMap != null) {
                                nodePL = policyNodeMap.get(actualPolicyNodeId);
                            } else {
                                try {
                                    nodePL = configProcessor.getPolicy("CMTPolicy", actualPolicyNodeId);
                                }
                                catch (SapphireException e) {
                                    Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                                }
                            }
                        }
                        if (nodePL == null || nodePL.isEmpty()) {
                            actualPolicyNodeId = reqPolicyNodeId + " Comp Custom";
                            if (propertyTree != null) {
                                try {
                                    nodePL = propertyTree.getNodePropertyList(actualPolicyNodeId, true);
                                }
                                catch (SapphireException e) {
                                    Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                                }
                            } else if (policyNodeMap != null) {
                                nodePL = policyNodeMap.get(actualPolicyNodeId);
                            } else {
                                try {
                                    nodePL = configProcessor.getPolicy("CMTPolicy", actualPolicyNodeId);
                                }
                                catch (SapphireException e) {
                                    Trace.logInfo(actualPolicyNodeId + " Node not found.", e);
                                }
                            }
                        }
                    }
                }
                if (nodePL == null || nodePL.size() == 0) {
                    actualPolicyNodeId = DEFAULT_POLICY_NODE;
                    nodePL = defaultPropertyList.copy();
                }
                if (policyNodeMap == null && sdcid != null && sdcid.length() >= 0) {
                    SDCProcessor sdcProcessor = rakFile != null ? new SDCProcessor(rakFile, connectionid) : new SDCProcessor(connectionid);
                    CMTPolicy.checkIncludeAllDetails(sdcid, (PropertyList)nodePL, sdcProcessor);
                }
                if (nodePL != null && nodePL.size() > 0) {
                    CMTPolicy.sanitizePolicyValues((PropertyList)nodePL, false, sdcid, defaultPropertyList);
                    ((PropertyList)nodePL).setProperty("__actualnodeid", actualPolicyNodeId);
                    if (!useProvidedPolicyData) {
                        CacheUtil.put(databaseid, CMTPolicyNode_CacheName, databaseid + ";" + sdcid + ";" + reqPolicyNodeId, nodePL);
                    }
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to retrieve the node of CMTPolicy:" + reqPolicyNodeId, e);
            }
        } else {
            nodePL = (PropertyList)CacheUtil.get(databaseid, CMTPolicyNode_CacheName, databaseid + ";" + sdcid + ";" + reqPolicyNodeId);
            actualPolicyNodeId = ((PropertyList)nodePL).getProperty("__actualnodeid");
        }
        CMTPolicy cmtPolicy = new CMTPolicy();
        cmtPolicy.sdcid = sdcid == null || sdcid.length() == 0 ? ((PropertyList)nodePL).getProperty("sdcid") : sdcid;
        cmtPolicy.databaseid = databaseid;
        cmtPolicy.requestedPolicyNodeId = reqPolicyNodeId;
        cmtPolicy.actualPolicyNodeId = actualPolicyNodeId;
        cmtPolicy.policyPropertyList = nodePL;
        cmtPolicy.defaultPolicyPropertyList = defaultPropertyList;
        cmtPolicy.setRakFile(rakFile);
        cmtPolicy.setConnectionId(connectionid);
        cmtPolicy.logger.setLoggerName("CMTPolicy");
        cmtPolicy.policyValueTreeXML = policyValueTreeXML;
        cmtPolicy.policyDefTreeXML = policyDefTreeXML;
        cmtPolicy.policyNodeMap = policyNodeMap;
        return cmtPolicy;
    }

    private static void sanitizePolicyValues(PropertyList nodePL, boolean isDefaultNode, String sdcId, PropertyList defaultNodePL) throws SapphireException {
        if ("SDC".equalsIgnoreCase(sdcId) && !isDefaultNode) {
            CMTPolicy.setupSDCSDCNodeProps(nodePL);
        }
        PropertyListCollection embeddedSDIsPLC = nodePL.getCollectionNotNull("sdidatasets");
        nodePL.setProperty("sdidatasets", embeddedSDIsPLC);
        for (int i = embeddedSDIsPLC.size() - 1; i >= 0; --i) {
            PropertyList props = embeddedSDIsPLC.getPropertyList(i);
            String enabledFlag = props.getProperty("enabled", "Y");
            String linkType = props.getProperty("linktype", "");
            String linkInfo = "";
            try {
                linkInfo = CMTPolicy.getAssociatedSDILinkInfo(props);
            }
            catch (SapphireException e) {
                linkInfo = "";
            }
            if ("Y".equals(enabledFlag) && linkType.length() != 0 && linkInfo.length() != 0) continue;
            embeddedSDIsPLC.remove(i);
        }
        PropertyListCollection transferSDIsPLC = nodePL.getCollectionNotNull("adhoctransfersdidatasets");
        nodePL.setProperty("adhoctransfersdidatasets", transferSDIsPLC);
        for (int i = transferSDIsPLC.size() - 1; i >= 0; --i) {
            PropertyList props = transferSDIsPLC.getPropertyList(i);
            String enabledFlag = props.getProperty("enabled", "Y");
            String linkType = props.getProperty("linktype", "");
            String linkInfo = "";
            try {
                linkInfo = CMTPolicy.getAssociatedSDILinkInfo(props);
            }
            catch (SapphireException e) {
                linkInfo = "";
            }
            if ("Y".equals(enabledFlag) && linkType.length() != 0 && linkInfo.length() != 0) continue;
            transferSDIsPLC.remove(i);
        }
        PropertyListCollection detailTablesPLC = nodePL.getCollectionNotNull("detaildatasets");
        nodePL.setProperty("detaildatasets", detailTablesPLC);
        for (int i = detailTablesPLC.size() - 1; i >= 0; --i) {
            PropertyList props = detailTablesPLC.getPropertyList(i);
            String enabledFlag = props.getProperty("enabled", "Y");
            String tableId = props.getProperty("table", "");
            if (!"Y".equals(enabledFlag) || tableId.length() == 0) {
                detailTablesPLC.remove(i);
                continue;
            }
            PropertyListCollection detailFKLinksPLC = props.getCollectionNotNull("links");
            for (int j = detailFKLinksPLC.size() - 1; j >= 0; --j) {
                PropertyList linkProps = detailFKLinksPLC.getPropertyList(j);
                String enabledLinkFlag = linkProps.getProperty("enabled", "Y");
                String linkInfo = "";
                try {
                    linkInfo = CMTPolicy.getDetailFKLinkInfo(linkProps);
                }
                catch (SapphireException e) {
                    linkInfo = "";
                }
                if ("Y".equals(enabledLinkFlag) && linkInfo.length() != 0) continue;
                detailFKLinksPLC.remove(j);
            }
        }
        PropertyListCollection sdiDetailTablesPLC = nodePL.getCollectionNotNull("sdidetaildatasets");
        nodePL.setProperty("sdidetaildatasets", sdiDetailTablesPLC);
        if (!isDefaultNode && sdcId != null && sdcId.length() > 0) {
            String certificationsOwner;
            String certifOwnerLinkBy;
            PropertyList sdiCertProps = sdiDetailTablesPLC.find("table", "s_sdicertification");
            if (sdiCertProps == null) {
                sdiCertProps = new PropertyList();
                sdiDetailTablesPLC.add(sdiCertProps);
                sdiCertProps.setProperty("enabled", "Y");
                sdiCertProps.setProperty("table", "s_sdicertification");
                sdiCertProps.setProperty("includeintransfer", "Y");
            }
            if ((certifOwnerLinkBy = CMTPolicy.getCertificationOwnerLinkCol(certificationsOwner = CMTPolicy.getCertificationsOwner(defaultNodePL), sdcId)).length() == 0) {
                sdiCertProps.setProperty("enabled", "N");
            } else {
                sdiCertProps.setProperty("retrieveby", certifOwnerLinkBy);
            }
        }
        for (int i = sdiDetailTablesPLC.size() - 1; i >= 0; --i) {
            PropertyList props = sdiDetailTablesPLC.getPropertyList(i);
            String enabledFlag = props.getProperty("enabled", "Y");
            String tableId = props.getProperty("table", "");
            if (!"Y".equals(enabledFlag) || tableId.length() == 0) {
                sdiDetailTablesPLC.remove(i);
                continue;
            }
            PropertyListCollection detailFKLinksPLC = props.getCollectionNotNull("links");
            for (int j = detailFKLinksPLC.size() - 1; j >= 0; --j) {
                PropertyList linkProps = detailFKLinksPLC.getPropertyList(j);
                String enabledLinkFlag = linkProps.getProperty("enabled", "Y");
                String linkAlias = linkProps.getProperty("linkalias", "");
                String linkInfo = "";
                try {
                    linkInfo = CMTPolicy.getDetailFKLinkInfo(linkProps);
                }
                catch (SapphireException e) {
                    linkInfo = "";
                }
                if (!"Y".equals(enabledLinkFlag)) {
                    detailFKLinksPLC.remove(j);
                    continue;
                }
                if (linkInfo.length() != 0) continue;
                if ("categoryitem".equalsIgnoreCase(tableId) && "Category".equals(linkAlias)) {
                    linkProps.setProperty("linkid_fk", "Category;category");
                    continue;
                }
                if ("sdiattribute".equalsIgnoreCase(tableId) && "Attribute Def".equals(linkAlias)) {
                    linkProps.setProperty("linkid_fk", "LV_AttributeDef;attributedef");
                    continue;
                }
                detailFKLinksPLC.remove(j);
            }
        }
    }

    public static String getCertificationOwnerLinkCol(String globalCertifOwner, String sdcId) {
        String certifOwnerLinkCol = "";
        if ("User".equals(globalCertifOwner)) {
            if ("User".equals(sdcId)) {
                certifOwnerLinkCol = CERTIFICATIONS_OWNER_RESOURCESDC;
            }
        } else if ("Certified For SDI".equals(globalCertifOwner) && !"User".equals(sdcId)) {
            certifOwnerLinkCol = CERTIFICATIONS_OWNER_CERTIFIEDFORSDC;
        }
        if ("Instrument".equals(sdcId)) {
            certifOwnerLinkCol = CERTIFICATIONS_OWNER_RESOURCESDC;
        }
        return certifOwnerLinkCol;
    }

    private static void setupSDCSDCNodeProps(PropertyList nodePL) {
        PropertyListCollection sdcDetailTablesPLC = nodePL.getCollectionNotNull("detaildatasets");
        nodePL.setProperty("detaildatasets", sdcDetailTablesPLC);
        PropertyList detailTableProps = sdcDetailTablesPLC.find("table", "sdcattributedef", true);
        if (detailTableProps == null) {
            detailTableProps = new PropertyList();
            sdcDetailTablesPLC.add(detailTableProps);
        }
        detailTableProps.setProperty("enabled", "Y");
        detailTableProps.setProperty("table", "sdcattributedef");
        detailTableProps.setProperty("includeintransfer", "Y");
        PropertyListCollection fkLinks = new PropertyListCollection();
        PropertyList fkLinkProps = new PropertyList();
        fkLinkProps.setProperty("enabled", "Y");
        fkLinkProps.setProperty("linkalias", "Editor Styles");
        fkLinkProps.setProperty("linkid_fk", "LV_EditorStyle;editorstyle");
        fkLinkProps.setProperty("refpolicynodeid", "LV_EditorStyle Custom");
        fkLinks.add(fkLinkProps);
        fkLinkProps = new PropertyList();
        fkLinkProps.setProperty("enabled", "Y");
        fkLinkProps.setProperty("linkalias", "Ref Types");
        fkLinkProps.setProperty("linkid_fk", "RefType;edit reftype");
        fkLinkProps.setProperty("refpolicynodeid", "RefType");
        fkLinks.add(fkLinkProps);
        detailTableProps.setProperty("links", fkLinks);
        detailTableProps = sdcDetailTablesPLC.find("table", "sdclink", true);
        if (detailTableProps == null) {
            detailTableProps = new PropertyList();
            sdcDetailTablesPLC.add(detailTableProps);
        }
        detailTableProps.setProperty("enabled", "Y");
        detailTableProps.setProperty("table", "sdclink");
        detailTableProps.setProperty("includeintransfer", "Y");
        fkLinks = new PropertyListCollection();
        fkLinkProps = new PropertyList();
        fkLinkProps.setProperty("enabled", "Y");
        fkLinkProps.setProperty("linkalias", "Ref Types");
        fkLinkProps.setProperty("linkid_fk", "RefType;Link Reftype");
        fkLinkProps.setProperty("refpolicynodeid", "RefType");
        fkLinks.add(fkLinkProps);
        detailTableProps.setProperty("links", fkLinks);
        detailTableProps = sdcDetailTablesPLC.find("table", "sdcdetaillink", true);
        if (detailTableProps == null) {
            detailTableProps = new PropertyList();
            sdcDetailTablesPLC.add(detailTableProps);
        }
        detailTableProps.setProperty("enabled", "Y");
        detailTableProps.setProperty("table", "sdcdetaillink");
        detailTableProps.setProperty("includeintransfer", "Y");
        fkLinks = new PropertyListCollection();
        fkLinkProps = new PropertyList();
        fkLinkProps.setProperty("enabled", "Y");
        fkLinkProps.setProperty("linkalias", "Ref Types");
        fkLinkProps.setProperty("linkid_fk", "RefType;Detail Link Reftype");
        fkLinkProps.setProperty("refpolicynodeid", "RefType");
        fkLinks.add(fkLinkProps);
        detailTableProps.setProperty("links", fkLinks);
        PropertyListCollection sdcSDIxxxTablesPLC = nodePL.getCollectionNotNull("sdidetaildatasets");
        for (int i = 0; i < sdcSDIxxxTablesPLC.size(); ++i) {
            PropertyList sdixxxTableProps = sdcSDIxxxTablesPLC.getPropertyList(i);
            if ("categoryitem".equalsIgnoreCase(sdixxxTableProps.getProperty("table"))) {
                sdixxxTableProps.setProperty("enabled", "Y");
                sdixxxTableProps.setProperty("includeintransfer", "Y");
                continue;
            }
            sdixxxTableProps.setProperty("enabled", "N");
        }
        PropertyListCollection sdcAdhocSDIPLC = nodePL.getCollectionNotNull("adhoctransfersdidatasets");
        nodePL.setProperty("adhoctransfersdidatasets", sdcAdhocSDIPLC);
        PropertyList adhocSDIProps = new PropertyList();
        adhocSDIProps.setProperty("enabled", "Y");
        adhocSDIProps.setProperty("linkalias", "Security Sets");
        adhocSDIProps.setProperty("linktype", SnapshotItem.LinkType.FK.getCode());
        adhocSDIProps.setProperty("linkid_fk", "LV_SecuritySet;Default Securityset");
        adhocSDIProps.setProperty("refpolicynodeid", "LV_SecuritySet");
        PropertyList transferProps = new PropertyList();
        transferProps.setProperty("flush", "N");
        transferProps.setProperty("importoption", IMPORT_OPTION_IGNORE_IF_EXIST);
        adhocSDIProps.setProperty(POLICY_TRANSFER_OPTIONS, transferProps);
        sdcAdhocSDIPLC.add(adhocSDIProps);
        adhocSDIProps = new PropertyList();
        adhocSDIProps.setProperty("enabled", "Y");
        adhocSDIProps.setProperty("linkalias", "Approval Types");
        adhocSDIProps.setProperty("linktype", SnapshotItem.LinkType.FK.getCode());
        adhocSDIProps.setProperty("linkid_fk", "ApprovalType;Version ApprovalType");
        adhocSDIProps.setProperty("refpolicynodeid", "ApprovalType Custom");
        transferProps = new PropertyList();
        transferProps.setProperty("flush", "N");
        transferProps.setProperty("importoption", IMPORT_OPTION_IGNORE_IF_EXIST);
        adhocSDIProps.setProperty(POLICY_TRANSFER_OPTIONS, transferProps);
        sdcAdhocSDIPLC.add(adhocSDIProps);
        adhocSDIProps = new PropertyList();
        adhocSDIProps.setProperty("enabled", "Y");
        adhocSDIProps.setProperty("linkalias", "Reason RefType");
        adhocSDIProps.setProperty("linktype", SnapshotItem.LinkType.FK.getCode());
        adhocSDIProps.setProperty("linkid_fk", "RefType;RefType");
        adhocSDIProps.setProperty("refpolicynodeid", "RefType");
        transferProps = new PropertyList();
        transferProps.setProperty("flush", "N");
        transferProps.setProperty("importoption", IMPORT_OPTION_IGNORE_IF_EXIST);
        adhocSDIProps.setProperty(POLICY_TRANSFER_OPTIONS, transferProps);
        sdcAdhocSDIPLC.add(adhocSDIProps);
    }

    private static void checkIncludeAllDetails(String sdcid, PropertyList nodePL, SDCProcessor sdcProcessor) {
        if ("Y".equals(nodePL.getProperty("includealldetail"))) {
            PropertyListCollection detaildatasets = nodePL.getCollection("detaildatasets");
            HashSet<PropertyList> exludedSet = new HashSet<PropertyList>();
            HashSet<String> excludedtableSet = new HashSet<String>();
            if (detaildatasets == null) {
                detaildatasets = new PropertyListCollection();
                nodePL.setProperty("detaildatasets", detaildatasets);
            } else {
                for (int i = 0; i < detaildatasets.size(); ++i) {
                    if (!"N".equals(detaildatasets.getPropertyList(i).getProperty("enabled")) && detaildatasets.getPropertyList(i).getProperty("table", "").length() != 0) continue;
                    exludedSet.add(detaildatasets.getPropertyList(i));
                    excludedtableSet.add(detaildatasets.getPropertyList(i).getProperty("table"));
                }
                detaildatasets.removeAll(exludedSet);
            }
            if (sdcid != null && sdcid.length() > 0) {
                PropertyListCollection detailLinks;
                DataSet linksData = sdcProcessor.getLinksData(sdcid);
                if (linksData != null) {
                    for (int i = 0; i < linksData.getRowCount(); ++i) {
                        String linkType = linksData.getValue(i, "linktype");
                        String tablename = linksData.getValue(i, "linktableid");
                        if (!"D".equals(linkType) && !"M".equals(linkType) || excludedtableSet.contains(tablename) || detaildatasets.find("table", tablename, true) != null) continue;
                        PropertyList detailPL = new PropertyList();
                        detailPL.setProperty("table", tablename);
                        detaildatasets.add(detailPL);
                    }
                }
                if ((detailLinks = sdcProcessor.getDetailLinks(sdcid)) != null) {
                    for (int i = 0; i < detailLinks.size(); ++i) {
                        PropertyList detailLinkProps = detailLinks.getPropertyList(i);
                        String tableName = detailLinkProps.getProperty("linktableid");
                        if (!"D".equalsIgnoreCase(detailLinkProps.getProperty("linktype")) || excludedtableSet.contains(tableName) || detaildatasets.find("table", tableName, true) != null) continue;
                        PropertyList detailPL = new PropertyList();
                        detailPL.setProperty("table", tableName);
                        detaildatasets.add(detailPL);
                    }
                }
            }
        }
    }

    public static void resetCache(String databaseid) {
        CacheUtil.clear(databaseid, CMTPolicyNode_CacheName, true);
    }

    public String getChangeControlledFlag() {
        if (this.changeControlledFlag == null) {
            if ("Y".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol")) || "R".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol"))) {
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                this.changeControlledFlag = sdcProcessor.getProperty(this.sdcid, "changecontrolledflag", "N");
                if ("Y".equals(this.changeControlledFlag) && "R".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol"))) {
                    this.changeControlledFlag = "R";
                }
            } else {
                this.changeControlledFlag = "N";
            }
        }
        return this.changeControlledFlag;
    }

    public PropertyListCollection getHosts() {
        PropertyListCollection hosts = (PropertyListCollection)this.getDefaultPolicyPropertyList().getCollectionNotNull("hosts").clone();
        return hosts;
    }

    public boolean isChangeControlEnabled() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol"));
    }

    public boolean isChangeControlDeferToRepository() {
        return "R".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol"));
    }

    public boolean allowPerpetualCheckout() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getProperty("allowperpetualcheckout"));
    }

    public boolean isChangeRequestMandatory() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getPropertyList("changerequest").getProperty("mandatory"));
    }

    public boolean isChangeRequestRequireAcceptance() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getPropertyList("changerequest").getProperty("requireacceptance"));
    }

    public boolean isChangeRequestRequireApproval() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getPropertyList("changerequest").getProperty("requireapproval"));
    }

    public boolean isAutoPopulateChangeRequest() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getPropertyList("changerequest").getProperty("autopopulate"));
    }

    public boolean isDefaultTransferCMT() {
        return !"CTT".equals(this.getDefaultPolicyPropertyList().getProperty("defaulttransfertool"));
    }

    public boolean isBlockInvalidChecksum() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getProperty("blockInvalidChecksum", "N"));
    }

    public String getCertificationsOwner() {
        return CMTPolicy.getCertificationsOwner(this.getDefaultPolicyPropertyList());
    }

    private static String getCertificationsOwner(PropertyList props) {
        return props.getProperty("certificationsowner", "User");
    }

    public boolean isImportUserAsDisabled() {
        return "Y".equals(this.getDefaultPolicyPropertyList().getProperty("isImportUserAsDisabled"));
    }

    public boolean isMasterRepositoryEnabled() {
        return "R".equals(this.getDefaultPolicyPropertyList().getProperty("enablechangecontrol"));
    }

    public String getRepositoryURL() {
        return this.getDefaultPolicyPropertyList().getPropertyListNotNull("masterrepository").getProperty("masterserverurl");
    }

    public String getRepositoryAuthToken() {
        return this.getDefaultPolicyPropertyList().getPropertyListNotNull("masterrepository").getProperty("masterserverauthtoken");
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public String getRequestedPolicyNodeId() {
        return this.requestedPolicyNodeId;
    }

    public String getActualPolicyNodeId() {
        return this.actualPolicyNodeId;
    }

    public String getExportLabel() {
        return this.policyPropertyList.getProperty("label").length() > 0 ? this.policyPropertyList.getProperty("label") : this.getRequestedPolicyNodeId();
    }

    public String getDefaultExportLabel() {
        return this.getDefaultPolicyPropertyList().getProperty("label").length() > 0 ? this.getDefaultPolicyPropertyList().getProperty("label") : "Export Minimum";
    }

    public String getIndentifyColumn() {
        String col = "";
        if (this.policyPropertyList != null && this.policyPropertyList.getPropertyList("primary") != null && this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS) != null) {
            col = this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS).getProperty(POLICY_TRANSFER_OPTIONS_ALT_IDENTIFIER);
        }
        return col;
    }

    public boolean isTriggerBusinessRule() {
        return !"N".equals(this.policyPropertyList.getPropertyListNotNull("primary").getPropertyListNotNull(POLICY_TRANSFER_OPTIONS).getProperty("triggerbusinessrule"));
    }

    public String getImportOption() {
        if (this.policyPropertyList.getPropertyList("primary") != null && this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS) != null) {
            return this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS).getProperty("importoption");
        }
        return "";
    }

    public ConfigTransferOption getTransferOption() {
        return new ConfigTransferOption(this.policyPropertyList.getPropertyListNotNull(POLICY_TRANSFER_OPTIONS));
    }

    public boolean isAllowImporterToChoose() {
        if (this.policyPropertyList.getPropertyList("primary") != null && this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS) != null) {
            return "Y".equals(this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS).getProperty("allowimporterchooseoption"));
        }
        return true;
    }

    public String getImportVersionedSDIOption() {
        if (this.policyPropertyList.getPropertyList("primary") != null && this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS) != null) {
            return this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS).getProperty("importversionedsdioption");
        }
        return "";
    }

    public PropertyList getPolicyPropertyList() {
        return this.policyPropertyList;
    }

    public PropertyList getDefaultPolicyPropertyList() {
        return this.defaultPolicyPropertyList;
    }

    public SDIRequest getSDIRequest() throws SapphireException {
        return this.getSDIRequest(false);
    }

    public SDIRequest getSDIRequest(boolean isFullExport) throws SapphireException {
        PropertyListCollection m2mLinks;
        PropertyList detailrequestPL;
        int i;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.sdcid);
        String requestitem = "primary";
        PropertyList primaryProps = this.policyPropertyList.getPropertyListNotNull("primary");
        PropertyList primaryTransferOptions = primaryProps.getPropertyListNotNull(POLICY_TRANSFER_OPTIONS);
        String identifyCols = primaryTransferOptions.getProperty(POLICY_TRANSFER_OPTIONS_ALT_IDENTIFIER, "").trim();
        if (identifyCols.length() > 0) {
            requestitem = "primary[*," + identifyCols + "]";
        }
        sdiRequest.setRequestItem(requestitem);
        sdiRequest.setExtendedDataTypes(true);
        sdiRequest.setOverrideLoadFlag(true);
        sdiRequest.setShowHiddenRecords(true);
        sdiRequest.setSecurityBypassCode(1);
        PropertyListCollection detaildatasets = this.policyPropertyList.getCollection("detaildatasets");
        if (detaildatasets != null) {
            for (i = 0; i < detaildatasets.size(); ++i) {
                detailrequestPL = detaildatasets.getPropertyList(i);
                if ((!isFullExport || "N".equals(detailrequestPL.getProperty("includeintransfer", "Y"))) && isFullExport || !"Y".equals(StringUtil.getYN(detailrequestPL.getProperty("enabled"), "Y"))) continue;
                requestitem = detailrequestPL.getProperty("table");
                sdiRequest.setRequestItem(requestitem);
            }
        }
        if ((detaildatasets = this.policyPropertyList.getCollection("sdidetaildatasets")) != null) {
            for (i = 0; i < detaildatasets.size(); ++i) {
                detailrequestPL = detaildatasets.getPropertyList(i);
                if ((!isFullExport || "N".equals(detailrequestPL.getProperty("includeintransfer", "Y"))) && isFullExport || !"Y".equals(StringUtil.getYN(detailrequestPL.getProperty("enabled"), "Y"))) continue;
                requestitem = SDIData.getDatasetNameByTableName(detailrequestPL.getProperty("table"));
                sdiRequest.setRequestItem(requestitem);
                if ("dataset".equalsIgnoreCase(requestitem)) {
                    sdiRequest.setRequestItem("datasetattribute");
                    continue;
                }
                if ("dataitem".equalsIgnoreCase(requestitem)) {
                    sdiRequest.setRequestItem("dataitemattribute");
                    continue;
                }
                if (!"sdiworkitem".equalsIgnoreCase(requestitem)) continue;
                sdiRequest.setRequestItem("sdiworkitemattribute");
            }
        }
        if ((m2mLinks = this.getAssociatedSDIFilteredCollection(isFullExport, SnapshotItem.LinkType.M2M)).size() > 0) {
            for (int i2 = 0; i2 < m2mLinks.size(); ++i2) {
                PropertyList m2mLinkProps = m2mLinks.getPropertyList(i2);
                String m2mLinkId = CMTPolicy.getAssociatedSDILinkLinkId(m2mLinkProps);
                PropertyList linkProps = new PropertyList(this.getSDCProcessor().getLinkProperties(this.sdcid, m2mLinkId));
                String linkTableId = linkProps.getProperty("linktableid");
                if (sdiRequest.isRequestItem(linkTableId)) continue;
                sdiRequest.setRequestItem(linkTableId);
            }
        }
        return sdiRequest;
    }

    public boolean isSDC(String requestItem) {
        PropertyListCollection sdidatasets = this.policyPropertyList.getCollection("sdidatasets");
        boolean isSDC = false;
        if (sdidatasets != null) {
            for (int i = 0; i < sdidatasets.size(); ++i) {
                PropertyList sdirequestPL = sdidatasets.getPropertyList(i);
                if (!requestItem.equals(sdirequestPL.getProperty("sdcid"))) continue;
                isSDC = true;
                break;
            }
        }
        return isSDC;
    }

    public CMTPolicy getReferencedPolicy(String sdcId, String nodeId) throws SapphireException {
        CMTPolicy cmtPolicy = null;
        cmtPolicy = CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdcId, nodeId, this.policyValueTreeXML, this.policyDefTreeXML, this.policyNodeMap);
        return cmtPolicy;
    }

    public ArrayList<String> getChildNodeidList() {
        ArrayList<String> nodeidList = new ArrayList<String>();
        WebAdminProcessor wap = this.getRakFile() != null ? new WebAdminProcessor(this.getRakFile(), this.getConnectionid()) : new WebAdminProcessor(this.getConnectionid());
        try {
            String nodeid;
            NodeList nlist;
            PropertyTree propertyTree = null;
            if (this.policyValueTreeXML != null && this.policyValueTreeXML.length() > 0) {
                propertyTree = new PropertyTree("CMTPolicy");
                propertyTree.setValueXML(this.policyValueTreeXML);
                propertyTree.setDefinitionXML(this.policyDefTreeXML);
            } else {
                propertyTree = wap.getPropertyTree("CMTPolicy");
            }
            try {
                nlist = propertyTree.getNodeDescendantList(this.sdcid + " Custom");
                for (Node n : nlist) {
                    nodeid = n.getNodeId();
                    if (nodeid.indexOf(" Product") == nodeid.length() - 8) continue;
                    if (nodeid.indexOf(" Custom") == nodeid.length() - 7) {
                        nodeid = nodeid.substring(0, nodeid.indexOf(" Custom"));
                    }
                    nodeidList.add(nodeid);
                }
            }
            catch (SapphireException e) {
                Trace.log("Failed to retrieve Descendent node list for: " + this.sdcid + " Custom");
            }
            try {
                nlist = propertyTree.getNodeDescendantList(this.sdcid + " Comp Custom");
                for (Node n : nlist) {
                    nodeid = n.getNodeId();
                    if (nodeid.indexOf(" Product") == nodeid.length() - 8) continue;
                    if (nodeid.indexOf(" Custom") == nodeid.length() - 7) {
                        nodeid = nodeid.substring(0, nodeid.indexOf(" Custom"));
                    }
                    nodeidList.add(nodeid);
                }
            }
            catch (SapphireException e) {
                Trace.log("Failed to retrieve Descendent node list for: " + this.sdcid + " Comp Custom");
            }
        }
        catch (Exception e) {
            Trace.log("Failed to retrieve CMTPolicy");
        }
        return nodeidList;
    }

    public ArrayList<CMTPolicy> getChildNodePolicyList() {
        ArrayList<CMTPolicy> list = new ArrayList<CMTPolicy>();
        ArrayList<String> nodeidList = this.getChildNodeidList();
        for (String nodeid : nodeidList) {
            list.add(CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), this.sdcid, nodeid, this.policyValueTreeXML, this.policyDefTreeXML, this.policyNodeMap));
        }
        return list;
    }

    public ArrayList<PropertyList> getAssociatedSDIPropertyList(boolean includeTransferItems) throws SapphireException {
        return CMTPolicy.getAssociatedSDIPropertyList(includeTransferItems, this.policyPropertyList);
    }

    public static ArrayList<PropertyList> getAssociatedSDIPropertyList(boolean includeTransferItems, PropertyList policyNodeProps) throws SapphireException {
        ArrayList<PropertyList> associatedSDIPropertyList = new ArrayList<PropertyList>();
        PropertyListCollection sdidatasets = policyNodeProps.getCollectionNotNull("sdidatasets");
        for (int i = 0; i < sdidatasets.size(); ++i) {
            String linkInfo;
            String linkType;
            PropertyList sdidatasetPL = sdidatasets.getPropertyList(i);
            if (!"Y".equals(StringUtil.getYN(sdidatasetPL.getProperty("enabled"), "Y")) || (linkType = sdidatasetPL.getProperty("linktype", "")).length() <= 0 || (linkInfo = CMTPolicy.getAssociatedSDILinkInfo(sdidatasetPL)).length() <= 0) continue;
            associatedSDIPropertyList.add(sdidatasetPL);
        }
        if (includeTransferItems) {
            PropertyListCollection transfersdidatasets = policyNodeProps.getCollectionNotNull("adhoctransfersdidatasets");
            for (int i = 0; i < transfersdidatasets.size(); ++i) {
                String linkInfo;
                String linkType;
                PropertyList sdidatasetPL = transfersdidatasets.getPropertyList(i);
                if (!"Y".equals(StringUtil.getYN(sdidatasetPL.getProperty("enabled"), "Y")) || (linkType = sdidatasetPL.getProperty("linktype", "")).length() <= 0 || (linkInfo = CMTPolicy.getAssociatedSDILinkInfo(sdidatasetPL)).length() <= 0) continue;
                sdidatasetPL.setProperty("includeintransfer", "Y");
                associatedSDIPropertyList.add(sdidatasetPL);
            }
        }
        return associatedSDIPropertyList;
    }

    public PropertyListCollection getAssociatedSDIFilteredCollection(boolean includeTransferItems, SnapshotItem.LinkType linkType) throws SapphireException {
        ArrayList<PropertyList> associatedSDIColCollection = this.getAssociatedSDIPropertyList(includeTransferItems);
        PropertyListCollection filteredCollection = new PropertyListCollection();
        for (PropertyList associatedSDIColProps : associatedSDIColCollection) {
            if (!associatedSDIColProps.getProperty("linktype").equals(linkType.getCode())) continue;
            filteredCollection.add(associatedSDIColProps);
        }
        return filteredCollection;
    }

    public static PropertyListCollection getAssociatedSDIFilteredCollection(boolean includeTransferItems, SnapshotItem.LinkType linkType, PropertyList policyNodeprops) throws SapphireException {
        ArrayList<PropertyList> associatedSDIColCollection = CMTPolicy.getAssociatedSDIPropertyList(includeTransferItems, policyNodeprops);
        PropertyListCollection filteredCollection = new PropertyListCollection();
        for (PropertyList associatedSDIColProps : associatedSDIColCollection) {
            if (!associatedSDIColProps.getProperty("linktype").equals(linkType.getCode())) continue;
            filteredCollection.add(associatedSDIColProps);
        }
        return filteredCollection;
    }

    public static String getAssociatedSDILinkInfo(PropertyList associatedSDIPolicyPL) throws SapphireException {
        String linkInfo;
        SnapshotItem.LinkType linkType = SnapshotItem.LinkType.getByCode(associatedSDIPolicyPL.getProperty("linktype"));
        if (linkType == null) {
            throw new SapphireException("Invalid Link Type found.");
        }
        String linkId = "";
        switch (linkType) {
            case FK: {
                linkId = "linkid_fk";
                linkInfo = associatedSDIPolicyPL.getProperty(linkId);
                break;
            }
            case M2M: {
                linkId = "linkid_m2m";
                linkInfo = associatedSDIPolicyPL.getProperty(linkId);
                break;
            }
            case REVFK: {
                linkId = "linkid_rfk";
                linkInfo = associatedSDIPolicyPL.getProperty(linkId);
                break;
            }
            case REVSOFTLINK: {
                linkInfo = associatedSDIPolicyPL.getProperty("linksdcid");
                break;
            }
            case SQL: {
                linkInfo = associatedSDIPolicyPL.getProperty("linksdcid");
                break;
            }
            default: {
                throw new SapphireException("Invalid Link Type found.");
            }
        }
        return linkInfo;
    }

    public static String getAssociatedSDILinkSDCId(PropertyList associatedSDIPolicyPL) throws SapphireException {
        return StringUtil.split(CMTPolicy.getAssociatedSDILinkInfo(associatedSDIPolicyPL), ";")[0];
    }

    public static String getAssociatedSDILinkLinkId(PropertyList associatedSDIPolicyPL) throws SapphireException {
        String linkInfo = CMTPolicy.getAssociatedSDILinkInfo(associatedSDIPolicyPL);
        if (linkInfo.indexOf(";") > 0) {
            return StringUtil.split(CMTPolicy.getAssociatedSDILinkInfo(associatedSDIPolicyPL), ";")[1];
        }
        return "";
    }

    public PropertyListCollection getDetailFKLinksProps(String detailTableId, boolean isTransfer) {
        return this.getAnyDetailFKLinksProps(detailTableId, isTransfer, "detaildatasets");
    }

    public PropertyListCollection getSDIDetailFKLinksProps(String sdiDetailTableId, boolean isTransfer) {
        return this.getAnyDetailFKLinksProps(sdiDetailTableId, isTransfer, "sdidetaildatasets");
    }

    public PropertyList getDetailProps(String detailTableId) {
        return this.getAnyDetailProps(detailTableId, "detaildatasets");
    }

    public PropertyList getSDIDetailProps(String detailTableId) {
        return this.getAnyDetailProps(detailTableId, "sdidetaildatasets");
    }

    private PropertyList getAnyDetailProps(String detailTableId, String propertyCollectionid) {
        PropertyListCollection detailTables = this.policyPropertyList.getCollectionNotNull(propertyCollectionid);
        PropertyList detailTableProps = detailTables.find("table", detailTableId, true);
        return detailTableProps;
    }

    private PropertyListCollection getAnyDetailFKLinksProps(String detailTableId, boolean isTransfer, String propertyCollectionid) {
        PropertyListCollection detailFKLinksProps2 = new PropertyListCollection();
        PropertyList detailTableProps = this.getAnyDetailProps(detailTableId, propertyCollectionid);
        if (detailTableProps != null) {
            boolean includeDetailInTransfer = "Y".equals(StringUtil.getYN(detailTableProps.getProperty("includeintransfer"), "Y"));
            if (isTransfer && includeDetailInTransfer) {
                PropertyListCollection detailFKLinksProps = detailTableProps.getCollectionNotNull("links");
                for (int i = 0; i < detailFKLinksProps.size(); ++i) {
                    PropertyList fkLinkProps = detailFKLinksProps.getPropertyList(i);
                    if (!"Y".equals(fkLinkProps.getProperty("enabled", "Y"))) continue;
                    fkLinkProps.setProperty("includeintransfer", "Y");
                    detailFKLinksProps2.add(fkLinkProps);
                }
            }
        }
        return detailFKLinksProps2;
    }

    public static String getDetailFKLinkInfo(PropertyList detailFKLinkProps) throws SapphireException {
        String linkInfo = detailFKLinkProps.getProperty("linkid_fk");
        return linkInfo;
    }

    public static String getDetailFKLinkSDCId(PropertyList detailFKLinkProps) throws SapphireException {
        return StringUtil.split(CMTPolicy.getDetailFKLinkInfo(detailFKLinkProps), ";")[0];
    }

    public static String getDetailFKLinkLinkId(PropertyList detailFKLinkProps) throws SapphireException {
        return StringUtil.split(CMTPolicy.getDetailFKLinkInfo(detailFKLinkProps), ";")[1];
    }

    public boolean isIncludedForTransfer(PropertyList policyColProps) {
        return policyColProps.containsKey("includeintransfer") && "Y".equals(policyColProps.getProperty("includeintransfer", "Y"));
    }

    public ArrayList<String> getDetailDataSetList(boolean isTransfer) {
        ArrayList<String> associatedDetailNames = new ArrayList<String>();
        PropertyListCollection detaildatasets = this.policyPropertyList.getCollection("detaildatasets");
        if (detaildatasets != null) {
            for (int i = 0; i < detaildatasets.size(); ++i) {
                PropertyList detailrequestPL = detaildatasets.getPropertyList(i);
                if ((!isTransfer || !"Y".equals(detailrequestPL.getProperty("includeintransfer", "Y"))) && isTransfer) continue;
                String requestitem = detailrequestPL.getProperty("table");
                associatedDetailNames.add(requestitem);
            }
        }
        return associatedDetailNames;
    }

    public ArrayList<String> getSDIDetailDataSetList(boolean isTransfer) {
        ArrayList<String> associatedDetailNames = new ArrayList<String>();
        PropertyListCollection detaildatasets = this.policyPropertyList.getCollection("sdidetaildatasets");
        if (detaildatasets != null) {
            for (int i = 0; i < detaildatasets.size(); ++i) {
                PropertyList detailrequestPL = detaildatasets.getPropertyList(i);
                if ((!isTransfer || !"Y".equals(detailrequestPL.getProperty("includeintransfer", "Y"))) && isTransfer) continue;
                String requestitem = detailrequestPL.getProperty("table");
                associatedDetailNames.add(requestitem);
            }
        }
        return associatedDetailNames;
    }

    public String getSDISnapShotTreeHtml(boolean isTransfer) throws SapphireException {
        SDISnapshotItem sdiItem = new SDISnapshotItem(this.getSdcid(), "keyid1", "", "", this.getActualPolicyNodeId());
        JSONArray rootArray = new JSONArray();
        ArrayList<NodeItem> rootnodeItemList = this.getSDISnapShotTreeNodes(isTransfer, sdiItem, null, 0);
        for (NodeItem item : rootnodeItemList) {
            rootArray.put(item);
        }
        StringBuilder out = new StringBuilder();
        String id = "treeelement_" + isTransfer;
        out.append("<div style=\"display:block;min-width:100px;min-height:50px\" id=\"" + id + "\"></div>");
        out.append("<script>");
        try {
            out.append("\nvar initialContextData_" + id + "=" + rootArray.toString(4) + ";");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        out.append("\nvar mode=\"treeelement\"");
        out.append("\nvar navigator_props=" + new PropertyList().toJSONString(false, false) + ";");
        out.append("\nsapphire.gwt.addGWTElement('navigator','" + id + "', navigator_props );");
        out.append("\n</script>");
        return out.toString();
    }

    private ArrayList<NodeItem> getSDISnapShotTreeNodes(boolean isTransfer, SDISnapshotItem sdiTree, NodeItem parentItem, int nodecount) throws SapphireException {
        boolean showDetailList = parentItem == null;
        boolean showSDIDetailList = false;
        TranslationProcessor tp = this.getTranslationProcessor();
        ArrayList<NodeItem> nodeItemList = parentItem == null ? new ArrayList<NodeItem>() : null;
        NodeItem nodeItem = null;
        if (nodecount > 30) {
            parentItem.addChildItem(new NodeItem("" + ++nodecount, "...", false, ""));
            return null;
        }
        if (sdiTree.hasSameAncestor(false)) {
            nodeItem = new NodeItem("" + ++nodecount, "(" + tp.translate("Loop to Node: ") + sdiTree.getPolicyNodeId() + ")", false, "");
            if (parentItem != null) {
                parentItem.addChildItem(nodeItem);
            } else {
                nodeItemList.add(nodeItem);
            }
        } else {
            ArrayList<String> detailList;
            ArrayList<PropertyList> sdilist = this.getAssociatedSDIPropertyList(isTransfer);
            if (sdilist.size() > 0) {
                NodeItem headerNodeItem = new NodeItem("" + ++nodecount, this.sdcid + " " + (tp != null ? tp.translate("Associated SDCs") : "Associated SDCs") + " (" + sdilist.size() + ")", true, "");
                if (parentItem != null) {
                    parentItem.addChildItem(headerNodeItem);
                } else {
                    nodeItemList.add(headerNodeItem);
                }
                for (PropertyList object : sdilist) {
                    nodeItem = new NodeItem("" + ++nodecount, object.getProperty("linkalias"), true, "");
                    headerNodeItem.addChildItem(nodeItem);
                    try {
                        String sdcid = CMTPolicy.getAssociatedSDILinkSDCId(object);
                        if ("PropertyTree".equals(sdcid)) {
                            String exportNodeAncestors;
                            PropertyList propertyTreeProps = object.getPropertyListNotNull("propertytreeprops");
                            String expDefinition = propertyTreeProps.getProperty("exportdefinition", "N");
                            if ("Y".equals(expDefinition)) {
                                nodeItem = new NodeItem("" + ++nodecount, tp.translate("PropertyTree Definition"), false, "");
                                if (parentItem != null) {
                                    parentItem.addChildItem(nodeItem);
                                } else {
                                    nodeItemList.add(nodeItem);
                                }
                            }
                            if ("Y".equals(exportNodeAncestors = propertyTreeProps.getProperty("exportnodeancestors", "N"))) {
                                nodeItem = new NodeItem("" + ++nodecount, tp.translate("Ancestor Node Hierarchy"), false, "");
                                if (parentItem != null) {
                                    parentItem.addChildItem(nodeItem);
                                } else {
                                    nodeItemList.add(nodeItem);
                                }
                            }
                        }
                        if (object.getProperty("refpolicynodeid").length() <= 0 && sdcid.length() <= 0) continue;
                        CMTPolicy refpolicy = CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdcid, object.getProperty("refpolicynodeid"), this.policyValueTreeXML, this.policyDefTreeXML, this.policyNodeMap);
                        SDISnapshotItem childItem = new SDISnapshotItem(refpolicy.getSdcid(), "keyid1", "", "", refpolicy.getActualPolicyNodeId());
                        sdiTree.addLink(childItem, SnapshotItem.LinkType.getByCode(object.getProperty("linktype")), CMTPolicy.getAssociatedSDILinkLinkId(object), null);
                        refpolicy.getSDISnapShotTreeNodes(isTransfer, childItem, nodeItem, ++nodecount);
                    }
                    catch (SapphireException sdcid) {}
                }
            }
            if (showDetailList && (detailList = this.getDetailDataSetList(isTransfer)).size() > 0) {
                NodeItem detailHeaderNodeItem = new NodeItem("" + ++nodecount, this.sdcid + " " + (tp != null ? tp.translate("Detail Tables") : "Detail Tables") + " (" + detailList.size() + ")", true, "");
                if (parentItem != null) {
                    parentItem.addChildItem(detailHeaderNodeItem);
                } else {
                    nodeItemList.add(detailHeaderNodeItem);
                }
                for (String s : detailList) {
                    NodeItem detailnodeItem = new NodeItem("" + ++nodecount, s, false, "");
                    detailHeaderNodeItem.addChildItem(detailnodeItem);
                    PropertyListCollection detailFKLinks = this.getDetailFKLinksProps(s, isTransfer);
                    if (!isTransfer || detailFKLinks.size() <= 0) continue;
                    NodeItem detailFKnodeItem = new NodeItem("" + ++nodecount, s + " " + (tp != null ? tp.translate("Detail FK-linked SDCs") : "Detail FK-linked SDCs") + " (" + detailFKLinks.size() + ")", true, "");
                    detailnodeItem.addChildItem(detailFKnodeItem);
                    for (int i = 0; i < detailFKLinks.size(); ++i) {
                        PropertyList pl = detailFKLinks.getPropertyList(i);
                        String linkid_fk = pl.getProperty("linkid_fk");
                        String sdcid = linkid_fk.indexOf(";") > 0 ? linkid_fk.substring(0, linkid_fk.indexOf(";")) : linkid_fk;
                        NodeItem linkid_fkNodeItem = new NodeItem("" + ++nodecount, StringUtil.replaceAll(linkid_fk, ";", " - "), true, "");
                        detailFKnodeItem.addChildItem(linkid_fkNodeItem);
                        if ("PropertyTree".equals(sdcid)) {
                            String exportNodeAncestors;
                            PropertyList propertyTreeProps = pl.getPropertyListNotNull("propertytreeprops");
                            String expDefinition = propertyTreeProps.getProperty("exportdefinition", "N");
                            if ("Y".equals(expDefinition)) {
                                linkid_fkNodeItem.addChildItem(new NodeItem("" + ++nodecount, tp.translate("PropertyTree Definition"), false, ""));
                            }
                            if ("Y".equals(exportNodeAncestors = propertyTreeProps.getProperty("exportnodeancestors", "N"))) {
                                linkid_fkNodeItem.addChildItem(new NodeItem("" + ++nodecount, tp.translate("Ancestor Node Hierarchy"), false, ""));
                            }
                        }
                        if (pl.getProperty("refpolicynodeid").length() <= 0 && sdcid.length() <= 0) continue;
                        CMTPolicy refpolicy = CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdcid, pl.getProperty("refpolicynodeid"), this.policyValueTreeXML, this.policyDefTreeXML, this.policyNodeMap);
                        SDISnapshotItem childItem = new SDISnapshotItem(refpolicy.getSdcid(), "keyid1", "", "", refpolicy.getActualPolicyNodeId());
                        pl.setProperty("linktype", "FK");
                        sdiTree.addLink(childItem, SnapshotItem.LinkType.getByCode(pl.getProperty("linktype")), CMTPolicy.getAssociatedSDILinkLinkId(pl), null);
                        refpolicy.getSDISnapShotTreeNodes(isTransfer, childItem, linkid_fkNodeItem, ++nodecount);
                    }
                }
            }
            if ((detailList = this.getSDIDetailDataSetList(isTransfer)).size() > 0 && showSDIDetailList) {
                for (String string : detailList) {
                }
            }
        }
        return nodeItemList;
    }

    public ArrayList<String> getExcludedTableList() {
        ArrayList<String> excludecolumnlist = new ArrayList<String>();
        PropertyListCollection[] collectionArr = new PropertyListCollection[]{this.policyPropertyList.getCollectionNotNull("detaildatasets"), this.policyPropertyList.getCollectionNotNull("sdidetaildatasets")};
        for (PropertyListCollection detailDatasets : collectionArr) {
            for (int i = 0; i < detailDatasets.size(); ++i) {
                PropertyList detailrequestPL = detailDatasets.getPropertyList(i);
                if ("Y".equals(detailrequestPL.getProperty("includeintransfer", "Y"))) continue;
                excludecolumnlist.add(detailrequestPL.getProperty("table"));
            }
        }
        return excludecolumnlist;
    }

    public ArrayList<String> getExcludedColumnList(String requestItem) {
        ArrayList<String> excludecolumnlist = new ArrayList<String>();
        PropertyList transferOptionPL = null;
        if ("primary".equals(requestItem)) {
            transferOptionPL = this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS);
        } else if ("s_sdicertification".equals(requestItem) || "sdcsecurity".equals(requestItem) || "sdcjobtypesecurity".equals(requestItem)) {
            transferOptionPL = this.policyPropertyList.getPropertyList("primary").getPropertyList(POLICY_TRANSFER_OPTIONS);
        } else {
            PropertyList detailrequestPL;
            PropertyListCollection sdidatasets = this.policyPropertyList.getCollection("sdidatasets");
            if (sdidatasets != null) {
                for (int i = 0; i < sdidatasets.size(); ++i) {
                    PropertyList sdirequestPL = sdidatasets.getPropertyList(i);
                    if (!requestItem.equals(sdirequestPL.getProperty("sdcid"))) continue;
                    transferOptionPL = sdirequestPL.getPropertyList(POLICY_TRANSFER_OPTIONS);
                    break;
                }
            }
            if (transferOptionPL == null && this.policyPropertyList.getCollection("detaildatasets") != null) {
                PropertyListCollection detaildatasets = this.policyPropertyList.getCollectionNotNull("detaildatasets");
                for (int i = 0; i < detaildatasets.size(); ++i) {
                    detailrequestPL = detaildatasets.getPropertyList(i);
                    if (!requestItem.equals(detailrequestPL.getProperty("table"))) continue;
                    transferOptionPL = detailrequestPL.getPropertyList(POLICY_TRANSFER_OPTIONS);
                    break;
                }
            }
            if (transferOptionPL == null && this.policyPropertyList.getCollection("sdidetaildatasets") != null) {
                PropertyListCollection sdiDetaildatasets = this.policyPropertyList.getCollectionNotNull("sdidetaildatasets");
                for (int i = 0; i < sdiDetaildatasets.size(); ++i) {
                    detailrequestPL = sdiDetaildatasets.getPropertyList(i);
                    if (!requestItem.equals(detailrequestPL.getProperty("table"))) continue;
                    transferOptionPL = detailrequestPL.getPropertyList(POLICY_TRANSFER_OPTIONS);
                    break;
                }
            }
        }
        if (transferOptionPL != null) {
            String altIdColumns = "," + transferOptionPL.getProperty(POLICY_TRANSFER_OPTIONS_ALT_IDENTIFIER, "") + ",";
            if ("Y".equals(transferOptionPL.getProperty("excludeauditcolumn", "Y"))) {
                if (altIdColumns.indexOf(",auditsequence,") == -1) {
                    excludecolumnlist.add("auditsequence");
                }
                if (altIdColumns.indexOf(",createby,") == -1) {
                    excludecolumnlist.add("createby");
                }
                if (altIdColumns.indexOf(",createdt,") == -1) {
                    excludecolumnlist.add("createdt");
                }
                if (altIdColumns.indexOf(",createtool,") == -1) {
                    excludecolumnlist.add("createtool");
                }
                if (altIdColumns.indexOf(",modby,") == -1) {
                    excludecolumnlist.add("modby");
                }
                if (altIdColumns.indexOf(",moddt,") == -1) {
                    excludecolumnlist.add("moddt");
                }
                if (altIdColumns.indexOf(",modtool,") == -1) {
                    excludecolumnlist.add("modtool");
                }
            }
            if ("U".equals(transferOptionPL.getProperty("excludedeptseccolumn"))) {
                excludecolumnlist.add("securityuser");
            }
            if ("B".equals(transferOptionPL.getProperty("excludedeptseccolumn"))) {
                excludecolumnlist.add("securityuser");
                excludecolumnlist.add("securitydepartment");
            }
            if ("Y".equals(transferOptionPL.getProperty("excludesdisecuritycolumn"))) {
                excludecolumnlist.add("securityset");
            }
            if (transferOptionPL.getProperty("excludecolumnlist").trim().length() > 0) {
                String[] cols = StringUtil.split(transferOptionPL.getProperty("excludecolumnlist"), ",");
                for (int i = 0; i < cols.length; ++i) {
                    if ("sdiattachment".equalsIgnoreCase(requestItem) && "sdiattachmentid".equalsIgnoreCase(cols[i])) continue;
                    excludecolumnlist.add(cols[i].toLowerCase());
                }
            }
        }
        if ("sdiattachment".equalsIgnoreCase(requestItem)) {
            excludecolumnlist.add("attachmentrepositoryid");
            excludecolumnlist.add("attachmentrepositorynodeid");
            excludecolumnlist.add("externalrepository");
            excludecolumnlist.add("datahash");
            excludecolumnlist.add("encryptedflag");
            excludecolumnlist.add("compressedflag");
            excludecolumnlist.add("lockedflag");
            excludecolumnlist.add("lockedby");
        }
        if ("primary".equalsIgnoreCase(requestItem) && "User".equals(this.getSdcid())) {
            excludecolumnlist.add("password");
        }
        excludecolumnlist.add("tracelogid");
        return excludecolumnlist;
    }

    public String toString() {
        return this.getSdcid() + ":" + this.getRequestedPolicyNodeId();
    }

    public String toXML() {
        return this.policyPropertyList.toXMLString();
    }

    public String getCMTAdminRoleID() {
        return this.getDefaultPolicyPropertyList().getProperty("cmtadminroleid", "CMT Admin");
    }

    public static class NodeItem
    extends JSONObject {
        public NodeItem(String nodeid, String nodelabel, boolean isHeader) {
            this(nodeid, nodelabel, isHeader, "");
        }

        public NodeItem(String nodeid, String nodelabel, boolean isHeader, String image) {
            try {
                this.put("nodeid", nodeid);
                this.put("nodelabel", nodelabel);
                this.put("isheader", isHeader ? "Y" : "N");
                this.put("image", image);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        public void addChildItem(NodeItem item) {
            try {
                JSONArray childArray;
                JSONArray jSONArray = childArray = this.has("childitems") ? this.getJSONArray("childitems") : null;
                if (childArray == null) {
                    childArray = new JSONArray();
                    this.put("childitems", childArray);
                }
                childArray.put(item);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

