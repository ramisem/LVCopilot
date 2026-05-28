/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 *  org.apache.lucene.index.DirectoryReader
 *  org.apache.lucene.index.IndexNotFoundException
 *  org.apache.lucene.index.IndexReader
 *  org.apache.lucene.queryparser.classic.QueryParser
 *  org.apache.lucene.search.IndexSearcher
 *  org.apache.lucene.search.Query
 *  org.apache.lucene.search.ScoreDoc
 *  org.apache.lucene.search.TopDocs
 *  org.apache.lucene.store.Directory
 *  org.apache.lucene.store.FSDirectory
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.indexers.AttachmentIndexer;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.regex.RegexUtil;
import com.labvantage.sapphire.xml.Column;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.jsp.JspWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Searcher {
    public static final int SEARCHMAX = 500;
    public static final String DEFAULT_SEARCHLIMIT = "500";
    public static final String DEFAULT_COLUMNSIZE = "1000";
    public static final String ADDMATCHING_ALL = "A";
    public static final String ADDMATCHING_FIRST = "F";
    public static final String ADDMATCHING_NONE = "N";
    public static final String DEFAULT_ADDMATCHING = "A";
    public static final String DEFAULT_SHOWAUDITSUMMARY = "Y";
    public static final String DEFAULT_SHOWINTERNALSUMMARY = "N";
    public static final String DEFAULT_SHOWATTACHMENTSNIPPETS = "Y";
    public static final int DEFAULT_ATTACHMENTSNIPPETITEMS = 5;
    public static final int DEFAULT_ATTACHMENTSNIPPETSECTIONS = 5;
    public static final int DEFAULT_MAX_INLINE_ATTACHMENTSIZE = 100000;
    public static final String SEARCHTYPE_QUERY = "Q";
    public static final String SEARCHTYPE_SUGGEST = "S";
    private IndexSearcher indexSearcher;
    private Query query;
    private Indexer indexer;
    private SapphireConnection sapphireConnection;
    private M18NUtil m18n;
    private SDIProcessor sdiProcessor;
    private QueryProcessor queryProcessor;
    private DAMProcessor damProcessor;
    private TranslationProcessor tp;
    private PropertyListCollection sdcRenderRules;
    private String addMatchingColumns = "A";
    private boolean showAuditSummary = true;
    private boolean showInternalSummary = false;
    private boolean showAttachmentSnippets = true;
    private int attachmentSnippetItems = 5;
    private int attachmentSnippetSections = 5;
    private int attachmentSizeLimit = 100000;

    public Searcher(SapphireConnection sapphireConnection) throws SapphireException {
        this(sapphireConnection, null);
    }

    public Searcher(SapphireConnection sapphireConnection, PropertyList searchResults) throws SapphireException {
        try {
            this.sapphireConnection = sapphireConnection;
            this.indexer = Indexer.getInstance(sapphireConnection.getDatabaseId());
            DirectoryReader reader = DirectoryReader.open((Directory)FSDirectory.open((Path)this.indexer.getIndexDir().toPath()));
            this.indexSearcher = new IndexSearcher((IndexReader)reader);
            this.m18n = new M18NUtil(sapphireConnection);
            this.sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
            this.queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
            this.damProcessor = new DAMProcessor(sapphireConnection.getConnectionId());
            this.tp = new TranslationProcessor(sapphireConnection.getConnectionId());
            if (searchResults == null) {
                searchResults = new PropertyList();
            }
            this.addMatchingColumns = searchResults.getProperty("addmatchingcolumns", "A");
            this.showAuditSummary = searchResults.getProperty("showauditsummary", "Y").equals("Y");
            this.showInternalSummary = searchResults.getProperty("showinternalsummary", "N").equals("Y");
            this.showAttachmentSnippets = searchResults.getProperty("showattachmentsnippets", "Y").equals("Y");
            try {
                this.attachmentSnippetItems = Integer.parseInt(searchResults.getProperty("attachmentsnippetitems", String.valueOf(5)));
                this.attachmentSnippetSections = Integer.parseInt(searchResults.getProperty("attachmentsnippetsections", String.valueOf(5)));
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.sdcRenderRules = searchResults.getCollection("sdcs");
            if (this.sdcRenderRules != null) {
                this.sdcRenderRules.index("sdcid");
                for (int i = 0; i < this.sdcRenderRules.size(); ++i) {
                    PropertyList sdc = this.sdcRenderRules.getPropertyList(i);
                    PropertyListCollection resultcolumns = sdc.getCollection("sdiresultcolumns");
                    if (resultcolumns == null) {
                        resultcolumns = new PropertyListCollection();
                    }
                    resultcolumns.index("columnid");
                }
            } else {
                this.sdcRenderRules = new PropertyListCollection();
            }
        }
        catch (IndexNotFoundException e) {
            throw new SapphireException("Index not found - indexing may not have been correctly enabled or the server requires a restart.");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get searcher. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public String getRSet(String sdcid, String searchquery) throws SapphireException {
        try {
            SearchRequest searchRequest = new SearchRequest(searchquery);
            searchRequest.setSdcid(sdcid);
            this.generateSearchQuery(searchRequest);
            SearchResults searchResults = new SearchResults(searchRequest);
            TopDocs topDocs = this.getTopDocs(searchRequest, searchResults);
            SDIList sdiList = new SDIList();
            sdiList.setSdcid(sdcid);
            ScoreDoc[] hits = topDocs.scoreDocs;
            if (hits.length > 0) {
                for (ScoreDoc scoreDoc : hits) {
                    SearchDocument doc = new SearchDocument(this.indexer, null, this.m18n, this.indexSearcher.doc(scoreDoc.doc), scoreDoc.doc, scoreDoc.score);
                    if (doc.isChildSDC()) {
                        sdiList.addSDI(doc.getParentKeyid1(), doc.getParentKeyid2(), doc.getParentKeyid3());
                        continue;
                    }
                    sdiList.addSDI(doc.getKeyid1(), doc.getKeyid2(), doc.getKeyid3());
                }
                return this.damProcessor.createRSet(sdcid, sdiList.getSDIList(SDIList.KeyId.KEYID1), sdiList.getSDIList(SDIList.KeyId.KEYID2), sdiList.getSDIList(SDIList.KeyId.KEYID3));
            }
            return "_xxx";
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get searcher. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
    }

    public SearchResults getSearchResults(SearchRequest searchRequest) throws SapphireException {
        SearchResults searchResults = new SearchResults(searchRequest);
        long startTime = System.currentTimeMillis();
        try {
            SDIList sdiList;
            this.generateSearchQuery(searchRequest);
            TopDocs topDocs = this.getTopDocs(searchRequest, searchResults);
            ScoreDoc[] hits = topDocs.scoreDocs;
            long searchCompleteTime = System.currentTimeMillis() - startTime;
            HashMap<String, SDIList> sdiListMap = new HashMap<String, SDIList>();
            LinkedList<SearchDocument> searchDocuments = new LinkedList<SearchDocument>();
            HashMap<Integer, SearchDocument> docidMap = new HashMap<Integer, SearchDocument>(hits.length);
            for (ScoreDoc scoreDoc : hits) {
                SearchDocument doc = new SearchDocument(this.indexer, this.sdcRenderRules, this.m18n, this.indexSearcher.doc(scoreDoc.doc), scoreDoc.doc, scoreDoc.score);
                searchDocuments.add(doc);
                docidMap.put(scoreDoc.doc, doc);
                String sdcid = doc.isChildSDC() ? doc.getParentSdcid() : doc.getSdcid();
                sdiList = (SDIList)sdiListMap.get(sdcid);
                if (sdiList == null) {
                    sdiList = new SDIList();
                    sdiList.setSdcid(sdcid);
                    sdiList.setAllowDups(true);
                    sdiListMap.put(sdcid, sdiList);
                }
                int index = doc.isChildSDC() ? sdiList.addSDI(doc.getParentKeyid1(), doc.getParentKeyid2(), doc.getParentKeyid3()) : sdiList.addSDI(doc.getKeyid1(), doc.getKeyid2(), doc.getKeyid3());
                sdiList.setSDIAttribute(index, "docid", String.valueOf(scoreDoc.doc));
            }
            HashMap<String, Object> keyMap = new HashMap<String, Object>();
            HashSet<Integer> validDocs = new HashSet<Integer>();
            String[] queryparts = StringUtil.split(searchRequest.getSearchQuery(), " ");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (String sdcid : sdiListMap.keySet()) {
                sdiList = (SDIList)sdiListMap.get(sdcid);
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDIList(sdiList);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("attribute");
                sdiRequest.setRequestItem("notes");
                if (sdcid.equals("LV_Document")) {
                    sdiRequest.setRequestItem("documentfield");
                    sdiRequest.setRequestItem("documentreviewitem");
                }
                sdiRequest.setExtendedDataTypes(true);
                SDIData sdiData = this.sdiProcessor.getSDIData(sdiRequest);
                if (sdiData == null || sdiData.getDataset("primary") == null) continue;
                DataSet primary = sdiData.getDataset("primary");
                String[] columns = primary.getColumns();
                String[] keys = sdiData.getKeys("primary");
                String linkPageOperation = Searcher.getDefaultLinkPageOperation(this.sapphireConnection.getDatabaseId(), sdcid);
                boolean isLinkPageOperationCheckReqd = false;
                DataSet editableSDIListDS = new DataSet();
                if (linkPageOperation != null && linkPageOperation.length() > 0 && !"list".equalsIgnoreCase(linkPageOperation)) {
                    isLinkPageOperationCheckReqd = true;
                    SDIList editableSDIList = this.damProcessor.checkSDIAccess(sdcid, sdiList.getKeyid1(), keys.length >= 2 && keys[1].length() > 0 ? sdiList.getKeyid2() : "", keys.length >= 3 && keys[2].length() > 0 ? sdiList.getKeyid3() : "", true, linkPageOperation);
                    if (editableSDIList != null) {
                        editableSDIListDS = editableSDIList.toDataSet();
                    }
                }
                for (int i = 0; i < sdiList.size(); ++i) {
                    int findRow;
                    keyMap.clear();
                    keyMap.put(keys[0], sdiList.getKeyid1(i));
                    if (keys.length >= 2 && keys[1].length() > 0) {
                        keyMap.put(keys[1], sdiList.getKeyid2(i));
                    }
                    if (keys.length >= 3 && keys[2].length() > 0) {
                        keyMap.put(keys[2], sdiList.getKeyid3(i));
                    }
                    if ((findRow = primary.findRow(keyMap)) <= -1 || !searchRequest.isShowTemplates() && (searchRequest.isShowTemplates() || !primary.getValue(findRow, "templateflag", "N").equals("N"))) continue;
                    int docid = Integer.parseInt(sdiList.getSDIAttribute(i, "docid"));
                    validDocs.add(docid);
                    SearchDocument doc = (SearchDocument)docidMap.get(docid);
                    if (isLinkPageOperationCheckReqd) {
                        filterMap.clear();
                        filterMap.put("sdcid", sdcid);
                        filterMap.put("keyid1", sdiList.getKeyid1(i));
                        if (keys.length >= 2 && keys[1].length() > 0) {
                            filterMap.put("keyid2", sdiList.getKeyid2(i));
                        }
                        if (keys.length >= 3 && keys[2].length() > 0) {
                            filterMap.put("keyid3", sdiList.getKeyid3(i));
                        }
                        if (editableSDIListDS.findRow(filterMap) == -1) {
                            doc.setShowLinkPage(false);
                        }
                    }
                    HashMap primaryData = (HashMap)primary.get(findRow);
                    if (doc.isSDI()) {
                        int k;
                        DataSet attributes = sdiData.getDataset("attribute");
                        if (attributes != null) {
                            keyMap.clear();
                            keyMap.put("keyid1", sdiList.getKeyid1(i));
                            keyMap.put("keyid2", sdiList.getKeyid2(i));
                            keyMap.put("keyid3", sdiList.getKeyid3(i));
                            DataSet sdiAttributes = attributes.getFilteredDataSet(keyMap);
                            for (int j = 0; j < sdiAttributes.size(); ++j) {
                                if (!this.setMatchingColumn(doc, queryparts, "sdiattribute." + sdiAttributes.getValue(j, "attributeid"), sdiAttributes.getValue(j, "textvalue"))) continue;
                                primaryData.put("sdiattribute." + sdiAttributes.getValue(j, "attributeid"), sdiAttributes.getValue(j, "textvalue"));
                            }
                        }
                        if (sdcid.equals("LV_Document")) {
                            DataSet documentreviewitem;
                            DataSet documentfields = sdiData.getDataset("documentfield");
                            if (documentfields != null) {
                                keyMap.clear();
                                keyMap.put("documentid", sdiList.getKeyid1(i));
                                keyMap.put("documentversionid", sdiList.getKeyid2(i));
                                DataSet fields = documentfields.getFilteredDataSet(keyMap);
                                for (int j = 0; j < fields.size(); ++j) {
                                    if (!this.setMatchingColumn(doc, queryparts, "documentfield." + fields.getValue(j, "fieldid"), fields.getValue(j, "enteredtext"))) continue;
                                    primaryData.put("documentfield." + fields.getValue(j, "fieldid"), fields.getValue(j, "enteredtext"));
                                }
                            }
                            if ((documentreviewitem = sdiData.getDataset("documentreviewitem")) != null) {
                                keyMap.clear();
                                keyMap.put("documentid", sdiList.getKeyid1(i));
                                keyMap.put("documentversionid", sdiList.getKeyid2(i));
                                DataSet reviewitems = documentreviewitem.getFilteredDataSet(keyMap);
                                for (int j = 0; j < reviewitems.size(); ++j) {
                                    if (!this.setMatchingColumn(doc, queryparts, "documentreviewitem." + reviewitems.getValue(j, "reviewitemid"), reviewitems.getValue(j, "reviewitemtext"))) continue;
                                    primaryData.put("documentreviewitem." + reviewitems.getValue(j, "reviewitemid"), reviewitems.getValue(j, "reviewitemtext"));
                                }
                            }
                        }
                        if (doc.isChildSDC()) {
                            doc.setMatchingColumn(doc.getMatchTitle());
                            primaryData.put(doc.getMatchTitle(), doc.getDescCol());
                        }
                        doc.setPrimaryData(primaryData);
                        doc.setKeycolid1(keys[0]);
                        for (int j = 0; j < columns.length; ++j) {
                            primaryData.put(columns[j] + "__type", primary.getColumnType(columns[j]));
                            if (primary.getColumnType(columns[j]) != 0 && (primary.getColumnType(columns[j]) != 3 || Column.isAuditColumn(columns[j]))) continue;
                            this.setMatchingColumn(doc, queryparts, columns[j], primary.getValue(findRow, columns[j]));
                        }
                        if (doc.isKeyMatch()) {
                            searchDocuments.remove(doc);
                            searchDocuments.addFirst(doc);
                            continue;
                        }
                        if (!doc.isBoostMatch()) continue;
                        searchDocuments.remove(doc);
                        for (k = 0; k < searchDocuments.size() && (searchDocuments.get(k).isKeyMatch() || searchDocuments.get(k).isBoostMatch()); ++k) {
                        }
                        searchDocuments.add(k, doc);
                        continue;
                    }
                    if (doc.isAttachment()) {
                        String filename = doc.getField("filename");
                        HashMap<String, String> attachment = new HashMap<String, String>();
                        attachment.put("filename", filename);
                        doc.setAttachment(attachment);
                        this.setMatchingColumn(doc, queryparts, "filename", filename);
                        if (doc.getMatchingColumns().size() == 0) {
                            doc.setMatchingColumn("attachment");
                        }
                        if (primaryData == null || primaryData.size() <= 0) continue;
                        doc.setPrimaryData(primaryData);
                        continue;
                    }
                    if (!doc.isNote()) continue;
                    DataSet notes = sdiData.getDataset("notes");
                    if (notes != null) {
                        keyMap.clear();
                        keyMap.put("keyid1", sdiList.getKeyid1(i));
                        keyMap.put("keyid2", sdiList.getKeyid2(i));
                        keyMap.put("keyid3", sdiList.getKeyid3(i));
                        keyMap.put("notenum", new BigDecimal(doc.getAttachmentNum()));
                        findRow = notes.findRow(keyMap);
                        if (findRow > -1) {
                            doc.setNote((HashMap)notes.get(findRow));
                        }
                    }
                    doc.setMatchingColumn("note");
                    if (primaryData == null || primaryData.size() <= 0) continue;
                    doc.setPrimaryData(primaryData);
                }
            }
            block10: for (int i = searchDocuments.size() - 1; i >= 0; --i) {
                SearchDocument searchDocument = (SearchDocument)searchDocuments.get(i);
                if (!validDocs.contains(searchDocument.getDocid())) {
                    searchDocuments.remove(i);
                    continue;
                }
                for (int j = 0; j < i; ++j) {
                    if (!searchDocuments.get(j).matches(searchDocument)) continue;
                    searchDocuments.get(j).addMatchingDocument(searchDocument);
                    searchDocuments.remove(i);
                    continue block10;
                }
            }
            long processingCompleteTime = System.currentTimeMillis() - startTime - searchCompleteTime;
            searchResults.setSearchDocuments(searchDocuments);
            searchResults.setStats(topDocs.totalHits, searchCompleteTime, processingCompleteTime);
            return searchResults;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get searcher. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
    }

    private void generateSearchQuery(SearchRequest searchRequest) {
        String searchquery = searchRequest.getEnteredQuery();
        if (searchquery.equalsIgnoreCase("or")) {
            searchquery = "\\or";
        } else if (searchquery.equalsIgnoreCase("and")) {
            searchquery = "\\and";
        } else if (searchquery.equalsIgnoreCase("not")) {
            searchquery = "\\not";
        }
        searchquery = searchRequest.getSdcid().toLowerCase().equals("LV_Worksheet".toLowerCase()) ? "(" + searchquery + ") AND +(sdcid:" + "LV_Worksheet".toLowerCase() + " OR sdcid:" + "LV_WorksheetSection".toLowerCase() + " OR sdcid:" + "LV_WorksheetItem".toLowerCase() + ")" : (searchRequest.getSdcid().length() > 0 ? "(" + searchquery + ") AND +sdcid:" + searchRequest.getSdcid() : searchquery);
        String type = "";
        type = type + (!searchRequest.isShowSDI() ? "type:sdi" : "");
        type = type + (!searchRequest.isShowAttachments() ? (type.length() > 0 ? " || " : "") + "type:attachment" : "");
        type = type + (!searchRequest.isShowNotes() ? (type.length() > 0 ? " || " : "") + "type:note" : "");
        searchquery = type.length() > 0 ? "(" + searchquery + ") and not (" + type + ")" : searchquery;
        DateTimeUtil dtu = new DateTimeUtil(this.sapphireConnection);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String[] ranges = StringUtil.getTokens(searchquery);
        for (int i = 0; i < ranges.length; ++i) {
            Calendar from;
            int toPos = ranges[i].toLowerCase().indexOf(" to ");
            if (toPos > 0) {
                from = dtu.getCalendar(ranges[i].substring(0, toPos));
                Calendar to = dtu.getCalendar(ranges[i].substring(toPos + 4));
                searchquery = StringUtil.replaceAll(searchquery, "[" + ranges[i] + "]", "[" + (from != null ? sdf.format(from.getTime()) : ranges[i].substring(0, toPos)) + " TO " + (to != null ? sdf.format(to.getTime()) : ranges[i].substring(toPos + 4)) + "]");
                continue;
            }
            from = dtu.getCalendar(ranges[i]);
            searchquery = StringUtil.replaceAll(searchquery, "[" + ranges[i] + "]", "[" + (from != null ? sdf.format(from.getTime()) : ranges[i]) + " TO " + sdf.format(dtu.getCalendar("n").getTime()) + "]");
        }
        String[] queryparts = StringUtil.split(searchquery, " ");
        searchquery = "";
        for (int i = 0; i < queryparts.length; ++i) {
            if (queryparts[i].trim().length() <= 0) continue;
            searchquery = queryparts[i].trim().contains("-") ? searchquery + " " + StringUtil.replaceAll(queryparts[i].trim(), "-", "__") + " " : (queryparts[i].trim().equalsIgnoreCase("or") ? searchquery + " OR " : (queryparts[i].trim().equalsIgnoreCase("and") ? searchquery + " AND " : (queryparts[i].trim().equalsIgnoreCase("not") ? searchquery + " NOT " : (queryparts[i].trim().equalsIgnoreCase("to") ? searchquery + " TO " : searchquery + " " + queryparts[i].trim() + " "))));
        }
        searchRequest.setSearchQuery(searchquery);
    }

    private TopDocs getTopDocs(SearchRequest searchRequest, SearchResults searchResults) throws Exception {
        QueryParser parser = new QueryParser("content", this.indexer.getAnalyzer());
        parser.setLowercaseExpandedTerms(true);
        parser.setAllowLeadingWildcard(true);
        parser.setLocale(this.m18n.getLocale());
        this.query = parser.parse(searchRequest.getSearchQuery());
        searchResults.setQueryClass(this.query.getClass().getSimpleName());
        searchResults.setQuery(this.query.toString());
        try {
            if (searchRequest.getSearchLimit() == -1) {
                searchRequest.setSearchLimit(Integer.parseInt(this.indexer.getSearchPolicy().getProperty("searchlimit", DEFAULT_SEARCHLIMIT)));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (searchRequest.getSearchLimit() > 500) {
            searchRequest.setSearchLimit(500);
        }
        return this.indexSearcher.search(this.query, searchRequest.getSearchLimit());
    }

    private boolean setMatchingColumn(SearchDocument searchDocument, String[] queryparts, String columnid, String value) {
        boolean matching = false;
        for (int i = 0; i < queryparts.length; ++i) {
            int pos;
            String querypart = queryparts[i];
            if (querypart.contains(":")) {
                querypart = querypart.substring(querypart.indexOf(":") + 1);
            }
            if ((querypart = StringUtil.replaceAll(querypart, "__", "-").toLowerCase()).length() <= 0 || querypart.equals("and") || querypart.equals("or") || querypart.equals("not") || queryparts.equals("to") || (pos = value.toLowerCase().indexOf(querypart)) <= -1 && !RegexUtil.wildcardMatch(value.toLowerCase(), "*" + querypart + "*")) continue;
            searchDocument.setMatchingColumn(columnid);
            matching = true;
        }
        return matching;
    }

    public Query getQuery() {
        return this.query;
    }

    public int renderResults(SearchResults results, String searchPage, String searchid, JspWriter out) throws IOException {
        List<SearchDocument> searchDocuments = results.getSearchDocuments();
        HashMap<String, String> sdiLinkPages = new HashMap<String, String>();
        HashMap<String, String> sdiPreviewPages = new HashMap<String, String>();
        SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        StringBuffer resultsArray = new StringBuffer();
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
        int attachmentsShown = 0;
        for (int i = 0; i < searchDocuments.size(); ++i) {
            ArrayList<SearchDocument> docSearchDocuments;
            SearchDocument doc = searchDocuments.get(i);
            String id = doc.isChildSDC() ? doc.getParentId() : doc.getId();
            String docSdcid = doc.isChildSDC() ? doc.getParentSdcid() : doc.getSdcid();
            PropertyList sdcPolicy = this.indexer.getSDCPolicy(doc.getSdcid());
            PropertyList sdcRenderRule = this.sdcRenderRules.getIndexedPropertyList(docSdcid);
            if (sdcRenderRule == null) {
                sdcRenderRule = new PropertyList();
            }
            if (doc.isSDI() || doc.isAttachment() || doc.isNote()) {
                if (!sdiLinkPages.containsKey(docSdcid)) {
                    sdiLinkPages.put(docSdcid, sdcRenderRule.getProperty("linkpage", Searcher.getDefaultLinkPage(this.sapphireConnection.getDatabaseId(), doc.getSdcid())));
                }
                if (!sdiPreviewPages.containsKey(docSdcid)) {
                    sdiPreviewPages.put(docSdcid, sdcRenderRule.getProperty("previewpage", Searcher.getDefaultPreviewPage(doc.getSdcid())));
                }
            }
            resultsArray.append(",'").append(id).append("'");
            out.println("<tr id=\"" + id + "_row\" rownum=\"" + i + "\" valign=\"top\" style=\"padding:top:3px;padding-bottom:3px\" tabindex=\"0\" onkeydown=\"keyDownRow( event.keyCode, '" + searchid + "', " + i + ", " + searchDocuments.size() + ")\" onmouseover=\"mouseOverRow( '" + id + "' )\" onmouseout=\"mouseOutRow( '" + id + "' )\">");
            out.println("<td id\"\"" + id + "_linkcol\"><input style=\"margin-top:3px\" type=\"checkbox\" id=\"" + id + "\" name=\"selector\" onclick=\"selectorClick( event )\"/></td>");
            out.println("<td><img src=\"" + doc.getImage(this.sapphireConnection.getConnectionId()) + "\" width=\"32px\" height=\"32px\"/></td>");
            out.println("<td>");
            if (doc.isSDI()) {
                int j;
                out.println("<table border=\"0\" cellspacing=\"0\" cellpadding\"2\" width=\"100%\"><tr>");
                out.println("<td>");
                if (doc.isShowLinkPage()) {
                    out.println("<a href=\"javascript:sapphire.search.queryLink('" + id + "', '" + (String)sdiLinkPages.get(docSdcid) + "', '" + searchPage + "', '" + searchid + "', " + (i + 1) + ")\" title=\"" + this.tp.translate("Open") + this.tp.translate(sdcProcessor.getProperty(docSdcid, "singular").toLowerCase()) + "\">");
                }
                out.println(this.tp.translate(sdcProcessor.getProperty(docSdcid, "singular").toUpperCase()) + ": " + results.highlight(doc.isChildSDC() ? doc.getParentKeyid1() : doc.getKeyid1()) + (doc.hasKeyid2() ? " ver: " + (doc.isChildSDC() ? doc.getParentKeyid2() : doc.getKeyid2()) + (doc.hasKeyid3() ? " var: " + (doc.isChildSDC() ? doc.getParentKeyid3() : doc.getKeyid3()) : "") : ""));
                if (doc.isShowLinkPage()) {
                    out.println("</a>");
                }
                out.println("</td>");
                out.println("<td>" + this.tp.translate(results.highlight(doc.isChildSDC() ? doc.getValue(sdcPolicy.getProperty("parentdesccol")) : doc.getDescCol())) + "</td>");
                PropertyListCollection operations = sdcRenderRule.getCollection("operations");
                out.println("<td align=\"right\">");
                if (operations != null) {
                    for (int j2 = 0; j2 < operations.size(); ++j2) {
                        PropertyList operation = operations.getPropertyList(j2);
                        out.println("<a name=\"" + id + "_operations\" style=\"visibility:hidden\" href=\"javascript:operationSelected('" + doc.getKeyid1() + (doc.hasKeyid2() ? "|" + doc.getKeyid2() + (doc.hasKeyid3() ? "|" + doc.getKeyid3() : "") : "") + "');" + operation.getProperty("href") + ";javascript:operationSelected('');\" title=\"" + this.tp.translate(operation.getProperty("tip")) + "\">" + this.tp.translate(operation.getProperty("title")) + "</a>&nbsp;");
                    }
                }
                out.println("</td></tr></table>");
                ArrayList<String> matchingColumns = new ArrayList<String>();
                matchingColumns.addAll(doc.getMatchingColumns());
                PropertyListCollection resultColumns = sdcRenderRule.getCollection("sdiresultcolumns");
                int maxlen = -1;
                try {
                    maxlen = Integer.parseInt(sdcRenderRule.getProperty("sdiresultmaxlen", DEFAULT_COLUMNSIZE));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                int resultcols = 1;
                try {
                    resultcols = Integer.parseInt(sdcRenderRule.getProperty("sdiresultcols", "1"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                out.println("<table border=\"0\" cellspacing=\"0\" cellpadding\"2\" width=\"80%\">");
                out.println("<tr valign=\"top\">");
                int colPos = 0;
                if (resultColumns != null) {
                    for (j = 0; j < resultColumns.size(); ++j) {
                        PropertyList link;
                        String[] displays;
                        PropertyList resultColumn = resultColumns.getPropertyList(j);
                        String columnid = resultColumn.getProperty("columnid");
                        int colspan = 1;
                        try {
                            colspan = Integer.parseInt(resultColumn.getProperty("colspan", "1"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        String value = doc.getValue(columnid);
                        String displayvalue = resultColumn.getProperty("displayvalue");
                        if (displayvalue.length() > 0 && (displays = StringUtil.split(displayvalue, ";")) != null && displays.length > 0) {
                            for (int k = 0; k < displays.length; ++k) {
                                String display = displays[k].trim();
                                int pos = display.indexOf("=");
                                if (pos <= -1 || !display.substring(0, pos).equals(value)) continue;
                                value = display.substring(pos + 1).trim();
                            }
                        }
                        if ((value == null || value.length() <= 0) && (value != null && value.length() != 0 || !resultColumn.getProperty("hideifnull", "N").equals("N"))) continue;
                        if ((colPos += colspan) > resultcols) {
                            out.println("<td colspan=\"" + String.valueOf((resultcols - colPos + colspan) * 2) + "\">&nbsp;</td></tr>");
                            if (j < resultColumns.size()) {
                                out.println("<tr valign=\"top\">");
                            }
                            colPos = colspan;
                        }
                        String linkHref = (link = resultColumn.getPropertyList("link")) != null ? link.getProperty("href") : "";
                        String linkTarget = link != null ? link.getProperty("target", "_parent") : "";
                        out.println("<td nowrap width=\"200px\">" + this.tp.translate(resultColumn.getProperty("title", sdcProcessor.getSDCColumnProperty(docSdcid, columnid, "columnlabel"))) + "</td>");
                        out.println("<td colspan=\"" + String.valueOf(2 * (colspan - 1) + 1) + "\">");
                        if (value != null) {
                            if (doc.getColumnType(columnid) == 3) {
                                try {
                                    Document jdoc = Jsoup.parse((String)value);
                                    value = jdoc.body().text();
                                }
                                catch (Exception jdoc) {
                                    // empty catch block
                                }
                            }
                            if (matchingColumns.contains(columnid)) {
                                matchingColumns.remove(columnid);
                                value = results.highlight(value, maxlen);
                            }
                            if (linkHref.length() > 0) {
                                String[] tokens = StringUtil.getTokens(linkHref);
                                if (tokens != null && tokens.length > 0) {
                                    for (int k = 0; k < tokens.length; ++k) {
                                        if (tokens[k].equals("columnid") && columnid.length() > 0) {
                                            linkHref = StringUtil.replaceAll(linkHref, "[" + tokens[k] + "]", value);
                                            continue;
                                        }
                                        if (tokens[k].startsWith("columnid=")) {
                                            linkHref = StringUtil.replaceAll(linkHref, "[" + tokens[k] + "]", doc.getValue(tokens[k].substring(tokens[k].indexOf(61) + 1)));
                                            continue;
                                        }
                                        if (!tokens[k].equalsIgnoreCase("returntolistpage")) continue;
                                        linkHref = StringUtil.replaceAll(linkHref, "[" + tokens[k] + "]", searchPage);
                                    }
                                }
                                out.println("<a target=\"" + linkTarget + "\" href=\"" + linkHref + "\">" + value + "</a>");
                            } else {
                                out.println(value);
                            }
                        } else {
                            out.println("&nbsp;");
                        }
                        out.println("</td>");
                        if (colPos % resultcols == 0) {
                            out.println("</tr>");
                            if (j < resultColumns.size() - 1) {
                                out.println("<tr valign=\"top\">");
                            }
                        }
                        colPos %= resultcols;
                    }
                }
                if (colPos != 0) {
                    out.println("<td colspan=\"" + String.valueOf(2 * (resultcols - colPos)) + "\">&nbsp;</td></tr>");
                }
                if (matchingColumns.size() > 0 && !this.addMatchingColumns.equals("N")) {
                    for (j = 0; j < matchingColumns.size(); ++j) {
                        out.println("<tr valign=\"top\">");
                        String label = sdcProcessor.getSDCColumnProperty(docSdcid, (String)matchingColumns.get(j), "columnlabel");
                        out.println("<td nowrap width=\"200px\"><i>" + this.tp.translate(label.length() > 0 ? label : (String)matchingColumns.get(j)) + ":</i></td><td colspan=\"" + (resultcols * 2 - 1) + "\">");
                        String value = doc.getValue((String)matchingColumns.get(j));
                        if (doc.getColumnType((String)matchingColumns.get(j)) == 3) {
                            try {
                                Document jdoc = Jsoup.parse((String)value);
                                value = jdoc.body().text();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        out.println(results.highlight(value, maxlen));
                        out.println("</td></tr>");
                        if (this.addMatchingColumns.equals(ADDMATCHING_FIRST)) break;
                    }
                }
                out.println("</table>");
            }
            if (doc.isAttachment()) {
                block65: {
                    out.println("<table style=\"border: 0\" cellspacing=\"0\" cellpadding\"2\" width=\"100%\"><tr>");
                    out.println("<td><a href=\"javascript:sapphire.search.queryLink('" + id + "', '" + (String)sdiLinkPages.get(docSdcid) + "', '" + searchPage + "', '" + searchid + "', " + (i + 1) + ")\" title=\"" + this.tp.translate("Open") + this.tp.translate(sdcProcessor.getProperty(docSdcid, "singular").toLowerCase()) + "\">");
                    out.println(this.tp.translate(sdcProcessor.getProperty(docSdcid, "singular").toUpperCase()) + ": " + results.highlight(doc.isChildSDC() ? doc.getParentKeyid1() : doc.getKeyid1()) + (doc.hasKeyid2() ? " ver: " + (doc.isChildSDC() ? doc.getParentKeyid2() : doc.getKeyid2()) + (doc.hasKeyid3() ? " var: " + (doc.isChildSDC() ? doc.getParentKeyid3() : doc.getKeyid3()) : "") : ""));
                    out.println("</a></td>");
                    out.println("<td><a href=\"javascript:sapphire.search.queryViewAttachment('" + id + "', '" + searchid + "', " + (i + 1) + ")\" title=\"" + this.tp.translate("View attachment") + "\">");
                    out.println(this.tp.translate("Attachment") + ": " + results.highlight(doc.getDescCol()) + "</a></td>");
                    out.println("</tr></table>");
                    if (!"(Unknown)".equalsIgnoreCase(doc.getField("filename"))) {
                        out.println("<span style=\"font-size:1em\">" + this.tp.translate("Filename:") + " " + results.highlight(doc.getField("filename")) + "</span>");
                    }
                    if (this.showAttachmentSnippets) {
                        String attachmentKey = docSdcid + ";" + doc.getKeyid1() + ";" + doc.getKeyid2() + ";" + doc.getKeyid3() + ";" + doc.getAttachmentNum();
                        if (attachmentsShown < this.attachmentSnippetItems) {
                            ++attachmentsShown;
                            try {
                                AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.sapphireConnection.getConnectionId());
                                Attachment attachment = (Attachment)Attachment.getAttachment(doc.getSdcid(), doc.getKeyid1(), doc.getKeyid2(), doc.getKeyid3(), Integer.parseInt(doc.getAttachmentNum()));
                                attachment.setTriggerBusinessRule(false);
                                attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
                                StringBuffer contentExtract = new StringBuffer();
                                byte[] byteData = null;
                                if (attachment.getType().equals("M")) {
                                    String clob = attachment.getClob();
                                    if (clob != null && clob.length() > 0) {
                                        Document jdoc = Jsoup.parse((String)clob);
                                        byteData = jdoc.body().text().getBytes();
                                    }
                                } else {
                                    byteData = attachment.getData(this.attachmentSizeLimit);
                                }
                                if (byteData == null || byteData.length == 0) {
                                    out.println("<br/><span style=\"color:red\">ERROR: Could not find attachment or attachment is empty.</span>");
                                    break block65;
                                }
                                if (byteData.length > this.attachmentSizeLimit) {
                                    out.println("<br/><span style=\"color:red\">ERROR: Attachment to large</span>");
                                    break block65;
                                }
                                AttachmentIndexer.extractAttachmentContent(attachment, contentExtract, byteData, this.sapphireConnection.getConnectionId());
                                out.println("<div id=\"" + attachmentKey + "\" style=\"font-size:1em\">" + results.highlightMatchLines(contentExtract.toString(), "\n", this.attachmentSnippetSections).replaceAll("\n", "<br/>") + "</div>");
                            }
                            catch (Exception se) {
                                out.println("<br/><span style=\"color:red\">ERROR:" + se.getMessage() + "</span>");
                            }
                        } else {
                            out.println("<div id=\"" + attachmentKey + "\" style=\"font-size:1em\"><a href=\"Javascript:showAttachmentMatches( '" + attachmentKey + "','" + results.getQuery() + "' )\">" + this.tp.translate("Show matches") + "</a></div>");
                        }
                    }
                }
                out.println("<br/>");
            }
            if (doc.isNote()) {
                out.println("<table border=\"0\" cellspacing=\"0\" cellpadding\"2\" width=\"100%\"><tr>");
                out.println("<td><a href=\"javascript:sapphire.search.queryLink('" + id + "', '" + (String)sdiLinkPages.get(docSdcid) + "', '" + searchPage + "', '" + searchid + "', " + (i + 1) + ")\" title=\"Open " + sdcProcessor.getProperty(docSdcid, "singular").toLowerCase() + "\">");
                out.println(sdcProcessor.getProperty(docSdcid, "singular").toUpperCase() + ": " + results.highlight(doc.isChildSDC() ? doc.getParentKeyid1() : doc.getKeyid1()) + (doc.hasKeyid2() ? " ver: " + (doc.isChildSDC() ? doc.getParentKeyid2() : doc.getKeyid2()) + (doc.hasKeyid3() ? " var: " + (doc.isChildSDC() ? doc.getParentKeyid3() : doc.getKeyid3()) : "") : ""));
                out.println("</a></td>");
                out.println("<td>");
                out.println(this.tp.translate("Note") + ": " + results.highlight(doc.getDescCol()) + "</td>");
                out.println("</tr></table>");
                out.println("<span style=\"font-size:1em\">" + this.tp.translate("Note") + ": " + results.highlight(doc.getNoteValue("note")) + "</span>");
                out.println("<br/>");
            }
            if ((docSearchDocuments = doc.getSearchDocuments()).size() > 0) {
                out.println("<table border=\"0\" cellspacing=\"0\" cellpadding\"2\" width=\"100%\"><tr>");
                for (int j = 0; j < docSearchDocuments.size(); ++j) {
                    SearchDocument searchDocument = docSearchDocuments.get(j);
                    out.println("<tr valign=\"top\">");
                    if (searchDocument.isSDI()) {
                        out.println("<td nowrap width=\"200px\"><i>" + this.tp.translate(searchDocument.getMatchTitle()) + ":</i></td><td>");
                        out.println(results.highlight(searchDocument.getDescCol()));
                    } else if (searchDocument.isNote()) {
                        out.println("<td nowrap width=\"200px\"><i>" + this.tp.translate(searchDocument.getMatchTitle()) + ":</i></td><td>");
                        out.println(results.highlight(searchDocument.getDescCol()));
                    } else {
                        out.println("<td nowrap width=\"200px\"><i>" + this.tp.translate(searchDocument.getMatchTitle()) + ":</i></td><td>");
                        out.println(results.highlight(searchDocument.getDescCol()));
                    }
                    out.println("</td></tr>");
                }
                out.println("</table>");
            }
            if (this.showAuditSummary) {
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("createby", doc.getField("createby"));
                tokenMap.put("createdt", doc.getCreatedt());
                tokenMap.put("modby", doc.getField("modby"));
                tokenMap.put("moddt", doc.getModdt());
                out.println("<span style=\"font-size:1em;color:green\">" + this.tp.translate("Created by [createby] on [createdt]", tokenMap) + "&nbsp;&nbsp;&nbsp;&nbsp;" + this.tp.translate("Last modified by [modby] on [moddt]", tokenMap) + "</span>");
            }
            if (this.showInternalSummary) {
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("docid", id);
                tokenMap.put("docnum", String.valueOf(doc.getDocid()));
                tokenMap.put("score", String.valueOf(doc.getScore()));
                tokenMap.put("matches", doc.getMatchingColumns().toString());
                out.println((this.showAuditSummary ? "<br/>" : "") + "<span style=\"font-size:0.8em;color:green\">" + this.tp.translate("id:[id] doc num:[docnum] score:[score] matches:[matches]", tokenMap) + "</span>");
            }
            out.println("</td>");
            out.println("<td width=\"32px\">");
            out.println("<a style=\"display:none\" id=\"" + id + "_arrow\" href=\"#\" onclick=\"rowClick('" + id + "', '" + searchid + "', " + (i + 1) + ")\"><img title=\"" + this.tp.translate("Preview") + "\" border=\"none\" width=\"32px\" height=\"32px\" src=\"WEB-CORE/elements/advancedsearch/images/opensearchbar.gif\"/></a>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("<script>");
        out.println("var resultDocIds = new Array(" + (resultsArray.length() > 0 ? resultsArray.substring(1) : "") + ");");
        out.println("var sdiPreviewPages = new Array();");
        for (String sdcid : sdiPreviewPages.keySet()) {
            out.println("sdiPreviewPages['" + sdcid + "']='" + (String)sdiPreviewPages.get(sdcid) + "';");
        }
        out.println("var sdiLinkPages = new Array();");
        for (String sdcid : sdiLinkPages.keySet()) {
            out.println("sdiLinkPages['" + sdcid + "']='" + (String)sdiLinkPages.get(sdcid) + "';");
        }
        out.println("</script>");
        return searchDocuments.size();
    }

    public static String getDefaultLinkPage(String databaseid, String sdcid) {
        String linkpageid = null;
        try {
            Indexer indexer = Indexer.getInstance(databaseid);
            PropertyList sdcPolicy = indexer.getSDCPolicy(sdcid);
            linkpageid = sdcPolicy != null ? sdcPolicy.getProperty("linkpage") : "";
        }
        catch (SapphireException e) {
            Trace.logInfo("Failed to get indexer instance. Reason: " + e.getMessage(), e);
        }
        if (linkpageid != null && linkpageid.length() > 0) {
            return linkpageid;
        }
        if (sdcid.equals("LV_Form") || sdcid.equals("LV_Document")) {
            return "rc?command=page&page=EForm";
        }
        if (sdcid.equals("LV_Worksheet")) {
            return "rc?command=page&page=WorksheetManager";
        }
        return "rc?command=page&page=" + sdcid + "Maint";
    }

    public static String getDefaultLinkPageOperation(String databaseid, String sdcid) {
        String pageOperation = null;
        try {
            Indexer indexer = Indexer.getInstance(databaseid);
            PropertyList sdcPolicy = indexer.getSDCPolicy(sdcid);
            pageOperation = sdcPolicy != null ? sdcPolicy.getProperty("linkpageoperation") : "";
        }
        catch (SapphireException e) {
            Trace.logInfo("Failed to get indexer instance. Reason: " + e.getMessage(), e);
        }
        return pageOperation;
    }

    public static String getDefaultPreviewPage(String sdcid) {
        if (sdcid.equals("LV_Form")) {
            return "rc?command=page&page=EForm&efmmode=FormPreview";
        }
        if (sdcid.equals("LV_Document")) {
            return "rc?command=page&page=EForm&efmmode=DocumentViewer";
        }
        if (sdcid.equals("LV_Worksheet") || sdcid.equals("LV_WorksheetSection") || sdcid.equals("LV_WorksheetItem")) {
            return "rc?command=page&page=WorksheetManager&wsmmode=WorksheetViewer";
        }
        if (sdcid.equals("LV_WorkflowDef")) {
            return "rc?command=file&file=WEB-CORE/elements/workflow/workflowdefpainter.jsp&viewonly=Y&painteronly=Y";
        }
        if (sdcid.equals("LV_TaskDef")) {
            return "rc?command=file&file=WEB-CORE/elements/workflow/taskdefpainter.jsp&viewonly=Y&painteronly=Y";
        }
        if (sdcid.equals("LV_TaskExec")) {
            return "rc?command=file&file=WEB-CORE/modules/workflow/taskexecreport.jsp&mode=TE";
        }
        return "rc?command=page&page=" + sdcid + "View";
    }
}

