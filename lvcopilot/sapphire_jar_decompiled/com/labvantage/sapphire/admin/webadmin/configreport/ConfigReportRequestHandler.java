/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.webadmin.configreport.BaseConfigReport;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportButtonList;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportButtonRoles;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportHtml;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportXml;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.RequestService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ConfigReportRequestHandler
extends PropertyHandler {
    public static final String WEBPAGELIST = "webpagelist";
    public static final String PRODUCTEDITIONLIST = "producteditionlist";
    public static final String IMAGEROOT = "imageroot";
    public static final String FILENAME = "filename";
    public static final String FOLDERLOCATION = "folderlocation";
    public static final String OPTION_INCLUDEPAGEDETAILS = "option_includepagedetails";
    public static final String OPTION_INCLUDETOOLBARBUTTONS = "option_includebuttons";
    public static final String OPTION_INCLUDELISTCOLUMNS = "option_includelistcolumns";
    public static final String OPTION_INCLUDEMAINTCOLUMNS = "option_includemaintcolumns";
    public static final String OPTION_REPORTTYPE = "option_reporttype";
    public static final String OPTION_COPYIMAGES = "option_copyimages";
    public static final String OPTION_REPORTSTYLES = "option_styles";
    public static final String OPTION_REPORTTITLE = "option_reporttitle";
    public static final String REPORT_TYPE_XML = "xml";
    public static final String REPORT_TYPE_HTML = "html";
    public static final String REPORT_TYPE_BUTTONLIST = "buttonlist";
    public static final String REPORT_TYPE_BUTTONROLES = "buttonroles";
    public static final String RETURN_FILEPATH = "return_filepath";
    protected boolean includePageDetails;
    protected boolean includeButtons;
    protected boolean includeListColumns;
    protected boolean includeMaintColumns;
    protected boolean copyImages;
    protected String imageRoot;
    protected String sourceImageRoot;
    protected String reportStyles;
    protected String reportTitle;
    protected String filename;
    protected String folderLocation;

    @Override
    public void processProperties(HashMap map) throws SapphireException {
        DBUtil database = new DBUtil();
        try {
            database.setConnection(this.sapphireConnection);
            this.loadOptions(map);
            File folderLocationFile = new File(this.folderLocation);
            if (!folderLocationFile.exists()) {
                folderLocationFile.mkdirs();
            }
            if (this.copyImages) {
                File imageFolder = new File(this.folderLocation + "images");
                imageFolder.mkdirs();
            }
            BaseConfigReport report = this.getReportRenderer(map);
            report.initialize(this.folderLocation, this.filename, this.imageRoot, this.reportTitle, this.reportStyles);
            report.beginReport();
            String pageList = (String)map.get(WEBPAGELIST);
            String editionList = (String)map.get(PRODUCTEDITIONLIST);
            String[] pageids = StringUtil.split(pageList, ";");
            String[] editions = StringUtil.split(editionList, ";");
            RequestService requestService = new RequestService(this.sapphireConnection);
            for (int i = 0; i < pageids.length; ++i) {
                String webpageid = pageids[i];
                String productedition = editions[i];
                WebPage webpage = new WebPage();
                webpage.webpageid = webpageid;
                webpage.productedition = productedition;
                if (this.includePageDetails) {
                    database.createPreparedResultSet("pages", "SELECT * FROM webpage WHERE webpageid=? AND productedition=?", new String[]{webpageid, productedition});
                    if (database.getNext("pages")) {
                        webpage.description = database.getString("pages", "webpagedesc");
                        if (webpage.description == null) {
                            webpage.description = "";
                        }
                        webpage.virtualpage = "Y".equals(database.getString("pages", "virtualpageflag"));
                        webpage.expresspage = "Y".equals(database.getString("pages", "expresspageflag"));
                        database.createPreparedResultSet("roles", "SELECT roleid FROM sdirole WHERE sdcid='WebPage' and keyid1=?", new String[]{webpageid});
                        while (database.getNext("roles")) {
                            webpage.rolelist = webpage.rolelist + ", " + database.getString("roles", "roleid");
                        }
                        if (webpage.rolelist.length() > 0) {
                            webpage.rolelist = webpage.rolelist.substring(2);
                        }
                        database.closeStatement("roles");
                        database.createPreparedResultSet("pagetype", "SELECT propertytreeid FROM webpagepropertytree WHERE webpageid=? AND productedition=? AND elementid='pagedata'", new String[]{webpageid, productedition});
                        if (database.getNext("pagetype")) {
                            webpage.pagetype = database.getString("pagetype", "propertytreeid");
                        } else {
                            String location = database.getString("pages", "location");
                            webpage.pagetype = "Custom (" + (location == null ? "" : location) + database.getString("pages", FILENAME) + ")";
                        }
                        database.closeStatement("pagetype");
                    }
                    database.closeStatement("pages");
                }
                report.nextWebPage(webpage, this.includePageDetails);
                PropertyList dummy = new PropertyList();
                PropertyList pageProps = requestService.getWebPageProperties(webpageid, productedition, dummy);
                if (this.includeButtons) {
                    PropertyList toolbar = pageProps.getPropertyListNotNull("advancedtoolbar");
                    this.processToolbarButtons(toolbar, report, webpageid, productedition);
                }
                if (this.includeListColumns) {
                    PropertyList list = pageProps.getPropertyListNotNull("list");
                    this.processListColumns(list, report, webpageid, productedition);
                }
                if (!this.includeMaintColumns) continue;
                PropertyList maint = pageProps.getPropertyListNotNull("maint");
                this.processMaintColumns(maint, report, webpageid, productedition);
            }
            this.writeToFile(map, report);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate report: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
        finally {
            database.reset();
        }
    }

    private void loadOptions(HashMap map) {
        this.includePageDetails = "Y".equals(map.get(OPTION_INCLUDEPAGEDETAILS));
        this.includeButtons = "Y".equals(map.get(OPTION_INCLUDETOOLBARBUTTONS));
        this.includeListColumns = "Y".equals(map.get(OPTION_INCLUDELISTCOLUMNS));
        this.includeMaintColumns = "Y".equals(map.get(OPTION_INCLUDEMAINTCOLUMNS));
        this.copyImages = "Y".equals(map.get(OPTION_COPYIMAGES));
        this.reportStyles = (String)map.get(OPTION_REPORTSTYLES);
        this.reportTitle = (String)map.get(OPTION_REPORTTITLE);
        this.filename = (String)map.get(FILENAME);
        this.folderLocation = (String)map.get(FOLDERLOCATION);
        this.folderLocation = StringUtil.replaceAll(this.folderLocation, "\\", "/");
        if (this.folderLocation.charAt(this.folderLocation.length() - 1) != '/') {
            this.folderLocation = this.folderLocation + '/';
        }
        this.sourceImageRoot = (String)map.get(IMAGEROOT);
        this.sourceImageRoot = StringUtil.replaceAll(this.sourceImageRoot, "\\", "/");
        if (this.sourceImageRoot.charAt(this.sourceImageRoot.length() - 1) != '/') {
            this.sourceImageRoot = this.sourceImageRoot + '/';
        }
        this.imageRoot = this.copyImages ? "images/" : this.sourceImageRoot;
    }

    private BaseConfigReport getReportRenderer(HashMap map) throws SapphireException {
        BaseConfigReport report;
        String reportType = (String)map.get(OPTION_REPORTTYPE);
        if (reportType.equals(REPORT_TYPE_HTML)) {
            report = new ConfigReportHtml();
        } else if (reportType.equals(REPORT_TYPE_XML)) {
            report = new ConfigReportXml();
        } else if (reportType.equals(REPORT_TYPE_BUTTONLIST)) {
            report = new ConfigReportButtonList();
        } else if (reportType.equals(REPORT_TYPE_BUTTONROLES)) {
            report = new ConfigReportButtonRoles();
        } else {
            throw new SapphireException("Unrecognized report type");
        }
        return report;
    }

    private void writeToFile(HashMap map, BaseConfigReport report) throws IOException {
        String fileExtension = report.getFileExtension();
        String filePath = this.folderLocation + this.filename + "." + fileExtension;
        File exportFile = new File(this.folderLocation + this.filename + "." + fileExtension);
        FileOutputStream fos = null;
        PrintStream out = null;
        fos = new FileOutputStream(exportFile);
        out = new PrintStream((OutputStream)fos, true, "UTF-8");
        map.put(RETURN_FILEPATH, filePath);
        String output = report.getFinalOutput();
        out.println(output);
        out.close();
        fos.close();
    }

    private void processToolbarButtons(PropertyList toolbar, BaseConfigReport report, String webpageid, String productedition) throws Exception {
        if (report.wantsButtons()) {
            PropertyListCollection buttons = toolbar.getCollectionNotNull("buttons");
            boolean hasButtons = false;
            for (Comparable button : buttons) {
                PropertyList common = button.getPropertyListNotNull("commonprops");
                hasButtons |= !common.getProperty("show").equals("N");
            }
            report.beginButtons(hasButtons);
            if (hasButtons) {
                Iterator iterator = buttons.iterator();
                while (iterator.hasNext()) {
                    Comparable button;
                    button = new Button();
                    PropertyList buttonProps = (PropertyList)iterator.next();
                    PropertyList commonProps = buttonProps.getPropertyListNotNull("commonprops");
                    String show = commonProps.getProperty("show");
                    if (show.equals("N")) continue;
                    ((Button)button).id = buttonProps.getProperty("id");
                    ((Button)button).type = buttonProps.getProperty("buttontype", "Unknown");
                    ((Button)button).text = commonProps.getProperty("text");
                    ((Button)button).image = commonProps.getProperty("image");
                    ((Button)button).rolelist = buttonProps.getAttribute("rolelist");
                    ((Button)button).webpageid = webpageid;
                    ((Button)button).productedition = productedition;
                    if (((Button)button).type.equals("Standard")) {
                        PropertyList standard = buttonProps.getPropertyListNotNull("standardbuttonprops");
                        String action = standard.getProperty("action");
                        String page = standard.getProperty("page");
                        ((Button)button).operation = "Function: " + action;
                        if (page.length() > 0) {
                            ((Button)button).operation = ((Button)button).operation + " (Using " + page + ")";
                        }
                    } else if (((Button)button).type.equals("Action")) {
                        PropertyList actionprops = buttonProps.getPropertyListNotNull("actionbuttonprops");
                        PropertyListCollection actions = actionprops.getCollectionNotNull("actions");
                        StringBuffer actionList = new StringBuffer();
                        for (PropertyList action : actions) {
                            actionList.append(", ").append(action.getProperty("actionid"));
                        }
                        ((Button)button).operation = "Actions: " + (actionList.length() > 0 ? actionList.substring(2) : "None");
                    } else if (((Button)button).type.equals("User")) {
                        PropertyList user = buttonProps.getPropertyListNotNull("userbuttonprops");
                        String javascript = user.getProperty("action");
                        ((Button)button).operation = "JavaScript: " + javascript;
                    }
                    if (this.copyImages && ((Button)button).image.length() > 0) {
                        this.copyFile(new File(this.sourceImageRoot + ((Button)button).image), new File(this.folderLocation + "images/" + ((Button)button).image));
                    }
                    report.nextButton((Button)button);
                }
            }
            report.endButtons(hasButtons);
        }
    }

    private void processListColumns(PropertyList list, BaseConfigReport report, String webpageid, String productedition) throws Exception {
        if (report.wantsListColumns()) {
            PropertyListCollection columns = list.getCollectionNotNull("columns");
            boolean hasColumns = false;
            for (PropertyList column : columns) {
                hasColumns |= !column.getProperty("mode").equals("Hidden Value");
            }
            report.beginListColumns(hasColumns);
            if (hasColumns) {
                String sdcid = list.getProperty("sdcid");
                Iterator iterator = columns.iterator();
                while (iterator.hasNext()) {
                    ListColumn column = new ListColumn();
                    PropertyList columnProps = (PropertyList)iterator.next();
                    PropertyList linkProps = columnProps.getPropertyListNotNull("link");
                    String mode = columnProps.getProperty("mode");
                    if (mode.equals("Hidden Value")) continue;
                    column.sdcid = sdcid;
                    column.id = columnProps.getProperty("id");
                    column.columnid = columnProps.getProperty("columnid");
                    column.title = columnProps.getProperty("title");
                    column.rolelist = columnProps.getAttribute("rolelist");
                    column.webpageid = webpageid;
                    column.productedition = productedition;
                    String link = linkProps.getProperty("href");
                    if (link.trim().length() == 0) {
                        column.link = "";
                    } else {
                        int amp;
                        String target = linkProps.getProperty("target");
                        boolean popup = target.trim().length() == 0 || target.trim().equalsIgnoreCase("_self");
                        int pagePos = link.toLowerCase().indexOf("page=");
                        int filePos = link.toLowerCase().indexOf("file=");
                        if (link.trim().toLowerCase().startsWith("javascript")) {
                            column.link = "JavaScript";
                        } else if (pagePos >= 0) {
                            amp = link.indexOf("&", pagePos);
                            column.link = "Page: " + (amp == -1 ? link.substring(pagePos + 5) : link.substring(pagePos + 5, amp));
                            if (popup) {
                                column.link = column.link + " (Popup)";
                            }
                        } else if (filePos >= 0) {
                            amp = link.indexOf("&", filePos);
                            column.link = "File: " + (amp == -1 ? link.substring(filePos + 5) : link.substring(filePos + 5, amp));
                            if (popup) {
                                column.link = column.link + " (Popup)";
                            }
                        } else {
                            column.link = "Unrecognized link";
                            if (popup) {
                                column.link = column.link + " (Popup)";
                            }
                        }
                    }
                    report.nextListColumn(column);
                }
            }
            report.endListColumns(hasColumns);
        }
    }

    private void processMaintColumns(PropertyList maint, BaseConfigReport report, String webpageid, String productedition) throws Exception {
        if (report.wantsMaintColumns()) {
            PropertyListCollection columns = maint.getCollectionNotNull("columns");
            boolean hasColumns = false;
            for (PropertyList column : columns) {
                hasColumns |= !column.getProperty("mode").equals("hidden");
            }
            report.beginMaintColumns(hasColumns);
            if (hasColumns) {
                String sdcid = maint.getProperty("sdcid");
                Iterator iterator = columns.iterator();
                while (iterator.hasNext()) {
                    MaintColumn column = new MaintColumn();
                    PropertyList columnProps = (PropertyList)iterator.next();
                    PropertyList linkProps = columnProps.getPropertyListNotNull("link");
                    String mode = columnProps.getProperty("mode");
                    if (mode.equals("hidden")) continue;
                    column.sdcid = sdcid;
                    column.id = columnProps.getProperty("id");
                    column.columnid = columnProps.getProperty("columnid");
                    column.title = columnProps.getProperty("title");
                    column.rolelist = columnProps.getAttribute("rolelist");
                    column.webpageid = webpageid;
                    column.productedition = productedition;
                    column.mode = mode;
                    column.defaultvalue = columnProps.getProperty("defaultvalue");
                    column.validationrule = columnProps.getProperty("validation");
                    report.nextMaintColumn(column);
                }
            }
            report.endMaintColumns(hasColumns);
        }
    }

    protected void copyFile(File in, File out) throws Exception {
        if (in.exists()) {
            out.getParentFile().mkdirs();
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out).getChannel();
            sourceChannel.transferTo(0L, sourceChannel.size(), destinationChannel);
            sourceChannel.close();
            destinationChannel.close();
        }
    }

    class MaintColumn
    implements Comparable {
        public String sdcid = "";
        public String id = "";
        public String columnid = "";
        public String title = "";
        public String mode = "";
        public String defaultvalue = "";
        public String validationrule = "";
        public String rolelist = "";
        public String webpageid = "";
        public String productedition = "";

        MaintColumn() {
        }

        public int compareTo(Object column) {
            return this.columnid.compareTo(((ListColumn)column).columnid);
        }
    }

    class ListColumn
    implements Comparable {
        public String sdcid = "";
        public String id = "";
        public String columnid = "";
        public String title = "";
        public String link = "";
        public String rolelist = "";
        public String webpageid = "";
        public String productedition = "";

        ListColumn() {
        }

        public int compareTo(Object column) {
            return this.columnid.compareTo(((ListColumn)column).columnid);
        }
    }

    class Button
    implements Comparable {
        public String id = "";
        public String type = "";
        public String text = "";
        public String image = "";
        public String operation = "";
        public String rolelist = "";
        public String webpageid = "";
        public String productedition = "";

        Button() {
        }

        public int compareTo(Object button) {
            return this.text.compareTo(((Button)button).text);
        }
    }

    class WebPage {
        public String webpageid = "";
        public String productedition = "";
        public String description = "";
        public String pagetype = "";
        public boolean virtualpage;
        public boolean expresspage;
        public String rolelist = "";

        WebPage() {
        }
    }
}

