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

public class AttachmentType_PlainText
extends BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getDisplayValue() {
        return "P=Plain Text";
    }

    @Override
    public String getDisplayValue(String typeflag) {
        return "P=Plain Text";
    }

    @Override
    public String getLabel(String typeflag) {
        return "Plain Text";
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
    public String getOtherHtml() {
        StringBuffer content = new StringBuffer();
        content.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        content.append("<textarea id=\"gridtextarea\" onblur=\"hideGridTextArea()\" onmouseout=\"this.blur();this.onblur()\" onchange=\"currentTextAreaChanged( this )\" onkeyup=\"currentTextAreaChanged( this )\" rows=\"10\" cols=\"75\" style=\"border:1px solid;display: none; z-index: 100; position: absolute; top: 100px; left: 100px;width:475px\"></textarea>\n");
        return content.toString();
    }

    @Override
    public void getFilenameFieldInitialRender(AttachmentManager am, StringBuffer content, boolean viewOnly, boolean canRestore, boolean isLocked, int row, String type, String contentValue, PropertyList col, String name, String hint, TranslationProcessor tp, Browser browser) {
        content.append("<table><tr><td>");
        content.append("<textarea ").append(viewOnly ? "readonly" : "").append(" id=\"").append(ATTACHMENT_CODE).append(row).append("_plaintext\" name=\"plaintext\" style=\" top: 0px; left: 0px;overflow:hidden;overflow-y: hidden;overflow-x: hidden;width:").append(this.getWidth(col, -35)).append("px;\" maxlength=\"2000\" onclick=\"showGridTextArea( this, 2000 )\" rows=\"1\" cols=\"75\" onchange=\"sdiSetRowUpdate(event);\">").append(contentValue).append("</textarea>");
        content.append("</td><td>");
        content.append("<button ").append(viewOnly ? "disabled" : "").append(" type=\"button\" onclick=\"sapphire.cc.openEditor('").append(ATTACHMENT_CODE).append(row).append("_plaintext").append("', false, '").append(tp.translate("Plain Text Editor")).append("');\" id=\"").append(ATTACHMENT_CODE).append(row).append("_dotdotdot_btn\" style=\"height:18px;width=18px;font-size:8pt;\">").append(tp.translate("...")).append("</button>");
        content.append("</td></tr></table>");
    }

    @Override
    public void getFilenameFieldTemplateRow(AttachmentManager am, StringBuffer content, PropertyList col, TranslationProcessor tp, Browser browser) {
        content.append("<div id=\"").append(ATTACHMENT_CODE).append("[__row]_contentdiv_plaintext\" style=\"display:none;\">");
        content.append("<table><tr><td>");
        content.append("<textarea id=\"").append(ATTACHMENT_CODE).append("[__row]_plaintext\" name=\"plaintext\" style=\"top: 0px; left: 0px;overflow:hidden;overflow-y: hidden;overflow-x: hidden;width:").append(this.getWidth(col, -35)).append("px;\" maxlength=\"2000\" onclick=\"showGridTextArea( this, 2000 )\" rows=\"1\" cols\"75\" onchange=\"sdiSetRowUpdate(event);\" ></textarea>");
        content.append("</td><td>");
        content.append("<button type=\"button\" onclick=\"sapphire.cc.openEditor('").append(ATTACHMENT_CODE).append("[__row]_plaintext").append("', false, '").append(tp.translate("Plain Text Editor")).append("');\" id=\"").append(ATTACHMENT_CODE).append("[__row]_dotdotdot_btn\" style=\"height:18px;width=18px;font-size:8pt;\">").append("...").append("</button>");
        content.append("</td></tr></table>");
        content.append("</div>");
    }

    @Override
    public String getAllTypeflagList() {
        return "P";
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
        String text = FileUpload.getFileItemValue(fileItems, "plaintext");
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
                map.put("type", "P");
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
                Logger.logError("No plaintextprovided.");
                errorMsg = "No plaintext reference provided.";
            }
        } else {
            errorMsg = "No plaintext provided.";
        }
        return errorMsg;
    }

    @Override
    public String tempAttachmentMultiPart(HttpServletRequest request, HttpServletResponse response, String sdcid, String keyid1, String keyid2, String keyid3, String connectionId, List fileItems, JSONObject job) throws IOException {
        PrintWriter out = response.getWriter();
        String text = FileUpload.getFileItemValue(fileItems, "plaintext");
        if (text != null) {
            // empty if block
        }
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
            HashMap map = additionalFields != null && additionalFields.size() > 0 ? (HashMap)additionalFields.clone() : new HashMap();
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
            map.put("attachmentclob", map.get("plaintext"));
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
        return "            var oPlainText = oDoc.getElementById( 'at' + iRow + '_contentdiv_plaintext' );\n            if ( oPlainText != null ) {\n                oPlainText.style.display = 'none';\n            }";
    }

    @Override
    public String getShowContentDivJavascript() {
        return "            if ( typeflag == 'P' ) { // PLain text\n                var oPlainText = oDoc.getElementById( 'at' + iRow + '_contentdiv_plaintext' );\n                if ( oPlainText != null ) {\n                    oPlainText.style.display = 'block';\n                }\n            }";
    }

    @Override
    public StringBuffer renderActionColumn(int attnum, String contentValue, String row, boolean showEditIcon, boolean showDownloadIcon, boolean viewOnly, TranslationProcessor tp, Browser browser) {
        StringBuffer content = new StringBuffer("");
        if (attnum > -1) {
            content.append("<table id=\"actionBtnTable\" width=\"100%\"><tr>\n");
            content.append("<td width=\"25%\" id=\"viewTD\">");
            content.append("&nbsp;");
            content.append("<img id=\"btViewAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("View attachment Plain text")).append("\" src=\"WEB-CORE/images/gif/ViewAttachments.gif\" onclick=\"executeView_plainText( ").append(attnum).append(",'N',").append(row).append(" )\">");
            content.append("<td width=\"25%\" id=\"downloadTD\">");
            if (showDownloadIcon) {
                content.append("&nbsp;");
                content.append("<img id=\"btDownloadAction").append(row).append("\" class=\"btn_enabled\" title=\"").append(tp.translate("Download attachment to your PC")).append("\" src=\"WEB-CORE/images/gif/DownloadAttachment.gif\" onclick=\"executeView_plainText( ").append(attnum).append(", 'Y',").append(row).append(" )\">");
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
        html.append("//textarea stuff\nvar ta_currentgridtextarea = null;\nvar ta_gridtextarea = null;\nvar ta_currentmaxlength = null;\nfunction currentTextAreaChanged( e ) {\n    if ( ta_gridtextarea != null && ta_currentmaxlength != null && ta_currentmaxlength > 0 && ta_gridtextarea.value.length > ta_currentmaxlength ) {\n        ta_gridtextarea.value = ta_gridtextarea.value.substring( 0, ta_currentmaxlength );\n    }\n\telse if ( e.maxlength != null && e.value.length > e.maxlength ){\n\t    e.value = e.value.substring( 0, e.maxlength );\n\t    sapphire.events.fireEvent( e, 'onchange' );\n    }\n}\nfunction showGridTextArea( e, maxlength ) {\n    ta_currentgridtextarea = e;\n    ta_currentmaxlength = maxlength;\n\tta_gridtextarea = document.getElementById( 'gridtextarea' );\n    var oPB = document.getElementById('pagebody');\n    ta_gridtextarea.style.left = (dd_getOffsetLeft( ta_currentgridtextarea ) - ( oPB != null ? oPB.scrollLeft : 0 ) ) + 'px';\n    ta_gridtextarea.style.top =  (dd_getOffsetTop( ta_currentgridtextarea ) - ( oPB != null ? oPB.scrollTop : 0 ) ) + 'px';\n    ta_gridtextarea.cols = ta_currentgridtextarea.cols;\n    ta_gridtextarea.value = ta_currentgridtextarea.value;\n    ta_gridtextarea.readOnly = ta_currentgridtextarea.readOnly;\n    ta_gridtextarea.style.display = 'block';\n    ta_gridtextarea.focus();\n}\nfunction hideGridTextArea() {\n    if ( ta_currentgridtextarea && ta_gridtextarea ) {\n        ta_currentgridtextarea.value = ta_gridtextarea.value;\n        sapphire.events.fireEvent( ta_currentgridtextarea, 'onchange' );\n    }\n    ta_gridtextarea.style.display='none';\n}");
        html.append("function executeView_plainText( sAttNum, sDownload, row ){\n");
        html.append("if ( sDownload == 'Y' ) {\n");
        html.append("var oForm = document.getElementById( 'viewform' );\n");
        html.append("oForm['attachmentnum'].value = sAttNum;\n");
        html.append("oForm['download'].value = sDownload;\n");
        html.append("oForm.target = '_blank';\n");
        html.append("oForm.submit();\n");
        html.append("}\n");
        html.append("else\n");
        html.append("{\n");
        html.append("var sFieldName = 'at'+row+'_plaintext';\n");
        html.append("var sHTML='<textarea style=width:100%;height:100%;border-style:none;border-width:0px;padding-top:0px;margin-top:0px; name=viewPlainTextEditor id=viewPlainTextEditor>' + document.getElementById('at' + row + '_plaintext').value + '</textarea>'\n");
        html.append("var odialog = sapphire.ui.dialog.create( 500, 200, 'Plain Text Editor', sHTML, null, true, true, true, 0);\n");
        html.append("odialog.dialogType = 'alert';\n");
        html.append("return odialog;\n");
        html.append("}\n");
        html.append("}\n");
        return html;
    }

    @Override
    public StringBuffer getEditJavaScript() {
        StringBuffer html = new StringBuffer("\n");
        return html;
    }
}

