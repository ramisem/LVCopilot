/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Tree;
import com.labvantage.sapphire.pageelements.forms.PropertyBuilder;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormBuilder
extends BaseElement {
    private static final String IMAGE_DATASOURCES_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_DATASOURCES_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_FIELDS_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_FIELDS_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_COLUMNS_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_COLUMNS_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_SECTIONS_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_SECTIONS_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_GROUPS_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_GROUPS_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_PAGES_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_PAGES_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_ELEMENTS_C = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
    private static final String IMAGE_ELEMENTS_E = "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg";
    private static final String IMAGE_PAGE = "WEB-CORE/imageref/flat/16/flat_black_file2.svg";
    private static final String IMAGE_ELEMENT = "WEB-CORE/imageref/flat/16/flat_black_markup.svg";
    private static final String IMAGE_SCREEN = "WEB-CORE/imageref/flat/16/flat_black_monitor.svg";
    private static final String IMAGE_DATASOURCE = "WEB-CORE/imageref/flat/16/flat_black_database.svg";
    private static final String IMAGE_FIELD = "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg";
    private static final String IMAGE_FIELD_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg";
    private static final String IMAGE_FIELD_CONTROLLED = "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg";
    private static final String IMAGE_FIELD_CONTROLLED_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg";
    private static final String IMAGE_MEMBER = "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg";
    private static final String IMAGE_MEMBER_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg";
    private static final String IMAGE_MEMBER_CONTROLED = "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg";
    private static final String IMAGE_MEMBER_CONTROLED_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg";
    private static final String IMAGE_GROUP = "WEB-CORE/imageref/flat/16/flat_black_list.svg";
    private static final String IMAGE_LABEL = "WEB-CORE/imageref/flat/16/flat_black_font.svg";
    private static final String IMAGE_LABEL_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_font.svg";
    private static final String IMAGE_SECTION = "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg";
    private static final String IMAGE_SECTION_LOCKED = "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg";
    private static final String DEFAULTPAGE_CONFIG = "formbuilder_defaultpage";
    public static final String CACHE_FORMOBJECTS = "formbuilder_formobjects_pd";
    public static final String CACHE_FORMOBJECTS_SIMPLE = "formbuilder_formobjects_simple_pd";
    public static final String CACHE_FORMOBJECTS_ELN = "formbuilder_formobjects_simple_pd";
    private static final String RICHTEXT_ELEMENT = "richTextElement";
    private static final String DEFAULT_PAGE = "page001";
    public static final String PROPERTY_FORMOBJECT = "formobject";
    public static final String PROPERTY_FORMLAYOUT = "formlayout";
    public static final String PROPERTY_FORMPROPERTIES = "formproperties";
    public static final String PROPERTY_FORMMAINTVALUES = "formmaintvalues";
    public static final String PROPERTY_FORMLETMAINTVALUES = "formletmaintvalues";
    public static final String PROPERTY_FIELDIDS = "fieldids";
    public static final String PROPERTY_CALLBACK = "callback";
    public static final String PROPERTY_FORMLET = "formlet";
    public static final String PROPERTY_BUILDMODE = "buildmode";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_SHOWBUTTONS = "showbuttons";
    public static final String PROPERTY_SHOWOBJECTS = "showobjects";
    public static final String PROPERTY_EMBEDDED = "embedded";
    public static final String PROPERTY_PHRASETYPE = "phrasetype";
    public static final String PROPERTY_PHRASELOOKUP = "phraselookup";
    public static final String RESOURCE_FILE = "formobjects.xml";
    public static final String RESOURCE_FILE_SIMPLE = "simpleformobjects.xml";
    public static final String RESOURCE_FILE_ELN = "elnformobjects.xml";
    private String keyid1;
    private String keyid2;
    private PropertyList formprops;
    private boolean xmlMode = false;
    private boolean formObjectMode = false;
    private String formlayout;
    private PropertyList userConfig;
    private String fieldIds;
    private String callbackFunc;
    private boolean isFormlet;
    private Mode mode = Mode.FORM;
    private boolean viewonly = false;
    private boolean showbuttons = false;
    private boolean showobjects = false;
    private boolean embedded = false;
    private String phrasetype = "";
    private String phraselookup = "";
    private boolean devMode;

    public FormBuilder(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.mode = Mode.getMode(pageproperties.getProperty("mode", ""));
            this.logger.debug("mode = " + this.mode.toString());
            this.setUpObjects(pageContext, this.mode);
            this.setUp(pageproperties, (HttpServletRequest)pageContext.getRequest());
        }
        catch (Exception e) {
            this.formprops = null;
            this.logger.error("Could not set up form builder: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private InputStream getResourceStream(Mode mode) {
        String resourceFile = mode == Mode.SIMPLEFORM ? RESOURCE_FILE_SIMPLE : (mode == Mode.ELNFORM ? RESOURCE_FILE_ELN : RESOURCE_FILE);
        URL fileurl = this.getClass().getResource(resourceFile);
        InputStream out = null;
        if (fileurl != null) {
            if (fileurl.getPath().contains(".jar!/")) {
                this.logger.info("Resource " + resourceFile + " is Jar resource.");
            } else {
                this.logger.info("Resource " + resourceFile + " is class resource.");
            }
            InputStream is = this.getClass().getResourceAsStream(resourceFile);
            if (is != null) {
                this.logger.debug("Input stream obatined.");
                out = is;
            } else {
                this.logger.warn("Could not load formobjects.xml resource. Input stream could not be created.");
            }
        } else {
            this.logger.warn("Could not load formobjects.xml resource. File could not be found.");
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setUpObjects(PageContext pageConext, Mode mode) {
        PropertyDefinitionList propdeflist = null;
        String cache = mode == Mode.SIMPLEFORM ? "formbuilder_formobjects_simple_pd" : (mode == Mode.ELNFORM ? "formbuilder_formobjects_simple_pd" : CACHE_FORMOBJECTS);
        Object ob = pageConext.getSession().getAttribute(cache);
        if (ob == null || !(ob instanceof PropertyDefinitionList) || this.devMode) {
            block12: {
                this.logger.debug("Objects not in cache, thus load.");
                try {
                    InputStream is = this.getResourceStream(mode);
                    if (is != null) {
                        try {
                            PropertyTree tree = new PropertyTree();
                            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
                            handler.setXMLString(FileUtil.getInputStreamString(is));
                            handler.setPrintStream(null);
                            SaxUtil.parseString(handler);
                            propdeflist = tree.getPropertyDefinitionList();
                            break block12;
                        }
                        finally {
                            is.close();
                        }
                    }
                    this.logger.warn("Property definition could be obtained");
                }
                catch (Exception e) {
                    this.logger.warn("Could not load resource. File could not be parsed. " + e.getMessage());
                }
            }
            if (propdeflist == null) {
                propdeflist = new PropertyDefinitionList("formobjects");
            } else {
                propdeflist.setPropertyDefId("formobjects");
            }
            if (pageConext != null && pageConext.getSession() != null) {
                pageConext.getSession().setAttribute(cache, (Object)propdeflist);
            } else {
                this.logger.warn("Could not cache formobjects. PageContext is null.");
            }
        } else {
            this.logger.debug("Obtained objects from cache.");
        }
    }

    private PropertyList getFormProps(boolean xmlMode, String propertylist) {
        PropertyList out;
        if (xmlMode) {
            try {
                out = new PropertyList();
                out.setPropertyList(propertylist);
            }
            catch (Exception e) {
                out = new PropertyList();
                this.logger.error("Could not create propertylist(1): " + e.getMessage(), e);
            }
        } else {
            try {
                out = new PropertyList(new JSONObject(propertylist));
            }
            catch (Exception e) {
                out = new PropertyList();
                this.logger.error("Could not create propertylist(2): " + e.getMessage(), e);
            }
        }
        return out;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setUp(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String[] values;
        PropertyList maintprops;
        pagedata.setProperty("jsrequest", "exclude=all");
        String show = pagedata.getProperty("showtitle", "N");
        this.logger.debug("show = " + show);
        PropertyList layout = pagedata.getPropertyList("layout");
        if (layout != null) {
            layout.setProperty("hideshadow", "Y");
            if (show.equalsIgnoreCase("N")) {
                layout.setProperty("hidetitle", "Y");
            } else {
                layout.setProperty("hidetitle", "N");
            }
        }
        String formlet = pagedata.getProperty(PROPERTY_FORMLET, "N");
        this.isFormlet = formlet.equalsIgnoreCase("Y");
        this.logger.debug("isFormlet = " + this.isFormlet);
        this.phrasetype = pagedata.getProperty(PROPERTY_PHRASETYPE, "");
        this.phraselookup = pagedata.getProperty(PROPERTY_PHRASELOOKUP, "");
        this.keyid1 = pagedata.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pagedata.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.showbuttons = !pagedata.getProperty(PROPERTY_SHOWBUTTONS, "y").equalsIgnoreCase("n");
        this.logger.debug("showbuttons = " + this.showbuttons);
        this.showobjects = !pagedata.getProperty(PROPERTY_SHOWOBJECTS, "y").equalsIgnoreCase("n");
        this.logger.debug("showobjects = " + this.showobjects);
        this.embedded = pagedata.getProperty(PROPERTY_EMBEDDED, "n").equalsIgnoreCase("y");
        this.logger.debug("embedded = " + this.embedded);
        String xmlmode = pagedata.getProperty("xmlmode", "Y");
        this.logger.debug("xmlmode = " + xmlmode);
        this.xmlMode = xmlmode.equalsIgnoreCase("y") || xmlmode.equalsIgnoreCase("true") || xmlmode.equalsIgnoreCase("yes");
        this.formObjectMode = pagedata.containsKey(PROPERTY_FORMOBJECT);
        if (this.formObjectMode) {
            String formobjectstring = pagedata.getProperty(PROPERTY_FORMOBJECT, this.xmlMode ? "<propertylist></propertylist>" : "{}").trim();
            if (pagedata.getProperty("encodedformobject", "N").equalsIgnoreCase("Y")) {
                formobjectstring = sapphire.util.HttpUtil.decodeURIComponent(formobjectstring).trim();
                pagedata.setProperty(PROPERTY_FORMOBJECT, formobjectstring);
            }
            this.logger.debug("Form object passed");
            this.xmlMode = formobjectstring.startsWith("<propertylist") && formobjectstring.endsWith("</propertylist>") || formobjectstring.equals("<propertylist/>");
            try {
                PropertyList pl;
                PropertyList formobject = new PropertyList();
                formobject.setPropertyList(formobjectstring);
                PropertyList propertyList = pl = formobject.containsKey(PROPERTY_FORMPROPERTIES) ? formobject.get(PROPERTY_FORMPROPERTIES) : new PropertyList();
                this.formprops = pl instanceof PropertyList ? pl : this.getFormProps(this.xmlMode, formobject.getProperty(PROPERTY_FORMPROPERTIES, this.xmlMode ? "<propertylist></propertylist>" : "{}"));
                this.formlayout = formobject.getProperty(PROPERTY_FORMLAYOUT, "");
            }
            catch (Exception e) {
                throw new SapphireException("Could not create propertylist(formobject): " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        } else {
            this.logger.debug("Traditional form propertylist and layout passed.");
            String propertylist = pagedata.getProperty(PROPERTY_FORMPROPERTIES, this.xmlMode ? "<propertylist></propertylist>" : "{}").trim();
            this.xmlMode = propertylist.startsWith("<propertylist") && propertylist.endsWith("</propertylist>") || propertylist.equals("<propertylist/>");
            this.formprops = this.getFormProps(this.xmlMode, propertylist);
            this.formlayout = pagedata.getProperty(PROPERTY_FORMLAYOUT, "");
        }
        this.logger.debug("xmlMode = " + this.xmlMode);
        String formmaintvalues = pagedata.getProperty(PROPERTY_FORMMAINTVALUES, "");
        String formletmaintvalues = pagedata.getProperty(PROPERTY_FORMLETMAINTVALUES, "");
        if (formmaintvalues.length() > 0) {
            maintprops = new PropertyList();
            values = StringUtil.split(formmaintvalues, ";");
            for (int i = 0; i < values.length; ++i) {
                int pos = values[i].indexOf("=");
                maintprops.setProperty(values[i].substring(0, pos), pos + 1 == values[i].length() ? "" : values[i].substring(pos + 1));
            }
            this.formprops.setProperty("form", maintprops);
            PropertyListCollection datasources = this.formprops.getCollection("datasources");
            if ((datasources == null || datasources.size() == 0) && maintprops.getProperty("formtype").equals("Worksheet") && maintprops.getProperty("worksheettype").length() > 0) {
                PropertyList statusmgmt;
                PropertyList sdi;
                datasources = new PropertyListCollection();
                this.formprops.setProperty("datasources", datasources);
                if (maintprops.getProperty("worksheettype").equals("sdi")) {
                    PropertyList datasource = new PropertyList();
                    datasources.add(datasource);
                    datasource.setProperty("datasourceid", "SDIDatasource");
                    datasource.setProperty("type", "sdi");
                    sdi = new PropertyList();
                    datasource.setProperty("sdi", sdi);
                    sdi.setProperty("sdcid", "$G{params.sdcid}");
                    sdi.setProperty(PROPERTY_KEYID1, "$G{params.keyid1}");
                    sdi.setProperty(PROPERTY_KEYID2, "$G{params.keyid2}");
                    sdi.setProperty("keyid3", "$G{params.keyid3}");
                    statusmgmt = new PropertyList();
                    statusmgmt.setProperty("autorelease", "Y");
                    statusmgmt.setProperty("syncprimarystatus", "Y");
                    statusmgmt.setProperty("primarystatuscolumn", "");
                    statusmgmt.setProperty("syncaqcstatus", "");
                    datasource.setProperty("statusmgmt", statusmgmt);
                } else if (maintprops.getProperty("worksheettype").equals("dataset")) {
                    PropertyList datasource = new PropertyList();
                    datasources.add(datasource);
                    datasource.setProperty("datasourceid", "DataSetDatasource");
                    datasource.setProperty("type", "dataset");
                    PropertyList dataset = new PropertyList();
                    datasource.setProperty("dataset", dataset);
                    dataset.setProperty("sdcid", "$G{params.sdcid}");
                    dataset.setProperty(PROPERTY_KEYID1, "$G{params.keyid1}");
                    dataset.setProperty(PROPERTY_KEYID2, "$G{params.keyid2}");
                    dataset.setProperty("keyid3", "$G{params.keyid3}");
                    dataset.setProperty("paramlistid", "$G{params.paramlistid}");
                    dataset.setProperty("paramlistversionid", "$G{params.paramlistversionid}");
                    dataset.setProperty("variantid", "$G{params.variantid}");
                    dataset.setProperty("dataset", "$G{params.dataset}");
                    statusmgmt = new PropertyList();
                    statusmgmt.setProperty("autorelease", "Y");
                    statusmgmt.setProperty("syncprimarystatus", "Y");
                    statusmgmt.setProperty("primarystatuscolumn", "");
                    statusmgmt.setProperty("syncaqcstatus", "");
                    datasource.setProperty("statusmgmt", statusmgmt);
                } else if (maintprops.getProperty("worksheettype").equals("workitem")) {
                    PropertyList datasource = new PropertyList();
                    datasources.add(datasource);
                    datasource.setProperty("datasourceid", "WorkItemDatasource");
                    datasource.setProperty("type", "workitem");
                    PropertyList workitem = new PropertyList();
                    datasource.setProperty("workitem", workitem);
                    workitem.setProperty("sdcid", "$G{params.sdcid}");
                    workitem.setProperty(PROPERTY_KEYID1, "$G{params.keyid1}");
                    workitem.setProperty(PROPERTY_KEYID2, "$G{params.keyid2}");
                    workitem.setProperty("keyid3", "$G{params.keyid3}");
                    workitem.setProperty("workitemid", "$G{params.workitemid}");
                    workitem.setProperty("workiteminstance", "$G{params.workiteminstance}");
                    statusmgmt = new PropertyList();
                    statusmgmt.setProperty("autorelease", "Y");
                    statusmgmt.setProperty("syncprimarystatus", "Y");
                    statusmgmt.setProperty("primarystatuscolumn", "");
                    statusmgmt.setProperty("syncaqcstatus", "");
                    datasource.setProperty("statusmgmt", statusmgmt);
                } else if (maintprops.getProperty("worksheettype").equals("qcbatch")) {
                    PropertyList qcbdatasource = new PropertyList();
                    datasources.add(qcbdatasource);
                    qcbdatasource.setProperty("datasourceid", "QCBatchDatasource");
                    qcbdatasource.setProperty("type", "sdi");
                    sdi = new PropertyList();
                    qcbdatasource.setProperty("sdi", sdi);
                    sdi.setProperty("sdcid", "QCBatch");
                    sdi.setProperty(PROPERTY_KEYID1, "$G{params.qcbatchid}");
                    sdi.setProperty(PROPERTY_KEYID2, "");
                    sdi.setProperty("keyid3", "");
                    PropertyList qcbstatusmgmt = new PropertyList();
                    qcbstatusmgmt.setProperty("autorelease", "");
                    qcbstatusmgmt.setProperty("syncprimarystatus", "");
                    qcbstatusmgmt.setProperty("primarystatuscolumn", "");
                    qcbstatusmgmt.setProperty("syncaqcstatus", "");
                    qcbdatasource.setProperty("statusmgmt", qcbstatusmgmt);
                    PropertyList qcsdatasource = new PropertyList();
                    datasources.add(qcsdatasource);
                    qcsdatasource.setProperty("datasourceid", "QCBatchItemsDatasource");
                    qcsdatasource.setProperty("type", "qcbatch");
                    PropertyList qcbatch = new PropertyList();
                    qcsdatasource.setProperty("qcbatch", qcbatch);
                    qcbatch.setProperty("qcbatchid", "$G{params.qcbatchid}");
                    PropertyList qcsstatusmgmt = new PropertyList();
                    qcsstatusmgmt.setProperty("autorelease", "Y");
                    qcsstatusmgmt.setProperty("syncprimarystatus", "Y");
                    qcsstatusmgmt.setProperty("primarystatuscolumn", "");
                    qcsstatusmgmt.setProperty("syncaqcstatus", "Y");
                    qcsdatasource.setProperty("statusmgmt", qcsstatusmgmt);
                    PropertyList qcbstdatasource = new PropertyList();
                    datasources.add(qcbstdatasource);
                    qcbstdatasource.setProperty("datasourceid", "QCBatchSampleTypeDatasource");
                    qcbstdatasource.setProperty("type", "sdi");
                    PropertyList stsdi = new PropertyList();
                    qcbstdatasource.setProperty("sdi", stsdi);
                    stsdi.setProperty("sdcid", "QCBatchSampleType");
                    stsdi.setProperty("queryfrom", "s_qcbatchsampletype");
                    stsdi.setProperty("querywhere", "$G{\"qcbatchid = '${params.qcbatchid}'\"}");
                    stsdi.setProperty("queryorderby", "s_qcbatchsampletypeid");
                    PropertyList qcbststatusmgmt = new PropertyList();
                    qcbststatusmgmt.setProperty("autorelease", "");
                    qcbststatusmgmt.setProperty("syncprimarystatus", "");
                    qcbststatusmgmt.setProperty("primarystatuscolumn", "");
                    qcbststatusmgmt.setProperty("syncaqcstatus", "");
                    qcbstdatasource.setProperty("statusmgmt", qcbststatusmgmt);
                }
            }
        }
        if (formletmaintvalues.length() > 0) {
            maintprops = new PropertyList();
            values = StringUtil.split(formletmaintvalues, ";");
            for (int i = 0; i < values.length; ++i) {
                int pos = values[i].indexOf("=");
                maintprops.setProperty(values[i].substring(0, pos), pos + 1 == values[i].length() ? "" : values[i].substring(pos + 1));
            }
            this.formprops.setProperty(PROPERTY_FORMLET, maintprops);
        }
        if (this.formprops == null) throw new SapphireException("Propertylist could not be created.");
        if (this.formlayout.length() == 0) {
            this.logger.debug("No layout provided... start.");
        }
        this.fieldIds = pagedata.getProperty(PROPERTY_FIELDIDS, "");
        this.callbackFunc = pagedata.getProperty(PROPERTY_CALLBACK, "");
        this.logger.debug("fieldIds = " + this.fieldIds + ", callbackFunc = " + this.callbackFunc);
        if (this.callbackFunc.length() <= 0 && this.fieldIds.length() <= 0) throw new SapphireException("Either a callback function or return field ids are required.");
        if ((this.mode == Mode.FORM || this.mode == Mode.SIMPLEFORM || this.mode == Mode.ELNFORM) && this.formprops.containsKey("columns")) {
            throw new SapphireException("Mode is set to form but property list contains columns.");
        }
        if (this.mode == Mode.MAINTENANCE && this.formprops.containsKey("fields")) {
            throw new SapphireException("Mode is set to evergreen but property list contains fields.");
        }
        if (this.mode == Mode.MAINTENANCE && this.formprops.containsKey("maint")) {
            this.logger.debug("Maint element found so use...");
            this.formprops = this.formprops.getPropertyList("maint");
        }
        if (!(this.mode != Mode.FORM && this.mode != Mode.SIMPLEFORM && this.mode != Mode.ELNFORM || this.formprops.containsKey("fields"))) {
            this.logger.debug("Fields collection not found therefore create...");
            this.formprops.setProperty("fields", new PropertyListCollection());
        } else if (this.mode == Mode.MAINTENANCE && !this.formprops.containsKey("columns")) {
            this.logger.debug("Columns collection not found therefore create...");
            this.formprops.setProperty("columns", new PropertyListCollection());
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig != null) return;
        throw new SapphireException("User configuration could not be obtained.");
    }

    public static String getObjectsHtml(PropertyList props, Mode buildMode, boolean isFormlet, String connectionId, TranslationProcessor tp, HttpServletRequest request) {
        RequestContext rc;
        StringBuffer sb = new StringBuffer();
        PropertyList userConfig = null;
        if (request != null && (rc = RequestContext.getInstance(request)) != null) {
            if (connectionId == null || connectionId.length() == 0) {
                connectionId = rc.getConnectionId();
            }
            userConfig = rc.getPropertyList("userconfig");
        }
        PropertyDefinitionList propertydeflist = null;
        Object defob = request.getSession().getAttribute(buildMode == Mode.SIMPLEFORM ? "formbuilder_formobjects_simple_pd" : (buildMode == Mode.ELNFORM ? "formbuilder_formobjects_simple_pd" : CACHE_FORMOBJECTS));
        if (defob != null && defob instanceof PropertyDefinitionList) {
            propertydeflist = (PropertyDefinitionList)defob;
        }
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            if (props != null && (props.containsKey("fields") || props.containsKey("columns"))) {
                String title;
                PropertyList subitem;
                String id;
                String groupstext;
                PropertyListCollection subitems;
                PropertyList tree = new PropertyList();
                PropertyListCollection rootitems = new PropertyListCollection();
                if (!(isFormlet || propertydeflist != null && propertydeflist.getPropertyDef("datasources") == null)) {
                    String datasourcestext = "Data Sources";
                    if (tp != null) {
                        datasourcestext = tp.translate(datasourcestext);
                    }
                    PropertyList datasourcerootitem = new PropertyList();
                    datasourcerootitem.setProperty("expanded", "N");
                    datasourcerootitem.setProperty("translate", "N");
                    datasourcerootitem.setProperty("showexpandcollapse", "Y");
                    datasourcerootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                    datasourcerootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                    datasourcerootitem.setProperty("prehtml", "<div id=\"formbuilder_di_\" itemlocked=false collectionid=\"datasources\" itemid=\"\">");
                    datasourcerootitem.setProperty("posthtml", "</div>");
                    subitems = new PropertyListCollection();
                    PropertyListCollection datasources = props.getCollection("datasources");
                    if (datasources == null) {
                        datasources = new PropertyListCollection();
                        props.setProperty("datasources", datasources);
                    }
                    if (datasources.size() > 0) {
                        datasourcerootitem.setProperty("text", datasourcestext + " (" + datasources.size() + ")");
                        for (int i = 0; i < datasources.size(); ++i) {
                            PropertyList datasource = datasources.getPropertyList(i);
                            String id2 = datasource.getProperty("datasourceid", "");
                            if (id2.length() > 0) {
                                String title2 = datasource.getProperty("type", "");
                                if (title2.length() == 0) {
                                    title2 = "No Type";
                                    if (tp != null) {
                                        title2 = tp.translate(title2);
                                    }
                                }
                                PropertyList subitem2 = new PropertyList();
                                subitem2.setProperty("text", id2 + " (" + title2 + ")");
                                subitem2.setProperty("expanded", "N");
                                subitem2.setProperty("translate", "N");
                                subitem2.setProperty("imageclick", "formBuilder.dataSourceItemClick('" + id2 + "');");
                                subitem2.setProperty("textclick", "formBuilder.dataSourceItemClick('" + id2 + "');");
                                subitem2.setProperty("image", IMAGE_DATASOURCE);
                                subitem2.setProperty("prehtml", "<div id=\"formbuilder_di_" + id2 + "\" itemlocked=false collectionid=\"datasources\" itemid=\"" + id2 + "\">");
                                subitem2.setProperty("posthtml", "</div>");
                                subitems.add(subitem2);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Datasource located (" + i + ") with no Id.");
                        }
                        datasourcerootitem.setProperty("items", subitems);
                    } else {
                        datasourcerootitem.setProperty("text", datasourcestext + " (0)");
                        String nodatasourcetext = "No Data Sources";
                        if (tp != null) {
                            nodatasourcetext = tp.translate(nodatasourcetext);
                        }
                        PropertyList subitem3 = new PropertyList();
                        subitem3.setProperty("text", nodatasourcetext);
                        subitem3.setProperty("expanded", "N");
                        subitem3.setProperty("translate", "N");
                        subitem3.setProperty("image", "");
                        subitems.add(subitem3);
                    }
                    datasourcerootitem.setProperty("items", subitems);
                    rootitems.add(datasourcerootitem);
                }
                ArrayList<String> controlledfields = new ArrayList<String>();
                ArrayList<String> lockedfields = new ArrayList<String>();
                String nofieldstext = "No Fields";
                String nolabelstext = "No Labels";
                if (tp != null) {
                    nofieldstext = tp.translate(nofieldstext);
                    nolabelstext = tp.translate(nolabelstext);
                }
                if (buildMode == Mode.MAINTENANCE || propertydeflist == null || propertydeflist.getPropertyDef("fields") != null) {
                    String idprop = "";
                    String typeprop = "";
                    String defaulttype = "";
                    PropertyList fieldrootitem = new PropertyList();
                    String text = "";
                    String collectionid = "";
                    if (buildMode == Mode.MAINTENANCE) {
                        text = "Columns";
                        fieldrootitem.setProperty("expanded", "Y");
                        fieldrootitem.setProperty("translate", "N");
                        fieldrootitem.setProperty("showexpandcollapse", "Y");
                        fieldrootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                        fieldrootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                        collectionid = "columns";
                        idprop = "columnid";
                        typeprop = "mode";
                        defaulttype = "No Mode";
                    } else if (buildMode == Mode.FORM || buildMode == Mode.SIMPLEFORM || buildMode == Mode.ELNFORM) {
                        text = "Fields";
                        fieldrootitem.setProperty("expanded", "Y");
                        fieldrootitem.setProperty("translate", "N");
                        fieldrootitem.setProperty("showexpandcollapse", "Y");
                        fieldrootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                        fieldrootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                        collectionid = "fields";
                        idprop = "fieldid";
                        typeprop = "type";
                        defaulttype = "No Type";
                    }
                    fieldrootitem.setProperty("prehtml", "<div id=\"formbuilder_fi\" itemlocked=false collectionid=\"" + collectionid + "\" itemid=\"\">");
                    fieldrootitem.setProperty("posthtml", "</div>");
                    PropertyListCollection fields = props.getCollection(collectionid);
                    if (tp != null) {
                        defaulttype = tp.translate(defaulttype);
                        text = tp.translate(text);
                    }
                    subitems = new PropertyListCollection();
                    if (fields != null && fields.size() > 0) {
                        fieldrootitem.setProperty("text", text + " (" + fields.size() + ")");
                        for (int i = 0; i < fields.size(); ++i) {
                            PropertyListCollection sections;
                            PropertyList field = fields.getPropertyList(i);
                            String id3 = field.getProperty(idprop, "");
                            field.setProperty(PROPERTY_KEYID1, id3);
                            boolean fieldlocked = field.getProperty("locked", "N").equalsIgnoreCase("Y");
                            if (!(buildMode != Mode.FORM && buildMode != Mode.SIMPLEFORM && buildMode != Mode.ELNFORM || fieldlocked || (sections = props.getCollection("sections")) == null)) {
                                for (int s = 0; s < sections.size(); ++s) {
                                    PropertyList currsec = sections.getPropertyList(s);
                                    PropertyListCollection secfields = currsec.getCollection("fields");
                                    if (secfields == null || secfields.find("fieldid", id3) == null || !currsec.getProperty("locked", "N").equalsIgnoreCase("Y") && !currsec.getProperty("formletlocked", "N").equalsIgnoreCase("Y")) continue;
                                    fieldlocked = true;
                                    lockedfields.add(id3);
                                    break;
                                }
                            }
                            if (id3.length() > 0) {
                                PropertyListCollection valid;
                                int labelcount = 0;
                                PropertyListCollection labels = field.getCollection("labels");
                                if (labels != null) {
                                    labelcount = labels.size();
                                }
                                String type = field.getProperty(typeprop, defaulttype);
                                boolean controlled = field.getProperty("controlled", "N").equalsIgnoreCase("Y");
                                PropertyList subitem4 = new PropertyList();
                                subitem4.setProperty("imageclick", "formBuilder.fieldItemClick('" + id3 + "');");
                                subitem4.setProperty("textclick", "formBuilder.fieldItemClick('" + id3 + "');");
                                subitem4.setProperty("prehtml", "<div id=\"formbuilder_fi_" + id3 + "\" itemlocked=" + fieldlocked + " collectionid=\"" + collectionid + "\" itemid=\"" + id3 + "\">");
                                subitem4.setProperty("posthtml", "</div>");
                                subitem4.setProperty("text", id3 + " (" + type + ") (" + labelcount + ")");
                                subitem4.setProperty("expanded", "N");
                                subitem4.setProperty("translate", "N");
                                if (controlled) {
                                    if (fieldlocked) {
                                        subitem4.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                    } else {
                                        subitem4.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                    }
                                    controlledfields.add(id3);
                                } else if (fieldlocked) {
                                    subitem4.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                } else {
                                    subitem4.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                }
                                PropertyListCollection memsubitems = new PropertyListCollection();
                                if (labelcount > 0) {
                                    for (int k = 0; k < labelcount; ++k) {
                                        PropertyList label = labels.getPropertyList(k);
                                        String labelid = label.getProperty("labelid", "");
                                        if (labelid.length() <= 0) continue;
                                        PropertyList memsubitem = new PropertyList();
                                        memsubitem.setProperty("imageclick", "formBuilder.labelItemClick('" + labelid + "');");
                                        memsubitem.setProperty("textclick", "formBuilder.labelItemClick('" + labelid + "');");
                                        memsubitem.setProperty("prehtml", "<div id=\"formbuilder_li_" + labelid + "\" itemlocked=" + fieldlocked + " collectionid=\"fields.labels\" itemid=\"" + id3 + "." + labelid + "\">");
                                        memsubitem.setProperty("posthtml", "</div>");
                                        memsubitem.setProperty("text", labelid);
                                        memsubitem.setProperty("expanded", "N");
                                        memsubitem.setProperty("translate", "N");
                                        if (fieldlocked) {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font.svg");
                                        } else {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font.svg");
                                        }
                                        memsubitems.add(memsubitem);
                                    }
                                    subitem4.setProperty("items", memsubitems);
                                } else {
                                    PropertyList memsubitem = new PropertyList();
                                    memsubitem.setProperty("text", nolabelstext);
                                    memsubitem.setProperty("expanded", "N");
                                    memsubitem.setProperty("translate", "N");
                                    memsubitem.setProperty("image", "");
                                    memsubitems.add(memsubitem);
                                    subitem4.setProperty("items", memsubitems);
                                }
                                subitems.add(subitem4);
                                if (buildMode != Mode.FORM && buildMode != Mode.SIMPLEFORM && buildMode != Mode.ELNFORM || (valid = field.getCollection("validation")) != null) continue;
                                valid = new PropertyListCollection();
                                field.setProperty("validation", valid);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Field located (" + i + ") with no Id.");
                        }
                    } else {
                        fieldrootitem.setProperty("text", text + " (0)");
                        PropertyList subitem5 = new PropertyList();
                        subitem5.setProperty("text", nofieldstext);
                        subitem5.setProperty("expanded", "N");
                        subitem5.setProperty("translate", "N");
                        subitem5.setProperty("image", "");
                        subitems.add(subitem5);
                    }
                    fieldrootitem.setProperty("items", subitems);
                    rootitems.add(fieldrootitem);
                }
                if (!(buildMode != Mode.FORM && buildMode != Mode.SIMPLEFORM && buildMode != Mode.ELNFORM || propertydeflist != null && propertydeflist.getPropertyDef("groups") == null)) {
                    groupstext = "Groups";
                    if (tp != null) {
                        groupstext = tp.translate(groupstext);
                    }
                    PropertyList grouprootitem = new PropertyList();
                    grouprootitem.setProperty("expanded", "N");
                    grouprootitem.setProperty("translate", "N");
                    grouprootitem.setProperty("showexpandcollapse", "Y");
                    grouprootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                    grouprootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                    grouprootitem.setProperty("prehtml", "<div id=\"formbuilder_gi\" itemlocked=false collectionid=\"groups\" itemid=\"\">");
                    grouprootitem.setProperty("posthtml", "</div>");
                    subitems = new PropertyListCollection();
                    PropertyListCollection groups = props.getCollection("groups");
                    if (groups == null) {
                        groups = new PropertyListCollection();
                        props.setProperty("groups", groups);
                    }
                    if (groups.size() > 0) {
                        grouprootitem.setProperty("text", groupstext + " (" + groups.size() + ")");
                        for (int i = 0; i < groups.size(); ++i) {
                            PropertyList group = groups.getPropertyList(i);
                            id = group.getProperty("groupid", "");
                            if (id.length() > 0) {
                                int mem = 0;
                                PropertyListCollection members = group.getCollection("members");
                                if (members != null) {
                                    mem = members.size();
                                }
                                PropertyList subitem6 = new PropertyList();
                                subitem6.setProperty("imageclick", "formBuilder.groupItemClick('" + id + "');");
                                subitem6.setProperty("textclick", "formBuilder.groupItemClick('" + id + "');");
                                subitem6.setProperty("prehtml", "<div id=\"formbuilder_gi_" + id + "\" itemlocked=false collectionid=\"groups.members\" itemid=\"" + id + "\">");
                                subitem6.setProperty("text", id + " (" + mem + ")");
                                subitem6.setProperty("posthtml", "</div>");
                                subitem6.setProperty("expanded", "N");
                                subitem6.setProperty("translate", "N");
                                subitem6.setProperty("expandedimage", IMAGE_GROUP);
                                subitem6.setProperty("collapsedimage", IMAGE_GROUP);
                                PropertyListCollection memsubitems = new PropertyListCollection();
                                if (mem > 0) {
                                    for (int k = 0; k < mem; ++k) {
                                        PropertyList member = members.getPropertyList(k);
                                        String memid = member.getProperty("fieldid", "");
                                        if (memid.length() <= 0) continue;
                                        boolean memberlocked = false;
                                        if (lockedfields.contains(memid)) {
                                            memberlocked = true;
                                        }
                                        PropertyList memsubitem = new PropertyList();
                                        memsubitem.setProperty("imageclick", "formBuilder.fieldItemClick('" + memid + "');");
                                        memsubitem.setProperty("textclick", "formBuilder.fieldItemClick('" + memid + "');");
                                        memsubitem.setProperty("prehtml", "<div id=\"formbuilder_mi_" + i + "_" + memid + "\" itemlocked=" + memberlocked + " collectionid=\"groups.members\" itemid=\"" + id + "." + memid + "\">");
                                        memsubitem.setProperty("posthtml", "</div>");
                                        memsubitem.setProperty("text", memid);
                                        memsubitem.setProperty("expanded", "N");
                                        memsubitem.setProperty("translate", "N");
                                        if (controlledfields.contains(memid)) {
                                            if (memberlocked) {
                                                memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                            } else {
                                                memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                            }
                                        } else if (memberlocked) {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                        } else {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                        }
                                        memsubitems.add(memsubitem);
                                    }
                                    subitem6.setProperty("items", memsubitems);
                                } else {
                                    PropertyList memsubitem = new PropertyList();
                                    memsubitem.setProperty("text", nofieldstext);
                                    memsubitem.setProperty("expanded", "N");
                                    memsubitem.setProperty("translate", "N");
                                    memsubitem.setProperty("image", "");
                                    memsubitems.add(memsubitem);
                                    subitem6.setProperty("items", memsubitems);
                                }
                                subitems.add(subitem6);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Field located (" + i + ") with no Id.");
                        }
                        grouprootitem.setProperty("items", subitems);
                    } else {
                        grouprootitem.setProperty("text", groupstext + " (0)");
                        String nogroupstext = "No Groups";
                        if (tp != null) {
                            nogroupstext = tp.translate(nogroupstext);
                        }
                        subitem = new PropertyList();
                        subitem.setProperty("text", nogroupstext);
                        subitem.setProperty("expanded", "N");
                        subitem.setProperty("translate", "N");
                        subitem.setProperty("image", "");
                        subitems.add(subitem);
                        grouprootitem.setProperty("items", subitems);
                    }
                    rootitems.add(grouprootitem);
                }
                if (!(buildMode != Mode.FORM && buildMode != Mode.SIMPLEFORM && buildMode != Mode.ELNFORM || propertydeflist != null && propertydeflist.getPropertyDef("sections") == null)) {
                    groupstext = "Sections";
                    if (tp != null) {
                        groupstext = tp.translate(groupstext);
                    }
                    PropertyList sectionrootitem = new PropertyList();
                    sectionrootitem.setProperty("expanded", "N");
                    sectionrootitem.setProperty("translate", "N");
                    sectionrootitem.setProperty("showexpandcollapse", "Y");
                    sectionrootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                    sectionrootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                    sectionrootitem.setProperty("prehtml", "<div id=\"formbuilder_si\" itemlocked=false collectionid=\"sections\" itemid=\"\">");
                    sectionrootitem.setProperty("posthtml", "</div>");
                    subitems = new PropertyListCollection();
                    PropertyListCollection sections = props.getCollection("sections");
                    if (sections == null) {
                        sections = new PropertyListCollection();
                        props.setProperty("sections", sections);
                    }
                    if (sections.size() > 0) {
                        sectionrootitem.setProperty("text", groupstext + " (" + sections.size() + ")");
                        for (int i = 0; i < sections.size(); ++i) {
                            PropertyList section = sections.getPropertyList(i);
                            id = section.getProperty("sectionid", "");
                            if (id.length() > 0) {
                                int mem = 0;
                                PropertyListCollection members = section.getCollection("fields");
                                if (members != null) {
                                    mem = members.size();
                                }
                                boolean sectionlocked = section.getProperty("locked", "N").equalsIgnoreCase("Y") || section.getProperty("formletlocked", "N").equalsIgnoreCase("Y");
                                PropertyList subitem7 = new PropertyList();
                                subitem7.setProperty("imageclick", "formBuilder.sectionItemClick('" + id + "');");
                                subitem7.setProperty("textclick", "formBuilder.sectionItemClick('" + id + "');");
                                subitem7.setProperty("prehtml", "<div id=\"formbuilder_si_" + id + "\" itemlocked=" + sectionlocked + " collectionid=\"sections.fields\" itemid=\"" + id + "\">");
                                subitem7.setProperty("text", id + " (" + mem + ")");
                                subitem7.setProperty("posthtml", "</div>");
                                subitem7.setProperty("expanded", "N");
                                subitem7.setProperty("translate", "N");
                                if (sectionlocked) {
                                    subitem7.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg");
                                    subitem7.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg");
                                } else {
                                    subitem7.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg");
                                    subitem7.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_cell_align.svg");
                                }
                                PropertyListCollection memsubitems = new PropertyListCollection();
                                if (mem > 0) {
                                    for (int k = 0; k < mem; ++k) {
                                        PropertyList member = members.getPropertyList(k);
                                        String memid = member.getProperty("fieldid", "");
                                        if (memid.length() <= 0) continue;
                                        PropertyList memsubitem = new PropertyList();
                                        memsubitem.setProperty("imageclick", "formBuilder.fieldItemClick('" + memid + "');");
                                        memsubitem.setProperty("textclick", "formBuilder.fieldItemClick('" + memid + "');");
                                        memsubitem.setProperty("prehtml", "<div id=\"formbuilder_sfi_" + i + "_" + memid + "\" itemlocked=" + sectionlocked + " collectionid=\"sections.fields\" itemid=\"" + id + "." + memid + "\">");
                                        memsubitem.setProperty("posthtml", "</div>");
                                        memsubitem.setProperty("text", memid);
                                        memsubitem.setProperty("expanded", "N");
                                        memsubitem.setProperty("translate", "N");
                                        if (controlledfields.contains(memid)) {
                                            if (sectionlocked) {
                                                memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                            } else {
                                                memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_font_edit.svg");
                                            }
                                        } else if (sectionlocked) {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                        } else {
                                            memsubitem.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_interface_textbox.svg");
                                        }
                                        memsubitems.add(memsubitem);
                                    }
                                    subitem7.setProperty("items", memsubitems);
                                } else {
                                    PropertyList memsubitem = new PropertyList();
                                    memsubitem.setProperty("text", nofieldstext);
                                    memsubitem.setProperty("expanded", "N");
                                    memsubitem.setProperty("translate", "N");
                                    memsubitem.setProperty("image", "");
                                    memsubitems.add(memsubitem);
                                    subitem7.setProperty("items", memsubitems);
                                }
                                subitems.add(subitem7);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Field located (" + i + ") with no Id.");
                        }
                        sectionrootitem.setProperty("items", subitems);
                    } else {
                        sectionrootitem.setProperty("text", groupstext + " (0)");
                        String nosectionstext = "No Sections";
                        if (tp != null) {
                            nosectionstext = tp.translate(nosectionstext);
                        }
                        subitem = new PropertyList();
                        subitem.setProperty("text", nosectionstext);
                        subitem.setProperty("expanded", "N");
                        subitem.setProperty("translate", "N");
                        subitem.setProperty("image", "");
                        subitems.add(subitem);
                        sectionrootitem.setProperty("items", subitems);
                    }
                    rootitems.add(sectionrootitem);
                }
                if (propertydeflist == null || propertydeflist.getPropertyDef("elements") != null) {
                    String elementstext = "Elements";
                    if (tp != null) {
                        elementstext = tp.translate(elementstext);
                    }
                    PropertyList elementrootitem = new PropertyList();
                    elementrootitem.setProperty("expanded", "N");
                    elementrootitem.setProperty("translate", "N");
                    elementrootitem.setProperty("showexpandcollapse", "Y");
                    elementrootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                    elementrootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                    elementrootitem.setProperty("prehtml", "<div id=\"formbuilder_ei_\" itemlocked=false collectionid=\"elements\" itemid=\"\">");
                    elementrootitem.setProperty("posthtml", "</div>");
                    subitems = new PropertyListCollection();
                    PropertyListCollection elements = props.getCollection("elements");
                    if (elements == null) {
                        elements = new PropertyListCollection();
                        props.setProperty("elements", elements);
                    }
                    if (elements.size() > 0) {
                        elementrootitem.setProperty("text", elementstext + " (" + elements.size() + ")");
                        for (int i = 0; i < elements.size(); ++i) {
                            PropertyList element = elements.getPropertyList(i);
                            id = element.getProperty("elementid", "");
                            if (id.length() > 0) {
                                title = element.getProperty("type", "");
                                if (title.length() == 0) {
                                    title = "No Type";
                                    if (tp != null) {
                                        title = tp.translate(title);
                                    }
                                }
                                PropertyList subitem8 = new PropertyList();
                                subitem8.setProperty("text", id + " (" + title + ")");
                                subitem8.setProperty("expanded", "N");
                                subitem8.setProperty("translate", "N");
                                subitem8.setProperty("imageclick", "formBuilder.elementItemClick('" + id + "');");
                                subitem8.setProperty("textclick", "formBuilder.elementItemClick('" + id + "');");
                                subitem8.setProperty("image", IMAGE_ELEMENT);
                                subitem8.setProperty("prehtml", "<div id=\"formbuilder_ei_" + id + "\" itemlocked=false collectionid=\"elements\" itemid=\"" + id + "\">");
                                subitem8.setProperty("posthtml", "</div>");
                                subitems.add(subitem8);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Element located (" + i + ") with no Id.");
                        }
                        elementrootitem.setProperty("items", subitems);
                    } else {
                        elementrootitem.setProperty("text", elementstext + " (0)");
                        String noelementstext = "No Elements";
                        if (tp != null) {
                            noelementstext = tp.translate(noelementstext);
                        }
                        subitem = new PropertyList();
                        subitem.setProperty("text", noelementstext);
                        subitem.setProperty("expanded", "N");
                        subitem.setProperty("translate", "N");
                        subitem.setProperty("image", "");
                        subitems.add(subitem);
                    }
                    elementrootitem.setProperty("items", subitems);
                    rootitems.add(elementrootitem);
                }
                if (!(isFormlet || propertydeflist != null && propertydeflist.getPropertyDef("pages") == null)) {
                    String pagestext = "Pages";
                    if (tp != null) {
                        pagestext = tp.translate(pagestext);
                    }
                    PropertyList pagerootitem = new PropertyList();
                    pagerootitem.setProperty("expanded", "N");
                    pagerootitem.setProperty("translate", "N");
                    pagerootitem.setProperty("showexpandcollapse", "Y");
                    pagerootitem.setProperty("expandedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_open.svg");
                    pagerootitem.setProperty("collapsedimage", "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg");
                    pagerootitem.setProperty("prehtml", "<div id=\"formbuilder_pi_\" itemlocked=false collectionid=\"pages\" itemid=\"\">");
                    pagerootitem.setProperty("posthtml", "</div>");
                    subitems = new PropertyListCollection();
                    PropertyListCollection pages = props.getCollection("pages");
                    if (pages == null) {
                        pages = new PropertyListCollection();
                        props.setProperty("pages", pages);
                        PropertyList page = new PropertyList();
                        page.setProperty("pageid", DEFAULT_PAGE);
                        pages.add(page);
                    }
                    if (pages.size() > 0) {
                        pagerootitem.setProperty("text", pagestext + " (" + pages.size() + ")");
                        for (int i = 0; i < pages.size(); ++i) {
                            PropertyList page = pages.getPropertyList(i);
                            id = page.getProperty("pageid", "");
                            if (id.length() > 0) {
                                title = page.getProperty("title", "");
                                if (title.length() == 0) {
                                    title = "No Title";
                                    if (tp != null) {
                                        title = tp.translate(title);
                                    }
                                }
                                String mode = page.getProperty("mode", "page");
                                PropertyList subitem9 = new PropertyList();
                                subitem9.setProperty("text", id + " (" + title + ")");
                                subitem9.setProperty("expanded", "N");
                                subitem9.setProperty("translate", "N");
                                subitem9.setProperty("imageclick", "formBuilder.pageItemClick('" + id + "');");
                                subitem9.setProperty("textclick", "formBuilder.pageItemClick('" + id + "');");
                                if (mode.equalsIgnoreCase("screen")) {
                                    subitem9.setProperty("image", IMAGE_SCREEN);
                                } else {
                                    subitem9.setProperty("image", IMAGE_PAGE);
                                }
                                subitem9.setProperty("prehtml", "<div id=\"formbuilder_pi_" + id + "\" itemlocked=false collectionid=\"pages\" itemid=\"" + id + "\">");
                                subitem9.setProperty("posthtml", "</div>");
                                subitems.add(subitem9);
                                continue;
                            }
                            Logger.logWarn("getObjectsHtml - Field located (" + i + ") with no Id.");
                        }
                        pagerootitem.setProperty("items", subitems);
                    }
                    rootitems.add(pagerootitem);
                }
                tree.setProperty("rootitems", rootitems);
                Tree t = new Tree(connectionId, tp);
                if (userConfig != null) {
                    t.setUserConfig(userConfig);
                }
                t.setId("object_fields");
                t.setElementProperties(tree);
                sb.append(t.getHtml());
            } else {
                String msg = "Form properties not correct.";
                if (tp != null) {
                    msg = tp.translate(msg);
                }
                sb.append(msg);
            }
        } else {
            String msg = "No connection Id provided.";
            if (tp != null) {
                msg = tp.translate(msg);
            }
            sb.append(msg);
        }
        return sb.toString();
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/formbuilder.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/formbuilder_propertychange.js\"></script>");
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<script type=\"text/javascript\">");
        html.append("var __t = formBuilder.contextMenu.texts;");
        html.append("__t.collapse = '").append(tp.translate("Collapse")).append("';");
        html.append("__t.expand = '").append(tp.translate("Expand")).append("';");
        html.append("__t.addDataSource = '").append(tp.translate("Add Data Source")).append("';");
        html.append("__t.removeDataSource = '").append(tp.translate("Remove Data Source")).append("';");
        html.append("__t.editLabel = '").append(tp.translate("Edit Label")).append("';");
        html.append("__t.removeLabel = '").append(tp.translate("Remove Label")).append("';");
        html.append("__t.addGroup = '").append(tp.translate("Add Group")).append("';");
        html.append("__t.removeSection = '").append(tp.translate("Remove Section")).append("';");
        html.append("__t.addToGroup = '").append(tp.translate("Add to Group")).append("';");
        html.append("__t.removeFromGroup = '").append(tp.translate("Remove from Group")).append("';");
        html.append("__t.addPage = '").append(tp.translate("Add Page")).append("';");
        html.append("__t.removeGroup = '").append(tp.translate("Remove Group")).append("';");
        html.append("__t.editPage = '").append(tp.translate("Edit Page")).append("';");
        html.append("__t.showOverflow = '").append(tp.translate("Show Overflow")).append("';");
        html.append("__t.hideOverflow = '").append(tp.translate("Hide Overflow")).append("';");
        html.append("__t.removePage = '").append(tp.translate("Remove Page")).append("';");
        html.append("__t.saveAsField = '").append(tp.translate("Save As Field")).append("';");
        html.append("__t.categorized = '").append(tp.translate("Categorized")).append("';");
        html.append("__t.alphabetical = '").append(tp.translate("Alphabetical")).append("';");
        html.append("__t.usedUnused = '").append(tp.translate("Used & Unused")).append("';");
        html.append("__t.editorType = '").append(tp.translate("Editor Type")).append("';");
        html.append("__t.noGrouping = '").append(tp.translate("No Grouping")).append("';");
        html.append("</script>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String keyid1, String keyid2, Mode mode, boolean xmlMode, boolean formObjectMode, String fieldIds, String callback, String defaultNoProps, boolean isFormlet, boolean viewOnly, boolean embedded) {
        StringBuffer html = new StringBuffer();
        boolean drafts = false;
        if (keyid1.length() > 0) {
            try {
                PropertyList actionprops = new PropertyList();
                actionprops.put("mode", "list");
                if (isFormlet) {
                    actionprops.put("sdcid", "LV_Formlet");
                    actionprops.put(PROPERTY_KEYID1, keyid1);
                } else {
                    actionprops.put("sdcid", "LV_Form");
                    actionprops.put(PROPERTY_KEYID1, keyid1);
                    actionprops.put(PROPERTY_KEYID2, keyid2);
                }
                actionprops.put("tempid", "draftproperties;draftlayout");
                this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.sdi.SDITemp", actionprops, false);
                String count = actionprops.getProperty("count", "0");
                if (count.length() > 0 && count.equals("2")) {
                    drafts = true;
                }
            }
            catch (Exception e) {
                this.logger.debug("Could not find drafts");
            }
        }
        html.append("<script type=\"text/javascript\">");
        html.append("formBuilder.properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        html.append("formBuilder.viewonly=").append(viewOnly).append(";");
        html.append("formBuilder.embedded=").append(embedded).append(";");
        html.append("formBuilder.sdcid='").append(isFormlet ? "LV_Formlet" : "LV_Form").append("';");
        html.append("formBuilder.keyid1='").append(keyid1).append("';");
        html.append("formBuilder.keyid2='").append(keyid2).append("';");
        html.append("formBuilder.isFormlet=").append(isFormlet).append(";");
        html.append("formBuilder.buildMode='").append(mode.toString()).append("';");
        html.append("formBuilder.xmlMode=").append(xmlMode).append(";");
        html.append("formBuilder.formObjectMode=").append(formObjectMode).append(";");
        html.append("formBuilder.fieldIds='").append(fieldIds).append("';");
        html.append("formBuilder.callback='").append(callback).append("';");
        html.append("formBuilder.draftsAvailable=").append(drafts).append(";");
        html.append("formBuilder.defaultNoProps='").append(defaultNoProps).append("';");
        html.append("formBuilder.propertyGrouping='").append(this.userConfig != null ? this.userConfig.getProperty("propertybuilder_groupby", "cat") : "cat").append("';");
        html.append("formBuilder.restoreMaximise=").append((this.userConfig != null ? this.userConfig.getProperty("formbuilder_maximised", "N") : "N").equalsIgnoreCase("Y")).append(";");
        html.append("sapphire.ui.resize.setResizeEvent(form_objects_row,formBuilder.resizeObjectsEnd);");
        html.append("sapphire.ui.resize.setResizeEvent(form_leftbar,formBuilder.resizeWidthEnd);");
        html.append("htmlEditor.postInit = formBuilder.doRichTextPostLoad;");
        html.append("</script>");
        return html;
    }

    private String getRTEHtml(String formlayout, Mode buildMode, boolean isFormlet, boolean viewOnly, boolean embedded, boolean devMode) {
        HTMLEditorControl.EditorType editorType = isFormlet ? HTMLEditorControl.EditorType.FORMLET : (buildMode == Mode.SIMPLEFORM ? HTMLEditorControl.EditorType.SIMPLEFORM : (buildMode == Mode.ELNFORM ? HTMLEditorControl.EditorType.ELNFORM : HTMLEditorControl.EditorType.FORM));
        HTMLEditorControl htmlEditorControl = new HTMLEditorControl(RICHTEXT_ELEMENT, editorType, this.logger);
        htmlEditorControl.setRtl(this.getConnectionProcessor().getSapphireConnection().isRtl());
        htmlEditorControl.setHeight("100%");
        htmlEditorControl.setDevMode(devMode);
        if (this.phrasetype.length() > 0) {
            htmlEditorControl.setPhraseType(this.phrasetype);
        }
        if (this.phraselookup.length() > 0) {
            htmlEditorControl.setPhraseLookup(this.phraselookup);
        }
        if (viewOnly) {
            htmlEditorControl.setViewOnly(true);
        }
        if (this.keyid1 != null && this.keyid1.length() > 0) {
            htmlEditorControl.setSDI(isFormlet ? "LV_Formlet" : "LV_Form", this.keyid1, this.keyid2 != null && this.keyid2.length() > 0 ? this.keyid2 : "", "");
        }
        htmlEditorControl.setCanUpload(true);
        if (isFormlet) {
            htmlEditorControl.getEditor().setDefaultPage(HTMLEditorControl.PageMode.GROWABLE);
            htmlEditorControl.getEditor().setResizablePage(true);
        } else {
            htmlEditorControl.getEditor().setDefaultPage(HTMLEditorControl.PageMode.LETTERPORTRAIT);
        }
        StringBuffer formcontent = new StringBuffer(formlayout);
        HTMLEditorControl.processImages(formcontent, false, this.getConnectionId());
        htmlEditorControl.setContent(formcontent.toString());
        htmlEditorControl.setEvent("selectionchange", HTMLEditorControl.Events.SELECTIONCLEARED, "formBuilder.onRichTextSelectClear");
        htmlEditorControl.setEvent("select", HTMLEditorControl.Events.NODESELECTED, "formBuilder.onRichTextSelect");
        htmlEditorControl.setEvent("change", HTMLEditorControl.Events.CHANGE, "formBuilder.onRichTextChange");
        htmlEditorControl.setEvent("contentadded", HTMLEditorControl.Events.CONTENTADDED, "formBuilder.onRichTextSetContent");
        htmlEditorControl.setEvent("click", HTMLEditorControl.Events.CLICK, "formBuilder.onRichTextClick");
        StringBuffer content = new StringBuffer();
        content.append(htmlEditorControl.getIncludesHTML((HttpServletRequest)this.pageContext.getRequest()));
        content.append(htmlEditorControl.getHtml());
        content.append("<script type=\"text/javascript\">");
        content.append(htmlEditorControl.getScript());
        content.append("htmlEditor.data['").append(RICHTEXT_ELEMENT).append("'].imagetools = 'lv_formelement';");
        content.append("htmlEditor.data['").append(RICHTEXT_ELEMENT).append("'].tabletools = 'lv_tablesection lv_rowsection';");
        content.append("sapphire.events.attachEvent(window,'load',function(){").append(htmlEditorControl.getInitScript("formBuilder.doRichTextLoad")).append("});");
        content.append("</script>");
        return content.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.formprops != null) {
            html.append(this.getScriptAndStyle());
            if (this.browser == null || this.browser.isIE() || this.browser.isWebkit()) {
                html.append(this.getDivsHtml(this.viewonly));
                html.append("<table border=\"0\" id=\"form_layout_table\" cellpadding=\"0\" cellspacing=\"0\" style=\"display:block;width:100%;height:100%;table-layout:auto;\">");
                html.append("<tbody>");
                html.append("<tr>");
                html.append(this.getLeftBarStart(this.showobjects, this.viewonly, this.embedded));
                html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar\">");
                html.append("<tbody>");
                boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
                html.append("<tr class=\"layout_sidebar_tab_row\" ").append("").append(">");
                html.append("<td align=\"").append(rtl ? "right" : "left").append("\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" valign=\"middle\" nowrap>");
                html.append(this.getTranslationProcessor().translate("Objects"));
                html.append("</td>");
                html.append("<td align=").append(rtl ? "left" : "right").append("\" valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
                Image i = new Image(this.pageContext);
                i.setImageId(rtl ? "FlatBlackDoubleChevronRight" : "FlatBlackDoubleChevronLeft");
                i.setDimensions(9, 9);
                html.append("<div style=\"display:block;cursor: pointer;padding-right:2px;").append(this.getConnectionProcessor().getSapphireConnection().isRtl() ? "text-align:left;" : "").append("\" onclick=\"formBuilder.collapseLeftBar();\">");
                html.append(i.getHtml());
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"form_merge\">");
                html.append("<td colspan=\"2\">");
                html.append("</td>");
                html.append("</tr>");
                String rowheight = this.userConfig.getProperty("formbuilder_objects" + (this.viewonly ? "VO" : ""), "200px");
                if (!rowheight.endsWith("px")) {
                    rowheight = rowheight + "px";
                }
                html.append("<tr id=\"form_objects_row\" style=\"height:").append(rowheight).append(rowheight.endsWith("px") ? "" : "px").append(";\">");
                html.append("<td colspan=\"2\" class=\"form_bar_parentcell\">");
                html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
                html.append("<tbody>");
                html.append("<tr>");
                html.append("<td valign=\"top\" align=\"left\" class=\"form_bar_childcell\" id=\"form_objects_content_cell\" nowrap style=\"height:100%;position:relative;\">");
                html.append("<div align=\"left\" id=\"form_objects_content\" onscroll=\"formBuilder.doScroll()\" style=\"position:absolute;overflow-y:auto;overflow-x:auto;width:auto;height:auto;top:0;left:0;bottom:0;right:0;\">");
                html.append(FormBuilder.getObjectsHtml(this.formprops, this.mode, this.isFormlet, this.getConnectionId(), this.getTranslationProcessor(), (HttpServletRequest)this.pageContext.getRequest()));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"layout_sidebar_tab_row\" style=\"cursor:n-resize;").append("").append("\" onmousedown=\"if(event.").append(this.browser.isIE() ? "srcElement" : "target").append("!=groupingbutton && !groupingbutton.contains(event.").append(this.browser.isIE() ? "srcElement" : "target").append("))sapphire.ui.resize.start(form_objects_row,[],'s',0,60);\">");
                html.append("<td valign=\"middle\" class=\"layout_sidebar_tab layout_sidebar_tab_back\"  nowrap >");
                html.append(this.getTranslationProcessor().translate("Properties"));
                html.append("</td>");
                html.append("<td align=left valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
                html.append("<div id=\"groupingbutton\" onclick=\"formBuilder.showPropertyGrouping()\" class=\"form_groupingbutton\" onmouseenter=\"this.style.backgroundColor='#F9F9F5';this.style.border='solid 1px #CECEC3';\" onmouseleave=\"this.style.backgroundColor='';this.style.border='';\"><img src=\"WEB-CORE/imageref/flat/16/flat_black_list_hidden.svg\"><img src=\"WEB-CORE/elements/richtext/images/dropdownarrow.gif\"></div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"form_merge\">");
                html.append("<td colspan=\"2\">");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr id=\"form_properties_row\" style=\"\">");
                html.append("<td colspan=\"2\" class=\"form_bar_parentcell\">");
                html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
                html.append("<tbody>");
                html.append("<tr >");
                html.append("<td  valign=\"top\" align=\"left\" class=\"form_bar_childcell\" style=\"position:relative;height:100%;\" id=\"form_properties_content_cell\" nowrap>");
                html.append("<div align=\"left\" id=\"form_properties_content\" onscroll=\"formBuilder.doScroll()\" style=\"overflow-y:auto;overflow-x:auto;width:auto;height:auto;top:0;left:0;right:0;bottom:0;position:absolute;\">");
                PropertyBuilder.clearSession(this.pageContext.getSession());
                String defaultNoProps = FormBuilder.getPropertiesHtml(null, null, null, null, null, null, this.formprops, this.viewonly, this.mode, this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.pageContext.getSession(), ProcessingUtil.createBindingsMap(null, this.getQueryProcessor(), this.getSDCProcessor(), null, null));
                html.append(defaultNoProps);
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append(this.getLeftBarEnd());
                html.append("<td colspan=\"2\" id=\"form_centerarea\">");
                html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
                html.append("<tbody>");
                html.append("<tr style=\"height:auto;\">");
                html.append("<td colspan=\"2\">");
                html.append("<div id=\"form_richtext_content\"  style=\"overflow-y:hidden;overflow-x:hidden;width:100%;height:100%;\">");
                html.append(this.getRTEHtml(this.formlayout, this.mode, this.isFormlet, this.viewonly, this.embedded, this.devMode));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                if (this.showbuttons) {
                    html.append("<tr style=\"height:32px;\">");
                    html.append("<td valign=\"middle\" align=\"").append(rtl ? "right" : "left").append("\" class=\"form_buttons\" nowrap>");
                    html.append("<div id=\"form_devbuttons_content\"  style=\"overflow:").append(this.browser.isIE() ? "auto" : "hidden").append(";width:100%;height:100%;\">");
                    html.append(this.getDevButtonsHtml(this.devMode));
                    html.append("</div>");
                    html.append("</td>");
                    html.append("<td valign=\"middle\" align=\"").append(rtl ? "left" : "right").append("\" class=\"form_buttons\" nowrap>");
                    html.append("<div id=\"form_buttons_content\"  style=\"overflow:").append(this.browser.isIE() ? "auto" : "hidden").append(";width:100%;height:100%;\">");
                    html.append(this.getButtonsHtml(this.viewonly));
                    html.append("</div>");
                    html.append("</td>");
                    html.append("</tr>");
                }
                html.append("</tbody>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append("<div id=\"form_movementarrows\" __itemid=\"\" __itemtype=\"\" style=\"display:none;\">").append("<input style=\"cursor: pointer;margin:1 1 1 1;\" type=\"image\" onmouseleave=\"formBuilder.updown.buttonLeave(this)\" onmouseenter=\"formBuilder.updown.buttonEnter(this)\" onclick=\"formBuilder.updown.buttonClick(this,true)\" src=\"WEB-CORE/elements/richtext/images/moveup.gif\" title=\"").append(this.getTranslationProcessor().translate("Move item up")).append("\">").append("&nbsp;").append("<input  style=\"cursor: pointer;margin:1 1 1 1;\" type=\"image\" onmouseleave=\"formBuilder.updown.buttonLeave(this)\" onmouseenter=\"formBuilder.updown.buttonEnter(this)\" onclick=\"formBuilder.updown.buttonClick(this,false)\" src=\"WEB-CORE/elements/richtext/images/movedown.gif\" title=\"").append(this.getTranslationProcessor().translate("Move item down")).append("\">").append("</div>");
                html.append(this.getEndScript(this.formprops, this.keyid1, this.keyid2, this.mode, this.xmlMode, this.formObjectMode, this.fieldIds, this.callbackFunc, defaultNoProps, this.isFormlet, this.viewonly, this.embedded));
            } else {
                html.append(this.getRTEHtml(this.formlayout, this.mode, this.isFormlet, true, this.embedded, this.devMode));
            }
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load editor.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(String fieldId, String groupId, String sectionid, String pageId, PropertyList props, Mode buildMode, String connectionId, TranslationProcessor tp, boolean primaryCall, HttpSession session, HashMap bindingMap) {
        return FormBuilder.getPropertiesHtml(fieldId, groupId, sectionid, pageId, "", props, buildMode, connectionId, tp, primaryCall, session, bindingMap);
    }

    public static String getPropertiesHtml(String fieldId, String groupId, String sectionid, String pageId, String labelId, PropertyList props, Mode buildMode, String connectionId, TranslationProcessor tp, boolean primaryCall, HttpSession session, HashMap bindingMap) {
        return FormBuilder.getPropertiesHtml("", fieldId, groupId, sectionid, pageId, labelId, props, false, buildMode, connectionId, null, tp, session, bindingMap);
    }

    public static String getPropertiesHtml(String datasourceId, String fieldId, String groupId, String sectionid, String pageId, String labelId, PropertyList props, boolean viewOnly, Mode buildMode, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        return FormBuilder.getPropertiesHtml("", fieldId, groupId, sectionid, pageId, labelId, "", props, false, buildMode, connectionId, null, tp, session, bindingMap);
    }

    private static void ProcessPropertyListMergingStage1(PropertyList found, PropertyList merged) {
        for (String prop : found.keySet()) {
            Object obV = found.get(prop);
            if (obV instanceof String) {
                String value = found.getProperty(prop, "");
                if (!merged.containsKey(prop)) {
                    merged.put(prop, new ArrayList());
                }
                ((ArrayList)merged.get(prop)).add(value);
                continue;
            }
            if (!(obV instanceof PropertyList)) continue;
            if (!merged.containsKey(prop)) {
                merged.setProperty(prop, new PropertyList());
            }
            FormBuilder.ProcessPropertyListMergingStage1((PropertyList)obV, merged.getPropertyList(prop));
        }
    }

    private static void ProcessPropertyListMergingStage2(PropertyList merged, String[] ids) {
        for (String prop : merged.keySet()) {
            Object obV = merged.get(prop);
            if (obV instanceof ArrayList) {
                ArrayList vals = (ArrayList)obV;
                if (vals.size() == ids.length) {
                    String prev = "";
                    boolean ok = true;
                    for (int k = 0; k < vals.size(); ++k) {
                        String item = vals.get(k).toString();
                        if (!(prev.length() == 0 || item.length() > 0 && item.equalsIgnoreCase(prev))) {
                            ok = false;
                            break;
                        }
                        prev = item;
                    }
                    if (ok) {
                        merged.setProperty(prop, prev);
                        continue;
                    }
                    merged.setProperty(prop, "");
                    continue;
                }
                merged.setProperty(prop, "");
                continue;
            }
            if (obV instanceof PropertyList) {
                FormBuilder.ProcessPropertyListMergingStage2((PropertyList)obV, ids);
                continue;
            }
            merged.setProperty(prop, "");
        }
    }

    private static PropertyList getProperties(PropertyListCollection collection, String idProp, String id) {
        String[] ids = id.split(";");
        if (ids.length == 1) {
            return collection.find(idProp, ids[0]);
        }
        PropertyList merged = new PropertyList();
        for (int i = 0; i < ids.length; ++i) {
            PropertyList found = collection.find(idProp, ids[i]);
            if (found == null) continue;
            FormBuilder.ProcessPropertyListMergingStage1(found, merged);
        }
        FormBuilder.ProcessPropertyListMergingStage2(merged, ids);
        merged.setProperty(idProp, id);
        collection.add(merged);
        return merged;
    }

    public static String getPropertiesHtml(String datasourceId, String fieldId, String groupId, String sectionid, String pageId, String labelId, String elementId, PropertyList props, boolean viewOnly, Mode buildMode, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        StringBuffer sb = new StringBuffer();
        if (connectionId != null && connectionId.length() > 0) {
            Object defob;
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            if ((defob = session.getAttribute(buildMode == Mode.SIMPLEFORM ? "formbuilder_formobjects_simple_pd" : (buildMode == Mode.ELNFORM ? "formbuilder_formobjects_simple_pd" : CACHE_FORMOBJECTS))) != null && defob instanceof PropertyDefinitionList) {
                PropertyDefinitionList propertydeflist = (PropertyDefinitionList)defob;
                if (props != null && (props.containsKey("fields") || props.containsKey("columns"))) {
                    if (datasourceId != null && datasourceId.length() > 0) {
                        PropertyBuilder.renderProperties(0, datasourceId, 5, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                    } else if (elementId != null && elementId.length() > 0) {
                        PropertyBuilder.renderProperties(0, elementId, 6, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                    } else if (fieldId != null && fieldId.length() > 0 && (labelId == null || labelId.length() == 0)) {
                        if (props.getCollection("fields") != null) {
                            String[] fieldids = fieldId.split(";");
                            PropertyList field = FormBuilder.getProperties(props.getCollection("fields"), "fieldid", fieldId);
                            if (field != null) {
                                field.setProperty(PROPERTY_KEYID1, field.getProperty("fieldid", fieldId));
                                boolean fieldlocked = viewOnly;
                                if (!fieldlocked) {
                                    PropertyList curfield;
                                    block0: for (int i = 0; i < fieldids.length && !(fieldlocked = (curfield = FormBuilder.getProperties(props.getCollection("fields"), "fieldid", fieldids[i])).getProperty("locked", "N").equalsIgnoreCase("Y")); ++i) {
                                        PropertyListCollection sections = props.getCollection("sections");
                                        if (sections == null) continue;
                                        for (int s = 0; s < sections.size(); ++s) {
                                            PropertyList currsec = sections.getPropertyList(s);
                                            PropertyListCollection secfields = currsec.getCollection("fields");
                                            if (secfields == null || secfields.find("fieldid", fieldids[i]) == null || !currsec.getProperty("locked", "N").equalsIgnoreCase("Y") && !currsec.getProperty("formletlocked", "N").equalsIgnoreCase("Y")) continue;
                                            fieldlocked = true;
                                            break block0;
                                        }
                                    }
                                }
                                PropertyBuilder.renderProperties(0, field.getProperty("fieldid", fieldId), 0, propertydeflist, props, fieldlocked, sb, connectionId, userConfig, tp, session, bindingMap);
                            } else {
                                PropertyBuilder.addPropertyMsg("No field properties found", sb, tp);
                            }
                        } else {
                            PropertyBuilder.addPropertyMsg("No fields collection found", sb, tp);
                        }
                    } else if (groupId != null && groupId.length() > 0) {
                        if (props.getCollection("groups") != null) {
                            PropertyBuilder.renderProperties(0, groupId, 1, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                        } else {
                            PropertyBuilder.addPropertyMsg("No groups collection found", sb, tp);
                        }
                    } else if (sectionid != null && sectionid.length() > 0) {
                        PropertyBuilder.renderProperties(0, sectionid, 2, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                    } else if (pageId != null && pageId.length() > 0) {
                        PropertyList page = FormBuilder.getProperties(props.getCollection("pages"), "pageid", pageId);
                        PropertyBuilder.renderProperties(0, pageId, 4, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                    } else if (labelId != null && labelId.length() > 0 && fieldId != null && fieldId.length() > 0) {
                        PropertyDefinition fieldsdef = propertydeflist.getPropertyDef("fields");
                        if (fieldsdef != null && fieldsdef.getPropertyDefinitionList() != null && fieldsdef.getPropertyDefinitionList().getPropertyDef("labels") != null && fieldsdef.getPropertyDefinitionList().getPropertyDef("labels").getPropertyDefinitionList() != null) {
                            PropertyList field = props.getCollection("fields").find("fieldid", fieldId);
                            if (field != null) {
                                if (field.containsKey("labels")) {
                                    PropertyListCollection labels = field.getCollection("labels");
                                    if (labels != null && labels.size() > 0) {
                                        PropertyList label = labels.find("labelid", labelId);
                                        if (label != null) {
                                            PropertyBuilder.renderProperties(0, fieldId + "." + labelId, 3, fieldsdef.getPropertyDefinitionList(), field, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
                                        } else {
                                            PropertyBuilder.addPropertyMsg("No label properties found", sb, tp);
                                        }
                                    } else {
                                        PropertyBuilder.addPropertyMsg("No label properties found", sb, tp);
                                    }
                                } else {
                                    PropertyBuilder.addPropertyMsg("Incorrect label properties found", sb, tp);
                                }
                            } else {
                                PropertyBuilder.addPropertyMsg("No labels for field found", sb, tp);
                            }
                        } else {
                            PropertyBuilder.addPropertyMsg("No label property definition found", sb, tp);
                        }
                    } else {
                        PropertyBuilder.addPropertyMsg("No Properties", sb, tp);
                    }
                } else {
                    PropertyBuilder.addPropertyMsg("Form properties not correct", sb, tp);
                }
            } else {
                PropertyBuilder.addPropertyMsg("Property definition not provided", sb, tp);
            }
        } else {
            PropertyBuilder.addPropertyMsg("No connection Id provided", sb, tp);
        }
        return sb.toString();
    }

    private StringBuffer getDivsHtml(boolean viewOnly) {
        int left;
        StringBuffer sb = new StringBuffer();
        String width = this.userConfig.getProperty("formbuilder_width" + (viewOnly ? "VO" : ""), "200");
        try {
            left = Integer.parseInt(width) - 5;
        }
        catch (Exception e) {
            left = 195;
        }
        boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
        if (this.userConfig.getProperty("formbuilder_left" + (viewOnly ? "VO" : ""), viewOnly ? "N" : "Y").equalsIgnoreCase("Y")) {
            sb.append("<div id=\"form_leftbar_expand\" style=\"display:none;\" onclick=\"formBuilder.expandLeftBar();\">");
            Image i = new Image(this.pageContext);
            i.setImageId(rtl ? "FlatBlackDoubleChevronLeft" : "FlatBlackDoubleChevronRight");
            i.setDimensions(9, 9);
            sb.append(i.getHtml());
            sb.append("</div>");
            sb.append("<div id=\"form_leftbar_resize\" style=\"display:block;left:").append(left).append("px;\" onmousedown=\"sapphire.ui.resize.start(form_leftbar, [], 'e',150);\"></div>");
        } else {
            sb.append("<div id=\"form_leftbar_expand\" style=\"display:block;\" onclick=\"formBuilder.expandLeftBar();\">");
            Image i = new Image(this.pageContext);
            i.setImageId(rtl ? "FlatBlackDoubleChevronLeft" : "FlatBlackDoubleChevronRight");
            i.setDimensions(9, 9);
            sb.append(i.getHtml());
            sb.append("</div>");
            sb.append("<div id=\"form_leftbar_resize\" style=\"display:none;left:").append(left).append("px;\" onmousedown=\"sapphire.ui.resize.start(form_leftbar,[],'e',150);\"></div>");
        }
        return sb;
    }

    private StringBuffer getLeftBarStart(boolean showBar, boolean viewOnly, boolean embedded) {
        int width;
        String twidth = this.userConfig.getProperty("formbuilder_width" + (viewOnly ? "VO" : ""), "200");
        try {
            width = Integer.parseInt(twidth);
        }
        catch (Exception e) {
            width = 200;
        }
        StringBuffer sb = new StringBuffer();
        if (!showBar) {
            sb.append("<td  id=\"form_leftbar_collapsed\" style=\"display:none;").append(embedded ? "background-image:none;" : "").append("\"></td>");
            sb.append("<td id=\"form_leftbar\" class=\"form_leftbar\" style=\"display:none;width:").append(width).append("px;").append("").append("\">");
        } else if (this.userConfig.getProperty("formbuilder_left" + (viewOnly ? "VO" : ""), viewOnly ? "N" : "Y").equalsIgnoreCase("Y")) {
            sb.append("<td  id=\"form_leftbar_collapsed\" style=\"display:none;").append(embedded ? "background-image:none;" : "").append("\"></td>");
            sb.append("<td id=\"form_leftbar\" class=\"form_leftbar\" style=\"display:table-cell;width:").append(width).append("px;").append("").append("\">");
        } else {
            sb.append("<td  id=\"form_leftbar_collapsed\" style=\"display:table-cell;").append(embedded ? "background-image:none;" : "").append("\"></td>");
            sb.append("<td id=\"form_leftbar\" class=\"form_leftbar\" style=\"display:none;width:").append(width).append("px;").append("").append("\">");
        }
        return sb;
    }

    private String getButtonsHtml(boolean viewOnly) {
        Button but;
        StringBuffer sb = new StringBuffer();
        if (!viewOnly) {
            but = new Button(this.pageContext);
            but.setText(this.getTranslationProcessor().translate("OK"));
            but.setTip(this.getTranslationProcessor().translate("OK and return"));
            but.setId("formbuilder_btn_ok");
            but.setAction("formBuilder.buttons.doOK()");
            but.setWidth("100");
            sb.append(but.getHtml());
            sb.append("&nbsp;&nbsp;");
        }
        but = new Button(this.pageContext);
        if (viewOnly) {
            but.setText(this.getTranslationProcessor().translate("Close"));
            but.setTip(this.getTranslationProcessor().translate("Close"));
        } else {
            but.setText(this.getTranslationProcessor().translate("Cancel"));
            but.setTip(this.getTranslationProcessor().translate("Cancel and return"));
        }
        but.setId("formbuilder_btn_cancel");
        but.setAction("formBuilder.buttons.doCancel()");
        but.setWidth("100");
        sb.append(but.getHtml());
        return sb.toString();
    }

    private String getDevButtonsHtml(boolean devMode) {
        StringBuffer sb = new StringBuffer("&nbsp;");
        if (devMode) {
            Button but = new Button(this.pageContext);
            but.setTip(this.getTranslationProcessor().translate("Show source code (only available in development mode)"));
            but.setId("formbuilder_btn_source");
            but.setImg("WEB-CORE/elements/richtext/images/gif/ToolbarViewSource.gif");
            but.setAction("formBuilder.showSource()");
            sb.append(but.getHtml());
        }
        return sb.toString();
    }

    private String getLeftBarEnd() {
        return "</td>";
    }

    public static enum Mode {
        FORM(0),
        SIMPLEFORM(2),
        ELNFORM(3),
        MAINTENANCE(1);

        private int modeNum = 0;

        private Mode(int modeNum) {
            this.modeNum = modeNum;
        }

        public int getModeNum() {
            return this.modeNum;
        }

        private static Mode getModeFromNum(int num) {
            for (Mode m : Mode.values()) {
                if (num != m.getModeNum()) continue;
                return m;
            }
            return FORM;
        }

        public static Mode getMode(String mode) {
            Mode r = FORM;
            if (mode != null && mode.length() > 0) {
                try {
                    r = Mode.valueOf(mode.toUpperCase());
                }
                catch (Exception e1) {
                    try {
                        int m = Integer.parseInt(mode);
                        r = Mode.getModeFromNum(m);
                    }
                    catch (NumberFormatException e2) {
                        r = FORM;
                    }
                }
            }
            return r;
        }
    }
}

