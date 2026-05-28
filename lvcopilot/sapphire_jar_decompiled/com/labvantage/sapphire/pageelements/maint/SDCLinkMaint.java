/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.lookup.DataLookup;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.MiscUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCLinkMaint
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 100445 $";
    static final String PROPERTYHANDLERCLASS = "com.labvantage.sapphire.pageelements.maint.SDCLinkMaintPropertyHandler";
    static final int PERFORMANCERSETCUTOFF = 30;
    static final int LINKTYPE_DETAIL = 0;
    static final int LINKTYPE_FKEY = 1;
    static final int LINKTYPE_RELATIONAL = 2;
    static final String PROPERTYLIST_LINK = "link";
    static final String PROPERTYLIST_DETAIL = "detail";
    static final String PROPERTYLIST_FKEY = "fkey";
    static final String PROPERTYLIST_RELATION = "relation";
    static final String PROPERTY_SDCID = "sdcid";
    static final String PROPERTY_FROMSDCID = "fromsdc";
    static final String PROPERTY_TOSDCID = "tosdc";
    static final String PROPERTY_FROMCOLUMN1 = "fromcolumn1";
    static final String PROPERTY_TOCOLUMN1 = "tocolumn1";
    static final String PROPERTY_FROMCOLUMN2 = "fromcolumn2";
    static final String PROPERTY_TOCOLUMN2 = "tocolumn2";
    static final String PROPERTY_FROMCOLUMN3 = "fromcolumn3";
    static final String PROPERTY_TOCOLUMN3 = "tocolumn3";
    static final String PROPERTY_LINKID = "linkid";
    static final String PROPERTY_BUTTONPLACEMENT = "buttonplacement";
    static final String PROPERTY_ADDITION = "addition";
    static final String PROPERTY_EDIT = "edit";
    static final String PROPERTY_TYPE = "type";
    static final String PROPERTY_ADDLOOKUPPAGE = "lookuppage";
    static final String PROPERTYLIST_COLUMNS = "columns";
    static final String PROPERTY_COLUMNID = "columnid";
    static final String PROPERTY_MAPCOLUMNID = "mapcolumnid";
    static final String PROPERTY_TITLE = "title";
    static final String PROPERTY_MODE = "mode";
    static final String PROPERTY_QUERYWHERE = "querywhere";
    static final String PROPERTY_TABLEID = "tableid";
    static final String PROPERTY_MULTISELECT = "multiselect";
    static final String PROPERTY_UNIQUE = "unique";
    static final String PROPERTY_SHOWPROGRESS = "showprogress";
    static final String PROPERTY_LOOKUPCALLBACK = "lookupcallback";
    static final String PROPERTY_VIEWONLY = "viewonly";
    static final String PROPERTY_GROUP = "group";
    static final String PROPERTY_CELLCSSCLASS = "cellcssclass";
    static final String PROPERTY_TAB = "tab";
    static final String PROPERTY_TEXT = "text";
    static final String PROPERTY_FIXEDCOLS = "fixedcols";
    static final String PROPERTYLIST_FILTER = "filter";
    static final String PROPERTY_VALUE = "value";
    static final String PROPERTY_SELECTORTYPE = "selectortype";
    static final String PROPERTY_RESTRICTIVEWHERE = "restrictivewhere";
    static final String PROPERTY_DIALOGTYPE = "dialogtype";
    static final String PROPERTY_MERGE = "merge";
    static final String PROPERTY_EDITPAGE = "editpage";
    static final String PROPERTY_EDITTARGET = "edittarget";
    static final String PROPERTY_FKWHERE = "fkwhere";
    static final String PROPERTY_STYLE = "style";
    static final String PROPERTY_ADVSORTBY = "advsortby";
    static final String PROPERTY_ASC_DESC = "asc_desc";
    static final String PROPERTY_RETURNVALUE = "returnvalue";
    static final String PROPERTY_AUTOADDNEWROW = "autoaddnewrow";
    static final String DEFAULT_BUTTONPLACEMENT = "topleft";
    static final String DEFAULT_LOOKUPPAGE = "rc?command=file&file=WEB-CORE/lookup/datalookup.jsp";
    static final String DEFAULT_LOOKUPLISTPAGE = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
    static final String DEFAULT_EDITTARGET = "New Window";
    static final String JS_OBJECT = "dataView";
    static final String JS_ADDFUNCTION = "addDataViewRow";
    static final String JS_EDITFUNCTION = "editDataViewRow";
    static final String JS_ADDFROMLOOKUPFUNCTION = "addDataViewRowFromLookup";
    static final String JS_REMOVEFUNCTION = "deleteDataviewRow";
    static final String JS_UPFUNCTION = "sequenceUp";
    static final String JS_DOWNFUNCTION = "sequenceDown";
    static final String JS_SETEQUENCEFUNCTION = "setSequence";
    static final String JS_NOOPFUNCTION = "noop";
    static final String JS_LOCKEDFUNCTION = "lockedCall";
    static final String JS_NOTAVAILABLEFUNCTION = "notAvailable";
    static final String TABPREFIX = "_tabitem";
    static final String TABSTYLEPREFIX = "_tabstyle";
    private int linkType = -1;
    private String fromsdcId;
    private String tosdcId;
    private String fromcolumn1;
    private String tocolumn1;
    private String fromcolumn2;
    private String tocolumn2;
    private String fromcolumn3;
    private String tocolumn3;
    private String linkId;
    private String buttonPlacement;
    private String addLookupPage;
    private boolean usingDataLookup = false;
    private String lookupColumns;
    private String lookupColumnTitles;
    private String lookupReturnColumns;
    private String lookupColumnMap;
    private boolean addFromLookup;
    private boolean autoAddRow;
    private boolean multiSelect;
    private String lookupSdcid;
    private String lookupTableid;
    private String lookupQueryWhere;
    private String lookupUniqueColumns;
    private boolean showProgress;
    private String lookupCallback;
    private boolean useCustomLookup;
    private boolean viewOnly;
    PropertyList lookupDirectives = new PropertyList();
    String lookupRestrictiveWhere;
    boolean lookupUseSapphrieDialog;
    boolean mergeLookup;
    private String editPage;
    private String editTarget;
    private String fkWhereClause;
    private boolean sorted;
    private boolean paging = false;
    private int pagefrom = 1;
    private int pagecount = 0;
    private int totalpages = 0;
    private int currentpage = 0;
    private boolean pageNext = false;
    private boolean pagePrev = false;

    private boolean loadProperties() {
        String temp;
        this.logger.debug("loadProperties called...");
        if (this.requestContext != null && this.requestContext.getProperty("sdclinkmaintpagefrom_" + this.elementid).length() > 0) {
            try {
                this.pagefrom = Integer.parseInt(this.requestContext.getProperty("sdclinkmaintpagefrom_" + this.elementid));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean theReturn = false;
        if (this.element.getProperty(PROPERTY_STYLE, "").length() == 0) {
            this.element.setProperty(PROPERTY_STYLE, "GridWithCheckBox");
        }
        this.buttonPlacement = this.element.getProperty(PROPERTY_BUTTONPLACEMENT);
        if (this.buttonPlacement == null || this.buttonPlacement.length() == 0) {
            this.logger.debug("Defaulting button placement.");
            this.buttonPlacement = DEFAULT_BUTTONPLACEMENT;
        }
        this.logger.debug("buttonPlacement = " + this.buttonPlacement);
        this.fkWhereClause = this.element.getProperty(PROPERTY_FKWHERE, "");
        this.logger.debug("fkWhereClause = " + this.fkWhereClause);
        if (this.element.containsKey(PROPERTY_ADVSORTBY) && this.element.getCollection(PROPERTY_ADVSORTBY).size() > 0) {
            this.sorted = true;
        }
        this.addFromLookup = false;
        PropertyList addtionList = this.element.getPropertyList(PROPERTY_ADDITION);
        if (addtionList != null) {
            temp = addtionList.getProperty(PROPERTY_TYPE);
            if (temp != null && temp.length() > 0 && temp.equalsIgnoreCase("use lookup")) {
                this.autoAddRow = addtionList.getProperty(PROPERTY_AUTOADDNEWROW, "N").equalsIgnoreCase("Y");
                this.addFromLookup = true;
                this.lookupSdcid = addtionList.getProperty(PROPERTY_SDCID);
                if (this.lookupSdcid == null) {
                    this.lookupSdcid = "";
                }
                this.logger.debug("lookupSdcid = " + this.lookupSdcid);
                this.lookupTableid = addtionList.getProperty(PROPERTY_TABLEID);
                if (this.lookupTableid == null) {
                    this.lookupTableid = "";
                }
                this.logger.debug("lookupTableid = " + this.lookupTableid);
                this.addLookupPage = addtionList.getProperty(PROPERTY_ADDLOOKUPPAGE).trim();
                if (this.addLookupPage == null || this.addLookupPage.length() == 0) {
                    if (this.lookupSdcid.length() > 0 && this.lookupTableid.length() == 0) {
                        this.addLookupPage = DEFAULT_LOOKUPLISTPAGE;
                    } else {
                        this.addLookupPage = DEFAULT_LOOKUPPAGE;
                        this.usingDataLookup = true;
                    }
                    this.useCustomLookup = false;
                } else {
                    this.useCustomLookup = true;
                    if (this.addLookupPage.toLowerCase().contains(PROPERTY_RESTRICTIVEWHERE)) {
                        int pos = this.addLookupPage.indexOf("restrictivewhere=");
                        int pos2 = this.addLookupPage.indexOf("&", pos + 17);
                        this.addLookupPage = this.addLookupPage.substring(0, pos + 17) + EncryptDecrypt.obfsql(this.addLookupPage.substring(pos + 17, pos2 > -1 ? pos2 : this.addLookupPage.length())) + (pos2 > -1 ? this.addLookupPage.substring(pos2) : "");
                    }
                }
                this.logger.debug("addLookupPage = " + this.addLookupPage);
                this.logger.debug("useCustomLookup = " + this.useCustomLookup);
                this.logger.debug("usingDataLookup = " + this.usingDataLookup);
                temp = addtionList.getProperty(PROPERTY_MULTISELECT);
                this.multiSelect = temp == null || temp.length() == 0 ? true : !temp.equalsIgnoreCase("n");
                this.logger.debug("multiSelect = " + this.multiSelect);
                if (!this.useCustomLookup && this.lookupSdcid.length() > 0 && this.lookupTableid.length() == 0) {
                    this.mergeLookup = true;
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    String lookupDirectivesCacheKey = "lookupDirectivesCacheKey_" + this.lookupSdcid + ";" + this.multiSelect;
                    this.lookupDirectives = (PropertyList)this.pageContext.getAttribute(lookupDirectivesCacheKey);
                    if (this.lookupDirectives == null) {
                        this.lookupDirectives = SDITagUtil.getLookupPageDirectives(this.lookupSdcid, sdcProcessor.getProperty(this.lookupSdcid, "keycolid1"), sdcProcessor.getProperty(this.lookupSdcid, "keycolid2"), sdcProcessor.getProperty(this.lookupSdcid, "versionedflag").equalsIgnoreCase("Y"), this.multiSelect ? "checkbox" : "radiobutton", false, "", "", sdcProcessor.getProperty(this.lookupSdcid, PROPERTY_TABLEID), "", "", new PropertyListCollection(), true, new StringBuffer(), new StringBuffer(), this.getTranslationProcessor(), sdcProcessor);
                        this.pageContext.setAttribute(lookupDirectivesCacheKey, (Object)this.lookupDirectives);
                        this.logger.debug(this.logName, "Cached lookupDirectives cachekey: " + lookupDirectivesCacheKey);
                    }
                } else {
                    this.mergeLookup = addtionList.getProperty(PROPERTY_MERGE, "N").equalsIgnoreCase("Y");
                    if (this.multiSelect) {
                        this.lookupDirectives.setProperty(PROPERTY_SELECTORTYPE, "checkbox");
                    } else {
                        this.lookupDirectives.setProperty(PROPERTY_SELECTORTYPE, "radiobutton");
                    }
                }
                this.logger.debug("mergeLookup = " + this.mergeLookup);
                this.lookupCallback = addtionList.getProperty(PROPERTY_LOOKUPCALLBACK);
                if (this.lookupCallback == null || this.lookupCallback.length() == 0) {
                    this.lookupCallback = "";
                }
                this.logger.debug("lookupCallback = " + this.lookupCallback);
                this.lookupUseSapphrieDialog = addtionList.getProperty(PROPERTY_DIALOGTYPE, "Browser Popup").equalsIgnoreCase("Sapphire Dialog");
                this.logger.debug("lookupUseSapphrieDialog = " + this.lookupUseSapphrieDialog);
                if (this.lookupUseSapphrieDialog) {
                    PropertyList layout = new PropertyList();
                    layout.setProperty("hidetitle", "Y");
                    this.lookupDirectives.setProperty("layout", layout);
                }
                this.lookupRestrictiveWhere = addtionList.getProperty(PROPERTY_RESTRICTIVEWHERE, "");
                this.logger.debug("lookupRestrictiveWhere = " + this.lookupRestrictiveWhere);
                this.lookupRestrictiveWhere = EncryptDecrypt.obfsql(this.lookupRestrictiveWhere);
                this.lookupQueryWhere = addtionList.getProperty(PROPERTY_QUERYWHERE);
                if (this.lookupQueryWhere == null) {
                    this.lookupQueryWhere = "";
                }
                this.logger.debug("lookupQueryWhere = " + this.lookupQueryWhere);
                StringBuffer columnsBuffer = new StringBuffer();
                StringBuffer columnTitlesBuffer = new StringBuffer();
                StringBuffer returnsBuffer = new StringBuffer();
                StringBuffer mapBuffer = new StringBuffer();
                StringBuffer uniqueBuffer = new StringBuffer();
                PropertyListCollection columns = addtionList.getCollection(PROPERTYLIST_COLUMNS);
                if (columns != null) {
                    PropertyListCollection pd_cols = (PropertyListCollection)columns.clone();
                    for (int index = 0; index < pd_cols.size(); ++index) {
                        PropertyList tempList = pd_cols.getPropertyList(index);
                        String tempColumnId = tempList.getProperty(PROPERTY_COLUMNID);
                        if (tempColumnId.contains(" ")) {
                            tempColumnId = tempColumnId.substring(tempColumnId.lastIndexOf(" ") + 1);
                        }
                        if (tempColumnId != null && tempColumnId.length() > 0) {
                            this.logger.debug("tempColumnId = " + tempColumnId);
                            String tempMode = tempList.getProperty(PROPERTY_MODE);
                            this.logger.debug("tempMode = " + tempMode);
                            String tempTitle = tempList.getProperty(PROPERTY_TITLE);
                            this.logger.debug("tempTitle = " + tempTitle);
                            String tempMap = tempList.getProperty(PROPERTY_MAPCOLUMNID);
                            this.logger.debug("tempMap = " + tempMap);
                            tempList.remove(PROPERTY_MAPCOLUMNID);
                            String tempUnique = tempList.getProperty(PROPERTY_UNIQUE);
                            this.logger.debug("tempUnique = " + tempUnique);
                            tempList.remove(PROPERTY_UNIQUE);
                            if (tempMode == null || tempMode.length() == 0) {
                                tempList.setProperty(PROPERTY_MODE, "Display Value");
                                tempList.setProperty(PROPERTY_RETURNVALUE, "Y");
                                this.logger.debug("mode is blank therefore default to display and return.");
                                MiscUtil.MiscString.appendDelimeteredString(returnsBuffer, tempColumnId, ";");
                                MiscUtil.MiscString.appendDelimeteredString(columnsBuffer, tempColumnId, ";");
                                if (tempTitle != null && tempTitle.length() > 0) {
                                    MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempTitle, ";");
                                } else {
                                    this.logger.debug("title blank thus default to column id.");
                                    MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempColumnId, ";");
                                }
                                if (tempMap != null && tempMap.length() > 0) {
                                    MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempMap, ";");
                                    if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                                    MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempMap, ";");
                                    continue;
                                }
                                this.logger.debug("map blank thus default to column id.");
                                MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempColumnId, ";");
                                if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                                MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempColumnId, ";");
                                continue;
                            }
                            if (tempMode.equalsIgnoreCase("Display Only")) {
                                tempList.setProperty(PROPERTY_MODE, "Display Value");
                                tempList.setProperty(PROPERTY_RETURNVALUE, "N");
                                MiscUtil.MiscString.appendDelimeteredString(columnsBuffer, tempColumnId, ";");
                                if (tempTitle != null && tempTitle.length() > 0) {
                                    MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempTitle, ";");
                                    continue;
                                }
                                this.logger.debug("title blank thus default to column id.");
                                MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempColumnId, ";");
                                continue;
                            }
                            if (tempMode.equalsIgnoreCase("Return Only")) {
                                tempList.setProperty(PROPERTY_MODE, "Hidden Value");
                                tempList.setProperty(PROPERTY_RETURNVALUE, "Y");
                                MiscUtil.MiscString.appendDelimeteredString(returnsBuffer, tempColumnId, ";");
                                if (tempMap != null && tempMap.length() > 0) {
                                    MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempMap, ";");
                                    if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                                    MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempMap, ";");
                                    continue;
                                }
                                this.logger.debug("map blank thus default to column id.");
                                MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempColumnId, ";");
                                if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                                MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempColumnId, ";");
                                continue;
                            }
                            if (tempMode.equalsIgnoreCase("Hidden Only")) {
                                tempList.setProperty(PROPERTY_MODE, "Hidden Value");
                                tempList.setProperty(PROPERTY_RETURNVALUE, "N");
                                continue;
                            }
                            tempList.setProperty(PROPERTY_MODE, "Display Value");
                            tempList.setProperty(PROPERTY_RETURNVALUE, "Y");
                            MiscUtil.MiscString.appendDelimeteredString(returnsBuffer, tempColumnId, ";");
                            MiscUtil.MiscString.appendDelimeteredString(columnsBuffer, tempColumnId, ";");
                            if (tempTitle != null && tempTitle.length() > 0) {
                                MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempTitle, ";");
                            } else {
                                this.logger.debug("title blank thus default to column id.");
                                MiscUtil.MiscString.appendDelimeteredString(columnTitlesBuffer, tempColumnId, ";");
                            }
                            if (tempMap != null && tempMap.length() > 0) {
                                MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempMap, ";");
                                if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                                MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempMap, ";");
                                continue;
                            }
                            this.logger.debug("map blank thus default to column id.");
                            MiscUtil.MiscString.appendDelimeteredString(mapBuffer, tempColumnId, ";");
                            if (tempUnique == null || !tempUnique.equalsIgnoreCase("y")) continue;
                            MiscUtil.MiscString.appendDelimeteredString(uniqueBuffer, tempColumnId, ";");
                            continue;
                        }
                        this.logger.debug("Blank column id found.");
                    }
                    this.lookupColumns = columnsBuffer.length() > 0 ? columnsBuffer.toString() : "";
                    this.lookupReturnColumns = returnsBuffer.length() > 0 ? returnsBuffer.toString() : "";
                    this.lookupColumnTitles = columnTitlesBuffer.length() > 0 ? columnTitlesBuffer.toString() : "";
                    this.lookupColumnMap = mapBuffer.length() > 0 ? mapBuffer.toString() : "";
                    this.lookupUniqueColumns = uniqueBuffer.length() > 0 ? uniqueBuffer.toString() : "";
                    this.lookupDirectives.setProperty(PROPERTYLIST_COLUMNS, pd_cols);
                } else {
                    this.logger.debug("columns not found.");
                }
            } else {
                this.autoAddRow = addtionList.getProperty(PROPERTY_AUTOADDNEWROW, "N").equalsIgnoreCase("Y");
                this.addLookupPage = "";
                this.lookupColumns = "";
                this.lookupColumnTitles = "";
                this.lookupReturnColumns = "";
                this.lookupUniqueColumns = "";
                this.lookupColumnMap = "";
            }
        } else {
            this.autoAddRow = false;
            this.addLookupPage = "";
            this.lookupColumns = "";
            this.lookupColumnTitles = "";
            this.lookupReturnColumns = "";
            this.lookupUniqueColumns = "";
            this.lookupColumnMap = "";
        }
        this.logger.debug("addFromLookup = " + this.addFromLookup);
        this.logger.debug("addLookupPage = " + this.addLookupPage);
        this.logger.debug("lookupColumns = " + this.lookupColumns);
        this.logger.debug("lookupColumnTitles = " + this.lookupColumnTitles);
        this.logger.debug("lookupReturnColumns = " + this.lookupReturnColumns);
        this.logger.debug("lookupUniqueColumns = " + this.lookupUniqueColumns);
        this.logger.debug("lookupColumnMap = " + this.lookupColumnMap);
        PropertyList editList = this.element.getPropertyList(PROPERTY_EDIT);
        if (editList != null) {
            this.editPage = editList.getProperty(PROPERTY_EDITPAGE, "");
            this.editTarget = editList.getProperty(PROPERTY_EDITTARGET, DEFAULT_EDITTARGET);
        } else {
            this.editPage = "";
            this.editTarget = DEFAULT_EDITTARGET;
        }
        this.logger.debug("editPage = " + this.editPage);
        this.logger.debug("editTarget = " + this.editTarget);
        this.fromsdcId = this.element.getProperty(PROPERTY_SDCID);
        if (this.fromsdcId != null && this.fromsdcId.length() > 0) {
            this.logger.debug("sdcId = " + this.fromsdcId);
            PropertyList link = this.element.getPropertyList(PROPERTYLIST_LINK);
            if (link != null) {
                PropertyList rel;
                PropertyList fkey;
                PropertyList detail = link.getPropertyList(PROPERTYLIST_DETAIL);
                if (detail != null) {
                    this.linkId = detail.getProperty(PROPERTY_LINKID);
                    if (this.linkId != null && this.linkId.length() > 0) {
                        this.logger.debug("detail.linkId = " + this.linkId);
                        this.logger.debug("Link Type = DETAIL");
                        this.linkType = 0;
                        theReturn = true;
                    }
                }
                if (this.linkType == -1 && (fkey = link.getPropertyList(PROPERTYLIST_FKEY)) != null) {
                    this.tosdcId = fkey.getProperty(PROPERTY_TOSDCID);
                    if (this.tosdcId != null && this.tosdcId.length() > 0) {
                        this.logger.debug("fkey.sdcId = " + this.tosdcId);
                        this.linkId = fkey.getProperty(PROPERTY_LINKID);
                        if (this.linkId != null && this.linkId.length() > 0) {
                            this.logger.debug("fkey.linkId = " + this.linkId);
                            this.logger.debug("Link Type = FKEY");
                            this.linkType = 1;
                            theReturn = true;
                        }
                    }
                }
                if (this.linkType == -1 && (rel = link.getPropertyList(PROPERTYLIST_RELATION)) != null) {
                    this.tosdcId = rel.getProperty(PROPERTY_TOSDCID);
                    if (this.tosdcId != null && this.tosdcId.length() > 0) {
                        this.logger.debug("relation.tosdcId = " + this.tosdcId);
                        this.fromcolumn1 = rel.getProperty(PROPERTY_FROMCOLUMN1);
                        if (this.fromcolumn1 != null && this.fromcolumn1.length() > 0) {
                            this.logger.debug("relation.fromcolumn1 = " + this.fromcolumn1);
                            this.tocolumn1 = rel.getProperty(PROPERTY_TOCOLUMN1);
                            if (this.tocolumn1 != null && this.tocolumn1.length() > 0) {
                                this.logger.debug("relation.tocolumn = " + this.tocolumn1);
                                this.logger.debug("Link Type = RELATIONAL");
                                this.fromcolumn2 = rel.getProperty(PROPERTY_FROMCOLUMN2);
                                this.fromcolumn3 = rel.getProperty(PROPERTY_FROMCOLUMN3);
                                this.tocolumn2 = rel.getProperty(PROPERTY_TOCOLUMN2);
                                this.tocolumn3 = rel.getProperty(PROPERTY_TOCOLUMN3);
                                this.linkType = 2;
                                theReturn = true;
                            }
                        }
                    }
                }
            }
            if (this.linkType == -1) {
                this.linkId = this.element.getProperty(PROPERTY_LINKID);
                if (this.linkId != null && this.linkId.length() > 0) {
                    this.logger.debug("linkId = " + this.linkId);
                    this.logger.debug("Link Type = DETAIL");
                    this.linkType = 0;
                    theReturn = true;
                }
            }
        }
        this.showProgress = (temp = this.element.getProperty(PROPERTY_SHOWPROGRESS)) == null || temp.length() == 0 ? false : temp.equalsIgnoreCase("y");
        this.logger.debug("showProgress = " + this.showProgress);
        temp = this.element.getProperty(PROPERTY_VIEWONLY);
        this.viewOnly = temp == null || temp.length() == 0 ? false : temp.equalsIgnoreCase("y");
        this.logger.debug("viewOnly = " + this.viewOnly);
        return theReturn;
    }

    private void renderDataView(StringBuffer html, String datasetName, DataSet dataset, String[] keyCols, String sdcid) {
        DataView dataView;
        this.logger.debug("renderDataView called...");
        PropertyListCollection filtercol = this.element.getCollection(PROPERTYLIST_FILTER);
        if (filtercol != null && filtercol.size() > 0) {
            QueryData qdtemp;
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < filtercol.size(); ++i) {
                PropertyList entry = filtercol.getPropertyList(i);
                String column = entry.getProperty(PROPERTY_COLUMNID, "");
                if (column.length() <= 0) continue;
                String value = entry.getProperty(PROPERTY_VALUE, "");
                value = ElementUtil.evaluateExpression(datasetName, -1, "", value, this.sdiInfo, this.getTranslationProcessor(), this.requestContext.getPropertyList());
                filter.put(column, value);
            }
            dataset = dataset.getFilteredDataSet(filter);
            if (this.sdiInfo != null && (qdtemp = this.sdiInfo.getQueryData(datasetName)) != null) {
                qdtemp.setQueryData(dataset);
            }
        }
        if (this.sdiInfo != null) {
            if (this.sdiInfo.getQueryData(datasetName) == null) {
                this.sdiInfo.setDataSet(datasetName, dataset);
                dataView = new DataView(this.pageContext, datasetName, this.sdiInfo, "[default]", this.getConnectionId());
            } else {
                dataView = new DataView(this.pageContext, datasetName, this.sdiInfo, "[default]", this.getConnectionId());
            }
        } else {
            dataView = new DataView(this.pageContext, datasetName, dataset, "[default]", this.getConnectionId());
            dataView.getSDIInfo().setSdcid(sdcid);
        }
        dataView.setElementid(this.elementid);
        dataView.setElementProperties(this.element);
        dataView.setKeyCols(keyCols);
        dataView.setRenderTagsJS(false);
        if (sdcid != null && sdcid.length() > 0) {
            dataView.setSDCId(sdcid);
        }
        html.append(dataView.getHtml());
        html.append("<div id=\"__").append(this.elementid).append("_norows\" style=\"display:").append("none").append(";\" >");
        if (this.pageContext != null) {
            html.append(this.getTranslationProcessor().translate("No records found"));
        } else {
            html.append("No records found");
        }
        html.append("</div>");
        if (dataset.getRowCount() == 0) {
            html.append("<script>sapphire.events.attachEvent(window,'onload',function(){dataView.toggleNoRecordsMsg('" + this.elementid + "',true)});</script>");
        }
    }

    private boolean checkLockState(String datasetName) {
        boolean theReturn;
        this.logger.debug("checkLockState called...");
        try {
            DataSet data = this.sdiInfo.getDataSet(datasetName);
            if (data.size() == 0) {
                data = this.sdiInfo.getDataSet("primary");
            }
            if (data != null) {
                String lockedBy = data.getValue(0, "__lockedby", "");
                if (lockedBy == null || lockedBy.length() == 0) {
                    theReturn = false;
                    this.logger.debug("Not locked.");
                } else {
                    theReturn = true;
                    this.logger.debug("Locked by " + lockedBy + ".");
                }
            } else {
                theReturn = false;
            }
        }
        catch (Exception e) {
            theReturn = true;
            this.logger.warn("Could not obtain lock information therefore default to locked.");
        }
        return theReturn;
    }

    private String parseExpressionsFromString(String input, String elementid, String datasetname, String sdcid, String tosdcid, String linkid, String[] keycols, DataSet primary) {
        String[] tokens = StringUtil.getExpressionTokens(input);
        String output = input;
        for (int i = 0; i < tokens.length; ++i) {
            String tok = tokens[i];
            int pos = input.indexOf(tok);
            int obfpos = input.indexOf("{@}");
            boolean obfuscated = false;
            if (obfpos != -1 && pos > obfpos) {
                obfuscated = true;
            }
            if (tok.equalsIgnoreCase("keyid1")) {
                if (keycols.length > 0 && primary.getRowCount() > 0) {
                    output = StringUtil.replaceAll(output, "[keyid1]", obfuscated ? "[*@" + primary.getValue(0, keycols[0], "") + "@*]" : primary.getValue(0, keycols[0], ""));
                    continue;
                }
                output = StringUtil.replaceAll(output, "[keyid1]", "");
                continue;
            }
            if (tok.equalsIgnoreCase("keyid2")) {
                if (keycols.length > 1 && primary.getRowCount() > 0) {
                    output = StringUtil.replaceAll(output, "[keyid2]", obfuscated ? "[*@" + primary.getValue(0, keycols[1], "") + "@*]" : primary.getValue(0, keycols[1], ""));
                    continue;
                }
                output = StringUtil.replaceAll(output, "[keyid2]", "");
                continue;
            }
            if (tok.equalsIgnoreCase("keyid3")) {
                if (keycols.length > 2 && primary.getRowCount() > 0) {
                    output = StringUtil.replaceAll(output, "[keyid3]", obfuscated ? "[*@" + primary.getValue(0, keycols[2], "") + "@*]" : primary.getValue(0, keycols[2], ""));
                    continue;
                }
                output = StringUtil.replaceAll(output, "[keyid3]", "");
                continue;
            }
            if (tok.equalsIgnoreCase(PROPERTY_SDCID)) {
                output = StringUtil.replaceAll(output, "[sdcid]", obfuscated ? "[*@" + sdcid + "@*]" : sdcid);
                continue;
            }
            if (tok.equalsIgnoreCase("tosdcid")) {
                output = StringUtil.replaceAll(output, "[tosdcid]", obfuscated ? "[*@" + tosdcid + "@*]" : tosdcid);
                continue;
            }
            if (tok.equalsIgnoreCase("fromsdcid")) {
                output = StringUtil.replaceAll(output, "[fromsdcid]", obfuscated ? "[*@" + sdcid + "@*]" : sdcid);
                continue;
            }
            if (tok.equalsIgnoreCase(PROPERTY_LINKID)) {
                output = StringUtil.replaceAll(output, "[linkid]", obfuscated ? "[*@" + linkid + "@*]" : linkid);
                continue;
            }
            if (tok.equalsIgnoreCase("element")) {
                output = StringUtil.replaceAll(output, "[element]", obfuscated ? "[*@" + elementid + "@*]" : elementid);
                continue;
            }
            if (tok.equalsIgnoreCase("elementid")) {
                output = StringUtil.replaceAll(output, "[elementid]", obfuscated ? "[*@" + elementid + "@*]" : elementid);
                continue;
            }
            if (tok.equalsIgnoreCase("dataset")) {
                output = StringUtil.replaceAll(output, "[dataset]", obfuscated ? "[*@" + datasetname + "@*]" : datasetname);
                continue;
            }
            if (!tok.equalsIgnoreCase("datasetname")) continue;
            output = StringUtil.replaceAll(output, "[datasetname]", obfuscated ? "[*@" + datasetname + "@*]" : datasetname);
        }
        return output;
    }

    private String renderDetailHTML(String sdcId, String linkId, String buttonPlacement, boolean doAddFromLookup, String theAddLookupPage, String theLookupSDCId, String theLookupTableId, String theLookupQueryWhere, boolean doMultiSelect, String theLookupColumns, String theLookupTitles, String theLookupReturns, String theLookupColumnMap, String theLookupUnique, boolean canShowProgress, boolean isUsingCustomLookup, String theLookupCallback, boolean isViewOnly, PropertyList theLookupDirectives, String theRestrictiveWhere, boolean isUseSapphireDialog, boolean isMergeLookup, String theEditPage, String theEditTarget, boolean isSorted) {
        boolean locked = true;
        String newDetailKey = "";
        String datasetName = "";
        String[] keyCols = null;
        String errorMsg = "";
        this.logger.debug("renderDetailHTML called...");
        DataSet data = null;
        if (linkId.length() > 0) {
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            String cachekey = "sdcProps_" + sdcId;
            PropertyList sdcProps = (PropertyList)this.pageContext.getAttribute(cachekey);
            if (sdcProps == null) {
                sdcProps = sdcProcessor.getPropertyList(sdcId);
                this.pageContext.setAttribute(cachekey, (Object)sdcProps);
                this.logger.debug(this.logName, "Cached sdcProps1 cachekey: " + cachekey);
            }
            if (sdcProps != null && sdcProps.size() > 0) {
                PropertyListCollection links = sdcProps.getCollection("links");
                PropertyList linkProps = links.getPropertyList(linkId);
                if (linkProps != null && linkProps.size() > 0) {
                    if (this.sdiInfo.getSdcid().equalsIgnoreCase(sdcId)) {
                        DataSet primaryDataSet = this.sdiInfo.getQueryData("primary").getQuerydata();
                        if (primaryDataSet != null && primaryDataSet.getRowCount() > 0) {
                            int index;
                            String[] primaryKeyCols = new String[Integer.parseInt(sdcProps.getProperty("keycolumns"))];
                            for (int index2 = 0; index2 < primaryKeyCols.length; ++index2) {
                                primaryKeyCols[index2] = sdcProps.getProperty("keycolid" + (index2 + 1));
                            }
                            datasetName = linkProps.getProperty("linktableid");
                            this.logger.debug("datasetName = " + datasetName);
                            if (theAddLookupPage != null && theAddLookupPage.length() > 0) {
                                theAddLookupPage = this.parseExpressionsFromString(theAddLookupPage, this.elementid, datasetName, sdcId, "", linkId, primaryKeyCols, primaryDataSet);
                            }
                            if (theEditPage != null && theEditPage.length() > 0) {
                                theEditPage = this.parseExpressionsFromString(theEditPage, this.elementid, datasetName, sdcId, "", linkId, primaryKeyCols, primaryDataSet);
                            }
                            keyCols = new String[Integer.parseInt(linkProps.getProperty("keycolcount"))];
                            StringBuffer sb = new StringBuffer();
                            StringBuffer sbw = new StringBuffer();
                            for (index = 0; index < keyCols.length; ++index) {
                                keyCols[index] = linkProps.getProperty("keycolid" + (index + 1));
                                if (sb.length() > 0) {
                                    sb.append(";");
                                }
                                if (sbw.length() > 0) {
                                    sbw.append(";");
                                }
                                sb.append(primaryDataSet.getString(0, keyCols[index], "(null)"));
                                sbw.append("(null)");
                            }
                            if (sb.toString().equalsIgnoreCase(sbw.toString())) {
                                this.logger.debug("Could not build newDetailKey using column names.");
                                sb = new StringBuffer();
                                for (index = 0; index < keyCols.length; ++index) {
                                    if (sb.length() > 0) {
                                        sb.append(";");
                                    }
                                    sb.append(index < primaryKeyCols.length ? primaryDataSet.getString(0, primaryKeyCols[index], "(null)") : "(null)");
                                }
                            } else {
                                this.logger.debug("newDetailKey built using column names.");
                            }
                            if ("true".equalsIgnoreCase(this.requestContext.getProperty("newAddMode"))) {
                                this.logger.debug("Rebuilding newDetailKey in Add Mode.");
                                sb = new StringBuffer();
                                for (index = 0; index < keyCols.length; ++index) {
                                    if (sb.length() > 0) {
                                        sb.append(";");
                                    }
                                    String key = "";
                                    List<String> primaryKeys = Arrays.asList(primaryKeyCols);
                                    int primaryIndex = primaryKeys.indexOf(keyCols[index]);
                                    if (primaryIndex != -1) {
                                        key = this.requestContext.getProperty("keyid" + ++primaryIndex);
                                    }
                                    sb.append(!"".equals(key) ? key : "(null)");
                                }
                            }
                            newDetailKey = sb.toString();
                            this.logger.debug("newDetailKey = " + newDetailKey);
                            data = this.sdiInfo.getDataSet(datasetName);
                            if (data != null) {
                                this.playWithData(data, keyCols);
                                this.pagecount = 0;
                                if (this.pagecount > 0 && data.getRowCount() > this.pagecount) {
                                    int count = data.getRowCount();
                                    this.totalpages = (int)Math.ceil(count / this.pagecount);
                                    this.currentpage = (int)Math.ceil(this.pagefrom / this.pagecount);
                                    if (this.pagefrom - 1 + this.pagecount < count) {
                                        this.pageNext = true;
                                    }
                                    data = data.getRows(this.pagefrom - 1, this.pagefrom - 1 + this.pagecount);
                                    this.sdiInfo.setDataSet(datasetName, data);
                                    this.paging = true;
                                    if (this.pagefrom > this.pagecount) {
                                        this.pagePrev = true;
                                    }
                                }
                                if (isSorted) {
                                    StringBuffer orderby = new StringBuffer();
                                    for (int k = 0; k < this.element.getCollection(PROPERTY_ADVSORTBY).size(); ++k) {
                                        PropertyList sort = this.element.getCollection(PROPERTY_ADVSORTBY).getPropertyList(k);
                                        String col = sort.getProperty(PROPERTY_COLUMNID, "");
                                        if (col.length() <= 0) continue;
                                        String ascdesc = sort.getProperty(PROPERTY_ASC_DESC, "a");
                                        if (orderby.length() > 0) {
                                            orderby.append(",");
                                        }
                                        if (ascdesc.equalsIgnoreCase("d")) {
                                            orderby.append(col).append(" d");
                                            continue;
                                        }
                                        orderby.append(col);
                                    }
                                    if (orderby.length() > 0) {
                                        data.sort(orderby.toString());
                                    }
                                }
                            } else {
                                errorMsg = "You have specified a detail link but have not included the element in the request. Therefore no data is available.";
                                this.logger.error(errorMsg);
                                datasetName = "";
                            }
                            locked = this.checkLockState(datasetName);
                        } else {
                            errorMsg = "Could not obtain primary data.";
                            this.logger.error(errorMsg);
                        }
                    } else {
                        errorMsg = "Element SDC does not match primary SDC.";
                        this.logger.error(errorMsg);
                    }
                } else {
                    errorMsg = "Could not obtain link details for Link Id " + linkId + ".";
                    this.logger.error(errorMsg);
                }
            } else {
                errorMsg = "Could not obtain SDC details for SDC Id " + sdcId + ".";
                this.logger.error(errorMsg);
            }
            if (keyCols != null && datasetName.length() > 0 && newDetailKey.length() > 0 && data != null) {
                return this.renderDataViewFull(0, sdcId, "", linkId, "", "", newDetailKey, datasetName, data, keyCols, locked, "", buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, theLookupColumnMap, theLookupUnique, canShowProgress, isUsingCustomLookup, theLookupCallback, isViewOnly, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, theEditPage, theEditTarget, isSorted);
            }
            this.logger.error("Could not obtain data.");
            if (this.pageContext != null) {
                errorMsg = this.getTranslationProcessor().translate(errorMsg);
            }
            return "<font style=\"color:red;\">" + errorMsg + "</font>";
        }
        errorMsg = "No linkid provided.";
        this.logger.error(errorMsg);
        if (this.pageContext != null) {
            errorMsg = this.getTranslationProcessor().translate(errorMsg);
        }
        return "<font style=\"color:red;\">" + errorMsg + "</font>";
    }

    private String getColumnsList(PropertyListCollection columns, PropertyList sdcOrLinkProps, StringBuffer colsToSelect, boolean appendTableName) {
        StringBuffer out = new StringBuffer();
        PropertyListCollection sdccolumns = sdcOrLinkProps.containsKey(PROPERTYLIST_COLUMNS) ? sdcOrLinkProps.getCollection(PROPERTYLIST_COLUMNS) : sdcOrLinkProps.getCollection("linkcolumns");
        if (sdccolumns != null && columns.size() > 0) {
            String colid = null;
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                if (colid == null) {
                    if (column.containsKey(PROPERTY_COLUMNID)) {
                        colid = PROPERTY_COLUMNID;
                        if (column.getProperty(colid, "").length() == 0) {
                            columns.remove(i);
                            --i;
                        }
                    } else if (column.containsKey("linkcolumnid")) {
                        colid = "linkcolumnid";
                        if (column.getProperty(colid, "").length() == 0) {
                            columns.remove(i);
                            --i;
                        }
                    } else {
                        columns.remove(i);
                        --i;
                    }
                } else if (!column.containsKey(colid) || column.getProperty(colid, "").length() == 0) {
                    columns.remove(i);
                    --i;
                }
                if (!column.getProperty(colid).contains("[currentuser]")) continue;
                column.setProperty(colid, StringUtil.replaceAll(column.getProperty(colid), "[currentuser]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId()));
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
            String tableName = sdcOrLinkProps.getProperty(PROPERTY_TABLEID);
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
                keycol.setProperty(PROPERTY_MODE, "hidden");
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
                keycol.setProperty(PROPERTY_MODE, "hidden");
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
                keycol.setProperty(PROPERTY_MODE, "hidden");
                columns.add(keycol);
            }
        }
        if (out.length() > 0) {
            return out.toString();
        }
        return "";
    }

    private String renderFKeyHTML(String fromSDCId, String toSDCId, String linkId, String oldRsetId, boolean clearOldRset, String buttonPlacement, boolean doAddFromLookup, String theAddLookupPage, String theLookupSDCId, String theLookupTableId, String theLookupQueryWhere, boolean doMultiSelect, String theLookupColumns, String theLookupTitles, String theLookupReturns, String theLookupColumnMap, String theLookupUnique, boolean canShowProgress, boolean isUsingCustomLookup, String theLookupCallback, boolean isViewOnly, PropertyList theLookupDirectives, String theRestrictiveWhere, boolean isUseSapphireDialog, boolean isMergeLookup, String theEditPage, String theEditTarget, String fkWhereClause, boolean isSorted) {
        boolean locked = true;
        String newDetailKey = "";
        String datasetName = this.elementid;
        String[] keyCols = null;
        String rsetId = "";
        String linkkeyId1Col = "";
        String linkkeyId2Col = "";
        String linkkeyId3Col = "";
        String errorMsg = "";
        String[] primaryKeyCols = null;
        this.logger.debug("renderFKeyHTML called...");
        DataSet data = null;
        if (linkId.length() > 0) {
            HashMap revLinkProps = null;
            String cachekey = "sdcProps_" + fromSDCId;
            PropertyList sdcProps = (PropertyList)this.pageContext.getAttribute(cachekey);
            if (sdcProps == null) {
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                sdcProps = sdcProcessor.getPropertyList(fromSDCId);
                this.pageContext.setAttribute(cachekey, (Object)sdcProps);
                this.logger.debug(this.logName, "Cached sdcProps2 cachekey: " + cachekey);
            }
            if (sdcProps != null) {
                PropertyListCollection reverseLinklinks = sdcProps.getCollection("reverselinks");
                if (reverseLinklinks != null && reverseLinklinks.getPropertyList(linkId + ";" + toSDCId) != null) {
                    revLinkProps = reverseLinklinks.getPropertyList(linkId + ";" + toSDCId);
                } else {
                    errorMsg = "Could not obtain reverse link details for SDC Id " + toSDCId + " (1).";
                    this.logger.error(errorMsg);
                }
            } else {
                errorMsg = "Could not obtain SDC details for SDC Id " + fromSDCId + " (1).";
                this.logger.error(errorMsg);
            }
            if (revLinkProps != null && revLinkProps.size() > 0) {
                String linksdcid = ((PropertyList)revLinkProps).getProperty("linksdcid");
                if (this.sdiInfo.getSdcid().equalsIgnoreCase(fromSDCId) && linksdcid.equalsIgnoreCase(fromSDCId)) {
                    cachekey = "sdcProps_" + toSDCId;
                    PropertyList toSDCProps = (PropertyList)this.pageContext.getAttribute(cachekey);
                    if (toSDCProps == null) {
                        toSDCProps = this.getSDCProcessor().getPropertyList(toSDCId);
                        this.pageContext.setAttribute(cachekey, (Object)toSDCProps);
                        this.logger.debug(this.logName, "Cached sdcProps2 cachekey: " + cachekey);
                    }
                    PropertyList linkProps = null;
                    if (toSDCProps != null) {
                        PropertyListCollection links = toSDCProps.getCollection("links");
                        if (links != null && links.getPropertyList(linkId) != null) {
                            linkProps = links.getPropertyList(linkId);
                        } else {
                            errorMsg = "Could not obtain link details for SDC Id " + toSDCId + " (1).";
                            this.logger.error(errorMsg);
                        }
                    } else {
                        errorMsg = "Could not obtain SDC details for SDC Id " + toSDCProps + " (2).";
                        this.logger.error(errorMsg);
                    }
                    if (toSDCProps != null && linkProps != null) {
                        DataSet primary;
                        StringBuffer colsToSelect = new StringBuffer();
                        String orgcolslist = this.getColumnsList(this.element.getCollection(PROPERTYLIST_COLUMNS), toSDCProps, colsToSelect, false);
                        String colslist = "";
                        if (orgcolslist.length() > 0) {
                            colslist = colslist + colsToSelect + "";
                        }
                        if ((primary = this.sdiInfo.getDataSet("primary")) != null) {
                            primaryKeyCols = new String[Integer.parseInt(sdcProps.getProperty("keycolumns"))];
                            for (int index = 0; index < primaryKeyCols.length; ++index) {
                                primaryKeyCols[index] = sdcProps.getProperty("keycolid" + (index + 1));
                            }
                            linkkeyId1Col = linkProps.getProperty("sdccolumnid", "");
                            linkkeyId2Col = linkProps.getProperty("sdccolumnid2", "");
                            linkkeyId3Col = linkProps.getProperty("sdccolumnid3", "");
                            this.logger.debug("primarykeyId1Col =  " + primaryKeyCols[0]);
                            this.logger.debug("linkkeyId1Col =  " + linkkeyId1Col + ", linkkeyId2Col =  " + linkkeyId2Col + ", linkkeyId3Col =  " + linkkeyId3Col);
                            String keycolcount = toSDCProps.getProperty("keycolumns", "1");
                            keyCols = keycolcount.equals("2") ? new String[]{toSDCProps.getProperty("keycolid1", ""), toSDCProps.getProperty("keycolid2", "")} : (keycolcount.equals("3") ? new String[]{toSDCProps.getProperty("keycolid1", ""), toSDCProps.getProperty("keycolid2", ""), toSDCProps.getProperty("keycolid3", "")} : new String[]{toSDCProps.getProperty("keycolid1", "")});
                            String tableid = toSDCProps.getProperty(PROPERTY_TABLEID);
                            newDetailKey = primary.getValue(0, primaryKeyCols[0], "");
                            if (primaryKeyCols.length > 1) {
                                newDetailKey = newDetailKey + ";" + primary.getValue(0, primaryKeyCols[1], "");
                                if (primaryKeyCols.length > 2) {
                                    newDetailKey = newDetailKey + ";" + primary.getValue(0, primaryKeyCols[2], "");
                                }
                            }
                            if (theAddLookupPage != null && theAddLookupPage.length() > 0) {
                                theAddLookupPage = this.parseExpressionsFromString(theAddLookupPage, this.elementid, datasetName, fromSDCId, toSDCId, linkId, primaryKeyCols, primary);
                            }
                            if (theEditPage != null && theEditPage.length() > 0) {
                                theEditPage = this.parseExpressionsFromString(theEditPage, this.elementid, datasetName, fromSDCId, toSDCId, linkId, primaryKeyCols, primary);
                            }
                            if (fkWhereClause.length() > 0 && (fkWhereClause = ElementUtil.evaluateExpression("primary", 0, "", fkWhereClause, this.sdiInfo, this.getTranslationProcessor()).trim()).toLowerCase().startsWith("where ")) {
                                fkWhereClause = fkWhereClause.substring(6);
                            }
                            StringBuffer orderby = new StringBuffer();
                            if (isSorted) {
                                for (int k = 0; k < this.element.getCollection(PROPERTY_ADVSORTBY).size(); ++k) {
                                    PropertyList sort = this.element.getCollection(PROPERTY_ADVSORTBY).getPropertyList(k);
                                    String col = sort.getProperty(PROPERTY_COLUMNID, "");
                                    if (col.length() <= 0) continue;
                                    String ascdesc = sort.getProperty(PROPERTY_ASC_DESC, "a");
                                    if (orderby.length() > 0) {
                                        orderby.append(" , ");
                                    } else {
                                        orderby.append(" ORDER BY ");
                                    }
                                    if (ascdesc.equalsIgnoreCase("d")) {
                                        orderby.append(" ").append(col).append(" DESC ");
                                        continue;
                                    }
                                    orderby.append(" ").append(col).append(" ");
                                }
                            } else if (sdcProps != null && sdcProps.containsKey(PROPERTYLIST_COLUMNS) && sdcProps.getCollection(PROPERTYLIST_COLUMNS).find(PROPERTY_COLUMNID, "usersequence") != null) {
                                orderby.append(" ORDER BY usersequence ");
                            }
                            DAMProcessor dam = new DAMProcessor(this.pageContext);
                            if (oldRsetId != null && oldRsetId.length() > 0 && clearOldRset) {
                                try {
                                    if (dam.clearRSet(oldRsetId) == 2) {
                                        this.logger.warn("Old rset could not be cleared(1)");
                                    }
                                }
                                catch (Exception e) {
                                    this.logger.warn("Old rset could not be cleared(2)");
                                }
                            }
                            try {
                                SDIRequest sdireq = new SDIRequest();
                                sdireq.setSDCid(toSDCId);
                                sdireq.setRequestItem("primary[" + colslist + "]");
                                sdireq.setRetainRsetid(true);
                                sdireq.setReturnMaskedData(true);
                                if (oldRsetId != null && oldRsetId.length() > 0 && !clearOldRset) {
                                    sdireq.setRsetid(oldRsetId);
                                    sdireq.setAutoLockTimeout(true);
                                } else {
                                    if (!this.viewOnly || primary.getValue(0, "__lockedby").length() > 0) {
                                        sdireq.setDataLockOption("LA");
                                        sdireq.setLockOption("LA");
                                        sdireq.setPrimaryLockOption("LA");
                                        sdireq.setAutoLockTimeout(true);
                                    }
                                    sdireq.setQueryFrom(tableid + "");
                                    if (this.element.getProperty("fktemplates").equalsIgnoreCase("Y")) {
                                        sdireq.setShowTemplates(true);
                                    }
                                    StringBuffer querywhere = new StringBuffer();
                                    boolean isOracle = new ConnectionProcessor(this.pageContext).isOra();
                                    querywhere.append("").append(tableid).append(".").append(linkkeyId1Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(0, primaryKeyCols[0]), isOracle)).append("'");
                                    if (linkkeyId2Col != null && linkkeyId2Col.length() > 0 && primaryKeyCols.length > 1) {
                                        querywhere.append(" AND ").append(tableid).append(".").append(linkkeyId2Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(0, primaryKeyCols[1]), isOracle)).append("'");
                                        if (linkkeyId3Col != null && linkkeyId3Col.length() > 0 && primaryKeyCols.length > 2) {
                                            querywhere.append(" AND ").append(tableid).append(".").append(linkkeyId3Col).append(" = '").append(SafeSQL.encodeForSQL(primary.getString(0, primaryKeyCols[2]), isOracle)).append("'");
                                        }
                                    }
                                    if (fkWhereClause.length() > 0) {
                                        querywhere.append(" AND ( ").append(fkWhereClause).append(" ) ");
                                    }
                                    sdireq.setQueryWhere(querywhere.toString());
                                }
                                sdireq.setQueryOrderBy(orderby.toString().substring(9));
                                SDIProcessor sdiproc = this.pageContext == null ? new SDIProcessor(this.getConnectionid()) : new SDIProcessor(this.pageContext);
                                SDIData fksdidata = sdiproc.getSDIData(sdireq);
                                if (fksdidata != null && fksdidata.getDataset("primary") != null) {
                                    rsetId = fksdidata.getRsetid();
                                    data = fksdidata.getDataset("primary");
                                    if (data != null) {
                                        locked = data.getColumnValues("__lockedby", "").length() > 0;
                                    }
                                }
                            }
                            catch (Exception e) {
                                this.logger.error("Could not obtain FK data.", e);
                            }
                        }
                    } else {
                        errorMsg = "Could not obtain link details for Link Id " + linkId + "(2).";
                        this.logger.error(errorMsg);
                    }
                } else {
                    errorMsg = "Link specified does not originate from primary SDC or primary SDC specified for element does not match the primary SDC.";
                    this.logger.error(errorMsg);
                }
            } else {
                errorMsg = "Could not obtain reverse link details for Link Id " + linkId + "(1).";
                this.logger.error(errorMsg);
            }
            if (primaryKeyCols != null && keyCols != null && datasetName.length() > 0 && newDetailKey.length() > 0 && data != null) {
                String toCol = linkkeyId1Col;
                if (linkkeyId2Col.length() > 0) {
                    toCol = toCol + ";" + linkkeyId2Col;
                    if (linkkeyId3Col.length() > 0) {
                        toCol = toCol + ";" + linkkeyId3Col;
                    }
                }
                this.protectColumn(this.element.getCollection(PROPERTYLIST_COLUMNS), toCol);
                return this.renderDataViewFull(1, fromSDCId, toSDCId, linkId, (String)primaryKeyCols[0], toCol, newDetailKey, datasetName, data, keyCols, locked, rsetId, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, theLookupColumnMap, theLookupUnique, canShowProgress, isUsingCustomLookup, theLookupCallback, isViewOnly, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, theEditPage, theEditTarget, isSorted);
            }
            this.logger.error("Could not obtain data.");
            if (this.pageContext != null) {
                errorMsg = this.getTranslationProcessor().translate(errorMsg);
            }
            return "<font style=\"color:red;\">" + errorMsg + ".</font>";
        }
        this.logger.error("No linkid provided.");
        if (this.pageContext != null) {
            errorMsg = this.getTranslationProcessor().translate(errorMsg);
        }
        return "<font style=\"color:red;\">" + errorMsg + ".</font>";
    }

    private void protectColumn(PropertyListCollection cols, String columnid) {
        if (cols != null) {
            String[] columns;
            for (String column : columns = StringUtil.split(columnid, ";")) {
                String mode;
                PropertyList pl = cols.find(PROPERTY_COLUMNID, column);
                if (pl == null || (mode = pl.getProperty(PROPERTY_MODE, "input")).equalsIgnoreCase("readonly") || mode.equalsIgnoreCase("retrievedata") || mode.equalsIgnoreCase("hidden")) continue;
                pl.setProperty(PROPERTY_MODE, "readonly");
            }
        }
    }

    private String renderRelationalHTML(String fromSDCId, String toSDCId, String fromColumn1, String toColumn1, String fromColumn2, String toColumn2, String fromColumn3, String toColumn3, String oldRsetId, boolean clearOldRset, String buttonPlacement, boolean doAddFromLookup, String theAddLookupPage, String theLookupSDCId, String theLookupTableId, String theLookupQueryWhere, boolean doMultiSelect, String theLookupColumns, String theLookupTitles, String theLookupReturns, String theLookupColumnMap, String theLookupUnique, boolean canShowProgress, boolean isUsingCustomLookup, String theLookupCallback, boolean isViewOnly, PropertyList theLookupDirectives, String theRestrictiveWhere, boolean isUseSapphireDialog, boolean isMergeLookup, String theEditPage, String theEditTarget, String fkWhereClause, boolean isSorted) {
        boolean locked = true;
        String newDetailKey1 = "";
        String newDetailKey2 = "";
        String newDetailKey3 = "";
        String datasetName = "";
        String[] keyCols = null;
        String newrsetId = "";
        this.logger.debug("renderRelationalHTML called...");
        DataSet data = null;
        if (fromColumn1.length() > 0 && toColumn1.length() > 0) {
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            String cachekey = "sdcProps_" + toSDCId;
            PropertyList sdcToProps = (PropertyList)this.pageContext.getAttribute(cachekey);
            if (sdcToProps == null) {
                sdcToProps = sdcProcessor.getPropertyList(toSDCId);
                this.pageContext.setAttribute(cachekey, (Object)sdcToProps);
                this.logger.debug(this.logName, "Cached sdcProps3 cachekey: " + cachekey);
            }
            if (sdcToProps != null && sdcToProps.size() > 0) {
                PropertyList sdcFromProps = (PropertyList)this.pageContext.getAttribute("sdcProps_" + fromSDCId);
                if (sdcFromProps == null) {
                    cachekey = "sdcProps_" + fromSDCId;
                    sdcFromProps = sdcProcessor.getPropertyList(fromSDCId);
                    this.pageContext.setAttribute(cachekey, (Object)sdcFromProps);
                    this.logger.debug(this.logName, "Cached sdcProps4 cachekey: " + cachekey);
                }
                if (sdcFromProps != null && sdcFromProps.size() > 0) {
                    if (sdcToProps.getCollection(PROPERTYLIST_COLUMNS).find(PROPERTY_COLUMNID, toColumn1) != null) {
                        if (sdcFromProps.getCollection(PROPERTYLIST_COLUMNS).find(PROPERTY_COLUMNID, fromColumn1) != null) {
                            if (this.sdiInfo.getSdcid().equalsIgnoreCase(fromSDCId)) {
                                DataSet primary = this.sdiInfo.getDataSet("primary");
                                if (primary != null) {
                                    StringBuffer colsToSelect = new StringBuffer();
                                    this.getColumnsList(this.element.getCollection(PROPERTYLIST_COLUMNS), sdcToProps, colsToSelect, true);
                                    String colslist = colsToSelect.toString() + ", ";
                                    String tableid = sdcToProps.getProperty(PROPERTY_TABLEID);
                                    newDetailKey1 = primary.getValue(0, fromColumn1, "");
                                    newDetailKey2 = fromColumn2.length() > 0 ? primary.getValue(0, fromColumn2, "") : "";
                                    newDetailKey3 = fromColumn3.length() > 0 ? primary.getValue(0, fromColumn3, "") : "";
                                    String keyId1Col = sdcToProps.getProperty("keycolid1", "");
                                    String keyId2Col = sdcToProps.getProperty("keycolid2", "");
                                    String keyId3Col = sdcToProps.getProperty("keycolid3", "");
                                    keyCols = keyId2Col.length() > 0 && keyId3Col.length() > 0 ? new String[]{keyId1Col, keyId2Col, keyId3Col} : (keyId2Col.length() > 0 ? new String[]{keyId1Col, keyId2Col} : new String[]{keyId1Col});
                                    this.logger.debug("keyid1 column =  " + keyId1Col);
                                    SDIRequest toRequest = new SDIRequest();
                                    toRequest.setSDCid(toSDCId);
                                    toRequest.setRequestItem("primary");
                                    toRequest.setRetainRsetid(true);
                                    if (fkWhereClause.length() > 0 && (fkWhereClause = ElementUtil.evaluateExpression("primary", 0, "", fkWhereClause, this.sdiInfo, this.getTranslationProcessor()).trim()).toLowerCase().startsWith("where ")) {
                                        fkWhereClause = fkWhereClause.substring(6);
                                    }
                                    StringBuffer orderby = new StringBuffer();
                                    if (isSorted) {
                                        for (int k = 0; k < this.element.getCollection(PROPERTY_ADVSORTBY).size(); ++k) {
                                            PropertyList sort = this.element.getCollection(PROPERTY_ADVSORTBY).getPropertyList(k);
                                            String col = sort.getProperty(PROPERTY_COLUMNID, "");
                                            if (col.length() <= 0) continue;
                                            String ascdesc = sort.getProperty(PROPERTY_ASC_DESC, "a");
                                            if (orderby.length() > 0) {
                                                orderby.append(" , ");
                                            } else {
                                                orderby.append(" ORDER BY ");
                                            }
                                            if (ascdesc.equalsIgnoreCase("d")) {
                                                orderby.append(" ").append(col).append(" DESC ");
                                                continue;
                                            }
                                            orderby.append(" ").append(col).append(" ");
                                        }
                                    } else if (sdcToProps != null && sdcToProps.containsKey(PROPERTYLIST_COLUMNS) && sdcToProps.getCollection(PROPERTYLIST_COLUMNS).find(PROPERTY_COLUMNID, "usersequence") != null) {
                                        orderby.append(" ORDER BY usersequence ");
                                    }
                                    if (oldRsetId != null && !clearOldRset) {
                                        toRequest.setRsetid(oldRsetId);
                                        toRequest.setAutoLockTimeout(true);
                                    } else {
                                        DAMProcessor dam;
                                        if (clearOldRset && (dam = new DAMProcessor(this.pageContext)).clearRSet(oldRsetId) == 2) {
                                            this.logger.warn("Old rset could not be cleared(1)");
                                        }
                                        if (!isViewOnly || primary.getValue(0, "__lockedby").length() > 0) {
                                            toRequest.setDataLockOption("LA");
                                            toRequest.setLockOption("LA");
                                            toRequest.setPrimaryLockOption("LA");
                                            toRequest.setAutoLockTimeout(true);
                                        }
                                        toRequest.setQueryFrom(tableid);
                                        StringBuilder where = new StringBuilder();
                                        boolean isOracle = new ConnectionProcessor(this.pageContext).isOra();
                                        where.append(tableid).append(".").append(toColumn1).append(" = '").append(SafeSQL.encodeForSQL(newDetailKey1, isOracle)).append("'");
                                        if (toColumn2.length() > 0) {
                                            where.append(" AND ").append(tableid).append(".").append(toColumn2).append(" = '").append(SafeSQL.encodeForSQL(newDetailKey2, isOracle)).append("'");
                                        }
                                        if (toColumn3.length() > 0) {
                                            where.append(" AND ").append(tableid).append(".").append(toColumn3).append(" = '").append(SafeSQL.encodeForSQL(newDetailKey3, isOracle)).append("'");
                                        }
                                        if (fkWhereClause.length() > 0) {
                                            where.append(" AND ( ").append(fkWhereClause).append(" ) ");
                                        }
                                        toRequest.setQueryWhere(where.toString());
                                    }
                                    toRequest.setQueryOrderBy(orderby.toString().substring(9));
                                    SDIProcessor sdiProcessor = this.pageContext == null ? new SDIProcessor(this.getConnectionid()) : new SDIProcessor(this.pageContext);
                                    SDIData sdiData = sdiProcessor.getSDIData(toRequest);
                                    if (sdiData != null) {
                                        newrsetId = sdiData.getRsetid();
                                        data = sdiData.getDataset("primary");
                                        if (data != null) {
                                            locked = data.getColumnValues("__lockedby", "").length() > 0;
                                        }
                                        datasetName = this.elementid;
                                    } else {
                                        this.logger.warn("Failed to obtain relational data.");
                                    }
                                }
                            } else {
                                this.logger.error("Primary SDC specified for element does not match the primary SDC.");
                            }
                        } else {
                            this.logger.error("Column " + fromColumn1 + " does not exist in SDC " + fromSDCId + ".");
                        }
                    } else {
                        this.logger.error("Column " + toColumn1 + " does not exist in SDC " + toSDCId + ".");
                    }
                } else {
                    this.logger.error("Could not obtain SDC details for SDC Id " + fromSDCId + ".");
                }
            } else {
                this.logger.error("Could not obtain SDC details for SDC Id " + toSDCId + ".");
            }
            if (keyCols != null && datasetName.length() > 0 && newDetailKey1.length() > 0 && data != null) {
                this.protectColumn(this.element.getCollection(PROPERTYLIST_COLUMNS), toColumn1);
                return this.renderDataViewFull(2, fromSDCId, toSDCId, "", fromColumn1 + (this.fromcolumn2.length() > 0 ? ";" + fromColumn2 + (this.fromcolumn3.length() > 0 ? ";" + fromColumn3 : "") : ""), toColumn1 + (toColumn2.length() > 0 ? ";" + this.tocolumn2 + (this.tocolumn3.length() > 0 ? ";" + toColumn3 : "") : ""), newDetailKey1 + (this.fromcolumn2.length() > 0 ? ";" + newDetailKey2 + (this.fromcolumn3.length() > 0 ? ";" + newDetailKey3 : "") : ""), datasetName, data, keyCols, locked, newrsetId, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, theLookupColumnMap, theLookupUnique, canShowProgress, isUsingCustomLookup, theLookupCallback, isViewOnly, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, theEditPage, theEditTarget, false);
            }
            this.logger.error("Could not obtain data.");
            return "";
        }
        this.logger.error("No from column or to column provided.");
        return "";
    }

    private void renderPageLink(StringBuffer html, int p) {
        if (p == this.currentpage) {
            html.append("<span title=\"").append(this.getTranslationProcessor().translate("Currently on page ")).append(p + 1).append("\">").append(p + 1).append("</span>");
        } else {
            html.append("<a title=\"").append(this.getTranslationProcessor().translate("Jump to page ")).append(p + 1).append("\" href=\"#\" onclick=\"").append(JS_OBJECT).append(".pageJump('").append(this.elementid).append("',").append(p).append(");\">").append(p + 1).append("</a>");
        }
        html.append("&nbsp;");
    }

    private void renderPaging(StringBuffer html, DataSet data) {
        html.append(this.getTranslationProcessor().translate("Displaying rows ")).append(this.pagefrom).append(" to ").append(data.getRowCount() + this.pagefrom - 1);
        html.append("&nbsp;");
        if (this.pagePrev) {
            html.append("<a title=\"").append(this.getTranslationProcessor().translate("Previous")).append("\" href=\"#\" onclick=\"").append(JS_OBJECT).append(".pagePrev('").append(this.elementid).append("');\">").append("&lt; ").append(this.getTranslationProcessor().translate("Prev")).append("</a>");
        } else {
            html.append("<span title=\"").append(this.getTranslationProcessor().translate("Previous")).append("\">").append("&lt; ").append(this.getTranslationProcessor().translate("Prev")).append("</span>");
        }
        html.append("&nbsp;");
        if (this.totalpages > 10) {
            if (this.currentpage < 7) {
                for (int p = 0; p < 11; ++p) {
                    this.renderPageLink(html, p);
                }
                html.append("...");
                this.renderPageLink(html, this.totalpages + 1);
            } else if (this.currentpage > this.totalpages - 7) {
                this.renderPageLink(html, 0);
                html.append("...");
                for (int p = this.totalpages - 10; p < this.totalpages + 1; ++p) {
                    this.renderPageLink(html, p);
                }
            } else {
                int p;
                this.renderPageLink(html, 0);
                html.append("...");
                for (p = this.currentpage - 5; p < this.currentpage; ++p) {
                    this.renderPageLink(html, p);
                }
                this.renderPageLink(html, this.currentpage);
                for (p = this.currentpage + 1; p < this.currentpage + 6; ++p) {
                    this.renderPageLink(html, p);
                }
                html.append("...");
                this.renderPageLink(html, this.totalpages);
            }
        } else {
            for (int p = 0; p < this.totalpages + 1; ++p) {
                this.renderPageLink(html, p);
            }
        }
        if (this.pageNext) {
            html.append("<a title=\"").append(this.getTranslationProcessor().translate("Next")).append("\" href=\"#\" onclick=\"").append(JS_OBJECT).append(".pageNext('").append(this.elementid).append("');\">").append(this.getTranslationProcessor().translate("Next")).append("&gt; ").append("</a>");
        } else {
            html.append("<span title=\"").append(this.getTranslationProcessor().translate("Next")).append("\">").append(this.getTranslationProcessor().translate("Next")).append("&gt; ").append("</span>");
        }
    }

    private String renderDataViewFull(int linkType, String fromSDCId, String toSDCId, String linkId, String fromCol, String toCol, String newDetailKey, String datasetName, DataSet data, String[] keyCols, boolean locked, String rsetId, String buttonPlacement, boolean doAddFromLookup, String theAddLookupPage, String theLookupSDCId, String theLookupTableId, String theLookupQueryWhere, boolean doMultiSelect, String theLookupColumns, String theLookupTitles, String theLookupReturns, String theLookupColumnMap, String theLookupUnique, boolean canShowProgress, boolean isUsingCustomLookup, String theLookupCallback, boolean isViewOnly, PropertyList theLookupDirectives, String theRestrictiveWhere, boolean isUseSapphireDialog, boolean isMergeLookup, String theEditPage, String theEditTarget, boolean isSorted) {
        StringBuffer html = new StringBuffer();
        if (this.autoAddRow && (locked || this.viewOnly)) {
            this.autoAddRow = false;
        }
        String sdcid = null;
        if (linkType == 2 || linkType == 1) {
            sdcid = toSDCId;
            this.renderPropertyHandlerFields(html, linkType, fromSDCId, toSDCId, linkId, fromCol, toCol, newDetailKey);
            if (linkType == 2 && doAddFromLookup) {
                this.logger.warn("You have provided a lookup page for addition which is not possible for Relational links.");
                doAddFromLookup = false;
            }
        }
        if (theRestrictiveWhere != null && theRestrictiveWhere.length() > 0) {
            theRestrictiveWhere = ElementUtil.evaluateExpression("primary", 0, "", theRestrictiveWhere, this.sdiInfo, null).trim();
        }
        boolean alreadyHasRow = false;
        if (this.requestContext != null && this.requestContext.getProperty("__formsuccess").length() > 0 && this.sdiInfo != null && this.sdiInfo.getQueryData(datasetName) != null) {
            DataSet ds = this.sdiInfo.getQueryData(datasetName).getQuerydata();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (ds.getValue(i, "__rowstatus", "S").equalsIgnoreCase("I")) {
                    alreadyHasRow = true;
                    continue;
                }
                if (!alreadyHasRow && ds.getValue(i, "__rowstatus", "S").equalsIgnoreCase("A")) {
                    alreadyHasRow = true;
                    continue;
                }
                if (!ds.getValue(i, "__rowstatus", "S").equalsIgnoreCase("D")) continue;
                ds.setValue(i, "__rowstatus", "S");
            }
        }
        this.renderHeader(html, this.elementid, this.sdiFormId, theLookupColumnMap, theLookupUnique, newDetailKey, this.elementid + "_table", canShowProgress, isViewOnly, rsetId);
        if (buttonPlacement.equals("none") || isViewOnly) {
            this.renderDataView(html, datasetName, data, keyCols, sdcid);
        } else if (buttonPlacement.equals(DEFAULT_BUTTONPLACEMENT) || buttonPlacement.equals("topmiddle") || buttonPlacement.equals("topright")) {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            this.renderButtons(html, datasetName, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, locked, isUsingCustomLookup, theLookupCallback, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, linkType, theEditPage, theEditTarget, keyCols, isSorted);
            html.append("</td>\n</tr>\n<tr>\n<td>\n");
            if (this.paging) {
                this.renderPaging(html, data);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
            }
            this.renderDataView(html, datasetName, data, keyCols, sdcid);
            html.append("</td>\n</tr>\n</table>\n");
        } else if (buttonPlacement.equals("bottomleft") || buttonPlacement.equals("bottommiddle") || buttonPlacement.equals("bottomright")) {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            if (this.paging) {
                this.renderPaging(html, data);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
            }
            this.renderDataView(html, datasetName, data, keyCols, sdcid);
            html.append("</td>\n</tr>\n<tr>\n<td>\n");
            this.renderButtons(html, datasetName, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, locked, isUsingCustomLookup, theLookupCallback, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, linkType, theEditPage, theEditTarget, keyCols, isSorted);
            html.append("</td>\n</tr>\n</table>\n");
        } else {
            html.append("<table cellpadding=0 cellspacing=0 border=0>\n<tr>\n<td>\n");
            if (this.paging) {
                this.renderPaging(html, data);
                html.append("</td>\n</tr>\n<tr>\n<td>\n");
            }
            this.renderButtons(html, datasetName, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, locked, isUsingCustomLookup, theLookupCallback, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, linkType, theEditPage, theEditTarget, keyCols, isSorted);
            html.append("</td>\n</tr>\n<tr>\n<td>\n");
            this.renderDataView(html, datasetName, data, keyCols, sdcid);
            html.append("</td>\n</tr>\n<tr>\n<td>\n");
            this.renderButtons(html, datasetName, buttonPlacement, doAddFromLookup, theAddLookupPage, theLookupSDCId, theLookupTableId, theLookupQueryWhere, doMultiSelect, theLookupColumns, theLookupTitles, theLookupReturns, locked, isUsingCustomLookup, theLookupCallback, theLookupDirectives, theRestrictiveWhere, isUseSapphireDialog, isMergeLookup, linkType, theEditPage, theEditTarget, keyCols, isSorted);
            html.append("</td>\n</tr>\n</table>\n");
        }
        if (html.length() > 0) {
            StringBuilder s = new StringBuilder();
            if (this.autoAddRow && !alreadyHasRow) {
                s.append("sapphire.events.registerLoadListener( ");
                s.append("function(){ ");
                s.append(JS_OBJECT).append(".").append(JS_ADDFUNCTION).append("('").append(this.elementid).append("','").append(this.sdiFormId).append("','").append(this.elementid).append("_table',").append(JS_OBJECT).append(".saDatasetName['").append(this.elementid).append("'],'',true)");
                s.append("}");
                s.append(", false );\n");
            }
            if (this.paging) {
                s.append("sapphire.util.dom.setFormField( document.forms['sdiedit'], 'sdclinkmaintpagefrom_").append(this.elementid).append("', '' + ").append(this.pagefrom).append(", 'hidden' );");
            }
            if (s.length() > 0) {
                html.append("<script>");
                html.append((CharSequence)s);
                html.append("</script>");
            }
            return html.toString();
        }
        return "";
    }

    private void playWithData(DataSet linkedData, String[] keyCols) {
        this.logger.debug("playWithData called...");
        if (linkedData != null) {
            this.logger.debug("linkedData.size = " + linkedData.size());
            if (keyCols.length > 0) {
                for (int row = 0; row < linkedData.getRowCount(); ++row) {
                    for (int i = 0; i < keyCols.length; ++i) {
                        if (linkedData.getColumnType(keyCols[i]) != 0 || !linkedData.getString(row, keyCols[i], "").equals("(null)")) continue;
                        this.logger.debug("(null) found in keycol " + keyCols[i] + "...");
                        linkedData.setString(row, keyCols[i], "__null");
                    }
                }
            }
        } else {
            this.logger.warn("Could not obtain data.");
        }
    }

    private void renderHeader(StringBuffer html, String theElementId, String theFormId, String theMap, String theUnique, String theNewKeyValue, String theHTMLTableId, boolean canShowProgress, boolean isViewOnly, String rsetId) {
        PropertyList column;
        int index;
        int numTabs;
        int fixedcolumns;
        PropertyListCollection columns = null;
        this.logger.debug("renderHeader called...");
        String tabtext = this.element.getPropertyList(PROPERTY_TAB).getProperty(PROPERTY_TEXT);
        try {
            String fixedcols = this.element.getProperty(PROPERTY_FIXEDCOLS);
            fixedcolumns = fixedcols != null ? ((fixedcols = fixedcols.trim()).length() > 0 ? Integer.parseInt(fixedcols) : 0) : 0;
        }
        catch (Exception e) {
            fixedcolumns = 0;
        }
        if (tabtext != null && tabtext.trim().length() > 0 && fixedcolumns == 0) {
            String[] tabs = tabtext.trim().split(";");
            if (tabs.length == 1) {
                numTabs = 0;
            } else {
                numTabs = tabs.length;
                columns = this.element.getCollection(PROPERTYLIST_COLUMNS);
                block2: for (index = 0; index < columns.size(); ++index) {
                    column = columns.getPropertyList(index);
                    String group = column.getProperty(PROPERTY_GROUP);
                    if (group != null && group.trim().length() > 0) {
                        this.logger.debug("group = " + group);
                        for (int tabindex = 0; tabindex < tabs.length; ++tabindex) {
                            if (this.pageContext != null) {
                                boolean isTranslated;
                                String transTab = this.getTranslationProcessor().translate(tabs[tabindex]);
                                boolean bl = isTranslated = !transTab.equalsIgnoreCase(tabs[tabindex]);
                                if (isTranslated && group.equalsIgnoreCase(transTab)) {
                                    column.setProperty(PROPERTY_CELLCSSCLASS, theElementId + TABPREFIX + tabindex);
                                    continue block2;
                                }
                                if (!group.equalsIgnoreCase(tabs[tabindex])) continue;
                                column.setProperty(PROPERTY_CELLCSSCLASS, theElementId + TABPREFIX + tabindex);
                                continue block2;
                            }
                            if (!group.equalsIgnoreCase(tabs[tabindex])) continue;
                            column.setProperty(PROPERTY_CELLCSSCLASS, theElementId + TABPREFIX + tabindex);
                            continue block2;
                        }
                        continue;
                    }
                    this.logger.debug("No group");
                }
                html.append("<style id='").append(theElementId).append(TABSTYLEPREFIX).append("'>\n");
                for (index = 0; index < numTabs; ++index) {
                    html.append(".").append(theElementId).append(TABPREFIX).append(index).append("{\n");
                    if (index == 0) {
                        String displayProp = this.browser.isIE() && this.browser.getVersion() < 9.0 ? "block" : "table-cell";
                        html.append("display:" + displayProp + " !important;\n");
                    } else {
                        html.append("display:none !important;\n");
                    }
                    html.append("}\n");
                }
                html.append("</style>\n");
            }
        } else {
            numTabs = 0;
            this.logger.debug("Either no tabs have been specified or fixed columns is in use.");
        }
        this.logger.debug("numTabs = " + numTabs);
        html.append("<script>\n");
        if (rsetId.length() > 0) {
            html.append("if ( typeof(sdiAddRSet) != 'undefined'){\n");
            html.append("sdiAddRSet( '").append(rsetId).append("' );");
            html.append("}\n");
        }
        PropertyList displayval = new PropertyList();
        if (columns == null) {
            columns = this.element.getCollection(PROPERTYLIST_COLUMNS);
        }
        for (index = 0; index < columns.size(); ++index) {
            column = columns.getPropertyList(index);
            if (column.getProperty(PROPERTY_COLUMNID, "").length() <= 0) continue;
            if (column.getProperty("displayvalue", "").length() > 0) {
                displayval.setProperty(column.getProperty(PROPERTY_COLUMNID), column.getProperty("displayvalue", ""));
                continue;
            }
            if (!column.getProperty("translatevalue", "N").equalsIgnoreCase("Y")) continue;
            displayval.setProperty(column.getProperty(PROPERTY_COLUMNID), "translate");
        }
        html.append("function registerDataView_").append(theElementId).append("(){\n");
        html.append("try{\n");
        html.append(JS_OBJECT).append(".laAutoAddRow.").append(theElementId).append(" = ").append(this.autoAddRow).append(";\n");
        html.append(JS_OBJECT).append(".laShowProgressDialog.").append(theElementId).append(" = ").append(canShowProgress).append(";\n");
        html.append(JS_OBJECT).append(".saFormId.").append(theElementId).append(" = '").append(theFormId).append("';\n");
        html.append(JS_OBJECT).append(".saLookupColumnMap.").append(theElementId).append(" = '").append(theMap).append("';\n");
        html.append(JS_OBJECT).append(".saLookupUnique.").append(theElementId).append(" = '").append(theUnique).append("';\n");
        html.append(JS_OBJECT).append(".saNewKeyValue.").append(theElementId).append(" = '").append(theNewKeyValue).append("';\n");
        html.append(JS_OBJECT).append(".saTableId.").append(theElementId).append(" = '").append(theHTMLTableId).append("';\n");
        html.append(JS_OBJECT).append(".saDatasetName.").append(theElementId).append(" = ").append(JS_OBJECT).append(".getDatasetName('").append(theElementId).append("')").append(";\n");
        html.append(JS_OBJECT).append(".saElementId[").append(JS_OBJECT).append(".saDatasetName['").append(theElementId).append("']").append("] = '").append(theElementId).append("'").append(";\n");
        html.append(JS_OBJECT).append(".iaTabCount.").append(theElementId).append(" = ").append(numTabs).append(";\n");
        html.append(JS_OBJECT).append(".iaFixedCols.").append(theElementId).append(" = ").append(fixedcolumns).append(";\n");
        html.append(JS_OBJECT).append(".laViewOnly.").append(theElementId).append(" = ").append(isViewOnly).append(";\n");
        html.append(JS_OBJECT).append(".oaDisplayValue.").append(theElementId).append(" = ").append(displayval.size() > 0 ? displayval.toJSONString(false) : "null").append(";\n");
        html.append(JS_OBJECT).append(".iaPageFrom.").append(theElementId).append(" = ").append(this.pagefrom).append(";\n");
        html.append(JS_OBJECT).append(".iaPageCount.").append(theElementId).append(" = ").append(this.pagecount).append(";\n");
        if (numTabs > 0) {
            html.append("if ( typeof( ").append(theElementId).append("handler1 ) != 'undefined' ){\n");
            html.append(theElementId).append("handler1").append(".dontUseHiddenFields = true;\n");
            html.append("}\n");
        }
        html.append(JS_OBJECT).append(".doSizeColumns(").append(JS_OBJECT).append(".saDatasetName['").append(theElementId).append("']").append(")").append(";\n");
        html.append("}\ncatch( e ){\n}\n");
        html.append("}\n");
        html.append("sapphire.events.registerLoadListener( registerDataView_").append(theElementId).append(", true );\n");
        html.append("</script>\n");
        if (rsetId.length() > 0) {
            html.append("<input type=hidden name=\"__").append(theElementId).append("_rsetid\" id=\"__").append(theElementId).append("_rsetid\" value=\"").append(rsetId).append("\">");
            html.append("<input type=hidden name=\"__").append(theElementId).append("_rsetidinvalid\" id=\"__").append(theElementId).append("_rsetidinvalid\" value=\"N\">");
        }
    }

    private void renderButtons(StringBuffer html, String datasetName, String buttonPlacement, boolean doAddFromLookup, String theAddLookupPage, String theLookupSDCId, String theLookupTableId, String theLookupQueryWhere, boolean doMultiSelect, String theLookupCols, String theLookupTitles, String theLookupReturns, boolean locked, boolean isUsingCustomLookup, String theLookupCallback, PropertyList theLookupDirectives, String theRestrictiveWhere, boolean isUseSapphireDialog, boolean isMergeLookup, int linkType, String theEditPage, String theEditTarget, String[] keyCols, boolean isSorted) {
        this.logger.debug("renderButtons called...");
        PropertyListCollection buttons = this.element.getCollection("buttons");
        if (buttons != null && buttons.size() > 0) {
            this.logger.debug("About to render " + buttons.size() + " buttons...");
            if (doAddFromLookup) {
                html.append("<script>");
                html.append("oALUPD_").append(this.elementid).append("=");
                if (isUsingCustomLookup || isMergeLookup) {
                    if (!isUsingCustomLookup) {
                        // empty if block
                    }
                    JSONObject job = theLookupDirectives.toJSONObject();
                    job.remove("__propertylistid");
                    job.remove("__propertylistsequence");
                    html.append(job.toString());
                    html.append(";");
                } else {
                    html.append("{};");
                }
                html.append("</script>");
            }
            if (buttonPlacement.endsWith("middle")) {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=center>\n<tr>\n");
            } else if (buttonPlacement.endsWith("right")) {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=" + (this.connectionInfo.isRtl() ? "left" : "right") + ">\n<tr>\n");
            } else {
                html.append("<table cellspacing=\"3\" cellpadding=\"3\" border=\"0\" align=" + (this.connectionInfo.isRtl() ? "right" : "left") + ">\n<tr>\n");
            }
            if (this.usingDataLookup) {
                DataLookup.setUpLookup(this.pageContext, "sdclinkmaint_" + this.elementid, theLookupSDCId, theLookupTableId, theLookupQueryWhere, theLookupCols, theLookupTitles, theLookupReturns, theRestrictiveWhere);
            }
            for (int i = 0; i < buttons.size(); ++i) {
                PropertyList buttonProps = buttons.getPropertyList(i);
                if (!buttonProps.getProperty("show", "Y").equals("Y")) continue;
                html.append("<td>\n");
                Button button = new Button(this.pageContext);
                String id = buttonProps.getProperty("id");
                if (id != null && id.length() > 0) {
                    button.setId(StringUtil.replaceAll(id, "[elementid]", this.elementid, false));
                }
                button.setText(buttonProps.getProperty(PROPERTY_TEXT));
                button.setImg(buttonProps.getProperty("img"));
                button.setTip(buttonProps.getProperty("tip"));
                button.setAppearance(buttonProps.getProperty("appearance"));
                button.setMargin(buttonProps.getProperty("margin"));
                button.setStyle(buttonProps.getProperty(PROPERTY_STYLE));
                button.setWidth(buttonProps.getProperty("width"));
                String js = buttonProps.getProperty("js");
                this.logger.debug("js = " + js);
                if (js.length() == 0) {
                    js = "dataView.noop();";
                } else {
                    String temp;
                    int index;
                    int fixedcolumns;
                    try {
                        String fixedcols = this.element.getProperty(PROPERTY_FIXEDCOLS);
                        fixedcolumns = fixedcols != null ? ((fixedcols = fixedcols.trim()).length() > 0 ? Integer.parseInt(fixedcols) : 0) : 0;
                    }
                    catch (Exception e) {
                        fixedcolumns = 0;
                    }
                    if (locked) {
                        index = js.indexOf(JS_SETEQUENCEFUNCTION);
                        if (index > -1) {
                            this.logger.debug("setSequence found...");
                            temp = js.substring(index, js.indexOf(")", index) + 1);
                            this.logger.debug("temp = " + temp);
                            js = StringUtil.replaceAll(js, temp, "");
                        }
                        js = StringUtil.replaceAll(js, "add()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "edit()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "removeLink()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "remove()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "removeAll()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "moveUp()", "dataView.lockedCall();");
                        js = StringUtil.replaceAll(js, "moveDown()", "dataView.lockedCall();");
                    } else if (fixedcolumns > 0) {
                        index = js.indexOf(JS_SETEQUENCEFUNCTION);
                        if (index > -1) {
                            this.logger.debug("setSequence found...");
                            temp = js.substring(index, js.indexOf(")", index) + 1);
                            this.logger.debug("temp = " + temp);
                            js = StringUtil.replaceAll(js, temp, "");
                        }
                        js = StringUtil.replaceAll(js, "add()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "edit()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "removeLink()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "remove()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "removeAll()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "moveUp()", "dataView.notAvailable( 'Fixed Columns' );");
                        js = StringUtil.replaceAll(js, "moveDown()", "dataView.notAvailable( 'Fixed Columns' );");
                    } else {
                        String column;
                        index = js.indexOf(JS_SETEQUENCEFUNCTION);
                        if (index > -1) {
                            this.logger.debug("setSequence found...");
                            temp = js.substring(index, js.indexOf(")", index) + 1);
                            this.logger.debug("temp = " + temp);
                            column = js.substring(js.indexOf("(", index) + 1, js.indexOf(")", index));
                            column = StringUtil.replaceAll(column, "\"", "'");
                            this.logger.debug("column = " + column);
                            js = StringUtil.replaceAll(js, temp, "");
                        } else {
                            column = "''";
                        }
                        js = doAddFromLookup ? (this.usingDataLookup ? StringUtil.replaceAll(js, "add()", "dataView.addDataViewRowFromLookup('" + theAddLookupPage + "','','', ''," + doMultiSelect + ",'','','','" + this.elementid + "','" + datasetName + "', " + column + ", " + isUsingCustomLookup + ",'" + theLookupCallback + "',oALUPD_" + this.elementid + ",''," + isUseSapphireDialog + "," + isMergeLookup + ",'" + "sdclinkmaint_" + this.elementid + "')") : StringUtil.replaceAll(js, "add()", "dataView.addDataViewRowFromLookup('" + theAddLookupPage + "','" + theLookupSDCId + "','" + theLookupTableId + "', '" + StringUtil.replaceAll(theLookupQueryWhere, "'", "\\'") + "'," + doMultiSelect + ",'" + theLookupCols + "','" + theLookupTitles + "','" + theLookupReturns + "','" + this.elementid + "','" + datasetName + "', " + column + ", " + isUsingCustomLookup + ",'" + theLookupCallback + "',oALUPD_" + this.elementid + ",'" + StringUtil.replaceAll(theRestrictiveWhere, "'", "\\'") + "'," + isUseSapphireDialog + "," + isMergeLookup + ")")) : StringUtil.replaceAll(js, "add()", "dataView.addDataViewRow('" + this.elementid + "','" + this.sdiFormId + "','" + this.elementid + "_table','" + datasetName + "'," + column + "," + false + ")");
                        if (linkType == 1) {
                            StringBuffer keys = new StringBuffer();
                            for (int k = 0; k < keyCols.length; ++k) {
                                if (k > 0) {
                                    keys.append(";");
                                }
                                keys.append(keyCols[k]);
                            }
                            js = StringUtil.replaceAll(js, "edit()", "dataView.editDataViewRow('" + this.elementid + "','" + datasetName + "','" + keys + "','" + theEditPage + "','" + theEditTarget + "')");
                        } else {
                            js = StringUtil.replaceAll(js, "edit()", "dataView.notAvailable( 'Detail Links' );");
                        }
                        js = StringUtil.replaceAll(js, "remove()", "dataView.deleteDataviewRow('" + this.elementid + "','" + this.sdiFormId + "','" + this.elementid + "_table','" + datasetName + "',getSelectedDetail('" + this.elementid + "'),false)");
                        js = linkType == 1 ? StringUtil.replaceAll(js, "removeLink()", "dataView.deleteDataviewRow('" + this.elementid + "','" + this.sdiFormId + "','" + this.elementid + "_table','" + datasetName + "',getSelectedDetail('" + this.elementid + "'),true)") : StringUtil.replaceAll(js, "removeLink()", "dataView.notAvailable( 'Detail Links' );");
                        js = StringUtil.replaceAll(js, "removeAll()", "dataView.deleteDataviewRow('" + this.elementid + "','" + this.sdiFormId + "','" + this.elementid + "_table','" + datasetName + "','-1',false)");
                        if (!isSorted) {
                            js = StringUtil.replaceAll(js, "moveUp()", "dataView.sequenceUp('" + this.elementid + "','" + this.elementid + "_table','" + datasetName + "',getSelectedDetail('" + this.elementid + "'))");
                            js = StringUtil.replaceAll(js, "moveDown()", "dataView.sequenceDown('" + this.elementid + "','" + this.elementid + "_table','" + datasetName + "',getSelectedDetail('" + this.elementid + "'))");
                        } else {
                            js = StringUtil.replaceAll(js, "moveUp()", "dataView.notAvailable( 'Sorted Data' );");
                            js = StringUtil.replaceAll(js, "moveDown()", "dataView.notAvailable( 'Sorted Data' );");
                        }
                    }
                }
                button.setAction(js);
                html.append(button.getHtml());
                html.append("</td>\n");
            }
            html.append("</tr>\n</table>\n");
        } else {
            this.logger.debug("No buttons found.");
        }
    }

    private String renderPropertyHandlerFields(StringBuffer html, int linkType, String fromSDC, String toSDC, String linkId, String fromCol, String toCol, String linkKeyId) {
        html.append("\n<input type='hidden' name='").append("__propertyhandler_").append(this.elementid).append("' value='").append(PROPERTYHANDLERCLASS).append("'/>\n");
        html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_type").append("' value='").append(this.element.getProperty("propertytreeid", "")).append("'/>\n");
        html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_tocol").append("' value='").append(toCol).append("'/>\n");
        html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linkkeyid").append("' value='").append(linkKeyId).append("'/>\n");
        if (linkType == 1) {
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linktype").append("' value='").append(1).append("'/>\n");
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_fromsdc").append("' value='").append(fromSDC).append("'/>\n");
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_tosdc").append("' value='").append(toSDC).append("'/>\n");
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linkid").append("' value='").append(linkId).append("'/>\n");
        } else {
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linktype").append("' value='").append(2).append("'/>\n");
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_fromsdc").append("' value='").append(fromSDC).append("'/>\n");
            html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_tosdc").append("' value='").append(toSDC).append("'/>\n");
        }
        html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_linkelement_").append(toSDC).append("' value='").append(this.elementid).append("'/>\n");
        html.append("<input type='hidden' name='").append("___").append(this.elementid).append("_addfromlookup").append("' value='").append(this.addFromLookup ? "Y" : "N").append("'/>\n");
        return html.toString();
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public String getHtml() {
        block15: {
            block14: {
                this.logger.debug("getHtml called...");
                htmlReturn = "";
                if (this.sdiInfo == null) break block14;
                if ("RefType".equals(this.sdiInfo.getSdcid()) && "Spec Condition".equals(this.sdiInfo.getString("primary", 0, "reftypeid"))) {
                    this.element.setProperty("viewonly", "Y");
                } else if ("RefType".equals(this.sdiInfo.getSdcid()) && "Time Zone".equals(this.sdiInfo.getString("primary", 0, "reftypeid"))) {
                    this.element.setProperty("viewonly", "Y");
                }
                if (!this.loadProperties()) break block15;
                rsetId = this.pageContext.getRequest().getParameter("__" + this.elementid + "_rsetid");
                clearrsetId = false;
                if (this.isRetainRsetValue()) {
                    rsetClear = this.pageContext.getRequest().getParameter("__" + this.elementid + "_rsetidinvalid");
                    if (rsetClear != null && rsetClear.equalsIgnoreCase("y")) {
                        clearrsetId = true;
                    }
                } else {
                    clearrsetId = true;
                }
                switch (this.linkType) {
                    case 0: {
                        htmlReturn = this.renderDetailHTML(this.fromsdcId, this.linkId, this.buttonPlacement, this.addFromLookup, this.addLookupPage, this.lookupSdcid, this.lookupTableid, this.lookupQueryWhere, this.multiSelect, this.lookupColumns, this.lookupColumnTitles, this.lookupReturnColumns, this.lookupColumnMap, this.lookupUniqueColumns, this.showProgress, this.useCustomLookup, this.lookupCallback, this.viewOnly, this.lookupDirectives, this.lookupRestrictiveWhere, this.lookupUseSapphrieDialog, this.mergeLookup, this.editPage, this.editTarget, this.sorted);
                        if ("RefType".equals(this.sdiInfo.getSdcid()) && "Spec Condition".equals(this.sdiInfo.getString("primary", 0, "reftypeid"))) {
                            htmlReturn = "<p style=\"color:red\">" + this.getTranslationProcessor().translate("Spec Condition Ref Values are maintained in DataEntry Policy") + htmlReturn;
                            ** break;
                        }
                        if ("RefType".equals(this.sdiInfo.getSdcid()) && "Time Zone".equals(this.sdiInfo.getString("primary", 0, "reftypeid"))) {
                            htmlReturn = "<p style=\"color:red\">" + this.getTranslationProcessor().translate("Time Zone Reference Values cannot be modified.") + htmlReturn;
                            ** break;
                        }
                        break block15;
                    }
                    case 1: {
                        htmlReturn = this.renderFKeyHTML(this.fromsdcId, this.tosdcId, this.linkId, rsetId, clearrsetId, this.buttonPlacement, this.addFromLookup, this.addLookupPage, this.lookupSdcid, this.lookupTableid, this.lookupQueryWhere, this.multiSelect, this.lookupColumns, this.lookupColumnTitles, this.lookupReturnColumns, this.lookupColumnMap, this.lookupUniqueColumns, this.showProgress, this.useCustomLookup, this.lookupCallback, this.viewOnly, this.lookupDirectives, this.lookupRestrictiveWhere, this.lookupUseSapphrieDialog, this.mergeLookup, this.editPage, this.editTarget, this.fkWhereClause, this.sorted);
                        ** break;
                    }
                    case 2: {
                        htmlReturn = this.renderRelationalHTML(this.fromsdcId, this.tosdcId, this.fromcolumn1, this.tocolumn1, this.fromcolumn2, this.tocolumn2, this.fromcolumn3, this.tocolumn3, rsetId, clearrsetId, this.buttonPlacement, this.addFromLookup, this.addLookupPage, this.lookupSdcid, this.lookupTableid, this.lookupQueryWhere, this.multiSelect, this.lookupColumns, this.lookupColumnTitles, this.lookupReturnColumns, this.lookupColumnMap, this.lookupUniqueColumns, this.showProgress, this.useCustomLookup, this.lookupCallback, this.viewOnly, this.lookupDirectives, this.lookupRestrictiveWhere, this.lookupUseSapphrieDialog, this.mergeLookup, this.editPage, this.editTarget, this.fkWhereClause, this.sorted);
                        ** break;
                    }
                    default: {
                        this.logger.error("Unknown link type.");
                        ** break;
                    }
                }
lbl38:
                // 5 sources

                break block15;
            }
            this.logger.error("Element not bounded by SDI Tag - sdiInfo not populated.");
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            htmlReturn = this.getError();
        }
        return htmlReturn;
    }

    @Override
    public boolean isVisibleInAddMode() {
        String showinaddmode = this.element.getProperty("showinaddmode", "");
        if ("Y".equalsIgnoreCase(showinaddmode)) {
            return true;
        }
        if ("N".equalsIgnoreCase(showinaddmode)) {
            return false;
        }
        String toSDC = "";
        try {
            toSDC = this.element.getPropertyList(PROPERTYLIST_LINK).getPropertyList(PROPERTYLIST_FKEY).getProperty(PROPERTY_TOSDCID);
        }
        catch (Exception e) {
            toSDC = "";
        }
        return toSDC == null || toSDC.trim().length() == 0;
    }

    public boolean isRetainRsetValue() {
        boolean retainrset = true;
        String resetValue = this.element.getProperty("retainrset", "");
        if ("N".equalsIgnoreCase(resetValue)) {
            retainrset = false;
        }
        return retainrset;
    }

    private class SDITagInfoAccessor
    extends SDITagInfo {
        public SDITagInfoAccessor(HashMap querydatamap) {
            super(querydatamap);
        }
    }
}

