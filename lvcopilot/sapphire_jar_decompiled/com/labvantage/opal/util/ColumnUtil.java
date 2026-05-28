/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ColumnUtil {
    public static String getColumnHtml(PropertyList column, QueryProcessor queryProcessor, SDIProcessor sdiProcessor) {
        String fieldid = "colfield_" + column.getProperty("column");
        return ColumnUtil.getColumnHtml(column, fieldid, "", queryProcessor, sdiProcessor);
    }

    public static String getColumnHtml(PropertyList column, String fieldid, String attributes, QueryProcessor queryProcessor, SDIProcessor sdiProcessor) {
        StringBuilder sb = new StringBuilder();
        String mode = column.getProperty("mode", "input");
        if (column.getProperty("column", "").endsWith(".alias")) {
            sb.append(ColumnUtil.getAliasInputHtml(column, fieldid, attributes, queryProcessor));
        } else if ("readonly".equals(mode)) {
            String columnwidth = column.getProperty("width", "120");
            if (!columnwidth.endsWith("%")) {
                columnwidth = columnwidth + "px";
            }
            sb.append("<input readonly");
            sb.append(" ").append(attributes);
            sb.append(" style='width:").append(columnwidth).append("'");
            sb.append(" id='").append(fieldid).append("'>");
        } else if ("input".equals(mode)) {
            sb.append(ColumnUtil.getInputHtml(column, fieldid, attributes));
        } else if ("dropdownlist".equals(mode)) {
            sb.append(ColumnUtil.getDropDownListHtml(column, fieldid, attributes, queryProcessor, sdiProcessor));
        } else if ("datelookup".equals(mode)) {
            String dateFormat = column.getProperty("format", "").trim();
            if (dateFormat.length() == 0) {
                try {
                    dateFormat = new ConfigurationProcessor(queryProcessor.getConnectionid()).getPolicy("DateFormatPolicy", "Sapphire Custom").getProperty("defaultdateformat", "");
                    column.setProperty("format", dateFormat);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            }
            sb.append(ColumnUtil.getDateLookupHtml(column, fieldid, attributes));
        } else if ("lookup".equals(mode)) {
            sb.append(ColumnUtil.getLookupHtml(column, fieldid, attributes));
        } else if ("checkbox".equals(mode)) {
            sb.append(ColumnUtil.getCheckBoxHtml(column, fieldid, attributes));
        }
        return sb.toString();
    }

    private static String getAliasInputHtml(PropertyList column, String fieldid, String attributes, QueryProcessor queryProcessor) {
        StringBuilder sb = new StringBuilder();
        String aliastype = column.getProperty("aliastype", "");
        String aliasreftype = column.getProperty("aliasreftype", "");
        StringBuilder s = new StringBuilder();
        if (OpalUtil.isEmpty(aliastype) && OpalUtil.isEmpty(aliasreftype)) {
            sb.append("<span style='color:red'>");
            sb.append("Invalid Configuration. Either Alias Type or Alias Reference Type must be provided");
            sb.append("</span>");
        } else {
            if (OpalUtil.isNotEmpty(aliastype)) {
                attributes = attributes + " aliastype=\"" + aliastype + "\"";
            } else {
                s.append("<select id=\"").append(fieldid).append("_aliastype\">");
                s.append("<option value=\"\">-- Alias Type --</option>");
                DataSet ds = queryProcessor.getRefTypeDataSet(aliasreftype);
                if (ds != null) {
                    for (int i = 0; i < ds.size(); ++i) {
                        String refvalueid = ds.getString(i, "refvalueid");
                        String refdisplayvalue = ds.getString(i, "refdisplayvalue", refvalueid);
                        s.append("<option \"").append(refvalueid).append("\">").append(refdisplayvalue).append("</option>");
                    }
                }
                s.append("</select>");
            }
            if (s.length() > 0) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0><tr>");
                sb.append("<td>").append(ColumnUtil.getInputHtml(column, fieldid, attributes)).append("</td>");
                sb.append("<td>").append(s.toString()).append("</td></tr></table>");
            } else {
                sb.append(ColumnUtil.getInputHtml(column, fieldid, attributes));
            }
        }
        return sb.toString();
    }

    public static String getInputHtml(PropertyList column, String fieldid, String attributes) {
        return ColumnUtil.getInputHtml(column, fieldid, attributes, "");
    }

    public static String getInputHtml(PropertyList column, String fieldid, String attributes, String klass) {
        String columnwidth;
        String defaultvalue;
        String validation = column.getProperty("validation").trim();
        boolean mandatory = validation.contains("Mandatory") || "Y".equals(column.getProperty("mandatory"));
        String string = defaultvalue = attributes.contains("value=") ? "" : column.getProperty("default");
        if (defaultvalue.contains("\"")) {
            defaultvalue = StringUtil.replaceAll(defaultvalue, "\"", "&quot;");
        }
        if (!(columnwidth = column.getProperty("width", "120")).endsWith("%")) {
            columnwidth = columnwidth + "px";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<input");
        sb.append(" ").append(attributes);
        sb.append(OpalUtil.isNotEmpty(defaultvalue) ? " value=\"" + defaultvalue + "\"" : "");
        if (OpalUtil.isNotEmpty(validation)) {
            sb.append(" fieldval=\"").append(validation).append("\"");
        }
        klass = klass + " input_field" + (mandatory ? " mandatoryfield" : "");
        sb.append(" class='").append(klass).append("'");
        sb.append(" ttl=\"").append(StringUtil.replaceAll(column.getProperty("title", column.getProperty("columnid")), "\"", "&quot;")).append("\"");
        sb.append(" style='width:").append(columnwidth).append("'");
        sb.append(" id='").append(fieldid).append("'>");
        return sb.toString();
    }

    public static String getCheckBoxHtml(PropertyList column, String fieldid, String attributes) {
        return ColumnUtil.getCheckBoxHtml(column, fieldid, attributes, "");
    }

    public static String getCheckBoxHtml(PropertyList column, String fieldid, String attributes, String klass) {
        String validation = column.getProperty("validation").trim();
        boolean mandatory = validation.contains("Mandatory") || "Y".equals(column.getProperty("mandatory"));
        StringBuilder sb = new StringBuilder();
        sb.append("<input");
        sb.append(" ").append(attributes);
        if (OpalUtil.isNotEmpty(validation)) {
            sb.append(" fieldval=\"").append(validation).append("\"");
        }
        klass = klass + " input_field" + (mandatory ? " mandatoryfield" : "");
        sb.append(" class='").append(klass).append("'");
        sb.append(" type=\"checkbox\"");
        sb.append(" id='").append(fieldid).append("'>");
        return sb.toString();
    }

    public static String getDropDownListHtml(PropertyList column, String fieldid, String attributes, QueryProcessor queryProcessor, SDIProcessor sdiProcessor) {
        return ColumnUtil.getDropDownListHtml(column, fieldid, attributes, queryProcessor, sdiProcessor, "");
    }

    public static String getDropDownListHtml(PropertyList column, String fieldid, String attributes, QueryProcessor queryProcessor, SDIProcessor sdiProcessor, String klass) {
        String columnwidth;
        String defaultvalue;
        String validation = column.getProperty("validation").trim();
        boolean mandatory = validation.contains("Mandatory") || "Y".equals(column.getProperty("mandatory"));
        String columnValue = "";
        try {
            if (attributes.contains("value=")) {
                int startindex = attributes.indexOf("value=\"");
                int endindex = attributes.indexOf("\" ");
                columnValue = attributes.substring(startindex + 7, endindex);
            }
        }
        catch (Exception e) {
            columnValue = "";
        }
        String string = defaultvalue = attributes.contains("value=") ? columnValue : column.getProperty("default");
        if (defaultvalue.contains("\"")) {
            defaultvalue = StringUtil.replaceAll(defaultvalue, "\"", "&quot;");
        }
        if (!(columnwidth = column.getProperty("width", "120")).endsWith("%")) {
            columnwidth = columnwidth + "px";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<select");
        sb.append(" ").append(attributes);
        if (OpalUtil.isNotEmpty(validation)) {
            sb.append(" fieldval=\"").append(validation).append("\"");
        }
        klass = klass + " input_field" + (mandatory ? " mandatoryfield" : "");
        sb.append(" class='").append(klass).append("'");
        sb.append(" ttl=\"").append(StringUtil.replaceAll(column.getProperty("title", column.getProperty("columnid")), "\"", "&quot;")).append("\"");
        sb.append(" style='width:").append(columnwidth).append("' id='").append(fieldid).append("'>");
        sb.append("<option></option>");
        String linkreftypeid = column.getProperty("linkreftypeid");
        String dropdownsql = column.getProperty("dropdownsql");
        if (OpalUtil.isNotEmpty(linkreftypeid)) {
            DataSet ds = queryProcessor.getRefTypeDataSet(linkreftypeid);
            if (ds != null) {
                for (int row = 0; row < ds.size(); ++row) {
                    String value = ds.getValue(row, "refvalueid");
                    String displayvalue = ds.getValue(row, "refdisplayvalue", value);
                    sb.append("<option value='").append(value).append("'");
                    sb.append(value.equals(defaultvalue) ? " selected" : "");
                    sb.append(">").append(displayvalue).append("</option>");
                }
            }
        } else if (OpalUtil.isNotEmpty(dropdownsql)) {
            if (!dropdownsql.contains("[")) {
                DataSet ds = queryProcessor.getSqlDataSet(dropdownsql);
                if (ds != null) {
                    for (int row = 0; row < ds.size(); ++row) {
                        String valuecolumnid = ds.getColumnId(0);
                        String displayvaluecolumnid = ds.getColumnCount() > 1 ? ds.getColumnId(1) : valuecolumnid;
                        String value = ds.getValue(row, valuecolumnid);
                        String displayvalue = ds.getValue(row, displayvaluecolumnid, value);
                        sb.append("<option value='").append(value).append("'");
                        sb.append(value.equals(defaultvalue) ? " selected" : "");
                        sb.append(">").append(displayvalue).append("</option>");
                    }
                }
            } else {
                String[] tokens = StringUtil.getTokens(dropdownsql);
                sb.insert(0, "<input type=\"hidden\" sourcecolumnid=\"" + OpalUtil.toDelimitedString(Arrays.asList(tokens), ";") + "\" destcolumnid=\"" + column.getProperty("columnid") + "\" value=\"" + dropdownsql + "\">");
            }
        } else {
            PropertyList dropdowndefinition = column.getPropertyListNotNull("dropdowndefinition");
            String sdcid = dropdowndefinition.getProperty("sdcid");
            if (OpalUtil.isNotEmpty(sdcid)) {
                DataSet ds;
                String queryfrom = dropdowndefinition.getProperty("queryfrom");
                if (OpalUtil.isEmpty(queryfrom)) {
                    SDCProcessor sdcProcessor = new SDCProcessor(queryProcessor.getConnectionid());
                    queryfrom = sdcProcessor.getProperty(sdcid, "tableid");
                }
                String querywhere = dropdowndefinition.getProperty("querywhere");
                String queryorderby = dropdowndefinition.getProperty("queryorderby");
                String valuecolumn = dropdowndefinition.getProperty("valuecolumn");
                String displaycolumn = dropdowndefinition.getProperty("displaycolumn", valuecolumn);
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequest.setQueryFrom(queryfrom);
                if (OpalUtil.isNotEmpty(querywhere)) {
                    sdiRequest.setQueryWhere(querywhere);
                }
                if (OpalUtil.isNotEmpty(queryorderby)) {
                    sdiRequest.setQueryOrderBy(queryorderby);
                }
                if ((ds = sdiProcessor.getSDIData(sdiRequest).getDataset("primary")) != null) {
                    for (int row = 0; row < ds.size(); ++row) {
                        String value = ds.getValue(row, valuecolumn);
                        String displayvalue = ds.getValue(row, displaycolumn, value);
                        sb.append("<option value='").append(value).append("'");
                        sb.append(value.equals(defaultvalue) ? " selected" : "");
                        sb.append(">").append(displayvalue).append("</option>");
                    }
                }
            }
        }
        sb.append("</select>");
        return sb.toString();
    }

    public static String getLookupHtml(PropertyList column, String fieldid, String attributes) {
        return ColumnUtil.getLookupHtml(column, fieldid, attributes, "");
    }

    public static String getLookupHtml(PropertyList column, String fieldid, String attributes, String klass) {
        String columnwidth;
        String defaultvalue;
        String validation = column.getProperty("validation").trim();
        boolean mandatory = validation.contains("Mandatory") || "Y".equals(column.getProperty("mandatory"));
        String string = defaultvalue = attributes.contains("value=") ? "" : column.getProperty("default");
        if (defaultvalue.contains("\"")) {
            defaultvalue = StringUtil.replaceAll(defaultvalue, "\"", "&quot;");
        }
        if (!(columnwidth = column.getProperty("width", "120")).endsWith("%")) {
            columnwidth = columnwidth + "px";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=0 cellspacing=0 border=0>");
        sb.append("<tr datatype='skip'><td>");
        sb.append("<input id=\"").append(fieldid).append("\" readonly");
        sb.append(" ").append(attributes);
        sb.append(OpalUtil.isNotEmpty(defaultvalue) ? " value=\"" + defaultvalue + "\"" : "");
        if (OpalUtil.isNotEmpty(validation)) {
            sb.append(" fieldval=\"").append(validation).append("\"");
        }
        klass = klass + " input_field" + (mandatory ? " mandatoryfield" : "");
        sb.append(" class='").append(klass).append("'");
        sb.append(" ttl=\"").append(StringUtil.replaceAll(column.getProperty("title", column.getProperty("columnid")), "\"", "&quot;")).append("\"");
        sb.append(" style=\"width:").append(columnwidth).append("px\"");
        sb.append(" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear( '").append(fieldid).append("' );};\"");
        sb.append(">");
        sb.append("</td><td valign=middle>");
        sb.append("<img id='img_").append(fieldid).append("' src='WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg' style='cursor:pointer' class='lookup_img'");
        sb.append(" onclick=\"lookupPageSelect( '").append(column.getProperty("lookuppage")).append("', '").append(fieldid).append("')\">");
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    public static String getDateLookupHtml(PropertyList column, String fieldid, String attributes) {
        return ColumnUtil.getDateLookupHtml(column, fieldid, attributes, "");
    }

    public static String getDateLookupHtml(PropertyList column, String fieldid, String attributes, String klass) {
        String columnwidth;
        String defaultvalue;
        String validation = column.getProperty("validation").trim();
        boolean mandatory = validation.contains("Mandatory") || "Y".equals(column.getProperty("mandatory"));
        String string = defaultvalue = attributes.contains("value=") ? "" : column.getProperty("default");
        if (defaultvalue.contains("\"")) {
            defaultvalue = StringUtil.replaceAll(defaultvalue, "\"", "&quot;");
        }
        if (!(columnwidth = column.getProperty("width", "160")).endsWith("%")) {
            columnwidth = columnwidth + "px";
        }
        String dateFormat = column.getProperty("format", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<input id='").append(fieldid).append("'");
        sb.append(" ").append(attributes);
        sb.append(OpalUtil.isNotEmpty(defaultvalue) ? " value=\"" + defaultvalue + "\"" : "");
        if (OpalUtil.isNotEmpty(validation)) {
            sb.append(" fieldval=\"").append(validation).append("\"");
        }
        klass = klass + " input_field" + (mandatory ? " mandatoryfield" : "");
        sb.append(" class='").append(klass).append("'");
        sb.append(" size='23'");
        sb.append(" ttl=\"").append(StringUtil.replaceAll(column.getProperty("title", column.getProperty("columnid")), "\"", "&quot;")).append("\"");
        sb.append(" style='width:").append(columnwidth).append("' dateformat=\"").append(dateFormat).append("\">");
        sb.append("<img src='WEB-CORE/imageref/flat/32/flat_black_calendar2.svg' id='img_").append(fieldid).append("' style='cursor:pointer' class='datelookup_img'");
        sb.append(" onclick='sapphire.lookup.date.open( \"").append(fieldid).append("\", \"\", \"\", \"\", \"\", \"").append(dateFormat).append("\" );return false;'>");
        return sb.toString();
    }

    public static String parseDisplayValue(String value, String displayvalue) {
        if (OpalUtil.isNotEmpty(displayvalue)) {
            List<String> st = OpalUtil.toList(displayvalue, ";");
            for (String str : st) {
                int equalindex = str.indexOf("=");
                String replacestr = str.substring(0, equalindex).trim();
                String replacement = str.substring(++equalindex).trim();
                if (!value.equals(replacestr)) continue;
                value = replacement;
                break;
            }
        }
        return value;
    }

    public static String parseColumnLink(String value, DataSet ds, int index, PropertyList link) {
        String href;
        if (link != null && OpalUtil.isNotEmpty(href = link.getProperty("href"))) {
            String[] s;
            String target = link.getProperty("target", "_blank");
            String title = link.getProperty("title", "");
            for (String str : s = StringUtil.getTokens(href)) {
                if (!ds.isValidColumn(str)) continue;
                href = StringUtil.replaceAll(href, "[" + str + "]", ds.getValue(index, str, ""));
            }
            value = "<a target='" + target + "' href='" + href + "'" + (OpalUtil.isNotEmpty(title) ? " title='" + title + "'" : "") + ">" + value + "</a>";
        }
        return value;
    }
}

