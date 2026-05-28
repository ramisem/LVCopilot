/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.storage.Label;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import java.util.HashMap;
import java.util.UUID;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StorageUnitRenderer
extends BaseElement {
    public static String LABVANTAGE_CVS_ID = "$Revision: 90253 $";
    public static final String LAYOUT_GRID = "Grid";
    public static final String LAYOUT_CIRCULAR = "Circular";
    public static final String LAYOUT_LINEAR = "Linear";
    public static final String LAYOUT_NO_LAYOUT = "No Layout";
    public static final String DEFAULTCOLOR_BORDER = "#cdcdcd";
    public static final String DEFAULTCOLOR_BACKGROUND = "#efefef";
    public static final String DEFAULTCOLOR_CELL_EMPTY = "#ffffff";
    public static final String DEFAULTCOLOR_CELL_FILLED = "#ffa500";
    public static final String DEFAULTCOLOR_CELL_RESERVED = "#cdc9c9";
    public static final String DEFAULTCOLOR_CELL_START = "#9acd32";
    public static final String DEFAULTCOLOR_CELL_DISABLED = "#999";
    public static final String DEFAULTCOLOR_CELL_BORDER = "#b0c4de";
    public static final String DEFAULTCOLOR_CELL_FONT = "#111";
    public static final String DEFAULTCOLOR_HOVERDIV_BORDER = "#4682b4";
    public static final String DEFAULTCOLOR_HOVERDIV_BACKGROUND = "#cae1ff";
    public static final String DEFAULTCOLOR_HEADER_BACKGROUND = "#abcdef";
    public static final String DEFAULTCOLOR_HEADER_FONT = "#000000";
    public static final String ORIENTATION_HORIZONTAL = "Horizontal";
    public static final String ORIENTATION_VERTICAL = "Vertical";
    public static final String ORIENTATION_CIRCULAR = "Circular";
    public static final String ORIENTATION_ROWMAJOR = "Row Major";
    public static final String ORIENTATION_COLUMNMAJOR = "Column Major";
    public static final String STATUS_FILLED = "filled";
    public static final String STATUS_EMPTY = "empty";
    public static final String STATUS_RESERVED = "reserved";
    public static final String STATUS_DISABLED = "disabled";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String NEWLINE = "\n";

    @Override
    public String getHtml() {
        String thiselementid = this.element.getId();
        String elementid = "S" + String.valueOf(System.currentTimeMillis());
        String mode = this.element.getProperty("mode");
        PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        String storageUnitId = pagedata.getProperty(this.element.getId() + "_storageunitid", pagedata.getProperty("storageunitid"));
        this.element.setProperty("property_storageunitid", storageUnitId);
        this.element.setProperty("property_sdcid", pagedata.getProperty("sdcid"));
        this.element.setProperty("property_keyid1", pagedata.getProperty("keyid1"));
        this.element.setProperty("property_isRTL", this.getConnectionProcessor().getSapphireConnection().isRtl() ? YES : NO);
        this.element.setProperty("property_sysuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        this.element.setId(elementid);
        String html = "<script type=\"text/javascript\" src=\"WEB-CORE/elements/scripts/storageunit.js\"></script>";
        if ("Consolidate".equals(mode)) {
            html = html + "<link rel=\"stylesheet\" href=\"WEB-OPAL/pagetypes/storage/storageconsolidate/storageconsolidate.css\">";
            html = html + "<script type=\"text/javascript\" src=\"WEB-OPAL/pagetypes/storage/storageconsolidate/storageconsolidate.js\"></script>";
            html = html + "<script type=\"text/javascript\">";
            html = html + "event_draggable.containment = 'table.sutable';";
            html = html + "event_draggable.appendTo = 'table.sutable';";
            html = html + "$( document ).ready( function() {handleBoxHolderAdd( '" + elementid + "' ) } );";
            html = html + "</script>";
            html = html + "<input type='hidden' name='__propertyhandler_" + elementid + "' value='com.labvantage.sapphire.pageelements.storageunit.StorageUnitRendererPropertyHandler'/>";
            html = html + "<input type='hidden' id='__" + elementid + "_data' name='__" + elementid + "_data' value=''>";
        }
        html = html + StorageUnitRenderer.getDisplayHTML(this.element, this.getQueryProcessor(), this.getTranslationProcessor(), this.connectionInfo.getSysuserId());
        if ("Edit".equals(mode) || mode.length() == 0) {
            html = html + "<script>var " + thiselementid + " = " + elementid + ";</script>";
        }
        return html;
    }

    public static String getDisplayHTML(PropertyList element, QueryProcessor queryProcessor, TranslationProcessor translationProcessor, String sysuserid) {
        if (YES.equals(element.getProperty("property_consolidatepage"))) {
            element.setId("S" + OpalUtil.getUniqueID());
        }
        String layout = LAYOUT_GRID;
        String html = "";
        String storageunittype = "";
        String show = element.getProperty("show", YES);
        String storageUnitId = element.getProperty("property_storageunitid");
        if (!NO.equals(show)) {
            DataSet storageUnit;
            String mode = element.getProperty("mode", "Edit");
            boolean editMode = "Edit".equals(mode);
            if (!editMode && StringUtil.getLen(storageUnitId) == 0L) {
                storageUnitId = OpalUtil.getColumnValue(queryProcessor, "storageunit", "storageunitid", "linksdcid = ? and linkkeyid1 = ?", new String[]{element.getProperty("property_sdcid"), element.getProperty("property_keyid1")});
                element.setProperty("property_storageunitid", storageUnitId);
                if (StringUtil.getLen(storageUnitId) == 0L) {
                    return "No storage unit found for SDI (" + element.getProperty("property_sdcid") + " " + element.getProperty("property_keyid1") + ")";
                }
            }
            if (storageUnitId != null && storageUnitId.length() > 0 && (storageUnit = queryProcessor.getPreparedSqlDataSet("select su.trackitemallowedflag, su.linksdcid, su.linkkeyid1, su.linkkeyid2, su.linkkeyid3, su.labelpath, su.storageunitsize, su.storageunittype, su.propertytreeid from storageunit su where su.storageunitid = ?" + ("Consolidate".equals(mode) ? "" : " and su.maxtiallowed = 0"), (Object[])new String[]{storageUnitId})) != null && storageUnit.size() == 1) {
                layout = storageUnit.getString(0, "propertytreeid");
                element.setProperty("property_layout", layout);
                int storageunitsize = storageUnit.getInt(0, "storageunitsize");
                storageunittype = storageUnit.getString(0, "storageunittype");
                PropertyList storageunitprops = StorageUnitUtil.getDefinition(new WebAdminProcessor(queryProcessor.getConnectionid()), storageunittype, layout);
                if (layout != null && storageunitprops != null) {
                    PropertyList list;
                    element.setProperty("property_storageunitsize", String.valueOf(storageunitsize));
                    PropertyList cellDisplayFormatProps = storageunitprops.getPropertyListNotNull("rendererdisplayprops");
                    PropertyListCollection displaycolumns = cellDisplayFormatProps.getCollectionNotNull("columns");
                    PropertyListCollection displayformats = cellDisplayFormatProps.getCollectionNotNull("formats");
                    if (displayformats.size() > 0) {
                        PropertyListCollection _displayFormats = new PropertyListCollection();
                        for (int i = 0; i < displayformats.size(); ++i) {
                            if (NO.equals(displayformats.getPropertyList(i).getProperty("show"))) continue;
                            _displayFormats.add(displayformats.getPropertyList(i));
                        }
                        displayformats.clear();
                        displayformats.addAll(_displayFormats);
                        _displayFormats.clear();
                    }
                    if (displaycolumns.size() == 0) {
                        list = new PropertyList();
                        list.setProperty("parameter", "storageunitindex");
                        list.setProperty("columnid", "storageunitindex");
                        displaycolumns.add(list);
                    }
                    if (displayformats.size() == 0) {
                        list = new PropertyList();
                        list.setProperty("title", translationProcessor.translate("Storage Unit Index"));
                        list.setProperty("displayformat", "[storageunitindex]");
                        displayformats.add(list);
                    }
                    element.setProperty("_displaycolumns", displaycolumns);
                    element.setProperty("_displayformats", displayformats);
                    html = "<script>";
                    html = html + "\nvar __storageunit_layout = '" + layout + "';";
                    html = html + "</script>";
                    if (layout.equals("Circular") || layout.equals(LAYOUT_GRID) || layout.equals(LAYOUT_LINEAR)) {
                        html = html + StorageUnitRenderer.renderHtml(element, storageunitprops, queryProcessor, translationProcessor, sysuserid);
                    } else if (layout.equals(LAYOUT_NO_LAYOUT) && "Consolidate".equals(mode)) {
                        html = html + StorageUnitRenderer.renderNoLayoutHtml(element, storageunitprops, queryProcessor, translationProcessor);
                    }
                }
            }
        }
        if (html.length() == 0) {
            html = "<script>var __storageunit_layout = '';if(typeof(handleStorageWithNoLayout)=='function'){handleStorageWithNoLayout();}</script>";
            html = html + translationProcessor.translate("Storage Unit has no layout/is not shown") + " (" + storageUnitId + ")";
        } else {
            html = "<div id='" + element.getId() + "_container' style='padding-bottom:10px;'>" + html + "</div>";
            if (!LAYOUT_NO_LAYOUT.equals(layout)) {
                html = html + "<script>" + element.getId() + ".resetCellSize();</script>";
            }
        }
        return html;
    }

    private static String renderNoLayoutHtml(PropertyList element, PropertyList storageunitprops, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        String id = element.getId();
        String labelpath = "";
        int maxtiallowed = 0;
        boolean consolidationMode = "Consolidate".equals(element.getProperty("mode"));
        String storageunitid = element.getProperty("property_storageunitid");
        DataSet suds = queryProcessor.getPreparedSqlDataSet("select storageunitid, maxtiallowed, labelpath from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        if (suds != null && suds.size() > 0) {
            labelpath = suds.getString(0, "labelpath");
            maxtiallowed = suds.getInt(0, "maxtiallowed");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet("select trackitemid, linksdcid, linkkeyid1 from trackitem where currentstorageunitid = ?", (Object[])new String[]{storageunitid});
        StringBuilder sb = new StringBuilder();
        sb.append(StorageUnitRenderer.getStyles(element, queryProcessor));
        String topbarDisplayHTML = StorageUnitUtil.parseExplorerDisplayData(storageunitprops.getPropertyListNotNull("explorerprops").getPropertyListNotNull("displaydata"), storageunitid, 1, StorageUnitUtil.getTrackItemCountInStorageContainer(queryProcessor, storageunitid), StorageUnitUtil.getMaxTrackItemAllowedInStorageContainer(queryProcessor, storageunitid), queryProcessor, translationProcessor);
        sb.append("<script>");
        sb.append("var ").append(id).append(" = new StorageUnit( \"").append(id).append("\" );");
        sb.append(id).append(".setLayout( \"No Layout\" );");
        sb.append("</script>");
        sb.append("<div style='padding:4px;background:#efefef;border:1px solid #ccc;' id='").append(element.getId()).append("_topbar'>");
        sb.append("<div class='storageunitrenderer_title' style='padding-bottom: 2px;margin-bottom: 2px;border-bottom: 1px solid #cfcfcf;'>").append(topbarDisplayHTML).append("</div>");
        PropertyListCollection displayformats = element.getCollectionNotNull("_displayformats");
        sb.append("<div style='display:flex;align-items:center;'>");
        sb.append(translationProcessor.translate("Show")).append("&nbsp;<select id='").append(element.getId()).append("_cellDisplayFormatSelect' onchange='").append(element.getId()).append(".handleNoLayoutDisplayFormatChange(this);'>");
        sb.append("<option value=\"[linksdcid] [linkkeyid1]\"></option>");
        for (int i = 0; i < displayformats.size(); ++i) {
            PropertyList list = displayformats.getPropertyList(i);
            if (list == null) continue;
            String title = list.getProperty("title");
            String displayformat = list.getProperty("format", "[linksdcid] [linkkeyid1]");
            displayformat = StringUtil.replaceAll(displayformat, NEWLINE, "<br>");
            displayformat = StringUtil.replaceAll(displayformat, "\"", "&quot;");
            boolean selected = YES.equals(list.getProperty("default", NO));
            sb.append("<option").append(selected ? " selected" : "").append(" value=\"").append(displayformat).append("\">").append(title).append("</option>");
            if (!selected) continue;
            element.setProperty("_displayformat", displayformat);
        }
        if (OpalUtil.isEmpty(element.getProperty("_displayformat"))) {
            element.setProperty("_displayformat", "[storageunitindex]");
        }
        sb.append("</select>");
        sb.append("&nbsp;<div style='display:flex;align-items:center;padding-left:4px;'>");
        sb.append("<img src='rc?command=image&image=FlatBlackTextSizePlus' class='buttonFontResize buttonFontLeft'");
        sb.append(" title=\"").append(translationProcessor.translate("Increase Font Size")).append("\"");
        sb.append(" onclick=\"resizeRendererFont('").append(element.getId()).append("',true)\">");
        sb.append("<img src='rc?command=image&image=FlatBlackTextSizeMinus' class='buttonFontResize buttonFontRight'");
        sb.append(" title=\"").append(translationProcessor.translate("Decrease Font Size")).append("\"");
        sb.append(" onclick=\"resizeRendererFont('").append(element.getId()).append("',false)\" style='width:12px;'>");
        sb.append("</div></div>");
        sb.append("</div>");
        sb.append("<textarea id='").append(element.getId()).append("_displayFormatColumns' style='display:none' readonly>").append(element.getCollectionNotNull("_displaycolumns").toJSONString()).append("</textarea>");
        sb.append("<div style='height:10px;'></div>");
        sb.append("<div style='position:relative;' index='0' maxtiallowed='").append(maxtiallowed).append("' storageunitid='").append(storageunitid).append("'");
        sb.append(" labelpath='").append(labelpath).append("' class='nolayoutcellcontainer sutable' id='").append(id).append("'");
        if (consolidationMode) {
            if (StorageUnitUtil.getStorageRestrictions(queryProcessor, storageunitid, new ConnectionProcessor(queryProcessor.getConnectionid()).isOra()).size() > 0) {
                sb.append(" restrictions='Y'");
            }
            sb.append(">");
        }
        if (ds.size() > 0) {
            PropertyListCollection displaycolumns = element.getCollectionNotNull("_displaycolumns");
            String displayformat = element.getProperty("_displayformat");
            for (int i = 0; i < ds.size(); ++i) {
                String trackitemid = ds.getString(i, "trackitemid");
                String displayhtml = StorageUnitUtil.parseRendererDisplayData(displaycolumns, displayformat, ds, i, translationProcessor);
                sb.append("<div class='nolayoutcell cell' name='cell' status='filled'");
                sb.append(" id='").append(UUID.randomUUID().toString()).append("'");
                sb.append(" sdcid='").append(ds.getString(i, "linksdcid", "")).append("'");
                sb.append(" keyid1='").append(ds.getString(i, "linkkeyid1", "")).append("'");
                sb.append(" labelpath='").append(labelpath).append("'");
                sb.append(" storageunitid='").append(storageunitid).append("'");
                sb.append(" trackitemid='").append(trackitemid).append("'>");
                sb.append(displayhtml);
                sb.append("</div>");
            }
        } else {
            sb.append(translationProcessor.translate("Empty"));
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static String renderHtml(PropertyList element, PropertyList storageunitprops, QueryProcessor queryProcessor, TranslationProcessor translationProcessor, String sysuserid) {
        StringBuilder sb = new StringBuilder();
        String storageunitid = element.getProperty("property_storageunitid");
        DataSet ds = StorageUnitRenderer.getChildStorageUnitDataSet(queryProcessor, translationProcessor, storageunitid, element.getCollectionNotNull("_displaycolumns"), sysuserid);
        if (ds.size() > 0) {
            int contentCount;
            boolean editMode = "Edit".equals(element.getProperty("mode", "Edit"));
            sb.append(StorageUnitRenderer.getStyles(element, queryProcessor));
            PropertyList cellColors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors");
            PropertyListCollection displayformats = element.getCollectionNotNull("_displayformats");
            int maxContentCount = -1;
            StorageUnitTypeDef sutypedef = StorageUnitTypeDef.getInstance();
            DataSet rootds = queryProcessor.getPreparedSqlDataSet("select s.storageunitid, s.storageunittype, (sus.specimencount + coalesce(sus.extraspecimentotal, 0)) specimencount, sus.specimencapacity from storageunit s left outer join storageunitstats sus on sus.storageunitid = s.storageunitid where s.storageunitid = ?", (Object[])new String[]{storageunitid});
            if (rootds != null && rootds.size() > 0) {
                String storageunittype = rootds.getString(0, "storageunittype", "");
                String specimencapacity = rootds.getValue(0, "specimencapacity", "");
                String specimencount = rootds.getValue(0, "specimencount", "");
                if (specimencount.length() == 0 && specimencapacity.length() == 0 && sutypedef.isStorageContainer(storageunittype, sutypedef.getTypeMap(queryProcessor))) {
                    contentCount = StorageUnitUtil.getTrackItemCountInStorageContainer(queryProcessor, storageunitid);
                    maxContentCount = StorageUnitUtil.getMaxTrackItemAllowedInStorageContainer(queryProcessor, storageunitid);
                } else {
                    contentCount = rootds.getInt(0, "specimencount", 0);
                    maxContentCount = rootds.getInt(0, "specimencapacity", -1);
                }
            } else {
                contentCount = StorageUnitUtil.getTrackItemCountInStorageContainer(queryProcessor, storageunitid);
                maxContentCount = StorageUnitUtil.getMaxTrackItemAllowedInStorageContainer(queryProcessor, storageunitid);
            }
            String topbarDisplayHTML = StorageUnitUtil.parseExplorerDisplayData(storageunitprops.getPropertyListNotNull("explorerprops").getPropertyListNotNull("displaydata"), storageunitid, 1, contentCount, maxContentCount, queryProcessor, translationProcessor);
            sb.append("<div style='padding:4px;background:#efefef;border:1px solid #ccc;' id='").append(element.getId()).append("_topbar'>");
            sb.append("<div class='storageunitrenderer_title' style='padding-bottom: 2px;margin-bottom: 2px;border-bottom: 1px solid #cfcfcf;'>").append(topbarDisplayHTML).append("</div>");
            sb.append("<div style='display:flex;'>");
            sb.append("<div>");
            sb.append(translationProcessor.translate("Show")).append("&nbsp;<select id='").append(element.getId()).append("_cellDisplayFormatSelect' class='input_field' onchange='").append(element.getId()).append(".handleDisplayFormatChange(this);'>");
            sb.append("<option value=\"[storageunitindex]\"></option>");
            for (int i = 0; i < displayformats.size(); ++i) {
                PropertyList list = displayformats.getPropertyList(i);
                if (list == null) continue;
                String title = list.getProperty("title");
                String displayformat = list.getProperty("format", "[storageunitindex]");
                displayformat = StringUtil.replaceAll(displayformat, NEWLINE, "<br>");
                displayformat = StringUtil.replaceAll(displayformat, "\"", "&quot;");
                boolean selected = YES.equals(list.getProperty("default", NO));
                sb.append("<option").append(selected ? " selected" : "").append(" value=\"").append(displayformat).append("\">").append(title).append("</option>");
                if (!selected) continue;
                element.setProperty("_displayformat", displayformat);
            }
            if (OpalUtil.isEmpty(element.getProperty("_displayformat"))) {
                element.setProperty("_displayformat", "[storageunitindex]");
            }
            sb.append("</select>");
            sb.append("</div>");
            sb.append("&nbsp;<div style='display:flex;align-items:center;padding-left:4px;'>");
            sb.append("<img src='rc?command=image&image=FlatBlackTextSizePlus' class='buttonFontResize buttonFontLeft'");
            sb.append(" title=\"").append(translationProcessor.translate("Increase Font Size")).append("\"");
            sb.append(" onclick=\"resizeRendererFont('").append(element.getId()).append("',true)\">");
            sb.append("<img src='rc?command=image&image=FlatBlackTextSizeMinus' class='buttonFontResize buttonFontRight'");
            sb.append(" title=\"").append(translationProcessor.translate("Decrease Font Size")).append("\"");
            sb.append(" onclick=\"resizeRendererFont('").append(element.getId()).append("',false)\" style='width:12px;'>");
            sb.append("</div>");
            sb.append("<div style='display:flex;align-items:center;padding-left:4px;'>");
            sb.append("<div style='display:flex;border-left:1px solid #666;border-top:1px solid #666;cursor:pointer;' id='legendBar' onclick=\"su_toggleLegendContainer('").append(element.getId()).append("')\"");
            sb.append(" title='").append(translationProcessor.translate("Show Legend")).append("'>");
            sb.append("<div style='width:4px;height:100%;background:").append(cellColors.getProperty(STATUS_EMPTY, DEFAULTCOLOR_CELL_EMPTY)).append(";border-bottom:1px solid #666;border-right:1px solid #666;'>&nbsp;</div>");
            sb.append("<div style='width:4px;height:100%;background:").append(cellColors.getProperty(STATUS_FILLED, DEFAULTCOLOR_CELL_FILLED)).append(";border-bottom:1px solid #666;border-right:1px solid #666;'>&nbsp;</div>");
            sb.append("<div style='width:4px;height:100%;background:").append(cellColors.getProperty(STATUS_RESERVED, DEFAULTCOLOR_CELL_RESERVED)).append(";border-bottom:1px solid #666;border-right:1px solid #666;'>&nbsp;</div>");
            sb.append("<div style='width:4px;height:100%;background:").append(cellColors.getProperty("start", DEFAULTCOLOR_CELL_START)).append(";border-bottom:1px solid #666;border-right:1px solid #666;'>&nbsp;</div>");
            sb.append("</div>");
            if (editMode) {
                sb.append("<div>&nbsp;<img src='rc?command=image&image=FlatBlackCog' onclick=\"su_toggleOptions('").append(element.getId()).append("')\" id='rendererSetting'");
                sb.append(" style='cursor:pointer;width:16px;' title='").append(translationProcessor.translate("Show Options")).append("'></div>");
            }
            sb.append("</div>");
            if ("Consolidate".equals(element.getProperty("mode"))) {
                sb.append("<img src='rc?command=image&image=FlatBlackSearch' onclick=\"su_toggleSearchBox('").append(element.getId()).append("')\" id='searchBoxImg'");
                sb.append(" style='cursor:pointer;width:16px;padding-left:6px;' title='").append(translationProcessor.translate("Search")).append("'>");
            }
            sb.append("</div>");
            sb.append(StorageUnitRenderer.renderLegends(element, queryProcessor, translationProcessor));
            if (editMode) {
                sb.append(StorageUnitRenderer.renderOptions(element, storageunitprops, translationProcessor));
            }
            if ("Consolidate".equals(element.getProperty("mode"))) {
                sb.append("<div id='").append(element.getId()).append("_searchBoxContainer' style='display:none;padding:6px;'>");
                sb.append("<fieldset style='border-radius:4px;'><legend>").append(translationProcessor.translate("Select")).append("</legend>");
                sb.append("<div style='display:flex;width:100%;'>");
                sb.append("<select class='input_field' id='").append(element.getId()).append("_searchBoxSelect'>");
                PropertyList suprops = StorageUnitTypeDef.getInstance().getTypeDefinitionByID(queryProcessor, element.getProperty("property_storageunitid"));
                PropertyListCollection collection = suprops.getCollectionNotNull("contentsearch");
                StringBuilder sb2 = new StringBuilder();
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList propertyList = collection.getPropertyList(i);
                    String show = propertyList.getProperty("show", YES);
                    String id = propertyList.getProperty("id");
                    String title = propertyList.getProperty("title");
                    String sql = propertyList.getProperty("sql");
                    if (!YES.equals(show) || id.length() <= 0 || title.length() <= 0 || sql.length() <= 0) continue;
                    sb.append("<option value='").append(id).append("'>").append(title).append("</option>");
                    sb2.append("<span id='").append(element.getId()).append("_sql_").append(id).append("' style='display:none'>").append(sql).append("</span>");
                }
                sb.append("</select>");
                sb.append((CharSequence)sb2);
                sb.append("<input class='input_field searchSUBoxField' style='width:200px;' elementid='").append(element.getId()).append("'>");
                sb.append("<img src='rc?command=image&image=FlatBlackSearch&size=16' class='ui-button ui-widget ui-corner-all' onclick=\"su_searchBox('").append(element.getId()).append("')\">");
                sb.append("<input type=\"checkbox\" id=\"").append(element.getId()).append("_search_reset\" checked>&nbsp;<label for=\"").append(element.getId()).append("_search_reset\">").append(translationProcessor.translate("Reset Search")).append("</label>");
                sb.append("<div class='notFoundMessage' style='display:none;padding:4px;'>&nbsp;<img src='rc?command=image&image=FlatBlackWarning&color=red'> ").append(translationProcessor.translate("Not found"));
                sb.append("<span class='notFoundSearchText' style='padding:4px;'></span></div>");
                sb.append("</div>");
                sb.append("</fieldset>");
                sb.append("</div>");
            }
            sb.append("</div>");
            sb.append("<div style='height:10px;'></div>");
            sb.append("<script>");
            sb.append("var cellcolor_filled = '").append(cellColors.getProperty(STATUS_FILLED, DEFAULTCOLOR_CELL_FILLED)).append("';");
            sb.append("var cellcolor_empty = '").append(cellColors.getProperty(STATUS_EMPTY, DEFAULTCOLOR_CELL_EMPTY)).append("';");
            sb.append("</script>");
            sb.append("<textarea id='").append(element.getId()).append("_displayFormatColumns' style='display:none' readonly>").append(element.getCollectionNotNull("_displaycolumns").toJSONString()).append("</textarea>");
            try {
                sb.append(StorageUnitRenderer.renderView(ds, element, storageunitprops, queryProcessor, translationProcessor));
            }
            catch (Exception e) {
                sb.append("<font color='red'>Exception: ").append(e.getMessage());
            }
        }
        return sb.toString();
    }

    private static String renderView(DataSet ds, PropertyList element, PropertyList storageunitprops, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) throws Exception {
        boolean editMode = "Edit".equals(element.getProperty("mode", "Edit"));
        boolean consolidationMode = "Consolidate".equals(element.getProperty("mode"));
        String cellShowIndex = element.getPropertyListNotNull("cell").getProperty("showindex", YES);
        PropertyListCollection displaycolumns = element.getCollectionNotNull("_displaycolumns");
        String displayformat = element.getProperty("_displayformat");
        String id = element.getId();
        PropertyList cellColors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors");
        StringBuilder sb = new StringBuilder();
        sb.append("<table id='").append(id).append("' cellpadding=0 cellspacing=0 border=0 style='direction:ltr' class='sutable'");
        if (consolidationMode) {
            String storageunitid = element.getProperty("property_storageunitid");
            if (StorageUnitUtil.getStorageRestrictions(queryProcessor, storageunitid, new ConnectionProcessor(queryProcessor.getConnectionid()).isOra()).size() > 0) {
                sb.append(" restrictions='Y'");
            }
            sb.append(">");
        } else {
            sb.append(">");
        }
        sb.append("</table>");
        sb.append(NEWLINE).append("<script>");
        sb.append(NEWLINE).append("var ").append(id).append(" = new StorageUnit( \"").append(id).append("\", ").append(YES.equals(element.getProperty("animate", YES))).append(" );");
        sb.append(NEWLINE).append(id).append(".setColor( \"empty\", \"").append(cellColors.getProperty(STATUS_EMPTY, DEFAULTCOLOR_CELL_EMPTY)).append("\" );");
        sb.append(NEWLINE).append(id).append(".setColor( \"filled\", \"").append(cellColors.getProperty(STATUS_FILLED, DEFAULTCOLOR_CELL_FILLED)).append("\" );");
        sb.append(NEWLINE).append(id).append(".setColor( \"reserved\", \"").append(cellColors.getProperty(STATUS_RESERVED, DEFAULTCOLOR_CELL_RESERVED)).append("\" );");
        sb.append(NEWLINE).append(id).append(".setColor( \"start\", \"").append(cellColors.getProperty("start", DEFAULTCOLOR_CELL_START)).append("\" );");
        sb.append(NEWLINE).append(id).append(".setShowLocation( \"").append(cellShowIndex.equals(YES) ? "true" : "false").append("\" );");
        if (!editMode) {
            sb.append(NEWLINE).append(id).append(".setMode( \"").append(element.getProperty("mode", "Edit")).append("\" );");
        }
        sb.append(NEWLINE).append(StorageUnitRenderer.setRendererProps(element, storageunitprops, translationProcessor));
        sb.append(NEWLINE).append(id).append(".render();");
        PropertyListCollection sampleTypeColors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors").getCollectionNotNull("bysampletype");
        if (sampleTypeColors.size() > 0) {
            sb.append(NEWLINE);
            for (int i = 0; i < sampleTypeColors.size(); ++i) {
                PropertyList color = sampleTypeColors.getPropertyList(i);
                String sampletypeid = color.getProperty("sampletypeid");
                String sampletypecolor = color.getProperty("color");
                String sampletypefontcolor = color.getProperty("fontcolor", "#000");
                if (!OpalUtil.isNotEmpty(sampletypeid) || !OpalUtil.isNotEmpty(sampletypecolor)) continue;
                sb.append(id).append(".addSampleTypeColor('").append(sampletypeid).append("', '").append(sampletypecolor).append("', '").append(sampletypefontcolor).append("');\n");
            }
        }
        if (ds != null && ds.size() > 0) {
            int startPosition = 1;
            for (int i = 0; i < ds.size(); ++i) {
                String arraylayoutzone;
                int cell = i + 1;
                String storageunitid = ds.getString(i, "storageunitid");
                String trackitemid = ds.getString(i, "trackitemid", "");
                String sampletypeid = ds.getString(i, "sampletypeid", "");
                String sdcid = ds.getString(i, "trackitemsdcid", "");
                String keyid1 = ds.getString(i, "trackitemkeyid1", "");
                String status = ds.getString(i, "status");
                String displayhtml = StorageUnitUtil.parseRendererDisplayData(displaycolumns, displayformat, ds, i, translationProcessor);
                sb.append(NEWLINE).append(id).append(".initCellProperty( '").append(cell).append("', '").append(sdcid).append("', '").append(keyid1).append("', '").append(storageunitid).append("', '").append(translationProcessor.translate(SafeHTML.encodeForJavaScript(ds.getString(i, "storageunitlabel", "")))).append("', '").append(SafeHTML.encodeForJavaScript(ds.getValue(i, "labelpath"))).append("', ").append("\"").append(displayhtml).append("\", \"").append(sampletypeid).append("\" );");
                if (STATUS_FILLED.equals(status)) {
                    sb.append(NEWLINE).append(id).append(".putAt( '").append(cell).append("', '").append(trackitemid).append("' );");
                } else if (STATUS_RESERVED.equals(status)) {
                    sb.append(NEWLINE).append(id).append(".reserve( '").append(cell).append("', '").append(trackitemid).append("' );");
                } else if (STATUS_DISABLED.equals(status)) {
                    sb.append(NEWLINE).append(id).append(".disable( '").append(cell).append("', '").append(ds.getValue(i, "tooltip")).append("' );");
                }
                if (StringUtil.getLen(keyid1) > 0L) {
                    startPosition = cell + 1;
                }
                if ((arraylayoutzone = ds.getString(i, "arraylayoutzone", "")).length() <= 0) continue;
                sb.append(NEWLINE).append(element.getId()).append(".setCellZone('").append(storageunitid).append("', '").append(arraylayoutzone).append("'");
                sb.append(",'").append(element.getProperty("zone_" + arraylayoutzone)).append("' );");
            }
            if (startPosition > ds.size()) {
                startPosition = -1;
            }
            sb.append(NEWLINE).append(id).append(".setStartPosition( \"").append(startPosition).append("\" );");
            if (editMode) {
                sb.append(NEWLINE).append(id).append(".resetStartPosition();");
            }
        }
        PropertyListCollection displayformats = element.getCollectionNotNull("_displayformats");
        for (int i = 0; i < displayformats.size(); ++i) {
            PropertyList list = displayformats.getPropertyList(i);
            if (!YES.equals(list.getProperty("showmouseoverinfo", YES))) continue;
            sb.append(NEWLINE).append(element.getId()).append(".addToShowInfo( \"").append(list.getProperty("title")).append("\" );");
        }
        sb.append(NEWLINE).append("</script>");
        return sb.toString();
    }

    private static DataSet getChildStorageUnitDataSet(QueryProcessor queryProcessor, TranslationProcessor translationProcessor, String storageunitid, PropertyListCollection displaycolumns, String sysuserid) {
        DataSet data = new DataSet();
        DataSet reserveDS = StorageUnitRenderer.getReservedStorageUnits(queryProcessor, storageunitid, displaycolumns, sysuserid);
        StringBuilder sql = new StringBuilder("select storageunit.storageunitid, storageunit.linksdcid storagesdcid, storageunit.linkkeyid1 storagekeyid1,");
        sql.append(" (select s_sample.sampletypeid from s_sample where trackitem.linksdcid = 'Sample' and s_sample.s_sampleid = trackitem.linkkeyid1) sampletypeid,");
        sql.append(" storageunit.storageunitlabel, storageunit.labelpath, storageunit.storageunitindex, storageunit.maxtiallowed,");
        sql.append(" trackitem.trackitemid, trackitem.linksdcid trackitemsdcid, trackitem.linkkeyid1 trackitemkeyid1,");
        sql.append(" '' tooltip, storageunit.storageunittype, storageunit.arraylayoutzone");
        for (int i = 0; i < displaycolumns.size(); ++i) {
            PropertyList propertyList = displaycolumns.getPropertyList(i);
            String columnid = propertyList.getProperty("columnid");
            if (columnid.trim().length() <= 0) continue;
            columnid = StringUtil.replaceAll(columnid, "[%currentuser%]", sysuserid);
            columnid = StringUtil.replaceAll(columnid, "[currentuser]", sysuserid);
            columnid = StringUtil.replaceAll(columnid, "'[trackitemid]'", "(select trackitem.trackitemid from trackitem where trackitem.currentstorageunitid = storageunit.storageunitid)");
            sql.append(",").append(columnid);
        }
        sql.append(" from storageunit left outer join trackitem on trackitem.currentstorageunitid = storageunit.storageunitid");
        sql.append(" where storageunit.parentid = ?");
        sql.append(" order by storageunit.storageunitindex");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            ds.addColumn("status", 0);
            for (int i = 0; i < ds.size(); ++i) {
                int maxtiallowed = ds.getInt(i, "maxtiallowed", 0);
                if (maxtiallowed != 1) {
                    ds.setValue(i, "status", STATUS_DISABLED);
                    if (maxtiallowed == 0) {
                        ds.setValue(i, "tooltip", translationProcessor.translate("This position does not allow filing any item"));
                    } else {
                        ds.setValue(i, "tooltip", translationProcessor.translate("This position allows filing more than one item"));
                    }
                    ds.setValue(i, "storageunitlabel", ds.getValue(i, "storageunittype"));
                } else {
                    String storageid = ds.getString(i, "storageunitid");
                    String trackitemid = ds.getString(i, "trackitemid");
                    if (trackitemid == null || trackitemid.trim().length() == 0) {
                        int reserveRow = reserveDS.findRow("storageunitid", storageid);
                        if (reserveRow != -1) {
                            for (int col = 0; col < reserveDS.getColumnCount(); ++col) {
                                String value;
                                String columnid = reserveDS.getColumnId(col);
                                if (!ds.isValidColumn(columnid) || (value = reserveDS.getValue(reserveRow, columnid, "")).length() <= 0) continue;
                                ds.setValue(i, columnid, value);
                            }
                            ds.setValue(i, "status", STATUS_RESERVED);
                        } else {
                            ds.setValue(i, "status", STATUS_EMPTY);
                        }
                    } else {
                        ds.setValue(i, "status", STATUS_FILLED);
                    }
                }
                data.copyRow(ds, i, 1);
            }
        }
        return data;
    }

    public static DataSet getReservedStorageUnits(QueryProcessor queryProcessor, String storageunitid, PropertyListCollection displaycolumns, String sysuserid) {
        HashMap reserveMap = new HashMap();
        StringBuilder sql = new StringBuilder();
        sql.append("select reservestorageunit.storageunitid, reservestorageunit.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1");
        for (int i = 0; i < displaycolumns.size(); ++i) {
            PropertyList propertyList = displaycolumns.getPropertyList(i);
            String columnid = propertyList.getProperty("columnid");
            if (columnid.trim().length() <= 0 || !columnid.contains("(")) continue;
            columnid = StringUtil.replaceAll(columnid, "[%currentuser%]", sysuserid);
            columnid = StringUtil.replaceAll(columnid, "[currentuser]", sysuserid);
            columnid = StringUtil.replaceAll(columnid, "'[trackitemid]'", "reservestorageunit.trackitemid");
            sql.append(",").append(columnid);
        }
        sql.append(" from reservestorageunit, trackitem");
        sql.append(" where reservestorageunit.trackitemid = trackitem.trackitemid");
        sql.append(" and reservestorageunit.storageunitid in ( select storageunit.storageunitid from storageunit where storageunit.parentid = ?)");
        return queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
    }

    private static String setRendererProps(PropertyList element, PropertyList storageunitprops, TranslationProcessor translationProcessor) throws Exception {
        String id = element.getId();
        StringBuilder sb = new StringBuilder();
        PropertyList labelGenRule = storageunitprops.getPropertyListNotNull("labelgenrule");
        String useIndex = labelGenRule.getProperty("useindex", NO);
        if (element.getProperty("property_layout").equalsIgnoreCase(LAYOUT_GRID)) {
            String orientation = storageunitprops.getProperty("orientation", ORIENTATION_ROWMAJOR);
            if (ORIENTATION_COLUMNMAJOR.equals(orientation)) {
                sb.append(NEWLINE).append(id).append(".setColumnMajor();");
            }
            PropertyList indexOrder = storageunitprops.getPropertyListNotNull("indexorder");
            String horizontalIndexOrder = indexOrder.getProperty("horizontal", "Left->Right");
            String verticalIndexOrder = indexOrder.getProperty("vertical", "Top->Bottom");
            String startAtFirst = indexOrder.getProperty("startatfirst", YES);
            String cols = storageunitprops.getProperty("columns", "5");
            String rows = storageunitprops.getProperty("rows", "5");
            int colCount = Integer.parseInt(cols);
            String fillOrderHorizontal = "Left->Right".equals(horizontalIndexOrder) ? "LR" : "RL";
            String fillOrderVertical = "Top->Bottom".equals(verticalIndexOrder) ? "TB" : "BT";
            sb.append(NEWLINE).append(id).append(".setRows( \"").append(rows).append("\" );");
            sb.append(NEWLINE).append(id).append(".setCols( \"").append(cols).append("\" );");
            sb.append(NEWLINE).append(id).append(".setFillOrder( \"H\", \"").append(fillOrderHorizontal).append("\" );");
            sb.append(NEWLINE).append(id).append(".setFillOrder( \"V\", \"").append(fillOrderVertical).append("\" );");
            sb.append(NEWLINE).append(id).append(".setStartAtFirst( \"").append(startAtFirst).append("\" );");
            if (NO.equalsIgnoreCase(useIndex)) {
                String[] s;
                PropertyList horizontalLabelProps = labelGenRule.getPropertyListNotNull("horizontallabelgenrule");
                Label horizonalLabelGenerator = new Label(horizontalLabelProps);
                String tempHorizontalLabels = horizonalLabelGenerator.getLabels(Integer.parseInt(cols));
                if ("RL".equals(fillOrderHorizontal)) {
                    tempHorizontalLabels = OpalUtil.reverse(tempHorizontalLabels, ";");
                }
                StringBuilder translatedLabels = new StringBuilder();
                for (String label : s = StringUtil.split(tempHorizontalLabels, ";")) {
                    translatedLabels.append(";").append(StringUtil.getLen(label) > 0L ? translationProcessor.translate(label) : "");
                }
                sb.append(NEWLINE).append(id).append(".setHorizontalLabel( \"").append(translatedLabels.substring(1)).append("\" );");
                PropertyList verticalLabelProps = labelGenRule.getPropertyListNotNull("verticallabelgenrule");
                Label verticalLabelGenerator = new Label(verticalLabelProps);
                String tempVerticalLabels = verticalLabelGenerator.getLabels(Integer.parseInt(element.getProperty("property_storageunitsize")) / colCount);
                if ("BT".equals(fillOrderVertical)) {
                    tempVerticalLabels = OpalUtil.reverse(tempVerticalLabels, ";");
                }
                translatedLabels.setLength(0);
                for (String label : s = StringUtil.split(tempVerticalLabels, ";")) {
                    translatedLabels.append(";").append(StringUtil.getLen(label) > 0L ? translationProcessor.translate(label) : "");
                }
                sb.append(NEWLINE).append(id).append(".setVerticalLabel( \"").append(translatedLabels.substring(1)).append("\" );");
            }
        } else if (element.getProperty("property_layout").equalsIgnoreCase("Circular")) {
            String indexOrder = storageunitprops.getProperty("indexorder", "Clockwise");
            sb.append(NEWLINE).append(id).append(".setFillOrder( \"H\", \"").append("Clockwise".equals(indexOrder) ? "LR" : "RL").append("\" );");
            sb.append(NEWLINE).append(id).append(".setLayout( \"Circular\" );");
            if (NO.equalsIgnoreCase(useIndex)) {
                Label label = new Label(labelGenRule);
                String l = label.getIndices(Integer.parseInt(element.getProperty("property_storageunitsize")));
                sb.append(NEWLINE).append(id).append(".setHorizontalLabel( \"").append(l).append("\" );");
                sb.append(NEWLINE).append(id).append(".setVerticalLabel( \"").append(l).append("\" );");
            }
        } else if (element.getProperty("property_layout").equalsIgnoreCase(LAYOUT_LINEAR)) {
            String fillOrder;
            String indexOrder;
            String orientation = storageunitprops.getProperty("orientation", ORIENTATION_HORIZONTAL);
            sb.append(NEWLINE).append(id).append(".setLayout( \"Linear\" );");
            if (orientation.equals(ORIENTATION_HORIZONTAL)) {
                indexOrder = storageunitprops.getProperty("indexorder", "Left->Right");
                fillOrder = "Left->Right".equals(indexOrder) ? "LR" : "RL";
                sb.append(NEWLINE).append(id).append(".setFillOrder( \"H\", \"").append(fillOrder).append("\" );");
            } else {
                indexOrder = storageunitprops.getProperty("indexorder", "Top->Bottom");
                fillOrder = "Top->Bottom".equals(indexOrder) ? "TB" : "BT";
                sb.append(NEWLINE).append(id).append(".setFillOrder( \"H\", \"").append(fillOrder).append("\" );");
            }
            if (NO.equalsIgnoreCase(useIndex)) {
                String[] s;
                Label label = new Label(labelGenRule);
                String l = label.getLabels(Integer.parseInt(element.getProperty("property_storageunitsize")));
                if (fillOrder.equals("BT") || fillOrder.equals("RL")) {
                    l = OpalUtil.reverse(l, ";");
                }
                StringBuilder translatedLabels = new StringBuilder();
                for (String _label : s = StringUtil.split(l, ";")) {
                    translatedLabels.append(";").append(StringUtil.getLen(_label) > 0L ? translationProcessor.translate(_label) : "");
                }
                sb.append(NEWLINE).append(id).append(".setHorizontalLabel( \"").append(translatedLabels.substring(1)).append("\" );");
            }
        }
        sb.append(NEWLINE).append(id).append(".setSize( \"").append(Integer.parseInt(element.getProperty("property_storageunitsize"))).append("\" );");
        sb.append(NEWLINE).append(id).append(".setConsolidationMode( '").append("Consolidate".equals(element.getProperty("mode")) ? YES : NO).append("' );");
        sb.append(NEWLINE).append("var __su_headercolor = '").append(element.getPropertyListNotNull("header").getProperty("backcolor", DEFAULTCOLOR_HEADER_BACKGROUND)).append("';");
        sb.append(NEWLINE).append("var __su_layout = '").append(element.getProperty("property_layout")).append("';");
        return sb.toString();
    }

    private static String renderOptions(PropertyList element, PropertyList storageunitprops, TranslationProcessor translationProcessor) {
        String id = element.getId();
        StringBuilder sb = new StringBuilder();
        sb.append("<div id='").append(id).append("_optionsContainer' style='display:none;padding:6px;'>");
        sb.append("<fieldset style='border-radius:4px;'><legend>").append(translationProcessor.translate("Options")).append("</legend>");
        sb.append("<div style='padding:5px;display:flex;'>");
        if (element.getProperty("property_layout").equals(LAYOUT_GRID)) {
            PropertyList indexOrder = storageunitprops.getPropertyListNotNull("indexorder");
            String orientation = storageunitprops.getProperty("orientation", ORIENTATION_ROWMAJOR);
            String horOrder = indexOrder.getProperty("horizontal");
            String verOrder = indexOrder.getProperty("vertical");
            sb.append("<fieldset><legend><b>").append(translationProcessor.translate("Fill direction")).append("</b></legend>");
            sb.append("<fieldset><legend>").append(translationProcessor.translate(ORIENTATION_HORIZONTAL)).append("</legend>");
            sb.append("<table cellpadding=3 cellspacing=0 border=0><tr><td>");
            sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"LR\")'");
            sb.append("Left->Right".equals(horOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/MoveRight.gif'>");
            sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"RL\")'");
            sb.append("Right->Left".equals(horOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/MoveLeft.gif'>");
            sb.append("</td><td>");
            sb.append("<div style='padding-left:10px;text-align:right' id='").append(id).append("_fillorder_hor'>");
            sb.append("Left->Right".equals(horOrder) ? translationProcessor.translate("Left to Right") : translationProcessor.translate("Right to Left")).append("</div>");
            sb.append("</td></tr></table>");
            sb.append("</fieldset>");
            sb.append("<fieldset><legend>").append(translationProcessor.translate(ORIENTATION_VERTICAL)).append("</legend>");
            sb.append("<table cellpadding=3 cellspacing=0 border=0><tr><td>");
            sb.append("<input type=radio name=fillver onclick='su_setFillOrder(\"").append(id).append("\", \"V\", \"TB\")'");
            sb.append("Top->Bottom".equals(verOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/MoveDown.gif'>");
            sb.append("<input type=radio name=fillver onclick='su_setFillOrder(\"").append(id).append("\", \"V\", \"BT\")'");
            sb.append("Bottom->Top".equals(verOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/MoveUp.gif'>");
            sb.append("</td><td>");
            sb.append("<div style='padding-left:10px;text-align:right' id='").append(id).append("_fillorder_ver'>");
            sb.append("Top->Bottom".equals(verOrder) ? translationProcessor.translate("Top to Bottom") : translationProcessor.translate("Bottom to Top")).append("</div>");
            sb.append("</td></tr></table>");
            sb.append("</fieldset></fieldset>");
            sb.append("<fieldset><legend><b>").append(translationProcessor.translate("Orientation")).append("</b></legend>");
            sb.append("<input type=radio name=rdorientation onclick='su_setOrientation( \"").append(id).append("\", 0 )' ");
            sb.append(ORIENTATION_ROWMAJOR.equals(orientation) ? "CHECKED" : "").append(">&nbsp;").append(translationProcessor.translate(ORIENTATION_ROWMAJOR));
            sb.append("<br><input type=radio name=rdorientation onclick='su_setOrientation( \"").append(id).append("\", 1 )' ");
            sb.append(ORIENTATION_COLUMNMAJOR.equals(orientation) ? "CHECKED" : "").append(">&nbsp;").append(translationProcessor.translate(ORIENTATION_COLUMNMAJOR));
            sb.append("</fieldset>");
        } else if (element.getProperty("property_layout").equals(LAYOUT_LINEAR)) {
            String orientation = storageunitprops.getProperty("orientation", ORIENTATION_HORIZONTAL);
            String indexOrder = storageunitprops.getProperty("indexorder");
            sb.append("<fieldset><legend><b>Fill direction</b></legend>");
            sb.append("<table cellpadding=3 cellspacing=0 border=0><tr><td>");
            if (ORIENTATION_HORIZONTAL.equals(orientation)) {
                sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"LR\")'");
                sb.append("Left->Right".equals(indexOrder) ? " CHECKED>" : ">");
                sb.append("<img src='WEB-CORE/images/gif/MoveRight.gif'>");
                sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"RL\")'");
                sb.append("Right->Left".equals(indexOrder) ? " CHECKED>" : ">");
                sb.append("<img src='WEB-CORE/images/gif/MoveLeft.gif'>");
                sb.append("</td><td>");
                sb.append("<div style='padding-left:10px;text-align:right' id='").append(id).append("_fillorder_hor'>");
                sb.append(indexOrder).append("</div>");
            } else {
                sb.append("<input type=radio name=fillver onclick='su_setFillOrder(\"").append(id).append("\", \"V\", \"TB\")'");
                sb.append("Top->Bottom".equals(indexOrder) ? " CHECKED>" : ">");
                sb.append("<img src='WEB-CORE/images/gif/MoveDown.gif'>");
                sb.append("<input type=radio name=fillver onclick='su_setFillOrder(\"").append(id).append("\", \"V\", \"BT\")'");
                sb.append("Bottom->Top".equals(indexOrder) ? " CHECKED>" : ">");
                sb.append("<img src='WEB-CORE/images/gif/MoveUp.gif'>");
                sb.append("</td><td>");
                sb.append("<div style='padding-left:10px;text-align:right' id='").append(id).append("_fillorder_ver'>");
                sb.append(indexOrder).append("</div>");
            }
            sb.append("</td></tr></table>");
            sb.append("</fieldset>");
        } else if (element.getProperty("property_layout").equals("Circular")) {
            String indexOrder = storageunitprops.getProperty("indexorder", "Clockwise");
            sb.append("<fieldset><legend><b>Fill direction</b></legend>");
            sb.append("<table cellpadding=3 cellspacing=0 border=0><tr><td>");
            sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"LR\")'");
            sb.append("Clockwise".equals(indexOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/Clockwise.gif'>");
            sb.append("<input type=radio name=fillhor onclick='su_setFillOrder(\"").append(id).append("\", \"H\", \"RL\")'");
            sb.append("Anti-Clockwise".equals(indexOrder) ? " CHECKED>" : ">");
            sb.append("<img src='WEB-CORE/images/gif/AntiClockwise.gif'>");
            sb.append("</td><td>");
            sb.append("<div style='padding-left:10px;text-align:right' id='").append(id).append("_fillorder_hor'>");
            sb.append(indexOrder).append("</div>");
            sb.append("</td></tr></table>");
            sb.append("</fieldset>");
        }
        sb.append("</div>");
        sb.append("</fieldset></div>");
        return sb.toString();
    }

    private static String renderLegends(PropertyList element, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        String storageunitid;
        DataSet ds;
        boolean editMode = "Edit".equals(element.getProperty("mode", "Edit"));
        PropertyList cellColors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors");
        StringBuilder sb = new StringBuilder();
        sb.append("<div id='").append(element.getId()).append("_legendContainer' style='display:none;padding:10px;'>");
        sb.append("<fieldset style='display:flex;border-radius:4px;'><legend>").append(translationProcessor.translate("Legend")).append("</legend>");
        sb.append("<fieldset style='border:1px solid lightgray;'><legend style='color:gray;'>").append(translationProcessor.translate("Cell Status Colors")).append("</legend>");
        sb.append("<div style='display:flex;flex-wrap:wrap'>");
        sb.append("<table>");
        sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(cellColors.getProperty(STATUS_EMPTY, DEFAULTCOLOR_CELL_EMPTY)).append("'>&nbsp;</td>");
        sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate("Empty")).append("</td></tr>");
        sb.append("</table>");
        sb.append("<table>");
        sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(cellColors.getProperty(STATUS_FILLED, DEFAULTCOLOR_CELL_FILLED)).append("'>&nbsp;</td>");
        sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate("Filled")).append("</td></tr>\n");
        sb.append("</table>");
        sb.append("<table>");
        sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(cellColors.getProperty(STATUS_RESERVED, DEFAULTCOLOR_CELL_RESERVED)).append("'>&nbsp;</td>");
        sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate("Reserved")).append("</td></tr>\n");
        sb.append("</table>");
        if (editMode) {
            sb.append("<table>");
            sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(cellColors.getProperty("start", DEFAULTCOLOR_CELL_START)).append("'>&nbsp;</td>");
            sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate("Start")).append("</td></tr>\n");
            sb.append("</table>");
        }
        sb.append("<table>");
        sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(cellColors.getProperty(STATUS_DISABLED, DEFAULTCOLOR_CELL_DISABLED)).append("'>&nbsp;</td>");
        sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate("Disabled")).append("</td></tr>\n");
        sb.append("</table></div>");
        sb.append("</fieldset>");
        PropertyListCollection sampleTypeColors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors").getCollectionNotNull("bysampletype");
        if (sampleTypeColors.size() > 0) {
            sb.append("<fieldset style='border:1px solid lightgray;'><legend style='color:gray;'>").append(translationProcessor.translate("Sample Type Colors")).append("</legend>");
            sb.append("<div style='display:flex;flex-wrap:wrap'>");
            for (int i = 0; i < sampleTypeColors.size(); ++i) {
                PropertyList color = sampleTypeColors.getPropertyList(i);
                String sampletypeid = color.getProperty("sampletypeid");
                String sampletypecolor = color.getProperty("color");
                if (!OpalUtil.isNotEmpty(sampletypeid) || !OpalUtil.isNotEmpty(sampletypecolor)) continue;
                sb.append("<table>");
                sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:").append(sampletypecolor).append("'>&nbsp;</td>");
                sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate(sampletypeid)).append("</td></tr>\n");
                sb.append("</table>");
            }
            sb.append("</div>");
            sb.append("</fieldset>");
        }
        if ((ds = queryProcessor.getPreparedSqlDataSet("select distinct arraylayoutzone, arraylayoutzonecolor color from storageunit where parentid = ? order by arraylayoutzone", (Object[])new String[]{storageunitid = element.getProperty("property_storageunitid")})).size() == 0) {
            ds = queryProcessor.getPreparedSqlDataSet("select arraylayoutzone.arraylayoutzone, arraylayoutzone.color from arraylayoutzone, storageunit where arraylayoutzone.arraylayoutid = storageunit.arraylayoutid and arraylayoutzone.arraylayoutversionid = '1' and storageunit.storageunitid = ? and arraylayoutzone.arraylayoutzone != '(FullArray)' order by arraylayoutzone.usersequence", (Object[])new String[]{storageunitid});
        }
        if (ds != null && ds.size() > 0) {
            sb.append("<fieldset style='border:1px solid lightgray;'><legend style='color:gray;'>").append(translationProcessor.translate("Loading Zone Colors")).append("</legend>");
            sb.append("<div style='display:flex;flex-wrap:wrap'>");
            for (int i = 0; i < ds.size(); ++i) {
                String zone = ds.getString(i, "arraylayoutzone", "");
                String zoneClass = "Z" + OpalUtil.getUniqueID();
                element.setProperty("zone_" + zone, zoneClass);
                String zonecolor = ds.getString(i, "color", "#FFF");
                sb.append("<table>");
                sb.append("<tr><td style='width:20px;height:20px;border:1px solid lightgray;background:#FFF;' class='").append(zoneClass).append("'>&nbsp;</td>");
                sb.append("<td style='font:normal 12 sans-serif'>&nbsp;").append(translationProcessor.translate(ds.getString(i, "arraylayoutzone"))).append("</td></tr>\n");
                sb.append("</table>");
                if (zone.length() <= 0) continue;
                sb.append("<style>");
                sb.append(".").append(zoneClass).append("{");
                sb.append("-webkit-box-shadow: inset 0px 0px 10px 2px ").append(zonecolor).append(";").append("-moz-box-shadow: inset 0px 0px 10px 2px ").append(zonecolor).append(";").append("box-shadow: inset 0px 0px 10px 2px ").append(zonecolor).append(";");
                sb.append("}");
                sb.append("</style>");
            }
            sb.append("</div></fieldset>");
        }
        sb.append("</fieldset>");
        sb.append("</div>");
        return sb.toString();
    }

    public static String getStyles(PropertyList element, QueryProcessor queryProcessor) {
        String fontSize = OpalUtil.getColumnValue(queryProcessor, "profileproperty", "propertyvalue", "sysuserid=? and propertyid='userconfig_renderer_font_size'", new String[]{element.getProperty("property_sysuserid")});
        PropertyList colors = element.getPropertyListNotNull("cell").getPropertyListNotNull("colors");
        String cellWidth = element.getPropertyListNotNull("cell").getProperty("width", "25");
        String cellHeight = element.getPropertyListNotNull("cell").getProperty("height", "25");
        StringBuilder sb = new StringBuilder();
        sb.append("<style type=\"text/css\">\n");
        sb.append(" fieldset { border:1px solid #aaa;margin-top:4px;margin-left:4px;margin-right:4px;background:white; }");
        sb.append("    img.buttonFontResize {width:12px;height:12px;cursor:pointer;border:1px solid #ccc;padding:2px;background:white;}");
        if (YES.equals(element.getProperty("property_isRTL"))) {
            sb.append("    img.buttonFontLeft {border-top-right-radius:10px;border-bottom-right-radius:10px;padding-right:6px;}");
            sb.append("    img.buttonFontRight {border-top-left-radius:10px;border-bottom-left-radius:10px;padding-left:6px;}");
        } else {
            sb.append("    img.buttonFontLeft {border-top-left-radius:6px;border-bottom-left-radius:6px;padding-left:6px;}");
            sb.append("    img.buttonFontRight {border-top-right-radius:6px;border-bottom-right-radius:6px;padding-right:6px;}");
        }
        sb.append("    img.buttonFontResize:hover {background:#ccc;}");
        sb.append("    .nolayoutcellcontainer {font-size:").append(OpalUtil.isEmpty(fontSize) ? "12" : fontSize).append("px;}\n");
        sb.append("    td.cell {\n");
        sb.append("        border:1px solid ").append(colors.getProperty("border", DEFAULTCOLOR_CELL_BORDER)).append(";\n");
        sb.append("        background: ").append(DEFAULTCOLOR_CELL_EMPTY).append(";\n");
        sb.append("        cursor: default;\n");
        sb.append("        text-align: center;\n");
        sb.append("        padding: 2px;\n");
        sb.append("        position: relative;\n");
        sb.append("        white-space: nowrap;\n");
        sb.append("        color: #111;\n");
        sb.append("        width:").append(cellWidth).append("px;\n");
        sb.append("        height:").append(cellHeight).append("px;\n");
        sb.append("        font-size: ").append(OpalUtil.isEmpty(fontSize) ? "12" : fontSize).append("px;\n");
        sb.append("    }\n");
        sb.append("    td.cell_header {\n");
        sb.append("        border:1px solid ").append(colors.getProperty("border", DEFAULTCOLOR_CELL_BORDER)).append(";\n");
        sb.append("        background:").append(element.getPropertyListNotNull("header").getProperty("backcolor", DEFAULTCOLOR_HEADER_BACKGROUND)).append(";\n");
        sb.append("        color:").append(element.getPropertyListNotNull("header").getProperty("fontcolor", DEFAULTCOLOR_HEADER_FONT)).append(";\n");
        sb.append("        font-family:sans-serif;\n");
        sb.append("        font-size:10;\n");
        sb.append("        text-align:center;\n");
        sb.append("        min-width:20px;\n");
        sb.append("        width:").append(cellWidth).append("px;\n");
        sb.append("        height:").append(cellHeight).append("px;\n");
        if ("Consolidate".equals(element.getProperty("mode"))) {
            sb.append("        cursor:pointer;\n");
        }
        sb.append("    }\n");
        sb.append("    div.infodiv {\n");
        sb.append("        border:1px solid ").append(DEFAULTCOLOR_HOVERDIV_BORDER).append(";\n");
        sb.append("        position:absolute;\n");
        sb.append("        z-index:999;\n");
        sb.append("        background:").append(DEFAULTCOLOR_HOVERDIV_BACKGROUND).append(";\n");
        sb.append("    }\n");
        sb.append("    td.infosdc {\n");
        sb.append("        font:normal 10 sans-serif;\n");
        sb.append("        text-align:right;\n");
        sb.append("    }\n");
        sb.append("    td.infokey {\n");
        sb.append("        font:normal 10 sans-serif;\n");
        sb.append("    }");
        sb.append("    div.progressbar-small {");
        sb.append("        position: relative;");
        sb.append("        display: inline-block;");
        sb.append("        width: 120px;");
        sb.append("        height: 14px;");
        sb.append("        border: 1px solid grey;");
        sb.append("        background: #fff;");
        sb.append("        vertical-align: bottom;");
        sb.append("    }");
        sb.append("    div.progressbar-small div.bar {");
        sb.append("        text-align: center;");
        sb.append("        width: 0;");
        sb.append("        background: #B1CADD;");
        sb.append("        height: 100%;");
        sb.append("    }");
        sb.append("    div.progressbar-small div.text {");
        sb.append("        position: absolute;");
        sb.append("        top: 0;");
        sb.append("        left: 0;");
        sb.append("        height: 100%;");
        sb.append("        width: 100%;");
        sb.append("        text-align: center;");
        sb.append("        background: transparent;");
        sb.append("        color: #333;");
        sb.append("        font-size: 0.9em;");
        sb.append("        font-weight: normal;");
        sb.append("        text-wrap: none;");
        sb.append("    }");
        sb.append("    div.progressbar-unlimited {");
        sb.append("        position: relative;");
        sb.append("        display: inline-block;");
        sb.append("        width: 120px;");
        sb.append("        height: 14px;");
        sb.append("        border: 1px dashed grey;");
        sb.append("        background: #fff;");
        sb.append("        vertical-align: bottom;");
        sb.append("    }");
        sb.append("    div.progressbar-unlimited div.text {");
        sb.append("        position: absolute;");
        sb.append("        top: 0;");
        sb.append("        left: 0;");
        sb.append("        height: 100%;");
        sb.append("        width: 100%;");
        sb.append("        text-align: center;");
        sb.append("        background: transparent;");
        sb.append("        color: #333;");
        sb.append("        font-size: 0.9em;");
        sb.append("        font-weight: normal;");
        sb.append("        text-wrap: none;");
        sb.append("    }");
        sb.append("    div.ohovertitle {");
        sb.append("        position:absolute;");
        sb.append("        z-index:9999;");
        sb.append("        background:#efefef;");
        sb.append("    }");
        sb.append("    div.celldrag {background:").append(colors.getProperty(STATUS_FILLED, DEFAULTCOLOR_CELL_FILLED)).append(";}");
        sb.append("</style>\n");
        return sb.toString();
    }
}

