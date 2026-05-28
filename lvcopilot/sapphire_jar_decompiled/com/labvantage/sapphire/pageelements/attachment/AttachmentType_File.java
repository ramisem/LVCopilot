/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.attachment.NetworkAttachmentRepository;
import com.labvantage.sapphire.pageelements.attachment.AttachmentManager;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.ForwardUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentType_File
extends BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private final String REFERENCE = "R";
    private final String UPLOADANDSTORE = "S";
    private final String UPLOADANDREFERENCE = "U";
    private final String STORE = "F";

    @Override
    public String getDisplayValue() {
        return "R=Reference;F=Store;U=Upload & Reference;S=Upload & Store";
    }

    @Override
    public String getDisplayValue(String typeflag) {
        if ("R".equals(typeflag)) {
            return "R=Reference";
        }
        if ("F".equals(typeflag)) {
            return "F=Store";
        }
        if ("U".equals(typeflag)) {
            return "U=Upload & Reference";
        }
        if ("S".equals(typeflag)) {
            return "S=Upload & Store";
        }
        return "";
    }

    @Override
    public String getLabel(String typeflag) {
        if ("R".equals(typeflag)) {
            return "Reference";
        }
        if ("F".equals(typeflag)) {
            return "Store";
        }
        if ("U".equals(typeflag)) {
            return "Upload & Reference";
        }
        if ("S".equals(typeflag)) {
            return "Upload & Store";
        }
        return "Unknown";
    }

    @Override
    public String getHint(String typeflag, DataSet data, int row, String filename, TranslationProcessor tp) {
        if ("R".equals(typeflag)) {
            return tp.translate("Referenced by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
        }
        if ("F".equals(typeflag)) {
            return tp.translate("Stored by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
        }
        if ("U".equals(typeflag)) {
            String source = data.getValue(row, "sourcefilename", "");
            if (source.length() > 0) {
                String file;
                source = source.lastIndexOf("\\") > -1 ? source.substring(source.lastIndexOf("\\") + 1) : source.substring(source.lastIndexOf("/") + 1);
                String string = file = filename.lastIndexOf("\\") > -1 ? filename.substring(source.lastIndexOf("\\") + 1) : filename.substring(filename.lastIndexOf("/") + 1);
                if (!file.equalsIgnoreCase(source)) {
                    return source + " " + tp.translate("stored as") + " " + filename + ";" + tp.translate("Uploaded from") + " " + data.getValue(row, "sourcefilename", tp.translate("unknown location")) + " " + tp.translate("by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
                }
                return tp.translate("Uploaded from") + " " + data.getValue(row, "sourcefilename", tp.translate("unknown location")) + " " + tp.translate("by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
            }
            return tp.translate("Uploaded from") + " " + data.getValue(row, "sourcefilename", tp.translate("unknown location")) + " " + tp.translate("by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
        }
        if ("S".equals(typeflag)) {
            return tp.translate("Uploaded and stored from") + " " + filename + " " + tp.translate("by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
        }
        return "Unknown";
    }

    @Override
    public String getAllTypeflagList() {
        return "R;F;U;S";
    }

    @Override
    public boolean isAllowedDownload(int row, DataSet attachment) {
        return true;
    }

    @Override
    public String getHideContentDivJavascript() {
        return "            var oServer = oDoc.getElementById( 'at' + iRow + '_server' );\n            if ( oServer != null ) {\n                oServer.style.display = 'none';\n            }\n\n            var oLocal = oDoc.getElementById( 'at' + iRow + '_local' );\n            if ( oLocal != null ) {\n                oLocal.style.display = 'none';\n            }\n\n            var oUpDiv = oDoc.getElementById( 'at' + iRow + '_uploaddiv' );\n            if ( oUpDiv != null ) {\n                oUpDiv.style.display = 'none';\n            }";
    }

    @Override
    public String getShowContentDivJavascript() {
        return "            if ( typeflag == 'R' || typeflag == 'F' ) { // ref or store\n                var oServer = oDoc.getElementById( 'at' + iRow + '_server' );\n                if ( oServer != null ) {\n                    oServer.style.display = 'block';\n                }\n            }\n            if ( typeflag == 'S' ) { // store\n                var oLocal = oDoc.getElementById( 'at' + iRow + '_local' );\n                if ( oLocal != null ) {\n                    oLocal.style.display = 'block';\n                }\n            }\n            if ( typeflag == 'U' ) { // ref or store\n                var oLocal = oDoc.getElementById( 'at' + iRow + '_local' );\n                if ( oLocal != null ) {\n                    oLocal.style.display = 'block';\n                }\n                oUpDiv = oDoc.getElementById( 'at' + iRow + '_uploaddiv' );\n                if ( oUpDiv != null ) {\n                    oUpDiv.style.display = 'block';\n                }\n            }";
    }

    @Override
    public void getFilenameFieldInitialRender(AttachmentManager am, StringBuffer content, boolean viewOnly, boolean canRestore, boolean isLocked, int row, String type, String filename, PropertyList col, String name, String hint, TranslationProcessor tp, Browser browser) {
        content.append("<div dir=\"ltr\">");
        if (type.equalsIgnoreCase("R") || viewOnly || isLocked || !canRestore) {
            if (browser.isEdge()) {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" name=\"").append(name).append("\"_reference\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -70)).append("px;\" ");
                if (isLocked) {
                    content.append(" class=\"maint_lockedfield\" ");
                }
                content.append(" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\">");
                content.append("<button type=\"button\" ").append(viewOnly || isLocked || !canRestore ? "disabled" : "").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
            } else if (browser.isWebkit()) {
                content.append("<button type=\"button\" ").append(viewOnly || isLocked || !canRestore ? "disabled" : "").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:auto;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Choose File")).append("</button>");
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" name=\"").append(name).append("\"_reference\" value=\"").append(filename.length() > 0 ? filename : tp.translate("no file selected")).append("\" readonly style=\"width:").append(this.getWidth(col, -65)).append("px;\" ");
                if (isLocked) {
                    content.append(" class=\"maint_lockedfield\" ");
                }
                content.append(" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\">");
            } else {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" name=\"").append(name).append("\"_reference\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -70)).append("px;\" ");
                if (isLocked) {
                    content.append(" class=\"maint_lockedfield\" ");
                }
                content.append(" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\">");
                content.append("<button type=\"button\" ").append(viewOnly || isLocked || !canRestore ? "disabled" : "").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
            }
        } else if (type.equalsIgnoreCase("U")) {
            if (hint.contains(";") && filename.length() > 0) {
                String[] hints = StringUtil.split(hint, ";");
                hint = hints[1];
                filename = hints[0];
            }
            if (viewOnly || isLocked || !canRestore) {
                if (browser.isWebkit()) {
                    content.append("<button type=\"button\" disabled ").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:auto;font-size:8pt;\" >").append(tp.translate("Choose File")).append("</button>");
                    content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" name=\"").append(name).append("\"_reference\" value=\"").append(filename.length() > 0 ? filename : tp.translate("no file selected")).append("\" readonly style=\"width:").append(this.getWidth(col, -65)).append("px;\" ");
                    if (isLocked) {
                        content.append(" class=\"maint_lockedfield\" ");
                    }
                    content.append(" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\">");
                } else {
                    content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" name=\"").append(name).append("\"_reference\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -65)).append("px;\" ");
                    if (isLocked) {
                        content.append(" class=\"maint_lockedfield\" ");
                    }
                    content.append(" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\">");
                    content.append("<button type=\"button\" disabled ").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" >").append(tp.translate("Browse...")).append("</button>");
                }
            } else {
                content.append("<input type=file id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_local\" name=\"").append(name).append("\" style=\"width:").append(this.getWidth(col, 0)).append("px;z-index: -1;\" value=\"").append("\" onchange=\"").append("attachmentManager").append(".doReBrowseChange();\"");
                if (isLocked) {
                    content.append(" class=\"maint_lockedfield\" ");
                }
                content.append(">");
                if (browser.isEdge()) {
                    content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -80)).append("px;z-index:").append("1").append(";position:absolute;left:").append("0").append("px;margin-top:").append("0").append(";\" title=\"").append(hint).append("\">");
                } else if (browser.isWebkit()) {
                    content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, browser.isChrome() ? -75 : -80)).append("px;z-index:").append("1").append(";position:absolute;left:").append(browser.isChrome() ? "83" : "90").append("px;margin-top:").append(browser.isChrome() ? "0;top:0px;" : "0px;top:-5px;").append(";\" title=\"").append(hint).append("\">");
                } else {
                    content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -80)).append("px;z-index:").append("1").append(";position:absolute;left:0;top:0px;\" title=\"").append(hint).append("\" >");
                }
            }
        } else if (type.equalsIgnoreCase("S")) {
            content.append("<input type=file id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_local\" name=\"").append(name).append("\" style=\"width:").append(this.getWidth(col, 0)).append("px;\" value=\"").append("\" onchange=\"").append("attachmentManager").append(".doReBrowseChange();\" style=\"z-index:1;position:absolute;left:0px;top:2px;\" title=\"").append(hint).append("\"");
            content.append(">");
            if (browser.isEdge()) {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -80)).append("px;z-index:").append("1").append(";position:absolute;left:").append("0").append("px;margin-top:").append("0").append(";\" title=\"").append(hint).append("\">");
            } else if (browser.isWebkit()) {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, browser.isChrome() ? -75 : -80)).append("px;z-index:").append("1").append(";position:absolute;left:").append(browser.isChrome() ? "83" : "90").append("px;margin-top:").append(browser.isChrome() ? "0px;top:0px;" : "0px;top:-5px;").append(";\" title=\"").append(hint).append("\">");
            } else {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename\" value=\"").append(filename).append("\" readonly style=\"width:").append(this.getWidth(col, -80)).append("px;z-index:").append("1").append(";position:absolute;left:0px;top:-0px;\" title=\"").append(hint).append("\">");
            }
        } else if (type.equalsIgnoreCase("F")) {
            if (browser.isEdge()) {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" value=\"").append(filename).append("\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -75)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\"");
                content.append(">");
                content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
            } else if (browser.isWebkit()) {
                content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:auto;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Choose File")).append("</button>");
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" value=\"").append(filename).append("\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -65)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\"");
                content.append(">");
            } else {
                content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server\" value=\"").append(filename).append("\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -75)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\" title=\"").append(hint).append("\"");
                content.append(">");
                content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append(row).append("_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
            }
        }
        content.append("</div>");
    }

    @Override
    public void getFilenameFieldTemplateRow(AttachmentManager am, StringBuffer content, PropertyList col, TranslationProcessor tp, Browser browser) {
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_server\" style=\"display:none;\">");
        if (browser.isEdge()) {
            content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -75)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\"");
            content.append(">");
            content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
        } else if (browser.isWebkit()) {
            content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server_btn\" style=\"height:auto;width:auto;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Choose File")).append("</button>");
            content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -65)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\"");
            content.append(">");
        } else {
            content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server\" name=\"file1_reference\" readonly style=\"width:").append(this.getWidth(col, -75)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\"");
            content.append(">");
            content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_server_btn\" style=\"height:auto;width:65px;font-size:8pt;\" onclick=\"").append("attachmentManager").append(".serverBrowse();\">").append(tp.translate("Browse...")).append("</button>");
        }
        content.append("</div>");
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_local\" style=\"display:block;\">");
        content.append("<input type=file id=\"").append(ATTACHMENT_CODE).append("[__row]_filename_local\" name=\"file1\" style=\"width:").append(this.getWidth(col, 0)).append("px;\" value=\"").append("\" onchange=\"sdiSetRowUpdate(event);\"");
        content.append(">");
        content.append("</div>");
    }

    @Override
    public void viewAttachment(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Attachment attachment) throws IOException {
    }

    public static String renameFileFromPolicy(String infilename, String connectionId, boolean useKeyId2, boolean useKeyId3) {
        return AttachmentType_File.renameFileFromPolicy(infilename, connectionId, useKeyId2, useKeyId3, "");
    }

    public static String renameFileFromPolicy(String infilename, String connectionId, boolean useKeyId2, boolean useKeyId3, String policynode) {
        ConfigurationProcessor cp = new ConfigurationProcessor(connectionId);
        PropertyList policy = null;
        try {
            policy = cp.getPolicy("AttachmentPolicy", policynode != null && policynode.length() > 0 ? policynode : "Sapphire Custom");
            return AttachmentType_File.renameFileFromPolicy(infilename, useKeyId2, useKeyId3, policy);
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
            return infilename;
        }
    }

    public static String renameFileFromPolicy(String infilename, boolean useKeyId2, boolean useKeyId3, PropertyList policy) {
        String outfilename = infilename;
        String renameString = "";
        boolean renameOnUpload = false;
        boolean systemRename = false;
        if (policy != null && policy.size() > 0 && policy.containsKey("filereference") && policy.getPropertyList("filereference").containsKey("renameonupload")) {
            PropertyList rnu = policy.getPropertyList("filereference").getPropertyList("renameonupload");
            renameOnUpload = rnu.getProperty("rename", "N").equalsIgnoreCase("Y") || rnu.getProperty("rename", "N").equalsIgnoreCase("S");
            systemRename = rnu.getProperty("rename", "N").equalsIgnoreCase("S");
            renameString = rnu.getProperty("pattern", "");
        }
        return NetworkAttachmentRepository.renameFileFromExpression(infilename, useKeyId2, useKeyId3, systemRename, renameOnUpload ? renameString : "");
    }

    @Override
    public String addAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, String description, String typeflag, HashMap addtionalFields, List fileItems, String errorMsg) throws IOException {
        return "";
    }

    @Override
    public String tempAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, List fileItems, JSONObject job) throws IOException {
        return "";
    }

    @Override
    public byte[] getTempAttachment(String tempid, String connectionId) {
        FileManager.TempFile tempdata = FileManager.TempFile.getTempFile(tempid, true, new QueryProcessor(connectionId), connectionId);
        if (tempdata != null) {
            return tempdata.getData() != null ? tempdata.getData().getData() : null;
        }
        return null;
    }

    private String doUploadReferenceAddUpdate(int mode, String keyid2, String keyid3, String connectionId, HashMap addtionalFields, List fileItems, PrintWriter out, HttpServletRequest request) {
        String errorMsg = "";
        String uploaddir = FileUpload.getFileItemValue(fileItems, "uploaddir");
        String uploadto = FileUpload.getFileItemValue(fileItems, "uploadto");
        String locationpolicynode = FileUpload.getFileItemValue(fileItems, "locationpolicynode");
        String locationpolicyitem = FileUpload.getFileItemValue(fileItems, "locationpolicyitem");
        if (uploadto.length() == 0 && uploaddir.length() == 0 && locationpolicyitem.length() > 0 && locationpolicynode.length() > 0) {
            try {
                uploaddir = FileManager.getUploadLocation(locationpolicynode, locationpolicyitem, connectionId);
            }
            catch (Exception e) {
                Trace.logWarn("Could not process upload location from policy.");
            }
        }
        if (uploadto.length() > 0) {
            errorMsg = AttachmentType_File.addUpdate(out, fileItems, connectionId, mode, "U", addtionalFields, request);
        } else if (uploaddir.length() > 0) {
            String prefix;
            FileItem fifile;
            if (uploaddir.endsWith("\\") || uploaddir.endsWith("/")) {
                uploaddir = uploaddir.substring(0, uploaddir.length() - 1);
            }
            if ((fifile = FileUpload.getFileItem(fileItems, prefix = "file1")) == null) {
                prefix = "file[0]";
                fifile = FileUpload.getFileItem(fileItems, prefix);
            }
            if (fifile != null) {
                String newfilename = FileUpload.getFileName(fifile);
                if (!addtionalFields.containsKey("__ignorerename")) {
                    newfilename = AttachmentType_File.renameFileFromPolicy(newfilename, connectionId, keyid2 != null && keyid2.length() > 0, keyid3 != null && keyid3.length() > 0);
                }
                addtionalFields.remove("__ignorerename");
                if (newfilename != null && newfilename.length() > 0) {
                    if (newfilename.indexOf("\\") > -1) {
                        newfilename = newfilename.substring(newfilename.lastIndexOf("\\") + 1);
                    } else if (newfilename.indexOf("/") > -1) {
                        newfilename = newfilename.substring(newfilename.lastIndexOf("//") + 1);
                    }
                    newfilename = uploaddir.indexOf("\\") > -1 ? uploaddir + "\\" + newfilename : uploaddir + "/" + newfilename;
                    addtionalFields.put(prefix + "_uploadto", newfilename);
                    errorMsg = AttachmentType_File.addUpdate(out, fileItems, connectionId, mode, "U", addtionalFields, request);
                } else if (mode == 0) {
                    errorMsg = "No file name provided.";
                }
            } else if (mode == 0) {
                errorMsg = "No file provided.";
            }
        } else if (mode == 0) {
            errorMsg = "No upload directory or upload path provided.";
        }
        return errorMsg;
    }

    @Override
    public void processGetAttachment(Attachment attachment, String connectionId) throws ServiceException {
        if (attachment.getBlob() != null && (attachment.getType().equalsIgnoreCase("S") || attachment.getType().equalsIgnoreCase("F"))) {
            try {
                attachment.setSize(attachment.getBlob().length());
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                attachment.setInputStream(attachment.getBlob().getBinaryStream());
            }
            catch (Exception e) {
                Logger.logError("Could not obtain blob.", e);
            }
        } else if ((attachment.getType().equalsIgnoreCase("R") || attachment.getType().equalsIgnoreCase("U")) && attachment.getFilename() != null && attachment.getFilename().length() > 0) {
            File f = new File(attachment.getFilename());
            if (f.exists()) {
                attachment.setSize(f.length());
                try {
                    attachment.setInputStream(new FileInputStream(attachment.getFilename()));
                }
                catch (Exception e) {
                    Logger.logError("Could not read file.", e);
                }
            } else {
                Logger.logError("File does not exsit. " + attachment.getFilename());
            }
        }
    }

    @Override
    public String getContentValue(DataSet data, int row) {
        return data.getValue(row, "filename", "");
    }

    private String addReference(String fileName, String description, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, HashMap additionalFields) {
        String error = "";
        if (fileName.length() > 0) {
            Logger.logDebug("fileName = " + fileName);
            File file = new File(fileName);
            if (file.exists()) {
                HashMap map = additionalFields != null && additionalFields.size() > 0 ? (HashMap)additionalFields.clone() : new HashMap();
                map.put("sdcid", sdcid);
                map.put("keyid1", keyid1);
                if (keyid2.length() > 0) {
                    map.put("keyid2", keyid2);
                }
                if (keyid3.length() > 0) {
                    map.put("keyid3", keyid3);
                }
                map.put("type", "R");
                map.put("filename", fileName);
                map.put("sourcefilename", "");
                map.put("description", description);
                ActionBlock actionBlock = new ActionBlock();
                try {
                    actionBlock.setAction("AddSDIAttachment", "AddSDIAttachment", "1", map);
                    new ActionProcessor(connectionId).processActionBlock(actionBlock);
                }
                catch (ActionException ae) {
                    Logger.logError("Could not save attachment with error:" + ae.getMessage());
                    error = ae.getMessage();
                }
            } else {
                Logger.logError("Could not access file from server.");
                error = "Could not access file from server.";
            }
        } else {
            Logger.logError("No file reference provided.");
            error = "No file reference provided.";
        }
        return error;
    }

    public static String addUpdate(PrintWriter output, List fileItems, String connectionid, int mode, String attachmenttype, HashMap additionalFields, HttpServletRequest request) {
        String errormessage = "";
        String sdcid = null;
        String keyid1 = null;
        String keyid2 = null;
        String keyid3 = null;
        String nexturl = null;
        StringBuffer uploadedFiles = new StringBuffer();
        StringBuffer attachmentNumbers = new StringBuffer();
        StringBuffer attachmentFiles = new StringBuffer();
        String uploadoptions = null;
        try {
            Iterator i = fileItems.iterator();
            PropertyList fields = additionalFields == null || additionalFields.size() == 0 ? new PropertyList() : new PropertyList(additionalFields);
            while (i.hasNext()) {
                FileItem fi = (FileItem)i.next();
                if (!fi.isFormField()) continue;
                String fieldName = fi.getFieldName();
                String fieldValue = FileUpload.getFileItemString(fi);
                if (fieldName.equals("keyid1")) {
                    keyid1 = fieldValue;
                    continue;
                }
                if (fieldName.equals("keyid2")) {
                    keyid2 = fieldValue;
                    continue;
                }
                if (fieldName.equals("keyid3")) {
                    keyid3 = fieldValue;
                    continue;
                }
                if (fieldName.equals("sdcid")) {
                    sdcid = fieldValue;
                    continue;
                }
                if (fieldName.equals("__nexturl")) {
                    nexturl = fieldValue;
                    continue;
                }
                if (fieldName.equals("uploadoptions")) {
                    uploadoptions = fieldValue;
                    continue;
                }
                fields.setProperty(fieldName, fieldValue);
            }
            i = fileItems.iterator();
            boolean filefound = false;
            while (i.hasNext()) {
                FileItem fi = (FileItem)i.next();
                String fieldName = fi.getFieldName();
                String fileName = FileUpload.getFileName(fi);
                if (fileName == null) continue;
                if (fileName.length() == 0) {
                    int filesizelimit;
                    filefound = true;
                    byte[] indata = fi.get();
                    try {
                        filesizelimit = Integer.parseInt(fields.getProperty(fieldName + "_maxsize", "0"));
                    }
                    catch (NumberFormatException nfe) {
                        Logger.logWarn("Could not obtain file size limit error: " + nfe.getMessage());
                        filesizelimit = 0;
                    }
                    String uploadtoStr = fields.getProperty(fieldName + "_uploadto", "");
                    String attachmentNum = "";
                    if (mode == 1) {
                        attachmentNum = fields.getProperty(fieldName + "_attachmentnum", "");
                    }
                    String fileDesc = fields.getProperty(fieldName + "_desc", fields.getProperty(fieldName + "_description", fields.getProperty("attachmentdesc", "")));
                    if (attachmenttype == null || attachmenttype.length() == 0) {
                        attachmenttype = mode == 0 ? (uploadtoStr != null && uploadtoStr.length() > 0 ? "R" : "S") : "";
                    }
                    if ((attachmenttype.equalsIgnoreCase("R") || attachmenttype.equalsIgnoreCase("U")) && (uploadtoStr == null || uploadtoStr.length() == 0)) {
                        errormessage = "No upload destination provided for upload.";
                        Logger.logError(errormessage);
                    } else {
                        HashMap<String, String> extrafields = new HashMap<String, String>();
                        if (additionalFields != null && additionalFields.size() > 0) {
                            Iterator it = additionalFields.keySet().iterator();
                            while (it.hasNext()) {
                                String key = it.next().toString();
                                if (key.startsWith(fieldName)) {
                                    String newkey = key.substring(fieldName.length() + 1);
                                    if (newkey.equalsIgnoreCase("maxsize") || newkey.equalsIgnoreCase("uploadto") || newkey.equalsIgnoreCase("desc") || newkey.equalsIgnoreCase("attachmentnum")) continue;
                                    extrafields.put(newkey, additionalFields.get(key).toString());
                                    continue;
                                }
                                if (key.startsWith("file")) continue;
                                extrafields.put(key, additionalFields.get(key).toString());
                            }
                        }
                        errormessage = AttachmentType_File.processAddUpdate(connectionid, sdcid, keyid1, keyid2, keyid3, attachmentNum, fileDesc, mode, indata, fileName, attachmenttype, filesizelimit, uploadtoStr, uploadoptions, extrafields, uploadedFiles, attachmentNumbers, attachmentFiles);
                    }
                    if (errormessage.length() <= 0) continue;
                    break;
                }
                errormessage = "Invalid file type. Please check File Location policy Attachment node.";
            }
            if (!filefound && errormessage.length() == 0) {
                errormessage = "No files provided.";
            }
            if (errormessage.length() == 0) {
                output.println("All file(s) uploaded as attachments successfully.");
                output.flush();
                if (nexturl != null && nexturl.length() > 0 && !nexturl.equalsIgnoreCase("null")) {
                    ForwardUtil forward = new ForwardUtil((ServletRequest)request);
                    forward.setProperty("sdcid", sdcid);
                    forward.setProperty("keyid1", keyid1);
                    forward.setProperty("keyid2", keyid2);
                    forward.setProperty("keyid3", keyid3);
                    forward.setProperty("__attachmentnumbers", attachmentNumbers.length() > 0 ? attachmentNumbers.substring(1) : "");
                    forward.setProperty("__attachmentfiles", attachmentFiles.length() > 0 ? attachmentFiles.substring(1) : "");
                    forward.setProperty("__uploadedfiles", uploadedFiles.length() > 0 ? uploadedFiles.substring(1) : "");
                    forward.setProperty("__uploadsuccess", "true");
                    forward.setProperties(fields);
                    output.println(forward.getForm("", nexturl, "post", true));
                }
            } else {
                output.println("File uploaded with the following error message: " + errormessage);
                output.flush();
            }
        }
        catch (Exception e) {
            output.print("Error: " + e.getMessage());
            output.flush();
            Logger.logStackTrace(e);
        }
        return errormessage;
    }

    private String storeAttachment(String fileName, String attachmentNum, String description, int maxSize, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, HashMap additionalFields) {
        String errormsg = "";
        if (fileName != null && fileName.length() > 0) {
            File localfile = new File(fileName);
            if (localfile.exists()) {
                int mode = 0;
                if (attachmentNum != null && attachmentNum.length() > 0) {
                    mode = 1;
                } else {
                    attachmentNum = "";
                }
                try {
                    byte[] indata = FileUpload.getBytesFromFile(localfile);
                    errormsg = AttachmentType_File.processAddUpdate(connectionId, sdcid, keyid1, keyid2, keyid3, attachmentNum, description, mode, indata, fileName, "F", maxSize, "", "", additionalFields, null, null, null);
                }
                catch (IOException e) {
                    errormsg = e.getMessage();
                    Logger.logError(errormsg, e);
                }
            } else {
                errormsg = "The file provided for storage does not exist or is unreachable from the server.";
                Logger.logError(errormsg);
            }
        }
        return errormsg;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String processAddUpdate(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String attachmentNum, String fileDesc, int mode, byte[] indata, String fileName, String attachmentType, int filesizelimit, String uploadtoStr, String uploadoptions, HashMap additionalFields, StringBuffer uploadedFiles, StringBuffer attachmentNumbers, StringBuffer attachmentFiles) {
        String errormessage = "";
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(connectionid);
        boolean sizeOK = true;
        if (filesizelimit > 0 && indata.length > filesizelimit) {
            sizeOK = false;
            errormessage = "File " + fileName + " is not added or updated as attachment due to exceeding size limit of " + filesizelimit + " set by your administrator.";
        }
        if (sizeOK) {
            if ("Upload Only".equals(uploadoptions) && uploadtoStr != null && uploadtoStr.length() > 0) {
                PropertyList filelocationPolicy = null;
                try {
                    filelocationPolicy = new ConfigurationProcessor(connectionid).getPolicy("FileLocationPolicy", "Upload Custom");
                }
                catch (Exception e) {
                    Trace.logWarn("Unable to obtain file location policy. Please make sure you have an Upload Custom node.");
                    filelocationPolicy = new PropertyList();
                }
                if (!FileManager.isValidFileLocation(uploadtoStr, filelocationPolicy)) {
                    errormessage = "Upload location not a valid file location.";
                } else {
                    try (FileOutputStream fileout = new FileOutputStream(uploadtoStr);){
                        fileout.write(indata);
                        if (uploadedFiles != null) {
                            uploadedFiles.append(";").append(uploadtoStr);
                        }
                    }
                    catch (IOException e) {
                        errormessage = "Failed to upload file with error " + e.getMessage();
                    }
                }
            } else {
                HashMap props = additionalFields != null && additionalFields.size() > 0 ? (HashMap)additionalFields.clone() : new HashMap();
                if (sdcid != null) {
                    if (keyid1 != null) {
                        if (keyid2 == null) {
                            keyid2 = "";
                        }
                        if (keyid3 == null) {
                            keyid3 = "";
                        }
                        props.put("sdcid", sdcid);
                        props.put("keyid1", keyid1);
                        props.put("keyid2", keyid2);
                        props.put("keyid3", keyid3);
                        props.put("description", fileDesc);
                        if (attachmentType.equalsIgnoreCase("F") || attachmentType.equalsIgnoreCase("S") || attachmentType.equalsIgnoreCase("U") || attachmentType.equalsIgnoreCase("R")) {
                            props.put("type", attachmentType);
                        } else if (attachmentType.length() == 0) {
                            if (mode == 0) {
                                errormessage = "No attachment type provided.";
                            } else {
                                Logger.logDebug("No attachment type provided");
                            }
                        } else {
                            errormessage = "Incorrect attachment type provided.";
                        }
                        if (attachmentType.equalsIgnoreCase("R") || attachmentType.equalsIgnoreCase("U")) {
                            if (uploadtoStr != null && uploadtoStr.length() > 0) {
                                props.put("uploadto", uploadtoStr);
                                props.put("filename", uploadtoStr);
                                props.put("sourcefilename", fileName);
                            } else {
                                errormessage = "No upload directory provided.";
                            }
                        } else {
                            props.put("filename", fileName);
                        }
                        if (attachmentFiles != null) {
                            attachmentFiles.append(";").append(fileName);
                        }
                        if (attachmentNum != null && attachmentNum.length() > 0) {
                            props.put("attachmentnum", attachmentNum);
                            if (attachmentNumbers != null) {
                                attachmentNumbers.append(";").append(attachmentNum);
                            }
                        } else if (attachmentNumbers != null) {
                            attachmentNumbers.append(";").append("");
                        }
                        if (errormessage.length() == 0) {
                            int rc = 1;
                            try {
                                if (mode == 0) {
                                    Logger.logDebug("AttachmentType_File", "About to call attachmentProcessor.addSDIAttachment...");
                                    rc = attachmentProcessor.addSDIAttachment(props, indata);
                                } else if (mode == 1) {
                                    Logger.logDebug("AttachmentType_File", "About to call attachmentProcessor.editSDIAttachment...");
                                    rc = attachmentProcessor.editSDIAttachment(props, indata);
                                }
                            }
                            catch (Exception e) {
                                Logger.logError("AttachmentRequest - " + e.getMessage());
                                rc = 2;
                            }
                            if (rc == 2) {
                                errormessage = attachmentProcessor.getLastErrorMessage();
                            }
                        }
                    } else {
                        errormessage = "No Key Id 1provided.";
                    }
                } else {
                    errormessage = "No SDC Id provided.";
                }
            }
        }
        return errormessage;
    }

    @Override
    public String addAttachmentNormalRequest(HttpServletRequest request, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, String typeflag, String description, HashMap addtionalFields) {
        String errorMsg;
        String fileName = this.getFileName(request, typeflag);
        if (typeflag.equalsIgnoreCase("R")) {
            errorMsg = this.addReference(fileName, description, connectionId, sdcid, keyid1, keyid2, keyid3, addtionalFields);
        } else if (typeflag.equalsIgnoreCase("F")) {
            int maxSize;
            try {
                maxSize = Integer.parseInt(this.getRequestParameter(request, "maxsize"));
            }
            catch (Exception e) {
                Logger.logWarn("Could not obtain maxsize");
                maxSize = 0;
            }
            errorMsg = this.storeAttachment(fileName, null, description, maxSize, connectionId, sdcid, keyid1, keyid2, keyid3, addtionalFields);
        } else {
            errorMsg = "Invalid type for normal request provided.";
            Logger.logError("Invalid type for normal request provided");
        }
        return errorMsg;
    }

    private String getFileName(HttpServletRequest request, String typeflag) {
        String fileName = "";
        if ((typeflag.equalsIgnoreCase("R") || typeflag.equalsIgnoreCase("F")) && (fileName = this.getRequestParameter(request, "reference")).length() == 0 && (fileName = this.getRequestParameter(request, "filename")).length() == 0) {
            fileName = this.getRequestParameter(request, "");
        }
        return fileName;
    }

    private String getRequestParameter(HttpServletRequest request, String parameterName) {
        String out = request.getParameter(parameterName);
        if (out == null || out.length() == 0) {
            out = request.getParameter("file1_" + parameterName);
        }
        if (out != null && out.length() > 0) {
            return out;
        }
        return "";
    }

    @Override
    public String editAttachment(String attnum, String description, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, HashMap additionalFields, List fileItems) {
        String error = "";
        if (attnum.length() > 0) {
            HashMap map = additionalFields != null && additionalFields.size() > 0 ? (HashMap)additionalFields.clone() : new HashMap();
            String fileName = FileUpload.getFileItemValue(fileItems, "file1");
            if (fileName.length() > 0) {
                map.put("filename", fileName);
            }
            map.put("sdcid", sdcid);
            map.put("keyid1", keyid1);
            if (keyid2.length() > 0) {
                map.put("keyid2", keyid2);
            }
            if (keyid3.length() > 0) {
                map.put("keyid3", keyid3);
            }
            map.put("attachmentnum", attnum);
            map.put("description", description);
            try {
                new ActionProcessor(connectionId).processActionClass("com.labvantage.sapphire.actions.sdi.EditSDIAttachment", map, true);
            }
            catch (ActionException ae) {
                Logger.logError("Could not save attachment with error:" + ae.getMessage());
                error = ae.getMessage();
            }
        } else {
            Logger.logError("No attachment number provided.");
            error = "No attachment number provided.";
        }
        return error;
    }

    @Override
    public String postEditMultiPart(HttpServletRequest request, HttpServletResponse response, List fileItems, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, String description, String attNum, String typeflag, HashMap addtionalFields, String errorMsg) throws IOException {
        PrintWriter out = response.getWriter();
        String fileName = "";
        if ((typeflag.equalsIgnoreCase("R") || typeflag.equalsIgnoreCase("F")) && (fileName = FileUpload.getFileItemValue(fileItems, "reference")).length() == 0 && (fileName = FileUpload.getFileItemValue(fileItems, "filename")).length() == 0) {
            fileName = FileUpload.getFileItemValue(fileItems, "");
        }
        if (typeflag.equalsIgnoreCase("S")) {
            String thefilename;
            Logger.logDebug("About to upload file changes.");
            FileItem file = FileUpload.getFileItem(fileItems, "file1");
            if (file != null && (thefilename = FileUpload.getFileName(file)) != null && thefilename.length() > 0) {
                AttachmentType_File.addUpdate(out, fileItems, connectionId, 1, "S", addtionalFields, request);
            }
        } else if (typeflag.equalsIgnoreCase("U")) {
            errorMsg = this.doUploadReferenceAddUpdate(1, keyid2, keyid3, connectionId, addtionalFields, fileItems, out, request);
        } else if (typeflag.equalsIgnoreCase("F")) {
            int maxSize;
            Logger.logDebug("About to re-store file changes.");
            try {
                maxSize = Integer.parseInt(this.getRequestParameter(request, "maxsize"));
            }
            catch (Exception e) {
                Logger.logWarn("Could not obtain maxsize");
                maxSize = 0;
            }
            errorMsg = this.storeAttachment(fileName, attNum, description, maxSize, connectionId, sdcid, keyid1, keyid2, keyid3, addtionalFields);
        }
        return errorMsg;
    }

    private String resolveFileName(HttpServletRequest request, String typeflag, String fileName) {
        if ((typeflag.equalsIgnoreCase("R") || typeflag.equalsIgnoreCase("F")) && (fileName = this.getRequestParameter(request, "reference")).length() == 0 && (fileName = this.getRequestParameter(request, "filename")).length() == 0) {
            fileName = this.getRequestParameter(request, "");
        }
        return fileName;
    }

    @Override
    public StringBuffer renderUploadContainer(String type, PropertyListCollection uploadData, TranslationProcessor tp, String row, boolean viewOnly) {
        if (!viewOnly && type.equalsIgnoreCase("U")) {
            StringBuffer content = new StringBuffer();
            content.append("<div id=\"").append(ATTACHMENT_CODE).append(row + "_uploaddiv\" style=\"display:block;\">");
            content.append("&nbsp;<img src=\"WEB-CORE/images/gif/UploadRight.gif\" id=\"").append(ATTACHMENT_CODE).append(row + "_uploadimg\" title=\"").append(tp.translate("Upload to...")).append("\">&nbsp;");
            content.append("<select class=\"attman_medfield\" id=\"").append(ATTACHMENT_CODE).append(row + "_uploaddir\" name=\"file1_uploaddir\" \" onchange=\"sdiSetRowUpdate(event);\" >");
            if (uploadData != null && uploadData.size() > 0) {
                content.append("<option value=\"\" title=\"\" selected></option>");
                for (int i = 0; i < uploadData.size(); ++i) {
                    PropertyList uploadItem = uploadData.getPropertyList(i);
                    String location = FileManager.getFileLocation(uploadItem.getProperty("location", ""));
                    String title = uploadItem.getProperty("title", location);
                    content.append("<option value=\"").append(location).append("\" title=\"");
                    content.append(uploadItem.getProperty("description", title)).append(" - ").append(location);
                    content.append("\">").append(title).append("</option>");
                }
            } else {
                content.append("<option value=\"\" title=\"No Upload Locations Defined\"></option>");
            }
            content.append("</select>");
            content.append("</div>");
            return content;
        }
        return super.renderUploadContainer(type, uploadData, tp, row, viewOnly);
    }

    @Override
    public StringBuffer renderActionColumn(int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp, Browser browser) {
        StringBuffer content = new StringBuffer("");
        if (attnum > -1) {
            content.append("<table id=\"actionBtnTable\" width=\"100%\"><tr>\n");
            content.append("<td width=\"25%\" id=\"viewTD\" nowrap>");
            content.append("&nbsp;");
            content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("View attachment")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\" onclick=\"executeView_File( ").append(attnum).append(", 'N' )\">");
            content.append("</td>");
            content.append("<td width=\"25%\" id=\"downloadTD\" nowrap>");
            if (showDownloadIcon) {
                content.append("&nbsp;");
                content.append("<img id=\"btDownloadAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Download attachment to your PC")).append("\" src=\"WEB-CORE/images/gif/DownloadAttachment.gif\" onclick=\"executeView_File( ").append(attnum).append(", 'Y' )\">");
            }
            content.append("</td>");
            showEditIcon = showEditIcon && browser.isIE();
            String enablePromote = showEditIcon && !viewOnly ? "Y" : "N";
            content.append("<td width=\"25%\" id=\"editTD\" nowrap>");
            if (enablePromote.equalsIgnoreCase("Y")) {
                content.append("&nbsp;");
                content.append("<img id=\"btEditAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Edit attachment on your PC")).append("\" src=\"WEB-CORE/images/gif/EditAttachment.gif\" onclick=\"executeEdit_File( ").append(attnum).append(", '").append(StringUtil.replaceAll(contentValue, "\\", "/")).append("', ").append(row).append(" )\">");
            } else {
                content.append("&nbsp;");
                content.append("<img id=\"btEditAction").append(row).append("\" class=\"btn_disabled\" title=\"").append(tp.translate("Edit attachment on your PC")).append("\" src=\"WEB-CORE/images/gif/EditAttachment.gif\">");
            }
            content.append("</td>");
            content.append("<td width=\"25%\" id=\"showHistoryTD\" nowrap>");
            content.append("&nbsp;");
            content.append("<img id=\"btShowHistoryAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Show attachment history.")).append("\" src=\"WEB-CORE/images/gif/AttachmentsHistory.png\" onclick=\"showAttachmentHistory(").append(attnum).append(",").append(row).append(",'").append(enablePromote).append("')\">");
            content.append("</td>");
            content.append("</tr></table>\n");
        }
        return content;
    }

    @Override
    public StringBuffer getViewJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        html.append("function executeView_File( sAttNum, sDownload ){\n");
        html.append("var oForm = document.getElementById( 'viewform' );\n");
        html.append("oForm['attachmentnum'].value = sAttNum;\n");
        html.append("oForm['download'].value = sDownload;\n");
        html.append("if ( sDownload == 'Y' ) {\n");
        html.append("oForm.target = '_layout';\n");
        html.append("attachmentManager.aboutToDownload = true;\n");
        html.append("}\n");
        html.append("else{\n");
        html.append("oForm.target = '_blank';\n");
        html.append("}\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        return html;
    }

    @Override
    public StringBuffer getEditJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        html.append("function executeEdit_File( sAttNum, sFile, iRow ) {\n");
        html.append("if ( attachmentManager.attachmentObject.callEditFile( sAttNum, sFile ) ) {\n");
        html.append("attachmentManager.enableDisableReset( true, iRow );\n");
        html.append("}\n");
        html.append("}\n");
        return html;
    }

    @Override
    public String getOtherHtml() {
        return "";
    }
}

