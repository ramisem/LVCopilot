/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.genealogyviewer;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.genealogyviewer.Batch;
import com.labvantage.sapphire.pageelements.genealogyviewer.BatchDisplayProperty;
import com.labvantage.sapphire.pageelements.genealogyviewer.GenealogyViewer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenealogyViewerUtil
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 88254 $";
    private Batch rootBatch = null;
    private int imageWidth;
    private int imageHeight;
    private int minXPos = 0;
    private int maxXPos = 0;
    private int rectWidth = 120;
    private int rectHeight = 40;
    private int verticalSpace = 10;
    private int textOffset = 3;
    private int fontSize = 12;
    private boolean drawNextIcon;
    private boolean drawPrevIcon;
    private Batch nextIcon;
    private Batch prevIcon;
    private int parentLevel;
    private int childLevel;
    private String rootBatchId;
    private String userString;
    private int displayValueLength;
    private String batchSequence;
    private int maxParentLevel;
    private int maxChildLevel;
    private String hrefURL;
    private String returnToListUrl;
    private String webAppRoot;
    private int maxParent;
    private int maxChild;
    private String colorCodeColumn = "";
    private String legendTitle = "";
    private String currNodeHighlightFlag = "";
    private String currNodeTextFlag = "";
    private String currNodeText = "";
    private String currNodeColor = "";
    private static final String currNodeMapId = "#CURRENT NODE#";
    private Map<String, Color> modeColor;
    private Map<String, String> legendTextMap;
    private boolean fullView;
    private boolean heterogeneous;
    private String detailpageurl = "";
    private QueryProcessor queryProcessor;
    private DAMProcessor damProcessor;
    private SDCProcessor sdcProcessor;
    private TranslationProcessor translationProcessor;
    private boolean isOra;

    public GenealogyViewerUtil(QueryProcessor queryProcessor, DAMProcessor damProcessor, SDCProcessor sdcProcessor, TranslationProcessor translationProcessor, boolean isOra, String rootBatchId, Map elementLevel, String batchSequence, PropertyList elementProperty, String webAppRoot) {
        this.queryProcessor = queryProcessor;
        this.damProcessor = damProcessor;
        this.sdcProcessor = sdcProcessor;
        this.translationProcessor = translationProcessor;
        this.isOra = isOra;
        this.batchSequence = batchSequence;
        this.rootBatchId = rootBatchId;
        this.hrefURL = elementProperty.getProperty("hrefURL");
        this.webAppRoot = webAppRoot;
        this.returnToListUrl = elementProperty.getProperty("returntolistpage");
        this.heterogeneous = "Y".equals(elementProperty.getProperty("heterogeneous"));
        try {
            this.maxParent = Integer.parseInt(elementProperty.getProperty("maxparentcount"));
            this.maxChild = Integer.parseInt(elementProperty.getProperty("maxchildcount"));
        }
        catch (NumberFormatException e) {
            this.fullView = true;
        }
        this.rectHeight = Integer.parseInt(elementProperty.getProperty("nodeheight"));
        this.rectWidth = Integer.parseInt(elementProperty.getProperty("nodewidth"));
        this.currNodeHighlightFlag = elementProperty.getProperty("currentnodehighlightflag");
        this.currNodeTextFlag = elementProperty.getProperty("currentnodetextflag");
        this.currNodeText = elementProperty.getProperty("currentnodetext");
        this.currNodeColor = elementProperty.getProperty("currentnodecolor");
        this.colorCodeColumn = elementProperty.getProperty("basedoncolumn");
        if (this.colorCodeColumn.trim().length() > 0) {
            this.setColor(elementProperty.getProperty("nodecolor", ""), elementProperty.getProperty("legendtext", ""));
            this.legendTitle = elementProperty.getProperty("legendtitle");
        }
        this.buildTreeFromSQL(rootBatchId, elementLevel, elementProperty);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setColor(String nodeColors, String legendText) {
        if (nodeColors.trim().length() > 0) {
            this.modeColor = new LinkedHashMap<String, Color>();
            this.legendTextMap = new LinkedHashMap<String, String>();
            if (nodeColors.length() > 0) {
                int intValue;
                String[] colorProps = nodeColors.split(";");
                Color aColor = null;
                for (String colorProp : colorProps) {
                    String[] prop = colorProp.split("-");
                    String nodeType = prop[0];
                    String colorCode = prop[1];
                    try {
                        intValue = Integer.parseInt(colorCode, 16);
                        aColor = new Color(intValue);
                    }
                    catch (NumberFormatException e) {
                        aColor = Color.WHITE;
                    }
                    this.modeColor.put(nodeType, aColor);
                }
                if ("Y".equalsIgnoreCase(this.currNodeHighlightFlag)) {
                    try {
                        intValue = Integer.parseInt(this.currNodeColor, 16);
                        aColor = new Color(intValue);
                    }
                    catch (NumberFormatException e) {
                        aColor = Color.WHITE;
                    }
                    finally {
                        this.modeColor.put(currNodeMapId, aColor);
                    }
                }
                this.modeColor.put("", Color.WHITE);
            }
            if (legendText.length() > 0) {
                String[] legendTexts;
                for (String textProp : legendTexts = legendText.split(";")) {
                    String[] prop = textProp.split("-");
                    String nodeType = prop[0];
                    String text = prop[1];
                    this.legendTextMap.put(nodeType, text);
                }
                if ("Y".equalsIgnoreCase(this.currNodeHighlightFlag) && "Y".equalsIgnoreCase(this.currNodeTextFlag)) {
                    this.legendTextMap.put(currNodeMapId, this.currNodeText);
                }
            }
        }
    }

    public Batch getRootBatch() {
        return this.rootBatch;
    }

    public int getParentLevel() {
        return this.parentLevel;
    }

    public void setParentLevel(int parentLevel) {
        this.parentLevel = parentLevel;
    }

    public int getChildLevel() {
        return this.childLevel;
    }

    public void setChildLevel(int childLevel) {
        this.childLevel = childLevel;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        if (this.modeColor != null) {
            return this.imageHeight + this.modeColor.size() * 22 + (this.legendTitle.length() > 0 ? 25 : 0);
        }
        return this.imageHeight;
    }

    public boolean isFullView() {
        return this.fullView;
    }

    private String createSql(PropertyList elementProperty, String keyid1, boolean isOracle) {
        StringBuilder sql = new StringBuilder();
        String table = elementProperty.getProperty("table");
        String childColumn = elementProperty.getProperty("childcolumn");
        String parentColumn = elementProperty.getProperty("parentcolumn");
        if (isOracle) {
            sql.append("select * from ").append(table).append(" start with ");
            sql.append(childColumn).append(" = '").append(keyid1).append("' connect by ").append(childColumn).append(" = prior ").append(parentColumn);
            sql.append(" union ");
            sql.append("select * from ").append(table).append(" start with ");
            sql.append(parentColumn).append(" = '").append(keyid1).append("' connect by ").append(parentColumn).append(" = prior ").append(childColumn);
        } else {
            String msSQL = "WITH Childs \nAS\n(\n    SELECT e.* \n    FROM " + table + " AS e\n    WHERE " + parentColumn + "='" + keyid1 + "'\n    UNION ALL\n    SELECT e.* \n    FROM " + table + " AS e\n    INNER JOIN Childs AS d\n        ON e." + parentColumn + "= d." + childColumn + "\n),\nParents \nAS\n(\n    SELECT e.*\n        \n    FROM " + table + " AS e\n    WHERE " + childColumn + "='" + keyid1 + "'\n    UNION ALL\n    SELECT e.*\n    FROM " + table + " AS e\n    INNER JOIN Parents AS d\n        ON e." + childColumn + "= d." + parentColumn + "\n)\nSELECT *\nFROM Childs\nunion\nSELECT *\nFROM Parents";
            sql.append(msSQL);
        }
        return sql.toString();
    }

    private void buildTreeFromSQL(String rootBatchId, Map elementLevel, PropertyList elementProperty) {
        int childLevel;
        int parentLevel;
        PropertyListCollection additionalsdc;
        PropertyList pl;
        HashMap<String, BatchDisplayProperty> displayPropertyMap = new HashMap<String, BatchDisplayProperty>();
        BatchDisplayProperty batchDisplayProperty = new BatchDisplayProperty(elementProperty.getProperty("parentsdc"), elementProperty.getProperty("displayvalue"), elementProperty.getProperty("basedoncolumn"));
        batchDisplayProperty.setLinkurl(this.hrefURL);
        batchDisplayProperty.setDetailpageurl("");
        String nodeColors = elementProperty.getProperty("nodecolor");
        if (nodeColors.length() > 0) {
            String[] colors;
            for (String color : colors = StringUtil.split(nodeColors, ";")) {
                String[] s = StringUtil.split(color, "-");
                String value = s[0];
                String valuecolor = s[1];
                batchDisplayProperty.setColorCodeValue(value, valuecolor);
            }
        }
        displayPropertyMap.put(elementProperty.getProperty("parentsdc"), batchDisplayProperty);
        boolean fullView = "Y".equals(elementProperty.getProperty("fullview"));
        String parentSDCColumn = elementProperty.getProperty("parentsdccolumn");
        String childSDCColumn = elementProperty.getProperty("childsdccolumn");
        if (this.heterogeneous && (pl = (additionalsdc = elementProperty.getCollectionNotNull("additionalsdc")).getPropertyList(0)) != null && pl.size() > 0) {
            for (Object key : pl.keySet()) {
                PropertyList p = (PropertyList)pl.get(key);
                String sdcid = p.getProperty("sdcid");
                String displayvalue = p.getProperty("displayvalue");
                if (OpalUtil.isNotEmpty(sdcid) && OpalUtil.isNotEmpty(displayvalue) && !displayPropertyMap.containsKey(sdcid)) {
                    PropertyList colorCodeProps = p.getPropertyListNotNull("colorcodeprops");
                    batchDisplayProperty = new BatchDisplayProperty(sdcid, displayvalue, colorCodeProps.getProperty("basedoncolumn"));
                    PropertyListCollection collection = colorCodeProps.getCollectionNotNull("nodecolor");
                    for (int c = 0; c < collection.size(); ++c) {
                        PropertyList colorProps = collection.getPropertyList(c);
                        batchDisplayProperty.setColorCodeValue(colorProps.getProperty("nodetype"), colorProps.getProperty("color"));
                    }
                    batchDisplayProperty.setDetailpageurl(p.getProperty("detailpageurl"));
                }
                displayPropertyMap.put(sdcid, batchDisplayProperty);
            }
        }
        try {
            parentLevel = Integer.parseInt((String)elementLevel.get(GenealogyViewer.PARENT_LEVEL));
            childLevel = Integer.parseInt((String)elementLevel.get(GenealogyViewer.CHILD_LEVEL));
        }
        catch (NumberFormatException e) {
            parentLevel = 2;
            childLevel = 2;
        }
        this.parentLevel = parentLevel;
        this.childLevel = childLevel;
        HashMap<String, Batch> batchMap = new HashMap<String, Batch>();
        String sql = this.createSql(elementProperty, rootBatchId, this.isOra);
        DataSet genealogyDataset = this.queryProcessor.getSqlDataSet(sql);
        String childColumn = elementProperty.getProperty("childcolumn");
        String parentColumn = elementProperty.getProperty("parentcolumn");
        String parentSDC = elementProperty.getProperty("parentsdc");
        this.colorCodeColumn = elementProperty.getProperty("basedoncolumn");
        if (genealogyDataset.size() > 0) {
            for (int i = 0; i < genealogyDataset.size(); ++i) {
                String childBatchId;
                Batch childBatch;
                String parentBatchId = genealogyDataset.getValue(i, parentColumn, "").trim();
                Batch parentBatch = null;
                if (parentBatchId.length() > 0 && (parentBatch = (Batch)batchMap.get(parentBatchId)) == null) {
                    String parentSDCID;
                    String parentsdcid = parentSDC;
                    if (this.heterogeneous && parentSDCColumn.length() > 0 && (parentSDCID = genealogyDataset.getString(i, parentSDCColumn, "")).length() > 0) {
                        parentsdcid = parentSDCID;
                    }
                    parentBatch = new Batch(parentsdcid, parentBatchId);
                    parentBatch.setBatchDisplayProperty((BatchDisplayProperty)displayPropertyMap.get(parentsdcid));
                }
                if ((childBatch = (Batch)batchMap.get(childBatchId = genealogyDataset.getValue(i, childColumn, "").trim())) == null) {
                    String childSDCID;
                    String childsdcid = parentSDC;
                    if (this.heterogeneous && childSDCColumn.length() > 0 && (childSDCID = genealogyDataset.getString(i, childSDCColumn, "")).length() > 0) {
                        childsdcid = childSDCID;
                    }
                    childBatch = new Batch(childsdcid, childBatchId);
                    childBatch.setBatchDisplayProperty((BatchDisplayProperty)displayPropertyMap.get(childsdcid));
                }
                if (parentBatch != null) {
                    if (!fullView) {
                        if (childBatch.getParents().size() < this.maxParent) {
                            childBatch.addParent(parentBatch);
                            batchMap.put(childBatchId, childBatch);
                        } else {
                            childBatch.addParent(this.createDummyNode());
                        }
                        if (parentBatch.getChildren().size() < this.maxChild) {
                            parentBatch.addChild(childBatch);
                            batchMap.put(parentBatchId, parentBatch);
                            continue;
                        }
                        parentBatch.addChild(this.createDummyNode());
                        continue;
                    }
                    childBatch.addParent(parentBatch);
                    parentBatch.addChild(childBatch);
                    batchMap.put(childBatchId, childBatch);
                    batchMap.put(parentBatchId, parentBatch);
                    continue;
                }
                batchMap.put(childBatchId, childBatch);
            }
            this.populateBatchDisplayProperties(batchMap);
            this.rootBatch = (Batch)batchMap.get(rootBatchId);
            if ("Y".equals(this.currNodeHighlightFlag)) {
                try {
                    int intValue = Integer.parseInt(this.currNodeColor, 16);
                    this.rootBatch.setBatchBackgroundColor(new Color(intValue));
                }
                catch (NumberFormatException e) {
                    int intValue = Integer.parseInt("#FFFFFF", 16);
                    this.rootBatch.setBatchBackgroundColor(new Color(intValue));
                }
            }
            if (!fullView) {
                this.setBatchParentLevels(this.rootBatch, 1);
                this.setBatchChildLevels(this.rootBatch, 1);
                this.truncateParentLevels(this.rootBatch);
                this.truncateChildLevels(this.rootBatch);
            }
        } else {
            this.rootBatch = new Batch(parentSDC, rootBatchId);
            this.maxParentLevel = 0;
            this.maxChildLevel = 0;
            this.rootBatch.setY(35);
        }
        this.detailpageurl = this.rootBatch.getDetailPageURL();
    }

    private void truncateParentLevels(Batch batch) {
        List<Batch> parents = batch.getParents();
        for (Batch parent : parents) {
            if (parent.getParentLevel() == this.parentLevel) {
                parent.getParents().clear();
                continue;
            }
            this.truncateParentLevels(parent);
        }
    }

    private void truncateChildLevels(Batch batch) {
        List<Batch> childs = batch.getChildren();
        for (Batch child : childs) {
            if (child.getChildLevel() == this.childLevel) {
                child.getChildren().clear();
                continue;
            }
            this.truncateChildLevels(child);
        }
    }

    private void setBatchParentLevels(Batch batch, int level) {
        List<Batch> parents = batch.getParents();
        for (Batch parent : parents) {
            parent.setParentLevel(level);
            this.setBatchParentLevels(parent, level + 1);
        }
    }

    private void setBatchChildLevels(Batch batch, int level) {
        List<Batch> childs = batch.getChildren();
        for (Batch child : childs) {
            child.setChildLevel(level);
            this.setBatchChildLevels(child, level + 1);
        }
    }

    /*
     * WARNING - void declaration
     */
    private void populateBatchDisplayProperties(Map<String, Batch> batchMap) {
        for (String batchKeyId : batchMap.keySet()) {
            Batch batch = batchMap.get(batchKeyId);
            String sdcid = batch.getSdcid();
            String keyid = batch.getKeyId();
            String tableid = this.sdcProcessor.getProperty(sdcid, "tableid");
            String keycolid1 = this.sdcProcessor.getProperty(sdcid, "keycolid1");
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet("select * from " + tableid + " where " + keycolid1 + " = ?", (Object[])new String[]{keyid});
            if (ds != null && ds.size() > 0) {
                String[] tokens;
                BatchDisplayProperty batchDisplayProperty = batch.getBatchDisplayProperty();
                String displayExpression = batchDisplayProperty.getDisplayExpression();
                for (String string : tokens = StringUtil.getTokens(displayExpression)) {
                    DataSet tids;
                    String sql;
                    String columnid;
                    String value = "";
                    if (string.toLowerCase().startsWith("trackitem.")) {
                        columnid = string.substring(10);
                        sql = "select " + columnid + " from trackitem where linksdcid = ? and linkkeyid1 = ?";
                        tids = this.queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid, keyid});
                        if (tids != null && tids.size() > 0) {
                            value = tids.getValue(0, columnid, "");
                        }
                    } else if (string.toLowerCase().startsWith("storageunit.")) {
                        columnid = string.substring(12);
                        sql = "select " + columnid + " from storageunit where linksdcid = ? and linkkeyid1 = ?";
                        tids = this.queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid, keyid});
                        if (tids != null && tids.size() > 0) {
                            value = tids.getValue(0, columnid, "");
                        }
                    } else if (string.startsWith("alias")) {
                        String aliasType = null;
                        aliasType = string.length() > 6 ? string.substring(6) : "";
                        SafeSQL safeSQL = new SafeSQL();
                        StringBuilder sql2 = new StringBuilder("select aliasid from sdialias where sdcid = ").append(safeSQL.addVar(sdcid)).append(" and keyid1 = ").append(safeSQL.addVar(keyid));
                        if (aliasType.length() > 0) {
                            sql2.append(" and aliastype = ").append(safeSQL.addVar(aliasType));
                        }
                        DataSet aliases = this.queryProcessor.getPreparedSqlDataSet(sql2.toString(), safeSQL.getValues());
                        StringBuilder alias = new StringBuilder("");
                        if (aliases != null && aliases.size() > 0) {
                            for (int i = 0; i < aliases.getRowCount(); ++i) {
                                if (i == 0) {
                                    alias.append(aliases.getString(i, "aliasid", ""));
                                    continue;
                                }
                                alias.append(", ").append(aliases.getString(i, "aliasid", ""));
                            }
                        }
                        value = alias.toString();
                    } else {
                        value = ds.getValue(0, string.toLowerCase(), "");
                    }
                    displayExpression = StringUtil.replaceAll(displayExpression, "[" + string + "]", value);
                }
                displayExpression = StringUtil.replaceAll(displayExpression, "\n", "\\n");
                String[] strDisplayLines = StringUtil.split(displayExpression, "\\n");
                StringBuilder modifiedLine = new StringBuilder();
                int maxTextPerLine = this.rectWidth / 6;
                for (String strDisplayLine : strDisplayLines) {
                    while (strDisplayLine.length() > maxTextPerLine) {
                        modifiedLine.append(strDisplayLine.substring(0, maxTextPerLine));
                        modifiedLine.append("\n");
                        strDisplayLine = strDisplayLine.substring(maxTextPerLine);
                    }
                    modifiedLine.append(strDisplayLine).append("\n");
                }
                batch.setDisplayValue(modifiedLine.substring(0, modifiedLine.length() - 1));
                if (OpalUtil.isNotEmpty(batchDisplayProperty.getColorCodeColumn())) {
                    String string = ds.getValue(0, batchDisplayProperty.getColorCodeColumn(), "");
                    String batchBackgroundColor = batchDisplayProperty.getColorCodeMap().get(string);
                    if (OpalUtil.isNotEmpty(batchBackgroundColor)) {
                        int intValue = Integer.parseInt(batchBackgroundColor, 16);
                        batch.setBatchBackgroundColor(new Color(intValue));
                    }
                }
                if (OpalUtil.isNotEmpty(this.hrefURL)) {
                    void var16_24;
                    String string = this.hrefURL;
                    for (String token3 : tokens = StringUtil.getTokens(this.hrefURL)) {
                        String string2 = StringUtil.replaceAll((String)var16_24, "[" + token3 + "]", ds.getValue(0, token3, ""));
                    }
                    batch.setHrefURL((String)var16_24);
                }
                batch.setDetailPageURL(batchDisplayProperty.getDetailpageurl());
                continue;
            }
            batch.setDisplayValue("Element Configuration Error");
        }
    }

    private Batch createDummyNode() {
        Batch dummyNode = new Batch("", "...");
        dummyNode.setDisplayValue("...");
        return dummyNode;
    }

    public void setLayout() {
        int totalHeight;
        this.arrangeChildren(0, 0, this.rootBatch);
        int rootchildY = this.rootBatch.getY();
        this.arrangeParents(0, 0, this.rootBatch);
        int rootparentY = this.rootBatch.getY();
        int minWidth = -1 * this.getMinWidth(this.rootBatch);
        int totalWidth = minWidth + this.getMaxWidth(this.rootBatch) + 20;
        int childHeight = this.getMaxChildrenHeight(this.rootBatch);
        int parentHeight = this.getMaxParentHeight(this.rootBatch);
        if (childHeight > parentHeight) {
            this.rootBatch.setY(rootchildY);
            totalHeight = childHeight + 20;
            this.yShiftParents(rootchildY - rootparentY, this.rootBatch);
        } else {
            if (this.rootBatch.getChildren().size() == 0 && this.rootBatch.getParents().size() == 0) {
                this.rootBatch.setY(rootparentY + 35);
            } else {
                this.rootBatch.setY(rootparentY);
            }
            totalHeight = parentHeight + 20;
            this.yShiftChildren(rootparentY - rootchildY, this.rootBatch);
        }
        this.xShift(minWidth + 100, this.rootBatch, 0);
        this.imageHeight = totalHeight;
        this.imageWidth = totalWidth + 400;
    }

    private void arrangeChildren(int depth, int verticalOffset, Batch currentNode) {
        List<Batch> children = currentNode.getChildren();
        if (depth > 0 || children.size() > 0) {
            int overall = this.getChildHeight(currentNode);
            int x = this.rectWidth / 2 + 10 + depth * (this.rectWidth + 40);
            int y = verticalOffset + overall / 2;
            currentNode.setX(x);
            currentNode.setY(y);
            if (x > this.maxXPos) {
                this.maxXPos = x;
            }
            int offset = verticalOffset;
            for (Batch child : children) {
                this.arrangeChildren(depth + 1, offset, child);
                offset += this.getChildHeight(child);
            }
        }
    }

    public void arrangeParents(int depth, int verticalOffset, Batch currentBatch) {
        List<Batch> parents = currentBatch.getParents();
        if (depth < 0 || parents.size() > 0) {
            int overall = this.getParentHeight(currentBatch);
            int x = this.rectWidth / 2 + 10 + depth * (this.rectWidth + 40);
            int y = verticalOffset + overall / 2;
            currentBatch.setX(x);
            currentBatch.setY(y);
            if (x < this.minXPos) {
                this.minXPos = x;
            }
            int offset = verticalOffset;
            for (Batch parent : parents) {
                this.arrangeParents(depth - 1, offset, parent);
                offset += this.getParentHeight(parent);
            }
        }
    }

    public int getChildHeight(Batch currentBatch) {
        List<Batch> children = currentBatch.getChildren();
        if (children.size() == 0) {
            return this.rectHeight + this.verticalSpace;
        }
        int height = 0;
        for (Batch child : children) {
            height += this.getChildHeight(child);
        }
        return height;
    }

    public int getParentHeight(Batch currentBatch) {
        List<Batch> parents = currentBatch.getParents();
        if (parents.size() == 0) {
            return this.rectHeight + this.verticalSpace;
        }
        int height = 0;
        for (Batch parent : parents) {
            height += this.getParentHeight(parent);
        }
        return height;
    }

    public void xShift(int shiftX, Batch currentNode, int direction) {
        if (!currentNode.isShifted()) {
            int x = currentNode.getX() + shiftX;
            currentNode.setX(x);
            currentNode.setShifted(true);
            if (x > this.maxXPos) {
                this.maxXPos = x;
            }
        }
        if (direction >= 0) {
            List<Batch> children = currentNode.getChildren();
            for (Batch child : children) {
                this.xShift(shiftX, child, 1);
            }
        }
        if (direction <= 0) {
            List<Batch> parents = currentNode.getParents();
            for (Batch parent : parents) {
                this.xShift(shiftX, parent, -1);
            }
        }
    }

    public void addMapArea(JSONArray jArray) {
        JSONObject object;
        String href;
        String coords;
        String displayValue;
        int y;
        int x;
        JSONObject objectBSeq = new JSONObject();
        try {
            if (this.batchSequence != null) {
                objectBSeq.put("batchsequence", this.batchSequence);
            } else {
                objectBSeq.put("batchsequence", "");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        jArray.put(objectBSeq);
        this.addMapArea(jArray, this.rootBatch, 0);
        String onMousedown = "";
        String title = "";
        if (this.drawNextIcon) {
            x = this.maxXPos + this.rectWidth / 2 + 10;
            y = this.imageHeight / 2 - 5;
            this.nextIcon = new Batch("", "next");
            displayValue = String.valueOf(this.maxChildLevel - this.childLevel) + " more";
            this.nextIcon.setDisplayValue(displayValue);
            this.nextIcon.setX(x);
            this.nextIcon.setY(y);
            onMousedown = "next";
            title = "This is information about next icon";
            coords = "" + (this.nextIcon.getX() + this.rectWidth / 2) + "," + (this.nextIcon.getY() - this.rectHeight / 2) + "," + (this.nextIcon.getX() - this.rectWidth / 2) + "," + (this.nextIcon.getY() + this.rectHeight / 2) + "";
            href = null;
            object = this.getJSONObject(onMousedown, coords, title, href, null, "next");
            jArray.put(object);
        }
        if (this.drawPrevIcon) {
            x = 0;
            y = this.imageHeight / 2 - 5;
            this.prevIcon = new Batch("", "prev");
            displayValue = "          " + String.valueOf(this.maxParentLevel - this.parentLevel) + " more";
            this.prevIcon.setDisplayValue(displayValue);
            this.prevIcon.setX(x);
            this.prevIcon.setY(y);
            onMousedown = "prev";
            title = "This is information about prev icon";
            coords = "" + (this.prevIcon.getX() + this.rectWidth / 2) + "," + (this.prevIcon.getY() - this.rectHeight / 2) + "," + 1 + "," + (this.prevIcon.getY() + this.rectHeight / 2) + "";
            href = null;
            object = this.getJSONObject(onMousedown, coords, title, href, null, "prev");
            jArray.put(object);
        }
    }

    public void drawImage(Graphics2D g) {
        this.drawChildren(g, null, this.rootBatch);
        this.drawParents(g, null, this.rootBatch);
        this.drawArrow(g);
        if (this.modeColor != null) {
            this.drawLegend(g);
        }
    }

    private void drawLegend(Graphics2D g) {
        int x = 0;
        int y = this.imageHeight;
        if (this.legendTitle.length() > 0) {
            g.drawString(this.legendTitle, x + 25, y + 16);
            y += 25;
        }
        x = 25;
        for (String node : this.modeColor.keySet()) {
            if (!this.legendTextMap.containsKey(node)) continue;
            Color color = this.modeColor.get(node);
            String text = this.legendTextMap.get(node);
            g.setColor(color);
            g.fillRoundRect(x, y, 20, 20, 3, 3);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, 20, 20, 3, 3);
            String string = text = text != null ? text : node;
            if (text == null) {
                text = "Null";
            }
            g.drawString(text, x + 25, y + 16);
            y += 22;
        }
        this.imageHeight += y;
    }

    private void drawArrow(Graphics2D g) {
        if (this.drawPrevIcon) {
            String prevIconPath = this.webAppRoot + "WEB-CORE/images/gif/Back.gif";
            this.drawNavigator(g, this.prevIcon.getDisplayValue(), this.prevIcon.getX(), this.prevIcon.getY(), Color.blue, prevIconPath, this.prevIcon.getX(), this.prevIcon.getY() - 10);
        }
        if (this.drawNextIcon) {
            String nextIconPath = this.webAppRoot + "WEB-CORE/images/gif/Forward.gif";
            this.drawNavigator(g, this.nextIcon.getDisplayValue(), this.nextIcon.getX(), this.nextIcon.getY(), Color.blue, nextIconPath, this.nextIcon.getX() + 30, this.nextIcon.getY() - 10);
        }
    }

    private void drawNavigator(Graphics2D g, String text, int x, int y, Color color, String iconPath, int iconX, int iconY) {
        g.setColor(color);
        Image img = Toolkit.getDefaultToolkit().getImage(iconPath);
        g.drawImage(img, iconX + 10, iconY, Color.WHITE, null);
        this.drawString(g, text, x, y);
    }

    private void drawChildren(Graphics2D g, Batch parent, Batch batch) {
        List<Batch> children;
        this.drawText(g, batch.getDisplayValue(), batch.getX(), batch.getY(), batch.getBatchBackgroundColor());
        if (parent != null) {
            this.drawArrow(g, parent.getX() + this.rectWidth / 2, parent.getY(), batch.getX() - this.rectWidth / 2, batch.getY());
        }
        if ((children = batch.getChildren()).size() > 0) {
            for (Batch child : children) {
                this.drawChildren(g, batch, child);
            }
        }
    }

    private void drawParents(Graphics2D g, Batch child, Batch batch) {
        List<Batch> parents;
        Color selectedColor = batch.getBatchBackgroundColor();
        this.drawText(g, batch.getDisplayValue(), batch.getX(), batch.getY(), selectedColor);
        if (child != null) {
            this.drawArrow(g, batch.getX() + this.rectWidth / 2, batch.getY(), child.getX() - this.rectWidth / 2, child.getY());
        }
        if ((parents = batch.getParents()).size() > 0) {
            for (Batch parent : parents) {
                this.drawParents(g, batch, parent);
            }
        }
    }

    private void addMapArea(JSONArray jArray, Batch batch, int direction) {
        if (batch.getKeyId().equals("...")) {
            return;
        }
        String onMousedown = "event,'" + batch.getKeyId() + "'," + (batch.getY() - this.rectHeight / 2) + "," + (batch.getX() + this.rectWidth / 2);
        String coords = batch.getX() + this.rectWidth / 2 + "," + (batch.getY() - this.rectHeight / 2) + "," + (batch.getX() - this.rectWidth / 2) + "," + (batch.getY() + this.rectHeight / 2);
        String title = "This is information about " + batch.getKeyId();
        String href = batch.getHrefURL();
        if (!batch.getKeyId().equals(this.rootBatchId) && !this.hrefURL.contains("javascript")) {
            if (!this.hrefURL.contains("&keyid1=")) {
                href = href + "&keyid1=" + batch.getKeyId();
            }
            if (this.returnToListUrl != null && this.returnToListUrl.trim().length() > 0) {
                href = href + "&returntolistpage=" + this.returnToListUrl;
            }
        }
        String target = "_top";
        JSONObject object = this.getJSONObject(onMousedown, coords, title, href, target, batch.getKeyId());
        jArray.put(object);
        if (direction >= 0) {
            List<Batch> children = batch.getChildren();
            for (Batch child : children) {
                this.addMapArea(jArray, child, 1);
            }
        }
        if (direction <= 0) {
            List<Batch> parents = batch.getParents();
            for (Batch parent : parents) {
                this.addMapArea(jArray, parent, -1);
            }
        }
    }

    private JSONObject getJSONObject(String onMousedown, String coords, String title, String href, String target, String id) {
        JSONObject object = new JSONObject();
        try {
            object.put("onmousedown", onMousedown);
            object.put("shape", "rect");
            object.put("coords", coords);
            object.put("title", title);
            object.put("id", id);
            object.put("batchSequence", this.batchSequence);
            if (target != null) {
                object.put("target", target);
            }
            if (href != null) {
                object.put("href", href);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void drawText(Graphics2D g, String text, int x, int y, Color color) {
        Font font = new Font("SansSerif", 0, this.fontSize);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textHeight = metrics.getHeight();
        int textWidth = metrics.stringWidth(text);
        g.setColor(color);
        g.fillRoundRect(x - this.rectWidth / 2, y - this.rectHeight / 2, this.rectWidth, this.rectHeight, 5, 5);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x - this.rectWidth / 2, y - this.rectHeight / 2, this.rectWidth, this.rectHeight, 5, 5);
        if (text.contains("\n")) {
            this.drawString(g, text, x - this.rectWidth / 2 + 5, y + textHeight / 2 - this.textOffset - 15);
        } else {
            g.drawString(text, x - textWidth / 2, y + textHeight / 2 - this.textOffset);
        }
    }

    private void drawString(Graphics g, String text, int x, int y) {
        String[] lines;
        for (String line : lines = text.split("\n")) {
            g.drawString(line, x, y);
            y += g.getFontMetrics().getHeight();
        }
    }

    private void drawArrow(Graphics2D g, int x, int y, int xx, int yy) {
        float arrowWidth = 7.0f;
        float theta = 0.423f;
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        float[] vecLine = new float[2];
        float[] vecLeft = new float[2];
        xPoints[0] = xx;
        yPoints[0] = yy;
        vecLine[0] = (float)xPoints[0] - (float)x;
        vecLine[1] = (float)yPoints[0] - (float)y;
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];
        float fLength = (float)Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        float th = arrowWidth / (2.0f * fLength);
        float ta = arrowWidth / (2.0f * ((float)Math.tan(theta) / 2.0f) * fLength);
        float baseX = (float)xPoints[0] - ta * vecLine[0];
        float baseY = (float)yPoints[0] - ta * vecLine[1];
        xPoints[1] = (int)(baseX + th * vecLeft[0]);
        yPoints[1] = (int)(baseY + th * vecLeft[1]);
        xPoints[2] = (int)(baseX - th * vecLeft[0]);
        yPoints[2] = (int)(baseY - th * vecLeft[1]);
        g.drawLine(x, y, (int)baseX, (int)baseY);
        g.fillPolygon(xPoints, yPoints, 3);
    }

    public int getMaxWidth(Batch batch) {
        int maxWidth = batch.getX() + this.rectWidth / 2;
        List<Batch> children = batch.getChildren();
        if (children.size() > 0) {
            for (Batch child : children) {
                int childWidth = this.getMaxWidth(child);
                maxWidth = childWidth > maxWidth ? childWidth : maxWidth;
            }
        }
        return maxWidth;
    }

    public int getMinWidth(Batch node) {
        int minWidth = node.getX() - this.rectWidth / 2;
        for (Batch parent : node.getParents()) {
            int parentWidth = this.getMinWidth(parent);
            minWidth = parentWidth < minWidth ? parentWidth : minWidth;
        }
        return minWidth;
    }

    public void yShiftChildren(int shiftY, Batch node) {
        List<Batch> children = node.getChildren();
        for (Batch child : children) {
            child.setY(child.getY() + shiftY);
            this.yShiftChildren(shiftY, child);
        }
    }

    public void yShiftParents(int shiftY, Batch node) {
        List<Batch> parents = node.getParents();
        for (Batch parent : parents) {
            parent.setY(parent.getY() + shiftY);
            this.yShiftParents(shiftY, parent);
        }
    }

    public int getMaxChildrenHeight(Batch node) {
        List<Batch> children = node.getChildren();
        int maxHeight = node.getY() + this.rectHeight / 2;
        for (Batch child : children) {
            int childHeight = this.getMaxChildrenHeight(child);
            maxHeight = childHeight > maxHeight ? childHeight : maxHeight;
        }
        return maxHeight;
    }

    public int getMaxParentHeight(Batch node) {
        List<Batch> parents = node.getParents();
        int maxHeight = node.getY() + this.rectHeight / 2;
        for (Batch parent : parents) {
            int parentHeight = this.getMaxParentHeight(parent);
            maxHeight = parentHeight > maxHeight ? parentHeight : maxHeight;
        }
        return maxHeight;
    }

    public String getRootBatchSDC() {
        return this.rootBatch.getSdcid();
    }

    public String getDetailpageurl() {
        return this.detailpageurl;
    }
}

