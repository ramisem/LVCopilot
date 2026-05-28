/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MapHelper {
    public static final String COLUMN_PROJECTION = "projection";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    static final String PROPERTY_COLUMNID = "columnid";
    private ConfigurationProcessor configurationProcessor;
    private ConnectionProcessor connectionProcessor;
    private PropertyList policyProps = null;
    private PageContext pageContext;
    private PropertyList extraColumnsConf = null;

    public MapHelper(PageContext pageContext) {
        this.pageContext = pageContext;
        this.connectionProcessor = new ConnectionProcessor(pageContext);
        this.configurationProcessor = new ConfigurationProcessor(pageContext);
        try {
            this.policyProps = this.configurationProcessor.getPolicy("MapPolicy", "Sapphire Custom");
        }
        catch (SapphireException e) {
            this.policyProps = new PropertyList();
        }
    }

    public boolean checkMapAllowed(String sdcid) {
        if (sdcid != null && sdcid.length() > 0) {
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            boolean allowed = sdcProcessor.getProperty(sdcid, "coordinatableflag").startsWith("Y");
            return allowed;
        }
        return false;
    }

    public boolean checkCoordinateColumns(String sdcid, PropertyListCollection columns, boolean onlyCheck) {
        boolean coordinateSupported = this.checkMapAllowed(sdcid);
        boolean columnsFound = false;
        boolean projectionFound = false;
        boolean latitudeFound = false;
        boolean longitudeFound = false;
        for (int index = 0; index < columns.size(); ++index) {
            String columnname = RequestParser.parseAlias(columns.getPropertyList(index).getProperty(PROPERTY_COLUMNID, ""));
            if (columnname.length() <= 0) continue;
            if (columnname.equals(COLUMN_LATITUDE)) {
                latitudeFound = true;
                continue;
            }
            if (columnname.equals(COLUMN_LONGITUDE)) {
                longitudeFound = true;
                continue;
            }
            if (!columnname.equals(COLUMN_PROJECTION)) continue;
            projectionFound = true;
        }
        if (latitudeFound && longitudeFound && projectionFound) {
            columnsFound = true;
        }
        if (coordinateSupported && !columnsFound) {
            if (!latitudeFound && !onlyCheck) {
                PropertyList column = new PropertyList();
                column.setId(COLUMN_LATITUDE);
                column.setProperty(PROPERTY_COLUMNID, COLUMN_LATITUDE);
                column.setProperty("mode", "hidden");
                columns.add(column);
            }
            if (!longitudeFound && !onlyCheck) {
                PropertyList column = new PropertyList();
                column.setId(COLUMN_LONGITUDE);
                column.setProperty(PROPERTY_COLUMNID, COLUMN_LONGITUDE);
                column.setProperty("mode", "hidden");
                columns.add(column);
            }
            if (!projectionFound && !onlyCheck) {
                PropertyList column = new PropertyList();
                column.setId(COLUMN_PROJECTION);
                column.setProperty(PROPERTY_COLUMNID, COLUMN_PROJECTION);
                column.setProperty("mode", "hidden");
                columns.add(column);
            }
            columnsFound = true;
        }
        return columnsFound;
    }

    public String getHtml() {
        return this.getHtml(new PropertyListCollection(), false);
    }

    public String getHtml(boolean isPopup) {
        return this.getHtml(new PropertyListCollection(), isPopup);
    }

    public String getHtml(PropertyListCollection iconStyles) {
        return this.getHtml(iconStyles, false);
    }

    public void setExtraColumnConf(String extraColumnConfId) {
        PropertyListCollection configs = this.policyProps.getCollectionNotNull("extracolumns");
        boolean foundMatch = false;
        for (int i = 0; i < configs.size(); ++i) {
            PropertyList config = configs.getPropertyList(i);
            String id = config.getProperty("id", "");
            if (!id.toLowerCase().equals(extraColumnConfId.toLowerCase())) continue;
            this.extraColumnsConf = config;
            foundMatch = true;
            break;
        }
    }

    public String setExtraColumns(SDITagInfo sdiInfo, String extraColumnConfId, PropertyList element) {
        String[] primaryKeyCols = null;
        String linkkeyId1Col = "";
        String linkkeyId2Col = "";
        String linkkeyId3Col = "";
        PropertyListCollection configs = this.policyProps.getCollectionNotNull("extracolumns");
        PropertyList columnsConf = new PropertyList();
        boolean foundMatch = false;
        for (int i = 0; i < configs.size(); ++i) {
            PropertyList config = configs.getPropertyList(i);
            String id = config.getProperty("id", "");
            if (!id.toLowerCase().equals(extraColumnConfId.toLowerCase())) continue;
            columnsConf = config;
            foundMatch = true;
            break;
        }
        if (!foundMatch) {
            return "";
        }
        String toSDCId = columnsConf.getProperty("sdcid");
        String errorMsg = "";
        String linkId = columnsConf.getProperty("linkid");
        String fromSDCId = columnsConf.getProperty("primarysdcid");
        String fkWhereClause = columnsConf.getProperty("fkwhere", "");
        PropertyListCollection extraColumns = columnsConf.getCollectionNotNull("columns");
        String orderby = columnsConf.getProperty("orderby", "");
        HashMap revLinkProps = null;
        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
        PropertyList sdcProps = sdcProcessor.getPropertyList(fromSDCId);
        if (sdcProps != null) {
            PropertyListCollection reverseLinklinks = sdcProps.getCollection("reverselinks");
            if (reverseLinklinks != null && reverseLinklinks.getPropertyList(linkId + ";" + toSDCId) != null) {
                revLinkProps = reverseLinklinks.getPropertyList(linkId + ";" + toSDCId);
            } else {
                errorMsg = "Could not obtain reverse link details for SDC Id " + toSDCId + " (1).";
            }
        } else {
            errorMsg = "Could not obtain SDC details for SDC Id " + fromSDCId + " (1).";
        }
        if (revLinkProps != null && revLinkProps.size() > 0) {
            String linksdcid = ((PropertyList)revLinkProps).getProperty("linksdcid");
            if ((sdiInfo.getSdcid() == null || sdiInfo.getSdcid().equalsIgnoreCase(fromSDCId)) && linksdcid.equalsIgnoreCase(fromSDCId)) {
                PropertyList toSDCProps = sdcProcessor.getPropertyList(toSDCId);
                PropertyList linkProps = null;
                if (toSDCProps != null) {
                    PropertyListCollection links = toSDCProps.getCollection("links");
                    if (links != null && links.getPropertyList(linkId) != null) {
                        linkProps = links.getPropertyList(linkId);
                    } else {
                        errorMsg = "Could not obtain link details for SDC Id " + toSDCId + " (1).";
                    }
                } else {
                    errorMsg = "Could not obtain SDC details for SDC Id " + toSDCProps + " (2).";
                }
                if (toSDCProps != null && linkProps != null) {
                    DataSet primary;
                    StringBuffer colsToSelect = new StringBuffer();
                    String orgcolslist = this.getColumnsList(extraColumns, toSDCProps, colsToSelect, false);
                    PropertyListCollection columns = element.getCollectionNotNull("columns");
                    if (extraColumns != null && extraColumns.size() > 0) {
                        for (int c = 0; c < extraColumns.size(); ++c) {
                            PropertyList extraColumnProps = extraColumns.getPropertyList(c);
                            columns.add(extraColumnProps);
                        }
                    }
                    String colslist = "";
                    if (orgcolslist.length() > 0) {
                        colslist = colslist + colsToSelect + "";
                    }
                    if ((primary = sdiInfo.getDataSet("primary")) != null) {
                        primaryKeyCols = new String[Integer.parseInt(sdcProps.getProperty("keycolumns"))];
                        for (int index = 0; index < primaryKeyCols.length; ++index) {
                            primaryKeyCols[index] = sdcProps.getProperty("keycolid" + (index + 1));
                        }
                        linkkeyId1Col = linkProps.getProperty("sdccolumnid", "");
                        linkkeyId2Col = linkProps.getProperty("sdccolumnid2", "");
                        linkkeyId3Col = linkProps.getProperty("sdccolumnid3", "");
                        String tableid = toSDCProps.getProperty("tableid");
                        boolean columnsAdded = false;
                        for (int i = 0; i < primary.getRowCount(); ++i) {
                            try {
                                int columnType;
                                String columnid;
                                int j;
                                SDIRequest sdireq = new SDIRequest();
                                sdireq.setSDCid(toSDCId);
                                sdireq.setRequestItem("primary[" + colslist + "]");
                                sdireq.setRetainRsetid(false);
                                sdireq.setQueryFrom(tableid + "");
                                StringBuffer querywhere = new StringBuffer();
                                querywhere.append("").append(tableid).append(".").append(linkkeyId1Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(i, primaryKeyCols[0]), this.connectionProcessor.isOra())).append("'");
                                if (linkkeyId2Col != null && linkkeyId2Col.length() > 0 && primaryKeyCols.length > 1) {
                                    querywhere.append(" AND ").append(tableid).append(".").append(linkkeyId2Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(i, primaryKeyCols[1]), this.connectionProcessor.isOra())).append("'");
                                    if (linkkeyId3Col != null && linkkeyId3Col.length() > 0 && primaryKeyCols.length > 2) {
                                        querywhere.append(" AND ").append(tableid).append(".").append(linkkeyId3Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(i, primaryKeyCols[2]), this.connectionProcessor.isOra())).append("'");
                                    }
                                }
                                if (fkWhereClause.length() > 0) {
                                    querywhere.append(" AND ( ").append(fkWhereClause).append(" ) ");
                                }
                                sdireq.setQueryOrderBy(orderby);
                                sdireq.setRetrieveLimit(1);
                                sdireq.setUseRSetOrderBy(true);
                                sdireq.setQueryWhere(querywhere.toString());
                                SDIProcessor sdiproc = new SDIProcessor(this.pageContext);
                                SDIData fksdidata = sdiproc.getSDIData(sdireq);
                                if (fksdidata == null || fksdidata.getDataset("primary") == null) continue;
                                DataSet data = fksdidata.getDataset("primary");
                                if (!columnsAdded) {
                                    for (j = 0; j < data.getColumnCount(); ++j) {
                                        columnid = data.getColumnId(j);
                                        columnType = data.getColumnType(columnid);
                                        if (primary.isValidColumn(columnid)) continue;
                                        primary.addColumn(columnid, columnType);
                                    }
                                    columnsAdded = true;
                                }
                                if (data.getRowCount() <= 0) continue;
                                for (j = 0; j < data.getColumnCount(); ++j) {
                                    columnid = data.getColumnId(j);
                                    columnType = data.getColumnType(columnid);
                                    if (columnType == 0) {
                                        primary.setString(i, columnid, data.getString(0, columnid));
                                        continue;
                                    }
                                    if (columnType == 1) {
                                        primary.setNumber(i, columnid, data.getBigDecimal(0, columnid));
                                        continue;
                                    }
                                    if (columnType != 2) continue;
                                    primary.setDate(i, columnid, data.getCalendar(0, columnid));
                                }
                                continue;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (sdiInfo.getKeycols() == null) {
                            sdiInfo.setKeycols(primaryKeyCols);
                        }
                    }
                } else {
                    errorMsg = "Could not obtain link details for Link Id " + linkId + "(2).";
                }
            } else {
                errorMsg = "Link specified does not originate from primary SDC or primary SDC specified for element does not match the primary SDC.";
            }
        } else {
            errorMsg = "Could not obtain reverse link details for Link Id " + linkId + "(1).";
        }
        return "<font style=\"color:red;\">" + errorMsg + "</font>";
    }

    private String getColumnsList(PropertyListCollection columns, PropertyList sdcOrLinkProps, StringBuffer colsToSelect, boolean appendTableName) {
        StringBuffer out = new StringBuffer();
        PropertyListCollection sdccolumns = sdcOrLinkProps.containsKey("columns") ? sdcOrLinkProps.getCollection("columns") : sdcOrLinkProps.getCollection("linkcolumns");
        if (sdccolumns != null && columns.size() > 0) {
            String colid = null;
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                if (colid == null) {
                    if (column.containsKey(PROPERTY_COLUMNID)) {
                        colid = PROPERTY_COLUMNID;
                        if (column.getProperty(colid, "").length() != 0) continue;
                        columns.remove(i);
                        --i;
                        continue;
                    }
                    if (column.containsKey("linkcolumnid")) {
                        colid = "linkcolumnid";
                        if (column.getProperty(colid, "").length() != 0) continue;
                        columns.remove(i);
                        --i;
                        continue;
                    }
                    columns.remove(i);
                    --i;
                    continue;
                }
                if (column.containsKey(colid) && column.getProperty(colid, "").length() != 0) continue;
                columns.remove(i);
                --i;
            }
            if (colid == null) {
                colid = "linkcolumnid";
            }
            boolean keycol1found = false;
            boolean keycol2found = false;
            boolean keycol3found = false;
            String keycolid1 = sdcOrLinkProps.containsKey("keycolid1") ? sdcOrLinkProps.getProperty("keycolid1") : sdcOrLinkProps.getProperty("keycolid1");
            String keycolid2 = sdcOrLinkProps.containsKey("keycolid2") ? sdcOrLinkProps.getProperty("keycolid2") : sdcOrLinkProps.getProperty("keycolid2");
            String keycolid3 = sdcOrLinkProps.containsKey("keycolid3") ? sdcOrLinkProps.getProperty("keycolid3") : sdcOrLinkProps.getProperty("keycolid3");
            String tableName = sdcOrLinkProps.getProperty("tableid");
            for (int index = 0; index < columns.size(); ++index) {
                String columnname = columns.getPropertyList(index).getProperty(PROPERTY_COLUMNID, "");
                if (columnname.length() <= 0) continue;
                if (sdccolumns.find(colid, columnname) != null) {
                    if (columnname.equalsIgnoreCase(keycolid1)) {
                        keycol1found = true;
                    }
                    if (keycolid2.length() > 0 && columnname.equalsIgnoreCase(keycolid2)) {
                        keycol2found = true;
                    }
                    if (keycolid3.length() > 0 && columnname.equalsIgnoreCase(keycolid3)) {
                        keycol3found = true;
                    }
                    if (out.length() == 0) {
                        out.append(columnname);
                        colsToSelect.append(appendTableName ? tableName : "").append(appendTableName ? "." : "").append(columnname);
                        continue;
                    }
                    out.append(appendTableName ? ", " : ",").append(columnname);
                    colsToSelect.append(appendTableName ? ", " : ",").append(appendTableName ? tableName : "").append(appendTableName ? "." : "").append(columnname);
                    continue;
                }
                if (out.length() == 0) {
                    out.append(columnname);
                    colsToSelect.append(columnname);
                    continue;
                }
                out.append(appendTableName ? ", " : ",").append(columnname);
                colsToSelect.append(appendTableName ? ", " : ",").append(columnname);
            }
            if (!keycol1found) {
                if (out.length() == 0) {
                    out.append(keycolid1);
                    colsToSelect.append(tableName).append(".").append(keycolid1);
                } else {
                    out.append(", ").append(keycolid1);
                    colsToSelect.append(",").append(tableName).append(".").append(keycolid1);
                }
                PropertyList keycol = new PropertyList();
                keycol.setId("keycolid1");
                keycol.setProperty(PROPERTY_COLUMNID, keycolid1);
                keycol.setProperty("mode", "hidden");
                columns.add(keycol);
            }
            if (keycolid2.length() > 0 && !keycol2found) {
                if (out.length() == 0) {
                    out.append(keycolid2);
                    colsToSelect.append(tableName).append(".").append(keycolid2);
                } else {
                    out.append(", ").append(keycolid2);
                    colsToSelect.append(",").append(tableName).append(".").append(keycolid2);
                }
                PropertyList keycol = new PropertyList();
                keycol.setId("keycolid2");
                keycol.setProperty(PROPERTY_COLUMNID, keycolid2);
                keycol.setProperty("mode", "hidden");
                columns.add(keycol);
            }
            if (keycolid3.length() > 0 && !keycol3found) {
                if (out.length() == 0) {
                    out.append(keycolid3);
                    colsToSelect.append(tableName).append(".").append(keycolid3);
                } else {
                    out.append(", ").append(keycolid3);
                    colsToSelect.append(",").append(tableName).append(".").append(keycolid3);
                }
                PropertyList keycol = new PropertyList();
                keycol.setId("keycolid3");
                keycol.setProperty(PROPERTY_COLUMNID, keycolid3);
                keycol.setProperty("mode", "hidden");
                columns.add(keycol);
            }
        }
        if (out.length() > 0) {
            return out.toString();
        }
        return "";
    }

    public String getHtml(PropertyListCollection iconStyles, boolean isLookup) {
        StringBuilder html = new StringBuilder();
        PropertyList mapProps = null;
        PropertyList sources = this.policyProps.getPropertyListNotNull("sources");
        mapProps = this.policyProps.getPropertyListNotNull("mapprops");
        PropertyListCollection sourceColl = sources.getCollectionNotNull("sources");
        for (int i = 0; i < sourceColl.size(); ++i) {
            PropertyList src = sourceColl.getPropertyList(i);
            String type = src.getProperty("type", "");
            String id = src.getProperty("id");
            String sourceXmlStr = "";
            if (type.equals("wmts")) {
                String urlStr = src.getPropertyListNotNull("wmtsprops").getProperty("wmtsurl");
                sourceXmlStr = (String)CacheUtil.get(this.connectionProcessor.getSapphireConnection().getDatabaseId(), "MAPSOURCES_CACHE", urlStr);
                if (sourceXmlStr == null || sourceXmlStr.length() == 0) {
                    try {
                        if (!urlStr.isEmpty()) {
                            String inputLine;
                            URLConnection connection = new URL(urlStr).openConnection();
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            sourceXmlStr = response.toString();
                        }
                    }
                    catch (Exception e) {
                        sourceXmlStr = "";
                    }
                    PropertyListCollection replaceColl = src.getCollectionNotNull("replace");
                    for (int j = 0; j < replaceColl.size(); ++j) {
                        PropertyList replaceProps = replaceColl.getPropertyList(j);
                        String from = replaceProps.getProperty("from");
                        String to = replaceProps.getProperty("to");
                        sourceXmlStr = StringUtil.replaceAll(sourceXmlStr, from, to);
                    }
                    CacheUtil.put(this.connectionProcessor.getSapphireConnection().getDatabaseId(), "MAPSOURCES_CACHE", urlStr, sourceXmlStr);
                }
            }
            sourceXmlStr = sapphire.util.HttpUtil.encodeURIComponent(sourceXmlStr);
            html.append("<input type=\"hidden\" id=\"source_" + id + "\" value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(sourceXmlStr)).append("\"/>\n");
        }
        HashSet<String> activeStyles = new HashSet<String>();
        String defaultIconStyleCollectionId = "";
        for (int i = 0; i < iconStyles.size(); ++i) {
            PropertyList styleProp = iconStyles.getPropertyList(i);
            String styleId = styleProp.getProperty("styleid");
            boolean active = styleProp.getProperty("active", "Y").startsWith("Y");
            if (!active) continue;
            if (activeStyles.size() == 0) {
                defaultIconStyleCollectionId = styleId;
            }
            activeStyles.add(styleId);
        }
        PropertyListCollection allStyles = mapProps.getCollectionNotNull("iconpropscollection");
        for (int i = allStyles.size(); i > 0; --i) {
            int index = i - 1;
            PropertyList styleProp = allStyles.getPropertyList(index);
            String styleId = styleProp.getProperty("id");
            if (activeStyles.contains(styleId)) continue;
            allStyles.remove(index);
        }
        html.append("<input type=\"hidden\" id=\"mapprops\" value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(mapProps.toJSONString())).append("\"/>\n");
        html.append("<input type=\"hidden\" id=\"sourceprops\" value=\"").append(sapphire.util.HttpUtil.encodeURIComponent(sources.toJSONString())).append("\"/>\n");
        html.append("<link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/openlayers/ol.css\" type=\"text/css\">\n");
        html.append("<link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/openlayers/ol3-layerswitcher.css\"/>\n");
        html.append("<link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/openlayers/ol3-popup.css\"/>\n");
        html.append("<script src=\"WEB-CORE/extscripts/openlayers/ol-debug.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/openlayers/ol3-popup.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/openlayers/ol3-layerswitcher.js\"></script>\n");
        html.append(this.getProjectionHtml());
        html.append("<script>\n");
        html.append(" var defaultIconStyleCollection = '" + SafeHTML.encodeForJavaScript(defaultIconStyleCollectionId) + "';\n");
        html.append(" var isLookup = " + SafeHTML.encodeForJavaScript(isLookup ? "true" : "false") + ";\n");
        boolean canGeoLocate = this.canGeoLocate();
        html.append(" var canGeolocate = " + SafeHTML.encodeForJavaScript(canGeoLocate ? "true" : "false") + ";\n");
        html.append("</script>\n");
        html.append("<script src=\"WEB-CORE/pagetypes/list/scripts/map.js\"></script>\n");
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/pagetypes/list/style/map.css", this.pageContext) + "\"/>\n");
        html.append("<table cellspacing=0 cellpadding=0 width=100% height=100% border=0 style=\"margin-left:0px; margin-right:0px; margin-top:0px; margin-bottom:0px;\">\n");
        html.append("<tr>\n");
        html.append("<td valign=top>\n");
        html.append("<div id=\"selectors\"></div>\n");
        html.append("<div id=\"map\" class=\"map\" tabindex=\"0\">\n");
        html.append(" <div id=\"popup\"></div>\n");
        html.append("<div id=\"legend\">\n");
        html.append("<div id=\"inforow\"></div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</tr>\n");
        html.append("</td>\n");
        html.append("</table>\n");
        return html.toString();
    }

    public boolean canGeoLocate() {
        String geolocateMode = this.policyProps.getPropertyListNotNull("sources").getPropertyListNotNull("viewprops").getProperty("geolocatemode", "auto");
        boolean allowGeolocate = true;
        if (geolocateMode.equals("auto")) {
            boolean isSecure = this.pageContext.getRequest().isSecure();
            Browser browser = new Browser(this.pageContext);
            allowGeolocate = !browser.isChrome() || browser.isChrome() && isSecure || this.pageContext.getRequest().getLocalName().equals(this.pageContext.getRequest().getServerName());
        } else if (geolocateMode.equals("disable")) {
            allowGeolocate = false;
        }
        return allowGeolocate;
    }

    public String getProjectionHtml() {
        StringBuilder html = new StringBuilder();
        PropertyListCollection projections = this.policyProps.getCollectionNotNull("projections");
        PropertyList viewProps = this.policyProps.getPropertyListNotNull("sources").getPropertyListNotNull("viewprops");
        html.append("<script src=\"WEB-CORE/extscripts/openlayers/proj4-src.js\"></script>\n");
        html.append("<script type='text/javascript'>");
        String baseprojection = viewProps.getProperty("baseprojection", "EPSG:3857");
        String numberOfDecimals = "2";
        for (int i = 0; i < projections.size(); ++i) {
            PropertyList projection = projections.getPropertyList(i);
            String proj = projection.getProperty(COLUMN_PROJECTION);
            String type = projection.getProperty("type");
            String conversion = projection.getProperty("conversion");
            if (baseprojection.equals(proj)) {
                numberOfDecimals = projection.getProperty("numberofdecimals", "2");
            }
            if (type.equals("Alias")) {
                String alias = projection.getProperty("aliasof");
                if (alias.isEmpty()) continue;
                html.append("proj4.defs('" + SafeHTML.encodeForJavaScript(proj) + "', proj4.defs('" + SafeHTML.encodeForJavaScript(alias) + "'));\n");
                continue;
            }
            if (conversion.isEmpty()) continue;
            html.append("proj4.defs('" + SafeHTML.encodeForJavaScript(proj) + "','" + SafeHTML.encodeForJavaScript(conversion) + "');\n");
        }
        html.append(" var baseProjection = '" + SafeHTML.encodeForJavaScript(baseprojection) + "';\n");
        html.append(" var numberOfDecimals = '" + SafeHTML.encodeForJavaScript(numberOfDecimals) + "';\n");
        html.append("</script>\n");
        return html.toString();
    }
}

