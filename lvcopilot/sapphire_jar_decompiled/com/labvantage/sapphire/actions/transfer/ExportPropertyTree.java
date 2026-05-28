/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.transfer.TransferAction;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeBuilder;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.StringLogger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportPropertyTree
extends BaseAction
implements sapphire.action.ExportPropertyTree,
TransferAction {
    public static final String PROPERTY_CREATEANTFILE = "createantfile";
    public static final String PROPERTY_EXPORTLOG = "exportlog";
    public static final String PROPERTY_TEMPFILEOUTPUT = "tempfileoutput";
    public static final String PROPERTY_TEMPID = "tempid";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        ArrayList<Column> columns;
        SDITransfer sdi;
        String propertytreeid = props.getProperty("propertytreeid");
        String exporttype = props.getProperty("exporttype", "propertytree");
        boolean generateTempFile = props.getProperty(PROPERTY_TEMPFILEOUTPUT, "N").equalsIgnoreCase("Y");
        boolean useZipFile = false;
        String dirname = "";
        if (generateTempFile) {
            useZipFile = true;
            try {
                dirname = FileManager.TempFile.createTempDir();
            }
            catch (Exception e) {
                throw new SapphireException("Failed to create temporary directory.", e);
            }
        } else {
            useZipFile = props.getProperty("zipoutput", "N").equals("Y");
            dirname = props.getProperty("dirname");
        }
        String filename = dirname + File.separator + props.getProperty("filename");
        ArrayList<Cloneable> transferList = new ArrayList<Cloneable>();
        if (exporttype.equals("propertytree")) {
            if (props.getProperty("nodeexists").equals("replace")) {
                sdi = new SDITransfer("PropertyTree");
                sdi.setKeyid1(propertytreeid);
                sdi.setPrimaryForceUpdate("true");
                transferList.add(sdi);
            } else {
                sdi = new SDITransfer("PropertyTree");
                sdi.setKeyid1(propertytreeid);
                sdi.setPrimaryForceUpdate("true");
                sdi.setTransferOption("forcedevmode", props.getProperty("forcedevmode"));
                columns = new ArrayList();
                Column all = new Column();
                all.setColumnid("*");
                all.setForceUpdate("true");
                all.setForceNullUpdate("false");
                Column valuetree = new Column();
                valuetree.setColumnid("valuetree");
                valuetree.setFile("[tableid]" + File.separator + "[rowkey]_[columnid].xml");
                valuetree.setForceUpdate("true");
                PropertyTreeTransfer propertytree = new PropertyTreeTransfer(propertytreeid);
                propertytree.setTransferOption("forcedevmode", props.getProperty("forcedevmode"));
                propertytree.setExists("merge");
                propertytree.setNotexists("add");
                Node allnodes = new Node("*");
                allnodes.setExists("default");
                allnodes.setNotexists("add");
                NodeList nodes = new NodeList();
                nodes.add(allnodes);
                propertytree.setNodeList(nodes);
                valuetree.setPropertyTreeTransfer(propertytree);
                columns.add(valuetree);
                columns.add(all);
                sdi.setColumns(columns);
                transferList.add(sdi);
            }
        } else if (exporttype.equals("definitiononly")) {
            sdi = new SDITransfer("PropertyTree");
            sdi.setKeyid1(propertytreeid);
            sdi.setPrimaryForceUpdate("true");
            sdi.setTransferOption("forcedevmode", props.getProperty("forcedevmode"));
            columns = new ArrayList<Column>();
            Column id = new Column();
            id.setColumnid("propertytreeid");
            columns.add(id);
            Column propertytreetype = new Column();
            propertytreetype.setColumnid("propertytreetype");
            columns.add(propertytreetype);
            Column objectname = new Column();
            objectname.setColumnid("objectname");
            objectname.setForceUpdate("true");
            columns.add(objectname);
            Column definitiontree = new Column();
            definitiontree.setForceUpdate("true");
            definitiontree.setColumnid("definitiontree");
            definitiontree.setFile("[tableid]" + File.separator + "[rowkey]_[columnid].xml");
            columns.add(definitiontree);
            sdi.setColumns(columns);
            transferList.add(sdi);
        } else {
            String nodelist = (String)props.get("nodelist");
            if (nodelist != null && nodelist.length() > 0) {
                String[] nodearray = StringUtil.split(nodelist, ";");
                PropertyTreeTransfer propertytree = new PropertyTreeTransfer(propertytreeid);
                propertytree.setTransferOption("forcedevmode", props.getProperty("forcedevmode"));
                propertytree.setExists("merge");
                propertytree.setNotexists("add");
                NodeList nodes = new NodeList();
                for (int i = 0; i < nodearray.length; ++i) {
                    Node node = new Node(nodearray[i]);
                    node.setCollapseAncestors(true);
                    if (exporttype.equals("properties")) {
                        node.setExists("merge");
                        node.setNotexists("add");
                    } else {
                        node.setExists(props.getProperty("nodeexists", "default"));
                        node.setNotexists("add");
                        if (node.getExists().equals("merge")) {
                            PropertyListTransfer transferPropertyList = new PropertyListTransfer();
                            transferPropertyList.setExists(props.getProperty("propertyexists", "replace"));
                            transferPropertyList.setNotexists("add");
                            node.setPropertyList(transferPropertyList);
                        }
                    }
                    nodes.add(node);
                    if (!exporttype.equals("properties")) continue;
                    String xml = (String)props.get("propertylistxml");
                    PropertyListTransfer transferPropertyList = new PropertyListTransfer();
                    transferPropertyList.setExists(props.getProperty("propertyexists", "replace"));
                    transferPropertyList.setNotexists("add");
                    transferPropertyList.setPropertyList(xml);
                    node.setPropertyList(transferPropertyList);
                }
                propertytree.setNodeList(nodes);
                transferList.add(propertytree);
            }
        }
        File file = new File(filename);
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        StringLogger logger = new StringLogger();
        try {
            ExportXML export = new ExportXML(this.database, transferList, file);
            FileManager.TempFile tempZipFile = null;
            if (useZipFile) {
                File zipFile = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip");
                if (generateTempFile) {
                    tempZipFile = new FileManager.TempFile(zipFile.getName(), FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                    zipFile = tempZipFile.getData().getFile().toFile();
                }
                if (zipFile != null) {
                    zipFile.getParentFile().mkdirs();
                    zipFos = new FileOutputStream(zipFile);
                    zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
                    export.setZipOut(zipOut);
                }
            }
            export.setHeaderAttribute("esigpassword", props.getProperty("esigpassword", "N"));
            export.setHeaderAttribute("esigreason", props.getProperty("esigreason", "N"));
            export.setHeaderAttribute("logimport", props.getProperty("logimport", "Y"));
            export.setHeaderAttribute("checksum", props.getProperty("checksum", "N"));
            export.setExportLog(logger);
            export.export();
            StringBuilder tempid = new StringBuilder();
            if (generateTempFile && tempZipFile != null) {
                String id = tempZipFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                tempid.append(id);
            }
            props.setProperty(PROPERTY_EXPORTLOG, logger.getLog());
            if (props.getProperty(PROPERTY_CREATEANTFILE).equals("Y")) {
                String antfilename = StringUtil.replaceAll(filename, "/", "\\");
                int pos = antfilename.lastIndexOf("\\");
                antfilename = pos == -1 ? "ant_" + antfilename : antfilename.substring(0, pos + 1) + "ant_" + antfilename.substring(pos + 1);
                File antfile = new File(antfilename);
                FileManager.TempFile tempAntFile = null;
                if (generateTempFile) {
                    tempAntFile = new FileManager.TempFile(antfile.getName(), FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                    antfile = tempAntFile.getData().getFile().toFile();
                    export.setFilename(null);
                }
                HashMap antProps = new HashMap();
                antProps.putAll(AntUtil.getConnectionProps(this.connectionInfo));
                export.generateAntScript(antfile, "export", antProps);
                if (generateTempFile && tempAntFile != null) {
                    String id = tempAntFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                    tempid.append(";").append(id);
                }
            }
            props.setProperty(PROPERTY_TEMPID, tempid.toString());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to export property tree " + propertytreeid + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (zipFos != null) {
                    zipFos.close();
                }
            }
            catch (IOException iOException) {}
            if (generateTempFile) {
                try {
                    Files.delete(FileSystems.getDefault().getPath(dirname, new String[0]));
                }
                catch (Exception e2) {
                    this.logTrace("Failed to delete temp directory.");
                }
            }
        }
    }

    @Override
    public PropertyList convertWizardProperties(PropertyList wizardProps, String connectionid) throws Exception {
        String nodeid;
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("propertytreeid", wizardProps.getProperty("propertytreeid"));
        actionProps.setProperty("exporttype", wizardProps.getProperty("exporttype"));
        actionProps.setProperty("nodelist", wizardProps.getProperty("nodelist"));
        actionProps.setProperty("nodeexists", wizardProps.getProperty("nodeexists"));
        actionProps.setProperty("propertyexists", wizardProps.getProperty("propertyexists"));
        actionProps.setProperty("esigpassword", wizardProps.getProperty("esigpassword"));
        actionProps.setProperty("esigreason", wizardProps.getProperty("esigreason"));
        actionProps.setProperty("logimport", wizardProps.getProperty("logimport"));
        actionProps.setProperty("checksum", wizardProps.getProperty("checksum"));
        actionProps.setProperty("zipoutput", wizardProps.getProperty("zipoutput"));
        actionProps.setProperty(PROPERTY_CREATEANTFILE, wizardProps.getProperty(PROPERTY_CREATEANTFILE));
        actionProps.setProperty("dirname", wizardProps.getProperty("folderlocation"));
        actionProps.setProperty("filename", wizardProps.getProperty("filename"));
        actionProps.setProperty(PROPERTY_TEMPFILEOUTPUT, wizardProps.getProperty(PROPERTY_TEMPFILEOUTPUT));
        String propertytreeid = wizardProps.getProperty("propertytreeid");
        String exporttype = wizardProps.getProperty("exporttype");
        if (exporttype.equals("properties") && (nodeid = wizardProps.getProperty("nodelist")).length() > 0) {
            WebAdminProcessor wp = new WebAdminProcessor(connectionid);
            PropertyTree pt = wp.getPropertyTree(propertytreeid);
            PropertyList reference = pt.getNodePropertyList(nodeid, true);
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(wizardProps.getProperty("propertycount"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            HashSet<String> includeList = new HashSet<String>();
            for (int i = 0; i < propertyCount; ++i) {
                String id = wizardProps.getProperty("property_" + i);
                if (id.length() <= 0) continue;
                includeList.add(id);
            }
            PropertyList transferPropertyList = PropertyTreeBuilder.buildExportPropertyList(reference, "EXPORT__root_0", includeList);
            actionProps.setProperty("propertylistxml", transferPropertyList.toXMLString());
        }
        return actionProps;
    }
}

