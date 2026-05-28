/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class RenderChildStorage
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuilder sb = new StringBuilder();
        int level = Integer.parseInt(ajaxResponse.getRequestParameter("level", "0")) + 1;
        PropertyList uiprops = new PropertyList();
        uiprops.setProperty("selectortype", ajaxResponse.getRequestParameter("selectortype", "radio"));
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        String searchmode = ajaxResponse.getRequestParameter("searchmode");
        PropertyList storageProperty = StorageUnitSDC.getSUValueTree(this.getQueryProcessor(), storageunitid);
        boolean islive = "Live".equals(storageProperty.getPropertyListNotNull("explorerprops").getProperty("showstats", "Cached"));
        long start = System.currentTimeMillis();
        if (islive) {
            boolean updateStorageUnitStats = false;
            try {
                PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                updateStorageUnitStats = !"Manual Only".equals(policy.getPropertyListNotNull("storageexplorer").getProperty("refreshstatistics"));
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            if (updateStorageUnitStats) {
                if (this.getConnectionProcessor().isOra()) {
                    this.getQueryProcessor().execSQL("call LV_SUS.RefreshBranch('" + storageunitid + "', LV_SUS.FindTopLevel('" + storageunitid + "'))");
                } else {
                    this.getQueryProcessor().execSQL("DECLARE @topsu NVARCHAR(40); SET @topsu = dbo.LV_SUS_FindTopLevel ('" + storageunitid + "'); EXEC LV_SUS_RefreshBranch '" + storageunitid + "',@topsu;");
                }
                this.logger.debug("Refreshing branch for storageunit " + storageunitid + " took " + (System.currentTimeMillis() - start) + " ms.");
            }
        }
        String parentStorageUnitType = "";
        String parentPropertyTree = "";
        DataSet parentLayout = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, storageunittype, propertytreeid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (parentLayout != null && parentLayout.size() > 0) {
            parentStorageUnitType = parentLayout.getString(0, "storageunittype");
            parentPropertyTree = parentLayout.getString(0, "propertytreeid");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select s.storageunitid, s.storageunittype, s.linksdcid, s.linkkeyid1, s.storageunitlabel, s.propertytreeid,");
        sql.append(" s.lastnodeflag, s.maxtiallowed,");
        sql.append(" sus.enteredspecimencapacity, sus.filledlastnodecount, sus.lastnodecapacity, sus.lastnodecount, sus.specimencapacity,");
        sql.append(" (sus.specimencount + coalesce(sus.extraspecimentotal, 0)) specimencount");
        sql.append(" from storageunit s left outer join storageunitstats sus");
        if (this.getConnectionProcessor().isOra()) {
            sql.append(" on sus.storageunitid = s.storageunitid and sus.parentid = s.parentid and sus.toplevelid = LV_SUS.FindTopLevel(s.storageunitid)");
        } else {
            sql.append(" on sus.storageunitid = s.storageunitid and sus.parentid = s.parentid and sus.toplevelid = dbo.LV_SUS_FindTopLevel(s.storageunitid)");
        }
        sql.append(" where s.parentid = ?");
        sql.append(" order by s.storageunitindex");
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid}, true);
        if (dataSet != null && dataSet.size() > 0) {
            String orientation = ajaxResponse.getRequestParameter("orientation", "");
            if (OpalUtil.isEmpty(orientation)) {
                orientation = level % 2 == 0 ? "Horizontal" : "Vertical";
            }
            boolean horizontal = "Horizontal".equals(orientation);
            if ("Grid".equals(parentPropertyTree)) {
                int columns;
                int rows;
                PropertyList list = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), parentStorageUnitType);
                try {
                    rows = Integer.parseInt(list.getProperty("rows", "0"));
                }
                catch (NumberFormatException e) {
                    rows = 0;
                }
                try {
                    columns = Integer.parseInt(list.getProperty("columns", "0"));
                }
                catch (NumberFormatException e) {
                    columns = 0;
                }
                if (rows > 0 && columns > 0) {
                    int index = 0;
                    for (int row = 0; row < rows; ++row) {
                        sb.append("<div style='display:flex;flex-wrap:wrap;' class='suitem-container'>");
                        for (int col = 0; col < columns; ++col) {
                            if (dataSet.size() > index) {
                                sb.append("<div style='display:flex;flex-direction:row;flex-wrap:wrap;'>");
                                String storageunittype = dataSet.getString(index, "storageunittype");
                                PropertyList storageUnitProps = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
                                PropertyList explorerprops = storageUnitProps.getPropertyListNotNull("explorerprops");
                                explorerprops.setProperty("lastnodeflag", dataSet.getString(index, "lastnodeflag", "N"));
                                uiprops.setProperty("explorerprops", explorerprops);
                                uiprops.setProperty("orientation", storageUnitProps.getProperty("orientation", ""));
                                sb.append(this.renderSUItem(dataSet, index, uiprops, level));
                                sb.append("</div>");
                            }
                            ++index;
                        }
                        sb.append("</div>");
                    }
                } else {
                    sb.append("<div style='color:red'>");
                    sb.append(this.getTranslationProcessor().translate("Invalid storage unit type property."));
                    sb.append("<br>");
                    sb.append(this.getTranslationProcessor().translate("Both Rows and Columns must have positive integer values for storage unit type: " + parentStorageUnitType));
                    sb.append("</div>");
                }
            } else {
                sb.append("<div style='display:flex;flex-direction:").append(horizontal ? "row" : "column").append(";flex-wrap:wrap;' class='suitem-container'>");
                DataSet trackitemds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linksdcid, linkkeyid1 from trackitem where currentstorageunitid = ? and not exists (select 1 from storageunit where storageunit.linksdcid = trackitem.linksdcid and storageunit.linkkeyid1 = trackitem.linkkeyid1)", (Object[])new String[]{storageunitid});
                if (trackitemds != null && trackitemds.size() > 0) {
                    String linksdcid = trackitemds.getString(0, "linksdcid", "");
                    for (int i = 1; i < trackitemds.size(); ++i) {
                        if (linksdcid.equals(trackitemds.getString(i, "linksdcid", ""))) continue;
                        linksdcid = "";
                        break;
                    }
                    String sdcdisplayname = "trackitem(s)";
                    if (linksdcid.length() > 0) {
                        sdcdisplayname = this.getSDCProcessor().getProperty(linksdcid, trackitemds.size() > 1 ? "plural" : "singular", linksdcid);
                    }
                    sb.append("<div class='suitem' style='border:0;'>");
                    sb.append("<div class='suitem-title' style='display:flex;margin-top:2px;'>");
                    sb.append("<div style='font-style:italic;'>");
                    sb.append("<a title='View Trackitems' href='#ViewTrackitems' onclick=\"javascript:lvShowTrackItemsOnStorage('").append(storageunitid).append("');\">");
                    sb.append(trackitemds.size()).append(" ").append(sdcdisplayname);
                    sb.append("</a></div>");
                    sb.append("</div></div>");
                }
                for (int i = 0; i < dataSet.size(); ++i) {
                    String storageunittype = dataSet.getString(i, "storageunittype");
                    PropertyList storageUnitProps = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
                    PropertyList explorerprops = storageUnitProps.getPropertyListNotNull("explorerprops");
                    explorerprops.setProperty("lastnodeflag", dataSet.getString(i, "lastnodeflag", "N"));
                    uiprops.setProperty("explorerprops", explorerprops);
                    uiprops.setProperty("orientation", storageUnitProps.getProperty("orientation", ""));
                    sb.append(this.renderSUItem(dataSet, i, uiprops, level));
                }
                sb.append("</div>");
            }
        } else {
            sb.append("<div style='display:flex;flex-direction:column;flex-wrap:wrap;'>");
            DataSet trackitemds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linksdcid, linkkeyid1 from trackitem where currentstorageunitid = ? and not exists (select 1 from storageunit where storageunit.linksdcid = trackitem.linksdcid and storageunit.linkkeyid1 = trackitem.linkkeyid1)", (Object[])new String[]{storageunitid});
            if (trackitemds != null && trackitemds.size() > 0) {
                String linksdcid = trackitemds.getString(0, "linksdcid", "");
                for (int i = 1; i < trackitemds.size(); ++i) {
                    if (linksdcid.equals(trackitemds.getString(i, "linksdcid", ""))) continue;
                    linksdcid = "";
                    break;
                }
                String sdcdisplayname = "trackitem(s)";
                if (linksdcid.length() > 0) {
                    sdcdisplayname = this.getSDCProcessor().getProperty(linksdcid, trackitemds.size() > 1 ? "plural" : "singular", linksdcid);
                }
                sb.append("<div class='suitem' style='border:0;'>");
                sb.append("<div class='suitem-title' style='display:flex;margin-top:2px;padding:10px;border:1px solid lightgrey;'>");
                sb.append("<div style='font-style:italic;'>");
                sb.append("<a title='View Trackitems' href='#ViewTrackitems' onclick=\"javascript:lvShowTrackItemsOnStorage('").append(storageunitid).append("');\">");
                sb.append(trackitemds.size()).append(" ").append(sdcdisplayname);
                sb.append("</a></div>");
                sb.append("</div></div>");
            }
            sb.append("</div>");
        }
        this.logger.debug("Rendering child for storageunit " + storageunitid + " took " + (System.currentTimeMillis() - start) + " ms.");
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.addCallbackArgument("level", level);
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }

    private String renderSUItem(DataSet dataSet, int row, PropertyList props, int level) {
        StringBuilder sb = new StringBuilder();
        String selectorType = props.getProperty("selectortype", "radio");
        String storageunittype = dataSet.getString(row, "storageunittype", "");
        PropertyList explorerprops = props.getPropertyListNotNull("explorerprops");
        PropertyList colorprops = explorerprops.getPropertyListNotNull("colors");
        String backgroundcolor = colorprops.getProperty("backgroundcolor", "#EFF2F7");
        String fontcolor = colorprops.getProperty("fontcolor", "#000000");
        String bordercolor = colorprops.getProperty("bordercolor", "#E2E2E2");
        String expandonload = explorerprops.getProperty("expandonload", "Only First");
        String islast = explorerprops.getProperty("lastnodeflag", "N");
        boolean expanded = false;
        if ("Only First".equals(expandonload)) {
            if (row == 0) {
                expanded = true;
            }
        } else if ("All".equals(expandonload)) {
            expanded = true;
        }
        boolean renderchild = !"Y".equals(islast);
        String storageunitid = dataSet.getString(row, "storageunitid");
        sb.append("<div class='suitem' suid='").append(storageunitid).append("' type='").append(storageunittype).append("' style='background:#FFFFFF;border-color:").append(bordercolor).append("'>");
        if (renderchild) {
            int specimencapacity = dataSet.getInt(row, "specimencapacity", -1);
            int specimencount = dataSet.getInt(row, "specimencount", 0);
            boolean isStorageContainer = false;
            String linksdcid = dataSet.getString(row, "linksdcid", "");
            String linkkeyid1 = dataSet.getString(row, "linkkeyid1", "");
            if (linksdcid.length() > 0 && linkkeyid1.length() > 0 && this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid from trackitem where linksdcid = ? and linkkeyid1 = ?", (Object[])new String[]{dataSet.getString(row, "linksdcid", ""), dataSet.getString(row, "linkkeyid1", "")}).size() > 0) {
                isStorageContainer = true;
                specimencapacity = StorageUnitUtil.getMaxTrackItemAllowedInStorageContainer(this.getQueryProcessor(), linksdcid, linkkeyid1);
                specimencount = StorageUnitUtil.getTrackItemCountInStorageContainer(this.getQueryProcessor(), linksdcid, linkkeyid1);
            }
            String displayhtml = StorageUnitUtil.parseExplorerDisplayData(explorerprops.getPropertyListNotNull("displaydata"), storageunitid, row, specimencount, specimencapacity, this.getQueryProcessor(), this.getTranslationProcessor());
            PropertyList viewpage = explorerprops.getPropertyListNotNull("viewpage");
            String viewpagelink = viewpage.getProperty("page").trim();
            sb.append("<div class='suitem-title' style='background:").append(backgroundcolor).append(";color:").append(fontcolor).append(";'");
            sb.append(" linkkeyid1=\"").append(linkkeyid1).append("\" storageunitid=\"").append(storageunitid).append("\"");
            if (viewpagelink.length() > 0) {
                String viewpagewidth = viewpage.getProperty("width", "640");
                String viewpageheight = viewpage.getProperty("height", "480");
                sb.append(" viewpage='").append(viewpagelink).append("'");
                sb.append(" viewpagewidth='").append(viewpagewidth).append("' viewpageheight='").append(viewpageheight).append("'");
            }
            sb.append(">");
            if (!"none".equals(selectorType)) {
                sb.append("<div class='selector'><input type='").append(selectorType).append("' name='suselect' class='suselector' suid='").append(storageunitid).append("'></div>");
            }
            sb.append("<div class='sudata'>").append(displayhtml).append("</div>");
            sb.append("<div style='float:right;' class='suoperations'>");
            if (!isStorageContainer) {
                if (expanded) {
                    sb.append("<img title='").append(this.getTranslationProcessor().translate("Collapse")).append("' src='rc?command=image&image=FlatBlackNoEntry");
                } else {
                    sb.append("<img title='").append(this.getTranslationProcessor().translate("Expand")).append("' src='rc?command=image&image=FlatBlackPlus2");
                }
                sb.append("&color=").append(fontcolor).append("' class='operationimg' onclick='togglethis(this);' color='").append(fontcolor).append("'>");
            }
            sb.append("</div></div>");
            sb.append("<div class='suitem-body' storageunitid='").append(dataSet.getString(row, "storageunitid")).append("'");
            sb.append(" renderchild='Y' expandonload='").append(expanded ? "Y" : "N").append("'");
            sb.append(" orientation='").append(props.getProperty("orientation")).append("'");
            if (!expanded) {
                sb.append(" style=\"display:none;color:").append(fontcolor).append("\" childrendered=\"N\"");
            } else {
                sb.append(" style=\"color:").append(fontcolor).append(";\" childrendered=\"Y\"");
            }
            sb.append(" level='").append(level).append("'>&nbsp;</div>");
        } else {
            PropertyList lastNodeTypeDefinition = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
            PropertyList explorerProps = lastNodeTypeDefinition.getPropertyListNotNull("explorerprops");
            PropertyList lastNodeColorProps = explorerProps.getPropertyListNotNull("colors");
            String lastNodeBackgroundColor = lastNodeColorProps.getProperty("backgroundcolor", "#EFF2F7");
            String lastNodeFontColor = lastNodeColorProps.getProperty("fontcolor", "#000000");
            PropertyList lastNodeViewPage = explorerprops.getPropertyListNotNull("viewpage");
            String lastNodeViewPageLink = lastNodeViewPage.getProperty("page").trim();
            String sql = "select trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1,   storageunit.storageunitid, storageunit.storageunittype, storageunit.storageunitlabel, storageunit.propertytreeid, storageunit.maxtiallowed,   storageunit.storageunitindex, storageunit.storageunitsize, (select count(t2.trackitemid) from trackitem t2 where t2.currentstorageunitid = storageunit.storageunitid) boxticount, (select count(t3.trackitemid) from trackitem t3, storageunit s2 where t3.currentstorageunitid = s2.storageunitid and s2.parentid = storageunit.storageunitid) boxposticount  from TRACKITEM LEFT OUTER JOIN storageunit on storageunit.linksdcid = trackitem.LINKSDCID and storageunit.LINKKEYID1 = trackitem.LINKKEYID1 where trackitem.currentstorageunitid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                String childstorageunitid;
                int i;
                sb.append("<div class='suitem-title' style='background:").append(lastNodeBackgroundColor).append(";color:").append(lastNodeFontColor).append(";'");
                sb.append(" storageunitid=\"").append(storageunitid).append("\"");
                if (lastNodeViewPageLink.length() > 0) {
                    String viewpagewidth = lastNodeViewPage.getProperty("width", "640");
                    String viewpageheight = lastNodeViewPage.getProperty("height", "480");
                    sb.append(" viewpage='").append(lastNodeViewPageLink).append("'");
                    sb.append(" viewpagewidth='").append(viewpagewidth).append("' viewpageheight='").append(viewpageheight).append("'");
                }
                sb.append(">");
                int maxtiallowed = dataSet.getInt(row, "maxtiallowed", 0);
                if (maxtiallowed > 1 || maxtiallowed == -1) {
                    sb.append("<input type='").append(selectorType).append("' name='suselect' class='suselector' suid='").append(storageunitid).append("' style='margin-top:2px;'>");
                }
                int specimencapacity = dataSet.getInt(row, "specimencapacity", -1);
                int specimencount = dataSet.getInt(row, "specimencount", 0);
                String lastNodeDisplayHTML = StorageUnitUtil.parseExplorerDisplayData(explorerprops.getPropertyListNotNull("displaydata"), storageunitid, row, specimencount, specimencapacity, this.getQueryProcessor(), this.getTranslationProcessor());
                sb.append("<span style='width:10px;'></span>");
                sb.append(lastNodeDisplayHTML);
                sb.append("</div>");
                sb.append("<div class='suitem-body'>");
                int trackitemcount = 0;
                String sdcid = "NONE";
                for (i = 0; i < ds.size(); ++i) {
                    childstorageunitid = ds.getString(i, "storageunitid", "");
                    if (childstorageunitid.length() != 0) continue;
                    ++trackitemcount;
                    if ("trackitem(s)".equals(sdcid)) continue;
                    if ("NONE".equals(sdcid)) {
                        sdcid = ds.getString(i, "linksdcid", "");
                        continue;
                    }
                    if (sdcid.equals(ds.getString(i, "linksdcid", ""))) continue;
                    sdcid = "trackitem(s)";
                }
                if (trackitemcount > 0) {
                    String sdcdisplayname = this.getSDCProcessor().getProperty(sdcid, trackitemcount > 1 ? "plural" : "singular", sdcid);
                    sb.append("<div class='suitem' style='border:0;'>");
                    sb.append("<div class='suitem-title' style='display:flex;margin-top:2px;'>");
                    sb.append("<div style='font-style:italic;'>");
                    sb.append("<a title='View Trackitems' href='#ViewTrackitems' onclick=\"javascript:lvShowTrackItemsOnStorage('").append(storageunitid).append("');\">");
                    sb.append(trackitemcount).append(" ").append(this.getTranslationProcessor().translate(sdcdisplayname));
                    sb.append("</a></div>");
                    sb.append("</div></div>");
                }
                for (i = 0; i < ds.size(); ++i) {
                    childstorageunitid = ds.getString(i, "storageunitid", "");
                    if (childstorageunitid.length() <= 0) continue;
                    String childStorageUnitType = ds.getString(i, "storageunittype");
                    String linksdcid = ds.getString(i, "linksdcid");
                    String linkkeyid1 = ds.getString(i, "linkkeyid1");
                    String trackitemid = ds.getString(i, "trackitemid");
                    PropertyList storageUnitTypeDefinition = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), childStorageUnitType);
                    PropertyList childExplorerProps = storageUnitTypeDefinition.getPropertyListNotNull("explorerprops");
                    PropertyList viewpage = childExplorerProps.getPropertyListNotNull("viewpage");
                    String viewpagelink = viewpage.getProperty("page");
                    PropertyList childColorProps = childExplorerProps.getPropertyListNotNull("colors");
                    String childBackgroundColor = childColorProps.getProperty("backgroundcolor", "#EFF2F7");
                    String childFontColor = childColorProps.getProperty("fontcolor", "#000000");
                    String childBorderColor = childColorProps.getProperty("bordercolor", "#E2E2E2");
                    int maxcontentcount = StorageUnitUtil.getMaxTrackItemAllowedInStorageContainer(this.getQueryProcessor(), childstorageunitid);
                    int contentcount = ds.getInt(i, "boxticount", 0) + ds.getInt(i, "boxposticount", 0);
                    String displayhtml = StorageUnitUtil.parseExplorerDisplayData(childExplorerProps.getPropertyListNotNull("displaydata"), childstorageunitid, row + 1, contentcount, maxcontentcount, this.getQueryProcessor(), this.getTranslationProcessor());
                    sb.append("<div class='suitem' suid='").append(childstorageunitid).append("' type='").append(childStorageUnitType).append("' style='border:0;'>");
                    sb.append("<div class='suitem-title' linkkeyid1='").append(linkkeyid1).append("' style='display:flex;background:").append(childBackgroundColor);
                    sb.append(";color:").append(childFontColor);
                    sb.append(";border:1px solid ").append(childBorderColor).append(";margin-top:2px;'");
                    sb.append(" storageunitid=\"").append(childstorageunitid).append("\"");
                    if (viewpagelink.length() > 0) {
                        String viewpagewidth = viewpage.getProperty("width", "640");
                        String viewpageheight = viewpage.getProperty("height", "480");
                        sb.append(" viewpage='").append(viewpagelink).append("'");
                        sb.append(" viewpagewidth='").append(viewpagewidth).append("' viewpageheight='").append(viewpageheight).append("'");
                    }
                    sb.append(">");
                    if (!"none".equals(selectorType)) {
                        sb.append("<div class='selector'>");
                        sb.append("<input type='").append(selectorType).append("' name='suselect' class='suselector' suid='").append(childstorageunitid).append("'");
                        sb.append(" iscontainer='Y' trackitemid='").append(trackitemid).append("' sdcid='").append(linksdcid).append("' keyid1='").append(linkkeyid1).append("'>");
                        sb.append("</div>");
                    }
                    sb.append("<div class='sudata'>").append(displayhtml).append("</div>");
                    sb.append("</div>");
                    sb.append("</div>");
                }
                sb.append("</div>");
            } else {
                int maxcontentcount = dataSet.getInt(row, "maxtiallowed", 0);
                String displayhtml = StorageUnitUtil.parseExplorerDisplayData(explorerprops.getPropertyListNotNull("displaydata"), storageunitid, row, 0, maxcontentcount, this.getQueryProcessor(), this.getTranslationProcessor());
                sb.append("<div class='suitem-title' style='background:").append(backgroundcolor).append(";color:").append(fontcolor).append(";'");
                sb.append(" storageunitid=\"").append(dataSet.getString(0, "storageunitid")).append("\"");
                if (lastNodeViewPageLink.length() > 0) {
                    String viewpagewidth = lastNodeViewPage.getProperty("width", "640");
                    String viewpageheight = lastNodeViewPage.getProperty("height", "480");
                    sb.append(" viewpage='").append(lastNodeViewPageLink).append("'");
                    sb.append(" viewpagewidth='").append(viewpagewidth).append("' viewpageheight='").append(viewpageheight).append("'");
                }
                sb.append(">");
                if (maxcontentcount > 0 || maxcontentcount == -1) {
                    sb.append("<input type='").append(selectorType).append("' name='suselect' class='suselector' suid='").append(storageunitid).append("' style='margin-top:2px;'>1");
                }
                sb.append(displayhtml);
                sb.append("</div>");
                sb.append("<div class='emptynode'>&nbsp;</div>");
            }
        }
        sb.append("</div>");
        return sb.toString();
    }
}

