/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import sapphire.accessor.SDIProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ImageMapGizmo
extends BaseGizmo {
    public static final String PROPERTY_MAP = "map";
    public static final String PROPERTY_SDCPROPS = "sdcprops";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_HOTSPOTINDICATOR = "hotspotindicator";
    public static final String PROPERTY_COORDINATES = "coordinates";
    public static final String PROPERTY_IMAGESOURCE = "imagesource";
    public static final String PROPERTY_IMAGESOURCE_IMAGE = "Image";
    public static final String PROPERTY_IMAGESOURCE_ATTACHMENT = "Attachment";
    public static final String PROPERTY_IMAGE_FILE = "filename";
    public static final String EXCURSION_SHAPE_CIRCLE = "circle";
    public static final String EXCURSION_SHAPE_SQUARE = "square";
    public static final String IMAGE = "Compass";

    @Override
    public boolean init() {
        this.setRefreshOnResize(false);
        this.setResizable(true);
        this.setTimeout(-1);
        this.setCount(this.evalCount());
        return true;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        boolean isLocationGizmo = false;
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            String indicatorcolor = this.element.getProperty("indicatorcolor", "white");
            if (this.element.getProperty("mouseoverhotspot", "Y").equals("Y")) {
                html.append("<style>.hotspot:hover {\n  opacity: 0.5;\n  background-color: " + indicatorcolor + ";\n}</style>");
            }
            html.append("<style>.hotspot_highlight {\n  background-color: " + indicatorcolor + ";\n}</style>");
            PropertyList parameters = this.element.getPropertyListNotNull("parameters");
            String keyid1Parameter = parameters.getProperty("keyid1parameterid");
            String keyid2Parameter = parameters.getProperty("keyid2parameterid");
            String keyid3Parameter = parameters.getProperty("keyid3parameterid");
            PropertyList imageProps = this.element.getPropertyListNotNull(PROPERTY_IMAGE);
            String sourcesdcid = imageProps.getProperty("sdcid");
            String sourcekeyid1 = this.cleanKeyidProperty(imageProps.getProperty("keyid1"));
            String sourcekeyid2 = this.cleanKeyidProperty(imageProps.getProperty("keyid2"));
            String sourcekeyid3 = this.cleanKeyidProperty(imageProps.getProperty("keyid3"));
            DataSet hotspotds = this.isImageMapPresent(sourcekeyid1);
            if (hotspotds.getClob(0, "hotspotpropertyclob") != null && OpalUtil.isNotEmpty(hotspotds.getClob(0, "hotspotpropertyclob"))) {
                isLocationGizmo = true;
            }
            String forcewidth = imageProps.getProperty("width");
            String forceheight = imageProps.getProperty("height");
            boolean forcesize = forcewidth.length() > 0 && forceheight.length() > 0;
            String style = "z-index:0;" + (forcesize ? "width:" + forcewidth + "px;height:" + forceheight + "px;" : "");
            html.append("<div style=\"width:500%;height:500%;overflow:scroll;position:absolute\">");
            if (imageProps.getProperty(PROPERTY_IMAGESOURCE).equals(PROPERTY_IMAGESOURCE_IMAGE)) {
                html.append("<img id=\"masterimage_" + this.getGizmoDefId() + "\" style=\"" + style + "\" src=\"" + imageProps.getProperty(PROPERTY_IMAGE) + "\">");
            } else if (imageProps.getProperty(PROPERTY_IMAGESOURCE).equals(PROPERTY_IMAGESOURCE_ATTACHMENT)) {
                String sdcid = imageProps.getProperty("sdcid");
                String attachmentclass = imageProps.getProperty("attachmentclass");
                html.append("<img id=\"masterimage_" + this.getGizmoDefId() + "\" style=\"" + style + "\" src=\"rc?command=image&attachment=" + sdcid + ";" + sourcekeyid1 + ";" + sourcekeyid2 + ";" + sourcekeyid3 + ";" + attachmentclass + "\">");
            }
            String defaultquerytype = this.element.getProperty("defaultquerytype");
            String defaultquerysdcid = this.element.getProperty("defaultquerysdcid");
            String defaultqueryfrom = this.element.getProperty("defaultqueryfrom");
            String defaultquerywhere = this.element.getProperty("defaultquerywhere");
            String defaultsql = this.element.getProperty("defaultsql");
            if (defaultquerytype.equals("sql")) {
                String fileNameSql = this.replaceSourceSDI(defaultsql, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                fileNameSql = this.replaceRequest(fileNameSql);
                DataSet dataSet = this.getQueryProcessor().getSqlDataSet(fileNameSql);
            }
            String defaultURL = this.element.getProperty("defaulturl");
            String defaultTarget = this.element.getProperty("defaulttarget");
            String defaultLabelStyle = this.element.getProperty("defaultlabelstyle", "padding:1px;background-color:beige");
            boolean showZeroCount = "Show".equals(this.element.getProperty("showzerocount"));
            boolean showZeroImage = "Show Image".equals(this.element.getProperty("showzerocount"));
            String zeroImage = this.element.getProperty("zeroimage");
            String hotspotColor = this.element.getProperty("hotspotcountcolor", "red");
            PropertyListCollection c = this.element.getCollectionNotNull(PROPERTY_MAP);
            for (int i = 0; i < c.size(); ++i) {
                int temp;
                PropertyList area = c.getPropertyList(i);
                String coords = area.getProperty(PROPERTY_COORDINATES);
                String querytype = area.getProperty("querytype", defaultquerytype);
                String querysdcid = area.getProperty("querysdcid", defaultquerysdcid);
                String queryfrom = area.getProperty("queryfrom", defaultqueryfrom);
                String querywhere = area.getProperty("querywhere", defaultquerywhere);
                String sql = area.getProperty("sql", defaultsql);
                String url = area.getProperty(PROPERTY_URL, defaultURL);
                String target = area.getProperty(PROPERTY_TARGET, defaultTarget);
                String linksdcid = area.getProperty("sdcid");
                String linkkeyid1 = this.cleanKeyidProperty(area.getProperty("keyid1"));
                String linkkeyid2 = this.cleanKeyidProperty(area.getProperty("keyid2"));
                String linkkeyid3 = this.cleanKeyidProperty(area.getProperty("keyid3"));
                if (isLocationGizmo) {
                    if (linksdcid.length() == 0) continue;
                    PropertyListCollection sdcprops = this.element.getCollectionNotNull(PROPERTY_SDCPROPS);
                    for (int j = 0; j < sdcprops.size(); ++j) {
                        PropertyList props = sdcprops.getPropertyList(j);
                        if (!props.getProperty("sdcid").equalsIgnoreCase(linksdcid)) continue;
                        sql = props.getProperty("sdccountersql");
                        querytype = props.getProperty("counterquerytype", defaultquerytype);
                        querysdcid = props.getProperty("sdcid", defaultquerysdcid);
                        queryfrom = props.getProperty("counterqueryfrom", defaultqueryfrom);
                        querywhere = props.getProperty("counterquerywhere", defaultquerywhere);
                        indicatorcolor = props.getProperty("hotspotcolor");
                    }
                }
                String label = area.getProperty("label");
                label = this.replaceLinkSDI(label, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                label = this.replaceSourceSDI(label, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                label = this.replaceRequest(label);
                String labelStyle = area.getProperty("labelstyle", defaultLabelStyle);
                String popupTip = area.getProperty("tip", label);
                String[] coordbits = StringUtil.split(coords, ",");
                if (coordbits.length != 4) continue;
                int left = Integer.parseInt(coordbits[0]);
                int top = Integer.parseInt(coordbits[1]);
                int right = Integer.parseInt(coordbits[2]);
                int bottom = Integer.parseInt(coordbits[3]);
                if (left > right) {
                    temp = right;
                    right = left;
                    left = temp;
                }
                if (top > bottom) {
                    temp = bottom;
                    bottom = top;
                    top = temp;
                }
                int width = right - left;
                int height = bottom - top;
                String onclick = "";
                if (url.length() > 0) {
                    String imageMapGizmo;
                    url = this.replaceLinkSDI(url, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                    url = this.replaceSourceSDI(url, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                    url = this.replaceRequest(url);
                    DataSet ds = this.isImageMapPresent(linkkeyid1);
                    boolean isGizmoDefPresent = false;
                    boolean isHotspotPropPresent = false;
                    String hotspotProps = ds.getClob(0, "hotspotpropertyclob");
                    if (hotspotProps != null && OpalUtil.isNotEmpty(hotspotProps)) {
                        isHotspotPropPresent = true;
                    }
                    if ((imageMapGizmo = ds.getString(0, "gizmodefid")) != null && OpalUtil.isNotEmpty(imageMapGizmo)) {
                        isGizmoDefPresent = true;
                    }
                    if (this.getGizmoDefId() != null && this.getGizmoDefId().equalsIgnoreCase("Location") && !isGizmoDefPresent) {
                        url = isHotspotPropPresent ? "rc?command=gizmo&gizmo=Location&location=" + linkkeyid1 : "rc?command=page&page=" + linksdcid + "Maint&sdcid=" + linksdcid + "&keyid1=" + linkkeyid1;
                    }
                    onclick = target.length() == 0 ? onclick + "sapphire.page.navigate( '" + url + "' )" : onclick + "sapphire.lookup.open( '" + url + "', '" + target + "', 'newtab=Y' )";
                }
                if (keyid1Parameter.length() > 0 && linkkeyid1.length() > 0) {
                    onclick = onclick + ";" + this.elementid + "hotspotClicked( event, '" + linkkeyid1 + "',  '" + linkkeyid2 + "',  '" + linkkeyid3 + "' )";
                }
                html.append("<div name=\"hotspot_" + this.elementid + "\" class=\"hotspot\" onclick=\"" + onclick + "\" title=\"" + popupTip + "\" style=\"z-index:1;position:absolute;left:" + left + "px;top:" + top + "px;width:" + width + "px;height:" + height + "px\" ").append(isLocationGizmo ? "onmouseover=\"$(this).css('background','" + indicatorcolor + "');\" onmouseout=\"$(this).css('background','none');" : "").append("\">");
                int count = -1;
                String color = null;
                String shape = null;
                if (querytype.equals("sdirequest")) {
                    querywhere = this.replaceLinkSDI(querywhere, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                    querywhere = this.replaceSourceSDI(querywhere, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                    querywhere = this.replaceRequest(querywhere);
                    count = ImageMapGizmo.getSDICount(this.getSDIProcessor(), querysdcid, null, null, queryfrom, querywhere);
                } else if (querytype.equals("sql")) {
                    sql = this.replaceLinkSDI(sql, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                    sql = this.replaceSourceSDI(sql, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                    sql = this.replaceRequest(sql);
                    DataSet dp = this.getQueryProcessor().getSqlDataSet(sql);
                    if (dp != null && dp.getRowCount() > 0) {
                        count = Integer.parseInt(dp.getValue(0, "excursioncount", "0"));
                        color = dp.getValue(0, "color", "red");
                        shape = dp.getValue(0, "shape", EXCURSION_SHAPE_CIRCLE);
                    }
                }
                if (count == 0 && showZeroImage) {
                    Image image = new Image(this.pageContext);
                    image.setImageSrc(zeroImage);
                    image.setTitle(popupTip);
                    image.setStyle("position:absolute;left:" + (width / 2 - 10) + "px;top:" + (height / 2 - 10) + "px");
                    html.append(image.getHtml());
                } else if (count > 0 || showZeroCount) {
                    String borderRadius;
                    String string = borderRadius = shape.equalsIgnoreCase(EXCURSION_SHAPE_SQUARE) ? "0px" : "8px";
                    if (hotspotColor.startsWith("$G{") && hotspotColor.endsWith("}")) {
                        color = this.getHotspotColor(hotspotColor, count);
                    }
                    html.append("<span title=\"" + popupTip + "\" class=\"ws_notify ").append(showZeroCount || count > 0 ? "ws_count_on" : "ws_count_off").append("\" style=\"background-color:" + color + ";position:absolute;left:" + (width / 2 - 10) + "px;top:" + (height / 2 - 10) + "px;border-radius:").append(borderRadius).append("!important").append("\">").append("" + count).append("</span>");
                }
                html.append("</div>");
                if (label.length() <= 0) continue;
                html.append("<div name=\"hotspot_" + this.elementid + "\" class=\"hotspot_label\" style=\"z-index:1;text-align:center;position:absolute;left:" + left + "px;top:" + bottom + "px;" + labelStyle).append("\">" + label + "</div>");
            }
            PropertyList hotspot = this.element.getPropertyListNotNull(PROPERTY_HOTSPOTINDICATOR);
            if (hotspot.getProperty("show").equals("Y")) {
                String image = hotspot.getProperty(PROPERTY_IMAGE, "rc?command=image&image=ComponentRed");
                int size = 32;
                try {
                    size = Integer.parseInt(hotspot.getProperty("size", "32"));
                }
                catch (Exception querytype) {
                    // empty catch block
                }
                String postype = hotspot.getProperty("positiontype", "custom");
                String positionLeft = hotspot.getProperty("positionleft", "5");
                String positionTop = hotspot.getProperty("positiontop", "5");
                String left = "";
                String top = "";
                if (postype.equals("Custom")) {
                    left = "e.style.left ='" + positionLeft + "px';";
                } else if (postype.contains("Left")) {
                    left = "e.style.left = '5px';";
                } else if (postype.contains("Center")) {
                    left = "e.style.left = ((i.clientWidth/2) -" + size / 2 + ") + 'px';";
                } else if (postype.contains("Right")) {
                    left = "e.style.left = (i.clientWidth -" + (size + 5) + ") + 'px';";
                }
                if (postype.equals("Custom")) {
                    top = "e.style.top ='" + positionTop + "px';";
                } else if (postype.contains("Top")) {
                    top = "e.style.top = '5px';";
                } else if (postype.contains("Middle")) {
                    top = "e.style.top = ((i.clientHeight/2) -" + size / 2 + ") + 'px';";
                } else if (postype.contains("Bottom")) {
                    top = "e.style.top = (i.clientHeight -" + (size + 5) + ") + 'px';";
                }
                html.append("<span id=\"hotspotindicator_" + this.getGizmoDefId() + "\" onclick=\"toggleHotspots( '" + this.elementid + "' )\" style=\"position:absolute;display:none;z-index:9\"><img src=\"" + image + "&size=" + size + "\"</span>");
                html.append("<script>");
                String fn = StringUtil.replaceAll(this.elementid + "hotspotMove", " ", "_");
                html.append("function ").append(fn).append("(){");
                html.append("var e = document.getElementById( 'hotspotindicator_" + this.getGizmoDefId() + "' );");
                html.append("var i = document.getElementById( 'masterimage_" + this.getGizmoDefId() + "' );");
                html.append("if( e && i ) {");
                html.append("e.style.display='block';");
                html.append(left);
                html.append(top);
                html.append("}");
                html.append("}");
                html.append("sapphire.events.attachEvent( window, 'onload', " + fn + " );");
                html.append("</script>");
            }
            html.append("</div>");
            if (keyid1Parameter.length() > 0) {
                StringBuffer script = new StringBuffer();
                PropertyListCollection col = parameters.getCollection("refresh");
                script.append("function ").append(this.elementid).append("hotspotClicked(event, keyid1, keydi2, keyid3){");
                script.append("try{");
                script.append("dashboard.updateParameter( '" + keyid1Parameter + "', keyid1 );");
                script.append(keyid2Parameter.length() > 0 ? "dashoard.updateParameter( '" + keyid2Parameter + "', keyid2 );" : "");
                script.append(keyid3Parameter.length() > 0 ? "dashoard.updateParameter( '" + keyid3Parameter + "', keyid3 );" : "");
                script.append("}catch(de){sapphire.alert('Unable to update parameter');}");
                script.append("try{");
                for (int i = 0; i < col.size(); ++i) {
                    String giz = col.getPropertyList(i).getProperty("gizmoid", "");
                    if (giz.length() <= 0) continue;
                    script.append("dashboard.refreshGizmo( dashboard.currentTab, '").append(giz).append("', false );");
                }
                script.append("}catch(e){}");
                script.append("}");
                html.append("<script>").append(script).append("</script>");
            }
        }
        return html.toString();
    }

    private String addLocation(String url, String linkkeyid1) {
        StringBuffer text = new StringBuffer(url);
        text.append("&location=").append(linkkeyid1);
        return text.toString();
    }

    private String cleanKeyidProperty(String keyid) {
        if (keyid.contains("|")) {
            keyid = keyid.substring(0, keyid.indexOf("|"));
        }
        return keyid;
    }

    private String replaceRequest(String text) {
        String[] tokens = StringUtil.getTokens(text);
        for (int i = 0; i < tokens.length; ++i) {
            if (this.request.getParameter(tokens[i]) == null || this.request.getParameter(tokens[i]).length() <= 0) continue;
            text = StringUtil.replaceAll(text, "[" + tokens[i] + "]", this.request.getParameter(tokens[i]));
        }
        return text;
    }

    private String replaceLinkSDI(String text, String linksdcid, String linkkeyid1, String linkkeyid2, String linkkeyid3) {
        text = StringUtil.replaceAll(text, "[keyid1]", linkkeyid1);
        text = StringUtil.replaceAll(text, "[keyid2]", linkkeyid2);
        text = StringUtil.replaceAll(text, "[keyid3]", linkkeyid3);
        text = this.replaceKeyid1Column(text, "keyid1", linksdcid, linkkeyid1);
        text = StringUtil.replaceAll(text, "[linkkeyid1]", linkkeyid1);
        text = StringUtil.replaceAll(text, "[linkkeyid2]", linkkeyid2);
        text = StringUtil.replaceAll(text, "[linkkeyid3]", linkkeyid3);
        text = this.replaceKeyid1Column(text, "linkkeyid1", linksdcid, linkkeyid1);
        return text;
    }

    private String replaceSourceSDI(String text, String sourcesdcid, String sourcekeyid1, String sourcekeyid2, String sourcekeyid3) {
        text = StringUtil.replaceAll(text, "[attachmentkeyid1]", sourcekeyid1);
        text = StringUtil.replaceAll(text, "[attachmentkeyid2]", sourcekeyid2);
        text = StringUtil.replaceAll(text, "[attachmentkeyid3]", sourcekeyid3);
        text = this.replaceKeyid1Column(text, "attachmentkeyid1", sourcesdcid, sourcekeyid1);
        text = StringUtil.replaceAll(text, "[sourcekeyid1]", sourcekeyid1);
        text = StringUtil.replaceAll(text, "[sourcekeyid2]", sourcekeyid2);
        text = StringUtil.replaceAll(text, "[sourcekeyid3]", sourcekeyid3);
        text = this.replaceKeyid1Column(text, "sourcekeyid1", sourcesdcid, sourcekeyid1);
        return text;
    }

    private String replaceKeyid1Column(String text, String token, String sdcid, String keyid1) {
        try {
            String[] tokens = StringUtil.getTokens(text);
            for (int i = 0; i < tokens.length; ++i) {
                if (!tokens[i].startsWith(token + ".")) continue;
                String columnid = tokens[i].substring(tokens[i].indexOf(".") + 1);
                SDIRequest request = new SDIRequest();
                request.setSDCid(sdcid);
                request.setKeyid1List(keyid1);
                request.setRequestItem("primary");
                SDIData sdidata = this.getSDIProcessor().getSDIData(request);
                DataSet primary = sdidata.getDataset("primary");
                String value = primary.getValue(0, columnid);
                if (value.length() <= 0) continue;
                text = StringUtil.replaceAll(text, "[" + tokens[i] + "]", value);
            }
        }
        catch (Exception e) {
            this.logger.info("Failed to do column substitution in gizmo " + this.getGizmoDefId() + ": " + e.getMessage());
        }
        return text;
    }

    @Override
    public String getScript() {
        StringBuffer script = new StringBuffer();
        script.append("function toggleHotspots( elementid ) {").append("var ee = document.getElementsByName( 'hotspot_' + elementid );").append("if ( ee.length > 0 ) {").append("  for ( var i=0; i < ee.length; i++ ) {").append("   ee[i].className = ( ee[i].className=='hotspot_highlight'? 'hotspot': ee[i].className=='hotspot'? 'hotspot_highlight': ee[i].className );").append("   ee[i].className = ( ee[i].className=='hotspot_label_highlight'? 'hotspot_label': ee[i].className=='hotspot_label'? 'hotspot_label_highlight': ee[i].className );").append("  }").append("}").append("}");
        return script.toString();
    }

    @Override
    public String getImageSrc() {
        return "FlatBlackPicture";
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("groupgizmo", this.getTitle()), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getIconHtml() {
        if (this.element.getProperty("mode", "menu").equalsIgnoreCase("button")) {
            StringBuffer html = new StringBuffer();
            BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
            if (gizmoStyle.showImage) {
                html.append("<span id=\"e_").append(this.elementid).append("\" onclick=\"").append("modernLayout.setGroup('").append(this.element.getProperty("groupgizmo", "")).append("')\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : "").append(">");
                html.append(this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("groupgizmo", this.getTitle()), gizmoStyle.size).getHtml());
                html.append("</span>");
            }
            if (gizmoStyle.showTitle) {
                html.append("<span onclick=\"").append("modernLayout.setGroup('").append(this.element.getProperty("groupgizmo", "")).append("')\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : "").append(">");
                html.append(this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Group Picker");
                html.append("</span>");
            }
            return html.toString();
        }
        return super.getIconHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return IMAGE;
    }

    public String getSmallPreview() {
        return this.getSmall(48);
    }

    private String getSmall(int size) {
        Image im;
        if (this.pageContext == null) {
            im = new Image();
            im.setConnectionId(this.getConnectionId());
        } else {
            im = new Image(this.pageContext);
        }
        im.setImageId(this.element.getProperty("smallicon", "Map"));
        im.setDimensions(size, size);
        im.setTitle(this.element.getProperty("title"));
        return im.getHtml();
    }

    public String getSmallIcon() {
        return this.getSmall(16);
    }

    @Override
    public int evalCount() {
        PropertyList imageProps = this.element.getPropertyListNotNull(PROPERTY_IMAGE);
        String sourcesdcid = imageProps.getProperty("sdcid");
        String sourcekeyid1 = imageProps.getProperty("keyid1");
        String sourcekeyid2 = imageProps.getProperty("keyid2");
        String sourcekeyid3 = imageProps.getProperty("keyid3");
        PropertyList count = this.element.getPropertyListNotNull("count");
        String querytype = count.getProperty("querytype");
        if (querytype.equals("sdirequest")) {
            PropertyList sdirequest = count.getPropertyList("sdirequest").copy();
            PropertyListCollection params = sdirequest.getCollectionNotNull("params");
            for (int i = 0; i < params.size(); ++i) {
                PropertyList p = params.getPropertyList(i);
                String value = p.getProperty("paramvalue");
                value = this.replaceSourceSDI(value, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                value = this.replaceRequest(value);
                p.setProperty("paramvalue", value);
            }
            String querywhere = sdirequest.getProperty("querywhere");
            querywhere = this.replaceSourceSDI(querywhere, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
            querywhere = this.replaceRequest(querywhere);
            return ImageMapGizmo.getSDICount(this.getSDIProcessor(), sdirequest.getProperty("sdcid"), sdirequest.getProperty("queryid"), params, sdirequest.getProperty("queryfrom"), querywhere);
        }
        if (querytype.equals("sql")) {
            String sql = count.getPropertyList("sql").getProperty("sql");
            if (sql.length() > 0) {
                sql = this.replaceSourceSDI(sql, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3);
                sql = this.replaceRequest(sql);
                DataSet countdataset = this.getQueryProcessor().getSqlDataSet(StringUtil.replaceAll(StringUtil.replaceAll(sql, "[%currentuser%]", this.connectionInfo.getSysuserId()), "[currentuser]", this.connectionInfo.getSysuserId()));
                if (countdataset == null || countdataset.size() != 1) {
                    this.logger.error("Count SQL must return a single row with a single column called 'count'");
                }
                return countdataset != null && countdataset.size() == 1 ? countdataset.getInt(0, countdataset.getColumnId(0)) : -1;
            }
            return -1;
        }
        return -1;
    }

    @Override
    public String getURL() {
        return "rc?command=gizmo&gizmo=" + this.getGizmoDefId();
    }

    protected String getHotspotColor(String expression, int count) {
        if (expression.startsWith("$G{") && expression.endsWith("}")) {
            try {
                HashMap<String, Object> bindingMap = GroovyUtil.getCommonBindingMap(this.connectionInfo, null, this.element, null, null);
                HashMap gizmo = new HashMap();
                HashMap<String, Integer> hotspot = new HashMap<String, Integer>();
                hotspot.put("count", count);
                gizmo.put("hotspot", hotspot);
                bindingMap.put("gizmo", gizmo);
                return GroovyUtil.getInstance(this.pageContext).evaluateSecure(expression.substring(3, expression.length() - "}".length()), bindingMap);
            }
            catch (Exception e) {
                this.logger.error("Failed to evaluate groovy expression '" + expression + "'. Reason: " + e.getMessage(), e);
                return expression;
            }
        }
        return expression;
    }

    private DataSet isImageMapPresent(String locationId) {
        DataSet out = null;
        String sessionid = this.connectionInfo.getDatabaseId() + ";" + this.connectionInfo.getSysuserId() + ";";
        String sessionidref = sessionid + this.getConnectionId();
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefAll", sessionid);
        SDIRequest sdiReq = new SDIRequest();
        sdiReq.setSDCid("Location");
        sdiReq.setQueryFrom("s_location");
        sdiReq.setQueryWhere("S_LOCATIONID= '" + locationId + "'");
        sdiReq.setRequestItem("primary");
        sdiReq.setExtendedDataTypes(true);
        SDIData sdi = new SDIProcessor(this.getConnectionId()).getSDIData(sdiReq);
        if (sdi != null) {
            out = sdi.getDataset("primary");
        }
        return out;
    }
}

