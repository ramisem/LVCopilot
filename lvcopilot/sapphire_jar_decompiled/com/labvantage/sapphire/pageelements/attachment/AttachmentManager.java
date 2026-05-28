/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.File;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentManager
extends BaseElement {
    private static final String PROPERTY_KEYID1 = "keyid1";
    private static final String PROPERTY_SDIDATAID = "sdidataid";
    private static final String PROPERTY_SDIDATAITEMID = "sdidataitemid";
    private static final String PROPERTY_PARAMLISTID = "paramlistid";
    private static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    private static final String PROPERTY_VARIANTID = "variantid";
    private static final String PROPERTY_DATASET = "dataset";
    private static final String PROPERTY_PARAMID = "paramid";
    private static final String PROPERTY_PARAMTYPE = "paramtype";
    private static final String PROPERTY_REPLICATEID = "replicateid";
    private static final String PROPERTY_KEYID2 = "keyid2";
    private static final String PROPERTY_KEYID3 = "keyid3";
    private static final String PROPERTY_SDCID = "sdcid";
    private static final String PROPERTY_RSETID = "rsetid";
    private static final String PROPERTY_TITLE = "title";
    private static final String PROPERTY_TABTITLE = "tabtitle";
    private static final String PROPERTY_SHOWPRIMARY = "showprimary";
    private static final String PROPERTY_ALLOWEDIT = "allowedit";
    private static final String PROPERTY_BUTTONPOS = "buttonpos";
    private static final String PROPERTY_VIEWONLY = "viewonly";
    private static final String PROPERTY_THUMBNAIL = "thumbnail";
    private static final String PROPERTY_CANRESTORE = "canrestore";
    private static final String PROPERTY_BUTTONS = "buttons";
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_TEXT = "text";
    private static final String PROPERTY_TIP = "tip";
    private static final String PROPERTY_IMG = "img";
    private static final String PROPERTY_JS = "js";
    private static final String PROPERTY_COLUMNS = "columns";
    private static final String PROPERTY_COLUMNID = "columnid";
    private static final String PROPERTY_MODE = "mode";
    private static final String PROPERTY_TYPES = "types";
    private static final String PROPERTY_CONTENTEDITABLE = "contenteditable";
    private static final String PROPERTY_TYPE = "type";
    protected static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_FIELDNAME = "fieldname";
    private static final String PROPERTY_MANDATORY = "mandatory";
    private static final String PROPERTY_ONCLOSE = "onclose";
    private static final String PROPERTY_SHOWTAB = "showtab";
    private static final String PROPERTY_INNERSCROLL = "innerscroll";
    private static final String PROPERTY_GREYBUTTONS = "greybuttons";
    private static final String PROPERTY_ALLOWOVERWRITE = "allowoverwrite";
    private static final int BTNPOS_TOP = 0;
    private static final int BTNPOS_BOTTOM = 1;
    private static final int BTNPOS_BOTH = 2;
    private static final int BTNPOS_NONE = 3;
    public static final String UPLOAD_FILELOCATION = "Upload";
    protected static final String MAIN_JS_OBJECT = "attachmentManager";
    private static final String ATT_JS_OBJECT = "attachments";
    private static final String ATTACHMENT_CODE = SDIData.getDatasetCode("attachment");
    private static final int ACTIONSWIDTH = 95;
    private static final int ACTIONSWIDTH_VO = 50;
    protected static final int DEFAULTWIDTH = 140;
    private static final int UPLOADWIDTH = 190;
    private static final int UPLOADWIDTH_VO = 1;
    private static final int MODE_SDC = 0;
    private static final int MODE_DATASET = 1;
    private static final int MODE_DATAITEM = 2;
    public String keyid1;
    public String keyid2;
    public String keyid3;
    public String sdcid;
    private String plkeyid1;
    private String plkeyid2;
    private String plkeyid3;
    private String plsdcid;
    private String rsetid;
    private String paramlistid;
    private String paramlistversionid;
    private String variantid;
    private String datasetnumber;
    private String paramid;
    private String paramtype;
    private String replicateid;
    private int mode;
    private String title = "Attachment Manager";
    private String tabtitle = "[sdcid] [keycolid1] ([rowcount])";
    private boolean allowedit = false;
    private String allowOverwrite = "Prompt";
    private boolean showprimary = true;
    private int buttonpos = 1;
    private boolean viewonly = false;
    private boolean thumbnails = false;
    private boolean canRestore = true;
    private boolean showTab = true;
    private String onclose = "";
    private boolean renderToolbar = true;
    private String amPage = "";
    private boolean innerScroll = false;
    private boolean greyButtons = false;
    private PropertyList contentEditable = new PropertyList();
    private PropertyList attachmentPolicy;

    public AttachmentManager(PageContext pageContext, PropertyList pageproperties) {
        try {
            this.setPageContext(pageContext);
            this.setElementProperties(pageproperties);
        }
        catch (Exception e) {
            this.debugErrorMsg = e.getMessage();
            this.logger.error(this.debugErrorMsg);
        }
    }

    public AttachmentManager(PageContext pageContext) {
        try {
            this.setPageContext(pageContext);
        }
        catch (Exception e) {
            this.debugErrorMsg = e.getMessage();
            this.logger.error(this.debugErrorMsg);
        }
    }

    private int getWidth(PropertyList col, int correction) {
        int width;
        try {
            width = Integer.parseInt(col.getProperty(PROPERTY_WIDTH, "140"));
        }
        catch (Exception e) {
            width = 140;
        }
        return width + correction;
    }

    private boolean loadProperties() {
        try {
            this.attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", "Sapphire Custom");
        }
        catch (SapphireException e) {
            this.logger.error("Failed to load AttachmentPolicy");
        }
        boolean out = true;
        PropertyList pageInfo = this.element;
        this.amPage = "rc?command=";
        String f = pageInfo.getProperty("command", this.pageContext != null && this.pageContext.getRequest().getParameter("command") != null ? this.pageContext.getRequest().getParameter("command") : "page");
        this.amPage = f.equalsIgnoreCase("file") ? this.amPage + "file&file=" + pageInfo.getProperty("file", this.pageContext != null && this.pageContext.getRequest().getParameter("file") != null ? this.pageContext.getRequest().getParameter("file") : "") : this.amPage + "page&page=" + pageInfo.getProperty("page", this.pageContext != null && this.pageContext.getRequest().getParameter("page") != null ? this.pageContext.getRequest().getParameter("page") : "AttachmentManager");
        this.onclose = pageInfo.getProperty(PROPERTY_ONCLOSE, "");
        this.logger.debug("onclose = " + this.onclose);
        this.renderToolbar = this.element.containsKey("advancedtoolbar");
        this.logger.debug("renderToolbar = " + this.renderToolbar);
        this.sdcid = pageInfo.getProperty(PROPERTY_SDCID, "");
        if (this.sdcid.length() > 0) {
            this.keyid1 = pageInfo.getProperty(PROPERTY_KEYID1, "");
            if (this.keyid1.length() > 0) {
                this.keyid2 = pageInfo.getProperty(PROPERTY_KEYID2, "");
                this.keyid3 = pageInfo.getProperty(PROPERTY_KEYID3, "");
                if (pageInfo.containsKey(PROPERTY_SDIDATAID) || pageInfo.containsKey(PROPERTY_SDIDATAITEMID) || pageInfo.containsKey(PROPERTY_PARAMLISTID)) {
                    this.plkeyid1 = this.keyid1;
                    this.plkeyid2 = this.keyid2;
                    this.plkeyid3 = this.keyid3;
                    this.plsdcid = this.sdcid;
                    this.paramlistid = pageInfo.getProperty(PROPERTY_PARAMLISTID, "");
                    if (this.paramlistid.length() > 0) {
                        this.logger.debug("paramlistid = " + this.paramlistid);
                        this.paramlistversionid = pageInfo.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
                        if (this.paramlistversionid.length() > 0) {
                            this.logger.debug("paramlistid = " + this.paramlistid);
                            this.logger.debug("paramlistversionid = " + this.paramlistversionid);
                            this.variantid = pageInfo.getProperty(PROPERTY_VARIANTID, "");
                            if (this.variantid.length() > 0) {
                                this.logger.debug("variantid = " + this.variantid);
                                this.datasetnumber = pageInfo.getProperty(PROPERTY_DATASET, "1");
                                this.logger.debug("datasetnumber = " + this.datasetnumber);
                                if (pageInfo.containsKey(PROPERTY_SDIDATAITEMID) || pageInfo.containsKey(PROPERTY_PARAMID)) {
                                    this.mode = 2;
                                    this.keyid1 = pageInfo.getProperty(PROPERTY_SDIDATAITEMID, "");
                                    this.keyid2 = "";
                                    this.keyid3 = "";
                                    this.sdcid = "DataItem";
                                    this.paramid = pageInfo.getProperty(PROPERTY_PARAMID, "");
                                    if (this.paramid.length() > 0) {
                                        this.logger.debug("paramid = " + this.paramid);
                                        this.paramtype = pageInfo.getProperty(PROPERTY_PARAMTYPE, "");
                                        if (this.paramtype.length() > 0) {
                                            this.logger.debug("paramtype = " + this.paramtype);
                                            this.replicateid = pageInfo.getProperty(PROPERTY_REPLICATEID, "1");
                                            this.logger.debug("replicateid = " + this.replicateid);
                                        } else {
                                            out = false;
                                            this.logger.error("DataItem not given parameter type.");
                                        }
                                    } else {
                                        out = false;
                                        this.logger.error("DataItem not given parameter id.");
                                    }
                                } else {
                                    this.keyid1 = pageInfo.getProperty(PROPERTY_SDIDATAID, "");
                                    this.keyid2 = "";
                                    this.keyid3 = "";
                                    this.sdcid = "DataSet";
                                    this.mode = 1;
                                }
                            } else {
                                out = false;
                                this.logger.error("DataItem or DataSet not given variant id.");
                            }
                        } else {
                            out = false;
                            this.logger.error("DataItem or DataSet not given parameter list version id.");
                        }
                    } else {
                        out = false;
                        this.logger.error("DataItem or DataSet not given parameter list id.");
                    }
                } else {
                    this.mode = 0;
                    this.plkeyid1 = "";
                    this.plkeyid2 = "";
                    this.plkeyid3 = "";
                    this.plsdcid = "";
                }
                if (out) {
                    this.logger.debug("keyid1 = " + this.keyid1);
                    this.logger.debug("keyid2 = " + this.keyid2);
                    this.logger.debug("keyid3 = " + this.keyid3);
                    this.logger.debug("sdcid = " + this.sdcid);
                    this.logger.debug("plkeyid1 = " + this.plkeyid1);
                    this.logger.debug("plkeyid2 = " + this.plkeyid2);
                    this.logger.debug("plkeyid3 = " + this.plkeyid3);
                    this.logger.debug("plsdcid = " + this.plsdcid);
                    this.logger.debug("mode = " + this.mode);
                    this.rsetid = pageInfo.getProperty(PROPERTY_RSETID, "");
                    this.logger.debug("rsetid = " + this.rsetid);
                    try {
                        int count = this.getQueryProcessor().getPreparedCount("SELECT count(rsetid) FROM rsetitems WHERE rsetid = ?", new Object[]{this.rsetid});
                        if (count == 0) {
                            this.logger.info("Rset removed. Generate new");
                            this.rsetid = "";
                        }
                    }
                    catch (Exception e) {
                        this.logger.info("Rset expired. Generate new");
                        this.rsetid = "";
                    }
                    PropertyList pagedata = this.element.getPropertyList("pagedata");
                    if (pagedata == null) {
                        pagedata = this.element;
                    }
                    if (pagedata != null) {
                        this.tabtitle = pagedata.getProperty(PROPERTY_TABTITLE, this.tabtitle);
                        this.logger.debug("tabtitle = " + this.tabtitle);
                        this.allowOverwrite = pagedata.getProperty(PROPERTY_ALLOWOVERWRITE, this.allowOverwrite);
                        this.logger.debug("allowOverwrite = " + this.allowOverwrite);
                        this.showTab = pagedata.getProperty(PROPERTY_SHOWTAB, this.showTab ? "Y" : "N").equalsIgnoreCase("Y");
                        this.logger.debug("showTab = " + this.showTab);
                        this.title = pagedata.getProperty(PROPERTY_TITLE, this.title);
                        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, this.viewonly ? "Y" : "N").equalsIgnoreCase("Y");
                        this.logger.debug("viewonly = " + this.viewonly);
                        if (this.viewonly) {
                            this.thumbnails = pagedata.getProperty(PROPERTY_THUMBNAIL, this.thumbnails ? "Y" : "N").equalsIgnoreCase("Y");
                            this.logger.debug("thumbnails = " + this.thumbnails);
                        }
                        this.canRestore = !pagedata.getProperty(PROPERTY_CANRESTORE, this.canRestore ? "Y" : "N").equalsIgnoreCase("N");
                        this.logger.debug("canRestore = " + this.canRestore);
                        this.allowedit = pagedata.getProperty(PROPERTY_ALLOWEDIT, this.allowedit ? "Y" : "N").equalsIgnoreCase("Y");
                        this.logger.debug("allowedit = " + this.allowedit);
                        this.showprimary = pagedata.getProperty(PROPERTY_SHOWPRIMARY, this.showprimary ? "Y" : "N").equalsIgnoreCase("Y");
                        this.logger.debug("showprimary = " + this.showprimary);
                        this.innerScroll = !pagedata.getProperty(PROPERTY_INNERSCROLL, this.innerScroll ? "Y" : "N").equalsIgnoreCase("N");
                        this.logger.debug("innerScroll = " + this.innerScroll);
                        this.greyButtons = !pagedata.getProperty(PROPERTY_GREYBUTTONS, this.greyButtons ? "Y" : "N").equalsIgnoreCase("N");
                        this.logger.debug("greyButtons = " + this.greyButtons);
                        String temp = pagedata.getProperty(PROPERTY_BUTTONPOS, "");
                        this.logger.debug("buttonpos (1) = " + temp);
                        this.buttonpos = temp.equalsIgnoreCase("top") ? 0 : (temp.equalsIgnoreCase("both") ? 2 : (temp.equalsIgnoreCase("none") ? 3 : 1));
                        this.logger.debug("buttonpos (2) = " + this.buttonpos);
                        PropertyListCollection types = this.element.containsKey("pagedata") ? this.element.getPropertyList("pagedata").getCollection(PROPERTY_TYPES) : this.element.getCollection(PROPERTY_TYPES);
                        if (types != null && types.size() > 0) {
                            for (int i = 0; i < types.size(); ++i) {
                                PropertyList tempPL = types.getPropertyList(i);
                                String typeflag = Attachment.correctTypeFlag(tempPL.getProperty(PROPERTY_TYPE));
                                String contentEditableFlag = tempPL.getProperty(PROPERTY_CONTENTEDITABLE, "N");
                                this.contentEditable.setProperty(typeflag, contentEditableFlag);
                            }
                        } else {
                            String canRestoreString = this.canRestore ? "Y" : "N";
                            this.contentEditable.setProperty("R", canRestoreString);
                            this.contentEditable.setProperty("U", canRestoreString);
                            this.contentEditable.setProperty("L", canRestoreString);
                            this.contentEditable.setProperty("P", canRestoreString);
                            this.contentEditable.setProperty("D", canRestoreString);
                            this.contentEditable.setProperty("M", canRestoreString);
                            this.contentEditable.setProperty("S", canRestoreString);
                            this.contentEditable.setProperty("F", canRestoreString);
                        }
                    } else {
                        out = false;
                        this.logger.error("Could not obtain properties.");
                    }
                }
            } else {
                this.logger.error("KeyId1 not provided.");
                out = false;
            }
        } else {
            this.logger.error("SDCId not provided.");
            out = false;
        }
        return out;
    }

    private SDIData getEmptyAttachmentData(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid, int mode) {
        SDIData sdidata = null;
        DataSet pr = new DataSet();
        pr.addColumn(PROPERTY_SDCID, 0);
        pr.addColumn(PROPERTY_KEYID1, 0);
        pr.addColumn(PROPERTY_KEYID2, 0);
        pr.addColumn(PROPERTY_KEYID3, 0);
        pr.addColumn(PROPERTY_PARAMLISTID, 0);
        pr.addColumn(PROPERTY_PARAMLISTVERSIONID, 0);
        pr.addColumn(PROPERTY_VARIANTID, 0);
        pr.addColumn(PROPERTY_DATASET, 1);
        int row = pr.addRow();
        pr.setValue(row, PROPERTY_SDCID, sdcid);
        pr.setValue(row, PROPERTY_KEYID1, keyid1);
        pr.setValue(row, PROPERTY_KEYID2, keyid2);
        pr.setValue(row, PROPERTY_KEYID3, keyid3);
        pr.setValue(row, PROPERTY_PARAMLISTID, paramlistid);
        pr.setValue(row, PROPERTY_PARAMLISTVERSIONID, paramlistversionid);
        pr.setValue(row, PROPERTY_VARIANTID, variantid);
        pr.setNumber(row, PROPERTY_DATASET, dataset);
        if (mode == 2) {
            sdidata = new SDIData("DataItem");
            pr.addColumn(PROPERTY_PARAMID, 0);
            pr.addColumn(PROPERTY_PARAMTYPE, 0);
            pr.addColumn(PROPERTY_REPLICATEID, 1);
            pr.setValue(row, PROPERTY_PARAMID, paramid);
            pr.setValue(row, PROPERTY_PARAMTYPE, paramtype);
            pr.setNumber(row, PROPERTY_REPLICATEID, replicateid);
        } else if (mode == 1) {
            sdidata = new SDIData("DataSet");
        }
        if (sdidata != null) {
            DataSet att = new DataSet();
            att.addColumn(PROPERTY_SDCID, 0);
            att.addColumn(PROPERTY_KEYID1, 0);
            att.addColumn(PROPERTY_KEYID2, 0);
            att.addColumn(PROPERTY_KEYID3, 0);
            att.addColumn("attachmentnum", 1);
            att.addColumn("attachmentdesc", 0);
            att.addColumn("filename", 0);
            att.addColumn("url", 0);
            att.addColumn("sourcefilename", 0);
            att.addColumn("typeflag", 0);
            sdidata.setDataset("primary", pr);
            sdidata.setDataset("attachment", att);
        }
        return sdidata;
    }

    private SDIData getAttachmentData(String thesdcid, String thekeyid1, String thekeyid2, String thekeyid3, String rsetid, int mode, boolean viewOnly) {
        SDIProcessor sdi = new SDIProcessor(this.pageContext);
        SDIRequest req = new SDIRequest();
        req.setExtendedDataTypes(true);
        switch (mode) {
            case 1: {
                PropertyListCollection priColumns;
                if (!this.showprimary) {
                    req.setRequestItem("primary[sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset]");
                    break;
                }
                PropertyListCollection propertyListCollection = priColumns = this.element.getPropertyList("pagedata") != null ? this.element.getPropertyList("pagedata").getCollection("primarycolumns") : this.element.getCollection("primarycolumns");
                if (priColumns != null && priColumns.size() > 0) {
                    StringBuffer request = new StringBuffer();
                    for (int c = 0; c < priColumns.size(); ++c) {
                        PropertyList priCol = priColumns.getPropertyList(c);
                        if (request.length() > 0) {
                            request.append(",");
                        }
                        request.append(priCol.getProperty(PROPERTY_COLUMNID));
                    }
                    req.setRequestItem("primary[" + request.toString() + "]");
                    break;
                }
                req.setRequestItem("primary[sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset]");
                break;
            }
            case 2: {
                PropertyListCollection priColumns;
                if (!this.showprimary) {
                    req.setRequestItem("primary[sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid,paramtype,replicateid]");
                    break;
                }
                PropertyListCollection propertyListCollection = priColumns = this.element.getPropertyList("pagedata") != null ? this.element.getPropertyList("pagedata").getCollection("primarycolumns") : this.element.getCollection("primarycolumns");
                if (priColumns != null && priColumns.size() > 0) {
                    StringBuffer request = new StringBuffer();
                    for (int c = 0; c < priColumns.size(); ++c) {
                        PropertyList priCol = priColumns.getPropertyList(c);
                        if (request.length() > 0) {
                            request.append(",");
                        }
                        request.append(priCol.getProperty(PROPERTY_COLUMNID));
                    }
                    req.setRequestItem("primary[" + request.toString() + "]");
                    break;
                }
                req.setRequestItem("primary[sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid,paramtype,replicateid]");
                break;
            }
            case 0: {
                PropertyListCollection priColumns;
                if (!this.showprimary) {
                    req.setRequestItem("primary[]");
                    break;
                }
                PropertyListCollection propertyListCollection = priColumns = this.element.getPropertyList("pagedata") != null ? this.element.getPropertyList("pagedata").getCollection("primarycolumns") : this.element.getCollection("primarycolumns");
                if (priColumns != null && priColumns.size() > 0) {
                    StringBuffer request = new StringBuffer();
                    for (int c = 0; c < priColumns.size(); ++c) {
                        PropertyList priCol = priColumns.getPropertyList(c);
                        if (request.length() > 0) {
                            request.append(",");
                        }
                        request.append(priCol.getProperty(PROPERTY_COLUMNID));
                    }
                    req.setRequestItem("primary[" + request.toString() + "]");
                    break;
                }
                req.setRequestItem("primary[]");
                break;
            }
            default: {
                req.setRequestItem("primary[]");
            }
        }
        req.setRequestItem("attachment");
        if (rsetid.length() > 0) {
            req.setRsetid(rsetid);
        }
        req.setSDCid(thesdcid);
        req.setKeyid1List(thekeyid1);
        if (thekeyid2.length() > 0) {
            req.setKeyid2List(thekeyid2);
        }
        if (thekeyid3.length() > 0) {
            req.setKeyid3List(thekeyid3);
        }
        if (viewOnly) {
            req.setPrimaryLockOption("");
        } else {
            req.setPrimaryLockOption("LA");
        }
        req.setDataLockOption("");
        req.setLockOption("");
        req.setRetainRsetid(true);
        return sdi.getSDIData(req);
    }

    private SDITagInfo createSDIInfo(DataSet data, String dataSetName) {
        HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
        if (!data.isValidColumn("__rowstatus")) {
            data.addColumn("__rowstatus", 0);
            data.setString(-1, "__rowstatus", "S");
        }
        if (!data.isValidColumn("__rowid")) {
            data.addColumn("__rowid", 0);
            for (int row = 0; row < data.getRowCount(); ++row) {
                data.setString(row, "__rowid", "" + row);
            }
        }
        QueryData querydata = new QueryData(data);
        querymap.put(dataSetName, querydata);
        return new SDITagInfo(querymap);
    }

    private void renderKeysInputs(String row, StringBuffer content, String sdcid, String keyid1, String keyid2, String keyid3, String connectionid) {
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_sdcid\" name=\"sdcid\" type=\"hidden\" value=\"").append(sdcid).append("\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_keyid1\" name=\"keyid1\" type=\"hidden\" value=\"").append(keyid1).append("\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_keyid2\" name=\"keyid2\" type=\"hidden\" value=\"").append(keyid2).append("\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_keyid3\" name=\"keyid3\" type=\"hidden\" value=\"").append(keyid3).append("\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_rownum\" name=\"rownum\" type=\"hidden\" value=\"").append(row).append("\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_auditreason\" name=\"auditreason\" type=\"hidden\" value=\"\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_auditactivity\" name=\"auditactivity\" type=\"hidden\" value=\"\">");
        content.append("<input id=\"").append(ATTACHMENT_CODE).append(row).append("_auditsignedflag\" name=\"auditsignedflag\" type=\"hidden\" value=\"\">");
    }

    private void renderActionColumn(StringBuffer content, int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp) {
        content.append("&nbsp;");
        content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_disabled\" title=\"").append(tp.translate("View attachment")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\">");
        content.append("&nbsp;");
        content.append("<img id=\"btDownloadAction").append(row).append("\" class=\"btn_disabled\" title=\"").append(tp.translate("Download attachment to your PC")).append("\" src=\"WEB-CORE/images/gif/DownloadAttachment.gif\">");
        content.append("&nbsp;");
        content.append("<img id=\"btEditAction").append(row).append("\" class=\"btn_disabled\" title=\"").append(tp.translate("Edit attachment on your PC")).append("\" src=\"WEB-CORE/images/gif/EditAttachment.gif\">");
        content.append("&nbsp;");
        content.append("<img id=\"btResetAction").append(row).append("\" class=\"btn_disabled\" title=\"").append(tp.translate("Reset changes")).append("\" src=\"WEB-CORE/images/gif/Reset.gif\" onclick=\"").append(MAIN_JS_OBJECT).append(".executeReset( ").append("-1").append(", ").append(row).append(" )\">");
    }

    private void renderTable(StringBuffer content, PropertyListCollection columns, DataSet data, String sdcid, String keyid1, String keyid2, String keyid3, boolean viewOnly, boolean allowEdit, boolean canRestore, boolean isLocked, String lockedby, PropertyListCollection uploadData, TranslationProcessor tp) {
        int i;
        Object colid;
        PropertyList col;
        int i2;
        String allowableTypes = this.getAllowableTypes();
        boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
        int totalwidth = 0;
        for (i2 = 0; i2 < columns.size(); ++i2) {
            col = columns.getPropertyList(i2);
            colid = col.getProperty(PROPERTY_COLUMNID, "");
            if (((String)colid).length() <= 0 || !data.isValidColumn((String)colid) || col.getProperty(PROPERTY_MODE, "").equals("hidden")) continue;
            totalwidth += this.getWidth(col, 5) + (this.browser.isChrome() ? 10 : (this.browser.isSafari() ? 20 : 0));
        }
        totalwidth += 95;
        totalwidth += 190;
        for (int row = 0; row < data.getRowCount(); ++row) {
            String typeflag = Attachment.correctTypeFlag(data.getValue(row, "typeflag", ""));
            if (this.isAllowed(typeflag, allowableTypes)) continue;
            data.deleteRow(row);
            --row;
        }
        content.append("<table id=\"").append(ATTACHMENT_CODE).append("_table").append("\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" rowcount=\"").append(data.getRowCount()).append("\" columncount=\"").append(columns.size()).append("\" style=\"border:none;table-layout:fixed;width:auto;\">\n");
        content.append("<tr class=\"gridmaint_tablehead\" style=\"height:20px;\">\n");
        if (!viewOnly) {
            content.append("<td class=\"gridmaint_fieldtitle\" style=\"width:30px;\">\n");
            content.append("<input type=checkbox id=\"").append(ATTACHMENT_CODE).append("_checkall\" onclick=\"").append(MAIN_JS_OBJECT).append(".checkAll()\">");
            content.append("</td>");
        }
        content.append("<td>");
        content.append("<table cellpadding=0 cellspacing=0 style=\"table-layout:fixed;width:").append(totalwidth).append("px;\">");
        content.append("<tr style=\"height:22px;\">");
        for (i2 = 0; i2 < columns.size(); ++i2) {
            col = columns.getPropertyList(i2);
            colid = col.getProperty(PROPERTY_COLUMNID, "");
            if (((String)colid).length() <= 0 || !data.isValidColumn((String)colid) || col.getProperty(PROPERTY_MODE, "").equals("hidden")) continue;
            content.append("<td class=\"gridmaint_fieldtitle\" style=\"border-top:none;").append(viewOnly ? "" : (rtl ? "border-right:none;" : "border-left:none;")).append("width:").append(this.getWidth(col, 5) + (this.browser.isChrome() ? 10 : (this.browser.isSafari() ? 20 : 0))).append("px;\">").append(col.getProperty(PROPERTY_TITLE, "")).append("</td>\n");
        }
        int awidth = viewOnly ? 95 : 95;
        content.append("<td class=\"gridmaint_fieldtitle\" style=\"border-top:none;").append(rtl ? "border-right:none;border-left:none;" : "border-left:none;border-right:none;").append("width:").append(awidth).append("px;\">\n");
        content.append("&nbsp;");
        content.append("</td>");
        int uwidth = viewOnly ? 1 : 190;
        content.append("<td class=\"\" style=\"border-top:none;border:none;width:").append(uwidth).append("px;\">\n");
        content.append("&nbsp;");
        content.append("</td>");
        content.append("</tr>");
        content.append("</table>");
        content.append("</td>");
        content.append("</tr>");
        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
            content.append(type.getOtherHtml());
        }
        this.setSDIInfo(this.createSDIInfo(data, "attachment"));
        QueryData querydata = this.sdiInfo.getQueryData("attachment");
        for (int row = 0; row < querydata.getRowCount(); ++row) {
            String typeflag = Attachment.correctTypeFlag(data.getValue(row, "typeflag", "S"));
            BaseAttachmentType type = Attachment.getAttachmentType(typeflag);
            boolean attachmentClassEditable = this.isAttachmentClassEditable(data.getValue(row, "attachmentclass", ""));
            boolean editableflagcol = type.isAllowedEdit(row, data);
            boolean contentEditableFlag = !"N".equals(this.contentEditable.getProperty(typeflag)) && attachmentClassEditable;
            boolean showEditIcon = allowEdit && editableflagcol && contentEditableFlag;
            boolean showDownloadIcon = type.isAllowedDownload(row, data);
            content.append("<tr id=\"tablerow_").append(row).append("\" row=\"").append(row).append("\">");
            if (!viewOnly) {
                content.append("<td id=\"tablecell_").append(row).append("_check\" style=\"width:30px;padding-bottom:1px;border-top:none;\" ");
                if (isLocked) {
                    content.append(" class=\"maint_lockedfield\" ");
                } else {
                    content.append(" class=\"gridmaint_field\" ");
                }
                content.append(">\n");
                content.append("<input type=checkbox id=\"check_").append(row).append("\">");
                content.append("</td>");
            }
            content.append("<td class=\"\" id=\"tablecell_").append(row).append("_container\">");
            content.append("<form  method=POST enctype=\"multipart/form-data\" target=\"upload_frame\" action=\"rc?command=attachment").append("\" id=\"").append(ATTACHMENT_CODE).append("_form_").append(row).append("\" name=\"").append(ATTACHMENT_CODE).append("_form_").append(row).append("\" style=\"margin-top:0px;margin-bottom:0px;\">");
            this.renderKeysInputs("" + row, content, sdcid, keyid1, keyid2, keyid3, this.getConnectionId());
            content.append("<input type=\"hidden\" name=\"file1_").append("rs\" id=\"__").append(ATTACHMENT_CODE).append(row).append("_rs\" value=\"").append("S").append("\">\n");
            content.append("<table  border=0 cellpadding=0 cellspacing=0 style=\"height:26px;table-layout:fixed;width:").append(totalwidth).append("px;\">");
            content.append("<tr id=\"innertablerow_").append(row).append("\">\n");
            querydata.setCurrentRow(row);
            String contentValue = type.getContentValue(data, row);
            for (int i3 = 0; i3 < columns.size(); ++i3) {
                PropertyList col2 = columns.getPropertyList(i3);
                String colid2 = col2.getProperty(PROPERTY_COLUMNID, "");
                if (colid2.length() > 0) {
                    if (data.isValidColumn(colid2)) {
                        if (!col2.getProperty(PROPERTY_MODE, "").equals("hidden")) {
                            content.append("<td style=\"border-top:none;").append(i3 == 0 && viewOnly ? "" : (rtl ? "border-right:none;" : "border-left:none;")).append(";width:").append(this.getWidth(col2, 5) + (this.browser.isChrome() ? 10 : (this.browser.isSafari() ? 20 : 0))).append("px;\" id=\"tablecell_").append(row).append("_").append(i3).append("\" ");
                            if (isLocked) {
                                content.append(" class=\"maint_lockedfield\" ");
                            } else {
                                content.append(" class=\"gridmaint_field\" ");
                            }
                            content.append(">\n");
                        }
                        String name = col2.getProperty(PROPERTY_FIELDNAME, "");
                        if (colid2.equalsIgnoreCase("filename")) {
                            String hint;
                            boolean hidden = col2.getProperty(PROPERTY_MODE, "").equals("hidden");
                            if (hidden) {
                                content.append("<td id=\"").append(ATTACHMENT_CODE).append(row).append("_server\" style=\"display:none;").append("\">");
                                hint = type.getHint(typeflag, data, row, contentValue, tp);
                                type.getFilenameFieldInitialRender(this, content, viewOnly || !showEditIcon, showEditIcon, isLocked, row, typeflag, contentValue, col2, name, hint, tp, this.browser);
                                content.append("</td>");
                            } else {
                                content.append("<div id=\"").append(ATTACHMENT_CODE).append(row).append("_server\" style=\"display:block;position:relative;").append(this.browser.isChrome() ? "height:26px;" : "").append("\">");
                                hint = type.getHint(typeflag, data, row, contentValue, tp);
                                type.getFilenameFieldInitialRender(this, content, viewOnly || !showEditIcon, showEditIcon, isLocked, row, typeflag, contentValue, col2, name, hint, tp, this.browser);
                                content.append("</div>");
                            }
                        } else {
                            if (colid2.equalsIgnoreCase("typeflag")) {
                                col2.setProperty("displayvalue", Attachment.getSelectedDisplayValue(allowableTypes));
                                col2 = col2.copy();
                                col2.setProperty(PROPERTY_MODE, "hidden");
                            }
                            boolean colMadeReadonly = false;
                            if (!(editableflagcol || "hidden".equals(col2.getProperty(PROPERTY_MODE)) || "retrievedata".equals(col2.getProperty(PROPERTY_MODE)))) {
                                col2 = col2.copy();
                                col2.setProperty(PROPERTY_MODE, "readonly");
                                colMadeReadonly = true;
                            }
                            MaintColumn mc = new MaintColumn(this.pageContext, this.sdiInfo, this.getConnectionId());
                            if (!colid2.equalsIgnoreCase("typeflag") && !colid2.equalsIgnoreCase("attachmentdesc") && !colid2.equalsIgnoreCase("attachmentnum") && col2.getProperty(PROPERTY_MANDATORY, "N").equalsIgnoreCase("Y")) {
                                col2.setProperty("validation", "Mandatory");
                            }
                            mc.setColumn(col2);
                            mc.setDatasetname("attachment");
                            String inputhtml = mc.getHtml();
                            if (name.length() > 0) {
                                String id = mc.getId();
                                inputhtml = StringUtil.replaceAll(inputhtml, "name=\"" + id + "\"", "name=\"" + name + "\"");
                            }
                            inputhtml = inputhtml.indexOf("style=\"") > -1 ? StringUtil.replaceAll(inputhtml, "style=\"", "style=\"width:" + this.getWidth(col2, 0) + "px;") : StringUtil.replaceAll(inputhtml, " id=\"", " style=\"width:" + this.getWidth(col2, 0) + "px;\" id=\"");
                            if (colid2.equalsIgnoreCase("attachmentnum") && !colMadeReadonly) {
                                int left = this.getWidth(col2, 0) / 2;
                                inputhtml = inputhtml + "<img id=\"at" + row + "_confirm\" src=\"WEB-CORE/images/gif/Confirm.gif\" style=\"position:relative;" + (rtl ? "right:" : "left:") + left + "px;top:-19px;display:none;\">";
                                left = this.getWidth(col2, 0) - 16 + 5;
                                if (isLocked) {
                                    inputhtml = inputhtml + "<img src=\"WEB-CORE/elements/images/locked.gif\" title=\"" + tp.translate("Locked by") + " " + lockedby + "\" style=\"position:relative;" + (rtl ? "right:" : "left:") + left + "px;top:-14px;\">";
                                }
                            }
                            content.append(inputhtml);
                            if (colid2.equalsIgnoreCase("typeflag") && !colMadeReadonly) {
                                col2 = columns.getPropertyList(i3);
                                mc.setColumn(col2);
                                mc.setDatasetname("attachment");
                                inputhtml = mc.getHtml();
                                inputhtml = inputhtml.indexOf("style=\"") > -1 ? StringUtil.replaceAll(inputhtml, "style=\"", "style=\"width:" + this.getWidth(col2, 0) + "px;") : StringUtil.replaceAll(inputhtml, " id=\"", " style=\"width:" + this.getWidth(col2, 0) + "px;\" id=\"");
                                inputhtml = StringUtil.replaceAll(inputhtml, "<select ", "<select disabled ", false);
                                inputhtml = StringUtil.replaceAll(inputhtml, "name=\"", "_name=\"", false);
                                inputhtml = StringUtil.replaceAll(inputhtml, "id=\"", "_id=\"", false);
                                content.append(inputhtml);
                            }
                        }
                        if (columns.getPropertyList(i3).getProperty(PROPERTY_MODE, "").equals("hidden")) continue;
                        content.append("</td>\n");
                        continue;
                    }
                    this.logger.warn("Column " + colid2 + " not found in data.");
                    continue;
                }
                this.logger.warn("Empty column found.");
            }
            int attnum = Integer.parseInt(querydata.getValue("attachmentnum", "-1"));
            content.append("<td id=\"tablecell_").append(row).append("_").append(columns.size() + 1).append("\" style=\"border-top:none;").append(rtl ? "border-right:none;" : "border-left:none;").append("width:").append(awidth).append("px;\" ");
            if (isLocked) {
                content.append(" class=\"maint_lockedfield\" ");
            } else {
                content.append(" class=\"gridmaint_field\" ");
            }
            content.append(">\n");
            content.append(type.renderActionColumn(attnum, contentValue, "" + row, showEditIcon, showDownloadIcon, viewOnly || isLocked, tp, this.browser));
            content.append("</td>");
            content.append("<td class=\"\" id=\"tablecell_").append(row).append("_").append(columns.size() + 2).append("\" style=\"border:none;width:").append(uwidth).append("px;\">\n");
            content.append(type.renderUploadContainer(typeflag, uploadData, tp, "" + row, viewOnly || !showEditIcon));
            content.append("</td>");
            content.append("</tr>\n");
            content.append("</table>\n");
            content.append("</form>\n");
            content.append("</td></tr>\n");
        }
        content.append("</table>\n");
        content.append("<table id=\"__").append(ATTACHMENT_CODE).append("_templatetable").append("\" style=\"width:").append(totalwidth + 30).append("px;display:none;\">\n");
        content.append("<tr valign=\"top\" id=\"tablerow_[__row]").append("\" row=\"[__row]\">");
        if (!viewOnly) {
            content.append("<td class=\"gridmaint_field\" id=\"tablecell_[__row]").append("_check\"  style=\"width:30px;border-top:none;\">\n");
            content.append("<input type=checkbox id=\"check_[__row]").append("\">");
            content.append("</td>");
        }
        content.append("<td style=\"\" id=\"tablecell_[__row]_container\">");
        content.append("<form method=POST enctype=\"multipart/form-data\" target=\"upload_frame\" action=\"rc?command=attachment").append("\" id=\"").append(ATTACHMENT_CODE).append("_form_[__row]").append("\" name=\"").append(ATTACHMENT_CODE).append("_form_[__row]").append("\" style=\"margin-top:0px;margin-bottom:0px;\">");
        this.renderKeysInputs("[__row]", content, sdcid, keyid1, keyid2, keyid3, this.getConnectionId());
        content.append("<table border=0 cellpadding=0 cellspacing=0 style=\"height:26px;table-layout:fixed;width:").append(totalwidth).append("px;\">");
        content.append("<tr valign=\"top\" id=\"innertablerow_[__row]").append("\">\n");
        querydata.setCurrentRow(-9999);
        content.append("<input type=\"hidden\" name=\"file1_").append("rs\" id=\"__").append(ATTACHMENT_CODE).append("[__row]").append("_rs\" value=\"").append("I").append("\">\n");
        for (i = 0; i < columns.size(); ++i) {
            PropertyList col3 = columns.getPropertyList(i);
            String colid3 = col3.getProperty(PROPERTY_COLUMNID, "");
            if (colid3.length() > 0) {
                if (data.isValidColumn(colid3)) {
                    if (!col3.getProperty(PROPERTY_MODE, "").equals("hidden")) {
                        content.append("<td class=\"gridmaint_field\"  style=\"border-top:none;").append(rtl ? "border-right:none;" : "border-left:none;").append("width:").append(this.getWidth(col3, 5) + (this.browser.isChrome() ? 10 : (this.browser.isSafari() ? 20 : 0))).append("px;\" id=\"tablecell_[__row]_").append(i).append("\">\n");
                    }
                    if (colid3.equalsIgnoreCase("filename")) {
                        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
                            type.getFilenameFieldTemplateRow(this, content, col3, tp, this.browser);
                        }
                    } else {
                        if (colid3.equalsIgnoreCase("typeflag")) {
                            col3.setProperty("displayvalue", Attachment.getSelectedDisplayValue(allowableTypes));
                        }
                        MaintColumn mc = new MaintColumn(this.pageContext, this.sdiInfo, this.getConnectionId());
                        if (!colid3.equalsIgnoreCase("typeflag") && !colid3.equalsIgnoreCase("attachmentdesc") && !colid3.equalsIgnoreCase("attachmentnum") && col3.getProperty(PROPERTY_MANDATORY, "N").equalsIgnoreCase("Y")) {
                            col3.setProperty("validation", "Mandatory");
                        }
                        mc.setColumn(col3);
                        mc.setDatasetname("attachment");
                        String inputhtml = mc.getHtml();
                        inputhtml = inputhtml.indexOf("style=\"") > -1 ? StringUtil.replaceAll(inputhtml, "style=\"", "style=\"width:" + this.getWidth(col3, 0) + "px;") : StringUtil.replaceAll(inputhtml, " id=\"", " style=\"width:" + this.getWidth(col3, 0) + "px;\" id=\"");
                        if (colid3.equalsIgnoreCase("typeflag")) {
                            inputhtml = StringUtil.replaceAll(inputhtml, "<option value=\"\"></option>", "", true);
                        } else if (colid3.equalsIgnoreCase("attachmentnum")) {
                            inputhtml = StringUtil.replaceAll(inputhtml, "value=\"\"", "value=\"(auto)\"", false);
                            inputhtml = inputhtml + "<img id=\"at[__row]_confirm\" src=\"WEB-CORE/images/gif/Confirm.gif\" style=\"position:relative;" + (rtl ? "right:40px;" : "left:40px;") + "top:-19px;display:none;\">";
                        }
                        String name = col3.getProperty(PROPERTY_FIELDNAME, "");
                        if (name.length() > 0) {
                            String id = mc.getId();
                            inputhtml = StringUtil.replaceAll(inputhtml, "name=\"" + id + "\"", "name=\"" + name + "\"");
                        }
                        content.append(inputhtml);
                    }
                    if (col3.getProperty(PROPERTY_MODE, "").equals("hidden")) continue;
                    content.append("</td>\n");
                    continue;
                }
                this.logger.warn("Column " + colid3 + " not found in data.");
                continue;
            }
            this.logger.warn("Empty column found.");
        }
        content.append("<td class=\"gridmaint_field\" id=\"tablecell_[__row]_").append(columns.size() + 1).append("\" style=\"border-top:none;").append(rtl ? "border-right:none;" : "border-left:none;").append("width:").append(awidth).append("px;\">\n");
        this.renderActionColumn(content, -1, "", "[__row]", allowEdit, true, viewOnly || isLocked, tp);
        content.append("</td>");
        content.append("<td class=\"\" id=\"tablecell_[__row]_").append(columns.size() + 2).append("\" style=\"border:none;width:").append(uwidth).append("px;\">\n");
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_uploaddiv\" style=\"display:none;\">");
        content.append("&nbsp;<img src=\"WEB-CORE/images/gif/UploadRight.gif\" id=\"").append(ATTACHMENT_CODE).append("[__row]_uploadimg\" title=\"").append(tp.translate("Upload to...")).append("\">&nbsp;");
        content.append("<select class=\"attman_medfield\" id=\"").append(ATTACHMENT_CODE).append("[__row]_uploaddir\" name=\"file1_uploaddir\" onchange=\"").append(MAIN_JS_OBJECT).append(".sdiSetRowUpdate(event);\" >");
        if (uploadData != null && uploadData.size() > 0) {
            content.append("<option value=\"\" title=\"\" selected></option>");
            for (i = 0; i < uploadData.size(); ++i) {
                PropertyList uploadItem = uploadData.getPropertyList(i);
                String location = FileSystem.getFileLocation(uploadItem.getProperty("location", ""));
                String title = uploadItem.getProperty(PROPERTY_TITLE, location);
                content.append("<option value=\"").append(location).append("\" title=\"");
                content.append(uploadItem.getProperty("description", title)).append(" - ").append(location);
                content.append("\">").append(title).append("</option>");
            }
        } else {
            content.append("<option value=\"\" title=\"No Upload Locations Defined\"></option>");
        }
        content.append("</select>");
        content.append("</div>");
        content.append("</td>");
        content.append("</tr>\n");
        content.append("</table>\n");
        content.append("</form>\n");
        content.append("</td></tr>\n");
        content.append("</table>\n");
    }

    private boolean isAttachmentClassEditable(String attachmentClassString) {
        PropertyListCollection classes = this.attachmentPolicy.getCollection("classes");
        PropertyList attachmentClass = null;
        if (classes != null) {
            attachmentClass = classes.find("class", attachmentClassString);
        }
        return attachmentClass == null || !attachmentClass.getProperty(PROPERTY_CONTENTEDITABLE, "Y").equals("N");
    }

    private PropertyListCollection getColumns(boolean viewOnly, StringBuffer mandatory) {
        PropertyListCollection columns = this.element.containsKey("pagedata") ? this.element.getPropertyList("pagedata").getCollection(PROPERTY_COLUMNS) : this.element.getCollection(PROPERTY_COLUMNS);
        if (columns == null) {
            columns = new PropertyListCollection();
            this.element.setProperty(PROPERTY_COLUMNS, columns);
        }
        if (columns != null) {
            boolean attachmentNumFound = false;
            boolean typeFlagFound = false;
            boolean fileFound = false;
            boolean descriptionFound = false;
            boolean classFound = false;
            for (int i = 0; i < columns.size(); ++i) {
                String mode;
                PropertyList col = columns.getPropertyList(i);
                String colid = col.getProperty(PROPERTY_COLUMNID);
                if (colid.equalsIgnoreCase("attachmentnum")) {
                    attachmentNumFound = true;
                    col.setProperty(PROPERTY_FIELDNAME, "file1_attachmentnum");
                    col.setProperty(PROPERTY_MANDATORY, "Y");
                } else if (colid.equalsIgnoreCase("typeflag")) {
                    typeFlagFound = true;
                    col.setProperty(PROPERTY_FIELDNAME, "file1_typeflag");
                    col.setProperty(PROPERTY_MANDATORY, "Y");
                } else if (colid.equalsIgnoreCase("attachmentclass")) {
                    classFound = true;
                    mode = col.getProperty(PROPERTY_MODE, "input");
                    if (!(!viewOnly || mode.equalsIgnoreCase("hidden") && mode.equalsIgnoreCase("readonly") && mode.equalsIgnoreCase("retrievedata"))) {
                        col.setProperty(PROPERTY_MODE, "readonly");
                    }
                } else if (colid.equalsIgnoreCase("filename")) {
                    fileFound = true;
                    col.setProperty(PROPERTY_FIELDNAME, "file1");
                    col.setProperty(PROPERTY_MANDATORY, "Y");
                } else {
                    if (colid.equalsIgnoreCase("attachmentdesc")) {
                        descriptionFound = true;
                        col.setProperty(PROPERTY_FIELDNAME, "file1_desc");
                        col.setProperty(PROPERTY_MANDATORY, "Y");
                    }
                    mode = col.getProperty(PROPERTY_MODE, "input");
                    if (!(!viewOnly || mode.equalsIgnoreCase("hidden") && mode.equalsIgnoreCase("readonly") && mode.equalsIgnoreCase("retrievedata"))) {
                        col.setProperty(PROPERTY_MODE, "readonly");
                    }
                }
                if (mandatory == null || !col.getProperty(PROPERTY_MANDATORY, "N").equalsIgnoreCase("Y")) continue;
                if (mandatory.length() == 0) {
                    mandatory.append(colid);
                    continue;
                }
                mandatory.append(";").append(colid);
            }
            if (!attachmentNumFound) {
                this.logger.debug("Could not find attachmentnum column in defined columns thus creating.");
                PropertyList pl = new PropertyList();
                pl.setProperty(PROPERTY_COLUMNID, "attachmentnum");
                pl.setProperty(PROPERTY_FIELDNAME, "file1_attachmentnum");
                pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("Number"));
                pl.setProperty(PROPERTY_MODE, "readonly");
                pl.setProperty("class", "attman_shortfield");
                pl.setProperty(PROPERTY_WIDTH, "40");
                pl.setProperty(PROPERTY_MANDATORY, "Y");
                columns.add(pl);
                if (mandatory != null) {
                    if (mandatory.length() == 0) {
                        mandatory.append("attachmentnum");
                    } else {
                        mandatory.append(";attachmentnum");
                    }
                }
            }
            if (!descriptionFound) {
                this.logger.debug("Could not find attachmentdesc column in defined columns thus creating.");
                PropertyList pl = new PropertyList();
                pl.setProperty(PROPERTY_COLUMNID, "attachmentdesc");
                pl.setProperty(PROPERTY_FIELDNAME, "file1_desc");
                pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("Description"));
                if (viewOnly) {
                    pl.setProperty(PROPERTY_MODE, "readonly");
                } else {
                    pl.setProperty(PROPERTY_MODE, "input");
                }
                pl.setProperty(PROPERTY_WIDTH, "200");
                pl.setProperty(PROPERTY_MANDATORY, "Y");
                columns.add(pl);
                if (mandatory != null) {
                    if (mandatory.length() == 0) {
                        mandatory.append("attachmentdesc");
                    } else {
                        mandatory.append(";attachmentdesc");
                    }
                }
            }
            if (!classFound) {
                this.logger.debug("Could not find attachmentclass column in defined columns thus creating.");
                PropertyList pl = new PropertyList();
                pl.setProperty(PROPERTY_COLUMNID, "attachmentclass");
                pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("Class"));
                if (viewOnly) {
                    pl.setProperty(PROPERTY_MODE, "readonly");
                } else {
                    pl.setProperty(PROPERTY_MODE, "dropdownlist");
                }
                pl.setProperty("reftypeid", "AttachmentClass");
                columns.add(pl);
            }
            if (!typeFlagFound) {
                this.logger.debug("Could not find typeflag column in defined columns thus creating.");
                PropertyList pl = new PropertyList();
                pl.setProperty(PROPERTY_COLUMNID, "typeflag");
                pl.setProperty(PROPERTY_FIELDNAME, "file1_typeflag");
                pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("Type"));
                pl.setProperty(PROPERTY_MODE, "dropdownlist");
                pl.setProperty("displayvalue", Attachment.getDisplayValue());
                PropertyList event = new PropertyList();
                event.setProperty("event", "onchange");
                event.setProperty(PROPERTY_JS, "attachmentManager.doTypeChange()");
                PropertyListCollection events = new PropertyListCollection();
                events.add(event);
                pl.setProperty("events", events);
                pl.setProperty(PROPERTY_WIDTH, "120");
                pl.setProperty(PROPERTY_MANDATORY, "Y");
                columns.add(pl);
                if (mandatory != null) {
                    if (mandatory.length() == 0) {
                        mandatory.append("typeflag");
                    } else {
                        mandatory.append(";typeflag");
                    }
                }
            }
            if (!fileFound) {
                this.logger.debug("Could not find filename column in defined columns thus creating.");
                if (viewOnly) {
                    PropertyList pl = new PropertyList();
                    pl.setProperty(PROPERTY_COLUMNID, "filename");
                    pl.setProperty(PROPERTY_FIELDNAME, "file1");
                    pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("File / URL"));
                    pl.setProperty(PROPERTY_MODE, "hidden");
                    pl.setProperty(PROPERTY_WIDTH, "400");
                    columns.add(pl);
                } else {
                    PropertyList pl = new PropertyList();
                    pl.setProperty(PROPERTY_COLUMNID, "filename");
                    pl.setProperty(PROPERTY_FIELDNAME, "file1");
                    pl.setProperty(PROPERTY_TITLE, this.getTranslationProcessor().translate("File / URL"));
                    pl.setProperty(PROPERTY_MODE, "");
                    pl.setProperty(PROPERTY_WIDTH, "400");
                    pl.setProperty(PROPERTY_MANDATORY, "Y");
                    columns.add(pl);
                    if (mandatory != null) {
                        if (mandatory.length() == 0) {
                            mandatory.append("filename");
                        } else {
                            mandatory.append(";filename");
                        }
                    }
                }
            }
            return columns;
        }
        return columns;
    }

    private void renderButtons(StringBuffer content, PropertyListCollection buttons, TranslationProcessor tp) {
        for (int i = 0; i < buttons.size(); ++i) {
            PropertyList thisbtn = buttons.getPropertyList(i);
            Button btn = new Button(this.pageContext);
            btn.setId(thisbtn.getProperty(PROPERTY_ID, ""));
            btn.setImg(thisbtn.getProperty(PROPERTY_IMG, ""));
            btn.setAppearance("Standard");
            btn.setText(tp.translate(thisbtn.getProperty(PROPERTY_TEXT, "")));
            btn.setTip(tp.translate(thisbtn.getProperty(PROPERTY_TIP, "")));
            btn.setAction(thisbtn.getProperty(PROPERTY_JS, ""));
            content.append(btn.getHtml());
            content.append("&nbsp;");
        }
    }

    private void renderThumbnailView(StringBuffer html, PropertyListCollection columns, DataSet data) {
        String allowableTypes = this.getAllowableTypes();
        if (data.getRowCount() > 0) {
            String typeflag;
            int row;
            for (row = 0; row < data.getRowCount(); ++row) {
                typeflag = Attachment.correctTypeFlag(data.getValue(row, "typeflag", ""));
                if (this.isAllowed(typeflag, allowableTypes)) continue;
                data.deleteRow(row);
                --row;
            }
            for (row = 0; row < data.getRowCount(); ++row) {
                Image image;
                typeflag = Attachment.correctTypeFlag(data.getValue(row, "typeflag", "S"));
                BaseAttachmentType type = Attachment.getAttachmentType(typeflag);
                String filename = data.getValue(row, "filename", "");
                String lfi = filename.toLowerCase();
                String desc = data.getValue(row, "attachmentdesc", filename);
                int attachmentnum = 0;
                try {
                    attachmentnum = Integer.parseInt(data.getValue(row, "attachmentnum", "0"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                String curretentKeyid1 = data.getValue(row, PROPERTY_KEYID1, this.keyid1);
                String curretentKeyid2 = data.getValue(row, PROPERTY_KEYID2, this.keyid2);
                String curretentKeyid3 = data.getValue(row, PROPERTY_KEYID3, this.keyid3);
                html.append("<div attachmentnum=\"").append(attachmentnum).append("\" keyid1=\"").append(curretentKeyid1).append("\" keyid2=\"").append(curretentKeyid2).append("\" keyid3=\"").append(curretentKeyid3).append("\" attachmentdesc=\"").append(desc).append("\" typeflag=\"").append(typeflag).append("\" class=\"icon\" onmouseout=\"attachmentManager.iconMouseOut(this,event);\" onclick=\"attachmentManager.iconClick(this,event);\" onmouseover=\"attachmentManager.iconMouseOver(this,event);\">");
                FileType fileType = typeflag.equalsIgnoreCase("P") ? FileType.getFileTypeByName("TXT", this.getConnectionId()) : (typeflag.equalsIgnoreCase("L") ? FileType.getFileTypeByName("URL", this.getConnectionId()) : (typeflag.equalsIgnoreCase("D") ? FileType.getFileTypeByName("MDB", this.getConnectionId()) : (typeflag.equalsIgnoreCase("M") ? FileType.getFileTypeByName("RTF", this.getConnectionId()) : FileType.getFileType(filename, this.getConnectionId()))));
                if (fileType.getType() == FileType.NamedType.IMAGE) {
                    html.append("<div class=\"icon_prev\">");
                    image = new Image(this.pageContext);
                    image.setAttachment(this.sdcid, curretentKeyid1, curretentKeyid2, curretentKeyid3, attachmentnum);
                    image.setWidth(100);
                    image.setTitle(desc + " (" + attachmentnum + ")");
                    html.append(image.getHtml());
                    html.append("</div>");
                } else {
                    html.append("<div class=\"icon_img\">");
                    image = new Image(this.pageContext);
                    if (fileType.getImageRefId().length() > 0) {
                        image.setImageId(fileType.getImageRefId());
                        image.setDefault(fileType.getImage());
                    } else {
                        image.setImageSrc(fileType.getImage());
                    }
                    image.setWidth(42);
                    image.setHeight(42);
                    image.setTitle(desc + " (" + attachmentnum + ")");
                    html.append(image.getHtml());
                    html.append("</div>");
                }
                html.append("<div class=\"icon_txt\">");
                html.append("").append(desc);
                html.append("</div>");
                html.append("</div>");
            }
        } else {
            html.append(this.getTranslationProcessor().translate("No attachments available."));
        }
    }

    private void renderScriptAndStyle(StringBuffer html, boolean grey) {
        if (!this.viewonly) {
            html.append("\n<script type=\"text/javascript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
        }
        html.append("\n<script type=\"text/javascript\" src=\"WEB-CORE/elements/scripts/attachmentmanager.js\"></script>\n");
        html.append("\n<script type=\"text/javascript\" src=\"WEB-CORE/elements/scripts/attachments.js\"></script>\n");
        html.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/pagetypes/attachment/style/attachmentmanager.css", this.pageContext) + "\">\n");
        html.append("\n<style>\n");
        if (this.renderToolbar && this.showTab && this.showprimary) {
            html.append("body{overflow:hidden !important;}");
        }
        html.append("#pagebody{overflow-x:auto !important;overflow-y:auto !important;}");
        if (grey) {
            html.append(".btn_disabled{cursor: pointer;opacity:0.5}\n");
        } else {
            html.append(".btn_disabled{visibility:hidden;}\n");
        }
        html.append("#tab_primary_tab__expanded{overflow:visible !important;}");
        html.append("\n</style>\n");
    }

    private void renderAdvancedScript(StringBuffer html, String sdcid, String keyid1, String keyid2, String keyid3, boolean viewOnly, boolean allowEdit, String allowOverwrite, String onclose) {
        html.append("\n<script type=\"text/javascript\">\n");
        html.append("function hideAllDivs( oDoc, iRow ) {\n");
        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
            html.append(type.getHideContentDivJavascript());
        }
        html.append("}\n");
        html.append("function showDiv( typeflag, oDoc, iRow ) {\n");
        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
            html.append(type.getShowContentDivJavascript());
        }
        html.append("}\n");
        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
            html.append(type.getViewJavaScript());
        }
        for (BaseAttachmentType type : Attachment.getAllAttachmentTypesList()) {
            html.append(type.getEditJavaScript());
        }
        html.append(BaseAttachmentType.getShowHistoryScript());
        try {
            html.append(MAIN_JS_OBJECT).append(".").append("serverFolder = '").append(StringUtil.replaceAll(Configuration.getInstance().getSapphireHome(), "\\", "/")).append("';\n");
        }
        catch (Exception e) {
            this.logger.warn("Encountered error trying to obtain sapphire home directory.");
        }
        html.append(MAIN_JS_OBJECT).append(".attachmentObject = ").append(ATT_JS_OBJECT).append(";\n");
        html.append(MAIN_JS_OBJECT).append(".viewOnly = ").append(viewOnly).append(";\n");
        html.append(MAIN_JS_OBJECT).append(".allowEdit = ").append(allowEdit).append(";\n");
        html.append(MAIN_JS_OBJECT).append(".allowOverwrite = '").append(allowOverwrite).append("';\n");
        html.append(MAIN_JS_OBJECT).append(".onclose = '").append(onclose).append("';\n");
        html.append(MAIN_JS_OBJECT).append(".keyid1 = '").append(keyid1).append("';\n");
        html.append(MAIN_JS_OBJECT).append(".keyid2 = '").append(keyid2).append("';\n");
        html.append(MAIN_JS_OBJECT).append(".keyid3 = '").append(keyid3).append("';\n");
        html.append(MAIN_JS_OBJECT).append(".sdcid = '").append(sdcid).append("';\n");
        String webApp = ((HttpServletRequest)this.pageContext.getRequest()).getContextPath();
        if (webApp.startsWith("/")) {
            webApp = webApp.substring(1);
        }
        html.append("sapphire.events.attachEvent( window, 'onload', new Function( 'attachments.doInit( \"").append(this.pageContext.getRequest().getScheme()).append("\", \"").append(this.pageContext.getRequest().getServerName());
        html.append("\", \"").append(this.pageContext.getRequest().getServerPort()).append("\", \"").append(webApp).append("\", \"").append(this.getConnectionId());
        html.append("\", \"").append(sdcid).append("\", \"").append(keyid1).append("\", \"").append(keyid2);
        html.append("\", \"").append(keyid3).append("\", false, false );' ) );\n");
        html.append("window.onbeforeunload = attachmentManager.doOnUnload;\n");
        html.append("sapphire.events.attachEvent( window, 'onunload', attachments.doFinish );\n");
        html.append("document.body.style.overflow = 'scroll';\n");
        html.append("\n</script>\n");
    }

    private void renderRsetScript(StringBuffer html, String rsetId, boolean isLocked, PropertyListCollection uploadData, StringBuffer mandatory) {
        html.append("\n<script type=\"text/javascript\">\n");
        html.append(MAIN_JS_OBJECT).append(".locked = ").append(isLocked).append(";\n");
        html.append(MAIN_JS_OBJECT).append(".rsetId = '").append(rsetId).append("';\n");
        html.append("__rsetlist = '").append(rsetId).append("';\n");
        html.append("  sapphire.connection.pingRset('").append(rsetId).append("');\n");
        if (uploadData != null && uploadData.size() > 0) {
            this.logger.debug("Upload Data --- uploadData.size = " + uploadData.size());
            html.append(MAIN_JS_OBJECT).append(".uploadAvailable = ").append(true).append(";\n");
        } else {
            this.logger.debug("No Upload Data.");
            html.append(MAIN_JS_OBJECT).append(".uploadAvailable = ").append(false).append(";\n");
        }
        if (mandatory != null && mandatory.length() > 0) {
            html.append(MAIN_JS_OBJECT).append(".mandatoryFields = ('").append(mandatory).append("').split(';');\n");
        } else {
            html.append(MAIN_JS_OBJECT).append(".mandatoryFields = null;\n");
        }
        html.append("\n</script>\n");
    }

    private void renderToolbar(StringBuffer html) {
        boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
        PropertyList toolbar = this.element.getPropertyList("advancedtoolbar");
        if (toolbar != null) {
            boolean isRib = AdvancedToolbar.isRibbon(toolbar);
            html.append("<tr class=\"pagebuttonsection\" height=\"22\">");
            html.append("<td colspan=\"2\">");
            html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\">");
            html.append("<tr>");
            html.append("<td colspan=\"2\" align=\"").append(rtl ? "right" : "left").append("\" valign=\"top\">");
            if (isRib) {
                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
            } else {
                html.append("<table border=\"0\" width=\"100%\" height=\"100%\">");
            }
            html.append("<tr>");
            if (!isRib) {
                html.append("<td valign=top width=\"10%\" class=layout_pagetitle id=\"pagetitle\">");
                html.append(this.getTranslationProcessor().translate(this.title));
                html.append("</td>");
            } else {
                toolbar.setProperty("pagetitle", this.getTranslationProcessor().translate(this.title));
            }
            html.append("<td valign=top align=\"").append(rtl ? "right" : "left").append("\">");
            html.append("<table border=0 cellspacing=0 cellpadding=0 ").append(isRib ? "width=\"100%\"" : "").append(">");
            html.append("<tr>");
            html.append("<td align=\"").append(rtl ? "right" : "left").append("\">");
            toolbar.setProperty("rendermode", "Button");
            AdvancedToolbar tb = new AdvancedToolbar();
            tb.setElementid("advancedtoolbar");
            tb.setElementProperties(toolbar);
            tb.setPageContext(this.pageContext);
            html.append(tb.getHtml());
            html.append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</table>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</table>");
            html.append("</td>");
            html.append("</tr>");
            html.append(" </table> ");
            html.append("</td>");
            html.append("</tr>");
            html.append(this.getEsigFramework());
        }
    }

    private StringBuffer getEsigFramework() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<form name=\"esigform\" id=\"esigform\" method=\"post\" style=\"display:none;\">\n\t<input type=\"hidden\" name=\"sdcid\" clearable=\"yes\" value=\"" + this.sdcid + "\"/>\n\t<input type=\"hidden\" name=\"keyid1\" clearable=\"yes\" value=\"" + this.keyid1 + "\"/>\n\t<input name=\"esigpage\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"auditreasonreftype\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"reasonpromptoption\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"message\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"operation\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"callback\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"setesigreasoncallback\" type=\"hidden\" clearable=\"yes\"/>\n\t<input name=\"keysfromlookup\" type=\"hidden\" clearable=\"yes\" value=\"\"/>\n</form>");
        buffer.append("\n\n");
        buffer.append("<script>function setESigReason( reason, operation, signedflag ) {\n    var aoAuditReason = document.getElementsByName( \"auditreason\" );\n    var aoAuditActivity = document.getElementsByName( \"auditactivity\" );\n    var aoAuditSignedFlag = document.getElementsByName( \"auditsignedflag\" );\n\n    for ( var i = 0; i < aoAuditReason.length; i++ ) {\n        aoAuditReason[i].value = reason;\n    }\n\n    for ( var i = 0; i < aoAuditActivity.length; i++ ) {\n        aoAuditActivity[i].value = operation;\n    }\n\n    for ( var i = 0; i < aoAuditSignedFlag.length; i++ ) {\n        aoAuditSignedFlag[i].value = signedflag;\n    }\n}</script>");
        return buffer;
    }

    private String parseTabText(String orgtabtext, String sdcid, String keyid1, String keyid2, String keyid3, DataSet primarydata, int rowcount) {
        String out = orgtabtext;
        String[] tokens = StringUtil.getExpressionTokens(out);
        for (int i = 0; i < tokens.length; ++i) {
            String token = tokens[i];
            if (token.equalsIgnoreCase(PROPERTY_SDCID)) {
                out = StringUtil.replaceAll(out, "[" + token + "]", sdcid, false);
                continue;
            }
            if (token.equalsIgnoreCase(PROPERTY_KEYID1) || token.equalsIgnoreCase("keycolid1")) {
                out = StringUtil.replaceAll(out, "[" + token + "]", StringUtil.replaceAll(keyid1, ";", " "), false);
                continue;
            }
            if (token.equalsIgnoreCase(PROPERTY_KEYID2) || token.equalsIgnoreCase("keycolid2")) {
                out = StringUtil.replaceAll(out, "[" + token + "]", StringUtil.replaceAll(keyid2, ";", " "), false);
                continue;
            }
            if (token.equalsIgnoreCase(PROPERTY_KEYID3) || token.equalsIgnoreCase("keycolid2")) {
                out = StringUtil.replaceAll(out, "[" + token + "]", StringUtil.replaceAll(keyid3, ";", " "), false);
                continue;
            }
            if (token.equalsIgnoreCase("desccol") || token.equalsIgnoreCase("description")) {
                SDCProcessor sdc = new SDCProcessor(this.pageContext);
                HashMap sdcprops = sdc.getSDCProperties(sdcid);
                if (sdcprops == null) continue;
                String desccol = sdcprops.get("desccol").toString();
                String value = primarydata.getValue(0, desccol, "");
                out = StringUtil.replaceAll(out, "[" + token + "]", value, false);
                continue;
            }
            if (token.equalsIgnoreCase("rowcount")) {
                out = StringUtil.replaceAll(out, "[" + token + "]", "" + rowcount, false);
                continue;
            }
            String value = primarydata.getValue(0, token, "");
            out = StringUtil.replaceAll(out, "[" + token + "]", value, false);
        }
        return out;
    }

    private StringBuffer getPrimaryRowHTMLSingle(String title, String columnid, SDITagInfo pri, boolean isLocked, String lockedbytext) {
        StringBuffer content = new StringBuffer();
        content.append("<tr height=\"8\">");
        content.append("<td class=\"maintform_fieldtitle\">");
        content.append(title.length() > 0 ? title : columnid);
        content.append("</td>");
        content.append("<td colspan=\"1\" ");
        if (isLocked) {
            content.append(" class=\"maint_lockedfield\" ");
        } else {
            content.append(" class=\"maintform_field\" ");
        }
        content.append(">\n");
        MaintColumn mc = new MaintColumn(this.pageContext, pri, this.getConnectionId());
        PropertyList column1 = new PropertyList();
        column1.setProperty(PROPERTY_COLUMNID, columnid);
        column1.setProperty(PROPERTY_MODE, "readonly");
        mc.setColumn(column1);
        mc.setDatasetname("primary");
        content.append(mc.getHtml());
        if (isLocked && lockedbytext.length() > 0) {
            content.append("<img src=\"WEB-CORE/elements/images/locked.gif\" title=\"").append(lockedbytext).append("\">");
        }
        content.append("</td>");
        content.append("</tr>");
        return content;
    }

    private StringBuffer getPrimaryRowHTMLSingle(String title, String columnid, SDITagInfo pri, boolean isLocked) {
        return this.getPrimaryRowHTMLSingle(title, columnid, pri, isLocked, "");
    }

    private StringBuffer getPrimaryRowHTMLDual(String title1, String columnid1, String title2, String columnid2, SDITagInfo pri, boolean isLocked, String lockedbytext) {
        StringBuffer content = new StringBuffer();
        content.append("<tr height=\"8\">");
        content.append("<td class=\"maintform_fieldtitle\">");
        content.append(title1.length() > 0 ? title1 : columnid1);
        content.append("</td>");
        content.append("<td colspan=\"").append(columnid2.length() > 0 ? "1" : "3").append("\" ");
        if (isLocked) {
            content.append(" class=\"maint_lockedfield\" ");
        } else {
            content.append(" class=\"maintform_field\" ");
        }
        content.append(">\n");
        MaintColumn mc = new MaintColumn(this.pageContext, pri, this.getConnectionId());
        PropertyList column1 = new PropertyList();
        column1.setProperty(PROPERTY_COLUMNID, columnid1);
        column1.setProperty(PROPERTY_MODE, "readonly");
        mc.setColumn(column1);
        mc.setDatasetname("primary");
        content.append(mc.getHtml());
        if (isLocked && lockedbytext.length() > 0) {
            content.append("<img src=\"WEB-CORE/elements/images/locked.gif\" title=\"").append(lockedbytext).append("\">");
        }
        content.append("</td>");
        if (columnid2.length() > 0) {
            content.append("<td class=\"maintform_fieldtitle\">");
            content.append(title2.length() > 0 ? title2 : columnid2);
            content.append("</td>");
            content.append("<td colspan=\"1\" ");
            if (isLocked) {
                content.append(" class=\"maint_lockedfield\" ");
            } else {
                content.append(" class=\"maintform_field\" ");
            }
            content.append(">\n");
            mc = new MaintColumn(this.pageContext, pri, this.getConnectionId());
            column1 = new PropertyList();
            column1.setProperty(PROPERTY_COLUMNID, columnid2);
            column1.setProperty(PROPERTY_MODE, "readonly");
            mc.setColumn(column1);
            mc.setDatasetname("primary");
            content.append(mc.getHtml());
            content.append("</td>");
        }
        content.append("</tr>");
        return content;
    }

    private StringBuffer getPrimaryRowHTMLDual(String title1, String columnid1, String title2, String columnid2, SDITagInfo pri, boolean isLocked) {
        return this.getPrimaryRowHTMLDual(title1, columnid1, title2, columnid2, pri, isLocked, "");
    }

    private String renderPrimaryContent(String sdcid, DataSet primary, boolean isLocked, String lockedby, int mode, TranslationProcessor tp) {
        return this.renderPrimaryContent(sdcid, primary, 0, isLocked, lockedby, mode, tp);
    }

    private String renderPrimaryContent(String sdcid, DataSet primary, int row, boolean isLocked, String lockedby, int mode, TranslationProcessor tp) {
        PropertyListCollection priColumns;
        StringBuffer content = new StringBuffer();
        SDCProcessor sdc = this.getSDCProcessor();
        String keycol = sdc.getProperty(sdcid, "keycolid1");
        PropertyListCollection columns = sdc.getColumns(sdcid);
        PropertyListCollection propertyListCollection = priColumns = this.element.getPropertyList("pagedata") != null ? this.element.getPropertyList("pagedata").getCollection("primarycolumns") : this.element.getCollection("primarycolumns");
        if (priColumns == null) {
            priColumns = new PropertyListCollection();
        }
        if (priColumns.size() == 0) {
            PropertyList priCol;
            if (mode == 2 || mode == 1) {
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_SDCID);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_SDCID);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("SDC Id"));
                priColumns.add(priCol);
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_KEYID1);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_KEYID1);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("KeyId1"));
                priColumns.add(priCol);
                if (!primary.getValue(row, PROPERTY_KEYID2, "(null)").equalsIgnoreCase("(null)")) {
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, PROPERTY_KEYID2);
                    priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_KEYID2);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("KeyId2"));
                    priColumns.add(priCol);
                }
                if (!primary.getValue(row, PROPERTY_KEYID3, "(null)").equalsIgnoreCase("(null)")) {
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, PROPERTY_KEYID3);
                    priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_KEYID3);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("KeyId3"));
                    priColumns.add(priCol);
                }
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_PARAMLISTID);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_PARAMLISTID);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("Parameter List Id"));
                priColumns.add(priCol);
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_PARAMLISTVERSIONID);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_PARAMLISTVERSIONID);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("Version Id"));
                priColumns.add(priCol);
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_VARIANTID);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_VARIANTID);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("Variant Id"));
                priColumns.add(priCol);
                priCol = new PropertyList();
                priCol.setProperty(PROPERTY_ID, PROPERTY_DATASET);
                priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_DATASET);
                priCol.setProperty(PROPERTY_TITLE, tp.translate("Dataset Number"));
                priColumns.add(priCol);
                if (mode == 2) {
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, PROPERTY_PARAMID);
                    priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_PARAMID);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("Parameter Id"));
                    priColumns.add(priCol);
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, PROPERTY_PARAMTYPE);
                    priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_PARAMTYPE);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("Parameter Type"));
                    priColumns.add(priCol);
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, PROPERTY_REPLICATEID);
                    priCol.setProperty(PROPERTY_COLUMNID, PROPERTY_REPLICATEID);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("Replicate"));
                    priColumns.add(priCol);
                }
            } else {
                PropertyList col;
                priCol = new PropertyList();
                String sdccolumn = keycol;
                priCol.setProperty(PROPERTY_ID, sdccolumn);
                priCol.setProperty(PROPERTY_COLUMNID, sdccolumn);
                try {
                    col = columns.getPropertyList(sdccolumn);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate(col.getProperty("columnlabel", sdccolumn.substring(0, 1).toUpperCase() + sdccolumn.substring(1))));
                }
                catch (Exception e) {
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("ID"));
                }
                priColumns.add(priCol);
                priCol = new PropertyList();
                sdccolumn = sdc.getProperty(sdcid, "desccol");
                priCol.setProperty(PROPERTY_ID, sdccolumn);
                priCol.setProperty(PROPERTY_COLUMNID, sdccolumn);
                try {
                    col = columns.getPropertyList(sdccolumn);
                    priCol.setProperty(PROPERTY_TITLE, tp.translate(col.getProperty("columnlabel", sdccolumn.substring(0, 1).toUpperCase() + sdccolumn.substring(1))));
                }
                catch (Exception e) {
                    priCol.setProperty(PROPERTY_TITLE, tp.translate("Description"));
                }
                priColumns.add(priCol);
                sdccolumn = sdc.getProperty(sdcid, "keycolid2", "");
                if (sdccolumn.length() > 0) {
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, sdccolumn);
                    priCol.setProperty(PROPERTY_COLUMNID, sdccolumn);
                    try {
                        col = columns.getPropertyList(sdccolumn);
                        priCol.setProperty(PROPERTY_TITLE, tp.translate(col.getProperty("columnlabel", sdccolumn.substring(0, 1).toUpperCase() + sdccolumn.substring(1))));
                    }
                    catch (Exception e) {
                        priCol.setProperty(PROPERTY_TITLE, tp.translate("ID 2"));
                    }
                    priColumns.add(priCol);
                }
                if ((sdccolumn = sdc.getProperty(sdcid, "keycolid3", "")).length() > 0) {
                    priCol = new PropertyList();
                    priCol.setProperty(PROPERTY_ID, sdccolumn);
                    priCol.setProperty(PROPERTY_COLUMNID, sdccolumn);
                    try {
                        col = columns.getPropertyList(sdccolumn);
                        priCol.setProperty(PROPERTY_TITLE, tp.translate(col.getProperty("columnlabel", sdccolumn.substring(0, 1).toUpperCase() + sdccolumn.substring(1))));
                    }
                    catch (Exception e) {
                        priCol.setProperty(PROPERTY_TITLE, tp.translate("ID 3"));
                    }
                    priColumns.add(priCol);
                }
            }
        }
        SDITagInfo pri = this.createSDIInfo(primary, "primary");
        pri.getQueryData("primary").setCurrentRow(row);
        content.append("<table id=\"mainttable_").append(sdcid).append("\" class=\"maintform_table\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" style=\"display:block\">");
        boolean dual = false;
        for (int c = 0; c < priColumns.size(); ++c) {
            PropertyList priColumn = priColumns.getPropertyList(c);
            PropertyList nextPriColumn = c + 1 < priColumns.size() ? priColumns.getPropertyList(c + 1) : null;
            boolean isKey = priColumn.getProperty(PROPERTY_COLUMNID).equalsIgnoreCase(keycol);
            if (priColumns.size() > 2 && nextPriColumn != null) {
                content.append(this.getPrimaryRowHTMLDual(priColumn.getProperty(PROPERTY_TITLE), priColumn.getProperty(PROPERTY_COLUMNID), nextPriColumn.getProperty(PROPERTY_TITLE), nextPriColumn.getProperty(PROPERTY_COLUMNID), pri, isLocked, isKey ? tp.translate("Locked by") + lockedby : ""));
                dual = true;
                ++c;
                continue;
            }
            if (dual) {
                content.append(this.getPrimaryRowHTMLDual(priColumn.getProperty(PROPERTY_TITLE), priColumn.getProperty(PROPERTY_COLUMNID), "", "", pri, isLocked, isKey ? tp.translate("Locked by") + lockedby : ""));
                continue;
            }
            content.append(this.getPrimaryRowHTMLSingle(priColumn.getProperty(PROPERTY_TITLE), priColumn.getProperty(PROPERTY_COLUMNID), pri, isLocked, isKey ? tp.translate("Locked by") + lockedby : ""));
        }
        content.append("</table>");
        return content.toString();
    }

    private String getLockedBy(DataSet dataset) {
        if (dataset != null && dataset.size() > 0) {
            return dataset.getValue(0, "__lockedby", "");
        }
        return "";
    }

    private boolean generateKey(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid) {
        PropertyList sdc = null;
        if (this.mode == 1) {
            sdc = this.getSDCProcessor().getPropertyList("DataSet");
        } else if (this.mode == 2) {
            sdc = this.getSDCProcessor().getPropertyList("DataItem");
        }
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("UPDATE ").append(sdc.getProperty("tableid", "")).append(" ");
        sql.append("SET ").append(sdc.getProperty("keycolid1", "")).append(" = ").append(sdc.getProperty("keycolid1", "")).append(" WHERE ");
        sql.append("keyid1 = ").append(safeSQL.addVar(keyid1)).append(" AND ");
        if (keyid2.length() > 0) {
            sql.append("keyid2 = ").append(safeSQL.addVar(keyid2)).append(" AND ");
            if (keyid3.length() > 0) {
                sql.append("keyid3 = ").append(safeSQL.addVar(keyid3)).append(" AND ");
            }
        }
        sql.append("sdcid = ").append(safeSQL.addVar(sdcid)).append(" AND ");
        sql.append("paramlistid = ").append(safeSQL.addVar(paramlistid)).append(" AND ");
        sql.append("paramlistversionid = ").append(safeSQL.addVar(paramlistversionid)).append(" AND ");
        sql.append("variantid = ").append(safeSQL.addVar(variantid)).append(" AND ");
        sql.append("dataset = ").append(safeSQL.addVar(dataset)).append(" ");
        if (this.mode == 2) {
            sql.append("AND paramid = ").append(safeSQL.addVar(paramid)).append(" AND ");
            sql.append("paramtype = ").append(safeSQL.addVar(paramtype)).append(" AND ");
            sql.append("replicateid = ").append(safeSQL.addVar(replicateid)).append(" ");
        }
        return this.getQueryProcessor().execPreparedUpdate(sql.toString(), safeSQL.getValues()) == 1;
    }

    private PropertyList getRealProperties(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid, String rset, int mode, boolean autocreate) {
        PropertyList out = new PropertyList();
        out.setProperty(PROPERTY_KEYID1, "");
        out.setProperty(PROPERTY_KEYID2, "");
        out.setProperty(PROPERTY_KEYID3, "");
        out.setProperty(PROPERTY_SDCID, "");
        String error = "";
        DataSet ds = null;
        if (rset.length() > 0) {
            String sql = "SELECT sdcid, keyid1 as keycolvalue FROM rsetitems WHERE rsetid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rset});
        } else {
            PropertyList sdc = null;
            if (mode == 1) {
                sdc = this.getSDCProcessor().getPropertyList("DataSet");
            } else if (mode == 2) {
                sdc = this.getSDCProcessor().getPropertyList("DataItem");
            }
            if (sdc != null) {
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT ").append(sdc.getProperty("keycolid1", "")).append(" as keycolvalue FROM ").append(sdc.getProperty("tableid")).append(" WHERE ");
                sql.append("keyid1 = ").append(safeSQL.addVar(keyid1)).append(" AND ");
                if (keyid2.length() > 0) {
                    sql.append("keyid2 = ").append(safeSQL.addVar(keyid2)).append(" AND ");
                    if (keyid3.length() > 0) {
                        sql.append("keyid3 = ").append(safeSQL.addVar(keyid3)).append(" AND ");
                    }
                }
                sql.append("sdcid = ").append(safeSQL.addVar(sdcid)).append(" AND ");
                sql.append("paramlistid = ").append(safeSQL.addVar(paramlistid)).append(" AND ");
                sql.append("paramlistversionid = ").append(safeSQL.addVar(paramlistversionid)).append(" AND ");
                sql.append("variantid = ").append(safeSQL.addVar(variantid)).append(" AND ");
                sql.append("dataset = ").append(safeSQL.addVar(dataset)).append(" ");
                if (mode == 2) {
                    sql.append("AND paramid = ").append(safeSQL.addVar(paramid)).append(" AND ");
                    sql.append("paramtype = ").append(safeSQL.addVar(paramtype)).append(" AND ");
                    sql.append("replicateid = ").append(safeSQL.addVar(replicateid)).append(" ");
                }
                if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null) {
                    ds.addColumn(PROPERTY_SDCID, 0);
                    ds.setValue(0, PROPERTY_SDCID, sdc.getProperty(PROPERTY_SDCID));
                }
            } else {
                error = "Invalid mode.";
            }
        }
        if (ds != null && ds.getRowCount() > 0 && ds.getColumnCount() > 1) {
            String keyid = ds.getValue(0, "keycolvalue", "");
            if (keyid.length() == 0) {
                if (autocreate) {
                    this.logger.debug("Key not found... try to create...");
                    if (this.generateKey(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid)) {
                        out = this.getRealProperties(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, rset, mode, false);
                    } else {
                        error = "Could not update keyid.";
                    }
                } else {
                    this.logger.debug("No keyid obtained therefore will leave generation to save.");
                    out.setProperty(PROPERTY_KEYID1, "");
                    out.setProperty(PROPERTY_SDCID, ds.getValue(0, PROPERTY_SDCID, ""));
                }
            } else {
                out.setProperty(PROPERTY_KEYID1, ds.getValue(0, "keycolvalue", ""));
                out.setProperty(PROPERTY_SDCID, ds.getValue(0, PROPERTY_SDCID, ""));
            }
        } else {
            error = "Could not use rset to obtain dataset/dataitem sdikeyid.";
        }
        if (error.length() > 0) {
            out.setProperty("error", error);
        }
        return out;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (this.loadProperties()) {
            this.renderScriptAndStyle(html, this.greyButtons);
            html.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"  id=\"layout_maintable\" style=\"height:" + this.element.getProperty(PROPERTY_WIDTH, "100%") + ";width:" + this.element.getProperty("height", "100%") + ";\">");
            if (this.renderToolbar) {
                this.renderToolbar(html);
                html.append("<tr>");
                html.append("<td class=\"layout_pageshadow\"></td>");
                html.append("</tr>");
            }
            html.append("<tr>");
            boolean rtl = this.getConnectionProcessor().getSapphireConnection() != null && this.getConnectionProcessor().getSapphireConnection().isRtl();
            html.append("<td align=\"").append(rtl ? "right" : "left").append("\" valign=\"top\" >");
            if (this.innerScroll) {
                html.append("<div style=\"overflow-y:scroll;width:100%;height:100%;\" >");
            }
            html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"80%\">");
            if (!this.thumbnails) {
                html.append("<tr>");
                html.append("<td></td>");
                html.append("</tr>");
            }
            html.append("<tr>");
            html.append("<td valign=\"top\" align=\"").append(rtl ? "right" : "left").append("\">");
            StringBuffer tabstuff = new StringBuffer();
            SDIData sdidata = null;
            if (this.mode == 2 || this.mode == 1) {
                if (this.keyid1.length() == 0) {
                    PropertyList real = this.getRealProperties(this.plsdcid, this.plkeyid1, this.plkeyid2, this.plkeyid3, this.paramlistid, this.paramlistversionid, this.variantid, this.datasetnumber, this.paramid, this.paramtype, this.replicateid, this.rsetid, this.mode, !this.viewonly);
                    this.keyid1 = real.getProperty(PROPERTY_KEYID1, "");
                    this.keyid2 = real.getProperty(PROPERTY_KEYID2, "");
                    this.keyid3 = real.getProperty(PROPERTY_KEYID2, "");
                    if (real.getProperty("error", "").length() == 0) {
                        this.renderAdvancedScript(html, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.allowedit, this.allowOverwrite, this.onclose);
                        if (this.keyid1.length() == 0) {
                            if (this.viewonly) {
                                sdidata = this.getEmptyAttachmentData(this.plsdcid, this.plkeyid1, this.plkeyid2, this.plkeyid3, this.paramlistid, this.paramlistversionid, this.variantid, this.datasetnumber, this.paramid, this.paramtype, this.replicateid, this.mode);
                            } else {
                                this.logger.error("Could not obtain key for edit mode.");
                            }
                        } else {
                            sdidata = this.getAttachmentData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.rsetid, this.mode, this.viewonly);
                        }
                    } else {
                        this.logger.error(real.getProperty("error", ""));
                    }
                } else {
                    this.renderAdvancedScript(html, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.allowedit, this.allowOverwrite, this.onclose);
                    sdidata = this.getAttachmentData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.rsetid, this.mode, this.viewonly);
                }
            } else {
                if (this.sdcid.equalsIgnoreCase(PROPERTY_DATASET)) {
                    this.mode = 1;
                    this.logger.debug("Mode changed to = " + this.mode);
                } else if (this.sdcid.equalsIgnoreCase("dataitem")) {
                    this.mode = 2;
                    this.logger.debug("Mode changed to = " + this.mode);
                }
                if ((this.mode == 2 || this.mode == 1) && this.keyid1.length() == 0) {
                    this.logger.error("No keyid provided.");
                } else {
                    this.renderAdvancedScript(html, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.allowedit, this.allowOverwrite, this.onclose);
                    sdidata = this.getAttachmentData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.rsetid, this.mode, this.viewonly);
                }
            }
            if (sdidata != null) {
                this.rsetid = sdidata.getRsetid();
                this.logger.debug("rsetid = " + this.rsetid);
                DataSet primarydata = sdidata.getDataset("primary");
                if (primarydata != null) {
                    if (Trace.isDebugEnabled()) {
                        this.logger.debug("primarydata -------------- ");
                        primarydata.showData();
                    }
                    if ((this.mode == 1 || this.mode == 2) && primarydata.size() > 0) {
                        this.plsdcid = primarydata.getValue(0, PROPERTY_SDCID, "");
                        this.plkeyid1 = primarydata.getValue(0, PROPERTY_KEYID1, "");
                        this.plkeyid2 = primarydata.getValue(0, PROPERTY_KEYID2, "");
                        this.plkeyid3 = primarydata.getValue(0, PROPERTY_KEYID3, "");
                        this.paramlistid = primarydata.getValue(0, PROPERTY_PARAMLISTID, "");
                        this.paramlistversionid = primarydata.getValue(0, PROPERTY_PARAMLISTVERSIONID, "");
                        this.variantid = primarydata.getValue(0, PROPERTY_VARIANTID, "");
                        this.datasetnumber = primarydata.getValue(0, PROPERTY_DATASET, "1");
                        if (this.mode == 2) {
                            this.paramid = primarydata.getValue(0, PROPERTY_PARAMID, "");
                            this.paramtype = primarydata.getValue(0, PROPERTY_PARAMTYPE, "");
                            this.replicateid = primarydata.getValue(0, PROPERTY_REPLICATEID, "1");
                        } else {
                            this.paramid = "";
                            this.paramtype = "";
                            this.replicateid = "";
                        }
                    }
                    PropertyListCollection uploadData = FileSystem.getFileLocations(UPLOAD_FILELOCATION, this.pageContext);
                    for (int i = 0; i < uploadData.size(); ++i) {
                        PropertyList location = uploadData.getPropertyList(i);
                        String path = FileSystem.getFileLocation(location.getProperty("location", ""));
                        try {
                            if (path.length() != 0 && new File(path).exists()) continue;
                            this.logger.warn("Upload location \"" + path + "\" does not exist or is invalid and therefore shall not be used.");
                            uploadData.remove(i);
                            --i;
                            continue;
                        }
                        catch (Exception e) {
                            this.logger.warn("Upload location \"" + path + "\" could not validated and therefore shall not be used.");
                        }
                    }
                    String lockedby = this.getLockedBy(primarydata);
                    boolean islocked = lockedby.length() > 0;
                    StringBuffer mandatory = new StringBuffer();
                    PropertyListCollection columns = this.getColumns(this.viewonly, mandatory);
                    this.renderRsetScript(html, this.rsetid, islocked, uploadData, mandatory);
                    if (this.showprimary) {
                        SDCProcessor sdc;
                        String stext = tp.translate("Primary details");
                        if (primarydata.getRowCount() > 1) {
                            TabGroup pritabgroup = new TabGroup();
                            pritabgroup.setId("primary_tab");
                            sdc = this.getSDCProcessor();
                            if (this.mode == 2 || this.mode == 1) {
                                pritabgroup.setBodywidth("850");
                            } else {
                                pritabgroup.setBodywidth("430");
                            }
                            for (int row = 0; row < primarydata.getRowCount(); ++row) {
                                Tab pritab = new Tab();
                                pritab.setText(sdc.getProperty(this.sdcid, "singular", this.sdcid) + " " + primarydata.getValue(row, sdc.getProperty(this.sdcid, "keycolid1"), ""));
                                pritab.setTip(stext);
                                pritab.setId("primary_tab");
                                pritab.setCollapsedtext(stext);
                                pritab.setExpandable("true");
                                pritab.setExpanded("true");
                                pritab.setContent(this.renderPrimaryContent(this.sdcid, primarydata, row, islocked, lockedby, this.mode, tp));
                                pritabgroup.setTab(pritab);
                            }
                            html.append(pritabgroup.getHtml());
                        } else {
                            Tab pritab = new Tab();
                            sdc = this.getSDCProcessor();
                            pritab.setText(tp.translate(sdc.getProperty(this.sdcid, "singular", this.sdcid)));
                            pritab.setTip(stext);
                            pritab.setId("primary_tab");
                            pritab.setCollapsedtext(stext);
                            pritab.setExpandable("true");
                            pritab.setExpanded("true");
                            if (this.mode == 2 || this.mode == 1 || sdc.getProperty(this.sdcid, "keycolid2", "").length() > 0) {
                                pritab.setBodywidth("850");
                            } else {
                                pritab.setBodywidth("430");
                            }
                            pritab.setContent(this.renderPrimaryContent(this.sdcid, primarydata, islocked, lockedby, this.mode, tp));
                            html.append(pritab.getHtml());
                        }
                        html.append("<br>&nbsp;<p>");
                    }
                    if (primarydata.getRowCount() == 1 || this.thumbnails) {
                        DataSet attdata = sdidata.getDataset("attachment");
                        if (attdata != null) {
                            if (Trace.isDebugEnabled()) {
                                this.logger.debug("attdata -------------- ");
                                attdata.showData();
                            }
                            attdata.sort("attachmentnum");
                            Tab tab = null;
                            if (this.showTab) {
                                tab = new Tab();
                                tab.setText(tp.translate(this.parseTabText(this.tabtitle, this.sdcid, this.keyid1, this.keyid2, this.keyid3, primarydata, attdata.getRowCount())));
                                tab.setTip(tp.translate("Attachment Manager"));
                                tab.setId("attachmentManager_tab");
                                if (this.showprimary) {
                                    tab.setCollapsedtext(tp.translate("Attachment Manager"));
                                    tab.setExpandable("true");
                                    tab.setExpanded("true");
                                }
                            }
                            if (columns != null) {
                                StringBuffer content = new StringBuffer();
                                content.append("<div id=\"attachment_content\" style=\"width:100%;overflow:hidden;\">");
                                if (this.viewonly && this.thumbnails) {
                                    if (this.keyid1.indexOf(";") > -1) {
                                        String[] keys1 = StringUtil.split(this.keyid1, ";", true);
                                        String[] keys2 = this.keyid2.length() > 0 ? StringUtil.split(this.keyid2, ";", true) : null;
                                        String[] keys3 = this.keyid3.length() > 0 ? StringUtil.split(this.keyid3, ";", true) : null;
                                        StringBuffer tabText = new StringBuffer();
                                        StringBuffer tabAction = new StringBuffer();
                                        tabText.append(tp.translate("All ")).append(this.sdcid).append(tp.translate(" Attachments ")).append(" (").append(attdata.getRowCount()).append(")");
                                        tabAction.append(MAIN_JS_OBJECT).append(".changeThumbnailTab(").append(0).append(",").append(keys1.length + 1).append(")");
                                        content.append("<div id=\"tabContent_0\" style=\"display:block;\">");
                                        this.renderThumbnailView(content, columns, attdata);
                                        content.append("</div>");
                                        for (int a = 0; a < keys1.length; ++a) {
                                            String currKey1 = keys1[a];
                                            tabText.append(";");
                                            tabAction.append(";");
                                            HashMap<String, String> filterMap = new HashMap<String, String>();
                                            filterMap.put(PROPERTY_KEYID1, currKey1);
                                            if (keys2 != null && keys2.length == keys1.length) {
                                                filterMap.put(PROPERTY_KEYID2, keys2[a]);
                                            }
                                            if (keys3 != null && keys3.length == keys1.length) {
                                                filterMap.put(PROPERTY_KEYID3, keys3[a]);
                                            }
                                            DataSet filteredAttData = attdata.getFilteredDataSet(filterMap);
                                            tabText.append(tp.translate(this.parseTabText(this.tabtitle, this.sdcid, currKey1, keys2 != null && keys2.length == keys1.length ? keys2[a] : "", keys3 != null && keys3.length == keys1.length ? keys3[a] : "", primarydata, filteredAttData.getRowCount())));
                                            content.append("<div id=\"tabContent_" + (a + 1) + "\" style=\"display:none;\">");
                                            this.renderThumbnailView(content, columns, filteredAttData);
                                            content.append("</div>");
                                            tabAction.append(MAIN_JS_OBJECT).append(".changeThumbnailTab(").append(a + 1).append(",").append(keys1.length + 1).append(")");
                                        }
                                        tab.setText(tabText.toString());
                                        tab.setAction(tabAction.toString());
                                    } else {
                                        this.renderThumbnailView(content, columns, attdata);
                                    }
                                } else {
                                    content.append("&nbsp;<p>");
                                    PropertyListCollection btns = this.element.containsKey("pagedata") ? this.element.getPropertyList("pagedata").getCollection(PROPERTY_BUTTONS) : this.element.getCollection(PROPERTY_BUTTONS);
                                    if (btns == null) {
                                        btns = new PropertyListCollection();
                                        if (!this.viewonly) {
                                            PropertyList btn = new PropertyList();
                                            btn.setProperty(PROPERTY_TEXT, "Add");
                                            btn.setProperty(PROPERTY_JS, "attachmentManager.add()");
                                            btn.setProperty(PROPERTY_IMG, "WEB-CORE/images/gif/Add.gif");
                                            btn.setProperty(PROPERTY_TIP, "Add attachment");
                                            btns.add(btn);
                                            btn = new PropertyList();
                                            btn.setProperty(PROPERTY_TEXT, "Remove");
                                            btn.setProperty(PROPERTY_JS, "attachmentManager.remove()");
                                            btn.setProperty(PROPERTY_IMG, "WEB-CORE/images/gif/Delete.gif");
                                            btn.setProperty(PROPERTY_TIP, "Remove attachment");
                                            btns.add(btn);
                                        }
                                    }
                                    if (this.buttonpos == 0 || this.buttonpos == 2) {
                                        this.renderButtons(content, btns, tp);
                                        content.append("<br>&nbsp;<p>");
                                    }
                                    this.renderTable(content, columns, attdata, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.allowedit, this.canRestore, islocked, lockedby, uploadData, tp);
                                    content.append("&nbsp;<p>");
                                    if (this.buttonpos == 1 || this.buttonpos == 2) {
                                        this.renderButtons(content, btns, tp);
                                    }
                                }
                                content.append("</div>");
                                if (tab != null) {
                                    tab.setContent(content.toString());
                                    tabstuff.append(tab.getHtml());
                                } else {
                                    tabstuff.append(content);
                                }
                                if (!this.viewonly) {
                                    tabstuff.append("<iframe style=\"display:none\" width=600 height=200 source=\"about:blank\" name=\"upload_frame\" id=\"upload_frame\" onload=\"").append(MAIN_JS_OBJECT).append(".uploadFrameLoad(this, event)").append("\"></iframe>");
                                }
                                tabstuff.append("<form id=refreshform name=refreshform action=\"").append(this.amPage).append("\" method=POST>");
                                if (this.mode == 0) {
                                    tabstuff.append("<input type=hidden name=\"sdcid\" value=\"").append(this.sdcid).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid1\" value=\"").append(this.keyid1).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid2\" value=\"").append(this.keyid2).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid3\" value=\"").append(this.keyid3).append("\">");
                                } else {
                                    tabstuff.append("<input type=hidden name=\"sdcid\" value=\"").append(this.plsdcid).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid1\" value=\"").append(this.plkeyid1).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid2\" value=\"").append(this.plkeyid2).append("\">");
                                    tabstuff.append("<input type=hidden name=\"keyid3\" value=\"").append(this.plkeyid3).append("\">");
                                    tabstuff.append("<input type=hidden name=\"paramlistid\" value=\"").append(this.paramlistid).append("\">");
                                    tabstuff.append("<input type=hidden name=\"paramlistversionid\" value=\"").append(this.paramlistversionid).append("\">");
                                    tabstuff.append("<input type=hidden name=\"variantid\" value=\"").append(this.variantid).append("\">");
                                    tabstuff.append("<input type=hidden name=\"dataset\" value=\"").append(this.datasetnumber).append("\">");
                                    if (this.mode == 2) {
                                        tabstuff.append("<input type=hidden name=\"sdidataitemid\" value=\"").append(this.keyid1).append("\">");
                                        tabstuff.append("<input type=hidden name=\"paramid\" value=\"").append(this.paramid).append("\">");
                                        tabstuff.append("<input type=hidden name=\"paramtype\" value=\"").append(this.paramtype).append("\">");
                                        tabstuff.append("<input type=hidden name=\"replicateid\" value=\"").append(this.replicateid).append("\">");
                                    } else {
                                        tabstuff.append("<input type=hidden name=\"sdidataid\" value=\"").append(this.keyid1).append("\">");
                                    }
                                }
                                tabstuff.append("<input type=hidden name=\"rsetid\" value=\"").append(this.rsetid).append("\">");
                                tabstuff.append("<input type=hidden name=\"onclose\" value=\"").append(this.onclose).append("\">");
                                tabstuff.append("</form>");
                                tabstuff.append("<form id=viewform name=viewform action=\"rc?command=attachment").append("\" method=POST target=\"_blank\">");
                                tabstuff.append("<input type=hidden name=mode value=\"").append("view").append("\">");
                                tabstuff.append("<input type=hidden name=sdcid value=\"").append(this.sdcid).append("\">");
                                tabstuff.append("<input type=hidden name=keyid1 value=\"").append(this.keyid1).append("\">");
                                tabstuff.append("<input type=hidden name=keyid2 value=\"").append(this.keyid2).append("\">");
                                tabstuff.append("<input type=hidden name=keyid3 value=\"").append(this.keyid3).append("\">");
                                tabstuff.append("<input type=hidden name=attachmentnum value=\"\">");
                                tabstuff.append("<input type=hidden name=download value=\"N\">");
                                tabstuff.append("</form>");
                            } else {
                                this.debugErrorMsg = "Core columns not defined in column collection.";
                                this.logger.error(this.debugErrorMsg);
                            }
                        } else {
                            this.debugErrorMsg = "Could not obtain attachment attdata.";
                            this.logger.error(this.debugErrorMsg);
                        }
                    } else {
                        this.debugErrorMsg = "Only a single SDI can have attachments managed.";
                        this.logger.error(this.debugErrorMsg);
                    }
                } else {
                    this.debugErrorMsg = "Could not obtain Primary data.";
                    this.logger.error(this.debugErrorMsg);
                }
            } else {
                this.debugErrorMsg = "Could not obtain SDI data.";
                this.logger.error(this.debugErrorMsg);
            }
            if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
                html.append(tabstuff);
            }
        } else {
            this.debugErrorMsg = "Could not load required properties.";
            this.logger.error(this.debugErrorMsg);
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            html.append(this.getError());
        }
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        if (this.innerScroll) {
            html.append("</div>");
        }
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        if (html.length() > 0) {
            return html.toString();
        }
        return "";
    }

    private String getAllowableTypes() {
        PropertyListCollection types = this.element.containsKey("pagedata") ? this.element.getPropertyList("pagedata").getCollection(PROPERTY_TYPES) : this.element.getCollection(PROPERTY_TYPES);
        String allowableTypes = "";
        if (types != null) {
            for (int i = 0; i < types.size(); ++i) {
                String typeflag = Attachment.correctTypeFlag(types.getPropertyList(i).getProperty(PROPERTY_TYPE));
                if (typeflag.length() <= 0) continue;
                allowableTypes = allowableTypes + ";" + typeflag;
            }
            String string = allowableTypes = allowableTypes.length() > 0 ? allowableTypes.substring(1) : "";
        }
        if (allowableTypes.length() == 0) {
            allowableTypes = Attachment.getAllTypeflagList();
        }
        return allowableTypes;
    }

    private boolean isAllowed(String type, String allowableTypes) {
        return allowableTypes.contains(type);
    }
}

