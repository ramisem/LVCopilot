/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.lucene.document.Document
 *  org.apache.lucene.document.Field$Store
 *  org.apache.lucene.document.StringField
 *  org.apache.lucene.document.TextField
 *  org.apache.lucene.index.IndexWriter
 *  org.apache.lucene.index.IndexableField
 *  org.apache.lucene.index.Term
 */
package com.labvantage.sapphire.modules.search.indexers;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import com.labvantage.sapphire.modules.search.indexers.Index;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class WorksheetIndexer
extends BaseIndexer
implements Index,
ELNConstants {
    public WorksheetIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps, String resultsetName) {
        super(indexer, dbu, indexWriter, sdcProps, resultsetName);
    }

    public WorksheetIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps) {
        super(indexer, dbu, indexWriter, sdcProps);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void indexSet() {
        block37: {
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String id = null;
            String mapId = "";
            StringBuffer contentInput = new StringBuffer();
            String sdcid = this.sdcProps.getProperty("sdcid");
            if (sdcid.equalsIgnoreCase("LV_Worksheet")) {
                try {
                    if (this.sdcConditionMatch(sdcid)) {
                        String worksheetid = this.getValue("worksheetid");
                        String worksheetversionid = this.getValue("worksheetversionid");
                        String correctedKeyid1 = this.substituteChars(worksheetid);
                        String correctedKeyid2 = this.substituteChars(worksheetversionid);
                        String correctedKeyid3 = "";
                        id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                        mapId = "SDI;" + sdcid + ";" + worksheetid + ";" + worksheetversionid + ";" + "";
                        Document doc = new Document();
                        this.addKeyFields(id, "SDI", sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                        this.addAuditFields(doc);
                        doc.add((IndexableField)new StringField("desccol", this.getValue("worksheetdesc"), Field.Store.YES));
                        Trace.logDebug("INDEXER", "Desc col: worksheetdesc");
                        doc.add((IndexableField)new StringField("singular", "worksheet", Field.Store.YES));
                        Trace.logDebug("INDEXER", "Singular: worksheet");
                        contentInput.append(worksheetid);
                        contentInput.append(" ").append(worksheetversionid);
                        contentInput.append(" ").append(this.getValue("worksheetname"));
                        contentInput.append(" ").append(this.getValue("worksheetdesc"));
                        contentInput.append(" ").append(this.getValue("worksheetstatus"));
                        contentInput.append(" ").append(this.getValue("templateid"));
                        contentInput.append(" ").append(this.getValue("templateversionid"));
                        contentInput.append(" ").append(this.getValue("authorid"));
                        String worksheetstatus = this.getValue("worksheetstatus");
                        this.addAttributes("LV_Worksheet", worksheetid, worksheetversionid, "", contentInput);
                        TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                        content.setBoost(2.0f);
                        doc.add((IndexableField)content);
                        Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                        this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length());
                        try {
                            ConnectionInfo connectionInfo = new ConnectionInfo(this.indexer.getSapphireDatabase());
                            this.dbu.createPreparedResultSet("worksheetsection", "SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=?", new String[]{worksheetid, worksheetversionid});
                            while (this.dbu.getNext("worksheetsection")) {
                                String worksheetsectionid = this.dbu.getString("worksheetsection", "worksheetsectionid");
                                String worksheetsectionversionid = this.dbu.getString("worksheetsection", "worksheetsectionversionid");
                                if ("backlog".equals(this.indexer.getCurrentIndexingStatus())) {
                                    Indexer.indexSDI(connectionInfo, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "", "B");
                                    continue;
                                }
                                Indexer.indexSDI(connectionInfo, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "");
                            }
                            this.dbu.createPreparedResultSet("worksheetitem", "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid=? AND worksheetversionid=?", new String[]{worksheetid, worksheetversionid});
                            while (this.dbu.getNext("worksheetitem")) {
                                String worksheetitemid = this.dbu.getString("worksheetitem", "worksheetitemid");
                                String worksheetitemversionid = this.dbu.getString("worksheetitem", "worksheetitemversionid");
                                if ("backlog".equals(this.indexer.getCurrentIndexingStatus())) {
                                    Indexer.indexSDI(connectionInfo, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "", "B");
                                    continue;
                                }
                                Indexer.indexSDI(connectionInfo, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "");
                            }
                            break block37;
                        }
                        finally {
                            this.dbu.closeResultSet("worksheetsection");
                            this.dbu.closeResultSet("worksheetitem");
                        }
                    }
                    Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove Worksheet from index.");
                    String keyId1 = this.getValue("worksheetid");
                    String keyId2 = this.getValue("worksheetversionid");
                    this.removeEntireWorksheet(keyId1, keyId2);
                }
                catch (Exception e) {
                    try {
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length(), e.getMessage());
                        Trace.logError("INDEXER", (Object)("Failed to index worksheet record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
                    }
                    catch (SapphireException se) {
                        Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
                    }
                }
            } else if (sdcid.equalsIgnoreCase("LV_WorksheetSection")) {
                try {
                    String worksheetsectionid = this.getValue("worksheetsectionid");
                    String worksheetsectionversionid = this.getValue("worksheetsectionversionid");
                    this.dbu.createPreparedResultSet("worksheet", "SELECT * FROM worksheet, worksheetsection WHERE worksheet.worksheetid = worksheetsection.worksheetid AND worksheet.worksheetversionid = worksheetsection.worksheetversionid AND worksheetsection.worksheetsectionid=? AND worksheetsection.worksheetsectionversionid=?", new String[]{worksheetsectionid, worksheetsectionversionid});
                    this.dbu.getNext("worksheet");
                    String worksheetid = this.dbu.getString("worksheet", "worksheetid");
                    String worksheetversionid = this.dbu.getString("worksheet", "worksheetversionid");
                    if (this.sdcConditionMatch("LV_Worksheet", "worksheet")) {
                        String correctedKeyid1 = this.substituteChars(worksheetsectionid);
                        String correctedKeyid2 = this.substituteChars(worksheetsectionversionid);
                        String correctedKeyid3 = "";
                        id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                        mapId = "SDI;" + sdcid + ";" + worksheetsectionid + ";" + worksheetsectionversionid + ";" + "";
                        Document doc = new Document();
                        this.addKeyFields(id, "SDI", sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                        this.addAuditFields(doc);
                        doc.add((IndexableField)new StringField("desccol", this.getValue("worksheetsectiondesc"), Field.Store.YES));
                        Trace.logDebug("INDEXER", "Desc col: worksheetsectiondesc");
                        doc.add((IndexableField)new StringField("singular", "worksheet section", Field.Store.YES));
                        Trace.logDebug("INDEXER", "Singular: worksheet section");
                        doc.add((IndexableField)new StringField("worksheetid", this.substituteChars(this.getValue("worksheetid")), Field.Store.YES));
                        doc.add((IndexableField)new StringField("worksheetversionid", this.substituteChars(this.getValue("worksheetversionid")), Field.Store.YES));
                        contentInput.append(worksheetsectionid);
                        contentInput.append(" ").append(worksheetsectionversionid);
                        contentInput.append(" ").append(this.getValue("worksheetsectiondesc"));
                        contentInput.append(" ").append(this.getValue("sectionstatus"));
                        contentInput.append(" ").append(this.getValue("templateid"));
                        contentInput.append(" ").append(this.getValue("templateversionid"));
                        this.addAttributes("LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "", contentInput);
                        TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                        content.setBoost(2.0f);
                        doc.add((IndexableField)content);
                        Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                        this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length());
                    }
                    Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove Worksheet from index.");
                    this.removeEntireWorksheet(worksheetid, worksheetversionid);
                }
                catch (Exception e) {
                    try {
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length(), e.getMessage());
                        Trace.logError("INDEXER", (Object)("Failed to index worksheet section record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
                    }
                    catch (SapphireException se) {
                        Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
                    }
                }
                finally {
                    this.dbu.closeResultSet("worksheet");
                }
            } else {
                try {
                    String worksheetitemid = this.getValue("worksheetitemid");
                    String worksheetitemversionid = this.getValue("worksheetitemversionid");
                    this.dbu.createPreparedResultSet("worksheet", "SELECT * FROM worksheet, worksheetitem WHERE worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND worksheetitem.worksheetitemid=? AND worksheetitem.worksheetitemversionid=?", new String[]{worksheetitemid, worksheetitemversionid});
                    this.dbu.getNext("worksheet");
                    String worksheetid = this.dbu.getString("worksheet", "worksheetid");
                    String worksheetversionid = this.dbu.getString("worksheet", "worksheetversionid");
                    if (this.sdcConditionMatch("LV_Worksheet", "worksheet")) {
                        String correctedKeyid1 = this.substituteChars(worksheetitemid);
                        String correctedKeyid2 = this.substituteChars(worksheetitemversionid);
                        String correctedKeyid3 = "";
                        id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                        mapId = "SDI;" + sdcid + ";" + worksheetitemid + ";" + worksheetitemversionid + ";" + "";
                        Document doc = new Document();
                        this.addKeyFields(id, "SDI", sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                        this.addAuditFields(doc);
                        String desccolvalue = this.getValue("worksheetitemdesc");
                        this.dbu.createPreparedResultSet("worksheetitem", "SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
                        DataSet item = new DataSet();
                        item.setResultSet(this.dbu.getResultSet("worksheetitem"), true, this.dbu.getDbms());
                        if (item.size() == 1) {
                            WorksheetItem worksheetItem = WorksheetItemFactory.getIndexingInstance(this.dbu, (HashMap)item.get(0));
                            String indexingText = worksheetItem.getIndexingText();
                            if (indexingText != null && indexingText.length() > 0) {
                                contentInput.append(" ").append(indexingText);
                            }
                            if (desccolvalue.length() == 0) {
                                desccolvalue = worksheetItem.getName(true);
                            }
                        }
                        doc.add((IndexableField)new StringField("desccol", desccolvalue, Field.Store.YES));
                        Trace.logDebug("INDEXER", "Desc col: worksheetitemdesc");
                        doc.add((IndexableField)new StringField("singular", "worksheet item", Field.Store.YES));
                        Trace.logDebug("INDEXER", "Singular: worksheet item");
                        doc.add((IndexableField)new StringField("worksheetid", this.substituteChars(this.getValue("worksheetid")), Field.Store.YES));
                        doc.add((IndexableField)new StringField("worksheetversionid", this.substituteChars(this.getValue("worksheetversionid")), Field.Store.YES));
                        if (contentInput.length() > 0) {
                            contentInput.append(" ");
                        }
                        contentInput.append(worksheetitemid);
                        contentInput.append(" ").append(worksheetitemversionid);
                        contentInput.append(" ").append(this.getValue("worksheetitemdesc"));
                        contentInput.append(" ").append(this.getValue("itemstatus"));
                        contentInput.append(" ").append(this.getValue("templateid"));
                        contentInput.append(" ").append(this.getValue("templateversionid"));
                        this.addAttributes("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "", contentInput);
                        TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                        content.setBoost(2.0f);
                        doc.add((IndexableField)content);
                        Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                        this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length());
                    } else {
                        Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove Worksheet from index.");
                        this.removeEntireWorksheet(worksheetid, worksheetversionid);
                    }
                }
                catch (Exception e) {
                    try {
                        this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length(), e.getMessage());
                        Trace.logError("INDEXER", (Object)("Failed to index worksheet item record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
                    }
                    catch (SapphireException se) {
                        Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
                    }
                }
                finally {
                    this.dbu.closeResultSet("worksheet");
                }
            }
        }
    }

    public void delete(String keyid1, String keyid2) {
        String sdcid = this.sdcProps.getProperty("sdcid");
        this.delete(sdcid, keyid1, keyid2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeEntireWorksheet(String worksheetid, String worksheetversionid) {
        ConnectionInfo connectionInfo = new ConnectionInfo(this.indexer.getSapphireDatabase());
        try {
            Indexer.removeSDI(connectionInfo, "LV_Worksheet", worksheetid, worksheetversionid, "");
            this.dbu.createPreparedResultSet("worksheetsection", "SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=?", new String[]{worksheetid, worksheetversionid});
            while (this.dbu.getNext("worksheetsection")) {
                String worksheetsectionid = this.dbu.getString("worksheetsection", "worksheetsectionid");
                String worksheetsectionversionid = this.dbu.getString("worksheetsection", "worksheetsectionversionid");
                Indexer.removeSDI(connectionInfo, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "");
            }
            this.dbu.createPreparedResultSet("worksheetitem", "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid=? AND worksheetversionid=?", new String[]{worksheetid, worksheetversionid});
            while (this.dbu.getNext("worksheetitem")) {
                String worksheetitemid = this.dbu.getString("worksheetitem", "worksheetitemid");
                String worksheetitemversionid = this.dbu.getString("worksheetitem", "worksheetitemversionid");
                Indexer.removeSDI(connectionInfo, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "");
            }
        }
        catch (SapphireException e) {
            Trace.logError("INDEXER", (Object)"Failed to remove worksheet & it's detail SDIs", e);
        }
        finally {
            this.dbu.closeResultSet("worksheetsection");
            this.dbu.closeResultSet("worksheetitem");
        }
    }

    private void delete(String sdcid, String keyid1, String keyid2) {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        try {
            String correctedKeyid1 = this.substituteChars(keyid1);
            String correctedKeyid2 = this.substituteChars(keyid2);
            String correctedKeyid3 = "";
            id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
            mapId = "SDI;" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + "";
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", sdcid, 0L, e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to delete worksheet index record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to delete indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }
}

