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
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPropertyHandler;
import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.nio.file.Path;
import java.util.HashMap;
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
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.ext.BaseIssueHandler;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.ExternalHandlerProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DynamicLookupRequest
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        boolean isOracle = "ORA".equals(connectionInfo.getDbms());
        String sysuserid = connectionInfo.getSysuserId();
        String jsonString = request.getParameter("lookupprops");
        String keyinput = request.getParameter("keyinput");
        keyinput = keyinput == null ? "" : keyinput.toLowerCase();
        try {
            DataSet ds;
            PropertyList lookupprops = new PropertyList(new JSONObject(jsonString));
            boolean isRepositoryDataRequest = "Y".equals(lookupprops.getProperty("repositorydatarequest"));
            boolean isIssueManagementDataRequest = "Y".equals(lookupprops.getProperty("issuemanagementdatarequest"));
            if (isRepositoryDataRequest) {
                isOracle = !"MSS".equals(lookupprops.getProperty("dbms"));
            }
            SDCProcessor sdcProcessor = new SDCProcessor(connectionid);
            if (isIssueManagementDataRequest) {
                String issueRepositoryId = lookupprops.getProperty("issuerepositoryid", "CMTCommunication Custom");
                ds = this.searchIssueData(issueRepositoryId, keyinput);
            } else if (lookupprops.getProperty("searchquery", "N").equals("N")) {
                boolean isPhrase;
                String keycolid1;
                String sdcid = lookupprops.getProperty("sdcid");
                String restrictivewhere = EncryptDecrypt.unobfsql(lookupprops.getProperty("restrictivewhere"));
                String querywhere = EncryptDecrypt.unobfsql(lookupprops.getProperty("querywhere"));
                String phrasetype = lookupprops.getProperty("phrasetype");
                PropertyListCollection columns = lookupprops.getCollection("columns");
                String requestcolumn = "";
                String orderby = "";
                String searchableWhere = "";
                StringBuffer searchableWhereSb = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                if (columns == null) {
                    columns = new PropertyListCollection();
                    keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                    PropertyList keycolid1PL = new PropertyList();
                    keycolid1PL.setProperty("columnid", keycolid1);
                    keycolid1PL.setProperty("searchable", "Y");
                    columns.add(keycolid1PL);
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                    if (keycols > 1) {
                        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                        PropertyList keycolid2PL = new PropertyList();
                        keycolid2PL.setProperty("columnid", keycolid2);
                        keycolid2PL.setProperty("searchable", "N");
                        columns.add(keycolid2PL);
                        if (keycols > 2) {
                            String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                            PropertyList keycolid3PL = new PropertyList();
                            keycolid3PL.setProperty("columnid", keycolid3);
                            keycolid3PL.setProperty("searchable", "Y");
                            columns.add(keycolid3PL);
                        }
                    }
                    String desccol = sdcProcessor.getProperty(sdcid, "desccol");
                    PropertyList desccolPL = new PropertyList();
                    desccolPL.setProperty("columnid", desccol);
                    desccolPL.setProperty("searchable", "Y");
                    columns.add(desccolPL);
                }
                if (columns != null) {
                    int orderbyIndex = 1;
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList columnProps = columns.getPropertyList(i);
                        String columnid = columnProps.getProperty("columnid");
                        if (columnid.length() <= 0) continue;
                        String columnsql = columnid;
                        if (!"N".equals(columnProps.getProperty("searchable"))) {
                            String lastword;
                            if (columnid.indexOf(" ") > 0) {
                                columnsql = columnid.substring(0, columnid.lastIndexOf(" "));
                            }
                            searchableWhereSb.append("OR (lower( " + columnsql + " ) LIKE " + safeSQL.addVar(keyinput + "%") + " OR lower( " + columnsql + " ) LIKE " + safeSQL.addVar("% " + keyinput + "%") + ") ");
                            if ("Phrase".equals(sdcid) && keyinput.indexOf(" ") > 0 && (lastword = keyinput.substring(keyinput.lastIndexOf(" "))).length() > 1) {
                                searchableWhereSb.append("OR (lower( phraseshortcut ) LIKE " + safeSQL.addVar(lastword.substring(1) + "%") + ") ");
                            }
                        }
                        if (searchableWhereSb.indexOf("OR") == 0) {
                            searchableWhere = searchableWhereSb.substring(2);
                        }
                        requestcolumn = requestcolumn + "," + columnid;
                        orderby = orderby + "," + orderbyIndex++;
                    }
                    requestcolumn = requestcolumn.substring(1);
                    orderby = orderby.substring(1);
                    if (searchableWhere.trim().length() == 0) {
                        throw new Exception("Must define at least one searhable column");
                    }
                    searchableWhere = "(" + searchableWhere + ")";
                    if (querywhere.trim().length() > 0) {
                        searchableWhere = searchableWhere + " AND (" + querywhere + ") ";
                    } else if (phrasetype.length() > 0) {
                        searchableWhere = searchableWhere + " AND ( phrasetype is null OR phrasetype='' OR phrasetype=" + safeSQL.addVar(phrasetype) + " ) ";
                    }
                    if (restrictivewhere.trim().length() > 0) {
                        searchableWhere = searchableWhere + " AND (" + restrictivewhere + ") ";
                    }
                    if (searchableWhere != null && searchableWhere.indexOf("[currentuser]") >= 0) {
                        searchableWhere = StringUtil.replaceAll(searchableWhere, "[currentuser]", sysuserid);
                    }
                } else {
                    keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                    String i = sdcProcessor.getProperty(sdcid, "desccol");
                }
                boolean isSDCBased = true;
                if (lookupprops.getProperty("tableid").length() > 0) {
                    isSDCBased = false;
                }
                String tableid = lookupprops.getProperty("tableid").length() > 0 ? lookupprops.getProperty("tableid") : sdcProcessor.getProperty(sdcid, "tableid");
                String securityfilter = "";
                String fromClause = " FROM " + tableid;
                if (isSDCBased) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("mode", "securityfilterrequest");
                    props.put("sdcid", sdcid);
                    new RequestProcessor(connectionid).processRequest(AdhocQueryPropertyHandler.class.getName(), props);
                    securityfilter = (String)props.get("securityfilter");
                }
                if (securityfilter.length() > 0) {
                    searchableWhere = "(" + searchableWhere + ") AND (" + securityfilter + ")";
                }
                if (isSDCBased && "Y".equals(sdcProcessor.getProperty(sdcid, "activeableflag")) && !"(system)".equals(connectionInfo.getSysuserId()) && !"Y".equals(new ConfigurationProcessor(connectionid).getProfileProperty("viewhidden", "N"))) {
                    String activefilter = tableid + ".activeflag!='N' or " + tableid + ".activeflag is null";
                    searchableWhere = "(" + searchableWhere + ") AND (" + activefilter + ")";
                }
                if (requestcolumn != null && (requestcolumn.indexOf("[keycolid1]") >= 0 || requestcolumn.indexOf("[primarytable]") >= 0) || requestcolumn.indexOf("[sdcid]") >= 0) {
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[sdcid]", sdcid);
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid1]", sdcProcessor.getProperty(sdcid, "keycolid1"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid2]", sdcProcessor.getProperty(sdcid, "keycolid2"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid3]", sdcProcessor.getProperty(sdcid, "keycolid3"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[primarytable]", sdcProcessor.getProperty(sdcid, "tableid"));
                }
                if (searchableWhere != null && (searchableWhere.indexOf("[keycolid1]") >= 0 || searchableWhere.indexOf("[primarytable]") >= 0) || searchableWhere.indexOf("[sdcid]") >= 0) {
                    searchableWhere = StringUtil.replaceAll(searchableWhere, "[sdcid]", sdcid);
                    searchableWhere = StringUtil.replaceAll(searchableWhere, "[keycolid1]", sdcProcessor.getProperty(sdcid, "keycolid1"));
                    searchableWhere = StringUtil.replaceAll(searchableWhere, "[keycolid2]", sdcProcessor.getProperty(sdcid, "keycolid2"));
                    searchableWhere = StringUtil.replaceAll(searchableWhere, "[keycolid3]", sdcProcessor.getProperty(sdcid, "keycolid3"));
                    searchableWhere = StringUtil.replaceAll(searchableWhere, "[primarytable]", sdcProcessor.getProperty(sdcid, "tableid"));
                }
                orderby = (isPhrase = "LV_Phrase".equals(sdcid)) ? "phrasetext,phraseshortcut" : orderby;
                String sql = "";
                if (isRepositoryDataRequest && "LV_ChangeRequest".equals(sdcid)) {
                    searchableWhere = "(" + searchableWhere + ") and ( changerequeststatus in ( 'Accepted', 'In Progress' ) )";
                }
                sql = isOracle ? "SELECT * FROM ( SELECT " + (isPhrase ? "" : "distinct ") + requestcolumn + fromClause + " WHERE (" + searchableWhere + ") order by " + orderby + ") where rownum < 2501" : "SELECT " + (isPhrase ? "" : "distinct ") + "top 2500 " + requestcolumn + fromClause + " WHERE (" + searchableWhere + ") order by " + orderby;
                if (isRepositoryDataRequest) {
                    Object[] vals;
                    CMTPolicy policy = CMTPolicy.getPolicy(this.getConnectionid(), "");
                    String remoteServerURL = policy.getRepositoryURL();
                    if (remoteServerURL.indexOf("/sc") != remoteServerURL.length() - 3) {
                        remoteServerURL = remoteServerURL + "/sc";
                    }
                    String authtoken = policy.getRepositoryAuthToken();
                    ExternalHandlerProcessor externalHandlerProcessor = new ExternalHandlerProcessor(authtoken, remoteServerURL);
                    PropertyList requestPL = new PropertyList();
                    requestPL.setProperty("sdcid", sdcid);
                    requestPL.setProperty("sqlcode", sql);
                    StringBuilder sb = new StringBuilder();
                    for (Object o : vals = safeSQL.getValues()) {
                        sb.append(";" + o);
                    }
                    requestPL.setProperty("bindvars", sb.length() > 1 ? sb.substring(1) : "");
                    PropertyList responsePL = externalHandlerProcessor.sendCommandToLIMS("COMMAND_DATASETREQUEST", requestPL);
                    String datasetXML = responsePL.getProperty("datasetxml");
                    ds = new DataSet();
                    ds.setXML(datasetXML);
                } else {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
                }
            } else {
                ds = new DataSet();
                ds.addColumn("searchitem", 0);
                ds.addColumn("searchlink", 0);
                try {
                    Indexer indexer = Indexer.getInstance(connectionInfo.getDatabaseId());
                    DirectoryReader reader = DirectoryReader.open((Directory)FSDirectory.open((Path)indexer.getIndexDir().toPath()));
                    int showFreq = 1;
                    Fields fields = MultiFields.getFields((IndexReader)reader);
                    Terms terms = fields.terms("content");
                    TermsEnum iterator = terms.iterator();
                    BytesRef byteRef = null;
                    String[] inputparts = StringUtil.split(keyinput, " ");
                    String matchterm = inputparts[inputparts.length - 1].trim();
                    if (!(matchterm.equalsIgnoreCase("and") || matchterm.equalsIgnoreCase("or") || matchterm.equalsIgnoreCase("to"))) {
                        int count = 0;
                        while ((byteRef = iterator.next()) != null && count < 2500) {
                            String term = StringUtil.replaceAll(new String(byteRef.bytes, byteRef.offset, byteRef.length).toLowerCase(), "__", "-");
                            if (!term.startsWith(matchterm)) continue;
                            int row = ds.addRow();
                            ++count;
                            ds.setString(row, "searchitem", term);
                            int freq = iterator.docFreq();
                            if (freq <= showFreq) {
                                DocsEnum docs = iterator.docs(null, null);
                                StringBuffer searchlink = new StringBuffer();
                                int docId = -1;
                                while ((docId = docs.nextDoc()) != Integer.MAX_VALUE) {
                                    SearchDocument doc = new SearchDocument(indexer, null, null, reader.document(docId), docId, 0.0f);
                                    String singular = sdcProcessor.getProperty(doc.getSdcid(), "singular");
                                    if (doc.isSDI() || doc.isNote() || doc.isAttachment()) {
                                        searchlink.append("<br/><a href=\"javascript:sapphire.search.suggestLink('").append(doc.getId()).append("', '").append(Searcher.getDefaultLinkPage(connectionInfo.getDatabaseId(), doc.getSdcid())).append("', '").append(keyinput).append("', '").append(term).append("', ").append(freq).append(")\">").append(singular).append(": ").append(doc.getKeyid1()).append("</a>");
                                    } else {
                                        searchlink.append(doc.getId()).append(" referenced");
                                    }
                                    if (doc.isNote()) {
                                        searchlink.append("&nbsp;<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" border=\"0\" style=\"cursor:pointer;margin-bottom:-4px\">");
                                        continue;
                                    }
                                    if (!doc.isAttachment()) continue;
                                    searchlink.append("&nbsp;<img src=\"WEB-CORE/images/png/Attachments.png\" border=\"0\" style=\"cursor:pointer;margin-bottom:-4px\">");
                                }
                                ds.setString(row, "searchlink", searchlink.length() > 0 ? searchlink.substring(5) : "");
                                continue;
                            }
                            ds.setString(row, "searchlink", freq + " items referenced");
                        }
                    }
                }
                catch (Exception e) {
                    this.logError("Failed to access index for term suggestions. Reason: " + e.getMessage(), e);
                }
            }
            JSONObject jsonResponseObj = JSONUtil.toJSONObject(ds, false);
            jsonResponseObj.write(response.getWriter());
        }
        catch (Exception e) {
            throw new ServletException("Error:" + e.getMessage());
        }
    }

    private DataSet searchIssueData(String issueRepositoryId, String issueId) throws SapphireException {
        PropertyListCollection issues;
        PropertyList props = new PropertyList();
        props.setProperty("issuerepositoryid", issueRepositoryId);
        props.setProperty("issueid", issueId);
        PropertyList repositoryProps = IssueManagementUtil.getIssueRepositoryProperties(issueRepositoryId, this.getTranslationProcessor(), this.getConfigurationProcessor());
        String handlerClassName = repositoryProps.getProperty("handlerclass");
        BaseIssueHandler baseIssueHandler = BaseIssueHandler.getInstance(handlerClassName, this.getConnectionProcessor().getSapphireConnection());
        baseIssueHandler.executeMethod("searchIssue", props);
        PropertyList searchResultProps = props.getPropertyList("searchresults");
        DataSet ds = new DataSet();
        if (!searchResultProps.isEmpty() && !(issues = searchResultProps.getCollectionNotNull("issues")).isEmpty()) {
            ds.addColumn("key", 0);
            ds.addColumn("summary", 0);
            for (int i = 0; i < issues.size(); ++i) {
                PropertyList issue = issues.getPropertyList(i);
                ds.addRow();
                ds.setString(i, "key", issue.getProperty("key", ""));
                ds.setString(i, "summary", issue.getProperty("summary", ""));
            }
        }
        return ds;
    }
}

