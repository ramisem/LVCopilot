/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Document
 *  com.aspose.words.Comment
 *  com.aspose.words.CommentRangeEnd
 *  com.aspose.words.CommentRangeStart
 *  com.aspose.words.ConvertUtil
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBase
 *  com.aspose.words.DocumentBuilder
 *  com.aspose.words.FindReplaceOptions
 *  com.aspose.words.Font
 *  com.aspose.words.HtmlSaveOptions
 *  com.aspose.words.List
 *  com.aspose.words.Node
 *  com.aspose.words.PageSetup
 *  com.aspose.words.PdfSaveOptions
 *  com.aspose.words.PreferredWidth
 *  com.aspose.words.RowFormat
 *  com.aspose.words.SaveFormat
 *  com.aspose.words.SaveOptions
 *  com.aspose.words.Section
 *  com.aspose.words.Style
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.eln;

import com.aspose.words.Comment;
import com.aspose.words.CommentRangeEnd;
import com.aspose.words.CommentRangeStart;
import com.aspose.words.ConvertUtil;
import com.aspose.words.Document;
import com.aspose.words.DocumentBase;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.Font;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.List;
import com.aspose.words.Node;
import com.aspose.words.PageSetup;
import com.aspose.words.PdfSaveOptions;
import com.aspose.words.PreferredWidth;
import com.aspose.words.RowFormat;
import com.aspose.words.SaveFormat;
import com.aspose.words.SaveOptions;
import com.aspose.words.Section;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.DigitalSignatureRectangle;
import com.labvantage.sapphire.modules.eln.DocumentUtil;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WordWorksheet
extends Worksheet
implements ELNConstants {
    private String worksheetid;
    private String worksheetversionid;
    private Document document = null;
    DocumentBuilder builder = null;
    private PropertyList policy;
    private PropertyList exportOptions;
    private boolean isFirstSection = true;
    private ArrayList<String> bookmarks = new ArrayList();
    static int BOOKMARK_LEVEL1 = 0;
    static int BOOKMARK_ALLLEVELS = 1;
    static int BOOKMARK_ALLLEVELS_CAPTIONS = 2;
    private ExportProperties exportProps;
    private PageProperties pageProps;
    private HeaderFooterProperties headerfooterProps;
    private TitlePageProperties titlePageProps;
    private SectionProperties sectionProps;
    private TocProperties tocProps;
    private PDFProperties pdfProps;
    private ContentProperties contentProps;
    private DigitalSignPolicies digitalSignPolicies;
    private DigitalSignatureRectangle digitalSignatureRectangle;
    private Map<String, Style> styles = new HashMap<String, Style>();
    private Map<String, String> replaceMap = new HashMap<String, String>();
    private HashMap<String, com.aspose.pdf.Document> pdfAttachmentCache = new HashMap();
    private HashMap<String, String> pdfCaption = new HashMap();
    private HashMap<String, String> cachedCSS = new HashMap();
    private String cachedBaseCSS = "";

    public PropertyList getPolicy() {
        return this.policy;
    }

    public String getCachedBaseCSS() {
        return this.cachedBaseCSS;
    }

    public void setCachedBaseCSS(String cachedBaseCSS) {
        this.cachedBaseCSS = cachedBaseCSS;
    }

    public String getCachedCSS(String name) {
        return this.cachedCSS.get(name);
    }

    public void setCachedCSS(String name, String cachedCSS) {
        this.cachedCSS.put(name, cachedCSS);
    }

    public String getWorksheetid() {
        return this.worksheetid;
    }

    public void setWorksheetid(String worksheetid) {
        this.worksheetid = worksheetid;
    }

    public String getWorksheetversionid() {
        return this.worksheetversionid;
    }

    public void setWorksheetversionid(String worksheetversionid) {
        this.worksheetversionid = worksheetversionid;
    }

    public WordWorksheet(SapphireConnection sapphireConnection, String worksheetid, String worksheetversionid, PropertyList exportOptions) throws Exception {
        super(sapphireConnection);
        this.worksheetid = worksheetid;
        this.worksheetversionid = worksheetversionid;
        this.exportOptions = exportOptions;
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(sapphireConnection.getConnectionId());
        this.policy = configProcessor.getPolicy("ELNPolicy", WordWorksheet.getPolicyNode(new QueryProcessor(sapphireConnection.getConnectionId()), worksheetid, worksheetversionid));
    }

    public Document createDocument() throws Exception {
        this.setupProperties();
        if (this.exportProps.wordtemplate.length() == 0) {
            this.createStyles();
        }
        PropertyList loadProps = new PropertyList();
        loadProps.setProperty("worksheetid", this.worksheetid);
        loadProps.setProperty("worksheetversionid", this.worksheetversionid);
        loadProps.setProperty("loadviewhtml", "Y");
        loadProps.setProperty("loadoptions", "Y");
        loadProps.setProperty("convertoptionstojson", "N");
        loadProps.setProperty("export", "Y");
        this.load(this.worksheetid, this.worksheetversionid, loadProps);
        DataSet worksheet = this.getWorksheet();
        DataSet workbook = this.getWorkbook();
        DataSet worksheetsections = this.getWorksheetSections();
        DataSet worksheetitems = this.getWorksheetItems();
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        DataSet allSectionNotes = null;
        DataSet allItemNotes = null;
        DataSet allItemMetadata = null;
        DataSet allSectionMetadata = null;
        if (this.contentProps.showNotes) {
            String threadClause = !this.contentProps.showComments ? " AND sdinote.threadflag='R' " : "";
            allSectionNotes = qp.getPreparedSqlDataSet("SELECT worksheetsectionid, sysuser.sysuserdesc, sysuser.initials, sdinote.* FROM sdinote, worksheetsection, sysuser  WHERE sdinote.ownerid=sysuser.sysuserid AND worksheetid=? AND worksheetversionid=? AND sdinote.sdcid='LV_WorksheetSection' and sdinote.keyid1=worksheetsectionid " + threadClause + " order by threadnum, notenum", (Object[])new String[]{this.worksheetid, this.worksheetversionid}, true);
            allItemNotes = qp.getPreparedSqlDataSet("SELECT worksheetitemid, sysuser.sysuserdesc, sysuser.initials, sdinote.* FROM sdinote, worksheetitem, sysuser  WHERE sdinote.ownerid=sysuser.sysuserid AND worksheetid=? AND worksheetversionid=? AND sdinote.sdcid='LV_WorksheetItem' and sdinote.keyid1=worksheetitemid " + threadClause + " order by threadnum, notenum", (Object[])new String[]{this.worksheetid, this.worksheetversionid}, true);
        }
        if (this.contentProps.showMetadata) {
            allItemMetadata = qp.getPreparedSqlDataSet("SELECT worksheetitemid, sdiattribute.*, attributedef.attributetitle FROM sdiattribute, worksheetitem, attributedef   WHERE worksheetid=? AND worksheetversionid=? AND sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid=attributedef.attributedefid AND  sdiattribute.sdcid='LV_WorksheetItem' and sdiattribute.keyid1=worksheetitemid AND worksheetitem.propertytreeid<>'AttributesControl'  order by sdiattribute.usersequence, sdiattribute.attributeid", (Object[])new String[]{this.worksheetid, this.worksheetversionid}, true);
            allSectionMetadata = qp.getPreparedSqlDataSet("SELECT worksheetsectionid, sdiattribute.*, attributedef.attributetitle FROM sdiattribute, worksheetsection, attributedef  WHERE worksheetid=? AND worksheetversionid=? AND sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid=attributedef.attributedefid AND  sdiattribute.sdcid='LV_WorksheetSection' and sdiattribute.keyid1=worksheetsectionid  order by sdiattribute.usersequence, sdiattribute.attributeid", (Object[])new String[]{this.worksheetid, this.worksheetversionid}, true);
        }
        this.setTitle(this.unescapeChars(worksheet.getValue(0, "worksheetdesc")));
        this.exportProps.authorid = worksheet.getValue(0, "authorid");
        this.exportProps.authorname = worksheet.getValue(0, "authorid.sysuserdesc");
        this.exportProps.worksheetstatus = worksheet.getValue(0, "worksheetstatus");
        this.exportProps.workbookid = workbook.getValue(0, "workbookid");
        this.exportProps.workbookdesc = workbook.getValue(0, "workbookdesc");
        this.exportProps.workbookownerid = workbook.getValue(0, "ownerid");
        this.exportProps.workbookownername = workbook.getValue(0, "ownername");
        PropertyList worksheetOptions = new PropertyList();
        worksheetOptions.setPropertyList(worksheet.getValue(0, "options"));
        this.populateReplaceMap(qp);
        if (this.exportProps.wordtemplate.length() > 0) {
            File f;
            this.exportProps.wordtemplate = FileUtil.substituteConfigurationPaths(this.exportProps.wordtemplate);
            String altFileName = worksheetOptions.getProperty("publishwordtemplate");
            if (altFileName.length() > 0) {
                String fileName = FileManager.getFileName(this.exportProps.wordtemplate, true);
                this.exportProps.wordtemplate = StringUtil.replaceAll(this.exportProps.wordtemplate, fileName, altFileName);
                this.exportProps.wordtemplate = StringUtil.replaceAll(this.exportProps.wordtemplate, "\"", "/");
                this.exportProps.wordtemplate = StringUtil.replaceAll(this.exportProps.wordtemplate, "//", "/");
            }
            if ((f = new File(this.exportProps.wordtemplate)).exists()) {
                Document doc = new Document(f.getAbsolutePath());
                this.document = (Document)doc.deepClone(true);
                this.replaceTokens(this.document);
                this.builder = new DocumentBuilder(this.document);
                this.builder.moveToDocumentEnd();
            }
        }
        if (this.document == null) {
            this.document = new Document();
            this.builder = new DocumentBuilder(this.document);
        }
        this.performPageSetup();
        this.createTitlePage();
        if (this.exportProps.wordtemplate.length() == 0) {
            this.defineHeaderFooter();
        }
        this.createTableOfContents();
        if (!this.sectionProps.suppressNumbering) {
            this.defineSectionNumbering();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        HashMap<Integer, Integer> levelcounts = new HashMap<Integer, Integer>();
        int currentLevel = 0;
        int excludeLevel = -1;
        for (int i = 0; i < worksheetsections.size(); ++i) {
            int j;
            String sectionBehavior;
            boolean hiddenBehavior;
            int level = Integer.parseInt(worksheetsections.getValue(i, "sectionlevel"));
            String options = worksheetsections.getValue(i, "options");
            PropertyList sectionOptions = new PropertyList();
            sectionOptions.setPropertyList(options);
            if (excludeLevel > -1 && level <= excludeLevel) {
                excludeLevel = -1;
            }
            boolean bl = hiddenBehavior = (sectionBehavior = worksheetsections.getValue(i, "_behavior")).equals("hide") || this.exportProps.excludedisabledsections && sectionBehavior.equals("disable");
            if ("Y".equals(sectionOptions.getProperty("excludesectionfromexport")) || hiddenBehavior) {
                excludeLevel = level;
            }
            if (level >= excludeLevel && excludeLevel != -1 || worksheetsections.getValue(i, "include", "Y").equals("N")) continue;
            String worksheetsectionid = worksheetsections.getValue(i, "worksheetsectionid");
            String worksheetsectionversionid = worksheetsections.getValue(i, "worksheetsectionversionid");
            if (level > 0) {
                if (level <= currentLevel) {
                    levelcounts.put(level, (Integer)levelcounts.get(level) + 1);
                }
                StringBuilder num = new StringBuilder();
                for (j = 1; j <= level; ++j) {
                    Integer count = (Integer)levelcounts.get(j);
                    if (count == null) {
                        count = 1;
                        levelcounts.put(j, count);
                    }
                    num.append(count).append(".");
                }
                currentLevel = level;
                worksheetsections.setValue(i, "sectionnum", num.toString());
                HashMap<String, String> findSectionDetail = new HashMap<String, String>();
                findSectionDetail.put("keyid1", worksheetsectionid);
                DataSet notes = this.contentProps.showNotes && allSectionNotes != null ? allSectionNotes.getFilteredDataSet(findSectionDetail) : null;
                String metadataFootnote = "";
                if (this.contentProps.showMetadata && allSectionMetadata != null) {
                    metadataFootnote = this.getMetaDataFootnote(allSectionMetadata, findSectionDetail);
                }
                this.addSection(num.toString(), worksheetsections.getValue(i, "worksheetsectiondesc"), level, notes, metadataFootnote);
            }
            this.builder.getParagraphFormat().clearFormatting();
            filter.put("worksheetsectionid", worksheetsectionid);
            filter.put("worksheetsectionversionid", worksheetsectionversionid);
            DataSet sectionitems = worksheetitems.getFilteredDataSet(filter);
            for (j = 0; j < sectionitems.size(); ++j) {
                WorksheetItem worksheetItem;
                boolean excludeItem;
                boolean isLastSectionControl;
                boolean bl2 = isLastSectionControl = i == worksheetsections.size() - 1 && j == sectionitems.size() - 1;
                if (sectionitems.getString(j, "include", "Y").equals("N") || (excludeItem = "Y".equals((worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, null, (HashMap)sectionitems.get(j))).getWorksheetItemOptions().getOption("excludeitemfromexport")))) continue;
                String worksheetitemid = sectionitems.getString(j, "worksheetitemid");
                HashMap<String, String> findItemDetail = new HashMap<String, String>();
                findItemDetail.put("keyid1", worksheetitemid);
                if (this.contentProps.showNotes && allItemNotes != null) {
                    DataSet notes = allItemNotes.getFilteredDataSet(findItemDetail);
                    for (int n = 0; n < notes.size(); ++n) {
                        Comment comment = new Comment((DocumentBase)this.document);
                        String text = notes.getValue(n, "note");
                        if (notes.getValue(n, "followupflag").equalsIgnoreCase("Y") && notes.getValue(n, "resolvedflag").equalsIgnoreCase("Y")) {
                            text = text + "\n\nResolved by " + notes.getValue(n, "resolvedby") + ":\n" + notes.getValue(n, "resolvednote");
                        }
                        comment.setText(text);
                        comment.setAuthor(notes.getValue(n, "sysuserdesc", notes.getValue(n, "ownerid")));
                        comment.setInitial(this.getInitials(notes, n));
                        comment.setDateTime(notes.getCalendar(n, "createdt").getTime());
                        this.builder.insertNode((Node)comment);
                    }
                }
                this.builder.getParagraphFormat().clearFormatting();
                String metadataFootnote = "";
                if (this.contentProps.showMetadata && allItemMetadata != null) {
                    metadataFootnote = this.getMetaDataFootnote(allItemMetadata, findItemDetail);
                }
                String captionflag = sectionitems.getValue(j, "captionflag");
                String caption = sectionitems.getValue(j, "worksheetitemdesc");
                boolean footnoteAdded = false;
                if (captionflag.equals("A")) {
                    this.writeCaption(caption, metadataFootnote);
                    footnoteAdded = true;
                }
                this.builder.getParagraphFormat().clearFormatting();
                this.setTextStyle("Normal");
                this.exportOptions.setProperty("shownotes", this.contentProps.showNotes ? "Y" : "N");
                this.exportOptions.setProperty("showcomments", this.contentProps.showComments ? "Y" : "N");
                this.exportOptions.setProperty("showimagemarkup", this.contentProps.showImageMarkup ? "Y" : "N");
                worksheetItem.addWordContent(this, this.document, this.builder, this.exportOptions);
                if (this.exportOptions.getProperty("resetmargins").equalsIgnoreCase("Y")) {
                    this.performPageSetup();
                }
                if (isLastSectionControl) {
                    if (captionflag.equals("B") || metadataFootnote.length() > 0) {
                        this.builder.insertBreak(0);
                    }
                } else {
                    this.builder.insertBreak(0);
                }
                this.builder.getParagraphFormat().clearFormatting();
                if (captionflag.equals("B")) {
                    this.writeCaption(caption, metadataFootnote);
                    this.builder.insertBreak(0);
                    footnoteAdded = true;
                }
                if (footnoteAdded || metadataFootnote.length() <= 0) continue;
                PropertyList wsOptions = new PropertyList();
                String wsOptionsValue = sectionitems.getValue(j, "options", "{}");
                if (wsOptionsValue.trim().startsWith("<propertylist")) {
                    wsOptions.setPropertyList(wsOptionsValue);
                } else {
                    wsOptions.setJSONString(wsOptionsValue);
                }
                String dummyCaption = wsOptions.getProperty("name") + (metadataFootnote.length() > 0 ? this.contentProps.metadataTitle : "");
                this.writeCaption(dummyCaption, metadataFootnote);
                this.builder.insertBreak(0);
            }
        }
        return this.document;
    }

    private String getMetaDataFootnote(DataSet allItemMetadata, HashMap<String, String> findItemDetail) {
        DataSet itemMetadata = allItemMetadata.getFilteredDataSet(findItemDetail);
        StringBuilder meta = new StringBuilder();
        if (itemMetadata.size() > 0) {
            for (int n = 0; n < itemMetadata.size(); ++n) {
                meta.append(meta.length() > 0 ? ", " : "");
                String datatype = itemMetadata.getValue(n, "datatype");
                String columnid = datatype.equals("D") || datatype.equals("O") ? "datevalue" : (datatype.equals("N") ? "numericvalue" : "textvalue");
                itemMetadata.setDateDisplayFormat(columnid, datatype.equals("O") ? itemMetadata.getM18n().getDefaultDateOnlyFormat() : null);
                String textvalue = itemMetadata.getValue(n, columnid);
                if (textvalue.length() <= 0) continue;
                meta.append(itemMetadata.getValue(n, "attributetitle", itemMetadata.getValue(n, "attributeid"))).append(": " + textvalue);
            }
        }
        return meta.toString();
    }

    private String getInitials(DataSet itemNotes, int n) {
        String initials = itemNotes.getValue(n, "initials").toUpperCase();
        if (initials.length() == 0) {
            String[] parts = StringUtil.split(itemNotes.getValue(n, "sysuserdesc", "L V"), " ");
            initials = parts[0].substring(0, 1).toUpperCase();
            if (parts.length > 0) {
                initials = initials + parts[parts.length - 1].substring(0, 1).toUpperCase();
            }
        }
        return initials;
    }

    private void writeCaption(String caption, String metadataFootnote) throws Exception {
        this.setTextStyle("Caption");
        this.builder.getParagraphFormat().setStyleIdentifier(34);
        this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
        this.builder.write(this.unescapeChars(caption));
        if (metadataFootnote.length() > 0) {
            this.builder.insertFootnote(0, metadataFootnote);
        }
        this.builder.insertBreak(0);
        this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(0);
        this.builder.getParagraphFormat().setStyleIdentifier(0);
    }

    private void performPageSetup() {
        PageSetup ps = this.builder.getPageSetup();
        ps.setPaperSize(this.pageProps.size);
        ps.setOrientation(this.pageProps.orientation);
        ps.setTopMargin(this.pageProps.marginTop);
        ps.setBottomMargin(this.pageProps.marginBottom);
        ps.setLeftMargin(this.pageProps.marginLeft);
        ps.setRightMargin(this.pageProps.marginRight);
    }

    private void setupProperties() throws SapphireException {
        PropertyList format = this.policy.getPropertyListNotNull("exportformat");
        this.exportProps = new ExportProperties(format);
        this.pageProps = new PageProperties(format);
        this.titlePageProps = new TitlePageProperties(format);
        this.headerfooterProps = new HeaderFooterProperties(format);
        this.sectionProps = new SectionProperties(format);
        this.pdfProps = new PDFProperties(format);
        this.tocProps = new TocProperties(format);
        this.contentProps = new ContentProperties(format);
        this.digitalSignPolicies = new DigitalSignPolicies(format);
    }

    private void createStyles() {
        PropertyList format = this.policy.getPropertyList("exportformat");
        PropertyListCollection styles = format.getCollection("textstyles");
        for (int i = 0; i < styles.size(); ++i) {
            PropertyList props = styles.getPropertyList(i);
            String id = props.getProperty("styleid");
            try {
                this.styles.put(id, new Style(props));
                continue;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public void cachePdfAttachment(String attachmentKey, com.aspose.pdf.Document nestedPDF) {
        this.pdfAttachmentCache.put(attachmentKey, nestedPDF);
    }

    public void cachePdfCaption(String attachmentKey, String caption) {
        this.pdfCaption.put(attachmentKey, caption);
    }

    public void setTitle(String title) {
        this.pageProps.setTitle(title);
    }

    public String getTitle() {
        return this.pageProps.title;
    }

    public String getExportFilename() {
        return this.replaceTokens(this.exportProps.filename);
    }

    private void defineSectionNumbering() {
        List list = this.document.getLists().add(13);
        com.aspose.words.Style styleH1 = this.document.getStyles().getByStyleIdentifier(1);
        styleH1.getListFormat().setList(list);
        styleH1.getListFormat().setListLevelNumber(0);
        styleH1.getListFormat().getListLevel().setNumberFormat("\u0000");
        styleH1.getListFormat().getListLevel().setNumberStyle(0);
        com.aspose.words.Style styleH2 = this.document.getStyles().getByStyleIdentifier(2);
        styleH2.getListFormat().setList(list);
        styleH2.getListFormat().setListLevelNumber(1);
        styleH2.getListFormat().getListLevel().setRestartAfterLevel(0);
        styleH2.getListFormat().getListLevel().setNumberPosition(0.0);
        styleH2.getListFormat().getListLevel().setNumberFormat("\u0000.\u0001");
        styleH2.getListFormat().getListLevel().setNumberStyle(0);
        com.aspose.words.Style styleH3 = this.document.getStyles().getByStyleIdentifier(3);
        styleH3.getListFormat().setList(list);
        styleH3.getListFormat().setListLevelNumber(2);
        styleH3.getListFormat().getListLevel().setRestartAfterLevel(1);
        styleH3.getListFormat().getListLevel().setNumberFormat("\u0000.\u0001.\u0002");
        styleH3.getListFormat().getListLevel().setNumberStyle(0);
        com.aspose.words.Style styleH4 = this.document.getStyles().getByStyleIdentifier(4);
        styleH4.getListFormat().setList(list);
        styleH4.getListFormat().setListLevelNumber(3);
        styleH4.getListFormat().getListLevel().setRestartAfterLevel(2);
        styleH4.getListFormat().getListLevel().setNumberFormat("\u0000.\u0001.\u0002.\u0003");
        styleH4.getListFormat().getListLevel().setNumberStyle(0);
    }

    private void defineHeaderFooter() throws Exception {
        String right;
        String center;
        String left;
        Section currentSection = this.builder.getCurrentSection();
        PageSetup pageSetup = currentSection.getPageSetup();
        if (this.headerfooterProps.showHeader) {
            pageSetup.setHeaderDistance(20.0);
            this.builder.moveToHeaderFooter(1);
            left = this.replaceTokens(this.headerfooterProps.headerLeft);
            center = this.replaceTokens(this.headerfooterProps.headerCenter);
            right = this.replaceTokens(this.headerfooterProps.headerRight);
            this.writeHeaderFooterRow(left, center, right, this.headerfooterProps.headerStyle);
        }
        if (this.headerfooterProps.showFooter) {
            pageSetup.setHeaderDistance(20.0);
            this.builder.moveToHeaderFooter(3);
            left = this.replaceTokens(this.headerfooterProps.footerLeft);
            center = this.replaceTokens(this.headerfooterProps.footerCenter);
            right = this.replaceTokens(this.headerfooterProps.footerRight);
            this.writeHeaderFooterRow(left, center, right, this.headerfooterProps.footerStyle);
        }
        this.builder.moveToDocumentEnd();
    }

    private void writeHeaderFooterRow(String left, String middle, String right, String style) throws Exception {
        int width = 0;
        boolean hasLeft = left.length() > 0;
        boolean hasMiddle = middle.length() > 0;
        boolean hasRight = right.length() > 0;
        boolean showLeft = true;
        boolean showMiddle = true;
        boolean showRight = true;
        if (hasMiddle && !hasLeft && !hasRight) {
            showLeft = false;
            showMiddle = true;
            showRight = false;
            width = 100;
        } else if (hasLeft && !hasMiddle && !hasRight) {
            showLeft = true;
            showMiddle = false;
            showRight = false;
            width = 100;
        } else if (hasRight && !hasMiddle && !hasLeft) {
            showLeft = false;
            showMiddle = false;
            showRight = true;
            width = 100;
        } else if (hasLeft && !hasMiddle && hasRight) {
            showLeft = true;
            showMiddle = false;
            showRight = true;
            width = 50;
        } else if (hasLeft && hasMiddle && !hasRight) {
            showLeft = true;
            showMiddle = true;
            showRight = true;
            width = 50;
        } else if (!hasLeft && hasMiddle && hasRight) {
            showLeft = true;
            showMiddle = true;
            showRight = false;
            width = 50;
        } else if (hasLeft && hasMiddle && hasRight) {
            showLeft = true;
            showMiddle = true;
            showRight = true;
            width = 33;
        } else if (!(hasLeft || hasMiddle || hasRight)) {
            width = 0;
        }
        if (width > 0) {
            this.builder.startTable();
            this.setTextStyle(style);
            RowFormat rowFormat = this.builder.getRowFormat();
            rowFormat.setHeight(20.0);
            rowFormat.setHeightRule(1);
            this.builder.getCellFormat().clearFormatting();
            this.builder.getCellFormat().getBorders().setLineStyle(0);
            if (showLeft) {
                this.builder.insertCell();
                this.builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPercent((double)width));
                this.builder.getCellFormat().setWrapText(false);
                this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(0);
                this.writeWithField(left);
            }
            if (showMiddle) {
                this.builder.insertCell();
                this.builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPercent((double)width));
                this.builder.getCellFormat().setWrapText(false);
                this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
                this.writeWithField(middle);
            }
            if (showRight) {
                this.builder.insertCell();
                this.builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPercent((double)width));
                this.builder.getCellFormat().setWrapText(false);
                this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(2);
                this.writeWithField(right);
            }
            this.builder.endRow();
            this.builder.endTable();
        }
    }

    private void writeWithField(String text) {
        if (text != null && text.length() > 0) {
            if (text.contains("[page]")) {
                this.builder.write(text.substring(0, text.indexOf("[page]")));
                this.builder.insertField("PAGE", "");
                text = text.substring(text.indexOf("[page]") + 6);
            }
            if (text.contains("[numpages]")) {
                this.builder.write(text.substring(0, text.indexOf("[numpages]")));
                this.builder.insertField("NUMPAGES", "");
                text = text.substring(text.indexOf("[numpages]") + 10);
            }
            this.builder.write(text);
        }
    }

    /*
     * Exception decompiling
     */
    public String streamToWord(HttpServletResponse response) throws SapphireException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public byte[] streamToWord(OutputStream outputStream) throws SapphireException {
        byte[] documetByte;
        if (!this.exportProps.allowpdf) {
            throw new SapphireException("You are not allowed to export this worksheet in PDF format. . Check the Policy settings.");
        }
        try {
            this.document.updateFields();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.document.save((OutputStream)byteArrayOutputStream, 20);
            outputStream.write(byteArrayOutputStream.toByteArray());
            documetByte = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to complete worksheet for printing", e);
        }
        return documetByte;
    }

    public void streamToWordForExistingEvent(HttpServletResponse response, SapphireReportEvent reportEvent) throws SapphireException {
        try (ServletOutputStream outputStream = response.getOutputStream();){
            response.setContentType("application/msword");
            String filename = reportEvent.getFilename();
            filename = StringUtil.replaceAll(filename, ",", " ");
            filename = StringUtil.replaceAll(filename, "  ", " ");
            filename = filename + (!filename.endsWith("docx") ? ".docx" : "");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            outputStream.write(reportEvent.getReportByte());
        }
        catch (Exception e) {
            throw new SapphireException("Unable to get worksheet from event", e);
        }
    }

    /*
     * Exception decompiling
     */
    public String streamToPdf(HttpServletResponse response) throws SapphireException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 5 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private void addDocumentSignature(Object document) throws Exception {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String peerApprovedBy = "";
        String managerApprovedBy = "";
        String sql = "SELECT sdiapprovalstep.*, sysuser.sysuserdesc FROM sdiapprovalstep LEFT OUTER JOIN sysuser ON sysuser.sysuserid=sdiapprovalstep.reviewedby WHERE sdcid=" + safeSQL.addVar("LV_Worksheet") + " AND keyid1=" + safeSQL.addVar(this.getWorksheetid()) + "AND keyid2=" + safeSQL.addVar(this.getWorksheetversionid()) + "order by sdiapprovalstep.usersequence, sdiapprovalstep.approvalstep";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getValue(i, "approvalstep").equalsIgnoreCase("Peer")) {
                    peerApprovedBy = ds.getValue(i, "reviewedby");
                    continue;
                }
                if (!ds.getValue(i, "approvalstep").equalsIgnoreCase("Manager")) continue;
                managerApprovedBy = ds.getValue(i, "reviewedby");
            }
        }
        this.handleSignatureImage(document, peerApprovedBy, managerApprovedBy);
    }

    private void handleSignatureImage(Object document, String peerApprovedBy, String managerApprovedBy) throws Exception {
        DocumentUtil.replaceTextOnAllPages(document, "Signature Image will be placed in", "");
        DocumentUtil.replaceTextWithImage(document, "[SIGNATURE_COMPLETED_BY]", this.getUserSignature(this.sapphireConnection.getSysuserId()));
        if (OpalUtil.isNotEmpty(peerApprovedBy)) {
            DocumentUtil.replaceTextWithImage(document, "[SIGNATURE_PEER_APPROVED_BY]", this.getUserSignature(peerApprovedBy));
        }
        if (OpalUtil.isNotEmpty(managerApprovedBy)) {
            DocumentUtil.replaceTextWithImage(document, "[SIGNATURE_MNGR_APPROVED_BY]", this.getUserSignature(managerApprovedBy));
        }
        DocumentUtil.replaceTextWithImage(document, "[SIGNATURE_CONFIRMED_BY]", this.getUserSignature(this.sapphireConnection.getSysuserId()));
    }

    public byte[] streamToPdf(OutputStream outputStream) throws SapphireException {
        byte[] documetByte;
        try {
            PdfSaveOptions options = new PdfSaveOptions();
            if (this.pdfProps.compliance >= 0) {
                options.setCompliance(this.pdfProps.compliance);
            }
            for (String bookmark : this.bookmarks) {
                int level = Integer.parseInt(bookmark.substring(0, bookmark.indexOf(";")));
                bookmark = bookmark.substring(bookmark.indexOf(";") + 1);
                options.getOutlineOptions().getBookmarksOutlineLevels().add(bookmark, level);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.document.updateFields();
            this.document.save((OutputStream)byteArrayOutputStream, (SaveOptions)options);
            outputStream.write(byteArrayOutputStream.toByteArray());
            documetByte = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to complete worksheet for printing", e);
        }
        return documetByte;
    }

    public void streamToPdfForExistingEvent(HttpServletResponse response, SapphireReportEvent reportEvent) throws SapphireException {
        try (ServletOutputStream outputStream = response.getOutputStream();){
            response.setContentType("application/pdf");
            String filename = reportEvent.getFilename();
            filename = StringUtil.replaceAll(filename, ",", " ");
            filename = StringUtil.replaceAll(filename, "  ", " ");
            filename = filename + (!filename.endsWith("pdf") ? ".pdf" : "");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            outputStream.write(reportEvent.getReportByte());
        }
        catch (Exception e) {
            throw new SapphireException("Unable to get worksheet from event", e);
        }
    }

    public void streamToHTML(HttpServletResponse response) throws SapphireException {
        try {
            this.document.updateFields();
            HtmlSaveOptions options = new HtmlSaveOptions();
            options.setExportImagesAsBase64(true);
            options.setSaveFormat(50);
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("text/html");
            this.document.save((OutputStream)outputStream, (SaveOptions)options);
            outputStream.close();
        }
        catch (Exception e) {
            throw new SapphireException("Unable to complete worksheet for printing", e);
        }
    }

    public void streamToHTML(OutputStream outputStream) throws SapphireException {
        try {
            this.document.updateFields();
            HtmlSaveOptions options = new HtmlSaveOptions();
            options.setExportImagesAsBase64(true);
            options.setSaveFormat(50);
            this.document.save(outputStream, (SaveOptions)options);
        }
        catch (Exception e) {
            throw new SapphireException("Unable to complete worksheet for printing", e);
        }
    }

    public void print(String printername) throws SapphireException {
        try {
            this.document.updateFields();
            this.document.print(printername);
        }
        catch (Exception e) {
            throw new SapphireException("Unable to complete worksheet for printing", e);
        }
    }

    public void addSection(String number, String title, int level, DataSet notes, String metadataFootnote) throws SapphireException {
        try {
            if (this.sectionProps.startNewPageOnLevel1 && level == 1 && !this.isFirstSection) {
                this.builder.insertBreak(1);
            }
            this.isFirstSection = false;
            this.builder.getParagraphFormat().clearFormatting();
            this.setTextStyle(this.sectionProps.levelFonts[level]);
            switch (level) {
                case 1: {
                    this.builder.getParagraphFormat().setStyleIdentifier(1);
                    break;
                }
                case 2: {
                    this.builder.getParagraphFormat().setStyleIdentifier(2);
                    break;
                }
                case 3: {
                    this.builder.getParagraphFormat().setStyleIdentifier(3);
                    break;
                }
                case 4: {
                    this.builder.getParagraphFormat().setStyleIdentifier(4);
                    break;
                }
                case 5: {
                    this.builder.getParagraphFormat().setStyleIdentifier(5);
                    break;
                }
                case 6: {
                    this.builder.getParagraphFormat().setStyleIdentifier(6);
                    break;
                }
                case 7: {
                    this.builder.getParagraphFormat().setStyleIdentifier(7);
                    break;
                }
                case 8: {
                    this.builder.getParagraphFormat().setStyleIdentifier(8);
                    break;
                }
                case 9: {
                    this.builder.getParagraphFormat().setStyleIdentifier(9);
                    break;
                }
                default: {
                    this.builder.getParagraphFormat().setStyleIdentifier(9);
                }
            }
            String bookmarkid = "";
            if (this.pdfProps.addBookmarks && (this.pdfProps.bookmarkMode == BOOKMARK_ALLLEVELS || this.pdfProps.bookmarkMode == BOOKMARK_ALLLEVELS_CAPTIONS || this.pdfProps.bookmarkMode == BOOKMARK_LEVEL1 && level == 1)) {
                bookmarkid = number + " " + this.unescapeChars(title);
                this.builder.startBookmark(bookmarkid);
            }
            ArrayList<Comment> comments = new ArrayList<Comment>();
            if (notes != null) {
                for (int n = 0; n < notes.size(); ++n) {
                    Comment comment = new Comment((DocumentBase)this.document);
                    String text = notes.getValue(n, "note");
                    if (notes.getValue(n, "followupflag").equalsIgnoreCase("Y") && notes.getValue(n, "resolvedflag").equalsIgnoreCase("Y")) {
                        text = text + "\n\nResolved by " + notes.getValue(n, "resolvedby") + ":\n" + notes.getValue(n, "resolvednote");
                    }
                    comment.setText(text);
                    comment.setAuthor(notes.getValue(n, "sysuserdesc", notes.getValue(n, "ownerid")));
                    comment.setInitial(this.getInitials(notes, n));
                    comment.setDateTime(notes.getCalendar(n, "createdt").getTime());
                    CommentRangeStart start = new CommentRangeStart((DocumentBase)this.document, comment.getId());
                    this.builder.insertNode((Node)start);
                    comments.add(comment);
                }
            }
            this.builder.write(this.unescapeChars(title));
            if (metadataFootnote.length() > 0) {
                this.builder.insertFootnote(0, metadataFootnote);
            }
            if (notes != null) {
                Collections.reverse(comments);
                for (Comment comment : comments) {
                    CommentRangeEnd end = new CommentRangeEnd((DocumentBase)this.document, comment.getId());
                    this.builder.insertNode((Node)end);
                    this.builder.insertNode((Node)comment);
                }
            }
            if (bookmarkid.length() > 0) {
                this.builder.endBookmark(bookmarkid);
                this.bookmarks.add(level + ";" + bookmarkid);
            }
            this.builder.insertBreak(0);
        }
        catch (Exception e) {
            throw new SapphireException("Unable to add a section", e);
        }
    }

    public void createTitlePage() throws Exception {
        if (this.titlePageProps.show) {
            Section currentSection = this.builder.getCurrentSection();
            PageSetup pageSetup = currentSection.getPageSetup();
            pageSetup.setDifferentFirstPageHeaderFooter(true);
            String topText = this.replaceTokens(this.titlePageProps.topText);
            String middleText = this.replaceTokens(this.titlePageProps.middleText);
            String bottomText = this.replaceTokens(this.titlePageProps.bottomText);
            this.builder.startTable();
            this.builder.getCellFormat().clearFormatting();
            double height = -1.0 + (this.builder.getPageSetup().getPageHeight() - this.builder.getPageSetup().getTopMargin() - this.builder.getPageSetup().getHeaderDistance() - this.builder.getPageSetup().getFooterDistance() - this.builder.getPageSetup().getBottomMargin()) / 3.0;
            RowFormat rowFormat = this.builder.getRowFormat();
            rowFormat.setHeight(height);
            rowFormat.setHeightRule(1);
            this.builder.getCellFormat().getBorders().setLineStyle(0);
            this.builder.insertCell();
            this.setTextStyle(this.titlePageProps.topText);
            this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
            this.builder.getCellFormat().setVerticalAlignment(0);
            this.builder.write(topText);
            this.builder.endRow();
            this.setTextStyle(this.titlePageProps.middleStyle);
            this.builder.insertCell();
            this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
            this.builder.getCellFormat().setVerticalAlignment(1);
            this.builder.write(middleText);
            this.builder.endRow();
            this.setTextStyle(this.titlePageProps.bottomStyle);
            this.builder.insertCell();
            this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
            this.builder.getCellFormat().setVerticalAlignment(2);
            this.builder.write(bottomText);
            this.builder.endRow();
            this.builder.endTable();
            this.builder.insertBreak(5);
        }
    }

    public void createTableOfContents() throws Exception {
        if (this.tocProps.show) {
            this.setTextStyle(this.tocProps.titleStyle);
            this.builder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
            this.builder.writeln(this.tocProps.title);
            this.document.getStyles().getByStyleIdentifier(19).getParagraphFormat().setSpaceAfter(6.0);
            this.document.getStyles().getByStyleIdentifier(20).getParagraphFormat().setSpaceAfter(6.0);
            this.document.getStyles().getByStyleIdentifier(21).getParagraphFormat().setSpaceAfter(6.0);
            this.document.getStyles().getByStyleIdentifier(22).getParagraphFormat().setSpaceAfter(6.0);
            this.document.getStyles().getByStyleIdentifier(34).getParagraphFormat().setSpaceAfter(6.0);
            this.builder.insertTableOfContents("\\o \"1-4\" \\t \"Caption,3\" \\h \\z \\t");
            this.builder.insertBreak(5);
            this.builder.getCurrentSection().getPageSetup().setDifferentFirstPageHeaderFooter(false);
        }
    }

    private void setTextStyle(String stylename) {
        Style textStyle = this.styles.get(stylename);
        if (textStyle != null) {
            Font font = this.builder.getFont();
            font.setSize((double)textStyle.size);
            font.setBold(textStyle.bold);
            if (textStyle.color.length() > 0) {
                font.setColor(Color.decode(textStyle.color));
            } else {
                font.setColor(Color.BLACK);
            }
            font.setName(textStyle.fontName);
            font.setUnderline(textStyle.underline ? 1 : 0);
        } else if (stylename.length() > 0) {
            try {
                this.builder.getParagraphFormat().setStyleName(stylename);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void populateReplaceMap(QueryProcessor qp) {
        this.replaceMap.put("[currentuser]", this.sapphireConnection.getSysuserId());
        this.replaceMap.put("[authorid]", this.exportProps.authorid);
        this.replaceMap.put("[author]", this.exportProps.authorname);
        this.replaceMap.put("[authorname]", this.exportProps.authorname);
        this.replaceMap.put("[title]", this.pageProps.title);
        this.replaceMap.put("[worksheetname]", this.pageProps.title);
        this.replaceMap.put("[worksheetdesc]", this.pageProps.title);
        this.replaceMap.put("[worksheetid]", this.worksheetid);
        this.replaceMap.put("[worksheetversionid]", this.worksheetversionid);
        this.replaceMap.put("[worksheetstatus]", this.exportProps.worksheetstatus);
        this.replaceMap.put("[workbookid]", this.exportProps.workbookid);
        this.replaceMap.put("[workbookdesc]", this.exportProps.workbookdesc);
        this.replaceMap.put("[workbookname]", this.exportProps.workbookdesc);
        this.replaceMap.put("[workbookownerid]", this.exportProps.workbookownerid);
        this.replaceMap.put("[workbookowner]", this.exportProps.workbookownername);
        this.replaceMap.put("[workbookownername]", this.exportProps.workbookownername);
        this.replaceMap.put("[shortdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate]"));
        this.replaceMap.put("[mediumdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate]"));
        this.replaceMap.put("[longdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[longdate]"));
        this.replaceMap.put("[shortdatetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate] [shorttime]"));
        this.replaceMap.put("[mediumdatetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate] [shorttime]"));
        this.replaceMap.put("[longdatetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[longdate] [shorttime]"));
        this.replaceMap.put("[date]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate]"));
        this.replaceMap.put("[datetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate] [shorttime]"));
        this.replaceMap.put("[currentdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate]"));
        this.replaceMap.put("[currentdatetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate] [shorttime]"));
        DataSet metadata = qp.getPreparedSqlDataSet("SELECT * FROM sdiattribute WHERE sdcid='LV_Worksheet' AND keyid1=? AND keyid2=?", (Object[])new String[]{this.worksheetid, this.worksheetversionid}, true);
        for (int i = 0; i < metadata.size(); ++i) {
            String attributeid = metadata.getValue(i, "attributeid");
            String datatype = metadata.getValue(i, "datatype");
            String columnid = datatype.equals("D") || datatype.equals("O") ? "datevalue" : (datatype.equals("N") ? "numericvalue" : "textvalue");
            metadata.setDateDisplayFormat(columnid, datatype.equals("O") ? metadata.getM18n().getDefaultDateOnlyFormat() : null);
            String value = metadata.getValue(i, columnid, metadata.getValue(i, "default" + columnid));
            this.replaceMap.put("[metadata." + attributeid + "]", value == null ? "" : value);
        }
    }

    public String replaceTokens(String in) {
        for (String key : this.replaceMap.keySet()) {
            if (this.replaceMap.get(key) == null) continue;
            in = StringUtil.replaceAll(in, key, this.replaceMap.get(key), true);
            in = StringUtil.replaceAll(in, key.toUpperCase(), this.replaceMap.get(key).toUpperCase(), true);
        }
        return in;
    }

    public void replaceTokens(Document document) {
        FindReplaceOptions fro = new FindReplaceOptions();
        fro.setMatchCase(true);
        for (String key : this.replaceMap.keySet()) {
            try {
                if (this.replaceMap.get(key) == null) continue;
                document.getRange().replace(key, this.replaceMap.get(key), fro);
                document.getRange().replace(key.toUpperCase(), this.replaceMap.get(key).toUpperCase(), fro);
            }
            catch (Exception exception) {}
        }
    }

    private String unescapeChars(String text) {
        return text.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    public SapphireReportEvent getEventForWorkSheet(HashMap<String, Object> paramMap) throws SapphireException {
        String documentFormat = paramMap.get("documentFormat").toString();
        SapphireReportEvent event = null;
        if (this.exportProps.controlledFlag) {
            if (paramMap.get("eventType") != null && paramMap.get("eventType").equals("RePublish")) {
                try {
                    DBUtil dbu = new DBUtil();
                    SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(this.sapphireConnection.getDatabaseId());
                    DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
                    dbu.setConnection(database.getDbms(), dataSource.getConnection());
                    this.sapphireConnection.setConnection(dbu.getConnection());
                    event = new SapphireReportEvent(this, paramMap, this.sapphireConnection);
                    event.setDisplaytype(documentFormat);
                    String reportEventID = event.getReporteventid();
                    paramMap.put("SAPPHIRE_ReportEventID", reportEventID);
                    paramMap.put("SAPPHIRE_BaseReportEventID", reportEventID.contains("-") ? reportEventID.substring(0, reportEventID.indexOf("-")) : reportEventID);
                    paramMap.put("SAPPHIRE_ReportEventVersionID", event.getReporteventversionid());
                    paramMap.put("SAPPHIRE_PriorReportEventID", event.getPriorReportEventID());
                    event.setParamMap(paramMap);
                    event.getAttachment().setFilename(this.getExportFilename() + "." + documentFormat);
                }
                catch (SQLException e) {
                    throw new SapphireException("Failed to set sapphire connection.");
                }
            } else {
                event = new SapphireReportEvent(this);
                event.setDisplaytype(documentFormat);
                String reportEventID = event.getReporteventid();
                paramMap.put("SAPPHIRE_ReportEventID", reportEventID);
                paramMap.put("SAPPHIRE_BaseReportEventID", reportEventID.contains("-") ? reportEventID.substring(0, reportEventID.indexOf("-")) : reportEventID);
                paramMap.put("SAPPHIRE_ReportEventVersionID", event.getReporteventversionid());
                paramMap.put("SAPPHIRE_PriorReportEventID", event.getPriorReportEventID());
                event.setParamMap(paramMap);
                event.getAttachment().setFilename(this.getExportFilename() + "." + documentFormat);
            }
        }
        return event;
    }

    private boolean isPublishEventRequired(String controlEvent, String worksheetStatus) {
        return controlEvent.equals("Always") || controlEvent.contains(worksheetStatus);
    }

    public WordWorksheet(SapphireConnection sapphireConnection, String reporteventid) throws Exception {
        super(sapphireConnection);
        QueryProcessor qp = new QueryProcessor(sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT ITEMKEYID1,ITEMKEYID2 FROM reporteventitem WHERE reporteventid=" + safeSQL.addVar(reporteventid) + " and ITEMSDCID='LV_Worksheet'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            this.worksheetid = ds.getString(0, "ITEMKEYID1", "");
            this.worksheetversionid = ds.getString(0, "ITEMKEYID2", "");
        }
        this.exportOptions = new PropertyList(new JSONObject("{}"));
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(sapphireConnection.getConnectionId());
        this.policy = configProcessor.getPolicy("ELNPolicy", WordWorksheet.getPolicyNode(new QueryProcessor(sapphireConnection.getConnectionId()), this.worksheetid, this.worksheetversionid));
    }

    private InputStream getUserSignature(String userid) throws SapphireException {
        QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
        Attachment signatureAttachment = null;
        String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'User' AND keyid1 = ? AND attachmentclass = 'ReportSignature'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{userid});
        if (ds.getRowCount() > 0) {
            signatureAttachment = Attachment.getAttachment("User", userid, null, null, ds.getInt(0, "attachmentnum"));
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.sapphireConnection.getConnectionId());
            signatureAttachment = attachmentProcessor.getSDIAttachment(signatureAttachment, Attachment.ThumbnailGeneration.DISABLED);
        }
        if (signatureAttachment == null) {
            throw new SapphireException("User Signature attachment is missing.");
        }
        return signatureAttachment.getInputStream();
    }

    public String getReportEventid(String worksheetid, String worksheetversionid) {
        String reporteventid = "";
        QueryProcessor qp = new QueryProcessor(this.getConnectionProcessor().getSapphireConnection().getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT reporteventid FROM reporteventitem WHERE ITEMSDCID='LV_Worksheet' and itemkeyid1= " + safeSQL.addVar(worksheetid) + " and itemkeyid2 = " + safeSQL.addVar(worksheetversionid) + " order by reporteventid";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            reporteventid = ds.getString(0, "reporteventid");
        }
        return reporteventid;
    }

    public String performPublishEvent(String format, byte[] documentByte, boolean digitallysigned) throws SapphireException, IOException {
        String reporteventid = "";
        if (this.isPublishEventRequired(this.exportProps.controlevent, this.exportProps.worksheetstatus)) {
            ByteArrayInputStream documentStream = new ByteArrayInputStream(documentByte);
            String fileName = this.getExportFilename();
            if (format.equalsIgnoreCase(SaveFormat.getName((int)20).toLowerCase())) {
                fileName = fileName + (!fileName.endsWith("docx") ? ".docx" : "");
            }
            if (format.equalsIgnoreCase(SaveFormat.getName((int)40).toLowerCase())) {
                fileName = fileName + (!fileName.endsWith("pdf") ? ".pdf" : "");
            }
            HashMap<String, Object> eventProps = new HashMap<String, Object>();
            eventProps.put("documentFormat", format);
            eventProps.put("filename", fileName);
            reporteventid = this.getReportEventid(this.worksheetid, this.worksheetversionid);
            if (OpalUtil.isEmpty(reporteventid)) {
                eventProps.put("eventType", "Publish");
            } else {
                eventProps.put("eventType", "RePublish");
                eventProps.put("parentreporteventid", reporteventid);
            }
            SapphireReportEvent event = this.getEventForWorkSheet(eventProps);
            if (event != null) {
                event.setFilename(eventProps.get("filename").toString());
                event.setEventtype(eventProps.get("eventType").toString());
                if (digitallysigned) {
                    event.setDigitallysigned("Y");
                }
                event.saveEvent(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), documentStream);
                reporteventid = event.getReporteventid();
            }
            documentStream.close();
        }
        return reporteventid;
    }

    private class DigitalSignPolicies {
        String signingMode = "";
        String signingProvider = "";
        String signingProviderNode = "";

        public DigitalSignPolicies(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("digitalsigning");
            this.signingMode = props.getProperty("signingmode", "Never");
            this.signingProvider = props.getProperty("signingprovider");
            this.signingProviderNode = props.getProperty("signingprovidernode");
        }
    }

    private static class ExportProperties {
        String filename;
        String wordtemplate;
        boolean allowword;
        boolean allowpdf;
        String authorid = "";
        String worksheetstatus = "";
        String authorname = "";
        String workbookid = "";
        String workbookdesc = "";
        String workbookownername = "";
        String workbookownerid = "";
        boolean controlledFlag;
        boolean confirmationrequired;
        boolean vieweventrequired;
        String controlevent;
        boolean excludedisabledsections;

        public ExportProperties(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("exportoptions");
            this.filename = props.getProperty("filename", "Worksheet");
            this.wordtemplate = props.getProperty("wordtemplate");
            this.allowword = props.getProperty("allowword", "Y").equals("Y");
            this.allowpdf = props.getProperty("allowpdf", "Y").equals("Y");
            this.controlledFlag = props.getProperty("controlledflag").equalsIgnoreCase("Y");
            this.confirmationrequired = props.getProperty("confirmationrequired").equalsIgnoreCase("Y");
            this.vieweventrequired = props.getProperty("vieweventrequired").equals("Y");
            this.controlevent = props.getProperty("controlevent", "Always");
            this.excludedisabledsections = props.getProperty("excludedisabledsections", "N").equals("Y");
        }
    }

    private static class SectionProperties {
        boolean startNewPageOnLevel1;
        boolean suppressNumbering;
        String[] levelFonts = new String[10];

        public SectionProperties(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("sections");
            this.startNewPageOnLevel1 = props.getProperty("startnewpageonlevel1", "Y").equals("Y");
            this.suppressNumbering = props.getProperty("suppressnumbering", "N").equals("Y");
            this.levelFonts[1] = props.getProperty("level1style");
            this.levelFonts[2] = props.getProperty("level2style");
            this.levelFonts[3] = props.getProperty("level3style");
            this.levelFonts[4] = props.getProperty("level4style");
            this.levelFonts[5] = props.getProperty("level4style");
            this.levelFonts[6] = props.getProperty("level4style");
            this.levelFonts[7] = props.getProperty("level4style");
            this.levelFonts[8] = props.getProperty("level4style");
            this.levelFonts[9] = props.getProperty("level4style");
        }
    }

    private static class HeaderFooterProperties {
        boolean showHeader;
        String headerLeft;
        String headerCenter;
        String headerRight;
        String headerStyle;
        boolean showFooter;
        String footerLeft;
        String footerCenter;
        String footerRight;
        String footerStyle;

        public HeaderFooterProperties(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("headerfooter");
            this.showHeader = props.getProperty("showheader", "Y").equals("Y");
            this.headerLeft = props.getProperty("headerlefttext");
            this.headerCenter = props.getProperty("headermiddletext");
            this.headerRight = props.getProperty("headerrighttext");
            this.headerStyle = props.getProperty("headerstyle");
            this.showFooter = props.getProperty("showfooter", "Y").equals("Y");
            this.footerLeft = props.getProperty("footerlefttext");
            this.footerCenter = props.getProperty("footermiddletext");
            this.footerRight = props.getProperty("footerrighttext");
            this.footerStyle = props.getProperty("footerstyle");
        }
    }

    private class TocProperties {
        boolean show;
        String title;
        String titleStyle;
        boolean includeInPageCount;

        public TocProperties(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("toc");
            this.includeInPageCount = props.getProperty("includeinpagecount", "Y").equals("Y");
            this.show = WordWorksheet.this.exportOptions.getProperty("toc", props.getProperty("show", "Y")).equals("Y");
            this.title = props.getProperty("titletext", "Table of Contents");
            this.titleStyle = props.getProperty("titlestyle");
        }
    }

    private static class TitlePageProperties {
        boolean show;
        float paddingTop;
        float paddingBottom;
        String topText;
        String middleText;
        String bottomText;
        String topStyle;
        String middleStyle;
        String bottomStyle;

        public TitlePageProperties(PropertyList format) {
            PropertyList props = format.getPropertyListNotNull("titlepage");
            this.show = props.getProperty("show", "Y").equals("Y");
            this.topText = props.getProperty("toptext");
            this.topStyle = props.getProperty("topstyle");
            this.paddingTop = Float.parseFloat(props.getProperty("paddingtop", "0"));
            this.middleText = props.getProperty("middletext");
            this.middleStyle = props.getProperty("middlestyle");
            this.bottomText = props.getProperty("bottomtext");
            this.bottomStyle = props.getProperty("bottomstyle");
            this.paddingBottom = Float.parseFloat(props.getProperty("paddingbottom", "0"));
        }
    }

    private class ContentProperties {
        boolean showNotes;
        boolean showComments;
        boolean showImageMarkup;
        boolean showMetadata;
        String metadataTitle;

        public ContentProperties(PropertyList format) {
            PropertyList notes = format.getPropertyListNotNull("notes");
            this.showNotes = WordWorksheet.this.exportOptions.getProperty("notes", notes.getProperty("shownotes", "Y")).equals("Y");
            this.showComments = this.showNotes && WordWorksheet.this.exportOptions.getProperty("comments", notes.getProperty("showcomments", "N")).equals("Y");
            PropertyList images = format.getPropertyListNotNull("images");
            this.showImageMarkup = WordWorksheet.this.exportOptions.getProperty("imagemarkup", images.getProperty("showimagemarkup", "Y")).equals("Y");
            PropertyList metadata = format.getPropertyListNotNull("metadata");
            this.showMetadata = WordWorksheet.this.exportOptions.getProperty("metadata", metadata.getProperty("showmetadata", "Y")).equals("Y");
            this.metadataTitle = metadata.getProperty("metadatatitle", "Metadata");
        }
    }

    private static class PDFProperties {
        boolean addBookmarks;
        int bookmarkMode;
        int compliance = -1;

        public PDFProperties(PropertyList format) {
            String comp;
            String bm;
            PropertyList props = format.getPropertyListNotNull("pdfoptions");
            this.addBookmarks = props.getProperty("addbookmarks", "Y").equals("Y");
            switch (bm = props.getProperty("bookmarkmode", "All Sections")) {
                case "Level 1 Sections": {
                    this.bookmarkMode = BOOKMARK_LEVEL1;
                    break;
                }
                case "All Sections": {
                    this.bookmarkMode = BOOKMARK_ALLLEVELS;
                    break;
                }
                case "All Sections and Captions": {
                    this.bookmarkMode = BOOKMARK_ALLLEVELS_CAPTIONS;
                    break;
                }
            }
            switch (comp = props.getProperty("compliance")) {
                case "PDF 1.5": {
                    this.compliance = 1;
                    break;
                }
                case "PDF/A-1a": {
                    this.compliance = 2;
                    break;
                }
                case "PDF/A-1b": {
                    this.compliance = 3;
                    break;
                }
            }
        }
    }

    private class PageProperties {
        String title;
        int size;
        int orientation;
        double custompagewidth;
        double custompageheight;
        double marginLeft;
        double marginRight;
        double marginTop;
        double marginBottom;

        public PageProperties(PropertyList policy) {
            PropertyList props = policy.getPropertyListNotNull("pagesetup");
            String ps = WordWorksheet.this.exportOptions.getProperty("pagesize", props.getProperty("pagesize"));
            switch (ps.toLowerCase()) {
                case "letter": {
                    this.size = 9;
                    break;
                }
                case "legal": {
                    this.size = 8;
                    break;
                }
                case "a4": {
                    this.size = 1;
                    break;
                }
                case "a5": {
                    this.size = 1;
                    break;
                }
                case "b5": {
                    this.size = 1;
                    break;
                }
                case "custom": {
                    this.size = 16;
                    this.custompagewidth = Float.parseFloat(props.getProperty("custompagewidth"));
                    this.custompageheight = Float.parseFloat(props.getProperty("custompageheight"));
                }
            }
            String margins = props.getProperty("margins");
            switch (margins.toLowerCase()) {
                case "normal": {
                    this.marginTop = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginBottom = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginLeft = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginRight = ConvertUtil.millimeterToPoint((double)25.4);
                    break;
                }
                case "narrow": {
                    this.marginTop = ConvertUtil.millimeterToPoint((double)12.7);
                    this.marginBottom = ConvertUtil.millimeterToPoint((double)12.7);
                    this.marginLeft = ConvertUtil.millimeterToPoint((double)12.7);
                    this.marginRight = ConvertUtil.millimeterToPoint((double)12.7);
                    break;
                }
                case "moderate": {
                    this.marginTop = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginBottom = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginLeft = ConvertUtil.millimeterToPoint((double)19.1);
                    this.marginRight = ConvertUtil.millimeterToPoint((double)19.1);
                    break;
                }
                case "wide": {
                    this.marginTop = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginBottom = ConvertUtil.millimeterToPoint((double)25.4);
                    this.marginLeft = ConvertUtil.millimeterToPoint((double)50.8);
                    this.marginRight = ConvertUtil.millimeterToPoint((double)50.8);
                    break;
                }
                case "custom": {
                    this.marginTop = Float.parseFloat(props.getProperty("margintop"));
                    this.marginBottom = Float.parseFloat(props.getProperty("marginbottom"));
                    this.marginLeft = Float.parseFloat(props.getProperty("marginleft"));
                    this.marginRight = Float.parseFloat(props.getProperty("marginright"));
                }
            }
            this.orientation = WordWorksheet.this.exportOptions.getProperty("orientation", props.getProperty("orientation", "Portrait")).equals("Portrait") ? 1 : 2;
        }

        void setTitle(String title) {
            this.title = title;
        }
    }

    private static class Style {
        protected String fontName;
        protected float size;
        protected String color;
        protected boolean bold;
        protected boolean italics;
        protected boolean underline;

        Style(PropertyList props) throws IOException {
            this.fontName = props.getProperty("fontname", "helvetica");
            this.size = Float.parseFloat(props.getProperty("fontsize", "12"));
            this.color = props.getProperty("textcolor");
            this.bold = props.getProperty("bold").equals("Y");
            this.italics = props.getProperty("italics").equals("Y");
            this.underline = props.getProperty("underline").equals("Y");
        }
    }
}

