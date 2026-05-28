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
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.indexers.BaseIndexer;
import com.labvantage.sapphire.modules.search.indexers.Index;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerCodes;
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
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.SDIList;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskExecIndexer
extends BaseIndexer
implements Index {
    public TaskExecIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps, String resultsetName) {
        super(indexer, dbu, indexWriter, sdcProps, resultsetName);
    }

    public TaskExecIndexer(Indexer indexer, DBUtil dbu, IndexWriter indexWriter, PropertyList sdcProps) {
        super(indexer, dbu, indexWriter, sdcProps);
    }

    @Override
    public void indexSet() {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String sdcid = "LV_TaskExec";
        String id = null;
        String mapId = "";
        StringBuffer contentInput = new StringBuffer();
        try {
            if (this.sdcConditionMatch(sdcid)) {
                String taskexecid = this.getValue("taskexecid");
                String correctedKeyid1 = this.substituteChars(taskexecid);
                String correctedKeyid2 = "";
                String correctedKeyid3 = "";
                id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
                mapId = "SDI;" + sdcid + ";" + taskexecid + ";" + "" + ";" + "";
                Document doc = new Document();
                this.addKeyFields(id, "SDI", sdcid, correctedKeyid1, correctedKeyid2, correctedKeyid3, doc);
                this.addAuditFields(doc);
                doc.add((IndexableField)new StringField("desccol", this.getValue("taskexecdesc"), Field.Store.YES));
                Trace.logDebug("INDEXER", "Desc col: taskexecdesc");
                doc.add((IndexableField)new StringField("singular", "taskexec", Field.Store.YES));
                Trace.logDebug("INDEXER", "Singular: taskexec");
                contentInput.append(taskexecid);
                contentInput.append(" ").append(WorkflowManagerCodes.getWorkflowExecStatusText(this.getValue("execstatus")));
                contentInput.append(" ").append(this.getValue("taskdefid"));
                contentInput.append(" ").append(this.getValue("taskdefversionid"));
                contentInput.append(" ").append(this.getValue("taskdefvariantid"));
                contentInput.append(" ").append(this.getValue("workflowexecid"));
                contentInput.append(" ").append(this.getValue("assignedanalyst"));
                contentInput.append(" ").append(this.getValue("assigneddepartment"));
                contentInput.append(" ").append(this.getValue("assignedrole"));
                contentInput.append(" ").append(this.getValue("summary"));
                this.addDateField(this.getTimestamp("startdt"), doc, "startdt");
                this.addDateField(this.getTimestamp("completedt"), doc, "completedt");
                this.dbu.createPreparedResultSet("TaskExecTaskDef", "SELECT taskdef FROM taskdef WHERE taskdefid = ? AND taskdefversionid = ? AND taskdefvariantid = ?", new Object[]{this.getValue("taskdefid"), this.getValue("taskdefversionid"), this.getValue("taskdefvariantid")});
                if (this.dbu.getNext("TaskExecTaskDef")) {
                    PropertyList taskdef = new PropertyList();
                    taskdef.setPropertyList(this.dbu.getClob("TaskExecTaskDef", "taskdef"));
                    PropertyListCollection variablesDef = taskdef.getCollection("variables");
                    if (variablesDef != null) {
                        PropertyList taskexec = new PropertyList();
                        taskexec.setPropertyList(this.getClob("taskexec"));
                        PropertyListCollection variables = taskexec.getCollection("variables");
                        if (variables != null) {
                            variables.index("variableid");
                            for (int i = 0; i < variablesDef.size(); ++i) {
                                PropertyList variableDef = variablesDef.getPropertyList(i);
                                if (!variableDef.getProperty("setup").equals("Y") && !variableDef.getProperty("exposed").equals("Y")) continue;
                                String variableid = variableDef.getProperty("variableid");
                                PropertyList variable = variables.getIndexedPropertyList(variableid);
                                String type = variable.getProperty("type", "string");
                                try {
                                    Object value;
                                    if (type.equalsIgnoreCase("sdilist")) {
                                        value = new SDIList();
                                        ((SDIList)value).setJSONObject(new JSONObject(variable.getProperty("value")));
                                        continue;
                                    }
                                    if (type.equalsIgnoreCase("form")) {
                                        value = new FormValue(variable.getProperty("value"));
                                        continue;
                                    }
                                    value = new JSONableString(variable.getProperty("value")).toString();
                                    if (((String)value).length() <= 0) continue;
                                    contentInput.append(" ").append(variableid);
                                    contentInput.append(" ").append((String)value);
                                    doc.add((IndexableField)new StringField(variableid, ((String)value).toLowerCase(), Field.Store.YES));
                                    continue;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                    }
                }
                TextField content = new TextField("content", (Reader)new BufferedReader(new StringReader(this.substituteChars(contentInput.toString().toLowerCase()))));
                content.setBoost(2.0f);
                doc.add((IndexableField)content);
                Trace.logDebug("INDEXER", "Content: " + contentInput.toString().toLowerCase());
                this.indexWriter.updateDocument(new Term("id", id), (Iterable)doc);
                this.updateIndexMap(mapId, now, "SDI", sdcid, contentInput.length());
            } else {
                Trace.logDebug("INDEXER", "SDC Condition not match for indexing. Remove TaskExec from index.");
                String keyId1 = this.getValue("taskexecid");
                this.delete(keyId1);
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

    public void delete(String taskexecid) {
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String sdcid = "LV_TaskExec";
        String id = null;
        String mapId = "";
        try {
            String correctedKeyid1 = this.substituteChars(taskexecid);
            String correctedKeyid2 = "";
            String correctedKeyid3 = "";
            id = "SDI;" + sdcid + ";" + correctedKeyid1 + ";" + correctedKeyid2 + ";" + correctedKeyid3;
            mapId = "SDI;" + sdcid + ";" + taskexecid + ";" + "" + ";" + "";
            this.deleteIndexItem(id, mapId);
        }
        catch (Exception e) {
            try {
                this.updateIndexMap(mapId, now, "SDI", sdcid, 0L, e.getMessage());
                Trace.logError("INDEXER", (Object)("Failed to delete index record '" + (id != null ? id : "N/A") + "'. Reason: " + e.getMessage()), e);
            }
            catch (SapphireException se) {
                Trace.logError("INDEXER", (Object)("Failed to delete indexmap for '" + (id != null ? id : "N/A") + "'. Reason: " + se.getMessage()), se);
            }
        }
    }
}

