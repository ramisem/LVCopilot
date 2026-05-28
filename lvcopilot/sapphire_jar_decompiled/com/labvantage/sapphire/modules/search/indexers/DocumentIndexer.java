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
import com.labvantage.sapphire.modules.search.indexers.Index;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentCodes;
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

public class DocumentIndexer
extends BaseIndexer
implements Index {
    public DocumentIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps, String resultsetName) {
        super(indexer, dbu, indexWriter, sdcProps, resultsetName);
    }

    public DocumentIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps) {
        super(indexer, dbu, indexWriter, sdcProps);
    }

    @Override
    public void indexSet() {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String sdcid = "LV_Document";
        String id = null;
        String mapId = "";
        StringBuffer contentInput = new StringBuffer();
        try {
            if (this.sdcConditionMatch(sdcid)) {
                String documentid = this.getValue("documentid");
                String documentversionid = this.getValue("documentversionid");
                String correctedKeyid1 = this.substituteChars(documentid);
                String correctedKeyid2 = this.substituteChars(documentversionid);
                String correctedKeyid3 = "";
                id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                mapId = "SDI;" + sdcid + ";" + documentid + ";" + documentversionid + ";" + "";
                Document doc = new Document();
                this.addKeyFields(id, "SDI", sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                this.addAuditFields(doc);
                doc.add((IndexableField)new StringField("desccol", this.getValue("documentdesc"), Field.Store.YES));
                Trace.logDebug("INDEXER", "Desc col: documentdesc");
                doc.add((IndexableField)new StringField("singular", "document", Field.Store.YES));
                Trace.logDebug("INDEXER", "Singular: document");
                contentInput.append(documentid);
                contentInput.append(" ").append(documentversionid);
                contentInput.append(" ").append(DocumentCodes.getDocumentStatusText(this.getValue("documentstatus")));
                contentInput.append(" ").append(this.getValue("statusmessage"));
                contentInput.append(" ").append(this.getValue("formid"));
                contentInput.append(" ").append(this.getValue("formversionid"));
                contentInput.append(" ").append(this.getValue("sysuserid1"));
                contentInput.append(" ").append(this.getValue("sysuserid2"));
                this.dbu.createPreparedResultSet("documentfields", "SELECT fieldid, enteredtext, displayvalue, numericvalue FROM documentfield WHERE documentid = ? AND documentversionid = ?", new Object[]{documentid, documentversionid});
                while (this.dbu.getNext("documentfields")) {
                    String fieldid = this.dbu.getValue("documentfields", "fieldid");
                    String enteredtext = this.dbu.getValue("documentfields", "enteredtext");
                    String displayvalue = this.dbu.getValue("documentfields", "displayvalue");
                    if (enteredtext.length() <= 0 && displayvalue.length() <= 0) continue;
                    contentInput.append(" ").append(fieldid);
                    contentInput.append(" ").append(enteredtext);
                    contentInput.append(" ").append(displayvalue);
                    doc.add((IndexableField)new StringField(fieldid, (displayvalue.length() > 0 ? displayvalue : enteredtext).toLowerCase(), Field.Store.YES));
                }
                this.dbu.createPreparedResultSet("documentreviewitems", "SELECT * FROM documentreviewitem WHERE documentid = ? AND documentversionid = ?", new Object[]{documentid, documentversionid});
                while (this.dbu.getNext("documentreviewitems")) {
                    if (this.dbu.getValue("documentreviewitems", "reviewitemtext").length() > 0) {
                        contentInput.append(" ").append(this.dbu.getValue("documentreviewitems", "reviewitemtext"));
                    }
                    if (this.dbu.getValue("documentreviewitems", "resolvedtext").length() <= 0) continue;
                    contentInput.append(" ").append(this.dbu.getValue("documentreviewitems", "resolvedtext"));
                }
                TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                content.setBoost(2.0f);
                doc.add((IndexableField)content);
                Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length());
            } else {
                Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove Document from index.");
                String keyId1 = this.getValue("documentid");
                String keyId2 = this.getValue("documentversionid");
                this.delete(keyId1, keyId2);
            }
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length(), e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to index primary record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to update indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }

    public void delete(String documentid, String documentversionid) {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String sdcid = "LV_Document";
        String id = null;
        String mapId = "";
        try {
            String correctedKeyid1 = this.substituteChars(documentid);
            String correctedKeyid2 = this.substituteChars(documentversionid);
            String correctedKeyid3 = "";
            id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
            mapId = "SDI;" + sdcid + ";" + documentid + ";" + documentversionid + ";" + "";
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", sdcid, 0L, e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to delete document index record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to delete indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }
}

