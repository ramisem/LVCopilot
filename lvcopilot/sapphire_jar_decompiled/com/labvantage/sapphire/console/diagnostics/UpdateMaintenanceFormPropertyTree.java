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
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class UpdateMaintenanceFormPropertyTree
extends BaseDiagnostic {
    static final String LABVANTAGE_CVS_ID = "$Revision: 86177 $";

    public UpdateMaintenanceFormPropertyTree(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    @Override
    public String getTitle() {
        return "Update MaintenanceForm PageType Propertytree with the custom nodes of clone PageTypes";
    }

    @Override
    public String getDescription() {
        return "Update MaintenanceForm PageType Propertytree with the custom nodes of clone PageTypes";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        String[] clonePageTypes = new String[]{"QCBatchMaintForm", "ProtocolMaintForm", "StudyMaintForm", "StorageUnitMaintForm", "StorageCondMaint", "StorageEnvMaint", "SMSSampleMaintForm", "IncidentDetailMaint"};
        String[] nodes = new String[]{"QCBatch Custom", "Protocol Custom", "StudySDC Custom", "StorageUnitSDC Custom", "StorageCondTypeSDC Custom", "StorageEnvSDC Custom", "SMSSampleMaint Custom", "LV_Incdt Custom"};
        String[] nodeNames = new String[]{"QCBatch", "Protocol", "StudySDC", "StorageUnitSDC", "StorageCondTypeSDC", "StorageEnvSDC", "SMSSampleMaint", "LV_Incdt"};
        try {
            PropertyTree maint = PropertyTreeUtil.getPropertyTree(this.database, "MaintenanceForm", false);
            for (int i = 0; i < clonePageTypes.length; ++i) {
                PropertyTree clone = PropertyTreeUtil.getPropertyTree(this.database, clonePageTypes[i], false);
                Node customNode = clone.getNode("Sapphire Custom");
                Node node = maint.getNode(nodes[i]);
                if (node == null) {
                    Trace.log("Not found node " + nodes[i]);
                    Trace.logError("Not found node " + nodes[i]);
                    continue;
                }
                UpdateMaintenanceFormPropertyTree.resolveProperytList(node.getPropertyList(), customNode.getPropertyList());
                NodeList customNodeList = customNode.getNodeList();
                if (customNodeList.size() <= 0) continue;
                for (int n = 0; n < customNodeList.size(); ++n) {
                    Node scNode;
                    int c;
                    NodeList subchildNodes;
                    Node childNode = (Node)customNodeList.get(n);
                    if ("EditPage Product".equals(childNode.getNodeId())) {
                        childNode.renameNode(nodeNames[i] + "EditPage Product");
                        subchildNodes = childNode.getNodeList();
                        for (c = 0; c < subchildNodes.size(); ++c) {
                            scNode = (Node)subchildNodes.get(c);
                            if (!"EditPage Custom".equals(scNode.getNodeId())) continue;
                            scNode.renameNode(nodeNames[i] + "EditPage Custom");
                        }
                    }
                    if ("ViewPage Product".equals(childNode.getNodeId())) {
                        childNode.renameNode(nodeNames[i] + "ViewPage Product");
                        subchildNodes = childNode.getNodeList();
                        for (c = 0; c < subchildNodes.size(); ++c) {
                            scNode = (Node)subchildNodes.get(c);
                            if (!"ViewPage Custom".equals(scNode.getNodeId())) continue;
                            scNode.renameNode(nodeNames[i] + "ViewPage Custom");
                        }
                    }
                    if (!"DetailEditPage Product".equals(childNode.getNodeId())) continue;
                    childNode.renameNode(nodeNames[i] + "DetailEditPage Product");
                    subchildNodes = childNode.getNodeList();
                    for (c = 0; c < subchildNodes.size(); ++c) {
                        scNode = (Node)subchildNodes.get(c);
                        if (!"DetailEditPage Custom".equals(scNode.getNodeId())) continue;
                        scNode.renameNode(nodeNames[i] + "DetailEditPage Custom");
                    }
                }
                node.getNodeList().mergeNodes(customNodeList);
                Trace.log(" Updated node " + nodes[i]);
            }
            try {
                Statement stmt = this.database.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT valuetree FROM propertytree WHERE propertytreeid = 'MaintenanceForm' " + (this.database.isOracle() ? " FOR UPDATE " : ""));
                rs.next();
                Clob clob = rs.getClob("valuetree");
                Writer writeClob = clob.setCharacterStream(1L);
                String s = maint.toXMLString();
                writeClob.write(maint.toXMLString());
                writeClob.close();
                PreparedStatement pstatement = this.database.prepareStatement("UPDATE propertytree SET valuetree = ? where propertytreeid =?");
                pstatement.setClob(1, clob);
                pstatement.setString(2, "MaintenanceForm");
                pstatement.execute();
                pstatement.close();
                stmt.close();
                rs.close();
                this.database.createResultSet("getvaluetree", "select valuetree from propertytree where propertytreeid='MaintenanceForm'");
                ResultSet rs1 = this.database.getResultSet("getvaluetree");
                rs1.next();
                Reader reader = rs1.getClob("valuetree").getCharacterStream();
                StringBuilder builder = new StringBuilder();
                int charsRead = -1;
                char[] chars = new char[100];
                do {
                    if ((charsRead = reader.read(chars, 0, chars.length)) <= 0) continue;
                    builder.append(chars, 0, charsRead);
                } while (charsRead > 0);
                boolean successful = s.equals(builder.toString());
                if (!successful) {
                    throw new SapphireException("Custom node creation under MaintenanceForm PageType is unsuccessful");
                }
                Logger.logTrace(2, "Custom node creation under MaintenanceForm PageType successful.");
            }
            catch (IOException ioe) {
                throw new SapphireException("updateOracleClob. Exception: " + ErrorUtil.extractMessageFromException(ioe, true), ioe);
            }
            catch (SQLException sq) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(sq, true));
            }
        }
        catch (SapphireException e) {
            Trace.logError("[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage());
            return "[" + this.getClass().getName() + "] Diagnostic error message: " + e.getMessage();
        }
        return "";
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    public static PropertyList resolveProperytList(PropertyList maintPropertyList, PropertyList clonePropertyList) {
        Set s = clonePropertyList.keySet();
        for (String propertyid : s) {
            Object value = clonePropertyList.get(propertyid);
            if (value == null) continue;
            if (value instanceof String) {
                if (((String)value).equals(maintPropertyList.getProperty(propertyid))) continue;
                maintPropertyList.setProperty(propertyid, (String)value);
                continue;
            }
            if (value instanceof PropertyList) {
                PropertyList maintSubPL = maintPropertyList.getPropertyList(propertyid);
                if (maintSubPL == null) {
                    maintPropertyList.setProperty(propertyid, (PropertyList)value);
                    continue;
                }
                Set p = ((PropertyList)value).keySet();
                for (String propid : p) {
                    Object val = ((PropertyList)value).get(propid);
                    if (((String)val).equals(maintSubPL.getProperty(propid))) continue;
                    maintSubPL.setProperty(propid, (String)val);
                }
                continue;
            }
            if (!(value instanceof PropertyListCollection)) continue;
            PropertyListCollection collection = maintPropertyList.getCollection(propertyid);
            if (collection != null && collection.size() > 0) {
                if ("elements".equalsIgnoreCase(propertyid)) {
                    for (PropertyList cloneSubPropertyList : (PropertyListCollection)value) {
                        String elementId = cloneSubPropertyList.getProperty("elementid");
                        String elementType = cloneSubPropertyList.getProperty("elementtype");
                        boolean found = false;
                        for (PropertyList maintSubPL : collection) {
                            String maintElemId = maintSubPL.getProperty("elementid");
                            String maintElemType = maintSubPL.getProperty("elementtype");
                            if (!elementId.equalsIgnoreCase(maintElemId) || !elementType.equalsIgnoreCase(maintElemType)) continue;
                            found = true;
                            Set p = cloneSubPropertyList.keySet();
                            for (String propid : p) {
                                String val = cloneSubPropertyList.getProperty(propid);
                                if (val.equals(maintSubPL.getProperty(propid))) continue;
                                maintSubPL.setProperty(propid, val);
                            }
                        }
                        if (found) continue;
                        collection.add(cloneSubPropertyList);
                    }
                    continue;
                }
                if ("includes".equalsIgnoreCase(propertyid)) {
                    for (PropertyList cloneSubPropertyList : (PropertyListCollection)value) {
                        String url = cloneSubPropertyList.getProperty("url");
                        if (url.length() == 0) continue;
                        boolean found = false;
                        for (PropertyList maintSubPL : collection) {
                            String maintUrl = maintSubPL.getProperty("url");
                            if (!url.equalsIgnoreCase(maintUrl)) continue;
                            found = true;
                            break;
                        }
                        if (found) continue;
                        collection.add(cloneSubPropertyList);
                    }
                    continue;
                }
                if (!"actions".equalsIgnoreCase(propertyid)) continue;
                for (PropertyList cloneSubPropertyList : (PropertyListCollection)value) {
                    String actionId = cloneSubPropertyList.getProperty("actionid");
                    if (actionId.length() == 0) continue;
                    boolean found = false;
                    for (PropertyList maintSubPL : collection) {
                        String maintActionId = maintSubPL.getProperty("actionid");
                        if (!actionId.equalsIgnoreCase(maintActionId)) continue;
                        found = true;
                        Set p = cloneSubPropertyList.keySet();
                        for (String propid : p) {
                            String val = cloneSubPropertyList.getProperty(propid);
                            if (val.equals(maintSubPL.getProperty(propid))) continue;
                            maintSubPL.setProperty(propid, val);
                        }
                    }
                    if (found) continue;
                    collection.add(cloneSubPropertyList);
                }
                continue;
            }
            maintPropertyList.setProperty(propertyid, (PropertyListCollection)value);
        }
        return maintPropertyList;
    }
}

