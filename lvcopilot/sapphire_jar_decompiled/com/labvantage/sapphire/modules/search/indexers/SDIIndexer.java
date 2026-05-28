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
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.search.indexers;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import com.labvantage.sapphire.modules.search.indexers.Index;
import com.labvantage.sapphire.xml.Column;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIIndexer
extends BaseIndexer
implements Index {
    private int keycols;
    private String sdcid;
    private String keycolid1;
    private String keycolid2;
    private String keycolid3;
    private String singular;
    private String desccol;
    private PropertyListCollection searchColumns;

    public SDIIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps, String resultsetName) {
        super(indexer, dbu, indexWriter, sdcProps, resultsetName);
        this.keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        this.sdcid = sdcProps.getProperty("sdcid");
        this.keycolid1 = sdcProps.getProperty("keycolid1");
        this.keycolid2 = sdcProps.getProperty("keycolid2");
        this.keycolid3 = sdcProps.getProperty("keycolid3");
        this.singular = sdcProps.getProperty("singular");
        this.desccol = sdcProps.getProperty("desccol");
        PropertyList sdcPolicy = indexer.getSDCPolicy(this.sdcid);
        this.searchColumns = sdcPolicy.getCollection("searchcolumns");
    }

    public SDIIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps) {
        this(indexer, dbu, indexWriter, sdcProps, "");
    }

    @Override
    public void indexSet() {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        StringBuffer contentInput = new StringBuffer();
        try {
            if (this.sdcConditionMatch(this.sdcid)) {
                int i;
                String correctedKeyid1 = this.substituteChars(this.getValue(this.keycolid1));
                String correctedKeyid2 = this.keycols > 1 ? this.substituteChars(this.getValue(this.keycolid2)) : "";
                String correctedKeyid3 = this.keycols > 2 ? this.substituteChars(this.getValue(this.keycolid3)) : "";
                id = "SDI;" + this.sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                mapId = "SDI;" + this.sdcid + ";" + this.getValue(this.keycolid1) + ";" + (this.keycols > 1 ? this.getValue(this.keycolid2) : "") + ";" + (this.keycols > 2 ? this.getValue(this.keycolid3) : "");
                org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                this.addKeyFields(id, "SDI", this.sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                this.addAuditFields(doc);
                if (!this.indexer.isColumnExcluded(this.sdcid, this.desccol)) {
                    doc.add((IndexableField)new StringField("desccol", this.getValue(this.desccol), Field.Store.YES));
                    Trace.logDebug("INDEXER", "Desc col excluded: " + this.desccol);
                } else {
                    doc.add((IndexableField)new StringField("desccol", "", Field.Store.YES));
                    Trace.logDebug("INDEXER", "Desc col: " + this.desccol);
                }
                doc.add((IndexableField)new StringField("singular", this.singular.toLowerCase(), Field.Store.YES));
                Trace.logDebug("INDEXER", "Singular: " + this.singular.toLowerCase());
                ResultSetMetaData rsmd = this.getMetaData();
                int cols = rsmd.getColumnCount();
                for (i = 0; i < cols; ++i) {
                    String columnid = rsmd.getColumnName(i + 1);
                    if (Column.isAuditColumn(columnid) || this.indexer.isColumnExcluded(this.sdcid, columnid)) continue;
                    String value = null;
                    switch (rsmd.getColumnType(i + 1)) {
                        case -9: 
                        case 1: 
                        case 12: {
                            value = this.getValue(columnid);
                            break;
                        }
                        case -16: 
                        case -1: 
                        case 2005: {
                            value = this.getClob(columnid);
                            try {
                                Document jdoc = Jsoup.parse((String)value);
                                value = jdoc.body().text();
                            }
                            catch (Exception jdoc) {}
                            break;
                        }
                    }
                    if (value == null || value.length() <= 1) continue;
                    contentInput.append(" ").append(value);
                }
                if (this.searchColumns != null) {
                    for (i = 0; i < this.searchColumns.size(); ++i) {
                        PropertyList searchColumn = this.searchColumns.getPropertyList(i);
                        String fieldid = searchColumn.getProperty("fieldid");
                        String simplificationrule = searchColumn.getProperty("simplificationrule");
                        String value = this.getValue(searchColumn.getProperty("columnid"));
                        String simplifiedvalue = "";
                        if (simplificationrule.equals("SLCZ")) {
                            int j;
                            for (j = 0; j < value.length() && (value.charAt(j) < '1' || value.charAt(j) > '9'); ++j) {
                            }
                            if (j < value.length()) {
                                simplifiedvalue = value.substring(j);
                                contentInput.append(" ").append(simplifiedvalue);
                            }
                        }
                        if (value.length() <= 0 || fieldid.length() <= 0) continue;
                        doc.add((IndexableField)new StringField(fieldid.toLowerCase(), value.toLowerCase(), Field.Store.YES));
                        if (simplifiedvalue.length() > 0) {
                            doc.add((IndexableField)new StringField(fieldid.toLowerCase(), simplifiedvalue.toLowerCase(), Field.Store.YES));
                        }
                        Trace.logDebug("INDEXER", "Search col: " + searchColumn.getProperty("columnid") + "=" + value.toLowerCase());
                    }
                }
                this.addAttributes(this.sdcid, this.getValue(this.keycolid1), this.keycols > 1 ? this.getValue(this.keycolid2) : "", this.keycols > 2 ? this.getValue(this.keycolid3) : "", contentInput);
                TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                content.setBoost(2.0f);
                doc.add((IndexableField)content);
                Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                this.updateIndexMap(mapId, now, "SDI", this.sdcid, contentInput.length());
            } else {
                Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove SDI from index.");
                String keyId1 = this.getValue(this.keycolid1);
                String keyId2 = this.keycols > 1 ? this.getValue(this.keycolid2) : "";
                String keyId3 = this.keycols > 2 ? this.getValue(this.keycolid3) : "";
                this.removeSDI(keyId1, keyId2, keyId3);
            }
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", this.sdcid, contentInput.length(), e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to index primary record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }

    private void removeSDI(String keyid1, String keyid2, String keyid3) {
        ConnectionInfo connectionInfo = new ConnectionInfo(this.indexer.getSapphireDatabase());
        Indexer.removeSDI(connectionInfo, this.sdcid, keyid1, keyid2, keyid3);
    }

    public void delete(String keyid1, String keyid2, String keyid3) {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        try {
            String correctedKeyid1 = this.substituteChars(keyid1);
            String correctedKeyid2 = this.keycols > 1 ? this.substituteChars(keyid2) : "";
            String correctedKeyid3 = this.keycols > 2 ? this.substituteChars(keyid3) : "";
            id = "SDI;" + this.sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
            mapId = "SDI;" + this.sdcid + ";" + keyid1 + ";" + (this.keycols > 1 ? keyid2 : "") + ";" + (this.keycols > 2 ? keyid3 : "");
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", this.sdcid, 0L, e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to delete index primary record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to delete indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }
}

