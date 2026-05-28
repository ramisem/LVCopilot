/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.CriteriaEditor;
import com.labvantage.sapphire.modules.adhocbrowser.SearchableColumn;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DefaultCriteriaEditor
extends CriteriaEditor {
    @Override
    public PropertyList getEditorProperty(String sdcid, PropertyList column, AdhocMetaData adhocmetadata, SDCProcessor sdcProcessor, SDITagUtil sdiTagUtil, TranslationProcessor tp, PropertyList pagedata) throws SapphireException {
        PropertyList tempcolumn = new PropertyList();
        String columnid = column.getProperty("columnid");
        String isdetail = column.getProperty("isdetail");
        String connectionid = sdcProcessor.getConnectionid();
        if (columnid != null && columnid.length() > 0) {
            PropertyList pageColDef;
            SearchableColumn searchableColumn;
            String tableid = adhocmetadata.getTableid(sdcid);
            boolean isDetailColumn = false;
            if (columnid.indexOf(".") > 0) {
                try {
                    String tempcolumnid = columnid.substring(0, columnid.lastIndexOf("."));
                    tableid = AdhocMetaData.getReferenceEntityName(connectionid, adhocmetadata.getTableid(sdcid), tempcolumnid);
                    if (tableid == null || tableid.length() == 0) {
                        isDetailColumn = true;
                    } else if ("documentfield".equals(tableid)) {
                        isDetailColumn = true;
                    }
                }
                catch (Exception me) {
                    isDetailColumn = true;
                }
                if (isDetailColumn) {
                    tableid = columnid.substring(0, columnid.indexOf("."));
                }
            } else if ("true".equals(isdetail)) {
                isDetailColumn = true;
                columnid = tableid + "." + columnid;
                column.setProperty("columnid", columnid);
            }
            String tempsdcid = adhocmetadata.getSdcId(tableid);
            if ("worksheetsdi_Sample".equals(tableid)) {
                tempsdcid = "Sample";
            }
            DataSet links = sdcProcessor.getLinksData(tempsdcid);
            String findcolid = columnid;
            if (columnid.indexOf(".") > 0) {
                findcolid = columnid.substring(columnid.lastIndexOf(".") + 1);
            }
            if ((searchableColumn = adhocmetadata.getSearchableColumn(tableid, findcolid)) != null) {
                column.setProperty("datatype", searchableColumn.getDatatype());
                column.setProperty("columndesc", searchableColumn.getColumndesc());
                column.setProperty("columndesctrans", tp.translate(searchableColumn.getColumndesc()));
                column.setProperty("columnlength", searchableColumn.getColumnlength());
            } else if (isDetailColumn) {
                column.setProperty("datatype", "C");
                column.setProperty("columndesc", "");
                column.setProperty("columnlength", "");
            } else {
                Trace.logWarn("Not a valid searchable column:" + columnid + " on table " + tableid);
            }
            if (tempsdcid != null && (pageColDef = AdhocQueryPageUtil.getColumnPropertyList(tempsdcid, findcolid, pagedata, sdcProcessor.getColumns(sdcid), tp)) != null) {
                column.setProperty("mode", pageColDef.getProperty("mode"));
                column.setProperty("sql", pageColDef.getProperty("sql"));
                if (pageColDef.getProperty("title").length() > 0) {
                    column.setProperty("title", pageColDef.getProperty("title"));
                    column.setProperty("columndesctrans", pageColDef.getProperty("title"));
                }
                if (pageColDef.getPropertyList("lookuplink") != null && pageColDef.getPropertyList("lookuplink").getProperty("href").length() > 0) {
                    column.setProperty("lookuplink", pageColDef.getPropertyList("lookuplink"));
                }
            }
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("sdccolumnid", findcolid);
            DataSet columnlink = links != null ? links.getFilteredDataSet(filterMap) : null;
            String linktype = "";
            if (columnlink != null && columnlink.size() > 0) {
                linktype = columnlink.getValue(0, "linktype");
            }
            column.setProperty("value", column.getProperty("defaultvalue"));
            column.setProperty("name", DefaultCriteriaEditor.getUniqueId());
            if (column.getProperty("mode").length() == 0) {
                column.setProperty("mode", "input");
            }
            if (linktype.length() == 0) {
                if ("C".equals(column.getProperty("datatype"))) {
                    String mode = column.getProperty("mode");
                    if (!"dropdownlist".equals(mode)) {
                        String columnlength = column.getProperty("columnlength");
                        if (columnlength.length() == 0) {
                            columnlength = "80";
                        }
                        column.setProperty("maxlen", columnlength);
                        int collength = Integer.parseInt(columnlength);
                        if (collength > 20 && collength <= 80) {
                            column.setProperty("size", "35");
                        } else if (collength > 80) {
                            column.setProperty("size", "45");
                        } else {
                            column.setProperty("size", columnlength);
                        }
                        if ("lookup".equals(mode)) {
                            DefaultCriteriaEditor.setDefaultLookupLink(column, columnlink.getValue(0, "linksdcid"), new QueryProcessor(tp.getConnectionid()));
                        }
                    }
                } else if ("D".equals(column.getProperty("datatype"))) {
                    column.setProperty("onblur", "validateValue( 'Date', this )");
                    column.setProperty("mode", "datelookup");
                    if (column.getProperty("format").length() == 0 && "Y".equals(sdcProcessor.getSDCColumnProperty(sdcid, columnid, "timezoneindependent"))) {
                        column.setProperty("format", "O");
                        column.setProperty("timezoneindependent", "Y");
                    }
                    column.setProperty("name", DefaultCriteriaEditor.getUniqueId());
                } else if ("N".equals(column.getProperty("datatype")) || "R".equals(column.getProperty("datatype"))) {
                    column.setProperty("onblur", "validateValue( 'Number', this )");
                    column.setProperty("mode", "input");
                    column.setProperty("size", "10");
                    column.setProperty("name", DefaultCriteriaEditor.getUniqueId());
                }
            } else {
                column.setProperty("linktype", linktype);
                column.setProperty("linktableid", columnlink.getValue(0, "tableid"));
                column.setProperty("linkid", columnlink.getValue(0, "linkid"));
                if ("F".equals(linktype)) {
                    if ("dropdownlist".equals(column.getProperty("mode"))) {
                        column.setProperty("mode", "dropdownlist");
                        String sql = column.getProperty("sql").trim();
                        if (sql.length() > 0) {
                            String[] tokens = StringUtil.getTokens(sql);
                            if (tokens == null || tokens.length > 0) {
                                // empty if block
                            }
                            column.setProperty("sdcid", "");
                        } else {
                            column.setProperty("sdcid", columnlink.getValue(0, "linksdcid"));
                        }
                    } else {
                        column.setProperty("sdcid", columnlink.getValue(0, "linksdcid"));
                        column.setProperty("mode", "lookup");
                        column.setProperty("img", "WEB-CORE/elements/images/lookup.gif");
                        DefaultCriteriaEditor.setDefaultLookupLink(column, columnlink.getValue(0, "linksdcid"), new QueryProcessor(tp.getConnectionid()));
                    }
                    column.setProperty("linksdcid", column.getProperty("sdcid"));
                } else if ("V".equals(linktype) || "R".equals(linktype)) {
                    column.setProperty("mode", "dropdownlist");
                    if ("V".equals(linktype)) {
                        column.setProperty("reftypeid", columnlink.getValue(0, "reftypeid"));
                    } else {
                        column.setProperty("sql", "select distinct " + columnid + " from " + tableid + " where " + columnid + " is not null order by " + columnid);
                    }
                }
            }
            return column;
        }
        throw new SapphireException("No columnid specified");
    }
}

