/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.AttributeTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.DataEntryTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.FieldTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.RootSDCTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.SDCTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.WorksheetItemFieldTreeRenderer;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdhocQueryObjectTreeRenderer {
    private String rootsdcid = null;
    private String sdcid = null;
    private String tableid = null;
    private SDCProcessor sdcProcessor = null;
    private QueryProcessor queryProcessor = null;
    private TranslationProcessor tp = null;
    private HttpServletRequest request = null;
    private boolean isRoot = true;
    private String linkcolumnid = "";
    private String detailname = "";
    private RequestContext requestContext;
    private PropertyList pagedata = null;
    private AdhocMetaData adhocmetadata = null;
    private String filtertext = "";

    public AdhocQueryObjectTreeRenderer(PageContext pageContext) throws SapphireException {
        this.queryProcessor = new QueryProcessor(pageContext);
        this.sdcProcessor = new SDCProcessor(pageContext);
        this.tp = new TranslationProcessor(pageContext);
        ServletRequest request = pageContext.getRequest();
        this.sdcid = request.getParameter("sdcid") != null ? request.getParameter("sdcid") : "";
        this.requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        this.pagedata = this.requestContext.getPropertyList().getPropertyList("pagedata");
        pageContext.getSession().setAttribute("adhocbrowser_" + this.pagedata.getProperty("page"), (Object)this.requestContext.getPropertyList().getPropertyList("pagedata"));
        this.request = (HttpServletRequest)pageContext.getRequest();
        pageContext.getSession().setAttribute("Adhocquery_pagedata", (Object)this.pagedata);
        this.adhocmetadata = AdhocMetaData.getInstance(pageContext);
        if (this.sdcid.length() == 0) {
            PropertyListCollection searchablesdcs = this.pagedata.getCollection("searchablesdcs");
            this.sdcid = this.pagedata != null && searchablesdcs != null && searchablesdcs.size() > 0 ? searchablesdcs.getPropertyList(0).getProperty("sdcid") : AdhocQueryPageUtil.getDefaultSdcId(request, this.queryProcessor);
        }
        this.tableid = this.adhocmetadata.getTableid(this.sdcid);
    }

    public AdhocQueryObjectTreeRenderer(HttpServletRequest request) throws SapphireException {
        this.isRoot = false;
        this.request = request;
        this.sdcid = AdhocQueryPageUtil.getDefaultSdcId((ServletRequest)request, this.queryProcessor);
        this.requestContext = (RequestContext)request.getAttribute("RequestContext");
        this.sdcProcessor = new SDCProcessor(this.requestContext.getConnectionId());
        this.queryProcessor = new QueryProcessor(this.requestContext.getConnectionId());
        this.tp = new TranslationProcessor(this.requestContext.getConnectionId());
        String pageid = request.getParameter("adhocquerypageid");
        if (pageid != null && pageid.length() > 0) {
            this.pagedata = (PropertyList)request.getSession().getAttribute("adhocbrowser_" + pageid);
        }
        this.adhocmetadata = AdhocMetaData.getInstance(request);
        this.filtertext = request.getParameter("filtertext");
    }

    public PropertyList getPropertyList() throws SapphireException {
        TreeRenderer treeRenderer = null;
        if (this.isRoot) {
            boolean showRootDropDown = true;
            treeRenderer = new RootSDCTreeRenderer(showRootDropDown, this.sdcid, this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
        } else {
            this.linkcolumnid = this.request.getParameter("linkcolumnid");
            if ("dataentryroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".dataentryroot") > 0 || this.linkcolumnid.indexOf("sdidata[") >= 0) {
                boolean isDetailDataEntry = false;
                String softlinktable = "";
                if (this.linkcolumnid.indexOf(".sdidata[") > 0 || this.linkcolumnid.indexOf(".dataentryroot") > 0) {
                    String reftableid = null;
                    try {
                        reftableid = AdhocMetaData.getReferenceEntityName(this.requestContext.getConnectionId(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid.substring(0, this.linkcolumnid.lastIndexOf(".")));
                        this.sdcid = this.adhocmetadata.getSdcId(reftableid);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (reftableid == null || reftableid.length() == 0) {
                        this.detailname = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf("."));
                        reftableid = this.adhocmetadata.getReverseFKTableId(this.detailname);
                    }
                    this.sdcid = this.adhocmetadata.getSdcId(reftableid);
                    if (this.sdcid == null) {
                        this.sdcid = reftableid.substring(reftableid.lastIndexOf("_") + 1);
                        softlinktable = reftableid.substring(0, reftableid.indexOf(this.sdcid) - 1);
                        isDetailDataEntry = this.linkcolumnid.indexOf(".") > 0;
                    } else {
                        isDetailDataEntry = this.linkcolumnid.indexOf(reftableid + "_") == 0;
                    }
                }
                treeRenderer = this.getDataEntryTreeRenderer(softlinktable);
                if (isDetailDataEntry) {
                    ((DataEntryTreeRenderer)treeRenderer).setIsCriteriaOnly(true);
                }
            } else if ("fieldroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".fieldroot") > 0 || this.linkcolumnid.indexOf("fieldcategory[") > 0) {
                String reftableid = null;
                try {
                    if (this.linkcolumnid.indexOf(".fieldroot") > 0) {
                        reftableid = AdhocMetaData.getReferenceEntityName(this.requestContext.getConnectionId(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid.substring(0, this.linkcolumnid.lastIndexOf(".fieldroot")));
                        this.sdcid = this.adhocmetadata.getSdcId(reftableid);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                treeRenderer = this.getFieldTreeRenderer();
            } else if ("worksheetitemfieldroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".worksheetitemfieldroot") > 0) {
                String reftableid = null;
                try {
                    if (this.linkcolumnid.indexOf(".worksheetitemfieldroot") > 0) {
                        reftableid = AdhocMetaData.getReferenceEntityName(this.requestContext.getConnectionId(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid.substring(0, this.linkcolumnid.lastIndexOf(".fieldroot")));
                        this.sdcid = this.adhocmetadata.getSdcId(reftableid);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                treeRenderer = this.getWorkSheetItemFieldTreeRenderer();
            } else if ("attributeroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".attributeroot") > 0 || this.linkcolumnid.indexOf("attributegroup[") > 0) {
                String reftableid = null;
                try {
                    if (this.linkcolumnid.indexOf(".attributeroot") > 0) {
                        reftableid = AdhocMetaData.getReferenceEntityName(this.requestContext.getConnectionId(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid.substring(0, this.linkcolumnid.lastIndexOf(".fieldroot")));
                        this.sdcid = this.adhocmetadata.getSdcId(reftableid);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                treeRenderer = this.getAttributeTreeRenderer();
            } else if (this.linkcolumnid != null && this.linkcolumnid.length() > 0) {
                try {
                    this.tableid = AdhocMetaData.getReferenceEntityName(this.requestContext.getConnectionId(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (this.tableid != null && this.tableid.length() > 0) {
                    this.rootsdcid = "" + this.sdcid;
                    this.sdcid = this.adhocmetadata.getSdcId(this.tableid);
                    if (this.sdcid != null) {
                        treeRenderer = new SDCTreeRenderer(this.sdcid, this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
                    } else {
                        this.detailname = this.tableid;
                        boolean isDetail_SDC = true;
                        this.sdcid = isDetail_SDC ? this.detailname.substring(this.detailname.lastIndexOf("_") + 1) : null;
                        treeRenderer = new DetailTreeRenderer(this.sdcid, this.detailname, this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
                    }
                } else {
                    this.detailname = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf("."));
                    this.tableid = this.adhocmetadata.getReverseFKTableId(this.detailname);
                    this.rootsdcid = "" + this.sdcid;
                    this.sdcid = this.adhocmetadata.getSdcId(this.tableid);
                    this.tableid = this.detailname;
                    treeRenderer = new DetailTreeRenderer(this.sdcid, this.detailname, this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
                }
            }
        }
        treeRenderer.setRootsdcid(this.rootsdcid);
        treeRenderer.setLinkcolumnid(this.linkcolumnid);
        return ((TreeRenderer)treeRenderer).getNodePropertyList();
    }

    private TreeRenderer getDataEntryTreeRenderer(String softlinktable) {
        DataEntryTreeRenderer treeRenderer = new DataEntryTreeRenderer(this.sdcid, "DataEntry", this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
        treeRenderer.init(this.sdcid, this.filtertext, softlinktable);
        return treeRenderer;
    }

    private TreeRenderer getFieldTreeRenderer() {
        FieldTreeRenderer treeRenderer = new FieldTreeRenderer(this.sdcid, "Field", this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
        treeRenderer.init(this.filtertext);
        return treeRenderer;
    }

    private TreeRenderer getWorkSheetItemFieldTreeRenderer() {
        WorksheetItemFieldTreeRenderer treeRenderer = new WorksheetItemFieldTreeRenderer(this.sdcid, "Field", this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
        treeRenderer.init(this.filtertext);
        return treeRenderer;
    }

    private TreeRenderer getAttributeTreeRenderer() {
        AttributeTreeRenderer treeRenderer = new AttributeTreeRenderer(this.sdcid, "Attribute", this.pagedata, this.adhocmetadata, this.queryProcessor, this.sdcProcessor, this.tp);
        treeRenderer.init(this.filtertext);
        return treeRenderer;
    }
}

