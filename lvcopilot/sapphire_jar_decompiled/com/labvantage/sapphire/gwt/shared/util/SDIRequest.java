/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared.util;

import com.labvantage.sapphire.gwt.shared.constants.DatasetNameConstants;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class SDIRequest
implements Serializable,
DatasetNameConstants {
    protected String _sdcid;
    protected String _keyid1list = "";
    protected String _keyid2list = "";
    protected String _keyid3list = "";
    protected String _queryid = "";
    protected String[] _queryparams = null;
    protected String _queryfrom = "";
    protected String _querywhere = "";
    protected String _altKeyList = "";
    protected String _altKeyColumnId = "";
    protected String _queryorderby = "";
    protected String _rsetid = "";
    protected String _lockoption = "";
    protected String _primarylockoption = "";
    protected String _primaryfkcolumnlock = "";
    protected String _datalockoption = "";
    protected boolean countrequest = false;
    private boolean _validateCheckout = false;
    private boolean _autolocktimeout = false;
    protected boolean _usersetorderby = false;
    protected boolean _retainrsetid = false;
    protected boolean _retrieve = true;
    protected String _showtemplates = "false";
    protected boolean _propsmatch = false;
    protected String _paramlistidlist = "";
    protected String _paramlistversionidlist = "";
    protected String _variantidlist = "";
    protected String _datasetlist = "";
    protected String _workitemidlist = "";
    protected String _workiteminstancelist = "";
    protected int _retrievelimit = 0;
    protected String _versionstatus = "";
    protected boolean overrideLoadFlag = false;
    protected boolean extendedDataTypes = false;
    protected boolean retrieveMappedKey = true;
    protected boolean extendedAudit = false;
    protected boolean showHiddenRecords = false;
    protected boolean _returnMaskedData = false;
    protected int securityBypassCode = 0;
    protected int queryTimeout = -1;
    protected String _requestid = "";
    protected String _linkid = "";
    protected HashSet _requestitems = new HashSet();
    protected HashSet _sdirequests = new HashSet();

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append(this._sdcid != null && this._sdcid.length() > 0 ? "sdcid=" + this._sdcid : "");
        out.append(this._keyid1list != null && this._keyid1list.length() > 0 ? (out.length() > 0 ? ";" : "") + "keyid1=" + this._keyid1list : "");
        out.append(this._keyid2list != null && this._keyid2list.length() > 0 ? (out.length() > 0 ? ";" : "") + "keyid2=" + this._keyid2list : "");
        out.append(this._keyid3list != null && this._keyid3list.length() > 0 ? (out.length() > 0 ? ";" : "") + "keyid3=" + this._keyid3list : "");
        out.append(this._queryid != null && this._queryid.length() > 0 ? (out.length() > 0 ? ";" : "") + "queryid=" + this._queryid : "");
        out.append(this._queryparams != null && this._queryparams.length > 0 ? (out.length() > 0 ? ";" : "") + "queryparams=" + Arrays.toString(this._queryparams) : "");
        out.append(this._queryfrom != null && this._queryfrom.length() > 0 ? (out.length() > 0 ? ";" : "") + "queryfrom=" + this._queryfrom : "");
        out.append(this._querywhere != null && this._querywhere.length() > 0 ? (out.length() > 0 ? ";" : "") + "querywhere=" + this._querywhere : "");
        out.append(this._rsetid != null && this._rsetid.length() > 0 ? (out.length() > 0 ? ";" : "") + "rsetid=" + this._rsetid : "");
        out.append(this.countrequest ? (out.length() > 0 ? ";" : "") + "countrequest" : "");
        return out.toString();
    }

    public void setOverrideLoadFlag(boolean overrideLoadFlag) {
        this.overrideLoadFlag = overrideLoadFlag;
    }

    public boolean isOverrideLoadFlag() {
        return this.overrideLoadFlag;
    }

    public void setRetrieveMappedKey(boolean retrieveMappedKey) {
        this.retrieveMappedKey = retrieveMappedKey;
    }

    public boolean isRetrieveMappedKey() {
        return this.retrieveMappedKey;
    }

    public void setSDIList(String sdcid, String keyid1list, String keyid2list, String keyid3list) {
        this._sdcid = sdcid;
        this._keyid1list = keyid1list;
        this._keyid2list = keyid2list;
        this._keyid3list = keyid3list;
    }

    public void setRequestid(String requestid) {
        this._requestid = requestid;
    }

    public String getRequestid() {
        return this._requestid;
    }

    public void setSDCid(String sdcid) {
        this._sdcid = sdcid;
    }

    public String getSDCid() {
        return this._sdcid;
    }

    public void setKeyid1List(String keyid1list) {
        this._keyid1list = keyid1list;
    }

    public String getKeyid1List() {
        return this._keyid1list;
    }

    public void setKeyid2List(String keyid2list) {
        this._keyid2list = keyid2list;
    }

    public String getKeyid2List() {
        return this._keyid2list;
    }

    public void setKeyid3List(String keyid3list) {
        this._keyid3list = keyid3list;
    }

    public String getKeyid3List() {
        return this._keyid3list;
    }

    public void setQueryid(String queryid) {
        this._queryid = queryid;
    }

    public String getQueryid() {
        return this._queryid;
    }

    public void setQueryParams(String[] queryparams) {
        this._queryparams = queryparams;
    }

    public String[] getQueryParams() {
        return this._queryparams;
    }

    public void setQueryFrom(String queryfrom) {
        this._queryfrom = queryfrom;
    }

    public String getQueryFrom() {
        return this._queryfrom;
    }

    public void setQueryWhere(String querywhere) {
        this._querywhere = querywhere;
    }

    public void setAltKeyList(String altKeyList) {
        this._altKeyList = altKeyList;
    }

    public void setAltKeyColumnId(String altKeyColumnId) {
        this._altKeyColumnId = altKeyColumnId;
    }

    public String getAltKeyList() {
        return this._altKeyList;
    }

    public String getAltKeyIdCol() {
        return this._altKeyColumnId;
    }

    public String getQueryWhere() {
        return this._querywhere;
    }

    public void setQueryOrderBy(String queryorderby) {
        this._queryorderby = queryorderby;
    }

    public String getQueryOrderBy() {
        return this._queryorderby;
    }

    public void setRequestItem(String requestitem) {
        this._requestitems.add(requestitem);
    }

    public String[] getRequestItems() {
        return this._requestitems.toArray(new String[0]);
    }

    public void setSDIRequest(SDIRequest sdiRequest) {
        this._sdirequests.add(sdiRequest);
    }

    public void setRsetid(String rsetid) {
        this._rsetid = rsetid;
    }

    public String getRsetid() {
        return this._rsetid;
    }

    public boolean getValidateCheckout() {
        return this._validateCheckout;
    }

    public void setValidateCheckout(boolean validateCheckout) {
        this._validateCheckout = validateCheckout;
    }

    public void setLockOption(String lockoption) {
        this._lockoption = lockoption;
    }

    public String getLockOption() {
        return this._lockoption;
    }

    public void setPrimaryLockOption(String lockoption) {
        this._primarylockoption = lockoption;
    }

    public String getPrimaryLockOption() {
        return this._primarylockoption;
    }

    public void setPrimaryFKColumnLock(String primaryfkcolumnlock) {
        this._primaryfkcolumnlock = primaryfkcolumnlock;
    }

    public String getPrimaryFKColumnLock() {
        return this._primaryfkcolumnlock;
    }

    public void setDataLockOption(String lockoption) {
        this._datalockoption = lockoption;
    }

    public void setAutoLockTimeout(boolean autolocktimeout) {
        this._autolocktimeout = autolocktimeout;
    }

    public boolean getAutoLockTimeout() {
        return this._autolocktimeout;
    }

    public boolean isUseRSetOrderBy() {
        return this._usersetorderby;
    }

    public void setUseRSetOrderBy(boolean usersetorderby) {
        this._usersetorderby = usersetorderby;
    }

    public String getDataLockOption() {
        return this._datalockoption;
    }

    public boolean isLockRequest() {
        if (this.getPrimaryLockOption() != null && this.getPrimaryLockOption().length() > 0 || this.getLockOption() != null && this.getLockOption().length() > 0) {
            return true;
        }
        return this.getDataLockOption() != null && this.getDataLockOption().length() > 0 || this.getLockOption() != null && this.getLockOption().length() > 0;
    }

    public void setRetainRsetid(boolean retainrsetid) {
        this._retainrsetid = retainrsetid;
    }

    public boolean getRetainRsetid() {
        return this._retainrsetid;
    }

    public void setRetrieve(boolean retrieve) {
        this._retrieve = retrieve;
    }

    public boolean getRetrieve() {
        return this._retrieve;
    }

    public void setShowTemplates(boolean showtemplates) {
        this._showtemplates = showtemplates ? "true" : "false";
    }

    public void setShowTemplates(String showtemplates) {
        this._showtemplates = showtemplates;
    }

    public boolean getShowTemplates() {
        return this._showtemplates.equals("true");
    }

    public boolean getShowTemplatesOnly() {
        return this._showtemplates.equals("only");
    }

    public void setParamlistidList(String paramlistidlist) {
        this._paramlistidlist = paramlistidlist;
    }

    public String getParamlistidList() {
        return this._paramlistidlist;
    }

    public void setParamlistversionidList(String paramlistversionidlist) {
        this._paramlistversionidlist = paramlistversionidlist;
    }

    public String getParamlistversionidList() {
        return this._paramlistversionidlist;
    }

    public void setVariantidList(String variantidlist) {
        this._variantidlist = variantidlist;
    }

    public String getVariantidList() {
        return this._variantidlist;
    }

    public void setDatasetList(String datasetlist) {
        this._datasetlist = datasetlist;
    }

    public String getDatasetList() {
        return this._datasetlist;
    }

    public void setWorkitemidList(String workitemidlist) {
        this._workitemidlist = workitemidlist;
    }

    public String getWorkitemidList() {
        return this._workitemidlist;
    }

    public void setWorkiteminstanceList(String workiteminstancelist) {
        this._workiteminstancelist = workiteminstancelist;
    }

    public String getWorkiteminstanceList() {
        return this._workiteminstancelist;
    }

    public void setRetrieveLimit(int retrievelimit) {
        this._retrievelimit = retrievelimit;
    }

    public String getVersionStatus() {
        return this._versionstatus;
    }

    public void setVersionStatus(String versionstatus) {
        this._versionstatus = versionstatus;
    }

    public int getRetrieveLimit() {
        return this._retrievelimit;
    }

    public void setPropsMatch(boolean propsmatch) {
        this._propsmatch = propsmatch;
    }

    public boolean getPropsMatch() {
        return this._propsmatch;
    }

    public boolean isExtendedDataTypes() {
        return this.extendedDataTypes;
    }

    public void setExtendedDataTypes(boolean extendedDataTypes) {
        this.extendedDataTypes = extendedDataTypes;
    }

    public boolean isExtendedAudit() {
        return this.extendedAudit;
    }

    public void setExtendedAudit(boolean extendedAudit) {
        this.extendedAudit = extendedAudit;
    }

    public int getQueryTimeout() {
        return this.queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setCountRequest(boolean countrequest) {
        this._requestitems.clear();
        this.setRequestItem("primary");
        this.countrequest = countrequest;
    }

    public boolean isCountRequest() {
        return this.countrequest;
    }

    public String getRequest(String requestitem) {
        String request = "";
        String[] requestitems = this.getRequestItems();
        if (requestitems != null) {
            for (int i = 0; i < requestitems.length && request.length() == 0; ++i) {
                if (!requestitems[i].equalsIgnoreCase(requestitem) && !requestitems[i].equalsIgnoreCase(requestitem + "+") && !requestitems[i].startsWith(requestitem + "[") && !requestitems[i].equalsIgnoreCase("all")) continue;
                request = requestitems[i];
            }
        }
        return request;
    }

    public boolean isRequestItem(String requestitem) {
        boolean isrequestitem = false;
        String[] requestitems = this.getRequestItems();
        if (requestitems != null) {
            for (int i = 0; i < requestitems.length && !isrequestitem; ++i) {
                if (!requestitems[i].equalsIgnoreCase(requestitem) && !requestitems[i].equalsIgnoreCase(requestitem + "+") && !requestitems[i].startsWith(requestitem + "[") && !requestitems[i].equalsIgnoreCase("all")) continue;
                isrequestitem = true;
            }
        }
        return isrequestitem;
    }

    public boolean containsDataRequest() {
        boolean containsdatarequest = false;
        String[] requestitems = this.getRequestItems();
        if (requestitems != null) {
            for (int i = 0; i < requestitems.length && !containsdatarequest; ++i) {
                if (!requestitems[i].equalsIgnoreCase("dataset") && !requestitems[i].startsWith("dataset[") && !requestitems[i].startsWith("dataset [") && !requestitems[i].equalsIgnoreCase("dataitem") && !requestitems[i].startsWith("dataitem[") && !requestitems[i].startsWith("dataitem [") && !requestitems[i].equalsIgnoreCase("datalimit") && !requestitems[i].startsWith("datalimit[") && !requestitems[i].startsWith("datalimit [") && !requestitems[i].equalsIgnoreCase("dataapproval") && !requestitems[i].startsWith("dataapproval[") && !requestitems[i].startsWith("dataapproval [") && !requestitems[i].equalsIgnoreCase("dataspec") && !requestitems[i].startsWith("dataspec[") && !requestitems[i].startsWith("dataspec [") && !requestitems[i].equalsIgnoreCase("datarelation") && !requestitems[i].startsWith("datarelation[") && !requestitems[i].startsWith("datarelation [") && !requestitems[i].equalsIgnoreCase("reagentrelation") && !requestitems[i].startsWith("reagentrelation[") && !requestitems[i].startsWith("reagentrelation [")) continue;
                containsdatarequest = true;
            }
        }
        return containsdatarequest;
    }

    public boolean containsNonDataRequest() {
        boolean containsnondatarequest = false;
        String[] requestitems = this.getRequestItems();
        if (requestitems != null) {
            for (int i = 0; i < requestitems.length && !containsnondatarequest; ++i) {
                if (requestitems[i].equalsIgnoreCase("dataset") || requestitems[i].startsWith("dataset[") || requestitems[i].startsWith("dataset [") || requestitems[i].equalsIgnoreCase("dataitem") || requestitems[i].startsWith("dataitem[") || requestitems[i].startsWith("dataitem [") || requestitems[i].equalsIgnoreCase("datalimit") || requestitems[i].startsWith("datalimit[") || requestitems[i].startsWith("datalimit [") || requestitems[i].equalsIgnoreCase("dataapproval") || requestitems[i].startsWith("dataapproval[") || requestitems[i].startsWith("dataapproval [") || requestitems[i].equalsIgnoreCase("dataspec") || requestitems[i].startsWith("dataspec[") || requestitems[i].startsWith("dataspec [")) continue;
                containsnondatarequest = true;
            }
        }
        return containsnondatarequest;
    }

    public boolean isQueryRequest() {
        return this.getQueryid() != null && this.getQueryid().length() > 0 || this.getQueryFrom() != null && this.getQueryFrom().length() > 0;
    }

    public boolean isShowHiddenRecords() {
        return this.showHiddenRecords;
    }

    public void setShowHiddenRecords(boolean showHiddenRecords) {
        this.showHiddenRecords = showHiddenRecords;
    }

    public int getSecurityBypassCode() {
        return this.securityBypassCode;
    }

    public void setSecurityBypassCode(int securityBypassCode) {
        this.securityBypassCode = securityBypassCode;
    }

    public boolean isReturnMaskedData() {
        return this._returnMaskedData;
    }

    public void setReturnMaskedData(boolean returnMaskedData) {
        this._returnMaskedData = returnMaskedData;
    }

    public String getLinkId() {
        return this._linkid;
    }

    public void setLinkId(String linkId) {
        this._linkid = linkId;
    }
}

