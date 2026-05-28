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
import sapphire.xml.PropertyList;

public class AttachmentType_URL
extends BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getDisplayValue() {
        return "L=URL";
    }

    @Override
    public String getDisplayValue(String typeflag) {
        return "L=URL";
    }

    @Override
    public String getLabel(String typeflag) {
        return "URL";
    }

    @Override
    public String getHint(String typeflag, DataSet data, int row, String content, TranslationProcessor tp) {
        return tp.translate("Linked by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
    }

    @Override
    public boolean isAllowedDownload(int row, DataSet attachment) {
        return true;
    }

    @Override
    public void getFilenameFieldInitialRender(AttachmentManager am, StringBuffer content, boolean viewOnly, boolean canRestore, boolean isLocked, int row, String type, String contentValue, PropertyList col, String name, String hint, TranslationProcessor tp, Browser browser) {
        content.append("<input ").append(viewOnly ? "readonly" : "").append(" type=input id=\"").append(ATTACHMENT_CODE).append(row).append("_url\" name=\"url\" style=\"width:").append(this.getWidth(col, -30)).append("px;\" value=\"").append(contentValue.length() > 0 ? contentValue : tp.translate("no url selected")).append("\" onchange=\"sdiSetRowUpdate(event);\">");
        content.append("<button ").append(viewOnly ? "disabled" : "").append(" type=\"button\" id=\"").append(ATTACHMENT_CODE).append(row).append("_internalurl_browse__btn\" style=\"height:18px;width=18px;font-size:8pt;\" onclick=\"executeEdit_URL('").append(row).append("');\">").append("...").append("</button>");
    }

    @Override
    public void getFilenameFieldTemplateRow(AttachmentManager am, StringBuffer content, PropertyList col, TranslationProcessor tp, Browser browser) {
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_contentdiv_url\" style=\"display:none;\">");
        content.append("<input type=input id=\"").append(ATTACHMENT_CODE).append("[__row]_url\" name=\"url\" style=\"width:").append(this.getWidth(col, -30)).append("px;\" value=\"\" onchange=\"sdiSetRowUpdate(event);\">");
        content.append("<button type=\"button\" id=\"").append(ATTACHMENT_CODE).append("[__row]_internalurl_browse__btn\" style=\"height:18px;width=18px;font-size:8pt;\" onclick=\"executeEdit_URL('").append("[__row]');\">").append("...").append("</button>");
        content.append("</div>");
    }

    @Override
    public String getAllTypeflagList() {
        return "L";
    }

    @Override
    public String getOtherHtml() {
        return "";
    }

    @Override
    public void viewAttachment(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Attachment attachment) throws IOException {
    }

    @Override
    public void processGetAttachment(Attachment attachment, String connectionid) throws ServiceException {
    }

    @Override
    public String getContentValue(DataSet data, int row) {
        return data.getValue(row, "url", "");
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
        String url = FileUpload.getFileItemValue(fileItems, "url");
        if (url != null) {
            if (url.length() > 0) {
                Logger.logDebug("url = " + url);
                HashMap map = addtionalFields != null && addtionalFields.size() > 0 ? (HashMap)addtionalFields.clone() : new HashMap();
                map.put("sdcid", sdcid);
                map.put("keyid1", keyid1);
                if (keyid2.length() > 0) {
                    map.put("keyid2", keyid2);
                }
                if (keyid3.length() > 0) {
                    map.put("keyid3", keyid3);
                }
                map.put("type", "L");
                map.put("url", url);
                map.put("sourcefilename", "");
                map.put("description", description);
                ActionBlock actionBlock = new ActionBlock();
                try {
                    actionBlock.setAction("AddSDIAttachment", "AddSDIAttachment", "1", map);
                    new ActionProcessor(connectionId).processActionBlock(actionBlock);
                }
                catch (ActionException ae) {
                    Logger.logError("Could not save attachment with error:" + ae.getMessage());
                    errorMsg = ae.getMessage();
                }
            } else {
                Logger.logError("No URL reference provided.");
                errorMsg = "No URL reference provided.";
            }
        } else {
            errorMsg = "No URL provided.";
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
        return "            var oURL = oDoc.getElementById( 'at' + iRow + '_contentdiv_url' );\n            if ( oURL != null ) {\n                oURL.style.display = 'none';\n            }";
    }

    @Override
    public String getShowContentDivJavascript() {
        return "            if ( typeflag == 'L' ) { // URL\n                var oURL = oDoc.getElementById( 'at' + iRow + '_contentdiv_url' );\n                if ( oURL != null ) {\n                    oURL.style.display = 'block';\n                }\n            }";
    }

    @Override
    public StringBuffer renderActionColumn(int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp, Browser browser) {
        StringBuffer content = new StringBuffer("");
        if (attnum > -1) {
            content.append("<table id=\"actionBtnTable\" width=\"100%\"><tr>\n");
            content.append("<td width=\"25%\" id=\"viewTD\">");
            content.append("&nbsp;");
            content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("View attachment")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\" onclick=\"executeView_URL( ").append(attnum).append(", 'N' )\">");
            content.append("</td>");
            content.append("<td width=\"25%\" id=\"downloadTD\">");
            if (showDownloadIcon) {
                content.append("&nbsp;");
                content.append("<img id=\"btDownloadAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Download attachment to your PC")).append("\" src=\"WEB-CORE/images/gif/DownloadAttachment.gif\" onclick=\"executeView_URL( ").append(attnum).append(", 'Y',").append(row).append(" )\">");
            }
            content.append("</td>");
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
        html.append("function executeView_URL( sAttNum, sDownload, srow ){\n");
        html.append("var oForm = document.getElementById( 'viewform' );\n");
        html.append("oForm['attachmentnum'].value = sAttNum;\n");
        html.append("oForm['download'].value = sDownload;\n");
        html.append("oForm.target = '_blank';\n");
        html.append("if ( sDownload == 'Y' ) {\n");
        html.append("var url = document.getElementById('at'+( srow )+'_url').value;\n");
        html.append("if ( url.indexOf( 'rc?' ) != -1 ) {\n");
        html.append("sapphire.alert( sapphire.translate( 'Internal URL cannot be downloaded.' ) );\n");
        html.append("}\n");
        html.append("else {\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        html.append("}\n");
        html.append("else {\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        html.append("}\n");
        return html;
    }

    @Override
    public StringBuffer getEditJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        html.append("function executeEdit_URL( iRow ) {\n");
        html.append("var sFieldId = 'at'+iRow+'_url';\n");
        html.append("if ( typeof(sapphire) != 'undefined' && typeof(sapphire.ui) != 'undefined' ){\n");
        html.append("var iWidth = 650 + 25;\n");
        html.append("var iHeight = 170 + 40;\n");
        html.append("var sTitle = 'Lookup Link';\n");
        html.append("var sURL = 'rc?command=file&file=WEB-CORE/modules/webadmin/editorlookup_url.jsp&url=' + escape(document.getElementById(sFieldId).value);\n");
        html.append("var oBtns = { 'OK': 'this.dialog.frame.ok()', 'Cancel': 'this.dialog.frame.cancel()' };\n");
        html.append("var oDialog = sapphire.ui.dialog.open( sTitle,sURL + '&btns=N&dummy=' + new Date().getSeconds() + new Date().getMilliseconds(),true, iWidth, iHeight, oBtns );\n");
        html.append("oDialog.fieldName = sFieldId;\n");
        html.append("oDialog.propertyid = 'newurl';\n");
        html.append("oDialog.dialogArguments = [];\n");
        html.append("oDialog.dialogCallback = 'opener.editURL_Callback';\n");
        html.append("}\n");
        html.append("}\n");
        html.append("\n\n");
        html.append("function editURL_Callback ( oDialog ){\n");
        html.append("var oArgs = oDialog.dialogArguments;\n");
        html.append("if ( typeof( oArgs ) != undefined && oArgs != null ) {\n");
        html.append("var oField = document.getElementById(oDialog.fieldName);\n");
        html.append("var sNew = oArgs[ oDialog.propertyid ];\n");
        html.append("if ( sNew != undefined && sNew.length > 0 && oField != null ){\n");
        html.append("oField.value = sNew;\n");
        html.append("sapphire.events.fireEvent( oField, 'onchange');\n");
        html.append("sapphire.events.fireEvent( oField, 'onblur');\n");
        html.append("}\n");
        html.append("}\n");
        html.append("}\n");
        return html;
    }
}

