/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.HtmlSaveOptions
 *  com.aspose.cells.ImageFormat
 *  com.aspose.cells.ImageOrPrintOptions
 *  com.aspose.cells.License
 *  com.aspose.cells.PageSetup
 *  com.aspose.cells.PdfSaveOptions
 *  com.aspose.cells.SaveOptions
 *  com.aspose.cells.SheetRender
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 *  com.aspose.imaging.Color
 *  com.aspose.imaging.Image
 *  com.aspose.imaging.ImageOptionsBase
 *  com.aspose.imaging.License
 *  com.aspose.imaging.ResolutionSetting
 *  com.aspose.imaging.fileformats.emf.EmfImage
 *  com.aspose.imaging.fileformats.tiff.TiffImage
 *  com.aspose.imaging.fileformats.wmf.WmfImage
 *  com.aspose.imaging.imageoptions.JpegOptions
 *  com.aspose.imaging.imageoptions.PngOptions
 *  com.aspose.imaging.imageoptions.VectorRasterizationOptions
 *  com.aspose.imaging.imageoptions.WmfRasterizationOptions
 *  com.aspose.pdf.AbsorbedCell
 *  com.aspose.pdf.AbsorbedRow
 *  com.aspose.pdf.AbsorbedTable
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.HtmlSaveOptions
 *  com.aspose.pdf.ImageType
 *  com.aspose.pdf.License
 *  com.aspose.pdf.LoadOptions
 *  com.aspose.pdf.Page
 *  com.aspose.pdf.SaveOptions
 *  com.aspose.pdf.TableAbsorber
 *  com.aspose.pdf.TextAbsorber
 *  com.aspose.pdf.XImage
 *  com.aspose.pdf.XpsLoadOptions
 *  com.aspose.pdf.devices.JpegDevice
 *  com.aspose.pdf.devices.PngDevice
 *  com.aspose.pdf.devices.Resolution
 *  com.aspose.slides.HtmlFormatter
 *  com.aspose.slides.HtmlOptions
 *  com.aspose.slides.IHtmlFormatter
 *  com.aspose.slides.ISaveOptions
 *  com.aspose.slides.ISlide
 *  com.aspose.slides.License
 *  com.aspose.slides.Presentation
 *  com.aspose.words.Body
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBuilder
 *  com.aspose.words.Field
 *  com.aspose.words.FileFormatUtil
 *  com.aspose.words.HtmlFixedSaveOptions
 *  com.aspose.words.HtmlSaveOptions
 *  com.aspose.words.ImageSaveOptions
 *  com.aspose.words.License
 *  com.aspose.words.LoadOptions
 *  com.aspose.words.NodeCollection
 *  com.aspose.words.PageSet
 *  com.aspose.words.Row
 *  com.aspose.words.SaveOptions
 *  com.aspose.words.Shape
 *  com.aspose.words.Table
 *  com.aspose.words.TxtSaveOptions
 *  javax.servlet.jsp.PageContext
 *  org.apache.commons.codec.binary.Base64
 *  org.apache.commons.io.input.BOMInputStream
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.nodes.Node
 *  org.jsoup.select.Elements
 */
package com.labvantage.sapphire.util.file;

