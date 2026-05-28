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
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class NoteIndexer
extends BaseIndexer {
    private String sdcid;

    public NoteIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, String sdcid, String resultsetName) {
        super(indexer, dbu, indexWriter, null, resultsetName);
        this.sdcid = sdcid;
    }

    public NoteIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter) {
        super(indexer, dbu, indexWriter, null);
    }

    @Override
    public void indexSet() {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        StringBuffer contentInput = new StringBuffer();
        try {
            String keyId1 = this.getValue("keyid1");
            String keyId2 = this.getValue("keyid2");
            String keyId3 = this.getValue("keyid3");
            int noteNum = this.getInt("notenum");
            if (this.sdcConditionMatch(this.sdcid)) {
                String correctedKeyid1 = this.substituteChars(this.getValue("keyid1"));
                String correctedKeyid2 = this.substituteChars(this.getValue("keyid2"));
                String correctedKeyid3 = this.substituteChars(this.getValue("keyid3"));
                id = "NOTE;" + this.sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3 + ";" + this.getValue("notenum");
                mapId = "NOTE;" + this.sdcid + ";" + this.getValue("keyid1") + ";" + this.getValue("keyid2") + ";" + this.getValue("keyid3") + ";" + this.getValue("notenum");
                Document doc = new Document();
                this.addKeyFields(id, "NOTE", this.sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                contentInput.append(this.getClob("note"));
                if (contentInput != null) {
                    doc.add((IndexableField)new StringField("desccol", contentInput.substring(0, contentInput.length() > 40 ? 40 : contentInput.length()).toLowerCase(), Field.Store.YES));
                    doc.add((IndexableField)new TextField("content", (Reader)new BufferedReader(new StringReader(contentInput.toString()))));
                }
                this.addAuditFields(doc);
                PropertyList sdcPolicy = this.indexer.getSDCPolicy(this.sdcid);
                if (sdcPolicy.getProperty("childsdc").equals("Y")) {
                    doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid1"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid1"))), Field.Store.YES));
                    if (sdcPolicy.getProperty("parentkeycolid2").length() > 0) {
                        doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid2"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid2"))), Field.Store.YES));
                    }
                    if (sdcPolicy.getProperty("parentkeycolid3").length() > 0) {
                        doc.add((IndexableField)new StringField(sdcPolicy.getProperty("parentkeycolid3"), this.substituteChars(this.getValue(sdcPolicy.getProperty("parentkeycolid3"))), Field.Store.YES));
                    }
                }
                this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                this.updateIndexMap(mapId, now, "NOTE", this.sdcid, contentInput.length());
            } else {
                Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove Note from index.");
                this.delete(this.sdcid, keyId1, keyId2, keyId3, noteNum);
            }
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "NOTE", this.sdcid, contentInput.length(), e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to index sdinote '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }

    public void delete(String sdcid, String keyid1, String keyid2, String keyid3, int noteNum) {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String id = null;
        String mapId = "";
        try {
            String correctedKeyid1 = this.substituteChars(keyid1);
            String correctedKeyid2 = this.substituteChars(keyid2);
            String correctedKeyid3 = this.substituteChars(keyid3);
            id = "NOTE;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3 + ";" + noteNum;
            mapId = "NOTE;" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + noteNum;
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "NOTE", sdcid, 0L, e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to delete index sdinote '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to delete indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }
}

