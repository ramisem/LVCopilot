/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.ChemicalViewer;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.ChemicalFileDetails;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class ChemicalViewerAjaxHandler
extends BaseAjaxRequest {
    static final int TYPE_AUTO = 0;
    static final int TYPE_MOL = 1;
    static final int TYPE_SMILES = 2;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block22: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                String elementid = ajaxresponse.getRequestParameter("elementid");
                Mode mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", Mode.RENDERUPLOAD.toString()).toUpperCase());
                PropertyList config = new PropertyList();
                PropertyList fileDetails = new PropertyList();
                try {
                    if (ajaxresponse.getRequestParameter("config").length() > 0) {
                        config.setJSONString(ajaxresponse.getRequestParameter("config"));
                    }
                    if (ajaxresponse.getRequestParameter("fileproperties").length() > 0) {
                        fileDetails.setJSONString(ajaxresponse.getRequestParameter("fileproperties"));
                    }
                }
                catch (JSONException e) {
                    ajaxresponse.setError("Unable to parse config properties");
                }
                String worksheetitemid = ajaxresponse.getRequestParameter("worksheetitemid");
                String worksheetitemversionid = ajaxresponse.getRequestParameter("worksheetitemversionid");
                if (mode == Mode.RENDERUPLOAD) {
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    String chemType = fileDetails.getProperty("chemtype");
                    ajaxresponse.addCallbackArgument("html", ChemicalViewerAjaxHandler.getUploadArea(elementid, chemType));
                    break block22;
                }
                if (mode == Mode.REFRESH) {
                    String fileproperties = ajaxresponse.getRequestParameter("fileproperties", "");
                    if (fileproperties.length() > 0) {
                        try {
                            PropertyList markup = fileDetails.getPropertyList("markup");
                            if (markup != null && markup.getProperty("refresh", "N").equalsIgnoreCase("Y")) {
                                markup.setProperty("refresh", "N");
                                ChemicalViewerAjaxHandler.refreshFileObject(fileDetails, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, config, this.getConnectionId(), this.logger);
                            }
                            ajaxresponse.addCallbackArgument("elementid", elementid);
                            ajaxresponse.addCallbackArgument("html", ChemicalViewerAjaxHandler.getViewArea(fileDetails, worksheetitemid, worksheetitemversionid));
                            ajaxresponse.addCallbackArgument("file", fileDetails.toJSONString());
                        }
                        catch (Exception e) {
                            ajaxresponse.setError("Could not process file properties.");
                        }
                    }
                    break block22;
                }
                if (mode != Mode.UPLOAD && mode != Mode.UPLOADTEXT) break block22;
                String filedata = ajaxresponse.getRequestParameter("filedata", "");
                if (filedata.length() > 0) {
                    String chemtype = ajaxresponse.getRequestParameter("chemtype");
                    String attNum = ajaxresponse.getRequestParameter("attachment");
                    String worksheetid = ajaxresponse.getRequestParameter("worksheetid");
                    String worksheetversionid = ajaxresponse.getRequestParameter("worksheetversionid");
                    String filename = "";
                    if (mode == Mode.UPLOAD) {
                        filename = ajaxresponse.getRequestParameter("filename");
                    } else {
                        filename = "pastedtext.mol";
                        try {
                            filedata = "data:;base64," + Base64.encodeBase64String((byte[])filedata.getBytes("UTF8"));
                        }
                        catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mode == Mode.UPLOADTEXT || filedata.startsWith("data:")) {
                        try {
                            PropertyList fileout = this.storeAsAttachment(filedata, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, filename, chemtype, attNum, config, Worksheet.getPolicyNode(this.getQueryProcessor(), worksheetid, worksheetversionid), this.getTranslationProcessor());
                            attNum = fileout.getProperty("attachment", "");
                            if (attNum.length() > 0) {
                                ajaxresponse.addCallbackArgument("elementid", elementid);
                                ajaxresponse.addCallbackArgument("filedata", fileout.toJSONString());
                                break block22;
                            }
                            ajaxresponse.setError(fileout.getProperty("lasterror", "Failed to upload or parse chemical file."));
                        }
                        catch (Exception e) {
                            ajaxresponse.setError(e.getMessage(), e);
                        }
                        break block22;
                    }
                    ajaxresponse.setError("No data URL provided.");
                    break block22;
                }
                ajaxresponse.setError("No File data provided.");
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    private static void updateFileObject(PropertyList fileDetails, ByteArrayInputStream bis, String chemType, Logger logger, PropertyList config, String worksheetitemid, TranslationProcessor tp) throws Exception {
        StringBuffer display = new StringBuffer();
        PropertyList markup = fileDetails.getPropertyList("markup");
        bis.reset();
        try {
            ChemicalFileDetails chemFileDetails = new ChemicalFileDetails();
            chemFileDetails.setMarkup(config, markup);
            try {
                ChemicalViewer.getChemicalFromBIS(display, bis, chemFileDetails, chemType, config, logger, tp);
            }
            catch (Exception e) {
                display.append(e.getMessage());
            }
            fileDetails.setProperty("markup", chemFileDetails.getMarkup());
            fileDetails.setProperty("display", display.toString());
        }
        catch (Exception e) {
            display.append(e.getMessage());
        }
        fileDetails.setProperty("display", display.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void refreshFileObject(PropertyList fileDetails, String sdcid, String keyid1, String keyid2, PropertyList config, String connectionid, Logger logger) throws Exception {
        int attNum = -1;
        try {
            attNum = Integer.parseInt(fileDetails.getProperty("attachment"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        String worksheetitemid = keyid1;
        String chemType = fileDetails.getProperty("chemtype");
        if (attNum <= -1) throw new Exception("No attachment stored.");
        AttachmentProcessor arp = new AttachmentProcessor(connectionid);
        TranslationProcessor tp = new TranslationProcessor(connectionid);
        Attachment attachment = arp.getSDIAttachment(sdcid, keyid1, keyid2, "(null)", attNum);
        if (attachment == null) throw new Exception("Could not obtain attachment.");
        byte[] data = attachment.getData();
        if (data == null) throw new Exception("Attachment has no data. File may be invalid.");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);){
            if (fileDetails.getProperty("filename").equalsIgnoreCase(attachment.getFilename())) {
                try {
                    ChemicalViewerAjaxHandler.updateFileObject(fileDetails, bis, chemType, logger, config, worksheetitemid, tp);
                    return;
                }
                catch (Exception e) {
                    throw new Exception("Could not obtain stored file type.", e);
                }
            }
            logger.warn("Attachment file has changed. Update types.");
            fileDetails.setProperty("filename", attachment.getFilename());
            fileDetails.setProperty("description", attachment.getDescription());
            try {
                fileDetails.setProperty("shortfilename", new File(attachment.getFilename()).getName());
            }
            catch (Exception e) {
                fileDetails.setProperty("shortfilename", attachment.getFilename());
            }
            PropertyList oldmarkup = fileDetails.getPropertyList("markup");
            if (oldmarkup == null) {
                oldmarkup = new PropertyList();
            }
            fileDetails.setProperty("markup", oldmarkup);
            ChemicalViewerAjaxHandler.updateFileObject(fileDetails, bis, chemType, logger, config, worksheetitemid, tp);
            return;
        }
    }

    private static void generateFileObject(PropertyList fileDetails, ByteArrayInputStream bis, String filename, String chemType, String filedata, String attNum, PropertyList config, Logger logger, String worksheetitemid, TranslationProcessor tp) throws Exception {
        StringBuffer display = new StringBuffer();
        bis.reset();
        ChemicalFileDetails chemFileDetails = new ChemicalFileDetails();
        chemFileDetails.setConfig(config);
        try {
            ChemicalViewer.getChemicalFromBIS(display, bis, chemFileDetails, chemType, config, logger, tp);
        }
        catch (Exception e) {
            display.append(e.getMessage());
            fileDetails.setProperty("lasterror", e.getMessage());
            throw e;
        }
        fileDetails.setProperty("attachment", attNum);
        fileDetails.setProperty("filename", filename);
        fileDetails.setProperty("chemtype", chemType);
        fileDetails.setProperty("markup", chemFileDetails.getMarkup());
        fileDetails.setProperty("display", display.toString());
    }

    private PropertyList storeAsAttachment(String filedata, String sdcid, String keyid1, String keyid2, String filename, String chemType, String attNum, final PropertyList config, String elnpolicynode, final TranslationProcessor tp) throws Exception {
        final PropertyList outProp = new PropertyList();
        String filerepositoryid = "";
        String filerepositorynode = "";
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        PropertyList policy = configProcessor.getPolicy("ELNPolicy", elnpolicynode);
        if (policy != null && policy.containsKey("attachments")) {
            PropertyList attachments = policy.getPropertyList("attachments");
            filerepositoryid = attachments.getProperty("filerepositoryid", "");
            filerepositorynode = attachments.getProperty("filerepositorynode", "");
        }
        final String worksheetitemid = keyid1;
        final String ct = chemType;
        FileManager.storeAsAttachment(filedata, FileTypeGroup.CHEMICAL, sdcid, keyid1, keyid2, "", filename, attNum, filerepositoryid, filerepositorynode, new FileManager.AttachmentHandler(){

            @Override
            public void processAttachment(ByteArrayInputStream bis, String filename, FileTypeGroup fileType, String filedata, int attachmentNum) throws Exception {
                ChemicalViewerAjaxHandler.generateFileObject(outProp, bis, filename, ct, filedata, attachmentNum + "", config, ChemicalViewerAjaxHandler.this.logger, worksheetitemid, tp);
            }
        }, this.getConnectionId());
        return outProp;
    }

    public static String getUploadArea(String id, String preferredType) {
        StringBuffer area = new StringBuffer();
        area.append("Choose Chemical File Type<select id=\"" + id + "_chemtype\">");
        area.append("<option value=\"auto\">Auto Select</option>");
        for (int i = 0; i < ChemicalViewer.chemTypes.length; ++i) {
            String[] chemType = ChemicalViewer.chemTypes[i];
            area.append("<option title=\"" + chemType[ChemicalViewer.CHEMTYPE_HELP] + "\" " + (chemType[ChemicalViewer.CHEMTYPE_CODE].equalsIgnoreCase(preferredType) ? " selected " : "") + " value=\"" + chemType[ChemicalViewer.CHEMTYPE_CODE] + "\">" + chemType[ChemicalViewer.CHEMTYPE_TITLE] + "</option>");
        }
        area.append("</select>");
        area.append("<table style=\"width:100%\">");
        area.append("<tr>");
        area.append("<td width=\"50%\"><div id=\"" + id + "_uploader\" class=\"dropzone\" style=\"height:197px;width:100%\"></div></td>");
        area.append("<td width=\"50%\" style=\"vertical-align:top;padding-left:20px\">");
        area.append("<span style=\"\">Or use CTRL-V to paste your text here:</span>");
        area.append("<textarea style=\"height:175px;width:100%\" onpaste=\"chemicalViewer.paste( '" + id + "', this )\"></textarea>");
        area.append("</td></tr>");
        area.append("</table>");
        return area.toString();
    }

    public static String getEditArea(String id, PropertyList fileDetails, String keyid1, String keyid2, Logger logger, PropertyList config, boolean devMode, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"filetoolbar\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: rgb(246, 246, 246); border: 1px solid gray; padding: 2px;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left: 5px; padding-right: 5px;\"><tbody><tr>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"chemicalViewer.renderUpload('").append(id).append("');\" title=\"").append(tp.translate("Upload a new file")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_upload.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"chemicalViewer.doRefreshFile('").append(id).append("');\" title=\"").append(tp.translate("Revert file from attachment")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_return.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("</tr></tbody></table>");
        html.append("</td>");
        html.append("</tr></tbody></table>");
        html.append("</div>");
        html.append("<div style=\"background-color:#FFFFFF;border:solid 1px #A9A9A9;\">");
        html.append("<div style=\"padding:5px;\">");
        html.append(tp.translate("Uploaded File")).append(": <strong>").append(fileDetails.getProperty("description", fileDetails.getProperty("shortfilename", fileDetails.getProperty("filename")))).append("</strong> ").append(tp.translate("of type ")).append(fileDetails.getProperty("chemtype", "Unknown"));
        html.append("</div>");
        html.append("<div style=\"padding:5px;\">");
        PropertyList markup = fileDetails.getPropertyList("markup") != null ? fileDetails.getPropertyList("markup") : new PropertyList();
        html.append(tp.translate("Size")).append(" ");
        html.append("<input ").append("type=text size=3 value=\"").append(markup.getProperty("imagewidth", "0")).append("\" id=\"").append(id).append("_imagewidth\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">px");
        html.append(" ").append(tp.translate("by")).append(" ");
        html.append("<input  ").append("type=text size=3 value=\"").append(markup.getProperty("imageheight", "0")).append("\" id=\"").append(id).append("_imageheight\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">px");
        html.append("&nbsp;&nbsp;&nbsp;");
        html.append(tp.translate(" Atom Numbers: ")).append(" ");
        html.append("<select id=\"").append(id).append("_showatomnumbers\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">");
        html.append("<option value=\"Y\" " + (markup.getProperty("showatomnumbers", "N").equals("Y") ? " selected " : "") + ">" + tp.translate("Yes") + "</option>");
        html.append("<option value=\"N\" " + (markup.getProperty("showatomnumbers", "N").equals("N") ? " selected " : "") + ">" + tp.translate("No") + "</option>");
        html.append("</select>");
        html.append("&nbsp;&nbsp;&nbsp;" + tp.translate(" Atom Colors ")).append(" ");
        html.append("<select id=\"").append(id).append("_showatomcolors\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">");
        html.append("<option value=\"Y\" " + (markup.getProperty("showatomcolors", "N").equals("Y") ? " selected " : "") + ">" + tp.translate("Yes") + "</option>");
        html.append("<option value=\"N\" " + (markup.getProperty("showatomcolors", "N").equals("N") ? " selected " : "") + ">" + tp.translate("No") + "</option>");
        html.append("</select>");
        String carbon = markup.getProperty("showcarbons", "none");
        html.append("&nbsp;&nbsp;&nbsp;" + tp.translate(" Carbons: ")).append(" ");
        html.append("<select id=\"").append(id).append("_showcarbons\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">");
        html.append("<option value=\"none\" " + (carbon.equals("none") ? " selected " : "") + ">" + tp.translate("None") + "</option>");
        html.append("<option value=\"all\" " + (carbon.equals("all") ? " selected " : "") + ">" + tp.translate("All ") + "</option>");
        html.append("<option value=\"terminal\" " + (carbon.equals("terminal") ? " selected " : "") + ">" + tp.translate("Terminal") + "</option>");
        html.append("</select>");
        html.append("&nbsp;&nbsp;&nbsp;" + tp.translate(" Descriptors ")).append(" ");
        html.append("<select id=\"").append(id).append("_showdescriptors\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">");
        html.append("<option value=\"Y\" " + (markup.getProperty("showdescriptors", "N").equals("Y") ? " selected " : "") + ">" + tp.translate("Yes") + "</option>");
        html.append("<option value=\"N\" " + (markup.getProperty("showdescriptors", "N").equals("N") ? " selected " : "") + ">" + tp.translate("No") + "</option>");
        html.append("</select>");
        String highlight = markup.getProperty("highlightatoms");
        html.append("&nbsp;&nbsp;&nbsp;" + tp.translate(" Highlight Atoms ")).append(" ");
        html.append("<input id=\"").append(id).append("_highlightatoms\" style=\"width:120px\" value=\"" + highlight + "\" title=\"").append(tp.translate("Enter a list of atom numbers")).append("\" onchange=\"chemicalViewer.doChange(this,'").append(id).append("')\">");
        html.append("</select>");
        html.append("</div>");
        html.append("<div id=\"").append(id).append("_preview\" style=\"margin: 5px; padding:5px; border: solid 1px #CFDFF0;overflow:auto;height:305px;\">");
        html.append(ChemicalViewerAjaxHandler.getViewArea(fileDetails, keyid1, keyid2));
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    public static int getTotal(PropertyList markup) {
        int wt = 0;
        try {
            wt = Integer.parseInt(markup.getProperty("total", "0"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return wt;
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
                    if (markup != null && markup.getProperty("raw", "N").equalsIgnoreCase("Y")) {
                        url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
                    } else {
                        url = fileDetails.getProperty("display");
                        if (url.length() > 0) {
                            if (!url.startsWith("data:")) {
                                url = "data:" + url;
                            }
                        } else {
                            url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
                        }
                    }
                    html.append("<img src=\"").append(url).append("\" title=\"").append(fileDetails.get("filename")).append("\">");
                    break;
                }
                default: {
                    html.append(fileDetails.getProperty("display"));
                }
            }
        }
        return html.toString();
    }

    private static enum Mode {
        UPLOAD,
        UPLOADTEXT,
        RENDERUPLOAD,
        REFRESH;

    }
}

