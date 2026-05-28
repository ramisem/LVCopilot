/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.Logger;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MigratePre85Properties
extends BaseDiagnostic {
    String labvantageHome = "";

    public MigratePre85Properties(DBAccess database, ConnectionInfo conenctionInfo, String labvantageHome) {
        super(database, conenctionInfo);
        this.labvantageHome = labvantageHome;
    }

    public MigratePre85Properties(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    @Override
    public String getTitle() {
        return "Migrating pre 8.5 properties to 8.5 system";
    }

    @Override
    public String getDescription() {
        return "Migrating pre 8.5 properties to 8.5 system";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        String msg = "";
        try {
            String copyDownPolicy_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "CopyDownPolicy", true);
            PropertyTree copyDown_propertyTree = new PropertyTree();
            copyDown_propertyTree.setValueXML(copyDownPolicy_propertyTreeXML);
            msg = msg + this.migrateToCopyDownPolicyIntraSDIPropertList(copyDown_propertyTree, copyDownPolicy_propertyTreeXML);
            String attachmentPolicy_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "AttachmentPolicy", true);
            PropertyTree attchmentPolicypropertyTree = new PropertyTree();
            attchmentPolicypropertyTree.setValueXML(attachmentPolicy_propertyTreeXML);
            msg = msg + this.migrateFromAttachmentPolicyToCopyDownPolicy(copyDown_propertyTree, copyDownPolicy_propertyTreeXML, attchmentPolicypropertyTree, attachmentPolicy_propertyTreeXML);
            String netWorkFileRepository_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "NetworkFileRepository", true);
            String netWorkFileRepository_propertyTreeDefXML = PropertyTreeUtil.getPropertyTreeDefinition(this.database, "NetworkFileRepository", true);
            PropertyTree netWorkFileRepositoryPropertyTree = new PropertyTree();
            netWorkFileRepositoryPropertyTree.setValueXML(netWorkFileRepository_propertyTreeXML);
            netWorkFileRepositoryPropertyTree.setDefinitionXML(netWorkFileRepository_propertyTreeDefXML);
            PropertyTree propTree = PropertyTreeUtil.getPropertyTree(this.database, "FileLocationPolicy");
            msg = msg + this.copyFileLocationPolicyCustomNodePathToNetworkFileRepository(netWorkFileRepositoryPropertyTree, netWorkFileRepository_propertyTreeXML, propTree);
            msg = msg + this.populateRepositoryForAttachmentPolicyClasses(attchmentPolicypropertyTree, attachmentPolicy_propertyTreeXML, netWorkFileRepositoryPropertyTree, netWorkFileRepository_propertyTreeXML);
            String attachmentelem_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "attachment", true);
            String attachmentelem_propertyTreeDefXML = PropertyTreeUtil.getPropertyTreeDefinition(this.database, "attachment", true);
            PropertyTree attachmentelem_propertyTree = new PropertyTree();
            attachmentelem_propertyTree.setValueXML(attachmentelem_propertyTreeXML);
            attachmentelem_propertyTree.setDefinitionXML(attachmentelem_propertyTreeDefXML);
            msg = msg + this.migrateAttachmentTypesToAttachmentElement(attchmentPolicypropertyTree, attachmentPolicy_propertyTreeXML, attachmentelem_propertyTree, attachmentelem_propertyTreeXML);
            msg = msg + this.migrateAttachmentElementClassToFilterClassCollection(attachmentelem_propertyTree, attachmentelem_propertyTreeXML);
            String filelocationPolicy_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "FileLocationPolicy", true);
            PropertyTree filelocationPolicypropertyTree = new PropertyTree();
            filelocationPolicypropertyTree.setValueXML(filelocationPolicy_propertyTreeXML);
            msg = msg + this.migrateAttachmentExcludesToAttachmentElement(attachmentelem_propertyTree, attachmentelem_propertyTreeXML, filelocationPolicypropertyTree, filelocationPolicy_propertyTreeXML);
            String elnPolicy_propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, "ELNPolicy", true);
            PropertyTree elnPolicyPropertyTree = new PropertyTree();
            elnPolicyPropertyTree.setValueXML(elnPolicy_propertyTreeXML);
            msg = msg + this.updatePre85ELNPolicy(elnPolicyPropertyTree, elnPolicy_propertyTreeXML);
        }
        catch (SapphireException e) {
            Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            msg = "[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage();
        }
        return msg;
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    public String migrateToCopyDownPolicyIntraSDIPropertList(PropertyTree copyDown_propertyTree, String copyDownPolicy_propertyTreeXML) throws SapphireException {
        ArrayList copyDown_allNodes = copyDown_propertyTree.getAllNodes();
        boolean updateCopyDownPolicy = false;
        String msg = "";
        for (Node node : copyDown_allNodes) {
            if (!node.isCustom()) continue;
            PropertyList pl = node.getPropertyList();
            PropertyList plIntraSDICopying = pl.getPropertyListNotNull("intrasdicopying");
            PropertyList fromTemplate = plIntraSDICopying.getPropertyListNotNull("fromtemplate");
            PropertyList upversioning = plIntraSDICopying.getPropertyListNotNull("upversioning");
            PropertyList copyFromSDI = plIntraSDICopying.getPropertyListNotNull("copyfromsdi");
            if (pl.containsKey("specapplyflag")) {
                String specApplyFlag = pl.getProperty("specapplyflag");
                if (specApplyFlag.length() > 0 && specApplyFlag.length() > 0) {
                    fromTemplate.setProperty("specapplyflag", specApplyFlag);
                    upversioning.setProperty("specapplyflag", specApplyFlag);
                    copyFromSDI.setProperty("specapplyflag", specApplyFlag);
                }
                pl.deleteProperty("specapplyflag");
                updateCopyDownPolicy = true;
                msg = "Moved pre85 CopyDownPolicy -> \"Spec Apply Flag\" property value from all Custom nodes to respective Custom node of the CopyDownPolicy ->\"Intra SDI Copying\" property list.";
            }
            if (!pl.containsKey("copytemplatedepartment")) continue;
            String copyTemplateDepartment = pl.getProperty("copytemplatedepartment");
            if (copyTemplateDepartment.length() > 0) {
                fromTemplate.setProperty("copysecuritydepartment", copyTemplateDepartment);
                upversioning.setProperty("copysecuritydepartment", copyTemplateDepartment);
                copyFromSDI.setProperty("copysecuritydepartment", copyTemplateDepartment);
            }
            pl.deleteProperty("copytemplatedepartment");
            updateCopyDownPolicy = true;
            msg = msg + "\nMoved pre85 CopyDownPolicy -> \"Always copy Template Department\" property value from all Custom nodes to the respective Custom node of CopyDownPolicy ->\"Intra SDI Copying\" property list.";
        }
        if (updateCopyDownPolicy && copyDownPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("CopyDownPolicy", copyDownPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "CopyDownPolicy", copyDown_propertyTree.toXMLString());
        }
        return msg;
    }

    public String migrateFromAttachmentPolicyToCopyDownPolicy(PropertyTree copyDown_propertyTree, String copyDownPolicy_propertyTreeXML, PropertyTree attchmentPolicypropertyTree, String attachmentPolicy_propertyTreeXML) throws SapphireException {
        PropertyListCollection classes;
        Node attchmentPolicySCNode = attchmentPolicypropertyTree.getNode("Sapphire Custom");
        PropertyList attchmentPolicyPl = attchmentPolicySCNode.getPropertyList();
        String msg = "";
        String copyTemplate = attchmentPolicyPl.getProperty("copytemplate");
        Node node = copyDown_propertyTree.getNode("Sapphire Custom");
        PropertyList pl = node.getPropertyList();
        PropertyList plIntraSDICopying = pl.getPropertyListNotNull("intrasdicopying");
        PropertyList fromTemplate = plIntraSDICopying.getPropertyListNotNull("fromtemplate");
        PropertyList upversioning = plIntraSDICopying.getPropertyListNotNull("upversioning");
        PropertyList copyFromSDI = plIntraSDICopying.getPropertyListNotNull("copyfromsdi");
        boolean updateCopyDownPolicy = false;
        boolean updateAttachmentPolicy = false;
        if (copyTemplate.length() > 0) {
            fromTemplate.setProperty("copyattachments", copyTemplate);
            upversioning.setProperty("copyattachments", copyTemplate);
            copyFromSDI.setProperty("copyattachments", copyTemplate);
            updateCopyDownPolicy = true;
        }
        if ((classes = attchmentPolicyPl.getCollectionNotNull("classes")).size() > 0) {
            PropertyList newAttachmentPl;
            String className;
            PropertyList attachmentPl;
            int i;
            PropertyList attachmentdetailsfromtemplate = fromTemplate.getPropertyListNotNull("attachmentdetails");
            PropertyList attachmentdetailscopyfromsdi = copyFromSDI.getPropertyListNotNull("attachmentdetails");
            PropertyList attachmentdetailsversion = upversioning.getPropertyListNotNull("attachmentdetails");
            PropertyListCollection collectionCopyFromSDI = attachmentdetailscopyfromsdi.getCollectionNotNull("attachmentclasses");
            PropertyListCollection collectionCopyFromTemplate = attachmentdetailsfromtemplate.getCollectionNotNull("attachmentclasses");
            PropertyListCollection collectionUpVersion = attachmentdetailsversion.getCollectionNotNull("attachmentclasses");
            long sequence = 0L;
            long propertyListIdCount = System.currentTimeMillis();
            for (i = 0; i < classes.size(); ++i) {
                attachmentPl = classes.getPropertyList(i);
                className = attachmentPl.getProperty("class");
                String copySDIMode = attachmentPl.getProperty("copysdicopymode");
                newAttachmentPl = new PropertyList();
                newAttachmentPl.setProperty("attachmentclass", className);
                newAttachmentPl.setProperty("attachmentcopyoption", copySDIMode);
                newAttachmentPl.setSequence(sequence + 100000L);
                newAttachmentPl.setId(Long.toString(copyDown_propertyTree.getUniquePropertyListId(propertyListIdCount)));
                collectionCopyFromSDI.add(newAttachmentPl);
                collectionCopyFromTemplate.add(newAttachmentPl);
                attachmentPl.deleteProperty("copysdicopymode");
                updateAttachmentPolicy = true;
            }
            if (collectionCopyFromSDI.size() > 0) {
                attachmentdetailsfromtemplate.setProperty("copyallattachments", "N");
                attachmentdetailscopyfromsdi.setProperty("copyallattachments", "N");
                attachmentdetailscopyfromsdi.setProperty("attachmentclasses", collectionCopyFromSDI);
                attachmentdetailsfromtemplate.setProperty("attachmentclasses", collectionCopyFromTemplate);
                fromTemplate.setProperty("attachmentdetails", attachmentdetailsfromtemplate);
                copyFromSDI.setProperty("attachmentdetails", attachmentdetailscopyfromsdi);
                updateCopyDownPolicy = true;
            }
            sequence = 0L;
            for (i = 0; i < classes.size(); ++i) {
                attachmentPl = classes.getPropertyList(i);
                className = attachmentPl.getProperty("class");
                String upversionCopyMode = attachmentPl.getProperty("upversioncopymode");
                newAttachmentPl = new PropertyList();
                newAttachmentPl.setProperty("attachmentclass", className);
                newAttachmentPl.setProperty("attachmentcopyoption", upversionCopyMode);
                newAttachmentPl.setSequence(sequence + 100000L);
                newAttachmentPl.setId(Long.toString(copyDown_propertyTree.getUniquePropertyListId(propertyListIdCount)));
                collectionUpVersion.add(newAttachmentPl);
                attachmentPl.deleteProperty("upversioncopymode");
                updateAttachmentPolicy = true;
            }
            if (collectionUpVersion.size() > 0) {
                attachmentdetailsversion.setProperty("copyallattachments", "N");
                attachmentdetailsversion.setProperty("attachmentclasses", collectionUpVersion);
                upversioning.setProperty("attachmentdetails", attachmentdetailsversion);
                updateCopyDownPolicy = true;
            }
        }
        if (updateCopyDownPolicy && copyDownPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("CopyDownPolicy", copyDownPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "CopyDownPolicy", copyDown_propertyTree.toXMLString());
        }
        if (updateAttachmentPolicy && attachmentPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("AttachmentPolicy", attachmentPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "AttachmentPolicy", attchmentPolicypropertyTree.toXMLString());
        }
        return msg;
    }

    public String populateRepositoryForAttachmentPolicyClasses(PropertyTree attchmentPolicypropertyTree, String attachmentPolicy_propertyTreeXML, PropertyTree netWorkFileRepositoryPropertyTree, String netWorkFileRepository_propertyTreeXML) throws SapphireException {
        boolean contentEditable;
        PropertyList classPl;
        int i;
        String msg = "";
        Node attchmentPolicySCNode = attchmentPolicypropertyTree.getNode("Sapphire Custom");
        PropertyList attchmentPolicyPl = attchmentPolicySCNode.getPropertyList();
        PropertyList serverSide = attchmentPolicyPl.getPropertyListNotNull("serverside");
        PropertyListCollection classes = attchmentPolicyPl.getCollectionNotNull("classes");
        boolean updateAttachmentPolicy = false;
        boolean updateNetworkFileRepsitory = false;
        boolean copyServerSide = false;
        for (i = 0; i < classes.size(); ++i) {
            classPl = classes.getPropertyList(i);
            contentEditable = !"N".equalsIgnoreCase(classPl.getProperty("contenteditable"));
            String uploadeStorageMode = attchmentPolicyPl.getProperty("byreference", "Y");
            if (attchmentPolicyPl.containsKey("byreference")) {
                PropertyList renameOnUploadPL;
                PropertyList plnetworkRepositoryNode;
                Node networkRepositoryNode;
                if (!contentEditable || uploadeStorageMode.length() <= 0) continue;
                msg = msg + "\nSeeding File Repository Id and File Repository Node for content editable class " + classPl.getProperty("class") + " in Sapphire Custom node of AttachmentPolicy based on \"Upload Attachment Storage Mode\" in Sapphire Custom node of pre 85 Attachment Policy";
                updateAttachmentPolicy = true;
                if (uploadeStorageMode.equalsIgnoreCase("N")) {
                    classPl.setProperty("filerepositoryid", "DatabaseFileRepository");
                    classPl.setProperty("filerepositorynode", "Sapphire Custom");
                    copyServerSide = false;
                    continue;
                }
                if (!uploadeStorageMode.equalsIgnoreCase("Y")) continue;
                PropertyList fileRefPL = attchmentPolicyPl.getPropertyListNotNull("filereference");
                PropertyList renameUploadPL = fileRefPL.getPropertyListNotNull("renameonupload");
                String rename = renameUploadPL.getProperty("rename");
                String pattern = renameUploadPL.getProperty("pattern");
                if (rename.equalsIgnoreCase("S")) {
                    classPl.setProperty("filerepositoryid", "NetworkFileRepository");
                    classPl.setProperty("filerepositorynode", "Managed Custom");
                    continue;
                }
                if (rename.equalsIgnoreCase("N")) {
                    classPl.setProperty("filerepositoryid", "NetworkFileRepository");
                    classPl.setProperty("filerepositorynode", "Unmanaged Custom");
                    copyServerSide = true;
                    networkRepositoryNode = netWorkFileRepositoryPropertyTree.getNode("Unmanaged Custom");
                    plnetworkRepositoryNode = networkRepositoryNode.getPropertyList();
                    renameOnUploadPL = plnetworkRepositoryNode.getPropertyListNotNull("renameonupload");
                    renameOnUploadPL.setProperty("rename", "N");
                    updateNetworkFileRepsitory = true;
                    continue;
                }
                classPl.setProperty("filerepositoryid", "NetworkFileRepository");
                classPl.setProperty("filerepositorynode", "UnmanagedRename Custom");
                copyServerSide = true;
                networkRepositoryNode = netWorkFileRepositoryPropertyTree.getNode("Unmanaged Custom");
                plnetworkRepositoryNode = networkRepositoryNode.getPropertyList();
                renameOnUploadPL = plnetworkRepositoryNode.getPropertyListNotNull("renameonupload");
                renameOnUploadPL.setProperty("pattern", pattern);
                renameOnUploadPL.setProperty("rename", "Y");
                updateNetworkFileRepsitory = true;
                continue;
            }
            attchmentPolicyPl.deleteProperty("byreference");
            updateAttachmentPolicy = true;
        }
        for (i = 0; i < classes.size(); ++i) {
            classPl = classes.getPropertyList(i);
            boolean bl = contentEditable = !"N".equalsIgnoreCase(classPl.getProperty("contenteditable"));
            if (contentEditable) continue;
            classes.remove(i);
            updateAttachmentPolicy = true;
        }
        if (serverSide.size() > 0) {
            if (copyServerSide) {
                Node networkRepositoryNodeSC = netWorkFileRepositoryPropertyTree.getNode("Sapphire Custom");
                PropertyList plnetworkRepositoryNode = networkRepositoryNodeSC.getPropertyList();
                plnetworkRepositoryNode.setProperty("serverside", serverSide);
                Node networkRepositoryNodeUC = netWorkFileRepositoryPropertyTree.getNode("Unmanaged Custom");
                PropertyList plnetworkRepositoryNodeUC = networkRepositoryNodeUC.getPropertyList();
                plnetworkRepositoryNodeUC.setProperty("serverside", serverSide);
                updateNetworkFileRepsitory = true;
                msg = msg + "\nMoved pre85 AttachmentPolicy -> \"Server Side Browsing\" property list from Sapphire Custom node to NetworkFileRepository Sapphire Custom Node";
            }
            attchmentPolicyPl.deleteProperty("serverside");
            updateAttachmentPolicy = true;
        }
        if (updateAttachmentPolicy && attachmentPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("AttachmentPolicy", attachmentPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "AttachmentPolicy", attchmentPolicypropertyTree.toXMLString());
        }
        if (updateNetworkFileRepsitory && netWorkFileRepository_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("NetworkFileRepository", netWorkFileRepository_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "NetworkFileRepository", netWorkFileRepositoryPropertyTree.toXMLString());
            msg = msg + "\nAll nodes of the 84 FileLocationPolicy copied as nodes in the NetworkFileRepository, preserving the paths";
        }
        return msg;
    }

    public String migrateAttachmentTypesToAttachmentElement(PropertyTree attchmentPolicypropertyTree, String attachmentPolicy_propertyTreeXML, PropertyTree attachmentelem_propertyTree, String attachmentelem_propertyTreeXML) throws SapphireException {
        String msg = "";
        Node attchmentPolicySCNode = attchmentPolicypropertyTree.getNode("Sapphire Custom");
        PropertyList attchmentPolicyPl = attchmentPolicySCNode.getPropertyList();
        PropertyListCollection attchmentTypes = attchmentPolicyPl.getCollectionNotNull("attachmenttypes");
        boolean updateAttachmentPolicy = false;
        boolean updateAttachmentElement = false;
        if (attchmentTypes.size() > 0) {
            Node attachmentElement = attachmentelem_propertyTree.getNode("Sapphire Custom");
            PropertyList attachmentElemPL = attachmentElement.getPropertyList();
            PropertyListCollection elemattchmentTypes = attachmentElemPL.getCollectionNotNull("attachmenttypes");
            if (elemattchmentTypes.size() == 0) {
                attachmentElemPL.setProperty("attachmenttypes", attchmentTypes);
                updateAttachmentElement = true;
            } else {
                for (int a = 0; a < attchmentTypes.size(); ++a) {
                    PropertyList apl = attchmentTypes.getPropertyList(a);
                    String type = apl.getProperty("type");
                    boolean typeExists = false;
                    for (int e = 0; e < elemattchmentTypes.size(); ++e) {
                        PropertyList epl = elemattchmentTypes.getPropertyList(e);
                        if (!type.equals(epl.getProperty("type"))) continue;
                        typeExists = true;
                        break;
                    }
                    if (typeExists) continue;
                    elemattchmentTypes.add(apl);
                    updateAttachmentElement = true;
                }
            }
            attchmentPolicyPl.deleteProperty("attachmenttypes");
            updateAttachmentPolicy = true;
            msg = msg + "\nMoved pre85 AttachmentPolicy -> \"AttachmentTypes \" collection from Sapphire Custom node to Attachment element Sapphire Custom Node";
        }
        if (updateAttachmentElement && attachmentelem_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("attachment", attachmentelem_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "attachment", attachmentelem_propertyTree.toXMLString());
        }
        if (updateAttachmentPolicy && attachmentPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("AttachmentPolicy", attachmentPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "AttachmentPolicy", attchmentPolicypropertyTree.toXMLString());
        }
        return msg;
    }

    public String migrateAttachmentElementClassToFilterClassCollection(PropertyTree attachmentelem_propertyTree, String attachmentelem_propertyTreeXML) throws SapphireException {
        String msg = "";
        boolean updateAttachmentElement = false;
        ArrayList allachmentelem_allNodes = attachmentelem_propertyTree.getAllNodes();
        long propertyListIdCount = System.currentTimeMillis();
        for (Node node : allachmentelem_allNodes) {
            if (!node.isCustom()) continue;
            PropertyList nodepl = node.getPropertyList();
            String attachmentclass = nodepl.getProperty("attachmentclass");
            if (node.getNodeId().equals("Sapphire Custom")) {
                if (attachmentclass.length() > 0) {
                    PropertyListCollection collection = nodepl.getCollectionNotNull("filterclasses");
                    PropertyList newPl = new PropertyList();
                    newPl.setProperty("class", attachmentclass);
                    newPl.setSequence(100000L);
                    String id = Long.toString(attachmentelem_propertyTree.getUniquePropertyListId(propertyListIdCount));
                    newPl.setId(id);
                    collection.add(newPl);
                    nodepl.setProperty("filterclasses", collection);
                    nodepl.setProperty("defaultclass", attachmentclass);
                    nodepl.deleteProperty("attachmentclass");
                    updateAttachmentElement = true;
                }
                if (!updateAttachmentElement) continue;
                PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "attachment", attachmentelem_propertyTree.toXMLString());
                continue;
            }
            if (attachmentclass.length() <= 0) continue;
            PropertyList inheritedPropertyList = attachmentelem_propertyTree.getNodePropertyList(node.getNodeId(), true);
            PropertyListCollection filterclassesInherited = inheritedPropertyList.getCollectionNotNull("filterclasses");
            PropertyListCollection filterclassesOnThisNode = nodepl.getCollectionNotNull("filterclasses");
            if (filterclassesInherited.size() == 0) {
                PropertyList newPl = new PropertyList();
                newPl.setProperty("class", attachmentclass);
                String id = Long.toString(attachmentelem_propertyTree.getUniquePropertyListId(propertyListIdCount));
                newPl.setId(id);
                newPl.setSequence(100000L);
                filterclassesOnThisNode.add(newPl);
                nodepl.setProperty("filterclasses", filterclassesOnThisNode);
            } else {
                String overrideId = filterclassesInherited.getPropertyList(0).getId();
                PropertyList overrideCollectionItem = filterclassesOnThisNode.getPropertyList(overrideId);
                if (overrideCollectionItem == null) {
                    overrideCollectionItem = new PropertyList(overrideId);
                    String id = Long.toString(attachmentelem_propertyTree.getUniquePropertyListId(propertyListIdCount));
                    overrideCollectionItem.setId(id);
                    overrideCollectionItem.setSequence(100000L);
                    filterclassesOnThisNode.add(overrideCollectionItem);
                }
                overrideCollectionItem.setProperty("class", attachmentclass);
            }
            nodepl.setProperty("defaultclass", attachmentclass);
            nodepl.deleteProperty("attachmentclass");
            node.setPropertyList(nodepl);
            updateAttachmentElement = true;
        }
        if (updateAttachmentElement && attachmentelem_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("attachment", attachmentelem_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "attachment", attachmentelem_propertyTree.toXMLString());
            msg = msg + "\n Moved pre85 AttachmentClass property in every Custom node of the 84 Attachment Element to the first item in the FilterAttachmentClass collection of the Attachment Element in the respective Custom node";
        }
        return msg;
    }

    public String migrateAttachmentExcludesToAttachmentElement(PropertyTree attachmentelem_propertyTree, String attachmentelem_propertyTreeXML, PropertyTree filelocationPolicypropertyTree, String filelocationPolicy_propertyTreeXML) throws SapphireException {
        boolean updateAttachmentElement = false;
        boolean updateFileLocationPolicy = false;
        String msg = "";
        Node filelocationPolicyATCNode = filelocationPolicypropertyTree.getNode("Attachment Custom");
        PropertyList fileLocationATCNodePl = filelocationPolicyATCNode.getPropertyList();
        PropertyList attchmentExcludes = new PropertyList();
        if (fileLocationATCNodePl.containsKey("attachmentexcludes")) {
            attchmentExcludes = fileLocationATCNodePl.getPropertyListNotNull("attachmentexcludes");
            updateFileLocationPolicy = true;
        }
        Node filelocationPolicySCNode = filelocationPolicypropertyTree.getNode("Sapphire Custom");
        PropertyList fileLocationSCNodePl = filelocationPolicySCNode.getPropertyList();
        PropertyList scExcludes = new PropertyList();
        if (fileLocationSCNodePl.containsKey("attachmentexcludes")) {
            scExcludes = fileLocationSCNodePl.getPropertyListNotNull("attachmentexcludes");
            updateFileLocationPolicy = true;
        }
        if (attchmentExcludes.size() > 0) {
            Node node = attachmentelem_propertyTree.getNode("Sapphire Custom");
            PropertyList nodepl = node.getPropertyList();
            PropertyList excludePL = nodepl.getPropertyListNotNull("filetypeexcludes");
            excludePL.setProperty("filetypestoexclude", attchmentExcludes.getProperty("filetypestoexclude", scExcludes.getProperty("filetypestoexclude")));
            excludePL.setProperty("filetypestoinclude", attchmentExcludes.getProperty("filetypestoinclude", scExcludes.getProperty("filetypestoinclude")));
            nodepl.setProperty("filetypeexcludes", excludePL);
            updateAttachmentElement = true;
            msg = msg + "\nMoved pre85 Attachment Excludes PropertyList from FileLocationPolicy Attachment Custom node to Sapphire Custom node of Attachment Element";
        } else if (scExcludes.size() > 0) {
            Node node = attachmentelem_propertyTree.getNode("Sapphire Custom");
            PropertyList nodepl = node.getPropertyList();
            PropertyList excludePL = nodepl.getPropertyListNotNull("filetypeexcludes");
            excludePL.setProperty("filetypestoexclude", scExcludes.getProperty("filetypestoexclude"));
            excludePL.setProperty("filetypestoinclude", scExcludes.getProperty("filetypestoinclude"));
            nodepl.setProperty("filetypeexcludes", excludePL);
            updateAttachmentElement = true;
            msg = msg + "\nMoved pre85 Attachment Excludes PropertyList from FileLocationPolicy Sapphire Custom node to Sapphire Custom node of Attachment Element";
        }
        if (updateFileLocationPolicy) {
            fileLocationATCNodePl.deleteProperty("attachmentexcludes");
            fileLocationSCNodePl.deleteProperty("attachmentexcludes");
        }
        if (updateAttachmentElement && attachmentelem_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("attachment", attachmentelem_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "attachment", attachmentelem_propertyTree.toXMLString());
        }
        if (updateFileLocationPolicy && filelocationPolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("FileLocationPolicy", filelocationPolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "FileLocationPolicy", filelocationPolicypropertyTree.toXMLString());
        }
        return msg;
    }

    public String copyFileLocationPolicyCustomNodePathToNetworkFileRepository(PropertyTree netWorkFileRepositoryPropertyTree, String netWorkFileRepository_propertyTreeXML, PropertyTree filelocationPolicypropertyTree) throws SapphireException {
        String msg = "";
        boolean updateNetworkFileRepsitory = false;
        Node sapphireCustomNode = netWorkFileRepositoryPropertyTree.getNode("Sapphire Custom");
        ArrayList allNodes = filelocationPolicypropertyTree.getAllNodes();
        for (Node node : allNodes) {
            if (!node.isCustom() && node.isProduct() || !this.setNodeLocationPath(node, sapphireCustomNode, netWorkFileRepositoryPropertyTree, filelocationPolicypropertyTree)) continue;
            updateNetworkFileRepsitory = true;
        }
        if (updateNetworkFileRepsitory && netWorkFileRepository_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("NetworkFileRepository", netWorkFileRepository_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "NetworkFileRepository", netWorkFileRepositoryPropertyTree.toXMLString());
            msg = msg + "\nAll nodes of the 84 FileLocationPolicy copied as nodes in the NetworkFileRepository, preserving the paths";
        }
        return msg;
    }

    public String updatePre85ELNPolicy(PropertyTree elnPolicyPropertyTree, String elnpolicy_propertyTreeXML) throws SapphireException {
        String msg = "";
        boolean updateELNPolicy = false;
        ArrayList elnpolicy_allNodes = elnPolicyPropertyTree.getAllNodes();
        for (Node node : elnpolicy_allNodes) {
            PropertyList elnPL;
            PropertyList plElnAttachment;
            if (!node.isCustom() || !(plElnAttachment = (elnPL = node.getPropertyList()).getPropertyListNotNull("attachments")).containsKey("attachbyrefpolicynode")) continue;
            plElnAttachment.deleteProperty("attachbyrefpolicynode");
            plElnAttachment.deleteProperty("attachmentsbyref");
            plElnAttachment.deleteProperty("attachbyreffilelocation");
            updateELNPolicy = true;
        }
        if (updateELNPolicy && elnpolicy_propertyTreeXML.length() > 0) {
            this.createPre85ValueTreeBackup("ELNPolicy", elnpolicy_propertyTreeXML);
            PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), "ELNPolicy", elnPolicyPropertyTree.toXMLString());
            msg = msg + "\nRemoved old properties from ELNPolicy";
        }
        return msg;
    }

    private boolean setNodeLocationPath(Node node, Node parentNode, PropertyTree netWorkFileRepositoryPropertyTree, PropertyTree filelocationPolicypropertyTree) throws SapphireException {
        boolean updateNWFileRepository = false;
        PropertyList fileLocationCustomNodePL = filelocationPolicypropertyTree.getNodePropertyList(node.getNodeId(), true);
        String startLocation = fileLocationCustomNodePL.getProperty("startlocation").trim();
        PropertyListCollection locs = fileLocationCustomNodePL.getCollectionNotNull("locations");
        String netWorkPath = "";
        if (locs.size() > 0) {
            if (startLocation.length() > 0) {
                PropertyList startLocationPL = locs.find("id", startLocation, true);
                if (startLocationPL != null) {
                    netWorkPath = startLocationPL.getProperty("location");
                }
            } else {
                PropertyList locPl;
                for (int i = 0; i < locs.size() && (netWorkPath = (locPl = locs.getPropertyList(i)).getProperty("location")).length() <= 0; ++i) {
                }
            }
        }
        if (netWorkPath.length() > 0) {
            String nodeId;
            int index;
            Node netWorkFileNode = netWorkFileRepositoryPropertyTree.getNode(node.getNodeId());
            if (netWorkFileNode == null && (index = (nodeId = node.getNodeId()).indexOf(" Custom")) > -1) {
                nodeId = nodeId.substring(0, nodeId.indexOf(" Custom"));
                netWorkFileNode = netWorkFileRepositoryPropertyTree.getNode(nodeId);
            }
            if (netWorkFileNode != null) {
                PropertyList netWorkFileNodePL = netWorkFileNode.getPropertyList();
                String oldPath = netWorkFileNodePL.getProperty("locationpath");
                if (oldPath.length() == 0) {
                    netWorkFileNodePL.setProperty("locationpath", netWorkPath);
                }
            } else {
                Node netWorkSCNode = netWorkFileRepositoryPropertyTree.getNode("Sapphire Custom");
                PropertyList serverside = netWorkSCNode.getPropertyList().getPropertyListNotNull("serverside");
                if ("Y".equalsIgnoreCase(serverside.getProperty("allow")) && node.getNodeId().equals(serverside.getProperty("locationpolicynode")) && parentNode.getNodeId().equals("Sapphire Custom")) {
                    parentNode = netWorkFileRepositoryPropertyTree.getNode("Unmanaged Custom");
                }
                this.createNode(node, parentNode, netWorkPath);
            }
            updateNWFileRepository = true;
        }
        return updateNWFileRepository;
    }

    private void createNode(Node node, Node parentNode, String startLocation) {
        node.setCollapseAncestors(true);
        String nodeId = node.getNodeId();
        int index = nodeId.indexOf(" Custom");
        if (index > -1) {
            nodeId = nodeId.substring(0, nodeId.indexOf(" Custom"));
        }
        Node newCustomNode = new Node(nodeId);
        PropertyList props = new PropertyList();
        props.setProperty("locationpath", startLocation);
        newCustomNode.setPropertyList(props);
        parentNode.getNodeList().add(newCustomNode);
        NodeList subchildNodes = node.getNodeList();
        for (int n = 0; n < subchildNodes.size(); ++n) {
            Node childNode = (Node)subchildNodes.get(n);
            childNode.setCollapseAncestors(true);
            PropertyList nodePL = childNode.getPropertyList();
            PropertyListCollection locs = nodePL.getCollectionNotNull("locations");
            String netWorkPath = "";
            if (locs.size() > 0) {
                if (startLocation.length() > 0) {
                    PropertyList startLocationPL = locs.find("id", startLocation, true);
                    if (startLocationPL != null) {
                        netWorkPath = startLocationPL.getProperty("location");
                    }
                } else {
                    PropertyList locPl;
                    for (int i = 0; i < locs.size() && (netWorkPath = (locPl = locs.getPropertyList(i)).getProperty("location")).length() <= 0; ++i) {
                    }
                }
            }
            this.createNode(childNode, newCustomNode, netWorkPath);
        }
    }

    private void createPre85ValueTreeBackup(String propertyTree, String pre85PropertyTreeXml) throws SapphireException {
        boolean created;
        File destination_Directory;
        String timeStamp = "" + Calendar.getInstance().getTimeInMillis();
        String fileName = "pre85_" + propertyTree + "_" + timeStamp + ".xml";
        String folderPath = "";
        if (this.labvantageHome == null || this.labvantageHome.length() == 0) {
            try {
                folderPath = new File(".").getCanonicalPath() + "/pre85propertytree_backup";
            }
            catch (IOException iOException) {}
        } else {
            folderPath = this.labvantageHome + "/pre85propertytree_backup";
        }
        if (!(destination_Directory = new File(folderPath)).exists() && !(created = destination_Directory.mkdirs())) {
            throw new SapphireException("Directory cannot be created to store ore 85 propertytree backup XML files.");
        }
        File file = new File(folderPath + "/" + fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(pre85PropertyTreeXml.getBytes());
            fos.close();
            Logger.logTrace(2, "Created backup file of pre 85 " + propertyTree + " . File name :" + file.getAbsolutePath());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create backup file of pre 85 " + propertyTree + ErrorUtil.extractMessageFromException(e, true));
        }
        finally {
            try {
                fos.close();
            }
            catch (IOException iOException) {}
        }
    }
}

