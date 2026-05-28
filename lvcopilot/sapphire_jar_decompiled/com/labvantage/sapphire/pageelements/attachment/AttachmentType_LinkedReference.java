/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.pageelements.attachment.AttachmentManager;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AttachmentType_LinkedReference
extends BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getDisplayValue() {
        return "D=Linked Reference";
    }

    @Override
    public String getDisplayValue(String typeflag) {
        return "D=Linked Reference";
    }

    @Override
    public String getLabel(String typeflag) {
        return "Linked Reference";
    }

    @Override
    public String getHint(String typeflag, DataSet data, int row, String content, TranslationProcessor tp) {
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("userid", data.getValue(row, "modby", tp.translate("unknown")));
        tokenMap.put("linksdcid", data.getValue(row, "linksdcid"));
        tokenMap.put("linkkeyid1", data.getValue(row, "linkkeyid1"));
        return tp.translate("Linked by user [userid] to [linksdcid] [linkkeyid1]", tokenMap);
    }

    @Override
    public boolean isAllowedEdit(int row, DataSet attachment) {
        return false;
    }

    @Override
    public boolean isAllowedDownload(int row, DataSet attachment) {
        return false;
    }

    @Override
    public String getOtherHtml() {
        return "";
    }

    @Override
    public void getFilenameFieldInitialRender(AttachmentManager am, StringBuffer content, boolean viewOnly, boolean canRestore, boolean isLocked, int row, String type, String contentValue, PropertyList col, String name, String hint, TranslationProcessor tp, Browser browser) {
        String[] contentParts = StringUtil.split(contentValue, ";");
        content.append("&nbsp;&nbsp;");
        content.append("<label for=\"linkedreftable\" style=\"font-size: 1em; margin-top: 0em; font-family: Verdana, Arial, Helvetica, 'MS Sans Serif'\">");
        content.append("from: ");
        content.append(contentParts.length == 5 ? contentParts[0] : "").append(" ");
        content.append(contentParts.length == 5 ? contentParts[1] : "");
        content.append(contentParts.length == 5 && contentParts[2].length() > 0 ? ";" + contentParts[2] : "");
        content.append(contentParts.length == 5 && contentParts[3].length() > 0 ? ";" + contentParts[3] : "");
        content.append("</label>");
        content.append("<table style=\"display: none;\" id=\"linkedreftable\"><tr><td>");
        content.append("SDC");
        content.append("</td><td>");
        content.append("<input readonly type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_linksdcid\" name=\"linksdcid\" style=\"width:").append(this.getWidth(col, 0) - 45).append("px;\" value=\"").append(contentParts.length == 5 ? contentParts[0] : "").append("\" onchange=\"sdiSetRowUpdate(event);\"><br>");
        content.append("</td></tr><tr><td>");
        content.append("KEYID1");
        content.append("</td><td>");
        content.append("<input readonly type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_linkkeyid1\" name=\"linkkeyid1\" style=\"width:").append(this.getWidth(col, 0) - 45).append("px;\" value=\"").append(contentParts.length == 5 ? contentParts[1] : "").append("\" onchange=\"sdiSetRowUpdate(event);\"><br>");
        content.append("</td></tr><tr><td>");
        content.append("KEYID2");
        content.append("</td><td>");
        content.append("<input readonly type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_linkkeyid2\" name=\"linkkeyid2\" style=\"width:").append(this.getWidth(col, 0) - 45).append("px;\" value=\"").append(contentParts.length == 5 ? contentParts[2] : "").append("\" onchange=\"sdiSetRowUpdate(event);\"><br>");
        content.append("</td></tr><tr><td>");
        content.append("KEYID3");
        content.append("</td><td>");
        content.append("<input readonly type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_linkkeyid3\" name=\"linkkeyid3\" style=\"width:").append(this.getWidth(col, 0) - 45).append("px;\" value=\"").append(contentParts.length == 5 ? contentParts[3] : "").append("\" onchange=\"sdiSetRowUpdate(event);\"><br>");
        content.append("</td></tr><tr><td>");
        content.append("#");
        content.append("</td><td>");
        content.append("<input readonly type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_linkattachmentnum\" name=\"linkattachmentnum\" style=\"width:").append(this.getWidth(col, 0) - 45).append("px;\" value=\"").append(contentParts.length == 5 ? contentParts[4] : "").append("\" onchange=\"sdiSetRowUpdate(event);\">");
        content.append("</td></tr></table>");
    }

    @Override
    public void getFilenameFieldTemplateRow(AttachmentManager am, StringBuffer content, PropertyList col, TranslationProcessor tp, Browser browser) {
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_contentdiv_linkedreference\" style=\"display:none;\">");
        content.append("<table><tr><td>");
        content.append("</td></tr></table>");
        content.append("</div>");
    }

    @Override
    public String getAllTypeflagList() {
        return "D";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void viewAttachment(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Attachment attachment) throws IOException {
        try (PrintWriter pw = response.getWriter();){
            String linkkeyid2 = attachment.getLinkKeyId2();
            String linkkeyid3 = attachment.getLinkKeyId3();
            String url = "rc?command=ViewAttachment&sdcid=" + attachment.getLinkSdcid() + "&keyid1=" + attachment.getLinkKeyId1() + (linkkeyid2 != null && linkkeyid2.length() > 0 ? "&keyid2=" + linkkeyid2 : "") + (linkkeyid3 != null && linkkeyid3.length() > 0 ? "&keyid3=" + linkkeyid3 : "") + "&attachmentnum=" + attachment.getLinkAttachmentNum();
            pw.println("<html><head><meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=" + url + "\"></head></html>");
            pw.flush();
        }
    }

    @Override
    public void processGetAttachment(Attachment attachment, String connectionid) throws ServiceException {
    }

    @Override
    public String getContentValue(DataSet data, int row) {
        return data.getValue(row, "linksdcid", "") + ";" + data.getValue(row, "linkkeyid1", "") + ";" + data.getValue(row, "linkkeyid2", "") + ";" + data.getValue(row, "linkkeyid3", "") + ";" + data.getValue(row, "linkattachmentnum", "");
    }

    @Override
    public String addAttachmentNormalRequest(HttpServletRequest request, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, String typeflag, String description, HashMap addtionalFields) {
        return "";
    }

    @Override
    public String postEditMultiPart(HttpServletRequest request, HttpServletResponse response, List fileItems, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, String description, String attNum, String typeflag, HashMap addtionalFields, String errorMsg) {
        return "";
    }

    @Override
    public String addAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, String description, String typeflag, HashMap addtionalFields, List fileItems, String errorMsg) {
        String linksdcid = FileUpload.getFileItemValue(fileItems, "linksdcid");
        String linkkeyid1 = FileUpload.getFileItemValue(fileItems, "linkkeyid1");
        String linkkeyid2 = FileUpload.getFileItemValue(fileItems, "linkkeyid2");
        String linkkeyid3 = FileUpload.getFileItemValue(fileItems, "linkkeyid3");
        String linkattachmentnum = FileUpload.getFileItemValue(fileItems, "linkattachmentnum");
        if (linksdcid != null && linksdcid.length() > 0 && linkkeyid1 != null && linkkeyid1.length() > 0) {
            Logger.logDebug("linksdcid = " + linksdcid + ", linkkeyid1=" + linkkeyid1);
            HashMap map = addtionalFields != null && addtionalFields.size() > 0 ? (HashMap)addtionalFields.clone() : new HashMap();
            map.put("sdcid", sdcid);
            map.put("keyid1", keyid1);
            if (keyid2.length() > 0) {
                map.put("keyid2", keyid2);
            }
            if (keyid3.length() > 0) {
                map.put("keyid3", keyid3);
            }
            map.put("type", "D");
            map.put("linksdcid", linksdcid);
            map.put("linkkeyid1", linkkeyid1);
            map.put("linkkeyid2", linkkeyid2);
            map.put("linkkeyid3", linkkeyid3);
            map.put("linkattachmentnum", linkattachmentnum);
            map.put("sourcefilename", "");
            map.put("description", description);
            ActionBlock actionBlock = new ActionBlock();
            try {
                actionBlock.setAction("AddSDIAttachment", "AddSDIAttachment", "1", map);
                new ActionProcessor(connectionId).processActionBlock(actionBlock);
            }
            catch (ActionException ae) {
                Logger.logError("Could not save attachment with error:" + ae.getMessage());
                errorMsg = "Could not save attachment.";
            }
        } else {
            Logger.logError("No Linked Reference provided.");
            errorMsg = "No Linked Reference provided.";
        }
        return errorMsg;
    }

    @Override
    public String tempAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, List fileItems, JSONObject job) throws IOException {
        return "";
    }

    @Override
    public byte[] getTempAttachment(String tempid, String connectionId) {
        return null;
    }

    @Override
    public String getHideContentDivJavascript() {
        return "            var oLinkedReference = oDoc.getElementById( 'at' + iRow + '_contentdiv_linkedreference' );\n            if ( oLinkedReference != null ) {\n                oLinkedReference.style.display = 'none';\n            }";
    }

    @Override
    public String getShowContentDivJavascript() {
        return "            if ( typeflag == 'D' ) { // URL\n                var oLinkedReference = oDoc.getElementById( 'at' + iRow + '_contentdiv_linkedreference' );\n                if ( oLinkedReference != null ) {\n                    oLinkedReference.style.display = 'block';\n                }\n                sapphire.ui.dialog.alert(sapphire.translate( 'Cannot add Linked Reference manually. Currently will be added only by copy-down mechanism' ));\n            }";
    }

    @Override
    public StringBuffer renderActionColumn(int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp, Browser browser) {
        StringBuffer content = new StringBuffer("");
        if (attnum > -1) {
            content.append("<table id=\"actionBtnTable\" width=\"100%\"><tr>\n");
            content.append("<td width=\"25%\" id=\"viewTD\">");
            content.append("&nbsp;");
            content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("View Linked Attachment")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\" onclick=\"executeView_LinkedReference( ").append(attnum).append(", 'N' )\">");
            content.append("</td>");
            content.append("<td width=\"25%\" id=\"downloadTD\">&nbsp;</td>");
            content.append("<td width=\"25%\" id=\"editTD\">&nbsp;</td>");
            String enablePromote = showEditIcon && !viewOnly ? "Y" : "N";
            content.append("<td width=\"25%\" id=\"showHistoryTD\">");
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
        html.append("function executeView_LinkedReference( sAttNum,sDownload  ){\n");
        html.append("var oForm = document.getElementById( 'viewform' );\n");
        html.append("oForm['attachmentnum'].value = sAttNum;\n");
        html.append("oForm['download'].value = sDownload;\n");
        html.append("oForm.target = '_blank';\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        return html;
    }

    @Override
    public StringBuffer getEditJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        return html;
    }
}

