/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.list.ListView;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.MiscUtil;
import java.util.regex.Pattern;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataLookup
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85560 $";
    public static final String JS_FILE = "WEB-CORE/lookup/scripts/datalookup.js";
    public static final String JS_OBJECT = "oDataLookup";
    public static final String JS_FIELDACCEPT = "doFieldAccept";
    public static final String JS_LOOKUPACCEPT = "doLookupAccept";
    public static final String JS_DETAILACCEPT = "doDetailAccept";
    public static final String JS_CLOSE = "close";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_TABLEID = "tableid";
    public static final String PROPERTY_COLUMNS = "columns";
    public static final String PROPERTY_COLUMNTRANSLATE = "columntranslate";
    public static final String PROPERTY_COLUMNTITLES = "columntitles";
    public static final String PROPERTY_RETURNCOLUMNS = "returncolumns";
    public static final String PROPERTY_QUERYWHERE = "querywhere";
    public static final String PROPERTY_RESTRICTIVEWHERE = "restrictivewhere";
    public static final String PROPERTY_MULTISELECT = "multiselect";
    public static final String PROPERTY_BUTTONPOSITION = "buttonposition";
    public static final String PROPERTY_INDEX = "index";
    public static final String PROPERTY_DSNAME = "dsname";
    public static final String PROPERTY_FIELDID = "fieldid";
    public static final String PROPERTY_LOOKUPCALLBACK = "lookupcallback";
    public static final String PROPERTY_DETAILCALLBACK = "detailcallback";
    public static final String PROPERTY_ELEMENTID = "elementid";
    public static final String PROPERTY_EXTRA = "extra";
    public static final String PROPERTY_GROUPBY = "groupby";
    public static final String PROPERTY_GROUPBYTEXT = "groupbytext";
    public static final String PROPERTY_TOOLBARSTYLE = "toolbarstyle";
    public static final String PROPERTY_SHOWLAYOUT = "showlayout";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_SORTBY = "sortby";
    public static final short SELECTOR_CHECKBOX = 0;
    public static final short SELECTOR_RADIOBUTTON = 1;
    public static final short POSITION_TOPLEFT = 0;
    public static final short POSITION_TOPRIGHT = 1;
    public static final short POSITION_BOTTOMLEFT = 2;
    public static final short POSITION_BOTTOMRIGHT = 3;
    public static final String DEFAULT_SEPARATOR = ";";
    public static final short DEFAULT_SELECTOR = 1;
    public static final short DEFAULT_BUTTONPOSITION = 0;
    private String tableid;
    private String[] columnsArray;
    private String[] columnsTranslateArray;
    private String[] columnsTitlesArray;
    private String returnColumnHeaders;
    private String[] returnColumnsArray;
    private String queryWhere;
    private String restrictiveWhere;
    private short selector;
    private short buttonPosition;
    private String fieldid;
    private String lookupcallback;
    private String detailcallback;
    private String row;
    private String dsname;
    private String extra;
    private String elementId;
    private String groupbyid;
    private String groupbytext;
    private String toolbarstyle;
    private boolean showlayout;
    private String title;
    private String sortby;
    private int lookupid = -1;

    public DataLookup(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    private void renderHTML(DataSet data, String[] theColumns, String[] theTitles, String[] theTranslate, String[] theReturnCols, short theSelector, StringBuffer buffer) {
        this.logger.info("renderHTML called... ");
        if (data != null && data.size() > 0) {
            PropertyList link;
            PropertyList column;
            int index;
            ListView listView = new ListView(this.pageContext, data, this.getConnectionId());
            PropertyList element = new PropertyList();
            switch (theSelector) {
                case 0: {
                    element.setProperty("selectortype", "checkbox");
                    break;
                }
                case 1: {
                    element.setProperty("selectortype", "radiobutton");
                    break;
                }
                default: {
                    element.setProperty("selectortype", "radiobutton");
                }
            }
            element.setProperty("initselectall", "");
            element.setProperty("dataset", "");
            element.setProperty("initexpandall", "");
            element.setProperty("rowsperpage", "");
            element.setProperty("retrievelimit", "");
            PropertyListCollection columns = new PropertyListCollection();
            this.logger.debug("About to create hidden columns...");
            for (index = 0; index < theReturnCols.length; ++index) {
                column = new PropertyList();
                column.setProperty("id", "col" + index);
                column.setProperty("columnid", theReturnCols[index]);
                int arrayIndex = MiscUtil.MiscArray.findString(theColumns, theReturnCols[index], 0, true);
                if (arrayIndex > -1) {
                    column.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate(theTitles[arrayIndex]));
                    column.setProperty("mode", "Display Text");
                    if (theTranslate != null && MiscUtil.MiscArray.isStringInArray(theTranslate, theColumns[index], true)) {
                        column.setProperty("translatevalue", "Y");
                    } else {
                        column.setProperty("translatevalue", "");
                    }
                } else {
                    column.setProperty(PROPERTY_TITLE, theReturnCols[index]);
                    column.setProperty("mode", "Hidden Value");
                    column.setProperty("translatevalue", "");
                }
                column.setProperty("width", "");
                column.setProperty("align", "");
                column.setProperty("returnvalue", "Y");
                column.setProperty("displayvalue", "");
                column.setProperty("pseudocolumn", "");
                column.setProperty("format", "");
                link = new PropertyList();
                link.setProperty("href", "");
                link.setProperty("target", "");
                link.setProperty("tip", "");
                column.setProperty("link", link);
                columns.add(column);
            }
            this.logger.debug("About to create visible columns...");
            for (index = 0; index < theColumns.length; ++index) {
                if (MiscUtil.MiscArray.isStringInArray(theReturnCols, theColumns[index], true)) continue;
                column = new PropertyList();
                column.setProperty("id", "col" + index);
                column.setProperty("columnid", theColumns[index]);
                column.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate(theTitles[index]));
                column.setProperty("mode", "Display Text");
                column.setProperty("width", "");
                column.setProperty("align", "");
                column.setProperty("returnvalue", "N");
                column.setProperty("displayvalue", "");
                column.setProperty("pseudocolumn", "");
                column.setProperty("format", "");
                if (theTranslate != null && MiscUtil.MiscArray.isStringInArray(theTranslate, theColumns[index], true)) {
                    column.setProperty("translatevalue", "Y");
                } else {
                    column.setProperty("translatevalue", "");
                }
                link = new PropertyList();
                link.setProperty("href", "");
                link.setProperty("target", "");
                link.setProperty("tip", "");
                column.setProperty("link", link);
                columns.add(column);
            }
            element.setProperty(PROPERTY_COLUMNS, columns);
            if (this.groupbyid != null && this.groupbyid.length() > 0) {
                PropertyListCollection groupby = new PropertyListCollection();
                PropertyList group = new PropertyList();
                group.setProperty("columnid", this.groupbyid);
                group.setProperty(PROPERTY_TITLE, this.groupbytext);
                groupby.add(group);
                element.setProperty(PROPERTY_GROUPBY, groupby);
                element.setProperty("initgrouped", "Y");
                element.setProperty("initexpandimg", "WEB-CORE/pagetypes/list/images/minus.gif");
            } else {
                element.setProperty("initgrouped", "N");
            }
            listView.setElementid("listview");
            listView.setElementProperties(element);
            this.logger.debug("About to get listview HTML...");
            buffer.append(listView.getHtml());
            this.logger.debug("dataview HTML obtained.");
        } else {
            buffer.append(this.getTranslationProcessor().translate("No rows returned."));
        }
    }

    private DataSet getData(String theTableId, String theQueryWhere, String theResWhere, String[] theColumnsArray, String[] theReturnArray, PageContext pageContext) {
        StringBuffer sql;
        this.logger.info("getData called...");
        if (this.isUnregisteredSQLPermitted() || this.lookupid > -1) {
            int index;
            sql = new StringBuffer("SELECT distinct ");
            StringBuffer columns = new StringBuffer();
            for (index = 0; index < theColumnsArray.length; ++index) {
                MiscUtil.MiscString.appendDelimeteredString(columns, theColumnsArray[index], ", ");
            }
            for (index = 0; index < theReturnArray.length; ++index) {
                if (MiscUtil.MiscArray.isStringInArray(theColumnsArray, theReturnArray[index], true)) continue;
                MiscUtil.MiscString.appendDelimeteredString(columns, theReturnArray[index], ", ");
            }
            sql.append(columns);
            if (this.sortby != null && this.sortby.length() > 0) {
                if (columns.length() > 0) {
                    sql.append(",");
                }
                sql.append(" ").append(StringUtil.replaceAll(this.sortby, DEFAULT_SEPARATOR, ", "));
            }
            sql.append(" FROM ").append(theTableId).append(" ");
            if (theQueryWhere.length() > 0) {
                if (theQueryWhere.toLowerCase().startsWith("where ")) {
                    sql.append(" ( ").append(theQueryWhere).append(" ) ");
                } else {
                    sql.append(" WHERE ( ").append(theQueryWhere).append(" ) ");
                }
            }
            if (theResWhere.length() > 0) {
                if (theQueryWhere.length() > 0) {
                    sql.append(" AND ");
                }
                sql.append(" ( ").append(theResWhere).append(" ) ");
            }
            if (this.sortby != null && this.sortby.length() > 0) {
                sql.append(" order by ").append(StringUtil.replaceAll(this.sortby, DEFAULT_SEPARATOR, ", "));
            } else {
                sql.append(" order by 1");
            }
        } else {
            this.logger.error("Unregistered lookups/sql is not permitted.");
            return null;
        }
        this.logger.debug("sql = " + sql.toString());
        QueryProcessor queryProcessor = new QueryProcessor(pageContext);
        DataSet data = queryProcessor.getSqlDataSet(sql.toString());
        return data;
    }

    public static void setUpLookup(PageContext pageContext, String lookupdefid, String sdcid, String tableid, String queryWhere, String columns, String titles, String returncolumns, String restrictiveWhere) {
        PropertyList toCache = new PropertyList();
        toCache.setProperty(PROPERTY_SDCID, sdcid);
        toCache.setProperty(PROPERTY_TABLEID, tableid);
        toCache.setProperty(PROPERTY_QUERYWHERE, queryWhere);
        toCache.setProperty(PROPERTY_COLUMNS, columns);
        toCache.setProperty(PROPERTY_COLUMNTITLES, titles);
        toCache.setProperty(PROPERTY_RETURNCOLUMNS, returncolumns);
        toCache.setProperty(PROPERTY_RESTRICTIVEWHERE, restrictiveWhere);
        if (pageContext != null) {
            pageContext.getSession().setAttribute("datalookup_" + lookupdefid, (Object)toCache);
        }
    }

    private boolean loadProperties() {
        String sdcid;
        this.logger.info("loadProperties called...");
        boolean isOK = false;
        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
        try {
            this.lookupid = Integer.parseInt(this.element.getProperty("lookupdefid", "-1"));
        }
        catch (Exception e) {
            this.lookupid = -1;
        }
        String columnsstring = "";
        String titlestring = "";
        String translatestring = "";
        switch (this.lookupid) {
            case 10001: {
                sdcid = "LV_AssayType";
                this.tableid = "";
                columnsstring = "s_assaytypeid;assaytypedesc";
                titlestring = "AssayType ID;Description";
                this.returnColumnHeaders = "s_assaytypeid";
                this.queryWhere = "";
                this.restrictiveWhere = "";
                break;
            }
            case 10002: {
                sdcid = "SampleType";
                this.tableid = "";
                columnsstring = "s_sampletypeid;sampletypedesc";
                titlestring = "SampleType ID;Description";
                this.returnColumnHeaders = "s_sampletypeid";
                this.queryWhere = "";
                this.restrictiveWhere = "";
                break;
            }
            case 10003: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "basedonid = '[?]' AND NOT(attributedefid IN ([?]) AND allowduplicatesflag = 'N')";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10023: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "sdcid = '[?]' AND NOT(attributedefid IN ([?]) AND allowduplicatesflag = 'N') AND attributedefid IN( SELECT attributeid FROM sdiattribute WHERE sdcid = '[?]' AND keyid1 = '[?]' AND keyid2 = '[?]')";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10004: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "basedonid = '[?]'";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10014: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "basedonid = '[?]' AND attributedefid IN( SELECT attributeid FROM sdiattribute WHERE sdcid = '[?]' AND keyid1 = '[?]' AND keyid2 = '[?]')";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10005: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "basedonid IN ([?]) AND NOT(attributedefid IN ([?]) AND allowduplicatesflag = 'N')";
                this.restrictiveWhere = " ( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10006: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;basedonid;attributetitle;helptext";
                titlestring = "Attribute;Target SDC;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid;basedonid";
                this.queryWhere = "basedonid IN ([?])";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10007: {
                sdcid = "";
                this.tableid = "refvalue";
                columnsstring = "refvalueid;refvaluedesc";
                titlestring = "Ref Value;Description";
                translatestring = "";
                this.returnColumnHeaders = "";
                this.queryWhere = "reftypeid='[?]'";
                this.restrictiveWhere = "";
                break;
            }
            case 10008: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;attributetitle;helptext";
                titlestring = "Metadata;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid";
                this.queryWhere = "basedonid = '[?]' AND NOT(attributedefid IN ([?]) AND allowduplicatesflag = 'N')";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10009: {
                sdcid = "";
                this.tableid = "attributedef";
                columnsstring = "attributedefid;attributetitle;helptext";
                titlestring = "Metadata;Title;Help Text";
                translatestring = "attributetitle;helptext";
                this.returnColumnHeaders = "attributedefid";
                this.queryWhere = "basedonid = '[?]'";
                this.restrictiveWhere = "( activeflag is null OR activeflag != 'N' )";
                break;
            }
            case 10010: {
                sdcid = "";
                this.tableid = "instrumenttypefield";
                columnsstring = "instrumentfieldid";
                titlestring = "Instrument Field";
                translatestring = "";
                this.returnColumnHeaders = "";
                this.queryWhere = "instrumenttypeid='[?]' or 'a[?]'='a'";
                this.restrictiveWhere = "";
                break;
            }
            case 10011: {
                sdcid = "";
                this.tableid = "sdialias";
                columnsstring = "aliasid;aliastype";
                titlestring = "Alias Id;Alias Type";
                translatestring = "";
                this.returnColumnHeaders = "";
                this.queryWhere = "sdcid='Param' and keyid1='[?]' and keyid2='(null)' and keyid3='(null)'";
                this.restrictiveWhere = "";
                break;
            }
            case 10012: {
                sdcid = "";
                this.tableid = "instrumenttypefield";
                columnsstring = "instrumentfieldid";
                titlestring = "Instrument Field";
                translatestring = "";
                this.returnColumnHeaders = "";
                this.queryWhere = "instrumenttypeid='[?]'";
                this.restrictiveWhere = "";
                break;
            }
            default: {
                PropertyList lookupdefid = null;
                if (this.lookupid < 0 && this.element.getProperty("lookupdefid", "").length() > 0 && this.pageContext != null) {
                    lookupdefid = (PropertyList)this.pageContext.getSession().getAttribute("datalookup_" + this.element.getProperty("lookupdefid"));
                }
                if (lookupdefid == null) {
                    this.lookupid = -1;
                    sdcid = this.element.getProperty(PROPERTY_SDCID);
                    columnsstring = this.element.getProperty(PROPERTY_COLUMNS);
                    translatestring = this.element.getProperty(PROPERTY_COLUMNTRANSLATE);
                    titlestring = this.element.getProperty(PROPERTY_COLUMNTITLES);
                    this.returnColumnHeaders = this.element.getProperty(PROPERTY_RETURNCOLUMNS);
                    this.queryWhere = this.element.getProperty(PROPERTY_QUERYWHERE);
                    this.restrictiveWhere = this.element.getProperty(PROPERTY_RESTRICTIVEWHERE);
                    if (sdcid != null && sdcid.length() != 0) break;
                    this.tableid = this.element.getProperty(PROPERTY_TABLEID);
                    break;
                }
                this.lookupid = 0;
                sdcid = lookupdefid.getProperty(PROPERTY_SDCID);
                this.tableid = lookupdefid.getProperty(PROPERTY_TABLEID);
                columnsstring = lookupdefid.getProperty(PROPERTY_COLUMNS);
                translatestring = "";
                titlestring = lookupdefid.getProperty(PROPERTY_COLUMNTITLES);
                this.returnColumnHeaders = lookupdefid.getProperty(PROPERTY_RETURNCOLUMNS);
                this.queryWhere = lookupdefid.getProperty(PROPERTY_QUERYWHERE);
                this.restrictiveWhere = lookupdefid.getProperty(PROPERTY_RESTRICTIVEWHERE);
            }
        }
        if (sdcid != null && sdcid.length() > 0) {
            this.logger.debug("sdcid = " + sdcid);
            this.tableid = (String)sdcProcessor.getSDCProperties(sdcid).get(PROPERTY_TABLEID);
        }
        if (this.tableid != null && this.tableid.length() > 0) {
            String[] queryparams;
            this.logger.debug("tableid = " + this.tableid);
            String[] tempreturn = null;
            String[] stringArray = queryparams = this.element.getProperty("queryparams", "").length() > 0 ? StringUtil.split(this.element.getProperty("queryparams", ""), DEFAULT_SEPARATOR) : null;
            if (queryparams != null && queryparams.length > 0 && this.queryWhere.length() > 0) {
                for (int q = 0; q < queryparams.length; ++q) {
                    this.queryWhere = this.queryWhere.replaceFirst(Pattern.quote("[?]"), queryparams[q]);
                }
            }
            if (columnsstring != null && columnsstring.length() > 0) {
                this.logger.debug("temp (columns) = " + columnsstring);
                this.columnsArray = columnsstring.split(DEFAULT_SEPARATOR);
                this.logger.debug("columnsArray.length = " + this.columnsArray.length);
                if (translatestring != null && translatestring.length() > 0) {
                    this.columnsTranslateArray = translatestring.split(DEFAULT_SEPARATOR);
                    this.logger.debug("columnsTranslateArray.length = " + this.columnsTranslateArray.length);
                }
                if (titlestring != null && titlestring.length() > 0) {
                    this.logger.debug("temp (titles) = " + titlestring);
                    this.columnsTitlesArray = titlestring.split(DEFAULT_SEPARATOR);
                    if (this.columnsTitlesArray.length != this.columnsArray.length) {
                        this.logger.warn("The titles provided do not match the columns provided thus default to columns.");
                        this.columnsTitlesArray = (String[])this.columnsArray.clone();
                    }
                } else {
                    this.logger.info("No titles provided thus default to columns.");
                    this.columnsTitlesArray = (String[])this.columnsArray.clone();
                }
                this.logger.debug("columnsTitlesArray.length = " + this.columnsTitlesArray.length);
            } else {
                this.logger.info("No columns provided therefore if sdc used then use keycols.");
                if (sdcid != null && sdcid.length() > 0) {
                    this.logger.info("SDC provided thus discover columns.");
                    int kc = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                    tempreturn = new String[kc];
                    this.columnsArray = new String[kc + 1];
                    this.columnsTitlesArray = new String[this.columnsArray.length];
                    if (this.columnsArray.length == 2) {
                        tempreturn[0] = sdcProcessor.getProperty(sdcid, "keycolid1");
                        this.columnsArray[0] = sdcProcessor.getProperty(sdcid, "keycolid1");
                        this.columnsTitlesArray[0] = "Id";
                    } else {
                        for (int index = 0; index < this.columnsArray.length - 1; ++index) {
                            tempreturn[index] = sdcProcessor.getProperty(sdcid, "keycolid" + (index + 1));
                            this.columnsArray[index] = sdcProcessor.getProperty(sdcid, "keycolid" + (index + 1));
                            this.columnsTitlesArray[index] = this.columnsArray[index];
                        }
                    }
                    this.columnsArray[this.columnsArray.length - 1] = sdcProcessor.getProperty(sdcid, "desccol");
                    this.columnsTitlesArray[this.columnsTitlesArray.length - 1] = "Description";
                    this.logger.debug("columnsArray.length = " + this.columnsArray.length);
                    this.logger.debug("columnsTitlesArray.length = " + this.columnsTitlesArray.length);
                } else {
                    this.logger.warn("No sdc provided so cannot discover columns.");
                    this.columnsArray = null;
                    this.columnsTitlesArray = null;
                }
            }
            if (this.columnsArray != null && this.columnsTitlesArray != null) {
                if (this.returnColumnHeaders != null && this.returnColumnHeaders.length() > 0) {
                    this.returnColumnsArray = this.returnColumnHeaders.split(DEFAULT_SEPARATOR);
                } else if (tempreturn != null) {
                    this.logger.info("No return columns provided thus default to key columns.");
                    this.returnColumnsArray = tempreturn;
                    this.returnColumnHeaders = this.arrayToString(tempreturn, DEFAULT_SEPARATOR).toString();
                } else {
                    this.logger.info("No return columns provided thus default to first in columns.");
                    this.returnColumnsArray = new String[]{this.columnsArray[0]};
                    this.returnColumnHeaders = this.columnsArray[0];
                }
                this.logger.debug("returnColumns = " + this.returnColumnHeaders);
                this.logger.debug("returnColumnsArray.length = " + this.returnColumnsArray.length);
                if (this.queryWhere == null) {
                    this.queryWhere = "";
                }
                this.sortby = this.element.getProperty(PROPERTY_SORTBY);
                if (this.sortby == null) {
                    this.sortby = "";
                }
                if (this.restrictiveWhere == null) {
                    this.restrictiveWhere = "";
                }
                String temp = this.element.getProperty(PROPERTY_MULTISELECT);
                this.logger.debug("temp = " + temp);
                this.selector = temp == null || temp.length() == 0 ? (short)1 : (temp.equalsIgnoreCase("Y") ? (short)0 : 1);
                temp = this.element.getProperty(PROPERTY_BUTTONPOSITION);
                this.logger.debug("temp = " + temp);
                this.buttonPosition = temp == null || temp.length() == 0 ? (short)0 : (temp.equalsIgnoreCase("bottomright") ? (short)3 : (temp.equalsIgnoreCase("bottomleft") ? (short)2 : (temp.equalsIgnoreCase("topright") ? (short)1 : 0)));
                this.logger.debug("buttonPosition = " + this.buttonPosition);
                this.lookupcallback = this.element.getProperty(PROPERTY_LOOKUPCALLBACK);
                if (this.lookupcallback == null) {
                    this.lookupcallback = "";
                }
                this.logger.debug("lookupcallback = " + this.lookupcallback);
                this.detailcallback = this.element.getProperty(PROPERTY_DETAILCALLBACK);
                if (this.detailcallback == null) {
                    this.detailcallback = "";
                }
                this.logger.debug("detailcallback = " + this.detailcallback);
                this.fieldid = this.element.getProperty(PROPERTY_FIELDID);
                if (this.fieldid == null) {
                    this.fieldid = "";
                }
                this.logger.debug("fieldid = " + this.fieldid);
                this.row = this.element.getProperty(PROPERTY_INDEX);
                if (this.row == null) {
                    this.row = "";
                }
                this.logger.debug("row = " + this.row);
                this.dsname = this.element.getProperty(PROPERTY_DSNAME);
                if (this.dsname == null) {
                    this.dsname = "";
                }
                this.logger.debug("dsname = " + this.dsname);
                this.elementId = this.element.getProperty(PROPERTY_ELEMENTID);
                if (this.elementId == null) {
                    this.elementId = "";
                }
                this.logger.debug("elementId = " + this.elementId);
                this.extra = this.element.getProperty(PROPERTY_EXTRA);
                if (this.extra == null) {
                    this.extra = "";
                }
                this.logger.debug("extra = " + this.extra);
                this.groupbyid = this.element.getProperty(PROPERTY_GROUPBY, "");
                this.groupbytext = this.groupbyid.length() > 0 ? this.element.getProperty(PROPERTY_GROUPBYTEXT, "") : "";
                this.toolbarstyle = this.element.getProperty(PROPERTY_TOOLBARSTYLE, "Classic");
                this.showlayout = this.element.getProperty(PROPERTY_SHOWLAYOUT, "Y").equalsIgnoreCase("Y");
                this.title = this.element.getProperty(PROPERTY_TITLE, "LabVantage Lookup");
                isOK = true;
            } else {
                this.logger.error("No column ids provided.");
            }
        } else {
            this.logger.error("No sdcid or tableid provided.");
        }
        return isOK;
    }

    private StringBuffer arrayToString(String[] array, String delimiter) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            if (i > 0) {
                out.append(delimiter);
            }
            out.append(array[i]);
        }
        return out;
    }

    private boolean isUnregisteredSQLPermitted() {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(this.getConnectionId()), SecurityService.isVirtualUser(this.getConnectionId()), SecurityService.isPortalUser(this.getConnectionId()));
        if (policy != null) {
            PropertyList section = policy.getPropertyList("ajax");
            return section.getProperty("allowunregisteredsql", "N").equals("Y");
        }
        return false;
    }

    private String renderToolbar(String theElementId, String theDSName, String theFieldId, String theRow, String theLookupCallback, String theDetailCallback, String theReturnColumns, short theSelectorType, String theExtra, String displaystyle) {
        String selectAction;
        boolean multi;
        this.logger.info("renderToolbar called... ");
        boolean bl = multi = theSelectorType != 1;
        if (theLookupCallback != null && theLookupCallback.length() > 0) {
            this.logger.info("Lookup callback provided.");
            selectAction = "oDataLookup.doLookupAccept('" + theFieldId + "', " + multi + ", '" + theLookupCallback + "', '" + theRow + "' );";
        } else if (theDetailCallback != null && theDetailCallback.length() > 0) {
            this.logger.info("Detail callback provided.");
            selectAction = "oDataLookup.doDetailAccept('" + theFieldId + "', " + multi + ", '" + theDetailCallback + "', '" + theRow + "', '" + theReturnColumns + "', '" + theDSName + "', '" + theElementId + "', '" + theExtra + "' );";
        } else if (theFieldId != null && theFieldId.length() > 0) {
            this.logger.info("Field Id provided.");
            selectAction = "oDataLookup.doFieldAccept( '" + theFieldId + "' );";
        } else {
            this.logger.info("No fieldid, lookupcallback or detailcallback provided.");
            selectAction = "alert('No fieldid, lookupcallback or detailcallback provided.');";
        }
        this.logger.debug("selectButton action set.");
        AdvancedToolbar at = new AdvancedToolbar();
        at.setPageContext(this.pageContext);
        at.setElementid("advancedtoolbar");
        PropertyList properties = new PropertyList();
        properties.setProperty("displaystyle", displaystyle);
        properties.setProperty("showtitle", "N");
        if (!this.showlayout && this.title != null && this.title.length() > 0) {
            properties.setProperty("showtitle", "Y");
            properties.setProperty("pagetitle", this.getTranslationProcessor().translate(this.title));
        } else {
            properties.setProperty("showtitle", "N");
        }
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyList button = new PropertyList();
        button.setProperty("id", "selectButton");
        button.setProperty("type", "User");
        PropertyList commonprops = new PropertyList();
        commonprops.setProperty("text", this.getTranslationProcessor().translate("Select & Return"));
        commonprops.setProperty("image", "WEB-CORE/images/png/SelectAndReturn.png");
        commonprops.setProperty("imagelarge", "WEB-CORE/images/png32/SelectAndReturn.png");
        commonprops.setProperty("show", "Y");
        commonprops.setProperty("ribbonstyle", "Large");
        commonprops.setProperty("appearance", "standard");
        commonprops.setProperty("tip", this.getTranslationProcessor().translate("Select and Close"));
        commonprops.setProperty("mode", "Button");
        PropertyList userbuttonprops = new PropertyList();
        userbuttonprops.setProperty("action", selectAction);
        button.setProperty("commonprops", commonprops);
        button.setProperty("userbuttonprops", userbuttonprops);
        buttons.add(button);
        button = new PropertyList();
        button.setProperty("id", "cancelButton");
        button.setProperty("type", "User");
        commonprops = new PropertyList();
        commonprops.setProperty("text", this.getTranslationProcessor().translate("Cancel"));
        commonprops.setProperty("image", "WEB-CORE/images/png/Cancel.png");
        commonprops.setProperty("imagelarge", "WEB-CORE/images/png32/Cancel.png");
        commonprops.setProperty("show", "Y");
        commonprops.setProperty("ribbonstyle", "Large");
        commonprops.setProperty("appearance", "standard");
        commonprops.setProperty("tip", this.getTranslationProcessor().translate("Cancel and Close"));
        commonprops.setProperty("mode", "Button");
        userbuttonprops = new PropertyList();
        userbuttonprops.setProperty("action", "oDataLookup.close();");
        button.setProperty("commonprops", commonprops);
        button.setProperty("userbuttonprops", userbuttonprops);
        buttons.add(button);
        properties.setProperty("buttons", buttons);
        properties.setProperty("rendermode", "Button");
        at.setElementProperties(properties);
        StringBuffer out = new StringBuffer();
        if (displaystyle.equalsIgnoreCase("classic")) {
            out.append("<table border=0 cellpadding=0 cellspacing=0 height=22 width=\"100%\"><tbody><tr class=\"pagebuttonsection\"><td>");
            if (!this.showlayout && this.title != null && this.title.length() > 0) {
                out.append("<table border=0 cellpadding=0 cellspacing=0 width=\"100%\"><tbody><tr class=\"pagebuttonsection\"><td>");
                out.append("<td valign=\"middle\" width=\"20%\" class=\"layout_pagetitle\" id=\"pagetitle\">").append(this.title).append("</td>");
                out.append("<td>");
            }
            out.append(at.getHtml());
            if (!this.showlayout && this.title != null && this.title.length() > 0) {
                out.append("</td></tr>\n");
                out.append("</tbody></table>");
            }
        } else {
            out.append("<table border=0 cellpadding=0 cellspacing=0 width=\"100%\"><tbody><tr class=\"pagebuttonsection\"><td>");
            out.append(at.getHtml());
        }
        out.append("</td></tr>\n");
        out.append("<tr><td class=\"layout_pageshadow\">\n");
        out.append("\n</td></tr>\n");
        out.append("</tbody></table>");
        return out.toString();
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public String getHtml() {
        this.logger.info("getHtml called... ");
        StringBuffer content = new StringBuffer();
        if (this.loadProperties()) {
            this.logger.debug("properties loaded...");
            DataSet data = this.getData(this.tableid, this.queryWhere, this.restrictiveWhere, this.columnsArray, this.returnColumnsArray, this.pageContext);
            if (data != null) {
                if (Trace.isDebugEnabled()) {
                    data.showData();
                }
                this.renderHTML(data, this.columnsArray, this.columnsTitlesArray, this.columnsTranslateArray, this.returnColumnsArray, this.selector, content);
                StringBuffer html = this.renderPage(content);
                return html.toString();
            }
            this.logger.error("Could not obtain data.");
            if (this.pageContext != null) {
                return this.getTranslationProcessor().translate("Could not obtain data.");
            }
            return "Could not obtain data.";
        }
        this.logger.error("Could not load required properties.");
        if (this.pageContext != null) {
            return this.getTranslationProcessor().translate("Could not load required properties.");
        }
        return "Could not load required properties.";
    }

    private StringBuffer renderPage(StringBuffer content) {
        this.logger.info("renderPage called... ");
        StringBuffer buffer = new StringBuffer();
        StringBuffer html = new StringBuffer();
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            buffer.append(this.getError());
        } else if (html.length() > 0) {
            buffer.append(content);
        } else {
            buffer.append("");
            this.logger.debug("No HTML to return.");
        }
        html.append("<script language='JavaScript' type='text/javascript' src='").append(JS_FILE).append("'></script>");
        html.append("<script>var _dataLoaded = false;</script>");
        html.append("<table cellpadding=0 cellspacing=0 border=0 width=100%>\n");
        switch (this.buttonPosition) {
            case 2: {
                html.append("<tr height='100%'>\n<td valign=top>\n");
                html.append("<div style='border-bottom:solid 5 #6495ED;  width:100%;height:100%;overflow-y:auto;'>\n");
                html.append(content);
                html.append("\n</div>\n");
                html.append("</td>\n</tr>\n");
                html.append("<tr>\n<td valign=top align=left>\n");
                html.append(this.renderToolbar(this.elementId, this.dsname, this.fieldid, this.row, this.lookupcallback, this.detailcallback, this.returnColumnHeaders, this.selector, this.extra, this.toolbarstyle));
                html.append("\n</div>\n");
                html.append("\n</td>\n</tr>\n");
                break;
            }
            case 3: {
                html.append("<tr>\n<td valign=top>\n");
                html.append("<div style='border-bottom:solid 5 #6495ED;  width:100%;height:100%;overflow-y:auto;'>\n");
                html.append(content);
                html.append("\n</div>\n");
                html.append("</td>\n</tr>\n");
                html.append("<tr height=22>\n<td valign=top align=right>\n");
                html.append(this.renderToolbar(this.elementId, this.dsname, this.fieldid, this.row, this.lookupcallback, this.detailcallback, this.returnColumnHeaders, this.selector, this.extra, this.toolbarstyle));
                html.append("\n</td>\n</tr>\n");
                break;
            }
            case 1: {
                html.append("<tr height=22>\n<td valign=top align=right>\n");
                html.append(this.renderToolbar(this.elementId, this.dsname, this.fieldid, this.row, this.lookupcallback, this.detailcallback, this.returnColumnHeaders, this.selector, this.extra, this.toolbarstyle));
                html.append("\n</td>\n</tr>\n");
                html.append("<tr >\n<td valign=top>\n");
                html.append("<div style='width:100%;height:100%;overflow-y:auto;'>\n");
                html.append(content);
                html.append("\n</div>\n");
                html.append("\n</td>\n</tr>\n");
                break;
            }
            default: {
                html.append("<tr height=22>\n<td valign=top align=left>\n");
                html.append(this.renderToolbar(this.elementId, this.dsname, this.fieldid, this.row, this.lookupcallback, this.detailcallback, this.returnColumnHeaders, this.selector, this.extra, this.toolbarstyle));
                html.append("\n</td>\n</tr>\n");
                html.append("<tr>\n<td valign=top>\n");
                html.append("<div style='width:100%;height:100%;overflow-y:auto;'>\n");
                html.append(content);
                html.append("\n</div>\n");
                html.append("\n</td>\n</tr>\n");
            }
        }
        html.append("</table>\n");
        return html;
    }
}

