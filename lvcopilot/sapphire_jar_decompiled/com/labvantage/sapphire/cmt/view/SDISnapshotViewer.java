/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.set.ListOrderedSet
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.cmt.GetSnapshotParseHTML;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.PropertyTreeRO;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.set.ListOrderedSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;
import sapphire.xml.cmt.SnapshotItem;

public class SDISnapshotViewer
extends SnapshotViewer {
    public static final String modifiedcolor = "#FF4300";
    public static final String modifiedicon = "rc?command=image&image=NoteEdit&color=%23FF4300";

    public SDISnapshotViewer() {
    }

    public SDISnapshotViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection) {
        super.initialize(sapphireConnection);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot) throws SapphireException {
        return SDISnapshotViewer.getHtml(sapphireConnection, srcSDISnapshot, true, false, true, false);
    }

    public static String getHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, boolean showAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        return SDISnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, srcSDISnapshot, showAuditColumns, usecustomrenderer, hideEmptyColumns, hideInheritedProperties);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot) throws SapphireException {
        return SDISnapshotViewer.hasDiff(sapphireConnection, srcSDISnapshot, refSDISnapshot, false);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean includeAuditColumns) throws SapphireException {
        return SDISnapshotViewer.hasDiff(sapphireConnection, srcSDISnapshot, refSDISnapshot, includeAuditColumns, null);
    }

    public static boolean hasDiff(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean includeAuditColumns, Set<String> ignoreColList) throws SapphireException {
        SDISnapshotViewer ssViewer = new SDISnapshotViewer(sapphireConnection);
        return ssViewer.hasDiff(srcSDISnapshot, refSDISnapshot, includeAuditColumns, ignoreColList);
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot) throws SapphireException {
        return SDISnapshotViewer.getDiffHtml(sapphireConnection, srcSDISnapshot, refSDISnapshot, true, false, true, false);
    }

    public static SDISnapshotViewer getCustomRenderer(SapphireConnection sapphireConnection, String sdcId) {
        try {
            String utilclassname = "com.labvantage.sapphire.cmt.view." + sdcId.substring(0, 1).toUpperCase() + sdcId.substring(1) + "Viewer";
            SDISnapshotViewer sdiSnapshotViewer = (SDISnapshotViewer)Class.forName(utilclassname).newInstance();
            sdiSnapshotViewer.initialize(sapphireConnection);
            return sdiSnapshotViewer;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        SDISnapshotViewer ssViewer = new SDISnapshotViewer(sapphireConnection);
        if (usecustomrenderer && (ssViewer = SDISnapshotViewer.getCustomRenderer(sapphireConnection, srcSDISnapshot.getSDCId())) == null) {
            ssViewer = new SDISnapshotViewer(sapphireConnection);
        }
        ConfigReportContent snapshotContent = ssViewer.getHtml(srcSDISnapshot, refSDISnapshot, showAuditColumns, hideEmptyColumns, hideInheritedProperties);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(snapshotContent);
        return stringBuilder.toString();
    }

    protected String[] getSDITableLabelInfo(SDISnapshotItem sdiSnapshotItem) throws SapphireException {
        String sdcid = sdiSnapshotItem.getSDCId();
        HashMap propertyList = this.sdcProcessor.getSDCProperties(sdcid);
        return this.getSDITableLabelInfo(sdcid);
    }

    protected String[] getSDITableLabelInfo(String sdcid) throws SapphireException {
        HashMap propertyList = this.sdcProcessor.getSDCProperties(sdcid);
        String[] labelinfo = new String[]{(String)propertyList.get("tablelabel"), (String)propertyList.get("itemdisplay")};
        Trace.logDebug("LinkLabelinfo for " + sdcid + " tablelabel:" + labelinfo[0] + " itemdisplay:" + labelinfo[1]);
        return labelinfo;
    }

    protected String getFormattedItemLabel(SDIData sdiData, String labelformat) {
        return this.getFormattedItemLabel(sdiData, labelformat, "");
    }

    private String getFormattedItemLabel(DataSet dataSet, String labelformat) {
        return this.getFormattedItemLabel(dataSet, labelformat, "");
    }

    protected String getFormattedItemLabel(SDIData sdiData, String labelformat, String defaultval) {
        if (labelformat == null || labelformat.length() == 0) {
            return SDISnapshotViewer.getSDI(sdiData).toString();
        }
        DataSet primary = sdiData.getDataset("primary");
        return this.getFormattedItemLabel(primary, labelformat, defaultval);
    }

    private String getFormattedItemLabel(DataSet dataSet, String labelformat, String defaultval) {
        String[] colnames = dataSet.getColumns();
        for (int i = 0; i < colnames.length; ++i) {
            if (!labelformat.contains(colnames[i])) continue;
            labelformat = labelformat.replace('[' + colnames[i] + ']', dataSet.getValue(0, colnames[i]));
        }
        if (labelformat == null || labelformat.length() == 0) {
            labelformat = defaultval;
        }
        return labelformat;
    }

    protected static SDI getSDI(SDIData sdiData) {
        String sdcid = sdiData.getSdcid();
        String[] keyes = sdiData.getKeys("primary");
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, keyes[0]);
        String keyid2 = keyes.length > 1 ? primary.getValue(0, keyes[1]) : "";
        String keyid3 = keyes.length > 2 ? primary.getValue(0, keyes[2]) : "";
        return new SDI(sdcid, keyid1, keyid2, keyid3);
    }

    private int getKeyCount(String[] primarykeycols) {
        int parentkeycount = 0;
        for (int c = 0; c < primarykeycols.length; ++c) {
            if (primarykeycols[c].length() <= 0) continue;
            ++parentkeycount;
        }
        return parentkeycount;
    }

    public String getVersionStatus(SDIData sdiData) {
        String colVal = this.getPrimaryValue(sdiData, "versionstatus");
        String versionStatus = "";
        versionStatus = "A".equals(colVal) ? "Active" : ("P".equals(colVal) ? "Provisional" : "");
        return versionStatus;
    }

    public SDISnapshotItem findMatchingSnapshotItem(List<SnapshotItem> items, SDISnapshotItem sdiItem) throws SapphireException {
        SDIData sdiItemData = sdiItem.getSDIData();
        String[] keyes = sdiItemData.getKeys("primary");
        String[] identifiers = sdiItem.getIdentifierColumns();
        boolean versioned = "Y".equals(this.sdcProcessor.getProperty(sdiItem.getSDCId(), "versionedflag"));
        if (!versioned && identifiers != null && identifiers.length > 0) {
            keyes = identifiers;
        }
        for (SnapshotItem item : items) {
            SDIData listItemSDIData = item.getSDIData();
            DataSet listItemPrimary = listItemSDIData.getDataset("primary");
            if (!item.getSDCId().equals(sdiItemData.getSdcid())) continue;
            boolean match = true;
            for (int i = 0; i < keyes.length; ++i) {
                if (sdiItemData.getDataset("primary").getValue(0, keyes[i], "").equals(listItemPrimary.getValue(0, keyes[i], ""))) continue;
                match = false;
                break;
            }
            if (!match) continue;
            return (SDISnapshotItem)item;
        }
        return null;
    }

    public static String getHeadingWithStatus(String heading, String diffstatus) {
        heading = diffstatus.equals("Modified") ? ConfigReportContent.getModifiedString(heading) : (diffstatus.equals("New") ? ConfigReportContent.getNewString(heading) : (diffstatus.equals("Deleted") ? ConfigReportContent.getDeletedString(heading) : "<font color=black>" + heading + "</black>"));
        return heading;
    }

    public boolean hasDiff(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns) throws SapphireException {
        return this.hasDiff(srcSDISnapshot, refSDISnapshot, showAuditColumns, null);
    }

    public boolean hasDiff(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, Set<String> ignoreColList) throws SapphireException {
        boolean hasDiff = false;
        try {
            if (srcSDISnapshot != null && srcSDISnapshot.getSnapshotItem() != null && srcSDISnapshot.getSnapshotItem().getSDIData() != null) {
                hasDiff = this.hasDiffSDISnapshot(srcSDISnapshot, refSDISnapshot, showAuditColumns, ignoreColList);
            }
        }
        catch (Throwable t) {
            Trace.logError("Server Error", t);
        }
        return hasDiff;
    }

    protected JSONArray getSDINodeTree(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, DataSet nodeinfo) throws JSONException, SapphireException {
        JSONArray rootArray = new JSONArray();
        SDISnapshot sdiSnapshot = srcSDISnapshot;
        SDISnapshotItem primaryItem = sdiSnapshot.getSnapshotItem();
        if (refSDISnapshot == null) {
            refSDISnapshot = srcSDISnapshot;
        }
        String[] ignoreDataSets = null;
        ignoreDataSets = this.getIgnoreDataSets();
        NodeItem nodeItem = NodeItem.getSDINodeTree(ignoreDataSets, sdiSnapshot, primaryItem, refSDISnapshot, refSDISnapshot.getSnapshotItem(), nodeinfo, this.translationProcessor);
        rootArray.put(nodeItem);
        return rootArray;
    }

    public ConfigReportContent getHtml(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        boolean newFlag = false;
        if (srcSDISnapshot == null) {
            throw new SapphireException("Source snapshot is null");
        }
        ConfigReportContent out = new ConfigReportContent(srcSDISnapshot.getSDCId() + ":" + srcSDISnapshot.getKeyId1() + ":" + srcSDISnapshot.getKeyId2() + ":" + srcSDISnapshot.getKeyId3(), this.translationProcessor);
        try {
            if (srcSDISnapshot.getSnapshotItem() != null && srcSDISnapshot.getSnapshotItem().getSDIData() != null) {
                ConfigReportContent rhs = new ConfigReportContent("contentframe", this.translationProcessor);
                rhs.append("<table> <tr> <td>");
                ConfigReportContent contentRwMode = this.renderSDISnapshotDiff(srcSDISnapshot, refSDISnapshot, showAuditColumns, true, hideEmptyColumns, hideInheritedProperties, false);
                rhs.append(contentRwMode.toString());
                rhs.append("</td></tr></table>");
                DataSet nodeinfo = contentRwMode.getNodeInfo();
                JSONArray rootArray = this.getSDINodeTree(srcSDISnapshot, refSDISnapshot, nodeinfo);
                out.append("\n<script>");
                out.append("\nvar initialContextData=" + rootArray.toString(4) + ";");
                out.append("\nvar mode=\"snapshotview\"");
                out.append("\nvar navigator_props=" + new PropertyList().toJSONString(false, false) + ";");
                out.append("\nsapphire.gwt.addGWTElement('navigator','navigator', navigator_props );");
                out.append("\n</script>");
                out.append("\n<div style=\"display:none\" id=\"" + srcSDISnapshot.getSnapshotItem().toString() + "\">" + rhs.toString() + "</div>");
            } else {
                out.append("<P>No snapshot retrieved!");
            }
        }
        catch (Throwable t) {
            Trace.logError("Server Error", t);
            out.append("\n<P>Server Error:" + t.getMessage());
        }
        out.append("\n<script>");
        out.append("\nvar currentRootNodeid;");
        out.append("\nvar currentEl;");
        out.append("\nfunction snapshotNodeSelected(nodeid, rootnodeid) {//called when a node is clicked");
        out.append("\n   var isRootNode = nodeid == rootnodeid;");
        out.append("\n if (isRootNode) { ");
        out.append("\nshowSnapshotHTML(rootnodeid);//Snapshot Navigator API method to replace right panel with the innerHtml from element with the id;");
        out.append("\n} else {");
        out.append("\nif (rootnodeid != currentRootNodeid) {");
        out.append("\nshowSnapshotHTML(rootnodeid)");
        out.append("\ncurrentRootNodeid = rootnodeid;");
        out.append("\n}");
        out.append("\nif (currentEl != null) {");
        out.append("\ncurrentEl.style.backgroundColor = '';");
        out.append("\n}");
        out.append("\ncurrentEl = document.getElementById(nodeid);");
        out.append("\ncurrentEl.style.backgroundColor = 'lightblue';");
        out.append("\ncurrentEl.scrollIntoView();");
        out.append("\n} //API from Navigator to update right panel");
        out.append("\n}");
        out.append("\n</script>");
        return out;
    }

    private void renderItemDetails(ConfigReportContent configReportContent, SDISnapshotItem sdiSnapshotItem, boolean showAuditColumns, boolean hideEmptyColumns, String status) throws SapphireException {
        Set datasets;
        SDIData sdiData = sdiSnapshotItem.getSDIData();
        if (sdiData != null && (datasets = sdiData.getDatasets()) != null) {
            for (String dataset : datasets) {
                String detailTable;
                if (dataset.equals("category") || (detailTable = SDIData.getDatasetTablename(dataset)).equals("primary")) continue;
                this.renderDetailDataSetDiff(configReportContent, sdiData, null, dataset, detailTable, showAuditColumns, true, hideEmptyColumns);
            }
        }
    }

    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        this.renderItemDetailsDiff(configReportContent, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns, sourceItem == null ? null : (sourceItem.getSDIData() != null ? sourceItem.getSDIData().getDatasets() : new HashSet()));
    }

    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, Set<String> datasets) throws SapphireException {
        if (refItem == null) {
            this.renderItemDetails(configReportContent, sourceItem, showAuditColumns, hideEmptyColumns, "New");
        } else if (sourceItem == null) {
            this.renderItemDetails(configReportContent, refItem, showAuditColumns, hideEmptyColumns, "Deleted");
        } else {
            SDIData sourceSDIData = sourceItem.getSDIData();
            SDIData refSDIData = refItem.getSDIData();
            if (sourceSDIData != null && refSDIData != null && datasets != null) {
                for (String dataset : datasets) {
                    String detailTable;
                    if (dataset.equals("category") || (detailTable = SDIData.getDatasetTablename(dataset)).equals("primary")) continue;
                    this.renderDetailDataSetDiff(configReportContent, sourceSDIData, refSDIData, dataset, detailTable, showAuditColumns, showTranslation, hideEmptyColumns);
                }
            }
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        if (str.length() > 0) {
            configReportContent.appendSpecialContent(str);
        }
    }

    protected void renderCategores(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        this.renderDetailDataSetDiff(configReportContent, sourceItem == null ? null : sourceItem.getSDIData(), refItem == null ? null : refItem.getSDIData(), "category", "categoryitem", showAuditColumns, showTranslation, hideEmptyColumns);
    }

    protected void renderRoles(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        this.renderDetailDataSetDiff(configReportContent, sourceItem == null ? null : sourceItem.getSDIData(), refItem == null ? null : refItem.getSDIData(), "role", "sdirole", showAuditColumns, showTranslation, hideEmptyColumns);
    }

    protected void renderOtherCommonDetails(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        this.renderDetailDataSetDiff(configReportContent, sourceItem == null ? null : sourceItem.getSDIData(), refItem == null ? null : refItem.getSDIData(), "attachment", "sdiattachment", showAuditColumns, showTranslation, hideEmptyColumns);
        this.renderDetailDataSetDiff(configReportContent, sourceItem == null ? null : sourceItem.getSDIData(), refItem == null ? null : refItem.getSDIData(), "attribute", "sdiattribute", showAuditColumns, showTranslation, hideEmptyColumns);
        this.renderDetailDataSetDiff(configReportContent, sourceItem == null ? null : sourceItem.getSDIData(), refItem == null ? null : refItem.getSDIData(), "approval", "sdiapproval", showAuditColumns, showTranslation, hideEmptyColumns);
    }

    protected ConfigReportContent renderCertifications(SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("SDI Certifications", this.translationProcessor);
        this.renderDetailDataSetDiff(content, sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), "s_sdicertification", "s_sdicertification", showAuditColumns, showTranslation, hideEmptyColumns);
        return content;
    }

    protected void renderSDIAttributes(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem) throws SapphireException {
        SDIData sourceSDIData = sourceItem.getSDIData();
        SDIData refSDIData = refItem.getSDIData();
        String dataset = "attribute";
        String detailTable = "sdiattribute";
        if (sourceSDIData.getDataset(dataset) != null && (sourceSDIData.getDataset(dataset).getRowCount() > 0 || refSDIData.getDataset(dataset).getRowCount() > 0)) {
            DataSet srcDS = this.getAttributeColumns(sourceSDIData.getDataset(dataset));
            DataSet refDS = this.getAttributeColumns(refSDIData.getDataset(dataset));
            ConfigReportContent detailContent = new ConfigReportContent(detailTable, this.translationProcessor);
            detailContent.startSubSection("Attributes", "");
            detailContent.renderDiffListTable(srcDS, refDS, new String[]{"atrributeid"});
            configReportContent.appendNodeContent(detailContent, "sdiattribute", "Attributes");
        }
    }

    private DataSet getAttributeColumns(DataSet fullDS) {
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Attribute", 0);
        ret.addColumn("Information Text", 0);
        ret.addColumn("Required", 0);
        ret.addColumn("Linked To", 0);
        ret.addColumn("Position", 0);
        ret.addColumn("Type", 0);
        ret.addColumn("Editor Style", 0);
        ret.addColumn("Editor SDCId", 0);
        ret.addColumn("Editor RefType", 0);
        ret.addColumn("Updateable", 0);
        ret.addColumn("Hidden", 0);
        ret.addColumn("Default Value", 0);
        for (int i = 0; i < fullDS.getRowCount(); ++i) {
            int row = ret.addRow();
            ret.setValue(row, "Attribute", "(auto)");
            ret.setValue(row, "Information Text", fullDS.getValue(i, "instructiontext", ""));
            ret.setValue(row, "Required", fullDS.getValue(i, "mandatoryflag", ""));
            ret.setValue(row, "Linked To", fullDS.getValue(i, "sourcesdcid", ""));
            ret.setValue(row, "Position", fullDS.getValue(i, "worksheetcontext", ""));
            ret.setValue(row, "Type", fullDS.getValue(i, "instructionflag", ""));
            ret.setValue(row, "Editor Style", fullDS.getValue(i, "editorstyleid", ""));
            ret.setValue(row, "Editor SDCId", fullDS.getValue(i, "editsdcid", ""));
            ret.setValue(row, "Updateable", fullDS.getValue(i, "updateableflag", ""));
            ret.setValue(row, "Hidden", fullDS.getValue(i, "hiddenflag", ""));
            ret.setValue(row, "Default Value", fullDS.getValue(i, "defaulttextvalue", ""));
        }
        return ret;
    }

    public void renderDetailDataSetDiff(ConfigReportContent configReportContent, SDIData sourceSDIData, SDIData refSDIData, String dataset, String table, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        if (sourceSDIData != null && sourceSDIData.getDataset(dataset) != null) {
            String tablelinkid = sourceSDIData.getLinkid(table);
            if (sourceSDIData.getDataset(dataset) != null && sourceSDIData.getDataset(dataset).getRowCount() > 0 || refSDIData != null && refSDIData.getDataset(dataset) != null && refSDIData.getDataset(dataset).getRowCount() > 0) {
                DataSet cleanSource = new DataSet();
                if (sourceSDIData.getDataset(dataset) != null) {
                    cleanSource.copyRow(sourceSDIData.getDataset(dataset), -1, 1);
                }
                if (!showAuditColumns) {
                    cleanSource = ConfigReportContent.removeAuditColumns(cleanSource);
                }
                DataSet cleanRef = new DataSet();
                if (refSDIData != null && refSDIData.getDataset(dataset) != null) {
                    cleanRef.copyRow(refSDIData.getDataset(dataset), -1, 1);
                }
                if (!showAuditColumns) {
                    cleanRef = ConfigReportContent.removeAuditColumns(cleanRef);
                }
                HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.sdcProcessor, table, cleanSource.getColumns());
                ConfigReportContent detailTableContent = new ConfigReportContent(table, this.translationProcessor);
                String detailTableLabel = "";
                String detailItemDisplay = "";
                if (tablelinkid != null) {
                    detailTableLabel = DDTLabelsUtil.getLinkTableLabel(this.sdcProcessor, sourceSDIData.getSdcid(), tablelinkid, table);
                    detailItemDisplay = DDTLabelsUtil.getLinkItemDisplay(this.sdcProcessor, sourceSDIData.getSdcid(), tablelinkid, table);
                } else {
                    detailTableLabel = this.getDetailLinkTableLabel(this.queryProcessor, sourceSDIData.getSdcid(), table);
                    detailItemDisplay = this.getDetailLinkItemDisplay(this.queryProcessor, sourceSDIData.getSdcid(), table);
                }
                if (showTranslation) {
                    detailTableLabel = this.translationProcessor.translate(detailTableLabel);
                }
                if (!table.startsWith("sdi")) {
                    detailTableContent.startSubSection(detailTableLabel, "");
                    detailTableContent.renderDetailTablesDiff(columnTitleMap, table, detailTableLabel, detailItemDisplay, cleanSource, cleanRef, sourceSDIData.getLinkTableKeys(table), this.translationProcessor, hideEmptyColumns);
                    configReportContent.appendNodeContent(detailTableContent, table, detailTableLabel);
                } else {
                    detailTableContent.startSubSection(detailTableLabel, "");
                    detailTableContent.renderDetailTablesDiff(columnTitleMap, table, detailTableLabel, detailItemDisplay, cleanSource, cleanRef, sourceSDIData.getKeys(dataset), this.translationProcessor, hideEmptyColumns);
                    configReportContent.appendNodeContent(detailTableContent, table, detailTableLabel);
                }
            }
        }
    }

    public String getDetailLinkTableLabel(QueryProcessor queryProcessor, String sdcid, String tableid) throws SapphireException {
        if (tableid.startsWith("sdi")) {
            return DDTLabelsUtil.getSDIDetailTableLabel(tableid);
        }
        if (tableid.equals("s_sdicertification")) {
            return "Certifications";
        }
        if (tableid.equals("categoryitem")) {
            return "Categories";
        }
        String label = tableid;
        String sql = "SELECT tablelabel, tableid, itemdisplay FROM systable WHERE lower( systable.tableid ) = ?";
        DataSet ret = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{tableid.toLowerCase()});
        if (ret != null && ret.getRowCount() > 0) {
            label = ret.getValue(0, "tablelabel", tableid);
        }
        return label;
    }

    public String getDetailLinkItemDisplay(QueryProcessor queryProcessor, String sdcid, String detailtableid) throws SapphireException {
        if (detailtableid.startsWith("sdi")) {
            return DDTLabelsUtil.getSDIDetailItemDisplay(detailtableid);
        }
        if (detailtableid.equals("s_sdicertification")) {
            return "[certificationtype], [certifiedforsdcid], [certifiedforkeyid1], [certifiedforkeyid2], [certifiedforkeyid3]";
        }
        if (detailtableid.equals("categoryitem")) {
            return "[categoryid]";
        }
        if (detailtableid.equals("sdcsecurity")) {
            return "[sdcid], [operationid], [sysuserid]";
        }
        if (detailtableid.equals("sdcjobtypesecurity")) {
            return "[sdcid], [operationid], [jobtypeid]";
        }
        String itemdisplay = "";
        String sql = "SELECT tablelabel, tableid, itemdisplay FROM systable WHERE lower( systable.tableid ) = ?";
        DataSet ret = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{detailtableid.toLowerCase()});
        if (ret != null && ret.getRowCount() > 0) {
            itemdisplay = ret.getValue(0, "itemdisplay", "");
        }
        return itemdisplay;
    }

    private boolean hasDiffSnapshotItemDetails(SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean includeAuditColumns) throws SapphireException {
        if (refItem == null) {
            return true;
        }
        if (sourceItem == null) {
            return true;
        }
        SDIData sourceSDIData = sourceItem.getSDIData();
        SDIData refSDIData = refItem.getSDIData();
        Set datasets = sourceItem.getSDIData().getDatasets();
        for (String dataset : datasets) {
            String[] keyColumns;
            String detailTable = SDIData.getDatasetTablename(dataset);
            if (detailTable.equals("primary") || sourceSDIData.getDataset(dataset) == null || sourceSDIData.getDataset(dataset).getRowCount() <= 0) continue;
            DataSet cleanSource = new DataSet();
            if (sourceSDIData.getDataset(dataset) != null) {
                cleanSource.copyRow(sourceSDIData.getDataset(dataset), -1, 1);
            }
            if (!includeAuditColumns) {
                cleanSource = ConfigReportContent.removeAuditColumns(cleanSource);
            }
            DataSet cleanRef = new DataSet();
            if (refSDIData.getDataset(dataset) != null) {
                cleanRef.copyRow(refSDIData.getDataset(dataset), -1, 1);
            }
            if (!includeAuditColumns) {
                cleanRef = ConfigReportContent.removeAuditColumns(cleanRef);
            }
            ConfigReportContent detailTableContent = new ConfigReportContent(detailTable, this.translationProcessor);
            if (!(!detailTable.startsWith("sdi") ? (detailTable.equals("sdcjobtypesecurity") ? detailTableContent.hasDiffDetailTables(cleanSource, cleanRef, keyColumns = new String[]{"jobtypeid", "sdcid", "operationid", "accesstype"}) : detailTableContent.hasDiffDetailTables(cleanSource, cleanRef, sourceSDIData.getLinkTableKeys(detailTable))) : detailTableContent.hasDiffDetailTables(cleanSource, cleanRef, sourceSDIData.getLinkTableKeys(detailTable)))) continue;
            return true;
        }
        return false;
    }

    private ConfigReportContent renderSDISnapshotDiff(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent primary = new ConfigReportContent("Primary", this.translationProcessor);
        primary.appendSpecialContent(this.renderPrimarySDISnapshotItem(srcSDISnapshot.getSnapshotItem(), refSDISnapshot == null ? null : refSDISnapshot.getSnapshotItem(), showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild));
        return primary;
    }

    private boolean hasDiffSDISnapshot(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns) throws SapphireException {
        return this.hasDiffSDISnapshot(srcSDISnapshot, refSDISnapshot, showAuditColumns, null);
    }

    private boolean hasDiffSDISnapshot(SDISnapshot srcSDISnapshot, SDISnapshot refSDISnapshot, boolean showAuditColumns, Set<String> ignoreColList) throws SapphireException {
        if (srcSDISnapshot == null || refSDISnapshot == null) {
            return true;
        }
        return this.hasDiffPrimarySDISnapshotItem(srcSDISnapshot.getSnapshotItem(), refSDISnapshot.getSnapshotItem(), showAuditColumns, ignoreColList);
    }

    private ConfigReportContent renderPrimarySDISnapshotItem(SDISnapshotItem srcSDISnapshotItem, SDISnapshotItem refSDISnapshotItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        SnapshotItem.LinkType[] linkTypes;
        String itemdisplay;
        String itemLabel;
        SDIData srcSDIData = null;
        if (srcSDISnapshotItem != null) {
            srcSDIData = srcSDISnapshotItem.getSDIData();
        }
        SDIData refSDIData = null;
        if (refSDISnapshotItem != null) {
            refSDIData = refSDISnapshotItem.getSDIData();
        }
        String[] pLabelInfo = new String[2];
        ConfigReportContent content = new ConfigReportContent("primary", this.translationProcessor);
        if (refSDIData == null) {
            pLabelInfo = this.getSDITableLabelInfo(srcSDISnapshotItem);
            String tablelabel = pLabelInfo[1];
            itemLabel = SDISnapshotViewer.getSDI(srcSDISnapshotItem.getSDIData()).toString();
            if (tablelabel != null && tablelabel.length() > 0) {
                itemLabel = this.getFormattedItemLabel(srcSDISnapshotItem.getSDIData(), tablelabel);
            }
            content.appendNodeContent(this.renderPrimary(srcSDISnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, "New", isChild), srcSDISnapshotItem.toString(), itemLabel, "New");
        } else if (srcSDIData == null) {
            pLabelInfo = this.getSDITableLabelInfo(refSDISnapshotItem);
            itemdisplay = pLabelInfo[1];
            itemLabel = SDISnapshotViewer.getSDI(refSDISnapshotItem.getSDIData()).toString();
            if (itemdisplay != null && itemdisplay.length() > 0) {
                itemLabel = this.getFormattedItemLabel(refSDISnapshotItem.getSDIData(), itemdisplay);
            }
            content.appendNodeContent(this.renderPrimary(refSDISnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, "Deleted", isChild), refSDISnapshotItem.toString(), itemLabel, "Deleted");
        } else {
            itemdisplay = this.getSDITableLabelInfo(srcSDISnapshotItem)[1];
            String itemLable = SDISnapshotViewer.getSDI(srcSDISnapshotItem.getSDIData()).toString();
            if (itemdisplay != null && itemdisplay.length() > 0) {
                itemLable = this.getFormattedItemLabel(srcSDISnapshotItem.getSDIData(), itemdisplay);
            }
            content.appendNodeContent(this.renderPrimaryDiff(srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild), srcSDISnapshotItem.toString(), itemLable);
        }
        this.renderItemDetailsDiff(content, srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns);
        List<Object> srcChildSnapShotItems = new ArrayList();
        for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
            List<String> srclinkIds = null;
            List<String> reflinkIds = null;
            List<String> nonNullLinks = null;
            if (srcSDISnapshotItem != null) {
                nonNullLinks = srclinkIds = srcSDISnapshotItem.getLinkIdsByType(linkType);
            }
            if (refSDISnapshotItem != null) {
                reflinkIds = refSDISnapshotItem.getLinkIdsByType(linkType);
            }
            if (srclinkIds != null && srclinkIds.size() == 0 && reflinkIds != null && reflinkIds.size() > 0) {
                nonNullLinks = reflinkIds;
            }
            if (nonNullLinks == null) continue;
            boolean hasChildren = false;
            for (String currlinkid : nonNullLinks) {
                ConfigReportContent snapshotContent;
                SDISnapshot embeddedRefSDISnapshot;
                SDISnapshot embeddedSrcSDISnapshot;
                boolean usecustomrenderer;
                SDISnapshotViewer embeddedSnapshotViewer;
                ConfigReportContent sdidatacontent;
                int i;
                String[] labelinfo;
                ConfigReportContent linkContent = new ConfigReportContent("Content for" + currlinkid, this.translationProcessor);
                if (srcSDISnapshotItem != null) {
                    srcChildSnapShotItems = srcSDISnapshotItem.getLinkItemsByLinkId(linkType, currlinkid);
                }
                List<Object> refChildSnapShotItems = new ArrayList();
                if (refSDISnapshotItem != null) {
                    refChildSnapShotItems = refSDISnapshotItem.getLinkItemsByLinkId(linkType, currlinkid);
                }
                String childsditablelabel = "";
                String childsdiitemdisplay = "";
                String childsdcid = "";
                if (srcChildSnapShotItems != null && srcChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)srcChildSnapShotItems.get(0));
                    childsditablelabel = labelinfo[0] != null ? labelinfo[0] : currlinkid;
                    childsdiitemdisplay = labelinfo[1] != null ? labelinfo[1] : "";
                    childsdcid = ((SnapshotItem)srcChildSnapShotItems.get(0)).getSDCId();
                } else if (refChildSnapShotItems != null && refChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)refChildSnapShotItems.get(0));
                    childsditablelabel = labelinfo[0] != null ? labelinfo[0] : currlinkid;
                    childsdiitemdisplay = labelinfo[1] != null ? labelinfo[1] : "";
                    childsdcid = ((SnapshotItem)refChildSnapShotItems.get(0)).getSDCId();
                }
                ConfigReportContent childsnapshots = new ConfigReportContent("childsnapshots", this.translationProcessor);
                linkContent.startSection(childsditablelabel);
                for (i = 0; i < srcChildSnapShotItems.size(); ++i) {
                    SDISnapshotItem srcItem = (SDISnapshotItem)srcChildSnapShotItems.get(i);
                    if (srcItem.isIncludedForTransfer()) continue;
                    hasChildren = true;
                    SDIData srcItemSDIData = srcItem.getSDIData();
                    if (srcItemSDIData == null) continue;
                    SDISnapshotItem refItem = this.findMatchingSnapshotItem(refChildSnapShotItems, srcItem);
                    sdidatacontent = new ConfigReportContent("", this.translationProcessor);
                    embeddedSnapshotViewer = new SDISnapshotViewer(this.sapphireConnection);
                    usecustomrenderer = true;
                    if (usecustomrenderer && (embeddedSnapshotViewer = SDISnapshotViewer.getCustomRenderer(this.sapphireConnection, srcItem.getSDCId())) == null) {
                        embeddedSnapshotViewer = new SDISnapshotViewer(this.sapphireConnection);
                    }
                    embeddedSrcSDISnapshot = new SDISnapshot();
                    embeddedSrcSDISnapshot.setSnapshotItem(srcItem);
                    embeddedRefSDISnapshot = new SDISnapshot();
                    if (refItem != null) {
                        embeddedRefSDISnapshot.setSnapshotItem(refItem);
                    }
                    snapshotContent = embeddedSnapshotViewer.renderSDISnapshotDiff(embeddedSrcSDISnapshot, embeddedRefSDISnapshot, showAuditColumns, true, hideEmptyColumns, hideInheritedProperties, true);
                    sdidatacontent.appendSpecialContent(snapshotContent);
                    linkContent.appendSpecialContent(sdidatacontent);
                }
                for (i = 0; i < refChildSnapShotItems.size(); ++i) {
                    SDISnapshotItem srcItem;
                    SDISnapshotItem refItem = (SDISnapshotItem)refChildSnapShotItems.get(i);
                    if (refItem.isIncludedForTransfer()) continue;
                    hasChildren = true;
                    SDIData refItemSDIData = refItem.getSDIData();
                    if (refItemSDIData == null || (srcItem = this.findMatchingSnapshotItem(srcChildSnapShotItems, refItem)) != null) continue;
                    sdidatacontent = new ConfigReportContent("", this.translationProcessor);
                    embeddedSnapshotViewer = new SDISnapshotViewer(this.sapphireConnection);
                    usecustomrenderer = true;
                    if (usecustomrenderer && (embeddedSnapshotViewer = SDISnapshotViewer.getCustomRenderer(this.sapphireConnection, refItem.getSDCId())) == null) {
                        embeddedSnapshotViewer = new SDISnapshotViewer(this.sapphireConnection);
                    }
                    embeddedSrcSDISnapshot = new SDISnapshot();
                    embeddedRefSDISnapshot = new SDISnapshot();
                    if (refItem != null) {
                        embeddedRefSDISnapshot.setSnapshotItem(refItem);
                    }
                    snapshotContent = embeddedSnapshotViewer.renderSDISnapshotDiff(embeddedSrcSDISnapshot, embeddedRefSDISnapshot, showAuditColumns, true, hideEmptyColumns, hideInheritedProperties, true);
                    sdidatacontent.appendSpecialContent(snapshotContent);
                    linkContent.appendSpecialContent(sdidatacontent);
                }
                if (!hasChildren) continue;
                content.appendNodeContent(linkContent, currlinkid, childsditablelabel);
            }
        }
        return content;
    }

    public static String generateTitle(String connectionid, DataSet changelogentries, String cachedsnapshotkey, boolean showsnapshotdiff, String targetsdikey) {
        String status;
        String title = "";
        if (cachedsnapshotkey.length() > 0) {
            SDISnapshot sourceSnapshot;
            title = !showsnapshotdiff ? ((sourceSnapshot = (SDISnapshot)GetSnapshotParseHTML.getCachedSnapshot(connectionid, cachedsnapshotkey)) != null ? "Viewing import content for " + sourceSnapshot.getSDCId() + ", " + sourceSnapshot.getKeyId1() + "," + sourceSnapshot.getKeyId2() + ", " + sourceSnapshot.getKeyId3() : "Viewing import content") : "Comparing snapshot being imported with the current database version for " + targetsdikey;
            return title;
        }
        title = changelogentries != null && changelogentries.getRowCount() == 1 ? ((status = changelogentries.getString(0, "changelogstatus")).equals("Checked In") ? (!showsnapshotdiff ? "Details of changes made to [sdikey] checked in on [checkedindt] by [checkedinby],([changelogid])" : "Details of changes made to [sdikey] and checked in on [checkedindt] by [checkedinby] ([changelogid])") : "Details of changes made to [sdikey] after being checked out by [checkedoutbyuserid] on [checkedoutdt] ([changelogid])") : "Differences between check-ins made on [dd/mm/yyyy] by [userid] ([changelogid]) and [dd/mm/yyyy] by [userid2], ([changelogid])";
        if (cachedsnapshotkey.length() == 0) {
            String pk = changelogentries.getString(0, "linksdcid") + ";" + changelogentries.getString(0, "linkkeyid1") + ";" + changelogentries.getString(0, "linkkeyid2", "") + ";" + changelogentries.getString(0, "linkkeyid3", "");
            title = title.replace("[sdikey]", pk);
            title = title.replace("[changelogid]", changelogentries.getString(0, "changelogid", ""));
            title = title.replace("[checkedinby]", changelogentries.getString(0, "checkedinby", ""));
            title = title.replace("[checkedoutbyuserid]", changelogentries.getString(0, "checkedoutbyuserid", "<missing>"));
            title = title.replace("[checkedinby]", changelogentries.getString(0, "checkedinby", "<missing>"));
            title = title.replace("[checkedindt]", changelogentries.getValue(0, "checkedindt", ""));
            title = title.replace("[checkedoutdt]", changelogentries.getValue(0, "checkedoutdt", ""));
        }
        return title;
    }

    private boolean hasDiffPrimarySDISnapshotItem(SDISnapshotItem srcSDISnapshotItem, SDISnapshotItem refSDISnapshotItem, boolean showAuditColumns) throws SapphireException {
        return this.hasDiffPrimarySDISnapshotItem(srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, null);
    }

    private boolean hasDiffPrimarySDISnapshotItem(SDISnapshotItem srcSDISnapshotItem, SDISnapshotItem refSDISnapshotItem, boolean showAuditColumns, Set<String> ignoreColList) throws SapphireException {
        SnapshotItem.LinkType[] linkTypes;
        SDIData srcSDIData = null;
        if (srcSDISnapshotItem != null) {
            srcSDIData = srcSDISnapshotItem.getSDIData();
        }
        SDIData refSDIData = null;
        if (refSDISnapshotItem != null) {
            refSDIData = refSDISnapshotItem.getSDIData();
        }
        if (srcSDIData == null || refSDIData == null) {
            return true;
        }
        if (this.hasDiffPrimary(srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, ignoreColList)) {
            return true;
        }
        if (this.hasDiffSnapshotItemDetails(srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns)) {
            return true;
        }
        List<Object> srcChildSnapShotItems = new ArrayList();
        for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
            List<String> linkIds = srcSDISnapshotItem != null ? srcSDISnapshotItem.getLinkIdsByType(linkType) : refSDISnapshotItem.getLinkIdsByType(linkType);
            for (String linkId : linkIds) {
                if (srcSDISnapshotItem != null) {
                    srcChildSnapShotItems = srcSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                List<Object> refChildSnapShotItems = new ArrayList();
                if (refSDISnapshotItem != null) {
                    refChildSnapShotItems = refSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                boolean hasGrandChildren = false;
                Object allDSNames = new ListOrderedSet();
                if (srcChildSnapShotItems.size() > 0 && ((SnapshotItem)srcChildSnapShotItems.get(0)).getSDIData() != null) {
                    allDSNames = ((SnapshotItem)srcChildSnapShotItems.get(0)).getSDIData().getDatasets();
                } else if (refChildSnapShotItems.size() > 0 && ((SnapshotItem)refChildSnapShotItems.get(0)).getSDIData() != null) {
                    allDSNames = ((SnapshotItem)refChildSnapShotItems.get(0)).getSDIData().getDatasets();
                }
                PropertyList policyNodeProps = null;
                PropertyListCollection embeddedSDCProps = srcSDISnapshotItem.getPolicyNodeProps().getCollectionNotNull("sdidatasets");
                for (String dsName : allDSNames) {
                    int i;
                    SDISnapshotItem refItem;
                    DataSet currentSrcDS = new DataSet();
                    String[] keyCols = null;
                    ConfigReportContent dataSetContent = new ConfigReportContent(dsName, this.translationProcessor);
                    for (int i2 = 0; i2 < srcChildSnapShotItems.size(); ++i2) {
                        SDIData sdiData;
                        DataSet temp;
                        SDISnapshotItem srcItem = (SDISnapshotItem)srcChildSnapShotItems.get(i2);
                        if (policyNodeProps == null) {
                            policyNodeProps = srcItem.getPolicyNodeProps();
                        }
                        if (srcItem.getLinkItems().size() > 0) {
                            hasGrandChildren |= true;
                        }
                        if ((temp = (sdiData = srcItem.getSDIData()).getDataset(dsName)) != null) {
                            currentSrcDS.copyRow(temp, -1, 1);
                            if (dsName.equals("primary")) {
                                currentSrcDS.setString(currentSrcDS.size() - 1, "_snapshotitemkeys", srcItem.toString());
                            }
                        }
                        keyCols = sdiData.getKeys(dsName);
                    }
                    DataSet currentRefDS = new DataSet();
                    for (int i3 = 0; i3 < refChildSnapShotItems.size(); ++i3) {
                        SDIData sdiData;
                        DataSet temp;
                        refItem = (SDISnapshotItem)refChildSnapShotItems.get(i3);
                        if (policyNodeProps == null) {
                            policyNodeProps = refItem.getPolicyNodeProps();
                        }
                        if (refItem.getLinkItems().size() > 0) {
                            hasGrandChildren |= true;
                        }
                        if ((temp = (sdiData = refItem.getSDIData()).getDataset(dsName)) == null) continue;
                        currentRefDS.copyRow(temp, -1, 1);
                        if (!dsName.equals("primary")) continue;
                        currentRefDS.setString(currentRefDS.size() - 1, "_snapshotitemkeys", refItem.toString());
                    }
                    if (currentSrcDS.getRowCount() > 0 || currentRefDS.getRowCount() > 0) {
                        PropertyListCollection detaildatasetsProps;
                        currentSrcDS = showAuditColumns ? currentSrcDS : ConfigReportContent.removeAuditColumns(currentSrcDS);
                        currentRefDS = showAuditColumns ? currentRefDS : ConfigReportContent.removeAuditColumns(currentRefDS);
                        PropertyList detailTableProps = null;
                        if (!dsName.startsWith("sdi")) {
                            if (policyNodeProps != null) {
                                detaildatasetsProps = policyNodeProps.getCollection("detaildatasets");
                                detailTableProps = detaildatasetsProps.find("table", dsName);
                            } else {
                                Trace.logDebug("Did not find policy node for snapshot items");
                            }
                        } else if (policyNodeProps != null) {
                            detaildatasetsProps = policyNodeProps.getCollection("sdidetaildatasets");
                            detailTableProps = detaildatasetsProps.find("table", dsName);
                        } else {
                            Trace.logDebug("Did not find policy node for snapshot items");
                        }
                        if (dataSetContent.hasDiffDetailTables(currentSrcDS, currentRefDS, keyCols)) {
                            return true;
                        }
                    }
                    if (!hasGrandChildren) continue;
                    for (i = 0; i < srcChildSnapShotItems.size(); ++i) {
                        SDISnapshotItem srcItem = (SDISnapshotItem)srcChildSnapShotItems.get(i);
                        SDIData srcItemSDIData = srcItem.getSDIData();
                        if (srcItemSDIData == null) continue;
                        SDISnapshotItem refItem2 = this.findMatchingSnapshotItem(refChildSnapShotItems, srcItem);
                        if (refItem2 == null) {
                            return true;
                        }
                        if (!this.hasDiffSDISnapshotItem(srcItem, refItem2, showAuditColumns)) continue;
                        return true;
                    }
                    for (i = 0; i < refChildSnapShotItems.size(); ++i) {
                        refItem = (SDISnapshotItem)refChildSnapShotItems.get(i);
                        SDIData refItemSDIData = refItem.getSDIData();
                        if (refItemSDIData == null) continue;
                        SDISnapshotItem srcItem = this.findMatchingSnapshotItem(srcChildSnapShotItems, refItem);
                        if (srcItem == null) {
                            return true;
                        }
                        if (!this.hasDiffSDISnapshotItem(srcItem, refItem, showAuditColumns)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ConfigReportContent renderSDISnapshotItem(SDISnapshotItem srcSDISnapshotItem, SDISnapshotItem refSDISnapshotItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        SnapshotItem.LinkType[] linkTypes;
        ConfigReportContent content = new ConfigReportContent("primary", this.translationProcessor);
        this.renderItemDetailsDiff(content, srcSDISnapshotItem, refSDISnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns);
        List<Object> srcChildSnapShotItems = new ArrayList();
        for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
            List<String> linkIds = srcSDISnapshotItem != null ? srcSDISnapshotItem.getLinkIdsByType(linkType) : refSDISnapshotItem.getLinkIdsByType(linkType);
            for (String linkId : linkIds) {
                String[] labelinfo;
                if (srcSDISnapshotItem != null) {
                    srcChildSnapShotItems = srcSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                List<Object> refChildSnapShotItems = new ArrayList();
                if (refSDISnapshotItem != null) {
                    refChildSnapShotItems = refSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                boolean hasGrandChildren = false;
                ConfigReportContent linkidContent = new ConfigReportContent(linkId, this.translationProcessor);
                Object allDSNames = new ListOrderedSet();
                String linkAlias = linkId;
                if (srcChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)srcChildSnapShotItems.get(0));
                    allDSNames = ((SnapshotItem)srcChildSnapShotItems.get(0)).getSDIData().getDatasets();
                    linkAlias = labelinfo[0] != null ? labelinfo[0] : linkId;
                } else if (refChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)refChildSnapShotItems.get(0));
                    allDSNames = ((SnapshotItem)refChildSnapShotItems.get(0)).getSDIData().getDatasets();
                    linkAlias = labelinfo[0] != null ? labelinfo[0] : linkId;
                }
                PropertyList policyNodeProps = null;
                PropertyListCollection embeddedSDCProps = srcSDISnapshotItem.getPolicyNodeProps().getCollectionNotNull("sdidatasets");
                Object keyCols = null;
                for (int i = 0; i < srcChildSnapShotItems.size(); ++i) {
                    SDISnapshotItem currentSrcChildItem = (SDISnapshotItem)srcChildSnapShotItems.get(i);
                    if (currentSrcChildItem.isIncludedForTransfer()) continue;
                    if (policyNodeProps == null) {
                        policyNodeProps = currentSrcChildItem.getPolicyNodeProps();
                    }
                    SDISnapshotItem currentRefChildSnapshotItem = null;
                    for (int j = 0; j < refChildSnapShotItems.size(); ++j) {
                        if (!((SnapshotItem)refChildSnapShotItems.get(j)).getKeyId1().equals(currentSrcChildItem.getKeyId1()) || !((SnapshotItem)refChildSnapShotItems.get(j)).getKeyId2().equals(currentSrcChildItem.getKeyId2()) || !((SnapshotItem)refChildSnapShotItems.get(j)).getKeyId3().equals(currentSrcChildItem.getKeyId3())) continue;
                        currentRefChildSnapshotItem = (SDISnapshotItem)refChildSnapShotItems.get(j);
                        break;
                    }
                    if (currentRefChildSnapshotItem == null) continue;
                    content.appendNodeContent(this.renderSDISnapshotItem(currentSrcChildItem, currentRefChildSnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns), linkId, linkAlias);
                }
            }
        }
        return content;
    }

    private boolean hasDiffSDISnapshotItem(SDISnapshotItem srcSDISnapshotItem, SDISnapshotItem refSDISnapshotItem, boolean includeAuditColumns) throws SapphireException {
        SnapshotItem.LinkType[] linkTypes;
        if (this.hasDiffSnapshotItemDetails(srcSDISnapshotItem, refSDISnapshotItem, includeAuditColumns)) {
            return true;
        }
        List<Object> srcChildSnapShotItems = new ArrayList();
        for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
            List<String> linkIds = srcSDISnapshotItem != null ? srcSDISnapshotItem.getLinkIdsByType(linkType) : refSDISnapshotItem.getLinkIdsByType(linkType);
            for (String linkId : linkIds) {
                String[] labelinfo;
                if (srcSDISnapshotItem != null) {
                    srcChildSnapShotItems = srcSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                List<Object> refChildSnapShotItems = new ArrayList();
                if (refSDISnapshotItem != null) {
                    refChildSnapShotItems = refSDISnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                }
                boolean hasGrandChildren = false;
                Object allDSNames = new ListOrderedSet();
                String linkAlias = linkId;
                if (srcChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)srcChildSnapShotItems.get(0));
                    allDSNames = ((SnapshotItem)srcChildSnapShotItems.get(0)).getSDIData().getDatasets();
                    linkAlias = labelinfo[0] != null ? labelinfo[0] : linkId;
                } else if (refChildSnapShotItems.size() > 0) {
                    labelinfo = this.getSDITableLabelInfo((SDISnapshotItem)refChildSnapShotItems.get(0));
                    allDSNames = ((SnapshotItem)refChildSnapShotItems.get(0)).getSDIData().getDatasets();
                    linkAlias = labelinfo[0] != null ? labelinfo[0] : linkId;
                }
                PropertyList policyNodeProps = null;
                for (String dsName : allDSNames) {
                    int i;
                    SDISnapshotItem refItem;
                    DataSet currentSrcDS = new DataSet();
                    String[] keyCols = null;
                    for (int i2 = 0; i2 < srcChildSnapShotItems.size(); ++i2) {
                        SDIData sdiData;
                        DataSet temp;
                        SDISnapshotItem srcItem = (SDISnapshotItem)srcChildSnapShotItems.get(i2);
                        if (policyNodeProps == null) {
                            policyNodeProps = srcItem.getPolicyNodeProps();
                        }
                        if (srcItem.getLinkItems().size() > 0) {
                            hasGrandChildren |= true;
                        }
                        if ((temp = (sdiData = srcItem.getSDIData()).getDataset(dsName)) != null) {
                            currentSrcDS.copyRow(temp, -1, 1);
                            if (dsName.equals("primary")) {
                                currentSrcDS.setString(currentSrcDS.size() - 1, "_snapshotitemkeys", srcItem.toString());
                            }
                        }
                        keyCols = sdiData.getKeys(dsName);
                    }
                    DataSet currentRefDS = new DataSet();
                    for (int i3 = 0; i3 < refChildSnapShotItems.size(); ++i3) {
                        SDIData sdiData;
                        DataSet temp;
                        refItem = (SDISnapshotItem)refChildSnapShotItems.get(i3);
                        if (policyNodeProps == null) {
                            policyNodeProps = refItem.getPolicyNodeProps();
                        }
                        if (refItem.getLinkItems().size() > 0) {
                            hasGrandChildren |= true;
                        }
                        if ((temp = (sdiData = refItem.getSDIData()).getDataset(dsName)) == null) continue;
                        currentRefDS.copyRow(temp, -1, 1);
                        if (!dsName.equals("primary")) continue;
                        currentRefDS.setString(currentRefDS.size() - 1, "_snapshotitemkeys", refItem.toString());
                    }
                    if (currentSrcDS.getRowCount() > 0 || currentRefDS.getRowCount() > 0) {
                        PropertyListCollection detaildatasetsProps;
                        currentSrcDS = includeAuditColumns ? currentSrcDS : ConfigReportContent.removeAuditColumns(currentSrcDS);
                        currentRefDS = includeAuditColumns ? currentRefDS : ConfigReportContent.removeAuditColumns(currentRefDS);
                        PropertyList detailTableProps = null;
                        if (!dsName.startsWith("sdi")) {
                            if (policyNodeProps != null) {
                                detaildatasetsProps = policyNodeProps.getCollection("detaildatasets");
                                detailTableProps = detaildatasetsProps.find("table", dsName);
                            } else {
                                Trace.logDebug("Did not find policy node for snapshot items");
                            }
                        } else if (policyNodeProps != null) {
                            detaildatasetsProps = policyNodeProps.getCollection("sdidetaildatasets");
                            detailTableProps = detaildatasetsProps.find("table", dsName);
                        } else {
                            Trace.logDebug("Did not find policy node for snapshot items");
                        }
                        ConfigReportContent linkidContent = new ConfigReportContent("detail", this.translationProcessor);
                        if (linkidContent.hasDiffDetailTables(currentSrcDS, currentRefDS, keyCols)) {
                            return true;
                        }
                    }
                    if (!hasGrandChildren) continue;
                    for (i = 0; i < srcChildSnapShotItems.size(); ++i) {
                        SDISnapshotItem srcItem = (SDISnapshotItem)srcChildSnapShotItems.get(i);
                        SDIData srcItemSDIData = srcItem.getSDIData();
                        if (srcItemSDIData == null) continue;
                        SDISnapshotItem refItem2 = this.findMatchingSnapshotItem(refChildSnapShotItems, srcItem);
                        if (refItem2 == null) {
                            return true;
                        }
                        if (!this.hasDiffSDISnapshotItem(srcItem, refItem2, includeAuditColumns)) continue;
                        return true;
                    }
                    for (i = 0; i < refChildSnapShotItems.size(); ++i) {
                        refItem = (SDISnapshotItem)refChildSnapShotItems.get(i);
                        SDIData refItemSDIData = refItem.getSDIData();
                        if (refItemSDIData == null) continue;
                        SDISnapshotItem srcItem = this.findMatchingSnapshotItem(srcChildSnapShotItems, refItem);
                        if (srcItem == null) {
                            return true;
                        }
                        if (!this.hasDiffSDISnapshotItem(srcItem, refItem, includeAuditColumns)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected ConfigReportContent renderPrimary(SDISnapshotItem sdiSnapshotItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, String status, boolean isChild) throws SapphireException {
        if ("New".equals(status)) {
            return this.renderPrimaryDiff(sdiSnapshotItem, null, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild);
        }
        if ("Deleted".equals(status)) {
            return this.renderPrimaryDiff(null, sdiSnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild);
        }
        return this.renderPrimaryDiff(sdiSnapshotItem, sdiSnapshotItem, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild);
    }

    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        return this.renderPrimaryDiff(source, ref, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, false, isChild);
    }

    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean ignoreClobs, boolean isChild) throws SapphireException {
        String currColumn;
        String labelformat;
        ConfigReportContent configReportContent = new ConfigReportContent("primary", this.translationProcessor);
        if (sourceItem == null && refItem == null) {
            throw new SapphireException("Both source and ref cannot be null");
        }
        SDIData srcSDIData = new SDIData();
        SDIData refSDIData = new SDIData();
        SDIData nonEmptySDIData = new SDIData();
        String sdcid = "";
        String sdiTitle = "";
        ArrayList columns = new ArrayList();
        ArrayList columnLabels = new ArrayList();
        if (sourceItem != null) {
            nonEmptySDIData = srcSDIData = sourceItem.getSDIData();
            sdiTitle = SDISnapshotViewer.getSDI(srcSDIData).toString();
            sdcid = sourceItem.getSDCId();
            labelformat = this.getSDITableLabelInfo(sdcid)[1];
            if (labelformat != null && labelformat.length() > 0) {
                sdiTitle = this.getFormattedItemLabel(srcSDIData.getDataset("primary"), labelformat, sdiTitle);
            }
            columns = SDISnapshotViewer.getPrimaryColumnsList(srcSDIData, showAuditColumns);
            columnLabels = SDISnapshotViewer.getPrimaryColumnLabels(this.sdcProcessor, srcSDIData, showAuditColumns);
        }
        if (refItem != null) {
            nonEmptySDIData = refSDIData = refItem.getSDIData();
            sdiTitle = SDISnapshotViewer.getSDI(refSDIData).toString();
            sdcid = refItem.getSDCId();
            labelformat = this.getSDITableLabelInfo(sdcid)[1];
            if (labelformat != null && labelformat.length() > 0) {
                sdiTitle = this.getFormattedItemLabel(refSDIData.getDataset("primary"), labelformat, sdiTitle);
            }
            columns = SDISnapshotViewer.getPrimaryColumnsList(refSDIData, showAuditColumns);
            columnLabels = SDISnapshotViewer.getPrimaryColumnLabels(this.sdcProcessor, refSDIData, showAuditColumns);
        }
        if (isChild) {
            configReportContent.startSubSection(sdiTitle, "");
        } else {
            configReportContent.startSection(sdiTitle);
        }
        configReportContent.startTable();
        int i = 0;
        int currrowitems = 0;
        while (i < columns.size()) {
            if (currrowitems == 2) {
                currrowitems = 0;
                configReportContent.startRow();
            }
            currColumn = (String)columns.get(i);
            if (this.isClob(nonEmptySDIData, columns.get(i).toString())) {
                ++i;
            } else {
                String value = this.getPrimaryValue(srcSDIData, currColumn);
                String currlabel = columnLabels.get(i).toString();
                if (showTranslation) {
                    currlabel = this.translationProcessor.translate(currlabel);
                }
                ConfigReportContent rowitem = new ConfigReportContent("rowitem", this.translationProcessor);
                rowitem.addDiffRowItem(currlabel, value, this.getPrimaryValue(refItem == null ? new SDIData() : refItem.getSDIData(), currColumn), 1, false, this.translationProcessor, hideEmptyColumns);
                if (!hideEmptyColumns || rowitem.length() != 0) {
                    configReportContent.append(rowitem.toString());
                    ++currrowitems;
                }
                ++i;
            }
            if (currrowitems != 2) continue;
            configReportContent.endRow();
        }
        configReportContent.endTable();
        if (!ignoreClobs) {
            for (i = 0; i < columns.size(); ++i) {
                String diffval;
                currColumn = (String)columns.get(i);
                if (!this.isClob(nonEmptySDIData, columns.get(i).toString())) continue;
                String sourcevalue = this.getPrimaryValue(srcSDIData, currColumn);
                String refvalue = this.getPrimaryValue(refItem == null ? new SDIData() : refItem.getSDIData(), currColumn);
                String currlabel = columnLabels.get(i).toString();
                if (sourcevalue.length() <= 0 && refvalue.length() <= 0) continue;
                if (showTranslation) {
                    currlabel = this.translationProcessor.translate(currlabel);
                }
                if ((diffval = configReportContent.getFormattedDiffVal(currColumn, sourcevalue, refvalue, true, this.translationProcessor)).length() > 0) {
                    configReportContent.startSubSection(currlabel, "");
                    configReportContent.append("<table>");
                    configReportContent.append("<tr>");
                    configReportContent.append("<td>");
                    configReportContent.append(diffval);
                    configReportContent.append("</td>");
                    configReportContent.append("</tr>");
                    configReportContent.append("</table>");
                    continue;
                }
                configReportContent.startSubSection(currlabel, "");
                configReportContent.startTable();
                configReportContent.append("<tr><td>No contents</td></tr>");
                configReportContent.endTable();
            }
        }
        return configReportContent;
    }

    private boolean hasDiffPrimary(SDISnapshotItem source, SDISnapshotItem ref, boolean includeAuditColumns) throws SapphireException {
        return this.hasDiffPrimary(source, ref, includeAuditColumns, null);
    }

    private boolean hasDiffPrimary(SDISnapshotItem source, SDISnapshotItem ref, boolean includeAuditColumns, Set<String> ignoreColList) throws SapphireException {
        ArrayList columns = SDISnapshotViewer.getPrimaryColumnsList(source.getSDIData(), includeAuditColumns, ignoreColList);
        for (int i = 0; i < columns.size(); ++i) {
            String currColumn = (String)columns.get(i);
            String value = this.getPrimaryValue(source.getSDIData(), currColumn);
            if (value.equals(this.getPrimaryValue(ref.getSDIData(), currColumn))) continue;
            return true;
        }
        return false;
    }

    public String getPrimaryValue(SDIData sdiData, String columnName) {
        String value = "";
        if (sdiData.getDataset("primary") != null) {
            if (columnName.startsWith("(")) {
                return "[[queried data]]";
            }
            value = sdiData.getDataset("primary").getValue(0, columnName);
            if (value != null && value.length() > 0) {
                value = this.getLinkedRefTypeValue(sdiData.getSdcid(), columnName, value);
            }
        }
        return value;
    }

    public String getPrimaryValue(SDIData sdiData, String columnName, String defaultvalue) {
        String value = "";
        if (sdiData.getDataset("primary") != null) {
            if (columnName.startsWith("(")) {
                return "[[queried data]]";
            }
            value = sdiData.getDataset("primary").getValue(0, columnName, defaultvalue);
            if (value != null && value.length() > 0) {
                value = this.getLinkedRefTypeValue(sdiData.getSdcid(), columnName, value);
            }
        }
        return value;
    }

    public boolean isClob(SDIData sdiData, String columnName) {
        String type = this.sdcProcessor.getSDCColumnProperty(sdiData.getSdcid(), columnName, "datatype");
        return type.equals("T");
    }

    public String getLinkedRefTypeValue(String sdcid, String columnid, String value) {
        DataSet linksData = this.sdcProcessor.getLinksData(sdcid);
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdccolumnid", columnid);
        filter.put("linksdcid", "RefType");
        DataSet match = linksData.getFilteredDataSet(filter);
        if (match.getRowCount() > 0 && match.getString(0, "reftypeid", "").length() > 0) {
            String reftypeid = match.getString(0, "reftypeid");
            String sql = "SELECT refvalueid, refdisplayvalue FROM refvalue where reftypeid=? and refvalueid=?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(reftypeid);
            safeSQL.addVar(value);
            DataSet refvals = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (refvals.getRowCount() > 0 && refvals.getValue(0, "refdisplayvalue", "").length() > 0) {
                return refvals.getValue(0, "refdisplayvalue", "");
            }
        }
        return value;
    }

    public static ArrayList getPrimaryColumnsList(SDIData sdiData, boolean showAuditColumns) {
        return SDISnapshotViewer.getPrimaryColumnsList(sdiData, showAuditColumns, null);
    }

    public static ArrayList getPrimaryColumnsList(SDIData sdiData, boolean showAuditColumns, Set<String> ignoreColList) {
        String[] cols = sdiData.getDataset("primary").getColumns();
        ArrayList<String> ret = new ArrayList<String>();
        boolean j = false;
        for (int i = 0; i < cols.length; ++i) {
            if (cols[i].startsWith("__") || !showAuditColumns && (cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("usersequence") || cols[i].equals("tracelogid")) || ignoreColList != null && ignoreColList.contains(cols[i].toLowerCase())) continue;
            ret.add(cols[i]);
        }
        return ret;
    }

    public static ArrayList getPrimaryColumnLabels(SDCProcessor sdcProcessor, SDIData sdiData, boolean showAuditColumns) {
        ArrayList cols = SDISnapshotViewer.getPrimaryColumnsList(sdiData, showAuditColumns);
        ArrayList<String> ret = new ArrayList<String>();
        DataSet columnData = sdcProcessor.getColumnData(sdiData.getSdcid());
        for (int i = 0; i < cols.size(); ++i) {
            DataSet currCol;
            HashMap filter = new HashMap();
            filter.put("columnid", cols.get(i));
            String currLabel = cols.get(i).toString();
            if (columnData != null && (currCol = columnData.getFilteredDataSet(filter)).getRowCount() > 0) {
                currLabel = currCol.getString(0, "columnlabel", cols.get(i).toString());
            }
            ret.add(currLabel);
        }
        return ret;
    }

    private ArrayList<String> evaluateGroupIds(ArrayList<String> groupids, DataSet primary, PropertyList elementProps) throws SapphireException {
        String[] cols;
        ArrayList<String> ret = new ArrayList<String>();
        HashMap<String, String> primaryValues = new HashMap<String, String>();
        for (String col : cols = primary.getColumns()) {
            primaryValues.put(col, primary.getValue(0, col, ""));
        }
        HashMap bindings = new HashMap();
        bindings.put("primary", primaryValues);
        HashMap<String, PropertyValue> elementValues = new HashMap<String, PropertyValue>();
        Object[] names = elementProps.keySet().toArray();
        for (Object name : names) {
            elementValues.put((String)name, elementProps.getPropertyValue((String)name));
        }
        bindings.put("element", elementValues);
        for (String currId : groupids) {
            if (currId.startsWith("$G")) {
                currId = GroovyUtil.evaluate(currId, bindings);
            }
            if (ret.contains(currId)) continue;
            ret.add(currId);
        }
        return ret;
    }

    private String getLinkIdFromTableid(String sdcid, String tableid) throws SapphireException {
        String sql = "select linkid from sdclink where sdcid =? and linktableid=?";
        QueryProcessor querpProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        DataSet ds = querpProcessor.getPreparedSqlDataSet(sql, new Object[]{sdcid, tableid});
        if (ds != null && ds.getRowCount() == 1) {
            return ds.getString(0, "linkid");
        }
        return "";
    }

    public PropertyDefinitionList getPropertyDefinitionTree(QueryProcessor queryProcessor, String propertytreeid) throws SapphireException {
        PropertyTree currentPropertyTree = new PropertyTree();
        String sql = "SELECT * from propertytree where propertytreeid=?";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{propertytreeid}, true);
        if (ds != null && ds.getValue(0, "definitiontree", "").length() > 0) {
            currentPropertyTree.setDefinitionXML(ds.getValue(0, "definitiontree", ""));
        }
        return currentPropertyTree.getPropertyDefinitionList();
    }

    public static HashMap<String, PropertyList> getOverridingPropertyTrees(DataSet wp, int row) throws SapphireException {
        HashMap<String, PropertyList> ret = new HashMap<String, PropertyList>();
        if (wp == null) {
            return ret;
        }
        String xml = wp.getValue(row, "productvaluetree", "");
        PropertyList pv = new PropertyList();
        PropertyList v = new PropertyList();
        PropertyList c = new PropertyList();
        if (xml.length() > 0) {
            pv.setPropertyList(xml);
        }
        if ((xml = wp.getValue(row, "valuetree", "")).length() > 0) {
            v.setPropertyList(xml);
        }
        if ((xml = wp.getValue(row, "componentvaluetree", "")).length() > 0) {
            c.setPropertyList(xml);
        }
        ret.put("product", pv);
        ret.put("custom", v);
        ret.put("component", c);
        return ret;
    }

    public static ConfigReportContent renderOverriddenPropertyList(boolean isDevMode, boolean isCompMode, String connectionid, String title, String propertytreeid, String srcextendnodeid, String refextendnodeid, HashMap<String, PropertyList> srcProperties, HashMap<String, PropertyList> refProperties, boolean hideInheritedProperties, TranslationProcessor translationProcessor, boolean hideEmptyColumns) throws Exception {
        try {
            PropertyList collapsedRefPropertyList;
            PropertyList collapsedSrcPropertyList;
            ConfigReportContent configReportContent = new ConfigReportContent("Override PTree", translationProcessor);
            WebAdminProcessor webadminProcessor = new WebAdminProcessor(connectionid);
            PropertyTree srcpropertyTree = webadminProcessor.getPropertyTree(propertytreeid);
            PropertyDefinitionList pd = srcpropertyTree.getPropertyDefinitionList();
            Node srcextendednode = srcpropertyTree.getNode(srcextendnodeid);
            Node srcproductnode = srcpropertyTree.createNode("_productvaluetree", srcextendednode);
            srcproductnode.setPropertyList(srcProperties.get("product"));
            if (!isDevMode) {
                if (isCompMode) {
                    Node compnode = srcpropertyTree.createNode("_componentvaluetree", srcproductnode);
                    compnode.setPropertyList(srcProperties.get("component"));
                    collapsedSrcPropertyList = srcpropertyTree.getNodePropertyList("_componentvaluetree", true);
                } else {
                    Node customnode = srcpropertyTree.createNode("_valuetree", srcproductnode);
                    customnode.setPropertyList(srcProperties.get("custom"));
                    collapsedSrcPropertyList = srcpropertyTree.getNodePropertyList("_valuetree", true);
                }
            } else {
                collapsedSrcPropertyList = srcpropertyTree.getNodePropertyList("_productvaluetree", true);
            }
            PropertyTree refpropertyTree = webadminProcessor.getPropertyTree(propertytreeid);
            Node refextendednode = refpropertyTree.getNode(refextendnodeid);
            Node refproductnode = refpropertyTree.createNode("_productvaluetree", refextendednode);
            refproductnode.setPropertyList(refProperties.get("product"));
            if (!isDevMode) {
                if (isCompMode) {
                    Node compnode = refpropertyTree.createNode("_componentvaluetree", refproductnode);
                    compnode.setPropertyList(refProperties.get("component"));
                    collapsedRefPropertyList = refpropertyTree.getNodePropertyList("_componentvaluetree", true);
                } else {
                    Node customnode = refpropertyTree.createNode("_valuetree", refproductnode);
                    customnode.setPropertyList(refProperties.get("custom"));
                    collapsedRefPropertyList = refpropertyTree.getNodePropertyList("_valuetree", true);
                }
            } else {
                collapsedRefPropertyList = refpropertyTree.getNodePropertyList("_productvaluetree", true);
            }
            String basenode = "_productvaluetree";
            if (!isDevMode) {
                basenode = "_valuetree";
            }
            configReportContent.appendSubSection(configReportContent.renderOverridePropertyListDiff(basenode, collapsedSrcPropertyList, collapsedRefPropertyList, pd, true, true, hideInheritedProperties, new TranslationProcessor(connectionid), hideEmptyColumns), title, false);
            return configReportContent;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to fetch propertyTree");
        }
    }

    public String[] getIgnoreDataSets() {
        return null;
    }

    protected ConfigReportContent renderFieldObjectDiff(String srcfieldobject, String reffieldobject, TranslationProcessor translationProcessor, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent pages;
        ConfigReportContent elements;
        ConfigReportContent sections;
        ConfigReportContent groups;
        PropertyListCollection reffields;
        PropertyListCollection srcfields;
        ConfigReportContent section;
        ConfigReportContent ret = new ConfigReportContent("fieldobj", translationProcessor);
        PropertyList sourceFieldObjProps = new PropertyList();
        PropertyList refFieldObjProps = new PropertyList();
        if (srcfieldobject != null && srcfieldobject.length() > 0) {
            sourceFieldObjProps.setPropertyList(srcfieldobject);
        }
        if (reffieldobject != null && reffieldobject.length() > 0) {
            refFieldObjProps.setPropertyList(reffieldobject);
        }
        if ((section = this.getDataSourcesDiff(sourceFieldObjProps, refFieldObjProps, hideEmptyColumns)).length() > 0) {
            ret.startSubSection("Data Sources", "");
            ret.appendSubSection(section, "Data Sources");
        }
        if ((section = this.renderFieldsDiff(srcfields = sourceFieldObjProps.getCollectionNotNull("fields"), reffields = refFieldObjProps.getCollectionNotNull("fields"), translationProcessor, hideEmptyColumns)).length() > 0) {
            ret.startSubSection("Fields", "");
            ret.appendSubSection(section, "Fields");
        }
        if ((groups = this.getGroupsDiff(sourceFieldObjProps.getCollectionNotNull("groups"), refFieldObjProps.getCollectionNotNull("groups"), hideEmptyColumns)).length() > 0) {
            ret.startSubSection("Groups", "");
            ret.appendSubSection(groups, "Groups");
        }
        if ((sections = this.getSectionsDiff(sourceFieldObjProps.getCollectionNotNull("sections"), refFieldObjProps.getCollectionNotNull("sections"), hideEmptyColumns)).length() > 0) {
            ret.startSubSection("sections", "");
            ret.appendSubSection(sections, "Sections");
        }
        if ((elements = this.getElementsDiff(sourceFieldObjProps.getCollectionNotNull("elements"), refFieldObjProps.getCollectionNotNull("elements"), hideEmptyColumns)).length() > 0) {
            ret.startSubSection("elements", "");
            ret.appendSubSection(elements, "Elements");
        }
        if ((pages = this.getPagesDiff(sourceFieldObjProps.getCollectionNotNull("pages"), refFieldObjProps.getCollectionNotNull("pages"), hideEmptyColumns)).length() > 0) {
            ret.startSubSection("pages", "");
            ret.appendSubSection(pages, "Pages");
        }
        return ret;
    }

    private ConfigReportContent renderFieldsDiff(PropertyListCollection srcfields, PropertyListCollection reffields, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        ConfigReportContent allFields = new ConfigReportContent("allfields", translationProcessor);
        for (int i = 0; i < srcfields.size(); ++i) {
            ConfigReportContent misc;
            ConfigReportContent validation;
            ConfigReportContent lookup;
            ConfigReportContent data;
            ConfigReportContent content = new ConfigReportContent("currentfield", translationProcessor);
            PropertyList currentSrcfield = srcfields.getPropertyList(i);
            String currentfieldid = currentSrcfield.getProperty("fieldid");
            PropertyList currentRefField = reffields.find("fieldid", currentfieldid);
            String currentfieldtitle = currentSrcfield.getProperty("fieldtitle", currentfieldid);
            ConfigReportContent behavior = this.getFieldDetailsDiff("Behavior", currentSrcfield, currentRefField, hideEmptyColumns);
            content.startSubHeading(currentfieldtitle, "");
            content.startTable();
            if (behavior.length() > 0) {
                content.startRow();
                content.addRowItem("Behavior", behavior.toString());
                content.endRow();
            }
            if ((data = this.getFieldDetailsDiff("Data", currentSrcfield, currentRefField, hideEmptyColumns)).length() > 0) {
                content.startRow();
                content.addRowItem("Data", data.toString());
                content.endRow();
            }
            if ((lookup = this.getFieldDetailsDiff("Lookup", currentSrcfield, currentRefField, hideEmptyColumns)).length() > 0) {
                content.startRow();
                content.addRowItem("Lookup", lookup.toString());
                content.endRow();
            }
            if ((validation = this.getFieldDetailsDiff("Validation", currentSrcfield, currentRefField, hideEmptyColumns)).length() > 0) {
                content.startRow();
                content.addRowItem("Validation", validation.toString());
                content.endRow();
            }
            if ((misc = this.getFieldDetailsDiff("Misc", currentSrcfield, currentRefField, hideEmptyColumns)).length() > 0) {
                content.startRow();
                content.addRowItem("Misc", misc.toString());
                content.endRow();
            }
            content.endTable();
            allFields.appendSpecialContent(content);
        }
        return allFields;
    }

    private ConfigReportContent getFieldDetailsDiff(String group, PropertyList field, PropertyList reffield, boolean hideEmptyColumns) {
        if (field == null) {
            field = new PropertyList();
        }
        if (reffield == null) {
            reffield = new PropertyList();
        }
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        if (group.equals("Behavior")) {
            String refreadonly;
            ds.addColumn("Id", 0);
            ds.addColumn("Title", 0);
            ds.addColumn("Field Type", 0);
            ds.addColumn("Editor Style ID", 0);
            ds.addColumn("Data Type", 0);
            ds.addColumn("Mandatory", 0);
            ds.addColumn("Visible", 0);
            ds.addColumn("Read Only", 0);
            ds.addRow();
            ds.setString(0, "Id", ConfigReportContent.getDiffString(field.getProperty("fieldid", ""), reffield.getProperty("fieldid", "")));
            ds.setString(0, "Title", ConfigReportContent.getDiffString(field.getProperty("title", ""), reffield.getProperty("title", "")));
            ds.setString(0, "Field Type", ConfigReportContent.getDiffString(field.getProperty("type", ""), reffield.getProperty("type", "")));
            ds.setString(0, "Editor Style ID", ConfigReportContent.getDiffString(field.getProperty("editorstyleid", ""), reffield.getProperty("editorstyleid", "")));
            ds.setString(0, "Data Type", ConfigReportContent.getDiffString(field.getProperty("datatype", ""), reffield.getProperty("datatype", "")));
            ds.setString(0, "Mandatory", ConfigReportContent.getDiffString(field.getProperty("mandatory", ""), reffield.getProperty("mandatory", "")));
            ds.setString(0, "Visible", ConfigReportContent.getDiffString(field.getProperty("visible", ""), reffield.getProperty("visible", "")));
            String readonly = field.getProperty("readonly", "");
            if (readonly.equals("C")) {
                readonly = "On Creation";
            }
            if ((refreadonly = reffield.getProperty("readonly", "")).equals("C")) {
                refreadonly = "On Creation";
            }
            ds.setString(0, "Read Only", ConfigReportContent.getDiffString(readonly, refreadonly));
        } else if (group.equals("Data")) {
            ds.addColumn("Default Value", 0);
            ds.addColumn("SDC Id", 0);
            ds.addColumn("SDC Restrictive From", 0);
            ds.addColumn("SDC Restrictive Where", 0);
            ds.addColumn("Ref Type Id", 0);
            ds.addColumn("SQL", 0);
            ds.addColumn("Values", 0);
            ds.addRow();
            ds.setString(0, "Default Value", ConfigReportContent.getDiffString(field.getProperty("defaultvalue", ""), reffield.getProperty("defaultvalue", "")));
            ds.setString(0, "SDC Id", ConfigReportContent.getDiffString(field.getProperty("sdcid", ""), reffield.getProperty("sdcid", "")));
            ds.setString(0, "SDC Restrictive From", ConfigReportContent.getDiffString(field.getProperty("valuesqueryfrom", ""), reffield.getProperty("valuesqueryfrom", "")));
            ds.setString(0, "SDC Restrictive Where", ConfigReportContent.getDiffString(field.getProperty("valuesquerywhere", ""), reffield.getProperty("valuesquerywhere", "")));
            ds.setString(0, "Ref Type Id", ConfigReportContent.getDiffString(field.getProperty("reftypeid", ""), reffield.getProperty("reftypeid", "")));
            ds.setString(0, "SQL", ConfigReportContent.getDiffString(field.getProperty("sql", ""), reffield.getProperty("sql", "")));
            ds.setString(0, "Values", ConfigReportContent.getDiffString(field.getProperty("values", ""), reffield.getProperty("values", "")));
        } else if (group.equals("Lookup")) {
            if (field.getProperty("type").equals("lookup")) {
                ds.addColumn("Lookup Page", 0);
                ds.addColumn("Callback", 0);
                ds.addColumn("Selector Type", 0);
                ds.addColumn("Lookup Columns", 0);
                ds.addRow();
                ds.setString(0, "Lookup Page", ConfigReportContent.getDiffString(field.getProperty("lookuppageid", ""), reffield.getProperty("lookuppageid", "")));
                ds.setString(0, "Callback", ConfigReportContent.getDiffString(field.getProperty("lookupcallback", ""), reffield.getProperty("lookupcallback", "")));
                ds.setString(0, "Selector Type", ConfigReportContent.getDiffString(field.getProperty("lookupselectortype", ""), reffield.getProperty("lookupselectortype", "")));
                PropertyListCollection srclookupColumns = field.getCollectionNotNull("lookupcolumns");
                PropertyListCollection reflookupColumns = reffield.getCollectionNotNull("lookupcolumns");
                ConfigReportContent lookupColumnsContent = new ConfigReportContent("lookup", this.translationProcessor);
                lookupColumnsContent = lookupColumnsContent.renderCollectionDiff(srclookupColumns, reflookupColumns, false, this.translationProcessor, hideEmptyColumns);
                ds.setString(0, "Lookup Columns", lookupColumnsContent.toString());
            }
        } else if (group.equals("Validation")) {
            ds.addColumn("Simple Validation", 0);
            ds.addColumn("Script Validation", 0);
            ds.addColumn("Pass Condition", 0);
            ds.addColumn("Message", 0);
            ds.addColumn("Enabled", 0);
            ds.addColumn("Auto Check", 0);
            ds.addColumn("Key Id1 Field", 0);
            ds.addColumn("Key Id2 Field", 0);
            ds.addColumn("Key Id3 Field", 0);
            ds.addRow();
            PropertyListCollection validation = field.getCollectionNotNull("validation");
            PropertyListCollection refvalidation = reffield.getCollectionNotNull("validation");
            ConfigReportContent validationcontent = new ConfigReportContent("validation", this.translationProcessor);
            validationcontent = validationcontent.renderCollectionDiff(validation, refvalidation, false, this.translationProcessor, hideEmptyColumns);
            ds.setString(0, "Simple Validation", validationcontent.toString());
            PropertyList srccomplexvalidation = field.getPropertyListNotNull("complexvalidation");
            PropertyList refcomplexvalidation = reffield.getPropertyListNotNull("complexvalidation");
            ds.setString(0, "Script Validation", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("scriptvalidation", ""), refcomplexvalidation.getProperty("scriptvalidation", "")));
            ds.setString(0, "Pass Condition", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("passcondition", ""), refcomplexvalidation.getProperty("passcondition", "")));
            ds.setString(0, "Message", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("message", ""), refcomplexvalidation.getProperty("message", "")));
            ds.setString(0, "Enabled", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("enabled", ""), refcomplexvalidation.getProperty("enabled", "")));
            ds.setString(0, "Auto Check", ConfigReportContent.getDiffString(field.getProperty("autocheck", ""), reffield.getProperty("autocheck", "")));
            ds.setString(0, "Key Id1 Field", ConfigReportContent.getDiffString(field.getProperty("keyid1", ""), reffield.getProperty("keyid1", "")));
            ds.setString(0, "Key Id2 Field", ConfigReportContent.getDiffString(field.getProperty("keyid2", ""), reffield.getProperty("keyid2", "")));
            ds.setString(0, "Key Id3 Field", ConfigReportContent.getDiffString(field.getProperty("keyid3", ""), reffield.getProperty("keyid3", "")));
        } else if (group.equals("Misc")) {
            String refsave;
            ds.addColumn("Instrument Type", 0);
            ds.addColumn("Instrument Id", 0);
            ds.addColumn("Identitiy Field", 0);
            ds.addColumn("Auto Link", 0);
            ds.addColumn("Auto Attach", 0);
            ds.addColumn("Processing Field", 0);
            ds.addColumn("Save Field Value", 0);
            ds.addColumn("Attributes", 0);
            ds.addColumn("Show Toolbar", 0);
            ds.addRow();
            ds.setString(0, "Instrument Type", ConfigReportContent.getDiffString(field.getProperty("instrumenttypeid", ""), reffield.getProperty("instrumenttypeid", "")));
            ds.setString(0, "Instrument Id", ConfigReportContent.getDiffString(field.getProperty("instrumentid", ""), reffield.getProperty("instrumentid", "")));
            ds.setString(0, "Identity Field", ConfigReportContent.getDiffString(field.getProperty("identityfield", ""), reffield.getProperty("identityfield", "")));
            ds.setString(0, "Auto Link", ConfigReportContent.getDiffString(field.getProperty("autolink", ""), reffield.getProperty("autolink", "")));
            ds.setString(0, "Auto Attach", ConfigReportContent.getDiffString(field.getProperty("autoattach", ""), reffield.getProperty("autoattach", "")));
            ds.setString(0, "Processing Field", ConfigReportContent.getDiffString(field.getProperty("processingfield", ""), reffield.getProperty("processingfield", "")));
            String srcsave = field.getProperty("save", "");
            if (srcsave.equals("E")) {
                srcsave = "If Enabled";
            }
            if (srcsave.equals("V")) {
                srcsave = "If Visible";
            }
            if ((refsave = reffield.getProperty("save", "")).equals("E")) {
                refsave = "If Enabled";
            }
            if (refsave.equals("V")) {
                refsave = "If Visible";
            }
            ds.setString(0, "Save Field Value", ConfigReportContent.getDiffString(srcsave, refsave));
            PropertyListCollection srcAttributes = field.getCollectionNotNull("attributes");
            PropertyListCollection refAttributes = reffield.getCollectionNotNull("attributes");
            ConfigReportContent attributes = new ConfigReportContent("attributes", this.translationProcessor);
            attributes = attributes.renderCollectionDiff(srcAttributes, refAttributes, false, this.translationProcessor, hideEmptyColumns);
            ds.setString(0, "Attributes", attributes.toString());
            ds.setString(0, "Show Toolbar", ConfigReportContent.getDiffString(field.getProperty("showtoolbar", ""), reffield.getProperty("showtoolbar", "")));
        }
        ConfigReportContent content = new ConfigReportContent("ds", this.translationProcessor);
        content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
        return content;
    }

    private ConfigReportContent getDataSourcesDiff(PropertyList fieldObj, PropertyList refFieldObj, boolean hideEmptyColumns) {
        if (fieldObj == null) {
            fieldObj = new PropertyList();
        }
        if (refFieldObj == null) {
            refFieldObj = new PropertyList();
        }
        PropertyListCollection dataSources = fieldObj.getCollectionNotNull("datasources");
        PropertyListCollection refDataSources = refFieldObj.getCollectionNotNull("datasources");
        ConfigReportContent allDS = new ConfigReportContent("all datasources", this.translationProcessor);
        for (int i = 0; i < dataSources.size(); ++i) {
            ConfigReportContent datasetcontent;
            PropertyList refdatasetProps;
            PropertyList datasetProps;
            DataSet dataset;
            PropertyList srcDataSource = dataSources.getPropertyList(i);
            PropertyList refDataSource = refDataSources.find("datasourceid", srcDataSource.getProperty("datasourceid"));
            if (refDataSource == null) {
                refDataSource = new PropertyList();
            }
            DataSet source = new DataSet();
            source.setColidCaseSensitive(true);
            allDS.startSubHeading(srcDataSource.getProperty("datasourceid", ""), "");
            source.addColumn("Id", 0);
            source.addColumn("Type", 0);
            source.addRow();
            source.setString(0, "Id", ConfigReportContent.getDiffString(srcDataSource.getProperty("datasourceid", ""), refDataSource.getProperty("datasourceid", "")));
            source.setString(0, "Type", ConfigReportContent.getDiffString(srcDataSource.getProperty("type", ""), refDataSource.getProperty("type", "")));
            ConfigReportContent sourceContent = new ConfigReportContent("source", this.translationProcessor);
            sourceContent.renderListTable(source, false, this.translationProcessor, "", hideEmptyColumns);
            allDS.startTable();
            allDS.startRow();
            allDS.addRowItem("Source", sourceContent.toString(), hideEmptyColumns);
            allDS.endRow();
            if (srcDataSource.getPropertyList("sdi") != null) {
                DataSet sdi = new DataSet();
                sdi.setColidCaseSensitive(true);
                PropertyList sdiProps = srcDataSource.getPropertyListNotNull("sdi");
                PropertyList refsdiProps = refDataSource.getPropertyListNotNull("sdi");
                sdi.addRow();
                sdi.setString(0, "SDC Id", ConfigReportContent.getDiffString(sdiProps.getProperty("sdcid", ""), refsdiProps.getProperty("sdcid", "")));
                sdi.setString(0, "KeyId 1", ConfigReportContent.getDiffString(sdiProps.getProperty("keyid1", ""), refsdiProps.getProperty("keyid1", "")));
                sdi.setString(0, "KeyId 2", ConfigReportContent.getDiffString(sdiProps.getProperty("keyid2", ""), refsdiProps.getProperty("keyid2", "")));
                sdi.setString(0, "KeyId 3", ConfigReportContent.getDiffString(sdiProps.getProperty("keyid3", ""), refsdiProps.getProperty("keyid3", "")));
                sdi.setString(0, "Query From", ConfigReportContent.getDiffString(sdiProps.getProperty("queryfrom", ""), refsdiProps.getProperty("queryfrom", "")));
                sdi.setString(0, "Query Where", ConfigReportContent.getDiffString(sdiProps.getProperty("querywhere", ""), refsdiProps.getProperty("querywhere", "")));
                ConfigReportContent sdicontent = new ConfigReportContent("sdiprops", this.translationProcessor);
                sdicontent.renderListTable(sdi, false, this.translationProcessor, "", hideEmptyColumns);
                allDS.startRow();
                allDS.addRowItem("SDI", sdicontent.toString(), hideEmptyColumns);
                allDS.endRow();
            }
            if (srcDataSource.getPropertyList("dataset") != null) {
                dataset = new DataSet();
                dataset.setColidCaseSensitive(true);
                datasetProps = srcDataSource.getPropertyListNotNull("dataset");
                refdatasetProps = refDataSource.getPropertyListNotNull("dataset");
                dataset.addRow();
                dataset.setString(0, "SDC Id", ConfigReportContent.getDiffString(datasetProps.getProperty("sdcid", ""), refdatasetProps.getProperty("sdcid", "")));
                dataset.setString(0, "KeyId 1 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid1", ""), refdatasetProps.getProperty("keyid1", "")));
                dataset.setString(0, "KeyId 2 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid2", ""), refdatasetProps.getProperty("keyid2", "")));
                dataset.setString(0, "KeyId 3 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid3", ""), refdatasetProps.getProperty("keyid3", "")));
                dataset.setString(0, "ParamList Id", ConfigReportContent.getDiffString(datasetProps.getProperty("paramlistid", ""), refdatasetProps.getProperty("paramlistid", "")));
                dataset.setString(0, "Version", ConfigReportContent.getDiffString(datasetProps.getProperty("paramlistversionid", ""), refdatasetProps.getProperty("paramlistversionid", "")));
                dataset.setString(0, "Variant", ConfigReportContent.getDiffString(datasetProps.getProperty("variantid", ""), refdatasetProps.getProperty("variantid", "")));
                dataset.setString(0, "DataSet", ConfigReportContent.getDiffString(datasetProps.getProperty("dataset", ""), refdatasetProps.getProperty("dataset", "")));
                datasetcontent = new ConfigReportContent("datasetprops", this.translationProcessor);
                datasetcontent.renderListTable(dataset, false, this.translationProcessor, "", hideEmptyColumns);
                allDS.startRow();
                allDS.addRowItem("Data Set", datasetcontent.toString(), hideEmptyColumns);
                allDS.endRow();
            }
            if (srcDataSource.getPropertyList("workitem") != null) {
                dataset = new DataSet();
                dataset.setColidCaseSensitive(true);
                datasetProps = srcDataSource.getPropertyListNotNull("workitem");
                refdatasetProps = refDataSource.getPropertyListNotNull("workitem");
                dataset.addRow();
                dataset.setString(0, "SDC Id", ConfigReportContent.getDiffString(datasetProps.getProperty("sdcid", ""), refdatasetProps.getProperty("sdcid", "")));
                dataset.setString(0, "KeyId 1 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid1", ""), refdatasetProps.getProperty("keyid1", "")));
                dataset.setString(0, "KeyId 2 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid2", ""), refdatasetProps.getProperty("keyid2", "")));
                dataset.setString(0, "KeyId 3 List", ConfigReportContent.getDiffString(datasetProps.getProperty("keyid3", ""), refdatasetProps.getProperty("keyid3", "")));
                dataset.setString(0, "WorkItem Id", ConfigReportContent.getDiffString(datasetProps.getProperty("workitemid", ""), refdatasetProps.getProperty("workitemid", "")));
                dataset.setString(0, "WorkItem Instance", ConfigReportContent.getDiffString(datasetProps.getProperty("workiteminstance", ""), refdatasetProps.getProperty("workiteminstance", "")));
                datasetcontent = new ConfigReportContent("workitemprops", this.translationProcessor);
                datasetcontent.renderListTable(dataset, false, this.translationProcessor, "", hideEmptyColumns);
                allDS.startRow();
                allDS.addRowItem("WorkItem", datasetcontent.toString(), hideEmptyColumns);
                allDS.endRow();
            }
            if (srcDataSource.getPropertyList("qcbatch") != null) {
                dataset = new DataSet();
                dataset.setColidCaseSensitive(true);
                datasetProps = srcDataSource.getPropertyListNotNull("qcbatch");
                refdatasetProps = refDataSource.getPropertyListNotNull("qcbatch");
                dataset.addRow();
                dataset.setString(0, "QCBatch Id", ConfigReportContent.getDiffString(datasetProps.getProperty("qcbatchid", ""), refdatasetProps.getProperty("qcbatchid", "")));
                datasetcontent = new ConfigReportContent("qcbatch", this.translationProcessor);
                datasetcontent.renderListTable(dataset, false, this.translationProcessor, "", hideEmptyColumns);
                allDS.startRow();
                allDS.addRowItem("QCBatch", datasetcontent.toString(), hideEmptyColumns);
                allDS.endRow();
            }
            DataSet statusmgmt = new DataSet();
            statusmgmt.setColidCaseSensitive(true);
            statusmgmt.addColumn("Auto Release Data", 0);
            statusmgmt.addColumn("Sync Primary Status", 0);
            statusmgmt.addColumn("Primary Status Column", 0);
            statusmgmt.addColumn("Sync AQC Status", 0);
            PropertyList srcStatusMgmt = srcDataSource.getPropertyListNotNull("statusmgmt");
            PropertyList refStatusMgmt = refDataSource.getPropertyListNotNull("statusmgmt");
            statusmgmt.addRow();
            statusmgmt.setString(0, "Auto Release Data", ConfigReportContent.getDiffString(srcStatusMgmt.getProperty("autorelease", ""), refStatusMgmt.getProperty("autorelease", "")));
            statusmgmt.setString(0, "Sync Primary Status", ConfigReportContent.getDiffString(srcStatusMgmt.getProperty("syncprimarystatus", ""), refStatusMgmt.getProperty("syncprimarystatus", "")));
            statusmgmt.setString(0, "Primary Status Column", ConfigReportContent.getDiffString(srcStatusMgmt.getProperty("primarystatuscolumn", ""), refStatusMgmt.getProperty("primarystatuscolumn", "")));
            statusmgmt.setString(0, "Sync AQC Status", ConfigReportContent.getDiffString(srcStatusMgmt.getProperty("syncaqcstatus", ""), refStatusMgmt.getProperty("syncaqcstatus", "")));
            ConfigReportContent statusmgmtContent = new ConfigReportContent("statusmgmt", this.translationProcessor);
            statusmgmtContent.renderListTable(statusmgmt, false, this.translationProcessor, "", hideEmptyColumns);
            allDS.startRow();
            allDS.addRowItem("Status Management", statusmgmtContent.toString(), hideEmptyColumns);
            allDS.endRow();
            allDS.endTable();
        }
        return allDS;
    }

    private ConfigReportContent getGroupsDiff(PropertyListCollection srcGroups, PropertyListCollection refGroups, boolean hideEmptyColumns) {
        ConfigReportContent allgroups = new ConfigReportContent("All groups", this.translationProcessor);
        for (int i = 0; i < srcGroups.size(); ++i) {
            String refreadonly;
            PropertyList srcgroup = srcGroups.getPropertyList(i);
            PropertyList refgroup = refGroups.find("groupid", srcgroup.getProperty("groupid", ""));
            allgroups.startSubHeading(srcgroup.getProperty("groupid", ""), "");
            allgroups.startTable();
            if (refgroup == null) {
                refgroup = new PropertyList();
            }
            DataSet ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("Id", 0);
            ds.addColumn("Title", 0);
            ds.addColumn("Read Only", 0);
            ds.addColumn("Visible", 0);
            ds.addColumn("Value Rule", 0);
            ds.addRow();
            ds.setString(0, "Id", ConfigReportContent.getDiffString(srcgroup.getProperty("groupid", ""), refgroup.getProperty("groupid", "")));
            ds.setString(0, "Title", ConfigReportContent.getDiffString(srcgroup.getProperty("title", ""), refgroup.getProperty("title", "")));
            ds.setString(0, "Visible", ConfigReportContent.getDiffString(srcgroup.getProperty("visible", ""), refgroup.getProperty("visible", "")));
            ds.setString(0, "Value Rule", ConfigReportContent.getDiffString(srcgroup.getProperty("valuerule", ""), refgroup.getProperty("valuerule", "")));
            String readonly = srcgroup.getProperty("readonly", "");
            if (readonly.equals("C")) {
                readonly = "On Creation";
            }
            if ((refreadonly = refgroup.getProperty("readonly", "")).equals("C")) {
                refreadonly = "On Creation";
            }
            ds.setString(0, "Read Only", ConfigReportContent.getDiffString(readonly, refreadonly));
            ConfigReportContent content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            if (content.length() > 0) {
                allgroups.startRow();
                allgroups.addRowItem("Behavior", content.toString(), hideEmptyColumns);
                allgroups.endRow();
            }
            ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("Simple Validation", 0);
            ds.addColumn("Script Validation", 0);
            ds.addColumn("Pass Condition", 0);
            ds.addColumn("Message", 0);
            ds.addColumn("Enabled", 0);
            ds.addRow();
            PropertyListCollection validation = srcgroup.getCollectionNotNull("validation");
            PropertyListCollection refvalidation = refgroup.getCollectionNotNull("validation");
            ConfigReportContent validationcontent = new ConfigReportContent("validation", this.translationProcessor);
            validationcontent = validationcontent.renderCollectionDiff(validation, refvalidation, false, this.translationProcessor, hideEmptyColumns);
            ds.setString(0, "Simple Validation", validationcontent.toString());
            PropertyList srccomplexvalidation = srcgroup.getPropertyListNotNull("complexvalidation");
            PropertyList refcomplexvalidation = refgroup.getPropertyListNotNull("complexvalidation");
            ds.setString(0, "Script Validation", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("scriptvalidation", ""), refcomplexvalidation.getProperty("scriptvalidation", "")));
            ds.setString(0, "Pass Condition", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("passcondition", ""), refcomplexvalidation.getProperty("passcondition", "")));
            ds.setString(0, "Message", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("message", ""), refcomplexvalidation.getProperty("message", "")));
            ds.setString(0, "Enabled", ConfigReportContent.getDiffString(srccomplexvalidation.getProperty("enabled", ""), refcomplexvalidation.getProperty("enabled", "")));
            content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            if (content.length() > 0) {
                allgroups.startRow();
                allgroups.addRowItem("Validation", content.toString(), hideEmptyColumns);
                allgroups.endRow();
            }
            allgroups.endTable();
        }
        return allgroups;
    }

    private ConfigReportContent getSectionsDiff(PropertyListCollection srcSections, PropertyListCollection refSections, boolean hideEmptyColumns) {
        ConfigReportContent allsections = new ConfigReportContent("All sections", this.translationProcessor);
        for (int i = 0; i < srcSections.size(); ++i) {
            PropertyList srcsection = srcSections.getPropertyList(i);
            PropertyList refsection = refSections.find("sectionid", srcsection.getProperty("sectionid", ""));
            allsections.startSubHeading(srcsection.getProperty("sectionid", ""), "");
            allsections.startTable();
            if (refsection == null) {
                refsection = new PropertyList();
            }
            DataSet ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("Id", 0);
            ds.addColumn("Repeatable", 0);
            ds.addColumn("Initial Repeats", 0);
            ds.addColumn("Max Repeats", 0);
            ds.addColumn("Max Repeats Message", 0);
            ds.addColumn("Locked", 0);
            ds.addColumn("Separator", 0);
            ds.addColumn("Read Only", 0);
            ds.addColumn("Visible", 0);
            ds.addColumn("Clear Fields", 0);
            ds.addRow();
            ds.setString(0, "Id", ConfigReportContent.getDiffString(srcsection.getProperty("sectionid", ""), refsection.getProperty("sectionid", "")));
            ds.setString(0, "Repeatable", ConfigReportContent.getDiffString(srcsection.getProperty("repeatable", ""), refsection.getProperty("repeatable", "")));
            ds.setString(0, "Initial Repeats", ConfigReportContent.getDiffString(srcsection.getProperty("initialrepeats", ""), refsection.getProperty("initialrepeats", "")));
            ds.setString(0, "Max Repeats", ConfigReportContent.getDiffString(srcsection.getProperty("maxrepeats", ""), refsection.getProperty("maxrepeats", "")));
            ds.setString(0, "Max Repeats Message", ConfigReportContent.getDiffString(srcsection.getProperty("maxrepeatsmessage", ""), refsection.getProperty("maxrepeatsmessage", "")));
            ds.setString(0, "Locked", ConfigReportContent.getDiffString(srcsection.getProperty("locked", ""), refsection.getProperty("locked", "")));
            ds.setString(0, "Separator", ConfigReportContent.getDiffString(srcsection.getProperty("separator", ""), refsection.getProperty("separator", "")));
            ds.setString(0, "Read Only", ConfigReportContent.getDiffString(srcsection.getProperty("readonly", ""), refsection.getProperty("readonly", "")));
            ds.setString(0, "Visible", ConfigReportContent.getDiffString(srcsection.getProperty("visible", ""), refsection.getProperty("visible", "")));
            ds.setString(0, "Clear Fields", ConfigReportContent.getDiffString(srcsection.getProperty("clearfields", ""), refsection.getProperty("clearfields", "")));
            ConfigReportContent content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            allsections.startRow();
            allsections.addRowItem("Behavior", content.toString(), hideEmptyColumns);
            allsections.endRow();
            ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("From Formlet", 0);
            ds.addColumn("From Formlet Version", 0);
            ds.addColumn("Locked", 0);
            ds.addColumn("By Reference", 0);
            ds.addRow();
            ds.setString(0, "From Formlet", ConfigReportContent.getDiffString(srcsection.getProperty("formlet", ""), refsection.getProperty("formlet", "")));
            ds.setString(0, "From Formlet Version", ConfigReportContent.getDiffString(srcsection.getProperty("formletversion", ""), refsection.getProperty("formletversion", "")));
            ds.setString(0, "Locked", ConfigReportContent.getDiffString(srcsection.getProperty("formletlocked", ""), refsection.getProperty("formletlocked", "")));
            ds.setString(0, "By Reference", ConfigReportContent.getDiffString(srcsection.getProperty("formletbyreference", ""), refsection.getProperty("formletbyreference", "")));
            content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            if (content.length() > 0) {
                allsections.startRow();
                allsections.addRowItem("Formlets", content.toString(), hideEmptyColumns);
                allsections.endRow();
            }
            ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("DataSource Id", 0);
            ds.addColumn("Section Repeater", 0);
            ds.addColumn("Parameter List Id", 0);
            ds.addColumn("Parameter List Version Id", 0);
            ds.addColumn("Variant Id", 0);
            ds.addColumn("DataSet", 0);
            ds.addColumn("Parameter Id", 0);
            ds.addColumn("Parameter Type", 0);
            ds.addColumn("Replicate Id", 0);
            ds.addColumn("Reagent Type Id", 0);
            ds.addColumn("Exclude Binding Values", 0);
            ds.addRow();
            ds.setString(0, "DataSource Id", ConfigReportContent.getDiffString(srcsection.getProperty("datasourceid", ""), refsection.getProperty("datasourceid", "")));
            ds.setString(0, "Section Repeater", ConfigReportContent.getDiffString(srcsection.getProperty("datasourcerepeater", ""), refsection.getProperty("datasourcerepeater", "")));
            PropertyList srcBinding = srcsection.getPropertyListNotNull("binding");
            PropertyList refBinding = refsection.getPropertyListNotNull("binding");
            ds.setString(0, "Parameter List Id", ConfigReportContent.getDiffString(srcBinding.getProperty("paramlistid", ""), refBinding.getProperty("paramlistid", "")));
            ds.setString(0, "Parameter List Version Id", ConfigReportContent.getDiffString(srcBinding.getProperty("paramlistversionid", ""), refBinding.getProperty("paramlistversionid", "")));
            ds.setString(0, "Variant Id", ConfigReportContent.getDiffString(srcBinding.getProperty("variantid", ""), refBinding.getProperty("variantid", "")));
            ds.setString(0, "DataSet", ConfigReportContent.getDiffString(srcBinding.getProperty("dataset", ""), refBinding.getProperty("dataset", "")));
            ds.setString(0, "Parameter Id", ConfigReportContent.getDiffString(srcBinding.getProperty("paramid", ""), refBinding.getProperty("paramid", "")));
            ds.setString(0, "Parameter Type", ConfigReportContent.getDiffString(srcBinding.getProperty("paramtype", ""), refBinding.getProperty("paramtype", "")));
            ds.setString(0, "Replicate Id", ConfigReportContent.getDiffString(srcBinding.getProperty("replicateid", ""), refBinding.getProperty("replicateid", "")));
            ds.setString(0, "Reagent Type Id", ConfigReportContent.getDiffString(srcBinding.getProperty("reagenttypeid", ""), refBinding.getProperty("reagenttypeid", "")));
            ds.setString(0, "Exclude Binding Values", ConfigReportContent.getDiffString(srcsection.getProperty("excludebindingvalues", ""), refsection.getProperty("excludebindingvalues", "")));
            content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            if (content.length() > 0) {
                allsections.startRow();
                allsections.addRowItem("Datasource Binding", content.toString(), hideEmptyColumns);
                allsections.endRow();
            }
            allsections.endTable();
        }
        return allsections;
    }

    private ConfigReportContent getPagesDiff(PropertyListCollection srcPages, PropertyListCollection refPages, boolean hideEmptyColumns) {
        ConfigReportContent allgroups = new ConfigReportContent("All groups", this.translationProcessor);
        for (int i = 0; i < srcPages.size(); ++i) {
            PropertyList srcpage = srcPages.getPropertyList(i);
            PropertyList refpage = refPages.find("pageid", srcpage.getProperty("pageid", ""));
            allgroups.startSubHeading(srcpage.getProperty("pageid", ""), "");
            allgroups.startTable();
            if (refpage == null) {
                refpage = new PropertyList();
            }
            DataSet ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("Id", 0);
            ds.addColumn("Title", 0);
            ds.addColumn("Mode", 0);
            ds.addColumn("Read Only", 0);
            ds.addColumn("Visible", 0);
            ds.addColumn("Clear Fields", 0);
            ds.addRow();
            ds.setString(0, "Id", ConfigReportContent.getDiffString(srcpage.getProperty("pageid", ""), refpage.getProperty("pageid", "")));
            ds.setString(0, "Title", ConfigReportContent.getDiffString(srcpage.getProperty("title", ""), refpage.getProperty("title", "")));
            ds.setString(0, "Mode", ConfigReportContent.getDiffString(srcpage.getProperty("mode", ""), refpage.getProperty("mode", "")));
            ds.setString(0, "Read Only", ConfigReportContent.getDiffString(srcpage.getProperty("readonly", ""), refpage.getProperty("readonly", "")));
            ds.setString(0, "Visible", ConfigReportContent.getDiffString(srcpage.getProperty("visible", ""), refpage.getProperty("visible", "")));
            ds.setString(0, "Clear Fields", ConfigReportContent.getDiffString(srcpage.getProperty("clearfields", ""), refpage.getProperty("clearfields", "")));
            ConfigReportContent content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            allgroups.startRow();
            allgroups.addRowItem("Layout", content.toString(), hideEmptyColumns);
            allgroups.endRow();
            allgroups.endTable();
        }
        return allgroups;
    }

    private ConfigReportContent getElementsDiff(PropertyListCollection srcElements, PropertyListCollection refElements, boolean hideEmptyColumns) {
        ConfigReportContent allelements = new ConfigReportContent("All elements", this.translationProcessor);
        for (int i = 0; i < srcElements.size(); ++i) {
            PropertyList srcpage = srcElements.getPropertyList(i);
            PropertyList refpage = refElements.find("elementid", srcpage.getProperty("elementid", ""));
            allelements.startSubHeading(srcpage.getProperty("elementid", ""), "");
            allelements.startTable();
            if (refpage == null) {
                refpage = new PropertyList();
            }
            DataSet ds = new DataSet();
            ds.setColidCaseSensitive(true);
            ds.addColumn("Id", 0);
            ds.addColumn("Element Type", 0);
            ds.addColumn("Read Only", 0);
            ds.addColumn("Visible", 0);
            ds.addColumn("Color", 0);
            ds.addColumn("Background", 0);
            ds.addColumn("Class", 0);
            ds.addRow();
            ds.setString(0, "Id", ConfigReportContent.getDiffString(srcpage.getProperty("elementid", ""), refpage.getProperty("pageid", "")));
            ds.setString(0, "Element Type", ConfigReportContent.getDiffString(srcpage.getProperty("type", ""), refpage.getProperty("title", "")));
            ds.setString(0, "Read Only", ConfigReportContent.getDiffString(srcpage.getProperty("readonly", ""), refpage.getProperty("readonly", "")));
            ds.setString(0, "Visible", ConfigReportContent.getDiffString(srcpage.getProperty("visible", ""), refpage.getProperty("visible", "")));
            ds.setString(0, "Color", ConfigReportContent.getDiffString(srcpage.getProperty("color", ""), refpage.getProperty("color", "")));
            ds.setString(0, "Background", ConfigReportContent.getDiffString(srcpage.getProperty("background", ""), refpage.getProperty("background", "")));
            ds.setString(0, "Class", ConfigReportContent.getDiffString(srcpage.getProperty("class", ""), refpage.getProperty("class", "")));
            ConfigReportContent content = new ConfigReportContent("ds", this.translationProcessor);
            content.renderListTable(ds, false, this.translationProcessor, "", hideEmptyColumns);
            allelements.startRow();
            allelements.addRowItem("Element", content.toString(), hideEmptyColumns);
            allelements.endRow();
            allelements.endTable();
        }
        return allelements;
    }

    public static boolean showCustomRendererOption(SapphireConnection sapphireConnection, String sdcid) {
        return sdcid.equals("LV_Worksheet") || SDISnapshotViewer.getCustomRenderer(sapphireConnection, sdcid) != null;
    }

    public static boolean showAuditColumnsOption(SapphireConnection sapphireConnection, String sdcid, String usecustomrenderer, SDISnapshot snapshot) {
        if (sdcid.equals("LV_Worksheet")) {
            if (usecustomrenderer.equals("N")) {
                return snapshot.hasAuditColumns();
            }
            return false;
        }
        if (SDISnapshotViewer.getCustomRenderer(sapphireConnection, sdcid) != null) {
            if (usecustomrenderer.equals("N")) {
                return snapshot.hasAuditColumns();
            }
            return false;
        }
        return snapshot.hasAuditColumns();
    }

    public static boolean showEmptyColumnsOption(SapphireConnection sapphireConnection, String sdcid, String usecustomrenderer) {
        if (sdcid.equals("LV_Worksheet")) {
            return usecustomrenderer.equals("N");
        }
        if (SDISnapshotViewer.getCustomRenderer(sapphireConnection, sdcid) != null) {
            return usecustomrenderer.equals("N");
        }
        return true;
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

        public void addChildItems(ArrayList<NodeItem> childItemList) throws JSONException {
            JSONArray childArray = new JSONArray();
            for (NodeItem item : childItemList) {
                childArray.put(item);
            }
            try {
                this.put("childitems", childArray);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        protected static String getNodeLabel(String nodeid, DataSet nodeInfo) {
            return NodeItem.getNodeLabel(nodeid, nodeInfo, "");
        }

        protected static String getNodeLabel(String nodeid, DataSet nodeInfo, String instatus) {
            int i = nodeInfo.findRow("nodeid", nodeid);
            if (i != -1) {
                String status = nodeInfo.getString(i, "status", "None");
                if (instatus.length() > 0) {
                    status = instatus;
                }
                String label = nodeInfo.getString(i, "nodelabel");
                return SDISnapshotViewer.getHeadingWithStatus(label, status);
            }
            return "";
        }

        public static NodeItem getSDINodeTree(SDISnapshot sdiSnapshot, SDISnapshotItem primaryItem, DataSet nodeInfo, TranslationProcessor translationProcessor) throws SapphireException, JSONException {
            SnapshotItem.LinkType[] linkTypes;
            String primarynodeid = primaryItem.toString();
            String label = NodeItem.getNodeLabel(primarynodeid, nodeInfo);
            if (label.length() == 0) {
                label = ConfigReportContent.generateSDISectionTitle(SDISnapshotViewer.getSDI(primaryItem.getSDIData()));
            }
            NodeItem nodeItem = new NodeItem(primarynodeid, label, false);
            ArrayList<NodeItem> childitemList = new ArrayList<NodeItem>();
            SDIData sdiData = sdiSnapshot.getSDIData(primaryItem);
            Set datasets = sdiData.getDatasets();
            Object[] keys = datasets.toArray();
            for (int i = 0; i < datasets.size(); ++i) {
                DataSet ds;
                String datasetkey = (String)keys[i];
                if ("primary".equals(datasetkey) || (ds = sdiData.getDataset(datasetkey)) == null || ds.size() <= 0 || ds.equals("arraylayoutitem") || ds.equals("arraylayoutzoneitem")) continue;
                String detailtablenodeid = datasetkey;
                String detailtablelabel = NodeItem.getNodeLabel(detailtablenodeid, nodeInfo);
                if (detailtablelabel.length() == 0) {
                    detailtablelabel = ConfigReportContent.generateSectionTitle(detailtablenodeid);
                }
                detailtablelabel = translationProcessor.translate(detailtablelabel);
                NodeItem nodeItem1 = new NodeItem(detailtablenodeid, detailtablelabel, false);
                ArrayList<NodeItem> sdidetaildataList = new ArrayList<NodeItem>();
                for (int drow = 0; drow < ds.size(); ++drow) {
                    String detailitemnodeid = datasetkey + "_" + drow;
                    String detailitemnodelabel = NodeItem.getNodeLabel(detailitemnodeid, nodeInfo);
                    if (detailitemnodelabel.length() <= 0) continue;
                    sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false));
                }
                nodeItem1.addChildItems(sdidetaildataList);
                childitemList.add(nodeItem1);
            }
            for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
                childitemList = NodeItem.processLinksByType(sdiSnapshot, primaryItem, nodeInfo, childitemList, linkType, translationProcessor);
            }
            nodeItem.addChildItems(childitemList);
            return nodeItem;
        }

        public static NodeItem getSDINodeTree(String[] ignoreDataSets, SDISnapshot srcSdiSnapshot, SDISnapshotItem srcPrimaryItem, SDISnapshot refSdiSnapshot, SDISnapshotItem refPrimaryItem, DataSet nodeInfo, TranslationProcessor translationProcessor) throws SapphireException, JSONException {
            SnapshotItem.LinkType[] linkTypes;
            boolean ignore;
            String label;
            String primarynodeid = "";
            if (srcPrimaryItem != null) {
                primarynodeid = srcPrimaryItem.toString();
            }
            if (primarynodeid.length() == 0) {
                primarynodeid = refPrimaryItem.toString();
            }
            if ((label = NodeItem.getNodeLabel(primarynodeid, nodeInfo)).length() == 0) {
                label = srcPrimaryItem != null ? ConfigReportContent.generateSDISectionTitle(SDISnapshotViewer.getSDI(srcPrimaryItem.getSDIData())) : ConfigReportContent.generateSDISectionTitle(SDISnapshotViewer.getSDI(refPrimaryItem.getSDIData()));
            }
            String image = "";
            if (label.contains("diffreportnewitem")) {
                image = "WEB-CORE/images/png/Add.png";
            } else if (label.contains("diffreportdeleteditem")) {
                image = "WEB-CORE/images/png/Delete.png";
            } else if (label.contains("diffreportmodifieditem")) {
                image = SDISnapshotViewer.modifiedicon;
            }
            NodeItem nodeItem = new NodeItem(primarynodeid, label, false, image);
            ArrayList<NodeItem> childitemList = new ArrayList<NodeItem>();
            SDIData srcSdiData = null;
            if (srcSdiSnapshot != null) {
                srcSdiData = srcSdiSnapshot.getSDIData(srcPrimaryItem);
            }
            Set srcdatasets = null;
            if (srcSdiData != null) {
                srcdatasets = srcSdiData.getDatasets();
            }
            SDIData refSdiData = null;
            if (refSdiSnapshot != null) {
                refSdiData = refSdiSnapshot.getSDIData(refPrimaryItem);
            }
            Set refdatasets = null;
            if (refSdiData != null) {
                refdatasets = refSdiData.getDatasets();
            }
            if (srcdatasets != null) {
                for (Object srcdatasetkey : srcdatasets) {
                    if ("primary".equals(srcdatasetkey)) continue;
                    ignore = false;
                    if (ignoreDataSets != null) {
                        for (int i = 0; i < ignoreDataSets.length; ++i) {
                            if (!srcdatasetkey.equals(ignoreDataSets[i])) continue;
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore) continue;
                    DataSet srcds = srcSdiData.getDataset((String)srcdatasetkey);
                    DataSet refds = null;
                    if (refSdiData != null) {
                        refds = refSdiData.getDataset((String)srcdatasetkey);
                    }
                    String[] keycols = srcSdiData.getKeys((String)srcdatasetkey);
                    if (srcds == null || srcds.size() <= 0) continue;
                    String detailtablenodeid = SDIData.getDatasetTablename((String)srcdatasetkey);
                    String detailtablelabel = NodeItem.getNodeLabel(detailtablenodeid, nodeInfo);
                    if (detailtablelabel.length() == 0) {
                        detailtablelabel = ConfigReportContent.generateSectionTitle(detailtablenodeid);
                    }
                    image = "";
                    if ((detailtablelabel = translationProcessor.translate(detailtablelabel)).contains("diffreportmodifieditem")) {
                        image = SDISnapshotViewer.modifiedicon;
                    }
                    NodeItem nodeItem1 = new NodeItem(detailtablenodeid, detailtablelabel, false, image);
                    ArrayList<NodeItem> sdidetaildataList = new ArrayList<NodeItem>();
                    for (int drow = 0; drow < srcds.size(); ++drow) {
                        String detailitemnodelabel;
                        String detailitemnodeid;
                        String detailtableid;
                        int refrow = NodeItem.findRowInDetail(srcds, drow, refds, keycols);
                        if (refrow == -1) {
                            detailtableid = SDIData.getDatasetTablename((String)srcdatasetkey);
                            detailitemnodeid = detailtableid + "_" + drow;
                            detailitemnodelabel = NodeItem.getNodeLabel(detailitemnodeid, nodeInfo, "New");
                            if (detailitemnodelabel.length() <= 0) continue;
                            sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, "WEB-CORE/images/png/Add.png"));
                            continue;
                        }
                        detailtableid = SDIData.getDatasetTablename((String)srcdatasetkey);
                        detailitemnodeid = detailtableid + "_" + drow;
                        detailitemnodelabel = NodeItem.getNodeLabel(detailitemnodeid, nodeInfo);
                        if (detailitemnodelabel.length() <= 0) continue;
                        if (detailitemnodelabel.contains("diffreportmodifieditem")) {
                            sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, SDISnapshotViewer.modifiedicon));
                            continue;
                        }
                        sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, ""));
                    }
                    if (refds != null) {
                        int deletecount = 0;
                        for (int drow = 0; drow < refds.size(); ++drow) {
                            int srcrow = NodeItem.findRowInDetail(refds, drow, srcds, keycols);
                            if (srcrow != -1) continue;
                            String detailtableid = SDIData.getDatasetTablename((String)srcdatasetkey);
                            int itemrow = srcds.getRowCount() + deletecount++;
                            String detailitemnodeid = detailtableid + "_" + itemrow;
                            String detailitemnodelabel = NodeItem.getNodeLabel(detailitemnodeid, nodeInfo, "Deleted");
                            if (detailitemnodelabel.length() <= 0) continue;
                            sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, "WEB-CORE/images/png/Delete.png"));
                        }
                    }
                    nodeItem1.addChildItems(sdidetaildataList);
                    childitemList.add(nodeItem1);
                }
            }
            if (refdatasets != null) {
                for (Object refdatasetkey : refdatasets) {
                    DataSet ds;
                    if ("primary".equals(refdatasetkey)) continue;
                    ignore = false;
                    if (ignoreDataSets != null) {
                        for (int i = 0; i < ignoreDataSets.length; ++i) {
                            if (!refdatasetkey.equals(ignoreDataSets[i])) continue;
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore || srcdatasets != null && srcdatasets.contains(refdatasetkey) || (ds = refSdiData.getDataset((String)refdatasetkey)) == null || ds.size() <= 0) continue;
                    String detailtablenodeid = (String)refdatasetkey;
                    String detailtablelabel = NodeItem.getNodeLabel(detailtablenodeid, nodeInfo);
                    if (detailtablelabel.length() == 0) {
                        detailtablelabel = ConfigReportContent.generateSectionTitle(detailtablenodeid);
                    }
                    NodeItem nodeItem1 = new NodeItem(detailtablenodeid, detailtablelabel, false);
                    ArrayList<NodeItem> sdidetaildataList = new ArrayList<NodeItem>();
                    for (int drow = 0; drow < ds.size(); ++drow) {
                        String detailitemnodelabel;
                        String detailitemnodeid;
                        if ("sdiworkitem".equals(refdatasetkey)) {
                            detailitemnodeid = refdatasetkey + "_" + drow;
                            detailitemnodelabel = ds.getValue(drow, "workitemid");
                            sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, "WEB-CORE/images/png/OrphanSDIWorkItem.png"));
                            continue;
                        }
                        if ("sdispec".equals(refdatasetkey)) {
                            detailitemnodeid = refdatasetkey + "_" + drow;
                            detailitemnodelabel = ds.getValue(drow, "specid");
                            sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, "WEB-CORE/images/png/OrphanSDIWorkItem.png"));
                            continue;
                        }
                        detailitemnodeid = refdatasetkey + "_" + drow;
                        detailitemnodelabel = NodeItem.getNodeLabel(detailitemnodeid, nodeInfo);
                        if (!detailitemnodelabel.contains("")) continue;
                        sdidetaildataList.add(new NodeItem(detailitemnodeid, detailitemnodelabel, false, ""));
                    }
                    nodeItem1.addChildItems(sdidetaildataList);
                    childitemList.add(nodeItem1);
                }
            }
            for (SnapshotItem.LinkType linkType : linkTypes = SnapshotItem.LinkType.values()) {
                childitemList = srcSdiSnapshot == null ? NodeItem.processLinksByType(refSdiSnapshot, refPrimaryItem, nodeInfo, childitemList, linkType, translationProcessor) : (refSdiSnapshot == null ? NodeItem.processLinksByType(srcSdiSnapshot, srcPrimaryItem, nodeInfo, childitemList, linkType, translationProcessor) : NodeItem.processLinksByType(srcSdiSnapshot, srcPrimaryItem, refSdiSnapshot, refPrimaryItem, nodeInfo, childitemList, linkType, translationProcessor));
            }
            nodeItem.addChildItems(childitemList);
            return nodeItem;
        }

        public static NodeItem getPropertyTreeNodeTree(PropertyTreeRO srcRO, PropertyTreeRO refRO, DataSet nodeInfo, TranslationProcessor translationProcessor) throws SapphireException, JSONException {
            String primarynodeid = "root";
            String label = NodeItem.getNodeLabel(primarynodeid, nodeInfo);
            if (label.length() == 0) {
                label = srcRO != null ? ConfigReportContent.generateSDISectionTitle(SDISnapshotViewer.getSDI(srcRO.currentSDIData)) : ConfigReportContent.generateSDISectionTitle(SDISnapshotViewer.getSDI(refRO.currentSDIData));
            }
            String image = "";
            if (label.contains("diffreportnewitem")) {
                image = "WEB-CORE/images/png/Add.png";
            } else if (label.contains("diffreportdeleteditem")) {
                image = "WEB-CORE/images/png/Delete.png";
            } else if (label.contains("diffreportmodifieditem")) {
                image = SDISnapshotViewer.modifiedicon;
            }
            NodeItem nodeItem = new NodeItem(primarynodeid, "Root", false, image);
            ArrayList<NodeItem> childitemList = new ArrayList<NodeItem>();
            NodeItem nodeItem1 = new NodeItem("childnode", "Child Node", false, image);
            childitemList.add(nodeItem1);
            nodeItem.addChildItems(childitemList);
            return nodeItem;
        }

        private static ArrayList<NodeItem> processLinksByType(SDISnapshot sdiSnapshot, SDISnapshotItem sdiSnapshotItem, DataSet nodeInfo, ArrayList<NodeItem> childitemList, SnapshotItem.LinkType linkType, TranslationProcessor translationProcessor) throws SapphireException, JSONException {
            List<String> linkIds = sdiSnapshotItem.getLinkIdsByType(linkType);
            for (String linkId : linkIds) {
                List<SnapshotItem> linkItemList = sdiSnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                if (linkItemList.size() <= 0) continue;
                String nodeLabel = NodeItem.getNodeLabel(linkId, nodeInfo);
                NodeItem nodeItem1 = new NodeItem(linkId, nodeLabel, false);
                ArrayList<NodeItem> sdidetaildataList = new ArrayList<NodeItem>();
                for (SnapshotItem item : linkItemList) {
                    if (item.isIncludedForTransfer() || item.getSDIData() == null) continue;
                    NodeItem linkedItem = NodeItem.getSDINodeTree(sdiSnapshot, (SDISnapshotItem)item, nodeInfo, translationProcessor);
                    sdidetaildataList.add(linkedItem);
                }
                nodeItem1.addChildItems(sdidetaildataList);
                childitemList.add(nodeItem1);
            }
            return childitemList;
        }

        private static int findRowInDetail(DataSet input, int row, DataSet dataset, String[] keycols) {
            if (dataset != null) {
                for (int i = 0; i < dataset.getRowCount(); ++i) {
                    boolean match = true;
                    if (keycols != null) {
                        for (int keynum = 0; keynum < keycols.length; ++keynum) {
                            if (input.getValue(row, keycols[keynum], "").equals(dataset.getValue(i, keycols[keynum], ""))) continue;
                            match = false;
                            break;
                        }
                    } else {
                        Trace.log("Keycols for dataset are empty");
                    }
                    if (!match) continue;
                    return i;
                }
            }
            return -1;
        }

        private static ArrayList<NodeItem> processLinksByType(SDISnapshot srcSdiSnapshot, SDISnapshotItem srcSdiSnapshotItem, SDISnapshot refSdiSnapshot, SDISnapshotItem refSdiSnapshotItem, DataSet nodeInfo, ArrayList<NodeItem> childitemList, SnapshotItem.LinkType linkType, TranslationProcessor translationProcessor) throws SapphireException, JSONException {
            List<String> srclinkIds = srcSdiSnapshotItem.getLinkIdsByType(linkType);
            List<String> reflinkIds = refSdiSnapshotItem.getLinkIdsByType(linkType);
            if (srclinkIds != null && srclinkIds.size() == 0 && reflinkIds != null && reflinkIds.size() > 0) {
                srclinkIds = reflinkIds;
            }
            for (String linkId : srclinkIds) {
                SDISnapshotItem srcitem;
                NodeItem linkedItem;
                ArrayList<NodeItem> sdidetaildataList;
                NodeItem nodeItem1;
                String image;
                String nodeLabel;
                List<SnapshotItem> srclinkItemList = srcSdiSnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                List<SnapshotItem> reflinkItemList = refSdiSnapshotItem.getLinkItemsByLinkId(linkType, linkId);
                if (srclinkItemList.size() > 0) {
                    nodeLabel = NodeItem.getNodeLabel(linkId, nodeInfo);
                    image = "";
                    if (nodeLabel.contains("diffreportmodifieditem")) {
                        image = SDISnapshotViewer.modifiedicon;
                    }
                    nodeItem1 = new NodeItem(linkId, nodeLabel, false, image);
                    sdidetaildataList = new ArrayList<NodeItem>();
                    for (SnapshotItem srcitem2 : srclinkItemList) {
                        if (srcitem2.isIncludedForTransfer() || srcitem2.getSDIData() == null) continue;
                        SDISnapshotItem refitem = (SDISnapshotItem)NodeItem.findInSnapshotItemList(reflinkItemList, srcitem2);
                        if (refitem != null) {
                            if (refitem.getSDIData() == null) continue;
                            linkedItem = NodeItem.getSDINodeTree(null, srcSdiSnapshot, (SDISnapshotItem)srcitem2, refSdiSnapshot, refitem, nodeInfo, translationProcessor);
                            sdidetaildataList.add(linkedItem);
                            continue;
                        }
                        linkedItem = NodeItem.getSDINodeTree(null, srcSdiSnapshot, (SDISnapshotItem)srcitem2, null, null, nodeInfo, translationProcessor);
                        sdidetaildataList.add(linkedItem);
                    }
                    for (SnapshotItem refitem : reflinkItemList) {
                        if (refitem.isIncludedForTransfer() || refitem.getSDIData() == null || (srcitem = (SDISnapshotItem)NodeItem.findInSnapshotItemList(srclinkItemList, refitem)) != null || refitem.isIncludedForTransfer() || refitem.getSDIData() == null) continue;
                        linkedItem = NodeItem.getSDINodeTree(null, null, null, refSdiSnapshot, (SDISnapshotItem)refitem, nodeInfo, translationProcessor);
                        sdidetaildataList.add(linkedItem);
                    }
                    nodeItem1.addChildItems(sdidetaildataList);
                    childitemList.add(nodeItem1);
                    continue;
                }
                if (reflinkItemList.size() <= 0) continue;
                nodeLabel = NodeItem.getNodeLabel(linkId, nodeInfo);
                image = "";
                if (nodeLabel.contains("diffreportmodifieditem")) {
                    image = SDISnapshotViewer.modifiedicon;
                }
                nodeItem1 = new NodeItem(linkId, nodeLabel, false, image);
                sdidetaildataList = new ArrayList();
                for (SnapshotItem refitem : reflinkItemList) {
                    if (refitem.getSDIData() == null || (srcitem = (SDISnapshotItem)NodeItem.findInSnapshotItemList(srclinkItemList, refitem)) != null || refitem.isIncludedForTransfer() || refitem.getSDIData() == null) continue;
                    linkedItem = NodeItem.getSDINodeTree(null, null, null, refSdiSnapshot, (SDISnapshotItem)refitem, nodeInfo, translationProcessor);
                    sdidetaildataList.add(linkedItem);
                }
                nodeItem1.addChildItems(sdidetaildataList);
                childitemList.add(nodeItem1);
            }
            return childitemList;
        }

        private static SnapshotItem findInSnapshotItemList(List<SnapshotItem> list, SnapshotItem item) {
            for (SnapshotItem curritem : list) {
                if (!curritem.toString().equals(item.toString())) continue;
                return curritem;
            }
            return null;
        }
    }
}

