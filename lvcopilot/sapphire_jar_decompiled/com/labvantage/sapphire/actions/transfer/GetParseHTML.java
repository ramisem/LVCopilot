/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetParseHTML
extends BaseAction
implements TransferConstants {
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TEMPID = "tempid";
    public static final String PROPERTY_PARSEDHTML = "parsedhtml";
    private boolean regenKeys = false;

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String filename = props.getProperty(PROPERTY_FILENAME);
        String tempid = props.getProperty(PROPERTY_TEMPID);
        File file = null;
        if (tempid.length() > 0) {
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, this.getQueryProcessor(), this.getConnectionId());
            if (tempFile != null) {
                FileManager.FileData fileData = tempFile.getData();
                if (fileData != null) {
                    file = fileData.getFile().toFile();
                } else {
                    this.logger.warn("Failed to obtain file data.");
                }
            } else {
                this.logger.warn("Failed to obtain temp file.");
            }
        } else {
            file = new File(filename);
        }
        if (file == null) {
            throw new SapphireException("Failed to find import file.");
        }
        ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
        String checksumProcessing = cp.getSysConfigProperty("checksumprocessing", "I");
        props.setProperty("importprocessing", cp.getSysConfigProperty("importprocessing", "F"));
        this.regenKeys = props.getProperty("regenkeys", "N").equals("Y");
        ImportXML importer = new ImportXML(this.database, file);
        importer.setCommitScope("off");
        importer.setIgnoreMissingObjects(props.getProperty("ignoremissingobjects", "N").equals("Y"));
        importer.setRegenKeys(this.regenKeys);
        importer.setChecksumProcessing(checksumProcessing);
        List imports = importer.parseFiles();
        if (props.getProperty("importobjectsonly", "N").equals("N")) {
            StringBuffer out = new StringBuffer();
            try {
                for (int i = 0; i < imports.size(); ++i) {
                    List fileImports = (List)imports.get(i);
                    PropertyList fileProps = (PropertyList)fileImports.get(0);
                    props.putAll(fileProps);
                    out.append("<p><b>File: " + fileProps.getProperty(fileProps.getProperty("zipfile").length() > 0 ? "zipfileentry" : "importfilename") + "</b></p>");
                    out.append("<table cellspacing=\"0\" cellpadding=\"2\" class=\"gridtable\">");
                    for (int j = 1; j < fileImports.size(); ++j) {
                        Transferable transferable = (Transferable)fileImports.get(j);
                        if (transferable instanceof PropertyTreeTransfer) {
                            out.append(this.getPropertyTreeHTML((PropertyTreeTransfer)transferable));
                            continue;
                        }
                        if (transferable instanceof SDITransfer) {
                            out.append(this.getSDIHTML((SDITransfer)transferable));
                            continue;
                        }
                        if (transferable instanceof SDCTransfer) {
                            out.append(this.getSDCHTML((SDCTransfer)transferable));
                            continue;
                        }
                        if (transferable instanceof TableTransfer) {
                            out.append(this.getTableHTML((TableTransfer)transferable));
                            continue;
                        }
                        out.append("Importing ").append(transferable.getParsedData());
                    }
                    out.append("</table><hr>");
                }
                props.setProperty(PROPERTY_PARSEDHTML, out.toString());
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage(), e);
            }
        } else {
            props.put("importobjects", imports);
        }
    }

    private StringBuffer getSDCHTML(SDCTransfer transfer) {
        StringBuffer out = new StringBuffer("");
        String[] sdclist = StringUtil.split(transfer.getSdcid(), ";");
        out.append("<tr><td class=\"gridtitle\" colspan=\"4\"><b>Data model import</b></td>");
        out.append("<tr>");
        out.append("<td class=\"gridtitle\" width=\"20\">&nbsp;</td>");
        out.append("<td class=\"gridtitle\">Id</td>");
        out.append("<td class=\"gridtitle\" colspan=\"2\" >Operation</td>");
        out.append("</tr>");
        for (int i = 0; i < sdclist.length; ++i) {
            out.append("<tr>");
            out.append("<td class=\"gridcell\">&nbsp;</td>");
            out.append("<td class=\"gridcell\">" + sdclist[i] + "</td>");
            out.append("<td class=\"gridcell\" colspan=\"2\">Data model will be updated to match the definition in this file.</td>");
            out.append("</tr>");
        }
        return out;
    }

    private StringBuffer getSDIHTML(SDITransfer transfer) {
        StringBuffer out = new StringBuffer("");
        String sdcid = transfer.getSdcid();
        SDCProcessor sdcp = this.getSDCProcessor();
        String keycolid1 = sdcp.getProperty(sdcid, "keycolid1", transfer.getPrimaryTableid() + "id");
        String keycolid2 = sdcp.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcp.getProperty(sdcid, "keycolid3");
        String singular = sdcp.getProperty(sdcid, "singular", sdcid.toLowerCase());
        DataSet keys = (DataSet)transfer.getParsedData();
        ArrayList<String> altkeycolumns = transfer.getAltkeycols();
        out.append("<tr><td class=\"gridtitle\" colspan=\"4\"><b>" + sdcid + " import</b></td>");
        if (keys.size() > 0) {
            out.append("<tr>");
            out.append("<td class=\"gridtitle\" width=\"20\">&nbsp;</td>");
            out.append("<td class=\"gridtitle\">").append(altkeycolumns == null || altkeycolumns.size() == 0 ? "Id" : "Alt Id").append("</td>");
            out.append("<td class=\"gridtitle\" colspan=\"2\" >Operation</td>");
            out.append("</tr>");
            for (int i = 0; i < keys.size(); ++i) {
                String key = "";
                if (altkeycolumns == null || altkeycolumns.size() == 0) {
                    String keyid1 = keys.getValue(i, keycolid1);
                    String keyid2 = keycolid2.length() > 0 ? keys.getValue(i, keycolid2) : "";
                    String keyid3 = keycolid3.length() > 0 ? keys.getValue(i, keycolid3) : "";
                    key = keyid1 + (keycolid2.length() > 0 ? ";" + keyid2 : "") + (keycolid3.length() > 0 ? ";" + keyid3 : "");
                } else {
                    for (int j = 0; j < altkeycolumns.size(); ++j) {
                        key = key + ";" + keys.getValue(i, altkeycolumns.get(j));
                    }
                    key = key.substring(1);
                }
                int operation = keys.getInt(i, "__importaction");
                out.append("<tr>");
                out.append("<td class=\"gridcell\">&nbsp;</td>");
                out.append("<td class=\"gridcell\">" + key + "</td>");
                out.append("<td class=\"gridcell\" colspan=\"2\">" + (transfer.isFlushSDI() ? "The " + singular + " will be <b>DELETED</b> prior to being <b>ADDED</b> back into the datbase. " : (this.regenKeys ? "A new key will be generated for the " + singular + (operation == 2 || operation == 0 ? " (existing key exists in the database)" : "") : (operation == 2 ? "<span style=\"color:red\">The " + singular + " already exists in the database. It will be <b>UPDATED</b> if you continue with the import.</span>" : (operation == 1 ? "The " + singular + " will be <b>ADDED</b> into the database" : (operation == 0 ? "The " + singular + " already exists in the database. Any missing " + singular + " data may be <b>ADDED</b> if you continue with the import." : "Unknown Operation"))))) + "</td>");
                out.append("</tr>");
            }
        } else {
            out.append("<tr><td class=\"gridcell\" colspan=\"4\">No " + singular + "s found - definition only</td>");
        }
        return out;
    }

    private StringBuffer getTableHTML(TableTransfer transfer) throws SapphireException {
        StringBuffer out = new StringBuffer("");
        String tableid = transfer.getTableid();
        String[] keyCols = DDTService.getKeyColumns(this.database, tableid);
        DataSet keys = (DataSet)transfer.getParsedData();
        out.append("<tr><td class=\"gridtitle\" colspan=\"4\"><b>" + tableid + " table import</b></td>");
        if (keys.size() > 0) {
            out.append("<tr>");
            out.append("<td class=\"gridtitle\" width=\"20\">&nbsp;</td>");
            out.append("<td class=\"gridtitle\">Id</td>");
            out.append("<td class=\"gridtitle\" colspan=\"2\" >Operation</td>");
            out.append("</tr>");
            for (int i = 0; i < keys.size(); ++i) {
                StringBuffer keyid = new StringBuffer();
                for (int j = 0; j < keyCols.length; ++j) {
                    keyid.append(";").append(keys.getValue(i, keyCols[j]));
                }
                int operation = keys.getInt(i, "__importaction");
                out.append("<tr>");
                out.append("<td class=\"gridcell\">&nbsp;</td>");
                out.append("<td class=\"gridcell\">" + (keyid.length() > 0 ? keyid.substring(1) : "No id") + "</td>");
                out.append("<td class=\"gridcell\" colspan=\"2\">" + (operation == 2 ? "<span style=\"color:red\">The record already exists in the database. It will be <b>Replaced</b> if you continue with the import.</span>" : (operation == 1 ? "The record will be <b>Added</b> into the database" : (operation == 0 ? "The record already exists in the database and will NOT be imported." : "Unknown Operation"))) + "</td>");
                out.append("</tr>");
            }
        } else {
            out.append("<tr><td class=\"gridcell\" colspan=\"4\">No table data found - definition only</td>");
        }
        return out;
    }

    private StringBuffer getPropertyTreeHTML(PropertyTreeTransfer transfer) throws Exception {
        StringBuffer out = new StringBuffer();
        String importid = transfer.getId();
        if (importid != null && importid.length() > 0) {
            out.append("<tr><td class=\"gridtitle\" colspan=\"4\"><b>" + importid + " propertytree import</b></td>");
            PropertyTree propertyTree = null;
            try {
                WebAdminProcessor wp = new WebAdminProcessor(this.connectionInfo.getConnectionId());
                propertyTree = wp.getPropertyTree(importid);
            }
            catch (Exception e) {
                out.append("<tr>");
                out.append("<td class=\"gridcell\">&nbsp;</td>");
                out.append("<td class=\"gridcell\">PropertyTree Nodes</td>");
                out.append("<td class=\"gridcell\" colspan=\"2\">Nodes will be imported into the newly created PropertyTree</td>");
                out.append("</tr>");
                return out;
            }
            ArrayList nodes = transfer.getAllNodes();
            out.append("<tr><td class=\"gridtitle\" colspan=\"4\"><b>" + importid + " propertytree import</b></td>");
            out.append("<tr>");
            out.append("<td>&nbsp;</td>");
            out.append("<td width=\"120\" class=\"gridtitle\">Node</td>");
            out.append("<td width=\"120\" class=\"gridtitle\">Extends Node</td>");
            out.append("<td width=\"300\" class=\"gridtitle\">Operation</td>");
            out.append("</tr>");
            HashSet<String> nodesSoFar = new HashSet<String>();
            for (Node node : nodes) {
                String nodeid = node.getId();
                String extendsNodeid = node.getExtendsNodeId();
                String message = "OK";
                boolean problem = false;
                nodesSoFar.add(nodeid);
                if (propertyTree.getNode(extendsNodeid) == null && !extendsNodeid.equals("root") && !nodesSoFar.contains(extendsNodeid)) {
                    problem = true;
                    message = "Could not find extend node " + extendsNodeid;
                }
                if (!problem) {
                    String op = propertyTree.getNode(nodeid) == null ? node.getNotexists() : node.getExists();
                    message = op + " node " + nodeid;
                }
                out.append("<tr>");
                out.append("<td>&nbsp;</td>");
                out.append("<td class=\"gridcell\">" + nodeid + "</td>");
                out.append("<td class=\"gridcell\">" + extendsNodeid + "</td>");
                out.append("<td class=\"gridcell\">" + message + "</td>");
                out.append("</tr>");
            }
        }
        return out;
    }
}

