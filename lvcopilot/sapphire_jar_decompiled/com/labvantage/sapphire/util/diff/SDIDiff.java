/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.util.diff.DiffProgress;
import com.labvantage.sapphire.util.diff.PropertyDefinitionListDiff;
import com.labvantage.sapphire.util.diff.PropertyListDiff;
import com.labvantage.sapphire.util.diff.PropertyTreeDiff;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SDIDiff {
    private SDIData sourceSDIData;
    private SDIData targetSDIData;
    private DataSet results;
    private String sdcid;
    private Set excludeList;
    private Cache propertyTreeCache = new Cache("propertytree");
    public static final String STATUS_ADDED = "Added to source";
    public static final String STATUS_CHANGED = "Difference Found";
    public static final String STATUS_MISSING = "Missing from source";
    public static final String STATUS_SAME = "No Change";
    public static final String STATUS_ERROR_UNABLETOLOADSOURCE = "Unable to load details from source";
    public static final String STATUS_ERROR_UNABLETOLOADTARGET = "Unable to load details from target";
    public static final String CHANGETYPE_PRIMARY = "Primary field";
    public static final String RESULTS_SDCID = "sdcid";
    public static final String RESULTS_KEYID1 = "keyid1";
    public static final String RESULTS_KEYID2 = "keyid2";
    public static final String RESULTS_KEYID3 = "keyid3";
    public static final String RESULTS_STATUS = "status";
    public static final String RESULTS_CHANGETYPE = "changetype";
    public static final String RESULTS_CHANGETYPEDETAIL = "changetypedetail";
    public static final String RESULTS_SOURCEVALUE = "sourcevalue";
    public static final String RESULTS_TARGETVALUE = "targetvalue";
    public static final String RESULTS_LONGDESCRIPTION = "longdescription";
    public static final int FILTER_DETAIL = 0;
    public static final int FILTER_SDCKEYID1 = 1;
    public static final int FILTER_SDCKEYID123 = 2;
    public static final int FILTER_CUSTOM = 3;
    public static final int DETAILTYPE_DETAIL = 0;
    public static final int DETAILTYPE_SDIDETAIL = 1;
    public static final String TARGETFOUND = "__checked";
    private String connectionid;
    private final boolean includeAuditFields;
    private DiffProgress diffProgress = null;

    public SDIDiff(String connectionid, String sdcid, SDIData sourceSDIData, SDIData targetSDIData, DiffProgress diffProgress, boolean includeAuditFields) {
        this.diffProgress = diffProgress;
        this.sdcid = sdcid;
        this.sourceSDIData = sourceSDIData;
        this.targetSDIData = targetSDIData;
        this.connectionid = connectionid;
        this.includeAuditFields = includeAuditFields;
        this.excludeList = this.getExcludeList();
    }

    public DataSet diff() throws SapphireException {
        this.diffProgress.setCancelled(false);
        this.diffProgress.setPercentComplete(0);
        this.results = new DataSet();
        this.results.addColumn(RESULTS_STATUS, 0);
        if (this.sourceSDIData == null) {
            this.errorRow(STATUS_ERROR_UNABLETOLOADSOURCE);
        }
        if (this.targetSDIData == null) {
            this.errorRow(STATUS_ERROR_UNABLETOLOADTARGET);
        }
        if (this.sourceSDIData != null && this.targetSDIData != null) {
            DataSet sourcePrimary = this.sourceSDIData.getDataset("primary");
            DataSet targetPrimary = this.targetSDIData.getDataset("primary");
            targetPrimary.addColumn(TARGETFOUND, 0);
            targetPrimary.setString(-1, TARGETFOUND, "N");
            String[] primaryKeyCols = this.sourceSDIData.getKeys("primary");
            String primaryKeyColid1 = primaryKeyCols[0];
            String primaryKeyColid2 = primaryKeyCols[1];
            String primaryKeyColid3 = primaryKeyCols[2];
            List<String> primaryKeyList = Arrays.asList(primaryKeyCols);
            String[] primaryMatchColumns = this.getMatchColumns("SDC:" + this.sdcid, sourcePrimary, this.excludeList);
            ArrayList detailNameList = new ArrayList();
            ArrayList detailMatchColumnsList = new ArrayList();
            ArrayList detailKeysList = new ArrayList();
            ArrayList detailTitleKeysList = new ArrayList();
            ArrayList detailFilterTypeList = new ArrayList();
            String[] detailTables = this.sourceSDIData.getLinkTables();
            for (int i = 0; detailTables != null && i < detailTables.length; ++i) {
                String detailName = detailTables[i];
                this.addDetailTable(detailName, 0, detailNameList, detailFilterTypeList, detailKeysList, detailMatchColumnsList, detailTitleKeysList, primaryKeyList);
            }
            String[] datasetNames = SDIData.getDatasetNames();
            for (int i = 0; i < datasetNames.length; ++i) {
                String detailName = datasetNames[i];
                this.addDetailTable(detailName, 1, detailNameList, detailFilterTypeList, detailKeysList, detailMatchColumnsList, detailTitleKeysList, primaryKeyList);
            }
            HashMap[] filter = new HashMap[]{new HashMap(), new HashMap(), new HashMap(), new HashMap()};
            for (int primaryRowCount = 0; primaryRowCount < sourcePrimary.size() && !this.diffProgress.isCancelled(); ++primaryRowCount) {
                this.diffProgress.setPercentComplete(100 * primaryRowCount / sourcePrimary.size());
                String keyid1 = sourcePrimary.getString(primaryRowCount, primaryKeyColid1);
                String keyid2 = primaryKeyColid2.length() > 0 ? sourcePrimary.getString(primaryRowCount, primaryKeyColid2) : "";
                String keyid3 = primaryKeyColid3.length() > 0 ? sourcePrimary.getString(primaryRowCount, primaryKeyColid3) : "";
                SDIStatus sdiStatus = this.processRow(primaryKeyCols, sourcePrimary, primaryRowCount, targetPrimary, primaryMatchColumns, "Primary Field", "primary");
                if (sdiStatus.status.equals(STATUS_ADDED)) {
                    this.rowAdded(keyid1, keyid2, keyid3);
                    continue;
                }
                if (!sdiStatus.status.equals(STATUS_SAME) && sdiStatus.status.equals(STATUS_CHANGED)) {
                    this.rowChanged(keyid1, keyid2, keyid3, sdiStatus, "");
                }
                filter[0].put(primaryKeyColid1, keyid1);
                if (primaryKeyColid2.length() > 0) {
                    filter[0].put(primaryKeyColid2, keyid2);
                }
                if (primaryKeyColid3.length() > 0) {
                    filter[0].put(primaryKeyColid3, keyid3);
                }
                filter[1].put(RESULTS_SDCID, this.sdcid);
                filter[1].put(RESULTS_KEYID1, keyid1);
                filter[1].put(TARGETFOUND, "Y");
                filter[2].put(RESULTS_SDCID, this.sdcid);
                filter[2].put(RESULTS_KEYID1, keyid1);
                if (primaryKeyColid2.length() > 0) {
                    filter[2].put(RESULTS_KEYID2, keyid2);
                }
                if (primaryKeyColid2.length() > 0) {
                    filter[2].put(RESULTS_KEYID3, keyid3);
                }
                for (int detailTableCount = 0; detailTableCount < detailNameList.size(); ++detailTableCount) {
                    String detailTableid = (String)detailNameList.get(detailTableCount);
                    String[] detailMatchColumns = (String[])detailMatchColumnsList.get(detailTableCount);
                    String[] detailKeys = (String[])detailKeysList.get(detailTableCount);
                    String[] detailTableKeys = (String[])detailTitleKeysList.get(detailTableCount);
                    int filterType = (Integer)detailFilterTypeList.get(detailTableCount);
                    DataSet sourceDetail = this.sourceSDIData.getDataset(detailTableid);
                    DataSet targetDetail = this.targetSDIData.getDataset(detailTableid);
                    HashMap<String, String> detailFilter = null;
                    if (filterType == 3) {
                        if (detailTableid.equals("syscolumn")) {
                            String tableid = sourcePrimary.getString(primaryRowCount, "tableid");
                            detailFilter = new HashMap();
                            detailFilter.put("tableid", tableid);
                        } else {
                            detailFilter = new HashMap<String, String>();
                            detailFilter.put("dummy", "dummy");
                        }
                    } else {
                        detailFilter = filter[filterType];
                    }
                    DataSet sourceDetailFiltered = sourceDetail.getFilteredDataSet(detailFilter);
                    DataSet targetDetailFiltered = targetDetail.getFilteredDataSet(detailFilter);
                    for (int detailRowCount = 0; detailRowCount < sourceDetailFiltered.size(); ++detailRowCount) {
                        String detailId = this.getDetailId(sourceDetailFiltered, detailRowCount, detailTableKeys);
                        SDIStatus detailStatus = this.processRow(detailKeys, sourceDetailFiltered, detailRowCount, targetDetailFiltered, detailMatchColumns, detailTableid + " " + detailId, detailTableid);
                        if (detailStatus.status.equals(STATUS_SAME)) continue;
                        if (detailStatus.status.equals(STATUS_ADDED)) {
                            detailStatus.addChangeDetail(detailTableid, STATUS_ADDED, detailId, "");
                            this.rowChanged(keyid1, keyid2, keyid3, detailStatus, "");
                            continue;
                        }
                        if (!detailStatus.status.equals(STATUS_CHANGED)) continue;
                        this.rowChanged(keyid1, keyid2, keyid3, detailStatus, "Changed ");
                    }
                    HashMap<String, String> remainingFilter = new HashMap<String, String>();
                    remainingFilter.put(TARGETFOUND, "N");
                    DataSet remaining = targetDetailFiltered.getFilteredDataSet(remainingFilter);
                    for (int i = 0; i < remaining.size(); ++i) {
                        String detailId = this.getDetailId(remaining, i, detailTableKeys);
                        SDIStatus detailStatus = new SDIStatus();
                        detailStatus.addChangeDetail(detailTableid, STATUS_MISSING, "", detailId);
                        this.rowChanged(keyid1, keyid2, keyid3, detailStatus, "");
                    }
                }
            }
            HashMap<String, String> remainingFilter = new HashMap<String, String>();
            remainingFilter.put(TARGETFOUND, "N");
            DataSet remaining = targetPrimary.getFilteredDataSet(remainingFilter);
            for (int i = 0; i < remaining.size(); ++i) {
                String keyid1 = remaining.getString(i, primaryKeyColid1);
                String keyid2 = primaryKeyColid2.length() > 0 ? sourcePrimary.getString(i, primaryKeyColid2) : "";
                String keyid3 = primaryKeyColid3.length() > 0 ? sourcePrimary.getString(i, primaryKeyColid3) : "";
                this.rowDeleted(keyid1, keyid2, keyid3);
            }
        }
        if (this.diffProgress.isCancelled()) {
            throw new SapphireException("Execution Cancelled");
        }
        this.diffProgress.setCancelled(false);
        return this.results;
    }

    private void addDetailTable(String detailName, int detailType, ArrayList detailTableList, ArrayList detailFilterTypeList, ArrayList detailKeysList, ArrayList detailMatchColumnsList, ArrayList detailTitleKeysList, List primaryKeyList) {
        if (this.isValidDataset(detailName)) {
            DataSet sourceDetail = this.sourceSDIData.getDataset(detailName);
            DataSet targetDetail = this.targetSDIData.getDataset(detailName);
            if (sourceDetail != null && targetDetail != null && (sourceDetail.size() > 0 || targetDetail.size() > 0)) {
                detailTableList.add(detailName);
                detailFilterTypeList.add(new Integer(this.getFilterType(detailName, detailType)));
                String[] detailTableKeys = detailType == 0 ? this.sourceSDIData.getLinkTableKeys(detailName) : this.sourceSDIData.getKeys(detailName);
                detailKeysList.add(detailTableKeys);
                String[] detailMatchColumns = this.getMatchColumns(detailName, sourceDetail, this.excludeList);
                detailMatchColumnsList.add(detailMatchColumns);
                ArrayList<String> detailTitleKeys = new ArrayList<String>();
                for (int j = 0; j < detailTableKeys.length; ++j) {
                    String detailTableKey = detailTableKeys[j];
                    if (detailType == 0) {
                        if (primaryKeyList.contains(detailTableKey)) continue;
                        detailTitleKeys.add(detailTableKey);
                        continue;
                    }
                    if (detailType != 1 || detailTableKey.equals(RESULTS_SDCID) || detailTableKey.equals(RESULTS_KEYID1) || detailTableKey.equals(RESULTS_KEYID2) || detailTableKey.equals(RESULTS_KEYID3)) continue;
                    detailTitleKeys.add(detailTableKey);
                }
                detailTitleKeysList.add(detailTitleKeys.toArray(new String[0]));
                targetDetail.addColumn(TARGETFOUND, 0);
                targetDetail.setString(-1, TARGETFOUND, "N");
            }
        }
    }

    private int getFilterType(String datasetName, int detailType) {
        int filterType;
        int n = filterType = detailType == 0 ? 0 : 2;
        if (datasetName.equals("category")) {
            filterType = 1;
        }
        if (datasetName.equals("role")) {
            filterType = 1;
        }
        if (datasetName.equals("syscolumn")) {
            filterType = 3;
        }
        return filterType;
    }

    private String getDetailId(DataSet sourceDetailFiltered, int detailRowCount, String[] detailTitleTableKeys) {
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < detailTitleTableKeys.length; ++i) {
            id.append(" ").append(sourceDetailFiltered.getValue(detailRowCount, detailTitleTableKeys[i]));
        }
        return id.substring(1);
    }

    private SDIStatus processRow(String[] keyColumnid, DataSet source, int sourceRow, DataSet target, String[] matchColumns, String changeType, String tableid) {
        SDIStatus details = new SDIStatus();
        HashMap<String, Object> find = new HashMap<String, Object>();
        for (int i = 0; i < keyColumnid.length; ++i) {
            if (keyColumnid[i].length() <= 0) continue;
            find.put(keyColumnid[i], source.getObject(sourceRow, keyColumnid[i]));
        }
        int foundRow = target.findRow(find);
        if (foundRow >= 0) {
            target.setString(foundRow, TARGETFOUND, "Y");
            for (int j = 0; j < matchColumns.length; ++j) {
                String valueB;
                String columnid = matchColumns[j];
                String valueA = source.getValue(sourceRow, columnid);
                if (valueA.equals(valueB = target.getValue(foundRow, columnid))) continue;
                details.status = STATUS_CHANGED;
                if (tableid.equals("webpagepropertytree") && columnid.equals("valuetree")) {
                    PropertyList pla = new PropertyList();
                    PropertyList plb = new PropertyList();
                    boolean ok = true;
                    try {
                        pla.setPropertyList(valueA);
                        try {
                            plb.setPropertyList(valueB);
                        }
                        catch (SapphireException e) {
                            details.addChangeDetail(changeType, columnid, "", "Failed to parse: " + e.getMessage());
                            ok = false;
                        }
                    }
                    catch (SapphireException e) {
                        details.addChangeDetail(changeType, columnid, "Failed to parse: " + e.getMessage(), "");
                        ok = false;
                    }
                    if (!ok) continue;
                    try {
                        String propertytreeid = source.getValue(sourceRow, "propertytreeid");
                        PropertyDefinitionList propertyDefList = this.getPropertyDefList(propertytreeid);
                        PropertyListDiff diff = new PropertyListDiff(propertyDefList, pla, plb);
                        if (!diff.hasDifferences()) continue;
                        String results = diff.toHTML("");
                        details.addChangeDetail(changeType, columnid, results);
                    }
                    catch (Exception e) {
                        details.addChangeDetail(changeType, columnid, "Unknown error: " + e.getMessage(), "");
                    }
                    continue;
                }
                if (this.sdcid.equals("PropertyTree") && tableid.equals("primary") && columnid.equals("valuetree")) {
                    PropertyTree originalTree = new PropertyTree();
                    PropertyTree modifiedTree = new PropertyTree();
                    try {
                        originalTree.setValueXML(valueA);
                        modifiedTree.setValueXML(valueB);
                    }
                    catch (SapphireException e) {
                        details.addChangeDetail(changeType, columnid, "Unknown error: " + e.getMessage(), "");
                    }
                    PropertyTreeDiff ptd = new PropertyTreeDiff(originalTree, modifiedTree);
                    if (!ptd.hasDifferences()) continue;
                    DataSet nodeDiffs = ptd.getResults();
                    ArrayList<String> titles = new ArrayList<String>();
                    titles.add("Node");
                    titles.add("Status");
                    titles.add("Description");
                    details.addChangeDetail("valuetree ", "Changed", nodeDiffs.toHTML(titles, "prop"));
                    continue;
                }
                if (this.sdcid.equals("PropertyTree") && tableid.equals("primary") && columnid.equals("definitiontree")) {
                    try {
                        PropertyTree sourceTree = new PropertyTree();
                        sourceTree.setDefinitionXML(valueA);
                        PropertyDefinitionList sourceDef = sourceTree.getPropertyDefinitionList();
                        PropertyTree targetTree = new PropertyTree();
                        targetTree.setDefinitionXML(valueB);
                        PropertyDefinitionList targetDef = targetTree.getPropertyDefinitionList();
                        PropertyDefinitionListDiff diff = new PropertyDefinitionListDiff(sourceDef, targetDef);
                        if (!diff.hasDifferences()) continue;
                        DataSet d = diff.getResults();
                        ArrayList<String> titles = new ArrayList<String>();
                        titles.add("Property");
                        titles.add("Status");
                        titles.add("Description");
                        details.addChangeDetail("definitiontree", "Changed", d.toHTML(titles, "prop"));
                    }
                    catch (SapphireException e) {
                        details.addChangeDetail(changeType, columnid, "Unknown error: " + e.getMessage(), "");
                    }
                    continue;
                }
                details.addChangeDetail(changeType, columnid, source.getValue(sourceRow, columnid), target.getValue(foundRow, columnid));
            }
        } else {
            details.status = STATUS_ADDED;
        }
        return details;
    }

    private PropertyDefinitionList getPropertyDefList(String propertytreeid) throws Exception {
        PropertyDefinitionList propertyDefList = (PropertyDefinitionList)this.propertyTreeCache.get(propertytreeid);
        if (propertyDefList == null) {
            WebAdminProcessor wp = new WebAdminProcessor(this.connectionid);
            propertyDefList = wp.getPropertyDefinitionList(propertytreeid);
            this.propertyTreeCache.put(propertytreeid, propertyDefList);
        }
        return propertyDefList;
    }

    private Set getExcludeList() {
        HashSet<String> excludeList = new HashSet<String>();
        if (!this.includeAuditFields) {
            excludeList.add("createdt");
            excludeList.add("createby");
            excludeList.add("createtool");
            excludeList.add("moddt");
            excludeList.add("modby");
            excludeList.add("modtool");
            excludeList.add("auditsequence");
            excludeList.add("tracelogid");
        }
        excludeList.add("__rsetseq");
        excludeList.add("__lockstate");
        excludeList.add("__lockedby");
        return excludeList;
    }

    private Set getExtraExcludeList(String tableid) {
        HashSet<String> excludeList = new HashSet<String>();
        if (tableid.equals("SDC:User")) {
            excludeList.add("lastsetuserdt");
        }
        if (tableid.equals("SDC:User")) {
            excludeList.add("totalattempts");
        }
        return excludeList;
    }

    private String[] getMatchColumns(String tableid, DataSet dataset, Set excludeList) {
        Set extraExcludeList = this.getExtraExcludeList(tableid);
        ArrayList<String> columnsList = new ArrayList<String>();
        int columnCount = dataset.getColumnCount();
        for (int col = 0; col < columnCount; ++col) {
            String columnid = dataset.getColumnId(col);
            if (excludeList.contains(columnid) || extraExcludeList.contains(columnid)) continue;
            columnsList.add(columnid);
        }
        return columnsList.toArray(new String[0]);
    }

    private void rowChanged(String keyid1, String keyid2, String keyid3, SDIStatus sdiStatus, String changeTypeDetailPrefix) {
        for (ChangeDetail changeDetail : sdiStatus.changeDetails) {
            int row = this.results.addRow();
            this.results.setString(row, RESULTS_SDCID, this.sdcid);
            this.results.setString(row, RESULTS_KEYID1, keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                this.results.setString(row, RESULTS_KEYID2, keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0) {
                this.results.setString(row, RESULTS_KEYID3, keyid3);
            }
            this.results.setString(row, RESULTS_STATUS, STATUS_CHANGED);
            this.results.setString(row, RESULTS_CHANGETYPE, changeDetail.changeType);
            this.results.setString(row, RESULTS_CHANGETYPEDETAIL, changeTypeDetailPrefix + changeDetail.changeTypeDetails);
            this.results.setString(row, RESULTS_SOURCEVALUE, changeDetail.sourceValue);
            this.results.setString(row, RESULTS_TARGETVALUE, changeDetail.targetValue);
            this.results.setString(row, RESULTS_LONGDESCRIPTION, changeDetail.longDescription);
        }
    }

    private void rowDeleted(String keyid1, String keyid2, String keyid3) {
        int row = this.results.addRow();
        this.results.setString(row, RESULTS_SDCID, this.sdcid);
        this.results.setString(row, RESULTS_KEYID1, keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            this.results.setString(row, RESULTS_KEYID2, keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            this.results.setString(row, RESULTS_KEYID3, keyid3);
        }
        this.results.setString(row, RESULTS_STATUS, STATUS_MISSING);
    }

    private void rowAdded(String keyid1, String keyid2, String keyid3) {
        int row = this.results.addRow();
        this.results.setString(row, RESULTS_SDCID, this.sdcid);
        this.results.setString(row, RESULTS_KEYID1, keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            this.results.setString(row, RESULTS_KEYID2, keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            this.results.setString(row, RESULTS_KEYID3, keyid3);
        }
        this.results.setString(row, RESULTS_STATUS, STATUS_ADDED);
    }

    private void errorRow(String error) {
        int row = this.results.addRow();
        this.results.setString(row, RESULTS_SDCID, this.sdcid);
        this.results.setString(row, RESULTS_STATUS, error);
    }

    private boolean isValidDataset(String datasetTable) {
        return !datasetTable.equals("primary") && !datasetTable.equals("pricelistitem") && !datasetTable.equals("chargelistitem") && !datasetTable.equals("workgroupitem") && !datasetTable.equals("workgroupparamlist");
    }

    class SDIStatus {
        String status = "No Change";
        ArrayList changeDetails = new ArrayList();

        SDIStatus() {
        }

        public void addChangeDetail(String changeType, String changeTypeDetail, String sourceValue, String targetValue) {
            ChangeDetail cd = new ChangeDetail();
            cd.changeType = changeType;
            cd.changeTypeDetails = changeTypeDetail;
            cd.sourceValue = sourceValue;
            cd.targetValue = targetValue;
            this.changeDetails.add(cd);
        }

        public void addChangeDetail(String changeType, String changeTypeDetail, String longDescription) {
            ChangeDetail cd = new ChangeDetail();
            cd.changeType = changeType;
            cd.changeTypeDetails = changeTypeDetail;
            cd.longDescription = longDescription;
            this.changeDetails.add(cd);
        }
    }

    class ChangeDetail {
        String changeType;
        String changeTypeDetails;
        String sourceValue;
        String targetValue;
        String longDescription;

        ChangeDetail() {
        }
    }
}

