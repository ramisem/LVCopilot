/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 *  org.apache.xml.serialize.OutputFormat
 *  org.apache.xml.serialize.XMLSerializer
 *  org.w3c.tidy.Tidy
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RichTextEditor
extends BaseElement {
    private static final String JS_OBJECT = "richText";
    private static final String JS_ENABLEMETHOD = "enableDM";
    private static final String JS_DOIMAGEBTNMOUSEOVER = "doImageBtnMouseOver";
    private static final String JS_DOIMAGEBTNMOUSEOUT = "doImageBtnMouseOut";
    private static final String JS_DOIMAGEBTNMOUSEDOWN = "doImageBtnMouseDown";
    private static final String JS_DOIMAGEBTNMOUSEUP = "doImageBtnMouseUp";
    private static final String JS_DODROPDOWNMOUSEOVER = "doMouseOver";
    private static final String JS_DODROPDOWNMOUSEOUT = "doMouseOut";
    private static final String JS_DODROPDOWNMOUSEDOWN = "doMouseDown";
    private static final String JS_DODROPDOWNMOUSEUP = "doMouseUp";
    private static final String JS_DODROPDOWNITEMMOUSEOVER = "doMouseOverItem";
    private static final String JS_DODROPDOWNITEMMOUSEOUT = "doMouseOutItem";
    private static final String JS_DOIMAGEBTNCLICK = "doImageBtnCick";
    private static final String JS_DODROPDOWNCLICK = "doClick";
    private static final String JS_DOWNKEYPRESS = "doKey";
    private static final String JS_DODROPDOWNITEMCLICK = "doItemClick";
    private static final String JS_RESIZEMETHOD = "resizeToolbar";
    private static final String JS_MOUSEOVERTOOLBARMETHOD = "overToolbar";
    private static final String JS_MOUSEOUTTOOLBARMETHOD = "outToolbar";
    private static final String JS_DOUBLECLICKTOOLBARMETHOD = "dblClickToolbar";
    private static final String JS_DATAOBJECT = "data";
    private static final String JS_COMMANDOBJECT = "commands";
    private static final String JS_DROPDOWNOBJECT = "dropDown";
    private static final String DEFAULTPAGE_CONFIG = "richtexteditor_defaultpage";
    private static final int DEFAULTPAGE_WIDTH = 200;
    private static final int DEFAULTPAGE_HEIGHT = 400;
    private static final int DEFAULTPAGE_MARGIN_LEFT = 10;
    private static final int DEFAULTPAGE_MARGIN_RIGHT = 10;
    private static final int DEFAULTPAGE_MARGIN_TOP = 10;
    private static final int DEFAULTPAGE_MARGIN_BOTTOM = 10;
    private static final String DEFAULTPAGE_PAGE_UNIT = "mm";
    private static final String DEFAULTPAGE_MARGIN_UNIT = "mm";
    private static final boolean DEFAULTPAGE_GROWABLE = false;
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_TEXT = "text";
    public static final String PROPERTY_TIP = "tip";
    public static final String PROPERTY_ACTION = "action";
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_NEWLINE = "newline";
    public static final String PROPERTY_INVIEWMODE = "inviewmode";
    public static final String PROPERTY_SHORTCUTKEY = "shortcutkey";
    public static final String PROPERTY_COMMAND = "command";
    public static final String PROPERTY_EXECUTEFUNCTION = "executefunction";
    public static final String PROPERTY_QUERYFUNCTION = "queryfunction";
    public static final String PROPERTY_COMMANDOPTIONS = "commandoptions";
    public static final String PROPERTY_UNDOABLE = "undoable";
    public static final String PROPERTY_ITEMS = "items";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_HTML = "html";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PERSISTENT = "persistent";
    public static final String PROPERTYLIST_BUTTONS = "buttons";
    public static final String PROPERTY_SUBMENU = "submenu";
    public static final String PROPERTY_DDWIDTH = "ddwidth";
    public static final String PROPERTY_DEFAULTVALUE = "defaultvalue";
    public static final int PAGETYPE_A4PORTRAIT = 0;
    public static final int PAGETYPE_A4LANDSCAPE = 1;
    public static final int PAGETYPE_LETTERPORTRAIT = 2;
    public static final int PAGETYPE_LETTERLANDSCAPE = 3;
    public static final int PAGETYPE_LEGALLANDSCAPE = 4;
    public static final int PAGETYPE_LEGALPORTRAIT = 5;
    public static final int PAGETYPE_FULLSCREEN = 6;
    public static final int PAGETYPE_HIGHRESOLUTION = 7;
    public static final int PAGETYPE_MEDIUMRESOLUTION = 8;
    public static final int PAGETYPE_LOWRESOLUTION = 9;
    public static final int PAGETYPE_GROWABLE = 10;
    public static final int SOURCEMODE_HTML = 0;
    public static final int SOURCEMODE_XHTML = 1;
    public static final int TOOLBARTYPE_BASIC = 0;
    public static final int TOOLBARTYPE_FULL = 1;
    public static final int TOOLBARTYPE_ADVANCED = 3;
    public static final int TOOLBARTYPE_PRINTABLE = 4;
    public static final int TOOLBAR_EMPTY = -1;
    public static final int TOOLBAR_FORMAT = 0;
    public static final int TOOLBAR_ALIGNMENT = 1;
    public static final int TOOLBAR_CLIPBOARD = 2;
    public static final int TOOLBAR_LINKS = 3;
    public static final int TOOLBAR_LISTS = 4;
    public static final int TOOLBAR_FORM = 5;
    public static final int TOOLBAR_PANELS = 6;
    public static final int TOOLBAR_TABLE = 7;
    public static final int TOOLBAR_FONT = 8;
    public static final int TOOLBAR_PAGE = 9;
    public static final int TOOLBAR_INSERT = 10;
    public static final int TOOLBAR_CLIPBOARD_BASIC = 11;
    public static final int TOOLBAR_FONT_BASIC = 12;
    public static final int TOOLBAR_FORMAT_AND_FONT = 13;
    public static final int TOOLBAR_ALIGNMENT_AND_LIST = 14;
    private String htmlcontent = "";
    private String id = "";
    private String width = "100%";
    private String height = "100%";
    private boolean expandable = true;
    private boolean viewonly = false;
    private boolean maximized = false;
    private PropertyListCollection contextmenu;
    private PropertyListCollection toolbars;
    private PropertyListCollection commands;
    private PropertyListCollection events;
    private int globaltoolbartype;
    private int sourcemode = 0;
    private boolean pagemode = false;
    private boolean html5 = true;
    ToolbarCollapse toolbarCollapse = ToolbarCollapse.AUTOCOLLAPSE;
    private boolean devMode = false;
    private PropertyList defaultPage;
    private String defaultpageProperty = "richtexteditor_defaultpage";
    private TranslationProcessor tp = null;
    private boolean sourceContainsPages = false;
    private boolean allowTabThrough = false;
    private boolean showtoolbar = true;
    private PropertyList userConfig = null;

    public RichTextEditor(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.tp = this.getTranslationProcessor();
        if (this.requestContext == null && pageContext != null && pageContext.getRequest() instanceof HttpServletRequest) {
            this.requestContext = RequestContext.getInstance((HttpServletRequest)pageContext.getRequest());
        }
        this.userConfig = this.requestContext != null ? this.requestContext.getPropertyList("userconfig") : new PropertyList();
        ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
        try {
            this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            this.devMode = false;
        }
        this.html5 = !this.requestContext.getProperty("html5").equalsIgnoreCase("N");
        this.loadDefaultPage();
    }

    public void loadDefaultPage() {
        this.loadDefaultPage(null);
    }

    public void setToolbarCollapse(ToolbarCollapse t) {
        this.toolbarCollapse = t;
    }

    public void loadDefaultPage(String userConfigProperty) {
        String defaultPageString;
        if (userConfigProperty != null && userConfigProperty.length() > 0) {
            defaultPageString = this.userConfig.getProperty(userConfigProperty, "");
        } else {
            userConfigProperty = DEFAULTPAGE_CONFIG;
            defaultPageString = "";
        }
        if (defaultPageString.length() > 0) {
            try {
                this.defaultPage = new PropertyList(new JSONObject(HttpUtil.decodeURIComponent(defaultPageString)));
            }
            catch (Exception e) {
                this.defaultPage = new PropertyList();
                this.defaultPage.setProperty("width", "200");
                this.defaultPage.setProperty("height", "400");
                this.defaultPage.setProperty("marginBottom", "10");
                this.defaultPage.setProperty("marginTop", "10");
                this.defaultPage.setProperty("marginLeft", "10");
                this.defaultPage.setProperty("marginRight", "10");
                this.defaultPage.setProperty("widthUnits", "mm");
                this.defaultPage.setProperty("heightUnits", "mm");
                this.defaultPage.setProperty("marginBottomUnits", "mm");
                this.defaultPage.setProperty("marginTopUnits", "mm");
                this.defaultPage.setProperty("marginLeftUnits", "mm");
                this.defaultPage.setProperty("marginRightUnits", "mm");
                this.defaultPage.setProperty("growable", "N");
            }
        } else if (this.defaultPage == null) {
            this.defaultPage = new PropertyList();
            this.defaultPage.setProperty("width", "200");
            this.defaultPage.setProperty("height", "400");
            this.defaultPage.setProperty("marginBottom", "10");
            this.defaultPage.setProperty("marginTop", "10");
            this.defaultPage.setProperty("marginLeft", "10");
            this.defaultPage.setProperty("marginRight", "10");
            this.defaultPage.setProperty("widthUnits", "mm");
            this.defaultPage.setProperty("heightUnits", "mm");
            this.defaultPage.setProperty("marginBottomUnits", "mm");
            this.defaultPage.setProperty("marginTopUnits", "mm");
            this.defaultPage.setProperty("marginLeftUnits", "mm");
            this.defaultPage.setProperty("marginRightUnits", "mm");
            this.defaultPage.setProperty("growable", "N");
        }
        this.defaultpageProperty = userConfigProperty;
    }

    public RichTextEditor(String connectionId) {
        this.setConnectionId(connectionId);
        this.pageContext = null;
        this.tp = new TranslationProcessor(connectionId);
        this.html5 = true;
        this.loadDefaultPage();
    }

    public RichTextEditor(SapphireConnection sapphireConnection) {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.setBrowser(new Browser(sapphireConnection.getUserAgent()));
        this.pageContext = null;
        this.tp = new TranslationProcessor(sapphireConnection.getConnectionId());
        this.loadDefaultPage();
    }

    public void setDefaultPage(double width, double height, double bottomMargin, double rightMargin, double topMargin, double leftMargin, String dimensionunits, String marginunits) {
        this.setDefaultPage(width, height, bottomMargin, rightMargin, topMargin, leftMargin, dimensionunits, marginunits, false);
    }

    public void setDefaultPage(double width, double height, double bottomMargin, double rightMargin, double topMargin, double leftMargin, String dimensionunits, String marginunits, boolean growable) {
        this.defaultPage = new PropertyList();
        this.defaultPage.setProperty("width", "" + width);
        this.defaultPage.setProperty("height", "" + height);
        this.defaultPage.setProperty("marginBottom", "" + bottomMargin);
        this.defaultPage.setProperty("marginTop", "" + topMargin);
        this.defaultPage.setProperty("marginLeft", "" + leftMargin);
        this.defaultPage.setProperty("marginRight", "" + rightMargin);
        this.defaultPage.setProperty("growable", growable ? "Y" : "N");
        if (dimensionunits.length() > 0) {
            this.defaultPage.setProperty("widthUnits", "" + dimensionunits);
            this.defaultPage.setProperty("heightUnits", "" + dimensionunits);
        } else {
            this.defaultPage.setProperty("widthUnits", "mm");
            this.defaultPage.setProperty("heightUnits", "mm");
        }
        if (marginunits.length() > 0) {
            this.defaultPage.setProperty("marginBottomUnits", "" + marginunits);
            this.defaultPage.setProperty("marginTopUnits", "" + marginunits);
            this.defaultPage.setProperty("marginLeftUnits", "" + marginunits);
            this.defaultPage.setProperty("marginRightUnits", "" + marginunits);
        } else {
            this.defaultPage.setProperty("marginBottomUnits", "mm");
            this.defaultPage.setProperty("marginTopUnits", "mm");
            this.defaultPage.setProperty("marginLeftUnits", "mm");
            this.defaultPage.setProperty("marginRightUnits", "mm");
        }
    }

    public void setDefaultPage(int pageType) {
        switch (pageType) {
            case 0: {
                this.setDefaultPage(210.0, 297.0, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 1: {
                this.setDefaultPage(297.0, 210.0, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 2: {
                this.setDefaultPage(215.9, 279.4, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 3: {
                this.setDefaultPage(279.4, 215.9, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 5: {
                this.setDefaultPage(215.9, 355.6, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 4: {
                this.setDefaultPage(355.6, 215.9, 25.4, 25.4, 25.4, 25.4, "mm", "mm");
                break;
            }
            case 6: {
                this.setDefaultPage(100.0, -1.0, 10.0, 10.0, 10.0, 10.0, "%", "px");
                break;
            }
            case 7: {
                this.setDefaultPage(1280.0, 1024.0, 10.0, 10.0, 10.0, 10.0, "px", "px");
                break;
            }
            case 8: {
                this.setDefaultPage(1024.0, 768.0, 25.4, 25.4, 25.4, 25.4, "px", "px");
                break;
            }
            case 9: {
                this.setDefaultPage(800.0, 600.0, 25.4, 25.4, 25.4, 25.4, "px", "px");
                break;
            }
            case 10: {
                this.setDefaultPage(-1.0, -1.0, 0.0, 0.0, 0.0, 0.0, "%", "px", true);
            }
        }
    }

    public void setShowToolbar(boolean show) {
        this.showtoolbar = show;
    }

    public void setTabThrough(boolean tabThrough) {
        this.allowTabThrough = tabThrough;
    }

    public void setPageMode(boolean enabled) {
        this.pagemode = enabled;
    }

    public double[] getDefaultPage() {
        double width = 200.0;
        double height = 400.0;
        double marginBottom = 10.0;
        double marginRight = 10.0;
        double marginTop = 10.0;
        double marginLeft = 10.0;
        try {
            width = Double.parseDouble(this.defaultPage.getProperty("width", "200"));
            height = Double.parseDouble(this.defaultPage.getProperty("height", "400"));
            marginBottom = Double.parseDouble(this.defaultPage.getProperty("marginBottom", "10"));
            marginRight = Double.parseDouble(this.defaultPage.getProperty("marginRight", "10"));
            marginTop = Double.parseDouble(this.defaultPage.getProperty("marginTop", "10"));
            marginLeft = Double.parseDouble(this.defaultPage.getProperty("marginLeft", "10"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new double[]{width, height, marginBottom, marginRight, marginTop, marginLeft};
    }

    public boolean getPageMode() {
        return this.pagemode;
    }

    public void setToolbars(PropertyListCollection toolbars) {
        this.globaltoolbartype = -1;
        this.toolbars = toolbars;
    }

    public void setContextMenu(PropertyListCollection contextmenu) {
        this.globaltoolbartype = -1;
        this.contextmenu = contextmenu;
    }

    public PropertyListCollection getToolbars() {
        if (this.id.length() > 0 && this.toolbars == null && this.globaltoolbartype > -1) {
            this.createToolbars(this.globaltoolbartype);
        }
        return this.toolbars;
    }

    public PropertyListCollection getContextMenu() {
        if (this.id.length() > 0 && this.contextmenu == null && this.globaltoolbartype > -1) {
            this.createMenu(this.globaltoolbartype);
        }
        return this.contextmenu;
    }

    public PropertyListCollection getCustomCommands() {
        if (this.commands == null) {
            this.createCommands();
        }
        return this.commands;
    }

    public int addEvent(String eventname, String eventFunction) {
        if (this.events == null) {
            this.events = new PropertyListCollection();
        }
        if (eventname.length() > 0) {
            if ((eventname = eventname.toLowerCase()).startsWith("on")) {
                eventname = eventname.substring(2);
            }
            PropertyList event = new PropertyList(eventname);
            event.setProperty(PROPERTY_NAME, eventname);
            event.setProperty(PROPERTY_ACTION, eventFunction);
            this.events.add(event);
        }
        return this.events.size() - 1;
    }

    public int addCustomCommand(String command, String executeFunction, String queryFunction) {
        return this.addCustomCommand(command, executeFunction, queryFunction, false);
    }

    public int addCustomCommand(String command, String executeFunction, String queryFunction, boolean persistent) {
        PropertyListCollection commands = this.getCustomCommands();
        if (command.length() > 0 && executeFunction != null && executeFunction.length() > 0) {
            if (command.indexOf("$") == 0) {
                command = command.substring(1);
            }
            PropertyList com = new PropertyList();
            com.setProperty(PROPERTY_COMMAND, command);
            com.setProperty(PROPERTY_EXECUTEFUNCTION, executeFunction);
            if (queryFunction != null && queryFunction.length() > 0) {
                com.setProperty(PROPERTY_QUERYFUNCTION, queryFunction);
            }
            com.setProperty(PROPERTY_PERSISTENT, persistent ? "Y" : "N");
            com.setId(command);
            commands.add(com);
            return commands.size() - 1;
        }
        return -1;
    }

    public void setToolbarType(int globaltoolbartype) {
        this.globaltoolbartype = globaltoolbartype;
        if (this.id.length() > 0) {
            this.createToolbars(globaltoolbartype);
            this.createMenu(globaltoolbartype);
        }
    }

    public void setSourceMode(int sourcemode) {
        this.sourcemode = sourcemode;
    }

    public int getSourceMode() {
        return this.sourcemode;
    }

    public int addToolbar(int toolbartype, boolean newline, boolean inViewOnly) {
        if (this.toolbars == null) {
            this.toolbars = new PropertyListCollection();
        }
        this.globaltoolbartype = -1;
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty(PROPERTY_NEWLINE, newline ? "Y" : "N");
        toolbar.setProperty(PROPERTY_INVIEWMODE, inViewOnly ? "Y" : "N");
        PropertyListCollection buttons = this.createButtons(toolbartype);
        toolbar.setProperty(PROPERTYLIST_BUTTONS, buttons);
        this.toolbars.add(toolbar);
        return this.toolbars.size() - 1;
    }

    public int addToolbar(int toolbartype, boolean newline) {
        return this.addToolbar(toolbartype, newline, false);
    }

    public void removeMenuItem(int itemnumber) {
        if (this.contextmenu != null) {
            this.contextmenu.remove(itemnumber);
        }
    }

    public void removeButton(int toolbarnumber, int buttonnumber) {
        PropertyList toolbar;
        if (this.toolbars != null && (toolbar = this.toolbars.getPropertyList(toolbarnumber)) != null) {
            PropertyListCollection buttons = toolbar.getCollection(PROPERTYLIST_BUTTONS);
            buttons.remove(buttonnumber);
        }
    }

    public void addMenuItems(int toolbartype) {
        if (this.contextmenu == null) {
            this.contextmenu = new PropertyListCollection();
        }
        switch (toolbartype) {
            case 0: {
                break;
            }
            case 9: {
                break;
            }
            case 2: {
                if (this.browser.isIE() && !this.html5) {
                    this.addCommandMenuItem("Copy", "WEB-CORE/images/gif/Copy2.gif", "Copy", "", true);
                    this.addCommandMenuItem("Cut", "WEB-CORE/images/gif/Cut.gif", "Cut", "", true);
                    this.addCommandMenuItem("Paste", "WEB-CORE/images/gif/Paste.gif", "$Paste", "", true);
                    break;
                }
                this.addCommandMenuItem("Copy", "WEB-CORE/images/gif/Copy2.gif", "$Copy", "", true);
                this.addCommandMenuItem("Cut", "WEB-CORE/images/gif/Cut.gif", "$Cut", "", true);
                this.addCommandMenuItem("Paste", "WEB-CORE/images/gif/Paste.gif", "$Paste", "", true);
                break;
            }
            case 11: {
                if (this.browser.isIE() && !this.html5) {
                    this.addCommandMenuItem("Copy", "WEB-CORE/images/gif/Copy2.gif", "Copy", "", true);
                    this.addCommandMenuItem("Cut", "WEB-CORE/images/gif/Cut.gif", "Cut", "", true);
                    this.addCommandMenuItem("Paste", "WEB-CORE/elements/richtext/images/gif/ToolbarPasteText.gif", "$Paste", PROPERTY_TEXT, true);
                    break;
                }
                this.addCommandMenuItem("Copy", "WEB-CORE/images/gif/Copy2.gif", "$Copy", "", true);
                this.addCommandMenuItem("Cut", "WEB-CORE/images/gif/Cut.gif", "$Cut", "", true);
                this.addCommandMenuItem("Paste", "WEB-CORE/elements/richtext/images/gif/ToolbarPasteText.gif", "$Paste", PROPERTY_TEXT, true);
                break;
            }
            case 3: {
                this.addMenuSeparator();
                this.addCommandMenuItem("Open Link", "WEB-CORE/elements/richtext/images/gif/ToolbarOpenLink.gif", "$OpenLink", "", false);
                this.addCommandMenuItem("Edit Link", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertLink.gif", "$InsertLink", "", false);
                this.addCommandMenuItem("Break Link", "WEB-CORE/elements/richtext/images/gif/ToolbarBreakLink.gif", "Unlink", "", false);
                break;
            }
            case 1: {
                break;
            }
            case 4: {
                break;
            }
            case 6: {
                break;
            }
            case 7: {
                this.addMenuSeparator();
                this.addCommandMenuItem("Table Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertTable.gif", "$InsertTable", "Edit", false);
                int item = this.addCommandMenuItem("Table Border", "WEB-CORE/elements/richtext/images/gif/ToolbarTableBorder.gif", "$InsertTable", "", false);
                this.addSubMenuItem(item, "No Border", "WEB-CORE/elements/richtext/images/gif/ToolbarNoBorder.gif", "$TableBorder", "", "No", false);
                this.addSubMenuItem(item, "All Borders", "WEB-CORE/elements/richtext/images/gif/ToolbarTableBorder.gif", "$TableBorder", "", "All", false);
                this.addSubMenuItem(item, "Outside Borders", "WEB-CORE/elements/richtext/images/gif/ToolbarOutsideBorder.gif", "$TableBorder", "", "Outside", false);
                this.addSubMenuItem(item, "Thick Box Border", "WEB-CORE/elements/richtext/images/gif/ToolbarThickBorder.gif", "$TableBorder", "", "Thick", false);
                this.addCommandMenuItem("Cell Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertTable.gif", "$TableProperties", "Cell", false);
                this.addCommandMenuItem("Column Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertTable.gif", "$TableProperties", "Column", false);
                this.addCommandMenuItem("Row Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertTable.gif", "$TableProperties", "Row", false);
                this.addMenuSeparator();
                this.addCommandMenuItem("Add Row", "WEB-CORE/elements/richtext/images/gif/ToolbarAddRow.gif", "$InsertTableRow", "", false);
                this.addCommandMenuItem("Insert Row", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertRow.gif", "$InsertTableRow", "insert", false);
                this.addCommandMenuItem("Add Column", "WEB-CORE/elements/richtext/images/gif/ToolbarAddColumn.gif", "$InsertTableColumn", "", false);
                this.addCommandMenuItem("Insert Column", "WEB-CORE/elements/richtext/images/gif/ToolbarInsertColumn.gif", "$InsertTableColumn", "insert", false);
                this.addMenuSeparator();
                this.addCommandMenuItem("Merge Down", "WEB-CORE/elements/richtext/images/gif/ToolbarMergeRow.gif", "$MergeTableRow", "", false);
                this.addCommandMenuItem("Merge Right", "WEB-CORE/elements/richtext/images/gif/ToolbarMergeCell.gif", "$MergeTableCell", "", false);
                this.addCommandMenuItem("Remove Row", "WEB-CORE/elements/richtext/images/gif/ToolbarDeleteRow.gif", "$DeleteTableRow", "", false);
                this.addCommandMenuItem("Remove Column", "WEB-CORE/elements/richtext/images/gif/ToolbarDeleteColumn.gif", "$DeleteTableColumn", "", false);
                break;
            }
            case 5: {
                break;
            }
            case 10: {
                this.addCommandMenuItem("Image Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarImage.gif", "$InsertImage", "", false);
                this.addCommandMenuItem("IFrame Properties", "WEB-CORE/elements/richtext/images/gif/ToolbarFrame.gif", "$InsertIFrame", "", false);
                break;
            }
            case 8: {
                break;
            }
            case 12: {
                break;
            }
        }
    }

    public int addSubMenuItem(int parentmenu, String text, String image, String command, String action, String options, boolean persistent) {
        if (this.contextmenu != null) {
            if (this.contextmenu.size() > parentmenu && parentmenu > -1) {
                PropertyListCollection submenu = this.contextmenu.getPropertyList(parentmenu).getCollection(PROPERTY_SUBMENU);
                PropertyList submenuitem = new PropertyList();
                submenuitem.setProperty(PROPERTY_TEXT, text);
                submenuitem.setProperty(PROPERTY_IMAGE, image);
                if (command.length() > 0) {
                    submenuitem.setProperty(PROPERTY_COMMAND, command);
                } else {
                    submenuitem.setProperty(PROPERTY_COMMAND, "");
                }
                if (options.length() > 0) {
                    submenuitem.setProperty(PROPERTY_COMMANDOPTIONS, options);
                } else {
                    submenuitem.setProperty(PROPERTY_COMMANDOPTIONS, "");
                }
                if (action.length() > 0) {
                    submenuitem.setProperty(PROPERTY_ACTION, action);
                } else {
                    submenuitem.setProperty(PROPERTY_ACTION, "");
                }
                if (persistent) {
                    submenuitem.setProperty(PROPERTY_PERSISTENT, "Y");
                } else {
                    submenuitem.setProperty(PROPERTY_PERSISTENT, "N");
                }
                submenuitem.setProperty(PROPERTY_UNDOABLE, "Y");
                submenu.add(submenuitem);
                return submenu.size() - 1;
            }
            return -1;
        }
        return -1;
    }

    public int addMenuItem(String text, String image, String command, String action, String options, boolean persistent) {
        if (this.contextmenu != null) {
            PropertyList menuitem = new PropertyList();
            if (text != null && text.length() > 0 && !text.equals("-") && this.tp != null) {
                text = this.tp.translate(text);
            }
            menuitem.setProperty(PROPERTY_TEXT, text);
            menuitem.setProperty(PROPERTY_IMAGE, image);
            if (command.length() > 0) {
                menuitem.setProperty(PROPERTY_COMMAND, command);
            } else {
                menuitem.setProperty(PROPERTY_COMMAND, "");
            }
            if (options.length() > 0) {
                menuitem.setProperty(PROPERTY_COMMANDOPTIONS, options);
            } else {
                menuitem.setProperty(PROPERTY_COMMANDOPTIONS, "");
            }
            if (action.length() > 0) {
                menuitem.setProperty(PROPERTY_ACTION, action);
            } else {
                menuitem.setProperty(PROPERTY_ACTION, "");
            }
            if (persistent) {
                menuitem.setProperty(PROPERTY_PERSISTENT, "Y");
            } else {
                menuitem.setProperty(PROPERTY_PERSISTENT, "N");
            }
            menuitem.setProperty(PROPERTY_UNDOABLE, "Y");
            menuitem.setProperty(PROPERTY_SUBMENU, new PropertyListCollection());
            this.contextmenu.add(menuitem);
            return this.contextmenu.size() - 1;
        }
        return -1;
    }

    public int addCommandMenuItem(String text, String image, String command, String options, boolean persistent) {
        if (this.contextmenu == null) {
            this.contextmenu = new PropertyListCollection();
        }
        return this.addMenuItem(text, image, command, "", options, persistent);
    }

    public int addJSMenuItem(String text, String image, String action) {
        if (this.contextmenu == null) {
            this.contextmenu = new PropertyListCollection();
        }
        return this.addMenuItem(text, image, "", action, "", true);
    }

    public int addMenuSeparator() {
        if (this.contextmenu == null) {
            this.contextmenu = new PropertyListCollection();
        }
        return this.addMenuItem("-", "", "", "", "", true);
    }

    private PropertyList buildButton(String text, String tip, String image, String command, String action, String options, char shortcutkey, boolean shortcutshift) {
        PropertyList button = new PropertyList();
        if (this.tp != null) {
            if (text != null && text.length() > 0 && !text.equals("-")) {
                text = this.tp.translate(text);
            }
            if (tip != null && tip.length() > 0) {
                tip = this.tp.translate(tip);
            }
        }
        button.setProperty(PROPERTY_TEXT, text);
        button.setProperty(PROPERTY_IMAGE, image);
        button.setProperty(PROPERTY_TIP, tip);
        if (command.length() > 0) {
            button.setProperty(PROPERTY_COMMAND, command);
        }
        if (options.length() > 0) {
            button.setProperty(PROPERTY_COMMANDOPTIONS, options);
        }
        if (action.length() > 0) {
            button.setProperty(PROPERTY_ACTION, action);
        }
        if (shortcutkey > '\u0000') {
            if (!shortcutshift && Character.isUpperCase(shortcutkey)) {
                shortcutkey = Character.toLowerCase(shortcutkey);
                shortcutshift = true;
            }
            button.setProperty(PROPERTY_SHORTCUTKEY, (shortcutshift ? "+" : "") + shortcutkey);
        }
        return button;
    }

    public int insertButton(int toolbarnumber, int afterbutton, String text, String tip, String image, String command, String action, String options, char shortcutkey) {
        if (this.toolbars != null) {
            PropertyList toolbar = this.toolbars.getPropertyList(toolbarnumber);
            if (toolbar != null) {
                PropertyListCollection buttons = toolbar.getCollection(PROPERTYLIST_BUTTONS);
                PropertyList button = this.buildButton(text, tip, image, command, action, options, shortcutkey, false);
                buttons.add(afterbutton, button);
                return afterbutton;
            }
            return -1;
        }
        return -1;
    }

    public int addButton(int toolbarnumber, String text, String tip, String image, String command, String action, String options, char shortcutkey) {
        return this.addButton(toolbarnumber, text, tip, image, command, action, options, shortcutkey, false);
    }

    public int addButton(int toolbarnumber, String text, String tip, String image, String command, String action, String options, char shortcutkey, boolean shortcutshift) {
        if (this.toolbars != null) {
            PropertyList toolbar = this.toolbars.getPropertyList(toolbarnumber);
            if (toolbar != null) {
                PropertyListCollection buttons = toolbar.getCollection(PROPERTYLIST_BUTTONS);
                PropertyList button = this.buildButton(text, tip, image, command, action, options, shortcutkey, shortcutshift);
                buttons.add(button);
                return buttons.size() - 1;
            }
            return -1;
        }
        return -1;
    }

    public int addSelectButton(int toolbarnumber, String text, String[] html, String[] labeltext, String[] value, String command) {
        if (this.toolbars != null) {
            PropertyList toolbar = this.toolbars.getPropertyList(toolbarnumber);
            if (toolbar != null) {
                PropertyListCollection buttons = toolbar.getCollection(PROPERTYLIST_BUTTONS);
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_TEXT, text);
                if (command.length() > 0) {
                    button.setProperty(PROPERTY_COMMAND, command);
                }
                PropertyListCollection items = new PropertyListCollection();
                int length = value.length;
                if (length > html.length) {
                    length = html.length;
                }
                if (length > labeltext.length) {
                    length = labeltext.length;
                }
                for (int i = 0; i < length; ++i) {
                    String currhtml;
                    String currvalue = value[i];
                    if (currvalue.length() <= 0) continue;
                    String currtext = labeltext[i];
                    if (currtext.length() == 0) {
                        currtext = currvalue;
                    }
                    if ((currhtml = html[i]).length() == 0) {
                        currhtml = currtext;
                    }
                    PropertyList item = new PropertyList();
                    item.setProperty(PROPERTY_VALUE, currvalue);
                    item.setProperty(PROPERTY_HTML, currhtml);
                    item.setProperty(PROPERTY_TEXT, currtext);
                    items.add(item);
                }
                button.setProperty(PROPERTY_ITEMS, items);
                buttons.add(button);
                return buttons.size() - 1;
            }
            return -1;
        }
        return -1;
    }

    public int addSeparator(int toolbarnumber) {
        return this.addButton(toolbarnumber, "-", "", "", "", "", "", '\u0000');
    }

    public int addJSButton(int toolbarnumber, String tip, String image, String action) {
        return this.addButton(toolbarnumber, "", tip, image, "", action, "", '\u0000');
    }

    public int addCommandButton(int toolbarnumber, String tip, String image, String command, String options) {
        return this.addCommandButton(toolbarnumber, tip, image, command, options, '\u0000');
    }

    public int addCommandButton(int toolbarnumber, String tip, String image, String command, String options, char shortcutkey) {
        return this.addCommandButton(toolbarnumber, tip, image, command, options, shortcutkey, false);
    }

    public int addCommandButton(int toolbarnumber, String tip, String image, String command, String options, char shortcutkey, boolean shortcutshift) {
        return this.addButton(toolbarnumber, "", tip, image, command, "", options, shortcutkey, shortcutshift);
    }

    private void createMenu(int globaltoolbartype) {
        this.contextmenu = new PropertyListCollection();
        if (globaltoolbartype > -1) {
            if (globaltoolbartype != 4) {
                this.addMenuItems(2);
                this.addMenuItems(3);
                if (globaltoolbartype == 1) {
                    this.addMenuItems(7);
                }
                if (globaltoolbartype == 3) {
                    this.addMenuItems(5);
                    this.addMenuItems(6);
                }
            } else {
                this.addMenuItems(11);
            }
        }
    }

    private void createToolbars(int globaltoolbartype) {
        this.toolbars = new PropertyListCollection();
        if (globaltoolbartype > -1) {
            if (globaltoolbartype != 4) {
                this.addToolbar(0, false);
                this.addToolbar(2, false);
                this.addToolbar(1, false);
                if (globaltoolbartype == 1) {
                    this.addToolbar(3, false);
                    this.addToolbar(8, true);
                    this.addToolbar(4, false);
                    this.addToolbar(7, false);
                }
                if (globaltoolbartype == 3) {
                    this.addToolbar(5, true);
                    this.addToolbar(6, false);
                }
            } else {
                this.addToolbar(13, false);
                this.addToolbar(11, false);
                this.addToolbar(14, false);
            }
        }
    }

    private void createCommands() {
        this.commands = new PropertyListCollection();
        PropertyList command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "paste");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.paste");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryPaste");
        command.setId("paste");
        this.commands.add(command);
        if (!this.browser.isIE() || this.html5) {
            command = new PropertyList();
            command.setProperty(PROPERTY_COMMAND, "copy");
            command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.copy");
            command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryCopy");
            command.setId("copy");
            this.commands.add(command);
            command = new PropertyList();
            command.setProperty(PROPERTY_COMMAND, "cut");
            command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.cut");
            command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryCut");
            command.setId("cut");
            this.commands.add(command);
        }
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "inserttable");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertTable");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertTable");
        command.setId("inserttable");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "tableborder");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.tableBorder");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryTableBorder");
        command.setId("tableborder");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "tableproperties");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.tableProperties");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryTableProperties");
        command.setId("tableproperties");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "inserttablerow");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertTableRow");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertTableRow");
        command.setId("inserttablerow");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "inserttablecolumn");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertTableColumn");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertTableColumn");
        command.setId("inserttablecolumn");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "mergetablerow");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.mergeTableRow");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryMergeTableRow");
        command.setId("mergetablerow");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "mergetablecell");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.mergeTableCell");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryMergeTableCell");
        command.setId("mergetablecell");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "deletetablerow");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.deleteTableRow");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryDeleteTableRow");
        command.setId("deletetablerow");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "deletetablecolumn");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.deleteTableColumn");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryDeleteTableColumn");
        command.setId("deletetablecolumn");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertform");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertForm");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertForm");
        command.setId("insertform");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertdiv");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertDiv");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertDiv");
        command.setId("insertdiv");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "undo");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.undo");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryUndo");
        command.setId("undo");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "redo");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.redo");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryRedo");
        command.setId("redo");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertlink");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertLink");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertLink");
        command.setId("insertlink");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "openlink");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.openLink");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryOpenLink");
        command.setId("openlink");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertbookmark");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertBookmark");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertBookmark");
        command.setId("insertbookmark");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "forecolor");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.foreColor");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryForeColor");
        command.setId("forecolor");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "fontsize");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.fontSize");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryFontSize");
        command.setId("fontsize");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "fontname");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.fontName");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryFontName");
        command.setId("fontname");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "managepage");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.managePage");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryManagePage");
        command.setId("managepage");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertimage");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertImage");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertImage");
        command.setId("insertimage");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertiframe");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertIFrame");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertIFrame");
        command.setId("insertiframe");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "insertfieldset");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.insertFieldset");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryInsertFieldset");
        command.setId("insertfieldset");
        this.commands.add(command);
        command = new PropertyList();
        command.setProperty(PROPERTY_COMMAND, "zoom");
        command.setProperty(PROPERTY_EXECUTEFUNCTION, "richText.commands.zoom");
        command.setProperty(PROPERTY_QUERYFUNCTION, "richText.commands.queryZoom");
        command.setProperty(PROPERTY_PERSISTENT, "Y");
        command.setId("zoom");
        this.commands.add(command);
    }

    private void createFontButtons(PropertyListCollection buttons, boolean basic) {
        PropertyList button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "Size");
        PropertyListCollection items = new PropertyListCollection();
        PropertyList item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "1");
        item.setProperty(PROPERTY_HTML, "<font size=1>Size 1</font>");
        item.setProperty(PROPERTY_TEXT, "Size 1");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "2");
        item.setProperty(PROPERTY_HTML, "<font size=2>Size 2</font>");
        item.setProperty(PROPERTY_TEXT, "Size 2");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "3");
        item.setProperty(PROPERTY_HTML, "<font size=3>Size 3</font>");
        item.setProperty(PROPERTY_TEXT, "Size 3");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "4");
        item.setProperty(PROPERTY_HTML, "<font size=4>Size 4</font>");
        item.setProperty(PROPERTY_TEXT, "Size4");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "5");
        item.setProperty(PROPERTY_HTML, "<font size=5>Size 5</font>");
        item.setProperty(PROPERTY_TEXT, "Size 5");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "6");
        item.setProperty(PROPERTY_HTML, "<font size=6>Size 6</font>");
        item.setProperty(PROPERTY_TEXT, "Size 6");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "7");
        item.setProperty(PROPERTY_HTML, "<font size=7>Size 7</font>");
        item.setProperty(PROPERTY_TEXT, "Size 7");
        items.add(item);
        button.setProperty(PROPERTY_ITEMS, items);
        button.setProperty(PROPERTY_COMMAND, "$fontsize");
        button.setProperty(PROPERTY_DEFAULTVALUE, "Auto");
        button.setProperty(PROPERTY_DDWIDTH, "28");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "Font");
        items = new PropertyListCollection();
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "'Open Sans'");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:'Open Sans';\" title=\"Open Sans\">Open Sans</span>");
        item.setProperty(PROPERTY_TEXT, "Open Sans");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Arial");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:arial;\" title=\"Arial\">Arial</span>");
        item.setProperty(PROPERTY_TEXT, "Arial");
        items.add(item);
        if (!basic) {
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Comic Sans MS");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:comic sans ms;\" title=\"Comic Sans MS\">Comic Sans MS</span>");
            item.setProperty(PROPERTY_TEXT, "Comic Sans MS");
            items.add(item);
        }
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Courier New");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:courier new;\" title=\"Courier New\">Courier New</span>");
        item.setProperty(PROPERTY_TEXT, "Courier New");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Times New Roman");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Times New Roman;\" title=\"Times New Roman\">Times New Roman</span>");
        item.setProperty(PROPERTY_TEXT, "Times New Roman");
        items.add(item);
        if (!basic) {
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Verdana");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Verdana;\" title=\"Verdana\">Verdana</span>");
            item.setProperty(PROPERTY_TEXT, "Verdana");
            items.add(item);
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Georgia");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Georgia;\" title=\"Georgia\">Georgia</span>");
            item.setProperty(PROPERTY_TEXT, "Georgia");
            items.add(item);
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Trebuchet MS");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Trebuchet MS;\" title=\"Trebuchet MS\">Trebuchet MS</span>");
            item.setProperty(PROPERTY_TEXT, "Trebuchet MS");
            items.add(item);
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Impact");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Impact;\" title=\"Impact\">Impact</span>");
            item.setProperty(PROPERTY_TEXT, "Impact");
            items.add(item);
            item = new PropertyList();
            item.setProperty(PROPERTY_VALUE, "Webdings");
            item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Webdings;\" title=\"Webdings\">Webdings</span>");
            item.setProperty(PROPERTY_TEXT, "Webdings");
            items.add(item);
        }
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Symbol");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;font-family:Symbol;\" title=\"Webdings\">Symbol</span>");
        item.setProperty(PROPERTY_TEXT, "Symbol");
        items.add(item);
        button.setProperty(PROPERTY_ITEMS, items);
        button.setProperty(PROPERTY_COMMAND, "$FontName");
        button.setProperty(PROPERTY_DDWIDTH, "52");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "Color");
        items = new PropertyListCollection();
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "0");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:auto;\">Color Auto</span>");
        item.setProperty(PROPERTY_TEXT, "Auto");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Red");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Red;\">Color Red</span>");
        item.setProperty(PROPERTY_TEXT, "Red");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Blue");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Blue;\">Color Blue</span>");
        item.setProperty(PROPERTY_TEXT, "Blue");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Black");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Black;\">Color Black</span>");
        item.setProperty(PROPERTY_TEXT, "Black");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Yellow");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Yellow;\">Color Yellow</span>");
        item.setProperty(PROPERTY_TEXT, "Yellow");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Gray");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Gray;\">Color Gray</span>");
        item.setProperty(PROPERTY_TEXT, "Gray");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Purple");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Purple;\">Color Purple</span>");
        item.setProperty(PROPERTY_TEXT, "Purple");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Pink");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Pink;\">Color Pink</span>");
        item.setProperty(PROPERTY_TEXT, "Pink");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Teal");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Teal;\">Color Teal</span>");
        item.setProperty(PROPERTY_TEXT, "Teal");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Magenta");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Magenta;\">Color Magenta</span>");
        item.setProperty(PROPERTY_TEXT, "Magenta");
        items.add(item);
        item = new PropertyList();
        item.setProperty(PROPERTY_VALUE, "Orange");
        item.setProperty(PROPERTY_HTML, "<span style=\"font-size:8pt;color:Orange;\">Color Orange</span>");
        item.setProperty(PROPERTY_TEXT, "Orange");
        items.add(item);
        button.setProperty(PROPERTY_ITEMS, items);
        button.setProperty(PROPERTY_COMMAND, "$forecolor");
        button.setProperty(PROPERTY_DDWIDTH, "52");
        buttons.add(button);
    }

    private void createClipboardButtons(PropertyListCollection buttons, boolean basic) {
        PropertyList button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/images/gif/Copy2.gif");
        button.setProperty(PROPERTY_TIP, "Copy");
        button.setProperty(PROPERTY_COMMAND, this.browser.isIE() && !this.html5 ? "Copy" : "$Copy");
        button.setProperty(PROPERTY_SHORTCUTKEY, "c");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/images/gif/Cut.gif");
        button.setProperty(PROPERTY_TIP, "Cut");
        button.setProperty(PROPERTY_COMMAND, this.browser.isIE() && !this.html5 ? "Cut" : "$Cut");
        button.setProperty(PROPERTY_SHORTCUTKEY, "x");
        buttons.add(button);
        if (basic) {
            button = new PropertyList();
            button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarPasteText.gif");
            button.setProperty(PROPERTY_TIP, "Paste Text");
            button.setProperty(PROPERTY_COMMAND, "$Paste");
            button.setProperty(PROPERTY_COMMANDOPTIONS, PROPERTY_TEXT);
            button.setProperty(PROPERTY_SHORTCUTKEY, "v");
            buttons.add(button);
        } else {
            button = new PropertyList();
            button.setProperty(PROPERTY_IMAGE, "WEB-CORE/images/gif/Paste.gif");
            button.setProperty(PROPERTY_TIP, "Paste");
            button.setProperty(PROPERTY_COMMAND, "$Paste");
            button.setProperty(PROPERTY_SHORTCUTKEY, "v");
            buttons.add(button);
            button = new PropertyList();
            button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarPasteText.gif");
            button.setProperty(PROPERTY_TIP, "Paste Text");
            button.setProperty(PROPERTY_COMMAND, "$Paste");
            button.setProperty(PROPERTY_COMMANDOPTIONS, PROPERTY_TEXT);
            button.setProperty(PROPERTY_SHORTCUTKEY, "+v");
            buttons.add(button);
        }
        button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "-");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarUndo.gif");
        button.setProperty(PROPERTY_TIP, "Undo");
        button.setProperty(PROPERTY_COMMAND, "$Undo");
        button.setProperty(PROPERTY_SHORTCUTKEY, "z");
        button.setProperty(PROPERTY_UNDOABLE, "N");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarRedo.gif");
        button.setProperty(PROPERTY_TIP, "Redo");
        button.setProperty(PROPERTY_COMMAND, "$Redo");
        button.setProperty(PROPERTY_SHORTCUTKEY, "+z");
        button.setProperty(PROPERTY_UNDOABLE, "N");
        buttons.add(button);
    }

    private void createAlignmentButtons(PropertyListCollection buttons) {
        PropertyList button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarAlignLeft.gif");
        button.setProperty(PROPERTY_TIP, "Left Align");
        button.setProperty(PROPERTY_COMMAND, "JustifyLeft");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarAlignCenter.gif");
        button.setProperty(PROPERTY_TIP, "Center Align");
        button.setProperty(PROPERTY_COMMAND, "JustifyCenter");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarAlignRight.gif");
        button.setProperty(PROPERTY_TIP, "Right Align");
        button.setProperty(PROPERTY_COMMAND, "JustifyRight");
        buttons.add(button);
    }

    private void createFormatButtons(PropertyListCollection buttons) {
        PropertyList button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarBold.gif");
        button.setProperty(PROPERTY_TIP, "Bold");
        button.setProperty(PROPERTY_COMMAND, "Bold");
        button.setProperty(PROPERTY_SHORTCUTKEY, "b");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarItalic.gif");
        button.setProperty(PROPERTY_TIP, "Italic");
        button.setProperty(PROPERTY_COMMAND, "Italic");
        button.setProperty(PROPERTY_SHORTCUTKEY, "i");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarUnderline.gif");
        button.setProperty(PROPERTY_TIP, "Underline");
        button.setProperty(PROPERTY_COMMAND, "Underline");
        button.setProperty(PROPERTY_SHORTCUTKEY, "u");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarStrikethrough.gif");
        button.setProperty(PROPERTY_TIP, "Strike Through");
        button.setProperty(PROPERTY_COMMAND, "StrikeThrough");
        button.setProperty(PROPERTY_SHORTCUTKEY, "u");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "-");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarSubscript.gif");
        button.setProperty(PROPERTY_TIP, "Subscript");
        button.setProperty(PROPERTY_COMMAND, "Subscript");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarSuperscript.gif");
        button.setProperty(PROPERTY_TIP, "Superscript");
        button.setProperty(PROPERTY_COMMAND, "Superscript");
        buttons.add(button);
    }

    private void createListsButtons(PropertyListCollection buttons) {
        PropertyList button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarNumList.gif");
        button.setProperty(PROPERTY_TIP, "Numbered List");
        button.setProperty(PROPERTY_COMMAND, "InsertOrderedList");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarBulletList.gif");
        button.setProperty(PROPERTY_TIP, "Bulleted List");
        button.setProperty(PROPERTY_COMMAND, "InsertUnorderedList");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_TEXT, "-");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarOutdent.gif");
        button.setProperty(PROPERTY_TIP, "Outdent");
        button.setProperty(PROPERTY_COMMAND, "Outdent");
        buttons.add(button);
        button = new PropertyList();
        button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarIndent.gif");
        button.setProperty(PROPERTY_TIP, "Indent");
        button.setProperty(PROPERTY_COMMAND, "Indent");
        buttons.add(button);
    }

    private PropertyListCollection createButtons(int toolbartype) {
        PropertyListCollection buttons = new PropertyListCollection();
        switch (toolbartype) {
            case 9: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarNewPage.gif");
                button.setProperty(PROPERTY_TIP, "New Page");
                button.setProperty(PROPERTY_COMMAND, "$ManagePage");
                button.setProperty(PROPERTY_COMMANDOPTIONS, "New");
                button.setProperty(PROPERTY_SHORTCUTKEY, "n");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_TEXT, "-");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarEditPage.gif");
                button.setProperty(PROPERTY_TIP, "Edit Page");
                button.setProperty(PROPERTY_COMMAND, "$ManagePage");
                button.setProperty(PROPERTY_COMMANDOPTIONS, "Edit");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarDeletePage.gif");
                button.setProperty(PROPERTY_TIP, "Delete Page");
                button.setProperty(PROPERTY_COMMAND, "$ManagePage");
                button.setProperty(PROPERTY_COMMANDOPTIONS, "Delete");
                buttons.add(button);
                break;
            }
            case 0: {
                this.createFormatButtons(buttons);
                break;
            }
            case 2: {
                this.createClipboardButtons(buttons, false);
                break;
            }
            case 11: {
                this.createClipboardButtons(buttons, true);
                break;
            }
            case 1: {
                this.createAlignmentButtons(buttons);
                break;
            }
            case 4: {
                this.createListsButtons(buttons);
                break;
            }
            case 6: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertDiv.gif");
                button.setProperty(PROPERTY_TIP, "Insert Panel/Div");
                button.setProperty(PROPERTY_COMMAND, "$InsertDiv");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarPosition.gif");
                button.setProperty(PROPERTY_TIP, "Toggle Positioning For Selected Element");
                button.setProperty(PROPERTY_COMMAND, "AbsolutePosition");
                buttons.add(button);
                break;
            }
            case 7: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertTable.gif");
                button.setProperty(PROPERTY_TIP, "Insert Table");
                button.setProperty(PROPERTY_COMMAND, "$InsertTable");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarMergeRow.gif");
                button.setProperty(PROPERTY_TIP, "Merge Down");
                button.setProperty(PROPERTY_COMMAND, "$MergeTableRow");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarMergeCell.gif");
                button.setProperty(PROPERTY_TIP, "Merge Right");
                button.setProperty(PROPERTY_COMMAND, "$MergeTableCell");
                buttons.add(button);
                break;
            }
            case 5: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertForm.gif");
                button.setProperty(PROPERTY_TIP, "Insert Form");
                button.setProperty(PROPERTY_COMMAND, "$InsertForm");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertField.gif");
                button.setProperty(PROPERTY_TIP, "Insert Field");
                button.setProperty(PROPERTY_COMMAND, "InsertInputText");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertSelect.gif");
                button.setProperty(PROPERTY_TIP, "Insert Select Field");
                button.setProperty(PROPERTY_COMMAND, "InsertSelectDropdown");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertButton.gif");
                button.setProperty(PROPERTY_TIP, "Insert Button");
                button.setProperty(PROPERTY_COMMAND, "InsertButton");
                buttons.add(button);
                break;
            }
            case 3: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertLink.gif");
                button.setProperty(PROPERTY_TIP, "Insert Link");
                button.setProperty(PROPERTY_COMMAND, "$InsertLink");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarBreakLink.gif");
                button.setProperty(PROPERTY_TIP, "Break Link");
                button.setProperty(PROPERTY_COMMAND, "Unlink");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarInsertBookmark.gif");
                button.setProperty(PROPERTY_TIP, "Insert Bookmark");
                button.setProperty(PROPERTY_COMMAND, "$InsertBookmark");
                buttons.add(button);
                break;
            }
            case 10: {
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarImage.gif");
                button.setProperty(PROPERTY_TIP, "Insert Image");
                button.setProperty(PROPERTY_COMMAND, "$InsertImage");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarFrame.gif");
                button.setProperty(PROPERTY_TIP, "Insert IFrame");
                button.setProperty(PROPERTY_COMMAND, "$InsertIFrame");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarHorizontalRule.gif");
                button.setProperty(PROPERTY_TIP, "Insert Horizontal Rule");
                button.setProperty(PROPERTY_COMMAND, "InsertHorizontalRule");
                buttons.add(button);
                button = new PropertyList();
                button.setProperty(PROPERTY_IMAGE, "WEB-CORE/elements/richtext/images/gif/ToolbarGroupBox.gif");
                button.setProperty(PROPERTY_TIP, "Insert Group Box");
                button.setProperty(PROPERTY_COMMAND, "$InsertFieldset");
                buttons.add(button);
                break;
            }
            case 8: {
                this.createFontButtons(buttons, false);
                break;
            }
            case 12: {
                this.createFontButtons(buttons, true);
                break;
            }
            case 13: {
                this.createFormatButtons(buttons);
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_TEXT, "-");
                buttons.add(button);
                this.createFontButtons(buttons, true);
                break;
            }
            case 14: {
                this.createAlignmentButtons(buttons);
                PropertyList button = new PropertyList();
                button.setProperty(PROPERTY_TEXT, "-");
                buttons.add(button);
                this.createListsButtons(buttons);
                break;
            }
        }
        return buttons;
    }

    public void setViewOnly(String viewonly) {
        this.setViewOnly(viewonly.equalsIgnoreCase("y") || viewonly.equalsIgnoreCase("true") || viewonly.equalsIgnoreCase("yes"));
    }

    public void setMaximized(String maximized) {
        this.setMaximized(maximized.equalsIgnoreCase("y") || maximized.equalsIgnoreCase("true") || maximized.equalsIgnoreCase("yes"));
    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public void setViewOnly(boolean viewonly) {
        this.viewonly = viewonly;
    }

    public void setId(String id) {
        this.id = id;
        super.setElementid(id);
        if (this.userConfig != null) {
            this.setMaximized(this.userConfig.getProperty("richtexteditor_" + this.elementid + "_maximised", "n").equalsIgnoreCase("y"));
        }
    }

    @Override
    public void setElementid(String elementid) {
        this.setId(elementid);
    }

    public void setContent(String htmlcontent) {
        this.htmlcontent = htmlcontent;
        if ((this.htmlcontent.startsWith("<div") || this.htmlcontent.startsWith("<DIV")) && this.htmlcontent.contains("sapphire=\"page\"") && (this.htmlcontent.endsWith("</div>") || this.htmlcontent.endsWith("</DIV>"))) {
            this.sourceContainsPages = true;
            this.logger.debug("Content contains pages so switching to pagemode automatically.");
            this.setPageMode(true);
        } else {
            this.sourceContainsPages = false;
        }
    }

    public void setWidth(String width) {
        this.width = width.toLowerCase().endsWith("px") ? width.substring(0, width.length() - 2) : width;
    }

    public void setHeight(String height) {
        this.height = height.toLowerCase().endsWith("px") ? height.substring(0, height.length() - 2) : height;
    }

    public void setExpandable(String expandable) {
        this.expandable = expandable.equalsIgnoreCase("yes") || expandable.equalsIgnoreCase("y") || expandable.equalsIgnoreCase("true");
    }

    private JSONObject collectionToJSonObject(PropertyListCollection plc, String keyprop, String[] properties) {
        JSONObject ja = new JSONObject();
        for (int i = 0; i < plc.size(); ++i) {
            PropertyList pl = plc.getPropertyList(i);
            String keyval = pl.getProperty(keyprop, "");
            if (keyval.length() <= 0) continue;
            JSONObject job = new JSONObject();
            for (int k = 0; k < properties.length; ++k) {
                String prop = pl.getProperty(properties[k], "");
                try {
                    job.append(properties[k], prop);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            try {
                ja.put(keyval, job);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return ja;
    }

    private JSONArray collectionToJSonArray(PropertyListCollection plc, String[] properties) {
        JSONArray ja = new JSONArray();
        if (plc.size() != 0) {
            for (int i = 0; i < plc.size(); ++i) {
                PropertyList pl = plc.getPropertyList(i);
                JSONObject job = new JSONObject();
                if (properties == null || properties.length == 0) {
                    Iterator it = pl.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        if (pl.isCollection(key)) {
                            JSONArray jay = this.collectionToJSonArray(pl.getCollection(key), null);
                            try {
                                job.put(key, jay);
                            }
                            catch (Exception exception) {}
                            continue;
                        }
                        String prop = pl.getProperty(key, "");
                        try {
                            job.put(key, prop);
                        }
                        catch (Exception exception) {}
                    }
                } else {
                    for (int k = 0; k < properties.length; ++k) {
                        if (pl.isCollection(properties[k])) {
                            JSONArray jay = this.collectionToJSonArray(pl.getCollection(properties[k]), null);
                            try {
                                job.put(properties[k], jay);
                            }
                            catch (Exception exception) {}
                            continue;
                        }
                        String prop = pl.getProperty(properties[k], "");
                        try {
                            job.put(properties[k], prop);
                            continue;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                ja.put(job);
            }
        }
        return ja;
    }

    private void renderScript(StringBuffer content, String id, boolean viewonly, boolean maximized, boolean tabThrough, boolean showToolbar, PropertyListCollection customcommands, PropertyListCollection events, int sourcemode, boolean pageMode, PropertyList defaultPage, String defaultpageProperty, boolean sourceContainsPages) {
        Object ja;
        if (this.pageContext == null || this.pageContext.getRequest().getAttribute("richtext_rendered_mainscript") == null || this.pageContext.getRequest().getAttribute("richtext_rendered_mainscript") == Boolean.FALSE) {
            content.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/richtexteditor.js\"></script>");
            content.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/richtextcommands.js\"></script>");
            content.append("<textarea style=\"display:none;\" id=\"__richtext_clipboard\">").append(!this.browser.isIE() && this.userConfig != null ? this.userConfig.getProperty("richtexteditor_clipboard", "") : "").append("</textarea>");
            content.append("<script type=\"text/javascript\">");
            content.append("if (typeof(").append(JS_OBJECT).append(") == 'undefined'){");
            content.append("var ").append(JS_OBJECT).append(" = new RichTextEditor();");
            content.append(JS_OBJECT).append(".").append(JS_COMMANDOBJECT).append(" = new RichTextCommands();");
            content.append(JS_OBJECT).append(".debug = ").append(this.devMode).append(";");
            if (!this.browser.isIE() && this.userConfig != null && this.userConfig.getProperty("richtexteditor_clipboard", "").length() > 0) {
                content.append("try{");
                content.append(JS_OBJECT).append(".clipboard.populate(").append("__richtext_clipboard.value").append(");");
                content.append("}catch(_e1){}");
            }
            content.append("}");
            if (this.pageContext != null) {
                this.pageContext.getRequest().setAttribute("richtext_rendered_mainscript", (Object)Boolean.TRUE);
            }
        } else {
            content.append("<script type=\"text/javascript\">");
        }
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("']={};");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].source=false;");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].viewonly=").append(viewonly).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].pagedSource=").append(sourceContainsPages).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].toolbarvisible=").append(showToolbar).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].viewbarvisible=").append(showToolbar).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].sourcemode=").append(sourcemode).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].pagemode=").append(pageMode).append(";");
        if (defaultPage != null) {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].defaultpage=sapphire.util.propertyList.create(").append(defaultPage.toJSONString(false)).append(");");
        } else {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].defaultpage=sapphire.util.propertyList.create();");
        }
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].defaultpageproperty='").append(defaultpageProperty).append("';");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].maximized=").append(maximized).append(";");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].tabThrough=").append(tabThrough).append(";");
        if (customcommands != null) {
            ja = this.collectionToJSonObject(customcommands, PROPERTY_COMMAND, new String[]{PROPERTY_EXECUTEFUNCTION, PROPERTY_QUERYFUNCTION, PROPERTY_PERSISTENT});
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].customCommands=").append(((JSONObject)ja).toString()).append(";");
        } else {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].customCommands={};");
        }
        if (events != null) {
            ja = this.collectionToJSonArray(events, new String[]{PROPERTY_NAME, PROPERTY_ACTION});
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].events=").append(((JSONArray)ja).toString()).append(";");
        } else {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].events={};");
        }
        if (this.browser == null || this.browser.isIE() || this.browser.isChrome() || this.browser.isSafari()) {
            content.append("sapphire.events.attachEvent(window,'load',new Function('").append("richText.").append("enableDM(\\'").append(id).append("\\')'));");
        }
        content.append("</script>");
    }

    private void renderMenu(StringBuffer content, String id, PropertyListCollection menu) {
        content.append("<script type=\"text/javascript\">");
        if (menu != null && menu.size() > 0) {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].menu=sapphire.util.propertyListCollection.create(").append(menu.toJSONString()).append(");");
        } else {
            content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].menu=sapphire.util.propertyListCollection.create();");
        }
        content.append("</script>");
    }

    private void renderToolbar(StringBuffer content, String id, PropertyListCollection toolbars, boolean viewonly, boolean showtoolbar) {
        String display;
        JSONArray toolbarJson = new JSONArray();
        StringBuffer viewbarhtml = new StringBuffer();
        StringBuffer toolbarhtml = new StringBuffer();
        int vobar = -1;
        int tbar = -1;
        String string = showtoolbar ? (this.browser.isIE() && this.browser.getVersion() < 8.0 ? "block" : "table") : (display = "none");
        if (viewonly) {
            toolbarhtml.append("<table id=\"").append(id).append("_buttons\" style=\"display:none;").append(this.expandable && this.toolbarCollapse == ToolbarCollapse.ARROWS ? "width:100%;" : "").append("\" cellpadding=0 cellspacing=0 border=0>");
            viewbarhtml.append("<table id=\"").append(id).append("_vobuttons\" style=\"display:").append(display).append(";\" cellpadding=0 cellspacing=0 border=0>");
        } else {
            toolbarhtml.append("<table id=\"").append(id).append("_buttons\" style=\"display:").append(display).append(";").append(this.expandable && this.toolbarCollapse == ToolbarCollapse.ARROWS ? "width:100%;" : "").append("\" cellpadding=0 cellspacing=0 border=0>");
            viewbarhtml.append("<table id=\"").append(id).append("_vobuttons\" style=\"display:none;\" cellpadding=0 cellspacing=0 border=0>");
        }
        toolbarhtml.append("<tbody>");
        viewbarhtml.append("<tbody>");
        for (int i = 0; i < toolbars.size(); ++i) {
            int var;
            StringBuffer touse;
            PropertyList toolbar = toolbars.getPropertyList(i);
            boolean newLine = toolbar.getProperty(PROPERTY_NEWLINE, "N").equalsIgnoreCase("Y");
            boolean inviewmode = toolbar.getProperty(PROPERTY_INVIEWMODE, "N").equalsIgnoreCase("Y");
            if (inviewmode) {
                touse = viewbarhtml;
                var = ++vobar;
            } else {
                touse = toolbarhtml;
                var = ++tbar;
            }
            if (var == 0) {
                touse.append("<tr height=25><td>");
                touse.append("<table cellpadding=0 cellspacing=0>");
                touse.append("<tbody>");
                touse.append("<tr height=25><td>");
            } else if (newLine) {
                touse.append("</td></tr>");
                touse.append("</tbody>");
                touse.append("</table>");
                touse.append("</td></tr><tr height=25><td>");
                touse.append("<table cellpadding=0 cellspacing=0>");
                touse.append("<tbody>");
                touse.append("<tr height=25><td>");
            } else {
                touse.append("</td><td>");
            }
            String class1 = "richtext_toolbar_back_norm";
            PropertyListCollection buttons = toolbar.getCollection(PROPERTYLIST_BUTTONS);
            if (buttons == null || buttons.size() <= 0) continue;
            touse.append("<table cellpadding=0 cellspacing=0 id=\"").append(id).append("_toolbar").append(i).append("\" style=\"table-layout:fixed;\">");
            touse.append("<tbody>");
            touse.append("<tr height=25>");
            touse.append("<td class=\"richtext_toolbar_start\">");
            if (this.browser.isWebkit()) {
                touse.append("<div class=\"richtext_toolbar_start_fill\"></div>");
            }
            touse.append("</td>");
            JSONObject buttonJson = new JSONObject();
            try {
                buttonJson.put(PROPERTY_INVIEWMODE, inviewmode);
            }
            catch (Exception exception) {
                // empty catch block
            }
            for (int k = 0; k < buttons.size(); ++k) {
                PropertyList button = buttons.getPropertyList(k);
                String buttonId = button.getProperty(PROPERTY_ID, "button" + k);
                String buttonText = button.getProperty(PROPERTY_TEXT, "");
                String buttonTip = button.getProperty(PROPERTY_TIP, "");
                String buttonSrc = button.getProperty(PROPERTY_IMAGE, "");
                String buttonShortcutkey = button.getProperty(PROPERTY_SHORTCUTKEY, "");
                String buttonAction = button.getProperty(PROPERTY_ACTION, "");
                String buttonCommand = button.getProperty(PROPERTY_COMMAND, "");
                String buttonCommandOp = button.getProperty(PROPERTY_COMMANDOPTIONS, "");
                String buttonUndo = button.getProperty(PROPERTY_UNDOABLE, "Y");
                if (buttonText.length() > 0) {
                    if (buttonText.equalsIgnoreCase("-") || buttonText.equalsIgnoreCase("sep") || buttonText.equalsIgnoreCase("separator")) {
                        touse.append("<td align=center valign=middle class=\"").append("richtext_toolbar_sep").append("\">");
                        if (this.browser.isWebkit()) {
                            touse.append("<div class=\"richtext_toolbar_sep_fill\"></div>");
                        }
                        touse.append("</td>");
                        continue;
                    }
                    PropertyListCollection items = button.getCollection(PROPERTY_ITEMS);
                    if (items != null && items.size() > 0) {
                        int dw;
                        try {
                            dw = Integer.parseInt(button.getProperty(PROPERTY_DDWIDTH, "60"));
                        }
                        catch (Exception e) {
                            dw = 60;
                        }
                        int w = 45 + dw;
                        touse.append("<td style=\"").append(this.html5 ? "padding:0;" : "").append("").append("\" align=center valign=middle class=\"").append("richtext_toolbar_back").append("\">");
                        String btnId = id + "_" + i + "_" + buttonId;
                        touse.append("<table cellpadding=0 cellspacing=0 toolbar=\"").append(i).append("\" elementid=\"").append(id).append("\" buttonid=\"").append(buttonId).append("\" id=\"").append(btnId).append("\" src=\"").append(buttonSrc).append("\" title=\"").append(buttonTip).append("\" border=0 style=\"\">");
                        touse.append("<tbody>");
                        touse.append("<tr>");
                        touse.append("<td align=center style=\"padding-right:2px;vertical-align:top;padding-top: 0px\">");
                        touse.append(buttonText);
                        touse.append("</td>");
                        touse.append("<td align=center valign=middle style=\"width:1px;vertical-align:top;padding-top: 0px\">");
                        touse.append("<input readonly onkeydown=\"richText.dropDown").append(".").append(JS_DOWNKEYPRESS).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',document.getElementById('").append(btnId).append("_btn'),").append(dw).append(");\" onclick=\"richText.dropDown").append(".").append(JS_DODROPDOWNCLICK).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',document.getElementById('").append(btnId).append("_btn'),").append(dw).append(");\" class=\"richtext_dropdown\" style=\"").append("").append("width:").append(dw).append("px;\" id=\"").append(btnId).append("_select\">");
                        touse.append("</td>");
                        touse.append("<td align=right style=\"position:relative;width:18px;padding-top: ").append(this.browser.isIE() ? "0px" : "1px").append(";vertical-align:top;\">");
                        touse.append("<input type=image style=\"position:absolute;height:18px;font-size:").append(this.browser.isIE() ? "12px" : "12px").append(";display:block;\" id=\"").append(btnId).append("_btn\" onclick=\"richText.dropDown").append(".").append(JS_DODROPDOWNCLICK).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this,").append(dw).append(");\" src=\"WEB-CORE/elements/richtext/images/dropdown_norm.jpg\" onmouseover=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNMOUSEOVER).append("('").append(btnId).append("',this);\" onmouseout=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNMOUSEOUT).append("('").append(btnId).append("',this);\" onmousedown=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNMOUSEDOWN).append("('").append(btnId).append("',this);\" onmouseup=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNMOUSEUP).append("('").append(btnId).append("',this);\">");
                        touse.append("<div class=\"richtext_dropdownarea\" id=\"").append(btnId).append("_dd\" style=\"position:absolute;display:none;z-index:200;\">");
                        touse.append("<table cellpadding=0 cellspacing=0><tbody>");
                        for (int itemindex = 0; itemindex < items.size(); ++itemindex) {
                            PropertyList item = items.getPropertyList(itemindex);
                            String value = item.getProperty(PROPERTY_VALUE, "");
                            if (value.length() <= 0) continue;
                            String text = item.getProperty(PROPERTY_TEXT, value);
                            String html = item.getProperty(PROPERTY_HTML, text);
                            touse.append("<tr><td class=\"richtext_dropdownitem\" onclick=\"richText.dropDown").append(".").append(JS_DODROPDOWNITEMCLICK).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmouseover=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNITEMMOUSEOVER).append("(this);\" onmouseout=\"").append(JS_OBJECT).append(".").append(JS_DROPDOWNOBJECT).append(".").append(JS_DODROPDOWNITEMMOUSEOUT).append("(this);\" value=\"").append(value).append("\" text=\"").append(text).append("\">").append(html).append("</td></tr>");
                        }
                        touse.append("</tbody></table>");
                        touse.append("</div>");
                        touse.append("</td>");
                        touse.append("</tr>");
                        touse.append("</tbody>");
                        touse.append("</table>");
                        touse.append("</td>");
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "select");
                            jsonObject.put(PROPERTY_SHORTCUTKEY, "");
                            jsonObject.put(PROPERTY_ID, id + "_" + i + "_" + buttonId);
                            jsonObject.put("buttonid", buttonId);
                            jsonObject.put(PROPERTY_COMMAND, buttonCommand);
                            jsonObject.put(PROPERTY_COMMANDOPTIONS, buttonCommandOp);
                            jsonObject.put(PROPERTY_ACTION, buttonAction);
                            jsonObject.put(PROPERTY_UNDOABLE, buttonUndo);
                            jsonObject.put("checkstate", "N");
                            buttonJson.put(buttonId, jsonObject);
                        }
                        catch (Exception exception) {}
                        continue;
                    }
                    touse.append("<td style=\"width:75px;\" align=center valign=middle class=\"").append("richtext_toolbar_back").append("\" onmouseover=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEOVER).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmouseout=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEOUT).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmousedown=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEDOWN).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmouseup=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEUP).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\">");
                    touse.append("<table border=0 class=\"").append(class1).append("\" cellpadding=0 cellspacing=0 toolbar=\"").append(i).append("\" elementid=\"").append(id).append("\" buttonid=\"").append(buttonId).append("\" id=\"").append(id).append("_").append(i).append("_").append(buttonId).append("\" border=0 src=\"").append(buttonSrc).append("\" onclick=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNCLICK).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);").append("\" title=\"").append(buttonTip).append("\" style=\"width:20px;\">");
                    touse.append("<tbody>");
                    touse.append("<tr>");
                    if (buttonSrc.length() > 0) {
                        touse.append("<td align=\"center\" valign=\"middle\">");
                        touse.append("<img border=\"0\" src=\"").append(buttonSrc).append("\" title=\"").append(buttonTip).append("\" width=\"16\" height=\"16\">");
                        touse.append("</td>");
                    }
                    touse.append("<td align=\"center\" valign=\"middle\">");
                    touse.append(buttonText);
                    touse.append("</td>");
                    touse.append("</tr>");
                    touse.append("</tbody>");
                    touse.append("</table>");
                    touse.append("</td>");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", PROPERTY_TEXT);
                        jsonObject.put(PROPERTY_SHORTCUTKEY, buttonShortcutkey);
                        jsonObject.put(PROPERTY_ID, id + "_" + i + "_" + buttonId);
                        jsonObject.put("buttonid", buttonId);
                        jsonObject.put(PROPERTY_COMMAND, buttonCommand);
                        jsonObject.put(PROPERTY_COMMANDOPTIONS, buttonCommandOp);
                        jsonObject.put(PROPERTY_ACTION, buttonAction);
                        jsonObject.put(PROPERTY_UNDOABLE, buttonUndo);
                        jsonObject.put("checkstate", "N");
                        buttonJson.put(buttonId, jsonObject);
                    }
                    catch (Exception exception) {}
                    continue;
                }
                touse.append("<td style=\"").append(this.html5 ? "padding:0;" : "").append("width:23px;\" align=\"center\" valign=\"middle\" class=\"").append("richtext_toolbar_back").append("\" onmouseover=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEOVER).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmouseout=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEOUT).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmousedown=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEDOWN).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\" onmouseup=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNMOUSEUP).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);\">");
                touse.append("<table border=\"0\" class=\"").append(class1).append("\" cellpadding=0 cellspacing=0 toolbar=\"").append(i).append("\" elementid=\"").append(id).append("\" buttonid=\"").append(buttonId).append("\" id=\"").append(id).append("_").append(i).append("_").append(buttonId).append("\" border=0 src=\"").append(buttonSrc).append("\" onclick=\"").append(JS_OBJECT).append(".").append(JS_DOIMAGEBTNCLICK).append("('").append(id).append("',").append(i).append(",'").append(buttonId).append("',this);").append("\" title=\"").append(buttonTip).append("\">");
                touse.append("<tbody>");
                touse.append("<tr>");
                touse.append("<td>");
                touse.append("<img border=\"0\" src=\"").append(buttonSrc).append("\" title=\"").append(buttonTip).append("\" width=\"16\" height=\"16\">");
                touse.append("</td>");
                touse.append("</tr>");
                touse.append("</tbody>");
                touse.append("</table>");
                touse.append("</td>");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", PROPERTY_IMAGE);
                    jsonObject.put(PROPERTY_SHORTCUTKEY, buttonShortcutkey);
                    jsonObject.put(PROPERTY_ID, id + "_" + i + "_" + buttonId);
                    jsonObject.put("buttonid", buttonId);
                    jsonObject.put(PROPERTY_COMMAND, buttonCommand);
                    jsonObject.put(PROPERTY_COMMANDOPTIONS, buttonCommandOp);
                    jsonObject.put(PROPERTY_ACTION, buttonAction);
                    jsonObject.put(PROPERTY_UNDOABLE, buttonUndo);
                    jsonObject.put("checkstate", "N");
                    buttonJson.put(buttonId, jsonObject);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            try {
                toolbarJson.put(i, buttonJson);
            }
            catch (Exception exception) {
                // empty catch block
            }
            touse.append("<td class=\"richtext_toolbar_end\">");
            if (this.browser.isWebkit()) {
                touse.append("<div class=\"richtext_toolbar_end_fill\"></div>");
            }
            touse.append("</td>");
            touse.append("</tr>");
            touse.append("</tbody>");
            touse.append("</table>");
        }
        if (tbar > -1) {
            toolbarhtml.append("</td></tr>");
            toolbarhtml.append("</tbody>");
            toolbarhtml.append("</table>");
            toolbarhtml.append("</td>");
            if (this.expandable && this.toolbarCollapse == ToolbarCollapse.ARROWS) {
                toolbarhtml.append("<td onmouseover=\"this.style.border='solid 1px #030081';\" onmouseout=\"this.style.border = 'none';\" align=\"right\" style=\"cursor:pointer;padding-right:5px;padding-left:5px;\" onclick=\"").append(JS_OBJECT).append(".").append(JS_RESIZEMETHOD).append("('").append(id).append("');").append("\"><img id=\"").append(id).append("_buttons_tbimg\" src=\"WEB-CORE/elements/richtext/images/tb").append(viewonly ? "dn" : "up").append(".png\"></td>");
            }
            toolbarhtml.append("</tr>");
        }
        toolbarhtml.append("</tbody>");
        toolbarhtml.append("</table>");
        if (vobar > -1) {
            viewbarhtml.append("</td></tr>");
            viewbarhtml.append("</tbody>");
            viewbarhtml.append("</table>");
            viewbarhtml.append("</td>");
            if (this.expandable && this.toolbarCollapse == ToolbarCollapse.ARROWS) {
                viewbarhtml.append("<td onmouseover=\"this.style.border='solid 1px #030081';\" onmouseout=\"this.style.border = 'none';\" align=\"right\" style=\"cursor:pointer;padding-right:5px;padding-left:5px;\" onclick=\"").append(JS_OBJECT).append(".").append(JS_RESIZEMETHOD).append("('").append(id).append("');").append("\"><img id=\"").append(id).append("_vobuttons_tbimg\" src=\"WEB-CORE/elements/richtext/images/tb").append(viewonly ? "dn" : "up").append(".png\"></td>");
            }
            viewbarhtml.append("</tr>");
        }
        viewbarhtml.append("</tbody>");
        viewbarhtml.append("</table>");
        content.append(toolbarhtml);
        content.append(viewbarhtml);
        content.append("<script type=\"text/javascript\">");
        content.append(JS_OBJECT).append(".").append("data['").append(id).append("'].toolbar = ").append(toolbarJson.toString()).append(";");
        content.append("</script>");
    }

    @Override
    public String getHtml() {
        StringBuffer content = new StringBuffer();
        if (this.id.length() > 0) {
            if (this.browser == null || this.browser.isIE() || this.browser.isChrome() || this.browser.isSafari()) {
                PropertyListCollection customcommands = this.getCustomCommands();
                this.renderScript(content, this.id, this.viewonly, this.maximized, this.allowTabThrough, this.showtoolbar, customcommands, this.events, this.sourcemode, this.pagemode, this.defaultPage, this.defaultpageProperty, this.sourceContainsPages);
                PropertyListCollection menu = this.getContextMenu();
                if (menu != null) {
                    this.renderMenu(content, this.id, menu);
                }
                int pwidth = -1;
                try {
                    pwidth = Integer.parseInt(this.width);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                int pheight = -1;
                try {
                    pheight = Integer.parseInt(this.height);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                content.append("<table id=\"").append(this.id).append("_table\" width=\"").append(pwidth > -1 ? Integer.valueOf(pwidth) : this.width).append("\" height=\"").append(pheight > -1 ? Integer.valueOf(pheight) : this.height).append("\" style=\"width:").append(pwidth > -1 ? pwidth + "px" : this.width).append(";height:").append(pheight > -1 ? pheight + "px" : this.height).append(";border-top:solid 1px #B2B2A6;border-right:solid 1px #696969;border-left:solid 1px #696969;border-bottom:solid 1px #696969;\" cellpadding=0 cellspacing=0>");
                content.append("<tbody>");
                content.append("<tr height=5 class=\"").append(this.element.getProperty("appearance").equalsIgnoreCase("grey") ? "richtext_toolbar_grey" : "richtext_toolbar_blue").append("\" onmouseover=\"").append(JS_OBJECT).append(".").append(JS_MOUSEOVERTOOLBARMETHOD).append("('").append(this.id).append("');\" onmouseout=\"").append(JS_OBJECT).append(".").append(JS_MOUSEOUTTOOLBARMETHOD).append("('").append(this.id).append("');\" ondblclick=\"").append(JS_OBJECT).append(".").append(JS_DOUBLECLICKTOOLBARMETHOD).append("('").append(this.id).append("');\">");
                content.append("<td id=\"").append(this.id).append("_toolbar_cell\" valign=top class=\"richtext_toolbar_cell\">");
                PropertyListCollection toolbars = this.getToolbars();
                if (toolbars != null) {
                    this.renderToolbar(content, this.id, toolbars, this.viewonly, this.showtoolbar);
                }
                content.append("</td>");
                content.append("</tr>");
                if (this.expandable && this.toolbarCollapse == ToolbarCollapse.AUTOCOLLAPSE) {
                    content.append("<tr id=\"").append(this.id).append("_resize\" class=\"richtext_toolbar_resize\" style=\"cursor:n-resize;\" onclick=\"").append(JS_OBJECT).append(".").append(JS_RESIZEMETHOD).append("('").append(this.id).append("');\">");
                } else {
                    content.append("<tr id=\"").append(this.id).append("_resize\" onclick=\"\">");
                }
                content.append("<td style=\"height:2px;\">");
                content.append("");
                content.append("</td>");
                content.append("</tr>");
                if (pheight > -1) {
                    content.append("<tr style=\"height:").append(pheight - 10 - 2).append("px;\">");
                } else {
                    content.append("<tr>");
                }
                content.append("<td class=\"richtext_toolbar\" style=\"height:100%;\" valign=top>");
                content.append("<iframe style=\"display:block\" id=\"").append(this.id).append("_frame\" name=\"").append(this.id).append("_frame\" src=\"").append(this.browser.getBlankSrc()).append("\" frameborder=0 height=\"").append(pheight > -1 ? Integer.valueOf(pheight) : this.height).append("\" width=\"100%\">");
                content.append("</iframe>");
                content.append("<textarea style=\"display:none;width:100%;height:100%;\" id=\"").append(this.id).append("\" name=\"").append(this.id).append("\" >");
                content.append(RichTextEditor.escapeHTML(this.htmlcontent, false));
                content.append("</textarea>");
                content.append("</td>");
                content.append("</tr>");
                content.append("</tbody>");
                content.append("</table>");
            } else {
                this.renderScript(content, this.id, true, false, false, false, null, null, 0, false, null, "", false);
                content.append("<table id=\"").append(this.id).append("_table\"  cellpadding=0 cellspacing=0>");
                content.append("<tbody>");
                content.append("<tr>");
                content.append("<td style=\"color:red;\">");
                String t = "This feature is not fully supported in your browser.";
                content.append(this.tp != null ? this.tp.translate(t) : t);
                content.append("</td>");
                content.append("</tr>");
                content.append("<tr>");
                content.append("<td style=\"border-top:solid 1px #B2B2A6;border-right:solid 1px #696969;border-left:solid 1px #696969;border-bottom:solid 1px #696969;\">");
                content.append("<div id=\"").append(this.id).append("_noniediv\">");
                content.append(this.htmlcontent);
                content.append("</div>");
                content.append("<textarea style=\"display:none;width:100%;height:100%;\" id=\"").append(this.id).append("\" name=\"").append(this.id).append("\" >");
                content.append(RichTextEditor.escapeHTML(this.htmlcontent, false));
                content.append("</textarea>");
                content.append("</td>");
                content.append("</tr>");
                content.append("</tbody>");
                content.append("</table>");
            }
        } else {
            this.debugErrorMsg = "No Id or ElementId provided.";
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            return "<font style=\"color:red\">" + this.debugErrorMsg + "</font>";
        }
        return content.toString();
    }

    public static String escapeHTML(String html, boolean full) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < html.length(); ++i) {
            result.append(RichTextEditor.escapeHTML(html.charAt(i), full));
        }
        return result.toString();
    }

    public static String escapeHTML(char character, boolean full) {
        switch (character) {
            case '<': {
                return "&lt;";
            }
            case '>': {
                return "&gt;";
            }
            case '&': {
                return "&amp;";
            }
        }
        if (full) {
            switch (character) {
                case ' ': {
                    return "&nbsp;";
                }
                case '\"': {
                    return "&quot;";
                }
                case '\u00e0': {
                    return "&agrave;";
                }
                case '\uc380': {
                    return "&Agrave;";
                }
                case '\u00e2': {
                    return "&acirc;";
                }
                case '\uc382': {
                    return "&Acirc;";
                }
                case '\u00e4': {
                    return "&auml;";
                }
                case '\u00c4': {
                    return "&Auml;";
                }
                case '\u00e5': {
                    return "&aring;";
                }
                case '\u00c5': {
                    return "&Aring;";
                }
                case '\u00e6': {
                    return "&aelig;";
                }
                case '\u00c6': {
                    return "&AElig;";
                }
                case '\u00e7': {
                    return "&ccedil;";
                }
                case '\u00c7': {
                    return "&Ccedil;";
                }
                case '\u00e9': {
                    return "&eacute;";
                }
                case '\u00c9': {
                    return "&Eacute;";
                }
                case '\u00e8': {
                    return "&egrave;";
                }
                case '\uc388': {
                    return "&Egrave;";
                }
                case '\u00ea': {
                    return "&ecirc;";
                }
                case '\uc38a': {
                    return "&Ecirc;";
                }
                case '\u00eb': {
                    return "&euml;";
                }
                case '\uc38b': {
                    return "&Euml;";
                }
                case '\u00ef': {
                    return "&iuml;";
                }
                case '\uc38f': {
                    return "&Iuml;";
                }
                case '\u00f4': {
                    return "&ocirc;";
                }
                case '\uc394': {
                    return "&Ocirc;";
                }
                case '\u00f6': {
                    return "&ouml;";
                }
                case '\u00d6': {
                    return "&Ouml;";
                }
                case '\uc3b8': {
                    return "&oslash;";
                }
                case '\uc398': {
                    return "&Oslash;";
                }
                case '\u00df': {
                    return "&szlig;";
                }
                case '\u00f9': {
                    return "&ugrave;";
                }
                case '\uc399': {
                    return "&Ugrave;";
                }
                case '\u00fb': {
                    return "&ucirc;";
                }
                case '\uc39b': {
                    return "&Ucirc;";
                }
                case '\u00fc': {
                    return "&uuml;";
                }
                case '\u00dc': {
                    return "&Uuml;";
                }
                case '\uc2ae': {
                    return "&reg;";
                }
                case '\uc2a9': {
                    return "&copy;";
                }
                case '\u20ac': {
                    return "&euro;";
                }
            }
            return "" + character;
        }
        return new String("" + character);
    }

    public static String convertToXHTML(String rawHTML) {
        return RichTextEditor.convertToXHTML(rawHTML, false, false);
    }

    public static String convertToXHTML(String rawHTML, boolean useXeces) {
        return RichTextEditor.convertToXHTML(rawHTML, false, useXeces);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Document getHTMLDocument(String rawHTML) {
        byte[] htmlbytes;
        Tidy tidy = new Tidy();
        Document dom = null;
        try {
            htmlbytes = rawHTML.getBytes("UTF-8");
        }
        catch (Exception e) {
            Logger.logInfo("Could not read html as UFT");
            htmlbytes = rawHTML.getBytes();
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(htmlbytes);){
            tidy.setOnlyErrors(true);
            try {
                tidy.setInputEncoding("UTF-8");
                tidy.setOutputEncoding("UTF-8");
            }
            catch (Exception e) {
                Logger.logError("Tidy encoding failed with message: " + e.getMessage());
            }
            tidy.setErrout(new PrintWriter(new ByteArrayOutputStream()));
            dom = tidy.parseDOM((InputStream)byteArrayInputStream, null);
        }
        catch (Exception e1) {
            Logger.logError("Tidy parsing failed with message: " + e1.getMessage());
        }
        return dom;
    }

    public static String convertToXHTML(String rawHTML, boolean printable, boolean useXeces) {
        String out = "";
        if (rawHTML.length() > 0) {
            rawHTML = StringUtil.replaceAll(rawHTML, "</IMG>", "", false);
            rawHTML = StringUtil.replaceAll(rawHTML, " draggable=\"true\"", "", false);
            rawHTML = StringUtil.replaceAll(rawHTML, "<TR></TR>", "<!-- EMPTYROWPLACEHOLDER -->", false);
            out = RichTextEditor.serializeDocument(RichTextEditor.getHTMLDocument(rawHTML), printable, useXeces);
            out = StringUtil.replaceAll(out, "<!--  EMPTYROWPLACEHOLDER  -->", "<tr></tr>", false);
        } else {
            Logger.logInfo("Empty html provided.");
        }
        return out;
    }

    public static String serializeDocument(Document doc, boolean useXeces) {
        return RichTextEditor.serializeDocument(doc, false, useXeces);
    }

    public static String serializeDocument(Document doc, boolean printable, boolean useXeces) {
        Element docEl = doc.getDocumentElement();
        NodeList children = docEl.getChildNodes();
        if (children != null && children.getLength() == 2) {
            Node node = children.item(1);
            String out = RichTextEditor.serializeNode(node, printable, false, true, useXeces);
            if (useXeces && out.startsWith("<body>")) {
                out = out.substring(6, out.length() - 7);
            }
            return out;
        }
        Logger.logError("Created DOM does not contain head and body.");
        return "";
    }

    public static String serializeNode(Node node, boolean useFormatting, boolean excludeRoot, boolean useXeces) {
        return RichTextEditor.serializeNode(node, false, useFormatting, excludeRoot, useXeces);
    }

    public static String serializeNode(Node node, boolean printable, boolean useFormatting, boolean excludeRoot, boolean useXeces) {
        if (!useXeces) {
            StringBuffer xml = new StringBuffer();
            RichTextEditor.serializeNode(node, xml, printable, useFormatting, excludeRoot, " ", "", 0);
            return xml.toString();
        }
        return RichTextEditor.serializeNode(node);
    }

    public static String serializeNode(Node node, boolean useFormatting, boolean excludeRoot) {
        return RichTextEditor.serializeNode(node, useFormatting, excludeRoot, false);
    }

    private static void serializeNode(Node node, StringBuffer xml, boolean printable, boolean useFormatting, boolean excludeRoot, String indentString, String currentIndent, int level) {
        switch (node.getNodeType()) {
            case 9: {
                NodeList nodes;
                Document doc = (Document)node;
                if (level > 0 || !excludeRoot) {
                    xml.append("<?xml version=\"");
                    xml.append(doc.getXmlVersion());
                    xml.append("\" encoding=\"UTF-8\" standalone=\"");
                    if (doc.getXmlStandalone()) {
                        xml.append("yes");
                    } else {
                        xml.append("no");
                    }
                    xml.append("\"?>");
                    if (useFormatting) {
                        xml.append("\n");
                    }
                }
                if ((nodes = node.getChildNodes()) == null) break;
                for (int i = 0; i < nodes.getLength(); ++i) {
                    RichTextEditor.serializeNode(nodes.item(i), xml, printable, useFormatting, excludeRoot, indentString, currentIndent, level + 1);
                }
                break;
            }
            case 1: {
                String name = node.getNodeName();
                NodeList children = node.getChildNodes();
                if (printable) {
                    if (name.equalsIgnoreCase("strong")) {
                        name = "b";
                    } else if (name.equalsIgnoreCase("em")) {
                        name = "i";
                    }
                }
                if (level > 0 || !excludeRoot) {
                    if (useFormatting) {
                        xml.append(currentIndent);
                    }
                    xml.append("<").append(name);
                    NamedNodeMap attributes = node.getAttributes();
                    for (int i = 0; i < attributes.getLength(); ++i) {
                        Node current = attributes.item(i);
                        xml.append(" ").append(current.getNodeName()).append("=\"");
                        xml.append(current.getNodeValue());
                        xml.append("\"");
                    }
                    if (children == null || children.getLength() == 0) {
                        if (name.equalsIgnoreCase("br") || name.equalsIgnoreCase("hr")) {
                            xml.append("/");
                        } else {
                            xml.append("></").append(name);
                        }
                    }
                    xml.append(">");
                }
                if (children == null || children.getLength() <= 0) break;
                if (useFormatting && children.item(0) != null && children.item(0).getNodeType() == 1) {
                    xml.append("\n");
                }
                for (int i = 0; i < children.getLength(); ++i) {
                    RichTextEditor.serializeNode(children.item(i), xml, printable, useFormatting, excludeRoot, indentString, currentIndent + indentString, level + 1);
                }
                if (useFormatting && children.item(0) != null && children.item(children.getLength() - 1).getNodeType() == 1) {
                    xml.append(currentIndent);
                }
                if (level <= 0 && excludeRoot) break;
                xml.append("</").append(name).append(">");
                if (!useFormatting) break;
                xml.append("\n");
                break;
            }
            case 3: {
                xml.append(RichTextEditor.escapeHTML(node.getNodeValue(), true));
                break;
            }
            case 6: {
                break;
            }
            case 4: {
                xml.append("CDATA");
                xml.append(node.getNodeValue());
                xml.append("");
                break;
            }
            case 8: {
                if (useFormatting) {
                    xml.append(currentIndent);
                }
                xml.append("<!-- ").append(node.getNodeValue()).append(" -->");
                if (!useFormatting) break;
                xml.append("\n");
                break;
            }
            case 7: {
                xml.append("<?").append(node.getNodeName()).append(" ").append(node.getNodeValue()).append("?>");
                if (!useFormatting) break;
                xml.append("\n");
                break;
            }
            case 5: {
                xml.append("&").append(node.getNodeName()).append(";");
                break;
            }
            case 10: {
                DocumentType docType = (DocumentType)node;
                String publicId = docType.getPublicId();
                String systemId = docType.getSystemId();
                String internalSubset = docType.getInternalSubset();
                xml.append("<!DOCTYPE ").append(docType.getName());
                if (publicId != null) {
                    xml.append(" PUBLIC \"").append(publicId).append("\" ");
                } else {
                    xml.append(" SYSTEM ");
                }
                xml.append("\"").append(systemId).append("\"");
                if (internalSubset != null) {
                    xml.append(" [").append(internalSubset).append("]");
                }
                xml.append(">");
                if (!useFormatting) break;
                xml.append("\n");
            }
        }
    }

    private static String serializeNode(Node node) {
        String out = "";
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputFormat outputformat = new OutputFormat();
            outputformat.setIndent(0);
            outputformat.setIndenting(false);
            outputformat.setPreserveSpace(false);
            outputformat.setOmitDocumentType(true);
            outputformat.setOmitXMLDeclaration(true);
            outputformat.setStandalone(true);
            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputFormat(outputformat);
            serializer.setOutputByteStream((OutputStream)stream);
            serializer.asDOMSerializer();
            if (node instanceof Element) {
                serializer.serialize((Element)node);
                out = stream.toString();
            } else {
                Logger.logError("Node provided cannot be cast to Element.");
            }
        }
        catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return out;
    }

    public static enum ToolbarCollapse {
        AUTOCOLLAPSE,
        ARROWS;

    }
}

