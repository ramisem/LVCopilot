/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.database;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ResequencePropertyTree
extends BaseDiagnostic {
    public static HashMap sequenceCache = null;

    public ResequencePropertyTree(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Resequence Collections in Evergreen Properties if upgrading from R4.1";
    }

    @Override
    public String getDescription() {
        return "Resequence Collections in Evergreen Properties if upgrading from R4.1. This may take a few minutes.";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        throw new DiagnosticException(1, "Resequence Collections in Property Tree if upgrading from R4.1");
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        ResultSet rs = null;
        try {
            this.database.createResultSet("Select propertyid, propertyvalue from sysconfig where propertyid='build'");
            rs = this.database.getResultSet();
            String build = "";
            if (rs.next()) {
                build = rs.getString("propertyvalue");
            }
            rs.close();
            if (build == null || build.indexOf("0205.010.") != 0) {
                return "Not a R4.1 build, no resequence needed.";
            }
            HashMap<String, PropertyTree> valueTreeCache = new HashMap<String, PropertyTree>();
            String sql = "SELECT propertytreeid FROM propertytree WHERE propertytreetype in ('Element', 'Layout', 'Page Type')";
            this.database.createResultSet(sql);
            rs = this.database.getResultSet();
            while (rs.next()) {
                String propertytreeid = rs.getString("propertytreeid");
                Trace.log("Resequencing " + propertytreeid + "...\n");
                String propertyTreeXML = PropertyTreeUtil.getPropertyTreeValue(this.database, propertytreeid, true);
                PropertyTree afterTree = ResequencePropertyTree.resequencePropertyTreeXML(propertyTreeXML);
                afterTree.setDefinitionXML(PropertyTreeUtil.getPropertyTreeDefinition(this.database, propertytreeid, true));
                this.database.getConnection().setAutoCommit(false);
                Trace.log("DIAGNOSTICS", "Updating propertytree " + propertytreeid);
                PropertyTreeUtil.setPropertyTreeValue(this.database, this.connectionInfo.getSysuserId(), propertytreeid, afterTree.toXMLString());
                this.database.executeSQL("commit");
                this.database.getConnection().setAutoCommit(true);
                valueTreeCache.put(propertytreeid, afterTree);
                Trace.log("Done Resequencing and Updating " + propertytreeid + ".\n");
            }
            rs.close();
            sql = "SELECT webpageid, propertytreeid, elementid, extendnodeid from webpagepropertytree";
            this.database.createResultSet(sql);
            rs = this.database.getResultSet();
            DataSet ds = new DataSet(rs);
            rs.close();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String webpageid = ds.getString(i, "webpageid");
                String propertytreeid = ds.getString(i, "propertytreeid");
                String elementid = ds.getString(i, "elementid");
                String extendednodeid = ds.getString(i, "extendnodeid");
                String webpagePropertyListXML = PropertyTreeUtil.getWebPagePropertyTreeValue(this.database, webpageid, propertytreeid, elementid, true);
                PropertyTree propertyTree = (PropertyTree)valueTreeCache.get(propertytreeid);
                HashMap sequenceCache = new HashMap();
                try {
                    if (!"__root".equals(extendednodeid)) {
                        Trace.log("Retrieving node propertylist " + extendednodeid);
                        PropertyList nodePropertyList = propertyTree.getNodePropertyList(extendednodeid, true);
                        ResequencePropertyTree.buildSequenceCache(nodePropertyList, sequenceCache);
                    }
                    Trace.log("Resequencing Webpageid:" + webpageid + " PropertyTreeId:" + propertytreeid + " Elementid:" + elementid + " ExtendedNodeid:" + extendednodeid);
                    String afterXML = ResequencePropertyTree.resequencePropertyListXML(webpagePropertyListXML, sequenceCache);
                    this.database.getConnection().setAutoCommit(false);
                    Trace.log("DIAGNOSTICS", "Updating webpagepropertytreevalue Webpageid:" + webpageid + " PropertyTreeId:" + propertytreeid + " Elementid:" + elementid + " ExtendedNodeid:" + extendednodeid);
                    PropertyTreeUtil.setWebPagePropertyTreeValue(this.database, webpageid, propertytreeid, elementid, afterXML);
                    this.database.executeSQL("commit");
                    this.database.getConnection().setAutoCommit(true);
                    Trace.log("Done Resequencing and Updating Webpageid:" + webpageid + " PropertyTreeId:" + propertytreeid + " Elementid:" + elementid + " ExtendedNodeid:" + extendednodeid);
                    continue;
                }
                catch (Exception e) {
                    if (("Node id " + extendednodeid + " not found in propertytree").equals(e.getMessage())) {
                        Trace.log("Node id " + extendednodeid + " not found in propertytree " + propertytreeid + ". Ignored.");
                    }
                    Trace.logError("Error processing Webpageid:" + webpageid + " PropertyTreeId:" + propertytreeid + " Elementid:" + elementid + " ExtendedNodeid:" + extendednodeid, e);
                }
            }
        }
        catch (Exception e) {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            throw new DiagnosticException(e);
        }
        return "Done Resequencing.";
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    private static void fillGap(long[] sequences, int startindex, int endindex) {
        long sequence;
        int i;
        long endnum;
        int points = endindex - startindex - 1;
        long startnum = sequences[startindex];
        if (startnum == 1L) {
            startnum = 0L;
            ++points;
        }
        if ((endnum = sequences[endindex]) == 1L) {
            ++points;
            endnum = startnum + 1000000L;
        }
        long calcincrement = (endnum - startnum) / (long)(points + 1);
        int digit = (calcincrement + "").length();
        long increment = (int)(Math.floor((double)calcincrement / Math.pow(10.0, digit - 1)) * Math.pow(10.0, digit - 1));
        if (startnum == 0L) {
            startnum = increment;
            for (i = 0; i < points; ++i) {
                sequences[startindex + i] = sequence = startnum + increment * (long)i;
            }
        } else if (endnum == 1L) {
            for (i = 1; i <= points; ++i) {
                sequences[startindex + i] = sequence = startnum + increment * (long)i;
            }
        } else {
            for (i = 1; i <= points; ++i) {
                sequences[startindex + i] = sequence = startnum + increment * (long)i;
            }
        }
        for (i = 0; i < endindex - startindex + 1; ++i) {
        }
    }

    public static void reassignSequence(long[] sequences) {
        int currentstart;
        if (sequences[0] != 1L) {
            for (currentstart = 0; currentstart < sequences.length - 1 && sequences[currentstart + 1] != 1L; ++currentstart) {
            }
        }
        for (int currentend = currentstart + 1; currentend < sequences.length; ++currentend) {
            if (currentend == sequences.length - 1 && currentend - currentstart >= 1) {
                ResequencePropertyTree.fillGap(sequences, currentstart, currentend);
                continue;
            }
            if (currentstart == 0 && sequences[currentend] != 1L && currentend - currentstart == 1) {
                ResequencePropertyTree.fillGap(sequences, currentstart, currentend);
                currentstart = currentend;
                continue;
            }
            if (sequences[currentend] == 1L || currentend - currentstart <= 1) continue;
            ResequencePropertyTree.fillGap(sequences, currentstart, currentend);
            currentstart = currentend;
        }
    }

    public static PropertyTree resequencePropertyTreeXML(String propertyTreeXML) throws SapphireException {
        sequenceCache = new HashMap();
        propertyTreeXML = propertyTreeXML.replaceAll("sequence=\"(\\d)*\"", "sequence=\"y\"");
        PropertyTree tree = new PropertyTree();
        tree.setValueXML(propertyTreeXML);
        ArrayList nodes = tree.getAllNodes();
        for (Node node : nodes) {
            PropertyList pl = node.getPropertyList();
            if (pl == null) continue;
            ResequencePropertyTree.resequencePropertyList(pl, sequenceCache);
        }
        return tree;
    }

    public static void buildSequenceCache(PropertyList pl, HashMap sequenceCache) {
        Set s = pl.keySet();
        for (String propertyid : s) {
            if (pl.isPropertyList(propertyid)) {
                PropertyList propertyList = pl.getPropertyList(propertyid);
                sequenceCache.put(propertyList.getId(), "" + propertyList.getSequence());
                ResequencePropertyTree.buildSequenceCache(propertyList, sequenceCache);
                continue;
            }
            if (!pl.isCollection(propertyid)) continue;
            PropertyListCollection c = pl.getCollection(propertyid);
            for (int i = 0; i < c.size(); ++i) {
                sequenceCache.put(c.getPropertyList(i).getId(), "" + c.getPropertyList(i).getSequence());
                ResequencePropertyTree.buildSequenceCache(c.getPropertyList(i), sequenceCache);
            }
        }
    }

    public static void resequencePropertyList(PropertyList pl, HashMap sequenceCache) {
        Set s = pl.keySet();
        for (String propertyid : s) {
            if (pl.isCollection(propertyid)) {
                PropertyListCollection c = pl.getCollection(propertyid);
                ResequencePropertyTree.resequenceCollection(c, sequenceCache);
                continue;
            }
            if (!pl.isPropertyList(propertyid)) continue;
            PropertyList propertyList = pl.getPropertyList(propertyid);
            ResequencePropertyTree.resequencePropertyList(propertyList, sequenceCache);
        }
    }

    public static String resequencePropertyListXML(String propertyListXML, HashMap sequenceCache) throws SapphireException {
        propertyListXML = propertyListXML.replaceAll("sequence=\"(\\d)*\"", "sequence=\"y\"");
        PropertyList propertyList = new PropertyList();
        propertyList.setPropertyList(propertyListXML);
        ResequencePropertyTree.resequencePropertyList(propertyList, sequenceCache);
        return propertyList.toXMLString();
    }

    public static void resequenceCollection(PropertyListCollection collection, HashMap sequenceCache) {
        int i;
        long currentsequence = 1000000L;
        int size = collection.size();
        boolean[] isInherited = new boolean[size];
        long[] sequences = new long[size];
        boolean hasInherited = false;
        for (i = 0; i < collection.size(); ++i) {
            String sequenceString;
            PropertyList currentlist = collection.getPropertyList(i);
            ResequencePropertyTree.resequencePropertyList(currentlist, sequenceCache);
            String currentlistid = currentlist.getId();
            long sequence = currentlist.getSequence();
            if (sequence == -1L) {
                sequenceString = currentlist.getAttribute("sequence");
                long l = sequence = "y".equals(sequenceString) ? 1L : -1L;
            }
            if (sequence == -1L) {
                isInherited[i] = true;
                sequenceString = (String)sequenceCache.get(currentlistid);
                if (sequenceString == null) {
                    sequence = 1L;
                } else {
                    sequence = Long.parseLong(sequenceString);
                    hasInherited = true;
                }
                sequences[i] = sequence;
                continue;
            }
            if (sequence != 1L) continue;
            isInherited[i] = false;
            sequences[i] = 1L;
        }
        if (hasInherited) {
            ResequencePropertyTree.reassignSequence(sequences);
            for (i = 0; i < size; ++i) {
                if (isInherited[i]) continue;
                collection.getPropertyList(i).setSequence(sequences[i]);
                sequenceCache.put(collection.getPropertyList(i).getId(), "" + sequences[i]);
            }
        } else {
            for (i = 0; i < size; ++i) {
                long newSeq = currentsequence + (long)(1000000 * i);
                collection.getPropertyList(i).setSequence(newSeq);
                sequenceCache.put(collection.getPropertyList(i).getId(), "" + newSeq);
            }
        }
    }
}

