/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentCodes;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Search
extends BaseDocumentCommand
implements DocumentCommand {
    public Search(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String hostpageid = requestData.getProperty("hostpageid");
        String searchrows = requestData.getProperty("searchrows", "10000");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_Document");
        sdiRequest.setVersionStatus("P");
        sdiRequest.setRequestItem("primary[documentid, documentversionid, documentdesc, createdt, createby, moddt, modby, versionstatus, formid, formversionid, documentstatus, statusmessage, sysuserid1, sysuserid2, ddeflag, priority, duedt]");
        StringBuffer queryfrom = new StringBuffer();
        StringBuffer querywhere = new StringBuffer();
        StringBuffer queryorderby = new StringBuffer();
        PropertyList searchObjects = new PropertyList();
        String searchtype = requestData.getProperty("type");
        if ("query".equalsIgnoreCase(searchtype)) {
            sdiRequest.setQueryid(requestData.getProperty("queryid"));
            PropertyListCollection queryargs = requestData.getCollection("queryargs");
            if (queryargs != null && queryargs.size() > 0) {
                String[] queryparams = new String[queryargs.size()];
                for (int i = 0; i < queryargs.size(); ++i) {
                    queryparams[i] = queryargs.getPropertyList(i).getProperty("argvalue");
                }
                sdiRequest.setQueryParams(queryparams);
            }
        } else {
            if ("formsearch".equalsIgnoreCase(searchtype)) {
                try {
                    String formid = requestData.getProperty("formid");
                    String formversionid = requestData.getProperty("formversionid");
                    Form form = Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug);
                    PropertyListCollection fields = form.getFields();
                    PropertyList documentValues = requestData.getPropertyList("documentvalues");
                    PropertyList fieldValues = requestData.getPropertyList("fieldvalues");
                    int searchfields = 0;
                    StringBuffer from = new StringBuffer();
                    StringBuffer where = new StringBuffer();
                    boolean wildcardsearch = requestData.getProperty("wildcardsearch", "N").equals("Y");
                    for (int i = 0; i < fields.size(); ++i) {
                        PropertyList field = fields.getPropertyList(i);
                        String fieldid = field.getProperty("fieldid");
                        PropertyListCollection instances = fieldValues.getCollection(fieldid);
                        for (int j = 0; j < instances.size(); ++j) {
                            PropertyList instance = instances.getPropertyList(j);
                            String value = instance.getProperty("enteredtext");
                            if (value.length() <= 0) continue;
                            from.append(", documentfield df").append(++searchfields);
                            where.append(" AND df").append(searchfields).append(".fieldid='").append(fieldid).append("' AND ");
                            if (wildcardsearch) {
                                where.append("df").append(searchfields).append(".enteredtext like '%").append(value).append("%'");
                            } else {
                                where.append("df").append(searchfields).append(".enteredtext = '").append(value).append("'");
                            }
                            if (searchfields <= 1) continue;
                            where.append(" AND df").append(searchfields - 1).append(".documentid=df").append(searchfields).append(".documentid");
                        }
                    }
                    queryfrom.append("document, form");
                    querywhere.append("document.formid = form.formid AND document.formversionid = form.formversionid AND ");
                    querywhere.append("document.formid = '").append(SafeSQL.encodeForSQL(formid, this.sapphireConnection.isOracle())).append("' AND document.formversionid = '").append(SafeSQL.encodeForSQL(formversionid, this.sapphireConnection.isOracle())).append("' ");
                    if (documentValues != null && documentValues.size() > 0) {
                        SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
                        PropertyListCollection columns = sdcProcessor.getColumns("LV_Document");
                        for (int i = 0; i < columns.size(); ++i) {
                            PropertyList column = columns.getPropertyList(i);
                            String columnid = column.getProperty("columnid");
                            if (documentValues.getProperty(columnid).length() <= 0) continue;
                            querywhere.append("AND ").append(columnid).append("=");
                            if (!column.getProperty("datatype").equals("C")) continue;
                            querywhere.append("'").append(SafeSQL.encodeForSQL(documentValues.getProperty(columnid), this.sapphireConnection.isOracle())).append("' ");
                        }
                    }
                    querywhere.append(" AND document.documentid IN ");
                    if (searchfields > 0) {
                        querywhere.append("        (SELECT df1.documentid FROM document d").append(from).append(" WHERE d.documentid = df1.documentid AND d.documentversionid = df1.documentversionid ").append(where).append(") ");
                    } else {
                        querywhere.append("        (SELECT document.documentid from document)");
                    }
                    queryorderby.append("document.moddt DESC");
                    searchObjects.setProperty("formid", formid);
                    searchObjects.setProperty("formversionid", formversionid);
                    searchObjects.setProperty("fieldvalues", fieldValues);
                }
                catch (SapphireException e) {
                    this.logger.error("Failed to search for documents. Exception: " + e.getMessage(), e);
                }
            } else {
                queryfrom.append(EncryptDecrypt.unobfsql(requestData.getProperty("queryfrom")));
                querywhere.append(EncryptDecrypt.unobfsql(requestData.getProperty("querywhere")));
                queryorderby.append(EncryptDecrypt.unobfsql(requestData.getProperty("queryorderby")));
            }
            boolean restrictivewhere = requestData.getProperty("restrictivewhere").length() > 0;
            sdiRequest.setQueryFrom(this.evalTokens(requestData, queryfrom.toString()));
            sdiRequest.setQueryWhere(this.evalTokens(requestData, (restrictivewhere ? "(" : "") + querywhere.toString() + (restrictivewhere ? ") AND (" + EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere")) + ")" : "")));
            sdiRequest.setQueryOrderBy(this.evalTokens(requestData, queryorderby.toString()));
        }
        long start = System.currentTimeMillis();
        HashMap<String, String> responseData = new HashMap<String, String>();
        searchObjects.setProperty("searchid", requestData.getProperty("searchid"));
        searchObjects.setProperty("type", searchtype);
        searchObjects.setProperty("searchtitle", requestData.getProperty("searchtitle"));
        searchObjects.setProperty("searchargs", requestData.getCollection("queryargs"));
        searchObjects.setProperty("searchvalue", requestData.getProperty("currentsearchvalue"));
        searchObjects.setProperty("searchrows", searchrows);
        SDIData docData = new SDIProcessor(this.sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
        if (docData != null) {
            boolean docstatus;
            DataSet hits = docData.getDataset("primary");
            this.logger.info("Search: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            DataSet docs = new DataSet();
            String groupby = requestData.getProperty("groupby");
            int maxdocs = Integer.parseInt(searchrows);
            boolean bl = docstatus = hits.getColumnType("documentstatus") != -1;
            if (docstatus) {
                docs.addColumn("documentstatustext", 0);
            }
            if (groupby.equals("duedate") || groupby.equals("createdate") || groupby.equals("moddate")) {
                docs.addColumn("datesort", 0);
                docs.addColumn(groupby, 0);
            }
            DateTimeUtil dtu = new DateTimeUtil(this.sapphireConnection);
            String[] days = new String[]{this.trans.translate("Sunday"), this.trans.translate("Monday"), this.trans.translate("Tuesday"), this.trans.translate("Wednesday"), this.trans.translate("Thursday"), this.trans.translate("Friday"), this.trans.translate("Saturday")};
            String[] datetexts = new String[]{this.trans.translate("Today"), this.trans.translate("Yesterday"), "", "", "", "", "", this.trans.translate("Last Week"), this.trans.translate("Two Weeks Ago"), this.trans.translate("Three Weeks Ago"), this.trans.translate("Last Month")};
            String[] dateoffsets = new String[]{"0", "-1d", "-1d", "-1d", "-1d", "-1d", "-1d", "-7d", "-7d", "-7d", "-1m"};
            String lastgroupid = "__(unknown)__";
            int groups = 0;
            for (int i = 0; i < hits.size() && i < maxdocs; ++i) {
                docs.copyRow(hits, i, 1);
                if (docstatus) {
                    docs.setString(i, "documentstatustext", DocumentCodes.getDocumentStatusText(docs.getValue(i, "documentstatus")));
                }
                if (groupby.length() > 0) {
                    String groupid = docs.getValue(i, groupby);
                    if (!lastgroupid.equals(groupid)) {
                        ++groups;
                        lastgroupid = groupid;
                    }
                    if (groupby.equals("duedate") || groupby.equals("createdate") || groupby.equals("moddate")) {
                        Calendar dbdate = docs.getCalendar(i, groupby.equals("createdate") ? "createdt" : (groupby.equals("moddate") ? "moddt" : "duedt"));
                        docs.setString(i, "datesort", dbdate != null ? String.valueOf(dbdate.getTimeInMillis()) : "");
                        Calendar date = dtu.getCalendar("today");
                        int dow = date.get(7) - 1;
                        String datetext = null;
                        int index = 0;
                        while (index < datetexts.length && datetext == null) {
                            if (dateoffsets[index].equals("-1d")) {
                                date.add(6, -1);
                            } else if (dateoffsets[index].equals("-7d")) {
                                date.add(6, -7);
                            } else if (dateoffsets[index].equals("-1m")) {
                                date.add(2, -1);
                            }
                            if (dbdate == null) {
                                datetext = "N/A";
                            } else if (dbdate.after(date)) {
                                String string = datetext = datetexts[index].equals("") ? days[date.get(7) - 1] : datetexts[index];
                            }
                            if (index >= dow && index < 7) {
                                index += 7 - dow;
                                continue;
                            }
                            ++index;
                        }
                        if (datetext == null) {
                            datetext = this.trans.translate("Older");
                        }
                        docs.setString(i, groupby, datetext);
                    }
                }
                docs.setString(i, "thumbnailhtml", Document.getDocumentThumbnail());
            }
            if (groupby.length() > 0) {
                docs.sort((groupby.equals("duedate") || groupby.equals("createdate") || groupby.equals("moddate") ? "datesort D" : groupby) + "," + requestData.getProperty("sortby", "documentid") + " " + requestData.getProperty("sortbydir", "A"));
            }
            this.logger.info("Search hits: " + hits.size());
            this.logger.info("Search docs: " + docs.size());
            this.logger.info("Search groups: " + groups);
            this.logger.info("Search post process: " + (System.currentTimeMillis() - start));
            searchObjects.setProperty("hits", String.valueOf(hits.size()));
            searchObjects.setProperty("groups", String.valueOf(groups));
            searchObjects.setProperty("documents", JSONUtil.toJSONString(docs));
        } else {
            searchObjects.setProperty("status", "E");
        }
        this.debugReturn(requestData, searchObjects);
        responseData.put("jsonreturn", searchObjects.toJSONString(false));
        responseData.put("userconfig_efm_" + hostpageid + "_lastsearchid", requestData.getProperty("searchid"));
        responseData.put("userconfig_efm_" + hostpageid + "_lastsearchvalue", requestData.getProperty("currentsearchvalue"));
        return responseData;
    }
}

