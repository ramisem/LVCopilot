/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.platform.Configuration;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class WebPage
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet webPageHelpPage;
        String webpagetypeflag = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "S" : "U";
        String productedition = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "R5" : "U";
        DataSet primary = sdiData.getDataset("primary");
        primary.setString(-1, "webpagetypeflag", webpagetypeflag);
        if (!primary.getValue(0, "productedition").equals("Stellar")) {
            primary.setString(-1, "productedition", productedition);
        }
        if ((webPageHelpPage = sdiData.getDataset("webpagehelppage")) != null) {
            webPageHelpPage.setString(-1, "productedition", productedition);
        }
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "expresspage").length() <= 0) continue;
            String webpageid = primary.getString(i, "webpageid");
            String edition = primary.getString(i, "productedition");
            String hash = EncryptDecrypt.encodePageid(webpageid + edition);
            primary.setString(i, "expresspage", hash);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        ConfigurationProcessor cp = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
        boolean isDevMode = "Y".equals(cp.getSysConfigProperty("devmode"));
        String compCode = isDevMode ? "" : cp.getSysConfigProperty("compcode");
        String fromwebpageid = actionProps.getProperty("templateid");
        String fromeditionid = actionProps.getProperty("templatekeyid2");
        if (fromwebpageid.length() == 0) {
            fromwebpageid = actionProps.getProperty("templatekeyid1");
        }
        if (fromwebpageid.length() > 0) {
            try {
                DataSet primary = sdiData.getDataset("primary");
                String sql = "INSERT INTO webpagepropertytree( webpageid, productedition, propertytreeid, elementid, valuetree, productvaluetree, extendnodeid, usersequence ) SELECT ?, ?, propertytreeid, elementid, valuetree, productvaluetree, extendnodeid, usersequence + 1 FROM webpagepropertytree WHERE webpageid=? AND productedition=?";
                PreparedStatement ps = this.database.prepareStatement("copywebpagepropertytree", sql);
                ps.setString(3, fromwebpageid);
                ps.setString(4, fromeditionid);
                for (int i = 0; i < primary.size(); ++i) {
                    String webpageid = primary.getString(i, "webpageid");
                    String productedition = primary.getString(i, "productedition");
                    ps.setString(1, webpageid);
                    ps.setString(2, productedition);
                    ps.executeUpdate();
                    if (isDevMode || compCode.length() != 0) continue;
                    this.database.createPreparedResultSet("SELECT propertytreeid, elementid, valuetree, productvaluetree FROM webpagepropertytree WHERE webpageid = ? AND productedition = ?", new Object[]{webpageid, productedition});
                    while (this.database.getNext()) {
                        String productValueTree = this.database.getClob("productvaluetree");
                        if (productValueTree == null || productValueTree.length() <= 0) continue;
                        PropertyList pl = new PropertyList();
                        pl.setPropertyList(productValueTree);
                        String valueTree = this.database.getClob("valuetree");
                        if (valueTree != null && valueTree.length() > 0) {
                            pl.setPropertyList(valueTree, true);
                        }
                        String propertytreeid = this.database.getString("propertytreeid");
                        String elementid = this.database.getString("elementid");
                        ((DBUtil)this.database).updateClob("webpagepropertytree", "valuetree", pl.toXMLString(), new String[]{"webpageid", "productedition", "propertytreeid", "elementid"}, new String[]{webpageid, productedition, propertytreeid, elementid});
                        this.database.executePreparedUpdate("UPDATE webpagepropertytree SET productvaluetree = null where webpageid=? and productedition=? and propertytreeid=? and elementid=?", new String[]{webpageid, productedition, propertytreeid, elementid});
                    }
                }
                ps.close();
            }
            catch (SQLException e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String webpageid;
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.database.createPreparedResultSet("SELECT webpageid, productedition, webpagetypeflag FROM webpage, rsetitems WHERE webpage.webpageid = rsetitems.keyid1 AND webpage.productedition = rsetitems.keyid2 AND rsetid = ?", new Object[]{rsetid});
            while (this.database.getNext()) {
                webpageid = this.database.getString("webpageid");
                String webpagetype = this.database.getString("webpagetypeflag");
                if (!"S".equals(webpagetype) && !"C".equals(webpagetype)) continue;
                throw new SapphireException("'" + webpageid + "' is a protected " + (webpagetype.equals("S") ? "System" : "Core") + " page and cannot be deleted.");
            }
        }
        this.database.createPreparedResultSet("SELECT webpageid, productedition FROM webpage, rsetitems WHERE webpage.extendwebpageid = rsetitems.keyid1 AND webpage.extendproductedition = rsetitems.keyid2 AND rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            webpageid = this.database.getString("webpageid");
            String productedition = this.database.getString("productedition");
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "WebPage");
            props.put("keyid1", webpageid);
            props.put("keyid2", productedition);
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }

    @Override
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        DataSet webPagePropertyTree;
        if (!isAddSDI && (webPagePropertyTree = sdiData.getDataset("webpagepropertytree")) != null && webPagePropertyTree.getRowCount() > 0) {
            DataSet primary = sdiData.getDataset("primary");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM webpagepropertytree WHERE (webpageid = '" + primary.getString(0, "webpageid") + "' AND productedition = '" + primary.getString(0, "productedition") + "')");
            for (int i = 1; i < primary.getRowCount(); ++i) {
                sql.append(" OR (webpageid = '" + primary.getString(0, "webpageid") + "' AND productedition = '" + primary.getString(0, "productedition") + "')");
            }
            DataSet oldWebPagePropertyTree = this.getQueryProcessor().getSqlDataSet(sql.toString(), true);
            if (oldWebPagePropertyTree == null) {
                throw new SapphireException("Exception occurred while retrieving existing webpagepropertytree info.");
            }
            HashMap<String, String> findMap = new HashMap<String, String>();
            boolean isValTreeColValid = webPagePropertyTree.isValidColumn("valuetree");
            boolean isProdValTreeColValid = webPagePropertyTree.isValidColumn("productvaluetree");
            boolean isCompValTreeColValid = webPagePropertyTree.isValidColumn("componentvaluetree");
            for (int i = 0; i < webPagePropertyTree.getRowCount(); ++i) {
                findMap.clear();
                findMap.put("webpageid", webPagePropertyTree.getString(i, "webpageid"));
                findMap.put("propertytreeid", webPagePropertyTree.getString(i, "propertytreeid"));
                findMap.put("elementid", webPagePropertyTree.getString(i, "elementid"));
                findMap.put("productedition", webPagePropertyTree.getString(i, "productedition"));
                int findRow = oldWebPagePropertyTree.findRow(findMap);
                if (findRow <= -1) continue;
                if (!isValTreeColValid) {
                    webPagePropertyTree.setClob(i, "valuetree", oldWebPagePropertyTree.getClob(findRow, "valuetree", ""));
                }
                if (!isProdValTreeColValid) {
                    webPagePropertyTree.setClob(i, "productvaluetree", oldWebPagePropertyTree.getClob(findRow, "productvaluetree", ""));
                }
                if (isCompValTreeColValid) continue;
                webPagePropertyTree.setClob(i, "componentvaluetree", oldWebPagePropertyTree.getClob(findRow, "componentvaluetree", ""));
            }
        }
    }
}

