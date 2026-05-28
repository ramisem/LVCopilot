/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.pageelements.attachment.AttachmentType_File;
import com.labvantage.sapphire.pageelements.attachment.Files;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FilesAjaxHandler
extends BaseAjaxRequest
implements ELNConstants {
    protected static final String SESSION_NUMBER = "filesajaxhandler_attachmentnum";

    private int storeAsAttachment(String filedata, FileTypeGroup fileType, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String filename, String locationpolicynode, String locationpolicyitem, boolean byRef) throws Exception {
        int out = FileManager.storeAsAttachment(filedata, fileType, sdcid, keyid1, keyid2, keyid3, filename, attachmentNum > -1 ? "" + attachmentNum : "", byRef, locationpolicynode, locationpolicyitem, null, this.getConnectionId());
        return out;
    }

    private int getNextAttachmentNum(HttpServletRequest request, String sdcid, String keyid1, String keyid2, String keyid3) {
        Object[] params;
        int out = 1;
        int cache_attachmentNum = 0;
        int found_attachmentNum = 0;
        String key = sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3;
        HashMap<String, Integer> o = request.getSession().getAttribute(SESSION_NUMBER);
        if (o != null && o instanceof HashMap && ((HashMap)o).containsKey(key)) {
            cache_attachmentNum = (Integer)((HashMap)o).get(key);
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT attachmentnum FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? ");
        if (keyid2.length() > 0) {
            sql.append("AND keyid2 = ? ");
            if (keyid3.length() > 0) {
                sql.append("AND keyid3 = ? ");
                params = new Object[]{sdcid, keyid1, keyid2, keyid3};
            } else {
                params = new Object[]{sdcid, keyid1, keyid2};
            }
        } else {
            params = new Object[]{sdcid, keyid1};
        }
        sql.append("ORDER BY attachmentnum DESC ");
        DataSet attnumds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params, true);
        if (attnumds != null && attnumds.size() > 0) {
            found_attachmentNum = attnumds.getInt(0, "attachmentnum", 0);
        }
        out = found_attachmentNum > cache_attachmentNum ? found_attachmentNum + 1 : cache_attachmentNum + 1;
        if (o == null) {
            o = new HashMap<String, Integer>();
        }
        if (((HashMap)o).containsKey(key)) {
            o.remove(key);
        }
        o.put(key, new Integer(out));
        request.getSession().setAttribute(SESSION_NUMBER, o);
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block156: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                Mode mode = Mode.REFRESH;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", Mode.REFRESH.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (mode == Mode.REFRESH) {
                    String data;
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    boolean viewonly = ajaxresponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                    PropertyList properties = null;
                    String props2 = ajaxresponse.getRequestParameter("properties");
                    if (props2.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props2));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    DataSet attachmentdata = null;
                    if (!ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (data = ajaxresponse.getRequestParameter("data")).length() > 0) {
                        try {
                            attachmentdata = new DataSet(new JSONObject(data));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachments");
                    Files files = new Files(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    files.setElementid(elementid);
                    files.setElementProperties(properties);
                    files.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    if (attachmentdata != null) {
                        files.setAttachmentData(attachmentdata);
                    }
                    if (viewonly) {
                        files.setViewOnly(viewonly);
                    }
                    files.getElementProperties().setProperty("view", ajaxresponse.getRequestParameter("viewmode"));
                    request.getSession().setAttribute(SESSION_NUMBER, new HashMap());
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", files.getHtml());
                    ajaxresponse.addCallbackArgument("script", files.getScript());
                    if (ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (attachmentdata = files.getAttachmentData()) != null) {
                        ajaxresponse.addCallbackArgument("data", attachmentdata.toJSONString(true, true));
                    }
                    break block156;
                }
                if (mode == Mode.CREATE) {
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    PropertyList properties = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception props2) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachments");
                    Files files = new Files(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    files.setAjaxCreate(true);
                    files.setElementid(elementid);
                    files.setElementProperties(properties);
                    files.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    request.getSession().setAttribute(SESSION_NUMBER, new HashMap());
                    JSONObject fileElement = files.getFileElement();
                    String script = files.getScript();
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", files.getHtml());
                    ajaxresponse.addCallbackArgument("script", script);
                    ajaxresponse.addCallbackArgument("fileElement", fileElement.toString());
                    ajaxresponse.addCallbackArgument("properties", files.getElementProperties().toJSONString());
                    DataSet attachmentdata = files.getAttachmentData();
                    if (attachmentdata != null) {
                        ajaxresponse.addCallbackArgument("data", attachmentdata.toJSONString(true, true));
                    }
                    break block156;
                }
                if (mode == Mode.VALIDATE) {
                    PropertyList properties = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception keyid3) {
                            // empty catch block
                        }
                    }
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    int attachmentnum = 1;
                    try {
                        attachmentnum = Integer.parseInt(ajaxresponse.getRequestParameter("attachmentnum", "1"));
                    }
                    catch (Exception e) {
                        attachmentnum = 1;
                    }
                    int auditsequence = 1;
                    try {
                        auditsequence = Integer.parseInt(ajaxresponse.getRequestParameter("auditsequence", "1"));
                    }
                    catch (Exception e) {
                        auditsequence = 1;
                    }
                    String filename = ajaxresponse.getRequestParameter("filename");
                    String message = "";
                    String confirm = "";
                    String filelocationpolicynode = ajaxresponse.getRequestParameter("locationpolicynode");
                    if (FileManager.isValidFileType(filename, properties)) {
                        boolean byref = ajaxresponse.getRequestParameter("byref").equalsIgnoreCase("Y");
                        if (byref) {
                            String attachmentpolicynode = ajaxresponse.getRequestParameter("attachmentpolicynode");
                            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                            PropertyList attpolicy = null;
                            try {
                                attpolicy = cp.getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            filename = AttachmentType_File.renameFileFromPolicy(filename, keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)"), keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)"), attpolicy);
                            String filelocationpolicyitem = ajaxresponse.getRequestParameter("locationpolicyitem");
                            String uploadtopath = StringUtil.replaceAll(FileManager.getFileLocation(filelocationpolicynode, filelocationpolicyitem, this.getConnectionId()), "\\", "/");
                            uploadtopath = uploadtopath + (uploadtopath.endsWith("/") ? "" : "/") + filename;
                            uploadtopath = Attachment.evaluateFileNameExpressions(sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), uploadtopath, null);
                            this.logDebug("Validate Filepath: " + uploadtopath);
                            File file = new File(uploadtopath);
                            if (file.isDirectory()) {
                                message = this.getTranslationProcessor().translate("Upload location is invalid.");
                            } else if (file.exists()) {
                                boolean systemrename;
                                boolean bl = systemrename = attpolicy != null && attpolicy.getPropertyList("filereference") != null && attpolicy.getPropertyList("filereference").getPropertyList("renameonupload") != null ? attpolicy.getPropertyList("filereference").getPropertyList("renameonupload").getProperty("rename").equalsIgnoreCase("S") : false;
                                if (systemrename) {
                                    if (!file.canWrite()) {
                                        message = this.getTranslationProcessor().translate("File at system location cannot be written to.");
                                        this.logWarn("File can not be written to at system location: " + file.getAbsolutePath());
                                    }
                                } else if (!file.canWrite()) {
                                    message = this.getTranslationProcessor().translate("File at upload location cannot be written to.");
                                    this.logWarn("File can not be written to at upload location: " + file.getAbsolutePath());
                                } else {
                                    message = this.getTranslationProcessor().translate("File already exists at upload location.");
                                    confirm = "Overwrite?";
                                    this.logWarn("File exists at upload location: " + file.getAbsolutePath());
                                }
                            }
                        }
                    } else {
                        message = this.getTranslationProcessor().translate("Invalid file type selected.");
                    }
                    ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                    ajaxresponse.addCallbackArgument("filename", filename);
                    ajaxresponse.addCallbackArgument("message", message);
                    ajaxresponse.addCallbackArgument("confirm", confirm);
                    ajaxresponse.addCallbackArgument("doneid", ajaxresponse.getRequestParameter("doneid", "file"));
                    break block156;
                }
                if (mode == Mode.CLEANUP) {
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    PropertyList properties = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception attachmentnum) {
                            // empty catch block
                        }
                    }
                    DataSet attachmentdata = null;
                    String data = ajaxresponse.getRequestParameter("data");
                    if (data.length() > 0) {
                        try {
                            attachmentdata = new DataSet(new JSONObject(data));
                        }
                        catch (Exception filename) {
                            // empty catch block
                        }
                    }
                    StringBuffer temp = new StringBuffer();
                    for (int i = 0; i < attachmentdata.getRowCount(); ++i) {
                        String tempid = attachmentdata.getValue(i, "__tempid", "");
                        if (tempid.length() <= 0) continue;
                        if (temp.length() > 0) {
                            temp.append(";");
                        }
                        temp.append(tempid);
                    }
                    if (temp.length() > 0) {
                        try {
                            FileManager.TempFile.removeTempFile(temp.toString(), this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionId());
                        }
                        catch (Exception e) {
                            this.logError("Failed to remove temp file.", e);
                        }
                    }
                    break block156;
                }
                if (mode == Mode.UPLOAD || mode == Mode.UPLOADTEMP || mode == Mode.BROWSEFILE) {
                    String filedata = ajaxresponse.getRequestParameter("filedata", "");
                    if (filedata.length() > 0) {
                        String filename = ajaxresponse.getRequestParameter("filename");
                        String filetype = ajaxresponse.getRequestParameter("filetype");
                        String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                        String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                        String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                        String sdcid = ajaxresponse.getRequestParameter("sdcid");
                        String locationpolicynode = ajaxresponse.getRequestParameter("locationpolicynode");
                        String locationpolicyitem = ajaxresponse.getRequestParameter("locationpolicyitem");
                        boolean byref = ajaxresponse.getRequestParameter("byref", "Y").equalsIgnoreCase("Y");
                        if (filedata.startsWith("data:") || filedata.startsWith("file:")) {
                            FileType fileType = FileType.getFileType(filename, this.getConnectionId());
                            if (!fileType.getName().equals("UNKNOWN")) {
                                try {
                                    FileManager.FileData fileData;
                                    FileTypeGroup type = filedata.startsWith("file:") && filetype.length() == 0 ? FileTypeGroup.getFileTypeGroupByFileName(filename) : FileTypeGroup.getFileTypeGroupByType(filetype, this.getConnectionId());
                                    if (mode == Mode.UPLOAD) {
                                        int attachment = -1;
                                        if ((attachment = this.storeAsAttachment(filedata, type, sdcid, keyid1, keyid2, keyid3, attachment, filename, locationpolicynode, locationpolicyitem, byref)) > 0) {
                                            ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                                            ajaxresponse.addCallbackArgument("attachment", attachment);
                                        } else {
                                            ajaxresponse.setError("Could not upload attachment.");
                                        }
                                        break block156;
                                    }
                                    FileManager.TempFile tempFile = null;
                                    String orgtempid = null;
                                    FileManager.TempFile orgTemp = null;
                                    if (ajaxresponse.getRequestParameter("imagemarkup").equalsIgnoreCase("Y")) {
                                        FileManager.FileData orginalData;
                                        block157: {
                                            fileData = new FileManager.FileData(filedata);
                                            orginalData = null;
                                            if (ajaxresponse.getRequestParameter("originalattachmentnum").length() > 0) {
                                                try {
                                                    orgTemp = FileManager.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(ajaxresponse.getRequestParameter("originalattachmentnum")), this.getConnectionId());
                                                    if (orgTemp != null) {
                                                        orginalData = orgTemp.getOrgData() != null && orgTemp.getOrgData().getSize() > 0L ? orgTemp.getOrgData() : orgTemp.getData();
                                                        break block157;
                                                    }
                                                    this.logger.error("Failed to obtain orginal attachment data.");
                                                }
                                                catch (Exception e) {
                                                    this.logger.error("Failed to obtain orginal attachment. Error: " + e.getMessage());
                                                }
                                            } else if (ajaxresponse.getRequestParameter("originaltempid").length() > 0) {
                                                try {
                                                    orgTemp = FileManager.TempFile.getTempFile(ajaxresponse.getRequestParameter("originaltempid"), false, this.getQueryProcessor(), this.getConnectionId());
                                                    if (orgTemp != null) {
                                                        orgtempid = ajaxresponse.getRequestParameter("originaltempid");
                                                        orginalData = orgTemp.getOrgData() != null && orgTemp.getOrgData().getSize() > 0L ? orgTemp.getOrgData() : orgTemp.getData();
                                                        break block157;
                                                    }
                                                    this.logger.error("Failed to obtain orginal attachment temp record.");
                                                }
                                                catch (Exception e) {
                                                    this.logger.error("Failed to obtain orginal attachment temp. Error: " + e.getMessage());
                                                }
                                            } else if (ajaxresponse.getRequestParameter("imagemarkupcontent").length() > 0) {
                                                try (ByteArrayInputStream bin = new ByteArrayInputStream(fileData.getData());){
                                                    BufferedImage im = ImageIO.read(bin);
                                                    im.getWidth();
                                                    im.getHeight();
                                                    BufferedImage org = new BufferedImage(im.getWidth(), im.getHeight(), im.getType());
                                                    Graphics2D graphics = org.createGraphics();
                                                    graphics.setPaint(Color.WHITE);
                                                    graphics.fillRect(0, 0, im.getWidth(), im.getHeight());
                                                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                                                        ImageIO.write((RenderedImage)org, "png", bos);
                                                        orginalData = new FileManager.FileData(bos.toByteArray(), FileType.getFileTypeByName("PNG", this.getConnectionId()).getMime());
                                                    }
                                                }
                                                catch (Exception e1) {
                                                    this.logError(e1.getMessage());
                                                }
                                            }
                                        }
                                        if (orginalData != null) {
                                            tempFile = new FileManager.TempFile(fileData, filename, FileManager.TempSource.ATTACHMENT, true, this.getConnectionId());
                                            tempFile.setOrgData(orginalData);
                                            if (ajaxresponse.getRequestParameter("imagemarkupcontent").length() > 0) {
                                                try {
                                                    JSONObject data;
                                                    JSONObject markupOb = new JSONObject(ajaxresponse.getRequestParameter("imagemarkupcontent"));
                                                    if (markupOb.has("backgroundShapes") && markupOb.getJSONArray("backgroundShapes").length() > 0 && markupOb.getJSONArray("backgroundShapes").getJSONObject(0).has("data") && (data = markupOb.getJSONArray("backgroundShapes").getJSONObject(0).getJSONObject("data")).has("imageSrc") && data.getString("imageSrc").length() > 0) {
                                                        JSONObject oldData;
                                                        JSONObject orgMarkup;
                                                        String m = "";
                                                        if (orgTemp != null && (orgMarkup = orgTemp.getAttribute("markup", new JSONObject())) != null && orgMarkup.has("backgroundShapes") && orgMarkup.getJSONArray("backgroundShapes").length() > 0 && orgMarkup.getJSONArray("backgroundShapes").getJSONObject(0).has("data") && (oldData = orgMarkup.getJSONArray("backgroundShapes").getJSONObject(0).getJSONObject("data")).has("imageSrc") && oldData.getString("imageSrc").length() > 0) {
                                                            m = oldData.getString("imageSrc");
                                                        }
                                                        FileManager.FileData markupImage = null;
                                                        markupImage = m.length() > 0 ? new FileManager.FileData(m, FileType.getFileTypeByName("PNG", this.getConnectionId()).getMime()) : (orgTemp != null ? (orgTemp.getData() != null ? orgTemp.getData() : orgTemp.getOrgData()) : orginalData);
                                                        data.remove("imageSrc");
                                                        data.put("imageSrc", markupImage.getDataURL());
                                                    }
                                                    tempFile.setAttribute("markup", markupOb);
                                                }
                                                catch (Exception e) {
                                                    this.logger.error("Failed to update markup.");
                                                }
                                            }
                                        } else {
                                            this.logger.error("Failed to create markup image");
                                        }
                                    } else if (filedata.startsWith("file:")) {
                                        tempFile = new FileManager.TempFile(filename, FileType.getFileType(filename, this.getConnectionId()), filename, FileManager.TempSource.ATTACHMENT, Attachment.AttachmentType.FILE.getFlag(), true, this.getConnectionId());
                                    } else {
                                        fileData = new FileManager.FileData(filedata);
                                        tempFile = new FileManager.TempFile(fileData, filename, filename, FileManager.TempSource.ATTACHMENT, Attachment.AttachmentType.FILE.getFlag(), true, this.getConnectionId());
                                    }
                                    String tempid = null;
                                    try {
                                        tempid = tempFile != null ? tempFile.setTempFile(-1L, locationpolicynode, this.getConnectionId()) : "";
                                    }
                                    catch (Exception e) {
                                        this.logger.error("Failed to set back temp record.");
                                    }
                                    if (tempid != null && tempid.length() > 0) {
                                        if (orgtempid != null && orgtempid.length() > 0) {
                                            try {
                                                FileManager.TempFile.removeTempFile(orgtempid, this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionId());
                                            }
                                            catch (Exception e) {
                                                this.logger.warn("Failed to remove old temp file.");
                                            }
                                        }
                                        ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                                        ajaxresponse.addCallbackArgument("tempid", tempid);
                                        ajaxresponse.addCallbackArgument("filename", filename);
                                        ajaxresponse.addCallbackArgument("typeflag", tempFile != null ? (mode == Mode.BROWSEFILE ? "R" : tempFile.getTypeFlag()) : "");
                                        if (ajaxresponse.getRequestParameter("imagemarkup").equalsIgnoreCase("Y")) {
                                            ajaxresponse.addCallbackArgument("markup", tempFile != null && tempFile.hasAttribute("markup") ? tempFile.getAttribute("markup", new JSONObject()).toString() : "");
                                        }
                                        break block156;
                                    }
                                    ajaxresponse.setError("Could not upload file.");
                                }
                                catch (Exception e) {
                                    ajaxresponse.setError("Failed to upload file. " + e.getMessage(), e);
                                }
                                break block156;
                            }
                            ajaxresponse.setError("Invalid file type provided.");
                            break block156;
                        }
                        ajaxresponse.setError("No data URL provided.");
                        break block156;
                    }
                    ajaxresponse.setError("No File data provided.");
                    break block156;
                }
                if (mode == Mode.AZURE) {
                    String filename = ajaxresponse.getRequestParameter("filename");
                    String versionId = ajaxresponse.getRequestParameter("versionid");
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String accountName = ajaxresponse.getRequestParameter("accountName");
                    String accountKey = ajaxresponse.getRequestParameter("accountKey");
                    String container = ajaxresponse.getRequestParameter("container");
                    String folderPath = ajaxresponse.getRequestParameter("folderPath");
                    PropertyList props = new PropertyList();
                    props.setProperty("accountName", accountName);
                    props.setProperty("accountKey", accountKey);
                    props.setProperty("container", container);
                    props.setProperty("folderPath", folderPath);
                    props.setProperty("filename", filename);
                    props.setProperty("versionid", versionId);
                    InputStream inputStream = null;
                    try {
                        inputStream = BaseAttachmentRepository.getDirectInputStream("AzureFileRepository", "Sapphire Custom", props, this.getConnectionProcessor().getSapphireConnection());
                    }
                    catch (Exception e) {
                        throw new ServletException("Failed to obtain azure information", (Throwable)e);
                    }
                    if (inputStream == null) {
                        throw new ServletException("Failed to obtain azure file stream");
                    }
                    FileManager.FileData fileData = new FileManager.FileData(inputStream, FileType.getFileType(filename, this.getConnectionId()).getMime(), false);
                    FileManager.TempFile tempFile = new FileManager.TempFile(fileData, filename, filename, FileManager.TempSource.ATTACHMENT, Attachment.AttachmentType.FILE.getFlag(), true, this.getConnectionId());
                    String tempid = null;
                    try {
                        tempid = tempFile != null ? tempFile.setTempFile(-1L, "", this.getConnectionId()) : "";
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to set back temp record.");
                    }
                    ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                    ajaxresponse.addCallbackArgument("tempid", tempid);
                    ajaxresponse.addCallbackArgument("filename", filename);
                    ajaxresponse.addCallbackArgument("typeflag", tempFile != null ? tempFile.getTypeFlag() : "");
                    if (versionId != null && versionId.trim().length() > 0) {
                        ajaxresponse.addCallbackArgument("sourcefilename", filename + "_exists#" + versionId);
                    } else {
                        ajaxresponse.addCallbackArgument("sourcefilename", filename + "_exists");
                    }
                    break block156;
                }
                if (mode == Mode.AWS) {
                    String filename = ajaxresponse.getRequestParameter("filename");
                    String versionId = ajaxresponse.getRequestParameter("versionid");
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String accessKey = ajaxresponse.getRequestParameter("accesskey");
                    String secretAccessKey = ajaxresponse.getRequestParameter("secretaccesskey");
                    String region = ajaxresponse.getRequestParameter("region");
                    String bucketName = ajaxresponse.getRequestParameter("bucketname");
                    String folderPath = ajaxresponse.getRequestParameter("folderPath");
                    PropertyList props = new PropertyList();
                    props.setProperty("bucketname", bucketName);
                    props.setProperty("accesskey", accessKey);
                    props.setProperty("secretaccesskey", secretAccessKey);
                    props.setProperty("region", region);
                    props.setProperty("filename", filename);
                    props.setProperty("versionid", versionId);
                    props.setProperty("folderPath", folderPath);
                    InputStream inputStream = null;
                    try {
                        inputStream = BaseAttachmentRepository.getDirectInputStream("AWSFileRepository", "Sapphire Custom", props, this.getConnectionProcessor().getSapphireConnection());
                    }
                    catch (Exception e) {
                        throw new ServletException("Failed to obtain aws information", (Throwable)e);
                    }
                    if (inputStream == null) {
                        throw new ServletException("Failed to obtain aws file stream");
                    }
                    FileManager.FileData fileData = new FileManager.FileData(inputStream, FileType.getFileType(filename, this.getConnectionId()).getMime(), false);
                    FileManager.TempFile tempFile = new FileManager.TempFile(fileData, filename, filename, FileManager.TempSource.ATTACHMENT, Attachment.AttachmentType.FILE.getFlag(), true, this.getConnectionId());
                    String tempid = null;
                    try {
                        tempid = tempFile != null ? tempFile.setTempFile(-1L, "", this.getConnectionId()) : "";
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to set back temp record.");
                    }
                    ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                    ajaxresponse.addCallbackArgument("tempid", tempid);
                    ajaxresponse.addCallbackArgument("filename", filename);
                    ajaxresponse.addCallbackArgument("typeflag", tempFile != null ? tempFile.getTypeFlag() : "");
                    if (versionId != null && versionId.trim().length() > 0) {
                        ajaxresponse.addCallbackArgument("sourcefilename", filename + "_exists#" + versionId);
                    } else {
                        ajaxresponse.addCallbackArgument("sourcefilename", filename + "_exists");
                    }
                    break block156;
                }
                if (mode == Mode.REVERT) {
                    FileManager.TempFile tempFile = null;
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String locationpolicynode = ajaxresponse.getRequestParameter("locationpolicynode");
                    FileManager.TempFile orgTemp = null;
                    String orgtempid = null;
                    if (ajaxresponse.getRequestParameter("attachmentnum").length() > 0) {
                        try {
                            orgTemp = FileManager.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(ajaxresponse.getRequestParameter("attachmentnum")), this.getConnectionId());
                        }
                        catch (Exception e) {
                            this.logger.error("Failed to obtain orginal attachment. Error: " + e.getMessage());
                        }
                    } else if (ajaxresponse.getRequestParameter("tempid").length() > 0) {
                        try {
                            orgTemp = FileManager.TempFile.getTempFile(ajaxresponse.getRequestParameter("tempid"), false, this.getQueryProcessor(), this.getConnectionId());
                        }
                        catch (Exception e) {
                            this.logger.error("Failed to obtain orginal attachment temp. Error: " + e.getMessage());
                        }
                    }
                    if (orgTemp != null && (orgTemp.getData() != null || orgTemp.getOrgData() != null)) {
                        tempFile = new FileManager.TempFile(orgTemp.getOrgData() != null ? orgTemp.getOrgData() : orgTemp.getData(), orgTemp.getFileName(), FileManager.TempSource.ATTACHMENT, true, this.getConnectionId());
                    } else {
                        this.logger.error("Failed to revert markup image");
                    }
                    String tempid = null;
                    try {
                        tempid = tempFile != null ? tempFile.setTempFile(-1L, locationpolicynode, this.getConnectionId()) : "";
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to set back temp record.");
                    }
                    if (tempid != null && tempid.length() > 0) {
                        if (orgtempid != null && orgtempid.length() > 0) {
                            try {
                                FileManager.TempFile.removeTempFile(orgtempid, this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionId());
                            }
                            catch (Exception e) {
                                this.logger.warn("Failed to remove old temp file.");
                            }
                        }
                        ajaxresponse.addCallbackArgument("elementid", ajaxresponse.getRequestParameter("elementid", "attachments"));
                        ajaxresponse.addCallbackArgument("rownum", ajaxresponse.getRequestParameter("rownum"));
                        ajaxresponse.addCallbackArgument("tempid", tempid);
                        ajaxresponse.addCallbackArgument("filename", tempFile != null ? tempFile.getFileName() : "");
                        ajaxresponse.addCallbackArgument("typeflag", tempFile != null ? tempFile.getTypeFlag() : "");
                        break block156;
                    }
                    ajaxresponse.setError("Could not revert file.");
                    break block156;
                }
                if (mode == Mode.RESETHASH) {
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachments");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    int attachmentnum = 1;
                    try {
                        attachmentnum = Integer.parseInt(ajaxresponse.getRequestParameter("attachmentnum", "1"));
                    }
                    catch (Exception e) {
                        attachmentnum = 1;
                    }
                    String rl = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getRoleList();
                    if (rl.length() > 0 && Arrays.asList(StringUtil.split(rl, ";")).contains("Administrator")) {
                        SafeSQL safeSQL = new SafeSQL();
                        StringBuilder sql = new StringBuilder();
                        sql.append("UPDATE sdiattachment SET datahash=0 WHERE sdcid=").append(safeSQL.addVar(sdcid)).append(" ");
                        sql.append("AND keyid1=").append(safeSQL.addVar(keyid1)).append(" ");
                        if (keyid2.length() > 0) {
                            sql.append("AND keyid2=").append(safeSQL.addVar(keyid2)).append(" ");
                        }
                        if (keyid3.length() > 0) {
                            sql.append("AND keyid3=").append(safeSQL.addVar(keyid3)).append(" ");
                        }
                        sql.append("AND attachmentnum=").append(safeSQL.addVar(attachmentnum)).append("");
                        try {
                            this.getQueryProcessor().execPreparedUpdate(sql.toString(), safeSQL.getValues());
                            ajaxresponse.addCallbackArgument("elementid", elementid);
                        }
                        catch (Exception e) {
                            ajaxresponse.setError("Failed to update data hash.");
                        }
                    } else {
                        ajaxresponse.setError("Only Administrators can reset attachment hash data.");
                    }
                    break block156;
                }
                if (mode == Mode.CHECKHASH) {
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    boolean failed = false;
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuilder sql = new StringBuilder();
                    sql.append("SELECT datahash FROM sdiattachment WHERE datahash=-1 AND sdcid=").append(safeSQL.addVar(sdcid)).append(" ");
                    sql.append("AND keyid1=").append(safeSQL.addVar(keyid1)).append(" ");
                    if (keyid2.length() > 0) {
                        sql.append("AND keyid2=").append(safeSQL.addVar(keyid2)).append(" ");
                    }
                    if (keyid3.length() > 0) {
                        sql.append("AND keyid3=").append(safeSQL.addVar(keyid3)).append(" ");
                    }
                    try {
                        DataSet test = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        if (test != null && test.size() > 0) {
                            failed = true;
                        }
                    }
                    catch (Exception test) {
                        // empty catch block
                    }
                    ajaxresponse.addCallbackArgument("refresh", failed ? "Y" : "N");
                    break block156;
                }
                if (mode != Mode.SAVE) break block156;
                String sdcid = ajaxresponse.getRequestParameter("sdcid");
                boolean viewonly = ajaxresponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                if (!viewonly) {
                    String attachmentpolicynode = ajaxresponse.getRequestParameter("attachmentpolicynode");
                    PropertyList properties = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception safeSQL) {
                            // empty catch block
                        }
                    }
                    DataSet attachmentdata = null;
                    String data = ajaxresponse.getRequestParameter("data");
                    if (data.length() > 0) {
                        try {
                            attachmentdata = new DataSet(new JSONObject(data));
                        }
                        catch (Exception test) {
                            // empty catch block
                        }
                    }
                    try {
                        FileManager.saveAttachmentData(sdcid, attachmentdata, attachmentpolicynode, this.getConnectionId());
                        this.logger.debug("Attachments saved through Ajax.");
                    }
                    catch (SapphireException e) {
                        this.logger.error("Failed to save attachments through Ajax.", e);
                        ajaxresponse.setError("Failed to save attachments.", e);
                    }
                    break block156;
                }
                ajaxresponse.setError("Element is view only and cannot be saved.");
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    public static String getUploadArea(String id) {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"" + id + "_uploader\" class=\"dropzone\" style=\"height:197px;\"></div>");
        return html.toString();
    }

    public static String getEditArea(String id, PropertyList fileDetails, String keyid1, String keyid2, Logger logger, boolean devMode, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
        html.append("<div id=\"filetoolbar\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: rgb(246, 246, 246); border: 1px solid gray; padding: 2px;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left: 5px; padding-right: 5px;\"><tbody><tr>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderUpload('").append(id).append("');\" title=\"").append(tp.translate("Upload a new file")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_upload.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.doRefreshFile('").append(id).append("');\" title=\"").append(tp.translate("Revert file from attachment")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_return.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        if (fileType == FileTypeGroup.IMAGE) {
            html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderMarkup('").append(id).append("');\" title=\"").append(tp.translate("Markup image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_draw_paintbrush.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
            html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderCropper('").append(id).append("');\" title=\"").append(tp.translate("Crop and Edit image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_edit_box.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        }
        html.append("</tr></tbody></table>");
        html.append("</td>");
        html.append("</tr></tbody></table>");
        html.append("</div>");
        html.append("<div style=\"background-color:#FFFFFF;border:solid 1px #A9A9A9;\">");
        html.append("<div style=\"padding:5px;\">");
        html.append(tp.translate("Uploaded File")).append(": <strong>").append(fileDetails.getProperty("description", fileDetails.getProperty("shortfilename", fileDetails.getProperty("filename")))).append("</strong> ").append(tp.translate("of type")).append(" ").append(fileType.getName());
        html.append("</div>");
        html.append("<div style=\"padding:5px;\">");
        PropertyList markup = fileDetails.getPropertyList("markup") != null ? fileDetails.getPropertyList("markup") : new PropertyList();
        switch (fileType) {
            case IMAGE: {
                boolean raw = markup.getProperty("raw", "N").equalsIgnoreCase("Y");
                html.append(tp.translate("Size")).append(" ");
                html.append("<input ").append(raw ? "disabled " : "").append("type=text size=3 value=\"").append(markup.getProperty("x", "0")).append("\" id=\"").append(id).append("_x\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">px");
                html.append(" ").append(tp.translate("by")).append(" ");
                html.append("<input  ").append(raw ? "disabled " : "").append("type=text size=3 value=\"").append(markup.getProperty("y", "0")).append("\" id=\"").append(id).append("_y\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">px");
                html.append("&nbsp;");
                html.append("<input type=checkbox id=\"").append(id).append("_raw\" onclick=\"fileEditor.doChange(this,'").append(id).append("')\" ").append(raw ? "checked" : "").append(">");
                html.append(" ").append(tp.translate("Use Raw Image")).append("");
                html.append(" ").append(tp.translate("(Native Resolution: ")).append(markup.getProperty("total")).append(")");
                break;
            }
            case WORD: {
                int wt = 0;
                try {
                    wt = Integer.parseInt(markup.getProperty("total", "0"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (wt > 1) {
                    html.append(tp.translate("Show pages")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("x", "1")).append("\" id=\"").append(id).append("_x\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" ").append(tp.translate("to")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("y", wt + "")).append("\" id=\"").append(id).append("_y\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" (").append(tp.translate("maximum")).append(" ").append(wt).append(" ").append(tp.translate("pages")).append(")");
                    break;
                }
                html.append("(").append(tp.translate("single page document")).append(")");
                break;
            }
            case TXT: {
                int tt = 0;
                try {
                    tt = Integer.parseInt(markup.getProperty("to", markup.getProperty("total", "0")));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (tt > 1) {
                    html.append(tp.translate("Show lines")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("x", "1")).append("\" id=\"").append(id).append("_x\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" ").append(tp.translate("to")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("y", tt + "")).append("\" id=\"").append(id).append("_y\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" (").append(tp.translate("maximum")).append(" ").append(tt).append(" ").append(tp.translate("lines")).append(")");
                    break;
                }
                html.append("(").append(tp.translate("single line file")).append(")");
            }
        }
        html.append("</div>");
        html.append("<div id=\"").append(id).append("_preview\" style=\"margin: 5px; padding:5px; border: solid 1px #CFDFF0;overflow:auto;max-height:300px;\">");
        html.append(FilesAjaxHandler.getViewArea(fileDetails, keyid1, keyid2));
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    public static String getImageEditArea(String id, PropertyList fileDetails, String keyid1, String keyid2, Logger logger, boolean devMode, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
        html.append("<div id=\"imageedittoolbar\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: rgb(246, 246, 246); border: 1px solid gray; padding: 2px;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left: 5px; padding-right: 5px;\"><tbody><tr>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.crop('").append(id).append("');\" title=\"").append(tp.translate("Crop Image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_crop.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.rotate('").append(id).append("');\" title=\"").append(tp.translate("Rotate Clockwise 90%")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_rotate_clockwise.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.flipH('").append(id).append("');\" title=\"").append(tp.translate("Flip Horizontally")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_flip_horizontal.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.flipV('").append(id).append("');\" title=\"").append(tp.translate("Flip Vertically")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_flip_vertical.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("</tr></tbody></table>");
        html.append("</td>");
        html.append("</tr></tbody></table>");
        html.append("</div>");
        html.append("<div id=\"").append(id).append("_croppercontainer\" style=\"background-color:#FFFFFF;border:solid 1px #A9A9A9;\">");
        String url = "";
        PropertyList markup = fileDetails.getPropertyList("markup");
        String attNum = fileDetails.getProperty("attachment", "");
        url = markup != null && markup.getProperty("raw", "N").equalsIgnoreCase("Y") ? "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y" : ((url = fileDetails.getProperty("display")).length() > 0 ? "data:" + url : "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y");
        html.append("<img id=\"").append(id).append("_cropper\" src=\"").append(url).append("\" style=\"max-width:100%;\">");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    public static String getViewArea(PropertyList fileDetails, String keyid1, String keyid2) {
        StringBuffer html = new StringBuffer();
        String attNum = fileDetails.getProperty("attachment", "");
        if (attNum.length() > 0) {
            FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
            switch (fileType) {
                case IMAGE: {
                    String url = "";
                    PropertyList markup = fileDetails.getPropertyList("markup");
                    url = markup != null && markup.getProperty("raw", "N").equalsIgnoreCase("Y") ? "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y" : ((url = fileDetails.getProperty("display")).length() > 0 ? "data:" + url : "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y");
                    html.append("<img src=\"").append(url).append("\" title=\"").append(fileDetails.get("filename")).append("\">");
                    break;
                }
                case WORD: 
                case TXT: {
                    html.append(fileDetails.getProperty("display"));
                }
            }
        }
        return html.toString();
    }

    public static String getMarkupArea(String id, PropertyList fileDetails, Logger logger, boolean devMode, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
        PropertyList markup = fileDetails.getPropertyList("markup") != null ? fileDetails.getPropertyList("markup") : new PropertyList();
        switch (fileType) {
            case IMAGE: {
                html.append("<div id=\"").append(id).append("_paint\" style=\"border:solid 1px #A9A9A9;\"></div>");
                break;
            }
            case WORD: 
            case TXT: {
                html.append(tp.translate("No markup available."));
            }
        }
        return html.toString();
    }

    private static enum Mode {
        UPLOAD,
        UPLOADTEMP,
        VALIDATE,
        REFRESH,
        CREATE,
        CLEANUP,
        SAVE,
        REVERT,
        AWS,
        AZURE,
        CHECKHASH,
        RESETHASH,
        BROWSEFILE;

    }
}

