/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ResultPropertyListBuilder {
    public static PropertyList build(String sdcid, DataSet searchrequestDs, String[] realcols, PageContext pageContext) {
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String adhocquerypageid = pageContext.getRequest().getParameter("adhocquerypageid");
        PropertyList pagedata = (PropertyList)pageContext.getSession().getAttribute("adhocbrowser_" + adhocquerypageid);
        PropertyList dataentryPL = AdhocQueryPageUtil.getDataEntryPropertyList(sdcid, pagedata);
        PropertyListCollection columns = AdhocQueryPageUtil.getSDCColumnPLCollection(sdcid, pagedata, new SDCProcessor(pageContext).getColumns(sdcid), tp);
        HashMap<String, String> displayvaluesMap = new HashMap<String, String>();
        HashMap<String, String> formatMap = new HashMap<String, String>();
        HashMap<String, String> translatedTitleMap = new HashMap<String, String>();
        QueryProcessor qp = new QueryProcessor(pageContext);
        if (columns != null) {
            for (int i = 0; i < columns.size(); ++i) {
                DataSet displayvalueds;
                PropertyList column = columns.getPropertyList(i);
                formatMap.put(column.getProperty("columnid"), column.getProperty("format"));
                if (column.getProperty("displayvalue") != null && column.getProperty("displayvalue").length() > 0) {
                    displayvaluesMap.put(column.getProperty("columnid"), column.getProperty("displayvalue"));
                } else if (column.getProperty("sql") != null && column.getProperty("sql").length() > 0 && (displayvalueds = qp.getSqlDataSet(column.getProperty("sql"))) != null && displayvalueds.getColumns().length == 2) {
                    String rcolid = displayvalueds.getColumns()[0];
                    String dcolid = displayvalueds.getColumns()[1];
                    StringBuffer svalues = new StringBuffer();
                    for (int r = 0; r < displayvalueds.getRowCount(); ++r) {
                        String rvalue = displayvalueds.getValue(r, rcolid);
                        String dvalue = displayvalueds.getValue(r, dcolid);
                        if (dvalue == null || dvalue.length() <= 0) continue;
                        svalues.append(";" + rvalue + "=" + dvalue);
                    }
                    if (svalues.length() > 1) {
                        displayvaluesMap.put(column.getProperty("columnid"), svalues.substring(1));
                    }
                }
                if (column.getProperty("columndesctrans").length() <= 0) continue;
                translatedTitleMap.put(column.getProperty("columnid"), column.getProperty("columndesctrans"));
            }
        }
        String dataitemtitledisplay = "";
        if (dataentryPL != null) {
            dataitemtitledisplay = dataentryPL.getProperty("dataitemtitle");
        }
        PropertyList datalist = new PropertyList();
        datalist.setProperty("sdcid", sdcid);
        datalist.setProperty("dataset", "adhoc_dataset");
        datalist.setProperty("selectortype", "checkbox");
        datalist.setProperty("initialgrouped", "N");
        datalist.setProperty("showgroupby", "Y");
        datalist.setProperty("showgroupcount", "Y");
        datalist.setProperty("showcollapseall", "Y");
        PropertyListCollection displaycolumns = new PropertyListCollection();
        PropertyListCollection sortby = new PropertyListCollection();
        String[] tokens = StringUtil.getTokens(dataitemtitledisplay);
        String paramSelect = "";
        String paramlistSelect = "";
        for (int i = 0; i < tokens.length; ++i) {
            if (tokens[i].indexOf("param.") == 0) {
                if (paramSelect.length() == 0) {
                    paramSelect = "select paramid";
                }
                paramSelect = paramSelect + "," + tokens[i].substring(6);
                continue;
            }
            if (tokens[i].indexOf("paramlist.") != 0) continue;
            if (paramlistSelect.length() == 0) {
                paramlistSelect = "select paramlistid, paramlistversionid, variantid";
            }
            paramlistSelect = paramlistSelect + "," + tokens[i].substring(10);
        }
        DataSet paramdataset = null;
        StringBuffer paraminclause = null;
        StringBuffer paramlistinclause = null;
        if (paramSelect.length() > 0 || paramlistSelect.length() > 0) {
            for (int i = 0; i < searchrequestDs.size(); ++i) {
                String columnid;
                if (!"V".equals(searchrequestDs.getString(i, "group")) || (columnid = searchrequestDs.getString(i, "columnid")).indexOf("sdidataitem[") < 0) continue;
                for (int c = 0; c < realcols.length; ++c) {
                    if (realcols[c].indexOf(columnid) < 0) continue;
                    String[] dataitemparts = StringUtil.split(realcols[c].substring(realcols[c].indexOf("]") + 2), ";");
                    if (paraminclause == null) {
                        paraminclause = new StringBuffer(dataitemparts[4]);
                        paramlistinclause = new StringBuffer(dataitemparts[0]);
                        continue;
                    }
                    paraminclause.append("','" + dataitemparts[4]);
                    paramlistinclause.append("','" + dataitemparts[0]);
                }
            }
        }
        if (paramSelect.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            paramdataset = qp.getPreparedSqlDataSet(paramSelect + " from param" + (paraminclause == null ? "" : " where paramid in (" + safeSQL.addIn(paraminclause.toString()) + ")"), safeSQL.getValues());
        }
        DataSet paramlistdataset = null;
        if (paramlistSelect.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            paramlistdataset = qp.getPreparedSqlDataSet(paramlistSelect + " from paramlist" + (paramlistinclause == null ? "" : " where paramlistid in (" + safeSQL.addIn(paramlistinclause.toString()) + ")"), safeSQL.getValues());
        }
        for (int i = 0; i < searchrequestDs.size(); ++i) {
            String columnid;
            if ("V".equals(searchrequestDs.getString(i, "group"))) {
                String documentfieldtitle;
                String[] documentfieldparts;
                int c;
                PropertyList column = new PropertyList();
                columnid = searchrequestDs.getString(i, "columnid");
                String title = searchrequestDs.getString(i, "title");
                if (columnid.indexOf("sdidataitem[") >= 0) {
                    for (c = 0; c < realcols.length; ++c) {
                        if (realcols[c].indexOf(columnid) < 0) continue;
                        column = new PropertyList();
                        String[] dataitemparts = StringUtil.split(realcols[c].substring(realcols[c].indexOf("]") + 2), ";");
                        String dataitemtitle = "";
                        if (dataitemtitledisplay != null && dataitemtitledisplay.length() > 0) {
                            dataitemtitle = dataitemtitledisplay;
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[paramlistid]", dataitemparts[0]);
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[paramlistversionid]", dataitemparts[1]);
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[variantid]", dataitemparts[2]);
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[dataset]", dataitemparts[3]);
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[paramid]", dataitemparts[4]);
                            dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[paramtype]", dataitemparts[5]);
                            String[] customtokens = StringUtil.getTokens(dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[replicateid]", dataitemparts[6]));
                            if (customtokens.length > 0) {
                                for (int t = 0; t < customtokens.length; ++t) {
                                    String colid;
                                    if (customtokens[t].indexOf("param.") == 0) {
                                        colid = customtokens[t].substring(6);
                                        String colvalue = paramdataset.getValue(paramdataset.findRow("paramid", dataitemparts[4]), colid, dataitemparts[4]);
                                        dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[" + customtokens[t] + "]", colvalue);
                                    }
                                    if (customtokens[t].indexOf("paramlist.") != 0) continue;
                                    colid = customtokens[t].substring(10);
                                    HashMap<String, String> findMap = new HashMap<String, String>();
                                    findMap.put("paramlistid", dataitemparts[0]);
                                    findMap.put("paramlistversionid", dataitemparts[1]);
                                    findMap.put("variantid", dataitemparts[2]);
                                    String colvalue = paramlistdataset.getValue(paramlistdataset.findRow(findMap), colid, dataitemparts[0]);
                                    dataitemtitle = StringUtil.replaceAll(dataitemtitle, "[" + customtokens[t] + "]", colvalue);
                                }
                            }
                        } else {
                            dataitemtitle = dataitemparts[0] + "(var:" + dataitemparts[2] + "dataset:" + dataitemparts[3] + " )<br/>" + dataitemparts[4] + "(" + dataitemparts[5] + ") rep:" + dataitemparts[6];
                        }
                        column.setProperty("columnid", realcols[c]);
                        column.setProperty("title", dataitemtitle);
                        displaycolumns.add(column);
                    }
                    datalist.setProperty("headerheight", "49");
                    continue;
                }
                if (columnid.indexOf("documentfield[") >= 0) {
                    for (c = 0; c < realcols.length; ++c) {
                        if (realcols[c].indexOf(columnid) < 0) continue;
                        column = new PropertyList();
                        documentfieldparts = StringUtil.split(realcols[c].substring(realcols[c].indexOf("]") + 2), ";");
                        documentfieldtitle = "";
                        documentfieldtitle = documentfieldparts.length == 2 ? documentfieldparts[0] + "<br/>" + title : title;
                        column.setProperty("columnid", realcols[c]);
                        column.setProperty("title", documentfieldtitle);
                        displaycolumns.add(column);
                    }
                    continue;
                }
                if (columnid.indexOf("worksheetitemfield[") >= 0) {
                    for (c = 0; c < realcols.length; ++c) {
                        if (realcols[c].indexOf(columnid) < 0) continue;
                        column = new PropertyList();
                        documentfieldparts = StringUtil.split(realcols[c].substring(realcols[c].indexOf("]") + 2), ";");
                        documentfieldtitle = "";
                        documentfieldtitle = documentfieldparts.length == 2 ? documentfieldparts[0] + "<br/>" + title : title;
                        column.setProperty("columnid", realcols[c]);
                        column.setProperty("title", documentfieldtitle);
                        displaycolumns.add(column);
                    }
                    datalist.setProperty("headerheight", "30");
                    continue;
                }
                if (columnid.indexOf("attribute[") >= 0) {
                    for (c = 0; c < realcols.length; ++c) {
                        if (realcols[c].indexOf(columnid) < 0) continue;
                        column = new PropertyList();
                        documentfieldparts = StringUtil.split(realcols[c].substring(realcols[c].indexOf("]") + 2), ";");
                        documentfieldtitle = "";
                        documentfieldtitle = documentfieldparts.length == 2 ? documentfieldparts[0] + "<br/>" + title : title;
                        column.setProperty("columnid", realcols[c]);
                        column.setProperty("title", documentfieldtitle);
                        displaycolumns.add(column);
                    }
                    continue;
                }
                column.setProperty("columnid", columnid.toLowerCase());
                if (translatedTitleMap.get(columnid) != null && ((String)translatedTitleMap.get(columnid)).length() > 0) {
                    title = (String)translatedTitleMap.get(columnid);
                }
                column.setProperty("title", title);
                column.setProperty("format", (String)formatMap.get(columnid));
                if (displayvaluesMap.get(columnid) != null) {
                    column.setProperty("displayvalue", (String)displayvaluesMap.get(columnid));
                }
                displaycolumns.add(column);
                continue;
            }
            if (!"S".equals(searchrequestDs.getString(i, "group"))) continue;
            PropertyList sortbycol = new PropertyList();
            columnid = searchrequestDs.getString(i, "columnid");
            String direction = searchrequestDs.getString(i, "operator");
            sortbycol.setProperty("columnid", columnid);
            sortbycol.setProperty("asc_desc", direction);
            sortbycol.setProperty("callback", "Y");
            sortby.add(sortbycol);
        }
        if (displaycolumns.size() == 0) {
            if ("DataSet".equals(sdcid) || "DataItem".equals(sdcid)) {
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                for (int c = 0; c < ("DataSet".equals(sdcid) ? 8 : 11); ++c) {
                    PropertyList column = new PropertyList();
                    column.setProperty("columnid", keycols[c]);
                    column.setProperty("title", tp.translate(keycols[c]));
                    displaycolumns.add(column);
                }
            } else {
                String desccolumnid;
                SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
                PropertyListCollection ddtcolumns = sdcProcessor.getColumns(sdcid);
                displaycolumns.add(ResultPropertyListBuilder.getColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid1"), pagedata, ddtcolumns, tp));
                if (sdcProcessor.getProperty(sdcid, "keycolid2") != null && sdcProcessor.getProperty(sdcid, "keycolid2").length() > 0) {
                    displaycolumns.add(ResultPropertyListBuilder.getColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid2"), pagedata, ddtcolumns, tp));
                }
                if (sdcProcessor.getProperty(sdcid, "keycolid3") != null && sdcProcessor.getProperty(sdcid, "keycolid3").length() > 0) {
                    displaycolumns.add(ResultPropertyListBuilder.getColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid3"), pagedata, ddtcolumns, tp));
                }
                if (!(desccolumnid = sdcProcessor.getProperty(sdcid, "desccol")).equals(sdcProcessor.getProperty(sdcid, "keycolid1"))) {
                    displaycolumns.add(ResultPropertyListBuilder.getColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "desccol"), pagedata, ddtcolumns, tp));
                }
            }
        }
        datalist.setProperty("columns", displaycolumns);
        PropertyListCollection groupbys = new PropertyListCollection();
        groupbys.addAll(displaycolumns);
        datalist.setProperty("groupby", groupbys);
        datalist.setProperty("showgroupby", "Y");
        datalist.setProperty("sortby", sortby);
        String pageorfile = "";
        pageorfile = "page".equals(pageContext.getRequest().getParameter("command")) ? ((PropertyList)pageContext.getRequest().getAttribute("pagedata")).getProperty("page") : pageContext.getRequest().getParameter("file");
        if (sortby.size() == 0) {
            PropertyList sortbycol = new PropertyList();
            sortbycol.setProperty("columnid", displaycolumns.getPropertyList(0).getProperty("columnid"));
            sortbycol.setProperty("asc_desc", "a");
            sortbycol.setProperty("callback", "Y");
            sortby.add(sortbycol);
            pageContext.getSession().removeAttribute(pageorfile + "_listsortby");
        } else {
            pageContext.getSession().removeAttribute(pageorfile + "_listsortby");
        }
        return datalist;
    }

    private static PropertyList getColumnProperty(String sdcid, String columnid, PropertyList pagedata, PropertyListCollection ddtcolumns, TranslationProcessor tp) {
        PropertyList column = AdhocQueryPageUtil.getColumnPropertyList(sdcid, columnid, pagedata, ddtcolumns, tp);
        if (column == null) {
            column = new PropertyList();
            column.setProperty("columnid", columnid);
            column.setProperty("title", tp.translate(columnid));
        } else {
            column.setProperty("title", tp.translate(column.getProperty("columndesc")));
        }
        return column;
    }
}

