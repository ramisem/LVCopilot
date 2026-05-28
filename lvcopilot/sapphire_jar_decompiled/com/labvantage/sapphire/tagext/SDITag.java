/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.pageelements.attachment.Files;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.QueryStatus;
import com.labvantage.sapphire.tagext.SDIFormTag;
import com.labvantage.sapphire.util.logger.LogUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDITag
extends BaseBodyTagSupport {
    protected SDIRequest _sdirequest = null;
    private SDIData _sdidata = null;
    private HashMap _querydatamap = new HashMap();
    private HashMap _querystatusmap = new HashMap();
    private SDIFormTag _sdiformtag = null;
    private boolean _retrieve = true;
    private boolean errorPage = true;
    private boolean _autolocktimeout = false;
    private String _showtemplates = "false";
    private String _retrieveString = "true";
    private String _autolocktimeoutString = "false";
    private String _propsMatch = "false";
    private String _rsetid = "";
    private String _queryid = "";
    private String _queryfrom = "";
    private String _querywhere = "";
    private String _altkeylist = "";
    private String _altkeycolumnid = "";
    private String _queryorderby = "";
    private String[] _queryparams = new String[12];
    private String _elementlist = "";
    private String _sdcid = "";
    private String _request = "primary";
    private String _keyid1list = "";
    private String _keyid2list = "";
    private String _keyid3list = "";
    private String _error = "";
    private String _sdierror = "";
    private String _nullvalue = "";
    private String _textsearch = "";
    private String _formcommand = "";
    private String _lockoption = "";
    private String _primarylock = "";
    private String _primaryfkcolumnlock = "";
    private String _datalock = "";
    private String _endstring = "";
    private String _paramlistidlist = "";
    private String _paramlistversionidlist = "";
    private String _variantidlist = "";
    private String _datasetlist = "";
    private String _var = "sdidata";
    private String _varStatus = "sdistatus";
    private String _id = "";
    private String _mergequerywhere = "N";
    private String _versionstatus = "";
    private String _retrievelimit = "0";
    private String _querytimeout = "";
    private String _showhiddenrecords = "N";
    private String _bypasssecuritycode = "0";
    private String _pageid = null;
    private String _pageedition = null;

    public void setElementlist(String elementlist) {
        this._elementlist = elementlist;
    }

    public void setQueryid(String queryid) {
        this._queryid = queryid;
    }

    public void setQueryfrom(String queryfrom) {
        this._queryfrom = queryfrom;
    }

    public void setQuerywhere(String querywhere) {
        this._querywhere = querywhere;
    }

    public void setAltkeylist(String altkeylist) {
        this._altkeylist = altkeylist;
    }

    public void setAltkeycolumnid(String altkeycolumnid) {
        this._altkeycolumnid = altkeycolumnid;
    }

    public void setMergequerywhere(String mergequerywhere) {
        this._mergequerywhere = mergequerywhere;
    }

    public void setRetrievelimit(String retrievelimit) {
        this._retrievelimit = retrievelimit;
    }

    public void setVersionstatus(String versionstatus) {
        this._versionstatus = versionstatus;
    }

    public void setQueryorderby(String queryorderby) {
        this._queryorderby = queryorderby;
    }

    public void setParam1(String param1) {
        this._queryparams[0] = param1;
    }

    public void setParam2(String param2) {
        this._queryparams[1] = param2;
    }

    public void setParam3(String param3) {
        this._queryparams[2] = param3;
    }

    public void setParam4(String param4) {
        this._queryparams[3] = param4;
    }

    public void setParam5(String param5) {
        this._queryparams[4] = param5;
    }

    public void setParam6(String param6) {
        this._queryparams[5] = param6;
    }

    public void setParam7(String param7) {
        this._queryparams[6] = param7;
    }

    public void setParam8(String param8) {
        this._queryparams[7] = param8;
    }

    public void setParam9(String param9) {
        this._queryparams[8] = param9;
    }

    public void setParam10(String param10) {
        this._queryparams[9] = param10;
    }

    public void setParam11(String param11) {
        this._queryparams[10] = param11;
    }

    public void setParam12(String param12) {
        this._queryparams[11] = param12;
    }

    public void setTextsearch(String textsearch) {
        this._textsearch = textsearch;
    }

    public void setSdcid(String sdcid) {
        this._sdcid = sdcid;
    }

    public String getSdcid() {
        return this._sdcid;
    }

    public void setKeyid1(String keyid1) {
        this._keyid1list = keyid1;
    }

    public void setKeyid2(String keyid2) {
        this._keyid2list = keyid2;
    }

    public void setKeyid3(String keyid3) {
        this._keyid3list = keyid3;
    }

    public void setParamlistid(String paramlistidlist) {
        this._paramlistidlist = paramlistidlist;
    }

    public void setParamlistversionid(String paramlistversionidlist) {
        this._paramlistversionidlist = paramlistversionidlist;
    }

    public void setVariantid(String variantidlist) {
        this._variantidlist = variantidlist;
    }

    public void setDataset(String datasetlist) {
        this._datasetlist = datasetlist;
    }

    public void setNullvalue(String nullvalue) {
        this._nullvalue = nullvalue;
    }

    public void setRequest(String request) {
        this._request = request;
    }

    public void setRetrieve(String retrieve) {
        this._retrieveString = retrieve;
    }

    public void setAutolocktimeout(String autolocktimeout) {
        this._autolocktimeoutString = autolocktimeout;
    }

    public String getAutolocktimeout() {
        return this._autolocktimeoutString;
    }

    public void setShowtemplates(String showtemplates) {
        this._showtemplates = showtemplates;
    }

    public void setShowhiddenrecords(String showhiddenrecords) {
        this._showhiddenrecords = showhiddenrecords;
    }

    public void setBypasssecuritycode(String bypasssecuritycode) {
        this._bypasssecuritycode = bypasssecuritycode;
    }

    public void setPropsmatch(String propsMatch) {
        this._propsMatch = propsMatch;
    }

    public void setLockoption(String lockoption) {
        this._lockoption = lockoption.equalsIgnoreCase("true") ? "DA" : (lockoption.equalsIgnoreCase("false") ? "" : (lockoption.equalsIgnoreCase("lockall") ? "DA" : (lockoption.equalsIgnoreCase("lockpartial") ? "LA" : "")));
    }

    public void setPrimarylock(String lock) {
        this._primarylock = lock.equalsIgnoreCase("true") ? "LA" : "";
    }

    public void setPrimaryfkcolumnlock(String fkcolumnlock) {
        this._primaryfkcolumnlock = fkcolumnlock;
    }

    public void setDatalock(String lock) {
        this._datalock = lock.equalsIgnoreCase("true") ? "LA" : "";
    }

    public String getLockoption() {
        return this._lockoption;
    }

    public String getPrimarylock() {
        return this._primarylock;
    }

    public String getDatalock() {
        return this._datalock;
    }

    public void setVar(String var) {
        this._var = var;
    }

    public void setVarStatus(String varStatus) {
        this._varStatus = varStatus;
    }

    public void setRsetid(String rsetid) {
        this._rsetid = rsetid;
    }

    public QueryData getData(String data) {
        return this.getData(data, "");
    }

    public QueryData getData(String data, String filter) {
        return this.getData(data, filter, ",");
    }

    public QueryData getData(String data, String filter, String filterseparator) {
        boolean filterset = filter != null && filter.length() > 0;
        HashMap<String, Object> datasetfilter = null;
        data = data.toLowerCase();
        if (filterset) {
            datasetfilter = new HashMap<String, Object>();
            if (filter.equalsIgnoreCase("primary")) {
                String[] keycols = this._sdidata.getKeys(data);
                String[] prkeycols = this._sdidata.getKeys("primary");
                QueryData prqd = (QueryData)this._querydatamap.get("primary");
                for (int i = 0; i < prkeycols.length; ++i) {
                    if (prkeycols[i] == null || prkeycols[i].length() <= 0) continue;
                    datasetfilter.put(keycols[i + 1], prqd.getValue(prkeycols[i], ""));
                }
            } else {
                String[] filterparts = StringUtil.split(filter, filterseparator);
                M18NUtil m18n = new M18NUtil(this.pageContext);
                block6: for (int i = 0; i < filterparts.length; ++i) {
                    String[] filterstmt = StringUtil.split(filterparts[i].trim(), "=");
                    if (filterstmt.length != 2) continue;
                    switch (this._sdidata.getDataset(data).getColumnType(filterstmt[0])) {
                        case 0: {
                            datasetfilter.put(filterstmt[0], filterstmt[1]);
                            continue block6;
                        }
                        case 1: {
                            datasetfilter.put(filterstmt[0], m18n.parseBigDecimal(filterstmt[1]));
                            continue block6;
                        }
                        case 2: {
                            datasetfilter.put(filterstmt[0], m18n.parseCalendar(filterstmt[1]));
                        }
                    }
                }
            }
            DataSet ds = this._sdidata.getDataset(data).getFilteredDataSet(datasetfilter);
            QueryData tempqd = (QueryData)this._querydatamap.get(data);
            tempqd.setQueryData(ds);
        }
        return (QueryData)this._querydatamap.get(data);
    }

    public String[] getKeyCols(String datasetname) {
        return this._sdidata.getKeys(datasetname);
    }

    public String[] getColumns(String datasetname) {
        return this._sdidata.getDataset(datasetname).getColumns();
    }

    public int getRequestStatus() {
        return this._sdidata.getRequestStatus();
    }

    public boolean isSDIForm() {
        return this._sdiformtag != null;
    }

    public String getSDIError() {
        return this._sdierror;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getId() {
        return this._id;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.errorPage = true;
        this.doInit();
        if ("true".equals(this.requestContext.getProperty("__hasoldformsucesstag")) && "true".equals(this.requestContext.getProperty("__formsuccess"))) {
            return 0;
        }
        if (this.isControlledPage()) {
            this._sdiformtag = (SDIFormTag)TagSupport.findAncestorWithClass((Tag)this, SDIFormTag.class);
            this.evaluateExpressions();
            if (this.start() == 1) {
                rc = 2;
            } else if (this.errorPage) {
                if (this._error.indexOf("ORA-01013") >= 0 || this._error.indexOf("Query was cancelled or timed out") >= 0) {
                    this._error = "<font color=\"red\">" + new TranslationProcessor(this.pageContext).translate("Your query has been timed out. Please refine your query and search again.") + "</font>";
                } else {
                    this._error = "";
                    try {
                        this.goErrorPage("<font color=\"red\">SDITag failed to retrieve data!</font><br/>" + this._error);
                    }
                    catch (IllegalStateException e) {
                        this._error = "<font color=\"red\">SDITag failed to retrieve data!</font><br/>" + this._error;
                    }
                }
            }
        } else {
            this.goErrorPage("RequestContext or controlled page tag does not exist. Tags can only be used via the Request Controller and in a controlled page.");
        }
        return rc;
    }

    public int start() throws JspTagException {
        int rc = 1;
        if (Trace.on) {
            this.logTrace("Request Properties: " + this.requestContext.getPropertyList().toString());
        }
        if (this.getSDIData() == 1) {
            if (Trace.on) {
                this.logTrace("Data for SDI retrieved successfully... reconciling form propeties with data");
            }
            if (this.pageContext != null) {
                SDCProcessor sdcProc = new SDCProcessor(this.pageContext);
                PropertyList sdcProps = sdcProc.getProperties(this._sdcid);
                sdcProps.setProperty("columns", sdcProc.getColumns(this._sdcid));
                this.write(JavaScriptAPITag.getSDCScript(sdcProps, true));
            }
            this.reconcileFormProperties();
            SDITagInfo taginfo = new SDITagInfo(this._querydatamap);
            taginfo.setSdcid(this._sdcid);
            taginfo.setError(this._sdierror);
            taginfo.setKeycols(this._sdidata.getKeys("primary"));
            taginfo.setSDIData(this._sdidata);
            taginfo.setSDIRequest(this._sdirequest);
            taginfo.setPageContext(this.pageContext);
            this._sdirequest = null;
            this.pageContext.setAttribute("sdiinfo", (Object)taginfo);
            this.pageContext.setAttribute(this._var, (Object)this._querydatamap);
            this.pageContext.setAttribute(this._varStatus, (Object)this._querystatusmap);
            boolean useTagsInCore = false;
            try {
                InputStream in = this.pageContext.getServletContext().getResource("/WEB-CORE/scripts/tags.js").openStream();
                if (in != null) {
                    useTagsInCore = true;
                    in.close();
                }
            }
            catch (Exception in) {
                // empty catch block
            }
            if (useTagsInCore) {
                this.write("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
            } else {
                this.write("<script language=\"JavaScript\" src=\"tags.js\"></script>\n");
            }
            if (this.isSDIForm()) {
                this._sdiformtag.addPrefix(this.getId());
                this._sdiformtag.addRset(this._rsetid);
                if (this._lockoption.length() > 0 || this._primarylock.equals("LA") || this._datalock.equals("LA")) {
                    this._sdiformtag.setRsetLockOption("", "", "");
                    this._sdiformtag.setPing(true);
                    this.write(this._sdiformtag.getPingHtml());
                }
                String SEP = "{+}";
                StringBuffer inputs = new StringBuffer("<input type=\"hidden\" name=\"__" + this._id + "attributes\" id=\"__" + this._id + "attributes\" value=\"" + this._queryid + SEP + this._queryparams[0] + SEP + this._queryparams[1] + SEP + this._queryparams[2] + SEP + this._queryparams[3] + SEP + this._queryparams[4] + SEP + this._queryparams[5] + SEP + this._queryparams[6] + SEP + this._queryparams[7] + SEP + this._queryparams[8] + SEP + this._queryparams[9] + SEP + this._queryparams[10] + SEP + this._queryparams[11] + SEP + EncryptDecrypt.obfsql(this._queryfrom) + SEP + EncryptDecrypt.obfsql(this._querywhere) + SEP + EncryptDecrypt.obfsql(this._queryorderby) + SEP + this._sdcid + SEP + this._keyid1list + SEP + this._keyid2list + SEP + this._keyid3list + SEP + this._rsetid + SEP + EncryptDecrypt.obfsql(this._request) + SEP + this._lockoption + SEP + this._nullvalue + SEP + (this._retrieve ? "true" : "false") + SEP + this._propsMatch + SEP + this._mergequerywhere + SEP + this._versionstatus + SEP + this._retrievelimit + SEP + this._pageid + SEP + this._pageedition + "\">\n");
                this.write(inputs.toString());
            }
            rc = 1;
        } else {
            if (Trace.on) {
                this.logTrace("Data for SDI could not be retrieved");
            }
            rc = 2;
        }
        return rc;
    }

    public int createSDIRequest() {
        SDCProcessor sdcProcessor;
        int rc = 1;
        this._sdirequest = new SDIRequest();
        this._sdirequest.setExtendedDataTypes(true);
        this._sdirequest.setPropsMatch(this._propsMatch.equals("true"));
        int retrievelimit = 0;
        try {
            retrievelimit = Integer.parseInt(this._retrievelimit);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this._sdirequest.setRetrieveLimit(retrievelimit);
        if (this._querytimeout.length() > 0) {
            this._sdirequest.setQueryTimeout(Integer.parseInt(this._querytimeout));
        }
        this._sdirequest.setVersionStatus(this._versionstatus);
        if (this._retrieve) {
            this._sdirequest.setShowHiddenRecords("Y".equals(this._showhiddenrecords));
            int bypasscode = 0;
            try {
                bypasscode = Integer.parseInt(this._bypasssecuritycode);
            }
            catch (Exception exception) {
                // empty catch block
            }
            this._sdirequest.setSecurityBypassCode(bypasscode);
            if (this._textsearch != null && this._textsearch.length() > 0) {
                try {
                    ConnectionProcessor connectionProcessor = new ConnectionProcessor(this.pageContext);
                    Searcher searcher = new Searcher(connectionProcessor.getSapphireConnection());
                    this._rsetid = searcher.getRSet(this._sdcid, this._textsearch);
                }
                catch (Exception e) {
                    rc = 2;
                    this._error = "Failed to get search results with text searching. Reason: " + e.getMessage();
                }
            }
            if (rc == 1) {
                if (this._rsetid != null && this._rsetid.length() > 0) {
                    if (Trace.on) {
                        this.logTrace("RSet SDIRequest: " + this._rsetid);
                    }
                    this._sdirequest.setSDCid(this._sdcid);
                    this._sdirequest.setRsetid(this._rsetid);
                } else if (this._altkeycolumnid != null && this._altkeycolumnid.length() > 0) {
                    this._sdirequest.setSDCid(this._sdcid);
                    this._sdirequest.setAltKeyColumnId(this._altkeycolumnid);
                    this._sdirequest.setAltKeyList(this._altkeylist);
                    this._sdirequest.setQueryWhere(this._querywhere);
                } else if (this._queryid != null && this._queryid.length() > 0) {
                    if (Trace.on) {
                        this.logTrace("Query SDIRequest: " + this._queryid);
                    }
                    this._sdirequest.setSDCid(this._sdcid);
                    this._sdirequest.setQueryid(this._queryid);
                    this._sdirequest.setQueryParams(this._queryparams);
                    if ("Y".equals(this._mergequerywhere)) {
                        if (Trace.on) {
                            this.logTrace("Query SDIRequest: " + this._queryid + " with additional whereclause: " + this._querywhere);
                        }
                        this._sdirequest.setQueryWhere(this._querywhere);
                    }
                } else if (this._queryfrom != null && this._queryfrom.length() > 0) {
                    if (Trace.on) {
                        this.logTrace("QueryPart SDIRequest: " + this._queryfrom + " : " + this._querywhere + " : " + this._queryorderby);
                    }
                    this._sdirequest.setSDCid(this._sdcid);
                    this._sdirequest.setQueryFrom(this._queryfrom);
                    this._sdirequest.setQueryWhere(this._querywhere);
                    this._sdirequest.setQueryOrderBy(this._queryorderby);
                } else if (this._sdcid != null && this._sdcid.length() > 0 && this._keyid1list != null && this._keyid1list.length() > 0) {
                    if (Trace.on) {
                        this.logTrace("Keyid SDIRequest: " + this._keyid1list + ", " + this._keyid2list + ", " + this._keyid3list);
                    }
                    this._sdirequest.setSDIList(this._sdcid, this._keyid1list, this._keyid2list, this._keyid3list);
                    if ("Y".equals(this._mergequerywhere)) {
                        if (Trace.on) {
                            this.logTrace("Keyid SDIRequest with additional whereclause: " + this._querywhere);
                        }
                        this._sdirequest.setQueryWhere(this._querywhere);
                    }
                } else {
                    this._error = this._error + "TAG ERROR: queryid/queryfrom or sdcid/keyid attributes not specified in sdi tag.";
                    rc = 2;
                }
                if (rc == 1 && this._primaryfkcolumnlock != null && this._primaryfkcolumnlock.length() > 0) {
                    this._sdirequest.setPrimaryFKColumnLock(this._primaryfkcolumnlock);
                }
            }
        } else if (this._sdcid != null && this._sdcid.length() > 0) {
            if (Trace.on) {
                this.logTrace("Metadata SDIRequest");
            }
            this._sdirequest.setSDCid(this._sdcid);
            this._sdirequest.setRetrieve(false);
        } else {
            this._error = this._error + "TAG ERROR: sdcid not specified in sdi tag.";
            rc = 2;
        }
        if (rc == 1) {
            this._sdirequest.setParamlistidList(this._paramlistidlist);
            this._sdirequest.setParamlistversionidList(this._paramlistversionidlist);
            this._sdirequest.setVariantidList(this._variantidlist);
            this._sdirequest.setDatasetList(this._datasetlist);
            String[] request = RequestParser.parseRequestItem(this._request);
            for (int i = 0; i < request.length; ++i) {
                this._sdirequest.setRequestItem(request[i].trim());
            }
            if (this.isSDIForm() && (this._lockoption.length() > 0 || this._primarylock.length() > 0 || this._datalock.length() > 0)) {
                this._sdirequest.setAutoLockTimeout(this._autolocktimeout);
            }
            if (this.isSDIForm() && (this._lockoption.length() > 0 || this._primarylock.equals("LA") || this._datalock.equals("LA"))) {
                this._sdirequest.setRetainRsetid(true);
                String changecontrolflag = CMTPolicy.getPolicy(this.getConnectionId(), this._sdcid).getChangeControlledFlag();
                boolean validateCheckout = "Y".equals(changecontrolflag) || "T".equals(changecontrolflag) && "true".equals(this._showtemplates);
                this._sdirequest.setValidateCheckout(validateCheckout);
            }
            this._sdirequest.setShowTemplates(this._showtemplates);
            if (this._lockoption.length() > 0 && (this._primarylock.equals("LA") || this._datalock.equals("LA"))) {
                this._error = this._error + "TAG ERROR: lock options are incompatable.";
                rc = 2;
            } else {
                this._sdirequest.setLockOption(this._lockoption);
                this._sdirequest.setPrimaryLockOption(this._primarylock);
                this._sdirequest.setDataLockOption(this._datalock);
            }
        }
        if (this._sdirequest.getSDCid() != null && this._sdirequest.getSDCid().length() > 0 && "Y".equalsIgnoreCase((sdcProcessor = new SDCProcessor(this.pageContext)).getProperty(this._sdirequest.getSDCid(), "maskableflag", "N"))) {
            this._sdirequest.setReturnMaskedData(true);
        }
        return rc;
    }

    public int getSDIData() {
        int rc = this.createSDIRequest();
        if (rc == 1) {
            if (Trace.on) {
                this.logTrace("Retrieving data for SDITag");
            }
            SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
            this._sdidata = sdiProcessor.getSDIData(this._sdirequest);
            if (this._sdidata != null) {
                this._rsetid = this._sdidata.getRsetid();
                this.pageContext.setAttribute("__sdirequestUsed", (Object)this._sdirequest);
                this.pageContext.setAttribute("__rsetidRetained", (Object)this._rsetid);
            } else {
                if (Trace.on) {
                    this.logTrace("Error retrieving data. Reason: " + sdiProcessor.getLastError());
                }
                this._error = LogUtil.getStackTraceMessages(sdiProcessor.getLastException(), "<br/>", true, true);
                if (this._error.toLowerCase().contains("global query limit")) {
                    this.errorPage = false;
                }
                rc = 2;
            }
        } else {
            this.logError(this._error);
        }
        return rc;
    }

    public void reconcileFormProperties() {
        String[] commands = StringUtil.split(this._formcommand, "|");
        for (String datasetname : this._sdidata.getDatasets()) {
            if (Trace.on) {
                this.logTrace("Reconciling " + datasetname + "...");
            }
            String datasetcode = SDIData.getDatasetCode(datasetname);
            DataSet dataset = this._sdidata.getDataset(datasetname);
            for (int i = 0; i < dataset.getRowCount(); ++i) {
                dataset.setString(i, "__rowstatus", "S");
                dataset.setString(i, "__rowid", String.valueOf(i));
            }
            boolean doaddrow = false;
            boolean refreshdataset = false;
            if (this._formcommand.regionMatches(true, 0, datasetcode + "_addrow", 0, 9)) {
                doaddrow = true;
                refreshdataset = true;
            } else if (!this._formcommand.equalsIgnoreCase("save")) {
                refreshdataset = true;
            }
            if (refreshdataset) {
                String[] keycols = this._sdidata.getKeys(datasetname);
                String[] datasetcols = dataset.getColumns();
                String keyvalue = "";
                HashMap findmap = new HashMap();
                int findrow = -1;
                boolean morerows = true;
                int row = 0;
                while (morerows) {
                    boolean autoBlankRow = this.requestContext.getProperty("__" + datasetname + row + "_rs").equalsIgnoreCase("A");
                    keyvalue = this.requestContext.getProperty("__" + datasetcode + String.valueOf(row) + "_key");
                    if (keyvalue != null && keyvalue.length() > 0) {
                        int col;
                        String[] keyvals = StringUtil.split(keyvalue, ";");
                        if (datasetname.equalsIgnoreCase("sdclink") && keyvalue.contains("+")) {
                            String[] newkeyvals = Arrays.copyOf(keyvals, keyvals.length + 1);
                            newkeyvals[1] = keyvals[1].substring(0, keyvals[1].indexOf("+"));
                            newkeyvals[2] = keyvals[1].substring(keyvals[1].indexOf("+") + 1);
                            keyvals = newkeyvals;
                        }
                        findmap.clear();
                        for (int k = 0; k < keycols.length && k < keyvals.length; ++k) {
                            if (keycols[k] == null || keycols[k].length() <= 0) continue;
                            dataset.populateFindMap(keycols[k], keyvals[k].trim(), findmap);
                        }
                        findrow = dataset.findRow(findmap);
                        if (findrow > -1) {
                            if (Trace.on) {
                                this.logTrace(keyvalue + " found at row " + findrow + " - overwriting changed proprties...");
                            }
                            for (col = 0; col < datasetcols.length; ++col) {
                                if (!this.requestContext.isProperty(datasetcode + String.valueOf(row) + "_" + datasetcols[col])) continue;
                                dataset.setValue(findrow, datasetcols[col], this.requestContext.getProperty(datasetcode + String.valueOf(row) + "_" + datasetcols[col]));
                            }
                        } else {
                            if (Trace.on) {
                                this.logTrace(keyvalue + " previously added");
                            }
                            findrow = dataset.addRow();
                            dataset.setString(findrow, "__rowstatus", autoBlankRow ? "A" : "I");
                            dataset.setString(findrow, "__rowid", String.valueOf(findrow));
                            for (col = 0; col < datasetcols.length; ++col) {
                                for (int k = 0; k < keycols.length; ++k) {
                                    if (!keycols[k].equals(datasetcols[col]) || keyvals[k].equals("(null)")) continue;
                                    dataset.setValue(findrow, datasetcols[col], keyvals[k]);
                                }
                                if (!this.requestContext.isProperty(datasetcode + String.valueOf(row) + "_" + datasetcols[col])) continue;
                                dataset.setValue(findrow, datasetcols[col], this.requestContext.getProperty(datasetcode + String.valueOf(row) + "_" + datasetcols[col]));
                            }
                        }
                        if (this.requestContext.getProperty("__" + datasetcode + String.valueOf(row) + "_rs").equals("U")) {
                            dataset.setString(findrow, "__rowstatus", "U");
                        }
                        if (this.requestContext.getProperty("__" + datasetcode + String.valueOf(row) + "_rs").equals("D")) {
                            dataset.setString(findrow, "__rowstatus", "D");
                        }
                    } else {
                        morerows = false;
                    }
                    ++row;
                }
            }
            if (doaddrow) {
                for (int cmd = 0; cmd < commands.length; ++cmd) {
                    if (!commands[cmd].startsWith(datasetcode + "_addrow")) continue;
                    String[] addcmd = StringUtil.split(commands[cmd], ";");
                    int addrows = 1;
                    int colstart = 2;
                    if (addcmd.length > 1) {
                        try {
                            addrows = Integer.parseInt(addcmd[1]);
                        }
                        catch (NumberFormatException nfe) {
                            colstart = 1;
                        }
                    }
                    String trace = "Adding " + String.valueOf(addrows) + " new rows - column values are";
                    for (int addrow = 0; addrow < addrows; ++addrow) {
                        int newrow = dataset.addRow();
                        dataset.setString(newrow, "__rowstatus", "I");
                        dataset.setString(newrow, "__rowid", String.valueOf(newrow));
                        for (int i = colstart; i < addcmd.length; ++i) {
                            String[] addcols = StringUtil.split(addcmd[i], "=");
                            if (addcols.length != 2 || addcols[0].length() <= 0 || addcols[1].length() <= 0) continue;
                            dataset.setValue(newrow, addcols[0], addcols[1]);
                            trace = trace + ": " + addcols[0] + "=" + addcols[1];
                        }
                    }
                    if (!Trace.on) continue;
                    this.logTrace(trace);
                }
            }
            QueryData qd = new QueryData(datasetname, dataset);
            qd.setNullValue(this._nullvalue);
            this._querydatamap.put(datasetname, qd);
            this._querystatusmap.put(datasetname, new QueryStatus(qd));
        }
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this.isSDIForm()) {
            this.write("<script>\n");
            if (this.pageContext.getAttribute("__currentindex") == null) {
                this.write("var __currentindex = new Array();\n");
                this.pageContext.setAttribute("__currentindex", (Object)"Y");
            }
            for (String datasetname : this._sdidata.getDatasets()) {
                if (!this._querydatamap.containsKey(datasetname)) continue;
                QueryData qd = (QueryData)this._querydatamap.get(datasetname);
                this.write("__currentindex[\"" + this._id + datasetname + "\"] = " + (qd.getRowCount() - 1) + ";\n");
            }
            if (this._rsetid != null && this._rsetid.length() > 0) {
                this.write("sdiAddRSet('" + this._rsetid + "');\n");
            }
            this.write("</script>\n");
        }
        if (Trace.on) {
            this.logTrace("Ending SDITag");
        }
        if (this._endstring.length() > 0) {
            this.write(this._endstring);
        }
        if (this._error.length() > 0) {
            this.write(StringUtil.replaceAll(this._error, "\n", "<br/>"));
        }
        this._sdirequest = null;
        this._sdidata = null;
        this._querydatamap = new HashMap();
        this._querystatusmap = new HashMap();
        this._sdiformtag = null;
        this._retrieve = true;
        this.errorPage = true;
        this._autolocktimeout = false;
        this._showtemplates = "false";
        this._retrieveString = "true";
        this._autolocktimeoutString = "false";
        this._propsMatch = "false";
        this._rsetid = "";
        this._queryid = "";
        this._queryfrom = "";
        this._querywhere = "";
        this._queryorderby = "";
        this._queryparams = new String[12];
        this._elementlist = "";
        this._sdcid = "";
        this._request = "primary";
        this._keyid1list = "";
        this._keyid2list = "";
        this._keyid3list = "";
        this._error = "";
        this._sdierror = "";
        this._nullvalue = "";
        this._formcommand = "";
        this._lockoption = "";
        this._primarylock = "";
        this._datalock = "";
        this._endstring = "";
        this._paramlistidlist = "";
        this._paramlistversionidlist = "";
        this._variantidlist = "";
        this._datasetlist = "";
        this._var = "sdidata";
        this._varStatus = "sdistatus";
        this._id = "";
        this._mergequerywhere = "N";
        this._versionstatus = "";
        this._retrievelimit = "0";
        this._showhiddenrecords = "N";
        this._bypasssecuritycode = "0";
        this._altkeycolumnid = "";
        this._altkeylist = "";
        this._pageid = "";
        this._pageedition = "";
        super.doEndTag();
        return rc;
    }

    public static void processPageRequest(StringBuffer _keyid1list, StringBuffer _keyid2list, StringBuffer _keyid3list, String page, String rowsperpage) {
        try {
            int pageno = Integer.parseInt(page);
            int numperpage = Integer.parseInt(rowsperpage);
            if (pageno > 0 && numperpage > 0) {
                String[] keyid1s = StringUtil.split(_keyid1list.toString(), ";");
                String[] keyid2s = StringUtil.split(_keyid2list.toString(), ";");
                String[] keyid3s = StringUtil.split(_keyid3list.toString(), ";");
                StringBuffer keyid1list = new StringBuffer();
                StringBuffer keyid2list = new StringBuffer();
                StringBuffer keyid3list = new StringBuffer();
                boolean hasKeyid2 = false;
                boolean hasKeyid3 = false;
                if (keyid2s.length == keyid1s.length) {
                    hasKeyid2 = true;
                }
                if (keyid3s.length == keyid1s.length) {
                    hasKeyid3 = true;
                }
                int startrow = (pageno - 1) * numperpage;
                int endrow = pageno * numperpage < keyid1s.length ? pageno * numperpage : keyid1s.length;
                for (int i = startrow; i < endrow; ++i) {
                    keyid1list.append(";" + keyid1s[i]);
                    if (!hasKeyid2) continue;
                    keyid2list.append(";" + keyid2s[i]);
                    if (!hasKeyid3) continue;
                    keyid3list.append(";" + keyid3s[i]);
                }
                _keyid1list.delete(0, _keyid1list.length());
                _keyid1list.append(keyid1list.toString().substring(1));
                if (hasKeyid2) {
                    _keyid2list.delete(0, _keyid2list.length());
                    _keyid2list.append(keyid2list.toString().substring(1));
                    if (hasKeyid3) {
                        _keyid3list.delete(0, _keyid3list.length());
                        _keyid3list.append(keyid3list.toString().substring(1));
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            Trace.logError("Pageno or rowsperpage is not a number!", e);
        }
    }

    private void evaluateExpressions() {
        String tagRequest = this.requestContext.getProperty("__tagrequest");
        if (tagRequest != null && tagRequest.equals("sdiform") && this.isSDIForm()) {
            this._sdcid = this.requestContext.getProperty("__sdcid");
            this._queryid = this.requestContext.getProperty("__queryid");
            this._queryparams[0] = this.requestContext.getProperty("__param1");
            this._queryparams[1] = this.requestContext.getProperty("__param2");
            this._queryparams[2] = this.requestContext.getProperty("__param3");
            this._queryparams[3] = this.requestContext.getProperty("__param4");
            this._queryparams[4] = this.requestContext.getProperty("__param5");
            this._queryparams[5] = this.requestContext.getProperty("__param6");
            this._queryparams[6] = this.requestContext.getProperty("__param7");
            this._queryparams[7] = this.requestContext.getProperty("__param8");
            this._queryparams[8] = this.requestContext.getProperty("__param9");
            this._queryparams[9] = this.requestContext.getProperty("__param10");
            this._queryparams[10] = this.requestContext.getProperty("__param11");
            this._queryparams[11] = this.requestContext.getProperty("__param12");
            this._queryfrom = EncryptDecrypt.unobfsql(this.requestContext.getProperty("__queryfrom"));
            this._querywhere = EncryptDecrypt.unobfsql(this.requestContext.getProperty("__querywhere"));
            this._queryorderby = EncryptDecrypt.unobfsql(this.requestContext.getProperty("__queryorderby"));
            this._keyid1list = this.requestContext.getProperty("__keyid1");
            this._keyid2list = this.requestContext.getProperty("__keyid2");
            this._keyid3list = this.requestContext.getProperty("__keyid3");
            this._rsetid = this.requestContext.getProperty("__rsetid");
            this._request = EncryptDecrypt.unobfsql(this.requestContext.getProperty("__request"));
            this._lockoption = this.requestContext.getProperty("__lockoption");
            this._nullvalue = this.requestContext.getProperty("__nullvalue");
            this._retrieve = Boolean.valueOf(JstlUtil.evaluateExpression(this._retrieveString, this.pageContext, "").toString());
            this._pageid = this.requestContext.getProperty("__pageid");
            this._pageedition = this.requestContext.getProperty("__pageedition");
            this._autolocktimeout = Boolean.valueOf(JstlUtil.evaluateExpression(this._autolocktimeoutString, this.pageContext, "").toString());
            this._formcommand = this.requestContext.getProperty("__formcommand");
            this._propsMatch = this.requestContext.getProperty("__propsmatch");
            this._mergequerywhere = this.requestContext.getProperty("__mergequerywhere");
            this._versionstatus = this.requestContext.getProperty("__versionstatus");
            this._retrievelimit = this.requestContext.getProperty("__retrievelimit");
        } else {
            String page;
            this._sdcid = JstlUtil.evaluateExpression(this._sdcid, this.pageContext, "").toString();
            this._id = JstlUtil.evaluateExpression(this._id, this.pageContext, "").toString();
            this._keyid1list = JstlUtil.evaluateExpression(this._keyid1list, this.pageContext, "").toString();
            this._keyid2list = JstlUtil.evaluateExpression(this._keyid2list, this.pageContext, "").toString();
            this._keyid3list = JstlUtil.evaluateExpression(this._keyid3list, this.pageContext, "").toString();
            this._queryid = JstlUtil.evaluateExpression(this._queryid, this.pageContext, "").toString();
            if (this._queryparams != null) {
                if (this._queryparams.length > 0) {
                    this._queryparams[0] = JstlUtil.evaluateExpression(this._queryparams[0], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 1) {
                    this._queryparams[1] = JstlUtil.evaluateExpression(this._queryparams[1], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 2) {
                    this._queryparams[2] = JstlUtil.evaluateExpression(this._queryparams[2], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 3) {
                    this._queryparams[3] = JstlUtil.evaluateExpression(this._queryparams[3], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 4) {
                    this._queryparams[4] = JstlUtil.evaluateExpression(this._queryparams[4], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 5) {
                    this._queryparams[5] = JstlUtil.evaluateExpression(this._queryparams[5], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 6) {
                    this._queryparams[6] = JstlUtil.evaluateExpression(this._queryparams[6], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 7) {
                    this._queryparams[7] = JstlUtil.evaluateExpression(this._queryparams[7], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 8) {
                    this._queryparams[8] = JstlUtil.evaluateExpression(this._queryparams[8], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 9) {
                    this._queryparams[9] = JstlUtil.evaluateExpression(this._queryparams[9], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 10) {
                    this._queryparams[10] = JstlUtil.evaluateExpression(this._queryparams[10], this.pageContext, "").toString();
                }
                if (this._queryparams.length > 11) {
                    this._queryparams[11] = JstlUtil.evaluateExpression(this._queryparams[11], this.pageContext, "").toString();
                }
            }
            this._queryfrom = JstlUtil.evaluateExpression(EncryptDecrypt.unobfsql(this._queryfrom), this.pageContext, "").toString();
            this._querywhere = JstlUtil.evaluateExpression(EncryptDecrypt.unobfsql(this._querywhere), this.pageContext, "").toString();
            this._queryorderby = JstlUtil.evaluateExpression(EncryptDecrypt.unobfsql(this._queryorderby), this.pageContext, "").toString();
            this._request = JstlUtil.evaluateExpression(EncryptDecrypt.unobfsql(this._request), this.pageContext, "").toString();
            this._versionstatus = JstlUtil.evaluateExpression(this._versionstatus, this.pageContext, "").toString();
            this._mergequerywhere = JstlUtil.evaluateExpression(this._mergequerywhere, this.pageContext, "").toString();
            this._textsearch = JstlUtil.evaluateExpression(this._textsearch, this.pageContext, "").toString();
            if (this._keyid1list != null && this._keyid1list.length() > 0 && (page = this.pageContext.getRequest().getParameter("pageno")) != null && page.length() > 0) {
                StringBuffer k1 = new StringBuffer(this._keyid1list);
                StringBuffer k2 = new StringBuffer(this._keyid2list);
                StringBuffer k3 = new StringBuffer(this._keyid3list);
                SDITag.processPageRequest(k1, k2, k3, page, this.pageContext.getRequest().getParameter("rowsperpage"));
                this._keyid1list = k1.toString();
                this._keyid2list = k2.toString();
                this._keyid3list = k3.toString();
            }
            HashMap<String, PropertyList> elementmap = new HashMap<String, PropertyList>();
            if (this._elementlist.length() > 0) {
                String[] elementids = StringUtil.split(JstlUtil.evaluateExpression(this._elementlist, this.pageContext, "").toString(), ",");
                for (int e = 0; e < elementids.length; ++e) {
                    elementids[e] = elementids[e].trim();
                    PropertyList element = null;
                    element = this.requestContext.getPropertyList().getPropertyList(elementids[e]);
                    if (element == null) continue;
                    elementmap.put(elementids[e], element);
                }
            }
            boolean notPassedIn = this._request.equals("primary");
            Set keys = elementmap.keySet();
            for (String elementid : keys) {
                Object linkcolist;
                PropertyList element = (PropertyList)elementmap.get(elementid);
                if (element != null) {
                    element.setProperty("_prefix", this.getId());
                }
                String objectname = element.getProperty("objectname");
                if (elementid.equals("list") || elementid.equals("maint") || objectname.equals("com.labvantage.sapphire.pageelements.list.List") || objectname.equals("com.labvantage.sapphire.pageelements.maint.Maint") || objectname.equals("com.labvantage.sapphire.modules.dashboard.gizmos.ListGizmo")) {
                    if (this._sdcid == null || this._sdcid.length() == 0) {
                        this._sdcid = JstlUtil.evaluateExpression(this._sdcid, this.pageContext, element != null ? element.getProperty("sdcid") : "").toString();
                    }
                    if ((notPassedIn || this._request.indexOf("primary[") < 0) && element != null) {
                        PropertyListCollection columns = element.getCollection("columns");
                        PropertyListCollection groupby = element.getCollection("groupby");
                        PropertyListCollection sortby = element.getCollection("sortby");
                        linkcolist = new ArrayList();
                        this.getRequestItemCols(columns, (ArrayList)linkcolist);
                        if (groupby != null && groupby.size() > 0) {
                            String groupbycolid = groupby.getPropertyList(0).getProperty("columnid");
                            PropertyListCollection groupbycols = groupby.getPropertyList(0).getCollection("columns");
                            if (groupbycolid.length() > 0 && ((ArrayList)linkcolist).indexOf(groupbycolid) < 0) {
                                ((ArrayList)linkcolist).add(groupbycolid);
                            }
                            if (groupbycols != null) {
                                this.getRequestItemCols(groupbycols, (ArrayList)linkcolist);
                            }
                        }
                        if (sortby != null && sortby.size() > 0) {
                            this.getRequestItemCols(sortby, (ArrayList)linkcolist);
                        }
                        if (((ArrayList)linkcolist).size() > 0) {
                            this._request = this._request.replaceFirst("primary", "primary[" + this.getColumnList((ArrayList)linkcolist) + "]");
                        }
                    }
                    if (objectname.equals("com.labvantage.sapphire.pageelements.list.List")) {
                        if ("0".equals(this._retrievelimit)) {
                            this._retrievelimit = element.getProperty("retrievelimit");
                        }
                        this._querytimeout = element.getProperty("querytimeout");
                        if (this._versionstatus == null || this._versionstatus.length() == 0) {
                            this._versionstatus = element.getProperty("versionstatus");
                        }
                        if ("N".equals(this._mergequerywhere)) {
                            this._mergequerywhere = element.getProperty("mergequerywhere");
                        }
                    }
                    if (!objectname.equals("com.labvantage.sapphire.pageelements.maint.Maint")) continue;
                    this._request = this._request + ",notes";
                    continue;
                }
                if (objectname.equals("com.labvantage.sapphire.pageelements.dataentry.DataEntryList")) {
                    String[] cols = new String[]{"primarycolumns", "datasetcolumns", "dataitemcolumns"};
                    for (int i = 0; i < cols.length; ++i) {
                        PropertyListCollection columns = element.getCollection(cols[i]);
                        String dataset = i == 0 ? "primary" : (i == 1 ? "dataset" : "dataitem");
                        ArrayList columnlist = new ArrayList();
                        this.getRequestItemCols(columns, columnlist);
                        String linkcolist2 = this.getColumnList(columnlist);
                        if (linkcolist2.length() > 0) {
                            if (notPassedIn && !dataset.equals("primary") && this._request.indexOf(dataset) < 0) {
                                this._request = this._request + "," + dataset + "[" + linkcolist2 + "]";
                                continue;
                            }
                            if (this._request.indexOf(dataset) < 0 || this._request.indexOf(dataset + "[") > 0) continue;
                            this._request = this._request.replaceFirst(dataset, dataset + "[" + linkcolist2 + "]");
                            continue;
                        }
                        this._request = this._request + "," + dataset;
                    }
                    continue;
                }
                String dataset = element.getProperty("datasetname");
                if (dataset.length() == 0) {
                    if (objectname.equals("com.labvantage.sapphire.pageelements.dataentry.DataEntryGrid") || objectname.equals("com.labvantage.sapphire.pageelements.gwt.server.GWTDataEntry")) {
                        dataset = "dataitem";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintDataSet")) {
                        dataset = "dataset";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintAddress")) {
                        dataset = "address";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintAttachment")) {
                        dataset = "attachment";
                    } else if (objectname.equals(Files.class.getName())) {
                        dataset = "attachment";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintSpec")) {
                        dataset = "spec";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintWorkflow")) {
                        dataset = "workflow";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintAttribute")) {
                        dataset = "attribute";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintAttachmentOperation")) {
                        dataset = "attachmentoperation";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintDataCapture")) {
                        dataset = "datacapture";
                    } else if (objectname.equals("com.labvantage.sapphire.pageelements.maint.SDCLinkMaint") || objectname.equals("com.labvantage.sapphire.pageelements.maint.MaintDetail") && element.getProperty("propertytreeid").equals("manytomany")) {
                        HashMap linkProps;
                        PropertyList detail;
                        PropertyList link;
                        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
                        String linkid = element.getProperty("linkid");
                        if ((linkid == null || linkid.trim().length() == 0) && (link = element.getPropertyList("link")) != null && (detail = link.getPropertyList("detail")) != null) {
                            linkid = detail.getProperty("linkid");
                        }
                        if ((linkProps = sdcProcessor.getLinkProperties(element.getProperty("sdcid"), linkid)) != null) {
                            dataset = (String)linkProps.get("linktableid");
                        } else {
                            Trace.logWarn("Could not obtain link properties. If you are using a Foreign key or Relational link then you should not include the element in the request.");
                        }
                    }
                }
                if (dataset.length() == 0) {
                    dataset = elementid;
                }
                if (!notPassedIn && this._request.indexOf(dataset) >= 0 && (this._request.indexOf(dataset) < 0 || this._request.indexOf(dataset + "[") >= 0) || element == null) continue;
                PropertyListCollection columns = element.getCollection("columns");
                ArrayList columnlist = new ArrayList();
                this.getRequestItemCols(columns, columnlist);
                linkcolist = this.getColumnList(columnlist);
                if (((String)linkcolist).length() > 0) {
                    if (notPassedIn || this._request.indexOf(dataset) < 0) {
                        this._request = this._request + "," + dataset + "[" + (String)linkcolist + "]";
                        continue;
                    }
                    this._request = this._request.replaceFirst(dataset, dataset + "[" + (String)linkcolist + "]");
                    continue;
                }
                this._request = this._request + "," + dataset;
            }
            if (this._request != null && this._request.indexOf("[currentuser]") >= 0) {
                this._request = StringUtil.replaceAll(this._request, "[currentuser]", this.requestContext.getProperty("sysuserid"));
            }
            if (this._request != null && this._request.indexOf("[sdcid]") >= 0) {
                this._request = StringUtil.replaceAll(this._request, "[sdcid]", this._sdcid);
            }
            if (this._request != null && (this._request.indexOf("[keycolid1]") >= 0 || this._request.indexOf("[primarytable]") >= 0) && this.pageContext != null) {
                SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
                this._request = StringUtil.replaceAll(this._request, "[keycolid1]", sdcProcessor.getProperty(this._sdcid, "keycolid1"));
                this._request = StringUtil.replaceAll(this._request, "[keycolid2]", sdcProcessor.getProperty(this._sdcid, "keycolid2"));
                this._request = StringUtil.replaceAll(this._request, "[keycolid3]", sdcProcessor.getProperty(this._sdcid, "keycolid3"));
                this._request = StringUtil.replaceAll(this._request, "[primarytable]", sdcProcessor.getProperty(this._sdcid, "tableid"));
            }
            this._propsMatch = JstlUtil.evaluateExpression(this._propsMatch, this.pageContext, "").toString();
            this._lockoption = JstlUtil.evaluateExpression(this._lockoption, this.pageContext, "").toString();
            this._primarylock = JstlUtil.evaluateExpression(this._primarylock, this.pageContext, "").toString();
            this._datalock = JstlUtil.evaluateExpression(this._datalock, this.pageContext, "").toString();
            this._showtemplates = JstlUtil.evaluateExpression(this._showtemplates, this.pageContext, "").toString();
            this._nullvalue = JstlUtil.evaluateExpression(this._nullvalue, this.pageContext, "").toString();
            this._retrieve = Boolean.valueOf(JstlUtil.evaluateExpression(this._retrieveString, this.pageContext, "").toString());
            this._pageid = this.requestContext.getPropertyList("pagedata").getProperty("webpageid");
            this._pageedition = this.requestContext.getProperty("__productedition");
            this._autolocktimeout = Boolean.valueOf(JstlUtil.evaluateExpression(this._autolocktimeoutString, this.pageContext, "").toString());
            this._mergequerywhere = JstlUtil.evaluateExpression(this._mergequerywhere, this.pageContext, "").toString();
            this._versionstatus = JstlUtil.evaluateExpression(this._versionstatus, this.pageContext, "").toString();
            this._retrievelimit = JstlUtil.evaluateExpression(this._retrievelimit, this.pageContext, "").toString();
            this._rsetid = JstlUtil.evaluateExpression(this._rsetid, this.pageContext, "").toString();
        }
    }

    private void getRequestItemCols(PropertyListCollection columns, ArrayList columnlist) {
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                String colid;
                if ("Do Not Retrieve".equals(columns.getPropertyList(i).getProperty("mode")) || columns.getPropertyList(i).getProperty("mode").indexOf("Deferred Display") == 0 || (colid = columns.getPropertyList(i).getProperty("columnid")).length() <= 0 || columnlist.indexOf(colid) >= 0) continue;
                columnlist.add(colid);
            }
        }
    }

    private String getColumnList(ArrayList columnlist) {
        StringBuffer cols = new StringBuffer();
        for (int i = 0; i < columnlist.size(); ++i) {
            if (i == 0) {
                cols.append((String)columnlist.get(i));
                continue;
            }
            cols.append("," + (String)columnlist.get(i));
        }
        return cols.toString();
    }
}

