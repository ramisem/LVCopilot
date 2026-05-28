/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryAdmin;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryRequest;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryService;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.QueryService;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class AdhocQueryPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        DataSet ds = null;
        String mode = (String)props.get("mode");
        if ("initialize".equals(mode)) {
            new AdhocQueryService(this.sapphireConnection);
        } else if ("savesdcsearchableflag".equals(mode) || "savecolumnsearchableflag".equals(mode) || "deleteadhocquery".equals(mode) || "saveadhocquery".equals(mode)) {
            new AdhocQueryAdmin(this.sapphireConnection).processSaveRequest(props);
        } else if ("adhocrequest".equals(mode) || "adhocrequestcount".equals(mode)) {
            AdhocQueryRequest adhocRequest = this.getAdhocQueryRequest(props, mode);
            AdhocQueryService adhocQuery = new AdhocQueryService(this.sapphireConnection);
            try {
                ds = adhocQuery.getResultDataSet(adhocRequest);
            }
            catch (Exception e) {
                props.put("errormessage", e.getMessage());
            }
            props.put("dataset", ds);
            props.put("errormessage", adhocQuery.getErrorMessage());
        } else if ("exportall".equals(mode)) {
            AdhocQueryRequest adhocRequest = this.getAdhocQueryRequest(props, mode);
            AdhocQueryService adhocQuery = new AdhocQueryService(this.sapphireConnection);
            try {
                File rf = adhocQuery.getResultFile(adhocRequest);
                props.put("resultfile", rf);
                props.put("rowsprocessed", "" + adhocQuery.rowsProcessed);
                String[] resultcolumns = adhocQuery.getResultColumns();
                props.put("resultcolumns", resultcolumns);
            }
            catch (Exception e) {
                this.logError("Fail to generate file", e);
                props.put("errormessage", e.getMessage());
            }
            props.put("errormessage", adhocQuery.getErrorMessage());
        } else if ("testhql".equals(mode)) {
            AdhocQueryService adhocQuery = new AdhocQueryService(this.sapphireConnection);
            props.put("list", adhocQuery.testHQL((String)props.get("sHQL")));
            props.put("errormessage", adhocQuery.getErrorMessage());
        } else if ("getadhocmetadata".equals(mode)) {
            AdhocMetaData adhocmetadata = SapphireHibernateUtil.getInstance(this.sapphireConnection).getAdhocMetaData();
            props.put("adhocmetadata", adhocmetadata);
        } else if ("getreferenceentityname".equals(mode)) {
            String referenceentityname = SapphireHibernateUtil.getInstance(this.sapphireConnection).getReferenceEntityName((String)props.get("tableid"), (String)props.get("columnid"));
            props.put("referenceentityname", referenceentityname);
        } else if ("securityfilterrequest".equals(mode)) {
            try {
                QueryService queryService = new QueryService(this.sapphireConnection);
                String sdcid = (String)props.get("sdcid");
                props.put("securityfilter", queryService.getSecurityFilterWhere(sdcid));
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
    }

    private AdhocQueryRequest getAdhocQueryRequest(HashMap props, String mode) {
        AdhocQueryRequest adhocRequest = new AdhocQueryRequest();
        String sdcid = (String)props.get("sdcid");
        if (props.get("adhocrequest") != null) {
            adhocRequest = (AdhocQueryRequest)props.get("adhocrequest");
        } else {
            String textsearchinput = (String)props.get("textsearchinput");
            if (textsearchinput != null && textsearchinput.length() > 0) {
                try {
                    if (!Indexer.getInstance(this.sapphireConnection.getDatabaseId()).isSearching()) {
                        throw new RuntimeException("Text Searching not enabled with the database.");
                    }
                    Searcher searcher = new Searcher(this.sapphireConnection);
                    String _rsetid = searcher.getRSet(sdcid, textsearchinput);
                    adhocRequest.setSearchWithinRset(_rsetid);
                }
                catch (Exception e) {
                    Trace.logError("Unknown exception while calling searcher getRSet:", e);
                }
            }
            String currentresultsdcid = (String)props.get("sdcid");
            String currentresultkeyid1 = (String)props.get("currentresultkeyid1");
            String currentresultkeyid2 = (String)props.get("currentresultkeyid2");
            String currentresultkeyid3 = (String)props.get("currentresultkeyid3");
            adhocRequest.setSdcid(sdcid);
            adhocRequest.setBetweenGroupBoolean((String)props.get("betweengroupboolean"));
            adhocRequest.setMaxResults(Integer.parseInt((String)props.get("maxresults")));
            adhocRequest.setArgumentFromXML((String)props.get("searchrequest"));
            String restrictiveWhere = (String)props.get("restrictivewhere");
            adhocRequest.setRestrictiveWhere(restrictiveWhere);
            String querytimeout = (String)props.get("querytimeout");
            if (adhocRequest.getSdcid().equals(currentresultsdcid)) {
                adhocRequest.setSearchWithinKeyid1(currentresultkeyid1);
                adhocRequest.setSearchWithinKeyid2(currentresultkeyid2);
                adhocRequest.setSearchWithinKeyid3(currentresultkeyid3);
            }
            if (querytimeout != null && querytimeout.length() > 0) {
                adhocRequest.setQueryTimeout(Integer.parseInt(querytimeout));
            }
            if ("adhocrequestcount".equals(mode)) {
                adhocRequest.setRequestCount(true);
            }
        }
        return adhocRequest;
    }
}

