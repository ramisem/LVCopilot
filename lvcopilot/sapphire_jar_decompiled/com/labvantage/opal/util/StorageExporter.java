/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Document
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.XMLWriter
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class StorageExporter {
    private static final String SDC_PHYSICALSTORE = "PhysicalStore";
    private static final String SDC_STORAGEUNITSDC = "StorageUnitSDC";
    public static final int MODE_EXPORT = 0;
    public static final int MODE_COPY = 1;
    private List<String> nonExportColumns = new ArrayList<String>();
    private String viewXML = "";
    private DataSet storageUnitDataSet;
    private DataSet storageRestrictionsDataSet;
    private Map<String, DataSet> linkedSDIDataMap;
    private String storageunitid;
    private DataSet referenceDataSet;
    private HashMap<String, String> filter = new HashMap();
    private int mode;
    private boolean withoutBoxes;

    public StorageExporter(String storageunitid, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, DAMProcessor damProcessor, boolean isOra, boolean withoutBoxes) throws SapphireException {
        this(storageunitid, 0, queryProcessor, sdcProcessor, damProcessor, isOra, withoutBoxes);
    }

    public StorageExporter(String storageunitid, int mode, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, DAMProcessor damProcessor, boolean isOra, boolean withoutBoxes) throws SapphireException {
        this.storageunitid = storageunitid;
        this.mode = mode;
        this.withoutBoxes = withoutBoxes;
        this.referenceDataSet = new DataSet();
        this.referenceDataSet.addColumn("sdcid", 0);
        this.referenceDataSet.addColumn("keyid1", 0);
        this.referenceDataSet.addColumn("keyid2", 0);
        this.referenceDataSet.addColumn("keyid3", 0);
        this.linkedSDIDataMap = new HashMap<String, DataSet>();
        this.nonExportColumns.add("activeflag");
        this.nonExportColumns.add("auditsequence");
        this.nonExportColumns.add("createby");
        this.nonExportColumns.add("createdt");
        this.nonExportColumns.add("createtool");
        this.nonExportColumns.add("generatedlabel");
        this.nonExportColumns.add("lastmoveddt");
        this.nonExportColumns.add("modby");
        this.nonExportColumns.add("moddt");
        this.nonExportColumns.add("modtool");
        this.nonExportColumns.add("templateflag");
        this.nonExportColumns.add("tracelogid");
        this.nonExportColumns.add("usersequence");
        this.nonExportColumns.add("valuetree");
        this.nonExportColumns.add("labelpath");
        this.nonExportColumns.add("storageunitid");
        this.nonExportColumns.add("parentid");
        this.nonExportColumns.add("ancestorid");
        DataSet columnData = sdcProcessor.getColumnData(SDC_STORAGEUNITSDC);
        StringBuilder sql = new StringBuilder();
        sql.append("select storageunit.storageunitid, storageunit.parentid,");
        for (int i = 0; i < columnData.size(); ++i) {
            String columnid = columnData.getString(i, "columnid");
            if (this.nonExportColumns.contains(columnid)) continue;
            sql.append("storageunit.").append(columnid).append(",");
        }
        sql.append(" trackitem.trackitemid");
        sql.append(" from storageunit left outer join trackitem on trackitem.linksdcid = storageunit.linksdcid and trackitem.linkkeyid1 = storageunit.linkkeyid1");
        if (isOra) {
            sql.append(" connect by prior storageunit.storageunitid = storageunit.parentid");
            sql.append(" start with storageunit.storageunitid = ?");
            this.storageUnitDataSet = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        } else {
            String allStorageUnits = StorageUnitSDC.getAllStorageUnits(queryProcessor, false, storageunitid);
            String rsetid = damProcessor.createRSet(SDC_STORAGEUNITSDC, allStorageUnits, null, null);
            sql.append(" where storageunit.storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
            this.storageUnitDataSet = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            damProcessor.clearRSet(rsetid);
        }
        if (OpalUtil.isNotEmpty(this.storageUnitDataSet)) {
            String storageunitrsetid = damProcessor.createRSet(SDC_STORAGEUNITSDC, this.storageUnitDataSet.getColumnValues("storageunitid", ";"), null, null);
            this.storageRestrictionsDataSet = queryProcessor.getPreparedSqlDataSet("select storageunitid, restrictionbasedon, propertyid, propertyvalue, operator, failuremessage from storagerestriction where storageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?) order by storageunitid, usersequence", (Object[])new String[]{storageunitrsetid});
            damProcessor.clearRSet(storageunitrsetid);
            HashMap linkSDCMap = new HashMap();
            for (int i = 0; i < this.storageUnitDataSet.size(); ++i) {
                String linksdcid = this.storageUnitDataSet.getString(i, "linksdcid", "");
                String linkkeyid1 = this.storageUnitDataSet.getString(i, "linkkeyid1", "");
                if (linksdcid.length() <= 0 || SDC_PHYSICALSTORE.equals(linksdcid) || linkkeyid1.length() <= 0) continue;
                if (!linkSDCMap.containsKey(linksdcid)) {
                    linkSDCMap.put(linksdcid, new HashSet());
                }
                ((Set)linkSDCMap.get(linksdcid)).add(linkkeyid1);
            }
            if (linkSDCMap.size() > 0) {
                for (String sdcid : linkSDCMap.keySet()) {
                    columnData = sdcProcessor.getColumnData(sdcid);
                    String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                    String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                    String rsetid = damProcessor.createRSet(sdcid, OpalUtil.toDelimitedString((Collection)linkSDCMap.get(sdcid), ";"), null, null);
                    sql.setLength(0);
                    sql.append("select ").append(keycolid1).append(" linkkeyid1");
                    for (int i = 0; i < columnData.size(); ++i) {
                        String columnid = columnData.getString(i, "columnid");
                        if (this.nonExportColumns.contains(columnid) || keycolid1.equals(columnid)) continue;
                        sql.append(",").append(columnid);
                    }
                    sql.append(" from ").append(tableid).append(" where ").append(keycolid1).append(" in ");
                    sql.append("(select r.keyid1 from rsetitems r where r.rsetid = ?)");
                    DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    damProcessor.clearRSet(rsetid);
                    if (!OpalUtil.isNotEmpty(ds)) continue;
                    this.linkedSDIDataMap.put(sdcid, ds);
                }
            }
        }
    }

    public String getExportXML() {
        Document exportDocument = DocumentHelper.createDocument();
        Element storageExport = exportDocument.addElement("storageunits");
        storageExport.addAttribute("trackitemallowedflag", "N");
        storageExport.addAttribute("spaceavailflag", "N");
        storageExport.addAttribute("moveableflag", "N");
        storageExport.addAttribute("lastnodeflag", "N");
        storageExport.addAttribute("maxtiallowed", "0");
        storageExport.addAttribute("size", String.valueOf(this.storageUnitDataSet.size()));
        Document viewDocument = DocumentHelper.createDocument();
        Element storageview = null;
        if (this.mode == 0) {
            storageview = viewDocument.addElement("storageview");
        }
        this.addStorageUnitElement(storageExport, storageview, this.storageunitid);
        if (this.mode == 0) {
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            StringWriter stringWriter = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter((Writer)stringWriter, outputFormat);
            try {
                xmlWriter.write(viewDocument);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            this.viewXML = stringWriter.toString();
        }
        return exportDocument.asXML();
    }

    public String getViewXML() {
        return this.viewXML;
    }

    public DataSet getReferenceDataSet() {
        return this.referenceDataSet;
    }

    private void addStorageUnitElement(Element storageExportElement, Element parentStorageViewElement, String storageunitid) {
        block7: {
            DataSet childds;
            Element storageViewElement;
            int row;
            block8: {
                block9: {
                    row = this.storageUnitDataSet.findRow("storageunitid", storageunitid);
                    if (row == -1) break block7;
                    storageViewElement = null;
                    childds = this.getChildStorageDataSet(this.storageUnitDataSet, storageunitid);
                    String trackitemid = this.storageUnitDataSet.getString(row, "trackitemid", "");
                    if (trackitemid.length() <= 0) break block8;
                    if (this.withoutBoxes) break block7;
                    Element storageunitElement = storageExportElement.addElement("storagecontainer");
                    this.addStorageAttributes(storageunitElement, this.storageUnitDataSet, row);
                    if (parentStorageViewElement != null) {
                        storageViewElement = parentStorageViewElement.addElement(StringUtil.replaceAll(this.storageUnitDataSet.getString(row, "storageunittype"), " ", "_"));
                        storageViewElement.addAttribute("Layout", this.storageUnitDataSet.getValue(row, "propertytreeid"));
                    }
                    this.addStorageRestrictions(storageunitElement, storageViewElement, storageunitid);
                    if (!OpalUtil.isNotEmpty(childds)) break block9;
                    StringBuilder childlabel = new StringBuilder();
                    StringBuilder childlabelcol = new StringBuilder();
                    StringBuilder childlabelrow = new StringBuilder();
                    StringBuilder childmaxtiallowed = new StringBuilder();
                    for (int i = 0; i < childds.size(); ++i) {
                        childlabel.append(childds.getString(i, "storageunitlabel", "")).append(";");
                        childlabelrow.append(childds.getString(i, "labelrow", "")).append(";");
                        childlabelcol.append(childds.getString(i, "labelcol", "")).append(";");
                        childmaxtiallowed.append(childds.getValue(i, "maxtiallowed", "")).append(";");
                    }
                    childlabel.setLength(childlabel.length() - 1);
                    childlabelrow.setLength(childlabelrow.length() - 1);
                    childlabelcol.setLength(childlabelcol.length() - 1);
                    childmaxtiallowed.setLength(childmaxtiallowed.length() - 1);
                    String storageunittypeid = childds.getString(0, "storageunittype", "");
                    String propertytreeid = childds.getString(0, "propertytreeid", "");
                    Element childstorageunit = storageunitElement.addElement("storageunit");
                    childstorageunit.addAttribute("storageunittype", storageunittypeid);
                    childstorageunit.addAttribute("propertytreeid", propertytreeid);
                    childstorageunit.addAttribute("storageunitlabel", childlabel.toString());
                    childstorageunit.addAttribute("labelrow", childlabelrow.toString());
                    childstorageunit.addAttribute("labelcol", childlabelcol.toString());
                    childstorageunit.addAttribute("maxtiallowed", childmaxtiallowed.toString());
                    if (storageViewElement != null) {
                        storageViewElement.addAttribute("Size", this.storageUnitDataSet.getValue(row, "storageunitsize"));
                        storageViewElement.addAttribute("Rows", this.storageUnitDataSet.getValue(row, "numrows"));
                        storageViewElement.addAttribute("Columns", this.storageUnitDataSet.getValue(row, "numcol"));
                    }
                    if (this.mode == 0) {
                        this.addToReferenceDataSet("PropertyTree", propertytreeid, storageunittypeid);
                    }
                    break block7;
                }
                if (storageViewElement == null) break block7;
                storageViewElement.addAttribute("MaxTrackitemsAllowed", this.storageUnitDataSet.getValue(row, "maxtiallowed"));
                break block7;
            }
            Element storageUnitElement = storageExportElement.addElement("storageunit");
            this.addStorageAttributes(storageUnitElement, this.storageUnitDataSet, row);
            if (parentStorageViewElement != null) {
                storageViewElement = parentStorageViewElement.addElement(StringUtil.replaceAll(this.storageUnitDataSet.getString(row, "storageunittype"), " ", "_"));
                storageViewElement.addAttribute("Label", this.storageUnitDataSet.getString(row, "storageunitlabel"));
            }
            this.addStorageRestrictions(storageUnitElement, storageViewElement, storageunitid);
            if (OpalUtil.isNotEmpty(childds)) {
                for (int i = 0; i < childds.size(); ++i) {
                    this.addStorageUnitElement(storageUnitElement, storageViewElement, childds.getString(i, "storageunitid"));
                }
            }
        }
    }

    private void addToReferenceDataSet(String sdcid, String keyid1, String keyid2) {
        this.filter.clear();
        this.filter.put("sdcid", sdcid);
        this.filter.put("keyid1", keyid1);
        if (OpalUtil.isNotEmpty(keyid2)) {
            this.filter.put("keyid2", keyid2);
        }
        if (this.referenceDataSet.findRow(this.filter) == -1) {
            int row = this.referenceDataSet.addRow();
            this.referenceDataSet.setString(row, "sdcid", sdcid);
            this.referenceDataSet.setString(row, "keyid1", keyid1);
            if (OpalUtil.isNotEmpty(keyid2)) {
                this.referenceDataSet.setString(row, "keyid2", keyid2);
            }
        }
    }

    private DataSet getChildStorageDataSet(DataSet dataSet, String storageunitid) {
        this.filter.clear();
        this.filter.put("parentid", storageunitid);
        DataSet ds = dataSet.getFilteredDataSet(this.filter);
        if (OpalUtil.isNotEmpty(ds)) {
            ds.sort("storageunitindex");
        }
        return ds;
    }

    private void addStorageAttributes(Element storageUnitElement, DataSet storageUnitDataSet, int row) {
        block18: for (int col = 0; col < storageUnitDataSet.getColumnCount(); ++col) {
            String value;
            String columnid = storageUnitDataSet.getColumnId(col).toLowerCase();
            if (this.nonExportColumns.contains(columnid) || (value = storageUnitDataSet.getValue(row, columnid, "")).length() <= 0) continue;
            switch (columnid) {
                case "trackitemallowedflag": 
                case "spaceavailflag": 
                case "moveableflag": 
                case "lastnodeflag": {
                    if (!"Y".equals(value)) continue block18;
                    storageUnitElement.addAttribute(columnid, "Y");
                    continue block18;
                }
                case "maxtiallowed": {
                    if ("0".equals(value)) continue block18;
                    storageUnitElement.addAttribute(columnid, value);
                    continue block18;
                }
                case "linksdcid": 
                case "linkkeyid1": 
                case "linkkeyid2": 
                case "linkkeyid3": 
                case "arraylayoutid": 
                case "arraylayoutversionid": {
                    continue block18;
                }
                default: {
                    storageUnitElement.addAttribute(columnid, this.sanitizeText(value));
                }
            }
        }
        String linksdcid = storageUnitDataSet.getString(row, "linksdcid", "");
        if (SDC_PHYSICALSTORE.equals(linksdcid)) {
            // empty if block
        }
        String arraylayoutid = storageUnitDataSet.getString(row, "arraylayoutid", "");
        String arraylayoutversionid = storageUnitDataSet.getValue(row, "arraylayoutversionid", "");
        if (arraylayoutid.length() > 0 && arraylayoutversionid.length() > 0) {
            this.addToReferenceDataSet("LV_ArrayLayout", arraylayoutid, arraylayoutversionid);
        }
        if (this.mode == 0) {
            String storageunittypeid = storageUnitDataSet.getString(row, "storageunittype", "");
            String propertytreeid = storageUnitDataSet.getString(row, "propertytreeid", "");
            this.addToReferenceDataSet("PropertyTree", propertytreeid, storageunittypeid);
        }
        if (linksdcid.length() > 0 && !SDC_PHYSICALSTORE.equals(linksdcid)) {
            int keyrow;
            String linkkeyid1 = storageUnitDataSet.getString(row, "linkkeyid1", "");
            DataSet linkds = this.linkedSDIDataMap.get(linksdcid);
            if (OpalUtil.isNotEmpty(linkds) && (keyrow = linkds.findRow("linkkeyid1", linkkeyid1)) != -1) {
                storageUnitElement.addAttribute("link_sdcid", linksdcid);
                for (int col = 0; col < linkds.getColumnCount(); ++col) {
                    String value;
                    String columnid = linkds.getColumnId(col).toLowerCase();
                    if (this.nonExportColumns.contains(columnid) || (value = linkds.getValue(keyrow, columnid, "")).length() <= 0) continue;
                    storageUnitElement.addAttribute("link_" + columnid, this.sanitizeText(value));
                }
            }
        }
    }

    private void addStorageRestrictions(Element storageUnitElement, Element storageViewElement, String storageunitid) {
        if (OpalUtil.isNotEmpty(this.storageRestrictionsDataSet)) {
            this.filter.clear();
            this.filter.put("storageunitid", storageunitid);
            DataSet ds = this.storageRestrictionsDataSet.getFilteredDataSet(this.filter);
            if (OpalUtil.isNotEmpty(ds)) {
                for (int i = 0; i < ds.size(); ++i) {
                    String failureMessage = this.sanitizeText(ds.getValue(i, "failuremessage", ""));
                    Element restriction = storageUnitElement.addElement("storagerestriction");
                    restriction.addAttribute("restrictionbasedon", ds.getValue(i, "restrictionbasedon", ""));
                    restriction.addAttribute("propertyid", ds.getValue(i, "propertyid", ""));
                    restriction.addAttribute("propertyvalue", ds.getValue(i, "propertyvalue", ""));
                    restriction.addAttribute("operator", ds.getValue(i, "operator", ""));
                    restriction.addAttribute("failuremessage", failureMessage);
                    restriction.addAttribute("usersequence", String.valueOf(i + 1));
                    if (storageViewElement == null) continue;
                    Element restrictionview = storageViewElement.addElement("StorageRestriction");
                    restrictionview.addAttribute("restriction", failureMessage);
                }
            }
        }
    }

    private String sanitizeText(String text) {
        text = StringUtil.replaceAll(text, "\"", "||quot||");
        text = StringUtil.replaceAll(text, "\n", "||newline||");
        return text;
    }
}

