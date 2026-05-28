/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.lucene.document.Document
 *  org.apache.lucene.document.Field$Store
 *  org.apache.lucene.document.StringField
 *  org.apache.lucene.index.IndexWriter
 *  org.apache.lucene.index.IndexableField
 *  org.apache.lucene.index.Term
 */
package com.labvantage.sapphire.modules.search.indexers;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.Index;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseIndexer
implements Index {
    protected Indexer indexer;
    protected DBUtil dbu;
    protected IndexWriter indexWriter;
    protected PropertyList sdcProps;
    private String resultsetName;

    public BaseIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps) {
        this.indexer = indexer;
        this.dbu = dbu;
        this.indexWriter = indexWriter;
        this.sdcProps = sdcProps;
    }

    public BaseIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps, String resultsetName) {
        this.indexer = indexer;
        this.dbu = dbu;
        this.indexWriter = indexWriter;
        this.sdcProps = sdcProps;
        this.resultsetName = resultsetName;
    }

    protected boolean sdcConditionMatch(String sdcid) throws SapphireException {
        return this.sdcConditionMatch(sdcid, this.resultsetName);
    }

    protected boolean sdcConditionMatch(String sdcid, String resultsetid) throws SapphireException {
        boolean match;
        PropertyList sdcPolicy = this.indexer.getSDCPolicy(sdcid);
        boolean andOperator = sdcPolicy.getProperty("conditionsoperator", "AND").equals("AND");
        boolean orOperator = !andOperator;
        PropertyListCollection conditions = sdcPolicy.getCollection("conditions");
        boolean bl = match = !orOperator;
        if (conditions != null) {
            for (int i = 0; i < conditions.size(); ++i) {
                PropertyList condition = conditions.getPropertyList(i);
                if (condition.getProperty("columnid").length() <= 0) continue;
                String valueoperator = condition.getProperty("valueoperator");
                String value1 = condition.getProperty("value1");
                String value2 = condition.getProperty("value2");
                Object columnvalue = this.getObject(resultsetid, condition.getProperty("columnid"));
                if (ConditionItem.conditionMatch(columnvalue, value1, value2, valueoperator)) {
                    if (!orOperator) continue;
                    match = true;
                } else {
                    if (!andOperator) continue;
                    match = false;
                }
                break;
            }
        } else {
            match = true;
        }
        return match;
    }

    protected Object getObject(String columnid) throws SapphireException {
        return this.dbu.getObject(this.resultsetName, columnid);
    }

    protected Object getObject(String overrideResultsetName, String columnid) throws SapphireException {
        return this.dbu.getObject(overrideResultsetName, columnid);
    }

    protected String getValue(String columnid) throws SapphireException {
        return this.dbu.getValue(this.resultsetName, columnid);
    }

    protected int getInt(String columnid) throws SapphireException {
        return this.dbu.getInt(this.resultsetName, columnid);
    }

    protected String getValue(String columnid, String defaultValue) throws SapphireException {
        String value = this.dbu.getValue(this.resultsetName, columnid);
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    protected String getClob(String columnid) throws SapphireException {
        return this.dbu.getClob(this.resultsetName, columnid);
    }

    protected Blob getBlob(String columnid) throws SapphireException {
        return this.dbu.getBlob(this.resultsetName, columnid);
    }

    protected Timestamp getTimestamp(String columnid) throws SapphireException {
        return this.dbu.getTimestamp(this.resultsetName, columnid);
    }

    protected ResultSetMetaData getMetaData() throws SapphireException, SQLException {
        return this.dbu.getResultSet(this.resultsetName).getMetaData();
    }

    protected void updateIndexMap(String id, Timestamp indexdt, String indextype, String indexsdcid, long contentSize) throws SapphireException {
        this.updateIndexMap(id, indexdt, indextype, indexsdcid, contentSize, null, null, 0L, "", "");
    }

    protected void updateIndexMap(String id, Timestamp indexdt, String indextype, String indexsdcid, long contentSize, String errortext) throws SapphireException {
        this.updateIndexMap(id, indexdt, indextype, indexsdcid, contentSize, null, null, 0L, "E", errortext);
    }

    protected void updateIndexMap(String id, Timestamp indexdt, String indextype, String indexsdcid, long contentSize, String attachmentType, String attachmentFileType, long attachmentFileSize) throws SapphireException {
        this.updateIndexMap(id, indexdt, indextype, indexsdcid, contentSize, attachmentType, attachmentFileType, attachmentFileSize, "", "");
    }

    protected void updateIndexMap(String id, Timestamp indexdt, String indextype, String indexsdcid, long contentSize, String attachmentType, String attachmentFileType, long attachmentFileSize, String errortext) throws SapphireException {
        this.updateIndexMap(id, indexdt, indextype, indexsdcid, contentSize, attachmentType, attachmentFileType, attachmentFileSize, "E", errortext);
    }

    private void updateIndexMap(String id, Timestamp indexdt, String indextype, String indexsdcid, long contentSize, String attachmentType, String attachmentFileType, long attachmentFileSize, String indexflag, String errortext) throws SapphireException {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        long time = now.getTime() - indexdt.getTime();
        try {
            if (attachmentType != null && attachmentType.length() > 0) {
                if (this.dbu.executePreparedUpdate("UPDATE indexmap SET indexdt = ?, indextype = ?, indexsdcid = ?, indexflag = " + (indexflag.equals("E") ? "'E', " : "NULL, ") + "indextime = ?, contentsize = ?, attachmenttypeflag = ?, attachmentfiletype = ?, attachmentfilesize = ?, errortext = ? WHERE indexitem = ?", new Object[]{indexdt, indextype, indexsdcid, time, contentSize, attachmentType, attachmentFileType, attachmentFileSize, errortext, id}) != 1) {
                    this.dbu.executePreparedUpdate("INSERT INTO indexmap ( indexitem, indexdt, indexflag, indextype, indexsdcid, indextime, contentsize, attachmenttypeflag, attachmentfiletype, attachmentfilesize, errortext ) VALUES ( ?, ?, " + (indexflag.equals("E") ? "'E', " : "NULL, ") + "?, ?, ?, ?, ?, ?, ?, ? )", new Object[]{id, indexdt, indextype, indexsdcid, time, contentSize, attachmentType, attachmentFileType, attachmentFileSize, errortext});
                }
            } else if (this.dbu.executePreparedUpdate("UPDATE indexmap SET indexdt = ?, indextype = ?, indexsdcid = ?, indexflag = " + (indexflag.equals("E") ? "'E', " : "NULL, ") + "indextime = ?, contentsize = ?, errortext = ? WHERE indexitem = ?", new Object[]{indexdt, indextype, indexsdcid, time, contentSize, errortext, id}) != 1) {
                this.dbu.executePreparedUpdate("INSERT INTO indexmap ( indexitem, indexdt, indexflag, indextype, indexsdcid, indextime, contentsize, errortext ) VALUES ( ?, ?, " + (indexflag.equals("E") ? "'E', " : "NULL, ") + "?, ?, ?, ?, ? )", new Object[]{id, indexdt, indextype, indexsdcid, time, contentSize, errortext});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to update indexmap for indexitem '" + id + "'. Reason: " + e.getMessage(), e);
        }
    }

    protected void deleteIndexItem(String id, String mapId) throws IOException, SapphireException {
        this.indexWriter.deleteDocuments(new Term[]{new Term("id", id)});
        this.dbu.executePreparedUpdate("DELETE FROM indexmap WHERE indexitem = ?", new Object[]{mapId});
    }

    protected void addKeyFields(String id, String type, String sdcid, String keyid1, String keyid2, String keyid3, Document doc) {
        Trace.logInfo("INDEXER", "Indexing " + type + ": " + sdcid + " " + keyid1 + " " + keyid2 + " " + keyid3);
        doc.add((IndexableField)new StringField("id", id, Field.Store.YES));
        doc.add((IndexableField)new StringField("type", type.toLowerCase(), Field.Store.YES));
        doc.add((IndexableField)new StringField("sdcid", sdcid.toLowerCase(), Field.Store.YES));
        doc.add((IndexableField)new StringField("keyid1", keyid1.toLowerCase(), Field.Store.YES));
        if (keyid2.length() > 0) {
            doc.add((IndexableField)new StringField("keyid2", keyid2.toLowerCase(), Field.Store.YES));
        }
        if (keyid3.length() > 0) {
            doc.add((IndexableField)new StringField("keyid3", keyid3.toLowerCase(), Field.Store.YES));
        }
    }

    protected void addAuditFields(Document doc) throws SapphireException {
        doc.add((IndexableField)new StringField("createby", this.getValue("createby").toLowerCase(), Field.Store.YES));
        doc.add((IndexableField)new StringField("modby", this.getValue("modby").toLowerCase(), Field.Store.YES));
        this.addDateField(this.getTimestamp("createdt"), doc, "createdt");
        this.addDateField(this.getTimestamp("moddt"), doc, "moddt");
    }

    protected void addDateField(Timestamp timestamp, Document doc, String fieldid) throws SapphireException {
        if (timestamp != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            doc.add((IndexableField)new StringField(fieldid, sdf.format(cal.getTime()), Field.Store.YES));
        }
    }

    protected String substituteChars(String input) {
        return StringUtil.replaceAll(input, "-", "__");
    }

    protected void addAttributes(String sdcId, String keyId1, String keyId2, String keyId3, StringBuffer contentInput) throws SapphireException {
        this.dbu.createPreparedResultSet("sdiattributes", "SELECT attributeid, textvalue FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attributesdcid = ?", new Object[]{sdcId, keyId1, keyId2 != null && keyId2.length() > 0 ? keyId2 : "(null)", keyId3 != null && keyId3.length() > 0 ? keyId3 : "(null)", sdcId});
        while (this.dbu.getNext("sdiattributes")) {
            contentInput.append(" ").append(this.dbu.getValue("sdiattributes", "textvalue"));
        }
    }
}

