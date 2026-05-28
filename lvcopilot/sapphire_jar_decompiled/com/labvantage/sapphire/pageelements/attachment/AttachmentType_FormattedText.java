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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AttachmentType_FormattedText
extends BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getDisplayValue() {
        return "M=Rich Text";
    }

    @Override
    public String getDisplayValue(String typeflag) {
        return "M=Rich Text";
    }

    @Override
    public String getLabel(String typeflag) {
        return "Rich Text";
    }

    @Override
    public String getHint(String typeflag, DataSet data, int row, String content, TranslationProcessor tp) {
        return tp.translate("Created by user") + " " + data.getValue(row, "modby", tp.translate("unknown"));
    }

    @Override
    public boolean isAllowedDownload(int row, DataSet attachment) {
        return true;
    }

    @Override
    public void getFilenameFieldInitialRender(AttachmentManager am, StringBuffer content, boolean viewOnly, boolean canRestore, boolean isLocked, int row, String type, String contentValue, PropertyList col, String name, String hint, TranslationProcessor tp, Browser browser) {
        content.append("<table><tr><td>");
        String formattedText = StringUtil.replaceAll(contentValue, "&", "&amp;");
        content.append("<textarea ").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_formattedtext\" name=\"formattedtext\" style=\" display:none;\"").append(" maxlength=\"2000\" onchange=\"sdiSetRowUpdate(event);\">").append(formattedText).append("</textarea>");
        content.append("<div ").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_formattedtext_div\" name=\"formattedtext_div\" style=\" overflow:hidden; overflow-y:hidden; overflow-x:hidden; top:0px; left:0px; height: 20px; width:").append(this.getWidth(col, -30)).append("px;\">").append(contentValue).append("</div>");
        content.append("</td><td>");
        content.append("<button ").append(viewOnly ? "disabled" : "").append(" type=\"button\" onclick=\"executeEdit_formattedText( ").append(row).append(" );\" id=\"").append(ATTACHMENT_CODE).append(row).append("_dotdotdot_btn\" style=\"height:18px;width=18px;font-size:8pt;\">").append("...").append("</button>");
        content.append("</td></tr></table>");
    }

    @Override
    public void getFilenameFieldTemplateRow(AttachmentManager am, StringBuffer content, PropertyList col, TranslationProcessor tp, Browser browser) {
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_contentdiv_formattedtext\" style=\"display:none;\">");
        content.append("<table><tr><td>");
        content.append("<textarea id=\"").append(ATTACHMENT_CODE).append("[__row]_formattedtext\" name=\"formattedtext\" style=\"display: none;").append("\" onchange=\"sdiSetRowUpdate(event);\" ></textarea>");
        content.append("<div ").append(" id=\"").append(ATTACHMENT_CODE).append("[__row]_formattedtext_div\" name=\"formattedtext_div\" style=\" overflow:hidden; overflow-y:hidden; overflow-x:hidden; top:0px; left:0px; height: 20px; width:").append(this.getWidth(col, -30)).append("px;\">").append("</div>");
        content.append("</td><td>");
        content.append("<button type=\"button\" onclick=\"executeEdit_formattedText(").append("[__row]").append(");\" id=\"").append(ATTACHMENT_CODE).append("[__row]_dotdotdot_btn\" style=\"height:18px;width=18px;font-size:8pt;\">").append("...").append("</button>");
        content.append("</td></tr></table>");
        content.append("</div>");
    }

    @Override
    public String getAllTypeflagList() {
        return "M";
    }

    @Override
    public void viewAttachment(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Attachment attachment) throws IOException {
    }

    @Override
    public void processGetAttachment(Attachment attachment, String connectionid) throws ServiceException {
    }

    @Override
    public String getContentValue(DataSet data, int row) {
        return data.getValue(row, "attachmentclob", "");
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
        String text = FileUpload.getFileItemValue(fileItems, "formattedtext");
        if (text != null) {
            if (text.length() > 0) {
                text = StringUtil.replaceAll(text, ";", "#semicolon#");
                HashMap map = addtionalFields != null && addtionalFields.size() > 0 ? (HashMap)addtionalFields.clone() : new HashMap();
                map.put("sdcid", sdcid);
                map.put("keyid1", keyid1);
                if (keyid2.length() > 0) {
                    map.put("keyid2", keyid2);
                }
                if (keyid3.length() > 0) {
                    map.put("keyid3", keyid3);
                }
                map.put("type", "M");
                map.put("attachmentclob", text);
                map.put("sourcefilename", "");
                map.put("description", description);
                map.put("attachmentclob", text);
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
                Logger.logError("No formattedtext provided.");
                errorMsg = "No formattedtext reference provided.";
            }
        } else {
            errorMsg = "No formattedtext provided.";
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
    public String editAttachment(String attnum, String description, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, HashMap additionalFields, List fileItems) {
        String error = "";
        if (attnum.length() > 0) {
            HashMap map;
            if (additionalFields != null && additionalFields.size() > 0) {
                String text = StringUtil.replaceAll((String)additionalFields.get("formattedtext"), ";", "#semicolon#");
                additionalFields.put("formattedtext", text);
                map = (HashMap)additionalFields.clone();
            } else {
                map = new HashMap();
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
            map.put("attachmentclob", map.get("formattedtext"));
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
    public String getHideContentDivJavascript() {
        return "            var oFormattedText = oDoc.getElementById( 'at' + iRow + '_contentdiv_formattedtext' );\n            if ( oFormattedText != null ) {\n                oFormattedText.style.display = 'none';\n            }";
    }

    @Override
    public String getShowContentDivJavascript() {
        return "            if ( typeflag == 'M' ) { // FormattedText text\n                var oFormattedText = oDoc.getElementById( 'at' + iRow + '_contentdiv_formattedtext' );\n                if ( oFormattedText != null ) {\n                    oFormattedText.style.display = 'block';\n                }\n            }";
    }

    @Override
    public StringBuffer renderActionColumn(int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp, Browser browser) {
        StringBuffer content = new StringBuffer("");
        if (attnum > -1) {
            content.append("<table id=\"actionBtnTable\" width=\"100%\"><tr>\n");
            content.append("<td width=\"25%\" id=\"viewTD\">");
            content.append("&nbsp;");
            content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("View attachment Formatted text")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\" onclick=\"executeView_formattedText( ").append(attnum).append(",'N',").append(row).append(" )\">");
            content.append("</td>");
            content.append("<td width=\"25%\" id=\"downloadTD\">");
            if (showDownloadIcon) {
                content.append("&nbsp;");
                content.append("<img id=\"btDownloadAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Download attachment to your PC")).append("\" src=\"WEB-CORE/images/gif/DownloadAttachment.gif\" onclick=\"executeView_formattedText( ").append(attnum).append(", 'Y',").append(row).append(" )\">");
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
    public String getOtherHtml() {
        return "";
    }

    @Override
    public StringBuffer getViewJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        html.append("function executeView_formattedText( sAttNum, sDownload, row ){\n");
        html.append("if ( sDownload == 'Y' ) {\n");
        html.append("var oForm = document.getElementById( 'viewform' );\n");
        html.append("oForm['attachmentnum'].value = sAttNum;\n");
        html.append("oForm['download'].value = sDownload;\n");
        html.append("oForm.target = '_blank';\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        html.append("else\n");
        html.append("{\n");
        html.append("var formattedTextEditorURL = 'rc?command=page&page=FormattedTextEditor';\n");
        html.append("var inputs = new Array();\n");
        html.append("inputs.push({name:'textvalue', id:'textvalue', value:sapphire.util.url.encodeComponent(document.getElementById( 'at' + row + '_formattedtext' ).value)});\n");
        html.append("inputs.push({name:'fieldid', id:'fieldid', value:'at' + row + '_formattedtext'});\n");
        html.append("inputs.push({name:'mode', id:'mode', value:'view'});\n");
        html.append("inputs.push({name:'rnd',id:'rnd', value: (Math.random() * 11) });\n");
        html.append("var oBtns = { 'Close': \"eval(closeWindow())\" };\n");
        html.append("var form = sapphire.ajax.util.createForm(formattedTextEditorURL, 'POST', inputs, document.body);\n");
        html.append("var oDialog = sapphire.ui.dialog.open('Rich Text Editor', formattedTextEditorURL, true, 800, 638, oBtns, form, true);\n");
        html.append("}\n");
        html.append("}\n");
        return html;
    }

    @Override
    public StringBuffer getEditJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        html.append("function executeEdit_formattedText( row ){\n");
        html.append("var formattedTextEditorURL = 'rc?command=page&page=FormattedTextEditor';\n");
        html.append("var inputs = new Array();\n");
        html.append("inputs.push({name:'textvalue', id:'textvalue', value:sapphire.util.url.encodeComponent(document.getElementById( 'at' + row + '_formattedtext' ).value)});\n");
        html.append("inputs.push({name:'fieldid', id:'fieldid', value:'at' + row + '_formattedtext'});\n");
        html.append("inputs.push({name:'mode', id:'mode', value:'edit'});\n");
        html.append("inputs.push({name:'rnd',id:'rnd', value: (Math.random() * 11) });\n");
        html.append("var oBtns = { 'OK': \"eval(ok())\", 'Close': \"eval(closeWindow())\" };\n");
        html.append("var form = sapphire.ajax.util.createForm(formattedTextEditorURL, 'POST', inputs, document.body);\n");
        html.append("var oDialog = sapphire.ui.dialog.open('Rich Text Editor', formattedTextEditorURL, true, 800, 680, oBtns, form, true);\n");
        html.append("}\n");
        return html;
    }
}