import com.aspose.cells.HtmlSaveOptions;
import com.aspose.cells.ImageFormat;
import com.aspose.cells.ImageOrPrintOptions;
import com.aspose.cells.License;
import com.aspose.cells.PageSetup;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.SheetRender;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.imaging.Image;
import com.aspose.imaging.ImageOptionsBase;
import com.aspose.imaging.ResolutionSetting;
import com.aspose.imaging.fileformats.emf.EmfImage;
import com.aspose.imaging.fileformats.tiff.TiffImage;
import com.aspose.imaging.fileformats.wmf.WmfImage;
import com.aspose.imaging.imageoptions.JpegOptions;
import com.aspose.imaging.imageoptions.PngOptions;
import com.aspose.imaging.imageoptions.VectorRasterizationOptions;
import com.aspose.imaging.imageoptions.WmfRasterizationOptions;
import com.aspose.pdf.AbsorbedCell;
import com.aspose.pdf.AbsorbedRow;
import com.aspose.pdf.AbsorbedTable;
import com.aspose.pdf.ImageType;
import com.aspose.pdf.LoadOptions;
import com.aspose.pdf.Page;
import com.aspose.pdf.SaveOptions;
import com.aspose.pdf.TableAbsorber;
import com.aspose.pdf.TextAbsorber;
import com.aspose.pdf.XImage;
import com.aspose.pdf.XpsLoadOptions;
import com.aspose.pdf.devices.JpegDevice;
import com.aspose.pdf.devices.PngDevice;
import com.aspose.pdf.devices.Resolution;
import com.aspose.slides.HtmlFormatter;
import com.aspose.slides.HtmlOptions;
import com.aspose.slides.IHtmlFormatter;
import com.aspose.slides.ISaveOptions;
import com.aspose.slides.ISlide;
import com.aspose.slides.Presentation;
import com.aspose.words.Body;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.Field;
import com.aspose.words.FileFormatUtil;
import com.aspose.words.HtmlFixedSaveOptions;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.NodeCollection;
import com.aspose.words.PageSet;
import com.aspose.words.Row;
import com.aspose.words.Shape;
import com.aspose.words.Table;
import com.aspose.words.TxtSaveOptions;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.instrument.csv.CSVWriter;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.DocumentFileParsingOptions;
import com.labvantage.sapphire.util.file.ExcelFileDetails;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.file.PPTFileDetails;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.TextFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.servlet.jsp.PageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FileManager {
    public static final String FILELOCATIONS_POLICY = "FileLocationPolicy";
    public static final String ATTACHMENT_POLICY = "AttachmentPolicy";
    public static final String FILELOCATIONS_POLICYNODE = "Sapphire Custom";
    public static final String FILELOCATIONS_LOCATIONS_COLLECTION = "locations";
    public static final String FILELOCATIONS_LOCATION_PROPERTY = "location";
    public static final String FILELOCATIONS_ID_PROPERTY = "id";
    public static final String FILELOCATIONS_TITLE_PROPERTY = "title";
    public static final String FILELOCATIONS_DESCRIPTION_PROPERTY = "description";
    public static final String FILELOCATIONS_IMAGE_PROPERTY = "image";
    public static final String FILELOCATIONS_LOCATIONS_STARTLOCATION = "startlocation";
    public static final String MAXIMAGEWIDTH = "maximagewidth";
    public static final String MAXIMAGEHEIGHT = "maximageheight";
    public static final String MAXWORDPAGES = "maxwordpages";
    public static final String SHOWGRIDLINES = "showgridlines";
    public static final String PUBLISHATTACHMENT = "publishattachment";
    public static final String PUBLISHATTACHMENTCAPTION = "publishattachmentcaption";
    public static final String MAXPPTSLIDES = "maxpptslides";
    public static final String MAXPDFPAGES = "maxpdfpages";
    public static final String MAXTEXTLINES = "maxtextlines";
    public static final String INITAILRENDERFROM = "initialrenderfrom";
    public static final String MAXALLOWEDWORDPAGES = "maxallowedwordpages";
    public static final String MAXALLOWEDPDFPAGES = "maxallowedpdfpages";
    public static final String MAXALLOWEDTEXTLINES = "maxallowedtextlines";
    public static final String MAXALLOWEDPPTSLIDES = "maxallowedpptslides";
    public static final String PPTRENDERSTYLE = "pptrenderstyle";
    public static final String RENDERSTYLE = "renderstyle";
    public static final String PUBLISHRENDERSTYLE = "publishrenderstyle";
    public static final String EXPORTRULE = "exportrule";
    public static final String EXPORTFROM = "exportfrom";
    public static final String EXPORTTO = "exportto";
    public static final String EMPTYTHUMBNAIL = "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
    private static String ATTACHMENTNODE = "Attachment Custom";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBinaryData(InputStream in, int maxsizelimit) throws Exception {
        byte[] data = null;
        if (in != null) {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();){
                int lengthread;
                byte[] bytebuff = new byte[500];
                while ((lengthread = in.read(bytebuff)) != -1) {
                    output.write(bytebuff, 0, lengthread);
                    if (maxsizelimit <= 0 || output.size() <= maxsizelimit) continue;
                }
                byte[] byArray = data = output.toByteArray();
                return byArray;
            }
        }
        return data;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static FileData generateThumbnail(String filename, String mimetype, int width, int height, Logger logger, String connectionId) {
        File file = new File(filename);
        if (file.exists()) {
            FileData fileData;
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                byte[] data = FileManager.getBinaryData(fileInputStream, -1);
                FileData input = new FileData(data, mimetype);
                fileData = FileManager.generateThumbnail(input, width, height, logger, connectionId);
            }
            catch (Throwable throwable) {
                try {
                    fileInputStream.close();
                    throw throwable;
                }
                catch (Exception e) {
                    logger.error("Could not read file.", e);
                    return null;
                }
            }
            fileInputStream.close();
            return fileData;
        }
        logger.error("File " + filename + " does not exist.");
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static FileData generateThumbnail(FileData indata, int width, int height, Logger logger, String connectionId) {
        boolean disablethumb = false;
        ConfigurationProcessor configuration = new ConfigurationProcessor(connectionId);
        try {
            disablethumb = configuration.getSysConfigProperty("disablethumbnails").equalsIgnoreCase("Y");
        }
        catch (Exception exception) {
            // empty catch block
        }
        FileData outdata = null;
        long allowedSize = 0x40000000L;
        if (disablethumb) {
            Trace.logDebug("Thumbnail Generation - Disabled.");
            return outdata;
        }
        if (indata.getSize() >= allowedSize) {
            Trace.logDebug("Thumbnail Generation - Size over 1 Gb.");
            return outdata;
        }
        if (width < 1) {
            width = 128;
        }
        if (height < 1) {
            height = 128;
        }
        FileType png = FileType.getFileTypeByName("PNG", connectionId);
        String outputtype = png.toString().toLowerCase();
        String outputmimetype = png.getMime();
        if (!FileTypeGroup.isValidTypeGroup(indata.getMimetype(), connectionId)) return outdata;
        if (!FileTypeGroup.isValidPreviewTypeGroup(indata.getMimetype(), connectionId)) return outdata;
        Trace.logDebug("Generating Thumbnail. Size = " + indata.getSize());
        FileTypeGroup type = FileTypeGroup.getFileTypeGroupByType(indata.getMimetype(), connectionId);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(indata.getData());){
            switch (type) {
                case IMAGE: {
                    BufferedImage srcImage = FileManager.getImageFromBIS(bis, null, new Dimension(width, height), logger);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        ImageIO.write((RenderedImage)srcImage, outputtype, baos);
                        outdata = new FileData(baos.toByteArray(), outputmimetype);
                        return outdata;
                    }
                }
                case EXCEL: {
                    ExcelFileDetails excelFileDetails = new ExcelFileDetails();
                    Workbook workbook = FileManager.getExcelWorkbookFromBis(bis, excelFileDetails, logger);
                    if (workbook.getWorksheets().getCount() <= 0) return outdata;
                    PdfSaveOptions excelsaveoptions = new PdfSaveOptions(13);
                    workbook.getWorksheets().setActiveSheetIndex(0);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        workbook.save((OutputStream)baos, (com.aspose.cells.SaveOptions)excelsaveoptions);
                        outdata = FileManager.generateThumbnail(new FileData(baos.toByteArray(), FileType.getFileTypeByName("PDF", connectionId).getMime()), width, height, logger, connectionId);
                        return outdata;
                    }
                }
                case PDF: {
                    PdfFileDetails pdfFileDetails = new PdfFileDetails();
                    pdfFileDetails.setFromPage(1);
                    pdfFileDetails.setToPage(1);
                    pdfFileDetails.setMaxAllowed(1);
                    com.aspose.pdf.Document pdfdoc = FileManager.getPdfDocumentFromBis(bis, pdfFileDetails, logger);
                    if (pdfdoc.getPages().size() <= 0) return outdata;
                    Resolution resolution = new Resolution(300);
                    PngDevice pngDevice = new PngDevice(width, height, resolution);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        BufferedImage bufferedImage = pngDevice.processToBufferedImage(pdfdoc.getPages().get_Item(1));
                        ImageIO.write((RenderedImage)bufferedImage, "png", baos);
                        outdata = new FileData(baos.toByteArray(), outputmimetype);
                        return outdata;
                    }
                }
                case PPT: {
                    PPTFileDetails pptFileDetails = new PPTFileDetails();
                    pptFileDetails.setMaxAllowed(1);
                    pptFileDetails.setFromSlide(1);
                    pptFileDetails.setToSlide(1);
                    pptFileDetails.setRenderStyle(FILELOCATIONS_IMAGE_PROPERTY);
                    Presentation presentation = FileManager.getPptPresentationFromBis(bis, pptFileDetails, logger);
                    if (presentation.getSlides().size() <= 0) return outdata;
                    ISlide slide = presentation.getSlides().get_Item(0);
                    BufferedImage slideimage = slide.getThumbnail(1.0f, 1.0f);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        ImageIO.write((RenderedImage)slideimage, outputtype, baos);
                        outdata = FileManager.generateThumbnail(new FileData(baos.toByteArray(), outputmimetype), width, height, logger, connectionId);
                        return outdata;
                    }
                }
                case TXT: {
                    DocumentBuilder documentBuilder = new DocumentBuilder();
                    TextFileDetails textFileDetails = new TextFileDetails();
                    textFileDetails.setFromLine(1);
                    textFileDetails.setToLine(100);
                    StringBuilder stringdata = FileManager.getTxtHTMLFromBis(bis, textFileDetails, logger);
                    documentBuilder.write(stringdata.toString());
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        documentBuilder.getDocument().save((OutputStream)baos, 20);
                        outdata = FileManager.generateThumbnail(new FileData(baos.toByteArray(), FileType.getFileTypeByName("DOCX", connectionId).getMime()), width, height, logger, connectionId);
                        return outdata;
                    }
                }
                case WORD: {
                    WordFileDetails wordFileDetails = new WordFileDetails();
                    wordFileDetails.setFromPage(1);
                    wordFileDetails.setToPage(1);
                    wordFileDetails.setMaxAllowed(1);
                    com.aspose.words.Document document = FileManager.getWordDocumentFromBis(bis, wordFileDetails, logger);
                    ImageSaveOptions options = new ImageSaveOptions(101);
                    options.setPageSet(new PageSet(0));
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        document.save((OutputStream)baos, (com.aspose.words.SaveOptions)options);
                        outdata = FileManager.generateThumbnail(new FileData(baos.toByteArray(), outputmimetype), width, height, logger, connectionId);
                        return outdata;
                    }
                }
                case HTML: {
                    com.aspose.words.LoadOptions loadOptions = new com.aspose.words.LoadOptions();
                    loadOptions.setLoadFormat(50);
                    com.aspose.words.Document htmldoc = new com.aspose.words.Document((InputStream)bis, loadOptions);
                    ImageSaveOptions htmloptions = new ImageSaveOptions(101);
                    htmloptions.setPageSet(new PageSet(1));
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                        htmldoc.save((OutputStream)baos, (com.aspose.words.SaveOptions)htmloptions);
                        outdata = FileManager.generateThumbnail(new FileData(baos.toByteArray(), outputmimetype), width, height, logger, connectionId);
                        return outdata;
                    }
                }
            }
            return outdata;
        }
        catch (Exception e) {
            Logger.logError(e.getMessage());
            return outdata;
        }
    }

    public static boolean isValidFileLocation(String destFilePathString, PropertyList filelocationPolicy) {
        boolean pathValid = false;
        Path destPath = Paths.get(destFilePathString, new String[0]);
        if (!Files.isDirectory(destPath, new LinkOption[0])) {
            destPath = destPath.getParent();
        }
        for (int l = 0; l < filelocationPolicy.getCollectionNotNull(FILELOCATIONS_LOCATIONS_COLLECTION).size(); ++l) {
            PropertyList location = filelocationPolicy.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION).getPropertyList(l);
            String loc = location.getProperty(FILELOCATIONS_LOCATION_PROPERTY);
            Path ploc = Paths.get(loc = FileSystem.getFileLocation(loc), new String[0]);
            if (!Files.isDirectory(ploc, new LinkOption[0])) {
                ploc = ploc.getParent();
            }
            if (!destPath.toAbsolutePath().startsWith(ploc)) continue;
            pathValid = true;
            break;
        }
        return pathValid;
    }

    public static TempFile getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectionId) {
        AttachmentProcessor arp = new AttachmentProcessor(connectionId);
        Attachment attachment = arp.getSDIAttachment(sdcid, keyid1, keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)", keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)", attachmentNum);
        return FileManager.getAttachment(attachment, connectionId);
    }

    public static TempFile getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentClass, String connectionId) {
        AttachmentProcessor arp = new AttachmentProcessor(connectionId);
        Attachment attachment = arp.getSDIAttachment(sdcid, keyid1, keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)", keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)", attachmentClass);
        return FileManager.getAttachment(attachment, connectionId);
    }

    private static TempFile getAttachment(sapphire.attachment.Attachment attachment, String connectionId) {
        TempFile out = null;
        if (attachment != null) {
            if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getClob() != null && attachment.getClob().length() > 0) {
                FileData fileData = new FileData(attachment.getInputStream(), FileType.getFileType(attachment.getFilename(), connectionId).getMime(), false);
                out = new TempFile(attachment.getClob(), fileData, true, connectionId);
            } else if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE) {
                FileData fileData = new FileData(attachment.getInputStream(), FileType.getFileType(attachment.getFilename(), connectionId).getMime(), false);
                out = new TempFile(fileData, attachment.getFilename(), attachment.getSourceFilename(), TempSource.ATTACHMENT, attachment.getAttachmentType().getFlag(), true, connectionId);
            } else if (attachment.getClob() != null) {
                FileData fileData = new FileData(attachment.getClob().getBytes(), FileType.getFileTypeByName("TXT", connectionId).getMime());
                out = new TempFile(fileData, "", "", TempSource.ATTACHMENT, attachment.getAttachmentType().getFlag(), true, connectionId);
            }
        }
        return out;
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension newsize, Dimension boundary) {
        double scalex = newsize.getWidth() / imgSize.getWidth();
        double scaley = newsize.getHeight() / imgSize.getHeight();
        double scale = Math.min(scalex, scaley);
        double new_width = Math.round(scale * imgSize.getWidth());
        double new_height = Math.round(scale * imgSize.getHeight());
        if (boundary.getHeight() > 0.0 && boundary.getWidth() > 0.0) {
            if (imgSize.getWidth() > boundary.getWidth()) {
                new_width = boundary.getWidth();
                new_height = new_width * imgSize.getHeight() / imgSize.getWidth();
            }
            if (new_height > boundary.getHeight()) {
                new_height = boundary.getHeight();
                new_width = new_height * imgSize.getWidth() / imgSize.getHeight();
            }
        }
        return new Dimension((int)Math.round(new_width), (int)Math.round(new_height));
    }

    private static BufferedImage resizeImage(BufferedImage orginal, Dimension scale) {
        int w = (int)Math.round(scale.getWidth());
        int h = (int)Math.round(scale.getHeight());
        java.awt.Image tmp = orginal.getScaledInstance(w, h, 4);
        BufferedImage dimg = new BufferedImage(w, h, 2);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public static BufferedImage getImageFromBIS(ByteArrayInputStream bis, Dimension newsize, Dimension maxsize, Logger logger) throws Exception {
        return FileManager.getImageFromIS(bis, newsize, maxsize, 100, logger);
    }

    public static BufferedImage getImageFromIS(InputStream is, Dimension newsize, Dimension maxsize, int imageScale, Logger logger) throws Exception {
        BufferedImage image = ImageIO.read(is);
        if (newsize == null) {
            newsize = new Dimension(image.getWidth(), image.getHeight());
        }
        Dimension scale = new Dimension(0, 0);
        scale.width = newsize.getWidth() > 0.0 ? (int)Math.ceil(newsize.getWidth() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)image.getWidth() * ((double)imageScale / 100.0));
        scale.height = newsize.getHeight() > 0.0 ? (int)Math.ceil(newsize.getHeight() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)image.getHeight() * ((double)imageScale / 100.0));
        Dimension size = FileManager.getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), scale, maxsize);
        maxsize.setSize(image.getWidth(), image.getHeight());
        image = FileManager.resizeImage(image, size);
        return image;
    }

    public static Dimension getImageFromBIS(StringBuilder out, ByteArrayInputStream bis, Dimension newsize, Dimension maxsize, Logger logger, String connectionId) throws Exception {
        return FileManager.getImageFromBIS(out, bis, newsize, maxsize, null, logger, connectionId);
    }

    public static Dimension getImageFromBIS(StringBuilder out, ByteArrayInputStream bis, Dimension newsize, Dimension maxsize, FileType fileType, Logger logger, String connectionid) throws Exception {
        return FileManager.getImageFromBIS(out, bis, newsize, maxsize, fileType, false, 100, 80, logger, connectionid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Dimension getImageFromBIS(StringBuilder out, ByteArrayInputStream bis, Dimension newsize, Dimension maxsize, FileType fileType, boolean jpegOut, int imageScale, int quality, Logger logger, String connectionid) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage image = null;
        Dimension outDim = null;
        try {
            if (!(fileType == null || fileType.getName().equals("TIF") || fileType.getName().equals("TIFF") || fileType.getName().equals("WMF"))) {
                image = FileManager.getImageFromIS(bis, newsize, maxsize, imageScale, logger);
                if (image != null) {
                    if (jpegOut) {
                        if (image.getType() != 1) {
                            int w = image.getWidth();
                            int h = image.getHeight();
                            BufferedImage newImage = new BufferedImage(w, h, 1);
                            int[] rgb = image.getRGB(0, 0, w, h, null, 0, w);
                            newImage.setRGB(0, 0, w, h, rgb, 0, w);
                            image = newImage;
                        }
                        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                        try {
                            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                            jpgWriteParam.setCompressionMode(2);
                            jpgWriteParam.setCompressionQuality((float)quality / 100.0f);
                            jpgWriter.setOutput(ImageIO.createImageOutputStream(baos));
                            IIOImage outputImage = new IIOImage(image, null, null);
                            jpgWriter.write(null, outputImage, jpgWriteParam);
                        }
                        finally {
                            jpgWriter.dispose();
                        }
                    } else {
                        ImageIO.write((RenderedImage)image, "png", baos);
                    }
                    baos.flush();
                }
                outDim = image != null ? new Dimension(image.getWidth(), image.getHeight()) : null;
            } else if (fileType.getName().equals("TIF") || fileType.getName().equals("TIFF")) {
                TiffImage tiffImage = (TiffImage)Image.load((InputStream)bis);
                if (tiffImage.getFrames().length > 0) {
                    Dimension scale = new Dimension(0, 0);
                    scale.width = newsize.getWidth() > 0.0 ? (int)Math.ceil(newsize.getWidth() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)tiffImage.getWidth() * ((double)imageScale / 100.0));
                    scale.height = newsize.getHeight() > 0.0 ? (int)Math.ceil(newsize.getHeight() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)tiffImage.getHeight() * ((double)imageScale / 100.0));
                    Dimension size = FileManager.getScaledDimension(new Dimension(tiffImage.getWidth(), tiffImage.getHeight()), scale, maxsize);
                    tiffImage.getFrames()[0].resize((int)Math.round(size.getWidth()), (int)Math.round(size.getHeight()));
                    Dimension dimension = outDim = tiffImage != null ? new Dimension(tiffImage.getWidth(), tiffImage.getHeight()) : null;
                    if (jpegOut) {
                        try (JpegOptions jpegOptions = new JpegOptions();){
                            jpegOptions.setResolutionSettings(new ResolutionSetting(96.0, 96.0));
                            jpegOptions.setQuality(quality);
                            tiffImage.getFrames()[0].save((OutputStream)baos, (ImageOptionsBase)jpegOptions);
                        }
                    } else {
                        tiffImage.getFrames()[0].save((OutputStream)baos, (ImageOptionsBase)new PngOptions());
                    }
                }
            } else if (fileType.getName().equals("WMF") || fileType.getName().equals("EMF")) {
                Object emfImage = null;
                emfImage = fileType.getName().equals("WMF") ? (WmfImage)Image.load((InputStream)bis) : (EmfImage)Image.load((InputStream)bis);
                if (emfImage != null && emfImage.getWidth() > 0) {
                    Dimension scale = new Dimension(0, 0);
                    scale.width = newsize.getWidth() > 0.0 ? (int)Math.ceil(newsize.getWidth() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)emfImage.getWidth() * ((double)imageScale / 100.0));
                    scale.height = newsize.getHeight() > 0.0 ? (int)Math.ceil(newsize.getHeight() * ((double)imageScale / 100.0)) : (int)Math.ceil((double)emfImage.getHeight() * ((double)imageScale / 100.0));
                    Dimension size = FileManager.getScaledDimension(new Dimension(emfImage.getWidth(), emfImage.getHeight()), scale, maxsize);
                    emfImage.resize((int)Math.round(size.getWidth()), (int)Math.round(size.getHeight()));
                    outDim = emfImage != null ? new Dimension(emfImage.getWidth(), emfImage.getHeight()) : null;
                    WmfRasterizationOptions rasterizationOptions = new WmfRasterizationOptions();
                    rasterizationOptions.setBackgroundColor(com.aspose.imaging.Color.getWhite());
                    rasterizationOptions.setPageWidth((float)emfImage.getWidth());
                    rasterizationOptions.setPageHeight((float)emfImage.getHeight());
                    if (jpegOut) {
                        try (JpegOptions op = new JpegOptions();){
                            op.setVectorRasterizationOptions((VectorRasterizationOptions)rasterizationOptions);
                            emfImage.save((OutputStream)baos, (ImageOptionsBase)op);
                            op.setResolutionSettings(new ResolutionSetting(96.0, 96.0));
                            op.setQuality(quality);
                            emfImage.save((OutputStream)baos, (ImageOptionsBase)op);
                        }
                    } else {
                        PngOptions op = new PngOptions();
                        op.setVectorRasterizationOptions((VectorRasterizationOptions)rasterizationOptions);
                        emfImage.save((OutputStream)baos, (ImageOptionsBase)op);
                    }
                }
            }
            if (baos != null && baos.size() > 0) {
                FileData fileData = new FileData(baos.toByteArray(), "image/png");
                out.append(fileData.getDataURL());
            }
        }
        finally {
            baos.close();
        }
        return outDim;
    }

    public static com.aspose.words.Document getWordDocumentFromBis(InputStream bis, WordFileDetails fileDetails, Logger logger) throws Exception {
        int fromPage = fileDetails.getFromPage();
        int toPage = fileDetails.getToPage();
        int maxAllowed = fileDetails.getMaxAllowed();
        com.aspose.words.Document out = null;
        try {
            com.aspose.words.Document extractedPages;
            com.aspose.words.Document document = new com.aspose.words.Document(bis);
            int pages = document.getPageCount();
            if (pages == 0) {
                throw new SapphireException("No pages found.");
            }
            if (fromPage < 1) {
                fromPage = 1;
            }
            if (toPage < 0) {
                toPage = maxAllowed;
            }
            if (fromPage > toPage) {
                fromPage = toPage;
            }
            if (toPage > pages) {
                toPage = pages;
            }
            if (toPage - fromPage >= maxAllowed) {
                throw new SapphireException("You can only display up to " + maxAllowed + " pages. Please choose a narrower range");
            }
            fileDetails.setTotalPagesAvailable(pages);
            out = extractedPages = document.extractPages(fromPage - 1, toPage - fromPage + 1);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SapphireException(e.getMessage());
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringBuilder getWordHtmlFromBis(InputStream bis, WordFileDetails fileDetails, Logger logger) throws Exception {
        StringBuilder out;
        block33: {
            out = new StringBuilder();
            try {
                com.aspose.words.Document document = FileManager.getWordDocumentFromBis(bis, fileDetails, logger);
                if (fileDetails.getRenderStyle().equalsIgnoreCase(FILELOCATIONS_IMAGE_PROPERTY)) {
                    ImageSaveOptions options = new ImageSaveOptions(104);
                    options.setJpegQuality(fileDetails.getImageQuality());
                    options.setResolution(96.0f);
                    options.setScale((float)fileDetails.getImageScale() / 100.0f);
                    for (int i = 0; i <= document.getPageCount(); ++i) {
                        options.setPageSet(new PageSet(i));
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        try {
                            try {
                                document.save((OutputStream)os, (com.aspose.words.SaveOptions)options);
                                FileManager.writeImageStream(out, os, true, fileDetails.hasImageBorder());
                            }
                            finally {
                                os.close();
                            }
                            out.append("<br>");
                            continue;
                        }
                        catch (Exception e) {
                            logger.warn("Failed to write word page " + i + " to image. Error: " + e.getMessage());
                        }
                    }
                    break block33;
                }
                if (fileDetails.getHtmlPageContainer().length() > 0) {
                    com.aspose.words.HtmlSaveOptions options = new com.aspose.words.HtmlSaveOptions();
                    options.setExportImagesAsBase64(true);
                    options.setSaveFormat(50);
                    options.setExportPageSetup(true);
                    options.setExportPageMargins(true);
                    options.setExportDocumentProperties(true);
                    options.setExportTocPageNumbers(true);
                    options.setCssClassNamePrefix(fileDetails.getUniqueid() + "_");
                    Document htmldoc = null;
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                        document.save((OutputStream)bos, (com.aspose.words.SaveOptions)options);
                        String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        Document outputDoc = Jsoup.parse((String)html);
                        htmldoc = Jsoup.parseBodyFragment((String)("<head>" + outputDoc.head().html() + "</head><body></body>"));
                        Document container = Jsoup.parseBodyFragment((String)fileDetails.getHtmlPageContainer());
                        container.body().child(0).appendChild((Node)outputDoc.body());
                        htmldoc.body().appendChild((Node)container.body().child(0));
                    }
                    out.append(htmldoc != null ? htmldoc.html() : "");
                    break block33;
                }
                if (fileDetails.isFixedLayout()) {
                    HtmlFixedSaveOptions options = new HtmlFixedSaveOptions();
                    options.setExportEmbeddedCss(true);
                    options.setExportEmbeddedImages(true);
                    options.setExportEmbeddedSvg(true);
                    options.setExportEmbeddedFonts(true);
                    options.setShowPageBorder(true);
                    options.setUseAntiAliasing(true);
                    options.setPageMargins(0.0);
                    options.setPageHorizontalAlignment(0);
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                        document.save((OutputStream)bos, (com.aspose.words.SaveOptions)options);
                        String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        html = FileManager.makeImagesUnique(html, fileDetails.getUniqueid());
                        out.append(html);
                        break block33;
                    }
                }
                com.aspose.words.HtmlSaveOptions options = new com.aspose.words.HtmlSaveOptions();
                options.setExportImagesAsBase64(true);
                options.setSaveFormat(50);
                options.setExportPageSetup(fileDetails.isExportPageSetup());
                options.setExportPageMargins(fileDetails.isExportPageMargins());
                options.setExportDocumentProperties(true);
                options.setExportTocPageNumbers(true);
                Document htmldoc = null;
                for (int i = 0; i < document.getPageCount(); ++i) {
                    com.aspose.words.Document splitdoc = document.extractPages(i, 1);
                    for (Field field : splitdoc.getRange().getFields()) {
                        if (field.getType() != 33) continue;
                        field.setResult("" + (i + 1));
                        field.unlink();
                    }
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                        splitdoc.save((OutputStream)bos, (com.aspose.words.SaveOptions)options);
                        String s = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        Document nextdoc = Jsoup.parse((String)StringUtil.replaceAll(s, "-aw-headerfooter-type", "xxxx-aw-headerfooter-type"));
                        if (htmldoc == null) {
                            htmldoc = Jsoup.parseBodyFragment((String)("<head>" + nextdoc.head().html() + "</head><body></body>"));
                        }
                        htmldoc.body().child(0).appendChild((Node)nextdoc.body());
                        continue;
                    }
                }
                out.append(htmldoc != null ? htmldoc.html() : "");
            }
            catch (Exception e) {
                throw new SapphireException("Could not process Word document: " + e.getMessage());
            }
        }
        return out;
    }

    private static String makeImagesUnique(String html, String uniqueid) {
        if (!html.contains("<image ")) {
            return html;
        }
        Document doc = Jsoup.parse((String)html);
        Elements images = doc.select("defs > image");
        for (Element image : images) {
            String id = image.attributes().get(FILELOCATIONS_ID_PROPERTY);
            if (!id.startsWith(FILELOCATIONS_IMAGE_PROPERTY)) continue;
            String newid = uniqueid + "_" + id;
            image.attributes().put(FILELOCATIONS_ID_PROPERTY, newid);
            Elements uses = doc.select("use[xlink:href='#" + id + "']");
            for (Element use : uses) {
                use.attributes().put("xlink:href", "#" + newid);
            }
        }
        return doc.toString();
    }

    public static DataSet getDataSetFromWordDocument(String inpath) throws SapphireException {
        DataSet outd = null;
        try (FileInputStream fis = new FileInputStream(inpath);){
            outd = FileManager.getDataSetFromWordDocument(fis, null);
        }
        catch (IOException e) {
            throw new SapphireException("IOException " + e.getMessage(), e);
        }
        return outd;
    }

    public static DataSet getDataSetFromWordDocument(InputStream is, WordFileDetails fileDetails) throws SapphireException {
        DataSet outdata;
        block16: {
            if (fileDetails == null) {
                fileDetails = new WordFileDetails();
            }
            outdata = null;
            int fromPage = fileDetails.getFromPage();
            int toPage = fileDetails.getToPage();
            int maxAllowed = fileDetails.getMaxAllowed();
            try {
                com.aspose.words.Document document = new com.aspose.words.Document(is);
                int pages = document.getPageCount();
                if (pages == 0) {
                    throw new SapphireException("No pages found.");
                }
                if (fromPage < 1) {
                    fromPage = 1;
                }
                if (toPage < 0) {
                    toPage = maxAllowed;
                }
                if (fromPage > toPage) {
                    fromPage = toPage;
                }
                if (toPage > pages) {
                    toPage = pages;
                }
                if (toPage - fromPage >= maxAllowed) {
                    throw new SapphireException("You can only rip " + maxAllowed + " pages. Please choose a narrower range");
                }
                fileDetails.setFromPage(fromPage);
                fileDetails.setToPage(toPage);
                fileDetails.setTotalPagesAvailable(pages);
                if (!(fromPage == 1 && toPage == pages || toPage == 0 && toPage == pages)) {
                    document = document.extractPages(fromPage - 1, toPage - fromPage + 1);
                }
                if (document != null) {
                    PropertyList outlist = new PropertyList();
                    PropertyListCollection outlistpages = new PropertyListCollection();
                    outlist.setProperty("sections", outlistpages);
                    for (int p = 0; p < document.getSections().getCount(); ++p) {
                        PropertyList currentsection = new PropertyList();
                        Body body = document.getSections().get(p).getBody();
                        if (body.getTables().getCount() <= 0) continue;
                        outdata = new DataSet();
                        Table table = body.getTables().get(0);
                        for (int r = 0; r < table.getRows().getCount(); ++r) {
                            Row row = table.getRows().get(r);
                            if (r == 0) {
                                for (int c = 0; c < row.getCells().getCount(); ++c) {
                                    outdata.addColumn(row.getCells().get(c).getText().trim(), 0);
                                }
                                continue;
                            }
                            int dsr = outdata.addRow();
                            for (int c = 0; c < row.getCells().getCount(); ++c) {
                                outdata.setValue(dsr, outdata.getColumnId(c), row.getCells().get(c).getText().trim());
                            }
                        }
                        break block16;
                    }
                    break block16;
                }
                throw new Exception("Document is null or no output path provided.");
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage());
            }
        }
        return outdata;
    }

    public static com.aspose.pdf.Document getPdfDocumentFromBis(InputStream bis, PdfFileDetails fileDetails, Logger logger) throws Exception {
        int fromPage = fileDetails.getFromPage();
        int toPage = fileDetails.getToPage();
        int maxAllowed = fileDetails.getMaxAllowed();
        try {
            com.aspose.pdf.Document document = new com.aspose.pdf.Document(bis);
            int pages = document.getPages().size();
            if (pages == 0) {
                throw new SapphireException("No pages found.");
            }
            if (fromPage < 1) {
                fromPage = 1;
            }
            if (toPage < 0) {
                toPage = maxAllowed;
            }
            if (fromPage > toPage) {
                fromPage = toPage;
            }
            if (toPage > pages) {
                toPage = pages;
            }
            if (toPage - fromPage >= maxAllowed) {
                throw new SapphireException("You can only display up to " + maxAllowed + " pages. Please choose a narrower range");
            }
            fileDetails.setFromPage(fromPage);
            fileDetails.setToPage(toPage);
            fileDetails.setTotalPagesAvailable(pages);
            if (!(fromPage == 1 && toPage == pages || toPage == 0 && toPage == pages)) {
                int p;
                int toPageRel = toPage;
                for (p = fromPage - 1; p > 0; --p) {
                    document.getPages().delete(p);
                    --toPageRel;
                }
                for (p = document.getPages().size(); p > toPageRel; --p) {
                    document.getPages().delete(p);
                }
            }
            return document;
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public static StringBuilder getPdfHtmlFromBis(InputStream bis, PdfFileDetails fileDetails, Logger logger) throws Exception {
        StringBuilder out = new StringBuilder();
        com.aspose.pdf.Document document = null;
        try {
            document = FileManager.getPdfDocumentFromBis(bis, fileDetails, logger);
        }
        catch (Exception e) {
            throw new SapphireException("Could not read PDF document");
        }
        if (document != null) {
            out.append((CharSequence)FileManager.getPdfHtmlFromDoc(document, fileDetails, logger));
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringBuilder getPdfHtmlFromDoc(com.aspose.pdf.Document document, PdfFileDetails fileDetails, Logger logger) throws Exception {
        StringBuilder out;
        block31: {
            out = new StringBuilder();
            try {
                if (fileDetails.getRenderStyle().equalsIgnoreCase(FILELOCATIONS_IMAGE_PROPERTY)) {
                    for (int i = 0; i < document.getPages().size(); ++i) {
                        double w = document.getPageInfo().getWidth();
                        double h = document.getPageInfo().getHeight();
                        int height = (int)Math.ceil(h);
                        int width = (int)Math.ceil(w);
                        Resolution resolution = new Resolution(96, 96);
                        int resizeHeight = (int)Math.ceil((double)height * ((double)fileDetails.getImageScale() / 100.0));
                        int resizeWidth = (int)Math.ceil((double)width * ((double)fileDetails.getImageScale() / 100.0));
                        JpegDevice jpegDevice = new JpegDevice(resizeWidth, resizeHeight, resolution, fileDetails.getImageQuality());
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        try {
                            try {
                                jpegDevice.process(document.getPages().get_Item(i + 1), (OutputStream)os);
                                FileManager.writeImageStream(resizeWidth, resizeHeight, out, os, true, fileDetails.hasImageBorder());
                            }
                            catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            finally {
                                os.close();
                            }
                            out.append("<br>");
                            continue;
                        }
                        catch (Exception e) {
                            logger.warn("Failed to write pdf page " + i + " to image. Error: " + e.getMessage());
                        }
                    }
                    break block31;
                }
                com.aspose.pdf.HtmlSaveOptions htmlOptions = new com.aspose.pdf.HtmlSaveOptions();
                htmlOptions.PartsEmbeddingMode = 0;
                htmlOptions.LettersPositioningMethod = 0;
                htmlOptions.RasterImagesSavingMode = 2;
                htmlOptions.FontSavingMode = 3;
                htmlOptions.setFixedLayout(true);
                htmlOptions.setCompressSvgGraphicsIfAny(false);
                htmlOptions.SaveTransparentTexts = true;
                htmlOptions.SaveShadowedTextsAsTransparentTexts = true;
                htmlOptions.ExcludeFontNameList = new String[]{"ArialMT", "SymbolMT"};
                htmlOptions.setUseZOrder(true);
                htmlOptions.setSplitIntoPages(false);
                htmlOptions.CssClassNamesPrefix = fileDetails.getUniqueid() + "_";
                if (fileDetails.getHtmlPageContainer().length() > 0) {
                    Document htmldoc = null;
                    for (int i = 0; i < document.getPages().size(); ++i) {
                        com.aspose.pdf.Document newdoc = new com.aspose.pdf.Document();
                        Page page = document.getPages().get_Item(i + 1);
                        newdoc.getPages().add(page);
                        StringBuilder current = new StringBuilder();
                        File temp = FileUtil.createTempFile("lvconvert", ".tmp").toFile();
                        try {
                            newdoc.save(temp.getAbsolutePath(), (SaveOptions)htmlOptions);
                            try (BufferedReader bufferedReader = null;){
                                String line;
                                bufferedReader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(temp), "UTF8"));
                                while ((line = bufferedReader.readLine()) != null) {
                                    current.append(line);
                                    current.append("\n");
                                }
                            }
                        }
                        finally {
                            temp.delete();
                        }
                        Document nextdoc = Jsoup.parse((String)current.toString());
                        if (htmldoc == null) {
                            htmldoc = Jsoup.parseBodyFragment((String)("<head>" + nextdoc.head().html() + "</head><body></body>"));
                        }
                        Document container = Jsoup.parseBodyFragment((String)fileDetails.getHtmlPageContainer());
                        container.body().child(0).appendChild((Node)nextdoc.body());
                        htmldoc.body().appendChild((Node)container.body().child(0));
                    }
                    out.append(htmldoc != null ? htmldoc.html() : "");
                    break block31;
                }
                File temp = FileUtil.createTempFile("lvconvert", ".tmp").toFile();
                try {
                    document.save(temp.getAbsolutePath(), (SaveOptions)htmlOptions);
                    try (BufferedReader bufferedReader = null;){
                        String line;
                        bufferedReader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(temp), "UTF8"));
                        while ((line = bufferedReader.readLine()) != null) {
                            out.append(line);
                            out.append("\n");
                        }
                        HTMLEditorControl.processHTML(out, "link", new HTMLEditorControl.ElementProcessor(){

                            @Override
                            public void process(Element element) {
                                element.remove();
                            }

                            @Override
                            public void complete() {
                            }
                        });
                    }
                }
                finally {
                    temp.delete();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Could not process PDF document: " + e.getMessage());
            }
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringBuilder getTxtHTMLFromBis(ByteArrayInputStream bis, TextFileDetails fileDetails, Logger logger) throws Exception {
        StringBuilder out = new StringBuilder();
        int fromLine = fileDetails.getFromLine();
        int toLine = fileDetails.getToLine();
        int maxAllowed = fileDetails.getMaxAllowed();
        try {
            StringBuilder tempBuf;
            if (fromLine < 1) {
                fromLine = 1;
            }
            if (toLine < 0) {
                toLine = maxAllowed;
            }
            if (fromLine > toLine) {
                fromLine = toLine;
            }
            if (toLine == 0) {
                toLine = 1;
            }
            if (fromLine > toLine) {
                fromLine = toLine;
            }
            if (toLine - fromLine + 1 > maxAllowed) {
                throw new SapphireException("The number of lines selected exceeds the maximum allowed.");
            }
            fileDetails.setFromLine(fromLine);
            fileDetails.setToLine(toLine);
            BufferedReader bfReader = new BufferedReader(new InputStreamReader((InputStream)bis, "UTF8"));
            String temp = null;
            int lines = 0;
            int tabsize = 8;
            boolean toImage = fileDetails.getRenderStyle().equalsIgnoreCase(FILELOCATIONS_IMAGE_PROPERTY);
            if (!toImage) {
                tempBuf = out;
                tempBuf.append("<pre style=\"tab-size:" + tabsize + "\"><font face=\"courier\">");
            } else {
                tempBuf = new StringBuilder();
            }
            while ((temp = bfReader.readLine()) != null) {
                if (++lines > maxAllowed || fromLine >= 1 && lines < fromLine || toLine >= 1 && lines > toLine) continue;
                if (tempBuf.length() > 0) {
                    if (toImage) {
                        tempBuf.append("\n");
                    } else {
                        tempBuf.append("<br>");
                    }
                }
                if (!toImage) {
                    temp = StringUtil.replaceAll(temp, " ", "&nbsp;");
                    temp = StringUtil.replaceAll(StringUtil.replaceAll(temp, ">", "&gt;"), "<", "&lt;");
                }
                tempBuf.append(temp);
            }
            if (!toImage) {
                tempBuf.append("</font></pre>");
            } else {
                DocumentBuilder documentBuilder = new DocumentBuilder();
                documentBuilder.getPageSetup().setPaperSize(9);
                documentBuilder.getPageSetup().setOrientation(2);
                documentBuilder.getPageSetup().setBottomMargin(1.0);
                documentBuilder.getPageSetup().setTopMargin(1.0);
                documentBuilder.getPageSetup().setLeftMargin(1.0);
                documentBuilder.getPageSetup().setRightMargin(1.0);
                documentBuilder.getFont().setSize(10.0);
                documentBuilder.getFont().setName("Courier New");
                documentBuilder.write(tempBuf.toString());
                com.aspose.words.Document document = documentBuilder.getDocument();
                ImageSaveOptions options = new ImageSaveOptions(104);
                options.setResolution(115.0f);
                options.setScale((float)fileDetails.getImageScale() / 100.0f);
                options.setJpegQuality(fileDetails.getImageQuality());
                for (int i = 0; i <= document.getPageCount(); ++i) {
                    options.setPageSet(new PageSet(i));
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        try {
                            document.save((OutputStream)os, (com.aspose.words.SaveOptions)options);
                            FileManager.writeImageStream(out, os, true, fileDetails.hasImageBorder());
                        }
                        finally {
                            os.close();
                        }
                        out.append("<br>");
                        continue;
                    }
                    catch (Exception e) {
                        logger.warn("Failed to write text " + i + " to image. Error: " + e.getMessage());
                    }
                }
            }
            fileDetails.setTotalRowsAvailable(lines);
        }
        catch (Exception e) {
            throw new SapphireException("Could not process file: " + e.getMessage());
        }
        return out;
    }

    public static Presentation getPptPresentationFromBis(ByteArrayInputStream bis, PPTFileDetails fileDetails, Logger logger) throws SapphireException {
        int fromSlide = fileDetails.getFromSlide();
        int toSlide = fileDetails.getToSlide();
        int scaleFactor = fileDetails.getScaleFactor();
        int maxAllowed = fileDetails.getMaxAllowed();
        try {
            Presentation pres = new Presentation((InputStream)bis);
            int slides = pres.getSlides().size();
            if (slides == 0) {
                throw new SapphireException("No slides found.");
            }
            if (fromSlide < 1) {
                fromSlide = 1;
            }
            if (toSlide < 0) {
                toSlide = maxAllowed;
            }
            if (fromSlide > toSlide) {
                fromSlide = toSlide;
            }
            if (toSlide > slides) {
                toSlide = slides;
            }
            if (toSlide - fromSlide >= maxAllowed) {
                throw new SapphireException("You can only display up to " + maxAllowed + " pages. Please choose a narrower range");
            }
            if (scaleFactor <= 0) {
                scaleFactor = 100;
            }
            fileDetails.setFromSlide(fromSlide);
            fileDetails.setToSlide(toSlide);
            fileDetails.setTotalSlidesAvailable(slides);
            fileDetails.setScaleFactor(scaleFactor);
            return pres;
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    public static StringBuilder getPptHtmlFromBis(ByteArrayInputStream bis, PPTFileDetails fileDetails, Logger logger) throws SapphireException {
        StringBuilder out = new StringBuilder();
        try {
            Presentation pres = FileManager.getPptPresentationFromBis(bis, fileDetails, logger);
            int fromSlide = fileDetails.getFromSlide();
            int toSlide = fileDetails.getToSlide();
            --fromSlide;
            --toSlide;
            String renderStyle = fileDetails.getRenderStyle();
            int scaleFactor = fileDetails.getScaleFactor();
            if (renderStyle.equalsIgnoreCase(FILELOCATIONS_IMAGE_PROPERTY)) {
                int curScaleFactor = (int)Math.ceil((double)scaleFactor * ((double)fileDetails.getImageScale() / 100.0));
                for (int i = fromSlide; i <= toSlide; ++i) {
                    ISlide slide = pres.getSlides().get_Item(i);
                    BufferedImage image = slide.getThumbnail((float)curScaleFactor / 100.0f, (float)curScaleFactor / 100.0f);
                    FileManager.writeBufferedImage(out, image, true, fileDetails.getImageQuality(), fileDetails.hasImageBorder());
                    out.append("<br>");
                }
            } else {
                HtmlOptions options = new HtmlOptions();
                options.setHtmlFormatter((IHtmlFormatter)HtmlFormatter.createDocumentFormatter((String)"", (boolean)false));
                int[] slide = new int[toSlide - fromSlide + 1];
                for (int i = fromSlide; i <= toSlide; ++i) {
                    slide[i - fromSlide] = i + 1;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                pres.save((OutputStream)bos, slide, 13, (ISaveOptions)options);
                String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                out.append(html);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not process PowerPoint presentation: " + e.getMessage());
        }
        return out;
    }

    public static Workbook getExcelWorkbookFromBis(ByteArrayInputStream bis, ExcelFileDetails fileDetails, Logger logger) throws Exception {
        try {
            String sheetName = fileDetails.getSheetName();
            Workbook workbook = new Workbook((InputStream)bis);
            int sheets = workbook.getWorksheets().getCount();
            if (sheets > 0) {
                String allNames = "";
                for (int i = 0; i < sheets; ++i) {
                    Worksheet worksheet = workbook.getWorksheets().get(i);
                    allNames = allNames + ";" + worksheet.getName();
                }
                fileDetails.setAllSheets(allNames.substring(1));
                if (!sheetName.equals("_all")) {
                    int sheetNumber = -1;
                    Worksheet selectedSheet = workbook.getWorksheets().get(sheetName);
                    if (selectedSheet != null) {
                        sheetNumber = selectedSheet.getIndex();
                    } else {
                        sheetNumber = FileManager.getInt(sheetName, 1);
                        if (sheetNumber > 0 && (selectedSheet = workbook.getWorksheets().get(sheetNumber - 1)) != null) {
                            fileDetails.setSheetName(selectedSheet.getName());
                            --sheetNumber;
                        }
                    }
                    if (sheetNumber > -1 && sheetName.length() > 0) {
                        for (int i = sheets - 1; i >= 0; --i) {
                            if (i == sheetNumber) continue;
                            workbook.getWorksheets().removeAt(i);
                        }
                    }
                }
            }
            return workbook;
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringBuilder getExcelHtmlFromBis(ByteArrayInputStream bis, ExcelFileDetails fileDetails, Logger logger) throws Exception {
        StringBuilder out;
        block16: {
            out = new StringBuilder();
            try {
                boolean showGridlines = fileDetails.isShowGridLines();
                String getUniqueid = fileDetails.getUniqueid();
                Workbook workbook = FileManager.getExcelWorkbookFromBis(bis, fileDetails, logger);
                int sheets = workbook.getWorksheets().getCount();
                if (sheets <= 0) break block16;
                if (fileDetails.getRenderStyle().equalsIgnoreCase(FILELOCATIONS_IMAGE_PROPERTY)) {
                    ImageOrPrintOptions options = new ImageOrPrintOptions();
                    options.setHorizontalResolution(96);
                    options.setVerticalResolution(96);
                    int d = (int)Math.ceil(1250.0 * ((double)fileDetails.getImageScale() / 100.0));
                    options.setDesiredSize(d, d);
                    options.setImageFormat(ImageFormat.getJpeg());
                    options.setQuality(fileDetails.getImageQuality());
                    for (int i = 0; i < workbook.getWorksheets().getCount(); ++i) {
                        Worksheet sheet = workbook.getWorksheets().get(i);
                        PageSetup pageSetup = sheet.getPageSetup();
                        pageSetup.setFitToPagesWide(1);
                        pageSetup.setFitToPagesTall(1);
                        SheetRender sr = new SheetRender(sheet, options);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        try {
                            try {
                                sr.toImage(0, (OutputStream)os);
                                if (os.size() > 0) {
                                    FileManager.writeImageStream(out, os, true, fileDetails.hasImageBorder());
                                } else {
                                    BufferedImage blank = new BufferedImage(100, 100, 1);
                                    Graphics2D g = blank.createGraphics();
                                    g.setColor(Color.WHITE);
                                    g.fillRect(0, 0, blank.getWidth(), blank.getHeight());
                                    g.dispose();
                                    FileManager.writeBufferedImage(out, blank, true, fileDetails.getImageQuality(), fileDetails.hasImageBorder());
                                }
                            }
                            finally {
                                os.close();
                            }
                            out.append("<br>");
                            continue;
                        }
                        catch (Exception e) {
                            logger.warn("Failed to write excel sheet " + i + " to image. Error: " + e.getMessage());
                        }
                    }
                    break block16;
                }
                HtmlSaveOptions options = new HtmlSaveOptions(12);
                options.setExportActiveWorksheetOnly(true);
                options.setPresentationPreference(true);
                options.setExportImagesAsBase64(true);
                options.setExportFrameScriptsAndProperties(false);
                options.setCellCssPrefix(getUniqueid + "_");
                options.setTableCssId("atable");
                options.setExcludeUnusedStyles(true);
                options.setCellCssPrefix("acell");
                options.setExportGridLines(showGridlines);
                for (int i = 0; i < workbook.getWorksheets().getCount(); ++i) {
                    workbook.getWorksheets().setActiveSheetIndex(i);
                    Worksheet sheet = workbook.getWorksheets().get(i);
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                        workbook.save((OutputStream)bos, (com.aspose.cells.SaveOptions)options);
                        String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        int p = html.indexOf("<style>");
                        html = html.substring(p);
                        html = StringUtil.replaceAll(html, "<head", "<dummy-head");
                        html = StringUtil.replaceAll(html, "</head", "</dummy-head");
                        html = StringUtil.replaceAll(html, "<body", "<dummy-body");
                        html = StringUtil.replaceAll(html, "</body", "</dummy-body");
                        html = StringUtil.replaceAll(html, "</html>", "");
                        String divid = "filecontrol_" + getUniqueid + "_" + i;
                        html = "<div style=\"border:1px solid grey\" id=\"" + divid + "\">" + html + "</div>";
                        if (workbook.getWorksheets().getCount() > 1) {
                            html = html + "<span style=\"border:1px solid grey;padding-left: 5px; padding-right: 5px\">" + sheet.getName() + "</span><br><br>";
                        }
                        Document doc = Jsoup.parse((String)html);
                        String style = "\n" + doc.select("style").first().data() + "\n";
                        style = StringUtil.replaceAll(style, "<!--table", "table");
                        style = StringUtil.replaceAll(style, "-->", "");
                        style = StringUtil.replaceAll(style, "\ntable\n", "\n#" + divid + " table\n");
                        style = StringUtil.replaceAll(style, "\ntd\n", "\n#" + divid + " td\n");
                        style = StringUtil.replaceAll(style, "\ntr\n", "\n#" + divid + " tr\n");
                        style = StringUtil.replaceAll(style, "\n." + getUniqueid + "_", "\n#" + divid + " ." + getUniqueid + "_");
                        doc.select("style").first().text(style);
                        String html2 = doc.toString();
                        out.append(html2);
                        out.append("<br><br>");
                        continue;
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException("Could not process Excel document: " + e.getMessage());
            }
        }
        return out;
    }

    public static void writeBufferedImage(StringBuilder out, BufferedImage image, boolean jpegOut, int quality) throws IOException {
        FileManager.writeBufferedImage(out, image, jpegOut, quality, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeBufferedImage(StringBuilder out, BufferedImage image, boolean jpegOut, int quality, boolean border) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
            if (jpegOut) {
                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                try {
                    ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                    jpgWriteParam.setCompressionMode(2);
                    jpgWriteParam.setCompressionQuality((float)quality / 100.0f);
                    try {
                        jpgWriter.setOutput(ImageIO.createImageOutputStream(baos));
                        IIOImage outputImage = new IIOImage(image, null, null);
                        jpgWriter.write(null, outputImage, jpgWriteParam);
                    }
                    catch (Exception e) {
                        Trace.logError(e.getMessage(), e);
                    }
                }
                finally {
                    jpgWriter.dispose();
                }
            } else {
                ImageIO.write((RenderedImage)image, "png", baos);
            }
            baos.flush();
        }
        FileManager.writeImageStream(out, baos, jpegOut, border);
    }

    private static void writeImageStream(StringBuilder out, ByteArrayOutputStream baos, boolean jpg, boolean border) throws IOException {
        FileData fileData = new FileData(baos.toByteArray(), jpg ? "image/jpeg" : "image/png");
        out.append("<img src=\"");
        out.append(fileData.getDataURL());
        if (border) {
            out.append("\" style=\"border:solid 1px black;");
        }
        out.append("\">");
    }

    private static void writeImageStream(int width, int height, StringBuilder out, ByteArrayOutputStream baos, boolean jpg, boolean border) throws IOException {
        FileData fileData = new FileData(baos.toByteArray(), jpg ? "image/jpeg" : "image/png");
        out.append("<img width=\"" + width + "\" height=\"" + height + "\" src=\"");
        out.append(fileData.getDataURL());
        if (border) {
            out.append("\" style=\"border:solid 1px black;");
        }
        out.append("\">");
    }

    public static String getUploadLocation(String locationpolicynode, String locationpolicyitem, String connectionId) throws Exception {
        String out = FileManager.getFileLocation(locationpolicynode, locationpolicyitem, connectionId);
        return out;
    }

    public static void saveAttachmentData(String sdcid, DataSet attachmentData, String connectionId) throws SapphireException {
        FileManager.saveAttachmentData(sdcid, attachmentData, "", connectionId);
    }

    private static DataSet createDataSet() {
        DataSet dataSet = new DataSet();
        dataSet.addColumn("sdcid", 0);
        dataSet.addColumn("keyid1", 0);
        dataSet.addColumn("keyid2", 0);
        dataSet.addColumn("keyid3", 0);
        dataSet.addColumn("attachmentdesc", 0);
        dataSet.addColumn("attachmentclass", 0);
        dataSet.addColumn("typeflag", 0);
        dataSet.addColumn("filename", 0);
        dataSet.addColumn("sourcefilename", 0);
        dataSet.addColumn("__rowstatus", 0);
        dataSet.addColumn("__tempid", 0);
        dataSet.addColumn("__uploadto", 0);
        dataSet.addColumn("__inputstream", -1);
        return dataSet;
    }

    public static sapphire.attachment.Attachment addFileReferenceAttachment(String sdcid, String keyid1, String keyid2, String keyid3, Path file, String attachmentClass, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionid) throws SapphireException {
        DataSet dataSet = FileManager.createDataSet();
        dataSet.addRow();
        dataSet.setValue(0, "sdcid", sdcid);
        dataSet.setValue(0, "keyid1", keyid1);
        if (keyid2.length() > 0) {
            dataSet.setValue(0, "keyid2", sdcid);
        }
        if (keyid3.length() > 0) {
            dataSet.setValue(0, "keyid3", sdcid);
        }
        dataSet.setValue(0, "typeflag", "R");
        dataSet.setValue(0, "__rowstatus", "I");
        dataSet.setValue(0, "filename", file.toString());
        if (attachmentClass != null && attachmentClass.length() > 0) {
            dataSet.setValue(0, "attachmentclass", attachmentClass);
        }
        FileManager.saveAttachmentData(sdcid, dataSet, FILELOCATIONS_POLICYNODE, ap, qp, at, connectionid);
        if (metaData != null && metaData.size() > 0 && dataSet.getInt(0, "attachmentnum", -1) > -1) {
            BaseSDIAttributeAction.addAttachmentMetaData(metaData, dataSet.getValue(0, "sdcid", ""), dataSet.getValue(0, "keyid1", ""), dataSet.getValue(0, "keyid2", ""), dataSet.getValue(0, "keyid3", ""), dataSet.getInt(0, "attachmentnum", -1), qp, sdcProcessor, ap);
        }
        return sapphire.attachment.Attachment.getAttachment(dataSet, 0, connectionid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static sapphire.attachment.Attachment addFileAttachment(String sdcid, String keyid1, String keyid2, String keyid3, Path file, String attachmentclass, boolean deleteOriginal, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionId) throws SapphireException {
        sapphire.attachment.Attachment attachment;
        FileInputStream is = new FileInputStream(file.toFile());
        try {
            attachment = FileManager.addFileAttachment(sdcid, keyid1, keyid2, keyid3, FileManager.getFileName(file.toAbsolutePath().toString(), true), attachmentclass, is, metaData, ap, qp, at, sdcProcessor, connectionId);
        }
        catch (Throwable throwable) {
            try {
                ((InputStream)is).close();
                if (deleteOriginal) {
                    try {
                        Files.delete(file);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to add file attachment.");
            }
        }
        ((InputStream)is).close();
        if (deleteOriginal) {
            try {
                Files.delete(file);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return attachment;
    }

    public static sapphire.attachment.Attachment addFileAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, String attachmentclass, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionId) throws SapphireException {
        return FileManager.addFileAttachment(sdcid, keyid1, keyid2, keyid3, "", attachmentclass, null, tempid, metaData, ap, qp, at, sdcProcessor, connectionId);
    }

    public static sapphire.attachment.Attachment addFileAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String filename, String attachmentclass, InputStream inputStream, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionId) throws SapphireException {
        return FileManager.addFileAttachment(sdcid, keyid1, keyid2, keyid3, filename, attachmentclass, inputStream, null, metaData, ap, qp, at, sdcProcessor, connectionId);
    }

    private static sapphire.attachment.Attachment addFileAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String filename, String attachmentclass, InputStream inputStream, String tempid, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionId) throws SapphireException {
        return FileManager.addFileAttachment(sdcid, keyid1, keyid2, keyid3, filename, attachmentclass, "", "", inputStream, tempid, metaData, ap, qp, at, sdcProcessor, connectionId);
    }

    private static sapphire.attachment.Attachment addFileAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String filename, String attachmentclass, String attachmentRepository, String attachmentRepositoryNode, InputStream inputStream, String tempid, PropertyList metaData, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor at, SDCProcessor sdcProcessor, String connectionId) throws SapphireException {
        DataSet dataSet = FileManager.createDataSet();
        dataSet.addRow();
        dataSet.setValue(0, "sdcid", sdcid);
        dataSet.setValue(0, "keyid1", keyid1);
        if (keyid2.length() > 0) {
            dataSet.setValue(0, "keyid2", keyid2);
        }
        if (keyid3.length() > 0) {
            dataSet.setValue(0, "keyid3", keyid3);
        }
        dataSet.setValue(0, "typeflag", Attachment.AttachmentType.FILE.getFlag());
        dataSet.setValue(0, "__rowstatus", "I");
        if (inputStream != null) {
            dataSet.setObject(0, "__inputstream", inputStream);
            dataSet.setValue(0, "filename", filename);
            dataSet.setValue(0, "sourcefilename", filename);
        } else {
            dataSet.setValue(0, "__tempid", tempid);
        }
        if (attachmentclass != null && attachmentclass.length() > 0) {
            dataSet.setValue(0, "attachmentclass", attachmentclass);
        }
        if (attachmentRepository.length() > 0) {
            dataSet.setValue(0, "attachmentrepositoryid", attachmentRepository);
            if (attachmentRepositoryNode.length() > 0) {
                dataSet.setValue(0, "attachmentrepositorynodeid", attachmentRepositoryNode);
            }
        }
        FileManager.saveAttachmentData(sdcid, dataSet, FILELOCATIONS_POLICYNODE, ap, qp, at, connectionId);
        if (metaData != null && metaData.size() > 0 && dataSet.getInt(0, "attachmentnum", -1) > -1) {
            BaseSDIAttributeAction.addAttachmentMetaData(metaData, dataSet.getValue(0, "sdcid", ""), dataSet.getValue(0, "keyid1", ""), dataSet.getValue(0, "keyid2", ""), dataSet.getValue(0, "keyid3", ""), dataSet.getInt(0, "attachmentnum", -1), qp, sdcProcessor, ap);
        }
        return sapphire.attachment.Attachment.getAttachment(dataSet, 0, connectionId);
    }

    public static void saveAttachmentData(String sdcid, DataSet attachmentData, String attachmentpolicynode, String connectionId) throws SapphireException {
        ActionProcessor ap = new ActionProcessor(connectionId);
        QueryProcessor qp = new QueryProcessor(connectionId);
        sapphire.accessor.AttachmentProcessor attachmentProcessor = new sapphire.accessor.AttachmentProcessor(connectionId);
        FileManager.saveAttachmentData(sdcid, attachmentData, attachmentpolicynode, ap, qp, attachmentProcessor, connectionId);
    }

    private static void saveAttachmentData(String sdcid, DataSet attachmentData, String attachmentpolicynode, ActionProcessor ap, QueryProcessor qp, sapphire.accessor.AttachmentProcessor attachmentProcessor, String connectionId) throws SapphireException {
        StringBuffer removekeyid1 = new StringBuffer();
        StringBuffer removekeyid2 = new StringBuffer();
        StringBuffer removekeyid3 = new StringBuffer();
        StringBuffer removeattachmentnum = new StringBuffer();
        Trace.logDebug("Processing attachments...");
        for (int i = 0; i < attachmentData.getRowCount(); ++i) {
            String tempid;
            String rowstaus = attachmentData.getValue(i, "__rowstatus", "S");
            if (rowstaus.equalsIgnoreCase("I") || rowstaus.equalsIgnoreCase("U")) {
                sapphire.attachment.Attachment ret;
                InputStream ais;
                boolean edit = rowstaus.equalsIgnoreCase("U");
                sapphire.attachment.Attachment attachment = sapphire.attachment.Attachment.getAttachment(attachmentData, i, connectionId);
                Object inputStream = null;
                boolean availableData = false;
                Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachmentData.getValue(i, "typeflag"));
                String tempid2 = attachmentData.getValue(i, "__tempid");
                InputStream inputStream2 = tempid2.length() > 0 ? null : (ais = attachmentData.getObject(i, "__inputstream") instanceof InputStream ? (InputStream)attachmentData.getObject(i, "__inputstream") : null);
                if (ais != null) {
                    attachment.setInputStream(ais);
                } else if (tempid2.length() > 0) {
                    attachment.setTempId(tempid2);
                }
                Trace.logDebug("Processing " + (edit ? "edit" : "add") + " sdi attachment...");
                sapphire.attachment.Attachment attachment2 = ret = edit ? attachmentProcessor.editSDIAttachment(attachment, false, true, attachmentpolicynode) : attachmentProcessor.addSDIAttachment(attachment, false, true, attachmentpolicynode);
                if (ret != null) {
                    ((Attachment)ret).writeToDataSet(attachmentData, i);
                    continue;
                }
                Logger.logError((edit ? "Edit" : "Add") + " SDI Attachment failed.");
                throw new SapphireException("Failed to save attachment.");
            }
            if (rowstaus.equalsIgnoreCase("D")) {
                String tempid3;
                String keyid1 = attachmentData.getValue(i, "keyid1", "");
                String keyid2 = attachmentData.getValue(i, "keyid2", "");
                String keyid3 = attachmentData.getValue(i, "keyid3", "");
                String attachmentnum = attachmentData.getValue(i, "attachmentnum", "");
                if (removekeyid1.length() > 0) {
                    removekeyid1.append(";");
                    removeattachmentnum.append(";");
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        removekeyid2.append(";");
                    }
                    if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                        removekeyid3.append(";");
                    }
                }
                removekeyid1.append(keyid1);
                removeattachmentnum.append(attachmentnum);
                if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                    removekeyid2.append(keyid2);
                }
                if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                    removekeyid3.append(keyid3);
                }
                if ((tempid3 = attachmentData.getValue(i, "__tempid")).length() <= 0) continue;
                TempFile.removeTempFile(tempid3, ap, qp, connectionId);
                continue;
            }
            if (!rowstaus.equalsIgnoreCase("X") || (tempid = attachmentData.getValue(i, "__tempid")).length() <= 0) continue;
            TempFile.removeTempFile(tempid, ap, qp, connectionId);
        }
        if (removeattachmentnum.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", removekeyid1.toString());
            if (removekeyid2.length() > 0) {
                props.setProperty("keyid2", removekeyid2.toString());
            }
            if (removekeyid3.length() > 0) {
                props.setProperty("keyid3", removekeyid3.toString());
            }
            props.setProperty("attachmentnum", removeattachmentnum.toString());
            props.setProperty("attachmentpolicynode", attachmentpolicynode);
            try {
                ap.processAction("DeleteSDIAttachment", "1", props);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to remove attachment.");
            }
        }
        Trace.logDebug("Attachments processed.");
    }

    public static int storeAsAttachment(String filedata, FileTypeGroup fileType, String sdcid, String keyid1, String keyid2, String keyid3, String filename, String attNum, boolean storeAsReference, String filelocationpolicyNode, String filelocationpolicyItem, AttachmentHandler handler, String connectionId) throws Exception {
        return FileManager.storeAsAttachment(filedata, fileType, sdcid, keyid1, keyid2, keyid3, filename, attNum, "", "", handler, connectionId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int storeAsAttachment(String filedata, FileTypeGroup fileType, String sdcid, String keyid1, String keyid2, String keyid3, String filename, String attNum, String attachmentRepositoryid, String attachmentRepositoryNode, AttachmentHandler handler, String connectionId) throws Exception {
        int attachmentNumberOut = -1;
        FileData fileData = new FileData(filedata);
        if (fileData != null && fileData.getData() != null) {
            String mimetype = fileData.getMimetype();
            byte[] imageByte = fileData.getData();
            try {
                String[] keyid1s = StringUtil.split(keyid1, ";");
                String[] keyid2s = keyid2.length() > 0 ? StringUtil.split(keyid2, ";") : new String[]{};
                String[] keyid3s = keyid3.length() > 0 ? StringUtil.split(keyid3, ";") : new String[]{};
                for (int u = 0; u < keyid1s.length; ++u) {
                    String ckeyid2;
                    String ckeyid1 = keyid1s[u];
                    String string = keyid2s.length > 0 ? (keyid2s.length == keyid1s.length ? keyid2s[u] : keyid2s[0]) : (ckeyid2 = "");
                    String ckeyid3 = keyid3s.length > 0 ? (keyid3s.length == keyid1s.length ? keyid3s[u] : keyid3s[0]) : "";
                    int attNumber = -1;
                    if (attNum.length() > 0) {
                        try {
                            attNumber = Integer.parseInt(attNum);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);){
                        sapphire.attachment.Attachment attachment = sapphire.attachment.Attachment.getAttachment(sdcid, ckeyid1, ckeyid2, ckeyid3);
                        attachment.setInputStream(bis);
                        attachment.setFilename(filename);
                        attachment.setSourceFilename(FileManager.getFileName(filename, true));
                        attachment.setDescription(FileManager.getFileName(filename, true));
                        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
                        if (attachmentRepositoryid.length() > 0) {
                            ((Attachment)attachment).setAttachmentRepositoryId(attachmentRepositoryid);
                            if (attachmentRepositoryNode.length() > 0) {
                                ((Attachment)attachment).setAttachmentRepositoryNodeId(attachmentRepositoryNode);
                            }
                        }
                        boolean isAdd = true;
                        if (attNumber > -1) {
                            isAdd = false;
                        }
                        int ret = -1;
                        sapphire.accessor.AttachmentProcessor attachmentProcessor = new sapphire.accessor.AttachmentProcessor(connectionId);
                        sapphire.attachment.Attachment outAtt = null;
                        if (!isAdd) {
                            ((Attachment)attachment).setAttachmentNum(attNumber);
                            outAtt = attachmentProcessor.editSDIAttachment(attachment);
                        } else {
                            outAtt = attachmentProcessor.addSDIAttachment(attachment, false, false, FILELOCATIONS_POLICYNODE);
                        }
                        if (outAtt != null) {
                            try {
                                attachmentNumberOut = outAtt.getAttachmentNum();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            if (handler == null) continue;
                            handler.processAttachment(bis, filename, fileType, filedata, attachmentNumberOut);
                            continue;
                        }
                        Trace.logError("Failed to upload attachment.");
                        throw new Exception("Failed to upload attachment.");
                    }
                }
            }
            catch (Exception e) {
                Trace.logError(e.getMessage());
            }
        } else {
            throw new Exception("File provided is empty.");
        }
        return attachmentNumberOut;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String storeImageAsAttachment(String image, String sdcid, String keyid1, String keyid2, String keyid3, String attNum, String attClass, String attUse, String filename, String description, boolean attachmentsByReference, String locationpolicynode, String locationpolicyitem, AttachmentHandler handler, QueryProcessor queryProcessor, sapphire.accessor.AttachmentProcessor attachmentProcessor, sapphire.accessor.ConfigurationProcessor configProcessor) {
        int i = image.indexOf("data:image/");
        StringBuffer outAttNum = new StringBuffer();
        if (i > -1) {
            String string = image = image.startsWith("data:image") ? image.substring(i + 11) : image;
            if (image.startsWith("jpeg") || image.startsWith("gif") || image.startsWith("png")) {
                i = image.indexOf(";base64,");
                if (i > -1) {
                    String type = image.substring(0, i);
                    if (type.equalsIgnoreCase("jpeg")) {
                        type = "jpg";
                    }
                    image = image.substring(i + 8);
                    try {
                        byte[] imageByte = Base64.decodeBase64((String)image);
                        String[] keyid1s = StringUtil.split(keyid1, ";");
                        String[] keyid2s = keyid2.length() > 0 ? StringUtil.split(keyid2, ";") : new String[]{};
                        String[] keyid3s = keyid3.length() > 0 ? StringUtil.split(keyid3, ";") : new String[]{};
                        String[] attNums = attNum.length() > 0 ? StringUtil.split(attNum, ";") : new String[]{};
                        for (int u = 0; u < keyid1s.length; ++u) {
                            String ckeyid3;
                            String ckeyid2;
                            String ckeyid1 = keyid1s[u];
                            String string2 = keyid2s.length > 0 ? (keyid2s.length == keyid1s.length ? keyid2s[u] : keyid2s[0]) : (ckeyid2 = "");
                            String string3 = keyid3s.length > 0 ? (keyid3s.length == keyid1s.length ? keyid3s[u] : keyid3s[0]) : (ckeyid3 = "");
                            String cattnum = attNums.length > 0 ? (attNums.length == keyid1s.length ? attNums[u] : attNums[0]) : "";
                            int attNumber = -1;
                            if (cattnum.length() > 0) {
                                try {
                                    attNumber = Integer.parseInt(cattnum);
                                }
                                catch (Exception exception) {}
                            } else if (attClass.length() > 0) {
                                StringBuffer sql = new StringBuffer();
                                SafeSQL safeSQL = new SafeSQL();
                                sql.append("SELECT attachmentnum, attachmentclass FROM sdiattachment WHERE sdcid= ").append(safeSQL.addVar(sdcid)).append(" AND keyid1= ").append(safeSQL.addVar(ckeyid1)).append(" ");
                                if (ckeyid2.length() > 0) {
                                    sql.append(" AND keyid2= ").append(safeSQL.addVar(ckeyid2)).append(" ");
                                }
                                if (ckeyid3.length() > 0) {
                                    sql.append(" AND keyid3= ").append(safeSQL.addVar(ckeyid3)).append(" ");
                                }
                                sql.append(" AND attachmentclass= ").append(safeSQL.addVar(attClass)).append(" ");
                                DataSet atts = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                if (atts != null && atts.size() > 0) {
                                    attNumber = atts.getBigDecimal(0, "attachmentnum").intValue();
                                }
                            }
                            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);){
                                String finalFilename;
                                HashMap<String, String> props = new HashMap<String, String>();
                                props.put("sdcid", sdcid);
                                props.put("keyid1", ckeyid1);
                                if (ckeyid2.length() > 0) {
                                    props.put("keyid2", ckeyid2);
                                }
                                if (ckeyid3.length() > 0) {
                                    props.put("keyid3", ckeyid3);
                                }
                                if (attClass.length() > 0) {
                                    props.put("attachmentclass", attClass);
                                }
                                if (attUse.length() > 0) {
                                    props.put("attachmentuse", attUse);
                                }
                                props.put(FILELOCATIONS_DESCRIPTION_PROPERTY, description);
                                String string4 = finalFilename = filename.endsWith("." + type) ? filename : filename + "." + type;
                                if (attachmentsByReference) {
                                    String finalfilepath;
                                    if (locationpolicyitem.length() <= 0 || locationpolicynode.length() <= 0) throw new Exception("No file location policy information provided for upload.");
                                    PropertyList flp = configProcessor.getPolicy(FILELOCATIONS_POLICY, locationpolicynode);
                                    if (flp == null || !flp.containsKey(FILELOCATIONS_LOCATIONS_COLLECTION)) throw new Exception("Could not find location policy information provided for upload.");
                                    PropertyList location = flp.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION).find(FILELOCATIONS_ID_PROPERTY, locationpolicyitem);
                                    if (location == null) {
                                        location = flp.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION).find(FILELOCATIONS_TITLE_PROPERTY, locationpolicyitem);
                                    }
                                    if (location == null || location.getProperty(FILELOCATIONS_LOCATION_PROPERTY).length() <= 0) throw new Exception("No location could be found from policy information provided for upload.");
                                    String filelocation = FileManager.getFileLocation(location.getProperty(FILELOCATIONS_LOCATION_PROPERTY));
                                    String string5 = finalfilepath = (filelocation = StringUtil.replaceAll(filelocation, "\\", "/")).endsWith("/") ? filelocation + finalFilename : filelocation + "/" + finalFilename;
                                    if (attNumber > -1) {
                                        props.put("filename", finalfilepath);
                                    } else {
                                        props.put("uploadto", finalfilepath);
                                        props.put("filename", finalfilepath);
                                    }
                                    props.put("type", "U");
                                } else {
                                    props.put("filename", finalFilename);
                                    props.put("type", "S");
                                }
                                int ret = -1;
                                if (attNumber > -1) {
                                    props.put("attachmentnum", "" + attNumber);
                                    ret = attachmentProcessor.editSDIAttachment(props, bis);
                                } else {
                                    ret = attachmentProcessor.addSDIAttachment(props, bis);
                                }
                                if (ret == 1) {
                                    int attachmentNumber = -1;
                                    try {
                                        attachmentNumber = Integer.parseInt(props.containsKey("attachmentnum") ? props.get("attachmentnum").toString() : "" + attNumber);
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                    if (handler != null) {
                                        handler.processAttachment(bis, filename, FileTypeGroup.IMAGE, image, attachmentNumber);
                                    }
                                    if (outAttNum.length() > 0) {
                                        outAttNum.append(";");
                                    }
                                    outAttNum.append(props.containsKey("attachmentnum") ? props.get("attachmentnum") : "" + attNumber);
                                    continue;
                                }
                                Trace.logError("Failed to upload attachment.");
                                continue;
                            }
                        }
                        return outAttNum.toString();
                    }
                    catch (Exception e) {
                        Trace.logError(e.getMessage());
                        return outAttNum.toString();
                    }
                }
                Trace.logError("Not a valid Base64 image.");
                return outAttNum.toString();
            }
            Trace.logError("Invalid image type, only PNG, JPG and GIF supported.");
            return outAttNum.toString();
        }
        Trace.logError("Invalid image format.");
        return outAttNum.toString();
    }

    public static String getFileLocation(String location) {
        if (location.indexOf("[") > -1) {
            String[] tokens = StringUtil.getExpressionTokens(location);
            for (int i = 0; i < tokens.length; ++i) {
                String tok = tokens[i];
                if (tok.equalsIgnoreCase("root")) {
                    location = StringUtil.replaceAll(location, "[" + tok + "]", "\\", false);
                    continue;
                }
                if (tok.equalsIgnoreCase("temp") || tok.equalsIgnoreCase("tmp") || tok.equalsIgnoreCase("tmpdir")) {
                    String tempdir = System.getProperty("java.io.tmpdir");
                    if ((tempdir = StringUtil.replaceAll(tempdir, "\\", "/", false)).endsWith("/")) {
                        tempdir = tempdir.substring(0, tempdir.length() - 1);
                    }
                    location = StringUtil.replaceAll(location, "[" + tok + "]", tempdir, false);
                    continue;
                }
                if (tok.equalsIgnoreCase("sapphirehome") || tok.equalsIgnoreCase("sapphire_home") || tok.equalsIgnoreCase("labvantagehome")) {
                    try {
                        String spphirehome = Configuration.getInstance().getSapphireHome();
                        location = StringUtil.replaceAll(location, "[" + tok + "]", spphirehome, false);
                    }
                    catch (Exception e) {
                        location = StringUtil.replaceAll(location, "[" + tok + "]", "\\", false);
                    }
                    continue;
                }
                if (!tok.equalsIgnoreCase("applicationhome") && !tok.equalsIgnoreCase("application_home")) continue;
                try {
                    String apphome = Configuration.getInstance().getApplicationHome();
                    location = StringUtil.replaceAll(location, "[" + tok + "]", apphome, false);
                    continue;
                }
                catch (Exception e) {
                    location = StringUtil.replaceAll(location, "[" + tok + "]", "\\", false);
                }
            }
            return location;
        }
        return location;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String uploadFile(String filedata, String filename, String filelocation, String connectionId) throws Exception {
        String outfilepath;
        block14: {
            outfilepath = "";
            PropertyList filelocationPolicy = new sapphire.accessor.ConfigurationProcessor(connectionId).getPolicy(FILELOCATIONS_POLICY, "Upload Custom");
            if (filelocationPolicy != null) {
                PropertyList location;
                PropertyListCollection locations = filelocationPolicy.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION);
                PropertyList propertyList = location = locations != null ? locations.find(FILELOCATIONS_ID_PROPERTY, filelocation) : null;
                if (location != null) {
                    String filepath = StringUtil.replaceAll(FileManager.getFileLocation(location.getProperty(FILELOCATIONS_LOCATION_PROPERTY)), "\\", "/");
                    if (filepath.length() > 0) {
                        File path = new File(filepath);
                        if (path.exists() && path.isDirectory()) {
                            int i;
                            if (!filepath.endsWith("/")) {
                                filepath = filepath + "/";
                            }
                            filepath = filepath + filename;
                            if (filedata.length() > 0 && (i = filedata.indexOf(",")) > -1) {
                                filedata = filedata.substring(i + 1);
                            }
                            byte[] data = null;
                            try {
                                data = Base64.decodeBase64((String)filedata);
                            }
                            catch (Exception e) {
                                Logger.logError("Could not convert file data from base64. Error = " + e.getMessage());
                            }
                            if (data != null && data.length > 0) {
                                try {
                                    FileOutputStream output = new FileOutputStream(new File(filepath));
                                    try {
                                        outfilepath = filepath;
                                        output.write(data);
                                        break block14;
                                    }
                                    finally {
                                        output.close();
                                        data = null;
                                    }
                                }
                                catch (Exception e) {
                                    throw new Exception("Could not write out file.", e);
                                }
                            }
                            throw new Exception("Could not read file data.");
                        }
                        throw new Exception("Could not validate file path in location.");
                    }
                    throw new Exception("No file path provided.");
                }
                throw new Exception("Could not obtain file location.");
            }
            throw new Exception("Could not find file location policy.");
        }
        return outfilepath;
    }

    public static PropertyListCollection getFileLocations(String locationType, PageContext pageContext) {
        return FileManager.getFileLocations(locationType, new sapphire.accessor.ConfigurationProcessor(pageContext));
    }

    private static PropertyListCollection getFileLocations(PropertyList policy) {
        if (policy != null) {
            String start = policy.getProperty(FILELOCATIONS_LOCATIONS_STARTLOCATION, "");
            PropertyListCollection locations = policy.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION);
            if (locations != null && locations.size() > 0) {
                PropertyList startLocation;
                if (start.length() > 0 && (startLocation = locations.find(FILELOCATIONS_ID_PROPERTY, start, true)) != null) {
                    startLocation.setProperty("default", "Y");
                }
            } else {
                locations = new PropertyListCollection();
                PropertyList location = new PropertyList();
                location.setProperty(FILELOCATIONS_ID_PROPERTY, "start");
                location.setProperty(FILELOCATIONS_LOCATION_PROPERTY, "[labvantagehome]");
                locations.add(location);
            }
            return locations;
        }
        return null;
    }

    private static PropertyListCollection getFileLocations(String locationType, sapphire.accessor.ConfigurationProcessor cp) {
        PropertyList policy = null;
        try {
            policy = cp.getPolicy(FILELOCATIONS_POLICY, locationType);
            if (!(policy != null && policy.size() != 0 || locationType.endsWith("Product") && locationType.endsWith("Custom") || (policy = cp.getPolicy(FILELOCATIONS_POLICY, locationType + " Custom")) != null && policy.size() != 0)) {
                policy = cp.getPolicy(FILELOCATIONS_POLICY, locationType + " Product");
            }
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
        return FileManager.getFileLocations(policy);
    }

    public static String getFileLocation(String locationType, String connectionId) {
        return FileManager.getFileLocation(locationType, null, connectionId);
    }

    private static String getFileLocation(PropertyListCollection filelocs, String locationItem) {
        if (filelocs != null && filelocs.size() > 0) {
            if (locationItem != null && locationItem.length() > 0) {
                PropertyList fileloc = filelocs.find(FILELOCATIONS_ID_PROPERTY, locationItem, true);
                if (fileloc != null) {
                    return FileManager.getFileLocation(fileloc.getProperty(FILELOCATIONS_LOCATION_PROPERTY));
                }
                fileloc = filelocs.find(FILELOCATIONS_TITLE_PROPERTY, locationItem, true);
                if (fileloc != null) {
                    return FileManager.getFileLocation(fileloc.getProperty(FILELOCATIONS_LOCATION_PROPERTY));
                }
                return FileManager.getFileLocation(filelocs.getPropertyList(0).getProperty(FILELOCATIONS_LOCATION_PROPERTY));
            }
            PropertyList fileloc = filelocs.find("default", "Y", true);
            if (fileloc == null) {
                fileloc = filelocs.getPropertyList(0);
            }
            return FileManager.getFileLocation(fileloc.getProperty(FILELOCATIONS_LOCATION_PROPERTY));
        }
        return "";
    }

    public static String getFileLocation(PropertyList filelocationPolicy) {
        return FileManager.getFileLocation(filelocationPolicy.getCollection(FILELOCATIONS_LOCATIONS_COLLECTION), null);
    }

    public static String getFileLocation(String locationType, String locationItem, String connectionId) {
        PropertyListCollection filelocs = FileManager.getFileLocations(locationType, new sapphire.accessor.ConfigurationProcessor(connectionId));
        return FileManager.getFileLocation(filelocs, locationItem);
    }

    public static PropertyList getFileTypeExcludes(String node, String connectionId) {
        return FileManager.getFileTypeExcludes(node, new sapphire.accessor.ConfigurationProcessor(connectionId));
    }

    private static PropertyList getFileTypeExcludes(PropertyList propertyList) {
        return propertyList != null ? propertyList.getPropertyList("filetypeexcludes") : null;
    }

    private static PropertyList getFileTypeExcludes(String node, sapphire.accessor.ConfigurationProcessor cp) {
        PropertyList policy = null;
        try {
            policy = cp.getPolicy(FILELOCATIONS_POLICY, node);
            if (!(policy != null && policy.size() != 0 || node.endsWith("Product") && node.endsWith("Custom") || (policy = cp.getPolicy(FILELOCATIONS_POLICY, node + " Custom")) != null && policy.size() != 0)) {
                policy = cp.getPolicy(FILELOCATIONS_POLICY, node + " Product");
            }
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
        if (policy != null) {
            return policy.getPropertyList("attachmentexcludes");
        }
        return null;
    }

    public static PropertyList getFileTypeExcludes(String node, PageContext pageContext) {
        return FileManager.getFileTypeExcludes(node, new sapphire.accessor.ConfigurationProcessor(pageContext));
    }

    public static String getExtension(String filepath) {
        return FileUtil.getExtension(filepath);
    }

    public static String getFileName(String filepath, boolean includeExtension) {
        return FileUtil.getFileName(filepath, includeExtension);
    }

    public static String getExtensions(String extensionList, String filelocationPolicyNode, String connectionId) {
        if (extensionList.length() > 0) {
            ArrayList<String> extens = new ArrayList<String>();
            String[] array = StringUtil.split(extensionList, ";");
            StringBuilder out = new StringBuilder();
            PropertyList extList = FileManager.getFileTypeExcludes(filelocationPolicyNode, connectionId);
            if (extList == null) {
                extList = new PropertyList();
            }
            for (int i = 0; i < array.length; ++i) {
                String e = array[i];
                if (e.length() <= 0) continue;
                if (!e.startsWith(".")) {
                    e = "." + e;
                }
                if (!FileManager.isValidExtension(e, extList) || extens.size() != 0 && extens.contains(e)) continue;
                extens.add(e);
                if (out.length() > 0) {
                    out.append(",");
                }
                out.append(e);
            }
            return out.toString();
        }
        return FileManager.getExtensions(filelocationPolicyNode, connectionId);
    }

    public static String getExtensions(String filelocationPolicyNode, String connectionId) {
        PropertyList extList = FileManager.getFileTypeExcludes(filelocationPolicyNode, connectionId);
        return FileManager.getExtensionsString(extList);
    }

    public static String getDefaultExtensions(PropertyList propertyList) {
        String excludes;
        String includes;
        PropertyList extList = FileManager.getFileTypeExcludes(propertyList);
        if (extList == null) {
            extList = new PropertyList();
        }
        String string = includes = extList != null ? extList.getProperty("filetypestoinclude", "").trim() : "";
        String[] includesArray = includes.length() > 0 ? (includes.indexOf(";") > -1 ? StringUtil.split(includes, ";") : StringUtil.split(includes, ",")) : new String[]{};
        String string2 = excludes = extList != null ? extList.getProperty("filetypestoexclude", "").trim() : "";
        String[] excludesArray = excludes.length() > 0 ? (excludes.indexOf(";") > -1 ? StringUtil.split(excludes, ";") : StringUtil.split(excludes, ",")) : new String[]{};
        StringBuffer out = new StringBuffer();
        for (String ext : includesArray) {
            int i = ext.indexOf(".");
            ext = i > -1 ? ext.substring(i) : "." + ext;
            boolean isValidExt = true;
            for (String exclude : excludesArray) {
                int k = exclude.indexOf(".");
                exclude = k > -1 ? exclude.substring(k) : "." + exclude;
                if (!exclude.equalsIgnoreCase(ext)) continue;
                isValidExt = false;
                break;
            }
            if (!isValidExt) continue;
            if (out.length() > 0) {
                out.append(",");
            }
            out.append(ext);
        }
        return out.toString();
    }

    public static String getExtensions(String extensionList, PropertyList propertyList) {
        if (extensionList.length() > 0) {
            ArrayList<String> extens = new ArrayList<String>();
            String[] array = StringUtil.split(extensionList, ";");
            StringBuilder out = new StringBuilder();
            PropertyList extList = FileManager.getFileTypeExcludes(propertyList);
            if (extList == null) {
                extList = new PropertyList();
            }
            for (int i = 0; i < array.length; ++i) {
                String e = array[i];
                if (e.length() <= 0) continue;
                if (!e.startsWith(".")) {
                    e = "." + e;
                }
                if (!FileManager.isValidExtension(e, extList) || extens.size() != 0 && extens.contains(e)) continue;
                extens.add(e);
                if (out.length() > 0) {
                    out.append(",");
                }
                out.append(e);
            }
            return out.toString();
        }
        return FileManager.getExtensions(propertyList);
    }

    public static String getExtensions(PropertyList propertyList) {
        PropertyList extList = FileManager.getFileTypeExcludes(propertyList);
        return FileManager.getExtensionsString(extList);
    }

    private static String getExtensionsString(PropertyList extList) {
        String includes;
        String string = includes = extList != null ? extList.getProperty("filetypestoinclude", "").trim() : "";
        if (includes.length() > 0) {
            String[] exts = includes.indexOf(";") > -1 ? StringUtil.split(includes, ";") : StringUtil.split(includes, ",");
            StringBuffer out = new StringBuffer();
            for (String ext : exts) {
                int i = ext.indexOf(".");
                ext = i > -1 ? ext.substring(i) : "." + ext;
                if (out.length() > 0) {
                    out.append(",");
                }
                out.append(ext);
            }
            return out.toString();
        }
        return "";
    }

    public static boolean isValidExtension(String ext, PropertyList extList) {
        int i;
        String[] ex;
        String includes = extList.getProperty("filetypestoinclude", "").trim();
        String excludes = extList.getProperty("filetypestoexclude", "").trim();
        boolean accept = true;
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        if (includes.length() > 0) {
            accept = false;
            for (String extension : ex = includes.indexOf(";") > -1 ? StringUtil.split(includes, ";") : StringUtil.split(includes, ",")) {
                i = extension.indexOf(".");
                if (i > -1) {
                    extension = extension.substring(i + 1);
                }
                if (!extension.equalsIgnoreCase(ext)) continue;
                accept = true;
            }
        }
        if (accept && excludes.length() > 0) {
            for (String extension : ex = excludes.indexOf(";") > -1 ? StringUtil.split(excludes, ";") : StringUtil.split(excludes, ",")) {
                i = extension.indexOf(".");
                if (i > -1) {
                    extension = extension.substring(i + 1);
                }
                if (!extension.equalsIgnoreCase(ext)) continue;
                accept = false;
            }
        }
        return accept;
    }

    public static boolean isValidExtension(String ext, String filelocationPolicyNode, String connectionId) {
        PropertyList extList = FileManager.getFileTypeExcludes(filelocationPolicyNode, connectionId);
        if (extList != null && extList.size() > 0) {
            return FileManager.isValidExtension(ext, extList);
        }
        return true;
    }

    public static boolean isValidFileType(String filename, String filelocationPolicyNode, String connectionId) {
        PropertyList extList = FileManager.getFileTypeExcludes(filelocationPolicyNode, connectionId);
        return FileManager.isValidFileTypeFromExtList(filename, extList);
    }

    public static boolean isValidFileType(String filename, PropertyList propertyList) {
        PropertyList extList = FileManager.getFileTypeExcludes(propertyList);
        return FileManager.isValidFileTypeFromExtList(filename, extList);
    }

    private static boolean isValidFileTypeFromExtList(String filename, PropertyList extList) {
        if (extList != null && extList.size() > 0) {
            if (filename.length() > 0) {
                int index;
                int lastWindowsPos;
                int extensionPos = filename.lastIndexOf(".");
                int lastUnixPos = filename.lastIndexOf("/");
                int lastSeparator = Math.max(lastUnixPos, lastWindowsPos = filename.lastIndexOf("\\"));
                int n = index = lastSeparator > extensionPos ? -1 : extensionPos;
                if (index == -1) {
                    return extList.getProperty("blankextension", "N").equalsIgnoreCase("Y");
                }
                String ext = filename.substring(index + 1);
                if (ext.length() == 0) {
                    return extList.getProperty("blankextension", "N").equalsIgnoreCase("Y");
                }
                return FileManager.isValidExtension(ext, extList);
            }
            return false;
        }
        return true;
    }

    private static int getInt(String value, int def) {
        int ret;
        try {
            ret = Integer.parseInt(value);
        }
        catch (Exception e) {
            ret = def;
        }
        return ret;
    }

    public static long getUploadMaxFileSizeMB(String attachmentPolicy, String connectionId) {
        return (long)((float)FileManager.getUploadMaxFileSize(attachmentPolicy, connectionId) / 1000000.0f);
    }

    public static long getUploadMaxFileSize(String attachmentPolicy, String connectionId) {
        return FileUpload.getMaxUploadSize(attachmentPolicy, connectionId);
    }

    public static long getUploadMaxFileSizeMB(String connectionId) {
        return FileManager.getUploadMaxFileSizeMB("", connectionId);
    }

    public static long getUploadMaxFileSize(String connectionId) {
        return FileManager.getUploadMaxFileSize("", connectionId);
    }

    public static long getDownloadMaxFileSizeMB(String connectionId) {
        long maxsize = -1L;
        ConfigurationProcessor configuration = new ConfigurationProcessor(connectionId);
        try {
            String maxDownloadSize = configuration.getSysConfigProperty("filedownloadmaxsize");
            maxsize = Long.parseLong(maxDownloadSize);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (maxsize < 0L) {
            maxsize = 1099511L;
        }
        return maxsize;
    }

    public static long getDownloadMaxFileSize(String connectionId) {
        long maxsize = -1L;
        ConfigurationProcessor configuration = new ConfigurationProcessor(connectionId);
        try {
            String maxDownloadSize = configuration.getSysConfigProperty("filedownloadmaxsize");
            maxsize = Long.parseLong(maxDownloadSize) * 1000000L;
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (maxsize < 0L) {
            maxsize = 0x10000000000L;
        }
        return maxsize;
    }

    public static long getUploadDFDMaxFileSizeMB(String connectionId) {
        return Math.round(FileManager.getUploadDFDMaxFileSize(connectionId) / 1000000L);
    }

    public static long getUploadDFDMaxFileSize(String connectionId) {
        long maxsize = -1L;
        ConfigurationProcessor configuration = new ConfigurationProcessor(connectionId);
        try {
            String maxDownloadSize = configuration.getSysConfigProperty("fileuploaddfdmaxsize");
            maxsize = maxDownloadSize.trim().length() > 0 ? ((maxsize = Long.parseLong(maxDownloadSize)) <= 0L ? FileManager.getUploadMaxFileSize(connectionId) : (maxsize *= 1000000L)) : 1000000L;
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (maxsize < 0L) {
            maxsize = 0x10000000000L;
        }
        return maxsize;
    }

    public static Path writeTable(DataSet outtable, Path filename, List<DataSetUtil.MergeColumn> mergeColumns) throws SapphireException {
        if (outtable.size() > 0 || outtable.getColumnCount() > 0) {
            if (filename == null) {
                try {
                    filename = FileUtil.createTempFile("outputtable", ".csv");
                }
                catch (Exception e) {
                    throw new SapphireException(e.getMessage(), e);
                }
            }
            outtable = DataSetUtil.mergeColumns(outtable, mergeColumns);
            CSVWriter csv = new CSVWriter(true, ',', '\"', '\\');
            csv.writeCSVToFile(outtable, filename.toFile());
            return filename;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void checkAsposeLicense() {
        ClassLoader classLoader;
        InputStream licenseStream;
        if (!com.aspose.pdf.Document.isLicensed() && (licenseStream = (classLoader = Thread.currentThread().getContextClassLoader()).getResourceAsStream("META-INF/Aspose.Total.Java.lic")) != null) {
            try {
                try {
                    com.aspose.pdf.License license1 = new com.aspose.pdf.License();
                    license1.setLicense(licenseStream);
                    licenseStream.reset();
                    com.aspose.words.License license2 = new com.aspose.words.License();
                    license2.setLicense(licenseStream);
                    licenseStream.reset();
                    com.aspose.slides.License license3 = new com.aspose.slides.License();
                    license3.setLicense(licenseStream);
                    licenseStream.reset();
                    License license4 = new License();
                    license4.setLicense(licenseStream);
                    licenseStream.reset();
                    com.aspose.imaging.License license5 = new com.aspose.imaging.License();
                    license5.setLicense(licenseStream);
                }
                finally {
                    licenseStream.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static List<DataSet> parseDataSetsFromDocumentFile(Path fileToParse, DocumentFileParsingOptions options, String connectionId) throws SapphireException {
        FileManager.checkAsposeLicense();
        options.setBackwardsCompatiableTableColumns(true);
        options.setExtractImages(false);
        options.setExtractTables(true);
        options.setExtractAsXML(false);
        options.setGeneratePDFForPS(false);
        options.setExtractText(false);
        ArrayList<DataSet> container = new ArrayList<DataSet>();
        try (InputStream fis = Files.newInputStream(fileToParse, new OpenOption[0]);){
            Path p = FileManager.parseDocumentFile(fis, FileType.getFileTypeByFileName(fileToParse.toString(), connectionId), null, options, container, connectionId);
            try {
                if (container.size() <= 0) throw new SapphireException("No table data extracted");
                ArrayList<DataSet> arrayList = container;
                return arrayList;
            }
            finally {
                try {
                    FileUtil.deleteDirectory(p.toFile());
                }
                catch (Exception exception) {}
            }
        }
        catch (IOException e) {
            throw new SapphireException("Failed to read file.", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Path parseDocumentFile(Path fileToParse, Path directory, DocumentFileParsingOptions options, String connectionId) throws SapphireException {
        Path path;
        InputStream fis = Files.newInputStream(fileToParse, new OpenOption[0]);
        try {
            path = FileManager.parseDocumentFile(fis, FileType.getFileTypeByFileName(fileToParse.toString(), connectionId), directory, options, null, connectionId);
        }
        catch (Throwable throwable) {
            try {
                fis.close();
                throw throwable;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to read file.", e);
            }
        }
        fis.close();
        return path;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Path parseDocumentFile(InputStream streamToParse, FileType fileType, Path directory, DocumentFileParsingOptions options, List<DataSet> outTables, String connectionId) throws SapphireException {
        FileManager.checkAsposeLicense();
        try {
            if (directory == null) {
                directory = Files.createTempDirectory("parsePDFOrPS", new FileAttribute[0]);
            } else if (!Files.exists(directory, new LinkOption[0])) {
                Files.createDirectory(directory, new FileAttribute[0]);
            } else if (options.getClearWorkingFolder()) {
                for (File file : directory.toFile().listFiles()) {
                    if (file.isDirectory()) continue;
                    try {
                        file.delete();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage(), e);
        }
        ParsingDocumentContainer pdc = new ParsingDocumentContainer(options, streamToParse, fileType);
        if (pdc.pdfDocument != null && options.getGeneratePDFForPS() && fileType.getName().equals("XPS")) {
            String d = directory.resolve(options.getPdfFilenamePrefix() + ".pdf").toString();
            pdc.pdfDocument.save(d);
            Logger.logDebug("PS file output as PDF.");
            if (!pdc.isAvailable()) {
                pdc.pdfDocument = new com.aspose.pdf.Document(d);
            }
        }
        if (options.getExtractText()) {
            try {
                if (pdc.pdfDocument != null) {
                    String extractedText = "";
                    TextAbsorber textAbsorber = new TextAbsorber();
                    pdc.pdfDocument.getPages().accept(textAbsorber);
                    extractedText = textAbsorber.getText();
                    if (extractedText.length() > 0) {
                        Logger.logDebug("Text Extracted");
                        Files.write(directory.resolve(options.getTextFilenamePrefix() + ".txt"), extractedText.getBytes(), new OpenOption[0]);
                    }
                } else if (pdc.wordDocument != null) {
                    TxtSaveOptions txtSaveOptions = new TxtSaveOptions();
                    txtSaveOptions.setPreserveTableLayout(true);
                    pdc.wordDocument.save(directory.resolve(options.getTextFilenamePrefix() + ".txt").toString(), (com.aspose.words.SaveOptions)txtSaveOptions);
                }
                Logger.logDebug("Text File Written");
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage(), e);
            }
        }
        boolean xmlGenerated = false;
        if (options.getExtractTables()) {
            ArrayList<DataSet> finalOuttables;
            block139: {
                finalOuttables = new ArrayList<DataSet>();
                boolean excel = false;
                boolean processXML = false;
                try {
                    if (pdc.pdfDocument != null) {
                        boolean tableFound = false;
                        int tabCheck = -1;
                        for (int i = 0; i < pdc.pdfDocument.getPages().size(); ++i) {
                            TableAbsorber tableAbsorber = new TableAbsorber();
                            tableAbsorber.visit(pdc.pdfDocument.getPages().get_Item(i + 1));
                            for (int t = 0; t < tableAbsorber.getTableList().size(); ++t) {
                                int mc;
                                if (tabCheck >= 1) continue;
                                DataSet outtable = new DataSet();
                                AbsorbedTable absorbedTable = (AbsorbedTable)tableAbsorber.getTableList().get(t);
                                for (int r = 0; r < absorbedTable.getRowList().size(); ++r) {
                                    int row = outtable.addRow();
                                    AbsorbedRow absorbedRow = (AbsorbedRow)absorbedTable.getRowList().get(r);
                                    for (int c = 0; c < absorbedRow.getCellList().size(); ++c) {
                                        if (!outtable.isValidColumn("column" + (c + 1))) {
                                            outtable.addColumn("column" + (c + 1), 0);
                                        }
                                        StringBuilder content = new StringBuilder();
                                        AbsorbedCell absorbedCell = (AbsorbedCell)absorbedRow.getCellList().get(c);
                                        for (int k = 0; k < absorbedCell.getTextFragments().size(); ++k) {
                                            content.append(absorbedCell.getTextFragments().get_Item(k + 1).getText()).append("");
                                        }
                                        outtable.setValue(row, "column" + (c + 1), content.toString().trim());
                                    }
                                }
                                if (options.getMergeColumns() != null && options.getMergeColumns().size() > 0) {
                                    if (options.getBackwardsCompatiableTableColumns()) {
                                        int cols = outtable.getColumnCount();
                                        if ((outtable = DataSetUtil.mergeColumns(outtable, options.getMergeColumns())).getColumnCount() < cols) {
                                            for (mc = 0; mc < cols - outtable.getColumnCount(); ++mc) {
                                                outtable.addColumn("premerged" + (mc + 1), 0);
                                            }
                                        }
                                    } else {
                                        outtable = DataSetUtil.mergeColumns(outtable, options.getMergeColumns());
                                    }
                                }
                                if (options.getTableColumns() != null && options.getTableColumns().length > 0) {
                                    DataSet finalOutTable = new DataSet();
                                    for (int v = 0; v < outtable.getRowCount(); ++v) {
                                        String value;
                                        int q;
                                        HashMap<String, Integer> colsFound = new HashMap<String, Integer>();
                                        for (int d = 0; d < outtable.getColumns().length; ++d) {
                                            String value2 = outtable.getValue(v, outtable.getColumnId(d), "").trim();
                                            if (value2.length() <= 0) continue;
                                            for (int c = 0; c < options.getTableColumns().length; ++c) {
                                                if (options.getBackwardsCompatiableTableColumns()) {
                                                    if (!value2.toLowerCase().trim().contains(options.getTableColumns()[c].toLowerCase().trim())) continue;
                                                    if (!finalOutTable.isValidColumn(options.getTableColumns()[c])) {
                                                        finalOutTable.addColumn(options.getTableColumns()[c], 0);
                                                    }
                                                    colsFound.put(options.getTableColumns()[c].toLowerCase(), d);
                                                    outtable.setValue(v, outtable.getColumnId(d), options.getTableColumns()[c].toLowerCase());
                                                    continue;
                                                }
                                                if (value2.toLowerCase().trim().startsWith(options.getTableColumns()[c].toLowerCase().trim())) {
                                                    if (!finalOutTable.isValidColumn(options.getTableColumns()[c])) {
                                                        finalOutTable.addColumn(options.getTableColumns()[c], 0);
                                                    }
                                                    colsFound.put(options.getTableColumns()[c].toLowerCase(), d);
                                                    continue;
                                                }
                                                if (!options.getTableColumns()[c].equalsIgnoreCase("*")) continue;
                                                colsFound.put(options.getTableColumns()[c].toLowerCase(), d);
                                            }
                                        }
                                        if (colsFound.size() != options.getTableColumns().length) continue;
                                        if (options.getTableColumns()[0].equalsIgnoreCase("*")) {
                                            finalOutTable = outtable;
                                            break;
                                        }
                                        if (options.getBackwardsCompatiableTableColumns()) {
                                            if (colsFound.size() <= 0) break;
                                            for (q = 0; q < outtable.getRowCount(); ++q) {
                                                if (q == 0) {
                                                    for (int c = 0; c < outtable.getColumns().length; ++c) {
                                                        String oldCol = outtable.getColumnId(c);
                                                        String value3 = outtable.getValue(q, oldCol, oldCol);
                                                        if (finalOutTable.isValidColumn(value3)) continue;
                                                        finalOutTable.addColumn(value3, 0);
                                                    }
                                                    continue;
                                                }
                                                int r = finalOutTable.addRow();
                                                for (int c = 0; c < outtable.getColumns().length; ++c) {
                                                    String oldCol = outtable.getColumnId(c);
                                                    value = outtable.getValue(q, oldCol, "");
                                                    finalOutTable.setValue(r, finalOutTable.getColumnId(c), value);
                                                }
                                            }
                                            break;
                                        }
                                        for (q = v + 1; q < outtable.getRowCount(); ++q) {
                                            int r = finalOutTable.addRow();
                                            for (int c = 0; c < finalOutTable.getColumns().length; ++c) {
                                                String oldCol = outtable.getColumnId((Integer)colsFound.get(finalOutTable.getColumnId(c).toLowerCase()));
                                                value = outtable.getValue(q, oldCol, "");
                                                finalOutTable.setValue(r, finalOutTable.getColumnId(c), value);
                                            }
                                        }
                                        break;
                                    }
                                    outtable = finalOutTable;
                                }
                                if (options.getEndTableText().length() > 0) {
                                    block49: for (int v = 0; v < outtable.getRowCount(); ++v) {
                                        for (int d = 0; d < outtable.getColumns().length; ++d) {
                                            String value = outtable.getValue(v, outtable.getColumnId(d), "");
                                            if (!value.toLowerCase().contains(options.getEndTableText().toLowerCase())) continue;
                                            for (int r = outtable.getRowCount() - 1; r >= v; --r) {
                                                outtable.deleteRow(r);
                                            }
                                            continue block49;
                                        }
                                    }
                                }
                                tableFound = true;
                                if (outtable.getColumns().length <= 0) continue;
                                if (options.getBackwardsCompatiableTableColumns()) {
                                    outtable = options.getMergeColumns().size() > 0 && !options.getBackwardsCompatiableTableColumns() ? DataSetUtil.mergeColumns(outtable, options.getMergeColumns()) : outtable;
                                    finalOuttables.add(outtable);
                                    continue;
                                }
                                int cols = outtable.getColumnCount();
                                DataSet dataSet = outtable = options.getMergeColumns().size() > 0 ? DataSetUtil.mergeColumns(outtable, options.getMergeColumns()) : outtable;
                                if (outtable.getColumnCount() < cols) {
                                    for (mc = 0; mc < cols - outtable.getColumnCount(); ++mc) {
                                        outtable.addColumn("merged" + (mc + 1), 0);
                                    }
                                }
                                finalOuttables.add(outtable);
                            }
                        }
                        if (!tableFound) {
                            try {
                                Logger.logDebug("Writing raw XML.");
                                pdc.pdfDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), 4);
                                processXML = true;
                                xmlGenerated = true;
                            }
                            catch (Exception e) {
                                Logger.logDebug("Failed to write as raw XML.");
                            }
                            if (!processXML) {
                                Logger.logDebug("Writing Excel XML.");
                                try {
                                    pdc.pdfDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), 9);
                                    processXML = true;
                                    xmlGenerated = true;
                                    excel = true;
                                }
                                catch (Exception e) {
                                    Logger.logDebug("Failed to write as excel XML.");
                                }
                            }
                        } else {
                            processXML = false;
                        }
                    } else if (pdc.wordDocument != null) {
                        try {
                            com.aspose.words.HtmlSaveOptions htmlSaveOptions = new com.aspose.words.HtmlSaveOptions();
                            htmlSaveOptions.setExportDocumentProperties(false);
                            htmlSaveOptions.setExportDropDownFormFieldAsText(true);
                            htmlSaveOptions.setExportFontResources(false);
                            htmlSaveOptions.setExportImagesAsBase64(true);
                            htmlSaveOptions.setExportLanguageInformation(false);
                            htmlSaveOptions.setExportTextInputFormFieldAsText(true);
                            pdc.wordDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), (com.aspose.words.SaveOptions)htmlSaveOptions);
                            processXML = true;
                            xmlGenerated = true;
                        }
                        catch (Exception e) {
                            Logger.logDebug("Failed to write as word XML.");
                        }
                    }
                    if (!processXML) break block139;
                    Logger.logDebug("XML file written...");
                    Document doc = null;
                    try {
                        doc = Jsoup.parse((File)new File(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString()), (String)Charset.defaultCharset().name());
                    }
                    catch (Exception e) {
                        throw new SapphireException(e.getMessage(), e);
                    }
                    Logger.logDebug("XML file read.");
                    Elements tables = pdc.wordDocument != null ? doc.body().select("table") : (excel ? doc.body().getElementsByTag("ss:Table") : doc.body().select("TABLE"));
                    Logger.logDebug("Table absorber loaded...");
                    int tabcount = 0;
                    for (int t = 0; t < tables.size(); ++t) {
                        DataSet outtable = new DataSet();
                        Element table = (Element)tables.get(t);
                        Elements rows = pdc.wordDocument != null ? table.select("tr") : (excel ? table.getElementsByTag("ss:Row") : table.select("TR"));
                        int startcol = 0;
                        boolean tableFound = false;
                        for (int r = 0; r < rows.size(); ++r) {
                            String val;
                            int c;
                            Element row = (Element)rows.get(r);
                            int filledcolumns = 0;
                            int tableColsFound = 0;
                            boolean endTableFound = false;
                            Elements cols = pdc.wordDocument != null ? row.select("th,td") : (excel ? row.getElementsByTag("ss:Cell") : row.select("TH,TD"));
                            for (c = 0; c < cols.size(); ++c) {
                                val = ((Element)cols.get(c)).text();
                                if (val.length() > 0) {
                                    ++filledcolumns;
                                }
                                if (options.getTableColumns().length <= 0) continue;
                                if (!tableFound) {
                                    for (int a = 0; a < options.getTableColumns().length; ++a) {
                                        if (!options.getTableColumns()[a].equalsIgnoreCase(val)) continue;
                                        ++tableColsFound;
                                    }
                                    continue;
                                }
                                if (endTableFound || options.getEndTableText().length() <= 0 || !val.toLowerCase().contains(options.getEndTableText().toLowerCase())) continue;
                                endTableFound = true;
                            }
                            if (!tableFound && options.getTableColumns().length > 0 && options.getTableColumns().length == tableColsFound) {
                                tableFound = true;
                            } else if (!tableFound && options.getTableColumns().length == 0 && filledcolumns > 1) {
                                tableFound = true;
                            } else if (tableFound && options.getEndTableText().length() == 0 && filledcolumns < 1) {
                                tableFound = false;
                            } else if (endTableFound) {
                                tableFound = false;
                            }
                            if (outtable.getColumnCount() == 0 && tableFound) {
                                for (c = 0; c < cols.size(); ++c) {
                                    val = ((Element)cols.get(c)).text();
                                    if (val.length() == 0) {
                                        val = "column" + (c + 1);
                                    }
                                    outtable.addColumn(val, 0);
                                }
                                continue;
                            }
                            if (outtable.getColumnCount() == 0 && !tableFound) continue;
                            if (outtable.getColumnCount() > 0 && !tableFound) {
                                if (options.getTableToExtract() < 1 || options.getTableToExtract() == tabcount + 1) {
                                    finalOuttables.add(options.getMergeColumns().size() > 0 ? DataSetUtil.mergeColumns(outtable, options.getMergeColumns()) : outtable);
                                }
                                ++tabcount;
                                outtable = new DataSet();
                                continue;
                            }
                            int rowindex = outtable.addRow();
                            for (int c2 = startcol; c2 < cols.size(); ++c2) {
                                if (outtable.getColumnCount() <= c2) {
                                    outtable.addColumn("column" + (c2 + 1), 0);
                                }
                                outtable.setValue(rowindex, outtable.getColumnId(c2 - startcol), ((Element)cols.get(c2)).text());
                            }
                        }
                        if (options.getTableToExtract() < 1 || options.getTableToExtract() == tabcount + 1) {
                            finalOuttables.add(options.getMergeColumns().size() > 0 ? DataSetUtil.mergeColumns(outtable, options.getMergeColumns()) : outtable);
                        }
                        ++tabcount;
                    }
                }
                finally {
                    if (processXML && !options.getExtractAsXML()) {
                        try {
                            Files.deleteIfExists(directory.resolve(options.getXmlFilenamePrefix() + ".xml"));
                        }
                        catch (Exception doc) {}
                    }
                }
            }
            if (finalOuttables.size() > 0) {
                int t;
                Logger.logDebug(finalOuttables.size() + " tables found that match options.");
                if (options.getTableColumns().length == 0) {
                    for (t = 0; t < finalOuttables.size(); ++t) {
                        DataSet table = (DataSet)finalOuttables.get(t);
                        if (table.getColumns().length <= 0 || table.getRowCount() <= 0) continue;
                        DataSet newTable = new DataSet();
                        for (int c = 0; c < table.getColumns().length; ++c) {
                            newTable.addColumn(table.getValue(0, table.getColumnId(c), table.getColumnId(c)).trim(), 0);
                        }
                        if (table.getRowCount() > 1) {
                            for (int r = 1; r < table.getRowCount(); ++r) {
                                int nR = newTable.addRow();
                                for (int c = 0; c < newTable.getColumns().length; ++c) {
                                    newTable.setValue(nR, newTable.getColumnId(c), table.getValue(r, table.getColumnId(c)));
                                }
                            }
                        }
                        finalOuttables.set(t, newTable);
                    }
                }
                if (outTables != null) {
                    if (options.getTableToExtract() < 0) {
                        for (DataSet table : finalOuttables) {
                            outTables.add(table);
                        }
                    } else if (options.getTableToExtract() <= finalOuttables.size()) {
                        outTables.add((DataSet)finalOuttables.get(options.getTableToExtract() - 1));
                    }
                } else if (options.getTableToExtract() < 0) {
                    for (t = 0; t < finalOuttables.size(); ++t) {
                        FileManager.writeTable((DataSet)finalOuttables.get(t), directory.resolve(options.getTableFilenamePrefix() + t + ".csv"), options.getMergeColumns());
                    }
                } else if (options.getTableToExtract() <= finalOuttables.size()) {
                    FileManager.writeTable((DataSet)finalOuttables.get(options.getTableToExtract() - 1), directory.resolve(options.getTableFilenamePrefix() + ".csv"), options.getMergeColumns());
                }
            } else {
                Logger.logDebug("No tables found that match options.");
            }
            Logger.logDebug("Table absorber finished.");
        }
        if (options.getExtractAsXML() && !xmlGenerated) {
            if (pdc.wordDocument != null) {
                try {
                    com.aspose.words.HtmlSaveOptions htmlSaveOptions = new com.aspose.words.HtmlSaveOptions();
                    htmlSaveOptions.setExportDocumentProperties(false);
                    htmlSaveOptions.setExportDropDownFormFieldAsText(true);
                    htmlSaveOptions.setExportFontResources(false);
                    htmlSaveOptions.setExportImagesAsBase64(true);
                    htmlSaveOptions.setExportLanguageInformation(false);
                    htmlSaveOptions.setExportTextInputFormFieldAsText(true);
                    pdc.wordDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), (com.aspose.words.SaveOptions)htmlSaveOptions);
                    xmlGenerated = true;
                }
                catch (Exception e) {
                    Logger.logDebug("Failed to write as word XML.");
                }
            } else if (pdc.pdfDocument != null) {
                try {
                    Logger.logDebug("Writing raw XML.");
                    pdc.pdfDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), 4);
                    xmlGenerated = true;
                }
                catch (Exception e) {
                    Logger.logDebug("Failed to write as raw XML.");
                    Logger.logDebug("Writing PDF XML.");
                    try {
                        pdc.pdfDocument.save(directory.resolve(options.getXmlFilenamePrefix() + ".xml").toString(), 9);
                        xmlGenerated = true;
                    }
                    catch (Throwable e2) {
                        Logger.logDebug("Failed to write as excel XML.");
                    }
                }
            }
        }
        if (options.getExtractImages()) {
            Logger.logDebug("Images processing...");
            int imcount = 0;
            if (pdc.pdfDocument != null) {
                for (Page page : pdc.pdfDocument.getPages()) {
                    for (XImage image : page.getResources().getImages()) {
                        try {
                            try (FileOutputStream fos = new FileOutputStream(directory.resolve(options.getImageFilenamePrefix() + imcount + ".png").toFile(), false);){
                                image.save((OutputStream)fos, ImageType.getPng());
                                Logger.logDebug("Image written");
                            }
                            ++imcount;
                        }
                        catch (Exception exception) {}
                    }
                }
            } else if (pdc.wordDocument != null) {
                NodeCollection shapes = pdc.wordDocument.getChildNodes(18, true);
                for (Shape shape : shapes) {
                    try {
                        if (!shape.hasImage()) continue;
                        String extension = FileFormatUtil.imageTypeToExtension((int)shape.getImageData().getImageType()).toLowerCase();
                        shape.getImageData().save(directory.resolve(options.getImageFilenamePrefix() + imcount + "" + extension).toString());
                        ++imcount;
                    }
                    catch (Exception exception) {}
                }
            }
            Logger.logDebug("Images finished.");
        }
        return directory;
    }

    public static boolean isValidMagicByte(List fileItems, String connectionId) {
        boolean valid = false;
        try {
            FileItem fifile = FileUpload.getFileItem(fileItems, "file");
            String filename = FileUpload.getFileItemValue(fileItems, "filename");
            InputStream inputStream = fifile.getInputStream();
            FileType fileType = FileType.getFileType(filename, connectionId);
            valid = FileManager.isValidMagicByte(inputStream, fileType, connectionId);
        }
        catch (Exception e) {
            Trace.logError("Failed to validate magic byte", e);
        }
        return valid;
    }

    public static boolean isValidMagicByte(sapphire.attachment.Attachment attachment, String connectionId) {
        boolean spawedStream = false;
        boolean valid = false;
        if (FileManager.isUseMagicByteOnUpload(connectionId)) {
            int maxfilesize = FileManager.getMagicbyteMaxFileSize(connectionId);
            if (maxfilesize == -1 || attachment.getSize() <= (long)(maxfilesize * 1024 * 1024)) {
                FileType fileType = FileType.getFileTypeByFileName(attachment.getFilename(), connectionId);
                if (fileType.getMagicBytes() != null && fileType.getMagicBytes().size() > 0) {
                    try {
                        if (!attachment.getInputStream().markSupported()) {
                            Path tempFile = FileUtil.createTempFile("buff", "tmp", true);
                            FileTransferOptions fto = new FileTransferOptions();
                            fto.setCloseInputStream(true);
                            fto.setCloseOutputStream(true);
                            fto.setReplaceTarget(true);
                            FileTransfer.safeDataTransfer(attachment.getInputStream(), tempFile.toFile(), fto);
                            attachment.setInputStream(new BufferedInputStream(Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE)));
                        }
                        valid = FileManager.isValidMagicByte(attachment.getInputStream(), fileType);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to validate magic byte on attachment", e);
                    }
                } else {
                    valid = true;
                }
            } else {
                Trace.logInfo("Magic byte check skipped due to attachment size.");
                valid = true;
            }
        }
        return valid;
    }

    public static boolean isValidMagicByte(InputStream inputStream, FileType fileType, File rakFile, String connectionId) {
        boolean valid;
        block5: {
            valid = false;
            if (FileManager.isUseMagicByteOnUpload(rakFile, connectionId)) {
                try {
                    int maxfilesize = FileManager.getMagicbyteMaxFileSize(rakFile, connectionId);
                    if (maxfilesize == -1 || inputStream.available() <= maxfilesize * 1024 * 1024) {
                        valid = FileManager.isValidMagicByte(inputStream, fileType);
                        break block5;
                    }
                    Trace.logInfo("Magic byte check skipped due to file size.");
                    valid = true;
                }
                catch (Exception e) {
                    Trace.logError("Failed to validate magic byte", e);
                }
            } else {
                valid = true;
            }
        }
        return valid;
    }

    public static boolean isValidMagicByte(InputStream inputStream, FileType fileType, String connectionId) {
        boolean valid;
        block5: {
            valid = false;
            if (FileManager.isUseMagicByteOnUpload(connectionId)) {
                try {
                    int maxfilesize = FileManager.getMagicbyteMaxFileSize(connectionId);
                    if (maxfilesize == -1 || inputStream.available() <= maxfilesize * 1024 * 1024) {
                        valid = FileManager.isValidMagicByte(inputStream, fileType);
                        break block5;
                    }
                    Trace.logInfo("Magic byte check skipped due to file size.");
                    valid = true;
                }
                catch (Exception e) {
                    Trace.logError("Failed to validate magic byte", e);
                }
            } else {
                valid = true;
            }
        }
        return valid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean isValidMagicByte(InputStream inputStream, FileType fileType) {
        boolean valid;
        block11: {
            valid = false;
            try {
                if (inputStream.markSupported()) {
                    PropertyListCollection magicbytes = fileType.getMagicBytes();
                    if (magicbytes == null || magicbytes.size() == 0) {
                        valid = true;
                        break block11;
                    }
                    for (Object magicbytePL1 : magicbytes) {
                        PropertyList magicbytePL = (PropertyList)magicbytePL1;
                        boolean enabled = magicbytePL.getProperty("enabled", "Y").equalsIgnoreCase("Y");
                        boolean foundenabled = false;
                        if (enabled) {
                            foundenabled = true;
                            String magicbyte = magicbytePL.getProperty("magicbyte", "");
                            String offsetStr = magicbytePL.getProperty("offset", "0");
                            byte[] magicBytes = FileManager.getByteArrayFromHexCharacters(magicbyte);
                            long offset = Long.parseLong(offsetStr);
                            int magicbyteArrLen = magicBytes.length;
                            byte[] fileContentBytes = new byte[magicbyteArrLen];
                            try {
                                inputStream.mark(0);
                                if (offset > 0L) {
                                    inputStream.skip(offset);
                                }
                                BOMInputStream bomIn = new BOMInputStream(inputStream);
                                bomIn.read(fileContentBytes, 0, magicbyteArrLen);
                                valid = Arrays.equals(magicBytes, fileContentBytes);
                            }
                            finally {
                                inputStream.reset();
                            }
                            if (valid) break block11;
                        }
                        if (foundenabled) continue;
                        valid = true;
                    }
                    break block11;
                }
                valid = true;
                Trace.logWarn("Magic byte check skippped due to incompatiable stream.");
            }
            catch (Exception e) {
                Trace.logError("Failed to validate magic byte", e);
            }
        }
        return valid;
    }

    public static byte[] getByteArrayFromHexCharacters(String hexchars) {
        hexchars = hexchars.trim();
        String[] hexcharsArr = StringUtil.split(hexchars, " ");
        byte[] bytearray = new byte[hexcharsArr.length];
        for (int i = 0; i < bytearray.length; ++i) {
            bytearray[i] = (byte)Integer.parseInt(hexcharsArr[i], 16);
        }
        return bytearray;
    }

    public static boolean isUseMagicByteOnUpload(File rakFile, String connectionId) {
        return FileManager.isUseMagicByteOnUpload(new sapphire.accessor.ConfigurationProcessor(rakFile, connectionId));
    }

    public static boolean isUseMagicByteOnUpload(String connectionId) {
        return FileManager.isUseMagicByteOnUpload(new sapphire.accessor.ConfigurationProcessor(connectionId));
    }

    public static boolean isUseMagicByteOnUpload(sapphire.accessor.ConfigurationProcessor cp) {
        boolean usemagicbyteOnUpload = false;
        try {
            PropertyList securityPolicy = cp.getPolicy("SecurityPolicy", FILELOCATIONS_POLICYNODE);
            PropertyList filerequestProps = securityPolicy.getPropertyList("filerequests");
            usemagicbyteOnUpload = filerequestProps.getProperty("usemagicbyteupload", "Y").equalsIgnoreCase("Y");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return usemagicbyteOnUpload;
    }

    public static int getMagicbyteMaxFileSize(String connectionId) {
        return FileManager.getMagicbyteMaxFileSize(new sapphire.accessor.ConfigurationProcessor(connectionId));
    }

    public static int getMagicbyteMaxFileSize(File rakFile, String connectionId) {
        return FileManager.getMagicbyteMaxFileSize(new sapphire.accessor.ConfigurationProcessor(rakFile, connectionId));
    }

    public static int getMagicbyteMaxFileSize(sapphire.accessor.ConfigurationProcessor cp) {
        int magicbytemaxfilesize = -1;
        try {
            PropertyList securityPolicy = cp.getPolicy("SecurityPolicy", FILELOCATIONS_POLICYNODE);
            PropertyList filerequestProps = securityPolicy.getPropertyList("filerequests");
            magicbytemaxfilesize = Integer.parseInt(filerequestProps.getProperty("magicbytemaxfilesize", "-1"));
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return magicbytemaxfilesize;
    }

    public static boolean isForceDownload(String connectionId) {
        return FileManager.isForceDownload(new sapphire.accessor.ConfigurationProcessor(connectionId));
    }

    public static boolean isForceDownload(sapphire.accessor.ConfigurationProcessor cp) {
        boolean forcedownload = false;
        try {
            PropertyList securityPolicy = cp.getPolicy("SecurityPolicy", FILELOCATIONS_POLICYNODE);
            PropertyList filerequestProps = securityPolicy.getPropertyList("filerequests");
            forcedownload = filerequestProps.getProperty("forcedownload", "Y").equalsIgnoreCase("Y");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return forcedownload;
    }

    public static void validateFileLocationPolicyForUpload(PropertyList sdcprops, sapphire.accessor.ConfigurationProcessor cp, String connectionId) throws ServiceException, SapphireException {
        File file = new File(sdcprops.getProperty("filename"));
        if (file.isFile() && file.exists()) {
            File tempDir;
            String uploadPath = file.getParent();
            if (uploadPath.startsWith((tempDir = new File(System.getProperty("java.io.tmpdir"))).getAbsolutePath())) {
                return;
            }
            String filelocationpolicynode = sdcprops.getProperty("filelocationpolicynode", ATTACHMENTNODE);
            PropertyList filelocationPolicy = null;
            try {
                filelocationPolicy = cp.getPolicy(FILELOCATIONS_POLICY, filelocationpolicynode);
            }
            catch (Exception e) {
                throw new SapphireException("Unable to obtain file location policy. Please make sure you have an " + filelocationpolicynode + " node.");
            }
            if (!FileManager.isValidFileLocation(uploadPath, filelocationPolicy)) {
                throw new SapphireException(" Upload location " + uploadPath + " not a valid file location. Please review 'File Locations' property in file location policy - " + filelocationpolicynode + ".");
            }
            if (!FileManager.isValidFileType(file.getName(), filelocationpolicynode, connectionId)) {
                throw new SapphireException("File type is not valid in file location policy - " + filelocationpolicynode + ".");
            }
        }
    }

    public static boolean hasIllegalJSTags(List fileItems, String connectionId) {
        boolean jsTagPresent = false;
        FileItem fifile = FileUpload.getFileItem(fileItems, "file");
        String filename = FileUpload.getFileItemValue(fileItems, "filename");
        String locationpolicynode = FileUpload.getFileItemValue(fileItems, "locationpolicynode");
        String elementid = FileUpload.getFileItemValue(fileItems, "elementid");
        jsTagPresent = SecurityPolicyUtil.hasJSTags(connectionId, filename) || SecurityPolicyUtil.hasJSTags(connectionId, locationpolicynode) || SecurityPolicyUtil.hasJSTags(connectionId, elementid);
        return jsTagPresent;
    }

    public static class FileData {
        byte[] data;
        String mimetype;
        String base64;
        Path file;
        boolean fileSystem = false;
        boolean temp = false;

        public FileData(FileType fileType) {
            this.file = null;
            this.setMimetype(fileType.getMime());
        }

        public FileData(String base64) {
            this.setBase64(base64);
        }

        public FileData(String base64, String mimetype) {
            this.setBase64(base64);
            this.setMimetype(mimetype);
        }

        public FileData(Path file, String mimetype) {
            try {
                this.setFile(file);
                this.setMimetype(mimetype);
            }
            catch (Exception e) {
                Trace.logError("Failed to set file", e);
            }
        }

        public FileData(InputStream inputStream, String mimetype, boolean useFileSystem) {
            try {
                this.fileSystem = useFileSystem;
                this.setInputStream(inputStream);
                this.setMimetype(mimetype);
            }
            catch (Exception e) {
                Trace.logError("Failed to set input stream", e);
            }
        }

        public FileData(File file, String mimetype, boolean useFileSystem) {
            try {
                this.fileSystem = useFileSystem;
                this.setFile(file.toPath());
                this.setMimetype(mimetype);
            }
            catch (Exception e) {
                Trace.logError("Failed to set file", e);
            }
        }

        public FileData(byte[] data, String mimetype) {
            this.setData(data);
            this.setMimetype(mimetype);
        }

        public String getBase64() {
            if (this.base64 == null || this.base64.length() == 0) {
                if (this.data != null && this.data.length > 0) {
                    this.base64 = Base64.encodeBase64String((byte[])this.data);
                } else if (this.file != null) {
                    this.base64 = Base64.encodeBase64String((byte[])this.getData());
                }
            }
            return this.base64;
        }

        public void setBase64(String base64) {
            if (base64.startsWith("data:")) {
                base64 = base64.substring(5);
            }
            if (base64.length() > 0) {
                int i = base64.indexOf(";base64,");
                if (i > -1) {
                    Object data = null;
                    String mimetype = base64.substring(0, i);
                    String base64data = base64.substring(i + 8);
                    this.data = data;
                    this.base64 = base64data;
                    this.setMimetype(mimetype);
                } else {
                    this.base64 = base64;
                }
            }
        }

        public String getDataURL() {
            StringBuffer out = new StringBuffer();
            out.append("data:").append(this.getMimetype()).append(";base64,");
            out.append(this.getBase64());
            return out.toString();
        }

        public void deleteFile() {
            if (this.file != null) {
                try {
                    Files.delete(this.file);
                }
                catch (Exception e) {
                    Trace.logError("Failed to remove file.", e);
                }
            }
        }

        public boolean isFileSystem() {
            return this.fileSystem;
        }

        public void setUseFileSystem(boolean useFileSystem) {
            this.fileSystem = useFileSystem;
        }

        private File createFile() throws IOException {
            byte[] b = this.getData();
            File temp = FileUtil.createTempFile(null, ".tmp").toFile();
            return temp;
        }

        public Path getFile() {
            if (this.file == null) {
                try {
                    File temp = this.createFile();
                    Path file = temp.toPath();
                    byte[] d = this.getData();
                    if (d != null) {
                        Files.write(file, d, new OpenOption[0]);
                    }
                    this.temp = true;
                    this.file = file;
                }
                catch (Exception e) {
                    Trace.logError("Failed to get path", e);
                }
            }
            return this.file;
        }

        public void setFile(Path file) {
            if (this.fileSystem) {
                if (this.file == null) {
                    this.file = file;
                } else if (Files.exists(this.file, new LinkOption[0])) {
                    try {
                        if (!Files.exists(file, new LinkOption[0]) && !Files.exists(file.getParent(), new LinkOption[0])) {
                            Files.createDirectories(file, new FileAttribute[0]);
                        }
                        Files.copy(this.file, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    if (this.temp) {
                        try {
                            Files.delete(this.file);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    this.file = file;
                    this.temp = false;
                } else {
                    this.file = file;
                }
            } else {
                this.temp = false;
                this.file = null;
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
                    Files.copy(file, byteArrayOutputStream);
                    this.data = byteArrayOutputStream.toByteArray();
                }
                catch (Exception e) {
                    Trace.logError("Failed to read file", e);
                }
            }
        }

        public byte[] getData() {
            if (this.data == null || this.data.length == 0) {
                if (this.base64 != null && this.base64.length() > 0) {
                    try {
                        this.data = Base64.decodeBase64((String)this.base64);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to get base64", e);
                    }
                } else if (this.file != null) {
                    try {
                        this.data = Files.readAllBytes(this.file);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to get bytes", e);
                    }
                }
            }
            return this.data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public InputStream getInputStream() {
            InputStream out = null;
            if (this.data == null || this.data.length == 0) {
                if (this.base64 != null && this.base64.length() > 0) {
                    try {
                        this.data = Base64.decodeBase64((String)this.base64);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to get base64", e);
                    }
                    out = new ByteArrayInputStream(this.data);
                } else if (this.file != null) {
                    try {
                        out = new FileInputStream(this.file.toFile());
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to get bytes", e);
                    }
                }
            } else {
                out = new ByteArrayInputStream(this.data);
            }
            return out;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void setInputStream(InputStream inputStream) {
            block10: {
                try {
                    try {
                        if (this.fileSystem) {
                            File temp = this.createFile();
                            this.file = temp.toPath();
                            this.temp = true;
                            Files.copy(inputStream, this.file, StandardCopyOption.REPLACE_EXISTING);
                            break block10;
                        }
                        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
                            int len;
                            byte[] buf = new byte[1024];
                            while ((len = inputStream.read(buf)) > 0) {
                                byteArrayOutputStream.write(buf, 0, len);
                            }
                            this.data = byteArrayOutputStream.toByteArray();
                        }
                    }
                    finally {
                        inputStream.close();
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to set input stream", e);
                }
            }
        }

        public void writeFile() {
            if (this.file != null) {
                try {
                    byte[] d = this.getData();
                    if (d != null) {
                        Files.write(this.file, d, new OpenOption[0]);
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to write file", e);
                }
            }
        }

        public long getSize() {
            if (this.data != null && this.data.length > 0) {
                return this.data.length;
            }
            if (this.base64 != null && this.base64.length() > 0) {
                return this.getData().length;
            }
            if (this.file != null) {
                try {
                    return Files.size(this.file);
                }
                catch (Exception e) {
                    Trace.logError("Failed to obtain size", e);
                }
            }
            return 0L;
        }

        public String getMimetype() {
            return this.mimetype;
        }

        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }
    }

    public static class TempFile
    implements JSONable {
        public static String CONNECTIONTEMP = "CONNECTIONID";
        public static String SDCTEMP = "SDC";
        private FileData data = null;
        private FileData orgdata = null;
        private String fileName = "";
        private String sourceFileName = "";
        private TempSource source = null;
        private String typeflag = Attachment.AttachmentType.FILE.getFlag();
        private String mimetype = "";
        private String thumbnail = "";
        private JSONObject attributes = new JSONObject();
        private boolean keepfile = false;

        public TempFile(String tempdata, boolean generateThumb, String connectionId) {
            this.setFromJSON(tempdata, null, generateThumb, connectionId);
        }

        public TempFile(String tempdata, FileData data, boolean generateThumb, String connectionId) {
            this.setFromJSON(tempdata, data, generateThumb, connectionId);
        }

        public TempFile(String fileName, TempSource tempSource, String connectionId) {
            this.data = new FileData(FileType.getFileTypeByFileName(fileName, connectionId));
            this.data.setUseFileSystem(true);
            this.mimetype = this.data.getMimetype();
            this.fileName = fileName;
            this.sourceFileName = fileName;
            this.source = tempSource;
            this.typeflag = Attachment.AttachmentType.FILE.getFlag();
        }

        public TempFile(FileData fileData, TempSource tempSource) {
            String fn = fileData.getFile().toString();
            this.data = fileData;
            this.mimetype = fileData.getMimetype();
            this.fileName = fn;
            this.sourceFileName = fn;
            this.source = tempSource;
            this.typeflag = Attachment.AttachmentType.FILE.getFlag();
        }

        public TempFile(FileData fileData, String fileName, TempSource tempSource, boolean generateThumb, String connectionId) {
            this.data = fileData;
            this.mimetype = fileData.getMimetype();
            this.fileName = fileName;
            this.sourceFileName = fileName;
            this.source = tempSource;
            this.typeflag = Attachment.AttachmentType.FILE.getFlag();
            if (generateThumb) {
                FileData thumb = FileManager.generateThumbnail(fileData, -1, -1, new Logger(new LogContext()), connectionId);
                this.thumbnail = thumb != null ? thumb.getDataURL() : null;
            }
        }

        public TempFile(FileData fileData, String fileName, String sourceFileName, TempSource tempSource, String typeflag, boolean generateThumb, String connectionId) {
            this.data = fileData;
            this.mimetype = fileData.getMimetype();
            this.fileName = fileName;
            this.sourceFileName = sourceFileName;
            this.source = tempSource;
            this.typeflag = typeflag;
            if (this.typeflag.length() == 0) {
                this.typeflag = Attachment.AttachmentType.FILE.getFlag();
            }
            if (generateThumb) {
                FileData thumb = FileManager.generateThumbnail(fileData, -1, -1, new Logger(new LogContext()), connectionId);
                this.thumbnail = thumb != null ? thumb.getDataURL() : null;
            }
        }

        public TempFile(String fileName, FileType type, String sourceFileName, TempSource tempSource, String typeflag, boolean generateThumb, String connectionId) {
            this.data = null;
            this.mimetype = type.getMime();
            this.fileName = fileName;
            this.sourceFileName = sourceFileName;
            this.source = tempSource;
            this.typeflag = typeflag;
            if (this.typeflag.length() == 0) {
                this.typeflag = Attachment.AttachmentType.FILE.getFlag();
            }
            if (generateThumb) {
                FileData thumb = FileManager.generateThumbnail(fileName, type.getMime(), -1, -1, new Logger(new LogContext()), connectionId);
                this.thumbnail = thumb != null ? thumb.getDataURL() : null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static int removeOldTempFiles(DateTimeUtil dtu, DBUtil db, String connectionId) throws SapphireException {
            Timestamp sditemptimeout = dtu.getTimestamp("now-7d");
            db.createPreparedResultSet("selecttemps", "SELECT tempid, tempvalue FROM sditemp WHERE moddt < ?", sditemptimeout);
            try {
                DataSet ds = new DataSet();
                ds.setResultSet(db.getResultSet("selecttemps"), true, db.getDbms());
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    TempFile t = new TempFile(ds.getClob(i, "tempvalue"), false, connectionId);
                    if (t.data == null || t.keepfile) continue;
                    t.data.deleteFile();
                }
            }
            finally {
                db.closeResultSet("selecttemps");
            }
            return db.executePreparedUpdate("DELETE FROM sditemp WHERE moddt < ?", sditemptimeout);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static int removeTempFilesForConnection(String connectionid, DBUtil dbu) throws SapphireException {
            String cid = SecurityService.decryptConnectionId(connectionid);
            String databaseid = SecurityService.getDatabaseId(cid);
            String[] parts = StringUtil.split(cid = cid.substring(databaseid.length() + 1), "-");
            if (parts.length > 1) {
                dbu.createPreparedResultSet("selecttemps", "SELECT tempid, tempvalue FROM sditemp WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=?", new Object[]{SDCTEMP, CONNECTIONTEMP, parts[0], parts[1]});
            } else {
                dbu.createPreparedResultSet("selecttemps", "SELECT tempid, tempvalue FROM sditemp WHERE sdcid=? AND keyid1=? AND keyid3=?", new Object[]{SDCTEMP, CONNECTIONTEMP, parts[0]});
            }
            try {
                DataSet ds = new DataSet();
                ds.setResultSet(dbu.getResultSet("selecttemps"), true, dbu.getDbms());
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    TempFile t = new TempFile(ds.getClob(i, "tempvalue"), false, connectionid);
                    if (t.data == null || t.keepfile) continue;
                    t.data.deleteFile();
                }
            }
            finally {
                dbu.closeResultSet("selecttemps");
            }
            if (parts.length > 1) {
                return dbu.executePreparedUpdate("DELETE sditemp WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=?", new Object[]{SDCTEMP, CONNECTIONTEMP, parts[0], parts[1]});
            }
            return dbu.executePreparedUpdate("DELETE sditemp WHERE sdcid=? AND keyid1=? AND keyid3=?", new Object[]{SDCTEMP, CONNECTIONTEMP, parts[0]});
        }

        public static void removeTempFile(String tempid, ActionProcessor ap, QueryProcessor qp, String connectionId) throws SapphireException {
            TempFile.removeTempFile("", "", "", "", tempid, ap, qp, connectionId);
        }

        public static void removeTempFile(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, ActionProcessor ap, QueryProcessor qp, String connectionId) throws SapphireException {
            TempFile t = TempFile.getTempFile(sdcid, keyid1, keyid2, keyid3, tempid, false, qp, connectionId);
            if (t != null && t.data != null) {
                t.data.deleteFile();
            }
            PropertyList remove = new PropertyList();
            if (sdcid != null && sdcid.length() > 0) {
                remove.setProperty("sdcid", sdcid);
            }
            if (keyid1 != null && keyid1.length() > 0) {
                remove.setProperty("keyid1", keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    remove.setProperty("keyid2", keyid2);
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    remove.setProperty("keyid3", keyid3);
                }
            }
            remove.setProperty("mode", "remove");
            remove.setProperty(FileManager.FILELOCATIONS_ID_PROPERTY, tempid);
            ap.processActionClass("com.labvantage.sapphire.actions.sdi.SDITemp", remove);
        }

        public static TempFile getTempFile(String tempid, boolean thumbnail, QueryProcessor queryProcessor, String connectionId) {
            return TempFile.getTempFile("", "", "", "", tempid, thumbnail, queryProcessor, connectionId);
        }

        public static TempFile getTempFile(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, boolean thumbnail, QueryProcessor queryProcessor, String connectionId) {
            TempFile outP = null;
            String sql = "";
            Object[] ob = null;
            if (keyid2 == null || keyid2.equalsIgnoreCase("(null)")) {
                keyid2 = "";
            }
            if (keyid3 == null || keyid3.equalsIgnoreCase("(null)")) {
                keyid3 = "";
            }
            if (keyid1.length() > 0) {
                sql = "SELECT tempvalue FROM sditemp WHERE  tempid=? AND sdcid = ? AND keyid1 = ?" + (keyid2.length() > 0 ? " AND keyid2=?" + (keyid3.length() > 0 ? " AND keyid3=?" : "") : "") + "";
                ob = keyid2.length() > 0 && keyid3.length() > 0 ? new Object[]{tempid, sdcid, keyid1, keyid2, keyid3} : (keyid2.length() > 0 ? new Object[]{tempid, sdcid, keyid1, keyid2} : new Object[]{tempid, sdcid, keyid1});
            } else {
                sql = "SELECT tempvalue FROM sditemp WHERE  tempid=?";
                ob = new Object[]{tempid};
            }
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, ob, true);
            if (ds != null && ds.size() > 0) {
                String outR = ds.getClob(0, "tempvalue");
                outP = new TempFile(outR, thumbnail, connectionId);
            }
            return outP;
        }

        public static String createTempDir() throws IOException {
            SecureRandom random = new SecureRandom();
            return Files.createTempDirectory(new BigInteger(130, random).toString(32), new FileAttribute[0]).toFile().getAbsolutePath();
        }

        private void setFromJSON(String tempdata, FileData data, boolean generateThumb, String connectionId) {
            try {
                JSONObject job = new JSONObject(tempdata);
                this.mimetype = job.getString("mimetype");
                if (data == null) {
                    if (job.has("data") && job.getString("data").length() > 0) {
                        this.data = new FileData(job.getString("data"), this.mimetype);
                    } else if (job.has("datafile") && job.getString("datafile").length() > 0) {
                        this.data = new FileData(new File(job.getString("datafile")), this.mimetype, true);
                    }
                } else {
                    this.data = data;
                }
                if (job.has("orgdata") && job.getString("orgdata").length() > 0) {
                    this.orgdata = new FileData(job.getString("orgdata"), this.mimetype);
                } else if (job.has("orgfile") && job.getString("orgfile").length() > 0) {
                    this.orgdata = new FileData(new File(job.getString("orgfile")), this.mimetype, true);
                }
                this.typeflag = job.getString("typeflag");
                if (this.typeflag.length() == 0) {
                    this.typeflag = Attachment.AttachmentType.FILE.getFlag();
                }
                this.fileName = job.getString("filename");
                this.sourceFileName = job.getString("sourcefilename");
                this.keepfile = job.getString("keepfile").equalsIgnoreCase("Y");
                this.source = TempSource.valueOf(job.getString("source").toUpperCase());
                this.thumbnail = job.getString("thumbnail");
                if (generateThumb && (this.thumbnail == null || this.thumbnail.length() == 0) && this.data != null) {
                    FileData thumb = FileManager.generateThumbnail(this.data, -1, -1, new Logger(new LogContext()), connectionId);
                    this.thumbnail = thumb.getDataURL();
                }
                if (job.has("attributes")) {
                    this.attributes = job.getJSONObject("attributes");
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        public void setKeepPhysicalFile(boolean keepPhysicalFile) {
            this.keepfile = keepPhysicalFile;
        }

        public boolean getKeepPhyiscalFile() {
            return this.keepfile;
        }

        private void setInternalAttribute(String name, Object value) {
            try {
                if (this.attributes.has(name)) {
                    this.attributes.remove(name);
                }
                this.attributes.put(name, value);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        public void setAttribute(String name, JSONObject value) {
            this.setInternalAttribute(name, value);
        }

        public void setAttribute(String name, String value) {
            this.setInternalAttribute(name, value);
        }

        public JSONObject getAttribute(String name, JSONObject adefault) {
            try {
                if (!this.attributes.has(name)) {
                    return adefault;
                }
                Object out = this.attributes.get(name);
                return out instanceof JSONObject ? (JSONObject)out : adefault;
            }
            catch (Exception e) {
                return adefault;
            }
        }

        public String getAttribute(String name, String adefault) {
            try {
                if (!this.attributes.has(name)) {
                    return adefault;
                }
                Object out = this.attributes.get(name);
                return out instanceof String ? (String)out : adefault;
            }
            catch (Exception e) {
                return adefault;
            }
        }

        public boolean hasAttribute(String name) {
            return this.attributes != null && this.attributes.has(name);
        }

        public String setTempFile(int maxsize, String filelocationPolicyNode, String connectionId, ActionProcessor ap) throws SapphireException {
            return this.setTempFile(null, null, null, null, maxsize, filelocationPolicyNode, connectionId, ap);
        }

        public String setTempFile(String filelocationPolicyNode, String connectionId, ActionProcessor ap) throws SapphireException {
            return this.setTempFile(null, null, null, null, -1L, filelocationPolicyNode, connectionId, ap);
        }

        public String setTempFile(long maxsize, String filelocationPolicyNode, String connectionId) throws SapphireException {
            return this.setTempFile(null, null, null, null, maxsize, filelocationPolicyNode, connectionId);
        }

        public String setTempFile(String sdcid, String keyid1, String keyid2, String keyid3, long maxsize, String filelocationPolicyNode, String connectionId) throws SapphireException {
            return this.setTempFile(sdcid, keyid1, keyid2, keyid3, maxsize, filelocationPolicyNode, connectionId, new ActionProcessor(connectionId));
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public String setTempFile(String sdcid, String keyid1, String keyid2, String keyid3, long maxsize, String filelocationPolicyNode, String connectionId, ActionProcessor ap) throws SapphireException {
            String tempid = "";
            if (!FileManager.isValidFileType(this.fileName, filelocationPolicyNode.length() > 0 ? filelocationPolicyNode : FileManager.FILELOCATIONS_POLICYNODE, connectionId)) throw new SapphireException("Invalid file type. Please check File Location policy node.");
            boolean canContinue = false;
            if (this.data != null && maxsize > -1L) {
                long size = this.data.getSize();
                if (size > -1L) {
                    if (size > maxsize) {
                        if (maxsize >= 0L) throw new SapphireException("File exceeds max size configured.");
                    }
                    canContinue = true;
                }
            } else {
                canContinue = true;
            }
            if (!canContinue) return tempid;
            PropertyList sditemp = new PropertyList();
            if (keyid1 != null && keyid1.length() > 0) {
                sditemp.setProperty("keyid1", keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    sditemp.setProperty("keyid2", keyid2);
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    sditemp.setProperty("keyid3", keyid3);
                }
            } else {
                keyid1 = CONNECTIONTEMP;
                sditemp.setProperty("keyid1", keyid1);
                String cid = SecurityService.decryptConnectionId(connectionId);
                String databaseid = SecurityService.getDatabaseId(cid);
                cid = cid.substring(databaseid.length() + 1);
                String[] parts = StringUtil.split(cid, "-");
                keyid2 = parts.length > 0 ? parts[0] : "";
                sditemp.setProperty("keyid2", keyid2);
                if (parts.length > 1) {
                    keyid3 = parts.length > 1 ? parts[1] : "";
                    sditemp.setProperty("keyid3", keyid3);
                }
            }
            if (sdcid != null && sdcid.length() > 0) {
                sditemp.setProperty("sdcid", sdcid);
            }
            String id = (this.source == TempSource.UPLOAD ? "UPL-" : (this.source == TempSource.DOWNLOAD ? "DNL-" : "ATT-")) + new SequenceProcessor(connectionId).getSequence("SDITemp", this.source == TempSource.UPLOAD ? "UPLOAD" : "ATTACHMENT");
            sditemp.setProperty("mode", "add");
            sditemp.setProperty("tempid", id);
            String orgFilename = this.fileName;
            sditemp.setProperty("tempvalue", this.toJSONString());
            ap.processActionClass("com.labvantage.sapphire.actions.sdi.SDITemp", sditemp);
            return sditemp.getProperty("tempid");
        }

        public FileData getData() {
            return this.data;
        }

        public void setData(FileData data) {
            this.data = data;
        }

        public FileData getOrgData() {
            return this.orgdata;
        }

        public void setOrgData(FileData orgdata) {
            this.orgdata = orgdata;
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getSourceFileName() {
            return this.sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public TempSource getSource() {
            return this.source;
        }

        public void setSource(TempSource source) {
            this.source = source;
        }

        public String getTypeFlag() {
            return this.typeflag;
        }

        public void setTypeFlag(String typeflag) {
            this.typeflag = typeflag;
        }

        public String getMimeType() {
            return this.mimetype;
        }

        public void setMimeType(String mimetype) {
            this.mimetype = mimetype;
        }

        public String getThumbnail() {
            return this.thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public JSONObject toJSONObject() {
            return this.toJSONObject(true, true, true);
        }

        public JSONObject toJSONObject(boolean includeData, boolean includeOriginal, boolean includeThumb) {
            JSONObject job = new JSONObject();
            try {
                if (this.data != null) {
                    if (this.data.isFileSystem()) {
                        if (includeData) {
                            job.put("datafile", this.data != null ? this.data.getFile().toString() : "");
                        }
                        if (includeOriginal) {
                            job.put("orgfile", this.orgdata != null ? this.orgdata.getFile().toString() : "");
                        }
                    } else {
                        if (includeData) {
                            job.put("data", this.data != null ? this.data.getBase64() : "");
                        }
                        if (includeOriginal) {
                            job.put("orgdata", this.orgdata != null ? this.orgdata.getBase64() : "");
                        }
                    }
                }
                job.put("filename", this.fileName);
                job.put("sourcefilename", this.sourceFileName);
                job.put("keepfile", this.keepfile ? "Y" : "N");
                job.put("source", this.source != null ? this.source.toString().toUpperCase() : TempSource.ATTACHMENT.toString());
                job.put("typeflag", this.typeflag);
                job.put("mimetype", this.mimetype);
                if (includeThumb && this.thumbnail != null) {
                    job.put("thumbnail", this.thumbnail);
                }
                if (this.attributes != null && this.attributes.length() > 0) {
                    job.put("attributes", this.attributes);
                }
                return job;
            }
            catch (Exception exception) {
                return null;
            }
        }

        @Override
        public String toJSONString() {
            JSONObject out = this.toJSONObject();
            return out == null ? "{}" : out.toString();
        }
    }

    public static interface AttachmentHandler {
        public void processAttachment(ByteArrayInputStream var1, String var2, FileTypeGroup var3, String var4, int var5) throws Exception;
    }

    public static enum TempSource {
        ATTACHMENT,
        UPLOAD,
        DOWNLOAD,
        RICHTEXT,
        CMIS;

    }

    public static enum StorageMode {
        FILESYSTEM("Y"),
        DATABASE("N"),
        CMS("C");

        private String value = "";

        private StorageMode(String value) {
            this.value = value;
        }

        public static StorageMode getByValue(String value) {
            if (value.equalsIgnoreCase("C")) {
                return CMS;
            }
            if (value.equalsIgnoreCase("N")) {
                return DATABASE;
            }
            return FILESYSTEM;
        }
    }

    private static class ParsingDocumentContainer {
        com.aspose.pdf.Document pdfDocument = null;
        com.aspose.words.Document wordDocument = null;
        DocumentFileParsingOptions options = null;
        FileType fileType = null;
        InputStream streamToParse = null;

        ParsingDocumentContainer(DocumentFileParsingOptions options, InputStream streamToParse, FileType fileType) throws SapphireException {
            this.options = options;
            this.fileType = fileType;
            this.streamToParse = streamToParse;
            this.openDocuments();
        }

        protected boolean isAvailable() {
            try {
                if (this.pdfDocument != null) {
                    this.pdfDocument.getVersion();
                    return true;
                }
                if (this.wordDocument != null) {
                    this.wordDocument.getPageCount();
                    return true;
                }
                return false;
            }
            catch (Throwable e) {
                return false;
            }
        }

        protected void openDocuments() throws SapphireException {
            if (this.fileType.getName().equals("XPS")) {
                XpsLoadOptions psLoadOptions = new XpsLoadOptions();
                this.pdfDocument = new com.aspose.pdf.Document(this.streamToParse, (LoadOptions)psLoadOptions);
                Logger.logDebug("XPS Read");
            } else if (this.fileType.getName().equals("PDF")) {
                this.pdfDocument = new com.aspose.pdf.Document(this.streamToParse);
                Logger.logDebug("PDF Read");
            } else if (this.fileType.getName().equals("DOCX") || this.fileType.getName().equals("DOC")) {
                Logger.logDebug("DOC or DOCX Read");
                try {
                    this.wordDocument = new com.aspose.words.Document(this.streamToParse);
                }
                catch (Exception e) {
                    throw new SapphireException(e.getMessage(), e);
                }
            } else {
                throw new SapphireException("Invalid file type provided.");
            }
            try {
                int fromPage = this.options.getPageFrom();
                int toPage = this.options.getPageTo();
                if (fromPage > 0 && toPage > 0) {
                    if (fromPage > toPage) {
                        throw new SapphireException("Incorrect page numbers provided");
                    }
                    if (this.wordDocument != null && (fromPage > this.wordDocument.getPageCount() || toPage > this.wordDocument.getPageCount())) {
                        throw new SapphireException("Incorrect page numbers provided for Word based document");
                    }
                    if (this.pdfDocument != null && (fromPage > this.pdfDocument.getPages().size() || toPage > this.pdfDocument.getPages().size())) {
                        throw new SapphireException("Incorrect page numbers provided for PDF/PS based document");
                    }
                    if (this.wordDocument != null) {
                        this.wordDocument = this.wordDocument.extractPages(fromPage - 1, toPage - fromPage + 1);
                    } else if (this.pdfDocument != null) {
                        int p;
                        int toPageRel = toPage;
                        for (p = fromPage - 1; p > 0; --p) {
                            this.pdfDocument.getPages().delete(p);
                            --toPageRel;
                        }
                        for (p = this.pdfDocument.getPages().size(); p > toPageRel; --p) {
                            this.pdfDocument.getPages().delete(p);
                        }
                    }
                } else if (fromPage > 0) {
                    if (this.wordDocument != null && fromPage > this.wordDocument.getPageCount()) {
                        throw new SapphireException("Incorrect page numbers provided for Word based document");
                    }
                    if (this.pdfDocument != null && fromPage > this.pdfDocument.getPages().size()) {
                        throw new SapphireException("Incorrect page numbers provided for PDF/PS based document");
                    }
                    if (this.wordDocument != null) {
                        this.wordDocument = this.wordDocument.extractPages(fromPage - 1, toPage - fromPage + 1);
                    } else if (this.pdfDocument != null) {
                        for (int p = fromPage - 1; p > 0; --p) {
                            this.pdfDocument.getPages().delete(p);
                        }
                    }
                } else if (toPage > 0) {
                    if (this.wordDocument != null && fromPage >= this.wordDocument.getPageCount()) {
                        throw new SapphireException("Incorrect page numbers provided for Word based document");
                    }
                    if (this.pdfDocument != null && fromPage >= this.pdfDocument.getPages().size()) {
                        throw new SapphireException("Incorrect page numbers provided for PDF/PS based document");
                    }
                    if (this.wordDocument != null) {
                        this.wordDocument = this.wordDocument.extractPages(fromPage - 1, toPage - fromPage + 1);
                    } else if (this.pdfDocument != null) {
                        int toPageRel = toPage;
                        for (int p = this.pdfDocument.getPages().size(); p > toPageRel; --p) {
                            this.pdfDocument.getPages().delete(p);
                        }
                    }
                }
            }
            catch (Throwable e) {
                throw new SapphireException("Failed to process pages", e);
            }
        }
    }
}

