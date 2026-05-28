/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.lucene.index.DirectoryReader
 *  org.apache.lucene.index.DocsEnum
 *  org.apache.lucene.index.Fields
 *  org.apache.lucene.index.IndexReader
 *  org.apache.lucene.index.MultiFields
 *  org.apache.lucene.index.Terms
 *  org.apache.lucene.index.TermsEnum
 *  org.apache.lucene.store.Directory
 *  org.apache.lucene.store.FSDirectory
 *  org.apache.lucene.util.BytesRef
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SearchGizmoOperations
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block33: {
            ajaxResponse = new AjaxResponse(request, response);
            try {
                ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
                SapphireConnection sapphireConnection = connectionProcessor.getSapphireConnection();
                TranslationProcessor tp = this.getTranslationProcessor();
                String operation = ajaxResponse.getRequestParameter("operation");
                ajaxResponse.addCallbackArgument("elementid", ajaxResponse.getRequestParameter("elementid"));
                int items = 6;
                try {
                    items = Integer.parseInt(ajaxResponse.getRequestParameter("items", "6"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (operation.equals("getinitialitems")) {
                    DataSet recentitems = new DataSet();
                    if (ajaxResponse.getRequestParameter("getrecentitems", "Y").equals("Y")) {
                        StringBuffer select = new StringBuffer("SELECT\twt.webpagelogid, wt.title \"text\", wt.tip, w.webpagerequest FROM\twebpagelogtitle wt,webpagelog w WHERE\tw.webpagelogid IN ( ");
                        if (sapphireConnection.getDbms().equals("MSS")) {
                            select.append("( SELECT TOP ").append(items + 10).append(" webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) ");
                        } else {
                            select.append("( SELECT webpagelogid FROM ( SELECT webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) WHERE rownum < ").append(items + 10).append(" ) ");
                        }
                        select.append(") AND w.webpagelogid = wt.webpagelogid ORDER BY w.requestdt DESC");
                        recentitems = this.getQueryProcessor().getPreparedSqlDataSet(select.toString(), new Object[]{sapphireConnection.getSysuserId()});
                    }
                    ajaxResponse.addCallbackArgument("recentitems", recentitems);
                    DataSet latestsearches = new DataSet();
                    Indexer indexer = Indexer.getInstance(sapphireConnection.getDatabaseId());
                    if (indexer.isIndexing() && indexer.isSearching() && ajaxResponse.getRequestParameter("getlatestsearches", "Y").equals("Y")) {
                        latestsearches = this.getQueryProcessor().getPreparedSqlDataSet(sapphireConnection.isOracle() ? "SELECT DISTINCT * FROM (SELECT enteredquery, enteredquery \"text\" FROM search WHERE searchtypeflag='Q' AND sysuserid=? ORDER BY searchdt DESC) WHERE ROWNUM < 6" : "SELECT TOP 5 * FROM (SELECT enteredquery, enteredquery \"text\", Min (searchdt) searchdt FROM search WHERE searchtypeflag='Q' AND sysuserid=? GROUP BY enteredquery, enteredquery ) myview ORDER BY searchdt DESC", new Object[]{sapphireConnection.getSysuserId()});
                    }
                    ajaxResponse.addCallbackArgument("latestsearches", latestsearches);
                    ajaxResponse.addCallbackArgument("maxitems", items);
                    break block33;
                }
                if (!operation.equals("getmatchingitems")) break block33;
                String query = ajaxResponse.getRequestParameter("query");
                ajaxResponse.addCallbackArgument("query", query.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
                ArrayList<String> queryparts = SearchResults.createQueryParts(query);
                DataSet recentitems = new DataSet();
                if (query.length() > 0 && ajaxResponse.getRequestParameter("getrecentitems", "Y").equals("Y")) {
                    StringBuffer select = new StringBuffer("SELECT\twt.webpagelogid, wt.title \"text\", wt.tip, w.webpagerequest FROM\twebpagelogtitle wt,webpagelog w WHERE\tw.webpagelogid IN ( ");
                    if (sapphireConnection.getDbms().equals("MSS")) {
                        select.append("( SELECT TOP ").append(items).append(" webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) ");
                    } else {
                        select.append("( SELECT webpagelogid FROM ( SELECT webpagelogid FROM webpagelog WHERE sysuserid = ? AND propertyclob is not null ORDER BY requestdt DESC ) WHERE rownum < ").append(items).append(" ) ");
                    }
                    select.append(") AND w.webpagelogid = wt.webpagelogid AND lower( wt.title ) like ? ORDER BY w.requestdt DESC");
                    recentitems = this.getQueryProcessor().getPreparedSqlDataSet(select.toString(), new Object[]{sapphireConnection.getSysuserId(), "%" + query.toLowerCase() + "%"});
                    for (int i = 0; i < recentitems.size(); ++i) {
                        recentitems.setValue(i, "text", SearchResults.highlight(recentitems.getValue(i, "text"), query, "<b>", "</b>"));
                    }
                }
                ajaxResponse.addCallbackArgument("recentitems", recentitems);
                DataSet menulinks = new DataSet();
                if (query.length() > 0 && ajaxResponse.getRequestParameter("getmenulinks", "Y").equals("Y")) {
                    String language = sapphireConnection.getLanguage() != null && sapphireConnection.getLanguage().length() > 0 ? sapphireConnection.getLanguage() : "(none)";
                    String menugizmos = (String)CacheUtil.get(sapphireConnection.getDatabaseId(), "menugizmos", language);
                    if (menugizmos == null) {
                        DataSet gizmomenus = this.getQueryProcessor().getPreparedSqlDataSet("SELECT gizmodefid, extendnodeid FROM GIZMODEF WHERE propertytreeid = ?", new Object[]{"menugizmo"}, true);
                        menugizmos = gizmomenus.getColumnValues("gizmodefid", ";");
                        CacheUtil.put(sapphireConnection.getDatabaseId(), "menugizmos", language, menugizmos);
                    }
                    if (menugizmos != null && menugizmos.length() > 0) {
                        String[] gizmodefids = StringUtil.split(menugizmos, ";");
                        for (int i = 0; i < gizmodefids.length; ++i) {
                            PropertyListCollection tabs;
                            sapphire.pageelements.BaseGizmo gizmo = BaseGizmo.getInstance(this.getConnectionId(), gizmodefids[i], true);
                            if (gizmo == null || (tabs = gizmo.getElementProperties().getCollection("tabs")) == null) continue;
                            for (int j = 0; j < tabs.size(); ++j) {
                                PropertyListCollection menus = tabs.getPropertyList(j).getCollection("menus");
                                if (menus == null) continue;
                                for (int k = 0; k < menus.size(); ++k) {
                                    PropertyList menu = menus.getPropertyList(k);
                                    String menutext = menu.getProperty("text");
                                    if (menulinks.size() > items || !menutext.toLowerCase().contains(query.toLowerCase()) || menulinks.findRow("rawtext", menutext) != -1) continue;
                                    int row = menulinks.addRow();
                                    menulinks.setString(row, "text", SearchResults.highlight(menutext, query, "<b>", "</b>"));
                                    menulinks.setString(row, "rawtext", menutext);
                                    menulinks.setString(row, "link", menu.getProperty("link"));
                                }
                            }
                        }
                    }
                }
                menulinks.sort("rawtext");
                ajaxResponse.addCallbackArgument("menulinks", menulinks);
                Indexer indexer = Indexer.getInstance(sapphireConnection.getDatabaseId());
                if (indexer.isIndexing() && indexer.isSearching()) {
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    DataSet searchterms = new DataSet();
                    if (query.length() > 0 && ajaxResponse.getRequestParameter("getterms", "Y").equals("Y")) {
                        searchterms.addColumn("term", 0);
                        searchterms.addColumn("text", 0);
                        searchterms.addColumn("docid", 0);
                        searchterms.addColumn("type", 0);
                        searchterms.addColumn("page", 0);
                        searchterms.addColumn("singular", 0);
                        searchterms.addColumn("sdcid", 0);
                        searchterms.addColumn("keyid1", 0);
                        searchterms.addColumn("keyid2", 0);
                        searchterms.addColumn("keyid3", 0);
                        searchterms.addColumn("term", 0);
                        searchterms.addColumn("freq", 1);
                        DirectoryReader reader = DirectoryReader.open((Directory)FSDirectory.open((Path)indexer.getIndexDir().toPath()));
                        int showFreq = 1;
                        int count = 0;
                        Fields fields = MultiFields.getFields((IndexReader)reader);
                        Terms terms = fields.terms("content");
                        TermsEnum iterator = terms.iterator();
                        BytesRef byteRef = null;
                        String[] inputparts = StringUtil.split(query, " ");
                        String matchterm = inputparts[inputparts.length - 1].trim();
                        int abscount = 0;
                        if (!(matchterm.equalsIgnoreCase("and") || matchterm.equalsIgnoreCase("or") || matchterm.equalsIgnoreCase("to"))) {
                            this.logInfo("$***find suggestions for query:" + query);
                            long starttime = System.currentTimeMillis();
                            while ((byteRef = iterator.next()) != null && count < 10) {
                                String term = StringUtil.replaceAll(new String(byteRef.bytes, byteRef.offset, byteRef.length).toLowerCase(), "__", "-");
                                if (abscount % 1000000 == 0) {
                                    this.logInfo("$***fields count:" + abscount + ", term:" + term);
                                    if (System.currentTimeMillis() - starttime > 10000L) {
                                        this.logWarn("$$$***More than 10 seconds in loops! Stop loop at " + abscount + " term:" + term + " suggestions found:" + count);
                                        break;
                                    }
                                }
                                if (abscount++ > 10000000) {
                                    this.logWarn("$$$***More than " + abscount + " terms iterated! Stop loop. term:" + term + " suggestions found:" + count);
                                    break;
                                }
                                if (!term.toLowerCase().startsWith(matchterm.toLowerCase())) continue;
                                int row = searchterms.addRow();
                                ++count;
                                int freq = iterator.docFreq();
                                if (freq <= showFreq) {
                                    DocsEnum docs = iterator.docs(null, null);
                                    int docId = -1;
                                    while ((docId = docs.nextDoc()) != Integer.MAX_VALUE) {
                                        SearchDocument doc = new SearchDocument(indexer, null, null, reader.document(docId), docId, 0.0f);
                                        String singular = sdcProcessor.getProperty(doc.getSdcid(), "singular");
                                        searchterms.setString(row, "docid", doc.getId());
                                        searchterms.setString(row, "type", doc.getType());
                                        searchterms.setString(row, "page", Searcher.getDefaultLinkPage(sapphireConnection.getDatabaseId(), doc.getSdcid()));
                                        searchterms.setString(row, "singular", singular);
                                        searchterms.setString(row, "sdcid", doc.getSdcid());
                                        searchterms.setString(row, "keyid1", doc.getKeyid1());
                                        searchterms.setString(row, "keyid2", doc.getKeyid2());
                                        searchterms.setString(row, "keyid3", doc.getKeyid3());
                                        searchterms.setString(row, "ischild", doc.isChildSDC() ? "Y" : "N");
                                        searchterms.setString(row, "parentdocid", doc.getParentId());
                                        searchterms.setString(row, "parentsdcid", doc.getParentSdcid());
                                        searchterms.setString(row, "parentkeyid1", doc.getParentKeyid1());
                                        searchterms.setString(row, "parentkeyid2", doc.getParentKeyid2());
                                        searchterms.setString(row, "parentkeyid3", doc.getParentKeyid3());
                                        searchterms.setString(row, "term", term);
                                        searchterms.setString(row, "text", SearchResults.highlight(term, queryparts, "<b>", "</b>"));
                                        searchterms.setNumber(row, "freq", freq);
                                    }
                                    continue;
                                }
                                searchterms.setString(row, "term", term);
                                searchterms.setString(row, "text", SearchResults.highlight(term, queryparts, "<b>", "</b>"));
                                searchterms.setNumber(row, "freq", freq);
                            }
                        }
                    }
                    ajaxResponse.addCallbackArgument("searchterms", searchterms);
                    DataSet searchresults = new DataSet();
                    if (query.length() > 0 && ajaxResponse.getRequestParameter("getsearchresults", "Y").equals("Y")) {
                        Searcher searcher = new Searcher(sapphireConnection);
                        SearchRequest searchRequest = new SearchRequest(query);
                        SearchResults results = searcher.getSearchResults(searchRequest);
                        List<SearchDocument> searchDocuments = results.getSearchDocuments();
                        searchresults.addColumn("term", 0);
                        searchresults.addColumn("docid", 0);
                        searchresults.addColumn("type", 0);
                        searchresults.addColumn("page", 0);
                        searchresults.addColumn("singular", 0);
                        searchresults.addColumn("sdcid", 0);
                        searchresults.addColumn("keyid1", 0);
                        searchresults.addColumn("keyid2", 0);
                        searchresults.addColumn("keyid3", 0);
                        searchresults.addColumn("term", 0);
                        searchresults.addColumn("freq", 1);
                        for (int i = 0; i < searchDocuments.size() && i < items; ++i) {
                            SearchDocument searchDocument = searchDocuments.get(i);
                            String singular = sdcProcessor.getProperty(searchDocument.getSdcid(), "singular");
                            searchresults.addRow();
                            searchresults.setString(i, "docid", String.valueOf(searchDocument.getDocid()));
                            searchresults.setString(i, "type", searchDocument.getType());
                            searchresults.setString(i, "page", Searcher.getDefaultLinkPage(sapphireConnection.getDatabaseId(), searchDocument.getSdcid()));
                            searchresults.setString(i, "singular", singular);
                            searchresults.setString(i, "sdcid", searchDocument.getSdcid());
                            searchresults.setString(i, "keyid1", searchDocument.getKeyid1());
                            searchresults.setString(i, "keyid2", searchDocument.getKeyid2());
                            searchresults.setString(i, "keyid3", searchDocument.getKeyid3());
                            searchresults.setString(i, "ischild", searchDocument.isChildSDC() ? "Y" : "N");
                            searchresults.setString(i, "parentdocid", searchDocument.getParentId());
                            searchresults.setString(i, "parentsdcid", searchDocument.getParentSdcid());
                            searchresults.setString(i, "parentkeyid1", searchDocument.getParentKeyid1());
                            searchresults.setString(i, "parentkeyid2", searchDocument.getParentKeyid2());
                            searchresults.setString(i, "parentkeyid3", searchDocument.getParentKeyid3());
                        }
                    }
                    ajaxResponse.addCallbackArgument("searchresults", searchresults);
                } else {
                    ajaxResponse.addCallbackArgument("searchterms", new DataSet());
                    ajaxResponse.addCallbackArgument("searchresults", new DataSet());
                }
                DataSet matchingsearches = new DataSet();
                if (ajaxResponse.getRequestParameter("getmatchingsearches", "Y").equals("Y")) {
                    DataSet searches = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT enteredquery, enteredquery \"text\" FROM search WHERE searchtypeflag='Q' AND sysuserid=? AND lower( enteredquery ) LIKE ? ORDER BY enteredquery", new Object[]{sapphireConnection.getSysuserId(), "%" + query.toLowerCase() + "%"});
                    for (int i = 0; i < searches.size() && i < items; ++i) {
                        int row = matchingsearches.addRow();
                        matchingsearches.setString(row, "enteredquery", searches.getValue(i, "enteredquery"));
                        matchingsearches.setString(row, "text", SearchResults.highlight(searches.getValue(i, "text"), queryparts, "<b>", "</b>"));
                    }
                }
                ajaxResponse.addCallbackArgument("matchingsearches", matchingsearches);
            }
            catch (Exception e) {
                this.logError("Failed to get search gizmo data. Reason: " + e.getMessage(), e);
            }
        }
        ajaxResponse.print();
    }
}

