/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import java.io.Serializable;
import sapphire.util.SDIRequest;

public class SDIRequestTransportBean
implements Serializable {
    private String sdcid = "";
    private String keyid1list = "";
    private String keyid2list = "";
    private String keyid3list = "";
    private String queryid = "";
    private String[] queryparams = new String[0];
    private String queryfrom = "";
    private String querywhere = "";
    private String queryorderby = "";
    private String rsetid = "";
    private String lockoption = "";
    private String primarylockoption = "";
    private String datalockoption = "";
    private boolean retainrsetid = false;
    private boolean retrieve = true;
    private boolean showtemplates = false;
    private boolean propsmatch = false;
    private String paramlistidlist = "";
    private String paramlistversionidlist = "";
    private String variantidlist = "";
    private String datasetlist = "";
    private int retrievelimit = 0;
    private String versionstatus = "";
    private boolean overrideLoadFlag = false;
    private boolean extendedDataTypes = false;
    private boolean retrieveMappedKey = true;
    private boolean extendedAudit = false;
    private String[] sdirequestitems = new String[0];

    public SDIRequestTransportBean() {
        this.queryparams = new String[0];
        this.sdirequestitems = new String[1];
        this.sdirequestitems[0] = "primary[]";
    }

    public SDIRequest toSDIRequest() {
        return this.getSDIRequest();
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getKeyid1list() {
        return this.keyid1list;
    }

    public void setKeyid1list(String keyid1list) {
        this.keyid1list = keyid1list;
    }

    public String getKeyid2list() {
        return this.keyid2list;
    }

    public void setKeyid2list(String keyid2list) {
        this.keyid2list = keyid2list;
    }

    public String getKeyid3list() {
        return this.keyid3list;
    }

    public void setKeyid3list(String keyid3list) {
        this.keyid3list = keyid3list;
    }

    public String getQueryid() {
        return this.queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public String[] getQueryparams() {
        return this.queryparams;
    }

    public void setQueryparams(String[] queryparams) {
        this.queryparams = queryparams;
    }

    public String getQueryfrom() {
        return this.queryfrom;
    }

    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    public String getQuerywhere() {
        return this.querywhere;
    }

    public void setQuerywhere(String querywhere) {
        this.querywhere = querywhere;
    }

    public String getQueryorderby() {
        return this.queryorderby;
    }

    public void setQueryorderby(String queryorderby) {
        this.queryorderby = queryorderby;
    }

    public String getRsetid() {
        return this.rsetid;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public String getLockoption() {
        return this.lockoption;
    }

    public void setLockoption(String lockoption) {
        this.lockoption = lockoption;
    }

    public String getPrimarylockoption() {
        return this.primarylockoption;
    }

    public void setPrimarylockoption(String primarylockoption) {
        this.primarylockoption = primarylockoption;
    }

    public String getDatalockoption() {
        return this.datalockoption;
    }

    public void setDatalockoption(String datalockoption) {
        this.datalockoption = datalockoption;
    }

    public boolean getRetainrsetid() {
        return this.retainrsetid;
    }

    public void setRetainrsetid(boolean retainrsetid) {
        this.retainrsetid = retainrsetid;
    }

    public boolean getRetrieve() {
        return this.retrieve;
    }

    public void setRetrieve(boolean retrieve) {
        this.retrieve = retrieve;
    }

    public boolean getShowtemplates() {
        return this.showtemplates;
    }

    public void setShowtemplates(boolean showtemplates) {
        this.showtemplates = showtemplates;
    }

    public boolean getPropsmatch() {
        return this.propsmatch;
    }

    public void setPropsmatch(boolean propsmatch) {
        this.propsmatch = propsmatch;
    }

    public String getParamlistidlist() {
        return this.paramlistidlist;
    }

    public void setParamlistidlist(String paramlistidlist) {
        this.paramlistidlist = paramlistidlist;
    }

    public String getParamlistversionidlist() {
        return this.paramlistversionidlist;
    }

    public void setParamlistversionidlist(String paramlistversionidlist) {
        this.paramlistversionidlist = paramlistversionidlist;
    }

    public String getVariantidlist() {
        return this.variantidlist;
    }

    public void setVariantidlist(String variantidlist) {
        this.variantidlist = variantidlist;
    }

    public String getDatasetlist() {
        return this.datasetlist;
    }

    public void setDatasetlist(String datasetlist) {
        this.datasetlist = datasetlist;
    }

    public int getRetrievelimit() {
        return this.retrievelimit;
    }

    public void setRetrievelimit(int retrievelimit) {
        this.retrievelimit = retrievelimit;
    }

    public String getVersionstatus() {
        return this.versionstatus;
    }

    public void setVersionstatus(String versionstatus) {
        this.versionstatus = versionstatus;
    }

    public boolean getOverrideLoadFlag() {
        return this.overrideLoadFlag;
    }

    public void setOverrideLoadFlag(boolean overrideLoadFlag) {
        this.overrideLoadFlag = overrideLoadFlag;
    }

    public boolean getExtendedDataTypes() {
        return this.extendedDataTypes;
    }

    public void setExtendedDataTypes(boolean extendedDataTypes) {
        this.extendedDataTypes = extendedDataTypes;
    }

    public boolean getRetrieveMappedKey() {
        return this.retrieveMappedKey;
    }

    public void setRetrieveMappedKey(boolean retrieveMappedKey) {
        this.retrieveMappedKey = retrieveMappedKey;
    }

    public boolean getExtendedAudit() {
        return this.extendedAudit;
    }

    public void setExtendedAudit(boolean extendedAudit) {
        this.extendedAudit = extendedAudit;
    }

    public String[] getSdirequestitems() {
        return this.sdirequestitems;
    }

    public void setSdirequestitems(String[] sdirequestitems) {
        this.sdirequestitems = sdirequestitems;
    }

    protected SDIRequest getSDIRequest() {
        SDIRequest sdireq = new SDIRequest();
        sdireq.setDataLockOption(this.getDatalockoption());
        sdireq.setDatasetList(this.getDatasetlist());
        sdireq.setExtendedAudit(this.getExtendedAudit());
        sdireq.setExtendedDataTypes(this.getExtendedDataTypes());
        sdireq.setKeyid1List(this.getKeyid1list());
        sdireq.setKeyid2List(this.getKeyid2list());
        sdireq.setKeyid3List(this.getKeyid3list());
        sdireq.setLockOption(this.getLockoption());
        sdireq.setOverrideLoadFlag(this.getOverrideLoadFlag());
        sdireq.setParamlistidList(this.getParamlistidlist());
        sdireq.setParamlistversionidList(this.getParamlistversionidlist());
        sdireq.setPrimaryLockOption(this.getPrimarylockoption());
        sdireq.setPropsMatch(this.getPropsmatch());
        sdireq.setQueryFrom(this.getQueryfrom());
        sdireq.setQueryid(this.getQueryid());
        sdireq.setQueryOrderBy(this.getQueryorderby());
        sdireq.setQueryParams(this.getQueryparams());
        sdireq.setQueryWhere(this.getQuerywhere());
        for (int i = 0; i < this.sdirequestitems.length; ++i) {
            sdireq.setRequestItem(this.sdirequestitems[i]);
        }
        sdireq.setRetrieve(this.getRetrieve());
        sdireq.setRetrieveLimit(this.getRetrievelimit());
        sdireq.setRetrieveMappedKey(this.getRetrieveMappedKey());
        sdireq.setRsetid(this.getRsetid());
        sdireq.setSDCid(this.getSdcid());
        sdireq.setShowTemplates(this.getShowtemplates());
        sdireq.setVariantidList(this.getVariantidlist());
        return sdireq;
    }
}

