/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.workflow.StepUtil;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public class PropertyTreeUtil {
    public static HashMap propertyTreeLoadMap;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getPropertyTreeType(DBAccess database, String propertytreeid) throws SapphireException {
        String type;
        block4: {
            String RESULTSET = database.newName();
            type = null;
            try {
                database.createPreparedResultSet(RESULTSET, "SELECT propertytreetype FROM propertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
                if (database.getNext(RESULTSET)) {
                    type = database.getString(RESULTSET, "propertytreetype");
                    break block4;
                }
                throw new SapphireException("Property tree " + propertytreeid + " not found");
            }
            finally {
                database.closeResultSet(RESULTSET);
            }
        }
        return type != null && type.length() > 0 ? type : "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getPropertyTreeValue(DBAccess database, String propertytreeid, boolean defaultEmptyValue) throws SapphireException {
        String value;
        block4: {
            String RESULTSET = database.newName();
            value = null;
            try {
                database.createPreparedResultSet(RESULTSET, "SELECT valuetree FROM propertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
                if (database.getNext(RESULTSET)) {
                    Trace.logDebug("Retrieving valuetree information for tree: " + propertytreeid);
                    value = database.getClob(RESULTSET, "valuetree");
                    break block4;
                }
                throw new SapphireException("Property tree " + propertytreeid + " not found");
            }
            finally {
                database.closeResultSet(RESULTSET);
            }
        }
        return value != null && value.length() > 0 ? value : (defaultEmptyValue ? "<propertytree><nodelist/></propertytree>" : null);
    }

    public static PropertyTree getPropertyTree(DBAccess database, String propertytreeid) throws SapphireException {
        return PropertyTreeUtil.getPropertyTree(database, propertytreeid, true);
    }

    @Deprecated
    public static PropertyTree getPropertyTree(DBAccess database, String propertytreeid, boolean includeDefinition) throws SapphireException {
        PropertyTree propertyTree = new PropertyTree(propertytreeid);
        String xml = PropertyTreeUtil.getPropertyTreeValue(database, propertytreeid, true);
        propertyTree.setValueXML(xml);
        if (includeDefinition) {
            String definition = PropertyTreeUtil.getPropertyTreeDefinition(database, propertytreeid, true);
            propertyTree.setDefinitionXML(definition);
        }
        return propertyTree;
    }

    public static void setPropertyTreeValue(DBAccess database, String sysuserid, String propertytreeid, String propertytreevalue) throws SapphireException {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        try {
            String sql = "update propertytree set valuetree = ?, modby = ?,  moddt = {ts '" + now.toString() + "'} WHERE  propertytreeid=?";
            PreparedStatement ps = null;
            ps = database.getConnection().prepareStatement(sql);
            ps.setCharacterStream(1, (Reader)new StringReader(propertytreevalue), propertytreevalue.length());
            ps.setString(2, sysuserid);
            ps.setString(3, propertytreeid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception sqle) {
            throw new SapphireException(sqle.getMessage(), sqle);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean propertyTreeExists(DBAccess database, String propertytreeid) throws SapphireException {
        boolean exists;
        String RESULTSET = database.newName();
        try {
            database.createPreparedResultSet(RESULTSET, "SELECT propertytreeid FROM propertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
            exists = database.getNext(RESULTSET);
        }
        finally {
            database.closeResultSet(RESULTSET);
        }
        return exists;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean webpagePropertyTreeExists(DBAccess database, String propertytreeid, String webpageid, String elementid) throws SapphireException {
        boolean exists;
        String RESULTSET = database.newName();
        try {
            database.createPreparedResultSet(RESULTSET, "SELECT productvaluetree FROM webpagepropertytree WHERE webpageid=? AND propertytreeid=? AND elementid=?", new String[]{webpageid, propertytreeid, elementid});
            exists = database.getNext(RESULTSET);
        }
        finally {
            database.closeResultSet(RESULTSET);
        }
        return exists;
    }

    public static String getPropertyTreeValue(File file, String propertytreeid, boolean defaultEmptyValue) throws SapphireException {
        String value = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] input = new byte[new Long(file.length()).intValue()];
            fis.read(input);
            Trace.logDebug("Retrieving valuetree information for: " + propertytreeid);
            value = new String(input);
            fis.close();
        }
        catch (IOException se) {
            throw new SapphireException("File IO error reading file " + file.getAbsolutePath() + " for property tree " + propertytreeid, se);
        }
        return value != null && value.length() > 0 ? value : (defaultEmptyValue ? "<propertytree><propertydeflist/></propertytree>" : null);
    }

    public static PropertyTree getPropertyTree(File file, String propertytreeid) throws SapphireException {
        PropertyTree propertyTree = new PropertyTree(propertytreeid);
        String xml = PropertyTreeUtil.getPropertyTreeValue(file, propertytreeid, true);
        propertyTree.setValueXML(xml);
        return propertyTree;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getPropertyTreeDefinition(DBAccess database, String propertytreeid, boolean defaultEmptyDefinition) throws SapphireException {
        String definitiontree;
        block6: {
            String RESULTSET = database.newName();
            try {
                definitiontree = null;
                database.createPreparedResultSet(RESULTSET, "SELECT propertytreetype, objectname, definitiontree FROM propertytree WHERE propertytreeid = ?", new Object[]{propertytreeid});
                if (database.getNext(RESULTSET)) {
                    if (Trace.on) {
                        Trace.log("Retrieving definitiontree information for: " + propertytreeid);
                    }
                    definitiontree = database.getClob(RESULTSET, "definitiontree");
                    if ("Step".equals(database.getValue(RESULTSET, "propertytreetype"))) {
                        definitiontree = StepUtil.prependDefinitions(definitiontree, database.getValue(RESULTSET, "objectname"));
                    }
                    break block6;
                }
                throw new SapphireException("Property tree " + propertytreeid + " not found");
            }
            finally {
                database.closeResultSet(RESULTSET);
            }
        }
        return definitiontree != null && definitiontree.length() > 0 ? definitiontree : (defaultEmptyDefinition ? "<propertytree><propertydeflist/></propertytree>" : null);
    }

    public static PropertyDefinitionList getPropertyTreeDefinitionList(DBAccess database, String propertytreeid) throws SapphireException {
        String xml = PropertyTreeUtil.getPropertyTreeDefinition(database, propertytreeid, true);
        PropertyTree tree = new PropertyTree();
        tree.setDefinitionXML(xml);
        return tree.getPropertyDefinitionList();
    }

    public static String getPropertyTreeDefinition(File file, String propertytreeid, boolean defaultEmptyDefinition) throws SapphireException {
        String definition = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] input = new byte[new Long(file.length()).intValue()];
            fis.read(input);
            if (Trace.on) {
                Trace.log("Retrieving definitiontree information for: " + propertytreeid);
            }
            definition = new String(input);
            fis.close();
        }
        catch (IOException se) {
            throw new SapphireException("File IO error reading file " + file.getAbsolutePath() + " for property tree " + propertytreeid + ": " + se.getMessage(), se);
        }
        return definition != null && definition.length() > 0 ? definition : (defaultEmptyDefinition ? "<propertytree><propertydeflist/></propertytree>" : null);
    }

    public static void setPropertyTreeDefinition(DBAccess database, String sysuserid, String propertytreeid, String propertyDefList) throws SapphireException {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        try {
            String sql = "update propertytree set definitiontree = ?, modby = ?,  moddt = {ts '" + now.toString() + "'} WHERE  propertytreeid=?";
            PreparedStatement ps = null;
            ps = database.getConnection().prepareStatement(sql);
            ps.setCharacterStream(1, (Reader)new StringReader(propertyDefList), propertyDefList.length());
            ps.setString(2, sysuserid);
            ps.setString(3, propertytreeid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception sqle) {
            throw new SapphireException(sqle.getMessage(), sqle);
        }
    }

    public static void setPropertyTreeDefinition(File file, String propertytreeid, String definition) throws SapphireException {
        if (definition == null || definition.length() == 0) {
            definition = "<propertytree><propertydeflist/></propertytree>";
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(definition.getBytes());
            if (Trace.on) {
                Trace.log("Saving definitiontree information for: " + propertytreeid);
            }
            fos.close();
        }
        catch (IOException se) {
            throw new SapphireException("File IO error writing file " + file.getAbsolutePath() + " for property tree " + propertytreeid + ": " + se.getMessage(), se);
        }
    }

    public static String getWebPagePropertyTreeValue(File file, String propertytreeid, boolean defaultEmptyValue) throws SapphireException {
        String value = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] input = new byte[new Long(file.length()).intValue()];
            fis.read(input);
            Trace.logDebug("Retrieving valuetree information for: " + propertytreeid);
            value = new String(input);
            fis.close();
        }
        catch (IOException se) {
            throw new SapphireException("File IO error reading file " + file.getAbsolutePath() + " for property tree " + propertytreeid, se);
        }
        return value != null && value.length() > 0 ? value : (defaultEmptyValue ? "<propertylist />" : null);
    }

    public static String getWebPagePropertyTreeValue(DBAccess database, String webpageid, String propertytreeid, String elementid, boolean defaultEmptyValue) throws SapphireException {
        return PropertyTreeUtil.getWebPagePropertyTreeValue(database, webpageid, propertytreeid, elementid, null, defaultEmptyValue);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getWebPagePropertyTreeValue(DBAccess database, String webpageid, String propertytreeid, String elementid, String productedition, boolean defaultEmptyValue) throws SapphireException {
        String value;
        block6: {
            String RESULTSET = database.newName();
            value = null;
            try {
                if (productedition != null && productedition.trim().length() > 0) {
                    database.createPreparedResultSet(RESULTSET, "SELECT productvaluetree FROM webpagepropertytree WHERE webpageid=? AND propertytreeid=? AND elementid=? AND productedition=?", new String[]{webpageid, propertytreeid, elementid, productedition});
                } else {
                    database.createPreparedResultSet(RESULTSET, "SELECT productvaluetree FROM webpagepropertytree WHERE webpageid=? AND propertytreeid=? AND elementid=?", new String[]{webpageid, propertytreeid, elementid});
                }
                if (database.getNext(RESULTSET)) {
                    Trace.logDebug("Retrieving valuetree information for :" + webpageid + ", " + propertytreeid + ", " + elementid);
                    value = database.getClob(RESULTSET, "productvaluetree");
                    break block6;
                }
                throw new SapphireException("Valuetree for " + webpageid + ", " + propertytreeid + ", " + elementid + " not found");
            }
            finally {
                database.closeResultSet(RESULTSET);
            }
        }
        return value != null && value.length() > 0 ? value : (defaultEmptyValue ? "<propertylist />" : null);
    }

    public static PropertyList getWebPagePropertyTreePropertyList(File file, String propertytreeid) throws SapphireException {
        PropertyList propertyList = new PropertyList();
        String xml = PropertyTreeUtil.getWebPagePropertyTreeValue(file, propertytreeid, true);
        propertyList.setPropertyList(xml);
        return propertyList;
    }

    public static PropertyList getWebPagePropertyTreePropertyList(DBAccess database, String webpageid, String propertytreeid, String elementid) throws SapphireException {
        return PropertyTreeUtil.getWebPagePropertyTreePropertyList(database, webpageid, propertytreeid, elementid, null);
    }

    public static PropertyList getWebPagePropertyTreePropertyList(DBAccess database, String webpageid, String propertytreeid, String elementid, String productedition) throws SapphireException {
        PropertyList propertyList = new PropertyList();
        String xml = PropertyTreeUtil.getWebPagePropertyTreeValue(database, webpageid, propertytreeid, elementid, productedition, true);
        propertyList.setPropertyList(xml);
        return propertyList;
    }

    public static void setWebPagePropertyTreeValue(DBAccess database, String webpageid, String propertytreeid, String elementid, String propertylistvalue) throws SapphireException {
        PropertyTreeUtil.setWebPagePropertyTreeValue(database, webpageid, propertytreeid, elementid, null, propertylistvalue);
    }

    public static void setWebPagePropertyTreeValue(DBAccess database, String webpageid, String propertytreeid, String elementid, String productedition, String propertylistvalue) throws SapphireException {
        try {
            boolean includeedition = productedition != null && productedition.length() > 0;
            String sql = "update webpagepropertytree set productvaluetree = ? WHERE webpageid=? AND propertytreeid=? AND elementid=?";
            if (includeedition) {
                sql = sql + " AND productedition = '" + productedition + "'";
            }
            PreparedStatement ps = null;
            ps = database.getConnection().prepareStatement(sql);
            ps.setCharacterStream(1, (Reader)new StringReader(propertylistvalue), propertylistvalue.length());
            ps.setString(2, webpageid);
            ps.setString(3, propertytreeid);
            ps.setString(4, elementid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception sqle) {
            throw new SapphireException(sqle.getMessage(), sqle);
        }
    }

    public static void importParentNodes(DBAccess database, String propertyTreeId, PropertyTree propertyTree, File dir, Logger logger) throws SapphireException {
        if (propertyTreeLoadMap == null) {
            propertyTreeLoadMap = new HashMap();
        }
        PropertyTree loadedPropertyTree = new PropertyTree();
        loadedPropertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(database, propertyTreeId, true));
        NodeList nodelist = propertyTree.getNodeList();
        for (int i = 0; i < nodelist.size(); ++i) {
            Node parentNode;
            Node node = (Node)nodelist.get(i);
            String extendsnodeid = node.getExtendsNodeId();
            if (extendsnodeid == null || extendsnodeid.length() <= 0 || extendsnodeid.equals("root") || (parentNode = loadedPropertyTree.getNode(extendsnodeid)) != null) continue;
            HashSet<String> nodeLoadSet = (HashSet<String>)propertyTreeLoadMap.get(propertyTreeId);
            if (nodeLoadSet == null) {
                nodeLoadSet = new HashSet<String>();
                propertyTreeLoadMap.put(propertyTreeId, nodeLoadSet);
            }
            if (nodeLoadSet.contains(extendsnodeid)) continue;
            File file = new File(dir, extendsnodeid + ".xml");
            ImportXML importXML = new ImportXML(database, file);
            importXML.setImportLog(logger);
            importXML.importFiles();
            loadedPropertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(database, propertyTreeId, true));
            nodeLoadSet.add(extendsnodeid);
        }
    }
}

