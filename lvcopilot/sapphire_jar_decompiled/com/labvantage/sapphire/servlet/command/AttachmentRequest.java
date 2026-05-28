/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.io.input.BOMInputStream
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_File;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.servlet.command.CommandException;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.servlet.command.MultipartRequest;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.servlet.command.fileupload.FileUploadException;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.http.HttpSender;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SDIList;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AttachmentRequest
extends MultipartRequest {
    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;

    public void view(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.view(request, response, servletContext, request.getParameter("download") != null && request.getParameter("download").equalsIgnoreCase("Y"));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void respondAttachment(sapphire.attachment.Attachment attachment, boolean isDownload, String connectionid, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws SapphireException, IOException {
        Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getType());
        String filename = AttachmentRequest.getFileName(attachment);
        InputStream is = AttachmentRequest.getInputStream(attachment, isDownload, "Y".equals(request.getParameter("inframe")));
        if (is == null) throw new SapphireException("No data returned in attachment.");
        long max = FileManager.getDownloadMaxFileSize(connectionid);
        if (attachment.getSize() >= max) throw new SapphireException(max == 0L ? "Downloads disabled in system configuration from download max set to zero" : "Size of file is larger than max download size.");
        if (attachment.isInvalidHash()) {
            throw new SapphireException("Hash check on file failed. File '" + filename + "' is corrupted.");
        }
        AttachmentRequest.setUpResponse(request, response, servletContext, is, type, filename, attachment.getOleClass(), isDownload);
        AttachmentRequest.streamAttachment(is, (OutputStream)response.getOutputStream());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void view(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, boolean isDownload) throws ServletException {
        sapphire.attachment.Attachment attachment;
        String connectionid;
        block28: {
            String keyid3;
            connectionid = "";
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            if (requestContext != null) {
                connectionid = requestContext.getConnectionId();
            }
            Logger.logDebug("ATTACHMENT", "View attachment connection: " + connectionid);
            if (connectionid == null || connectionid.length() <= 0) throw new LoginException(request.getQueryString(), "In order to access this resource, you must login.");
            if (!this.hasViewAttachmentAcess(request, connectionid)) {
                throw new CommandException("Due to departmental security You are not allowed to do the requested 'ViewAttachment' operation.", "ViewAttachment");
            }
            if (FileManager.isForceDownload(connectionid)) {
                isDownload = true;
            }
            String sdcid = request.getParameter("sdcid");
            String keyid1 = request.getParameter("keyid1");
            int attachmentnum = -1;
            String attachmentclass = request.getParameter("attachmentclass");
            String tempid = request.getParameter("tempid");
            try {
                String an = request.getParameter("attachmentnum");
                if (an == null || an.length() == 0) {
                    if (!(attachmentclass != null && attachmentclass.length() != 0 || tempid != null && tempid.length() != 0)) {
                        attachmentnum = 1;
                    }
                } else {
                    attachmentnum = Integer.parseInt(an);
                }
            }
            catch (NumberFormatException e) {
                attachmentnum = 1;
            }
            int auditsequence = -1;
            try {
                String an = request.getParameter("auditsequence");
                auditsequence = Integer.parseInt(an);
            }
            catch (NumberFormatException e) {
                auditsequence = -1;
            }
            String cid = SecurityService.decryptConnectionId(connectionid);
            String databaseid = SecurityService.getDatabaseId(cid);
            cid = cid.substring(databaseid.length() + 1);
            String[] cparts = sapphire.util.StringUtil.split(cid, "-");
            String keyid2 = request.getParameter("keyid2");
            if (keyid2 == null || keyid2.length() == 0) {
                keyid2 = tempid != null && tempid.length() > 0 && sdcid != null && sdcid.equals(FileManager.TempFile.SDCTEMP) && keyid1 != null && keyid1.equals(FileManager.TempFile.CONNECTIONTEMP) ? (cparts.length > 0 ? cparts[0] : "") : "(null)";
            }
            if ((keyid3 = request.getParameter("keyid3")) == null || keyid3.length() == 0) {
                keyid3 = tempid != null && tempid.length() > 0 && sdcid != null && sdcid.equals(FileManager.TempFile.SDCTEMP) && keyid1 != null && keyid1.equals(FileManager.TempFile.CONNECTIONTEMP) ? (cparts.length > 1 ? cparts[1] : "") : "(null)";
            }
            Attachment.ThumbnailGeneration thumbnailGeneration = Attachment.ThumbnailGeneration.getThumbnailGeneration(request.getParameter("thumbnail"));
            attachment = null;
            if (sdcid != null && sdcid.length() > 0 && keyid1 != null && keyid1.length() > 0 && (attachmentnum > -1 || attachmentclass != null && attachmentclass.length() != 0 || tempid != null && tempid.length() != 0)) {
                try {
                    AttachmentProcessor arp = new AttachmentProcessor(connectionid);
                    if (attachmentnum > -1) {
                        attachment = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum);
                        if (auditsequence >= 0) {
                            this.checkAttachmentClassAccess(connectionid, sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence);
                            String allowlocalcaching = request.getParameter("allowlocalcaching");
                            if (allowlocalcaching != null && allowlocalcaching.equalsIgnoreCase("N")) {
                                ((Attachment)attachment).setAllowLocalCache(false);
                            }
                            arp.getSDIAttachment(attachment, auditsequence, thumbnailGeneration == null ? Attachment.ThumbnailGeneration.DISABLED : thumbnailGeneration);
                        } else {
                            this.checkAttachmentClassAccess(connectionid, sdcid, keyid1, keyid2, keyid3, attachmentnum, -1);
                            arp.getSDIAttachment(attachment, thumbnailGeneration == null ? Attachment.ThumbnailGeneration.DISABLED : thumbnailGeneration);
                        }
                        break block28;
                    }
                    attachment = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3);
                    if (tempid != null && tempid.length() > 0) {
                        attachment.setTempId(tempid);
                        arp.getTempAttachment(attachment, thumbnailGeneration == null ? Attachment.ThumbnailGeneration.DISABLED : thumbnailGeneration);
                        break block28;
                    }
                    attachment.setAttachmentClass(attachmentclass);
                    this.checkAttachmentClassAccess(connectionid, sdcid, attachmentclass);
                    arp.getSDIAttachment(attachment, thumbnailGeneration == null ? Attachment.ThumbnailGeneration.DISABLED : thumbnailGeneration);
                }
                catch (Exception e) {
                    this.sendError(response, "Failed to retreive the attachment: " + e.getMessage(), e);
                }
            } else {
                String attachmenttype = request.getParameter("attachmenttype");
                String attachmentclob = request.getParameter("attachmentclob");
                if (attachmenttype == null || attachmenttype.length() <= 0 || attachmentclob == null || attachmentclob.length() <= 0) throw new CommandException("You need to pass a valid SDI attachment", "viewattachment");
                Attachment.AttachmentType attType = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachmenttype);
                if (attType == Attachment.AttachmentType.PLAINTEXT || attType == Attachment.AttachmentType.RICHTEXT || attType == Attachment.AttachmentType.URL) {
                    attachment = new Attachment();
                    String attachmentdesc = request.getParameter("attachmentdesc");
                    attachment.setDescription(attachmentdesc != null ? attachmentdesc : "");
                    attachment.setAttachmentType(attType);
                    attachment.setClob(HttpUtil.decodeURIComponent(attachmentclob));
                    attachment.setFilename("unsaved" + (attType == Attachment.AttachmentType.PLAINTEXT ? ".txt" : (attType == Attachment.AttachmentType.RICHTEXT ? ".html" : ".url")));
                    attachment.setSourceFilename(attachment.getFilename());
                } else {
                    Trace.logError("Direct viewing of attachment data only compatible with Plain Text and Rich Text.");
                    throw new CommandException("You need to pass a valid SDI attachment", "viewattachment");
                }
            }
        }
        try {
            if (attachment != null) {
                try {
                    AttachmentRequest.respondAttachment(attachment, isDownload, connectionid, request, response, servletContext);
                    return;
                }
                catch (Exception e) {
                    this.sendError(response, e.getMessage(), e);
                }
                return;
            }
            this.sendError(response, "No attachment available.", null);
            return;
        }
        catch (Exception e) {
            this.sendError(response, "Failed to stream the attachment: " + e.getMessage(), e);
        }
    }

    private void sendError(HttpServletResponse response, String error, Throwable e) {
        try {
            response.sendError(400, error);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (e != null) {
            Trace.logError(error, e);
        } else {
            Trace.logError(error);
        }
    }

    private static String getMimeType(String filename, ServletContext sc, ConfigurationProcessor cp) {
        return FileType.getFileType(filename, cp).getMime();
    }

    private static void setUpViewURL(HttpServletResponse response, String filename, boolean download) throws IOException {
        AttachmentRequest.setUpViewClob(response, filename, download);
    }

    static InputStream getInputStream(sapphire.attachment.Attachment attachment, boolean download, boolean isInframe) throws IOException {
        InputStream in = null;
        try {
            switch (attachment.getAttachmentType()) {
                case PLAINTEXT: {
                    in = new ByteArrayInputStream(AttachmentRequest.getClob(attachment, !download).getBytes("UTF8"));
                    break;
                }
                case RICHTEXT: {
                    in = new ByteArrayInputStream(AttachmentRequest.getClob(attachment, false).getBytes("UTF8"));
                    break;
                }
                case URL: {
                    in = new ByteArrayInputStream(AttachmentRequest.getURL(attachment, download, isInframe).getBytes("UTF8"));
                    break;
                }
                default: {
                    in = new BufferedInputStream(attachment.getInputStream());
                    break;
                }
            }
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return in;
    }

    static InputStream getInputStream(sapphire.attachment.Attachment attachment, boolean download) throws IOException {
        return AttachmentRequest.getInputStream(attachment, download, false);
    }

    public static String getFileName(sapphire.attachment.Attachment attachment) {
        String filename;
        String string = filename = attachment.getSourceFilename() == null || attachment.getSourceFilename().length() == 0 ? "" : attachment.getSourceFilename();
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE) {
            if (filename.length() == 0) {
                String string2 = filename = attachment.getFilename() != null && attachment.getFilename().length() > 0 ? attachment.getFilename() : "";
            }
            filename = filename.length() == 0 ? "attachment(" + attachment.getAttachmentNum() + ")" : FileManager.getFileName(filename, true);
        } else {
            if (filename.length() == 0) {
                String string3 = filename = attachment.getDescription() == null || attachment.getDescription().length() == 0 ? "" : attachment.getDescription();
            }
            if (filename.length() == 0) {
                filename = "attachment(" + attachment.getAttachmentNum() + ")";
            }
            switch (attachment.getAttachmentType()) {
                case PLAINTEXT: {
                    filename = FileUtil.getFileName(filename, false) + ".txt";
                    break;
                }
                case RICHTEXT: {
                    filename = FileUtil.getFileName(filename, false) + ".html";
                    break;
                }
                case URL: {
                    filename = FileUtil.getFileName(filename, false) + ".url";
                }
            }
        }
        return filename;
    }

    private static void setUpViewClob(HttpServletResponse response, String filename, boolean download) throws IOException {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0L);
        if (download) {
            response.setHeader("Content-Disposition", "attachment;filename=" + HttpUtil.encodeURIComponent(filename));
            response.setContentType("application/download");
        }
    }

    private static String getClob(sapphire.attachment.Attachment attachment, boolean replacebreaks) {
        String o = attachment.getClob();
        if (o != null && o.length() > 0) {
            if (replacebreaks) {
                o = o.replaceAll("\r\n", "</br>");
                o = SafeHTML.encodeForHTML(o);
            }
            return o;
        }
        return "";
    }

    private static String getURL(sapphire.attachment.Attachment attachment, boolean isURLFile, boolean isInframe) {
        String url = attachment.getUrl();
        if (url == null) {
            url = attachment.getClob();
            Trace.logDebug("URL From Clob");
        } else {
            Trace.logDebug("URL From URL");
        }
        Trace.logDebug("URL = " + url);
        if (url != null) {
            url = sapphire.util.StringUtil.replaceAll(url, "[sdcid]", attachment.getSDCId());
            url = sapphire.util.StringUtil.replaceAll(url, "[keyid1]", attachment.getKeyId1());
            url = sapphire.util.StringUtil.replaceAll(url, "[keyid2]", attachment.getKeyId2());
            if (!(url = sapphire.util.StringUtil.replaceAll(url, "[keyid3]", attachment.getKeyId3())).startsWith("http") && !url.startsWith("rc?")) {
                url = "http://" + url;
            }
            url = isURLFile ? "[InternetShortcut]\nURL=" + url + "\n" : "<script>\nwindow.onload = window.open(\"" + url + "\",\"" + (isInframe ? "_blank" : "_self") + "\");\n</script>";
        }
        return url;
    }

    private static void setUpResponse(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, InputStream is, Attachment.AttachmentType type, String filename, String oleclass, boolean download) throws IOException {
        if (type == Attachment.AttachmentType.PLAINTEXT) {
            AttachmentRequest.setUpViewClob(response, filename, download);
        } else if (type == Attachment.AttachmentType.RICHTEXT) {
            AttachmentRequest.setUpViewClob(response, filename, download);
        } else if (type == Attachment.AttachmentType.URL) {
            AttachmentRequest.setUpViewURL(response, filename, download);
        } else {
            AttachmentRequest.setUpViewInputSteam(request, response, servletContext, is, filename, oleclass, download);
        }
    }

    private static String setUpViewInputSteam(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, InputStream is, String filename, String oleclass, boolean download) throws IOException {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0L);
        Browser browser = new Browser(request);
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        ConfigurationProcessor cp = new ConfigurationProcessor(requestContext.getConnectionId());
        if (download) {
            response.setContentType("application/download");
            if (browser.isSafari() && browser.getVersion() < 6.0) {
                response.setHeader("Content-Disposition", "attachment;filename=" + HttpUtil.encodeURIComponent(filename));
            } else {
                response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + HttpUtil.encodeURIComponent(filename));
            }
        } else {
            if (browser.isSafari() && browser.getVersion() < 6.0) {
                response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent(filename));
            } else {
                response.setHeader("Content-Disposition", "inline;filename*=UTF-8''" + HttpUtil.encodeURIComponent(filename));
            }
            String mimetype = request.getParameter("mimetype");
            if (mimetype == null) {
                mimetype = "";
            }
            if (mimetype.length() == 0 && oleclass != null && oleclass.length() > 0) {
                if (oleclass.equals("Microsoft Word Document")) {
                    mimetype = "application/msword";
                } else if (oleclass.equals("Microsoft Excel Worksheet")) {
                    mimetype = "application/vnd.ms-excel";
                } else if (oleclass.equals("Media Clip")) {
                    mimetype = "video/mpeg";
                } else if (oleclass.equals("Video Clip")) {
                    mimetype = "video/x-msvideo";
                } else if (oleclass.equals("Bitmap Image")) {
                    mimetype = "image/bmp";
                } else if (oleclass.equals("Package")) {
                    mimetype = "image/gif";
                }
            } else if (mimetype.length() == 0 && filename != null && filename.length() > 3 && (mimetype = AttachmentRequest.getMimeType(filename, servletContext, cp)).equalsIgnoreCase("application/xml") && is != null) {
                String encoding = "";
                XMLStreamReader xmlStreamReader = null;
                try {
                    if (is.markSupported()) {
                        is.mark(is.available());
                    }
                    BOMInputStream bomIn = new BOMInputStream(is);
                    xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader((InputStream)bomIn));
                    encoding = xmlStreamReader.getCharacterEncodingScheme();
                    if (is.markSupported()) {
                        is.reset();
                    }
                }
                catch (XMLStreamException e) {
                    encoding = "";
                }
                catch (Exception e) {
                    throw e;
                }
                finally {
                    try {
                        if (xmlStreamReader != null) {
                            xmlStreamReader.close();
                        }
                    }
                    catch (Exception e) {
                        throw new IOException(e);
                    }
                }
                if (encoding != null && encoding.trim().length() > 0) {
                    mimetype = mimetype + "; charset=" + encoding;
                }
            }
            if (mimetype == null || mimetype.length() == 0) {
                response.setContentType("application/download");
                if (browser.isSafari() && browser.getVersion() < 6.0) {
                    response.setHeader("Content-Disposition", "attachment;filename=" + HttpUtil.encodeURIComponent(filename));
                } else {
                    response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + HttpUtil.encodeURIComponent(filename));
                }
            } else {
                response.setContentType(mimetype);
            }
        }
        return filename;
    }

    public static void streamAttachment(InputStream input, OutputStream output) throws IOException {
        AttachmentRequest.streamAttachment(input, output, true);
    }

    public static void streamAttachment(InputStream input, OutputStream output, boolean closeOutput) throws IOException {
        FileTransferOptions fileTransferOptions = new FileTransferOptions();
        fileTransferOptions.setCloseInputStream(true);
        fileTransferOptions.setCloseOutputStream(closeOutput);
        try {
            FileTransfer.safeDataTransfer(input, output, fileTransferOptions);
        }
        catch (Throwable e) {
            throw new IOException("Unable to perform a data transfer", e);
        }
    }

    private void logError(StringBuilder errorBuff, String error, Throwable throwable) {
        if (errorBuff.length() > 0) {
            errorBuff.append("\n");
        }
        errorBuff.append(error);
        if (throwable != null) {
            Trace.logError(error, throwable);
        } else {
            Trace.logError(error);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void download(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String connectionid = "";
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        if (requestContext != null) {
            connectionid = requestContext.getConnectionId();
        }
        Logger.logDebug("ATTACHMENT", "Download attachment connection: " + connectionid);
        if (connectionid == null || connectionid.length() <= 0) throw new LoginException(request.getQueryString(), "In order to access this resource, you must login.");
        ConfigurationProcessor cp = new ConfigurationProcessor(connectionid);
        String sdcidlist = request.getParameter("sdcid");
        if (sdcidlist == null || sdcidlist.length() <= 0) throw new CommandException("You need to pass a valid SDI attachment (sdcid)", "downloadattachment");
        String[] sdcid = request.getParameter("sdcid") != null ? StringUtil.split(request.getParameter("sdcid"), ";") : null;
        String[] keyid1 = request.getParameter("keyid1") != null ? StringUtil.split(request.getParameter("keyid1"), ";") : null;
        String[] keyid2 = request.getParameter("keyid2") != null ? StringUtil.split(request.getParameter("keyid2"), ";") : null;
        String[] keyid3 = request.getParameter("keyid3") != null ? StringUtil.split(request.getParameter("keyid3"), ";") : null;
        String[] attachmentclass = request.getParameter("attachmentclass") != null ? StringUtil.split(request.getParameter("attachmentclass"), ";") : null;
        String[] attachmentnum = request.getParameter("attachmentnum") != null ? StringUtil.split(request.getParameter("attachmentnum"), ";") : null;
        String[] auditsequence = request.getParameter("auditsequence") != null ? StringUtil.split(request.getParameter("auditsequence"), ";") : null;
        String[] tempid = request.getParameter("tempid") != null ? StringUtil.split(request.getParameter("tempid"), ";") : null;
        String[] attachmenttype = request.getParameter("attachmenttype") != null ? StringUtil.split(request.getParameter("attachmenttype"), ";") : null;
        String[] attachmentclob = request.getParameter("attachmentclob") != null ? StringUtil.split(request.getParameter("attachmentclob"), ";") : null;
        String[] attachmentdesc = request.getParameter("attachmentdesc") != null ? StringUtil.split(request.getParameter("attachmentdesc"), ";") : null;
        boolean lockattachment = request.getParameter("lockattachment") != null && "Y".equalsIgnoreCase(request.getParameter("lockattachment"));
        Attachment.ThumbnailGeneration thumbnailGeneration = Attachment.ThumbnailGeneration.getThumbnailGeneration(request.getParameter("thumbnail"));
        if (keyid1.length <= 0) throw new CommandException("You need to pass a valid SDI attachment (keyid1)", "downloadattachment");
        if (keyid1.length == 1) {
            try {
                this.view(request, response, servletContext, true);
                return;
            }
            catch (Exception e) {
                response.setContentType("text/html");
                throw e;
            }
        }
        try {
            Path downloadZip = FileUtil.createTempFile("downloadzip", ".zip");
            try {
                StringBuilder errorFound = new StringBuilder();
                try (OutputStream fos = Files.newOutputStream(downloadZip, new OpenOption[0]);
                     ZipOutputStream zipOutputStream = new ZipOutputStream(fos);){
                    for (int i = 0; i < keyid1.length; ++i) {
                        String currkeyid3;
                        String currkeyid2;
                        String currsdcid = sdcid.length > i ? sdcid[i] : sdcid[0];
                        String currkeyid1 = keyid1[i];
                        String string = currkeyid2 = keyid2 != null && keyid2.length > i ? keyid2[i] : "(null)";
                        if (currkeyid2.length() == 0) {
                            currkeyid2 = "(null)";
                        }
                        String string2 = currkeyid3 = keyid3 != null && keyid3.length > i ? keyid3[i] : "(null)";
                        if (currkeyid3.length() == 0) {
                            currkeyid3 = "(null)";
                        }
                        String currattachmentclass = attachmentclass != null && attachmentclass.length > i ? attachmentclass[i] : "";
                        String currenttempid = tempid != null && tempid.length > i ? tempid[i] : "";
                        String currentattachmentclob = attachmentclob != null && attachmentclob.length > i ? attachmentclob[i] : "";
                        String currentattachmenttype = attachmenttype != null && attachmenttype.length > i ? attachmenttype[i] : "";
                        String currentattachmentdesc = attachmentdesc != null && attachmentdesc.length > i ? attachmentdesc[i] : "";
                        String can = attachmentnum != null && attachmentnum.length > i ? attachmentnum[i] : "";
                        String cas = auditsequence != null && auditsequence.length > i ? auditsequence[i] : "";
                        int currattachmentnum = -1;
                        try {
                            if (can != null && can.length() > 0) {
                                currattachmentnum = Integer.parseInt(can);
                            }
                        }
                        catch (NumberFormatException e) {
                            currattachmentnum = -1;
                        }
                        int currauditsequence = -1;
                        try {
                            currauditsequence = Integer.parseInt(cas);
                        }
                        catch (NumberFormatException e) {
                            currauditsequence = -1;
                        }
                        try {
                            AttachmentProcessor arp = new AttachmentProcessor(connectionid);
                            sapphire.attachment.Attachment attachment = null;
                            if (currenttempid.length() > 0) {
                                attachment = sapphire.attachment.Attachment.getAttachment(currsdcid, currkeyid1, currkeyid2, currkeyid3);
                                attachment.setTempId(currenttempid);
                                arp.getTempAttachment(attachment, thumbnailGeneration != null ? thumbnailGeneration : Attachment.ThumbnailGeneration.DISABLED);
                            } else if (currattachmentnum > -1) {
                                attachment = sapphire.attachment.Attachment.getAttachment(currsdcid, currkeyid1, currkeyid2, currkeyid3, currattachmentnum);
                                if (currauditsequence >= 0) {
                                    try {
                                        this.checkAttachmentClassAccess(connectionid, currsdcid, currkeyid1, currkeyid2, currkeyid3, currattachmentnum, currauditsequence);
                                    }
                                    catch (SapphireException e) {
                                        this.logError(errorFound, e.getMessage(), e);
                                        continue;
                                    }
                                    attachment.setLockAttachment(lockattachment);
                                    arp.getSDIAttachment(attachment, currauditsequence, thumbnailGeneration != null ? thumbnailGeneration : Attachment.ThumbnailGeneration.DISABLED);
                                } else {
                                    try {
                                        this.checkAttachmentClassAccess(connectionid, currsdcid, currkeyid1, currkeyid2, currkeyid3, currattachmentnum, -1);
                                    }
                                    catch (SapphireException e) {
                                        this.logError(errorFound, e.getMessage(), e);
                                        continue;
                                    }
                                    attachment.setLockAttachment(lockattachment);
                                    arp.getSDIAttachment(attachment, thumbnailGeneration != null ? thumbnailGeneration : Attachment.ThumbnailGeneration.DISABLED);
                                }
                            } else if (currentattachmentclob.length() > 0) {
                                Attachment.AttachmentType attType = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(currentattachmenttype);
                                if (attType == Attachment.AttachmentType.PLAINTEXT || attType == Attachment.AttachmentType.RICHTEXT || attType == Attachment.AttachmentType.URL) {
                                    attachment = sapphire.attachment.Attachment.getAttachment(currsdcid, currkeyid1, currkeyid2, currkeyid3);
                                    attachment.setDescription(currentattachmentdesc);
                                    attachment.setAttachmentType(attType);
                                    attachment.setClob(HttpUtil.decodeURIComponent(currentattachmentclob));
                                    attachment.setFilename("unsaved" + i + (attType == Attachment.AttachmentType.PLAINTEXT ? ".txt" : (attType == Attachment.AttachmentType.RICHTEXT ? ".html" : ".url")));
                                    attachment.setSourceFilename(attachment.getFilename());
                                } else {
                                    attachment = null;
                                    Trace.logError("Direct downloading of attachment data only compatible with Plain Text and Rich Text.");
                                }
                            } else {
                                attachment = sapphire.attachment.Attachment.getAttachment(currsdcid, currkeyid1, currkeyid2, currkeyid3);
                                attachment.setAttachmentClass(currattachmentclass);
                                try {
                                    this.checkAttachmentClassAccess(connectionid, currsdcid, currattachmentclass);
                                }
                                catch (SapphireException e) {
                                    this.logError(errorFound, e.getMessage(), e);
                                    continue;
                                }
                                arp.getSDIAttachment(attachment, thumbnailGeneration != null ? thumbnailGeneration : Attachment.ThumbnailGeneration.DISABLED);
                            }
                            if (attachment != null) {
                                String filename = AttachmentRequest.getFileName(attachment);
                                if (!attachment.isInvalidHash()) {
                                    Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getType());
                                    ZipEntry zipEntry = new ZipEntry(filename);
                                    zipOutputStream.putNextEntry(zipEntry);
                                    FileTransferOptions fileTransferOptions = new FileTransferOptions();
                                    fileTransferOptions.setCloseInputStream(false);
                                    fileTransferOptions.setCloseInputStream(true);
                                    FileTransfer.safeDataTransfer(AttachmentRequest.getInputStream(attachment, true), zipOutputStream, fileTransferOptions);
                                    continue;
                                }
                                this.logError(errorFound, "Hash check on file failed. File is '" + filename + "' corrupted.", null);
                                continue;
                            }
                            Trace.logWarn("Empty attachment found.");
                            continue;
                        }
                        catch (Exception e) {
                            this.logError(errorFound, e.getMessage(), e);
                        }
                    }
                }
                if (errorFound.length() == 0) {
                    long max = FileManager.getDownloadMaxFileSize(connectionid);
                    if (Files.size(downloadZip) < max) {
                        AttachmentRequest.setUpViewInputSteam(request, response, servletContext, null, "download.zip", "", true);
                        FileTransferOptions fileTransferOptions = new FileTransferOptions();
                        fileTransferOptions.setCloseInputStream(true);
                        fileTransferOptions.setCloseOutputStream(false);
                        FileTransfer.safeFileTransfer(downloadZip.toFile(), (OutputStream)response.getOutputStream(), fileTransferOptions);
                        return;
                    } else {
                        response.sendError(400, max == 0L ? "Downloads disabled in system configuration from download max set to zero" : "Size of file is larger than max download size.");
                    }
                    return;
                } else {
                    response.sendError(400, "An error occured while building and downloading the zip file.\n" + errorFound.toString());
                }
                return;
            }
            finally {
                Files.deleteIfExists(downloadZip);
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to download attachment.", e);
        }
    }

    public void add(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        Logger.logWarn("AttachmentRequest - add - Attachment functionality deprecated.");
    }

    public void update(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        Logger.logDebug(this.getClass().getName(), "update called...");
        Logger.logWarn("AttachmentRequest - update - Attachment functionality deprecated.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void manageNormalRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String mode = request.getParameter("mode");
        if (mode == null || mode.length() <= 0) throw new ServletException("No mode provided.");
        Logger.logDebug("mode = " + mode);
        if (mode.equalsIgnoreCase("view")) {
            if (request.getParameter("download") == null || !request.getParameter("download").equalsIgnoreCase("Y")) {
                this.view(request, response, servletContext, false);
                return;
            } else {
                this.download(request, response, servletContext);
            }
            return;
        }
        if (mode.equalsIgnoreCase("download")) {
            this.download(request, response, servletContext);
            return;
        }
        try (PrintWriter out = response.getWriter();){
            out.println("Saving attachment file(s) please wait...<br>");
            out.flush();
            String errorMsg = "";
            String keyid1 = "";
            String keyid2 = "";
            String keyid3 = "";
            int row = -1;
            String connectionId = "";
            try {
                connectionId = ((RequestContext)request.getAttribute("RequestContext")).getConnectionId();
            }
            catch (Exception e3) {
                Logger.logError("Could not obtain connection id.");
                Logger.logStackTrace(e3);
            }
            try {
                String trow = this.getRequestParameter(request, "rownum");
                if (trow.length() > 0) {
                    row = Integer.parseInt(trow);
                }
            }
            catch (NumberFormatException e) {
                Logger.logError("Could not get row number", e);
            }
            String sdcid = this.getRequestParameter(request, "sdcid");
            if (sdcid.length() > 0) {
                keyid1 = this.getRequestParameter(request, "keyid1");
                if (keyid1.length() > 0) {
                    keyid2 = this.getRequestParameter(request, "keyid2");
                    keyid3 = this.getRequestParameter(request, "keyid3");
                    String description = this.getRequestParameter(request, "description");
                    if (description.length() == 0 && (description = this.getRequestParameter(request, "desc")).length() == 0) {
                        description = this.getRequestParameter(request, "attachmentdesc");
                    }
                    String attNum = this.getRequestParameter(request, "attachmentnum");
                    String typeflag = this.getRequestParameter(request, "type");
                    if (typeflag.length() == 0) {
                        typeflag = this.getRequestParameter(request, "typeflag");
                    }
                    typeflag = Attachment.correctTypeFlag(typeflag);
                    HashMap addtionalFields = this.getAddtionalFields(request, null);
                    Attachment attachment = new Attachment();
                    attachment.setSDCId(sdcid);
                    attachment.setKeyId1(keyid1);
                    attachment.setKeyId2(keyid2);
                    attachment.setKeyId3(keyid3);
                    attachment.setType(typeflag);
                    attachment.setDescription(description);
                    attachment.setAdditionalColumns((PropertyList)addtionalFields, connectionId);
                    attachment.setFilename(request.getParameter("filename"));
                    AttachmentProcessor arp = new AttachmentProcessor(connectionId);
                    if (mode.equalsIgnoreCase("edit")) {
                        attachment.setAttachmentNum(Integer.parseInt(attNum));
                        try {
                            arp.editSDIAttachment(attachment);
                        }
                        catch (Exception e) {
                            errorMsg = e.getMessage();
                        }
                    } else if (mode.equalsIgnoreCase("add")) {
                        if (typeflag.length() > 0) {
                            Logger.logDebug("type = " + typeflag);
                            try {
                                arp.addSDIAttachment(attachment);
                            }
                            catch (Exception e) {
                                errorMsg = e.getMessage();
                            }
                        } else {
                            errorMsg = "No type or typeflag provided.";
                            Logger.logError("No type or typeflag provided.");
                        }
                    } else {
                        if (!mode.equalsIgnoreCase("delete")) throw new ServletException("Invalid mode provided.");
                        attachment.setAttachmentNum(Integer.parseInt(attNum));
                        arp.deleteSDIAttachment(attachment);
                    }
                } else {
                    errorMsg = "No keyid1 provided.";
                }
            } else {
                errorMsg = "No sdcid provided.";
            }
            if (errorMsg.length() == 0) {
                out.println("<br>Successful.");
            } else {
                out.println("<br>" + errorMsg);
            }
            out.println("<script type=\"text/javascript\">");
            out.println("if ( window.frameElement != null && typeof( window.parent.attachmentCallback ) != 'undefined' ){");
            out.println("\twindow.parent.attachmentCallback( '" + sdcid + "', '" + keyid1 + "', '" + keyid2 + "', '" + keyid3 + "', " + row + ", '" + errorMsg + "'  );");
            out.println("}");
            out.println("</script>");
            out.flush();
            return;
        }
        catch (IOException e) {
            Logger.logError("Could not get response.", e);
            throw new ServletException(e.getMessage());
        }
    }

    private String tempAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, List fileItems, JSONObject job) throws IOException {
        String errormessage;
        block11: {
            String tempid = "";
            PrintWriter out = response.getWriter();
            String prefix = "file1";
            errormessage = "";
            long maxsize = FileManager.getUploadMaxFileSize(request.getHeader("attachmentpolicynode") != null && request.getHeader("attachmentpolicynode").length() > 0 ? request.getHeader("attachmentpolicynode") : null, connectionId);
            FileItem fifile = FileUpload.getFileItem(fileItems, prefix);
            if (fifile == null) {
                prefix = "file[0]";
                fifile = FileUpload.getFileItem(fileItems, prefix);
                String fileName = FileUpload.getFileName(fifile);
                if (fileName != null) {
                    try {
                        String locpolicynode;
                        String type = fifile.getContentType();
                        String orgFilename = fileName;
                        InputStream is = fifile.getInputStream();
                        if (!FileManager.isValidMagicByte(is, FileType.getFileTypeByFileName(fileName, connectionId), connectionId)) {
                            errormessage = "File content does not match with file extension.";
                            break block11;
                        }
                        FileManager.TempFile tempFile = new FileManager.TempFile(new FileManager.FileData(is, type, true), fileName, orgFilename, FileManager.TempSource.ATTACHMENT, Attachment.AttachmentType.FILE.getFlag(), false, connectionId);
                        FileItem lpn = FileUpload.getFileItem(fileItems, "locationpolicynode");
                        String string = locpolicynode = lpn != null ? lpn.getString() : "";
                        if (locpolicynode.length() == 0) {
                            locpolicynode = "Attachment Custom";
                        }
                        if ((tempid = tempFile.setTempFile(maxsize, locpolicynode, connectionId)).length() > 0) {
                            if (job != null) {
                                try {
                                    job.put("tempid", tempid);
                                    job.put("filename", fileName);
                                    job.put("sourcefilename", orgFilename);
                                    job.put("attachmentdesc", FileUpload.getFileItemValue(fileItems, prefix + "_desc"));
                                    job.put("type", type);
                                }
                                catch (Exception exception) {}
                            }
                            break block11;
                        }
                        errormessage = "Could not upload temp file.";
                        Logger.logError(errormessage);
                    }
                    catch (Exception e) {
                        errormessage = e.getMessage();
                        Logger.logError(errormessage);
                    }
                } else {
                    errormessage = "No filename provided.";
                }
            }
        }
        return errormessage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void manageMultipartRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            List fileItems = this.getFileItems(request, servletContext);
            String mode = "";
            String rs = FileUpload.getFileItemValue(fileItems, "rs");
            if (rs.length() > 0) {
                if (rs.equalsIgnoreCase("U")) {
                    mode = "edit";
                } else if (rs.equalsIgnoreCase("I")) {
                    mode = "add";
                } else if (rs.equalsIgnoreCase("D")) {
                    mode = "delete";
                }
            } else {
                mode = FileUpload.getFileItemValue(fileItems, "mode");
            }
            boolean jsonresponse = FileUpload.getFileItemValue(fileItems, "output").equalsIgnoreCase("json");
            Logger.logDebug("mode = " + mode);
            Logger.logDebug("jsonresponse = " + jsonresponse);
            try {
                PrintWriter out = response.getWriter();
                try {
                    String sdcid;
                    JSONObject job;
                    int row;
                    String keyid3;
                    String keyid2;
                    String keyid1;
                    String errorMsg;
                    block65: {
                        if (!jsonresponse) {
                            out.println("Saving attachment file(s) please wait...<br>");
                            out.flush();
                        }
                        errorMsg = "";
                        String output = "";
                        keyid1 = "";
                        keyid2 = "";
                        keyid3 = "";
                        row = -1;
                        job = new JSONObject();
                        String connectionId = "";
                        try {
                            connectionId = ((RequestContext)request.getAttribute("RequestContext")).getConnectionId();
                        }
                        catch (Exception e3) {
                            Logger.logError("Could not obtain connection id.");
                            Logger.logStackTrace(e3);
                        }
                        try {
                            String trow = FileUpload.getFileItemValue(fileItems, "rownum");
                            if (trow.length() > 0) {
                                row = Integer.parseInt(trow);
                            }
                        }
                        catch (NumberFormatException e) {
                            Logger.logError("Could not get row number", e);
                        }
                        sdcid = FileUpload.getFileItemValue(fileItems, "sdcid");
                        if (sdcid.length() > 0 || mode.equalsIgnoreCase("temp")) {
                            keyid1 = FileUpload.getFileItemValue(fileItems, "keyid1");
                            if (keyid1.length() > 0 || mode.equalsIgnoreCase("temp")) {
                                String typeflag;
                                keyid2 = FileUpload.getFileItemValue(fileItems, "keyid2");
                                keyid3 = FileUpload.getFileItemValue(fileItems, "keyid3");
                                String description = FileUpload.getFileItemValue(fileItems, "description");
                                if (description.length() == 0 && (description = FileUpload.getFileItemValue(fileItems, "desc")).length() == 0) {
                                    description = FileUpload.getFileItemValue(fileItems, "attachmentdesc");
                                }
                                job.put("attachmentdesc", description);
                                String attNum = FileUpload.getFileItemValue(fileItems, "attachmentnum");
                                job.put("attachmentnum", attNum);
                                String rownum = FileUpload.getFileItemValue(fileItems, "rownum");
                                if (rownum != null && rownum.length() > 0) {
                                    job.put("rownum", rownum);
                                }
                                if ((typeflag = FileUpload.getFileItemValue(fileItems, "type")).length() == 0) {
                                    typeflag = FileUpload.getFileItemValue(fileItems, "typeflag");
                                }
                                typeflag = Attachment.correctTypeFlag(typeflag);
                                job.put("typeflag", typeflag);
                                Logger.logDebug("type = " + typeflag);
                                PropertyList addtionalFields = (PropertyList)this.getAddtionalFields(request, fileItems);
                                Attachment attachment = new Attachment();
                                attachment.setSDCId(sdcid);
                                attachment.setKeyId1(keyid1);
                                attachment.setKeyId2(keyid2);
                                attachment.setKeyId3(keyid3);
                                attachment.setDescription(description);
                                attachment.setType(typeflag);
                                attachment.setAdditionalColumns(addtionalFields, connectionId);
                                AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionId);
                                if (mode.equalsIgnoreCase("edit") || mode.equalsIgnoreCase("add")) {
                                    String r = FileUpload.getFileItemValue(fileItems, "reference");
                                    if (r != null && r.length() > 0) {
                                        attachment.setFilename(r);
                                    } else {
                                        r = FileUpload.getFileItemValue(fileItems, "plaintext");
                                        if (r != null && r.length() > 0) {
                                            attachment.setClob(r);
                                        } else {
                                            r = FileUpload.getFileItemValue(fileItems, "formattedtext");
                                            if (r != null && r.length() > 0) {
                                                attachment.setClob(r);
                                            } else {
                                                r = FileUpload.getFileItemValue(fileItems, "url");
                                                if (r != null && r.length() > 0) {
                                                    attachment.setUrl(r);
                                                } else {
                                                    FileItem fileItem = FileUpload.getFile(fileItems);
                                                    if (fileItem != null) {
                                                        attachment.setFilename(fileItem.getName());
                                                        attachment.setSourceFilename(attachment.getFilename());
                                                        if (fileItem.getSize() > 0L) {
                                                            attachment.setInputStream(fileItem.getInputStream());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (mode.equalsIgnoreCase("edit")) {
                                    attachment.setAttachmentNum(Integer.parseInt(attNum));
                                    try {
                                        if (!FileManager.isValidMagicByte(attachment, connectionId)) {
                                            errorMsg = "File content does not match with file extension.";
                                            break block65;
                                        }
                                        attachmentProcessor.editSDIAttachment(attachment);
                                    }
                                    catch (Exception e) {
                                        errorMsg = e.getMessage();
                                    }
                                } else if (mode.equalsIgnoreCase("add")) {
                                    try {
                                        if (!FileManager.isValidMagicByte(attachment, connectionId)) {
                                            errorMsg = "File content does not match with file extension.";
                                            break block65;
                                        }
                                        attachmentProcessor.addSDIAttachment(attachment);
                                    }
                                    catch (Exception e) {
                                        errorMsg = e.getMessage();
                                    }
                                } else if (mode.equalsIgnoreCase("delete")) {
                                    attachment.setAttachmentNum(Integer.parseInt(attNum));
                                    try {
                                        attachmentProcessor.deleteSDIAttachment(attachment);
                                    }
                                    catch (Exception e) {
                                        errorMsg = e.getMessage();
                                    }
                                } else {
                                    if (!mode.equalsIgnoreCase("temp")) throw new ServletException("Invalid mode provided.");
                                    try {
                                        errorMsg = this.tempAttachmentMultiPart(request, response, sdcid, keyid1, keyid2, keyid3, connectionId, fileItems, job);
                                        if (attNum.length() > 0) {
                                            job.put("attachmentnum", attNum);
                                        }
                                    }
                                    catch (Exception e) {
                                        errorMsg = "Unable to create temporary file with error: " + e.getMessage();
                                    }
                                }
                            } else {
                                errorMsg = "No keyid1 provided.";
                            }
                        } else {
                            errorMsg = "No sdcid provided.";
                        }
                    }
                    if (jsonresponse) {
                        if (errorMsg.length() > 0) {
                            out.print(errorMsg);
                            response.setStatus(406);
                        } else {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            job.put("error", errorMsg);
                            job.put("keyid1", keyid1);
                            job.put("keyid2", keyid2);
                            job.put("keyid3", keyid3);
                            job.put("sdcid", sdcid);
                            out.print(job.toString());
                        }
                    } else {
                        if (errorMsg.length() == 0) {
                            out.println("<br>Successful.");
                        } else {
                            out.println("<br>" + errorMsg);
                        }
                        out.println("<script type=\"text/javascript\">");
                        out.println("if ( window.frameElement != null && typeof( window.parent.attachmentCallback ) != 'undefined' ){");
                        out.println("\twindow.parent.attachmentCallback( '" + sdcid + "', '" + keyid1 + "', '" + keyid2 + "', '" + keyid3 + "', " + row + ", '" + errorMsg.replaceAll("'", "\\\\'") + "'  );");
                        out.println("}");
                        out.println("</script>");
                    }
                    out.flush();
                }
                finally {
                    out.close();
                    for (Object obj : fileItems) {
                        FileItem item = (FileItem)obj;
                        item.delete();
                    }
                    return;
                }
            }
            catch (IOException e) {
                Logger.logError("Could not get response.", e);
                throw new ServletException(e.getMessage(), (Throwable)e);
            }
        }
        catch (FileUploadException eu) {
            Logger.logError("Could not upload file.", eu);
            if (eu.getMessage().contains("size exceeds allowed range")) {
                response.setStatus(413);
                return;
            } else {
                response.setStatus(400);
            }
            return;
        }
        catch (Exception e) {
            Logger.logError("Could not parse request.", e);
            throw new ServletException("Could not parse request.", (Throwable)e);
        }
    }

    public void manage(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        Logger.logDebug(this.getClass().getName(), "manage called...");
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = "";
        if (requestContext != null) {
            connectionid = requestContext.getConnectionId();
        }
        Logger.logDebug("ATTACHMENT", "manage attachment connection: " + connectionid);
        if (connectionid != null && connectionid.length() > 0) {
            String contentType = request.getHeader("Content-type");
            if (contentType != null && contentType.startsWith("multipart/")) {
                this.manageMultipartRequest(request, response, servletContext);
            } else {
                this.manageNormalRequest(request, response, servletContext);
            }
        } else {
            throw new LoginException(request.getQueryString(), "In order to access this resource, you must login.");
        }
    }

    private void validate(HttpServletRequest request, HttpServletResponse response) {
        Logger.logDebug(this.getClass().getName(), "validate called...");
        try {
            PrintWriter output = response.getWriter();
            String filelist = request.getParameter("filelist");
            StringBuffer existFilelist = new StringBuffer();
            if (filelist != null && filelist.length() > 0) {
                String[] files = sapphire.util.StringUtil.split(filelist, ";");
                for (int i = 0; i < files.length; ++i) {
                    File file = new File(files[i]);
                    if (!file.exists()) continue;
                    existFilelist.append(";").append(files[i]);
                }
                filelist = existFilelist.length() > 0 ? existFilelist.substring(1) : "";
            } else {
                filelist = "";
            }
            output.println("<script>parent." + request.getParameter("fileexistcallback") + "( '" + filelist.replaceAll("\\\\", "\\\\\\\\") + "' );</script>");
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    private void addUpdateBinary(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Logger.logDebug(this.getClass().getName(), "addUpdateBinary called...");
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            PrintWriter output = response.getWriter();
            output.println("Uploading attachment file please wait...<br>");
            String webapp = request.getContextPath();
            if (webapp.startsWith("/")) {
                webapp = webapp.substring(1);
            }
            String connectionid = requestContext != null ? requestContext.getConnectionId() : "";
            String url = "http://" + request.getServerName() + ":" + request.getServerPort() + "/" + webapp + "/rc?command=updateattachment&connectionid=" + HttpUtil.encodeURIComponent(connectionid);
            Logger.logDebug("URL = " + url);
            HashMap<String, String> data = new HashMap<String, String>();
            String sdcid = request.getParameter("sdcid");
            String keyid1 = request.getParameter("keyid1");
            String keyid2 = request.getParameter("keyid2");
            String keyid3 = request.getParameter("keyid3");
            String attnum = request.getParameter("file1_attachmentnum");
            String attdesc = request.getParameter("file1_desc");
            if (attdesc == null || attdesc.length() == 0) {
                attdesc = request.getParameter("file1_description");
            }
            String attupload = request.getParameter("file1_uploadto");
            String attmaxsize = request.getParameter("file1_maxsize");
            String nexturl = request.getParameter("__nexturl");
            String upoptions = request.getParameter("uploadoptions");
            data.put("sdcid", sdcid);
            data.put("keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                data.put("keyid2", keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0) {
                data.put("keyid3", keyid3);
            }
            if (attnum != null && attnum.length() > 0) {
                data.put("file1_attachmentnum", attnum);
            }
            if (attdesc != null && attdesc.length() > 0) {
                data.put("file1_desc", attdesc);
            }
            if (attupload != null && attupload.length() > 0) {
                data.put("file1_uploadto", attupload);
            }
            if (attmaxsize != null && attmaxsize.length() > 0) {
                data.put("file1_maxsize", attmaxsize);
            }
            if (nexturl != null && nexturl.length() > 0) {
                data.put("__nexturl", nexturl);
            }
            if (upoptions != null && upoptions.length() > 0) {
                data.put("uploadoptions", upoptions);
            }
            try {
                String theResponse;
                data.put("file1", (String)request.getInputStream());
                HttpSender httpSender = new HttpSender();
                try {
                    theResponse = httpSender.connectAndSendMultipart(url, data);
                }
                catch (Exception e1) {
                    Logger.logInfo("Failed to send multipart request, try alternative address...");
                    url = "http://" + request.getLocalAddr() + ":" + request.getServerPort() + "/" + webapp + "/rc?command=updateattachment&connectionid=" + HttpUtil.encodeURIComponent(connectionid);
                    Logger.logDebug("URL = " + url);
                    try {
                        theResponse = httpSender.connectAndSendMultipart(url, data);
                    }
                    catch (Exception e2) {
                        throw new ServletException("Could not send multipart request.", (Throwable)e2);
                    }
                }
                if (theResponse.indexOf("Error: ") > -1 || theResponse.indexOf("error message: ") > -1) {
                    throw new ServletException("Errors occured during upload.");
                }
                Logger.logDebug(this.getClass().getName(), "Request forwarding and upload successful.");
                output.print(theResponse);
            }
            catch (Exception e) {
                throw new ServletException("Could not add file to multipart.");
            }
        }
        catch (Exception e) {
            throw new ServletException("Unable to handle request.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addUpdate(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, int mode) {
        Logger.logDebug(this.getClass().getName(), "addUpdate called...");
        try (PrintWriter output = response.getWriter();){
            output.println("Uploading attachment file(s) please wait...");
            output.flush();
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String connectionid = "";
            if (requestContext != null) {
                connectionid = requestContext.getConnectionId();
            }
            try {
                List fileItems = this.getFileItems(request, servletContext);
                try {
                    AttachmentType_File.addUpdate(output, fileItems, connectionid, mode, "", null, request);
                }
                catch (Exception fe) {
                    output.println("Failed to save attachments.");
                    Logger.logStackTrace(fe);
                }
            }
            catch (FileUploadException fe) {
                output.println("Failed to parse attachment request.");
                Logger.logStackTrace(fe);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    private void checkAttachmentClassAccess(String connectionId, String sdcId, String keyId1, String keyId2, String keyId3, int attachmentNum, int auditSeq) throws SapphireException {
        if ("LV_Issue".equals(sdcId)) {
            QueryProcessor qp = new QueryProcessor(connectionId);
            DataSet attClass = auditSeq > -1 ? qp.getPreparedSqlDataSet("SELECT attachmentclass FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ? AND auditsequence = ?", new Object[]{sdcId, keyId1, keyId2, keyId3, new Integer(attachmentNum), new Integer(auditSeq)}) : qp.getPreparedSqlDataSet("SELECT attachmentclass FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{sdcId, keyId1, keyId2, keyId3, new Integer(attachmentNum)});
            if (attClass != null && attClass.getRowCount() > 0) {
                this.checkAttachmentClassAccess(connectionId, sdcId, attClass.getString(0, "attachmentclass", ""));
            }
        }
    }

    private void checkAttachmentClassAccess(String connectionId, String sdcId, String attachmentClass) throws SapphireException {
        String roleList;
        if ("LV_Issue".equals(sdcId) && attachmentClass != null && "Issue Log Snapshot".equals(attachmentClass) && (roleList = ";" + new ConnectionProcessor(connectionId).getSapphireConnection().getRoleList() + ";").indexOf(";Administrator;") == -1 && roleList.indexOf(";IssueSubmitter;") == -1) {
            throw new SapphireException("Un-Authorized Access", "FAILURE", "You do not have required privileges to view/download this attachment.");
        }
    }

    private boolean hasViewAttachmentAcess(HttpServletRequest request, String connectionId) {
        boolean viewAttachmentAccess = true;
        String sdcid = request.getParameter("sdcid");
        String keyid1 = request.getParameter("keyid1");
        String keyid2 = request.getParameter("keyid2");
        String keyid3 = request.getParameter("keyid3");
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(new SDCProcessor(connectionId).getProperty(sdcid, "accesscontrolledflag"));
        try {
            if (deptSecurityEnabled) {
                DAMProcessor damProcessor = new DAMProcessor(connectionId);
                SDIList sdiList = damProcessor.checkSDIAccess(sdcid, keyid1, keyid2, keyid3, true, "list");
                DataSet listAccess = sdiList.toDataSet();
                if (listAccess.size() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "select operationid from sdcoperation where sdcid = " + safeSQL.addVar(sdcid) + " and operationid in ('ManageAttachment','ViewAttachment')";
                    DataSet ds = new QueryProcessor(connectionId).getPreparedSqlDataSet(sql, safeSQL.getValues());
                    if (ds != null && ds.getRowCount() > 0) {
                        DataSet viewAccess;
                        viewAttachmentAccess = false;
                        sdiList = damProcessor.checkSDIAccess(sdcid, keyid1, keyid2, keyid3, true, "ManageAttachment");
                        DataSet manageAccess = sdiList.toDataSet();
                        viewAttachmentAccess = manageAccess.size() > 0 ? true : (viewAccess = (sdiList = damProcessor.checkSDIAccess(sdcid, keyid1, keyid2, keyid3, true, "ViewAttachment")).toDataSet()).size() > 0;
                    }
                } else {
                    viewAttachmentAccess = false;
                }
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return viewAttachmentAccess;
    }
}

